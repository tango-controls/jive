package jive3;

/**
 * Navigation listener
 */
public interface NavigationListener {

  public void backAction(NavigationBar src);
  public void forwardAction(NavigationBar src);
  public void refreshAction(NavigationBar src);
  public void searchAction(NavigationBar src);

}
