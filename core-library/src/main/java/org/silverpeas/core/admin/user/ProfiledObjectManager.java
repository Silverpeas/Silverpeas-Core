/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.admin.user;

import org.silverpeas.core.admin.ProfiledObjectId;
import org.silverpeas.core.admin.ProfiledObjectIds;
import org.silverpeas.core.admin.ProfiledObjectType;
import org.silverpeas.core.admin.persistence.OrganizationSchema;
import org.silverpeas.core.admin.persistence.UserRoleRow;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.dao.RoleDAO;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.Pair;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.*;

@Service
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

  public List<ProfileInst> getProfiles(ProfiledObjectId objectRef, int componentId)
      throws AdminException {
    List<ProfileInst> profiles = new ArrayList<>();

    String[] asProfileIds = null;
    try {
      // Get the profiles
      int objectId = Integer.parseInt(objectRef.getId());
      String objectType = objectRef.getType().getCode();
      asProfileIds = organizationSchema.userRole()
          .getAllUserRoleIdsOfObject(objectId, objectType, componentId);
    } catch (Exception e) {
      throw new AdminException(e.getMessage(), e);
    }

    // Insert the profileInst in the componentInst
    for (int nI = 0; asProfileIds != null && nI < asProfileIds.length; nI++) {
      ProfileInst profileInst = profileInstManager.getProfileInst(asProfileIds[nI], false);
      profiles.add(profileInst);
    }

    return profiles;
  }

  public String[] getUserProfileNames(final ProfiledObjectId objectRef, final int componentId,
      final int userId, final List<String> groupIds) throws AdminException {
    if (objectRef.isNotDefined()) {
      return new String[0];
    }
    try (final Connection con = DBUtil.openConnection()) {
      return roleDAO.getRoles(con, ProfiledObjectIds.fromProfileObjectId(objectRef), singleton(componentId), groupIds, userId).stream()
          .map(UserRoleRow::getRoleName)
          .distinct()
          .toArray(String[]::new);
    } catch (Exception e) {
      throw new AdminException(e.getMessage(), e);
    }
  }

  public Map<Pair<Integer, Integer>, Set<String>> getUserProfileNames(
      final ProfiledObjectIds profiledObjectIds, final Collection<Integer> componentIds,
      final int userId, final List<String> groupIds) throws AdminException {
    if (profiledObjectIds.isEmpty() || profiledObjectIds.stream().anyMatch(NodePK.UNDEFINED_NODE_ID::equals)) {
      return emptyMap();
    }
    try (final Connection con = DBUtil.openConnection()) {
      return roleDAO.getRoles(con, profiledObjectIds, componentIds, groupIds, userId).stream()
          .collect(groupingBy(r -> Pair.of(r.getInstanceId(), r.getObjectId()),
                   mapping(UserRoleRow::getRoleName, toSet())));
    } catch (Exception e) {
      throw new AdminException(e.getMessage(), e);
    }
  }

  public Map<String, List<String>> getUserProfileNames(ProfiledObjectType profiledObjectType,
      int componentId, int userId, List<String> groupIds) throws AdminException {
    try (final Connection con = DBUtil.openConnection()) {
      final List<UserRoleRow> roles =
          roleDAO.getRoles(con, ProfiledObjectIds.ofType(profiledObjectType), singleton(componentId), groupIds, userId);
      final Map<String, List<String>> objectProfiles = new HashMap<>(roles.size());
      roles.sort(Comparator.comparingInt(UserRoleRow::getObjectId));
      int currentObjectId = -1;
      List<String> roleNames = new ArrayList<>();
      for (UserRoleRow role : roles) {
        if (currentObjectId != role.getObjectId()) {
          currentObjectId = role.getObjectId();
          roleNames = new ArrayList<>();
          objectProfiles.put(String.valueOf(currentObjectId), roleNames);
        }
        roleNames.add(role.getRoleName());
      }
      return objectProfiles;
    } catch (Exception e) {
      throw new AdminException(e.getMessage(), e);
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
      ProfileInst profileInst = profileInstManager.getProfileInst(asProfileIds[nI], false);
      profiles.add(profileInst);
    }

    return profiles;
  }
}