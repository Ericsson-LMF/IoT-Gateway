package com.ericsson.deviceaccess.basedriver.upnp.lite.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
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

	public String getName() {
		return name;
	}

	public String getReturnArgumentName() {
		return null;
	}

	public String[] getInputArgumentNames() {
		return (String[]) inputArgumentNames.keySet().toArray(new String[0]);
	}

	public String[] getOutputArgumentNames() {
		return (String[]) outputArgumentNames.keySet().toArray(new String[0]);
	}

	public UPnPStateVariable getStateVariable(String argumentName) {
		String stateVarName = (String) outputArgumentNames.get(argumentName);
		return m_service.getStateVariable(stateVarName);
	}

	public Dictionary invoke(Dictionary args) throws Exception {
		Properties ret = new Properties();
		
		// Create action arguments
        StringBuffer arguments = new StringBuffer();
        for (Iterator i = inputArgumentNames.keySet().iterator(); i.hasNext(); ) {
        	String argumentName = (String) i.next();
            String argumentValue = (String)	args.get(argumentName);
            if (argumentValue!=null)
                argumentValue = UPnPUtils.escapeXml(argumentValue);
            else
                argumentValue="";
            
            if (argumentValue.length() == 0)
            	arguments.append("         <" + argumentName + " />\r\n" );
            else
            	arguments.append("         <" + argumentName + ">" + argumentValue + "</" + argumentName + ">\r\n");
        }
        
        // Insert UPnP request in a SOAP body
        String actionDataTransport = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" +
        "<s:Envelope s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">\r\n" +
        "   <s:Body>\r\n" + 
        "      <u:" + name + " xmlns:u=\"" + m_service.m_serviceType + "\">\r\n" + 
        arguments + 
        "      </u:" + name + ">\r\n" +  
        "   </s:Body>\r\n" +
        "</s:Envelope>";
        
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

			byte[] data = actionDataTransport.getBytes("UTF8");
			OutputStream os = sock.getOutputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(os, "UTF8"));
			wr.write("POST " + url.getFile() + " HTTP/1.1\r\n");
			wr.write("HOST: " + host + ":" + port + "\r\n");
			wr.write("SOAPACTION: " + "\"" + m_service.m_serviceType + "#" + name + "\"\r\n");
			wr.write("CONTENT-TYPE: text/xml; charset=\"utf-8\"\r\n");
			wr.write("Content-Length: " + data.length + "\r\n");
			wr.write("\r\n");
			wr.write(actionDataTransport);
			wr.flush();
			
			log.debug("Invoking action + " + name + ":\n" +
				"POST " + url.getFile() + " HTTP/1.1\n" +
				"HOST: " + host + ":" + port + "\n" +
				"CONTENT-TYPE: text/xml; charset=\"utf-8\"\n" + 
				"SOAPACTION: " + "\"" + m_service.m_serviceType + "#" + name + "\"\n" +
				"Content-Length: " + data.length + "\n" +
				"\n" + actionDataTransport);
			
			// Remove header fields
			StringBuffer buf = new StringBuffer("Got response from action " + name + ":\n");
			String inputLine;
			while ((inputLine = in.readLine()).trim().length() != 0)
				buf.append("\n" + inputLine);
        	
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
			while(!(p.getEventType() == XmlPullParser.END_TAG && p.getName().equalsIgnoreCase("Envelope"))) {
				if (p.getEventType() == XmlPullParser.START_TAG) {
					if (p.getName().equalsIgnoreCase(responseTag)) {
						inResponseTag = true;
					} else {
						tagName = p.getName();
					}
				} else if (p.getEventType() == XmlPullParser.TEXT) {
					tagValue = p.getText();
					if (inResponseTag) {
						buf.append("\nTag: " + tagName + " = " + tagValue);
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
			} catch (Throwable t) {}
        } catch (Exception e) {
        	log.warn(UPnPServiceImpl.class.getName() + " invokeAction, close failed", e);
        }
        
		return ret;
    }
}
