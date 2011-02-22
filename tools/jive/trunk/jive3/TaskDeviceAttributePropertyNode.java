package jive3;

import fr.esrf.TangoApi.Database;
import fr.esrf.TangoApi.DbAttribute;
import fr.esrf.Tango.DevFailed;

import javax.swing.*;

import jive.JiveUtils;

import java.util.Vector;

// ---------------------------------------------------------------

class TaskDeviceAttributePropertyNode extends PropertyNode {

  private Database db;
  private String   devName;
  private String   attributeName;
  private int      idl;

  TaskDeviceAttributePropertyNode(TreePanel parent,Database db,String devName,String attributeName,int idl) {
    this.db = db;
    this.devName = devName;
    this.attributeName = attributeName;
    this.parentPanel = parent;
    this.idl = idl;
  }

  void populateNode() throws DevFailed {
  }

  ImageIcon getIcon() {
    return TangoNodeRenderer.leaficon;
  }

  public String toString() {
    return attributeName;
  }

  String getTitle() {
    return "Device attribute properties";
  }

  String getName() {
    return devName;
  }
  
  String getAttributeName() {
    return attributeName;
  }

  public boolean isLeaf() {
    return true;
  }

  String[][] getProperties() {

    String[][] ret = new String[0][0];

    try {

      DbAttribute lst = db.get_device_attribute_property(devName, attributeName);

      String plist[] = lst.get_property_list();
      Vector pvec = new Vector();
      for(int i = 0; i < plist.length ; i++) {
        if(JiveUtils.IsAttCfgItem(plist[i],idl)<0) {
          pvec.add(plist[i]);
        }
      }

      ret = new String[pvec.size()][2];

      for (int i = 0; i < pvec.size(); i++) {
        ret[i][0] = (String) pvec.get(i);
        ret[i][1] = lst.get_string_value((String) pvec.get(i));
      }

    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

    return ret;

  }

  void setProperty(String propName, String value) {

    try {
      DbAttribute att = new DbAttribute(attributeName);
      att.add(propName, JiveUtils.makeStringArray(value));
      db.put_device_attribute_property(devName,att);
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void deleteProperty(String propName) {

    try {
      db.delete_device_attribute_property(devName,attributeName,propName);
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  public void execAction(int number) {
    switch(number) {

      case TreePanel.ACTION_COPY:
        JiveUtils.the_clipboard.clear();
        String[][] props = getProperties();
        for(int i=0;i<props.length;i++)
          JiveUtils.the_clipboard.add(props[i][0],attributeName,props[i][1]);
        break;

      case TreePanel.ACTION_PASTE:
        for(int i=0;i<JiveUtils.the_clipboard.getAttPropertyLength();i++)
          setProperty(JiveUtils.the_clipboard.getAttPropertyName(i),
                      JiveUtils.the_clipboard.getAttPropertyValue(i));
        parentPanel.refreshValues();
        break;

    }
  }

}