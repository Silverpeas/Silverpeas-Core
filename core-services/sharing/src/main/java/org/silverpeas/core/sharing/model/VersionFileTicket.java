/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.sharing.model;

import org.silverpeas.core.sharing.security.ShareableAccessControl;
import org.silverpeas.core.sharing.security.ShareableResource;
import org.silverpeas.core.sharing.security.ShareableVersionDocument;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.attachment.AttachmentException;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.HistorisedDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Date;

/**
 * Ticket for files with versions.
 */
@Entity
@DiscriminatorValue("Versionned")
public class VersionFileTicket extends Ticket {
  private static final long serialVersionUID = 7046398587440076818L;

  private static final VersionFileAccessControl accessControl = new VersionFileAccessControl();

  public VersionFileTicket(int sharedObjectId, String componentId, String creatorId,
      Date creationDate, Date endDate, int nbAccessMax) {
    super(sharedObjectId, componentId, creatorId, creationDate, endDate, nbAccessMax);
    this.sharedObjectType = VERSION_TYPE;
  }

  public VersionFileTicket(int sharedObjectId, String componentId, UserDetail creator,
      Date creationDate, Date endDate, int nbAccessMax) {
    super(sharedObjectId, componentId, creator, creationDate, endDate, nbAccessMax);
    this.sharedObjectType = VERSION_TYPE;
  }

  protected VersionFileTicket() {
    this.sharedObjectType = VERSION_TYPE;
  }

  public HistorisedDocument getDocument() {
    try {
      SimpleDocumentPK pk = new SimpleDocumentPK("" + getSharedObjectId(), getComponentId());
      pk.setOldSilverpeasId(getSharedObjectId());
      return (HistorisedDocument) AttachmentServiceProvider.getAttachmentService().
          searchDocumentById(pk, null);
    } catch (AttachmentException e) {
      SilverTrace.error("fileSharing", "Ticket.getDocument", "root.MSG_GEN_PARAM_VALUE", e);
    }
    return null;
  }

  @Override
  public ShareableResource<HistorisedDocument> getResource() {
    try {
      SimpleDocumentPK pk = new SimpleDocumentPK("" + getSharedObjectId(), getComponentId());
      pk.setOldSilverpeasId(getSharedObjectId());
      HistorisedDocument doc = (HistorisedDocument) AttachmentServiceProvider.getAttachmentService().
          searchDocumentById(pk, null);
      if(doc != null) {
        return new ShareableVersionDocument(getToken(),
            (HistorisedDocument) doc.getLastPublicVersion());
      }
    } catch (AttachmentException e) {
      SilverTrace.error("fileSharing", "Ticket.getDocument", "root.MSG_GEN_PARAM_VALUE", e);
    }
    return null;

  }

  @Override
  public ShareableAccessControl<VersionFileTicket, HistorisedDocument> getAccessControl() {
    return accessControl;
  }
}
