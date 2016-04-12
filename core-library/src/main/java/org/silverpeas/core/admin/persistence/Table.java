/**
* Copyright (C) 2000 - 2013 Silverpeas
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation, either version 3 of the
* License, or (at your option) any later version.
*
* As a special exception to the terms and conditions of version 3.0 of
* the GPL, you may redistribute this Program in connection with Free/Libre
* Open Source Software ("FLOSS") applications as described in Silverpeas's
* FLOSS exception. You should have received a copy of the text describing
* the FLOSS exception, and it is also available here:
* "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package org.silverpeas.core.admin.persistence;

import static org.silverpeas.core.util.StringUtil.isDefined;

import org.silverpeas.core.admin.domain.synchro.SynchroDomainReport;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.Schema;
import org.silverpeas.core.exception.SilverpeasException;

import java.sql.*;
import java.util.*;
import java.util.Date;

/**
* A Table object manages a table in a database.
*/
public abstract class Table<T> {

  public Table(Schema schema, String tableName) {
    this.schema = schema;
    this.tableName = tableName;
  }

  static String getNotNullString(String sn) {
    if (!isDefined(sn)) {
      return "";
    }
    return sn;
  }

  static Timestamp getSqlTimestamp(Date date) {
    if (date == null) {
      return null;
    }
    return new Timestamp(date.getTime());
  }

  /**
* Builds an aliased columns list from a row alias and a columns list. Returns
* "u.id,u.firstName,u.lastName" for row alias "u" columns list "id,firstName,lastName".
* @param rowAlias
* @param columnList
* @return
*/
  static public String aliasColumns(String rowAlias, String columnList) {
    StringBuilder result = new StringBuilder();
    StringTokenizer st = new StringTokenizer(columnList, ",");
    String separator = "";
    while (st.hasMoreTokens()) {
      String column = st.nextToken();
      result.append(separator);
      result.append(rowAlias);
      result.append('.');
      result.append(column);
      separator = ",";
    }

    return result.toString();
  }

  /**
* Truncates a string value to be inserted in a fixed size column
* @param value
* @param maxSize
* @return
*/
  static public String truncate(String value, int maxSize) {
    if (value != null && value.length() > maxSize) {
      return value.substring(0, maxSize - 1);
    }
    return value;
  }

  /**
* Returns the next id which can be used to create a new row.
* @return
* @throws SQLException
*/
  public int getNextId() throws SQLException {
    int nextId;
    try {
      nextId = DBUtil.getNextId(tableName, "id");
    } catch (Exception e) {
      throw new SQLException(e.toString(), e);
    }
    if (nextId == 0) {
      return 1;
    }
    return nextId;
  }

  /**
* Builds a new row object which values are retrieved from the given ResultSet.
* @param rs
* @return
* @throws SQLException
*/
  abstract protected T fetchRow(ResultSet rs) throws SQLException;

  /**
* Set all the parameters of the insert PreparedStatement built from the insertQuery in order to
* insert the given row.
* @param insertQuery
* @param insert
* @param row
* @throws SQLException
*/
  abstract protected void prepareInsert(String insertQuery, PreparedStatement insert, T row)
          throws SQLException;

  /**
* Set all the parameters of the update PreparedStatement built from the updateQuery in order to
* update the given row.
* @param updateQuery
* @param update
* @param row
* @throws SQLException
*/
  abstract protected void prepareUpdate(String updateQuery, PreparedStatement update, T row)
          throws SQLException;

  /**
* Returns the unique row referenced by the given query and id. Returns null if no rows match the
* id. Throws a AdminPersistenceException if more then one row match the id.
* @return the requiered row or null.
* @throws AdminPersistenceException
* @param query the sql query string must be like "select * from ... where ... id=?" where id is
* an int column.
* @param id references an unique row.
*/
  protected synchronized T getUniqueRow(String query, int id) throws AdminPersistenceException {
    ResultSet rs = null;
    PreparedStatement select = null;
    try {
      select = schema.getStatement(query);
      select.setInt(1, id);
      rs = select.executeQuery();
      return getUniqueRow(rs);
    } catch (SQLException e) {
      throw new AdminPersistenceException("Table.getUniqueRow", SilverpeasException.ERROR,
              "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      DBUtil.close(rs, select);
    }
  }

  /**
* Returns the unique row referenced by the given query and id. Returns null if no rows match the
* id. Throws a AdminPersistenceException if more then one row match the id.
* @return the requiered row or null.
* @throws AdminPersistenceException
* @param query the sql query string must be like "select * from ... where ... id=?" where id is
* an String column.
* @param id references an unique row.
*/
  protected synchronized T getUniqueRow(String query, String id) throws AdminPersistenceException {
    ResultSet rs = null;
    PreparedStatement select = null;
    try {
      select = schema.getStatement(query);
      select.setString(1, id);
      rs = select.executeQuery();
      return getUniqueRow(rs);
    } catch (SQLException e) {
      throw new AdminPersistenceException("Table.getUniqueRow", SilverpeasException.ERROR,
              "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      DBUtil.close(rs, select);
    }
  }

  /**
   * Returns the ids described by the given no parameters query.
   * @param query
   * @return
   * @throws AdminPersistenceException
   */
  protected synchronized List<String> getIds(String query) throws AdminPersistenceException {
    return getIdsUnsynchronized(query, null);
  }

  /**
   * Returns the ids described by the given query with one id parameter.
   * @param query
   * @param id
   * @return
   * @throws AdminPersistenceException
   */
  protected synchronized List<String> getIds(String query, int id)
      throws AdminPersistenceException {
    return getIdsUnsynchronized(query, Collections.singletonList(id));
  }

  /**
   * Returns the rows described by the given query with parameters.
   * Handled Types :
   * - Integer
   * - Long
   * - String
   * - Date (util)
   * - Date (sql)
   * - Timestamp
   * @param query
   * @param params
   * @return
   * @throws AdminPersistenceException
   */
  protected synchronized List<String> getIds(String query, List<?> params)
      throws AdminPersistenceException {
    return getIdsUnsynchronized(query, params);
  }

  /**
   * Returns the rows described by the given query with parameters.
   * @param query
   * @param params
   * @return
   * @throws AdminPersistenceException
   */
  private List<String> getIdsUnsynchronized(String query, List<?> params)
      throws AdminPersistenceException {
    ResultSet rs = null;
    PreparedStatement select = null;
    try {
      logQuery("getIds", query, params);
      select = schema.getStatement(query);
      performPrepareStatementParams(select, params);
      rs = select.executeQuery();
      return getIds(rs);
    } catch (SQLException e) {
      throw new AdminPersistenceException("Table.getIds", SilverpeasException.ERROR,
          "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      DBUtil.close(rs, select);
    }
  }

  /**
   * Returns the rows described by the given no parameters query.
   * @param query
   * @return
   * @throws AdminPersistenceException
   */
  protected synchronized List<T> getRows(String query) throws AdminPersistenceException {
    return getRowsUnsynchronized(query, null);
  }

  /**
   * Returns the rows described by the given query with one id parameter.
   * @param query
   * @param id
   * @return
   * @throws AdminPersistenceException
   */
  protected synchronized List<T> getRows(String query, int id) throws AdminPersistenceException {
    return getRowsUnsynchronized(query, Collections.singletonList(id));
  }

  /**
   * Returns the rows described by the given query with parameters.
   * Handled Types :
   * - Integer
   * - Long
   * - String
   * - Date (util)
   * - Date (sql)
   * - Timestamp
   * @param query
   * @param params
   * @return
   * @throws AdminPersistenceException
   */
  protected synchronized List<T> getRows(String query, List<?> params)
      throws AdminPersistenceException {
    return getRowsUnsynchronized(query, params);
  }

  /**
   * Returns the rows described by the given query with parameters.
   * @param query
   * @param params
   * @return
   * @throws AdminPersistenceException
   */
  private List<T> getRowsUnsynchronized(String query, List<?> params)
      throws AdminPersistenceException {
    ResultSet rs = null;
    PreparedStatement select = null;
    try {
      logQuery("getRows", query, params);
      select = schema.getStatement(query);
      performPrepareStatementParams(select, params);
      rs = select.executeQuery();
      return getRows(rs);
    } catch (SQLException e) {
      throw new AdminPersistenceException("Table.getRows", SilverpeasException.ERROR,
          "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      DBUtil.close(rs, select);
    }
  }

  /**
   * Centralization of log writing.
   * @param fromMethodName
   * @param query
   * @param params
   */
  private void logQuery(String fromMethodName, String query, List<?> params) {
    StringBuilder message = new StringBuilder(query);
    if (params != null && !params.isEmpty()) {
      message.append(" params[]: ");
      message.append(Arrays.toString(params.toArray()));
    }
  }

  /**
   * Centralization of PreparedStatement parameter setting.
   * @param ps
   * @param params
   * @throws SQLException
   */
  protected void performPrepareStatementParams(PreparedStatement ps, List<?> params)
      throws SQLException {
    int i = 1;
    if (params != null) {
      for (Object param : params) {
        if (param instanceof Integer) {
          ps.setInt(i++, (Integer) param);
        } else if (param instanceof String) {
          ps.setString(i++, (String) param);
        } else if (param instanceof Long) {
          ps.setLong(i++, (Long) param);
        } else if (param instanceof java.sql.Date) {
          ps.setDate(i++, (java.sql.Date) param);
        } else if (param instanceof Timestamp) {
          ps.setTimestamp(i++, (Timestamp) param);
        } else if (param instanceof Date) {
          ps.setDate(i++, new java.sql.Date(((Date) param).getTime()));
        }
      }
    }
  }

  /**
   * Returns the nb of rows in the given table agregated on the given column
   * @param tableName
   * @param whereClause
   * @param param
   * @return
   * @throws AdminPersistenceException
   */
  protected int getCount(String tableName, String whereClause, String param)
      throws AdminPersistenceException {
    ResultSet rs = null;
    String query = "select count(*) as nbResult from " + tableName + " where " + whereClause;
    PreparedStatement select = null;
    try {
      select = schema.getStatement(query);
      select.setString(1, param);
      rs = select.executeQuery();
      if (rs.next()) {
        return rs.getInt(1);
      }
      return 0;
    } catch (SQLException e) {
      throw new AdminPersistenceException("Table.getCount", SilverpeasException.ERROR,
          "root.EX_SQL_QUERY_FAILED", e);
    } catch (Exception e) {
      throw new AdminPersistenceException("Table.getCount", SilverpeasException.ERROR,
          "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      DBUtil.close(rs, select);
    }
  }

  /**
   * Returns the nb of rows in the given table agregated on the given column
   * @param tableName
   * @param whereClause
   * @param id
   * @param param
   * @return
   * @throws AdminPersistenceException
   */
  protected int getCount(String tableName, String whereClause, int id, String param)
      throws AdminPersistenceException {
    ResultSet rs = null;
    String query = "select count(*) as nbResult from " + tableName + " where " + whereClause;
    PreparedStatement select = null;

    try {
      select = schema.getStatement(query);
      select.setInt(1, id);
      select.setString(2, param);
      rs = select.executeQuery();
      if (rs.next()) {
        return rs.getInt(1);
      }
      return 0;
    } catch (SQLException e) {
      throw new AdminPersistenceException("Table.getCount", SilverpeasException.ERROR,
          "root.EX_SQL_QUERY_FAILED", e);
    } catch (Exception e) {
      throw new AdminPersistenceException("Table.getCount", SilverpeasException.ERROR,
          "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      DBUtil.close(rs, select);
    }
  }

  /**
   * Returns the rows like a sample row. The sample is build from a matchColumns names list and a
   * matchValues list of values. For each matchColumn with a non null matchValue is added a
   * criterium : where matchColumn like 'matchValue' The wildcard caracters %, must be set by the
   * caller : so we can choose and do queries as "login like 'exactlogin'" and queries as
   * "lastName like 'Had%'" or "lastName like '%addo%'". The returned rows are given by the
   * returnedColumns parameter which is of the form 'col1, col2, ..., colN'.
   * @param returnedColumns
   * @param matchColumns
   * @param matchValues
   * @return
   * @throws AdminPersistenceException
   */
  protected List<T> getMatchingRows(String returnedColumns, String[] matchColumns,
          String[] matchValues) throws AdminPersistenceException {
    String query = "select " + returnedColumns + " from " + tableName;
    List<String> notNullValues = new ArrayList<String>();
    String sep = " where ";
    for (int i = 0; i < matchColumns.length; i++) {
      if (matchValues[i] != null) {
        query += sep + matchColumns[i] + " like ?";
        sep = " , ";
        notNullValues.add(matchValues[i]);
      }
    }
    return getRows(query, notNullValues);
  }

  /**
* Returns the integer of the single row, single column resultset returned by the given query with
* id parameters. Returns null if the result set was empty.
* @param query
* @param ids
* @return
* @throws AdminPersistenceException
*/
  protected Integer getInteger(String query, int[] ids) throws AdminPersistenceException {
    ResultSet rs = null;
    PreparedStatement select = null;
    try {
      select = schema.getStatement(query);
      for (int i = 0; i < ids.length; i++) {
        select.setInt(i + 1, ids[i]);
      }
      rs = select.executeQuery();
      return getInteger(rs);
    } catch (SQLException e) {
      throw new AdminPersistenceException("Table.getInteger",
              SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      DBUtil.close(rs, select);
    }
  }

  protected T getUniqueRow(ResultSet rs) throws SQLException, AdminPersistenceException {
    T result;
    if (!rs.next()) {// no row found
      return null;
    }
    result = fetchRow(rs);
    if (rs.next()) { // more then one row !
      throw new AdminPersistenceException("Table.getUniqueRow", SilverpeasException.ERROR,
              "admin.EX_ERR_NOT_UNIQUE_ROW");
    }
    return result;
  }

  protected List<T> getRows(ResultSet rs) throws SQLException {
    List<T> result = new ArrayList<>();
    while (rs.next()) {
      result.add(fetchRow(rs));
    }
    return result;
  }

  protected List<String> getIds(ResultSet rs) throws SQLException {
    List<String> result = new ArrayList<>();
    while (rs.next()) {
      result.add(String.valueOf(rs.getInt(1)));
    }
    return result;
  }

  protected Integer getInteger(ResultSet rs) throws SQLException, AdminPersistenceException {
    if (!rs.next()) { // no row found
      return null;
    }
    int result = rs.getInt(1);
    if (rs.next()) { // more then one row !
      throw new AdminPersistenceException("Table.getInteger", SilverpeasException.ERROR,
              "admin.EX_ERR_NOT_UNIQUE_ROW");
    }
    return result;
  }

  protected int insertRow(String insertQuery, T row) throws AdminPersistenceException {
    int rowsCount;
    PreparedStatement statement = null;
    try {
      statement = schema.getStatement(insertQuery);
      prepareInsert(insertQuery, statement, row);
      rowsCount = statement.executeUpdate();
      return rowsCount;
    } catch (SQLException e) {
      SynchroDomainReport.error("Table.insertRow()", "Exception SQL : " + e.getMessage(), null);
      throw new AdminPersistenceException("Table.insertRow", SilverpeasException.ERROR,
              "root.EX_RECORD_INSERTION_FAILED", e);
    } finally {
      DBUtil.close(statement);
    }
  }

  protected int updateRow(String updateQuery, T row) throws AdminPersistenceException {
    int rowsCount;
    PreparedStatement statement = null;
    try {
      statement = schema.getStatement(updateQuery);
      prepareUpdate(updateQuery, statement, row);
      rowsCount = statement.executeUpdate();
      return rowsCount;
    } catch (SQLException e) {
      SynchroDomainReport.error("Table.updateRow()", "Exception SQL : " + e.getMessage(), null);
      throw new AdminPersistenceException("Table.updateRow",
              SilverpeasException.ERROR, "admin.EX_ERR_UPDATE", e);
    } finally {
      DBUtil.close(statement);
    }
  }

  protected int updateRelation(String query) throws AdminPersistenceException {
    int rowsCount;
    PreparedStatement statement = null;
    try {
      statement = schema.getStatement(query);
      rowsCount = statement.executeUpdate();
      return rowsCount;
    } catch (SQLException e) {
      throw new AdminPersistenceException("Table.updateRelation", SilverpeasException.ERROR,
              "admin.EX_ERR_UPDATE", e);
    } finally {
      DBUtil.close(statement);
    }
  }

  protected int updateRelation(String query, int param) throws AdminPersistenceException {
    int rowsCount;
    PreparedStatement statement = null;
    try {
      statement = schema.getStatement(query);
      if (param == -1) {
        statement.setNull(1, Types.INTEGER);
      } else {
        statement.setInt(1, param);
      }
      rowsCount = statement.executeUpdate();
      return rowsCount;
    } catch (SQLException e) {
      SynchroDomainReport.error("Table.updateRelation()", "Exception SQL : " + e.getMessage(), null);
      throw new AdminPersistenceException("Table.updateRelation", SilverpeasException.ERROR,
              "admin.EX_ERR_UPDATE", e);
    } finally {
      DBUtil.close(statement);
    }
  }

  protected int updateRelation(String query, int[] ids) throws AdminPersistenceException {
    int rowsCount;
    PreparedStatement statement = null;
    try {
      statement = schema.getStatement(query);
      for (int i = 0; i < ids.length; i++) {
        if (ids[i] == -1) {
          statement.setNull(i + 1, Types.INTEGER);
        } else {
          statement.setInt(i + 1, ids[i]);
        }
      }
      rowsCount = statement.executeUpdate();
      return rowsCount;
    } catch (SQLException e) {
      SynchroDomainReport.error("Table.updateRelation()", "Exception SQL : " + e.getMessage(), null);
      throw new AdminPersistenceException("Table.updateRelation", SilverpeasException.ERROR,
              "admin.EX_ERR_UPDATE", e);
    } finally {
      DBUtil.close(statement);
    }
  }
  private Schema schema = null;
  private String tableName = null;

  protected boolean addParamToQuery(Collection<Object> theVect, StringBuilder theQuery, String value,
          String column, boolean concatAndOr, String andOr) {
    if ((value != null) && (value.length() > 0)) {
      return addParamToQuery(theVect, theQuery, (Object) value, column, concatAndOr, andOr);
    }
    return concatAndOr;
  }

  protected boolean addParamToQuery(Collection<Object> theVect, StringBuilder theQuery,
      Timestamp value, String column, boolean concatAndOr, String andOr) {
    if (value != null) {
      return addParamToQuery(theVect, theQuery, (Object) value, column, concatAndOr, andOr);
    }
    return concatAndOr;
  }

  protected boolean addParamToQuery(Collection<Object> theVect, StringBuilder theQuery, int value,
      String column, boolean concatAndOr, String andOr) {
    return addParamToQuery(theVect, theQuery, (Object) value, column, concatAndOr, andOr);
  }

  protected boolean addIdToQuery(Collection<Object> theVect, StringBuilder theQuery, int value,
          String column, boolean concatAndOr, String andOr) {
    if (value != -2) {
      return addParamToQuery(theVect, theQuery, value, column, concatAndOr, andOr);
    }
    return concatAndOr;
  }

  private boolean addParamToQuery(Collection<Object> theVect, StringBuilder theQuery, Object value,
      String column, boolean concatAndOr, String andOr) {
    if (concatAndOr) {
      theQuery.append(andOr);
    } else {
      theQuery.append(" where (");
    }
    if (value instanceof String) {
      theQuery.append("LOWER(").append(column).append(")" + " LIKE LOWER(?)");
    } else {
      theQuery.append(column).append(" = ?");
    }
    theVect.add(value);
    return true;
  }
}