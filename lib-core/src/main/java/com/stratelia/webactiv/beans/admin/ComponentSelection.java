/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.beans.admin;

import java.io.Serializable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;

import com.silverpeas.util.clipboard.ClipboardSelection;
import com.silverpeas.util.clipboard.SilverpeasKeyData;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntry;

public class ComponentSelection extends ClipboardSelection implements Serializable {
  
  private static final long serialVersionUID = 4750709802063183409L;
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
   */
  public ComponentSelection(ComponentInst component) {
    super();
    componentInst = component;
    super.addFlavor(ComponentDetailFlavor);
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
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
   */
  public IndexEntry getIndexEntry() {
    IndexEntry indexEntry = new IndexEntry(componentInst.getId(), "Component",
        componentInst.getId());
    indexEntry.setTitle(componentInst.getLabel());
    return indexEntry;
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------ Tranformation obligatoire en SilverpeasKeyData
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
