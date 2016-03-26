package jive3;

import jive.JiveUtils;
import jive.MultiLineCellEditor;
import jive.MultiLineCellRenderer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SingleAttributePanel extends JPanel implements ActionListener {

  private final static int NB_ROW = 31;

  private TaskSingleAttributeNode[]   source = null;

  private JTable            attTable;
  private DefaultTableModel attModel;

  MultiLineCellEditor editor;

  private JButton refreshBtn;
  private JButton applyBtn;
  private boolean[] updatedProp;

  public SingleAttributePanel() {

    updatedProp = new boolean[NB_ROW];

    setLayout(new BorderLayout());

    // -- Attribute table -------------------------------
    attModel = new DefaultTableModel() {

      public Class getColumnClass(int columnIndex) {
        return String.class;
      }
      public boolean isCellEditable(int row, int column) {
        return (column!=0) && (!JiveUtils.readOnly)
                && (row!=0) && (row!=3) && (row!=6) && (row!=10) && (row!=12) && (row!=15)
                && (row!=19) && (row!=22) && (row!=29);
      }

      public void setValueAt(Object aValue, int row, int column) {
        if(!aValue.equals(getValueAt(row,column))) {
          super.setValueAt(aValue,row,column);
          applyBtn.setEnabled(true);
          updatedProp[row] = true;
        }
      }

    };

    attTable = new JTable(attModel);

    editor = new MultiLineCellEditor(attTable);
    attTable.setDefaultEditor(String.class, editor);

    MultiLineCellRenderer renderer = new MultiLineCellRenderer(true,true,false);
    attTable.setDefaultRenderer(String.class, renderer);

    JScrollPane attView = new JScrollPane(attTable);

    add(attView,BorderLayout.CENTER);

    // Button
    JPanel btnPanel = new JPanel();
    btnPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    add(btnPanel,BorderLayout.SOUTH);

    refreshBtn = new JButton("Refresh");
    refreshBtn.addActionListener(this);
    btnPanel.add(refreshBtn);

    applyBtn = new JButton("Apply");
    applyBtn.addActionListener(this);
    btnPanel.add(applyBtn);

    Border b = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"...");
    setBorder(b);

  }

  public void actionPerformed(ActionEvent e) {

    Object src = e.getSource();

    if( src==refreshBtn ) {
      refreshValue();
    } else if ( src==applyBtn ) {
      saveChange();
    }

  }

  public void setSource(TaskSingleAttributeNode[] src) {
    this.source = src;
    refreshValue();
  }

  public boolean hasChanged() {
    return applyBtn.isEnabled();
  }

  public void saveChange() {

    String newVal;

    // Update property
    int nb = getNbUpdated() * source.length;
    int k = 0;
    if (nb > 1) ProgressFrame.displayProgress("Updating properties");
    for (int i = 0; i < updatedProp.length; i++) {
      if (updatedProp[i]) {
        for (int j = 0; j < source.length; j++) {
          k++;
          ProgressFrame.setProgress("Applying " + source[j].getTitle(),
                  (k * 100) / nb);

          switch (i) {

            case 1:
            case 2:
              // Polling
              boolean polled = ((String) (attModel.getValueAt(1, 1))).equalsIgnoreCase("true");
              String period = (String) attModel.getValueAt(2, 1);
              source[j].setAttributePolling(polled, period);
              break;

            case 4:
              // Absolute change event
              newVal = (String) attModel.getValueAt(4, 1);
              source[j].setAbsoluteChange(newVal);
              break;

            case 5:
              // Relative change event
              newVal = (String) attModel.getValueAt(5, 1);
              source[j].setRelativeChange(newVal);
              break;

            case 7:
              // Archive Absolute change event
              newVal = (String) attModel.getValueAt(7, 1);
              source[j].setArchAbsoluteChange(newVal);
              break;

            case 8:
              // Archive Relative change event
              newVal = (String) attModel.getValueAt(8, 1);
              source[j].setArchRelativeChange(newVal);
              break;

            case 9:
              // Archive Period event
              newVal = (String) attModel.getValueAt(9, 1);
              source[j].setArchPeriod(newVal);
              break;

            case 11:
              // Periodic event
              newVal = (String) attModel.getValueAt(11, 1);
              source[j].setPeriod(newVal);
              break;

            case 13:
              // Label
              newVal = (String) attModel.getValueAt(13, 1);
              source[j].setLabel(newVal);
              break;

            case 14:
              // Format
              newVal = (String) attModel.getValueAt(14, 1);
              source[j].setFormat(newVal);
              break;

            case 16:
              // Unit
              newVal = (String) attModel.getValueAt(16, 1);
              source[j].setUnit(newVal);
              break;

            case 17:
              // Display Unit
              newVal = (String) attModel.getValueAt(17, 1);
              source[j].setDisplayUnit(newVal);
              break;

            case 18:
              // Standard Unit
              newVal = (String) attModel.getValueAt(18, 1);
              source[j].setStandardUnit(newVal);
              break;

            case 20:
              // Min
              newVal = (String) attModel.getValueAt(20, 1);
              source[j].setMin(newVal);
              break;

            case 21:
              // Max
              newVal = (String) attModel.getValueAt(21, 1);
              source[j].setMax(newVal);
              break;

            case 23:
              // Min alarm
              newVal = (String) attModel.getValueAt(23, 1);
              source[j].setMinAlarm(newVal);
              break;

            case 24:
              // Max alarm
              newVal = (String) attModel.getValueAt(24, 1);
              source[j].setMaxAlarm(newVal);
              break;

            case 25:
              // Min warning
              newVal = (String) attModel.getValueAt(25, 1);
              source[j].setMinWarning(newVal);
              break;

            case 26:
              // Max warning
              newVal = (String) attModel.getValueAt(26, 1);
              source[j].setMaxWarning(newVal);
              break;

            case 27:
              // Delta T
              newVal = (String) attModel.getValueAt(27, 1);
              source[j].setDeltaT(newVal);
              break;

            case 28:
              // Delta Val
              newVal = (String) attModel.getValueAt(28, 1);
              source[j].setDeltaVal(newVal);
              break;

            case 30:
              // Description
              newVal = (String) attModel.getValueAt(30, 1);
              source[j].setDescr(newVal);
              break;


          }
        }
      }
    }

    refreshValue();
    ProgressFrame.hideProgress();

  }

  private int getNbUpdated() {
    int k = 0;
    for(int i=0;i<updatedProp.length;i++)
      if(updatedProp[i]) k++;
    return k;
  }

  private void refreshValue() {

    if (source != null) {

      String title;
      if(source.length==1) {
        title = "Attribute configuration [" + source[0].getTitle() + "]";
      } else {
        title = "Attribute configuration [" + source.length + " attributes selected]";
      }
      Border b = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), title);
      setBorder(b);

      source[0].browseAttributeInfo();
      source[0].updatePollingInfo();

      for(int i=0;i<NB_ROW;i++) updatedProp[i] = false;
      applyBtn.setEnabled(false);

      // Attribute table
      String attColName[] = {"Name" , "Value" };

      Object[][] attInfo = new Object[NB_ROW][2];

      attInfo[0][0] = "/BPolling";
      attInfo[0][1] = "";
      attInfo[1][0] = "Is Polled";
      attInfo[1][1] = Boolean.toString(source[0].isPolled());
      attInfo[2][0] = "Polling Period";
      attInfo[2][1] = source[0].getPollingPeriod();

      attInfo[3][0] = "/BChange Event";
      attInfo[3][1] = "";
      attInfo[4][0] = "Absolute";
      attInfo[4][1] = source[0].getAbsoluteChange();
      attInfo[5][0] = "Relative";
      attInfo[5][1] = source[0].getRelativeChange();

      attInfo[6][0] = "/BArchive Event";
      attInfo[6][1] = "";
      attInfo[7][0] = "Absolute";
      attInfo[7][1] = source[0].getArchAbsoluteChange();
      attInfo[8][0] = "Relative";
      attInfo[8][1] = source[0].getArchRelativeChange();
      attInfo[9][0] = "Period";
      attInfo[9][1] = source[0].getArchPeriod();

      attInfo[10][0] = "/BPeriodic Event";
      attInfo[10][1] = "";
      attInfo[11][0] = "Period";
      attInfo[11][1] = source[0].getPeriodic();

      attInfo[12][0] = "/BDisplay";
      attInfo[12][1] = "";
      attInfo[13][0] = "Label";
      attInfo[13][1] = source[0].getLabel();
      attInfo[14][0] = "Format";
      attInfo[14][1] = source[0].getFormat();

      attInfo[15][0] = "/BUnit";
      attInfo[15][1] = "";
      attInfo[16][0] = "Unit";
      attInfo[16][1] = source[0].getUnit();
      attInfo[17][0] = "Display Unit";
      attInfo[17][1] = source[0].getDisplayUnit();
      attInfo[18][0] = "Standard Unit";
      attInfo[18][1] = source[0].getStandardUnit();

      attInfo[19][0] = "/BRange";
      attInfo[19][1] = "";
      attInfo[20][0] = "Min value";
      attInfo[20][1] = source[0].getMin();
      attInfo[21][0] = "Max value";
      attInfo[21][1] = source[0].getMax();

      attInfo[22][0] = "/BAlarms";
      attInfo[22][1] = "";
      attInfo[23][0] = "Min Alarm";
      attInfo[23][1] = source[0].getMinAlarm();
      attInfo[24][0] = "Max Alarm";
      attInfo[24][1] = source[0].getMaxAlarm();
      attInfo[25][0] = "Min Warning";
      attInfo[25][1] = source[0].getMinWarning();
      attInfo[26][0] = "Max Warning";
      attInfo[26][1] = source[0].getMaxWarning();
      attInfo[27][0] = "Delta T";
      attInfo[27][1] = source[0].getDeltaT();
      attInfo[28][0] = "Delta Val";
      attInfo[28][1] = source[0].getDeltaVal();

      attInfo[29][0] = "/BDescription";
      attInfo[29][1] = "";
      attInfo[30][0] = "Description";
      attInfo[30][1] = source[0].getDescr();

      attModel.setDataVector(attInfo, attColName);
      attTable.getColumnModel().getColumn(1).setPreferredWidth(250);
      editor.updateRows();

    } else {
      Border b = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "...");
      setBorder(b);
    }

  }

}
