/*
 * TangoTreeNodeCellRenderer.java
 *
 */
package jive;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

class TangoTreeNodeRenderer extends DefaultTreeCellRenderer {

  ImageIcon hosticon;
  ImageIcon devicon;
  ImageIcon deviconbig;
  ImageIcon propiconbig;
  ImageIcon aliasiconbig;
  ImageIcon srvicon;
  ImageIcon srviconbig;
  ImageIcon classicon;
  ImageIcon uclassicon;
  ImageIcon classiconbig;
  ImageIcon leaficon;
  ImageIcon uleaficon;
  ImageIcon leafcfgicon;
  ImageIcon leaflogicon;
  ImageIcon cmdicon;
  ImageIcon atticon;
  ImageIcon attcfgicon;
  ImageIcon uattcfgicon;
  ImageIcon hdbcfgicon;
  ImageIcon alarmicon;
  ImageIcon eventicon;
  Font bigFont;
  Font defFont;

  public TangoTreeNodeRenderer() {
    hosticon = new ImageIcon(getClass().getResource("/jive/host_big.gif"));
    devicon = new ImageIcon(getClass().getResource("/jive/device.gif"));
    deviconbig = new ImageIcon(getClass().getResource("/jive/device_big.gif"));
    propiconbig = new ImageIcon(getClass().getResource("/jive/property_big.gif"));
    aliasiconbig = new ImageIcon(getClass().getResource("/jive/alias_big.gif"));
    srvicon = new ImageIcon(getClass().getResource("/jive/server.gif"));
    srviconbig = new ImageIcon(getClass().getResource("/jive/server_big.gif"));
    classicon = new ImageIcon(getClass().getResource("/jive/class.gif"));
    uclassicon = new ImageIcon(getClass().getResource("/jive/uclass.gif"));
    classiconbig = new ImageIcon(getClass().getResource("/jive/class_big.gif"));
    leaficon = new ImageIcon(getClass().getResource("/jive/leaf.gif"));
    uleaficon = new ImageIcon(getClass().getResource("/jive/uleaf.gif"));
    leafcfgicon = new ImageIcon(getClass().getResource("/jive/leafcfg.gif"));
    leaflogicon = new ImageIcon(getClass().getResource("/jive/logleaf.gif"));
    cmdicon = new ImageIcon(getClass().getResource("/jive/command.gif"));
    atticon = new ImageIcon(getClass().getResource("/jive/attribute.gif"));
    attcfgicon = new ImageIcon(getClass().getResource("/jive/attleaf.gif"));
    uattcfgicon = new ImageIcon(getClass().getResource("/jive/uattleaf.gif"));
    hdbcfgicon = new ImageIcon(getClass().getResource("/jive/hdbleaf.gif"));
    alarmicon = new ImageIcon(getClass().getResource("/jive/alarms.gif"));
    eventicon = new ImageIcon(getClass().getResource("/jive/event.gif"));
    bigFont = new Font("Dialog",Font.BOLD,16);
    defFont = getFont();
  }

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

    TangoTreeNode n = (TangoTreeNode) value;
    setToolTipText(n.help);

    // Master nodes
    if(n.level<=1) {
      setFont(bigFont);
      if (n.level == 1) {
        switch (n.type) {
          case 1:
            setIcon(classiconbig);
            break;
          case 2:
            setIcon(srviconbig);
            break;
          case 3:
            setIcon(deviconbig);
            break;
          case 4:
            setIcon(propiconbig);
            break;
          case 5:
            setIcon(aliasiconbig);
            break;
        }
      } else {
        setIcon(hosticon);
      }
      return this;
    } else {
      setFont(defFont);
    }

    // Device Icon
    if ((n.level == 4) && (n.type == 3)) {
      setIcon(devicon);
      return this;
    }

    if ((n.level == 2) && (n.type == 5)) {
      setIcon(devicon);
      return this;
    }

    if ((n.level == 5) && (n.type == 2)) {
      setIcon(devicon);
      return this;
    }

    // Class Icon
    if ((n.level == 4) && (n.type == 2)) {
      if(n.isValid())
        setIcon(classicon);
      else
        setIcon(uclassicon);
      return this;
    }

    if ((n.level == 2) && (n.type == 1)) {
      setIcon(classicon);
      return this;
    }

    // Server icon
    if ((n.level == 2 || n.level == 3) && (n.type == 2)) {
      setIcon(srvicon);
      return this;
    }

    if ((n.level == 6) && (n.getParent().toString().equals("COMMAND"))) {
      setIcon(cmdicon);
      return this;
    }

    if ((n.level == 6) && (n.getParent().toString().equals("ATTRIBUTE"))) {
      setIcon(atticon);
      return this;
    }
    
    if (n.isAlarmCfgItem() && (n.level == 7) && (n.getParent().getParent().toString().equals("ATTRIBUTE"))) {
      setIcon(alarmicon);
      return this;
    }

    if (n.isEventCfgItem() && (n.level == 7) && (n.getParent().getParent().toString().equals("ATTRIBUTE"))) {
      setIcon(eventicon);
      return this;
    }

    // Special leaf (Configuration item)

    if (n.isLeaf()) {

      if (n.isPollCfgItem()) {
        setIcon(leafcfgicon);
        return this;
      }

      if (n.isLogCfgItem()) {
        setIcon(leaflogicon);
        return this;
      }

      if (n.isSystemItem()) {
        setIcon(uleaficon);
        return this;
      }

      boolean isAtt = ((n.level == 7) && n.getParent().getParent().toString().equals("ATTRIBUTE")) ||
                      ((n.level == 8) && n.getParent().getParent().getParent().toString().equals("ATTRIBUTE"));

      if (n.isAttCfgItem() && isAtt) {
        if (n.isValid()) {
          setIcon(attcfgicon);
        } else {
          setIcon(uattcfgicon);
        }
        return this;
      }

      setIcon(leaficon);
    }

    return this;
  }
}
