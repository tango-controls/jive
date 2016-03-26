package jive3;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.AttributeInfoEx;
import fr.esrf.TangoApi.Database;
import fr.esrf.TangoApi.DeviceProxy;
import jive.JiveUtils;

import javax.swing.*;
import java.util.Vector;

public class TaskSingleAttributeNode extends TangoNode {

  private Database db;
  private String   devName;
  private String   attributeName;
  private String   shortAttName;
  private int      idl;

  // Polling
  private String   pollingPeriod = "";
  private boolean  isPolled = false;

  // Attribute properties
  private String min_alarm = "None";
  private String max_alarm = "None";
  private String min_warning = "None";
  private String max_warning = "None";
  private String delta_t = "None";
  private String delta_val = "None";
  private String unit = "None";
  private String display_unit = "None";
  private String standard_unit = "None";
  private String min = "None";
  private String max = "None";
  private String format = "None";
  private String label = "None";
  private String descr = "None";

  private String abs_change = "None";
  private String rel_change = "None";
  private String periodic = "None";
  private String archive_abs_change = "None";
  private String archive_rel_change = "None";
  private String archive_period = "None";

  TaskSingleAttributeNode(TreePanel parent,Database db,String devName,String attributeName,int idl) {

    this.db = db;
    this.devName = devName;
    this.attributeName = attributeName;
    this.parentPanel = parent;
    this.idl = idl;
    this.shortAttName = attributeName.substring(attributeName.lastIndexOf('/')+1);

  }

  void populateNode() throws DevFailed {
  }

  ImageIcon getIcon() {
    return TangoNodeRenderer.atticon;
  }

  public String toString() {
    return "Config";
  }

  String getTitle() {
    return attributeName;
  }

  String getShortAttName() {
    return shortAttName;
  }

  public boolean isLeaf() {
    return true;
  }

  public boolean isPolled() {
    return isPolled;
  }

  public String getPollingPeriod() {
    return pollingPeriod;
  }

  public String getRelativeChange() {
    return rel_change;
  }

  public String getAbsoluteChange() {
    return abs_change;
  }

  public String getArchRelativeChange() {
    return archive_rel_change;
  }

  public String getArchAbsoluteChange() {
    return archive_rel_change;
  }

  public String getArchPeriod() {
    return archive_period;
  }

  public String getPeriodic() {
    return periodic;
  }

  public String getLabel() {
    return label;
  }

  public String getFormat() {
    return format;
  }

  public String getUnit() {
    return unit;
  }

  public String getDisplayUnit() {
    return display_unit;
  }

  public String getStandardUnit() {
    return standard_unit;
  }

  public String getMin() {
    return min;
  }

  public String getMax() {
    return max;
  }

  public String getMinAlarm() {
    return min_alarm;
  }

  public String getMaxAlarm() {
    return max_alarm;
  }

  public String getMinWarning() {
    return min_warning;
  }

  public String getMaxWarning() {
    return max_warning;
  }

  public String getDeltaT() {
    return delta_t;
  }

  public String getDeltaVal() {
    return delta_val;
  }

  public String getDescr() {
    return descr;
  }

  void setAttributePolling(boolean isPolled,String period) {

    int polling_period = 3000;

    if(isPolled) {

      if(period!="") {
        try {
          polling_period = Integer.parseInt(period);
        } catch (NumberFormatException e) {
          JiveUtils.showJiveError("Invalid period value for attribute "+shortAttName+"\n" + e.getMessage());
        }
      }

      try {
        DeviceProxy ds = new DeviceProxy(devName);
        ds.poll_attribute(shortAttName,polling_period);
      } catch(DevFailed ex) {
        JiveUtils.showTangoError(ex);
      }

    } else {

      try {
        DeviceProxy ds = new DeviceProxy(devName);
        ds.stop_poll_attribute(shortAttName);
      } catch(DevFailed ex) {
        JiveUtils.showTangoError(ex);
      }

    }

  }

  void setAbsoluteChange(String value) {

    try {
      DeviceProxy ds = new DeviceProxy(devName);
      if (ds.get_idl_version() >= 3) {
        AttributeInfoEx ai = ds.get_attribute_info_ex(shortAttName);
        ai.events.ch_event.abs_change = value;
        ds.set_attribute_info(new AttributeInfoEx[]{ai});
      }
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void setRelativeChange(String value) {

    try {
      DeviceProxy ds = new DeviceProxy(devName);
      if (ds.get_idl_version() >= 3) {
        AttributeInfoEx ai = ds.get_attribute_info_ex(shortAttName);
        ai.events.ch_event.rel_change = value;
        ds.set_attribute_info(new AttributeInfoEx[]{ai});
      }
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void setArchAbsoluteChange(String value) {

    try {
      DeviceProxy ds = new DeviceProxy(devName);
      if (ds.get_idl_version() >= 3) {
        AttributeInfoEx ai = ds.get_attribute_info_ex(shortAttName);
        ai.events.arch_event.abs_change = value;
        ds.set_attribute_info(new AttributeInfoEx[]{ai});
      }
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void setArchRelativeChange(String value) {

    try {
      DeviceProxy ds = new DeviceProxy(devName);
      if (ds.get_idl_version() >= 3) {
        AttributeInfoEx ai = ds.get_attribute_info_ex(shortAttName);
        ai.events.arch_event.rel_change = value;
        ds.set_attribute_info(new AttributeInfoEx[]{ai});
      }
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void setArchPeriod(String value) {

    try {
      DeviceProxy ds = new DeviceProxy(devName);
      if (ds.get_idl_version() >= 3) {
        AttributeInfoEx ai = ds.get_attribute_info_ex(shortAttName);
        ai.events.arch_event.period = value;
        ds.set_attribute_info(new AttributeInfoEx[]{ai});
      }
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void setPeriod(String value) {

    try {
      DeviceProxy ds = new DeviceProxy(devName);
      if (ds.get_idl_version() >= 3) {
        AttributeInfoEx ai = ds.get_attribute_info_ex(shortAttName);
        ai.events.per_event.period = value;
        ds.set_attribute_info(new AttributeInfoEx[]{ai});
      }
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void setLabel(String value) {

    try {
      DeviceProxy ds = new DeviceProxy(devName);
      AttributeInfoEx ai = ds.get_attribute_info_ex(shortAttName);
      ai.label = value;
      ds.set_attribute_info(new AttributeInfoEx[]{ai});
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void setFormat(String value) {

    try {
      DeviceProxy ds = new DeviceProxy(devName);
      AttributeInfoEx ai = ds.get_attribute_info_ex(shortAttName);
      ai.format = value;
      ds.set_attribute_info(new AttributeInfoEx[]{ai});
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void setUnit(String value) {

    try {
      DeviceProxy ds = new DeviceProxy(devName);
      AttributeInfoEx ai = ds.get_attribute_info_ex(shortAttName);
      ai.unit = value;
      ds.set_attribute_info(new AttributeInfoEx[]{ai});
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void setDisplayUnit(String value) {

    try {
      DeviceProxy ds = new DeviceProxy(devName);
      AttributeInfoEx ai = ds.get_attribute_info_ex(shortAttName);
      ai.display_unit = value;
      ds.set_attribute_info(new AttributeInfoEx[]{ai});
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void setStandardUnit(String value) {

    try {
      DeviceProxy ds = new DeviceProxy(devName);
      AttributeInfoEx ai = ds.get_attribute_info_ex(shortAttName);
      ai.standard_unit = value;
      ds.set_attribute_info(new AttributeInfoEx[]{ai});
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void setMin(String value) {

    try {
      DeviceProxy ds = new DeviceProxy(devName);
      AttributeInfoEx ai = ds.get_attribute_info_ex(shortAttName);
      ai.min_value = value;
      ds.set_attribute_info(new AttributeInfoEx[]{ai});
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void setMax(String value) {

    try {
      DeviceProxy ds = new DeviceProxy(devName);
      AttributeInfoEx ai = ds.get_attribute_info_ex(shortAttName);
      ai.max_value = value;
      ds.set_attribute_info(new AttributeInfoEx[]{ai});
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void setMinAlarm(String value) {

    try {
      DeviceProxy ds = new DeviceProxy(devName);
      AttributeInfoEx ai = ds.get_attribute_info_ex(shortAttName);
      if(ai.alarms != null) {
        ai.alarms.min_alarm = value;
      } else {
        ai.min_alarm = value;
      }
      ds.set_attribute_info(new AttributeInfoEx[]{ai});
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void setMaxAlarm(String value) {

    try {
      DeviceProxy ds = new DeviceProxy(devName);
      AttributeInfoEx ai = ds.get_attribute_info_ex(shortAttName);
      if(ai.alarms != null) {
        ai.alarms.max_alarm = value;
      } else {
        ai.max_alarm = value;
      }
      ds.set_attribute_info(new AttributeInfoEx[]{ai});
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void setMinWarning(String value) {

    try {
      DeviceProxy ds = new DeviceProxy(devName);
      AttributeInfoEx ai = ds.get_attribute_info_ex(shortAttName);
      if(ai.alarms != null) {
        ai.alarms.min_warning = value;
      }
      ds.set_attribute_info(new AttributeInfoEx[]{ai});
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void setMaxWarning(String value) {

    try {
      DeviceProxy ds = new DeviceProxy(devName);
      AttributeInfoEx ai = ds.get_attribute_info_ex(shortAttName);
      if(ai.alarms != null) {
        ai.alarms.max_warning = value;
      }
      ds.set_attribute_info(new AttributeInfoEx[]{ai});
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void setDeltaT(String value) {

    try {
      DeviceProxy ds = new DeviceProxy(devName);
      AttributeInfoEx ai = ds.get_attribute_info_ex(shortAttName);
      if(ai.alarms != null) {
        ai.alarms.delta_t = value;
      }
      ds.set_attribute_info(new AttributeInfoEx[]{ai});
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void setDeltaVal(String value) {

    try {
      DeviceProxy ds = new DeviceProxy(devName);
      AttributeInfoEx ai = ds.get_attribute_info_ex(shortAttName);
      if(ai.alarms != null) {
        ai.alarms.delta_val = value;
      }
      ds.set_attribute_info(new AttributeInfoEx[]{ai});
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void setDescr(String value) {

    try {
      DeviceProxy ds = new DeviceProxy(devName);
      AttributeInfoEx ai = ds.get_attribute_info_ex(shortAttName);
      ai.description = value;
      ds.set_attribute_info(new AttributeInfoEx[]{ai});
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  public void updatePollingInfo() {

    String[] ps = null;

    try {
      DeviceProxy ds = new DeviceProxy(devName);
      ps = ds.polling_status();
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

    int i;
    String[] pinfo = null;
    boolean found = false;
    for (i = 0; i < ps.length && !found;) {
      pinfo = JiveUtils.extractPollingInfo(ps[i]);
      found = pinfo[0].equalsIgnoreCase("attribute") && pinfo[1].equalsIgnoreCase(shortAttName);
      if (!found) i++;
    }

    if(found) {
      pollingPeriod = pinfo[2];
      isPolled = true;
    } else {
      pollingPeriod = "";
      isPolled = false;
    }

  }

  private String reformat(String s) {
    if(s.equalsIgnoreCase("Not specified")    ||
       s.equalsIgnoreCase("No standard unit") ||
       s.equalsIgnoreCase("No unit")          ||
       s.equalsIgnoreCase("No description")   ||
       s.equalsIgnoreCase("No display unit"))
      return "None";
    else
      return s;
  }

  void browseAttributeInfo() {

    try {

      DeviceProxy ds = new DeviceProxy(devName);
      AttributeInfoEx ai = ds.get_attribute_info_ex(shortAttName);
      if( ai!=null ) {

        if (ai.alarms != null) {
          min_alarm = reformat(ai.alarms.min_alarm);
          max_alarm = reformat(ai.alarms.max_alarm);
          min_warning = reformat(ai.alarms.min_warning);
          max_warning = reformat(ai.alarms.max_warning);
          delta_t = reformat(ai.alarms.delta_t);
          delta_val = reformat(ai.alarms.delta_val);
        } else {
          min_alarm = reformat(ai.min_alarm);
          max_alarm = reformat(ai.max_alarm);
          min_warning = "None";
          max_warning = "None";
          delta_t = "None";
          delta_val = "None";
        }

        unit = reformat(ai.unit);
        display_unit = reformat(ai.display_unit);
        standard_unit = reformat(ai.standard_unit);
        min = reformat(ai.min_value);
        max = reformat(ai.max_value);
        format = reformat(ai.format);
        label = reformat(ai.label);
        descr = reformat(ai.description);

        if( ai.events!=null ) {

          rel_change = ai.events.ch_event.rel_change;
          abs_change = ai.events.ch_event.abs_change;
          archive_rel_change = ai.events.arch_event.rel_change;
          archive_abs_change = ai.events.arch_event.abs_change;
          archive_period = ai.events.arch_event.period;
          periodic = ai.events.per_event.period;

        }

      }

    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

}
