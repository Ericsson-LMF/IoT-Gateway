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
package com.ericsson.deviceaccess.tutorial.rest;

import com.ericsson.common.util.LegacyUtil;
import com.ericsson.common.util.serialization.Format;
import com.ericsson.common.util.serialization.SerializationException;
import com.ericsson.common.util.serialization.SerializationUtil;
import com.ericsson.common.util.serialization.View;
import com.ericsson.deviceaccess.api.Constants;
import com.ericsson.deviceaccess.api.GenericDevice;
import com.ericsson.deviceaccess.api.genericdevice.GDAction;
import com.ericsson.deviceaccess.api.genericdevice.GDActionResult;
import com.ericsson.deviceaccess.api.genericdevice.GDEventListener;
import com.ericsson.deviceaccess.api.genericdevice.GDException;
import com.ericsson.deviceaccess.api.genericdevice.GDProperties;
import com.ericsson.deviceaccess.api.genericdevice.GDService;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class GenericDeviceServlet extends NanoHTTPD implements BundleActivator, GDEventListener, ServiceListener {

    private static Log logger = LogFactory.getLog(GenericDeviceServlet.class);

    private BundleContext context;
    private boolean shutdown;

//	private LogTracker logger = null;
    private ServiceRegistration sr;
    protected HashMap sockets;

    public GenericDeviceServlet(int port) {
        super(port);
    }

    public GenericDeviceServlet() {
        super(8090);
    }

    @Override
    public Response serve(String uri, String method, Properties header, Properties parms, Properties files) {
        logger.debug("REST: Get request for " + uri);
        String resource;
        try {
            if (uri.startsWith("/devices")) {
                if (uri.length() <= 9) {
                    resource = getAllDevices();
                    return new NanoHTTPD.Response(HTTP_OK, "application/json", resource);
                } else {
                    uri = uri.substring(8);
                    String deviceId;
                    String requestedNode = "";
                    int pathIndex = uri.indexOf('/', 1);
                    if (pathIndex < 0) {
                        deviceId = uri.substring(1);
                        GenericDevice dev = getDevice(deviceId);
                        if (dev == null) {
                            return new NanoHTTPD.Response(HTTP_NOTFOUND, "text/plain", "Device " + deviceId + " not found");
                        }
                        resource = dev.getSerializedNode(requestedNode, Format.JSON);
                        return new NanoHTTPD.Response(HTTP_OK, "application/json", resource);
                    } else {
                        deviceId = uri.substring(1, pathIndex);
                        requestedNode = uri.substring(pathIndex + 1);
                        return handleAction(deviceId, requestedNode, parms);
                    }
                }
            } else {
                return new NanoHTTPD.Response(HTTP_NOTFOUND, "text/plain", "No servlet registered for " + uri);
            }
        } catch (GDException | IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            return new NanoHTTPD.Response(HTTP_INTERNALERROR, "text/plain", "Failed to get resource " + uri + " due to " + sw.toString());
        } finally {
            logger.debug("REST: Returned");
        }
    }

    /**
     * @param req
     * @param dev
     * @throws IOException
     */
    private NanoHTTPD.Response handleAction(String deviceId, String request, Properties parms) throws IOException {
        StringTokenizer st = new StringTokenizer(request, "/?&");

        try {
            String service = st.nextToken();
            String action = st.nextToken();
            GenericDevice dev = getDevice(deviceId);
            if (dev != null) {
                GDService svc = dev.getService(service);
                if (svc != null) {
                    GDAction act = svc.getAction(action);
                    if (act != null) {
                        GDProperties properties = act.createArguments();
                        properties.setStringValue("device", deviceId);
                        properties.setStringValue("service", service);
                        properties.setStringValue("action", action);
                        properties.setStringValue("authorized", "true");
                        setArguments(properties, parms);
                        GDActionResult result = act.execute(properties);
                        return new NanoHTTPD.Response(HTTP_OK, "application/json", result.getValue().serialize(Format.JSON));
                    } else {
                        return new NanoHTTPD.Response(HTTP_NOTFOUND, "text/plain", "Could not find action: " + action);
                    }
                } else {
                    return new NanoHTTPD.Response(HTTP_NOTFOUND, "text/plain", "Could not find service: " + service);
                }
            } else {
                return new NanoHTTPD.Response(HTTP_NOTFOUND, "text/plain", "Could not find device: " + deviceId);
            }
        } catch (GDException e) {
            return new NanoHTTPD.Response(HTTP_INTERNALERROR, "text/plain", "Failed to perform action " + request + " due to " + e);
        }
    }

    /**
     * @param ac
     */
    private void setArguments(GDProperties ac, Properties parms) {
        Enumeration parameterNames = parms.keys();
        while (parameterNames.hasMoreElements()) {
            String paramName = (String) parameterNames.nextElement();
            String paramValue = parms.getProperty(paramName);
            ac.setStringValue(paramName, paramValue);
        }
    }

    private GenericDevice getDevice(String deviceId) {
        try {
            String idFilter = "(" + Constants.PARAM_DEVICE_ID + "=" + deviceId + ")";
            ServiceReference[] refs = context.getAllServiceReferences(GenericDevice.class.getName(), idFilter);
            if (refs != null) {
                logger.debug(refs.length + " device found");
                for (ServiceReference ref : refs) {
                    GenericDevice dev = (GenericDevice) context.getService(ref);
                    if (deviceId.equals(dev.getId())) {
                        return dev;
                    }
                }
            }
        } catch (InvalidSyntaxException e) {
            logger.error(e);
        }
        logger.error("No such device found");
        return null;
    }

    private String getAllDevices() {
        try {
            Map<String, GenericDevice> devices = context
                    .getServiceReferences(GenericDevice.class, null)
                    .stream()
                    .map(ref -> context.getService(ref))
                    .collect(Collectors.toMap(dev -> dev.getId(), Function.identity()));
            //TODO: Remove debug print
            for (ServiceReference s : context.getAllServiceReferences(null, null)) {
                System.out.println(s.getBundle().getSymbolicName());
            }
            try {
                //TODO: Figure out can ClassDefNotFoundException be fixed by what class loader
                Thread.currentThread().setContextClassLoader(devices.getClass().getClassLoader());
                SerializationUtil.get(Format.JSON)
                        .writerWithView(View.ID.Ignore.class)
                        .writeValueAsString(devices);
            } catch (JsonProcessingException e) {
                logger.error(e);
            }
            return SerializationUtil.execute(Format.JSON, mapper -> mapper.writerWithView(View.ID.Ignore.class).writeValueAsString(devices));
        } catch (InvalidSyntaxException | SerializationException e) {
            logger.error(e);
        }
        return "{}";
    }

    @Override
    public void start(BundleContext bc) throws Exception {
        context = bc;
        Map<String, Object> props = new HashMap<>();
        props.put(GDEventListener.GENERICDEVICE_FILTER, "(device.id=*)");
        sr = bc.registerService(GDEventListener.class, this, LegacyUtil.toDictionary(props));

        try {
            bc.addServiceListener(this, "(" + org.osgi.framework.Constants.OBJECTCLASS + "=" + GenericDevice.class.getName() + ")");
        } catch (InvalidSyntaxException e) {
            logger.error(e);
        }
    }

    @Override
    public void stop(BundleContext bc) throws Exception {
        shutdown = true;
        sr.unregister();
        shutdown();
    }

    @Override
    public void notifyGDEvent(String deviceId, String serviceName, Map<String, Object> properties) {
        logger.debug("REST Servlet received event: " + deviceId + ", " + serviceName + ", " + properties);
    }

    @Override
    public void notifyGDPropertyEvent(Type type, String deviceId, String serviceName, String propertyId) {
        logger.debug(type + " property " + propertyId + " from " + deviceId + "." + propertyId);
    }

    @Override
    public void serviceChanged(ServiceEvent event) {
        ServiceReference sr = event.getServiceReference();
        switch (event.getType()) {
            case ServiceEvent.REGISTERED: {
                GenericDevice device = (GenericDevice) context.getService(sr);
//			logger.log(LogService.LOG_DEBUG, "added:" + device.getId());
                logger.debug("added:" + device.getId());
            }
            break;
            case ServiceEvent.UNREGISTERING: {
                GenericDevice device = (GenericDevice) context.getService(sr);
//			logger.log(LogService.LOG_DEBUG, "removed:" + device.getId());
                logger.debug("removed:" + device.getId());
            }
            break;
            default:
                break;
        }
    }
}
