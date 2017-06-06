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
import org.silverpeas.core.admin.persistence.GroupRow;
import org.silverpeas.core.admin.persistence.OrganizationSchema;
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
import java.util.stream.Collectors;

import static org.silverpeas.core.SilverpeasExceptionMessages.*;

@Singleton
@Transactional(Transactional.TxType.MANDATORY)
public class GroupManager {

  public static final String GROUP = "group";
  public static final String GROUP_MANAGER_GET_GROUPS_OF_DOMAIN =
      "GroupManager.getGroupsOfDomain()";
  public static final String GROUP_MANAGER_ADD_GROUP = "GroupManager.addGroup()";
  public static final String GROUP_MANAGER_UPDATE_GROUP = "GroupManager.updateGroup()";
  public static final String IN_GROUP = "in group ";
  @Inject
  private GroupDAO groupDao;
  @Inject
  private UserDAO userDao;
  @Inject
  private GroupEventNotifier notifier;
  @Inject
  private OrganizationSchema organizationSchema;
  @Inject
  private DomainDriverManager domainDriverManager;

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
      return userDao.getUserCountByCriteria(connection, criteriaOnUsers.
          onDomainId(domainId).
          onGroupIds(groupIds.toArray(new String[groupIds.size()])));
    } catch (SQLException e) {
      throw new AdminException(e.getMessage(), e);
    } finally {
      DBUtil.close(connection);
    }
  }

  /**
   * Add a user to a group
   *
   * @param sUserId
   * @param sGroupId
   * @throws AdminException
   */
  public void addUserInGroup(String sUserId, String sGroupId) throws
          AdminException {
    try {
      organizationSchema.group().addUserInGroup(idAsInt(sUserId), idAsInt(sGroupId));
    } catch (Exception e) {
      throw new AdminException(failureOnAdding("user " + sUserId, IN_GROUP + sGroupId), e);
    }
  }

  public void addUsersInGroup(final List<String> userIds, final String groupId)
      throws AdminException {
    try {
      organizationSchema.group()
          .addUsersInGroup(userIds.toArray(new String[userIds.size()]), Integer.parseInt(groupId),
              false);
    } catch (SQLException e) {
      throw new AdminException(e.getMessage(), e);
    }
  }

  /**
   * Remove a user from a group
   *
   * @param sUserId
   * @param sGroupId
   * @throws AdminException
   */
  public void removeUserFromGroup(String sUserId, String sGroupId)
          throws AdminException {
    try {
      organizationSchema.group().removeUserFromGroup(idAsInt(sUserId), idAsInt(sGroupId));
    } catch (Exception e) {
      throw new AdminException(failureOnDeleting("user " + sUserId, IN_GROUP + sGroupId), e);
    }
  }

  public void removeUsersFromGroup(final List<String> userIds, final String groupId)
      throws AdminException {

    try {
      organizationSchema.group()
          .removeUsersFromGroup(userIds.toArray(new String[userIds.size()]),
              Integer.parseInt(groupId), false);
    } catch (SQLException e) {
      throw new AdminException(
          failureOnDeleting("users " + userIds.stream().collect(Collectors.joining("n")),
              IN_GROUP + groupId), e);
    }
  }

  /**
   * Get the direct groups id containing a user. Groups that the user is linked to by
   * transitivity are not returned.
   * @param sUserId
   * @return
   * @throws AdminException
   */
  public String[] getDirectGroupsOfUser(String sUserId) throws
          AdminException {
    try {
      GroupRow[] grs = organizationSchema.group().getDirectGroupsOfUser(idAsInt(sUserId));
      // Convert GroupRow objects in GroupDetail Object
      String[] groups = new String[grs.length];
      for (int nI = 0; nI < grs.length; nI++) {
        groups[nI] = idAsString(grs[nI].id);
      }
      return groups;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("direct groups of user", sUserId), e);
    }
  }

  /**
   * Get all group ids containing a user. So, groups that the user is linked to by
   * transitivity are returned too (recursive treatment).
   * @param userId
   * @return
   * @throws AdminException
   */
  public List<String> getAllGroupsOfUser(String userId) throws AdminException {
    Set<String> allGroupsOfUser = new HashSet<>();

    String[] directGroupIds = getDirectGroupsOfUser(userId);
    for (String directGroupId : directGroupIds) {
      GroupDetail group = getGroupDetail(directGroupId);
      if (group != null) {
        allGroupsOfUser.add(group.getId());
        while (group != null && StringUtil.isDefined(group.getSuperGroupId())) {
          group = getGroupDetail(group.getSuperGroupId());
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
   * @param sSpecificId
   * @param sDomainId
   * @return
   * @throws AdminException
   */
  public String getGroupIdBySpecificIdAndDomainId(String sSpecificId,
          String sDomainId) throws AdminException {
    try {
      GroupRow gr = organizationSchema.group().getGroupBySpecificId(
              idAsInt(sDomainId), sSpecificId);
      return idAsString(gr.id);
    } catch (Exception e) {
      throw new AdminException(
          failureOnGetting("groups by specific id and domain", sSpecificId + "/" + sDomainId), e);
    }
  }

  /**
   * Get the all the groups id available in Silverpeas
   *
   * @return
   * @throws AdminException
   */
  public String[] getAllGroupIds() throws AdminException {
    try {
      String[] asGroupIds = organizationSchema.group().getAllGroupIds();

      if (asGroupIds != null) {
        return asGroupIds;
      }
      return ArrayUtil.EMPTY_STRING_ARRAY;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("all groups", ""), e);
    }
  }

  /**
   * Get the all the root groups id available in Silverpeas
   *
   * @return
   * @throws AdminException
   */
  public String[] getAllRootGroupIds() throws AdminException {
    try {
      String[] asGroupIds = organizationSchema.group().getAllRootGroupIds();
      if (asGroupIds != null) {
        return asGroupIds;
      }
      return ArrayUtil.EMPTY_STRING_ARRAY;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("all root groups", ""), e);
    }
  }

  /**
   * Gets all the root user groups in Silverpeas.
   *
   * @return an array of root user groups.
   * @throws AdminException if an error occurs while getting the root groups.
   */
  public GroupDetail[] getAllRootGroups() throws AdminException {
    try {
      GroupRow[] rows = organizationSchema.group().getAllRootGroups();
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
    }
  }

  /**
   * Get the all the direct sub groups id of a given group
   *
   * @param superGroupId
   * @return
   * @throws AdminException
   */
  public String[] getAllSubGroupIds(String superGroupId) throws
          AdminException {
    try {
      String[] asGroupIds = organizationSchema.group().getDirectSubGroupIds(idAsInt(
              superGroupId));
      if (asGroupIds != null) {
        return asGroupIds;
      }
      return ArrayUtil.EMPTY_STRING_ARRAY;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("all subgroups of group", superGroupId), e);
    }
  }

  /**
   * Get the path from root to a given group
   *
   * @param groupId
   * @return
   * @throws AdminException
   */
  public List<String> getPathToGroup(String groupId) throws
          AdminException {
    try {
      List<String> path = new ArrayList<>();
      GroupRow superGroup = organizationSchema.group().getSuperGroup(idAsInt(groupId));
      while (superGroup != null) {
        path.add(0, idAsString(superGroup.id));
        superGroup = organizationSchema.group().getSuperGroup(superGroup.id);
      }

      return path;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("path to group", groupId), e);
    }
  }

  /**
   * /**
   * Check if the given group exists
   *
   * @param sName
   * @return true if a group with the given name
   * @throws AdminException
   */
  public boolean isGroupExist(String sName) throws AdminException {
    try {
      // build GroupRow to search
      GroupRow searchedGroup = new GroupRow();
      searchedGroup.specificId = null;
      searchedGroup.name = sName;
      searchedGroup.description = null;

      // search for group
      GroupRow[] group = organizationSchema.group().getAllMatchingGroups(searchedGroup);

      return (group.length > 0);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("all groups matching name", sName), e);
    }
  }

  private GroupDetail getGroupDetail(String groupId) throws AdminException {
    try(Connection connection = DBUtil.openConnection()) {
      return groupDao.getGroup(connection, groupId);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting(GROUP, groupId), e);
    }
  }

  /**
   * Get group information with the given id from Silverpeas
   *
   * @param sGroupId
   * @return
   * @throws AdminException
   */
  public GroupDetail getGroup(String sGroupId) throws AdminException {
    GroupDetail group = getGroupDetail(sGroupId);
    if (group != null) {
      // Get the selected users for this group
      setDirectUsersOfGroup(group);
    }
    return group;
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
   * @param sGroupName
   * @param sDomainFatherId
   * @return
   * @throws AdminException
   */
  public GroupDetail getGroupByNameInDomain(String sGroupName,
          String sDomainFatherId) throws AdminException {
    try {
      GroupDetail group = domainDriverManager.getGroupByNameInDomain(sGroupName, sDomainFatherId);

      if (group != null) {
        String specificId = group.getSpecificId();
        GroupRow gr = organizationSchema.group().getGroupBySpecificId(
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
    }
  }

  /**
   *
   * @param sDomainId
   * @return
   * @throws AdminException
   */
  public GroupDetail[] getRootGroupsOfDomain(String sDomainId) throws
          AdminException {
    try {
      // Get groups of domain from Silverpeas database
      GroupRow[] grs = organizationSchema.group().getAllRootGroupsOfDomain(idAsInt(sDomainId));
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
    }
  }

  /**
   *
   * @return
   * @throws AdminException
   */
  public GroupDetail[] getSynchronizedGroups() throws AdminException {
    try {
      // Get groups of domain from Silverpeas database
      GroupRow[] grs = organizationSchema.group().getSynchronizedGroups();
      // Convert GroupRow objects in GroupDetail Object
      GroupDetail[] groups = new GroupDetail[grs.length];
      for (int nI = 0; nI < grs.length; nI++) {
        groups[nI] = groupRow2Group(grs[nI]);
      }
      return groups;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("synchronized groups", ""), e);
    }
  }

  /**
   *
   * @param sDomainId
   * @return
   * @throws AdminException
   */
  public String[] getRootGroupIdsOfDomain(String sDomainId) throws
          AdminException {
    try {
      // Get groups of domain from Silverpeas database
      String[] groupIds = organizationSchema.group().getAllRootGroupIdsOfDomain(idAsInt(
              sDomainId));
      if (groupIds != null) {
        return groupIds;
      }
      return ArrayUtil.EMPTY_STRING_ARRAY;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("root groups in domain", sDomainId), e);
    }
  }

  /**
   * Get the groups of domain
   *
   * @param sDomainId
   * @return
   * @throws AdminException
   */
  public GroupDetail[] getGroupsOfDomain(String sDomainId) throws
          AdminException {
    try {
      // Get organization
      SynchroDomainReport.info(GROUP_MANAGER_GET_GROUPS_OF_DOMAIN,
              "Recherche des groupes du domaine LDAP dans la base...");
      // Get groups of domain from Silverpeas database
      GroupRow[] grs = organizationSchema.group().getAllGroupsOfDomain(idAsInt(sDomainId));
      // Convert GroupRow objects in GroupDetail Object
      GroupDetail[] groups = new GroupDetail[grs.length];
      for (int nI = 0; nI < grs.length; nI++) {
        groups[nI] = groupRow2Group(grs[nI]);
        SynchroDomainReport.debug(
            GROUP_MANAGER_GET_GROUPS_OF_DOMAIN, "Groupe trouvé no : " + Integer.
                toString(nI) + ", specificID : " + groups[nI].getSpecificId() + ", desc. : "
                + groups[nI].getDescription());
      }
      SynchroDomainReport.info(GROUP_MANAGER_GET_GROUPS_OF_DOMAIN,
              "Récupération de " + grs.length + " groupes du domaine LDAP dans la base");
      return groups;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("groups in domain", sDomainId), e);
    }
  }

  public String[] searchGroupsIds(boolean isRootGroup,
          int componentId, String[] aProfileId, GroupDetail modelGroup) throws AdminException {
    try {
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
      return organizationSchema.group().searchGroupsIds(isRootGroup,
              componentId, aRoleId, model);
    } catch (Exception e) {
      throw new AdminException("Fail to search groups", e);
    }
  }

  /**
   *
   * @param modelGroup
   * @param isAnd
   * @return
   * @throws AdminException
   */
  public GroupDetail[] searchGroups(GroupDetail modelGroup, boolean isAnd) throws
          AdminException {
    try {
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
      GroupRow[] grs = organizationSchema.group().searchGroups(model, isAnd);
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
    }
  }

  /**
   * Add the given group in Silverpeas
   *
   * @param group
   * @param onlyInSilverpeas
   * @return
   * @throws AdminException
   */
  public String addGroup(GroupDetail group, boolean onlyInSilverpeas)
          throws AdminException {
    if (group == null || !StringUtil.isDefined(group.getName())) {
      if (group != null) {
        SynchroDomainReport.error(GROUP_MANAGER_ADD_GROUP, "Problème lors de l'ajout du groupe "
                + group.getSpecificId() + " dans la base, ce groupe n'a pas de nom", null);
      }
      return "";
    }

    try {
      // Create group in specific domain (if onlyInSilverpeas is not true)
      // if domainId=-1 then group is a silverpeas organization
      if (!onlyInSilverpeas) {
        String specificId;
        if (group.getDomainId() != null) {
          specificId = domainDriverManager.createGroup(group);
          group.setSpecificId(specificId);
        }
      }
      // Create the group node in Silverpeas
      GroupRow gr = group2GroupRow(group);
      if (gr.superGroupId != -1) {
        SynchroDomainReport.info(GROUP_MANAGER_ADD_GROUP, "Ajout du groupe " + group.getName()
                + " (père=" + getGroupDetail(group.getSuperGroupId()).getSpecificId()
                + ") dans la table ST_Group");
      } else { // pas de père
        SynchroDomainReport.info(GROUP_MANAGER_ADD_GROUP, "Ajout du groupe " + group.getName()
                + " (groupe racine) dans la table ST_Group...");
      }
      organizationSchema.group().createGroup(gr);
      String sGroupId = idAsString(gr.id);
      group.setId(sGroupId);
      notifier.notifyEventOn(ResourceEvent.Type.CREATION, group);

      // index group information
      domainDriverManager.indexGroup(gr);

      // Create the links group_user in Silverpeas
      SynchroDomainReport.info(GROUP_MANAGER_ADD_GROUP,
              "Inclusion des utilisateurs directement associés au groupe " + group.getName()
              + " (table ST_Group_User_Rel)");
      String[] asUserIds = group.getUserIds();
      int nUserAdded = 0;
      for (String asUserId : asUserIds) {
        if (StringUtil.isDefined(asUserId)) {
          organizationSchema.group().addUserInGroup(idAsInt(asUserId), idAsInt(sGroupId));
          nUserAdded++;
        }
      }
      SynchroDomainReport.info(
          GROUP_MANAGER_ADD_GROUP, nUserAdded + " utilisateurs ajoutés au groupe "
              + group.getName() + " dans la base");
      return sGroupId;
    } catch (Exception e) {
      SynchroDomainReport.error(GROUP_MANAGER_ADD_GROUP, "problème lors de l'ajout du groupe "
              + group.getName() + " - " + e.getMessage(), null);
      throw new AdminException(failureOnAdding(GROUP, group.getName()), e);
    }
  }

  /**
   * Delete the group with the given Id The delete is apply recursively to the sub-groups
   *
   * @param group
   * @param onlyInSilverpeas
   * @return
   * @throws AdminException
   */
  public String deleteGroupById(GroupDetail group,
          boolean onlyInSilverpeas) throws AdminException {
    try {
      if (group.getDomainId() != null && !onlyInSilverpeas) {
        domainDriverManager.deleteGroup(group.getId());
      }
      // Delete the group node from Silverpeas
      organizationSchema.group().removeGroup(idAsInt(group.getId()));
      notifier.notifyEventOn(ResourceEvent.Type.DELETION, group);

      // Delete index of group information
      domainDriverManager.unindexGroup(group.getId());

      return group.getId();
    } catch (Exception e) {
      SynchroDomainReport.error("GroupManager.deleteGroupById()",
              "problème lors de la suppression du groupe " + group.getName()
              + " - " + e.getMessage(), null);
      throw new AdminException(failureOnDeleting(GROUP, group.getId()), e);
    }
  }

  /**
   * Update the given group
   *
   * @param group
   * @param onlyInSilverpeas
   * @return
   * @throws AdminException
   */
  public String updateGroup(GroupDetail group, boolean onlyInSilverpeas)
          throws AdminException {
    ArrayList<String> alRemUsers = new ArrayList<>();
    ArrayList<String> alAddUsers = new ArrayList<>();

    if (group == null || !StringUtil.isDefined(group.getName()) || !StringUtil.isDefined(
            group.getId())) {
      if (group != null) {
        SynchroDomainReport.error(GROUP_MANAGER_UPDATE_GROUP, "Problème lors de maj du groupe "
                + group.getSpecificId() + " dans la base, ce groupe n'a pas de nom", null);
      }
      return "";
    }
    try {
      if (group.getDomainId() != null && !onlyInSilverpeas) {
        domainDriverManager.updateGroup(group);
      }
      // Get the group id
      String sGroupId = group.getId();
      String strInfoSycnhro;
      if (group.getSuperGroupId() != null) {
        strInfoSycnhro = "Maj du groupe " + group.getName() + " (père=" + getGroupDetail(group.
                getSuperGroupId()).getSpecificId() + ") dans la base (table ST_Group)...";
      } else {
        strInfoSycnhro = "Maj du groupe " + group.getName()
                + " (groupe racine) dans la base (table ST_Group)...";
      }
      SynchroDomainReport.info(GROUP_MANAGER_UPDATE_GROUP, strInfoSycnhro);
      // Update the group node
      GroupRow gr = group2GroupRow(group);
      organizationSchema.group().updateGroup(gr);

      // index group information
      domainDriverManager.indexGroup(gr);

      // Update the users if necessary
      SynchroDomainReport.info(GROUP_MANAGER_UPDATE_GROUP, "Maj éventuelle des relations du groupe "
              + group.getName()
              + " avec les utilisateurs qui y sont directement inclus (tables ST_Group_User_Rel)");

      try (Connection connection = DBUtil.openConnection()) {
        List<String> asOldUsersId = userDao.getDirectUserIdsInGroup(connection, sGroupId);

        // Compute the remove users list
        List<String> asNewUsersId = Arrays.asList(group.getUserIds());
        asOldUsersId.stream()
            .filter(u -> !asNewUsersId.contains(u))
            .forEach(alRemUsers::add);
        // Compute the add users list
        asNewUsersId.stream()
            .filter(u -> !asOldUsersId.contains(u))
            .forEach(alAddUsers::add);
      }
      // Remove the users that are not in this group anymore
      for (String alRemUser : alRemUsers) {
        organizationSchema.group().removeUserFromGroup(
                idAsInt(alRemUser), idAsInt(sGroupId));
      }

      // Add the new users of the group
      for (String alAddUser : alAddUsers) {
        organizationSchema.group().addUserInGroup(idAsInt(alAddUser), idAsInt(sGroupId));
      }

      SynchroDomainReport.info(GROUP_MANAGER_UPDATE_GROUP, "Groupe : "
              + group.getName() + ", ajout de " + alAddUsers.size()
              + " nouveaux utilisateurs, suppression de " + alRemUsers.size()
              + " utilisateurs");
      return sGroupId;
    } catch (Exception e) {
      SynchroDomainReport.error(GROUP_MANAGER_UPDATE_GROUP,
              "problème lors de la maj du groupe " + group.getName() + " - "
              + e.getMessage(), null);
      throw new AdminException(failureOnUpdate(GROUP, group.getId()), e);
    }
  }

  /**
   * Get Silverpeas admin organization
   *
   * @return an array of AdminGroupInst containing the organization
   * @throws AdminException
   */
  public AdminGroupInst[] getAdminOrganization() throws AdminException {
    try {
      String[] asGroupIds = this.getAllGroupIds();
      GroupDetail[] aGroup = new GroupDetail[asGroupIds.length];
      for (int nI = 0; nI < asGroupIds.length; nI++) {
        aGroup[nI] = this.getGroupDetail(asGroupIds[nI]);
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
        aAdminGroupInst[nI].setChildrenAdminGroupInst(
            this.getChildrenGroupInst(aAdminGroupInst[nI].getGroup().getId(), aGroup));
      }
      return aAdminGroupInst;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("admin organization", ""), e);
    }
  }

  /**
   * Get the list of children groups of the given group
   */
  private ArrayList<AdminGroupInst> getChildrenGroupInst(String sFatherGroupId,
      GroupDetail[] aGroup) {
    ArrayList<AdminGroupInst> alChildrenGroupInst = new ArrayList<>();

    // Search the children group
    for (GroupDetail anAGroup : aGroup) {
      if (anAGroup.getSuperGroupId() != null && anAGroup.getSuperGroupId().equals(sFatherGroupId)) {
        AdminGroupInst adminGroupInst = new AdminGroupInst();
        adminGroupInst.setGroup(anAGroup);
        adminGroupInst.setChildrenAdminGroupInst(
            this.getChildrenGroupInst(adminGroupInst.getGroup().getId(), aGroup));
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
          AdminException {
    try(Connection connection = DBUtil.openConnection()) {
      List<String> userIds = userDao
          .getDirectUserIdsInGroup(connection, group.getId());
      group.setUserIds(userIds.toArray(new String[userIds.size()]));
    } catch (Exception e) {
      throw new AdminException(e);
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
  private static String idAsString(int id) {
    if (id == -1) {
      return null;
    } else {
      return Integer.toString(id);
    }
  }
}