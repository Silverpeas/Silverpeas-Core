/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.admin.space;

import org.silverpeas.core.clipboard.ClipboardSelection;
import org.silverpeas.core.clipboard.SilverpeasKeyData;
import org.silverpeas.core.index.indexing.model.IndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;

import javax.annotation.Nonnull;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.Serializable;

public class SpaceSelection extends ClipboardSelection implements Serializable {

  private static final long serialVersionUID = -1717229458481172945L;
  public static final DataFlavor SpaceFlavor = new DataFlavor(SpaceInst.class,
      "Space");
  private final SpaceInst spaceInst;

  /**
   * @param space the selected space
   */
  public SpaceSelection(SpaceInst space) {
    super();
    spaceInst = space;
    super.addFlavor(SpaceFlavor);
  }

  /**
   * Returns the data transferred.
   * @param parFlavor the DataFlavor.
   * @return the dta copied.
   * @throws UnsupportedFlavorException if an error occurs
   */
  @Override
  @Nonnull
  public synchronized Object getTransferData(DataFlavor parFlavor)
      throws UnsupportedFlavorException {
    Object transferedData;
    try {
      transferedData = super.getTransferData(parFlavor);
    } catch (UnsupportedFlavorException e) {
      if (SpaceFlavor.equals(parFlavor)) {
        transferedData = spaceInst;
      } else {
        throw e;
      }
    }
    return transferedData;
  }

  /**
   * Returns the IndexEntry for the space being copied
   * @return an IndexEntry for this space
   */
  @Override
  public IndexEntry getIndexEntry() {
    IndexEntry indexEntry =
        new IndexEntry(new IndexEntryKey(spaceInst.getId(), "Space", spaceInst.getId()));
    indexEntry.setTitle(spaceInst.getName());
    return indexEntry;
  }

  /**
   * Tranforms the dat into a SilverpeasKeyData.
   */
  @Override
  public SilverpeasKeyData getKeyData() {
    SilverpeasKeyData keyData = new SilverpeasKeyData();
    keyData.setTitle(spaceInst.getName());
    keyData.setAuthor(spaceInst.getCreatorUserId());
    keyData.setCreationDate(spaceInst.getCreationDate());
    keyData.setDesc(spaceInst.getDescription());
    return keyData;
  }
}