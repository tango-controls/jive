package jive3;

import fr.esrf.TangoApi.*;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevVarLongStringArray;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.TreeNode;
import java.awt.event.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import jive.JiveUtils;
import jive.ExecDev;

class Action {

  static TangoNode[] selectedNodes = null;

  Action(int a,boolean aM,String n) {

    action = a;
    allowMultiple = aM;
    name = n;
    menuItem = new JMenuItem(name);

    menuItem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        execAction();
      }
    });

  }

  void execAction() {

    if(selectedNodes!=null) {

      if(allowMultiple && selectedNodes.length>1) {

        Vector<String> errors = new Vector<String>();

        switch(action) {

          case TreePanel.ACTION_DELETE:

            int ok = JOptionPane.showConfirmDialog(TreePanel.invoker, "Delete " + selectedNodes.length + " items ?",
                "Confirm delete", JOptionPane.YES_NO_OPTION);

            if (ok == JOptionPane.YES_OPTION) {

              for(int i=0;i<selectedNodes.length;i++) {
                try {
                  selectedNodes[i].execAction(action,true);
                } catch (IOException e) {
                  errors.add(e.getMessage());
                }
              }
              if(errors.size()>0)
                JiveUtils.showJiveErrors(errors);

            }

            TreePanel.panelInvoker.refresh();
            break;

          case TreePanel.ACTION_SAVESERVER:

            File file = TreePanel.getSaveFile(TreePanel.invoker);
            if( file!=null ) {

              try {

                TreePanel.globalResFile = new FileWriter(file.getAbsolutePath());
                Date date = new Date(System.currentTimeMillis());
                TreePanel.globalResFile.write("#\n# Resource backup , created " + date + "\n#\n\n");
                for(int i=0;i<selectedNodes.length;i++)
                  selectedNodes[i].execAction(action,true);
                TreePanel.globalResFile.close();

              } catch (IOException e) {
                JiveUtils.showJiveError(e.getMessage());
              }

            }

            break;

          default:

            try {
              for(int i=0;i<selectedNodes.length;i++)
                selectedNodes[i].execAction(action,true);
            } catch (IOException e) {
              JiveUtils.showJiveError("Error while performing " + name + " !\n");
            }

        }


      } else {

        try {
          if(selectedNodes.length>0)
            selectedNodes[0].execAction(action,false);
        } catch (IOException e) {
          JiveUtils.showJiveError("Error while performing " + name + " !\n");
        }

      }
    }

  }

  String name;
  int action;
  boolean allowMultiple;
  JMenuItem menuItem;

}

/**
 * An abstract class for tree panel.
 */
public abstract class TreePanel extends JPanel implements TreeSelectionListener,MouseListener,TreeExpansionListener,KeyListener {

  protected JTree            tree;
  protected JScrollPane      treeView = null;
  protected DefaultTreeModel treeModel;
  protected TangoNode        root;
  protected Database         db;
  TreePanel                  self;
  private   boolean          updateOnChange;

  // Actions
  public final static int ACTION_COPY          = 0;
  public final static int ACTION_PASTE         = 1;
  public final static int ACTION_RENAME        = 2;
  public final static int ACTION_DELETE        = 3;
  public final static int ACTION_ADDCLASS      = 4;
  public final static int ACTION_TESTADMIN     = 5;
  public final static int ACTION_SAVESERVER    = 6;
  public final static int ACTION_CLASSWIZ      = 7;
  public final static int ACTION_ADDDEVICE     = 8;
  public final static int ACTION_DEVICESWIZ    = 9;
  public final static int ACTION_MONITORDEV    = 10;
  public final static int ACTION_TESTDEV       = 11;
  public final static int ACTION_DEFALIAS      = 12;
  public final static int ACTION_GOTODEVNODE   = 13;
  public final static int ACTION_RESTART       = 14;
  public final static int ACTION_DEVICEWIZ     = 15;
  public final static int ACTION_GOTOSERVNODE  = 16;
  public final static int ACTION_GOTOADMINNODE = 17;
  public final static int ACTION_ADDCLASSATT   = 18;
  public final static int ACTION_UNEXPORT      = 19;
  public final static int ACTION_SELECT_PROP    = 20;
  public final static int ACTION_SELECT_POLLING = 21;
  public final static int ACTION_SELECT_EVENT   = 22;
  public final static int ACTION_SELECT_ATTCONF = 23;
  public final static int ACTION_SELECT_LOGGING = 24;
  public final static int ACTION_LOG_VIEWER     = 25;
  public final static int ACTION_DEV_DEPEND     = 26;
  public final static int ACTION_THREAD_POLL    = 27;
  public final static int ACTION_VIEW_HISTORY   = 28;
  public final static int ACTION_MOVE_SERVER    = 29;
  public final static int ACTION_CREATE_ATTPROP = 30;
  public final static int ACTION_START_SERVER   = 31;
  public final static int ACTION_STOP_SERVER    = 32;
  public final static int ACTION_RESTART_SERVER = 33;
  public final static int ACTION_START_LEVEL    = 34;
  public final static int ACTION_STOP_LEVEL     = 35;
  public final static int ACTION_START_HOST     = 36;
  public final static int ACTION_STOP_HOST      = 37;
  public final static int ACTION_CH_HOST_USAGE  = 38;
  public final static int ACTION_GO_TO_STATER   = 39;
  public final static int ACTION_CH_LEVEL       = 40;
  public final static int ACTION_TERMINAL       = 41;
  public final static int ACTION_NEW_SERVERS    = 42;
  public final static int ACTION_COPY_ATT_SET   = 43;
  public final static int ACTION_SAVE_PROP      = 44;

  public final static Action[] actions = {
    new Action(ACTION_COPY,false,"Copy"),
    new Action(ACTION_PASTE,false,"Paste"),
    new Action(ACTION_RENAME,false,"Rename"),
    new Action(ACTION_DELETE,true,"Delete"),
    new Action(ACTION_ADDCLASS,false,"Add class"),
    new Action(ACTION_TESTADMIN,false,"Test admin server"),
    new Action(ACTION_SAVESERVER,true,"Save server data"),
    new Action(ACTION_CLASSWIZ,false,"Class(es) wizard"),
    new Action(ACTION_ADDDEVICE,false,"Add device"),
    new Action(ACTION_DEVICESWIZ,false,"Device(s) wizard"),
    new Action(ACTION_MONITORDEV,true,"Monitor device"),
    new Action(ACTION_TESTDEV,true,"Test device"),
    new Action(ACTION_DEFALIAS,false,"Define device alias"),
    new Action(ACTION_GOTODEVNODE,false,"Go to device node"),
    new Action(ACTION_RESTART,false,"Restart device"),
    new Action(ACTION_DEVICEWIZ,false,"Device wizard"),
    new Action(ACTION_GOTOSERVNODE,false,"Go to server node"),
    new Action(ACTION_GOTOADMINNODE,false,"Go to device admin node"),
    new Action(ACTION_ADDCLASSATT,false,"Add attribute"),
    new Action(ACTION_UNEXPORT,false,"Unexport devices"),
    new Action(ACTION_SELECT_PROP,false,"Select 'property node'"),
    new Action(ACTION_SELECT_POLLING,false,"Select 'polling' nodes"),
    new Action(ACTION_SELECT_EVENT,false,"Select 'event' nodes"),
    new Action(ACTION_SELECT_ATTCONF,false,"Select 'attribute config' nodes"),
    new Action(ACTION_SELECT_LOGGING,false,"Select 'logging' nodes"),
    new Action(ACTION_LOG_VIEWER,false,"Log Viewer"),
    new Action(ACTION_DEV_DEPEND,false,"Devices dependencies"),
    new Action(ACTION_THREAD_POLL,false,"Polling threads manager"),
    new Action(ACTION_VIEW_HISTORY,false,"View history"),
    new Action(ACTION_MOVE_SERVER,false,"Move server"),
    new Action(ACTION_CREATE_ATTPROP,false,"Create attribute property"),
    new Action(ACTION_START_SERVER,false,"Start Server"),
    new Action(ACTION_STOP_SERVER,false,"Stop Server"),
    new Action(ACTION_RESTART_SERVER,false,"Restart Server"),
    new Action(ACTION_START_LEVEL,false,"Start all servers (Level)"),
    new Action(ACTION_STOP_LEVEL,false,"Stop all servers (Level)"),
    new Action(ACTION_START_HOST,false,"Start all servers (Host)"),
    new Action(ACTION_STOP_HOST,false,"Stop all servers (Host)"),
    new Action(ACTION_CH_HOST_USAGE,false,"Edit Host Usage"),
    new Action(ACTION_GO_TO_STATER,false,"Go to Starter Node"),
    new Action(ACTION_CH_LEVEL,false,"Change Level"),
    new Action(ACTION_TERMINAL,false,"Open terminal"),
    new Action(ACTION_NEW_SERVERS,false,"Start new servers"),
    new Action(ACTION_COPY_ATT_SET,false,"Copy Setpoints"),
    new Action(ACTION_SAVE_PROP,false,"Save properties")
  };

  static File       lastFile = null;
  static FileWriter globalResFile;
  static MainPanel  invoker;
  static TreePanel  panelInvoker;

  private static JPopupMenu actionMenu;

  static {
    actionMenu = new JPopupMenu();
    for(int i=0;i<actions.length;i++)
      actionMenu.add(actions[i].menuItem);
  }

  // Initialise tree root
  abstract TangoNode createRoot();

  // Initialise the tree
  public void initTree() {

    root = createRoot();
    treeModel = new DefaultTreeModel(root);
    tree = new JTree(treeModel);
    tree.setEditable(false);
    tree.setCellRenderer(new TangoNodeRenderer());
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    //tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    tree.setBorder(BorderFactory.createLoweredBevelBorder());
    tree.addMouseListener(this);
    tree.addTreeSelectionListener(this);
    tree.addKeyListener(this);
    tree.setToggleClickCount(0);
    treeView = new JScrollPane(tree);
    add(treeView, BorderLayout.CENTER);
    updateOnChange = true;
    tree.addTreeExpansionListener(this);

  }

  static Action getAction(int action) {

    boolean found = false;
    int i = 0;

    while(!found && i<actions.length) {
      found = actions[i].action == action;
      if(!found) i++;
    }

    if(!found)
      throw new IllegalStateException("Action.getAction() unexpected action code");

    return actions[i];

  }

  static Action nodeAction(TangoNode node,int action) {

    boolean found = false;
    int i=0;
    Action[] nodeActions = node.getAction();
    while(!found && i<nodeActions.length) {
      found = nodeActions[i].action == action;
      if(!found) i++;
    }

    if(!found)
      return null;
    else
      return nodeActions[i];

  }

  static File getSaveFile(MainPanel invoker) {

    JFileChooser chooser = new JFileChooser(".");
    int ok = JOptionPane.YES_OPTION;
    if (lastFile != null)
      chooser.setSelectedFile(lastFile);

    int returnVal = chooser.showSaveDialog(invoker);

    if (returnVal == JFileChooser.APPROVE_OPTION) {
      lastFile = chooser.getSelectedFile();
      if (lastFile != null) {
        if (lastFile.exists()) ok = JOptionPane.showConfirmDialog(invoker, "Do you want to overwrite " +
            lastFile.getName() + " ?", "Confirm overwrite", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
          return lastFile;
        } else {
          return null;
        }
      }
    }

    return null;

  }

  // Set the database
  public void setDatabase(Database db) {

    this.db = db;
    if(treeView!=null) {
      remove(treeView);
      treeView=null;
    }
    if(db!=null) initTree();
    else repaint();

  }

  // Refresh the tree
  public void refresh() {

    if(treeView==null)
      return;
    
    TreePath oldPath = tree.getSelectionPath();
    remove(treeView);
    if(db!=null) initTree();

    invoker.resetNavigation();
    selectPath(oldPath);

  }

  public void selectPath(TreePath path) {

    if (path != null) {

      // Reselect old node
      TreePath newPath = new TreePath(root);
      TangoNode node = root;
      boolean found = true;
      int i = 1;
      while (found && i < path.getPathCount()) {

        String item = path.getPathComponent(i).toString();

        // Search for item
        node = searchNode(node,item);

        // Construct the new path
        if (node!=null) {
          newPath = newPath.pathByAddingChild(node);
          i++;
        } else {
          found = false;
        }

      }

      tree.setSelectionPath(newPath);
      tree.expandPath(newPath);
      tree.makeVisible(newPath);
      tree.scrollPathToVisible(newPath);

    }

  }

  // Refresh value
  public void refreshValues() {

    if(treeView==null)
      return;

    TreePath[] selPaths = tree.getSelectionPaths();
    if(selPaths!=null) {

      TangoNode[] nodes = new TangoNode[selPaths.length];
      for(int i=0;i<nodes.length;i++)
        nodes[i] = (TangoNode)selPaths[i].getLastPathComponent();
      invoker.updatePanel(nodes);

      // Handle bug when keyboard is used
      if(selPaths.length==1)
        tree.scrollPathToVisible(selPaths[0]);

    } else {

      invoker.updatePanel(null);

    }

  }

  // Check if itemName if an item at first level
  public boolean isRootItem(String itemName) {

    TangoNode node = searchNodeStartingWith(root, itemName);
    return (node!=null);

  }

  // Select a "first level" item
  public TreePath selectRootItem(String itemName) {

    TangoNode node = searchNodeStartingWith(root, itemName);
    TreePath selPath = new TreePath(root);
    selPath = selPath.pathByAddingChild(node);
    tree.setSelectionPath(selPath);
    return selPath;

  }

  // Search the tree
  public TangoNode searchNode(TangoNode startNode,String value) {

    int numChild = treeModel.getChildCount(startNode);
    int i = 0;
    boolean found = false;
    TangoNode elem = null;

    while (i < numChild && !found) {
      elem = (TangoNode) treeModel.getChild(startNode, i);
      found = elem.toString().compareToIgnoreCase(value) == 0;
      if (!found) i++;
    }

    if(found) {
      return elem;
    } else {
      return null;
    }

  } 
  // Search the tree
  public TangoNode searchNodeStartingWith(TangoNode startNode,String value) {

    int numChild = treeModel.getChildCount(startNode);
    int i = 0;
    boolean found = false;
    TangoNode elem = null;

    while (i < numChild && !found) {
      elem = (TangoNode) treeModel.getChild(startNode, i);
      found = elem.toString().toLowerCase().startsWith(value.toLowerCase());
      if (!found) i++;
    }

    if(found) {
      return elem;
    } else {
      return null;
    }

  }

  // Search the tree
  public TangoNode searchNodeCaseSensitive(TangoNode startNode,String value) {

    int numChild = treeModel.getChildCount(startNode);
    int i = 0;
    boolean found = false;
    TangoNode elem = null;

    while (i < numChild && !found) {
      elem = (TangoNode) treeModel.getChild(startNode, i);
      found = elem.toString().compareTo(value) == 0;
      if (!found) i++;
    }

    if(found) {
      return elem;
    } else {
      return null;
    }

  }

  // Test a device
  public void testDevice(String devName) {

    JDialog dlg = new JDialog(invoker,false);
    dlg.setTitle("Device Panel ["+devName+"]");
    try {
      ExecDev p = new ExecDev(devName);
      dlg.setContentPane(p);
      JiveUtils.centerDialog(dlg);
      dlg.setVisible(true);
    } catch(DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  public void pasteDeviceProperty(Database db, String devname) {

    int i;
    try {

      JiveUtils.the_clipboard.parse();

      // Paste device properties
      for (i = 0; i < JiveUtils.the_clipboard.getObjectPropertyLength(); i++) {
        db.put_device_property(devname,
                JiveUtils.makeDbDatum(JiveUtils.the_clipboard.getObjectPropertyName(i),
                        JiveUtils.the_clipboard.getObjectPropertyValue(i)));
      }

      // Paste attribute properties
      for (i = 0; i < JiveUtils.the_clipboard.getAttPropertyLength(); i++) {
        DbAttribute att = new DbAttribute(JiveUtils.the_clipboard.getAttName(i));
        att.add(JiveUtils.the_clipboard.getAttPropertyName(i),
                JiveUtils.the_clipboard.getAttPropertyValue(i));
        db.put_device_attribute_property(devname, att);
      }

    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  // Return deviceInfo
  public String getDeviceInfo(String devName) {

    String result = "";
    int i;

    try {

      DeviceProxy ds = new DeviceProxy(devName);

      result = "<b>Device Info</b>\n<hr>\n";
      DeviceInfo info = ds.get_info();
      result += info.toString()+"\n";

      // Append Polling status
      result += "\n\n<b>Polling Status</b>\n<hr>\n";
      String[] pi = ds.polling_status();
      for (i = 0; i < pi.length; i++) result += (pi[i] + "\n\n");

    } catch (DevFailed e) {

      for (i = 0; i < e.errors.length; i++) {
        result += "Desc -> " + e.errors[i].desc + "\n";
        result += "Reason -> " + e.errors[i].reason + "\n";
        result += "Origin -> " + e.errors[i].origin + "\n";
      }

    }
    return result;

  }

  // ---------------------------------------------------------------

  public void treeExpanded(TreeExpansionEvent event) {}

  public void treeCollapsed(TreeExpansionEvent event) {}

  // ---------------------------------------------------------------

  public void valueChanged(TreeSelectionEvent e) {

    if(updateOnChange) refreshValues();

  }

  // ---------------------------------------------------------------
  public void refreshNode(TangoNode node,String childToSelect) {

    node.clearNodes();
    node.getChildCount();
    treeModel.nodeStructureChanged(node);
    if( childToSelect!=null ) {

      TreeNode[] nodes = node.getPath();
      TreePath path = new TreePath(nodes);
      TreeNode subNode = searchNode(node,childToSelect);
      path = path.pathByAddingChild(subNode);
      tree.setSelectionPath(path);
      tree.expandPath(path);
      tree.makeVisible(path);
      tree.scrollPathToVisible(path);

    }

  }

  // ---------------------------------------------------------------
  private void createSelectedNodes(TreePath[] paths) {

    Action.selectedNodes = new TangoNode[paths.length];
    for(int i=0;i<paths.length;i++)
      Action.selectedNodes[i] = (TangoNode)(paths[i].getLastPathComponent());

  }

  // ---------------------------------------------------------------
  private TreePath getNodePath(TangoNode node) {

    TreeNode[] pNodes = node.getPath();
    return new TreePath(pNodes);

  }

  private boolean isGoodClass(String devName,String className,String[] devList) {

    boolean found = false;
    int i=0;
    while(i<devList.length && !found) {
      found = devList[i].equalsIgnoreCase(devName);
      if(found)
        return devList[i+1].equalsIgnoreCase(className);
      i+=2;
    }

    return false;

  }

  // ---------------------------------------------------------------
  public void selectNodesFromDomain(TangoNode startNode,String nodeName) {

    tree.clearSelection();
    updateOnChange = false;
    String[] list;

    // Get the list of device name with class
    try {

      DeviceData argin = new DeviceData();
      String request = "select name,class from device where domain='" + startNode.toString() + "'";
      argin.insert(request);
      DeviceData argout = db.command_inout("DbMySqlSelect",argin);

      DevVarLongStringArray arg = argout.extractLongStringArray();
      Vector vList = new Vector();
      for(int i=0;i<arg.svalue.length;i+=2) {
        if(arg.lvalue[i/2]!=0) {
          vList.add(arg.svalue[i]);
          vList.add(arg.svalue[i+1]);
        }
      }
      list = new String[vList.size()];
      for(int i=0;i<list.length;i++)
        list[i]=(String)vList.get(i);

    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
      return;
    }

    JFrame top = (JFrame) ATKGraphicsUtils.getWindowForComponent(this);
    TangoClassSelector classSel = new TangoClassSelector(top,list,nodeName);
    String classToSelect = classSel.getSelectedClass();
    if( classToSelect==null ) {
      updateOnChange = true;
      return;
    }

    int numFamily = treeModel.getChildCount(startNode);
    for(int i=0;i<numFamily;i++) {
      TangoNode fNode = (TangoNode) treeModel.getChild(startNode, i);
      int numDev = treeModel.getChildCount(fNode);
      for(int j=0;j<numDev;j++) {
        TangoNode devNode = (TangoNode) treeModel.getChild(fNode, j);
        String devName = devNode.getParent().getParent().toString() + "/" +
                         devNode.getParent().toString() + "/" +
                         devNode.toString();
        if( isGoodClass(devName,classToSelect,list) )
          selectNodes(devNode,nodeName);
      }
    }

    refreshValues();
    updateOnChange = true;

  }

  // ---------------------------------------------------------------
  public void selectNodesFromFamily(TangoNode startNode,String nodeName) {

    tree.clearSelection();
    updateOnChange = false;
    String[] list;

    // Get the list of device name with class
    try {

      DeviceData argin = new DeviceData();
      String request = "select name,class from device where family='" + startNode.toString() + "' and domain='" + startNode.getParent().toString() + "'";
      argin.insert(request);
      DeviceData argout = db.command_inout("DbMySqlSelect",argin);

      DevVarLongStringArray arg = argout.extractLongStringArray();
      Vector vList = new Vector();
      for(int i=0;i<arg.svalue.length;i+=2) {
        if(arg.lvalue[i/2]!=0) {
          vList.add(arg.svalue[i]);
          vList.add(arg.svalue[i+1]);
        }
      }
      list = new String[vList.size()];
      for(int i=0;i<list.length;i++)
        list[i]=(String)vList.get(i);

    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
      return;
    }

    JFrame top = (JFrame) ATKGraphicsUtils.getWindowForComponent(this);
    TangoClassSelector classSel = new TangoClassSelector(top,list,nodeName);
    String classToSelect = classSel.getSelectedClass();
    if( classToSelect==null ) {
      updateOnChange = true;
      return;
    }


    int numDev = treeModel.getChildCount(startNode);
    for(int i=0;i<numDev;i++) {
      TangoNode devNode = (TangoNode) treeModel.getChild(startNode, i);
      String devName = devNode.getParent().getParent().toString() + "/" +
                       devNode.getParent().toString() + "/" +
                       devNode.toString();
      if( isGoodClass(devName,classToSelect,list) )
        selectNodes(devNode,nodeName);
    }

    refreshValues();
    updateOnChange = true;

  }

  // ---------------------------------------------------------------
  private void selectNodes(TangoNode startNode,String nodeName) {

    int numItem = treeModel.getChildCount(startNode);
    int j=0;
    boolean found = false;
    while(!found && j<numItem) {
      TangoNode iNode = (TangoNode) treeModel.getChild(startNode, j);
      found = nodeName.equalsIgnoreCase(iNode.toString());
      if(found) {
        tree.addSelectionPath(getNodePath(iNode));
      }
      j++;
    }

  }

  // ---------------------------------------------------------------
  public String filterToRegExp(String filter) {
    
    if(filter.equals("*")) return "*";
    // Replace * by [a-z0-9_\\-\\.]*
    String wildcard = "[a-z0-9_\\-\\.]*";
    StringBuffer ret= new StringBuffer();
    int length = filter.length();
    for(int i=0;i<length;i++) {
      char c = filter.charAt(i);
      if( c=='*' ) {
        ret.append(wildcard);
      } else {
        ret.append(c);
      }
    }
    return ret.toString().toLowerCase();

  }

  // ---------------------------------------------------------------
  public String replaceWildcard(String in) {

    StringBuffer ret = new StringBuffer();
    int length = in.length();
    int idx=0;
    while(idx<length) {
      if( in.charAt(idx)=='*' ) {
        ret.append('%');
      } else if ( in.charAt(idx)=='_' ) {
        ret.append("\\_");
      } else {
        ret.append(in.charAt(idx));
      }
      idx++;
    }
    return ret.toString();

  }

  // ---------------------------------------------------------------
  public void launchLogViewer(String devName) {
    fr.esrf.logviewer.Main m = new fr.esrf.logviewer.Main(new String[0],true);
    m.selectDevice(devName);
  }

  // ---------------------------------------------------------------
  public void launchDevDepend(String srvName) {

    try {
      admin.astor.tools.DeviceHierarchyDialog dlg = new admin.astor.tools.DeviceHierarchyDialog(invoker, srvName);
      dlg.setTitle("Device Hierarchy");
      dlg.setVisible(true);
    } catch(DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  // ---------------------------------------------------------------
  public void launchPollingThreadsManager(String srvName) {

    try {
      admin.astor.tools.PoolThreadsManager dlg = new admin.astor.tools.PoolThreadsManager(invoker, srvName);
      dlg.setTitle("Polling threads manager");
      dlg.setVisible(true);
    } catch(DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  // ---------------------------------------------------------------
  public void createEmptyAttributeProperty(String devName) {

    String propName = JOptionPane.showInputDialog(invoker, "Name: (attribute/prop_name)" , "Create attribute property", JOptionPane.OK_CANCEL_OPTION | JOptionPane.PLAIN_MESSAGE);
    if (propName != null) {
      String[] split = propName.split("/");
      if(split.length!=2) {
        JiveUtils.showJiveError("Invalid name syntax\nattribute/prop_name expected");
      } else {
        try {
          DbAttribute dbAtt = new DbAttribute(split[0]);
          dbAtt.add(split[1],"");
          db.put_device_attribute_property(devName, dbAtt);
        } catch (DevFailed e) {
          JiveUtils.showTangoError(e);
        }
        refresh();
      }
    }

  }

  // ---------------------------------------------------------------
  public void putAttributeProperty(String devName, String attName, String propName, String value) {

    try {
      DbAttribute att = new DbAttribute(attName);
      att.add(propName, JiveUtils.makeStringArray(value));
      db.put_device_attribute_property(devName, att);
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  // ---------------------------------------------------------------
  void saveServerData(FileWriter fw,String srvName) throws IOException {

    int i,l;

    try {

      JiveUtils.savedClass.clear();

      String[] class_list = db.get_server_class_list(srvName);

      for (i = 0; i < class_list.length; i++) {

        // Device declaration and resource

        fw.write("#---------------------------------------------------------\n");
        fw.write("# SERVER " + srvName + ", " + class_list[i] + " device declaration\n");
        fw.write("#---------------------------------------------------------\n\n");

        String[] dev_list = db.get_device_name(srvName, class_list[i]);
        JiveUtils.printFormatedRes(srvName + "/DEVICE/" + class_list[i] + ": ", dev_list, fw);
        fw.write("\n");

        for (l = 0; l < dev_list.length; l++) {

          DbFileWriter.SaveDeviceProperties(dev_list[l], fw);

          try {

            DbFileWriter.SaveDeviceAttributesProperties(dev_list[l], fw);

          } catch (DevFailed e) {

            JiveUtils.showJiveError("Attribute properties for " + dev_list[l] + " has not been saved !\n"
                + e.errors[0].desc);

          }

        }

        fw.write("\n");

        // We save class properties only once
        if( !JiveUtils.isSavedClass(class_list[i]) ) {

          fw.write("#---------------------------------------------------------\n");
          fw.write("# CLASS " + class_list[i] + " properties\n");
          fw.write("#---------------------------------------------------------\n\n");

          DbFileWriter.SaveClassProperties(class_list[i],fw);
          DbFileWriter.SaveClassAttributesProperties(class_list[i],fw);

          fw.write("\n");

          // Mark class as saved
          JiveUtils.addSavedClass(class_list[i]);

        }

      }

      // Save admin server data
      String[] prop_list;
      String admDevName = "dserver/" + srvName;
      DbFileWriter.SaveDeviceProperties(admDevName, fw);

    } catch (DevFailed e) {

      JiveUtils.showTangoError(e);

    }

  }

  // ---------------------------------------------------------------

  public void keyTyped(KeyEvent e) {}
  public void keyPressed(KeyEvent e) {

    if( tree.isSelectionEmpty() )
      return;

    TreePath[] paths = tree.getSelectionPaths();
    createSelectedNodes(paths);

    if( e.getKeyCode() == KeyEvent.VK_F2 ) {

      // Rename
      if(paths.length==1) {
        Action a = nodeAction(Action.selectedNodes[0], ACTION_RENAME);
        if(a!=null) a.execAction();
      }

    }

    if( e.getKeyCode() == KeyEvent.VK_DELETE ) {

      Action a = nodeAction(Action.selectedNodes[0],ACTION_DELETE);
      boolean allOk = a!=null;
      int i = 1;
      while(allOk && i<Action.selectedNodes.length) {
        allOk = nodeAction(Action.selectedNodes[i],ACTION_DELETE)!=null;
        i++;
      }
      TreePanel.panelInvoker = this;
      if(allOk)
        a.execAction();

    }


  }
  public void keyReleased(KeyEvent e) {}

  // ---------------------------------------------------------------
  public void mousePressed(MouseEvent e) {

    TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
    if (selPath != null) {

      if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3) {

        // Multiple selection
        if (!tree.isSelectionEmpty()) {

          // Check that the node is not already selected
          // If not, add it to the path
          if( !tree.isPathSelected(selPath) )
            tree.addSelectionPath(selPath);

          TreePath[] paths = tree.getSelectionPaths();

          if(paths.length>1) {

            int hasMultiple[] = new int[actions.length];
            for(int i=0;i<paths.length;i++) {
              Action[] nodeActions = ((TangoNode)paths[i].getLastPathComponent()).getAction();
              for(int j=0;j<nodeActions.length;j++) {
                if(nodeActions[j].allowMultiple)
                  hasMultiple[nodeActions[j].action]++;
              }
            }

            boolean popupMultiple = false;
            for(int i=0;i<hasMultiple.length;i++) {
              if( hasMultiple[i]==paths.length ) {
                popupMultiple = true;
              }
            }

            if( popupMultiple ) {

              createSelectedNodes(paths);
              for (int i = 0; i < actions.length; i++) {
                if(hasMultiple[i]==paths.length)
                  actionMenu.getComponent(i).setVisible(true);
                else
                  actionMenu.getComponent(i).setVisible(false);
              }
              actionMenu.show(tree, e.getX(), e.getY());
              TreePanel.panelInvoker = this;
              return;

            }

          }

        }

        // Single selection only
        createSelectedNodes(new TreePath[]{selPath});
        tree.setSelectionPath(selPath);
        Action[] actionsNode = Action.selectedNodes[0].getAction();
        if (actions.length > 0) {
          for(int i = 0; i < actions.length; i++)
            actionMenu.getComponent(i).setVisible(false);
          for(int i = 0; i < actionsNode.length;i ++)
            actionsNode[i].menuItem.setVisible(true);
          actionMenu.show(tree, e.getX(), e.getY());
        }

      }

      // Launch ATK panel on double click (when possible)
      if(e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1 ) {

        // Force single selection on double click
        createSelectedNodes(new TreePath[]{selPath});
        tree.setSelectionPath(selPath);
        Action a = nodeAction(Action.selectedNodes[0],ACTION_MONITORDEV);
        if(a!=null) a.execAction();

      }

    }

  }
  public void mouseClicked(MouseEvent e) {}
  public void mouseReleased(MouseEvent e) {}
  public void mouseEntered(MouseEvent e) {}
  public void mouseExited(MouseEvent e) {}

}
