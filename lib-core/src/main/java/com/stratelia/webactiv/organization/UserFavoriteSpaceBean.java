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

package com.stratelia.webactiv.organization;

import com.stratelia.webactiv.persistence.SilverpeasBean;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;

public class UserFavoriteSpaceBean extends SilverpeasBean {

  /**
   * Generated serial version identifier
   */
  private static final long serialVersionUID = 8352658910643666026L;

  /**
   * User identifier
   */
  private int userId = -1;

  /**
   * Space identifier
   */
  private int spaceId = -1;

  /**
   * Default constructor
   */
  public UserFavoriteSpaceBean() {
  }

  public UserFavoriteSpaceBean(int userId, int spaceId) {
    this.userId = userId;
    this.spaceId = spaceId;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int value) {
    userId = value;
  }

  public int getSpaceId() {
    return spaceId;
  }

  public void setSpaceId(int spaceId) {
    this.spaceId = spaceId;
  }

  /*****************************************************************************/
  /**
   *
   */
  public int _getConnectionType() {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }

  /**
   *
   */
  public String _getTableName() {
    return "ST_UserFavoriteSpaces";
  }

}