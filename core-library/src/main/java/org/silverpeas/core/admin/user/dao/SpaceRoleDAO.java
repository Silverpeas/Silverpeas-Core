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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.user.dao;

import org.silverpeas.core.admin.persistence.SpaceUserRoleRow;
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.silverpeas.core.util.CollectionUtil.isNotEmpty;

@Repository
public class SpaceRoleDAO {

  private static final String SPACEUSERROLE_COLUMNS =
      "id,spaceId,name,RoleName,description,isInherited";
  private static final String FROM_GROUP_RELATION =
      " from ST_SpaceUserRole r, ST_SpaceUserRole_Group_Rel gr";
  private static final String WHERE_ID_EQUALS_SPACEUSERROLEID = " where r.id=gr.spaceUserRoleId";
  private static final String ROLE_SPACE_ID = "r.spaceId";
  private static final String FROM_USER_RELATION =
      " from ST_SpaceUserRole r, ST_SpaceUserRole_User_Rel ur";
  private static final String WHERE_ID_EQUALS_TO_SPACEUSERROLEID = " where r.id=ur.spaceUserRoleId";

  /**
   * Gets several groups and/or one user roles for the given identifier of space.
   * To get only groups roles, specify -1 for userId.
   * To get only user roles, specify null or empty list for groupIds.
   * @param con the connection with the database.
   * @param groupIds a list of group identifiers.
   * @param userId the user identifier.
   * @param spaceIds the space identifiers.
   * @return a list of {@link SpaceUserRoleRow} space identifiers.
   * @throws SQLException if an error occurs.
   */
  public List<SpaceUserRoleRow> getSpaceRoles(Connection con, List<String> groupIds, int userId,
      Collection<Integer> spaceIds) throws SQLException {
    List<SpaceUserRoleRow> roles = new ArrayList<>();
    if (isNotEmpty(groupIds)) {
      roles.addAll(getGroupRoles(con, spaceIds, groupIds));
    }
    if (userId != -1) {
      roles.addAll(getSpaceUserRoles(con, spaceIds, userId));
    }
    return roles;
  }

  private List<SpaceUserRoleRow> getGroupRoles(final Connection con,
      final Collection<Integer> spaceIds, final List<String> groupIds) throws SQLException {
    final List<SpaceUserRoleRow> roles = new ArrayList<>();
    final List<Integer> groupIdsAsInt = groupIds.stream().map(Integer::parseInt).collect(Collectors.toList());
    JdbcSqlQuery.executeBySplittingOn(groupIdsAsInt, (groupIdBatch, ignore) ->
        JdbcSqlQuery.executeBySplittingOn(spaceIds, (spaceIdBatch, ignoreToo) ->
            getQueryCommons(FROM_GROUP_RELATION, WHERE_ID_EQUALS_SPACEUSERROLEID)
            .and("gr.groupId").in(groupIdBatch)
            .and(ROLE_SPACE_ID).in(spaceIdBatch)
            .executeWith(con, r -> roles.add(SpaceUserRoleRow.fetch(r)))));
    return roles;
  }

  private List<SpaceUserRoleRow> getSpaceUserRoles(final Connection con,
      final Collection<Integer> spaceIds, final int userId) throws SQLException {
    final List<SpaceUserRoleRow> roles = new ArrayList<>();
    JdbcSqlQuery.executeBySplittingOn(spaceIds, (idBatch, ignore) ->
        getQueryCommons(FROM_USER_RELATION, WHERE_ID_EQUALS_TO_SPACEUSERROLEID)
        .and("ur.userId = ?", userId)
        .and(ROLE_SPACE_ID).in(idBatch)
        .executeWith(con, r -> roles.add(SpaceUserRoleRow.fetch(r))));
    return roles;
  }

  private JdbcSqlQuery getQueryCommons(final String joins, final String clauses) {
    return JdbcSqlQuery.createSelect("DISTINCT " + SPACEUSERROLE_COLUMNS)
        .addSqlPart(joins)
        .addSqlPart(clauses);
  }
}