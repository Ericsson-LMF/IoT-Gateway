# License/Copyright statement here

-------
OUTLINE
-------
This project contains a set of OSGi modules, Linux utilities and OSGi framework files for executing the Generic Device Access(GDA) framework as well as example drivers, adaptors and connectors. For more information about these concepts please refer to the accompanying manual. 

This readme.txt file is organized in the following sections: 
1. Contents of project: Description of the main folders and modules of the project
2. Installation instructions: Description of installation steps for this project
3. Build instructions: Description of the development cycle for this project
4. Execution: Description of how one can  execute of the project and on what platforms
5. Terminology: Description main terms used in this file

-----------
1. CONTENTS
-----------
The project contains the following files and folders:

./common  
This folder contains common bundles used for supporting the GDA framework. It contains a top level maven pom.xml file that builds all the dependent modules.

./dist: 
This folder contains a maven pom.xml file that builds the OSGi framework JAR (framework.X.Y.Z.jar), and the necessary jar files (in ./dist/jars folder) in order to support the developer in preparing a self contained OSGi package for deployment onto a target machine. 

./osgi
This folder contain the main bundles for the Generic Device Access and a few driver, adaptors and connectors. 

./tutorial:
This folder contains a maven project for building a set of bundles for a projet tutorial 

./pom.xml: 
This is the parent Maven POM file for building the whole project. Please note that this POM file does not build the "dist" module. This module has to be build separately.

./readme.txt: this file


---------------
2. INSTALLATION
---------------

The package comes in a compressed file that can be extracted in any place on a host file system. In the instructions below it is assumed that the compressed file is extracted in /opt. It is assumed here that the development takes place on Linux-type of host but a Windows or MacOSX type of host with the appropriate tools. Please see BUILD below for the required tools.  

The target machine can be either the same as the the host machine or completely different. The requirements for the target machine are explained in the the EXECUTION section below. 

--------
3. BUILD
--------

Required tools
------------------
The project was built on a host with the following development environment
* Maven 3.0.4 
* Java 1.6.0_41  
Ii is assumed that any host platform that supports Java 1.6 or later and Maven 3.0.4 or later would be fine. 

Building the bare bones project
-------------------------------
Execute "mvn clean install" in the top level folder where the parent POM file exists. 
This should build all the required modules apart from the ones in the "dist" folder. 
The dist module can be build separately _after_ building the whole project by executing "mvn clean install" in the dist folder. 
The dist module needs to be build after the whole project since the building process needs the JAR files from the other modules in order to create the single bundle to be deployed to the target machine. 
When building the "dist" module the build process creates the environment for a bare bones OSGi environment without the GDA components.
Typically one must install and start the appropriate bundles manually after this step or edit the init.xargs and props.xargs files in order to configure the OSGi environment to install and start the necessary bundles.

Building the tutorial
---------------------
The pom.xml file in the "dist" folder contains a profile called "tutorial" which can be invoked by "mvn -P tutorial clean install". 
This profile will set up the appropriate bundles in the "dist/jars" folder and make sure that the right init.xargs file is in place for running the tutorial. Please note that there are two init*.xargs files which overwrite (depending on the target) the init.xargs file which is used by the execution scripts start.sh and start.bat.

Troubleshooting
---------------
if maven takes too long to start building the typical problem is the missing  HTTP proxy configuration for maven. The solution is to add a proxy configuration in a file "settings.xml" in $HOME/.m2 or in the host machine folder that maven uses for storing already built JAR packages. 
The "settings.xml" file should contain the following XML code

  <proxies>
   <proxy>
      <active>true</active>
      <protocol>http</protocol>
      <host>YOUR_HTTP_PROXY_NAME_OR_IP_ADDRESS</host>
      <port>YOUR_HTTP_PROXY_PORT</port>
    </proxy>
   <proxy>
      <active>true</active>
      <protocol>https</protocol>
      <host>YOUR_HTTPS_PROXY_NAME_OR_IP_ADDRESS</host>
      <port>YOUR_HTTPS_PROXY_PORT</port>
    </proxy>
  </proxies>


------------
4. EXECUTION
------------

Supported platforms
------------------- 
Most of the bundles contain platform independent software. However the developer would like to communicate with devices over e.g. the serial port of the target machine one could use the RXTX JAR file either as a plan JAR file or as a bundle. The RXTX bundle  is part java software (native and non-native) and part native libraries which are platform dependent. Please make sure that the native libraries of the target platform are included in the RXTX bundle otherwise there will be run-time exceptions. 

The project was executed on the following platforms
1) Linux target which is the same as the host machine
* Java 1.6.0_XYZ 
2) Raspberry Pi target
* Java 1.6.0_XYZ 

Command line execution
----------------------
First build the "dist" module and then run "./start.sh on *nix environments or start.bat on Windows.  
This will run the OSGi framework with the bare minimum set of example bundles that don't include the GDA bundles. 
In the same folder as the framework-X.Y.Z.jar there are two OSGi framework configuration files "init.xargs" and "props.xargs". The "init.xargs" is used for initiating specific framwork bundles apart from the default ones and the "props.xargs" contain directives or properties for the OSGi framework. 
Typically one must add manually the additional bundles JAR files in the dist/jars folder and update these .xargs files to make sure that the OSGi environment installs and starts all the desired bundles. However by modifying the pom.xml file to follow a similar practice as the tutorial these manual steps could be automated a little bit using the power ov Maven to resolve dependencies. 
Please also note that user defined ".xargs" files could also be created. e.g. "raspberry_pi.xargs" and then called from "init.xargs" with the following line:
-xargs raspberry_pi.xargs 

 Running the tutorial 
---------------------
The tutorial can be run by invoking ./start.sh on a *nix target or start.bat on a windows target. 
The appropriate init.xargs has already been put in place by the build process.

Testing the tutorial 
---------------------
The tutorial uses a UPnP basedriver, UPnP adaptor, and the an HTTP restlet connector which listens for HTTP requests on port localhost:8090.
The UPnP basedriver discovers UPnP devices in the Local Area Network and the adaptor registers them the GDA framework as GDA devices
One can from a web browser access the webpage "http://localhost:8090/devices" to check which UPnP devices are discovered. 
If none are discovred an empty JSON string will be displayed "{}", otherwise a JSON document with the description of the UPnP devices will be displayed.  

If one would like to test with simulated UPnP devices one can install XBMC 
http://xbmc.org/

or the Intel Developer Tools for UPnP:
http://software.intel.com/en-us/articles/intel-tools-for-upnp-technologies

--------------
5. TERMINOLOGY
--------------

Host: This is the machine is where the compilation of the project occurs 
Target: This is the machine where the compiled Java bytecode executes


