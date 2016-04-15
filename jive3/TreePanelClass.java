package jive3;

import fr.esrf.Tango.DevFailed;

import javax.swing.*;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import fr.esrf.Tango.DevVarLongStringArray;
import fr.esrf.TangoApi.*;
import jive.JiveUtils;


/**
 * A panel for selecting tango classes
 */
public class TreePanelClass extends TreePanel {

  // Filtering stuff
  String  classFilterString="*";
  Pattern classPattern=null;
  String[] classList;

  public TreePanelClass(MainPanel parent) {

    this.invoker = parent;
    this.self = this;
    setLayout(new BorderLayout());

  }

  public TangoNode createRoot() {
    return new RootNode();
  }

  void addAttribute(TangoNode attNode,String className,String attName) {
    if(searchNode(attNode,attName)!=null) {
      JOptionPane.showMessageDialog(null,"Attribute already exists","Error",JOptionPane.ERROR_MESSAGE);
      return;
    }
    treeModel.insertNodeInto(new TaskClassAttributePropertyNode(self,db,className,attName), attNode, 0);
  }

  public void applyFilter(String filter) {

    classFilterString = filter;

    if( filter.equals("*") ) {
      classPattern = null;
    } else if (filter.length()==0) {
      classPattern = null;
    } else {
      try {
        String f = filterToRegExp(classFilterString);
        classPattern = Pattern.compile(f);
      } catch (PatternSyntaxException e) {
        JOptionPane.showMessageDialog(invoker,e.getMessage());
      }
    }

  }

  public String getFilter() {
    return classFilterString;
  }

  public String[] getClassList() {
    return classList;
  }

  // ---------------------------------------------------------------

  class RootNode extends TangoNode {

    void populateNode() throws DevFailed {
      classList = db.get_class_list("*");
      for (int i = 0; i < classList.length; i++) {
        if( classPattern!=null ) {
          Matcher matcher =  classPattern.matcher(classList[i].toLowerCase());
          if( matcher.find() && matcher.start()==0 && matcher.end()==classList[i].length() ) {
            add(new ClassNode(classList[i]));
          }
        } else {
          add(new ClassNode(classList[i]));
        }
      }
    }

    public String toString() {
      return "Class:";
    }

  }

  // ---------------------------------------------------------------

  class ClassNode extends TangoNode {

    private String className;

    ClassNode(String className) {
      this.className = className;
    }

    void populateNode() throws DevFailed {
      add(new TaskClassPropertyNode(self,db,className));
      add(new AttributeNode(className));
      add(new DevicesNode(className));
    }

    public String toString() {
      return className;
    }

  }

  // ---------------------------------------------------------------

  class AttributeNode extends TangoNode {

    private String className;

    AttributeNode(String className) {
      this.className = className;
    }

    void populateNode() throws DevFailed {
      String[] list = db.get_class_attribute_list(getParent().toString(), "*");
      for(int i=0;i<list.length;i++)
        add(new TaskClassAttributePropertyNode(self,db,className,list[i]));
    }

    public String toString() {
      return "Attribute properties";
    }

    public ImageIcon getIcon() {
      return TangoNodeRenderer.atticon;
    }

    public int[] getAction() {
      if(JiveUtils.readOnly)
        return new int[0];
      else
        return new int[] {ACTION_ADDCLASSATT};
    }

    public void execAction(int action) {
      switch(action) {
        case ACTION_ADDCLASSATT:
          String newName = JOptionPane.showInputDialog(null,"Add class attribute","");
          if(newName==null) return;
          addAttribute(this,className,newName);
          break;
      }
    }

  }

  // ---------------------------------------------------------------

  class DevicesNode extends TangoNode {

    private String className;

    DevicesNode(String className) {
      this.className = className;
    }

    void populateNode() throws DevFailed {

      // Get the list of device name of the specified class
      DeviceData argin = new DeviceData();
      String request = "select name from device where class='" + className + "' order by name";
      argin.insert(request);
      DeviceData argout = db.command_inout("DbMySqlSelect", argin);
      DevVarLongStringArray arg = argout.extractLongStringArray();
      for(int i=0;i<arg.svalue.length;i++)
        add(new DeviceNode(arg.svalue[i]));



    }

    public String toString() {
      return "Devices";
    }

    public int[] getAction() {
      return new int[0];
    }

    public void execAction(int action) {
    }

  }

  // ---------------------------------------------------------------

  class DeviceNode extends TangoNode {

    private String devName;

    DeviceNode(String devName) {
      this.devName = devName;
    }

    void populateNode() throws DevFailed {
      add(new TaskDevicePropertyNode(self,db,devName));
      add(new TaskPollingNode(db,devName));
      add(new TaskEventNode(db,devName));
      add(new TaskAttributeNode(db,devName));
      add(new AttributeNode(devName));
      add(new TaskLoggingNode(db,devName));
    }

    public String toString() {
      return devName;
    }

    ImageIcon getIcon() {
      return TangoNodeRenderer.devicon;
    }

    String getValue() {
      return getDeviceInfo(devName);
    }

    String getTitle() {
      return "Device Info";
    }
    int[] getAction() {
      if(JiveUtils.readOnly)
        return new int[]{ACTION_MONITORDEV,
                         ACTION_TESTDEV,
                         ACTION_GOTOSERVNODE
        };
      else
        return new int[]{ACTION_COPY,
                         ACTION_PASTE,
                         ACTION_DELETE,
                         ACTION_MONITORDEV,
                         ACTION_TESTDEV,
                         ACTION_DEFALIAS,
                         ACTION_GOTOSERVNODE,
                         ACTION_RESTART,
                         ACTION_LOG_VIEWER
        };
    }

    void execAction(int actionNumber) {

      switch(actionNumber) {

        // ----------------------------------------------------------------------------
        case ACTION_COPY:
          JiveUtils.copyDeviceProperties(db,devName);
          break;

        // ----------------------------------------------------------------------------
        case ACTION_PASTE:
          pasteDeviceProperty(db,devName);
          break;

        // ----------------------------------------------------------------------------
        case ACTION_DELETE:
          int ok = JOptionPane.showConfirmDialog(invoker, "Delete device " + devName + " ?", "Confirm delete", JOptionPane.YES_NO_OPTION);
          if (ok == JOptionPane.YES_OPTION) {
            try {
              db.delete_device(devName);
            } catch (DevFailed e) {
              JiveUtils.showTangoError(e);
            }
            refresh();
          }
          break;

        // ----------------------------------------------------------------------------
        case ACTION_MONITORDEV:
          new atkpanel.MainPanel(devName, false, true, !JiveUtils.readOnly);
          break;

        // ----------------------------------------------------------------------------
        case ACTION_TESTDEV:
          testDevice(devName);
          break;

        // ----------------------------------------------------------------------------
        case ACTION_DEFALIAS:
          String alias = JOptionPane.showInputDialog(null,"Define device alias","");
          if(alias==null) return;
          try {
            db.put_device_alias(devName, alias);
          } catch (DevFailed e) {
            JiveUtils.showTangoError(e);
          }
          break;

        // ----------------------------------------------------------------------------
        case ACTION_GOTOSERVNODE:
          try {
            DbDevImportInfo info = db.import_device(devName);
            invoker.goToServerFullNode(info.server);
          } catch (DevFailed e) {
            JiveUtils.showTangoError(e);
          }
          break;

        // ----------------------------------------------------------------------------
        case ACTION_RESTART:
          try {
            DbDevImportInfo info = db.import_device(devName);
            DeviceProxy ds = new DeviceProxy("dserver/" + info.server);
            DeviceData in = new DeviceData();
            in.insert(devName);
            ds.command_inout("DevRestart", in);
          } catch (DevFailed e) {
            JiveUtils.showTangoError(e);
          }
          break;

        // ----------------------------------------------------------------------------
        case ACTION_LOG_VIEWER:
          launchLogViewer(devName);
          break;

      }

    }

  }

}
