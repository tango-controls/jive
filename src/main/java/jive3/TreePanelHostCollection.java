package jive3;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.Tango.DevVarLongStringArray;
import fr.esrf.TangoApi.*;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import jive.DevWizard;
import jive.JiveUtils;
import jive.ServerDlg;
import jive.ThreadDlg;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * A panel for selecting host coolection (Astor display)
 */
public class TreePanelHostCollection extends TreePanel {

  public static int NB_LEVELS = 5;

  public TreePanelHostCollection(MainPanel parent) {

    this.invoker = parent;
    this.self = this;
    setLayout(new BorderLayout());

  }

  public TangoNode createRoot() {
    return new RootNode();
  }

  String getLevelStr(int level) {
    if(level==0)
      return "Not Controlled";
    else
      return "Level " + Integer.toString(level);
  }


  DevState[] getServerState(DeviceProxy starter,String[] serverNames) throws DevFailed {

    DevState[] retStates = new DevState[serverNames.length];
    for(int i=0;i<serverNames.length;i++)
      retStates[i] = DevState.UNKNOWN;

    DeviceAttribute att = starter.read_attribute("Servers");
    String[] list = att.extractStringArray();

    for (String line : list) {
      StringTokenizer stk = new StringTokenizer(line);
      String name = stk.nextToken();
      String st = stk.nextToken();
      //String str_ctrl = stk.nextToken();
      int idx = JiveUtils.isInsideArray(name,serverNames);
      if (idx>=0) {
        if(st.equalsIgnoreCase("FAULT")) {
          retStates[idx] = DevState.OFF;
        } else {
          retStates[idx] = DevState.ON;
        }
      }
    }

    return retStates;

  }

  String getHostStateString(DevState state) {

    switch(state.value()) {
      case DevState._ON:
        return "<font color=\"green\">All servers are running</font>";
      case DevState._OFF:
        return "<font color=\"#A0A0A0\">All servers are stopped</font>";
      case DevState._ALARM:
        return "<font color=\"orange\">At least one of the servers is stopped</font>";
      case DevState._MOVING:
        return "<font color=\"blue\">At least one of the servers is not responding</font>";
      case DevState._FAULT:
        return "<font color=\"red\">FAULT</font>";
      default:
        return "<font color=\"red\">Not responding</font>";
    }

  }

  String getServerStateString(DevState state) {

    if(state==DevState.OFF)
      return "<font color=\"red\">Stopped</font>";
    else if(state==DevState.ON)
      return "<font color=\"green\">Running</font>";
    else
      return "<font color=\"grey\">Unknown</font>";

  }

  boolean allServersOff(DevState[] allStates) {

    boolean off = true;
    int i=0;
    while(off && i<allStates.length) {
      off = allStates[i]==DevState.OFF || allStates[i]==DevState.UNKNOWN;
      i++;
    }
    return off;

  }

  public void stopServer(DeviceProxy starter,String serverName) throws DevFailed {
    stopServers(starter,new String[]{serverName});
  }

  public void stopServers(DeviceProxy starter,String[] serverNames) throws DevFailed {

    DevState[] srvState = getServerState(starter,serverNames);

    // Stop servers
    for(int i=0;i<serverNames.length;i++) {
      if( srvState[i]==DevState.ON ) {
        // Stop it
        DeviceData argin = new DeviceData();
        argin.insert(serverNames[i]);
        starter.command_inout("DevStop", argin);
      }
    }

    if( !allServersOff(srvState) ) {

      // Wait for completion
      int nbTry = 5;
      while( !allServersOff(srvState) && nbTry>0 ) {

        JiveUtils.sleep(1000);
        srvState = getServerState(starter,serverNames);
        nbTry--;
        if( !allServersOff(srvState) && nbTry==0 ) {
          int ok = JOptionPane.showConfirmDialog(null,"Some servers are still running.\nSend Hard kill ?","Question",JOptionPane.YES_NO_OPTION);
          if( ok==JOptionPane.YES_OPTION ) {
            for(int i=0;i<serverNames.length;i++) {
              if( srvState[i]==DevState.ON ) {
                DeviceData argin = new DeviceData();
                argin.insert(serverNames[i]);
                starter.command_inout("HardKillServer",argin);
              }
            }
            nbTry=1;

          }
        }
      }

    }


  }

  public void startServer(DeviceProxy starter,String serverName) throws DevFailed {

    startServers(starter, new String[]{serverName});

  }

  public void startServers(DeviceProxy starter,String[] serverNames) throws DevFailed {

    for(int i=0;i<serverNames.length;i++) {
      DeviceData argin = new DeviceData();
      argin.insert(serverNames[i]);
      starter.command_inout("DevStart",argin);
    }

  }

  public TangoNode selectHost(String collection,String host) {

    // Search server
    TangoNode colNode = searchNode(root,collection);
    if(colNode==null) return null;
    TangoNode hostNode = searchNode(colNode,host);
    if(hostNode==null) return null;

    TreePath selPath = new TreePath(root);
    selPath = selPath.pathByAddingChild(colNode);
    selPath = selPath.pathByAddingChild(hostNode);
    tree.setSelectionPath(selPath);

    return hostNode;

  }

  public TangoNode selectServer(String collection,String host,int level,String server) {

    // Search server
    TangoNode colNode = searchNode(root,collection);
    if(colNode==null) return null;
    TangoNode hostNode = searchNode(colNode,host);
    if(hostNode==null) return null;
    TangoNode lvNode = searchNode(hostNode,getLevelStr(level));
    if(lvNode==null) return null;
    TangoNode srvNode = searchNode(lvNode,server);
    if(srvNode==null) return null;

    TreePath selPath = new TreePath(root);
    selPath = selPath.pathByAddingChild(colNode);
    selPath = selPath.pathByAddingChild(hostNode);
    selPath = selPath.pathByAddingChild(lvNode);
    selPath = selPath.pathByAddingChild(srvNode);
    tree.setSelectionPath(selPath);

    return srvNode;

  }

  public TangoNode selectClass(String collection,String host,int level,String server,String className) {

    // Search server
    TangoNode colNode = searchNode(root,collection);
    if(colNode==null) return null;
    TangoNode hostNode = searchNode(colNode,host);
    if(hostNode==null) return null;
    TangoNode lvNode = searchNode(hostNode,getLevelStr(level));
    if(lvNode==null) return null;
    TangoNode srvNode = searchNode(lvNode,server);
    if(srvNode==null) return null;
    TangoNode classNode = searchNode(srvNode,className);
    if(classNode==null) return null;

    TreePath selPath = new TreePath(root);
    selPath = selPath.pathByAddingChild(colNode);
    selPath = selPath.pathByAddingChild(hostNode);
    selPath = selPath.pathByAddingChild(lvNode);
    selPath = selPath.pathByAddingChild(srvNode);
    selPath = selPath.pathByAddingChild(classNode);
    tree.setSelectionPath(selPath);

    return classNode;
  }

  public TangoNode selectDevice(String collection,String host,int level,String server,String className,String devName) {

    // Search server
    TangoNode colNode = searchNode(root,collection);
    if(colNode==null) return null;
    TangoNode hostNode = searchNode(colNode,host);
    if(hostNode==null) return null;
    TangoNode lvNode = searchNode(hostNode,getLevelStr(level));
    if(lvNode==null) return null;
    TangoNode srvNode = searchNode(lvNode,server);
    if(srvNode==null) return null;
    TangoNode classNode = searchNode(srvNode,className);
    if(classNode==null) return null;
    TangoNode devNode = searchNode(classNode,devName);
    if(devNode==null) return null;

    TreePath selPath = new TreePath(root);
    selPath = selPath.pathByAddingChild(colNode);
    selPath = selPath.pathByAddingChild(hostNode);
    selPath = selPath.pathByAddingChild(lvNode);
    selPath = selPath.pathByAddingChild(srvNode);
    selPath = selPath.pathByAddingChild(classNode);
    selPath = selPath.pathByAddingChild(devNode);
    tree.setSelectionPath(selPath);

    return devNode;
  }

  public void treeExpanded(TreeExpansionEvent event) {

    TangoNode expanded = (TangoNode)event.getPath().getLastPathComponent();
    if(expanded instanceof HostNode) {
      // Expand level node when HostNode is expanded
      for(int i=0;i<expanded.getChildCount();i++) {
        TangoNode nd = (TangoNode)expanded.getChildAt(i);
        tree.expandPath(nd.getCompletePath());
      }
    }

  }

// ---------------------------------------------------------------

  class RootNode extends TangoNode {

    void populateNode() throws DevFailed {

      // Get collection list
      DeviceData argin = new DeviceData();
      String request = "select distinct value from property_device where device like 'tango/admin/%' and name='HostCollection'";
      argin.insert(request);
      DeviceData argout = db.command_inout("DbMySqlSelect", argin);
      DevVarLongStringArray arg = argout.extractLongStringArray();

      // Get last collection list
      ArrayList<String> collection = new ArrayList<String>();
      ArrayList<String> lastCollection = new ArrayList<String>();
      String lastCols[] = new String[0];

      DbDatum datum = db.get_property("Astor", "LastCollections");
      if (!datum.is_empty())
        lastCols = datum.extractStringArray();

      for (int i = 0; i < arg.svalue.length; i++) {
        if (JiveUtils.isInsideArray(arg.svalue[i], lastCols) < 0)
          collection.add(arg.svalue[i]);
        else
          lastCollection.add(arg.svalue[i]);
      }

      Collections.sort(collection);
      Collections.sort(lastCollection);
      collection.addAll(lastCollection);

      for (int i = 0; i < collection.size(); i++)
        add(new CollectionNode(collection.get(i)));

    }

    public String toString() {
      return "Collection:";
    }

  }

// ---------------------------------------------------------------

  class CollectionNode extends TangoNode {

    private String collection;

    CollectionNode(String collection) {
      this.collection = collection;
    }

    void populateNode() throws DevFailed {

      // Get host collection list
      DeviceData argin = new DeviceData();
      String request = "select distinct device from property_device where device like 'tango/admin/%' and name='HostCollection' and value='" + collection + "'";
      argin.insert(request);
      DeviceData argout = db.command_inout("DbMySqlSelect", argin);
      DevVarLongStringArray arg = argout.extractLongStringArray();

      for (int i = 0; i < arg.svalue.length; i++) {
        String s = arg.svalue[i];
        add(new HostNode(collection,s.substring(s.lastIndexOf('/') + 1)));
      }

    }

    public String getValue() {

      StringBuffer retStr = new StringBuffer();
      int count = getChildCount();
      for(int i=0;i<count;i++) {
        HostNode n = (HostNode)getChildAt(i);
        retStr.append(n.getState());
        retStr.append("\n");
      }
      return retStr.toString();

    }

    public String toString() {
      return collection;
    }

    public String getTitle() {
      return collection;
    }

    ImageIcon getIcon() {
      return TangoNodeRenderer.hostcollectionicon;
    }

    int[] getAction() {
      return new int[0];
    }

  }

// ---------------------------------------------------------------

  class HostNode extends TangoNode {

    private String collection;
    private String host;
    private String hostUsage = "";
    private DeviceProxy starter = null;
    private String hostInfo = "";
    private String[] allHostServers;
    private String starterName;

    HostNode(String collection,String host) {

      this.host = host;
      this.collection = collection;
      this.allHostServers = new String[0];

      // Connect to the starter
      starterName = "tango/admin/" + host;
      try {
        starter = new DeviceProxy(starterName);
        hostUsage = db.get_device_property(starterName,"HostUsage").extractString();
      } catch (DevFailed e) {
        JiveUtils.showTangoError(e);
      }

      // Get info from hostInfo
      try {
        DeviceProxy hI = new DeviceProxy("host/info/" + host);
        String arch = hI.read_attribute("kernelVersion").extractString();
        String ht = hI.read_attribute("hostType").extractString();
        String os = hI.read_attribute("osDistribution").extractString();
        hostInfo = ht + " " + os + " (" + arch + ")";
      } catch (DevFailed e) {
      }

    }

    void populateNode() throws DevFailed {

      // Get level list
      DeviceData argin = new DeviceData();
      argin.insert(host);
      DeviceData argout = db.command_inout("DbGetHostServersInfo", argin);
      String[] arg = argout.extractStringArray();

      DbDatum da = db.get_class_property("Starter","NbStartupLevels");
      if(!da.is_empty()) NB_LEVELS = da.extractLong();

      Integer[] allLevels = new Integer[NB_LEVELS+1];
      for(int i=1;i<=NB_LEVELS;i++)
        allLevels[i-1] = i;
      allLevels[NB_LEVELS] = 0;

      ArrayList<String> allHS = new ArrayList<String>();

      for(int level:allLevels) {
        ArrayList<String> servers = new ArrayList<String>();
        for(int i=0;i<arg.length;i+=3) {
          int l = 0;
          try {
            l = Integer.parseInt(arg[i+2]);
          } catch(NumberFormatException e) {}
          if( l==level && !arg[i].toLowerCase().contains("starter")) servers.add(arg[i]);
          if( l>0 && !arg[i].toLowerCase().contains("starter")) allHS.add(arg[i]);
        }
        if(servers.size()>0)
          add(new LevelNode(collection,host,level,servers));
      }

      allHostServers = new String[allHS.size()];
      for(int i=0;i<allHS.size();i++)
        allHostServers[i] = allHS.get(i);

    }

    String getState() {

      try {
        DeviceData dd = starter.command_inout("State");
        return host + ": " + getHostStateString(dd.extractDevState());
      } catch (DevFailed e) {
        return host + ": " + getHostStateString(DevState.UNKNOWN);
      }

    }

    public String getValue() {

      StringBuffer result = new StringBuffer();

      if(hostInfo.length()>0) {
        result.append("<b>");
        result.append(hostInfo);
        result.append("</b>\n\n");
      }

      int count = getChildCount();
      for(int i=0;i<count;i++) {
        LevelNode l = (LevelNode)getChildAt(i);
        result.append("<b>"+l.toString()+"</b>\n");
        result.append("<hr>\n");
        result.append(l.getValue(true));
        result.append("\n");
      }

      return result.toString();

    }

    public String getDisplayValue() {
      if(hostUsage.length()>0)
        return toString() + " (" + hostUsage + ")";
      else
        return toString();
    }

    public String toString() {
      return host;
    }

    ImageIcon getIcon() {
      return TangoNodeRenderer.hostsmallicon;
    }

    int[] getAction() {
      return new int[] {
          ACTION_START_HOST,
          ACTION_STOP_HOST,
          ACTION_CH_HOST_USAGE,
          ACTION_GO_TO_STATER,
          ACTION_NEW_SERVERS,
          ACTION_TERMINAL
      };
    }

    void execAction(int actionNumber) {

      int ok;

      switch (actionNumber) {
        // ----------------------------------------------------------------------------
        case ACTION_START_HOST:
          ok = JOptionPane.showConfirmDialog(invoker, "Start all servers on " + host + " ?", "Confirmation", JOptionPane.YES_NO_OPTION);
          if (ok == JOptionPane.YES_OPTION) {
            try {
              startServers(starter, allHostServers);
            } catch (DevFailed e) {
              JiveUtils.showTangoError(e);
            }
          }
          break;

        // ----------------------------------------------------------------------------
        case ACTION_STOP_HOST:
          ok = JOptionPane.showConfirmDialog(invoker, "Stop all servers on " + host + "?", "Confirmation", JOptionPane.YES_NO_OPTION);
          if (ok == JOptionPane.YES_OPTION) {
            try {
              stopServers(starter, allHostServers);
              refreshValues();
            } catch (DevFailed e) {
              JiveUtils.showTangoError(e);
            }
          }
          break;

        // ----------------------------------------------------------------------------
        case ACTION_CH_HOST_USAGE:

          String newUsage = JOptionPane.showInputDialog(invoker,"Host Usage",hostUsage);
          if (newUsage != null) {
            try {
              DbDatum datum = new DbDatum("HostUsage",newUsage);
              db.put_device_property(starterName,new DbDatum[]{datum});
              refresh();
            } catch (DevFailed e) {
              JiveUtils.showTangoError(e);
            }
          }
          break;

        // ----------------------------------------------------------------------------
        case ACTION_GO_TO_STATER:
          invoker.goToServerNode("Starter/"+host);
          break;

        // ----------------------------------------------------------------------------
        case ACTION_NEW_SERVERS:
          StartServerDlg dlg = new StartServerDlg(db,host,starter,invoker);
          try {
            dlg.setServerList(db.get_server_list());
            ATKGraphicsUtils.centerFrameOnScreen(dlg);
            dlg.setVisible(true);
          } catch (DevFailed e) {
            JiveUtils.showTangoError(e);
          }
          break;

        // ----------------------------------------------------------------------------
        case ACTION_TERMINAL:
          JSSHTerminal.MainPanel terminal;
          String defaultUser = null;
          String defaultPassword = null;
          try {
            DbDatum dd = db.get_property("Astor","RloginUser");
            if(!dd.is_empty())
              defaultUser = dd.extractString();
            dd = db.get_property("Astor","RloginPassword");
            if(!dd.is_empty())
              defaultPassword = dd.extractString();
          } catch (DevFailed e) {}

          if(defaultUser!=null) {
            terminal = new JSSHTerminal.MainPanel(host,defaultUser,defaultPassword,80,24,500);
            terminal.setX11Forwarding(true);
            terminal.setExitOnClose(false);
            ATKGraphicsUtils.centerFrameOnScreen(terminal);
            terminal.setVisible(true);
          } else {
            JiveUtils.showJiveError("No username !\nAStor/RloginUser free property not defined.");
          }
          break;

      }

    }

  }

  // ---------------------------------------------------------------

  class LevelNode extends TangoNode {

    private String   collection;
    private String   host;
    private int      level;
    private String[] allServers;

    LevelNode(String collection,String host,int level,ArrayList<String> servers) {

      this.host = host;
      this.collection = collection;
      this.level = level;
      allServers = new String[servers.size()];

      for(int i=0;i<servers.size();i++) {
        add(new ServerNode(collection,host,level,servers.get(i)));
        allServers[i] = servers.get(i);
      }

    }

    void populateNode() throws DevFailed {
    }

    DeviceProxy getStarter() {
      return ((HostNode)getParent()).starter;
    }

    public String toString() {
      return getLevelStr(level);
    }

    public String getTitle() {
      return host + " - " + toString();
    }

    String getValue(boolean tab) {

      String bStr = "";
      if(tab) bStr = "   ";

      String result = "";
      if(getStarter()==null)
        return result;

      try {

        DevState[] allStates = getServerState(getStarter(),allServers);
        for(int i=0;i<allServers.length;i++)
          result += bStr + allServers[i] + ": " + getServerStateString(allStates[i]) + "\n";

      } catch (DevFailed e) {

        for (int i = 0; i < e.errors.length; i++) {
          result += bStr + "Desc -> " + e.errors[i].desc + "\n";
          result += bStr + "Reason -> " + e.errors[i].reason + "\n";
          result += bStr + "Origin -> " + e.errors[i].origin + "\n";
        }

      }

      return result;

    }

    String getValue() {
      return getValue(false);
    }

    ImageIcon getIcon() {
      return TangoNodeRenderer.levelicon;
    }

    int[] getAction() {
      return new int[] {
          ACTION_START_LEVEL,
          ACTION_STOP_LEVEL
      };
    }

    void execAction(int actionNumber) {

      int ok;

      switch (actionNumber) {
        // ----------------------------------------------------------------------------
        case ACTION_START_LEVEL:
          ok = JOptionPane.showConfirmDialog(invoker, "Start level " + level + " on " + host + " ?", "Confirmation", JOptionPane.YES_NO_OPTION);
          if (ok == JOptionPane.YES_OPTION) {
            try {
              startServers(getStarter(), allServers);
            } catch (DevFailed e) {
              JiveUtils.showTangoError(e);
            }
          }
          break;

        // ----------------------------------------------------------------------------
        case ACTION_STOP_LEVEL:
          ok = JOptionPane.showConfirmDialog(invoker, "Stop level " + level + " on " + host + "?", "Confirmation", JOptionPane.YES_NO_OPTION);
          if (ok == JOptionPane.YES_OPTION) {
              try {
                stopServers(getStarter(), allServers);
                refreshValues();
              } catch (DevFailed e) {
                JiveUtils.showTangoError(e);
              }
          }

          break;
      }

    }


  }

  // ---------------------------------------------------------------

  class ServerNode extends TangoNode implements IServerAction {

    private String collection;
    private String host;
    private int    level;
    private String server;

    ServerNode(String collection,String host,int level,String server) {
      this.server = server;
      this.host = host;
      this.level = level;
      this.collection = collection;
    }

    DeviceProxy getStarter() {
      return ((LevelNode)getParent()).getStarter();
    }

    void populateNode() throws DevFailed {

      String[] srvList = null;
      String[] dbList = null;
      int i;

      try {
        // Try to get class list through the admin device
        String admName = "dserver/" + server;
        DeviceProxy adm = new DeviceProxy(admName);
        DeviceData datum = adm.command_inout("QueryClass");
        srvList = datum.extractStringArray();
      } catch (DevFailed e) {}

      // Get the list from the database
      dbList = db.get_server_class_list(server);

      if(srvList!=null) {

        // Add actual class
        for (i = 0; i < srvList.length; i++)
          add(new ClassNode(collection,host,level,server,srvList[i]));

        // No add other class found in database as invalid
        for (i = 0; i < dbList.length; i++) {
          if(!JiveUtils.contains(srvList,dbList[i])) {
            add(new ClassNode(collection,host,level,server,dbList[i]));
          }
        }

      } else {

        // Old fashion
        for (i = 0; i < dbList.length; i++)
          add(new ClassNode(collection,host,level,server,dbList[i]));

      }

    }

    String getTitle() {
      return "Server Info";
    }

    public String toString() {
      return server;
    }

    ImageIcon getIcon() {
      return TangoNodeRenderer.srvicon;
    }

    DevState getState() throws DevFailed {
      return getServerState(getStarter(),new String[]{server})[0];
    }

    String getValue() {

      String result = "";
      if(getStarter()==null)
        return result;

      try {

        DevState serverState = getState();
        result = server + ": " + getServerStateString(serverState) + "\n";

        result += "-- Log ------------------------------\n";
        DeviceData argin = new DeviceData();
        argin.insert(server);
        DeviceData argout = getStarter().command_inout("DevReadLog",argin);
        result += argout.extractString();

      } catch (DevFailed e) {

        for (int i = 0; i < e.errors.length; i++) {
          result += "Desc -> " + e.errors[i].desc + "\n";
          result += "Reason -> " + e.errors[i].reason + "\n";
          result += "Origin -> " + e.errors[i].origin + "\n";
        }

      }

      return result;

    }

    int[] getAction() {
      if (JiveUtils.readOnly)
        return new int[0];
      else
        return new int[]{
            ACTION_DELETE,
            ACTION_ADDCLASS,
            ACTION_TESTADMIN,
            ACTION_SAVESERVER,
            ACTION_CLASSWIZ,
            ACTION_UNEXPORT,
            ACTION_DEV_DEPEND,
            ACTION_THREAD_POLL,
            ACTION_MOVE_SERVER,
            ACTION_RESTART_SERVER,
            ACTION_START_SERVER,
            ACTION_STOP_SERVER,
            ACTION_CH_LEVEL,
        };

    }

    void execAction(int actionNumber) {

      int ok;

      switch(actionNumber) {

        // ----------------------------------------------------------------------------
        case ACTION_DELETE:

          ok = JOptionPane.showConfirmDialog(invoker, "Delete server " + server + " ?", "Confirm delete", JOptionPane.YES_NO_OPTION);
          if (ok == JOptionPane.YES_OPTION) {
            try {
              db.delete_server(server);
            } catch (DevFailed e) {
              JiveUtils.showTangoError(e);
            }
            refresh();
            selectHost(collection,host);
          }
          break;

        // ----------------------------------------------------------------------------
        case ACTION_ADDCLASS:

          ServerDlg sdlg = new ServerDlg(this);
          sdlg.setClassList(invoker.getClassTreePanel().getClassList());
          sdlg.setValidFields(false, true);
          sdlg.setDefaults(server, "");
          ATKGraphicsUtils.centerFrame(invoker.innerPanel, sdlg);
          sdlg.setVisible(true);
          break;

        // ----------------------------------------------------------------------------
        case ACTION_TESTADMIN:
          testDevice("dserver/" + server);
          break;

        // ----------------------------------------------------------------------------
        case ACTION_SAVESERVER:

          FileWriter resFile;

          JFileChooser chooser = new JFileChooser(".");
          ok = JOptionPane.YES_OPTION;
          if (lastFile != null)
            chooser.setSelectedFile(lastFile);

          int returnVal = chooser.showSaveDialog(invoker);

          if (returnVal == JFileChooser.APPROVE_OPTION) {
            lastFile = chooser.getSelectedFile();
            if (lastFile != null) {
              if (lastFile.exists()) ok = JOptionPane.showConfirmDialog(invoker, "Do you want to overwrite " + lastFile.getName() + " ?", "Confirm overwrite", JOptionPane.YES_NO_OPTION);
              if (ok == JOptionPane.YES_OPTION) {
                try {
                  resFile = new FileWriter(lastFile.getAbsolutePath());
                  Date date = new Date(System.currentTimeMillis());
                  resFile.write("#\n# Resource backup , created " + date + "\n#\n\n");
                  saveServerData(resFile,server);
                  resFile.close();
                } catch (IOException e) {
                  JiveUtils.showJiveError("Failed to create resource file !\n" + e.getMessage());
                }
              }
            }
          }

          break;

        // ----------------------------------------------------------------------------
        case TreePanel.ACTION_CLASSWIZ:

          DevWizard cwdlg = new DevWizard(invoker);
          cwdlg.showClassesWizard(server);
          refresh();
          break;

        // ----------------------------------------------------------------------------
        case TreePanel.ACTION_UNEXPORT:

          ok = JOptionPane.showConfirmDialog(invoker, "This will unexport all devices of " + server + "\n Do you want to continue ?", "Confirm unexport device", JOptionPane.YES_NO_OPTION);
          if (ok == JOptionPane.YES_OPTION) {
            try {
              //System.out.println(" Unexport device of" + srvName);
              db.unexport_server(server);
            } catch (DevFailed e) {
              JiveUtils.showTangoError(e);
            }
          }

          break;

        // ----------------------------------------------------------------------------
        case ACTION_DEV_DEPEND:
          launchDevDepend(server);
          break;

        // ----------------------------------------------------------------------------
        case ACTION_THREAD_POLL:
          launchPollingThreadsManager(server);
          break;

        // ----------------------------------------------------------------------------
        case ACTION_MOVE_SERVER:

          // Rename a server
          String newSName = JOptionPane.showInputDialog(null,"Rename server",server);
          if(newSName==null) return;
          try {
            db.rename_server(server,newSName);
            refresh();
            selectServer(collection, host, level, newSName);
          } catch (DevFailed e) {
            JiveUtils.showTangoError(e);
          }
          break;

        // ----------------------------------------------------------------------------
        case ACTION_RESTART_SERVER:
          // Restart a controlled server
          try {
            if(getStarter()!=null) {
              stopServer(getStarter(), server);
              startServer(getStarter(), server);
            }
          } catch (DevFailed e) {
            JiveUtils.showTangoError(e);
          }

          break;

        // ----------------------------------------------------------------------------
        case ACTION_START_SERVER:
          // Start a controlled server
          try {
            if(getStarter()!=null)
              startServer(getStarter(), server);
          } catch (DevFailed e) {
            JiveUtils.showTangoError(e);
          }

          break;

        // ----------------------------------------------------------------------------
        case ACTION_STOP_SERVER:
          // Stop a controlled server
          try {
            if(getStarter()!=null)
              stopServer(getStarter(), server);
          } catch (DevFailed e) {
            JiveUtils.showTangoError(e);
          }
          refreshValues();

          break;

        // ----------------------------------------------------------------------------
        case ACTION_CH_LEVEL:

          DbServer dbs;
          DbServInfo info = null;

          try {
            dbs = new DbServer(server);
            info = dbs.get_info();
          } catch (DevFailed e) {
            JiveUtils.showTangoError(e);
            return;
          }

          ServerInfoDlg dlg = new ServerInfoDlg(invoker);
          if (dlg.showDialog(info) == ServerInfoDlg.RET_OK) {

            try {
              info = dlg.getSelection();
              if (info != null) {

                info.host = host;
                if (info.startup_level == 0)
                  info.controlled = false;
                dbs.put_info(info);

                refresh();
                selectServer(collection, host, info.startup_level, server);

              } else {

                //	Check if Server is stopped
                if (getState() == DevState.ON) {
                  JiveUtils.showJiveError("Stop " + server + "  Server before !");
                  return;
                }

                //	Remove Server info in database
                dbs.put_info(new DbServInfo(server, host, false, 0));

                //	Register devices on empty host and un export.
                String[] deviceName = dbs.get_device_class_list();
                for (int i = 0; i < deviceName.length; i += 2) {
                  db.export_device(new DbDevExportInfo(deviceName[i], "", "", ""));
                  db.unexport_device(deviceName[i]);
                }
                  
                refresh();
                selectHost(collection, host);

              }


            } catch (DevFailed e) {
              JiveUtils.showTangoError(e);
            }

          }

          break;

      }

    }

    // IServerAction (Call by ServerDlg)
    public void doJob(String server, String classname, String[] devices) {

      // Add devices
      try {
        for (int i = 0; i < devices.length; i++)
          db.add_device(devices[i], classname, server);
      } catch (DevFailed e) {
        JiveUtils.showTangoError(e);
      }

      refresh();
      selectClass(collection,host,level,server,classname);

    }


  }

  // ---------------------------------------------------------------

  class ClassNode extends TangoNode implements IServerAction {

    private String collection;
    private String host;
    private int    level;
    private String server;
    private String className;
    private String[] devList = new String[0];

    ClassNode(String collection,String host,int level,String server,String className) {
      this.collection = collection;
      this.host = host;
      this.level = level;
      this.server = server;
      this.className = className;
      try {
        devList = db.get_device_name(server , className);
      } catch (DevFailed e) {}
    }

    void populateNode() throws DevFailed {
      for (int i = 0; i < devList.length; i++)
        add(new DeviceServerNode(collection,host,level,server,className,devList[i]));
    }

    public boolean isLeaf() {
      return devList.length == 0;
    }

    ImageIcon getIcon() {
      if(devList.length==0)
        return TangoNodeRenderer.uclassicon;
      else
        return TangoNodeRenderer.classicon;
    }

    public String toString() {
      return className;
    }

    int[] getAction() {
      if(JiveUtils.readOnly)
        return new int[0];
      else
        return new int[]{ACTION_RENAME,
            ACTION_DELETE,
            ACTION_ADDDEVICE,
            ACTION_DEVICESWIZ
        };
    }

    void execAction(int actionNumber) {

      switch(actionNumber) {

        // ----------------------------------------------------------------------------
        case ACTION_RENAME:

          // Rename a class
          String newName = JOptionPane.showInputDialog(null,"Rename class",className);
          if(newName==null) return;
          if(searchNode((TangoNode)getParent(),newName)!=null) {
            JiveUtils.showJiveError("Name already exists.");
            return;
          }

          for(int i=0;i<getChildCount();i++) {
            // Devices
            TangoNode n0 = (TangoNode)getChildAt(i);
            try {
              db.add_device(n0.toString(),newName,server);
            } catch (DevFailed e) {
              JiveUtils.showTangoError(e);
            }
          }

          refresh();
          selectClass(collection,host,level,server,newName);

          break;

        // ----------------------------------------------------------------------------
        case ACTION_DELETE:

          int ok = JOptionPane.showConfirmDialog(invoker, "Delete class " + className + " ?", "Confirm delete", JOptionPane.YES_NO_OPTION);
          if (ok == JOptionPane.YES_OPTION) {

            for(int i=0;i<getChildCount();i++) {
              // Devices
              TangoNode n0 = (TangoNode)getChildAt(i);
              try {
                db.delete_device(n0.toString());
              } catch (DevFailed e) {
                JiveUtils.showTangoError(e);
              }
            }

            refresh();
            selectServer(collection, host, level, server);

          }
          break;

        // ----------------------------------------------------------------------------
        case ACTION_ADDDEVICE:

          ServerDlg sdlg = new ServerDlg(this);
          sdlg.setValidFields(false, false);
          sdlg.setDefaults(server, className);
          ATKGraphicsUtils.centerFrame(invoker.innerPanel, sdlg);
          sdlg.setVisible(true);
          break;

        // ----------------------------------------------------------------------------
        case ACTION_DEVICESWIZ:
          DevWizard dswdlg = new DevWizard(invoker);
          dswdlg.showDevicesWizard(server,className);
          refresh();
          break;

      }

    }

    // IServerAction (Call by ServerDlg)
    public void doJob(String server, String classname, String[] devices) {

      // Add devices
      try {
        for (int i = 0; i < devices.length; i++)
          db.add_device(devices[i], classname, server);
      } catch (DevFailed e) {
        JiveUtils.showTangoError(e);
      }

      refresh();
      selectClass(collection,host,level,server,classname);

    }

  }

  // ---------------------------------------------------------------

  class AttributeNode extends TangoNode {

    private String devName;

    AttributeNode(String devName) {
      this.devName = devName;
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
      return new int[] {
          TreePanel.ACTION_COPY,
          TreePanel.ACTION_PASTE,
          TreePanel.ACTION_CREATE_ATTPROP
      };
    }

    public void execAction(int actionNumber) {
      switch(actionNumber) {

        case TreePanel.ACTION_CREATE_ATTPROP:
          createEmptyAttributeProperty(devName);
          break;

        case TreePanel.ACTION_COPY:
          // Copy all attribute property to the clipboard
          int nbAtt = getChildCount();
          JiveUtils.the_clipboard.clear();
          for(int i=0;i<nbAtt;i++) {
            TaskDeviceAttributePropertyNode node = (TaskDeviceAttributePropertyNode)getChildAt(i);
            String[][] props = node.getProperties();
            for(int j=0;j<props.length;j++)
              JiveUtils.the_clipboard.add(props[j][0],node.getAttributeName(),props[j][1]);
          }
          break;

        case TreePanel.ACTION_PASTE:
          for(int i=0;i<JiveUtils.the_clipboard.getAttPropertyLength();i++) {
            putAttributeProperty( devName,
                JiveUtils.the_clipboard.getAttName(i),
                JiveUtils.the_clipboard.getAttPropertyName(i),
                JiveUtils.the_clipboard.getAttPropertyValue(i));
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

  // ---------------------------------------------------------------

  class DeviceServerNode extends TangoNode {

    private String collection;
    private String host;
    private int    level;
    private String server;
    private String className;
    private String devName;

    DeviceServerNode(String collection,String host,int level,String server, String className, String devName) {
      this.collection = collection;
      this.host = host;
      this.level = level;
      this.server = server;
      this.className = className;
      this.devName = devName;
    }

    void populateNode() throws DevFailed {
      add(new TaskDevicePropertyNode(self,db,devName));
      add(new TaskPollingNode(db,devName));
      add(new TaskEventNode(db,devName));
      add(new TaskAttributeNode(db,devName));
      add(new TaskPipeNode(db,devName));
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
            ACTION_GOTODEVNODE
        };
      else
        return new int[]{ACTION_COPY,
            ACTION_PASTE,
            ACTION_RENAME,
            ACTION_DELETE,
            ACTION_MONITORDEV,
            ACTION_TESTDEV,
            ACTION_DEFALIAS,
            ACTION_GOTODEVNODE,
            ACTION_RESTART,
            ACTION_DEVICEWIZ,
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
        case ACTION_RENAME:
          String newName = JOptionPane.showInputDialog(null,"Rename device",devName);
          if(newName==null) return;
          if( renameDevice(newName) ) {
            refresh();
            selectDevice(collection,host,level,server,className,newName);
          }
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
        case ACTION_GOTODEVNODE:
          invoker.goToDeviceNode(devName);
          break;

        // ----------------------------------------------------------------------------
        case ACTION_RESTART:
          try {
            DeviceProxy ds = new DeviceProxy("dserver/" + server);
            DeviceData in = new DeviceData();
            in.insert(devName);
            ds.command_inout("DevRestart", in);
          } catch (DevFailed e) {
            JiveUtils.showTangoError(e);
          }
          break;

        // ----------------------------------------------------------------------------
        case ACTION_DEVICEWIZ:
          DevWizard dwdlg = new DevWizard(invoker);
          dwdlg.showDeviceWizard(server , className , devName);
          break;

        // ----------------------------------------------------------------------------
        case ACTION_LOG_VIEWER:
          launchLogViewer(devName);
          break;

      }

    }

    boolean renameDevice(String nDevName) {

      boolean isAlive = false;
      boolean success = false;

      try {

        // Check if the device exixts
        DbDevImportInfo ii = db.import_device(nDevName);
        JiveUtils.showJiveError("The device " + nDevName + " already exits.\nServer: " + ii.server);

      } catch (DevFailed e1) {

        // try to create the new device
        try {

          db.add_device(nDevName,className,server);

          // The new device exists
          success = true;

          DeviceProxy ds = null;
          try {
            ds = new DeviceProxy(devName);
            ds.ping();
            isAlive=true;
          } catch (DevFailed e2) {}

          int ok = JOptionPane.showConfirmDialog(invoker, "Do you want to copy propeties of " + devName + " to " + nDevName + " ?", "Confirm propety move", JOptionPane.YES_NO_OPTION);
          if (ok == JOptionPane.YES_OPTION) {

            // Clone device properties
            String[] propList = db.get_device_property_list(devName,"*");
            if (propList.length > 0) {
              DbDatum[] data = db.get_device_property(devName, propList);
              db.put_device_property(nDevName, data);
            }

            // Clone attributes propeties
            try {

              String[] attList = db.get_device_attribute_list(devName);

              if (attList.length > 0) {
                DbAttribute[] adata = db.get_device_attribute_property(devName, attList);
                db.put_device_attribute_property(nDevName, adata);
              }

            } catch (DevFailed e3) {
              JiveUtils.showJiveError("Failed to copy attribute properties of " + devName + "\n" + e3.errors[0].desc);
            }

          }

          // Remove the old device
          if(isAlive)
            JiveUtils.showJiveWarning("The old device " + devName + " is still alive and should be removed by hand.");
          else
            db.delete_device(devName);

        } catch (DevFailed e4) {
          JiveUtils.showTangoError(e4);
        }

      }

      return success;

    }

  }

}
