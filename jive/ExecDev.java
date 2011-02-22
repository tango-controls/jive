/* Class ExecDev (from Jive original by M.Perez) */
/* Port to Swing and Tango2 by JLP               */

package jive;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.File;
import java.util.Vector;
import java.util.Properties;
import java.lang.Object;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Date;

/***************************************************************************** 
 *
 *
 */
class CmdInfo
{
 public CmdInfo() { }

 public CmdInfo(String cmd_name,
               int cmd_tag,
               int in_type,
               int out_type,
               String in_type_desc,
               String out_type_desc)
 {
  this.cmd_name      = cmd_name;
  this.cmd_tag       = cmd_tag;
  this.in_type       = in_type;
  this.out_type      = out_type;
  this.in_type_desc  = in_type_desc;
  this.out_type_desc = out_type_desc;
 }

 public String cmd_name;
 public int cmd_tag;
 public int in_type;
 public int out_type;
 public String in_type_desc;
 public String out_type_desc;

} // end of class CmdInfo

/***************************************************************************** 
 *
 * The aim of this class which is a redefinition of Tango AttributeInfo
 * class is to avoid having any Tango dependency when running as an Applet
 *
 */
class AttrInfo
{
 public AttrInfo() { }

 public AttrInfo(
	String name,
	String writable,
	String data_format,
	String data_type,
	int    data_type_num,
	String max_dim_x,
	String max_dim_y,
	String description,
	String label,
	String unit,
	String standard_unit,
	String display_unit,
	String format,
	String min_value,
	String max_value,
	String min_alarm,
	String max_alarm)
 {
  this.name          = name;
  this.writable      = writable;
  this.data_format   = data_format;
  this.data_type     = data_type;
  this.data_type_num = data_type_num;
  this.max_dim_x     = max_dim_x;
  this.max_dim_y     = max_dim_y;
  this.description   = description;
  this.label         = label;
  this.unit          = unit;
  this.standard_unit = standard_unit;
  this.display_unit  = display_unit;
  this.format        = format;
  this.min_value     = min_value;
  this.max_value     = max_value;
  this.min_alarm     = min_alarm;
  this.max_alarm     = max_alarm;
 }

 public String name;
 public String writable;
 public String data_format;
 public String data_type;
 public int    data_type_num;
 public String max_dim_x;
 public String max_dim_y;
 public String description;
 public String label;
 public String unit;
 public String standard_unit;
 public String display_unit;
 public String format;

 public String min_value;
 public String max_value;
 public String min_alarm;
 public String max_alarm;

} // end of class AttrInfo

/***************************************************************************** 
 *
 */
 
public class ExecDev
        extends
                JPanel
        implements
                ActionListener,
                ListSelectionListener,
		MouseListener

{
 Thread			thread;
 String			client_host  = null;

 // TANGO device stuff
 DevHelper		device;
 String			device_name = null;
 String			session_id = null;
 int			nb_commands;
 CmdInfo[] 		commands;
 String[]		attrs;
 AttrInfo[]		attr_configs;
 final String		EXEC_SERVLET_NAME = "/jive/servlet/CmdExec";
 final String		SEP_COMMAND_NAME  = "_____________";
 int			getto_index;
 int			setto_index;
 int			setlim_index;
 int			answer_limit = -1; // no limitation by default

 // UI components
 JList 			list_commands;
 JList 			list_attrs;
 JList 			list_attr_props;
 JScrollPane		list_commands_view;
 JScrollPane		list_attrs_view;
 JScrollPane		list_attr_props_view;
 
 JTextField  		in_type_field;
 JTextField  		out_type_field;
 
 JTextArea  		in_type_desc;
 JTextArea  		out_type_desc;
 JScrollPane  		in_type_desc_view;
 JScrollPane  		out_type_desc_view;
 
 JTextField  		in_value;
 JTextArea       	history;
 JScrollPane  		history_view;
 JButton		exec_button;
 JButton		clear_button;
 JButton		rdattr_button;
 JButton		wrattr_button;
 JButton		close_button;
 
 JLabel			title_label;
 JLabel			in_example;
 JLabel                 in_value_lb;
 JLabel                 command_list_lb;
 JLabel                 argin_type_lb;
 JLabel                 argout_type_lb;
 JLabel                 argin_desc_lb;
 JLabel                 argout_desc_lb;
 JLabel			att_list_lb;
 JLabel			att_prop_lb;
 
 boolean                isAnApplet  = false;
 
 static final int	DBG_NONE   = 0;	// no verbose at all
 static final int	DBG_ERRORS = 1; // minimum verbose level
 static final int	DBG_TRACE  = 2;
 static final int	DBG_INFOS  = 3;
 static final int	DBG_MAX    = 4; // maximum verbose level

 // Taken from fr.esrf.TangoDs.TangoConst to avoid using Tango.jar
 final String[] Tango_CmdArgTypeName = {
                new String("DevVoid"),
                new String("DevBoolean"),
                new String("DevShort"),
                new String("DevLong"),
                new String("DevFloat"),
                new String("DevDouble"),
                new String("DevUShort"),
                new String("DevULong"),
                new String("DevString"),
                new String("DevVarCharArray"),
                new String("DevVarShortArray"),
                new String("DevVarLongArray"),
                new String("DevVarFloatArray"),
                new String("DevVarDoubleArray"),
                new String("DevVarUShortArray"),
                new String("DevVarULongArray"),
                new String("DevVarStringArray"),
                new String("DevVarLongStringArray"),
                new String("DevVarDoubleStringArray"),
                new String("DevState")
 };


 /**********************************
  *
  */
 public ExecDev()
 {
  //
  // Construct the user interface panel
  //
    
  // Construct the UI itself placing components according to predefined layout
  setLayout(null);

  title_label = new JLabel();
  title_label.setHorizontalAlignment(JLabel.CENTER);
  title_label.setFont( new java.awt.Font("Lucida Bright", 1, 16) );
  add(title_label);

  in_value_lb = new JLabel("Argin Value");
  add(in_value_lb);

  in_example = new JLabel("");
  add(in_example);

  in_value = new JTextField();
  in_value.setBackground(Color.white);
  in_value.setEditable(true);
  add(in_value);

  command_list_lb = new JLabel("Command List");
  add(command_list_lb);

  argin_type_lb = new JLabel("Argin Type");
  add(argin_type_lb);

  argout_type_lb = new JLabel("Argout Type");
  add(argout_type_lb);

  // Add list of commands
  list_commands = new JList(); 
  list_commands.addListSelectionListener(this);
  list_commands.addMouseListener(this);
  list_commands_view = new JScrollPane(list_commands);
  add(list_commands_view);

  in_type_field = new JTextField();
  in_type_field.setEditable(false);
  add(in_type_field);
  out_type_field = new JTextField(10);
  out_type_field.setEditable(false);
  add(out_type_field);

  argin_desc_lb = new JLabel("Argin Description");
  add(argin_desc_lb);

  argout_desc_lb = new JLabel("Argout Description");
  add(argout_desc_lb);

  in_type_desc=new JTextArea();
  in_type_desc.setEditable(false);
  in_type_desc.setBackground( in_type_field.getBackground() );
  in_type_desc_view = new JScrollPane(in_type_desc);
  in_type_desc_view.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
  add(in_type_desc_view);

  out_type_desc = new JTextArea();
  out_type_desc.setEditable(false);
  out_type_desc.setBackground( out_type_field.getBackground() );
  out_type_desc_view = new JScrollPane(out_type_desc);
  out_type_desc_view.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
  add(out_type_desc_view);

  exec_button = new JButton("Execute Command");
  exec_button.addActionListener(this);
  add(exec_button);

  clear_button = new JButton("Clear History");
  clear_button.addActionListener(this);
  add(clear_button);

  att_list_lb = new JLabel("Attribute List");
  add(att_list_lb);

  att_prop_lb = new JLabel("Attribute properties");
  add(att_prop_lb);

  // Add list of device attributes
  list_attrs = new JList(); 
  list_attrs.addListSelectionListener(this);
  list_attrs.addMouseListener(this);
  list_attrs_view = new JScrollPane(list_attrs);
  add(list_attrs_view);

  // Add list of attribute properties
  list_attr_props = new JList(); 
  list_attr_props.addListSelectionListener(this);
  list_attr_props.addMouseListener(this);
  list_attr_props_view = new JScrollPane(list_attr_props);
  add(list_attr_props_view);
  
  rdattr_button = new JButton("Read Attribute");
  rdattr_button.addActionListener(this);
  add(rdattr_button);

  wrattr_button = new JButton("Write Attribute");
  wrattr_button.addActionListener(this);
  add(wrattr_button);

  close_button = new JButton("Dismiss");
  close_button.addActionListener(this);
  add(close_button);

  // New Row________________________

  history = new JTextArea(""); 
  history.setEditable(false);
  history_view = new JScrollPane(history);  
  add(history_view);
    
 }

 /**********************************
  *
  */
  
  public Rectangle getPRect(double x1,double y1,double x2,double y2)
  {
    
    Dimension d=getSize();	
    double w = (double)d.width / 100.0;
    double h = (double)d.height / 100.0;    
    Rectangle ret = new Rectangle( (int)(x1*w) , (int)(y1*h) , (int)( (x2-x1)*w ) , (int)( (y2-y1)*h ) );
    return ret;
  } 
  
  public void placeComponents(Dimension d) {    

    setSize(d);
          
    title_label.setBounds( getPRect( 2.0 , 1.0 , 98.0 , 4.0 ) );
    in_value_lb.setBounds( getPRect( 2.0 , 4.0 , 20.0 , 6.4 ) );
    in_example.setBounds( getPRect( 36.4 , 10.3 , 98.0 , 12.5 ) );
    in_value.setBounds( getPRect( 2.0 , 6.8 , 98.0 , 10.2 ) );
    command_list_lb.setBounds( getPRect( 2.0 , 10.5 , 22.6 , 12.5 ) );
    list_commands_view.setBounds( getPRect(2.0 ,13.0 , 31.6 , 33.0 ) );
    
    argin_type_lb.setBounds(      getPRect(33.4,15.4, 62.2,18.2) ) ;
    in_type_field.setBounds(      getPRect(33.4,18.6, 62.6,22.8) );
    argout_type_lb.setBounds(     getPRect(66.4,15.4, 98.0,18.2) );
    out_type_field.setBounds(     getPRect(66.4,18.6, 98.0,22.8) ); 
    argin_desc_lb.setBounds(      getPRect(33.4,23.6, 62.2,26.2 ) );
    in_type_desc_view.setBounds(  getPRect(33.4,26.6, 63.0,33.0) );
    argout_desc_lb.setBounds(     getPRect(66.4,23.6, 98.0,26.2 ) );
    out_type_desc_view.setBounds( getPRect(66.4,26.6, 98.0,33.0) ); 

    exec_button.setBounds( getPRect( 20.0 , 34.0 , 49.0 , 38.0 ) );
    clear_button.setBounds( getPRect(51.0 , 34.0 , 80.0 , 38.0) );

    list_attrs_view.setBounds( getPRect(     2.0,42.0,30.5,55.4) );
    att_list_lb.setBounds( getPRect(         2.0,38.5,29.6,41.5 ) );
    list_attr_props_view.setBounds( getPRect(31.4,42.0,98.0,55.4) );
    att_prop_lb.setBounds( getPRect(         31.6,38.5,98.0,41.5 ) );  
    
    rdattr_button.setBounds( getPRect( 20.0 , 56.8 , 49.0 , 60.8 ) );
    wrattr_button.setBounds( getPRect( 51.0 , 56.8 , 80.0 , 60.8 ) ); 

    history_view.setBounds( getPRect( 2.0 , 61.6 , 98.0 , 94.0 ) );

    close_button.setBounds( getPRect( 40.0 , 95.0 , 60.0 , 99.0 ) ); 
  
    list_commands_view.revalidate();
    history_view.revalidate();
    list_attrs_view.revalidate();
    list_attr_props_view.revalidate();
    out_type_desc_view.revalidate();
    in_type_desc_view.revalidate();
  }

 


 /**********************************
  *
  */
 public void actionPerformed(ActionEvent evt)
 {
  Object src = evt.getSource();
  
  if(src == clear_button)
   history.replaceRange("",0,history.getText().length());

  if(src == exec_button)
   execCurCommand();

  if(src == rdattr_button)
   readCurAttr();

  if(src == wrattr_button)
   writeCurAttr();

  if(src == close_button) {
    // Go up to the frame/dialog parent to hide it
    getParent().getParent().getParent().getParent().setVisible(false);
  }
  
 }




 /**********************************
  * Called when an item in the command list is selected
  */
 public void valueChanged(ListSelectionEvent e) 
 {
  JList list  = (JList)(e.getSource());

  if(list == list_commands)
  {
    debug(DBG_INFOS, "Command selected: " + list.getSelectedValue());
    update_command_infos(list.getSelectedIndex());
  }
  else if(list == list_attrs)
  {
    debug(DBG_INFOS, "Attribute selected: " + list.getSelectedValue());
    update_attr_infos(list.getSelectedIndex());
  }
   
 }




 /**********************************
  * Required by MouseListener interface
  */
 public void mousePressed(MouseEvent evt) {}
 public void mouseReleased(MouseEvent evt) {}
 public void mouseEntered(MouseEvent evt) {}
 public void mouseExited(MouseEvent evt) {}
 public void mouseClicked(MouseEvent evt) 
 {

  Object src = evt.getSource();

  // If double clicked on a command then execute it
  if(evt.getClickCount() >= 2) {
   if( src==list_attrs)    readCurAttr();
   if( src==list_commands) execCurCommand();
  }
 
 }
 
  public void display_error(String msg) {
    JOptionPane.showMessageDialog(null, msg , "Device Panel Error", JOptionPane.ERROR_MESSAGE);
  }

 /**********************************
  * Return: false if device could be imported
  */
 public boolean set_device_name(String devname)
 {
  device_name = devname;

  debug(DBG_TRACE, "Entering set_device_name()");

  title_label.setText(device_name);


  //
  // TANGO init (only if running in standalone application)
  //
  if(!isAnApplet)
  {
   try 
   {   
    device = new DevHelper(device_name); 
   }
   catch(DevHelperDevFailed e)
   {
    StringBuffer errmsg = new StringBuffer();

    errmsg.append("failed to import device: " + device_name + "\n");
    errmsg.append("reason:\t" + e.reason + "\n");
    errmsg.append("origin:\t" + e.origin + "\n");
    errmsg.append("desc:\t" + e.desc   + "\n");

    debug(DBG_ERRORS, errmsg.toString());

// TODO
//    has_to_exit = true;
    display_error(errmsg.toString());
    
    return true;
   }
   catch(Exception e)
   {
    StringBuffer errmsg = new StringBuffer();

    errmsg.append("ORB failed connecting to database");

    debug(DBG_ERRORS, errmsg.toString());

// TODO
//    has_to_exit = true;
    display_error(errmsg.toString());
    
    return true;
   }
  }

  //
  // Get the device name, device commands and device attributes
  // (as applet arguments if running as an applet or directly from
  // the database server if running a a standalone application)
  //
  get_commands();
  get_attrs();




  // 
  // Fill the UI fields with default values
  //

  // Fill command and attribute lists
  update_ui_list();
  
  // Clear any previous command history
  history.replaceRange("",0,history.getText().length());




  // Normal end
  return false;

 }





 /**********************************
  * Fill command and attribute lists
  */
 void update_ui_list()
 {
  // Set the list of commands
  list_commands.removeAll();
  DefaultListModel ml = new DefaultListModel();  
  for(int i=0; i<nb_commands;i++)
    ml.add(i,commands[i].cmd_name);
    
  list_commands.setModel(ml); 
  list_commands.setSelectedIndex(0);

  // Set the command fields
  update_command_infos(0);

  // Enable attributes if this device has some
  list_attrs.removeAll();
  list_attr_props.removeAll();
  if(attrs.length > 0)
  {
   // Set the list of attributes
   ml = new DefaultListModel();
   for(int i=0; i<attrs.length;i++)
    ml.add(i,attrs[i]);
   list_attrs.setModel(ml);
   list_attrs.setSelectedIndex(0);

   // Set the attribute properties
   update_attr_infos(0);

   // Allow attribute handling
   rdattr_button.setEnabled(true);
   wrattr_button.setEnabled(true);
  }
  else
  {
   // Disable attribute handling
   rdattr_button.setEnabled(false);
   wrattr_button.setEnabled(false);
  }

 }





 /**********************************
  * Get the device name and commands
  *
  * from applet arguments if running as an applet or directly from
  * the database server if running as a standalone application.
  */
 void get_commands()
 {
  debug(DBG_TRACE, "Entering get_commands()");
  CmdInfo[] 		pure_commands    = null;
  int			nb_pure_commands = 0;

  if(isAnApplet)
  {
/* TODO
   //
   // Running as an applet, access to TANGO through servlets
   // 

   // Set a default verbose level for APPLET debug
   verbose_level = DBG_MAX;
   
   // Get parameters from HTML page
   device_name = this.getParameter("device");
   dbase_host  = this.getParameter("dbase_host");
   dbase_port  = this.getParameter("dbase_port");

   session_id = this.getParameter("session_id");
   debug(DBG_INFOS, "Session id: " + session_id);

   nb_pure_commands = Integer.parseInt(this.getParameter("nb_commands"));

   // Prepare to store the commands information
   pure_commands = new CmdInfo[nb_pure_commands];

   // Add list of commands
   for(int i=0; i<nb_pure_commands;i++)
   {
    String  num = Integer.toString(i);

    pure_commands[i] = new CmdInfo();

    pure_commands[i].cmd_name = this.getParameter("cmd"+num);

    pure_commands[i].in_type  = 
	Integer.parseInt(this.getParameter("cmd_in"+num));
    pure_commands[i].in_type_desc = this.getParameter("cmd_in_desc"+num);

    pure_commands[i].out_type = 
	Integer.parseInt(this.getParameter("cmd_out"+num));
    pure_commands[i].out_type_desc = this.getParameter("cmd_out_desc"+num);
   }
*/
  }
  else
  {
   //
   // Running in a standalone application, direct access to TANGO
   //

   fr.esrf.TangoApi.CommandInfo[]	tango_commands;

   // Get an array with the command list (including CORBA operations ping()
   // blackbox() etc) available for this device.
   tango_commands = device.command_list_query();

   // Get the number of commands of this device
   nb_pure_commands = tango_commands.length;

   // Prepare to store the commands information
   pure_commands = new CmdInfo[nb_pure_commands];

   // Convert from fr.esrf.Tango.CommandInfo
   for(int i=0; i<nb_pure_commands;i++)
   {
    pure_commands[i] = new CmdInfo();

    pure_commands[i].cmd_name      = tango_commands[i].cmd_name;

    pure_commands[i].in_type       = tango_commands[i].in_type;
    pure_commands[i].in_type_desc  = tango_commands[i].in_type_desc;

    pure_commands[i].out_type      = tango_commands[i].out_type;
    pure_commands[i].out_type_desc = tango_commands[i].out_type_desc;
   }
  }



  //
  // Add some GUI separator and also the extra commands commands
  // (not commands of the device itself)
  //
  int nb_extra_commands = 4; // including the separator
  nb_commands = nb_pure_commands + nb_extra_commands;
  commands    = new CmdInfo[nb_commands];
  int j = 0;
  
  for(int i=0; i<nb_pure_commands;i++,j++)
  {
   // prepare some room for (the separator + extra commands) after the
   // three minimum commands (ping, info, blackbox)
   if(i == 3)
    j+=nb_extra_commands;

   commands[j] = new CmdInfo();

   commands[j].cmd_name      = pure_commands[i].cmd_name;

   commands[j].in_type       = pure_commands[i].in_type;
   commands[j].in_type_desc  = pure_commands[i].in_type_desc;

   commands[j].out_type      = pure_commands[i].out_type;
   commands[j].out_type_desc = pure_commands[i].out_type_desc;
  }

  // Add the extra commands
  j = 3;
  commands[j] = new CmdInfo();
  commands[j].cmd_name      = "SetTimeout";
  commands[j].in_type       =  2; // index in Tango_CmdArgTypeName[]
  commands[j].in_type_desc  = "Timeout in mS";
  commands[j].out_type      =  0;
  commands[j].out_type_desc = "";
  setto_index = j;
  j++;
  commands[j] = new CmdInfo();
  commands[j].cmd_name      = "GetTimeout";
  commands[j].in_type       =  0;
  commands[j].in_type_desc  = "";
  commands[j].out_type      =  2; // index in Tango_CmdArgTypeName[]
  commands[j].out_type_desc = "Timeout in mS";
  getto_index = j;
  j++;
  commands[j] = new CmdInfo();
  commands[j].cmd_name      = "SetAnswerLimit";
  commands[j].in_type       =  2; // index in Tango_CmdArgTypeName[]
  commands[j].in_type_desc  = "Argout print limit (-1 for no limit)";
  commands[j].out_type      =  0;
  commands[j].out_type_desc = "";
  setlim_index = j;

  // Add the separator
  j++;
  commands[j] = new CmdInfo();
  commands[j].cmd_name      = SEP_COMMAND_NAME;
  commands[j].in_type       =  0;
  commands[j].in_type_desc  = "";
  commands[j].out_type      =  0;
  commands[j].out_type_desc = "";
  j++;


  debug(DBG_TRACE, "Leaving get_commands()");
 }



 /**********************************
  * Get the device attributes
  *
  * from applet arguments if running as an applet or directly from
  * the database server if running as a standalone application.
  */
 void get_attrs()
 {
  debug(DBG_TRACE, "Entering get_attrs()");

  if(isAnApplet)
  {
   //
   // Running as an applet, access to TANGO through servlets
   // 
/* TODO
*/
  }
  else
  {
   //
   // Running in a standalone application, direct access to TANGO
   //

   // Get the list of attributes of the device
   attrs = device.get_attribute_list();

   // Get for each attribute all its properties
   fr.esrf.TangoApi.AttributeInfo[] tango_attr_configs;
   tango_attr_configs = device.get_attribute_config();

   // Get the number of attributes
   int nb_attrs = tango_attr_configs.length;

   // Convert Tango stuff to non Tango (required when running as Applet)
   attr_configs = new AttrInfo[nb_attrs];
   for(int i=0;i<nb_attrs;i++)
   {
    attr_configs[i] = new AttrInfo(
	tango_attr_configs[i].name,
	AttrWriteType_to_string(tango_attr_configs[i].writable),
	AttrDataFormat_to_string(tango_attr_configs[i].data_format),
	Tango_CmdArgTypeName[tango_attr_configs[i].data_type],
	tango_attr_configs[i].data_type,
	Integer.toString(tango_attr_configs[i].max_dim_x),
	Integer.toString(tango_attr_configs[i].max_dim_y),
	tango_attr_configs[i].description,
	tango_attr_configs[i].label,
	tango_attr_configs[i].unit,
	tango_attr_configs[i].standard_unit,
	tango_attr_configs[i].display_unit,
	tango_attr_configs[i].format,
	tango_attr_configs[i].min_value,
	tango_attr_configs[i].max_value,
	tango_attr_configs[i].min_alarm,
	tango_attr_configs[i].max_alarm);
   }

  }

  debug(DBG_TRACE, "Leaving get_attrs()");
 }


 /**********************************
  * 
  *
  */
 String AttrWriteType_to_string(fr.esrf.Tango.AttrWriteType in)
 {
  String ret;

  if(in == fr.esrf.Tango.AttrWriteType.READ)
   ret = "READ";
  else if(in == fr.esrf.Tango.AttrWriteType.READ_WITH_WRITE)
   ret = "READ_WITH_WRITE";
  else if(in == fr.esrf.Tango.AttrWriteType.WRITE)
   ret = "WRITE";
  else if(in == fr.esrf.Tango.AttrWriteType.READ_WRITE)
   ret = "READ_WRITE";
  else
   ret = "UNKNOWN";
 
  return ret;
 }



 /**********************************
  * 
  *
  */
 String AttrDataFormat_to_string(fr.esrf.Tango.AttrDataFormat in)
 {
  String ret;

  if(in == fr.esrf.Tango.AttrDataFormat.SCALAR)
   ret = "SCALAR";
  else if(in == fr.esrf.Tango.AttrDataFormat.SPECTRUM)
   ret = "SPECTRUM";
  else if(in == fr.esrf.Tango.AttrDataFormat.IMAGE)
   ret = "IMAGE";
  else
   ret = "UNKNOWN";
 
  return ret;
 }



 /**********************************
  * Change the timeout on communication to the device
  *
  * talk to the device through a servlet if running as an applet or 
  * talk directly to the device if running as a standalone application.
  */
 String get_timeout()
 {
  int	ret_value = -1;


  debug(DBG_TRACE, "Entering get_timeout()");


  if(isAnApplet)
  {
   /*
   //
   // Running as an applet, access to TANGO through servlets
   // 
   try
   {
    String command_name = DevHelper.GET_TIMEOUT_CMD;

    URL          servlet_url = new URL(
        getCodeBase(),
        debug_url + 
	EXEC_SERVLET_NAME);

    HttpMessage  msg         = new HttpMessage(servlet_url);



    Properties   params      = new Properties();
    params.put("host",         client_host);
    params.put("device",       device_name);
    params.put("dbase_host",   dbase_host);
    params.put("dbase_port",   dbase_port);
    params.put("cmd",          command_name);

    debug(DBG_TRACE, "Waiting for servlet...");
    InputStream  in          = msg.sendPostMessage(params);
    debug(DBG_TRACE, "done, something to print!!");


    BufferedReader data      = new BufferedReader(
                               new InputStreamReader(
                               new BufferedInputStream(in)));
    
    String ret_client_string = data.readLine();
    debug(DBG_INFOS, "NextResult:BEG" + ret_client_string + "END");
    ret_value = Integer.parseInt(ret_client_string);

   }
   catch(Exception e)
   {
    StringBuffer errmsg = new StringBuffer();
    errmsg.append("Exception: " + e.getClass().getName() + "\n");
    errmsg.append("message:\t" + e.getMessage()   + "\n");

    debug(DBG_ERRORS, errmsg.toString());

    append_error(errmsg.toString());
   }
   */
  }
  else
  {
   //
   // Running in a standalone application, direct access to TANGO
   //
   try
   {
    ret_value = device.get_timeout();
   }
   catch(DevHelperDevFailed e)
   {
    StringBuffer errmsg = new StringBuffer();

    errmsg.append("failed getting timeout\n");
    errmsg.append("reason:\t" + e.reason + "\n");
    errmsg.append("origin:\t" + e.origin + "\n");
    errmsg.append("desc:\t" + e.desc   + "\n");

    debug(DBG_ERRORS, errmsg.toString());

    display_error(errmsg.toString());
    return null;
   }
   catch(Exception e)
   {
    StringBuffer errmsg = new StringBuffer();
    errmsg.append("Exception: " + e.getClass().getName() + "\n");
    errmsg.append("message:\t" + e.getMessage()   + "\n");

    debug(DBG_ERRORS, errmsg.toString());

    append_error(errmsg.toString());
    return null;
   }

  }


  //
  // Update on UI 
  //
  StringBuffer msg = new StringBuffer();
  msg.append("\n###################################\n");
  msg.append("Current timeout to communicate with device\n"); 
  msg.append(Integer.toString(ret_value) + " mS\n"); 


  debug(DBG_TRACE, "Leaving get_timeout()");
  return msg.toString();
 }






 /**********************************
  * Change the timeout on communication to the device
  *
  * talk to the device through a servlet if running as an applet or 
  * talk directly to the device if running as a standalone application.
  */
 String set_timeout(String argin_string)
 {
  debug(DBG_TRACE, "Entering set_timeout()");

  String ret_client_string = null;


  // Minimum check on the new timeout value
  try
  {
   int bidon = Integer.valueOf(argin_string).intValue();

   if(bidon < 0)
     throw(new java.lang.NumberFormatException());
  }
  catch(java.lang.NumberFormatException ignored)
  {
   append_error("New timeout value not a positive integer value");
   return null;
  }




  if(isAnApplet)
  {
   /*
   //
   // Running as an applet, access to TANGO through servlets
   // 
   try
   {
    String dummy  = null;
    String command_name = DevHelper.SET_TIMEOUT_CMD;

    URL          servlet_url = new URL(
        getCodeBase(),
        debug_url + 
	EXEC_SERVLET_NAME);

    HttpMessage  msg         = new HttpMessage(servlet_url);



    Properties   params      = new Properties();
    params.put("host",         client_host);
    params.put("cmd",          command_name);
    params.put("device",       device_name);
    params.put("dbase_host",   dbase_host);
    params.put("dbase_port",   dbase_port);
    params.put("argin",        argin_string);

    debug(DBG_TRACE, "Waiting for servlet...");
    InputStream  in          = msg.sendPostMessage(params);
    debug(DBG_TRACE, "done, something to print!!");


    BufferedReader data      = new BufferedReader(
                               new InputStreamReader(
                               new BufferedInputStream(in)));
    
    dummy = data.readLine();
    ret_client_string = dummy + "\n";
    while((dummy = data.readLine()) != null)
     ret_client_string += dummy + "\n";

    debug(DBG_INFOS, "NextResult:BEG" + ret_client_string + "END");
   }
   catch(Exception e)
   {
    StringBuffer errmsg = new StringBuffer();
    errmsg.append("Exception: " + e.getClass().getName() + "\n");
    errmsg.append("message:\t" + e.getMessage()   + "\n");

    debug(DBG_ERRORS, errmsg.toString());

    append_error(errmsg.toString());
   }
   */
  }
  else
  {
   //
   // Running in a standalone application, direct access to TANGO
   //
   try
   {
    ret_client_string = device.set_timeout(argin_string);
   }
   catch(DevHelperDevFailed e)
   {
    StringBuffer errmsg = new StringBuffer();

    errmsg.append("failed setting timeout to: " + argin_string + "\n");
    errmsg.append("reason:\t" + e.reason + "\n");
    errmsg.append("origin:\t" + e.origin + "\n");
    errmsg.append("desc:\t" + e.desc   + "\n");

    debug(DBG_ERRORS, errmsg.toString());

    display_error(errmsg.toString());
    return null;
   }
   catch(Exception e)
   {
    StringBuffer errmsg = new StringBuffer();
    errmsg.append("Exception: " + e.getClass().getName() + "\n");
    errmsg.append("message:\t" + e.getMessage()   + "\n");

    debug(DBG_ERRORS, errmsg.toString());

    append_error(errmsg.toString());
    return null;
   }

  }

  debug(DBG_TRACE, "Leaving set_timeout()");
  return ret_client_string;
 }






 /**********************************
  * Execute the current selected command on the device
  *
  * talk to the device through a servlet if running as an applet or 
  * talk directly to the device if running as a standalone application.
  */
 void execCurCommand()
 {
  debug(DBG_TRACE, "Entering execCurCommand()");

  // Get from UI command to be executed
  int index = list_commands.getSelectedIndex();
  String command_name = commands[index].cmd_name;
  debug(DBG_INFOS, "Command to be executed: " + command_name);

  // Get argin string from UI component
  String argin_string = in_value.getText();

  // Prepare command specific informations
  int	command_in_type  = commands[index].in_type;
  int	command_out_type = commands[index].out_type;

  String ret_client_string = null;



  // Check if user is on a separator in the command list
  if(command_name.equals(SEP_COMMAND_NAME))
   return;



  // Check first if the command to execute is not an extra one
  if(index == getto_index)
   ret_client_string = get_timeout();
  else if(index == setto_index)
   ret_client_string = set_timeout(argin_string);
  else if(index == setlim_index)
  {
   // keep a record of the value
   try
   {
    answer_limit = Integer.parseInt(argin_string);
   }
   catch(NumberFormatException e)
   {
    StringBuffer errmsg = new StringBuffer();
    errmsg.append("wrong argin\n");
    errmsg.append("expecting an integer value\n");

    debug(DBG_ERRORS, errmsg.toString());
    append_error(errmsg.toString());
    return;
   }

   // Prepare string to update the UI 
   StringBuffer msg = new StringBuffer();
   msg.append("\n###################################\n");
   if(answer_limit > 0)
   {
    msg.append("Argout arrays print out limited to ");
    msg.append(Integer.toString(answer_limit) + " elements\n"); 
   }
   else
    msg.append("Argout arrays print out not limited\n");

   ret_client_string = msg.toString();
  }
  else if(isAnApplet)
  {
   /*
   //
   // Running as an applet, access to TANGO through servlets
   // 
   try
   {
    String dummy  = null;
    URL          servlet_url = new URL(
        getCodeBase(),
        debug_url + 
	EXEC_SERVLET_NAME);

    HttpMessage  msg         = new HttpMessage(servlet_url);



    Properties   params      = new Properties();
    params.put("host",         client_host);
    params.put("cmd",          command_name);
    params.put("device",       device_name);
    params.put("dbase_host",   dbase_host);
    params.put("dbase_port",   dbase_port);
    params.put("cmd_in",       Integer.toString(command_in_type));
    params.put("cmd_in_desc",  commands[index].in_type_desc);
    params.put("cmd_out",      Integer.toString(command_out_type));
    params.put("cmd_out_desc", commands[index].out_type_desc);
    params.put("argin",        argin_string);

    debug(DBG_TRACE, "Waiting for servlet...");
    InputStream  in          = msg.sendPostMessage(params);
    debug(DBG_TRACE, "done, something to print!!");


    BufferedReader data      = new BufferedReader(
                               new InputStreamReader(
                               new BufferedInputStream(in)));
    
    dummy = data.readLine();
    ret_client_string = dummy + "\n";
    while((dummy = data.readLine()) != null)
     ret_client_string += dummy + "\n";

    debug(DBG_INFOS, "NextResult:BEG" + ret_client_string + "END");
   }
   catch(Exception e)
   {
    StringBuffer errmsg = new StringBuffer();
    errmsg.append("Exception: " + e.getClass().getName() + "\n");
    errmsg.append("message:\t" + e.getMessage()   + "\n");

    debug(DBG_ERRORS, errmsg.toString());

    append_error(errmsg.toString());
   }
   */
  }
  else
  {
   //
   // Running in a standalone application, direct access to TANGO
   //
   try
   {
    ret_client_string = device.exec_command(
	command_name, 
	argin_string,
	answer_limit);
   }
   catch(DevHelperDevFailed e)
   {
    StringBuffer errmsg = new StringBuffer();

    errmsg.append("failed execution command: " + command_name + "\n");
    errmsg.append("reason:\t" + e.reason + "\n");
    errmsg.append("origin:\t" + e.origin + "\n");
    errmsg.append("desc:\t" + e.desc   + "\n");

    debug(DBG_ERRORS, errmsg.toString());

    append_error(errmsg.toString());
    return;
   }
   catch(Exception e)
   {
    StringBuffer errmsg = new StringBuffer();
    errmsg.append("Exception: " + e.getClass().getName() + "\n");
    errmsg.append("message:\t" + e.getMessage()   + "\n");

    debug(DBG_ERRORS, errmsg.toString());

    append_error(errmsg.toString());
    return;
   }

  }

  //
  // Update history on UI 
  //
  if(ret_client_string != null)
  {
   append_history(ret_client_string);

   // Check if device has been restarted
   if(ret_client_string.indexOf("WARNING: device re-imported") != -1)
   {
    // Get the device name, device commands and device attributes
    get_commands();
    get_attrs();

    // Fill command and attribute lists
    update_ui_list();
   }
  }

  debug(DBG_TRACE, "Leaving execCurCommand()");
 }







 /**********************************
  *
  *
  *
  */
 void readCurAttr()
 {
  debug(DBG_TRACE, "Entering readCurAttr()");

  // Get from UI attribute to be read
  int index = list_attrs.getSelectedIndex();
  String attr_name = attrs[index];
  debug(DBG_INFOS, "Attribute to be read: " + attr_name);

  String ret_client_string = null;

  if(isAnApplet)
  {
   
   //
   // Running as an applet, access to TANGO through servlets
   // 

/* TODO  implement this
*/
  }
  else
  {
   //
   // Running in a standalone application, direct access to TANGO
   //

   try
   {
    ret_client_string = device.read_attr(attr_name);
   }
   catch(DevHelperDevFailed e)
   {
    StringBuffer errmsg = new StringBuffer();

    errmsg.append("failed reading attribute: " + attr_name + "\n");
    errmsg.append("reason:\t" + e.reason + "\n");
    errmsg.append("origin:\t" + e.origin + "\n");
    errmsg.append("desc:\t" + e.desc   + "\n");

    debug(DBG_ERRORS, errmsg.toString());

    append_error(errmsg.toString());
    return;
   }
   catch(Exception e)
   {
    StringBuffer errmsg = new StringBuffer();
    errmsg.append("Exception: " + e.getClass().getName() + "\n");
    errmsg.append("message:\t" + e.getMessage()   + "\n");

    debug(DBG_ERRORS, errmsg.toString());

    append_error(errmsg.toString());
    return;
   }
  }

  //
  // Update history on UI 
  //
  if(ret_client_string != null)
  {
   append_history(ret_client_string);

   // Check if device has been restarted
   if(ret_client_string.indexOf("WARNING: device re-imported") != -1)
   {
    // Get the device name, device commands and device attributes
    get_commands();
    get_attrs();

    // Fill command and attribute lists
    update_ui_list();
   }
  }

  debug(DBG_TRACE, "Leaving readCurAttr()");
 }






 /**********************************
  *
  *
  *
  */
 void writeCurAttr()
 {
  debug(DBG_TRACE, "Entering writeCurAttr()");

  // Get from UI attribute to be read
  int index = list_attrs.getSelectedIndex();
  String attr_name = attrs[index];
  debug(DBG_INFOS, "Attribute to write: " + attr_name);

  // Get argin string from UI component
  String argin_string = in_value.getText();

  String ret_client_string = null;

  if(isAnApplet)
  {
   //
   // Running as an applet, access to TANGO through servlets
   // 

/* TODO  implement this
*/
  }
  else
  {
   //
   // Running in a standalone application, direct access to TANGO
   //

   try
   {
    ret_client_string = device.write_attr(
	attr_name,
	argin_string);
   }
   catch(DevHelperDevFailed e)
   {
    StringBuffer errmsg = new StringBuffer();

    errmsg.append("failed writting attribute: " + attr_name + "\n");
    errmsg.append("reason:\t" + e.reason + "\n");
    errmsg.append("origin:\t" + e.origin + "\n");
    errmsg.append("desc:\t" + e.desc   + "\n");

    debug(DBG_ERRORS, errmsg.toString());

    append_error(errmsg.toString());
    return;
   }
   catch(Exception e)
   {
    StringBuffer errmsg = new StringBuffer();
    errmsg.append("Exception: " + e.getClass().getName() + "\n");
    errmsg.append("message:\t" + e.getMessage()   + "\n");

    debug(DBG_ERRORS, errmsg.toString());

    append_error(errmsg.toString());
    return;
   }
  }

  //
  // Update history on UI 
  //
  if(ret_client_string != null)
  {
   append_history(ret_client_string);

   // Check if device has been restarted
   if(ret_client_string.indexOf("WARNING: device re-imported") != -1)
   {
    // Get the device name, device commands and device attributes
    get_commands();
    get_attrs();

    // Fill command and attribute lists
    update_ui_list();
   }
 }

  debug(DBG_TRACE, "Leaving writeCurAttr()");
 }





 /**********************************
  * Update the UI fields according to the command number specified
  */
 private void update_command_infos(int i)
 {
  in_type_field.setText(Tango_CmdArgTypeName[commands[i].in_type]);
  if(commands[i].in_type != 0)
  {
   in_type_desc.setText(commands[i].in_type_desc);
   in_value.setEditable(true);
   in_example.setText(HtmlHelper.argin_example(commands[i].in_type)); 
  }
  else
  {
   in_type_desc.setText("");
   in_value.setText("");
   in_value.setEditable(false);
   in_example.setText("");
  }

  in_type_desc.setCaretPosition(0);

  out_type_field.setText(Tango_CmdArgTypeName[commands[i].out_type]);
  if(commands[i].out_type != 0)
   out_type_desc.setText(commands[i].out_type_desc);
  else
   out_type_desc.setText("");

  out_type_desc.setCaretPosition(0);

 }






 /**********************************
  * Update the UI fields according to the attribute number specified
  */
 private void update_attr_infos(int i)
 {
  list_attr_props.removeAll();

  if(attr_configs.length == 0)
   return;

  DefaultListModel ml = new DefaultListModel();  

  ml.add(0 ,"Name:          "+attr_configs[i].name);
  ml.add(1 ,"Writable:      "+attr_configs[i].writable);
  ml.add(2 ,"Data Format:   "+attr_configs[i].data_format);
  ml.add(3 ,"Data Type:     "+attr_configs[i].data_type);
  ml.add(4 ,"Max Dim X:     "+attr_configs[i].max_dim_x);
  ml.add(5 ,"Max Dim Y:     "+attr_configs[i].max_dim_y);
  ml.add(6 ,"Description:   "+attr_configs[i].description);
  ml.add(7 ,"Label:         "+attr_configs[i].label);
  ml.add(8 ,"Unit:          "+attr_configs[i].unit);
  ml.add(9 ,"Standard Unit: "+attr_configs[i].standard_unit);
  ml.add(10,"Display Unit:  "+attr_configs[i].display_unit);
  ml.add(11,"Format:        "+attr_configs[i].format);
  ml.add(12,"Min Value:     "+attr_configs[i].min_value);
  ml.add(13,"Max Value:     "+attr_configs[i].max_value);
  ml.add(14,"Min Alarm:     "+attr_configs[i].min_alarm);
  ml.add(15,"Max Alarm:     "+attr_configs[i].max_alarm);

  list_attr_props.setModel(ml);
  list_attr_props.setSelectedIndex(0);

  if(attr_configs[i].writable.indexOf("WRITE") != -1)
  {
   in_value.setEditable(true);
   in_example.setText(HtmlHelper.argin_example(attr_configs[i].data_type_num)); 
  }
  else
  {
   in_value.setEditable(false);
   in_example.setText(""); 
  }
 }




 /**********************************
  *
  */
 private void append_error(String message)
 {
  debug(DBG_ERRORS, message);
  append_history(
        "\n###################################\nERROR !!\n" +
	message);
 }


 /**********************************
  *
  */
 private void append_history(String message)
 {
  history.append(message);
  int last_pos = history.getText().length();
  history.select(last_pos, last_pos);
 }


 /**********************************
  *
  */
 private void debug(int level, String message)
 {
   /*
   if( level==DBG_INFOS ) {
    System.out.println("ExecDev: " + message);
   }
   */
 }
 
 // Test function
  
 static public void main(String args[]) {
 
 	   if( args.length != 1 ) {
	     
	     System.out.println("Usage: ExecDev devicename");
	   
	   } else {
	     
	       final ExecDev p = new ExecDev();	     
	       if( !p.set_device_name( args[0] ) ) {
	     
	         final JFrame f = new JFrame();
		 f.setTitle("Device Panel");
		 f.getContentPane().setLayout(null);
	         f.getContentPane().add(p);   	     	     
	         f.addComponentListener( new ComponentListener() {
                 public void componentHidden(ComponentEvent e) {
		   System.exit(0);
		 } 
                 public void componentMoved(ComponentEvent e) {}	  	     
                 public void componentResized(ComponentEvent e) { 
	           p.placeComponents(f.getContentPane().getSize());
	         }
                 public void componentShown(ComponentEvent e) { 
	           p.placeComponents(f.getContentPane().getSize());
	         }
	         });
	         f.setBounds(50,50,450,650);
	         f.setVisible(true);
	      } else {
	        System.exit(0);
	      }
	    	   
	  }
	
	}

} // end of class ExecDev
