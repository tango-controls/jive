package jive;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

/**
 * Class for managaging cut/copy/paste
 *
 * @author  pons
 */

class Item {

  public final static int OBJ_PROPERTY = 1;
  public final static int ATT_PROPERTY = 2;
  public final static int SRV_INSTANCE = 3;

  public int    type;
  public String prop_name;
  public String att_name;
  public String inst_name;
  public String class_name;
  public String value;

  // Construct default Item object

  Item(int type) {
    this.type = type;
    att_name = null;
    prop_name = null;
    inst_name = null;
    class_name = null;
    value = null;
  }

  private String getSplitValue() {

    String[] split_value = value.split("\n");
    String ret = "";

    int i;
    for (i = 0; i < split_value.length; i++) {
      ret += split_value[i];
      if ((i + 1) < split_value.length) ret += ",";
    }

    return ret;

  }

  public String toString() {

    String ret = "";

    switch(type) {
      case OBJ_PROPERTY:
        ret += "OBJ_PROPERTY:" + prop_name + ": " + getSplitValue();
        break;
      case ATT_PROPERTY:
        ret += "ATT_PROPERTY:" + att_name + "/" + prop_name + ": " + getSplitValue();
        break;
      case SRV_INSTANCE:
        ret += "SRV_INSTANCE:" + inst_name + "/" + class_name + ": " + value;
        break;
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
        setVisible(false);
        dispose();
      }
    });

     okButton = new JButton("Ok");
    okButton.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
        setVisible(false);
        dispose();
      }
    });
     
     textArea = new JTextArea();
     textArea.setText( txt );
     textArea.setEditable( false );
     textView = new JScrollPane(textArea);
     getContentPane().add( textView , BorderLayout.CENTER );
     getContentPane().add( okButton , BorderLayout.SOUTH );     
     setTitle("Clipboard content");

  }
    
}

public class Clipboard {

  private Vector Obj_Items;
  private Vector Att_Items;
  private Vector Srv_Items;
  
  // Construct empty clipboard
  Clipboard() {
    Obj_Items = new Vector();
    Att_Items = new Vector();
    Srv_Items = new Vector();
  }
  
  // Add a property item to the clipboard
  public void add( String prop_name , String value ) {
    Item it      = new Item(Item.OBJ_PROPERTY);
    it.prop_name = prop_name;
    it.value     = value;
    Obj_Items.add( it );
  }

  // Add an attribute property to the clipboard
  public void add( String prop_name , String att_name , String value ) {
    Item it      = new Item(Item.ATT_PROPERTY);
    it.att_name  = att_name;
    it.prop_name = prop_name;
    it.value     = value;
    Att_Items.add( it );
  }

  // Add an instance/class (including device) to the clipboard
  public void addDevice( String inst_name, String class_name, String device) {
    Item it      = new Item(Item.SRV_INSTANCE);
    it.inst_name  = inst_name;
    it.class_name = class_name;
    it.value      = device;
    Srv_Items.add( it );
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

  // Get instance/class/device
  public int getSrvInstanceLength() {
    return Srv_Items.size();
  }
  public String getInstanceName(int idx) {
    Item it = (Item)Srv_Items.get(idx);
    return it.inst_name;
  }
  public String getClassName(int idx) {
    Item it = (Item)Srv_Items.get(idx);
    return it.class_name;
  }
  public String getDeviceName(int idx) {
    Item it = (Item)Srv_Items.get(idx);
    return it.value;    
  }

  // Clear the entire clipboard
  public void clear() {
    Obj_Items.removeAllElements();
    Att_Items.removeAllElements();
    Srv_Items.removeAllElements();
  }
  
  // Return true if clipboard empty
  public boolean empty() {
    return ( (Obj_Items.size()+Att_Items.size()+Srv_Items.size() ) == 0);
  }

  // Print the clipboard content
  public String toString() {

    int i;
    String ret = "";

    for (i = 0; i < Obj_Items.size(); i++) {
      Item it = (Item) Obj_Items.get(i);
      ret += it;
      ret += "\n";
    }

    for (i = 0; i < Att_Items.size(); i++) {
      Item it = (Item) Att_Items.get(i);
      ret += it;
      ret += "\n";
    }

    for (i = 0; i < Srv_Items.size(); i++) {
      Item it = (Item) Srv_Items.get(i);
      ret += it;
      ret += "\n";
    }

    return ret;
  }

  // Show the contents of the clipbaord
  public void show(Frame parent) {
    ClipboardDlg dlg = new ClipboardDlg( parent , toString() );
    JiveUtils.centerDialog(dlg,410,290);
    dlg.setVisible(true);
  }
  
}
