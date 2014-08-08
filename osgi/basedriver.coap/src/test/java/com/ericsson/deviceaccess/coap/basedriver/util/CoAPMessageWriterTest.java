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

import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPMessage.CoAPMessageType;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPMessageFormat;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionHeader;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPResponse;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPResponseCode;
import java.net.DatagramPacket;
import java.net.URI;
import java.net.URISyntaxException;
import junit.framework.TestCase;

public class CoAPMessageWriterTest extends TestCase {

    public CoAPMessageWriterTest() {
        super("OutgoingCoAPMessageParserTest");
    }

    /*
     * The only public method is the encode method, so test that
     *
     * @throws URISyntaxException
     */
    public void testEncode() throws URISyntaxException {

        URI uri = new URI("coap://127.0.0.1:5683/storage/helloworld");
        CoAPMessageType type = CoAPMessageType.NON_CONFIRMABLE;

        int msgCode = 2;
        int version = 1;
        short id = 2;

        //FIXME fix dependency to mock..
		/*Mockery context = new Mockery() {
         {
         setImposteriser(ClassImposteriser.INSTANCE);
         }
         };

         final TransportLayerSender sender = context
         .mock(TransportLayerSender.class);
         context.checking(new Expectations() {
         {
         allowing(sender).sendMessage(with(aNonNull(CoAPMessage.class)));
         }
         });
         *

         CoAPEndpointFactory endpointFactory = CoAPEndpointFactory.getInstance();
         CoAPMessageHandlerFactory messageHandlerFactory = CoAPMessageHandlerFactory
         .getInstance();

         OutgoingMessageHandler handler = messageHandlerFactory
         .getOutgoingCoAPMessageHandler(sender);
         IncomingMessageHandler incomingMessageHandler = messageHandlerFactory
         .getIncomingCoAPMessageHandler();

         InetAddress address = null;
         int coapPort = 5683;
         String socketAddress = "127.0.0.1";
         LocalCoAPEndpoint endpoint = null;

         InetSocketAddress sockaddr = null;
         try {
         address = InetAddress.getByName(socketAddress);
         sockaddr = new InetSocketAddress(address, coapPort);
         endpoint = endpointFactory.createLocalCoAPEndpoint(
         handler, incomingMessageHandler, address, coapPort);
         } catch (Exception e) {
         e.printStackTrace();
         }

         String path = uri.getPath();
         String[] inputSegments = path.split("/");
         CoAPRequest req = null;
         try {
         req = endpoint
         .createCoAPRequest(type, msgCode, sockaddr, uri, null);
         req.generateTokenHeader();
         } catch (CoAPException e) {
         e.printStackTrace();
         }

         int optionCount = 0;

         short contentTypeId = 41; // try with application/xml

         byte[] contentTypeBytes = BitOperations.splitShortToBytes(id);
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
         outputStream.write(contentTypeBytes[1]);

         CoAPOptionHeader h = new CoAPOptionHeader(CoAPOptionName.CONTENT_TYPE,

         outputStream.toByteArray());
         req.addOptionHeader(h);

         optionCount++;
         context.assertIsSatisfied();
         assertNotNull(handler);

         endpoint.sendRequest(req);

         CoAPMessageWriter writer = new CoAPMessageWriter(req);
         byte[] stream = writer.encode();

         DatagramPacket packet = new DatagramPacket(stream, stream.length);
         packet.setSocketAddress(sockaddr);

         CoAPMessageReader reader = new CoAPMessageReader(packet);
         CoAPMessage msg = reader.decode();

         assertEquals(msg.getOptionCount(), 6);
         assertEquals(msg.getCode(), msgCode);
         assertEquals(msg.getMessageType(), type);

         LinkedList<CoAPOptionHeader> options = msg.getOptionHeaders();
         Iterator<CoAPOptionHeader> it = options.iterator();
         int i = 0;
         // Go through different option numbers
         while (it.hasNext()) {

         CoAPOptionHeader header = (CoAPOptionHeader) it.next();
         String value = new String(header.getValue());

         if (header.getOptionNumber() == 3) {
         assertEquals(header.getOptionName(), "Proxy-Uri");
         assertEquals(value, "testserver");
         } else if (header.getOptionNumber() == 9) {
         assertEquals(header.getOptionName(), "Uri-Path");
         if (i == 1) {
         assertEquals("storage", value);
         } else if (i == 2) {
         assertEquals("helloworld", value);
         }
         }
         i++;
         }

         // This should remove proxy-port, proxy-path & proxy-host headers
         CoAPOptionHeader test = new CoAPOptionHeader(
         CoAPOptionName.PROXY_URI.getNo(),
         CoAPOptionName.PROXY_URI.getName(), "testserver".getBytes());
         req.addOptionHeader(test);

         assertEquals(3, req.getOptionCount());*/
    }

    public void testFencepostOptions() throws URISyntaxException {
        URI uri = new URI("coap://127.0.0.1:/storage/helloworld");
        CoAPMessageType type = CoAPMessageType.NON_CONFIRMABLE;

        int version = 1;
        short id = 2;

        CoAPResponse resp = new CoAPResponse(version, type, CoAPResponseCode.CONTENT, id);

        CoAPOptionHeader option1 = new CoAPOptionHeader("option_1", "test1".getBytes());
        CoAPOptionHeader option2 = new CoAPOptionHeader(CoAPOptionName.ETAG, "hello".getBytes());

        resp.addOptionHeader(option1);
        resp.addOptionHeader(option2);

        assertEquals(2, resp.getOptionCount());

        CoAPMessageWriter writer = new CoAPMessageWriter(resp);
        byte[] stream;
        try {
            stream = writer.encode();
        } catch (CoAPMessageFormat.IncorrectMessageException ex) {
            fail("Problem with encoding; " + ex);
            return;
        }

        DatagramPacket packet = new DatagramPacket(stream, stream.length);

        CoAPMessageReader reader = new CoAPMessageReader(packet);
        CoAPResponse msg;
        try {
            msg = (CoAPResponse) reader.decode();
        } catch (CoAPMessageFormat.IncorrectMessageException ex) {
            fail("Problem with decoding; " + ex);
            return;
        }
        assertEquals(1, msg.getOptionHeaders(CoAPOptionName.ETAG).size());
        assertEquals(1, msg.getOptionHeaders(CoAPOptionName.UNKNOWN).size());
        assertEquals(2, msg.getOptionHeaders().size());
    }

    public void testEmptyAck() {
        CoAPResponse response = new CoAPResponse(1,
                CoAPMessageType.CONFIRMABLE, CoAPResponseCode.NOT_IMPLEMENTED, 1234);

        CoAPResponse emptyAck = response.createAcknowledgement();
        assertEquals(0, emptyAck.getCode().getNo());

    }
}
