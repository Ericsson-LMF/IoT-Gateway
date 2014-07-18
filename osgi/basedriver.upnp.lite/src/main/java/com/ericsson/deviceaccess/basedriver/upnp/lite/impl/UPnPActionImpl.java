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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;
import org.kxml2.io.KXmlParser;
import org.osgi.service.upnp.UPnPAction;
import org.osgi.service.upnp.UPnPStateVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParser;

public class UPnPActionImpl implements UPnPAction {

    private static final Logger log = LoggerFactory.getLogger(UPnPActionImpl.class);
    UPnPServiceImpl m_service;
    private String name;
    private LinkedHashMap inputArgumentNames;
    private LinkedHashMap outputArgumentNames;

    protected UPnPActionImpl(UPnPServiceImpl m_service, String name, LinkedHashMap inputArgumentNames, LinkedHashMap outputArgumentNames) {
        this.m_service = m_service;
        this.name = name;
        this.inputArgumentNames = inputArgumentNames;
        this.outputArgumentNames = outputArgumentNames;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getReturnArgumentName() {
        return null;
    }

    @Override
    public String[] getInputArgumentNames() {
        return (String[]) inputArgumentNames.keySet().toArray(new String[0]);
    }

    @Override
    public String[] getOutputArgumentNames() {
        return (String[]) outputArgumentNames.keySet().toArray(new String[0]);
    }

    @Override
    public UPnPStateVariable getStateVariable(String argumentName) {
        String stateVarName = (String) outputArgumentNames.get(argumentName);
        return m_service.getStateVariable(stateVarName);
    }

    @Override
    public Dictionary invoke(Dictionary args) throws Exception {
        Properties ret = new Properties();

        // Create action arguments
        StringBuffer arguments = new StringBuffer();
        for (Iterator i = inputArgumentNames.keySet().iterator(); i.hasNext();) {
            String argumentName = (String) i.next();
            String argumentValue = (String) args.get(argumentName);
            if (argumentValue != null) {
                argumentValue = UPnPUtils.escapeXml(argumentValue);
            } else {
                argumentValue = "";
            }

            if (argumentValue.length() == 0) {
                arguments.append("         <").append(argumentName).append(" />\r\n");
            } else {
                arguments.append("         <").append(argumentName).append(">").append(argumentValue).append("</").append(argumentName).append(">\r\n");
            }
        }

        // Insert UPnP request in a SOAP body
        String actionDataTransport = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n"
                + "<s:Envelope s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">\r\n"
                + "   <s:Body>\r\n"
                + "      <u:" + name + " xmlns:u=\"" + m_service.m_serviceType + "\">\r\n"
                + arguments
                + "      </u:" + name + ">\r\n"
                + "   </s:Body>\r\n"
                + "</s:Envelope>";

        // Send the request
        try {
            /*
             URL url = new URL(m_service.m_controlUrl);
             HttpURLConnection conn = (HttpURLConnection) url.openConnection();
             conn.setDoInput(true);
             conn.setDoOutput(true);
             conn.setUseCaches(false);
             conn.setRequestProperty("Content-Type", "text/xml; charset=\"utf-8\"");
             conn.setRequestProperty("SOAPACTION", "\"" + m_service.m_serviceType + "#" + name + "\"");
             conn.setRequestProperty("Content-Length", "" + Integer.toString(actionDataTransport.getBytes("UTF-8").length));

             DataOutputStream out = new DataOutputStream(conn.getOutputStream());
             out.write(actionDataTransport.getBytes("UTF-8"));
             out.flush();
             */

            URL url = new URL(m_service.m_controlUrl);
            String host = url.getHost();
            int port = (url.getPort() > 0 ? url.getPort() : 80);
            SocketAddress sockaddr = new InetSocketAddress(host, port);
            Socket sock = new Socket();
            sock.connect(sockaddr, 10000);

            byte[] data = actionDataTransport.getBytes(StandardCharsets.UTF_8);
            OutputStream os = sock.getOutputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
            wr.write("POST " + url.getFile() + " HTTP/1.1\r\n");
            wr.write("HOST: " + host + ":" + port + "\r\n");
            wr.write("SOAPACTION: " + "\"" + m_service.m_serviceType + "#" + name + "\"\r\n");
            wr.write("CONTENT-TYPE: text/xml; charset=\"utf-8\"\r\n");
            wr.write("Content-Length: " + data.length + "\r\n");
            wr.write("\r\n");
            wr.write(actionDataTransport);
            wr.flush();

            log.debug("Invoking action + " + name + ":\n"
                    + "POST " + url.getFile() + " HTTP/1.1\n"
                    + "HOST: " + host + ":" + port + "\n"
                    + "CONTENT-TYPE: text/xml; charset=\"utf-8\"\n"
                    + "SOAPACTION: " + "\"" + m_service.m_serviceType + "#" + name + "\"\n"
                    + "Content-Length: " + data.length + "\n"
                    + "\n" + actionDataTransport);

            // Remove header fields
            StringBuilder buf = new StringBuilder("Got response from action " + name + ":\n");
            String inputLine;
            while ((inputLine = in.readLine()).trim().length() != 0) {
                buf.append("\n").append(inputLine);
            }

            // Parse response
            String responseTag = name + "Response";
            String tagName = "";
            String tagValue = "";
            boolean inResponseTag = false;
            XmlPullParser p = new KXmlParser();
            p.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
            //InputStream in = conn.getInputStream();
            //p.setInput(in, null);
            p.setInput(in);
            while (!(p.getEventType() == XmlPullParser.END_TAG && p.getName().equalsIgnoreCase("Envelope"))) {
                if (p.getEventType() == XmlPullParser.START_TAG) {
                    if (p.getName().equalsIgnoreCase(responseTag)) {
                        inResponseTag = true;
                    } else {
                        tagName = p.getName();
                    }
                } else if (p.getEventType() == XmlPullParser.TEXT) {
                    tagValue = p.getText();
                    if (inResponseTag) {
                        buf.append("\nTag: ").append(tagName).append(" = ").append(tagValue);
                        UPnPStateVariableImpl var = (UPnPStateVariableImpl) getStateVariable(tagName);
                        if (var != null && tagValue != null && tagValue.trim().length() > 0) {
                            Object val = UPnPUtils.parseString(tagValue, var.getUPnPDataType());
                            ret.put(tagName, val);
                        }
                    }
                } else if (p.getEventType() == XmlPullParser.END_TAG) {
                    if (p.getName().equalsIgnoreCase(responseTag)) {
                        inResponseTag = false;
                    }
                }
                p.next();
            }

            log.debug(buf.toString());

            try {
                in.close();
                //out.close();
                wr.close();
            } catch (IOException t) {
            }
        } catch (Exception e) {
            log.warn(UPnPServiceImpl.class.getName() + " invokeAction, close failed", e);
        }

        return ret;
    }
}
