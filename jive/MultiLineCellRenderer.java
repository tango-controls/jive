package jive;

/**
 * <p>Copyright: Copyright (c) 2002</p>
 * @author Guillaume Barreau (guillaume@runtime-collective.com)
 * @version 1.0
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class MultiLineCellRenderer extends JTextArea implements TableCellRenderer {

  private final static Font  TABLE_FONT = new Font("Dialog",Font.PLAIN,12);
  private final static Font  TABLE_FONT_BOLD = new Font("Dialog",Font.BOLD,12);

  Color  selColor = new Color(200,200,255);
  Border selBorder = BorderFactory.createLineBorder(selColor);

  private boolean doBold = false;
  private boolean doSelection = false;

  public MultiLineCellRenderer() {
    setEditable(false);
    setLineWrap(false);
    setWrapStyleWord(false);
  }

  public MultiLineCellRenderer(boolean doBold,boolean doSelection) {
    setEditable(false);
    setLineWrap(false);
    setWrapStyleWord(false);
    this.doBold = doBold;
    this.doSelection = doSelection;
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
}

