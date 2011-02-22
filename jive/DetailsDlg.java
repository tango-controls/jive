package jive;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.tree.*;

/**
 * Class for display property details
 *
 * @author  pons
 */

public class DetailsDlg extends JDialog {

  private JScrollPane textView;
  private JTable      theTable;
  private JButton     okButton;
  private JButton     applyButton;
  private JPanel      innerPanel;
  private Vector      rows;
  private TreePath[]  paths;  
    
  // Apply resource change
  public void applyChange() {
	
    int ok=JOptionPane.showConfirmDialog(this,"Do you want to apply change ?","Update Tango Database",JOptionPane.YES_NO_OPTION);
    if( ok == JOptionPane.YES_OPTION ) {
      for(int i=0;i<paths.length;i++) {
        TangoTreeNode.setValue(paths[i],(String)theTable.getValueAt(i,1));
      }
    }
        
  }
  
  // Construction
  public DetailsDlg(Frame parent,Object[][] rows,TreePath[] p) {
     super(parent,true);
     
     paths=p;
             
     getContentPane().setLayout( new BorderLayout() );

     addWindowListener(new java.awt.event.WindowAdapter() {
          public void windowClosing(java.awt.event.WindowEvent evt) {
	    hide();
	    dispose();
          }
     });

     innerPanel = new JPanel();
     innerPanel.setLayout( new FlowLayout() );
     
     okButton = new JButton("Dismiss");
     okButton.addMouseListener(new MouseAdapter() {
       public void mouseClicked(MouseEvent evt) {
	 hide();
	 dispose();
       }
     });
     
     applyButton = new JButton("Apply change");
     applyButton.addMouseListener(new MouseAdapter() {
       public void mouseClicked(MouseEvent evt) {
         // Apply resources
         applyChange();
       }
     });
     
     innerPanel.add(applyButton);
     innerPanel.add(okButton);

     DefaultTableModel dm = new DefaultTableModel() {
       public Class getColumnClass(int columnIndex) {
        return String.class;
       }
     };
     
     String colName[] = { "Name" , "Value" };     
     dm.setDataVector( rows , colName );
     theTable = new JTable(dm);
          
     MultiLineCellEditor editor = new MultiLineCellEditor(theTable);
     theTable.setDefaultEditor(String.class,editor);
     theTable.getColumnModel().getColumn(0).setPreferredWidth(350);
     
     textView = new JScrollPane(theTable);
     getContentPane().add( textView , BorderLayout.CENTER );
     getContentPane().add( innerPanel , BorderLayout.SOUTH );     
     setTitle("View details");

     Rectangle r = parent.getBounds();
     int x = r.x + (r.width-600)/2;
     int y = r.y + (r.height-400)/2;
     setBounds(x,y,600,400);          
  }  
     
}
