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

import com.ericsson.commonutil.LegacyUtil;
import com.ericsson.deviceaccess.api.GenericDevice;
import com.ericsson.deviceaccess.coap.basedriver.api.CoAPException;
import com.ericsson.deviceaccess.coap.basedriver.api.CoAPRemoteEndpoint;
import com.ericsson.deviceaccess.coap.basedriver.api.CoAPService;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPMessage;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPRequest;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPRequestListener;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPResponse;
import com.ericsson.deviceaccess.coap.basedriver.api.resources.CoAPObservationResource;
import com.ericsson.deviceaccess.coap.basedriver.api.resources.CoAPResource;
import com.ericsson.deviceaccess.coap.basedriver.api.resources.CoAPResourceObserver;
import java.net.URI;
import java.util.Iterator;
import java.util.Map.Entry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * CoAPDeviceAgent
 *
 * This class is instantiated by the CoAPDeviceFactory when a new CoAP device is
 * discovered. The class creates a HashMap containing the relevant Service
 * Schema (in this case, we use the WeatherResource and HelloWorld schema) in
 * the update() method and also creates and registers a GenericDeviceImpl object
 * with the OSGi framework.
 */
public class CoAPDeviceAgent implements CoAPResourceObserver {

    private final BundleContext context;
    //private LogTracker log;
    private ServiceRegistration devReg;
    private CoAPService coapService;
    private CoAPDevice coap;
    private CoAPResource resource;
    private String path = "";

    public CoAPDeviceAgent(BundleContext bc, CoAPRemoteEndpoint endpoint) {
        this.context = bc;
        //this.log = log;

        // Create an iterator to iterate through the resources a device might have attached to it
        Iterator<Entry<URI, CoAPResource>> it = endpoint.getResources().entrySet().iterator();
        it.forEachRemaining(entry -> {
            URI uri = entry.getKey();
            CoAPResource rsc = entry.getValue();

            // Check for known resources. This is one of the attributes of the GDA:
            // The matching between the device's resources and the service schemas
            // is currently statically defined.
            switch (rsc.getUri().getPath()) {
                case "/weatherResource":
                    System.out.println("[CoAPDeviceAgent]: FOUND WEATHER RESOURCE!");
                    // When a known resource is detected, create a CoAPDevice and attach the resource
                    coap = new CoAPDevice(uri);
                    coap.putService(new WeatherResourceImpl(coap));
                    // Interface with basedriver to retrieve data from the resource
                    try {
                        ServiceReference<CoAPService> reference
                                = context.getServiceReference(CoAPService.class);
                        if (reference != null) {
                            coapService = context.getService(reference);
                        } else {
                            //this.log
                            //      .warn("Could still not fetch a reference to CoAPService");
                        }

                        // Extract the details of the resource from the CoAPResource object
                        path = rsc.getUri().getPath();
                        CoAPRequest req = coapService.createGetRequest(rsc.getUri()
                                .getHost(), rsc.getUri().getPort(),
                                path, CoAPMessage.CoAPMessageType.CONFIRMABLE);
                        // Create inner classes to handle possible responses to CoAP request
                        req.setListener(new CoAPAgentListener());
                        // Below commented out as CoAP can only handle one request at a time
                        // This is how CoAP handles QoS
                        // System.out.println("[CoAPDeviceAgent]: sent message!");
                        // coapService.sendRequest(req);
                    } catch (CoAPException e) {
                        e.printStackTrace();
                    }
                    break;
                case "/helloWorld":
                    // HelloWorld resource is how we test CoAP Observations (subscription/notification)
                    System.out.println("[CoAPDeviceAgent]: FOUND HELLOWORLD RESOURCE!");
                    // can we move the below statement outside of this if/else clause?
                    // When a known resource is detected, create a CoAPDevice and attach the resource
                    coap = new CoAPDevice(uri);
                    coap.putService(new HelloWorldImpl(coap));
                    try {
                        createObservationRelationShip();
                    } catch (CoAPException e) {
                        System.out.println("an exception has occurred");
                    }
                    break;
            }
        });
    }

    private void createObservationRelationShip() throws CoAPException {
        // Create Observation relationship between device and the GDA adaptor/basedriver
        path = "helloWorld";
        System.out.println("***** SEND OBSERVE REQUEST *****");
        coapService.createObservationRelationship(resource.getUri().getHost(), resource.getUri().getPort(), path, this);
    }

    public void observeTerminationReceived(CoAPResource resource) {
        // Handle Observation terminations
        System.out.println("OBSERVER TERMINATION RECEIVED");

    }

    public void observeResponseReceived(CoAPResponse response,
            CoAPResource resource) {
        // Handle Observation responses
        System.out.println("***** OBSERVER RESPONSE RECEIVED *****");
        // if (logger != null) {
        //     logger.debug("Observe response received from resource [" + resource
        // 		 + "]");
        //     logger.debug("Response type: "
        // 		 + response.getMessageType().toString());
        String payload = new String(resource.getContent());
        coap.deviceUpdate(payload);
        //logger.debug("Observation message payload : [" + payload + "]");
    }

    public void update() {
        // String id = info.getPropertyString("deviceid");
        // if(id != null){
        // 	dev.setId(id.replaceAll(":", ""));
        // 	dev.setName(info.getName());
        // 	dev.setProductClass(info.getPropertyString("model"));
        // 	dev.setOnline(true);
        // 	dev.setType(info.getType());

        // 	HashMap services = new HashMap();
        // //	WeatherResource rc = new WeatherResourceImpl(info);
        // 	//services.put(rc.getName(), rc);
        // 	// NOTE: use putService() API call
        // 	dev.setService(services);
        // 	dev.setIcon("http://ergo.ericssonresearch.jp:8080/common/res/images/appletv.png");
        // 	infoMap.put(info.getType(), info);
        // 	//notifyUpdate("/");
        // }
    }

    public void start() {
        // Here we actually register the CoAPDevice object to the OSGi framework allowing the rest of the
        // GDA framework to interact with the CoAPDevice
        //log.info("[CoAPDeviceAgent]: Registering GenericDevImpl");
        devReg = context.registerService(GenericDevice.class, coap, LegacyUtil.toDictionary(coap.getDeviceProperties()));
        coap.setServiceRegistration(devReg);
    }

    public void stop() {
        coap.setOnline(false);
    }

    public void observeTerminationReceived(CoAPRequest arg0, String arg1) {
        // TODO Auto-generated method stub
        // log.error("This method was missing, so just added a stub. ");
    }

    @Override
    public void observationRelationshipTerminated(CoAPResponse resp, CoAPObservationResource res, CoAPRequest req) {
        // TODO
    }

    @Override
    public void observeResponseReceived(CoAPResponse resp, CoAPResource res, CoAPRequest req) {
        //TODO
    }

    public void resetResponseReceived(CoAPResponse resp, CoAPRequest req) {
        //TODO
    }

    private class CoAPAgentListener implements CoAPRequestListener {

        public CoAPAgentListener() {
        }

        @Override
        public void resetResponseReceived(CoAPResponse resp, CoAPRequest req) {
            //TODO
        }

        @Override
        public void emptyAckReceived(CoAPResponse response,
                CoAPRequest request) {
            // TODO Auto-generated method stub

        }

        @Override
        public void maximumRetransmissionsReached(
                CoAPRequest request) {
            // TODO Auto-generated method stub

        }

        @Override
        public void piggyPackedResponseReceived(
                CoAPResponse response, CoAPRequest request) {
            String payload = new String(response.getPayload());
            // System.out.println("[CoAPDeviceAgent]: Piggypacked message payload: ["
            // 		 + payload + "]");

        }

        @Override
        public void separateResponseReceived(
                CoAPResponse response, CoAPRequest request) {
            String payload = new String(response.getPayload());
            // System.out.println("[CoAPDeviceAgent]: Separate message payload: ["
            // 		 + payload + "]");
            coap.deviceUpdate(payload);

        }

        @Override
        public void serviceBusy(CoAPRequest request) {
            // TODO Auto-generated method stub

        }
    }
}
