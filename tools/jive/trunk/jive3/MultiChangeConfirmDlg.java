package jive3;

import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import jive.MultiLineCellEditor;
import jive.MultiLineCellRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * Ask for confirmation on multiple property change
 */
public class MultiChangeConfirmDlg extends JDialog implements ActionListener {

  private JScrollPane textView;
  private JTable theTable;
  private MultiLineCellEditor editor;
  private DefaultTableModel dm;

  private JButton cancelBtn;
  private JButton performBtn;
  private JPanel  btnPanel;
  private JPanel  innerPanel;
  private JPanel warningPanel;
  private JTextArea warningText;

  private boolean goFlag;

  public MultiChangeConfirmDlg(Vector propValues,int nbDevice,String extraInfo) {

    super((JFrame)null,true);
    innerPanel = new JPanel();
    innerPanel.setLayout(new BorderLayout());

    // Warning panel
    warningPanel = new JPanel();
    warningPanel.setLayout(new BorderLayout());
    innerPanel.add(warningPanel,BorderLayout.NORTH);

    JLabel warningIcon = new JLabel();
    warningIcon.setIcon(UIManager.getIcon("OptionPane.warningIcon"));
    warningIcon.setBorder(BorderFactory.createEmptyBorder(10,10,10,20));
    warningPanel.add(warningIcon,BorderLayout.WEST);

    warningText = new JTextArea();
    warningText.setEditable(false);
    warningText.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
    warningText.setBackground(innerPanel.getBackground());
    warningPanel.add(warningText,BorderLayout.CENTER);

    int nbProp = propValues.size()/2;
    String nbPropStr = "";
    if( nbProp==1 )  nbPropStr = nbProp + " property";
    else             nbPropStr = nbProp + " properties";
    warningText.setText("You are going to modify " + nbPropStr + " on " + nbDevice + " devices" + extraInfo + ".\nDo you want to proceed ?");

    // Table model
    dm = new DefaultTableModel() {

      public Class getColumnClass(int columnIndex) {
        return String.class;
      }

      public boolean isCellEditable(int row, int column) {
        return false;
      }

      public void setValueAt(Object aValue, int row, int column) {
      }

    };

    // Table initialisation
    theTable = new JTable(dm);
    //theTable.addMouseListener(this);

    editor = new MultiLineCellEditor(theTable);
    //editor.getTextArea().addMouseListener(this);
    theTable.setDefaultEditor(String.class, editor);

    MultiLineCellRenderer renderer = new MultiLineCellRenderer();
    theTable.setDefaultRenderer(String.class, renderer);

    textView = new JScrollPane(theTable);
    add(textView, BorderLayout.CENTER);
    innerPanel.add(textView, BorderLayout.CENTER);

    // Fill table
    String colName[] = {"Property", "New value"};

    String[][] prop = new String[propValues.size() / 2][2];
    for (int i = 0; i < propValues.size(); i += 2) {
      prop[i / 2][0] = (String) propValues.get(i);
      prop[i / 2][1] = (String) propValues.get(i + 1);
    }

    dm.setDataVector(prop, colName);
    editor.updateRows();
    theTable.getColumnModel().getColumn(1).setPreferredWidth(250);

    theTable.validate();

    // Button panel
    btnPanel = new JPanel();
    btnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
    innerPanel.add(btnPanel, BorderLayout.SOUTH);

    performBtn = new JButton("Proceed");
    performBtn.addActionListener(this);
    btnPanel.add(performBtn);

    cancelBtn = new JButton("Cancel");
    cancelBtn.addActionListener(this);
    btnPanel.add(cancelBtn);

    setContentPane(innerPanel);
    setTitle("Multiple change confirmation");
    goFlag = false;

  }

  static public boolean confirmChange(Vector propValues,int nbDevice) {

    MultiChangeConfirmDlg dlg = new MultiChangeConfirmDlg(propValues,nbDevice,"");
    ATKGraphicsUtils.centerDialog(dlg);
    dlg.setVisible(true);
    return dlg.isOK();

  }
 
  
  static public boolean confirmChange(Vector propValues,int nbDevice,String extra) {

    MultiChangeConfirmDlg dlg = new MultiChangeConfirmDlg(propValues,nbDevice,extra);
    ATKGraphicsUtils.centerDialog(dlg);
    dlg.setVisible(true);
    return dlg.isOK();

  }

  public boolean isOK() {
    return goFlag;
  }

  public void actionPerformed(ActionEvent e) {

    if (e.getSource() == performBtn) {
      setVisible(false);
      goFlag = true;
    } else if(e.getSource() == cancelBtn ) {
      setVisible(false);
    }

  }

}
