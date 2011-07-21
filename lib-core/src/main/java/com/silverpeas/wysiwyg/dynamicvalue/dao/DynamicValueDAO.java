/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

package com.silverpeas.wysiwyg.dynamicvalue.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.silverpeas.util.ConfigurationControl;
import org.apache.commons.lang.StringUtils;

import com.silverpeas.wysiwyg.dynamicvalue.exception.PropertyNotFoundRuntimeException;
import com.silverpeas.wysiwyg.dynamicvalue.model.DynamicValue;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * data access object layer, this class allows executing operation on the table which contains the
 * dynamic values
 */
public class DynamicValueDAO {

  /**
   * name of the table which contains data
   */
  private static String tableName = null;
  /**
   * name of the database columns which contains key value
   */
  private static String keyColumnName = null;

  /**
   * name of the database columns which contains value corresponding to the key
   */
  private static String valueColumnName = null;

  /**
   * name of the database columns which contains validity start date
   */
  private static String startDateColumnName = null;

  /**
   * name of the database columns which contains validity end date
   */
  private static String endDateColumnName = null;

  static {
    initTableInfos();
  }

  /**
   * gets a dynamic value object by his ID. Only the value with a valid start and end date will be
   * returned
   * @param conn connection object to access database
   * @param id DynamicValue object identifier
   * @return a DynamicValue object
   * @throws SQLException whether a SQl error occurred during the process
   */
  public static DynamicValue getValidDynamicValue(Connection conn, String id) throws SQLException {
    // check if information on table are correctly initialized
    checkTableInfos();
    // init local variables
    DynamicValue value = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    // execute the query to database to get a dynamic value by his Id
    try {
      String query =
          "SELECT " + keyColumnName + "," + valueColumnName + "," + startDateColumnName + "," +
          endDateColumnName + " FROM " + tableName + " WHERE (" + endDateColumnName + " >= ? " +
          " OR " + endDateColumnName + " IS NULL )  AND (" + startDateColumnName +
          " <= ?  OR " + startDateColumnName + " IS NULL )  AND " +
          keyColumnName + " = ?";
      pst = conn.prepareStatement(query);

      // today date
      java.sql.Date sqlToday = getTodayDate();
      pst.setString(3, id);
      pst.setDate(1, sqlToday);
      pst.setDate(2, sqlToday);

      rs = pst.executeQuery();

      if (rs.next()) {
        value =
            new DynamicValue(rs.getString(keyColumnName), rs.getString(valueColumnName), rs
            .getDate(startDateColumnName), rs.getDate(endDateColumnName));
      }
    } finally {
      DBUtil.close(rs, pst);
    }
    return value;

  }

  /**
   * gets a list of DynamicValue object. Only the value with a valid start and end date will be
   * returned
   * @param conn connection object to access database
   * @return a list of DynamicValue object
   * @throws SQLException whether a SQl error occurred during the process
   */
  public static List<DynamicValue> getAllValidDynamicValue(Connection conn) throws SQLException {
    checkTableInfos();
    // initializes local variables
    ArrayList<DynamicValue> values = new ArrayList<DynamicValue>();
    PreparedStatement pst = null;
    ResultSet rs = null;

    try {
      // building query
      String query =
          "SELECT " + keyColumnName + "," + valueColumnName + "," + startDateColumnName + "," +
          endDateColumnName + " FROM " + tableName + " WHERE (" + endDateColumnName + " >= ? " +
          " OR " + endDateColumnName + " IS NULL ) AND (" + startDateColumnName +
          " <= ?  OR " + startDateColumnName + " IS NULL ) ORDER BY " + keyColumnName;

      pst = conn.prepareStatement(query);

      Date currentDate = getTodayDate();
      pst.setDate(1, currentDate);
      pst.setDate(2, currentDate);

      // executes query
      rs = pst.executeQuery();

      // loading DynamicValue object in container of results
      while (rs.next()) {
        DynamicValue value =
            new DynamicValue(rs.getString(keyColumnName), rs.getString(valueColumnName), rs
            .getDate(startDateColumnName), rs.getDate(endDateColumnName));
        values.add(value);

      }
    } finally {
      DBUtil.close(rs, pst);
    }
    return values;

  }

  /**
   * realizes a search by criterion.Only the value with a valid start and end date will be returned.
   * The search is realized on key column
   * @param conn connection object to access database
   * @param criterion String used to realize the search
   * @return a list of DynamicValue object corresponding to the search criterion
   * @throws SQLException whether a SQl error occurred during the process
   */
  public static List<DynamicValue> searchValidDynamicValue(Connection conn, String criterion)
      throws SQLException {
    checkTableInfos();
    // initializes local variables
    ArrayList<DynamicValue> values = new ArrayList<DynamicValue>();
    PreparedStatement pst = null;
    ResultSet rs = null;

    try {
      // building query
      String query =
          "SELECT " + keyColumnName + "," + valueColumnName + "," + startDateColumnName + "," +
          endDateColumnName + " FROM " + tableName + " WHERE (" + endDateColumnName + " >= ? " +
          " OR " + endDateColumnName + " IS NULL ) AND (" + startDateColumnName +
          " <= ?  OR " + startDateColumnName + " IS NULL ) AND " +
          keyColumnName + " like ? ORDER BY " +
          keyColumnName;

      pst = conn.prepareStatement(query);

      Date currentDate = getTodayDate();
      pst.setDate(1, currentDate);
      pst.setDate(2, currentDate);
      pst.setString(3, "%" + criterion + "%");

      // executes query
      rs = pst.executeQuery();

      // loading DynamicValue object in container of results
      while (rs.next()) {
        DynamicValue value =
            new DynamicValue(rs.getString(keyColumnName), rs.getString(valueColumnName), rs
            .getDate(startDateColumnName), rs.getDate(endDateColumnName));
        values.add(value);

      }
    } finally {
      DBUtil.close(rs, pst);
    }

    return values;

  }

  /**
   * Initializes the information about the table which contains the dynamic values
   */
  private static void initTableInfos() {
    try {
      ResourceBundle bundle = ResourceBundle.getBundle(
          "com.silverpeas.wysiwyg.dynamicvalue.settings.dynamicValueSettings",
          new ConfigurationControl());
      DynamicValueDAO.tableName = bundle.getString("tableName").trim();
      DynamicValueDAO.keyColumnName = bundle.getString("keyColumnName").trim();
      DynamicValueDAO.valueColumnName = bundle.getString("valueColumnName").trim();
      DynamicValueDAO.startDateColumnName = bundle.getString("startDateColumnName").trim();
      DynamicValueDAO.endDateColumnName = bundle.getString("endDateColumnName").trim();
    } catch (Exception e) {

    }
  }

  /**
   * check if the table information has been correctly initialize, if not throws a exception
   * @throws PropertyNotFoundRuntimeException
   */
  private static void checkTableInfos() throws PropertyNotFoundRuntimeException {

    if (StringUtils.isEmpty(tableName) || StringUtils.isEmpty(keyColumnName) ||
        StringUtils.isEmpty(valueColumnName) || StringUtils.isEmpty(startDateColumnName)) {
      throw new PropertyNotFoundRuntimeException("DynamicValueDAO", SilverpeasException.ERROR,
          "wysiwyg.DAO_INITILIZATION_FAILED");
    }

  }

  /**
   * gets the today date in SQL format
   * @return a java.sql.Date Object
   */
  private static java.sql.Date getTodayDate() {
    java.util.Date today = new java.util.Date();
    java.sql.Date sqlToday = new java.sql.Date(today.getTime());
    return sqlToday;
  }

}