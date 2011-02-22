package jive3;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceProxy;

import javax.swing.*;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import jive.JiveUtils;


/**
 * A panel for selecting tango alias
 */
public class TreePanelAlias extends TreePanel {

  // Filtering stuff
  String  aliasFilterString="*";
  Pattern aliasPattern=null;

  public TreePanelAlias(MainPanel parent) {

    this.invoker = parent;
    this.self = this;
    setLayout(new BorderLayout());

  }

  public TangoNode createRoot() {
    return new RootNode();
  }

  public void applyFilter(String filter) {

    aliasFilterString = filter;

    if( filter.equals("*") ) {
      aliasPattern = null;
    } else if (filter.length()==0) {
      aliasPattern = null;
    } else {
      try {
        String f = filterToRegExp(aliasFilterString);
        aliasPattern = Pattern.compile(f);
      } catch (PatternSyntaxException e) {
        JOptionPane.showMessageDialog(invoker,e.getMessage());
      }
    }

  }

  public String getFilter() {
    return aliasFilterString;
  }


  // ---------------------------------------------------------------

  class RootNode extends TangoNode {

    void populateNode() throws DevFailed {
      String[] list = db.get_device_alias_list("*");
      for (int i = 0; i < list.length; i++) {
        if( aliasPattern!=null ) {
          Matcher matcher =  aliasPattern.matcher(list[i].toLowerCase());
          if( matcher.find() && matcher.start()==0 && matcher.end()==list[i].length() ) {
            add(new AliasNode(list[i]));
          }
        } else {
          add(new AliasNode(list[i]));
        }
      }
    }

    public String toString() {
      return "Alias: ";
    }

  }

  // ---------------------------------------------------------------

  class AttributeNode extends TangoNode {

    private String devName;

    AttributeNode(String devName) {
      this.devName = devName;
    }

    void populateNode() throws DevFailed {

      String[] list = null;
      int idl = 0; // 0 means that no property will be considered as attribute config.
                   // In other terms , that means that if the device doesn't run , all
                   // attribute properties will appear in the attribute property node.
      DeviceProxy ds = new DeviceProxy(devName);

      try {
        list = ds.get_attribute_list();
        idl = ds.get_idl_version();
      } catch( DevFailed e) {
        // If the device failed, try to get the list
        // via the database
        list = db.get_device_attribute_list(devName);
      }

      JiveUtils.sortList(list);
      for(int i=0;i<list.length;i++)
        add(new TaskDeviceAttributePropertyNode(self,db,devName,list[i],idl));
    }

    public String toString() {
      return "Attribute properties";
    }

    public ImageIcon getIcon() {
      return TangoNodeRenderer.atticon;
    }

  }

  // ---------------------------------------------------------------

  class AliasNode extends TangoNode {

    private String aliasName;

    AliasNode(String aliasName) {
      this.aliasName = aliasName;
    }

    void populateNode() throws DevFailed {
      // Retrieve the device name
      String devName = db.get_alias_device(aliasName);
      add(new TaskDevicePropertyNode(self,db,devName));
      add(new TaskPollingNode(db,devName));
      add(new TaskEventNode(db,devName));
      add(new TaskAttributeNode(db,devName));
      add(new AttributeNode(devName));
      add(new TaskLoggingNode(db,devName));
    }

    public String toString() {
      return aliasName;
    }

    public ImageIcon getIcon() {
      return TangoNodeRenderer.devicon;
    }

    public int[] getAction() {
      return new int[]{TreePanel.ACTION_TESTDEV,TreePanel.ACTION_GOTODEVNODE,TreePanel.ACTION_DELETE};
    }

    public void goToDeviceNode() {
      try {
        String devName = db.get_alias_device(aliasName);
        invoker.goToDeviceNode(devName);
      } catch(DevFailed e) {
        JiveUtils.showTangoError(e);
      }
    }

    public void execAction(int actionNumber) {
      switch(actionNumber) {
        case TreePanel.ACTION_DELETE:
          try {
            db.delete_device_alias(aliasName);
          } catch (DevFailed e) {
            JiveUtils.showTangoError(e);
          }
          refresh();
          break;
        case TreePanel.ACTION_TESTDEV:
          testDevice(aliasName);
          break;
        case TreePanel.ACTION_GOTODEVNODE:
          goToDeviceNode();
          break;
      }
    }

  }

}
