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

package com.stratelia.webactiv.organization;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.SynchroReport;
import com.stratelia.webactiv.util.Schema;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import java.util.Collection;

/**
 * A Table object manages a table in a database.
 */
public abstract class Table {
  public Table(Schema schema, String tableName) {
    this.schema = schema;
    this.tableName = tableName;
  }

  static String getNotNullString(String sn) {
    if ((sn == null) || (sn.equalsIgnoreCase("null"))) {
      return "";
    } else {
      return sn;
    }
  }

  /**
   * Builds an aliased columns list from a row alias and a columns list. Returns
   * "u.id,u.firstName,u.lastName" for row alias "u" columns list "id,firstName,lastName".
   */
  static public String aliasColumns(String rowAlias, String columnList) {
    StringBuffer result = new StringBuffer();
    StringTokenizer st = new StringTokenizer(columnList, ",");

    String column;
    String separator = "";
    while (st.hasMoreTokens()) {
      column = st.nextToken();
      result.append(separator);
      result.append(rowAlias);
      result.append(".");
      result.append(column);

      separator = ",";
    }

    return result.toString();
  }

  /**
   * Truncates a string value to be inserted in a fixed size column
   */
  static public String truncate(String value, int maxSize) {
    if (value != null && value.length() > maxSize) {
      return value.substring(0, maxSize - 1);
    } else
      return value;
  }

  /**
   * Returns the next id which can be used to create a new row.
   */
  public int getNextId() throws SQLException {
    int nextId = 0;

    try {
      nextId = com.stratelia.webactiv.util.DBUtil.getNextId(tableName, "id");
    } catch (Exception e) {
      throw new SQLException(e.toString());
    }

    if (nextId == 0)
      return 1;
    else
      return nextId;
  }

  /**
   * Builds a new row object which values are retrieved from the given ResultSet.
   */
  abstract protected Object fetchRow(ResultSet rs) throws SQLException;

  /**
   * Set all the parameters of the insert PreparedStatement built from the insertQuery in order to
   * insert the given row.
   */
  abstract protected void prepareInsert(String insertQuery,
      PreparedStatement insert, Object row) throws SQLException;

  /**
   * Set all the parameters of the update PreparedStatement built from the updateQuery in order to
   * update the given row.
   */
  abstract protected void prepareUpdate(String updateQuery,
      PreparedStatement update, Object row) throws SQLException;

  /**
   * Returns the unique row referenced by the given query and id. Returns null if no rows match the
   * id. Throws a AdminPersistenceException if more then one row match the id.
   * @return the requiered row or null.
   * @param query the sql query string must be like "select * from ... where ... id=?" where id is
   * an int column.
   * @param id references an unique row.
   */
  protected Object getUniqueRow(String query, int id)
      throws AdminPersistenceException {
    ResultSet rs = null;

    PreparedStatement select = null;
    try {
      SilverTrace.debug("admin", "Table.getUniqueRow", "root.MSG_QUERY", query
          + "  id: " + id);
      select = schema.getStatement(query);
      // synchronized (select)
      // {
      select.setInt(1, id);
      rs = select.executeQuery();
      return getUniqueRow(rs);
      // }
    } catch (SQLException e) {
      throw new AdminPersistenceException("Table.getUniqueRow",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      schema.releaseAll(rs, select);
    }
  }

  /**
   * Returns the unique row referenced by the given query and id. Returns null if no rows match the
   * id. Throws a AdminPersistenceException if more then one row match the id.
   * @return the requiered row or null.
   * @param query the sql query string must be like "select * from ... where ... id=?" where id is
   * an String column.
   * @param id references an unique row.
   */
  protected Object getUniqueRow(String query, String id)
      throws AdminPersistenceException {
    ResultSet rs = null;

    PreparedStatement select = null;
    try {
      SilverTrace.debug("admin", "Table.getUniqueRow", "root.MSG_QUERY", query
          + "  id String: " + id);
      select = schema.getStatement(query);
      // synchronized (select)
      // {
      select.setString(1, id);
      rs = select.executeQuery();
      return getUniqueRow(rs);
      // }
    } catch (SQLException e) {
      throw new AdminPersistenceException("Table.getUniqueRow",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      schema.releaseAll(rs, select);
    }
  }

  /**
   * Returns the ids described by the given no parameters query.
   */
  protected ArrayList<String> getIds(String query) throws AdminPersistenceException {
    ResultSet rs = null;
    PreparedStatement select = null;
    try {
      SilverTrace.debug("admin", "Table.getIds", "root.MSG_QUERY", query);
      select = schema.getStatement(query);
      // synchronized (select)
      // {
      rs = select.executeQuery();
      return getIds(rs);
      // }
    } catch (SQLException e) {
      throw new AdminPersistenceException("Table.getIds",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      schema.releaseAll(rs, select);
    }
  }

  /**
   * Returns the ids described by the given query with one id parameter.
   */
  protected ArrayList<String> getIds(String query, int id)
      throws AdminPersistenceException {
    ResultSet rs = null;

    PreparedStatement select = null;
    try {
      SilverTrace.debug("admin", "Table.getIds", "root.MSG_QUERY", query
          + "  id: " + id);
      select = schema.getStatement(query);
      // synchronized (select)
      // {
      select.setInt(1, id);
      rs = select.executeQuery();
      return getIds(rs);
      // }
    } catch (SQLException e) {
      throw new AdminPersistenceException("Table.getIds",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      schema.releaseAll(rs, select);
    }
  }

  /**
   * Returns the ids described by the given query with id parameters.
   */
  protected ArrayList<String> getIds(String query, int[] ids)
      throws AdminPersistenceException {
    ResultSet rs = null;

    PreparedStatement select = null;
    try {
      SilverTrace.debug("admin", "Table.getRows", "root.MSG_QUERY", query
          + "  ids: " + ids);
      select = schema.getStatement(query);
      // synchronized (select)
      // {
      for (int i = 0; i < ids.length; i++) {
        select.setInt(i + 1, ids[i]);
      }
      rs = select.executeQuery();
      return getIds(rs);
      // }
    } catch (SQLException e) {
      throw new AdminPersistenceException("Table.getIds",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      schema.releaseAll(rs, select);
    }
  }

  /**
   * Returns the rows described by the given query with id and String parameters.
   */
  protected ArrayList<String> getIds(String query, int[] ids, String[] params)
      throws AdminPersistenceException {
    ResultSet rs = null;
    PreparedStatement select = null;

    try {
      SilverTrace.debug("admin", "Table.getIds", "root.MSG_QUERY", query
          + "  id[]: " + ids + "   params[]: " + params);
      select = schema.getStatement(query);
      // synchronized (select)
      // {
      int i, j;

      for (i = 0; i < ids.length; i++) {
        select.setInt(i + 1, ids[i]);
      }
      for (j = 0; j < params.length; j++) {
        select.setString(i + j + 1, params[j]);
      }
      rs = select.executeQuery();
      return getIds(rs);
      // }
    } catch (SQLException e) {
      throw new AdminPersistenceException("Table.getIds",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      schema.releaseAll(rs, select);
    }
  }

  /**
   * Returns the rows described by the given no parameters query.
   */
  protected ArrayList<? extends Object> getRows(String query) throws AdminPersistenceException {
    ResultSet rs = null;
    PreparedStatement select = null;

    try {
      SilverTrace.debug("admin", "Table.getRows", "root.MSG_QUERY", query);
      select = schema.getStatement(query);
      // synchronized (select)
      // {
      rs = select.executeQuery();
      return getRows(rs);
      // }
    } catch (SQLException e) {
      throw new AdminPersistenceException("Table.getRows",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      schema.releaseAll(rs, select);
    }
  }

  /**
   * Returns the rows described by the given query with one id parameter.
   */
  protected List<? extends Object> getRows(String query, int id)
      throws AdminPersistenceException {
    ResultSet rs = null;
    PreparedStatement select = null;

    try {
      SilverTrace.debug("admin", "Table.getRows", "root.MSG_QUERY", query
          + "  id: " + id);
      select = schema.getStatement(query);
      // synchronized (select)
      // {
      select.setInt(1, id);
      rs = select.executeQuery();
      return getRows(rs);
      // }
    } catch (SQLException e) {
      throw new AdminPersistenceException("Table.getRows",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      schema.releaseAll(rs, select);
    }
  }

  /**
   * Returns the rows described by the given query with id parameters.
   */
  protected ArrayList<? extends Object> getRows(String query, int[] ids)
      throws AdminPersistenceException {
    ResultSet rs = null;
    PreparedStatement select = null;

    try {
      SilverTrace.debug("admin", "Table.getRows", "root.MSG_QUERY", query
          + "  id[]: " + ids);
      select = schema.getStatement(query);
      // synchronized (select)
      // {
      for (int i = 0; i < ids.length; i++) {
        select.setInt(i + 1, ids[i]);
      }
      rs = select.executeQuery();
      return getRows(rs);
      // }
    } catch (SQLException e) {
      throw new AdminPersistenceException("Table.getRows",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      schema.releaseAll(rs, select);
    }
  }

  /**
   * Returns the rows described by the given query and String parameters.
   */
  protected ArrayList<? extends Object> getRows(String query, String[] params)
      throws AdminPersistenceException {
    ResultSet rs = null;
    PreparedStatement select = null;

    try {
      SilverTrace.debug("admin", "Table.getRows", "root.MSG_QUERY", query
          + "  params: " + params);
      select = schema.getStatement(query);
      // synchronized (select)
      // {
      for (int i = 0; i < params.length; i++) {
        select.setString(i + 1, params[i]);
      }
      rs = select.executeQuery();
      return getRows(rs);
      // }
    } catch (SQLException e) {
      throw new AdminPersistenceException("Table.getRows",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      schema.releaseAll(rs, select);
    }
  }

  /**
   * Returns the rows described by the given query with id and String parameters.
   */
  protected ArrayList<? extends Object> getRows(String query, int[] ids, String[] params)
      throws AdminPersistenceException {
    ResultSet rs = null;
    PreparedStatement select = null;

    try {
      SilverTrace.debug("admin", "Table.getRows", "root.MSG_QUERY", query
          + "  id[]: " + ids + "   params[]: " + params);
      select = schema.getStatement(query);
      // synchronized (select)
      // {
      int i, j;

      for (i = 0; i < ids.length; i++) {
        select.setInt(i + 1, ids[i]);
      }
      for (j = 0; j < params.length; j++) {
        select.setString(i + j + 1, params[j]);
      }
      rs = select.executeQuery();
      return getRows(rs);
      // }
    } catch (SQLException e) {
      throw new AdminPersistenceException("Table.getRows",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      schema.releaseAll(rs, select);
    }
  }

  /**
   * Returns the nb of rows in the given table agregated on the given column
   */
  protected int getCount(String tableName, String agregateColumn)
      throws AdminPersistenceException {
    ResultSet rs = null;
    String query = "select count(*) as nbResult from " + tableName;
    PreparedStatement select = null;

    try {
      SilverTrace.debug("admin", "Table.getCount", "root.MSG_QUERY", query);
      select = schema.getStatement(query);
      // synchronized (select)
      // {
      rs = select.executeQuery();
      if (rs.next())
        return rs.getInt(1);
      else
        return 0;
      // }
    } catch (SQLException e) {
      throw new AdminPersistenceException("Table.getCount",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      schema.releaseAll(rs, select);
    }
  }

  /**
   * Returns the nb of rows in the given table agregated on the given column
   */
  protected int getCount(String tableName, String agregateColumn,
      String whereClause, int id) throws AdminPersistenceException {
    ResultSet rs = null;
    String query = "select count(*) as nbResult from " + tableName + " where "
        + whereClause;
    PreparedStatement select = null;

    try {
      SilverTrace.debug("admin", "Table.getCount", "root.MSG_QUERY", query
          + "  id: " + id);
      select = schema.getStatement(query);
      // synchronized (select)
      // {
      select.setInt(1, id);
      rs = select.executeQuery();
      if (rs.next())
        return rs.getInt(1);
      else
        return 0;
      // }
    } catch (SQLException e) {
      throw new AdminPersistenceException("Table.getCount",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      schema.releaseAll(rs, select);
    }
  }

  /**
   * Returns the nb of rows in the given table agregated on the given column
   */
  protected int getCount(String tableName, String agregateColumn,
      String whereClause, String param) throws AdminPersistenceException {
    ResultSet rs = null;
    String query = "select count(*) as nbResult from " + tableName + " where "
        + whereClause;
    PreparedStatement select = null;

    try {
      SilverTrace.debug("admin", "Table.getCount", "root.MSG_QUERY", query
          + " param: " + param);
      select = schema.getStatement(query);
      // synchronized (select)
      // {
      select.setString(1, param);
      rs = select.executeQuery();
      if (rs.next())
        return rs.getInt(1);
      else
        return 0;
      // }
    } catch (SQLException e) {
      throw new AdminPersistenceException("Table.getCount",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", e);
    } catch (Exception e) {
      throw new AdminPersistenceException("Table.getCount",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      schema.releaseAll(rs, select);
    }
  }

  /**
   * Returns the nb of rows in the given table agregated on the given column
   */
  protected int getCount(String tableName, String agregateColumn,
      String whereClause, int id, String param)
      throws AdminPersistenceException {
    ResultSet rs = null;
    String query = "select count(*) as nbResult from " + tableName + " where "
        + whereClause;
    PreparedStatement select = null;

    try {
      SilverTrace.debug("admin", "Table.getCount", "root.MSG_QUERY", query
          + "  id: " + Integer.toString(id) + " param: " + param);
      select = schema.getStatement(query);
      // synchronized (select)
      // {
      select.setInt(1, id);
      select.setString(2, param);
      rs = select.executeQuery();
      if (rs.next())
        return rs.getInt(1);
      else
        return 0;
      // }
    } catch (SQLException e) {
      throw new AdminPersistenceException("Table.getCount",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", e);
    } catch (Exception e) {
      throw new AdminPersistenceException("Table.getCount",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      schema.releaseAll(rs, select);
    }
  }

  /**
   * Returns the nb of rows in the given table agregated on the given column
   */
  protected int getCount(String tableName, String agregateColumn,
      String whereClause, int[] ids) throws AdminPersistenceException {
    ResultSet rs = null;
    String query = "select count(*) as nbResult from " + tableName + " where "
        + whereClause;
    PreparedStatement select = null;

    try {
      SilverTrace.debug("admin", "Table.getCount", "root.MSG_QUERY", query
          + "  id[]: " + ids);
      select = schema.getStatement(query);
      // synchronized (select)
      // {
      for (int i = 0; i < ids.length; i++) {
        select.setInt(i + 1, ids[i]);
      }
      rs = select.executeQuery();
      if (rs.next())
        return rs.getInt(1);
      else
        return 0;
      // }
    } catch (SQLException e) {
      throw new AdminPersistenceException("Table.getCount",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      schema.releaseAll(rs, select);
    }
  }

  /**
   * Returns the nb of rows in the given table agregated on the given column
   */
  protected int getCount(String tableName, String agregateColumn,
      String whereClause, String[] params) throws AdminPersistenceException {
    ResultSet rs = null;
    String query = "select count(*) as nbResult from " + tableName + " where "
        + whereClause;
    PreparedStatement select = null;

    try {
      SilverTrace.debug("admin", "Table.getCount", "root.MSG_QUERY", query
          + "  params[]: " + params);
      select = schema.getStatement(query);
      // synchronized (select)
      // {
      for (int i = 0; i < params.length; i++) {
        select.setString(i + 1, params[i]);
      }
      rs = select.executeQuery();
      if (rs.next())
        return rs.getInt(1);
      else
        return 0;
      // }
    } catch (SQLException e) {
      throw new AdminPersistenceException("Table.getCount",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      schema.releaseAll(rs, select);
    }
  }

  /**
   * Returns the nb of rows in the given table agregated on the given column
   */
  protected int getCount(String tableName, String agregateColumn,
      String whereClause, int[] ids, String[] params)
      throws AdminPersistenceException {
    ResultSet rs = null;
    String query = "select count(*) as nbResult from " + tableName + " where "
        + whereClause;
    PreparedStatement select = null;

    try {
      SilverTrace.debug("admin", "Table.getCount", "root.MSG_QUERY", query
          + "  id[]: " + ids + "   params[]: " + params);
      select = schema.getStatement(query);
      // synchronized (select)
      // {
      int i, j;

      for (i = 0; i < ids.length; i++) {
        select.setInt(i + 1, ids[i]);
      }
      for (j = 0; j < params.length; j++) {
        select.setString(i + j + 1, params[j]);
      }
      rs = select.executeQuery();
      if (rs.next())
        return rs.getInt(1);
      else
        return 0;
      // }
    } catch (SQLException e) {
      throw new AdminPersistenceException("Table.getCount",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      schema.releaseAll(rs, select);
    }
  }

  /**
   * Returns the rows like a sample row. The sample is build from a matchColumns names list and a
   * matchValues list of values. For each matchColumn with a non null matchValue is added a
   * criterium : where matchColumn like 'matchValue' The wildcard caracters %, must be set by the
   * caller : so we can choose and do queries as "login like 'exactlogin'" and queries as
   * "lastName like 'Had%'" or "lastName like '%addo%'". The returned rows are given by the
   * returnedColumns parameter which is of the form 'col1, col2, ..., colN'.
   */
  protected ArrayList<? extends Object> getMatchingRows(String returnedColumns,
      String[] matchColumns, String[] matchValues)
      throws AdminPersistenceException {
    String query = "select " + returnedColumns + " from " + tableName;
    ArrayList<String> notNullValues = new ArrayList<String>();

    String sep = " where ";
    for (int i = 0; i < matchColumns.length; i++) {
      if (matchValues[i] != null) {
        query += sep + matchColumns[i] + " like ?";
        sep = " , ";
        notNullValues.add(matchValues[i]);
      }
    }

    return getRows(query, notNullValues.toArray(new String[0]));
  }

  /**
   * Returns the integer of the single row, single column resultset returned by the given query with
   * id parameters. Returns null if the result set was empty.
   */
  protected Integer getInteger(String query, int[] ids)
      throws AdminPersistenceException {
    ResultSet rs = null;
    PreparedStatement select = null;

    try {
      SilverTrace.debug("admin", "Table.getInteger", "root.MSG_QUERY", query
          + "  ids[]: " + ids);
      select = schema.getStatement(query);
      // synchronized (select)
      // {
      for (int i = 0; i < ids.length; i++) {
        select.setInt(i + 1, ids[i]);
      }
      rs = select.executeQuery();
      return getInteger(rs);
      // }
    } catch (SQLException e) {
      throw new AdminPersistenceException("Table.getInteger",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      schema.releaseAll(rs, select);
    }
  }

  protected Object getUniqueRow(ResultSet rs) throws SQLException,
      AdminPersistenceException {
    Object result = null;

    if (!rs.next()) {
      // no row found
      return null;
    }
    result = fetchRow(rs);
    if (rs.next()) {
      // more then one row !
      throw new AdminPersistenceException("Table.getUniqueRow",
          SilverpeasException.ERROR, "admin.EX_ERR_NOT_UNIQUE_ROW");
    }
    return result;
  }

  protected ArrayList<? extends Object> getRows(ResultSet rs) throws SQLException {
    ArrayList<Object> result = new ArrayList<Object>();

    while (rs.next()) {
      result.add(fetchRow(rs));
    }
    return result;
  }

  protected ArrayList<String> getIds(ResultSet rs) throws SQLException {
    ArrayList<String> result = new ArrayList<String>();

    while (rs.next()) {
      result.add(String.valueOf(rs.getInt(1)));
    }
    return result;
  }

  protected Integer getInteger(ResultSet rs) throws SQLException,
      AdminPersistenceException {
    int result;

    if (!rs.next()) {
      // no row found
      return null;
    }
    result = rs.getInt(1);
    if (rs.next()) {
      // more then one row !
      throw new AdminPersistenceException("Table.getInteger",
          SilverpeasException.ERROR, "admin.EX_ERR_NOT_UNIQUE_ROW");
    }
    return new Integer(result);
  }

  protected int insertRow(String insertQuery, Object row)
      throws AdminPersistenceException {
    int rowsCount = 0;
    PreparedStatement statement = null;

    try {
      statement = schema.getStatement(insertQuery);
      // synchronized (statement)
      // {
      prepareInsert(insertQuery, statement, row);
      rowsCount = statement.executeUpdate();
      // }
      return rowsCount;
    } catch (SQLException e) {
      SynchroReport.error("Table.insertRow()", "Exception SQL : "
          + e.getMessage(), null);
      throw new AdminPersistenceException("Table.insertRow",
          SilverpeasException.ERROR, "root.EX_RECORD_INSERTION_FAILED", e);
    } finally {
      schema.releaseStatement(statement);
    }
  }

  protected int updateRow(String updateQuery, Object row)
      throws AdminPersistenceException {
    int rowsCount = 0;
    PreparedStatement statement = null;

    try {
      statement = schema.getStatement(updateQuery);
      // synchronized (statement)
      // {
      prepareUpdate(updateQuery, statement, row);
      rowsCount = statement.executeUpdate();
      // }
      return rowsCount;
    } catch (SQLException e) {
      SynchroReport.error("Table.updateRow()", "Exception SQL : "
          + e.getMessage(), null);
      throw new AdminPersistenceException("Table.updateRow",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE", e);
    } finally {
      schema.releaseStatement(statement);
    }
  }

  protected int updateRelation(String query) throws AdminPersistenceException {
    int rowsCount = 0;
    PreparedStatement statement = null;

    try {
      statement = schema.getStatement(query);
      // synchronized (statement)
      // {
      rowsCount = statement.executeUpdate();
      // }
      return rowsCount;
    } catch (SQLException e) {
      throw new AdminPersistenceException("Table.updateRelation",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE", e);
    } finally {
      schema.releaseStatement(statement);
    }
  }

  protected int updateRelation(String query, int param)
      throws AdminPersistenceException {
    int rowsCount = 0;
    PreparedStatement statement = null;

    try {
      statement = schema.getStatement(query);
      // synchronized (statement)
      // {
      if (param == -1)
        statement.setNull(1, Types.INTEGER);
      else
        statement.setInt(1, param);
      rowsCount = statement.executeUpdate();
      // }
      return rowsCount;
    } catch (SQLException e) {
      SynchroReport.error("Table.updateRelation()", "Exception SQL : "
          + e.getMessage(), null);
      throw new AdminPersistenceException("Table.updateRelation",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE", e);
    } finally {
      schema.releaseStatement(statement);
    }
  }

  protected int updateRelation(String query, int[] ids)
      throws AdminPersistenceException {
    int rowsCount = 0;
    PreparedStatement statement = null;

    try {
      statement = schema.getStatement(query);
      // synchronized (statement)
      // {
      for (int i = 0; i < ids.length; i++) {
        if (ids[i] == -1)
          statement.setNull(i + 1, Types.INTEGER);
        else
          statement.setInt(i + 1, ids[i]);
      }
      rowsCount = statement.executeUpdate();
      // }
      return rowsCount;
    } catch (SQLException e) {
      SynchroReport.error("Table.updateRelation()", "Exception SQL : "
          + e.getMessage(), null);
      throw new AdminPersistenceException("Table.updateRelation",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE", e);
    } finally {
      schema.releaseStatement(statement);
    }
  }

  private Schema schema = null;
  private String tableName = null;

  protected boolean addParamToQuery(Collection<String> theVect, StringBuffer theQuery,
      String value, String column, boolean concatAndOr, String andOr) {
    boolean valret = concatAndOr;

    if ((value != null) && (value.length() > 0)) {
      if (concatAndOr) {
        theQuery.append(andOr);
      } else {
        theQuery.append(" where (");
        valret = true;
      }
      theQuery.append("LOWER(" + column + ")" + " LIKE LOWER(?)");
      theVect.add(value);
    }
    return valret;
  }

  protected boolean addIdToQuery(Collection<Integer> theVect, StringBuffer theQuery,
      int value, String column, boolean concatAndOr, String andOr) {
    boolean valret = concatAndOr;

    if (value != -2) {
      if (concatAndOr) {
        theQuery.append(andOr);
      } else {
        theQuery.append(" where (");
        valret = true;
      }
      theQuery.append(column + " = ?");
      theVect.add(new Integer(value));
    }
    return valret;
  }
}
