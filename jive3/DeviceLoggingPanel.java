package jive3;

import jive.JiveUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class DeviceLoggingPanel extends JPanel implements ActionListener, MouseListener {

  private JScrollPane textView;
  private JTable      theTable;
  private JButton     refreshButton;
  private JButton     applyButton;

  private JPopupMenu  tableMenu;
  private JMenuItem   resetMenuItem;

  private TaskLoggingNode[]  source = null;
  private DefaultTableModel   dm;
  private int selectedRow;

  DeviceLoggingPanel()  {

    setLayout(new BorderLayout());

    // Table model
    dm = new DefaultTableModel() {

      public Class getColumnClass(int columnIndex) {
        return String.class;
      }

      public boolean isCellEditable(int row, int column) {
          return (column==1) && (!JiveUtils.readOnly);
      }

      public void setValueAt(Object aValue, int row, int column) {
        if(!aValue.equals(getValueAt(row,column))) {
          super.setValueAt(aValue,row,column);
          int nb = source.length;
          int k = 0;
          switch(row) {
            case 0:
              if(nb>1) ProgressFrame.displayProgress("Updating logging config");
              for(int i=0;i<source.length;i++) {
                k++;
                ProgressFrame.setProgress("Updating " + source[i].getName() + "/logging_level",
                                          (k*100)/nb );
                source[i].setLoggingLevel((String)aValue);
              }
              ProgressFrame.hideProgress();
              refreshValue();
              break;
            case 1:
              if(nb>1) ProgressFrame.displayProgress("Updating logging config");
              for(int i=0;i<source.length;i++) {
                k++;
                ProgressFrame.setProgress("Updating " + source[i].getName() + "/cur_logging_level",
                                          (k*100)/nb );
                source[i].setCurrentLoggingLevel((String)aValue);
              }
              ProgressFrame.hideProgress();
              refreshValue();
              break;
            case 2:
              if(nb>1) ProgressFrame.displayProgress("Updating logging config");
              for(int i=0;i<source.length;i++) {
                k++;
                ProgressFrame.setProgress("Updating " + source[i].getName() + "/logging_target",
                                          (k*100)/nb );
                source[i].setLoggingTarget((String)aValue);
              }
              ProgressFrame.hideProgress();
              refreshValue();
              break;
            case 3:
              if(nb>1) ProgressFrame.displayProgress("Updating logging config");
              for(int i=0;i<source.length;i++) {
                k++;
                ProgressFrame.setProgress("Updating " + source[i].getName() + "/cur_logging_target",
                                          (k*100)/nb );
                source[i].setCurrentLoggingTarget((String)aValue);
              }
              ProgressFrame.hideProgress();
              refreshValue();
              break;
            case 4:
              if(nb>1) ProgressFrame.displayProgress("Updating logging config");
              for(int i=0;i<source.length;i++) {
                k++;
                ProgressFrame.setProgress("Updating " + source[i].getName() + "/logging_rft",
                                          (k*100)/nb );
                source[i].setLoggingRft((String)aValue);
              }
              ProgressFrame.hideProgress();
              refreshValue();
              break;
          }
        }
      }

    };

    // Table initialisation
    theTable = new JTable(dm);
    theTable.addMouseListener(this);

    textView = new JScrollPane(theTable);
    add(textView, BorderLayout.CENTER);
    Border b = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"...");
    textView.setBorder(b);
    add(textView,BorderLayout.CENTER);

    // Bottom panel
    JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    refreshButton = new JButton("Refresh");
    refreshButton.addActionListener(this);
    applyButton = new JButton("Apply");
    applyButton.setEnabled(!JiveUtils.readOnly);
    applyButton.addActionListener(this);
    btnPanel.add(refreshButton);
    btnPanel.add(applyButton);
    add(btnPanel,BorderLayout.SOUTH);

    // Contextual menu
    tableMenu = new JPopupMenu();
    resetMenuItem = new JMenuItem("Reset to default value");
    resetMenuItem.addActionListener(this);
    tableMenu.add(resetMenuItem);


  }

  // Mouse listener -------------------------------------------

  public void mousePressed(MouseEvent e) {}
  public void mouseClicked(MouseEvent e) {

    if(e.getButton() == MouseEvent.BUTTON3 && e.getClickCount()==1 && !JiveUtils.readOnly) {

      int row = getRowForLocation(e.getY());
      if(row==0 || row==2 || row==4) {
        theTable.clearSelection();
        theTable.addRowSelectionInterval(row,row);
        theTable.setColumnSelectionInterval(0,theTable.getColumnCount()-1);
        selectedRow = row;
        tableMenu.show(theTable, e.getX(), e.getY());
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

    } else if (src==applyButton) {

      if(theTable.isEditing()) {
        String t = ((JTextField)theTable.getEditorComponent()).getText();
        dm.setValueAt(t,theTable.getEditingRow(),theTable.getEditingColumn());
      }

    } else if (src==resetMenuItem) {

      int nb = source.length;
      int k = 0;

      switch(selectedRow) {
        case 0:
          if(nb>1) ProgressFrame.displayProgress("Reseting logging config");
          for(int i=0;i<source.length;i++) {
            k++;
            ProgressFrame.setProgress("Reseting " + source[i].getName() + "/logging_level",
                                      (k*100)/nb );
            source[i].resetLoggingLevel();
          }
          ProgressFrame.hideProgress();
          refreshValue();
          break;
        case 2:
          if(nb>1) ProgressFrame.displayProgress("Reseting logging config");
          for(int i=0;i<source.length;i++) {
            k++;
            ProgressFrame.setProgress("Reseting " + source[i].getName() + "/logging_target",
                                      (k*100)/nb );
            source[i].resetLoggingTarget();
          }
          ProgressFrame.hideProgress();
          refreshValue();
          break;
        case 4:
          if(nb>1) ProgressFrame.displayProgress("Reseting logging config");
          for(int i=0;i<source.length;i++) {
            k++;
            ProgressFrame.setProgress("Reseting " + source[i].getName() + "/logging_rft",
                                      (k*100)/nb );
            source[i].resetLoggingRft();
          }
          ProgressFrame.hideProgress();
          refreshValue();
          break;
      }

    }

  }

  private int getRowForLocation(int y) {

    boolean found = false;
    int i = 0;
    int h = 0;

    while(i<theTable.getRowCount() && !found) {
      found = (y>=h && y<=h+theTable.getRowHeight(i));
      if(!found) {
        h+=theTable.getRowHeight(i);
        i++;
      }
    }

    if(found) {
      return i;
    } else {
      return -1;
    }

  }


  void setSource(TaskLoggingNode[] src) {
    this.source = src;
    // Check that all source belong to the same class

    refreshValue();
  }

  private void refreshValue() {

    if (source != null) {

      // Change event
      String colName[] = {"Property name", "Value"};
      Object[][] loggingInfo = new Object[5][2];
      loggingInfo[0][0] = "Logging level";
      loggingInfo[0][1] = source[0].getLoggingLevel();
      loggingInfo[1][0] = "Current logging level";
      loggingInfo[1][1] = source[0].getCurrentLoggingLevel();
      loggingInfo[2][0] = "Logging target";
      loggingInfo[2][1] = source[0].getLoggingTarget();
      loggingInfo[3][0] = "Current logging target";
      loggingInfo[3][1] = source[0].getCurrentLoggingTarget();
      loggingInfo[4][0] = "Logging RFT";
      loggingInfo[4][1] = source[0].getLoggingRft();
      dm.setDataVector(loggingInfo, colName);
      theTable.getColumnModel().getColumn(0).setPreferredWidth(200);

      String title = source[0].getTitle();
      if(source.length==1) {
        title += " [" + source[0].getName() + "]";
      } else {
        title += " [" + source.length + " devices selected]";
      }
      Border b = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),title);
      textView.setBorder(b);

    }

  }


}
