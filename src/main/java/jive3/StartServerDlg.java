package jive3;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.*;
import jive.JiveUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Start new server Dlg
 */
public class StartServerDlg extends JFrame implements ActionListener {

  class LevelComboBoxRenderer extends JComboBox implements TableCellRenderer {

    public LevelComboBoxRenderer(String[] items) {
      super(items);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
      if (isSelected) {
        setForeground(table.getSelectionForeground());
        super.setBackground(table.getSelectionBackground());
      } else {
        setForeground(table.getForeground());
        setBackground(table.getBackground());
      }
      int idx = ((Integer)value).intValue();
      setSelectedIndex(idx);
      return this;
    }

  }

  class LevelComboBoxEditor extends DefaultCellEditor {

    public LevelComboBoxEditor(String[] items) {
      super(new JComboBox(items));
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
                                          boolean isSelected,
                                          int row, int column) {

      JComboBox cb = (JComboBox)editorComponent;
      int idx = ((Integer)value).intValue();
      cb.setSelectedIndex(idx);
      return editorComponent;

    }

    public Object getCellEditorValue() {

      JComboBox cb = (JComboBox)editorComponent;
      return cb.getSelectedIndex();

    }

  }

  class ServerInfo {

    String serverName;
    int level;
    String host;

  }

  private JTextTips serverText;
  private JScrollPane serversView;
  private JTable serversTable;
  private DefaultTableModel serversModel;
  private JPanel innerPanel;
  private JButton addButton;
  private JButton addFromButton;
  private String  hostName;
  private DeviceProxy starter;
  private MainPanel invoker;

  private JButton clearAllButton;
  private JButton clearButton;
  private JButton startButton;
  private JButton dismissButton;

  private ArrayList<ServerInfo> serverInfos;
  private Database db;

  public StartServerDlg(Database db,String hostName,DeviceProxy starter,MainPanel invoker) {

    this.db = db;
    this.hostName = hostName;
    this.starter = starter;
    this.invoker = invoker;
    serverInfos = new ArrayList<>();

    innerPanel = new JPanel();
    innerPanel.setLayout(new BorderLayout());

    String[] levels = new String[TreePanelHostCollection.NB_LEVELS+2];
    levels[0] = "Remove Starter Info";
    levels[1] = "Not controlled";
    for(int i=2;i<levels.length;i++) levels[i] = "Level " + (i-1);

    // ------------------------------------------------------------------

    JPanel upPanel = new JPanel();
    upPanel.setLayout(new GridBagLayout());
    upPanel.setBorder(BorderFactory.createEtchedBorder());

    GridBagConstraints gbc = new GridBagConstraints();

    gbc.fill = GridBagConstraints.BOTH;
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new Insets(3,3,3,3);
    JLabel serverLabel = new JLabel("Server");
    upPanel.add(serverLabel,gbc);

    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.weightx = 1.0;
    serverText = new JTextTips();
    serverText.addActionListener(this);
    upPanel.add(serverText,gbc);

    gbc.gridx = 2;
    gbc.gridy = 0;
    gbc.weightx = 0.0;
    addButton = new JButton("Add");
    addButton.addActionListener(this);
    upPanel.add(addButton,gbc);

    gbc.gridx = 3;
    gbc.gridy = 0;
    gbc.weightx = 0.0;
    addFromButton = new JButton("Move servers from an other host");
    addFromButton.addActionListener(this);
    upPanel.add(addFromButton,gbc);

    innerPanel.add(upPanel,BorderLayout.NORTH);

    // ------------------------------------------------------------------

    serversModel = new DefaultTableModel() {

      public Class getColumnClass(int columnIndex) {
        switch (columnIndex) {
          case 0:
            return String.class;
          case 1:
            return Integer.class;
          default:
            return String.class;
        }
      }

      public boolean isCellEditable(int row, int column) {
        return (column != 0) && (!JiveUtils.readOnly);
      }

      public void setValueAt(Object aValue, int row, int column) {
        if(column==1) {
          int level = ((Integer)aValue).intValue();
          serverInfos.get(row).level = level;
          updateTable();
        }
      }

    };

    serversTable = new JTable();
    serversTable.setDefaultRenderer(Integer.class,new LevelComboBoxRenderer(levels));
    serversTable.setDefaultEditor(Integer.class,new LevelComboBoxEditor(levels));
    serversTable.setModel(serversModel);
    serversTable.setRowHeight(25);
    serversView = new JScrollPane(serversTable);

    innerPanel.add(serversView,BorderLayout.CENTER);


    JPanel btnPanel = new JPanel();
    btnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
    btnPanel.setBorder(BorderFactory.createEtchedBorder());
    innerPanel.add(btnPanel,BorderLayout.SOUTH);

    clearButton = new JButton("Clear selected");
    clearButton.addActionListener(this);
    btnPanel.add(clearButton);

    clearAllButton = new JButton("Clear all");
    clearAllButton.addActionListener(this);
    btnPanel.add(clearAllButton);

    startButton = new JButton("Start server(s) on " + hostName);
    startButton.addActionListener(this);
    btnPanel.add(startButton);

    dismissButton = new JButton("Dismiss");
    dismissButton.addActionListener(this);
    btnPanel.add(dismissButton);

    innerPanel.setPreferredSize(new Dimension(640, 480));
    setContentPane(innerPanel);
    setTitle("Start Servers on " + hostName);
    updateTable();

  }

  public String getHost() throws DevFailed {

    final JDialog dlg = new JDialog(this,true);
    final StringBuffer result = new StringBuffer();
    final JList list = new JList(db.get_host_list());
    final JScrollPane listView = new JScrollPane(list);
    listView.setPreferredSize(new Dimension(0,300));
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    list.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if(e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) {
          Object s = list.getSelectedValue();
          if(s!=null) {
            result.append(s.toString());
            dlg.setVisible(false);
          }
        }
      }
    });

    JPanel innerPanel = new JPanel();
    innerPanel.setLayout(new BorderLayout());
    innerPanel.add(listView,BorderLayout.CENTER);

    JPanel btnPanel = new JPanel();
    btnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
    btnPanel.setBorder(BorderFactory.createEtchedBorder());
    innerPanel.add(btnPanel,BorderLayout.SOUTH);

    JButton selectButton = new JButton("Select");
    selectButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        result.append(list.getSelectedValue().toString());
        dlg.setVisible(false);
      }
    });
    btnPanel.add(selectButton);

    JButton dismissButton = new JButton("Dismiss");
    dismissButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        dlg.setVisible(false);
      }
    });
    btnPanel.add(dismissButton);

    innerPanel.add(btnPanel,BorderLayout.SOUTH);
    dlg.setTitle("Select Host");
    dlg.setContentPane(innerPanel);
    dlg.pack();
    dlg.setLocationRelativeTo(this);
    dlg.setVisible(true);

    return result.toString();

  }

  public void setServerList(String[] list) {
    serverText.setTips(list);
  }

  private void updateTable() {

    String colNames[] = {"Server" , "Level" , "Current host"};
    Object[][] info = new Object[serverInfos.size()][3];
    for(int i=0;i<serverInfos.size();i++) {
      info[i][0] = serverInfos.get(i).serverName;
      info[i][1] = new Integer(serverInfos.get(i).level);
      info[i][2] = serverInfos.get(i).host;
    }
    serversModel.setDataVector(info, colNames);
    serversTable.getColumnModel().getColumn(0).setPreferredWidth(250);

  }

  private void addServer(String srvName) throws DevFailed {

    // Ignore Starter
    if(srvName.startsWith("Starter"))
      return;

    boolean found = false;
    int i = 0;
    while(!found && i<serverInfos.size()) {
      found = srvName.equals(serverInfos.get(i).serverName);
      if(!found) i++;
    }

    if (!found) {

      DbServInfo srvInfo = db.get_server_info(srvName);

      ServerInfo si = new ServerInfo();
      si.serverName = srvName;
      if (srvInfo.controlled)
        si.level = srvInfo.startup_level + 1;
      else
        si.level = 1;
      si.host = srvInfo.host;
      serverInfos.add(si);

    }

  }

  private void addServers() {

    try {

      String[] toAdd;

      String srvName = serverText.getText();
      if (srvName.contains("*")) {
        toAdd = db.get_server_list(srvName);
      } else {
        toAdd = new String[1];
        toAdd[0] = srvName;
      }

      for(int i=0;i<toAdd.length;i++)
        addServer(toAdd[i]);

    } catch (DevFailed ex) {
      JiveUtils.showTangoError(ex);
    }

    updateTable();

  }

  private void addServerFromHost(String hostName) throws DevFailed {

    if( hostName.length()>0 ) {
      String[] toAdd = db.get_host_server_list(hostName);
      for(int i=0;i<toAdd.length;i++)
        addServer(toAdd[i]);
    }

  }

  private boolean ping(String srvName) throws DevFailed {

    String deviceName = "dserver/" + srvName;
    DeviceProxy dev = new DeviceProxy(deviceName);
    try {
      dev.ping();
      return true;
    } catch (DevFailed e) {
    }

    return false;

  }

  private void registerServer(String srvName,String hostName) throws DevFailed {

    // Register server (export/unexport admin device ) to be known by starter.
    String devname = "dserver/" + srvName;
    DbDevExportInfo info =
        new DbDevExportInfo(devname, "null", hostName, "null");
    db.export_device(info);
    db.unexport_device(devname);

  }

  private void startServer(String srvName) throws DevFailed {

    DeviceData argin = new DeviceData();
    argin.insert(srvName);
    starter.command_inout("DevStart", argin);

  }

  private void updateLevel(String srvName,int level) throws DevFailed {

    DbServer dbServer = new DbServer(srvName);
    DbServInfo info = dbServer.get_info();

    if(level==0) {

      //	Remove Server info in database
      dbServer.put_info(new DbServInfo(srvName, hostName, false, 0));

      //	Register devices on empty host and unexport.
      String[] deviceName = dbServer.get_device_class_list();
      for (int i = 0; i < deviceName.length; i += 2) {
        db.export_device(new DbDevExportInfo(deviceName[i], "", "", ""));
        db.unexport_device(deviceName[i]);
      }

    } else {

      info.host = hostName;
      info.startup_level = level-1;
      info.controlled = info.startup_level!=0;
      dbServer.put_info(info);

    }

  }

  public boolean startServers() {

    StringBuffer errStr = new StringBuffer();

    if(serverInfos.size()==0) {
      JiveUtils.showJiveError("Nothing to start !");
      return false;
    }

    if(serverInfos.size()>1) ProgressFrame.displayProgress("Starting servers");
    for(int i=0;i<serverInfos.size();i++) {

      String srvName = serverInfos.get(i).serverName;
      ProgressFrame.setProgress("Processing " + srvName,(i*100)/serverInfos.size() );

      try {
        if(ping(srvName)) {
          errStr.append(srvName + " is already running !\n");
        } else {
           registerServer(srvName,hostName);
           startServer(srvName);
           updateLevel(srvName,serverInfos.get(i).level);
        }
      } catch (DevFailed e) {
        errStr.append(srvName + ":" + e.errors[0].desc + "\n");
      }

    }

    try {
      starter.command_inout("UpdateServersInfo");
    } catch (DevFailed e) {
      errStr.append("UpdateServersInfo failed on " + starter.get_name() + " : " + e.errors[0].desc);
    }

    ProgressFrame.hideProgress();

    if(errStr.length()>0) {
      JiveUtils.showJiveError(errStr.toString());
    }

    return true;

  }

  @Override
  public void actionPerformed(ActionEvent e) {

    Object src = e.getSource();

    if(src==dismissButton) {

      setVisible(false);

    } else if(src==addButton || src==serverText) {

      addServers();

    } else if(src==addFromButton) {

      try {
        addServerFromHost(getHost());
      } catch (DevFailed ex) {
        JiveUtils.showTangoError(ex);
      }
      updateTable();

    } else if (src==clearButton) {

      int[] rows = serversTable.getSelectedRows();
      Arrays.sort(rows);
      for(int i=rows.length-1;i>=0;i--) {
        serverInfos.remove(rows[i]);
      }
      updateTable();

    } else if (src==clearAllButton) {

      serverInfos.clear();
      updateTable();

    } else if (src==startButton) {

      if( startServers() ) {
        setVisible(false);
        invoker.getHsotCollectionTreePanel().refresh();
      }

    }

  }

}
