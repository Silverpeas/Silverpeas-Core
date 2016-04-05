/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.admin.user.dao;

import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.silverpeas.core.util.ListSlice;

import javax.inject.Singleton;

@Singleton
public class GroupDAO {

  static final private String GROUP_COLUMNS =
      "id,specificId,domainId,superGroupId,name,description,synchroRule";

  public GroupDAO() {
  }
  static final private String queryGetGroup = "select " + GROUP_COLUMNS
      + " from ST_Group where id = ?";

  /**
   * Gets all the user groups available in Silverpeas whatever the user domain they belongs to.
   *
   * @param connection the connection with the data source to use.
   * @return a list of user groups.
   * @throws SQLException if an error occurs while getting the user groups from the data source.
   */
  public List<Group> getAllGroups(Connection connection) throws SQLException {
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      String query = "select " + GROUP_COLUMNS
          + " from st_group"
          + " order by name";
      statement = connection.prepareStatement(query);
      resultSet = statement.executeQuery();
      return theGroupsFrom(resultSet);
    } finally {
      DBUtil.close(resultSet, statement);
    }
  }

  /**
   * Gets the user groups that match the specified criteria. The criteria are provided by a
   * SearchCriteriaBuilder instance that was used to create them.
   *
   * @param connection the connetion with a data source to use.
   * @param criteria a builder with which the criteria the user groups must satisfy has been built.
   * @return a list slice of user groups matching the criteria or an empty list if no such user groups are
   * found. The slice is set by the pagination criteriion. If no such criterion is provided, then it
   * is the whole list of groups matching the other criteria.
   */
  public ListSlice<Group> getGroupsByCriteria(Connection connection,
      GroupSearchCriteriaForDAO criteria) throws SQLException {
    ListSlice<Group> groups;
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      String query = criteria.toSQLQuery(GROUP_COLUMNS);
      statement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
          ResultSet.CONCUR_READ_ONLY);
      resultSet = statement.executeQuery();
      if (criteria.isPaginationSet()) {
        PaginationPage page = criteria.getPagination();
        int start = (page.getPageNumber() - 1) * page.getPageSize();
        int end = start + page.getPageSize();
        groups = theGroupsFrom(resultSet, start, end);
      } else {
        groups = new ListSlice<Group>(theGroupsFrom(resultSet));
      }
    } finally {
      DBUtil.close(resultSet, statement);
    }
    return groups;
  }

  public Group getGroup(Connection con, String groupId)
      throws SQLException {
    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.prepareStatement(queryGetGroup);
      stmt.setInt(1, Integer.parseInt(groupId));

      rs = stmt.executeQuery();

      Group group = null;
      if (rs.next()) {
        group = fetchGroup(rs);
      }
      return group;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }
  static final private String queryGetSubGroups = "select " + GROUP_COLUMNS
      + " from ST_Group where superGroupId = ?";

  public List<Group> getSubGroups(Connection con, String groupId) throws SQLException {
    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      List<Group> groups = new ArrayList<Group>();
      stmt = con.prepareStatement(queryGetSubGroups);
      stmt.setInt(1, Integer.parseInt(groupId));

      rs = stmt.executeQuery();

      while (rs.next()) {
        groups.add(fetchGroup(rs));
      }
      return groups;
    } finally {
      DBUtil.close(rs, stmt);
    }

  }
  static final private String queryGetNBUsersDirectlyInGroup = "select count(userid)"
      + " from st_group_user_rel where groupid = ?";

  public int getNBUsersDirectlyInGroup(Connection con, String groupId) throws SQLException {
    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.prepareStatement(queryGetNBUsersDirectlyInGroup);
      stmt.setInt(1, Integer.parseInt(groupId));

      rs = stmt.executeQuery();

      if (rs.next()) {
        return rs.getInt(1);
      }
      return 0;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  static final private String queryGetUsersDirectlyInGroup = "select userid"
      + " from st_group_user_rel where groupid = ?";

  public List<String> getUsersDirectlyInGroup(Connection con, String groupId) throws SQLException {
    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      List<String> userIds = new ArrayList<String>();
      stmt = con.prepareStatement(queryGetUsersDirectlyInGroup);
      stmt.setInt(1, Integer.parseInt(groupId));

      rs = stmt.executeQuery();

      while (rs.next()) {
        userIds.add(Integer.toString(rs.getInt(1)));
      }
      return userIds;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  public List<String> getManageableGroupIds(Connection con, String userId,
      List<String> groupIds) throws SQLException {
    List<String> manageableGroupIds = new ArrayList<String>();
    if (StringUtil.isDefined(userId)) {
      manageableGroupIds.addAll(getManageableGroupIdsByUser(con, userId));
    }
    if (groupIds != null && groupIds.size() > 0) {
      manageableGroupIds.addAll(getManageableGroupIdsByGroups(con, groupIds));
    }
    return manageableGroupIds;
  }
  static final private String queryGetManageableGroupIdsByUser = "select st_groupuserrole.groupid"
      + " from st_groupuserrole_user_rel, st_groupuserrole "
      + " where st_groupuserrole_user_rel.groupuserroleid=st_groupuserrole.id"
      + " and st_groupuserrole_user_rel.userid=?";

  private List<String> getManageableGroupIdsByUser(Connection con, String userId)
      throws SQLException {
    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      List<String> groupIds = new ArrayList<String>();
      stmt = con.prepareStatement(queryGetManageableGroupIdsByUser);
      stmt.setInt(1, Integer.parseInt(userId));

      rs = stmt.executeQuery();

      while (rs.next()) {
        groupIds.add(Integer.toString(rs.getInt(1)));
      }
      return groupIds;
    } finally {
      DBUtil.close(rs, stmt);
    }

  }

  private List<String> getManageableGroupIdsByGroups(Connection con, List<String> groupIds)
      throws SQLException {
    Statement stmt = null;
    ResultSet rs = null;

    try {
      String aQueryGetManageableGroupIdsByUser = "select st_groupuserrole.groupid"
          + " from st_groupuserrole_group_rel, st_groupuserrole "
          + " where st_groupuserrole_group_rel.groupuserroleid=st_groupuserrole.id"
          + " and st_groupuserrole_group_rel.groupid IN (" + list2String(groupIds) + ")";

      List<String> manageableGroupIds = new ArrayList<String>();
      stmt = con.createStatement();

      rs = stmt.executeQuery(aQueryGetManageableGroupIdsByUser);

      while (rs.next()) {
        manageableGroupIds.add(Integer.toString(rs.getInt(1)));
      }
      return manageableGroupIds;
    } finally {
      DBUtil.close(rs, stmt);
    }

  }

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

  /**
   * Fetch the current group row from a resultSet.
   */
  private static Group fetchGroup(ResultSet rs) throws SQLException {

    Group group = new Group();
    group.setId(Integer.toString(rs.getInt("id")));
    group.setSpecificId(rs.getString("specificId"));
    if (group.getSpecificId().equals("-1")) {
      group.setSpecificId(null);
    }
    group.setDomainId(Integer.toString(rs.getInt("domainId")));
    group.setSuperGroupId(Integer.toString(rs.getInt("superGroupId")));
    if (rs.wasNull()) {
      group.setSuperGroupId(null);
    }
    group.setName(rs.getString("name"));
    group.setDescription(rs.getString("description"));
    group.setRule(rs.getString("synchroRule"));
    return group;
  }

  private static List<Group> theGroupsFrom(ResultSet rs) throws SQLException {
    List<Group> groups = new ArrayList<Group>();
    while (rs.next()) {
      groups.add(fetchGroup(rs));
    }
    return groups;
  }

  @SuppressWarnings("empty-statement")
  private static ListSlice<Group> theGroupsFrom(ResultSet rs, int start, int end) throws SQLException {
    ListSlice<Group> groups = new ListSlice<Group>(start, end);
    if (start > 0) {
      rs.next();
      rs.relative(start - 1);
    }
    int i;
    for (i = start; rs.next(); i++) {
      if (i < end) {
        groups.add(fetchGroup(rs));
      }
    }
    groups.setOriginalListSize(i);
    return groups;
  }
}
