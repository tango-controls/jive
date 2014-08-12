package jive3;

import jive.JiveUtils;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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

public class NavigationBar extends JPanel implements ActionListener,KeyListener {

  private final static Insets nullInsets = new Insets(0,0,0,0);
  private JButton backBtn;
  private JButton forwardBtn;
  private JComboBox searchText;
  private JButton searchBtn;
  private JButton refreshBtn;
  private Vector<NavigationListener> listeners;
  private JButton upBtn;
  private JButton downBtn;

  private boolean isUpdating;

  NavigationBar() {

    setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();

    backBtn = new JButton();
    backBtn.setDisabledIcon(new ImageIcon(NavigationBar.class.getResource("/jive/bw_btn_disa.gif")));
    backBtn.setPressedIcon(new ImageIcon(NavigationBar.class.getResource("/jive/bw_btn_pressed.gif")));
    backBtn.setIcon(new ImageIcon(NavigationBar.class.getResource("/jive/bw_btn.gif")));
    backBtn.setMargin(nullInsets);
    backBtn.addActionListener(this);
    backBtn.setToolTipText("Go back");
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
    forwardBtn.setToolTipText("Go forward");
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
    refreshBtn.setToolTipText("Refresh the tree");
    gbc.gridx = 2;
    gbc.gridy = 0;
    gbc.weightx = 0;
    gbc.fill = GridBagConstraints.VERTICAL;
    add(refreshBtn,gbc);

    isUpdating = true;
    searchText = new JComboBox();
    searchText.setEditable(true);
    searchText.getEditor().getEditorComponent().addKeyListener(this);
    searchText.addActionListener(this);
    gbc.gridx = 3;
    gbc.gridy = 0;
    gbc.weightx = 1;
    gbc.fill = GridBagConstraints.BOTH;
    add(searchText, gbc);
    isUpdating = false;

    downBtn = new JButton();
    downBtn.setPressedIcon(new ImageIcon(NavigationBar.class.getResource("/jive/down_btn_pressed.gif")));
    downBtn.setIcon(new ImageIcon(NavigationBar.class.getResource("/jive/down_btn.gif")));
    downBtn.setDisabledIcon(new ImageIcon(NavigationBar.class.getResource("/jive/down_btn_disa.gif")));
    downBtn.setMargin(nullInsets);
    downBtn.addActionListener(this);
    downBtn.setToolTipText("Next occurence");
    gbc.gridx = 4;
    gbc.gridy = 0;
    gbc.weightx = 0;
    gbc.fill = GridBagConstraints.VERTICAL;
    add(downBtn,gbc);

    //upBtn = new JButton();
    //upBtn.setPressedIcon(new ImageIcon(NavigationBar.class.getResource("/jive/up_btn_pressed.gif")));
    //upBtn.setIcon(new ImageIcon(NavigationBar.class.getResource("/jive/up_btn.gif")));
    //upBtn.setDisabledIcon(new ImageIcon(NavigationBar.class.getResource("/jive/up_btn_disa.gif")));
    //upBtn.setMargin(nullInsets);
    //upBtn.addActionListener(this);
    //upBtn.setToolTipText("Previous occurence");
    //gbc.gridx = 5;
    //gbc.gridy = 0;
    //gbc.weightx = 0;
    //gbc.fill = GridBagConstraints.VERTICAL;
    //add(upBtn,gbc);

    searchBtn = new JButton();
    searchBtn.setPressedIcon(new ImageIcon(NavigationBar.class.getResource("/jive/search_btn_pressed.gif")));
    searchBtn.setIcon(new ImageIcon(NavigationBar.class.getResource("/jive/search_btn.gif")));
    searchBtn.setMargin(nullInsets);
    searchBtn.addActionListener(this);
    searchBtn.setToolTipText("Search");
    gbc.gridx = 5;
    gbc.gridy = 0;
    gbc.weightx = 0;
    gbc.fill = GridBagConstraints.VERTICAL;
    add(searchBtn,gbc);

    listeners = new Vector<NavigationListener>();

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

  public void enableNextOcc(boolean enable) {
    downBtn.setEnabled(enable);
  }

  public void enablePreviousOcc(boolean enable) {
    //upBtn.setEnabled(enable);
  }

  public void actionPerformed(ActionEvent evt) {

    Object src = evt.getSource();

    if(src==backBtn) {
      for(int i=0;i<listeners.size();i++) listeners.get(i).backAction(this);
    } else if(src==forwardBtn) {
      for(int i=0;i<listeners.size();i++) listeners.get(i).forwardAction(this);
    } else if(src==refreshBtn) {
      for(int i=0;i<listeners.size();i++) listeners.get(i).refreshAction(this);
    } else if(src==downBtn) {
      for(int i=0;i<listeners.size();i++) listeners.get(i).nextOccAction(this);
    } else if(src==searchBtn) {
      TreePath path = getSelectedItemPath();
      for(int i=0;i<listeners.size();i++) listeners.get(i).searchAction(this,path);
      if(path==null) {
        isUpdating = true;
        addSearchText(getSearchText(),null);
        isUpdating = false;
      }
    } else if(src==searchText ) {
      if( !isUpdating ) {
        TreePath path = getSelectedItemPath();
        if(path!=null) {
          for(int i=0;i<listeners.size();i++) listeners.get(i).searchAction(this,path);
        }
      }
    }

  }

  public void keyTyped(KeyEvent e) {}
  public void keyPressed(KeyEvent e) {}
  public void keyReleased(KeyEvent e) {

    if(e.getKeyCode()==KeyEvent.VK_ENTER) {
      TreePath path = getSelectedItemPath();
      for(int i=0;i<listeners.size();i++) listeners.get(i).searchAction(this,path);
      isUpdating = true;
      addSearchText(getSearchText(),null);
      isUpdating = false;
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
