package jive3;

import fr.esrf.tangoatk.widget.util.ATKConstant;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;

import jive.JiveUtils;

public class DevicePollingPanel extends JPanel implements MouseListener,ActionListener {

  private JTabbedPane tabPane;
  private JTable      cmdTable;
  private DefaultTableModel cmdModel;
  private JTable      attTable;
  private DefaultTableModel attModel;
  private JTable      adminTable;
  private DefaultTableModel adminModel;
  private JButton     refreshButton;
  private JButton     applyButton;
  private JButton     resetButton;
  private JPopupMenu  tableMenu;
  private JMenuItem   enablePollingMenuItem;
  private JMenuItem   disablePollingMenuItem;
  private JMenuItem   setPeriodMenuItem;

  private JTable selectedTable;
  private int[]  selectedRows;

  private TaskPollingNode[]   source = null;

  DevicePollingPanel()  {

    setLayout(new BorderLayout());

    // -- Command table -------------------------------
    cmdModel = new DefaultTableModel() {

      public Class getColumnClass(int columnIndex) {
        if(columnIndex==1)
          return Boolean.class;
        else
          return String.class;
      }
      public boolean isCellEditable(int row, int column) {
          return (column!=0) && (!JiveUtils.readOnly);
      }

      public void setValueAt(Object aValue, int row, int column) {
        if(!aValue.equals(getValueAt(row,column))) {
          super.setValueAt(aValue,row,column);
          String  name = (String)cmdModel.getValueAt(row,0);
          boolean polled = ((Boolean)cmdModel.getValueAt(row,1)).booleanValue();
          String  period = (String)cmdModel.getValueAt(row,2);
          if(column==2) { polled=true; }
          int nb = source.length;
          int k = 0;
          if(nb>1) ProgressFrame.displayProgress("Updating polling");
          for(int i=0;i<source.length;i++) {
            k++;
            ProgressFrame.setProgress("Updating " + source[i].getName() + "/" + name,
                                      (k*100)/nb );
            source[i].updateCommandPolling(name,polled,period);
          }
          ProgressFrame.hideProgress();
          refreshValue();
        }
      }

    };
    cmdTable = new JTable(cmdModel);
    cmdTable.addMouseListener(this);
    JScrollPane cmdView = new JScrollPane(cmdTable);

    // -- Attribute table -------------------------------
    attModel = new DefaultTableModel() {

      public Class getColumnClass(int columnIndex) {
        if(columnIndex==1)
          return Boolean.class;
        else
          return String.class;
      }
      public boolean isCellEditable(int row, int column) {
        return (column!=0) && (!JiveUtils.readOnly);
      }

      public void setValueAt(Object aValue, int row, int column) {
        if(!aValue.equals(getValueAt(row,column))) {
          super.setValueAt(aValue,row,column);
          String  name = (String)attModel.getValueAt(row,0);
          boolean polled = ((Boolean)attModel.getValueAt(row,1)).booleanValue();
          String  period = (String)attModel.getValueAt(row,2);
          if(column==2) { polled=true; }
          int nb = source.length;
          int k = 0;
          if(nb>1) ProgressFrame.displayProgress("Updating polling");
          for(int i=0;i<source.length;i++) {
            k++;
            ProgressFrame.setProgress("Updating " + source[i].getName() + "/" + name,
                                      (k*100)/nb );
            source[i].updateAttributePolling(name,polled,period);
          }
          ProgressFrame.hideProgress();
          refreshValue();
        }
      }

    };
    attTable = new JTable(attModel);
    attTable.addMouseListener(this);
    JScrollPane attView = new JScrollPane(attTable);

    // -- Admin table -------------------------------
    adminModel = new DefaultTableModel() {

      public Class getColumnClass(int columnIndex) {
        return String.class;
      }
      public boolean isCellEditable(int row, int column) {
        return (column!=0) && (!JiveUtils.readOnly);
      }

      public void setValueAt(Object aValue, int row, int column) {
        if(!aValue.equals(getValueAt(row,column))) {
          super.setValueAt(aValue,row,column);
          int nb = source.length;
          int k = 0;
          if(nb>1) ProgressFrame.displayProgress("Updating polling");
          if (row == 0) {
            for (int i = 0; i < source.length; i++) {
              k++;
              ProgressFrame.setProgress("Updating " + source[i].getName() + "/PollOldFactor",
                      (k * 100) / nb);
              source[i].setPollOldFactor((String) aValue);
            }
          } else {
            for (int i = 0; i < source.length; i++) {
              k++;
              ProgressFrame.setProgress("Updating " + source[i].getName() + "/PollRingDepth",
                      (k * 100) / nb);
              source[i].setPollRingDepth((String) aValue);
            }
          }
          ProgressFrame.hideProgress();
          refreshValue();
        }
      }

    };
    adminTable = new JTable(adminModel);
    adminTable.addMouseListener(this);
    JScrollPane adminView = new JScrollPane(adminTable);

    tabPane = new JTabbedPane();
    tabPane.setFont(ATKConstant.labelFont);
    tabPane.add("Command",cmdView);
    tabPane.add("Attribute",attView);
    tabPane.add("Settings",adminView);

    Border b = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"...");
    tabPane.setBorder(b);
    add(tabPane,BorderLayout.CENTER);

    // Bottom panel
    JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    refreshButton = new JButton("Refresh");
    refreshButton.addActionListener(this);
    btnPanel.add(refreshButton);
    applyButton = new JButton("Apply");
    applyButton.setEnabled(!JiveUtils.readOnly);
    applyButton.addActionListener(this);
    btnPanel.add(applyButton);
    resetButton = new JButton("Reset");
    resetButton.setEnabled(!JiveUtils.readOnly);
    resetButton.addActionListener(this);
    btnPanel.add(resetButton);
    add(btnPanel,BorderLayout.SOUTH);

    // Contextual menu
    tableMenu = new JPopupMenu();
    enablePollingMenuItem = new JMenuItem("Enable polling");
    enablePollingMenuItem.addActionListener(this);
    disablePollingMenuItem = new JMenuItem("Disable polling");
    disablePollingMenuItem.addActionListener(this);
    setPeriodMenuItem = new JMenuItem("Set period");
    setPeriodMenuItem.addActionListener(this);
    tableMenu.add(enablePollingMenuItem);
    tableMenu.add(disablePollingMenuItem);
    tableMenu.add(setPeriodMenuItem);

  }
  // Mouse listener -------------------------------------------

  public void mousePressed(MouseEvent e) {

    selectedTable = (JTable)e.getSource();

    if(e.getButton() == MouseEvent.BUTTON3 && e.getClickCount()==1 && !JiveUtils.readOnly) {

      if (selectedTable == attTable || selectedTable == cmdTable) {
        int row = getRowForLocation(e.getY());
        if (row != -1) {
          selectedTable.addRowSelectionInterval(row, row);
          selectedTable.setColumnSelectionInterval(0, 1);
          selectedRows = selectedTable.getSelectedRows();
          tableMenu.show(selectedTable, e.getX(), e.getY());
        }
      }

    }


  }
  public void mouseClicked(MouseEvent e) {}
  public void mouseReleased(MouseEvent e) {}
  public void mouseEntered(MouseEvent e) {}
  public void mouseExited(MouseEvent e) {}

  // Action listener -------------------------------------------

  public void actionPerformed(ActionEvent e) {

    Object src = e.getSource();

    if( src==refreshButton ) {

      // Refresh the panel
      refreshValue();

    } else if (src==applyButton) {

      if(cmdTable.isEditing()) {
        String t = ((JTextField)cmdTable.getEditorComponent()).getText();
        cmdModel.setValueAt(t,cmdTable.getEditingRow(),cmdTable.getEditingColumn());
      }
      if(attTable.isEditing()) {
        String t = ((JTextField)attTable.getEditorComponent()).getText();
        attModel.setValueAt(t,attTable.getEditingRow(),attTable.getEditingColumn());
      }
      if(adminTable.isEditing()) {
        String t = ((JTextField)adminTable.getEditorComponent()).getText();
        adminModel.setValueAt(t,adminTable.getEditingRow(),adminTable.getEditingColumn());
      }

    } else if (src==resetButton) {

      int ok = JOptionPane.showConfirmDialog(null,
                                             "This will reset polling configuration for all\n" +
                                             "attributes and commands.\nReset configuration ?",
                                             "Confirm reset", JOptionPane.YES_NO_OPTION);
      if (ok == JOptionPane.YES_OPTION) {

        int nb = source.length;
        int k = 0;
        if(nb>1) ProgressFrame.displayProgress("Reseting config");
        for (int i = 0; i < source.length; i++) {
          k++;
          ProgressFrame.setProgress("Reseting " + source[i].getName(), (k*100)/nb);
          source[i].resetConfig();
        }
        ProgressFrame.hideProgress();
        JOptionPane.showMessageDialog(null,"The polling configuration has been succesfully reset.\n" +
                                           "The server needs to be restarted to take change into account.");
      }

    } else if (src==enablePollingMenuItem) {

      int nb = selectedRows.length * source.length;
      int k = 0;
      if(nb>1) ProgressFrame.displayProgress("Enabling polling");
      TableModel model = selectedTable.getModel();
      for(int j=0;j<source.length;j++) {
        for(int i=0;i<selectedRows.length;i++) {
          k++;
          ProgressFrame.setProgress("Enabling " + source[j].getName() + "/" + model.getValueAt(selectedRows[i],0),
                                      (k*100)/nb );
          if (selectedTable == attTable) {
            source[j].updateAttributePolling((String) model.getValueAt(selectedRows[i], 0),
                                             true,
                                             (String) model.getValueAt(selectedRows[i], 2));
          } else if (selectedTable == cmdTable) {
            source[j].updateCommandPolling((String) model.getValueAt(selectedRows[i], 0),
                                           true,
                                           (String) model.getValueAt(selectedRows[i], 2));
          }
        }
      }
      ProgressFrame.hideProgress();
      refreshValue();

    } else if (src==disablePollingMenuItem) {

      int nb = selectedRows.length * source.length;
      int k = 0;
      if(nb>1) ProgressFrame.displayProgress("Disabling polling");
      TableModel model = selectedTable.getModel();
      for(int j=0;j<source.length;j++) {
        for(int i=0;i<selectedRows.length;i++) {
          k++;
          ProgressFrame.setProgress("Disabling " + source[j].getName() + "/" + model.getValueAt(selectedRows[i],0),
                                      (k*100)/nb );
          if (selectedTable == attTable) {
            source[j].updateAttributePolling((String) model.getValueAt(selectedRows[i], 0),
                                             false,
                                             (String) model.getValueAt(selectedRows[i], 2));
          } else if (selectedTable == cmdTable) {
            source[j].updateCommandPolling((String) model.getValueAt(selectedRows[i], 0),
                                           false,
                                           (String) model.getValueAt(selectedRows[i], 2));
          }
        }
      }
      ProgressFrame.hideProgress();
      refreshValue();

    } else if (src==setPeriodMenuItem) {

      String period = JOptionPane.showInputDialog(null,"Enter polling period (ms)","3000");
      if(period==null) return;

      int nb = selectedRows.length * source.length;
      int k = 0;
      if(nb>1) ProgressFrame.displayProgress("Updating polling period");
      TableModel model = selectedTable.getModel();
      for(int j=0;j<source.length;j++) {
        for(int i=0;i<selectedRows.length;i++) {
          k++;
          ProgressFrame.setProgress("Setting " + source[j].getName() + "/" + model.getValueAt(selectedRows[i],0),
                                      (k*100)/nb );
          if (selectedTable == attTable) {
            source[j].updateAttributePolling((String) model.getValueAt(selectedRows[i], 0),
                                             true,
                                             period);
          } else if (selectedTable == cmdTable) {
            source[j].updateCommandPolling((String) model.getValueAt(selectedRows[i], 0),
                                           true,
                                           period);
          }
        }
      }
      ProgressFrame.hideProgress();
      refreshValue();

    }

  }

  void setSource(TaskPollingNode[] src) {
    this.source = src;
    refreshValue();
  }

  // ---------------------------------------------------------------

  private int getRowForLocation(int y) {

    boolean found = false;
    int i = 0;
    int h = 0;

    while(i<selectedTable.getRowCount() && !found) {
      found = (y>=h && y<=h+selectedTable.getRowHeight(i));
      if(!found) {
        h+=selectedTable.getRowHeight(i);
        i++;
      }
    }

    if(found) {
      return i;
    } else {
      return -1;
    }

  }

  // ---------------------------------------------------------------

  private void refreshValue() {

    if (source != null) {

      source[0].browsePollingStatus();

      // Command
      String cmdColName[] = {"Command name" , "Polled" , "Period (ms)"};
      Object[][] cmdInfo = new Object[source[0].getCommandNumber()][3];
      for(int i=0;i<source[0].getCommandNumber();i++) {
        cmdInfo[i][0] = source[0].getCommandName(i);
        cmdInfo[i][1] = new Boolean(source[0].isCommandPolled(i));
        cmdInfo[i][2] = source[0].getCommandPollingPeriod(i);
      }
      cmdModel.setDataVector(cmdInfo, cmdColName);
      cmdTable.getColumnModel().getColumn(0).setPreferredWidth(200);

      // Attribute
      String attColName[] = {"Attribute name" , "Polled" , "Period (ms)"};
      Object[][] attInfo = new Object[source[0].getAttributeNumber()][3];
      for(int i=0;i<source[0].getAttributeNumber();i++) {
        attInfo[i][0] = source[0].getAttributeName(i);
        attInfo[i][1] = new Boolean(source[0].isAttributePolled(i));
        attInfo[i][2] = source[0].getAttributePollingPeriod(i);
      }
      attModel.setDataVector(attInfo, attColName);
      attTable.getColumnModel().getColumn(0).setPreferredWidth(200);

      // Settings
      String adminColName[] = {"Parameters name" , "Value" };
      Object[][] adminInfo = new Object[2][2];
      adminInfo[0][0] = "Poll old factor";
      adminInfo[0][1] = source[0].getPollOldFactor();
      adminInfo[1][0] = "Poll ring depth";
      adminInfo[1][1] = source[0].getPollRingDepth();
      adminModel.setDataVector(adminInfo, adminColName);
      adminTable.getColumnModel().getColumn(0).setPreferredWidth(200);

      String title = source[0].getTitle();
      if(source.length==1) {
        title += " [" + source[0].getName() + "]";
      } else {
        title += " [" + source.length + " devices selected]";
      }
      Border b = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),title);
      tabPane.setBorder(b);

    }

  }

}



