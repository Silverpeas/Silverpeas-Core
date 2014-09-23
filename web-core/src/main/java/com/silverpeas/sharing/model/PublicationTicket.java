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
package com.silverpeas.sharing.model;

import java.util.Date;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.silverpeas.sharing.security.ShareableAccessControl;
import com.silverpeas.sharing.security.ShareablePublication;
import com.silverpeas.sharing.security.ShareableResource;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.publication.control.PublicationBm;
import com.stratelia.webactiv.publication.model.PublicationDetail;
import com.stratelia.webactiv.publication.model.PublicationPK;

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
    PublicationBm publicationBm = EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME, PublicationBm.class);
    PublicationDetail publication = publicationBm.getDetail(new PublicationPK(String.valueOf(getSharedObjectId()),
        getComponentId()));
    if (publication != null) {
      return new ShareablePublication(getToken(), publication);
    }
    return null;
  }
}
