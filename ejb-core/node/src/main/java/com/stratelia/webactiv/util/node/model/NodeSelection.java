/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.util.node.model;

import com.silverpeas.util.clipboard.ClipboardSelection;
import com.silverpeas.util.clipboard.SilverpeasKeyData;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.Serializable;
import java.text.ParseException;

import com.stratelia.webactiv.util.DateUtil;
import org.silverpeas.search.indexEngine.model.IndexEntry;

public class NodeSelection extends ClipboardSelection implements Serializable {

  private static final long serialVersionUID = -6462797069972573255L;
  static public DataFlavor NodeDetailFlavor;

  static {
    NodeDetailFlavor = new DataFlavor(NodeDetail.class, "Node");
  }
  private NodeDetail nodeDetail;

  public NodeSelection(NodeDetail node) {
    super();
    nodeDetail = node;
    super.addFlavor(NodeDetailFlavor);
  }

  @Override
  public synchronized Object getTransferData(DataFlavor parFlavor)
      throws UnsupportedFlavorException {
    Object transferedData;

    try {
      transferedData = super.getTransferData(parFlavor);
    } catch (UnsupportedFlavorException e) {
      if (parFlavor.equals(NodeDetailFlavor)) {
        transferedData = nodeDetail;
      } else {
        throw e;
      }
    }
    return transferedData;
  }

  @Override
  public IndexEntry getIndexEntry() {
    NodePK pk = nodeDetail.getNodePK();
    IndexEntry indexEntry = new IndexEntry(pk.getInstanceId(), "Node", pk.getId());
    indexEntry.setTitle(nodeDetail.getName());
    return indexEntry;
  }

  @Override
  public SilverpeasKeyData getKeyData() {
    SilverpeasKeyData keyData = new SilverpeasKeyData();
    keyData.setTitle(nodeDetail.getName());
    keyData.setAuthor(nodeDetail.getCreatorId());
    try {
      keyData.setCreationDate(DateUtil.parse(nodeDetail.getCreationDate()));
    } catch (ParseException e) {
      SilverTrace.error("node", "NodeSelection.getKeyData()", "root.EX_NO_MESSAGE", e);
    }
    keyData.setDesc(nodeDetail.getDescription());
    return keyData;
  }
}