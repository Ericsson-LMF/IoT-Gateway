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
import com.ericsson.deviceaccess.coap.basedriver.api.CoAPEndpoint;
import com.ericsson.deviceaccess.coap.basedriver.api.CoAPException;
import com.ericsson.deviceaccess.coap.basedriver.api.IncomingCoAPRequestListener;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPMessage.CoAPMessageType;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPMethodCode;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionHeader;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPRequest;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPRequestListener;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPResponse;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPResponseCode;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPUtil;
import com.ericsson.deviceaccess.coap.basedriver.api.resources.CoAPResource;
import com.ericsson.deviceaccess.coap.basedriver.api.resources.CoAPResourceObserver;
import com.ericsson.deviceaccess.coap.basedriver.osgi.BlockwiseResponseCache.SessionData;
import com.ericsson.deviceaccess.coap.basedriver.util.BitOperations;
import com.ericsson.deviceaccess.coap.basedriver.util.CoAPOptionHeaderConverter;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class handles the incoming CoAP Requests and responses. From the
 * functionality point of view, it is assumed that this gateway will not receive
 * requests but only responses to requests it has sent out
 *
 */
public class LocalCoAPEndpoint extends CoAPEndpoint implements
        IncomingCoAPListener {

    private final OutgoingMessageHandler outgoingMessageHandler;
    private final IncomingMessageHandler incomingMessageHandler;

    private final ObservationHandler observationHandler;
    private final BlockwiseTransferHandler blockwiseHandler;

    private final Timer timer;

    // Max block-size defined in the draft-ietf-core-block-07
    private int maxBlockSize;
    private int maxSzx;

    private final HashMap<URI, CachedResponse> incomingResponseCache;
    private final BlockwiseResponseCache ongoingBlockwiseResponses;

    /**
     * Private class that is responsible for caching the received responses. The
     * rules for caching are defined in the core draft.
     */
    private class CachedResponse extends TimerTask {

        private final CoAPResponse cachedResponse;
        private final CoAPRequest originalRequest;

        protected CachedResponse(CoAPResponse cachedResponse,
                CoAPRequest originalRequest) {
            this.cachedResponse = cachedResponse;
            this.originalRequest = originalRequest;
        }

        @Override
        public void run() {
            // cached response has expired, remove
            try {
                URI uri = originalRequest.getUriFromRequest();
                removeCachedResponse(uri);
            } catch (CoAPException e) {
                e.printStackTrace();
            }
        }

        public CoAPResponse getCachedResponse() {
            return this.cachedResponse;
        }

        public CoAPRequest getRequest() {
            return this.originalRequest;
        }
    }

    /**
     * Constructor is protected. A LocalCoAPEndpoint should be instantiated
     * using the CoAPEndpointFactory.
     */
    protected LocalCoAPEndpoint(OutgoingMessageHandler outgoingMessageHandler,
            IncomingMessageHandler incomingMessageHandler, URI uri) {
        super(uri);
        this.outgoingMessageHandler = outgoingMessageHandler;
        this.incomingMessageHandler = incomingMessageHandler;
        this.observationHandler = new ObservationHandler(this);
        this.incomingResponseCache = new HashMap<>();
        this.timer = new Timer();

        this.maxBlockSize = 1024;
        this.maxSzx = 6;
        this.blockwiseHandler = new BlockwiseTransferHandler(this);

        this.ongoingBlockwiseResponses = new BlockwiseResponseCache(10000); /* every 10sec */

    }

    /**
     * This method is called if the maxSzx value is different than 6 in the
     * properties file.
     *
     * @param maxBlockSzx maximum value for szx
     */
    public void setMaxSzx(int maxBlockSzx) {
        this.maxSzx = maxBlockSzx;
        // max block size is 2**(4+szx)
        Double blockSizeDouble = Math.pow(2, (this.maxSzx + 4));
        this.maxBlockSize = blockSizeDouble.intValue();
        this.blockwiseHandler.setMaxSzx(this.maxSzx);
    }

    public int getMaxSzx() {
        return this.maxSzx;
    }

    /**
     * This method will send out the request using the lower layers
     *
     * @param request request to send out
     */
    public void sendRequest(CoAPRequest request) {
        // check if a matching cached response could be found
        try {
            URI destination = request.getUriFromRequest();
            CachedResponse resp = this.incomingResponseCache.get(destination);

            // If no match, move on
            if (resp == null) {

                // TODO are block1 and block2 alternatives?
                boolean block2Request = false;
                boolean block1Request = false;
                if (request.getOptionHeaders(CoAPOptionName.BLOCK2).size() > 0) {
                    block2Request = true;
                }
                if (request.getOptionHeaders(CoAPOptionName.BLOCK1).size() > 0) {
                    block1Request = true;
                }
                // If size of the request is larger than default max size, split
                // into smaller blocks
                if ((request.getPayload() != null && request.getPayload().length > maxBlockSize)
                        || block2Request || block1Request) {

                    int szx = maxSzx;

                    int blockNumber = 0;
                    // need to check for the block number from the request

                    if (block2Request || block1Request) {
                        CoAPOptionHeader h;
                        if (block2Request) {
                            h = (CoAPOptionHeader) request.getOptionHeaders(
                                    CoAPOptionName.BLOCK2).get(0);
                        } else {
                            h = (CoAPOptionHeader) request.getOptionHeaders(
                                    CoAPOptionName.BLOCK1).get(0);
                        }

                        BlockOptionHeader blockOptionHeader = new BlockOptionHeader(
                                h);
                        szx = blockOptionHeader.getSzx();
                        blockNumber = blockOptionHeader.getBlockNumber();
                    }

                    request = this.blockwiseHandler.createBlockwiseRequest(
                            request, blockNumber, szx);
                }
                this.outgoingMessageHandler.send(request, false);

            } else {
                // If the response included a block2 option, do not use cached
                // response (blockwise transfer)
                List<CoAPOptionHeader> block2Headers = resp.getCachedResponse().getOptionHeaders(
                        CoAPOptionName.BLOCK2);
                if (!block2Headers.isEmpty()) {
                    this.outgoingMessageHandler.send(request, false);
                    return;
                }

                /*
                 * For a presented request, a CoAP end-point MUST NOT use a
                 * stored response, unless:
                 *
                 * o the presented request method and that used to obtain the
                 * stored response match,
                 *
                 * o all options match between those in the presented request
                 * and those of the request used to obtain the stored response
                 * (which includes the request URI), except that there is no
                 * need for a match of the Token, Max-Age, or ETag request
                 * option(s), and
                 *
                 * o the stored response is either fresh or successfully
                 * validated as defined below.
                 */
                // Match request codes:
                CoAPRequest originalReq = resp.getRequest();
                int originalCode = originalReq.getCode();

                if (originalCode != request.getCode()) {
                    this.outgoingMessageHandler.send(request, false);
                    return;
                }

                List<CoAPOptionHeader> headers = request.optionsForMatching();
                Collections.sort(headers);
                List<CoAPOptionHeader> originalHeaders = originalReq.optionsForMatching();
                Collections.sort(originalHeaders);

                int toRemove = 0;

                if (headers.size() == originalHeaders.size()) {

                    for (int i = 0; i < headers.size(); i++) {

                        CoAPOptionHeader header = (CoAPOptionHeader) headers
                                .get(i);
                        CoAPOptionHeader originalHeader = (CoAPOptionHeader) originalHeaders
                                .get(i);

                        if (Arrays.equals(originalHeader.getValue(),
                                header.getValue())) {

                            if (originalHeader.getOptionName().trim()
                                    .equals(header.getOptionName().trim())) {
                                toRemove++;
                            }
                        }
                    }
                }

                if (toRemove < headers.size()) {
                    this.outgoingMessageHandler.send(request, false);
                    return;
                }

                /*
                 CoAPActivator.logger
                 .debug("Option headers match, use cached response");
                 */
                CoAPResponse response = resp.getCachedResponse();

                // Remove old headers from the response and replace with updated
                // information
                // Replace token header with the token from the request
                CoAPOptionHeader tokenHeader = request.getTokenHeader();
                CoAPOptionHeader oldTokenHeader = response.getTokenHeader();
                List<CoAPOptionHeader> options = response.getOptionHeaders();

                // Replace max-age option header
                List maxAgeHeader = response
                        .getOptionHeaders(CoAPOptionName.MAX_AGE);

                options.remove(oldTokenHeader);
                options.add(tokenHeader);

                if (maxAgeHeader.size() > 0) {
                    options.removeAll(maxAgeHeader);
                }

                long timeLeft = resp.scheduledExecutionTime()
                        - System.currentTimeMillis();

                int seconds = (int) (timeLeft / 1000) % 60;
                byte[] maxAgeBytes = BitOperations.splitIntToBytes(seconds);

                CoAPOptionHeader newMaxAgeHeader = new CoAPOptionHeader(
                        CoAPOptionName.MAX_AGE, maxAgeBytes);
                options.add(newMaxAgeHeader);

                response.setOptionHeaders(options);
                // Notify listener

                if (response.getMessageType() == CoAPMessageType.CONFIRMABLE
                        || response.getMessageType() == CoAPMessageType.NON_CONFIRMABLE) {

                    // Cache response
                    // Check the response code to check conditions for caching
                    if (observationHandler.isObserved(request
                            .getUriFromRequest())) {
                        observationHandler.handleObserveResponse(request,
                                response);
                        return;
                    }

                    // Notify listener
                    CoAPRequestListener listener = request.getListener();
                    if (listener != null) {
                        listener.separateResponseReceived(response, request);
                    }
                } else if (response.getMessageType() == CoAPMessageType.ACKNOWLEDGEMENT) {

                    if (observationHandler.isObserved(request
                            .getUriFromRequest())) {
                        observationHandler.handleObserveResponse(request,
                                response);
                        return;
                    }

                    // Check for block options
                    List block2 = response
                            .getOptionHeaders(CoAPOptionName.BLOCK2);
                    List block1 = response
                            .getOptionHeaders(CoAPOptionName.BLOCK1);

                    if (block1.size() > 0) {
                        // TODO
                        System.out
                                .println("TODO: Handle block-wise transfer with option Block");
                    } else if (block2.size() > 0) {
                        // TODO
                        System.out
                                .println("TODO: Handle block-wise transfer with option Block2");
                    }

                    CoAPRequestListener listener = request.getListener();
                    if (listener != null) {
                        listener.piggyPackedResponseReceived(response, request);
                    }
                }
            }
        } catch (CoAPException e) {
            e.printStackTrace();
        }
    }

    public void sendResponse(CoAPResponse response) {
        this.sendResponse(response, false);
    }

    /**
     * This method will send out the response using the lower layers
     *
     * @param response response to be sent out
     */
    private void sendResponse(CoAPResponse response, boolean dontCache) {
		// TODO handle different type of requests differently
		/*
         CoAPActivator.logger.debug("sendResponse()");
         */

        // Check the response options
        //     ETag option: If response message has payload but doesn't have ETag option,
        //                  then ETag options is added with the value of MD5(payload).
        //     Token option: exist, or not. If not, copy later
        //
        byte[] payload = response.getPayload();

        boolean hasEtagOption = false;
        boolean hasTokenOption = false;
        for (CoAPOptionHeader optionHeader : response.getOptionHeaders()) {
            String optionName = optionHeader.getOptionName();
            if (CoAPOptionName.ETAG.getName().equals(optionName)) {
                hasEtagOption = true;
            } else if (CoAPOptionName.TOKEN.getName().equals(optionName)) {
                hasTokenOption = true;
            }
        }

        // Add ETag option if payload exists
        if ((payload != null) && (payload.length > 0) && !hasEtagOption) {
            MessageDigest md5;
            try {
                md5 = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("MD5 not supported", e);
            }
            CoAPOptionHeader etagOptionHeader = new CoAPOptionHeader(CoAPOptionName.ETAG, md5.digest(payload));
            response.addOptionHeader(etagOptionHeader);
        }

        // Check the request options
        //     Block2 Option: to check the requested block size and number
        //     Token option: to copy the value of request
        int szx = this.getMaxSzx();
        int blockNumber = 0;
        boolean hasBlock2Option = false;

        CoAPRequest request = this.incomingMessageHandler.getIncomingRequest(response);
        if (request != null) {
            for (CoAPOptionHeader optionHeader : request.getOptionHeaders()) {
                String optionName = optionHeader.getOptionName();
                if (CoAPOptionName.BLOCK2.getName().equals(optionName)) {
                    hasBlock2Option = true;
                    try {
                        BlockOptionHeader block2OptionHeader = new BlockOptionHeader(optionHeader);
                        szx = block2OptionHeader.getSzx();
                        blockNumber = block2OptionHeader.getBlockNumber();
                    } catch (CoAPException e) {
                        // throw new RuntimeException("Invalid Block2 option", e);
                        // XXX: Move on
                    }
                } else if (CoAPOptionName.TOKEN.getName().equals(optionName)) {
                    if (!hasTokenOption) {
                        CoAPOptionHeader tokenOptionHeader = new CoAPOptionHeader(CoAPOptionName.TOKEN, optionHeader.getValue());
                        response.addOptionHeader(tokenOptionHeader);
                    }
                }
            }
        }

        /* cache payload if long enough */
        if ((!dontCache) && (request != null) && (!hasBlock2Option) && ((payload != null) && (payload.length > CoAPUtil.getBlockSize(szx)))) {
            /*
             CoAPActivator.logger.debug("Cache blockwise response: ");
             */
            try {
                this.ongoingBlockwiseResponses.put(request, response);
                this.ongoingBlockwiseResponses.updateTimer(request);

            } catch (CoAPException e) {
                e.printStackTrace();
            }
        }

        if (hasBlock2Option || ((payload != null) && (payload.length > CoAPUtil.getBlockSize(szx)))) {
            response = this.blockwiseHandler.createBlockwiseResponse(response, blockNumber, szx);
        }
        this.outgoingMessageHandler.send(response, false);
    }

    /**
     * Handle a received request. Handling of requests is lower priority for the
     * gateway functionality.
     *
     * @param request incoming request
     */
    @Override
    public void handleRequest(CoAPRequest request) {
		// TODO handle different type of requests differently
		/*
         CoAPActivator.logger.debug("handleRequest() : Received ["
         + request.getMessageType().getName() + "] request");

         */

        // look up blockwise response cache if Request is GET and has Block2 option
        int code = request.getCode();
        boolean hasBlock2Option = (request.getOptionHeaders(CoAPOptionName.BLOCK2).size() > 0);
        if ((code == CoAPMethodCode.GET.ordinal()) && (hasBlock2Option)) {
            try {
                SessionData sessionData = this.ongoingBlockwiseResponses.get(request);
                if (sessionData != null) {
                    this.ongoingBlockwiseResponses.updateTimer(request);

                    byte[] payload = sessionData.getPayload();
                    if (payload != null) {
                        /*
                         CoAPActivator.logger.debug("Cached blockwise response found");
                         */
                        CoAPMessageType msgType
                                = ((request.getMessageType() == CoAPMessageType.CONFIRMABLE)) ? CoAPMessageType.ACKNOWLEDGEMENT
                                : (((request.getMessageType() == CoAPMessageType.NON_CONFIRMABLE)) ? CoAPMessageType.NON_CONFIRMABLE : null);
                        if (msgType != null) {
                            CoAPResponse response = new CoAPResponse(
                                    1, /* version */
                                    msgType,
                                    sessionData.getResponseCode(), request.getMessageId());
                            response.setPayload(payload);
                            response.setSocketAddress(request.getSocketAddress());
                            this.sendResponse(response, true);
                            return;
                        }
                    }
                }
            } catch (CoAPException e) {
                e.printStackTrace();
            }
        }

        try {
            if (request.getUriFromRequest().getPath().startsWith(WELLKNOWN_CORE)
                    && request.getCode() == 1) {
                this.replyToResourceDiscovery(request);
                return;
            }
        } catch (CoAPException e) {
            e.printStackTrace();
            this.replyWithNotImplemented(request);
            return;
        }

        // request type can be confirmable or non-confirmable
        if (request.getMessageType() == CoAPMessageType.CONFIRMABLE) {

            // Reply for now with a not implemented response code
            // if no listeners are found.
            Object[] services = CoAPActivator.incomingCoAPTracker.getServices();
            if (services != null) {
                for (Object s : services) {
                    ((IncomingCoAPRequestListener) s).incomingRequestReceived(request);
                }
            } else {
                this.replyWithNotImplemented(request);
            }
        } else if (request.getMessageType().equals(
                CoAPMessageType.NON_CONFIRMABLE)) {
            Object[] services = CoAPActivator.incomingCoAPTracker.getServices();
            if (services != null) {
                for (Object s : services) {
                    ((IncomingCoAPRequestListener) s).incomingRequestReceived(request);
                }
            }
        }
    }

    /**
     * Called by the IncomingMessageHandler when a new incoming response is
     * received
     *
     * @param received response
     * @param original request that this request matches to
     */
    @Override
    public void handleResponse(CoAPResponse resp) throws CoAPException {
        /*
         CoAPActivator.logger.info("CoAP response of type ["
         + resp.getMessageType().toString() + "] received");
         */

        List<CoAPOptionHeader> headers = resp.getOptionHeaders();

        headers.stream().forEach(h -> {
            CoAPOptionHeaderConverter converter = new CoAPOptionHeaderConverter();
            /*
             String headerValue = "";
             headerValue = converter.convertOptionHeaderToString(h);
             CoAPActivator.logger.debug("CoAPOptionHeader ["
             + h.getOptionName() + "] in the response with value ["
             + headerValue + "]");
             */
        });

        if (resp.getMessageType() == CoAPMessageType.RESET) {
            this.handleReset(resp);
        } else if (resp.getMessageType() == CoAPMessageType.CONFIRMABLE
                || resp.getMessageType() == CoAPMessageType.NON_CONFIRMABLE) {
            this.handleConAndNon(resp);

        } else if (resp.getMessageType() == CoAPMessageType.ACKNOWLEDGEMENT) {
            this.handleAck(resp);
        }
    }

    public CoAPRequest createCoAPRequest(CoAPMessageType messageType,
            int methodCode, InetSocketAddress address, URI uri,
            CoAPOptionHeader tokenHeader) throws CoAPException {
        /*
         CoAPActivator.logger.debug("Create CoAP request");
         */

        int messageId = this.outgoingMessageHandler.generateMessageId();
        CoAPRequest req = new CoAPRequest(messageType, methodCode, messageId);
        req.setUri(uri);
        req.setSocketAddress(address);

        if (tokenHeader != null) {
            req.addOptionHeader(tokenHeader);
        }
        String path = "";
        // Add host, port and path options
        String host = uri.getHost();
        if (uri.getPath() != null) {
            path = uri.getPath();
        }

        CoAPOptionHeader hostOpt = new CoAPOptionHeader(
                CoAPOptionName.URI_HOST, host.getBytes());
        req.addOptionHeader(hostOpt);

        if (uri.getPort() != -1) {
            Integer port = uri.getPort();

            byte[] portBytes = BitOperations.splitIntToBytes(uri.getPort());

            short shortInt = BitOperations.mergeBytesToShort(portBytes[2],
                    portBytes[3]);

            int unsignedShort = shortInt & 0xFFFF;

            CoAPOptionHeader portOpt = new CoAPOptionHeader(
                    CoAPOptionName.URI_PORT, portBytes);
            req.addOptionHeader(portOpt);
        }

        if (path != null && !path.equals("")) {
            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            // The Uri-Path header contains one segment of the absolute path
            // Thus, divide into several headers if contains "/"
            LinkedList pathParts = new LinkedList();

            int index = path.indexOf("/");
            while (index > -1) {
                String str = path.substring(0, index);
                pathParts.add(str);
                path = path.substring(index + 1);
                index = path.indexOf("/");
            }

            pathParts.add(path);

            Iterator it = pathParts.iterator();
            while (it.hasNext()) {
                String pathPart = (String) it.next();
                CoAPOptionHeader pathOpt = new CoAPOptionHeader(
                        CoAPOptionName.URI_PATH, pathPart.getBytes());
                req.addOptionHeader(pathOpt);
            }
        }
        return req;
    }

    /**
     * Get the OutgoingMessageHandler instance
     *
     * @return OutgoingMessageHandler instance used by this endpoint
     */
    @Override
    public OutgoingMessageHandler getOutgoingMessageHandler() {
        return this.outgoingMessageHandler;
    }

    /**
     * Get the IncomingMessageHandler instance
     *
     * @return IncomingMessageHandler instance used by this endpoint
     */
    @Override
    public IncomingMessageHandler getIncomingMessageHandler() {
        return this.incomingMessageHandler;
    }

    /**
     * Use this method if there doesn't exist request with the same identifier
     * (that's a separate response is used)
     *
     * @param resp response that matches the given request
     * @return request that was matched based on the token
     */
    private CoAPRequest matchBasedOnTokens(CoAPResponse resp) {

        CoAPOptionHeader tokenHeader = resp.getTokenHeader();
        if (tokenHeader == null) {
            return null;
        }
        String token = new String(tokenHeader.getValue());

        HashMap requests = outgoingMessageHandler.getOutgoingRequests();
        Iterator it = requests.values().iterator();
        try {
            while (it.hasNext()) {
                CoAPRequest req = (CoAPRequest) it.next();
                if (req.getTokenHeader() == null) {
                    return null;
                }
                String reqToken = new String(req.getTokenHeader().getValue());

                if (token.equals(reqToken)) {
                    return req;
                }
            }
            // If the concurrent modification exception happens, just try
            // again.. :)
        } catch (ConcurrentModificationException e) {
            matchBasedOnTokens(resp);
        }

        return null;
    }

    /**
     * Helper method to match a response to a request based on the message
     * identifiers
     *
     * @param resp response to match to
     * @return request that matches the given response
     */
    private CoAPRequest matchBasedOnIdentifier(CoAPResponse resp) {
        return (CoAPRequest) this.outgoingMessageHandler.getOutgoingRequests()
                .get(resp.getIdentifier());
    }

    private void handleReset(CoAPResponse resp) throws CoAPException {
        CoAPRequest originalRequest = this.matchBasedOnIdentifier(resp);
        if (originalRequest == null) {
            /*
             CoAPActivator.logger.warn("[RESET] Matching request was not found");
             */
            return;
        }

        if (resp.isCacheable()) {
            this.cacheResponse(resp, originalRequest);
        }

        CoAPRequestListener listener = originalRequest.getListener();

        if (listener != null) {
            listener.resetResponseReceived(resp, originalRequest);
        }
    }

    /**
     * This method handles incoming ACK responses (empty ack, normal ack and
     * acks related to observation relationships)
     *
     * @param resp response that is an acknowledgement
     */
    private void handleAck(CoAPResponse resp) throws CoAPException {
        CoAPRequest originalRequest = this.matchBasedOnIdentifier(resp);
        if (originalRequest == null) {
            // Reply with a RESET message??
			/*
             CoAPActivator.logger.warn("[ACK] Matching request was not found");
             */
            return;
        }

        // Handle empty ack
        if (resp.getCode() == 0) {
            /*
             CoAPActivator.logger.debug("An empty ACK received");
             */

            CoAPRequestListener listener = originalRequest.getListener();
            if (listener != null) {
                listener.emptyAckReceived(resp, originalRequest);
            }

            return;
        }

        // Check for block options
        List block2 = resp.getOptionHeaders(CoAPOptionName.BLOCK2);
        List block1 = resp.getOptionHeaders(CoAPOptionName.BLOCK1);

        // Cache response
        // Check the response code to check conditions for caching, do not also
        // cache is there's a block option (either block1 or block2 present)
        if (resp.isCacheable() && block2.isEmpty() && block1.isEmpty()) {
            this.cacheResponse(resp, originalRequest);
        }

        // Do not return from these methods, use the normal callback methods
        if (block1.size() > 0) {
            CoAPRequest nextBlock = this.blockwiseHandler
                    .block1OptionResponseReceived(resp, originalRequest);
            if (nextBlock != null) {
                outgoingMessageHandler.send(nextBlock, false);
            }
        } else if (block2.size() > 0) {
            CoAPRequest nextBlock = this.blockwiseHandler.block2OptionReceived(
                    resp, originalRequest);

            // Send the next block
            if (nextBlock != null) {
                outgoingMessageHandler.send(nextBlock, false);
            }
        }

        // if the message is related to an observed resource or the resource
        // contains observe header
        if (observationHandler.isObserved(originalRequest.getUriFromRequest())) {
            observationHandler.handleObserveResponse(originalRequest, resp);
        } else {
            CoAPRequestListener listener = originalRequest.getListener();

            if (listener != null) {
                listener.piggyPackedResponseReceived(resp, originalRequest);
            }
        }
    }

    /**
     * This method handles incoming CON responses
     *
     * @param resp response to handle
     */
    private void handleConAndNon(CoAPResponse resp) throws CoAPException {

        CoAPRequest originalRequest = null;
        // Confirmable messages are identified based on tokens
        try {
            originalRequest = this.matchBasedOnTokens(resp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (originalRequest == null) {
            /*
             CoAPActivator.logger.warn("[ConAndNon] Matching request was not found");
             */

            // If no matching request can be found for a confirmable response,
            // reply with a reset
            if (resp.getMessageType() == CoAPMessageType.CONFIRMABLE) {
                this.sendResponse(resp.createReset());
            }
            return;
        }

        // Check for block options
        List block2 = resp.getOptionHeaders(CoAPOptionName.BLOCK2);
        List block1 = resp.getOptionHeaders(CoAPOptionName.BLOCK1);

        // Cache response
        // Check the response code to check conditions for caching, do not also
        // cache is there's a block option (either block1 or block2 present)
        if (resp.isCacheable() && block2.isEmpty() && block1.isEmpty()) {
            this.cacheResponse(resp, originalRequest);
        }

        // Do not return from these methods, use the normal callback methods
        if (block1.size() > 0) {
            CoAPRequest nextBlock = this.blockwiseHandler
                    .block1OptionResponseReceived(resp, originalRequest);
            if (nextBlock != null) {
                outgoingMessageHandler.send(nextBlock, false);
            }
        } else if (block2.size() > 0) {
            CoAPRequest nextBlock = this.blockwiseHandler.block2OptionReceived(
                    resp, originalRequest);
            if (nextBlock != null) {
                outgoingMessageHandler.send(nextBlock, false);
            }
        }
        // if the message is related to an observed resource or the resource
        // contains observe header
        if (observationHandler.isObserved(originalRequest.getUriFromRequest())) {
            observationHandler.handleObserveResponse(originalRequest, resp);
        } else {
            CoAPRequestListener listener = originalRequest.getListener();

            if (listener != null) {
                listener.separateResponseReceived(resp, originalRequest);
            }
        }

    }

    /**
     * This method creates an observation relationship with the resource
     * identified by the given URI and the observer. If there already exist a
     * relationship with the resource, the same relationship will be used.
     *
     * @param uri URI to the resource
     * @param observer observer who wants to receive the notification
     * @return CoAPResource matching the given URI
     * @throws CoAPException
     */
    public CoAPResource createObservationRelationship(URI uri,
            CoAPResourceObserver observer) throws CoAPException {
        return this.observationHandler.createObservationRelationship(uri,
                observer);
    }

    /**
     * Terminates an observation relationship with the given resource and
     * observer. If the resource does not have further observers, the whole
     * relationship will be terminated.
     *
     * @param resource CoAP resource from which the relationship is to be
     * terminated
     * @param observer observer who wants to terminate the subscription
     * @throws CoAPException
     */
    public boolean terminateObservationRelationship(CoAPResource resource,
            CoAPResourceObserver observer) throws CoAPException {
        return this.observationHandler.terminateObservationRelationship(
                resource, observer);
    }

    /**
     * This method is called when the timertask expires for a cached response.
     * The response will be removed from the cache.
     *
     * @param uri URI identifying the response to be removed
     */
    protected synchronized void removeCachedResponse(URI uri) {
        /*
         CoAPActivator.logger.debug("Cached response for resource at ["
         + uri.toString() + "] expired, remove from cache");
         */
        this.incomingResponseCache.remove(uri);
    }

    /**
     * This private method caches incoming responses for the time defined in the
     * max-age option header. By default Max-age option is 60 seconds.
     *
     * @param resp received response
     * @param originalRequest original request that was sent out
     * @throws CoAPException
     */
    private void cacheResponse(CoAPResponse resp, CoAPRequest originalRequest)
            throws CoAPException {

        // Cached response for the time defined by the max-age option.
        long maxAge = resp.getMaxAge();
        CachedResponse task = new CachedResponse(resp, originalRequest);

        /*
         CoAPActivator.logger.debug("Cache response for resource at ["
         + originalRequest.getUriFromRequest().toString()
         + "] for [" + maxAge + "] seconds");
         */
        // Cache response based on the max-age option
        this.incomingResponseCache.put(originalRequest.getUriFromRequest(),
                task);
        try {
            timer.schedule(task, maxAge * 1000);
        } catch (IllegalArgumentException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method cancels the timer and the scheduled tasks. It's needed when
     * the bundle is stopped
     */
    public void stopService() {
        timer.cancel();
        this.observationHandler.stopService();
        if (this.ongoingBlockwiseResponses != null) {
            this.ongoingBlockwiseResponses.cleanup();
        }
    }

    private void replyToResourceDiscovery(CoAPRequest req) {

        String payloadStr = "";
        CoAPMessageType messageType = this.getResponseType(req);

        // If no uri-query parameters present for rd service or mp service,
        // reply from here
        List queryHeaders = req.getOptionHeaders(CoAPOptionName.URI_QUERY);
        CoAPOptionHeader tokenHeader = req.getTokenHeader();

        // TODO how to handle these properly without enum etc..
        Iterator headerIt = queryHeaders.iterator();
        HashMap attributes = new HashMap();

        // Read the attributes from the incoming request to a hasmhap
        while (headerIt.hasNext()) {
            CoAPOptionHeader h = (CoAPOptionHeader) headerIt.next();
            String headerValue = new String(h.getValue());
            int index = headerValue.indexOf("=");
            if (index > 0) {
                String attributeName = headerValue.substring(0, index);
                String attributeValue = headerValue.substring(index + 1,
                        headerValue.length());
                attributes.put(attributeName, attributeValue);
            } else {
                CoAPResponse resp = new CoAPResponse(1, messageType,
                        CoAPResponseCode.BAD_REQUEST.getNo(),
                        req.getMessageId());
                // resp.addOptionHeader(h);
                if (tokenHeader != null) {
                    resp.addOptionHeader(tokenHeader);
                }
                resp.setSocketAddress(req.getSocketAddress());
                this.outgoingMessageHandler.send(resp, false);
                return;
            }
        }

        // Compare the existing CoAP resources at the gateway to the request
        // query parameters
        try {

            if (getResources().values() != null) {
                for (CoAPResource res : getResources().values()) {
                    boolean matches = res.matchWithQueryParams(attributes);
                    if (matches) {
                        String linkFormat = res
                                .getLinkFormatPresentation(false);
                        payloadStr += linkFormat + ",";
                    }
                }
            }
            CoAPResponse resp;
            if (!payloadStr.equals("")) {

                // remove the last comma
                payloadStr = payloadStr.substring(0, (payloadStr.length() - 1));

                short contentTypeId = 40;
                byte[] contentTypeBytes = BitOperations
                        .splitShortToBytes(contentTypeId);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                outputStream.write(contentTypeBytes[1]);

                CoAPOptionHeader h = new CoAPOptionHeader(
                        CoAPOptionName.CONTENT_TYPE, outputStream.toByteArray());
                resp = new CoAPResponse(1, messageType,
                        CoAPResponseCode.CONTENT.getNo(), req.getMessageId());
                if (tokenHeader != null) {
                    resp.addOptionHeader(tokenHeader);
                }

                byte[] payload = payloadStr.getBytes();
                resp.setPayload(payload);
                resp.addOptionHeader(h);
            } else {
                resp = new CoAPResponse(1, messageType,
                        CoAPResponseCode.NOT_FOUND.getNo(), req.getMessageId());
                if (tokenHeader != null) {
                    resp.addOptionHeader(tokenHeader);
                }
            }

            resp.setSocketAddress(req.getSocketAddress());
            this.outgoingMessageHandler.send(resp, false);
        } catch (CoAPException e) {
            e.printStackTrace();
        }
    }

    /**
     * private helper method to justify the correct response type for a message
     *
     * @param req
     * @return
     */
    private CoAPMessageType getResponseType(CoAPRequest req) {
        CoAPMessageType type;
        if (req.getMessageType() == CoAPMessageType.CONFIRMABLE) {
            type = CoAPMessageType.ACKNOWLEDGEMENT;
        } else if (req.getMessageType() == CoAPMessageType.NON_CONFIRMABLE) {
            type = CoAPMessageType.NON_CONFIRMABLE;
        } else {
            // / TODO check correct response type for other cases
            type = req.getMessageType();
        }
        return type;
    }

    private void replyWithNotImplemented(CoAPRequest request) {
        int messageId = request.getMessageId();
        CoAPResponse resp = new CoAPResponse(1, CoAPMessageType.CONFIRMABLE,
                CoAPResponseCode.NOT_IMPLEMENTED.getNo(), messageId);

        for (CoAPOptionHeader header : request.getOptionHeaders()) {
            if (header.getOptionName().equals(CoAPOptionName.TOKEN.getName())) {
                CoAPOptionHeader respHeader = new CoAPOptionHeader(
                        CoAPOptionName.TOKEN, header.getValue());
                resp.addOptionHeader(respHeader);
            }
        }

        resp.setSocketAddress(request.getSocketAddress());
        // reply directly from here if no listeners are found
        resp = new CoAPResponse(1, CoAPMessageType.CONFIRMABLE,
                CoAPResponseCode.NOT_FOUND.getNo(), messageId);
        resp.setSocketAddress(request.getSocketAddress());
        this.outgoingMessageHandler.send(resp, false);
    }
}
