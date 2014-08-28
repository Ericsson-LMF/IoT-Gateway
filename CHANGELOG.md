This project was refactored during the summer of 2014 by a 4-people team from University of Helsinki. You'll find the biggest changes made to the project below, first with an overview and afterwards by folder.


General
-------

* A new guide for creating a new device has been written, NEW DEVICE.md 
* README.md has been rewritten to give a more detailed view of the project and to make the tutorial easier to set-up! Added instruction for Raspberry Pi
* Earlier JSON has been replaced with Jackson.
⋅⋅* No more manually printing JSON
⋅⋅* Supports serialization to XML too.
⋅⋅* JSON reference library is still used in tests to ensure legal JSON just in case
* Made experimental Gradle build script. Need some more work on manifest files and private imports.
* Code
⋅⋅* Updated to java 8
⋅⋅* Uses now lambdas heavily
⋅⋅* Updated old non-Collection data structures to Collection ones
⋅⋅* Made collections to use Generics
* Tests
⋅⋅* JMock to use JUnit4 integration
⋅⋅* Improved reporting on failed Expectations etc.
⋅⋅* Removed references to mockito (wasn't used and JMock is widely used in project)
⋅⋅* JUnit 3 style tests & deprecated JUnit stuff => JUnit 4

* Documentation
⋅⋅* Javadoc has been rewritten, clarified and improved throughout the project


common
------

* General utility classes are refactored to common.util
⋅⋅* Some new useful stuff added and redundant removed

dist
----
* Updated xargs
⋅⋅* Now there is separate profiles/xargs for upnp and coap
⋅⋅* Tutorial profile starts both upnp and coap

osgi
----

* Codegenerator rewritten using builder pattern
⋅⋅* Generated code simplified
* UPnP
* CoAP
⋅⋅* Refactored and updated to RFC7252
⋅⋅* Commented logger calls uncommented and now uses the new logging system

tutorial
--------

* Refactoring to handle the changes elsewhere
