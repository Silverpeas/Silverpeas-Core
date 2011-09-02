/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.comment.model;

import com.silverpeas.SilverpeasContent;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * This object contains the description of document
 * @author Georgy Shakirin
 * @version 1.0
 */
public class Comment implements SilverpeasContent {

  private static final long serialVersionUID = 3738544756345055840L;
  private CommentPK pk;
  private WAPrimaryKey foreign_key;
  private int owner_id;
  private String message;
  private String creation_date;
  private String modification_date;
  private UserDetail ownerDetail;

  private void init(CommentPK pk, WAPrimaryKey foreign_key, int owner_id,
      String message, String creation_date, String modification_date) {
    this.pk = pk;
    this.foreign_key = foreign_key;
    this.owner_id = owner_id;
    this.message = message;
    this.creation_date = creation_date;
    this.modification_date = modification_date;
  }

  public Comment(CommentPK pk, WAPrimaryKey foreign_key, int owner_id,
      String owner, String message, String creation_date,
      String modification_date) {
    init(pk, foreign_key, owner_id, message, creation_date,
        modification_date);
  }

  public void setCommentPK(CommentPK pk) {
    this.pk = pk;
  }

  public CommentPK getCommentPK() {
    return this.pk;
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

  public void setCreationDate(String creation_date) {
    this.creation_date = creation_date;
  }

  public String getCreationDate() {
    return this.creation_date;
  }

  public void setModificationDate(String modification_date) {
    this.modification_date = modification_date;
  }

  public String getModificationDate() {
    return this.modification_date;
  }
  
  public UserDetail getOwnerDetail() {
    return ownerDetail;
  }

  public void setOwnerDetail(UserDetail ownerDetail) {
    this.ownerDetail = ownerDetail;
  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();
    str.append("getCommentPK() = ").append(getCommentPK().toString()).append(
        ", \n");
    str.append("getForeignKey() = ").append(getForeignKey().toString()).append(
        ", \n");
    str.append("getOwnerId() = ").append(getOwnerId()).append(", \n");
    str.append("getMessage() = ").append(getMessage())
        .append(", \n");
    str.append("getCreationDate() = ").append(getCreationDate())
        .append(", \n");
    str.append("getModificationDate() = ").append(
        getModificationDate());
    return str.toString();
  }

  @Override
  public UserDetail getCreator() {
    if (ownerDetail == null) {
      OrganizationController organizationController = getOrganizationController();
      ownerDetail = organizationController.getUserDetail(String.valueOf(owner_id));
    }
    return ownerDetail;
  }

  @Override
  public String getTitle() {
    return "";
  }

  @Override
  public String getURL() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public String getId() {
    return pk.getId();
  }

  @Override
  public String getComponentInstanceId() {
    return pk.getInstanceId();
  }
  
  private OrganizationController getOrganizationController() {
    return new OrganizationController();
  }
}