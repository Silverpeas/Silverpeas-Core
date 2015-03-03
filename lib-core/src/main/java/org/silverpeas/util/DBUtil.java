/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.util;

import org.apache.commons.lang3.tuple.Pair;
import org.silverpeas.persistence.Transaction;
import org.silverpeas.util.pool.ConnectionPool;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

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

  private static final Logger logger = Logger.getLogger(DBUtil.class.getSimpleName());

  /**
   * fabrique une nouvelle connection
   * @return a new connection to the database.
   */
  public static Connection openConnection() throws SQLException {
    return ServiceProvider.getService(ConnectionPool.class).getConnection();
  }

  /**
   * Returns a new unique identifier of type string.
   * @return a new unique string identifier.
   */
  public static String getUniqueId() {
    return UUID.randomUUID().toString();
  }

  /**
   * Return a new unique Id for a table.
   * @param tableName the name of the table.
   * @param idName the name of the column.
   * @return a unique id.
   * @throws java.sql.SQLException
   */
  public static synchronized int getNextId(final String tableName, final String idName)
      throws SQLException {
    //noinspection RedundantCast
    final Pair<Integer, SQLException> result =
        Transaction.performInNew((Transaction.Process<Pair<Integer, SQLException>>) () -> {
          try (Connection connection = openConnection()) {
            return Pair.of(getMaxId(connection, tableName, idName), null);
          } catch (SQLException ex) {
            return Pair.of(null, ex);
          }
        });
    if (result.getRight() != null) {
      throw result.getRight();
    }
    return result.getLeft();
  }

  private static int getMaxId(Connection connection, String tableName, String idName)
      throws SQLException {
    // tentative d'update
    return selectMaxFromTable(connection, tableName, idName);
  }

  private static int updateMaxFromTable(Connection connection, String tableName, int oldValue)
      throws SQLException {
    String table = tableName.toLowerCase(Locale.ROOT);
    int max = 0;
    int count = 0;
    try (PreparedStatement updateStmt = connection.prepareStatement(
        "UPDATE UniqueId SET maxId = maxId + 1 WHERE tableName = ?" + " AND maxId = ?")) {
      updateStmt.setString(1, table);
      updateStmt.setInt(2, oldValue);
      count = updateStmt.executeUpdate();
    }

    if (count == 1) {
      try (PreparedStatement selectStmt = connection
          .prepareStatement("SELECT maxId FROM UniqueId WHERE tableName = ?")) {
        // update of max identifier has been done successfully, so the value of this new
        // identifier is retrieved
        selectStmt.setString(1, table);
        try (ResultSet rs = selectStmt.executeQuery()) {
          if (!rs.next()) {
            throw new RuntimeException("Erreur Interne DBUtil.getNextId()");
          }
          max = rs.getInt(1);
        }
      }
      return max;
    }
    throw new SQLException("Update impossible : Ligne non existante");
  }

  private static int selectMaxFromTable(Connection connection, String tableName, String idName) {
    int max = 0;
    try (PreparedStatement selectStmt = connection
        .prepareStatement("SELECT maxId FROM UniqueId WHERE tableName = ?")) {
      selectStmt.setString(1, tableName.toLowerCase(Locale.ROOT));
      try (ResultSet rs = selectStmt.executeQuery()) {
        // Insert or update strategy
        if (!rs.next()) {
          return insertMax(connection, tableName, idName);
        } else {
          // tableName exists inside UniqueId, we have to increment and return new one
          max = rs.getInt(1);
          return updateMaxFromTable(connection, tableName, max);
        }
      }
    } catch (SQLException e) {
      logger.log(Level.WARNING, e.getMessage(), e);
    }
    return max;
  }

  private static int insertMax(Connection connection, String tableName, String idName) {
    int max = getMaxFromTable(connection, tableName, idName);
    String createStatement = "INSERT INTO UniqueId (maxId, tableName) VALUES (?, ?)";
    try (PreparedStatement createStmt = connection.prepareStatement(createStatement)) {
      // Persist the max
      createStmt.setInt(1, max);
      createStmt.setString(2, tableName.toLowerCase());
      createStmt.executeUpdate();
    } catch (Exception e) {
      // access concurrency
      logger.log(Level.WARNING, e.getMessage(), e);
    }
    return max;
  }


  private static int getMaxFromTable(Connection con, String tableName, String idName) {
    if (!StringUtil.isDefined(tableName) || !StringUtil.isDefined(idName)) {
      return 1;
    }
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      int maxFromTable = 0;
      String nextPKStatement = "SELECT MAX(" + idName + ") " + "FROM " + tableName;
      prepStmt = con.prepareStatement(nextPKStatement);
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        maxFromTable = rs.getInt(1);
      }
      return maxFromTable + 1;
    } catch (SQLException ex) {
      return 1;
    } finally {
      close(rs, prepStmt);
    }
  }

  // Close JDBC ResultSet and Statement
  public static void close(ResultSet rs, Statement st) {
    if (rs != null) {
      try {
        rs.close();
      } catch (SQLException e) {
        logger.log(Level.SEVERE, e.getMessage(), e);
      }
    }
    if (st != null) {
      try {
        st.close();
      } catch (SQLException e) {
        logger.log(Level.SEVERE, e.getMessage(), e);
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
        logger.log(Level.SEVERE, e.getMessage(), e);
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
        logger.log(Level.SEVERE, e.getMessage(), e);
      }
    }
  }

  private final static String TABLE_NAME = "TABLE_NAME";

  /**
   * Gets all table names.
   * @return
   */
  public static Set<String> getAllTableNames() {
    Connection connection = null;
    ResultSet tables_rs = null;
    Set<String> tableNames = new LinkedHashSet<String>();
    try {
      connection = openConnection();
      DatabaseMetaData dbMetaData = connection.getMetaData();
      tables_rs = dbMetaData.getTables(null, null, null, null);
      tables_rs.getMetaData();

      while (tables_rs.next()) {
        tableNames.add(tables_rs.getString(TABLE_NAME));
      }
    } catch (Exception e) {
    } finally {
      close(tables_rs);
      close(connection);
    }
    return tableNames;
  }
}
