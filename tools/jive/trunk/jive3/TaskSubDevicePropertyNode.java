package jive3;

import fr.esrf.TangoApi.Database;
import fr.esrf.Tango.DevFailed;

import javax.swing.*;

import jive.JiveUtils;

import java.util.Vector;

// ---------------------------------------------------------------

class TaskSubDevicePropertyNode extends PropertyNode {

  private Database db;
  private String   devName;
  private String   subName;

  TaskSubDevicePropertyNode(TreePanel parent,Database db,String devName,String subName) {
    this.db = db;
    this.devName = devName;
    this.parentPanel = parent;
    this.subName = subName;
  }

  void populateNode() throws DevFailed {
  }

  ImageIcon getIcon() {
    return TangoNodeRenderer.leaficon;
  }

  public String toString() {
    return subName;
  }

  String getTitle() {
    return "Device properties";
  }

  String getName() {
    return devName+"/"+subName;
  }
  
  String getDevName() {
    return devName;  
  }
  
  String getSubName() {
    return subName;
  }

  public boolean isLeaf() {
    return true;
  }

  private boolean acceptProperty(String name) {

    return !(JiveUtils.IsPollCfgItem(name) ||
             JiveUtils.IsLogCfgItem(name) ||
             JiveUtils.IsHdbCfgItem(name) ||
             JiveUtils.IsSystemItem(name));

  }

  String[][] getProperties() {

    String[][] ret = new String[0][0];

    try {

      String plist[] = db.get_device_property_list(devName, subName + "/*");
      Vector pvec = new Vector();
      int cutIdx = subName.length() + 1;

      for (int i = 0; i < plist.length; i++) {
        // Remove config item
        if( acceptProperty(plist[i]) ) {
          pvec.add(plist[i].substring(cutIdx));
          String[] res = db.get_device_property(devName, plist[i]).extractStringArray();
          pvec.add(JiveUtils.stringArrayToString(res));
        }
      }

      ret = new String[pvec.size()/2][2];
      for(int i=0;i<ret.length;i++) {
        ret[i][0] = (String)pvec.get(2*i);
        ret[i][1] = (String)pvec.get(2*i+1);
      }

    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

    return ret;

  }

  void setProperty(String propName, String value) {

    try {
      db.put_device_property(devName, JiveUtils.makeDbDatum(subName+"/"+propName, value));
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void deleteProperty(String propName) {

    try {
      db.delete_device_property(devName, JiveUtils.makeDbDatum(subName+"/"+propName, ""));
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

}