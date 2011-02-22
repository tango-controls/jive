package jive3;

import fr.esrf.tangoatk.widget.util.ATKConstant;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;

import jive.JiveUtils;

public class DeviceEventPanel extends JPanel implements MouseListener,ActionListener {

  private JTabbedPane tabPane;
  private JTable      changeTable;
  private DefaultTableModel changeModel;
  private JTable      archTable;
  private DefaultTableModel archModel;
  private JTable      perTable;
  private DefaultTableModel perModel;
  private JButton     refreshButton;
  private JButton     applyButton;
  private JTable      selectedTable;
  private int[]       selectedRows;

  private JPopupMenu  tableMenu;
  private JMenuItem   resetMenuItem;
  private JMenuItem   setAbsMenuItem;
  private JMenuItem   setRelMenuItem;
  private JMenuItem   setPeriodMenuItem;


  private TaskEventNode[]   source = null;

  DeviceEventPanel()  {

    setLayout(new BorderLayout());

    // -- Change event table -------------------------------
    changeModel = new DefaultTableModel() {

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
          switch(column) {
            case 1:
              if(nb>1) ProgressFrame.displayProgress("Updating event config");
              for(int i=0;i<source.length;i++) {
                k++;
                ProgressFrame.setProgress("Updating " + source[i].getName() + "/abs_change",
                                          (k*100)/nb );
                source[i].setAbsChange(row,(String)aValue);
              }
              ProgressFrame.hideProgress();
              refreshValue();
              break;
            case 2:
              if(nb>1) ProgressFrame.displayProgress("Updating event config");
              for(int i=0;i<source.length;i++) {
                k++;
                ProgressFrame.setProgress("Updating " + source[i].getName() + "/rel_change",
                                          (k*100)/nb );
                source[i].setRelChange(row,(String)aValue);
              }
              ProgressFrame.hideProgress();
              refreshValue();

              break;
          }
        }
      }

    };
    changeTable = new JTable(changeModel);
    changeTable.addMouseListener(this);
    JScrollPane changeView = new JScrollPane(changeTable);

    // -- Archive event table -------------------------------
    archModel = new DefaultTableModel() {

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
          switch(column) {
            case 1:
              if(nb>1) ProgressFrame.displayProgress("Updating event config");
              for(int i=0;i<source.length;i++) {
                k++;
                ProgressFrame.setProgress("Updating " + source[i].getName() + "/arch_abs_change",
                                          (k*100)/nb );
                source[i].setArchAbsChange(row,(String)aValue);
              }
              ProgressFrame.hideProgress();
              refreshValue();
              break;
            case 2:
              if(nb>1) ProgressFrame.displayProgress("Updating event config");
              for(int i=0;i<source.length;i++) {
                k++;
                ProgressFrame.setProgress("Updating " + source[i].getName() + "/arch_rel_change",
                                          (k*100)/nb );
                source[i].setArchRelChange(row,(String)aValue);
              }
              ProgressFrame.hideProgress();
              refreshValue();
              break;
            case 3:
              if(nb>1) ProgressFrame.displayProgress("Updating event config");
              for(int i=0;i<source.length;i++) {
                k++;
                ProgressFrame.setProgress("Updating " + source[i].getName() + "/arch_period",
                                          (k*100)/nb );
                source[i].setArchPeriod(row,(String)aValue);
              }
              ProgressFrame.hideProgress();
              refreshValue();
              break;
          }
        }
      }

    };
    archTable = new JTable(archModel);
    archTable.addMouseListener(this);
    JScrollPane archView = new JScrollPane(archTable);

    // -- Periodic event table -------------------------------
    perModel = new DefaultTableModel() {

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
          switch(column) {
            case 1:
              if(nb>1) ProgressFrame.displayProgress("Updating event config");
              for(int i=0;i<source.length;i++) {
                k++;
                ProgressFrame.setProgress("Updating " + source[i].getName() + "/period",
                                          (k*100)/nb );
                source[i].setPeriodic(row,(String)aValue);
              }
              ProgressFrame.hideProgress();
              refreshValue();
              break;
          }
        }
      }

    };
    perTable = new JTable(perModel);
    perTable.addMouseListener(this);
    JScrollPane perView = new JScrollPane(perTable);

    tabPane = new JTabbedPane();
    tabPane.setFont(ATKConstant.labelFont);
    tabPane.add("Change event",changeView);
    tabPane.add("Archive event",archView);
    tabPane.add("Periodic event",perView);

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
    add(btnPanel,BorderLayout.SOUTH);

    // Contextual menu
    tableMenu = new JPopupMenu();
    resetMenuItem = new JMenuItem("Reset to default value");
    resetMenuItem.addActionListener(this);
    tableMenu.add(resetMenuItem);
    setAbsMenuItem = new JMenuItem("Set absolute change");
    setAbsMenuItem.addActionListener(this);
    tableMenu.add(setAbsMenuItem);
    setRelMenuItem = new JMenuItem("Set relative change");
    setRelMenuItem.addActionListener(this);
    tableMenu.add(setRelMenuItem);
    setPeriodMenuItem = new JMenuItem("Set period");
    setPeriodMenuItem.addActionListener(this);
    tableMenu.add(setPeriodMenuItem);


  }
  // Mouse listener -------------------------------------------

  public void mousePressed(MouseEvent e) {}
  public void mouseClicked(MouseEvent e) {

    selectedTable = (JTable)e.getSource();

    if(e.getButton() == MouseEvent.BUTTON3 && e.getClickCount()==1 && !JiveUtils.readOnly) {

      int row = getRowForLocation(e.getY());
      if(row!=-1) {
        selectedTable.addRowSelectionInterval(row,row);
        selectedTable.setColumnSelectionInterval(0,selectedTable.getColumnCount()-1);
        selectedRows = selectedTable.getSelectedRows();
        if(selectedTable == changeTable) {
          setAbsMenuItem.setVisible(true);
          setRelMenuItem.setVisible(true);
          setPeriodMenuItem.setVisible(false);
        } else if (selectedTable == archTable) {
          setAbsMenuItem.setVisible(true);
          setRelMenuItem.setVisible(true);
          setPeriodMenuItem.setVisible(true);
        } else {
          setAbsMenuItem.setVisible(false);
          setRelMenuItem.setVisible(false);
          setPeriodMenuItem.setVisible(true);
        }
        tableMenu.show(selectedTable, e.getX(), e.getY());
      } else {
        selectedTable = null;
      }

    }

  }
  public void mouseReleased(MouseEvent e) {}
  public void mouseEntered(MouseEvent e) {}
  public void mouseExited(MouseEvent e) {}

  // Action listener -------------------------------------------

  public void actionPerformed(ActionEvent e) {

    Object src = e.getSource();

    if( src==refreshButton ) {

      // Refresh the panel
      refreshValue();

    } else if (src==resetMenuItem) {

      int nb = selectedRows.length * source.length;
      int k = 0;
      if(nb>1) ProgressFrame.displayProgress("Reseting event config");

      if(selectedTable == changeTable) {

        for(int j=0;j<source.length;j++) {
          for(int i=0;i<selectedRows.length;i++) {
            k++;
            ProgressFrame.setProgress("Reseting " + source[j].getName() + "/" + changeModel.getValueAt(selectedRows[i],0),
                                        (k*100)/nb );
            source[j].resetChangeEvent(selectedRows[i]);
          }
          source[j].restartDevice();
        }

      } else if (selectedTable==archTable) {

        for(int j=0;j<source.length;j++) {
          for(int i=0;i<selectedRows.length;i++) {
            k++;
            ProgressFrame.setProgress("Reseting " + source[j].getName() + "/" + archModel.getValueAt(selectedRows[i],0),
                                        (k*100)/nb );
            source[j].resetArchEvent(selectedRows[i]);
          }
          source[j].restartDevice();
        }

      } else if (selectedTable==perTable) {

        for(int j=0;j<source.length;j++) {
          for(int i=0;i<selectedRows.length;i++) {
            k++;
            ProgressFrame.setProgress("Reseting " + source[j].getName() + "/" + perModel.getValueAt(selectedRows[i],0),
                                        (k*100)/nb );
            source[j].resetPeriodicEvent(selectedRows[i]);
          }
          source[j].restartDevice();
        }

      }
      ProgressFrame.hideProgress();
      refreshValue();

    } else if (src==setAbsMenuItem) {

      String abs = JOptionPane.showInputDialog(null,"Enter absolute change value","");
      if(abs==null) return;
      int nb = selectedRows.length * source.length;
      int k = 0;
      if(nb>1) ProgressFrame.displayProgress("Updating abs change");

      if(selectedTable == changeTable) {

        for(int j=0;j<source.length;j++) {
          for(int i=0;i<selectedRows.length;i++) {
            k++;
            ProgressFrame.setProgress("Updating " + source[j].getName() + "/" + changeModel.getValueAt(selectedRows[i],0),
                                        (k*100)/nb );
            source[j].setAbsChange(selectedRows[i],abs);
          }
        }

      } else if (selectedTable==archTable) {

        for(int j=0;j<source.length;j++) {
          for(int i=0;i<selectedRows.length;i++) {
            k++;
            ProgressFrame.setProgress("Updating " + source[j].getName() + "/" + archModel.getValueAt(selectedRows[i],0),
                                        (k*100)/nb );
            source[j].setArchAbsChange(selectedRows[i],abs);
          }
        }

      }
      ProgressFrame.hideProgress();
      refreshValue();

    } else if (src==setRelMenuItem) {

      String rel = JOptionPane.showInputDialog(null,"Enter relative change value","");
      if(rel==null) return;
      int nb = selectedRows.length * source.length;
      int k = 0;
      if(nb>1) ProgressFrame.displayProgress("Updating rel change");

      if(selectedTable == changeTable) {

        for(int j=0;j<source.length;j++) {
          for(int i=0;i<selectedRows.length;i++) {
            k++;
            ProgressFrame.setProgress("Updating " + source[j].getName() + "/" + changeModel.getValueAt(selectedRows[i],0),
                                        (k*100)/nb );
            source[j].setRelChange(selectedRows[i],rel);
          }
        }

      } else if (selectedTable==archTable) {

        for(int j=0;j<source.length;j++) {
          for(int i=0;i<selectedRows.length;i++) {
            k++;
            ProgressFrame.setProgress("Updating " + source[j].getName() + "/" + archModel.getValueAt(selectedRows[i],0),
                                        (k*100)/nb );
            source[j].setArchRelChange(selectedRows[i],rel);
          }
        }

      }
      ProgressFrame.hideProgress();
      refreshValue();

    } else if (src==setPeriodMenuItem) {

      String per = JOptionPane.showInputDialog(null,"Enter period value (ms)","");
      if(per==null) return;
      int nb = selectedRows.length * source.length;
      int k = 0;
      if(nb>1) ProgressFrame.displayProgress("Updating period");

      if(selectedTable == archTable) {

        for(int j=0;j<source.length;j++) {
          for(int i=0;i<selectedRows.length;i++) {
            k++;
            ProgressFrame.setProgress("Updating " + source[j].getName() + "/" + changeModel.getValueAt(selectedRows[i],0),
                                        (k*100)/nb );
            source[j].setArchPeriod(selectedRows[i],per);
          }
        }

      } else if (selectedTable==perTable) {

        for(int j=0;j<source.length;j++) {
          for(int i=0;i<selectedRows.length;i++) {
            k++;
            ProgressFrame.setProgress("Updating " + source[j].getName() + "/" + archModel.getValueAt(selectedRows[i],0),
                                        (k*100)/nb );
            source[j].setPeriodic(selectedRows[i],per);
          }
        }

      }
      ProgressFrame.hideProgress();
      refreshValue();

    } else if (src==applyButton) {

      if(changeTable.isEditing()) {
        String t = ((JTextField)changeTable.getEditorComponent()).getText();
        changeModel.setValueAt(t,changeTable.getEditingRow(),changeTable.getEditingColumn());
      }
      if(archTable.isEditing()) {
        String t = ((JTextField)archTable.getEditorComponent()).getText();
        archModel.setValueAt(t,archTable.getEditingRow(),archTable.getEditingColumn());
      }
      if(perTable.isEditing()) {
        String t = ((JTextField)perTable.getEditorComponent()).getText();
        perModel.setValueAt(t,perTable.getEditingRow(),perTable.getEditingColumn());
      }

    }

  }

  void setSource(TaskEventNode[] src) {
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

      //for(int i=0;i<source.length;i++)
      //  source[i].browseEventInfo();
      source[0].browseEventInfo();

      // Change event
      String changeColName[] = {"Attribute name" , "Absolute" , "Relative"};
      Object[][] changeInfo = new Object[source[0].getAttributeNumber()][3];
      for(int i=0;i<source[0].getAttributeNumber();i++) {
        changeInfo[i][0] = source[0].getAttName(i);
        changeInfo[i][1] = source[0].getAbsChange(i);
        changeInfo[i][2] = source[0].getRelChange(i);
      }
      changeModel.setDataVector(changeInfo, changeColName);
      changeTable.getColumnModel().getColumn(0).setPreferredWidth(200);

      // Archive event
      String archColName[] = {"Attribute name" , "Absolute" , "Relative" , "Period"};
      Object[][] archInfo = new Object[source[0].getAttributeNumber()][4];
      for(int i=0;i<source[0].getAttributeNumber();i++) {
        archInfo[i][0] = source[0].getAttName(i);
        archInfo[i][1] = source[0].getArchAbsChange(i);
        archInfo[i][2] = source[0].getArchRelChange(i);
        archInfo[i][3] = source[0].getArchPeriod(i);
      }
      archModel.setDataVector(archInfo, archColName);
      archTable.getColumnModel().getColumn(0).setPreferredWidth(200);

      // Periodic event
      String perColName[] = {"Attribute name" , "Period"};
      Object[][] perInfo = new Object[source[0].getAttributeNumber()][2];
      for(int i=0;i<source[0].getAttributeNumber();i++) {
        perInfo[i][0] = source[0].getAttName(i);
        perInfo[i][1] = source[0].getPeriodic(i);
      }
      perModel.setDataVector(perInfo, perColName);
      perTable.getColumnModel().getColumn(0).setPreferredWidth(200);

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



