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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NanoHTTPD {

    private static final Logger log = LoggerFactory.getLogger(NanoHTTPD.class);
    public static final String HTTP_OK = "200 OK";
    public static final String HTTP_PARTIALCONTENT = "206 Partial Content";
    public static final String HTTP_RANGE_NOT_SATISFIABLE = "416 Requested Range Not Satisfiable";
    public static final String HTTP_REDIRECT = "301 Moved Permanently";
    public static final String HTTP_FORBIDDEN = "403 Forbidden";
    public static final String HTTP_NOTFOUND = "404 Not Found";
    public static final String HTTP_BADREQUEST = "400 Bad Request";
    public static final String HTTP_INTERNALERROR = "500 Internal Server Error";
    public static final String HTTP_NOTIMPLEMENTED = "501 Not Implemented";
    public static final String MIME_PLAINTEXT = "text/plain";
    public static final String MIME_HTML = "text/html";
    public static final String MIME_DEFAULT_BINARY = "application/octet-stream";
    public static final String MIME_XML = "text/xml";
    /**
     * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
     */
    private static Hashtable theMimeTypes = new Hashtable();
    /**
     * GMT date formatter
     */
    private static java.text.SimpleDateFormat gmtFrmt;

    static {
        StringTokenizer st = new StringTokenizer(
                "htm		text/html "
                + "xml		text/xml "
                + "txt		text/plain "
                + "class		application/octet-stream ");
        while (st.hasMoreTokens()) {
            theMimeTypes.put(st.nextToken(), st.nextToken());
        }
    }

    static {
        gmtFrmt = new java.text.SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
    private int myTcpPort;
    private ServerSocket myServerSocket;
    private Thread myThread;

    public NanoHTTPD() {
        myServerSocket = null;
        try {
            myServerSocket = new ServerSocket(0);
            myTcpPort = myServerSocket.getLocalPort();

            myThread = new Thread(() -> {
                try {
                    while (true) {
                        new HTTPSession(myServerSocket.accept());
                    }
                } catch (IOException ioe) {
                }
            });
            myThread.setDaemon(true);
            myThread.start();
        } catch (IOException ioe) {
        }
    }

    public void shutdown() {
        try {
            myServerSocket.close();
            myThread.join();
        } catch (IOException | InterruptedException ioe) {
        }
    }

    public int getListenPort() {
        return myTcpPort;
    }

    public Response serve(String uri, String method, Properties header, Properties parms, BufferedReader in) {
        return new Response();
    }

    /**
     * URL-encodes everything between "/"-characters. Encodes spaces as '%20'
     * instead of '+'.
     */
    private String encodeUri(String uri) {
        String newUri = "";
        StringTokenizer st = new StringTokenizer(uri, "/ ", true);
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            switch (tok) {
                case "/": {
                    newUri += "/";
                }
                break;
                case " ": {
                    newUri += "%20";
                }
                break;
                default:
                    newUri += URLEncoder.encode(tok);
                    // For Java 1.4 you'll want to use this instead:
                    // try { newUri += URLEncoder.encode( tok, "UTF-8" ); } catch ( java.io.UnsupportedEncodingException uee ) {}
                    break;
            }
        }
        return newUri;
    }

    public class Response {

        public String status;
        public String mimeType;
        public InputStream data;
        public Properties header = new Properties();

        public Response() {
            this.status = HTTP_OK;
        }

        public Response(String status, String mimeType, String txt) {
            this.status = status;
            this.mimeType = mimeType;
            this.data = new ByteArrayInputStream(txt.getBytes(StandardCharsets.UTF_8));
        }

        public void addHeader(String name, String value) {
            header.put(name, value);
        }
    }

    private class HTTPSession implements Runnable {

        private Socket mySocket;

        HTTPSession(Socket s) {
            mySocket = s;
            Thread t = new Thread(this);
            t.setDaemon(true);
            t.start();
        }

        @Override
        public void run() {
            try {
                try (InputStream is = mySocket.getInputStream()) {
                    if (is == null) {
                        return;
                    }
                    int bufsize = 8192;
                    byte[] buf = new byte[bufsize];
                    int rlen = is.read(buf, 0, bufsize);
                    if (rlen <= 0) {
                        return;
                    }
                    String raw = new String(buf, 0, rlen);
                    //System.out.println(raw);
                    // Create a BufferedReader for parsing the header.
                    ByteArrayInputStream hbis = new ByteArrayInputStream(buf, 0, rlen);
                    BufferedReader hin = new BufferedReader(new InputStreamReader(hbis));
                    Properties pre = new Properties();
                    Properties parms = new Properties();
                    Properties header = new Properties();
                    Properties files = new Properties();
                    // Decode the header into parms and header java properties
                    decodeHeader(hin, pre, parms, header);
                    String method = pre.getProperty("method");
                    String uri = pre.getProperty("uri");
                    long size = 0x7FFFFFFFFFFFFFFFl;
                    String contentLength = header.getProperty("content-length");
                    if (contentLength != null) {
                        try {
                            size = Integer.parseInt(contentLength);
                        } catch (NumberFormatException ex) {
                        }
                    }
                    // We are looking for the byte separating header from body.
                    // It must be the last byte of the first two sequential new lines.
                    int splitbyte = 0;
                    boolean sbfound = false;
                    while (splitbyte < rlen) {
                        if (buf[splitbyte] == '\r' && buf[++splitbyte] == '\n' && buf[++splitbyte] == '\r' && buf[++splitbyte] == '\n') {
                            sbfound = true;
                            break;
                        }
                        splitbyte++;
                    }
                    splitbyte++;
                    // Write the part of body already read to ByteArrayOutputStream f
                    ByteArrayOutputStream f = new ByteArrayOutputStream();
                    if (splitbyte < rlen) {
                        f.write(buf, splitbyte, rlen - splitbyte);
                    }
                    if (splitbyte < rlen) {
                        size -= rlen - splitbyte + 1;
                    } else if (!sbfound || size == 0x7FFFFFFFFFFFFFFFl) {
                        size = 0;
                    }
                    // Now read all the body and write it to f
                    buf = new byte[512];
                    while (rlen >= 0 && size > 0) {
                        rlen = is.read(buf, 0, 512);
                        size -= rlen;
                        if (rlen > 0) {
                            f.write(buf, 0, rlen);
                        }
                    }
                    // Get the raw body as a byte []
                    byte[] fbuf = f.toByteArray();
                    raw = new String(fbuf);
                    //System.out.println("Raw: " + raw);
                    // Create a BufferedReader for easily reading it as string.
                    ByteArrayInputStream bin = new ByteArrayInputStream(fbuf);
                    // If the method is POST, there may be parameters
                    // in data section, too, read it:
                    //System.out.println("#########################NANO: " + method);
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(bin))) {
                        // If the method is POST, there may be parameters
                        // in data section, too, read it:
                        //System.out.println("#########################NANO: " + method);
                        if (method.equalsIgnoreCase("POST")) {
                            String contentType = "";
                            String contentTypeHeader = header.getProperty("content-type");
                            StringTokenizer st = new StringTokenizer(contentTypeHeader, "; ");
                            if (st.hasMoreTokens()) {
                                contentType = st.nextToken();
                            }

                            // Handle application/x-www-form-urlencoded
                            String postLine = "";
                            char pbuf[] = new char[512];
                            int read = in.read(pbuf);
                            while (read >= 0 && !postLine.endsWith("\r\n")) {
                                postLine += String.valueOf(pbuf, 0, read);
                                read = in.read(pbuf);
                            }
                            postLine = postLine.trim();
                            //System.out.println(postLine);
                            //System.out.println("#########################NANO");
                            decodeParms(postLine, parms);
                        }
                        // Ok, now do the serve()
                        Response r = serve(uri, method, header, parms, in);
                        if (r == null) {
                            sendError(HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: Serve() returned a null response.");
                        } else {
                            sendResponse(r.status, r.mimeType, r.header, r.data);
                        }
                    }
                }
            } catch (IOException ioe) {
                try {
                    sendError(HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
                } catch (InterruptedException t) {
                }
            } catch (InterruptedException ie) {
                // Thrown by sendError, ignore and exit the thread.
            }
        }

        /**
         * Decodes the sent headers and loads the data into java Properties' key
         * - value pairs
         *
         */
        private void decodeHeader(BufferedReader in, Properties pre, Properties parms, Properties header) throws InterruptedException {
            try {
                // Read the request line
                String inLine = in.readLine();
                if (inLine == null) {
                    return;
                }
                StringTokenizer st = new StringTokenizer(inLine);
                if (!st.hasMoreTokens()) {
                    sendError(HTTP_BADREQUEST, "BAD REQUEST: Syntax error. Usage: GET /example/file.html");
                }
                String method = st.nextToken();
                pre.put("method", method);
                if (!st.hasMoreTokens()) {
                    sendError(HTTP_BADREQUEST, "BAD REQUEST: Missing URI. Usage: GET /example/file.html");
                }
                String uri = st.nextToken();
                // Decode parameters from the URI
                int qmi = uri.indexOf('?');
                if (qmi >= 0) {
                    decodeParms(uri.substring(qmi + 1), parms);
                    uri = decodePercent(uri.substring(0, qmi));
                } else {
                    uri = decodePercent(uri);
                }
                // If there's another token, it's protocol version,
                // followed by HTTP headers. Ignore version but parse headers.
                // NOTE: this now forces header names lowercase since they are
                // case insensitive and vary by client.
                if (st.hasMoreTokens()) {
                    String line = in.readLine();
                    while (line != null && line.trim().length() > 0) {
                        int p = line.indexOf(':');
                        if (p >= 0) {
                            header.put(line.substring(0, p).trim().toLowerCase(), line.substring(p + 1).trim());
                        }
                        line = in.readLine();
                    }
                }
                pre.put("uri", uri);
            } catch (IOException ioe) {
                sendError(HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
            }
        }

        /**
         * Decodes the percent encoding scheme. <br/>
         * For example: "an+example%20string" -> "an example string"
         */
        private String decodePercent(String str) throws InterruptedException {
            try {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < str.length(); i++) {
                    char c = str.charAt(i);
                    switch (c) {
                        case '+':
                            sb.append(' ');
                            break;
                        case '%':
                            sb.append((char) Integer.parseInt(str.substring(i + 1, i + 3), 16));
                            i += 2;
                            break;
                        default:
                            sb.append(c);
                            break;
                    }
                }
                return sb.toString();
            } catch (NumberFormatException e) {
                sendError(HTTP_BADREQUEST, "BAD REQUEST: Bad percent-encoding.");
                return null;
            }
        }

        /**
         * Decodes parameters in percent-encoded URI-format ( e.g.
         * "name=Jack%20Daniels&pass=Single%20Malt" ) and adds them to given
         * Properties. NOTE: this doesn't support multiple identical keys due to
         * the simplicity of Properties -- if you need multiples, you might want
         * to replace the Properties with a Hashtable of Vectors or such.
         */
        private void decodeParms(String parms, Properties p) throws InterruptedException {
            if (parms == null) {
                return;
            }
            StringTokenizer st = new StringTokenizer(parms, "&");
            while (st.hasMoreTokens()) {
                String e = st.nextToken();
                int sep = e.indexOf('=');
                if (sep >= 0) {
                    p.put(decodePercent(e.substring(0, sep)).trim(),
                            decodePercent(e.substring(sep + 1)));
                }
            }
        }

        /**
         * Returns an error message as a HTTP response and throws
         * InterruptedException to stop further request processing.
         */
        private void sendError(String status, String msg) throws InterruptedException {
            sendResponse(status, MIME_PLAINTEXT, null, new ByteArrayInputStream(msg.getBytes()));
            throw new InterruptedException();
        }

        /**
         * Sends given response to the socket.
         */
        private void sendResponse(String status, String mime, Properties header, InputStream data) {
            try {
                if (status == null) {
                    throw new Error("sendResponse(): Status can't be null.");
                }
                try (OutputStream out = mySocket.getOutputStream()) {
                    PrintWriter pw = new PrintWriter(out);
                    pw.print("HTTP/1.1 " + status + " \r\n");
                    if (mime != null) {
                        pw.print("Content-Type: " + mime + "\r\n");
                    }
                    if (header == null || header.getProperty("Date") == null) {
                        pw.print("Date: " + gmtFrmt.format(new Date()) + "\r\n");
                    }
                    if (header != null) {
                        Enumeration e = header.keys();
                        while (e.hasMoreElements()) {
                            String key = (String) e.nextElement();
                            String value = header.getProperty(key);
                            pw.print(key + ": " + value + "\r\n");
                        }
                    }
                    pw.print("\r\n");
                    pw.flush();
                    if (data != null) {
                        int pending = data.available();	// This is to support partial sends, see serveFile()
                        byte[] buff = new byte[2048];
                        while (pending > 0) {
                            int read = data.read(buff, 0, pending > 2048 ? 2048 : pending);
                            if (read <= 0) {
                                break;
                            }
                            out.write(buff, 0, read);
                            pending -= read;
                        }
                    }
                    out.flush();
                }
                if (data != null) {
                    data.close();
                }
            } catch (IOException ioe) {
                // Couldn't write? No can do.
                try {
                    mySocket.close();
                } catch (IOException t) {
                }
            }
        }
    }
}
