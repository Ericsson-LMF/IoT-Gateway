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

import static com.ericsson.common.util.BitUtil.getBitsInByteAsByte;
import static com.ericsson.common.util.BitUtil.mergeBytesToShort;
import com.ericsson.common.util.function.FunctionalUtil;
import com.ericsson.deviceaccess.coap.basedriver.api.CoAPException;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPMessage;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPMessage.CoAPMessageType;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPMessageFormat;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionHeader;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPRequest;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPRequestCode;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPResponse;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPResponseCode;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for decoding a byte array containing CoAP message
 * (either request or response)
 */
public class CoAPMessageReader implements CoAPMessageFormat {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoAPMessageReader.class);
    // this is the message being decoded
    private CoAPMessage message;
    private final DatagramPacket packet;
    private boolean okOptions;

    /**
     * Constructor. A datagram containing the data to be decoded is given as
     * parameter.
     *
     * @param packet
     */
    public CoAPMessageReader(DatagramPacket packet) {
        this.packet = packet;
        this.okOptions = true;
    }

    /**
     * This method returns a boolean value indicating if all the options were
     * successfully added
     *
     * @return true, if all options were added. false, otherwise
     */
    public boolean validOptions() {
        return okOptions;
    }

    public CoAPMessage decodeStart() throws IncorrectMessageException {
        decodeStartPos(packet.getData());
        return message;
    }

    /**
     * This method decodes the datagram
     *
     * @return
     * @throws
     * com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPMessageFormat.IncorrectMessageException
     */
    public CoAPMessage decode() throws IncorrectMessageException {
        LOGGER.debug("CoAPMessageReader: Decode message");
        byte[] bytes = packet.getData();

        int position = decodeStartPos(bytes);

        decodeOptions(bytes, position);

        // Only payload left
        readPayload(bytes, position);

        return message;
    }

    private int decodeStartPos(byte[] bytes) throws IncorrectMessageException {
        int position = 0;
        // Get version
        // Version should be the first 2 bits (unsigned)
        byte versionByte = getBitsInByteAsByte(bytes[position], VERSION_START, VERSION_LENGTH);
        int version = (int) versionByte & 0xff;
        if (version != 1) {
            //MUST be silently ignored //TODO: Handle this
            throw new IncorrectMessageException("Wrong version: " + version);
        }

        // Get type
        byte typeByte = getBitsInByteAsByte(bytes[position], TYPE_START, TYPE_LENGTH);
        int type = (int) typeByte & 0xff;

        // Get token length
        byte tokenLengthByte = getBitsInByteAsByte(bytes[position], TOKEN_LENGTH_START, TOKEN_LENGTH_LENGTH);
        position++;
        int tokenLength = (int) tokenLengthByte & 0xff;
        if (tokenLength > 8) {
            //MUST NOT be send and MUST be processed as a message format error //TODO: Handle this
            throw new IncorrectMessageException("Wrong token length: " + tokenLength);
        }

        // Get code
        byte codeByte = getBitsInByteAsByte(bytes[position], CODE_START, CODE_LENGTH);
        position++;
        int code = (int) codeByte & 0xff;

        // Get message id
        // Message ID is a 16-bit unsigned => merge two bytes
        byte firstByte = bytes[position];
        position++;
        byte secondByte = bytes[position];
        position++;
        short shortInt = mergeBytesToShort(firstByte, secondByte);
        // mask to unsigned 16 bit -> int
        int messageId = shortInt & 0xFFFF;

        //Get token
        byte[] token = new byte[tokenLength];
        System.arraycopy(bytes, position, token, 0, tokenLength);
        position += tokenLength;

        CoAPMessageType messageType = CoAPMessageType.getType(type);

        if (code == 0) {
            // handle empty messages as responses, only acks can be empty?
            message = new CoAPResponse(version, messageType, CoAPResponseCode.get(code), messageId, token);
        } else if (CoAPResponseCode.isAllowed(code)) {
            message = new CoAPResponse(version, messageType, CoAPResponseCode.get(code), messageId, token);
        } else if (CoAPRequestCode.isAllowed(code)) {
            message = new CoAPRequest(version, messageType, CoAPRequestCode.get(code), messageId, token);
        } else {
            // TODO exception handling
            message = null;
        }
        if (message != null && packet.getPort() != -1) {
            message.setSocketAddress((InetSocketAddress) packet.getSocketAddress());
        }
        FunctionalUtil.acceptIfCan(CoAPRequest.class, message, m -> {
            try {
                m.createUriFromRequest(packet.getSocketAddress());
            } catch (CoAPException e) {
                LOGGER.debug("Creating URI from request failed.", e);
            }
        });
        return position;
    }

    /**
     * This method decodes the options in the given data
     *
     * @param bytes bytes to decode
     * @param optionCount option count (determined from the option count header)
     */
    private int decodeOptions(byte[] bytes, int position) throws IncorrectMessageException {
        byte cur = bytes[position];
        position++;
        int delta = getBitsInByteAsByte(cur, OPTION_DELTA_START, OPTION_DELTA_LENGTH);
        int length = getBitsInByteAsByte(cur, OPTION_LENGTH_START, OPTION_LENGTH_LENGTH);
        int optionNumber = 0;
        // An Option can be followed by the end of the message, by another Option, or by the Payload Marker and the payload.
        while (delta != PAYLOAD_MARKER) {
            //Determine option number
            if (delta == ADDITIONAL_DELTA) {
                delta += bytes[position];
                position++;
            } else if (delta == ADDITIONAL_DELTA_2) {
                cur = bytes[position];
                position++;
                delta = ADDITIONAL_DELTA_MAX + ((cur << 8) + bytes[position]);
                position++;
            }
            optionNumber += delta;
            CoAPOptionName name = CoAPOptionName.getFromNo(optionNumber);

            //Determine length
            if (length == ADDITIONAL_LENGTH) {
                length += bytes[position];
                position++;
            } else if (length == ADDITIONAL_LENGTH_2) {
                cur = bytes[position];
                position++;
                length = ADDITIONAL_LENGTH_MAX + ((cur << 8) + bytes[position]);
                position++;
            }
            if (!name.isLegalSize(length)) {
                //If the length of an option value in a request is outside the defined range, that option MUST be treated like an unrecognized option
                okOptions = false; //TODO: Handle this
            }

            byte[] value = new byte[length];
            System.arraycopy(bytes, position, value, 0, length);
            position += length;
            boolean okToAdd = message.addOptionHeader(new CoAPOptionHeader(name, value));
            // TODO draft-ietf-core-coap-08 specifies behaviour when
            // unrecognized options are received in confirmable req/resp.
            // So apply to CON messages only
            if (!okToAdd && name.isCritical() && message.getMessageType() == CoAPMessageType.CONFIRMABLE) {
                this.okOptions = false; //TODO: Handle this
                LOGGER.debug("Unrecognized options in a confirmable message");
            }

            if (position >= bytes.length) {
                return position;
            }
            cur = bytes[position];
            position++;
            delta = getBitsInByteAsByte(cur, OPTION_DELTA_START, OPTION_DELTA_LENGTH);
            length = getBitsInByteAsByte(cur, OPTION_LENGTH_START, OPTION_LENGTH_LENGTH);
        }
        if (length != 0) {
            // If the field is set to this value but the entire byte is not the payload marker, this MUST be processed as a message format error.
            throw new IncorrectMessageException("Payload marker was bad: " + length); //TODO: Handle this
        }
        if (position == bytes.length - 1) {
            // The presence of a marker followed by a zero-length payload MUST be processed as a message format error.
            throw new IncorrectMessageException("Empty payload after payload marker"); //TODO: Handle this
        }
        return position;
    }

    /**
     * Read the content of the message
     *
     * @param bytes byte array to decode
     */
    private void readPayload(byte[] bytes, int position) {
        int bytesLeft = bytes.length - position;
        byte[] payload = new byte[bytesLeft];
        System.arraycopy(bytes, position, payload, 0, bytesLeft);
        message.setPayload(payload);
    }
}
