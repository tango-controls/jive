package jive3;

import fr.esrf.Tango.DevFailed;

import javax.swing.*;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import jive.JiveUtils;


/**
 * A panel for selecting tango classes
 */
public class TreePanelClass extends TreePanel {

  // Filtering stuff
  String  classFilterString="*";
  Pattern classPattern=null;

  public TreePanelClass(MainPanel parent) {

    this.invoker = parent;
    this.self = this;
    setLayout(new BorderLayout());

  }

  public TangoNode createRoot() {
    return new RootNode();
  }

  void addAttribute(TangoNode attNode,String className,String attName) {
    if(searchNode(attNode,attName)!=null) {
      JOptionPane.showMessageDialog(null,"Attribute already exists","Error",JOptionPane.ERROR_MESSAGE);
      return;
    }
    treeModel.insertNodeInto(new TaskClassAttributePropertyNode(self,db,className,attName), attNode, 0);
  }

  public void applyFilter(String filter) {

    classFilterString = filter;

    if( filter.equals("*") ) {
      classPattern = null;
    } else if (filter.length()==0) {
      classPattern = null;
    } else {
      try {
        String f = filterToRegExp(classFilterString);
        classPattern = Pattern.compile(f);
      } catch (PatternSyntaxException e) {
        JOptionPane.showMessageDialog(invoker,e.getMessage());
      }
    }

  }

  public String getFilter() {
    return classFilterString;
  }


  // ---------------------------------------------------------------

  class RootNode extends TangoNode {

    void populateNode() throws DevFailed {
      String[] list = db.get_class_list("*");
      for (int i = 0; i < list.length; i++) {
        if( classPattern!=null ) {
          Matcher matcher =  classPattern.matcher(list[i].toLowerCase());
          if( matcher.find() && matcher.start()==0 && matcher.end()==list[i].length() ) {
            add(new ClassNode(list[i]));
          }
        } else {
          add(new ClassNode(list[i]));
        }
      }
    }

    public String toString() {
      return "Class: ";
    }

  }

  // ---------------------------------------------------------------

  class ClassNode extends TangoNode {

    private String className;

    ClassNode(String className) {
      this.className = className;
    }

    void populateNode() throws DevFailed {
      add(new TaskClassPropertyNode(self,db,className));
      add(new AttributeNode(className));
    }

    public String toString() {
      return className;
    }

  }

  // ---------------------------------------------------------------

  class AttributeNode extends TangoNode {

    private String className;

    AttributeNode(String className) {
      this.className = className;
    }

    void populateNode() throws DevFailed {
      String[] list = db.get_class_attribute_list(getParent().toString(), "*");
      for(int i=0;i<list.length;i++)
        add(new TaskClassAttributePropertyNode(self,db,className,list[i]));
    }

    public String toString() {
      return "Attribute properties";
    }

    public ImageIcon getIcon() {
      return TangoNodeRenderer.atticon;
    }

    public int[] getAction() {
      if(JiveUtils.readOnly)
        return new int[0];
      else
        return new int[] {ACTION_ADDCLASSATT};
    }

    public void execAction(int action) {
      switch(action) {
        case ACTION_ADDCLASSATT:
          String newName = JOptionPane.showInputDialog(null,"Add class attribute","");
          if(newName==null) return;
          addAttribute(this,className,newName);
          break;
      }
    }

  }

}
