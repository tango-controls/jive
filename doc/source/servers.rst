.. |br| raw:: html

   <br />

.. |clearfloat|  raw:: html

    <div class="clearer"></div>

Manage Servers
**************

Creating a Tango server
=======================


.. figure:: server_menu.jpg
   :align:   left

To Create a server , Open the server creation dialog within the **Edit** menu.

|clearfloat|

.. figure::  server_dialog.jpg
   :align:   left

- Enter the server name including its instance name:|br|
  Ex: Modbus/tra1 where Modbus is the name of the process (the executable name) and tra1 its instance name.Then you can run your server by launching "Modbus tra1".
- Enter the class name
- Enter all devices of this class
- Click on Register server

|clearfloat|

Adding or Removing device to/from an existing server
====================================================

.. figure::  server_adddevice.jpg
   :align:   left

Select the class in the server you want to add a device, then right click on it and select "Add a device". This will ask you for a device name.
Select "Delete" if you want to remove the device. Note that when you remove a device all its properties are also removed.

|clearfloat|

Adding or Removing class to/from an existing server
====================================================

.. figure::  server_addclass.jpg
   :align:   left

Select the server you want to add a class, then right click on it and select "Add class". This will show you the Create/Edit server window with the server name locked. Enter the class and devices name and click "Register Server".

|clearfloat|

Renaming a server
=================

.. figure::  server_rename.jpg
   :align:   left

This is a global rename of a server (executable name), all instances are affected.

|clearfloat|

.. figure::  server_rename_inst.jpg
   :align:   left

This allows to rename the instance of one specific server.

|clearfloat|

.. figure::  server_move.jpg
   :align:   left

This allows to rename the server name and instance name of one specific server.

|clearfloat|


