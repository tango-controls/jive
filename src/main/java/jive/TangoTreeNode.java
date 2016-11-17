package jive;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.*;
import fr.esrf.TangoDs.TangoConst;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.io.FileWriter;
import java.io.IOException;

/**
 *  Tango database Tree viewer
 *  Jean-Luc PONS     2002
 *
 *  TreeNode that builds children on the fly.
 *  The key idea is that getChildCount is always called before
 *  any actual children are requested. So getChildCount builds
 *  the children if they don't already exist.
 */
public class TangoTreeNode extends DefaultMutableTreeNode {

  private boolean areChildrenDefined = false;
  public int type;
  public int level;
  private String value;
  private Database db;
  private boolean valid;
  public String help;
  public int idl = 0;

  // *****************************************************************************************************************
  // Construct a Tango Tree
  public TangoTreeNode(int level, int type, String value, Database db, boolean valid) {
    initNode(level,type,value,db,valid);
  }

  // Defaulyt initialisation
  private void initNode(int level, int type, String value, Database db, boolean valid) {

    this.level = level;
    this.type = type;
    this.value = value;
    this.db = db;
    this.valid = valid;
    help = TangoHelp.getHelp(toString(), type, level);

    if (!isValid()) {
      if (help != null)
        help += "\n";
      else
        help = "";
      if((level == 4) && (type == 2))
        help += "Warning: The class " + value + " is not defined for this server.";
      else
        help += "Warning: The value stored in the database differs from the server value";
    }

  }

  // Add a node and check value validity.
  void addNode(DbAttribute dbai,String name,String srvVal) {

    boolean valid=true;

    if (srvVal != null) {
      if (!dbai.is_empty(name)) {
        String dbVal = dbai.get_string_value(name);
        valid = srvVal.equalsIgnoreCase(dbVal);
      }
    }

    areChildrenDefined = true;
    addNode(new TangoTreeNode(level+1,type,name,db,valid));

  }

  void addNode(TangoTreeNode n) {
    n.idl = idl;
    add(n);
  }

  // Construct the attribute property node.
  void constructAttNode(String devName,String attName) throws DevFailed {

    String[] att_list = {attName};

    // Get values from database
    DbAttribute dbai = db.get_device_attribute_property(devName, att_list)[0];

    // Get values from server
    DeviceProxy ds = new DeviceProxy(devName);
    AttributeInfoEx ai = ds.get_attribute_info_ex(attName);

    // ------ Contruct event nodes ------------------------------------------------

    if(ai.events!=null) {

      TangoTreeNode archEvent = new TangoTreeNode(level+1,type,"Archive Event",db,true);
      addNode(archEvent);
      archEvent.addNode(dbai,"archive_abs_change",JiveUtils.convertEventString(ai.events.arch_event.abs_change));
      archEvent.addNode(dbai,"archive_rel_change",JiveUtils.convertEventString(ai.events.arch_event.rel_change));
      archEvent.addNode(dbai,"archive_period",ai.events.arch_event.period);

      TangoTreeNode changeEvent = new TangoTreeNode(level+1,type,"Change Event",db,true);
      addNode(changeEvent);
      changeEvent.addNode(dbai,"abs_change",JiveUtils.convertEventString(ai.events.ch_event.abs_change));
      changeEvent.addNode(dbai,"rel_change",JiveUtils.convertEventString(ai.events.ch_event.rel_change));

      TangoTreeNode periodEvent = new TangoTreeNode(level+1,type,"Periodic Event",db,true);
      addNode(periodEvent);
      periodEvent.addNode(dbai,"event_period",ai.events.per_event.period);

    }

    // ------ Contruct alarm node ------------------------------------------------

    TangoTreeNode alarms = new TangoTreeNode(level+1,type,"Alarms",db,true);
    addNode(alarms);
    if( ai.alarms!=null ) {

      alarms.addNode(dbai,"min_alarm",ai.alarms.min_alarm);
      alarms.addNode(dbai,"max_alarm",ai.alarms.max_alarm);
      alarms.addNode(dbai,"min_warning",ai.alarms.min_warning);
      alarms.addNode(dbai,"max_warning",ai.alarms.max_warning);
      alarms.addNode(dbai,"delta_t",ai.alarms.delta_t);
      alarms.addNode(dbai,"delta_val",ai.alarms.delta_val);

    } else {

      alarms.addNode(dbai,"min_alarm",ai.min_alarm);
      alarms.addNode(dbai,"max_alarm",ai.max_alarm);

    }

    // ------ Now add standart attribute property

    addNode(dbai,"is_polled",null);
    addNode(dbai,"polling_period",null);
    addNode(dbai,"description",ai.description);
    addNode(dbai,"label",ai.label);
    addNode(dbai,"unit",ai.unit);
    addNode(dbai,"standard_unit",ai.standard_unit);
    addNode(dbai,"display_unit",ai.display_unit);
    addNode(dbai,"format",ai.format);
    addNode(dbai,"min_value",ai.min_value);
    addNode(dbai,"max_value",ai.max_value);

    // ------ Now add free attribute property

    String[] propList = dbai.get_property_list();
    for(int i=0;i<propList.length;i++) {
      if(JiveUtils.IsAttCfgItem(propList[i],idl)<0) {
        addNode(dbai,propList[i],null);
      }
    }

    /* JiveUtils.sortList(list, valid); */

  }

  // consturct the Server class node
  void constructClassNode() throws DevFailed {

    String[] srvList = null;
    String[] dbList = null;
    int i;

    try {
      // Try to get class list through the admin device
      String admName = "dserver/" + getParent().toString() + "/" + toString();
      DeviceProxy adm = new DeviceProxy(admName);
      DeviceData datum = adm.command_inout("QueryClass");
      srvList = datum.extractStringArray();
    } catch (DevFailed e) {}

    // Get the list from the database
    DbServer dbs = new DbServer(getParent().toString() + "/" + toString());
    dbList = dbs.get_class_list();

    if(srvList!=null) {

      // Add actual class
      for (i = 0; i < srvList.length; i++)
        addNode(new TangoTreeNode(level + 1, type, srvList[i], db, true));

      // No add other class found in database as invalid
      for (i = 0; i < dbList.length; i++) {
        if(!JiveUtils.contains(srvList,dbList[i])) {
          addNode(new TangoTreeNode(level + 1, type, dbList[i], db, false));
        }
      }

    } else {

      // Old fashion
      for (i = 0; i < dbList.length; i++)
        addNode(new TangoTreeNode(level + 1, type, dbList[i], db, true));

    }

  }

  // *****************************************************************************************************************
  // Property fields
  public boolean isValid() {
    return valid;
  }

  public boolean isLeaf() {

    switch (type) {
      case 1: // CLASS
        if ((level == 4) && getParent().toString().equals("PROPERTY"))
          return true;
        if ((level == 5) && getParent().getParent().toString().equals("ATTRIBUTE"))
          return true;
        return false;
      case 2: // SERVER
        return (level >= 5);
      case 3: // DEVICE
        if ((level == 5) && !toString().equals("PROPERTY")
                && !toString().equals("ATTRIBUTE")
                && !toString().equals("COMMAND"))
          return true;
        if ((level == 6) && getParent().toString().equals("PROPERTY"))
          return true;
        if ((level == 7) && getParent().getParent().toString().equals("ATTRIBUTE"))
          return !(isAlarmCfgItem() || isEventCfgItem());
        if ((level == 7) && getParent().getParent().toString().equals("COMMAND"))
          return true;
        if ((level == 8) && getParent().getParent().getParent().toString().equals("ATTRIBUTE"))
          return true;

        return false;
      case 4: // PROPERTY
        if (level == 3) return true;
        return false;
      case 5: // ALIAS
        return (level >= 2);
      default:
        return false;
    }

  }

  public boolean isDeviceNode() {
    return toString().equals("PROPERTY") ||
            toString().equals("ATTRIBUTE") ||
            toString().equals("COMMAND");
  }

  public boolean isPollCfgItem() {
    return JiveUtils.IsPollCfgItem(toString());
  }

  public boolean isAttCfgItem() {
    return (JiveUtils.IsAttCfgItem(toString(),idl) != -1);
  }

  public boolean isAlarmCfgItem() {
    return (JiveUtils.IsAlarmCfgItem(toString()));
  }

  public boolean isEventCfgItem() {
    return (JiveUtils.IsEventCfgItem(toString()));
  }

  public boolean isSystemItem() {
    return JiveUtils.IsSystemItem(toString());
  }

  public boolean isLogCfgItem() {
    return JiveUtils.IsLogCfgItem(toString());
  }

  public boolean isCopiableItem() {
    return !(isLogCfgItem()   ||
             isPollCfgItem()  ||
             isSystemItem()   ||
             isAlarmCfgItem() ||
             isEventCfgItem() );
  }

  public int getChildCount() {
    if (!areChildrenDefined)
      defineChildNodes();
    return (super.getChildCount());
  }

  public Database getDB() {
    return db;
  }

  // *****************************************************************************************************************
  // Add dinamycaly nodes in the tree when the user open a branch.
  private void defineChildNodes() {

    //  The flag areChildrenDefined must set before defining children
    // Otherwise you get an infinite recursive loop, since add results
    // in a call to getChildCount.

    areChildrenDefined = true;
    int i;

    if (level == 0) {

      // ROOT node
      add(new TangoTreeNode(1, 1, "CLASS", db, true));
      add(new TangoTreeNode(1, 2, "SERVER", db, true));
      add(new TangoTreeNode(1, 3, "DEVICE", db, true));
      add(new TangoTreeNode(1, 4, "PROPERTY", db, true));
      add(new TangoTreeNode(1, 5, "ALIAS", db, true));

    } else if (db != null) {

      // Tango DB Browsing
      String[] list = null;
      String devname;

      try {
        switch (type) {

          case 1:  //*********************************** CLASS

            try {
              switch (level) {
                case 1:
                  list = db.get_class_list("*");
                  break;
                case 2:
                  list = list = new String[2];
                  list[0] = "PROPERTY";
                  list[1] = "ATTRIBUTE";
                  break;
                case 3:
                  if (toString().equals("PROPERTY")) {
                    list = db.get_class_property_list(getParent().toString(), "*");
                  } else {
                    list = db.get_class_attribute_list(getParent().toString(), "*");
                  }
                  break;
                case 4:
                  String[] att_list = {toString()};
                  DbAttribute lst[] = db.get_class_attribute_property(getParent().getParent().toString(), att_list);
                  if (lst.length > 0) list = lst[0].get_property_list();
                  break;

              }
            } catch (DevFailed e) {
              JiveUtils.showTangoError(e);
            }

            break;

          case 2:  //*********************************** SERVER

            try {
              DbServer dbs;

              switch (level) {

                case 1:
                  list = db.get_server_name_list();
                  break;
                case 2:
                  list = db.get_instance_name_list(toString());
                  break;
                case 3:
                  constructClassNode();
                  return;
                case 4:
                  dbs = new DbServer(getParent().getParent().toString() + "/" + getParent().toString());
                  list = dbs.get_device_name(toString());
                  break;

              }
            } catch (DevFailed e) {
              JiveUtils.showTangoError(e);
            }
            break;

          case 3:  //*********************************** DEVICE

            try {
              switch (level) {

                case 1:
                  list = db.get_device_domain("*");
                  break;
                case 2:
                  list = db.get_device_family(toString() + "/*");
                  break;
                case 3:
                  list = db.get_device_member(getParent().toString() + "/" + toString() + "/*");
                  break;
                case 4:

                  String dm = getParent().getParent().toString();
                  if (dm.equalsIgnoreCase("dserver"))
                    list = new String[11];
                  else
                    list = new String[10];

                  list[0] = "poll_old_factor";
                  list[1] = "poll_ring_depth";
                  list[2] = "logging_level";
                  list[3] = "current_logging_level";
                  list[4] = "logging_target";
                  list[5] = "current_logging_target";
                  list[6] = "logging_rft";

                  if (list.length == 11) {
                    list[7] = "logging_path";
                    list[8] = "PROPERTY";
                    list[9] = "ATTRIBUTE";
                    list[10] = "COMMAND";
                  } else {
                    list[7] = "PROPERTY";
                    list[8] = "ATTRIBUTE";
                    list[9] = "COMMAND";
                  }
                  break;

                case 5:
                  devname = getDevname(1);
                  if (toString().equals("PROPERTY")) {

                    String plist[] = db.get_device_property_list(devname, "*");
                    boolean[] iscfg = new boolean[plist.length];
                    int nbItem = 0;
                    int j = 0;

                    //Remove all configuration item
                    for (i = 0; i < plist.length; i++) {
                      iscfg[i] = JiveUtils.IsPollCfgItem(plist[i]) ||
                              JiveUtils.IsLogCfgItem(plist[i]);

                      if (!iscfg[i]) nbItem++;
                    }
                    list = new String[nbItem];
                    for (i = 0; i < plist.length; i++)
                      if (!iscfg[i]) list[j++] = plist[i];

                    JiveUtils.sortList(list, null, idl);

                  } else if (toString().equals("ATTRIBUTE")) {
                    DeviceProxy ds = new DeviceProxy(devname);
                    list = ds.get_attribute_list();
                    idl = ds.get_idl_version();
                  } else if (toString().equals("COMMAND")) {
                    DeviceProxy ds = new DeviceProxy(devname);
                    CommandInfo[] ci = ds.command_list_query();
                    list = new String[ci.length];
                    for (i = 0; i < ci.length; i++)
                      list[i] = ci[i].cmd_name;
                  }
                  break;

               case 6:
                  devname = getDevname(2);
                  if (getParent().toString().equals("ATTRIBUTE")) {
                    constructAttNode(devname,toString());
                    return;
                  } else if (getParent().toString().equals("COMMAND")) {
                    list = new String[2];
                    list[0] = "is_polled";
                    list[1] = "polling_period";
                  }

                  break;

              }
            } catch (DevFailed e) {
              JiveUtils.showTangoError(e);
            }
            break;

          case 4:  //*********************************** PROPERTY

            try {
              switch (level) {
                case 1:
                  list = db.get_object_list("*");
                  break;
                case 2:
                  list = db.get_object_property_list(toString(), "*");
                  break;
              }
            } catch (DevFailed e) {
              JiveUtils.showTangoError(e);
            }
            break;

          case 5:  //*********************************** ALIAS

            try {
              switch (level) {
                case 1:
                  list = db.get_device_alias_list("*");
                  break;
              }
            } catch (DevFailed e) {
              JiveUtils.showTangoError(e);
            }
            break;

        }
      } catch (Exception e) {
        System.out.println(e);
        e.printStackTrace();
      }

      if (list != null) {
        for (i = 0; i < list.length; i++)
          addNode(new TangoTreeNode(level + 1, type, list[i], db, true));
      }

    }

  }

// *****************************************************************************************************************
  // Get the property value (array[0]) according to the given path
  // and returns an empty string when no value is associated
  // with the path
  // The second element is the title of the view which will
  // display the result
  public String[] getValue() {

    String result = "";
    String pname = "...";
    String[] ret = new String[2];
    int i;

    if (db == null) {

      result = "No connection to database server.";

    } else {

      switch (type) {

        case 0: // Tango host node
          pname = "Database info";

          try {

            result = db.get_info();

          } catch (DevFailed e) {

            for (i = 0; i < e.errors.length; i++) {
              result += "Desc -> " + e.errors[i].desc + "\n";
              result += "Reason -> " + e.errors[i].reason + "\n";
              result += "Origin -> " + e.errors[i].origin + "\n";
            }

          }
          break;

        case 1: // CLASS

          // Getting class property value
          if ((level == 4) && getParent().toString().equals("PROPERTY"))
            try {
              String[] res = db.get_class_property(getParent().getParent().toString(), toString()).extractStringArray();

              result = JiveUtils.stringArrayToString(res);
              pname = "Property value";
              if (res.length >= 2) pname = pname + " [" + res.length + " items]";

            } catch (Exception e) {
              System.out.println(e);
              e.printStackTrace();
            }

          // Getting class attribute property value
          if ((level == 5) && getParent().getParent().toString().equals("ATTRIBUTE"))
            try {

              String[] att_list = {getParent().toString()};
              DbAttribute lst[] = db.get_class_attribute_property(getParent().getParent().getParent().toString(), att_list);

              if (lst != null)
                if (lst.length > 0) {
                  result += lst[0].get_string_value(toString());
                }
              pname = "Property value";

            } catch (Exception e) {
              System.out.println(e);
              e.printStackTrace();
            }

          break;

        case 2: // SERVER
        case 3: // DEVICE

          // Print device info
          if ((type == 3 && level == 4) || (type == 2 && level == 5)) {

            try {
              try {

                result = "- Device Info ----------------------------------------\n\n";
                String devname;
                if (type == 2)
                  devname = toString();
                else
                  devname = getDevname(0);

                DeviceProxy dbdev = new DeviceProxy(devname);
                DbDevImportInfo info = dbdev.import_device();
                result += info.toString();

                // Append Polling status
                result += "\n\n- Polling Status -------------------------------------\n\n";
                String[] pi = dbdev.polling_status();
                for (i = 0; i < pi.length; i++) result += (pi[i] + "\n\n");

                pname = "Device info";

              } catch (DevFailed e) {

                for (i = 0; i < e.errors.length; i++) {
                  result += "Desc -> " + e.errors[i].desc + "\n";
                  result += "Reason -> " + e.errors[i].reason + "\n";
                  result += "Origin -> " + e.errors[i].origin + "\n";
                }

              }

            } catch (Exception e) {
              System.out.println(e);
              e.printStackTrace();
            }
          }

          // Print server info
          if ((type == 2) && (level == 3)) {

            try {
              try {

                DbServer dbserv = new DbServer(toString());
                DbServInfo info = dbserv.get_info();
                result = info.toString();
                pname = "Server info";

              } catch (DevFailed e) {

                for (i = 0; i < e.errors.length; i++) {
                  result += "Desc -> " + e.errors[i].desc + "\n";
                  result += "Reason -> " + e.errors[i].reason + "\n";
                  result += "Origin -> " + e.errors[i].origin + "\n";
                }

              }

            } catch (Exception e) {
              System.out.println(e);
              e.printStackTrace();
            }
          }

          if (type == 2) break;
          // Device node only

          // Configuration stuff
          if ((level == 5) && !isDeviceNode()) {

            String[] res = null;
            String devname = getDevname(1);

            if (isPollCfgItem()) {
              pname = "Polling configuration property";
            } else if (isLogCfgItem()) {
              pname = "Logging configuration property";
            }

            try {
              if (toString().equals("current_logging_level")) {

                // Getting values from the device
                res = JiveUtils.getLoggingStatus(db, devname, 0);
                result = JiveUtils.stringArrayToString(res);

              } else if (toString().equals("current_logging_target")) {

                // Getting values from the device
                res = JiveUtils.getLoggingStatus(db, devname, 1);
                result = JiveUtils.stringArrayToString(res);

              } else {
                res = db.get_device_property(devname, toString()).extractStringArray();
                result = JiveUtils.stringArrayToString(res);
              }

            } catch (Exception e) {
            }

            if (res == null || res.length == 0) {

              // Return known default value
              if (toString().equals("poll_old_factor")) {
                result = "4";
              } else if (toString().equals("poll_ring_depth")) {
                result = "10";
              } else if (toString().equals("logging_rft")) {
                result = "2";
              }

            } else {
              if (res.length >= 2) pname = pname + " [" + res.length + " items]";
            }

          }

          // Getting device property value
          if ((level == 6) && getParent().toString().equals("PROPERTY")) {

            try {

              if (isSystemItem())
                pname = "System property";
              else
                pname = "Property value";

              String devname = getDevname(2);
              String[] res = db.get_device_property(devname, toString()).extractStringArray();
              result = JiveUtils.stringArrayToString(res);

              if (res != null)
                if (res.length >= 2) pname = pname + " [" + res.length + " items]";

            } catch (Exception e) {
              System.out.println(e);
              e.printStackTrace();
            }

          }

          // Getting device attribute property value
          if ((level == 6) && getParent().toString().equals("ATTRIBUTE")) {

            String devname = getDevname(2);
            try {
              result += "- Attribute Info -------------------------------------\n\n";
              DeviceProxy ds = new DeviceProxy(devname);
              AttributeInfo ai = ds.get_attribute_info(toString());

              result += "Data type:     " + TangoConst.Tango_CmdArgTypeName[ai.data_type] + "\n";
              result += "Data format:   " + AttributePanel.getFormatString(ai) + "\n";
              result += "Display level: " + ai.level + "\n";
              result += "Writable:      " + AttributePanel.getWriteString(ai) + "\n";
              result += "Writable name: " + ai.writable_attr_name + "\n";
              if (ai.extensions.length > 0) {
                for (i = 0; i < ai.extensions.length; i++)
                  result += "Extension " + i + "  :" + ai.extensions[0] + "\n";
              } else {
                result += "No extensions.\n";
              }
            } catch (DevFailed e) {
            }

            result += "\n\n- Polling Status -------------------------------------\n\n";
            String ps = JiveUtils.getPollingStatus(devname, toString(), "attribute", 4);
            if (ps == null)
              result += "The attribute is not polled";
            else
              result += ps;

            pname = "Attribute info";

          }

          if ((level == 7) && getParent().getParent().toString().equals("ATTRIBUTE")) {

            try {

              String name = toString();

              if (name.equals("is_polled")) {
                result = JiveUtils.getPollingStatus(getDevname(3), getParent().toString(), "attribute", 1);
                if (result == null)
                  result = "No";
                else
                  result = "Yes";
                pname = "Polling configuration property";
              } else if (name.equals("polling_period")) {
                result = JiveUtils.getPollingStatus(getDevname(3), getParent().toString(), "attribute", 2);
                if (result == null) result = "";
                pname = "Polling configuration property";
              } else {

                if (!JiveUtils.IsAlarmCfgItem(toString()) && !JiveUtils.IsEventCfgItem(toString())) {

                  String devname = getDevname(3);

                  int p = JiveUtils.IsAttCfgItem(toString(),idl);
                  if (p < 0) {

                    // Free property
                    String[] att_list = {getParent().toString()};
                    DbAttribute lst[] = db.get_device_attribute_property(devname, att_list);

                    if (lst != null)
                      if (lst.length > 0) {
                        result += lst[0].get_string_value(toString());
                      }
                    pname = "Property value";

                  } else {

                    // Attrribute config

                    DeviceProxy ds = new DeviceProxy(devname);
                    AttributeInfoEx ai = ds.get_attribute_info_ex(getParent().toString());
                    switch (p) {
                      case 0:
                        result += ai.description;
                        break;
                      case 1:
                        result += ai.label;
                        break;
                      case 2:
                        result += ai.unit;
                        break;
                      case 3:
                        result += ai.standard_unit;
                        break;
                      case 4:
                        result += ai.display_unit;
                        break;
                      case 5:
                        result += ai.format;
                        break;
                      case 6:
                        result += ai.min_value;
                        break;
                      case 7:
                        result += ai.max_value;
                        break;
                    }
                    pname = "Attribute configuration property";
                  }
                }
              }
            } catch (Exception e) {
              System.out.println(e);
              e.printStackTrace();
            }

          }

          if ((level == 8) && getParent().getParent().getParent().toString().equals("ATTRIBUTE")) {

            String devname = getDevname(4);
            pname = "Attribute configuration property";

            try {
              DeviceProxy ds = new DeviceProxy(devname);
              AttributeInfoEx ai = ds.get_attribute_info_ex(getParent().getParent().toString());
              int p = JiveUtils.IsAttCfgItem(toString(),idl);
              switch (p) {
                // --------------
                case 8:
                  result += ai.min_alarm;
                  break;
                case 9:
                  result += ai.max_alarm;
                  break;
                case 10:
                  if(ai.alarms!=null) result += ai.alarms.min_warning;
                  break;
                case 11:
                  if (ai.alarms != null) result += ai.alarms.max_warning;
                  break;
                case 12:
                  if (ai.alarms != null) result += ai.alarms.delta_t;
                  break;
                case 13:
                  if (ai.alarms != null) result += ai.alarms.delta_val;
                  break;
                // --------------
                case 14:
                  if (ai.events != null) result += ai.events.ch_event.rel_change;
                  break;
                case 15:
                  if (ai.events != null)  result += ai.events.ch_event.abs_change;
                  break;
                case 16:
                  if (ai.events != null)  result += ai.events.per_event.period;
                  break;
                case 17:
                  if (ai.events != null)  result += ai.events.arch_event.abs_change;
                  break;
                case 18:
                  if (ai.events != null)  result += ai.events.arch_event.rel_change;
                  break;
                case 19:
                  if (ai.events != null)  result += ai.events.arch_event.period;
                  break;
                default:
                  result = "Not supported property :" + toString();
                  break;
              }
            } catch (Exception e) {
              System.out.println(e);
              e.printStackTrace();
            }

          }

          // Comnmands
          if ((level == 6) && getParent().toString().equals("COMMAND")) {

            result = JiveUtils.getPollingStatus(getDevname(2), toString(), "command", 4);
            if (result == null) result = "The command is not polled";
            pname = "Command info";

          }

          if ((level == 7) && getParent().getParent().toString().equals("COMMAND")) {
            if (toString().equals("is_polled")) {
              result = JiveUtils.getPollingStatus(getDevname(3), getParent().toString(), "command", 1);
              if (result == null)
                result = "No";
              else
                result = "Yes";
            } else if (toString().equals("polling_period")) {
              result = JiveUtils.getPollingStatus(getDevname(3), getParent().toString(), "command", 2);
              if (result == null) result = "";
            }
            pname = "Polling configuration property";
          }

          break;

        case 4: // PROPERTY

          // Getting device property value
          if (level == 3) {
            try {

              String[] res = db.get_property(getParent().toString(), toString()).extractStringArray();

              result = JiveUtils.stringArrayToString(res);
              pname = "Property value";
              if (res.length >= 2) pname = pname + " [" + res.length + " items]";

            } catch (Exception e) {
              System.out.println(e);
              e.printStackTrace();
            }
          }

          break;

        case 5: // ALIAS
          if (level == 2) {

            try {

              String devname;
              devname = toString();
              DbDevice dbdev = new DbDevice(devname);
              DbDevImportInfo info = dbdev.import_device();
              result = info.toString();
              pname = "Device info";

            } catch (DevFailed e) {

              for (i = 0; i < e.errors.length; i++) {
                result += "Desc -> " + e.errors[i].desc + "\n";
                result += "Reason -> " + e.errors[i].reason + "\n";
                result += "Origin -> " + e.errors[i].origin + "\n";
              }

            }
          }

          break;
      }
    }

    ret[0] = result;
    ret[1] = pname;
    return ret;

  }

  // *****************************************************************************************************************
  // Set the property value according to the given path
  // multinline value are interpreted as string array
  // return true if action has been succesfully done
  public boolean setValue(String value) {

    int i;

    switch (type) {

      case 1: // CLASS

        // Remove all property for a class
        if (level == 2 && value.equals("%")) {
          String classname = toString();
          int ok = JOptionPane.showConfirmDialog(JiveUtils.parent, "This will erase all class and attribute properties for " + classname + "\n Do you want to continue ?", "Confirm delete class properties", JOptionPane.YES_NO_OPTION);
          if (ok == JOptionPane.YES_OPTION)
            return JiveUtils.removeClassProperties(db, classname);
          else
            return false;
        }

        // Set/Remove class property value
        if ((level == 4) && getParent().toString().equals("PROPERTY"))
          try {
            if (value.equals("%"))
              db.delete_class_property(getParent().getParent().toString(), JiveUtils.makeDbDatum(toString(), value));
            else
              db.put_class_property(getParent().getParent().toString(), JiveUtils.makeDbDatum(toString(), value));
          } catch (DevFailed e) {
            JiveUtils.showTangoError(e);
            return false;
          }

        // Set/Remove class attribute property value
        if ((level == 5) && getParent().getParent().toString().equals("ATTRIBUTE"))
          try {
            String className = getParent().getParent().getParent().toString();
            DbAttribute att = new DbAttribute(getParent().toString());
            att.add(toString(), value);

            if (!value.equals("%"))
              db.put_class_attribute_property(className, att);
            else
              db.delete_class_attribute_property(className, getParent().toString(), toString());

          } catch (DevFailed e) {
            JiveUtils.showTangoError(e);
            return false;
          }

        break;

      case 2: // Server

        if (level == 3) {
          if (value.equals("%")) {
            try {
              db.delete_server(getParent().toString() + "/" + toString());
            } catch (DevFailed e) {
              JiveUtils.showTangoError(e);
              return false;
            }
          }
        }

        if (level == 4) {

          try {

            if (value.equals("%")) {
              // remove all device for the specified class
              for (i = 0; i < getChildCount(); i++)
                db.delete_device(getChildAt(i).toString());
            }

          } catch (DevFailed e) {
            JiveUtils.showTangoError(e);
            return false;
          }

        }

        if (level == 5) {
          try {
            if (value.equals("%"))
              db.delete_device(toString());
            else {
              db.add_device(toString(),
                            getParent().toString(),
                            getParent().getParent().getParent().toString() + "/" + getParent().getParent().toString());
            }
          } catch (DevFailed e) {
            JiveUtils.showTangoError(e);
            return false;
          }
        }
        break;

      case 3: // DEVICE

        // Remove all property for a device
        if (level == 4 && value.equals("%")) {
          String devname = getDevname(0);
          int ok = JOptionPane.showConfirmDialog(JiveUtils.parent, "This will erase all device and attribute properties for " + devname + "\n Do you want to continue ?\nHint: To remove the device itself from the database,\ngo to the server node and delete the device\nfrom its class.", "Confirm delte device properties", JOptionPane.YES_NO_OPTION);
          if (ok == JOptionPane.YES_OPTION)
            return JiveUtils.removeDeviceProperties(db, devname);
          else
            return false;
        }

        // Configuration stuff
        if ((level == 5) && !isDeviceNode()) {

          String devname = getDevname(1);

          try {

            if (toString().equals("current_logging_target")) {

              return JiveUtils.setLoggingTarget(db, devname, value);

            } else if (toString().equals("current_logging_level")) {

              return JiveUtils.setLoggingLevel(db, devname, value);

            } else {

              if (value.equals("%"))
                db.delete_device_property(devname, JiveUtils.makeDbDatum(toString(), value));
              else
                db.put_device_property(devname, JiveUtils.makeDbDatum(toString(), value));
            }

          } catch (DevFailed e) {
            JiveUtils.showTangoError(e);
            return false;
          }

        }

        // Set/Remove device property value
        if ((level == 6) && getParent().toString().equals("PROPERTY")) {
          try {

            String devname = getDevname(2);

            if (value.equals("%"))
              db.delete_device_property(devname, JiveUtils.makeDbDatum(toString(), value));
            else
              db.put_device_property(devname, JiveUtils.makeDbDatum(toString(), value));

          } catch (DevFailed e) {
            JiveUtils.showTangoError(e);
            return false;
          }
        }

        // Set/Remove device attribute property value
        if ((level == 7) && getParent().getParent().toString().equals("ATTRIBUTE")) {

          try {

            String devname = getDevname(3);
            int p = JiveUtils.IsAttCfgItem(toString(),idl);

            if (isPollCfgItem()) {

              // Update attribute polling config
              String period = JiveUtils.getPollingStatus(getDevname(3), getParent().toString(), "attribute", 2);

              if (toString().equals("is_polled")) {
                if (value.equalsIgnoreCase("yes")) {
                  if (period == null) {
                    // Add the object
                    JiveUtils.addPolling(devname, getParent().toString(), "attribute", "3000");
                  }
                } else {
                  if (period != null) {
                    // Remove the object
                    JiveUtils.remPolling(devname, getParent().toString(), "attribute");
                  }
                }
              } else if (toString().equals("polling_period")) {

                if (value.length() > 0) {
                  if (period == null) {
                    // Add the objet
                    JiveUtils.addPolling(devname, getParent().toString(), "attribute", value);

                  } else {
                    // Update the polling period
                    JiveUtils.addPolling(devname, getParent().toString(), "attribute", value);
                  }
                }

              }

            } else if (p >= 0) {

              // Set attribute configuration
              DeviceProxy ds = new DeviceProxy(devname);
              AttributeInfo ai = ds.get_attribute_info(getParent().toString());

              switch (p) {
                case 0:
                  ai.description = value;
                  break;
                case 1:
                  ai.label = value;
                  break;
                case 2:
                  ai.unit = value;
                  break;
                case 3:
                  ai.standard_unit = value;
                  break;
                case 4:
                  ai.display_unit = value;
                  break;
                case 5:
                  ai.format = value;
                  break;
                case 6:
                  ai.min_value = value;
                  break;
                case 7:
                  ai.max_value = value;
                  break;
              }

              AttributeInfo[] ais = new AttributeInfo[1];
              ais[0] = ai;
              ds.set_attribute_info(ais);

            } else {

              // Free property
              DbAttribute att = new DbAttribute(getParent().toString());
              att.add(toString(), value);
              if (value.equals("%"))
                db.delete_device_attribute_property(devname, att);
              else
                db.put_device_attribute_property(devname, att);

            }

          } catch (DevFailed e) {
            JiveUtils.showTangoError(e);
            return false;
          }

        }

        // Set/Remove device attribute property value
        if ((level == 8) && getParent().getParent().getParent().toString().equals("ATTRIBUTE")) {

          try {

            String devname = getDevname(4);
            int p = JiveUtils.IsAttCfgItem(toString(),idl);

            if (p >= 0) {

              // Set attribute configuration
              DeviceProxy ds = new DeviceProxy(devname);
              AttributeInfoEx ai = ds.get_attribute_info_ex(getParent().getParent().toString());

              // Check here that the device support attribute property
              if(ai.alarms==null || ai.events==null) {
                if(p>9) {
                  JiveUtils.showJiveError("The attribute property " + toString() + " is not supported by the device.");
                  return false;
                }
              }

              switch (p) {
                // --------------
                case 8:
                  ai.min_alarm = value;
                  if(ai.alarms!=null)
                    ai.alarms.min_alarm = value;
                  break;
                case 9:
                  ai.max_alarm = value;
                  if(ai.alarms!=null)
                    ai.alarms.max_alarm = value;
                  break;
                case 10:
                  ai.alarms.min_warning = value;
                  break;
                case 11:
                  ai.alarms.max_warning = value;
                  break;
                case 12:
                  ai.alarms.delta_t = value;
                  break;
                case 13:
                  ai.alarms.delta_val = value;
                  break;
                // --------------
                case 14:
                  ai.events.ch_event.rel_change = value;
                  break;
                case 15:
                  ai.events.ch_event.abs_change = value;
                  break;
                case 16:
                  ai.events.per_event.period = value;
                  break;
                case 17:
                  ai.events.arch_event.abs_change = value;
                  break;
                case 18:
                  ai.events.arch_event.rel_change = value;
                  break;
                case 19:
                  ai.events.arch_event.period = value;
                  break;
                default:
                  JiveUtils.showJiveError("Unexpected property : " + toString());
                  break;
              }

              AttributeInfoEx[] ais = new AttributeInfoEx[1];
              ais[0] = ai;
              ds.set_attribute_info(ais);

            }

          } catch (DevFailed e) {
            JiveUtils.showTangoError(e);
            return false;
          }

        }

        if ((level == 7) && getParent().getParent().toString().equals("COMMAND")) {

          try {

            String devname = getDevname(3);

            if (isPollCfgItem()) {

              // Update command polling config
              String period = JiveUtils.getPollingStatus(getDevname(3), getParent().toString(), "command", 2);

              if (toString().equals("is_polled")) {
                if (value.equalsIgnoreCase("yes")) {
                  if (period == null) {
                    // Add the object
                    JiveUtils.addPolling(devname, getParent().toString(), "command", "3000");
                  }
                } else {
                  if (period != null) {
                    // Remove the object
                    JiveUtils.remPolling(devname, getParent().toString(), "command");
                  }
                }
              } else if (toString().equals("polling_period")) {
                if (period == null) {
                  // Add the objet
                  JiveUtils.addPolling(devname, getParent().toString(), "command", value);

                } else {
                  // Update the polling period
                  JiveUtils.addPolling(devname, getParent().toString(), "command", value);

                }
              }

            }

          } catch (DevFailed e) {
            JiveUtils.showTangoError(e);
            return false;
          }

        }

        break;
      case 4: // PROPERTY

        // Set/Remove property value
        if (level == 3) {
          try {

            if (value.equals("%"))
              db.delete_property(getParent().toString(), toString());//JiveUtils.makeDbDatum(toString(), value));
            else
              db.put_property(getParent().toString(), JiveUtils.makeDbDatum(toString(), value));

          } catch (DevFailed e) {
            JiveUtils.showTangoError(e);
            return false;
          }
        }

        break;
      case 5: // ALIAS
        break;
    }

    return true;

  }

  // *****************************************************************************************************************
  // Retreive the device name in the given tree path
  public String getDevname(int level) {

    TreeNode father = this;

    for (int i = 0; i < level; i++)
      father = father.getParent();

    return father.getParent().getParent().toString() + "/" +
            father.getParent().toString() + "/" +
            father.toString();

  }

  // *****************************************************************************************************************
  // Save all data belonging to a server
  public void saveServerData(FileWriter fw) throws IOException {

    int i,j,k,l;

    String srvName = getParent().toString() + "/" + toString();
    boolean prtOut;

    try {

      DbServer dbs = new DbServer(srvName);
      String[] class_list = dbs.get_class_list();

      for (i = 0; i < class_list.length; i++) {

        String[] prop_list;
        String[] att_list;
        DbAttribute lst[];

        // We save class properties only once
        if( !JiveUtils.isSavedClass(class_list[i]) ) {

          fw.write("#---------------------------------------------------------\n");
          fw.write("# CLASS " + class_list[i] + " properties\n");
          fw.write("#---------------------------------------------------------\n\n");

          prop_list = db.get_class_property_list(class_list[i], "*");
          for (j = 0; j < prop_list.length; j++) {
            String[] value = db.get_class_property(class_list[i], prop_list[j]).extractStringArray();
            if (prop_list[j].indexOf(' ') != -1) prop_list[j] = "\"" + prop_list[j] + "\"";
            JiveUtils.printFormatedRes("CLASS/" + class_list[i] + "->" + prop_list[j] + ": ", value, fw);
          }

          att_list = db.get_class_attribute_list(class_list[i], "*");
          lst = db.get_class_attribute_property(class_list[i], att_list);
          prtOut = false;
          for (k = 0; k < lst.length; k++) {
            prop_list = lst[k].get_property_list();
            for (j = 0; j < prop_list.length; j++) {
              if(!prtOut) {
                fw.write("\n# CLASS " + class_list[i] + " attribute properties\n\n");
                prtOut=true;
              }
              if (prop_list[j].indexOf(' ') != -1  || prop_list[j].indexOf('/') != -1) prop_list[j] = "\"" + prop_list[j] + "\"";
              String[] value = lst[k].get_value(j);
              JiveUtils.printFormatedRes("CLASS/" + class_list[i] + "/" + att_list[k] + "->" + prop_list[j] + ": ", value, fw);
            }
          }

          fw.write("\n");

          // Mark class as saved
          JiveUtils.addSavedClass(class_list[i]);

        }

        // Device declaration and resource

        fw.write("#---------------------------------------------------------\n");
        fw.write("# SERVER " + srvName + ", " + class_list[i] + " device declaration\n");
        fw.write("#---------------------------------------------------------\n\n");

        String[] dev_list = dbs.get_device_name(class_list[i]);
        JiveUtils.printFormatedRes(srvName + "/DEVICE/" + class_list[i] + ": ", dev_list, fw);
        fw.write("\n");

        for (l = 0; l < dev_list.length; l++) {

          prop_list = db.get_device_property_list(dev_list[l], "*");
          if (prop_list.length > 0) {
            fw.write("\n# --- " + dev_list[l] + " properties\n\n");
            for (j = 0; j < prop_list.length; j++) {
              String[] value = db.get_device_property(dev_list[l], prop_list[j]).extractStringArray();
              if (prop_list[j].indexOf(' ') != -1  || prop_list[j].indexOf('/') != -1) prop_list[j] = "\"" + prop_list[j] + "\"";
              JiveUtils.printFormatedRes(dev_list[l] + "->" + prop_list[j] + ": ", value, fw);
            }
          }

          try {

            DeviceProxy ds = new DeviceProxy(dev_list[l]);
            att_list = ds.get_attribute_list();
            lst = db.get_device_attribute_property(dev_list[l], att_list);
            prtOut = false;
            for (k = 0; k < lst.length; k++) {
              prop_list = lst[k].get_property_list();
              for (j = 0; j < prop_list.length; j++) {
                if (!prtOut) {
                  fw.write("\n# --- " + dev_list[l] + " attribute properties\n\n");
                  prtOut = true;
                }
                if (prop_list[j].indexOf(' ') != -1  || prop_list[j].indexOf('/') != -1) prop_list[j] = "\"" + prop_list[j] + "\"";
                String[] value = lst[k].get_value(j);
                JiveUtils.printFormatedRes(dev_list[l] + "/" + att_list[k] + "->" + prop_list[j] + ": ", value, fw);
              }
            }

          } catch (DevFailed e) {

            JiveUtils.showJiveError("Attribute properties for " + dev_list[l] + " has not been saved !\n"
                                    + e.errors[0].desc);

          }

        }

        fw.write("\n");

      }

    } catch (DevFailed e) {

      JiveUtils.showTangoError(e);

    }

  }

  // *****************************************************************************************************************
  // Edit all the properties of the node
  public void editProperties(String info) {

    int count = getChildCount();
    int i;

    if (count > 0) {

      TreePath[] paths = new TreePath[count];
      Object rows[][] = new Object[count][2];

      for (i = 0; i < count; i++) {
        TangoTreeNode n = (TangoTreeNode) getChildAt(i);
        paths[i] = n.getCompletePath();
        String[] values = n.getValue();
        rows[i][0] = JiveUtils.formatPath(paths[i]);
        rows[i][1] = values[0];
      }

      DetailsDlg dlg = new DetailsDlg(JiveUtils.parent, rows, paths);
      dlg.showDlg();

    } else {

      JiveUtils.showJiveError("No properties defined for " + info);

    }

  }

  // *****************************************************************************************************************
  // Edit all the properties of the attribute node
  public void editAttProperties(String info) {

    int count = getChildCount();
    int i,j,k;

    TreePath[] paths = new TreePath[count+8];
    Object rows[][] = new Object[count+8][2];

    k = 0;
    for (i = 0; i < count; i++) {
      TangoTreeNode n = (TangoTreeNode) getChildAt(i);
      if (n.isLeaf()) {
        String[] values = n.getValue();
        paths[k] = n.getCompletePath();
        rows[k][0] = JiveUtils.formatPath(paths[k]);
        rows[k][1] = values[0];
        k++;
      } else {
        for(j=0;j<n.getChildCount();j++) {
          TangoTreeNode n2 = (TangoTreeNode) n.getChildAt(j);
          String[] values = n2.getValue();
          paths[k] = n2.getCompletePath();
          rows[k][0] = JiveUtils.formatPath(paths[k]);
          rows[k][1] = values[0];
          k++;
        }
      }
    }

    DetailsDlg dlg = new DetailsDlg(JiveUtils.parent, rows, paths);
    dlg.showDlg();

  }

  // *****************************************************************************************************************
  // Return the value of the node
  public String toString() {
    return (value);
  }

  // *****************************************************************************************************************
  // Return the complete path of the node
  public TreePath getCompletePath() {
    int i;

    // Construct the path
    TangoTreeNode node = this;
    TangoTreeNode[] nodes = new TangoTreeNode[node.level + 1];
    for (i = nodes.length - 1; i >= 0; i--) {
      nodes[i] = node;
      node = (TangoTreeNode) node.getParent();
    }
    return new TreePath(nodes);

  }

  // *****************************************************************************************************************
  // Return the complete path of the node as string
  public String toStringEx() {

    String ret = "";
    int i;

    // Construct full string
    TangoTreeNode node = this;
    TangoTreeNode[] nodes = new TangoTreeNode[node.level + 1];
    for (i = nodes.length - 1; i >= 0; i--) {
      nodes[i] = node;
      node = (TangoTreeNode) node.getParent();
    }
    for (i = 0; i < nodes.length; i++) {
      ret += nodes[i].toString();
      ret += "/";
    }
    return ret;

  }

}
