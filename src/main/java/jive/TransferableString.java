package jive;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * A class to transfer string via drag and drop
 */
class TransferableString implements Transferable {

  private String str;

  public TransferableString(String str) {
    this.str = str;
  }

  public DataFlavor[] getTransferDataFlavors() {
    DataFlavor[] result= new DataFlavor[1];
    result[0]= DataFlavor.stringFlavor;
    return result;
  }

  public Object getTransferData(DataFlavor flavor)
      throws UnsupportedFlavorException, IOException {
    return str;
  }

  public boolean isDataFlavorSupported(DataFlavor flavor) {
    return DataFlavor.stringFlavor.equals(flavor);
  }

  public String toString() {
    return str;
  }

}
