/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.admin.user;

import org.silverpeas.core.admin.persistence.OrganizationSchema;
import org.silverpeas.core.admin.persistence.UserRoleRow;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.dao.RoleDAO;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.persistence.jdbc.DBUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
@Transactional(Transactional.TxType.MANDATORY)
public class ProfiledObjectManager {

  @Inject
  private ProfileInstManager profileInstManager;
  @Inject
  private OrganizationSchema organizationSchema;
  @Inject
  private RoleDAO roleDAO;

  /**
   * Constructor
   */
  protected ProfiledObjectManager() {
  }

  public List<ProfileInst> getProfiles(int objectId, String objectType, int componentId)
      throws AdminException {
    List<ProfileInst> profiles = new ArrayList<>();

    String[] asProfileIds = null;
    try {
      // Get the profiles
      asProfileIds = organizationSchema.userRole().getAllUserRoleIdsOfObject(
          objectId, objectType, componentId);
    } catch (Exception e) {
      throw new AdminException(e.getMessage(), e);
    }

    // Insert the profileInst in the componentInst
    for (int nI = 0; asProfileIds != null && nI < asProfileIds.length; nI++) {
      ProfileInst profileInst = profileInstManager.getProfileInst(asProfileIds[nI]);
      profiles.add(profileInst);
    }

    return profiles;
  }

  public String[] getUserProfileNames(int objectId, String objectType, int componentId, int userId,
      List<String> groupIds) throws AdminException {
    if (objectId == -1) {
      return new String[0];
    }

    Connection con = null;
    try {
      con = DBUtil.openConnection();

      List<UserRoleRow> roles =
          roleDAO.getRoles(con, objectId, objectType, componentId, groupIds, userId);
      List<String> roleNames = new ArrayList<>();

      for (UserRoleRow role : roles) {
        roleNames.add(role.roleName);
      }

      return roleNames.toArray(new String[roleNames.size()]);

    } catch (Exception e) {
      throw new AdminException(e.getMessage(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  public Map<Integer, List<String>> getUserProfileNames(String objectType, int componentId,
      int userId, List<String> groupIds) throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();

      List<UserRoleRow> roles =
          roleDAO.getRoles(con, -1, objectType, componentId, groupIds, userId);
      Map<Integer, List<String>> objectProfiles = new HashMap<>(roles.size());

      Collections.sort(roles, Comparator.comparingInt(o -> o.objectId));

      int currentObjectId = -1;
      List<String> roleNames = new ArrayList<>();
      for (UserRoleRow role : roles) {
        if (currentObjectId != role.objectId) {
          currentObjectId = role.objectId;
          roleNames = new ArrayList<>();
          objectProfiles.put(currentObjectId, roleNames);
        }
        roleNames.add(role.roleName);
      }

      return objectProfiles;

    } catch (Exception e) {
      throw new AdminException(e.getMessage(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  public List<ProfileInst> getProfiles(int componentId)
      throws AdminException {
    List<ProfileInst> profiles = new ArrayList<>();

    String[] asProfileIds = null;
    try {
      // Get the profiles
      asProfileIds = organizationSchema.userRole()
          .getAllObjectUserRoleIdsOfInstance(componentId);
    } catch (Exception e) {
      throw new AdminException(e.getMessage(), e);
    }

    for (int nI = 0; asProfileIds != null && nI < asProfileIds.length; nI++) {
      ProfileInst profileInst = profileInstManager.getProfileInst(asProfileIds[nI]);
      profiles.add(profileInst);
    }

    return profiles;
  }
}