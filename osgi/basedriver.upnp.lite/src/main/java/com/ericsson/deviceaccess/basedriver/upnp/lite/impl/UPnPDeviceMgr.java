package com.ericsson.deviceaccess.basedriver.upnp.lite.impl;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

public class UPnPDeviceMgr {
    private static final Logger log = LoggerFactory.getLogger(UPnPDeviceMgr.class);
	private static final String SSDP_ADDRESS = "239.255.255.250";
	private static final int SSDP_PORT = 1900;
	private static final int SEARCH_WAIT = 60;
	private static final int SEARCH_INTERVAL = 60*1000;

	private HashMap m_deviceList = new HashMap();
	private Vector m_searchThreads = new Vector();
	private final Object m_searchThreadSyncObject = new Object();
	private Thread m_listenThread = null;
	private final Object m_listenThreadSyncObjet = new Object();
	private boolean shutdown;
	private MulticastSocket m_listenSocket = null;
	private String lanIP = null;
	private BundleContext context;
	private UPnPEventHandler eventHandler = null;
	
	public UPnPDeviceMgr(BundleContext context) {
		this.context = context;
		
		// toUppercase issue with Turkish Language
		Locale.setDefault(Locale.ENGLISH);
	}
    
	public void start(String lanIP) {
		this.lanIP  = lanIP;
		
		shutdown = false;

		// Start event handler for incoming device registrations/deregistrations and UPnP events
		eventHandler  = new UPnPEventHandler(context);
		eventHandler.start();

		startListenOnMulticast();
		
		// Start a search thread on all IP addresses of the local machine
		try {
			// Make it possible to bind to a specific interface
			if (lanIP != null) {
		        m_searchThreads.add(startSearchThread(new InetSocketAddress(lanIP, 0)));
		    } else {
				for (Enumeration interfaces = NetworkInterface.getNetworkInterfaces(); interfaces.hasMoreElements(); ) {
					NetworkInterface ni = (NetworkInterface)interfaces.nextElement();
					for (Enumeration inetAddresses = ni.getInetAddresses(); inetAddresses.hasMoreElements(); ) {
						InetAddress ia = (InetAddress)inetAddresses.nextElement();
						if (ia != null) {
							if (ia.getHostAddress().indexOf(":") < 0) {
								log.debug(ni.getDisplayName() + ", " + ia.getHostAddress());
								m_searchThreads.add(startSearchThread(new InetSocketAddress(ia.getHostAddress(), 0)));
							}
						}
					}
				}
		    }
		} catch (SocketException e) {
            log.warn(e.getMessage(), e);
		}
	}

	public void stop() {
		stopListenOnMulticast();
		eventHandler.stop();
		shutdown = true;
	}

	public HashMap getDevices() {
		synchronized(m_deviceList) {
			HashMap devices = new HashMap();
			for (Iterator i=m_deviceList.keySet().iterator(); i.hasNext(); ) {
				String udn = (String) i.next();
				UPnPDeviceImpl device = (UPnPDeviceImpl) m_deviceList.get(udn);
				if (device.isReady())
					devices.put(device.getUuid(), device);
			}
			return devices;
		}
	}
	
	// Send SSDP M-SEARCH request to 239.255.255.255:1900 and listen for responses
	private Thread startSearchThread(final SocketAddress bindaddr) {
		Thread t = new Thread() {
			public void run() {
				DatagramSocket socket;
				try {
					socket = new DatagramSocket(bindaddr);
				} catch (Exception e) {
					log.warn(e.getMessage(), e);
					return;
				}
				
				while(!shutdown) {
					try {
						// Send the M-SEARCH messages from all interfaces
						try {
							String msearch = "M-SEARCH * HTTP/1.1\r\n" + 
							"HOST: " + SSDP_ADDRESS + ":" + SSDP_PORT + "\r\n" +
							"MAN: \"ssdp:discover\"\r\n" +
							"MX: " + Integer.toString(SEARCH_WAIT) + "\r\n" +
							"ST: upnp:rootdevice\r\n" +
							"Content-Length: 0\r\n\r\n";

							DatagramPacket msearchDp = new DatagramPacket(msearch.getBytes(), 0, msearch.getBytes().length, InetAddress.getByName(SSDP_ADDRESS), SSDP_PORT);

							// Send multicast M-SEARCH message from this network interfaces
							socket.send(msearchDp);
						} catch(Exception e) {
                            log.warn(e.getMessage(), e);
						}

						Vector answers = new Vector();
						byte[] buf = new byte[20000];
						DatagramPacket dp = new DatagramPacket(buf, buf.length);
						
						// Receive the responses for SEARCH_WAIT seconds
						long startTime = System.currentTimeMillis();
						while(System.currentTimeMillis() < startTime + SEARCH_WAIT * 1000 && !shutdown) {
							try {
								dp.setLength(buf.length);
								socket.receive(dp);
								String localIp = socket.getLocalAddress().getHostAddress();
								UPnPDeviceImpl device = UPnPMessageParser.parseSearchResponse(context, localIp, eventHandler, new String(dp.getData(), 0, dp.getLength()));
								if (device == null) {
										//log.debug(this, "Ignoring invalid SSDP response: " + new String(dp.getData(), 0, dp.getLength()));
								} else {
									UPnPDeviceImpl d = getDevice(device.getUuid());
									if (d == null) {
										addUPnPDeviceInstance(device);
									} else {
										d.setAlive();
									}
								}
							} catch (SocketTimeoutException socketException) {
							} catch(Exception e2) {
								//e2.printStackTrace();
							}
						}
						
						// Remove stale devices
						HashMap devices = getDevices();
						for (Iterator i=devices.values().iterator(); i.hasNext();) {
							UPnPDeviceImpl device = ((UPnPDeviceImpl) i.next());
							if (! device.isAlive()) {
								removeUPnPDeviceInstance(device);
							}
						}
					} catch(Exception e) {
                        log.warn("Got exception in search thread", e);
					}
				}
			}
		};
		t.start();
		return t;
	}

	private void startListenOnMulticast() {
		m_listenThread = new Thread() {
			public void run() {
				try {
					m_listenSocket = new MulticastSocket(SSDP_PORT);
					m_listenSocket.joinGroup(InetAddress.getByName(SSDP_ADDRESS));
					m_listenSocket.setBroadcast(true);

					Vector answers = new Vector();
					byte[] buf = new byte[m_listenSocket.getReceiveBufferSize()];
					DatagramPacket dp = new DatagramPacket(buf, buf.length);

					while (!shutdown) {
						try {
							dp.setLength(buf.length);
							m_listenSocket.receive(dp);
							
							UPnPDeviceImpl device = UPnPMessageParser.parseNotifyMessage(context, new String(dp.getData(), 0, dp.getLength()));
							if (device == null)
								continue;
							if (device.isAlive()) {
								UPnPDeviceImpl oldDevice = getDevice(device.getUuid());
								if (oldDevice == null) {
									// Add this device if it's new
									// TODO: Can use this since we can figure out which interface it's coming from: addUPnPDeviceInstance(device);
								} else {
									// Otherwise update it's timestamp
									oldDevice.setAlive();
								}
							} else {
								// This was a byebye message so remove the device
								removeUPnPDeviceInstance(device);
							}
						} catch (Exception e) {
							log.warn("Error when receiving message", e);
						}
					}
				} catch (IOException e) {
					log.warn("Error when handling socket (local:"
							+ m_listenSocket.getLocalAddress() + ", remote:"
							+ m_listenSocket.getInetAddress() + ")", e);
				} finally {
					if (m_listenSocket != null) {
						m_listenSocket.close();
					}
				}
			}
		};

		m_listenThread.start();
	}

	private void stopListenOnMulticast() {
		if(m_listenSocket != null) {
			try {
				m_listenSocket.close();
			} catch(Exception e) {
                log.warn("Got exception when closing listen socket", e);
            }
		}
	}

	private UPnPDeviceImpl getDevice(String uuid) {
		synchronized(m_deviceList) {
			return (UPnPDeviceImpl) m_deviceList.get(uuid.toLowerCase());
		}
	}

	private void removeUPnPDeviceInstance(UPnPDeviceImpl device) {
		synchronized (m_deviceList) {
			UPnPDeviceImpl oldDevice = getDevice(device.getUuid());
			if (oldDevice == null) {
				m_deviceList.remove(device.getUuid().toLowerCase());
				log.debug("Remove device which broadcasted byebye: " + device.getUuid());
			}
		}
		
		device.stop();
	}

	private void addUPnPDeviceInstance(UPnPDeviceImpl device) {
		log.debug("Add device which broadcasted itself: " + device.getUuid());
		synchronized (m_deviceList) {
			m_deviceList.put(device.getUuid().toLowerCase(), device);
		}
		
		try {
			device.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
