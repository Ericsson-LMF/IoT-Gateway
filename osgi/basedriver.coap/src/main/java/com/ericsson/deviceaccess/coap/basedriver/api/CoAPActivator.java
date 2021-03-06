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
package com.ericsson.deviceaccess.coap.basedriver.api;

import com.ericsson.deviceaccess.coap.basedriver.util.NetUtil;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Properties;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements BundleActivator and is responsible for starting the UDP
 * sockets for sending and receiving UDP packets.
 * <p/>
 * It will read a properties file which includes for example the IP address and
 * port where to send the discovery requests. The user can also define how often
 * to send discovery requests.
 */
public class CoAPActivator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoAPActivator.class);
    public static ServiceTracker tracker;
    public static ServiceTracker incomingCoAPTracker;

    private static final String COAP_ADDRESS = "COAP_ADDRESS";
    public static BufferedWriter out;
    private BundleContext context;

    private CoAPService service;

    private ServiceRegistration serviceRegistration;

    /**
     * This will be run when the bundle is started. It will start the UDP thread
     * to listen for incoming UDP packets.
     */
    @Override
    public void start(BundleContext context) throws Exception {
        this.context = context;
        LOGGER.info("Start CoAPActivator, try to register coap service");
        Bundle bundle = context.getBundle();
        InputStream is = null;

        URL url = bundle.getEntry("/META-INF/coap.properties");
        if (url != null) {
            try {
                is = url.openStream();
            } catch (IOException ex) {
                LOGGER.warn("CoAP configuration file cannot be loaded.", ex);
            }
        }

        Properties p = new Properties();
        InetAddress address = null;
        int coapPort = -1;
        int discoveryInterval = 0;
        int discoveryPort = -1;
        InetAddress discovery = null;
        int maximumBlockSzx = 6;

        if (is != null) {
            LOGGER.debug("Read properties file");
            try {
                p.load(is);
            } catch (IOException e) {
                LOGGER.warn("CoAP configuration file cannot be loaded.", e);
            }
            LOGGER.debug("Properties file loaded");

            String socketAddress = p.getProperty(COAP_ADDRESS);
            String port = p.getProperty("COAP_PORT");

            if (socketAddress != null) {
                try {
                    address = null;
                    try {
                        NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
                        Enumeration<InetAddress> ia = ni.getInetAddresses();
                        while (ia.hasMoreElements()) {
                            InetAddress elem = ia.nextElement();

							//String ipv6 = p.getProperty("IPV6");
                            //Boolean ipv6Boolean = new Boolean(ipv6);
							/*if (ipv6Boolean.booleanValue()) {
                             if (elem instanceof Inet6Address
                             && !elem.isLoopbackAddress()) {
                             address = elem;
                             }
                             }*/
                            // This is commented out, as otherwise testing
                            // on
                            // localhost 127.0.0.1 does not work. If needed,
                            // uncomment this one for not using the loopback
                            // addresses
							/*
                             * else { if (!elem.isLoopbackAddress() && !(elem
                             * instanceof Inet6Address)) { address = elem; } }
                             */
                        }

                    } catch (NullPointerException e) {
                        LOGGER.warn("Retrieving Information from NetworkInterface failed", e);
                    }

                    if (address == null) {
                        address = InetAddress.getByName(socketAddress);
                    }
                    LOGGER.debug("Local address is now [" + address + "]");

                } catch (UnknownHostException e) {
                    throw new CoAPException(e);
                }
            } else {
                address = NetUtil.getMyInetAddress(NetUtil.ADDR_SCOPE_PRIORITISED_IPV6);
                if (address == null) {
                    address = InetAddress.getByName(null);
                }
            }
            if (port != null) {
                try {
                    coapPort = Integer.parseInt(port);
                    if (coapPort <= 0) {
                        coapPort = -1;
                        LOGGER.debug("set port to [" + coapPort + "]");
                    }
                } catch (NumberFormatException e) {
                    coapPort = 5684;
                }
            }

            String interval = p.getProperty("DISCOVERY_INTERVAL");
            if (interval != null && !interval.isEmpty()) {
                discoveryInterval = Integer.parseInt(interval);
            }

            // The maximum allowed szx value is 6. If the value is set bigger in
            // the properties file, set it to 6. Also, if the value is set
            // smaller than 0 in the properties file, use 0 instead.
            String maximumBlockSzxStr = p.getProperty("MAXIMUM_BLOCK_SZX");
            if (maximumBlockSzxStr != null && !maximumBlockSzxStr.isEmpty()) {
                maximumBlockSzx = Integer.parseInt(maximumBlockSzxStr);
                if (maximumBlockSzx > 6) {
                    maximumBlockSzx = 6;
                } else if (maximumBlockSzx < 0) {
                    maximumBlockSzx = 0;
                }
            }

            String discoveryAddress = p.getProperty("DISCOVERY_ADDRESS");
            if (discoveryAddress != null) {
                try {
                    discovery = InetAddress.getByName(discoveryAddress);
                } catch (UnknownHostException e) {
                    throw new CoAPException(e);
                }
            }

            port = p.getProperty("DISCOVERY_PORT");
            if (port != null) {
                discoveryPort = Integer.parseInt(port);
            }
        } else {
            LOGGER.debug("Problem reading properties file, use hard coded values");
            try {
                address = NetUtil.getMyInetAddress(NetUtil.ADDR_SCOPE_PRIORITISED_IPV6);
                if (address == null) {
                    address = InetAddress.getLocalHost();
                }
                coapPort = 5684;
                discoveryPort = 5683;
                discovery = InetAddress.getByName("ff02::1:fe00:1");
                discoveryInterval = 60;
            } catch (UnknownHostException e) {
                throw new CoAPException(e);
            }
        }

        LOGGER.info("CoAP driver address: " + address.getHostAddress());
        service = new CoAPService(address, coapPort, maximumBlockSzx);
        serviceRegistration = context.registerService(CoAPService.class, service, null);

        LOGGER.debug("Service registered");
        // Create a tracker for CoAPRequestListener services.
        tracker = new ServiceTracker(context, DeviceInterface.class, null);
        tracker.open();

        incomingCoAPTracker = new ServiceTracker(context, IncomingCoAPRequestListener.class, null);
        incomingCoAPTracker.open();

        out = new BufferedWriter(new FileWriter("coapmessaging.log"));

        service.init();
        service.startResourceDiscoveryService(discoveryInterval, discovery, discoveryPort);
    }

    /**
     * This method unregisters the service and stops the threads of this bundle
     *
     * @param context bundle to stop
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        LOGGER.debug("Stop CoAPService");
        service.stopService();

        service = null;
        tracker.close();
        out.flush();
        out.close();
        out = null;

        serviceRegistration.unregister();
        this.context = null;
    }
}
