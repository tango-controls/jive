package jive;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
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

}

class ClipboardDlg extends JDialog {

  private JScrollPane textView;
  private JTextArea   textArea;
  private JButton dismissButton;
  
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


    dismissButton = new JButton("Dismiss");
    dismissButton.addMouseListener(new MouseAdapter() {
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

    JPanel btnPanel = new JPanel();
    btnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
    btnPanel.add(dismissButton);

    getContentPane().add(btnPanel, BorderLayout.SOUTH );
    setTitle("Clipboard content");

  }
    
}

public class Clipboard {

  java.awt.datatransfer.Clipboard clipboard;
  private Vector Obj_Items;
  private Vector Att_Items;
  private Vector Srv_Items;

  private Reader theStream;
  private char nextNextChar;
  private char nextChar;
  private char currentChar;
  private boolean backSlashed;

  // ****************************************************
  private void read_char() {

    backSlashed = false;

    try {
      int c = theStream.read();
      currentChar = nextChar;
      nextChar = nextNextChar;
      if(c<0) {
        nextNextChar = 0;
      } else {
        nextNextChar = (char) c;
      }
    } catch (Exception e) {
      nextNextChar = 0;
      nextChar = 0;
      currentChar = 0;
    }

    /* Treat \" here */
    if(currentChar=='\\' && nextChar=='"') {
      read_char(); // return '"'
      backSlashed = true;
    }

  }

  // ****************************************************
  private void jump_space() {
    while (currentChar <= 32 && currentChar > 0) read_char();
  }

  // ****************************************************
  private String read_word() throws IOException {

    StringBuffer ret_word = new StringBuffer();

    /* Jump space and comments */
    jump_space();

    /* Treat special character */
    if (currentChar == ',' || currentChar == ':' || currentChar == '/') {
      ret_word.append(currentChar);
      read_char();
      return ret_word.toString();
    }

    /* Treat string */
    if (currentChar=='"' && !backSlashed) {
      read_char();
      while ((currentChar != '"' || backSlashed) && currentChar != 0 && currentChar != '\n') {
        ret_word.append(currentChar);
        read_char();
      }
      if (currentChar == 0 || currentChar == '\n') {
        IOException e = new IOException("Unterminated string.");
        throw e;
      }
      read_char();
      return ret_word.toString();
    }

    /* Treat other word */
    while (currentChar > 32 && currentChar != ',' && currentChar != ':' && currentChar != '/') {
      ret_word.append(currentChar);
      read_char();
    }

    if (ret_word.length() == 0) {
      return null;
    }

    return ret_word.toString();
  }

  // ****************************************************
  private void jump_sep(String sep) throws IOException {
    String w = read_word();
    if(w==null)
      throw new IOException("Separator " + sep + " expected.");
    if(!w.equals(sep))
      throw new IOException("Separator " + sep + " expected.");
  }

  // Construct empty clipboard

  Clipboard() {
    clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    Obj_Items = new Vector();
    Att_Items = new Vector();
    Srv_Items = new Vector();
  }

  private String quoteValue(String value) {

    if(value.length()==0)
      return "\"\"";

    if(value.indexOf('\"')>=0) {
      // We have to backslash '\"'
      StringBuffer b= new StringBuffer();
      for(int i=0;i<value.length();i++) {
        if(value.charAt(i)=='"')
          b.append('\\');
        b.append(value.charAt(i));
      }
      value = b.toString();
    }

    if(value.indexOf(' ')>=0 || value.indexOf(',')>=0 || value.indexOf(':')>=0 || value.indexOf('/')>=0) {
      return "\"" + value + "\"";
    } else {
      return value;
    }

  }

  private String getCommaSeparatedValue(String value) {

    String[] split_value = value.split("\n");
    String ret = "";

    int i;
    for (i = 0; i < split_value.length; i++) {
      ret += quoteValue(split_value[i]);
      if ((i + 1) < split_value.length) ret += ",";
    }

    return ret;

  }

  private String getClipboardContent() {

    String ret = "";
    java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    try {
      ret = (String)(clipboard.getData(DataFlavor.stringFlavor));
    }
    catch (UnsupportedFlavorException e1) {
      JiveUtils.showJiveError("Clipboard error :" + e1.getMessage());
    } catch (IOException e2) {
      JiveUtils.showJiveError("Clipboard error :" + e2.getMessage());
    }
    return ret;

  }

  // Add a property item to the clipboard
  public void add( String prop_name , String value ) {

    String content = getClipboardContent();
    content += "OBJ_PROPERTY:" + quoteValue(prop_name) + ": " + getCommaSeparatedValue(value) + "\n";
    StringSelection stringSelection = new StringSelection( content );
    clipboard.setContents( stringSelection, null );

  }

  // Add an attribute property to the clipboard
  public void add( String prop_name , String att_name , String value ) {

    String content = getClipboardContent();
    content += "ATT_PROPERTY:" + att_name + "/" + quoteValue(prop_name) + ": " + getCommaSeparatedValue(value) + "\n";
    StringSelection stringSelection = new StringSelection( content );
    clipboard.setContents( stringSelection, null );

  }

  // Add an instance/class (including device) to the clipboard
  public void addDevice( String inst_name, String class_name, String device) {

    String content = getClipboardContent();
    content += "SRV_INSTANCE:" + inst_name + "/" + class_name + ": " + getCommaSeparatedValue(device) + "\n";
    StringSelection stringSelection = new StringSelection( content );
    clipboard.setContents( stringSelection, null );

  }

  public String parseValue() throws IOException {

    StringBuffer b = new StringBuffer();

    do {
      if(currentChar==',')
        read_char();
      String s = read_word();
      b.append(s);
      if(currentChar==',')
        b.append("\n");
    } while (currentChar==',');

    return b.toString();

  }

  public void parseObject() throws IOException {

    String type = read_word();
    if(type==null) return;

    if( type.equals("OBJ_PROPERTY") ) {
      jump_sep(":");
      Item it      = new Item(Item.OBJ_PROPERTY);
      it.prop_name = read_word();
      jump_sep(":");
      it.value = parseValue();
      Obj_Items.add( it );
    } else if (type.equals("ATT_PROPERTY")) {
      jump_sep(":");
      Item it      = new Item(Item.ATT_PROPERTY);
      it.att_name  = read_word();
      jump_sep("/");
      it.prop_name = read_word();
      jump_sep(":");
      it.value     = parseValue();
      Att_Items.add( it );
    } else if (type.equals("SRV_INSTANCE")) {
      Item it      = new Item(Item.SRV_INSTANCE);
      it.inst_name  = read_word();
      jump_sep("/");
      it.class_name = read_word();
      jump_sep(":");
      it.value      = parseValue();
      Srv_Items.add( it );
    } else {
      throw new IOException("Unexpected object " + type);
    }

  }

  public void parse() {

    Obj_Items.clear();
    Att_Items.clear();
    Srv_Items.clear();

    String content = getClipboardContent();
    theStream = new StringReader(content);

    // Done 3 times to initialise nextchars and currentChar
    read_char();
    read_char();
    read_char();

    try {

      do {
        parseObject();
      } while (currentChar != 0);

    } catch (IOException e) {
      JiveUtils.showJiveError("Invalid clipboard content\n"+e.getMessage());
    }

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

    StringSelection stringSelection = new StringSelection("");
    clipboard.setContents( stringSelection, null );

  }
  
  // Return true if clipboard empty
  public boolean empty() {
    return ( (Obj_Items.size()+Att_Items.size()+Srv_Items.size() ) == 0);
  }

  // Show the contents of the clipbaord
  public void show(Frame parent) {
    ClipboardDlg dlg = new ClipboardDlg( parent ,getClipboardContent() );
    JiveUtils.centerDialog(dlg,410,290);
    dlg.setVisible(true);
  }
  
}
