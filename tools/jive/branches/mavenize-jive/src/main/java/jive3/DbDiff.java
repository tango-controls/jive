package jive3;

import fr.esrf.TangoApi.Database;
import fr.esrf.TangoApi.ApiUtil;
import fr.esrf.Tango.DevFailed;
import jive.JiveUtils;
import jive.TangoFileReader;

import java.util.Vector;

/**
 * A command line tool for making diff between database and file
 */
public class DbDiff {

  Database db;

  private void printDiff(Vector diff) {

    for(int i=0;i<diff.size();i+=3) {
      System.out.println("Name: " + diff.get(i) + " -------------");
      System.out.println("--> DB:");
      System.out.println(diff.get(i+1));
      System.out.println("--> File:");
      System.out.println(diff.get(i+2));
    }

  }

  DbDiff(String fileName,boolean showDiff)  {

    try {
      String err = "";
      db = ApiUtil.get_db_obj();
    } catch (DevFailed e) {
      JiveUtils.printTangoError(e);      
    }

    String err = "";
    TangoFileReader fr = new TangoFileReader(db);

    Vector diff = new Vector();
    err = fr.check_res_file(fileName,diff);

    if (err.length() > 0) {
      System.out.println(fileName + ":" + err);
    } else {
      if( diff.size()>0 ) {
        // Differ
        System.out.println(fileName + " : differs");
        if(showDiff) printDiff(diff);
      } else {
        // Match
        System.out.println(fileName + " : matches");
      }
    }

  }

  public static void main(String[] args) {

    if(args.length==1) {
      new DbDiff(args[0],false);
    } else if (args.length==2) {
      if( args[1].equalsIgnoreCase("-v") ) {
        new DbDiff(args[0],true);
      } else {
        System.out.println("Usage: tg_diff filename [-v]");
      }
    } else {
      System.out.println("Usage: tg_diff filename [-v]");
    }

  }

}
