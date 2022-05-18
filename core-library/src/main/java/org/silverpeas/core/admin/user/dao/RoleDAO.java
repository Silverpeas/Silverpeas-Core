/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.admin.user.dao;

import org.silverpeas.core.admin.ProfiledObjectIds;
import org.silverpeas.core.admin.ProfiledObjectType;
import org.silverpeas.core.admin.persistence.UserRoleRow;
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.silverpeas.core.util.CollectionUtil.isNotEmpty;

@Repository
public class RoleDAO {

  private static final String USERROLE_COLUMNS =
      "id,instanceId,name,roleName,description,isInherited,objectId,objecttype";
  private static final String FROM_GROUP_RELATION =
      " from st_userrole r, st_userrole_group_rel gr";
  private static final String WHERE_ID_EQUALS_USERROLEID = " where r.id=gr.userroleid";
  private static final String AND_GROUP_ID_IN = " and gr.groupId IN (";
  private static final String ROLE_INSTANCE_ID = "r.instanceId";


  private static String list2String(List<String> ids) {
    StringBuilder str = new StringBuilder();
    for (int i = 0; i < ids.size(); i++) {
      if (i != 0) {
        str.append(",");
      }
      str.append(ids.get(i));
    }
    return str.toString();
  }

  private static final String SELECT = "select ";
  private static final String FROM_USER_RELATION =
      " from st_userrole r, st_userrole_user_rel ur";
  private static final String WHERE_ID_EQUALS_TO_USERROLEID = " where r.id=ur.userroleid";
  private static final String AND_USER_ID_EQUALS_TO_GIVEN_VALUE = " and ur.userId = ? ";
  private static final String QUERY_ALL_USER_ROLES =
      SELECT + USERROLE_COLUMNS + FROM_USER_RELATION +
          WHERE_ID_EQUALS_TO_USERROLEID + " and r.objectId is null " +
          AND_USER_ID_EQUALS_TO_GIVEN_VALUE;

  private List<UserRoleRow> getRoles(Connection con, int userId) throws SQLException {
    return getUserRoleRows(con, QUERY_ALL_USER_ROLES, userId);
  }

  private List<UserRoleRow> getUserRoleRows(final Connection con, final String queryAllUserRoles,
      final int userId) throws SQLException {
    final List<UserRoleRow> roles = new ArrayList<>();
    try (final PreparedStatement stmt = con.prepareStatement(queryAllUserRoles)) {
      stmt.setInt(1, userId);
      try (final ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
          roles.add(UserRoleRow.fetch(rs));
        }
      }
      return roles;
    }
  }


  /**
   * Gets several groups and/or one user component object roles (not the direct component roles).
   * To get only groups roles, specify -1 for userId.
   * To get only user roles, specify null or empty list for groupIds.
   * @param con connection with the database.
   * @param groupIds a list of group identifiers.
   * @param userId the user identifier.
   * @return a list of {@link UserRoleRow} instances.
   * @throws SQLException if an error occurs.
   */
  public List<UserRoleRow> getAllComponentObjectRoles(Connection con, List<String> groupIds,
      int userId) throws SQLException {
    List<UserRoleRow> roles = new ArrayList<>();
    if (isNotEmpty(groupIds)) {
      roles.addAll(getAllComponentObjectRolesForGroups(con, groupIds));
    }
    if (userId != -1) {
      roles.addAll(getAllComponentObjectRolesForUser(con, userId));
    }
    return roles;
  }

  private static final String QUERY_ALL_USER_COMPONENT_OBJECT_ROLES =
      SELECT + USERROLE_COLUMNS + FROM_USER_RELATION +
          WHERE_ID_EQUALS_TO_USERROLEID + " and r.objectId is not null " +
          AND_USER_ID_EQUALS_TO_GIVEN_VALUE;

  private List<UserRoleRow> getAllComponentObjectRolesForUser(Connection con, int userId)
      throws SQLException {
    return getUserRoleRows(con, QUERY_ALL_USER_COMPONENT_OBJECT_ROLES, userId);
  }

  private List<UserRoleRow> getRoles(Connection con, List<String> groupIds) throws SQLException {
    final String query = SELECT + USERROLE_COLUMNS + FROM_GROUP_RELATION +
        WHERE_ID_EQUALS_USERROLEID + " and r.objectId is null " + AND_GROUP_ID_IN +
        list2String(groupIds) + ")";

    return getUserRoleRows(con, query);
  }

  @Nonnull
  private List<UserRoleRow> getUserRoleRows(final Connection con, final String query)
      throws SQLException {
    final List<UserRoleRow> roles = new ArrayList<>();
    try (final PreparedStatement stmt = con.prepareStatement(query);
         final ResultSet rs = stmt.executeQuery()) {
      while (rs.next()) {
        roles.add(UserRoleRow.fetch(rs));
      }
    }
    return roles;
  }

  private List<UserRoleRow> getAllComponentObjectRolesForGroups(Connection con, List<String> groupIds) throws SQLException {
    String query = SELECT + USERROLE_COLUMNS + FROM_GROUP_RELATION +
        WHERE_ID_EQUALS_USERROLEID + " and r.objectId is not null " + AND_GROUP_ID_IN +
        list2String(groupIds) + ")";
    return getUserRoleRows(con, query);
  }

  public List<UserRoleRow> getRoles(Connection con, List<String> groupIds, int userId)
      throws SQLException {
    List<UserRoleRow> roles = new ArrayList<>();
    if (isNotEmpty(groupIds)) {
      roles.addAll(getRoles(con, groupIds));
    }
    if (userId != -1) {
      roles.addAll(getRoles(con, userId));
    }
    return roles;
  }

  /**
   * Gets several groups and/or one user roles for the given identifier of component instance
   * (not of objects managed by the component).
   * To get only groups roles, specify -1 for userId.
   * To get only user roles, specify null or empty list for groupIds.
   * @param con the connection with the database.
   * @param groupIds a list of group identifiers.
   * @param userId the user identifier.
   * @param instanceIds the component instance identifiers.
   * @return a list of {@link UserRoleRow} instances.
   * @throws SQLException if an error occurs.
   */
  public List<UserRoleRow> getRoles(Connection con, List<String> groupIds, int userId,
      Collection<Integer> instanceIds) throws SQLException {
    List<UserRoleRow> roles = new ArrayList<>();
    if (isNotEmpty(groupIds)) {
      roles.addAll(getGroupRoles(con, ProfiledObjectIds.none(), instanceIds, groupIds));
    }
    if (userId != -1) {
      roles.addAll(getUserRoles(con, ProfiledObjectIds.none(), instanceIds, userId));
    }
    return roles;
  }

  /**
   * Gets several groups and/or one user roles for objects of given identifiers of component
   * instance (not directly those of of the component).
   * To get only groups roles, specify -1 for userId.
   * To get only user roles, specify null or empty list for groupIds.
   * @param con a connection with the database.
   * @param profiledObjectIds if NOTHING is given, then all the rows associated to the type
   * are returned, otherwise all the rows associated to type and ids.
   * @param instanceIds the component instance identifiers.
   * @param groupIds a list of group identifiers.
   * @param userId the user identifier.
   * @return a list of {@link UserRoleRow} instances.
   * @throws SQLException if an error occurs.
   */
  public List<UserRoleRow> getRoles(Connection con, final ProfiledObjectIds profiledObjectIds,
      final Collection<Integer> instanceIds, final List<String> groupIds, final int userId)
      throws SQLException {
    final List<UserRoleRow> roles = new ArrayList<>();
    if (isNotEmpty(groupIds)) {
      roles.addAll(getGroupRoles(con, profiledObjectIds, instanceIds, groupIds));
    }
    if (userId != -1) {
      roles.addAll(getUserRoles(con, profiledObjectIds, instanceIds, userId));
    }
    return roles;
  }

  private List<UserRoleRow> getGroupRoles(final Connection con,
      final ProfiledObjectIds profiledObjectIds, final Collection<Integer> instanceIds,
      final List<String> groupIds) throws SQLException {
    final List<UserRoleRow> roles = new ArrayList<>();
    final List<Integer> groupIdsAsInt = groupIds.stream().map(Integer::parseInt).collect(Collectors.toList());
    final ProfiledObjectType type = profiledObjectIds.getType();
    if (type == ProfiledObjectType.NONE || profiledObjectIds.typeFilledOnly()) {
      JdbcSqlQuery.executeBySplittingOn(groupIdsAsInt, (groupIdBatch, ignore) ->
          JdbcSqlQuery.executeBySplittingOn(instanceIds, (instanceIdBatch, ignoreToo) ->
              getQueryCommons(FROM_GROUP_RELATION, WHERE_ID_EQUALS_USERROLEID, profiledObjectIds, type)
              .and("gr.groupId").in(groupIdBatch)
              .and(ROLE_INSTANCE_ID).in(instanceIdBatch)
              .executeWith(con, r -> roles.add(UserRoleRow.fetch(r)))));
    } else {
      JdbcSqlQuery.executeBySplittingOn(profiledObjectIds, (profileIdBatch, ignore) ->
        JdbcSqlQuery.executeBySplittingOn(groupIdsAsInt, (groupIdBatch, ignoreToo) ->
          JdbcSqlQuery.executeBySplittingOn(instanceIds, (instanceIdBatch, ignoreAlsoToo) ->
              getQueryCommons(FROM_GROUP_RELATION, WHERE_ID_EQUALS_USERROLEID, profileIdBatch, type)
              .and("gr.groupId").in(groupIdBatch)
              .and(ROLE_INSTANCE_ID).in(instanceIdBatch)
              .executeWith(con, r -> roles.add(UserRoleRow.fetch(r))))));
    }
    return roles;
  }

  private List<UserRoleRow> getUserRoles(final Connection con,
      final ProfiledObjectIds profiledObjectIds, final Collection<Integer> instanceIds,
      final int userId) throws SQLException {
    final List<UserRoleRow> roles = new ArrayList<>();
    final ProfiledObjectType type = profiledObjectIds.getType();
    if (type == ProfiledObjectType.NONE || profiledObjectIds.typeFilledOnly()) {
      JdbcSqlQuery.executeBySplittingOn(instanceIds, (idBatch, ignore) ->
          getQueryCommons(FROM_USER_RELATION, WHERE_ID_EQUALS_TO_USERROLEID, profiledObjectIds, type)
          .and("ur.userId = ?", userId)
          .and(ROLE_INSTANCE_ID).in(idBatch)
          .executeWith(con, r -> roles.add(UserRoleRow.fetch(r))));
    } else {
      JdbcSqlQuery.executeBySplittingOn(profiledObjectIds, (profileIdBatch, ignore) ->
        JdbcSqlQuery.executeBySplittingOn(instanceIds, (instanceIdBatch, ignoreToo) ->
            getQueryCommons(FROM_USER_RELATION, WHERE_ID_EQUALS_TO_USERROLEID, profileIdBatch, type)
            .and("ur.userId = ?", userId)
            .and(ROLE_INSTANCE_ID).in(instanceIdBatch)
            .executeWith(con, r -> roles.add(UserRoleRow.fetch(r)))));
    }
    return roles;
  }

  private JdbcSqlQuery getQueryCommons(final String joins, final String clauses,
      final Collection<String> objectIds, final ProfiledObjectType profiledObjectType) {
    final JdbcSqlQuery query = JdbcSqlQuery.createSelect("DISTINCT " + USERROLE_COLUMNS)
        .addSqlPart(joins)
        .addSqlPart(clauses);
    if (isNotEmpty(objectIds)) {
      query.and("r.objectId").in(objectIds.stream().map(Integer::parseInt).collect(Collectors.toSet()));
    }
    if (profiledObjectType != ProfiledObjectType.NONE) {
      query.and("r.objectType = ?", profiledObjectType.getCode());
    } else {
      query.andNull("r.objectType");
    }
    return query;
  }
}