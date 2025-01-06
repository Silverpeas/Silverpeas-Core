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
package org.silverpeas.core.admin.component.model;

import org.silverpeas.core.clipboard.ClipboardSelection;
import org.silverpeas.core.clipboard.SKDException;
import org.silverpeas.core.clipboard.SilverpeasKeyData;
import org.silverpeas.core.index.indexing.model.IndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.annotation.Nonnull;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.Serializable;

public class ComponentSelection extends ClipboardSelection implements Serializable {

  private static final long serialVersionUID = 4750709802063183409L;
  private static final String TYPE = "Component";
  public static final DataFlavor ComponentDetailFlavor = new DataFlavor(ComponentInst.class, TYPE);
  private final ComponentInst componentInst;

  /**
   * @param component the component selected.
   */
  public ComponentSelection(ComponentInst component) {
    super();
    componentInst = component;
    super.addFlavor(ComponentDetailFlavor);
  }

  /**
   * Returns the transferred data.
   * @param parFlavor the DataFlavor.
   * @return the data copied.
   * @throws UnsupportedFlavorException if the flavor isn't supported by the selection.
   */
  @Override
  @Nonnull
  public synchronized Object getTransferData(DataFlavor parFlavor)
      throws UnsupportedFlavorException {
    Object transferredData;
    try {
      transferredData = super.getTransferData(parFlavor);
    } catch (UnsupportedFlavorException e) {
      if (ComponentDetailFlavor.equals(parFlavor)) {
        transferredData = componentInst;
      } else {
        throw e;
      }
    }
    return transferredData;
  }

  /**
   * Returns the IndexEntry for the component being copied.
   * @return an IndexEntry for this component
   */
  @Override
  public IndexEntry getIndexEntry() {
    IndexEntry indexEntry =
        new IndexEntry(new IndexEntryKey(componentInst.getId(), TYPE, componentInst.getId()));
    indexEntry.setTitle(componentInst.getLabel());
    return indexEntry;
  }

  /**
   * Transforms the data into a SilverpeasKeyData.
   */
  @Override
  public SilverpeasKeyData getKeyData() {
    SilverpeasKeyData keyData = new SilverpeasKeyData(componentInst.getId());
    keyData.setTitle(componentInst.getLabel());
    keyData.setAuthor(componentInst.getCreatorUserId());
    keyData.setCreationDate(componentInst.getCreationDate());
    keyData.setDesc(componentInst.getDescription());
    keyData.setType(TYPE);
    keyData.setLink(URLUtil.getSimpleURL(URLUtil.URL_COMPONENT, componentInst.getId()));
    try {
      keyData.setProperty("COMPONENT_NAME", ComponentInst.getComponentName(componentInst.getId()));
    } catch (SKDException e) {
      SilverLogger.getLogger(this).error(e);
    }
    return keyData;
  }
}
