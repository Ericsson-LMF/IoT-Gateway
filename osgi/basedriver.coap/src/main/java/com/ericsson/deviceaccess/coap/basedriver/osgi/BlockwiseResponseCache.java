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

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import com.ericsson.deviceaccess.coap.basedriver.api.CoAPException;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionHeader;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPRequest;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPResponse;

public class BlockwiseResponseCache {

    final private Map<SessionKey, SessionData> cache = new HashMap<>();
    final private long cacheTime;
    final private Timer timer;

    class SessionKey {

        final private InetSocketAddress clientAddress;
        final private URI resourceUri;
        final private List queryStrings; // List<String>

        SessionKey(InetSocketAddress clientAddress, URI resourceUri, List queryHeaders) {
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

            return (((this.clientAddress == null) && (target.clientAddress == null))
                    || ((this.clientAddress != null) && (this.clientAddress.equals(target.clientAddress))))
                    && (((this.resourceUri == null) && (target.resourceUri == null))
                    || ((this.resourceUri != null) && (this.resourceUri.equals(target.resourceUri))))
                    && (((this.queryStrings == null) && (target.queryStrings == null))
                    || ((this.queryStrings != null) && (this.queryStrings.equals(target.queryStrings))));
        }

        @Override
        public int hashCode() {
            int value = 1;

            value += value * 31 + ((clientAddress != null) ? clientAddress.hashCode() : Void.class.hashCode());
            value += value * 31 + ((resourceUri != null) ? resourceUri.hashCode() : Void.class.hashCode());
            value += value * 31 + ((queryStrings != null) ? queryStrings.hashCode() : Void.class.hashCode());

            return value;
        }
    }

    class SessionData {

        final private SessionKey key;
        final private byte[] payload;
        final private int responseCode;

        private TimerTask timerTask = null;

        public SessionData(SessionKey key, byte[] payload, int responseCode) {
            this.key = key;
            this.payload = payload;
            this.responseCode = responseCode;
        }

        public SessionKey getSessionKey() {
            return this.key;
        }

        // Never change payload !!
        public byte[] getPayload() {
            return this.payload;
            // return (byte[])this.payload.clone();
        }

        public int getResponseCode() {
            return this.responseCode;
        }

        synchronized public void startTimer() {
            this.stopTimer();

            this.timerTask = new TimerTask() {
                @Override
                public void run() {
                    synchronized (cache) {
                        cache.remove(key);
                    }
                }
            };
            timer.schedule(this.timerTask, cacheTime);
        }

        synchronized public void stopTimer() {
            if (this.timerTask != null) {
                this.timerTask.cancel();
                this.timerTask = null;
            }
        }
    }

    public BlockwiseResponseCache(long cacheTime) {
        this.cacheTime = cacheTime;
        this.timer = new Timer();
    }

    public void cleanup() {
        synchronized (cache) {
            cache.values().forEach(session -> session.stopTimer());
            cache.clear();
        }
    }

    public void put(InetSocketAddress clientAddress, URI resourceUri, List queryHeaders, byte[] payload, int responseCode) {
        synchronized (cache) {
            SessionKey key = new SessionKey(clientAddress, resourceUri, queryHeaders);
            SessionData data = new SessionData(key, payload, responseCode);

            SessionData oldData = cache.get(key);
            if (oldData != null) {
                oldData.stopTimer();
            }

            this.cache.put(key, data);
        }
    }

    public void put(CoAPRequest request, CoAPResponse response) throws CoAPException {
        this.put(request.getSocketAddress(), request.getUriFromRequest(), getQueryStrings(request), response.getPayload(), response.getCode());
    }

    public SessionData get(InetSocketAddress clientAddress, URI resourceUri, List queryHeaders) {
        synchronized (cache) {
            return cache.get(new SessionKey(clientAddress, resourceUri, queryHeaders));
        }
    }

    public SessionData get(CoAPRequest request) throws CoAPException {
        return this.get(request.getSocketAddress(), request.getUriFromRequest(), getQueryStrings(request));
    }

    public void remove(InetSocketAddress clientAddress, URI resourceUri, List queryHeaders) {
        synchronized (cache) {
            SessionKey key = new SessionKey(clientAddress, resourceUri, queryHeaders);
            SessionData data = cache.get(key);
            if (data != null) {
                data.stopTimer();
            }
            cache.remove(key);
        }
    }

    public void remove(CoAPRequest request) throws CoAPException {
        this.remove(request.getSocketAddress(), request.getUriFromRequest(), getQueryStrings(request));
    }

    public void updateTimer(InetSocketAddress clientAddress, URI resourceUri, List queryHeaders) {
        synchronized (cache) {
            SessionData data = this.get(clientAddress, resourceUri, queryHeaders);
            if (data != null) {
                data.startTimer();
            }
        }
    }

    public void updateTimer(CoAPRequest request) throws CoAPException {
        this.updateTimer(request.getSocketAddress(), request.getUriFromRequest(), getQueryStrings(request));
    }

    static private List getQueryStrings(CoAPRequest request) {
        List queryStrings = new Vector();

        List queryHeaders = request.getOptionHeaders(CoAPOptionName.URI_QUERY);
        if (queryHeaders == null) {
            return queryStrings;
        }
        for (Iterator it = queryHeaders.iterator(); it.hasNext();) {
            CoAPOptionHeader header = (CoAPOptionHeader) it.next();
            byte[] value = header.getValue();
            if (value != null) {
                queryStrings.add(new String(value));
            }
        }

        return queryStrings;

    }
}
