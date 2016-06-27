.. |br| raw:: html

   <br />

.. |clearfloat|  raw:: html

    <div class="clearer"></div>

Command line
************

Dependencies (CLASSPATH)
------------------------

::

 ASTOR=$TANGO_JAVA_APPLIS/Astor.jar:$TANGO_JAVA_APPLIS/tool_panels.jar
 LOGVIEWER=$TANGO_JAVA_APPLIS/LogViewer.jar
 LOG4J=$TANGO_JAVA_LIBS/log4j.jar
 TANGO=$TANGO_JAVA_LIBS/JTango.jar
 TANGOATK=$TANGO_JAVA_LIBS/ATKCore.jar:$TANGO_JAVA_LIBS/ATKWidget.jar
 ATKPANEL=$TANGO_JAVA_APPLIS/atkpanel.jar
 JIVE=$TANGO_JAVA_APPLIS/Jive.jar
 
 CLASSPATH=$TANGO:$TACO:$TANGOATK:$ATKPANEL:$JIVE:$LOGVIEWER:$LOG4J:$ASTOR

Note: Astor.jar and tool_panels.jar are needed for "Polling thread manager" and "Device dependencies" features.

Command line
------------

::

  java -DTANGO_HOST=host:port jive3.MainPanel

Command line options
--------------------

::

  Usage: jive [-r] [-s server] [-d device]
    -r          Read only mode (No write access to database allowed)
    -s server   Open jive and show specified server node (server=ServerName/instance)
    -d device   Open jive and show specified device node (device=domain/family/member)
    -fs filter  Default server filter
    -fd filter  Default device filter
    -fc filter  Default class filter
    -fa filter  Default alias filter
    -faa filter Default attribute alias filter
    -fp filter  Default property filter


