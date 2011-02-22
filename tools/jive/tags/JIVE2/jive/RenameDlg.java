package jive;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 *
 * @author  pons
 */
 
public class RenameDlg extends JDialog {

  private JTextField theText;
  private JComboBox  theCombo;
  private boolean    ret_code;
  String  value;

  // Construction without predefined values
  public RenameDlg(Frame parent,String value,Rectangle bounds) {
     super(parent,true);
     getContentPane().setLayout(null);
     theText = new JTextField();
                    
     theText.addKeyListener(new KeyListener() {
     
       public void keyPressed(KeyEvent e) {
       }
       
       public void keyReleased(KeyEvent e) {
       
	 if( e.getKeyCode() == KeyEvent.VK_ENTER ) {
	   ret_code=true;
	   closeDlg();
	 }
	 
	 if( e.getKeyCode() == KeyEvent.VK_ESCAPE ) {
	   ret_code=false;
	   closeDlg();
	 }
	 	 
       }
       
       public void keyTyped(KeyEvent e) {
       }
       
     });
     
     getContentPane().add(theText);
     theText.setBounds(0,0,(int)bounds.getWidth(),(int)bounds.getHeight());     
     theText.setText(value);
     theText.setBorder( BorderFactory.createLineBorder(Color.black) );
     theText.selectAll();
     setBounds(bounds);
     setUndecorated(true);
     ret_code = false;
  }
  
  // Construction with predefined values
  public RenameDlg(Frame parent,String value,String[] choices,Rectangle bounds) {
     super(parent,true);
     getContentPane().setLayout(null);
     theCombo = new JComboBox();
     theCombo.setEditable(true);
     theCombo.removeAllItems();
     for(int i=0;i<choices.length;i++)
       theCombo.addItem(choices[i]);
       
     theText = (JTextField)theCombo.getEditor().getEditorComponent();
     
     theText.addKeyListener(new KeyListener() {
     
       public void keyPressed(KeyEvent e) {
       }
       
       public void keyReleased(KeyEvent e) {
       
	 if( e.getKeyCode() == KeyEvent.VK_ENTER ) {
	   ret_code=true;
	   closeDlg();
	 }
	 
	 if( e.getKeyCode() == KeyEvent.VK_ESCAPE ) {
	   ret_code=false;
	   closeDlg();
	 }
	 	 
       }
       
       public void keyTyped(KeyEvent e) {
       }
       
     });

 
     if( bounds.width < 100 ) bounds.width=100;
     
     getContentPane().add(theCombo);
     theCombo.setBounds(0,0,(int)bounds.getWidth(),(int)bounds.getHeight());     
     theText.setText(value);
     theText.selectAll();
     //theCombo.setBorder( BorderFactory.createLoweredBevelBorder() );
     setBounds(bounds);
     setUndecorated(true);
     ret_code = false;
  }
  
  public void closeDlg() {
    value = theText.getText();
    hide();
  }  
  
  public boolean showDlg() {
    show();
    return ret_code;
  }
  
  public String getNewName() {
    return value;
  }
  
  public void moveToLocation(int x,int y) {
    Rectangle r = getBounds();
    r.setLocation(x,y);
    setBounds(r);
  }
  
}
