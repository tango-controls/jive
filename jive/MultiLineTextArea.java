package jive;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Class used by the MultiLineCellEditor
 */
public class MultiLineTextArea extends JTextArea implements DocumentListener {

  boolean ignoreChange = true;
  int     rowEditing;
  int     columnEditing;
  MultiLineCellEditor  parent;

  MultiLineTextArea(MultiLineCellEditor parent) {
    this.parent = parent;
    getDocument().addDocumentListener(this);
    // This is a fix to Bug Id 4256006
    addAncestorListener(new AncestorListener() {
      public void ancestorAdded(AncestorEvent e) {
        requestFocus();
      }

      public void ancestorMoved(AncestorEvent e) {
      }

      public void ancestorRemoved(AncestorEvent e) {
      }
    });
  }

  public void updateField() {
    if (!ignoreChange) {
      parent.table.setValueAt(getText(), rowEditing, columnEditing);
      parent.updateRow(rowEditing);
    }
  }

  public void insertUpdate(DocumentEvent e) {
    updateField();
  }

  public void removeUpdate(DocumentEvent e) {
    updateField();
  }

  public void changedUpdate(DocumentEvent e) {
  }

}
