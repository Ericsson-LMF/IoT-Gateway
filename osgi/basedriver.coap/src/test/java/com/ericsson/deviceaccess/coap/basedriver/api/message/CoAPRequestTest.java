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
import com.ericsson.deviceaccess.coap.basedriver.util.TokenGenerator;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import junit.framework.TestCase;

public class CoAPRequestTest extends TestCase {

    private CoAPRequest req;
    private CoAPOptionHeader hostOpt;
    private CoAPOptionHeader portOpt;
    private String host;
    private Integer port;

    public CoAPRequestTest() {
        super("CoAPRequestTest");
        req = new CoAPRequest(CoAPMessageType.CONFIRMABLE, 1, 1234);
        host = "127.0.0.1";
        port = 8080;
        try {
            URI uri = new URI("coap", null, host, port, null, null, null);
            //req.setUri(uri);

            hostOpt = new CoAPOptionHeader(CoAPOptionName.URI_HOST,
                    host.getBytes());
            byte[] portBytes = BitOperations.splitShortToBytes(port
                    .shortValue());
            portOpt = new CoAPOptionHeader(CoAPOptionName.URI_PORT, portBytes);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    /**
     * Test how the URI is formed in different cases. In the beginning a null
     * should be returned.
     */
    public void testGetUriFromRequest() {
        URI uri = null;
        try {
            uri = req.getUriFromRequest();
            assertNull(uri);

        } catch (CoAPException e) {
            e.printStackTrace();
        }

        String path = "testPath";

        req.addOptionHeader(hostOpt);
        req.addOptionHeader(portOpt);

        CoAPOptionHeader pathOpt = new CoAPOptionHeader(
                CoAPOptionName.URI_PATH, path.getBytes());
        req.addOptionHeader(pathOpt);

        try {
            uri = req.getUriFromRequest();
            assertNotNull(uri);

            assert (uri.getPort() == port);
            assertEquals(uri.getHost(), host);
            // Check with different paths, with and without leading slash
            assertEquals(uri.getPath(), "/" + path);

            // Try to remove a header, should succeed
            boolean remove = req.removeOptionHeader(pathOpt);
            assertTrue(remove);

            // try to remove a header, should fail now
            remove = req.removeOptionHeader(pathOpt);
            assertFalse(remove);

            path = "/testPath";
            pathOpt = new CoAPOptionHeader(CoAPOptionName.URI_PATH,
                    path.getBytes());
            req.addOptionHeader(pathOpt);
            assertEquals(uri.getPath(), path);

            // A not valid uri-path contains "/"
            req.removeOptionHeader(pathOpt);
            path = "/testPath/notvalid";
            pathOpt = new CoAPOptionHeader(CoAPOptionName.URI_PATH,
                    path.getBytes());
            assertFalse(req.addOptionHeader(pathOpt));
            assertEquals(0, req.getOptionHeaders(CoAPOptionName.URI_PATH)
                    .size());

            String firstPart = "/firstPart";
            pathOpt = new CoAPOptionHeader(CoAPOptionName.URI_PATH,
                    firstPart.getBytes());
            req.addOptionHeader(pathOpt);

            String secondPart = "secondPart";
            pathOpt = new CoAPOptionHeader(CoAPOptionName.URI_PATH,
                    secondPart.getBytes());
            req.addOptionHeader(pathOpt);

            uri = req.getUriFromRequest();
            assertEquals((firstPart + "/" + secondPart), uri.getPath());

            req.removeOptionHeader(pathOpt);
            secondPart = "/secondPart";
            pathOpt = new CoAPOptionHeader(CoAPOptionName.URI_PATH,
                    secondPart.getBytes());
            req.addOptionHeader(pathOpt);

            uri = req.getUriFromRequest();
            assertEquals((firstPart + secondPart), uri.getPath());

        } catch (CoAPException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test that the list of options returned for matching excludes
     * max-age,token & etag
     */
    public void testOptionsForMatching() {

        String path = "testPath";
        req.addOptionHeader(hostOpt);
        req.addOptionHeader(portOpt);

        CoAPOptionHeader pathOpt = new CoAPOptionHeader(
                CoAPOptionName.URI_PATH, path.getBytes());
        req.addOptionHeader(pathOpt);

        // options for matching should exclude token, max-age and etag headers
        int seconds = 40;
        byte[] maxAgeBytes = BitOperations.splitIntToBytes(seconds);

        CoAPOptionHeader maxAgeHeader = new CoAPOptionHeader(
                CoAPOptionName.MAX_AGE, maxAgeBytes);

        req.addOptionHeader(maxAgeHeader);

        TokenGenerator generator = new TokenGenerator();
        try {
            long token = generator.createToken(req.getUriFromRequest());

            byte[] bytes = BitOperations.splitLongToBytes(token);
            CoAPOptionHeader tokenHeader = new CoAPOptionHeader(
                    CoAPOptionName.TOKEN, bytes);
            req.addOptionHeader(tokenHeader);
            String etag = "helloetag";

            CoAPOptionHeader etagOpt = new CoAPOptionHeader(
                    CoAPOptionName.ETAG, etag.getBytes());
            req.addOptionHeader(etagOpt);

            List<CoAPOptionHeader> options = req.optionsForMatching();
            assertFalse(options.contains(etagOpt));
            assertFalse(options.contains(tokenHeader));
            assertFalse(options.contains(maxAgeHeader));

            assertTrue(options.contains(this.hostOpt));
            assertTrue(options.contains(this.portOpt));

        } catch (CoAPException e) {
            e.printStackTrace();
        }
    }

}
