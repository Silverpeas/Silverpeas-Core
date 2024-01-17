/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.node.model;

import org.silverpeas.core.clipboard.ClipboardSelection;
import org.silverpeas.core.clipboard.SilverpeasKeyData;
import org.silverpeas.core.index.indexing.model.IndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;

import javax.annotation.Nonnull;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.Serializable;

public class NodeSelection extends ClipboardSelection implements Serializable {

  private static final long serialVersionUID = -6462797069972573255L;
  public static final DataFlavor NodeDetailFlavor = new DataFlavor(NodeDetail.class, "Node");
  private final NodeDetail nodeDetail;

  public NodeSelection(NodeDetail node) {
    super();
    nodeDetail = node;
    super.addFlavor(NodeDetailFlavor);
  }

  @Override
  @Nonnull
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
    IndexEntry indexEntry = new IndexEntry(new IndexEntryKey(pk.getInstanceId(), "Node",
        pk.getId()));
    indexEntry.setTitle(nodeDetail.getName());
    return indexEntry;
  }

  @Override
  public SilverpeasKeyData getKeyData() {
    SilverpeasKeyData keyData = new SilverpeasKeyData();
    keyData.setTitle(nodeDetail.getName());
    keyData.setAuthor(nodeDetail.getCreatorId());
    keyData.setCreationDate(nodeDetail.getCreationDate());
    keyData.setDesc(nodeDetail.getDescription());
    return keyData;
  }
}
