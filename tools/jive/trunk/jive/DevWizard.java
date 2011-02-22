package jive;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevSource;
import fr.esrf.TangoApi.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/** The Device wizard dialog. */
public class DevWizard extends JDialog implements ActionListener {

  final static Font wizFont = new Font("Dialog",Font.PLAIN,12);

  // Common component of the Wizard dialog
  private JButton     nextButton;
  private JButton     backButton;
  private JButton     skipButton;
  private JButton     cancelButton;
  private JPanel      buttoniPanel;
  private JPanel      buttonPanel;
  private JPanel      innerPanel;
  private JPanel      helpPanel;
  private JTextArea   helpText;
  private JTextArea   helpIconText;
  private JLabel      helpIcon;
  private JLabel      helpLabel;
  private JPanel      wizardContainer;
  private Vector      allPanels;
  int                 activePanel = -1;
  private Database    db;
  private String      dbName;


  static DeviceProxy	starter = null;
  static public String	lastServStarted;

  /**
   * Construct a wizard dialog.
   * @param parent Parent frame
   */
  public DevWizard(Frame parent) {
    super(parent,true);
    initComponents();
	this.starter = null;
	lastServStarted = null;
  }

  /**
   * Construct a wizard dialog.
   * @param parent Parent dialog
   */
  public DevWizard(Dialog parent) {
    super(parent,true);
    initComponents();
	this.starter = null;
	lastServStarted = null;
  }

  /**
   * Construct a wizard dialog.
   * @param parent Parent frame
   * @param starter Starter device reference to start created server.
  */
  public DevWizard(Frame parent, DeviceProxy starter) {
    this(parent);
	this.starter = starter;
 	lastServStarted = null;
 }
  
  /**
   * Construct a wizard dialog.
   * @param parent Parent dialog
   * @param starter Starter device reference to start created server.
  */
  public DevWizard(Dialog parent, DeviceProxy starter) {
    this(parent);
	this.starter = starter;
	lastServStarted = null;
  }

  /**
   * Shows the server wizard.
   * @param serverList List of allowed server. Pass null for free server selection.
   */
  public void showWizard(String[] serverList) {
    allPanels = new Vector();
    allPanels.add(new ServerPanel(this,serverList)); // Server registration
    displayWizard();
  }

  /**
   * Shows the classes wizard.
   * @param serverName Name of server to be edited
   */
  public void showClassesWizard(String serverName) {

    allPanels = new Vector();

    try {
      DeviceProxy admin = new DeviceProxy("dserver/"+serverName);
      addPanel(new ClassPanel(this,serverName,admin));
      displayWizard();
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  /**
   * Shows the devices wizard.
   * @param serverName Name of the server
   * @param className Name of class
   */
  public void showDevicesWizard(String serverName,String className) {
    allPanels = new Vector();
    WizardPanel devPanel = new DevicePanel(this,serverName,className);
    addPanel( devPanel );
    addPanel( new FinishPanel(this,serverName,null,devPanel) );
    displayWizard();
  }

  /**
   * Shows the property wizard for the specified device.
   * @param serverName Name of the server hosting the device
   * @param className Name of class hosting the device
   * @param devName Device to be edited
   */
  public void showDeviceWizard(String serverName,String className,String devName) {

    allPanels = new Vector();

    try {

      DeviceData classData = new DeviceData();
      classData.insert(className);
      DeviceProxy adminDev = new DeviceProxy("dserver/"+serverName);
      DeviceData ret = adminDev.command_inout("QueryWizardDevProperty", classData);
      String[] conf = ret.extractStringArray();
      for (int j = 0; j < conf.length; j += 3) {
        addPanel( new PropertyPanel(this, PropertyPanel.DEVICE_PROPERTY,serverName,
                                    devName, conf[j], conf[j + 1], conf[j + 2], className));
      }
      addPanel( new FinishPanel(this,serverName,null,null) );
      displayWizard();

    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  private void displayWizard() {
    setActivePanel(0);
    JiveUtils.centerDialog(this);
    setVisible(true);
    dispose();
  }

  boolean canBack() {
    return activePanel>0;
  }

  private void initComponents() {

    innerPanel = new JPanel(new BorderLayout());
    setContentPane(innerPanel);
    setResizable(false);

    // --------------------------------------------------------
    // Button panel
    // --------------------------------------------------------

    backButton = new JButton("< Back");
    backButton.setFont(wizFont);
    backButton.addActionListener(this);
    nextButton = new JButton("Next >");
    nextButton.setFont(wizFont);
    nextButton.addActionListener(this);
    skipButton = new JButton("Skip");
    skipButton.setFont(wizFont);
    skipButton.addActionListener(this);
    cancelButton = new JButton("Cancel");
    cancelButton.setFont(wizFont);
    cancelButton.addActionListener(this);
    buttoniPanel = new JPanel();
    FlowLayout fl = new FlowLayout(FlowLayout.RIGHT);
    buttoniPanel.setLayout(fl);
    buttoniPanel.add(backButton);
    buttoniPanel.add(nextButton);
    buttoniPanel.add(skipButton);
    buttoniPanel.add(new JLabel(" "));
    buttoniPanel.add(cancelButton);
    buttonPanel = new JPanel(new BorderLayout());
    buttonPanel.add(buttoniPanel,BorderLayout.CENTER);
    JSeparator sep = new JSeparator();
    sep.setOrientation(JSeparator.HORIZONTAL);
    buttonPanel.add(sep,BorderLayout.NORTH);
    innerPanel.add(buttonPanel,BorderLayout.SOUTH);

    // --------------------------------------------------------
    // Help panel
    // --------------------------------------------------------
    helpPanel = new JPanel(null);
    //helpPanel.setBackground(Color.GREEN);
    helpPanel.setPreferredSize(new Dimension(500,240));
    helpIcon = new JLabel();
    helpIcon.setBounds(10,10,128,128);
    helpPanel.add(helpIcon);

    helpLabel = new JLabel();
    helpLabel.setFont(new Font("Dialog",Font.BOLD,16));
    helpLabel.setBounds(140,10,350,30);
    helpPanel.add(helpLabel);

    helpText = new JTextArea();
    helpText.setEditable(false);
    helpText.setBorder(null);
    helpText.setBounds(140,40,350,98);
    helpText.setBackground(innerPanel.getBackground());
    helpText.setLineWrap(true);
    helpText.setWrapStyleWord(true);
    helpPanel.add(helpText);

    helpIconText = new JTextArea();
    helpIconText.setEditable(false);
    helpIconText.setBorder(null);
    helpIconText.setMargin(new Insets(0,0,0,0));
    helpIconText.setBackground(innerPanel.getBackground());
    helpIconText.setFont(new Font("Dialog",Font.BOLD,12));
    helpIconText.setLineWrap(true);
    helpIconText.setWrapStyleWord(true);
    helpIconText.setBounds(10,140,128,95);
    helpPanel.add(helpIconText);

    innerPanel.add(helpPanel,BorderLayout.NORTH);

    wizardContainer = new JPanel(new GridLayout(1,1));
    wizardContainer.setBorder(null);
    wizardContainer.setBounds(140,140,350,95);
    helpPanel.add(wizardContainer);

    // ------------------------------------------------------
    // Global Tango initialisation
    // ------------------------------------------------------
    try {
      db = ApiUtil.get_db_obj();
      dbName = db.get_tango_host();
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
      return;
    }
    setTitle("Tango Device Installation Wizard on " + dbName);

  }

  void addPanel(WizardPanel p) {
    allPanels.add(p);
  }

  void addPanel(int idx,WizardPanel p) {
    allPanels.add(idx,p);
  }

  WizardPanel getPanel(int i) {
    return (WizardPanel)allPanels.get(i);
  }

  Vector getPanels() {
    return allPanels;
  }

  private void setActivePanel(int p) {

    activePanel = p;
    WizardPanel panel = getPanel(p);
    helpIcon.setIcon(panel.getIcon());
    helpText.setText(panel.getDescription());
    helpLabel.setText(panel.getTitle());
    helpIconText.setText(panel.getSubTitle());
    wizardContainer.removeAll();
    wizardContainer.add(panel);
    backButton.setEnabled(panel.getBackState());
    backButton.setText(panel.getBackText());
    nextButton.setEnabled(panel.getNextState());
    nextButton.setText(panel.getNextText());
    skipButton.setVisible(panel.getSkipState());
    skipButton.setText(panel.getSkipText());
    innerPanel.revalidate();
    repaint();

  }

  private void goToNextPanel() {

    if(activePanel+1<allPanels.size())
      // go to the next panel
      setActivePanel(activePanel+1);
    else
      // Finished
      setVisible(false);

  }

  public void actionPerformed(ActionEvent e) {

    Object src = e.getSource();

    if(src==cancelButton) {

      if( JOptionPane.showConfirmDialog(this,"Do you want to exit wizard ?","Confirmation",JOptionPane.YES_NO_OPTION)==
             JOptionPane.YES_OPTION )
        setVisible(false);

    } else if (src==nextButton) {

      if( getPanel(activePanel).next() ) {
        goToNextPanel();
      }

    } else if (src==backButton) {

      if( getPanel(activePanel).back() ) {
        // go to the next panel
        setActivePanel(activePanel-1);
      }

    } else if (src==skipButton) {

      if( getPanel(activePanel).skip() ) {
        goToNextPanel();
      }

    }

  }

  public static void main(String args[]) {

    final DevWizard dlg = new DevWizard((Frame)null);
    //String[] myList = {"Server1","Motor","Serial"};
    //dlg.showWizard(myList);
    //dlg.showDeviceWizard("DSTestWz/jlp","Wizard");
    dlg.showWizard(null);
    System.exit(0);

  }

}

// -------------------------------------------------------------------------------

/**
 * An abstract class for Wizard panel
 */
abstract class WizardPanel extends JPanel {

  ImageIcon panelIcon;
  DevWizard parent;

  public Icon getIcon() {
    return panelIcon;
  }

  public abstract String  getDescription();
  public abstract String  getTitle();

  public String getSubTitle() {
    return "";
  }

  public boolean getBackState() {
    return parent.canBack();
  }

  public String getBackText() {
    return "< Back";
  }

  public boolean back() {
    return false;
  }

  public boolean getNextState() {
    return false;
  }

  public String getNextText() {
    return "Next >";
  }

  public boolean next() {
    return false;
  }

  public boolean getSkipState() {
    return false;
  }

  public String getSkipText() {
    return "Skip";
  }

  public boolean skip() {
    return false;
  }

  public void removeNextPanel() {
    // Remove all panels that follow this panel
    int idx = parent.getPanels().indexOf(this);
    int toDel = parent.getPanels().size() - idx - 1;
    for(int i=0;i<toDel;i++)
      parent.getPanels().removeElementAt(idx+1);
  }

  public boolean deviceExists(String devName,String serverName,String className) throws DevFailed {

    boolean found = false;
    int i=0;
    Database db = ApiUtil.get_db_obj();
    String[] devList = db.get_device_name(serverName ,className);
    while(i<devList.length && !found) {
      found = devList[i].equalsIgnoreCase(devName);
      if(!found) i++;
    }
    return found;

  }

}

/**
 * The server panel wizard.
 */
class ServerPanel extends WizardPanel implements ActionListener {

  private JLabel     serverLabel;
  private JComboBox  serverCombo;
  private JLabel     instanceLabel;
  private JComboBox  instanceCombo;

  public ServerPanel(DevWizard parent,String[] freeList) {

    setLayout(null);

    serverLabel = new JLabel("Server name");
    serverLabel.setFont(DevWizard.wizFont);
    serverLabel.setBounds(10,10,100,25);
    add(serverLabel);

    serverCombo = new JComboBox();
    serverCombo.setFont(DevWizard.wizFont);
    serverCombo.setBounds(120,10,140,25);
    add(serverCombo);

    // Initialise the server combo with the list of server
    if( freeList!=null ) {

      serverCombo.setEditable(false);
      for(int i=0;i<freeList.length;i++)
        serverCombo.addItem(freeList[i]);
      serverCombo.setSelectedIndex(0);

    } else {

      serverCombo.setEditable(true);
      try {
        Database db = ApiUtil.get_db_obj();
        String[] srvList = db.get_server_name_list();
        for(int i=0;i<srvList.length;i++)
          serverCombo.addItem(srvList[i]);
        serverCombo.setSelectedItem(null);
      } catch (DevFailed e) {
        JiveUtils.showTangoError(e);
      }

    }
    serverCombo.addActionListener(this);


    instanceLabel = new JLabel("Instance name");
    instanceLabel.setFont(DevWizard.wizFont);
    instanceLabel.setBounds(10,40,100,25);
    add(instanceLabel);

    instanceCombo = new JComboBox();
    instanceCombo.setEditable(true);
    instanceCombo.setFont(DevWizard.wizFont);
    instanceCombo.setBounds(120,40,140,25);
    add(instanceCombo);

    panelIcon = new ImageIcon(getClass().getResource("/jive/server_wz.gif"));

    this.parent = parent;

  }

  public void actionPerformed(ActionEvent e) {
    Object src = e.getSource();
    if( src==serverCombo ) {
      if(e.getActionCommand() == "comboBoxChanged") {
        String sName = (String)serverCombo.getSelectedItem();
        // Update the instanceCombo
        try {
          Object oldSelection = instanceCombo.getSelectedItem();
          instanceCombo.removeAllItems();
          Database db = ApiUtil.get_db_obj();
          String[] insList = db.get_instance_name_list(sName);
          for(int i=0;i<insList.length;i++)
            instanceCombo.addItem(insList[i]);
          instanceCombo.setSelectedItem(oldSelection);
        } catch (DevFailed ex) {
          JiveUtils.showTangoError(ex);
        }
      }
    }
  }

  public String getTitle() {
    return "Server Registration";
  }

  public String getDescription() {

    return "This wizard helps you to install a Tango device. First, you have to "+
           "enter the \"Server name\" (executable file name) and its \"instance name\".\n" +
           "To register the server, click [Next].";

  }

  public boolean getNextState() {
    return true;
  }

  public boolean next() {

    String srvName = (String)serverCombo.getSelectedItem();
    String srvInst = (String)instanceCombo.getSelectedItem();

    if(srvName==null || srvInst==null || srvName.length()==0 || srvInst.length()==0) {
      JiveUtils.showJiveError("Please enter a server and an instance name.");
      return false;
    }

    // Register the server by adding the admin device
    String dsName = "dserver/" + srvName + "/" + srvInst;
    String srName = srvName + "/" + srvInst;
    try {
      Database db = ApiUtil.get_db_obj();
      // Check that it does not already exits
      if( !deviceExists(dsName,srName,"DServer") ) {
        DbDevInfo devInfo = new DbDevInfo(dsName,"DServer",srName);
        db.add_device(devInfo);
      }
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
      return false;
    }

	//	If starter not null try to start server
	DeviceProxy	admin = null;
	if (DevWizard.starter!=null)
	{
		admin = startServer(srName);
    	// Add the new panel
		removeNextPanel();
		if (admin==null)
        	return false;
		else
			parent.addPanel(new ClassPanel(parent,srName, admin));
	}
	else
	{
	    // Add the new panel
    	removeNextPanel();
        parent.addPanel(new StartingPanel(parent,srName));
	}
    return true;

  }
  
  	/**
	 *	Check if server can be started on host
	 */
	private final static int	DO_NOT_START    = 0;
	private final static int	START_IT        = 1;
	private final static int	ALREADY_STARTED = 2;
	private int canBeStarted(DeviceProxy dev, String srvName, String hostname) throws DevFailed
	{
		//	Check if already running
		String	devname = "dserver/" + srvName;
		boolean	running = false;
		try {
			dev.ping();
			running = true;
		}catch(DevFailed e){}

		IORdump	d = new IORdump(devname);
		String	running_on = d.get_host();
		if (running)
		{
			//	Check if running on another host
			if (running_on.startsWith(hostname))
			{
				//	Same host -> display just a warning
				JOptionPane.showMessageDialog(parent,
							srvName + " is alredy running on " + running_on,
							"Jive Message", JOptionPane.WARNING_MESSAGE);
				return ALREADY_STARTED;
			}
			else
				fr.esrf.TangoDs.Except.throw_exception("StartServerFailed",
					srvName + " is already running on " + d.get_host(),
					"DevWizard.startServer()");		
		}

		//	Check if already registred on another host
		if (running_on!=null)
		{
			if (running_on.startsWith(hostname)==false)
			{
				//	Same host -> display warning and ask question
				Object[] options = { "Continue", "Cancel" };
				if (JOptionPane.showOptionDialog(parent,
						srvName + " is alredy registred on " + running_on,
						"Warning",
						JOptionPane.DEFAULT_OPTION,
						JOptionPane.WARNING_MESSAGE,
						null, options, options[0])!=JOptionPane.OK_OPTION)
					return DO_NOT_START;
			}
		}
		return START_IT;
	}
	/**
	 *	Start the server
	 */
	private DeviceProxy startServer(String srvName)
	{
		DeviceProxy	dev = null;
		String		devname = "dserver/" + srvName;
		String		hostname = DevWizard.starter.name();
		hostname = hostname.substring(hostname.lastIndexOf("/")+1);
		try
		{
			dev = new DeviceProxy(devname);
			switch(canBeStarted(dev, srvName, hostname))
			{
			case DO_NOT_START:
				return null;
			case ALREADY_STARTED:
				return dev;

			default:
				//	Register server on host to be displayed in astor
				//	even startup failed.
				DbDevExportInfo	info =
					new DbDevExportInfo(devname, "null", hostname, "null");
				ApiUtil.get_db_obj().export_device(info);
				ApiUtil.get_db_obj().unexport_device(devname);

				//	Try to start it
				DeviceData	argin = new DeviceData();
				argin.insert(srvName);
				DevWizard.starter.command_inout("DevStart", argin);

				//	Check if startup succeed
				DevWizard.starter.set_source(DevSource.DEV);
				boolean	started = false;
				for (int i=0 ; !started && i<5 ; i++)	//	5 seconds timeout
				{
					Thread.sleep(1000);
					DeviceAttribute	att = DevWizard.starter.read_attribute("RunningServers");
					String[]	servlist = att.extractStringArray();
					for (int j=0 ; j<servlist.length ; j++)
						if (servlist[j].equals(srvName))
						{
							started = true;
							dev = new DeviceProxy(devname);
							DevWizard.lastServStarted = srvName;
							String	msg = srvName + " is now running on " + hostname;
							JOptionPane.showMessageDialog(parent,
									msg, "Jive Message", JOptionPane.INFORMATION_MESSAGE);
							break;
						}
				}
			}
		}
		catch(DevFailed e)
		{
			fr.esrf.TangoDs.Except.print_exception(e);
			JiveUtils.showJiveError("Failed to start " + srvName + " :\n" + e.errors[0].desc);
			parent.setVisible(false);
		} 
		catch(InterruptedException e){}

		return dev;
	}

}

/**
 * The starting server panel wizard.
 */
class StartingPanel extends WizardPanel {

  String serverName;

  public StartingPanel(DevWizard parent,String srvName) {

    setLayout(null);

    panelIcon = new ImageIcon(getClass().getResource("/jive/server_wz.gif"));
    this.parent = parent;
    serverName = srvName;


  }

  public String getTitle() {
    return "Start the server";
  }

  public String getDescription() {
    return "The server \"" + serverName + "\" has been successfully registered. You have to start it now.\n"+
           "When done, click [Next] to continue or [Back] to register a new server.";
  }

  public boolean getNextState() {
    return true;
  }

  public boolean next() {

    // Getting configuration from admin device
    String dName = "dserver/"+serverName;
    try {
      DeviceProxy admin = new DeviceProxy(dName);
      removeNextPanel();
      parent.addPanel(new ClassPanel(parent,serverName,admin));
    } catch (DevFailed e) {
      JiveUtils.showJiveError("Failed to contact the admin server of " + serverName +
                              "\nCheck that " + serverName + " is running.\n" +
                              "\nThe wizard cannot continue....\n" +
                              e.errors[0].desc);
      return false;
    }
    return true;

  }

  public boolean back() {

    return true;
  }


}

/**
 * The class main panel wizard.
 */
class ClassPanel extends WizardPanel {

  DeviceProxy adminDev;
  String[]    allClasses = new String[0];
  JList       classList;
  JScrollPane classView;
  String      serverName;

  public ClassPanel(DevWizard parent,String serverName,DeviceProxy adm) {

    setLayout(null);
    this.parent = parent;
    adminDev = adm;
    this.serverName = serverName;

    try {
      DeviceData d = adminDev.command_inout("QueryClass");
      allClasses = d.extractStringArray();
    } catch(DevFailed e) {
      JiveUtils.showTangoError(e);
    }

    classList = new JList(allClasses);
    classList.setBorder(null);
    classList.setFont(DevWizard.wizFont);
    classView = new JScrollPane(classList);
    classView.setBounds(0,0,350,95);
    add(classView);

    panelIcon = new ImageIcon(getClass().getResource("/jive/class_wz.gif"));

  }

  public String getTitle() {
    return "Class Selection";
  }

  public String getDescription() {
    if(allClasses.length==0) {
      return "Sorry, but the server has no class...\nThe Wizard cannot conitnue.";
    } else {
      return "The server has been succesfully started and has "+Integer.toString(allClasses.length)+
             " class(es) . Keep in mind that modifying exiting class property may affect other running server.\n"+
             "Click [Edit Class] to edit properties of the selected class\n" +
             "Click [Declare device] to continue with device declaration.";
    }
  }

  public String getSubTitle() {
    String ret = "Server:\n" + serverName;
    return ret;
  }

  public boolean getNextState() {
    return allClasses.length>0;
  }

  public boolean next() {

    int idx = classList.getSelectedIndex();
    if(idx<0) {
      JiveUtils.showJiveError("Please select a class first.");
      return false;
    }

    removeNextPanel();

    // Create class property panel
    try {
      DeviceData classData = new DeviceData();
      classData.insert(allClasses[idx]);
      DeviceData ret = adminDev.command_inout("QueryWizardClassProperty", classData);
      String[] conf = ret.extractStringArray();
      for (int j = 0; j < conf.length; j += 3) {
        parent.addPanel(new PropertyPanel(parent, PropertyPanel.CLASS_PROPERTY,serverName,
                                        allClasses[idx], conf[j], conf[j + 1], conf[j + 2],null));
      }
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

    // Create Device panel
    WizardPanel devPanel = new DevicePanel(parent,serverName,allClasses[idx]);
    parent.addPanel( devPanel );
    parent.addPanel( new FinishPanel(parent,serverName,this,devPanel) );

    return true;
  }

  public boolean getSkipState() {
    return allClasses.length>0;
  }

  public boolean skip() {

    int idx = classList.getSelectedIndex();
    if(idx<0) {
      JiveUtils.showJiveError("Please select a class first.");
      return false;
    }

    // Skip class definiton
    // Jump to device definition
    // Create Device panel
    removeNextPanel();
    WizardPanel devPanel = new DevicePanel(parent,serverName,allClasses[idx]);
    parent.addPanel( devPanel );
    parent.addPanel( new FinishPanel(parent,serverName,this,devPanel) );

    return true;
  }

  public String getSkipText() {
    return "Declare device";
  }

  public String getNextText() {
    return "Edit Class";
  }

  public boolean back() {
    return true;
  }

}

/**
 * Device Decalration panel.
 */
class DevicePanel extends WizardPanel {

  String      className;
  String      serverName;

  JComboBox   deviceCombo;
  JLabel      deviceLabel;

  public DevicePanel(DevWizard parent,String serverName,String className) {

    setLayout(null);

    this.parent = parent;
    this.className = className;
    this.serverName = serverName;


    deviceLabel = new JLabel("Device name");
    deviceLabel.setFont(DevWizard.wizFont);
    deviceLabel.setBounds(10,30,100,25);
    add(deviceLabel);

    deviceCombo = new JComboBox();
    deviceCombo.setEditable(true);
    deviceCombo.setFont(DevWizard.wizFont);
    deviceCombo.setBounds(115,30,160,25);
    add(deviceCombo);

    try {
      Database db = ApiUtil.get_db_obj();
      String[] devList = db.get_device_name(serverName ,className);
      for(int i=0;i<devList.length;i++) {
        deviceCombo.addItem(devList[i]);
      }
    } catch(DevFailed e) {
      JiveUtils.showTangoError(e);
    }
    deviceCombo.setSelectedItem(null);

    panelIcon = new ImageIcon(getClass().getResource("/jive/device_wz.gif"));


  }

  public String getTitle() {
    return "Device Declaration of Class \"" + className + "\"";
  }

  public String getDescription() {
    return "It is now time to give a device name for the '"+className+"' class. " +
           "If this device does not already exit, it will be added. Otherwise, the " +
           "server's device list remains unchanged.\n" +
           "Click [Next] to edit device properties.";
  }

  public boolean getNextState() {
    return true;
  }

  public boolean next() {

    // Get the device name and add it to the database
    String devName = (String)deviceCombo.getSelectedItem();
    if(devName==null || devName.length()==0) {
      JiveUtils.showJiveError("Please enter a device name.");
      return false;
    }

    try {
      Database db = ApiUtil.get_db_obj();
      if(!deviceExists(devName,serverName,className)) {
        db.add_device(devName,className,serverName);
      }
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
      return false;
    }

    // Check that property has not been already added
    boolean isAdded = false;
    int idx = parent.getPanels().indexOf(this);
    if(idx<(parent.getPanels().size()-1))
      isAdded = parent.getPanel(idx+1) instanceof PropertyPanel;

    // Create class device property panels
    if (!isAdded) {

      try {

        DeviceData classData = new DeviceData();
        classData.insert(className);
        DeviceProxy adminDev = new DeviceProxy("dserver/"+serverName);
        DeviceData ret = adminDev.command_inout("QueryWizardDevProperty", classData);
        String[] conf = ret.extractStringArray();
        for (int j = 0; j < conf.length; j += 3) {
          parent.addPanel(idx+1+j/3, new PropertyPanel(parent, PropertyPanel.DEVICE_PROPERTY,serverName,
                                               devName, conf[j], conf[j + 1], conf[j + 2],className));
        }

      } catch (DevFailed e) {
        JiveUtils.showTangoError(e);
        return false;
      }

    } else {

      // Update device name
      boolean end=false;
      idx++;
      while(idx<parent.getPanels().size() && !end) {
        end = !(parent.getPanel(idx) instanceof PropertyPanel);
        if(!end) {
          ((PropertyPanel)parent.getPanel(idx)).setEntityName(devName);
          idx++;
        }
      }

    }

    return true;
  }

  public boolean back() {
    return true;
  }

}

/**
 * Property edition panel.
 */
class PropertyPanel extends WizardPanel implements ActionListener {

  final static int CLASS_PROPERTY  = 0;
  final static int DEVICE_PROPERTY = 1;

  JTextArea    propValue;
  JScrollPane  valueView;
  JButton      setDefaultButton;
  JButton      viewDefaultButton;
  int          type;
  String       entity;
  String       description;
  String       name;
  String       defValue;
  String       dbValue;
  String       srvName;
  String       devClass;

  public PropertyPanel(DevWizard parent,
                       int propertyType,
                       String serverName,
                       String entityName,
                       String propertyName,
                       String propertyDesc,
                       String defaultValue,
                       String deviceClassName) {

    setLayout(null);
    setPreferredSize(new Dimension(350,95));

    this.parent = parent;
    entity = entityName;
    type = propertyType;
    description = propertyDesc;
    name = propertyName;
    defValue = defaultValue;
    dbValue = defaultValue;
    srvName = serverName;
    devClass = deviceClassName;

    propValue = new JTextArea();
    propValue.setEditable(true);
    propValue.setBorder(BorderFactory.createLoweredBevelBorder());
    valueView = new JScrollPane(propValue);
    valueView.setBounds(0,0,230,95);
    add(valueView);

    refreshValue();

    setDefaultButton = new JButton("Set Default");
    setDefaultButton.setMargin(new Insets(2,1,2,1));
    setDefaultButton.setFont(DevWizard.wizFont);
    setDefaultButton.addActionListener(this);
    setDefaultButton.setBounds(235,0,115,25);
    add(setDefaultButton);

    viewDefaultButton = new JButton("View Default");
    viewDefaultButton.setMargin(new Insets(2,1,2,1));
    viewDefaultButton.setFont(DevWizard.wizFont);
    viewDefaultButton.addActionListener(this);
    viewDefaultButton.setBounds(235,30,115,25);
    add(viewDefaultButton);

    if(propertyType==CLASS_PROPERTY)
      panelIcon = new ImageIcon(getClass().getResource("/jive/class_wz.gif"));
    else
      panelIcon = new ImageIcon(getClass().getResource("/jive/device_wz.gif"));


  }

  private void refreshValue() {

    DbDatum d=null;
    try {

      Database db = ApiUtil.get_db_obj();
      // Get the value from the database
      switch(type) {
        case CLASS_PROPERTY:
          d = db.get_class_property(entity,name);
          break;
        case DEVICE_PROPERTY:
          d = db.get_device_property(entity,name);
          break;
      }
      dbValue = defValue;
      if(!d.is_empty()) dbValue = d.extractString();

    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }
    propValue.setText(dbValue);

  }

  public void setEntityName(String s) {
    entity = s;
    refreshValue();
  }

  public void actionPerformed(ActionEvent e) {

    Object src = e.getSource();
    if(src==setDefaultButton) {
      propValue.setText(defValue);
    } else if (src==viewDefaultButton) {
      JOptionPane.showMessageDialog(this,defValue,"Default value for "+name,JOptionPane.INFORMATION_MESSAGE);
    }

  }

  public String getTitle() {
    return "Property: " + name;
  }

  public String getDescription() {
    return description;
  }

  public boolean getNextState() {
    return true;
  }

  public boolean next() {

    String newValue = propValue.getText();
    if (!newValue.equals(dbValue)) {

      // Store property into the database
      DbDatum d = new DbDatum(name, newValue);
      DbDatum[] argin = new DbDatum[1];
      argin[0] = d;

      try {

        Database db = ApiUtil.get_db_obj();
        // Get the value from the database
        switch (type) {
          case CLASS_PROPERTY:
            db.put_class_property(entity, JiveUtils.makeDbDatum(name,newValue));
            break;
          case DEVICE_PROPERTY:
            db.put_device_property(entity, JiveUtils.makeDbDatum(name,newValue));
            break;
        }
        System.out.println("Writting " +entity + "/" + name + " to database");
        dbValue = newValue;

      } catch (DevFailed e) {
        JiveUtils.showTangoError(e);
        return false;
      }

    }

    return true;
  }

  public boolean back() {
    return true;
  }

  public String getSubTitle() {
    String ret = "Server:\n " + srvName + "\n";
    switch(type) {
      case CLASS_PROPERTY:
        ret += "Class:\n " + entity;
        break;
      case DEVICE_PROPERTY:
        ret += "Class:\n " + devClass + "\n";
        ret += "Device:\n " + entity;
        break;
    }
    return ret;
  }

}

/**
 * End panel.
 */
class FinishPanel extends WizardPanel {

  WizardPanel backPointClass;
  WizardPanel backPointDevice;
  String serverName;

  public FinishPanel(DevWizard parent,String serverName,WizardPanel backPointC,WizardPanel backPointD) {

    setLayout(null);

    panelIcon = new ImageIcon(getClass().getResource("/jive/device_wz.gif"));
    this.parent = parent;
    backPointDevice = backPointD;
    backPointClass = backPointC;
    this.serverName = serverName;

  }

  public String getTitle() {
    return "Configuration done";
  }

  public String getDescription() {
    return "The configuration of the device is now ended.\nClick [New device] to add a device to this class.\n"+
           "Click [New Class] to edit an other class\n";
  }

  public boolean getNextState() {
    return (backPointDevice!=null);
  }

  public String getNextText() {
    return "New Device";
  }

  public boolean next() {
    parent.activePanel = parent.getPanels().indexOf(backPointDevice) - 1;
    return true;
  }

  public boolean getBackState() {
    return (backPointClass!=null);
  }

  public String getBackText() {
    return "New Class";
  }

  public boolean back() {
    parent.activePanel = parent.getPanels().indexOf(backPointClass) + 1;
    return true;
  }

  public String getSkipText() {
    return "Finish";
  }

  public boolean getSkipState() {
    return true;
  }

  public boolean skip() {
    if(  JOptionPane.showConfirmDialog(this,"Would you like to reinitialize the server ?",
                                       "Server restart",JOptionPane.YES_NO_OPTION)
            ==JOptionPane.YES_OPTION) {
      try {
        DeviceProxy ds = new DeviceProxy("dserver/"+serverName);
        ds.command_inout("RestartServer");
      } catch (DevFailed e) {
        JiveUtils.showTangoError(e);
      }
    }
    // Finished
    return true;
  }

}
