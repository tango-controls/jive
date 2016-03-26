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
import java.util.Vector;

import jive.JiveUtils;

public class DeviceAttributePanel extends JPanel implements MouseListener,ActionListener {

  private JTabbedPane tabPane;
  private JTable      alarmTable;
  private DefaultTableModel alarmModel;
  private JTable      unitTable;
  private DefaultTableModel unitModel;
  private JTable      rangeTable;
  private DefaultTableModel rangeModel;
  private JTable      displayTable;
  private DefaultTableModel displayModel;
  private JTable      descriptionTable;
  private DefaultTableModel descriptionModel;
  private JTable      aliasTable;
  private DefaultTableModel aliasModel;
  private JButton     refreshButton;
  private JButton     applyButton;

  private JPopupMenu  tableMenu;
  private JMenuItem   resetMenuItem;
  private JMenuItem   resetLMenuItem;
  private JMenuItem   resetULMenuItem;
  private JMenuItem   resetCULMenuItem;
  private JMenuItem   alarmMinMenuItem;
  private JMenuItem   alarmMaxMenuItem;
  private JMenuItem   warningMinMenuItem;
  private JMenuItem   warningMaxMenuItem;
  private JMenuItem   deltaTMenuItem;
  private JMenuItem   deltaValMenuItem;
  private JMenuItem   rangeMinMenuItem;
  private JMenuItem   rangeMaxMenuItem;
  private JMenuItem   unitMenuItem;
  private JMenuItem   displayUnitMenuItem;
  private JMenuItem   standardUnitMenuItem;
  private JMenuItem   labelMenuItem;
  private JMenuItem   formatMenuItem;
  private JMenuItem   descriptionMenuItem;


  private JTable      selectedTable;
  private int[]       selectedRows;

  private TaskAttributeNode[]   source = null;

  DeviceAttributePanel()  {

    setLayout(new BorderLayout());

    // -- Alarm table -------------------------------
    alarmModel = new DefaultTableModel() {

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

          // Confirmation dialog
          if (source.length > 1) {

            String name = (String)alarmModel.getValueAt(row,0);
            Vector propChange = new Vector();
            switch(column) {
              case 1:
                propChange.add("min_alarm");
                break;
              case 2:
                propChange.add("max_alarm");
                break;
              case 3:
                propChange.add("min_warning");
                break;
              case 4:
                propChange.add("max_warning");
                break;
              case 5:
                propChange.add("delta_t");
                break;
              case 6:
                propChange.add("delta_val");
                break;
            }
            propChange.add((String)aValue);

            if (!MultiChangeConfirmDlg.confirmChange(propChange, source.length," for attribute " + name)) {
              refreshValue();
              return;
            }

          }

          switch(column) {
            case 1:
              if(nb>1) ProgressFrame.displayProgress("Updating alarm");
              for(int i=0;i<source.length;i++) {
                k++;
                ProgressFrame.setProgress("Updating " + source[i].getName() + "/min_alarm",
                                          (k*100)/nb );
                source[i].setMinAlarm(row,(String)aValue);
              }
              ProgressFrame.hideProgress();
              refreshValue();
              break;
            case 2:
              if(nb>1) ProgressFrame.displayProgress("Updating alarm");
              for(int i=0;i<source.length;i++) {
                k++;
                ProgressFrame.setProgress("Updating " + source[i].getName() + "/max_alarm",
                                          (k*100)/nb );
                source[i].setMaxAlarm(row,(String)aValue);
              }
              ProgressFrame.hideProgress();
              refreshValue();
              break;
            case 3:
              if(nb>1) ProgressFrame.displayProgress("Updating alarm");
              for(int i=0;i<source.length;i++) {
                k++;
                ProgressFrame.setProgress("Updating " + source[i].getName() + "/min_warning",
                                          (k*100)/nb );
                source[i].setMinWarning(row,(String)aValue);
              }
              ProgressFrame.hideProgress();
              refreshValue();
              break;
            case 4:
              if(nb>1) ProgressFrame.displayProgress("Updating alarm");
              for(int i=0;i<source.length;i++) {
                k++;
                ProgressFrame.setProgress("Updating " + source[i].getName() + "/max_warning",
                                          (k*100)/nb );
                source[i].setMaxWarning(row,(String)aValue);
              }
              ProgressFrame.hideProgress();
              refreshValue();
              break;
            case 5:
              if(nb>1) ProgressFrame.displayProgress("Updating alarm");
              for(int i=0;i<source.length;i++) {
                k++;
                ProgressFrame.setProgress("Updating " + source[i].getName() + "/delta_t",
                                          (k*100)/nb );
                source[i].setDeltaT(row,(String)aValue);
              }
              ProgressFrame.hideProgress();
              refreshValue();
              break;
            case 6:
              if(nb>1) ProgressFrame.displayProgress("Updating alarm");
              for(int i=0;i<source.length;i++) {
                k++;
                ProgressFrame.setProgress("Updating " + source[i].getName() + "/delta_val",
                                          (k*100)/nb );
                source[i].setDeltaVal(row,(String)aValue);
              }
              ProgressFrame.hideProgress();
              refreshValue();
              break;
          }
        }
      }

    };
    alarmTable = new JTable(alarmModel);
    alarmTable.addMouseListener(this);
    JScrollPane alarmView = new JScrollPane(alarmTable);

    // -- Unit table -------------------------------
    unitModel = new DefaultTableModel() {

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

          // Confirmation dialog
          if (source.length > 1) {

            String name = (String)unitModel.getValueAt(row,0);
            Vector propChange = new Vector();
            switch(column) {
              case 1:
                propChange.add("unit");
                break;
              case 2:
                propChange.add("display_unit");
                break;
              case 3:
                propChange.add("standard_unit");
                break;
            }
            propChange.add((String)aValue);

            if (!MultiChangeConfirmDlg.confirmChange(propChange, source.length," for attribute " + name)) {
              refreshValue();
              return;
            }

          }

          switch(column) {
            case 1:
              if(nb>1) ProgressFrame.displayProgress("Updating unit");
              for(int i=0;i<source.length;i++) {
                k++;
                ProgressFrame.setProgress("Updating " + source[i].getName() + "/unit",
                                          (k*100)/nb );
                source[i].setUnit(row,(String)aValue);
              }
              ProgressFrame.hideProgress();
              refreshValue();
              break;
            case 2:
              if(nb>1) ProgressFrame.displayProgress("Updating unit");
              for(int i=0;i<source.length;i++) {
                k++;
                ProgressFrame.setProgress("Updating " + source[i].getName() + "/display_unit",
                                          (k*100)/nb );
                source[i].setDisplayUnit(row,(String)aValue);
              }
              ProgressFrame.hideProgress();
              refreshValue();
              break;
            case 3:
              if(nb>1) ProgressFrame.displayProgress("Updating unit");
              for(int i=0;i<source.length;i++) {
                k++;
                ProgressFrame.setProgress("Updating " + source[i].getName() + "/standard_unit",
                                          (k*100)/nb );
                source[i].setStandardUnit(row,(String)aValue);
              }
              ProgressFrame.hideProgress();
              refreshValue();
              break;
          }
        }
      }

    };
    unitTable = new JTable(unitModel);
    unitTable.addMouseListener(this);
    JScrollPane unitView = new JScrollPane(unitTable);

    // -- Range table -------------------------------
    rangeModel = new DefaultTableModel() {

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

          // Confirmation dialog
          if (source.length > 1) {

            String name = (String)rangeModel.getValueAt(row,0);
            Vector propChange = new Vector();
            switch(column) {
              case 1:
                propChange.add("min");
                break;
              case 2:
                propChange.add("max");
                break;
            }
            propChange.add((String)aValue);

            if (!MultiChangeConfirmDlg.confirmChange(propChange, source.length," for attribute " + name)) {
              refreshValue();
              return;
            }

          }

          switch(column) {
            case 1:
              if(nb>1) ProgressFrame.displayProgress("Updating range");
              for(int i=0;i<source.length;i++) {
                k++;
                ProgressFrame.setProgress("Updating " + source[i].getName() + "/min",
                                          (k*100)/nb );
                source[i].setMin(row,(String)aValue);
              }
              ProgressFrame.hideProgress();
              refreshValue();
              break;
            case 2:
              if(nb>1) ProgressFrame.displayProgress("Updating range");
              for(int i=0;i<source.length;i++) {
                k++;
                ProgressFrame.setProgress("Updating " + source[i].getName() + "/max",
                                          (k*100)/nb );
                source[i].setMax(row,(String)aValue);
              }
              ProgressFrame.hideProgress();
              refreshValue();
              break;
          }
        }
      }

    };
    rangeTable = new JTable(rangeModel);
    rangeTable.addMouseListener(this);
    JScrollPane rangeView = new JScrollPane(rangeTable);

    // -- Display table -------------------------------
    displayModel = new DefaultTableModel() {

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

          // Confirmation dialog
          if (source.length > 1) {

            String name = (String)displayModel.getValueAt(row,0);
            Vector propChange = new Vector();
            switch(column) {
              case 1:
                propChange.add("label");
                break;
              case 2:
                propChange.add("format");
                break;
            }
            propChange.add((String)aValue);

            if (!MultiChangeConfirmDlg.confirmChange(propChange, source.length," for attribute " + name)) {
              refreshValue();
              return;
            }

          }

          switch(column) {
            case 1:
              if(nb>1) ProgressFrame.displayProgress("Updating label");
              for(int i=0;i<source.length;i++) {
                k++;
                ProgressFrame.setProgress("Updating " + source[i].getName() + "/label",
                                          (k*100)/nb );
                source[i].setLabel(row,(String)aValue);
              }
              ProgressFrame.hideProgress();
              refreshValue();
              break;
            case 2:
              if(nb>1) ProgressFrame.displayProgress("Updating format");
              for(int i=0;i<source.length;i++) {
                k++;
                ProgressFrame.setProgress("Updating " + source[i].getName() + "/format",
                                          (k*100)/nb );
                source[i].setFormat(row,(String)aValue);
              }
              ProgressFrame.hideProgress();
              refreshValue();
              break;
          }
        }
      }

    };
    displayTable = new JTable(displayModel);
    displayTable.addMouseListener(this);
    JScrollPane displayView = new JScrollPane(displayTable);

    // -- Description table -------------------------------
    descriptionModel = new DefaultTableModel() {

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

          // Confirmation dialog
          if (source.length > 1) {

            String name = (String)descriptionModel.getValueAt(row,0);
            Vector propChange = new Vector();
            switch(column) {
              case 1:
                propChange.add("description");
                break;
            }
            propChange.add((String)aValue);

            if (!MultiChangeConfirmDlg.confirmChange(propChange, source.length," for attribute " + name)) {
              refreshValue();
              return;
            }

          }

          switch(column) {
            case 1:
              if(nb>1) ProgressFrame.displayProgress("Updating description");
              for(int i=0;i<source.length;i++) {
                k++;
                ProgressFrame.setProgress("Updating " + source[i].getName() + "/description",
                                          (k*100)/nb );
                source[i].setDescription(row,(String)aValue);
              }
              ProgressFrame.hideProgress();
              refreshValue();
              break;
          }
        }
      }

    };
    descriptionTable = new JTable(descriptionModel);
    descriptionTable.addMouseListener(this);
    JScrollPane descriptionView = new JScrollPane(descriptionTable);

    // -- alias table -------------------------------
    aliasModel = new DefaultTableModel() {

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
              if(nb>1) {
                JiveUtils.showJiveError("Cannot apply attribute alias to multiple instance");
              } else if(nb==1) {
                source[0].setAlias(row,(String)aValue);
              }
              refreshValue();
              break;
          }
        }
      }

    };
    aliasTable = new JTable(aliasModel);
    aliasTable.addMouseListener(this);
    JScrollPane aliasView = new JScrollPane(aliasTable);

    tabPane = new JTabbedPane();
    tabPane.setFont(ATKConstant.labelFont);
    tabPane.add("Display",displayView);
    tabPane.add("Unit",unitView);
    tabPane.add("Range",rangeView);
    tabPane.add("Alarms",alarmView);
    tabPane.add("Description",descriptionView);
    tabPane.add("Alias",aliasView);

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

    resetLMenuItem = new JMenuItem("Return to lib default value");
    resetLMenuItem.addActionListener(this);
    tableMenu.add(resetLMenuItem);
    resetULMenuItem = new JMenuItem("Return to code/lib default value");
    resetULMenuItem.addActionListener(this);
    tableMenu.add(resetULMenuItem);
    resetCULMenuItem = new JMenuItem("Return to class/code/lib default value");
    resetCULMenuItem.addActionListener(this);
    tableMenu.add(resetCULMenuItem);

    alarmMinMenuItem = new JMenuItem("Set min alarm");
    alarmMinMenuItem.addActionListener(this);
    tableMenu.add(alarmMinMenuItem);
    alarmMaxMenuItem = new JMenuItem("Set max alarm");
    alarmMaxMenuItem.addActionListener(this);
    tableMenu.add(alarmMaxMenuItem);
    warningMinMenuItem = new JMenuItem("Set min warning");
    warningMinMenuItem.addActionListener(this);
    tableMenu.add(warningMinMenuItem);
    warningMaxMenuItem = new JMenuItem("Set max warning");
    warningMaxMenuItem.addActionListener(this);
    tableMenu.add(warningMaxMenuItem);
    deltaTMenuItem = new JMenuItem("Set delta time");
    deltaTMenuItem.addActionListener(this);
    tableMenu.add(deltaTMenuItem);
    deltaValMenuItem = new JMenuItem("Set delta value");
    deltaValMenuItem.addActionListener(this);
    tableMenu.add(deltaValMenuItem);
    rangeMinMenuItem = new JMenuItem("Set min");
    rangeMinMenuItem.addActionListener(this);
    tableMenu.add(rangeMinMenuItem);
    rangeMaxMenuItem = new JMenuItem("Set max");
    rangeMaxMenuItem.addActionListener(this);
    tableMenu.add(rangeMaxMenuItem);
    unitMenuItem = new JMenuItem("Set unit");
    unitMenuItem.addActionListener(this);
    tableMenu.add(unitMenuItem);
    displayUnitMenuItem = new JMenuItem("Set display unit");
    displayUnitMenuItem.addActionListener(this);
    tableMenu.add(displayUnitMenuItem);
    standardUnitMenuItem = new JMenuItem("Set standard unit");
    standardUnitMenuItem.addActionListener(this);
    tableMenu.add(standardUnitMenuItem);
    labelMenuItem = new JMenuItem("Set label");
    labelMenuItem.addActionListener(this);
    tableMenu.add(labelMenuItem);
    formatMenuItem = new JMenuItem("Set format");
    formatMenuItem.addActionListener(this);
    tableMenu.add(formatMenuItem);
    descriptionMenuItem = new JMenuItem("Set description");
    descriptionMenuItem.addActionListener(this);
    tableMenu.add(descriptionMenuItem);

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

        alarmMinMenuItem.setVisible(false);
        alarmMaxMenuItem.setVisible(false);
        warningMinMenuItem.setVisible(false);
        warningMaxMenuItem.setVisible(false);
        deltaTMenuItem.setVisible(false);
        deltaValMenuItem.setVisible(false);
        rangeMinMenuItem.setVisible(false);
        rangeMaxMenuItem.setVisible(false);
        unitMenuItem.setVisible(false);
        displayUnitMenuItem.setVisible(false);
        standardUnitMenuItem.setVisible(false);
        labelMenuItem.setVisible(false);
        formatMenuItem.setVisible(false);
        descriptionMenuItem.setVisible(false);

        if(selectedTable == alarmTable) {

          alarmMinMenuItem.setVisible(true);
          alarmMaxMenuItem.setVisible(true);
          warningMinMenuItem.setVisible(true);
          warningMaxMenuItem.setVisible(true);
          deltaTMenuItem.setVisible(true);
          deltaValMenuItem.setVisible(true);

        } else if (selectedTable == unitTable) {

          unitMenuItem.setVisible(true);
          displayUnitMenuItem.setVisible(true);
          standardUnitMenuItem.setVisible(true);

        } else if (selectedTable == rangeTable) {

          rangeMinMenuItem.setVisible(true);
          rangeMaxMenuItem.setVisible(true);

        } else if (selectedTable == displayTable) {

          labelMenuItem.setVisible(true);
          formatMenuItem.setVisible(true);

        } else if (selectedTable == descriptionTable) {

          descriptionMenuItem.setVisible(true);

        }

        boolean isTango8 = true;
        int i = 0;
        while(isTango8 && i<source.length) {
          isTango8 = source[i].isTango8();
          i++;
        }

        if( isTango8 ) {
          resetLMenuItem.setVisible(true);
          resetULMenuItem.setVisible(true);
          resetCULMenuItem.setVisible(true);
        } else {
          resetLMenuItem.setVisible(false);
          resetULMenuItem.setVisible(false);
          resetCULMenuItem.setVisible(false);
        }
        
        tableMenu.show(selectedTable, e.getX(), e.getY());
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

      // Reset to default menu item -----------------------------------------------------
      int nb = selectedRows.length * source.length;
      int k = 0;

      if(selectedTable == alarmTable) {

        // Confirmation message
        if( source.length>1 ) {
          String message = "You are going to reset alarm configuration for " + source.length + " device(s) and " + selectedRows.length + " attribute(s).\nDo you want to proceed ?";
          int result = JOptionPane.showConfirmDialog(this,message,"Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
          if( result==JOptionPane.NO_OPTION )
            return;
        }

        if(nb>1) ProgressFrame.displayProgress("Reseting alarms");
        for(int j=0;j<source.length;j++) {
          for(int i=0;i<selectedRows.length;i++) {
            k++;
            ProgressFrame.setProgress("Reseting " + source[j].getName() + "/" + alarmModel.getValueAt(selectedRows[i],0),
                                        (k*100)/nb );
            source[j].resetAlarms(selectedRows[i]);
          }
          source[j].restartDevice();
        }
        ProgressFrame.hideProgress();
        refreshValue();

      } else if (selectedTable == unitTable) {

        // Confirmation message
        if( source.length>1 ) {
          String message = "You are going to reset unit configuration for " + source.length + " device(s) and " + selectedRows.length + " attribute(s).\nDo you want to proceed ?";
          int result = JOptionPane.showConfirmDialog(this,message,"Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
          if( result==JOptionPane.NO_OPTION )
            return;
        }

        if(nb>1) ProgressFrame.displayProgress("Reseting units");
        for(int j=0;j<source.length;j++) {
          for(int i=0;i<selectedRows.length;i++) {
            k++;
            ProgressFrame.setProgress("Reseting " + source[j].getName() + "/" + unitModel.getValueAt(selectedRows[i],0),
                                        (k*100)/nb );
            source[j].resetUnit(selectedRows[i]);
          }
          source[j].restartDevice();
        }
        ProgressFrame.hideProgress();
        refreshValue();

      } else if (selectedTable == rangeTable) {

        // Confirmation message
        if( source.length>1 ) {
          String message = "You are going to reset range configuration for " + source.length + " device(s) and " + selectedRows.length + " attribute(s).\nDo you want to proceed ?";
          int result = JOptionPane.showConfirmDialog(this,message,"Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
          if( result==JOptionPane.NO_OPTION )
            return;
        }

        if(nb>1) ProgressFrame.displayProgress("Reseting ranges");
        for(int j=0;j<source.length;j++) {
          for(int i=0;i<selectedRows.length;i++) {
            k++;
            ProgressFrame.setProgress("Reseting " + source[j].getName() + "/" + rangeModel.getValueAt(selectedRows[i],0),
                                        (k*100)/nb );
            source[j].resetRange(selectedRows[i]);
          }
          source[j].restartDevice();
        }
        ProgressFrame.hideProgress();
        refreshValue();

      } else if (selectedTable == displayTable) {

        // Confirmation message
        if( source.length>1 ) {
          String message = "You are going to reset display configuration for " + source.length + " device(s) and " + selectedRows.length + " attribute(s).\nDo you want to proceed ?";
          int result = JOptionPane.showConfirmDialog(this,message,"Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
          if( result==JOptionPane.NO_OPTION )
            return;
        }

        if(nb>1) ProgressFrame.displayProgress("Reseting display");
        for(int j=0;j<source.length;j++) {
          for(int i=0;i<selectedRows.length;i++) {
            k++;
            ProgressFrame.setProgress("Reseting " + source[j].getName() + "/" + displayModel.getValueAt(selectedRows[i],0),
                                        (k*100)/nb );
            source[j].resetDisplay(selectedRows[i]);
          }
          source[j].restartDevice();
        }
        ProgressFrame.hideProgress();
        refreshValue();

      } else if (selectedTable == descriptionTable) {

        // Confirmation message
        if( source.length>1 ) {
          String message = "You are going to reset description for " + source.length + " device(s) and " + selectedRows.length + " attribute(s).\nDo you want to proceed ?";
          int result = JOptionPane.showConfirmDialog(this,message,"Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
          if( result==JOptionPane.NO_OPTION )
            return;
        }

        if(nb>1) ProgressFrame.displayProgress("Reseting description");
        for(int j=0;j<source.length;j++) {
          for(int i=0;i<selectedRows.length;i++) {
            k++;
            ProgressFrame.setProgress("Reseting " + source[j].getName() + "/" + descriptionModel.getValueAt(selectedRows[i],0),
                                        (k*100)/nb );
            source[j].resetDescription(selectedRows[i]);
          }
          source[j].restartDevice();
        }
        ProgressFrame.hideProgress();
        refreshValue();

      } else if (selectedTable == aliasTable) {

        // Confirmation message
        if( source.length>1 ) {
          String message = "You are going to reset alias for " + source.length + " device(s) and " + selectedRows.length + " attribute(s).\nDo you want to proceed ?";
          int result = JOptionPane.showConfirmDialog(this,message,"Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
          if( result==JOptionPane.NO_OPTION )
            return;
        }

        if(nb>1) ProgressFrame.displayProgress("Reseting alias");
        for(int j=0;j<source.length;j++) {
          for(int i=0;i<selectedRows.length;i++) {
            k++;
            ProgressFrame.setProgress("Reseting " + source[j].getName() + "/" + aliasModel.getValueAt(selectedRows[i],0),
                                        (k*100)/nb );
            source[j].setAlias(selectedRows[i],"");
          }
        }
        ProgressFrame.hideProgress();
        refreshValue();

      }

    } else if (src==resetLMenuItem) {

      // Reset to library default menu item -----------------------------------------------------
      int nb = selectedRows.length * source.length;
      int k = 0;

      if(selectedTable == alarmTable) {

        // Confirmation message
        if( source.length>1 ) {
          String message = "You are going to reset alarm configuration to library value for " + source.length + " device(s) and " + selectedRows.length + " attribute(s).\nDo you want to proceed ?";
          int result = JOptionPane.showConfirmDialog(this,message,"Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
          if( result==JOptionPane.NO_OPTION )
            return;
        }

        if(nb>1) ProgressFrame.displayProgress("Reseting alarms");
        for(int j=0;j<source.length;j++) {
          for(int i=0;i<selectedRows.length;i++) {
            k++;
            ProgressFrame.setProgress("Reseting " + source[j].getName() + "/" + alarmModel.getValueAt(selectedRows[i],0),
                                        (k*100)/nb );
            source[j].resetLAlarms(selectedRows[i]);
          }
        }
        ProgressFrame.hideProgress();
        refreshValue();

      } else if (selectedTable == unitTable) {

        // Confirmation message
        if( source.length>1 ) {
          String message = "You are going to reset unit configuration to library value for " + source.length + " device(s) and " + selectedRows.length + " attribute(s).\nDo you want to proceed ?";
          int result = JOptionPane.showConfirmDialog(this,message,"Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
          if( result==JOptionPane.NO_OPTION )
            return;
        }

        if(nb>1) ProgressFrame.displayProgress("Reseting units");
        for(int j=0;j<source.length;j++) {
          for(int i=0;i<selectedRows.length;i++) {
            k++;
            ProgressFrame.setProgress("Reseting " + source[j].getName() + "/" + unitModel.getValueAt(selectedRows[i],0),
                                        (k*100)/nb );
            source[j].resetLUnit(selectedRows[i]);
          }
        }
        ProgressFrame.hideProgress();
        refreshValue();

      } else if (selectedTable == rangeTable) {

        // Confirmation message
        if( source.length>1 ) {
          String message = "You are going to reset range configuration to library value for " + source.length + " device(s) and " + selectedRows.length + " attribute(s).\nDo you want to proceed ?";
          int result = JOptionPane.showConfirmDialog(this,message,"Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
          if( result==JOptionPane.NO_OPTION )
            return;
        }

        if(nb>1) ProgressFrame.displayProgress("Reseting ranges");
        for(int j=0;j<source.length;j++) {
          for(int i=0;i<selectedRows.length;i++) {
            k++;
            ProgressFrame.setProgress("Reseting " + source[j].getName() + "/" + rangeModel.getValueAt(selectedRows[i],0),
                                        (k*100)/nb );
            source[j].resetLRange(selectedRows[i]);
          }
        }
        ProgressFrame.hideProgress();
        refreshValue();

      } else if (selectedTable == displayTable) {

        // Confirmation message
        if( source.length>1 ) {
          String message = "You are going to reset display configuration to library value for " + source.length + " device(s) and " + selectedRows.length + " attribute(s).\nDo you want to proceed ?";
          int result = JOptionPane.showConfirmDialog(this,message,"Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
          if( result==JOptionPane.NO_OPTION )
            return;
        }

        if(nb>1) ProgressFrame.displayProgress("Reseting display");
        for(int j=0;j<source.length;j++) {
          for(int i=0;i<selectedRows.length;i++) {
            k++;
            ProgressFrame.setProgress("Reseting " + source[j].getName() + "/" + displayModel.getValueAt(selectedRows[i],0),
                                        (k*100)/nb );
            source[j].resetLDisplay(selectedRows[i]);
          }
        }
        ProgressFrame.hideProgress();
        refreshValue();

      } else if (selectedTable == descriptionTable) {

        // Confirmation message
        if( source.length>1 ) {
          String message = "You are going to reset description to library value for " + source.length + " device(s) and " + selectedRows.length + " attribute(s).\nDo you want to proceed ?";
          int result = JOptionPane.showConfirmDialog(this,message,"Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
          if( result==JOptionPane.NO_OPTION )
            return;
        }

        if(nb>1) ProgressFrame.displayProgress("Reseting description");
        for(int j=0;j<source.length;j++) {
          for(int i=0;i<selectedRows.length;i++) {
            k++;
            ProgressFrame.setProgress("Reseting " + source[j].getName() + "/" + descriptionModel.getValueAt(selectedRows[i],0),
                                        (k*100)/nb );
            source[j].resetLDescription(selectedRows[i]);
          }
        }
        ProgressFrame.hideProgress();
        refreshValue();
      }

    } else if (src==resetULMenuItem) {

      // Reset to user/library default menu item -----------------------------------------------------
      int nb = selectedRows.length * source.length;
      int k = 0;

      if(selectedTable == alarmTable) {

        // Confirmation message
        if( source.length>1 ) {
          String message = "You are going to reset alarm configuration to code/library value for " + source.length + " device(s) and " + selectedRows.length + " attribute(s).\nDo you want to proceed ?";
          int result = JOptionPane.showConfirmDialog(this,message,"Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
          if( result==JOptionPane.NO_OPTION )
            return;
        }

        if(nb>1) ProgressFrame.displayProgress("Reseting alarms");
        for(int j=0;j<source.length;j++) {
          for(int i=0;i<selectedRows.length;i++) {
            k++;
            ProgressFrame.setProgress("Reseting " + source[j].getName() + "/" + alarmModel.getValueAt(selectedRows[i],0),
                                        (k*100)/nb );
            source[j].resetULAlarms(selectedRows[i]);
          }
        }
        ProgressFrame.hideProgress();
        refreshValue();

      } else if (selectedTable == unitTable) {

        // Confirmation message
        if( source.length>1 ) {
          String message = "You are going to reset unit configuration to code/library value for " + source.length + " device(s) and " + selectedRows.length + " attribute(s).\nDo you want to proceed ?";
          int result = JOptionPane.showConfirmDialog(this,message,"Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
          if( result==JOptionPane.NO_OPTION )
            return;
        }

        if(nb>1) ProgressFrame.displayProgress("Reseting units");
        for(int j=0;j<source.length;j++) {
          for(int i=0;i<selectedRows.length;i++) {
            k++;
            ProgressFrame.setProgress("Reseting " + source[j].getName() + "/" + unitModel.getValueAt(selectedRows[i],0),
                                        (k*100)/nb );
            source[j].resetULUnit(selectedRows[i]);
          }
        }
        ProgressFrame.hideProgress();
        refreshValue();

      } else if (selectedTable == rangeTable) {

        // Confirmation message
        if( source.length>1 ) {
          String message = "You are going to reset range configuration to code/library value for " + source.length + " device(s) and " + selectedRows.length + " attribute(s).\nDo you want to proceed ?";
          int result = JOptionPane.showConfirmDialog(this,message,"Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
          if( result==JOptionPane.NO_OPTION )
            return;
        }

        if(nb>1) ProgressFrame.displayProgress("Reseting ranges");
        for(int j=0;j<source.length;j++) {
          for(int i=0;i<selectedRows.length;i++) {
            k++;
            ProgressFrame.setProgress("Reseting " + source[j].getName() + "/" + rangeModel.getValueAt(selectedRows[i],0),
                                        (k*100)/nb );
            source[j].resetULRange(selectedRows[i]);
          }
        }
        ProgressFrame.hideProgress();
        refreshValue();

      } else if (selectedTable == displayTable) {

        // Confirmation message
        if( source.length>1 ) {
          String message = "You are going to reset display configuration to code/library value for " + source.length + " device(s) and " + selectedRows.length + " attribute(s).\nDo you want to proceed ?";
          int result = JOptionPane.showConfirmDialog(this,message,"Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
          if( result==JOptionPane.NO_OPTION )
            return;
        }

        if(nb>1) ProgressFrame.displayProgress("Reseting display");
        for(int j=0;j<source.length;j++) {
          for(int i=0;i<selectedRows.length;i++) {
            k++;
            ProgressFrame.setProgress("Reseting " + source[j].getName() + "/" + displayModel.getValueAt(selectedRows[i],0),
                                        (k*100)/nb );
            source[j].resetULDisplay(selectedRows[i]);
          }
        }
        ProgressFrame.hideProgress();
        refreshValue();

      } else if (selectedTable == descriptionTable) {

        // Confirmation message
        if( source.length>1 ) {
          String message = "You are going to reset description to code/library value for " + source.length + " device(s) and " + selectedRows.length + " attribute(s).\nDo you want to proceed ?";
          int result = JOptionPane.showConfirmDialog(this,message,"Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
          if( result==JOptionPane.NO_OPTION )
            return;
        }

        if(nb>1) ProgressFrame.displayProgress("Reseting description");
        for(int j=0;j<source.length;j++) {
          for(int i=0;i<selectedRows.length;i++) {
            k++;
            ProgressFrame.setProgress("Reseting " + source[j].getName() + "/" + descriptionModel.getValueAt(selectedRows[i],0),
                                        (k*100)/nb );
            source[j].resetULDescription(selectedRows[i]);
          }
        }
        ProgressFrame.hideProgress();
        refreshValue();
      }

    } else if (src==resetCULMenuItem) {

      // Reset to class/user/library default menu item -----------------------------------------------------
      int nb = selectedRows.length * source.length;
      int k = 0;

      if(selectedTable == alarmTable) {

        // Confirmation message
        if( source.length>1 ) {
          String message = "You are going to reset alarm configuration to class/code/library value for " + source.length + " device(s) and " + selectedRows.length + " attribute(s).\nDo you want to proceed ?";
          int result = JOptionPane.showConfirmDialog(this,message,"Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
          if( result==JOptionPane.NO_OPTION )
            return;
        }

        if(nb>1) ProgressFrame.displayProgress("Reseting alarms");
        for(int j=0;j<source.length;j++) {
          for(int i=0;i<selectedRows.length;i++) {
            k++;
            ProgressFrame.setProgress("Reseting " + source[j].getName() + "/" + alarmModel.getValueAt(selectedRows[i],0),
                                        (k*100)/nb );
            source[j].resetCULAlarms(selectedRows[i]);
          }
        }
        ProgressFrame.hideProgress();
        refreshValue();

      } else if (selectedTable == unitTable) {

        // Confirmation message
        if( source.length>1 ) {
          String message = "You are going to reset unit configuration to class/code/library value for " + source.length + " device(s) and " + selectedRows.length + " attribute(s).\nDo you want to proceed ?";
          int result = JOptionPane.showConfirmDialog(this,message,"Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
          if( result==JOptionPane.NO_OPTION )
            return;
        }

        if(nb>1) ProgressFrame.displayProgress("Reseting units");
        for(int j=0;j<source.length;j++) {
          for(int i=0;i<selectedRows.length;i++) {
            k++;
            ProgressFrame.setProgress("Reseting " + source[j].getName() + "/" + unitModel.getValueAt(selectedRows[i],0),
                                        (k*100)/nb );
            source[j].resetCULUnit(selectedRows[i]);
          }
        }
        ProgressFrame.hideProgress();
        refreshValue();

      } else if (selectedTable == rangeTable) {

        // Confirmation message
        if( source.length>1 ) {
          String message = "You are going to reset range configuration to class/code/library value for " + source.length + " device(s) and " + selectedRows.length + " attribute(s).\nDo you want to proceed ?";
          int result = JOptionPane.showConfirmDialog(this,message,"Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
          if( result==JOptionPane.NO_OPTION )
            return;
        }

        if(nb>1) ProgressFrame.displayProgress("Reseting ranges");
        for(int j=0;j<source.length;j++) {
          for(int i=0;i<selectedRows.length;i++) {
            k++;
            ProgressFrame.setProgress("Reseting " + source[j].getName() + "/" + rangeModel.getValueAt(selectedRows[i],0),
                                        (k*100)/nb );
            source[j].resetCULRange(selectedRows[i]);
          }
        }
        ProgressFrame.hideProgress();
        refreshValue();

      } else if (selectedTable == displayTable) {

        // Confirmation message
        if( source.length>1 ) {
          String message = "You are going to reset display configuration to class/code/library value for " + source.length + " device(s) and " + selectedRows.length + " attribute(s).\nDo you want to proceed ?";
          int result = JOptionPane.showConfirmDialog(this,message,"Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
          if( result==JOptionPane.NO_OPTION )
            return;
        }

        if(nb>1) ProgressFrame.displayProgress("Reseting display");
        for(int j=0;j<source.length;j++) {
          for(int i=0;i<selectedRows.length;i++) {
            k++;
            ProgressFrame.setProgress("Reseting " + source[j].getName() + "/" + displayModel.getValueAt(selectedRows[i],0),
                                        (k*100)/nb );
            source[j].resetCULDisplay(selectedRows[i]);
          }
        }
        ProgressFrame.hideProgress();
        refreshValue();

      } else if (selectedTable == descriptionTable) {

        // Confirmation message
        if( source.length>1 ) {
          String message = "You are going to reset description to class/code/library value for " + source.length + " device(s) and " + selectedRows.length + " attribute(s).\nDo you want to proceed ?";
          int result = JOptionPane.showConfirmDialog(this,message,"Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
          if( result==JOptionPane.NO_OPTION )
            return;
        }

        if(nb>1) ProgressFrame.displayProgress("Reseting description");
        for(int j=0;j<source.length;j++) {
          for(int i=0;i<selectedRows.length;i++) {
            k++;
            ProgressFrame.setProgress("Reseting " + source[j].getName() + "/" + descriptionModel.getValueAt(selectedRows[i],0),
                                        (k*100)/nb );
            source[j].resetCULDescription(selectedRows[i]);
          }
        }
        ProgressFrame.hideProgress();
        refreshValue();
      }

    } else if (src==labelMenuItem) {

      String val = JOptionPane.showInputDialog(null,"Enter label","");
      if(val==null) return;
      int nb = selectedRows.length * source.length;
      int k = 0;

      // Confirmation dialog
      if (source.length > 1) {
        Vector propChange = new Vector();
        propChange.add("label");
        propChange.add(val);
        if (!MultiChangeConfirmDlg.confirmChange(propChange, source.length," and " + selectedRows.length + " attribute(s)")) {
          return;
        }
      }

      if(nb>1) ProgressFrame.displayProgress("Updating label");
      for(int j=0;j<source.length;j++) {
        for(int i=0;i<selectedRows.length;i++) {
          k++;
          ProgressFrame.setProgress("Updating " + source[j].getName() + "/" + displayModel.getValueAt(selectedRows[i],0),
                                      (k*100)/nb );
          source[j].setLabel(selectedRows[i],val);
        }
      }
      ProgressFrame.hideProgress();
      refreshValue();

    } else if (src==formatMenuItem) {

      String val = JOptionPane.showInputDialog(null,"Enter format","");
      if(val==null) return;
      int nb = selectedRows.length * source.length;
      int k = 0;

      // Confirmation dialog
      if (source.length > 1) {
        Vector propChange = new Vector();
        propChange.add("format");
        propChange.add(val);
        if (!MultiChangeConfirmDlg.confirmChange(propChange, source.length," and " + selectedRows.length + " attribute(s)")) {
          return;
        }
      }

      if(nb>1) ProgressFrame.displayProgress("Updating format");
      for(int j=0;j<source.length;j++) {
        for(int i=0;i<selectedRows.length;i++) {
          k++;
          ProgressFrame.setProgress("Updating " + source[j].getName() + "/" + displayModel.getValueAt(selectedRows[i],0),
                                      (k*100)/nb );
          source[j].setFormat(selectedRows[i],val);
        }
      }
      ProgressFrame.hideProgress();
      refreshValue();

    } else if (src==unitMenuItem) {

      String val = JOptionPane.showInputDialog(null,"Enter unit","");
      if(val==null) return;
      int nb = selectedRows.length * source.length;
      int k = 0;

      // Confirmation dialog
      if (source.length > 1) {
        Vector propChange = new Vector();
        propChange.add("unit");
        propChange.add(val);
        if (!MultiChangeConfirmDlg.confirmChange(propChange, source.length," and " + selectedRows.length + " attribute(s)")) {
          return;
        }
      }

      if(nb>1) ProgressFrame.displayProgress("Updating unit");
      for(int j=0;j<source.length;j++) {
        for(int i=0;i<selectedRows.length;i++) {
          k++;
          ProgressFrame.setProgress("Updating " + source[j].getName() + "/" + unitModel.getValueAt(selectedRows[i],0),
                                      (k*100)/nb );
          source[j].setUnit(selectedRows[i],val);
        }
      }
      ProgressFrame.hideProgress();
      refreshValue();

    } else if (src==displayUnitMenuItem) {

      String val = JOptionPane.showInputDialog(null,"Enter display unit","");
      if(val==null) return;
      int nb = selectedRows.length * source.length;
      int k = 0;

      // Confirmation dialog
      if (source.length > 1) {
        Vector propChange = new Vector();
        propChange.add("display_unit");
        propChange.add(val);
        if (!MultiChangeConfirmDlg.confirmChange(propChange, source.length," and " + selectedRows.length + " attribute(s)")) {
          return;
        }
      }

      if(nb>1) ProgressFrame.displayProgress("Updating display unit");
      for(int j=0;j<source.length;j++) {
        for(int i=0;i<selectedRows.length;i++) {
          k++;
          ProgressFrame.setProgress("Updating " + source[j].getName() + "/" + unitModel.getValueAt(selectedRows[i],0),
                                      (k*100)/nb );
          source[j].setDisplayUnit(selectedRows[i],val);
        }
      }
      ProgressFrame.hideProgress();
      refreshValue();

    } else if (src==standardUnitMenuItem) {

      String val = JOptionPane.showInputDialog(null,"Enter standard unit","");
      if(val==null) return;
      int nb = selectedRows.length * source.length;
      int k = 0;

      // Confirmation dialog
      if (source.length > 1) {
        Vector propChange = new Vector();
        propChange.add("standard_unit");
        propChange.add(val);
        if (!MultiChangeConfirmDlg.confirmChange(propChange, source.length," and " + selectedRows.length + " attribute(s)")) {
          return;
        }
      }

      if(nb>1) ProgressFrame.displayProgress("Updating standard unit");
      for(int j=0;j<source.length;j++) {
        for(int i=0;i<selectedRows.length;i++) {
          k++;
          ProgressFrame.setProgress("Updating " + source[j].getName() + "/" + unitModel.getValueAt(selectedRows[i],0),
                                      (k*100)/nb );
          source[j].setStandardUnit(selectedRows[i],val);
        }
      }
      ProgressFrame.hideProgress();
      refreshValue();

    } else if (src==rangeMinMenuItem) {

      String val = JOptionPane.showInputDialog(null,"Enter min","");
      if(val==null) return;
      int nb = selectedRows.length * source.length;
      int k = 0;

      // Confirmation dialog
      if (source.length > 1) {
        Vector propChange = new Vector();
        propChange.add("min");
        propChange.add(val);
        if (!MultiChangeConfirmDlg.confirmChange(propChange, source.length," and " + selectedRows.length + " attribute(s)")) {
          return;
        }
      }

      if(nb>1) ProgressFrame.displayProgress("Updating min");
      for(int j=0;j<source.length;j++) {
        for(int i=0;i<selectedRows.length;i++) {
          k++;
          ProgressFrame.setProgress("Updating " + source[j].getName() + "/" + rangeModel.getValueAt(selectedRows[i],0),
                                      (k*100)/nb );
          source[j].setMin(selectedRows[i],val);
        }
      }
      ProgressFrame.hideProgress();
      refreshValue();

    } else if (src==rangeMaxMenuItem) {

      String val = JOptionPane.showInputDialog(null,"Enter max","");
      if(val==null) return;
      int nb = selectedRows.length * source.length;
      int k = 0;

      // Confirmation dialog
      if (source.length > 1) {
        Vector propChange = new Vector();
        propChange.add("max");
        propChange.add(val);
        if (!MultiChangeConfirmDlg.confirmChange(propChange, source.length," and " + selectedRows.length + " attribute(s)")) {
          return;
        }
      }

      if(nb>1) ProgressFrame.displayProgress("Updating max");
      for(int j=0;j<source.length;j++) {
        for(int i=0;i<selectedRows.length;i++) {
          k++;
          ProgressFrame.setProgress("Updating " + source[j].getName() + "/" + rangeModel.getValueAt(selectedRows[i],0),
                                      (k*100)/nb );
          source[j].setMax(selectedRows[i],val);
        }
      }
      ProgressFrame.hideProgress();
      refreshValue();

    } else if (src==alarmMinMenuItem) {

      String val = JOptionPane.showInputDialog(null,"Enter min alarm","");
      if(val==null) return;
      int nb = selectedRows.length * source.length;
      int k = 0;

      // Confirmation dialog
      if (source.length > 1) {
        Vector propChange = new Vector();
        propChange.add("min_alarm");
        propChange.add(val);
        if (!MultiChangeConfirmDlg.confirmChange(propChange, source.length," and " + selectedRows.length + " attribute(s)")) {
          return;
        }
      }

      if(nb>1) ProgressFrame.displayProgress("Updating min alarm");
      for(int j=0;j<source.length;j++) {
        for(int i=0;i<selectedRows.length;i++) {
          k++;
          ProgressFrame.setProgress("Updating " + source[j].getName() + "/" + alarmModel.getValueAt(selectedRows[i],0),
                                      (k*100)/nb );
          source[j].setMinAlarm(selectedRows[i],val);
        }
      }
      ProgressFrame.hideProgress();
      refreshValue();

    } else if (src==alarmMaxMenuItem) {

      String val = JOptionPane.showInputDialog(null,"Enter max alarm","");
      if(val==null) return;
      int nb = selectedRows.length * source.length;
      int k = 0;

      // Confirmation dialog
      if (source.length > 1) {
        Vector propChange = new Vector();
        propChange.add("max_alarm");
        propChange.add(val);
        if (!MultiChangeConfirmDlg.confirmChange(propChange, source.length," and " + selectedRows.length + " attribute(s)")) {
          return;
        }
      }

      if(nb>1) ProgressFrame.displayProgress("Updating max alarm");
      for(int j=0;j<source.length;j++) {
        for(int i=0;i<selectedRows.length;i++) {
          k++;
          ProgressFrame.setProgress("Updating " + source[j].getName() + "/" + alarmModel.getValueAt(selectedRows[i],0),
                                      (k*100)/nb );
          source[j].setMaxAlarm(selectedRows[i],val);
        }
      }
      ProgressFrame.hideProgress();
      refreshValue();

    } else if (src==warningMinMenuItem) {

      String val = JOptionPane.showInputDialog(null,"Enter min warning","");
      if(val==null) return;
      int nb = selectedRows.length * source.length;
      int k = 0;

      // Confirmation dialog
      if (source.length > 1) {
        Vector propChange = new Vector();
        propChange.add("min_warning");
        propChange.add(val);
        if (!MultiChangeConfirmDlg.confirmChange(propChange, source.length," and " + selectedRows.length + " attribute(s)")) {
          return;
        }
      }

      if(nb>1) ProgressFrame.displayProgress("Updating min warning");
      for(int j=0;j<source.length;j++) {
        for(int i=0;i<selectedRows.length;i++) {
          k++;
          ProgressFrame.setProgress("Updating " + source[j].getName() + "/" + alarmModel.getValueAt(selectedRows[i],0),
                                      (k*100)/nb );
          source[j].setMinWarning(selectedRows[i],val);
        }
      }
      ProgressFrame.hideProgress();
      refreshValue();

    } else if (src==warningMaxMenuItem) {

      String val = JOptionPane.showInputDialog(null,"Enter max warning","");
      if(val==null) return;
      int nb = selectedRows.length * source.length;
      int k = 0;

      // Confirmation dialog
      if (source.length > 1) {
        Vector propChange = new Vector();
        propChange.add("max_warning");
        propChange.add(val);
        if (!MultiChangeConfirmDlg.confirmChange(propChange, source.length," and " + selectedRows.length + " attribute(s)")) {
          return;
        }
      }

      if(nb>1) ProgressFrame.displayProgress("Updating max warning");
      for(int j=0;j<source.length;j++) {
        for(int i=0;i<selectedRows.length;i++) {
          k++;
          ProgressFrame.setProgress("Updating " + source[j].getName() + "/" + alarmModel.getValueAt(selectedRows[i],0),
                                      (k*100)/nb );
          source[j].setMaxWarning(selectedRows[i],val);
        }
      }
      ProgressFrame.hideProgress();
      refreshValue();

    } else if (src==deltaTMenuItem) {

      String val = JOptionPane.showInputDialog(null,"Enter delta T","");
      if(val==null) return;
      int nb = selectedRows.length * source.length;
      int k = 0;

      // Confirmation dialog
      if (source.length > 1) {
        Vector propChange = new Vector();
        propChange.add("delta_t");
        propChange.add(val);
        if (!MultiChangeConfirmDlg.confirmChange(propChange, source.length," and " + selectedRows.length + " attribute(s)")) {
          return;
        }
      }

      if(nb>1) ProgressFrame.displayProgress("Updating delta T");
      for(int j=0;j<source.length;j++) {
        for(int i=0;i<selectedRows.length;i++) {
          k++;
          ProgressFrame.setProgress("Updating " + source[j].getName() + "/" + alarmModel.getValueAt(selectedRows[i],0),
                                      (k*100)/nb );
          source[j].setDeltaT(selectedRows[i],val);
        }
      }
      ProgressFrame.hideProgress();
      refreshValue();

    } else if (src==deltaValMenuItem) {

      String val = JOptionPane.showInputDialog(null,"Enter delta value","");
      if(val==null) return;
      int nb = selectedRows.length * source.length;
      int k = 0;

      // Confirmation dialog
      if (source.length > 1) {
        Vector propChange = new Vector();
        propChange.add("delta_val");
        propChange.add(val);
        if (!MultiChangeConfirmDlg.confirmChange(propChange, source.length," and " + selectedRows.length + " attribute(s)")) {
          return;
        }
      }

      if(nb>1) ProgressFrame.displayProgress("Updating delta value");
      for(int j=0;j<source.length;j++) {
        for(int i=0;i<selectedRows.length;i++) {
          k++;
          ProgressFrame.setProgress("Updating " + source[j].getName() + "/" + alarmModel.getValueAt(selectedRows[i],0),
                                      (k*100)/nb );
          source[j].setDeltaVal(selectedRows[i],val);
        }
      }
      ProgressFrame.hideProgress();
      refreshValue();

    } else if (src==descriptionMenuItem) {

      String val = JOptionPane.showInputDialog(null,"Enter description","");
      if(val==null) return;
      int nb = selectedRows.length * source.length;
      int k = 0;

      // Confirmation dialog
      if (source.length > 1) {
        Vector propChange = new Vector();
        propChange.add("description");
        propChange.add(val);
        if (!MultiChangeConfirmDlg.confirmChange(propChange, source.length," and " + selectedRows.length + " attribute(s)")) {
          return;
        }
      }

      if(nb>1) ProgressFrame.displayProgress("Updating description");
      for(int j=0;j<source.length;j++) {
        for(int i=0;i<selectedRows.length;i++) {
          k++;
          ProgressFrame.setProgress("Updating " + source[j].getName() + "/" + descriptionModel.getValueAt(selectedRows[i],0),
                                      (k*100)/nb );
          source[j].setDescription(selectedRows[i],val);
        }
      }
      ProgressFrame.hideProgress();
      refreshValue();

    } else if (src==applyButton) {

      if(alarmTable.isEditing()) {
        String t = ((JTextField)alarmTable.getEditorComponent()).getText();
        alarmModel.setValueAt(t,alarmTable.getEditingRow(),alarmTable.getEditingColumn());
      }
      if(unitTable.isEditing()) {
        String t = ((JTextField)unitTable.getEditorComponent()).getText();
        unitModel.setValueAt(t,unitTable.getEditingRow(),unitTable.getEditingColumn());
      }
      if(rangeTable.isEditing()) {
        String t = ((JTextField)rangeTable.getEditorComponent()).getText();
        rangeModel.setValueAt(t,rangeTable.getEditingRow(),rangeTable.getEditingColumn());
      }
      if(displayTable.isEditing()) {
        String t = ((JTextField)displayTable.getEditorComponent()).getText();
        displayModel.setValueAt(t,displayTable.getEditingRow(),displayTable.getEditingColumn());
      }
      if(descriptionTable.isEditing()) {
        String t = ((JTextField)descriptionTable.getEditorComponent()).getText();
        descriptionModel.setValueAt(t,descriptionTable.getEditingRow(),descriptionTable.getEditingColumn());
      }
      if(aliasTable.isEditing()) {
        String t = ((JTextField)aliasTable.getEditorComponent()).getText();
        aliasModel.setValueAt(t,aliasTable.getEditingRow(),aliasTable.getEditingColumn());
      }

    }

  }

  void setSource(TaskAttributeNode[] src) {
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
      //  source[i].browseAttributeInfo();
      source[0].browseAttributeInfo();

      // Alarms
      String alarmColName[] = {"Attribute name" , "Min alarm" , "Max alarm" , "Min Warning" , "Max Warning" , "Delta t" , "Delta val"};
      Object[][] alarmInfo = new Object[source[0].getAttributeNumber()][7];
      for(int i=0;i<source[0].getAttributeNumber();i++) {
        alarmInfo[i][0] = source[0].getAttName(i);
        alarmInfo[i][1] = source[0].getMinAlarm(i);
        alarmInfo[i][2] = source[0].getMaxAlarm(i);
        alarmInfo[i][3] = source[0].getMinWarning(i);
        alarmInfo[i][4] = source[0].getMaxWarning(i);
        alarmInfo[i][5] = source[0].getDeltaT(i);
        alarmInfo[i][6] = source[0].getDeltaVal(i);
      }
      alarmModel.setDataVector(alarmInfo, alarmColName);
      alarmTable.getColumnModel().getColumn(0).setPreferredWidth(200);

      // Unit
      String unitColName[] = {"Attribute name" , "Unit" , "Display Unit" , "Standard Unit"};
      Object[][] unitInfo = new Object[source[0].getAttributeNumber()][4];
      for(int i=0;i<source[0].getAttributeNumber();i++) {
        unitInfo[i][0] = source[0].getAttName(i);
        unitInfo[i][1] = source[0].getUnit(i);
        unitInfo[i][2] = source[0].getDisplayUnit(i);
        unitInfo[i][3] = source[0].getStandardUnit(i);
      }
      unitModel.setDataVector(unitInfo, unitColName);
      unitTable.getColumnModel().getColumn(0).setPreferredWidth(200);

      // Range
      String rangeColName[] = {"Attribute name" , "Min value" , "Max value"};
      Object[][] rangeInfo = new Object[source[0].getAttributeNumber()][3];
      for(int i=0;i<source[0].getAttributeNumber();i++) {
        rangeInfo[i][0] = source[0].getAttName(i);
        rangeInfo[i][1] = source[0].getMin(i);
        rangeInfo[i][2] = source[0].getMax(i);
      }
      rangeModel.setDataVector(rangeInfo, rangeColName);
      rangeTable.getColumnModel().getColumn(0).setPreferredWidth(200);

      // Display
      String displayColName[] = {"Attribute name" , "Label" , "Format"};
      Object[][] displayInfo = new Object[source[0].getAttributeNumber()][3];
      for(int i=0;i<source[0].getAttributeNumber();i++) {
        displayInfo[i][0] = source[0].getAttName(i);
        displayInfo[i][1] = source[0].getLabel(i);
        displayInfo[i][2] = source[0].getFormat(i);
      }
      displayModel.setDataVector(displayInfo, displayColName);
      displayTable.getColumnModel().getColumn(0).setPreferredWidth(200);

      // Description
      String descrColName[] = {"Attribute name" , "Description"};
      Object[][] descrInfo = new Object[source[0].getAttributeNumber()][2];
      for(int i=0;i<source[0].getAttributeNumber();i++) {
        descrInfo[i][0] = source[0].getAttName(i);
        descrInfo[i][1] = source[0].getDescription(i);
      }
      descriptionModel.setDataVector(descrInfo, descrColName);
      descriptionTable.getColumnModel().getColumn(1).setPreferredWidth(200);

      // Alias
      String aliasColName[] = {"Attribute name" , "Alias"};
      Object[][] aliasInfo = new Object[source[0].getAttributeNumber()][2];
      for(int i=0;i<source[0].getAttributeNumber();i++) {
        descrInfo[i][0] = source[0].getAttName(i);
        descrInfo[i][1] = source[0].getAlias(i);
      }
      aliasModel.setDataVector(descrInfo, aliasColName);
      aliasTable.getColumnModel().getColumn(1).setPreferredWidth(200);

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
