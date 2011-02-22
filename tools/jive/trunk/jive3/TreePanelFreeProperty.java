package jive3;

import fr.esrf.Tango.DevFailed;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


/**
 * A panel for selecting tango free property
 */
public class TreePanelFreeProperty extends TreePanel {

  // Filtering stuff
  String  propertyFilterString="*";
  Pattern propertyPattern=null;

  public TreePanelFreeProperty(MainPanel parent) {

    this.invoker = parent;
    this.self = this;
    setLayout(new BorderLayout());

  }

  public TangoNode createRoot() {
    return new RootNode();
  }

  void addProperty(String objectName) {
    if(searchNode(root,objectName)!=null) {
      JOptionPane.showMessageDialog(null,"Property already exists","Error",JOptionPane.ERROR_MESSAGE);
      return;
    }
    TreePath pth = new TreePath(root);
    TaskFreePropertyNode node = new TaskFreePropertyNode(self,db,objectName);
    treeModel.insertNodeInto(node, root, 0);
    pth = pth.pathByAddingChild(node);
    tree.setSelectionPath(pth);
    tree.makeVisible(pth);
    tree.scrollPathToVisible(pth);    
  }

  public void applyFilter(String filter) {

    propertyFilterString = filter;

    if( filter.equals("*") ) {
      propertyPattern = null;
    } else if (filter.length()==0) {
      propertyPattern = null;
    } else {
      try {
        String f = filterToRegExp(propertyFilterString);
        propertyPattern = Pattern.compile(f);
      } catch (PatternSyntaxException e) {
        JOptionPane.showMessageDialog(invoker,e.getMessage());
      }
    }

  }

  public String getFilter() {
    return propertyFilterString;
  }


  // ---------------------------------------------------------------

  class RootNode extends TangoNode {

    void populateNode() throws DevFailed {
      String[] list = db.get_object_list("*");
      for (int i = 0; i < list.length; i++) {
        if( propertyPattern!=null ) {
          Matcher matcher =  propertyPattern.matcher(list[i].toLowerCase());
          if( matcher.find() && matcher.start()==0 && matcher.end()==list[i].length() ) {
            add(new TaskFreePropertyNode(self,db,list[i]));
          }
        } else {
          add(new TaskFreePropertyNode(self,db,list[i]));
        }
      }
    }

    public String toString() {
      return "Property: ";
    }

  }

}
