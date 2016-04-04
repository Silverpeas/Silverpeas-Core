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
package org.silverpeas.core.persistence.jdbc;

import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class DBUtil {

  /**
   * @return the DateFieldLength
   */
  public static int getDateFieldLength() {
    return dateFieldLength;
  }

  /**
   * @return the TextMaxiLength
   */
  public static int getTextMaxiLength() {
    return textMaxiLength;
  }

  /**
   * @return the TextAreaLength
   */
  public static int getTextAreaLength() {
    return textAreaLength;
  }

  /**
   * @return the TextFieldLength
   */
  public static int getTextFieldLength() {
    return textFieldLength;
  }

  /**
   * TextFieldLength is the maximum length to store an html textfield input in db.
   */
  private static final int textFieldLength = 1000;
  /**
   * TextAreaLength is the maximum length to store an html textarea input in db.
   */
  private static final int textAreaLength = 2000;
  /**
   * TextMaxiLength is the maximum length to store in db. This length is to use with fields that
   * can
   * contain a lot of information. This is the case of publication's model for exemple. TODO : In
   * the near future, these fields will have to be put in BLOB (Binary Large OBject).
   */
  private static final int textMaxiLength = 4000;
  /**
   * DateFieldLength is the length to use for date storage.
   */
  private static final int dateFieldLength = 10;

  @Override
  public String toString() {
    return super.toString();
  }

  /**
   * fabrique une nouvelle connection
   * @return a new connection to the database.
   */
  public static Connection openConnection() throws SQLException {
    return ConnectionPool.getConnection();
  }

  /**
   * Returns a new unique identifier of type string.
   * @return a new unique string identifier.
   */
  public static String getUniqueId() {
    return UUID.randomUUID().toString();
  }

  /**
   * Return a new unique identifier value referenced by a name.
   * @param identifierName a name that does not correspond to something into persistence, but the
   * caller needs to handle unique identifiers for a resource.
   * @return a unique id.
   * @throws java.sql.SQLException
   */
  @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
  public static int getNextId(final String identifierName) throws SQLException {
    return getNextId(identifierName, null);
  }

  /**
   * Return a new unique identifier value referenced by a name.
   * @param identifierName a name of an identifier can be the name of an existing table or a name
   * that does not correspond to something into persistence, but the caller needs to handle
   * unique identifiers for a resource.
   * @param tableFieldIdentifierName the field name of the table name represented by
   * identifierName parameter that permits to initialize the first value of unique identifier for
   * the table in case of it is not yet referenced into the uniqueId table. If this value is not
   * defined, the identifierName parameter is not considered as a table name.
   * @return a unique id.
   * @throws java.sql.SQLException
   */
  @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
  public static int getNextId(final String identifierName, final String tableFieldIdentifierName)
      throws SQLException {
    final String identifierNameLowerCase = identifierName.toLowerCase(Locale.ROOT);
    while (true) {

      // Getting the next unique identifier value from uniqueId table
      Integer nextUniqueMaxId = nextUniqueIdentifierValue(identifierNameLowerCase);

      if (nextUniqueMaxId == null) {

        // The identifier is not yet registered into uniqueId table
        registeringIdentifierName(identifierNameLowerCase, tableFieldIdentifierName);

      } else if (nextUniqueMaxId != -1) {

        // The next identifier value has been well computed
        return nextUniqueMaxId;
      }
    }
  }

  /**
   * Updates and returns the next identifier value for given table name.
   * @param identifierNameLowerCase the name of identifier for which the next unique identifier
   * must be computed.
   * @return the next unique identifier if the identifier name is already registered into uniqueId
   * table, -1 if identifier name is already registered into uniqueId table but a concurrent server
   * process has just performed an update too (so caller has just to retry to call the method),
   * null if the identifier name is not yet registered into uniqueId table.
   * @throws SQLException
   */
  private static Integer nextUniqueIdentifierValue(String identifierNameLowerCase)
      throws SQLException {

    return Transaction.performInNew(() -> {

      // First getting the current unique identifier value
      final Integer currentUniqueValue;
      try (Connection connection = openConnection();
           PreparedStatement selectCurrentUniqueValueStmt = connection
               .prepareStatement("SELECT maxId FROM UniqueId WHERE tableName = ?")) {
        selectCurrentUniqueValueStmt.setString(1, identifierNameLowerCase);
        try (ResultSet rs = selectCurrentUniqueValueStmt.executeQuery()) {
          if (rs.next()) {
            currentUniqueValue = rs.getInt(1);
          } else {
            currentUniqueValue = null;
          }
        }

        // If the current identifier value exists, then computing the next one
        if (currentUniqueValue != null) {
          final int nextUniqueValue = (currentUniqueValue + 1);
          // MaxId data is part of the SQL update query clause in order to avoid to perform an
          // update whereas another server process has updated the value for the same identifier
          // name (so a typical concurrency case)
          try (PreparedStatement updateMaxIdStmt = connection.prepareStatement(
              "UPDATE UniqueId SET maxId = ? WHERE tableName = ? AND maxId = ?")) {
            updateMaxIdStmt.setInt(1, nextUniqueValue);
            updateMaxIdStmt.setString(2, identifierNameLowerCase);
            updateMaxIdStmt.setInt(3, currentUniqueValue);
            if (updateMaxIdStmt.executeUpdate() != 0) {
              // The next identifier value has been incremented successfully
              return nextUniqueValue;
            } else {
              // Another server process has just updated the next unique identifier value, so the
              // returned value indicates to the caller to retry to compute one
              SilverLogger.getLogger(DBUtil.class.getSimpleName()).info(
                  "The next unique identifier value '" + nextUniqueValue + "' for identifier '" +
                      identifierNameLowerCase +
                      "' has been computed by another server process call at the same time, " +
                      "trying " +
                      "again to get a next one");
              return -1;
            }
          }
        }
      } catch (SQLException ex) {
        return null;
      }

      // Returning null when the identifier name is not yet registered into uniqueId table
      return null;
    });
  }

  /**
   * Registers into uniqueId table the given identifier name. If it represents a table name and
   * if the identifier field name of this table is given, then the first value of the unique
   * identifier is the current maximum one of the table, otherwise the first value is 0.
   * @param identifierNameLowerCase a name of an identifier can be the name of an existing table or
   * a name that does not correspond to something into persistence, but the caller needs to handle
   * unique identifiers for a resource.
   * @param tableFieldIdentifierName the field name of the table name represented by
   * identifierName parameter that permits to initialize the first value of unique identifier for
   * the table in case of it is not yet referenced into the uniqueId table. If this value is not
   * defined, the identifierName parameter is not considered as a table name.
   * @throws SQLException
   */
  private static void registeringIdentifierName(String identifierNameLowerCase,
      String tableFieldIdentifierName) throws SQLException {
    try {
      Transaction.performInNew(() -> {

        int currentUniqueValue = 0;
        try (Connection connection = openConnection()) {

          if (StringUtil.isDefined(tableFieldIdentifierName)) {
            if (getAllTableNames(connection).contains(identifierNameLowerCase.toLowerCase())) {
              // Getting the maximum value of the field identifier of the table
              try (PreparedStatement selectTableCurrentUniqueValueStmt = connection
                  .prepareStatement("SELECT MAX(" + tableFieldIdentifierName + ") " + "FROM " +
                      identifierNameLowerCase)) {
                try (ResultSet rs = selectTableCurrentUniqueValueStmt.executeQuery()) {
                  if (rs.next()) {
                    currentUniqueValue = rs.getInt(1);
                  }
                }
              }
            }
          }
          // Registering the current unique value of the identifier
          try (PreparedStatement registerIdentifierStmt = connection
              .prepareStatement("INSERT INTO UniqueId (maxId, tableName) VALUES (?, ?)")) {
            registerIdentifierStmt.setInt(1, currentUniqueValue);
            registerIdentifierStmt.setString(2, identifierNameLowerCase);
            registerIdentifierStmt.executeUpdate();
          }

          SilverLogger.getLogger(DBUtil.class)
              .info("A new entry has been registered into UniqueId table for '" +
                  identifierNameLowerCase + "' table");
        }
        return null;
      });
    } catch (Exception e) {
      SilverLogger.getLogger(DBUtil.class)
          .warn("The unique identifier '" + identifierNameLowerCase +
              "' has not been registered because another server process has just done the " +
              "same " +
              "operation for the same resource.");
    }
  }

  // Close JDBC ResultSet and Statement
  public static void close(ResultSet rs, Statement st) {
    if (rs != null) {
      try {
        rs.close();
      } catch (SQLException e) {
        SilverLogger.getLogger(DBUtil.class).error(e.getMessage(), e);
      }
    }
    if (st != null) {
      try {
        st.close();
      } catch (SQLException e) {
        SilverLogger.getLogger(DBUtil.class).error(e.getMessage(), e);
      }
    }
  }

  // Close JDBC Statement
  public static void close(Statement st) {
    close(null, st);
  }

  // Close JDBC ResultSet
  public static void close(ResultSet rs) {
    close(rs, null);
  }

  public static void close(Connection connection) {
    if (connection != null) {
      try {
        connection.close();
      } catch (SQLException e) {
        SilverLogger.getLogger(DBUtil.class).error(e.getMessage(), e);
      }
    }
  }

  public static void rollback(Connection connection) {
    if (connection != null) {
      try {
        if (!connection.getAutoCommit() && !connection.isClosed()) {
          connection.rollback();
        }
      } catch (SQLException e) {
        SilverLogger.getLogger(DBUtil.class).error(e.getMessage(), e);
      }
    }
  }

  private final static String TABLE_NAME = "TABLE_NAME";

  /**
   * Gets all table names.
   * @param connection a current connection.
   * @return all the table name of the database.
   */
  private static Set<String> getAllTableNames(Connection connection) throws SQLException {
    Set<String> tableNames = new LinkedHashSet<>();
    DatabaseMetaData dbMetaData = connection.getMetaData();
    try (ResultSet tables_rs = dbMetaData.getTables(null, null, null, null)) {
      tables_rs.getMetaData();
      while (tables_rs.next()) {
        tableNames.add(tables_rs.getString(TABLE_NAME).toLowerCase());
      }
    }
    return tableNames;
  }

  /**
   * Gets all table names.
   * @return all the table name of the database.
   */
  @SuppressWarnings("unchecked")
  public static Set<String> getAllTableNames() {
    try (Connection connection = openConnection()) {
      return getAllTableNames(connection);
    } catch (Exception ignore) {
      return Collections.EMPTY_SET;
    }
  }
}
