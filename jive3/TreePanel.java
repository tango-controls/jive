package jive3;

import fr.esrf.TangoApi.*;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevVarLongStringArray;
import fr.esrf.tangoatk.widget.device.tree.DomainNode;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;

import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.TreeNode;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.io.File;
import java.util.Vector;

import jive.JiveUtils;
import jive.ExecDev;

/**
 * An abstract class for tree panel.
 */
public abstract class TreePanel extends JPanel implements TreeSelectionListener,MouseListener {

  protected JTree            tree;
  protected JScrollPane      treeView = null;
  protected DefaultTreeModel treeModel;
  protected TangoNode        root;
  protected Database         db;
  MainPanel                  invoker;
  TreePanel                  self;
  private   boolean          updateOnChange;

  // Static action menu
  public final static int ACTION_NUMBER       = 30;

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
  public final static int ACTION_CUT_INSTANCE   = 25;
  public final static int ACTION_PASTE_INSTANCE = 26;
  public final static int ACTION_LOG_VIEWER     = 27;
  public final static int ACTION_DEV_DEPEND     = 28;
  public final static int ACTION_THREAD_POLL    = 29;


  private static TangoNode[] selectedNodes = null;
  static         File       lastFile = null;
  private static JPopupMenu actionMenu;
  private static JMenuItem  copyMenu;
  private static JMenuItem  pasteMenu;
  private static JMenuItem  renameMenu;
  private static JMenuItem  deleteMenu;
  private static JMenuItem  addClassMenu;
  private static JMenuItem  testAdminMenu;
  private static JMenuItem  saveServerMenu;
  private static JMenuItem  classWizMenu;
  private static JMenuItem  addDeviceMenu;
  private static JMenuItem  devicesWizMenu;
  private static JMenuItem  monitorMenu;
  private static JMenuItem  testMenu;
  private static JMenuItem  aliasMenu;
  private static JMenuItem  goToDevMenu;
  private static JMenuItem  restartMenu;
  private static JMenuItem  deviceWizMenu;
  private static JMenuItem  goToServMenu;
  private static JMenuItem  goToAdminMenu;
  private static JMenuItem  addClassAttMenu;
  private static JMenuItem  unexportDevices;
  private static JMenuItem  selectPropNodeMenu;
  private static JMenuItem  selectPollingNodeMenu;
  private static JMenuItem  selectEventNodeMenu;
  private static JMenuItem  selectAttConfNodeMenu;
  private static JMenuItem  selectLoggingNodeMenu;
  private static JMenuItem  cutInstanceNodeMenu;
  private static JMenuItem  pasteInstanceNodeMenu;
  private static JMenuItem  logviewerMenu;
  private static JMenuItem  devDependMenu;
  private static JMenuItem  threadPollMenu;

  static {
    actionMenu = new JPopupMenu();

    copyMenu = new JMenuItem("Copy");
    actionMenu.add(copyMenu);
    copyMenu.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        selectedNodes[0].execAction(ACTION_COPY);
      }
    });
    pasteMenu = new JMenuItem("Paste");
    actionMenu.add(pasteMenu);
    pasteMenu.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        selectedNodes[0].execAction(ACTION_PASTE);
      }
    });
    renameMenu = new JMenuItem("Rename");
    actionMenu.add(renameMenu);
    renameMenu.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        selectedNodes[0].execAction(ACTION_RENAME);
      }
    });
    deleteMenu = new JMenuItem("Delete");
    actionMenu.add(deleteMenu);
    deleteMenu.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        selectedNodes[0].execAction(ACTION_DELETE);
      }
    });
    addClassMenu = new JMenuItem("Add class");
    actionMenu.add(addClassMenu);
    addClassMenu.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        selectedNodes[0].execAction(ACTION_ADDCLASS);
      }
    });
    testAdminMenu = new JMenuItem("Test admin server");
    actionMenu.add(testAdminMenu);
    testAdminMenu.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        selectedNodes[0].execAction(ACTION_TESTADMIN);
      }
    });
    saveServerMenu = new JMenuItem("Save server data");
    actionMenu.add(saveServerMenu);
    saveServerMenu.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        selectedNodes[0].execAction(ACTION_SAVESERVER);
      }
    });
    classWizMenu = new JMenuItem("Classes wizard");
    actionMenu.add(classWizMenu);
    classWizMenu.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        selectedNodes[0].execAction(ACTION_CLASSWIZ);
      }
    });
    addDeviceMenu = new JMenuItem("Add device");
    actionMenu.add(addDeviceMenu);
    addDeviceMenu.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        selectedNodes[0].execAction(ACTION_ADDDEVICE);
      }
    });
    devicesWizMenu = new JMenuItem("Devices wizard");
    actionMenu.add(devicesWizMenu);
    devicesWizMenu.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        selectedNodes[0].execAction(ACTION_DEVICESWIZ);
      }
    });
    monitorMenu = new JMenuItem("Monitor device");
    actionMenu.add(monitorMenu);
    monitorMenu.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        for(int i=0;i<selectedNodes.length;i++)
          selectedNodes[i].execAction(ACTION_MONITORDEV);
      }
    });
    testMenu = new JMenuItem("Test device");
    actionMenu.add(testMenu);
    testMenu.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        for(int i=0;i<selectedNodes.length;i++)
          selectedNodes[i].execAction(ACTION_TESTDEV);
      }
    });
    aliasMenu = new JMenuItem("Define device alias");
    actionMenu.add(aliasMenu);
    aliasMenu.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        selectedNodes[0].execAction(ACTION_DEFALIAS);
      }
    });
    goToDevMenu = new JMenuItem("Go to device node");
    actionMenu.add(goToDevMenu);
    goToDevMenu.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        selectedNodes[0].execAction(ACTION_GOTODEVNODE);
      }
    });
    restartMenu = new JMenuItem("Restart device");
    actionMenu.add(restartMenu);
    restartMenu.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        selectedNodes[0].execAction(ACTION_RESTART);
      }
    });
    deviceWizMenu = new JMenuItem("Device wizard");
    actionMenu.add(deviceWizMenu);
    deviceWizMenu.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        selectedNodes[0].execAction(ACTION_DEVICEWIZ);
      }
    });
    goToServMenu = new JMenuItem("Go to server node");
    actionMenu.add(goToServMenu);
    goToServMenu.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        selectedNodes[0].execAction(ACTION_GOTOSERVNODE);
      }
    });
    goToAdminMenu = new JMenuItem("Go to device admin node");
    actionMenu.add(goToAdminMenu);
    goToAdminMenu.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        selectedNodes[0].execAction(ACTION_GOTOADMINNODE);
      }
    });
    addClassAttMenu = new JMenuItem("Add attribute");
    actionMenu.add(addClassAttMenu);
    addClassAttMenu.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        selectedNodes[0].execAction(ACTION_ADDCLASSATT);
      }
    });
    unexportDevices = new JMenuItem("Unexport devices");
    actionMenu.add(unexportDevices);
    unexportDevices.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        selectedNodes[0].execAction(ACTION_UNEXPORT);
      }
    });
    selectPropNodeMenu = new JMenuItem("Select 'property' nodes");
    actionMenu.add(selectPropNodeMenu);
    selectPropNodeMenu.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        selectedNodes[0].execAction(ACTION_SELECT_PROP);
      }
    });
    selectPollingNodeMenu = new JMenuItem("Select 'polling' nodes");
    actionMenu.add(selectPollingNodeMenu);
    selectPollingNodeMenu.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        selectedNodes[0].execAction(ACTION_SELECT_POLLING);
      }
    });
    selectEventNodeMenu = new JMenuItem("Select 'event' nodes");
    actionMenu.add(selectEventNodeMenu);
    selectEventNodeMenu.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        selectedNodes[0].execAction(ACTION_SELECT_EVENT);
      }
    });
    selectAttConfNodeMenu = new JMenuItem("Select 'attribute config' nodes");
    actionMenu.add(selectAttConfNodeMenu);
    selectAttConfNodeMenu.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        selectedNodes[0].execAction(ACTION_SELECT_ATTCONF);
      }
    });
    selectLoggingNodeMenu = new JMenuItem("Select 'logging' nodes");
    actionMenu.add(selectLoggingNodeMenu);
    selectLoggingNodeMenu.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        selectedNodes[0].execAction(ACTION_SELECT_LOGGING);
      }
    });
    cutInstanceNodeMenu = new JMenuItem("Cut instance");
    actionMenu.add(cutInstanceNodeMenu);
    cutInstanceNodeMenu.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        selectedNodes[0].execAction(ACTION_CUT_INSTANCE);
      }
    });
    pasteInstanceNodeMenu = new JMenuItem("Paste instance");
    actionMenu.add(pasteInstanceNodeMenu);
    pasteInstanceNodeMenu.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        selectedNodes[0].execAction(ACTION_PASTE_INSTANCE);
      }
    });
    logviewerMenu = new JMenuItem("Log Viewer");
    actionMenu.add(logviewerMenu);
    logviewerMenu.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        selectedNodes[0].execAction(ACTION_LOG_VIEWER);
      }
    });
    devDependMenu = new JMenuItem("Devices dependencies");
    actionMenu.add(devDependMenu);
    devDependMenu.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        selectedNodes[0].execAction(ACTION_DEV_DEPEND);
      }
    });
    threadPollMenu = new JMenuItem("Polling threads manager");
    actionMenu.add(threadPollMenu);
    threadPollMenu.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        selectedNodes[0].execAction(ACTION_THREAD_POLL);
      }
    });

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
    tree.setToggleClickCount(0);
    treeView = new JScrollPane(tree);
    add(treeView, BorderLayout.CENTER);
    updateOnChange = true;

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

    if (oldPath != null) {

      // Reselect old node
      TreePath newPath = new TreePath(root);
      TangoNode node = root;
      boolean found = true;
      int i = 1;
      while (found && i < oldPath.getPathCount()) {

        String item = oldPath.getPathComponent(i).toString();

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

      result = "- Device Info ----------------------------------------\n\n";

      result += ds.get_info().toString();

      // Append Polling status
      result += "\n\n- Polling Status -------------------------------------\n\n";
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
  private void createSelectedNodes(int desriedLength) {

    if(selectedNodes==null) {
      selectedNodes = new TangoNode[desriedLength];
    } else {
      if( selectedNodes.length!=desriedLength ) {
        selectedNodes = new TangoNode[desriedLength];
      }
    }

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
  public void mousePressed(MouseEvent e) {

    TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
    if (selPath != null) {

      if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3) {

        // Multiple selection

        if( !tree.isSelectionEmpty() && (e.isControlDown() || e.isShiftDown()) ) {

          // Check that the node is not already selected
          // If not, add it to the path
          if( !tree.isPathSelected(selPath) )
            tree.addSelectionPath(selPath);

          // Check that if Test Device and Monitor device are available
          // for all selected nodes
          TreePath[] paths = tree.getSelectionPaths();
          if (paths.length>1) {

            boolean ok = true;
            int i = 0;
            createSelectedNodes(paths.length);
            while (ok && i < paths.length) {
              selectedNodes[i] = (TangoNode) paths[i].getLastPathComponent();
              int[] actions = selectedNodes[i].getAction();
              ok = (JiveUtils.contains(actions, ACTION_MONITORDEV) ||
                      JiveUtils.contains(actions, ACTION_TESTDEV));
              i++;
            }
            if (ok) {
              // Popup ACTION_MONITORDEV + ACTION_TESTDEV menu
              for (i = 0; i < ACTION_NUMBER; i++)
                  actionMenu.getComponent(i).setVisible(false);
              actionMenu.getComponent(ACTION_MONITORDEV).setVisible(true);
              actionMenu.getComponent(ACTION_TESTDEV).setVisible(true);
              actionMenu.show(tree, e.getX(), e.getY());
              return;
            }

          }

        }

        // Single selection only
        createSelectedNodes(1);
        tree.setSelectionPath(selPath);
        selectedNodes[0] = (TangoNode)selPath.getLastPathComponent();
        int[] actions = selectedNodes[0].getAction();
        if (actions.length > 0) {

          for (int i = 0; i < ACTION_NUMBER; i++) {
            if (JiveUtils.contains(actions, i))
              actionMenu.getComponent(i).setVisible(true);
            else
              actionMenu.getComponent(i).setVisible(false);
          }
          actionMenu.show(tree, e.getX(), e.getY());

        }

      }

      // Launch ATK panel on double click (when possible)
      if(e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1 ) {

        // Force single selection on right click
        createSelectedNodes(1);
        tree.setSelectionPath(selPath);
        selectedNodes[0] = (TangoNode)selPath.getLastPathComponent();
        int[] actions = selectedNodes[0].getAction();
        if (actions.length > 0) {
          if (JiveUtils.contains(actions, ACTION_MONITORDEV)) {
            selectedNodes[0].execAction(ACTION_MONITORDEV);
          }
        }

      }

    }

  }
  public void mouseClicked(MouseEvent e) {}
  public void mouseReleased(MouseEvent e) {}
  public void mouseEntered(MouseEvent e) {}
  public void mouseExited(MouseEvent e) {}

}
