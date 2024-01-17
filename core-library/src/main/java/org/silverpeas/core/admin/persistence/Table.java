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
/**
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
* FLOSS exception. You should have received a copy of the text describing
* the FLOSS exception, and it is also available here:
* "https://www.silverpeas.org/legal/floss_exception.html"
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/
package org.silverpeas.core.admin.persistence;

import org.silverpeas.core.persistence.jdbc.AbstractTable;
import org.silverpeas.core.persistence.jdbc.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import static org.silverpeas.core.SilverpeasExceptionMessages.unknown;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
* A Table object manages a table in a database.
*/
public abstract class Table<T> extends AbstractTable<T> {

  private static final String GROUP_EXISTENCE = "select id from ST_Group where id = ?";
  private static final String USER_EXISTENCE = "select id from ST_User where id = ?";

  protected Table(String tableName) {
    super(tableName);
  }

  protected String getNotNullString(String sn) {
    if (!isDefined(sn)) {
      return "";
    }
    return sn;
  }

  /**
   * Builds an aliased columns list from a row alias and a columns list. Returns
   * "u.id,u.firstName,u.lastName" for row alias "u" columns list "id,firstName,lastName".
   * @param rowAlias
   * @param columnList
   * @return
   */
  protected static String aliasColumns(String rowAlias, String columnList) {
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

  protected void checkUserExistence(final int userId) throws SQLException {
    try (Connection connection = DBUtil.openConnection();
         PreparedStatement statement = connection.prepareStatement(USER_EXISTENCE)) {
      statement.setInt(1, userId);
      try (ResultSet rs = statement.executeQuery()) {
        if (!rs.next()) {
          throw new SQLException(unknown("user", String.valueOf(userId)));
        }
      }
    }
  }

  protected void checkGroupExistence(int groupId) throws SQLException {
    try (Connection connection = DBUtil.openConnection();
         PreparedStatement statement = connection.prepareStatement(GROUP_EXISTENCE)) {
      statement.setInt(1, groupId);
      try (ResultSet rs = statement.executeQuery()) {
        if (!rs.next()) {
          throw new SQLException(unknown("group", String.valueOf(groupId)));
        }
      }
    }
  }

  /**
   * Returns the ids described by the given no parameters query.
   * @param query
   * @return
   * @throws SQLException
   */
  protected List<String> getIds(String query) throws SQLException {
    return getIds(query, null);
  }

  /**
   * Returns the ids described by the given query with one id parameter.
   * @param query
   * @param id
   * @return
   * @throws SQLException
   */
  protected List<String> getIds(String query, int id) throws SQLException {
    return getIds(query, Collections.singletonList(id));
  }

  /**
   * Returns the rows described by the given query with parameters.
   * @param query
   * @param params
   * @return
   * @throws SQLException
   */
  protected List<String> getIds(String query, List<?> params) throws SQLException {
    try (Connection connection = DBUtil.openConnection();
         PreparedStatement select = connection.prepareStatement(query)) {
      performPrepareStatementParams(select, params);
      try (ResultSet rs = select.executeQuery()) {
        return getIds(rs);
      }
    }
  }

  /**
   * Returns the rows described by the given query with parameters.
   * @param query
   * @param params
   * @return
   * @throws SQLException
   */
  protected List<T> getRows(String query, List<?> params) throws SQLException {
    try (Connection connection = DBUtil.openConnection();
         PreparedStatement select = connection.prepareStatement(query)) {
      performPrepareStatementParams(select, params);
      try (ResultSet rs = select.executeQuery()) {
        return getRows(rs);
      }
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
   * Returns the nb of rows in the given table aggregated on the given column
   * @param tableName
   * @param whereClause
   * @param param
   * @return
   * @throws SQLException
   */
  protected int getCount(String tableName, String whereClause, String param) throws SQLException {
    String query = MessageFormat.format("select count(*) as nbResult from {0} where {1}", tableName,
        whereClause);
    try (Connection connection = DBUtil.openConnection();
         PreparedStatement select = connection.prepareStatement(query)) {
      select.setString(1, param);
      try (ResultSet rs = select.executeQuery()) {
        if (rs.next()) {
          return rs.getInt(1);
        }
      }
      return 0;
    }
  }

  /**
   * Returns the nb of rows in the given table aggregated on the given column
   * @param tableName
   * @param whereClause
   * @param id
   * @param param
   * @return
   * @throws SQLException
   */
  protected int getCount(String tableName, String whereClause, int id, String param)
      throws SQLException {
    String query = MessageFormat.format("select count(*) as nbResult from {0} where {1}", tableName,
        whereClause);
    try (Connection connection = DBUtil.openConnection();
         PreparedStatement select = connection.prepareStatement(query)) {
      select.setInt(1, id);
      select.setString(2, param);
      try (ResultSet rs = select.executeQuery()) {
        if (rs.next()) {
          return rs.getInt(1);
        }
      }
      return 0;
    }
  }

  protected List<String> getIds(ResultSet rs) throws SQLException {
    List<String> result = new ArrayList<>();
    while (rs.next()) {
      result.add(String.valueOf(rs.getInt(1)));
    }
    return result;
  }

  protected int updateRelation(String query) throws SQLException {
    try (Connection connection = DBUtil.openConnection();
         PreparedStatement statement = connection.prepareStatement(query)) {
      return statement.executeUpdate();
    }
  }

  protected boolean addParamToQuery(Collection<Object> theVect, StringBuilder theQuery, String value,
          String column, boolean concatAndOr, String andOr) {
    if ((value != null) && (value.length() > 0)) {
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