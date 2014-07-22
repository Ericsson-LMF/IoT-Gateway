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
import com.ericsson.deviceaccess.api.genericdevice.GDEventListener;
import static com.ericsson.deviceaccess.api.genericdevice.GDEventListener.DEVICE_ID;
import static com.ericsson.deviceaccess.api.genericdevice.GDEventListener.DEVICE_NAME;
import static com.ericsson.deviceaccess.api.genericdevice.GDEventListener.DEVICE_PROTOCOL;
import static com.ericsson.deviceaccess.api.genericdevice.GDEventListener.DEVICE_STATE;
import static com.ericsson.deviceaccess.api.genericdevice.GDEventListener.DEVICE_URN;
import static com.ericsson.deviceaccess.api.genericdevice.GDEventListener.GENERICDEVICE_FILTER;
import static com.ericsson.deviceaccess.api.genericdevice.GDEventListener.SERVICE_NAME;
import com.ericsson.deviceaccess.api.genericdevice.GDEventListener.Type;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import static org.osgi.framework.ServiceEvent.MODIFIED;
import static org.osgi.framework.ServiceEvent.REGISTERED;
import static org.osgi.framework.ServiceEvent.UNREGISTERING;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event manager that handles issuing if events at changes in properties.
 * Matches events against listeners filter (see {@link GDEventListener} for
 * details).
 *
 */
public class EventManager implements ServiceListener, Runnable,
        ServiceTrackerCustomizer<GenericDevice, Object> {

    private static final Logger logger = LoggerFactory.getLogger(EventManager.class);
    private static final String LISTENER_FILTER = "(" + Constants.OBJECTCLASS + "=" + GDEventListener.class
            .getName() + ")";
    private static final Pattern DELTA_PATTERN = Pattern.compile("\\((([^(]*)__delta)");
    private BundleContext context;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Map<GDEventListener, Filter> listeners = new ConcurrentHashMap<>();
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

        GenericDeviceEvent event;
        while (running.get()) {
            try {
                // Wait for a GenericDeviceEvent to be received and forward this to all listeners
                event = events.take();
            } catch (InterruptedException ex) {
                continue;
            }
            //POISON is invalid and because running is set to false this exits.
            if (isEventInvalid(event)) {
                continue;
            }
            Map<String, Object> matching = new HashMap<>();
            if (!event.propertyEvent) {
                addForChangeEvent(event, matching);
            }
            invokeListeners(event, matching);
        }
    }

    /**
     * Track GenericDevice service registrations (used to only allow events from
     * registered instances)
     */
    private void createTracker() {
        deviceTracker = new ServiceTracker(context, GenericDevice.class.getName(), this);
        deviceTracker.open();
    }

    /**
     * Checks if event is invalid
     *
     * @param event
     * @return is invalid?
     */
    private boolean isEventInvalid(GenericDeviceEvent event) {
        if (event.serviceId == null || event.deviceId == null) {
            return true;
        }
        return event.properties == null && !event.propertyEvent;
    }

    /**
     * Adds properties needed in state change events
     *
     * @param event
     * @param matching
     */
    private void addForChangeEvent(GenericDeviceEvent event, Map<String, Object> matching) {
        matching.put(DEVICE_ID, event.deviceId);
        matching.put(SERVICE_NAME, event.serviceId);
        matching.put(DEVICE_PROTOCOL, event.device.getProtocol());
        matching.put(DEVICE_URN, event.device.getURN());
        matching.put(DEVICE_NAME, event.device.getName());
        matching.putAll(event.properties);
    }

    /**
     * Invokes listeners that listen this kind of event
     *
     * @param event
     * @param matchingProperties
     */
    private void invokeListeners(GenericDeviceEvent event, Map<String, Object> matchingProperties) {
        String deviceId = event.deviceId;
        String serviceName = event.serviceId;
        listeners.forEach((listener, filter) -> {
            checkForDeltaProperty(filter, event, matchingProperties);
            if (event.propertyEvent) {
                listener.notifyGDPropertyEvent(event.type, deviceId, serviceName, event.propertyId);
            } else if (filter.matches(matchingProperties)) {
                System.out.println(event);
                listener.notifyGDEvent(deviceId, serviceName, event.properties);
            }
        });
    }


    /**
     * Registers EventManager to listen generic device events
     */
    private void startListenGenericDeviceEvents() {
        try {
            context.addServiceListener(this, LISTENER_FILTER);

            // Check if there are already registered listeners
            context.getServiceReferences(GDEventListener.class, null)
                    .forEach(reference -> serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, reference)));
        } catch (InvalidSyntaxException e) {
            logger.warn("Filter format was hardcoded wrong", e);
        }
    }


    /**
     * If filter and event is for delta property, this then updates it
     *
     * @param filter
     * @param event
     * @param matchingProperties
     */
    private void checkForDeltaProperty(Filter filter, GenericDeviceEvent event, Map<String, Object> matchingProperties) {
        if (filter != null) {
            String deltaProperty = filter.toString();
            Matcher matcher = DELTA_PATTERN.matcher(deltaProperty);
            if (matcher.find()) {
                deltaProperty = matcher.group(2);
                // Is this an event update for the delta property?
                if (event.properties.get(deltaProperty) != null) {
                    updateDelta(matchingProperties, deltaProperty, event, matcher);
                }
            }
        }
    }

    /**
     * Updates delta property
     *
     * @param matchingProperties
     * @param deltaProperty
     * @param event
     * @param matcher
     */
    private void updateDelta(Map<String, Object> matchingProperties, String deltaProperty, GenericDeviceEvent event, Matcher matcher) {
        Object newProperty = matchingProperties.get(deltaProperty);
        String id = event.deviceId + event.serviceId + deltaProperty;
        // Any old values saved to calculate delta from?
        if (deltaValues.containsKey(id)) {
            Object delta = calculateDelta(newProperty, deltaValues.get(id));
            if (delta != null) {
                String deltaString = matcher.group(1);
                event.properties.put(deltaString, delta);
                matchingProperties.put(deltaString, delta);
            }
        }
        deltaValues.put(id, newProperty);
    }

    /**
     * Calculates delta value between new and old values
     *
     * @param newPropert
     * @param oldProperty
     * @return delta
     */
    private Object calculateDelta(Object newPropert, Object oldProperty) {
        if (newPropert instanceof Integer) {
            return Math.abs((Integer) oldProperty - (Integer) newPropert);
        } else if (newPropert instanceof Float) {
            return Math.abs(substract((Float) oldProperty, (Float) newPropert));
        }
        return null;
    }

    /**
     * Hack to get around bit errors when doing subtract of floats
     *
     * @param a
     * @param b
     * @return a - b
     */
    private float substract(float a, float b) {
        float delta = Math.round(a * 1000) - Math.round(b * 1000);
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
        GDEventListener listener = (GDEventListener) context.getService(reference);
        switch (event.getType()) {
            case REGISTERED:
                Object filter = reference.getProperty(GENERICDEVICE_FILTER);
                listeners.put(listener, getFilter(filter));
                break;
            case MODIFIED:
                break;
            case UNREGISTERING:
                listeners.remove(listener);
                break;
        }
    }

    /**
     * Gets filter from an object
     *
     * @param object
     * @return filter
     */
    private Filter getFilter(Object object) {
        if (object instanceof String) {
            try {
                return FrameworkUtil.createFilter((String) object);
            } catch (InvalidSyntaxException e) {
                throw new IllegalArgumentException("The filter string could not be parsed into a filter", e);
            }
        } else if (object instanceof Filter) {
            return (Filter) object;
        } else if (object == null) {
            return ALLOW_ALL;
        } else {
            throw new IllegalArgumentException("The filter must be null, string or Filter");
        }
    }

    /**
     * Starts the event manager.
     */
    public void start() {
        synchronized (running) {
            if (!running.compareAndSet(false, true)) {
                throw new IllegalStateException("There is thread already running");
            }
            thread = new Thread(this);
            try {
                thread.start();
            } catch (Throwable e) {
                logger.warn("Failed to start Event Manager: " + e);
                running.set(false);
                thread = null;
            }
        }
    }

    /**
     * Shuts the event manager.
     */
    public void shutdown() {
        synchronized (running) {
            if (!running.compareAndSet(true, false)) {
                throw new IllegalStateException("There wasn't thread running to shutdown");
            }
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
    public void addPropertyEvent(String deviceId, String serviceId, Map<String, Object> properties) {
        addEvent(deviceId, device -> new GenericDeviceEvent(device, deviceId, serviceId, properties));
    }

    public void addStateEvent(String deviceId, String serviceId, String propertyId, Type type) {
        addEvent(deviceId, device -> new GenericDeviceEvent(device, deviceId, serviceId, propertyId, type));
    }

    /**
     * Adds event to be received by listeners from existing devices only
     *
     * @param deviceId
     * @param func
     */
    private void addEvent(String deviceId, Function<GenericDevice, GenericDeviceEvent> func) {
        if (running.get()) {
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
        Map<String, Object> properties = new HashMap<>();
        properties.put(DEVICE_STATE, device.getState());
        addPropertyEvent(device.getId(), "DeviceProperties", properties);
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
        public Map<String, Object> properties;
        public boolean propertyEvent;
        public String propertyId;
        public Type type;
        public GenericDevice device;

        GenericDeviceEvent(GenericDevice device, String deviceId, String serviceId, Map<String, Object> properties) {
            propertyEvent = false;
            this.device = device;
            this.deviceId = deviceId;
            this.serviceId = serviceId;
            this.properties = properties;
        }

        GenericDeviceEvent(GenericDevice device, String deviceId, String serviceId, String propertyId, Type type) {
            propertyEvent = true;
            this.device = device;
            this.deviceId = deviceId;
            this.serviceId = serviceId;
            this.propertyId = propertyId;
            this.type = type;
            this.properties = new HashMap<>();
            properties.put(GDEventListener.DEVICE_ID, deviceId);
            properties.put(propertyId, new Object());
            properties.put(GDEventListener.SERVICE_NAME, serviceId);
        }

        @Override
        public String toString() {
            if (propertyId == null) {
                return deviceId + " " + serviceId + " " + properties;
            }
            return deviceId + " " + serviceId + " " + propertyId + " " + type;
        }
    }
}
