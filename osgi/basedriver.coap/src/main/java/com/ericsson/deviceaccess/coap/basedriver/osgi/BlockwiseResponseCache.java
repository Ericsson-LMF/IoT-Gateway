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

import com.ericsson.common.util.function.FunctionalUtil;
import com.ericsson.deviceaccess.coap.basedriver.api.CoAPException;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPRequest;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPResponse;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BlockwiseResponseCache {

    private static List<String> getQueryStrings(CoAPRequest request) {
        return request
                .getOptionHeaders(CoAPOptionName.URI_QUERY)
                .stream()
                .map(header -> header.getValue())
                .filter(value -> value != null)
                .map(value -> new String(value, StandardCharsets.UTF_8))
                .collect(Collectors.toList());
    }

    final private Map<SessionKey, SessionData> cache = new ConcurrentHashMap<>();
    final private long cacheTime;
    final private Timer timer;

    public BlockwiseResponseCache(long cacheTime) {
        this.cacheTime = cacheTime;
        this.timer = new Timer();
    }

    public void cleanup() {
        cache.values().forEach(session -> session.stopTimer());
        cache.clear();
    }

    public void put(InetSocketAddress clientAddress, URI resourceUri, List queryHeaders, byte[] payload, int responseCode) {
        SessionKey key = new SessionKey(clientAddress, resourceUri, queryHeaders);
        SessionData data = new SessionData(key, payload, responseCode);
        FunctionalUtil.putAndClean(cache, key, data, v -> v.stopTimer());
    }

    public void put(CoAPRequest request, CoAPResponse response) throws CoAPException {
        put(request.getSocketAddress(), request.getUriFromRequest(), getQueryStrings(request), response.getPayload(), response.getCode());
    }

    public SessionData get(InetSocketAddress clientAddress, URI resourceUri, List queryHeaders) {
        return cache.get(new SessionKey(clientAddress, resourceUri, queryHeaders));
    }

    public SessionData get(CoAPRequest request) throws CoAPException {
        return get(request.getSocketAddress(), request.getUriFromRequest(), getQueryStrings(request));
    }

    public void remove(InetSocketAddress clientAddress, URI resourceUri, List queryHeaders) {
        SessionKey key = new SessionKey(clientAddress, resourceUri, queryHeaders);
        FunctionalUtil.putAndClean(cache, key, null, v -> v.stopTimer());
    }

    public void remove(CoAPRequest request) throws CoAPException {
        this.remove(request.getSocketAddress(), request.getUriFromRequest(), getQueryStrings(request));
    }

    public void updateTimer(InetSocketAddress clientAddress, URI resourceUri, List queryHeaders) {
        SessionData data = get(clientAddress, resourceUri, queryHeaders);
        if (data != null) {
            data.startTimer();
        }
    }

    public void updateTimer(CoAPRequest request) throws CoAPException {
        this.updateTimer(request.getSocketAddress(), request.getUriFromRequest(), getQueryStrings(request));
    }

    public class SessionKey {

        final private InetSocketAddress clientAddress;
        final private URI resourceUri;
        final private List<String> queryStrings;

        private SessionKey(InetSocketAddress clientAddress, URI resourceUri, List<String> queryHeaders) {
            this.clientAddress = clientAddress;
            this.resourceUri = resourceUri;
            this.queryStrings = queryHeaders;
            Collections.sort(this.queryStrings);
        }

        // XXX: Should we check both address and port, or only address?
        // If source port always change packet by packet, this cache doesn't work at all.
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof SessionKey)) {
                return false;
            }
            SessionKey target = (SessionKey) obj;

            return (clientAddress == null && clientAddress == null
                    || clientAddress != null && clientAddress.equals(target.clientAddress))
                    && (resourceUri == null && target.resourceUri == null
                    || resourceUri != null && resourceUri.equals(target.resourceUri))
                    && (queryStrings == null && target.queryStrings == null
                    || queryStrings != null && queryStrings.equals(target.queryStrings));
        }

        @Override
        public int hashCode() {
            int value = 1;
            value += value * 31 + (clientAddress != null ? clientAddress.hashCode() : Void.class.hashCode());
            value += value * 31 + (resourceUri != null ? resourceUri.hashCode() : Void.class.hashCode());
            value += value * 31 + (queryStrings != null ? queryStrings.hashCode() : Void.class.hashCode());
            return value;
        }
    }

    public class SessionData {

        final private SessionKey key;
        final private byte[] payload;
        final private int responseCode;
        private TimerTask timerTask = null;

        private SessionData(SessionKey key, byte[] payload, int responseCode) {
            this.key = key;
            this.payload = payload;
            this.responseCode = responseCode;
        }

        public SessionKey getSessionKey() {
            return key;
        }

        // Never change payload !!
        public byte[] getPayload() {
            return payload;
        }

        public int getResponseCode() {
            return responseCode;
        }

        public void startTimer() {
            stopTimer();
            synchronized (cache) {
                if (timerTask == null) {
                    timerTask = FunctionalUtil.timerTask(() -> {
                        cache.remove(key);
                    });
                    timer.schedule(timerTask, cacheTime);
                }
            }
        }

        public void stopTimer() {
            synchronized (cache) {
                if (timerTask != null) {
                    timerTask.cancel();
                    timerTask = null;
                }
            }
        }
    }
}
