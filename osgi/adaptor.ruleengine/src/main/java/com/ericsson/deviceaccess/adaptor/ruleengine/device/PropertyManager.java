package com.ericsson.deviceaccess.adaptor.ruleengine.device;

import java.util.Calendar;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.ericsson.deviceaccess.api.GenericDevice;
import com.ericsson.deviceaccess.api.GenericDeviceEventListener;
import com.ericsson.deviceaccess.api.GenericDeviceService;

public class PropertyManager implements GenericDeviceEventListener, ServiceTrackerCustomizer {
	ServiceTracker deviceTracker;
	HashMap devices = new HashMap();
	Properties deviceProperties = new Properties();
	private BundleContext context;
	private ServiceRegistration sr;
	private RuleService ruleService;
	final Object mutex = new Object();
	static PropertyManager instance;
	
	public static PropertyManager getInstance() {
		return instance;
	}

	public void start(BundleContext context, RuleService rs) {
		instance = this;
		this.context = context;
		this.ruleService = rs;

		deviceTracker = new ServiceTracker(context, GenericDevice.class.getName(), this);
		deviceTracker.open();
		
		Properties props = new Properties();
		props.setProperty(GenericDeviceEventListener.GENERICDEVICE_FILTER, "(device.id=*)");
		sr = context.registerService(GenericDeviceEventListener.class.getName(), this, props);
	}
	
	public void stop() {
		ruleService = null;
		deviceTracker.close();
		sr.unregister();
	}
	
	public Properties getDeviceProperties() {
		return deviceProperties;
	}
	
	public void notifyGenericDeviceEvent(String deviceId, String serviceName, Dictionary properties) {
		if (properties == null)
			return;
		
		// Ignore updates of this property for performance reasons
		if (properties.get("lastUpdateTime") != null)
			return;
		
		synchronized (mutex) {
			if ("DeviceProperties".equals(serviceName) && properties.get(GenericDeviceEventListener.DEVICE_STATE) != null && properties.get(GenericDeviceEventListener.DEVICE_STATE).equals("Ready")) {
				GenericDevice device = (GenericDevice) devices.get(deviceId);
				if (device != null)
					updateDevice(device);
			} else {
				for (Enumeration i = properties.keys(); i.hasMoreElements(); ) {
					String propertyId = (String) i.nextElement();
					deviceProperties.put(deviceId + "." + serviceName + "." + propertyId, properties.get(propertyId));
					System.out.println(deviceId + "." + serviceName + "." + propertyId + "=" + properties.get(propertyId));
				}
			}
		}
		
		for (Enumeration i = properties.keys(); i.hasMoreElements(); ) {
			String propertyName = (String) i.nextElement();
			
			//TODO: Update weekDay and timeOfDay
			ruleService.handlePropertyUpdate(deviceProperties, deviceId, serviceName, propertyName);
		}
	}

	public void notifyGenericDevicePropertyAddedEvent(String deviceId, String serviceName, String propertyId) {
		// TOOD: Add property?
	}

	public void notifyGenericDevicePropertyRemovedEvent(String deviceId, String serviceName, String propertyId) {
		synchronized (mutex) {
			deviceProperties.remove(deviceId + "." + serviceName + "." +  propertyId);
		}
	}

	public Object addingService(ServiceReference reference) {
		GenericDevice device = (GenericDevice) context.getService(reference);
		devices.put(device.getId(), device);
		updateDevice(device);
		return null;
	}

	public void modifiedService(ServiceReference reference, Object service) {
	}

	public void removedService(ServiceReference reference, Object service) {
		GenericDevice device = (GenericDevice) context.getService(reference);

		synchronized (mutex) {
			// Remove all service properties
			String[] serviceNames = device.getServiceNames();
			for (int i = 0; i < serviceNames.length; i++) {
				String serviceName = serviceNames[i];
				GenericDeviceService srv = device.getService(serviceName);
				String[] propertyIds = srv.getProperties().getNames();
				for (int j = 0; j < propertyIds.length; j++) {
					String propertyId = propertyIds[j];
					Object value = srv.getProperties().getValue(propertyId);
					deviceProperties.remove(device.getId() + "." + serviceName + "." + propertyId);
				}
			}
			
			// Remove all device properties
			deviceProperties.remove(device.getId() + ".DeviceProperties.device.id");
			deviceProperties.remove(device.getId() + ".DeviceProperties.device.manufacturer");
			deviceProperties.remove(device.getId() + ".DeviceProperties.device.modelName");
			deviceProperties.remove(device.getId() + ".DeviceProperties.device.name");
			deviceProperties.remove(device.getId() + ".DeviceProperties.device.protocol");
			deviceProperties.remove(device.getId() + ".DeviceProperties.device.state");
			deviceProperties.remove(device.getId() + ".DeviceProperties.device.type");
			deviceProperties.remove(device.getId() + ".DeviceProperties.device.location");
			deviceProperties.remove(device.getId() + ".DeviceProperties.device.urn");
			deviceProperties.remove(device.getId() + ".DeviceProperties.device.online");
		}
	}

	private void updateDevice(GenericDevice device) {
		synchronized (mutex) {
			// Add or update all service properties
			String[] serviceNames = device.getServiceNames();
			for (int i = 0; i < serviceNames.length; i++) {
				String serviceName = serviceNames[i];
				GenericDeviceService service = device.getService(serviceName);
				String[] propertyIds = service.getProperties().getNames();
				for (int j = 0; j < propertyIds.length; j++) {
					String propertyId = propertyIds[j];
					if ("lastUpdateTime".equals(propertyId))
						continue;
					Object value = service.getProperties().getValue(propertyId);
					if (value != null) {
						System.out.println(device.getId() + "." + serviceName + "." + propertyId + "=" + value);
						deviceProperties.put(device.getId() + "." + serviceName + "." + propertyId, value);
					}
				}
			}
			
			// Add or update all device properties
			deviceProperties.put(device.getId() + ".DeviceProperties.device.id", device.getId());
			deviceProperties.put(device.getId() + ".DeviceProperties.device.manufacturer", device.getManufacturer());
			deviceProperties.put(device.getId() + ".DeviceProperties.device.modelName", device.getModelName());
			deviceProperties.put(device.getId() + ".DeviceProperties.device.name", device.getName());
			deviceProperties.put(device.getId() + ".DeviceProperties.device.protocol", device.getProtocol());
			deviceProperties.put(device.getId() + ".DeviceProperties.device.state", device.getState());
			deviceProperties.put(device.getId() + ".DeviceProperties.device.type", device.getType());
			deviceProperties.put(device.getId() + ".DeviceProperties.device.location", device.getLocation());
			deviceProperties.put(device.getId() + ".DeviceProperties.device.urn", device.getURN());
			deviceProperties.put(device.getId() + ".DeviceProperties.device.online", new Boolean(device.isOnline()));
		}
	}
}
