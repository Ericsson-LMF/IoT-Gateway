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

//import org.jmock.lib.legacy.ClassImposteriser;

import com.ericsson.deviceaccess.coap.basedriver.api.CoAPException;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionHeader;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName;
import com.ericsson.deviceaccess.coap.basedriver.osgi.BlockOptionHeader;
import com.ericsson.deviceaccess.coap.basedriver.osgi.BlockwiseTransferHandler;
import junit.framework.TestCase;

public class BlockOptionHeaderTest extends TestCase {

    private BlockwiseTransferHandler handler;

    public BlockOptionHeaderTest() {
        super("BlockOptionHeaderTest");

        /*Mockery context = new Mockery() {
         {
         setImposteriser(ClassImposteriser.INSTANCE);
         }
         };

         final LocalCoAPEndpoint ep = context.mock(LocalCoAPEndpoint.class);

         handler = new BlockwiseTransferHandler(ep);*/
    }

    public void test1ByteBlockOptionHeader() {
        BlockOptionHeader blockOption = new BlockOptionHeader(
                CoAPOptionName.BLOCK2, 0, false, 2);
        assertEquals(blockOption.getBlockNumber(), 0);
        assertEquals(blockOption.getSzx(), 2);

        assertFalse(blockOption.getMFlag());
        assertEquals(1, blockOption.getLength());
        blockOption = new BlockOptionHeader(CoAPOptionName.BLOCK1, 1, true, 2);
        assertEquals(blockOption.getBlockNumber(), 1);
        assertEquals(blockOption.getSzx(), 2);

        assertTrue(blockOption.getMFlag());
        assertEquals(1, blockOption.encode().length);

        CoAPOptionHeader h = new CoAPOptionHeader(CoAPOptionName.BLOCK1,
                blockOption.encode());
        BlockOptionHeader blockOpt = null;
        try {
            blockOpt = new BlockOptionHeader(h);
            assertEquals(1, blockOpt.getBlockNumber());
            assertTrue(blockOpt.getMFlag());
            assertEquals(blockOpt.getSzx(), 2);
        } catch (CoAPException e) {
            e.printStackTrace();
            assertTrue(false);
        }

        double largeDouble = 0;
        for (int i = 0; i < 4; i++) {
            largeDouble += Math.pow(2, i);
        }

        Double largeNum = largeDouble;
        int maxBlockNumber = largeNum.intValue();

        int szx = 4;

        blockOption = new BlockOptionHeader(CoAPOptionName.BLOCK1,
                maxBlockNumber, true, szx);

        assertEquals(blockOption.getBlockNumber(), maxBlockNumber);
        assertEquals(blockOption.getSzx(), szx);
        assertTrue(blockOption.getMFlag());
        assertEquals(1, blockOption.encode().length);

        h = new CoAPOptionHeader(CoAPOptionName.BLOCK1, blockOption.encode());

        try {
            blockOpt = new BlockOptionHeader(h);// handler.decodeBlockOption(h);
            assertEquals(maxBlockNumber, blockOpt.getBlockNumber());
            assertTrue(blockOpt.getMFlag());
            assertEquals(blockOpt.getSzx(), szx);
        } catch (CoAPException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void test2ByteBlockOptionHeader() {

        // Set the block number the maximum
        double largeDouble = 0;
        for (int i = 0; i < 12; i++) {
            largeDouble += Math.pow(2, i);
        }

        Double largeNum = largeDouble;
        int blockNumber = largeNum.intValue();
        boolean mFlag = true;

        BlockOptionHeader blockOption = new BlockOptionHeader(
                CoAPOptionName.BLOCK2, blockNumber, mFlag, 2);

        assertEquals(2, blockOption.getLength());
        byte[] headerBytes = blockOption.encode();

        largeDouble = 0;
        for (int i = 0; i < 8; i++) {
            largeDouble += Math.pow(2, i);
        }

        largeNum = largeDouble;
        int testBlock = largeNum.intValue();

        // Compare the first byte with the
        int headerByte = (int) headerBytes[0] & 0xFF;
        assertEquals(testBlock, headerByte);

        assertEquals(blockOption.getBlockNumber(), blockNumber);

        byte[] bytes = new byte[1];
        short test = BitOperations.mergeBytesToShort(bytes[0], headerBytes[1]);

        largeDouble = Math.pow(2, 1) + Math.pow(2, 3) + Math.pow(2, 4)
                + Math.pow(2, 5) + Math.pow(2, 6) + Math.pow(2, 7);
        largeNum = largeDouble;
        int headerValue = largeNum.intValue();

        assertEquals(headerValue, test);
        assertEquals(2, blockOption.encode().length);

        CoAPOptionHeader optionHeader = new CoAPOptionHeader(
                CoAPOptionName.BLOCK2, blockOption.encode());
        try {
            BlockOptionHeader decoded = new BlockOptionHeader(optionHeader);// handler.decodeBlockOption(optionHeader);

            assertEquals(blockNumber, decoded.getBlockNumber());

            assertEquals(mFlag, decoded.getMFlag());
        } catch (CoAPException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void test3ByteBlockOptionHeader() {
        int szx = 2;
        boolean mFlag = true;

        Double largeNum = Math.pow(2, 18);
        int testBlock = largeNum.intValue();

        // System.out.println("block number for 3 block header: " + testBlock);
        BlockOptionHeader blockOpt = new BlockOptionHeader(
                CoAPOptionName.BLOCK2, testBlock, mFlag, szx);

        assertEquals(3, blockOpt.getLength());
        byte[] headerBytes = blockOpt.encode();

        assertEquals(3, headerBytes.length);

        CoAPOptionHeader optionHeader = new CoAPOptionHeader(
                CoAPOptionName.BLOCK2, blockOpt.encode());
        BlockOptionHeader decoded;
        try {
            decoded = new BlockOptionHeader(optionHeader);
            assertEquals(testBlock, decoded.getBlockNumber());

            assertEquals(szx, decoded.getSzx());
            assertEquals(mFlag, decoded.getMFlag());
        } catch (CoAPException e) {
            e.printStackTrace();
            assertTrue(false);
        }

        szx = 2;
        mFlag = false;

        double largeDouble = 0;
        for (int i = 0; i < 20; i++) {
            largeDouble += Math.pow(2, i);
        }

        largeNum = largeDouble;
        testBlock = largeNum.intValue();

        int value = testBlock;
        int bitsNeeded = 0;
        while (value > 0) {
            bitsNeeded++;
            value = (value >> 1);
        }
        assertEquals(bitsNeeded, 20);

        blockOpt = new BlockOptionHeader(CoAPOptionName.BLOCK2, testBlock,
                mFlag, szx);

        assertEquals(testBlock, blockOpt.getBlockNumber());
        assertEquals(3, blockOpt.getLength());

        optionHeader = new CoAPOptionHeader(CoAPOptionName.BLOCK2,
                blockOpt.encode());
        try {
            decoded = new BlockOptionHeader(optionHeader);
            assertEquals(blockOpt.getBlockNumber(), decoded.getBlockNumber());

            assertEquals(szx, decoded.getSzx());
            assertEquals(mFlag, decoded.getMFlag());
        } catch (CoAPException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }
}
