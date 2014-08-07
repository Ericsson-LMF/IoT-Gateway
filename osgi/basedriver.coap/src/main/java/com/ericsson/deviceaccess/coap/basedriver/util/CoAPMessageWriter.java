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

import com.ericsson.common.util.BitUtil;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPMessage;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPMessageFormat;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionHeader;
import static com.ericsson.common.util.BitUtil.getBitsInIntAsByte;
import static com.ericsson.common.util.BitUtil.getBitsInIntAsInt;
import static com.ericsson.common.util.BitUtil.setBitsInByte;
import static com.ericsson.common.util.BitUtil.splitIntToBytes;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * This class is responsible for encoding a CoAPMessage (either request or
 * response) into a byte array
 */
public class CoAPMessageWriter implements CoAPMessageFormat {

    private final CoAPMessage message;
    private final ByteArrayOutputStream outputStream;

    /**
     * Constructor.
     *
     * @param message message to be encoded
     */
    public CoAPMessageWriter(CoAPMessage message) {
        this.message = message;
        this.outputStream = new ByteArrayOutputStream();
    }

    /**
     * This method actually does the encoding.
     *
     * @return encoded byte array
     * @throws
     * com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPMessageFormat.IncorrectMessageException
     */
    public byte[] encode() throws IncorrectMessageException {
        //CoAPActivator.logger.debug("CoAPMessageWriter: encode a message with message ID " + message.getIdentifier());
        // These are the header that all messages should have
        int version = message.getVersion();
        int messageType = message.getMessageType().getNo();
        byte[] token = message.getToken();
        int tokenLength = token == null ? 0 : token.length;
        if (tokenLength > 8) {
            //MUST NOT be send and MUST be processed as a message format error
            throw new IncorrectMessageException("Wrong token length: " + tokenLength); //TODO: Handle this
        }
        int messageCode = message.getCode();

        // First byte with version, message type & option count
        byte headerByte = setBitsInByte(0, VERSION_START,
                VERSION_LENGTH,
                getBitsInIntAsByte(version, 0, VERSION_LENGTH));
        headerByte = setBitsInByte(headerByte, TYPE_START,
                TYPE_LENGTH,
                getBitsInIntAsByte(messageType, 0, TYPE_LENGTH));
        headerByte = setBitsInByte(headerByte,
                TOKEN_LENGTH_START, TOKEN_LENGTH_LENGTH,
                getBitsInIntAsByte(tokenLength, 0, TOKEN_LENGTH_LENGTH));

        outputStream.write(headerByte);

        // byte tmpByteCode = ByteBuffer.allocate(1).get();
        byte codeByte = setBitsInByte(0, CODE_START,
                CODE_LENGTH,
                getBitsInIntAsByte(messageCode, 0, CODE_LENGTH));

        outputStream.write(codeByte);

        // Message ID is a 16-bit unsigned => two bytes
        byte[] messageIdBytes = splitIntToBytes(message.getMessageId());
        outputStream.write(messageIdBytes[2]);
        outputStream.write(messageIdBytes[3]);

        for (int n = 0; n < tokenLength; n++) {
            outputStream.write(token[n]);
        }

        this.encodeOptionHeaders();

        if (message.getPayload() != null && message.getPayload().length > 0) {
            try {
                outputStream.write(PAYLOAD_MARKER);
                outputStream.write(message.getPayload());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        byte[] byteArray = outputStream.toByteArray();

        try {
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return byteArray;
    }

    /**
     * This method encodes the option headers that are part of the message
     */
    private void encodeOptionHeaders() {

        // Sort options
        // TODO is there better to handle this??
        List<CoAPOptionHeader> options = message.getOptionHeaders();
        Collections.sort(options);

        // MultiMap options = message.getOptionHeaders();
        int previousOption = 0;

        // Go through different option numbers
        for (CoAPOptionHeader header : options) {
            int optionNumber = header.getOptionNumber();

            //Determine delta and if needed additional deltas
            int optionDelta = optionNumber - previousOption;
            int additionalDelta1 = -1;
            int additionalDelta2 = -1;
            if (optionDelta >= ADDITIONAL_DELTA) {
                if (optionDelta >= ADDITIONAL_DELTA_MAX) {
                    optionDelta -= ADDITIONAL_DELTA_MAX;
                    additionalDelta1 = getBitsInIntAsInt(optionDelta, 0, 8);
                    additionalDelta2 = getBitsInIntAsInt(optionDelta, 8, 8);
                    optionDelta = ADDITIONAL_DELTA_2;
                } else {
                    additionalDelta1 = optionDelta - ADDITIONAL_DELTA;
                    optionDelta = ADDITIONAL_DELTA;
                }
            }
            byte optionByte = setBitsInByte(0,
                    OPTION_DELTA_START, OPTION_DELTA_LENGTH, BitUtil
                    .getBitsInIntAsByte(optionDelta, 0,
                            OPTION_DELTA_LENGTH));
            previousOption = optionNumber;

            //Determine optionLength and if needed additional lengths
            int optionLength = header.getLength();
            int additionalLength1 = -1;
            int additionalLength2 = -1;
            if (optionLength >= ADDITIONAL_LENGTH) {
                if (optionLength >= ADDITIONAL_LENGTH_MAX) {
                    optionLength -= ADDITIONAL_LENGTH_MAX;
                    additionalLength1 = getBitsInIntAsInt(optionLength, 0, 8);
                    additionalLength2 = getBitsInIntAsInt(optionLength, 8, 8);
                    optionLength = ADDITIONAL_LENGTH_2;
                } else {
                    additionalLength1 = optionLength - ADDITIONAL_LENGTH;
                    optionLength = ADDITIONAL_LENGTH;
                }
            }
            optionByte = setBitsInByte(optionByte,
                    OPTION_LENGTH_START, OPTION_LENGTH_LENGTH,
                    getBitsInIntAsByte(optionLength, 0,
                            OPTION_LENGTH_LENGTH));
            outputStream.write(optionByte);

            if (additionalDelta1 != -1) {
                outputStream.write(additionalDelta1);
            }
            if (additionalDelta2 != -1) {
                outputStream.write(additionalDelta2);
            }
            if (additionalLength1 != -1) {
                outputStream.write(additionalLength1);
            }
            if (additionalLength2 != -1) {
                outputStream.write(additionalLength2);
            }

            if (header.getLength() > 0) {
                try {
                    outputStream.write(header.getValue());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
