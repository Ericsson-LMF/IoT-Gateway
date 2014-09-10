Outline
-------

Want to develop a IoT-Gateway module for some new CoAP service? Great! See the 
following tutorial on implementing your code to the GDA.

We'll be using CoAP examples found in the IoT-Gateway as a basic example. 
The best way to understand the different functions is to read the source
code, of course, but this should get you started.

1. Contents
-----------

Different components are found in osgi directory. For CoAP it consists of 
following items:

* CoAP Adaptor
> CoAP adaptor acts as an interface to actual CoAP devices. It enables GDA
to connect to the adapter.

* CoAP Basedriver
> Basedriver handles the actual CoAP request to and from the gateway.

2. CoAP Adaptor
---------------
Found in osgi/adaptor.coap

Adaptor is built on top of the CoAP basedriver. It acts as an endpoint to different resources (weatherResource & helloWorld as examples in CoAPDeviceAgent). All new devices should be added here.

A good example of CoAP service is the WeatherResourceImpl that is found inside the adaptor.  


3. CoAP Basedriver
------------------
Found in osgi/basedriver.coap

Includes all necessary elements to communicate with CoAP devices, according to RFC7252 (see section 4).  

4. Additional reading
---------------------

* readme.txt under iotgateway/blob/master/osgi/basedriver.coap/doc/ .
* CoAP protocol RFC, http://tools.ietf.org/html/rfc7252