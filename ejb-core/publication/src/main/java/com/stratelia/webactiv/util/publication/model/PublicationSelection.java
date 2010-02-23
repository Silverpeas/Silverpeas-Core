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
package com.stratelia.webactiv.util.publication.model;

import com.silverpeas.util.clipboard.ClipboardSelection;
import com.silverpeas.util.clipboard.SKDException;
import com.silverpeas.util.clipboard.SilverpeasKeyData;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.Serializable;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

import com.stratelia.webactiv.util.indexEngine.model.IndexEntry;
import com.stratelia.webactiv.util.publication.info.model.InfoDetail;
import com.stratelia.webactiv.util.publication.info.model.ModelDetail;

public class PublicationSelection extends ClipboardSelection implements Serializable {

  private static final long serialVersionUID = -1169335280661356348L;
  static public DataFlavor PublicationDetailFlavor;
  static public DataFlavor CompletePublicationFlavor;
  static {
    try {
      PublicationDetailFlavor = new DataFlavor(
          Class
          .forName("com.stratelia.webactiv.util.publication.model.PublicationDetail"),
          "Publication");
      CompletePublicationFlavor = new DataFlavor(
          Class
          .forName("com.stratelia.webactiv.util.publication.model.CompletePublication"),
          "Publication");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  private PublicationDetail m_pub;
  private ModelDetail m_modelDetail;
  private InfoDetail m_infoDetail;

  /**
   * --------------------------------------------------------------------------
   * ------------------------------ Constructor
   */
  public PublicationSelection(PublicationDetail pub) {
    super();
    m_pub = pub;
    super.addFlavor(PublicationDetailFlavor);
    super.addFlavor(CompletePublicationFlavor);
  }

  public PublicationSelection(CompletePublication pub) {
    this(pub.getPublicationDetail());
    m_modelDetail = pub.getModelDetail();
    m_infoDetail = pub.getInfoDetail();
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
      if (parFlavor.equals(PublicationDetailFlavor))
        transferedData = m_pub;
      else if (parFlavor.equals(CompletePublicationFlavor))
        transferedData = new CompletePublication(m_pub, m_modelDetail,
            m_infoDetail);
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
    IndexEntry indexEntry;
    PublicationPK pubPK = m_pub.getPK();
    indexEntry = new IndexEntry(pubPK.getComponentName(), "Publication", m_pub
        .getPK().getId());
    indexEntry.setTitle(m_pub.getName());
    return indexEntry;
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------ Tranformation obligatoire en SilverpeasKeyData
   */
  public SilverpeasKeyData getKeyData() {
    SilverpeasKeyData keyData = new SilverpeasKeyData();

    keyData.setTitle(m_pub.getName());
    keyData.setAuthor(m_pub.getCreatorId());
    keyData.setCreationDate(m_pub.getCreationDate());
    keyData.setDesc(m_pub.getDescription());
    keyData.setText(m_pub.getContent());
    try {
      keyData.setProperty("BEGINDATE", m_pub.getBeginDate().toString());
      keyData.setProperty("ENDDATE", m_pub.getEndDate().toString());
    } catch (SKDException e) {
      SilverTrace.error("publication", "PublicationSelection.getKeyData",
          "publication.ERROR_KEY_DATA", e);
    }
    return keyData;
  }

}
