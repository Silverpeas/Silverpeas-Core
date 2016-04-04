/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.contribution.publication.social;

import org.silverpeas.core.socialnetwork.model.AbstractSocialInformation;
import org.silverpeas.core.socialnetwork.model.SocialInformationType;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationWithStatus;

public class SocialInformationPublication extends AbstractSocialInformation {

  private final PublicationDetail publication;

  /**
   * Constructor with one param
   * @param publication
   */
  public SocialInformationPublication(PublicationWithStatus publication) {
    this.publication = publication.getPublication();
    if (publication.isUpdate()) {
      setAuthor(publication.getPublication().getUpdaterId());
      setDate(publication.getPublication().getUpdateDate());
    } else {
      setAuthor(publication.getPublication().getCreatorId());
      setDate(publication.getPublication().getCreationDate());
    }
    String instanceId = publication.getPublication().getInstanceId();
    SocialInformationType type = SocialInformationType.PUBLICATION;
    if (instanceId.startsWith("blog")) {
      type = SocialInformationType.POST;
    } else if (instanceId.startsWith("quickinfo")) {
      type = SocialInformationType.NEWS;
    } else if (instanceId.startsWith("bookmark")) {
      type = SocialInformationType.BOOKMARK;
    } else if (instanceId.startsWith("webSites")) {
      type = SocialInformationType.SITE;
    }
    setType(type.toString());
    setUrl(URLUtil.getSimpleURL(URLUtil.URL_PUBLI, publication.getPublication().getId(),
        instanceId, false));
    setUpdated(publication.isUpdate());
    setTitle(publication.getPublication().getTitle());
    setDescription(publication.getPublication().getDescription());
  }

  /**
   * compare to SocialInformationPublication if are iquals or not
   * @param obj
   * @return boolean
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final SocialInformationPublication other = (SocialInformationPublication) obj;

    if ((this.type == null) ? (other.type != null) : !this.type.equals(other.type)) {
      return false;
    }
    if ((this.author == null) ? (other.author != null) : !this.author.equals(other.author)) {
      return false;
    }
    if (this.date != other.date && (this.date == null || !this.date.equals(other.date))) {
      return false;
    }
    if ((this.url == null) ? (other.url != null) : !this.url.equals(other.url)) {
      return false;
    }
    if ((this.getTitle() == null) ? (other.getTitle() != null) : !this.getTitle().equals(other.
        getTitle())) {
      return false;
    }
    if ((this.getDescription() == null) ? (other.getDescription() != null) : !this.getDescription()
        .
        equals(other.getDescription())) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 43 * hash + (this.type != null ? this.type.hashCode() : 0);
    hash = 43 * hash + (this.author != null ? this.author.hashCode() : 0);
    hash = 43 * hash + (this.date != null ? this.date.hashCode() : 0);
    hash = 43 * hash + (this.url != null ? this.url.hashCode() : 0);
    return hash;
  }

  public PublicationDetail getPublication() {
    return publication;
  }
}