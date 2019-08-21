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
package org.silverpeas.core.admin.space;

import org.silverpeas.core.admin.persistence.OrganizationSchema;
import org.silverpeas.core.admin.persistence.SpaceUserRoleRow;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.GroupManager;
import org.silverpeas.core.admin.user.UserManager;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

import static org.silverpeas.core.SilverpeasExceptionMessages.*;

@Singleton
@Transactional(Transactional.TxType.MANDATORY)
public class SpaceProfileInstManager {

  public static final String SPACE_PROFILE = "space profile";
  @Inject
  private OrganizationSchema organizationSchema;

  /**
   * Constructor
   */
  protected SpaceProfileInstManager() {
  }

  /**
   * Get all the space profile Ids for the given user
   * @param sUserId
   * @return spaceProfile ids
   * @throws AdminException
   */
  public String[] getSpaceProfileIdsOfUserType(String sUserId)
      throws AdminException {
    try {
      List<String> roleIds = new ArrayList<>();

      //space user role
      SpaceUserRoleRow[] tabSpaceUserRole = organizationSchema.spaceUserRole()
          .getDirectSpaceUserRolesOfUser(Integer.parseInt(sUserId));
      for (SpaceUserRoleRow role : tabSpaceUserRole) {
        roleIds.add(Integer.toString(role.id));
      }

      return roleIds.toArray(new String[roleIds.size()]);

    } catch (Exception e) {
      throw new AdminException(failureOnGetting("space profiles of user", sUserId), e);
    }
  }

  /**
   * Get all the space profile Ids for the given group
   * @param groupId
   * @return spaceProfile ids
   * @throws AdminException
   */
  public String[] getSpaceProfileIdsOfGroupType(String groupId)
      throws AdminException {
    try {
      List<String> roleIds = new ArrayList<>();

      //space group role
      SpaceUserRoleRow[] tabSpaceGroupRole = organizationSchema.spaceUserRole()
          .getDirectSpaceUserRolesOfGroup(Integer.parseInt(groupId));
      for (SpaceUserRoleRow role : tabSpaceGroupRole) {
        roleIds.add(Integer.toString(role.id));
      }
      return roleIds.toArray(new String[roleIds.size()]);

    } catch (Exception e) {
      throw new AdminException(failureOnGetting("space profiles of group", groupId), e);
    }
  }

  /**
   * Create a new space profile instance in database
   *
   *
   * @param spaceProfileInst
   * @param parentSpaceLocalId
   * @return
   * @throws AdminException
   */
  public String createSpaceProfileInst(SpaceProfileInst spaceProfileInst, int parentSpaceLocalId)
      throws AdminException {
    try {
      // Create the spaceProfile node
      SpaceUserRoleRow newRole = makeSpaceUserRoleRow(spaceProfileInst);
      newRole.spaceId = parentSpaceLocalId;
      organizationSchema.spaceUserRole().createSpaceUserRole(newRole);
      String spaceProfileNodeId = idAsString(newRole.id);

      // Update the CSpace with the links TSpaceProfile-TGroup
      for (String groupId : spaceProfileInst.getAllGroups()) {
        organizationSchema.spaceUserRole().addGroupInSpaceUserRole(idAsInt(groupId),
            idAsInt(spaceProfileNodeId));
      }

      // Update the CSpace with the links TSpaceProfile-TUser
      for (String userId : spaceProfileInst.getAllUsers()) {
        organizationSchema.spaceUserRole().addUserInSpaceUserRole(idAsInt(userId),
            idAsInt(spaceProfileNodeId));
      }
      return spaceProfileNodeId;
    } catch (Exception e) {
      throw new AdminException(failureOnAdding(SPACE_PROFILE, spaceProfileInst.getName()), e);
    }
  }

  /**
   * Get Space profile information with given id and creates a new SpaceProfileInst
   *
   * @param spaceProfileId
   * @return
   * @throws AdminException
   */
  public SpaceProfileInst getSpaceProfileInst(String spaceProfileId) throws AdminException {
    try {
      // Load the profile detail
      SpaceUserRoleRow spaceUserRole = organizationSchema.spaceUserRole().
          getSpaceUserRole(idAsInt(spaceProfileId));

      SpaceProfileInst spaceProfileInst = null;
      if (spaceUserRole != null) {
        // Set the attributes of the space profile Inst
        spaceProfileInst = spaceUserRoleRow2SpaceProfileInst(spaceUserRole);
        setUsersAndGroups(spaceProfileInst);
      }
      return spaceProfileInst;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting(SPACE_PROFILE, spaceProfileId), e);
    }
  }

  /**
   * get information for given id and store it in the given SpaceProfileInst object
   *
   * @param spaceLocalId
   * @param roleName
   * @throws AdminException
   */
  public SpaceProfileInst getInheritedSpaceProfileInstByName(int spaceLocalId, String roleName)
      throws AdminException {
    return getSpaceProfileInst(spaceLocalId, roleName, true);
  }

  public SpaceProfileInst getSpaceProfileInstByName(int spaceLocalId, String roleName)
      throws AdminException {
    return getSpaceProfileInst(spaceLocalId, roleName, false);
  }

  private SpaceProfileInst getSpaceProfileInst(int spaceLocalId, String roleName,
      boolean isInherited) throws AdminException {
    try {
      int inherited = 0;
      if (isInherited) {
        inherited = 1;
      }
      // Load the profile detail
      SpaceUserRoleRow spaceUserRole = organizationSchema.spaceUserRole().
          getSpaceUserRole(spaceLocalId, roleName, inherited);

      SpaceProfileInst spaceProfileInst = null;
      if (spaceUserRole != null) {
        // Set the attributes of the space profile Inst
        spaceProfileInst = spaceUserRoleRow2SpaceProfileInst(spaceUserRole);
        setUsersAndGroups(spaceProfileInst);
      }
      return spaceProfileInst;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting(SPACE_PROFILE, roleName), e);
    }
  }

  private void setUsersAndGroups(SpaceProfileInst spaceProfileInst) throws AdminException {
    List<String> groupIds = GroupManager.get().getDirectGroupIdsInSpaceRole(spaceProfileInst.getId());
    spaceProfileInst.setGroups(groupIds);

    List<String> userIds = UserManager.get().getDirectUserIdsInSpaceRole(spaceProfileInst.getId());
    spaceProfileInst.setUsers(userIds);
  }

  private SpaceProfileInst spaceUserRoleRow2SpaceProfileInst(SpaceUserRoleRow spaceUserRole) {
    // Set the attributes of the space profile Inst
    SpaceProfileInst spaceProfileInst = new SpaceProfileInst();
    spaceProfileInst.setId(Integer.toString(spaceUserRole.id));
    spaceProfileInst.setName(spaceUserRole.roleName);
    spaceProfileInst.setLabel(spaceUserRole.name);
    spaceProfileInst.setDescription(spaceUserRole.description);
    spaceProfileInst.setSpaceFatherId(Integer.toString(spaceUserRole.spaceId));
    if (spaceUserRole.isInherited == 1) {
      spaceProfileInst.setInherited(true);
    }
    return spaceProfileInst;
  }

  /**
   * Deletes space profile instance from Silverpeas
   *
   * @param spaceProfileInst
   * @throws AdminException
   */
  public void deleteSpaceProfileInst(SpaceProfileInst spaceProfileInst) throws AdminException {
    try {
      // delete the spaceProfile node
      organizationSchema.spaceUserRole().removeSpaceUserRole(idAsInt(spaceProfileInst
          .getId()));
    } catch (Exception e) {
      throw new AdminException(failureOnDeleting(SPACE_PROFILE, spaceProfileInst.getId()), e);
    }
  }

  public void removeUserFromSpaceProfileInst(String userId, String spaceProdileId)
      throws AdminException {
    try {
      organizationSchema.spaceUserRole().removeUserFromSpaceUserRole(idAsInt(userId),
          idAsInt(spaceProdileId));
    } catch (Exception e) {
      throw new AdminException(failureOnDeleting("user from space profile", spaceProdileId), e);
    }
  }

  /**
   * Updates space profile instance
   *
   * @param spaceProfileInst
   * @param spaceProfileInstNew
   * @return
   * @throws AdminException
   */
  public String updateSpaceProfileInst(SpaceProfileInst spaceProfileInst,
      SpaceProfileInst spaceProfileInstNew)
      throws AdminException {
    try {
      // the groups in the previous state of the space profile
      List<String> alOldSpaceProfileGroup = spaceProfileInst.getAllGroups();

      // the groups in the new state of the space profile
      List<String> alNewSpaceProfileGroup = spaceProfileInstNew.getAllGroups();

      // Add the new groups in the profile
      for (String groupId : alNewSpaceProfileGroup) {
        if (!alOldSpaceProfileGroup.contains(groupId)) {
          // Create the links between the space profile and the group
          organizationSchema.spaceUserRole().addGroupInSpaceUserRole(
              idAsInt(groupId), idAsInt(spaceProfileInst.getId()));
        }
      }

      // Remove from the space profile the groups that are no more in the new state of the profile
      for (String groupId : alOldSpaceProfileGroup) {
        if (!alNewSpaceProfileGroup.contains(groupId)) {
          // delete the link between the space profile and the group
          organizationSchema.spaceUserRole().removeGroupFromSpaceUserRole(
              idAsInt(groupId), idAsInt(spaceProfileInst.getId()));
        }
      }

      // the users in the previous state of the space profile
      List<String> alOldSpaceProfileUser = spaceProfileInst.getAllUsers();

      // the users in the new state of the space profile
      List<String> alNewSpaceProfileUser = spaceProfileInstNew.getAllUsers();

      // Remove from the space profile the users that are no more in the new state of the profile
      for (String userId : alOldSpaceProfileUser) {
        if (!alNewSpaceProfileUser.contains(userId)) {
          // delete the link between the space profile and the user
          organizationSchema.spaceUserRole().removeUserFromSpaceUserRole(
              idAsInt(userId), idAsInt(spaceProfileInst.getId()));
        }
      }

      // Add the new users in the profile
      for (String userId : alNewSpaceProfileUser) {
        if (!alOldSpaceProfileUser.contains(userId)) {
          // Create the links between the space profile and the User
          organizationSchema.spaceUserRole().addUserInSpaceUserRole(
              idAsInt(userId), idAsInt(spaceProfileInst.getId()));
        }
      }

      // update the spaceProfile node
      SpaceUserRoleRow changedSpaceUserRole = makeSpaceUserRoleRow(spaceProfileInstNew);
      changedSpaceUserRole.id = idAsInt(spaceProfileInstNew.getId());
      organizationSchema.spaceUserRole().updateSpaceUserRole(changedSpaceUserRole);

      return idAsString(changedSpaceUserRole.id);
    } catch (Exception e) {
      throw new AdminException(failureOnUpdate(SPACE_PROFILE, spaceProfileInst.getId()), e);
    }
  }

  /**
   * Converts SpaceProfileInst to SpaceUserRoleRow
   */
  private SpaceUserRoleRow makeSpaceUserRoleRow(SpaceProfileInst spaceProfileInst) {
    SpaceUserRoleRow spaceUserRole = new SpaceUserRoleRow();

    spaceUserRole.id = idAsInt(spaceProfileInst.getId());
    spaceUserRole.roleName = spaceProfileInst.getName();
    spaceUserRole.name = spaceProfileInst.getLabel();
    spaceUserRole.description = spaceProfileInst.getDescription();
    if (spaceProfileInst.isInherited()) {
      spaceUserRole.isInherited = 1;
    }

    return spaceUserRole;
  }

  /**
   * Convert String Id to int Id
   */
  private int idAsInt(String id) {
    return StringUtil.asInt(id, -1);
  }

  /**
   * Convert int Id to String Id
   */
  private static String idAsString(int id) {
    return Integer.toString(id);
  }
}
