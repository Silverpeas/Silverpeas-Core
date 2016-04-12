/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.core.workflow.engine.user;

import org.silverpeas.core.workflow.api.user.User;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.AdministrationServiceProvider;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;

import java.util.List;

/**
 * A User implementation built upon the silverpeas user management system.
 */
public final class UserImpl implements User {
  /**
   * A UserImpl is a facade to silverpeas UserDetail
   */
  private UserDetail userDetail = null;

  /**
   * A UserImpl is a facade to silverpeas UserFull too only loaded on demand
   */
  private UserFull userFull = null;

  private List<String> groupIds = null;

  /**
   * UserImpl is built from a UserDetail and admin .
   */
  public UserImpl(UserDetail userDetail) {
    this.userDetail = userDetail;
  }

  /**
   * Returns the user id
   */
  public String getUserId() {
    return userDetail.getId();
  }

  /**
   * Returns the user full name (firstname lastname)
   */
  public String getFullName() {
    return userDetail.getFirstName() + " " + userDetail.getLastName();
  }

  /**
   * returns all the known info for an user; Each returned value can be used as a parameter to the
   * User method getInfo().
   */
  static public String[] getUserInfoNames() {
    return infoNames;
  }

  static private String[] infoNames = { "bossId" };

  /**
   * Returns the named info
   */
  public String getInfo(String infoName) {
    if (userFull == null) {

      try {
        userFull = AdministrationServiceProvider.getAdminService().getUserFull(getUserId());
      } catch (AdminException e) {
        return "";
      }

      if (userFull == null) {
        return "";
      }
    }

    return userFull.getValue(infoName);
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 29 * hash + (this.getUserId() != null ? this.getUserId().hashCode() : 0);
    return hash;
  }

  /**
   * compare this user with another
   * @return true if two users are the same
   */
  @Override
  public boolean equals(Object user) {
    if (user == null || !(user instanceof UserImpl)) {
      return false;
    }
    return this.getUserId().equals(((UserImpl) user).getUserId());
  }

  @Override
  public List<String> getGroupIds() {
    return groupIds;
  }

  public void setGroupIds(List<String> groupIds) {
    this.groupIds = groupIds;
  }
}
