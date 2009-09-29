package com.silverpeas.util.clipboard;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.Serializable;
import java.util.ArrayList;

import com.stratelia.webactiv.util.indexEngine.model.IndexEntry;

/**
 * Class declaration
 * 
 * 
 * @author
 */
public abstract class ClipboardSelection implements Serializable,
    ClipboardOwner, Transferable {
  static public DataFlavor IndexFlavor = new DataFlavor("silverpeas/index",
      "Silverpeas index");
  static public DataFlavor SilverpeasKeyDataFlavor = new DataFlavor(
      "silverpeas/keydata", "Silverpeas keydata");
  protected ArrayList supportedFlavorsList = new ArrayList();
  protected boolean selected = true;
  private boolean isCutted = false;

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   * 
   */
  public ClipboardSelection() {
    supportedFlavorsList.add(IndexFlavor);
    supportedFlavorsList.add(SilverpeasKeyDataFlavor);
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   * 
   */
  protected void addFlavor(DataFlavor parFlavor) {
    supportedFlavorsList.add(parFlavor);
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   * 
   */
  public boolean isDataFlavorSupported(DataFlavor parFlavor) {
    // return parFlavor.equals (IndexFlavor);
    boolean supported = true;

    try {
      int index = 0;

      while (!parFlavor
          .equals((DataFlavor) (supportedFlavorsList.get(index++))))
        ;
    } catch (IndexOutOfBoundsException e) {
      supported = false;
    }
    ;
    return supported;
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   * 
   */
  public synchronized DataFlavor[] getTransferDataFlavors() {
    return ((DataFlavor[]) (supportedFlavorsList.toArray()));
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   * 
   */
  public synchronized Object getTransferData(DataFlavor parFlavor)
      throws UnsupportedFlavorException {
    if (parFlavor.equals(IndexFlavor)) {
      return getIndexEntry();
    } else if (parFlavor.equals(SilverpeasKeyDataFlavor)) {
      return getKeyData();
    } else {
      throw new UnsupportedFlavorException(IndexFlavor);
    }
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   * 
   */
  public boolean isSelected() {
    return selected;
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   * 
   */
  public void setSelected(boolean setIt) {
    selected = setIt;
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   * 
   */
  public void lostOwnership(Clipboard parClipboard, Transferable parTransferable) {
    // System.out.println ("Lost ownership");
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   * 
   */
  abstract protected IndexEntry getIndexEntry();

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   * 
   */
  abstract protected SilverpeasKeyData getKeyData();

  public boolean isCutted() {
    return isCutted;
  }

  public void setCutted(boolean isCutted) {
    this.isCutted = isCutted;
  }

}
