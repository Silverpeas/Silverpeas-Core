/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
package com.stratelia.webactiv.organization;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.SynchroGroupReport;
import com.stratelia.webactiv.beans.admin.SynchroReport;
import com.stratelia.webactiv.beans.admin.cache.GroupCache;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * A GroupTable object manages the ST_Group table.
 */
public class GroupTable extends Table<GroupRow> {

  public GroupTable(OrganizationSchema schema) {
    super(schema, "ST_Group");
    this.organization = schema;
  }
  static final private String GROUP_COLUMNS = "id, specificId, domainId, superGroupId, name, "
          + "description, synchroRule";
  static final private String SELECT_GROUP_BY_ID = "SELECT id, specificId, domainId, superGroupId, "
          + "name, description, synchroRule FROM ST_Group WHERE id = ?";
  static final private String SELECT_GROUP_BY_SPECIFICID = "SELECT id, specificId, domainId, "
          + "superGroupId, name, description, synchroRule FROM ST_Group WHERE "
          + "domainId = ? AND specificId = ?";
  static final private String SELECT_ROOT_GROUP_BY_NAME = "SELECT id, specificId, domainId, "
          + "superGroupId, name, description, synchroRule FROM ST_Group WHERE "
          + "superGroupId IS NULL AND name = ?";
  static final private String SELECT_GROUP_BY_NAME = "SELECT id, specificId, domainId, "
          + "superGroupId, name, description, synchroRule FROM ST_Group WHERE "
          + "superGroupId = ? AND name = ?";

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
  public GroupRow getGroupBySpecificId(int domainId, String specificId) throws
          AdminPersistenceException {
    List<GroupRow> rows = getRows(SELECT_GROUP_BY_SPECIFICID, new int[]{domainId}, new String[]{
              specificId});
    GroupRow[] groups = rows.toArray(new GroupRow[rows.size()]);
    if (groups.length == 0) {
      return null;
    }
    if (groups.length == 1) {
      return groups[0];
    }
    throw new AdminPersistenceException("GroupTable.getGroupBySpecificId", SilverpeasException.ERROR,
            "admin.EX_ERR_GROUP_SPECIFIC_ID_FOUND_TWICE",
            "domain Id: '" + domainId + "', specific Id: '" + specificId + "'");

  }

  /**
   * Returns the root Group whith the given name.
   * @param name
   * @return the root Group whith the given name.
   * @throws AdminPersistenceException 
   */
  public GroupRow getRootGroup(String name) throws AdminPersistenceException {
    List<GroupRow> rows = getRows(SELECT_ROOT_GROUP_BY_NAME, new String[]{name});
    GroupRow[] groups = rows.toArray(new GroupRow[rows.size()]);

    if (groups.length == 0) {
      return null;
    }
    if (groups.length == 1) {
      return groups[0];
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
    List<GroupRow> rows = getRows(SELECT_GROUP_BY_NAME, new int[]{superGroupId}, new String[]{
              name});
    GroupRow[] groups = rows.toArray(new GroupRow[rows.size()]);
    if (groups.length == 0) {
      return null;
    }
    if (groups.length == 1) {
      return groups[0];
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
  static final private String SELECT_ALL_GROUPS = "select " + GROUP_COLUMNS
          + " from ST_Group";

  /**
   * Returns all the Groups.
   */
  public GroupRow[] getSynchronizedGroups() throws AdminPersistenceException {
    List<GroupRow> rows = getRows(SELECT_SYNCHRONIZED_GROUPS);
    return rows.toArray(new GroupRow[rows.size()]);
  }
  static final private String SELECT_SYNCHRONIZED_GROUPS = "select " + GROUP_COLUMNS
          + " from ST_Group where synchroRule is not null and synchroRule <> ''";

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
  static final private String SELECT_ALL_ROOT_GROUPS = "select "
          + GROUP_COLUMNS + " from ST_Group where superGroupId is null";

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
          "select id from ST_Group where superGroupId is null";

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
  static final private String SELECT_SUBGROUPS = "select " + GROUP_COLUMNS
          + " from ST_Group where superGroupId = ?";

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
  static final private String SELECT_SUBGROUP_IDS = "select id from ST_Group where superGroupId = ?";

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
  static final private String SELECT_ALL_ROOT_GROUPS_IN_DOMAIN = "select " + GROUP_COLUMNS
          + " from ST_Group where (domainId=?) AND (superGroupId is null)";
  static final private String SELECT_ALL_ROOT_GROUPS_IDS_IN_DOMAIN =
          "select id from ST_Group where (domainId=?) AND (superGroupId is null)";

  /**
   * Returns all the Groups having a given domain id.
   * @param domainId
   * @return all the Groups having a given domain id.
   * @throws AdminPersistenceException 
   */
  public GroupRow[] getAllGroupsOfDomain(int domainId) throws AdminPersistenceException {
    SynchroReport.debug("GroupTable.getAllGroupsOfDomain()",
            "Recherche de l'ensemble des groupes du domaine LDAP dans la base (ID "
            + domainId + "), requête : " + SELECT_ALL_GROUPS_IN_DOMAIN, null);
    List<GroupRow> rows = getRows(SELECT_ALL_GROUPS_IN_DOMAIN, domainId);
    return rows.toArray(new GroupRow[rows.size()]);
  }
  static final private String SELECT_ALL_GROUPS_IN_DOMAIN = "select " + GROUP_COLUMNS
          + " from ST_Group where domainId=?";

  /**
   * Returns the superGroup of a given subGroup.
   * @param subGroupId
   * @return the superGroup of a given subGroup.
   * @throws AdminPersistenceException 
   */
  public GroupRow getSuperGroup(int subGroupId) throws AdminPersistenceException {
    return getUniqueRow(SELECT_SUPERGROUP, subGroupId);
  }
  static final private String SELECT_SUPERGROUP = "select " + aliasColumns("sg", GROUP_COLUMNS)
          + " from ST_Group sg, ST_GROUP g where sg.id=g.superGroupId and g.id=?";

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
  static final private String SELECT_USER_GROUPS = "select " + GROUP_COLUMNS
          + " from ST_Group,ST_Group_User_Rel where id = groupId and userId = ?";

  /**
   * Returns all the groups in a given userRole (not recursive).
   * @param userRoleId
   * @return all the groups in a given userRole (not recursive).
   * @throws AdminPersistenceException 
   */
  public GroupRow[] getDirectGroupsInUserRole(int userRoleId) throws AdminPersistenceException {
    List<GroupRow> rows = getRows(SELECT_USERROLE_GROUPS, userRoleId);
    return rows.toArray(new GroupRow[rows.size()]);
  }
  static final private String SELECT_USERROLE_GROUPS = "select " + GROUP_COLUMNS
          + " from ST_Group,ST_UserRole_Group_Rel where id = groupId and userRoleId = ?";

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
   * Returns all the groups in a given spaceUserRole (not recursive).
   * @param spaceUserRoleId
   * @return all the groups in a given spaceUserRole (not recursive).
   * @throws AdminPersistenceException 
   */
  public GroupRow[] getDirectGroupsInSpaceUserRole(int spaceUserRoleId) throws
          AdminPersistenceException {
    List<GroupRow> rows = getRows(SELECT_SPACEUSERROLE_GROUPS, spaceUserRoleId);
    return rows.toArray(new GroupRow[rows.size()]);
  }
  static final private String SELECT_SPACEUSERROLE_GROUPS = "SELECT " + GROUP_COLUMNS
          + " FROM ST_Group,ST_SpaceUserRole_Group_Rel WHERE id = groupId AND spaceUserRoleId = ?";

  /**
   * Returns all the group ids in a given spaceUserRole (not recursive).
   * @param spaceUserRoleId
   * @return all the group ids in a given spaceUserRole (not recursive).
   * @throws AdminPersistenceException 
   */
  public String[] getDirectGroupIdsInSpaceUserRole(int spaceUserRoleId) throws
          AdminPersistenceException {
    List<String> ids = getIds(SELECT_SPACEUSERROLE_GROUP_IDS, spaceUserRoleId);
    return ids.toArray(new String[ids.size()]);
  }
  static final private String SELECT_SPACEUSERROLE_GROUP_IDS = "SELECT id FROM st_group, "
          + "st_spaceuserrole_group_rel WHERE id = groupId AND spaceUserRoleId = ?";

  /**
   * Returns all the groups in a given groupUserRole (not recursive).
   * @param groupUserRoleId
   * @return all the groups in a given groupUserRole (not recursive).
   * @throws AdminPersistenceException 
   */
  public GroupRow[] getDirectGroupsInGroupUserRole(int groupUserRoleId) throws
          AdminPersistenceException {
    List<GroupRow> rows = getRows(SELECT_GROUPUSERROLE_GROUPS, groupUserRoleId);
    return rows.toArray(new GroupRow[rows.size()]);
  }
  static final private String SELECT_GROUPUSERROLE_GROUPS = "SELECT " + GROUP_COLUMNS
          + " FROM ST_Group, ST_GroupUserRole_Group_Rel WHERE id = groupId AND groupUserRoleId = ?";

  /**
   * Returns the Group of a given group user role.
   * @param groupUserRoleId
   * @return the Group of a given group user role.
   * @throws AdminPersistenceException 
   */
  public GroupRow getGroupOfGroupUserRole(int groupUserRoleId) throws AdminPersistenceException {
    return getUniqueRow(SELECT_GROUPUSERROLE_GROUP, groupUserRoleId);
  }
  static final private String SELECT_GROUPUSERROLE_GROUP = "SELECT "
          + aliasColumns("i", GROUP_COLUMNS) + " FROM ST_Group i, ST_GroupUserRole us"
          + " WHERE i.id = us.groupId AND us.id = ?";

  /**
   * Returns all the group ids in a given groupUserRole (not recursive).
   * @param groupUserRoleId
   * @return all the group ids in a given groupUserRole (not recursive).
   * @throws AdminPersistenceException 
   */
  public String[] getDirectGroupIdsInGroupUserRole(int groupUserRoleId) throws
          AdminPersistenceException {
    List<String> ids = getIds(SELECT_GROUPUSERROLE_GROUP_IDS, groupUserRoleId);
    return ids.toArray(new String[ids.size()]);
  }
  static final private String SELECT_GROUPUSERROLE_GROUP_IDS = "SELECT id FROM ST_Group, "
          + "ST_GroupUserRole_Group_Rel WHERE id = groupId AND groupUserRoleId = ?";

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
    StringBuffer theQuery;
    List<Integer> ids = new ArrayList<Integer>();
    List<String> params = new ArrayList<String>();

    if ((aRoleId != null) && (aRoleId.length > 0)) {
      theQuery = new StringBuffer(SELECT_SEARCH_GROUPSID_IN_ROLE);
      theQuery.append(" WHERE ((ST_Group.id = ST_UserRole_Group_Rel.groupId) AND ");
      if (aRoleId.length > 1) {
        theQuery.append("(");
      }
      for (int i = 0; i < aRoleId.length; i++) {
        ids.add((aRoleId[i]));
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
      theQuery = new StringBuffer(SELECT_SEARCH_GROUPSID_IN_COMPONENT);
      ids.add(componentId);
      theQuery.append(" WHERE ((ST_UserRole.id = ST_UserRole_Group_Rel.userRoleId) AND (");
      theQuery.append(
              "ST_Group.id = ST_UserRole_Group_Rel.groupId) AND (ST_UserRole.instanceId = ?)");
      concatAndOr = true;
    } else {
      theQuery = new StringBuffer(SELECT_SEARCH_GROUPSID);
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
      concatAndOr = addIdToQuery(ids, theQuery, groupModel.superGroupId,
              "ST_Group.superGroupId", concatAndOr, andOr);
    }
    concatAndOr = addIdToQuery(ids, theQuery, groupModel.id, "ST_Group.id", concatAndOr, andOr);
    concatAndOr = addIdToQuery(ids, theQuery, groupModel.domainId, "ST_Group.domainId", concatAndOr,
            andOr);
    concatAndOr = addParamToQuery(params, theQuery, groupModel.name, "ST_Group.name", concatAndOr,
            andOr);
    concatAndOr = addParamToQuery(params, theQuery, groupModel.description, "ST_Group.description",
            concatAndOr, andOr);
    concatAndOr = addParamToQuery(params, theQuery, groupModel.specificId, "ST_Group.specificId",
            concatAndOr, andOr);
    if (concatAndOr) {
      theQuery.append(")");
    }
    theQuery.append(" ORDER BY UPPER(ST_Group.name)");

    int[] idsArray = new int[ids.size()];
    for (int i = 0; i < ids.size(); i++) {
      idsArray[i] = ids.get(i);
    }

    return getIds(theQuery.toString(), idsArray, params.toArray(new String[params.size()])).toArray(
        new String[getIds(theQuery.toString(), idsArray, params.toArray(new String[params.size()])).size()]);
  }
  static final private String SELECT_SEARCH_GROUPSID =
          "select DISTINCT ST_Group.id, UPPER(ST_Group.name) from ST_Group";
  static final private String SELECT_SEARCH_GROUPSID_IN_COMPONENT =
          "select DISTINCT ST_Group.id, UPPER(ST_Group.name) "
          + "from ST_Group,ST_UserRole_Group_Rel,ST_UserRole";
  static final private String SELECT_SEARCH_GROUPSID_IN_ROLE =
          "select DISTINCT ST_Group.id, UPPER(ST_Group.name) "
          + "from ST_Group,ST_UserRole_Group_Rel";

  /**
   * Returns all the Groups satiffying the model
   * @param groupModel
   * @param isAnd
   * @return
   * @throws AdminPersistenceException 
   */
  public GroupRow[] searchGroups(GroupRow groupModel, boolean isAnd) throws
          AdminPersistenceException {
    boolean concatAndOr = false;
    String andOr;
    StringBuffer theQuery = new StringBuffer(SELECT_SEARCH_GROUPS);
    List<Integer> ids = new ArrayList<Integer>();
    List<String> params = new ArrayList<String>();

    if (isAnd) {
      andOr = ") AND (";
    } else {
      andOr = ") OR (";
    }
    concatAndOr = addIdToQuery(ids, theQuery, groupModel.id, "id", concatAndOr,
            andOr);
    concatAndOr = addIdToQuery(ids, theQuery, groupModel.domainId, "domainId",
            concatAndOr, andOr);
    concatAndOr = addIdToQuery(ids, theQuery, groupModel.superGroupId,
            "superGroupId", concatAndOr, andOr);
    concatAndOr = addParamToQuery(params, theQuery, groupModel.name, "name",
            concatAndOr, andOr);
    concatAndOr = addParamToQuery(params, theQuery, groupModel.description,
            "description", concatAndOr, andOr);
    concatAndOr = addParamToQuery(params, theQuery, groupModel.specificId,
            "specificId", concatAndOr, andOr);
    if (concatAndOr) {
      theQuery.append(")");
    }
    theQuery.append(" order by UPPER(name)");

    int[] idsArray = new int[ids.size()];
    for (int i = 0; i < ids.size(); i++) {
      idsArray[i] = ids.get(i);
    }

    return getRows(theQuery.toString(), idsArray, params.toArray(new String[params.size()])).toArray(
        new GroupRow[getRows(theQuery.toString(), idsArray,
            params.toArray(new String[params.size()])).size()]);
  }
  static final private String SELECT_SEARCH_GROUPS = "SELECT " + GROUP_COLUMNS
          + ", UPPER(name) FROM ST_Group";

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
    SynchroReport.debug("GroupTable.createGroup()", "Ajout de " + group.name + ", requête : "
            + INSERT_GROUP, null);
    insertRow(INSERT_GROUP, group);

    CallBackManager callBackManager = CallBackManager.get();
    callBackManager.invoke(CallBackManager.ACTION_AFTER_CREATE_GROUP, group.id, null, null);
  }
  static final private String INSERT_GROUP = "INSERT INTO ST_Group("
          + GROUP_COLUMNS + ")" + " VALUES  (? ,? ,? ,? ,? ,? ,?)";

  @Override
  protected void prepareInsert(String insertQuery, PreparedStatement insert, GroupRow row) throws
          SQLException {
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

    insert.setString(7, row.rule);
  }

  /**
   * Updates a group row.
   * @param group
   * @throws AdminPersistenceException 
   */
  public void updateGroup(GroupRow group) throws AdminPersistenceException {
    SynchroReport.debug("GroupTable.updateGroup()", "Maj de " + group.name
            + ", Id=" + group.id + ", requête : " + UPDATE_GROUP, null);
    updateRow(UPDATE_GROUP, group);
  }
  static final private String UPDATE_GROUP = "update ST_Group set domainId = ?, specificId = ?, "
          + "name = ?, description = ?, superGroupId = ?, synchroRule = ? where id = ?";

  @Override
  protected void prepareUpdate(String updateQuery, PreparedStatement update, GroupRow row) throws
          SQLException {
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
    update.setString(6, row.rule);
    update.setInt(7, row.id);
  }

  /**
   * Delete the group and all the sub-groups
   * @param id
   * @throws AdminPersistenceException 
   */
  public void removeGroup(int id) throws AdminPersistenceException {
    CallBackManager callBackManager = CallBackManager.get();
    callBackManager.invoke(CallBackManager.ACTION_BEFORE_REMOVE_GROUP, id, null, null);
    GroupRow group = getGroup(id);
    if (group == null) {
      return;
    }

    SynchroReport.info("GroupTable.removeGroup()", "Suppression du groupe " + group.name
            + " dans la base...", null);
    // remove the group from each role where it's used.
    UserRoleRow[] roles = organization.userRole.getDirectUserRolesOfGroup(id);
    SynchroReport.info("GroupTable.removeGroup()", "Suppression de "
            + group.name + " des rôles dans la base", null);
    for (UserRoleRow role : roles) {
      organization.userRole.removeGroupFromUserRole(id, role.id);
    }

    // remove the group from each space role where it's used.
    SpaceUserRoleRow[] spaceRoles = organization.spaceUserRole.getDirectSpaceUserRolesOfGroup(id);
    SynchroReport.info("GroupTable.removeGroup()", "Suppression de " + group.name
            + " comme manager d'espace dans la base", null);
    for (SpaceUserRoleRow spaceRole : spaceRoles) {
      organization.spaceUserRole.removeGroupFromSpaceUserRole(id, spaceRole.id);
    }
    // remove all subgroups.
    GroupRow[] subGroups = getDirectSubGroups(id);
    if (subGroups.length > 0) {
      SynchroReport.info("GroupTable.removeGroup()", "Suppression des groupes fils de "
              + group.name + " dans la base", null);
    }
    for (GroupRow subGroup : subGroups) {
      removeGroup(subGroup.id);
    }
    // remove from the group any user.
    UserRow[] users = organization.user.getDirectUsersOfGroup(id);
    for (UserRow user : users) {
      removeUserFromGroup(user.id, id);
    }
    SynchroReport.info("GroupTable.removeGroup()", "Suppression de " + users.length
            + " utilisateurs inclus directement dans le groupe " + group.name + " dans la base",
            null);

    // remove the empty group.
    // organization.userSet.removeUserSet("G", id);
    SynchroReport.debug("GroupTable.removeGroup()", "Suppression de " + group.name + " (ID="
            + id + "), requête : " + DELETE_GROUP, null);
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
  static final private String SELECT_COUNT_GROUP_USER_REL = "select count(*) from ST_Group_User_Rel"
          + " where userId = ? and groupId = ?";

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
    SynchroReport.debug("GroupTable.addUserInGroup()",
            "Ajout de l'utilisateur d'ID " + userId + " dans le groupe d'ID " + groupId 
            + ", requête : " + INSERT_A_GROUP_USER_REL, null);
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
  public void addUsersInGroup(String[] userIds, int groupId, boolean checkRelation) throws
          AdminPersistenceException {
    SilverTrace.info("admin", "GroupTable.addUsersInGroup", "root.MSG_GEN_ENTER_METHOD",
            "groupId = " + groupId + ", userIds = " + Arrays.toString(userIds));

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
        SynchroGroupReport.debug("GroupTable.addUsersInGroup()", "Ajout de l'utilisateur d'ID "
            + userId + " dans le groupe d'ID " + groupId + ", requête : "
            + INSERT_A_GROUP_USER_REL, null);
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
    SynchroReport.debug("GroupTable.removeUserFromGroup()",
            "Retrait de l'utilisateur d'ID " + userId + " du groupe d'ID "
            + groupId + ", requête : " + DELETE_GROUP_USER_REL, null);
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
  public void removeUsersFromGroup(String[] userIds, int groupId, boolean checkRelation) throws
          AdminPersistenceException {
    SilverTrace.info("admin", "GroupTable.removeUsersFromGroup", "root.MSG_GEN_ENTER_METHOD", 
            "groupId = " + groupId + ", userIds = " + Arrays.toString(userIds));
    GroupRow group = getGroup(groupId);
    if (group == null) {
      throw new AdminPersistenceException("GroupTable.removeUsersFromGroup()",
              SilverpeasException.ERROR, "admin.EX_ERR_GROUP_NOT_FOUND",
              "group id: '" + groupId + "'");
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
            "Retrait de l'utilisateur d'ID " + userId + " du groupe d'ID " + groupId
                + ", requête : " + DELETE_GROUP_USER_REL, null);
        updateRelation(DELETE_GROUP_USER_REL, params);
        GroupCache.removeCacheOfUser(Integer.toString(userId));
      } else {
        throw new AdminPersistenceException("GroupTable.removeUsersFromGroup()",
            SilverpeasException.ERROR, "admin.EX_ERR_USER_NOT_IN_GROUP", "group id: '" + groupId
            + "', user id: '" + userId + "'");
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