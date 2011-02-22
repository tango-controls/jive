#!/bin/sh
JAVALIB=/segfs/tango/release/java/lib;
JAVABIN=/segfs/tango/release/java/appli;
CLASSPATH=$JAVALIB/TangORB.jar:$JAVABIN/atkpanel.jar:$JAVALIB/ATKWidget.jar:$JAVALIB/ATKCore.jar:$JAVABIN/LogViewer.jar:$JAVALIB/log4j.jar:$JAVABIN/Astor.jar:$JAVABIN/tool_panels.jar:.
export CLASSPATH
echo $CLASSPATH
echo   Compiling Jive ...
rm jive/*.class
rm jive3/*.class
javac -deprecation jive/MainPanel.java
javac -deprecation jive3/MainPanel.java
javac -deprecation jive3/DbReader.java
javac -deprecation jive3/DbWriter.java
