INSTALL & CONFIGURATION 

- follow instructions from CF related to Device Access API to install & start it
- Do configuration of OSGi modules in deviceaccess/trunk/distribution/framework/configs/config.json
- Disable e.g. the upnp & tutorial & bonjour modules to avoid extra logging in the console
- Set start for basedriver.coap to false for now!
- Start knoplerfish "mvn exec:java"
- When knoplerfish is running, run "start basedriver.coap"
- Install testbundle.coap with "install <path_to_testbundle_jar>"
- Start testbundle.coap with "start testbundle.coap"
- When testbundle.coap is started, it sends requests towards localhost 5863
- Use the CoAP demonstration server from https://github.com/mkovatsc/Californium to reply to the requests (simulates a node having resources)
- you can see logging when typing log (most of the logging will be there, some std.out printouts still there)


- in src/main/resources/META-INF there is a coap.properties file where you can define the local address and port to be used.
 There is also a discovery address, port and interval (in seconds). If the discovery interval is set to 0, no resource discovery requests
 will be sent out. If the local port to be used is random, the COAP_PORT should be set to -1.

API 

- the access point to the api is the CoAPService class e.g. CoAPService coapService = new CoAPService() will get an instance of the localcoapendpoint instance (at the moment static, should be changed when more applications are connecting to the system to the similar way of creating as used in Bonjour at the moment). more understanding what would be feasible way, when several services are using are api is needed! endpoints should probable be possible to run >1 on the gw, but only 1 inc & outgoing messagehandler per endpoint!
- all classes that are part of the api are in package com.ericsson.deviceaccess.coap.basedriver.api and its subpackages
- CoAPRequestListener and CoAPResourceObserver interfaces are used as listener interfaces for receiving callback regarding requests and observation relationships
- messagehandlers store information about outgoing & incoming messages
- they handle duplicate incoming message detection
- they handle retransmission for outgoing messages
- endpoint takes care of req/resp matching (it could be studied if this should be moved to messagehandlers, then it'd be more aligned with the abstract layering from core coap) using the information from the messagehandlers
- endpoint takes care of creating requests that are sent out towards CoAP
- endpoint takes care of caching the incoming responses

TODO list
- error handling tbd! it should be studied how other osgi bundles are handling it
- link format
- if observation framework requires that gw is able to handle requests, how to respond ? Do we need anything from the local services etc..
... 

KNOWN ISSUES

- one package per host at time (defined in the core 07 draft)
- stack is no way finished yet, basic functionality towards Californium working
- package ordering not handled!
+ lots of other stuff
