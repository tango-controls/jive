package jive3;

import fr.esrf.Tango.DevFailed;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.IOException;
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

  public TreePanelClass() {

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

  public TangoNode selectClass(String className) {

    // Search class
    TangoNode classNode = searchNode(root,className);
    if(classNode==null) return null;
    TreePath selPath = new TreePath(root);
    selPath = selPath.pathByAddingChild(classNode);
    tree.setSelectionPath(selPath);
    return classNode;

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

    void execAction(int number,boolean multipleCall) {
    }

  }

  // ---------------------------------------------------------------

  class ClassNode extends TangoNode {

    private String className;

    ClassNode(String className) {
      this.className = className;
    }

    Action[] getAction() {
      if(JiveUtils.readOnly)
        return new Action[0];
      else
        return new Action[] {
            TreePanel.getAction(ACTION_SAVE_PROP)
        };
    }

    void execAction(int actionNumber,boolean multipleCall) {

      switch(actionNumber) {

        // ----------------------------------------------------------------------------
        case ACTION_SAVE_PROP:
          try {
            DbFileWriter.SaveAllClassProperties(className);
          } catch (DevFailed e) {
            JiveUtils.showTangoError(e);
          } catch (IOException e2) {
            JiveUtils.showJiveError(e2.getMessage());
          }
          break;

      }

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

    public Action[] getAction() {
      if(JiveUtils.readOnly)
        return new Action[0];
      else
        return new Action[] {
            TreePanel.getAction(ACTION_ADDCLASSATT),
            TreePanel.getAction(ACTION_SAVE_PROP)
        };
    }

    public void execAction(int action,boolean multipleCall) {
      switch(action) {
        case ACTION_ADDCLASSATT:
          String newName = JOptionPane.showInputDialog(null,"Add class attribute","");
          if(newName==null) return;
          addAttribute(this,className,newName);
          break;
        case ACTION_SAVE_PROP:
          try {
            DbFileWriter.SaveClassAttributesProperties(className);
          } catch (DevFailed e) {
            JiveUtils.showTangoError(e);
          } catch (IOException e2) {
            JiveUtils.showJiveError(e2.getMessage());
          }
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

    public void execAction(int action,boolean multipleCall) {
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

    Action[] getAction() {
      if(JiveUtils.readOnly)
        return new Action[]{
            TreePanel.getAction(ACTION_MONITORDEV),
            TreePanel.getAction(ACTION_TESTDEV),
            TreePanel.getAction(ACTION_GOTOSERVNODE)
        };
      else
        return new Action[]{
            TreePanel.getAction(ACTION_COPY),
            TreePanel.getAction(ACTION_PASTE),
            TreePanel.getAction(ACTION_DELETE),
            TreePanel.getAction(ACTION_MONITORDEV),
            TreePanel.getAction(ACTION_TESTDEV),
            TreePanel.getAction(ACTION_DEFALIAS),
            TreePanel.getAction(ACTION_GOTOSERVNODE),
            TreePanel.getAction(ACTION_RESTART),
            TreePanel.getAction(ACTION_LOG_VIEWER),
            TreePanel.getAction(ACTION_SAVE_PROP)
        };
    }

    void execAction(int actionNumber,boolean multipleCall) throws IOException {

      switch(actionNumber) {

        // ----------------------------------------------------------------------------
        case ACTION_COPY:
          JiveUtils.the_clipboard.clear();
          JiveUtils.copyDeviceProperties(db,devName);
          break;

        // ----------------------------------------------------------------------------
        case ACTION_PASTE:
          pasteDeviceProperty(db,devName);
          break;

        // ----------------------------------------------------------------------------
        case ACTION_DELETE:
          if( multipleCall ) {

            try {
              db.delete_device(devName);
            } catch (DevFailed e) {
              throw new IOException(devName + ":" + e.errors[0].desc);
            }

          } else {

            int ok = JOptionPane.showConfirmDialog(invoker, "Delete device " + devName + " ?", "Confirm delete", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
              try {
                db.delete_device(devName);
              } catch (DevFailed e) {
                JiveUtils.showTangoError(e);
              }
              refresh();
            }

          }
          break;

        // ----------------------------------------------------------------------------
        case ACTION_MONITORDEV:
          JiveUtils.launchAtkPanel(devName);
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

        // ----------------------------------------------------------------------------
        case ACTION_SAVE_PROP:
          try {
            DbFileWriter.SaveAllDeviceProperties(devName);
          } catch (DevFailed e) {
            JiveUtils.showTangoError(e);
          } catch (IOException e2) {
            JiveUtils.showJiveError(e2.getMessage());
          }
          break;

      }

    }

  }

}
