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

package org.silverpeas.core.persistence.jdbc;

import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * A Table object manages a table in a database.
 *
 * Title:        Database object generator
 * Description:  Enable automatic generation of database objects.
 * Support :
 *   - creation, modification, deletion of a record.
 *   - cascading deletion of records.
 * Copyright:    Copyright (c) 2001
 * Company:      Stratelia
 * @author Eric BURGEL
 * @version 1.0
 */
@Transactional(Transactional.TxType.MANDATORY)
public abstract class AbstractTable<T> {

  private String tableName = null;

  protected AbstractTable(String tableName) {
    this.tableName = tableName;
  }

  /**
   * Truncates a string value to be inserted in a fixed size column
   */
  protected String truncate(String value, int maxSize) {
    if (value != null && value.length() > maxSize) {
      return value.substring(0, maxSize - 1);
    } else
      return value;
  }

  /**
   * Returns the next id which can be used to create a new row.
   */
  public int getNextId() throws SQLException {
    int nextId = DBUtil.getNextId(tableName, "id");

    if (nextId == 0) {
      return 1;
    }
    return nextId;
  }

  /**
   * Builds a new row object which values are retrieved from the given ResultSet.
   */
  protected abstract T fetchRow(ResultSet rs) throws SQLException;

  /**
   * Set all the parameters of the insert PreparedStatement built from the insertQuery in order to
   * insert the given row.
   */
  protected abstract void prepareInsert(String insertQuery, PreparedStatement insert, T row)
      throws SQLException;

  /**
   * Set all the parameters of the update PreparedStatement built from the updateQuery in order to
   * update the given row.
   */
  protected abstract void prepareUpdate(String updateQuery, PreparedStatement update, T row)
      throws SQLException;

  /**
   * Returns the unique row referenced by the given query and id. Returns null if no rows match the
   * id. Throws a {@link SQLException} if more then one row match the id.
   * @param query the sql query string must be like "select * from ... where ... id=?" where id is
   * an int column.
   * @param id references an unique row.
   * @return the required row or null.
   * @throws SQLException if an error occurs while getting the unique row.
   */
  protected T getUniqueRow(String query, int id) throws SQLException {
    try (Connection connection = DBUtil.openConnection();
         PreparedStatement select = connection.prepareStatement(query)) {
        select.setInt(1, id);
      try (ResultSet rs = select.executeQuery()) {
        return getUniqueRow(rs);
      }
    }
  }

  /**
   * Returns the unique row referenced by the given query and String parameter. Returns null if no
   * rows match the id. Throws a {@link SQLException} if more then one row match the id.
   * @param query the sql query string must be like "select * from ... where ... col=?" where col is
   * a text column.
   * @param parameter references an unique row.
   * @return the required row or null.
   * @throws SQLException if an error occurs while getting the unique row.
   */
  protected T getUniqueRow(String query, String parameter) throws SQLException {
    try (Connection connection = DBUtil.openConnection();
         PreparedStatement select = connection.prepareStatement(query)) {
      select.setString(1, parameter);
      try (ResultSet rs = select.executeQuery()) {
        return getUniqueRow(rs);
      }
    }
  }

  /**
   * Returns the unique row referenced by the given query and int[] ids. Returns null if no rows
   * match the id. Throws a {@link SQLException} if more then one row match the id.
   * @param query the sql query string must be like "select * from ... where ... col1=? ... coln=?"
   * @param ids references an unique row.
   * @return the required row or null.
   * where the col are int columns.
   * @throws SQLException if an error occurs while getting the unique row.
   */
  protected T getUniqueRow(String query, int[] ids) throws SQLException {
    try (Connection connection = DBUtil.openConnection();
         PreparedStatement select = connection.prepareStatement(query)) {
      for (int i = 0; i < ids.length; i++) {
        select.setInt(i + 1, ids[i]);
      }
      try (ResultSet rs = select.executeQuery()) {
        return getUniqueRow(rs);
      }
    }
  }

  /**
   * Returns the unique row referenced by the given query and String[] params. Returns null if no
   * rows match the id. Throws a {@link SQLException} if more then one row match the id.
   * @return the required row or null.
   * @param query the sql query string must be like "select * from ... where ... col1=? ... coln=?"
   * where the col are int columns.
   * @param params references an unique row.
   * @throws SQLException if an error occurs while getting an unique row.
   */
  protected T getUniqueRow(String query, String[] params) throws SQLException {
    try (Connection connection = DBUtil.openConnection();
         PreparedStatement select = connection.prepareStatement(query)) {
      for (int i = 0; i < params.length; i++) {
        select.setString(i + 1, params[i]);
      }
      try (ResultSet rs = select.executeQuery()) {
        return getUniqueRow(rs);
      }
    }
  }

  /**
   * Returns the unique row referenced by the given query, int[] ids and String[] params. Returns
   * null if no rows match the id. Throws a {@link SQLException} if more then one row match the id.
   * @param query the sql query string must be like "select * from ... where ... col1=? ... coln=?"
   * where the col are int columns.
   * @param ids references an unique row.
   * @param params references an unique row.
   * @return the required row or null.
   * @throws SQLException if an error occurs while getting an unique row.
   */
  protected T getUniqueRow(String query, int[] ids, String[] params) throws SQLException {
    try (Connection connection = DBUtil.openConnection();
         PreparedStatement select = connection.prepareStatement(query)) {
      for (int i = 0; i < ids.length; i++) {
        select.setInt(i + 1, ids[i]);
      }
      for (int j = 0; j < params.length; j++) {
        select.setString(ids.length + j + 1, params[j]);
      }
      try (ResultSet rs = select.executeQuery()) {
        return getUniqueRow(rs);
      }
    }
  }

  /**
   * Returns the unique row referenced by the given query with no parameters. Returns null if no
   * rows match the id. Throws a UtilException if more then one row match the id.
   * @param query the sql query string must be like "select * from ... where ..."
   * @return the required row or null.
   * @throws SQLException if an error occurs while getting an unique row.
   */
  protected T getUniqueRow(String query) throws SQLException {
    try (Connection connection = DBUtil.openConnection();
         PreparedStatement select = connection.prepareStatement(query);
         ResultSet rs = select.executeQuery()) {
        return getUniqueRow(rs);
    }
  }

  /**
   * Returns the rows described by the given no parameters query.
   */
  protected List<T> getRows(String query) throws SQLException {
    try (Connection connection = DBUtil.openConnection();
         PreparedStatement select = connection.prepareStatement(query);
         ResultSet rs = select.executeQuery()) {
      return getRows(rs);
    }
  }

  /**
   * Returns the rows described by the given query with one id parameter.
   */
  protected List<T> getRows(String query, int id) throws SQLException {
    try (Connection connection = DBUtil.openConnection();
         PreparedStatement select = connection.prepareStatement(query)) {
      select.setInt(1, id);
      try (ResultSet rs = select.executeQuery()) {
        return getRows(rs);
      }
    }
  }

  /**
   * Returns the rows described by the given query with one string parameter.
   */
  protected List<T> getRows(String query, String parameter) throws SQLException {
    try (Connection connection = DBUtil.openConnection();
         PreparedStatement select = connection.prepareStatement(query)) {
      select.setString(1, parameter);
      try (ResultSet rs = select.executeQuery()) {
        return getRows(rs);
      }
    }
  }

  /**
   * Returns the rows described by the given query with id parameters.
   */
  protected List<T> getRows(String query, int[] ids) throws SQLException {
    try (Connection connection = DBUtil.openConnection();
         PreparedStatement select = connection.prepareStatement(query)) {
      for (int i = 0; i < ids.length; i++) {
        select.setInt(i + 1, ids[i]);
      }
      try (ResultSet rs = select.executeQuery()) {
        return getRows(rs);
      }
    }
  }

  /**
   * Returns the rows described by the given query and String parameters.
   */
  protected List<T> getRows(String query, String[] params) throws SQLException {
    try (Connection connection = DBUtil.openConnection();
         PreparedStatement select = connection.prepareStatement(query)) {
      for (int i = 0; i < params.length; i++) {
        select.setString(i + 1, params[i]);
      }
      try (ResultSet rs = select.executeQuery()) {
        return getRows(rs);
      }
    }
  }

  /**
   * Returns the rows described by the given query with id and String parameters.
   */
  protected List<T> getRows(String query, int[] ids, String[] params) throws SQLException {
    try (Connection connection = DBUtil.openConnection();
         PreparedStatement select = connection.prepareStatement(query)) {
      for (int i = 0; i < ids.length; i++) {
        select.setInt(i + 1, ids[i]);
      }
      for (int j = 0; j < params.length; j++) {
        select.setString(ids.length + j + 1, params[j]);
      }
      try (ResultSet rs = select.executeQuery()) {
        return getRows(rs);
      }
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
   * @return a list of rows matching the specified columns.
   * @throws SQLException
   */
  protected List<T> getMatchingRows(String returnedColumns, String[] matchColumns,
      String[] matchValues) throws SQLException {
    StringBuilder query =
        new StringBuilder("select ").append(returnedColumns).append(" from ").append(tableName);
    List<String> notNullValues = new ArrayList<>();
    String sep = " where ";
    for (int i = 0; i < matchColumns.length; i++) {
      if (matchValues[i] != null) {
        query.append(sep).append(matchColumns[i]).append(" like ?");
        sep = " , ";
        notNullValues.add(matchValues[i]);
      }
    }
    return getRows(query.toString(), notNullValues.toArray(new String[notNullValues.size()]));
  }

  /**
   * Returns the integer of the single row, single column resultset returned by the given query with
   * id parameters. Returns null if the result set was empty.
   */
  protected Integer getInteger(String query, int[] ids) throws SQLException {
    try (Connection connection = DBUtil.openConnection();
         PreparedStatement select = connection.prepareStatement(query)) {
      for (int i = 0; i < ids.length; i++) {
        select.setInt(i + 1, ids[i]);
      }
      try (ResultSet rs = select.executeQuery()) {
        return getInteger(rs);
      }
    }
  }

  protected T getUniqueRow(ResultSet rs) throws SQLException {
    T result;

    if (!rs.next()) {
      // no row found
      return null;
    }
    result = fetchRow(rs);
    if (rs.next()) {
      // more then one row !
      throw new SQLException("Not unique row!");
    }
    return result;
  }

  protected ArrayList<T> getRows(ResultSet rs) throws SQLException {
    ArrayList<T> result = new ArrayList<>();
    while (rs.next()) {
      result.add(fetchRow(rs));
    }
    return result;
  }

  protected Integer getInteger(ResultSet rs) throws SQLException {
    int result;
    if (!rs.next()) {
      // no row found
      return null;
    }
    result = rs.getInt(1);
    if (rs.next()) {
      // more then one row !
      throw new SQLException("Not unique row !");
    }
    return result;
  }

  protected int insertRow(String insertQuery, T row) throws SQLException {
    try (Connection connection = DBUtil.openConnection();
         PreparedStatement statement = connection.prepareStatement(insertQuery)) {
        prepareInsert(insertQuery, statement, row);
      return statement.executeUpdate();
    }
  }

  protected int updateRow(String updateQuery, T row) throws SQLException {
    try (Connection connection = DBUtil.openConnection();
         PreparedStatement statement = connection.prepareStatement(updateQuery)) {
      prepareUpdate(updateQuery, statement, row);
      return statement.executeUpdate();
    }
  }

  protected int updateRelation(String query, int param) throws SQLException {
    try (Connection connection = DBUtil.openConnection();
         PreparedStatement statement = connection.prepareStatement(query)) {
      if (param == -1) {
        statement.setNull(1, Types.INTEGER);
      } else {
        statement.setInt(1, param);
      }
      return statement.executeUpdate();
    }
  }

  protected int updateRelation(String query, int[] param) throws SQLException {
    try (Connection connection = DBUtil.openConnection();
         PreparedStatement statement = connection.prepareStatement(query)) {
      for (int i = 0; i < param.length; i++) {
        if (param[i] == -1) {
          statement.setNull(i + 1, Types.INTEGER);
        } else {
          statement.setInt(i + 1, param[i]);
        }
      }
      return statement.executeUpdate();
    }
  }
}
