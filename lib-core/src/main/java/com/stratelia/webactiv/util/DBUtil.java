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
package com.stratelia.webactiv.util;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.exception.MultilangMessage;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.pool.ConnectionPool;
import org.apache.commons.lang3.tuple.Pair;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.*;

public class DBUtil {

  private static DBUtil instance;

  /**
   * @return the DateFieldLength
   */
  public static int getDateFieldLength() {
    return getInstance().dateFieldLength;
  }

  /**
   * @return the TextMaxiLength
   */
  public static int getTextMaxiLength() {
    return getInstance().textMaxiLength;
  }

  /**
   * @return the TextAreaLength
   */
  public static int getTextAreaLength() {
    return getInstance().textAreaLength;
  }

  /**
   * @return the TextFieldLength
   */
  public static int getTextFieldLength() {
    return getInstance().textFieldLength;
  }

  private Connection connectionForTest;

  private DBUtil(Connection connectionForTest) {
    this.connectionForTest = connectionForTest;
  }

  public static DBUtil getInstance() {
    synchronized (DBUtil.class) {
      if (instance == null) {
        instance = new DBUtil(null);
      }
    }
    return instance;
  }

  public static DBUtil getInstanceForTest(Connection connectionForTest) {
    clearTestInstance();
    synchronized (DBUtil.class) {
      if (connectionForTest != null) {
        instance = new DBUtil(connectionForTest);
      }
    }
    return instance;
  }

  public static void clearTestInstance() {
    synchronized (DBUtil.class) {
      if (instance != null) {
        close(instance.connectionForTest);
      }
      instance = new DBUtil(null);
      dsStock.clear();
    }
  }

  /**
   * TextFieldLength is the maximum length to store an html textfield input in db.
   */
  private volatile int textFieldLength = 1000;
  /**
   * TextAreaLength is the maximum length to store an html textarea input in db.
   */
  private volatile int textAreaLength = 2000;
  /**
   * TextMaxiLength is the maximum length to store in db. This length is to use with fields that
   * can
   * contain a lot of information. This is the case of publication's model for exemple. TODO : In
   * the near future, these fields will have to be put in BLOB (Binary Large OBject).
   */
  private volatile int textMaxiLength = 4000;
  /**
   * DateFieldLength is the length to use for date storage.
   */
  private volatile int dateFieldLength = 10;
  // Static for the makeConnection
  private InitialContext ic = null;
  private static Map<String, DataSource> dsStock = new HashMap<String, DataSource>(5);

  /**
   * fabrique une nouvelle connection
   * @param dbName le nom de la base de donnée
   * @return a new connection to the database.
   * @throws UtilException
   */
  public static Connection makeConnection(String dbName) {
    return getInstance().openConnection(dbName);
  }

  private synchronized Connection openConnection(String dbName) {
    SilverTrace.debug("util", "DBUtil makeConnection", "DBUtil : makeConnection : entree");
    DataSource ds = null;
    if (ic == null) {
      try {
        ic = new InitialContext();
      } catch (NamingException e) {
        throw new UtilException("DBUtil.makeConnection", "util.MSG_CANT_GET_INITIAL_CONTEXT", e);
      }
    }
    try {
      ds = dsStock.get(dbName);
      if (ds == null) {
        ds = (DataSource) ic.lookup(dbName);
        dsStock.put(dbName, ds);
      }
    } catch (NamingException e) {
      throw new UtilException("DBUtil.makeConnection",
          new MultilangMessage("util.MSG_BDD_REF_NOT_FOUND", dbName).toString(), e);
    }

    try {
      return ds.getConnection();
    } catch (SQLException e) {
      throw new UtilException("DBUtil.makeConnection",
          new MultilangMessage("util.MSG_BDD_REF_CANT_GET_CONNECTION", dbName).toString(), e);
    }
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
   * @throws UtilException
   */
  public static int getNextId(String tableName, String idName) throws UtilException {
    Connection privateConnection = null;
    boolean testingMode = false;
    try {
      // On ne peux pas utiliser une simple connection du pool
      // on utilise une connection extérieure au contexte transactionnel des ejb
      synchronized (DBUtil.class) {
        if (getInstance().connectionForTest != null) {
          privateConnection = getInstance().connectionForTest;
          testingMode = true;
        } else {
          privateConnection = ConnectionPool.getConnection();
        }
      }
      privateConnection.setAutoCommit(false);
      return getNextId(privateConnection, tableName, idName);
    } catch (Exception ex) {
      SilverTrace.debug("util", "DBUtil.getNextId", "impossible de recupérer le prochain id", ex);
      if (privateConnection != null) {
        rollback(privateConnection);
      }
      throw new UtilException("DBUtil.getNextId",
          new MultilangMessage("util.MSG_CANT_GET_A_NEW_UNIQUE_ID", tableName, idName).toString(),
          ex);
    } finally {
      try {
        if (privateConnection != null && !testingMode) {
          privateConnection.close();
        }
      } catch (SQLException e) {
        SilverTrace.error("util", "DBUtil.getNextId", "root.EX_CONNECTION_CLOSE_FAILED", e);
      }
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
  public static int getNextId(Connection connection, String tableName, String idName)
      throws SQLException {
    return getMaxId(connection, tableName, idName);
  }

  protected static int getMaxId(Connection privateConnection, String tableName, String idName)
      throws SQLException {
    // tentative d'update
    SilverTrace.debug("util", "DBUtil.getNextId", "dBName = " + tableName);
    try {
      int max = updateMaxFromTable(privateConnection, tableName);
      privateConnection.commit();
      return max;
    } catch (Exception e) {
      // l'update n'a rien fait, il faut recuperer une valeur par defaut.
      // on recupere le max (depuis la table existante du composant)
      SilverTrace.debug("util", "DBUtil.getNextId",
          "impossible d'updater, if faut recuperer la valeur initiale", e);
    }
    int max = getMaxFromTable(privateConnection, tableName, idName);
    PreparedStatement createStmt = null;
    try {
      // on enregistre le max
      String createStatement = "INSERT INTO UniqueId (maxId, tableName) VALUES (?, ?)";
      createStmt = privateConnection.prepareStatement(createStatement);
      createStmt.setInt(1, max);
      createStmt.setString(2, tableName.toLowerCase());
      createStmt.executeUpdate();
      privateConnection.commit();
      return max;
    } catch (Exception e) {
      // impossible de creer, on est en concurence, on reessaye l'update.
      SilverTrace
          .debug("util", "DBUtil.getNextId", "impossible de creer, if faut reessayer l'update", e);
      rollback(privateConnection);
    } finally {
      close(createStmt);
    }
    max = updateMaxFromTable(privateConnection, tableName);
    privateConnection.commit();
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
          SilverTrace.error("util", "DBUtil.getNextId", "util.MSG_NO_RECORD_FOUND");
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
        SilverTrace.error("util", "DBUtil.close", "util.CAN_T_CLOSE_RESULTSET", e);
      }
    }
    if (st != null) {
      try {
        st.close();
      } catch (SQLException e) {
        SilverTrace.error("util", "DBUtil.close", "util.CAN_T_CLOSE_STATEMENT", e);
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
        SilverTrace.error("util", "DBUtil.close", "util.CAN_T_CLOSE_CONNECTION", e);
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
        SilverTrace.error("util", "DBUtil.close", "util.CAN_T_ROLLBACK_CONNECTION", e);
      }
    }
  }

  private final static String TABLE_NAME = "TABLE_NAME";

  /**
   * Gets all table names.
   * @return
   */
  public static Set<String> getAllTableNames() {
    Connection privateConnection = null;
    ResultSet tables_rs = null;
    boolean testingMode = false;
    Set<String> tableNames = new LinkedHashSet<String>();
    try {
      // On ne peux pas utiliser une simple connection du pool
      // on utilise une connection extérieure au contexte transactionnel des ejb
      synchronized (DBUtil.class) {
        if (getInstance().connectionForTest != null) {
          privateConnection = getInstance().connectionForTest;
          testingMode = true;
        } else {
          privateConnection = ConnectionPool.getConnection();
        }
      }

      DatabaseMetaData dbMetaData = privateConnection.getMetaData();
      tables_rs = dbMetaData.getTables(null, null, null, null);
      tables_rs.getMetaData();

      while (tables_rs.next()) {
        tableNames.add(tables_rs.getString(TABLE_NAME));
      }
    } catch (Exception e) {
      SilverTrace.debug("util", "DBUtil.getAllTableNames", "database error ...", e);
    } finally {
      close(tables_rs);
      if (privateConnection != null && !testingMode) {
        close(privateConnection);
      }
    }
    return tableNames;
  }

  /**
   * Indicates if the specified value is defined in point of view of SQL.
   * @param sqlValue the value to verify.
   * @return true if defined, false otherwise.
   */
  public static boolean isSqlDefined(String sqlValue) {
    return StringUtil.isDefined(sqlValue) && !sqlValue.trim().equals("-1");
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
      while (rs.next()) {
        ROW_ENTITY entity = rowProcess.read(rs);
        if (entity != null) {
          entities.add(entity);
        }
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
    protected abstract ROW_ENTITY read(ResultSet rs) throws SQLException;
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
  public static <O> StringBuilder appendListOfParameters(StringBuilder sqlQuery,
      Collection<O> parameters) {
    StringBuilder params = new StringBuilder();
    for (Object ignored : parameters) {
      if (params.length() > 0) {
        params.append(",");
      }
      params.append("?");
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
      } else if (parameter instanceof UserDetail) {
        preparedStatement.setString(paramIndex, ((UserDetail) parameter).getId());
      } else {
        throw new IllegalArgumentException(
            "SQL parameter type not handled: " + parameter.getClass());
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
