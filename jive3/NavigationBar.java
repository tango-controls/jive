package jive3;

import jive.JiveUtils;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * Navigation bar
 */

class ComboItem {

  String text;
  TreePath path;

  public String toString() {
    return text;
  }

}

public class NavigationBar extends JPanel implements ActionListener {

  private final static Insets nullInsets = new Insets(0,0,0,0);
  private JButton backBtn;
  private JButton forwardBtn;
  private JComboBox searchText;
  private JButton searchBtn;
  private JButton refreshBtn;
  private Vector<NavigationListener> listeners;
  private boolean isUpdating;

  NavigationBar() {

    isUpdating = true;

    setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();

    backBtn = new JButton();
    backBtn.setDisabledIcon(new ImageIcon(NavigationBar.class.getResource("/jive/bw_btn_disa.gif")));
    backBtn.setPressedIcon(new ImageIcon(NavigationBar.class.getResource("/jive/bw_btn_pressed.gif")));
    backBtn.setIcon(new ImageIcon(NavigationBar.class.getResource("/jive/bw_btn.gif")));
    backBtn.setMargin(nullInsets);
    backBtn.addActionListener(this);
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 0;
    gbc.fill = GridBagConstraints.VERTICAL;
    add(backBtn,gbc);

    forwardBtn = new JButton();
    forwardBtn.setDisabledIcon(new ImageIcon(NavigationBar.class.getResource("/jive/fw_btn_disa.gif")));
    forwardBtn.setPressedIcon(new ImageIcon(NavigationBar.class.getResource("/jive/fw_btn_pressed.gif")));
    forwardBtn.setIcon(new ImageIcon(NavigationBar.class.getResource("/jive/fw_btn.gif")));
    forwardBtn.setMargin(nullInsets);
    forwardBtn.addActionListener(this);
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.weightx = 0;
    gbc.fill = GridBagConstraints.VERTICAL;
    add(forwardBtn,gbc);

    refreshBtn = new JButton();
    refreshBtn.setPressedIcon(new ImageIcon(NavigationBar.class.getResource("/jive/refresh_btn_pressed.gif")));
    refreshBtn.setIcon(new ImageIcon(NavigationBar.class.getResource("/jive/refresh_btn.gif")));
    refreshBtn.setMargin(nullInsets);
    refreshBtn.addActionListener(this);
    gbc.gridx = 2;
    gbc.gridy = 0;
    gbc.weightx = 0;
    gbc.fill = GridBagConstraints.VERTICAL;
    add(refreshBtn,gbc);

    searchText = new JComboBox();
    searchText.setEditable(true);
    searchText.addActionListener(this);
    gbc.gridx = 3;
    gbc.gridy = 0;
    gbc.weightx = 1;
    gbc.fill = GridBagConstraints.BOTH;
    add(searchText, gbc);

    searchBtn = new JButton();
    searchBtn.setPressedIcon(new ImageIcon(NavigationBar.class.getResource("/jive/search_btn_pressed.gif")));
    searchBtn.setIcon(new ImageIcon(NavigationBar.class.getResource("/jive/search_btn.gif")));
    searchBtn.setMargin(nullInsets);
    searchBtn.addActionListener(this);
    gbc.gridx = 4;
    gbc.gridy = 0;
    gbc.weightx = 0;
    gbc.fill = GridBagConstraints.VERTICAL;
    add(searchBtn,gbc);

    listeners = new Vector<NavigationListener>();
    isUpdating = false;
  }

  public void addNavigationListener(NavigationListener l) {
    listeners.add(l);
  }

  public void removeNavigationListener(NavigationListener l) {
    listeners.remove(l);
  }

  public String getSearchText() {
    return searchText.getEditor().getItem().toString();
  }

  public TreePath getSelectedItemPath() {

    Object selectedItem = searchText.getSelectedItem();
    if( selectedItem instanceof ComboItem ) {
      ComboItem item = (ComboItem)selectedItem;
      return item.path;
    }

    return null;

  }

  public void addLink(TreePath path) {
    isUpdating = true;
    int idx = addSearchText(JiveUtils.getPathAsText(path),path);
    searchText.setSelectedIndex(idx);
    isUpdating = false;
  }

  public void enableBack(boolean enable) {
    backBtn.setEnabled(enable);
  }

  public void enableForward(boolean enable) {
    forwardBtn.setEnabled(enable);
  }

  public void actionPerformed(ActionEvent evt) {

    Object src = evt.getSource();

    if(src==backBtn) {
      for(int i=0;i<listeners.size();i++) listeners.get(i).backAction(this);
    } else if(src==forwardBtn) {
      for(int i=0;i<listeners.size();i++) listeners.get(i).forwardAction(this);
    } else if(src==refreshBtn) {
      for(int i=0;i<listeners.size();i++) listeners.get(i).refreshAction(this);
    } else if(src==searchBtn) {
      TreePath path = getSelectedItemPath();
      for(int i=0;i<listeners.size();i++) listeners.get(i).searchAction(this,path);
      if(path==null) {
        isUpdating = true;
        addSearchText(getSearchText(),null);
        isUpdating = false;
      }
    } else if(src==searchText) {
      if(!isUpdating) {
        TreePath path = getSelectedItemPath();
        for(int i=0;i<listeners.size();i++) listeners.get(i).searchAction(this,path);
        if(path==null) {
          isUpdating = true;
          addSearchText(getSearchText(),null);
          isUpdating = false;
        }
      }
    }

  }

  private int addSearchText(String text,TreePath path) {

    // Add the input string if not already done
    boolean found = false;
    boolean alreadyThere = false;
    int i = 0;
    while(i<searchText.getItemCount() && !found) {
      int cmp = text.compareToIgnoreCase(searchText.getItemAt(i).toString());
      alreadyThere = cmp==0;
      found = cmp<=0;
      if(!found) i++;
    }

    ComboItem item = new ComboItem();
    item.text = text;
    item.path = path;
    if(!alreadyThere) searchText.insertItemAt(item, i);

    return i;

  }

}
