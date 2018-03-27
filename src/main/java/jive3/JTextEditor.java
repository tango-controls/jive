package jive3;

/*
 * JResEditor.java
 *
 * Simple multiline text editor. (Support styled and colored text)
 * JL Pons
 *
 */

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;




/**
 * Text Editor class
 */

public final class JTextEditor extends JComponent implements FocusListener,MouseListener,MouseMotionListener,KeyListener {


  /**
   * The editor content
   */

  final class EditorContent {

    private static final int SPARE_CAPACITY = 128;

    private char[] buffer;
    private int[] textInfo;
    private int used;

    public EditorContent() {

      buffer = new char[SPARE_CAPACITY];
      textInfo = new int[2*SPARE_CAPACITY];

    }

    public final int length() {

      return used;

    }

    public final int capacity() {

      return buffer.length;

    }

    public final int charAt(int index) {

      if (index >= 0 && index < length())
        return buffer[index];
      else
        return -1;

    }

    public final boolean isNewLine(int index) {

      return buffer[index]=='\n';

    }

    public String subString(int begin, int length) {

      return new String(buffer,begin,length);

    }

    public void setCharAt(int index, char c) {

      buffer[index] = c;

    }

    public void ensureCapacity(int minimumCapacity) {

      if (buffer.length < minimumCapacity) {
        int newCapacity = buffer.length * 2 + 2;
        if (newCapacity < minimumCapacity)
          newCapacity = minimumCapacity;
        char newBuffer[] = new char[newCapacity];
        int newTextInfo[] = new int[2*newCapacity];
        System.arraycopy(buffer, 0, newBuffer, 0, used);
        System.arraycopy(textInfo, 0, newTextInfo, 0, 2*used);
        buffer = newBuffer;
        textInfo = newTextInfo;
      }

    }

    public void setText(String s) {

      used = 0;
      append(s);

    }

    public EditorContent append(String s) {

      if (s == null)
        s = "null";
      int addedLength = s.length();
      int combinedLength = used + addedLength;
      ensureCapacity(combinedLength);
      s.getChars(0, addedLength, buffer, used);
      for(int i=0;i<addedLength;i++) {
        textInfo[2*(used+i)] = defaultColor.getRGB();
        textInfo[2*(used+i)+1] = 0;
      }
      used = combinedLength;

      return this;
    }


    public EditorContent append(char c) {

      if (used + 1 > buffer.length)
        ensureCapacity(used + 1);
      buffer[used] = c;
      textInfo[2*used] = defaultColor.getRGB();
      textInfo[2*used+1] = 0;
      used++;
      return this;

    }

    public final String toString() {
      return new String(buffer, 0, used);
    }

    public void clearStyleAndColor() {

      int rgb = defaultColor.getRGB();
      for(int i=0;i<used;i++) {
        textInfo[2*i] = rgb;
        textInfo[2*i+1] = 0;
      }

    }

    public void setForeground(Color c,int start,int lgth) {

      int stop = start+lgth;
      int length = length();
      if(stop>=length) stop = length;
      if(start>=length) start = length-1;
      int rgb = (c.getRed()&0xFF)<<16 |
          (c.getGreen()&0xFF)<<8 |
          c.getBlue();
      for(int i=start;i<stop;i++) {
        textInfo[2*i] = rgb;
      }

    }

    public int getForeground(int idx) {
      return textInfo[2*idx];
    }

    public void setStyle(int style,int start,int lgth) {

      int stop = start+lgth;
      int length = length();
      if(stop>=length) stop = length;
      if(start>=length) start = length-1;
      for(int i=start;i<stop;i++) {
        textInfo[2*i+1] = style;
      }

    }

    public int getStyle(int idx) {
      return textInfo[2*idx+1];
    }

    public void remove(int idx,int length) {

      int over = length() - (idx+length);
      System.arraycopy(buffer,idx+length,buffer,idx,over);
      System.arraycopy(textInfo,2*(idx+length),textInfo,2*idx,2*over);
      used-=length;

    }

    public void insert(char c,int idx) {

      ensureCapacity(used+1);
      if(idx>=length()) {
        append(c);
      } else {
        int over = length() - idx;
        System.arraycopy(buffer,idx,buffer,idx+1,over);
        System.arraycopy(textInfo,2*idx,textInfo,2*idx+2,2*over);
        buffer[idx] = c;
        textInfo[2*idx] = defaultColor.getRGB();
        textInfo[2*idx+1] = 0;
        used++;
      }

    }

    public void insert(String s,int idx) {

      ensureCapacity(used+s.length());
      if(idx>=length()) {
        append(s);
      } else {
        int over = length() - idx;
        System.arraycopy(buffer,idx,buffer,idx+s.length(),over);
        System.arraycopy(textInfo,2*idx,textInfo,2*(idx+s.length()),2*over);
        for(int i=0;i<s.length();i++) {
          buffer[idx+i] = s.charAt(i);
          textInfo[2*(idx+i)] = defaultColor.getRGB();
          textInfo[2*(idx+i)+1] = 0;
          used++;
        }
      }

    }

  }

  private final static int MAX_UNDO = 10;

  class UndoItem {
    String text;
    int cursorPos;
  }

  private Font plainFont = null;
  private Font boldFont = null;
  private Font italicFont = null;
  private Color defaultColor = new Color(80,80,80);
  private Color selBackColor = new Color(220,220,255);
  private Color curLineColor = new Color(255,255,200);

  private java.awt.datatransfer.Clipboard clipboard;

  private int charHeight;
  private int charDescent;
  private int charAscent;
  private int charLeading;
  private int charWidth;

  private EditorContent text;

  private int mX = 5;
  private int mY = 2;
  private int textCursorWidth;
  private boolean isDragging = false;
  private int cursorPos=0;
  private int lastCursorPos=0;
  private boolean cursorVisible=true;
  private int selStart = -1;
  private int selEnd = -1;
  private ArrayList<UndoItem> undoBuffer;
  private int undoPos = 0;
  private boolean isEditable;
  private ArrayList<ActionListener> docListeners;
  private Dimension lastSize = null;
  private JViewport parentViewport = null;

  /**
   * Construct a JTextEditor
   */
  public JTextEditor() {

    text = new EditorContent();
    initializeDefault();
    setOpaque(true);
    setToolTipText("");
    setCursor(new Cursor(Cursor.TEXT_CURSOR));
    textCursorWidth = 2;
    setToolTipText(null);
    clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    undoBuffer = new ArrayList<UndoItem>();
    isEditable = true;
    docListeners = new ArrayList<ActionListener>();

    setFocusable(true);
    addMouseListener(this);
    addMouseMotionListener(this);
    addKeyListener(this);
    addFocusListener(this);

  }

  /**
   * Sets the text
   * @param s Text
   */
  public void setText(String s) {

    text.setText(s);
    resetSelection();
    cursorPos = 0;
    undoBuffer.clear();
    undoPos = 0;
    updateScroll(true);

  }

  /**
   * Returns current text
   */
  public String getText() {
    return text.toString();
  }

  /**
   * Sets the foreground color of the specified area
   * @param c Foreground color
   * @param start Area start index
   * @param lgth Area length
   */
  public void setForeground(Color c,int start,int lgth) {
    text.setForeground(c,start,lgth);
  }

  /**
   * Sets the style color of the specified area
   * @param style Style (Font.PLAIN,Font.BOLD,Font.ITALIC)
   * @param start  Area start index
   * @param lgth Area length
   */
  public void setStyle(int style,int start,int lgth) {
    text.setStyle(style,start,lgth);
  }

  /**
   * Sets whether the text area is editable
   */
  public void setEditable(boolean editable) {
    isEditable = editable;
    repaint();
  }

  /**
   * Returns true if the text area is editable
   */
  public boolean isEditable() {
    return isEditable;
  }

  /**
   * Add documents listener
   */
  public void addActionListener(ActionListener l) {
    docListeners.add(l);
  }

  public void removeActionListener(ActionListener l) {
    docListeners.remove(l);
  }

  /**
   * Sets the default foreground color
   * @param f Foreground color
   */
  public void setDefaultForegroundColor(Color f) {
    defaultColor = f;
  }

  /**
   * Reset style and color to default
   */
  public void clearStyleAndColor() {
    text.clearStyleAndColor();
  }


  /**
   * Sets the scrollPane parent when the component is used inside a scroolPane
   * @param parent ScrollPane parent
   */
  public void setScrollPane(JScrollPane parent) {

    // Disable arrow key
    parent.getActionMap().put("unitScrollRight", new AbstractAction(){
      public void actionPerformed(ActionEvent e) {}});
    parent.getActionMap().put("unitScrollDown", new AbstractAction(){
      public void actionPerformed(ActionEvent e) {}});
    parent.getActionMap().put("unitScrollLeft", new AbstractAction(){
      public void actionPerformed(ActionEvent e) {}});
    parent.getActionMap().put("unitScrollUp", new AbstractAction(){
      public void actionPerformed(ActionEvent e) {}});

    // Disable PageUp/PageDown
    parent.getActionMap().put("scrollDown", new AbstractAction(){
      public void actionPerformed(ActionEvent e) {}});
    parent.getActionMap().put("scrollUp", new AbstractAction(){
      public void actionPerformed(ActionEvent e) {}});

    parentViewport = parent.getViewport();

  }


  public Dimension getPreferredSize() {

    int i = 0;
    int maxX = 0;
    int maxY = 0;
    int c = 0;
    while(i<text.length()) {
      if(text.isNewLine(i)) {
        if(c-1>maxX) maxX = c-1;
        maxY++;
        c = 0;
      }
      c++;
      i++;
    }
    if(c-1>maxX) maxX = c-1;

    return new Dimension( maxX*charWidth + 2*mX , (maxY+1)*charHeight + 2*mY );

  }

  public void paint(Graphics g) {

    int sX = mX;
    int sY = mY;
    Dimension d = getSize();
    g.setColor(getBackground());
    g.fillRect(0, 0, d.width, d.height);

    g.setColor(defaultColor);
    g.setFont(plainFont);
    //Graphics2D g2 = (Graphics2D)g;
    //g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
    //    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    Color gf = null;
    int sStart,sEnd;
    if(selStart>selEnd) {
      sStart = selEnd;
      sEnd = selStart;
    } else {
      sStart = selStart;
      sEnd = selEnd;
    }

    Point p = getPos(cursorPos);
    int curLine = 0;
    int curCol = 0;

    for(int i=0;i<text.length();i++) {

      if(curLine == p.y && curCol==0 && isEditable && hasFocus()) {
        g.setColor(curLineColor);
        g.fillRect(sX-1,sY-1,d.width,charHeight+2);
      }

      if(cursorPos==i && cursorVisible && isEditable && hasFocus()) {
        g.setColor(Color.BLACK);
        g.fillRect(sX-1,sY+1,2,charHeight-1);
      }

      int c = text.charAt(i);

      if (c != '\n') {

        if (i >= sStart && i < sEnd) {
          g.setColor(selBackColor);
          g.fillRect(sX, sY, charWidth, charHeight);
        }

        if( gf==null || gf.getRGB()!=text.getForeground(i) ) {
          gf = new Color(text.getForeground(i));
        }
        g.setColor(gf);

        switch (text.getStyle(i)) {
          case Font.PLAIN:
            g.setFont(plainFont);
            break;
          case Font.BOLD:
            g.setFont(boldFont);
            break;
          case Font.ITALIC:
            g.setFont(italicFont);
            break;
        }
      }

      if(c=='\n') {
        sX = mX;
        sY += charHeight;
        curLine++;
        curCol = 0;
      } else if (c==' ') {
        sX += charWidth;
        curCol ++;
      } else {
        g.drawString(String.valueOf((char)c), sX, sY + charAscent);
        sX += charWidth;
        curCol ++;
      }

    }

    if(cursorPos==text.length() && cursorVisible) {
      g.setColor(Color.BLACK);
      g.fillRect(sX-1,sY,2,charHeight);
    }

  }


  @Override
  public void mouseClicked(MouseEvent e) {

  }

  @Override
  public void mousePressed(MouseEvent e) {

    requestFocus();

    if(!isEditable)
      return;

    cursorPos = getCursorPos(e.getX(),e.getY());
    lastCursorPos = cursorPos;
    resetSelection();
    isDragging = true;
    repaint();

  }

  @Override
  public void mouseReleased(MouseEvent e) {

    if(!isEditable)
      return;

    isDragging = false;
    repaint();
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {

  }

  @Override
  public void mouseDragged(MouseEvent e) {

    if(!isEditable)
      return;

    if( isDragging ) {
      cursorPos = getCursorPos(e.getX(),e.getY());
      lastCursorPos = cursorPos;
      selEnd = cursorPos;
      repaint();
    }

  }

  @Override
  public void mouseMoved(MouseEvent e) {
  }

  public void keyTyped(KeyEvent e) {
  }

  public void keyPressed(KeyEvent e) {

    if(!isEditable)
      return;

    switch(e.getKeyCode()) {

      case KeyEvent.VK_PAGE_UP:
        int nbLine = getVisibleRect().height / charHeight;
        while( getUpPos()>=0 && nbLine>0) {
          cursorPos = getUpPos();
          nbLine--;
        }
        resetSelection();
        scrollToVisible();
        repaint();
        break;

      case KeyEvent.VK_PAGE_DOWN:
        nbLine = getVisibleRect().height / charHeight;
        while( nbLine>0) {
          cursorPos = getDownPos();
          nbLine--;
        }
        resetSelection();
        scrollToVisible();
        repaint();
        break;


      case KeyEvent.VK_RIGHT:
        cursorPos++;
        if(cursorPos>=text.length()) cursorPos = text.length();
        lastCursorPos = cursorPos;
        if(e.isShiftDown()) {
          selEnd = cursorPos;
        } else {
          resetSelection();
        }
        scrollToVisible();
        repaint();
        break;

      case KeyEvent.VK_LEFT:
        cursorPos--;
        if(cursorPos<0) cursorPos = 0;
        lastCursorPos = cursorPos;
        if(e.isShiftDown()) {
          selEnd = cursorPos;
        } else {
          resetSelection();
        }
        scrollToVisible();
        repaint();
        break;

      case KeyEvent.VK_UP:
        int s = getUpPos();
        if(s>=0) {
          cursorPos=s;
          if(e.isShiftDown()) {
            selEnd = cursorPos;
          } else {
            resetSelection();
          }
          scrollToVisible();
          repaint();
        }
        break;

      case KeyEvent.VK_DOWN:
        cursorPos = getDownPos();
        if(e.isShiftDown()) {
          selEnd = cursorPos;
        } else {
          resetSelection();
        }
        scrollToVisible();
        repaint();
        break;

      case KeyEvent.VK_BACK_SPACE:
        if( hasSelection() ) {
          modify();
          deleteSelection();
          fireUpdate();
        } else {
          if(cursorPos>0) {
            modify();
            cursorPos--;
            lastCursorPos = cursorPos;
            text.remove(cursorPos, 1);
            resetSelection();
            fireUpdate();
          }
        }
        repaint();
        scrollToVisible();
        break;

      case KeyEvent.VK_DELETE:
        if( hasSelection() ) {
          modify();
          deleteSelection();
          fireUpdate();
        } else {
          if(cursorPos<text.length()) {
            modify();
            text.remove(cursorPos,1);
            resetSelection();
            fireUpdate();
          }
        }
        repaint();
        scrollToVisible();
        break;

      case KeyEvent.VK_ENTER:
        modify();
        text.insert('\n', cursorPos);
        cursorPos++;
        resetSelection();
        fireUpdate();
        repaint();
        scrollToVisible();
        break;

      default:
        if(!e.isActionKey()) {

          char c = e.getKeyChar();
          if(c>=32 && c<=255) {

            // Insert printable char
            modify();
            deleteSelection();
            text.insert(c,cursorPos);
            cursorPos++;
            resetSelection();
            fireUpdate();
            scrollToVisible();
            repaint();

          } else {

            // CTRL+Key
            if( e.isControlDown() ) {

              switch (e.getKeyCode()) {

                case KeyEvent.VK_A:
                  selStart = 0;
                  selEnd = text.length();
                  repaint();
                  break;

                case KeyEvent.VK_C:
                  copy();
                  break;

                case KeyEvent.VK_V:
                  String str = getClipboardContent();
                  if( str!=null && str.length()>0 ) {
                    modify();
                    deleteSelection();
                    paste(str);
                    fireUpdate();
                    repaint();
                    scrollToVisible();
                  }
                  break;

                case KeyEvent.VK_X:
                  if( hasSelection() ) {
                    modify();
                    copy();
                    deleteSelection();
                    fireUpdate();
                    repaint();
                    scrollToVisible();
                  }
                  break;

                case KeyEvent.VK_Z:
                  if( !e.isShiftDown() ) {
                    // Undo
                    if( undoPos>0 ) {
                      if(undoPos==undoBuffer.size()) {
                        // We need to store present state
                        modify();
                        undoPos--;
                      }
                      undoPos--;
                      text.setText(undoBuffer.get(undoPos).text);
                      cursorPos = undoBuffer.get(undoPos).cursorPos;
                      fireUpdate();
                      repaint();
                      scrollToVisible();
                    }
                  } else {
                    // Redo
                    if( undoPos<undoBuffer.size()-1 ) {
                      undoPos++;
                      text.setText(undoBuffer.get(undoPos).text);
                      cursorPos = undoBuffer.get(undoPos).cursorPos;
                      fireUpdate();
                      repaint();
                      scrollToVisible();
                    }
                  }
                  break;

              }

            }

          }
        }
        break;

    }

  }

  public void keyReleased(KeyEvent e) {
  }

  @Override
  public void focusGained(FocusEvent e) {
    repaint();
  }

  @Override
  public void focusLost(FocusEvent e) {
    repaint();
  }
  // --------------------------------------------------------------------------------------------
  // Private stuff
  // --------------------------------------------------------------------------------------------
  private void fireUpdate() {

    for(int i=0;i<docListeners.size();i++) {
      ActionEvent e = new ActionEvent(this,i,"TextChanged");
      docListeners.get(i).actionPerformed(e);
    }
    updateScroll();

  }

  private void updateScroll() {
    updateScroll(false);
  }

  private void scrollToVisible() {

    if( parentViewport != null ) {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          Point p = getPos(cursorPos);
          Rectangle r = new Rectangle(p.x * charWidth, p.y * charHeight, charWidth + mX, charHeight + mY);
          scrollRectToVisible(r);
        }
      });
    }

  }

  private void updateScroll(boolean forceUpdate) {

    if( parentViewport != null ) {

      // We are in a scrollPane
      Dimension dim = getPreferredSize();

      if(forceUpdate) {
        lastSize = dim;
        parentViewport.revalidate();
      } else {
        if( lastSize==null || lastSize.getWidth() != dim.getWidth() || lastSize.getHeight() != dim.getHeight() ) {
          // Dimension changed
          lastSize = dim;
          parentViewport.revalidate();
        }
      }

    }

  }

  private void resetSelection() {
    selStart = cursorPos;
    selEnd = cursorPos;
  }

  private boolean hasSelection() {
    int length = Math.abs(selEnd - selStart);
    return length>0;
  }

  private boolean deleteSelection() {

    int length = Math.abs(selEnd - selStart);
    if(length>0) {
      int idx = Math.min(selStart,selEnd);
      text.remove(idx,length);
      cursorPos = idx;
      resetSelection();
      return true;
    }
    return false;

  }

  private void modify() {

    // Reset undo buffer
    int toRemove = undoBuffer.size() - undoPos;
    for(int i=0;i<toRemove;i++)
      undoBuffer.remove(undoBuffer.size()-1);

    // Keep MAX UNDO buffer
    if( undoBuffer.size() > MAX_UNDO ) {
      toRemove = undoBuffer.size() - MAX_UNDO;
      for(int i=0;i<toRemove;i++)
        undoBuffer.remove(0);
    }

    UndoItem it = new UndoItem();
    it.cursorPos = cursorPos;
    it.text = text.toString();
    undoBuffer.add(it);
    undoPos = undoBuffer.size();

  }

  private void copy() {

    int length = Math.abs(selEnd - selStart);
    if(length>0) {
      int idx = Math.min(selStart,selEnd);
      String str = text.subString(idx,length);
      StringSelection stringSelection = new StringSelection( str );
      clipboard.setContents( stringSelection, null );
    }

  }

  private String getClipboardContent() {

    String str = null;
    try {
      str = (String)(clipboard.getData(DataFlavor.stringFlavor));
    } catch (UnsupportedFlavorException e1) {
    } catch (IOException e2) {
    }

    return str;

  }

  private void paste(String str) {

    text.insert(str,cursorPos);
    cursorPos += str.length();
    resetSelection();

  }

  private int getCursorPos(int x,int y) {

    int xP = (x-mX) - charWidth/2 + textCursorWidth;
    int yP = (y-mY) / charHeight;

    int yl = 0;
    int i = 0;
    while(i<text.length() && yl<yP) {
      if(text.isNewLine(i)) {
        yl++;
      }
      i++;
    }

    int xl = 0;
    while(i<text.length() && xl<xP) {

      if(text.isNewLine(i)) {
        break;
      } else {
        xl+=charWidth;
      }
      i++;
    }

    return i;

  }

  private int getUpPos() {

    int s = getStartLine(cursorPos);
    if(s!=0) {
      int c = getColumn(lastCursorPos);
      int cp = s-1;
      int nc = getColumn(cp);
      while(nc>c) {
        nc--;
        cp--;
      }
      return cp;
    } else {
      return -1;
    }

  }

  private int getDownPos() {

    int cp = getNextLine(cursorPos);
    int c = getColumn(lastCursorPos);
    int nc = 0;
    while(nc<c && cp<text.length() && !text.isNewLine(cp)) {
      nc++;
      cp++;
    }
    return cp;

  }

  private int getNextLine(int pos) {

    int i = pos;
    while(i<text.length() && !text.isNewLine(i))
      i++;
    if(i<text.length())
      i++;
    return i;

  }

  private int getColumn(int pos) {

    int i = pos;
    int c = 0;
    while(i>0 && !text.isNewLine(i-1)) {
      i--;
      c++;
    }
    return c;

  }

  private int getStartLine(int pos) {

    int i = pos;
    while(i>0 && !text.isNewLine(i-1))
      i--;
    return i;

  }

  private Point getPos(int pos) {

    int i = 0;
    int l = 0;
    int c = 0;
    while(i<pos) {
      if(text.isNewLine(i)) {
        l++;
        c=0;
      } else {
        c++;
      }
      i++;
    }
    return new Point(c,l);

  }

  private void initializeDefault() {

    final String fontName = "MonoSpaced";
    final int fontSize = 12;

    plainFont = new Font(fontName, Font.PLAIN, fontSize);
    boldFont = new Font(fontName, Font.BOLD, fontSize);
    italicFont = new Font(fontName, Font.ITALIC, fontSize);

    FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(plainFont);

    final int plainAscent = fm.getAscent();
    final int plainDescent = fm.getDescent();
    final int plainLeading = fm.getLeading();

    charWidth = fm.charWidth('a');

    fm = Toolkit.getDefaultToolkit().getFontMetrics(boldFont);

    final int boldAscent = fm.getAscent();
    final int boldDescent = fm.getDescent();
    final int boldLeading = fm.getLeading();

    charAscent = plainAscent;
    if (boldAscent > charAscent)
      charAscent = boldAscent;

    charDescent = plainDescent;
    if (boldDescent > charDescent)
      charDescent = boldDescent;

    // Use no more than 1 pixel of leading.
    charLeading = (plainLeading > 0 || boldLeading > 0) ? 1 : 0;

    // Apply user-specified adjustments.
    final int adjustAscent = 0;
    final int adjustDescent = 0;
    final int adjustLeading = 0;

    if (charAscent + adjustAscent >= 0)
      charAscent += adjustAscent;
    if (charDescent + adjustDescent >= 0)
      charDescent += adjustDescent;
    if (charLeading + adjustLeading >= 0)
      charLeading += adjustLeading;

    charHeight = charAscent + charDescent + charLeading;

  }

}