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
package com.ericsson.deviceaccess.coap.basedriver.util;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum NetUtil {

    /**
     * Singleton.
     */
    INSTANCE;
    private static final Logger LOGGER = LoggerFactory.getLogger(NetUtil.class);
    static public final int IPV4_ONLY = 1;
    static public final int IPV6_ONLY = 2;
    static public final int ADDR_FAMILY_PRIORITISED_IPV4 = 3;
    static public final int ADDR_FAMILY_PRIORITISED_IPV6 = 4;
    static public final int ADDR_SCOPE_PRIORITISED_IPV4 = 5;
    static public final int ADDR_SCOPE_PRIORITISED_IPV6 = 6;

    private static InetAddress[] getGlobalI() throws SocketException {
        final ArrayList<InetAddress> inetAddrList = new ArrayList();

        Enumeration<NetworkInterface> eNetIf = NetworkInterface.getNetworkInterfaces();
        while (eNetIf.hasMoreElements()) {
            Enumeration<InetAddress> eInetAddr = eNetIf.nextElement().getInetAddresses();
            while (eInetAddr.hasMoreElements()) {
                InetAddress inetAddr = eInetAddr.nextElement();
                if (!inetAddr.isAnyLocalAddress()
                        && !inetAddr.isLinkLocalAddress()
                        && !inetAddr.isLoopbackAddress()
                        && !inetAddr.isMulticastAddress()) {
                    inetAddrList.add(inetAddr);
                }
            }
        }
        return inetAddrList.toArray(new InetAddress[inetAddrList.size()]);
    }

    static public InetAddress[] getGlobalInetAddress() throws SocketException {
        final ArrayList<InetAddress> inetAddrList = new ArrayList();

        Enumeration<NetworkInterface> eNetIf = NetworkInterface.getNetworkInterfaces();
        while (eNetIf.hasMoreElements()) {
            Enumeration<InetAddress> eInetAddr = eNetIf.nextElement().getInetAddresses();
            while (eInetAddr.hasMoreElements()) {
                InetAddress inetAddr = eInetAddr.nextElement();
                if (!inetAddr.isAnyLocalAddress()
                        && !inetAddr.isLinkLocalAddress()
                        && !inetAddr.isLoopbackAddress()
                        && !inetAddr.isMulticastAddress()) {
                    inetAddrList.add(inetAddr);
                }
            }
        }
        return inetAddrList.toArray(new InetAddress[inetAddrList.size()]);
    }

    public static InetAddress getMyInetAddress(int mode) {
        List<List<InetAddress>> listOfAddrs = new ArrayList();
        ClassifiedAddresses classifiedAddrs = new ClassifiedAddresses();
        switch (mode) {
            case IPV4_ONLY:
                listOfAddrs.add(classifiedAddrs.globalIPv4);
                listOfAddrs.add(classifiedAddrs.siteLocalIPv4);
                listOfAddrs.add(classifiedAddrs.linkLocalIPv4);
                break;
            case IPV6_ONLY:
                listOfAddrs.add(classifiedAddrs.globalIPv6);
                listOfAddrs.add(classifiedAddrs.siteLocalIPv6);
                listOfAddrs.add(classifiedAddrs.linkLocalIPv6);
                break;
            case ADDR_FAMILY_PRIORITISED_IPV4:
                listOfAddrs.add(classifiedAddrs.globalIPv4);
                listOfAddrs.add(classifiedAddrs.siteLocalIPv4);
                listOfAddrs.add(classifiedAddrs.linkLocalIPv4);
                listOfAddrs.add(classifiedAddrs.globalIPv6);
                listOfAddrs.add(classifiedAddrs.siteLocalIPv6);
                listOfAddrs.add(classifiedAddrs.linkLocalIPv6);
                break;
            case ADDR_FAMILY_PRIORITISED_IPV6:
                listOfAddrs.add(classifiedAddrs.globalIPv6);
                listOfAddrs.add(classifiedAddrs.siteLocalIPv6);
                listOfAddrs.add(classifiedAddrs.linkLocalIPv6);
                listOfAddrs.add(classifiedAddrs.globalIPv4);
                listOfAddrs.add(classifiedAddrs.siteLocalIPv4);
                listOfAddrs.add(classifiedAddrs.linkLocalIPv4);
                break;
            case ADDR_SCOPE_PRIORITISED_IPV4:
                listOfAddrs.add(classifiedAddrs.globalIPv4);
                listOfAddrs.add(classifiedAddrs.globalIPv6);
                listOfAddrs.add(classifiedAddrs.siteLocalIPv4);
                listOfAddrs.add(classifiedAddrs.siteLocalIPv6);
                listOfAddrs.add(classifiedAddrs.linkLocalIPv4);
                listOfAddrs.add(classifiedAddrs.linkLocalIPv6);
                break;
            case ADDR_SCOPE_PRIORITISED_IPV6:
            default:
                listOfAddrs.add(classifiedAddrs.globalIPv6);
                listOfAddrs.add(classifiedAddrs.globalIPv4);
                listOfAddrs.add(classifiedAddrs.siteLocalIPv6);
                listOfAddrs.add(classifiedAddrs.siteLocalIPv4);
                listOfAddrs.add(classifiedAddrs.linkLocalIPv6);
                listOfAddrs.add(classifiedAddrs.linkLocalIPv4);
                break;
        }
        for (List<InetAddress> addrs : listOfAddrs) {
            if (!addrs.isEmpty()) {
                return addrs.get(0);
            }
        }
        try {
            if (mode == IPV4_ONLY
                    || mode == ADDR_FAMILY_PRIORITISED_IPV4
                    || mode == ADDR_SCOPE_PRIORITISED_IPV4) {
                return Inet4Address.getLocalHost();
            } else {
                return Inet6Address.getLocalHost();
            }
        } catch (UnknownHostException e) {
            LOGGER.error("Local host was unknown.", e);
        }
        return null; // XXX: What can we do here??
    }

    /**
     * Get an InetSocketAddress from the String format "<Address>:<Port>", or
     * "<Address>".
     *
     * @param addressPortStr String for Address and Port. Enclose with "[" and
     * "]" for IPv6 addresses.
     * @param defaultPort defaultPort when port is not specified in
     * addressPortStr
     * @return InetSocketAddress
     * @throws UnknownHostException
     */
    public static InetSocketAddress getInetSocketAddress(String addressPortStr, int defaultPort) throws UnknownHostException {
        if (addressPortStr == null || addressPortStr.isEmpty()) {
            return null;
        }
        String addressStr;
        int port;
        if (addressPortStr.charAt(0) == '[') {
            // [2001:1::1]:1234 or [2001:1::1]
            int i = addressPortStr.indexOf(']');
            if (i <= 0) {
                return null;
            }
            addressStr = addressPortStr.substring(1, i);
            if (addressPortStr.length() > i + 2 && addressPortStr.charAt(i + 1) == ':') {
                // [2001:1::1]:1234
                port = Integer.parseInt(addressPortStr.substring(i + 2));
            } else if (addressPortStr.length() == i + 1) {
                // [2001:1::1] (is it legitimate??)
                port = defaultPort;
            } else {
                return null;
            }
        } else if (addressPortStr.contains(":")) {
            int i = addressPortStr.lastIndexOf(':');
            if (addressPortStr.indexOf(':') == i) {
                // www.fqdn.com:1234 or 192.168.1.1:1234
                addressStr = addressPortStr.substring(0, i);
                port = Integer.parseInt(addressPortStr.substring(i + 1));
            } else {
                // IPv6 Address 2001:1::1 (at least including two ':')
                addressStr = addressPortStr;
                port = defaultPort;
            }
        } else {
            // www.fqdn.com or 192.168.1.1
            addressStr = addressPortStr;
            port = defaultPort;
        }
        if (addressStr == null || port < 0) {
            return null;
        }
        return new InetSocketAddress(addressStr, port);
    }

    private static class ClassifiedAddresses {

        static ClassifiedAddresses getClassifiedAddresses() {
            ClassifiedAddresses result = new ClassifiedAddresses();

            Enumeration eNetIf = null;
            try {
                eNetIf = NetworkInterface.getNetworkInterfaces();
            } catch (SocketException e) {
                LOGGER.error("Couldn't get network interfaces.", e);
            }
            if (eNetIf == null) {
                return result;
            }
            while (eNetIf.hasMoreElements()) {
                NetworkInterface netIf = (NetworkInterface) eNetIf.nextElement();
                Enumeration eInetAddr = netIf.getInetAddresses();
                while (eInetAddr.hasMoreElements()) {
                    InetAddress inetAddr = (InetAddress) eInetAddr.nextElement();

                    if (inetAddr instanceof Inet4Address) {
                        if (inetAddr.isLinkLocalAddress()) {
                            result.linkLocalIPv4.add(inetAddr);
                        } else if (inetAddr.isSiteLocalAddress()) {
                            result.siteLocalIPv4.add(inetAddr);
                        } else if (!inetAddr.isAnyLocalAddress() && !inetAddr.isMulticastAddress()) {
                            result.globalIPv4.add(inetAddr);
                        }
                    } else if (inetAddr instanceof Inet6Address) {
                        if (inetAddr.isLinkLocalAddress()) {
                            result.linkLocalIPv6.add(inetAddr);
                        } else if (inetAddr.isSiteLocalAddress()) {
                            result.siteLocalIPv6.add(inetAddr);
                        } else if (!inetAddr.isAnyLocalAddress() && !inetAddr.isMulticastAddress()) {
                            result.globalIPv6.add(inetAddr);
                        }
                    }
                }
            }
            return result;
        }
        final public LinkedList<InetAddress> globalIPv4 = new LinkedList<>();
        final public LinkedList<InetAddress> globalIPv6 = new LinkedList<>();
        final public LinkedList<InetAddress> siteLocalIPv4 = new LinkedList<>();
        final public LinkedList<InetAddress> siteLocalIPv6 = new LinkedList<>();
        final public LinkedList<InetAddress> linkLocalIPv4 = new LinkedList<>();
        final public LinkedList<InetAddress> linkLocalIPv6 = new LinkedList<>();

        private ClassifiedAddresses() {
        }
    }
}
