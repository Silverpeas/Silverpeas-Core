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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.admin.user;

import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.clipboard.ClipboardSelection;
import org.silverpeas.core.clipboard.SilverpeasKeyData;
import org.silverpeas.core.index.indexing.model.IndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;

import javax.annotation.Nonnull;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.Serializable;

/**
 * Selection by an administrator of a given group of users.
 *
 * @author mmoquillon
 */
public class GroupSelection extends ClipboardSelection implements Serializable {

  private static final String TYPE = "Group";
  public static final DataFlavor GROUP_FLAVOR = new DataFlavor(Group.class, TYPE);

  private final Group group;

  public GroupSelection(Group group) {
    super();
    this.group = group;
    super.addFlavor(GROUP_FLAVOR);
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
      if (GROUP_FLAVOR.equals(parFlavor)) {
        transferedData = group;
      } else {
        throw e;
      }
    }
    return transferedData;
  }

  /**
   * Returns the IndexEntry for the group being copied/cut
   * @return an IndexEntry for the selected group
   */
  @Override
  public IndexEntry getIndexEntry() {
    IndexEntry indexEntry =
        new IndexEntry(new IndexEntryKey("Groups", TYPE, group.getId()));
    indexEntry.setTitle(group.getName());
    indexEntry.setPreview(group.getDescription());
    return indexEntry;
  }

  /**
   * Transforms the data into a SilverpeasKeyData.
   */
  @Override
  public SilverpeasKeyData getKeyData() {
    SilverpeasKeyData keyData = new SilverpeasKeyData(group.getId());
    keyData.setTitle(group.getName());
    keyData.setCreationDate(group.getCreationDate());
    keyData.setDesc(group.getDescription());
    keyData.setType(TYPE);
    return keyData;
  }
}
  