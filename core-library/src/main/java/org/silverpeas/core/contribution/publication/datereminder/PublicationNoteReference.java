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
package org.silverpeas.core.contribution.publication.datereminder;

import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.model.PublicationRuntimeException;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.persistence.EntityReference;

/**
 * An entity, which references a publication in Silverpeas, such publication being represented by a
 * <code>PublicationDetail</code> object.
 *
 * @author Cécile Bonin
 */
public class PublicationNoteReference extends EntityReference<PublicationDetail> {

  public PublicationNoteReference(String pubId) {
    super(pubId);
  }


  /**
   * Constructs a PublicationNoteReference from an publication.
   * @param publicationDetail the publication.
   * @return PublicationNoteReference
   */
  public static PublicationNoteReference fromPublicationDetail(final PublicationDetail publicationDetail) {
    return new PublicationNoteReference(publicationDetail.getPK().getId());
  }


  /**
   * Get the PublicationService interface.
   * @return PublicationService
   */
  private PublicationService getPublicationService() {
    try {
      return PublicationService.get();
    } catch (Exception e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Override
  public PublicationDetail getEntity() {
    PublicationPK pubPk = new PublicationPK(this.getId());
    return getPublicationService().getDetail(pubPk);
  }

}