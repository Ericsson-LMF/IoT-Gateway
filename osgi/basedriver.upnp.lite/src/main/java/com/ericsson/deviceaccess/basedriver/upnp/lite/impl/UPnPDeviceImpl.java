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

import com.ericsson.common.util.LegacyUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.kxml2.io.KXmlParser;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.upnp.UPnPDevice;
import static org.osgi.service.upnp.UPnPDevice.MANUFACTURER;
import static org.osgi.service.upnp.UPnPDevice.MANUFACTURER_URL;
import static org.osgi.service.upnp.UPnPDevice.MODEL_DESCRIPTION;
import static org.osgi.service.upnp.UPnPDevice.MODEL_NAME;
import static org.osgi.service.upnp.UPnPDevice.MODEL_NUMBER;
import static org.osgi.service.upnp.UPnPDevice.PRESENTATION_URL;
import static org.osgi.service.upnp.UPnPDevice.SERIAL_NUMBER;
import static org.osgi.service.upnp.UPnPDevice.UPC;
import org.osgi.service.upnp.UPnPIcon;
import org.osgi.service.upnp.UPnPService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class UPnPDeviceImpl implements UPnPDevice {

    private static final Logger log = LoggerFactory.getLogger(UPnPDeviceImpl.class);
    private String m_ip = null;
    private int m_port = 0;
    private String m_location = null;
    private final Map<String, UPnPService> m_services = new HashMap<>();
    private String m_descriptionXML;
    private long m_timestamp;
    private boolean isReady = false;
    private final List<UPnPIcon> m_icons = new ArrayList<>();
    private final String m_localIp;
    private final UPnPEventHandler m_eventHandler;
    private final BundleContext context;
    private ServiceRegistration sr = null;
    private final Map<String, Object> props = new HashMap<>();
    private final Map<String, String> map = new HashMap();

    {
        put("friendlyName", FRIENDLY_NAME);
        put("deviceType", TYPE);
        put("modelName", MODEL_NAME);
        put("modelNumber", MODEL_NUMBER);
        put("modelDescription", MODEL_DESCRIPTION);
        put("UPC", UPC);
        put("serialNumber", SERIAL_NUMBER);
        put("presentationURL", PRESENTATION_URL);
        put("manufacturer", MANUFACTURER);
        put("manufacturerURL", MANUFACTURER_URL);
    }

    protected UPnPDeviceImpl(BundleContext context, String uuid, String location, String ip, int port, String localIp, UPnPEventHandler eventHandler) {
        this.context = context;
        props.put(UDN, uuid);
        m_ip = ip;
        m_port = port;
        m_location = location;
        m_timestamp = System.currentTimeMillis();
        m_localIp = localIp;
        m_eventHandler = eventHandler;
    }

    @Override
    public UPnPIcon[] getIcons(String locale) {
        return m_icons.toArray(new UPnPIcon[m_icons.size()]);
    }

    @Override
    public Dictionary getDescriptions(String locale) {
        return LegacyUtil.toDictionary(props);
    }

    @Override
    public UPnPService[] getServices() {
        return m_services.values().toArray(new UPnPService[0]);
    }

    @Override
    public UPnPService getService(String serviceId) {
        return m_services.get(serviceId);
    }

    public UPnPIcon[] getIcons() {
        return m_icons.toArray(new UPnPIcon[m_icons.size()]);
    }

    // Returns false if no response has been received from the device within two minutes
    protected boolean isAlive() {
        if (System.currentTimeMillis() - m_timestamp > 120 * 1000) {
            return false;
        } else {
            return true;
        }
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
        if (relativeURL.startsWith("/")) {
            if (urlBase.toLowerCase().startsWith("http")) {
                if (urlBase.endsWith("/")) {
                    return urlBase.substring(0, urlBase.length() - 1) + relativeURL;
                } else {
                    return urlBase + relativeURL;
                }
            } else {
                return "http://" + m_ip + ":" + m_port + relativeURL;
            }
        } else {
            if (urlBase.startsWith("http")) {
                return urlBase + (urlBase.endsWith("/") ? relativeURL : "/" + relativeURL);
            } else {
                if (url.getPath().endsWith("/")) {
                    return url.getProtocol() + "://" + url.getHost() + ":" + url.getPort() + url.getPath() + relativeURL;
                } else {
                    return url.getProtocol() + "://" + url.getHost() + ":" + url.getPort() + url.getPath() + "/" + relativeURL;
                }
            }
        }
    }

    public String getLocalIp() {
        return m_localIp;
    }

    protected String getUuid() {
        return (String) props.get(UDN);
    }

    protected void stop() {
        if (sr != null) {
            sr.unregister();
        }

        m_services.values()
                .stream()
                .map(s -> (UPnPServiceImpl) s)
                .forEach(s -> s.stop());
    }

    private void put(String key, String value) {
        map.put(key.toLowerCase(), value);
    }

    protected void start() throws Exception {
        final UPnPDeviceImpl self = this;
        Thread receiveThread = new Thread() {

            @Override
            public void run() {
                try {
                    // Get the device description
                    URL url = new URL(m_location);
                    try {
                        StringBuilder sb = new StringBuilder();
                        try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()))) {
                            String inputLine;
                            while ((inputLine = in.readLine()) != null) {
                                sb.append(inputLine);
                            }
                        }
                        m_descriptionXML = sb.toString();
                    } catch (IOException e) {
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

                    while (p.getEventType() != XmlPullParser.END_DOCUMENT) {
                        try {
                            if (p.getEventType() == XmlPullParser.START_TAG) {
                                tagName = p.getName();
                            } else if (p.getEventType() == XmlPullParser.TEXT) {
                                String value = map.get(tagName.toLowerCase());
                                if (value != null) {
                                    props.put(value, p.getText());
                                } else if (tagName.equals("SCPDURL")) {
                                    scpdUrl = p.getText();
                                } else if (tagName.equalsIgnoreCase("serviceType")) {
                                    // TODO: Generate for lower versions as well
                                    serviceType = p.getText();
                                } else if (tagName.equalsIgnoreCase("serviceId")) {
                                    serviceId = p.getText();
                                } else if (tagName.equalsIgnoreCase("eventSubURL")) {
                                    eventSubUrl = p.getText();
                                } else if (tagName.equalsIgnoreCase("controlURL")) {
                                    controlUrl = p.getText();
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
                        } catch (NumberFormatException | XmlPullParserException e) {
                            log.error("Failed to retrieve valid description XML from device + \n" + m_descriptionXML, e);
                        }
                        p.next();
                    }

                    // Get all the service descriptions
                    for (Iterator i = m_services.values().iterator(); i.hasNext();) {
                        UPnPServiceImpl service = (UPnPServiceImpl) i.next();
                        service.start();
                    }

                    // Find the icon with the highest resolution of type png or jpg and put this as a property to be used by the UPnP Adaptor
                    UPnPIcon[] icons = getIcons();
                    UPnPIcon bestMatch = null;
                    int bestMatchSum = 0;
                    for (UPnPIcon icon : icons) {
                        if (icon.getMimeType().equalsIgnoreCase("image/jpeg") || icon.getMimeType().equalsIgnoreCase("image/png")) {
                            int iconSum = icon.getHeight() * icon.getWidth() * icon.getDepth();
                            if (bestMatch == null || bestMatchSum < iconSum) {
                                bestMatch = icon;
                                bestMatchSum = iconSum;
                            }
                        }
                    }

                    if (bestMatch != null) {
                        props.put("GDA_ICON", ((UPnPIconImpl) bestMatch).getUrl());
                    }

                    isReady = true;
                    log.debug("Finished with " + getUuid() + "(" + props.get(FRIENDLY_NAME) + ")");

                    sr = context.registerService(UPnPDevice.class, self, LegacyUtil.toDictionary(props));
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                }
            }
        };
        receiveThread.start();
    }

}
