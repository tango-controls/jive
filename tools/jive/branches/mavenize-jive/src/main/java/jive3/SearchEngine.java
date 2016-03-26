package jive3;

import jive.JiveUtils;
import jive.TangoTreeNode;
import jive.ThreadDlg;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.util.Stack;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Search the database
 */
public class SearchEngine {

  int               scanProgress;
  Stack<TangoNode>  searchStack = null;
  String            searchText;
  boolean           searchIgnoreCase;
  boolean           searchPath;
  boolean           searchProperty;
  boolean           searchPolling;
  boolean           searchAttConfig;
  boolean           searchEvent;
  boolean           searchAttProperty;
  boolean           searchUseRegexp;
  boolean           searchOnlyLeaf;
  TreePath          searchResult;
  ThreadDlg         searchDlg;
  JFrame            parent;
  TangoNode         focusedNode;

  SearchEngine(JFrame parent) {

    this.parent = parent;
    scanProgress = 0;
    searchStack = new Stack<TangoNode>();
    searchIgnoreCase = true;
    searchPath = true;
    searchProperty = false;
    searchPolling = false;
    searchAttConfig = false;
    searchEvent = false;
    searchAttProperty = false;
    searchUseRegexp = false;
    focusedNode = null;

  }

  public boolean isStackEmpty() {

    if( searchStack==null )
      return true;
    else
      return searchStack.empty();

  }

  public void resetSearch(TangoNode focusedNode) {

    this.focusedNode = focusedNode;
    searchStack.clear();

  }

  public void setSearchText(String searchText) {

    if(searchIgnoreCase)
      this.searchText=searchText.toLowerCase();
    else
      this.searchText=searchText;

  }

  public TreePath findText(String value,TangoNode root) {

    scanProgress = 0;
    searchStack.clear();
    searchStack.push(root);

    //System.out.println("SearchEngine::findText() Entering...");

    if(searchIgnoreCase)
      searchText=value.toLowerCase();
    else
      searchText=value;

    Thread doSearch = new Thread() {
      public void run() {
        //System.out.println("Starting thread.");
        searchResult = findTextTask();
        searchDlg.hideDlg();
        //System.out.println("Ending thread.");
      }
    };

    //System.out.println("SearchEngine::findText() Thread created...");

    searchResult = null;
    searchDlg = new ThreadDlg(parent,"Searching the database",false, doSearch);

    //System.out.println("SearchEngine::findText() Dialog created...");
    searchDlg.showDlg();

    // Wait for thread completion
    try { doSearch.join();}
    catch (InterruptedException e) {}
    doSearch=null;

    //System.out.println("SearchEngine::findText() Exiting...");
    return searchResult;
  }

  public TreePath findNext() {

    if( focusedNode!=null ) {
      // A fast search has been performed
      // We need to create a stack for the next action
      searchStack.clear();
      searchStack.push((TangoNode)focusedNode.getRoot());
      createStackTask();
      focusedNode = null;
    }

    Thread doSearch = new Thread() {
      public void run() {
        //System.out.println("Starting thread.");
        searchResult = findTextTask();
        searchDlg.hideDlg();
        //System.out.println("Ending thread.");
      }
    };

    //System.out.println("SearchEngine::findNext() Thread created...");

    searchResult = null;
    searchDlg = new ThreadDlg(parent,"Searching the database",false, doSearch);

    //System.out.println("SearchEngine::findNext() Dialog created...");
    searchDlg.showDlg();

    // Wait for thread completion
    try { doSearch.join();}
    catch (InterruptedException e) {}
    doSearch=null;

    //System.out.println("SearchEngine::findNext() Exiting...");
    return searchResult;

  }

  private TreePath findTextTask() {

    int       i;
    Pattern   p = null;
    TangoNode node = null;

    if (searchUseRegexp) {
      try {
        p = Pattern.compile(searchText);
      } catch (PatternSyntaxException e) {
        JiveUtils.showJiveError("Invalid regular expression\n" + e.getDescription());
        return null;
      }
    }

    boolean found = false;

    while (!searchStack.empty() && !ThreadDlg.stopflag && !found) {

      node = searchStack.get(0);
      searchStack.remove(0);

      scanProgress++;
      String pathText = null;

      if( searchPath ) {

        TreePath path = node.getCompletePath();
        pathText = JiveUtils.getPathAsText(path);
        //System.out.println("Looping..." + scanProgress + " " + searchText + " " + pathText);

        if (searchIgnoreCase)
          pathText = pathText.toLowerCase();

        if (searchUseRegexp) {
          found = (p.matcher(pathText).matches());
        } else {
          found = (pathText.indexOf(searchText) != -1);
        }

      }

      if( !found ) {
        int count = node.getChildCount();
        for (i = 0;i<count;i++) searchStack.add((TangoNode) node.getChildAt(i));
      }

    }

    if( found ) {
      return node.getCompletePath();
    } else {
      return null;
    }

  }

  private void createStackTask() {

    int       i;
    TangoNode node = null;
    boolean   found = false;

    while (!searchStack.empty() && !ThreadDlg.stopflag && !found) {

      node = searchStack.get(0);
      searchStack.remove(0);
      found = (node == focusedNode);

      if( !found ) {
        int count = node.getChildCount();
        for (i = 0;i<count;i++) searchStack.add((TangoNode) node.getChildAt(i));
      }

    }

  }

}
