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
package com.ericsson.deviceaccess.coap;

/**
 * CoAPDeviceFactory
 *
 * Here we listen to the basedriver via the DeviceInterface interface for the detection of new devices.
 * This class implements the DeviceInterface interface and provides the implementation of the deviceAdded() method.
 * Currently the class simply creates a new CoAPDeviceAgent object which subsequently handles the registration of
 * the object to the OSGi framework.
 */

import com.ericsson.deviceaccess.coap.basedriver.api.CoAPRemoteEndpoint;
import com.ericsson.deviceaccess.coap.basedriver.api.CoAPService;
import com.ericsson.deviceaccess.coap.basedriver.api.DeviceInterface;
import com.ericsson.deviceaccess.coap.basedriver.api.resources.CoAPResource;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.util.HashMap;

public class CoAPDeviceFactory implements BundleActivator, DeviceInterface {
    private HashMap agents;
    //private LogTracker logger;
    private BundleContext context;
    private CoAPService coapService;

    // implement servicetracker
    public void start(BundleContext context) {
        this.context = context;
        this.agents = new HashMap();
        //logger = new LogTracker(context);
        //logger.open();
        //logger.debug("Starting CoAP device factory");

        context.registerService(DeviceInterface.class.getName(), this, null);

        ServiceReference reference = context
                .getServiceReference(CoAPService.class.getName());
        if (reference != null) {
            coapService = (CoAPService) context.getService(reference);
        } else {
          //  this.logger.warn("Could not fetch a reference to CoAPService");
        }
    }

    public void stop(BundleContext context) {
    }

    public void coapResourceAdded(CoAPResource resource) {
        // 	System.out.println("NEW RESOURCE ADDED: "
        // 			+ resource.getUri().getPath().toString());

        // 	if (resource.getUri().getPath().equals("/weatherResource")) {

        // 		CoAPRequest req = null;
        // 		try {

        // 			// if (coapService == null) {
        // 			ServiceReference reference = context
        // 					.getServiceReference(CoAPService.class.getName());
        // 			if (reference != null) {
        // 				coapService = (CoAPService) context.getService(reference);
        // 			} else {
        // 				this.logger
        // 						.warn("Could still not fetch a reference to CoAPService");
        // 			}
        // 			// }

        // 			String path = resource.getUri().getPath();
        // 			req = coapService.createGetRequest(resource.getUri().getHost()
        // 					.toString(), resource.getUri().getPort(), path,
        // 					CoAPMessage.CoAPMessageType.CONFIRMABLE);

        // 			req.setListener(new CoAPRequestListener() {
        // 				public void emptyAckReceived(CoAPResponse response,
        // 						CoAPRequest request) {
        // 					// TODO Auto-generated method stub

        // 				}

        // 				public void maximumRetransmissionsReached(
        // 						CoAPRequest request) {
        // 					// TODO Auto-generated method stub

        // 				}

        // 				public void piggyPackedResponseReceived(
        // 						CoAPResponse response, CoAPRequest request) {
        // 					String payload = new String(response.getPayload());
        // 					logger.debug("Piggypacked message payload: [" + payload
        // 							+ "]");

        // 				}

        // 				public void requestTimeout(CoAPResponse response,
        // 						CoAPRequest request) {
        // 					// TODO Auto-generated method stub

        // 				}

        // 				public void separateResponseReceived(CoAPResponse response,
        // 						CoAPRequest request) {
        // 					String payload = new String(response.getPayload());
        // 					logger.debug("Separate message payload: [" + payload
        // 							+ "]");

        // 				}

        // 				public void serviceBusy(CoAPRequest request) {
        // 					// TODO Auto-generated method stub

        // 				}
        // 			});
        // 			coapService.sendRequest(req);

        // 		} catch (CoAPException e) {
        // 			e.printStackTrace();
        // 		}

        // 	}
        // 	// String localDeviceId = getLocalDeviceId(resource);
        // 	// CoAPDeviceAgent agent = (CoAPDeviceAgent) agents.get(localDeviceId);

    }

    public void coapResourceRemoved(CoAPResource resource) {
        // TODO Auto-generated method stub

    }

    public void deviceAdded(CoAPRemoteEndpoint endpoint) {
        // Method invoked by basedriver when new device is detected
        // CoAPRemoteEndpoint class is a description of the device + its resources
        System.out.println("[CoAPDeviceFactory]: Remote endpoint of type ["
                + endpoint.getEndpointType() + "] found");
        //logger.debug("Creating agent for " + endpoint.getEndpointType());
        CoAPDeviceAgent agent = new CoAPDeviceAgent(context, endpoint);
        agents.put("001", agent);
        agent.start();


    }

    public void deviceRemoved(CoAPRemoteEndpoint endpoint) {
        // TODO Auto-generated method stub

    }

}