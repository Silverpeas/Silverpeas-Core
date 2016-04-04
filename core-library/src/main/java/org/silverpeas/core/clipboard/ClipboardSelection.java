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

package org.silverpeas.core.clipboard;

import org.silverpeas.core.index.indexing.model.IndexEntry;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class declaration
 * @author
 */
public abstract class ClipboardSelection implements Serializable, ClipboardOwner, Transferable {

  private static final long serialVersionUID = 7296607705319157979L;
  final static public DataFlavor IndexFlavor = new DataFlavor("silverpeas/index",
      "Silverpeas index");
  final static public DataFlavor SilverpeasKeyDataFlavor = new DataFlavor(
      "silverpeas/keydata", "Silverpeas keydata");
  protected ArrayList<DataFlavor> supportedFlavorsList = new ArrayList<DataFlavor>();
  protected boolean selected = true;
  private boolean isCutted = false;

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   */
  public ClipboardSelection() {
    supportedFlavorsList.add(IndexFlavor);
    supportedFlavorsList.add(SilverpeasKeyDataFlavor);
  }

  /**
   * Add a new DataFlavor to the list of supported DataFlavors.
   * @param parFlavor
   */
  protected void addFlavor(DataFlavor parFlavor) {
    supportedFlavorsList.add(parFlavor);
  }

  /**
   * Indicates if a DataFlavor is in the list of supported DataFlavors.
   * @param parFlavor
   * @return true if the dataflavor is supported, false otherwise.
   */
  @Override
  public boolean isDataFlavorSupported(DataFlavor parFlavor) {
    if (parFlavor == null) {
      return false;
    }
    return supportedFlavorsList.contains(parFlavor);
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   */
  @Override
  public synchronized DataFlavor[] getTransferDataFlavors() {
    return supportedFlavorsList
        .toArray(new DataFlavor[supportedFlavorsList.size()]);
  }

  /**
   * Return the data stored into the DataFlavor.
   * @param parFlavor
   * @return
   */
  @Override
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

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   * @return
   */
  public boolean isSelected() {
    return selected;
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   * @param setIt
   */
  public void setSelected(boolean setIt) {
    selected = setIt;
  }

  /**
   * Does nothing.
   * @param parClipboard
   * @param parTransferable
   */
  @Override
  public void lostOwnership(Clipboard parClipboard, Transferable parTransferable) {
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   * @return
   */
  abstract protected IndexEntry getIndexEntry();

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   * @return
   */
  abstract protected SilverpeasKeyData getKeyData();

  public boolean isCutted() {
    return isCutted;
  }

  public void setCutted(boolean isCutted) {
    this.isCutted = isCutted;
  }

}
