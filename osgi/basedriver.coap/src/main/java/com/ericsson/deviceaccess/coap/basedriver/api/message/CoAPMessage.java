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
import com.ericsson.common.util.function.FunctionalUtil;
import com.ericsson.deviceaccess.coap.basedriver.api.CoAPException;
import com.ericsson.deviceaccess.coap.basedriver.util.CoAPMessageWriter;
import com.ericsson.deviceaccess.coap.basedriver.util.CoAPOptionHeaderConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.LoggerFactory;

/**
 * This class represents a CoAP message. There are two implementations of this
 * class, CoAPRequest and CoAPResponse.
 */
public abstract class CoAPMessage {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CoAPMessage.class);
    public static final String MESSAGE_ID = "messageID";
    public static final String TOKEN = "token";
    public static final String URI = "uri";

    // Core CoAP 07 draft
    /**
     * Version (Ver): 2-bit unsigned integer. Indicates the CoAP version number.
     * Implementations of this specification MUST set this field to 1. Other
     * values are reserved for future versions (see also Section 7.3.1).
     */
    private static final int VERSION_07 = 1;
    // constants as defined in core draft 07
    private static final int RESPONSE_TIMEOUT = 2000;
    private static final double RESPONSE_RANDOM_FACTOR = 1.5;

    /**
     * Type (T): 2-bit unsigned integer.
     */
    private CoAPMessageType messageType;

    /**
     * Code: 8-bit unsigned integer. Indicates if the message carries a request
     * (1-31) or a response (64-191), or is empty (0). (All other code values
     * are reserved.) In case of a request, the Code field indicates the Request
     * Method; in case of a response a Response Code.
     */
    private CoAPCode code;

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

    private InetSocketAddress remoteSocketAddress;

    /**
     * List of options. Options MUST appear in order of their Option Number.
     */
    private Map<CoAPOptionName, List<CoAPOptionHeader>> headers;

    private byte[] token;

    /**
     * Constructor.
     *
     * @param version CoAP version
     * @param messageType type of message
     * @param methodCode method code
     * @param messageId message ID
     * @param token token
     */
    public CoAPMessage(int version, CoAPMessageType messageType,
            CoAPCode methodCode, int messageId, byte[] token) {
        this(messageType, methodCode, messageId, token);
        this.version = version;
    }

    /**
     * Constructor. When this constructor is used, CoAP version 1 will be used.
     *
     * @param messageType type of message
     * @param methodCode method code
     * @param messageId message ID
     * @param token token
     */
    public CoAPMessage(CoAPMessageType messageType, CoAPCode methodCode,
            int messageId, byte[] token) {
        this.token = token;
        this.headers = new ConcurrentHashMap<>();
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
        return (int) headers
                .values()
                .stream()
                .filter(c -> !c.isEmpty())
                .count();
    }

    /**
     * Set code for this message.
     *
     * @param code code to set
     */
    public void setCode(CoAPCode code) {
        this.code = code;
    }

    /**
     * Get the code of this message
     *
     * @return code of this message
     */
    public CoAPCode getCode() {
        return this.code;
    }

    public void setToken(byte[] token) {
        this.token = token;
    }

    /**
     * Return the option header from this message (needs to present in all
     * messages)
     *
     * @return token as byte array or null if not found
     */
    public byte[] getToken() {
        return token;
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
        boolean ok = okToAddHeader(option);
        if (ok) {
            get(option).add(option);
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
        // If contains proxy-uri header(s), it must take precedence over
        // uri-host, uri-port, uri-path & uri-query. thus do not allow these
        // headers to be added if proxy-uri is there
        CoAPOptionName optionName = header.getOptionName();
        if (!get(CoAPOptionName.PROXY_URI).isEmpty()
                && (optionName == CoAPOptionName.URI_HOST
                || optionName == CoAPOptionName.URI_PATH
                || optionName == CoAPOptionName.URI_PORT)) {
            LOGGER.info("Proxy-uri option in the message, not possible to add [" + header.getOptionName() + "] option header");
            return false;
        }
        if (!optionName.isRepeatable() && !get(optionName).isEmpty()) {
            // Cannot add multiple repeatables
            return false;
        }

        // If Uri-Path, it should contain only one segment of the absolute path
        if (optionName == CoAPOptionName.URI_PATH && header.getLength() > 0) {
            String path = new String(header.getValue(), StandardCharsets.UTF_8);
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            LOGGER.warn("Path-Uri header should contain only one segment of the absolute path, cannot contain '/'");
            return !path.contains("/");
        }

        // Proxy-uri must take precedence over any of the Uri-Host,
        // Uri-Port, Uri-Path or Uri-Query options. Thus, remove all these
        // headers first!
        if (optionName == CoAPOptionName.PROXY_URI) {
            LOGGER.info("Proxy-uri in the message, overwrite Uri-host, Uri-port, Uri-path and Uri-query options");
            headers = headers.keySet().stream()
                    .filter(k -> k != CoAPOptionName.URI_HOST)
                    .filter(k -> k != CoAPOptionName.URI_PORT)
                    .filter(k -> k != CoAPOptionName.URI_PATH)
                    .filter(k -> k != CoAPOptionName.URI_QUERY)
                    .collect(Collectors.toMap(Function.identity(), headers::get));
        }
        return true;
    }

    /**
     * Remove an option header from this message
     *
     * @param option option to be removed
     * @return true, if an option header was successfully removed, false
     * otherwise
     */
    public boolean removeOptionHeader(CoAPOptionHeader option) {
        return get(option).remove(option);
    }

    private List<CoAPOptionHeader> get(CoAPOptionHeader option) {
        return get(option.getOptionName());
    }

    private List<CoAPOptionHeader> get(CoAPOptionName option) {
        return headers.computeIfAbsent(option, k -> Collections.synchronizedList(new ArrayList<>()));
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
     * Get the option headers in this message as List
     *
     * @return option headers in this message
     */
    public List<CoAPOptionHeader> getOptionHeaders() {
        List<CoAPOptionHeader> result = new ArrayList<>();
        headers.values().stream().forEach(result::addAll);
        return result;
    }

    /**
     * Returns a list of option headers with the given name. An empty list will
     * be returned if no option header(s) with the given name is found.
     *
     * @param optionName option to be found
     * @return list of options with the given option name.
     */
    public List<CoAPOptionHeader> getOptionHeaders(CoAPOptionName optionName) {
        return headers.getOrDefault(optionName, Collections.emptyList());
    }

    /**
     * Set the headers for this message
     *
     * @param headers list of option headers
     */
    public void setOptionHeaders(List<CoAPOptionHeader> headers) {
        this.headers.clear();
        headers.forEach(h -> get(h).add(h));
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
            this.timeout = rand.nextInt(max + 1 - min) + min;
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
            this.timeout *= 2;
        } else if (retransmissions == 4) {
            LOGGER.debug("Max nof retransmission reached (4), cancel this message");
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
     *
     * @param canceled
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

    public CoAPOptionHeader getUriHostOptionHeader() {
        return get(CoAPOptionName.URI_HOST).get(0);
    }

    /**
     * Get this message encoded as byte array
     *
     * @return encoded message
     */
    public byte[] encoded() {
        try {
            return new CoAPMessageWriter(this).encode();
        } catch (CoAPMessageFormat.IncorrectMessageException ex) {
            LOGGER.debug("Encoding failed.", ex);
        }
        return null;
    }

    /**
     * A helper method to check if this message relates to an observation
     * relationship. Returns true, if this message contains an "observe" option
     * header, false otherwise.
     *
     * @return true if this message contains "observe" option, false otherwise
     */
    public boolean isObserveMessage() {
        return headers.containsKey(CoAPOptionName.OBSERVE);
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
        List<CoAPOptionHeader> maxAgeOption = this.getOptionHeaders(CoAPOptionName.MAX_AGE);
        // If max-age is in the message, it should be there only once
        if (maxAgeOption.size() == 1) {
            byte[] bytes = maxAgeOption.get(0).getValue();

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
                maxAge = BitUtil.mergeBytesToInt(bytes[0], bytes[1],
                        bytes[2], bytes[3]);
            } else {
                maxAge = BitUtil.mergeBytesToInt(bytes[0], bytes[1],
                        bytes[2], bytes[3]);
            }
        } else if (maxAgeOption.size() > 1) {
            throw new CoAPException("Too many max-age options found");
        }

        // Make signed int to unsigned int -> becomes long
        return 0xffffffffL & maxAge;
    }

    /**
     * Helper method to print out the CoAP message
     *
     * @return String representation of the message with its remote socket
     * address, option headers, type, id, code and payload
     */
    public String logMessage() {
        StringBuilder logMessage = new StringBuilder("*****************************\n");
        try {
            StringBuilder type = new StringBuilder();
            FunctionalUtil.acceptIfCan(CoAPRequest.class, this, req -> {
                type.append("Request");
            }).orElse(CoAPResponse.class, res -> {
                type.append("Response");
            });
            logMessage.append("COAP [").append(type).append("]\n");

            if (getSocketAddress() != null) {
                logMessage.append("Remote socket address [").append(getSocketAddress()).append("]\n");
            }
            StringBuilder codeDescription = new StringBuilder();
            FunctionalUtil.acceptIfCan(CoAPRequest.class, this, req -> {
                try {
                    if (req.getUriFromRequest() != null) {
                        logMessage.append("Request URI [").append(req.getUriFromRequest()).append("]\n");
                    }
                    String name = getCode().toString();
                    if (name != null) {
                        codeDescription.append(name);
                    }
                } catch (CoAPException e) {
                    logMessage.append(e);
                }
            }).orElse(o -> {
                codeDescription.append(getCode().getDescription());
            });
            if (getMessageId() > 0) {
                logMessage.append("Message ID [").append(getMessageId()).append("]\n");
            }
            if (getMessageType() != null) {
                logMessage.append("Message type [").append(getMessageType()).append("]\n");
            }

            logMessage.append("Message code [").append(getCode()).append("]\n");
            if (codeDescription.length() > 0) {
                logMessage.append(type).append(" description [").append(codeDescription).append("]\n");
            }

            getOptionHeaders().forEach(item -> {
                CoAPOptionHeaderConverter converter = new CoAPOptionHeaderConverter();
                /*
                 String headerValue = "";
                 headerValue = converter.convertOptionHeaderToString(h);
                 logMessage += "Option [" + h.getOptionName() + "] value ["
                 + headerValue + "]\n";
                 */
            });
            // payload
            if (getPayload() != null) {
                String payloadStr = new String(payload, StandardCharsets.UTF_8);
                logMessage.append("Payload [").append(payloadStr).append("]\n");
            }
            logMessage.append("*****************************\n");
        } catch (Exception e) {
            logMessage.append(e);
        }
        return logMessage.toString();
    }

    /**
     * Enum representing the type of the message (as defined in coap core 07)
     */
    public static enum CoAPMessageType {

        CONFIRMABLE(1, "Confirmable"),
        NON_CONFIRMABLE(2, "Non-Confirmable"),
        ACKNOWLEDGEMENT(3, "Acknowledgement"),
        RESET(4, "Reset");

        private final String name;
        private final int no;

        private CoAPMessageType(int no, String name) {
            this.name = name;
            this.no = no;
        }

        public int getNo() {
            return no;
        }

        public String getPlainDescription() {
            return name;
        }

        public static CoAPMessageType getType(int no) {
            try {
                return CoAPMessageType.values()[no];
            } catch (Exception e) {
                return null;
            }
        }
    }
}
