package jive3;

import fr.esrf.TangoApi.Database;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoApi.CommandInfo;
import fr.esrf.TangoApi.DbDatum;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoDs.TangoConst;

import javax.swing.*;
import java.util.Vector;

import jive.JiveUtils;

// ---------------------------------------------------------------

public class TaskPollingNode extends TangoNode {

  class PInfo {
    String  name;
    String  type;
    String  period;
  }

  private Database db;
  private String   devName;
  private Vector   attPollingInfo;
  private Vector   cmdPollingInfo;

  TaskPollingNode(Database db,String devName) {
    this.db = db;
    this.devName = devName;
    attPollingInfo = null;
    cmdPollingInfo = null;
  }

  void populateNode() throws DevFailed {
  }

  ImageIcon getIcon() {
    return TangoNodeRenderer.leafcfgicon;
  }

  public String toString() {
    return "Polling";
  }

  String getTitle() {
    return "Device polling";
  }

  String getName() {
    return devName;
  }

  public boolean isLeaf() {
    return true;
  }

  // --------------------------------------------------------------------

  int getCommandNumber() {
    if( attPollingInfo==null || cmdPollingInfo==null ) browsePollingStatus();
    return cmdPollingInfo.size();
  }

  String getCommandName(int idx) {
    if( attPollingInfo==null || cmdPollingInfo==null ) browsePollingStatus();
    return ((PInfo)cmdPollingInfo.get(idx)).name;
  }

  boolean isCommandPolled(int idx) {
    if( attPollingInfo==null || cmdPollingInfo==null ) browsePollingStatus();
    return ((PInfo)cmdPollingInfo.get(idx)).period != "";
  }

  String getCommandPollingPeriod(int idx) {
    if( attPollingInfo==null || cmdPollingInfo==null ) browsePollingStatus();
    return ((PInfo)cmdPollingInfo.get(idx)).period;
  }

  void updateCommandPolling(String cmdName,boolean isPolled,String period) {

    int polling_period = 3000;

    if(isPolled) {

      if(period!="") {
        try {
          polling_period = Integer.parseInt(period);
        } catch (NumberFormatException e) {
          JiveUtils.showJiveError("Invalid period value for command "+cmdName+"\n" + e.getMessage());
        }
      }

      try {
        DeviceProxy ds = new DeviceProxy(devName);
        ds.poll_command(cmdName,polling_period);
      } catch(DevFailed ex) {
        JiveUtils.showTangoError(ex);
      }

    } else {

      try {
        DeviceProxy ds = new DeviceProxy(devName);
        ds.stop_poll_command(cmdName);
      } catch(DevFailed ex) {
        JiveUtils.showTangoError(ex);
      }

    }

  }

  int getAttributeNumber() {
    if( attPollingInfo==null || cmdPollingInfo==null ) browsePollingStatus();
    return attPollingInfo.size();
  }

  String getAttributeName(int idx) {
    if( attPollingInfo==null || cmdPollingInfo==null ) browsePollingStatus();
    return ((PInfo)attPollingInfo.get(idx)).name;
  }

  boolean isAttributePolled(int idx) {
    if( attPollingInfo==null || cmdPollingInfo==null ) browsePollingStatus();
    return ((PInfo)attPollingInfo.get(idx)).period != "";
  }

  String getAttributePollingPeriod(int idx) {
    if( attPollingInfo==null || cmdPollingInfo==null ) browsePollingStatus();
    return ((PInfo)attPollingInfo.get(idx)).period;
  }

  void updateAttributePolling(String attName,boolean isPolled,String period) {

    int polling_period = 3000;

    if(isPolled) {

      if(period!="") {
        try {
          polling_period = Integer.parseInt(period);
        } catch (NumberFormatException e) {
          JiveUtils.showJiveError("Invalid period value for attribute "+attName+"\n" + e.getMessage());
        }
      }

      try {
        DeviceProxy ds = new DeviceProxy(devName);
        ds.poll_attribute(attName,polling_period);
      } catch(DevFailed ex) {
        JiveUtils.showTangoError(ex);
      }

    } else {

      try {
        DeviceProxy ds = new DeviceProxy(devName);
        ds.stop_poll_attribute(attName);
      } catch(DevFailed ex) {
        JiveUtils.showTangoError(ex);
      }

    }

  }

  String getPollOldFactor() {

    String ret = "4";
    try {
      DbDatum data = db.get_device_property(devName,"poll_old_factor");
      if(!data.is_empty()) ret = data.extractString();
    } catch(DevFailed e) {
      JiveUtils.showTangoError(e);
    }
    return ret;

  }

  void setPollOldFactor(String value) {

    try {
      db.put_device_property(devName, JiveUtils.makeDbDatum("poll_old_factor", value));
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  String getPollRingDepth() {

    String ret = "10";
    try {
      DbDatum data = db.get_device_property(devName,"poll_ring_depth");
      if(!data.is_empty()) ret = data.extractString();
    } catch(DevFailed e) {
      JiveUtils.showTangoError(e);
    }
    return ret;

  }

  void setPollRingDepth(String value) {

    try {
      db.put_device_property(devName, JiveUtils.makeDbDatum("poll_ring_depth", value));
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }
    
  }

  void resetConfig() {

    try {
      db.delete_device_property(devName,"polled_attr");
      db.delete_device_property(devName,"polled_cmd");
      db.delete_device_property(devName,"non_auto_polled_cmd");
      db.delete_device_property(devName,"non_auto_polled_attr");
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  // --------------------------------------------------------------------

  private String[] getCommandList(DeviceProxy ds) throws DevFailed {

    int i;
    Vector cmds = new Vector();

    // Filter commands which can be polled
    CommandInfo[] ci = ds.command_list_query();
    for (i = 0; i < ci.length; i++) {
      if(ci[i].in_type==TangoConst.Tango_DEV_VOID)
        cmds.add(ci[i].cmd_name);
    }

    String[] ret = new String[cmds.size()];
    for(i=0;i<cmds.size();i++)
      ret[i] = (String)cmds.get(i);

    return ret;

  }

  private void update(String[] ps,PInfo pi) {

    int i;
    boolean found = false;
    String[] pinfo = null;
    for (i = 0; i < ps.length && !found;) {
      pinfo = JiveUtils.extractPollingInfo(ps[i]);
      found = pinfo[0].equalsIgnoreCase(pi.type) && pinfo[1].equalsIgnoreCase(pi.name);
      if (!found) i++;
    }

    if(found) {
      pi.period = pinfo[2];
    } else {
      pi.period = "";
    }

  }

  void browsePollingStatus() {

    int i;
    cmdPollingInfo = new Vector();
    attPollingInfo = new Vector();

    try {

      DeviceProxy ds = new DeviceProxy(devName);
      String[] ps = ds.polling_status();

      // Fill command structure
      String[] cmdList = getCommandList(ds);
      for(i=0;i<cmdList.length;i++) {
        PInfo pi = new PInfo();
        pi.name = cmdList[i];
        pi.type = "command";
        update(ps,pi);
        cmdPollingInfo.add(pi);
      }

      // Fill attribute structure
      String[] attList = ds.get_attribute_list();
      JiveUtils.sortList(attList);
      for(i=0;i<attList.length;i++) {
        PInfo pi = new PInfo();
        pi.name = attList[i];
        pi.type = "attribute";
        update(ps,pi);
        attPollingInfo.add(pi);
      }

    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

}
