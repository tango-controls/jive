package jive;

import fr.esrf.TangoApi.*;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevVarLongStringArray;

import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.*;
import java.awt.*;
import java.util.Stack;
import java.util.Vector;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

// Inner Class to handle search
class FindInfo {

  public boolean found;
  public int index;
  public TangoTreeNode nodeFound;

  FindInfo() {
    index = 0;
    found = false;
  }

  FindInfo(int i, boolean f, TangoTreeNode m) {
    index = i;
    found = f;
    nodeFound = m;
  }

}

// Inner class to handle the device panel
class JiveMenu extends JDialog {


  JiveMenu(Frame parent) {
    super(parent, false);
  }

  public void showDlg(String devname) {

    setTitle("Device Panel ["+devname+"]");
    try {
      ExecDev p = new ExecDev(devname);
      this.setContentPane(p);
      JiveUtils.centerDialog(this);
      setVisible(true);
    } catch(DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

}

/** Contains utils function for Jive */
public class JiveUtils {

  public static int nbAction = 29;
  public static RenameDlg dlg = null;

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
  public static atkpanel.MainPanel atkdlg = null;
  public static boolean error_report = true;
  public static boolean readOnly = false;
  public static FileWriter resFile = null;
  public static File lastFile = null;
  public static Vector savedClass = new Vector();
  public static Insets noMargin = new Insets(0,0,0,0);

  // Search options and control
  public static int scan_progress;
  static public Stack searchStack = null;
  static public String searchText;
  static public String searchTextValue;
  static public boolean searchIngoreCase;
  static public int     searchValues;
  static public boolean searchAttributes;
  static public boolean searchCommands;
  static public boolean searchUseRegexp;
  static public boolean searchOnlyLeaf;
  static public TreePath searchResult;
  static public TreePath[] searchResults;
  static public ThreadDlg searchDlg;

  //HDB stuff
  public static DeviceProxy hdbManager=null;
  public static boolean     hdbEnabled=false;

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
    return s.equals("is_polled") ||
        s.equals("polling_period") ||
        s.equals("poll_old_factor") ||
        s.equals("poll_ring_depth");
  }

  static public int IsAttCfgItem(String s,int idl) {

    int i = isInsideArray(s,att_prop_default);
    if(i<0 && idl>=3) {
      i = isInsideArray(s,att_prop_default_idl3);
      if(i>=0) i+=att_prop_default.length;
    }
    return i;

  }

  static public boolean IsSystemItem(String s) {
    return (s.equals("polled_cmd") ||
        s.equals("polled_attr") ||
        s.equals("non_auto_polled_cmd") ||
        s.equals("non_auto_polled_attr"));
  }

  static public boolean IsLogCfgItem(String s) {
    return (s.equals("logging_level") ||
        s.equals("logging_target") ||
        s.equals("current_logging_level") ||
        s.equals("current_logging_target") ||
        s.equals("logging_rft") ||
        s.equals("logging_path"));
  }

  static public boolean IsHdbCfgItem(String s) {
    return (s.equals("is_archived") ||
            s.equals("archiving_settings"));
  }

  static public boolean IsAlarmCfgItem(String s) {
      return s.equals("Alarms");
  }

  static public boolean IsEventCfgItem(String s) {
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
  // Return formatted path

  static public String formatPath(TreePath path) {

    String result = "";

    if (path == null)
      return result;

    int n = path.getPathCount();
    TangoTreeNode node = (TangoTreeNode) path.getLastPathComponent();

    for (int i = 1; i < n; i++) {

      result += path.getPathComponent(i);

      if (i < (n - 1) || !node.isLeaf())
        switch (node.type) {
          case 1:
            switch (i - 1) {
              case 0:
                result += ":";
                break;
              case 1:
                result += " ";
                break;
              case 2:
                result += ":";
                break;
              case 3:
                result += "/";
                break;
              default:
                result += " ";
            }
            break;
          case 2:
            switch (i - 1) {
              case 0:
                result += ":";
                break;
              case 1:
                result += "/";
                break;
              case 2:
                result += " CLASS:";
                break;
              default:
                result += " ";
            }

            break;
          case 3:
            switch (i - 1) {
              case 0:
                result += ":";
                break;
              case 1:
                result += "/";
                break;
              case 2:
                result += "/";
                break;
              case 3:
                result += " ";
                break;
              case 4:
                result += ":";
                break;
              case 5:
                result += "/";
                break;
              default:
                result += " ";
            }
            break;

          case 4:
          case 5:
            switch (i - 1) {
              case 0:
                result += ":";
                break;
              default:
                result += " ";
            }

            break;
        }

    }

    return result;
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

  public static boolean setLoggingLevel(Database db,String devname,String level) {

    // Find the corresponging string
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
      DeviceProxy ds = new DeviceProxy(devname);
      list = ds.get_attribute_list();
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
      DeviceProxy ds = new DeviceProxy(devname);
      list = ds.get_attribute_list();
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

  static public String[] getValue(TreePath path) {
    TangoTreeNode node = (TangoTreeNode) path.getLastPathComponent();
    return node.getValue();
  }

  static public boolean setValue(TreePath path, String value) {
    TangoTreeNode node = (TangoTreeNode) path.getLastPathComponent();
    return node.setValue(value);
  }

  // *****************************************************************************************************************
  // Show a tango error
  public static void showTangoError(DevFailed e) {

    String result = "";

    if (error_report && e != null) {

      for (int i = 0; i < e.errors.length; i++) {
        result += "Desc -> " + e.errors[i].desc + "\n";
        result += "Reason -> " + e.errors[i].reason + "\n";
        result += "Origin -> " + e.errors[i].origin + "\n";
      }

      if (result.length() > 0)
        JOptionPane.showMessageDialog(parent, result, "Tango error", JOptionPane.ERROR_MESSAGE);

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
    if (error_report) JOptionPane.showMessageDialog(parent, msg, "Jive error", JOptionPane.ERROR_MESSAGE);
  }

  public static void showJiveWarning(String msg) {
    JOptionPane.showMessageDialog(parent, msg, "Jive warning", JOptionPane.WARNING_MESSAGE);
  }

  // *****************************************************************************************************************
  // Check if a property already exists
  static public FindInfo propertyExist(String prop_name, DefaultTreeModel model, TangoTreeNode node) {
    // Check that property doesn't already exists
    int numChild = model.getChildCount(node);
    int i = 0;
    boolean found = false;
    TangoTreeNode elem = null;

    while (i < numChild && !found) {
      elem = (TangoTreeNode) model.getChild(node, i);
      found = elem.toString().compareToIgnoreCase(prop_name) == 0;
      if (!found) i++;
    }

    return new FindInfo(i, found, elem);
  }

  // *****************************************************************************************************************
  // Add a "New property" to the database and keep the tree synchronised
  static public TreePath addProperty(String prop_name, DefaultTreeModel model, TreePath path, String value, int pos) {

    TangoTreeNode node = (TangoTreeNode) path.getLastPathComponent();
    FindInfo fi;

    // Add it
    fi = propertyExist(prop_name, model, node);

    if (!fi.found) {
      TangoTreeNode n = new TangoTreeNode(node.level + 1, node.type, prop_name, node.getDB(), true);
      model.insertNodeInto(n, node, pos);
      TreePath np = path.pathByAddingChild(n);
      setValue(np, value);
      return np;
    } else {
      if (prop_name.toLowerCase().startsWith("new property")) {
        // Find new property number
        int n = 2;
        FindInfo f = propertyExist("New property#" + n, model, node);
        while (f.found) {
          n = n + 1;
          f = propertyExist("New property#" + n, model, node);
        }
        return addProperty("New property#" + n, model, path, value, pos);
      }
    }
    return fi.nodeFound.getCompletePath();

  }

  // *****************************************************************************************************************
  // Remove a property from the database and keep the tree synchronised
  static public void removeProperty(DefaultTreeModel model, TreePath path) {

    TangoTreeNode node = (TangoTreeNode) path.getLastPathComponent();
    if (setValue(path, "%")) model.removeNodeFromParent(node);

  }

  // *****************************************************************************************************************
  // Try to expand old path and return longest new valid path
  static public TreePath convertOldPath(DefaultTreeModel model, TangoTreeNode root, TreePath path) {

    TreePath np = new TreePath(root);

    if (path != null) {

      FindInfo f;
      int l = path.getPathCount();
      int i = 1;
      boolean end = false;
      TangoTreeNode r = root;

      while (!end && i < l) {
        TangoTreeNode old_node = (TangoTreeNode) path.getPathComponent(i);
        f = propertyExist(old_node.toString(), model, r);
        end = !f.found;
        if (!end) {
          r = (TangoTreeNode) model.getChild(r, f.index);
          np = np.pathByAddingChild(r);
          i++;
        }
      }

    }

    return np;
  }

  // *****************************************************************************************************************
  // Show the device/server dialog and exec action
  static public void showServerDlg(ServerDlg sdlg, TreePath path, DefaultTreeModel model, JTree tree) {

    TangoTreeNode node = (TangoTreeNode) path.getLastPathComponent();
    int i;

    if (node.level == 4) {
      // go back to server
      path = path.getParentPath();
      node = (TangoTreeNode) path.getLastPathComponent();
    }

    if (sdlg.showDlg()) {

      String[] devices = sdlg.getDeviceNames();
      String server = sdlg.getServerName();
      String classname = sdlg.getClassName();

      // Add a node for the server (if not exists)
      if (node.level == 1) path = addProperty(server, model, path, "", 0);

      // Add a node for the class (if not exists)
      TreePath np = addProperty(classname, model, path, "", 0);
      if (np != null) {
        tree.setSelectionPath(np);
      }

      // Add devices
      try {
        for (i = 0; i < devices.length; i++)
          node.getDB().add_device(new DbDevInfo(devices[i], classname, server));
      } catch (DevFailed e) {
        showTangoError(e);
      }

    }

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
  // Rename node
  static public boolean rename(TreePath path, JTree tree, DefaultTreeModel model, Rectangle bounds) {

    TangoTreeNode node = (TangoTreeNode) path.getLastPathComponent();
    TreePath np = null;
    int i,j,k;
    boolean need_refresh = false;
    boolean isAlive = false;

    // Show the appropriate rename dialog
//    if (((node.level == 5) && node.getParent().getParent().toString().equals("ATTRIBUTE"))
//            || ((node.level == 7) && node.getParent().getParent().toString().equals("ATTRIBUTE")))
//      dlg = new RenameDlg(parent, path.getLastPathComponent().toString(), att_prop_default, bounds);
//    else
    dlg = new RenameDlg(parent, path.getLastPathComponent().toString(), bounds);


    if (dlg.showDlg()) {

      DbServer dbs;
      String new_name = dlg.getNewName();
      FindInfo f1 = propertyExist(new_name, model, (TangoTreeNode) node.getParent());
      FindInfo f2 = propertyExist(node.toString(), model, (TangoTreeNode) node.getParent());
      if (f1.found && (f1.index == f2.index)) {

        // No change

      } else if (f1.found && (f1.index != f2.index)) {

        // Duplicate name
        showJiveError("Name already exists.");

      } else {

        if (node.type == 2 && node.level == 2) {

          // Rename a server ---------------------------------------------------------------

          // Create the new server
          np = addProperty(new_name, model, path.getParentPath(), "", 0);
          // Clone instances
          for (i = 0; i < node.getChildCount(); i++) {
            TreePath iPath = addProperty(node.getChildAt(i).toString(), model, np, "", i);
            // Clone classes
            for (j = 0; j < node.getChildAt(i).getChildCount(); j++) {
              TreePath cPath = addProperty(node.getChildAt(i).getChildAt(j).toString(), model, iPath, "", j);
              for (k = 0; k < node.getChildAt(i).getChildAt(j).getChildCount(); k++) {
                // Clone devices
                addProperty(node.getChildAt(i).getChildAt(j).getChildAt(k).toString(), model, cPath, "", k);
              }
            }
          }

          // Finaly remove old servers
          for (i = 0; i < node.getChildCount(); i++) {
            TangoTreeNode n = (TangoTreeNode) node.getChildAt(i);
            n.removeAllChildren();
            removeProperty(model, n.getCompletePath());
          }
          removeProperty(model, path);

        } else if (node.type == 2 && node.level == 3) {

          // Rename an instance ---------------------------------------------------------------

          // Create the new instance
          np = addProperty(new_name, model, path.getParentPath(), "", 0);
          // Clone classes
          for (i = 0; i < node.getChildCount(); i++) {
            TreePath iPath = addProperty(node.getChildAt(i).toString(), model, np, "", i);
            for (k = 0; k < node.getChildAt(i).getChildCount(); k++) {
              // Clone devices
              addProperty(node.getChildAt(i).getChildAt(k).toString(), model, iPath, "", k);
            }
          }

          // Finaly remove old intance
          for (i = 0; i < node.getChildCount(); i++) {
            TangoTreeNode n = (TangoTreeNode) node.getChildAt(i);
            n.removeAllChildren();
            removeProperty(model, n.getCompletePath());
          }
          removeProperty(model, path);

        } else if (node.type == 2 && node.level == 4) {

          // Rename a class ---------------------------------------------------------------
          try {

            // Get old device list
            dbs = new DbServer(node.getParent().getParent().toString() + "/" + node.getParent().toString());
            String[] devList = dbs.get_device_name(node.toString());

            // Add the new class
            np = addProperty(dlg.getNewName(), model, path.getParentPath(), "", f2.index);

            // Readd devices (They will be renamed by the Database server)
            if (np != null)
              for (i = 0; i < devList.length; i++)
                addProperty(devList[i], model, np, "", f2.index);

            // Finaly remove the node
            node.removeAllChildren();
            removeProperty(model, path);

          } catch (DevFailed e) {
            showTangoError(e);
          }

        } else if (node.type == 2 && node.level == 5) {

          // Rename a device ---------------------------------------------------------------
          String nDevName = dlg.getNewName();

          try {

            // Check if the device exixts
            DbDevImportInfo ii = node.getDB().import_device(nDevName);
            showJiveError("The device " + nDevName + " already exits.\nServer: " + ii.server);

          } catch (DevFailed e1) {

            // try to create the new device
            try {

              node.getDB().add_device(nDevName,
                                      node.getParent().toString(),
                                      node.getParent().getParent().getParent().toString() + "/" +
                                      node.getParent().getParent().toString());

              DeviceProxy ds = null;
              try {
                ds = new DeviceProxy(node.toString());
                ds.ping();
                isAlive=true;
              } catch (DevFailed e2) {}

              int ok = JOptionPane.showConfirmDialog(parent, "Do you want to copy propeties of " + node.toString() + " to " + nDevName + " ?", "Confirm propety move", JOptionPane.YES_NO_OPTION);
              if (ok == JOptionPane.YES_OPTION) {

                // Clone device properties
                String[] propList = node.getDB().get_device_property_list(node.toString(),"*");
                if (propList.length > 0) {
                  DbDatum[] data = node.getDB().get_device_property(node.toString(), propList);
                  node.getDB().put_device_property(nDevName, data);
                }

                // Clone attributes propeties
                if (isAlive) {
                  try {

                    String[] attList = ds.get_attribute_list();
                    if (attList.length > 0) {
                      DbAttribute[] adata = node.getDB().get_device_attribute_property(node.toString(), attList);
                      node.getDB().put_device_attribute_property(nDevName, adata);
                    }

                  } catch (DevFailed e3) {
                    showJiveError("Failed to copy attribute properties of " + node.toString() + "\n" + e3.errors[0].desc);
                  }
                } else {
                  showJiveError("Cannot copy attribute properties of " + node.toString() + "\nThe device is not alive.");
                }

              }

              // Remove the old device
              if(!isAlive)
                removeProperty(model, path);
              else
                showJiveWarning("The old device " + node.toString() + " is still alive and should be removed by hand.");

              np = path.getParentPath();
              need_refresh = true;

            } catch (DevFailed e4) {
              showTangoError(e4);
            }

          }

        } else {

          // Default Rename --------------------------------------------------------------

          String value = getValue(path)[0];
          removeProperty(model, path);
          np = addProperty(dlg.getNewName(), model, path.getParentPath(), value, f2.index);

        }

        if (np != null) {
          tree.setSelectionPath(np);
          tree.scrollPathToVisible(np);
        }
      }
    }

    //Destroy the dialog
    dlg = null;
    return need_refresh;

  }

  static public void paste(TangoTreeNode node) {

    int i;

    try {

      // Paste object property into a device
      if ((node.type == 3) && (node.level == 5) && (node.toString().equals("PROPERTY"))) {
        String devname = node.getDevname(1);
        for (i = 0; i < the_clipboard.getObjectPropertyLength(); i++) {
          node.getDB().put_device_property(devname,
                  makeDbDatum(the_clipboard.getObjectPropertyName(i), the_clipboard.getObjectPropertyValue(i)));
        }
      }

      // Paste attribute property into a device
      if ((node.type == 3) && (node.level == 5) && (node.toString().equals("ATTRIBUTE"))) {
        String devname = node.getDevname(1);
        for (i = 0; i < the_clipboard.getAttPropertyLength(); i++) {
          DbAttribute att = new DbAttribute(the_clipboard.getAttName(i));
          att.add(the_clipboard.getAttPropertyName(i), the_clipboard.getAttPropertyValue(i));
          node.getDB().put_device_attribute_property(devname, att);
        }
      }

      // Paste single attribute property in an attribute
      // Ignore attribute name
      if ((node.type == 3) && (node.level == 6) && (node.getParent().toString().equals("ATTRIBUTE"))) {
        String devname = node.getDevname(2);
        for (i = 0; i < the_clipboard.getAttPropertyLength(); i++) {
          DbAttribute att = new DbAttribute(node.toString());
          att.add(the_clipboard.getAttPropertyName(i), the_clipboard.getAttPropertyValue(i));
          node.getDB().put_device_attribute_property(devname, att);
        }
      }

      // Paste object property into a class
      if ((node.type == 1) && (node.level == 3) && (node.toString().equals("PROPERTY"))) {
        String classname = node.getParent().toString();
        for (i = 0; i < the_clipboard.getObjectPropertyLength(); i++) {
          node.getDB().put_class_property(classname,
                  makeDbDatum(the_clipboard.getObjectPropertyName(i), the_clipboard.getObjectPropertyValue(i)));
        }
      }

      // Paste object property into a free node
      if ((node.type == 4) && (node.level == 2)) {
        String objName = node.toString();
        for (i = 0; i < the_clipboard.getObjectPropertyLength(); i++) {
          node.getDB().put_property(objName,
                  makeDbDatum(the_clipboard.getObjectPropertyName(i), the_clipboard.getObjectPropertyValue(i)));
        }
      }

      // Paste attribute property into a device
      if ((node.type == 1) && (node.level == 3) && (node.toString().equals("ATTRIBUTE"))) {
        String classname = node.getParent().toString();
        for (i = 0; i < the_clipboard.getAttPropertyLength(); i++) {
          DbAttribute att = new DbAttribute(the_clipboard.getAttName(i));
          att.add(the_clipboard.getAttPropertyName(i), the_clipboard.getAttPropertyValue(i));
          node.getDB().put_class_attribute_property(classname, att);
        }
      }

      // Full device properties paste
      if (node.type == 3 && node.level == 4) {
        String devname = node.getDevname(0);
        for (i = 0; i < the_clipboard.getObjectPropertyLength(); i++) {
          node.getDB().put_device_property(devname,
                  makeDbDatum(the_clipboard.getObjectPropertyName(i), the_clipboard.getObjectPropertyValue(i)));
        }
        for (i = 0; i < the_clipboard.getAttPropertyLength(); i++) {
          DbAttribute att = new DbAttribute(the_clipboard.getAttName(i));
          att.add(the_clipboard.getAttPropertyName(i), the_clipboard.getAttPropertyValue(i));
          node.getDB().put_device_attribute_property(devname, att);
        }
      }


    } catch (DevFailed e) {
      showTangoError(e);
    }

  }

  // *****************************************************************************************************************
  // Execute a tree popup menu action
  static boolean execAction(int a,                     /* Action number              */
                            TreePath path,             /* Path to the selected node  */
                            JTree tree,                /* Parent tree                */
                            DefaultTreeModel model,    /* Tree model                 */
                            Rectangle bounds,          /* Tree bound rectangle       */
                            int idx,                   /* Multiple action node index */
                            int nbAction               /* Action number              */
                            ) {

    TangoTreeNode node = (TangoTreeNode) path.getLastPathComponent();
    TreePath np=null;
    ServerDlg sdlg;
    TangoTreeNode pnode = null;

    switch (a) {

      case 0:   // ------ CUT
      case 1:   // ------ COPY
        the_clipboard.clear();
      case 101: // ------ Multiple copy

        // Single copy
        if (node.isLeaf() && node.isCopiableItem()) {

          String val = getValue(path)[0];
          if ((node.type == 3 && node.level == 6) ||   // Device property
              (node.type == 1 && node.level == 4) ||   // Class property
              (node.type == 4 && node.level == 3)) {   // Free property
            // Adding property to the clipbaord
            the_clipboard.add(node.toString(), val);
          } else {
            // Adding attribute property to the clipbaord
            if (node.level == 7 /*Device*/ || node.level == 5 /*Class*/ ) {
              the_clipboard.add(node.toString(), node.getParent().toString(), val);
            } else if (node.level == 8) {
              the_clipboard.add(node.toString(), node.getParent().getParent().toString(), val);
            }
          }

        }

        // Full device propterty copy
        if ((node.type == 3 && node.level == 4)) {
          String devname = node.getDevname(0);
          copyDeviceProperties(node.getDB(),devname);
        }

        // Full class propterty copy
        if ((node.type == 1 && node.level == 2)) {
          copyClassProperties(node.getDB(),node.toString());
        }

        if (a == 0) removeProperty(model, path);	// Cut
        break;

      case 2: // ------ PASTE
        paste(node);
        return true;

      case 3:   // ------ DELETE
        if( JOptionPane.showConfirmDialog(parent, "Delete selection ?", "Confirm delete", JOptionPane.YES_NO_OPTION) ==
            JOptionPane.YES_OPTION ) {
          pnode = (TangoTreeNode) node.getParent();
          removeProperty(model, path);
          tree.setSelectionPath(pnode.getCompletePath());
        }
        break;
      case 103: // ------ Multiple delete
        removeProperty(model, path);
        break;

      case 4: // ------ ADD device
        {
          String devname = JOptionPane.showInputDialog(parent, "Enter device name", "Add device", JOptionPane.QUESTION_MESSAGE);
          if (devname != null) {
            np = addProperty(devname, model, path, "", 0);
            if (np != null) {
              tree.setSelectionPath(np);
            }
          }
        }
        break;

      case 5: // ------ Create server

        sdlg = new ServerDlg(parent);
        sdlg.setValidFields(true, true);
        sdlg.setDefaults("", "");
        showServerDlg(sdlg, path, model, tree);
        return true;

      case 6: // ------ Create new property

        if (node.level == 1) {
          // special case for object property
          String inputValue = JOptionPane.showInputDialog(parent, "Enter object name", "New property", JOptionPane.QUESTION_MESSAGE);
          np = addProperty(inputValue, model, path, "", 0);
          if (np != null) np = addProperty("New property", model, np, "", 0);
        } else
          np = addProperty("New property", model, path, "", 0);

        if (np != null) {
          // Automaticaly go to rename mode
          tree.setSelectionPath(np);
          Rectangle r = computeBounds(tree, np);
          execAction(9, np, tree, model, r, 0, 1);
        }

        break;

      case 8: // ------ Add a class to an existing server

        sdlg = new ServerDlg(parent);
        sdlg.setValidFields(false, true);
        sdlg.setDefaults(node.getParent().toString() + "/" + node.toString(), "");
        showServerDlg(sdlg, path, model, tree);
        return true;

      case 9: // ------ Rename
        return rename(path,tree,model,bounds);

      case 10: // ------ Monitor a device
        {
          String[] args = new String[1];

          if (node.isLeaf())
            args[0] = node.toString();
          else
            args[0] = node.getDevname(0);

          System.out.println("Running AtkPanel " + args[0]);
          atkdlg = new atkpanel.MainPanel(args[0], false, true, !JiveUtils.readOnly);
        }
        break;

      case 11:	// ------- Test a device
        {

          String devname;
          if (node.isLeaf())
            devname = node.toString();
          else
            devname = node.getDevname(0);

          JiveMenu m = new JiveMenu(parent);
          m.showDlg(devname);

        }

        break;

      case 12:	// ------- Show properties
        {

          // Show devices properties
          if ((node.type == 3 && node.level == 4)) {
            String devname = node.getDevname(0);
            // Define children
            int nc = node.getChildCount();
            // Go to device properties
            node = (TangoTreeNode) node.getChildAt(nc-3);
            node.editProperties("device:" + devname);
          }

          // Show device attribute properties
          if ((node.type == 3) && (node.level == 6) && (node.getParent().toString().equals("ATTRIBUTE"))) {
            String att_name = node.toString();
            node.editAttProperties("attribute:" + att_name);
          }

          // Show Class properties
          if ((node.type == 1 && node.level == 2)) {
            String classname = node.toString();
            // Define children
            node.getChildCount();
            // Go to device properties
            node = (TangoTreeNode) node.getChildAt(0);
            node.editProperties("class:" + classname);
          }

          // Show class attribute properties
          if ((node.type == 1) && (node.level == 4) && (node.getParent().toString().equals("ATTRIBUTE"))) {
            String att_name = node.toString();
            node.editProperties("attribute:" + att_name);
          }

          // Show device properties (from server browsing)
          if ((node.type == 2) && (node.level == 5)) {

            String devname = node.toString();
            TangoTreeNode root = (TangoTreeNode) path.getPathComponent(0);
            TangoTreeNode devnode = (TangoTreeNode) root.getChildAt(2);

            // Search the device node in the tree
            TangoTreeNode n = findDeviceNode(devnode, devname);

            if (n != null) {
              int nc = n.getChildCount();
              n = (TangoTreeNode) n.getChildAt(nc-3);
              n.editProperties("device:" + devname);
            }

          }

          // Show device properties (from alias browsing)
          if ((node.type == 5) && (node.level == 2)) {

            showJiveError("Edit properties from alias not yet supported");
            /*
            String devname        = node.toString();
            TangoTreeNode root    = (TangoTreeNode)path.getPathComponent(0);
            TangoTreeNode devnode = (TangoTreeNode)root.getChildAt(2);

            // Search the device node in the tree
            TangoTreeNode n = findDeviceNode( devnode , devname );

            if( n!=null ) {
              n.getChildCount();
              n = (TangoTreeNode)n.getChildAt(0);
              n.editProperties( "device:" + devname );
            }
            */
          }

        }
        break;

      case 13:	// ------- Test the admin server
        {

          String devname = "dserver/" + node.getParent().toString() + "/" + node.toString();

          JiveMenu m = new JiveMenu(parent);
          m.showDlg(devname);

        }
        break;

      case 14:	// ------- Unexport devices of a server
        {
          String srvName = node.getParent().toString() + "/" + node.toString();
          int ok = JOptionPane.showConfirmDialog(parent, "This will unexport all devices of " + srvName + "\n Do you want to continue ?", "Confirm unexport device", JOptionPane.YES_NO_OPTION);
          if (ok == JOptionPane.YES_OPTION) {
            try {
              //System.out.println(" Unexport device of" + srvName);
              node.getDB().unexport_server(srvName);
            } catch (DevFailed e) {
              showTangoError(e);
            }
          }

        }
        break;

      case 15:	// ------- Save server data
      case 115: // ------- Multiple Save
        {

          // Create the file at the first node
          if (resFile == null && idx==0) {

            JFileChooser chooser = new JFileChooser(".");
            int ok = JOptionPane.YES_OPTION;
            if (lastFile != null)
              chooser.setSelectedFile(lastFile);

            int returnVal = chooser.showSaveDialog(parent);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
              lastFile = chooser.getSelectedFile();
              if (lastFile != null) {
                if (lastFile.exists()) ok = JOptionPane.showConfirmDialog(parent, "Do you want to overwrite " + lastFile.getName() + " ?", "Confirm overwrite", JOptionPane.YES_NO_OPTION);
                if (ok == JOptionPane.YES_OPTION) {
                  try {
                    resFile = new FileWriter(lastFile.getAbsolutePath());
                    Date date = new Date(System.currentTimeMillis());
                    resFile.write("#\n# Resource backup , created " + date + "\n#\n\n");
                    savedClass.clear();
                  } catch (IOException e) {
                    JiveUtils.showJiveError("Failed to create resource file !\n" + e.getMessage());
                  }
                }
              }
            }

          }

          // Now save data

          if( resFile!=null ) {

            try {
              node.saveServerData(resFile);
              if(idx == nbAction-1) {
                // Last item, we have to close
                resFile.close();
                resFile = null;
              }
            } catch (IOException e) {
              JiveUtils.showJiveError("Failed to save resource file !\n" + e.getMessage());
            }

          }

        }
        break;

      case 16:	// ------- Define device alias
        {
          String inputValue = JOptionPane.showInputDialog(parent, "Enter alias", "Define device alias", JOptionPane.QUESTION_MESSAGE);

          // From device tree browsing
          if ((node.type == 3 && node.level == 4)) {
            String devname = node.getDevname(0);
            try {
              node.getDB().put_device_alias(devname, inputValue);
            } catch (DevFailed e) {
              showTangoError(e);
            }
          }

          // From server browsing
          if ((node.type == 2) && (node.level == 4)) {
            String devname = node.toString();
            try {
              node.getDB().put_device_alias(devname, inputValue);
            } catch (DevFailed e) {
              showTangoError(e);
            }
          }

        }
        break;

      case 17:	// ------- Go to server node
        {
          try {
            String devname = node.getDevname(0);
            DbDevImportInfo info = node.getDB().import_device(devname);
            TangoTreeNode root = (TangoTreeNode) path.getPathComponent(0);
            TangoTreeNode srvnode = (TangoTreeNode) root.getChildAt(1);

            // Search the server in the tree
            TangoTreeNode n = findServerNode(srvnode, info.server);
            if (n != null) {
              np = n.getCompletePath();
              tree.setSelectionPath(np);
              tree.scrollPathToVisible(np);
            }

          } catch (DevFailed e) {
            showTangoError(e);
          }
        }
        break;

      case 18:	// ------- Go to device node
        {
          String devname = node.toString();
          TangoTreeNode root = (TangoTreeNode) path.getPathComponent(0);
          TangoTreeNode devnode = (TangoTreeNode) root.getChildAt(2);

          // Search the server in the tree
          TangoTreeNode n = findDeviceNode(devnode, devname);
          if (n != null) {
            np = n.getCompletePath();
            tree.setSelectionPath(np);
            tree.scrollPathToVisible(np);
          }
        }
        break;

      case 19: // ----------- restart device
        try {

          String devname;
          if (node.type == 2)
            devname = node.toString();
          else
            devname = node.getDevname(0);
          DbDevImportInfo info = node.getDB().import_device(devname);
          DeviceProxy ds = new DeviceProxy("dserver/" + info.server);
          DeviceData in = new DeviceData();
          in.insert(devname);
          ds.command_inout("DevRestart", in);

        } catch (DevFailed e) {
          showTangoError(e);
        }
        return true;

      case 20: // ----------- Reset default value
        try {

          // Delete the property from the database
          String devname;
          DbAttribute att;
          if(node.level==7) {
            devname = node.getDevname(3);
            att = new DbAttribute(node.getParent().toString());
          } else {
            devname = node.getDevname(4);
            att = new DbAttribute(node.getParent().getParent().toString());
          }
          att.add(node.toString(), "");
          node.getDB().delete_device_attribute_property(devname, att);

          //Restart the device
          DbDevImportInfo info = node.getDB().import_device(devname);
          DeviceProxy ds = new DeviceProxy("dserver/" + info.server);
          DeviceData in = new DeviceData();
          in.insert(devname);
          ds.command_inout("DevRestart", in);

        } catch (DevFailed e) {
          showTangoError(e);
        }
        return true;

      case 21:	// ------- Go to device admin node
        {
          try {
            String devname = node.getDevname(0);
            DbDevImportInfo info = node.getDB().import_device(devname);
            TangoTreeNode root = (TangoTreeNode) path.getPathComponent(0);
            TangoTreeNode devnode = (TangoTreeNode) root.getChildAt(2);

            // Search the server in the tree
            TangoTreeNode n = findDeviceNode(devnode, "dserver/" + info.server);
            if (n != null) {
              np = n.getCompletePath();
              tree.setSelectionPath(np);
              tree.scrollPathToVisible(np);
            }

          } catch (DevFailed e) {
            showTangoError(e);
          }
        }
        break;

      case 22:	// ------- Change HDB mode
        {
          // No longer supported
        }
        break;

      case 23: // ------ Create am class attribute node (within the jive tree only)

        String inputValue = JOptionPane.showInputDialog(parent, "Enter class attribute name", "", JOptionPane.QUESTION_MESSAGE);
        np = addProperty(inputValue, model, path, "", 0);

        if (np != null)
          tree.setSelectionPath(np);

        break;

      case 24: // ------ Server wizard

        DevWizard wdlg = new DevWizard(parent);
        wdlg.showWizard(null);
        return true;

      case 25: // ------ Classes Wizard

        DevWizard cwdlg = new DevWizard(parent);
        cwdlg.showClassesWizard(node.getParent().toString() + "/" + node.toString());
        return true;

      case 26: // ------ Devices Wizard

        DevWizard dswdlg = new DevWizard(parent);
        dswdlg.showDevicesWizard(node.getParent().getParent().toString() + "/" + node.getParent().toString(),
                                node.toString());
        return true;

      case 27: // ------ Device Wizard

        DevWizard dwdlg = new DevWizard(parent);
        dwdlg.showDeviceWizard(node.getParent().getParent().getParent().toString() + "/" + node.getParent().getParent().toString(),
                               node.getParent().toString(),
                               node.toString());
        return true;

    }

    return false;
  }

  // ****************************************************************
  // Find a server node
  static public TangoTreeNode findServerNode(TangoTreeNode srv_node, String srvname) {
    int i;
    int count;
    boolean found;
    TangoTreeNode n = null;

    String[] srvnames = srvname.split("/");

    if (srvnames.length != 2) {
      showJiveError("Invalid server name:" + srvname);
      return null;
    }

    // find server
    count = srv_node.getChildCount();
    i = 0;
    found = false;
    while (!found && i < count) {
      n = (TangoTreeNode) srv_node.getChildAt(i);
      found = (srvnames[0].compareToIgnoreCase(n.toString()) == 0);
      if (!found) i++;
    }

    if (!found) {
      showJiveError("Server not found:" + srvname);
      return null;
    }

    // find instance
    srv_node = n;
    count = srv_node.getChildCount();
    i = 0;
    found = false;
    while (!found && i < count) {
      n = (TangoTreeNode) srv_node.getChildAt(i);
      found = (srvnames[1].compareToIgnoreCase(n.toString()) == 0);
      if (!found) i++;
    }

    if (!found) {
      showJiveError("Server not found:" + srvname);
      return null;
    }

    return n;

  }

  // ****************************************************************
  // Find a device node
  static public TangoTreeNode findDeviceNode(TangoTreeNode dev_node, String devname) {
    int i;
    int count;
    boolean found;
    TangoTreeNode n = null;

    String[] devnames = devname.split("/");

    if (devnames.length != 3) {
      showJiveError("Invalid device name:" + devname);
      return null;
    }

    // find device domain
    count = dev_node.getChildCount();
    i = 0;
    found = false;
    while (!found && i < count) {
      n = (TangoTreeNode) dev_node.getChildAt(i);
      found = (devnames[0].compareToIgnoreCase(n.toString()) == 0);
      if (!found) i++;
    }

    if (!found) {
      showJiveError("Device domain not found:" + devnames[0]);
      return null;
    }


    // find device family
    dev_node = n;
    count = dev_node.getChildCount();
    i = 0;
    found = false;
    while (!found && i < count) {
      n = (TangoTreeNode) dev_node.getChildAt(i);
      found = (devnames[1].compareToIgnoreCase(n.toString()) == 0);
      if (!found) i++;
    }

    if (!found) {
      showJiveError("Device family not found:" + devnames[1]);
      return null;
    }

    // find device member
    dev_node = n;
    count = dev_node.getChildCount();
    i = 0;
    found = false;
    while (!found && i < count) {
      n = (TangoTreeNode) dev_node.getChildAt(i);
      found = (devnames[2].compareToIgnoreCase(n.toString()) == 0);
      if (!found) i++;
    }

    if (!found) {
      showJiveError("Device member not found:" + devnames[2]);
      return null;
    }

    return n;
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

      // Quote resource with space or special char
      if (str.indexOf(' ') != -1 || str.indexOf('/') != -1 || str.indexOf(',') != -1)
        value[j] = new String("\"" + value[j] + "\"");

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

  // *****************************************************************************************************************
  // Initialise search
  static public void InitiateSearch(TangoTreeNode root, String textToFind , String textToFindValue , boolean ignorecase, int svalue, boolean sattribute, boolean scommand, boolean sregexp, boolean sonlyleaf) {
    searchStack = new Stack();
    searchStack.push(root);
    searchIngoreCase = ignorecase;
    searchValues = svalue;
    searchAttributes = sattribute;
    searchCommands = scommand;
    searchUseRegexp = sregexp;
    searchOnlyLeaf = sonlyleaf;
    searchText      = textToFind;
    searchTextValue = textToFindValue;
  }

  // *****************************************************************************************************************
  // Find a text in the tree ( text can be a regular expression )
  // return null when no path can be found
  static public TreePath findText() {

    //System.out.println("TangoTreeNode::findText() Entering...");

    Thread doSearch = new Thread() {
      public void run() {
        //System.out.println("Starting thread.");
        searchResult = findText_sub();
        searchDlg.hideDlg();
        //System.out.println("Ending thread.");
      }
    };

    //System.out.println("TangoTreeNode::findText() Thread created...");

    searchResult = null;
    searchDlg = new ThreadDlg(parent,"Searching the database",false, doSearch);

    //System.out.println("TangoTreeNode::findText() Dialog created...");
    searchDlg.showDlg();

    // Wait for thread completion
    try { doSearch.join();}
    catch (InterruptedException e) {}
    doSearch=null;

    //System.out.println("TangoTreeNode::findText() Exiting...");
    return searchResult;
  }

  static public TreePath[] findMultipleText() {

    boolean end = false;
    Vector pvect = new Vector();

    // Search the database
    while (!end) {
      searchResult = findText_sub();

      if (searchResult != null) {
        pvect.add(searchResult);
      } else {
        end = true;
      }
    }

    // Build result
    if ( (pvect.size()>0) && !ThreadDlg.stopflag ) {
      searchResults = new TreePath[pvect.size()];
      for (int i = 0; i < pvect.size(); i++) searchResults[i] = (TreePath) pvect.get(i);
    } else {
      searchResults = null;
    }

    return searchResults;
  }

  static public TreePath findText_sub() {

    int i;
    Pattern p = null;

    if (searchUseRegexp) {
      try {
        p = Pattern.compile(searchText);
      } catch (PatternSyntaxException e) {
        error_report = true;
        showJiveError("Invalid regular expression\n" + e.getDescription());
        return null;
      }
    }

    if (searchStack != null)
      while (!searchStack.empty() && !ThreadDlg.stopflag) {

        TangoTreeNode node = (TangoTreeNode) searchStack.pop();

        scan_progress++;
        String str1,str2;
        boolean ok=false;

        //System.out.println("Looping..." + scan_progress);

        if (node.isLeaf() || !searchOnlyLeaf) {

          if (searchIngoreCase) {
            str1 = node.toStringEx().toLowerCase();
            str2 = searchText.toLowerCase();
          } else {
            str1 = node.toStringEx();
            str2 = searchText;
          }

          //System.out.println("Comparing:" + str1 + "," + str2 );

          // Check property name
          if (searchUseRegexp) {
            ok = (p.matcher(str1).matches());
          } else {
            ok = (str1.indexOf(str2) != -1);
          }

          // Property match, now check for value
          if( ok ) {

            // No value check
            if( searchValues==0 )
              return node.getCompletePath();


            if (searchIngoreCase) {
              str1 = getValue(new TreePath(node))[0].toLowerCase();
              str2 = searchTextValue.toLowerCase();
            } else {
              str1 = getValue(new TreePath(node))[0];
              str2 = searchTextValue;
            }

            switch(searchValues) {
              case 1: // Strict equality
               if( str1.compareTo(str2)==0 )
                  return node.getCompletePath();
               break;

              case 2: // Contains
               if (str1.indexOf(str2) != -1)
                 return node.getCompletePath();
               break;
            }

          }

        }

        // Push children
        boolean perform_search = !((node.toString().equals("ATTRIBUTE") && !searchAttributes) ||
            (node.toString().equals("COMMAND") && !searchCommands));

        if (perform_search) {
          int count = node.getChildCount();
          for (i = count - 1; i >= 0; i--) searchStack.push(node.getChildAt(i));
        }

      }
    // No item found
    if(ThreadDlg.stopflag) System.out.println("Search interrupted by user.");
    return null;
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

  // *****************************************************************************************************************
  // Get the possible action according
  // to selection path
  // Return a boolean array
  // arr[0] = cut
  // arr[1] = copy
  // arr[2] = paste
  // arr[3] = delete
  // arr[4] = add device
  // arr[5] = create server
  // arr[6] = create new property
  // arr[7] = change tango host
  // arr[8] = add class to server
  // arr[9] = Rename
  // arr[10]= Monitor a device
  // arr[11]= Test a device
  // arr[12]= Show properties
  // arr[13]= Test admin server
  // arr[14]= Unexport devices
  // arr[15]= Save server data
  // arr[16]= Define device alias
  // arr[17]= Go to server node
  // arr[18]= Go to device node
  // arr[19]= Restart device
  // arr[20]= Set default value (config)
  // arr[21]= Go to device admin node
  // arr[22]= Set HDB mode (No longer supported)
  // arr[23]= Create class attribute
  // arr[24]= Server Wizard
  // arr[25]= Classes Wizard
  // arr[26]= Devices Wizard
  // arr[27]= Device Wizard

  static public boolean[] getAction(TreePath path) {

    TangoTreeNode node = (TangoTreeNode) path.getLastPathComponent();
    boolean[] arr = new boolean[nbAction];
    int i;

    for (i = 0; i < nbAction; i++) arr[i] = false;

    switch (node.type) {

      case 0: // ROOT NODE

        arr[7] = true;
        return arr;

      case 1: // CLASS

        if ((node.level == 2)) {
          arr[0] = !readOnly;
          arr[1] = !readOnly;
          arr[3] = !readOnly;
          arr[12] = true;
        }

        if (node.level == 2 || node.level == 3) {
          arr[2] = !the_clipboard.empty() && !readOnly;
        }

        if ((node.level == 3) && node.toString().equals("PROPERTY")) {
          arr[6] = !readOnly;
        }

        if ((node.level == 3) && node.toString().equals("ATTRIBUTE")) {
          arr[23] = !readOnly;
        }

        if ((node.level == 4) && node.getParent().toString().equals("PROPERTY")) {
          arr[0] = !readOnly;
          arr[1] = !readOnly;
          arr[3] = !readOnly;
          arr[9] = !readOnly;
        }

        if ((node.level == 4) && node.getParent().toString().equals("ATTRIBUTE")) {
          arr[12] = true;
          arr[6] = true  && !readOnly;
        }

        if ((node.level == 5) && node.getParent().getParent().toString().equals("ATTRIBUTE")) {
          arr[0] = !readOnly;
          arr[1] = !readOnly;
          arr[3] = !readOnly;
          arr[9] = !readOnly;
        }

        break;

      case 2: // SERVER

        if (node.level == 1) {
          arr[5] = !readOnly;
          arr[24] = !readOnly;
        }

        if (node.level == 2) {
          arr[9] = true  && !readOnly;
        }

        if (node.level == 3) {
          arr[3] = !readOnly;
          arr[8] = !readOnly;
          arr[9] = !readOnly;
          arr[13] = true;
          arr[14] = !readOnly;
          arr[15] = true;
          arr[25] = !readOnly;
        }

        if (node.level == 4) {
          arr[3] = !readOnly;
          arr[4] = !readOnly;
          arr[9] = !readOnly;
          arr[26] = !readOnly;
        }

        if (node.level == 5) {
          arr[3] = !readOnly;
          arr[9] = !readOnly;
          arr[10] = true;
          arr[11] = true;
          arr[12] = true;
          arr[16] = !readOnly;
          arr[18] = true;
          arr[19] = true;
          arr[27] = !readOnly;
        }

        break;
      case 3: // DEVICE

        if (node.level == 4 || node.level == 5) {
          arr[2] = !the_clipboard.empty()  && !readOnly;
        }

        if ((node.level == 4)) {
          arr[0] = !readOnly;
          arr[1] = !readOnly;
          arr[3] = !readOnly;
          arr[10] = true;
          arr[11] = true;
          arr[12] = true;
          arr[16] = !readOnly;
          arr[17] = true;
          arr[19] = true;
          arr[21] = true;
        }

        if ((node.level == 5) && node.toString().equals("PROPERTY")) {
          arr[6] = !readOnly;
          arr[2] = !the_clipboard.empty() && !readOnly;
        }

        if ((node.level == 6) && node.getParent().toString().equals("PROPERTY")) {
          arr[0] = !node.isSystemItem() && !node.isPollCfgItem()  && !readOnly;
          arr[1] = !node.isSystemItem() && !readOnly;
          arr[3] = !node.isSystemItem() && !node.isPollCfgItem() && !readOnly;
          arr[9] = !node.isSystemItem() && !node.isPollCfgItem() && !readOnly;
        }

        if ((node.level == 6) && node.getParent().toString().equals("ATTRIBUTE")) {
          arr[6] = !readOnly;
          arr[12] = true;
          arr[2] = !the_clipboard.empty() && !readOnly;
        }

        if ((node.level == 7) && node.getParent().getParent().toString().equals("ATTRIBUTE")) {
          arr[0] =!node.isEventCfgItem() && !node.isAlarmCfgItem() && !node.isPollCfgItem() && !node.isAttCfgItem() && !readOnly;
          arr[1] =!node.isEventCfgItem() && !node.isAlarmCfgItem() && !node.isPollCfgItem() && !readOnly;
          arr[3] =!node.isEventCfgItem() && !node.isAlarmCfgItem() && !node.isPollCfgItem() && !node.isAttCfgItem() && !readOnly;
          arr[9] =!node.isEventCfgItem() && !node.isAlarmCfgItem() && !node.isPollCfgItem() && !node.isAttCfgItem() && !readOnly;
          arr[20] = node.isAttCfgItem() && !readOnly;
        }

        if ((node.level == 8) && node.getParent().getParent().getParent().toString().equals("ATTRIBUTE")) {
          arr[1] = !readOnly;
          arr[20] = node.isAttCfgItem() && !readOnly;
        }

        break;
      case 4: // PROPERTY

        if ((node.level == 1)) {
          arr[6] = !readOnly;
        }

        if ((node.level == 2)) {
          arr[6] = !readOnly;
          arr[2] = !the_clipboard.empty() && !readOnly;
        }

        if ((node.level == 3)) {
          arr[0] = !readOnly;
          arr[1] = !readOnly;
          arr[3] = !readOnly;
          arr[9] = !readOnly;
        }

        break;

      case 5: // ALIAS
        if ((node.level == 2)) {
          arr[10] = true;
          arr[11] = true;
          arr[12] = true;
        }
        break;
    }

    return arr;

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

}
