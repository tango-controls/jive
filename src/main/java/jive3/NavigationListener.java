package jive3;

import javax.swing.tree.TreePath;

/**
 * Navigation listener
 */
public interface NavigationListener {

  public void backAction(NavigationBar src);
  public void forwardAction(NavigationBar src);
  public void refreshAction(NavigationBar src);
  public void searchAction(NavigationBar src,TreePath pathToSelect);
  public void nextOccAction(NavigationBar src);
  public void previousOccAction(NavigationBar src);

}
