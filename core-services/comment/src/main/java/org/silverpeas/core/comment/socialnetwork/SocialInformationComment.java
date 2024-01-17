/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.comment.socialnetwork;

import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.socialnetwork.model.AbstractSocialInformation;
import org.silverpeas.core.socialnetwork.model.SocialInformationType;

import java.util.Objects;

public class SocialInformationComment extends AbstractSocialInformation {

  private final Comment comment;

  /**
   * Constructor with one param
   * @param comment a comment
   */
  public SocialInformationComment(Comment comment) {
    super(comment.getIdentifier().toReference());
    this.comment = comment;
    setAuthor(comment.getCreatorId());
    if (comment.getLastUpdateDate().equals(comment.getCreationDate())) {
      setDate(comment.getCreationDate());
      setUpdated(false);
    } else {
      setDate(comment.getLastUpdateDate());
      setUpdated(true);
    }
    String instanceId = comment.getComponentInstanceId();
    SocialInformationType type = SocialInformationType.COMMENTPUBLICATION;
    if (instanceId.startsWith("blog")) {
      type = SocialInformationType.COMMENTPOST;
    } else if (instanceId.startsWith("quickinfo")) {
      type = SocialInformationType.COMMENTNEWS;
    } else if (instanceId.startsWith("gallery")) {
      type = SocialInformationType.COMMENTMEDIA;
    }
    setType(type.toString());
    setDescription(comment.getMessage());
  }

  /**
   * compare to SocialInformationComment if are equals or not
   * @param obj the other {@link SocialInformationComment} object.
   * @return true if they are equals, false otherwise.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final SocialInformationComment other = (SocialInformationComment) obj;

    if (!Objects.equals(this.type, other.type)) {
      return false;
    }
    if (!Objects.equals(this.author, other.author)) {
      return false;
    }
    if (!Objects.equals(this.date, other.date)) {
      return false;
    }
    if (!Objects.equals(this.url, other.url)) {
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
    return this.getDescription() == null ? other.getDescription() == null :
        this.getDescription().equals(other.getDescription());
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

  public Comment getComment() {
    return comment;
  }
}
