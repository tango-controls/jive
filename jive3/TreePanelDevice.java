package jive3;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevVarLongStringArray;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.TangoApi.DbDevImportInfo;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.Vector;

import jive.JiveUtils;


/**
 * A panel for selecting tango devices
 */
public class TreePanelDevice extends TreePanel {

  // Filtering stuff
  String  deviceFilterString="*/*/*";
  String  domainFilterString="*";
  String  familyFilterString="*";
  String  memberFilterString="*";

  public TreePanelDevice(MainPanel parent) {

    this.invoker = parent;
    this.self = this;
    setLayout(new BorderLayout());

  }

  public void selectDevice(String devName) {

    int bslash = devName.indexOf('/');
    int lslash = devName.lastIndexOf('/');

    String domain = devName.substring(0,bslash);
    String family = devName.substring(bslash+1,lslash);
    String member = devName.substring(lslash+1);

    // Search server
    TangoNode domainNode = searchNode(root,domain);
    if(domainNode==null) return;
    TangoNode familyNode = searchNode(domainNode,family);
    if(familyNode==null) return;
    TangoNode memberNode = searchNode(familyNode,member);
    if(memberNode==null) return;
    TreePath selPath = new TreePath(root);
    selPath = selPath.pathByAddingChild(domainNode);
    selPath = selPath.pathByAddingChild(familyNode);
    selPath = selPath.pathByAddingChild(memberNode);
    tree.setSelectionPath(selPath);

  }

  public TangoNode createRoot() {
    return new RootNode(this);
  }

  public void applyFilter(String filter) {

    String[] filters = filter.split("/");
    if(filters.length!=3) {
      JOptionPane.showMessageDialog(invoker,"Invalid device filter\n3 fields expected: domain/family/member");
      return;
    }

    deviceFilterString = filter;
    domainFilterString = filters[0];
    familyFilterString = filters[1];
    memberFilterString = filters[2];

  }

  public String getFilter() {
    return deviceFilterString;
  }

  // ---------------------------------------------------------------

  class RootNode extends TangoNode {

    RootNode(TreePanel parentPanel) {
      this.parentPanel = parentPanel;
    }

    void populateNode() throws DevFailed {

      if( domainFilterString.equals("*") &&
          familyFilterString.equals("*") &&
          memberFilterString.equals("*") ) {

        // No filtering
        String[] list = db.get_device_domain(domainFilterString);
        for (int i = 0; i < list.length; i++)
          add(new DomainNode(list[i],parentPanel));

      } else {

        // Filtering
        String nameFilter = domainFilterString+"/"+familyFilterString+"/"+memberFilterString;
        String nameFilterSQL = parentPanel.replaceWildcard(nameFilter);

        DeviceData argin = new DeviceData();
        String request = "select distinct domain from device where name LIKE '" + nameFilterSQL +
                         "' order by domain";
        argin.insert(request);
        DeviceData argout = db.command_inout("DbMySqlSelect",argin);

        DevVarLongStringArray arg = argout.extractLongStringArray();
        for(int i=0;i<arg.svalue.length;i++) {
          add(new DomainNode(arg.svalue[i],parentPanel));
        }

      }
    }

    public String toString() {
      return "Device: ";
    }

  }

  // ---------------------------------------------------------------

  class DomainNode extends TangoNode {

    private String domain;

    DomainNode(String domain,TreePanel parentPanel) {
      this.domain = domain;
      this.parentPanel = parentPanel;
    }

    void populateNode() throws DevFailed {

      if( domainFilterString.equals("*") &&
          familyFilterString.equals("*") &&
          memberFilterString.equals("*") ) {

        // No filtering
        String[] list = db.get_device_family(domain + "/" + familyFilterString);
        for (int i = 0; i < list.length; i++)
          add(new FamilyNode(domain, list[i], parentPanel));

      } else {

        // Filtering
        String nameFilter = domain+"/"+familyFilterString+"/"+memberFilterString;
        String nameFilterSQL = parentPanel.replaceWildcard(nameFilter);

        DeviceData argin = new DeviceData();
        String request = "select distinct family from device where name LIKE '" + nameFilterSQL +
                         "' order by family";
        argin.insert(request);
        DeviceData argout = db.command_inout("DbMySqlSelect",argin);

        DevVarLongStringArray arg = argout.extractLongStringArray();
        for(int i=0;i<arg.svalue.length;i++) {
          add(new FamilyNode(domain, arg.svalue[i],parentPanel));
        }

      }

    }

    public String toString() {
      return domain;
    }

    int[] getAction() {
      return new int[]{ACTION_SELECT_PROP,
              ACTION_SELECT_POLLING,
              ACTION_SELECT_EVENT,
              ACTION_SELECT_ATTCONF,
              ACTION_SELECT_LOGGING};
    }

    void execAction(int actionNumber) {
      switch(actionNumber) {
        case ACTION_SELECT_PROP:
          parentPanel.selectNodesFromDomain(this,"Properties");
          break;
        case ACTION_SELECT_POLLING:
          parentPanel.selectNodesFromDomain(this,"Polling");
          break;
        case ACTION_SELECT_EVENT:
          parentPanel.selectNodesFromDomain(this,"Event");
          break;
        case ACTION_SELECT_ATTCONF:
          parentPanel.selectNodesFromDomain(this,"Attribute config");
          break;
        case ACTION_SELECT_LOGGING:
          parentPanel.selectNodesFromDomain(this,"Logging");
          break;
      }
    }

  }

  // ---------------------------------------------------------------

  class FamilyNode extends TangoNode {

    private String domain;
    private String family;

    FamilyNode(String domain, String family,TreePanel parentPanel) {
      this.domain = domain;
      this.family = family;
      this.parentPanel = parentPanel;
    }

    void populateNode() throws DevFailed {

      if( domainFilterString.equals("*") &&
          familyFilterString.equals("*") &&
          memberFilterString.equals("*") ) {

        // No filtering
        String prefix = domain + "/" + family + "/";
        String[] list = db.get_device_member(prefix + memberFilterString);
        for (int i = 0; i < list.length; i++)
          add(new DeviceNode(domain,family,list[i]));

      } else {

        // Filtering
        String nameFilter = domain+"/"+family+"/"+memberFilterString;
        String nameFilterSQL = parentPanel.replaceWildcard(nameFilter);

        DeviceData argin = new DeviceData();
        String request = "select distinct member from device where name LIKE '" + nameFilterSQL +
                         "' order by member";
        argin.insert(request);
        DeviceData argout = db.command_inout("DbMySqlSelect",argin);

        DevVarLongStringArray arg = argout.extractLongStringArray();
        for(int i=0;i<arg.svalue.length;i++) {
          add(new DeviceNode(domain,family, arg.svalue[i]));
        }

      }

    }

    public String toString() {
      return family;
    }

    int[] getAction() {
      return new int[]{ACTION_SELECT_PROP,
              ACTION_SELECT_POLLING,
              ACTION_SELECT_EVENT,
              ACTION_SELECT_ATTCONF,
              ACTION_SELECT_LOGGING};
    }

    void execAction(int actionNumber) {
      switch(actionNumber) {
        case ACTION_SELECT_PROP:
          parentPanel.selectNodesFromFamily(this,"Properties");
          break;
        case ACTION_SELECT_POLLING:
          parentPanel.selectNodesFromFamily(this,"Polling");
          break;
        case ACTION_SELECT_EVENT:
          parentPanel.selectNodesFromFamily(this,"Event");
          break;
        case ACTION_SELECT_ATTCONF:
          parentPanel.selectNodesFromFamily(this,"Attribute config");
          break;
        case ACTION_SELECT_LOGGING:
          parentPanel.selectNodesFromFamily(this,"Logging");
          break;
      }
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

  class DeviceNode extends TangoNode {

    private String domain;
    private String family;
    private String member;
    private String devName;

    DeviceNode(String domain, String family,String member) {
      this.domain = domain;
      this.family = family;
      this.member = member;
      devName = this.domain + "/" + this.family + "/" + member;
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
      return member;
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
                         ACTION_GOTOSERVNODE,
                         ACTION_GOTOADMINNODE
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
                         ACTION_GOTOADMINNODE,
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
            invoker.goToServerNode(info.server);
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
        case ACTION_GOTOADMINNODE:
          try {
            DbDevImportInfo info = db.import_device(devName);
            selectDevice("dserver/"+info.server);
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

  // ---------------------------------------------------------------

}
