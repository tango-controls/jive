package jive3;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.Database;
import fr.esrf.TangoApi.DbDatum;
import fr.esrf.tangoatk.widget.util.ATKConstant;
import jive.JiveUtils;
import jive.MultiLineCellEditor;
import jive.MultiLineCellRenderer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

/**
 * Multiple selection dialog
 */

class PItem {

  String  devName;
  String  pName;
  boolean updated;
  String[]  value;

  public String toString() {
    return JiveUtils.stringArrayToString(value);
  }

};

public class SelectionDlg extends JFrame implements ActionListener {

  Database db;
  String colName[] = {"Name" , "Value"};
  private MultiLineCellEditor editor;
  JTextField selectionText;
  JLabel selectionLabel;
  JButton searchButton;
  JButton applyAllButton;
  JButton applyButton;
  JButton dismissButton;
  JLabel infoLabel;
  DefaultTableModel dm;
  JTable theTable;
  String selectText;
  Vector<PItem> items;

  public SelectionDlg() {
    this.db = null;
    initComponents();
  }

  public void setDatabase(Database db) {
    this.db = db;
  }

  public void clear() {
    items.clear();
    infoLabel.setText(items.size() + " item(s)");
    String[][] prop = new String[0][2];
    dm.setDataVector(prop, colName);
    editor.updateRows();
    theTable.getColumnModel().getColumn(1).setPreferredWidth(250);
    theTable.validate();
    ((JPanel)getContentPane()).revalidate();
  }

  void initComponents() {

    getContentPane().setLayout(new BorderLayout());
    JPanel innerPanel = new JPanel();
    innerPanel.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 0;
    gbc.fill = GridBagConstraints.VERTICAL;
    JLabel selectionLabel = new JLabel(" Selection ");
    selectionLabel.setFont(ATKConstant.labelFont);
    innerPanel.add(selectionLabel,gbc);

    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.weightx = 1;
    gbc.fill = GridBagConstraints.BOTH;
    selectionText = new JTextField();
    selectionText.addActionListener(this);
    selectionText.setFont(ATKConstant.labelFont);
    innerPanel.add(selectionText,gbc);

    gbc.gridx = 2;
    gbc.gridy = 0;
    gbc.weightx = 0;
    gbc.fill = GridBagConstraints.VERTICAL;
    searchButton = new JButton("Search");
    searchButton.addActionListener(this);
    innerPanel.add(searchButton,gbc);

    JPanel innerPanel2 = new JPanel();
    innerPanel2.setLayout(new FlowLayout(FlowLayout.RIGHT));
    infoLabel = new JLabel("0 item(s)");
    infoLabel.setFont(ATKConstant.labelFont);
    innerPanel2.add(infoLabel);

    applyAllButton = new JButton("Apply to all ...");
    applyAllButton.setToolTipText("Ask for a value and apply it to the current selection");
    applyAllButton.addActionListener(this);
    innerPanel2.add(applyAllButton);

    applyButton = new JButton("Apply");
    applyButton.setToolTipText("Apply change to the database");
    applyButton.addActionListener(this);
    applyButton.setEnabled(false);
    innerPanel2.add(applyButton);

    dismissButton = new JButton("Dismiss");
    dismissButton.addActionListener(this);
    innerPanel2.add(dismissButton);

    getContentPane().add(innerPanel, BorderLayout.NORTH);
    getContentPane().add(innerPanel2, BorderLayout.SOUTH);

    // Table model
    dm = new DefaultTableModel() {

      public Class getColumnClass(int columnIndex) {
        return String.class;
      }

      public boolean isCellEditable(int row, int column) {
        return column==1;
      }

      public void setValueAt(Object aValue, int row, int column) {
        if(!aValue.equals(getValueAt(row,column))) {
          super.setValueAt(aValue, row, column);
          items.get(row).value = JiveUtils.makeStringArray((String)aValue);
          items.get(row).updated = true;
          applyButton.setEnabled(true);
        }
      }

    };

    theTable = new JTable(dm);
    editor = new MultiLineCellEditor(theTable);
    theTable.setDefaultEditor(String.class, editor);
    MultiLineCellRenderer renderer = new MultiLineCellRenderer(false,false,false);
    theTable.setDefaultRenderer(String.class, renderer);
    theTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane textView = new JScrollPane(theTable);
    getContentPane().add(textView, BorderLayout.CENTER);
    setTitle("Multiple selection");
    setPreferredSize(new Dimension(800,600));

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        exitDlg();
        super.windowClosing(e);
      }
    });

    items = new Vector<PItem>();
    clear();

  }

  private void selectProperty() {
    refresh();
  }

  private void refresh() {

    String selectText = selectionText.getText();
    int slashCount = 0;
    for(int i=0;i<selectText.length();i++)
      if(selectText.charAt(i)=='/') slashCount++;

    if (slashCount != 3) {
      JiveUtils.showJiveError("Invalid selection pattern, 4 slash separated fields expected");
      return;
    }

    try {

      int idx = selectText.lastIndexOf('/');
      String devName = selectText.substring(0, idx);
      String propName = selectText.substring(idx + 1);

      items.clear();
      String[] devNames = db.get_device_list(devName);
      for( int i=0;i<devNames.length;i++ ) {
        String[] pList = db.get_device_property_list(devNames[i], propName);
        for(int j=0;j<pList.length;j++) {
          PItem pi = new PItem();
          pi.devName = devNames[i];
          pi.pName = pList[j];
          pi.updated = false;
          DbDatum dbd = db.get_device_property(pi.devName,pi.pName);
          pi.value = dbd.extractStringArray();
          items.add(pi);
        }
      }

      if(items.size()==0)
        JOptionPane.showMessageDialog(this,"No property found");

      infoLabel.setText(items.size()+ " item(s)");

      refreshTable();

    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  private void refreshTable() {

    String[][] prop = new String[items.size()][2];
    for (int i = 0; i < items.size(); i++) {
      prop[i][0] = items.get(i).devName + "/" + items.get(i).pName;
      prop[i][1] = items.get(i).toString();
    }

    dm.setDataVector(prop, colName);
    editor.updateRows();
    theTable.getColumnModel().getColumn(1).setPreferredWidth(250);
    theTable.validate();
    ((JPanel)getContentPane()).revalidate();

  }

  private void applyAll() {

    if(items.size()==0) {
      JOptionPane.showMessageDialog(this,"No selection");
      return;
    }

    String[] newValue = MultiLineInputDlg.getInputText(this,
                                                       "Apply to all",
                                                       "Apply to "+items.size()+" item(s)",
                                                       items.get(0).value);

    if( newValue!=null ) {

      for(int i=0;i<items.size();i++) {
        items.get(i).value = newValue;
        items.get(i).updated = true;
      }
      apply();
      refreshTable();

    }

  }

  private void apply() {

    try {
      for (int i = 0; i < items.size(); i++) {
        PItem pi = items.get(i);
        if (pi.updated) {
          DbDatum[] ds = new DbDatum[1];
          DbDatum d = new DbDatum(pi.pName);
          d.insert(pi.value);
          ds[0] = d;
          db.put_device_property(pi.devName, ds);
        }
        pi.updated = false;
      }
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

    applyButton.setEnabled(false);

  }

  public boolean hasChanged() {

    boolean hasChanged = false;
    int i=0;
    while(!hasChanged && i<items.size()) {
      hasChanged=items.get(i).updated;
      if(!hasChanged) i++;
    }
    return hasChanged;

  }

  private void exitDlg() {

    if( hasChanged() ) {
      int ok = JOptionPane.showConfirmDialog(this,"Some properties have changed\nDo you want to update the database ?",
        "Confirmation",JOptionPane.YES_NO_OPTION);
      if( ok==JOptionPane.YES_OPTION ) {
        apply();
      }
    }

  }

  public void actionPerformed(ActionEvent evt) {

    Object src = evt.getSource();

    if( src==searchButton || src==selectionText ) {
      selectProperty();
    } else if( src==dismissButton ) {
      exitDlg();
      setVisible(false);
    } else if( src==applyButton ) {
      apply();
    } else if ( src==applyAllButton ) {
      applyAll();
    }

  }


}
