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

Steps to build the project
Execute "mvn clean install" in the top level folder where the parent POM file exists. This should build all the required modules apart from the ones in the "dist" folder. The dist module can be build separately _after_ building the whole project by executing "mvn clean install" in the dist folder. The dist module needs to be build after the whole project since the building process needs the JAR files from the other modules in order to create the single bundle to be deployed to the target machine. 

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
First build the "dist" module and then run "java -jar framework-X.Y.Z.jar" where X.Y.Z is currenlty 5.3.3. This will run the OSGi framework with a set of example bundles. In the same folder as the framework-X.Y.Z.jar there are two OSGi framework configuration files "init.xargs" and "props.xargs". The "init.xargs" is used for initiating specific framwork bundles apart from the default ones and the "props.xargs" contain directives or properties for the OSGi framework. Please change these files accordingle to suit your requirements. Please also note that user defined ".xargs" files could also be created. e.g. "raspberry_pi.xargs" and then called from "init.xargs" with the following line:
-xargs raspberry_pi.xargs 

--------------
5. TERMINOLOGY
--------------

Host: This is the machine is where the compilation of the project occurs 
Target: This is the machine where the compiled Java bytecode executes


