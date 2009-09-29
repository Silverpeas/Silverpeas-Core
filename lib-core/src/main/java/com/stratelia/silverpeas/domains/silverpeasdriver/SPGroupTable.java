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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.stratelia.silverpeas.domains.silverpeasdriver;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.stratelia.webactiv.organization.AdminPersistenceException;
import com.stratelia.webactiv.organization.Table;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * A GroupTable object manages the DomainSP_Group table.
 */
public class SPGroupTable extends Table {
  public SPGroupTable(DomainSPSchema schema) {
    super(schema, "DomainSP_Group");
    this.organization = schema;
  }

  static final private String GROUP_COLUMNS = "id,superGroupId,name,description";

  /**
   * Fetch the current group row from a resultSet.
   */
  protected SPGroupRow fetchGroup(ResultSet rs) throws SQLException {
    SPGroupRow g = new SPGroupRow();

    g.id = rs.getInt(1);

    g.superGroupId = rs.getInt(2);
    if (rs.wasNull())
      g.superGroupId = -1;

    g.name = rs.getString(3);
    g.description = rs.getString(4);

    return g;
  }

  /**
   * Returns the Group whith the given id.
   */
  public SPGroupRow getGroup(int id) throws AdminPersistenceException {
    return (SPGroupRow) getUniqueRow(SELECT_GROUP_BY_ID, id);
  }

  static final private String SELECT_GROUP_BY_ID = "select " + GROUP_COLUMNS
      + " from DomainSP_Group where id = ?";

  /**
   * Returns the root Group whith the given name.
   */
  public SPGroupRow getRootGroup(String name) throws AdminPersistenceException {
    SPGroupRow[] groups = (SPGroupRow[]) getRows(SELECT_ROOT_GROUP_BY_NAME,
        new String[] { name }).toArray(new SPGroupRow[0]);

    if (groups.length == 0)
      return null;
    else if (groups.length == 1)
      return groups[0];
    else {
      throw new AdminPersistenceException("SPGroupTable.getRootGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_GROUP_NAME_FOUND_TWICE",
          "name : '" + name + "'");
    }
  }

  static final private String SELECT_ROOT_GROUP_BY_NAME = "select "
      + GROUP_COLUMNS + " from DomainSP_Group"
      + " where superGroupId is null and name = ?";

  /**
   * Returns the Group whith the given name in the given super group.
   */
  public SPGroupRow getGroup(int superGroupId, String name)
      throws AdminPersistenceException {
    SPGroupRow[] groups = (SPGroupRow[]) getRows(SELECT_GROUP_BY_NAME,
        new int[] { superGroupId }, new String[] { name }).toArray(
        new SPGroupRow[0]);

    if (groups.length == 0)
      return null;
    else if (groups.length == 1)
      return groups[0];
    else {
      throw new AdminPersistenceException("SPGroupTable.getGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_GROUP_NAME_FOUND_TWICE",
          "name: '" + name + "', father group Id: '" + superGroupId + "'");
    }
  }

  static final private String SELECT_GROUP_BY_NAME = "select " + GROUP_COLUMNS
      + " from DomainSP_Group" + " where superGroupId = ? and name = ?";

  /**
   * Returns all the Groups.
   */
  public SPGroupRow[] getAllGroups() throws AdminPersistenceException {
    return (SPGroupRow[]) getRows(SELECT_ALL_GROUPS).toArray(new SPGroupRow[0]);
  }

  static final private String SELECT_ALL_GROUPS = "select " + GROUP_COLUMNS
      + " from DomainSP_Group";

  /**
   * Returns all the Groups without a superGroup.
   */
  public SPGroupRow[] getAllRootGroups() throws AdminPersistenceException {
    return (SPGroupRow[]) getRows(SELECT_ALL_ROOT_GROUPS).toArray(
        new SPGroupRow[0]);
  }

  static final private String SELECT_ALL_ROOT_GROUPS = "select "
      + GROUP_COLUMNS + " from DomainSP_Group where superGroupId is null";

  /**
   * Returns all the Groups having a given superGroup.
   */
  public SPGroupRow[] getDirectSubGroups(int superGroupId)
      throws AdminPersistenceException {
    return (SPGroupRow[]) getRows(SELECT_SUBGROUPS, superGroupId).toArray(
        new SPGroupRow[0]);
  }

  static final private String SELECT_SUBGROUPS = "select " + GROUP_COLUMNS
      + " from DomainSP_Group where superGroupId = ?";

  /**
   * Returns the superGroup of a given subGroup.
   */
  public SPGroupRow getSuperGroup(int subGroupId)
      throws AdminPersistenceException {
    return (SPGroupRow) getUniqueRow(SELECT_SUPERGROUP, subGroupId);
  }

  static final private String SELECT_SUPERGROUP = "select "
      + aliasColumns("sg", GROUP_COLUMNS)
      + " from DomainSP_Group sg, DomainSP_Group g"
      + " where sg.id=g.superGroupId and g.id=?";

  /**
   * Returns all the groups of a given user (not recursive).
   */
  public SPGroupRow[] getDirectGroupsOfUser(int userId)
      throws AdminPersistenceException {
    return (SPGroupRow[]) getRows(SELECT_USER_GROUPS, userId).toArray(
        new SPGroupRow[0]);
  }

  static final private String SELECT_USER_GROUPS = "select " + GROUP_COLUMNS
      + " from DomainSP_Group,DomainSP_Group_User_Rel"
      + " where id = groupId and userId = ?";

  /**
   * Returns all the groups in a given userRole (not recursive).
   */
  public SPGroupRow[] getDirectGroupsInUserRole(int userRoleId)
      throws AdminPersistenceException {
    return (SPGroupRow[]) getRows(SELECT_USERROLE_GROUPS, userRoleId).toArray(
        new SPGroupRow[0]);
  }

  static final private String SELECT_USERROLE_GROUPS = "select "
      + GROUP_COLUMNS + " from DomainSP_Group,ST_UserRole_Group_Rel"
      + " where id = groupId and userRoleId = ?";

  /**
   * Returns all the groups in a given userRole (not recursive).
   */
  public String[] getDirectGroupIdsInUserRole(int userRoleId)
      throws AdminPersistenceException {
    return (String[]) getIds(SELECT_USERROLE_GROUP_IDS, userRoleId).toArray(
        new String[0]);
  }

  static final private String SELECT_USERROLE_GROUP_IDS = "select id from DomainSP_Group,ST_UserRole_Group_Rel"
      + " where id = groupId and userRoleId = ?";

  /**
   * Returns all the groups in a given spaceUserRole (not recursive).
   */
  public SPGroupRow[] getDirectGroupsInSpaceUserRole(int spaceUserRoleId)
      throws AdminPersistenceException {
    return (SPGroupRow[]) getRows(SELECT_SPACEUSERROLE_GROUPS, spaceUserRoleId)
        .toArray(new SPGroupRow[0]);
  }

  static final private String SELECT_SPACEUSERROLE_GROUPS = "select "
      + GROUP_COLUMNS + " from DomainSP_Group,ST_SpaceUserRole_Group_Rel"
      + " where id = groupId and spaceUserRoleId = ?";

  /**
   * Returns all the group ids in a given spaceUserRole (not recursive).
   */
  public String[] getDirectGroupIdsInSpaceUserRole(int spaceUserRoleId)
      throws AdminPersistenceException {
    return (String[]) getIds(SELECT_SPACEUSERROLE_GROUP_IDS, spaceUserRoleId)
        .toArray(new String[0]);
  }

  static final private String SELECT_SPACEUSERROLE_GROUP_IDS = "select id from DomainSP_Group,ST_SpaceUserRole_Group_Rel"
      + " where id = groupId and spaceUserRoleId = ?";

  /**
   * Returns the Group whose fields match those of the given sample group
   * fields.
   */
  public SPGroupRow[] getAllMatchingGroups(SPGroupRow sampleGroup)
      throws AdminPersistenceException {
    String[] columns = new String[] { "name", "description" };
    String[] values = new String[] { sampleGroup.name, sampleGroup.description };

    return (SPGroupRow[]) getMatchingRows(GROUP_COLUMNS, columns, values)
        .toArray(new SPGroupRow[0]);
  }

  /**
   * Insert a new group row.
   */
  public void createGroup(SPGroupRow group) throws AdminPersistenceException {
    SPGroupRow superGroup = null;

    if (group.superGroupId != -1) {
      superGroup = getGroup(group.superGroupId);
      if (superGroup == null) {
        throw new AdminPersistenceException("SPGroupTable.createGroup",
            SilverpeasException.ERROR, "admin.EX_ERR_UNKNOWN_FATHER",
            "father group Id: '" + group.superGroupId + "'");
      }
    }

    insertRow(INSERT_GROUP, group);
  }

  static final private String INSERT_GROUP = "insert into"
      + " DomainSP_Group(id,superGroupId,name,description)"
      + " values  (? ,? ,? ,?)";

  protected void prepareInsert(String insertQuery, PreparedStatement insert,
      Object row) throws SQLException {
    SPGroupRow g = (SPGroupRow) row;
    if (g.id == -1) {
      g.id = getNextId();
    }

    insert.setInt(1, g.id);
    if (g.superGroupId == -1)
      insert.setNull(2, Types.INTEGER);
    else
      insert.setInt(2, g.superGroupId);
    insert.setString(3, truncate(g.name, 100));
    insert.setString(4, truncate(g.description, 500));
  }

  /**
   * Updates a group row.
   */
  public void updateGroup(SPGroupRow group) throws AdminPersistenceException {
    updateRow(UPDATE_GROUP, group);
  }

  static final private String UPDATE_GROUP = "update DomainSP_Group set"
      + " name = ?," + " description = ?" + " where id = ?";

  protected void prepareUpdate(String updateQuery, PreparedStatement update,
      Object row) throws SQLException {
    SPGroupRow g = (SPGroupRow) row;

    update.setString(1, truncate(g.name, 100));
    update.setString(2, truncate(g.description, 500));
    update.setInt(3, g.id);
  }

  /**
   * Delete the group and all the sub-groups
   */
  public void removeGroup(int id) throws AdminPersistenceException {
    SPGroupRow group = getGroup(id);
    if (group == null)
      return;

    // remove all subgroups.
    SPGroupRow[] subGroups = getDirectSubGroups(id);
    for (int i = 0; i < subGroups.length; i++) {
      removeGroup(subGroups[i].id);
    }

    // remove from the group any user.
    SPUserRow[] users = organization.user.getDirectUsersOfGroup(id);
    for (int i = 0; i < users.length; i++) {
      removeUserFromGroup(users[i].id, id);
    }

    // remove the empty group.
    updateRelation(DELETE_GROUP, id);
  }

  static final private String DELETE_GROUP = "delete from DomainSP_Group where id = ?";

  /**
   * Tests if a user is in given group (not recursive).
   */
  public boolean isUserDirectlyInGroup(int userId, int groupId)
      throws AdminPersistenceException {
    int[] ids = new int[] { userId, groupId };
    Integer result = getInteger(SELECT_COUNT_GROUP_USER_REL, ids);

    if (result == null)
      return false;
    else
      return result.intValue() >= 1;
  }

  static final private String SELECT_COUNT_GROUP_USER_REL = "select count(*) from DomainSP_Group_User_Rel"
      + " where userId = ? and groupId = ?";

  /**
   * Add an user in this group.
   */
  public void addUserInGroup(int userId, int groupId)
      throws AdminPersistenceException {
    if (isUserDirectlyInGroup(userId, groupId))
      return;

    SPUserRow user = organization.user.getUser(userId);
    if (user == null) {
      throw new AdminPersistenceException("SPGroupTable.addUserInGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_UNKNOWN_USER", "user Id: '"
              + userId + "'");
    }

    SPGroupRow group = getGroup(groupId);
    if (group == null) {
      throw new AdminPersistenceException("SPGroupTable.addUserInGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_UNKNOWN_GROUP",
          "group Id: '" + groupId + "'");
    }

    int[] params = new int[] { groupId, userId };
    updateRelation(INSERT_A_GROUP_USER_REL, params);
  }

  static final private String INSERT_A_GROUP_USER_REL = "insert into DomainSP_Group_User_Rel(groupId, userId) values(?,?)";

  /**
   * Removes an user from this group.
   */
  public void removeUserFromGroup(int userId, int groupId)
      throws AdminPersistenceException {
    if (!isUserDirectlyInGroup(userId, groupId)) {
      throw new AdminPersistenceException("SPGroupTable.createGroup",
          SilverpeasException.ERROR, "admin.EX_ERR_USER_NOT_IN_GROUP",
          "group Id: '" + groupId + "', user Id: '" + userId + "'");
    }

    int[] params = new int[] { groupId, userId };
    updateRelation(DELETE_GROUP_USER_REL, params);
  }

  static final private String DELETE_GROUP_USER_REL = "delete from DomainSP_Group_User_Rel where groupId = ? and userId = ?";

  /**
   * Fetch the current group row from a resultSet.
   */
  protected Object fetchRow(ResultSet rs) throws SQLException {
    return fetchGroup(rs);
  }

  private DomainSPSchema organization = null;
}
