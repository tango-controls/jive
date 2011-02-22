package jive3;

import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.*;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.FileWriter;
import java.io.IOException;

import jive.JiveUtils;

class DbReader {

  String srvName;
  Database db;

  DbReader(String srvName)  {

    int i,j,k,l;
    this.srvName = srvName;

    boolean prtOut;

    try {

      db = ApiUtil.get_db_obj();
      DbServer dbs = new DbServer(srvName);
      String[] class_list = dbs.get_class_list();

      for (i = 0; i < class_list.length; i++) {

        String[] prop_list;
        String[] att_list;
        DbAttribute lst[];

        // We save class properties only once
        if( !JiveUtils.isSavedClass(class_list[i]) ) {

          System.out.print("#---------------------------------------------------------\n");
          System.out.print("# CLASS " + class_list[i] + " properties\n");
          System.out.print("#---------------------------------------------------------\n\n");

          prop_list = db.get_class_property_list(class_list[i], "*");
          for (j = 0; j < prop_list.length; j++) {
            String[] value = db.get_class_property(class_list[i], prop_list[j]).extractStringArray();
            if (prop_list[j].indexOf(' ') != -1) prop_list[j] = "\"" + prop_list[j] + "\"";
            JiveUtils.printFormatedRes("CLASS/" + class_list[i] + "->" + prop_list[j] + ": ", value, null);
          }

          att_list = db.get_class_attribute_list(class_list[i], "*");
          lst = db.get_class_attribute_property(class_list[i], att_list);
          prtOut = false;
          for (k = 0; k < lst.length; k++) {
            prop_list = lst[k].get_property_list();
            for (j = 0; j < prop_list.length; j++) {
              if(!prtOut) {
                System.out.print("\n# CLASS " + class_list[i] + " attribute properties\n\n");
                prtOut=true;
              }
              if (prop_list[j].indexOf(' ') != -1) prop_list[j] = "\"" + prop_list[j] + "\"";
              String[] value = lst[k].get_value(j);
              JiveUtils.printFormatedRes("CLASS/" + class_list[i] + "/" + att_list[k] + "->" + prop_list[j] + ": ", value, null);
            }
          }

          System.out.print("\n");

          // Mark class as saved
          JiveUtils.addSavedClass(class_list[i]);

        }

        // Device declaration and resource

        System.out.print("#---------------------------------------------------------\n");
        System.out.print("# SERVER " + srvName + ", " + class_list[i] + " device declaration\n");
        System.out.print("#---------------------------------------------------------\n\n");

        String[] dev_list = dbs.get_device_name(class_list[i]);
        JiveUtils.printFormatedRes(srvName + "/DEVICE/" + class_list[i] + ": ", dev_list, null);
        System.out.print("\n");

        for (l = 0; l < dev_list.length; l++) {

          prop_list = db.get_device_property_list(dev_list[l], "*");
          if (prop_list.length > 0) {
            System.out.print("\n# --- " + dev_list[l] + " properties\n\n");
            for (j = 0; j < prop_list.length; j++) {
              String[] value = db.get_device_property(dev_list[l], prop_list[j]).extractStringArray();
              if (prop_list[j].indexOf(' ') != -1 || prop_list[j].indexOf('/') != -1) prop_list[j] = "\"" + prop_list[j] + "\"";
              JiveUtils.printFormatedRes(dev_list[l] + "->" + prop_list[j] + ": ", value, null);
            }
          }

          try {

            DeviceProxy ds = new DeviceProxy(dev_list[l]);
            att_list = ds.get_attribute_list();
            lst = db.get_device_attribute_property(dev_list[l], att_list);
            prtOut = false;
            for (k = 0; k < lst.length; k++) {
              prop_list = lst[k].get_property_list();
              for (j = 0; j < prop_list.length; j++) {
                if (!prtOut) {
                  System.out.print("\n# --- " + dev_list[l] + " attribute properties\n\n");
                  prtOut = true;
                }
                if (prop_list[j].indexOf(' ') != -1  || prop_list[j].indexOf('/') != -1) prop_list[j] = "\"" + prop_list[j] + "\"";
                String[] value = lst[k].get_value(j);
                JiveUtils.printFormatedRes(dev_list[l] + "/" + att_list[k] + "->" + prop_list[j] + ": ", value, null);
              }
            }

          } catch (DevFailed e) {

            JiveUtils.showJiveError("Attribute properties for " + dev_list[l] + " has not been saved !\n"
                                    + e.errors[0].desc);

          }

        }

        System.out.print("\n");

      }

      // Save admin server data
      String[] prop_list;
      String admDevName = "dserver/" + srvName;

      prop_list = db.get_device_property_list(admDevName, "*");
      if (prop_list.length > 0) {
        System.out.print("\n# --- " + admDevName + " properties\n\n");
        for (j = 0; j < prop_list.length; j++) {
          String[] value = db.get_device_property(admDevName, prop_list[j]).extractStringArray();
          if (prop_list[j].indexOf(' ') != -1) prop_list[j] = "\"" + prop_list[j] + "\"";
          JiveUtils.printFormatedRes(admDevName + "->" + prop_list[j] + ": ", value, null);
        }
      }
      
    } catch (DevFailed e) {
      JiveUtils.printTangoError(e);
    } catch (IOException e) {
    }


  }

  public static void main(String[] args) {

    if (args.length != 1) {
      System.out.println("Usage: tg_devres server/instance");
    } else {
      new DbReader(args[0]);
    }

  }

}





