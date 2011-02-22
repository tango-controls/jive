package jive;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 *
 * @author  pons
 */
 
public class ServerDlg extends JDialog {

  private JTextField  serverText;
  private JTextField  classText;
  private JTextArea   deviceText;
  private JScrollPane deviceView;
  private JButton     ok;
  private JButton     cancel;
  
  private JPanel     jp;
  
  boolean ret_value = false;
  
  // Construction without predefined values
  public ServerDlg(Frame parent) {
     super(parent,true);
     getContentPane().setLayout(null);

     addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent evt) {
            ret_value = false;
	    hide();
	    dispose();
          }
     });

     setTitle("Create/Edit a server");
     
     jp = new JPanel();
     jp.setBackground( Color.white );
     jp.setOpaque(false);
     jp.setBorder( BorderFactory.createLoweredBevelBorder() );
     getContentPane().add(jp);

     serverText = new JTextField();
     serverText.setEditable(true);
     serverText.setBorder( BorderFactory.createTitledBorder("Server  (ServerName/Instane)") );
     getContentPane().add(serverText);
     
     classText = new JTextField();
     classText.setEditable(true);
     classText.setBorder( BorderFactory.createTitledBorder("Class") );
     getContentPane().add(classText);
     
     deviceText = new JTextArea();
     deviceText.setEditable(true);
     deviceView = new JScrollPane(deviceText);
     deviceView.setBorder( BorderFactory.createTitledBorder("Devices") );
     deviceView.setBackground( Color.white );
     getContentPane().add(deviceView);

     ok = new JButton();
     ok.setText("Register server");
     getContentPane().add(ok);
     
     cancel = new JButton();
     cancel.setText("Cancel");
     getContentPane().add(cancel);
               
     serverText.setBounds( 5,10,390,40 );
     classText.setBounds( 5,50,390,40 );
     deviceView.setBounds( 5,90,390,150 );
     jp.setBounds( 3,8,394,234 );
     
     ok.setBounds( 5,245,150,30 );
     cancel.setBounds( 315,245,80,30 );
     
     cancel.addMouseListener(new MouseAdapter() {
       public void mouseClicked(MouseEvent evt) {
         ret_value = false;
	 hide();
         dispose();
       }
     });
          
     ok.addMouseListener(new MouseAdapter() {
       public void mouseClicked(MouseEvent evt) {
         // Check if server name has a correct format
	 String s = serverText.getText();
	 if( s.indexOf('/') == -1 ) {
	   TangoTreeNode.showJiveError("Server name must be entered as Name/Instance");
	   ret_value = false;	   
	 } else if( s.indexOf('/') != s.lastIndexOf('/') ) {
	   TangoTreeNode.showJiveError("Server name must be entered as Name/Instance");
	   ret_value = false;
	 } else {
           ret_value = true;
	 }	 
	 
	 hide();
	 dispose();
       }
     });     
	
     Rectangle r = parent.getBounds();
     int x = r.x + (r.width-410)/2;
     int y = r.y + (r.height-310)/2;
     setBounds(x,y,410,310);     
  }
  
  public void setValidFields(boolean s,boolean c) {
    serverText.setEnabled(s);
    classText.setEnabled(c);
  }
   
  public void setDefaults(String s,String c) {
    serverText.setText(s);
    classText.setText(c);
  }
    
  public boolean showDlg() {
    show();    
    return ret_value;
  }
  
  public String getServerName() {
    return serverText.getText();    
  }
  
  public String getClassName() {
    return classText.getText();    
  }
  
  public String[] getDeviceNames() { 
    
    String value = deviceText.getText();
    String[] splitted = value.split("\n");
    String[] ret = new String[1];
    int i,j;
    
    for(i=0,j=0;i<splitted.length;i++) {
      if( splitted[i].length() > 0 ) j++;
    }
    
    ret = new String[j];
    
    for(i=0,j=0;i<splitted.length;i++) {
      if( splitted[i].length() > 0 ) {
        ret[j] = splitted[i];
	j++;
      }
    }
    
    return ret;
    
  }
      
}
