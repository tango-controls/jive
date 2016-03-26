package jive;

/**
 * <p>Copyright: Copyright (c) 2002</p>
 * @author Guillaume Barreau (guillaume@runtime-collective.com)
 * @version 1.0
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Vector;
import java.util.regex.Pattern;

class DevItem {
  String str;
  int x;
  int y;
  int h;
  boolean isDevice;
}

public class MultiLineCellRenderer extends JTextArea implements TableCellRenderer {

  private final static Font  TABLE_FONT = new Font("Dialog",Font.PLAIN,12);
  private final static Font  TABLE_FONT_BOLD = new Font("Dialog",Font.BOLD,12);
  private static BufferedImage arrowButton = null;

  Color  selColor = new Color(200,200,255);
  Border selBorder = BorderFactory.createLineBorder(selColor);

  private boolean doBold = false;
  private boolean doSelection = false;
  private Vector items;
  private boolean hasDevice = false;
  private boolean doDevice = true;

  public MultiLineCellRenderer() {
    setLayout(null);
    setEditable(false);
    setLineWrap(false);
    setWrapStyleWord(false);
    items = new Vector();
    if( arrowButton==null ) {
      try {
        arrowButton = ImageIO.read(MultiLineCellRenderer.class.getResource("/jive/arrow_btn.gif"));
      } catch(IOException e) {
        System.out.println("Warning, /jive/arrow_btn.gif is missing");
      }
    }
  }

  public MultiLineCellRenderer(boolean doBold,boolean doSelection,boolean doDevice) {
    this();
    this.doBold = doBold;
    this.doSelection = doSelection;
    this.doDevice = doDevice;
  }

  public Component getTableCellRendererComponent(JTable table,Object value,
												boolean isSelected, boolean hasFocus, int row, int column) {

    boolean selectable = true;

    if (value instanceof String) {
      if( doBold ) {
        String str = (String)value;
        if( str.startsWith("/B") ) {
          setFont(TABLE_FONT_BOLD);
          setText(str.substring(2));
          selectable = false;
        } else {
          setFont(TABLE_FONT);
          setText(str);
        }
      } else {
        setText((String)value);
      }
      // set the table's row height, if necessary
      //updateRowHeight(row,getPreferredSize().height);
    }
    else
      setText("");

    if (doSelection && selectable) {

      int[] selRows = table.getSelectedRows();

      if (JiveUtils.contains(selRows, row) && column == 0)
        setBackground(selColor);
      else
        setBackground(Color.WHITE);

      if (isSelected && column == 1) {
        setBorder(selBorder);
      } else {
        setBorder(null);
      }

    } else {

      setBackground(Color.WHITE);
      setBorder(null);

    }

    return this;
  }

  public void setText(String text) {

    super.setText(text);
    if(!doDevice) return;

    items.clear();

    if(text.length()==0) {
      hasDevice =  false;
      return;
    }

    String[] lines = JiveUtils.makeStringArray(text);
    int y = 0;
    boolean hasDev = false;
    for(int i=0;i<lines.length;i++) {
      boolean isDev = JiveUtils.isDeviceName(lines[i]);
      Dimension d = ATKGraphicsUtils.measureString(lines[i],getFont());
      DevItem di = new DevItem();
      di.str = lines[i];
      di.x = 16;
      di.y = y+1;
      di.h = d.height-2;
      di.isDevice = isDev;
      items.add(di);
      y+=(d.height+1);
      if(isDev) hasDev = true;
    }

    hasDevice = hasDev;

  }

  public boolean hasDevice() {
    return hasDevice;
  }

  public String getDevice(int x,int y) {

    int i=0;
    boolean found = false;

    while(!found && i<items.size()) {
      DevItem di = (DevItem)items.get(i);
      found = (x<di.x-2) && (x>2) && (y>di.y+2) && (y<di.y+2+di.h);
      if(!found) i++;
    }

    if(found) {
      String devName = ((DevItem)items.get(i)).str;
      if(devName.startsWith("tango:"))
        return devName.substring(6);
      else
        return devName;
    } else {
      return null;
    }

  }

  public void paint(Graphics g) {

    if( !hasDevice ) {

      super.paint(g);

    } else {

      Graphics2D g2 = (Graphics2D)g;
      g2.setFont(getFont());
      g2.setColor(getForeground());
      g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
          RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
      for(int i=0;i<items.size();i++) {
        DevItem di = (DevItem)items.get(i);
        g2.drawString(di.str, di.x, di.y + di.h - 1);
        if(di.isDevice) g.drawImage(arrowButton, 2, di.y + 2, null);
      }

    }

  }

}

