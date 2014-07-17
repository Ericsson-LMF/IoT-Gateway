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

import com.ericsson.deviceaccess.coap.basedriver.api.CoAPException;
import com.ericsson.deviceaccess.coap.basedriver.util.BitOperations;
import com.ericsson.deviceaccess.coap.basedriver.util.CoAPMessageWriter;
import com.ericsson.deviceaccess.coap.basedriver.util.CoAPOptionHeaderConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class represents a CoAP message. There are two implementations of this
 * class, CoAPRequest and CoAPResponse.
 */
public abstract class CoAPMessage {

    public static final String MESSAGE_ID = "messageID";
    public static final String TOKEN = "token";
    public static final String URI = "uri";

    /**
     * Enum representing the type of the message (as defined in coap core 07)
     */
    public static class CoAPMessageType {

        private final String name;
        private final int no;
        private static final List<CoAPMessageType> types = new ArrayList<>();

        public static final CoAPMessageType CONFIRMABLE = new CoAPMessageType(
                0, "Confirmable");
        public static final CoAPMessageType NON_CONFIRMABLE = new CoAPMessageType(
                1, "Non-Confirmable");
        public static final CoAPMessageType ACKNOWLEDGEMENT = new CoAPMessageType(
                2, "Acknowledgement");
        public static final CoAPMessageType RESET = new CoAPMessageType(3,
                "Reset");

        private CoAPMessageType(int no, String name) {
            this.name = name;
            this.no = no;
            types.add(this);
        }

        public String getName() {
            return this.name;
        }

        public int getNo() {
            return this.no;
        }

        public static CoAPMessageType getType(int no) {
            for (CoAPMessageType type : types) {
                if (type.getNo() == no) {
                    return type;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return no + ", " + name;
        }
    }

    // Core CoAP 07 draft
    /**
     * Version (Ver): 2-bit unsigned integer. Indicates the CoAP version number.
     * Implementations of this specification MUST set this field to 1. Other
     * values are reserved for future versions (see also Section 7.3.1).
     */
    private static final int VERSION_07 = 1;

    /**
     * Type (T): 2-bit unsigned integer.
     */
    private CoAPMessageType messageType;

    /*
     * Option Count (OC): 4-bit unsigned integer.
     */
    // private int optionCount;
    /**
     * Code: 8-bit unsigned integer. Indicates if the message carries a request
     * (1-31) or a response (64-191), or is empty (0). (All other code values
     * are reserved.) In case of a request, the Code field indicates the Request
     * Method; in case of a response a Response Code.
     */
    private int code;

    /**
     * Message ID: 16-bit unsigned integer. Used for the detection of message
     * duplication, and to match messages of type Acknowledgement/Reset and
     * messages of type Confirmable.
     */
    private int messageId;

    private byte[] payload;

    /**
     * CoAP version of the message. In core 07, the version is 1
     */
    private int version;

    // Number of transmissions this far (0-4)
    private int retransmissions;

    // Current timeout value (in milliseconds). For each retransmission, value
    // is doubled
    private int timeout;

    // boolean indicating if this message has been canceled
    private boolean canceled;

    // constants as defined in core draft 07
    private static final int RESPONSE_TIMEOUT = 2000;

    private static final double RESPONSE_RANDOM_FACTOR = 1.5;

    private InetSocketAddress remoteSocketAddress;

    /**
     * List of options. Options MUST appear in order of their Option Number.
     */
    private LinkedList<CoAPOptionHeader> headers;

    /**
     * Constructor.
     *
     * @param version CoAP version
     * @param messageType type of message
     * @param methodCode method code
     * @param messageId message ID
     */
    public CoAPMessage(int version, CoAPMessageType messageType,
            int methodCode, int messageId) {
        this(messageType, methodCode, messageId);
        this.version = version;
    }

    /**
     * Constructor. When this constructor is used, CoAP version 1 will be used.
     *
     * @param messageType type of message
     * @param methodCode method code
     * @param messageId message ID
     */
    public CoAPMessage(CoAPMessageType messageType, int methodCode,
            int messageId) {
        this.headers = new LinkedList();
        this.code = methodCode;
        this.messageType = messageType;
        this.version = VERSION_07;
        this.messageId = messageId;
        // TODO Generate message ID
        this.timeout = 0;
        this.retransmissions = 0;
        this.canceled = false;
        // set timeout value for confirmable message
        if (this.messageType == CoAPMessageType.CONFIRMABLE) {
            this.setTimeout();
        }
        this.payload = null;
    }

    /**
     * Set CoAP version of this message. For implementations of draft 07,
     * version 1 is used.
     *
     * @param version CoAP version of this message
     */
    public void setVersion(short version) {
        this.version = version;
    }

    /**
     * Get the CoAP version of this message
     *
     * @return CoAP version of this message
     */
    public int getVersion() {
        return this.version;
    }

    /**
     * Set the type of this CoAP message
     *
     * @param messageType message type to be set
     */
    public void setMessageType(CoAPMessageType messageType) {
        this.messageType = messageType;
    }

    /**
     * Get CoAP message type of this message
     *
     * @return CoAP message type of this message
     */
    public CoAPMessageType getMessageType() {
        return this.messageType;
    }

    /**
     * Get the option count of this message.
     *
     * @return option count of this message
     */
    public int getOptionCount() {
        return this.headers.size();
    }

    /**
     * Set code for this message.
     *
     * @param code code to set
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * Get the code of this message
     *
     * @return code of this message
     */
    public int getCode() {
        return this.code;
    }

    /**
     * Set message ID for this message
     *
     * @param messageId message ID to set
     */
    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    /**
     * Get message ID of this message
     *
     * @return message ID of this message
     */
    public int getMessageId() {
        return this.messageId;
    }

    /**
     * Add an option header for this message. This method will validate if the
     * option header of a particular type can be added or not in this message.
     * The rules for adding different option headers are based on the
     * draft-ietf-core-coap-08.
     *
     * Also, there are only 4 bits to represent the number of bits that can be
     * present in a message. Thus, if the nof headers is already 15, no further
     * options can be added.
     *
     * Proxy-uri header is handled in a different way than the other headers. In
     * case the Proxy-uri header is to be added, other URI related headers
     * cannot be included in the same message acc to core-coap-08. They'll be
     * removed by this method, and the method will return true.
     *
     * In the case of other headers, that can occur a limited number of times,
     * this method will check if the particular header is already present. If a
     * header that can occur only once is already there, this method will return
     * false
     *
     * @param option option header to add
     * @return boolean value indicating if a header could be added in the
     * message.
     */
    public synchronized boolean addOptionHeader(CoAPOptionHeader option) {
        if (this.headers.size() == 15) {
            return false;
        }

        boolean ok = this.okToAddHeader(option);
        if (ok) {
            this.headers.add(option);
        }
        return ok;
    }

    /**
     * This method will check the conditions for adding new headers. For
     * example, token header must be present only once. If Proxy-uri is present,
     * no Uri-host, Uri-port, Uri-path or Uri-query can be present. In case the
     * Proxy-uri header is to be added, Uri-host, Uri-port, Uri-path and
     * Uri-query headers will be removed if they are present in the message.
     *
     *
     * @param header
     * @return true, if header can be added. false, if the header cannot be
     * added
     */
    private boolean okToAddHeader(CoAPOptionHeader header) {

        // TODO make private methods for each header
        // TODO make if elses..
        // Token header can be added only once
        if (header.getOptionNumber() == CoAPOptionName.TOKEN.getNo()
                && this.getTokenHeader() != null) {

            /*
             CoAPActivator.logger
             .info("Token header already included in the message");
             */
            return false;
        }

        // If contains proxy-uri header(s), it must take precedence over
        // uri-host, uri-port, uri-path & uri-query. thus do not allow these
        // headers to be added if proxy-uri is there
        String optionName = header.getOptionName();
        if (this.getOptionHeaders(CoAPOptionName.PROXY_URI).size() > 0
                && (optionName.equals(CoAPOptionName.URI_HOST.getName())
                || optionName.equals(CoAPOptionName.URI_PATH.getName())
                || optionName.equals(CoAPOptionName.URI_PORT.getName()))) {

            /*
             CoAPActivator.logger
             .info("Proxy-uri option in the message, not possible to add ["
             + header.getOptionName() + "] option header");
             */
            return false;
        }

        // Uri-host should be added only once
        if (header.getOptionNumber() == CoAPOptionName.URI_HOST.getNo()
                && this.getUriHostOptionHeader() != null) {
            /*
             CoAPActivator.logger
             .info("Uri-host header already included in the message");
             */

            return false;
        }

        // Uri-port should be added only once
        if (header.getOptionNumber() == CoAPOptionName.URI_PORT.getNo()
                && this.findOptionHeader(CoAPOptionName.URI_PORT) != null) {
            /*
             CoAPActivator.logger
             .info("Uri-port header already included in the message");
             */
            return false;
        }

        // If Uri-Path, it should contain only one segment of the absolute path
        if (header.getOptionNumber() == CoAPOptionName.URI_PATH.getNo()) {

            if (header.getLength() > 0) {
                String path = new String(header.getValue());
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }

                if (path.contains("/")) {
                    /*
                     CoAPActivator.logger
                     .warn("Path-Uri header should contain only one segment of the absolute path, cannot contain '/'");
                     */
                    return false;
                }
            }
        }

        // Content-type should be added only once
        if (header.getOptionNumber() == CoAPOptionName.CONTENT_TYPE.getNo()
                && this.findOptionHeader(CoAPOptionName.CONTENT_TYPE) != null) {
            /*
             CoAPActivator.logger
             .info("Content-type option header already included in the message");
             */
            return false;
        }

        // max-age only once
        if (header.getOptionNumber() == CoAPOptionName.MAX_AGE.getNo()
                && this.findOptionHeader(CoAPOptionName.MAX_AGE) != null) {
            /*
             CoAPActivator.logger
             .info("Max-age header already included in the message");
             */
            return false;
        }

        // etag once in a response
        if (header.getOptionNumber() == CoAPOptionName.ETAG.getNo()
                && this.findOptionHeader(CoAPOptionName.ETAG) != null
                && (this instanceof CoAPResponse)) {
            /*
             CoAPActivator.logger
             .info("Etag header already included in the message");
             */
            return false;
        }

        // Proxy-uri must take precedence over any of the Uri-Host,
        // Uri-Port, Uri-Path or Uri-Query options. Thus, remove all these
        // headers first!
        if (header.getOptionNumber() == CoAPOptionName.PROXY_URI.getNo()) {

            /*
             CoAPActivator.logger
             .info("Proxy-uri in the message, overwrite Uri-host, Uri-port, Uri-path and Uri-query options");
             */
            // remove uri-host if present
            if (this.getUriHostOptionHeader() != null) {
                CoAPOptionHeader uriHost = this.getUriHostOptionHeader();
                this.headers.remove(uriHost);
            }

            // remove uri-port if present
            if (this.findOptionHeader(CoAPOptionName.URI_PORT) != null) {
                CoAPOptionHeader uriPort = this
                        .findOptionHeader(CoAPOptionName.URI_PORT);
                this.headers.remove(uriPort);
            }

            // remove uri-path header(s) if present
            if (this.findOptionHeader(CoAPOptionName.URI_PATH) != null) {

                List uriPathOptions = this
                        .getOptionHeaders(CoAPOptionName.URI_PATH);
                Iterator it = uriPathOptions.iterator();
                while (it.hasNext()) {
                    CoAPOptionHeader h = (CoAPOptionHeader) it.next();
                    this.headers.remove(h);
                }
            }

            // remove uri-query header(s) if present
            if (this.findOptionHeader(CoAPOptionName.URI_QUERY) != null) {
                List uriPathOptions = this
                        .getOptionHeaders(CoAPOptionName.URI_QUERY);
                Iterator it = uriPathOptions.iterator();
                while (it.hasNext()) {
                    CoAPOptionHeader h = (CoAPOptionHeader) it.next();
                    this.headers.remove(h);
                }
            }
            return true;
        }

        // if-none-match only once
        if (header.getOptionNumber() == CoAPOptionName.IF_NONE_MATCH.getNo()
                && this.findOptionHeader(CoAPOptionName.IF_NONE_MATCH) != null) {
            /*
             CoAPActivator.logger
             .info("If-none-match header already included in the message");
             }*/
            return false;
        }

        // observe only once
        if (header.getOptionNumber() == CoAPOptionName.OBSERVE.getNo()
                && this.findOptionHeader(CoAPOptionName.OBSERVE) != null) {
            /*
             CoAPActivator.logger
             .info("Observe header already included in the message");
             */
            return false;
        }

        // max-ofe only once in a response!
        if (header.getOptionNumber() == CoAPOptionName.MAX_OFE.getNo()
                && (this.findOptionHeader(CoAPOptionName.MAX_OFE) != null || !(this instanceof CoAPResponse))) {
            /*
             CoAPActivator.logger
             .info("Max-ofe header already included in the message or this message is not a response");
             */
            return false;
        }

        if (header.getOptionNumber() == CoAPOptionName.BLOCK1.getNo()
                && this.findOptionHeader(CoAPOptionName.BLOCK1) != null) {
            /*
             CoAPActivator.logger
             .info("Block1 header already included in the message");
             */
            return false;
        }

        return header.getOptionNumber() != CoAPOptionName.BLOCK2.getNo() || this.findOptionHeader(CoAPOptionName.BLOCK2) == null;
    }

    /**
     * Remove an option header from this message
     *
     * @param option option to be removed
     * @return true, if an option header was successfully removed, false
     * otherwise
     */
    public synchronized boolean removeOptionHeader(CoAPOptionHeader option) {
        return this.headers.remove(option);
    }

    /**
     * Set the payload for this mssage
     *
     * @param payload payload as byte array
     */
    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    /**
     * Get the payload of this message
     *
     * @return payload of this message
     */
    public byte[] getPayload() {
        return this.payload;
    }

    /**
     * Get the option headers in this message as LinkedList
     *
     * @return option headers in this message
     */
    public LinkedList getOptionHeaders() {
        return this.headers;
    }

    /**
     * Returns a list of option headers with the given name. An empty list will
     * be returned if no option header(s) with the given name is found.
     *
     * @param optionName option to be found
     * @return list of options with the given option name.
     */
    public List getOptionHeaders(CoAPOptionName optionName) {
        return headers.stream().filter(header -> header.getOptionName().equals(optionName.getName())).collect(Collectors.toList());
    }

    /**
     * Set the headers for this message
     *
     * @param headers list of option headers
     */
    public void setOptionHeaders(LinkedList headers) {
        this.headers.clear();
        this.headers = headers;
    }

    /**
     * Get the number of retransmissions done for this message (0-4)
     *
     * @return number of retransmissions done for this message
     */
    public int getRetransmissions() {
        return this.retransmissions;
    }

    /**
     * Get the timeout value (in milliseconds) for this message.
     *
     * @return timeout in milliseconds
     */
    public int getTimeout() {
        return this.timeout;
    }

    /**
     * Private method to set the timeout after the message has been sent.
     */
    private void setTimeout() {
        // sets the timeout values as milliseconds
        if (this.timeout == 0) {
            int min = RESPONSE_TIMEOUT;
            int max = (int) (RESPONSE_TIMEOUT * RESPONSE_RANDOM_FACTOR);
            Random rand = new Random();

            // Both max & min values are inclusive, that's why +1
            this.timeout = rand.nextInt((max + 1) - min) + min;
        }
    }

    /**
     * If message is retransmitted, the timeout value is doubled and
     * retransmissions number increased. This message is called e.g. from the
     * OutgoingMessageHandler class.
     */
    public void messageRetransmitted() {
        if (retransmissions < 4) {
            this.retransmissions++;
            this.timeout = this.timeout * 2;
        } else if (retransmissions == 4) {
            /*
             CoAPActivator.logger
             .debug("Max nof retransmission reached (4), cancel this message");
             */
        }
    }

    /**
     * Returns true if the retransmission counter for this message has reached
     * MAX_RETRANSMIT on a timeout or if the endpoint receives a reset message.
     *
     * @return true, if this message has been canceled
     */
    public boolean messageCanceled() {
        return this.canceled;
    }

    /**
     * If the retransmission counter reaches MAX_RETRANSMIT on a timeout, or if
     * the end-point receives a reset message, then the attempt to transmit the
     * message is canceled and the application process informed of failure.
     */
    public void setMessageCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    /**
     * Set the destination socket address for this message
     *
     * @param remoteSocketAddress
     */
    public void setSocketAddress(InetSocketAddress remoteSocketAddress) {
        this.remoteSocketAddress = remoteSocketAddress;
    }

    /**
     * Returns the destination socket address for this message
     *
     * @return destination socket address for this message, or null if no
     * destination address is set yet
     */
    public InetSocketAddress getSocketAddress() {
        return this.remoteSocketAddress;
    }

    /**
     * Get the identifier of this message (note, identifier != message ID). The
     * identifier is constructed of three variables, the host, port and message
     * ID
     *
     * @return unique identifier for this message, or null if no destination
     * address is set yet
     */
    public String getIdentifier() {
        if (this.remoteSocketAddress == null) {
            return null;
        }

        return this.remoteSocketAddress.getAddress().getCanonicalHostName()
                + ":"
                + this.remoteSocketAddress.getPort()
                + ":"
                + this.messageId;
    }

    /**
     * Return the option header from this message (needs to present in all
     * messages)
     *
     * @return token as byte array or null if not found
     */
    public CoAPOptionHeader getTokenHeader() {
        return this.findOptionHeader(CoAPOptionName.TOKEN);
    }

    public CoAPOptionHeader getUriHostOptionHeader() {
        return this.findOptionHeader(CoAPOptionName.URI_HOST);
    }

    private CoAPOptionHeader findOptionHeader(CoAPOptionName name) {
        for (CoAPOptionHeader header : this.headers) {
            if (header.getOptionName().equals(name.getName())) {
                return header;
            }
        }
        return null;
    }

    /**
     * Get this message encoded as byte array
     *
     * @return encoded message
     */
    public byte[] encoded() {
        CoAPMessageWriter writer = new CoAPMessageWriter(this);
        byte[] stream = writer.encode();
        return stream;
    }

    /**
     * A helper method to check if this message relates to an observation
     * relationship. Returns true, if this message contains an "observe" option
     * header, false otherwise.
     *
     * @return true if this message contains "observe" option, false otherwise
     */
    public boolean isObserveMessage() {
        return this.headers.stream().anyMatch(header -> header.getOptionName().equals(CoAPOptionName.OBSERVE.getName()));
    }

    /**
     * Helper method to read the max-age option. If no option is found, default
     * value 60 will be used.
     *
     * @return value of max-age option. if no max-age option is present, default
     * value 60 will be returned.
     * @throws CoAPException if there are more than one max-age option present
     * in the request (only 1 allowed)
     */
    public long getMaxAge() throws CoAPException {
        int maxAge = 60;
        List maxAgeOption = this.getOptionHeaders(CoAPOptionName.MAX_AGE);
        // If max-age is in the message, it should be there only once
        if (maxAgeOption.size() == 1) {
            byte[] bytes = ((CoAPOptionHeader) maxAgeOption.get(0)).getValue();

            if (bytes.length < 4) {
                ByteArrayOutputStream s = new ByteArrayOutputStream();

                int diff = 4 - bytes.length;
                for (int i = 0; i < diff; i++) {
                    s.write(0);
                }

                try {
                    s.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bytes = s.toByteArray();
                maxAge = BitOperations.mergeBytesToInt(bytes[0], bytes[1],
                        bytes[2], bytes[3]);
            } else {
                maxAge = BitOperations.mergeBytesToInt(bytes[0], bytes[1],
                        bytes[2], bytes[3]);
            }
        } else if (maxAgeOption.size() > 1) {
            throw new CoAPException("Too many max-age options found");
        }

        // Make signed int to unsigned int -> becomes long
        long unsignedLong = 0xffffffffL & maxAge;

        return unsignedLong;
    }

    /**
     * Helper method to print out the CoAP message
     *
     * @return String representation of the message with its remote socket
     * address, option headers, type, id, code and payload
     */
    public String logMessage() {
        String logMessage = "*****************************\n";
        try {
            String type;
            if (this instanceof CoAPRequest) {
                type = "Request ";
            } else {
                type = "Response";
            }

            logMessage += "COAP [" + type + "]\n";

            if (this.getSocketAddress() != null) {
                logMessage += "Remote socket address ["
                        + this.getSocketAddress().toString() + "]\n";
            }
            String codeDescription = "";
            if (this instanceof CoAPRequest) {
                try {

                    if (((CoAPRequest) (this)).getUriFromRequest() != null) {
                        String uri = ((CoAPRequest) (this)).getUriFromRequest()
                                .toString();
                        logMessage += "Request URI [" + uri + "]\n";
                    }
                    String name = CoAPMethodCode.getName(this.getCode());
                    if (name != null) {
                        codeDescription = name;
                    }

                } catch (CoAPException e) {
                    e.printStackTrace();
                }

            } else {

                if (CoAPResponseCode.getResponseName(this.getCode()) != null) {
                    codeDescription = CoAPResponseCode.getResponseName(
                            this.getCode()).getDescription();
                }
            }
            if (this.getMessageId() > 0) {
                logMessage += "Message ID [" + this.getMessageId() + "]\n";
            }
            if (this.getMessageType() != null) {
                logMessage += "Message type ["
                        + this.getMessageType().toString() + "]\n";
            }

            logMessage += "Message code [" + this.getCode() + "]\n";
            if (!codeDescription.equals("")) {
                logMessage += type + " description [" + codeDescription
                        + "]\n";
            }

            Iterator it = this.getOptionHeaders().iterator();
            while (it.hasNext()) {
                CoAPOptionHeader h = (CoAPOptionHeader) it.next();

                CoAPOptionHeaderConverter converter = new CoAPOptionHeaderConverter();
                /*
                 String headerValue = "";
                 headerValue = converter.convertOptionHeaderToString(h);
                 logMessage += "Option [" + h.getOptionName() + "] value ["
                 + headerValue + "]\n";
                 */
            }
            // payload
            if (this.getPayload() != null) {
                try {
                    String payloadStr = new String(payload, "UTF-8");
                    logMessage += "Payload \n";
                    logMessage += "[" + payloadStr + "]\n";
                } catch (UnsupportedEncodingException ex) {
                    ex.printStackTrace();
                }
            }
            logMessage += "*****************************\n";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return logMessage;
    }
}
