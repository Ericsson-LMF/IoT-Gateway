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

import com.ericsson.deviceaccess.coap.basedriver.api.message.*;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPMessage.CoAPMessageType;
import com.ericsson.deviceaccess.coap.basedriver.communication.TransportLayerSender;

import java.util.*;

/**
 * Sends the message using the transport layer. Takes care of retransmissions
 * and detecting duplicate messages.
 */
public class OutgoingMessageHandler {

    /**
     * Inner class for running the retransmission timers for outgoing,
     * confirmable messages
     */
    private class RetransmissionTask extends TimerTask {

        private CoAPMessage message;

        protected RetransmissionTask(CoAPMessage message) {
            this.message = message;
        }

        @Override
        public void run() {
            /*
             CoAPActivator.logger.debug("Retransmit message with ID: "
             + message.getIdentifier());
             */
            send(this.message, true);
        }

        public CoAPMessage getMessage() {
            return this.message;
        }
    }

    private static final int MAX_RETRANSMIT = 4;

	// TODO outgoing message cache should be cleaned up!!!
    // note that in case of transmission, the current state should be sent out
    // (rather than an old snapshot)
    // All sent messages with token as key
    private HashMap outgoingReplies;

    private HashMap outgoingRequests;

    // message id 16 bits, so values between 0 and 65535
    public final static int MESSAGE_ID_MAX = 65535;

    public final static int MESSAGE_ID_MIN = 0;

    // lower layer transport sender
    private TransportLayerSender sender;

    // this variable keeps track
    private int messageId;

    private Timer timer;

    private HashMap retransmissionTasks;

    /**
     * Constructor is protected, an instance of this handler should be fetched
     * using the MessageHandlerFactory
     *
     * @param sender an instance implementing TransportLayerSender interface
     */
    protected OutgoingMessageHandler(TransportLayerSender sender) {
        this.messageId = -1;
        this.outgoingReplies = new HashMap();
        this.outgoingRequests = new HashMap();
        this.timer = new Timer();
        this.sender = sender;
        this.retransmissionTasks = new HashMap();
    }

    /**
     * This method is responsible for actually sending the messages
     *
     * @param msg message to be sent
     * @param retransmission set to true if the message is resent
     */
    protected void send(CoAPMessage msg, boolean retransmission) {
		// System.out.println("SEND REQUEST: " + msg.getMessageType().toString()
        // + " and id: " + msg.getIdentifier());
        // Can send a new request to the same host, if no messages are in the
        // retransmission queue
        if (!retransmission
                && retransmissionTasks.containsKey(msg.getSocketAddress())
                && msg.getClass() == CoAPRequest.class) {
            CoAPRequest req = (CoAPRequest) msg;

            if (req.getListener() != null) {
                req.getListener().serviceBusy(req);
            }
            return;
        }

        // messages are only retransmitted if they're of type confirmable
        if (msg.getMessageType() == CoAPMessageType.CONFIRMABLE) {

            int timeoutValue = msg.getTimeout();

            /*
             CoAPActivator.logger
             .info("Transmit, nof retransmissions this far : ["
             + msg.getRetransmissions()
             + "] , max 4 (re)transmissions allowed");

             CoAPActivator.logger.debug("Timeout value in milliseconds: ["
             + timeoutValue + "]");
             */
            // start timer
            if (msg.getRetransmissions() < MAX_RETRANSMIT) {
                /*
                 CoAPActivator.logger.debug("Schedule retransmission");
                 */

				// Create retransmission only if this is not multicast message
                RetransmissionTask task = new RetransmissionTask(msg);
                this.retransmissionTasks.put(msg.getSocketAddress(), task);

                timer.schedule(task, timeoutValue);
                // Increase timeout and number of retransmission
                msg.messageRetransmitted();
            } else {
                /*
                 CoAPActivator.logger
                 .info("Maximum number of retransmissions reached, cancel this message");
                 */
                this.removeRetransmissionTask(msg);
                msg.setMessageCanceled(true);

                // Remove message from the memory
                if (msg.getClass() == CoAPRequest.class) {
                    this.outgoingRequests.remove(msg);

                    CoAPRequestListener listener = ((CoAPRequest) msg)
                            .getListener();
                    if (listener != null) {
                        listener.maximumRetransmissionsReached((CoAPRequest) msg);
                    }

                } else if (msg.getClass() == CoAPResponse.class) {
                    this.outgoingReplies.remove(msg);
                }
            }
        }

        if (!retransmission) {
			// cache outgoing confirmable and non-confirm and ack if it has
            // content
            if (msg.getMessageType() == CoAPMessageType.CONFIRMABLE
                    || msg.getMessageType() == CoAPMessageType.NON_CONFIRMABLE) {
                this.cacheMessage(msg);
            } else if (msg.getMessageType() == CoAPMessageType.ACKNOWLEDGEMENT
                    && msg.getCode() != 0) {
                this.cacheMessage(msg);
            }
        }

        /*
         try {
         String msgStr = "*** Outgoing CoAP message **\n"
         + msg.logMessage();
         CoAPActivator.logger.info(msgStr);
         try {
         // Write to a file too (interop tests)
         CoAPActivator.out.write(msgStr);
         CoAPActivator.out.flush();
         } catch (IOException e) {
         e.printStackTrace();
         }
         } catch (Exception e) {
         CoAPActivator.logger.debug("Problem to log the outgoing message");
         e.printStackTrace();
         }
         */
        // TODO How to handle non-confirmable messages??
        this.sender.sendMessage(msg);
    }

    /**
     * Private method to cache the outoing messages.
     *
     * @param msg message to cache
     */
    private synchronized void cacheMessage(CoAPMessage msg) {
        if (msg.getClass() == CoAPRequest.class) {
            this.outgoingRequests.put(msg.getIdentifier(), (CoAPRequest) msg);
        } else if (msg.getClass() == CoAPResponse.class) {
            this.outgoingReplies.put(msg.getIdentifier(), (CoAPResponse) msg);
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
        if (this.messageId == -1) {
            Random random = new Random();
            this.messageId = random.nextInt(MESSAGE_ID_MAX + 1);
            return this.messageId;
        }
        if (this.messageId < MESSAGE_ID_MAX) {
            this.messageId++;
        } else { // start from 0
            this.messageId = MESSAGE_ID_MIN;
        }
        return this.messageId;
    }

    /**
     * Return a list of responses that have been sent (for detecting duplicate
     * messages for example)
     *
     * @return list of responses
     */
    public synchronized HashMap getOutgoingResponses() {
        return this.outgoingReplies;
    }

    /**
     * Return a list of requests that have been sent
     *
     * @return list of requests
     */
    public synchronized HashMap getOutgoingRequests() {
        return this.outgoingRequests;
    }

    /**
     * This method is called when a response for the request is received at the
     * endpoint. Calling this method will cancel the retransmissions.
     *
     * @param message message for which a confirmation was received
     */
    protected synchronized void removeRetransmissionTask(CoAPMessage message) {
        RetransmissionTask task = null;

		// Iterate first to find the task to remove, then remove to avoid
        // concurrentmodificationexception
        for (Iterator i = this.retransmissionTasks.values().iterator(); i
                .hasNext();) {

            RetransmissionTask t = (RetransmissionTask) i.next();
            if (t.getMessage().getIdentifier().equals(message.getIdentifier())) {
                task = t;
                break;
            }
        }

        if (task != null) {
            task.cancel();
            RetransmissionTask t = (RetransmissionTask) this.retransmissionTasks
                    .remove(message.getSocketAddress());
        }
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
}
