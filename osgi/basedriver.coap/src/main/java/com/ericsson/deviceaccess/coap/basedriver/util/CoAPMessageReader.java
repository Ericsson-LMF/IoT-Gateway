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

import com.ericsson.deviceaccess.coap.basedriver.api.CoAPException;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPMessage;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPMessage.CoAPMessageType;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPMessageFormat;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionHeader;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPRequest;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;

/**
 * This class is responsible for decoding a byte array containing CoAP message
 * (either request or response)
 */
public class CoAPMessageReader implements CoAPMessageFormat {

    // this is the message being decoded
    private CoAPMessage message;
    private final DatagramPacket packet;
    private int bytePosition;
    private boolean okOptions;

    /**
     * Constructor. A datagram containing the data to be decoded is given as
     * parameter.
     *
     * @param packet
     */
    public CoAPMessageReader(DatagramPacket packet) {
        this.packet = packet;
        this.bytePosition = 0;
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

    /**
     * This method decodes the datagram
     *
     * @return
     */
    public CoAPMessage decode() {
        /*
         CoAPActivator.logger.debug("CoAPMessageReader: Decode message");
         */

        int dataLength = packet.getLength();

        // Version should be the first 2 bits (unsigned)
        byte[] bytes = packet.getData();

        byte versionByte = BitOperations.getBitsInByteAsByte(bytes[VERSION_BYTE], VERSION_START, VERSION_LENGTH);
        int version = (int) versionByte & 0xff;

        byte typeByte = BitOperations.getBitsInByteAsByte(bytes[TYPE_BYTE], TYPE_START, TYPE_LENGTH);
        int type = (int) typeByte & 0xff;

        // Get option count
        byte optionCountByte = BitOperations.getBitsInByteAsByte(bytes[OPTION_BYTE], OPTION_COUNT_START, OPTION_COUNT_LENGTH);
        bytePosition++;
        int optionCount = (int) optionCountByte & 0xff;

        byte codeByte = BitOperations.getBitsInByteAsByte(bytes[CODE_BYTE], CODE_START, CODE_LENGTH);
        bytePosition++;
        int code = (int) codeByte & 0xff;

        // Message ID is a 16-bit unsigned => merge two bytes
        byte firstByte = BitOperations.getBitsInByteAsByte(bytes[bytePosition], 0, 8);
        bytePosition++;
        byte secondByte = BitOperations.getBitsInByteAsByte(bytes[bytePosition], 0, 8);

        short shortInt = BitOperations.mergeBytesToShort(firstByte, secondByte);
        // mask to unsigned 16 bit -> int
        int messageId = shortInt & 0xFFFF;

        CoAPMessageType messageType = CoAPMessageType.getType(type);
        if (code == 0) {
            // handle empty messages as responses, only acks can be empty?
            this.message = new CoAPResponse(version, messageType, code, messageId);
        } // range 1-31 is a request
        else if (code > 0 && code < 32) {
            this.message = new CoAPRequest(version, messageType, code, messageId);
        } else if (code > 63 && code < 192) { // 64-191 response
            this.message = new CoAPResponse(version, messageType, code, messageId);
        } else {
            // TODO exception handling
            this.message = null;
            return this.message;
        }

        if (optionCount > 0) {
            this.decodeOptions(bytes, optionCount);
        }

        // Only payload left
        this.readPayload(bytes, dataLength);

        if (packet.getPort() != -1) {
            this.message.setSocketAddress((InetSocketAddress) packet.getSocketAddress());
        }

        if (this.message instanceof CoAPRequest) {
            try {
                ((CoAPRequest) message).createUriFromRequest(packet.getSocketAddress());
            } catch (CoAPException e) {
                e.printStackTrace();
            }
        }

        return message;
    }

    /**
     * This method decodes the options in the given data
     *
     * @param bytes bytes to decode
     * @param optionCount option count (determined from the option count header)
     */
    private void decodeOptions(byte[] bytes, int optionCount) {

        int optionStartByte = 4;
        bytePosition = optionStartByte; // Track the position
        int optionNumber = 0;

        /*
         * Options MUST appear in order of their Option Number!
         *
         * The fields in an option are defined as follows:
         *
         * Option Delta: 4-bit unsigned integer. Indicates the difference
         * between the Option Number of this option and the previous option (or
         * zero for the first option). In other words, the Option Number is
         * calculated by simply summing the Option Delta fields of this and
         * previous options before it. The Option Numbers 14, 28, 42, ... are
         * reserved for no-op options when they are sent with an empty value
         * (they are ignored) and can be used as "fenceposts" if deltas larger
         * than 15 would otherwise be required.
         *
         * Length: Indicates the length of the Option Value, in bytes. Normally
         * Length is a 4-bit unsigned integer allowing value lengths of 0-14
         * bytes. When the Length field is set to 15, another byte is added as
         * an 8-bit unsigned integer whose value is added to the 15, allowing
         * option value lengths of 15-270 bytes.
         *
         * The length and format of the Option Value depends on the respective
         * option, which MAY define variable length values. Options defined in
         * this document make use of the following formats for option values:
         */
        int previousOption = -1;

        for (int optionIndex = 0; optionIndex < optionCount; optionIndex++) {
            boolean fencePost = false;
            // Options start from byte 4

            byte optionDeltaByte = BitOperations.getBitsInByteAsByte(
                    bytes[bytePosition], OPTION_DELTA_START,
                    OPTION_DELTA_LENGTH);

            int optionDelta = (int) optionDeltaByte & 0xff;

            // If the same option number, delta is 0
            if (optionDelta == 0 && optionIndex > 0) {
                optionNumber = previousOption;
            } else {

                // sum the option delta of this and
                // previous options before it
                optionNumber += optionDelta;

                // If this option is fence post, it should not be added in the
                // headers
                if (optionNumber % 14 == 0) {
                    fencePost = true;
                }
            }

            // If option is fence post, ignore content (content should be empty)
            byte optionLengthByte = BitOperations.getBitsInByteAsByte(
                    bytes[bytePosition], OPTION_LENGTH_START,
                    OPTION_LENGTH_LENGTH);
            // first byte now handled, increase the index of byte being
            // progressed
            bytePosition++;

            // mask option byte
            int optionLength = (int) optionLengthByte & 0xff;
            // ByteBuffer buf;
            CoAPOptionHeader header;

            // If option length is set to 15, there is another byte=> read
            // that
            // to get length!
            if (optionLength == 15) {
                // read next byte for length
                optionLength = (int) BitOperations.getBitsInByteAsByte(bytes[bytePosition], 0, 8) & 0xff;
                bytePosition++;

                // FIXME in draft core 06 in the message format figure:
                // Length
                // - 15
                // (but I guess should be Length + 15 (acc to text)
                optionLength += 15;
            }
            // allocate a buffer with needed length

            ByteArrayOutputStream buf = new ByteArrayOutputStream();

            // Go through
            for (int i = 0; i < optionLength; i++) {
                buf.write(BitOperations.getBitsInByteAsByte(bytes[bytePosition], 0, 8));
                bytePosition++;
            }

            // Create new CoAPOptionHeader object and add it to the message
            // if the option is fence post, ignore value
            // if (!fencePost) {
            header = new CoAPOptionHeader(CoAPOptionName.getFromNo(optionNumber), buf.toByteArray());
            try {
                buf.flush();
                buf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            boolean okToAdd = message.addOptionHeader(header);
            // TODO draft-ietf-core-coap-08 specifies behaviour when
            // unrecognized options are received in confirmable req/resp.
            // So apply to CON messages only
            if (!okToAdd
                    && header.isCritical()
                    && message.getMessageType() == CoAPMessageType.CONFIRMABLE) {
                this.okOptions = false;
                //CoAPActivator.logger.debug("Unrecognized options in a confirmable message");
            }

            previousOption = optionNumber;
            fencePost = false;
        }
    }

    /**
     * Read the content of the message
     *
     * @param bytes byte array to decode
     */
    private void readPayload(byte[] bytes, int dataLength) {
        int bytesLeft = dataLength - bytePosition;

        ByteArrayOutputStream buf = new ByteArrayOutputStream();

        for (int i = 0; i < bytesLeft; i++) {
            buf.write(BitOperations.getBitsInByteAsByte(bytes[bytePosition], 0, 8));
            bytePosition++;
        }
        message.setPayload(buf.toByteArray());
        /*
         try {
         CoAPActivator.logger.debug("Received payload: "
         + new String(buf.toByteArray()));
         } catch (Exception e) {
         System.out.println("content cannot be shown as string");
         }
         */

        try {
            buf.flush();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
