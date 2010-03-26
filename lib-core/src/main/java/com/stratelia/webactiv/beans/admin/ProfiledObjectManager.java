/**
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
package com.stratelia.webactiv.beans.admin;

import java.util.ArrayList;
import java.util.List;

import com.stratelia.webactiv.organization.UserRoleRow;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class ProfiledObjectManager {
  static ProfileInstManager m_ProfileInstManager = new ProfileInstManager();

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
      asProfileIds = ddManager.organization.userRole.getAllUserRoleIdsOfObject(
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
      ProfileInst profileInst = m_ProfileInstManager.getProfileInst(ddManager,
          asProfileIds[nI], Integer.toString(componentId));
      profileInst.setObjectType(objectType);
      profiles.add(profileInst);
    }

    return profiles;
  }

  public String[] getUserProfileNames(DomainDriverManager ddManager,
      int objectId, String objectType, int componentId, int userId)
      throws AdminException {
    String[] profileNames = null;
    try {
      ddManager.getOrganizationSchema();
      // Get the profiles
      UserRoleRow[] roles = ddManager.organization.userRole
          .getRolesOfUserAndObject(objectId, objectType, componentId, userId);

      profileNames = new String[roles.length];
      for (int r = 0; r < roles.length; r++) {
        profileNames[r] = roles[r].roleName;
      }
    } catch (Exception e) {
      throw new AdminException("ProfiledObjectManager.getUserProfileNames",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_PROFILE", "objectId = "
          + objectId + ", componentId = " + componentId + ", userId = "
          + userId, e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
    return profileNames;
  }

  public boolean isObjectAvailable(DomainDriverManager ddManager, int userId,
      int objectId, String objectType, int componentId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      return ddManager.organization.userRole.isObjectAvailable(userId,
          componentId, objectId, objectType);
    } catch (Exception e) {
      throw new AdminException("ComponentInstManager.isComponentAvailable",
          SilverpeasException.ERROR,
          "admin.EX_ERR_GET_USER_AVAILABLE_COMPONENT_IDS", "userId = " + userId
          + ", componentId = " + componentId + ", objectId = " + objectId,
          e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  public List<ProfileInst> getProfiles(DomainDriverManager ddManager, int componentId)
      throws AdminException {
    List<ProfileInst> profiles = new ArrayList<ProfileInst>();

    String[] asProfileIds = null;
    try {
      ddManager.getOrganizationSchema();
      // Get the profiles
      asProfileIds = ddManager.organization.userRole
          .getAllObjectUserRoleIdsOfInstance(componentId);
    } catch (Exception e) {
      throw new AdminException("ProfiledObjectManager.getProfiles",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_PROFILE",
          "componentId = " + componentId, e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }

    for (int nI = 0; asProfileIds != null && nI < asProfileIds.length; nI++) {
      ProfileInst profileInst = m_ProfileInstManager.getProfileInst(ddManager,
          asProfileIds[nI], Integer.toString(componentId));
      // profileInst.setObjectType(objectType);
      profiles.add(profileInst);
    }

    return profiles;
  }
}