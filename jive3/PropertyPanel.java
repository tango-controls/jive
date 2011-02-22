package jive3;

import jive.MultiLineCellEditor;
import jive.JiveUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class PropertyPanel extends JPanel implements ActionListener,MouseListener {

  private JScrollPane textView;
  private JTable      theTable;
  private JButton     refreshButton;
  private JButton     applyButton;
  private JButton     deleteButton;
  private JButton     copyButton;
  private JButton     newButton;
  private MainPanel   parent;

  private PropertyNode[]      source = null;
  private DefaultTableModel   dm;
  private MultiLineCellEditor editor;
  private boolean[]           updatedProp;
  private JPopupMenu          tableMenu;
  private JMenuItem           historyMenuItem;
  private JMenuItem           renameMenuItem;
  private JMenuItem           deleteMenuItem;
  private JMenuItem           copyMenuItem;

  PropertyPanel()  {

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
          applyButton.setEnabled(true);
          updatedProp[row] = true;
        }
      }

    };

    // Table initialisation
    theTable = new JTable(dm);
    theTable.addMouseListener(this);

    editor = new MultiLineCellEditor(theTable);
    editor.getTextArea().addMouseListener(this);
    theTable.setDefaultEditor(String.class, editor);

    MultiLineCellRenderer renderer = new MultiLineCellRenderer();
    theTable.setDefaultRenderer(String.class, renderer);

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
    applyButton.addActionListener(this);
    deleteButton = new JButton("Delete");
    deleteButton.setEnabled(!JiveUtils.readOnly);
    deleteButton.addActionListener(this);
    copyButton = new JButton("Copy");
    copyButton.setEnabled(!JiveUtils.readOnly);
    copyButton.addActionListener(this);
    newButton = new JButton("New property");
    newButton.setEnabled(!JiveUtils.readOnly);
    newButton.addActionListener(this);
    btnPanel.add(refreshButton);
    btnPanel.add(applyButton);
    btnPanel.add(newButton);
    btnPanel.add(copyButton);
    btnPanel.add(deleteButton);
    add(btnPanel,BorderLayout.SOUTH);

    // Contextual menu
    tableMenu = new JPopupMenu();
    copyMenuItem = new JMenuItem("Copy");
    copyMenuItem.addActionListener(this);
    renameMenuItem = new JMenuItem("Rename");
    renameMenuItem.addActionListener(this);
    historyMenuItem = new JMenuItem("View history");
    historyMenuItem.addActionListener(this);
    deleteMenuItem = new JMenuItem("Delete");
    deleteMenuItem.addActionListener(this);
    tableMenu.add(copyMenuItem);
    tableMenu.add(deleteMenuItem);
    tableMenu.add(renameMenuItem);
    tableMenu.add(historyMenuItem);

  }

  // Mouse listener -------------------------------------------


  public void mousePressed(MouseEvent e) {}
  public void mouseClicked(MouseEvent e) {

    Object src = e.getSource();

    if(src==theTable && e.getButton() == MouseEvent.BUTTON3 && e.getClickCount()==1 && !JiveUtils.readOnly) {

      int row = getRowForLocation(e.getY());
      if(row!=-1) {
        theTable.addRowSelectionInterval(row,row);
        theTable.setColumnSelectionInterval(0,1);
        int[] rows = theTable.getSelectedRows();
        renameMenuItem.setEnabled(rows.length==1);
        historyMenuItem.setEnabled(rows.length==1);
        tableMenu.show(theTable, e.getX(), e.getY());
      }

    }

    if(src==editor.getTextArea() && e.getButton() == MouseEvent.BUTTON3 && e.getClickCount()==1) {
      String selText = editor.getTextArea().getSelectedText();
      if( selText!=null ) {
        // Basic device name check
        int slashCount=0;
        for(int i=0;i<selText.length();i++) {
          if( selText.charAt(i)=='/' ) slashCount++;
        }
        if( slashCount==2 ) {
          parent.goToDeviceNode(selText);
        }
      }
    }

  }
  public void mouseReleased(MouseEvent e) {}
  public void mouseEntered(MouseEvent e) {}
  public void mouseExited(MouseEvent e) {}

  // -----------------------------------------------------------
  
  public void setParent(MainPanel parent) {
    this.parent = parent;
  }

  public boolean hasChanged() {
    return applyButton.isEnabled();
  }

  public void saveChange() {

    // Update property
    int nb = getNbUpdated() * source.length;
    int k = 0;
    if(nb>1) ProgressFrame.displayProgress("Updating properties");
    for(int i=0;i<updatedProp.length;i++) {
      if(updatedProp[i]) {
        for(int j=0;j<source.length;j++) {
          k++;
          ProgressFrame.setProgress("Applying " + source[j].getName() + "/" + dm.getValueAt(i,0),
                                    (k*100)/nb );
          source[j].setProperty((String)dm.getValueAt(i,0),(String)dm.getValueAt(i,1));
        }
        updatedProp[i] = false;
      }
    }
    applyButton.setEnabled(false);
    ProgressFrame.hideProgress();

  }

  // Action listener -------------------------------------------

  public void actionPerformed(ActionEvent e) {
    Object src = e.getSource();

    if( src==refreshButton ) {

      // Refresh the panel
      refreshValue();

    } else if (src==applyButton) {

      saveChange();

    } else if (src==deleteMenuItem || src==deleteButton) {

      // Delete property
      int[] rows = theTable.getSelectedRows();
      if(rows.length==0) {
        JiveUtils.showJiveError("Selection is empty.");
        return;
      }
      int ok = JOptionPane.showConfirmDialog(this, "Delete selection ?", "Confirm delete", JOptionPane.YES_NO_OPTION);
      if (ok == JOptionPane.YES_OPTION) {
        int nb = rows.length * source.length;
        int k = 0;
        if(nb>1) ProgressFrame.displayProgress("Deleting properties");
        for(int i=0;i<rows.length;i++)
          for(int j=0;j<source.length;j++) {
            k++;
            ProgressFrame.setProgress("Deleting " + source[j].getName() + "/" + dm.getValueAt(rows[i],0),
                                      (k*100)/nb );
            source[j].deleteProperty((String)dm.getValueAt(rows[i],0));
          }
        ProgressFrame.hideProgress();
        refreshValue();
      }

    } else if (src==renameMenuItem) {

      int row = theTable.getSelectedRow();
      String oldName = (String)dm.getValueAt(row,0);
      String value = (String)dm.getValueAt(row,1);
      String newName = JOptionPane.showInputDialog(null,"Rename property",oldName);
      if(newName==null) return;
      if(propertyExists(newName)) {
        JiveUtils.showJiveError("Name already exists.");
        return;
      }
      int k = 0;
      int nb = source.length;
      if(nb>1) ProgressFrame.displayProgress("Renaming properties");
      for(int j=0;j<source.length;j++) {
        k++;
        ProgressFrame.setProgress("Renaming " + source[j].getName() + "/" + oldName,
                                  (k*100)/nb );
        source[j].rename(oldName,value,newName);
      }
      refreshValue();
      ProgressFrame.hideProgress();

    } else if (src==historyMenuItem) {
    
      int row = theTable.getSelectedRow();
      String propName = (String)dm.getValueAt(row,0);
      String objName = source[0].getName();
      
      if( source[0] instanceof TaskDevicePropertyNode ) {
            
        parent.historyDlg.viewDevicePropertyHistory(objName,propName);
        parent.showHistory();
	
      } else if ( source[0] instanceof TaskDeviceAttributePropertyNode ) {

        String attName = ((TaskDeviceAttributePropertyNode)source[0]).getAttributeName();
        parent.historyDlg.viewDeviceAttPropertyHistory(objName,attName,propName);
        parent.showHistory();
      
      } else if ( source[0] instanceof TaskClassAttributePropertyNode ) {
      
        String attName = ((TaskClassAttributePropertyNode)source[0]).getAttributeName();
        parent.historyDlg.viewClassAttPropertyHistory(objName,attName,propName);
        parent.showHistory();
      
      } else if ( source[0] instanceof TaskFreePropertyNode ) {
      
        parent.historyDlg.viewFreePropertyHistory(objName,propName);
        parent.showHistory();
      
      } else if ( source[0] instanceof TaskClassPropertyNode ) {
      
        parent.historyDlg.viewClassPropertyHistory(objName,propName);
        parent.showHistory();
      
      } else if ( source[0] instanceof TaskSubDevicePropertyNode ) {
      
        String devName  = ((TaskSubDevicePropertyNode)source[0]).getDevName();
        String pName = ((TaskSubDevicePropertyNode)source[0]).getSubName() + "/" + propName;
        parent.historyDlg.viewDevicePropertyHistory(devName,pName);
        parent.showHistory();
      
      }
    
    } else if (src==newButton) {

      String newName = JOptionPane.showInputDialog(null,"Add property","");
      if(newName==null) return;
      if(propertyExists(newName)) {
        JiveUtils.showJiveError("Name already exists.");
        return;
      }
      int k = 0;
      int nb = source.length;
      if(nb>1) ProgressFrame.displayProgress("Creating properties");
      for(int j=0;j<source.length;j++) {
        k++;
        ProgressFrame.setProgress("Creating " + source[j].getName() + "/" + newName,
                                  (k*100)/nb );
        source[j].setProperty(newName,"");
      }
      refreshValue();
      ProgressFrame.hideProgress();

    } else if (src==copyMenuItem || src==copyButton) {

      int[] rows = theTable.getSelectedRows();
      if(rows.length==0) {
        JiveUtils.showJiveError("Nothing to copy.");
        return;
      }
      JiveUtils.the_clipboard.clear();
      for(int i=0;i<rows.length;i++)
        JiveUtils.the_clipboard.add((String)dm.getValueAt(rows[i],0),(String)dm.getValueAt(rows[i],1));

    }

  }

  void setSource(PropertyNode[] src) {
    this.source = src;
    refreshValue();
  }

  private void refreshValue() {

    applyButton.setEnabled(false);
    if (source != null) {
      String colName[] = {"Property name", "Value"};
      String[][] prop = source[0].getProperties();
      dm.setDataVector(prop, colName);
      updatedProp = new boolean[prop.length];
      for(int i=0;i<prop.length;i++) updatedProp[i] = false;
      editor.updateRows();
      theTable.getColumnModel().getColumn(1).setPreferredWidth(250);
      theTable.validate();
      String title = source[0].getTitle();
      if(source.length==1) {
        title += " [" + source[0].getName() + "]";
      } else {
        title += " [" + source.length + " items selected]";
      }
      Border b = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),title);
      textView.setBorder(b);
    }

  }

  private boolean propertyExists(String name) {

    boolean found = false;
    int i=0;
    while(i<dm.getRowCount() && !found) {
      found = ((String)dm.getValueAt(i,0)).equalsIgnoreCase(name);
      if(!found) i++;
    }
    return found;

  }

  private int getNbUpdated() {
    int k = 0;
    for(int i=0;i<updatedProp.length;i++)
      if(updatedProp[i]) k++;
    return k;
  }

  private int getRowForLocation(int y) {

    boolean found = false;
    int i = 0;
    int h = 0;

    while(i<dm.getRowCount() && !found) {
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

  class MultiLineCellRenderer extends JTextArea implements TableCellRenderer {

    Color  selColor = new Color(200,200,255);
    Border selBorder = BorderFactory.createLineBorder(selColor);

    public MultiLineCellRenderer() {
      setEditable(false);
      setLineWrap(false);
      setWrapStyleWord(false);
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {

      if (value instanceof String) {
        setText((String) value);
        // set the table's row height, if necessary
        //updateRowHeight(row,getPreferredSize().height);
      } else
        setText("");

      int[] selRows = table.getSelectedRows();

      if (JiveUtils.contains(selRows,row) && column==0)
        setBackground(selColor);
      else
        setBackground(Color.WHITE);

      if(isSelected && column==1) {
        setBorder(selBorder);
      } else {
        setBorder(null);
      }

      return this;
    }
  }

}
