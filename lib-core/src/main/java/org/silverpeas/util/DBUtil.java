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

import org.silverpeas.util.pool.ConnectionPool;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
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
  public static int getNextId(String tableName, String idName) throws SQLException {
    Connection connection = null;
    try {
      connection = openConnection();
      connection.setAutoCommit(false);
      return getNextId(connection, tableName, idName);
    } catch (SQLException ex) {
      if (connection != null) {
        rollback(connection);
      }
      throw ex;
    } finally {
      close(connection);
    }
  }

  /**
   * Return a new unique Id for a table.
   * @param connection the JDBC connection.
   * @param tableName the name of the table.
   * @param idName the name of the column.
   * @return a unique id.
   * @throws SQLException
   */
  protected static int getNextId(Connection connection, String tableName, String idName)
      throws SQLException {
    return getMaxId(connection, tableName, idName);
  }

  protected static int getMaxId(Connection connection, String tableName, String idName)
      throws SQLException {
    // tentative d'update
    try {
      int max = updateMaxFromTable(connection, tableName);
      connection.commit();
      return max;
    } catch (Exception e) {
    }
    int max = getMaxFromTable(connection, tableName, idName);
    PreparedStatement createStmt = null;
    try {
      // on enregistre le max
      String createStatement = "INSERT INTO UniqueId (maxId, tableName) VALUES (?, ?)";
      createStmt = connection.prepareStatement(createStatement);
      createStmt.setInt(1, max);
      createStmt.setString(2, tableName.toLowerCase());
      createStmt.executeUpdate();
      connection.commit();
      return max;
    } catch (Exception e) {
      // impossible de creer, on est en concurence, on reessaye l'update.
      rollback(connection);
    } finally {
      close(createStmt);
    }
    max = updateMaxFromTable(connection, tableName);
    connection.commit();
    return max;
  }

  private static int updateMaxFromTable(Connection connection, String tableName)
      throws SQLException {
    String table = tableName.toLowerCase(Locale.ROOT);
    int max = 0;
    PreparedStatement prepStmt = null;
    int count = 0;
    try {
      prepStmt =
          connection.prepareStatement("UPDATE UniqueId SET maxId = maxId + 1 WHERE tableName = ?");
      prepStmt.setString(1, table);
      count = prepStmt.executeUpdate();
      connection.commit();
    } catch (SQLException sqlex) {
      rollback(connection);
      throw sqlex;
    } finally {
      close(prepStmt);
    }

    if (count == 1) {
      PreparedStatement selectStmt = null;
      ResultSet rs = null;
      try {
        // l'update c'est bien passe, on recupere la valeur
        selectStmt = connection.prepareStatement("SELECT maxId FROM UniqueId WHERE tableName = ?");
        selectStmt.setString(1, table);
        rs = selectStmt.executeQuery();
        if (!rs.next()) {
          throw new RuntimeException("Erreur Interne DBUtil.getNextId()");
        }
        max = rs.getInt(1);
      } finally {
        close(rs, selectStmt);
      }
      return max;
    }
    throw new SQLException("Update impossible : Ligne non existante");
  }

  public static int getMaxFromTable(Connection con, String tableName, String idName) {
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
      rollback(con);
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

  /**
   * Indicates if the specified value is defined in point of view of SQL.
   * @param sqlValue the value to verify.
   * @return true if defined, false otherwise.
   */
  public static boolean isSqlDefined(String sqlValue) {
    return StringUtil.isDefined(sqlValue) && !sqlValue.trim().equals("-1") &&
        !sqlValue.trim().equals("unknown");
  }

  /**
   * Select count SQL query executor.
   * @param con
   * @param selectCountQuery
   * @param idParam
   * @throws SQLException
   */
  @SuppressWarnings("unchecked")
  public static <O> long selectCount(Connection con, String selectCountQuery, O idParam)
      throws SQLException {
    return selectCount(con, selectCountQuery, Arrays.asList(idParam));
  }

  /**
   * Select count SQL query executor.
   * @param con
   * @param selectCountQuery
   * @param params
   * @throws SQLException
   */
  public static <O> long selectCount(Connection con, String selectCountQuery, Collection<O> params)
      throws SQLException {
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(selectCountQuery);
      DBUtil.setParameters(prepStmt, params);
      rs = prepStmt.executeQuery();
      rs.next();
      long count = rs.getLong(1);
      if (rs.next()) {
        throw new IllegalArgumentException("select count execution error");
      }
      return count;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   * Update query executor.
   * @param con
   * @param updateQuery
   * @param parameters
   * @throws SQLException
   */
  @SuppressWarnings("unchecked")
  public static <ROW_ENTITY> List<ROW_ENTITY> select(Connection con, String updateQuery,
      Object parameters, SelectResultRowProcessor<ROW_ENTITY> rowProcess) throws SQLException {
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      st = con.prepareStatement(updateQuery);
      final Collection<Object> sqlParams;
      if (parameters instanceof Object[]) {
        sqlParams = Arrays.asList((Object[]) parameters);
      } else if (parameters instanceof Collection) {
        sqlParams = (Collection) parameters;
      } else {
        sqlParams = Arrays.asList(parameters);
      }
      DBUtil.setParameters(st, sqlParams);
      rs = st.executeQuery();
      List<ROW_ENTITY> entities = new ArrayList<ROW_ENTITY>();
      int i = 0;
      while (rs.next()) {
        if (rowProcess.limit > 0 && entities.size() >= rowProcess.limit) {
          break;
        }
        ROW_ENTITY entity = rowProcess.currentRow(i, rs);
        if (entity != null) {
          entities.add(entity);
        }
        i++;
      }
      return entities;
    } finally {
      DBUtil.close(rs, st);
    }
  }

  /**
   * Result Set Row Processor
   * @param <ROW_ENTITY>
   */
  public static abstract class SelectResultRowProcessor<ROW_ENTITY> {
    private final int limit;

    protected SelectResultRowProcessor() {
      this(0);
    }

    protected SelectResultRowProcessor(int limit) {
      this.limit = limit;
    }

    protected abstract ROW_ENTITY currentRow(final int rowIndex, ResultSet rs) throws SQLException;
  }

  /**
   * Gets from a entity list the unique entity expected.
   * @param entities the entity list.
   * @return the unique entity result.
   * @throws IllegalArgumentException if it exists more than one entity in the specified
   * list.
   */
  public static <ENTITY> ENTITY unique(List<ENTITY> entities) {
    if (entities.isEmpty()) {
      return null;
    }
    if (entities.size() == 1) {
      return entities.get(0);
    }
    throw new IllegalArgumentException(
        "wanted to get a unique entity from a list that contains more than one...");
  }

  /**
   * Update query executor.
   * @param con
   * @param updateQueries
   * @throws SQLException
   */
  public static <O> long executeUpdate(Connection con, List<Pair<String, List<O>>> updateQueries)
      throws SQLException {
    long nbUpdate = 0;
    for (Pair<String, List<O>> updateQuery : updateQueries) {
      nbUpdate += executeUpdate(con, updateQuery.getLeft(), updateQuery.getRight());
    }
    return nbUpdate;
  }

  public static long executeUpdate(Connection con, String updateQuery, Object... parameters)
      throws SQLException {
    return executeUpdate(con, updateQuery, (Object) parameters);
  }

  /**
   * Update query executor.
   * @param con
   * @param updateQuery
   * @param parameters
   * @throws SQLException
   */
  @SuppressWarnings("unchecked")
  public static long executeUpdate(Connection con, String updateQuery, Object parameters)
      throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(updateQuery);
      final Collection<Object> sqlParams;
      if (parameters instanceof Object[]) {
        sqlParams = Arrays.asList((Object[]) parameters);
      } else if (parameters instanceof Collection) {
        sqlParams = (Collection) parameters;
      } else {
        sqlParams = Arrays.asList(parameters);
      }
      DBUtil.setParameters(prepStmt, sqlParams);
      return prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Centralization in order to populate the prepare statement parameters.
   * @param sqlQuery
   * @param paramName
   * @param paramValue
   * @param isInsert
   * @param parameters
   * @return
   */
  public static <O> StringBuilder appendSaveParameter(StringBuilder sqlQuery, String paramName,
      O paramValue, final boolean isInsert, Collection<O> parameters) {
    if (parameters.size() > 0) {
      sqlQuery.append(", ");
    }
    sqlQuery.append(paramName);
    if (!isInsert) {
      sqlQuery.append(" = ?");
    }
    parameters.add(paramValue);
    return sqlQuery;
  }

  /**
   * Centralization in order to populate the prepare statement parameters.
   * @param sqlQuery
   * @param sqlPart
   * @param parameters
   * @return
   */
  public static <O> StringBuilder appendParameter(StringBuilder sqlQuery, String sqlPart,
      O paramValue, Collection<O> parameters) {
    parameters.add(paramValue);
    return sqlQuery.append(sqlPart);
  }

  /**
   * Centralization in order to build easily a SQL in clause.
   * @param sqlQuery
   * @param parameters
   * @return
   */
  public static StringBuilder appendListOfParameters(StringBuilder sqlQuery,
      Collection<?> parameters) {
    return appendListOfParameters(sqlQuery, parameters, null);
  }

  /**
   * Centralization in order to build easily a SQL in clause.
   * @param sqlQuery
   * @param parameters
   * @param allParameters
   * @return
   */
  @SuppressWarnings("unchecked")
  public static StringBuilder appendListOfParameters(StringBuilder sqlQuery,
      Collection<?> parameters, Collection<?> allParameters) {
    StringBuilder params = new StringBuilder();
    if (parameters != null) {
      for (Object ignored : parameters) {
        if (params.length() > 0) {
          params.append(",");
        }
        params.append("?");
      }
      if (allParameters != null) {
        allParameters.addAll((Collection) parameters);
      }
    }
    return sqlQuery.append("(").append(params.toString()).append(")");
  }

  /**
   * Centralization in order to sets the parameters on a prepare statement.
   * @param preparedStatement
   * @param parameters
   * @throws SQLException
   */
  public static <O> void setParameters(PreparedStatement preparedStatement,
      Collection<O> parameters) throws SQLException {
    int paramIndex = 1;
    for (Object parameter : parameters) {
      if (parameter == null) {
        preparedStatement.setObject(paramIndex, null);
      } else if (parameter instanceof String) {
        preparedStatement.setString(paramIndex, (String) parameter);
      } else if (parameter instanceof Enum) {
        preparedStatement.setString(paramIndex, ((Enum) parameter).name());
      } else if (parameter instanceof Integer) {
        preparedStatement.setInt(paramIndex, (Integer) parameter);
      } else if (parameter instanceof Long) {
        preparedStatement.setLong(paramIndex, (Long) parameter);
      } else if (parameter instanceof Timestamp) {
        preparedStatement.setTimestamp(paramIndex, (Timestamp) parameter);
      } else if (parameter instanceof Date) {
        preparedStatement.setDate(paramIndex, new java.sql.Date(((Date) parameter).getTime()));
      } else {
        try {
          Method idGetter = parameter.getClass().getDeclaredMethod("getId");
          String id = (String) idGetter.invoke(parameter);
          preparedStatement.setString(paramIndex, id);
        } catch (NoSuchMethodException|InvocationTargetException|IllegalAccessException e) {
          throw new IllegalArgumentException(
              "SQL parameter type not handled: " + parameter.getClass());
        }
      }
      paramIndex++;
    }
  }

  /**
   * Gets a long value from a current result set.
   * @param resultSet
   * @param index
   * @return the long value if it exists, null otherwise.
   * @throws SQLException
   */
  public static Long getLong(ResultSet resultSet, int index) throws SQLException {
    if (resultSet.getObject(index) != null) {
      return resultSet.getLong(index);
    }
    return null;
  }

  /**
   * Gets a Date value from a Long value from a current result set.
   * @param resultSet
   * @param index
   * @return the Date value if it exists a long value, null otherwise.
   * @throws SQLException
   */
  public static Date getDateFromLong(ResultSet resultSet, int index) throws SQLException {
    Long dateIntoLongFormat = getLong(resultSet, index);
    if (dateIntoLongFormat != null) {
      return new Date(dateIntoLongFormat);
    }
    return null;
  }
}
