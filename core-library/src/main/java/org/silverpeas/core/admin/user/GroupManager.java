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

import org.silverpeas.core.admin.domain.DomainDriverManager;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.domain.synchro.SynchroDomainReport;
import org.silverpeas.core.admin.persistence.AdminPersistenceException;
import org.silverpeas.core.admin.persistence.GroupRow;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.dao.GroupDAO;
import org.silverpeas.core.admin.user.dao.GroupSearchCriteriaForDAO;
import org.silverpeas.core.admin.user.dao.SearchCriteriaDAOFactory;
import org.silverpeas.core.admin.user.dao.UserDAO;
import org.silverpeas.core.admin.user.dao.UserSearchCriteriaForDAO;
import org.silverpeas.core.admin.user.model.AdminGroupInst;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.admin.user.notification.GroupEventNotifier;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.ListSlice;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.silverpeas.core.SilverpeasExceptionMessages.*;

@Singleton
@Transactional(Transactional.TxType.MANDATORY)
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
  protected GroupManager() {
  }

  /**
   * Gets the groups that match the specified criteria.
   *
   * @param criteria the criteria in searching of user groups.
   * @return a slice of the list of user groups matching the criteria or an empty list of no ones are found.
   * @throws AdminException if an error occurs while getting the user groups.
   */
  public ListSlice<GroupDetail> getGroupsMatchingCriteria(final GroupSearchCriteriaForDAO criteria) throws
          AdminException {
    Connection connection = null;
    try {
      connection = DBUtil.openConnection();

      ListSlice<GroupDetail> groups = groupDao.getGroupsByCriteria(connection, criteria);

      String domainIdConstraint = null;
      List<String> domainIds = criteria.getCriterionOnDomainIds();
      for (String domainId : domainIds) {
        if (!domainId.equals(Domain.MIXED_DOMAIN_ID)) {
          domainIdConstraint = domainId;
          break;
        }
      }

      SearchCriteriaDAOFactory factory = SearchCriteriaDAOFactory.getFactory();
      for (GroupDetail group : groups) {
        List<String> groupIds = getAllSubGroupIdsRecursively(group.getId());
        groupIds.add(group.getId());
        UserSearchCriteriaForDAO criteriaOnUsers = factory.getUserSearchCriteriaDAO();
        Set<UserState> criterionOnUserStatesToExclude =
            criteria.getCriterionOnUserStatesToExclude();
        int userCount = userDao.getUserCountByCriteria(connection, criteriaOnUsers.
            onDomainId(domainIdConstraint).
            onGroupIds(groupIds.toArray(new String[groupIds.size()])).
            onUserStatesToExclude(criterionOnUserStatesToExclude
                .toArray(new UserState[criterionOnUserStatesToExclude.size()])));
        group.setTotalNbUsers(userCount);
      }
      return groups;
    } catch (Exception e) {
      throw new AdminException("Fail to search groups matching criteria", e);
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
          onGroupIds(groupIds.toArray(new String[groupIds.size()])));
      return userCount;
    } catch (SQLException e) {
      throw new AdminException(e.getMessage(), e);
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
      ddManager.holdOrganizationSchema();
      ddManager.getOrganization().group.addUserInGroup(idAsInt(sUserId), idAsInt(sGroupId));
    } catch (Exception e) {
      throw new AdminException(failureOnAdding("user " + sUserId, "in group " + sGroupId), e);
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
      ddManager.holdOrganizationSchema();
      ddManager.getOrganization().group.removeUserFromGroup(idAsInt(sUserId), idAsInt(sGroupId));
    } catch (Exception e) {
      throw new AdminException(failureOnDeleting("user " + sUserId, "in group " + sGroupId), e);
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
      ddManager.holdOrganizationSchema();
      GroupRow[] grs = ddManager.getOrganization().group.getDirectGroupsOfUser(idAsInt(sUserId));
      // Convert GroupRow objects in GroupDetail Object
      String[] groups = new String[grs.length];
      for (int nI = 0; nI < grs.length; nI++) {
        groups[nI] = idAsString(grs[nI].id);
      }
      return groups;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("direct groups of user", sUserId), e);
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
      GroupDetail group = getGroup(ddManager, directGroupId);
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
      ddManager.holdOrganizationSchema();
      GroupRow gr = ddManager.getOrganization().group.getGroupBySpecificId(
              idAsInt(sDomainId), sSpecificId);
      return idAsString(gr.id);
    } catch (Exception e) {
      throw new AdminException(
          failureOnGetting("groups by specific id and domain", sSpecificId + "/" + sDomainId), e);
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
      ddManager.holdOrganizationSchema();
      String[] asGroupIds = ddManager.getOrganization().group.getAllGroupIds();

      if (asGroupIds != null) {
        return asGroupIds;
      }
      return ArrayUtil.EMPTY_STRING_ARRAY;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("all groups", ""), e);
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
      ddManager.holdOrganizationSchema();
      String[] asGroupIds = ddManager.getOrganization().group.getAllRootGroupIds();
      if (asGroupIds != null) {
        return asGroupIds;
      }
      return ArrayUtil.EMPTY_STRING_ARRAY;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("all root groups", ""), e);
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
  public GroupDetail[] getAllRootGroups(DomainDriverManager ddManager) throws AdminException {
    try {
      ddManager.holdOrganizationSchema();
      GroupRow[] rows = ddManager.getOrganization().group.getAllRootGroups();
      GroupDetail[] rootGroups;
      if (rows != null) {
        rootGroups = new GroupDetail[rows.length];
        for (int i = 0; i < rows.length; i++) {
          rootGroups[i] = groupRow2Group(rows[i]);
          setDirectUsersOfGroup(rootGroups[i]);
        }
      } else {
        rootGroups = new GroupDetail[0];
      }
      return rootGroups;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("all root groups", ""), e);
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
      ddManager.holdOrganizationSchema();
      String[] asGroupIds = ddManager.getOrganization().group.getDirectSubGroupIds(idAsInt(
              superGroupId));
      if (asGroupIds != null) {
        return asGroupIds;
      }
      return ArrayUtil.EMPTY_STRING_ARRAY;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("all subgroups of group", superGroupId), e);
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
      ddManager.holdOrganizationSchema();
      List<String> path = new ArrayList<>();
      GroupRow superGroup = ddManager.getOrganization().group.getSuperGroup(idAsInt(groupId));
      while (superGroup != null) {
        path.add(0, idAsString(superGroup.id));
        superGroup = ddManager.getOrganization().group.getSuperGroup(superGroup.id);
      }

      return path;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("path to group", groupId), e);
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
      ddManager.holdOrganizationSchema();

      // build GroupRow to search
      GroupRow searchedGroup = new GroupRow();
      searchedGroup.specificId = null;
      searchedGroup.name = sName;
      searchedGroup.description = null;

      // search for group
      GroupRow[] group = ddManager.getOrganization().group.getAllMatchingGroups(searchedGroup);

      return (group.length > 0);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("all groups matching name", sName), e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  public GroupDetail getGroup(String groupId) throws AdminException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();
      GroupDetail group = groupDao.getGroup(con, groupId);
      return group;

    } catch (Exception e) {
      throw new AdminException(failureOnGetting("group", groupId), e);
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
  public GroupDetail getGroup(DomainDriverManager ddManager, String sGroupId) throws AdminException {
    try {
      ddManager.holdOrganizationSchema();
      GroupRow gr = ddManager.getOrganization().group.getGroup(idAsInt(sGroupId));

      GroupDetail group = new GroupDetail();

      if (gr != null) {
        group.setId(idAsString(gr.id));
        group.setSpecificId(gr.specificId);
        group.setDomainId(idAsString(gr.domainId));
        group.setSuperGroupId(idAsString(gr.superGroupId));
        group.setName(gr.name);
        group.setDescription(gr.description);
        group.setRule(gr.rule);

        // Get the selected users for this group
        setDirectUsersOfGroup(group);
      }

      return group;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("group", sGroupId), e);
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
      throw new AdminException(failureOnGetting("recursively all subgroups of group", superGroupId),
          e);
    } finally {
      DBUtil.close(con);
    }
  }

  private List<String> getSubGroupIds(Connection con, String groupId) throws SQLException {
    List<String> groupIds = new ArrayList<>();
    List<GroupDetail> groups = groupDao.getSubGroups(con, groupId);
    for (GroupDetail group : groups) {
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
  public GroupDetail getGroupByNameInDomain(DomainDriverManager ddManager, String sGroupName,
          String sDomainFatherId) throws AdminException {
    try {
      ddManager.holdOrganizationSchema();
      GroupDetail group = ddManager.getGroupByNameInDomain(sGroupName, sDomainFatherId);

      if (group != null) {
        String specificId = group.getSpecificId();
        GroupRow gr = ddManager.getOrganization().group.getGroupBySpecificId(
                idAsInt(sDomainFatherId),
                specificId);
        if (gr != null) {
          group.setId(idAsString(gr.id));
          // Get the selected users for this group
          setDirectUsersOfGroup(group);
        } else {
          return null;
        }
      }
      return group;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("group by name", sGroupName), e);
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
  public GroupDetail[] getRootGroupsOfDomain(DomainDriverManager ddManager, String sDomainId) throws
          AdminException {
    try {
      // Get organization
      ddManager.holdOrganizationSchema();
      // Get groups of domain from Silverpeas database
      GroupRow[] grs =
              ddManager.getOrganization().group.getAllRootGroupsOfDomain(idAsInt(sDomainId));
      // Convert GroupRow objects in GroupDetail Object
      GroupDetail[] groups = new GroupDetail[grs.length];
      for (int nI = 0; nI < grs.length; nI++) {
        groups[nI] = groupRow2Group(grs[nI]);
        // Get the selected users for this group
        setDirectUsersOfGroup(groups[nI]);
      }
      return groups;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("root groups in domain", sDomainId), e);
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
  public GroupDetail[] getSynchronizedGroups(DomainDriverManager ddManager) throws AdminException {
    try {
      // Get organization
      ddManager.holdOrganizationSchema();
      // Get groups of domain from Silverpeas database
      GroupRow[] grs = ddManager.getOrganization().group.getSynchronizedGroups();
      // Convert GroupRow objects in GroupDetail Object
      GroupDetail[] groups = new GroupDetail[grs.length];
      for (int nI = 0; nI < grs.length; nI++) {
        groups[nI] = groupRow2Group(grs[nI]);
      }
      return groups;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("synchronized groups", ""), e);
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
      ddManager.holdOrganizationSchema();
      // Get groups of domain from Silverpeas database
      String[] groupIds = ddManager.getOrganization().group.getAllRootGroupIdsOfDomain(idAsInt(
              sDomainId));
      if (groupIds != null) {
        return groupIds;
      }
      return ArrayUtil.EMPTY_STRING_ARRAY;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("root groups in domain", sDomainId), e);
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
  public GroupDetail[] getGroupsOfDomain(DomainDriverManager ddManager, String sDomainId) throws
          AdminException {
    try {
      // Get organization
      ddManager.holdOrganizationSchema();
      SynchroDomainReport.info("GroupManager.getGroupsOfDomain()",
              "Recherche des groupes du domaine LDAP dans la base...");
      // Get groups of domain from Silverpeas database
      GroupRow[] grs = ddManager.getOrganization().group.getAllGroupsOfDomain(idAsInt(sDomainId));
      // Convert GroupRow objects in GroupDetail Object
      GroupDetail[] groups = new GroupDetail[grs.length];
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
      throw new AdminException(failureOnGetting("groups in domain", sDomainId), e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  public String[] searchGroupsIds(DomainDriverManager ddManager, boolean isRootGroup,
          int componentId, String[] aProfileId, GroupDetail modelGroup) throws AdminException {
    try {
      // Get organization
      ddManager.holdOrganizationSchema();
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
      throw new AdminException("Fail to search groups", e);
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
  public GroupDetail[] searchGroups(DomainDriverManager ddManager, GroupDetail modelGroup, boolean isAnd) throws
          AdminException {
    try {
      // Get organization
      ddManager.holdOrganizationSchema();
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
      // Convert GroupRow objects in GroupDetail Object
      GroupDetail[] groups = new GroupDetail[grs.length];
      for (int nI = 0; nI < grs.length; nI++) {
        groups[nI] = groupRow2Group(grs[nI]);
        // Get the selected users for this group
        setDirectUsersOfGroup(groups[nI]);
      }
      return groups;
    } catch (Exception e) {
      throw new AdminException("Fail to search group", e);
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
  public String addGroup(DomainDriverManager ddManager, GroupDetail group, boolean onlyInSilverpeas)
          throws AdminException {
    if (group == null || !StringUtil.isDefined(group.getName())) {
      if (group != null) {
        SynchroDomainReport.error("GroupManager.addGroup()", "Problème lors de l'ajout du groupe "
                + group.getSpecificId() + " dans la base, ce groupe n'a pas de nom", null);
      }
      return "";
    }

    try {
      ddManager.holdOrganizationSchema();
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
      throw new AdminException(failureOnAdding("group", group.getName()), e);
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
  public String deleteGroupById(DomainDriverManager ddManager, GroupDetail group,
          boolean onlyInSilverpeas) throws AdminException {
    try {
      ddManager.holdOrganizationSchema();
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
      throw new AdminException(failureOnDeleting("group", group.getId()), e);
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
  public String updateGroup(DomainDriverManager ddManager, GroupDetail group, boolean onlyInSilverpeas)
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
      ddManager.holdOrganizationSchema();
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

      try (Connection connection = DBUtil.openConnection()) {
        List<String> asOldUsersId = userDao.getDirectUserIdsInGroup(connection, sGroupId);

        // Compute the remove users list
        List<String> asNewUsersId = Arrays.asList(group.getUserIds());
        asOldUsersId.stream()
            .filter(u -> !asNewUsersId.contains(u))
            .forEach(u -> alRemUsers.add(u));
        // Compute the add users list
        asNewUsersId.stream()
            .filter(u -> !asOldUsersId.contains(u))
            .forEach(u -> alAddUsers.add(u));
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
      throw new AdminException(failureOnUpdate("group", group.getId()), e);
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
      ddManager.holdOrganizationSchema();
      String[] asGroupIds = this.getAllGroupIds(ddManager);
      GroupDetail[] aGroup = new GroupDetail[asGroupIds.length];
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
      throw new AdminException(failureOnGetting("admin organization", ""), e);
    } finally {
      ddManager.releaseOrganizationSchema();
    }
  }

  /**
   * Get the list of children groups of the given group
   */
  private ArrayList<AdminGroupInst> getChildrenGroupInst(DomainDriverManager ddManager,
          String sFatherGroupId, GroupDetail[] aGroup) {
    ArrayList<AdminGroupInst> alChildrenGroupInst = new ArrayList<>();

    // Search the children group
    for (GroupDetail anAGroup : aGroup) {
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
      throw new AdminException(failureOnGetting("groups manageable by user", userId), e);
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
      throw new AdminException(failureOnGetting("user count in group", groupId), e);
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
      throw new AdminException(failureOnGetting("users in group", groupId), e);
    } finally {
      DBUtil.close(con);
    }
  }

  private void setDirectUsersOfGroup(final GroupDetail group) throws
          AdminPersistenceException {
    try(Connection connection = DBUtil.openConnection()) {
      List<String> userIds = userDao
          .getDirectUserIdsInGroup(connection, group.getId());
      group.setUserIds(userIds.toArray(new String[userIds.size()]));
    } catch (Exception e) {
      throw new AdminPersistenceException(e);
    }
  }

  /**
   * Convert GroupRow to GroupDetail
   */
  private GroupDetail groupRow2Group(GroupRow gr) {
    GroupDetail group = new GroupDetail();

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
   * Convert GroupDetail to GroupRow
   */
  private GroupRow group2GroupRow(GroupDetail group) {
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