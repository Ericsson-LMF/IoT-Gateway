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

import java.io.ByteArrayOutputStream;

import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionHeader;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName;
import com.ericsson.deviceaccess.coap.basedriver.util.BitOperations;

import junit.framework.TestCase;
import org.junit.Assert;

public class CoAPOptionHeaderTest extends TestCase {

    public CoAPOptionHeaderTest() {
        super("CoAPOptionHeaderTest");
    }

    public void testGetOptionName() {
        CoAPOptionHeader h = new CoAPOptionHeader(CoAPOptionName.CONTENT_TYPE);
        assertEquals(h.getOptionNumber(), CoAPOptionName.CONTENT_TYPE.getNo());

        h = new CoAPOptionHeader(CoAPOptionName.OBSERVE);
        assertEquals(h.getOptionNumber(), CoAPOptionName.OBSERVE.getNo());

        // one negative test
        h = new CoAPOptionHeader(CoAPOptionName.OBSERVE);
        assertFalse(h.getOptionNumber() == CoAPOptionName.CONTENT_TYPE.getNo());
    }

    public void testIsCritical() {
        // observe header is not critical (even option number)
        CoAPOptionHeader h = new CoAPOptionHeader(CoAPOptionName.OBSERVE);
        assertFalse(h.isCritical());
        // content-type header is critical (odd option number)
        h = new CoAPOptionHeader(CoAPOptionName.CONTENT_TYPE);
        assertTrue(h.isCritical());
    }

    public void testGetLength() {

        short id = 41; // try with application/xml

        byte[] contentTypeBytes = BitOperations.splitShortToBytes(id);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(contentTypeBytes[1]);

        CoAPOptionHeader h = new CoAPOptionHeader(CoAPOptionName.CONTENT_TYPE,
                outputStream.toByteArray());

        byte[] test = outputStream.toByteArray();

        int headerLength = h.getLength();
        assertEquals(test.length, headerLength);

        h = new CoAPOptionHeader(CoAPOptionName.CONTENT_TYPE.getNo(),
                CoAPOptionName.CONTENT_TYPE.getName(),
                outputStream.toByteArray());
        try {
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        headerLength = h.getLength();
        assertEquals(test.length, headerLength);

        headerLength++;

        assertFalse(test.length == headerLength);
    }

    public void testIsNormalLength() {
        short id = 41; // try with application/xml

        byte[] contentTypeBytes = BitOperations.splitShortToBytes(id);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(contentTypeBytes[1]);

        CoAPOptionHeader h = new CoAPOptionHeader(CoAPOptionName.CONTENT_TYPE,
                outputStream.toByteArray());

        byte[] test = outputStream.toByteArray();

        boolean isNormalLength = h.isNormalLength();
        assertTrue(isNormalLength);

        outputStream = new ByteArrayOutputStream();
        for (int i = 0; i < 17; i++) {
            outputStream.write(contentTypeBytes[1]);
        }
        h = new CoAPOptionHeader(CoAPOptionName.CONTENT_TYPE,
                outputStream.toByteArray());

        // Now the header is not normal length anymore
        assertFalse(h.isNormalLength());
    }

    public void testSetValue() {
        short id = 41; // try with application/xml

        byte[] contentTypeBytes = BitOperations.splitShortToBytes(id);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(contentTypeBytes[1]);

        CoAPOptionHeader h = new CoAPOptionHeader(CoAPOptionName.CONTENT_TYPE,
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

    public void testIsFencepost() {
        short id = 41;
        byte[] contentTypeBytes = BitOperations.splitShortToBytes(id);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(contentTypeBytes[1]);
        CoAPOptionHeader h = new CoAPOptionHeader(14, "test",
                outputStream.toByteArray());

        assertTrue(h.isFencepost());

        h = new CoAPOptionHeader(CoAPOptionName.ACCEPT);
        assertFalse(h.isFencepost());
    }

    public void testCompareTo() {
        CoAPOptionHeader h = new CoAPOptionHeader(CoAPOptionName.CONTENT_TYPE);
        CoAPOptionHeader h2 = new CoAPOptionHeader(CoAPOptionName.OBSERVE);

        assertEquals((h.getOptionNumber() - h2.getOptionNumber()), -9);
    }

    public void testOptionHeaderCodes() {
        // Test all the names and codes match!

        CoAPOptionName name = CoAPOptionName.CONTENT_TYPE;

        assertEquals(name.getName(), "Content-Type");
        assertEquals(CoAPOptionName.CONTENT_TYPE.getNo(), 1);

        assertEquals(CoAPOptionName.MAX_AGE.getName(), "Max-Age");
        assertEquals(CoAPOptionName.MAX_AGE.getNo(), 2);

        assertEquals(CoAPOptionName.PROXY_URI.getName(), "Proxy-Uri");
        assertEquals(CoAPOptionName.PROXY_URI.getNo(), 3);

        assertEquals(CoAPOptionName.ETAG.getName(), "ETag");
        assertEquals(CoAPOptionName.ETAG.getNo(), 4);

        assertEquals(CoAPOptionName.URI_HOST.getName(), "Uri-Host");
        assertEquals(CoAPOptionName.URI_HOST.getNo(), 5);

        assertEquals(CoAPOptionName.LOCATION_PATH.getName(), "Location-Path");
        assertEquals(CoAPOptionName.LOCATION_PATH.getNo(), 6);

        assertEquals(CoAPOptionName.URI_PORT.getName(), "Uri-Port");
        assertEquals(CoAPOptionName.URI_PORT.getNo(), 7);

        assertEquals(CoAPOptionName.LOCATION_QUERY.getName(), "Location-Query");
        assertEquals(CoAPOptionName.LOCATION_QUERY.getNo(), 8);

        assertEquals(CoAPOptionName.URI_PATH.getName(), "Uri-Path");
        assertEquals(CoAPOptionName.URI_PATH.getNo(), 9);

        assertEquals(CoAPOptionName.TOKEN.getName(), "Token");
        assertEquals(CoAPOptionName.TOKEN.getNo(), 11);

        assertEquals(CoAPOptionName.URI_QUERY.getName(), "Uri-Query");
        assertEquals(CoAPOptionName.URI_QUERY.getNo(), 15);

    }
}
