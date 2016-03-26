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
import java.util.Vector;

/**
 * Coherency checking between file and database
 */
public class DiffDlg extends JFrame implements ActionListener {

  private JScrollPane textView;
  private JTable      theTable;
  private MultiLineCellEditor editor;
  private DefaultTableModel   dm;

  private JButton     dismissBtn;
  private JPanel      btnPanel;
  private JPanel      innerPanel;

  public DiffDlg(Vector diff,String fileName) {

    innerPanel = new JPanel();
    innerPanel.setLayout(new BorderLayout());

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
    innerPanel.add(textView,BorderLayout.CENTER);

    // Fill table
    String colName[] = {"Property", "Database value", "File value"};

    String[][] prop = new String[diff.size()/3][3];
    for(int i=0;i<diff.size();i+=3) {
      prop[i/3][0] = (String)diff.get(i);
      prop[i/3][1] = (String)diff.get(i+1);
      prop[i/3][2] = (String)diff.get(i+2);
    }

    dm.setDataVector(prop, colName);
    editor.updateRows();
    theTable.validate();

    // Button panel
    btnPanel = new JPanel();
    btnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
    innerPanel.add(btnPanel,BorderLayout.SOUTH);

    dismissBtn = new JButton("Dismiss");
    dismissBtn.addActionListener(this);
    btnPanel.add(dismissBtn);

    setContentPane(innerPanel);
    setTitle("DB diff ["+fileName+"]");

  }


  public void actionPerformed(ActionEvent e) {

    if( e.getSource()==dismissBtn ) {
      setVisible(false);
    }

  }

}
