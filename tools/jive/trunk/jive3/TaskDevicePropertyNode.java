package jive3;

import fr.esrf.TangoApi.Database;
import fr.esrf.Tango.DevFailed;

import javax.swing.*;

import jive.JiveUtils;

import java.util.Vector;

// ---------------------------------------------------------------

class TaskDevicePropertyNode extends PropertyNode {

  private Database db;
  private String   devName;

  TaskDevicePropertyNode(TreePanel parent,Database db,String devName) {
    this.db = db;
    this.devName = devName;
    this.parentPanel = parent;
  }

  void populateNode() throws DevFailed {

    Vector nameList = new Vector();

    // Build subNames (Property which contains a / in the name)
    String plist[] = db.get_device_property_list(devName, "*");
    for(int i=0;i<plist.length;i++) {
      int slash = plist[i].indexOf('/');
      if(slash>=0) {
        String subName = plist[i].substring(0,slash);
        // add subName the the name list
        addToList(nameList,subName);
      }
    }

    for(int i=0;i<nameList.size();i++)
      add(new TaskSubDevicePropertyNode(parentPanel,db,devName,(String)nameList.get(i)));

  }

  ImageIcon getIcon() {
    return TangoNodeRenderer.leaficon;
  }

  public String toString() {
    return "Properties";
  }

  String getTitle() {
    return "Device properties";
  }

  String getName() {
    return devName;
  }

  public boolean isLeaf() {
    return false;
  }

  private void addToList(Vector v,String s) {

    int found = -1;
    int i = 0;
    while(i<v.size() && found<0) {
      found = ((String)v.get(i)).compareToIgnoreCase(s);
      if(found<0) i++;
    }
    if(found<0) {
      // Add at the end
      v.add(s);
    } else if(found>0) {
      // Insert at i
      v.add(i,s);
    }

  }

  private boolean acceptProperty(String name) {

    int slash = name.indexOf('/');
    return !(JiveUtils.IsPollCfgItem(name) ||
             JiveUtils.IsLogCfgItem(name) ||
             JiveUtils.IsHdbCfgItem(name) ||
             JiveUtils.IsSystemItem(name)) &&
           slash<0;

  }

  String[][] getProperties() {

    String[][] ret = new String[0][0];

    try {

      String plist[] = db.get_device_property_list(devName, "*");
      Vector pvec = new Vector();

      for (int i = 0; i < plist.length; i++) {
        // Remove config item
        if( acceptProperty(plist[i]) ) {
          pvec.add(plist[i]);
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

    // Update property
    try {
      db.put_device_property(devName, JiveUtils.makeDbDatum(propName, value));
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

    // Check if the property name contains a slash
    int slash = propName.indexOf('/');
    if(slash>=0) {
      // We need to refresh the tree under this node
      String subName = propName.substring(0,slash);
      parentPanel.refreshNode(this,subName);
    }

  }

  void deleteProperty(String propName) {

    try {
      db.delete_device_property(devName, JiveUtils.makeDbDatum(propName, ""));
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

}