package com.ericsson.deviceaccess.coap.testbundle;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;

import org.osgi.framework.Bundle;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import com.ericsson.deviceaccess.coap.basedriver.api.CoAPException;
import com.ericsson.deviceaccess.coap.basedriver.api.CoAPService;

import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionHeader;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPRequest;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPRequestListener;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPResponse;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPResponseCode;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPMessage.CoAPMessageType;

import com.ericsson.deviceaccess.coap.basedriver.api.resources.CoAPResource;
import com.ericsson.deviceaccess.coap.basedriver.api.resources.CoAPResourceObserver;
import com.ericsson.deviceaccess.coap.basedriver.util.BitOperations;

import com.ericsson.deviceaccess.coap.testbundle.http.CoAPTestServlet;
import com.ericsson.research.ag.util.LogTracker;

public class CoAPServiceTest implements CoAPRequestListener,
		CoAPResourceObserver {

	private LogTracker logger;
	private CoAPService coapService;

	private CoAPMessageType type;
	private String path;
	private String host;
	private int port;
	private CoAPResource res;
	private BundleContext bc;

	private static final String STORAGE_LARGE = "storage/large";

	private LinkedHashMap latestResponses; // this map keeps the latest response
											// regarding a CoAP resource (URI as
											// key)

	public CoAPServiceTest() {
		Bundle b = FrameworkUtil.getBundle(this.getClass());
		bc = b.getBundleContext();
		this.initService();
		latestResponses = new LinkedHashMap();
	}

	public void initService() {
		ServiceReference reference = bc.getServiceReference(CoAPService.class
				.getName());
		if (reference != null) {
			coapService = (CoAPService) bc.getService(reference);
		} else {
			CoAPTestServlet.logger
					.warn("Could not fetch a reference to CoAPService");
		}

		System.out.println("INIT COAPSERVICETEST");
		type = CoAPMessageType.CONFIRMABLE;
		// try all the tests towards Californium demonstration server
		path = "";
		// First do GET to well-known core
		host = "127.0.0.1";
		// host = "ff02::1:fe00:1";
		port = 5683;

		logger = CoAPTestServlet.logger;
	}

	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub

	}

	public void sendLargeGet() throws CoAPException {
		String large = "storage/large";
		CoAPRequest req = this.coapService.createGetRequest(host, port, large,
				type);

		byte[] bytes = new byte[1];
		byte tmpByte = bytes[0];

		byte header = BitOperations.setBitsInByte(tmpByte, 4, 4, 0);
		header = BitOperations.setBitsInByte(header, 3, 1, 0);
		header = BitOperations.setBitsInByte(header, 0, 3, 2);
		bytes[0] = header;

		CoAPOptionHeader option = new CoAPOptionHeader(CoAPOptionName.BLOCK2,
				bytes);

		req.addOptionHeader(option);
		req.setListener(this);

		System.out.println("***** SEND REQUEST TO LARGE RESOURCE *****");
		this.coapService.sendRequest(req);
	}

	private void sendSeparateRequest() throws CoAPException {
		String separate = "separate";
		CoAPRequest req2 = this.coapService.createGetRequest(host, port,
				separate, type);
		req2.setListener(this);
		System.out.println("***** SEND SEPARATE REQUEST *****");
		this.coapService.sendRequest(req2);
	}

	private void createDiscoveryRequest() throws CoAPException {
		path = ".well-known/core";
		CoAPRequest discoveryReq = this.coapService.createGetRequest(host,
				port, path, CoAPMessageType.CONFIRMABLE);
		discoveryReq.setListener(this);
		System.out.println("***** SEND DISCOVERY REQUEST *****");
		this.coapService.sendRequest(discoveryReq);
	}

	private void createObservationRelationShip() throws CoAPException {
		// path = "storage/test";
		path = "helloWorld";
		System.out
				.println("***** SEND OBSERVE REQUEST TOWARDS HELLO WORLD *****");
		this.coapService.createObservationRelationship(host, port, path, this);
	}

	public void piggyPackedResponseReceived(CoAPResponse response,
			CoAPRequest req) {

		System.out.println("piggypacked response received");

		String uriStr = this.readUriPathFromRequest(req);
		this.latestResponses.put(uriStr, response);

		if (this.logger != null) {
			try {
				this.logger
						.debug("Piggy-packed response received at CoAPServiceTest, message type: ["
								+ response.getMessageType().toString()
								+ "] from ["
								+ req.getUriFromRequest().toString()
								+ "], response code ["
								+ CoAPResponseCode.getOptionName(response
										.getCode()) + "]");
				String payload = new String(response.getPayload());
				this.logger.debug("Piggy-packed message payload: [" + payload
						+ "] ");
			} catch (CoAPException e) {
				e.printStackTrace();
			}

			// Check content-type
			List options = response
					.getOptionHeaders(CoAPOptionName.CONTENT_TYPE);

			if (options.size() > 0) {
				CoAPOptionHeader h = (CoAPOptionHeader) options.get(0);

				int code = (int) h.getValue()[0] & 0xff;
				System.out.println("content-type int: " + code);
			}
		}

	}

	public void requestTimeout(CoAPResponse response, CoAPRequest req) {
		if (this.logger != null) {
			this.logger
					.debug("Request timeout response received at CoAPServiceTest");
		}
	}

	public void separateResponseReceived(CoAPResponse response, CoAPRequest req) {

		String uriStr = this.readUriPathFromRequest(req);
		this.latestResponses.put(uriStr, response);

		if (logger != null) {
			try {
				this.logger
						.debug("Separate response received at CoAPServiceTest, message type: ["
								+ response.getMessageType().toString()
								+ "] from ["
								+ req.getUriFromRequest().toString() + "]");
			} catch (CoAPException e) {
				e.printStackTrace();
			}
			String payload = new String(response.getPayload());
			this.logger.debug("Separate message payload: [" + payload + "]");
		}
	}

	public void emptyAckReceived(CoAPResponse response, CoAPRequest req) {
		String uriStr = this.readUriPathFromRequest(req);
		this.latestResponses.put(uriStr, response);

		if (logger != null) {
			logger.debug("Empty ack received at CoAPServiceTest");
		}
	}

	public void maximumRetransmissionsReached(CoAPRequest request) {
		if (logger != null) {
			logger.debug("Maximum nof retransmissions reached");
		}
	}

	public void observeResponseReceived(CoAPResponse response,
			CoAPResource resource) {
		if (logger != null) {
			logger.debug("Observe response received from resource [" + resource
					+ "]");
			logger.debug("Response type: "
					+ response.getMessageType().toString());
			String payload = new String(resource.getContent());
			logger.debug("Observation message payload : [" + payload + "]");
		}
	}

	public void blockWiseResponseReceived(CoAPResponse response,
			CoAPRequest request) {
		String uriStr = this.readUriPathFromRequest(request);
		this.latestResponses.put(uriStr, response);

		if (logger != null) {
			logger.debug("Blockwise response received");
			logger.debug("Response type: "
					+ response.getMessageType().toString());
			String payload = new String(response.getPayload());
			logger.debug("Observation message payload : [" + payload + "]");
		}
	}

	public void serviceBusy(CoAPRequest request) {
		try {
			logger.debug("Service busy, could not send request to : ["
					+ request.getUriFromRequest().toString() + "]");
		} catch (CoAPException e) {
			e.printStackTrace();
		}

	}

	public void observeTerminationReceived(CoAPResource resource) {
		// TODO Auto-generated method stub

	}

	public static String byteToString(int b) {

		String result = new String("{");

		for (int ind = 7; ind >= 0; ind--) {
			if (getBitInInt(b, ind) == 1) {
				result = result.concat("1");
			} else {
				result = result.concat("0");
			}

		}

		result = result.concat("}");
		return result;
	}

	/**
	 * Get value of bit n in an integer (32 bits)
	 * 
	 * @param i
	 *            is a 32-bit value
	 * @param n
	 *            is the bit whose values is to be returned
	 */
	public static byte getBitInInt(int i, int n) {
		return (byte) ((i >> n) & 1);
	}

	public void sendLargePut() throws CoAPException {
		String storageStr = "storage/large";

		String str = "";

		str = str
				+ "/-------------------------------------------------------------\\\n";
		str = str
				+ "|                 RESOURCE BLOCK NO. 1 OF 8                   |\n";
		str = str
				+ "|               [each line contains 64 bytes]                 |\n";
		str = str
				+ "\\-------------------------------------------------------------/\n";
		str = str
				+ "/-------------------------------------------------------------\\\n";
		str = str
				+ "|                 RESOURCE BLOCK NO. 2 OF 8                   |\n";
		str = str
				+ "|               [each line contains 64 bytes]                 |\n";
		str = str
				+ "\\-------------------------------------------------------------/\n";
		str = str
				+ "/-------------------------------------------------------------\\\n";
		str = str
				+ "|                 RESOURCE BLOCK NO. 3 OF 8                   |\n";
		str = str
				+ "|               [each line contains 64 bytes]                 |\n";
		str = str
				+ "\\-------------------------------------------------------------/\n";
		str = str
				+ "/-------------------------------------------------------------\\\n";
		str = str
				+ "|                 RESOURCE BLOCK NO. 4 OF 8                   |\n";
		str = str
				+ "|               [each line contains 64 bytes]                 |\n";
		str = str
				+ "\\-------------------------------------------------------------/\n";
		str = str
				+ "/-------------------------------------------------------------\\\n";
		str = str
				+ "|                 RESOURCE BLOCK NO. 5 OF 8                   |\n";
		str = str
				+ "|               [each line contains 64 bytes]                 |\n";
		str = str
				+ "\\-------------------------------------------------------------/\n";
		str = str
				+ "/-------------------------------------------------------------\\\n";
		str = str
				+ "|                 RESOURCE BLOCK NO. 6 OF 8                   |\n";
		str = str
				+ "|               [each line contains 64 bytes]                 |\n";
		str = str
				+ "\\-------------------------------------------------------------/\n";
		str = str
				+ "/-------------------------------------------------------------\\\n";
		str = str
				+ "|                 RESOURCE BLOCK NO. 7 OF 8                   |\n";
		str = str
				+ "|               [each line contains 64 bytes]                 |\n";
		str = str
				+ "\\-------------------------------------------------------------/\n";
		str = str
				+ "/-------------------------------------------------------------\\\n";
		str = str
				+ "|                 RESOURCE BLOCK NO. 8 OF 8                   |\n";
		str = str
				+ "|               [each line contains 64 bytes]                 |\n";
		str = str
				+ "\\-------------------------------------------------------------/\n";

		byte[] payload = str.getBytes();

		CoAPRequest req = this.coapService.createPutRequest(host, port,
				storageStr, type, payload);
		req.generateTokenHeader();
		
		System.out.println("PUT PAYLOAD LENGTH: " + payload.length);

		byte[] bytes = new byte[1];
		byte tmpByte = bytes[0];

		byte header = BitOperations.setBitsInByte(tmpByte, 4, 4, 0);
		header = BitOperations.setBitsInByte(header, 3, 1, 1);
		header = BitOperations.setBitsInByte(header, 0, 3, 4);
		bytes[0] = header;

		CoAPOptionHeader option = new CoAPOptionHeader(CoAPOptionName.BLOCK1,
				bytes);
		System.out.println("add option header for block1");
		req.addOptionHeader(option);

		req.setListener(this);
		this.coapService.sendRequest(req);
	}

	public void sendLargePost() throws CoAPException {
		System.out.println("Send an update towards large resource");

		String str = "";

		str = str
				+ "/-------------------------------------------------------------\\\n";
		str = str
				+ "|                 Resource Block NO. 1 OF 8                   |\n";
		str = str
				+ "|               [each line contains 64 bytes]                 |\n";
		str = str
				+ "\\-------------------------------------------------------------/\n";
		str = str
				+ "/-------------------------------------------------------------\\\n";
		str = str
				+ "|                 Resource Block NO. 2 OF 8                   |\n";
		str = str
				+ "|               [each line contains 64 bytes]                 |\n";
		str = str
				+ "\\-------------------------------------------------------------/\n";
		str = str
				+ "/-------------------------------------------------------------\\\n";
		str = str
				+ "|                 Resource Block NO. 3 OF 8                   |\n";
		str = str
				+ "|               [each line contains 64 bytes]                 |\n";
		str = str
				+ "\\-------------------------------------------------------------/\n";
		str = str
				+ "/-------------------------------------------------------------\\\n";
		str = str
				+ "|                 Resource Block NO. 4 OF 8                   |\n";
		str = str
				+ "|               [each line contains 64 bytes]                 |\n";
		str = str
				+ "\\-------------------------------------------------------------/\n";
		str = str
				+ "/-------------------------------------------------------------\\\n";
		str = str
				+ "|                 Resource Block NO. 5 OF 8                   |\n";
		str = str
				+ "|               [each line contains 64 bytes]                 |\n";
		str = str
				+ "\\-------------------------------------------------------------/\n";
		str = str
				+ "/-------------------------------------------------------------\\\n";
		str = str
				+ "|                 Resource Block NO. 6 OF 8                   |\n";
		str = str
				+ "|               [each line contains 64 bytes]                 |\n";
		str = str
				+ "\\-------------------------------------------------------------/\n";
		str = str
				+ "/-------------------------------------------------------------\\\n";
		str = str
				+ "|                 Resource Block NO. 7 OF 8                   |\n";
		str = str
				+ "|               [each line contains 64 bytes]                 |\n";
		str = str
				+ "\\-------------------------------------------------------------/\n";
		str = str
				+ "/-------------------------------------------------------------\\\n";
		str = str
				+ "|                 Resource Block NO. 8 OF 8                   |\n";
		str = str
				+ "|               [each line contains 64 bytes]                 |\n";
		str = str
				+ "\\-------------------------------------------------------------/\n";

		byte[] payload = str.getBytes();

		CoAPRequest req = this.coapService.createPutRequest(host, port,
				STORAGE_LARGE, type, payload);
		req.generateTokenHeader();

		byte[] bytes = new byte[1];
		byte tmpByte = bytes[0];

		byte header = BitOperations.setBitsInByte(tmpByte, 4, 4, 0);
		header = BitOperations.setBitsInByte(header, 3, 1, 1);
		header = BitOperations.setBitsInByte(header, 0, 3, 4);
		bytes[0] = header;

		CoAPOptionHeader option = new CoAPOptionHeader(CoAPOptionName.BLOCK1,
				bytes);

		// req.addOptionHeader(option);

		req.setListener(this);
		this.coapService.sendRequest(req);
	}

	private String readUriPathFromRequest(CoAPRequest request) {
		String path = "";
		try {
			URI uri = request.getUriFromRequest();
			path = uri.getPath();
		} catch (CoAPException e) {
			e.printStackTrace();
		}
		return path;
	}

	public void sendPutRequest(String uriStr, byte[] payload) {
		try {
			CoAPRequest req = this.coapService.createPutRequest(host, port,
					uriStr, type, payload);
			req.generateTokenHeader();
			req.setListener(this);
			System.out.println("***** Send PUT request towards [" + uriStr
					+ "] *****");
			this.coapService.sendRequest(req);
		} catch (CoAPException e) {
			e.printStackTrace();
		}
	}

	public void sendGetRequest(String uriStr) {

		try {
			CoAPRequest req = this.coapService.createGetRequest(host, port,
					uriStr, type);
			req.generateTokenHeader();
			req.setListener(this);
			System.out.println("***** Send GET request towards [" + uriStr
					+ "] *****");
			this.coapService.sendRequest(req);
		} catch (CoAPException e) {
			e.printStackTrace();
		}
	}

	public void sendPostRequest(String uriStr, byte[] payload) {
		try {
			CoAPRequest req = this.coapService.createPostRequest(host, port,
					uriStr, type, payload);
			req.generateTokenHeader();
			req.setListener(this);
			System.out.println("***** Send POST request towards [" + uriStr
					+ "] *****");
			this.coapService.sendRequest(req);
		} catch (CoAPException e) {
			e.printStackTrace();
		}
	}

	public void sendDeleteRequest(String uriStr) {

	}

	public CoAPResponse getLatestResponse(String uriStr) {
		CoAPResponse resp = null;
		if (uriStr.endsWith("/status")) {
			uriStr = uriStr.substring(0, uriStr.indexOf("/status"));
		}
		if (this.latestResponses.get(uriStr) != null) {
			resp = (CoAPResponse) this.latestResponses.get(uriStr);
		}
		return resp;
	}

	public void resetResponseReceived(CoAPResponse response, CoAPRequest request) {
		System.out.println("reset response received");
	}
}
