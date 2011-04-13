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
 * FLOSS exception.  You should have received a copy of the text describing
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

import com.stratelia.webactiv.organization.AdminPersistenceException;
import com.stratelia.webactiv.organization.SpaceRow;
import com.stratelia.webactiv.organization.SpaceUserRoleRow;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class SpaceProfileInstManager {

  /**
   * Constructor
   */
  public SpaceProfileInstManager() {
  }

  /**
   * Create a new space profile instance in database   * 
   * @param spaceProfileInst
   * @param DDManager
   * @param sFatherId
   * @return
   * @throws AdminException 
   */
  public String createSpaceProfileInst(SpaceProfileInst spaceProfileInst,
      DomainDriverManager DDManager, String sFatherId) throws AdminException {
    try {
      // Create the spaceProfile node
      SpaceUserRoleRow newRole = makeSpaceUserRoleRow(spaceProfileInst);
      newRole.spaceId = idAsInt(sFatherId);
      DDManager.organization.spaceUserRole.createSpaceUserRole(newRole);
      String sSpaceProfileNodeId = idAsString(newRole.id);

      // Update the CSpace with the links TSpaceProfile-TGroup
      for (int nI = 0; nI < spaceProfileInst.getNumGroup(); nI++) {
        DDManager.organization.spaceUserRole.addGroupInSpaceUserRole(idAsInt(spaceProfileInst.
            getGroup(nI)), idAsInt(sSpaceProfileNodeId));
      }

      // Update the CSpace with the links TSpaceProfile-TUser
      for (int nI = 0; nI < spaceProfileInst.getNumUser(); nI++) {
        DDManager.organization.spaceUserRole.addUserInSpaceUserRole(idAsInt(spaceProfileInst.getUser(
            nI)), idAsInt(sSpaceProfileNodeId));
      }

      return sSpaceProfileNodeId;
    } catch (Exception e) {
      throw new AdminException("SpaceProfileInstManager.addSpaceProfileInst",
          SilverpeasException.ERROR, "admin.EX_ERR_ADD_SPACE_PROFILE",
          "space profile name: '" + spaceProfileInst.getName() + "'", e);
    }
  }

  /**
   * Get Space profile information with given id and creates a new SpaceProfileInst
   * @param ddManager
   * @param sSpaceProfileId
   * @param sFatherId
   * @return
   * @throws AdminException 
   */
  public SpaceProfileInst getSpaceProfileInst(DomainDriverManager ddManager, String sSpaceProfileId,
      String sFatherId) throws AdminException {
    if (sFatherId == null) {
      try {
        ddManager.getOrganizationSchema();
        SpaceRow space = ddManager.organization.space.getSpaceOfSpaceUserRole(idAsInt(
            sSpaceProfileId));
        if (space == null) {
          space = new SpaceRow();
        }
        sFatherId = idAsString(space.id);
      } catch (Exception e) {
        throw new AdminException("SpaceProfileInstManager.getSpaceProfileInst",
            SilverpeasException.ERROR, "admin.EX_ERR_GET_SPACE_PROFILE",
            "space profile Id: '" + sSpaceProfileId + "', space Id: '"
            + sFatherId + "'", e);
      } finally {
        ddManager.releaseOrganizationSchema();
      }
    }

    try {
      ddManager.getOrganizationSchema();

      // Load the profile detail
      SpaceUserRoleRow spaceUserRole = ddManager.organization.spaceUserRole.getSpaceUserRole(idAsInt(
          sSpaceProfileId));
      
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
          "space profile Id: '" + sSpaceProfileId + "', space Id: '"
          + sFatherId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * get information for given id and store it in the given SpaceProfileInst object
   * @param ddManager
   * @param spaceId
   * @param roleName
   * @throws AdminException 
   */
  public SpaceProfileInst getInheritedSpaceProfileInstByName(DomainDriverManager ddManager,
      String spaceId, String roleName)
      throws AdminException {
    return getSpaceProfileInst(ddManager, spaceId, roleName, true);
  }
  
  public SpaceProfileInst getSpaceProfileInstByName(DomainDriverManager ddManager,
      String spaceId, String roleName)
      throws AdminException {
    return getSpaceProfileInst(ddManager, spaceId, roleName, false);
  }
  
  private SpaceProfileInst getSpaceProfileInst(DomainDriverManager ddManager,
      String spaceId, String roleName, boolean inherited)
      throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      
      int iInherited = 0;
      if (inherited) {
        iInherited = 1;
      }

      // Load the profile detail
      SpaceUserRoleRow spaceUserRole =
          ddManager.organization.spaceUserRole.getSpaceUserRole(idAsInt(spaceId), roleName, iInherited);

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
          "spaceId = " + spaceId + ", role = " + roleName, e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }
  
  private void setUsersAndGroups(DomainDriverManager ddManager, SpaceProfileInst spaceProfileInst)
      throws AdminPersistenceException {
    
    // Get the groups
    String[] asGroupIds =
        ddManager.organization.group.getDirectGroupIdsInSpaceUserRole(idAsInt(spaceProfileInst
            .getId()));

    // Set the groups to the space profile
    if (asGroupIds != null) {
      for (String groupId : asGroupIds) {
        spaceProfileInst.addGroup(groupId);
      }
    }

    // Get the Users
    String[] asUsersIds = ddManager.organization.user.getDirectUserIdsOfSpaceUserRole(idAsInt(
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
   * @param spaceProfileInst
   * @param ddManager
   * @throws AdminException 
   */
  public void deleteSpaceProfileInst(SpaceProfileInst spaceProfileInst,
      DomainDriverManager ddManager) throws AdminException {
    try {
      // delete the spaceProfile node
      ddManager.organization.spaceUserRole.removeSpaceUserRole(idAsInt(spaceProfileInst.getId()));
    } catch (Exception e) {
      throw new AdminException("SpaceProfileInstManager.deleteSpaceProfileInst",
          SilverpeasException.ERROR, "admin.EX_ERR_DELETE_SPACEPROFILE", "space profile Id: '"
          + spaceProfileInst.getId() + "'", e);
    }
  }

  /**
   * Updates space profile instance
   * @param spaceProfileInst
   * @param ddManager
   * @param spaceProfileInstNew
   * @return
   * @throws AdminException 
   */
  public String updateSpaceProfileInst(SpaceProfileInst spaceProfileInst,
      DomainDriverManager ddManager, SpaceProfileInst spaceProfileInstNew)
      throws AdminException {
    ArrayList<String> alOldSpaceProfileGroup = new ArrayList<String>();
    ArrayList<String> alNewSpaceProfileGroup = new ArrayList<String>();
    ArrayList<String> alAddGroup = new ArrayList<String>();
    ArrayList<String> alRemGroup = new ArrayList<String>();
    ArrayList<String> alStayGroup = new ArrayList<String>();
    ArrayList<String> alOldSpaceProfileUser = new ArrayList<String>();
    ArrayList<String> alNewSpaceProfileUser = new ArrayList<String>();
    ArrayList<String> alAddUser = new ArrayList<String>();
    ArrayList<String> alRemUser = new ArrayList<String>();
    ArrayList<String> alStayUser = new ArrayList<String>();

    try {
      // Compute the Old spaceProfile group list
      ArrayList<String> alGroup = spaceProfileInst.getAllGroups();
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
        if (alNewSpaceProfileGroup.indexOf(groupId) == -1) {
          alRemGroup.add(groupId);
        }
      }

      // Compute the add and stay group list
      for (String groupId : alNewSpaceProfileGroup) {
        if (alOldSpaceProfileGroup.indexOf(groupId) == -1) {
          alAddGroup.add(groupId);
        } else {
          alStayGroup.add(groupId);
        }
      }

      // Add the new Groups
      for (String groupId : alAddGroup) {
        // Create the links between the spaceProfile and the group
        ddManager.organization.spaceUserRole.addGroupInSpaceUserRole(
            idAsInt(groupId), idAsInt(spaceProfileInst.getId()));
      }

      // Remove the removed groups
      for (String groupId : alRemGroup) {
        // delete the node link SpaceProfile_Group
        ddManager.organization.spaceUserRole.removeGroupFromSpaceUserRole(
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
        if (alNewSpaceProfileUser.indexOf(userId) == -1) {
          alRemUser.add(userId);
        }
      }

      // Compute the add and stay User list
      for (String userId : alNewSpaceProfileUser) {
        if (alOldSpaceProfileUser.indexOf(userId) == -1) {
          alAddUser.add(userId);
        } else {
          alStayUser.add(userId);
        }
      }

      // Add the new Users
      for (String userId : alAddUser) {
        // Create the links between the spaceProfile and the User
        ddManager.organization.spaceUserRole.addUserInSpaceUserRole(
            idAsInt(userId), idAsInt(spaceProfileInst.getId()));
      }

      // Remove the removed Users
      for (String userId : alRemUser) {
        // delete the node link SpaceProfile_User
        ddManager.organization.spaceUserRole.removeUserFromSpaceUserRole(
            idAsInt(userId), idAsInt(spaceProfileInst.getId()));
      }

      // update the spaceProfile node
      SpaceUserRoleRow changedSpaceUserRole = makeSpaceUserRoleRow(spaceProfileInstNew);
      changedSpaceUserRole.id = idAsInt(spaceProfileInstNew.getId());
      ddManager.organization.spaceUserRole.updateSpaceUserRole(changedSpaceUserRole);

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
