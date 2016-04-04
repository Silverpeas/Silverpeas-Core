/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.admin.component.model;

import org.silverpeas.core.clipboard.ClipboardSelection;
import org.silverpeas.core.clipboard.SilverpeasKeyData;
import org.silverpeas.core.index.indexing.model.IndexEntry;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.Serializable;

public class ComponentSelection extends ClipboardSelection implements Serializable {

  private static final long serialVersionUID = 4750709802063183409L;
  public static final DataFlavor ComponentDetailFlavor = new DataFlavor(ComponentInst.class,
      "Component");
  private ComponentInst componentInst;

  /**
   * @param component the component selected.
   */
  public ComponentSelection(ComponentInst component) {
    super();
    componentInst = component;
    super.addFlavor(ComponentDetailFlavor);
  }

  /**
   * Returns the data transfered.
   * @param parFlavor the DataFlavor.
   * @return the dta copied.
   * @throws UnsupportedFlavorException
   */
  @Override
  public synchronized Object getTransferData(DataFlavor parFlavor)
      throws UnsupportedFlavorException {
    Object transferedData;
    try {
      transferedData = super.getTransferData(parFlavor);
    } catch (UnsupportedFlavorException e) {
      if (ComponentDetailFlavor.equals(parFlavor)) {
        transferedData = componentInst;
      } else {
        throw e;
      }
    }
    return transferedData;
  }

  /**
   * Returns the IndexEntry for the component being copeid.
   * @return an IndexEntry for this component
   */
  @Override
  public IndexEntry getIndexEntry() {
    IndexEntry indexEntry =
        new IndexEntry(componentInst.getId(), "Component", componentInst.getId());
    indexEntry.setTitle(componentInst.getLabel());
    return indexEntry;
  }

  /**
   * Tranforms the dat into a SilverpeasKeyData.
   */
  @Override
  public SilverpeasKeyData getKeyData() {
    SilverpeasKeyData keyData = new SilverpeasKeyData();
    keyData.setTitle(componentInst.getName());
    keyData.setAuthor(componentInst.getCreatorUserId());
    keyData.setCreationDate(componentInst.getCreateDate());
    keyData.setDesc(componentInst.getDescription());
    return keyData;
  }
}
