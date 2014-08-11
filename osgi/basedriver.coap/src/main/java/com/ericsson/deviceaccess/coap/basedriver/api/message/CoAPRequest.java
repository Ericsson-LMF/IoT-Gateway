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
package com.ericsson.deviceaccess.coap.basedriver.api.message;

import com.ericsson.common.util.BitUtil;
import com.ericsson.deviceaccess.coap.basedriver.api.CoAPException;
import com.ericsson.deviceaccess.coap.basedriver.util.TokenGenerator;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class represents a CoAP request. A request is carried in a Confirmable
 * (CON) or Non-confirmable (NON) message, and if immediately available, the
 * response to a request carried in a Confirmable message is carried in the
 * resulting Acknowledgement (ACK) message. A Token Option is used to match
 * responses to requests independently from the underlying messages
 */
public class CoAPRequest extends CoAPMessage {

    private CoAPRequestListener listener;
    private URI uri;

    /**
     * Constructor
     *
     * @param version version of CoAP
     * @param messageType type of the request
     * @param methodCode code of the request
     * @param messageId message ID
     */
    public CoAPRequest(int version, CoAPMessageType messageType, CoAPRequestCode methodCode, int messageId, byte[] token) {
        super(version, messageType, methodCode, messageId, token);
    }

    public CoAPRequest(int version, CoAPMessageType messageType, CoAPRequestCode methodCode, int messageId) {
        super(version, messageType, methodCode, messageId, null);
    }

    /**
     * Constructor. When this constructor is used, CoAP version 1 will be used
     * by default.
     *
     * @param messageType type of the request
     * @param methodCode code of the request
     * @param messageId message ID
     */
    public CoAPRequest(CoAPMessageType messageType, CoAPRequestCode methodCode, int messageId, byte[] token) {
        super(messageType, methodCode, messageId, token);
    }

    public CoAPRequest(CoAPMessageType messageType, CoAPRequestCode methodCode, int messageId) {
        super(messageType, methodCode, messageId, null);
    }

    /**
     * CoAP URI of this request
     *
     * @param uri CoAP URI of the request
     */
    public void setUri(URI uri) {
        this.uri = uri;
    }

    /**
     * Token header is not anymore automatically generated (as there's an ETSI
     * test case without token header). Thus, this method must be called to
     * generate and add token header to the request (if needed). The token
     * header will be generated based on the URI.
     *
     * @throws CoAPException if the token cannot be created based on the URI
     */
    public void generateTokenHeader() throws CoAPException {
        if (uri == null) {
            throw new CoAPException("Token header cannot be created, URI not set for the request");
        }
        long token = TokenGenerator.createToken(uri);
        byte[] bytes = BitUtil.splitLongToBytes(token);

        setToken(bytes);
    }

    /**
     * Form the URI based on the CoAP request (from option header URI-PATH,
     * PORT-URI AND HOST-URI, store it in the uri variable
     *
     * @param socketAddress
     * @throws com.ericsson.deviceaccess.coap.basedriver.api.CoAPException
     */
    public void createUriFromRequest(SocketAddress socketAddress) throws CoAPException {
        // Get URI path of the message
        String host = "";

        StringBuilder pathParts = new StringBuilder();
        int port = 0;
        for (CoAPOptionHeader header : getOptionHeaders()) {
            CoAPOptionName name = header.getOptionName();
            if (name == CoAPOptionName.URI_PATH) {
                try {
                    String pathPart = new String(header.getValue(), "UTF8");
                    if (!pathPart.startsWith("/")) {
                        pathPart = "/" + pathPart;
                    }
                    pathParts.append(pathPart);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else if (name == CoAPOptionName.URI_HOST) {
                try {
                    host = new String(header.getValue(), "UTF8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else if (name == CoAPOptionName.URI_PORT) {
                short shortInt = BitUtil.mergeBytesToShort(header.getValue()[0], header.getValue()[1]);
                port = shortInt & 0xFFFF;
            }
        }
        // read from the socketaddress, that cannot be null
        if ((host.isEmpty() || port == 0) && socketAddress instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddr = (InetSocketAddress) socketAddress;
            if (host.isEmpty()) {
                host = inetSocketAddr.getAddress().toString();
                if (host.startsWith("/")) {
                    host = host.substring(1);
                }
            }
            if (port == 0) {
                port = inetSocketAddr.getPort();
            }
        }

        try {
            if (host == null || host.isEmpty()) {
                throw new CoAPException("Host not defined");
            }
            uri = new URI("coap", null, host, port, pathParts.toString(), null, null);
        } catch (URISyntaxException e) {
            throw new CoAPException(e);
        }
    }

    @Override
    public CoAPRequestCode getCode() {
        return (CoAPRequestCode) super.getCode();
    }

    /**
     * Form the URI based on the CoAP request (from option header URI-PATH,
     * PORT-URI AND HOST-URI
     *
     * @return constructed uri, or null if port or host was not defined
     * @throws CoAPException
     */
    public URI getUriFromRequest() throws CoAPException {
        // re-generate the URI always when the URI is tried to be accessed
        try {
            createUriFromRequest(getSocketAddress());
            // If creating the uri throws an CoAPException, the host is not
            // defined
        } catch (CoAPException e) {
            return null;
        }
        return uri;
    }

    /**
     * Set a listener for outgoing requests and the events related to those
     *
     * @param listener listener instance that will receive events regarding this
     * CoAP request.
     */
    public void setListener(CoAPRequestListener listener) {
        this.listener = listener;
    }

    /**
     * Get the listener instance related to this request.
     *
     * @return listener
     */
    public CoAPRequestListener getListener() {
        return listener;
    }

    /**
     * This method returns a list of CoAP option headers without max-age and
     * etag headers. The method is used when matching two requests for caching
     * purposes.
     *
     * @return
     */
    public Set<CoAPOptionHeader> optionsForMatching() {
        return getOptionHeaders()
                .stream()
                .filter(h -> h.getOptionName() != CoAPOptionName.ETAG)
                .filter(h -> h.getOptionName() != CoAPOptionName.MAX_AGE)
                .collect(Collectors.toSet());
    }
}
