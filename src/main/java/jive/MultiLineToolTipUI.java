/*
 * Originally written by Albert L. Ting and released into the public domain.
 *
 * Author: Albert L. Ting <alt@artisan.com>
 *
 * $Revision$
 * $Id$
 *
 */

package jive;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.ToolTipUI;
import java.awt.*;
import java.awt.event.InputEvent;

/**
 * To add multiline tooltip support to your swing applications, just add this
 * static call to your main method.  Note, you only need to do this once, even
 * if you change LookAndFeel as the UIManager knows not to overwrite the user
 * defaults.  Moreover, it uses the current L&F foreground/background colors
 *
 *        MultiLineToolTipUI.initialize();
 */
public class MultiLineToolTipUI extends ToolTipUI {
  static MultiLineToolTipUI singleton = new MultiLineToolTipUI();
  private static int inset = 3;
  private static int accelerator_offset = 15;

  private MultiLineToolTipUI() {
  }

  public static void initialize() {
    // don't hardcode the class name, fetch it dynamically.  This way we can
    // obfuscate.
    String key = "ToolTipUI";
    Class cls = singleton.getClass();
    String name = cls.getName();
    UIManager.put(key, name);
    UIManager.put(name, cls);	// needed for 1.2
  }

  public static ComponentUI createUI(JComponent c) {
    return singleton;
  }

  public void installUI(JComponent c) {
    installDefaults(c);
  }

  public void uninstallUI(JComponent c) {
    uninstallDefaults(c);
  }

  protected void installDefaults(JComponent c) {
    LookAndFeel.installColorsAndFont(c,
        "ToolTip.background",
        "ToolTip.foreground",
        "ToolTip.font");
    LookAndFeel.installBorder(c, "ToolTip.border");
  }

  protected void uninstallDefaults(JComponent c) {
    LookAndFeel.uninstallBorder(c);
  }

  public void paint(Graphics g, JComponent c) {
    Font font = c.getFont();
     FontMetrics fontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(font);

    Dimension dimension = c.getSize();
    int fontHeight = fontMetrics.getHeight();
    int fontAscent = fontMetrics.getAscent();
    String tipText = ((JToolTip) c).getTipText();
    if (tipText == null) return;
    String lines[] = tipText.split("\n");
    int num_lines = lines.length;
    int height;
    int i;

    g.setColor(c.getBackground());
    g.fillRect(0, 0, dimension.width, dimension.height);
    g.setColor(c.getForeground());
    for (i = 0, height = 2 + fontAscent; i < num_lines; i++, height += fontHeight) {
      g.drawString(lines[i], inset, height);
      if (i == num_lines - 1) {
        String keyText = getAcceleratorString((JToolTip) c);
        if (!keyText.equals("")) {
          Font small = new Font(font.getName(),
              font.getStyle(),
              font.getSize() - 2);
          g.setFont(small);
          g.drawString(keyText,
              fontMetrics.stringWidth(lines[i]) + accelerator_offset,
              height);
        }
      }
    }
  }

  public Dimension getPreferredSize(JComponent c) {
    Font font = c.getFont();
    FontMetrics fontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(font);
    int fontHeight = fontMetrics.getHeight();
    String tipText = ((JToolTip) c).getTipText();

    // bug 4094153 indicates string could be null
    if (tipText == null) return new Dimension(6, 6);

    String lines[] = tipText.split("\n");
    int num_lines = lines.length;
    Dimension dimension;
    int width,height,onewidth;

    height = num_lines * fontHeight;
    width = 0;
    for (int i = 0; i < num_lines; i++) {
      onewidth = fontMetrics.stringWidth(lines[i]);
      if (i == num_lines - 1) {
        String keyText = getAcceleratorString((JToolTip) c);
        onewidth += fontMetrics.stringWidth(keyText) + accelerator_offset;
      }
      width = Math.max(width, onewidth);
    }
    return new Dimension(width + inset * 2, height + inset * 2);
  }

  public Dimension getMinimumSize(JComponent c) {
    return getPreferredSize(c);
  }

  public Dimension getMaximumSize(JComponent c) {
    return getPreferredSize(c);
  }

  public String getAcceleratorString(JToolTip tip) {
    JComponent comp = tip.getComponent();
    if (comp == null) {
      return "";
    }
    KeyStroke[] keys = comp.getRegisteredKeyStrokes();
    String controlKeyStr = "";

    for (int i = 0; i < keys.length; i++) {

      char c = (char) keys[i].getKeyCode();
      int mod = keys[i].getModifiers();
      if (mod == InputEvent.CTRL_MASK) {
        controlKeyStr = "cntl+" + (char) keys[i].getKeyCode();
        break;
      } else if (mod == InputEvent.ALT_MASK) {
        controlKeyStr = "alt+" + (char) keys[i].getKeyCode();
        break;
      }
    }
    return controlKeyStr;
  }
}
