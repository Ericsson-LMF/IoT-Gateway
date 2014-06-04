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
package com.ericsson.deviceaccess.spi.event;

import com.ericsson.deviceaccess.api.GenericDevice;
import com.ericsson.deviceaccess.api.GenericDeviceEventListener;
import static com.ericsson.deviceaccess.api.GenericDeviceEventListener.DEVICE_ID;
import static com.ericsson.deviceaccess.api.GenericDeviceEventListener.DEVICE_NAME;
import static com.ericsson.deviceaccess.api.GenericDeviceEventListener.DEVICE_PROTOCOL;
import static com.ericsson.deviceaccess.api.GenericDeviceEventListener.DEVICE_STATE;
import static com.ericsson.deviceaccess.api.GenericDeviceEventListener.DEVICE_URN;
import static com.ericsson.deviceaccess.api.GenericDeviceEventListener.GENERICDEVICE_FILTER;
import static com.ericsson.deviceaccess.api.GenericDeviceEventListener.SERVICE_NAME;
import com.ericsson.research.commonutil.function.TriMonoConsumer;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event manager that handles issuing if events at changes in properties.
 * Matches events against listeners filter (see
 * {@link GenericDeviceEventListener} for details).
 *
 */
public class EventManager implements ServiceListener, Runnable,
        ServiceTrackerCustomizer<GenericDevice, Object> {

    private static final Logger logger = LoggerFactory.getLogger(EventManager.class);
    private static final String REGEX_DELTA = "/state/([^/]+)$";
    private BundleContext context;
    //TODO: this starts from false due tests not calling start but run
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    private final Map<GenericDeviceEventListener, Filter> listeners = new ConcurrentHashMap<>();
    private final BlockingQueue<GenericDeviceEvent> events = new LinkedBlockingQueue<>();
    private final Map<String, Object> deltaValues = new HashMap<>();
    private ServiceTracker deviceTracker;
    private final Map<String, GenericDevice> devices = new ConcurrentHashMap<>();

    private Thread thread;
    private final GenericDeviceEvent POISON = new GenericDeviceEvent(null, null, null, null);
    private Filter ALLOW_ALL = new Filter() {

        @Override
        public boolean match(ServiceReference reference) {
            return true;
        }

        @Override
        public boolean match(Dictionary dictionary) {
            return true;
        }

        @Override
        public boolean matchCase(Dictionary dictionary) {
            return true;
        }

        @Override
        public boolean matches(Map<String, ?> map) {
            return true;
        }
    };

    public EventManager() {
        super();
    }

    public void setContext(BundleContext context) {
        this.context = context;
    }

    /**
     * Thread body that consumes the event queue and issues events to listeners.
     */
    @Override
    public void run() {
        startListenGenericDeviceEvents();
        createTracker();

        // Wait for a GenericDeviceEvent to be received and forward this to all listeners
        while (!shutdown.get()) {
            GenericDeviceEvent event;
            try {
                event = events.take();
            } catch (InterruptedException ex) {
                continue;
            }
            if (event == POISON) {
                break;
            }
            if (isEventInvalid(event)) {
                continue;
            }
            Dictionary<String, Object> matching = new Hashtable<>();
            addForNoPropertyEvent(event, matching);
            invokeListeners(event, matching);
        }
    }

    private void createTracker() {
        // Track GenericDevice service registrations (used to only allow events
        // from registered instances)
        deviceTracker = new ServiceTracker(context, GenericDevice.class.getName(), this);
        deviceTracker.open();
    }

    private boolean isEventInvalid(GenericDeviceEvent event) {
        if (event.serviceId == null || event.deviceId == null) {
            return true;
        }
        return event.properties == null && !event.propertyEvent;
    }

    private void addForNoPropertyEvent(GenericDeviceEvent event, Dictionary<String, Object> matching) {
        if (!event.propertyEvent) {
            matching.put(DEVICE_ID, event.deviceId);
            matching.put(SERVICE_NAME, event.serviceId);
            matching.put(DEVICE_PROTOCOL, event.device.getProtocol());
            matching.put(DEVICE_URN, event.device.getURN());
            matching.put(DEVICE_NAME, event.device.getName());
            for (Enumeration e = event.properties.keys(); e.hasMoreElements();) {
                String key = (String) e.nextElement();
                matching.put(key, event.properties.get(key));
            }
        }
    }

    private void invokeListeners(GenericDeviceEvent event, Dictionary<String, Object> matchingProperties) {
        String deviceId = event.deviceId;
        String serviceName = event.serviceId;
        listeners.forEach((listener, filter) -> {
            checkForDeltaProperty(filter, event, matchingProperties);
//            try {
            // TODO: "Prevent this from hanging"? This comment was here... But I changed things around...
            if (event.propertyEvent) {
                TriMonoConsumer<String> consumer;
                if (event.propertyAdded) {
                    consumer = listener::notifyGenericDevicePropertyAddedEvent;
                } else {
                    consumer = listener::notifyGenericDevicePropertyRemovedEvent;
                }
                consumer.consume(deviceId, serviceName, event.propertyId);
            } else if (filter.match(matchingProperties)) {
                System.out.println(deviceId + " " + serviceName + " " + event.properties);
                listener.notifyGenericDeviceEvent(deviceId, serviceName, event.properties);
            }
//            } catch (Exception ex) {
//                logger.warn("Exception when invoking event listener", ex);
//            }
        });
    }

    private void startListenGenericDeviceEvents() {
        try {
            String filter = "(" + Constants.OBJECTCLASS + "=" + GenericDeviceEventListener.class
                    .getName() + ")";
            context.addServiceListener(this, filter);

            // Check if there are already registered listeners
            ServiceReference[] references = context.getServiceReferences((String) null, filter);
            if (references != null) {
                for (ServiceReference reference : references) {
                    serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, reference));
                }
            }
        } catch (InvalidSyntaxException e) {
            logger.warn("Filter format was hardcoded wrong", e);
        }
    }

    private void checkForDeltaProperty(Filter filter, GenericDeviceEvent event, Dictionary<String, Object> matchingProperties) {
        if (filter != null) {
            String deltaProperty = filter.toString();
            if (deltaProperty.contains("__delta")) {
                deltaProperty = deltaProperty.replaceAll("__delta.*", "");
                deltaProperty = deltaProperty.replaceAll(".*\\(", "");
                String deltaString = deltaProperty + "__delta";

                Dictionary properties = event.properties;
                // Is this an event update for the delta property?
                if (properties.get(deltaProperty) != null) {
                    Object property = matchingProperties.get(deltaProperty);
                    String id = event.deviceId + event.serviceId + deltaProperty;
                    // Any old values saved to calculate delta from?
                    if (deltaValues.containsKey(id)) {
                        if (property instanceof Integer) {
                            int oldValue = (Integer) deltaValues.get(id);
                            int newValue = (Integer) property;
                            int delta = Math.abs(oldValue - newValue);
                            properties.put(deltaString, delta);
                            matchingProperties.put(deltaString, delta);
                        } else if (property instanceof Float) {
                            float oldValue = (Float) deltaValues.get(id);
                            float newValue = (Float) property;
                            float delta = Math.abs(substract(oldValue, newValue));
                            properties.put(deltaString, delta);
                            matchingProperties.put(deltaString, delta);
                        }
                    }
                    deltaValues.put(id, property);
                }
            }
        }
    }

    private float substract(float a, float b) {
        // Hack to get around bit errors
        // when doing subtract of floats
        float delta = (Math.round(a * 1000) - Math.round(b * 1000));
        return delta / 1000;
    }

    /**
     * Handle notifications of new/removed GenericDeviceEventListeners
     *
     * @param event
     */
    @Override
    public void serviceChanged(ServiceEvent event) {
        ServiceReference reference = event.getServiceReference();
        GenericDeviceEventListener listener = (GenericDeviceEventListener) context.getService(reference);
        Object filterObj = reference.getProperty(GENERICDEVICE_FILTER);
        Filter filter;
        if (filterObj instanceof String) {
            try {
                filter = FrameworkUtil.createFilter((String) filterObj);
            } catch (InvalidSyntaxException e) {
                throw new IllegalArgumentException("The filter string could not be parsed into a filter. " + e);
            }
        } else if (filterObj instanceof Filter) {
            filter = (Filter) filterObj;
        } else if (filterObj == null) {
            filter = ALLOW_ALL;
        } else {
            throw new IllegalArgumentException("The filter must be null, string or Filter");
        }
        switch (event.getType()) {
            case ServiceEvent.REGISTERED:
                listeners.put(listener, filter);
                break;
            case ServiceEvent.MODIFIED:
                break;
            case ServiceEvent.UNREGISTERING:
                listeners.remove(listener);
                break;
        }
    }

    /**
     * Starts the event manager
     */
    public void start() {
        synchronized (shutdown) {
            if (!shutdown.get()) {
                throw new IllegalStateException("There is thread already running.");
            }
            thread = new Thread(this);
            shutdown.set(false);
            try {
                thread.start();
            } catch (Throwable e) {
                logger.warn("Failed to start Event Manager.");
                shutdown.set(true);
            }
        }
    }

    /**
     * Shuts down the event manager.
     */
    public void shutdown() {
        synchronized (shutdown) {
//TODO: This cannot be done due tests... Functionality should stay same even if testa are fixed to allow this.
//            if (shutdown.get()) {
//                throw new IllegalStateException("There wasn't thread running to shutdown.");
//            }
            shutdown.set(true);
            events.add(POISON);
            thread = null;
            if (deviceTracker != null) {
                deviceTracker.close();
            }
        }
    }

    /**
     * Notify about a changed state. To be called by protocol adaptors.
     *
     * @param deviceId
     * @param serviceId
     * @param properties
     */
    public void addEvent(String deviceId, String serviceId, Dictionary properties) {
        addEvent(deviceId, device -> new GenericDeviceEvent(device, deviceId, serviceId, properties));
    }

    public void addRemoveEvent(String deviceId, String serviceId, String propertyId) {
        addEvent(deviceId, device -> new GenericDeviceEvent(device, deviceId, serviceId, propertyId, false));
    }

    public void addAddEvent(String deviceId, String serviceId, String propertyId) {
        addEvent(deviceId, device -> new GenericDeviceEvent(device, deviceId, serviceId, propertyId, true));
    }

    private void addEvent(String deviceId, Function<GenericDevice, GenericDeviceEvent> func) {
        if (shutdown.get()) {
            logger.warn("Tried to notify event on closed event manager, dropping it!");
            return;
        }
        // Ignore events from devices that are not registered yet
        GenericDevice device = devices.get(deviceId);
        if (device != null) {
            events.add(func.apply(device));
            System.out.println(events);
        } else {
            logger.warn("There was no device registered with deviceID: " + deviceId);
        }
    }

    @Override
    public Object addingService(ServiceReference<GenericDevice> reference) {
        GenericDevice device = context.getService(reference);
        devices.put(device.getId(), device);

        // Always generate a state event when a new device is registered
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put(DEVICE_STATE, device.getState());
        addEvent(device.getId(), "DeviceProperties", properties);
        return device;
    }

    @Override
    public void modifiedService(ServiceReference<GenericDevice> reference, Object service) {
    }

    @Override
    public void removedService(ServiceReference<GenericDevice> reference, Object service) {
        devices.remove(context.getService(reference).getId());
    }

    /**
     * Internal class to hold an event
     */
    private class GenericDeviceEvent {

        public String deviceId;
        public String serviceId;
        public Dictionary<String, Object> properties;
        public boolean propertyEvent;
        public String propertyId;
        public boolean propertyAdded;
        public GenericDevice device;

        GenericDeviceEvent(GenericDevice device, String deviceId, String serviceId, Dictionary<String, Object> properties) {
            propertyEvent = false;
            this.device = device;
            this.deviceId = deviceId;
            this.serviceId = serviceId;
            this.properties = properties;
        }

        GenericDeviceEvent(GenericDevice device, String deviceId, String serviceId, String propertyId, boolean propertyAdded) {
            propertyEvent = true;
            this.device = device;
            this.deviceId = deviceId;
            this.serviceId = serviceId;
            this.propertyId = propertyId;
            this.propertyAdded = propertyAdded;
            this.properties = new Hashtable<>();
            properties.put(GenericDeviceEventListener.DEVICE_ID, deviceId);
            properties.put(propertyId, new Object());
            properties.put(GenericDeviceEventListener.SERVICE_NAME, serviceId);
        }

        @Override
        public String toString() {
            if (propertyId != null) {
                return deviceId + " " + serviceId + " " + propertyId + " " + propertyAdded;
            } else {
                return deviceId + " " + serviceId + " " + properties;
            }
        }
    }
}
