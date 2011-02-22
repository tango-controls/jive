#!/bin/sh
JAVALIB=/segfs/tango/lib/java;
JAVABIN=/segfs/tango/bin/java;
CLASSPATH=$JAVALIB/OB.jar:$JAVALIB/Tango.jar:$JAVABIN/atkpanel.jar:.
export CLASSPATH
echo $CLASSPATH
echo   Compiling Jive ...
rm jive/*.class
javac -deprecation jive/MainPanel.java
