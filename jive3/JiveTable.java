package jive3;

import jive.JiveUtils;
import jive.MultiLineCellEditor;
import jive.MultiLineCellRenderer;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/* A specific table for Jive */
public class JiveTable extends JTable {

  private MultiLineCellEditor editor;
  private MainPanel parent = null;

  public JiveTable(TableModel model) {

    super(model);
    editor = new MultiLineCellEditor(this);
    setDefaultEditor(String.class, editor);

    MultiLineCellRenderer renderer = new MultiLineCellRenderer(false,true,true);
    setDefaultRenderer(String.class, renderer);

  }

  public void setParent(MainPanel parent) {
    this.parent = parent;
  }

  protected void processEvent(AWTEvent e) {

    if( e instanceof MouseEvent ) {
      MouseEvent me = (MouseEvent)e;
      if(me.getButton()==1 && me.getID()==MouseEvent.MOUSE_PRESSED) {
        int column = getColumnForLocation(me.getX());
        if( column==1 ) {
          int row = getRowForLocation(me.getY());
          String value = (String)getModel().getValueAt(row,column);
          MultiLineCellRenderer c = (MultiLineCellRenderer)getCellRenderer(row, column);
          c.setText(value);
          if( c.hasDevice() ) {
            Rectangle rect = getCellRect(row,column,false);
            int x = me.getX() - rect.x;
            int y = me.getY() - rect.y;
            String name = c.getDevice(x,y);
            if( name!=null && parent!=null ) {
              // Go to device node
              parent.goToDeviceNode(name);
              return;
            }
          }
        }
      }
    }
    super.processEvent(e);
  }

  public MultiLineCellEditor getEditor() {
    return editor;
  }

  public void updateRows() {
    editor.updateRows();
  }

  public int getRowForLocation(int y) {

    boolean found = false;
    int i = 0;
    int h = 0;

    while(i<getModel().getRowCount() && !found) {
      found = (y>=h && y<=h+getRowHeight(i));
      if(!found) {
        h+=getRowHeight(i);
        i++;
      }
    }

    if(found) {
      return i;
    } else {
      return -1;
    }

  }

  public int getColumnForLocation(int x) {

    boolean found = false;
    int i = 0;
    int w = 0;

    while(i<getModel().getColumnCount() && !found) {
      int cWidth = getColumnModel().getColumn(i).getWidth();
      found = (x>=w && x<=w+cWidth);
      if(!found) {
        w+=cWidth;
        i++;
      }
    }

    if(found) {
      return i;
    } else {
      return -1;
    }

  }

}
