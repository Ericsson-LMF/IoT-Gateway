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
import com.ericsson.deviceaccess.api.GenericDevice.State;
import com.ericsson.deviceaccess.api.genericdevice.GDProperties;
import com.ericsson.deviceaccess.api.genericdevice.GDService;
import com.ericsson.deviceaccess.spi.schema.based.SBGenericDevice;
import com.ericsson.commonutil.LegacyUtil;
import com.ericsson.commonutil.function.FunctionalUtil;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
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

    private static final Logger logger = LoggerFactory.getLogger(UPnPDeviceAgent.class);

    protected static String getMediaTitle(String didl) {
        RE titleRE = new RE("dc:title[>&gt;]+([^&]*)[&<lt;]+/dc:title");
        if (titleRE.match(didl)) {
            return titleRE.getParen(1);
        }
        return "";
    }

    private final BundleContext context;
    private final SBGenericDevice device;
    private ServiceRegistration eventListenerReg;
    private ServiceRegistration devReg;

    final private Map<String, GDService> idToService = new HashMap<>();

    public UPnPDeviceAgent(BundleContext bc, UPnPDevice upnpdev) {
        this.context = bc;

        device = new SBGenericDevice() {
        };
        device.setId(UPnPUtil.getUDN(upnpdev));
        device.setOnline(true);
        device.setName(UPnPUtil.getFriendlyName(upnpdev));
        device.setProtocol("upnp");
        device.setType(UPnPUtil.getDeviceType(upnpdev));
        device.setURN((String) upnpdev.getDescriptions(null).get(UPnPDevice.UDN));

        device.setManufacturer((String) upnpdev.getDescriptions(null).get(UPnPDevice.MANUFACTURER));
        device.setSerialNumber((String) upnpdev.getDescriptions(null).get(UPnPDevice.SERIAL_NUMBER));
        device.setModelName((String) upnpdev.getDescriptions(null).get(UPnPDevice.MODEL_NAME));
        String productClass = (String) upnpdev.getDescriptions(null).get(UPnPDevice.UPC);
        if (productClass == null) {
            productClass = "";
        } else {
            productClass = productClass.trim();
        }
        device.setProductClass(productClass);

        device.setService(getServices(upnpdev));

        String iconUrl = (String) upnpdev.getDescriptions(null).get("GDA_ICON");
        device.setIcon(iconUrl);
    }

    public void update() {

    }

    public void start() {
        subscribeToEvents(UPnPFilterRule.deviceID(device.getId()));
        device.setState(State.ADDED);
        devReg = context.registerService(GenericDevice.class, device, LegacyUtil.toDictionary(device.getDeviceProperties()));
        device.setState(State.READY);
    }

    public void stop() {
        unsubscribeFromEvents();
        device.setOnline(false);
        device.setState(State.REMOVED);
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
            try {
                Filter filter = context.createFilter(rule.toFilterRule());
                Map<String, Object> props = new HashMap<>();
                props.put(UPnPEventListener.UPNP_FILTER, filter);
                this.eventListenerReg = context.registerService(UPnPEventListener.class, this, LegacyUtil.toDictionary(props));
            } catch (InvalidSyntaxException e) {
                logger.error("Parsing failed: " + e);
            }
        }

    }

    private Map<String, GDService> getServices(UPnPDevice dev) {
        HashMap<String, GDService> services = new HashMap<>();
        GDService service = null;
        if (UPnPUtil.isMediaRenderer(dev)) {
            logger.debug("Media Renderer is found");
            service = new RenderingControlUPnPImpl(dev);
        } else if (UPnPUtil.isMediaServer(dev)) {
            logger.debug("Media Server is found");
            service = new ContentDirectoryUPnPImpl(dev);
        } else if (UPnPUtil.isDimmableLight(dev)) {
            UPnPService[] upnpServices = dev.getServices();
            logger.debug("Dimmable Light is found");
            for (UPnPService upnpService : upnpServices) {
                String serviceType = upnpService.getType();
                String[] serviceTypeParts = UPnPUtil.parseServiceType(serviceType);
                if (serviceTypeParts == null || serviceTypeParts.length < 4) {
                    logger.debug("Unformatted service type: " + serviceType);
                    continue;
                }
                String type = serviceTypeParts[3];
                logger.debug("Serivce Type " + type);
                if (type != null) {
                    switch (type) {
                        case "DimmingService":
                            service = new DimmingUPnPImpl(dev, upnpService, logger);
                            break;
                        case "SwitchPower":
                            service = new SwitchPowerUPnPImpl(dev, upnpService, logger);
                            break;
                        default:
                            logger.debug("Unexpected service type: " + serviceType);
                            break;
                    }
                    if (service != null) {
                        idToService.put(upnpService.getId(), service);
                    }
                }
            }
        }
        if (service != null) {
            services.put(service.getName(), service);
        }
        return services;
    }

    @Override
    public void notifyUPnPEvent(String deviceId, String serviceId, Dictionary eventTable) {
        Map<String, String> events = LegacyUtil.toMap(eventTable);
        logger.debug("UPnP event received for " + deviceId + "#" + serviceId);
        if (deviceId.equals(device.getId())) {
            GDService svc = device.getService(getSWoTServiceNameFromUPnPServiceId(serviceId));
            if (svc != null) {
                GDProperties properties = svc.getProperties();
                events.forEach((event, data) -> {
                    if (event.equals("LastChange")) {
                        logger.debug("Received LastChange variables event");
                        UPnPUtil.parseLastChangeEvent(data).forEach((name, value) -> {
                            if (null != name) {
                                switch (name) {
                                    case "Volume":
                                        try {
                                            properties.setStringValue("CurrentVolume", value);
                                        } catch (Exception e) {
                                            // TODO: Parsing error, it seems the string contains channel as well
                                        }
                                        break;
                                    case "AVTransportURI":
                                        // TODO: This does not work with the Noxon. It uses a different variable for this information
                                        properties.setStringValue("CurrentUrl", value);
                                        break;
                                    case "CurrentTrackMetaData":
                                        // TODO: This does not work with the Noxon. It uses a different variable for this information
                                        String title = getMediaTitle(value);
                                        logger.debug("Media title is " + title);
                                        properties.setStringValue("CurrentTitle", title);
                                        break;
                                    case "TransportState":
                                        String state = value.toLowerCase();
                                        if (null != state) {
                                            switch (state) {
                                                case "playing":
                                                    properties.setStringValue("Status", "Playing");
                                                    break;
                                                case "stopped":
                                                    properties.setStringValue("Status", "Stopped");
                                                    break;
                                                case "paused":
                                                    properties.setStringValue("Status", "Paused");
                                                    break;
                                            }
                                        }
                                        break;
                                }
                            }
                        });
                        notifyUpdate(svc.getPath(true) + "/parameter");
                    }
                });
            }

            /*
             * Update properties of each service
             */
            GDService service = idToService.get(serviceId);
            if (service != null) {
                FunctionalUtil.doIfCan(UpdatePropertyInterface.class, service, s -> {
                    logger.debug("Found UpdatePropertyInterface instance");
                    events.forEach((name, value) -> {
                        s.updateProperty(name, value);
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
                    });

                });
            }
        }
    }

    private void notifyUpdate(String path) {
        if (devReg != null) {
            Map<String, Object> props = device.getDeviceProperties();
            props.put(Constants.UPDATED_PATH, path);
            devReg.setProperties(LegacyUtil.toDictionary(props));
        }

    }

    private String getSWoTServiceNameFromUPnPServiceId(String id) {
        if (id.contains(UPnPUtil.SRV_RENDERING_CONTROL) || id.contains(UPnPUtil.SRV_AV_TRANSPORT)) {
            return "RenderingControl";
        } else if (id.contains(UPnPUtil.SRV_CONTENT_DIRECTORY)) {
            return "ContentDirectory";
        }
        return "unsupported";
    }

    public interface UpdatePropertyInterface {

        public void updateProperty(String name, Object value);
    }

}
