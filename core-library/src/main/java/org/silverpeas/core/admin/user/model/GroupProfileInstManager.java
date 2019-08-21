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
package org.silverpeas.core.admin.user.model;

import org.silverpeas.core.admin.persistence.GroupUserRoleRow;
import org.silverpeas.core.admin.persistence.OrganizationSchema;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.dao.GroupDAO;
import org.silverpeas.core.admin.user.dao.UserDAO;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.util.List;

import static org.silverpeas.core.SilverpeasExceptionMessages.*;

@Singleton
@Transactional(Transactional.TxType.MANDATORY)
public class GroupProfileInstManager {

  public static final String GROUP_PROFILE = "group profile";
  @Inject
  private OrganizationSchema organizationSchema;
  @Inject
  private GroupDAO groupDAO;
  @Inject
  private UserDAO userDAO;

  /**
   * Constructor
   */
  protected GroupProfileInstManager() {

  }

  /**
   * Create a new group profile instance in database
   */
  public String createGroupProfileInst(GroupProfileInst groupProfileInst, String sgroupId)
      throws AdminException {
    try {
      // Create the groupProfile node
      GroupUserRoleRow newRole = makeGroupUserRoleRow(groupProfileInst);
      newRole.groupId = idAsInt(sgroupId);
      organizationSchema.groupUserRole().createGroupUserRole(newRole);
      String sProfileId = idAsString(newRole.id);

      for (int nI = 0; nI < groupProfileInst.getNumGroup(); nI++) {
        organizationSchema.groupUserRole().addGroupInGroupUserRole(
            idAsInt(groupProfileInst.getGroup(nI)), idAsInt(sProfileId));
      }

      for (int nI = 0; nI < groupProfileInst.getNumUser(); nI++) {
        organizationSchema.groupUserRole().addUserInGroupUserRole(
            idAsInt(groupProfileInst.getUser(nI)), idAsInt(sProfileId));
      }

      return sProfileId;
    } catch (Exception e) {
      throw new AdminException(failureOnAdding(GROUP_PROFILE, groupProfileInst.getName()), e);
    }
  }

  /**
   * Get Space profile information with given id and creates a new GroupProfileInst
   */
  public GroupProfileInst getGroupProfileInst(String sProfileId, String sGroupId)
      throws AdminException {
    String groupId = sGroupId;
    if (sGroupId == null) {
      try (Connection connection = DBUtil.openConnection()) {
        GroupDetail group = groupDAO.getGroupByGroupUserRole(connection, sProfileId);
        if (group == null) {
          groupId = "-1";
        } else {
          groupId = group.getId();
        }
      } catch (Exception e) {
        throw new AdminException(
            failureOnGetting("profile " + sProfileId, "of group " + groupId), e);
      }
    }

    GroupProfileInst groupProfileInst = new GroupProfileInst();
    groupProfileInst.removeAllGroups();
    groupProfileInst.removeAllUsers();
    this.setGroupProfileInst(groupProfileInst, sProfileId, groupId);

    return groupProfileInst;
  }

  /**
   * get information for given id and store it in the given GroupProfileInst object
   */
  public void setGroupProfileInst(GroupProfileInst groupProfileInst, String sProfileId,
      String sGroupId)
      throws AdminException {
    try (Connection connection = DBUtil.openConnection()) {
      // Load the profile detail
      GroupUserRoleRow groupUserRole = organizationSchema.groupUserRole()
          .getGroupUserRoleByGroupId(idAsInt(sGroupId));

      groupProfileInst.setGroupId(sGroupId);

      if (groupUserRole != null) {
        // Set the attributes of the space profile Inst
        groupProfileInst.setId(Integer.toString(groupUserRole.id));
        groupProfileInst.setName(groupUserRole.roleName);

        String profileId = groupProfileInst.getId();

        // set the groups
        groupDAO.getDirectGroupIdsByGroupUserRole(connection, profileId)
            .forEach(groupProfileInst::addGroup);

        // set the users
        userDAO.getDirectUserIdsByGroupUserRole(connection, profileId, false)
            .forEach(groupProfileInst::addUser);
      }
    } catch (Exception e) {
      throw new AdminException("Fail to set profile " + sProfileId + " to group " + sGroupId, e);
    }
  }

  /**
   * Deletes group profile instance from Silverpeas
   */
  public void deleteGroupProfileInst(GroupProfileInst groupProfileInst) throws AdminException {
    try {
      for (int nI = 0; nI < groupProfileInst.getNumGroup(); nI++) {
        organizationSchema.groupUserRole().removeGroupFromGroupUserRole(
            idAsInt(groupProfileInst.getGroup(nI)), idAsInt(groupProfileInst
            .getId()));
      }

      for (int nI = 0; nI < groupProfileInst.getNumUser(); nI++) {
        organizationSchema.groupUserRole().removeUserFromGroupUserRole(
            idAsInt(groupProfileInst.getUser(nI)), idAsInt(groupProfileInst
            .getId()));
      }

      // delete the groupProfile node
      organizationSchema.groupUserRole()
          .removeGroupUserRole(idAsInt(groupProfileInst.getId()));
    } catch (Exception e) {
      throw new AdminException(failureOnDeleting(GROUP_PROFILE, groupProfileInst.getId()), e);
    }
  }

  /**
   * Updates group profile instance
   */
  public String updateGroupProfileInst(GroupProfileInst groupProfileInst,
      GroupProfileInst groupProfileInstNew)
      throws AdminException {
    try {
      // the groups in the previous state of the group profile
      List<String> alOldProfileGroup = groupProfileInst.getAllGroups();

      // the new groups in the new state of the group profile
      List<String> alNewProfileGroup = groupProfileInstNew.getAllGroups();

      // Add the new groups in the profile
      for (String anAlNewProfileGroup : alNewProfileGroup) {
        if (!alOldProfileGroup.contains(anAlNewProfileGroup)) {
          // Create the links between the group profile and the group
          organizationSchema.groupUserRole()
              .addGroupInGroupUserRole(idAsInt(anAlNewProfileGroup),
                  idAsInt(groupProfileInst.getId()));
        }
      }

      // Remove from the profile the groups that are no more in the new state of the group profile
      for (String anAlRemGroup : alOldProfileGroup) {
        if (!alNewProfileGroup.contains(anAlRemGroup)) {
          // delete the link between the group profile and the group
          organizationSchema.groupUserRole().removeGroupFromGroupUserRole(
              idAsInt(anAlRemGroup), idAsInt(groupProfileInst.getId()));
        }
      }

      // the users in the previous state of the group profile
      List<String> alOldProfileUser = groupProfileInst.getAllUsers();

      // the users in the new state of the group profile
      List<String> alNewProfileUser = groupProfileInstNew.getAllUsers();

      // Add the new users
      for (String anAlAddUser : alNewProfileUser) {
        if (!alOldProfileUser.contains(anAlAddUser)) {
          // Create the links between the group profile and the user
          organizationSchema.groupUserRole().addUserInGroupUserRole(idAsInt(anAlAddUser),
              idAsInt(groupProfileInst.getId()));
        }
      }

      // Remove from the profile the users that are no more in the new state of the group profile
      for (String anAlRemUser : alOldProfileUser) {
        // delete the link between the group profile and the user
        if (!alNewProfileUser.contains(anAlRemUser)) {
          organizationSchema.groupUserRole().removeUserFromGroupUserRole(
              idAsInt(anAlRemUser), idAsInt(groupProfileInst.getId()));
        }
      }
      return groupProfileInst.getId();
    } catch (Exception e) {
      throw new AdminException(failureOnUpdate(GROUP_PROFILE, groupProfileInst.getId()), e);
    }
  }

  /**
   * Converts GroupProfileInst to GroupUserRoleRow
   */
  private GroupUserRoleRow makeGroupUserRoleRow(
      GroupProfileInst groupProfileInst) {
    GroupUserRoleRow groupUserRole = new GroupUserRoleRow();

    groupUserRole.id = idAsInt(groupProfileInst.getId());
    groupUserRole.roleName = groupProfileInst.getName();

    return groupUserRole;
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
