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

  }

  public TreePath findText(String value,TangoNode root) {

    scanProgress = 0;
    searchStack.clear();
    searchStack.push(root);

    //System.out.println("TangoTreeNode::findText() Entering...");
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

    //System.out.println("TangoTreeNode::findText() Thread created...");

    searchResult = null;
    searchDlg = new ThreadDlg(parent,"Searching the database",false, doSearch);

    //System.out.println("TangoTreeNode::findText() Dialog created...");
    searchDlg.showDlg();

    // Wait for thread completion
    try { doSearch.join();}
    catch (InterruptedException e) {}
    doSearch=null;

    //System.out.println("TangoTreeNode::findText() Exiting...");
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

      if( searchPath ) {

        TreePath path = node.getCompletePath();
        String pathText = getPathAsText(path);
        //System.out.println("Looping..." + scanProgress + " " + searchText + " " + pathText);

        if (searchIgnoreCase)
          pathText = pathText.toLowerCase();

        if (searchUseRegexp) {
          found = (p.matcher(pathText).matches());
        } else {
          found = (pathText.indexOf(searchText) != -1);
        }

      }

      int count = node.getChildCount();
      for (i = 0;i<count;i++) searchStack.add((TangoNode) node.getChildAt(i));

    }

    if( found ) {
      return node.getCompletePath();
    } else {
      return null;
    }

  }


  private String getPathAsText(TreePath path) {

    StringBuffer str = new StringBuffer();
    if(path==null) {
      str.append("null");
    } else {
      for(int i=0;i<path.getPathCount();i++) {
        str.append(path.getPathComponent(i).toString());
        if(i!=path.getPathCount()-1) str.append("/");
      }
    }
    return str.toString();

  }

}
