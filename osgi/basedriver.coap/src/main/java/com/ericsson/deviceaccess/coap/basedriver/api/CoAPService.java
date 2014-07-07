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

import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPMessage.CoAPMessageType;
import com.ericsson.deviceaccess.coap.basedriver.api.message.*;
import com.ericsson.deviceaccess.coap.basedriver.api.resources.CoAPResource;
import com.ericsson.deviceaccess.coap.basedriver.api.resources.CoAPResourceObserver;
import com.ericsson.deviceaccess.coap.basedriver.communication.TransportLayerReceiver;
import com.ericsson.deviceaccess.coap.basedriver.communication.TransportLayerSender;
import com.ericsson.deviceaccess.coap.basedriver.communication.UDPReceiver;
import com.ericsson.deviceaccess.coap.basedriver.communication.UDPSender;
import com.ericsson.deviceaccess.coap.basedriver.osgi.*;
import com.ericsson.deviceaccess.coap.basedriver.util.LinkFormatReader;



import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * CoAPService is the access point to the API. A CoAPService can be fetched
 * using a service reference.
 */
public class CoAPService {

	private TransportLayerReceiver transportLayerReceiver;
	private TransportLayerSender transportLayerSender;

	private IncomingMessageHandler incomingMessageHandler;
	private OutgoingMessageHandler outgoingMessageHandler;

	private InetAddress address;
	private int coapPort;
	private MulticastSocket multicastSocket;
	private DatagramSocket socket;

	private int maximumBlockSzx;

	private LocalCoAPEndpoint endpoint;
	private Timer timer;

	private InetAddress resourceDiscoveryAddress;
	private int resourceDiscoveryPort;

	protected LinkFormatReader reader;
	protected LinkFormatDirectory directory;

	/**
	 * This is a private class responsible for sending the resource discovery
	 * requests (link format)
	 */
	private class ResourceDiscoveryTask extends TimerTask implements
			CoAPRequestListener {

		private String path;

		/**
		 * Constructor
		 */
		protected ResourceDiscoveryTask() {
			path = ".well-known/core";
		}

		public void run() {

			/*
				CoAPActivator.logger.debug("Send a discovery request to "
						+ resourceDiscoveryAddress.getCanonicalHostName());
			*/

			try {
				CoAPRequest discoveryReq = createGetRequest(
						resourceDiscoveryAddress.getCanonicalHostName(),
						resourceDiscoveryPort, path,
						CoAPMessageType.NON_CONFIRMABLE);
				discoveryReq.generateTokenHeader();

				short contentType = 40;

				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				DataOutputStream dos = new DataOutputStream(stream);

				dos.writeShort(contentType);// 2 bytes
				dos.flush();

				CoAPOptionHeader header = new CoAPOptionHeader(
						CoAPOptionName.CONTENT_TYPE, stream.toByteArray());
				discoveryReq.addOptionHeader(header);
				discoveryReq.setListener(this);

				endpoint.sendRequest(discoveryReq);

			} catch (CoAPException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Callback implementation when an empty ack is received
		 * 
		 * @param response
		 */
		public void emptyAckReceived(CoAPResponse response, CoAPRequest request) {
			// TODO Auto-generated method stub

		}

		/**
		 * Callback implementation if the request reaches the maximum number of
		 * retransmissions
		 * 
		 * @param request
		 *            original request
		 */
		public void maximumRetransmissionsReached(CoAPRequest request) {
			// TODO Auto-generated method stub

		}

		/**
		 * Callback implementation for piggypacked responses
		 * 
		 * @param response
		 *            received response
		 * @param request
		 *            original request
		 */
		public void piggyPackedResponseReceived(CoAPResponse response,
				CoAPRequest request) {
			this.handleResponse(response);
		}

		/**
		 * Callback implementation for cases where the response for the resource
		 * discovery request is received in a separate message
		 * 
		 * @param response
		 *            received response
		 * @param request
		 *            original request
		 */
		public void separateResponseReceived(CoAPResponse response,
				CoAPRequest request) {
			this.handleResponse(response);

		}

		private void handleResponse(CoAPResponse response) {
			String payload = new String(response.getPayload());

			/*
				logger.debug("Message payload : [" + payload + "]");
			*/

			List resources = new LinkedList();

			try {
				// Parse the received message into the list of resources
				resources = reader.parseLinkFormatData(payload);
				// Let the LinkFormatDirectory class determine if there's
				// anything new in the response
				directory.handleResourceDiscoveryResponse(resources, response);
			} catch (CoAPException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Callback implementation for cases where there are active transactions
		 * towards the same CoAP server, thus new requests cannot be accepted
		 * 
		 * @param request
		 *            original request
		 */
		public void serviceBusy(CoAPRequest request) {
			// TODO Auto-generated method stub

		}

		public void resetResponseReceived(CoAPResponse response,
				CoAPRequest request) {
			// TODO Auto-generated method stub
			System.out.println("reset message received");
		}
	}

	/**
	 * Create a CoAPService that can be used for sending messages towards the
	 * CoAP network
	 * 
	 * @param
	 * @throws CoAPException
	 *             in case something goes wrong when initialising the service
	 */
	public CoAPService(InetAddress address, int coapPort, int maximumBlockSzx)
			throws CoAPException {
		this.coapPort = coapPort;
		this.address = address;

		this.reader = new LinkFormatReader();
		this.directory = new LinkFormatDirectory();
		this.maximumBlockSzx = maximumBlockSzx;
	}

	/**
	 * Init the UDP sockets
	 * 
	 * @throws CoAPException
	 * @throws SocketException 
	 */
	protected void init() throws CoAPException, SocketException {
		this.initUDPListeners();
		this.initCoAP();
	}

	/**
	 * Start the resource discovery service to the ./well-known/core address.
	 * These parameters can be specified in the coap.properties file. If the
	 * resource discovery interval is set to 0, no discovery requests will be
	 * sent
	 * 
	 * @param resourceDiscoveryInterval
	 *            interval at which the requests are made (in seconds),
	 * @param resourceDiscoveryAddress
	 *            address where to send the requests
	 * @param resourceDiscoveryPort
	 *            port where to send the requests
	 */
	protected void startResourceDiscoveryService(int resourceDiscoveryInterval,
			InetAddress resourceDiscoveryAddress, int resourceDiscoveryPort) {
		// Start the service if the value for the interval is > 0
		if (resourceDiscoveryInterval > 0) {
			this.directory
					.setResourceDiscoveryInterval(resourceDiscoveryInterval);
			this.timer = new Timer();
			ResourceDiscoveryTask task = new ResourceDiscoveryTask();
			this.resourceDiscoveryAddress = resourceDiscoveryAddress;
			this.resourceDiscoveryPort = resourceDiscoveryPort;

			// Schedule tasks to do resource discovery
			timer.schedule(task, 0, resourceDiscoveryInterval * 1000);
		} else {
			/*
				CoAPActivator.logger
						.debug("Resource discovery interval set to 0, do not use resource discovery");
			*/
		}
	}

	/**
	 * Creates a CoAP POST request. If request cannot be created, throws a
	 * CoAPException.
	 * <p/>
	 * Usage: String host = "127.0.0.1"; int port = 5683; String path =
	 * "helloWorld"; CoAPRequest req = this.coapService.createGetRequest(host,
	 * port, path, type);
	 * 
	 * @param host
	 *            destination host in String format
	 * @param port
	 *            destination port. Value for port should be 0-65535.
	 * @param path
	 *            path to the resource to which this request is to be sent
	 * @return created CoAP POST request
	 * @throws CoAPException
	 *             if request generation fails for some reason
	 */
	public CoAPRequest createPostRequest(String host, int port, String path,
			CoAPMessageType messageType, byte[] payload) throws CoAPException {

		CoAPRequest req = this.createRequest(messageType, 2, host, port, path);
		if (payload != null && payload.length > 0) {
			req.setPayload(payload);
		}
		return req;
	}

	/**
	 * Creates a CoAP GET request. If request cannot be created, throws a
	 * CoAPException
	 * 
	 * @param host
	 *            destination host in String format
	 * @param port
	 *            destination port. Value for port should be 0-65535.
	 * @param path
	 *            path to the resource to which this request is to be sent
	 * @param messageType
	 *            CoAP type of the message
	 * @return created GET request
	 * @throws CoAPException
	 *             if request generation fails for some reason
	 */
	public CoAPRequest createGetRequest(String host, int port, String path,
			CoAPMessageType messageType) throws CoAPException {
		return this.createRequest(messageType, 1, host, port, path);
	}

	/**
	 * Creates a CoAP PUT request. If request cannot be created, throws a
	 * CoAPException
	 * 
	 * @param host
	 *            destination host in String format
	 * @param port
	 *            destination port. Value for port should be 0-65535.
	 * @param path
	 *            path to the resource to which this request is to be sent
	 * @param messageType
	 *            CoAP type of the message
	 * @param payload
	 *            payload as byte array
	 * @return created CoAP PUT request
	 * @throws CoAPException
	 */
	public CoAPRequest createPutRequest(String host, int port, String path,
			CoAPMessageType messageType, byte[] payload) throws CoAPException {
		CoAPRequest req = this.createRequest(messageType, 3, host, port, path);
		if (payload != null && payload.length > 0) {
			req.setPayload(payload);
		}
		return req;
	}

	/**
	 * Creates a CoAP DELETE request. If request cannot be created, throws a
	 * CoAPException
	 * 
	 * @param host
	 *            destination host in String format
	 * @param port
	 *            destination port. Value for port should be 0-65535.
	 * @param path
	 *            path to the resource to which this request is to be sent
	 * @param messageType
	 *            CoAP type of the message
	 * @return created CoAP DELETE request
	 * @throws CoAPException
	 */
	public CoAPRequest createDeleteRequest(String host, int port, String path,
			CoAPMessageType messageType) throws CoAPException {
		return this.createRequest(messageType, 4, host, port, path);
	}

	/**
	 * Private method to create a CoAP request.
	 * 
	 * @param messageType
	 *            CoAP type of the message
	 * @param host
	 *            destination host in String format
	 * @param port
	 *            destination port
	 * @param path
	 *            path to the resource to which this request is to be sent
	 */
	private CoAPRequest createRequest(CoAPMessageType messageType,
			int messageCode, String host, int port, String path)
			throws CoAPException {

		if (path != null && !path.equals("")) {
			if (!path.startsWith("/")) {
				path = "/" + path;
			}
		}

		URI uri = null;
		try {

			if (port > 0 && port < 65536) {
				uri = new URI("coap", null, host, port, path, null, null);
			} else {
				throw new CoAPException("Port not in range 0-65535");
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new CoAPException(e);
		}

		InetSocketAddress sockaddr = null;
		try {
			String socketAddress = host;
			InetAddress addr = null;
			addr = InetAddress.getByName(socketAddress);

			sockaddr = new InetSocketAddress(addr, uri.getPort());

		} catch (UnknownHostException e) {
			e.printStackTrace();
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
	 * @param host
	 *            destination host in String format
	 * @param port
	 *            destination port
	 * @param path
	 *            path to the resource to which this request is to be sent
	 * @param observer
	 *            the listener class for the callbacks from the observed
	 *            resource
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
			e.printStackTrace();
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
	 * @throws CoAPException
	 */
	public boolean terminateObservationRelationship(CoAPResource resource,
			CoAPResourceObserver observer) throws CoAPException {
		return endpoint.terminateObservationRelationship(resource, observer);
	}

	/**
	 * Send a CoAP request towards the CoAP network
	 * 
	 * @param request
	 *            request to send
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
		incomingMessageHandler = messageHandlerFactory
				.getIncomingCoAPMessageHandler();
		outgoingMessageHandler = messageHandlerFactory
				.getOutgoingCoAPMessageHandler(this.transportLayerSender);

		// get the local endpoint (singleton), add outgoing message handler as
		// listener

		CoAPEndpointFactory endpointFactory = CoAPEndpointFactory.getInstance();
		endpoint = endpointFactory.createLocalCoAPEndpoint(
				outgoingMessageHandler, incomingMessageHandler, address,
				coapPort);

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
		if (address != null && address.isMulticastAddress()
				&& this.coapPort != -1) {
			// If given address is multicast, use multicast socket
			try {

				/*
					CoAPActivator.logger.debug("Join multicast group");
				*/

				this.multicastSocket = new MulticastSocket(this.coapPort);
				this.multicastSocket.joinGroup(address);

				this.transportLayerReceiver = new UDPReceiver(
						this.multicastSocket);

				this.transportLayerSender = new UDPSender(this.multicastSocket);
			} catch (IOException e) {
				e.printStackTrace();
				throw new CoAPException(e);
			}
			// Otherwise use normal UDP datagram socket
		} else {
			try {

				// If the port is set, use the defined port
				if (this.coapPort != -1 && address != null) {
					this.socket = new DatagramSocket(this.coapPort, address);
				} else if (this.coapPort != -1) {
					this.socket = new DatagramSocket(this.coapPort);
				} else {
					this.socket = new DatagramSocket();
				}
			} catch (SocketException e) {
				e.printStackTrace();
				throw new CoAPException(e);
			}
			this.socket.setReuseAddress(true);
			this.socket.setSoTimeout(0);

			// System.out.println("init receiver & sender");
			this.transportLayerReceiver = new UDPReceiver(this.socket);
			this.transportLayerSender = new UDPSender(this.socket);
		}
	}

	/**
	 * If a local service wants to expose its CoAP resources via the CoAP
	 * .well-known/core interface it can use this method. Then the resource will
	 * be included in the resource discovery responses sent by the gateway
	 * 
	 * @param res
	 *            the CoAPResource to be added
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

		if (this.socket != null) {
			this.socket.close();
		}
		if (this.multicastSocket != null) {
			this.multicastSocket.close();
		}

		CoAPEndpointFactory.stopService();
		CoAPMessageHandlerFactory.stopService();

		this.directory.stopService();

		this.transportLayerReceiver.stopService();
		this.transportLayerReceiver = null;
		this.transportLayerSender.stopService();
		this.transportLayerSender = null;

		if (this.timer != null) {
			this.timer.cancel();
		}
		this.outgoingMessageHandler.stopService();
		this.outgoingMessageHandler = null;

		this.endpoint.stopService();
		this.endpoint = null;
	}
}
