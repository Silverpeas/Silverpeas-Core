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

package org.silverpeas.core.web.selection.jdbc;

import org.silverpeas.core.web.selection.SelectionJdbcParams;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.persistence.jdbc.DBUtil;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * JDBC database access.
 * @author Antoine HEDIN
 */
public class JdbcConnectorDAO {

  private Driver driver = null;
  String driverClassName;
  String url;
  String login;
  String password;
  String tableName;

  ArrayList<String[]> data;
  String[] columnsNames;

  public JdbcConnectorDAO(SelectionJdbcParams jdbcParams) {
    driverClassName = jdbcParams.getDriverClassName();
    url = jdbcParams.getUrl();
    login = jdbcParams.getLogin();
    password = jdbcParams.getPassword();
    tableName = jdbcParams.getTableName();
  }

  /**
   * @return The list of columns which compose the table.
   */
  public String[] getColumnsNames() {
    if (columnsNames == null) {
      List<String> columns = new ArrayList<>();

      Connection con = null;
      ResultSet columnsRs = null;
      try {
        Properties info = new Properties();
        info.setProperty("user", login);
        info.setProperty("password", password);
        con = getDriver().connect(url, info);
        DatabaseMetaData dbMetaData = con.getMetaData();

        columnsRs = dbMetaData.getColumns(null, "%", tableName, "%");
        while (columnsRs.next()) {
          String columnName = columnsRs.getString("COLUMN_NAME");
          columns.add(columnName);
        }
      } catch (SQLException e) {
        SilverTrace.warn("selectionPeas", "JdbcConnectorDAO.getColumnsNames()",
            "selectionPeas.MSG_CONNECTION_NOT_STARTED", e);
      } finally {
        closeConnection(con, null, columnsRs);
      }
      columnsNames = columns.toArray(new String[columns.size()]);
    }
    return columnsNames;
  }

  /**
   * @return the table's data, after having loading them if needed.
   */
  public ArrayList<String[]> getData() {
    if (data == null) {
      loadData();
    }
    return data;
  }

  /**
   * Loads the table's data.
   */
  public void loadData() {
    StringBuilder columnNameSb = new StringBuilder(100);
    int columnsNamesCount = columnsNames.length;
    for (final String columnsName : columnsNames) {
      columnNameSb.append(columnsName).append(",");
    }
    final String query =
        "select " + columnNameSb.substring(0, columnNameSb.length() - 1) + " from " + tableName;
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
    try {
      Properties info = new Properties();
      info.setProperty("user", login);
      info.setProperty("password", password);
      con = getDriver().connect(url, info);
      stmt = con.createStatement();
      rs = stmt.executeQuery(query);
      data = new ArrayList<>();
      int i;
      while (rs.next()) {
        i = 0;
        String[] line = new String[columnsNamesCount];
        while (i < columnsNamesCount) {
          line[i] = rs.getString(i + 1);
          i++;
        }
        data.add(line);
      }
    } catch (SQLException e) {
      SilverTrace.warn("selectionPeas", "JdbcConnectorDAO.loadData()",
          "selectionPeas.MSG_DATA_CANNOT_BE_LOADED", e);
    } finally {
      closeConnection(con, stmt, rs);
    }
  }

  /**
   * @param index The index of the searched line.
   * @return The data of the table's line corresponding to the index.
   */
  public String[] getLine(String index) {
    return getData().get(Integer.parseInt(index));
  }

  /**
   * @return The number of lines in the table.
   */
  public int getLineCount() {
    return getData().size();
  }

  /**
   * @return The database connection driver.
   */
  private Driver getDriver() {
    if (driver == null) {
      try {
        driver = (Driver) Class.forName(driverClassName).newInstance();
      } catch (Exception e) {
        SilverTrace.warn("selectionPeas", "JdbcConnectorDAO.getDriver()",
            "selectionPeas.MSG_DRIVER_INIT_FAILED", e);
      }
    }
    return driver;
  }

  /**
   * Closes the connection and its associated elements (statement and result set) if they are
   * defined.
   * @param con The connection.
   * @param stmt The statement.
   * @param rs The result set.
   */
  private void closeConnection(Connection con, Statement stmt, ResultSet rs) {
    DBUtil.close(rs, stmt);
    DBUtil.close(con);
  }

}