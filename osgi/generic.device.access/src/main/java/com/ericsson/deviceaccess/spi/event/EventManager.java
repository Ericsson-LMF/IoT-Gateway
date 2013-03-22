package com.ericsson.deviceaccess.spi.event;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

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

import com.ericsson.deviceaccess.api.GenericDevice;
import com.ericsson.deviceaccess.api.GenericDeviceEventListener;

/**
 * Event manager that handles issuing if events at changes in properties.
 * Matches events against listeners filter (see
 * {@link GenericDeviceEventListener} for details).
 * 
 */
public class EventManager implements ServiceListener, Runnable,
		ServiceTrackerCustomizer {
	private static final Logger logger = LoggerFactory
			.getLogger(EventManager.class);
	private BundleContext context;
	private boolean shutdown = false;
	private final HashMap listeners = new HashMap();
	private final LinkedList events = new LinkedList();
	private final HashMap deltaValues = new HashMap();
	private ServiceTracker deviceTracker;
	private HashMap devices = new HashMap();

	private static final String REGEX_DELTA = "/state/([^/]+)$";

	private Thread thread = null;

	public EventManager() {
		super();
	}

	public void setContext(BundleContext context) {
		this.context = context;
	}

	/**
	 * Thread body that consumes the event queue and issues events to listeners.
	 */
	public void run() {
		// Start listening to GenericDeviceEventListener service registrations
		try {
			String filter = "(" + Constants.OBJECTCLASS + "="
					+ GenericDeviceEventListener.class.getName() + ")";
			context.addServiceListener(this, filter);

			// Check if there are already registered listeners
			ServiceReference[] srl = context.getServiceReferences(null, filter);
			for (int i = 0; srl != null && i < srl.length; i++) {
				serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, srl[i]));
			}
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}

		// Track GenericDevice service registrations (used to only allow events
		// from registered instances)
		deviceTracker = new ServiceTracker(context,
				GenericDevice.class.getName(), this);
		deviceTracker.open();

		// Wait for a GenericDeviceEvent to be received and forward this to all
		// listeners
		while (!shutdown) {
			GenericDeviceEvent event = null;
			synchronized (events) {
				if (events.size() == 0) {
					try {
						events.wait();
					} catch (InterruptedException e) {
						continue;
					}
				}
				if (events.size() > 0) {
					event = (GenericDeviceEvent) events.remove(0);
				}
			}

			if (event != null) {
				String deviceId = event.deviceId;
				String serviceName = event.serviceId;
				Properties matchingProperties = new Properties();
				;
				boolean propertyEvent = event.propertyEvent;

				if (serviceName == null || deviceId == null
						|| (event.properties == null && !propertyEvent))
					continue;

				if (!propertyEvent) {
					matchingProperties.put(
							GenericDeviceEventListener.DEVICE_ID, deviceId);
					matchingProperties.put(
							GenericDeviceEventListener.SERVICE_NAME,
							serviceName);
					matchingProperties.put(
							GenericDeviceEventListener.DEVICE_PROTOCOL,
							event.device.getProtocol());
					matchingProperties.put(
							GenericDeviceEventListener.DEVICE_URN,
							event.device.getURN());
					matchingProperties.put(
							GenericDeviceEventListener.DEVICE_NAME,
							event.device.getName());
					for (Enumeration i = event.properties.keys(); i
							.hasMoreElements();) {
						String key = (String) i.nextElement();
						matchingProperties.put(key, event.properties.get(key));
					}
				}

				synchronized (listeners) {
					for (Iterator i = listeners.keySet().iterator(); i
							.hasNext();) {
						GenericDeviceEventListener listener = (GenericDeviceEventListener) i
								.next();

						Filter filter = (Filter) listeners.get(listener);

						// Check to see if there are any delta-properties
						if (filter != null
								&& filter.toString().indexOf("__delta") > 0) {
							// Ugly parse to get the name of property with the
							// __delta suffix
							String filterstring = filter.toString();
							filterstring = filterstring.substring(0,
									filterstring.indexOf("__delta"));
							String deltaProperty = filterstring
									.substring(filterstring.lastIndexOf("(") + 1);

							// Is this an event update for the delta property?
							if (event.properties.get(deltaProperty) != null) {
								// Any old values saved to calculate delta from?
								if (deltaValues.get(event.deviceId
										+ event.serviceId + deltaProperty) != null) {
									if (matchingProperties.get(deltaProperty) instanceof Integer) {
										int oldValue = ((Integer) deltaValues
												.get(event.deviceId
														+ event.serviceId
														+ deltaProperty))
												.intValue();
										int newValue = ((Integer) matchingProperties
												.get(deltaProperty)).intValue();
										int delta = oldValue - newValue;

										if (delta < 0)
											delta = delta * -1; // Delta should
																// always be
																// positive

										event.properties
												.put(deltaProperty + "__delta",
														new Integer(delta));
										matchingProperties
												.put(deltaProperty + "__delta",
														new Integer(delta));
									} else if (matchingProperties
											.get(deltaProperty) instanceof Float) {
										Float oldValue = (Float) deltaValues
												.get(event.deviceId
														+ event.serviceId
														+ deltaProperty);
										Float newValue = (Float) matchingProperties
												.get(deltaProperty);

										if (newValue != null) {

											// Hack to get around bit errors
											// when doing subtract of floats
											float delta = (float) (Math
													.round(oldValue
															.floatValue() * 1000) - Math
													.round(newValue
															.floatValue() * 1000));
											delta = delta / 1000;

											if (delta < 0)
												delta = delta * -1; // Delta
																	// should
																	// always be
																	// positive

											event.properties.put(deltaProperty
													+ "__delta", new Float(
													delta));
											matchingProperties.put(
													deltaProperty + "__delta",
													new Float(delta));
										}
									}
								}
								deltaValues.put(event.deviceId
										+ event.serviceId + deltaProperty,
										matchingProperties.get(deltaProperty));
							}
						}

						try {
							// TODO: Prevent this from hanging
							if (propertyEvent) {
								if (event.propertyAdded)
									listener.notifyGenericDevicePropertyAddedEvent(
											deviceId, serviceName,
											event.propertyId);
								else
									listener.notifyGenericDevicePropertyRemovedEvent(
											deviceId, serviceName,
											event.propertyId);
							} else {
								if (filter == null
										|| filter.match(matchingProperties))
									listener.notifyGenericDeviceEvent(deviceId,
											serviceName, event.properties);
							}
						} catch (Throwable t) {
							logger.warn(
									"Exception when invoking event listener", t);
						}
					}
				}
			}

			// Event was received, forward it to all listeners
		}
	}

	/**
	 * Handle notifications of new/removed GenericDeviceEventListeners
	 * 
	 * @param event
	 */
	public void serviceChanged(ServiceEvent event) {
		ServiceReference sr = event.getServiceReference();
		GenericDeviceEventListener listener = (GenericDeviceEventListener) context
				.getService(sr);
		Object filterObj = sr
				.getProperty(GenericDeviceEventListener.GENERICDEVICE_FILTER);
		Filter filter = null;
		if (filterObj instanceof String) {
			String filterString = (String) filterObj;
			try {
				if (filterString != null) {
					filter = FrameworkUtil.createFilter(filterString);
				}
			} catch (InvalidSyntaxException e) {
				throw new IllegalArgumentException(
						"The filter string could not be parsed into a filter. "
								+ e);
			}
		} else if (filter == null || filterObj instanceof Filter) {
			filter = (Filter) filterObj;
		} else {
			throw new IllegalArgumentException(
					"The filter must either be a string or " + Filter.class);
		}

		switch (event.getType()) {
		case ServiceEvent.REGISTERED:
			synchronized (listeners) {
				listeners.put(listener, filter);
			}
			break;
		case ServiceEvent.MODIFIED:
			break;
		case ServiceEvent.UNREGISTERING:
			synchronized (listeners) {
				listeners.remove(listener);
			}
			break;
		}
	}

	/**
	 * Starts the event manager
	 */
	synchronized public void start() {
		this.thread = new Thread(this);
		this.shutdown = false;
		try {
			this.thread.start();
		} catch (Throwable e) {
			logger.warn("Failed to start Event Manager.");
			this.shutdown = true;
		}
	}

	/**
	 * Shuts down the event manager.
	 */
	synchronized public void shutdown() {
		shutdown = true;
		if (this.thread != null) {
			this.thread.interrupt();
			this.thread = null;
		}
		synchronized (events) {
			events.notifyAll();
		}

		if (deviceTracker != null)
			deviceTracker.close();
	}

	/**
	 * Notify about a changed state. To be called by protocol adaptors.
	 * 
	 * @param deviceId
	 * @param serviceId
	 * @param properties
	 */
	public void notifyGenericDeviceEvent(String deviceId, String serviceId,
			Dictionary properties) {
		if (shutdown) {
			logger.warn("Tried to notify event on closed event manager, dropping it!");
			return;
		}

		// Ignore events from devices that are not registered yet
		GenericDevice device = (GenericDevice) devices.get(deviceId);
		if (device == null)
			return;

		synchronized (events) {
			events.add(new GenericDeviceEvent(device, deviceId, serviceId,
					properties));
			events.notifyAll();
		}
	}

	public void notifyGenericDeviceEventRemoved(String deviceId,
			String serviceId, String propertyId) {
		if (shutdown) {
			logger.warn("Tried to notify event on closed event manager, dropping it!");
			return;
		}

		// Ignore events from devices that are not registered yet
		GenericDevice device = (GenericDevice) devices.get(deviceId);
		if (device == null)
			return;

		synchronized (events) {
			events.add(new GenericDeviceEvent(device, deviceId, serviceId,
					propertyId, false));
			events.notifyAll();
		}
	}

	public void notifyGenericDeviceEventAdded(String deviceId,
			String serviceId, String propertyId) {
		if (shutdown) {
			logger.warn("Tried to notify event on closed event manager, dropping it!");
			return;
		}

		// Ignore events from devices that are not registered yet
		GenericDevice device = (GenericDevice) devices.get(deviceId);
		if (device == null)
			return;

		synchronized (events) {
			events.add(new GenericDeviceEvent(device, deviceId, serviceId,
					propertyId, true));
			events.notifyAll();
		}
	}

	/**
	 * Internal class to hold an event
	 */
	private class GenericDeviceEvent {
		public String deviceId;
		public String serviceId;
		public Dictionary properties;
		public boolean propertyEvent = false;
		public String propertyId;
		public boolean propertyAdded;
		public GenericDevice device;

		public GenericDeviceEvent(GenericDevice device, String deviceId,
				String serviceId, Dictionary properties) {
			propertyEvent = false;
			this.device = device;
			this.deviceId = deviceId;
			this.serviceId = serviceId;
			this.properties = properties;
		}

		public GenericDeviceEvent(GenericDevice device, String deviceId,
				String serviceId, String propertyId, boolean propertyAdded) {
			propertyEvent = true;
			this.device = device;
			this.deviceId = deviceId;
			this.serviceId = serviceId;
			this.propertyId = propertyId;
			this.propertyAdded = propertyAdded;
			this.properties = new Hashtable();
			properties.put(GenericDeviceEventListener.DEVICE_ID, deviceId);
			properties.put(propertyId, new Object());
			properties.put(GenericDeviceEventListener.SERVICE_NAME, serviceId);
		}
	}

	public Object addingService(ServiceReference reference) {
		synchronized (devices) {
			final GenericDevice device = (GenericDevice) context
					.getService(reference);
			devices.put(device.getId(), device);

			// Always generate a state event when a new device is registered
			final Properties properties = new Properties() {
				{
					put(GenericDeviceEventListener.DEVICE_STATE,
							device.getState());
				}
			};
			notifyGenericDeviceEvent(device.getId(), "DeviceProperties",
					properties);

			return device;
		}
	}

	public void modifiedService(ServiceReference reference, Object service) {
	}

	public void removedService(ServiceReference reference, Object service) {
		synchronized (devices) {
			GenericDevice device = (GenericDevice) context
					.getService(reference);
			devices.remove(device.getId());
		}
	}
}
