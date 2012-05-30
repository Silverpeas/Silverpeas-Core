/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.sharing.model;

import com.silverpeas.sharing.security.ShareableAccessControl;
import com.silverpeas.sharing.security.ShareableAttachment;
import com.silverpeas.sharing.security.ShareableResource;
import com.silverpeas.sharing.services.SimpleFileAccessControl;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.util.UuidPk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Date;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

/**
 * Ticket for attached files.
 */
@Entity
@DiscriminatorValue("Attachment")
public class SimpleFileTicket extends Ticket {

  private static final long serialVersionUID = 1L;
  private static final SimpleFileAccessControl accessControl = new SimpleFileAccessControl();

  protected SimpleFileTicket() {
    this.sharedObjectType = FILE_TYPE;
  }

  public SimpleFileTicket(int sharedObjectId, String componentId, UserDetail creator,
      Date creationDate, Date endDate, int nbAccessMax) {
    super(sharedObjectId, componentId, creator, creationDate, endDate, nbAccessMax);
    this.sharedObjectType = FILE_TYPE;
  }

  public SimpleFileTicket(int sharedObjectId, String componentId, String creatorId,
      Date creationDate, Date endDate, int nbAccessMax) {
    super(sharedObjectId, componentId, creatorId, creationDate, endDate, nbAccessMax);
    this.sharedObjectType = FILE_TYPE;
  }

  @Override
  public ShareableAccessControl getAccessControl() {
    return accessControl;
  }

  public SimpleFileTicket(String key, int sharedObjectId, String componentId, UserDetail creator,
      Date creationDate, Date endDate, int nbAccessMax) {
    super(sharedObjectId, componentId, creator, creationDate, endDate, nbAccessMax);
    this.token = new UuidPk(key);
  }

  @Override
  public ShareableResource<SimpleDocument> getResource() {
    return new ShareableAttachment(getToken(), AttachmentServiceFactory.getAttachmentService().
        searchAttachmentById(new SimpleDocumentPK(String.valueOf(getSharedObjectId()),
        getComponentId()), null));
  }
}
