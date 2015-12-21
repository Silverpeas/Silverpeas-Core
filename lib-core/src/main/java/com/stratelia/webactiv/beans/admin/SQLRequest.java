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

package com.stratelia.webactiv.beans.admin;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.util.DBUtil;
import org.silverpeas.util.ResourceLocator;
import org.silverpeas.util.SettingBundle;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * The <code> SQLRequest </code> class allows a component to get a SQL request in order by the
 * server type (SQLServer, Oracle, PostgreSQL,...). The role of this class is to read a specific
 * property file (create, delete, update, insert), to extract the specific SQL code with specific
 * tag. And to return the complet rigth SQL code.
 */
public class SQLRequest {

  /**
   * Contains the name of the database supported by SilverPeas
   */
  private ArrayList<String> dbName = new ArrayList<String>();

  /**
   * the name of the client database
   */
  private static String dbServerName;

  /**
   * the name of the path where the system can found the property file
   */
  private String fullPathName;

  /**
   * Contains the value of the interne tag which must be replaced.
   */
  Hashtable<String, String> internalTag = new Hashtable<String, String>();

  /**
   * Contains all queries to create multiple tables for one SilverPeas component or to delete rows
   * or to insert data ...
   */
  private String allQueries;

  /**
   * The setting bundle containing the SQL command to create,
   * delete, insert or update data.
   */
  private SettingBundle sqlRequests;

  // constructor
  public SQLRequest(String fullPathName, String dbServerName) {

    init();

    // Manage the type of the client database
    // Warning, files must be exist
    if (dbName.contains(dbServerName)) {
      SQLRequest.dbServerName = dbServerName;
    } else {
      SQLRequest.dbServerName = "default";
    }
    this.fullPathName = fullPathName;
  }

  public SQLRequest(String fullPathName) {
    this(fullPathName, "default");
  }

  public SQLRequest() {
    this("org.silverpeas.node", "default");
  }

  //
  // protected methods
  //

  /**
   * Set create SQL file into the allQueries String
   */
  protected void setCreateQueries() {
    this.sqlRequests =
        ResourceLocator.getSettingBundle(fullPathName + "." + dbServerName + "_create");
    this.allQueries = this.sqlRequests.getString("cle");
  }

  /**
   * Set delete SQL file into the allQueries String
   */
  protected void setDeleteQueries() {
    this.sqlRequests =
        ResourceLocator.getSettingBundle(fullPathName + "." + dbServerName + "_delete");
    this.allQueries = this.sqlRequests.getString("cle");
  }

  /**
   * Set insert SQL file into the allQueries String
   */
  protected void setInsertQueries() {
    this.sqlRequests =
        ResourceLocator.getSettingBundle(fullPathName + "." + dbServerName + "_insert");
    this.allQueries = this.sqlRequests.getString("cle");
  }

  /**
   * Set update SQL file into the allQueries String
   */
  protected void setUpdateQueries() {
    this.sqlRequests =
        ResourceLocator.getSettingBundle(fullPathName + "." + dbServerName + "_update");
    this.allQueries = this.sqlRequests.getString("cle");
  }

  /**
   * Returns the complete SQL query which allows the administrator to create a table.
   * @param tableName (String) the name of the created table
   * @param tableType (String) the type of the table which must be created (forum, rights, message,
   * event, ...)
   * @return the complete SQL query to create a table
   */
  protected String getCreateQuery(String tableName, String tableType) {
    // set the value for the tableName tag
    internalTag.put("__tableName__", tableName);

    return substituteInternalTag(getSubQuery(tableType));
  }

  /**
   * Returns the complete SQL query which allows the administrator to delete some rows into table.
   * @param instanceId (String) the name of the created table
   * @param tableType (String) the type of the table which must be created (forum, rights, message,
   * event, ...)
   * @return the complete SQL query to delete a table
   */
  protected String getDeleteQuery(String instanceId, String tableType) {
    // set the value for the tableName tag
    internalTag.put("__ID__", instanceId);

    return substituteInternalTag(getSubQuery(tableType));
  }

  /**
   * Returns the complete SQL query which allows the administrator to update a table.
   * @param instanceId (String) the id of the component where their data must be updated into
   * tables.
   * @param tableType (String) the type of the table which must be created (forum, rights, message,
   * event, ...)
   * @return the complete SQL query to update a table
   */
  protected String getUpdateQuery(String instanceId, String tableType) {
    // set the value for the tableName tag
    internalTag.put("__ID__", instanceId);

    return substituteInternalTag(getSubQuery(tableType));
  }

  /**
   * Returns the complete SQL query which allows the administrator to insert data into tables.
   * @param instanceId (String) the id of the component where their data must be inserted into
   * tables.
   * @param tableType (String) the type of the table which must be created (forum, rights, message,
   * event, ...)
   * @return the complete SQL query to insert a table
   */
  protected String getInsertQuery(String instanceId, String tableType) {
    // set the value for the tableName tag
    internalTag.put("__ID__", instanceId);

    return substituteInternalTag(getSubQuery(tableType));
  }

  //
  // private methods
  //

  /**
   * Initialise the internal data.
   */
  private void init() {
    dbName.add("oracle");
    dbName.add("postgreSQL");
    dbName.add("SQLserver");
    internalTag.put("__TextFieldLength__", String
        .valueOf(DBUtil.getTextFieldLength()));
    internalTag
        .put("__TextAreaLength__", String.valueOf(DBUtil.getTextAreaLength()));
    internalTag.put("__DateFieldLength__", String
        .valueOf(DBUtil.getDateFieldLength()));
    internalTag
        .put("__TextMaxiLength__", String.valueOf(DBUtil.getTextMaxiLength()));
  }

  /**
   * Substitute all intern tag of the file. These tags ( __tableName__, ... ) will be replaced by
   * values received or read into the DBUtil.
   * @param localQuery
   * @return the complete request to allows the creation of a table.
   */
  private String substituteInternalTag(String localQuery) {

    // prepare the substitution
    String queryBeforeTag = "";
    String queryAfterTag = "";

    // for each element tag, we replace them by their values
    for (Enumeration<String> e = internalTag.keys(); e.hasMoreElements();) {
      String currentTag = e.nextElement();
      int posTag = localQuery.indexOf(currentTag);
      while (posTag != -1) {
        // while we found the element tag, we replace it
        int endPosition = posTag + currentTag.length();
        queryBeforeTag = localQuery.substring(0, posTag);
        queryAfterTag = localQuery.substring(endPosition);
        localQuery = queryBeforeTag + internalTag.get(currentTag) + queryAfterTag;

        posTag = localQuery.indexOf(currentTag);
      }
    }


    return localQuery;
  }

  /**
   * Returns a specific SQL query from the properties file.
   * @param tableType (String) the specific word which allows us to separate the right SQL code
   * @return a part of the SQL query whished
   */
  private String getSubQuery(String tableType) {
    // get the right SQL request
    int beginTableType = allQueries
        .indexOf("<" + tableType.toUpperCase() + ">")
        + tableType.length() + 2;
    int endTableType = allQueries.indexOf("</" + tableType.toUpperCase() + ">");

    // verify that's no pb
    if (beginTableType >= endTableType) {
      SilverTrace.warn("peasCore", "SQLRequest.getSubQuery",
          "root.MSG_GEN_PARAM_VALUE", "begin(" + beginTableType + ") >= end("
          + endTableType + ")");
      allQueries.indexOf("<BONUSTABLE>");
      return null;
    }
    String localQuery = allQueries.substring(beginTableType, endTableType);

    return localQuery;
  }
}
