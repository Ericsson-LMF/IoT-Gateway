/*
 * Copyright Ericsson AB 2011-2014. All Rights Reserved.
 *
 * The contents of this file are subject to the Lesser GNU Public License,
 *  (the "License"), either version 2.1 of the License, or
 * (at your option) any later version.; you may not use this file except in
 * compliance with the License. You should have received a copy of the
 * License along with this software. If not, it can be
 * retrieved online at https://www.gnu.org/licenses/lgpl.html. Moreover
 * it could also be requested from Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * BECAUSE THE LIBRARY IS LICENSED FREE OF CHARGE, THERE IS NO
 * WARRANTY FOR THE LIBRARY, TO THE EXTENT PERMITTED BY APPLICABLE LAW.
 * EXCEPT WHEN OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS AND/OR
 * OTHER PARTIES PROVIDE THE LIBRARY "AS IS" WITHOUT WARRANTY OF ANY KIND,

 * EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE ENTIRE RISK AS TO THE QUALITY AND PERFORMANCE OF THE
 * LIBRARY IS WITH YOU. SHOULD THE LIBRARY PROVE DEFECTIVE,
 * YOU ASSUME THE COST OF ALL NECESSARY SERVICING, REPAIR OR CORRECTION.
 *
 * IN NO EVENT UNLESS REQUIRED BY APPLICABLE LAW OR AGREED TO IN WRITING
 * WILL ANY COPYRIGHT HOLDER, OR ANY OTHER PARTY WHO MAY MODIFY AND/OR
 * REDISTRIBUTE THE LIBRARY AS PERMITTED ABOVE, BE LIABLE TO YOU FOR
 * DAMAGES, INCLUDING ANY GENERAL, SPECIAL, INCIDENTAL OR CONSEQUENTIAL
 * DAMAGES ARISING OUT OF THE USE OR INABILITY TO USE THE LIBRARY
 * (INCLUDING BUT NOT LIMITED TO LOSS OF DATA OR DATA BEING RENDERED
 * INACCURATE OR LOSSES SUSTAINED BY YOU OR THIRD PARTIES OR A FAILURE
 * OF THE LIBRARY TO OPERATE WITH ANY OTHER SOFTWARE), EVEN IF SUCH
 * HOLDER OR OTHER PARTY HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 */
package com.ericsson.deviceaccess.upnp;

import com.ericsson.deviceaccess.api.Constants;
import com.ericsson.deviceaccess.api.GenericDevice;
import com.ericsson.deviceaccess.api.GenericDeviceService;
import com.ericsson.deviceaccess.api.service.homeautomation.lighting.Dimming;
import com.ericsson.deviceaccess.api.service.homeautomation.power.SwitchPower;
import com.ericsson.deviceaccess.api.service.media.ContentDirectory;
import com.ericsson.deviceaccess.api.service.media.RenderingControl;
import com.ericsson.deviceaccess.spi.schema.SchemaBasedGenericDevice;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import org.apache.regexp.RE;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.upnp.UPnPDevice;
import org.osgi.service.upnp.UPnPEventListener;
import org.osgi.service.upnp.UPnPService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UPnPDeviceAgent implements UPnPEventListener {

    private final BundleContext context;
    private final SchemaBasedGenericDevice dev;
    private static final Logger logger = LoggerFactory.getLogger(UPnPDeviceAgent.class);
    private ServiceRegistration eventListenerReg;
    private ServiceRegistration devReg;

    public interface UpdatePropertyInterface {

        public void updateProperty(String name, Object value);
    }
    final private Map idToService = new HashMap();

    public UPnPDeviceAgent(BundleContext bc, UPnPDevice upnpdev) {
        this.context = bc;

        dev = new SchemaBasedGenericDevice() {
        };
        dev.setId(UPnPUtil.getUDN(upnpdev));
        dev.setOnline(true);
        dev.setName(UPnPUtil.getFriendlyName(upnpdev));
        dev.setProtocol("upnp");
        dev.setType(UPnPUtil.getDeviceType(upnpdev));
        dev.setURN((String) upnpdev.getDescriptions(null).get(UPnPDevice.UDN));

        dev.setManufacturer((String) upnpdev.getDescriptions(null).get(UPnPDevice.MANUFACTURER));
        dev.setSerialNumber((String) upnpdev.getDescriptions(null).get(UPnPDevice.SERIAL_NUMBER));
        dev.setModelName((String) upnpdev.getDescriptions(null).get(UPnPDevice.MODEL_NAME));
        String productClass = (String) upnpdev.getDescriptions(null).get(UPnPDevice.UPC);
        if (productClass == null) {
            productClass = "";
        } else {
            productClass = productClass.trim();
        }
        dev.setProductClass(productClass);

        dev.setService(getServices(upnpdev));

        String iconUrl = (String) upnpdev.getDescriptions(null).get("GDA_ICON");
        dev.setIcon(iconUrl);
    }

    public void update() {

    }

    public void start() {
        subscribeToEvents(UPnPFilterRule.deviceID(dev.getId()));
        dev.setState(GenericDevice.STATE_ADDED);
        devReg = context.registerService(GenericDevice.class, dev, dev.getDeviceProperties());
        dev.setState(GenericDevice.STATE_READY);
    }

    public void stop() {
        unsubscribeFromEvents();
        dev.setOnline(false);
        dev.setState(GenericDevice.STATE_REMOVED);
        if (devReg != null) {
            devReg.unregister();
            devReg = null;
        }
    }

    private void unsubscribeFromEvents() {
        if (this.eventListenerReg != null) {
            logger.debug("Unsubscribing from UPnP events");
            eventListenerReg.unregister();
            eventListenerReg = null;
        }
    }

    private void subscribeToEvents(UPnPFilterRule rule) {
        logger.debug("Subscribing to UPnP events");
        if (rule != null) {
            Filter filter;
            try {
                filter = context.createFilter(rule.toFilterRule());
                Dictionary<String, Object> props = new Hashtable<>();
                props.put(UPnPEventListener.UPNP_FILTER, filter);
                this.eventListenerReg = context.registerService(UPnPEventListener.class, this, props);
            } catch (InvalidSyntaxException e) {
                logger.error("Parsing failed: " + e);
            }
        }

    }

    // private HashMap<String, GenericDeviceService> getSWoTServices(UPnPDevice
    // dev) {
    private HashMap getServices(UPnPDevice dev) {
        // HashMap<String, GenericDeviceService> services = new HashMap<String,
        // GenericDeviceService>();
        HashMap services = new HashMap();
        if (UPnPUtil.isMediaRenderer(dev)) {
            logger.debug("Media Renderer is found");
            RenderingControl rc = new RenderingControlUPnPImpl(dev);
            services.put(rc.getName(), rc);

        } else if (UPnPUtil.isMediaServer(dev)) {
            logger.debug("Media Server is found");
            ContentDirectory cds = new ContentDirectoryUPnPImpl(dev);
            services.put(cds.getName(), cds);
        } else if (UPnPUtil.isDimmableLight(dev)) {
            UPnPService[] upnpServices = dev.getServices();
            logger.debug("Dimmable Light is found");
            for (int i = 0; i < upnpServices.length; ++i) {
                String serviceType = upnpServices[i].getType();
                String[] serviceTypeParts = UPnPUtil.parseServiceType(serviceType);
                if ((serviceTypeParts == null) || (serviceTypeParts.length < 4)) {
                    logger.debug("Unformatted service type: " + serviceType);
                    continue;
                }
                String type = serviceTypeParts[3];
                logger.debug("Serivce Type " + type);
                if (null != type) {
                    switch (type) {
                        case "DimmingService":
                            Dimming dim = new DimmingUPnPImpl(dev, upnpServices[i], logger);
                            services.put(dim.getName(), dim);
                            this.idToService.put(upnpServices[i].getId(), dim);
                            break;
                        case "SwitchPower":
                            SwitchPower switchPower = new SwitchPowerUPnPImpl(dev, upnpServices[i], logger);
                            services.put(switchPower.getName(), switchPower);
                            this.idToService.put(upnpServices[i].getId(), switchPower);
                            break;
                        default:
                            logger.debug("Unexpected service type: " + serviceType);
                            break;
                    }
                }
            }
        }
        return services;
    }

    @Override
    public void notifyUPnPEvent(String deviceId, String serviceId, Dictionary eventTable) {
        logger.debug("UPnP event received for " + deviceId + "#" + serviceId);
        if (deviceId.equals(dev.getId())) {
            GenericDeviceService svc = dev.getService(getSWoTServiceNameFromUPnPServiceId(serviceId));
            if (svc != null) {
                Dictionary lastChangeVariables = null;
                for (Enumeration events = eventTable.keys(); events.hasMoreElements();) {
                    String event = (String) events.nextElement();
                    if (event.equals("LastChange")) {
                        logger.debug("Received LastChange variables event");
                        Properties changedVars = UPnPUtil.parseLastChangeEvent((String) eventTable.get(event));
                        for (Enumeration vars = changedVars.keys(); vars.hasMoreElements();) {
                            String name = (String) vars.nextElement();
                            if (null != name) {
                                switch (name) {
                                    case "Volume":
                                        try {
                                            svc.getProperties().setStringValue("CurrentVolume", changedVars.getProperty(name));
                                        } catch (Exception e) {
                                            // TODO: Parsing error, it seems the string contains channel as well
                                        }
                                        break;
                                    case "AVTransportURI":
                                        // TODO: This does not work with the Noxon. It uses a different variable for this information
                                        svc.getProperties().setStringValue("CurrentUrl", changedVars.getProperty(name));
                                        break;
                                    case "CurrentTrackMetaData":
                                        // TODO: This does not work with the Noxon. It uses a different variable for this information
                                        String title = getMediaTitle(changedVars.getProperty(name));
                                        logger.debug("Media title is " + title);
                                        svc.getProperties().setStringValue("CurrentTitle", title);
                                        break;
                                    case "TransportState":
                                        String state = changedVars.getProperty(name).toLowerCase();
                                        if (null != state) {
                                            switch (state) {
                                                case "playing":
                                                    svc.getProperties().setStringValue("Status", "Playing");
                                                    break;
                                                case "stopped":
                                                    svc.getProperties().setStringValue("Status", "Stopped");
                                                    break;
                                                case "paused":
                                                    svc.getProperties().setStringValue("Status", "Paused");
                                                    break;
                                            }
                                        }
                                        break;
                                }
                            }
                        }
                        notifyUpdate(svc.getPath(true) + "/parameter");
                    }
                }
            }

            /*
             * Update properties of each service
             */
            Object service = this.idToService.get(serviceId);
            if ((service != null)
                    && (service instanceof UpdatePropertyInterface)
                    && (service instanceof GenericDeviceService)) {
                logger.debug("Found UpdatePropertyInterface instance");
                for (Enumeration events = eventTable.keys(); events.hasMoreElements();) {
                    String name = (String) events.nextElement();
                    Object value = eventTable.get(name);
                    ((UpdatePropertyInterface) service).updateProperty(name, value);
                    /*
                     logger.debug("Event: " + event + " = " + eventValue);
                     if ("LastChange".equals(event)) {
                     logger.debug("Received LastChange variables event");
                     Properties changedVars = UPnPUtil.parseLastChangeEvent((String)eventTable.get(event));
                     for (Enumeration vars = changedVars.keys(); vars.hasMoreElements();) {
                     String name = (String)vars.nextElement();
                     String value = (String)changedVars.getProperty(name);
                     }
                     }
                     */
                }
            }
        }
    }

    protected static String getMediaTitle(String didl) {
        RE titleRE = new RE("dc:title[>&gt;]+([^&]*)[&<lt;]+/dc:title");
        if (titleRE.match(didl)) {
            return titleRE.getParen(1);
        }
        return "";
    }

    private void notifyUpdate(String path) {
        if (devReg != null) {
            Dictionary<String, Object> props = dev.getDeviceProperties();
            props.put(Constants.UPDATED_PATH, path);
            devReg.setProperties(props);
        }

    }

    private String getSWoTServiceNameFromUPnPServiceId(String id) {
        if (id.indexOf(UPnPUtil.SRV_RENDERING_CONTROL) > 0 || id.indexOf(UPnPUtil.SRV_AV_TRANSPORT) > 0) {
            return "RenderingControl";
        } else if (id.indexOf(UPnPUtil.SRV_CONTENT_DIRECTORY) > 0) {
            return "ContentDirectory";
        }
        return "unsupported";
    }

}
