package jive3;

import fr.esrf.TangoApi.Database;
import fr.esrf.Tango.DevFailed;

import javax.swing.*;

import jive.JiveUtils;

// ---------------------------------------------------------------

class TaskClassPropertyNode extends PropertyNode {

  private Database db;
  private String   className;

  TaskClassPropertyNode(TreePanel parent,Database db,String className) {
    this.db = db;
    this.className = className;
    this.parentPanel = parent;
  }

  void populateNode() throws DevFailed {
  }

  ImageIcon getIcon() {
    return TangoNodeRenderer.leaficon;
  }

  public String toString() {
    return "Properties";
  }

  String getTitle() {
    return "Class properties";
  }

  String getName() {
    return className;
  }

  public boolean isLeaf() {
    return true;
  }

  String[][] getProperties() {

    String[][] ret = new String[0][0];

    try {

      String plist[] = db.get_class_property_list(className, "*");
      ret = new String[plist.length][2];

      for (int i = 0; i < plist.length; i++) {
        String[] res = db.get_class_property(className, plist[i]).extractStringArray();
        ret[i][0] = plist[i];
        ret[i][1] = JiveUtils.stringArrayToString(res);
      }

    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

    return ret;

  }

  void setProperty(String propName, String value) {

    try {
      db.put_class_property(className, JiveUtils.makeDbDatum(propName, value));
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void deleteProperty(String propName) {

    try {
      db.delete_class_property(className, JiveUtils.makeDbDatum(propName, ""));
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

}