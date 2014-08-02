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

import com.ericsson.commonutil.function.FunctionalUtil;
import com.ericsson.deviceaccess.coap.basedriver.api.CoAPException;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPMessage;
import static com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPMessage.CoAPMessageType.ACKNOWLEDGEMENT;
import static com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPMessage.CoAPMessageType.CONFIRMABLE;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPMessageFormat;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionHeader;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPRequest;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPResponse;
import static com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPResponseCode.BAD_OPTION;
import static com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPResponseCode.REQUEST_ENTITY_TOO_LARGE;
import com.ericsson.deviceaccess.coap.basedriver.util.CoAPMessageReader;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class handles the incoming (CoAP) messages, detects duplicates and
 * forwards incoming messages onwards to CoAP Endpoint (which will handle
 * req/resp matching)
 */
public class IncomingMessageHandler implements IncomingMessageListener {

    static final long incomingRequestsExpirationTime = 60000; //msec

    private IncomingCoAPListener incomingCoAPListener;

    // Keep track of incoming messages with socket address (toString) + message
    // ID as key, these are used for detecting duplicate incoming messages
    final private Map<String, CoAPRequest> incomingRequests;
    final private Map<String, CoAPResponse> incomingResponses;

    // Remove the entry of incomingRequests when expired
    final private Timer incomingRequestsTimer = new Timer();

    /**
     * Constructor is protected, this is a singleton class. Instance can be
     * fetched using the CoAPMessageHandlerFactory class.
     */
    protected IncomingMessageHandler() {
        incomingRequests = new ConcurrentHashMap<>();
        incomingResponses = new ConcurrentHashMap<>();
    }

    /**
     * This listener method is called by the TransportLayerReceived when a
     * datagram packet is received. It will further call on the
     * IncomingCoAPMessageListener which is set as listener
     *
     * @param datagram received datagram
     */
    @Override
    public void messageReceived(DatagramPacket datagram) {
        if (incomingCoAPListener == null) {
            // CoAPActivator.logger.error("No incoming CoAP listener defined");
            return;
        }

        CoAPMessageReader handler = new CoAPMessageReader(datagram);

        if (datagram.getLength() >= 1281) {
            // response with a 4.13 entity too large
            //CoAPActivator.logger.debug("Too large datagram received, reply with a 4.13 response");
            OutgoingMessageHandler outHandler = incomingCoAPListener.getOutgoingMessageHandler();
            CoAPMessage msg = null;
            try {
                msg = handler.decodeStart();
            } catch (CoAPMessageFormat.IncorrectMessageException ex) {
                Logger.getLogger(IncomingMessageHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (msg instanceof CoAPRequest) {
                CoAPResponse resp = new CoAPResponse(1, (CoAPRequest) msg, CONFIRMABLE, REQUEST_ENTITY_TOO_LARGE);
                resp.addOptionHeader(new CoAPOptionHeader(CoAPOptionName.SIZE1, "1280".getBytes(StandardCharsets.UTF_8)));
                outHandler.send(resp, false);
            } else {
                System.out.println("TODO: If a response is larger than would fit in the buffer, repeat the request with a suitable Block1 option");
            }
            return;
        }

        CoAPMessage msg = null;
        try {
            msg = handler.decode();
        } catch (CoAPMessageFormat.IncorrectMessageException ex) {
            Logger.getLogger(IncomingMessageHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        // log each message
        /*
         String msgStr = "*** Incoming CoAP message ***\n"
         + msg.logMessage();
         CoAPActivator.logger.info(msgStr);
         try {
         // Write to a file too (interop tests)
         CoAPActivator.out.write(msgStr);
         CoAPActivator.out.flush();
         } catch (IOException e) {
         e.printStackTrace();
         }
         */
        // If there are unrecognized options, reply with a reset message for
        // confirmable response and 4.02 response to a confirmable request
        // (fraft-ietf-core-coap-08)
        if (!handler.validOptions()) {
            if (msg.getMessageType() == CONFIRMABLE) {
                //CoAPActivator.logger.debug("Options in the message not valid");
                OutgoingMessageHandler outHandler = incomingCoAPListener.getOutgoingMessageHandler();
                // in case of response, return rst
                if (msg instanceof CoAPResponse) {
                    //CoAPActivator.logger.debug("Reply to unrecognized options in a response with reset message ");
                    resetMessage((CoAPResponse) msg);
                } else {
                    // in case of request, return 4.02 bad option
                    //CoAPActivator.logger.debug("Reply to unrecognized options in a response with a 4.02 response");
                    CoAPResponse resp = new CoAPResponse(1, (CoAPRequest) msg, CONFIRMABLE, BAD_OPTION);
                    outHandler.send(resp, false);
                }
            }
        } else {
            try {
                // If the incoming message is a response and try to find a match
                if (msg instanceof CoAPResponse) {
                    handleResponse((CoAPResponse) msg);
                } else if (msg instanceof CoAPRequest) {
                    handleRequest((CoAPRequest) msg);
                }
            } catch (CoAPException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This methods looks for duplicates
     *
     * @param response
     */
    private void handleResponse(CoAPResponse response) throws CoAPException {
        // On messaging level, check for duplicates based on message ID + IP
        // check the received responses
        /*
         CoAPActivator.logger
         .debug("Check if this is a duplicate response with ID: ["
         + resp.getIdentifier() + "] and message type ["
         + resp.getMessageType().getName() + "]");
         */
        String id = response.getIdentifier();
        OutgoingMessageHandler outHandler = incomingCoAPListener.getOutgoingMessageHandler();
        if (incomingResponses.containsKey(id)) {
            //CoAPActivator.loggerdebug("A duplicate response was received, try to reply with the same message");
            // TODO process only in case of CON??
            CoAPResponse cachedResponse = outHandler.getOutgoingResponses().get(id);
            if (cachedResponse != null) {
                //CoAPActivator.logger.debug("A cached response was found, try to reply with the same message");
                outHandler.send(cachedResponse, false);
            } else {
                //CoAPActivator.logger.warn("No cached response was found, discard this response");
            }
            return;
        }

        // No duplicate was found so this is a new incoming response. Stop retransmission task.
        Map<String, CoAPRequest> outgoingRequests = outHandler.getOutgoingRequests();
        Map<String, CoAPResponse> outgoingReplies = outHandler.getOutgoingResponses();
        CoAPRequest sentRequest = outgoingRequests.get(id);

        // Message ID will be different if this is a separate response (tokens
        // will match with the request)
        // If a match for the incoming response was found
        if (sentRequest != null) {
            if (response.getMessageType() == CONFIRMABLE) {
                ackMessage(response);
            }

            // some kind of response received, remove retransmission task
            outHandler.removeRetransmissionTask(sentRequest);
            incomingResponses.put(id, response);
            incomingCoAPListener.handleResponse(response);
            return;
        }

        if (response.getMessageType() == ACKNOWLEDGEMENT) {
            CoAPResponse sentResponse = outgoingReplies.get(id);
            if (sentResponse != null) {
                /*
                 * If the outgoing request was also a response, then there's no
                 * need to nofify the listener. the ack acknowledges the con
                 * response.
                 */
                outHandler.removeRetransmissionTask(sentResponse);
                return;
            }
        }

        // Check also the tokens, this is the case for separate responses
        byte[] header = response.getToken();
        if (header != null) {
            Optional<CoAPRequest> request = outgoingRequests
                    .values()
                    .stream()
                    .filter(v -> v.getToken() != null)
                    .filter(v -> Arrays.equals(v.getToken(), header))
                    .findAny();
            if (request.isPresent()) {
                if (response.getMessageType() == CONFIRMABLE) {
                    ackMessage(response);
                }
                outHandler.removeRetransmissionTask(request.get());
                incomingCoAPListener.handleResponse(response);
                return;
            }
        }
        resetMessage(response);
    }

    private void ackMessage(CoAPResponse resp) {
        //CoAPActivator.logger.info("Outgoing request found. Confirmable message received, ack immediately");
        incomingCoAPListener.getOutgoingMessageHandler().send(resp.createAcknowledgement(), false);
    }

    private void resetMessage(CoAPResponse resp) {
        incomingCoAPListener.getOutgoingMessageHandler().send(resp.createReset(), false);
    }

    /**
     * This just forwards incoming requests to incoming request listeners, they
     * should handle those
     *
     * @param req
     */
    private void handleRequest(final CoAPRequest req) {
        String id = req.getIdentifier();
        // Register requests until expired
        incomingRequests.put(id, req);
        incomingRequestsTimer.schedule(
                FunctionalUtil.timerTask(() -> incomingRequests.computeIfPresent(id, (k, v) -> v == req ? null : req)),
                incomingRequestsExpirationTime
        );
        incomingCoAPListener.handleRequest(req);
    }

    public CoAPRequest getIncomingRequest(CoAPMessage msg) {
        return incomingRequests.get(msg.getIdentifier());
    }

    /**
     * Set a listener for incoming CoAP messages received by this handler
     *
     * @param requestListener listener to set
     */
    public void setIncomingCoAPListener(IncomingCoAPListener requestListener) {
        incomingCoAPListener = requestListener;
    }
}
