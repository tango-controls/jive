package jive3;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DbAttribute;
import fr.esrf.TangoApi.DeviceProxy;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
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

  public TreePanelAlias() {

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
      return "Alias:";
    }

    void execAction(int number,boolean multipleCall) {
    }

  }

  // ---------------------------------------------------------------

  class AliasNode extends TangoNode {

    private String aliasName = null;
    private String devName = null;

    AliasNode(String aliasName) {
      this.aliasName = aliasName;
    }

    void populateNode() throws DevFailed {
      // Retrieve the device name
      devName = db.get_device_from_alias(aliasName);
      add(new TaskDevicePropertyNode(self,db,devName));
      add(new TaskPollingNode(db,devName));
      add(new TaskEventNode(db,devName));
      add(new TaskAttributeNode(db,devName));
      add(new TaskPipeNode(db,devName));
      add(new AttributeNode(self,devName,db));
      add(new TaskLoggingNode(db,devName));
    }

    public String toString() {
      return aliasName;
    }

    public ImageIcon getIcon() {
      return TangoNodeRenderer.devicon;
    }

    public Action[] getAction() {
      if( JiveUtils.readOnly ) {
        return new Action[]{
            TreePanel.getAction(ACTION_TESTDEV),
            TreePanel.getAction(ACTION_GOTODEVNODE)
        };
      } else {
        return new Action[]{
            TreePanel.getAction(ACTION_TESTDEV),
            TreePanel.getAction(ACTION_GOTODEVNODE),
            TreePanel.getAction(ACTION_DELETE)
        };
      }
    }

    public void goToDeviceNode() {
      try {
        if(devName==null) devName = db.get_device_from_alias(aliasName);
        invoker.goToDeviceNode(devName);
      } catch(DevFailed e) {
        JiveUtils.showTangoError(e);
      }
    }

    public void execAction(int actionNumber,boolean multipleCall) throws IOException {

      switch(actionNumber) {

        case TreePanel.ACTION_DELETE:
          if(multipleCall) {
            try {
              db.delete_device_alias(aliasName);
            } catch (DevFailed e) {
              throw new IOException(aliasName + ":" + e.errors[0].desc);
            }
          } else {
            int ok = JOptionPane.showConfirmDialog(invoker, "Delete alias " + aliasName + " ?", "Confirm delete", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
              try {
                db.delete_device_alias(aliasName);
              } catch (DevFailed e) {
                JiveUtils.showTangoError(e);
              }
              refresh();
            }
          }
          break;

        case TreePanel.ACTION_TESTDEV:
          testDevice(aliasName);
          break;

        case TreePanel.ACTION_GOTODEVNODE:
          goToDeviceNode();
          break;

      }
    }

    String getValue() {

      if( devName==null ) {
        try {
          devName = db.get_device_from_alias(aliasName);
        } catch(DevFailed e) {
          JiveUtils.showTangoError(e);
        }
      }

      return getDeviceInfo(devName);
    }

    String getTitle() {
      return "Device Info";
    }

  }


}
