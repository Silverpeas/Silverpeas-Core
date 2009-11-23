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
package com.silverpeas.util.clipboard;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.Serializable;
import java.util.ArrayList;

import com.stratelia.webactiv.util.indexEngine.model.IndexEntry;

/**
 * Class declaration
 * @author
 */
public abstract class ClipboardSelection implements Serializable, ClipboardOwner, Transferable {
  static public DataFlavor IndexFlavor = new DataFlavor("silverpeas/index",
      "Silverpeas index");
  static public DataFlavor SilverpeasKeyDataFlavor = new DataFlavor(
      "silverpeas/keydata", "Silverpeas keydata");
  protected ArrayList supportedFlavorsList = new ArrayList();
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
   * --------------------------------------------------------------------------
   * ------------------------------
   */
  protected void addFlavor(DataFlavor parFlavor) {
    supportedFlavorsList.add(parFlavor);
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   */
  public boolean isDataFlavorSupported(DataFlavor parFlavor) {
    // return parFlavor.equals (IndexFlavor);
    boolean supported = true;

    try {
      int index = 0;

      while (!parFlavor
          .equals((DataFlavor) (supportedFlavorsList.get(index++))))
        ;
    } catch (IndexOutOfBoundsException e) {
      supported = false;
    }
    ;
    return supported;
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   */
  public synchronized DataFlavor[] getTransferDataFlavors() {
    return ((DataFlavor[]) (supportedFlavorsList.toArray()));
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   */
  public synchronized Object getTransferData(DataFlavor parFlavor)
      throws UnsupportedFlavorException {
    if (parFlavor.equals(IndexFlavor)) {
      return getIndexEntry();
    } else if (parFlavor.equals(SilverpeasKeyDataFlavor)) {
      return getKeyData();
    } else {
      throw new UnsupportedFlavorException(IndexFlavor);
    }
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   */
  public boolean isSelected() {
    return selected;
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   */
  public void setSelected(boolean setIt) {
    selected = setIt;
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   */
  public void lostOwnership(Clipboard parClipboard, Transferable parTransferable) {
    // System.out.println ("Lost ownership");
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   */
  abstract protected IndexEntry getIndexEntry();

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   */
  abstract protected SilverpeasKeyData getKeyData();

  public boolean isCutted() {
    return isCutted;
  }

  public void setCutted(boolean isCutted) {
    this.isCutted = isCutted;
  }

}
