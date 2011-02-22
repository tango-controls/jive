package jive3;

import fr.esrf.TangoApi.Database;
import fr.esrf.Tango.DevFailed;

import javax.swing.*;

import jive.JiveUtils;

// ---------------------------------------------------------------

class TaskFreePropertyNode extends PropertyNode {

  private Database db;
  private String   objectName;

  TaskFreePropertyNode(TreePanel parent,Database db,String objectName) {
    this.db = db;
    this.objectName = objectName;
    this.parentPanel = parent;
  }

  void populateNode() throws DevFailed {
  }

  ImageIcon getIcon() {
    return TangoNodeRenderer.leaficon;
  }

  public String toString() {
    return objectName;
  }

  String getTitle() {
    return "Free properties";
  }

  String getName() {
    return objectName;
  }

  public boolean isLeaf() {
    return true;
  }

  String[][] getProperties() {

    String[][] ret = new String[0][0];

    try {

      String plist[] = db.get_object_property_list(objectName, "*");
      ret = new String[plist.length][2];

      for (int i = 0; i < plist.length; i++) {
        String[] res = db.get_property(objectName, plist[i]).extractStringArray();
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
      db.put_property(objectName, JiveUtils.makeDbDatum(propName, value));
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void deleteProperty(String propName) {

    try {
      db.delete_property(objectName, JiveUtils.makeDbDatum(propName, ""));
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

}