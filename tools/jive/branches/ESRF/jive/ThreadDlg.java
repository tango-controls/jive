package jive;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Class for handling the search in an asynchronous way
 *
 * @author  pons
 */

public class ThreadDlg extends JDialog {

  private JLabel      textArea;
  private JButton     okButton;
  private Thread      subProc;
  private JPanel      innerPanel;
  
  // Construction
  public ThreadDlg(Frame parent,Thread process) {
    
     super(parent,true);
     getContentPane().setLayout( null );
     okButton = new JButton("Stop search");
     okButton.addMouseListener(new MouseAdapter() {
       public void mouseClicked(MouseEvent evt) {
         TangoTreeNode.searchStopflag = true;
       }
     });
          
     textArea = new JLabel();
     innerPanel = new JPanel();
     innerPanel.setOpaque( false );
     innerPanel.setBorder( BorderFactory.createEtchedBorder() );
     
     textArea.setText( "Searching the database" );
     textArea.setBounds( 5   , 5  , 200 , 40 );
     textArea.setHorizontalAlignment(JLabel.CENTER);
     okButton.setBounds( 50 , 52 , 100  , 23 );
     innerPanel.setBounds(0,0,210,80);
     
     getContentPane().add( textArea );
     getContentPane().add( okButton );
     getContentPane().add( innerPanel );
     setUndecorated(true);
     
     // Add a thread listener
     subProc = process;
     
     Rectangle r = parent.getBounds();
     int x = r.x + (r.width-210)/2;
     int y = r.y + (r.height-80)/2;
     setBounds(x,y,210,80);
     
     // Add window listener to start the subProc
     // when the dialog is displayed
     addWindowListener(new WindowAdapter() {
       public void windowOpened(WindowEvent e) {
         subProc.start();
       }
     });

  }
        
}
