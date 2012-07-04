/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.stratelia.webactiv.organization;

import java.io.Serializable;

public class UserFavoriteSpaceVO implements Serializable {

  /**
   * Generated serial version identifier
   */
  private static final long serialVersionUID = 988654463271541068L;

  /**
   * User identifier
   */
  private int userId = -1;

  /**
   * Space identifier
   */
  private int spaceId = -1;

  /**
   * Default UserFavoriteSpaceVO constructor
   */
  public UserFavoriteSpaceVO() {
  }

  /**
   * Default UserFavoriteSpaceVO constructor
   * @param userId
   * @param spaceId
   */
  public UserFavoriteSpaceVO(int userId, int spaceId) {
    this.userId = userId;
    this.spaceId = spaceId;
  }

  /*
   * GETTER and SETTER
   */
  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public int getSpaceId() {
    return spaceId;
  }

  public void setSpaceId(int spaceId) {
    this.spaceId = spaceId;
  }
}
