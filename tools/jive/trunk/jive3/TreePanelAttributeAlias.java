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
public class TreePanelAttributeAlias extends TreePanel {

  // Filtering stuff
  String  aliasFilterString="*";
  Pattern aliasPattern=null;

  public TreePanelAttributeAlias(MainPanel parent) {

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
      String[] list = db.get_attribute_alias_list("*");
      JiveUtils.sortList(list);
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
      return "AttAlias:";
    }

  }

  // ---------------------------------------------------------------

  class AliasNode extends TangoNode {

    private String aliasName=null;
    private String attName=null;

    AliasNode(String aliasName) {
      this.aliasName = aliasName;
    }

    void populateNode() throws DevFailed {

      if(attName==null) attName = db.get_attribute_alias(aliasName);
      int aslash = attName.lastIndexOf("/");
      String devName = attName.substring(0,aslash);
      String aName = attName.substring(aslash+1);
      DeviceProxy ds = new DeviceProxy(devName);
      int idl = ds.get_idl_version();

      add(new TaskDeviceAttributePropertyNode(self,db,devName,aName,idl));
      add(new TaskSingleAttributeNode(self,db,devName,attName,idl));

    }

    public String toString() {
      return aliasName;
    }

    public ImageIcon getIcon() {
      return TangoNodeRenderer.atticon;
    }

    public int[] getAction() {
      return new int[]{TreePanel.ACTION_GOTODEVNODE,TreePanel.ACTION_DELETE};
    }

    public void goToDeviceNode() {

      try {
        if(attName==null) attName = db.get_attribute_alias(aliasName);
        int aslash = attName.lastIndexOf("/");
        String devName = attName.substring(0,aslash);
        invoker.goToDeviceNode(devName);
      } catch(DevFailed e) {
        JiveUtils.showTangoError(e);
      }

    }

    public void execAction(int actionNumber) {
      switch(actionNumber) {
        case TreePanel.ACTION_DELETE:
          int ok = JOptionPane.showConfirmDialog(invoker, "Delete attribute alias " + aliasName + " ?", "Confirm delete", JOptionPane.YES_NO_OPTION);
          if (ok == JOptionPane.YES_OPTION) {
            try {
              db.delete_attribute_alias(aliasName);
            } catch (DevFailed e) {
              JiveUtils.showTangoError(e);
            }
            refresh();
          }
          break;
        case TreePanel.ACTION_GOTODEVNODE:
          goToDeviceNode();
          break;
      }
    }

    public String getValue() {

      if( attName==null ) {
        try {
          attName = db.get_attribute_alias(aliasName);
        } catch(DevFailed e) {
          JiveUtils.showTangoError(e);
        }
      }

      return aliasName + " = " + attName;
    }

    public String getTitle() {
      return "Attribute alias";
    }

  }

}
