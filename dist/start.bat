rmdir /S /Q fwdir
java -Dfile.encoding=UTF-8 -Dorg.knopflerfish.gosg.jars=file:jars/ -jar framework-7.0.1.jar -xargs init.xargs -xargs props.xargs -init
PAUSE