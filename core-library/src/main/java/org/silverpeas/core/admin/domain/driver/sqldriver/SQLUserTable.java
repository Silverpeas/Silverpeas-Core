/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.admin.domain.driver.sqldriver;

import org.silverpeas.core.admin.domain.model.DomainProperty;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singleton;
import static org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery.unique;

/**
 * a DAO to manage the DomainSQL_User table.
 */
public class SQLUserTable {
  private static final String WHERE = " where ";
  private static final String UPDATE = "update ";
  private static final String SET = " set ";
  private static final String EQUAL_TO_GIVEN_VALUE = " = ?,";
  private static final String SELECT = "select ";
  private static final String FROM = " from ";
  private static final String WHERE_ID_EQUAL_TO_GIVEN_VALUE = " where id = ?";
  private SQLSettings drvSettings;

  SQLUserTable(SQLSettings ds) {
    drvSettings = ds;
  }

  /**
   * Inserts in the database a new user row.
   */
  public int createUser(Connection c, UserDetail user) throws AdminException {
    final String theQuery = "insert into " + drvSettings.getUserTableName() + "("
        + getColumns() + ") values (?,?,?,?,?)";
    try (final PreparedStatement statement = c.prepareStatement(theQuery)) {
      final int nextId = DBUtil.getNextId(drvSettings.getUserTableName(), drvSettings
          .getUserSpecificIdColumnName());
      statement.setInt(1, nextId);
      statement.setString(2, drvSettings.trunc(user.getFirstName(), 100));
      statement.setString(3, drvSettings.trunc(user.getLastName(), 100));
      statement.setString(4, drvSettings.trunc(user.geteMail(), 100));
      statement.setString(5, drvSettings.trunc(user.getLogin(), 50));
      statement.executeUpdate();
      return nextId;
    } catch (Exception e) {
      throw new AdminException(e.getMessage(), e);
    }
  }

  public void deleteUser(Connection c, int userId) throws AdminException {
    final String theQuery = "delete from " + drvSettings.getUserTableName() + WHERE +
        drvSettings.getUserSpecificIdColumnName() + " = ?";
    try (final PreparedStatement statement = c.prepareStatement(theQuery)) {
      statement.setInt(1, userId);
      statement.executeUpdate();
    } catch (Exception e) {
      throw new AdminException(e.getMessage(), e);
    }
  }

  public void updateUser(Connection c, UserDetail ud) throws AdminException {
    final String theQuery =
        UPDATE + drvSettings.getUserTableName() + SET + drvSettings.getUserFirstNameColumnName() +
            EQUAL_TO_GIVEN_VALUE + drvSettings.getUserLastNameColumnName() + EQUAL_TO_GIVEN_VALUE +
            drvSettings.getUserEMailColumnName() + EQUAL_TO_GIVEN_VALUE +
            drvSettings.getUserLoginColumnName() + " = ? " + WHERE
        + drvSettings.getUserSpecificIdColumnName() + " = ?";

    try (final PreparedStatement statement = c.prepareStatement(theQuery)) {
      statement.setString(1, drvSettings.trunc(ud.getFirstName(), 100));
      statement.setString(2, drvSettings.trunc(ud.getLastName(), 100));
      statement.setString(3, drvSettings.trunc(ud.geteMail(), 100));
      statement.setString(4, drvSettings.trunc(ud.getLogin(), 50));
      statement.setInt(5, Integer.parseInt(ud.getSpecificId()));
      statement.executeUpdate();
    } catch (Exception e) {
      throw new AdminException(e.getMessage(), e);
    }
  }

  /**
   * Inserts in the database a new user row.
   */
  void updateUserSpecificProperty(Connection c, int userId,
      DomainProperty dp, String value) throws AdminException {
    final String theQuery =
        UPDATE + drvSettings.getUserTableName() + SET + dp.getMapParameter() + " = ?" + WHERE
        + drvSettings.getUserSpecificIdColumnName() + " = ?";

    try (final PreparedStatement statement = c.prepareStatement(theQuery)) {
      if (DomainProperty.PROPERTY_TYPE_BOOLEAN.equals(dp.getType())) {
        statement.setInt(1, Integer.parseInt(value));
      } else {
        statement.setString(1, value);
      }
      statement.setInt(2, userId);
      statement.executeUpdate();
    } catch (Exception e) {
      throw new AdminException(e.getMessage(), e);
    }
  }

  /**
   * Inserts in the database a new user row.
   */
  void updateUserPassword(Connection c, int userId, String value)
      throws AdminException {
    if (drvSettings.isUserPasswordAvailable()) {
      final String theQuery =
          UPDATE + drvSettings.getUserTableName() + SET + drvSettings.getUserPasswordColumnName() +
              " = ?" + WHERE
          + drvSettings.getUserSpecificIdColumnName() + " = ?";

      try (final PreparedStatement statement = c.prepareStatement(theQuery)) {
        statement.setString(1, value);
        statement.setInt(2, userId);
        statement.executeUpdate();
      } catch (Exception e) {
        throw new AdminException(e.getMessage(), e);
      }
    }
  }

  /**
   * Inserts in the database a new user row.
   */
  void updateUserPasswordValid(Connection c, int userId, boolean value)
      throws AdminException {
    if (drvSettings.isUserPasswordValidAvailable()) {
      final String theQuery = UPDATE + drvSettings.getUserTableName() + SET +
          drvSettings.getUserPasswordValidColumnName() + " = ?" + WHERE
          + drvSettings.getUserSpecificIdColumnName() + " = ?";

      try (final PreparedStatement statement = c.prepareStatement(theQuery)) {
        statement.setString(1, (value) ? "Y" : "N");
        statement.setInt(2, userId);
        statement.executeUpdate();
      } catch (Exception e) {
        throw new AdminException(e.getMessage(), e);
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
    List<Integer> theResult = new ArrayList<>();
    final String theQuery =
        SELECT + drvSettings.getUserSpecificIdColumnName() + FROM + drvSettings.getUserTableName();

    try (final PreparedStatement statement = c.prepareStatement(theQuery);
         final ResultSet rs = statement.executeQuery()) {
      while (rs.next()) {
        theResult.add(rs.getInt(1));
      }
    } catch (SQLException e) {
      throw new AdminException(e.getMessage(), e);
    }
    return theResult;
  }

  /**
   * Returns all the groups in a given userRole (not recursive).
   */
  public List<UserDetail> getAllUsers(Connection c) throws AdminException {
    List<UserDetail> theResult = new ArrayList<>();
    final String theQuery = SELECT + getColumns() + FROM
        + drvSettings.getUserTableName();

    try (final PreparedStatement statement = c.prepareStatement(theQuery);
         final ResultSet rs = statement.executeQuery()) {
      while (rs.next()) {
        theResult.add(fetchUser(rs));
      }
    } catch (SQLException e) {
      throw new AdminException(e.getMessage(), e);
    }
    return theResult;
  }

  /**
   * Returns all the groups in a given userRole (not recursive).
   */
  public List<UserDetail> getUsersBySpecificProperty(Connection c,
      String propertyName, String value) throws AdminException {
    final List<UserDetail> theResult = new ArrayList<>();
    final String theQuery = SELECT + getColumns() + FROM
        + drvSettings.getUserTableName() + " where lower(" + propertyName + ") like lower(?) ";

    try (final PreparedStatement statement = c.prepareStatement(theQuery)) {
      statement.setString(1, value);
      try (final ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
          theResult.add(fetchUser(rs));
        }
      }
    } catch (SQLException e) {
      throw new AdminException(e.getMessage(), e);
    }
    return theResult;
  }

  /**
   * Returns the User whith the given id.
   */
  public UserDetail getUser(Connection c, int userId) throws AdminException {
    return unique(getUsers(c, singleton(userId)));
  }

  /**
   * Returns users corresponding to given user ids.
   */
  public List<UserDetail> getUsers(Connection c, Collection<Integer> userIds)
      throws AdminException {
    try {
      return JdbcSqlQuery.streamBySplittingOn(userIds, idBatch ->
              JdbcSqlQuery.createSelect(getColumns())
                  .from(drvSettings.getUserTableName())
                  .where("id").in(idBatch)
                  .executeWith(c, this::fetchUser))
          .collect(Collectors.toList());
    } catch (SQLException e) {
      throw new AdminException(e.getMessage(), e);
    }
  }

  /**
   * Returns the User whith the given id.
   */
  String getUserSpecificProperty(Connection c, int userId,
      DomainProperty dp) throws AdminException {
    String theQuery = SELECT + dp.getMapParameter() + FROM + drvSettings.getUserTableName() +
        WHERE_ID_EQUAL_TO_GIVEN_VALUE;
    return executeFieldQuery(c, theQuery, userId);
  }

  /**
   * Returns the User whith the given id.
   */
  String getUserPassword(Connection c, int userId) throws AdminException {
    if (drvSettings.isUserPasswordAvailable()) {
      String theQuery =
          SELECT + drvSettings.getUserPasswordColumnName() + FROM + drvSettings.getUserTableName() +
              WHERE_ID_EQUAL_TO_GIVEN_VALUE;
      return executeFieldQuery(c, theQuery, userId);
    } else {
      return "";
    }
  }

  private String executeFieldQuery(final Connection c, final String theQuery, final int userId)
      throws AdminException {
    try (final PreparedStatement statement = c.prepareStatement(theQuery)) {
      statement.setInt(1, userId);
      try (final ResultSet rs = statement.executeQuery()) {
        if (rs.next()) {
          return rs.getString(1);
        } else {
          return "";
        }
      }
    } catch (SQLException e) {
      throw new AdminException(e.getMessage(), e);
    }
  }

  /**
   * Returns the User whith the given id.
   */
  boolean getUserPasswordValid(Connection c, int userId)
      throws AdminException {
    if (drvSettings.isUserPasswordValidAvailable()) {
      final String theQuery = SELECT + drvSettings.getUserPasswordValidColumnName() + FROM +
          drvSettings.getUserTableName() + WHERE_ID_EQUAL_TO_GIVEN_VALUE;

      try (final PreparedStatement statement = c.prepareStatement(theQuery)) {
        statement.setInt(1, userId);
        try (final ResultSet rs = statement.executeQuery()) {
          if (rs.next()) {
            return "Y".equalsIgnoreCase(rs.getString(1));
          } else {
            return drvSettings.isUserPasswordAvailable();
          }
        }
      } catch (SQLException e) {
        throw new AdminException(e.getMessage(), e);
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
  private UserDetail fetchUser(ResultSet rs) throws SQLException {
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
