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

import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPMessage.CoAPMessageType;
import junit.framework.TestCase;

public class CoAPResponseTest extends TestCase {

    private CoAPResponse resp;

    public CoAPResponseTest() {
        super("CoAPResponseTest");
        resp = new CoAPResponse(1, CoAPMessageType.CONFIRMABLE, CoAPResponseCode.CREATED, 1234);
    }

    /*
     * This method tests creating an empty acknowledgement for a response
     */
    public void testCreateAcknowledgement() {
        CoAPResponse ack = resp.createAcknowledgement();
        assertEquals(ack.getMessageId(), resp.getMessageId());
        // Token header should match with the original response
        assertEquals(ack.getToken(), resp.getToken());
        // Message type should be acknowledgement
        assertEquals(ack.getMessageType(), CoAPMessageType.ACKNOWLEDGEMENT);
        assertEquals(ack.getSocketAddress(), resp.getSocketAddress());
    }

    /*
     * Reset message should contain the same token header as the received
     * response, and it should have the same message id
     */
    public void testCreateReset() {
        CoAPResponse reset = resp.createReset();

        assertEquals(reset.getToken(), resp.getToken());
        assertEquals(reset.getSocketAddress(), resp.getSocketAddress());
        assertEquals(reset.getMessageId(), resp.getMessageId());
        assertEquals(reset.getMessageType(), CoAPMessageType.RESET);
    }

    /*
     * Test the different CoAP response codes
     */
    public void testIsCachable() {
        CoAPResponse reset = resp.createReset();
        assertFalse(reset.isCacheable());
        resp = new CoAPResponse(1, CoAPMessageType.CONFIRMABLE,
                CoAPResponseCode.VALID, 1234);

        assertTrue(resp.isCacheable());
        resp = new CoAPResponse(1, CoAPMessageType.CONFIRMABLE,
                CoAPResponseCode.CONTENT, 1234);
        assertTrue(resp.isCacheable());

        resp = new CoAPResponse(1, CoAPMessageType.CONFIRMABLE,
                CoAPResponseCode.NOT_FOUND, 1234);
        assertTrue(resp.isCacheable());

        resp = new CoAPResponse(1, CoAPMessageType.CONFIRMABLE,
                CoAPResponseCode.INTERNAL_SERVER_ERROR, 1234);
        assertTrue(resp.isCacheable());
    }
}
