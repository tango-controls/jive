package jive3;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.util.Vector;

/**
 * Navigation manager
 */

class NavItem {

  TreePath    path;
  JTree       selectedTree;

  public String toString() {

    StringBuffer str = new StringBuffer();

    str.append(selectedTree.getName());
    str.append(":");
    if(path==null) {
      str.append("null");
    } else {
      for(int i=0;i<path.getPathCount();i++) {
        str.append("[");
        str.append(path.getPathComponent(i).toString());
        str.append("]");
      }
    }
    return str.toString();

  }

};

public class NavManager {

  MainPanel invoker;
  Vector<NavItem> items;
  int position;

  public NavManager(MainPanel invoker) {

    this.invoker = invoker;
    items = new Vector<NavItem>();
    position = 0;

  }

  public void reset() {

    items = new Vector<NavItem>();
    position = 0;

  }

  public void recordPath(JTree selectedTree) {
    NavItem item = new NavItem();
    item.selectedTree = selectedTree;
    item.path = selectedTree.getSelectionPath();
    for(int i=position+1;i<items.size();i++)
      items.remove(i);
    items.add(item);
    position=items.size()-1;
    //debug();
  }

  public void goBack() {
    position--;
    //debug();
  }

  public void goForward() {
    position++;
    //debug();
  }

  public boolean canGoBackward() {
    return position>0;
  }

  public boolean canGoForward() {
    return position<items.size()-1;
  }

  public TreePath getCurrentPath() {
    return items.get(position).path;
  }

  public JTree getCurrentTree() {
    return items.get(position).selectedTree;
  }

  public void debug() {
    System.out.println("====================================");
    for(int i=0;i<items.size();i++) {
      if(position==i) {
        System.out.println("-->" + items.get(i));
      } else {
        System.out.println("   " + items.get(i));
      }
    }
  }



}
