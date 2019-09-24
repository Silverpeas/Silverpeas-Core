/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.publication.model;

import org.silverpeas.core.clipboard.ClipboardSelection;
import org.silverpeas.core.clipboard.SKDException;
import org.silverpeas.core.clipboard.SilverpeasKeyData;
import org.silverpeas.core.index.indexing.model.IndexEntry;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.util.logging.SilverLogger;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.Serializable;

/**
 * Selection of a single publication in a given component instance.
 * <p>
 * In the case the selection is about a publication alias, the original publication is then
 * specified here as being the selected publication, not the alias. So, the component instance
 * attribute of the publication cannot be used to identify the component instance in which the
 * selection is done. This is why it is necessary to specify the component instance in which the
 * selection was done when constructing such this object.
 * </p>
 */
public class PublicationSelection extends ClipboardSelection implements Serializable {

  private static final long serialVersionUID = -1169335280661356348L;
  public static final DataFlavor PublicationDetailFlavor =
      new DataFlavor(PublicationDetail.class, "Publication");
  private final NodePK fatherPK;
  private PublicationDetail pub;

  /**
   * Constructs a new selection of a single publication in a given component instance.
   * @param pub the publication that is selected.
   * @param fatherPK the identifier of the resource that is the father of the publication. That is
   * to say the resource to which is linked directly the publication. If the father is the component
   * instance itself, then the {@link NodePK#getId()} can be null.
   */
  public PublicationSelection(PublicationDetail pub, NodePK fatherPK) {
    super();
    this.pub = pub;
    this.fatherPK = fatherPK;
    super.addFlavor(PublicationDetailFlavor);
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   */
  @Override
  public synchronized Object getTransferData(DataFlavor parFlavor)
      throws UnsupportedFlavorException {
    Object transferedData;
    try {
      transferedData = super.getTransferData(parFlavor);
    } catch (UnsupportedFlavorException e) {
      if (parFlavor.equals(PublicationDetailFlavor)) {
        transferedData = new TransferData(pub, fatherPK);
      } else {
        throw e;
      }
    }
    return transferedData;
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   */
  public IndexEntry getIndexEntry() {
    IndexEntry indexEntry;
    PublicationPK pubPK = pub.getPK();
    indexEntry = new IndexEntry(pubPK.getComponentName(), "Publication", pub
        .getPK().getId());
    indexEntry.setTitle(pub.getName());
    return indexEntry;
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------ Tranformation obligatoire en SilverpeasKeyData
   */
  public SilverpeasKeyData getKeyData() {
    SilverpeasKeyData keyData = new SilverpeasKeyData();

    keyData.setTitle(pub.getName());
    keyData.setAuthor(pub.getCreatorId());
    keyData.setCreationDate(pub.getCreationDate());
    keyData.setDesc(pub.getDescription());
    keyData.setText(pub.getContentPagePath());
    try {
      keyData.setProperty("BEGINDATE", pub.getBeginDate().toString());
      keyData.setProperty("ENDDATE", pub.getEndDate().toString());
      keyData.setProperty("INSTANCEID", fatherPK.getInstanceId());
      if (fatherPK.getId() != null) {
        keyData.setProperty("FATHERID", fatherPK.getId());
      }
    } catch (SKDException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return keyData;
  }

  /**
   * The data that is carried in the case of a publication selection.
   */
  public class TransferData {

    private final PublicationDetail publication;
    private final NodePK fatherPK;

    private TransferData(final PublicationDetail publication, final NodePK fatherPK) {
      this.publication = publication;
      this.fatherPK = fatherPK;
    }

    public PublicationDetail getPublicationDetail() {
      return publication;
    }

    public NodePK getFatherPK() {
      return fatherPK;
    }
  }

}
