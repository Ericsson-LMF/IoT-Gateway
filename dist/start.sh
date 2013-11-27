#!/bin/sh
CLASSPATH="framework.jar"

for jar in lib/*.jar ; do
	CLASSPATH="$CLASSPATH:$jar"
done

java -cp $CLASSPATH -Dfile.encoding=UTF-8 -Dorg.knopflerfish.gosg.jars=file:jars/ -jar framework-5.3.3.jar -init -xargs init.xargs -xargs props.xargs $*
