/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.persistence;

import org.silverpeas.core.admin.domain.synchro.SynchroDomainReport;
import org.silverpeas.core.admin.domain.synchro.SynchroGroupReport;
import org.silverpeas.core.admin.user.model.GroupCache;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.exception.SilverpeasException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A GroupTable object manages the ST_Group table.
 */
public class GroupTable extends Table<GroupRow> {

  public GroupTable(OrganizationSchema schema) {
    super(schema, "ST_Group");
    this.organization = schema;
  }

  static final private String GROUP_COLUMNS =
      "id, specificId, domainId, superGroupId, name, " + "description, synchroRule";
  static final private String SELECT_GROUP_BY_ID =
      "SELECT id, specificId, domainId, superGroupId, " +
          "name, description, synchroRule FROM ST_Group WHERE id = ?";
  static final private String SELECT_GROUP_BY_SPECIFICID = "SELECT id, specificId, domainId, " +
      "superGroupId, name, description, synchroRule FROM ST_Group WHERE " +
      "domainId = ? AND specificId = ?";
  static final private String SELECT_ROOT_GROUP_BY_NAME = "SELECT id, specificId, domainId, " +
      "superGroupId, name, description, synchroRule FROM ST_Group WHERE " +
      "superGroupId IS NULL AND name = ?";
  static final private String SELECT_GROUP_BY_NAME = "SELECT id, specificId, domainId, " +
      "superGroupId, name, description, synchroRule FROM ST_Group WHERE " +
      "superGroupId = ? AND name = ?";

  /**
   * Fetch the current group row from a resultSet.
   * @param rs
   * @return
   * @throws SQLException
   */
  protected GroupRow fetchGroup(ResultSet rs) throws SQLException {
    GroupRow g = new GroupRow();
    g.id = rs.getInt("id");
    g.specificId = rs.getString("specificId");
    if ("-1".equals(g.specificId)) {
      g.specificId = null;
    }
    g.domainId = rs.getInt("domainId");
    g.superGroupId = rs.getInt("superGroupId");
    if (rs.wasNull()) {
      g.superGroupId = -1;
    }
    g.name = rs.getString("name");
    g.description = rs.getString("description");
    g.rule = rs.getString("synchroRule");
    return g;
  }

  /**
   * Returns the Group whith the given id.
   * @param id
   * @return the Group whith the given id.
   * @throws AdminPersistenceException
   */
  public GroupRow getGroup(int id) throws AdminPersistenceException {
    return getUniqueRow(SELECT_GROUP_BY_ID, id);
  }

  /**
   * Returns Group whith the given specificId and domainId.
   * @param domainId
   * @param specificId
   * @return Group whith the given specificId and domainId.
   * @throws AdminPersistenceException
   */
  public GroupRow getGroupBySpecificId(int domainId, String specificId)
      throws AdminPersistenceException {
    List<Object> params = new ArrayList<>(2);
    params.add(domainId);
    params.add(specificId);
    List<GroupRow> groups = getRows(SELECT_GROUP_BY_SPECIFICID, params);
    if (groups.isEmpty()) {
      return null;
    }
    if (groups.size() == 1) {
      return groups.get(0);
    }
    throw new AdminPersistenceException("GroupTable.getGroupBySpecificId",
        SilverpeasException.ERROR, "admin.EX_ERR_GROUP_SPECIFIC_ID_FOUND_TWICE",
        "domain Id: '" + domainId + "', specific Id: '" + specificId + "'");

  }

  /**
   * Returns the root Group whith the given name.
   * @param name
   * @return the root Group whith the given name.
   * @throws AdminPersistenceException
   */
  public GroupRow getRootGroup(String name) throws AdminPersistenceException {
    List<GroupRow> groups = getRows(SELECT_ROOT_GROUP_BY_NAME, Collections.singletonList(name));
    if (groups.isEmpty()) {
      return null;
    }
    if (groups.size() == 1) {
      return groups.get(0);
    }
    throw new AdminPersistenceException("GroupTable.getRootGroup", SilverpeasException.ERROR,
        "admin.EX_ERR_GROUP_NAME_FOUND_TWICE", "group name: '" + name + "'");
  }

  /**
   * Returns the Group whith the given name in the given super group.
   * @param superGroupId
   * @param name
   * @return the Group whith the given name in the given super group.
   * @throws AdminPersistenceException
   */
  public GroupRow getGroup(int superGroupId, String name) throws AdminPersistenceException {
    List<Object> params = new ArrayList<>(2);
    params.add(superGroupId);
    params.add(name);
    List<GroupRow> groups = getRows(SELECT_GROUP_BY_NAME, params);
    if (groups.isEmpty()) {
      return null;
    }
    if (groups.size() == 1) {
      return groups.get(0);
    }
    throw new AdminPersistenceException("GroupTable.getGroup", SilverpeasException.ERROR,
        "admin.EX_ERR_GROUP_NAME_ID_FOUND_TWICE",
        "group name: '" + name + "', father group id: '" + superGroupId + "'");
  }

  /**
   * Returns all the Groups.
   * @return all the Groups.
   * @throws AdminPersistenceException
   */
  public GroupRow[] getAllGroups() throws AdminPersistenceException {
    List<GroupRow> rows = getRows(SELECT_ALL_GROUPS);
    return rows.toArray(new GroupRow[rows.size()]);
  }

  static final private String SELECT_ALL_GROUPS = "select " + GROUP_COLUMNS + " from ST_Group";

  /**
   * Returns all the Groups.
   */
  public GroupRow[] getSynchronizedGroups() throws AdminPersistenceException {
    List<GroupRow> rows = getRows(SELECT_SYNCHRONIZED_GROUPS);
    return rows.toArray(new GroupRow[rows.size()]);
  }

  static final private String SELECT_SYNCHRONIZED_GROUPS =
      "select " + GROUP_COLUMNS + " from ST_Group where synchroRule is not null";

  /**
   * Returns all the Group ids.
   * @return all the Group ids.
   * @throws AdminPersistenceException
   */
  public String[] getAllGroupIds() throws AdminPersistenceException {
    List<String> ids = getIds(SELECT_ALL_GROUP_IDS);
    return ids.toArray(new String[ids.size()]);
  }

  static final private String SELECT_ALL_GROUP_IDS = "select id from ST_Group";

  /**
   * Returns all the Groups without a superGroup.
   * @return all the Groups without a superGroup.
   * @throws AdminPersistenceException
   */
  public GroupRow[] getAllRootGroups() throws AdminPersistenceException {
    List<GroupRow> rows = getRows(SELECT_ALL_ROOT_GROUPS);
    return rows.toArray(new GroupRow[rows.size()]);
  }

  static final private String SELECT_ALL_ROOT_GROUPS = "select " + GROUP_COLUMNS +
      ", UPPER(name) from ST_Group where superGroupId is null order by UPPER(name)";

  /**
   * Returns all the Groups without a superGroup.
   * @return all the Groups without a superGroup.
   * @throws AdminPersistenceException
   */
  public String[] getAllRootGroupIds() throws AdminPersistenceException {
    List<String> ids = getIds(SELECT_ALL_ROOT_GROUP_IDS);
    return ids.toArray(new String[ids.size()]);
  }

  static final private String SELECT_ALL_ROOT_GROUP_IDS =
      "select id, UPPER(name) from ST_Group where superGroupId is null order by UPPER(name)";

  /**
   * Returns all the Groups having a given superGroup.
   * @param superGroupId
   * @return all the Groups having a given superGroup.
   * @throws AdminPersistenceException
   */
  public GroupRow[] getDirectSubGroups(int superGroupId) throws AdminPersistenceException {
    List<GroupRow> rows = getRows(SELECT_SUBGROUPS, superGroupId);
    return rows.toArray(new GroupRow[rows.size()]);
  }

  static final private String SELECT_SUBGROUPS =
      "select " + GROUP_COLUMNS + " from ST_Group where superGroupId = ?";

  /**
   * Returns all the Group ids having a given superGroup.
   * @param superGroupId
   * @return
   * @throws AdminPersistenceException
   */
  public String[] getDirectSubGroupIds(int superGroupId) throws AdminPersistenceException {
    List<String> ids = getIds(SELECT_SUBGROUP_IDS, superGroupId);
    return ids.toArray(new String[ids.size()]);
  }

  static final private String SELECT_SUBGROUP_IDS =
      "select id from ST_Group where superGroupId = ?";

  /**
   * Returns all the Root Groups having a given domain id.
   * @param domainId
   * @return all the Root Groups having a given domain id.
   * @throws AdminPersistenceException
   */
  public GroupRow[] getAllRootGroupsOfDomain(int domainId) throws AdminPersistenceException {
    List<GroupRow> rows = getRows(SELECT_ALL_ROOT_GROUPS_IN_DOMAIN, domainId);
    return rows.toArray(new GroupRow[rows.size()]);
  }

  /**
   * Returns all the Root Group Ids having a given domain id.
   * @param domainId
   * @return all the Root Group Ids having a given domain id.
   * @throws AdminPersistenceException
   */
  public String[] getAllRootGroupIdsOfDomain(int domainId) throws AdminPersistenceException {
    List<String> ids = getIds(SELECT_ALL_ROOT_GROUPS_IDS_IN_DOMAIN, domainId);
    return ids.toArray(new String[ids.size()]);
  }

  static final private String SELECT_ALL_ROOT_GROUPS_IN_DOMAIN =
      "select " + GROUP_COLUMNS + " from ST_Group where (domainId=?) AND (superGroupId is null)";
  static final private String SELECT_ALL_ROOT_GROUPS_IDS_IN_DOMAIN =
      "select id from ST_Group where (domainId=?) AND (superGroupId is null)";

  /**
   * Returns all the Groups having a given domain id.
   * @param domainId
   * @return all the Groups having a given domain id.
   * @throws AdminPersistenceException
   */
  public GroupRow[] getAllGroupsOfDomain(int domainId) throws AdminPersistenceException {
    SynchroDomainReport.debug("GroupTable.getAllGroupsOfDomain()",
        "Recherche de l'ensemble des groupes du domaine LDAP dans la base (ID " + domainId +
            "), requête : " + SELECT_ALL_GROUPS_IN_DOMAIN);
    List<GroupRow> rows = getRows(SELECT_ALL_GROUPS_IN_DOMAIN, domainId);
    return rows.toArray(new GroupRow[rows.size()]);
  }

  static final private String SELECT_ALL_GROUPS_IN_DOMAIN =
      "select " + GROUP_COLUMNS + " from ST_Group where domainId=?";

  /**
   * Returns the superGroup of a given subGroup.
   * @param subGroupId
   * @return the superGroup of a given subGroup.
   * @throws AdminPersistenceException
   */
  public GroupRow getSuperGroup(int subGroupId) throws AdminPersistenceException {
    return getUniqueRow(SELECT_SUPERGROUP, subGroupId);
  }

  static final private String SELECT_SUPERGROUP = "select " + aliasColumns("sg", GROUP_COLUMNS) +
      " from ST_Group sg, ST_GROUP g where sg.id=g.superGroupId and g.id=?";

  /**
   * Returns all the groups of a given user (not recursive).
   * @param userId
   * @return all the groups of a given user (not recursive).
   * @throws AdminPersistenceException
   */
  public GroupRow[] getDirectGroupsOfUser(int userId) throws AdminPersistenceException {
    List<GroupRow> rows = getRows(SELECT_USER_GROUPS, userId);
    return rows.toArray(new GroupRow[rows.size()]);
  }

  static final private String SELECT_USER_GROUPS = "select " + GROUP_COLUMNS +
      " from ST_Group,ST_Group_User_Rel where id = groupId and userId = ?";

  /**
   * Returns all the groups in a given userRole (not recursive).
   * @param userRoleId
   * @return all the groups in a given userRole (not recursive).
   * @throws AdminPersistenceException
   */
  public String[] getDirectGroupIdsInUserRole(int userRoleId) throws AdminPersistenceException {
    List<String> ids = getIds(SELECT_USERROLE_GROUP_IDS, userRoleId);
    return ids.toArray(new String[ids.size()]);
  }

  static final private String SELECT_USERROLE_GROUP_IDS =
      "SELECT id FROM st_group, st_userrole_group_rel WHERE id = groupid AND userroleid = ?";

  /**
   * Returns all the group ids in a given spaceUserRole (not recursive).
   * @param spaceUserRoleId
   * @return all the group ids in a given spaceUserRole (not recursive).
   * @throws AdminPersistenceException
   */
  public String[] getDirectGroupIdsInSpaceUserRole(int spaceUserRoleId)
      throws AdminPersistenceException {
    List<String> ids = getIds(SELECT_SPACEUSERROLE_GROUP_IDS, spaceUserRoleId);
    return ids.toArray(new String[ids.size()]);
  }

  static final private String SELECT_SPACEUSERROLE_GROUP_IDS = "SELECT id FROM st_group, " +
      "st_spaceuserrole_group_rel WHERE id = groupId AND spaceUserRoleId = ?";

  /**
   * Returns all the groups in a given groupUserRole (not recursive).
   * @param groupUserRoleId
   * @return all the groups in a given groupUserRole (not recursive).
   * @throws AdminPersistenceException
   */
  public GroupRow[] getDirectGroupsInGroupUserRole(int groupUserRoleId)
      throws AdminPersistenceException {
    List<GroupRow> rows = getRows(SELECT_GROUPUSERROLE_GROUPS, groupUserRoleId);
    return rows.toArray(new GroupRow[rows.size()]);
  }

  static final private String SELECT_GROUPUSERROLE_GROUPS = "SELECT " + GROUP_COLUMNS +
      " FROM ST_Group, ST_GroupUserRole_Group_Rel WHERE id = groupId AND groupUserRoleId = ?";

  /**
   * Returns the Group of a given group user role.
   * @param groupUserRoleId
   * @return the Group of a given group user role.
   * @throws AdminPersistenceException
   */
  public GroupRow getGroupOfGroupUserRole(int groupUserRoleId) throws AdminPersistenceException {
    return getUniqueRow(SELECT_GROUPUSERROLE_GROUP, groupUserRoleId);
  }

  static final private String SELECT_GROUPUSERROLE_GROUP =
      "SELECT " + aliasColumns("i", GROUP_COLUMNS) + " FROM ST_Group i, ST_GroupUserRole us" +
          " WHERE i.id = us.groupId AND us.id = ?";

  /**
   * Returns all the group ids in a given groupUserRole (not recursive).
   * @param groupUserRoleId
   * @return all the group ids in a given groupUserRole (not recursive).
   * @throws AdminPersistenceException
   */
  public String[] getDirectGroupIdsInGroupUserRole(int groupUserRoleId)
      throws AdminPersistenceException {
    List<String> ids = getIds(SELECT_GROUPUSERROLE_GROUP_IDS, groupUserRoleId);
    return ids.toArray(new String[ids.size()]);
  }

  static final private String SELECT_GROUPUSERROLE_GROUP_IDS = "SELECT id FROM ST_Group, " +
      "ST_GroupUserRole_Group_Rel WHERE id = groupId AND groupUserRoleId = ?";

  /**
   * Returns the Group whose fields match those of the given sample group fields.
   * @param sampleGroup
   * @return the Group whose fields match those of the given sample group fields.
   * @throws AdminPersistenceException
   */
  public GroupRow[] getAllMatchingGroups(GroupRow sampleGroup) throws AdminPersistenceException {
    String[] columns = new String[]{"name", "description"};
    String[] values = new String[]{sampleGroup.name, sampleGroup.description};
    List<GroupRow> rows = getMatchingRows(GROUP_COLUMNS, columns, values);
    return rows.toArray(new GroupRow[rows.size()]);
  }

  /**
   * Returns all the Groups satifying the model that are direct childs of a specific group
   * @param isRootGroup
   * @param componentId
   * @param aRoleId
   * @param groupModel
   * @return all the Groups satifying the model that are direct childs of a specific group
   * @throws AdminPersistenceException
   */
  public String[] searchGroupsIds(boolean isRootGroup, int componentId, int[] aRoleId,
      GroupRow groupModel) throws AdminPersistenceException {
    boolean concatAndOr = false;
    String andOr = ") AND (";
    StringBuilder theQuery;
    List<Object> params = new ArrayList<>();

    if ((aRoleId != null) && (aRoleId.length > 0)) {
      theQuery = new StringBuilder(SELECT_SEARCH_GROUPSID_IN_ROLE);
      theQuery.append(" WHERE ((ST_Group.id = ST_UserRole_Group_Rel.groupId) AND ");
      if (aRoleId.length > 1) {
        theQuery.append("(");
      }
      for (int i = 0; i < aRoleId.length; i++) {
        params.add((aRoleId[i]));
        if (i > 0) {
          theQuery.append(" OR ");
        }
        theQuery.append("(ST_UserRole_Group_Rel.userRoleId = ?)");
      }
      if (aRoleId.length > 1) {
        theQuery.append(")");
      }
      concatAndOr = true;
    } else if (componentId >= 0) {
      theQuery = new StringBuilder(SELECT_SEARCH_GROUPSID_IN_COMPONENT);
      params.add(componentId);
      theQuery.append(" WHERE ((ST_UserRole.id = ST_UserRole_Group_Rel.userRoleId) AND (");
      theQuery
          .append("ST_Group.id = ST_UserRole_Group_Rel.groupId) AND (ST_UserRole.instanceId = ?)");
      concatAndOr = true;
    } else {
      theQuery = new StringBuilder(SELECT_SEARCH_GROUPSID);
    }

    if (isRootGroup) {
      if (concatAndOr) {
        theQuery.append(andOr);
      } else {
        theQuery.append(" WHERE (");
        concatAndOr = true;
      }
      theQuery.append("ST_Group.superGroupId IS NULL");
    } else {
      concatAndOr = addIdToQuery(params, theQuery, groupModel.superGroupId, "ST_Group.superGroupId",
          concatAndOr, andOr);
    }
    concatAndOr = addIdToQuery(params, theQuery, groupModel.id, "ST_Group.id", concatAndOr, andOr);
    concatAndOr =
        addIdToQuery(params, theQuery, groupModel.domainId, "ST_Group.domainId", concatAndOr,
            andOr);
    concatAndOr =
        addParamToQuery(params, theQuery, groupModel.name, "ST_Group.name", concatAndOr, andOr);
    concatAndOr = addParamToQuery(params, theQuery, groupModel.description, "ST_Group.description",
        concatAndOr, andOr);
    concatAndOr =
        addParamToQuery(params, theQuery, groupModel.specificId, "ST_Group.specificId", concatAndOr,
            andOr);
    if (concatAndOr) {
      theQuery.append(")");
    }
    theQuery.append(" ORDER BY UPPER(ST_Group.name)");

    List<String> groupIds = getIds(theQuery.toString(), params);
    return groupIds.toArray(new String[groupIds.size()]);
  }

  static final private String SELECT_SEARCH_GROUPSID =
      "select DISTINCT ST_Group.id, UPPER(ST_Group.name) from ST_Group";
  static final private String SELECT_SEARCH_GROUPSID_IN_COMPONENT =
      "select DISTINCT ST_Group.id, UPPER(ST_Group.name) " +
          "from ST_Group,ST_UserRole_Group_Rel,ST_UserRole";
  static final private String SELECT_SEARCH_GROUPSID_IN_ROLE =
      "select DISTINCT ST_Group.id, UPPER(ST_Group.name) " + "from ST_Group,ST_UserRole_Group_Rel";

  /**
   * Returns all the Groups satiffying the model
   * @param groupModel
   * @param isAnd
   * @return
   * @throws AdminPersistenceException
   */
  public GroupRow[] searchGroups(GroupRow groupModel, boolean isAnd)
      throws AdminPersistenceException {
    boolean concatAndOr = false;
    String andOr;
    StringBuilder theQuery = new StringBuilder(SELECT_SEARCH_GROUPS);
    List<Object> params = new ArrayList<>();

    if (isAnd) {
      andOr = ") AND (";
    } else {
      andOr = ") OR (";
    }
    concatAndOr = addIdToQuery(params, theQuery, groupModel.id, "id", concatAndOr, andOr);
    concatAndOr =
        addIdToQuery(params, theQuery, groupModel.domainId, "domainId", concatAndOr, andOr);
    concatAndOr =
        addIdToQuery(params, theQuery, groupModel.superGroupId, "superGroupId", concatAndOr, andOr);
    concatAndOr = addParamToQuery(params, theQuery, groupModel.name, "name", concatAndOr, andOr);
    concatAndOr =
        addParamToQuery(params, theQuery, groupModel.description, "description", concatAndOr,
            andOr);
    concatAndOr =
        addParamToQuery(params, theQuery, groupModel.specificId, "specificId", concatAndOr, andOr);
    if (concatAndOr) {
      theQuery.append(")");
    }
    theQuery.append(" order by UPPER(name)");

    List<GroupRow> groups = getRows(theQuery.toString(), params);
    return groups.toArray(new GroupRow[groups.size()]);
  }

  static final private String SELECT_SEARCH_GROUPS =
      "SELECT " + GROUP_COLUMNS + ", UPPER(name) FROM ST_Group";

  /**
   * Insert a new group row.
   * @param group
   * @throws AdminPersistenceException
   */
  public void createGroup(GroupRow group) throws AdminPersistenceException {
    GroupRow superGroup;
    if (group.superGroupId != -1) {
      superGroup = getGroup(group.superGroupId);
      if (superGroup == null) {
        throw new AdminPersistenceException("GroupTable.createGroup", SilverpeasException.ERROR,
            "admin.EX_ERR_GROUP_NOT_FOUND", "father group id: '" + group.superGroupId + "'");
      }
    }
    SynchroDomainReport
        .debug("GroupTable.createGroup()", "Ajout de " + group.name + ", requête : " + INSERT_GROUP);
    insertRow(INSERT_GROUP, group);

  }

  static final private String INSERT_GROUP =
      "INSERT INTO ST_Group(" + GROUP_COLUMNS + ")" + " VALUES (? ,? ,? ,? ,? ,? ,?)";

  @Override
  protected void prepareInsert(String insertQuery, PreparedStatement insert, GroupRow row)
      throws SQLException {
    if (row.id == -1) {
      row.id = getNextId();
    }
    insert.setInt(1, row.id);
    if (row.specificId == null) {
      row.specificId = String.valueOf(row.id);
    }
    insert.setString(2, truncate(row.specificId, 500));
    insert.setInt(3, row.domainId);
    if (row.superGroupId == -1) {
      insert.setNull(4, Types.INTEGER);
    } else {
      insert.setInt(4, row.superGroupId);
    }
    insert.setString(5, truncate(row.name, 100));
    insert.setString(6, truncate(row.description, 500));

    insert.setString(7, StringUtil.isDefined(row.rule) ? row.rule : null);
  }

  /**
   * Updates a group row.
   * @param group
   * @throws AdminPersistenceException
   */
  public void updateGroup(GroupRow group) throws AdminPersistenceException {
    SynchroDomainReport.debug("GroupTable.updateGroup()",
        "Maj de " + group.name + ", Id=" + group.id + ", requête : " + UPDATE_GROUP);
    updateRow(UPDATE_GROUP, group);
  }

  static final private String UPDATE_GROUP = "update ST_Group set domainId = ?, specificId = ?, " +
      "name = ?, description = ?, superGroupId = ?, synchroRule = ? where id = ?";

  @Override
  protected void prepareUpdate(String updateQuery, PreparedStatement update, GroupRow row)
      throws SQLException {
    update.setInt(1, row.domainId);
    if (row.specificId == null) {
      row.specificId = String.valueOf(row.id);
    }
    update.setString(2, truncate(row.specificId, 500));

    update.setString(3, truncate(row.name, 100));
    update.setString(4, truncate(row.description, 500));
    if (row.superGroupId != -1) {
      update.setInt(5, row.superGroupId);
    } else {
      update.setNull(5, Types.INTEGER);
    }
    update.setString(6, StringUtil.isDefined(row.rule) ? row.rule : null);
    update.setInt(7, row.id);
  }

  /**
   * Delete the group and all the sub-groups
   * @param id
   * @throws AdminPersistenceException
   */
  public void removeGroup(int id) throws AdminPersistenceException {
    GroupRow group = getGroup(id);
    if (group == null) {
      return;
    }

    SynchroDomainReport.info("GroupTable.removeGroup()",
        "Suppression du groupe " + group.name + " dans la base...");
    // remove the group from each role where it's used.
    UserRoleRow[] roles = organization.userRole.getDirectUserRolesOfGroup(id);
    SynchroDomainReport.info("GroupTable.removeGroup()",
        "Suppression de " + group.name + " des rôles dans la base");
    for (UserRoleRow role : roles) {
      organization.userRole.removeGroupFromUserRole(id, role.id);
    }

    // remove the group from each space role where it's used.
    SpaceUserRoleRow[] spaceRoles = organization.spaceUserRole.getDirectSpaceUserRolesOfGroup(id);
    SynchroDomainReport.info("GroupTable.removeGroup()",
        "Suppression de " + group.name + " comme manager d'espace dans la base");
    for (SpaceUserRoleRow spaceRole : spaceRoles) {
      organization.spaceUserRole.removeGroupFromSpaceUserRole(id, spaceRole.id);
    }
    // remove all subgroups.
    GroupRow[] subGroups = getDirectSubGroups(id);
    if (subGroups.length > 0) {
      SynchroDomainReport.info("GroupTable.removeGroup()",
          "Suppression des groupes fils de " + group.name + " dans la base");
    }
    for (GroupRow subGroup : subGroups) {
      removeGroup(subGroup.id);
    }
    // remove from the group any user.
    UserRow[] users = organization.user.getDirectUsersOfGroup(id);
    for (UserRow user : users) {
      removeUserFromGroup(user.id, id);
    }
    SynchroDomainReport.info("GroupTable.removeGroup()",
        "Suppression de " + users.length + " utilisateurs inclus directement dans le groupe " +
            group.name + " dans la base");

    // remove the empty group.
    // organization.userSet.removeUserSet("G", id);
    SynchroDomainReport.debug("GroupTable.removeGroup()",
        "Suppression de " + group.name + " (ID=" + id + "), requête : " + DELETE_GROUP);
    updateRelation(DELETE_GROUP, id);
  }

  static final private String DELETE_GROUP = "delete from ST_Group where id = ?";

  /**
   * Tests if a user is in given group (not recursive).
   */
  private boolean isUserDirectlyInGroup(int userId, int groupId) throws AdminPersistenceException {
    int[] ids = new int[]{userId, groupId};
    Integer result = getInteger(SELECT_COUNT_GROUP_USER_REL, ids);
    return result != null && result >= 1;
  }

  static final private String SELECT_COUNT_GROUP_USER_REL =
      "select count(*) from ST_Group_User_Rel" + " where userId = ? and groupId = ?";

  /**
   * Add an user in this group.
   * @param userId
   * @param groupId
   * @throws AdminPersistenceException
   */
  public void addUserInGroup(int userId, int groupId) throws AdminPersistenceException {
    if (isUserDirectlyInGroup(userId, groupId)) {
      return;
    }
    UserRow user = organization.user.getUser(userId);
    if (user == null) {
      throw new AdminPersistenceException("GroupTable.addUserInGroup", SilverpeasException.ERROR,
          "admin.EX_ERR_USER_NOT_FOUND", "user id: '" + userId + "'");
    }

    GroupRow group = getGroup(groupId);
    if (group == null) {
      throw new AdminPersistenceException("GroupTable.addUserInGroup", SilverpeasException.ERROR,
          "admin.EX_ERR_GROUP_NOT_FOUND", "group id: '" + groupId + "'");
    }

    int[] params = new int[]{groupId, userId};
    SynchroDomainReport.debug("GroupTable.addUserInGroup()",
        "Ajout de l'utilisateur d'ID " + userId + " dans le groupe d'ID " + groupId +
            ", requête : " + INSERT_A_GROUP_USER_REL);
    updateRelation(INSERT_A_GROUP_USER_REL, params);
    GroupCache.removeCacheOfUser(Integer.toString(userId));
  }

  /**
   * Add an user in this group.
   * @param userIds
   * @param groupId
   * @param checkRelation
   * @throws AdminPersistenceException
   */
  public void addUsersInGroup(String[] userIds, int groupId, boolean checkRelation)
      throws AdminPersistenceException {


    GroupRow group = getGroup(groupId);
    if (group == null) {
      throw new AdminPersistenceException("GroupTable.addUsersInGroup", SilverpeasException.ERROR,
          "admin.EX_ERR_GROUP_NOT_FOUND", "group id: '" + groupId + "'");
    }

    for (String userId1 : userIds) {
      int userId = Integer.parseInt(userId1);

      boolean userInGroup = false;
      if (checkRelation) {
        userInGroup = isUserDirectlyInGroup(userId, groupId);
      }

      if (!userInGroup) {
        UserRow user = organization.user.getUser(userId);
        if (user == null) {
          throw new AdminPersistenceException("GroupTable.addUsersInGroup",
              SilverpeasException.ERROR, "admin.EX_ERR_USER_NOT_FOUND",
              "user id: '" + userId + "'");
        }

        int[] params = new int[]{groupId, userId};
        SynchroGroupReport.debug("GroupTable.addUsersInGroup()",
            "Ajout de l'utilisateur d'ID " + userId + " dans le groupe d'ID " + groupId +
                ", requête : " + INSERT_A_GROUP_USER_REL);
        updateRelation(INSERT_A_GROUP_USER_REL, params);
        GroupCache.removeCacheOfUser(Integer.toString(userId));
      }
    }
  }

  static final private String INSERT_A_GROUP_USER_REL =
      "insert into ST_Group_User_Rel(groupId, userId) values(?,?)";

  /**
   * Removes an user from this group.
   * @param userId
   * @param groupId
   * @throws AdminPersistenceException
   */
  public void removeUserFromGroup(int userId, int groupId) throws AdminPersistenceException {
    if (!isUserDirectlyInGroup(userId, groupId)) {
      throw new AdminPersistenceException("GroupTable.removeUserFromGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_USER_NOT_IN_GROUP",
          "group id: '" + groupId + "', user id: '" + userId + "'");
    }
    int[] params = new int[]{groupId, userId};
    SynchroDomainReport.debug("GroupTable.removeUserFromGroup()",
        "Retrait de l'utilisateur d'ID " + userId + " du groupe d'ID " + groupId + ", requête : " +
            DELETE_GROUP_USER_REL);
    updateRelation(DELETE_GROUP_USER_REL, params);
    GroupCache.removeCacheOfUser(Integer.toString(userId));
  }

  static final private String DELETE_GROUP_USER_REL =
      "delete from ST_Group_User_Rel where groupId = ? and userId = ?";

  /**
   * Add an user in this group.
   * @param userIds
   * @param groupId
   * @param checkRelation
   * @throws AdminPersistenceException
   */
  public void removeUsersFromGroup(String[] userIds, int groupId, boolean checkRelation)
      throws AdminPersistenceException {

    GroupRow group = getGroup(groupId);
    if (group == null) {
      throw new AdminPersistenceException("GroupTable.removeUsersFromGroup()",
          SilverpeasException.ERROR, "admin.EX_ERR_GROUP_NOT_FOUND", "group id: '" + groupId + "'");
    }

    for (String userId1 : userIds) {
      int userId = Integer.parseInt(userId1);

      boolean userInGroup = true;
      if (checkRelation) {
        userInGroup = isUserDirectlyInGroup(userId, groupId);
      }

      if (userInGroup) {
        int[] params = new int[]{groupId, userId};
        SynchroGroupReport.debug("GroupTable.removeUsersFromGroup()",
            "Retrait de l'utilisateur d'ID " + userId + " du groupe d'ID " + groupId +
                ", requête : " + DELETE_GROUP_USER_REL);
        updateRelation(DELETE_GROUP_USER_REL, params);
        GroupCache.removeCacheOfUser(Integer.toString(userId));
      } else {
        throw new AdminPersistenceException("GroupTable.removeUsersFromGroup()",
            SilverpeasException.ERROR, "admin.EX_ERR_USER_NOT_IN_GROUP",
            "group id: '" + groupId + "', user id: '" + userId + "'");
      }
    }
  }

  /**
   * Fetch the current group row from a resultSet.
   * @param rs
   * @return
   * @throws SQLException
   */
  @Override
  protected GroupRow fetchRow(ResultSet rs) throws SQLException {
    return fetchGroup(rs);
  }

  private OrganizationSchema organization = null;
}