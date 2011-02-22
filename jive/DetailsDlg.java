package jive;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

/**
 * Class for display property details
 *
 * @author  pons
 */

public class DetailsDlg extends JDialog {

  private JScrollPane textView;
  private JTable theTable;
  private JButton okButton;
  private JButton applyButton;
  private JPanel innerPanel;
  private TreePath[] paths;

  // Apply resource change
  public void applyChange() {

    int ok = JOptionPane.showConfirmDialog(this, "Do you want to apply change ?", "Update Tango Database", JOptionPane.YES_NO_OPTION);
    if (ok == JOptionPane.YES_OPTION) {
      for (int i = 0; i < paths.length; i++) {
        JiveUtils.setValue(paths[i], (String) theTable.getValueAt(i, 1));
      }
    }

  }

  // Construction
  public DetailsDlg(Frame parent, Object[][] rows, TreePath[] p) {
    super(parent, true);

    paths = p;

    getContentPane().setLayout(new BorderLayout());

    addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent evt) {
        setVisible(false);
        dispose();
      }
    });

    innerPanel = new JPanel();
    innerPanel.setLayout(new FlowLayout());

    okButton = new JButton("Dismiss");
    okButton.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
        setVisible(false);
        dispose();
      }
    });

    applyButton = new JButton("Apply change");
    applyButton.setEnabled(!JiveUtils.readOnly);
    if (!JiveUtils.readOnly) {
      applyButton.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent evt) {
          // Apply resources
          applyChange();
        }
      });
    }

    innerPanel.add(applyButton);
    innerPanel.add(okButton);

    DefaultTableModel dm = new DefaultTableModel() {
      public Class getColumnClass(int columnIndex) {
        return String.class;
      }
    };

    String colName[] = {"Name", "Value"};
    dm.setDataVector(rows, colName);
    theTable = new JTable(dm);

    MultiLineCellEditor editor = new MultiLineCellEditor(theTable);
    theTable.setDefaultEditor(String.class, editor);
    theTable.getColumnModel().getColumn(0).setPreferredWidth(350);

    textView = new JScrollPane(theTable);
    getContentPane().add(textView, BorderLayout.CENTER);
    getContentPane().add(innerPanel, BorderLayout.SOUTH);
    setTitle("View details");

  }

  public void showDlg() {
    JiveUtils.centerDialog(this,600,380);
    setVisible(true);
  }

}
