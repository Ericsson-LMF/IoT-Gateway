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
package com.ericsson.deviceaccess.coap.basedriver.osgi;

import com.ericsson.deviceaccess.coap.basedriver.api.CoAPException;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionHeader;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName;
import com.ericsson.deviceaccess.coap.basedriver.util.BitOperations;
import java.io.ByteArrayOutputStream;

/**
 * This class extends the CoAPOptionHeader. It is used for describing the Block
 * option headers.
 */
public class BlockOptionHeader extends CoAPOptionHeader {

    private int blockNumber;

    private int szx;

    private boolean mFlag;

    private int length;

    // TODO there's no error handling for valid input here..
    /**
     * Constructor.
     *
     * @param name name of the header
     * @param blockNumber number of the block. Maximum 20 bits can be used for
     * representing the block number
     * @param mFlag m flag indicating if there are more blocks. when set to
     * true, indicates there are more blocks.
     * @param szx size exponent (not the size of the block!!). The szx should be
     * between values 0 and 6.
     *
     */
    public BlockOptionHeader(CoAPOptionName name, int blockNumber,
            boolean mFlag, int szx) {
        super(name);
        this.mFlag = mFlag;
        this.blockNumber = blockNumber;

        this.szx = szx;

        int value = blockNumber;
        int bitsNeeded = 0;
        while (value > 0) {
            bitsNeeded++;
            value = (value >> 1);
        }

        if (bitsNeeded <= 4) {
            length = 1;
        } else if (4 < bitsNeeded && bitsNeeded <= 12) {
            length = 2;
        } else {
            length = 3;
        }

        super.setValue(encode());
    }

    /**
     * Generate a BlockOptionHeader from the CoAPOptionHeader of type Block1 or
     * Block2
     *
     * @param header
     * @throws CoAPException if a header that is not either Block1 or Block2 is
     * given as input
     */
    public BlockOptionHeader(CoAPOptionHeader header) throws CoAPException {
        super(CoAPOptionName.getFromNo(header.getOptionNumber()));
        decode(header);
    }

    private void decode(CoAPOptionHeader blockOptionHeader)
            throws CoAPException {
        CoAPOptionName optionName = CoAPOptionName.getFromNo(blockOptionHeader.getOptionNumber());
        if (optionName != CoAPOptionName.BLOCK1 && optionName != CoAPOptionName.BLOCK2) {
            throw new CoAPException("Only option header Block1 and Block2 can be decoded into BlockOptionHeaders");
        }
        byte[] blockValue = blockOptionHeader.getValue();

        // shift three to the right to get the M bit
        byte lastBlock = blockValue[blockValue.length - 1];
        int decodedMFlag = lastBlock >> 3 & 0x1;

        byte szxByte = BitOperations.getBitsInByteAsByte(blockValue[blockValue.length - 1], 0, 3);

        int decodedBlockNumber = -1;
        byte blockNumberByte;

        // Length of the block is 1-3 bytes, get the block number
        if (blockValue.length == 1) {
            blockNumberByte = BitOperations.getBitsInByteAsByte(blockValue[0], 4, 4);
            decodedBlockNumber = blockNumberByte & 0xFF;
        } else if (blockValue.length == 2) {
            byte[] testArray = new byte[2];
            byte newByte = BitOperations.setBitsInByte(testArray[0], 0, 4,
                    BitOperations.getBitsInIntAsInt(blockValue[0], 4, 4));
            byte secondByte = BitOperations.setBitsInByte(testArray[1], 4, 4,
                    BitOperations.getBitsInIntAsInt(blockValue[0], 0, 4));

            secondByte = BitOperations.setBitsInByte(secondByte, 0, 4,
                    BitOperations.getBitsInIntAsInt(blockValue[1], 4, 4));

            short blockNumberShort = BitOperations.mergeBytesToShort(newByte,
                    secondByte);

            decodedBlockNumber = blockNumberShort;

        } else if (blockValue.length == 3) {

            byte[] testArray = new byte[4];
            byte newByte = BitOperations.setBitsInByte(testArray[0], 0, 4,
                    BitOperations.getBitsInIntAsInt(blockValue[0], 4, 4));
            byte secondByte = BitOperations.setBitsInByte(testArray[1], 4, 4,
                    BitOperations.getBitsInIntAsInt(blockValue[0], 0, 4));

            secondByte = BitOperations.setBitsInByte(secondByte, 0, 4,
                    BitOperations.getBitsInIntAsInt(blockValue[1], 4, 4));

            byte thirdByte = BitOperations.setBitsInByte(testArray[2], 0, 4,
                    BitOperations.getBitsInByteAsByte(blockValue[2], 4, 4));
            thirdByte = BitOperations.setBitsInByte(thirdByte, 4, 4,
                    BitOperations.getBitsInByteAsByte(blockValue[1], 0, 4));

            byte[] bytes = new byte[1];
            byte tmpByte = bytes[0];
            decodedBlockNumber = BitOperations.mergeBytesToInt(tmpByte,
                    newByte, secondByte, thirdByte);
        }

        this.blockNumber = decodedBlockNumber;
        this.mFlag = (decodedMFlag != 0);
        this.szx = szxByte;

        int value = blockNumber;
        int bitsNeeded = 0;
        while (value > 0) {
            bitsNeeded++;
            value = (value >> 1);
        }

        if (bitsNeeded <= 4) {
            this.length = 1;
        } else if (4 < bitsNeeded && bitsNeeded <= 12) {
            this.length = 2;
        } else {
            this.length = 3;
        }

        // super.setValue(this.encode());

        /*
         * BlockOptionHeader option = new BlockOptionHeader(
         * CoAPOptionName.getOptionName(optionNumber), decodedBlockNumber,
         * (mFlag != 0), szx);
         *
         * return option;
         */
    }

    /**
     * Get the number of the block
     *
     * @return number of the block
     */
    public int getBlockNumber() {
        return this.blockNumber;
    }

    /**
     * Get the size exponent "szx" of the option header
     *
     * @return "szx" of the option header
     */
    public int getSzx() {
        return this.szx;
    }

    /**
     * Get the m flag of the block option header
     *
     * @return m flag of the header
     */
    public boolean getMFlag() {
        return this.mFlag;
    }

    /**
     * Get the length of the block option header
     *
     * @return length of the block option header
     */
    @Override
    public int getLength() {
        return this.length;
    }

    /**
     * Get the block option header as byte array
     *
     * @return block option header as byte array
     */
    public byte[] encode() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte firstByte;
        if (length == 1) {
            byte[] bytes = new byte[1];
            byte tmpByte = bytes[0];
            firstByte = BitOperations.setBitsInByte(tmpByte, 4, 4,
                    BitOperations.getBitsInIntAsInt(blockNumber, 0, 4));

            int flagVal = mFlag ? 1 : 0;
            firstByte = BitOperations.setBitInByte(firstByte, 3, flagVal);

            firstByte = BitOperations.setBitsInByte(firstByte, 0, 3,
                    BitOperations.getBitsInIntAsByte(this.szx, 0, 3));
            stream.write(firstByte);
        } else if (length == 2) {

            byte[] split = BitOperations.splitIntToBytes(blockNumber);
            int blockNumberShifted = (blockNumber << 4);
            split = BitOperations.splitIntToBytes(blockNumberShifted);
            stream.write(split[2]);

            byte[] testArray = new byte[1];
            byte newByte = BitOperations.setBitsInByte(testArray[0], 4, 4,
                    BitOperations.getBitsInIntAsInt(split[3], 4, 4));

            int flagVal = mFlag ? 1 : 0;
            newByte = BitOperations.setBitInByte(newByte, 3, flagVal);
            newByte = BitOperations.setBitsInByte(newByte, 0, 3,
                    BitOperations.getBitsInIntAsByte(this.szx, 0, 3));

            stream.write(newByte);
        } else if (length == 3) {

            byte[] split = BitOperations.splitIntToBytes(blockNumber);

            int blockNumberShifted = blockNumber << 4;
            split = BitOperations.splitIntToBytes(blockNumberShifted);
            stream.write(split[1]);
            stream.write(split[2]);

            byte thirdByte = (byte) (BitOperations.getBitsInByteAsByte(split[3], 4, 4) << 4);

            int flagVal = mFlag ? 1 : 0;
            thirdByte = BitOperations.setBitInByte(thirdByte, 3, flagVal);
            thirdByte = BitOperations.setBitsInByte(thirdByte, 0, 3,
                    BitOperations.getBitsInIntAsByte(this.szx, 0, 3));

            stream.write(thirdByte);
        }
        return stream.toByteArray();
    }
}
