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
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionHeader;
import static com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName.BLOCK1;
import static com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName.BLOCK2;
import static com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName.URI_HOST;
import static com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName.URI_PATH;
import static com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName.URI_PORT;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPRequest;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPResponse;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPUtil;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class takes care of handling blockwise transfers identified by Block1
 * and Block2 options. It will take care of creating the next block request to
 * be sent out etc, but LocalEndpoint class is responsible for sending out the
 * requests.
 *
 * Note that blockwise message transfers towards observable resources do not
 * work at the moment.
 */
public class BlockwiseTransferHandler {

    private final Map<String, CoAPMessage> blockwiseMessages;
    private final Map<String, CoAPRequest> ongoingBlockwiseRequests;
    private final LocalCoAPEndpoint endpoint;

    // Max block size from draft-ietf-core-blao
    // private static int maxBlockSize = 1024;
    private int maxBlockSize;
    private int maxSzx;

    /**
     * Constructor
     *
     * @param endpoint endpoint sending the messages
     */
    public BlockwiseTransferHandler(LocalCoAPEndpoint endpoint) {
        this.blockwiseMessages = new HashMap<>();
        this.ongoingBlockwiseRequests = new HashMap<>();
        this.endpoint = endpoint;
        this.maxSzx = 6;
        this.maxBlockSize = 1024; // blockwise transfer draft 07
    }

    /**
     * This method is used to set the maximum value of szx to something else
     * than 6
     *
     * @param szx maximum value of the szx (0<=szx<=6)
     */
    public void setMaxSzx(int szx) {
        this.maxSzx = szx;
        this.maxBlockSize = CoAPUtil.getBlockSize(maxSzx).intValue();
    }

    /**
     * This method is used when requests are sent blockwise. If the size of the
     * request is > allowed block size defined in the local CoAP endpoint, this
     * method will be called automatically to split the message.
     *
     * @param request request to be sent
     * @param blockNumber number of the block
     * @param szx
     * @return
     * @throws CoAPException
     */
    public CoAPRequest createBlockwiseRequest(CoAPRequest request, int blockNumber, int szx) throws CoAPException {
        if (request.getOptionHeaders(BLOCK2).isEmpty()) {
            return createBlock1Request(request, blockNumber, szx);
        }
        return request;
    }

    /**
     * This method is used when responses are sent blockwise. If the size of the
     * response is > allowed block size defined in the local CoAP endpoint, this
     * method will be called automatically to split the message.
     *
     * @param response response to be sent
     * @param blockNumber number of the block
     * @param szx
     * @return
     */
    public CoAPResponse createBlockwiseResponse(CoAPResponse response, int blockNumber, int szx) {
        // Control usage of Block1 option in CoAP response
        if (!response.getOptionHeaders(BLOCK1).isEmpty()) {
            return response;
        }
        // Descriptive usage of Block2 option
        return createBlock2Response(response, blockNumber, szx);
    }

    /**
     * This method will be called when a message with option header Block2 is
     * received. It will return the next block request that should be sent out,
     * or null if the last block was received.
     *
     * @param response response with block2 option received
     * @param request original request
     * @return
     * @throws CoAPException
     */
    public CoAPRequest block2OptionReceived(CoAPResponse response, CoAPRequest request) throws CoAPException {
        List<CoAPOptionHeader> block2 = response.getOptionHeaders(BLOCK2);
        // TODO can there be multiple block options?
        CoAPOptionHeader blockOption = block2.get(0);

        // Check if there exist a message with the same token
        String tokenString = new String(response.getToken(), StandardCharsets.UTF_8);
        CoAPMessage originalResponse = blockwiseMessages.get(tokenString);

        if (originalResponse != null) {

            // append received payload to first response
            byte[] initialPayload = originalResponse.getPayload();
            byte[] newPayload = new byte[initialPayload.length + response.getPayload().length];

            System.arraycopy(initialPayload, 0, newPayload, 0, initialPayload.length);
            System.arraycopy(response.getPayload(), 0, newPayload, initialPayload.length, response.getPayload().length);

            response.setPayload(newPayload);
        }

        blockwiseMessages.put(tokenString, response);

        BlockOptionHeader blockOptionHeader = new BlockOptionHeader(blockOption);

        // if the m flag is set, there are still more blocks
        if (blockOptionHeader.getMFlag()) {
            byte[] tokenHeader = response.getToken();
            CoAPRequest blockRequest = endpoint.createCoAPRequest(
                    request.getMessageType(), 1, request.getSocketAddress(),
                    request.getUriFromRequest(), tokenHeader);
            // blockRequest.setListener(request.getListener());
            int nextBlockNumber = blockOptionHeader.getBlockNumber() + 1;
            int szx = blockOptionHeader.getSzx();
            // If the received szx is bigger that the client can accept, change
            // it to correct one for the next request
            if (blockOptionHeader.getSzx() > maxSzx) {
                szx = maxSzx;
            }

            // TODO do this in a more clever way
            BlockOptionHeader nextBlock = new BlockOptionHeader(BLOCK2, nextBlockNumber, false, szx);

            blockRequest.setListener(request.getListener());
            blockRequest.addOptionHeader(nextBlock);

            return blockRequest;
        }
        return null;
    }

    /**
     * This method will be called when a response with block1 option is received
     *
     * @param response received response
     * @param request request that was sent out
     * @return
     * @throws CoAPException
     */
    public CoAPRequest block1OptionResponseReceived(CoAPResponse response, CoAPRequest request) throws CoAPException {
        // Block1 in a response means that the request is acknowledged
        // need to check if the size of block should be changed from the request

        // read the block size from the sent request
        CoAPOptionHeader blockOption = response.getOptionHeaders(BLOCK1).get(0);
        BlockOptionHeader header = new BlockOptionHeader(blockOption);

        // read szx from the response
        int szx = header.getSzx();

        String tokenString = new String(request.getToken(), StandardCharsets.UTF_8);
        CoAPRequest originalRequest = ongoingBlockwiseRequests.get(tokenString);
        int diff = 1;
        if (!originalRequest.getOptionHeaders(BLOCK1).isEmpty()) {
            CoAPOptionHeader coapOption = originalRequest.getOptionHeaders(BLOCK1).get(0);
            BlockOptionHeader h = new BlockOptionHeader(coapOption);
            int originalSzx = h.getSzx();

            // TODO Can the client still adjust the size of the block after
            // block1??
            if (originalSzx > szx && header.getBlockNumber() == 0) {
				// recalculate the blocknumber

                // TODO use bit operations for the calculations..
                int blockSize = CoAPUtil.getBlockSize(originalSzx).intValue();
                int newBlockSize = CoAPUtil.getBlockSize(szx).intValue();
                diff = blockSize / newBlockSize;
            }
        }

        // if the szx has been decreased by the server, increase the block
        // number too!!!
        int newBlockNumber = header.getBlockNumber() + diff;

        // TODO mflag in the response indicates only if the response carries
        // the final response
        // if (header.getMFlag()) {
        CoAPRequest blockRequest = createBlock1Request(originalRequest, newBlockNumber, szx);

        // Calculate szx from the block size:
        // 2^(4+SZX) = 512 =>
        return blockRequest;
    }

    private CoAPRequest createBlock1Request(CoAPRequest request,
            int blockNumber, int szx) throws CoAPException {

        CoAPRequest blockRequest = null;
        int payloadLeft = 0;
        int blockSize = CoAPUtil.getBlockSize(szx).intValue();

        int payloadHandled = blockNumber * blockSize;

        if (request.getPayload() != null) {
            payloadLeft = request.getPayload().length - payloadHandled;
        }
        if (payloadLeft <= 0) {
            return blockRequest;
        }

        blockRequest = endpoint.createCoAPRequest(request.getMessageType(),
                request.getCode(), request.getSocketAddress(),
                request.getUriFromRequest(), request.getToken());

        blockRequest.setListener(request.getListener());

        byte[] payload = new byte[blockSize];
        System.arraycopy(request.getPayload(), payloadHandled, payload, 0,
                // blockSize); // will cause OutOfArrayIndex
                Math.min(payloadLeft, blockSize));

        blockRequest.setPayload(payload);

        // Check for more blocks, if the payload left is bigger than the
        // blocksize, then needs to be split
        boolean mFlag = blockSize < payloadLeft;
        if (mFlag) {
            // TODO should this be the blocksize or the payload left
            blockSize = payloadLeft;
        }

        // copy the options
        request.getOptionHeaders()
                .stream()
                .filter(h -> h.getOptionName() != URI_HOST)
                .filter(h -> h.getOptionName() != URI_PATH)
                .filter(h -> h.getOptionName() != URI_PORT)
                .filter(h -> h.getOptionName() != BLOCK1)
                .filter(h -> h.getOptionName() != BLOCK2)
                .forEach(blockRequest::addOptionHeader);

        // Calculate szx from the block size:
        // 2^(4+SZX) = 512 =>
        // Form the block option header
        BlockOptionHeader nextBlock = new BlockOptionHeader(BLOCK1, blockNumber, mFlag, szx);

        boolean ok = blockRequest.addOptionHeader(nextBlock);
        blockRequest.setListener(request.getListener());

        if (blockNumber == 0) {
            String tokenString = new String(request.getToken(), StandardCharsets.UTF_8);
            ongoingBlockwiseRequests.put(tokenString, request);
        }
        return blockRequest;
    }

    // TODO:
    //   This method is almost same as createBlock1Request() method.
    //   So common part should be cut off as an another method and be shared.
    //
    private CoAPResponse createBlock2Response(CoAPResponse response, int blockNumber, int szx) {
        int payloadLeft = 0;
        int blockSize = CoAPUtil.getBlockSize(szx).intValue();

        int payloadHandled = blockNumber * blockSize;

        if (response.getPayload() != null) {
            payloadLeft = response.getPayload().length - payloadHandled;
        }
        // No need for blockwise transfer
        if (payloadLeft <= 0) {
            return response;
        }

        CoAPResponse blockResponse = new CoAPResponse(1, response.getMessageType(), response.getCode(), response.getMessageId(), response.getToken());
        blockResponse.setSocketAddress(response.getSocketAddress());

        //byte[] payload = new byte[blockSize];
        int size = Math.min(payloadLeft, blockSize);
        byte[] payload = new byte[size];
        System.arraycopy(response.getPayload(), payloadHandled, payload, 0, size);

        blockResponse.setPayload(payload);

        // Check for more blocks, if the payload left is bigger than the
        // blocksize, then needs to be split
        boolean mFlag = blockSize < payloadLeft;
        if (mFlag) {
            // TODO should this be the blocksize or the payload left
            blockSize = payloadLeft;
        }

        response.getOptionHeaders()
                .stream()
                .filter(h -> h.getOptionName() != BLOCK1)
                .filter(h -> h.getOptionName() != BLOCK2)
                .forEach(blockResponse::addOptionHeader);

        // Calculate szx from the block size:
        // 2^(4+SZX) = 512 =>
        // Form the block option header
        BlockOptionHeader nextBlock = new BlockOptionHeader(BLOCK2, blockNumber, mFlag, szx);

        boolean ok = blockResponse.addOptionHeader(nextBlock);

        /*
         if (blockNumber == 0) {
         String tokenString = "";
         try {
         tokenString = new String(response.getTokenHeader().getValue(), "UTF8");
         this.ongoingBlockwiseRequests.put(tokenString, response);
         } catch (UnsupportedEncodingException e) {
         e.printStackTrace();
         }
         }
         */
        return blockResponse;
    }

}
