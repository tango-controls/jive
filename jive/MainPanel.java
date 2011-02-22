package jive;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.Database;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoApi.ApiUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/**
 *
 * @author  pons
 */

public class MainPanel extends JFrame {

  // Global variables for MainPanel
  static ThreadDlg applyDlg;
  static TreePath[] applySelPath;
  static String applyValue;

  // Widget declarations
  private JTree mainTree = null;
  private DefaultTreeModel mainTreeModel;
  private DefaultMutableTreeNode rootNode;
  private TangoTreeNodeRenderer renderer;
  private MouseListener treeMousellistemner;
  private MouseListener valueMousellistemner;
  private TreeSelectionListener treeSelectionlistemner;
  private JScrollPane treeView;

  private JSplitPane splitPane;
  private JPanel    resPanel;
  private JPanel    btnPanel;
  private JTextArea resValue;
  private JScrollPane resView;
  private JButton applyPb;
  private JButton refreshPb;
  private JButton detailsPb;

  private Database db;
  private String tangoHost;
  private JLabel statusLine;
  private JMenuBar mainMenu;

  private JPopupMenu treeMenu;
  private JMenuItem[] treeMenuItem;
  private JMenuItem jMenuItemMDelete;
  private JMenuItem jMenuItemMCopy;
  private JMenuItem jMenuItemMSave;

  private JPopupMenu treeMenuMultiple;
  private JMenuItem  jMenuItemMDelete2;
  private JMenuItem  jMenuItemMCopy2;
  private JMenuItem  jMenuItemMSave2;

  // Control variable
  private boolean running_from_shell;
  private boolean refresh_values = true;
  private boolean searchInProgress = false;

  // Relase number (Let a space after the release number)
  final static private String appVersion = "Jive 2.7e ";

  // General constructor
  public MainPanel() {
    running_from_shell = false;
    initComponents();
    centerWindow();
    placeComponents();
    setTitle(appVersion);
    setVisible(true);
    JiveUtils.parent = this;
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
    placeComponents();
    setTitle(appVersion);
    setVisible(true);
    JiveUtils.parent = this;
  }

  // Center the window
  public void centerWindow() {
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Dimension scrsize = toolkit.getScreenSize();
    Dimension appsize = new Dimension(750, 500);
    int x = (scrsize.width - appsize.width) / 2;
    int y = (scrsize.height - appsize.height) / 2;
    setBounds(x, y, appsize.width, appsize.height);
  }

  // Return true when value is Editable
  private boolean isEditable(String name) {
    return name.startsWith("Property value") ||
      name.startsWith("Polling configuration property") ||
      name.startsWith("Attribute configuration property") ||
      name.startsWith("HDB configuration property") ||
      name.startsWith("Logging configuration property");
  }

  // Refresh res value text (single selection)
  private void refreshValues(TreePath selPath) {
    String[] values = JiveUtils.getValue(selPath);
    resValue.setText(values[0]);
    resValue.setCaretPosition(0);
    Border b = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),values[1]);
    resView.setBorder(b);
    resValue.setEditable(isEditable(values[1]) && !JiveUtils.readOnly);
    applyPb.setEnabled(false);
    detailsPb.setEnabled(false);
  }

  // Reset selection
  private void refreshEmptySelection() {
    resValue.setText("");
    Border b = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"...");
    resView.setBorder(b);
    resValue.setEditable(false);
    applyPb.setEnabled(false);
    detailsPb.setEnabled(false);
    statusLine.setText("");
    jMenuItemMDelete.setEnabled(false);
    jMenuItemMCopy.setEnabled(false);
    jMenuItemMSave.setEnabled(false);
    jMenuItemMDelete2.setEnabled(false);
    jMenuItemMCopy2.setEnabled(false);
    jMenuItemMSave2.setEnabled(false);
  }

  // Refresh res value text (multiple selection)
  private void refreshValuesMultiple(TreePath[] selPaths) {


    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

    String[] values = JiveUtils.getValue(selPaths[0]);

    boolean only_property_selected = isEditable(values[1]);
    boolean same_value = true;
    String res_value = values[0];
    int i;

    for (i = 1; i < selPaths.length; i++) {
      values = JiveUtils.getValue(selPaths[i]);
      same_value &= values[0].equals(res_value);
      only_property_selected &= isEditable(values[1]);
    }

    if (same_value)
      resValue.setText(res_value);
    else
      resValue.setText("Values are not equals");

    Border b = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Multiple selection");
    resView.setBorder(b);
    resValue.setEditable(only_property_selected && !JiveUtils.readOnly);
    applyPb.setEnabled(false);
    detailsPb.setEnabled(!same_value);

    this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
  }

  // Global value refresh
  private void gobalValueRefresh() {
    TreePath[] selPaths = mainTree.getSelectionPaths();
    if (selPaths != null) {
      if (selPaths.length == 1) {

        // Single selection
        String status = JiveUtils.formatPath(selPaths[0]);
        statusLine.setText(status);
        refreshValues(selPaths[0]);
        jMenuItemMDelete.setEnabled(false);
        jMenuItemMCopy.setEnabled(false);
        jMenuItemMSave.setEnabled(false);
        jMenuItemMDelete2.setEnabled(false);
        jMenuItemMCopy2.setEnabled(false);
        jMenuItemMSave2.setEnabled(false);

      } else if (selPaths.length > 1) {

        // Multiple selection
        statusLine.setText("Multiple selection: " + selPaths.length + " items selected");
        refreshValuesMultiple(selPaths);
        jMenuItemMDelete.setEnabled(!JiveUtils.readOnly);
        jMenuItemMCopy.setEnabled(!JiveUtils.readOnly);
        jMenuItemMSave.setEnabled(true);
        jMenuItemMDelete2.setEnabled(!JiveUtils.readOnly);
        jMenuItemMCopy2.setEnabled(!JiveUtils.readOnly);
        jMenuItemMSave2.setEnabled(true);

      } else if (selPaths.length == 0) {
        refreshEmptySelection();
      }
    } else {
      refreshEmptySelection();
    }
  }

  // Refresh the Tree
  private void refreshTree() {
    TreePath old_p = mainTree.getSelectionPath();
    splitPane.remove(treeView);
    createMainTree();
    splitPane.setLeftComponent(treeView);
    TreePath np = JiveUtils.convertOldPath(mainTreeModel, (TangoTreeNode) rootNode, old_p);
    mainTree.setSelectionPath(np);
    mainTree.expandPath(np);
    mainTree.makeVisible(np);
    mainTree.scrollPathToVisible(np);
    placeComponents();
  }

  // create the main tree
  private void createMainTree() {

    if (mainTree != null) ToolTipManager.sharedInstance().unregisterComponent(mainTree);
    renderer = new TangoTreeNodeRenderer();
    rootNode = new TangoTreeNode(0, 0, tangoHost, db, true);
    mainTreeModel = new DefaultTreeModel(rootNode);
    mainTree = new JTree(mainTreeModel);
    mainTree.setEditable(false);
    mainTree.setCellRenderer(renderer);
    //mainTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    mainTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    mainTree.setShowsRootHandles(true);
    mainTree.setBorder(BorderFactory.createLoweredBevelBorder());
    treeView = new JScrollPane(mainTree);
    mainTree.addMouseListener(treeMousellistemner);
    mainTree.addTreeSelectionListener(treeSelectionlistemner);
    ToolTipManager.sharedInstance().registerComponent(mainTree);
    ToolTipManager.sharedInstance().setDismissDelay(30000);
    ToolTipManager.sharedInstance().setInitialDelay(500);

  }

  // Show the clipbaord content
  public void showClipboard() {
    JiveUtils.the_clipboard.show(this);
  }

  // Show the details window
  public void showDetails() {
    TreePath[] selPaths = mainTree.getSelectionPaths();
    int i;

    if (selPaths != null) {

      Object rows[][] = new Object[selPaths.length][2];

      for (i = 0; i < selPaths.length; i++) {
        String[] values = JiveUtils.getValue(selPaths[i]);
        rows[i][0] = JiveUtils.formatPath(selPaths[i]);
        rows[i][1] = values[0];
      }

      DetailsDlg dlg = new DetailsDlg(this, rows, selPaths);
      dlg.showDlg();
    }
  }

  // Check the device name syntax
  public boolean checkDeviceName(String s) {
    if (s == null) return false;
    if (s.length() == 0) return false;
    //Check slash number
    int nb = 0;
    int i = 0;
    while (i < s.length() && nb <= 2) {
      if (s.charAt(i) == '/') nb++;
      i++;
    }
    return nb == 2;
  }

  // Apply change
  public void applyChange() {

    applySelPath = mainTree.getSelectionPaths();
    applyValue = resValue.getText();

    Thread doApply = new Thread() {
      public void run() {
        try {
          for (int i = 0; (i < applySelPath.length) && (!ThreadDlg.stopflag); i++) {
            JiveUtils.setValue(applySelPath[i], applyValue);
            applyDlg.setProgress(((i + 1) * 100) / applySelPath.length);
          }
          gobalValueRefresh();
        } catch (Exception e) {
          e.printStackTrace();
        }
        applyDlg.hideDlg();
      }
    };

    if (applySelPath != null) {
      if (applySelPath.length > 1) {
        // Display a progress window
        applyDlg = new ThreadDlg(this, "", true, doApply);
        applyDlg.showDlg();
      } else {
        for (int i = 0; (i < applySelPath.length); i++)
          JiveUtils.setValue(applySelPath[i], applyValue);
        gobalValueRefresh();
      }
    }

  }

  // Create the GUI
  private void initComponents() {

    getContentPane().setLayout(null);
    MultiLineToolTipUI.initialize();

    // *************************************************************
    // Initialise the Tango database
    // *************************************************************
    tangoHost = System.getProperty("TANGO_HOST", "null");

    if (tangoHost.equals("null") || tangoHost.equals("")) {
      System.out.println("TANGO_HOST no defined, exiting...");
      exitForm();
    }

    try {
      db = ApiUtil.get_db_obj();
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
      db = null;
    }

    if (JiveUtils.hdbEnabled) {
      try {
        JiveUtils.hdbManager = new DeviceProxy("Archivage/HdbManager/HdbManager");
        JiveUtils.hdbEnabled = true;
      } catch (DevFailed e) {
        //TangoTreeNode.showJiveError("Cannot connect to HdbManager, HDB support disabled.");
        JiveUtils.hdbEnabled = false;
      }
    }

    // *************************************************************
    // Resource value and apply/refresh buttons
    // *************************************************************
    splitPane = new JSplitPane();
    splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);

    resPanel = new JPanel(new BorderLayout());
    resPanel.setBorder(BorderFactory.createEtchedBorder());
    btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    resPanel.add(btnPanel,BorderLayout.SOUTH);
    resValue = new JTextArea();
    resValue.setBackground(Color.white);
    resValue.setForeground(Color.black);
    resValue.setOpaque(true);
    resValue.setText("");
    resValue.setEditable(false);
    resValue.setBorder(BorderFactory.createLoweredBevelBorder());
    resValue.setFont(new Font("Monospaced",Font.PLAIN,11));

    valueMousellistemner = new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3) {
          String sel = resValue.getSelectedText();
          if (checkDeviceName(sel)) {
            TangoTreeNode devnode = (TangoTreeNode) rootNode.getChildAt(2);
            // Search the server in the tree
            TangoTreeNode n = JiveUtils.findDeviceNode(devnode, sel);
            TreePath np;
            if (n != null) {
              np = n.getCompletePath();
              mainTree.setSelectionPath(np);
              mainTree.scrollPathToVisible(np);
            }
          }
        }
      }
    };
    resValue.addMouseListener(valueMousellistemner);

    resValue.getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e) {
        applyPb.setEnabled(!JiveUtils.readOnly);
      }

      public void removeUpdate(DocumentEvent e) {
        applyPb.setEnabled(!JiveUtils.readOnly);
      }

      public void changedUpdate(DocumentEvent e) {
        applyPb.setEnabled(!JiveUtils.readOnly);
      }

      public void updateLog(DocumentEvent e, String action) {
        applyPb.setEnabled(!JiveUtils.readOnly);
      }
    });

    resView = new JScrollPane(resValue);

    Border b = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"...");
    resView.setBorder(b);
    resPanel.add(resView,BorderLayout.CENTER);

    applyPb = new JButton();
    applyPb.setText("Apply change");
    applyPb.setEnabled(false);
    applyPb.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        if(applyPb.isEnabled()) applyChange();
      }
    });
    btnPanel.add(applyPb);

    detailsPb = new JButton();
    detailsPb.setText("Show details");
    detailsPb.setEnabled(false);
    detailsPb.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        if(detailsPb.isEnabled()) showDetails();
      }
    });
    btnPanel.add(detailsPb);

    refreshPb = new JButton();
    refreshPb.setText("Refresh");
    btnPanel.add(refreshPb);
    refreshPb.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        gobalValueRefresh();
      }
    });

    splitPane.setRightComponent(resPanel);

    // *************************************************************
    // statusLine
    // *************************************************************
    statusLine = new JLabel();
    statusLine.setFont(new Font("Dialog",Font.PLAIN,12));
    statusLine.setBackground(Color.white);
    statusLine.setForeground(Color.black);
    statusLine.setOpaque(true);
    statusLine.setText("");
    statusLine.setBorder(BorderFactory.createLoweredBevelBorder());
    getContentPane().add(statusLine);

    // *************************************************************
    // mainTree
    // *************************************************************

    // Listen for mouse click on the tree
    treeMousellistemner = new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        TreePath selPath = mainTree.getPathForLocation(e.getX(), e.getY());
        int selRow = mainTree.getRowForLocation(e.getX(), e.getY());
        if (selRow != -1) {
          if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3) {
            if (selPath != null) {

              // Check if the user click on the selection
              boolean[] menuState;
              boolean found=false;
              int i=0;
              while(i<mainTree.getSelectionCount() && !found) {
                found=mainTree.getSelectionPaths()[i].equals(selPath);
                if(!found) i++;
              }

              // Force single selection on right click
              // when click is made outside the selection
              if (mainTree.getSelectionCount() <= 1 || !found) {
                mainTree.setSelectionPath(selPath);
                menuState = JiveUtils.getAction(selPath);
                for (i = 0; i < menuState.length; i++) {
                  treeMenuItem[i].setEnabled(menuState[i]);
                  if (i > 3) treeMenuItem[i].setVisible(menuState[i]);
                }
                treeMenu.show(mainTree, e.getX(), e.getY());
              } else {
                boolean copyOk = true;
                boolean deleteOk = true;
                boolean saveOk = true;
                for(i=0;i<mainTree.getSelectionCount();i++) {
                  menuState = JiveUtils.getAction(mainTree.getSelectionPaths()[i]);
                  copyOk   = copyOk && menuState[1];
                  deleteOk = deleteOk && menuState[3];
                  saveOk = saveOk = menuState[15];
                }
                jMenuItemMCopy2.setEnabled(copyOk);
                jMenuItemMDelete2.setEnabled(deleteOk);
                jMenuItemMSave2.setEnabled(saveOk);
                treeMenuMultiple.show(mainTree, e.getX(), e.getY());
              }

            }
          }
        }
      }
    };

    //Listen for selection changes.
    treeSelectionlistemner = new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        if (refresh_values) {
          gobalValueRefresh();
        }
      }
    };

    createMainTree();
    splitPane.setLeftComponent(treeView);
    getContentPane().add(splitPane);

    // *************************************************************
    // MenuBar
    // *************************************************************

    mainMenu = new JMenuBar();
    JMenu jMenu1 = new JMenu();
    JMenu jMenu2 = new JMenu();
    JMenuItem jMenuItem1 = new JMenuItem();
    JMenuItem jMenuItem2 = new JMenuItem();
    JMenuItem jMenuItem3 = new JMenuItem();
    JMenuItem jMenuItem4 = new JMenuItem();
    jMenuItemMDelete = new JMenuItem();
    jMenuItemMCopy = new JMenuItem();
    jMenuItemMSave = new JMenuItem();
    JMenuItem jMenuItem7 = new JMenuItem();
    JMenuItem jMenuItem8 = new JMenuItem();
    JMenuItem jMenuItem9 = new JMenuItem();
    JMenuItem jMenuItem10 = new JMenuItem();

    jMenu1.setText("File");
    jMenu2.setText("Edit");
    jMenuItem1.setText("Exit");
    jMenuItem2.setText("Refresh tree");
    jMenuItem3.setText("Show clipbaord");
    jMenuItem4.setText("Clear clipbaord");
    jMenuItemMDelete.setText("Multiple delete");
    jMenuItemMCopy.setText("Multiple copy");
    jMenuItemMSave.setText("Multiple save");
    jMenuItem7.setText("Multiple selection");
    jMenuItem8.setText("Find property");
    jMenuItem9.setText("Find next");
    jMenuItem10.setText("Load property file");
    jMenuItem10.setEnabled(!JiveUtils.readOnly);

    // Exit Application
    jMenuItem1.addActionListener(new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        exitForm();
      }
    });

    // refresh the tree
    jMenuItem2.addActionListener(new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        refreshTree();
      }
    });
    jMenuItem2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));

    // Show the cliboard
    jMenuItem3.addActionListener(new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        showClipboard();
      }
    });

    // Clear the cliboard
    jMenuItem4.addActionListener(new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        JiveUtils.the_clipboard.clear();
      }
    });

    // Multiple delete
    jMenuItemMDelete.addActionListener(new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        execMultipleAction(3);
      }
    });

    // Multiple copy
    jMenuItemMCopy.addActionListener(new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        execMultipleAction(1);
      }
    });

    // Multiple save
    jMenuItemMSave.addActionListener(new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        execMultipleAction(15);
      }
    });

    // Multiple selection
    jMenuItem7.addActionListener(new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (!searchInProgress) {
          searchInProgress = true;
          selectProperties();
          searchInProgress = false;
        }
      }
    });

    // Find property
    jMenuItem8.addActionListener(new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (!searchInProgress) {
          searchInProgress = true;
          findProperty(0);
          searchInProgress = false;
        }
      }
    });

    // Find next
    jMenuItem9.addActionListener(new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (!searchInProgress) {
          searchInProgress = true;
          findProperty(1);
          searchInProgress = false;
        }
      }
    });
    jMenuItem9.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));

    // Load property file
    jMenuItem10.addActionListener(new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        loadPropFile();
      }
    });

    // Multiple action not enabled by default
    jMenuItemMDelete.setEnabled(false);
    jMenuItemMCopy.setEnabled(false);
    jMenuItemMSave.setEnabled(false);

    jMenu1.add(jMenuItem10);
    jMenu1.add(new JSeparator());
    jMenu1.add(jMenuItem1);
    jMenu2.add(jMenuItem2);
    jMenu2.add(jMenuItem3);
    jMenu2.add(jMenuItem4);
    jMenu2.add(jMenuItem7);
    jMenu2.add(new JSeparator());
    jMenu2.add(jMenuItemMDelete);
    jMenu2.add(jMenuItemMCopy);
    jMenu2.add(jMenuItemMSave);
    jMenu2.add(new JSeparator());
    jMenu2.add(jMenuItem8);
    jMenu2.add(jMenuItem9);

    mainMenu.add(jMenu1);
    mainMenu.add(jMenu2);
    setJMenuBar(mainMenu);

    //**************************************************************
    // Popup menu
    //**************************************************************
    treeMenu = new JPopupMenu();
    treeMenuItem = new JMenuItem[JiveUtils.nbAction];

    for (int i = 0; i < JiveUtils.nbAction; i++) {
      treeMenuItem[i] = new JMenuItem();
      treeMenuItem[i].setEnabled(false);

      treeMenuItem[i].addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          execMenuAction(evt);
        }
      });
    }

    treeMenuItem[0].setText("Cut");
    treeMenuItem[1].setText("Copy");
    treeMenuItem[2].setText("Paste");
    treeMenuItem[3].setText("Delete");
    treeMenuItem[4].setText("Add device");
    treeMenuItem[5].setText("Create server");
    treeMenuItem[6].setText("Create new property");
    treeMenuItem[7].setText("Change TANGO HOST");
    treeMenuItem[8].setText("Add class to server");
    treeMenuItem[9].setText("Rename");
    treeMenuItem[10].setText("Monitor device");
    treeMenuItem[11].setText("Test device");
    treeMenuItem[12].setText("Show properties");
    treeMenuItem[13].setText("Test admin server");
    treeMenuItem[14].setText("Unexport devices");
    treeMenuItem[15].setText("Save server data");
    treeMenuItem[16].setText("Define device alias");
    treeMenuItem[17].setText("Go to server node");
    treeMenuItem[18].setText("Go to device node");
    treeMenuItem[19].setText("Restart device");
    treeMenuItem[20].setText("Reset to default value");
    treeMenuItem[21].setText("Go to device admin node");
    treeMenuItem[22].setText("Change Archiving mode");
    treeMenuItem[23].setText("Create class attribute");
    treeMenuItem[24].setText("Server wizard");
    treeMenuItem[25].setText("Classes wizard");
    treeMenuItem[26].setText("Devices wizard");
    treeMenuItem[27].setText("Device wizard");

    for (int i = 0; i < JiveUtils.nbAction; i++) {
      if (i == 4) treeMenu.add(new JSeparator());
      treeMenu.add(treeMenuItem[i]);
    }

    treeMenuMultiple = new JPopupMenu();
    jMenuItemMDelete2 = new JMenuItem();
    jMenuItemMCopy2 = new JMenuItem();
    jMenuItemMSave2 = new JMenuItem();
    jMenuItemMDelete2.setText("Delete");
    jMenuItemMCopy2.setText("Copy");
    jMenuItemMSave2.setText("Save servers");
    treeMenuMultiple.add(jMenuItemMCopy2);
    treeMenuMultiple.add(jMenuItemMDelete2);
    treeMenuMultiple.add(jMenuItemMSave2);

    // Multiple delete
    jMenuItemMDelete2.addActionListener(new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        execMultipleAction(3);
      }
    });

    // Multiple copy
    jMenuItemMCopy2.addActionListener(new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        execMultipleAction(1);
      }
    });

    // Multiple Save
    jMenuItemMSave2.addActionListener(new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        execMultipleAction(15);
      }
    });

    //**************************************************************
    // Component listener
    //**************************************************************
    addComponentListener(new ComponentListener() {
      public void componentHidden(ComponentEvent e) {
        exitForm();
      }

      public void componentMoved(ComponentEvent e) {

        // Move the tree editor when window move
        if (JiveUtils.dlg != null) {
          TreePath selPath = mainTree.getSelectionPath();
          if (selPath != null) {
            mainTree.makeVisible(selPath);
            Rectangle r = mainTree.getPathBounds(selPath);
            Point pto = r.getLocation();
            SwingUtilities.convertPointToScreen(pto, mainTree);
            JiveUtils.dlg.moveToLocation(pto.x, pto.y);
          }
        }
      }

      public void componentResized(ComponentEvent e) {
        placeComponents();
      }

      public void componentShown(ComponentEvent e) {
        placeComponents();
      }
    });

    ImageIcon icon = new ImageIcon(getClass().getResource("/jive/jive.jpg"));
    setIconImage(icon.getImage());

  }

  // Load a property file in the database
  public void loadPropFile() {
    String err = "";
    TangoFileReader fr = new TangoFileReader(db);

    JFileChooser chooser = new JFileChooser(".");
    int returnVal = chooser.showOpenDialog(this);

    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File f = chooser.getSelectedFile();
      if (f != null) err = fr.parse_res_file(f.getAbsolutePath());
      if (err.length() > 0) JiveUtils.showJiveError(err);
    }
  }

  /* Execute an action from the popup menu */
  // action 0 = cut
  // action 1 = copy
  // action 2 = paste
  // action 3 = delete
  // action 4 = add device
  // action 5 = create server
  // action 6 = create new property
  // action 7 = change tango host
  // action 8 = add class to server
  // action 9 = Rename
  // action 10= Monitor a device
  // action 11= Test a device
  // action 12= Show properties
  // action 13= Test admin server
  // action 14= Unexport devices
  // action 15= Save server data
  // action 16= Define device alias
  // action 17= Go to server node
  // action 18= Go to device node
  // action 19= Restart device
  // action 20= Restore default value
  // action 21= Go to device admin node
  // action 22= Change HDB mode
  // action 23= Create class attribute

  public void execMenuAction(ActionEvent evt) {

    JMenuItem sel = (JMenuItem) evt.getSource();
    int i = 0;
    boolean found = false;
    while (i < JiveUtils.nbAction && !found) {
      found = (sel == treeMenuItem[i]);
      if (!found) i++;
    }

    // Treat "Change TANGO_HOST" here
    if (i == 7) {

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

        tangoHost = th;

        try {
          db = ApiUtil.change_db_obj(ths[0],ths[1]);
        } catch (DevFailed e) {
          JiveUtils.showTangoError(e);
          db = null;
        } catch (Exception e) {
          JiveUtils.showJiveError(e.getMessage());
          db=null;
        }

        refreshTree();
      }

    }

    TreePath selPath = mainTree.getSelectionPath();
    if (selPath != null) {
      Rectangle r = JiveUtils.computeBounds(mainTree, selPath);
      boolean needRefresh = JiveUtils.execAction(i, selPath, mainTree, mainTreeModel, r, 0, 1);
      if( needRefresh ) refreshTree();
    }


  }

  /* Execute action on multiple selection */
  public void execMultipleAction(int action) {

    Rectangle r = new Rectangle();
    TreePath[] selPaths = mainTree.getSelectionPaths();

    // Clear clipbaord before copy
    if(action==1)
      JiveUtils.the_clipboard.clear();

    if(action==3)
      if( JOptionPane.showConfirmDialog(this, "Delete multiple selection ?", "Confirm delete", JOptionPane.YES_NO_OPTION) !=
        JOptionPane.YES_OPTION )
      return;

    if (selPaths != null)
      for (int i = 0; i < selPaths.length; i++)
        JiveUtils.execAction(action + 100, selPaths[i], mainTree, mainTreeModel, r, i, selPaths.length);

  }

  /* Place components */
  public void placeComponents() {

    Dimension d = getContentPane().getSize();
    int w = d.width - 10;
    int h = d.height;
    statusLine.setBounds(5, 2, w, 20);
    splitPane.setBounds(5,25,w,h-30);
    splitPane.setDividerLocation(0.4);

  }

  /* Find property a=0 New search a=1 Continue search */
  public void findProperty(int a) {

    if (a == 0) {

      TangoTreeNode start_node;
      JiveUtils.scan_progress = 0;
      TreePath selPath = mainTree.getSelectionPath();
      if (selPath == null)
        start_node = (TangoTreeNode) rootNode;
      else
        start_node = (TangoTreeNode) selPath.getLastPathComponent();

      SearchDlg dlg = new SearchDlg(this, true, "Search Tango data base", "Text to find");

      if (!dlg.showDlg())
        return;

      JiveUtils.InitiateSearch(start_node,
        SearchDlg.textToFind,
        SearchDlg.textToFindValue,
        SearchDlg.dIgnoreCase,
        SearchDlg.dSearchValues,
        SearchDlg.dSearchAttributes,
        SearchDlg.dSearchCommands,
        SearchDlg.dSearchUseRegexp,
        false);

    }

    // Try to find a text in the tree

    JiveUtils.error_report = false;
    TreePath found = JiveUtils.findText();

    if (found != null) {
      mainTree.setSelectionPath(found);
      mainTree.scrollPathToVisible(found);
    }

    JiveUtils.error_report = true;
    if (found == null) JiveUtils.showJiveError("No item found...");


  }

  /* select multiple properties */
  public void selectProperties() {

    JiveUtils.scan_progress = 0;
    applySelPath = mainTree.getSelectionPaths();

    if (applySelPath == null) {
      JiveUtils.showJiveError("Please select starting nodes");
      return;
    }

    SearchDlg dlg = new SearchDlg(this, true, "Multiple selection", "Select");

    if (!dlg.showDlg()) return;

    // Searching and selecting thread
    Thread doApply = new Thread() {
      public void run() {
        try {

          int i,j;
          java.util.Vector foundPaths = new java.util.Vector();

          // Find properties
          for (i = 0; i < applySelPath.length && (!ThreadDlg.stopflag); i++) {

            TangoTreeNode start_node = (TangoTreeNode) applySelPath[i].getLastPathComponent();

            JiveUtils.InitiateSearch(start_node,
              SearchDlg.textToFind,
              SearchDlg.textToFindValue,
              SearchDlg.dIgnoreCase,
              SearchDlg.dSearchValues,
              SearchDlg.dSearchAttributes,
              SearchDlg.dSearchCommands,
              SearchDlg.dSearchUseRegexp,
              true);


            JiveUtils.error_report = false;
            TreePath[] found = JiveUtils.findMultipleText();

            if (found != null)
              for (j = 0; j < found.length; j++)
                foundPaths.add(found[j]);

            applyDlg.setProgress((int) (((double) (i + 1) / (double) applySelPath.length) * 50.0));

          }

          mainTree.clearSelection();

          // Abort search
          if (ThreadDlg.stopflag) {
            applyDlg.hideDlg();
            return;
          }

          // Do not refresh during multiple selection update
          refresh_values = false;

          // Select values
          int step = foundPaths.size() / 10 + 1;

          for (i = 0; i < foundPaths.size() && (!ThreadDlg.stopflag); i += step) {

            TreePath[] ps = null;

            if (i + step < foundPaths.size())
              ps = new TreePath[step];
            else
              ps = new TreePath[foundPaths.size() - i];

            for (j = 0; j < ps.length; j++) ps[j] = (TreePath) foundPaths.get(i + j);

            mainTree.addSelectionPaths(ps);
            applyDlg.setProgress((int) (((double) (i + 1) / (double) foundPaths.size()) * 50.0 + 50.0));

          }

          if (foundPaths.size() > 0)
            mainTree.scrollPathToVisible((TreePath) foundPaths.get(0));

          JiveUtils.error_report = true;

          gobalValueRefresh();

        } catch (Exception e) {
          e.printStackTrace();
        }
        refresh_values = true;
        applyDlg.hideDlg();
      }
    };

    applyDlg = new ThreadDlg(this, "", true, doApply);
    applyDlg.showDlg();

  }

  /* Exit the Application */
  private void exitForm() {
    if (running_from_shell)
      System.exit(0);
    else {
      setVisible(false);
      dispose();
    }
  }

  /* main */
  public static void main(String args[]) {
    //System.setProperty("TANGO_HOST", "gizmo:20000");
    //JFrame.setDefaultLookAndFeelDecorated(true);
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
