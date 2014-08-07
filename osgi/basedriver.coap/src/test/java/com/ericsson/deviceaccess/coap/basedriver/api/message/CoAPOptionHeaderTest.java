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

import com.ericsson.deviceaccess.coap.basedriver.util.BitOperations;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import junit.framework.TestCase;

public class CoAPOptionHeaderTest extends TestCase {

    public CoAPOptionHeaderTest() {
        super("CoAPOptionHeaderTest");
    }

    public void testGetOptionName() {
        CoAPOptionHeader h = new CoAPOptionHeader(CoAPOptionName.CONTENT_FORMAT);
        assertEquals(h.getOptionNumber(), CoAPOptionName.CONTENT_FORMAT.getNo());

        h = new CoAPOptionHeader(CoAPOptionName.OBSERVE);
        assertEquals(h.getOptionNumber(), CoAPOptionName.OBSERVE.getNo());

        // one negative test
        h = new CoAPOptionHeader(CoAPOptionName.OBSERVE);
        assertFalse(h.getOptionNumber() == CoAPOptionName.CONTENT_FORMAT.getNo());
    }

    public void testIsCritical() {
        // observe header is not critical (even option number)
        assertFalse(CoAPOptionName.OBSERVE.isCritical());
        // uri-path header is critical (odd option number)
        assertTrue(CoAPOptionName.URI_PATH.isCritical());
    }

    public void testGetLength() {

        short id = 41; // try with application/xml

        byte[] contentTypeBytes = BitOperations.splitShortToBytes(id);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(contentTypeBytes[1]);

        CoAPOptionHeader h = new CoAPOptionHeader(CoAPOptionName.CONTENT_FORMAT,
                outputStream.toByteArray());

        byte[] test = outputStream.toByteArray();

        int headerLength = h.getLength();
        assertEquals(test.length, headerLength);

        h = new CoAPOptionHeader(CoAPOptionName.CONTENT_FORMAT.getName(), outputStream.toByteArray());
        try {
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        headerLength = h.getLength();
        assertEquals(test.length, headerLength);

        headerLength++;

        assertFalse(test.length == headerLength);
    }

//    public void testIsNormalLength() {
//        short id = 41; // try with application/xml
//
//        byte[] contentTypeBytes = BitOperations.splitShortToBytes(id);
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        outputStream.write(contentTypeBytes[1]);
//
//        CoAPOptionHeader h = new CoAPOptionHeader(CoAPOptionName.CONTENT_FORMAT,
//                outputStream.toByteArray());
//
//        byte[] test = outputStream.toByteArray();
//
//        boolean isNormalLength = h.isNormalLength();
//        assertTrue(isNormalLength);
//
//        outputStream = new ByteArrayOutputStream();
//        for (int i = 0; i < 17; i++) {
//            outputStream.write(contentTypeBytes[1]);
//        }
//        h = new CoAPOptionHeader(CoAPOptionName.CONTENT_FORMAT,
//                outputStream.toByteArray());
//
//        // Now the header is not normal length anymore
//        assertFalse(h.isNormalLength());
//    }
    public void testSetValue() {
        short id = 41; // try with application/xml

        byte[] contentTypeBytes = BitOperations.splitShortToBytes(id);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(contentTypeBytes[1]);

        CoAPOptionHeader h = new CoAPOptionHeader(CoAPOptionName.CONTENT_FORMAT,
                outputStream.toByteArray());
        byte[] test = outputStream.toByteArray();
        org.junit.Assert.assertArrayEquals(h.getValue(), test);

        id = 43;
        contentTypeBytes = BitOperations.splitShortToBytes(id);
        outputStream = new ByteArrayOutputStream();
        outputStream.write(contentTypeBytes[1]);

        test = outputStream.toByteArray();
        org.junit.Assert.assertNotSame(test, h.getValue());
    }

//    public void testIsFencepost() {
//        short id = 41;
//        byte[] contentTypeBytes = BitOperations.splitShortToBytes(id);
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        outputStream.write(contentTypeBytes[1]);
//        CoAPOptionHeader h = new CoAPOptionHeader("test", outputStream.toByteArray());
//
//        assertTrue(h.isFencepost());
//
//        h = new CoAPOptionHeader(CoAPOptionName.ACCEPT);
//        assertFalse(h.isFencepost());
//    }
//    public void testCompareTo() {
//        CoAPOptionHeader h = new CoAPOptionHeader(CoAPOptionName.CONTENT_FORMAT);
//        CoAPOptionHeader h2 = new CoAPOptionHeader(CoAPOptionName.OBSERVE);
//
//        assertEquals(-9, h.getOptionNumber() - h2.getOptionNumber());
//    }
//    public void testOptionHeaderCodes() {
//        // Test all the names and codes match!
//
//        CoAPOptionName name = CoAPOptionName.CONTENT_FORMAT;
//
//        assertEquals("Content-Format", name.getName());
//        assertEquals(1, CoAPOptionName.CONTENT_FORMAT.getNo());
//
//        assertEquals("Max-Age", CoAPOptionName.MAX_AGE.getName());
//        assertEquals(2, CoAPOptionName.MAX_AGE.getNo());
//
//        assertEquals("Proxy-Uri", CoAPOptionName.PROXY_URI.getName());
//        assertEquals(3, CoAPOptionName.PROXY_URI.getNo());
//
//        assertEquals("ETag", CoAPOptionName.ETAG.getName());
//        assertEquals(4, CoAPOptionName.ETAG.getNo());
//
//        assertEquals("Uri-Host", CoAPOptionName.URI_HOST.getName());
//        assertEquals(5, CoAPOptionName.URI_HOST.getNo());
//
//        assertEquals("Location-Path", CoAPOptionName.LOCATION_PATH.getName());
//        assertEquals(6, CoAPOptionName.LOCATION_PATH.getNo());
//
//        assertEquals("Uri-Port", CoAPOptionName.URI_PORT.getName());
//        assertEquals(7, CoAPOptionName.URI_PORT.getNo());
//
//        assertEquals("Location-Query", CoAPOptionName.LOCATION_QUERY.getName());
//        assertEquals(8, CoAPOptionName.LOCATION_QUERY.getNo());
//
//        assertEquals("Uri-Path", CoAPOptionName.URI_PATH.getName());
//        assertEquals(9, CoAPOptionName.URI_PATH.getNo());
//
//        assertEquals("Uri-Query", CoAPOptionName.URI_QUERY.getName());
//        assertEquals(15, CoAPOptionName.URI_QUERY.getNo());
//
//    }
}
