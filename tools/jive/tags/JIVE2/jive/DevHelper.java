package jive;

/*****************************************************************************
 *
 * $Revision$
 *
 */
import java.util.Properties;
import java.util.Vector;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Date;



import fr.esrf.Tango.*;
import fr.esrf.TangoDs.*;
import fr.esrf.TangoApi.*;
import org.omg.CORBA.*;	// needed only for exception definition


/*****************************************************************************
 *
 *
 */
public class DevHelper implements TangoConst
{
 String 	id="$Id$";
 DeviceProxy	device;
 String		device_name;
 CommandInfo[] 	commands;
 String[]	attrs;
 AttributeInfo[] attr_configs = new AttributeInfo[0];
 String		client_host;


 public static final String CMD_RESULT_FINISHED = "finished";
 public static final String CMD_INTERNAL = "MP_Jive_Internal";
 public static final String GET_TIMEOUT_CMD = CMD_INTERNAL+"_Get_Timeout";
 public static final String SET_TIMEOUT_CMD = CMD_INTERNAL+"_Set_Timeout";


 /**
  * Constructor importing the device using the specified name and 
  * the database specified by its host machine name and the listening port.
  */
 public DevHelper(String dev_name) throws 	
		DevHelperDevFailed        // import device failed
 {


  //
  // Keep some global informations
  //

  // A record of device to deal with
  this.device_name = dev_name;
  
  // A record of the machine running on
  try
  {
   client_host = InetAddress.getLocalHost().getHostName();
  }
  catch(Exception ignored) {}
 


  //
  // Import the device wanted and get its command list
  //
  import_device();


 }



 /**
  * Import the TANGO Device Server and update variable member
  */
 private void import_device()
        throws 	
		DevHelperDevFailed        // import device failed
 {
  int			nb_commands;
  int			i;
  int			j;



  //
  // Import the TANGO device
  //
  try
  {
   device = new DeviceProxy(device_name);
  }
  catch(DevFailed e)
  {
   throw(new DevHelperDevFailed(
	e.errors[0].reason, 
	e.errors[0].origin,
	e.errors[0].desc));
  }







  //
  // Get the command list for the device (i.e. proceed the
  // command command_list_query() on the device itself)
  //
  CommandInfo[] dev_commands = null;
  try
  {
   dev_commands = device.command_list_query();
  }
  catch(DevFailed e)
  {
   throw(new DevHelperDevFailed(
	e.errors[0].reason, 
	e.errors[0].origin,
	e.errors[0].desc));
  }


  // Get the number of commands of this device
  nb_commands = dev_commands.length;

  //
  // Add CORBA operations available on all devices
  //
  commands = new CommandInfo[nb_commands + 3];
  DevCmdInfo tmp_cmd = new DevCmdInfo();
  
  // Add the CORBA commands
  i=0;
    
  tmp_cmd.cmd_name      = "BlackBox";
  tmp_cmd.in_type       = Tango_DEV_LONG;
  tmp_cmd.in_type_desc  = "Nb commands";
  tmp_cmd.out_type      = Tango_DEVVAR_STRINGARRAY;
  tmp_cmd.out_type_desc = "Last commands";
  commands[i++] = new CommandInfo(tmp_cmd);

  tmp_cmd.cmd_name      = "Info";
  tmp_cmd.in_type       = Tango_DEV_VOID;
  tmp_cmd.in_type_desc  = "";
  tmp_cmd.out_type      = Tango_DEV_STRING;
  tmp_cmd.out_type_desc = "Device infos";
  commands[i++] = new CommandInfo(tmp_cmd);
  
  tmp_cmd.cmd_name      = "Ping";
  tmp_cmd.in_type       = Tango_DEV_VOID;
  tmp_cmd.in_type_desc  = "";
  tmp_cmd.out_type      = Tango_DEV_STRING;
  tmp_cmd.out_type_desc = "Device alive";
  commands[i++] = new CommandInfo(tmp_cmd);

  // Copy the device specific commands
  for (j=0; j<nb_commands; j++)
   commands[j+i] = dev_commands[j];



  //
  // Get the attribute list of the device and
  // Get attribute properties  for all attributes
  //
  try
  {
   attrs = new String[1];
   attrs[0] = Tango_AllAttr;
   attr_configs = device.get_attribute_config(attrs);

   int nb_attrs = attr_configs.length;
   attrs = new String[nb_attrs];
   for(i=0;i<nb_attrs;i++)
     attrs[i] = attr_configs[i].name;
  }
  catch(DevFailed e)
  {
   if(e.errors[0].reason.equals("API_AttrNotFound"))
    attrs = new String[0];
   else
    throw(new DevHelperDevFailed(
        e.errors[0].reason,
        e.errors[0].origin,
        e.errors[0].desc));
  } 
 }




 /**
  * Get the device command list
  */
 public CommandInfo[] command_list_query()
 {
  return commands;
 }



 /**
  * Get the device attribute list
  */
 public String[] get_attribute_list()
 {
  return attrs;
 }



 /**
  * Get the attribute properties
  */
 public AttributeInfo[] get_attribute_config()
 {
  return attr_configs;
 }




 /**
  * Get the device name
  */
 public String get_device_name()
 {
  return device_name;
 }




 /**
  * Execute a command on the device with the specified argin
  */
 public String exec_command(
	String command_name, 
	String argin_string) throws
		DevHelperDevFailed,     // command execution on device failed
		NumberFormatException	// string to any convertion failed
 {
  return exec_command(
	command_name,
	argin_string,
	-1);
 }

 public String exec_command(
	String command_name, 
	String argin_string,
	int    answer_limit) throws
		DevHelperDevFailed,     // command execution on device failed
		NumberFormatException	// string to any convertion failed
 {
  int		index;
  String 	ret_client_string;

  long		t_before = 0;
  long		t_after  = 0;
  String	argout_string = new String("");

  DeviceData	send;
  DeviceData	received;

  boolean       comm_failure;
  int           nb_retry = 0;
  boolean       list_reimport = false;
  boolean       comm_reimport = false;




  //
  // Found the command information
  //
  for(
	index=0; 
	(index<commands.length) && 
	!command_name.equals(commands[index].cmd_name); 
	index++);


  //
  // If command not found in the current command list then
  // try once to update the command list (perhaps the DS is under
  // design and its command list has changed)
  //
  if(index >= commands.length)
  {
   import_device();
   list_reimport = true;

   // Found the command information
   // NOTE: need to repeat the following code instead of putting it
   // in a loop because the commands[] update was not correctly reported.
   for(
        index=0;
        (index<commands.length) &&
        !command_name.equals(commands[index].cmd_name);
        index++);

   // If the requested command doesn't exist in the list then inform the client
   if(index >= commands.length)
    throw(new DevHelperDevFailed(
        new String("unknown command for that device"),
        new String("DevHelper"),
	new String("unknown command even after a re-import of the device")));
  }




  //
  // Convert the argin string to TANGO DeviceData type according to command
  // specific argin type.
  //

  // Prepare the argin structure for the command to execute
  try
  {
   send = new DeviceData();
  }
  catch(DevFailed e)
  {
   throw(new DevHelperDevFailed(
	e.errors[0].reason, 
	e.errors[0].origin,
	e.errors[0].desc));
  }

  // Convert and parse the string 
  Vector   argin_array = new Vector();
  if(HtmlHelper.string_to_devicedata(
        argin_string,
        send,
        commands[index].in_type,
        argin_array) == -1)
  {
   String message = new String("");
   for(int i=0; i < argin_array.size(); i++)
    message += (String)argin_array.elementAt(i);

   throw new NumberFormatException(message);
  }



  //
  // Execute the command
  //
  nb_retry = 0;
  do
  {
   comm_failure = false;

   if(command_name.equals("BlackBox"))
   {
    try
    {
     String[] dummy = null;
     t_before = System.currentTimeMillis();
     dummy = device.black_box(Integer.parseInt(argin_string));
     t_after  = System.currentTimeMillis();
 
     // Convert argout to string
     for(int i=0; i<dummy.length; i++)
      argout_string += dummy[i] + "\n";
    }
    catch(DevFailed e)
    {
     if(((e.errors[0].reason.toUpperCase().indexOf("COMM_FAILURE") != -1) ||
        (e.errors[0].reason.toUpperCase().indexOf("OBJECT_NOT_EXIST") != -1) ||
        (e.errors[0].reason.toUpperCase().indexOf("TRANSIENT") != -1)) &&
	(comm_reimport == false))
      comm_failure = true;
     else
      throw(new DevHelperDevFailed(
	e.errors[0].reason, 
	e.errors[0].origin,
	e.errors[0].desc));
    }
   }
   else if(command_name.equals("Info"))
   {
    try
    {
     DevInfo dummy = null;

     t_before = System.currentTimeMillis();
     dummy = device.info();
     t_after  = System.currentTimeMillis();
 
     // Convert argout to string
     argout_string += "device class:   " + dummy.dev_class + "\n";
     argout_string += "server id:      " + dummy.server_id + "\n";
     argout_string += "server host:    " + dummy.server_host + "\n";
     argout_string += "server version: " + dummy.server_version + "\n";
     argout_string += "doc URL:        " + dummy.doc_url+ "\n";
    }
    catch(DevFailed e)
    {
     if(((e.errors[0].reason.toUpperCase().indexOf("COMM_FAILURE") != -1) ||
        (e.errors[0].reason.toUpperCase().indexOf("OBJECT_NOT_EXIST") != -1) ||
        (e.errors[0].reason.toUpperCase().indexOf("TRANSIENT") != -1)) &&
	(comm_reimport == false))
      comm_failure = true;
     else
      throw(new DevHelperDevFailed(
	e.errors[0].reason, 
	e.errors[0].origin,
	e.errors[0].desc));
    }
   }
   else if(command_name.equals("Ping"))
   {
    try
    {
     t_before = System.currentTimeMillis();
     device.ping();
     t_after  = System.currentTimeMillis();
     argout_string = "Device ALIVE";
    }
    catch(DevFailed e)
    {
     if(((e.errors[0].reason.toUpperCase().indexOf("COMM_FAILURE") != -1) ||
        (e.errors[0].reason.toUpperCase().indexOf("OBJECT_NOT_EXIST") != -1) ||
        (e.errors[0].reason.toUpperCase().indexOf("TRANSIENT") != -1)) &&
	(comm_reimport == false))
      comm_failure = true;
     else
     {
      argout_string = "Device NOT ALIVE";
      t_after = t_before = 0;
     }
    }
   }
   else
   {
    try
    {
     t_before = System.currentTimeMillis();
     received = device.command_inout(command_name, send);
     t_after  = System.currentTimeMillis();

     // Convert argout from TANGO Any type to string according to command
     // specific argout type.
     argout_string = HtmlHelper.devicedata_to_string(
	received, 
	commands[index].out_type,
	answer_limit);
    }
    catch(DevFailed e)
    {
     if(((e.errors[0].reason.toUpperCase().indexOf("COMM_FAILURE") != -1) ||
         (e.errors[0].reason.toUpperCase().indexOf("OBJECT_NOT_EXIST") != -1) ||
         (e.errors[0].reason.toUpperCase().indexOf("TRANSIENT") != -1)) &&
	(comm_reimport == false))
      comm_failure = true;
     else
      throw(new DevHelperDevFailed(
	e.errors[0].reason, 
	e.errors[0].origin,
	e.errors[0].desc));
    }


   }


   // Try to reconnect to the device the first time this one doesn't answer,
   // if it's not there two times then it's probably definitevely dead.
   if((nb_retry == 0) && comm_failure)
   {
    import_device();
    comm_reimport = true;
   }

     nb_retry++;
  }
  while((nb_retry<2) && comm_failure);








  //
  // Prepare string to return (include extra informations to command argout)
  //

  // Prepare Java date convertion stuff
  Date date = new Date();
  SimpleDateFormat dateformat =
        new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
  //dateformat.setTimeZone(TimeZone.getTimeZone("ECT"));
  dateformat.setTimeZone(TimeZone.getDefault());

  // Notify a re-import of the device
  String reimport = "";
  if(list_reimport || comm_reimport)
   reimport="   WARNING: device re-imported";

  // Construct the string
  ret_client_string = 
        "\n###################################\n" +
        dateformat.format(date) + reimport + "\n" +
        "From host   : " + client_host + "\n" +
        device_name + ": " + command_name + "\n" +
        "Duration    : " + Long.toString(t_after-t_before) + " (ms)\n";
  
  // Prepare string to append
  if(commands[index].in_type != Tango_DEV_VOID)
  {
   String ret_dummy = new String();
   for(int i=0; i < argin_array.size(); i++)
      ret_dummy += (String)argin_array.elementAt(i) + "\n";

   ret_client_string += "In  Argument(s):\n" + ret_dummy;
  }

  if(commands[index].out_type != Tango_DEV_VOID)
   ret_client_string += "Out Argument(s):\n" + argout_string;



  return ret_client_string;
 }







 /**
  * Read the specified attribute from the device
  */
 public String read_attr(String attr_name) throws
			DevHelperDevFailed
 {
  int			index;
  String 		ret_client_string;
  String		argout_string = new String("");

  long			t_before = 0;
  long			t_after  = 0;
  DeviceAttribute	received;

  int           	nb_retry = 0;
  boolean       	comm_failure;
  boolean       	comm_reimport = false;






  //
  // Found the attribute information
  //
  for(
	index=0; 
	(index<attr_configs.length) && 
	!attr_name.equals(attr_configs[index].name); 
	index++);



  //
  // If the attribute is not found in the current list then
  // try once to update the list (perhaps the DS is under
  // design and its attribute list has changed)
  //
  if(index >= attr_configs.length)
  {
   // TODO
  }



  //
  // Read the Attribute
  //
  nb_retry = 0;
  do
  {
   comm_failure = false;

   try
   {
    t_before = System.currentTimeMillis();
    received = device.read_attribute(attr_name);
    t_after  = System.currentTimeMillis();

    // Convert argout from TANGO type to string according to attribute
    // specific type.
    argout_string = HtmlHelper.deviceattribute_to_string(
	received, 
	attr_configs[index].writable,
	attr_configs[index].data_format,
	attr_configs[index].data_type);
   }
   catch(DevFailed e)
   {
    if(((e.errors[0].reason.toUpperCase().indexOf("COMM_FAILURE") != -1) ||
        (e.errors[0].reason.toUpperCase().indexOf("OBJECT_NOT_EXIST") != -1) ||
        (e.errors[0].reason.toUpperCase().indexOf("TRANSIENT") != -1)) &&
	(comm_reimport == false))
     comm_failure = true;
    else
     throw(new DevHelperDevFailed(
	e.errors[0].reason, 
	e.errors[0].origin,
	e.errors[0].desc));
   }




   // Try to reconnect to the device the first time this one doesn't answer,
   // if it's not there two times then it's probably definitevely dead.
   if((nb_retry == 0) && comm_failure)
   {
    import_device();
    comm_reimport = true;
   }

   nb_retry++;
  }
  while((nb_retry<2) && comm_failure);








  //
  // Prepare string to return (include extra informations to command argout)
  //

  // Prepare Java date convertion stuff
  Date date = new Date();
  SimpleDateFormat dateformat =
        new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
  //dateformat.setTimeZone(TimeZone.getTimeZone("ECT"));
  dateformat.setTimeZone(TimeZone.getDefault());

  // Notify a re-import of the device
  String reimport = "";
  if(comm_reimport)
   reimport="   WARNING: device re-imported";

  // Construct the string
  ret_client_string = 
        "\n###################################\n" +
        dateformat.format(date) + reimport + "\n" +
        "From host   : " + client_host + "\n" +
        device_name + ": " + attr_name + "\n" +
        "Duration    : " + Long.toString(t_after-t_before) + " (ms)\n";
  
  ret_client_string += "Out Argument(s):\n" + argout_string;


  return ret_client_string;
 }




 /**
  * Read the specified attribute from the device
  */
 public String write_attr(	
	String attr_name,
	String argin_string) throws
			DevHelperDevFailed
 {
  int			index;
  String 		ret_client_string;
  String		argout_string = new String("");

  long			t_before = 0;
  long			t_after  = 0;
  DeviceAttribute	send = null;

  int           	nb_retry = 0;
  boolean       	comm_failure;
  boolean       	list_reimport = false;
  boolean       	comm_reimport = false;






  //
  // Found the attribute information
  //
  for(
	index=0; 
	(index<attr_configs.length) && 
	!attr_name.equals(attr_configs[index].name); 
	index++);

  //
  // If the attribute is not found in the current list then
  // try once to update the list (perhaps the DS is under
  // design and its attribute list has changed)
  //
  if(index >= attr_configs.length)
  {
   import_device();
   list_reimport = true;

   // Found the attribute information
   for(
        index=0; 
        (index<attr_configs.length) && 
        !attr_name.equals(attr_configs[index].name);
        index++);


   //If the requested attribute doesn't exist in the list then inform the client
   if(index >= attr_configs.length)
    throw(new DevHelperDevFailed(
        new String("unknown attribute for that device"),
        new String("DevHelper"),
	new String("unknown attribute even after a re-import of the device")));
  }


  //
  // Convert the argin string to TANGO DeviceAttribute type 
  //

  // Convert and parse the string 
  Vector   argin_array = new Vector();
  send = new DeviceAttribute(attr_name,0);
  if(HtmlHelper.string_to_deviceattribute(
        argin_string,
        send,
	attr_configs[index].data_format,
	attr_configs[index].data_type,
        argin_array) == -1)
  {
   String message = new String("");
   for(int i=0; i < argin_array.size(); i++)
    message += (String)argin_array.elementAt(i);

   throw new NumberFormatException(message);
  }


  //
  // Write the Attribute
  //
  nb_retry = 0;
  do
  {
   comm_failure = false;

   try
   {
    t_before = System.currentTimeMillis();
    device.write_attribute(send);
    t_after  = System.currentTimeMillis();
   }
   catch(DevFailed e)
   {
    if(((e.errors[0].reason.toUpperCase().indexOf("COMM_FAILURE") != -1) ||
        (e.errors[0].reason.toUpperCase().indexOf("OBJECT_NOT_EXIST") != -1) ||
        (e.errors[0].reason.toUpperCase().indexOf("TRANSIENT") != -1)) &&
	(comm_reimport == false))
     comm_failure = true;
    else
     throw(new DevHelperDevFailed(
	e.errors[0].reason, 
	e.errors[0].origin,
	e.errors[0].desc));
   }


   // Try to reconnect to the device the first time this one doesn't answer,
   // if it's not there two times then it's probably definitevely dead.
   if((nb_retry == 0) && comm_failure)
   {
    import_device();
    comm_reimport = true;
   }

   nb_retry++;
  }
  while((nb_retry<2) && comm_failure);







  //
  // Prepare string to return (include extra informations to command argout)
  //

  // Prepare Java date convertion stuff
  Date date = new Date();
  SimpleDateFormat dateformat =
        new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
  //dateformat.setTimeZone(TimeZone.getTimeZone("ECT"));
  dateformat.setTimeZone(TimeZone.getDefault());

  // Notify a re-import of the device
  String reimport = "";
  if(comm_reimport)
   reimport="   WARNING: device re-imported";

  // Construct the string
  ret_client_string = 
        "\n###################################\n" +
        dateformat.format(date) + reimport + "\n" +
        "From host   : " + client_host + "\n" +
        device_name + ": " + attr_name + "\n" +
        "Duration    : " + Long.toString(t_after-t_before) + " (ms)\n";
  

  return ret_client_string;
 }







 /**
  * Set the communication timeout to the device server
  */
 public int get_timeout() throws
		DevHelperDevFailed     // command execution on device failed
 {
  boolean       comm_failure;
  int           nb_retry = 0;
  boolean       comm_reimport = false;
  int		ret_value = -1;


  //
  // Execute the command
  //
  nb_retry = 0;
  do
  {
   comm_failure = false;

   try
   {
    ret_value = device.get_timeout_millis();
   }
   catch(DevFailed e)
   {
    if(((e.errors[0].reason.toUpperCase().indexOf("COMM_FAILURE") != -1) ||
     (e.errors[0].reason.toUpperCase().indexOf("OBJECT_NOT_EXIST") != -1) ||
     (e.errors[0].reason.toUpperCase().indexOf("TRANSIENT") != -1)) &&
     (comm_reimport == false))
     comm_failure = true;
    else
     throw(new DevHelperDevFailed(
	e.errors[0].reason, 
	e.errors[0].origin,
	e.errors[0].desc));
   }


   // Try to reconnect to the device the first time this one doesn't answer,
   // if it's not there two times then it's probably definitevely dead.
   if((nb_retry == 0) && comm_failure)
   {
    import_device();
    comm_reimport = true;
   }

     nb_retry++;
  }
  while((nb_retry<2) && comm_failure);


  return ret_value;
 }





 /**
  * Set the communication timeout to the device server
  */
 public String set_timeout( String argin_string) throws
		DevHelperDevFailed,     // command execution on device failed
		NumberFormatException	// string convertion failed
 {
  boolean       comm_failure;
  int           nb_retry = 0;
  boolean       comm_reimport = false;

  String 	ret_client_string;
  long		t_before = 0;
  long		t_after  = 0;


  // Convert the argin string
  int new_timeout = Integer.parseInt(argin_string);



  //
  // Execute the command
  //
  nb_retry = 0;
  do
  {
   comm_failure = false;

   try
   {
    t_before = System.currentTimeMillis();
    device.set_timeout_millis(new_timeout);
    t_after  = System.currentTimeMillis();
   }
   catch(DevFailed e)
   {
    if(((e.errors[0].reason.toUpperCase().indexOf("COMM_FAILURE") != -1) ||
     (e.errors[0].reason.toUpperCase().indexOf("OBJECT_NOT_EXIST") != -1) ||
     (e.errors[0].reason.toUpperCase().indexOf("TRANSIENT") != -1)) &&
     (comm_reimport == false))
     comm_failure = true;
    else
     throw(new DevHelperDevFailed(
	e.errors[0].reason, 
	e.errors[0].origin,
	e.errors[0].desc));
   }


   // Try to reconnect to the device the first time this one doesn't answer,
   // if it's not there two times then it's probably definitevely dead.
   if((nb_retry == 0) && comm_failure)
   {
    import_device();
    comm_reimport = true;
   }

     nb_retry++;
  }
  while((nb_retry<2) && comm_failure);


  //
  // Prepare string to return (include extra informations to command argout)
  //

  // Notify a re-import of the device
  String reimport = "";
  if(comm_reimport)
   reimport="   WARNING: device re-imported";

  // Prepare Java date convertion stuff
  Date date = new Date();
  SimpleDateFormat dateformat =
        new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
  //dateformat.setTimeZone(TimeZone.getTimeZone("ECT"));
  dateformat.setTimeZone(TimeZone.getDefault());

  // Construct the string
  ret_client_string = 
        "\n###################################\n" +
        dateformat.format(date) + reimport + "\n" +
        "From host   : " + client_host + "\n" +
        device_name + ": setting communication timeout" + "\n" +
        "Duration    : " + Long.toString(t_after-t_before) + " (ms)\n";

  ret_client_string += "In  Argument(s):\n" + new_timeout + "\n";

  return ret_client_string;
 }
}
