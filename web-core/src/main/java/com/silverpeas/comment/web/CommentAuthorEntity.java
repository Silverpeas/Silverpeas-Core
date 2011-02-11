/*
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.comment.web;

import com.stratelia.webactiv.beans.admin.UserDetail;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import static com.silverpeas.util.StringUtil.*;

/**
 * The comment writer entity is a user that has written a comment and that is exposed in the web as
 * an entity (a web entity). As such, it publishes only some od its attributes.
 */
@XmlRootElement
public class CommentAuthorEntity implements Serializable {

  private static final long serialVersionUID = 1L;
  @XmlElement(required = true)
  private String fullName;
  @XmlElement(required = true)
  private String id;
  @XmlElement(defaultValue="")
  private String avatar;

  /**
   * Creates an new comment writer entity from the specified user.
   * @param user the user to entitify.
   * @return the comment writer.
   */
  public static CommentAuthorEntity fromUser(final UserDetail user) {
    return new CommentAuthorEntity(user);
  }

  /**
   * Gets the relative path of the user avatar.
   * @return the relative path of the URI refering the user avatar.
   */
  public String getAvatar() {
    return avatar;
  }

  /**
   * Gets the unique identifier of the writer.
   * @return the user identifier.
   */
  public String getId() {
    return id;
  }

  /**
   * Gets the full name of the writer (both the first name and the last name).
   * @return the user full name.
   */
  public String getFullName() {
    return fullName;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final CommentAuthorEntity other = (CommentAuthorEntity) obj;
    if (isDefined(id) && isDefined(other.getId())) {
      return id.equals(other.getId());
    } else {
      return fullName.equals(other.getFullName()) && avatar.equals(other.getAvatar());
    }
  }

  @Override
  public int hashCode() {
    int hash = 7;
    if (isDefined(id)) {
      hash = 59 * hash + this.id.hashCode();
    } else {
      hash = 59 * hash + (this.fullName != null ? this.fullName.hashCode() : 0);
      hash = 59 * hash + (this.avatar != null ? this.avatar.hashCode() : 0);
    }
    return hash;
  }



  /**
   * Gets a user detail from this object.
   * @return a UserDetail instance.
   */
  public UserDetail toUser() {
    UserDetail user = new UserDetail();
    user.setId(id);
    int separatorBetweenFirstAndLastName = fullName.indexOf(" ");
    user.setFirstName(fullName.substring(0,
        separatorBetweenFirstAndLastName));
    user.setLastName(fullName.substring(separatorBetweenFirstAndLastName + 1));
    return user;
  }

  private CommentAuthorEntity(final UserDetail userDetail) {
    this.fullName = userDetail.getDisplayedName();
    this.id = userDetail.getId();
    this.avatar = userDetail.getAvatar();
  }

  /**
   * Creates an empty comment writer.
   */
  protected CommentAuthorEntity() {}
}
