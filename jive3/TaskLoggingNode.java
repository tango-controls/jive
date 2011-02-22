package jive3;

import jive3.TangoNode;
import jive3.TangoNodeRenderer;
import fr.esrf.TangoApi.Database;
import fr.esrf.Tango.DevFailed;

import javax.swing.*;

import jive.JiveUtils;

public class TaskLoggingNode extends TangoNode {

  private Database db;
  private String   devName;

  TaskLoggingNode(Database db, String devName) {
    this.db = db;
    this.devName = devName;
  }

  void populateNode() throws DevFailed {
  }

  ImageIcon getIcon() {
    return TangoNodeRenderer.leaflogicon;
  }

  public String toString() {
    return "Logging";
  }

  String getTitle() {
    return "Logging";
  }

  String getName() {
    return devName;
  }

  public boolean isLeaf() {
    return true;
  }

  // -----------------------------------------

  String getCurrentLoggingLevel() {

    String[] res = JiveUtils.getLoggingStatus(db, devName, 0);
    return JiveUtils.stringArrayToString(res);

  }

  void setCurrentLoggingLevel(String value) {

    JiveUtils.setLoggingLevel(db,devName,value);

  }

  String getCurrentLoggingTarget() {

    String[] res = JiveUtils.getLoggingStatus(db, devName, 1);
    return JiveUtils.stringArrayToString(res);

  }

  void setCurrentLoggingTarget(String value) {

    JiveUtils.setLoggingTarget(db,devName,value);

  }

  String getLoggingLevel() {

    try {
      String[] res = db.get_device_property(devName, "logging_level").extractStringArray();
      return JiveUtils.stringArrayToString(res);
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

    return "";

  }

  void setLoggingLevel(String value) {

    try {
      db.put_device_property(devName, JiveUtils.makeDbDatum("logging_level", value));
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void resetLoggingLevel() {

    try {
      db.delete_device_property(devName,"logging_level");
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  String getLoggingTarget() {

    try {
      String[] res = db.get_device_property(devName, "logging_target").extractStringArray();
      return JiveUtils.stringArrayToString(res);
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

    return "";

  }

  void setLoggingTarget(String value) {

    try {
      db.put_device_property(devName, JiveUtils.makeDbDatum("logging_target", value));
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void resetLoggingTarget() {

    try {
      db.delete_device_property(devName,"logging_target");
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  String getLoggingRft() {

    try {
      String[] res = db.get_device_property(devName, "logging_rft").extractStringArray();
      return JiveUtils.stringArrayToString(res);
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

    return "2";

  }

  void setLoggingRft(String value) {

    try {
      db.put_device_property(devName, JiveUtils.makeDbDatum("logging_rft", value));
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void resetLoggingRft() {

    try {
      db.delete_device_property(devName,"logging_rft");
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

}