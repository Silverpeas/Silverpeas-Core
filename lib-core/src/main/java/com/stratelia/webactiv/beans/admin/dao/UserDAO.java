/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.stratelia.webactiv.beans.admin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.PaginationPage;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DBUtil;
import java.util.ArrayList;
import java.util.List;
import org.silverpeas.util.ListSlice;

public class UserDAO {

  static final private String USER_COLUMNS =
      "distinct(id),specificId,domainId,login,firstName,lastName,loginMail,email,accessLevel,loginQuestion,loginAnswer";
  private static final String SELECT_DOMAINS_BY_LOGIN = "SELECT DISTINCT(domainId) AS domain FROM "
      + "st_user WHERE accessLevel <> 'R' AND  login = ? ORDER BY domainId";
  public UserDAO() {
  }

  /**
   * Gets all the user details available in Silverpeas whatever the they are part or not of a user
   * group.
   *
   * @param connection the connection with the data source to use.
   * @return a list of user details.
   * @throws SQLException if an error occurs while getting the user details from the data source.
   */
  public List<UserDetail> getAllUsers(Connection connection) throws SQLException {
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      String query = "SELECT " + USER_COLUMNS
          + " FROM st_user WHERE accessLevel <> 'R' ORDER by lastName";
      statement = connection.prepareStatement(query);
      resultSet = statement.executeQuery();
      return theUserDetailsFrom(resultSet);
    } finally {
      DBUtil.close(resultSet, statement);
    }
  }

  /**
   * Get all the domains id in which a user with the specified login exists.
   *
   * @param connection the connection with the data source to use.
   * @param login the login for which we want the domains.
   * @return a list of domain ids.
   * @throws SQLException if an error occurs while getting the user details from the data source.
   */
  public List<String> getDomainsContainingLogin(Connection connection, String login) throws
      SQLException {
    List<String> domainIds = new ArrayList<String>();
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      statement = connection.prepareStatement(SELECT_DOMAINS_BY_LOGIN);
      statement.setString(1, login);
      resultSet = statement.executeQuery();
      while (resultSet.next()) {
        domainIds.add(resultSet.getString("domain"));
      }
      return domainIds;
    } finally {
      DBUtil.close(resultSet, statement);
    }
  }

  /**
   * Gets all the details on the users that are part of at least one user group in Silverpeas.
   *
   * @param connection the connetion with a data source to use.
   * @return a list of user details or an empty list of no users are found in a group.
   * @throws SQLException if an error occurs while getting the user details from the data source.
   */
  public List<UserDetail> getUsersOfAllGroups(Connection connection) throws SQLException {
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      String query = "SELECT " + USER_COLUMNS
          + " FROM st_user u, st_group_user_rel g WHERE u.id = g.userid AND accessLevel <> 'R'"
          + " ORDER BY lastName";
      statement = connection.prepareStatement(query);
      resultSet = statement.executeQuery();
      return theUserDetailsFrom(resultSet);
    } finally {
      DBUtil.close(resultSet, statement);
    }
  }

  /**
   * Gets the user details that match the specified criteria. The criteria are provided by an
   * UserSearchCriteriaBuilder instance that was used to create them.
   *
   * @param connection the connetion with a data source to use.
   * @param criteria a builder with which the criteria the user details must satisfy has been built.
   * @return a list of user details matching the criteria or an empty list if no such user details
   * are found.
   */
  public ListSlice<UserDetail> getUsersByCriteria(Connection connection,
      UserSearchCriteriaForDAO criteria) throws SQLException {
    ListSlice<UserDetail> users;
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      String query = criteria.toSQLQuery(USER_COLUMNS);
      statement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
          ResultSet.CONCUR_READ_ONLY);
      resultSet = statement.executeQuery();
      if (criteria.isPaginationSet()) {
        PaginationPage page = criteria.getPagination();
        int start = (page.getPageNumber() - 1) * page.getPageSize();
        int end = start + page.getPageSize();
        users = theUserDetailsFrom(resultSet, start, end);
      } else {
        users = new ListSlice<UserDetail>(theUserDetailsFrom(resultSet));
      }
    } finally {
      DBUtil.close(resultSet, statement);
    }
    return users;
  }

  /**
   * Gets the number of users that match the specified criteria. The criteria are provided by an
   * UserSearchCriteriaBuilder instance that was used to create them.
   *
   * @param connection the connetion with a data source to use.
   * @param criteria a builder with which the criteria the user profiles must satisfy has been
   * built.
   * @return the number of users that match the specified criteria.
   */
  public int getUserCountByCriteria(Connection connection, UserSearchCriteriaForDAO criteria) throws
      SQLException {
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      String query = criteria.toSQLQuery("COUNT(*)");
      statement = connection.prepareStatement(query);
      resultSet = statement.executeQuery();
      resultSet.next();
      return resultSet.getInt(1);
    } finally {
      DBUtil.close(resultSet, statement);
    }
  }

  public String[] getUserIdsInGroupAndInDomain(Connection connection, String groupId,
      String domainId) throws SQLException {
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    if (StringUtil.isDefined(groupId)) {
      throw new NullPointerException("The group identifier is null!");
    }
    try {
      StringBuilder query = new StringBuilder(200);
      query.append("SELECT DISTINCT st_user.id FROM st_user, st_group_user_rel");
      query.append(" WHERE st_group_user_rel.groupId = ").append(groupId);
      if (StringUtil.isDefined(domainId)) {
        query.append(" AND st_user.domainId = ").append(domainId);
      }
      query.append(" AND accessLevel <> 'R' ORDER BY lastName");
      statement = connection.prepareStatement(query.toString());
      resultSet = statement.executeQuery();
      return getIds(resultSet);
    } finally {
      DBUtil.close(resultSet, statement);
    }
  }

  public List<UserDetail> getUsersOfGroups(Connection con, List<String> groupIds)
      throws SQLException {
    Statement stmt = null;
    ResultSet rs = null;
    try {
      String query = "SELECT " + USER_COLUMNS
          + " FROM st_user u, st_group_user_rel g WHERE g.userid = u.id"
          + " AND g.groupid IN (" + StringUtil.join(groupIds, ',') + ") AND accessLevel <> 'R'"
          + " ORDER BY lastName";
      stmt = con.createStatement();
      rs = stmt.executeQuery(query);
      return theUserDetailsFrom(rs);
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  public List<String> getUserIdsOfGroups(Connection con, List<String> groupIds)
      throws SQLException {
    Statement stmt = null;
    ResultSet rs = null;

    try {
      String query = "SELECT DISTINCT(u.id), u.lastname FROM st_user u, st_group_user_rel g"
          + " WHERE g.userid = u.id AND g.groupid IN (" + StringUtil.join(groupIds, ',') + ")"
          + " AND u.accessLevel <> 'R' ORDER BY u.lastName";
      stmt = con.createStatement();
      rs = stmt.executeQuery(query);
      List<String> ids = new ArrayList<String>();
      while (rs.next()) {
        ids.add(Integer.toString(rs.getInt(1)));
      }
      return ids;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  private static List<UserDetail> theUserDetailsFrom(final ResultSet resultSet) throws SQLException {
    List<UserDetail> users = new ArrayList<UserDetail>();
    while (resultSet.next()) {
      users.add(fetchUser(resultSet));
    }
    return users;
  }

  @SuppressWarnings("empty-statement")
  private static ListSlice<UserDetail> theUserDetailsFrom(ResultSet rs, int start, int end) throws
      SQLException {
    ListSlice<UserDetail> users = new ListSlice<UserDetail>(start, end);
    rs.relative(start);
    int i;
    for (i = start; rs.next() && i < end; i++) {
      users.add(fetchUser(rs));
    }
    if (i == end) {
      i++;
    }
    for (; rs.next(); i++);
    users.setOriginalListSize(i);
    return users;
  }

  /**
   * Fetch the current user row from a resultSet.
   */
  private static UserDetail fetchUser(ResultSet rs) throws SQLException {
    UserDetail u = new UserDetail();
    u.setId(Integer.toString(rs.getInt(1)));
    u.setSpecificId(rs.getString(2));
    u.setDomainId(Integer.toString(rs.getInt(3)));
    u.setLogin(rs.getString(4));
    u.setFirstName(rs.getString(5));
    u.setLastName(rs.getString(6));
    u.seteMail(rs.getString(8));
    u.setAccessLevel(rs.getString(9));
    u.setLoginQuestion(rs.getString(10));
    u.setLoginAnswer(rs.getString(11));
    return u;
  }

  private static String[] getIds(ResultSet rs) throws SQLException {
    List<String> userIds = new ArrayList<String>();
    while (rs.next()) {
      userIds.add(String.valueOf(rs.getInt(1)));
    }
    return userIds.toArray(new String[userIds.size()]);
  }
}