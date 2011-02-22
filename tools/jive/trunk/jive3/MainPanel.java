package jive3;

import jive.*;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Vector;

import fr.esrf.TangoApi.ApiUtil;
import fr.esrf.TangoApi.Database;
import fr.esrf.Tango.DevFailed;
import fr.esrf.tangoatk.widget.util.ATKConstant;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;

public class MainPanel extends JFrame implements ChangeListener {

  private Database db;

  JTabbedPane       treePane;
  JSplitPane        splitPane;
  JPanel            lockPanel;
  TreePanelServer   serverTreePanel = null;
  TreePanelDevice   deviceTreePanel = null;
  TreePanelClass    classTreePanel = null;
  TreePanelAlias    aliasTreePanel = null;
  TreePanelFreeProperty propertyTreePanel = null;

  // Right panels
  DefaultPanel         defaultPanel;
  PropertyPanel        propertyPanel;
  DevicePollingPanel   devicePollingPanel;
  DeviceEventPanel     deviceEventPanel;
  DeviceAttributePanel deviceAttributePanel;
  DeviceLoggingPanel   deviceLoggingPanel;

  // Hisotry panel
  PropertyHistoryDlg   historyDlg;

  // Filter dialog
  FilterDlg filterDlg=null;

  private String lastResOpenedDir = ".";

  private boolean running_from_shell;

  // Relase number (Let a space after the release number)
  final static private String appVersion = "Jive 4.18 ";

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
    running_from_shell = runningFromShell;
    JiveUtils.readOnly = readOnly;
    initComponents();
    centerWindow();
    setVisible(true);
    JiveUtils.parent = this;
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
    splitPane.setDividerLocation(0.4);
    serverTreePanel = new TreePanelServer(this);
    serverTreePanel.setDatabase(db);
    deviceTreePanel = new TreePanelDevice(this);
    deviceTreePanel.setDatabase(db);
    classTreePanel = new TreePanelClass(this);
    classTreePanel.setDatabase(db);
    aliasTreePanel = new TreePanelAlias(this);
    aliasTreePanel.setDatabase(db);
    propertyTreePanel = new TreePanelFreeProperty(this);
    propertyTreePanel.setDatabase(db);
    historyDlg = new PropertyHistoryDlg();
    historyDlg.setDatabase(db,tangoHost);
    treePane = new JTabbedPane();
    treePane.setMinimumSize(new Dimension(300,0));
    treePane.setFont(ATKConstant.labelFont);
    treePane.add("Server",serverTreePanel);
    treePane.add("Device",deviceTreePanel);
    treePane.add("Class",classTreePanel);
    treePane.add("Alias",aliasTreePanel);
    treePane.add("Property",propertyTreePanel);
    treePane.addChangeListener(this);
    splitPane.setLeftComponent(treePane);
    getContentPane().add(splitPane,BorderLayout.CENTER);
    defaultPanel = new DefaultPanel();
    propertyPanel = new PropertyPanel();
    propertyPanel.setParent(this);
    devicePollingPanel = new DevicePollingPanel();
    deviceEventPanel = new DeviceEventPanel();
    deviceAttributePanel = new DeviceAttributePanel();
    deviceLoggingPanel = new DeviceLoggingPanel();
    splitPane.setRightComponent(defaultPanel);

    if( JiveUtils.readOnly ) {
      lockPanel = new JPanel();
      lockPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
      lockPanel.setBackground(new Color(233,233,233));
      JLabel lockIcon = new JLabel();
      lockIcon.setIcon(new ImageIcon(getClass().getResource("/jive/lock.gif")));
      lockPanel.add(lockIcon);
      JLabel lockLabel = new JLabel("Read only mode (No write access to database allowed)");
      lockLabel.setFont(ATKConstant.labelFont);
      lockPanel.add(lockLabel);
      getContentPane().add(lockPanel,BorderLayout.NORTH);
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
    JSeparator sep1 = new JSeparator();
    JMenuItem exit = new JMenuItem("Exit");
    exit.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        exitForm();
      }
    });

    fileMenu.add(loadFile);
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

    JMenuItem chTangoHost = new JMenuItem("Change Tango Host");
    chTangoHost.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        changeTangoHost();
      }
    });
    editMenu.add(chTangoHost);

    editMenu.add(new JSeparator());

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
    dbInfoMenu.addActionListener(new ActionListener(){
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
    filterClass.addActionListener(new ActionListener(){
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

  // Show the clipbaord content
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
      serverTreePanel.selectServer(server);

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

  // Change the TANGO HOST
  private void changeTangoHost() {

    String th = JOptionPane.showInputDialog(this, "Enter tango host (ex gizmo:20000)", "Jive", JOptionPane.QUESTION_MESSAGE);
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
      } catch (DevFailed e) {
        JiveUtils.showTangoError(e);
        db = null;
      } catch (Exception e) {
        JiveUtils.showJiveError(e.getMessage());
        db=null;
      }

      ProgressFrame.displayProgress("Refresh in progress");
      serverTreePanel.setDatabase(db);
      ProgressFrame.setProgress("Refreshing...",20);
      deviceTreePanel.setDatabase(db);
      ProgressFrame.setProgress("Refreshing...",40);
      classTreePanel.setDatabase(db);
      ProgressFrame.setProgress("Refreshing...",60);
      aliasTreePanel.setDatabase(db);
      ProgressFrame.setProgress("Refreshing...",80);
      propertyTreePanel.setDatabase(db);
      ProgressFrame.setProgress("Refreshing...",100);
      historyDlg.setDatabase(db,th);
      updateTitle(th);
      defaultPanel.setSource(null);
      splitPane.setRightComponent(defaultPanel);
      ProgressFrame.hideProgress();


    }

  }

  // Refresh all trees
  private void refreshTree() {

    ProgressFrame.displayProgress("Refresh in progress");
    serverTreePanel.refresh();
    ProgressFrame.setProgress("Refreshing...",20);
    deviceTreePanel.refresh();
    ProgressFrame.setProgress("Refreshing...",40);
    classTreePanel.refresh();
    ProgressFrame.setProgress("Refreshing...",60);
    aliasTreePanel.refresh();
    ProgressFrame.setProgress("Refreshing...",80);
    propertyTreePanel.refresh();
    updateTabbedPane();

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

  // Display the history window
  public void showHistory() {

    if(!historyDlg.isVisible())
      ATKGraphicsUtils.centerFrameOnScreen(historyDlg);
    historyDlg.setVisible(true);

  }

  // Select a device a show the device tree panel
  public void goToDeviceNode(String devName) {
    deviceTreePanel.selectDevice(devName);
    treePane.setSelectedComponent(deviceTreePanel);
    // Work around X11 bug
    treePane.getSelectedComponent().setVisible(true);
  }

  // Select a server a show the server tree panel
  public void goToServerNode(String srvName) {
    serverTreePanel.selectServer(srvName);
    treePane.setSelectedComponent(serverTreePanel);
    // Work around X11 bug
    treePane.getSelectedComponent().setVisible(true);
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
        propertyTreePanel.refreshValues();
        break;
    }

  }

  // TreeListener
  void updatePanel(TangoNode[] source) {

    int i;

    // Check if there is some unsaved change
    try {

      PropertyPanel propertyPanel = (PropertyPanel)splitPane.getRightComponent();
      if( propertyPanel.hasChanged() ) {
        if( JOptionPane.showConfirmDialog(this,"Some properties has been updated and not saved.\nWould you like to save change ?","Confirmation",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION )
          propertyPanel.saveChange();
      }

    } catch(Exception e) {}


    // No selection
    if(source==null || source.length==0) {
      defaultPanel.setSource(null);
      splitPane.setRightComponent(defaultPanel);
      return;
    }

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
    } else if(nodeClass == TaskLoggingNode.class) {
      TaskLoggingNode[] nodes = new TaskLoggingNode[source.length];
      for(i=0;i<source.length;i++) nodes[i] = (TaskLoggingNode)source[i];
      deviceLoggingPanel.setSource(nodes);
      splitPane.setRightComponent(deviceLoggingPanel);
    } else {
      defaultPanel.setSource(source[0]);
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
    Dimension appsize = new Dimension(770, 500);
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
