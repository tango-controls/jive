package jive3;

import fr.esrf.tangoatk.widget.util.ATKConstant;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.TangoApi.*;
import fr.esrf.Tango.DevFailed;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import jive.JiveUtils;
import jive.MultiLineCellEditor;

/**
 * A class to query the static database property history.
 */
public class PropertyHistoryDlg extends JFrame {

  private DevicePropertyHistPanel devicePropertyHistPanel;
  private DeviceAttributePropertyHistPanel deviceAttributePropertyHistPanel;
  private ClassPropertyHistPanel  classPropertyHistPanel;
  private ClassAttributePropertyHistPanel classAttributePropertyHistPanel;
  private ObjectPropertyHistPanel objectPropertyHistPanel;
  private JTabbedPane tabPane;

  PropertyHistoryDlg() {
    tabPane = new JTabbedPane();
    tabPane.setFont(ATKConstant.labelFont);
    devicePropertyHistPanel = new DevicePropertyHistPanel();
    tabPane.add(devicePropertyHistPanel,"Device properties");
    deviceAttributePropertyHistPanel = new DeviceAttributePropertyHistPanel();
    tabPane.add(deviceAttributePropertyHistPanel,"Device att. properties");
    classPropertyHistPanel = new ClassPropertyHistPanel();
    tabPane.add(classPropertyHistPanel,"Class properties");
    classAttributePropertyHistPanel = new ClassAttributePropertyHistPanel();
    tabPane.add(classAttributePropertyHistPanel,"Class att. properties");
    objectPropertyHistPanel = new ObjectPropertyHistPanel();
    tabPane.add(objectPropertyHistPanel,"Free properties");
    getContentPane().add(tabPane);
    setTitle("Tango Database History");
  }
  
  public void viewDevicePropertyHistory(String devName,String propName) {
  
    devicePropertyHistPanel.text1.setText(devName);
    devicePropertyHistPanel.text2.setText(propName);
    devicePropertyHistPanel.performSearch();
  
    tabPane.setSelectedComponent(devicePropertyHistPanel);
    // Work around X11 bug
    tabPane.getSelectedComponent().setVisible(true);

  }
  
  public void viewDeviceAttPropertyHistory(String devName,String attName,String propName) {
  
    deviceAttributePropertyHistPanel.text1.setText(devName);
    deviceAttributePropertyHistPanel.text2.setText(attName);
    deviceAttributePropertyHistPanel.text3.setText(propName);
    deviceAttributePropertyHistPanel.performSearch();
  
    tabPane.setSelectedComponent(deviceAttributePropertyHistPanel);
    // Work around X11 bug
    tabPane.getSelectedComponent().setVisible(true);

  }
  
  public void viewClassAttPropertyHistory(String className,String attName,String propName) {
  
    classAttributePropertyHistPanel.text1.setText(className);
    classAttributePropertyHistPanel.text2.setText(attName);
    classAttributePropertyHistPanel.text3.setText(propName);
    classAttributePropertyHistPanel.performSearch();
  
    tabPane.setSelectedComponent(classAttributePropertyHistPanel);
    // Work around X11 bug
    tabPane.getSelectedComponent().setVisible(true);

  }
  
  public void viewFreePropertyHistory(String objName,String propName) {
  
    objectPropertyHistPanel.text1.setText(objName);
    objectPropertyHistPanel.text2.setText(propName);
    objectPropertyHistPanel.performSearch();
  
    tabPane.setSelectedComponent(objectPropertyHistPanel);
    // Work around X11 bug
    tabPane.getSelectedComponent().setVisible(true);

  }
  
  public void viewClassPropertyHistory(String className,String propName) {
  
    classPropertyHistPanel.text1.setText(className);
    classPropertyHistPanel.text2.setText(propName);
    classPropertyHistPanel.performSearch();
  
    tabPane.setSelectedComponent(classPropertyHistPanel);
    // Work around X11 bug
    tabPane.getSelectedComponent().setVisible(true);

  }

  public void setDatabase(Database db,String title) {

    // Set database
    devicePropertyHistPanel.setDatabase(db);
    deviceAttributePropertyHistPanel.setDatabase(db);
    classPropertyHistPanel.setDatabase(db);
    classAttributePropertyHistPanel.setDatabase(db);
    objectPropertyHistPanel.setDatabase(db);

    // Update title
    if(db==null)
      setTitle("Tango Database History" + " [No connection]");
    else
      setTitle("Tango Database History" + " [" + title + "]");

  }

  public static void main(String[] args) {
    final PropertyHistoryDlg dlg = new PropertyHistoryDlg();
    Database db;
    try {
      db = ApiUtil.get_db_obj();
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
      db = null;
    }
    dlg.setDatabase(db,"");
    dlg.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    ATKGraphicsUtils.centerFrameOnScreen(dlg);
    dlg.setVisible(true);
  }

  // -------- Common property panel ---------------------------------

  abstract class PropertyHistPanel extends JPanel implements ActionListener {

    Database db;
    String colName[] = {"Date" , "Property name" , "Value"};
    private MultiLineCellEditor editor;
    JTextField text1;
    JTextField text2;
    JLabel selectionLabel;
    private JButton searchButton;
    private JButton reApplyButton;
    DefaultTableModel dm;
    JTable theTable;

    void initComponents(String name1,String name2) {

      setLayout(new BorderLayout());
      JPanel innerPanel = new JPanel(null);
      innerPanel.setPreferredSize(new Dimension(640,60));
      JLabel deviceLabel = new JLabel(name1);
      deviceLabel.setFont(ATKConstant.labelFont);
      deviceLabel.setBounds(5,5,100,25);
      innerPanel.add(deviceLabel);
      text1 = new JTextField();
      text1.setMargin(JiveUtils.noMargin);
      text1.setFont(ATKConstant.labelFont);
      text1.setBounds(110,5,200,25);
      innerPanel.add(text1);

      JLabel propertyLabel = new JLabel(name2);
      propertyLabel.setFont(ATKConstant.labelFont);
      propertyLabel.setBounds(5,30,100,25);
      innerPanel.add(propertyLabel);
      text2 = new JTextField();
      text2.setMargin(JiveUtils.noMargin);
      text2.setFont(ATKConstant.labelFont);
      text2.setBounds(110,30,200,25);
      innerPanel.add(text2);

      searchButton = new JButton("Search");
      searchButton.setBounds(320,5,100,25);
      searchButton.addActionListener(this);
      innerPanel.add(searchButton);

      JPanel innerPanel2 = new JPanel(null);
      innerPanel2.setPreferredSize(new Dimension(640,35));
      reApplyButton = new JButton("Reapply selection");
      reApplyButton.setBounds(5,5,200,25);
      reApplyButton.addActionListener(this);
      reApplyButton.setEnabled(false);
      innerPanel2.add(reApplyButton);

      selectionLabel = new JLabel("Selection:");
      selectionLabel.setFont(ATKConstant.labelFont);
      selectionLabel.setHorizontalAlignment(JLabel.LEFT);
      selectionLabel.setBounds(210,5,400,25);
      innerPanel2.add(selectionLabel);

      add(innerPanel,BorderLayout.NORTH);
      add(innerPanel2,BorderLayout.SOUTH);

      // Table model
      dm = new DefaultTableModel() {

        public Class getColumnClass(int columnIndex) {
          return String.class;
        }

        public boolean isCellEditable(int row, int column) {
            return false;
        }

      };

      theTable = new JTable(dm);
      editor = new MultiLineCellEditor(theTable);
      theTable.setDefaultEditor(String.class, editor);
      MultiLineCellRenderer renderer = new MultiLineCellRenderer();
      theTable.setDefaultRenderer(String.class, renderer);
      theTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      theTable.addMouseListener(new MouseAdapter(){
         public void mousePressed(MouseEvent e) {
           refreshSelection();
         }
        public void mouseReleased(MouseEvent e) {
          refreshSelection();
        }
      });
      JScrollPane textView = new JScrollPane(theTable);
      add(textView,BorderLayout.CENTER);

    }

    private void refreshTable() {

      editor.updateRows();
      theTable.getColumnModel().getColumn(0).setMaxWidth(140);
      theTable.getColumnModel().getColumn(0).setMinWidth(140);
      theTable.getColumnModel().getColumn(0).setPreferredWidth(140);
      theTable.getColumnModel().getColumn(2).setPreferredWidth(300);
      theTable.validate();

    }

    private void refreshSelection() {

      int row = theTable.getSelectedRow();
      if(row<0) {
        selectionLabel.setText("Selection:");
        reApplyButton.setEnabled(false);
      } else {
        String value = (String)dm.getValueAt(row,2);
        if(value.equals("DELETED")) {
          selectionLabel.setText("Selection:");
          reApplyButton.setEnabled(false);
        } else {
          String pDate = (String)dm.getValueAt(row,0);
          String pName = (String)dm.getValueAt(row,1);
          selectionLabel.setText("Selection:" + pName + " at " + pDate);
          reApplyButton.setEnabled(!JiveUtils.readOnly);
        }
      }

    }

    void setDatabase(Database db) {
      this.db = db;
      dm.setDataVector(new String[0][3], colName);
      selectionLabel.setText("Selection:");
      reApplyButton.setEnabled(false);
      refreshTable();
    }

    void updateHistory(DbHistory[] hist) {

      selectionLabel.setText("Selection:");
      reApplyButton.setEnabled(false);
      if (hist.length == 0) {
        JiveUtils.showJiveError("No data found.");
        dm.setDataVector(new String[0][3], colName);
      } else {
        String[][] prop = new String[hist.length][3];
        for (int i = 0; i < hist.length; i++) {
          prop[i][0] = hist[i].getDate();
          prop[i][1] = hist[i].getName();
          if (hist[i].isDeleted()) {
            prop[i][2] = "DELETED";
          } else {
            prop[i][2] = hist[i].getValue();
          }
        }
        dm.setDataVector(prop,colName);
      }
      refreshTable();

    }

    abstract void performSearch();

    abstract void reApply();

    public void actionPerformed(ActionEvent e) {

      Object src = e.getSource();

      if(src==searchButton) {
        performSearch();
      } else if(src==reApplyButton) {
        reApply();
      }

    }

  }

  // -------- Device property panel ---------------------------------

  class DevicePropertyHistPanel extends PropertyHistPanel {

    private String devName = null;

    DevicePropertyHistPanel() {
      initComponents("Device","Property");
    }

    void performSearch() {

      if(db==null) return;
      devName = text1.getText();
      String pName = text2.getText();
      try {
        DbHistory[] hist = db.get_device_property_history(devName, pName);
        updateHistory(hist);
      } catch (DevFailed ex) {
        JiveUtils.showTangoError(ex);
      }

    }

    void reApply() {

      if(db==null) return;
      if(devName==null) return;
      int row = theTable.getSelectedRow();
      if(row>=0) {

        String pName = (String)dm.getValueAt(row,1);
        String value = (String)dm.getValueAt(row,2);
        if(value.equals("DELETED")) {
          JiveUtils.showJiveError("Cannot reapply deleted property");
          return;
        }
        try {
          int ok = JOptionPane.showConfirmDialog(null,
                  "Do you to reapply device property " + pName + "?",
                  "Confirm reapply", JOptionPane.YES_NO_OPTION);
          if (ok == JOptionPane.YES_OPTION)
            db.put_device_property(devName,JiveUtils.makeDbDatum(pName, value));
        } catch (DevFailed ex) {
          JiveUtils.showTangoError(ex);
        }

      } else {
        JiveUtils.showJiveError("Empty selection");
      }

    }

  }

  // -------- Class property panel ----------------------------------

  class ClassPropertyHistPanel extends PropertyHistPanel {

    private String className = null;

    ClassPropertyHistPanel() {
      initComponents("Class","Property");
    }

    void performSearch() {

      if(db==null) return;
      className = text1.getText();
      String pName = text2.getText();
      try {
        DbHistory[] hist = db.get_class_property_history(className, pName);
        updateHistory(hist);
      } catch (DevFailed ex) {
        JiveUtils.showTangoError(ex);
      }

    }

    void reApply() {

      if(db==null) return;
      if(className==null) return;
      int row = theTable.getSelectedRow();
      if(row>=0) {

        String pName = (String)dm.getValueAt(row,1);
        String value = (String)dm.getValueAt(row,2);
        if(value.equals("DELETED")) {
          JiveUtils.showJiveError("Cannot reapply deleted property");
          return;
        }
        try {
          int ok = JOptionPane.showConfirmDialog(null,
                  "Do you to reapply class property " + pName + "?",
                  "Confirm reapply", JOptionPane.YES_NO_OPTION);
          if (ok == JOptionPane.YES_OPTION)
            db.put_class_property(className,JiveUtils.makeDbDatum(pName, value));
        } catch (DevFailed ex) {
          JiveUtils.showTangoError(ex);
        }

      } else {
        JiveUtils.showJiveError("Empty selection");
      }

    }

  }

  // -------- Object property panel ----------------------------------

  class ObjectPropertyHistPanel extends PropertyHistPanel {

    private String objName = null;

    ObjectPropertyHistPanel() {
      initComponents("Object","Property");
    }

    void performSearch() {

      if(db==null) return;
      objName = text1.getText();
      String pName = text2.getText();
      try {
        DbHistory[] hist = db.get_property_history(objName, pName);
        updateHistory(hist);
      } catch (DevFailed ex) {
        JiveUtils.showTangoError(ex);
      }

    }

    void reApply() {

      if(db==null) return;
      if(objName==null) return;
      int row = theTable.getSelectedRow();
      if(row>=0) {

        String pName = (String)dm.getValueAt(row,1);
        String value = (String)dm.getValueAt(row,2);
        if(value.equals("DELETED")) {
          JiveUtils.showJiveError("Cannot reapply deleted property");
          return;
        }
        try {
          int ok = JOptionPane.showConfirmDialog(null,
                  "Do you to reapply free property " + pName + "?",
                  "Confirm reapply", JOptionPane.YES_NO_OPTION);
          if (ok == JOptionPane.YES_OPTION)
            db.put_property(objName,JiveUtils.makeDbDatum(pName, value));
        } catch (DevFailed ex) {
          JiveUtils.showTangoError(ex);
        }

      } else {
        JiveUtils.showJiveError("Empty selection");
      }

    }

  }

  // -------- Common attribute property panel ------------------------

  abstract class AttributePropertyHistPanel extends JPanel implements ActionListener {

    Database db;
    String colName[] = {"Date" , "Attribute" , "Property name" , "Value"};
    private MultiLineCellEditor editor;
    JTextField text1;
    JTextField text2;
    JTextField text3;
    private JLabel selectionLabel;
    private JButton searchButton;
    private JButton reApplyButton;
    DefaultTableModel dm;
    JTable theTable;

    void initComponents(String name1,String name2,String name3) {

      setLayout(new BorderLayout());
      JPanel innerPanel = new JPanel(null);
      innerPanel.setPreferredSize(new Dimension(640,85));
      JLabel deviceLabel = new JLabel(name1);
      deviceLabel.setFont(ATKConstant.labelFont);
      deviceLabel.setBounds(5,5,100,25);
      innerPanel.add(deviceLabel);
      text1 = new JTextField();
      text1.setMargin(JiveUtils.noMargin);
      text1.setFont(ATKConstant.labelFont);
      text1.setBounds(110,5,200,25);
      innerPanel.add(text1);

      JLabel attributeLabel = new JLabel(name2);
      attributeLabel.setFont(ATKConstant.labelFont);
      attributeLabel.setBounds(5,30,100,25);
      innerPanel.add(attributeLabel);
      text2 = new JTextField();
      text2.setMargin(JiveUtils.noMargin);
      text2.setFont(ATKConstant.labelFont);
      text2.setBounds(110,30,200,25);
      innerPanel.add(text2);

      JLabel propertyLabel = new JLabel(name3);
      propertyLabel.setFont(ATKConstant.labelFont);
      propertyLabel.setBounds(5,55,100,25);
      innerPanel.add(propertyLabel);
      text3 = new JTextField();
      text3.setMargin(JiveUtils.noMargin);
      text3.setFont(ATKConstant.labelFont);
      text3.setBounds(110,55,200,25);
      innerPanel.add(text3);

      searchButton = new JButton("Search");
      searchButton.setBounds(320,5,100,25);
      searchButton.addActionListener(this);
      innerPanel.add(searchButton);

      JPanel innerPanel2 = new JPanel(null);
      innerPanel2.setPreferredSize(new Dimension(640,35));
      reApplyButton = new JButton("Reapply selection");
      reApplyButton.setBounds(5,5,200,25);
      reApplyButton.addActionListener(this);
      reApplyButton.setEnabled(false);
      innerPanel2.add(reApplyButton);

      selectionLabel = new JLabel("Selection:");
      selectionLabel.setFont(ATKConstant.labelFont);
      selectionLabel.setHorizontalAlignment(JLabel.LEFT);
      selectionLabel.setBounds(210,5,400,25);
      innerPanel2.add(selectionLabel);

      add(innerPanel,BorderLayout.NORTH);
      add(innerPanel2,BorderLayout.SOUTH);

      // Table model
      dm = new DefaultTableModel() {

        public Class getColumnClass(int columnIndex) {
          return String.class;
        }

        public boolean isCellEditable(int row, int column) {
            return false;
        }

      };

      theTable = new JTable(dm);
      editor = new MultiLineCellEditor(theTable);
      theTable.setDefaultEditor(String.class, editor);
      MultiLineCellRenderer renderer = new MultiLineCellRenderer();
      theTable.setDefaultRenderer(String.class, renderer);
      theTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      theTable.addMouseListener(new MouseAdapter(){
         public void mousePressed(MouseEvent e) {
           refreshSelection();
         }
        public void mouseReleased(MouseEvent e) {
          refreshSelection();
        }
      });
      JScrollPane textView = new JScrollPane(theTable);
      add(textView,BorderLayout.CENTER);

    }

    private void refreshTable() {

      editor.updateRows();
      theTable.getColumnModel().getColumn(0).setMaxWidth(140);
      theTable.getColumnModel().getColumn(0).setMinWidth(140);
      theTable.getColumnModel().getColumn(0).setPreferredWidth(140);
      theTable.getColumnModel().getColumn(3).setPreferredWidth(300);
      theTable.validate();

    }

    private void refreshSelection() {

      int row = theTable.getSelectedRow();
      if(row<0) {
        selectionLabel.setText("Selection:");
        reApplyButton.setEnabled(false);
      } else {
        String value = (String)dm.getValueAt(row,3);
        if(value.equals("DELETED")) {
          selectionLabel.setText("Selection:");
          reApplyButton.setEnabled(false);
        } else {
          String pDate = (String)dm.getValueAt(row,0);
          String aName = (String)dm.getValueAt(row,1);
          String pName = (String)dm.getValueAt(row,2);
          selectionLabel.setText("Selection: " +aName + "/" + pName + " at " + pDate);
          reApplyButton.setEnabled(!JiveUtils.readOnly);
        }
      }

    }

    void setDatabase(Database db) {
      this.db = db;
      dm.setDataVector(new String[0][3], colName);
      selectionLabel.setText("Selection:");
      reApplyButton.setEnabled(false);
      refreshTable();
    }

    void updateHistory(DbHistory[] hist) {

      selectionLabel.setText("Selection:");
      reApplyButton.setEnabled(false);
      if (hist.length == 0) {
        JiveUtils.showJiveError("No data found.");
        dm.setDataVector(new String[0][3], colName);
      } else {
        String[][] prop = new String[hist.length][4];
        for (int i = 0; i < hist.length; i++) {
          prop[i][0] = hist[i].getDate();
          prop[i][1] = hist[i].getAttributeName();
          prop[i][2] = hist[i].getName();
          if (hist[i].isDeleted()) {
            prop[i][3] = "DELETED";
          } else {
            prop[i][3] = hist[i].getValue();
          }
        }
        dm.setDataVector(prop,colName);
      }
      refreshTable();

    }

    abstract void performSearch();

    abstract void reApply();

    public void actionPerformed(ActionEvent e) {

      Object src = e.getSource();

      if(src==searchButton) {
        performSearch();
      } else if(src==reApplyButton) {
        reApply();
      }

    }

  }

  // -------- Device attribute property panel ------------------------

  class DeviceAttributePropertyHistPanel extends AttributePropertyHistPanel {

    private String devName = null;

    DeviceAttributePropertyHistPanel() {
      initComponents("Device","Attribute","Property");
    }

    void performSearch() {

      if(db==null) return;
      devName = text1.getText();
      String attName = text2.getText();
      String pName = text3.getText();
      try {
        DbHistory[] hist = db.get_device_attribute_property_history(devName, attName ,pName);
        updateHistory(hist);
      } catch (DevFailed ex) {
        JiveUtils.showTangoError(ex);
      }

    }

    void reApply() {

      if(db==null) return;
      if(devName==null) return;
      int row = theTable.getSelectedRow();
      if(row>=0) {

        String aName = (String)dm.getValueAt(row,1);
        String pName = (String)dm.getValueAt(row,2);
        String value = (String)dm.getValueAt(row,3);
        if(value.equals("DELETED")) {
          JiveUtils.showJiveError("Cannot reapply deleted property");
          return;
        }
        try {

          int ok = JOptionPane.showConfirmDialog(null,
                  "Do you to reapply attribute property " + pName + "?",
                  "Confirm reapply", JOptionPane.YES_NO_OPTION);
          if (ok == JOptionPane.YES_OPTION) {

            // Reapply
            DbAttribute dba = new DbAttribute(aName);
            dba.add(pName, JiveUtils.makeStringArray(value));
            db.put_device_attribute_property(devName, dba);
            ok = JOptionPane.showConfirmDialog(null,
                    "The device " + devName + " need to be restarted\nDo you want to do it now ?",
                    "Confirm restart", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
              // Restar the device
              DbDevImportInfo info = db.import_device(devName);
              DeviceProxy ds = new DeviceProxy("dserver/" + info.server);
              DeviceData in = new DeviceData();
              in.insert(devName);
              ds.command_inout("DevRestart", in);
            }

          }

        } catch (DevFailed ex) {
          JiveUtils.showTangoError(ex);
        }
      } else {
        JiveUtils.showJiveError("Empty selection");
      }

    }

  }

  // -------- Device attribute property panel ------------------------

  class ClassAttributePropertyHistPanel extends AttributePropertyHistPanel {

    private String className = null;

    ClassAttributePropertyHistPanel() {
      initComponents("Class","Attribute","Property");
    }

    void performSearch() {

      if(db==null) return;
      className = text1.getText();
      String attName = text2.getText();
      String pName = text3.getText();
      try {
        DbHistory[] hist = db.get_class_attribute_property_history(className, attName ,pName);
        updateHistory(hist);
      } catch (DevFailed ex) {
        JiveUtils.showTangoError(ex);
      }

    }

    void reApply() {

      if(db==null) return;
      if(className==null) return;
      int row = theTable.getSelectedRow();
      if(row>=0) {

        String aName = (String)dm.getValueAt(row,1);
        String pName = (String)dm.getValueAt(row,2);
        String value = (String)dm.getValueAt(row,3);
        if(value.equals("DELETED")) {
          JiveUtils.showJiveError("Cannot reapply deleted property");
          return;
        }
        try {

          int ok = JOptionPane.showConfirmDialog(null,
                  "Do you to reapply attribute property " + pName + "?",
                  "Confirm reapply", JOptionPane.YES_NO_OPTION);
          if (ok == JOptionPane.YES_OPTION) {

            // Reapply
            DbAttribute dba = new DbAttribute(aName);
            dba.add(pName, JiveUtils.makeStringArray(value));
            db.put_class_attribute_property(className, dba);

          }

        } catch (DevFailed ex) {
          JiveUtils.showTangoError(ex);
        }
      } else {
        JiveUtils.showJiveError("Empty selection");
      }

    }

  }

  // -- Multiline Cell renderer --------------------------------------

  class MultiLineCellRenderer extends JTextArea implements TableCellRenderer {

    Color  selColor = new Color(200,200,255);

    public MultiLineCellRenderer() {
      setEditable(false);
      setLineWrap(false);
      setWrapStyleWord(false);
    }

    public Component getTableCellRendererComponent(JTable table,Object value,
                          boolean isSelected, boolean hasFocus, int row, int column) {

      if (value instanceof String)  setText((String)value);
      else                          setText("");

      if(isSelected) setBackground(selColor);
      else           setBackground(Color.white);

      return this;

    }
  }

}


