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
import com.ericsson.deviceaccess.coap.basedriver.api.CoAPException;
import com.ericsson.deviceaccess.coap.basedriver.api.CoAPRemoteEndpoint;
import com.ericsson.deviceaccess.coap.basedriver.api.CoAPRemoteEndpoint.CoAPRemoteEndpointType;
import com.ericsson.deviceaccess.coap.basedriver.api.DeviceInterface;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPResponse;
import com.ericsson.deviceaccess.coap.basedriver.api.resources.CoAPResource;
import com.ericsson.deviceaccess.coap.basedriver.api.resources.CoAPResource.CoAPResourceType;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

/**
 * This class reads the resources received in the payload. It will notify the
 * coap adaptor using the device interface about new devices found in the
 * network. Note that the functionality of this class is very much hard coded to
 * match the CoAP californium server, that is used as a server in the prototype
 * (for now).
 */
public class LinkFormatDirectory {

    // This keeps the already known endpoints
    private final HashMap<URI, RemoteEndpointRefreshTask> refreshTasks;
    private final Timer timer;
    private int resourceDiscoveryInterval;


    /**
     * Constructor.
     */
    public LinkFormatDirectory() {
        this.refreshTasks = new HashMap<>();
        this.timer = new Timer();
    }

    /**
     * Set the interval at which the resource discovery requests are sent.
     *
     * @param interval interval for resource discovery requests (in seconds)
     */
    public void setResourceDiscoveryInterval(int interval) {
        this.resourceDiscoveryInterval = interval;
    }

    /**
     * Returns a list of known CoAP remote endpoints
     *
     * @return return the list of remote end points (devices) known
     */
    public List<CoAPRemoteEndpoint> getKnownDevices() {
        return refreshTasks
                .values()
                .stream()
                .map(task -> task.getCoAPRemoteEndpoint())
                .collect(Collectors.toList());
    }

    /**
     * Read the list of resources
     *
     * @param updatedResources
     * @param resp
     * @throws CoAPException
     */
    public void handleResourceDiscoveryResponse(
            List<CoAPResource> updatedResources, CoAPResponse resp)
            throws CoAPException {

        // Check first if this device is known
        String serverHost = resp.getSocketAddress().getAddress().getCanonicalHostName();
        int port = resp.getSocketAddress().getPort();
        CoAPRemoteEndpoint endpoint = null;
        try {
            URI serverURI = new URI("coap", null, serverHost, port, null, null, null);
            Object[] services = CoAPActivator.tracker.getServices();

            // Notify about new devices
            if (!refreshTasks.containsKey(serverURI)) {

                // This is a hack to figure out the type of the server
                CoAPRemoteEndpointType type = CoAPRemoteEndpointType.OTHER;
                if (!updatedResources.isEmpty()) {
                    if (updatedResources.get(1).getResourceType().equals("\"SepararateResponseTester\"")) {
                        type = CoAPRemoteEndpointType.CALIFORNIUM;
                    }
                }
                endpoint = new CoAPRemoteEndpoint(serverURI, type);

                // Start a "freshness" timer for the endpoint:
                RemoteEndpointRefreshTask task = new RemoteEndpointRefreshTask(serverURI, endpoint);
                this.refreshTasks.put(serverURI, task);

                // Schedule tasks to remove the endpoint if no new information
                // is received
                int scheduled = (30 + this.resourceDiscoveryInterval) * 1000;

                timer.schedule(task, scheduled);
                // New endpoint, all new resources
                for (CoAPResource res : updatedResources) {
                    URI resourcePath = res.getUri();

                    InetSocketAddress address = resp.getSocketAddress();
                    URI uri;

                    String resourceString = resourcePath.toString();
                    if (!resourceString.startsWith("/")) {
                        resourceString = "/" + resourceString;
                    }

                    // by default set the type to other
                    res.setCoAPResourceType(CoAPResourceType.OTHER);
                    for (CoAPResourceType resourceType : CoAPResourceType.values()) {
                        if (resourceString.equals(resourceType.getPath())) {
                            res.setCoAPResourceType(resourceType);
                            break;
                        }
                    }

                    uri = new URI("coap", null, address.getAddress()
                            .getCanonicalHostName(), address.getPort(),
                            resourceString, null, null);
                    res.setUri(uri);

                    /*
                     CoAPActivator.logger.info("A new resource with URI ["
                     + uri.toString() + "]");
                     */
                    endpoint.addResource(res);
                }

                if (services != null) {
                    for (Object s : services) {
                        ((DeviceInterface) s).deviceAdded(endpoint);
                    }
                }
            } else {
                // Else reset timer task

                /*
                 CoAPActivator.logger
                 .debug("Response received from known device ["
                 + serverURI.toString()
                 + "] before expiration, now reset timer!");
                 */
                RemoteEndpointRefreshTask task = refreshTasks.get(serverURI);
                // cancel the refresh task and update the scheduled time for
                // expiration
                try {
                    refreshTasks.remove(task.uri);
                    task.cancel();
                } catch (IllegalStateException e) {
                    /*
                     CoAPActivator.logger
                     .warn("Task already cancelled");
                     */
                }
                int scheduled = (30 + this.resourceDiscoveryInterval) * 1000;
                RemoteEndpointRefreshTask newTask = new RemoteEndpointRefreshTask(
                        serverURI, endpoint);
                this.refreshTasks.put(serverURI, newTask);
                timer.schedule(newTask, scheduled);
            }

        } catch (URISyntaxException e) {
            throw new CoAPException(e);
        }
    }

    /*
     * If no response from the server is received in 100 seconds after the
     * previous response, remove this server (consider it has left the network)
     */
    protected synchronized void removeCachedRemoteEndpoint(URI uri) {
        Object[] services = CoAPActivator.tracker.getServices();

        /*
         CoAPActivator.logger.debug("No response from a server  ["
         + uri.toString()
         + "] for a discovery request, remove from cache");
         */
        RemoteEndpointRefreshTask task = refreshTasks.get(uri);
        task.cancel();
        refreshTasks.remove(uri);

        // TODO call on the services that a server is removed!!
        if (services != null) {
            for (Object s : services) {
                ((DeviceInterface) s).deviceRemoved(task.getCoAPRemoteEndpoint());
            }
        }
    }

    public void stopService() {
        this.timer.cancel();
    }

    /**
     * This inner class is responsible for removing such remote endpoint entries
     * from which no reply to the resource discveory requests has been received
     * anymore
     */
    private class RemoteEndpointRefreshTask extends TimerTask {

        private final URI uri;
        private final CoAPRemoteEndpoint endpoint;

        protected RemoteEndpointRefreshTask(URI uri, CoAPRemoteEndpoint endpoint) {
            this.uri = uri;
            this.endpoint = endpoint;
        }

        @Override
        public void run() {
            /*
            CoAPActivator.logger.debug("Cached remote endpoint expired");
            */
            removeCachedRemoteEndpoint(uri);
        }

        public CoAPRemoteEndpoint getCoAPRemoteEndpoint() {
            return this.endpoint;
        }
    }
}
