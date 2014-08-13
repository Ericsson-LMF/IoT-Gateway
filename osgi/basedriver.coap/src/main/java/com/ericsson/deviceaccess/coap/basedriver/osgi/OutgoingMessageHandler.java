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

import com.ericsson.deviceaccess.coap.basedriver.api.CoAPActivator;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPMessage;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPMessage.CoAPMessageType;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPRequest;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPRequestListener;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPResponse;
import com.ericsson.deviceaccess.coap.basedriver.communication.TransportLayerSender;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sends the message using the transport layer. Takes care of retransmissions
 * and detecting duplicate messages.
 */
public class OutgoingMessageHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutgoingMessageHandler.class);
    private static final int MAX_RETRANSMIT = 4;

    // message id 16 bits, so values between 0 and 65535
    public final static int MESSAGE_ID_MAX = 65535;

    public final static int MESSAGE_ID_MIN = 0;
    // TODO outgoing message cache should be cleaned up!!!
    // note that in case of transmission, the current state should be sent out
    // (rather than an old snapshot)
    // All sent messages with token as key
    private final Map<String, CoAPResponse> outgoingReplies;
    private final Map<String, CoAPRequest> outgoingRequests;

    // lower layer transport sender
    private final TransportLayerSender sender;

    // this variable keeps track
    private int messageId;

    private final Timer timer;

    private final Map<InetSocketAddress, RetransmissionTask> retransmissionTasks;

    /**
     * Constructor is protected, an instance of this handler should be fetched
     * using the MessageHandlerFactory
     *
     * @param sender an instance implementing TransportLayerSender interface
     */
    protected OutgoingMessageHandler(TransportLayerSender sender) {
        this.messageId = -1;
        this.outgoingReplies = new ConcurrentHashMap<>();
        this.outgoingRequests = new ConcurrentHashMap<>();
        this.timer = new Timer();
        this.sender = sender;
        this.retransmissionTasks = new ConcurrentHashMap<>();
    }

    /**
     * This method is responsible for actually sending the messages
     *
     * @param msg message to be sent
     * @param retransmission set to true if the message is resent
     */
    protected void send(CoAPMessage msg, boolean retransmission) {
        LOGGER.debug("SEND REQUEST: " + msg.getMessageType() + " and id: " + msg.getIdentifier());
        // Can send a new request to the same host, if no messages are in the retransmission queue
        if (!retransmission
                && retransmissionTasks.containsKey(msg.getSocketAddress())
                && msg instanceof CoAPRequest) {
            CoAPRequest req = (CoAPRequest) msg;

            if (req.getListener() != null) {
                req.getListener().serviceBusy(req);
            }
            return;
        }

        // messages are only retransmitted if they're of type confirmable
        if (msg.getMessageType() == CoAPMessageType.CONFIRMABLE) {
            int timeoutValue = msg.getTimeout();
            LOGGER.info("Transmit, nof retransmissions this far : [" + msg.getRetransmissions() + "] , max 4 (re)transmissions allowed");
            LOGGER.debug("Timeout value in milliseconds: [" + timeoutValue + "]");
            // start timer
            if (msg.getRetransmissions() < MAX_RETRANSMIT) {
                LOGGER.debug("Schedule retransmission");
                // Create retransmission only if this is not multicast message
                RetransmissionTask task = new RetransmissionTask(msg);
                retransmissionTasks.put(msg.getSocketAddress(), task);

                timer.schedule(task, timeoutValue);
                // Increase timeout and number of retransmission
                msg.messageRetransmitted();
            } else {
                LOGGER.info("Maximum number of retransmissions reached, cancel this message");
                removeRetransmissionTask(msg);
                msg.setMessageCanceled(true);

                // Remove message from the memory
                if (msg instanceof CoAPRequest) {
                    outgoingRequests.remove(msg.getIdentifier());
                    CoAPRequestListener listener = ((CoAPRequest) msg).getListener();
                    if (listener != null) {
                        listener.maximumRetransmissionsReached((CoAPRequest) msg);
                    }
                } else if (msg instanceof CoAPResponse) {
                    outgoingReplies.remove(msg.getIdentifier());
                }
            }
        }

        if (!retransmission) {
            // cache outgoing confirmable and non-confirm and ack if it has content
            if (msg.getMessageType() == CoAPMessageType.CONFIRMABLE
                    || msg.getMessageType() == CoAPMessageType.NON_CONFIRMABLE) {
                this.cacheMessage(msg);
            } else if (msg.getMessageType() == CoAPMessageType.ACKNOWLEDGEMENT
                    && msg.getCode().getNo() != 0) {
                this.cacheMessage(msg);
            }
        }

        String msgStr = "*** Outgoing CoAP message **\n" + msg.logMessage();
        LOGGER.info(msgStr);
        try {
            // Write to a file too (interop tests)
            CoAPActivator.out.write(msgStr);
            CoAPActivator.out.flush();
        } catch (IOException e) {
            LOGGER.debug("Problem to log the outgoing message");
        }

        // TODO How to handle non-confirmable messages??
        sender.sendMessage(msg);
    }

    /**
     * Private method to cache the outoing messages.
     *
     * @param msg message to cache
     */
    private void cacheMessage(CoAPMessage msg) {
        if (msg instanceof CoAPRequest) {
            outgoingRequests.put(msg.getIdentifier(), (CoAPRequest) msg);
        } else if (msg instanceof CoAPResponse) {
            outgoingReplies.put(msg.getIdentifier(), (CoAPResponse) msg);
        }
    }

    /**
     * Generate a message ID between 0 and 655535 (those included). In this
     * implementation, the message ID is always increased by one (acc. to draft
     * it should be totally random so this can be changed later if needed).
     * Synchronized for thread safety.
     *
     * @return next message ID
     */
    public synchronized int generateMessageId() {
        // create message ID and return
        if (messageId == -1) {
            Random random = new Random();
            messageId = random.nextInt(MESSAGE_ID_MAX + 1);
            return messageId;
        }
        if (messageId < MESSAGE_ID_MAX) {
            messageId++;
        } else { // start from 0
            messageId = MESSAGE_ID_MIN;
        }
        return messageId;
    }

    /**
     * Return a list of responses that have been sent (for detecting duplicate
     * messages for example)
     *
     * @return list of responses
     */
    public Map<String, CoAPResponse> getOutgoingResponses() {
        return outgoingReplies;
    }

    /**
     * Return a list of requests that have been sent
     *
     * @return list of requests
     */
    public Map<String, CoAPRequest> getOutgoingRequests() {
        return outgoingRequests;
    }

    /**
     * This method is called when a response for the request is received at the
     * endpoint. Calling this method will cancel the retransmissions.
     *
     * @param message message for which a confirmation was received
     */
    protected void removeRetransmissionTask(CoAPMessage message) {
        retransmissionTasks
                .entrySet()
                .stream()
                .filter(e -> e.getValue().getMessage().getIdentifier().equals(message.getIdentifier()))
                .findAny()
                .ifPresent(e -> {
                    e.getValue().cancel();
                    retransmissionTasks.remove(e.getKey());
                });
    }

    /**
     * This method is used when the bundle is stopped. It will cancel the timer
     * and the scheduled tasks.
     */
    public void stopService() {
        if (timer != null) {
            timer.cancel();
        }
    }

    /**
     * Inner class for running the retransmission timers for outgoing,
     * confirmable messages
     */
    private class RetransmissionTask extends TimerTask {

        private final CoAPMessage message;

        protected RetransmissionTask(CoAPMessage message) {
            this.message = message;
        }

        @Override
        public void run() {
            LOGGER.debug("Retransmit message with ID: " + message.getIdentifier());
            send(this.message, true);
        }

        public CoAPMessage getMessage() {
            return this.message;
        }
    }
}
