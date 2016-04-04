/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.admin.user;

import org.silverpeas.core.admin.user.dao.RoleDAO;
import org.silverpeas.core.admin.persistence.UserRoleRow;
import org.silverpeas.core.admin.domain.DomainDriverManager;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.exception.SilverpeasException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class ProfiledObjectManager {

  @Inject
  private ProfileInstManager m_ProfileInstManager;

  /**
   * Constructor
   */
  public ProfiledObjectManager() {
  }

  public List<ProfileInst> getProfiles(DomainDriverManager ddManager, int objectId,
      String objectType, int componentId) throws AdminException {
    List<ProfileInst> profiles = new ArrayList<ProfileInst>();

    String[] asProfileIds = null;
    try {
      ddManager.getOrganizationSchema();
      // Get the profiles
      asProfileIds = ddManager.getOrganization().userRole.getAllUserRoleIdsOfObject(
          objectId, objectType, componentId);
    } catch (Exception e) {
      throw new AdminException("ProfiledObjectManager.getProfiles",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_PROFILE", "objectId = "
          + objectId + ", componentId = " + componentId, e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }

    // Insert the profileInst in the componentInst
    for (int nI = 0; asProfileIds != null && nI < asProfileIds.length; nI++) {
      ProfileInst profileInst = m_ProfileInstManager.getProfileInst(ddManager, asProfileIds[nI]);
      profileInst.setObjectType(objectType);
      profiles.add(profileInst);
    }

    return profiles;
  }

  public String[] getUserProfileNames(int objectId, String objectType, int componentId, int userId,
      List<String> groupIds) throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();

      List<UserRoleRow> roles =
          RoleDAO.getRoles(con, objectId, objectType, componentId, groupIds, userId);
      List<String> roleNames = new ArrayList<String>();

      for (UserRoleRow role : roles) {
        roleNames.add(role.roleName);
      }

      return roleNames.toArray(new String[roleNames.size()]);

    } catch (Exception e) {
      throw new AdminException("ProfiledObjectManager.getUserProfileNames",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_PROFILES", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public List<ProfileInst> getProfiles(DomainDriverManager ddManager, int componentId)
      throws AdminException {
    List<ProfileInst> profiles = new ArrayList<ProfileInst>();

    String[] asProfileIds = null;
    try {
      ddManager.getOrganizationSchema();
      // Get the profiles
      asProfileIds = ddManager.getOrganization().userRole
          .getAllObjectUserRoleIdsOfInstance(componentId);
    } catch (Exception e) {
      throw new AdminException("ProfiledObjectManager.getProfiles",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_PROFILE",
          "componentId = " + componentId, e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }

    for (int nI = 0; asProfileIds != null && nI < asProfileIds.length; nI++) {
      ProfileInst profileInst = m_ProfileInstManager.getProfileInst(ddManager, asProfileIds[nI]);
      profiles.add(profileInst);
    }

    return profiles;
  }
}