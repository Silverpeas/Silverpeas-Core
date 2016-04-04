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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.admin.domain.driver.sqldriver;

import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.domain.model.DomainProperty;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A UserTable object manages the DomainSQL_User table.
 */
public class SQLUserTable {
  SQLSettings drvSettings = new SQLSettings();

  public SQLUserTable(SQLSettings ds) {
    drvSettings = ds;
  }

  /**
   * Inserts in the database a new user row.
   */
  public int createUser(Connection c, UserDetail user) throws AdminException {
    PreparedStatement statement = null;
    int nextId = 0;
    String theQuery = "insert into " + drvSettings.getUserTableName() + "("
        + getColumns() + ") values (?,?,?,?,?)";

    try {
      nextId = DBUtil.getNextId(drvSettings.getUserTableName(), drvSettings
          .getUserSpecificIdColumnName());
      statement = c.prepareStatement(theQuery);
      statement.setInt(1, nextId);
      statement.setString(2, drvSettings.trunc(user.getFirstName(), 100));
      statement.setString(3, drvSettings.trunc(user.getLastName(), 100));
      statement.setString(4, drvSettings.trunc(user.geteMail(), 100));
      statement.setString(5, drvSettings.trunc(user.getLogin(), 50));
      statement.executeUpdate();
    } catch (Exception e) {
      throw new AdminException("SQLUserTable.createUser",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", "Query = "
          + theQuery, e);
    } finally {
      DBUtil.close(statement);
    }
    return nextId;
  }

  public void deleteUser(Connection c, int userId) throws AdminException {
    PreparedStatement statement = null;
    String theQuery = "delete from " + drvSettings.getUserTableName()
        + " where " + drvSettings.getUserSpecificIdColumnName() + " = ?";

    try {
      statement = c.prepareStatement(theQuery);
      statement.setInt(1, userId);
      statement.executeUpdate();
    } catch (Exception e) {
      throw new AdminException("SQLUserTable.deleteUser",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", "Query = "
          + theQuery, e);
    } finally {
      DBUtil.close(statement);
    }
  }

  public void updateUser(Connection c, UserDetail ud) throws AdminException {
    PreparedStatement statement = null;
    String theQuery = "update " + drvSettings.getUserTableName() + " set "
        + drvSettings.getUserFirstNameColumnName() + " = ?,"
        + drvSettings.getUserLastNameColumnName() + " = ?,"
        + drvSettings.getUserEMailColumnName() + " = ?,"
        + drvSettings.getUserLoginColumnName() + " = ? " + " where "
        + drvSettings.getUserSpecificIdColumnName() + " = ?";

    try {
      statement = c.prepareStatement(theQuery);
      statement.setString(1, drvSettings.trunc(ud.getFirstName(), 100));
      statement.setString(2, drvSettings.trunc(ud.getLastName(), 100));
      statement.setString(3, drvSettings.trunc(ud.geteMail(), 100));
      statement.setString(4, drvSettings.trunc(ud.getLogin(), 50));
      statement.setInt(5, Integer.parseInt(ud.getSpecificId()));
      statement.executeUpdate();
    } catch (Exception e) {
      throw new AdminException("SQLUserTable.updateUser",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", "Query = "
          + theQuery, e);
    } finally {
      DBUtil.close(statement);
    }
  }

  /**
   * Inserts in the database a new user row.
   */
  public void updateUserSpecificProperty(Connection c, int userId,
      DomainProperty dp, String value) throws AdminException {
    PreparedStatement statement = null;
    String theQuery = "update " + drvSettings.getUserTableName() + " set "
        + dp.getMapParameter() + " = ?" + " where "
        + drvSettings.getUserSpecificIdColumnName() + " = ?";

    try {
      statement = c.prepareStatement(theQuery);
      if (DomainProperty.PROPERTY_TYPE_BOOLEAN.equals(dp.getType())) {
        statement.setInt(1, Integer.parseInt(value));
      } else {
        statement.setString(1, value);
      }
      statement.setInt(2, userId);
      statement.executeUpdate();
    } catch (Exception e) {
      throw new AdminException("SQLUserTable.createUser",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", "Query = "
          + theQuery, e);
    } finally {
      DBUtil.close(statement);
    }
  }

  /**
   * Inserts in the database a new user row.
   */
  public void updateUserPassword(Connection c, int userId, String value)
      throws AdminException {
    if (drvSettings.isUserPasswordAvailable()) {
      PreparedStatement statement = null;
      String theQuery = "update " + drvSettings.getUserTableName() + " set "
          + drvSettings.getUserPasswordColumnName() + " = ?" + " where "
          + drvSettings.getUserSpecificIdColumnName() + " = ?";

      try {
        statement = c.prepareStatement(theQuery);
        statement.setString(1, value);
        statement.setInt(2, userId);
        statement.executeUpdate();
      } catch (Exception e) {
        throw new AdminException("SQLUserTable.updateUserPassword",
            SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", "Query = "
            + theQuery, e);
      } finally {
        DBUtil.close(statement);
      }
    }
  }

  /**
   * Inserts in the database a new user row.
   */
  public void updateUserPasswordValid(Connection c, int userId, boolean value)
      throws AdminException {
    if (drvSettings.isUserPasswordValidAvailable()) {
      PreparedStatement statement = null;
      String theQuery = "update " + drvSettings.getUserTableName() + " set "
          + drvSettings.getUserPasswordValidColumnName() + " = ?" + " where "
          + drvSettings.getUserSpecificIdColumnName() + " = ?";

      try {
        statement = c.prepareStatement(theQuery);
        statement.setString(1, (value) ? "Y" : "N");
        statement.setInt(2, userId);
        statement.executeUpdate();
      } catch (Exception e) {
        throw new AdminException("SQLUserTable.updateUserPasswordValid",
            SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", "Query = "
            + theQuery, e);
      } finally {
        DBUtil.close(statement);
      }
    }
  }

  /**
   * Inserts in the database a new user row.
   */
  public void updateUserSpecificProperty(Connection c, int userId,
      DomainProperty dp, boolean value) throws AdminException {
    updateUserSpecificProperty(c, userId, dp, (value) ? "Y" : "N");
  }

  /**
   * Returns all the groups in a given userRole (not recursive).
   */
  public List<Integer> getAllUserIds(Connection c) throws AdminException {
    ResultSet rs = null;
    PreparedStatement statement = null;
    List<Integer> theResult = new ArrayList<Integer>();
    String theQuery = "select " + drvSettings.getUserSpecificIdColumnName()
        + " from " + drvSettings.getUserTableName();

    try {
      statement = c.prepareStatement(theQuery);
      rs = statement.executeQuery();
      while (rs.next()) {
        theResult.add(rs.getInt(1));
      }
    } catch (SQLException e) {
      throw new AdminException("SQLUserTable.getDirectGroupIdsOfUser",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", "Query = "
          + theQuery, e);
    } finally {
      DBUtil.close(rs, statement);
    }
    return theResult;
  }

  /**
   * Returns all the groups in a given userRole (not recursive).
   */
  public List<UserDetail> getAllUsers(Connection c) throws AdminException {
    ResultSet rs = null;
    PreparedStatement statement = null;
    List<UserDetail> theResult = new ArrayList<UserDetail>();
    String theQuery = "select " + getColumns() + " from "
        + drvSettings.getUserTableName();

    try {
      statement = c.prepareStatement(theQuery);
      rs = statement.executeQuery();
      while (rs.next()) {
        theResult.add(fetchUser(rs));
      }
    } catch (SQLException e) {
      throw new AdminException("getAllUsers", SilverpeasException.ERROR,
          "root.EX_SQL_QUERY_FAILED", "Query = " + theQuery, e);
    } finally {
      DBUtil.close(rs, statement);
    }
    return theResult;
  }

  /**
   * Returns all the groups in a given userRole (not recursive).
   */
  public List<UserDetail> getUsersBySpecificProperty(Connection c,
      String propertyName, String value) throws AdminException {
    ResultSet rs = null;
    PreparedStatement statement = null;
    List<UserDetail> theResult = new ArrayList<UserDetail>();
    String theQuery = "select " + getColumns() + " from "
        + drvSettings.getUserTableName();
    theQuery += " where " + propertyName + " = ? ";

    try {
      statement = c.prepareStatement(theQuery);
      statement.setString(1, value);
      rs = statement.executeQuery();
      while (rs.next()) {
        theResult.add(fetchUser(rs));
      }
    } catch (SQLException e) {
      throw new AdminException("getUsersBySpecificProperty",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", "Query = "
          + theQuery, e);
    } finally {
      DBUtil.close(rs, statement);
    }
    return theResult;
  }

  /**
   * Returns the User whith the given id.
   */
  public UserDetail getUser(Connection c, int userId) throws AdminException {
    ResultSet rs = null;
    PreparedStatement statement = null;
    String theQuery = "select " + getColumns() + " from "
        + drvSettings.getUserTableName() + " where id = ?";

    try {
      statement = c.prepareStatement(theQuery);
      statement.setInt(1, userId);
      rs = statement.executeQuery();
      if (rs.next()) {
        return fetchUser(rs);
      } else {
        return null;
      }
    } catch (SQLException e) {
      throw new AdminException("SQLUserTable.getUser",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", "Query = "
          + theQuery, e);
    } finally {
      DBUtil.close(rs, statement);
    }
  }

  /**
   * Returns the User whith the given id.
   */
  public String getUserSpecificProperty(Connection c, int userId,
      DomainProperty dp) throws AdminException {
    ResultSet rs = null;
    PreparedStatement statement = null;
    String theQuery = "select " + dp.getMapParameter() + " from "
        + drvSettings.getUserTableName() + " where id = ?";

    try {
      statement = c.prepareStatement(theQuery);
      statement.setInt(1, userId);
      rs = statement.executeQuery();
      if (rs.next()) {
        return rs.getString(1);
      } else {
        return "";
      }
    } catch (SQLException e) {
      throw new AdminException("SQLUserTable.getUserSpecificProperty",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", "Query = "
          + theQuery, e);
    } finally {
      DBUtil.close(rs, statement);
    }
  }

  /**
   * Returns the User whith the given id.
   */
  public String getUserPassword(Connection c, int userId) throws AdminException {
    if (drvSettings.isUserPasswordAvailable()) {
      ResultSet rs = null;
      PreparedStatement statement = null;
      String theQuery = "select " + drvSettings.getUserPasswordColumnName()
          + " from " + drvSettings.getUserTableName() + " where id = ?";

      try {
        statement = c.prepareStatement(theQuery);
        statement.setInt(1, userId);
        rs = statement.executeQuery();
        if (rs.next()) {
          return rs.getString(1);
        } else {
          return "";
        }
      } catch (SQLException e) {
        throw new AdminException("SQLUserTable.getUserPassword",
            SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", "Query = "
            + theQuery, e);
      } finally {
        DBUtil.close(rs, statement);
      }
    } else {
      return "";
    }
  }

  /**
   * Returns the User whith the given id.
   */
  public boolean getUserPasswordValid(Connection c, int userId)
      throws AdminException {
    if (drvSettings.isUserPasswordValidAvailable()) {
      ResultSet rs = null;
      PreparedStatement statement = null;
      String theQuery = "select "
          + drvSettings.getUserPasswordValidColumnName() + " from "
          + drvSettings.getUserTableName() + " where id = ?";

      try {
        statement = c.prepareStatement(theQuery);
        statement.setInt(1, userId);
        rs = statement.executeQuery();
        if (rs.next()) {
          return "Y".equalsIgnoreCase(rs.getString(1));
        } else {
          return drvSettings.isUserPasswordAvailable();
        }
      } catch (SQLException e) {
        throw new AdminException("SQLUserTable.getUserPasswordValid",
            SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", "Query = "
            + theQuery, e);
      } finally {
        DBUtil.close(rs, statement);
      }
    } else {
      return drvSettings.isUserPasswordAvailable();
    }
  }

  protected String getColumns() {
    return drvSettings.getUserSpecificIdColumnName() + ", "
        + drvSettings.getUserFirstNameColumnName() + ", "
        + drvSettings.getUserLastNameColumnName() + ", "
        + drvSettings.getUserEMailColumnName() + ", "
        + drvSettings.getUserLoginColumnName();
  }

  /**
   * Fetch the current user row from a resultSet.
   */
  protected UserDetail fetchUser(ResultSet rs) throws SQLException {
    UserDetail u = new UserDetail();

    u.setSpecificId(Integer.toString(rs.getInt(1)));
    u.setFirstName(rs.getString(2));
    u.setLastName(rs.getString(3));
    u.seteMail(rs.getString(4));
    u.setLogin(rs.getString(5));
    u.setAccessLevel(UserAccessLevel.USER);
    return u;
  }
}
