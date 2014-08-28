This project was refactored during the summer of 2014 by a 4-people team from University of Helsinki. You'll find the biggest changes made to the project below, first with an overview and afterwards by folder.


General
-------

1. A new guide for creating a new device has been written, NEW DEVICE.md 
2. README.md has been rewritten to give a more detailed view of the project and to make the tutorial easier to set-up! Added instruction for Raspberry Pi
3. Earlier JSON has been replaced with Jackson.
⋅⋅* No more manually printing JSON
⋅⋅* Supports serialization to XML too.
⋅⋅* JSON reference library is still used in tests to ensure legal JSON just in case
4. Made experimental Gradle build script. Need some more work on manifest files and private imports.
5. Code
⋅⋅* Updated to java 8
⋅⋅* Uses now lambdas heavily
⋅⋅* Updated old non-Collection data structures to Collection ones
⋅⋅* Made collections to use Generics
6. Tests
⋅⋅* JMock to use JUnit4 integration
⋅⋅* Improved reporting on failed Expectations etc.
⋅⋅* Removed references to mockito (wasn't used and JMock is widely used in project)
⋅⋅* JUnit 3 style tests & deprecated JUnit stuff => JUnit 4

7. Documentation
⋅⋅* Javadoc has been rewritten, clarified and improved throughout the project


common
------

1. General utility classes are refactored to common.util
⋅⋅* Some new useful stuff added and redundant removed

dist
----
1. Updated xargs
⋅⋅* Now there is separate profiles/xargs for upnp and coap
⋅⋅* Tutorial profile starts both upnp and coap

osgi
----

1. Codegenerator rewritten using builder pattern
⋅⋅* Generated code simplified
2. UPnP
3. CoAP
⋅⋅* Refactored and updated to RFC7252
⋅⋅* Commented logger calls uncommented and now uses the new logging system

tutorial
--------

1. Refactoring to handle the changes elsewhere
