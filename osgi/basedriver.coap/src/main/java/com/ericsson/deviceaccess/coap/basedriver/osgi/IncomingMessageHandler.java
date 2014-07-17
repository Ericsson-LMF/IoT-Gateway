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
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPMessage;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPMessage.CoAPMessageType;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionHeader;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPRequest;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPResponse;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPResponseCode;
import com.ericsson.deviceaccess.coap.basedriver.util.CoAPMessageReader;

import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class handles the incoming (CoAP) messages, detects duplicates and
 * forwards incoming messages onwards to CoAP Endpoint (which will handle
 * req/resp matching)
 */
public class IncomingMessageHandler implements IncomingMessageListener {

    private IncomingCoAPListener incomingCoAPListener;

    // Keep track of incoming messages with socket address (toString) + message
    // ID as key, these are used for detecting duplicate incoming messages
    final private HashMap incomingRequests;
    final private HashMap incomingResponses;

    // Remove the entry of incomingRequests when expired
    final private Timer incomingRequestsTimer = new Timer();
    final static long incomingRequestsExpirationTime = 60000; //msec

    /**
     * Constructor is protected, this is a singleton class. Instance can be
     * fetched using the CoAPMessageHandlerFactory class.
     */
    protected IncomingMessageHandler() {
        incomingRequests = new HashMap();
        incomingResponses = new HashMap();
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
            /*
             CoAPActivator.logger.error("No incoming CoAP listener defined");
             */
            return;
        }

        CoAPMessageReader handler = new CoAPMessageReader(datagram);
        CoAPMessage msg = handler.decode();
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
        if (datagram.getLength() == 1281) {
            // response with a 4.13 entity too large
			/*CoAPActivator.logger
             .debug("Too large datagram received, reply with a 4.13 response");
             */
            OutgoingMessageHandler outgoingMessageHandler = this.incomingCoAPListener
                    .getOutgoingMessageHandler();
            if (msg.getClass() == CoAPRequest.class) {
                CoAPRequest req = (CoAPRequest) msg;
                CoAPResponse resp = new CoAPResponse(
                        CoAPMessageType.CONFIRMABLE,
                        CoAPResponseCode.REQUEST_ENTITY_TOO_LARGE.getNo(),
                        req.getMessageId());

                CoAPOptionHeader header = req.getTokenHeader();
                if (header != null) {
                    resp.addOptionHeader(header);
                }
                resp.setSocketAddress(req.getSocketAddress());
                outgoingMessageHandler.send(resp, false);
            } else {
                System.out
                        .println("TODO: If a response is larger than would fit in the buffer, repeat the request with a suitable Block1 option");
            }
            return;
        }

        // If there are unrecognized options, reply with a reset message for
        // confirmable response and 4.02 response to a confirmable request
        // (fraft-ietf-core-coap-08)
        if ((msg.getMessageType() == CoAPMessageType.CONFIRMABLE)
                && !handler.validOptions()) {
            //CoAPActivator.logger.debug("Options in the message not valid");

            OutgoingMessageHandler outgoingMessageHandler = this.incomingCoAPListener
                    .getOutgoingMessageHandler();

            // in case of response, return rst
            if (msg.getClass() == CoAPResponse.class) {
                CoAPResponse resp = (CoAPResponse) msg;
                CoAPResponse reset = resp.createReset();
                /*CoAPActivator.logger
                 .debug("Reply to unrecognized options in a response with reset message ");
                 */
                outgoingMessageHandler.send(reset, false);
            } else {
                // in case of request, return 4.02 bad option
                CoAPRequest req = (CoAPRequest) msg;
                CoAPResponse resp = new CoAPResponse(
                        CoAPMessageType.CONFIRMABLE,
                        CoAPResponseCode.BAD_OPTION.getNo(), req.getMessageId());
                /*CoAPActivator.logger
                 .debug("Reply to unrecognized options in a response with a 4.02 response");
                 */
                CoAPOptionHeader header = req.getTokenHeader();
                if (header != null) {
                    resp.addOptionHeader(header);
                }
                resp.setSocketAddress(req.getSocketAddress());
                outgoingMessageHandler.send(resp, false);
            }
        } else {
            try {
                // If the incoming message is a response and try to find a match
                if (msg.getClass() == CoAPResponse.class) {
                    this.handleResponse((CoAPResponse) msg);
                } else if (msg.getClass() == CoAPRequest.class) {
                    this.handleRequest((CoAPRequest) msg);
                }
            } catch (CoAPException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This methods looks for duplicates
     *
     * @param resp
     */
    private void handleResponse(CoAPResponse resp) throws CoAPException {
        // On messaging level, check for duplicates based on message ID + IP
        // check the received responses

        /*
         CoAPActivator.logger
         .debug("Check if this is a duplicate response with ID: ["
         + resp.getIdentifier() + "] and message type ["
         + resp.getMessageType().getName() + "]");
         */
        CoAPResponse duplicate = (CoAPResponse) this.incomingResponses.get(resp.getIdentifier());

        if (duplicate != null) {
            /*
             CoAPActivator.logger
             .debug("A duplicate response was received, try to reply with the same message");
             */
            // TODO process only in case of CON??

            OutgoingMessageHandler outgoingMessageHandler = this.incomingCoAPListener
                    .getOutgoingMessageHandler();
            CoAPResponse cachedResponse = (CoAPResponse) outgoingMessageHandler
                    .getOutgoingResponses().get(resp.getIdentifier());

            if (cachedResponse != null) {
                /*
                 CoAPActivator.logger
                 .debug("A cached response was found, try to reply with the same message");
                 */
                outgoingMessageHandler.send(cachedResponse, false);
            } else {
                /*
                 CoAPActivator.logger
                 .warn("No cached response was found, discard this response");
                 */
            }
            return;
        }

        // No duplicate was found so this is a new incoming response. Stop
        // retransmission task.
        OutgoingMessageHandler outgoingMessageHandler = this.incomingCoAPListener
                .getOutgoingMessageHandler();

        HashMap outgoingRequests = outgoingMessageHandler.getOutgoingRequests();
        HashMap outgoingReplies = outgoingMessageHandler.getOutgoingResponses();
        CoAPRequest sentRequest = (CoAPRequest) outgoingRequests.get(resp
                .getIdentifier());

        // Message ID will be different if this is a separate response (tokens
        // will match with the request)
        // If a match for the incoming response was found
        if (sentRequest != null) {
            if (resp.getMessageType() == CoAPMessageType.CONFIRMABLE) {
                ackMessage(resp);
            }

            // some kind of response received, remove retransmission task
            outgoingMessageHandler.removeRetransmissionTask(sentRequest);
            this.incomingResponses.put(resp.getIdentifier(), resp);
            incomingCoAPListener.handleResponse(resp);
            return;
        }

        if (resp.getMessageType() == CoAPMessageType.ACKNOWLEDGEMENT) {
            CoAPResponse sentResponse = (CoAPResponse) outgoingReplies.get(resp
                    .getIdentifier());
            if (sentResponse != null) {
                /*
                 * If the outgoing request was also a response, then there's no
                 * need to nofify the listener. the ack acknowledges the con
                 * response.
                 */
                outgoingMessageHandler.removeRetransmissionTask(sentResponse);
                return;
            }
        }

        // Check also the tokens, this is the case for separate responses
        boolean test = false;
        Iterator entries = outgoingRequests.entrySet().iterator();

        while (entries.hasNext()) {
            // Nullpointer somewhere here
            Map.Entry entry = (Entry) entries.next();
            if (entry.getValue() != null) {
                if (((CoAPMessage) entry.getValue()).getTokenHeader() != null) {

                    byte[] token = ((CoAPMessage) entry.getValue())
                            .getTokenHeader().getValue();

                    sentRequest = (CoAPRequest) entry.getValue();

                    if (resp.getTokenHeader() != null) {
                        if (resp.getTokenHeader() != null) {
                            byte[] respToken = resp.getTokenHeader().getValue();
                            test = Arrays.equals(token, respToken);
                            if (test) {
                                break;
                            }
                        }
                    }
                }
                // if tokens match, one should accept the request
            }
        }

        if (test) {
            // If response is confirmable, acknowledge it immediately
            if (resp.getMessageType() == CoAPMessageType.CONFIRMABLE) {
                ackMessage(resp);
            }

            outgoingMessageHandler.removeRetransmissionTask(sentRequest);
            incomingCoAPListener.handleResponse(resp);
        } else {
            /*
             CoAPActivator.logger
             .info("Sent out request could not be found, reply with a reset");
             */
            CoAPResponse reset = resp.createReset();
            this.incomingCoAPListener.getOutgoingMessageHandler().send(reset,
                    false);
            //CoAPActivator.logger.warn("incoming response could not be handled");
        }
    }

    private void ackMessage(CoAPResponse resp) {
        /*
         CoAPActivator.logger
         .info("Outgoing request found. Confirmable message received, ack immediately");
         */

        CoAPResponse acknowledgement = resp.createAcknowledgement();
        this.incomingCoAPListener.getOutgoingMessageHandler().send(
                acknowledgement, false);
    }

    /**
     * This just forwards incoming requests to incoming request listeners, they
     * should handle those
     *
     * @param req
     */
    private void handleRequest(final CoAPRequest req) {

        // Register requests until expired
        synchronized (this.incomingRequests) {
            this.incomingRequests.put(req.getIdentifier(), req);
        }
        this.incomingRequestsTimer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        synchronized (incomingRequests) {
                            CoAPRequest registeredReq = (CoAPRequest) incomingRequests.get(req.getIdentifier());
                            if (registeredReq == req) {
                                incomingRequests.remove(req.getIdentifier());
                            }
                        }
                    }
                },
                incomingRequestsExpirationTime
        );
        this.incomingCoAPListener.handleRequest(req);
    }

    public CoAPRequest getIncomingRequest(CoAPMessage msg) {
        synchronized (this.incomingRequests) {
            return (CoAPRequest) this.incomingRequests.get(msg.getIdentifier());
        }
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
