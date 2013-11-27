rmdir /S /Q fwdir
java -Dfile.encoding=UTF-8 -Dorg.knopflerfish.gosg.jars=file:jars/ -jar framework.jar -xargs init.xargs -xargs props.xargs -init
