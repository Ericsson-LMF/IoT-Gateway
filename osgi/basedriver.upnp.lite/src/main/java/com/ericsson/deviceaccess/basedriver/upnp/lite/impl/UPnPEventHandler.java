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
package com.ericsson.deviceaccess.basedriver.upnp.lite.impl;

import com.ericsson.deviceaccess.basedriver.upnp.lite.xml.XmlNode;
import com.ericsson.deviceaccess.basedriver.upnp.lite.xml.XmlParser;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.service.upnp.UPnPDevice;
import org.osgi.service.upnp.UPnPEventListener;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UPnPEventHandler extends NanoHTTPD implements ServiceTrackerCustomizer {

    private static final Logger log = LoggerFactory.getLogger(UPnPEventHandler.class);
    private static final int SUBSCRIBE_TIMEOUT = 300;
    private BundleContext context;
    private HashMap services = new HashMap();
    private HashMap timers = new HashMap();
    private HashMap listeners = new HashMap();
    private ServiceTracker eventListenerTracker = null;
    private boolean shutdown = false;
    private Thread subscriptionThread = null;

    public UPnPEventHandler(BundleContext context) {
        super();
        this.context = context;
    }

    public void start() {
        shutdown = false;
        eventListenerTracker = new ServiceTracker(context, UPnPEventListener.class.getName(), this);
        eventListenerTracker.open();
        startSubscriptionThread();
    }

    public void stop() {
        shutdown();
        shutdown = true;
        if (eventListenerTracker != null) {
            eventListenerTracker.close();
        }

        if (subscriptionThread != null) {
            subscriptionThread.interrupt();
        }
    }

    @Override
    public Response serve(String uri, String method, Properties header, Properties parms, BufferedReader in) {
        log.debug("UPnP Event: uri=" + uri + ", method=" + method + ", headers=" + header.toString());

        try {
            final UPnPServiceImpl service = (UPnPServiceImpl) services.get(uri.substring(1));
            final Properties events = new Properties();
            if (service != null) {
                XmlNode doc = XmlParser.parse(in, true);
                XmlNode[] properties = doc.getChildren("property");
                for (int i = 0; i < properties.length; i++) {
                    XmlNode[] values = properties[i].getChildren();
                    for (int j = 0; j < values.length; j++) {
                        XmlNode value = values[j];
                        String key = value.getName();
                        String val = value.getText();
                        UPnPStateVariableImpl var = (UPnPStateVariableImpl) service.m_variables.get(key);
                        if (var != null) {
                            var.setValue(val);
                            events.put(key, val);
                        }
                    }
                }
            }

            log.debug("Received UPnP event: " + service.getDevice().getUuid() + ", " + service.getId() + ", " + events.toString());

            for (Iterator i = listeners.keySet().iterator(); i.hasNext();) {
                final UPnPEventListener listener = (UPnPEventListener) i.next();
                Filter filter = (Filter) listeners.get(listener);
                if (filter.match(service.getProperties())) {
                    (new Thread() {
                        @Override
                        public void run() {
                            listener.notifyUPnPEvent(service.getDevice().getUuid(), service.getId(), events);
                        }
                    }).start();
                }
            }
        } catch (Exception e) {
            log.error("Received invalid UPnP event: " + uri + ", " + header.toString() + ", " + parms.toString(), e);
        }

        return new Response();
    }

    public void registerService(UPnPServiceImpl service) {
        services.put(service.getDevice().getUuid() + "/" + service.getId(), service);

        try {
            subscribeEvents(service);
            synchronized (timers) {
                timers.put(service, new Date());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unregisterService(UPnPServiceImpl service) {
        services.remove(service.getDevice().getUuid() + "/" + service.getId());
        synchronized (timers) {
            timers.remove(service);
        }
    }

    public void subscribeEvents(UPnPServiceImpl service) throws Exception {
        URL url = new URL(service.getEventUrl());
        String host = url.getHost();
        int port = (url.getPort() > 0 ? url.getPort() : 80);
        SocketAddress sockaddr = new InetSocketAddress(host, port);
        Socket sock = new Socket();
        sock.connect(sockaddr, 10000);

        log.debug("SUBSCRIBE " + url.getFile() + " HTTP/1.1\nHost: " + host + ":" + port + "\nCallback: " + "<http://" + service.getDevice().getLocalIp() + ":" + getListenPort() + "/" + service.getDevice().getUuid() + "/" + service.getId() + ">\nNT: upnp:event\nTIMEOUT: Second-" + SUBSCRIBE_TIMEOUT + "\nContent-Length: 0\n");

        try (BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream(), "UTF8"))) {
            wr.write("SUBSCRIBE " + url.getFile() + " HTTP/1.1\r\n");
            wr.write("Host: " + host + ":" + port + "\r\n");
            wr.write("Callback: " + "<http://" + service.getDevice().getLocalIp() + ":" + getListenPort() + "/" + service.getDevice().getUuid() + "/" + service.getId() + ">\r\n");
            wr.write("NT: upnp:event\r\n");
            wr.write("TIMEOUT: Second-" + SUBSCRIBE_TIMEOUT + "\r\n");
            wr.write("Content-Length: 0\r\n");
            wr.write("\r\n");
            wr.flush();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    sb.append(inputLine).append("\n");
                    if (inputLine.trim().length() == 0) {
                        break;
                    }
                }
                log.debug("SUBSCRIBE RESPONSE:\n" + sb.toString());
            }
        }
    }

    @Override
    public Object addingService(ServiceReference sr) {
        UPnPEventListener listener = (UPnPEventListener) context.getService(sr);
        synchronized (listeners) {
            listeners.put(listener, sr.getProperty(UPnPEventListener.UPNP_FILTER));
        }

        return listener;
    }

    @Override
    public void modifiedService(ServiceReference reference, Object service) {
    }

    @Override
    public void removedService(ServiceReference reference, Object service) {
        synchronized (listeners) {
            listeners.remove(service);
        }
    }

    private void startSubscriptionThread() {
        subscriptionThread = new Thread() {
            @Override
            public void run() {
                while (!shutdown) {
                    try {
                        Vector process = new Vector();
                        Date nextTimer = null;

                        synchronized (timers) {
                            for (Iterator i = timers.keySet().iterator(); i.hasNext();) {
                                UPnPServiceImpl service = (UPnPServiceImpl) i.next();
                                Date timer = (Date) timers.get(service);
                                long now = new Date().getTime();

                                // Check if the it's time to refresh the service
                                if (timer.getTime() + SUBSCRIBE_TIMEOUT * 500 < now) {
                                    process.add(service);
                                }

                                // Figure out which service needs to be refreshed after this batch
                                if (nextTimer == null) {
                                    nextTimer = timer;
                                } else if (timer.before(nextTimer)) {
                                    nextTimer = timer;
                                }
                            }
                        }

                        for (Iterator i = process.iterator(); i.hasNext();) {
                            UPnPServiceImpl service = (UPnPServiceImpl) i.next();
                            try {
                                synchronized (timers) {
                                    timers.put(service, new Date());
                                }
                                subscribeEvents(service);
                            } catch (Exception e) {
                                log.warn("Can not subscribe to events for device " + service.getDevice().getDescriptions(null).get(UPnPDevice.FRIENDLY_NAME) + " and service " + service.getType(), e);
                                synchronized (timers) {
                                    timers.remove(service);
                                }
                            }
                        }

                        if (nextTimer != null) {
                            long sleepTime = nextTimer.getTime() + SUBSCRIBE_TIMEOUT * 500 - new Date().getTime();
                            if (sleepTime > 0) {
                                Thread.sleep(sleepTime);
                            }
                        } else {
                            Thread.sleep(SUBSCRIBE_TIMEOUT * 500);
                        }
                    } catch (InterruptedException e) {
                        shutdown = true;
                    }
                }
            }
        };
        subscriptionThread.start();
    }

}
