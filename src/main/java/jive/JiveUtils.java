package jive;

import fr.esrf.TangoApi.*;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevVarLongStringArray;

import javax.swing.tree.TreePath;
import javax.swing.*;
import java.awt.*;
import java.util.Vector;
import java.util.regex.Pattern;
import java.io.FileWriter;
import java.io.IOException;

/** Contains utils function for Jive */
public class JiveUtils {

  private final static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

  public static String[] att_prop_default = {
    "description", "label", "unit", "standard_unit", "display_unit",
    "format", "min_value", "max_value", "min_alarm", "max_alarm"
  };

  public static String[] att_prop_default_idl3 = {
    "min_warning", "max_warning", "delta_t", "delta_val",
    "rel_change", "abs_change",
    "event_period",
    "archive_abs_change", "archive_rel_change", "archive_period"
  };

  public static String[] logging_level = { "OFF" , "FATAL" , "ERROR" , "WARNING" , "INFO" , "DEBUG" };

  public static Clipboard the_clipboard = new Clipboard();
  public static Frame parent = null;
  public static boolean readOnly = false;
  public static Vector savedClass = new Vector();
  public static Insets noMargin = new Insets(0,0,0,0);
  public static boolean showSystemProperty=false;

  //HDB stuff
  public static DeviceProxy hdbManager=null;
  public static boolean     hdbEnabled=false;

  public static String      atkPanelCmdLine = null;

  // *****************************************************************************************************************
  // Check item type
  static public int isInsideArray(String s,String[] arr) {

    boolean found = false;
    int i = 0;

    while (i < arr.length && !found) {
      found = arr[i].equalsIgnoreCase(s);
      if (!found) i++;
    }

    if(found) {
      return i;
    } else {
      return -1;
    }

  }

  static public int isInsideVector(String s,Vector arr) {

    boolean found = false;
    int i = 0;

    while (i < arr.size() && !found) {
      found = ((String)arr.get(i)).equalsIgnoreCase(s);
      if (!found) i++;
    }

    if(found) {
      return i;
    } else {
      return -1;
    }

  }

  static public boolean IsPollCfgItem(String s) {
    if( showSystemProperty )
      return false;
    else
      return s.equals("is_polled") ||
        s.equals("polling_period") ||
        s.equals("poll_old_factor") ||
        s.equals("poll_ring_depth");
  }

  static public int IsAttCfgItem(String s,int idl) {

    if( showSystemProperty )
      return -1;
    else {

      int i = isInsideArray(s,att_prop_default);
      if(i<0 && idl>=3) {
        i = isInsideArray(s,att_prop_default_idl3);
        if(i>=0) i+=att_prop_default.length;
      }
      return i;

    }

  }

  static public boolean IsSystemItem(String s) {
    if( showSystemProperty )
      return false;
    else
      return (s.equals("polled_cmd") ||
          s.equals("polled_attr") ||
          s.equals("non_auto_polled_cmd") ||
          s.equals("non_auto_polled_attr"));
  }

  static public boolean IsLogCfgItem(String s) {
    if( showSystemProperty )
      return false;
    else
      return (s.equals("logging_level") ||
        s.equals("logging_target") ||
        s.equals("current_logging_level") ||
        s.equals("current_logging_target") ||
        s.equals("logging_rft") ||
        s.equals("logging_path"));
  }

  static public boolean IsHdbCfgItem(String s) {
    if( showSystemProperty )
      return false;
    else
      return (s.equals("is_archived") ||
              s.equals("archiving_settings"));
  }

  static public boolean IsAlarmCfgItem(String s) {
    if( showSystemProperty )
      return false;
    else
      return s.equals("Alarms");
  }

  static public boolean IsEventCfgItem(String s) {
    if( showSystemProperty )
      return false;
    else
      return (s.equals("Archive Event") ||
              s.equals("Change Event") ||
              s.equals("Periodic Event"));
  }

  static public boolean isSavedClass(String className) {
    return isInsideVector(className,savedClass)>=0;
  }

  static public void addSavedClass(String className) {
    savedClass.add(className);
  }

  // *****************************************************************************************************************
  // Sort list

  static public void sortList(String []lst) {

    int i,j;
    boolean end = false;
    String tmp;

    // Bubble sort

    i = lst.length - 1;

    while (!end) {

      j = 0;
      end = true;

      while (j < i) {

        boolean swap = false;
        int diff = lst[j].compareToIgnoreCase(lst[j + 1]);
        swap = diff > 0;

        if (swap) {
          tmp = lst[j + 1];
          lst[j + 1] = lst[j];
          lst[j] = tmp;
          end = false;
        }
        j++;

      }
      i--;

    }

  }

  static public void sortList(String[] lst, boolean[] v,int idl) {

    int i,j;
    boolean end = false;
    String tmp;
    int[] weight = new int[lst.length];
    int tw;

    // Place System item first
    //       Polling Cfg Item secand
    //       property
    for (i = 0; i < lst.length; i++) {
      if (IsSystemItem(lst[i])) {
        weight[i] = 0;
      } else if (IsPollCfgItem(lst[i])) {
        weight[i] = 1;
      } else if (IsAlarmCfgItem(lst[i])) {
        weight[i] = 2;
      } else if (IsHdbCfgItem(lst[i])) {
        weight[i] = 3;
      } else if (IsLogCfgItem(lst[i])) {
        weight[i] = 4;
      } else if (IsAttCfgItem(lst[i],idl) >= 0) {
        weight[i] = 5;
      } else {
        weight[i] = 6;
      }
    }

    // Bubble sort
    i = lst.length - 1;
    while (!end) {
      j = 0;
      end = true;
      while (j < i) {
        // Compare weight
        boolean swap = false;

        if (weight[j] > weight[j + 1]) {
          swap = true;
        } else if (weight[j] == weight[j + 1]) {
          // Compare name
          int diff = lst[j].compareToIgnoreCase(lst[j + 1]);
          swap = diff > 0;
        }

        if (swap) {
          tmp = lst[j + 1];
          lst[j + 1] = lst[j];
          lst[j] = tmp;
          tw = weight[j + 1];
          weight[j + 1] = weight[j];
          weight[j] = tw;
          if (v != null) {
            boolean b = v[j];
            v[j] = v[j + 1];
            v[j + 1] = b;
          }
          end = false;
        }

        j++;
      }
      i--;
    }

  }

  // *****************************************************************************************************************

  public static boolean contains(String[] l, String item) {
    return (isInsideArray(item,l)>=0);
  }

  public static boolean contains(int[] arr, int item) {

    boolean found = false;
    int i = 0;

    while (i < arr.length && !found) {
      found = (arr[i] == item);
      if (!found) i++;
    }

    return found;

  }

  // *****************************************************************************************************************
  // Helper function

  public static String stringArrayToString(String[] res) {

    String result = "";
    int i;

    if (res != null)
      for (i = 0; i < res.length; i++) {
        result += res[i];
        if ((i + 1) < res.length) result += "\n";
      }

    return result;
  }

  static public String extractEndValue(String s) {
    int i = s.lastIndexOf('=');
    if (i != -1)
      return s.substring(i + 2, s.length());
    else
      return "";
  }

  static public String[] extractPollingInfo(String s) {

    String[] splitted = s.split("\n");

    String[] ret = new String[5];
    if (splitted[0].startsWith("Polled attribute"))
      ret[0] = "attribute";
    else
      ret[0] = "command";

    ret[1] = extractEndValue(splitted[0]);
    ret[2] = extractEndValue(splitted[1]);
    ret[3] = extractEndValue(splitted[2]);

    String status = "";
    for (int i = 3; i < splitted.length; i++) {
      status += splitted[i] + "\n";
    }

    ret[4] = status;

    return ret;
  }

  public static String[] makeStringArray(String value) {
    // Remove extra \n at the end of the string (not handled by split)
    while (value.endsWith("\n")) value = value.substring(0, value.length() - 1);
    return value.split("\n");
  }

  public static DbDatum[] makeDbDatum(String prop_name, String value) {

    String[] splitted = makeStringArray(value);
    DbDatum[] ret = new DbDatum[1];

    if (splitted.length == 1) {
      ret[0] = new DbDatum(prop_name, splitted[0]);
    } else {
      ret[0] = new DbDatum(prop_name, splitted);
    }

    return ret;
  }

  // *****************************************************************************************************************
  // Return Polling status for the specified command
  // field 0 => type (command or attribute)
  // field 1 => object name
  // field 2 => Polling period
  // field 3 => Ring Buffer Depth
  // field 4 => status
  static public String getPollingStatus(String devname, String name, String type,  int field) {

    String result = null;
    int i = 0;

    try {

      DeviceProxy ds = new DeviceProxy(devname);
      String[] pi = ds.polling_status();

      boolean found = false;
      String[] pinfo = null;
      for (i = 0; i < pi.length && !found;) {
        pinfo = extractPollingInfo(pi[i]);
        found = pinfo[0].equalsIgnoreCase(type) && pinfo[1].equalsIgnoreCase(name);
        if (!found) i++;
      }

      if (found) {
        result = pinfo[field];
      }

    } catch (DevFailed e) {

      // Return the error with status field else null
      if (field == 4) {
        result = "";
        for (i = 0; i < e.errors.length; i++) {
          result += "Desc -> " + e.errors[i].desc + "\n";
          result += "Reason -> " + e.errors[i].reason + "\n";
          result += "Origin -> " + e.errors[i].origin + "\n";
        }
      }

    }

    return result;
  }

  // ***************************************************************************************************
  // AddPollingObject

  static public void addPolling(String devname, String name, String type, String polling_period) throws DevFailed {

    //System.out.println("addPolling on " + adminname + " (" + devname + "," + type + "," + name + ")");
    int period;

    try {
      period = Integer.parseInt(polling_period);
    } catch (NumberFormatException e) {
      showJiveError("Invalid polling period " + devname + "/" + name + ":" + e.toString());
      return;
    }

    DeviceProxy ds = new DeviceProxy(devname);

    if(type.equalsIgnoreCase("command"))
      ds.poll_command(name,period);
    else
      ds.poll_attribute(name,period);

  }

  // ***************************************************************************************************
  // RemovePollingObject

  static public void remPolling(String devname, String name, String type) throws DevFailed {

    //System.out.println("remPolling on " + adminname + " (" + devname + "," + type + "," + name + ")");
    DeviceProxy ds = new DeviceProxy(devname);
    if(type.equalsIgnoreCase("command"))
      ds.stop_poll_command(name);
    else
      ds.stop_poll_attribute(name);

  }

  // *****************************************************************************************************************
  // Copy all property from a class to the clipboard

  public static void copyClassProperties(Database db,String classname) {

    String[] list;
    String[] list2;
    int i,j;

    try {

      // Get class prop list
      list = db.get_class_property_list(classname, "*");
      for (i = 0; i < list.length; i++) {
        String[] res = db.get_class_property(classname, list[i]).extractStringArray();
        the_clipboard.add(list[i], stringArrayToString(res));
      }

      // Get device attribute prop list
      list = db.get_class_attribute_list(classname, "*");
      for (i = 0; i < list.length; i++) {
        String att_list[] = {list[i]};
        DbAttribute lst[] = db.get_class_attribute_property(classname, att_list);
        if (lst.length > 0) {
          list2 = lst[0].get_property_list();
          for (j = 0; j < list2.length; j++) {
            the_clipboard.add(list2[j], list[i], lst[0].get_string_value(j));
          }
        }
      }

    } catch (DevFailed e) {
      showTangoError(e);
    }

  }

  // *****************************************************************************************************************
  // Return logging status
  // field 0 => logging level
  // field 1 => logging target

  public static String[] getLoggingStatus(Database db,String devname,int field) {

    String[] result = null;
    String devadmin;

    try {

      DbDevImportInfo info = db.import_device(devname);
      devadmin = "dserver/" + info.server;
      DeviceData argin = new DeviceData();
      DeviceProxy ds = new DeviceProxy(devadmin);
      DeviceData argout;

      switch(field) {

      case 0: // Trace level

        String[] names = new String[1];
        names[0] = devname;
        argin.insert(names);
        argout = ds.command_inout("GetLoggingLevel",argin);
        DevVarLongStringArray res=argout.extractLongStringArray();
        result = new String[1];
        result[0]=logging_level[res.lvalue[0]];
        return result;

      case 1: // Target

        argin.insert(devname);
        argout = ds.command_inout("GetLoggingTarget",argin);
        result = argout.extractStringArray();
        return result;

      default:
        return null;
      }

    } catch (DevFailed e) {

      showTangoError(e);
      return null;

    }

  }

  public static void launchAtkPanel(String devName) {

    if( atkPanelCmdLine == null ) {
      atkPanelCmdLine = System.getProperty("ATKPANEL");
      if( atkPanelCmdLine == null )
        atkPanelCmdLine = "";
    }

    String tgHost = null;
    try {
      tgHost = ApiUtil.get_db_obj().get_tango_host();
    } catch (DevFailed e) {
      atkPanelCmdLine = "";
    }

    if(atkPanelCmdLine.length()>0) {

      // Launch from shell
      try {
        Runtime.getRuntime().exec(atkPanelCmdLine+" " + tgHost + " " + devName);
      } catch (IOException e) {
        JiveUtils.showJiveError("Cannot launch AtkPanel\n"+e.getMessage());
      }

    } else {

      // Launch inside same JVM
      new atkpanel.MainPanel(devName, false, true, !JiveUtils.readOnly);

    }

  }

  public static boolean setLoggingLevel(Database db,String devname,String level) {

    // Find the corresponding string
    int i=isInsideArray(level,logging_level);

    if( i<0 ) {
      String err="Possible value are:";
      for(i=0;i<logging_level.length;i++) err+=" " + logging_level[i];
      showJiveError("Invalid logging level value.\n" + err);
      return false;
    }

    // Apply logging level
    try {

      DbDevImportInfo info = db.import_device(devname);
      String devadmin = "dserver/" + info.server;
      DeviceData argin = new DeviceData();
      DeviceProxy ds = new DeviceProxy(devadmin);
      int[]    l = new int[1];
      String[] s = new String[1];
      l[0]=i;
      s[0]=devname;
      DevVarLongStringArray la = new DevVarLongStringArray(l,s);
      argin.insert(la);
      ds.command_inout("SetLoggingLevel",argin);

    } catch (DevFailed e) {

      showTangoError(e);
      return false;

    }

    return true;

  }

  public static boolean setLoggingTarget(Database db,String devname,String tg) {

    String[]   settg;
    Vector     toAdd = new Vector();
    Vector     toRemove = new Vector();
    DeviceData argin;
    DeviceData argout;
    String[]   tin;
    int i;

    settg = tg.split("\n");

    // Apply logging target
    try {

      DbDevImportInfo info = db.import_device(devname);
      String devadmin = "dserver/" + info.server;
      DeviceProxy ds = new DeviceProxy(devadmin);
      argin = new DeviceData();
      argin.insert(devname);

      // Get current logging config
      argout = ds.command_inout("GetLoggingTarget",argin);
      String[] curtg = argout.extractStringArray();
      //for(i=0;i<curtg.length;i++) System.out.println("Cur["+i+"]:"+curtg[i]);

      // Check what has to be removed
      for(i=0;i<curtg.length;i++)
        if( !contains(settg,curtg[i]) ) toRemove.add(curtg[i]);

      // Check what has to be added
      for(i=0;i<settg.length;i++)
        if( !contains(curtg,settg[i]) && settg[i].length()>0 ) toAdd.add(settg[i]);

      // Remove item
      if( toRemove.size()>0 ) {
        tin = new String[toRemove.size()*2];
        for(i=0;i<toRemove.size();i++) {
          tin[2*i]   = devname;
          tin[2*i+1] = (String)toRemove.get(i);
          //System.out.println("Removing log target:" + tin[2*i+1]);
        }
        argin = new DeviceData();
        argin.insert(tin);
        ds.command_inout("RemoveLoggingTarget",argin);
      }

      // Add item
      if( toAdd.size()>0 ) {
        tin = new String[toAdd.size()*2];
        for(i=0;i<toAdd.size();i++) {
          tin[2*i]   = devname;
          tin[2*i+1] = (String)toAdd.get(i);
          //System.out.println("Adding log target:" + tin[2*i+1]);
        }
        argin = new DeviceData();
        argin.insert(tin);
        ds.command_inout("AddLoggingTarget",argin);
      }

    } catch (DevFailed e) {

      showTangoError(e);
      return false;

    }

    return true;

  }

  // *****************************************************************************************************************
  // Remove all property from a class

  public static boolean removeClassProperties(Database db,String classname) {

    String[] list;
    String[] list2;
    int i,j;
    String value = "%";

    try {

      // Get class prop list
      list = db.get_class_property_list(classname, "*");
      for (i = 0; i < list.length; i++) {
        //System.out.println("Removing: " + classname + " PROP " + list[i] );
        db.delete_class_property(classname, makeDbDatum(list[i], value));
      }

      // Get device attribute prop list
      list = db.get_class_attribute_list(classname, "*");
      for (i = 0; i < list.length; i++) {
        String att_list[] = {list[i]};
        DbAttribute lst[] = db.get_class_attribute_property(classname, att_list);
        if (lst.length > 0) {
          list2 = lst[0].get_property_list();
          for (j = 0; j < list2.length; j++) {
            System.out.println("Removing: " + classname + " ATT " + list[i] + " PROP " + list2[j]);
            db.delete_class_attribute_property( classname , list[i] , list2[j] );
          }
        }
      }

    } catch (DevFailed e) {
      showTangoError(e);
      return false;
    }

    return true;

  }

  // *****************************************************************************************************************
  // Copy all device properties into the clipboard

  public static void copyDeviceProperties(Database db,String devname) {

    String[] list;
    String[] list2;
    int i,j;

    try {

      // Get device prop list
      list = db.get_device_property_list(devname, "*");
      for (i = 0; i < list.length; i++) {
        String[] res = db.get_device_property(devname, list[i]).extractStringArray();
        the_clipboard.add(list[i], stringArrayToString(res));
      }

      // Get device attribute prop list
      list = db.get_device_attribute_list(devname);
      for (i = 0; i < list.length; i++) {
        String att_list[] = {list[i]};
        DbAttribute lst[] = db.get_device_attribute_property(devname, att_list);
        if (lst != null)
          if (lst.length > 0) {
            list2 = lst[0].get_property_list();
            for (j = 0; j < list2.length; j++) {
              the_clipboard.add(list2[j], list[i], lst[0].get_string_value(j));
            }
          }
      }

    } catch (DevFailed e) {
      showTangoError(e);
    }

  }

  // *****************************************************************************************************************
  // Remove all property from a device

  public static boolean removeDeviceProperties(Database db,String devname) {

    String[] list;
    String[] list2;
    int i,j;
    String value = "%";

    try {

      // Get device prop list
      list = db.get_device_property_list(devname, "*");
      for (i = 0; i < list.length; i++) {
        //System.out.println("Removing: " + devname + " PROP " + list[i] );
        db.delete_device_property(devname, makeDbDatum(list[i], value));
      }

      // Get device attribute prop list
      list = db.get_device_attribute_list(devname);
      for (i = 0; i < list.length; i++) {
        String att_list[] = {list[i]};
        DbAttribute lst[] = db.get_device_attribute_property(devname, att_list);
        if (lst.length > 0) {
          list2 = lst[0].get_property_list();
          for (j = 0; j < list2.length; j++) {
            //System.out.println("Removing: " + devname + " ATT " + list[i] + " PROP " + list2[j] );
            DbAttribute att = new DbAttribute(list[i]);
            att.add(list2[j], value);
            db.delete_device_attribute_property(devname, att);
          }
        }
      }

    } catch (DevFailed e) {
      showTangoError(e);
      return false;
    }

    return true;

  }

  // *****************************************************************************************************************
  // Show a tango error
  public static void showTangoError(DevFailed e) {


    if (e != null) {

      String[] result = new String[e.errors.length*3];

      for (int i = 0; i < e.errors.length; i++) {
        result[3*i]   = "Desc -> " + e.errors[i].desc;
        result[3*i+1] = "Reason -> " + e.errors[i].reason;
        result[3*i+2] = "Origin -> " + e.errors[i].origin;
      }

      if (result.length > 0)
        MessageDialog.showMessageDialog(parent, "Tango error", result, MessageDialog.ERROR_MESSAGE);

    }

  }

  public static void printTangoError(DevFailed e) {

    String result = "";

    if (e != null) {

      for (int i = 0; i < e.errors.length; i++) {
        result += " Desc -> " + e.errors[i].desc + "\n";
        result += " Reason -> " + e.errors[i].reason + "\n";
        result += " Origin -> " + e.errors[i].origin + "\n";
      }
      System.out.println("Tango exception:");
      System.out.print(result);

    }

  }

  // *****************************************************************************************************************
  // Show a Jive error

  public static void showJiveError(String msg) {
    MessageDialog.showMessageDialog(parent, "Jive error", new String[]{msg} , MessageDialog.ERROR_MESSAGE);
  }

  public static void showJiveErrors(String[] msg) {
    MessageDialog.showMessageDialog(parent, "Jive error", msg , MessageDialog.ERROR_MESSAGE);
  }

  public static void showJiveErrors(Vector<String> msg) {
    String[] errs = new String[msg.size()];
    for(int i=0;i<msg.size();i++)
      errs[i] = msg.get(i);
    MessageDialog.showMessageDialog(parent, "Jive error", errs , MessageDialog.ERROR_MESSAGE);
  }

  public static void showJiveWarning(String msg) {
    MessageDialog.showMessageDialog(parent, "Jive warning", new String[]{msg} , MessageDialog.WARNING_MESSAGE);
  }

  // *****************************************************************************************************************
  // Compute bound rectangle for a node
  static public Rectangle computeBounds(JTree tree, TreePath selPath) {
    tree.scrollPathToVisible(selPath);
    Rectangle r = tree.getPathBounds(selPath);
    Point pto = r.getLocation();
    SwingUtilities.convertPointToScreen(pto, tree);
    r.setLocation(pto);
    r.width += 20;
    r.height += 2;
    return r;
  }

  // *****************************************************************************************************************
  public static void printFormatedRes(String name, String[] vals, FileWriter fw)
      throws IOException {

    int i,j,k,shift;
    String to_write;

    // Check null value
    if (vals == null) {
      if( fw!=null ) {
        fw.write("\"\"");
      } else {
        System.out.print("\"\"");
      }
      return;
    }

    if (vals.length == 0) {
      if( fw!=null ) {
        fw.write("\"\"");
      } else {
        System.out.print("\"\"");
      }
      return;
    }

    // Convert String with \n to String[]
    Vector values = new Vector();

    for(j=0,i=0;i<vals.length;i++) {
      if( vals[i].indexOf('\n')!=-1 ) {
        String[] tmpStr = JiveUtils.makeStringArray(vals[i]);
        for(k=0;k<tmpStr.length;k++)
          values.add(tmpStr[k]);
      } else {
        values.add(vals[i]);
      }
    }

    Object[] value = values.toArray();

    shift = name.length();
    if( fw!=null ) {
      fw.write(name, 0, shift);
    } else {
      System.out.print(name);
    }

    for (j = 0; j < value.length; j++) {

      String str = (String) value[j];

      // Zero length not allowed
      if (str.length() == 0)
        value[j] = new String("\"\"");

      // backslash quotes
      StringBuffer qStr = new StringBuffer(str);
      int qIdx = qStr.indexOf("\"");
      while(qIdx!=-1) {
        qStr.insert(qIdx,"\\");
        qIdx = qStr.indexOf("\"",qIdx+2);
      }
      str = qStr.toString();

      // backslash backslash
      qStr = new StringBuffer(str);
      qIdx = qStr.indexOf("\\");
      while(qIdx!=-1) {
        qStr.insert(qIdx,"\\");
        qIdx = qStr.indexOf("\\",qIdx+2);
      }
      str = qStr.toString();

      // Quote resource with space or special char
      if (str.indexOf(' ') != -1 || str.indexOf('/') != -1 || str.indexOf(',') != -1 || str.indexOf('"') != -1)
        value[j] = new String("\"" + str + "\"");

      // Justify
      for (k = 0 ; j > 0 && k < shift ; k++) {
        if( fw!=null ) {
          fw.write(32);
        } else {
          System.out.print(" ");
        }
      }

      // Array
      if (j < value.length - 1)
        to_write = value[j] + ",\\ \n";
      else
        to_write = value[j] + "\n";

      if( fw!=null ) {
        fw.write(to_write, 0, to_write.length());
      } else {
        System.out.print(to_write.toString());
      }

    }

  }

  // *****************************************************************************************************************
  public static String convertEventString(String str) {
    int idx = str.indexOf(',');
    if(idx!=-1) {
      String[] ret = str.split(",");
      return ret[0]+"\n"+ret[1];
    } else {
      return str;
    }
  }

  static public void printAttInfo(AttributeInfoEx ai) {

    System.out.println("------- Attribute info -------");
    System.out.println("Name             :"+ai.name);
    System.out.println("Data format      :"+ai.data_format.value());
    System.out.println("Data type        :"+ai.data_type);
    System.out.println("Description      :"+ai.description);
    System.out.println("Display unit     :"+ai.display_unit);
    System.out.println("Std unit         :"+ai.standard_unit);
    System.out.println("Unit             :"+ai.unit);
    System.out.println("Format           :"+ai.format);
    System.out.println("Label            :"+ai.label);
    System.out.println("Disp level       :"+ai.level.value());
    System.out.println("Max alarm        :"+ai.max_alarm);
    System.out.println("Min alarm        :"+ai.min_alarm);
    System.out.println("Max value        :"+ai.max_value);
    System.out.println("Min value        :"+ai.min_value);
    System.out.println("Max DimX         :"+ai.max_dim_x);
    System.out.println("Max DimY         :"+ai.max_dim_y);
    System.out.println("Alarms.delta_t   :"+ai.alarms.delta_t);
    System.out.println("Alarms.delta_v   :"+ai.alarms.delta_val);
    System.out.println("Alarms.max       :"+ai.alarms.max_alarm);
    System.out.println("Alarms.min       :"+ai.alarms.min_alarm);
    System.out.println("Alarms.max_w     :"+ai.alarms.max_warning);
    System.out.println("Alarms.min_w     :"+ai.alarms.min_warning);
    System.out.println("ArchEvent.abs_ch :"+ai.events.arch_event.abs_change);
    System.out.println("ArchEvent.rel_ch :"+ai.events.arch_event.rel_change);
    System.out.println("ArchEvent.period :"+ai.events.arch_event.period);
    System.out.println("ChEvent.abs_ch   :"+ai.events.ch_event.abs_change);
    System.out.println("ChEvent.rel_ch   :"+ai.events.ch_event.rel_change);
    System.out.println("PerEvent.abs_ch  :"+ai.events.per_event.period);

  }

  /**
   * Center the given dialog according to its parent.
   */
  public static void centerDialog(Dialog dlg,int dlgWidth,int dlgHeight) {

    // Get the parent rectangle
    Rectangle r = new Rectangle(0,0,0,0);
    if (dlg.getParent()!=null && dlg.getParent().isVisible())
      r = dlg.getParent().getBounds();

    // Check rectangle validity
    if(r.width==0 || r.height==0) {
      r.x = 0;
      r.y = 0;
      r.width  = screenSize.width;
      r.height = screenSize.height;
    }

    // Get the window insets.
    dlg.pack();
    Insets insets = dlg.getInsets();

    // Center
    int xe,ye,wx,wy;
    wx = dlgWidth  + (insets.right + insets.left);
    wy = dlgHeight + (insets.bottom + insets.top);
    xe = r.x + (r.width - wx) / 2;
    ye = r.y + (r.height - wy) / 2;

    // Saturate
    if( xe<0 ) xe=0;
    if( ye<0 ) ye=0;
    if( (xe+wx) > screenSize.width )
      xe = screenSize.width - wx;
    if( (ye+wy) > screenSize.height )
      ye = screenSize.height - wy;

    // Set bounds
    dlg.setBounds(xe, ye, wx, wy);

  }

  /**
   * Center the given dialog according to its parent.
   */
  public static void centerDialog(Dialog dlg) {

    dlg.pack();

    // Get the parent rectangle
    Rectangle r = new Rectangle(0,0,0,0);
    if (dlg.getParent()!=null && dlg.getParent().isVisible())
      r = dlg.getParent().getBounds();

    // Check rectangle validity
    if(r.width==0 || r.height==0) {
      r.x = 0;
      r.y = 0;
      r.width  = screenSize.width;
      r.height = screenSize.height;
    }

    // Center
    int xe,ye,wx,wy;
    wx = dlg.getPreferredSize().width;
    wy = dlg.getPreferredSize().height;
    xe = r.x + (r.width - wx) / 2;
    ye = r.y + (r.height - wy) / 2;

    // Saturate
    if( xe<0 ) xe=0;
    if( ye<0 ) ye=0;
    if( (xe+wx) > screenSize.width )
      xe = screenSize.width - wx;
    if( (ye+wy) > screenSize.height )
      ye = screenSize.height - wy;

    // Set bounds
    dlg.setBounds(xe, ye, wx, wy);

  }

  /**
   * Center the given frame on screen. The frame is not displayed
   * after a call to this function, a call to setVisible() is needed.
   */
  public static void centerFrameOnScreen(Frame fr) {

    Rectangle r = new Rectangle(0,0,screenSize.width,screenSize.height);
    fr.pack();

    // Center
    int xe,ye,wx,wy;
    wx = fr.getPreferredSize().width;
    wy = fr.getPreferredSize().height;
    xe = r.x + (r.width - wx) / 2;
    ye = r.y + (r.height - wy) / 2;

    // Set bounds
    fr.setBounds(xe, ye, wx, wy);

  }

  /**
   * Center the given frame on screen. The frame is not displayed
   * after a call to this function, a call to setVisible() is needed.
   */
  public static void centerFrameOnScreen(Frame fr,int frWidth,int frHeight) {

    Rectangle r = new Rectangle(0,0,screenSize.width,screenSize.height);
    fr.pack();

    // Center
    int xe,ye,wx,wy;
    wx = frWidth;
    wy = frHeight;
    xe = r.x + (r.width - wx) / 2;
    ye = r.y + (r.height - wy) / 2;

    // Set bounds
    fr.setBounds(xe, ye, wx, wy);

  }

  /**
   * Return true if devName has a correct device name syntax
   * @param devName Name to be checked
   */
  static public boolean isDeviceName(String devName)
  {

    if(devName.length()<5 || devName.contains(" ")) return false;

    boolean   devNamePattern;

    String s = new String(devName);

    // Remove the 'tango:'
    if(s.startsWith("tango:")) s = s.substring(6);

    // Check full syntax: //hostName:portNumber/domain/family/member
    devNamePattern = Pattern.matches("//[a-zA-Z_0-9]+:[0-9]+/[a-zA-Z_0-9\\.[-]]+/[a-zA-Z_0-9\\.[-]]+/[a-zA-Z_0-9\\.[-]]+", s);

    // Check classic syntax: domain/family/member
    if (devNamePattern == false)
      devNamePattern = Pattern.matches("[a-zA-Z_0-9\\.[-]]+/[a-zA-Z_0-9\\.[-]]+/[[a-zA-Z_0-9\\.][-]]+", s);

    // Check full syntax: //ipAdress:portNumber/domain/family/member
    if (devNamePattern == false)
      devNamePattern = Pattern.matches("//[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+:[0-9]+/[a-zA-Z_0-9[-]]+/[a-zA-Z_0-9[-]]+/[a-zA-Z_0-9[-]]+", s);

    return devNamePattern;

  }

  /**
   * Return true if servName has a correct server name syntax
   * @param srvName Name to be checked
   */
  static public boolean isFullServerName(String srvName)
  {
    // Check classic syntax: Server/instance
    return Pattern.matches("[a-zA-Z_0-9\\.[-]]+/[a-zA-Z_0-9\\.[-]]+", srvName);
  }

  /**
   * Return a string corresponding to the path
   * @param path Path to convert
   */
  static public String getPathAsText(TreePath path) {

    StringBuffer str = new StringBuffer();
    if(path==null) {
      str.append("null");
    } else {
      for(int i=0;i<path.getPathCount();i++) {
        str.append(path.getPathComponent(i).toString());
        if(i!=path.getPathCount()-1) str.append("/");
      }
    }
    return str.toString();

  }

  static public void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {}
  }

  static private String starterDeviceHeader = null;
  static public String getStarterDeviceHeader() {
    if (starterDeviceHeader==null) {
      try {
        DbDatum datum = new DbClass("Starter").get_property("Domain");
        if (datum.is_empty())
          starterDeviceHeader = "tango/admin/"; // Not define --> default one
        else
          starterDeviceHeader = datum.extractString() + "/admin/"; // use specified domain
      }
      catch (DevFailed e) {
        starterDeviceHeader = "tango/admin/"; // Failed --> default one
      }
    }
    return starterDeviceHeader;
  }
  static public void refreshStarterDeviceHeader() {
    starterDeviceHeader = null;
  }

}
