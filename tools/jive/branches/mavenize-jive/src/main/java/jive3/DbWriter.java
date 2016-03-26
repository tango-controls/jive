package jive3;

import fr.esrf.TangoApi.Database;
import fr.esrf.TangoApi.ApiUtil;
import fr.esrf.Tango.DevFailed;
import jive.TangoFileReader;
import jive.JiveUtils;

import javax.swing.*;
import java.io.File;

/**
 * A command line tool for updating the Tango database.
 */
public class DbWriter {

  String srvName;
  Database db;

  DbWriter(String fileName)  {

    try {
      String err = "";
      db = ApiUtil.get_db_obj();
      TangoFileReader fr = new TangoFileReader(db);
      err = fr.parse_res_file(fileName);
      if (err.length() > 0) System.out.println(err);
    } catch (DevFailed e) {
      JiveUtils.printTangoError(e);      
    }

  }

  public static void main(String[] args) {

    if (args.length != 1) {
      System.out.println("Usage: tg_update filename");
    } else {
      new DbWriter(args[0]);
    }

  }

}
