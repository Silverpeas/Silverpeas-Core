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
* "http://repository.silverpeas.com/legal/licensing"
*
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
* even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License along with this program.
* If not, see <http://www.gnu.org/licenses/>.
*/
package com.stratelia.webactiv.beans.admin.dao;

import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

  static final private String USER_COLUMNS =
          "distinct(id),specificId,domainId,login,firstName,lastName,loginMail,email,accessLevel,loginQuestion,loginAnswer";

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
      String query = "select " + USER_COLUMNS
              + " from st_user"
              + " where accessLevel <> 'R'"
              + " order by lastName";
      statement = connection.prepareStatement(query);
      resultSet = statement.executeQuery();
      return theUserDetailsFrom(resultSet);
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
      String query = "select " + USER_COLUMNS
              + " from st_user u, st_group_user_rel g"
              + " where u.id = g.userid"
              + " and accessLevel <> 'R'"
              + " order by lastName";
      statement = connection.prepareStatement(query);
      resultSet = statement.executeQuery();
      return theUserDetailsFrom(resultSet);
    } finally {
      DBUtil.close(resultSet, statement);
    }
  }

  /**
* Gets the user details that match the specified criteria.
* The criteria are provided by an UserSearchCriteriaBuilder instance that was used to create
* them.
*
* @param connection the connetion with a data source to use.
* @param criteria a builder with which the criteria the user details must satisfy has been built.
* @return a list of user details matching the criteria or an empty list if no such user details
* are found.
*/
  public List<UserDetail> getUsersByCriteria(Connection connection,
          UserSearchCriteriaForDAO criteria) throws SQLException {
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      if (criteria.isEmpty()) {
        return getAllUsers(connection);
      }
      String query = "select " + USER_COLUMNS
              + " from " + criteria.impliedTables()
              + " where " + criteria.toString()
              + " and accessLevel <> 'R'"
              + " order by lastName";
      statement = connection.prepareStatement(query);
      resultSet = statement.executeQuery();
      return theUserDetailsFrom(resultSet);
    } finally {
      DBUtil.close(resultSet, statement);
    }
  }

  public List<UserDetail> getUsersOfGroups(Connection con, List<String> groupIds)
          throws SQLException {
    Statement stmt = null;
    ResultSet rs = null;
    try {
      String query = "select " + USER_COLUMNS
              + " from st_user u, st_group_user_rel g"
              + " where g.userid = u.id"
              + " and g.groupid IN (" + list2String(groupIds) + ")"
              + " and accessLevel <> 'R'"
              + " order by lastName";

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
      String query = "select distinct(u.id), u.lastname"
              + " from st_user u, st_group_user_rel g"
              + " where g.userid = u.id"
              + " and g.groupid IN (" + list2String(groupIds) + ")"
              + " and u.accessLevel <> 'R'"
              + " order by u.lastName";

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

  private static List<UserDetail> theUserDetailsFrom(final ResultSet resultSet) throws SQLException {
    List<UserDetail> users = new ArrayList<UserDetail>();
    while (resultSet.next()) {
      users.add(fetchUser(resultSet));
    }
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
}