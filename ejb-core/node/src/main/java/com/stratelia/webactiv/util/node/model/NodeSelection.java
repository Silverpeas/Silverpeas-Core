package com.stratelia.webactiv.util.node.model;

import com.silverpeas.util.clipboard.ClipboardSelection;
import com.silverpeas.util.clipboard.SilverpeasKeyData;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.Serializable;
import java.text.ParseException;

import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntry;

public class NodeSelection extends ClipboardSelection implements Serializable {

  static public DataFlavor NodeDetailFlavor;
  static {
    try {
      NodeDetailFlavor = new DataFlavor(Class
          .forName("com.stratelia.webactiv.util.node.model.NodeDetail"), "Node");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  private NodeDetail nodeDetail;

  /**
   * --------------------------------------------------------------------------
   * ------------------------------ Constructor
   * 
   */
  public NodeSelection(NodeDetail node) {
    super();
    nodeDetail = node;
    super.addFlavor(NodeDetailFlavor);
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
      if (parFlavor.equals(NodeDetailFlavor))
        transferedData = nodeDetail;
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
    NodePK pk = nodeDetail.getNodePK();
    IndexEntry indexEntry = new IndexEntry(pk.getInstanceId(), "Node", pk
        .getId());
    indexEntry.setTitle(nodeDetail.getName());
    return indexEntry;
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------ Tranformation obligatoire en
   * SilverpeasKeyData
   */
  public SilverpeasKeyData getKeyData() {
    SilverpeasKeyData keyData = new SilverpeasKeyData();

    keyData.setTitle(nodeDetail.getName());
    keyData.setAuthor(nodeDetail.getCreatorId());
    try {
      keyData.setCreationDate(DateUtil.parse(nodeDetail.getCreationDate()));
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    keyData.setDesc(nodeDetail.getDescription());
    return keyData;
  }
}