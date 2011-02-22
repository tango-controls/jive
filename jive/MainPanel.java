package jive;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import fr.esrf.TangoApi.*;
import fr.esrf.Tango.*;
import fr.esrf.TangoDs.*;

/**
 *
 * @author  pons
 */

public class MainPanel extends JFrame {

    // Global variables for MainPanel
    
    // Widget declarations
    private JTree            mainTree;
    private DefaultTreeModel mainTreeModel;
    private DefaultMutableTreeNode rootNode;
    private MouseListener         treeMousellistemner; 
    private TreeSelectionListener treeSelectionlistemner;
    private JScrollPane      treeView;
    
    private JTextArea        resValue;
    private JScrollPane      resView;
    private JButton          applyPb;
    private JButton          refreshPb;
    private JButton          detailsPb;

    private Database         db;
    private String           tangoHost;
    private JLabel           statusLine;
    private JMenuBar 	     mainMenu;

    private  JPopupMenu       treeMenu;
    private  JMenuItem[]      treeMenuItem;
    private  JMenuItem jMenuItem5;
    private  JMenuItem jMenuItem6;

    // Control variable    
    private boolean running_from_shell;
        
    // General constructor
    public MainPanel() {        
	running_from_shell = false;
        initComponents();
	setSize(650,480);
	placeComponents();
	setTitle("Jive 2.0 [beta release]");
	setVisible(true);
	TangoTreeNode.parent = this;
    }
    
    // General constructor
    public MainPanel(int dummy) {        
	running_from_shell = true;	
        initComponents();
	setSize(650,480);
	placeComponents();
	setTitle("Jive 2.0 [beta release]");
	setVisible(true);
	TangoTreeNode.parent = this;
    }
        
    // Refresh res value text (single selection)
    private void refreshValues(TreePath selPath) {
	String[] values=TangoTreeNode.getValue(selPath);
        resValue.setText(values[0]);
	resView.setBorder( BorderFactory.createTitledBorder(values[1]) );
	resValue.setEnabled( values[1].startsWith("Property value") );
        applyPb.setEnabled(false);	      
	detailsPb.setEnabled(false);
    }
    
    // Refresh res value text (multiple selection)
    private void refreshValuesMultiple(TreePath[] selPaths) {
    
	String[] values=TangoTreeNode.getValue(selPaths[0]);

        boolean only_property_selected = values[1].startsWith("Property value");
        boolean same_value             = true;
	String  res_value              = values[0];
	int     i;
		
	for(i=1;i<selPaths.length;i++) {
	  values=TangoTreeNode.getValue(selPaths[i]);
	  same_value             &= values[0].equals(res_value);
	  only_property_selected &= values[1].startsWith("Property value");
	}
	
        if( same_value ) resValue.setText(res_value);
	else             resValue.setText("Values are not equals");
	
	resView.setBorder( BorderFactory.createTitledBorder("Multiple selection") );
	resValue.setEnabled( only_property_selected );
        applyPb.setEnabled(false);
	detailsPb.setEnabled(!same_value);
    }
    
    // Global value refresh
    private void gobalValueRefresh() {    
      TreePath[] selPaths = mainTree.getSelectionPaths();	    	    
      if( selPaths!=null ) {
	if( selPaths.length == 1 ) {
	
	  // Single selection
	  String status=TangoTreeNode.formatPath(selPaths[0]);
	  statusLine.setText(status);
	  refreshValues(selPaths[0]);
          jMenuItem5.setEnabled(false);
          jMenuItem6.setEnabled(false);
	
	} else if (selPaths.length > 1) {
	
	  // Multiple selection
	  statusLine.setText("Multiple selection: " + selPaths.length + " items selected");
	  refreshValuesMultiple(selPaths);
          jMenuItem5.setEnabled(true);
          jMenuItem6.setEnabled(true);
	  
	}  
      }    
    }
    
    // Refresh the Tree
    private void refreshTree() {
	TreePath old_p = mainTree.getSelectionPath();
	getContentPane().remove(treeView);
	createMainTree();
	getContentPane().add(treeView);	    
	TreePath np = TangoTreeNode.convertOldPath(mainTreeModel,(TangoTreeNode)rootNode,old_p);
    	mainTree.setSelectionPath(np);
    	mainTree.expandPath(np);
    	mainTree.makeVisible(np);
   	placeComponents();
    }
    
    // create the main tree
    private void createMainTree() {
    
	rootNode = new TangoTreeNode(0,0,tangoHost,db);
        mainTreeModel = new DefaultTreeModel(rootNode);
	mainTree = new JTree(mainTreeModel);	
	mainTree.setEditable(false);
 	//mainTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
 	mainTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);	
	mainTree.setShowsRootHandles(true);
	mainTree.setBorder( BorderFactory.createLoweredBevelBorder() );
	treeView = new JScrollPane(mainTree);
	mainTree.addMouseListener(treeMousellistemner);
	mainTree.addTreeSelectionListener(treeSelectionlistemner);
	
    }
    
    // Show the clipbaord content
    public void showClipboard() {
      TangoTreeNode.the_clipboard.show(this);
    }
    
    // Show the details window
    public void showDetails() {
       TreePath[] selPaths = mainTree.getSelectionPaths();
       int i,j,l,max;
	    
	if( selPaths!=null ) {
	 
	  Object rows[][] = new Object[selPaths.length][2];
	   	 
	  for(i=0;i<selPaths.length;i++) {
	    String[] values = TangoTreeNode.getValue(selPaths[i]);
	    rows[i][0] = TangoTreeNode.formatPath(selPaths[i]);
	    rows[i][1] = values[0];
	  }
	  
	  DetailsDlg dlg = new DetailsDlg(this,rows,selPaths);
	  dlg.show();
	}		    
    }
    
    // Create the GUI    
    private void initComponents() {
    
	getContentPane().setLayout( null );
	
	// *************************************************************
	// Initialise the Tango database
	// *************************************************************
	tangoHost = System.getProperty("TANGO_HOST","null");
	
	if( tangoHost.equals("null") || tangoHost.equals("") ) {
	  System.out.println("TANGO_HOST no defined, exiting...");
	  exitForm();
	}
	
	try {
          db = new Database();
        } catch (DevFailed e) {
	    TangoTreeNode.showTangoError(e);
	    db = null;
        }
	
	// *************************************************************
	// Resource value and apply/refresh buttons
	// *************************************************************
	resValue = new JTextArea();
	resValue.setBackground( Color.white );
	resValue.setForeground( Color.black );
	resValue.setOpaque(true);
	resValue.setText("");
	resValue.setEnabled(false);
	resValue.setBorder( BorderFactory.createLoweredBevelBorder() );
	resView = new JScrollPane(resValue);
	resView.setBorder( BorderFactory.createTitledBorder("...") );
	getContentPane().add(resView);
	
	resValue.getDocument().addDocumentListener( new DocumentListener() {
	  public void insertUpdate(DocumentEvent e) {
            applyPb.setEnabled(true);
	  }
          public void removeUpdate(DocumentEvent e) {
            applyPb.setEnabled(true);
          }
          public void changedUpdate(DocumentEvent e) {
            applyPb.setEnabled(true);
          }
          public void updateLog(DocumentEvent e, String action) {
            applyPb.setEnabled(true);
          }
	});
	
        applyPb = new JButton();
	applyPb.setText("Apply change");
	applyPb.setEnabled(false);
	applyPb.addMouseListener(new java.awt.event.MouseAdapter() {
          public void mouseClicked(java.awt.event.MouseEvent evt) {
            TreePath[] selPaths = mainTree.getSelectionPaths();
	    String value=resValue.getText();
	    if( selPaths!=null ) {
	      for(int i=0;i<selPaths.length;i++)
	        TangoTreeNode.setValue(selPaths[i],value);
	    }	    
	    gobalValueRefresh();
          }
        });
	getContentPane().add(applyPb);
	
        detailsPb = new JButton();
	detailsPb.setText("Show details");
	detailsPb.setEnabled(false);
	detailsPb.addMouseListener(new java.awt.event.MouseAdapter() {
          public void mouseClicked(java.awt.event.MouseEvent evt) {
	    showDetails();
          }
        });
	getContentPane().add(detailsPb);
	
        refreshPb = new JButton();
	refreshPb.setText("Refresh");
	getContentPane().add(refreshPb);
	refreshPb.addMouseListener(new java.awt.event.MouseAdapter() {
          public void mouseClicked(java.awt.event.MouseEvent evt) {
	    gobalValueRefresh();
          }
        });
	
	// *************************************************************
	// statusLine
	// *************************************************************
	statusLine = new JLabel();
	statusLine.setBackground( Color.white );
	statusLine.setForeground( Color.black );
	statusLine.setOpaque(true);
	statusLine.setText("");
	statusLine.setBorder( BorderFactory.createLoweredBevelBorder() );
	getContentPane().add(statusLine);

	// *************************************************************
	// mainTree
        // *************************************************************        
	
	// Listen for mouse click on the tree
	treeMousellistemner = new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
           TreePath selPath = mainTree.getPathForLocation(e.getX(), e.getY());
           int      selRow  = mainTree.getRowForLocation(e.getX(), e.getY());
           if(selRow != -1) {
             if(e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3) {
               if( selPath!=null ) {
	          // Force single selection on right click
	          mainTree.setSelectionPath(selPath);
	          boolean[] menuState = TangoTreeNode.getAction(selPath);
		  for(int i=0;i<menuState.length;i++) {        
	            treeMenuItem[i].setEnabled(menuState[i]);
	            if(i>3) treeMenuItem[i].setVisible(menuState[i]);
		  }
	          treeMenu.show(mainTree,e.getX(),e.getY());
	       }	        
             }
           }
         }
        };
	
        //Listen for selection changes.
        treeSelectionlistemner = new TreeSelectionListener() {
          public void valueChanged(TreeSelectionEvent e) {
	    gobalValueRefresh();
    	  }
	};
	
	createMainTree();
	getContentPane().add(treeView);

        // *************************************************************
	// MenuBar
        // *************************************************************
	 
	mainMenu  	      = new JMenuBar();
        JMenu     jMenu1      = new JMenu();
        JMenu     jMenu2      = new JMenu();
        JMenuItem jMenuItem1  = new JMenuItem();
        JMenuItem jMenuItem2  = new JMenuItem();
        JMenuItem jMenuItem3  = new JMenuItem();
        JMenuItem jMenuItem4  = new JMenuItem();
        jMenuItem5  = new JMenuItem();
        jMenuItem6  = new JMenuItem();
        JMenuItem jMenuItem7  = new JMenuItem();
        JMenuItem jMenuItem8  = new JMenuItem();
        JMenuItem jMenuItem9  = new JMenuItem();

        jMenu1.setText ("File");
        jMenu2.setText ("Edit");
        jMenuItem1.setText ("Exit");
        jMenuItem2.setText ("Refresh tree");
        jMenuItem3.setText ("Show clipbaord");
        jMenuItem4.setText ("Clear clipbaord");
        jMenuItem5.setText ("Multiple delete");
        jMenuItem6.setText ("Multiple copy");
        jMenuItem7.setText ("Multiple selection");
        jMenuItem8.setText ("Find property");
        jMenuItem9.setText ("Find next");

        // Exit Application	
        jMenuItem1.addActionListener (new ActionListener () {
          public void actionPerformed (java.awt.event.ActionEvent evt) {
             exitForm();
          }
        } );
	
	// refresh the tree
        jMenuItem2.addActionListener (new ActionListener () {
          public void actionPerformed (java.awt.event.ActionEvent evt) {	    
	    refreshTree();
          }
        } );
	jMenuItem2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5,0));
	
	// Show the cliboard
        jMenuItem3.addActionListener (new ActionListener () {
          public void actionPerformed (java.awt.event.ActionEvent evt) {	    
	    showClipboard();
          }
        } );

	// Clear the cliboard
        jMenuItem4.addActionListener (new ActionListener () {
          public void actionPerformed (java.awt.event.ActionEvent evt) {	    
	    TangoTreeNode.the_clipboard.clear();
          }
        } );
	
	// Multiple delete
        jMenuItem5.addActionListener (new ActionListener () {
          public void actionPerformed (java.awt.event.ActionEvent evt) {	    
    	     execMultipleAction(3);
          }
        } );
	
	// Multiple copy
        jMenuItem6.addActionListener (new ActionListener () {
          public void actionPerformed (java.awt.event.ActionEvent evt) {	    
    	     execMultipleAction(1);
          }
        } );
	
	// Multiple selection
        jMenuItem7.addActionListener (new ActionListener () {
          public void actionPerformed (java.awt.event.ActionEvent evt) {
          selectProperties();
	  }
        } );

	// Find property
        jMenuItem8.addActionListener (new ActionListener () {
          public void actionPerformed (java.awt.event.ActionEvent evt) {
	  findProperty(0);	    
          }
        } );

	// Find next
        jMenuItem9.addActionListener (new ActionListener () {
          public void actionPerformed (java.awt.event.ActionEvent evt) {
	  findProperty(1);	    
          }
        } );	
	jMenuItem9.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3,0));

        // Multiple action not enabled by default
        jMenuItem5.setEnabled(false);
        jMenuItem6.setEnabled(false);
        	
	jMenu1.add (jMenuItem1);
	jMenu2.add (jMenuItem2);
	jMenu2.add (jMenuItem3);
	jMenu2.add (jMenuItem4);
	jMenu2.add (jMenuItem7);
	jMenu2.add (new JSeparator());
	jMenu2.add (jMenuItem5);
	jMenu2.add (jMenuItem6);
	jMenu2.add (new JSeparator());
	jMenu2.add (jMenuItem8);
	jMenu2.add (jMenuItem9);
	
	mainMenu.add(jMenu1);
	mainMenu.add(jMenu2);
	setJMenuBar(mainMenu);
	
	//**************************************************************
	// Popup menu
	//**************************************************************
	treeMenu     = new JPopupMenu();
	treeMenuItem = new JMenuItem[TangoTreeNode.nbAction];
	
	for(int i=0;i<TangoTreeNode.nbAction;i++) {
          treeMenuItem[i]=new JMenuItem();
	  treeMenuItem[i].setEnabled(false);
          
	  treeMenuItem[i].addActionListener (new ActionListener () {            
	    public void actionPerformed (ActionEvent evt) {
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
		
	for(int i=0;i<TangoTreeNode.nbAction;i++) {
	  if(i==4) treeMenu.add(new JSeparator());
	  treeMenu.add(treeMenuItem[i]);
	}
		
	//**************************************************************
        // Component listener
	//**************************************************************
	addComponentListener( new ComponentListener() {
          public void componentHidden(ComponentEvent e) {} 
          public void componentMoved(ComponentEvent e) {
	  
	   // Move the tree editor when window move
	   if( TangoTreeNode.dlg != null ) {
	     TreePath selPath = mainTree.getSelectionPath();
	     if( selPath != null ) {
	       mainTree.makeVisible(selPath);
	       Rectangle r  = mainTree.getPathBounds(selPath);
	       Point pto = r.getLocation();
	       SwingUtilities.convertPointToScreen(pto,mainTree);
               TangoTreeNode.dlg.moveToLocation(pto.x,pto.y);
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
    
    public void execMenuAction(ActionEvent evt) {    
       
       JMenuItem sel = (JMenuItem) evt.getSource();
       int i=0;
	boolean found=false;
	while(i<TangoTreeNode.nbAction && !found) {
	  found = (sel==treeMenuItem[i]);
	  if(!found) i++;
        }
	
	// Treat "Change TANGO_HOST" here
	if( i==7 ) {
	
	  String th = JOptionPane.showInputDialog(this,"Enter tango host (ex gizmo:20000)","Jive",JOptionPane.QUESTION_MESSAGE);
	  System.setProperty("TANGO_HOST", th);
	  tangoHost = th;
	  	
	  try {
            db = new Database();
          } catch (DevFailed e) {
	    TangoTreeNode.showTangoError(e);
	    db = null;
          }
	  
	  refreshTree();
	  	  
	}
	      
	TreePath selPath = mainTree.getSelectionPath();
	if( selPath != null ) {
	  mainTree.makeVisible(selPath);
	  Rectangle r  = mainTree.getPathBounds(selPath);
	  Point pto = r.getLocation();
	  SwingUtilities.convertPointToScreen(pto,mainTree);
          r.setLocation(pto);
	  r.width += 20;
	  TangoTreeNode.execAction(i,selPath,mainTree,mainTreeModel,r);
	  
	  // Refresh tree on some action
	  switch(i) {
	    case 2:
	    case 5:
	    case 8:
	      refreshTree();
	      break;
	  }
	  
	}
	
	
    }
    
    /* Execute action on multiple selection */    
    public void execMultipleAction(int action) {  
        
	  Rectangle r  = new Rectangle();
	  TreePath[] selPaths = mainTree.getSelectionPaths();	    
	  if( selPaths != null )
	    for(int i=0;i<selPaths.length;i++)
	      TangoTreeNode.execAction(action+100,selPaths[i],mainTree,mainTreeModel,r);	
    
    }

    /* Place components */
    public void placeComponents() {
    
        Dimension d=getContentPane().getSize();
	
	int w = d.width-10;
	int h = d.height;
	
	statusLine.setBounds(5,2,w,20);
	treeView.setBounds(5,25,(d.width/2)-10,h-30);
	resView.setBounds((d.width/2)+5,25,(d.width/2)-10,h-60);
	applyPb.setBounds((d.width/2)+5,h-30,110,25);
	refreshPb.setBounds((d.width/2)+120,h-30,80,25);
	detailsPb.setBounds((d.width/2)+205,h-30,110,25);

        // This to force JScroolPane to be redrawn
	treeView.revalidate();
	resView.revalidate();
    }

    /* Find property a=0 New search a=1 Continue search */    
    public void findProperty(int a) {  

	if( a==0 ) {   
	 
	    TangoTreeNode start_node;
	    TangoTreeNode.scan_progress = 0;
	    TreePath selPath = mainTree.getSelectionPath();
	    if( selPath == null )
	      start_node = (TangoTreeNode)rootNode;
	    else
	      start_node = (TangoTreeNode)selPath.getLastPathComponent();
	      
	    SearchDlg dlg = new SearchDlg(this,true,"Search Tango data base","Text to find");
	    
	    if( !dlg.showDlg() )
	      return;
	          	    
	    TangoTreeNode.InitiateSearch(start_node,
	      SearchDlg.dIgnoreCase,
	      SearchDlg.dSearchValues,
	      SearchDlg.dSearchAttributes,
	      SearchDlg.dSearchUseRegexp,
	      false);
	      
    	}
	       
        // Try to find a text in the tree	    
	    
	TangoTreeNode.error_report  = false;
	TreePath found = TangoTreeNode.findText(SearchDlg.textToFind);
	    
	if( found!=null ) {
	  mainTree.setSelectionPath(found);
	  mainTree.scrollPathToVisible(found);
	} 
	    
   	TangoTreeNode.error_report = true;
	if(found==null) TangoTreeNode.showJiveError("No item found...");
    }

    /* select multiple properties */    
    public void selectProperties() {  
	 
	    TangoTreeNode start_node;
	    TangoTreeNode.scan_progress = 0;
	    TreePath selPath = mainTree.getSelectionPath();
	    if( selPath == null )
	      start_node = (TangoTreeNode)rootNode;
	    else
	      start_node = (TangoTreeNode)selPath.getLastPathComponent();
	      
	    SearchDlg dlg = new SearchDlg(this,true,"Multiple selection","Select");
	    
	    if( !dlg.showDlg() )
	      return;
	          	    
	    TangoTreeNode.InitiateSearch(start_node,
	      SearchDlg.dIgnoreCase,
	      SearchDlg.dSearchValues,
	      SearchDlg.dSearchAttributes,
	      SearchDlg.dSearchUseRegexp,
	      true);
	      
	      	    
	    TangoTreeNode.error_report  = false;
	    TreePath[] found = TangoTreeNode.findMultipleText(SearchDlg.textToFind);
	    
	    if( found!=null ) {
	      if( found.length > 0 ) {	        
		mainTree.setSelectionPaths(found);
	        mainTree.scrollPathToVisible(found[0]);
	      }
	    } 
	    
   	    TangoTreeNode.error_report = true;
	    gobalValueRefresh();
    }
	
    /* Exit the Application */
    private void exitForm() {
     if(running_from_shell) System.exit(0);
     else { setVisible(false);dispose(); }
    }
        
    /* main */
    public static void main(String args[]) {
        //System.setProperty("TANGO_HOST", "gizmo:20000");
	//JFrame.setDefaultLookAndFeelDecorated(true);
	MainPanel window = new MainPanel(0);
    }
    
}
