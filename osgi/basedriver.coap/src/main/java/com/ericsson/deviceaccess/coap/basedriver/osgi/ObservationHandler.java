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
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPMessage.CoAPMessageType;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionHeader;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPRequest;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPResponse;
import com.ericsson.deviceaccess.coap.basedriver.api.resources.CoAPObservationResource;
import com.ericsson.deviceaccess.coap.basedriver.api.resources.CoAPResource;
import com.ericsson.deviceaccess.coap.basedriver.api.resources.CoAPResourceObserver;
import com.ericsson.deviceaccess.coap.basedriver.util.BitOperations;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class is responsible for handling the observation relationships. It
 * caches the received responses.
 * <p/>
 * Note that in case there will be several clients wanting to follow the same
 * CoAP resource, the observation handler will first check the cache. If there
 * doesn't exist a fresh response, a further observe request is sent. This means
 * that all listeners will be called when a response for that "refresh" request
 * is received.
 */
public class ObservationHandler {

    private final Map<URI, CoAPObservationResource> observedResources;

    private final Map<URI, CoAPRequest> originalRequests;

    private final Map<URI, RefreshTask> cachedResponses;

    private final Timer timer;

    private final LocalCoAPEndpoint endpoint;

    /**
     * Constructor
     *
     * @param endpoint the local endpoint
     */
    public ObservationHandler(LocalCoAPEndpoint endpoint) {
        this.observedResources = new HashMap<>();
        this.cachedResponses = new HashMap<>();
        this.originalRequests = new HashMap<>();
        this.endpoint = endpoint;
        this.timer = new Timer();
    }

    /**
     * This method is called by the local endpoint when a response with observe
     * option is received.
     *
     * @param originalRequest original request that the response is related to
     * @param resp response from the CoAP Server
     * @throws CoAPException
     */
    public void handleObserveResponse(CoAPRequest originalRequest, CoAPResponse resp) throws CoAPException {
        // Handle responses related to observe relationships
        //CoAPActivator.logger.debug("Response is related to an observation relationship");

        URI uri = originalRequest.getUriFromRequest();
        CoAPObservationResource res = observedResources.get(uri);

        if (resp.getOptionHeaders(CoAPOptionName.OBSERVE) == null
                || resp.getOptionHeaders(CoAPOptionName.OBSERVE).isEmpty()) {

            // this means the response is terminating an observation relationship
            observedResources.remove(uri);
            res.getObservers().forEach(obs -> {
                obs.observationRelationshipTerminated(resp, res, originalRequests.get(uri));
            });

            RefreshTask oldTask = cachedResponses.get(uri);
            if (oldTask != null) {
                oldTask.cancel();
            }
            return;
        }

        byte[] bytes = resp.getOptionHeaders(CoAPOptionName.OBSERVE).get(0).getValue();

        if (bytes.length == 2) {
            short test = BitOperations.mergeBytesToShort(bytes[0], bytes[1]);
            int observeValue = test & 0xFFFF;

            //CoAPActivator.logger.debug("Masked observe value in observation handler [" + observeValue + "]");
            if (resp.getOptionHeaders(CoAPOptionName.BLOCK2).isEmpty()) {
                // Check if the notification is fresh
                // if the response is not fresh, it can be discarded!
                if (!res.isFresh(observeValue, new java.util.Date())) {
                    return;
                }
            } else {
                System.out.println("TODO handling of freshness of blockwise observe responses");
            }
        }

        // Put in the cached responses, replacing the old task if
        RefreshTask task = new RefreshTask(resp, uri);

        RefreshTask oldTask = cachedResponses.get(uri);
        if (oldTask != null) {
            oldTask.cancel();
        }

        cachedResponses.put(uri, task);

        // Read the max-age option
        long maxAge = resp.getMaxAge();

        // from draft-ietf-core-observe-03, read the max-ofe header too
        List<CoAPOptionHeader> headers = resp.getOptionHeaders(CoAPOptionName.MAX_OFE);
        // by default the maxOfe is 0
        int maxOfe = 0;
        if (!headers.isEmpty()) {
            bytes = headers.get(0).getValue();

            // make the header 4 bytes long
            if (bytes.length < 4) {
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();

                int diff = 4 - bytes.length;
                for (int i = 0; i < diff; i++) {
                    outStream.write(0);
                }

                try {
                    outStream.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bytes = outStream.toByteArray();
                maxOfe = BitOperations.mergeBytesToInt(bytes[0], bytes[1], bytes[2], bytes[3]);
            } else {
                maxOfe = BitOperations.mergeBytesToInt(bytes[0], bytes[1], bytes[2], bytes[3]);
            }

            // make signed int to unsigned long
            res.setMaxOfe(0xffffffffL & maxOfe);
        }

        long cachingTime = maxAge + maxOfe;
        timer.schedule(task, cachingTime * 1000);

        if (res != null) {
            // TODO populate resource with more data?
            res.setContent(resp.getPayload());

            res.getObservers().forEach(observer -> {
                CoAPRequest req = originalRequests.get(res.getUri());
                observer.observeResponseReceived(resp, res, req);
            });
        }
    }

    /**
     * This method terminates an observation relationship between the given
     * resource and observer instance
     *
     * @param resource resource to which the observation is related to
     * @param observer observer of the resource
     * @throws CoAPException
     */
    public boolean terminateObservationRelationship(CoAPResource resource,
            CoAPResourceObserver observer) throws CoAPException {

        boolean removed = resource.removeObserver(observer);
        // If no more observers are left, finish the observation
        // relationship by sending a request without observe option
        if (removed && resource.getObservers().isEmpty()) {

            // TODO should the termination request be confirmable or non-confirmable
            InetSocketAddress sockaddr = null;
            try {
                String socketAddress = resource.getUri().getHost();
                InetAddress address = InetAddress.getByName(socketAddress);
                sockaddr = new InetSocketAddress(address, resource.getUri().getPort());
            } catch (UnknownHostException e) {
                throw new CoAPException(e);
            }

            CoAPRequest req = endpoint.createCoAPRequest(
                    CoAPMessageType.CONFIRMABLE, 1, sockaddr,
                    resource.getUri(), null);
            endpoint.sendRequest(req);

            originalRequests.remove(resource.getUri());
            // TODO identify if the relationship was terminated!
            observedResources.remove(resource.getUri());
        }
        return removed;
    }

    public boolean isObserved(URI uri) {
        return observedResources.get(uri) != null;
    }

    public CoAPResource getResource(URI uri) {
        return observedResources.get(uri);
    }

    /**
     * Remove cached response (expired)
     *
     * @param uri URI to the resource
     */
    protected synchronized void removeCachedResponse(URI uri) {
        // CoAPActivator.logger.debug("Cached response for URI [" + uri.toString() + " expired, remove from cache");
        cachedResponses.remove(uri);
    }

    /**
     * This methods create an observation relationship with the given uri and
     * the observer instance
     *
     * @param uri URI to the resource
     * @param observer observer who will be notified about the changes
     * @return CoAPResource representing the given URI
     * @throws CoAPException
     */
    public CoAPResource createObservationRelationship(URI uri,
            CoAPResourceObserver observer) throws CoAPException {
        // CoAPActivator.logger.debug("Create observation relationship to URI [" + uri.toString() + "]");
        CoAPObservationResource resource;

        // Check if there already exist observation for this resource
        if (observedResources.containsKey(uri)) {
            // get the resource based on key
            resource = observedResources.get(uri);
            resource.addObserver(observer);

            // Notify with a cached response
            if (cachedResponses.containsKey(uri)) {
                //CoAPActivator.logger.debug("A fresh response still found in cache");
                observer.observeResponseReceived(cachedResponses.get(uri).getResponse(),
                        resource, originalRequests.get(uri));
            } // If the response in the cache is older than max-age + max-ofe,
            // send a further observation request
            else {
                CoAPRequest req = createObservationRequest(uri);
                // Store in the local memory the original request
                originalRequests.put(uri, req);
                endpoint.sendRequest(req);
            }
        } // Otherwise create a new observation request and add it in the list of
        // observed resources
        else {
            CoAPRequest req = createObservationRequest(uri);

            resource = new CoAPObservationResource(uri);
            resource.addObserver(observer);
            originalRequests.put(uri, req);
            observedResources.put(uri, resource);

            endpoint.sendRequest(req);
        }
        return resource;
    }

    /**
     * Create observation request based on the given URI. This request contains
     * the observe option.
     *
     * @param uri URI
     * @return
     * @throws CoAPException
     */
    private CoAPRequest createObservationRequest(URI uri) throws CoAPException {
        InetSocketAddress sockaddr = null;
        try {
            String socketAddress = uri.getHost();
            InetAddress address = InetAddress.getByName(socketAddress);
            sockaddr = new InetSocketAddress(address, uri.getPort());
        } catch (UnknownHostException e) {
            throw new CoAPException(e);
        }

        // Add observe option in the request
        CoAPRequest req = endpoint.createCoAPRequest(
                CoAPMessageType.CONFIRMABLE, 1, sockaddr, uri, null);

        // A non-negative integer which is represented in network byte order
        short observe = 0;
        byte[] observeBytes = BitOperations.splitShortToBytes(observe);

        CoAPOptionHeader observeOpt = new CoAPOptionHeader(CoAPOptionName.OBSERVE, observeBytes);
        req.addOptionHeader(observeOpt);
        req.generateTokenHeader();
        return req;
    }

    /**
     * Cancel the timer and its tasks. This method is needed when stopping the
     * bundle.
     */
    public void stopService() {
        timer.cancel();

    }

    /**
     * Inner class to handle timers for cached observe responses
     */
    private class RefreshTask extends TimerTask {

        private final CoAPResponse cachedResponse;
        private final URI uri;

        /**
         * Constructor
         *
         * @param cachedResponse
         * @param uri
         */
        protected RefreshTask(CoAPResponse cachedResponse, URI uri) {
            this.cachedResponse = cachedResponse;
            this.uri = uri;
        }

        @Override
        public void run() {
            //CoAPActivator.logger.debug("Cached response expired");
            // If the cached response expires, remove first the cached stuff
            removeCachedResponse(uri);
            // TODO if cached response expires, should send a new GET request!!
            try {
                //CoAPActivator.logger.debug("Send a new GET request towards the server to refresh the observation");
                CoAPRequest req = createObservationRequest(uri);
                // Do no update the hashmap, keep the original request there
                endpoint.sendRequest(req);
            } catch (CoAPException e) {
                e.printStackTrace();
            }
        }

        public CoAPResponse getResponse() {
            return this.cachedResponse;
        }
    }
}
