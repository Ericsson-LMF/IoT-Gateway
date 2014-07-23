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
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPMessage.CoAPMessageType;
import com.ericsson.deviceaccess.coap.basedriver.util.BitOperations;
import com.ericsson.deviceaccess.coap.basedriver.util.CoAPOptionHeaderConverter;
import java.util.Iterator;
import java.util.List;
import junit.framework.TestCase;

public class CoAPMessageTest extends TestCase {

    private CoAPMessage resp;
    private CoAPRequest req;
    private CoAPOptionHeaderConverter converter;

    public CoAPMessageTest() {
        super("CoAPMessageTest");
        resp = new CoAPResponse(1, CoAPMessageType.CONFIRMABLE, 1, 123);
        req = new CoAPRequest(1, CoAPMessageType.CONFIRMABLE, 1, 123);
        converter = new CoAPOptionHeaderConverter();
    }

    public void testAddTokenHeader() {
        assertNull(resp.getTokenHeader());
        // Token header
        CoAPOptionHeader tokenHeader = new CoAPOptionHeader(
                CoAPOptionName.TOKEN);
        // should be ok to add one token header
        assertTrue(resp.addOptionHeader(tokenHeader));
        // but not a second one
        assertFalse(resp.addOptionHeader(tokenHeader));

        assertEquals(1, resp.getOptionCount());
    }

    public void testAddUriHeaders() {
        CoAPOptionHeader h = new CoAPOptionHeader(CoAPOptionName.URI_HOST);
        // should be ok to add one token header
        assertTrue(resp.addOptionHeader(h));
        // but not a second one
        assertFalse(resp.addOptionHeader(h));

        h = new CoAPOptionHeader(CoAPOptionName.URI_PORT);

        // should be ok to add one uri-port header
        assertTrue(resp.addOptionHeader(h));
        // but not a second one
        assertFalse(resp.addOptionHeader(h));

        h = new CoAPOptionHeader(CoAPOptionName.URI_PATH);

        // should be ok to add one uri-port header
        assertTrue(resp.addOptionHeader(h));
        // and a second one
        assertTrue(resp.addOptionHeader(h));

        h = new CoAPOptionHeader(CoAPOptionName.URI_QUERY);

        // should be ok to add one uri-port header
        assertTrue(resp.addOptionHeader(h));
        // and a second one
        assertTrue(resp.addOptionHeader(h));
    }

    /*
     * Adding proxy-uri header means that other uri related headers need to be
     * removed
     */
    public void testAddProxyUriHeader() {
        // If proxy-uri header is added, it should remove other uri related
        // headers
        CoAPOptionHeader h = new CoAPOptionHeader(CoAPOptionName.PROXY_URI);
        assertTrue(resp.addOptionHeader(h));

        assertEquals(0, resp.getOptionHeaders(CoAPOptionName.URI_HOST).size());
        assertEquals(0, resp.getOptionHeaders(CoAPOptionName.URI_PORT).size());
        assertEquals(0, resp.getOptionHeaders(CoAPOptionName.URI_PATH).size());
        assertEquals(0, resp.getOptionHeaders(CoAPOptionName.URI_QUERY).size());

        assertTrue(resp.addOptionHeader(h));
        assertTrue(resp.addOptionHeader(h));

        assertEquals(resp.getOptionHeaders(CoAPOptionName.PROXY_URI).size(), 3);
    }

    public void testAddContentTypeHeader() {
        // content-type
        CoAPOptionHeader h = new CoAPOptionHeader(CoAPOptionName.CONTENT_TYPE);
        resp.addOptionHeader(h);
        List optionHeaders = resp.getOptionHeaders();
        Iterator it = optionHeaders.iterator();

        while (it.hasNext()) {
            CoAPOptionHeader header = (CoAPOptionHeader) it.next();
            assertEquals(CoAPOptionName.CONTENT_TYPE, header.getOptionName());
        }

        // Token header
        // should not be ok to add the content-type header for the 2nd time
        assertFalse(resp.addOptionHeader(h));
        // remove and try to add again
        resp.removeOptionHeader(h);
        assertTrue(resp.addOptionHeader(h));
    }

    public void testAddMaxAgeHeader() {
        // max-age only once
        CoAPOptionHeader h = new CoAPOptionHeader(CoAPOptionName.MAX_AGE);
        // should be ok to add one uri-port header
        assertTrue(resp.addOptionHeader(h));
        // and a second one
        assertFalse(resp.addOptionHeader(h));
    }

    public void testAddEtagHeader() {

        // In a response, etag can be present only once
        CoAPOptionHeader h = new CoAPOptionHeader(CoAPOptionName.ETAG);
        // should be ok to add one uri-port header
        assertTrue(resp.addOptionHeader(h));
        // and a second one
        assertFalse(resp.addOptionHeader(h));

        // In a request, etag can be present several times
        // should be ok to add one uri-port header
        assertTrue(req.addOptionHeader(h));
        // and a second one
        assertTrue(req.addOptionHeader(h));
    }

    public void testAddIfNoneMatchHeader() {
        // In a response, etag can be present only once
        CoAPOptionHeader h = new CoAPOptionHeader(CoAPOptionName.IF_NONE_MATCH);
        // should be ok to add one uri-port header
        assertTrue(resp.addOptionHeader(h));
        // and a second one
        assertFalse(resp.addOptionHeader(h));
        assertNull(h.getValue());
        assertEquals(0, h.getLength());
    }

    public void testRetransmissions() {
        assertEquals(resp.getRetransmissions(), 0);
    }

    public void testMessageCanceled() {
        assertFalse(resp.messageCanceled());
    }

    public void testGetIdentifier() {
        assertNull(resp.getIdentifier());
    }

    public void testIsObserveMessage() {
        assertFalse(resp.isObserveMessage());

        // A non-negative integer which is represented in network byte order
        short observe = 0;
        byte[] observeBytes = BitOperations.splitShortToBytes(observe);

        CoAPOptionHeader observeOpt = new CoAPOptionHeader(
                CoAPOptionName.OBSERVE, observeBytes);
        resp.addOptionHeader(observeOpt);
        assertTrue(resp.isObserveMessage());

        resp.removeOptionHeader(observeOpt);

        // Test maximum value
        double max = Math.pow(2, 16) - 1;
        Double maxDouble = max;
        int unsignedShortMax = maxDouble.intValue();

        byte[] shortBytes = BitOperations.splitIntToBytes(unsignedShortMax);

        byte[] shortValue = new byte[2];
        shortValue[0] = shortBytes[2];
        shortValue[1] = shortBytes[3];
        observeOpt = new CoAPOptionHeader(CoAPOptionName.OBSERVE, shortValue);

        int observeValue = converter.shortToUnsignedInt(observeOpt);
        assertEquals(observeValue, unsignedShortMax);
    }

    public void testGetMaxAge() {
        try {
            long maxAge = resp.getMaxAge();
            assertEquals(60, maxAge);
        } catch (CoAPException e) {
            e.printStackTrace();

        }
        int maxAgeValue = 20;

        byte[] bytes = BitOperations.splitIntToBytes(maxAgeValue);

        CoAPOptionHeader maxAgeOpt = new CoAPOptionHeader(
                CoAPOptionName.MAX_AGE, bytes);
        resp.addOptionHeader(maxAgeOpt);
        try {
            long maxAge = resp.getMaxAge();
            assertEquals(20, maxAge);
        } catch (CoAPException e) {
            e.printStackTrace();
        }

        long unsignedLong = 0xffffffffL & Integer.MAX_VALUE;
        byte[] longBytes = BitOperations.splitLongToBytes(unsignedLong);

        byte[] intValue = new byte[4];
        intValue[0] = longBytes[4];
        intValue[1] = longBytes[5];
        intValue[2] = longBytes[6];
        intValue[3] = longBytes[7];

        resp.removeOptionHeader(maxAgeOpt);
        maxAgeOpt = new CoAPOptionHeader(CoAPOptionName.MAX_AGE, intValue);
        resp.addOptionHeader(maxAgeOpt);

        try {
            long maxAge = resp.getMaxAge();
            assertEquals(Integer.MAX_VALUE, maxAge);
        } catch (CoAPException e) {
            e.printStackTrace();
        }

        resp.removeOptionHeader(maxAgeOpt);
        // unsigned max value
        double max = Math.pow(2, 32) - 1;

        Double maxDouble = max;
        long unsignedIntMax = maxDouble.longValue();
        longBytes = BitOperations.splitLongToBytes(unsignedIntMax);

        intValue = new byte[4];
        intValue[0] = longBytes[4];
        intValue[1] = longBytes[5];
        intValue[2] = longBytes[6];
        intValue[3] = longBytes[7];

        maxAgeOpt = new CoAPOptionHeader(CoAPOptionName.MAX_AGE, intValue);
        resp.addOptionHeader(maxAgeOpt);

        try {
            assertEquals(unsignedIntMax, resp.getMaxAge());
        } catch (CoAPException e) {
            e.printStackTrace();
        }
    }

    /*
     * Test for max-ofe header (draft-ietf-core-observe-03). can be present only
     * in responses
     */
    public void testAddMaxOfeHeader() {
        int value = 20;
        byte[] bytes = BitOperations.splitIntToBytes(value);
        CoAPOptionHeader h = new CoAPOptionHeader(CoAPOptionName.MAX_OFE, bytes);
        // max-ofe cannot be added in request
        assertFalse(req.addOptionHeader(h));
        // can be added in response
        assertTrue(resp.addOptionHeader(h));

        resp.removeOptionHeader(h);

        double max = Math.pow(2, 32) - 1;

        Double maxDouble = max;
        long unsignedIntMax = maxDouble.longValue();
        byte[] longBytes = BitOperations.splitLongToBytes(unsignedIntMax);

        byte[] intValue = new byte[4];
        intValue[0] = longBytes[4];
        intValue[1] = longBytes[5];
        intValue[2] = longBytes[6];
        intValue[3] = longBytes[7];

        h = new CoAPOptionHeader(CoAPOptionName.MAX_OFE, intValue);
        resp.addOptionHeader(h);

        CoAPOptionHeader retrieved = resp.getOptionHeaders(CoAPOptionName.MAX_OFE).get(0);
        long maxOfeFromHeader = converter.convertIntToUnsignedLong(retrieved);
        assertEquals(maxOfeFromHeader, unsignedIntMax);

        // test with some normal value, 4
        resp.removeOptionHeader(h);

        int oneByteInt = 4;
        longBytes = BitOperations.splitLongToBytes(oneByteInt);

        intValue = new byte[4];
        intValue[0] = longBytes[4];
        intValue[1] = longBytes[5];
        intValue[2] = longBytes[6];
        intValue[3] = longBytes[7];

        h = new CoAPOptionHeader(CoAPOptionName.MAX_OFE, intValue);
        resp.addOptionHeader(h);

        retrieved = resp.getOptionHeaders(CoAPOptionName.MAX_OFE).get(0);
        maxOfeFromHeader = converter.convertIntToUnsignedLong(retrieved);
        assertEquals(maxOfeFromHeader, oneByteInt);

        resp.removeOptionHeader(h);

        // test with two byte value
        double twoByteValue = Math.pow(2, 9) - 1;
        maxDouble = twoByteValue;
        long twoByteLong = maxDouble.longValue();
        longBytes = BitOperations.splitLongToBytes(twoByteLong);

        intValue = new byte[4];
        intValue[0] = longBytes[4];
        intValue[1] = longBytes[5];
        intValue[2] = longBytes[6];
        intValue[3] = longBytes[7];

        h = new CoAPOptionHeader(CoAPOptionName.MAX_OFE, intValue);
        resp.addOptionHeader(h);

        retrieved = resp.getOptionHeaders(CoAPOptionName.MAX_OFE).get(0);
        maxOfeFromHeader = converter.convertIntToUnsignedLong(retrieved);
        assertEquals(maxOfeFromHeader, maxDouble.intValue());
    }


    /*
     * Test for observe option header (draft-ietf-core-observe-03). can be
     * present only once in req/resp
     */
    public void testAddObserveHeader() {
        int value = 20;
        byte[] bytes = BitOperations.splitIntToBytes(value);
        CoAPOptionHeader h = new CoAPOptionHeader(CoAPOptionName.OBSERVE, bytes);

        assertTrue(req.addOptionHeader(h));
        assertTrue(resp.addOptionHeader(h));
    }
}
