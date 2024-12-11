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
package org.silverpeas.core.comment.model;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.SilverpeasContent;
import org.silverpeas.kernel.bundle.SettingBundle;

import java.util.Date;

import static org.silverpeas.kernel.bundle.ResourceLocator.getSettingBundle;

/**
 * A comment on a given user contribution.
 */
public class Comment implements SilverpeasContent {

  private static final long serialVersionUID = 3738544756345055840L;
  private static final SettingBundle settingBundle =
      getSettingBundle("org.silverpeas.util.comment.Comment");
  public static final String CONTRIBUTION_TYPE = "Comment";
  private final CommentId id;
  private final String resourceType;
  private final ResourceReference resource;
  private String message;
  private final Date creationDate;
  private Date updateDate;
  private final String authorId;

  /**
   * Constructs a comment about the given resource and written by the specified author at the given
   * date.
   * @param id the unique identifier of the comment.
   * @param authorId the unique identifier of the author.
   * @param resourceType the type of the commented resource.
   * @param resource a reference to the commented resource.
   * @param creationDate the date at which the comment has been written.
   */
  public Comment(CommentId id, String authorId, String resourceType, ResourceReference resource,
      Date creationDate) {
    this.id = id;
    this.resourceType = resourceType;
    this.resource = resource;
    this.authorId = authorId;
    this.creationDate = creationDate;
    this.updateDate = creationDate;
  }

  @Override
  public CommentId getIdentifier() {
    return this.id;
  }

  /**
   * Gets the type of the resource commented out by this comment.
   * @return the type of the commented resource.
   */
  public String getResourceType() {
    return resourceType;
  }

  /**
   * Gets a reference to the resource commented out by this comment.
   * @return a reference to the commented resource.
   */
  public ResourceReference getResourceReference() {
    return resource;
  }

  public String getCreatorId() {
    return this.authorId;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getMessage() {
    return this.message;
  }

  @Override
  public Date getCreationDate() {
    return this.creationDate == null ? new Date() : new Date(this.creationDate.getTime());
  }

  public void setLastUpdateDate(Date modificationDate) {
    if (modificationDate != null) {
      this.updateDate = new Date(modificationDate.getTime());
    }
  }

  @Override
  public Date getLastUpdateDate() {
    return this.updateDate;
  }

  @Override
  public User getLastUpdater() {
    return getCreator();
  }

  @Override
  public User getCreator() {
    return User.getById(String.valueOf(authorId));
  }


  @Override
  public String getId() {
    return id.getLocalId();
  }

  @Override
  public String getComponentInstanceId() {
    return id.getComponentInstanceId();
  }

  @Override
  public String getContributionType() {
    return CONTRIBUTION_TYPE;
  }

  @Override
  public boolean canBeModifiedBy(final User user) {
    return !user.isAnonymous() && !user.isAccessGuest() && (user.getId().equals(authorId) ||
        (settingBundle.getBoolean("AdminAllowedToUpdate", true) &&
            user.isPlayingAdminRole(getComponentInstanceId())));
  }
}