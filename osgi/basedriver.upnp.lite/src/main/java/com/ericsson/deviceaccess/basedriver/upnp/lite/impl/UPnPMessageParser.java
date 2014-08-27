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

import org.osgi.framework.BundleContext;

public class UPnPMessageParser {

    protected static UPnPDeviceImpl parseSearchResponse(BundleContext context, String localIp, UPnPEventHandler eventHandler, String response) throws Exception {
        //log.debug(this, "Found: \n" + response);
        int start = response.toUpperCase().indexOf("LOCATION:");
        if (start == -1) {
            throw new Exception("No LOCATION parameter found");
        }
        int end = response.indexOf("\r\n", start);
        if (end == -1) {
            throw new Exception("No LOCATION parameter found");
        }
        String location = response.substring(start + 9, end).trim();

        start = location.toUpperCase().indexOf("HTTP://");
        if (start == -1) {
            throw new Exception("No HTTP URL found in LOCATION parameter");
        }
        end = location.toUpperCase().indexOf('/', start + 7);
        if (end == -1) {
            throw new Exception("Invalid HTTP URL found in LOCATION parameter");
        }

        String ipnport = location.substring(start + 7, end);
        String path = location.substring(end);

        String ip = ipnport;
        int port = 80;
        end = ipnport.indexOf(':');
        if (end != -1) {
            ip = ipnport.substring(0, end);
            port = Integer.parseInt(ipnport.substring(end + 1));
        }
        // Retrieve uuid....
        start = response.toUpperCase().indexOf("UUID:");
        if (start == -1) {
            throw new Exception("No UUID found");
        }
        end = response.indexOf("::", start);
        if (end == -1) {
            end = response.indexOf("\r\n", start);
            if (end == -1) {
                throw new Exception("Invalid UUID found");
            }
        }
        String uuid = response.substring(start + 5, end);

        UPnPDeviceImpl device = new UPnPDeviceImpl(context, uuid, location, ip, port, localIp, eventHandler);

        return device;
    }

    protected static UPnPDeviceImpl parseNotifyMessage(BundleContext context, String message, UPnPEventHandler eventHandler) throws Exception {
        if (!message.startsWith("NOTIFY * HTTP/1.1")) {
            return null;
        }
        int start = message.toUpperCase().indexOf("NTS:");
        if (start == -1) {
            throw new Exception("No NTS parameter in message:\n" + message);
        }
        int end = message.indexOf("\r\n", start);
        if (end == -1) {
            throw new Exception("Not a NOTIFY message in message:\n" + message);
        }
        String NTS = message.substring(start + 4, end).trim().toLowerCase();

        start = message.toUpperCase().indexOf("NT:");
        if (start == -1) {
            throw new Exception("No NT parameter in message:\n" + message);
        }
        end = message.indexOf("\r\n", start);
        if (end == -1) {
            throw new Exception("Invalid NT parameter in message:\n" + message);
        }
        String NT = message.substring(start + 3, end).trim();

        String ip = "";
        int port = 0;
        String path = "";
        String location = "";

        if (NTS.equals("ssdp:alive")) {
            start = message.toUpperCase().indexOf("LOCATION:");
            if (start == -1) {
                throw new Exception("No LOCATION in message:\n" + message);
            }
            end = message.indexOf("\r\n", start);
            if (end == -1) {
                throw new Exception("Invalid LOCATION parameter in message:\n" + message);
            }
            location = message.substring(start + 9, end).trim();

            start = location.toUpperCase().indexOf("HTTP://");
            if (start == -1) {
                throw new Exception("Invalid URL in message:\n" + message);
            }
            end = location.toUpperCase().indexOf('/', start + 7);
            if (end == -1) {
                throw new Exception("Invalid URL in message:\n" + message);
            }

            String ipnport = location.substring(start + 7, end);
            path = location.substring(end);

            ip = ipnport;
            port = 80;
            end = ipnport.indexOf(':');
            if (end != -1) {
                ip = ipnport.substring(0, end);
                port = Integer.parseInt(ipnport.substring(end + 1));
            }
        }
        start = message.toUpperCase().indexOf("USN:");
        start = message.toUpperCase().indexOf("UUID:", start);
        if (start == -1) {
            throw new Exception("No UUID in message:\n" + message);
        }
        end = message.indexOf("::", start);
        if (end == -1) {
            end = message.indexOf("\r\n", start);
            if (end == -1) {
                throw new Exception("Invalid UUID in message:\n" + message);
            }
        }
        String uuid = message.substring(start + 5, end).trim();

        UPnPDeviceImpl device = new UPnPDeviceImpl(context, uuid, location, ip, port, "", eventHandler);

        // Flag the device for removal if it's not an "alive" message (i.e. byebye)
        if (!NTS.equals("ssdp:alive")) {
            device.setExpired();
        }

        return device;
    }

    private UPnPMessageParser() {
    }
}
