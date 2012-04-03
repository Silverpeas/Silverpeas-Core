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
import com.silverpeas.sharing.security.ShareableResource;
import com.silverpeas.sharing.security.ShareableVersionDocument;
import com.silverpeas.sharing.services.VersionFileAccessControl;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import com.stratelia.webactiv.beans.admin.UserDetail;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.rmi.RemoteException;
import java.util.Date;

/**
 * Ticket for files with versions.
 */
@Entity
@DiscriminatorValue("Versionned")
public class VersionFileTicket extends Ticket {

  private static final long serialVersionUID = 1L;
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

  public Document getDocument() {
    try {
      return new VersioningUtil().getDocument(new DocumentPK((int) getSharedObjectId(),
              getComponentId()));
    } catch (RemoteException e) {
      SilverTrace.error("fileSharing", "Ticket.getDocument", "root.MSG_GEN_PARAM_VALUE", e);
    }
    return null;
  }
  
  @Override
  public ShareableResource<Document> getResource() {
    Document doc = null;
    try {
      doc = new VersioningUtil().getDocument(new DocumentPK((int) getSharedObjectId(),
              getComponentId()));
    } catch (RemoteException e) {
      SilverTrace.error("fileSharing", "Ticket.getDocument", "root.MSG_GEN_PARAM_VALUE", e);
    }
    return new ShareableVersionDocument(getToken(), doc);
  }

  @Override
  public ShareableAccessControl<Document> getAccessControl() {
    return accessControl;
  }
}
