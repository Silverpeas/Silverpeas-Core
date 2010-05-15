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

package com.stratelia.webactiv.beans.admin.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DBUtil;

public class UserDAO {

  static final private String USER_COLUMNS =
      "id,specificId,domainId,login,firstName,lastName,loginMail,email,accessLevel,loginQuestion,loginAnswer";

  public UserDAO() {

  }

  public static List<UserDetail> getUsersOfGroups(Connection con, List<String> groupIds)
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

      List<UserDetail> users = new ArrayList<UserDetail>();
      while (rs.next()) {
        users.add(fetchUser(rs));
      }
      return users;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  public static List<String> getUserIdsOfGroups(Connection con, List<String> groupIds)
      throws SQLException {
    Statement stmt = null;
    ResultSet rs = null;

    try {
      String query = "select id"
          + " from st_user u, st_group_user_rel g"
          + " where g.userid = u.id"
          + " and g.groupid IN (" + list2String(groupIds) + ")"
          + " and accessLevel <> 'R'"
          + " order by lastName";

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