package com.ericsson.deviceaccess.upnp;

import com.ericsson.deviceaccess.api.Constants;
import com.ericsson.deviceaccess.api.GenericDevice;
import com.ericsson.deviceaccess.api.GenericDeviceProperties;
import com.ericsson.deviceaccess.api.GenericDeviceService;
import com.ericsson.deviceaccess.api.service.homeautomation.lighting.Dimming;
import com.ericsson.deviceaccess.api.service.homeautomation.power.SwitchPower;
import com.ericsson.deviceaccess.api.service.media.ContentDirectory;
import com.ericsson.deviceaccess.api.service.media.RenderingControl;
import com.ericsson.deviceaccess.spi.schema.SchemaBasedGenericDevice;

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

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

public class UPnPDeviceAgent implements UPnPEventListener {
	
	
	private BundleContext context;
	private SchemaBasedGenericDevice dev;
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
		if (productClass == null)
			productClass = "";
		else
			productClass = productClass.trim();
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
		devReg = context.registerService(GenericDevice.class.getName(), dev, dev.getDeviceProperties());
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
				Properties props = new Properties();
				props.put(UPnPEventListener.UPNP_FILTER, filter);
				this.eventListenerReg = context.registerService(UPnPEventListener.class.getName(), this, props);
			} catch (InvalidSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
				if ("DimmingService".equals(type)) {
					Dimming dim = new DimmingUPnPImpl(dev, upnpServices[i], logger);
					services.put(dim.getName(), dim);
					this.idToService.put(upnpServices[i].getId(), dim);
				} else if ("SwitchPower".equals(type)) {
					SwitchPower switchPower = new SwitchPowerUPnPImpl(dev, upnpServices[i], logger);
					services.put(switchPower.getName(), switchPower);
					this.idToService.put(upnpServices[i].getId(), switchPower);
				} else {
					logger.debug("Unexpected service type: " + serviceType);
				}
			}
		}
		return services;
	}

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
							if ("Volume".equals(name)) {
								try {
									svc.getProperties().setStringValue("CurrentVolume", changedVars.getProperty(name).toString());
								} catch (Exception e) {
									// TODO: Parsing error, it seems the string contains channel as well
								}
							} else if ("AVTransportURI".equals(name)) {
								// TODO: This does not work with the Noxon. It uses a different variable for this information
								svc.getProperties().setStringValue("CurrentUrl", changedVars.getProperty(name));
							} else if ("CurrentTrackMetaData".equals(name)) {
								// TODO: This does not work with the Noxon. It uses a different variable for this information
								String title = getMediaTitle(changedVars.getProperty(name));
								logger.debug("Media title is " + title);
								svc.getProperties().setStringValue("CurrentTitle", title);
							} else if ("TransportState".equals(name)) {
								String state = changedVars.getProperty(name).toLowerCase();
								if ("playing".equals(state))
									svc.getProperties().setStringValue("Status", "Playing");
								else if ("stopped".equals(state))
									svc.getProperties().setStringValue("Status", "Stopped");
								else if ("paused".equals(state))
									svc.getProperties().setStringValue("Status", "Paused");
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
			if ((service != null) &&
				(service instanceof UpdatePropertyInterface) &&
				(service instanceof GenericDeviceService)) {
				logger.debug("Found UpdatePropertyInterface instance");
				for (Enumeration events = eventTable.keys(); events.hasMoreElements();) {
					String name = (String)events.nextElement();
					Object value = eventTable.get(name);
					((UpdatePropertyInterface)service).updateProperty(name, value);
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
			Properties props = dev.getDeviceProperties();
			props.setProperty(Constants.UPDATED_PATH, path);
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
