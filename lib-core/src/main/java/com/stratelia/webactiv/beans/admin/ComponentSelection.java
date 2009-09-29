package com.stratelia.webactiv.beans.admin;

import java.io.Serializable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;

import com.silverpeas.util.clipboard.ClipboardSelection;
import com.silverpeas.util.clipboard.SilverpeasKeyData;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntry;

public class ComponentSelection extends ClipboardSelection implements
    Serializable {
  static public DataFlavor ComponentDetailFlavor;
  static {
    try {
      ComponentDetailFlavor = new DataFlavor(Class
          .forName("com.stratelia.webactiv.beans.admin.ComponentInst"),
          "Component");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  private ComponentInst componentInst;

  /**
   * --------------------------------------------------------------------------
   * ------------------------------ Constructor
   * 
   */
  public ComponentSelection(ComponentInst component) {
    super();
    componentInst = component;
    super.addFlavor(ComponentDetailFlavor);
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   * 
   */
  public synchronized Object getTransferData(DataFlavor parFlavor)
      throws UnsupportedFlavorException {
    Object transferedData;

    try {
      transferedData = super.getTransferData(parFlavor);
    } catch (UnsupportedFlavorException e) {
      if (parFlavor.equals(ComponentDetailFlavor))
        transferedData = componentInst;
      else
        throw e;
    }
    return transferedData;
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   * 
   */
  public IndexEntry getIndexEntry() {
    IndexEntry indexEntry = new IndexEntry(componentInst.getId(), "Component",
        componentInst.getId());
    indexEntry.setTitle(componentInst.getLabel());
    return indexEntry;
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------ Tranformation obligatoire en
   * SilverpeasKeyData
   */
  public SilverpeasKeyData getKeyData() {
    SilverpeasKeyData keyData = new SilverpeasKeyData();

    keyData.setTitle(componentInst.getName());
    keyData.setAuthor(componentInst.getCreatorUserId());
    keyData.setCreationDate(componentInst.getCreateDate());
    keyData.setDesc(componentInst.getDescription());
    return keyData;
  }
}
