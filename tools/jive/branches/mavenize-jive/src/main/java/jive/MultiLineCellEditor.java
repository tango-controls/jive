package jive;

/* Class to handle multiline edition in a JTable
 * Modification by Jean-Luc PONS
 */

/*
* <p>Copyright: Copyright (c) 2002</p>
* @author Guillaume Barreau (guillaume@runtime-collective.com)
* @version 1.0
*
* Distributable under GPL license.
* See terms of license at gnu.org.
*/

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

public class MultiLineCellEditor extends AbstractCellEditor implements TableCellEditor {

  MultiLineTextArea textArea;
  JScrollPane textView;
  JTable table;

  public MultiLineCellEditor(JTable ta) {
    super();
    table = ta;
    // this component relies on having this renderer for the String class
    MultiLineCellRenderer renderer = new MultiLineCellRenderer();
    table.setDefaultRenderer(String.class, renderer);

    textArea = new MultiLineTextArea(this);
    textView = new JScrollPane(textArea);
    textView.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
    textView.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    textArea.setLineWrap(false);
    textArea.setWrapStyleWord(false);
    for (int i = 0; i < table.getRowCount(); i++) updateRow(i);
  }

  public MultiLineTextArea getTextArea() {
    return textArea;
  }

  // This method determines the height in pixel of a cell given the text it contains
  private int cellHeight(int row, int col) {
    if (row == table.getEditingRow() && col == table.getEditingColumn())
      return textArea.getPreferredSize().height+4;
    else
      return table.getDefaultRenderer(String.class).getTableCellRendererComponent(table,
              table.getModel().getValueAt(row, col), false, false, row, col).getPreferredSize().height + 1;
  }

  void updateRow(int row) {
    int maxHeight = 0;
    for (int j = 0; j < table.getColumnCount(); j++) {
      int ch;
      if ((ch = cellHeight(row, j)) > maxHeight) {
        maxHeight = ch;
      }
    }
    table.setRowHeight(row, maxHeight);
  }

  public void updateRows() {
    for (int i = 0; i < table.getRowCount(); i++) updateRow(i);
  }

  public Object getCellEditorValue() {
    return textArea.getText();
  }

  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                                               int row, int column) {
    textArea.rowEditing = row;
    textArea.columnEditing = column;
    textArea.setEditable(column != 0);
    textArea.ignoreChange = true;
    textArea.setText(table.getValueAt(row, column).toString());
    textArea.ignoreChange = false;
    // Update size
    table.setRowHeight(row, textArea.getPreferredSize().height+4);
    return textView;
  }

}
