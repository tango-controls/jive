package jive3;

import fr.esrf.Tango.DevFailed;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.*;
import java.awt.*;

import jive.JiveUtils;

/**
 * An abstract class for tree node.
 */
abstract class TangoNode extends DefaultMutableTreeNode {

  private boolean areChildrenDefined = false;
  TreePanel parentPanel = null;

  // Create node on the fly and return number of child
  public int getChildCount() {

    try {

      if(!areChildrenDefined) {
        areChildrenDefined = true;
        populateNode();
      }

    } catch (DevFailed e) {

      JiveUtils.showTangoError(e);

    }
    return super.getChildCount();

  }

  // Clear all child nodes
  public void clearNodes() {
     removeAllChildren();
     areChildrenDefined = false;
  }

  // Fill children list
  abstract void populateNode() throws DevFailed;

  // Returns node icon
  ImageIcon getIcon() {
    return null;
  }

  // Returns true if the node is a leaf, false otherwise
  public boolean isLeaf() {
    return false;
  }

  // Returns node value as String
  String getValue() {
    return "";
  }

  // Returns title as String
  String getTitle() {
    return "...";
  }

  // Returns the list of supported action
  int[] getAction() {
    return new int[0];
  }

  // Execute the given action
  void execAction(int actionNumber) {
    System.out.println("Warning, TangoNode.execAction("+actionNumber+") hasn't effect.");
  }

}

/**
 * Tango tree node renderer
 */
 class TangoNodeRenderer extends DefaultTreeCellRenderer {

   final static ImageIcon hosticon = new ImageIcon(TangoNodeRenderer.class.getResource("/jive/host_big.gif"));
   final static ImageIcon devicon = new ImageIcon(TangoNodeRenderer.class.getResource("/jive/device.gif"));
   final static ImageIcon deviconbig = new ImageIcon(TangoNodeRenderer.class.getResource("/jive/device_big.gif"));
   final static ImageIcon propiconbig = new ImageIcon(TangoNodeRenderer.class.getResource("/jive/property_big.gif"));
   final static ImageIcon aliasiconbig = new ImageIcon(TangoNodeRenderer.class.getResource("/jive/alias_big.gif"));
   final static ImageIcon srvicon = new ImageIcon(TangoNodeRenderer.class.getResource("/jive/server.gif"));
   final static ImageIcon srviconbig = new ImageIcon(TangoNodeRenderer.class.getResource("/jive/server_big.gif"));
   final static ImageIcon classicon = new ImageIcon(TangoNodeRenderer.class.getResource("/jive/class.gif"));
   final static ImageIcon uclassicon = new ImageIcon(TangoNodeRenderer.class.getResource("/jive/uclass.gif"));
   final static ImageIcon classiconbig = new ImageIcon(TangoNodeRenderer.class.getResource("/jive/class_big.gif"));
   final static ImageIcon leaficon = new ImageIcon(TangoNodeRenderer.class.getResource("/jive/leaf.gif"));
   final static ImageIcon uleaficon = new ImageIcon(TangoNodeRenderer.class.getResource("/jive/uleaf.gif"));
   final static ImageIcon leafcfgicon = new ImageIcon(TangoNodeRenderer.class.getResource("/jive/leafcfg.gif"));
   final static ImageIcon leaflogicon = new ImageIcon(TangoNodeRenderer.class.getResource("/jive/logleaf.gif"));
   final static ImageIcon cmdicon = new ImageIcon(TangoNodeRenderer.class.getResource("/jive/command.gif"));
   final static ImageIcon atticon = new ImageIcon(TangoNodeRenderer.class.getResource("/jive/attribute.gif"));
   final static ImageIcon attcfgicon = new ImageIcon(TangoNodeRenderer.class.getResource("/jive/attleaf.gif"));
   final static ImageIcon uattcfgicon = new ImageIcon(TangoNodeRenderer.class.getResource("/jive/uattleaf.gif"));
   final static ImageIcon hdbcfgicon = new ImageIcon(TangoNodeRenderer.class.getResource("/jive/hdbleaf.gif"));
   final static ImageIcon alarmicon = new ImageIcon(TangoNodeRenderer.class.getResource("/jive/alarms.gif"));
   final static ImageIcon eventicon = new ImageIcon(TangoNodeRenderer.class.getResource("/jive/event.gif"));


   public TangoNodeRenderer() {}

   public Component getTreeCellRendererComponent(
       JTree tree,
       Object value,
       boolean sel,
       boolean expanded,
       boolean leaf,
       int row,
       boolean hasFocus) {

     super.getTreeCellRendererComponent(
         tree, value, sel,
         expanded, leaf, row,
         hasFocus);

     TangoNode node = (TangoNode)value;
     ImageIcon icon = node.getIcon();
     if( icon!=null ) setIcon(icon);

     return this;
   }

 }
