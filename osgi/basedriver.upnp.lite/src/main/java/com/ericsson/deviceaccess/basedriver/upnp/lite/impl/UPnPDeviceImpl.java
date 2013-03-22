package com.ericsson.deviceaccess.basedriver.upnp.lite.impl;

import org.kxml2.io.KXmlParser;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.upnp.UPnPDevice;
import org.osgi.service.upnp.UPnPIcon;
import org.osgi.service.upnp.UPnPService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

public class UPnPDeviceImpl implements UPnPDevice {
    private static final Logger log = LoggerFactory.getLogger(UPnPDeviceImpl.class);
	private String m_ip = null;
	private int m_port = 0;
	private String m_location = null;
	private HashMap m_services = new HashMap();
	private String m_descriptionXML;
	private long m_timestamp;
	private boolean isReady = false;
	private Vector m_icons = new Vector();
	private UPnPDeviceImpl self;
	private String m_localIp;
	private UPnPEventHandler m_eventHandler;
	private BundleContext context;
	private ServiceRegistration sr = null;
	private Properties props = new Properties();

	protected UPnPDeviceImpl(BundleContext context, String uuid, String location, String ip, int port, String localIp, UPnPEventHandler eventHandler) {
		this.context = context;
		props.put(UDN, uuid);
		m_ip = ip;
		m_port = port;
		m_location = location;
		m_timestamp = System.currentTimeMillis();
		m_localIp = localIp;
		m_eventHandler = eventHandler;
		self = this;
	}

	public UPnPIcon[] getIcons(String locale) {
		return (UPnPIcon[]) m_icons.toArray(new UPnPIcon[0]);
	}

	public Dictionary getDescriptions(String locale) {
		return props;
	}
	public UPnPService[] getServices() {
		return (UPnPService[]) m_services.values().toArray(new UPnPService[0]);
	}
	
	public UPnPService getService(String serviceId) {
		return (UPnPService) m_services.get(serviceId);
	}
	
	public UPnPIconImpl[] getIcons() {
		return (UPnPIconImpl[]) m_icons.toArray(new UPnPIconImpl[0]);
	}

	// Returns false if no response has been received from the device within two minutes
	protected boolean isAlive() {
		if (System.currentTimeMillis() - m_timestamp > 120*1000)
			return false;
		else
			return true;
	}
	
	protected boolean isReady() {
		return isReady;
	}
	
	protected void setAlive() {
		m_timestamp = System.currentTimeMillis();
	}
	
	protected void setExpired() {
		m_timestamp = 0;
	}
	
	private String normalizeUrl(URL url, String urlBase, String relativeURL) {
		if (relativeURL.startsWith("/"))
			if (urlBase.toLowerCase().startsWith("http")) {
				if (urlBase.endsWith("/"))
					return urlBase.substring(0, urlBase.length()-1) + relativeURL;
				else
					return urlBase + relativeURL;
			} else {
				return "http://" + m_ip + ":"  + m_port + relativeURL;
			}
		else {
			if (urlBase.startsWith("http")) {
				return urlBase + (urlBase.endsWith("/") ? relativeURL : "/" + relativeURL);
			} else {
				if (url.getPath().endsWith("/"))
					return url.getProtocol() + "://" + url.getHost() + ":" + url.getPort() + url.getPath() + relativeURL;
				else
					return url.getProtocol() + "://" + url.getHost() + ":" + url.getPort() + url.getPath() + "/" + relativeURL;
			}
		}
	}

	public String getLocalIp() {
		return m_localIp;
	}
	
    protected String getUuid() {
    	return props.getProperty(UDN);
    }

	protected void stop() {
		if (sr != null)
			sr.unregister();
		
		for (Iterator i = m_services.values().iterator(); i.hasNext(); )
			((UPnPServiceImpl)i.next()).stop();
	}
	
	protected void start() throws Exception {
		final UPnPDeviceImpl self = this;
		Thread receiveThread = new Thread() {

			public void run() {
				try {
					// Get the device description
					URL url = new URL(m_location);
					try {
						BufferedReader in = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()));
						StringBuffer sb = new StringBuffer();
						String inputLine;
						while ((inputLine = in.readLine()) != null) 
						    sb.append(inputLine);
						in.close();
						m_descriptionXML = sb.toString();
					} catch (Exception e) {
						throw new Exception("Failed to retrieve device description: " + m_location);
					}

					// Parse the device description
					XmlPullParser p = new KXmlParser();
					p.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
					p.setInput(new StringReader(m_descriptionXML));
					
					String tagName = "";
					String scpdUrl = "";
					String controlUrl = "";
					String eventSubUrl = "";
					String serviceType = "";
					String serviceId = "";
					String urlBase = "";
					String iconMimetype = "";
					int iconWidth = 0;
					int iconHeight = 0;
					int iconDepth = 0;
					String iconUrl = "";
					props.put(UPnPDevice.DEVICE_CATEGORY, new String[]{UPnPDevice.DEVICE_CATEGORY});
					
					while(p.getEventType() != XmlPullParser.END_DOCUMENT) {
						try {
							if(p.getEventType() == XmlPullParser.START_TAG) {
								tagName = p.getName();
							} else if (p.getEventType() == XmlPullParser.TEXT) {
								if (tagName.equals("SCPDURL")) {
									scpdUrl = p.getText();
								} else if (tagName.equalsIgnoreCase("friendlyName")) {
									props.put(FRIENDLY_NAME, p.getText());
								} else if (tagName.equalsIgnoreCase("deviceType")) {
									props.put(TYPE, p.getText());
								} else if (tagName.equalsIgnoreCase("serviceType")) {
									// TODO: Generate for lower versions as well
									serviceType = p.getText();
								} else if (tagName.equalsIgnoreCase("serviceId")) {
									serviceId = p.getText();
								} else if (tagName.equalsIgnoreCase("eventSubURL")) {
									eventSubUrl = p.getText();
								} else if (tagName.equalsIgnoreCase("controlURL")) {
									controlUrl = p.getText();
								} else if (tagName.equalsIgnoreCase("modelName")) {
									props.put(MODEL_NAME, p.getText());
								} else if (tagName.equalsIgnoreCase("modelNumber")) {
									props.put(MODEL_NUMBER, p.getText());
								} else if (tagName.equalsIgnoreCase("modelDescription")) {
									props.put(MODEL_DESCRIPTION, p.getText());
								} else if (tagName.equalsIgnoreCase("UPC")) {
									props.put(UPC, p.getText());
								} else if (tagName.equalsIgnoreCase("serialNumber")) {
									props.put(SERIAL_NUMBER, p.getText());
								} else if (tagName.equalsIgnoreCase("presentationURL")) {
									props.put(PRESENTATION_URL, p.getText());
								} else if (tagName.equalsIgnoreCase("manufacturer")) {
									props.put(MANUFACTURER, p.getText());
								} else if (tagName.equalsIgnoreCase("manufacturerURL")) {
									props.put(MANUFACTURER_URL, p.getText());
								} else if (tagName.equalsIgnoreCase("URLBase")) {
									urlBase = p.getText();
								} else if (tagName.equalsIgnoreCase("width")) {
									iconWidth = Integer.parseInt(p.getText());
								} else if (tagName.equalsIgnoreCase("height")) {
									iconHeight = Integer.parseInt(p.getText());
								} else if (tagName.equalsIgnoreCase("depth")) {
									iconDepth = Integer.parseInt(p.getText());
								} else if (tagName.equalsIgnoreCase("mimetype")) {
									iconMimetype = p.getText();
								} else if (tagName.equalsIgnoreCase("url")) {
									iconUrl = normalizeUrl(url, urlBase, p.getText());
								}
								tagName = "";
							} else if (p.getEventType() == XmlPullParser.END_TAG) {
								if (p.getName().equalsIgnoreCase("service")) {
									scpdUrl = normalizeUrl(url, urlBase, scpdUrl);
									controlUrl = normalizeUrl(url, urlBase, controlUrl);
									eventSubUrl = normalizeUrl(url, urlBase, eventSubUrl);
									UPnPServiceImpl service = new UPnPServiceImpl(self, serviceType, serviceId, scpdUrl, controlUrl, eventSubUrl, m_eventHandler);
									m_services.put(serviceId, service);
								} else if (p.getName().equalsIgnoreCase("icon")) {
									UPnPIconImpl upnpIcon = new UPnPIconImpl(iconMimetype, iconWidth, iconHeight, iconDepth, iconUrl);
									m_icons.add(upnpIcon);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
							log.error("Failed to retrieve valid description XML from device + \n" + m_descriptionXML, e);
						}
						p.next();		
					}
					
					// Get all the service descriptions
					for (Iterator i = m_services.values().iterator(); i.hasNext(); ) {
						UPnPServiceImpl service = (UPnPServiceImpl) i.next();
						service.start();
					}

					// Find the icon with the highest resolution of type png or jpg and put this as a property to be used by the UPnP Adaptor
					UPnPIconImpl[] icons = getIcons();
					UPnPIconImpl bestMatch = null;
					int bestMatchSum = 0;
					for (int i = 0; i < icons.length; i++) {
						UPnPIconImpl icon = icons[i];
						if (icon.getMimeType().equalsIgnoreCase("image/jpeg") || icon.getMimeType().equalsIgnoreCase("image/png")) {
							int iconSum = icon.getHeight() * icon.getWidth() * icon.getDepth();
							if (bestMatch == null || bestMatchSum < iconSum) {
								bestMatch = icon;
								bestMatchSum = iconSum;
							}
						}
					}
					
					if (bestMatch != null)
						props.put("GDA_ICON", bestMatch.getUrl());

					isReady = true;
					log.debug("Finished with " + getUuid() + "(" + props.getProperty(FRIENDLY_NAME) + ")");
					
					sr  = context.registerService(UPnPDevice.class.getName(), self, props );
				} catch (Exception e) {
                    log.warn(e.getMessage(), e);
				}
			}
		};
		receiveThread.start();
	}
	
}
