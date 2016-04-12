/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.core.sharing.model;

import org.silverpeas.core.sharing.security.ShareableAccessControl;
import org.silverpeas.core.sharing.security.ShareablePublication;
import org.silverpeas.core.sharing.security.ShareableResource;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.util.ServiceProvider;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Date;

/**
 *
 * @author neysseric
 */
@Entity
@DiscriminatorValue("Publication")
public class PublicationTicket extends Ticket {

  private static final long serialVersionUID = 6661700474412230957L;

  private static final PublicationAccessControl accessControl = new PublicationAccessControl();

  public PublicationTicket(int sharedObjectId, String componentId, String creatorId, Date creationDate,
      Date endDate, int nbAccessMax) {
    super(sharedObjectId, componentId, creatorId, creationDate, endDate, nbAccessMax);
    this.sharedObjectType = PUBLICATION_TYPE;
  }

  public PublicationTicket(int sharedObjectId, String componentId, UserDetail creator, Date creationDate,
      Date endDate, int nbAccessMax) {
    super(sharedObjectId, componentId, creator, creationDate, endDate, nbAccessMax);
    this.sharedObjectType = PUBLICATION_TYPE;
  }

  protected PublicationTicket() {
    this.sharedObjectType = PUBLICATION_TYPE;
  }

  @Override
  public ShareableAccessControl<PublicationTicket, PublicationDetail> getAccessControl() {
    return accessControl;
  }

  @Override
  public ShareableResource<PublicationDetail> getResource() {
    PublicationService publicationService = ServiceProvider.getService(PublicationService.class);
    PublicationDetail publication = publicationService.getDetail(new PublicationPK(String.valueOf(getSharedObjectId()),
        getComponentId()));
    if (publication != null) {
      return new ShareablePublication(getToken(), publication);
    }
    return null;
  }
}
