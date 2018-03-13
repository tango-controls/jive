package jive3;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.ApiUtil;
import fr.esrf.TangoApi.Database;
import fr.esrf.TangoApi.DbAttribute;
import jive.JiveUtils;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Write property(ies) to a file
 */
public class DbFileWriter {

  // -------------------------------------------------------------------------------------------------------

  private static String lastResOpenedDir = ".";

  private static File chooseFile() {

    JFileChooser chooser = new JFileChooser(lastResOpenedDir);
    int returnVal = chooser.showSaveDialog(null);

    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File f = chooser.getSelectedFile();
      if( f==null ) {
        return null;
      } else {
        lastResOpenedDir = f.getAbsolutePath();
        if (f.exists()) returnVal = JOptionPane.showConfirmDialog(null, "Do you want to overwrite " + f.getName() + " ?", "Confirm overwrite", JOptionPane.YES_NO_OPTION);
        if (returnVal == JOptionPane.YES_OPTION) {
          return f;
        }
      }
    }

    return null;

  }

  // -------------------------------------------------------------------------------------------------------

  public static void SaveAllDeviceProperties(String devName) throws DevFailed,IOException {

    FileWriter fw = new FileWriter(chooseFile());
    SaveDeviceProperties(devName, fw);
    SaveDeviceAttributesProperties(devName, fw);
    fw.close();

  }

  public static void SaveDeviceProperties(String devName) throws DevFailed,IOException {

    FileWriter fw = new FileWriter(chooseFile());
    SaveDeviceProperties(devName, fw);
    fw.close();

  }

  public static void SaveDeviceAttributesProperties(String devName) throws DevFailed,IOException {

    FileWriter fw = new FileWriter(chooseFile());
    SaveDeviceAttributesProperties(devName, fw);
    fw.close();

  }

  public static void SaveDeviceAttributeProperties(String devName,String attName) throws DevFailed,IOException {

    FileWriter fw = new FileWriter(chooseFile());
    SaveDeviceAttributeProperties(devName, attName, fw);
    fw.close();

  }


  public static void SaveDeviceProperties(String devName, FileWriter fw) throws DevFailed,IOException {

    Database db = ApiUtil.get_db_obj();
    String[] propList = db.get_device_property_list(devName, "*");

    if (propList.length > 0) {
      fw.write("\n# --- " + devName + " properties\n\n");
      for (int j = 0; j < propList.length; j++) {
        String[] value = db.get_device_property(devName, propList[j]).extractStringArray();
        if (propList[j].indexOf(' ') != -1) propList[j] = "\"" + propList[j] + "\"";
        JiveUtils.printFormatedRes(devName + "->" + propList[j] + ": ", value, fw);
      }
    }

  }


  public static void SaveDeviceAttributesProperties(String devName, FileWriter fw) throws DevFailed,IOException {

    Database db = ApiUtil.get_db_obj();
    String[] attList = db.get_device_attribute_list(devName);
    fw.write("\n# --- " + devName + " attribute properties\n\n");
    for (int i = 0; i < attList.length; i++)
      SaveDeviceAttributeProperties(devName,attList[i],fw);

  }

  public static void SaveDeviceAttributeProperties(String devName, String attName, FileWriter fw) throws DevFailed,IOException {

    Database db = ApiUtil.get_db_obj();
    DbAttribute dba = db.get_device_attribute_property(devName, attName);
    String[] prop_list = dba.get_property_list();

    for (int j = 0; j < prop_list.length; j++) {
      if (prop_list[j].indexOf(' ') != -1) prop_list[j] = "\"" + prop_list[j] + "\"";
      String[] value = dba.get_value(j);
      JiveUtils.printFormatedRes(devName + "/" + attName + "->" + prop_list[j] + ": ", value, fw);
    }

  }

  // -------------------------------------------------------------------------------------------------------

  public static void SaveAllClassProperties(String className) throws DevFailed,IOException {

    FileWriter fw = new FileWriter(chooseFile());
    SaveClassProperties(className, fw);
    SaveClassAttributesProperties(className, fw);
    fw.close();

  }

  public static void SaveClassProperties(String className) throws DevFailed,IOException {

    FileWriter fw = new FileWriter(chooseFile());
    SaveClassProperties(className, fw);
    fw.close();

  }

  public static void SaveClassAttributesProperties(String className) throws DevFailed,IOException {

    FileWriter fw = new FileWriter(chooseFile());
    SaveClassAttributesProperties(className, fw);
    fw.close();

  }

  public static void SaveClassAttributeProperties(String className,String attName) throws DevFailed,IOException {

    FileWriter fw = new FileWriter(chooseFile());
    SaveClassAttributeProperties(className, attName, fw);
    fw.close();

  }


  public static void SaveClassProperties(String className,FileWriter fw) throws DevFailed,IOException {

    Database db = ApiUtil.get_db_obj();
    String[] propList = db.get_class_property_list(className, "*");

    for (int j = 0; j < propList.length; j++) {
      String[] value = db.get_class_property(className, propList[j]).extractStringArray();
      if (propList[j].indexOf(' ') != -1) propList[j] = "\"" + propList[j] + "\"";
      JiveUtils.printFormatedRes("CLASS/" + className + "->" + propList[j] + ": ", value, fw);
    }

  }

  public static void SaveClassAttributesProperties(String className,FileWriter fw) throws DevFailed,IOException {

    Database db = ApiUtil.get_db_obj();
    String[] attList = db.get_class_attribute_list(className, "*");
    fw.write("\n# CLASS " + className + " attribute properties\n\n");
    for(int i=0;i<attList.length;i++)
      SaveClassAttributeProperties(className,attList[i],fw);

  }

  public static void SaveClassAttributeProperties(String className,String attName,FileWriter fw) throws DevFailed,IOException {

    Database db = ApiUtil.get_db_obj();
    DbAttribute dba = db.get_class_attribute_property(className, attName);
    String[] propList = dba.get_property_list();

    for (int j = 0; j < propList.length; j++) {
      if (propList[j].indexOf(' ') != -1) propList[j] = "\"" + propList[j] + "\"";
      String[] value = dba.get_value(j);
      JiveUtils.printFormatedRes("CLASS/" + className + "/" + attName + "->" + propList[j] + ": ", value, fw);
    }

    fw.write("\n");

  }

  // -------------------------------------------------------------------------------------------------------

  public static void SaveFreeProperties(String objName) throws DevFailed,IOException {

    FileWriter fw = new FileWriter(chooseFile());
    SaveFreeProperties(objName, fw);
    fw.close();

  }

  public static void SaveFreeProperties(String objName,FileWriter fw) throws DevFailed,IOException {

    Database db = ApiUtil.get_db_obj();
    String[] propList = db.get_object_property_list(objName, "*");

    if (propList.length > 0) {
      fw.write("\n# --- " + objName + " properties\n\n");
      for (int j = 0; j < propList.length; j++) {
        String[] value = db.get_property(objName, propList[j]).extractStringArray();
        if (propList[j].indexOf(' ') != -1) propList[j] = "\"" + propList[j] + "\"";
        JiveUtils.printFormatedRes("FREE/"+objName + "->" + propList[j] + ": ", value, fw);
      }
    }

  }

}
