Outline
-------

Want to develop a IoT-Gateway module for some new device? Great! See the 
following tutorial on implementing your code to the GDA.

We'll be using CoAP support found in the IoT-Gateway as a basic example. 
The best way to understand the different functions is to read the source
code, of course, but this should get you started.

1. Contents
-----------

Different components are found in osgi directory. For CoAP it consists of 
following items:

* osgi/adaptor.coap
> CoAP adaptor acts as an interface to the actual CoAP device. It enables GDA
to connect to the adapter.

* osgi/basedriver.coap
> Basedriver handles the actual CoAP request to and from the gateway.

2. CoAP Adaptor
---------------

TODO

3. CoAP Basedriver
------------------

TODO

4. Additional reading
---------------------

* readme.txt under iotgateway/blob/master/osgi/basedriver.coap/doc/ .
* CoAP protocol RFC, http://tools.ietf.org/html/rfc7252