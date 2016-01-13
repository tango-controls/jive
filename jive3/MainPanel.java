package jive3;

import jive.*;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;
import java.util.prefs.Preferences;

import fr.esrf.TangoApi.ApiUtil;
import fr.esrf.TangoApi.Database;
import fr.esrf.Tango.DevFailed;
import fr.esrf.tangoatk.widget.util.ATKConstant;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;

public class MainPanel extends JFrame implements ChangeListener,NavigationListener {

  private Database db;

  JTabbedPane       treePane;
  JSplitPane        splitPane;
  JPanel            lockPanel;
  TreePanelServer   serverTreePanel = null;
  TreePanelDevice   deviceTreePanel = null;
  TreePanelClass    classTreePanel = null;
  TreePanelAlias    aliasTreePanel = null;
  TreePanelAttributeAlias attributeAliasTreePanel = null;
  TreePanelFreeProperty propertyTreePanel = null;

  // Right panels
  DefaultPanel         defaultPanel;
  PropertyPanel        propertyPanel;
  DevicePollingPanel   devicePollingPanel;
  DeviceEventPanel     deviceEventPanel;
  DeviceAttributePanel deviceAttributePanel;
  DevicePipePanel      devicePipePanel;
  DeviceLoggingPanel   deviceLoggingPanel;
  SingleAttributePanel singleAttributePanel;

  // History panel
  PropertyHistoryDlg   historyDlg;

  // Multiple selecection panel
  SelectionDlg   selectionDlg;

  // Filter dialog
  FilterDlg filterDlg=null;

  // Navigation stuff
  private NavManager    navManager;
  private NavigationBar navBar;
  private boolean       recordPos;

  //Search stuff
  SearchEngine          searchEngine;

  private String lastResOpenedDir = ".";

  private boolean running_from_shell;

  // User settings
  Preferences prefs;
  private String[] knownTangoHost;
  private String THID = "TangoHost";

  // Relase number (Let a space after the release number)
  final static private String appVersion = "Jive 6.8 ";

  // General constructor
  public MainPanel() {
    this(false,false);
  }

  /**
   * Construct a Jive application.
   * @param runningFromShell True if running from shell. If true , Jive calls System.exit().
   * @param readOnly Read only flag.
   */
  public MainPanel(boolean runningFromShell,boolean readOnly) {

    // Get user settings
    prefs = Preferences.userRoot().node(this.getClass().getName());
    knownTangoHost = JiveUtils.makeStringArray(prefs.get(THID,""));
    if(knownTangoHost.length==1)
      if(knownTangoHost[0].equals(""))
        knownTangoHost = new String[0];

    running_from_shell = runningFromShell;
    JiveUtils.readOnly = readOnly;
    initComponents();
    centerWindow();
    setVisible(true);
    JiveUtils.parent = this;
    navManager = new NavManager(this);
    searchEngine = new SearchEngine(this);
    recordPos = true;

  }

  // Init componenet
  private void initComponents() {

    getContentPane().setLayout(new BorderLayout());
    MultiLineToolTipUI.initialize();

    // *************************************************************
    // Initialise the Tango database
    // *************************************************************
    String tangoHost = null;

    try {
      tangoHost = ApiUtil.getTangoHost();
    } catch ( DevFailed e ) {
      System.out.println("TANGO_HOST no defined, exiting...");
      exitForm();
    }

    try {
      db = ApiUtil.get_db_obj();
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
      db = null;
    }

    updateTitle(tangoHost);

    // *************************************************************
    // Create widget
    // *************************************************************

    splitPane = new JSplitPane();
    splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
    serverTreePanel = new TreePanelServer(this);
    serverTreePanel.setDatabase(db);
    deviceTreePanel = new TreePanelDevice(this);
    deviceTreePanel.setDatabase(db);
    classTreePanel = new TreePanelClass(this);
    classTreePanel.setDatabase(db);
    aliasTreePanel = new TreePanelAlias(this);
    aliasTreePanel.setDatabase(db);
    attributeAliasTreePanel = new TreePanelAttributeAlias(this);
    attributeAliasTreePanel.setDatabase(db);
    propertyTreePanel = new TreePanelFreeProperty(this);
    propertyTreePanel.setDatabase(db);
    historyDlg = new PropertyHistoryDlg();
    historyDlg.setDatabase(db,tangoHost);
    selectionDlg = new SelectionDlg();
    selectionDlg.setDatabase(db);
    treePane = new JTabbedPane();
    treePane.setMinimumSize(new Dimension(365,0));
    treePane.setFont(ATKConstant.labelFont);
    treePane.add("Server", serverTreePanel);
    treePane.add("Device", deviceTreePanel);
    treePane.add("Class",classTreePanel);
    treePane.add("Alias",aliasTreePanel);
    treePane.add("Att. Alias",attributeAliasTreePanel);
    treePane.add("Property",propertyTreePanel);
    treePane.addChangeListener(this);
    splitPane.setLeftComponent(treePane);
    getContentPane().add(splitPane, BorderLayout.CENTER);
    defaultPanel = new DefaultPanel();
    propertyPanel = new PropertyPanel();
    propertyPanel.setParent(this);
    devicePollingPanel = new DevicePollingPanel();
    deviceEventPanel = new DeviceEventPanel();
    deviceAttributePanel = new DeviceAttributePanel();
    devicePipePanel = new DevicePipePanel();
    deviceLoggingPanel = new DeviceLoggingPanel();
    singleAttributePanel = new SingleAttributePanel();
    splitPane.setRightComponent(defaultPanel);

    navBar = new NavigationBar();
    navBar.enableBack(false);
    navBar.enableForward(false);
    navBar.enableNextOcc(false);
    navBar.enablePreviousOcc(false);
    navBar.addNavigationListener(this);

    if( JiveUtils.readOnly ) {

      JPanel upPanel = new JPanel();
      upPanel.setLayout(new BorderLayout());

      lockPanel = new JPanel();
      lockPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
      lockPanel.setBackground(new Color(233,233,233));
      JLabel lockIcon = new JLabel();
      lockIcon.setIcon(new ImageIcon(getClass().getResource("/jive/lock.gif")));
      lockPanel.add(lockIcon);
      JLabel lockLabel = new JLabel("Read only mode (No write access to database allowed)");
      lockLabel.setFont(ATKConstant.labelFont);
      lockPanel.add(lockLabel);
      upPanel.add(lockPanel,BorderLayout.NORTH);
      upPanel.add(navBar,BorderLayout.SOUTH);
      getContentPane().add(upPanel,BorderLayout.NORTH);

    } else {

      getContentPane().add(navBar,BorderLayout.NORTH);

    }

    //**************************************************************
    // Menu bar
    //**************************************************************
    JMenuBar mainMenu = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    JMenuItem loadFile = new JMenuItem("Load property file");
    loadFile.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        loadPropFile();
      }
    });
    loadFile.setEnabled(!JiveUtils.readOnly);
    JMenuItem checkFile = new JMenuItem("Check property file");
    checkFile.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        checkPropFile();
      }
    });
    JSeparator sep1 = new JSeparator();
    JMenuItem exit = new JMenuItem("Exit");
    exit.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        exitForm();
      }
    });

    fileMenu.add(loadFile);
    fileMenu.add(checkFile);
    fileMenu.add(sep1);
    fileMenu.add(exit);
    mainMenu.add(fileMenu);

    JMenu editMenu = new JMenu("Edit");
    JMenuItem refresh = new JMenuItem("Refresh Tree");
    refresh.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        refreshTree();
      }
    });
    refresh.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
    editMenu.add(refresh);

    editMenu.add(new JSeparator());

    if( running_from_shell ) {

      JMenuItem chTangoHost = new JMenuItem("Change Tango Host");
      chTangoHost.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        changeTangoHost();
      }
    });
      editMenu.add(chTangoHost);

      editMenu.add(new JSeparator());

    }

    JMenuItem createServer = new JMenuItem("Create server");
    createServer.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        createServer();
      }
    });
    createServer.setEnabled(!JiveUtils.readOnly);
    editMenu.add(createServer);

    JMenuItem createFreeProperty = new JMenuItem("Create free property");
    createFreeProperty.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        createFreeProperty();
      }
    });
    createFreeProperty.setEnabled(!JiveUtils.readOnly);
    editMenu.add(createFreeProperty);

    editMenu.add(new JSeparator());

    JMenuItem showClipboard = new JMenuItem("Show clipboard");
    showClipboard.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        showClipboard();
      }
    });
    editMenu.add(showClipboard);

    JMenuItem clearClipboard = new JMenuItem("Clear clipboard");
    clearClipboard.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        JiveUtils.the_clipboard.clear();
      }
    });
    editMenu.add(clearClipboard);

    editMenu.add(new JSeparator());

    final JCheckBoxMenuItem showSystemProperty = new JCheckBoxMenuItem("Show system property");
    showSystemProperty.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        JiveUtils.showSystemProperty = showSystemProperty.isSelected();
        refreshTree();
      }
    });
    editMenu.add(showSystemProperty);

    JMenu serverMenu = new JMenu("Tools");
    JMenuItem createServerWz = new JMenuItem("Server Wizard");
    createServerWz.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        createServerWz();
      }
    });
    createServerWz.setEnabled(!JiveUtils.readOnly);
    serverMenu.add(createServerWz);
    JMenuItem dbInfoMenu = new JMenuItem("Database Info");
    dbInfoMenu.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showDatabaseInfo();
      }
    });
    serverMenu.add(dbInfoMenu);
    JMenuItem dbHistMenu = new JMenuItem("Database history");
    dbHistMenu.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        showHistory();
      }
    });
    serverMenu.add(dbHistMenu);
    JMenuItem selectionMenu = new JMenuItem("Multiple selection");
    selectionMenu.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        showMultipleSelection();
      }
    });
    serverMenu.add(selectionMenu);

    JMenu filterMenu = new JMenu("Filter");
    JMenuItem filterServer = new JMenuItem("Server");
    filterServer.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        filterServer();
      }
    });
    filterMenu.add(filterServer);
    JMenuItem filterDevice = new JMenuItem("Device");
    filterDevice.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        filterDevice();
      }
    });
    filterMenu.add(filterDevice);
    JMenuItem filterClass = new JMenuItem("Class");
    filterClass.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        filterClass();
      }
    });
    filterMenu.add(filterClass);
    JMenuItem filterAlias = new JMenuItem("Alias");
    filterAlias.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        filterAlias();
      }
    });
    filterMenu.add(filterAlias);
    JMenuItem filterAttributeAlias = new JMenuItem("Att. Alias");
    filterAttributeAlias.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        filterAttributeAlias();
      }
    });
    filterMenu.add(filterAttributeAlias);
    JMenuItem filterProperty = new JMenuItem("Property");
    filterProperty.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        filterProperty();
      }
    });
    filterMenu.add(filterProperty);

    mainMenu.add(fileMenu);
    mainMenu.add(editMenu);
    mainMenu.add(serverMenu);
    mainMenu.add(filterMenu);
    setJMenuBar(mainMenu);

    //**************************************************************
    // Component listener
    //**************************************************************
    addComponentListener(new ComponentListener() {

      public void componentHidden(ComponentEvent e) {
        exitForm();
      }

      public void componentMoved(ComponentEvent e) {
      }

      public void componentResized(ComponentEvent e) {
      }

      public void componentShown(ComponentEvent e) {
      }

    });

    ImageIcon icon = new ImageIcon(getClass().getResource("/jive/jive.jpg"));
    setIconImage(icon.getImage());

  }

  //**************************************************************
  // Navigation listener
  //**************************************************************
  private void reselect() {

    TreePath path = navManager.getCurrentPath();
    JTree tree = navManager.getCurrentTree();

    recordPos = false;

    if(tree==serverTreePanel.tree) {
      treePane.setSelectedComponent(serverTreePanel);
    } else if (tree==deviceTreePanel.tree) {
      treePane.setSelectedComponent(deviceTreePanel);
    } else if (tree==classTreePanel.tree) {
      treePane.setSelectedComponent(classTreePanel);
    } else if (tree==aliasTreePanel.tree) {
      treePane.setSelectedComponent(aliasTreePanel);
    } else if (tree==attributeAliasTreePanel.tree) {
      treePane.setSelectedComponent(attributeAliasTreePanel);
    } else if (tree==propertyTreePanel.tree) {
      treePane.setSelectedComponent(propertyTreePanel);
    }
    // Work around X11 bug
    treePane.getSelectedComponent().setVisible(true);

    tree.setSelectionPath(path);
    recordPos = true;
    tree.scrollPathToVisible(path);

  }

  public void backAction(NavigationBar src) {
    navManager.goBack();
    navBar.enableBack(navManager.canGoBackward());
    navBar.enableForward(navManager.canGoForward());
    reselect();
  }
  public void forwardAction(NavigationBar src) {
    navManager.goForward();
    navBar.enableBack(navManager.canGoBackward());
    navBar.enableForward(navManager.canGoForward());
    reselect();
  }

  public void nextOccAction(NavigationBar src) {

    TreePath path = searchEngine.findNext();
    TreePanel selected = (TreePanel)treePane.getSelectedComponent();
    if(path!=null) {
      selected.tree.setSelectionPath(path);
      selected.tree.scrollPathToVisible(path);
    }

    navBar.enableNextOcc(!searchEngine.isStackEmpty());

  }

  public void previousOccAction(NavigationBar src) {

  }

  public void searchAction(NavigationBar src,TreePath pathToSelect) {

    // Check if we have a link
    if(pathToSelect!=null) {
      String treeName = pathToSelect.getPathComponent(0).toString();
      if(treeName.equals("Server:")) {
        serverTreePanel.selectPath(pathToSelect);
        treePane.setSelectedComponent(serverTreePanel);
        treePane.getSelectedComponent().setVisible(true);
      } else if(treeName.equals("Device:")) {
        deviceTreePanel.selectPath(pathToSelect);
        treePane.setSelectedComponent(deviceTreePanel);
        treePane.getSelectedComponent().setVisible(true);
      } else if(treeName.equals("Class:")) {
        classTreePanel.selectPath(pathToSelect);
        treePane.setSelectedComponent(classTreePanel);
        treePane.getSelectedComponent().setVisible(true);
      } else if(treeName.equals("Alias:")) {
        aliasTreePanel.selectPath(pathToSelect);
        treePane.setSelectedComponent(aliasTreePanel);
        treePane.getSelectedComponent().setVisible(true);
      } else if(treeName.equals("AttAlias:")) {
        attributeAliasTreePanel.selectPath(pathToSelect);
        treePane.setSelectedComponent(attributeAliasTreePanel);
        treePane.getSelectedComponent().setVisible(true);
      } else if(treeName.equals("FreeProperty:")) {
        propertyTreePanel.selectPath(pathToSelect);
        treePane.setSelectedComponent(propertyTreePanel);
        treePane.getSelectedComponent().setVisible(true);
      }
      resetSearch();
      return;
    }

    // Search
    String searchText = src.getSearchText();

    if( searchText.startsWith("Server:/") ) {
      searchText = searchText.substring(8);
      treePane.setSelectedComponent(serverTreePanel);
    } else if ( searchText.startsWith("Device:/") ) {
      searchText = searchText.substring(8);
      treePane.setSelectedComponent(deviceTreePanel);
    } else if ( searchText.startsWith("Class:/") ) {
      searchText = searchText.substring(7);
      treePane.setSelectedComponent(classTreePanel);
    } else if ( searchText.startsWith("Alias:/") ) {
      searchText = searchText.substring(7);
      treePane.setSelectedComponent(aliasTreePanel);
    } else if ( searchText.startsWith("AttAlias:/") ) {
      searchText = searchText.substring(10);
      treePane.setSelectedComponent(attributeAliasTreePanel);
    } else if ( searchText.startsWith("FreeProperty:/") ) {
      searchText = searchText.substring(14);
      treePane.setSelectedComponent(propertyTreePanel);
    }

    treePane.getSelectedComponent().setVisible(true);
    TreePanel selected = (TreePanel)treePane.getSelectedComponent();
    String[] fieldnames = searchText.split("/");

    // Fast one field search
    if( fieldnames.length==1 ) {
      // One field name given
      if( selected.isRootItem(fieldnames[0]) ) {
        TreePath path = selected.selectRootItem(fieldnames[0]);
        selected.tree.scrollPathToVisible(path);
        TangoNode focusedNode = (TangoNode)path.getLastPathComponent();
        searchEngine.setSearchText(searchText);
        resetSearch(focusedNode);
        return;
      }
    }

    // Fast device search
    if( JiveUtils.isDeviceName(searchText) ) {
      if( searchText.startsWith("tango:") )
        searchText = searchText.substring(6);
      if( deviceTreePanel.isDomain(fieldnames[0])) {
        TangoNode focusedNode = goToDeviceFullNode(searchText);
        if(focusedNode!=null) {
          searchEngine.setSearchText(searchText);
          resetSearch(focusedNode);
          return;
        }
      }
    }

    // Fast server search
    if( JiveUtils.isFullServerName(searchText) ) {
      if( serverTreePanel.isServer(fieldnames[0]) ) {
        TangoNode focusedNode = goToServerFullNode(searchText);
        if( focusedNode == null ) {
          // Try to go to server root
          focusedNode = goToServerRootNode(fieldnames[0]);
          if( focusedNode!=null ) {
            searchEngine.setSearchText(searchText);
            resetSearch(focusedNode);
            return;
          }
        } else {
          searchEngine.setSearchText(searchText);
          resetSearch(focusedNode);
          return;
        }
      }
    }

    // Default search
    TreePath path = searchEngine.findText(searchText,selected.root);
    if(path!=null) {
      selected.tree.setSelectionPath(path);
      selected.tree.scrollPathToVisible(path);
    }

    navBar.enableNextOcc(!searchEngine.isStackEmpty());

  }
  public void refreshAction(NavigationBar src) {
    refreshTree();
  }

  // Show the clipboard content
  public void showClipboard() {
    JiveUtils.the_clipboard.show(this);
  }

  // Create a free property object
  private void createFreeProperty() {

    String newProp = JOptionPane.showInputDialog(this, "Enter property object name", "Jive", JOptionPane.QUESTION_MESSAGE);
    if (newProp != null) {
      propertyTreePanel.addProperty(newProp);
      treePane.setSelectedComponent(propertyTreePanel);
      propertyTreePanel.setVisible(true);
    }

  }

  private void resetSearch() {
    resetSearch(null);
  }

  private void resetSearch(TangoNode focusedNode) {

    searchEngine.resetSearch(focusedNode);
    navBar.enableNextOcc(focusedNode!=null);
    navBar.enablePreviousOcc(false);

  }

  // Create a server
  private void createServer() {

    ServerDlg sdlg = new ServerDlg(this);
    sdlg.setValidFields(true, true);
    sdlg.setDefaults("", "");
    if (sdlg.showDlg()) {

      String[] devices = sdlg.getDeviceNames();
      String server = sdlg.getServerName();
      String classname = sdlg.getClassName();

      // Add devices
      try {

        // Check that device is not already existing
        Vector exDevices = new Vector();
        for (int i = 0; i < devices.length; i++) {
          try {
            db.import_device(devices[i]);
          } catch(DevFailed e) {
            continue;
          }
          exDevices.add(devices[i]);
        }

        if(exDevices.size()>0) {
          String message = "Warning, following device(s) already declared:\n";
          for(int i=0;i<exDevices.size();i++)
            message += "   " + exDevices.get(i) + "\n";
          message += "Do you want to continue ?";

          if( JOptionPane.showConfirmDialog(this,message,"Confirmation",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION )
            return;
        }

        for (int i = 0; i < devices.length; i++) {
          db.add_device(devices[i], classname, server);
        }

      } catch (DevFailed e) {
        JiveUtils.showTangoError(e);
      }

      refreshTree();
      serverTreePanel.selectFullServer(server);

    }

  }

  // Filter server
  private void filterServer() {

    if(filterDlg==null) filterDlg = new FilterDlg(this);
    filterDlg.setLabelName("Server filter");
    filterDlg.setFilter(serverTreePanel.getFilter());
    if( filterDlg.showDialog() ) {
      // Apply filter
      serverTreePanel.applyFilter(filterDlg.getFilterText());
      serverTreePanel.refresh();
      resetSearch();
    }

  }

  // Filter device
  private void filterDevice() {

    if(filterDlg==null) filterDlg = new FilterDlg(this);
    filterDlg.setLabelName("Device filter");
    filterDlg.setFilter(deviceTreePanel.getFilter());
    if( filterDlg.showDialog() ) {
      // Apply filter
      deviceTreePanel.applyFilter(filterDlg.getFilterText());
      deviceTreePanel.refresh();
      resetSearch();
    }

  }

  // Filter class
  private void filterClass() {

    if(filterDlg==null) filterDlg = new FilterDlg(this);
    filterDlg.setLabelName("Class filter");
    filterDlg.setFilter(classTreePanel.getFilter());
    if( filterDlg.showDialog() ) {
      // Apply filter
      classTreePanel.applyFilter(filterDlg.getFilterText());
      classTreePanel.refresh();
      resetSearch();
    }

  }

  // Filter alias
  private void filterAlias() {

    if(filterDlg==null) filterDlg = new FilterDlg(this);
    filterDlg.setLabelName("Alias filter");
    filterDlg.setFilter(aliasTreePanel.getFilter());
    if( filterDlg.showDialog() ) {
      // Apply filter
      aliasTreePanel.applyFilter(filterDlg.getFilterText());
      aliasTreePanel.refresh();
      resetSearch();
    }

  }

  // Filter attribute alias
  private void filterAttributeAlias() {

    if(filterDlg==null) filterDlg = new FilterDlg(this);
    filterDlg.setLabelName("Att. Alias filter");
    filterDlg.setFilter(attributeAliasTreePanel.getFilter());
    if( filterDlg.showDialog() ) {
      // Apply filter
      attributeAliasTreePanel.applyFilter(filterDlg.getFilterText());
      attributeAliasTreePanel.refresh();
      resetSearch();
    }

  }

  // Filter property
  private void filterProperty() {

    if(filterDlg==null) filterDlg = new FilterDlg(this);
    filterDlg.setLabelName("Property filter");
    filterDlg.setFilter(propertyTreePanel.getFilter());
    if( filterDlg.showDialog() ) {
      // Apply filter
      propertyTreePanel.applyFilter(filterDlg.getFilterText());
      propertyTreePanel.refresh();
      resetSearch();
    }

  }

  // Create a server using the wizard
  private void createServerWz() {

    DevWizard wdlg = new DevWizard(this);
    wdlg.showWizard(null);
    refreshTree();

  }

  // Update the title bar
  private void updateTitle(String tangoHost) {

    String title = new String(appVersion);
    if (JiveUtils.readOnly) {
        title += "(Read Only)";
    }
    if(db==null)
      setTitle(title + " [No connection]");
    else
      setTitle(title + " [" + tangoHost + "]");

  }

  private boolean isKnowTangoHost(String th) {

    boolean found = false;
    int i = 0;
    while(!found&&i<knownTangoHost.length) {
      found = knownTangoHost[i].equalsIgnoreCase(th);
      if(!found) i++;
    }
    return found;

  }

  // Change the TANGO HOST
  private void changeTangoHost() {

    TangoHostDlg dlg = new TangoHostDlg(this,knownTangoHost);
    String th = dlg.getTangoHost();

    if (th != null) {

      String[] ths = th.split(":");

      if( ths.length!=2 ) {
        JiveUtils.showJiveError("Invalid tango host syntax: should be host:port");
        return;
      }

      try {
        Integer.parseInt(ths[1]);
      } catch (NumberFormatException e) {
        JiveUtils.showJiveError("Invalid tango host port number\n" + e.getMessage());
        return;
      }

      try {

        db = ApiUtil.change_db_obj(ths[0],ths[1]);

        // Add this host the to list (if needed) and save to pref

        if( !isKnowTangoHost(th) ) {

          String[] newTH = new String[knownTangoHost.length+1];
          for(int i=0;i<knownTangoHost.length;i++) {
            newTH[i]=knownTangoHost[i];
          }
          newTH[knownTangoHost.length] = th;
          knownTangoHost = newTH;
          JiveUtils.sortList(knownTangoHost);
          prefs.put(THID,JiveUtils.stringArrayToString(knownTangoHost));

        }

      } catch (DevFailed e) {
        JiveUtils.showTangoError(e);
        db = null;
      } catch (Exception e) {
        JiveUtils.showJiveError(e.getMessage());
        db=null;
      }

      ProgressFrame.displayProgress("Refresh in progress");
      serverTreePanel.setDatabase(db);
      ProgressFrame.setProgress("Refreshing...", 20);
      deviceTreePanel.setDatabase(db);
      ProgressFrame.setProgress("Refreshing...", 40);
      classTreePanel.setDatabase(db);
      ProgressFrame.setProgress("Refreshing...", 60);
      aliasTreePanel.setDatabase(db);
      attributeAliasTreePanel.setDatabase(db);
      ProgressFrame.setProgress("Refreshing...", 80);
      propertyTreePanel.setDatabase(db);
      ProgressFrame.setProgress("Refreshing...", 100);
      historyDlg.setDatabase(db, th);
      selectionDlg.setDatabase(db);
      updateTitle(th);
      defaultPanel.setSource(null,0);
      splitPane.setRightComponent(defaultPanel);
      ProgressFrame.hideProgress();
      resetSearch();

    }

  }

  public void resetNavigation() {

    navManager.reset();
    navBar.enableForward(false);
    navBar.enableBack(false);

  }

  // Refresh all trees
  private void refreshTree() {

    ProgressFrame.displayProgress("Refresh in progress");
    serverTreePanel.refresh();
    ProgressFrame.setProgress("Refreshing...", 20);
    deviceTreePanel.refresh();
    ProgressFrame.setProgress("Refreshing...", 40);
    classTreePanel.refresh();
    ProgressFrame.setProgress("Refreshing...", 60);
    aliasTreePanel.refresh();
    attributeAliasTreePanel.refresh();
    ProgressFrame.setProgress("Refreshing...", 70);
    attributeAliasTreePanel.refresh();
    ProgressFrame.setProgress("Refreshing...",80);
    propertyTreePanel.refresh();
    updateTabbedPane();
    resetSearch();

    ProgressFrame.hideProgress();
  }

  // Load a property file in the database
  private void loadPropFile() {
    String err = "";
    TangoFileReader fr = new TangoFileReader(db);

    JFileChooser chooser = new JFileChooser(lastResOpenedDir);
    int returnVal = chooser.showOpenDialog(this);

    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File f = chooser.getSelectedFile();
      ResDlg dlg = new ResDlg(this,f.getAbsolutePath());
      if( dlg.showDlg() ) {
        if (f != null) err = fr.parse_res_file(f.getAbsolutePath());
        if (err.length() > 0) JiveUtils.showJiveError(err);
        if (f != null) lastResOpenedDir = f.getAbsolutePath();
      }
    }
  }

  // Load a property file in the database
  private void checkPropFile() {
    
    String err = "";
    TangoFileReader fr = new TangoFileReader(db);

    JFileChooser chooser = new JFileChooser(lastResOpenedDir);
    int returnVal = chooser.showOpenDialog(this);

    if (returnVal == JFileChooser.APPROVE_OPTION) {

      File f = chooser.getSelectedFile();
      if( f!=null ) {

        Vector diff = new Vector();
        err = fr.check_res_file(f.getAbsolutePath(),diff);
        lastResOpenedDir = f.getAbsolutePath();

        if (err.length() > 0) {
          JiveUtils.showJiveError(err);
        } else {
          if( diff.size()>0 ) {
            // Show differences
            DiffDlg dlg = new DiffDlg(diff,f.getAbsolutePath());
            ATKGraphicsUtils.centerFrameOnScreen(dlg);
            dlg.setVisible(true);
          } else {
            JOptionPane.showMessageDialog(this,"Database and file match.");
          }
        }

      }

    }
  }

  // Show database info
  private void showDatabaseInfo() {

    if(db==null) return;

    try {

      String result = db.get_info();
      JOptionPane.showMessageDialog(this,result,"Database Info",JOptionPane.INFORMATION_MESSAGE);

    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }
  
  // Show Multiple selection dialog
  private void showMultipleSelection() {

    if(!selectionDlg.isVisible())
      ATKGraphicsUtils.centerFrameOnScreen(selectionDlg);
    selectionDlg.clear();
    selectionDlg.setVisible(true);

  }

  // Display the history window
  public void showHistory() {

    if(!historyDlg.isVisible())
      ATKGraphicsUtils.centerFrameOnScreen(historyDlg);
    historyDlg.setVisible(true);

  }

  // Select a device and show the device tree panel
  public void goToDeviceNode(String devName) {
    deviceTreePanel.selectDevice(devName);
    treePane.setSelectedComponent(deviceTreePanel);
    // Work around X11 bug
    treePane.getSelectedComponent().setVisible(true);
  }

  private TangoNode goToDeviceFullNode(String devName) {
    TangoNode selected = deviceTreePanel.selectDevice(devName);
    if( selected!=null ) {
      treePane.setSelectedComponent(deviceTreePanel);
      // Work around X11 bug
      treePane.getSelectedComponent().setVisible(true);
    }
    return selected;
  }

  // Select a server and show the server tree panel
  public void goToServerNode(String srvName) {
    goToServerFullNode(srvName);
  }

  // Select a server and show the server tree panel
  public TangoNode goToServerFullNode(String srvName) {
    TangoNode selected = serverTreePanel.selectFullServer(srvName);
    if( selected!=null ) {
      treePane.setSelectedComponent(serverTreePanel);
      // Work around X11 bug
      treePane.getSelectedComponent().setVisible(true);
    }
    return selected;
  }

  // Select a server and show the server tree panel
  public TangoNode goToServerRootNode(String srvName) {
    TangoNode selected = serverTreePanel.selectServerRoot(srvName);
    if( selected!=null ) {
      treePane.setSelectedComponent(serverTreePanel);
      // Work around X11 bug
      treePane.getSelectedComponent().setVisible(true);
    }
    return selected;
  }

  // Tabbed pane listener
  public void stateChanged(ChangeEvent e) {

    updateTabbedPane();

  }

  private void updateTabbedPane() {

    switch(treePane.getSelectedIndex()) {
      case 0:
        serverTreePanel.refreshValues();
        break;
      case 1:
        deviceTreePanel.refreshValues();
        break;
      case 2:
        classTreePanel.refreshValues();
        break;
      case 3:
        aliasTreePanel.refreshValues();
        break;
      case 4:
        attributeAliasTreePanel.refreshValues();
        break;
      case 5:
        propertyTreePanel.refreshValues();
        break;
    }

  }

  // TreeListener
  void updatePanel(TangoNode[] source) {

    int i;

    // Check if there is some unsaved change
    try {

      Component panel = splitPane.getRightComponent();

      if( panel instanceof PropertyPanel) {
        PropertyPanel propertyPanel = (PropertyPanel)panel;
        if( propertyPanel.hasChanged() ) {
          if( JOptionPane.showConfirmDialog(this,"Some properties has been updated and not saved.\nWould you like to save change ?","Confirmation",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION )
            propertyPanel.saveChange();
        }
      }

      if( panel instanceof SingleAttributePanel) {
        SingleAttributePanel attPanel = (SingleAttributePanel)panel;
        if( attPanel.hasChanged() ) {
          if( JOptionPane.showConfirmDialog(this,"Some attribute properties has been updated and not saved.\nWould you like to save change ?","Confirmation",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION )
            attPanel.saveChange();
        }
      }

    } catch(Exception e) {}


    // No selection
    if(source==null || source.length==0) {
      defaultPanel.setSource(null,0);
      splitPane.setRightComponent(defaultPanel);
      return;
    }


    switch (treePane.getSelectedIndex()) {
      case 0:
        serverTreePanel.tree.setName("SERVER");
        if (recordPos) navManager.recordPath(serverTreePanel.tree);
        navBar.addLink(serverTreePanel.tree.getSelectionPath());
        break;
      case 1:
        deviceTreePanel.tree.setName("DEVICE");
        if (recordPos) navManager.recordPath(deviceTreePanel.tree);
        navBar.addLink(deviceTreePanel.tree.getSelectionPath());
        break;
      case 2:
        classTreePanel.tree.setName("CLASS");
        if (recordPos) navManager.recordPath(classTreePanel.tree);
        navBar.addLink(classTreePanel.tree.getSelectionPath());
        break;
      case 3:
        aliasTreePanel.tree.setName("DEV-ALIAS");
        if (recordPos) navManager.recordPath(aliasTreePanel.tree);
        navBar.addLink(aliasTreePanel.tree.getSelectionPath());
        break;
      case 4:
        attributeAliasTreePanel.tree.setName("ATT-ALIAS");
        if (recordPos) navManager.recordPath(attributeAliasTreePanel.tree);
        navBar.addLink(attributeAliasTreePanel.tree.getSelectionPath());
        break;
      case 5:
        propertyTreePanel.tree.setName("PROPERTY");
        if (recordPos) navManager.recordPath(propertyTreePanel.tree);
        navBar.addLink(propertyTreePanel.tree.getSelectionPath());
        break;
    }

    navBar.enableBack(navManager.canGoBackward());
    navBar.enableForward(navManager.canGoForward());



    // Check node class
    boolean sameClass = true;
    Class nodeClass = source[0].getClass();
    i=1;
    while(sameClass && i<source.length) {
      sameClass = (source[i].getClass() == nodeClass);
      i++;
    }

    // Get last selection when several node class are selected
    if( !sameClass ) {
      TangoNode[] newSource = new TangoNode[1];
      newSource[0] = source[0];
      source = newSource;
    }

    // Update the panel
    if(source[0] instanceof PropertyNode) {
      PropertyNode[] nodes = new PropertyNode[source.length];
      for(i=0;i<source.length;i++) nodes[i] = (PropertyNode)source[i];
      propertyPanel.setSource(nodes);
      splitPane.setRightComponent(propertyPanel);
    } else if(nodeClass == TaskPollingNode.class) {
      TaskPollingNode[] nodes = new TaskPollingNode[source.length];
      for(i=0;i<source.length;i++) nodes[i] = (TaskPollingNode)source[i];
      devicePollingPanel.setSource(nodes);
      splitPane.setRightComponent(devicePollingPanel);
    } else if(nodeClass == TaskEventNode.class) {
      TaskEventNode[] nodes = new TaskEventNode[source.length];
      for(i=0;i<source.length;i++) nodes[i] = (TaskEventNode)source[i];
      deviceEventPanel.setSource(nodes);
      splitPane.setRightComponent(deviceEventPanel);
    } else if(nodeClass == TaskAttributeNode.class) {
      TaskAttributeNode[] nodes = new TaskAttributeNode[source.length];
      for(i=0;i<source.length;i++) nodes[i] = (TaskAttributeNode)source[i];
      deviceAttributePanel.setSource(nodes);
      splitPane.setRightComponent(deviceAttributePanel);
    } else if(nodeClass == TaskPipeNode.class) {
      TaskPipeNode[] nodes = new TaskPipeNode[source.length];
      for(i=0;i<source.length;i++) nodes[i] = (TaskPipeNode)source[i];
      devicePipePanel.setSource(nodes);
      splitPane.setRightComponent(devicePipePanel);
    } else if(nodeClass == TaskLoggingNode.class) {
      TaskLoggingNode[] nodes = new TaskLoggingNode[source.length];
      for(i=0;i<source.length;i++) nodes[i] = (TaskLoggingNode)source[i];
      deviceLoggingPanel.setSource(nodes);
      splitPane.setRightComponent(deviceLoggingPanel);
    } else if(nodeClass == TaskSingleAttributeNode.class) {
      TaskSingleAttributeNode[] nodes = new TaskSingleAttributeNode[source.length];
      for(i=0;i<source.length;i++) nodes[i] = (TaskSingleAttributeNode)source[i];
      singleAttributePanel.setSource(nodes);
      splitPane.setRightComponent(singleAttributePanel);
    } else {
      defaultPanel.setSource(source[0],source.length);
      splitPane.setRightComponent(defaultPanel);
    }

  }

  // Exit application
  private void exitForm() {
    if (running_from_shell)
      System.exit(0);
    else {
      setVisible(false);
      dispose();
    }
  }

  // Center the window
  private void centerWindow() {
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Dimension scrsize = toolkit.getScreenSize();
    Dimension appsize = new Dimension(830, 500);
    int x = (scrsize.width - appsize.width) / 2;
    int y = (scrsize.height - appsize.height) / 2;
    setBounds(x, y, appsize.width, appsize.height);
  }

  // Main function
  public static void main(String args[]) {

    if(args.length==0) {
      new MainPanel(true,false);
    } else {
      if( args[0].equalsIgnoreCase("-r") ) {
        new MainPanel(true,true);
      } else {
        System.out.println("Usage: jive [-r]");
        System.out.println("   -r  Read only mode (No write access to database allowed)");
      }
    }

  }

}
