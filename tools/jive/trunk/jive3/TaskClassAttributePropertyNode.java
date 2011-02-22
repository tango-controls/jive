package jive3;

import fr.esrf.TangoApi.Database;
import fr.esrf.TangoApi.DbAttribute;
import fr.esrf.Tango.DevFailed;

import javax.swing.*;

import jive.JiveUtils;

// ---------------------------------------------------------------

class TaskClassAttributePropertyNode extends PropertyNode {

  private Database db;
  private String   className;
  private String   attributeName;

  TaskClassAttributePropertyNode(TreePanel parent,Database db,String className,String attributeName) {
    this.db = db;
    this.className = className;
    this.attributeName = attributeName;
    this.parentPanel = parent;
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
    return "Class attribute properties";
  }

  String getName() {
    return className;
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

      DbAttribute lst = db.get_class_attribute_property(className, attributeName);

      String plist[] = lst.get_property_list();
      ret = new String[plist.length][2];

      for (int i = 0; i < plist.length; i++) {
        ret[i][0] = plist[i];
        ret[i][1] = lst.get_string_value(plist[i]);
      }

    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

    return ret;

  }

  void setProperty(String propName, String value) {

    try {
      DbAttribute att = new DbAttribute(attributeName);
      att.add(propName,JiveUtils.makeStringArray(value));
      db.put_class_attribute_property(className,att);
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void deleteProperty(String propName) {

    try {
      db.delete_class_attribute_property(className,attributeName,propName);
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