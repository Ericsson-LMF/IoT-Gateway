package com.ericsson.deviceaccess.basedriver.upnp.lite.impl;

import org.osgi.framework.BundleContext;

public class UPnPMessageParser {
	
	protected static UPnPDeviceImpl parseSearchResponse(BundleContext context, String localIp, UPnPEventHandler eventHandler, String response) throws Exception {
		//log.debug(this, "Found: \n" + response);
		int start = response.toUpperCase().indexOf("LOCATION:");
		if(start == -1) {
			throw new Exception("No LOCATION parameter found");
		}
		int end = response.indexOf("\r\n", start);
		if(end == -1) {
			throw new Exception("No LOCATION parameter found");
		}
		String location = response.substring(start + 9, end).trim();

		start = location.toUpperCase().indexOf("HTTP://");
		if(start == -1) {
			throw new Exception("No HTTP URL found in LOCATION parameter");
		}
		end = location.toUpperCase().indexOf("/", start + 7);
		if(end == -1) {
			throw new Exception("Invalid HTTP URL found in LOCATION parameter");
		}

		String ipnport = location.substring(start + 7, end);
		String path = location.substring(end);

		String ip = ipnport;
		int port = 80;
		end = ipnport.indexOf(':');
		if(end != -1) {
			ip = ipnport.substring(0,end);
			port = Integer.parseInt(ipnport.substring(end + 1));
		}
		// Retrieve uuid....
		start = response.toUpperCase().indexOf("UUID:");
		if(start == -1) {
			throw new Exception("No UUID found");
		}
		end = response.indexOf("::", start);
		if(end == -1) {
			end = response.indexOf("\r\n", start);
			if(end == -1) {
				throw new Exception("Invalid UUID found");
			}
		}
		String uuid = response.substring(start + 5, end);

		UPnPDeviceImpl device = new UPnPDeviceImpl(context, uuid, location, ip, port, localIp, eventHandler);
		
		return device;
	}

	protected static UPnPDeviceImpl parseNotifyMessage(BundleContext context, String message) throws Exception {
		if(!message.startsWith("NOTIFY * HTTP/1.1")) {
			return null;
		}
		int start = message.toUpperCase().indexOf("NTS:");
		if(start == -1) {
			throw new Exception("No NTS parameter in message:\n" + message);
		}
		int end = message.indexOf("\r\n", start);
		if(end == -1) {
			throw new Exception("Not a NOTIFY message in message:\n" + message);
		}
		String NTS = message.substring(start + 4, end).trim().toLowerCase();

		start = message.toUpperCase().indexOf("NT:");
		if(start == -1) {
			throw new Exception("No NT parameter in message:\n" + message);
		}
		end = message.indexOf("\r\n", start);
		if(end == -1) {
			throw new Exception("Invalid NT parameter in message:\n" + message);
		}
		String NT = message.substring(start + 3, end).trim();

		String ip = "";
		int port = 0;
		String path = "";
		String location = "";

		if(NTS.equals("ssdp:alive")) {
			start = message.toUpperCase().indexOf("LOCATION:");
			if(start == -1) {
				throw new Exception("No LOCATION in message:\n" + message);
			}
			end = message.indexOf("\r\n", start);
			if(end == -1) {
				throw new Exception("Invalid LOCATION parameter in message:\n" + message);
			}
			location = message.substring(start + 9, end).trim();

			start = location.toUpperCase().indexOf("HTTP://");
			if(start == -1) {
				throw new Exception("Invalid URL in message:\n" + message);
			}
			end = location.toUpperCase().indexOf("/", start + 7);
			if(end == -1) {
				throw new Exception("Invalid URL in message:\n" + message);
			}

			String ipnport = location.substring(start + 7, end);
			path = location.substring(end);

			ip = ipnport;
			port = 80;
			end = ipnport.indexOf(':');
			if(end != -1) {
				ip = ipnport.substring(0,end);
				port = Integer.parseInt(ipnport.substring(end + 1));
			}
		}
		start = message.toUpperCase().indexOf("USN:");
		start = message.toUpperCase().indexOf("UUID:", start);
		if(start == -1) {
			throw new Exception("No UUID in message:\n" + message);
		}
		end = message.indexOf("::", start);
		if(end == -1) {
			end = message.indexOf("\r\n", start);
			if(end == -1) {
				throw new Exception("Invalid UUID in message:\n" + message);
			}
		}
		String uuid = message.substring(start + 5, end).trim();
		
		UPnPDeviceImpl device = new UPnPDeviceImpl(context, uuid, location, ip, port, "", null);
		
		// Flag the device for removal if it's not an "alive" message (i.e. byebye)
		if (! NTS.equals("ssdp:alive"))
			device.setExpired();

		return device;
	}
}
