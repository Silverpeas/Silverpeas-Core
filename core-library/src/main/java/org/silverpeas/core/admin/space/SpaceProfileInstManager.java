/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.space;

import java.util.ArrayList;
import java.util.List;

import org.silverpeas.core.admin.persistence.AdminPersistenceException;
import org.silverpeas.core.admin.persistence.SpaceRow;
import org.silverpeas.core.admin.persistence.SpaceUserRoleRow;
import org.silverpeas.core.admin.domain.DomainDriverManager;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.exception.SilverpeasException;

import javax.inject.Singleton;

@Singleton
public class SpaceProfileInstManager {

  /**
   * Constructor
   */
  public SpaceProfileInstManager() {
  }

  /**
   * Get all the space profile Ids for the given user
   * @param domainManager
   * @param sUserId
   * @return spaceProfile ids
   * @throws AdminException
   */
  public String[] getSpaceProfileIdsOfUserType(DomainDriverManager domainManager, String sUserId)
      throws AdminException {
    try {
      domainManager.getOrganizationSchema();

      List<String> roleIds = new ArrayList<String>();

      //space user role
      SpaceUserRoleRow[] tabSpaceUserRole = domainManager.getOrganization().spaceUserRole
          .getDirectSpaceUserRolesOfUser(Integer.parseInt(sUserId));
      for (SpaceUserRoleRow role : tabSpaceUserRole) {
        roleIds.add(Integer.toString(role.id));
      }

      return roleIds.toArray(new String[roleIds.size()]);

    } catch (Exception e) {
      throw new AdminException("SpaceProfileInstManager.getSpaceProfileIdsOfUser",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_PROFILES", e);
    } finally {
      domainManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get all the space profile Ids for the given group
   * @param domainManager
   * @param groupId
   * @return spaceProfile ids
   * @throws AdminException
   */
  public String[] getSpaceProfileIdsOfGroupType(DomainDriverManager domainManager, String groupId)
      throws AdminException {
    try {
      domainManager.getOrganizationSchema();

      List<String> roleIds = new ArrayList<String>();

      //space group role
      SpaceUserRoleRow[] tabSpaceGroupRole = domainManager.getOrganization().spaceUserRole
          .getDirectSpaceUserRolesOfGroup(Integer.parseInt(groupId));
      for (SpaceUserRoleRow role : tabSpaceGroupRole) {
        roleIds.add(Integer.toString(role.id));
      }
      return roleIds.toArray(new String[roleIds.size()]);

    } catch (Exception e) {
      throw new AdminException("SpaceProfileInstManager.getSpaceProfileIdsOfGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_PROFILES", e);
    } finally {
      domainManager.releaseOrganizationSchema();
    }
  }

  /**
   * Create a new space profile instance in database
   *
   *
   * @param spaceProfileInst
   * @param domainManager
   * @param parentSpaceLocalId
   * @return
   * @throws AdminException
   */
  public String createSpaceProfileInst(SpaceProfileInst spaceProfileInst,
      DomainDriverManager domainManager, int parentSpaceLocalId) throws AdminException {
    try {
      // Create the spaceProfile node
      SpaceUserRoleRow newRole = makeSpaceUserRoleRow(spaceProfileInst);
      newRole.spaceId = parentSpaceLocalId;
      domainManager.getOrganization().spaceUserRole.createSpaceUserRole(newRole);
      String spaceProfileNodeId = idAsString(newRole.id);

      // Update the CSpace with the links TSpaceProfile-TGroup
      for (String groupId : spaceProfileInst.getAllGroups()) {
        domainManager.getOrganization().spaceUserRole.addGroupInSpaceUserRole(idAsInt(groupId),
            idAsInt(spaceProfileNodeId));
      }

      // Update the CSpace with the links TSpaceProfile-TUser
      for (String userId : spaceProfileInst.getAllUsers()) {
        domainManager.getOrganization().spaceUserRole.addUserInSpaceUserRole(idAsInt(userId),
            idAsInt(spaceProfileNodeId));
      }
      return spaceProfileNodeId;
    } catch (Exception e) {
      throw new AdminException("SpaceProfileInstManager.addSpaceProfileInst",
          SilverpeasException.ERROR, "admin.EX_ERR_ADD_SPACE_PROFILE",
          "space profile name: '" + spaceProfileInst.getName() + "'", e);
    }
  }

  /**
   * Get Space profile information with given id and creates a new SpaceProfileInst
   *
   * @param ddManager
   * @param spaceProfileId
   * @param parentSpaceLocalId
   * @return
   * @throws AdminException
   */
  public SpaceProfileInst getSpaceProfileInst(DomainDriverManager ddManager,
      String spaceProfileId, Integer parentSpaceLocalId) throws AdminException {
    if (parentSpaceLocalId == null) {
      try {
        ddManager.getOrganizationSchema();
        SpaceRow space = ddManager.getOrganization().space.getSpaceOfSpaceUserRole(idAsInt(
            spaceProfileId));
        if (space == null) {
          space = new SpaceRow();
        }
        parentSpaceLocalId = space.id;
      } catch (Exception e) {
        throw new AdminException("SpaceProfileInstManager.getSpaceProfileInst",
            SilverpeasException.ERROR, "admin.EX_ERR_GET_SPACE_PROFILE",
            "space profile Id: '" + spaceProfileId + "', space Id: '"
            + parentSpaceLocalId + "'", e);
      } finally {
        ddManager.releaseOrganizationSchema();
      }
    }

    try {
      ddManager.getOrganizationSchema();

      // Load the profile detail
      SpaceUserRoleRow spaceUserRole = ddManager.getOrganization().spaceUserRole.
          getSpaceUserRole(idAsInt(spaceProfileId));

      SpaceProfileInst spaceProfileInst = null;
      if (spaceUserRole != null) {
        // Set the attributes of the space profile Inst
        spaceProfileInst = spaceUserRoleRow2SpaceProfileInst(spaceUserRole);
        setUsersAndGroups(ddManager, spaceProfileInst);
      }
      return spaceProfileInst;
    } catch (Exception e) {
      throw new AdminException("SpaceProfileInstManager.getSpaceProfileInst",
          SilverpeasException.ERROR, "admin.EX_ERR_SET_SPACE_PROFILE",
          "space profile Id: '" + spaceProfileId + "', space Id: '"
          + parentSpaceLocalId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * get information for given id and store it in the given SpaceProfileInst object
   *
   * @param ddManager
   * @param spaceLocalId
   * @param roleName
   * @throws AdminException
   */
  public SpaceProfileInst getInheritedSpaceProfileInstByName(DomainDriverManager ddManager,
      int spaceLocalId, String roleName) throws AdminException {
    return getSpaceProfileInst(ddManager, spaceLocalId, roleName, true);
  }

  public SpaceProfileInst getSpaceProfileInstByName(DomainDriverManager ddManager,
      int spaceLocalId, String roleName) throws AdminException {
    return getSpaceProfileInst(ddManager, spaceLocalId, roleName, false);
  }

  private SpaceProfileInst getSpaceProfileInst(DomainDriverManager ddManager,
      int spaceLocalId, String roleName, boolean isInherited) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      int inherited = 0;
      if (isInherited) {
        inherited = 1;
      }
      // Load the profile detail
      SpaceUserRoleRow spaceUserRole = ddManager.getOrganization().spaceUserRole.
          getSpaceUserRole(spaceLocalId, roleName, inherited);

      SpaceProfileInst spaceProfileInst = null;
      if (spaceUserRole != null) {
        // Set the attributes of the space profile Inst
        spaceProfileInst = spaceUserRoleRow2SpaceProfileInst(spaceUserRole);
        setUsersAndGroups(ddManager, spaceProfileInst);
      }
      return spaceProfileInst;
    } catch (Exception e) {
      throw new AdminException("SpaceProfileInstManager.getInheritedSpaceProfileInst",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_SPACE_PROFILE",
          "spaceId = " + spaceLocalId + ", role = " + roleName, e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  private void setUsersAndGroups(DomainDriverManager ddManager, SpaceProfileInst spaceProfileInst)
      throws AdminPersistenceException {
    // Get the groups
    String[] asGroupIds = ddManager.getOrganization().group.
        getDirectGroupIdsInSpaceUserRole(idAsInt(spaceProfileInst.getId()));

    // Set the groups to the space profile
    if (asGroupIds != null) {
      for (String groupId : asGroupIds) {
        spaceProfileInst.addGroup(groupId);
      }
    }
    // Get the Users
    String[] asUsersIds = ddManager.getOrganization().user.getDirectUserIdsOfSpaceUserRole(idAsInt(
        spaceProfileInst.getId()));

    // Set the Users to the space profile
    if (asUsersIds != null) {
      for (String userId : asUsersIds) {
        spaceProfileInst.addUser(userId);
      }
    }
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
   * @param ddManager
   * @throws AdminException
   */
  public void deleteSpaceProfileInst(SpaceProfileInst spaceProfileInst,
      DomainDriverManager ddManager) throws AdminException {
    try {
      // delete the spaceProfile node
      ddManager.getOrganization().spaceUserRole.removeSpaceUserRole(idAsInt(spaceProfileInst
          .getId()));
    } catch (Exception e) {
      throw new AdminException("SpaceProfileInstManager.deleteSpaceProfileInst",
          SilverpeasException.ERROR, "admin.EX_ERR_DELETE_SPACEPROFILE", "space profile Id: '"
          + spaceProfileInst.getId() + "'", e);
    }
  }

  /**
   * Updates space profile instance
   *
   * @param spaceProfileInst
   * @param ddManager
   * @param spaceProfileInstNew
   * @return
   * @throws AdminException
   */
  public String updateSpaceProfileInst(SpaceProfileInst spaceProfileInst,
      DomainDriverManager ddManager, SpaceProfileInst spaceProfileInstNew)
      throws AdminException {
    List<String> alOldSpaceProfileGroup = new ArrayList<String>();
    List<String> alNewSpaceProfileGroup = new ArrayList<String>();
    List<String> alAddGroup = new ArrayList<String>();
    List<String> alRemGroup = new ArrayList<String>();
    List<String> alOldSpaceProfileUser = new ArrayList<String>();
    List<String> alNewSpaceProfileUser = new ArrayList<String>();
    List<String> alAddUser = new ArrayList<String>();
    List<String> alRemUser = new ArrayList<String>();

    try {
      // Compute the Old spaceProfile group list
      List<String> alGroup = spaceProfileInst.getAllGroups();
      for (String groupId : alGroup) {
        alOldSpaceProfileGroup.add(groupId);
      }

      // Compute the New spaceProfile group list
      alGroup = spaceProfileInstNew.getAllGroups();
      for (String groupId : alGroup) {
        alNewSpaceProfileGroup.add(groupId);
      }

      // Compute the remove group list
      for (String groupId : alOldSpaceProfileGroup) {
        if (!alNewSpaceProfileGroup.contains(groupId)) {
          alRemGroup.add(groupId);
        }
      }

      // Compute the add and stay group list
      for (String groupId : alNewSpaceProfileGroup) {
        if (!alOldSpaceProfileGroup.contains(groupId)) {
          alAddGroup.add(groupId);
        }
      }

      // Add the new Groups
      for (String groupId : alAddGroup) {
        // Create the links between the spaceProfile and the group
        ddManager.getOrganization().spaceUserRole.addGroupInSpaceUserRole(
            idAsInt(groupId), idAsInt(spaceProfileInst.getId()));
      }

      // Remove the removed groups
      for (String groupId : alRemGroup) {
        // delete the node link SpaceProfile_Group
        ddManager.getOrganization().spaceUserRole.removeGroupFromSpaceUserRole(
            idAsInt(groupId), idAsInt(spaceProfileInst.getId()));
      }

      // Compute the Old spaceProfile User list
      ArrayList<String> alUser = spaceProfileInst.getAllUsers();
      for (String userId : alUser) {
        alOldSpaceProfileUser.add(userId);
      }

      // Compute the New spaceProfile User list
      alUser = spaceProfileInstNew.getAllUsers();
      for (String userId : alUser) {
        alNewSpaceProfileUser.add(userId);
      }

      // Compute the remove User list
      for (String userId : alOldSpaceProfileUser) {
        if (!alNewSpaceProfileUser.contains(userId)) {
          alRemUser.add(userId);
        }
      }

      // Compute the add and stay User list
      for (String userId : alNewSpaceProfileUser) {
        if (!alOldSpaceProfileUser.contains(userId)) {
          alAddUser.add(userId);
        }
      }

      // Add the new Users
      for (String userId : alAddUser) {
        // Create the links between the spaceProfile and the User
        ddManager.getOrganization().spaceUserRole.addUserInSpaceUserRole(
            idAsInt(userId), idAsInt(spaceProfileInst.getId()));
      }

      // Remove the removed Users
      for (String userId : alRemUser) {
        // delete the node link SpaceProfile_User
        ddManager.getOrganization().spaceUserRole.removeUserFromSpaceUserRole(
            idAsInt(userId), idAsInt(spaceProfileInst.getId()));
      }

      // update the spaceProfile node
      SpaceUserRoleRow changedSpaceUserRole = makeSpaceUserRoleRow(spaceProfileInstNew);
      changedSpaceUserRole.id = idAsInt(spaceProfileInstNew.getId());
      ddManager.getOrganization().spaceUserRole.updateSpaceUserRole(changedSpaceUserRole);

      return idAsString(changedSpaceUserRole.id);
    } catch (Exception e) {
      throw new AdminException("SpaceProfileInstManager.updateSpaceProfileInst",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_SPACEPROFILE",
          "space profile Id: '" + spaceProfileInst.getId() + "'", e);
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
    if (id == null || id.length() == 0) {
      return -1; // the null id.
    }
    try {
      return Integer.parseInt(id);
    } catch (NumberFormatException e) {
      return -1; // the null id.
    }
  }

  /**
   * Convert int Id to String Id
   */
  static private String idAsString(int id) {
    return Integer.toString(id);
  }
}
