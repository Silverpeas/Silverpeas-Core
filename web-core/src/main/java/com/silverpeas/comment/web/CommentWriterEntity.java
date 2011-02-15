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

/**
 * The comment writer entity is a user that has written a comment and that is exposed in the web as
 * an entity (a web entity). As such, it publishes only some od its attributes.
 */
public class CommentWriterEntity implements Serializable {
  private static final long serialVersionUID = 1L;
  private final UserDetail user;

  /**
   * Creates an new comment writer entity from the specified user.
   * @param user the user to entitify.
   * @return the comment writer.
   */
  public static CommentWriterEntity fromUser(final UserDetail user) {
    return new CommentWriterEntity(user);
  }

  /**
   * Gets the relative path of the user avatar.
   * @return the relative path of the URI refering the user avatar.
   */
  public String getAvatar() {
    return user.getAvatar();
  }

  /**
   * Gets the unique identifier of the writer.
   * @return the user identifier.
   */
  public String getId() {
    return user.getId();
  }

  /**
   * Gets the full name of the writer (both the first name and the last name).
   * @return the user full name.
   */
  public String getFullName() {
    return user.getDisplayedName();
  }

  private CommentWriterEntity(final UserDetail userDetail) {
    this.user = userDetail;
  }

}
