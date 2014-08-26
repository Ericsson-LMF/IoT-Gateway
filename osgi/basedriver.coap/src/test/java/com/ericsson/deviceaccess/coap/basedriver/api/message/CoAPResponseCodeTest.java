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

import java.net.URISyntaxException;
import org.junit.Test;
import static org.junit.Assert.*;

public class CoAPResponseCodeTest {
    @Test
    public void testResponseCodes() throws URISyntaxException {

        System.out.println("code to string:" + CoAPResponseCode.CREATED.toString());

        CoAPResponseCode code = CoAPResponseCode.get(65);

        System.out.println("code to string:" + code.toString());

        code = CoAPResponseCode.get(65);

        String responseText = code.toString();
        System.out.println("response text: " + responseText);

        assertEquals(CoAPResponseCode.CREATED.getNo(), 65);
        assertEquals(CoAPResponseCode.CREATED.getDescription(), "2.01 Created");

        assertEquals(CoAPResponseCode.DELETED.getNo(), 66);
        assertEquals(CoAPResponseCode.DELETED.getDescription(), "2.02 Deleted");

        assertEquals(CoAPResponseCode.VALID.getNo(), 67);
        assertEquals(CoAPResponseCode.VALID.getDescription(), "2.03 Valid");

        assertEquals(CoAPResponseCode.CHANGED.getNo(), 68);
        assertEquals(CoAPResponseCode.CHANGED.getDescription(), "2.04 Changed");

        assertEquals(CoAPResponseCode.CONTENT.getNo(), 69);
        assertEquals(CoAPResponseCode.CONTENT.getDescription(), "2.05 Content");

        assertEquals(CoAPResponseCode.BAD_REQUEST.getNo(), 128);
        assertEquals(CoAPResponseCode.BAD_REQUEST.getDescription(),
                "4.00 Bad Request");

        assertEquals(CoAPResponseCode.UNAUTHORIZED.getNo(), 129);
        assertEquals(CoAPResponseCode.UNAUTHORIZED.getDescription(),
                "4.01 Unauthorized");

        assertEquals(CoAPResponseCode.BAD_OPTION.getNo(), 130);
        assertEquals(CoAPResponseCode.BAD_OPTION.getDescription(),
                "4.02 Bad Option");

        assertEquals(CoAPResponseCode.FORBIDDEN.getNo(), 131);
        assertEquals(CoAPResponseCode.FORBIDDEN.getDescription(),
                "4.03 Forbidden");

        assertEquals(CoAPResponseCode.NOT_FOUND.getNo(), 132);
        assertEquals(CoAPResponseCode.NOT_FOUND.getDescription(),
                "4.04 Not Found");

        assertEquals(CoAPResponseCode.METHOD_NOT_ALLOWED.getNo(), 133);
        assertEquals(CoAPResponseCode.METHOD_NOT_ALLOWED.getDescription(),
                "4.05 Method Not Allowed");

        assertEquals(CoAPResponseCode.NOT_ACCEPTABLE.getNo(), 134);
        assertEquals(CoAPResponseCode.NOT_ACCEPTABLE.getDescription(),
                "4.06 Not Acceptable");

        assertEquals(CoAPResponseCode.REQUEST_ENTITY_TOO_LARGE.getNo(), 141);
        assertEquals(
                CoAPResponseCode.REQUEST_ENTITY_TOO_LARGE.getDescription(),
                "4.13 Request Entity Too Large");

        assertEquals(CoAPResponseCode.UNSUPPORTED_MEDIA_TYPE.getNo(), 143);
        assertEquals(CoAPResponseCode.UNSUPPORTED_MEDIA_TYPE.getDescription(),
                "4.15 Unsupported Media Type");

        assertEquals(CoAPResponseCode.INTERNAL_SERVER_ERROR.getNo(), 160);
        assertEquals(CoAPResponseCode.INTERNAL_SERVER_ERROR.getDescription(),
                "5.00 Internal Server Error");

        assertEquals(CoAPResponseCode.NOT_IMPLEMENTED.getNo(), 161);
        assertEquals(CoAPResponseCode.NOT_IMPLEMENTED.getDescription(),
                "5.01 Not Implemented");

        assertEquals(CoAPResponseCode.BAD_GATEWAY.getNo(), 162);
        assertEquals(CoAPResponseCode.BAD_GATEWAY.getDescription(),
                "5.02 Bad Gateway");

        assertEquals(CoAPResponseCode.SERVICE_UNAVAILABLE.getNo(), 163);
        assertEquals(CoAPResponseCode.SERVICE_UNAVAILABLE.getDescription(),
                "5.03 Service Unavailable");

        assertEquals(CoAPResponseCode.GATEWAY_TIMEOUT.getNo(), 164);
        assertEquals(CoAPResponseCode.GATEWAY_TIMEOUT.getDescription(),
                "5.04 Gateway Timeout");

        assertEquals(CoAPResponseCode.PROXYING_NOT_SUPPORTED.getNo(), 165);
        assertEquals(CoAPResponseCode.PROXYING_NOT_SUPPORTED.getDescription(),
                "5.05 Proxying Not Supported");
    }
}
