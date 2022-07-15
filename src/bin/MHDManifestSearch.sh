#!/bin/sh
# -------------------------------------------------------------------------
# MHDSEND  Launcher
# -------------------------------------------------------------------------

MAIN_CLASS=kr.irm.fhir.MHDManifestSearch

DIRNAME="`dirname "$0"`"

# Setup $MHDMANIFESTSEARCH_HOME
if [ "x$MHDMANIFESTSEARCH_HOME" = "x" ]; then
    MHDMANIFESTSEARCH_HOME=`cd "$DIRNAME"/..; pwd`
fi

# Setup the JVM
if [ "x$JAVA_HOME" != "x" ]; then
    JAVA=$JAVA_HOME/bin/java
else
    JAVA="java"
fi

# Setup the classpath
CP="$MHDMANIFESTSEARCH_HOME/etc/MHDManifestSearch/"
for s in $MHDMANIFESTSEARCH_HOME/lib/*.jar
do
	CP="$CP:$s"
done

# Execute the JVM

exec $JAVA $JAVA_OPTS -cp "$CP" $MAIN_CLASS "$@"