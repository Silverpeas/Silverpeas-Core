/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.sharing.model;

import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.sharing.security.ShareableAccessControl;
import org.silverpeas.core.sharing.security.ShareablePublication;
import org.silverpeas.core.sharing.security.ShareableResource;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import java.util.Date;

/**
 *
 * @author neysseric
 */
@Entity
@DiscriminatorValue("Publication")
public class PublicationTicket extends Ticket {

  private static final long serialVersionUID = 6661700474412230957L;

  private PublicationTicket(TicketDetail detail) {
    super(detail);
    this.sharedObjectType = PUBLICATION_TYPE;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(TicketDetail detail) {
    return new Builder(detail);
  }

  public static class Builder extends Ticket.Builder<Builder> {
    private Builder() {
    }

    private Builder(TicketDetail detail) {
      super();
      this.detail.setSharedObjectId(detail.getSharedObjectId())
          .setComponentId(detail.getComponentId())
          .setCreatorId(detail.getCreatorId())
          .setCreationDate(detail.getCreationDate())
          .setEndDate(detail.getEndDate())
          .setNbAccessMax(detail.getNbAccessMax())
          .setSecurityCode(detail.getSecurityCode())
          .setSharedObjectType(detail.getSharedObjectType())
          .setToken(detail.getToken());
    }

    @Override
    protected Builder self() {
      return this;
    }

    @Override
    public PublicationTicket build() {
      return new PublicationTicket(buildDetail(PUBLICATION_TYPE));
    }
  }

  protected PublicationTicket() {
    this.sharedObjectType = PUBLICATION_TYPE;
  }

  @Override
  @Transient
  public ShareableAccessControl getAccessControl() {
    return new PublicationAccessControl(this);
  }

  @Override
  @Transient
  @SuppressWarnings("unchecked")
  public ShareableResource<PublicationDetail> getResource() {
    PublicationService publicationService = PublicationService.get();
    PublicationDetail publication = publicationService.getDetail(new PublicationPK(String.valueOf(getSharedObjectId()),
        getComponentId()));
    if (publication != null) {
      return new ShareablePublication(getToken(), publication);
    }
    return null;
  }
}
