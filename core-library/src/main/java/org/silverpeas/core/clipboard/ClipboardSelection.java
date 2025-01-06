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
package org.silverpeas.core.clipboard;

import org.silverpeas.core.index.indexing.model.IndexEntry;
import org.silverpeas.kernel.annotation.NonNull;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * A clipboard selection represents a contribution or a resource in Silverpeas that has been
 * copied or cut; in other words, an element that has been put into the clipboard.
 */
public abstract class ClipboardSelection implements Serializable, ClipboardOwner, Transferable {

  private static final long serialVersionUID = 7296607705319157979L;
  public static final DataFlavor IndexFlavor = new DataFlavor("silverpeas/index",
      "Silverpeas index");
  public static final DataFlavor SilverpeasKeyDataFlavor = new DataFlavor(
      "silverpeas/keydata", "Silverpeas keydata");
  protected ArrayList<DataFlavor> supportedFlavorsList = new ArrayList<>();
  protected boolean selected = true;
  private boolean isCut = false;

  protected ClipboardSelection() {
    supportedFlavorsList.add(IndexFlavor);
    supportedFlavorsList.add(SilverpeasKeyDataFlavor);
  }

  /**
   * Add a new DataFlavor to the list of supported DataFlavors.
   * @param parFlavor the data flavor to support
   */
  protected void addFlavor(DataFlavor parFlavor) {
    supportedFlavorsList.add(parFlavor);
  }

  /**
   * Indicates if a DataFlavor is in the list of supported DataFlavors.
   * @param parFlavor the data flavor to check.
   * @return true if the data flavor is supported, false otherwise.
   */
  @Override
  public boolean isDataFlavorSupported(DataFlavor parFlavor) {
    if (parFlavor == null) {
      return false;
    }
    return supportedFlavorsList.contains(parFlavor);
  }

  @Override
  public synchronized DataFlavor[] getTransferDataFlavors() {
    return supportedFlavorsList
        .toArray(new DataFlavor[0]);
  }

  /**
   * Gets the data stored into the specified DataFlavor.
   * @param parFlavor the data stored in the specified data flavor.
   * @return the data in the given data flavor: it is either an {@link IndexEntry} instance for
   * an {@link ClipboardSelection#IndexFlavor} or a {@link SilverpeasKeyData} object for a
   * {@link ClipboardSelection#SilverpeasKeyDataFlavor}.
   * @throws UnsupportedFlavorException if the specified data flavor isn't supported.
   */
  @Override
  @NonNull
  public synchronized Object getTransferData(DataFlavor parFlavor)
      throws UnsupportedFlavorException {
    if (IndexFlavor.equals(parFlavor)) {
      return getIndexEntry();
    } else if (SilverpeasKeyDataFlavor.equals(parFlavor)) {
      return getKeyData();
    } else {
      throw new UnsupportedFlavorException(parFlavor);
    }
  }

  public boolean isSelected() {
    return selected;
  }

  public void setSelected(boolean setIt) {
    selected = setIt;
  }

  @Override
  public void lostOwnership(Clipboard parClipboard, Transferable parTransferable) {
  }

  protected abstract IndexEntry getIndexEntry();

  protected abstract SilverpeasKeyData getKeyData();

  public boolean isCut() {
    return isCut;
  }

  public void setCut(boolean isCut) {
    this.isCut = isCut;
  }

}
