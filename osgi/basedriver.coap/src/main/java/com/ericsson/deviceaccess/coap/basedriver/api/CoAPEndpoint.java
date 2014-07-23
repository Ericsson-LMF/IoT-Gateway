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
package com.ericsson.deviceaccess.coap.basedriver.api;

import com.ericsson.deviceaccess.coap.basedriver.api.resources.CoAPResource;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * CoAP endpoint can act both as CoAP client and server. Thus, it needs to be
 * able to handle both requests and responses (message handler).
 * <p/>
 * This class has two subclasses, LocalCoAPEndpoint, which represents the
 * gateway functionality and CoAPRemoteEndpoint, which represent a remote CoAP
 * server. The LocalCoAPEndpoint does not have own resources, but local services
 * can add themselves as resources through the CoAPService class. This way the
 * resources will be available in the resource discovery towards this gateway.
 *
 *
 */
public abstract class CoAPEndpoint {

    public static final String WELLKNOWN_CORE = "/.well-known/core";

    private final Map<URI, CoAPResource> resources;

    private URI uri;

    public CoAPEndpoint(URI uri) {
        resources = new HashMap<>();
    }

    public URI getUri() {
        return uri;
    }

    /**
     * Gets the list of resources available on the CoAP endpoint found using
     * ./well-known/core requests
     *
     * @return map of resources, URI as the key
     */
    public Map<URI, CoAPResource> getResources() {
        return resources;
    }

    /**
     * This method is used when resource discovery parses the responses from the
     * CoAp server. (Not that this method does not create resource on the CoAP
     * endpoint)
     *
     * @param resource found
     */
    public void addResource(CoAPResource resource) {
        resources.put(resource.getUri(), resource);
    }

    /**
     * This method will be needed from RD when updating the resource tree
     */
    public void removeResources() {
        resources.clear();
    }

    public void removeResource(CoAPResource res) {
        resources.remove(res.getUri());
    }
}
