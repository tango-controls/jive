package jive3;

import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.bind.Marshaller;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

/**
 * A TextEditor that display a popup with text suggestions
 */

class TipPopup implements MouseMotionListener,MouseListener {

  String[] items;
  JLabel text;
  JScrollPane textView;
  JTextTips parent;
  Popup popup;
  int hFont;
  int selectedIdx = -1;
  int[] globalIdx = null;
  boolean visible = false;
  String prefix;
  int compWidth;

  TipPopup(JTextTips parent) {

    this.parent = parent;
    text = new JLabel();
    text.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
    text.setBackground(new Color(245, 245, 250));
    text.setHorizontalAlignment(SwingConstants.LEFT);
    text.setOpaque(true);
    hFont = (int)(ATKGraphicsUtils.measureString("ABC",text.getFont()).getHeight()+0.5) + 1;

    text.addMouseMotionListener(this);
    text.addMouseListener(this);

    textView = new JScrollPane(text);
    textView.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

  }

  void setList(String[] items) {
    this.items = items;
  }

  void down() {
    if(visible && selectedIdx<globalIdx.length-1) {
      selectedIdx++;
      buildText();
      if(selectedIdx>=0)
        parent.setTextInternal(items[globalIdx[selectedIdx]]);
      scrollToVisible();
    }
  }

  void up() {
    if(visible && selectedIdx>0) {
      selectedIdx--;
      buildText();
      if(selectedIdx<globalIdx.length)
        parent.setTextInternal(items[globalIdx[selectedIdx]]);
      scrollToVisible();
    }
  }

  void select() {
    if(selectedIdx>=0 && selectedIdx<globalIdx.length)
      parent.setTextInternal(items[globalIdx[selectedIdx]]);
    selectedIdx = -1;
    globalIdx = new int[0];
    setVisible(false);
  }

  void scrollToVisible() {

    Rectangle r = new Rectangle();
    parent.getBounds(r);
    r.x = 0;
    r.y = selectedIdx * hFont;
    r.height = hFont;
    text.scrollRectToVisible(r);

  }

  int buildText() {

    if(items==null || items.length==0)
      return 0;

    String prefixLw = prefix.toLowerCase();
    int count = 0;
    Vector<Integer> v = new Vector<Integer>();

    StringBuffer str = new StringBuffer();
    str.append("<html>\n");
    str.append("<body>\n");
    str.append("<table cellpadding=\"0\" cellspacing=\"0\" width=\""+compWidth+"px\">\n");

    for(int i=0;i<items.length;i++) {
      if(items[i].toLowerCase().startsWith(prefixLw)) {
        String it = "<font color=\"#000000\" >"+prefix+"</font><font color=\"#A5A5A5\">"+items[i].substring(prefix.length())+"</font>";
        if(count==selectedIdx) {
          str.append("<tr><td bgcolor=\"#959595\">"+it+"</td></tr>");
        } else {
          str.append("<tr><td>"+it+"</td></tr>");
        }
        v.add(i);
        count++;
      }
    }

    str.append("</table>\n");
    str.append("</body>\n");
    str.append("</html>\n");

    text.setText(str.toString());

    globalIdx = new int[v.size()];
    for(int i=0;i<v.size();i++) globalIdx[i] = v.get(i);

    return count;

  }

  void updateText() {
    selectedIdx = -1;
    globalIdx = new int[0];
    prefix = parent.getText();
    setVisible( buildText()>0 );
  }

  public void setVisible(boolean visible) {

    if(popup!=null)
      popup.hide();

    if(visible) {
      Rectangle r = new Rectangle();
      parent.getBounds(r);
      compWidth = r.width;
      int h = text.getPreferredSize().height+10;
      if(h>150) h = 150;
      textView.setPreferredSize(new Dimension(r.width,h));
      Point p = parent.getLocationOnScreen();
      popup = PopupFactory.getSharedInstance().getPopup(parent,textView,p.x,p.y+r.height);
      popup.show();

    }

    this.visible = visible;

  }



  public void mouseDragged(MouseEvent e) {
  }

  public void mouseMoved(MouseEvent e) {
    selectedIdx = e.getY()/hFont;
    buildText();
  }

  public void mouseClicked(MouseEvent e) {
    selectedIdx = e.getY()/hFont;
    select();
  }

  public void mousePressed(MouseEvent e) {
  }

  public void mouseReleased(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }
}


public class JTextTips extends JTextField implements DocumentListener,KeyListener,FocusListener {

  TipPopup popup;
  boolean  isRefreshing;

  public JTextTips() {
    isRefreshing = false;
    popup = new TipPopup(this);
    getDocument().addDocumentListener(this);
    addKeyListener(this);
    addFocusListener(this);
  }

  public void setTextInternal(String text) {
    isRefreshing = true;
    setText(text);
    isRefreshing = false;
  }

  public void setTips(String[] list) {
    popup.setList(list);
  }

  public void insertUpdate(DocumentEvent e) {
    if(!isRefreshing) popup.updateText();
  }

  public void removeUpdate(DocumentEvent e) {
    if(!isRefreshing) popup.updateText();
  }

  public void changedUpdate(DocumentEvent e) {

  }

  public void keyTyped(KeyEvent e) {

  }

  public void keyPressed(KeyEvent e) {
    if(e.getKeyCode()==KeyEvent.VK_DOWN)
      popup.down();
    if(e.getKeyCode()==KeyEvent.VK_UP)
      popup.up();
    if(e.getKeyCode()==KeyEvent.VK_ENTER)
      popup.select();
  }

  public void keyReleased(KeyEvent e) {
  }

  public void focusGained(FocusEvent e) {
  }

  public void focusLost(FocusEvent e) {
    popup.setVisible(false);
  }
}
