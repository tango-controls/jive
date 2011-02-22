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
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;

public class MultiLineCellEditor extends AbstractCellEditor implements TableCellEditor {

	MyTextArea textArea;
	JTable table;

	public MultiLineCellEditor(JTable ta) {
	  super();
	  table = ta;
	  // this component relies on having this renderer for the String class
	  MultiLineCellRenderer renderer = new MultiLineCellRenderer();
	  table.setDefaultRenderer(String.class,renderer);

	  textArea = new MyTextArea();
	  textArea.setLineWrap(false);
    	  textArea.setWrapStyleWord(false);
	  for(int i=0;i<table.getRowCount();i++) updateRow(i);
	}

	// This method determines the height in pixel of a cell given the text it contains
	private int cellHeight(int row,int col) {
	  if (row == table.getEditingRow() && col == table.getEditingColumn())
	    return textArea.getPreferredSize().height;
	  else
	    return table.getDefaultRenderer(String.class).getTableCellRendererComponent(table,
		   table.getModel().getValueAt(row,col),false,false,row,col).getPreferredSize().height;
	}

	void updateRow(int row) {
		int maxHeight = 0;
		for(int j=0;j<table.getColumnCount();j++) {
			int ch;
			if ((ch = cellHeight(row,j)) > maxHeight) {
			  maxHeight = ch;
			}
		}
		table.setRowHeight(row,maxHeight);
	}

	public Object getCellEditorValue() {
		return textArea.getText();
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
						int row, int column) {
	  textArea.rowEditing = row;
	  textArea.columnEditing = column;
	  textArea.setEditable( column!=0 ); 
	  textArea.ingoreChange = true;
	  textArea.setText(table.getValueAt(row,column).toString());
	  textArea.ingoreChange = false;
	  
	  // Column 0 not editable
	  return textArea;
	}

	class MyTextArea extends JTextArea implements DocumentListener {
	
		boolean ingoreChange = true;
		int     rowEditing;
		int     columnEditing;

		MyTextArea() {
		  getDocument().addDocumentListener(this);
		  // This is a fix to Bug Id 4256006
		  addAncestorListener( new AncestorListener(){
		    public void ancestorAdded(AncestorEvent e){
		     requestFocus();
		    }
		    public void ancestorMoved(AncestorEvent e){}
		    public void ancestorRemoved(AncestorEvent e){}
		  });
		}
		
		public void updateField() {
		  if( !ingoreChange ) {
		    table.setValueAt(getText(),rowEditing,columnEditing);
		    updateRow(rowEditing);
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
}
