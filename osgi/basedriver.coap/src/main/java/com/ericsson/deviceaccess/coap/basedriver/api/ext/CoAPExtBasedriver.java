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
package com.ericsson.deviceaccess.coap.basedriver.api.ext;

import com.ericsson.deviceaccess.coap.basedriver.api.CoAPException;
import com.ericsson.deviceaccess.coap.basedriver.api.IncomingCoAPRequestListener;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPMessage.CoAPMessageType;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPRequest;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPRequestCode;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPResponse;
import com.ericsson.deviceaccess.coap.basedriver.api.resources.CoAPResource;
import com.ericsson.deviceaccess.coap.basedriver.api.resources.CoAPResourceObserver;
import com.ericsson.deviceaccess.coap.basedriver.communication.TransportLayerReceiver;
import com.ericsson.deviceaccess.coap.basedriver.communication.TransportLayerSender;
import com.ericsson.deviceaccess.coap.basedriver.communication.UDPReceiver;
import com.ericsson.deviceaccess.coap.basedriver.communication.UDPSender;
import com.ericsson.deviceaccess.coap.basedriver.osgi.CoAPMessageHandlerFactory;
import com.ericsson.deviceaccess.coap.basedriver.osgi.IncomingMessageHandler;
import com.ericsson.deviceaccess.coap.basedriver.osgi.LinkFormatDirectory;
import com.ericsson.deviceaccess.coap.basedriver.osgi.OutgoingMessageHandler;
import java.io.IOException;
import java.net.BindException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoAPExtBasedriver {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoAPExtBasedriver.class);

    private TransportLayerReceiver transportLayerReceiver;
    private TransportLayerSender transportLayerSender;

    private IncomingMessageHandler incomingMessageHandler;
    private OutgoingMessageHandler outgoingMessageHandler;

    private InetAddress address;
    private int coapPort;
    private MulticastSocket multicastSocket;
    private DatagramSocket socket;

    final private int maximumBlockSzx;

    private CoAPExtEndpoint endpoint;
    private Timer timer;

    protected LinkFormatDirectory directory;

    public CoAPExtBasedriver(InetAddress address, int coapPort, int maximumBlockSzx) throws CoAPException {
        LOGGER.debug("(Addr=" + ((address != null) ? address.getHostName() : "NULL") + ",port=" + coapPort + ")");

        this.address = address;
        this.coapPort = coapPort;
        this.maximumBlockSzx = maximumBlockSzx;
        this.directory = new LinkFormatDirectory();

        this.socket = null;
        this.multicastSocket = null;
    }

    /**
     * Init the UDP sockets
     *
     * @throws CoAPException
     * @throws SocketException
     */
    public void init() throws CoAPException, SocketException {
        this.initUDPListeners();
        this.initCoAP();
    }

    public InetAddress getLocalAddress() {
        return this.address;
    }

    public int getLocalPort() {
        return this.coapPort;
    }

    /**
     * Creates a CoAP POST request. If request cannot be created, throws a
     * CoAPException.
     * <p/>
     * Usage: String host = "127.0.0.1"; int port = 5683; String path =
     * "helloWorld"; CoAPRequest req = this.coapService.createGetRequest(host,
     * port, path, type);
     *
     * @param host destination host in String format
     * @param port destination port. Value for port should be 0-65535.
     * @param path path to the resource to which this request is to be sent
     * @param messageType
     * @param payload
     * @return created CoAP POST request
     * @throws CoAPException if request generation fails for some reason
     */
    public CoAPRequest createPostRequest(String host, int port, String path,
            CoAPMessageType messageType, byte[] payload) throws CoAPException {
        CoAPRequest req = this.createRequest(messageType, CoAPRequestCode.POST, host, port, path);
        if (payload != null && payload.length > 0) {
            req.setPayload(payload);
        }
        return req;
    }

    /**
     * Creates a CoAP GET request. If request cannot be created, throws a
     * CoAPException
     *
     * @param host destination host in String format
     * @param port destination port. Value for port should be 0-65535.
     * @param path path to the resource to which this request is to be sent
     * @param messageType CoAP type of the message
     * @return created GET request
     * @throws CoAPException if request generation fails for some reason
     */
    public CoAPRequest createGetRequest(String host, int port, String path,
            CoAPMessageType messageType) throws CoAPException {
        return this.createRequest(messageType, CoAPRequestCode.GET, host, port, path);
    }

    /**
     * Creates a CoAP PUT request. If request cannot be created, throws a
     * CoAPException
     *
     * @param host destination host in String format
     * @param port destination port. Value for port should be 0-65535.
     * @param path path to the resource to which this request is to be sent
     * @param messageType CoAP type of the message
     * @param payload payload as byte array
     * @return created CoAP PUT request
     * @throws CoAPException
     */
    public CoAPRequest createPutRequest(String host, int port, String path,
            CoAPMessageType messageType, byte[] payload) throws CoAPException {
        CoAPRequest req = this.createRequest(messageType, CoAPRequestCode.PUT, host, port, path);
        if (payload != null && payload.length > 0) {
            req.setPayload(payload);
        }
        return req;
    }

    /**
     * Creates a CoAP DELETE request. If request cannot be created, throws a
     * CoAPException
     *
     * @param host destination host in String format
     * @param port destination port. Value for port should be 0-65535.
     * @param path path to the resource to which this request is to be sent
     * @param messageType CoAP type of the message
     * @return created CoAP DELETE request
     * @throws CoAPException
     */
    public CoAPRequest createDeleteRequest(String host, int port, String path,
            CoAPMessageType messageType) throws CoAPException {
        return this.createRequest(messageType, CoAPRequestCode.DELETE, host, port, path);
    }

    /**
     * Private method to create a CoAP request.
     *
     * @param messageType CoAP type of the message
     * @param host destination host in String format
     * @param port destination port
     * @param path path to the resource to which this request is to be sent
     */
    private CoAPRequest createRequest(CoAPMessageType messageType,
            CoAPRequestCode messageCode, String host, int port, String path)
            throws CoAPException {

        if (path != null && !path.isEmpty() && !path.startsWith("/")) {
            path = "/" + path;
        }

        URI uri = null;
        try {
            if (port > 0 && port < 65536) {
                uri = new URI("coap", null, host, port, path, null, null);
            } else {
                throw new CoAPException("Port not in range 0-65535");
            }
        } catch (URISyntaxException e) {
            throw new CoAPException(e);
        }

        InetSocketAddress sockaddr = null;
        try {
            String socketAddress = host;
            InetAddress addr = InetAddress.getByName(socketAddress);

            sockaddr = new InetSocketAddress(addr, uri.getPort());

        } catch (UnknownHostException e) {
            throw new CoAPException(e);
        }

        return endpoint.createCoAPRequest(messageType, messageCode, sockaddr,
                uri, null);
    }

    /**
     * Create an observation relationship to a particular CoAP resource. A new
     * request will be sent towards the network if there doesn't exist a
     * relationship yet. If there are already active subscriptions for the same
     * CoAP resource,
     *
     * @param host destination host in String format
     * @param port destination port
     * @param path path to the resource to which this request is to be sent
     * @param observer the listener class for the callbacks from the observed
     * resource
     * @return CoAPResource on which the observation relationship was created
     * @throws CoAPException
     */
    public CoAPResource createObservationRelationship(String host, int port,
            String path, CoAPResourceObserver observer) throws CoAPException {

        // Form the URI
        URI uri = null;
        try {
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            uri = new URI("coap", null, host, port, path, null, null);
        } catch (URISyntaxException e) {
            throw new CoAPException(e);
        }

        return endpoint.createObservationRelationship(uri, observer);
    }

    /**
     * Terminate observation relationship to a particular observer. If there
     * still exist other observers for this resource, the relationship will be
     * kept active.
     *
     * @param resource
     * @param observer
     * @return
     * @throws CoAPException
     */
    public boolean terminateObservationRelationship(CoAPResource resource,
            CoAPResourceObserver observer) throws CoAPException {
        return endpoint.terminateObservationRelationship(resource, observer);
    }

    /**
     * Send a CoAP request towards the CoAP network
     *
     * @param request request to send
     */
    public void sendRequest(CoAPRequest request) {
        endpoint.sendRequest(request);
    }

    /**
     * Send a CoAP response towards the CoAP network
     *
     * @param response
     */
    public void sendResponse(CoAPResponse response) {
        endpoint.sendResponse(response);
    }

    /**
     * Returns a list of known devices. This list is based on the responses from
     * the network to the resource discovery requests.
     *
     * @return list of known devices
     */
    public List getKnownDevices() {
        return this.directory.getKnownDevices();
    }

    /**
     * Init needed message handlers for this CoAP service and add then as
     * listeners to transport layer.
     */
    private void initCoAP() throws CoAPException {
        CoAPMessageHandlerFactory messageHandlerFactory = CoAPMessageHandlerFactory
                .getInstance();

        // these are the "message level" handler, at this point divided into
        // incoming/outgoing
        //this.incomingMessageHandler = messageHandlerFactory.getIncomingCoAPMessageHandler();
        //this.outgoingMessageHandler = messageHandlerFactory.getOutgoingCoAPMessageHandler(this.transportLayerSender);
        this.incomingMessageHandler = new MyIncomingMessageHandler();
        this.outgoingMessageHandler = new MyOutgoingMessageHandler(this.transportLayerSender);

        // get the local endpoint (singleton), add outgoing message handler as
        // listener
        URI uri;
        try {
            String hostAddress = address != null ? address.getHostAddress() : null;
            uri = new URI(null, null, hostAddress, coapPort, null, null, null);
            endpoint = new CoAPExtEndpoint(outgoingMessageHandler, incomingMessageHandler, uri);
        } catch (URISyntaxException e) {
            LOGGER.error("URI Syntax Error : " + address.getAddress(), e);
        }

        if (this.maximumBlockSzx != 6) {
            endpoint.setMaxSzx(this.maximumBlockSzx);
        }

        incomingMessageHandler.setIncomingCoAPListener(endpoint);
        transportLayerReceiver.addListener(incomingMessageHandler);
    }

    /**
     * Init UDP sender and receiver. Receiver thread will be started
     * immediately.
     *
     * @throws CoAPException
     * @throws SocketException
     */
    private void initUDPListeners() throws CoAPException, SocketException {
        // If address is a multicast address, and port is set, use
        // multicastsocket
        if (address != null && address.isMulticastAddress() && this.coapPort != -1) {
            // If given address is multicast, use multicast socket
            LOGGER.debug("Multicast UDP");
            try {

                LOGGER.debug("Join multicast group");
                multicastSocket = new MulticastSocket(this.coapPort);
                multicastSocket.joinGroup(address);

                transportLayerReceiver = new UDPReceiver(multicastSocket);

                transportLayerSender = new UDPSender(multicastSocket);
            } catch (IOException e) {
                throw new CoAPException(e);
            }
            // Otherwise use normal UDP datagram socket
        } else {
            LOGGER.debug("Normal UDP");
            try {

                // If the port is set, use the defined port
                if (coapPort != -1 && address != null) {
                    LOGGER.info("UDP1: " + address + ":" + this.coapPort);
                    socket = new DatagramSocket(coapPort, address);
                } else if (coapPort != -1) {
                    socket = new DatagramSocket(coapPort);
                    address = socket.getLocalAddress();
                    LOGGER.info("UDP2: " + address + ":" + this.coapPort);
                } else {
                    socket = new DatagramSocket();
                    address = socket.getLocalAddress();
                    coapPort = socket.getLocalPort();
                    LOGGER.info("UDP3: " + address + ":" + this.coapPort);
                }
            } catch (BindException e) {
                // This could happen
                throw e;
            } catch (SocketException e) {
                throw new CoAPException(e);
            }
            socket.setReuseAddress(true);
            socket.setSoTimeout(0);

            // System.out.println("init receiver & sender");
            transportLayerReceiver = new UDPReceiver(this.socket);
            transportLayerSender = new UDPSender(this.socket);
        }
        transportLayerReceiver.start();
        transportLayerSender.start();
    }

    /**
     * If a local service wants to expose its CoAP resources via the CoAP
     * .well-known/core interface it can use this method. Then the resource will
     * be included in the resource discovery responses sent by the gateway
     *
     * @param res the CoAPResource to be added
     */
    public void addResource(CoAPResource res) {
        endpoint.addResource(res);
    }

    /**
     * Removes a local service and its link format description to be used on the
     * .well-known/core interface
     *
     * @param res CoAP resource to be removed
     */
    public void removeResource(CoAPResource res) {
        endpoint.removeResource(res);
    }

    /**
     * This method will be called when the bundle is stopped by the
     * CoAPActivator class. It will stop the running sockets, reset the
     * factories etc.
     */
    public void stopService() {
        LOGGER.debug("Stop service");

        if (socket != null) {
            socket.close();
        }
        if (multicastSocket != null) {
            multicastSocket.close();
        }

        if (directory != null) {
            directory.stopService();
        }

        transportLayerReceiver.stopService();
        transportLayerReceiver = null;
        transportLayerSender.stopService();
        transportLayerSender = null;

        if (timer != null) {
            timer.cancel();
        }
        outgoingMessageHandler.stopService();
        outgoingMessageHandler = null;

        endpoint.stopService();
        endpoint = null;
    }

    public void setIncomingCoAPRequestListener(IncomingCoAPRequestListener reqListener) {
        endpoint.setIncomingCoAPRequestListener(reqListener);
    }

    public void unsetIncomingCoAPRequestListener(IncomingCoAPRequestListener reqListener) {
        endpoint.unsetIncomingCoAPRequestListener(reqListener);
    }

    class MyIncomingMessageHandler extends IncomingMessageHandler {

        MyIncomingMessageHandler() {
            super();
        }
    }

    class MyOutgoingMessageHandler extends OutgoingMessageHandler {

        MyOutgoingMessageHandler(TransportLayerSender sender) {
            super(sender);
        }
    }
}
