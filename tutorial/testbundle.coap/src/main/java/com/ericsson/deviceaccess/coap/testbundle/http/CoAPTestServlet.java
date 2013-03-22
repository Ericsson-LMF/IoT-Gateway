package com.ericsson.deviceaccess.coap.testbundle.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;

import com.ericsson.deviceaccess.coap.basedriver.api.CoAPException;
import com.ericsson.deviceaccess.coap.basedriver.api.CoAPService;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPResponse;
import com.ericsson.deviceaccess.coap.testbundle.CoAPServiceTest;

import com.ericsson.research.ag.util.LogTracker;

/**
 * This is a test servlet to run individual requests towards the CoAP basedriver
 * and the Californium CoAP server.
 * 
 * 
 */
public class CoAPTestServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private BundleContext context;
	private HttpService httpService;
	public static LogTracker logger;

	private CoAPService coapService;

	private ServiceRegistration serviceRegistration;
	private CoAPServiceTest coapTest;

	private static final String STORAGE_LARGE = "/storage/large";
	private static final String STATUS = "/status";

	public void activate(ComponentContext cc) throws Exception {
		context = cc.getBundleContext();

		logger = new LogTracker(context);
		logger.open();
		logger.debug("Activate and register CoAPTestServlet");

		httpService.registerServlet("/coaptest", this, null, null);
		coapTest = new CoAPServiceTest();
	}

	public HttpService getHTTPService() {
		return httpService;
	}

	public void deactivate(ComponentContext cc) throws Exception {
		httpService.unregister("/coaptest");
		serviceRegistration.unregister();
	}

	public void setHttpService(HttpService hs) {
		this.httpService = hs;
	}

	public void unsetHttpService(HttpService hs) {
		this.httpService = null;
	}

	/**
	 * doGet method overrides the doGet from the HttpServlet class and sends a
	 * GET request towards the CoAP network
	 * 
	 * Because the responses from the CoAP network might take time because nodes
	 * are sleeping etc, the latest response for each resource is cached. If the
	 * request path ends with "/status", the latest response from the CoAP
	 * network for the particular CoAP resource should be returned. E.g.
	 * /helloworld/status would return the latest response (including the
	 * payload) that was received for coap://localhost:5683/helloworld. Note
	 * however, that the response codes between HTTP and CoAP need to be mapped
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		logger.debug("doGet for [" + req.getPathInfo() + "]");
		String requestPath = req.getPathInfo();

		/*
		 * Because the responses from the CoAP network might take time because
		 * nodes are sleeping etc, the latest response for each resource is
		 * cached. If the request
		 */
		if (requestPath.endsWith(STATUS)) {
			CoAPResponse response = coapTest.getLatestResponse(requestPath);

			if (response.getPayload().length > 0) {
				String test = new String(response.getPayload());
				PrintWriter w = resp.getWriter();
				w.write(test.toCharArray());
				w.flush();
			}
			// TODO content-type
			resp.setContentType("text/plain");
			resp.setStatus(this.respondCode(response.getCode()));

		} else {
			if (requestPath.startsWith("/")) {
				requestPath = requestPath.substring(1);
			}

			coapTest.sendGetRequest(requestPath);
			resp.setStatus(HttpServletResponse.SC_CREATED);
		}
	}

	/**
	 * This method sends a PUT request towards the CoAP network. Note that, if
	 * you use Californium, the PUT method can be used only resources under
	 * "/storage", e.g. "/storage/myresource"
	 */
	public void doPut(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {

		logger.debug("doPut for [" + req.getPathInfo() + "]");
		String requestPath = req.getPathInfo();

		if (requestPath.equals(STORAGE_LARGE)) {
			try {
				logger.debug("send [" + STORAGE_LARGE + "] put");
				coapTest.sendLargePut();
			} catch (CoAPException e) {
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
			resp.setStatus(HttpServletResponse.SC_CREATED);
		} else {
			if (requestPath.startsWith("/")) {
				requestPath = requestPath.substring(1);
			}

			InputStream is = req.getInputStream();
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			int nRead;
			byte[] data = new byte[16384];

			while ((nRead = is.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}

			buffer.flush();
			byte[] payload = buffer.toByteArray();

			coapTest.sendPutRequest(requestPath, payload);
			resp.setStatus(HttpServletResponse.SC_CREATED);
		}
	}

	/**
	 * doPost method can be used for modifying existing resources. Note that not
	 * all the resources existing on the Californium can be modified! But if you
	 * have created resources under "/storage", they can be modified
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		logger.debug("doPost for [" + req.getPathInfo() + "]");
		String requestPath = req.getPathInfo();

		if (requestPath.equals(STORAGE_LARGE)) {
			try {
				logger.debug("send [" + STORAGE_LARGE + "] POST");
				coapTest.sendLargePost();
			} catch (CoAPException e) {
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
			resp.setStatus(HttpServletResponse.SC_CREATED);
		} else {
			if (requestPath.startsWith("/")) {
				requestPath = requestPath.substring(1);
			}

			InputStream is = req.getInputStream();
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			int nRead;
			byte[] data = new byte[16384];

			while ((nRead = is.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}

			buffer.flush();
			byte[] payload = buffer.toByteArray();

			coapTest.sendPostRequest(requestPath, payload);
			resp.setStatus(HttpServletResponse.SC_CREATED);
		}
	}

	private int respondCode(int coapCode) {
		switch (coapCode) {
		case 0:
			return (HttpServletResponse.SC_ACCEPTED);
		case 65:
			return (HttpServletResponse.SC_CREATED);
		case 66:
			return (HttpServletResponse.SC_OK);
		case 67:
			return (HttpServletResponse.SC_OK);
		case 68:
			return (HttpServletResponse.SC_OK);
		case 69:
			return (HttpServletResponse.SC_OK);
		case 128:
			return (HttpServletResponse.SC_BAD_REQUEST);
		case 129:
			return (HttpServletResponse.SC_UNAUTHORIZED);
		case 130:
			return (HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		case 131:
			return (HttpServletResponse.SC_FORBIDDEN);
		case 132:
			return (HttpServletResponse.SC_NOT_FOUND);
		case 133:
			return (HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		case 134:
			return (HttpServletResponse.SC_NOT_ACCEPTABLE);
		case 140:
			return (HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		case 141:
			return (HttpServletResponse.SC_REQUEST_URI_TOO_LONG);
		case 143:
			return (HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
		case 160:
			return (HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		case 161:
			return (HttpServletResponse.SC_NOT_IMPLEMENTED);
		case 162:
			return (HttpServletResponse.SC_BAD_GATEWAY);
		case 163:
			return (HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		case 164:
			return (HttpServletResponse.SC_GATEWAY_TIMEOUT);
		case 165:
			return (HttpServletResponse.SC_UNAUTHORIZED);
		default:
			return (HttpServletResponse.SC_BAD_REQUEST);
		}
	}
}
