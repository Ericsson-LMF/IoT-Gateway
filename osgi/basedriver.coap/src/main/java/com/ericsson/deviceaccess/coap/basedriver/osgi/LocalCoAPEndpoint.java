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

import com.ericsson.common.util.BitUtil;
import com.ericsson.deviceaccess.coap.basedriver.api.CoAPActivator;
import com.ericsson.deviceaccess.coap.basedriver.api.CoAPEndpoint;
import com.ericsson.deviceaccess.coap.basedriver.api.CoAPException;
import com.ericsson.deviceaccess.coap.basedriver.api.IncomingCoAPRequestListener;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPMessage.CoAPMessageType;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionHeader;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName;
import static com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName.BLOCK1;
import static com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName.BLOCK2;
import static com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName.CONTENT_FORMAT;
import static com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName.ETAG;
import static com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName.MAX_AGE;
import static com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName.URI_HOST;
import static com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName.URI_PATH;
import static com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName.URI_PORT;
import static com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName.URI_QUERY;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPRequest;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPRequestCode;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPRequestListener;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPResponse;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPResponseCode;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPUtil;
import com.ericsson.deviceaccess.coap.basedriver.api.resources.CoAPResource;
import com.ericsson.deviceaccess.coap.basedriver.api.resources.CoAPResourceObserver;
import com.ericsson.deviceaccess.coap.basedriver.osgi.BlockwiseResponseCache.SessionData;
import com.ericsson.deviceaccess.coap.basedriver.util.CoAPOptionHeaderConverter;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class handles the incoming CoAP Requests and responses. From the
 * functionality point of view, it is assumed that this gateway will not receive
 * requests but only responses to requests it has sent out
 *
 */
public class LocalCoAPEndpoint extends CoAPEndpoint implements
        IncomingCoAPListener {

    private final OutgoingMessageHandler outHandler;
    private final IncomingMessageHandler inHandler;

    private final ObservationHandler obsHandler;
    private final BlockwiseTransferHandler blockHandler;

    private final Timer timer;

    // Max block-size defined in the draft-ietf-core-block-07
    private int maxBlockSize;
    private int maxSzx;

    private final Map<URI, CachedResponse> inResponseCache;
    private final BlockwiseResponseCache outBlockCache;

    /**
     * Constructor is protected. A LocalCoAPEndpoint should be instantiated
     * using the CoAPEndpointFactory.
     *
     * @param outgoingMessageHandler
     * @param incomingMessageHandler
     * @param uri
     */
    protected LocalCoAPEndpoint(OutgoingMessageHandler outgoingMessageHandler,
            IncomingMessageHandler incomingMessageHandler, URI uri) {
        super(uri);
        this.outHandler = outgoingMessageHandler;
        this.inHandler = incomingMessageHandler;
        this.obsHandler = new ObservationHandler(this);
        this.inResponseCache = new ConcurrentHashMap<>();
        this.timer = new Timer();

        this.maxBlockSize = 1024;
        this.maxSzx = 6;
        this.blockHandler = new BlockwiseTransferHandler(this);

        this.outBlockCache = new BlockwiseResponseCache(10000); /* every 10sec */

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
        this.maxBlockSize = CoAPUtil.getBlockSize(maxSzx).intValue();
        this.blockHandler.setMaxSzx(this.maxSzx);
    }

    public int getMaxSzx() {
        return maxSzx;
    }

    /**
     * This method will send out the request using the lower layers
     *
     * @param request request to send out
     */
    public void sendRequest(CoAPRequest request) {
        // check if a matching cached response could be found
        try {
            CachedResponse resp = inResponseCache.get(request.getUriFromRequest());
            // If no match, move on
            if (resp == null) {

                // TODO are block1 and block2 alternatives?
                boolean block2Request = !request.getOptionHeaders(BLOCK2).isEmpty();
                boolean block1Request = !request.getOptionHeaders(BLOCK1).isEmpty();
                // If size of the request is larger than default max size, split
                // into smaller blocks
                byte[] payload = request.getPayload();
                if (payload != null && payload.length > maxBlockSize || block2Request || block1Request) {

                    int szx = maxSzx;
                    int blockNumber = 0;
                    // need to check for the block number from the request

                    if (block2Request || block1Request) {
                        CoAPOptionHeader header;
                        if (block2Request) {
                            header = request.getOptionHeaders(BLOCK2).get(0);
                        } else {
                            header = request.getOptionHeaders(BLOCK1).get(0);
                        }
                        BlockOptionHeader blockOptionHeader = new BlockOptionHeader(header);
                        szx = blockOptionHeader.getSzx();
                        blockNumber = blockOptionHeader.getBlockNumber();
                    }
                    request = blockHandler.createBlockwiseRequest(request, blockNumber, szx);
                }
                outHandler.send(request, false);
            } else {
                // If the response included a block2 option, do not use cached
                // response (blockwise transfer)
                if (!resp.getCachedResponse().getOptionHeaders(BLOCK2).isEmpty()) {
                    outHandler.send(request, false);
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
                CoAPRequestCode originalCode = originalReq.getCode();

                if (originalCode != request.getCode()) {
                    outHandler.send(request, false);
                    return;
                }

                if (!request.optionsForMatching().equals(originalReq.optionsForMatching())) {
                    outHandler.send(request, false);
                    return;
                }

                //CoAPActivator.logger.debug("Option headers match, use cached response");
                CoAPResponse response = resp.getCachedResponse();

                // Remove old headers from the response and replace with updated
                // information
                // Replace token header with the token from the request
                response.setToken(request.getToken());

                // Replace max-age option header
                List<CoAPOptionHeader> options = response.getOptionHeaders();

                options.removeAll(response.getOptionHeaders(MAX_AGE));

                long timeLeft = resp.scheduledExecutionTime() - System.currentTimeMillis();

                int seconds = (int) (timeLeft / 1000) % 60;
                byte[] maxAgeBytes = BitUtil.splitIntToBytes(seconds);

                options.add(new CoAPOptionHeader(MAX_AGE, maxAgeBytes));
                // Notify listener
                CoAPMessageType type = response.getMessageType();
                if (type == CoAPMessageType.CONFIRMABLE || type == CoAPMessageType.NON_CONFIRMABLE) {
                    // Cache response
                    // Check the response code to check conditions for caching
                    if (obsHandler.isObserved(request.getUriFromRequest())) {
                        obsHandler.handleObserveResponse(request, response);
                        return;
                    }
                    // Notify listener
                    CoAPRequestListener listener = request.getListener();
                    if (listener != null) {
                        listener.separateResponseReceived(response, request);
                    }
                } else if (type == CoAPMessageType.ACKNOWLEDGEMENT) {
                    if (obsHandler.isObserved(request.getUriFromRequest())) {
                        obsHandler.handleObserveResponse(request, response);
                        return;
                    }
                    // Check for block options
                    List<CoAPOptionHeader> block1 = response.getOptionHeaders(BLOCK1);
                    List<CoAPOptionHeader> block2 = response.getOptionHeaders(BLOCK2);

                    if (!block1.isEmpty()) {
                        // TODO
                        System.out.println("TODO: Handle block-wise transfer with option Block");
                    } else if (!block2.isEmpty()) {
                        // TODO
                        System.out.println("TODO: Handle block-wise transfer with option Block2");
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
        sendResponse(response, false);
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

        boolean hasTokenOption = response.getToken() != null;
        boolean hasEtagOption = response.getOptionHeaders()
                .stream()
                .map(h -> h.getOptionName())
                .filter(n -> n == ETAG)
                .findAny()
                .isPresent();

        // Add ETag option if payload exists
        if (payload != null && payload.length > 0 && !hasEtagOption) {
            MessageDigest md5;
            try {
                md5 = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("MD5 not supported", e);
            }
            response.addOptionHeader(new CoAPOptionHeader(ETAG, md5.digest(payload)));
        }

        // Check the request options
        //     Block2 Option: to check the requested block size and number
        //     Token option: to copy the value of request
        int szx = this.getMaxSzx();
        int blockNumber = 0;
        boolean hasBlock2Option = false;

        CoAPRequest request = this.inHandler.getIncomingRequest(response);
        if (request != null) {
            for (CoAPOptionHeader optionHeader : request.getOptionHeaders()) {
                CoAPOptionName optionName = optionHeader.getOptionName();
                if (BLOCK2 == optionName) {
                    hasBlock2Option = true;
                    try {
                        BlockOptionHeader block2OptionHeader = new BlockOptionHeader(optionHeader);
                        szx = block2OptionHeader.getSzx();
                        blockNumber = block2OptionHeader.getBlockNumber();
                    } catch (CoAPException e) {
                        // throw new RuntimeException("Invalid Block2 option", e);
                        // XXX: Move on
                    }
                }
            }
            byte[] token = request.getToken();
            if (!hasTokenOption) {
                response.setToken(token);
            }
        }

        /* cache payload if long enough */
        if (!dontCache && request != null && !hasBlock2Option && payload != null && payload.length > CoAPUtil.getBlockSize(szx)) {
            /*
             CoAPActivator.logger.debug("Cache blockwise response: ");
             */
            try {
                outBlockCache.put(request, response);
                outBlockCache.updateTimer(request);
            } catch (CoAPException e) {
                e.printStackTrace();
            }
        }

        if (hasBlock2Option || payload != null && payload.length > CoAPUtil.getBlockSize(szx)) {
            response = blockHandler.createBlockwiseResponse(response, blockNumber, szx);
        }
        outHandler.send(response, false);
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
        //CoAPActivator.logger.debug("handleRequest() : Received [" + request.getMessageType().getName() + "] request");

        // look up blockwise response cache if Request is GET and has Block2 option
        CoAPRequestCode code = request.getCode();
        boolean hasBlock2Option = !request.getOptionHeaders(BLOCK2).isEmpty();
        if (code == CoAPRequestCode.GET && hasBlock2Option) {
            try {
                SessionData sessionData = outBlockCache.get(request);
                if (sessionData != null) {
                    outBlockCache.updateTimer(request);

                    byte[] payload = sessionData.getPayload();
                    if (payload != null) {
                        //CoAPActivator.logger.debug("Cached blockwise response found");
                        CoAPMessageType type = request.getMessageType();
                        //TODO: Replace with getResponseType?
                        CoAPMessageType msgType
                                = type == CoAPMessageType.CONFIRMABLE ? CoAPMessageType.ACKNOWLEDGEMENT
                                : type == CoAPMessageType.NON_CONFIRMABLE ? CoAPMessageType.NON_CONFIRMABLE : null;
                        if (msgType != null) {
                            CoAPResponse response = new CoAPResponse(
                                    1, /* version */
                                    msgType,
                                    sessionData.getResponseCode(), request.getMessageId());
                            response.setPayload(payload);
                            response.setSocketAddress(request.getSocketAddress());
                            sendResponse(response, true);
                            return;
                        }
                    }
                }
            } catch (CoAPException e) {
                e.printStackTrace();
            }
        }

        try {
            if (request.getUriFromRequest().getPath().startsWith(WELLKNOWN_CORE) && request.getCode() == CoAPRequestCode.GET) {
                replyToResourceDiscovery(request);
                return;
            }
        } catch (CoAPException e) {
            e.printStackTrace();
            replyWithNotImplemented(request);
            return;
        }

        // request type can be confirmable or non-confirmable
        if (request.getMessageType() == CoAPMessageType.CONFIRMABLE) {
            // Reply for now with a not implemented response code if no listeners are found.
            Object[] services = CoAPActivator.incomingCoAPTracker.getServices();
            if (services != null) {
                for (Object s : services) {
                    ((IncomingCoAPRequestListener) s).incomingRequestReceived(request);
                }
            } else {
                replyWithNotImplemented(request);
            }
        } else if (request.getMessageType() == CoAPMessageType.NON_CONFIRMABLE) {
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
     * @param resp response
     * @throws com.ericsson.deviceaccess.coap.basedriver.api.CoAPException
     */
    @Override
    public void handleResponse(CoAPResponse resp) throws CoAPException {
        //CoAPActivator.logger.info("CoAP response of type [" + resp.getMessageType().toString() + "] received");
        resp.getOptionHeaders().forEach(h -> {
            CoAPOptionHeaderConverter converter = new CoAPOptionHeaderConverter();
            /*
             String headerValue = "";
             headerValue = converter.convertOptionHeaderToString(h);
             CoAPActivator.logger.debug("CoAPOptionHeader ["
             + h.getOptionName() + "] in the response with value ["
             + headerValue + "]");
             */
        });

        CoAPMessageType type = resp.getMessageType();
        if (type == CoAPMessageType.RESET) {
            handleReset(resp);
        } else if (type == CoAPMessageType.CONFIRMABLE
                || type == CoAPMessageType.NON_CONFIRMABLE) {
            handleConAndNon(resp);
        } else if (type == CoAPMessageType.ACKNOWLEDGEMENT) {
            handleAck(resp);
        }
    }

    public CoAPRequest createCoAPRequest(CoAPMessageType messageType,
            CoAPRequestCode methodCode, InetSocketAddress address, URI uri,
            byte[] tokenHeader) throws CoAPException {
        //CoAPActivator.logger.debug("Create CoAP request");
        int messageId = outHandler.generateMessageId();
        CoAPRequest req = new CoAPRequest(messageType, methodCode, messageId, tokenHeader);
        req.setUri(uri);
        req.setSocketAddress(address);

        String path = "";
        // Add host, port and path options
        String host = uri.getHost();
        if (uri.getPath() != null) {
            path = uri.getPath();
        }

        req.addOptionHeader(new CoAPOptionHeader(URI_HOST, host.getBytes()));

        if (uri.getPort() != -1) {
            byte[] portBytes = BitUtil.splitIntToBytes(uri.getPort());
            req.addOptionHeader(new CoAPOptionHeader(URI_PORT, portBytes));
        }

        if (!path.isEmpty()) {
            // The Uri-Path header contains one segment of the absolute path
            // Thus, divide into several headers if contains "/"
            Arrays.stream(path.split("/"))
                    .filter(p -> !p.isEmpty())
                    .map(p -> p.getBytes(StandardCharsets.UTF_8))
                    .forEach(p -> req.addOptionHeader(new CoAPOptionHeader(URI_PATH, p)));
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
        return outHandler;
    }

    /**
     * Get the IncomingMessageHandler instance
     *
     * @return IncomingMessageHandler instance used by this endpoint
     */
    @Override
    public IncomingMessageHandler getIncomingMessageHandler() {
        return inHandler;
    }

    /**
     * Use this method if there doesn't exist request with the same identifier
     * (that's a separate response is used)
     *
     * @param resp response that matches the given request
     * @return request that was matched based on the token
     */
    private Optional<CoAPRequest> matchBasedOnTokens(CoAPResponse resp) {
        byte[] tokenHeader = resp.getToken();
        if (tokenHeader == null) {
            return Optional.empty();
        }
        String token = new String(tokenHeader, StandardCharsets.UTF_8);
        return outHandler.getOutgoingRequests()
                .values()
                .stream()
                .filter(req -> req != null)
                .filter(req -> token.equals(new String(req.getToken(), StandardCharsets.UTF_8)))
                .findAny();
    }

    /**
     * Helper method to match a response to a request based on the message
     * identifiers
     *
     * @param resp response to match to
     * @return request that matches the given response
     */
    private CoAPRequest matchBasedOnIdentifier(CoAPResponse resp) {
        return outHandler.getOutgoingRequests().get(resp.getIdentifier());
    }

    private void handleReset(CoAPResponse resp) throws CoAPException {
        CoAPRequest originalRequest = matchBasedOnIdentifier(resp);
        if (originalRequest == null) {
            //CoAPActivator.logger.warn("[RESET] Matching request was not found");
            return;
        }
        if (resp.isCacheable()) {
            cacheResponse(resp, originalRequest);
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
        CoAPRequest originalRequest = matchBasedOnIdentifier(resp);
        if (originalRequest == null) {
            // Reply with a RESET message??
            //CoAPActivator.logger.warn("[ACK] Matching request was not found");
            return;
        }

        // Handle empty ack
        if (resp.getCode() == CoAPResponseCode.EMPTY) {
            //CoAPActivator.logger.debug("An empty ACK received");
            CoAPRequestListener listener = originalRequest.getListener();
            if (listener != null) {
                listener.emptyAckReceived(resp, originalRequest);
            }
            return;
        }

        // Check for block options
        List block2 = resp.getOptionHeaders(BLOCK2);
        List block1 = resp.getOptionHeaders(BLOCK1);

        // Cache response
        // Check the response code to check conditions for caching, do not also
        // cache is there's a block option (either block1 or block2 present)
        if (resp.isCacheable() && block2.isEmpty() && block1.isEmpty()) {
            cacheResponse(resp, originalRequest);
        }

        // Do not return from these methods, use the normal callback methods
        if (!block1.isEmpty()) {
            CoAPRequest nextBlock = blockHandler.block1OptionResponseReceived(resp, originalRequest);
            if (nextBlock != null) {
                outHandler.send(nextBlock, false);
            }
        } else if (!block2.isEmpty()) {
            CoAPRequest nextBlock = blockHandler.block2OptionReceived(resp, originalRequest);
            // Send the next block
            if (nextBlock != null) {
                outHandler.send(nextBlock, false);
            }
        }

        // if the message is related to an observed resource or the resource
        // contains observe header
        if (obsHandler.isObserved(originalRequest.getUriFromRequest())) {
            obsHandler.handleObserveResponse(originalRequest, resp);
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
        Optional<CoAPRequest> originalRequest = Optional.empty();
        // Confirmable messages are identified based on tokens
        try {
            originalRequest = matchBasedOnTokens(resp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!originalRequest.isPresent()) {
            //CoAPActivator.logger.warn("[ConAndNon] Matching request was not found");

            // If no matching request can be found for a confirmable response,
            // reply with a reset
            if (resp.getMessageType() == CoAPMessageType.CONFIRMABLE) {
                sendResponse(resp.createReset());
            }
            return;
        }
        CoAPRequest oRequest = originalRequest.get();

        // Check for block options
        List<CoAPOptionHeader> block2 = resp.getOptionHeaders(BLOCK2);
        List<CoAPOptionHeader> block1 = resp.getOptionHeaders(BLOCK1);

        // Cache response
        // Check the response code to check conditions for caching, do not also
        // cache is there's a block option (either block1 or block2 present)
        if (resp.isCacheable() && block2.isEmpty() && block1.isEmpty()) {
            cacheResponse(resp, oRequest);
        }

        // Do not return from these methods, use the normal callback methods
        if (!block1.isEmpty()) {
            CoAPRequest nextBlock = blockHandler.block1OptionResponseReceived(resp, oRequest);
            if (nextBlock != null) {
                outHandler.send(nextBlock, false);
            }
        } else if (!block2.isEmpty()) {
            CoAPRequest nextBlock = blockHandler.block2OptionReceived(resp, oRequest);
            if (nextBlock != null) {
                outHandler.send(nextBlock, false);
            }
        }
        // if the message is related to an observed resource or the resource
        // contains observe header
        if (obsHandler.isObserved(oRequest.getUriFromRequest())) {
            obsHandler.handleObserveResponse(oRequest, resp);
        } else {
            CoAPRequestListener listener = oRequest.getListener();
            if (listener != null) {
                listener.separateResponseReceived(resp, oRequest);
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
        return obsHandler.createObservationRelationship(uri, observer);
    }

    /**
     * Terminates an observation relationship with the given resource and
     * observer. If the resource does not have further observers, the whole
     * relationship will be terminated.
     *
     * @param resource CoAP resource from which the relationship is to be
     * terminated
     * @param observer observer who wants to terminate the subscription
     * @return
     * @throws CoAPException
     */
    public boolean terminateObservationRelationship(CoAPResource resource,
            CoAPResourceObserver observer) throws CoAPException {
        return obsHandler.terminateObservationRelationship(resource, observer);
    }

    /**
     * This method is called when the timertask expires for a cached response.
     * The response will be removed from the cache.
     *
     * @param uri URI identifying the response to be removed
     */
    protected void removeCachedResponse(URI uri) {
        //CoAPActivator.logger.debug("Cached response for resource at [" + uri.toString() + "] expired, remove from cache");
        inResponseCache.remove(uri);
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

        //CoAPActivator.logger.debug("Cache response for resource at [" + originalRequest.getUriFromRequest().toString() + "] for [" + maxAge + "] seconds");
        // Cache response based on the max-age option
        inResponseCache.put(originalRequest.getUriFromRequest(), task);
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
        obsHandler.stopService();
        if (outBlockCache != null) {
            outBlockCache.cleanup();
        }
    }

    private void replyToResourceDiscovery(CoAPRequest req) {
        StringBuilder payloadStr = new StringBuilder();
        CoAPMessageType messageType = getResponseType(req);
        // If no uri-query parameters present for rd service or mp service,
        // reply from here
        List<CoAPOptionHeader> queryHeaders = req.getOptionHeaders(URI_QUERY);
        byte[] tokenHeader = req.getToken();

        // TODO how to handle these properly without enum etc..
        HashMap<String, String> attributes = new HashMap<>();
        // Read the attributes from the incoming request to a hasmhap
        for (CoAPOptionHeader header : queryHeaders) {
            String[] split = new String(header.getValue(), StandardCharsets.UTF_8).split("=");
            if (split.length > 1) {
                attributes.put(split[0], split[1]);
            } else {
                CoAPResponse resp = new CoAPResponse(1, req, messageType, CoAPResponseCode.BAD_REQUEST);
                outHandler.send(resp, false);
                return;
            }
        }

        // Compare the existing CoAP resources at the gateway to the request
        // query parameters
        try {
            if (getResources().values() != null) {
                for (CoAPResource res : getResources().values()) {
                    if (res.matchWithQueryParams(attributes)) {
                        payloadStr.append(res.getLinkFormatPresentation(false)).append(",");
                    }
                }
            }
            CoAPResponse resp;
            if (payloadStr.length() > 0) {

                // remove the last comma
                payloadStr.setLength(payloadStr.length() - 1);

                short contentTypeId = 40;
                byte[] contentTypeBytes = BitUtil.splitShortToBytes(contentTypeId);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                outputStream.write(contentTypeBytes[1]);

                CoAPOptionHeader header = new CoAPOptionHeader(CONTENT_FORMAT, outputStream.toByteArray());
                resp = new CoAPResponse(1, messageType, CoAPResponseCode.CONTENT, req.getMessageId(), tokenHeader);

                resp.setPayload(payloadStr.toString().getBytes(StandardCharsets.UTF_8));
                resp.addOptionHeader(header);
            } else {
                resp = new CoAPResponse(1, messageType, CoAPResponseCode.NOT_FOUND, req.getMessageId(), tokenHeader);
            }

            resp.setSocketAddress(req.getSocketAddress());
            outHandler.send(resp, false);
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
        CoAPMessageType type = req.getMessageType();
        if (type == CoAPMessageType.CONFIRMABLE) {
            return CoAPMessageType.ACKNOWLEDGEMENT;
        } else if (type == CoAPMessageType.NON_CONFIRMABLE) {
            return CoAPMessageType.NON_CONFIRMABLE;
        }
        // / TODO check correct response type for other cases
        return type;
    }

    //TODO: What is this method supposed to do?
    private void replyWithNotImplemented(CoAPRequest request) {
        int messageId = request.getMessageId();
        CoAPResponse resp = new CoAPResponse(1, CoAPMessageType.CONFIRMABLE, CoAPResponseCode.NOT_IMPLEMENTED, messageId, request.getToken());

        resp.setSocketAddress(request.getSocketAddress());
        // reply directly from here if no listeners are found
        resp = new CoAPResponse(1, CoAPMessageType.CONFIRMABLE, CoAPResponseCode.NOT_FOUND, messageId, request.getToken());
        resp.setSocketAddress(request.getSocketAddress());
        outHandler.send(resp, false);
    }

    /**
     * Private class that is responsible for caching the received responses. The
     * rules for caching are defined in the core draft.
     */
    private class CachedResponse extends TimerTask {

        private final CoAPResponse cachedResponse;
        private final CoAPRequest originalRequest;

        protected CachedResponse(CoAPResponse cachedResponse, CoAPRequest originalRequest) {
            this.cachedResponse = cachedResponse;
            this.originalRequest = originalRequest;
        }

        @Override
        public void run() {
            // cached response has expired, remove
            try {
                removeCachedResponse(originalRequest.getUriFromRequest());
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
}
