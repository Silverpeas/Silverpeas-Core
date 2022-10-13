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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.workflow.engine.user;

import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.AdministrationServiceProvider;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.workflow.api.user.User;

import java.util.List;

import static java.util.Arrays.asList;
import static org.silverpeas.core.admin.service.OrganizationControllerProvider.getOrganisationController;

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
    return userDetail.getDisplayedName();
  }

  /**
   * Returns the named info
   */
  public String getInfo(String infoName) {
    if (userFull == null) {
      try {
        userFull = AdministrationServiceProvider.getAdminService().getUserFull(getUserId());
      } catch (AdminException e) {
        SilverLogger.getLogger(this).error(e);
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
    if (groupIds == null) {
      groupIds = asList(getOrganisationController().getAllGroupIdsOfUser(getUserId()));
    }
    return groupIds;
  }
}
