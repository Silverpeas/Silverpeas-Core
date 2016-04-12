/**
* Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.admin.user;

import org.silverpeas.core.admin.domain.synchro.SynchroDomainReport;
import org.silverpeas.core.admin.user.dao.GroupDAO;
import org.silverpeas.core.admin.user.dao.GroupSearchCriteriaForDAO;
import org.silverpeas.core.admin.user.dao.UserDAO;
import org.silverpeas.core.admin.user.dao.UserSearchCriteriaForDAO;
import org.silverpeas.core.admin.persistence.AdminPersistenceException;
import org.silverpeas.core.admin.persistence.GroupRow;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.domain.DomainDriverManager;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.dao.SearchCriteriaDAOFactory;
import org.silverpeas.core.admin.user.model.AdminGroupInst;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.notification.GroupEventNotifier;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.ListSlice;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.exception.SilverpeasException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Singleton
public class GroupManager {

  @Inject
  private GroupDAO groupDao;
  @Inject
  private UserDAO userDao;
  @Inject
  private GroupEventNotifier notifier;

  /**
   * Constructor
   */
  public GroupManager() {
  }

  /**
   * Gets the groups that match the specified criteria.
   *
   * @param criteria the criteria in searching of user groups.
   * @return a slice of the list of user groups matching the criteria or an empty list of no ones are found.
   * @throws AdminException if an error occurs while getting the user groups.
   */
  public ListSlice<Group> getGroupsMatchingCriteria(final GroupSearchCriteriaForDAO criteria) throws
          AdminException {
    Connection connection = null;
    try {
      connection = DBUtil.openConnection();

      ListSlice<Group> groups = groupDao.getGroupsByCriteria(connection, criteria);

      String domainIdConstraint = null;
      List<String> domainIds = criteria.getCriterionOnDomainIds();
      for (String domainId : domainIds) {
        if (!domainId.equals(Domain.MIXED_DOMAIN_ID)) {
          domainIdConstraint = domainId;
          break;
        }
      }

      SearchCriteriaDAOFactory factory = SearchCriteriaDAOFactory.getFactory();
      for (Group group : groups) {
        List<String> groupIds = getAllSubGroupIdsRecursively(group.getId());
        groupIds.add(group.getId());
        UserSearchCriteriaForDAO criteriaOnUsers = factory.getUserSearchCriteriaDAO();
        Set<UserState> criterionOnUserStatesToExclude =
            criteria.getCriterionOnUserStatesToExclude();
        int userCount = userDao.getUserCountByCriteria(connection, criteriaOnUsers.
            onDomainId(domainIdConstraint).
            and().
            onGroupIds(groupIds.toArray(new String[groupIds.size()])).
            and().
            onUserStatesToExclude(criterionOnUserStatesToExclude
                .toArray(new UserState[criterionOnUserStatesToExclude.size()])));
        group.setTotalNbUsers(userCount);
      }
      return groups;
    } catch (Exception e) {
      throw new AdminException("GroupManager.getGroupsMatchingCriteria",
              SilverpeasException.ERROR, "admin.EX_ERR_GET_USER_GROUPS", e);
    } finally {
      DBUtil.close(connection);
    }
  }

  /**
   * Gets the total number of users in the specified group, that is to say the number of distinct
   * users in the specified group and in its subgroups.
   * @param domainId the unique identifier to which the group belong.
   * @param groupId the unique identifier of the group.
   * @return the total users count in the specified group.
   * @throws AdminException if an error occurs while computing the user count.
   */
  public int getTotalUserCountInGroup(String domainId, String groupId) throws AdminException {
    Connection connection = null;
    try {
      connection = DBUtil.openConnection();
      SearchCriteriaDAOFactory factory = SearchCriteriaDAOFactory.getFactory();
      List<String> groupIds = getAllSubGroupIdsRecursively(groupId);
      groupIds.add(groupId);
      UserSearchCriteriaForDAO criteriaOnUsers = factory.getUserSearchCriteriaDAO();
      int userCount = userDao.getUserCountByCriteria(connection, criteriaOnUsers.
          onDomainId(domainId).
          and().
          onGroupIds(groupIds.toArray(new String[groupIds.size()])));
      return userCount;
    } catch (SQLException e) {
      throw new AdminException("GroupManager.getGroupsMatchingCriteria",
          SilverpeasException.ERROR, "admin.EX_ERR_GET_USER_GROUPS", e);
    } finally {
      DBUtil.close(connection);
    }
  }

  /**
   * Add a user to a group
   *
   * @param ddManager
   * @param sUserId
   * @param sGroupId
   * @throws AdminException
   */
  public void addUserInGroup(DomainDriverManager ddManager, String sUserId, String sGroupId) throws
          AdminException {
    try {
      ddManager.getOrganizationSchema();
      ddManager.getOrganization().group.addUserInGroup(idAsInt(sUserId), idAsInt(sGroupId));
    } catch (Exception e) {
      throw new AdminException("GroupManager.removeUserFromGroup",
              SilverpeasException.ERROR, "admin.EX_ERR_GET_USER_GROUPS",
              "User Id: '" + sUserId + "' GroupId = '" + sGroupId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Remove a user from a group
   *
   * @param ddManager
   * @param sUserId
   * @param sGroupId
   * @throws AdminException
   */
  public void removeUserFromGroup(DomainDriverManager ddManager, String sUserId, String sGroupId)
          throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      ddManager.getOrganization().group.removeUserFromGroup(idAsInt(sUserId), idAsInt(sGroupId));
    } catch (Exception e) {
      throw new AdminException("GroupManager.removeUserFromGroup", SilverpeasException.ERROR,
              "admin.EX_ERR_GET_USER_GROUPS", "User Id: '" + sUserId + "' GroupId = '" + sGroupId
              + "'",
              e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get the direct groups id containing a user. Groups that the user is linked to by
   * transitivity are not returned.
   * @param ddManager
   * @param sUserId
   * @return
   * @throws AdminException
   */
  public String[] getDirectGroupsOfUser(DomainDriverManager ddManager, String sUserId) throws
          AdminException {
    try {
      ddManager.getOrganizationSchema();
      GroupRow[] grs = ddManager.getOrganization().group.getDirectGroupsOfUser(idAsInt(sUserId));
      // Convert GroupRow objects in Group Object
      String[] groups = new String[grs.length];
      for (int nI = 0; nI < grs.length; nI++) {
        groups[nI] = idAsString(grs[nI].id);
      }
      return groups;
    } catch (Exception e) {
      throw new AdminException("GroupManager.getDirectGroupsOfUser",
              SilverpeasException.ERROR, "admin.EX_ERR_GET_USER_GROUPS",
              "User Id: '" + sUserId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get all group ids containing a user. So, groups that the user is linked to by
   * transitivity are returned too (recursive treatment).
   * @param ddManager
   * @param userId
   * @return
   * @throws AdminException
   */
  public List<String> getAllGroupsOfUser(DomainDriverManager ddManager, String userId) throws AdminException {
    Set<String> allGroupsOfUser = new HashSet<>();

    String[] directGroupIds = getDirectGroupsOfUser(ddManager, userId);
    for (String directGroupId : directGroupIds) {
      Group group = getGroup(ddManager, directGroupId);
      if (group != null) {
        allGroupsOfUser.add(group.getId());
        while (group != null && StringUtil.isDefined(group.getSuperGroupId())) {
          group = getGroup(ddManager, group.getSuperGroupId());
          if (group != null) {
            allGroupsOfUser.add(group.getId());
          }
        }
      }
    }

    return new ArrayList<>(allGroupsOfUser);
  }

  /**
   * Get the Silverpeas group id of group qualified by given specific Id and domain id
   *
   * @param ddManager
   * @param sSpecificId
   * @param sDomainId
   * @return
   * @throws AdminException
   */
  public String getGroupIdBySpecificIdAndDomainId(DomainDriverManager ddManager, String sSpecificId,
          String sDomainId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      GroupRow gr = ddManager.getOrganization().group.getGroupBySpecificId(
              idAsInt(sDomainId), sSpecificId);
      return idAsString(gr.id);
    } catch (Exception e) {
      throw new AdminException("GroupManager.getGroupIdBySpecificIdAndDomain",
              SilverpeasException.ERROR, "admin.EX_ERR_GET_GROUP",
              "group specific Id: '" + sSpecificId + "', domain Id: '" + sDomainId
              + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get the all the groups id available in Silverpeas
   *
   * @param ddManager
   * @return
   * @throws AdminException
   */
  public String[] getAllGroupIds(DomainDriverManager ddManager) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      String[] asGroupIds = ddManager.getOrganization().group.getAllGroupIds();

      if (asGroupIds != null) {
        return asGroupIds;
      }
      return ArrayUtil.EMPTY_STRING_ARRAY;
    } catch (Exception e) {
      throw new AdminException("GroupManager.getAllGroupIds",
              SilverpeasException.ERROR, "admin.EX_ERR_GET_ALL_GROUP_IDS", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get the all the root groups id available in Silverpeas
   *
   * @param ddManager
   * @return
   * @throws AdminException
   */
  public String[] getAllRootGroupIds(DomainDriverManager ddManager) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      String[] asGroupIds = ddManager.getOrganization().group.getAllRootGroupIds();
      if (asGroupIds != null) {
        return asGroupIds;
      }
      return ArrayUtil.EMPTY_STRING_ARRAY;
    } catch (Exception e) {
      throw new AdminException("GroupManager.getAllRootGroupIds",
              SilverpeasException.ERROR, "admin.EX_ERR_GET_ALL_ROOT_GROUP_IDS", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Gets all the root user groups in Silverpeas.
   *
   * @param ddManager the manager of domain drivers in use in Silverpeas.
   * @return an array of root user groups.
   * @throws AdminException if an error occurs while getting the root groups.
   */
  public Group[] getAllRootGroups(DomainDriverManager ddManager) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      GroupRow[] rows = ddManager.getOrganization().group.getAllRootGroups();
      Group[] rootGroups;
      if (rows != null) {
        rootGroups = new Group[rows.length];
        for (int i = 0; i < rows.length; i++) {
          rootGroups[i] = groupRow2Group(rows[i]);
          setDirectUsersOfGroup(ddManager, rootGroups[i]);
        }
      } else {
        rootGroups = new Group[0];
      }
      return rootGroups;
    } catch (Exception e) {
      throw new AdminException("GroupManager.getAllRootGroupIds",
              SilverpeasException.ERROR, "admin.EX_ERR_GET_ALL_ROOT_GROUP_IDS", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get the all the direct sub groups id of a given group
   *
   * @param ddManager
   * @param superGroupId
   * @return
   * @throws AdminException
   */
  public String[] getAllSubGroupIds(DomainDriverManager ddManager, String superGroupId) throws
          AdminException {
    try {
      ddManager.getOrganizationSchema();
      String[] asGroupIds = ddManager.getOrganization().group.getDirectSubGroupIds(idAsInt(
              superGroupId));
      if (asGroupIds != null) {
        return asGroupIds;
      }
      return ArrayUtil.EMPTY_STRING_ARRAY;
    } catch (Exception e) {
      throw new AdminException("GroupManager.getAllSubGroupIds",
              SilverpeasException.ERROR, "admin.EX_ERR_GET_CHILDREN_GROUP_IDS",
              "father group Id: '" + superGroupId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get the path from root to a given group
   *
   * @param ddManager
   * @param groupId
   * @return
   * @throws AdminException
   */
  public List<String> getPathToGroup(DomainDriverManager ddManager, String groupId) throws
          AdminException {
    try {
      ddManager.getOrganizationSchema();
      List<String> path = new ArrayList<>();
      GroupRow superGroup = ddManager.getOrganization().group.getSuperGroup(idAsInt(groupId));
      while (superGroup != null) {
        path.add(0, idAsString(superGroup.id));
        superGroup = ddManager.getOrganization().group.getSuperGroup(superGroup.id);
      }

      return path;
    } catch (Exception e) {
      throw new AdminException("GroupManager.getPathToGroup",
              SilverpeasException.ERROR, "admin.EX_ERR_GET_PATH_TO_GROUP",
              "groupId = " + groupId, e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * /**
   * Check if the given group exists
   *
   * @param ddManager
   * @param sName
   * @return true if a group with the given name
   * @throws AdminException
   */
  public boolean isGroupExist(DomainDriverManager ddManager, String sName) throws AdminException {
    try {
      ddManager.getOrganizationSchema();

      // build GroupRow to search
      GroupRow searchedGroup = new GroupRow();
      searchedGroup.specificId = null;
      searchedGroup.name = sName;
      searchedGroup.description = null;

      // search for group
      GroupRow[] group = ddManager.getOrganization().group.getAllMatchingGroups(searchedGroup);

      return (group.length > 0);
    } catch (Exception e) {
      throw new AdminException("GroupManager.isGroupExist",
              SilverpeasException.ERROR, "admin.EX_ERR_IS_GROUP_EXIST",
              "group name: '" + sName + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  public Group getGroup(String groupId) throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();
      Group group = groupDao.getGroup(con, groupId);
      return group;

    } catch (Exception e) {
      throw new AdminException("GroupManager.getGroup",
              SilverpeasException.ERROR, "admin.EX_ERR_GET_GROUP", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Get group information with the given id from Silverpeas
   *
   * @param ddManager
   * @param sGroupId
   * @return
   * @throws AdminException
   */
  public Group getGroup(DomainDriverManager ddManager, String sGroupId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      GroupRow gr = ddManager.getOrganization().group.getGroup(idAsInt(sGroupId));

      Group group = new Group();

      if (gr != null) {
        group.setId(idAsString(gr.id));
        group.setSpecificId(gr.specificId);
        group.setDomainId(idAsString(gr.domainId));
        group.setSuperGroupId(idAsString(gr.superGroupId));
        group.setName(gr.name);
        group.setDescription(gr.description);
        group.setRule(gr.rule);
      }
      // Get the selected users for this group
      setDirectUsersOfGroup(ddManager, group);

      return group;
    } catch (Exception e) {
      throw new AdminException("GroupManager.getGroup",
              SilverpeasException.ERROR, "admin.EX_ERR_GET_GROUP", "group Id: '"
              + sGroupId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get the all the sub groups id of a given group
   *
   * @param superGroupId
   * @return
   * @throws AdminException
   */
  public List<String> getAllSubGroupIdsRecursively(String superGroupId) throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();
      return getSubGroupIds(con, superGroupId);
    } catch (Exception e) {
      throw new AdminException("GroupManager.getAllSubGroupIdsRecursively",
              SilverpeasException.ERROR, "admin.EX_ERR_GET_CHILDREN_GROUP_IDS",
              "father group Id: '" + superGroupId + "'", e);
    } finally {
      DBUtil.close(con);
    }
  }

  private List<String> getSubGroupIds(Connection con, String groupId) throws SQLException {
    List<String> groupIds = new ArrayList<>();
    List<Group> groups = groupDao.getSubGroups(con, groupId);
    for (Group group : groups) {
      groupIds.add(group.getId());
      groupIds.addAll(getSubGroupIds(con, group.getId()));
    }
    return groupIds;
  }

  /**
   * Get group information with the given group name
   *
   * @param ddManager
   * @param sGroupName
   * @param sDomainFatherId
   * @return
   * @throws AdminException
   */
  public Group getGroupByNameInDomain(DomainDriverManager ddManager, String sGroupName,
          String sDomainFatherId) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      Group group = ddManager.getGroupByNameInDomain(sGroupName, sDomainFatherId);

      if (group != null) {
        String specificId = group.getSpecificId();
        GroupRow gr = ddManager.getOrganization().group.getGroupBySpecificId(
                idAsInt(sDomainFatherId),
                specificId);
        if (gr != null) {
          group.setId(idAsString(gr.id));
          // Get the selected users for this group
          setDirectUsersOfGroup(ddManager, group);
        } else {
          return null;
        }
      }
      return group;
    } catch (Exception e) {
      throw new AdminException("GroupManager.getGroupByNameInDomain",
              SilverpeasException.ERROR, "admin.EX_ERR_GET_GROUP", "group Name: '"
              + sGroupName + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   *
   * @param ddManager
   * @param sDomainId
   * @return
   * @throws AdminException
   */
  public Group[] getRootGroupsOfDomain(DomainDriverManager ddManager, String sDomainId) throws
          AdminException {
    try {
      // Get organization
      ddManager.getOrganizationSchema();
      // Get groups of domain from Silverpeas database
      GroupRow[] grs =
              ddManager.getOrganization().group.getAllRootGroupsOfDomain(idAsInt(sDomainId));
      // Convert GroupRow objects in Group Object
      Group[] groups = new Group[grs.length];
      for (int nI = 0; nI < grs.length; nI++) {
        groups[nI] = groupRow2Group(grs[nI]);
        // Get the selected users for this group
        setDirectUsersOfGroup(ddManager, groups[nI]);
      }
      return groups;
    } catch (Exception e) {
      throw new AdminException("GroupManager.getRootGroupsOfDomain",
              SilverpeasException.ERROR, "admin.EX_ERR_GET_GROUPS_OF_DOMAIN",
              "domain Id: '" + sDomainId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   *
   * @param ddManager
   * @return
   * @throws AdminException
   */
  public Group[] getSynchronizedGroups(DomainDriverManager ddManager) throws AdminException {
    try {
      // Get organization
      ddManager.getOrganizationSchema();
      // Get groups of domain from Silverpeas database
      GroupRow[] grs = ddManager.getOrganization().group.getSynchronizedGroups();
      // Convert GroupRow objects in Group Object
      Group[] groups = new Group[grs.length];
      for (int nI = 0; nI < grs.length; nI++) {
        groups[nI] = groupRow2Group(grs[nI]);
      }
      return groups;
    } catch (Exception e) {
      throw new AdminException("GroupManager.getRootGroupsOfDomain",
              SilverpeasException.ERROR, "admin.EX_ERR_GET_GROUPS_OF_DOMAIN", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   *
   * @param ddManager
   * @param sDomainId
   * @return
   * @throws AdminException
   */
  public String[] getRootGroupIdsOfDomain(DomainDriverManager ddManager, String sDomainId) throws
          AdminException {
    try {
      // Get organization
      ddManager.getOrganizationSchema();
      // Get groups of domain from Silverpeas database
      String[] groupIds = ddManager.getOrganization().group.getAllRootGroupIdsOfDomain(idAsInt(
              sDomainId));
      if (groupIds != null) {
        return groupIds;
      }
      return ArrayUtil.EMPTY_STRING_ARRAY;
    } catch (Exception e) {
      throw new AdminException("GroupManager.getRootGroupIdsOfDomain",
              SilverpeasException.ERROR, "admin.EX_ERR_GET_GROUPS_OF_DOMAIN",
              "domain Id: '" + sDomainId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get the groups of domain
   *
   * @param ddManager
   * @param sDomainId
   * @return
   * @throws AdminException
   */
  public Group[] getGroupsOfDomain(DomainDriverManager ddManager, String sDomainId) throws
          AdminException {
    try {
      // Get organization
      ddManager.getOrganizationSchema();
      SynchroDomainReport.info("GroupManager.getGroupsOfDomain()",
              "Recherche des groupes du domaine LDAP dans la base...");
      // Get groups of domain from Silverpeas database
      GroupRow[] grs = ddManager.getOrganization().group.getAllGroupsOfDomain(idAsInt(sDomainId));
      // Convert GroupRow objects in Group Object
      Group[] groups = new Group[grs.length];
      for (int nI = 0; nI < grs.length; nI++) {
        groups[nI] = groupRow2Group(grs[nI]);
        SynchroDomainReport.debug("GroupManager.getGroupsOfDomain()", "Groupe trouvé no : " + Integer.
                toString(nI) + ", specificID : " + groups[nI].getSpecificId() + ", desc. : "
                + groups[nI].getDescription());
      }
      SynchroDomainReport.info("GroupManager.getGroupsOfDomain()",
              "Récupération de " + grs.length + " groupes du domaine LDAP dans la base");
      return groups;
    } catch (Exception e) {
      throw new AdminException("GroupManager.getGroupsOfDomain",
              SilverpeasException.ERROR, "admin.EX_ERR_GET_GROUPS_OF_DOMAIN",
              "domain Id: '" + sDomainId + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  public String[] searchGroupsIds(DomainDriverManager ddManager, boolean isRootGroup,
          int componentId, String[] aProfileId, Group modelGroup) throws AdminException {
    try {
      // Get organization
      ddManager.getOrganizationSchema();
      GroupRow model = group2GroupRow(modelGroup);
      // The Ids could be equal to -1 !!!! Put it to -2 if null
      if (!StringUtil.isDefined(modelGroup.getId())) {
        model.id = -2;
      }
      if (!StringUtil.isDefined(modelGroup.getDomainId())) {
        model.domainId = -2;
      }
      if (!StringUtil.isDefined(modelGroup.getSuperGroupId())) {
        model.superGroupId = -2;
      }

      int[] aRoleId = null;
      if (aProfileId != null) {
        aRoleId = new int[aProfileId.length];
        for (int i = 0; i < aProfileId.length; i++) {
          aRoleId[i] = idAsInt(aProfileId[i]);
        }
      }
      // Get groups
      return ddManager.getOrganization().group.searchGroupsIds(isRootGroup,
              componentId, aRoleId, model);
    } catch (Exception e) {
      throw new AdminException("GroupManager.searchGroupsIdsInGroup",
              SilverpeasException.ERROR, "admin.EX_ERR_GET_GROUPS_OF_DOMAIN", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   *
   * @param ddManager
   * @param modelGroup
   * @param isAnd
   * @return
   * @throws AdminException
   */
  public Group[] searchGroups(DomainDriverManager ddManager, Group modelGroup, boolean isAnd) throws
          AdminException {
    try {
      // Get organization
      ddManager.getOrganizationSchema();
      GroupRow model = group2GroupRow(modelGroup);
      // The Ids could be equal to -1 !!!! Put it to -2 if null
      if (!StringUtil.isDefined((modelGroup.getId()))) {
        model.id = -2;
      }
      if (!StringUtil.isDefined(modelGroup.getDomainId())) {
        model.domainId = -2;
      }
      if (!StringUtil.isDefined(modelGroup.getSuperGroupId())) {
        model.superGroupId = -2;
      }
      // Get groups
      GroupRow[] grs = ddManager.getOrganization().group.searchGroups(model, isAnd);
      // Convert GroupRow objects in Group Object
      Group[] groups = new Group[grs.length];
      for (int nI = 0; nI < grs.length; nI++) {
        groups[nI] = groupRow2Group(grs[nI]);
        // Get the selected users for this group
        setDirectUsersOfGroup(ddManager, groups[nI]);
      }
      return groups;
    } catch (Exception e) {
      throw new AdminException("GroupManager.searchGroups",
              SilverpeasException.ERROR, "admin.EX_ERR_GET_GROUPS_OF_DOMAIN", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Add the given group in Silverpeas
   *
   * @param ddManager
   * @param group
   * @param onlyInSilverpeas
   * @return
   * @throws AdminException
   */
  public String addGroup(DomainDriverManager ddManager, Group group, boolean onlyInSilverpeas)
          throws AdminException {
    if (group == null || !StringUtil.isDefined(group.getName())) {
      if (group != null) {
        SynchroDomainReport.error("GroupManager.addGroup()", "Problème lors de l'ajout du groupe "
                + group.getSpecificId() + " dans la base, ce groupe n'a pas de nom", null);
      }
      return "";
    }

    try {
      ddManager.getOrganizationSchema();
      // Create group in specific domain (if onlyInSilverpeas is not true)
      // if domainId=-1 then group is a silverpeas organization
      if (!onlyInSilverpeas) {
        String specificId;
        if (group.getDomainId() != null) {
          specificId = ddManager.createGroup(group);
          group.setSpecificId(specificId);
        }
      }
      // Create the group node in Silverpeas
      GroupRow gr = group2GroupRow(group);
      if (gr.superGroupId != -1) {
        SynchroDomainReport.info("GroupManager.addGroup()", "Ajout du groupe " + group.getName()
                + " (père=" + getGroup(ddManager, group.getSuperGroupId()).getSpecificId()
                + ") dans la table ST_Group");
      } else { // pas de père
        SynchroDomainReport.info("GroupManager.addGroup()", "Ajout du groupe " + group.getName()
                + " (groupe racine) dans la table ST_Group...");
      }
      ddManager.getOrganization().group.createGroup(gr);
      String sGroupId = idAsString(gr.id);
      group.setId(sGroupId);
      notifier.notifyEventOn(ResourceEvent.Type.CREATION, group);

      // index group information
      ddManager.indexGroup(gr);

      // Create the links group_user in Silverpeas
      SynchroDomainReport.info("GroupManager.addGroup()",
              "Inclusion des utilisateurs directement associés au groupe " + group.getName()
              + " (table ST_Group_User_Rel)");
      String[] asUserIds = group.getUserIds();
      int nUserAdded = 0;
      for (String asUserId : asUserIds) {
        if (StringUtil.isDefined(asUserId)) {
          ddManager.getOrganization().group.addUserInGroup(idAsInt(asUserId), idAsInt(sGroupId));
          nUserAdded++;
        }
      }
      SynchroDomainReport.info("GroupManager.addGroup()", nUserAdded + " utilisateurs ajoutés au groupe "
              + group.getName() + " dans la base");
      return sGroupId;
    } catch (Exception e) {
      SynchroDomainReport.error("GroupManager.addGroup()", "problème lors de l'ajout du groupe "
              + group.getName() + " - " + e.getMessage(), null);
      throw new AdminException("GroupManager.addGroup", SilverpeasException.ERROR,
              "admin.EX_ERR_ADD_GROUP", "group name: '" + group.getName() + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Delete the group with the given Id The delete is apply recursively to the sub-groups
   *
   * @param ddManager
   * @param group
   * @param onlyInSilverpeas
   * @return
   * @throws AdminException
   */
  public String deleteGroupById(DomainDriverManager ddManager, Group group,
          boolean onlyInSilverpeas) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      if (group.getDomainId() != null && !onlyInSilverpeas) {
        ddManager.deleteGroup(group.getId());
      }
      // Delete the group node from Silverpeas
      ddManager.getOrganization().group.removeGroup(idAsInt(group.getId()));
      notifier.notifyEventOn(ResourceEvent.Type.DELETION, group);

      // Delete index of group information
      ddManager.unindexGroup(group.getId());

      return group.getId();
    } catch (Exception e) {
      SynchroDomainReport.error("GroupManager.deleteGroupById()",
              "problème lors de la suppression du groupe " + group.getName()
              + " - " + e.getMessage(), null);
      throw new AdminException("GroupManager.deleteGroupById",
              SilverpeasException.ERROR, "admin.EX_ERR_DELETE_GROUP", "group Id: '"
              + group.getId() + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Update the given group
   *
   * @param ddManager
   * @param group
   * @param onlyInSilverpeas
   * @return
   * @throws AdminException
   */
  public String updateGroup(DomainDriverManager ddManager, Group group, boolean onlyInSilverpeas)
          throws AdminException {
    ArrayList<String> alRemUsers = new ArrayList<>();
    ArrayList<String> alAddUsers = new ArrayList<>();

    if (group == null || !StringUtil.isDefined(group.getName()) || !StringUtil.isDefined(
            group.getId())) {
      if (group != null) {
        SynchroDomainReport.error("GroupManager.updateGroup()", "Problème lors de maj du groupe "
                + group.getSpecificId() + " dans la base, ce groupe n'a pas de nom", null);
      }
      return "";
    }
    try {
      ddManager.getOrganizationSchema();
      if (group.getDomainId() != null && !onlyInSilverpeas) {
        ddManager.updateGroup(group);
      }
      // Get the group id
      String sGroupId = group.getId();
      String strInfoSycnhro;
      if (group.getSuperGroupId() != null) {
        strInfoSycnhro = "Maj du groupe " + group.getName() + " (père=" + getGroup(ddManager, group.
                getSuperGroupId()).getSpecificId() + ") dans la base (table ST_Group)...";
      } else {
        strInfoSycnhro = "Maj du groupe " + group.getName()
                + " (groupe racine) dans la base (table ST_Group)...";
      }
      SynchroDomainReport.info("GroupManager.updateGroup()", strInfoSycnhro);
      // Update the group node
      GroupRow gr = group2GroupRow(group);
      ddManager.getOrganization().group.updateGroup(gr);

      // index group information
      ddManager.indexGroup(gr);

      // Update the users if necessary
      SynchroDomainReport.info("GroupManager.updateGroup()", "Maj éventuelle des relations du groupe "
              + group.getName()
              + " avec les utilisateurs qui y sont directement inclus (tables ST_Group_User_Rel)");
      String[] asOldUsersId = ddManager.getOrganization().user.getDirectUserIdsOfGroup(idAsInt(
              sGroupId));

      // Compute the remove users list
      String[] asNewUsersId = group.getUserIds();
      for (String anAsOldUsersId : asOldUsersId) {
        boolean bFound = false;
        for (String anAsNewUsersId : asNewUsersId) {
          if (anAsOldUsersId.equals(anAsNewUsersId)) {
            bFound = true;
          }
        }

        if (!bFound) {
          alRemUsers.add(anAsOldUsersId);
        }
      }

      // Compute the add users list
      for (String anAsNewUsersId : asNewUsersId) {
        boolean bFound = false;
        for (String anAsOldUsersId : asOldUsersId) {
          if (anAsNewUsersId.equals(anAsOldUsersId)) {
            bFound = true;
          }
        }

        if (!bFound) {
          alAddUsers.add(anAsNewUsersId);
        }
      }
      // Remove the users that are not in this group anymore
      for (String alRemUser : alRemUsers) {
        ddManager.getOrganization().group.removeUserFromGroup(
                idAsInt(alRemUser), idAsInt(sGroupId));
      }

      // Add the new users of the group
      for (String alAddUser : alAddUsers) {
        ddManager.getOrganization().group.addUserInGroup(idAsInt(alAddUser), idAsInt(sGroupId));
      }

      SynchroDomainReport.info("GroupManager.updateGroup()", "Groupe : "
              + group.getName() + ", ajout de " + alAddUsers.size()
              + " nouveaux utilisateurs, suppression de " + alRemUsers.size()
              + " utilisateurs");
      return sGroupId;
    } catch (Exception e) {
      SynchroDomainReport.error("GroupManager.updateGroup()",
              "problème lors de la maj du groupe " + group.getName() + " - "
              + e.getMessage(), null);
      throw new AdminException("GroupManager.updateGroup",
              SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_GROUP", "group Id: '"
              + group.getId() + "'", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get Silverpeas admin organization
   *
   * @param ddManager
   * @return an array of AdminGroupInst containing the organization
   * @throws AdminException
   */
  public AdminGroupInst[] getAdminOrganization(DomainDriverManager ddManager) throws AdminException {
    try {
      ddManager.getOrganizationSchema();
      String[] asGroupIds = this.getAllGroupIds(ddManager);
      Group[] aGroup = new Group[asGroupIds.length];
      for (int nI = 0; nI < asGroupIds.length; nI++) {
        aGroup[nI] = this.getGroup(ddManager, asGroupIds[nI]);
      }
      // Search the root groups
      ArrayList<Integer> alRoot = new ArrayList<>();
      for (int nI = 0; nI < aGroup.length; nI++) {
        if (aGroup[nI].getSuperGroupId() == null) {
          alRoot.add(nI);
        }
      }
      AdminGroupInst[] aAdminGroupInst = new AdminGroupInst[alRoot.size()];
      for (int nI = 0; nI < alRoot.size(); nI++) {
        aAdminGroupInst[nI] = new AdminGroupInst();
        aAdminGroupInst[nI].setGroup(aGroup[alRoot.get(nI)]);
        aAdminGroupInst[nI].setChildrenAdminGroupInst(this.getChildrenGroupInst(ddManager,
                aAdminGroupInst[nI].getGroup().getId(), aGroup));
      }
      return aAdminGroupInst;
    } catch (Exception e) {
      throw new AdminException("GroupManager.getAdminOrganization",
              SilverpeasException.ERROR, "admin.EX_ERR_GET_ADMIN_ORGANIZATION", e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get the list of children groups of the given group
   */
  private ArrayList<AdminGroupInst> getChildrenGroupInst(DomainDriverManager ddManager,
          String sFatherGroupId, Group[] aGroup) {
    ArrayList<AdminGroupInst> alChildrenGroupInst = new ArrayList<>();

    // Search the children group
    for (Group anAGroup : aGroup) {
      if (anAGroup.getSuperGroupId() != null && anAGroup.getSuperGroupId().equals(sFatherGroupId)) {
        AdminGroupInst adminGroupInst = new AdminGroupInst();
        adminGroupInst.setGroup(anAGroup);
        adminGroupInst.setChildrenAdminGroupInst(this.getChildrenGroupInst(ddManager,
                adminGroupInst.getGroup().getId(), aGroup));
        alChildrenGroupInst.add(adminGroupInst);
      }
    }

    return alChildrenGroupInst;
  }

  public List<String> getManageableGroupIds(String userId, List<String> groupIds)
          throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();

      return groupDao.getManageableGroupIds(con, userId, groupIds);
    } catch (Exception e) {
      throw new AdminException("GroupManager.getManageableGroupIds",
              SilverpeasException.ERROR,
              "admin.EX_ERR_GET_USER_MANAGEABLE_GROUP_IDS", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public int getNBUsersDirectlyInGroup(String groupId) throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();

      return groupDao.getNBUsersDirectlyInGroup(con, groupId);
    } catch (Exception e) {
      throw new AdminException("GroupManager.getNBUsersDirectlyInGroup",
              SilverpeasException.ERROR,
              "admin.EX_ERR_GET_USER_OF_GROUP", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public List<String> getUsersDirectlyInGroup(String groupId) throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();

      return groupDao.getUsersDirectlyInGroup(con, groupId);
    } catch (Exception e) {
      throw new AdminException("GroupManager.getUsersDirectlyInGroup",
              SilverpeasException.ERROR,
              "admin.EX_ERR_GET_USER_OF_GROUP", e);
    } finally {
      DBUtil.close(con);
    }
  }

  private void setDirectUsersOfGroup(final DomainDriverManager ddManager, final Group group) throws
          AdminPersistenceException {
    String[] asUsersId = ddManager.getOrganization().user.getDirectUserIdsOfGroup(idAsInt(group.
            getId()));
    if (asUsersId != null) {
      group.setUserIds(asUsersId);
    }
  }

  /**
   * Convert GroupRow to Group
   */
  private Group groupRow2Group(GroupRow gr) {
    Group group = new Group();

    if (gr != null) {
      group.setId(idAsString(gr.id));
      group.setSpecificId(gr.specificId);
      group.setDomainId(idAsString(gr.domainId));
      group.setSuperGroupId(idAsString(gr.superGroupId));
      group.setName(gr.name);
      group.setDescription(gr.description);
      group.setRule(gr.rule);
    }
    return group;
  }

  /**
   * Convert Group to GroupRow
   */
  private GroupRow group2GroupRow(Group group) {
    GroupRow gr = new GroupRow();
    gr.id = idAsInt(group.getId());
    gr.specificId = group.getSpecificId();
    gr.domainId = idAsInt(group.getDomainId());
    gr.superGroupId = idAsInt(group.getSuperGroupId());
    gr.name = group.getName();
    gr.description = group.getDescription();
    gr.rule = group.getRule();

    return gr;
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
    if (id == -1) {
      return null;
    } else {
      return Integer.toString(id);
    }
  }
}