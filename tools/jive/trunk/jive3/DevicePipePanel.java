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

public class DevicePipePanel extends JPanel implements MouseListener,ActionListener {

  private JTabbedPane tabPane;
  private JTable      displayTable;
  private DefaultTableModel displayModel;
  private JTable      descriptionTable;
  private DefaultTableModel descriptionModel;
  private JButton     refreshButton;
  private JButton     applyButton;

  private JPopupMenu  tableMenu;
  private JMenuItem   resetMenuItem;
  private JMenuItem   resetLMenuItem;
  private JMenuItem   resetULMenuItem;
  private JMenuItem   resetCULMenuItem;
  private JMenuItem   labelMenuItem;
  private JMenuItem   descriptionMenuItem;

  private JTable      selectedTable;
  private int[]       selectedRows;

  private TaskPipeNode[]   source = null;

  DevicePipePanel()  {

    setLayout(new BorderLayout());

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

    tabPane = new JTabbedPane();
    tabPane.setFont(ATKConstant.labelFont);
    tabPane.add("Label",displayView);
    tabPane.add("Description",descriptionView);

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

    labelMenuItem = new JMenuItem("Set label");
    labelMenuItem.addActionListener(this);
    tableMenu.add(labelMenuItem);
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
        selectedTable.addRowSelectionInterval(row, row);
        selectedTable.setColumnSelectionInterval(0, selectedTable.getColumnCount() - 1);
        selectedRows = selectedTable.getSelectedRows();

        labelMenuItem.setVisible(false);
        descriptionMenuItem.setVisible(false);

        if(selectedTable == displayTable) {

          labelMenuItem.setVisible(true);

        } else if (selectedTable == descriptionTable) {

          descriptionMenuItem.setVisible(true);

        }

        resetLMenuItem.setVisible(true);
        resetULMenuItem.setVisible(true);
        resetCULMenuItem.setVisible(true);

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

      if(selectedTable == displayTable) {

        // Confirmation message
        if( source.length>1 ) {
          String message = "You are going to reset label configuration for " + source.length + " device(s) and " + selectedRows.length + " attribute(s).\nDo you want to proceed ?";
          int result = JOptionPane.showConfirmDialog(this,message,"Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
          if( result==JOptionPane.NO_OPTION )
            return;
        }

        if(nb>1) ProgressFrame.displayProgress("Reseting label");
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

      }

    } else if (src==resetLMenuItem) {

      // Reset to library default menu item -----------------------------------------------------
      int nb = selectedRows.length * source.length;
      int k = 0;

      if(selectedTable  == displayTable) {

        // Confirmation message
        if( source.length>1 ) {
          String message = "You are going to reset label configuration to library value for " + source.length + " device(s) and " + selectedRows.length + " attribute(s).\nDo you want to proceed ?";
          int result = JOptionPane.showConfirmDialog(this,message,"Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
          if( result==JOptionPane.NO_OPTION )
            return;
        }

        if(nb>1) ProgressFrame.displayProgress("Reseting label");
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

      if(selectedTable == displayTable) {

        // Confirmation message
        if( source.length>1 ) {
          String message = "You are going to reset label configuration to code/library value for " + source.length + " device(s) and " + selectedRows.length + " attribute(s).\nDo you want to proceed ?";
          int result = JOptionPane.showConfirmDialog(this,message,"Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
          if( result==JOptionPane.NO_OPTION )
            return;
        }

        if(nb>1) ProgressFrame.displayProgress("Reseting label");
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

      if(selectedTable == displayTable) {

        // Confirmation message
        if( source.length>1 ) {
          String message = "You are going to reset label configuration to class/code/library value for " + source.length + " device(s) and " + selectedRows.length + " attribute(s).\nDo you want to proceed ?";
          int result = JOptionPane.showConfirmDialog(this,message,"Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
          if( result==JOptionPane.NO_OPTION )
            return;
        }

        if(nb>1) ProgressFrame.displayProgress("Reseting label");
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
          source[j].setDescription(selectedRows[i], val);
        }
      }
      ProgressFrame.hideProgress();
      refreshValue();

    } else if (src==applyButton) {

      if(displayTable.isEditing()) {
        String t = ((JTextField)displayTable.getEditorComponent()).getText();
        displayModel.setValueAt(t,displayTable.getEditingRow(),displayTable.getEditingColumn());
      }
      if(descriptionTable.isEditing()) {
        String t = ((JTextField)descriptionTable.getEditorComponent()).getText();
        descriptionModel.setValueAt(t, descriptionTable.getEditingRow(), descriptionTable.getEditingColumn());
      }

    }

  }

  void setSource(TaskPipeNode[] src) {
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
      source[0].browsePipeInfo();

      // Display
      String displayColName[] = {"Attribute name" , "Label" };
      Object[][] displayInfo = new Object[source[0].getAttributeNumber()][2];
      for(int i=0;i<source[0].getAttributeNumber();i++) {
        displayInfo[i][0] = source[0].getAttName(i);
        displayInfo[i][1] = source[0].getLabel(i);
      }
      displayModel.setDataVector(displayInfo, displayColName);
      displayTable.getColumnModel().getColumn(1).setPreferredWidth(200);

      // Description
      String descrColName[] = {"Attribute name" , "Description"};
      Object[][] descrInfo = new Object[source[0].getAttributeNumber()][2];
      for(int i=0;i<source[0].getAttributeNumber();i++) {
        descrInfo[i][0] = source[0].getAttName(i);
        descrInfo[i][1] = source[0].getDescription(i);
      }
      descriptionModel.setDataVector(descrInfo, descrColName);
      descriptionTable.getColumnModel().getColumn(1).setPreferredWidth(200);

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
