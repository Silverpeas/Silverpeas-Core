/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.comment.model;

import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.SilverpeasContent;

import java.util.Date;

/**
 * This object contains the description of document
 * @author Georgy Shakirin
 * @version 1.0
 */
public class Comment implements SilverpeasContent {

  private static final long serialVersionUID = 3738544756345055840L;
  public static final String CONTRIBUTION_TYPE = "Comment";
  private CommentPK pk;
  private String resourceType;
  private WAPrimaryKey foreign_key;
  private int owner_id;
  private String message;
  private Date creation_date;
  private Date modification_date;
  private User ownerDetail;

  private void init(CommentPK pk, String resourceType, WAPrimaryKey foreign_key, int owner_id,
      String message, Date creation_date, Date modification_date) {
    this.pk = pk;
    this.resourceType = resourceType;
    this.foreign_key = foreign_key;
    this.owner_id = owner_id;
    this.message = message;
    this.creation_date = new Date(creation_date.getTime());
    if (modification_date != null) {
      this.modification_date = new Date(modification_date.getTime());
    }
  }

  public Comment(CommentPK pk, String resourceType, WAPrimaryKey foreign_key, int owner_id,
      String owner, String message, Date creation_date,
      Date modification_date) {
    init(pk, resourceType, foreign_key, owner_id, message, creation_date,
        modification_date);
  }

  public Comment(CommentPK pk, String resourceType, WAPrimaryKey contentPk, String authorId,
      String message, Date creationDate, Date modificationDate) {
    init(pk, resourceType, contentPk, Integer.valueOf(authorId), message, creationDate,
        modificationDate);
  }

  public void setCommentPK(CommentPK pk) {
    this.pk = pk;
  }

  public CommentPK getCommentPK() {
    return this.pk;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  public String getResourceType() {
    return resourceType;
  }

  public void setForeignKey(WAPrimaryKey foreign_key) {
    this.foreign_key = foreign_key;
  }

  public WAPrimaryKey getForeignKey() {
    return this.foreign_key;
  }

  public int getOwnerId() {
    return this.owner_id;
  }

  public String getOwner() {
    if (getOwnerDetail() != null) {
      return getOwnerDetail().getDisplayedName();
    }
    return "";
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getMessage() {
    return this.message;
  }

  public void setCreationDate(Date creation_date) {
    this.creation_date = new Date(creation_date.getTime());
  }

  @Override
  public Date getCreationDate() {
    return new Date(this.creation_date.getTime());
  }

  public void setModificationDate(Date modification_date) {
    this.modification_date = new Date(modification_date.getTime());
  }

  @Override
  public User getLastModifier() {
    return getCreator();
  }

  @Override
  public Date getLastModificationDate() {
    Date date = null;
    if (this.modification_date != null) {
      date = new Date(this.modification_date.getTime());
    }
    return date;
  }

  public User getOwnerDetail() {
    return getCreator();
  }

  public void setOwnerDetail(User ownerDetail) {
    this.ownerDetail = ownerDetail;
  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();
    str.append("getCommentPK() = ").append(getCommentPK().toString()).append(
        ", \n");
    str.append("getResourceType() = ").append(getResourceType()).append(
        ", \n");
    str.append("getForeignKey() = ").append(getForeignKey().toString()).append(
        ", \n");
    str.append("getOwnerId() = ").append(getOwnerId()).append(", \n");
    str.append("getMessage() = ").append(getMessage())
        .append(", \n");
    str.append("getCreationDate() = ").append(getCreationDate())
        .append(", \n");
    str.append("getModificationDate() = ").append(
        getLastModificationDate());
    return str.toString();
  }

  @Override
  public User getCreator() {
    if (ownerDetail == null || !ownerDetail.isFullyDefined()) {
      ownerDetail = User.getById(String.valueOf(owner_id));
    }
    return ownerDetail;
  }

  @Override
  public String getTitle() {
    return "";
  }

  @Override
  public String getDescription() {
    return "";
  }

  @Override
  public String getId() {
    return pk.getId();
  }

  @Override
  public String getComponentInstanceId() {
    return pk.getInstanceId();
  }

  @Override
  public String getContributionType() {
    return CONTRIBUTION_TYPE;
  }
}