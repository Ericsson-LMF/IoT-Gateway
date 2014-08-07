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
 * Class representing a CoAP response.
 */
public class CoAPResponse extends CoAPMessage {

    /**
     * Constructor.
     *
     * @param version CoAP version of the response
     * @param messageType type of the message
     * @param responseCode code of the message
     * @param messageId message ID
     * @param token token
     */
    public CoAPResponse(int version, CoAPMessageType messageType, int responseCode, int messageId, byte[] token) {
        super(version, messageType, responseCode, messageId, token);
    }

    public CoAPResponse(int version, CoAPMessageType messageType, int responseCode, int messageId) {
        super(version, messageType, responseCode, messageId, null);
    }

    /**
     * Constructor that will use draft-core-07 version by default
     *
     * @param messageType
     * @param responseCode
     * @param messageId
     * @param token token
     */
    public CoAPResponse(CoAPMessageType messageType, int responseCode, int messageId, byte[] token) {
        super(messageType, responseCode, messageId, token);
    }

    public CoAPResponse(CoAPMessageType messageType, int responseCode, int messageId) {
        super(messageType, responseCode, messageId, null);
    }

    public CoAPResponse(int version, CoAPMessageType messageType, CoAPResponseCode responseCode, int messageId, byte[] token) {
        this(version, messageType, responseCode.getNo(), messageId, token);
    }

    public CoAPResponse(int version, CoAPMessageType messageType, CoAPResponseCode responseCode, int messageId) {
        this(version, messageType, responseCode.getNo(), messageId, null);
    }

    public CoAPResponse(int version, CoAPRequest request, CoAPMessageType messageType, CoAPResponseCode responseCode) {
        this(version, messageType, responseCode.getNo(), request.getMessageId(), request.getToken());
        setSocketAddress(request.getSocketAddress());
    }

    /**
     * Create an empty ack for a response (empty ACKs are handled as a response)
     *
     * @return empty ACK for this response
     */
    public CoAPResponse createAcknowledgement() {
        CoAPResponse resp = new CoAPResponse(1, CoAPMessageType.ACKNOWLEDGEMENT, 0, this.getMessageId(), getToken());
        resp.setSocketAddress(getSocketAddress());
        return resp;
    }

    /**
     * Create an empty reset message based on the received response. A reset
     * message needs to be sent back if the received response cannot be handled
     * by the endpoint.
     *
     * @return a CoAP RST response for this CoAP response
     */
    public CoAPResponse createReset() {
        CoAPResponse resp = new CoAPResponse(1, CoAPMessageType.RESET, 0, getMessageId(), getToken());
        resp.setSocketAddress(getSocketAddress());
        return resp;
    }

    public boolean isCacheable() {
        return CoAPResponseCode.getResponseName(getCode()).isCacheable();
    }
}
