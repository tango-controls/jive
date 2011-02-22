package jive3;

import fr.esrf.TangoApi.*;
import fr.esrf.Tango.DevFailed;

import javax.swing.*;
import java.util.Vector;

import jive.JiveUtils;


public class TaskEventNode extends TangoNode {

  class EventInfo {
    String name;
    String abs_change;
    String rel_change;
    String periodic;
    String archive_abs_change;
    String archive_rel_change;
    String archive_period;
  }

  private Database db;
  private String   devName;
  private Vector   attEventInfo;

  TaskEventNode(Database db, String devName) {
    this.db = db;
    this.devName = devName;
    attEventInfo = null;
  }

  void populateNode() throws DevFailed {
  }

  ImageIcon getIcon() {
    return TangoNodeRenderer.eventicon;
  }

  public String toString() {
    return "Event";
  }

  String getTitle() {
    return "Event";
  }

  String getName() {
    return devName;
  }

  public boolean isLeaf() {
    return true;
  }

  int getAttributeNumber() {
    if( attEventInfo==null ) browseEventInfo();
    return attEventInfo.size();
  }

  String getAttName(int idx) {
    if( attEventInfo==null ) browseEventInfo();
    return ((EventInfo)attEventInfo.get(idx)).name;
  }

  String getAbsChange(int idx) {
    if( attEventInfo==null ) browseEventInfo();
    return ((EventInfo)attEventInfo.get(idx)).abs_change;
  }

  String getRelChange(int idx) {
    if( attEventInfo==null ) browseEventInfo();
    return ((EventInfo)attEventInfo.get(idx)).rel_change;
  }

  String getPeriodic(int idx) {
    if( attEventInfo==null ) browseEventInfo();
    return ((EventInfo)attEventInfo.get(idx)).periodic;
  }

  String getArchAbsChange(int idx) {
    if( attEventInfo==null ) browseEventInfo();
    return ((EventInfo)attEventInfo.get(idx)).archive_abs_change;
  }

  String getArchRelChange(int idx) {
    if( attEventInfo==null ) browseEventInfo();
    return ((EventInfo)attEventInfo.get(idx)).archive_rel_change;
  }

  String getArchPeriod(int idx) {
    if( attEventInfo==null ) browseEventInfo();
    return ((EventInfo)attEventInfo.get(idx)).archive_period;
  }

  void setAbsChange(int idx,String value) {

    try {
      DeviceProxy ds = new DeviceProxy(devName);
      if (ds.get_idl_version() >= 3) {
        AttributeInfoEx ai = ds.get_attribute_info_ex(getAttName(idx));
        ai.events.ch_event.abs_change = value;
        ds.set_attribute_info(new AttributeInfoEx[]{ai});
      }
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void setRelChange(int idx,String value) {

    try {
      DeviceProxy ds = new DeviceProxy(devName);
      if (ds.get_idl_version() >= 3) {
        AttributeInfoEx ai = ds.get_attribute_info_ex(getAttName(idx));
        ai.events.ch_event.rel_change = value;
        ds.set_attribute_info(new AttributeInfoEx[]{ai});
      }
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void setArchAbsChange(int idx,String value) {

    try {
      DeviceProxy ds = new DeviceProxy(devName);
      if (ds.get_idl_version() >= 3) {
        AttributeInfoEx ai = ds.get_attribute_info_ex(getAttName(idx));
        ai.events.arch_event.abs_change = value;
        ds.set_attribute_info(new AttributeInfoEx[]{ai});
      }
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void setArchRelChange(int idx,String value) {

    try {
      DeviceProxy ds = new DeviceProxy(devName);
      if (ds.get_idl_version() >= 3) {
        AttributeInfoEx ai = ds.get_attribute_info_ex(getAttName(idx));
        ai.events.arch_event.rel_change = value;
        ds.set_attribute_info(new AttributeInfoEx[]{ai});
      }
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void setArchPeriod(int idx,String value) {

    try {
      DeviceProxy ds = new DeviceProxy(devName);
      if (ds.get_idl_version() >= 3) {
        AttributeInfoEx ai = ds.get_attribute_info_ex(getAttName(idx));
        ai.events.arch_event.period = value;
        ds.set_attribute_info(new AttributeInfoEx[]{ai});
      }
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void setPeriodic(int idx,String value) {

    try {
      DeviceProxy ds = new DeviceProxy(devName);
      if (ds.get_idl_version() >= 3) {
        AttributeInfoEx ai = ds.get_attribute_info_ex(getAttName(idx));
        ai.events.per_event.period = value;
        ds.set_attribute_info(new AttributeInfoEx[]{ai});
      }
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void resetChangeEvent(int idx) {

    try {

      // Clear database
      String[] pNames = {"rel_change","abs_change"};
      db.delete_device_attribute_property(devName,getAttName(idx),pNames);

    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void resetArchEvent(int idx) {

    try {

      // Clear database
      String[] pNames = {"archive_rel_change","archive_abs_change","archive_period"};
      db.delete_device_attribute_property(devName,getAttName(idx),pNames);

    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void resetPeriodicEvent(int idx) {

    try {

      // Clear database
      String[] pNames = {"event_period"};
      db.delete_device_attribute_property(devName,getAttName(idx),pNames);

    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  public void restartDevice() {

    try {

      DbDevImportInfo info = db.import_device(devName);
      DeviceProxy ds = new DeviceProxy("dserver/" + info.server);
      DeviceData in = new DeviceData();
      in.insert(devName);
      ds.command_inout("DevRestart", in);

    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void browseEventInfo() {

    try {

      DeviceProxy ds = new DeviceProxy(devName);
      attEventInfo = new Vector();
      if (ds.get_idl_version() >= 3) {
        String[] attList = ds.get_attribute_list();
        JiveUtils.sortList(attList);
        for (int i = 0; i < attList.length; i++) {
          AttributeInfoEx ai = ds.get_attribute_info_ex(attList[i]);
          if(ai.events != null) {
            EventInfo ei = new EventInfo();
            ei.name = attList[i];
            ei.abs_change = reformat(ai.events.ch_event.abs_change);
            ei.rel_change = reformat(ai.events.ch_event.rel_change);
            ei.periodic   = reformat(ai.events.per_event.period);
            ei.archive_abs_change = reformat(ai.events.arch_event.abs_change);
            ei.archive_rel_change = reformat(ai.events.arch_event.rel_change);
            ei.archive_period = reformat(ai.events.arch_event.period);
            attEventInfo.add(ei);
          }
        }
      }

    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  private String reformat(String s) {
    if(s.equalsIgnoreCase("Not specified"))
      return "None";
    else
      return s;
  }

}
