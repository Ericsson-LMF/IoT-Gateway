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

/**
 * Listener interface to listen events about outgoing CoAP requests. This
 * interface will be used to notify about single requests, not CoAP observation
 * requests.
 */
public interface CoAPRequestListener {

    /**
     * Callback method that is called when a reset message is received for a
     * message sent out towards the CoAP network. Receiving a reset indicates a
     * failure at the receiver (e.g. receiver rebooted and missing state).
     *
     * @param response CoAP response received from the network
     * @param request original request that was sent out to CoAP network
     */
    void resetResponseReceived(CoAPResponse response, CoAPRequest request);

    /**
     * Callback method that is called when a separate response related to the
     * sent out request is received. This callback is used also for replies to
     * non-confirmable requests.
     *
     * @param response
     * @param request request that the response relates to
     */
    void separateResponseReceived(CoAPResponse response,
            CoAPRequest request);

    /**
     * Callback method that is called when a piggypacked response is received
     *
     * @param response
     * @param request request that the response relates to
     */
    void piggyPackedResponseReceived(CoAPResponse response,
            CoAPRequest request);

    /**
     * Callback method that is called when an empty ack is received from the
     * remote host. This means that the actual payload will arrive in a separate
     * response.
     *
     * @param response received response
     * @param request request that the response relates to
     */
    void emptyAckReceived(CoAPResponse response, CoAPRequest request);

    /**
     * This method will be called when a maximum 4 retransmissions for the
     * request is reached. The retransmission timer is used as defined in the
     * draft-ietf-core-coap-07
     *
     * @param request
     */
    void maximumRetransmissionsReached(CoAPRequest request);

    /**
     * This method will be called in case the request was not sent towards the
     * CoAP network because of congestion control. According to core draft 07,
     * one active interaction per server is allowed at time.
     *
     * @param request
     */
    void serviceBusy(CoAPRequest request);

}
