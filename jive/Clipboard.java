package jive;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Class for managaging cut/copy/paste
 *
 * @author  pons
 */

class Item {
   
   public String   prop_name;
   public String   att_name;
   public String   value;
   
   // Contruct default Item object
   Item() {
     att_name   = null;
     prop_name  = null;
     value      = null;
   }
      
   public String toString() {
   
     String ret = "";
     
     if( prop_name == null || value == null ) {
       return "Item not initialised !!!";
     }
     
     String[] splitted_value = value.split("\n");
     
     if( att_name == null )
       ret += "OBJ_PROPERTY:" + prop_name + ": ";
     else 
       ret += "ATT_PROPERTY:" + att_name + "/" + prop_name + ": ";

     int i;
     for(i=0;i<splitted_value.length;i++) {
       ret += splitted_value[i];
       if( (i+1)<splitted_value.length ) ret += ",";
     }
     
     return ret;
     
   }
   
}

class ClipboardDlg extends JDialog {

  private JScrollPane textView;
  private JTextArea   textArea;
  private JButton     okButton;
  
  // Construction
  public ClipboardDlg(Frame parent,String txt) {
     super(parent,true);
     getContentPane().setLayout( new BorderLayout() );

     addWindowListener(new java.awt.event.WindowAdapter() {
          public void windowClosing(java.awt.event.WindowEvent evt) {
	    hide();
	    dispose();
          }
     });

     okButton = new JButton("Ok");
     okButton.addMouseListener(new MouseAdapter() {
       public void mouseClicked(MouseEvent evt) {
	 hide();
	 dispose();
       }
     });
     
     textArea = new JTextArea();
     textArea.setText( txt );
     textArea.setEditable( false );
     textView = new JScrollPane(textArea);
     getContentPane().add( textView , BorderLayout.CENTER );
     getContentPane().add( okButton , BorderLayout.SOUTH );     
     setTitle("Clipbaord content");

     Rectangle r = parent.getBounds();
     int x = r.x + (r.width-410)/2;
     int y = r.y + (r.height-310)/2;
     setBounds(x,y,410,310);     
  }  
    
}

public class Clipboard {

  private Vector Obj_Items;
  private Vector Att_Items;
  
  // Construct empty clipboard
  Clipboard() {
    Obj_Items = new Vector();
    Att_Items = new Vector();
  }
  
  // Add a property item to the clipboard
  public void add( String prop_name , String value ) {
    Item it      = new Item();
    it.prop_name = prop_name;
    it.value     = value;
    Obj_Items.add( it );
  }

  // Add an attribute property to the clipboard
  public void add( String prop_name , String att_name , String value ) {
    Item it      = new Item();
    it.att_name  = att_name;
    it.prop_name = prop_name;
    it.value     = value;
    Att_Items.add( it );
  }
  
  // Get Object property
  public int getObjectPropertyLength() {
    return Obj_Items.size();
  }
  public String getObjectPropertyName(int idx) {
    Item it = (Item)Obj_Items.get(idx);
    return it.prop_name;    
  }
  public String getObjectPropertyValue(int idx) {
    Item it = (Item)Obj_Items.get(idx);
    return it.value;    
  }
  
  // Get Attribute property
  public int getAttPropertyLength() {
    return Att_Items.size();
  }
  public String getAttName(int idx) {
    Item it = (Item)Att_Items.get(idx);
    return it.att_name;    
  }
  public String getAttPropertyName(int idx) {
    Item it = (Item)Att_Items.get(idx);
    return it.prop_name;    
  }
  public String getAttPropertyValue(int idx) {
    Item it = (Item)Att_Items.get(idx);
    return it.value;    
  }
  
  // Clear the entire clipboard
  public void clear() {
    Obj_Items.removeAllElements();
    Att_Items.removeAllElements();
  }
  
  // Return true if clipbaord empty
  public boolean empty() {
    return ( (Obj_Items.size()+Att_Items.size()) == 0);
  }
  
  // Print the clipboard content
  public String toString() {
     int i;
     String ret = "";
     
     for(i=0;i<Obj_Items.size();i++) {
       Item it = (Item)Obj_Items.get(i);
       ret += it; ret += "\n";
     }
     
     for(i=0;i<Att_Items.size();i++) {
       Item it = (Item)Att_Items.get(i);
       ret += it; ret += "\n";
     }
     
     return ret;     
  }

  // Show the contents of the clipbaord
  public void show(Frame parent) {
    ClipboardDlg dlg = new ClipboardDlg( parent , toString() );
    dlg.show();
  }
  
}
