package jive3;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.Database;
import fr.esrf.TangoApi.DeviceProxy;
import jive.JiveUtils;

import javax.swing.*;
import java.io.IOException;

/**
 * Attribute Property node
 */
public class AttributeNode extends TangoNode {

  private String devName;
  private TreePanel self;
  private Database db;

  AttributeNode(TreePanel self,String devName,Database db) {
    this.devName = devName;
    this.self = self;
    this.db = db;
  }

  void populateNode() throws DevFailed {

    String[] list = new String[0];
    String[] devList = new String[0];
    String[] dbList = new String[0];
    int idl = 0; // 0 means that no property will be considered as attribute config.
    // In other terms , that means that if the device doesn't run , all
    // attribute properties will appear in the attribute property node.
    DeviceProxy ds = new DeviceProxy(devName);

    try {
      devList = ds.get_attribute_list();
      idl = ds.get_idl_version();
    } catch( DevFailed e) {
    }
    dbList = db.get_device_attribute_list(devName);

    JiveUtils.sortList(list);
    for(int i=0;i<devList.length;i++)
      add(new TaskDeviceAttributePropertyNode(self,db,devName,devList[i],idl,false));
    for(int i=0;i<dbList.length;i++)
      if(!JiveUtils.contains(devList,dbList[i]))
        add(new TaskDeviceAttributePropertyNode(self,db,devName,dbList[i],idl,true));

  }

  public int[] getAction() {
    return new int[]{
        TreePanel.ACTION_COPY,
        TreePanel.ACTION_COPY_ATT_SET,
        TreePanel.ACTION_PASTE,
        TreePanel.ACTION_CREATE_ATTPROP,
        TreePanel.ACTION_SAVE_PROP
    };
  }

  public void execAction(int actionNumber) {
    switch(actionNumber) {
      case TreePanel.ACTION_CREATE_ATTPROP:
        self.createEmptyAttributeProperty(devName);
        break;

      case TreePanel.ACTION_COPY:
        // Copy all attribute property to the clipboard
        int nbAtt = getChildCount();
        JiveUtils.the_clipboard.clear();
        for(int i=0;i<nbAtt;i++) {
          TaskDeviceAttributePropertyNode node = (TaskDeviceAttributePropertyNode)getChildAt(i);
          String[][] props = node.getProperties();
          for(int j=0;j<props.length;j++)
            if(!props[j][0].equalsIgnoreCase("__value"))
              JiveUtils.the_clipboard.add(props[j][0],node.getAttributeName(),props[j][1]);
        }
        break;

      case TreePanel.ACTION_COPY_ATT_SET:
        // Copy all attribute setpoint to the clipboard
        int nbAtt2 = getChildCount();
        JiveUtils.the_clipboard.clear();
        for(int i=0;i<nbAtt2;i++) {
          TaskDeviceAttributePropertyNode node = (TaskDeviceAttributePropertyNode)getChildAt(i);
          String[][] props = node.getProperties();
          for(int j=0;j<props.length;j++)
            if(props[j][0].equalsIgnoreCase("__value"))
              JiveUtils.the_clipboard.add(props[j][0],node.getAttributeName(),props[j][1]);
        }
        break;

      case TreePanel.ACTION_PASTE:
        JiveUtils.the_clipboard.parse();
        for(int i=0;i<JiveUtils.the_clipboard.getAttPropertyLength();i++) {
          self.putAttributeProperty( devName,
              JiveUtils.the_clipboard.getAttName(i),
              JiveUtils.the_clipboard.getAttPropertyName(i),
              JiveUtils.the_clipboard.getAttPropertyValue(i));
        }
        break;

      case TreePanel.ACTION_SAVE_PROP:
        try {
          DbFileWriter.SaveDeviceAttributesProperties(devName);
        } catch (DevFailed e) {
          JiveUtils.showTangoError(e);
        } catch (IOException e2) {
          JiveUtils.showJiveError(e2.getMessage());
        }
        break;
    }
  }

  public String toString() {
    return "Attribute properties";
  }

  public ImageIcon getIcon() {
    return TangoNodeRenderer.atticon;
  }

}
