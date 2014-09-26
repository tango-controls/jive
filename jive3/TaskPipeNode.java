package jive3;

import fr.esrf.Tango.DevVarLongStringArray;
import fr.esrf.TangoApi.*;
import fr.esrf.Tango.DevFailed;

import javax.swing.*;
import java.util.*;

import jive.JiveUtils;

public class TaskPipeNode extends TangoNode {

  private Database db;
  private String   devName;
  private List<PipeInfo> pipeInfo;

  TaskPipeNode(Database db, String devName) {
    this.db = db;
    this.devName = devName;
    pipeInfo = null;
  }

  void populateNode() throws DevFailed {
  }

  ImageIcon getIcon() {
    return TangoNodeRenderer.pipeicon;
  }

  public String toString() {
    return "Pipe config";
  }

  String getTitle() {
    return "Pipe configuration";
  }

  String getName() {
    return devName;
  }

  public boolean isLeaf() {
    return true;
  }

  int getAttributeNumber() {
    if(pipeInfo==null) browsePipeInfo();
    return pipeInfo.size();
  }

  String getAttName(int idx) {
    if(pipeInfo==null) browsePipeInfo();
    return pipeInfo.get(idx).getName();
  }

  // -- Display ----------------------------------------------------

  String getLabel(int idx) {
    if(pipeInfo==null) browsePipeInfo();
    return pipeInfo.get(idx).getLabel();
  }

  void setLabel(int idx,String value) {

    try {
      DeviceProxy ds = new DeviceProxy(devName);
      pipeInfo.get(idx).setLabel(value);
      ds.setPipeConfig(pipeInfo);
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void resetDisplay(int idx) {

    // TODO
    //try {
    //} catch (DevFailed e) {
    //  JiveUtils.showTangoError(e);
    //}
    resetLDisplay(idx);

  }

  void resetLDisplay(int idx) {

    // Restore library defaults (Tango8)
    try {
      DeviceProxy ds = new DeviceProxy(devName);
      pipeInfo.get(idx).setLabel("Not specified");
      ds.setPipeConfig(pipeInfo);
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void resetULDisplay(int idx) {

    // Restore user/library defaults (Tango8)
    try {
      DeviceProxy ds = new DeviceProxy(devName);
      pipeInfo.get(idx).setLabel("");
      ds.setPipeConfig(pipeInfo);
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void resetCULDisplay(int idx) {

    // Restore class/user/library defaults (Tango8)
    try {
      DeviceProxy ds = new DeviceProxy(devName);
      pipeInfo.get(idx).setLabel("NaN");
      ds.setPipeConfig(pipeInfo);
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  // -- Description -------------------------------------------------------------

  String getDescription(int idx) {
    if(pipeInfo==null) browsePipeInfo();
    return pipeInfo.get(idx).getDescription();
  }

  void setDescription(int idx,String value) {

    try {
      DeviceProxy ds = new DeviceProxy(devName);
      pipeInfo.get(idx).setDescription(value);
      ds.setPipeConfig(pipeInfo);
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void resetDescription(int idx) {

    //TODO
    //try {
    //} catch (DevFailed e) {
    //  JiveUtils.showTangoError(e);
    //}
    resetLDescription(idx);

  }

  void resetLDescription(int idx) {

    // Restore library defaults (Tango8)
    try {
      DeviceProxy ds = new DeviceProxy(devName);
      pipeInfo.get(idx).setDescription("Not specified");
      ds.setPipeConfig(pipeInfo);
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void resetULDescription(int idx) {

    // Restore user/library defaults (Tango8)
    try {
      DeviceProxy ds = new DeviceProxy(devName);
      pipeInfo.get(idx).setDescription("");
      ds.setPipeConfig(pipeInfo);
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  void resetCULDescription(int idx) {

    // Restore class/user/library defaults (Tango8)
    try {
      DeviceProxy ds = new DeviceProxy(devName);
      pipeInfo.get(idx).setDescription("NaN");
      ds.setPipeConfig(pipeInfo);
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  // -- Browsing -------------------------------------------------------------

  void browsePipeInfo() {

    try {
      DeviceProxy ds = new DeviceProxy(devName);
      pipeInfo = ds.getPipeConfig();
    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

    // Create empty list
    if(pipeInfo==null) pipeInfo = new List<PipeInfo>() {

      public int size() {
        return 0;
      }

      public boolean isEmpty() {
        return true;
      }

      public boolean contains(Object o) {
        return false;
      }

      public Iterator<PipeInfo> iterator() {
        return null;
      }

      public Object[] toArray() {
        return new Object[0];
      }

      public <T> T[] toArray(T[] a) {
        return null;
      }

      public boolean add(PipeInfo pipeInfo) {
        return false;
      }

      public boolean remove(Object o) {
        return false;
      }

      public boolean containsAll(Collection<?> c) {
        return false;
      }

      public boolean addAll(Collection<? extends PipeInfo> c) {
        return false;
      }

      public boolean addAll(int index, Collection<? extends PipeInfo> c) {
        return false;
      }

      public boolean removeAll(Collection<?> c) {
        return false;
      }

      public boolean retainAll(Collection<?> c) {
        return false;
      }

      public void clear() {

      }

      public PipeInfo get(int index) {
        return null;
      }

      public PipeInfo set(int index, PipeInfo element) {
        return null;
      }

      public void add(int index, PipeInfo element) {

      }

      public PipeInfo remove(int index) {
        return null;
      }

      public int indexOf(Object o) {
        return 0;
      }

      public int lastIndexOf(Object o) {
        return 0;
      }

      public ListIterator<PipeInfo> listIterator() {
        return null;
      }

      public ListIterator<PipeInfo> listIterator(int index) {
        return null;
      }

      public List<PipeInfo> subList(int fromIndex, int toIndex) {
        return null;
      }

    };

  }

  public void restartDevice() {

    try {

      DbDevImportInfo info = db.import_device(devName);
      DeviceProxy ds = new DeviceProxy("dserver/" + info.server);
      DeviceData in = new DeviceData();
      in.insert(devName);
      ds.command_inout("DevRestart", in);

    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

}
