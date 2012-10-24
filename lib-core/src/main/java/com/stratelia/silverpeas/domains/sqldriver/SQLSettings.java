/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.stratelia.silverpeas.domains.sqldriver;

import com.stratelia.silverpeas.authentication.Authentication;
import com.stratelia.silverpeas.domains.DriverSettings;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * This class read the property file and keep it's values accessible via the get functions
 * @author tleroi
 * @version 1.0
 */

public class SQLSettings extends DriverSettings {
  // For DB Access
  protected String SQLClassName = "";
  protected String SQLJDBCUrl = "";
  protected String SQLAccessLogin = "";
  protected String SQLAccessPasswd = "";
  protected String SQLPasswordEncryption = Authentication.ENC_TYPE_CLEAR;

  // For Table Names
  protected String SQLUserTableName = "DomainSQL_User";
  protected String SQLGroupTableName = "DomainSQL_Group";
  protected String SQLUserGroupTableName = "DomainSQL_Group_User_Rel";

  // For Users
  protected String SQLUserSpecificIdColumnName = "id";
  protected String SQLUserLoginColumnName = "login";
  protected String SQLUserFirstNameColumnName = "firstName";
  protected String SQLUserLastNameColumnName = "lastName";
  protected String SQLUserEMailColumnName = "email";
  protected String SQLUserPasswordColumnName = "";
  protected String SQLUserPasswordValidColumnName = "";

  // For Groups
  protected String SQLGroupSpecificIdColumnName = "id";
  protected String SQLGroupNameColumnName = "name";
  protected String SQLGroupDescriptionColumnName = "description";
  protected String SQLGroupParentIdColumnName = "superGroupId";

  // For Users-Groups relations
  protected String SQLUserGroupUIDColumnName = "userId";
  protected String SQLUserGroupGIDColumnName = "groupId";

  /**
   * Performs initialization from a properties file. The optional properties are retreive with
   * getSureString.
   * @param rs Properties resource file
   */
  public void initFromProperties(ResourceLocator rs) {
    // Database Settings
    // -----------------
    SQLClassName = rs.getString("database.SQLClassName", SQLClassName);
    SQLJDBCUrl = rs.getString("database.SQLJDBCUrl", SQLJDBCUrl);
    SQLAccessLogin = rs.getString("database.SQLAccessLogin", SQLAccessLogin);
    SQLAccessPasswd = rs.getString("database.SQLAccessPasswd", SQLAccessPasswd);
    SQLPasswordEncryption = rs.getString("database.SQLPasswordEncryption",
        SQLPasswordEncryption);

    // For Table Names
    SQLUserTableName = rs.getString("database.SQLUserTableName",
        SQLUserTableName);
    SQLGroupTableName = rs.getString("database.SQLGroupTableName",
        SQLGroupTableName);
    SQLUserGroupTableName = rs.getString("database.SQLUserGroupTableName",
        SQLUserGroupTableName);

    // For Users
    SQLUserSpecificIdColumnName = rs.getString(
        "database.SQLUserSpecificIdColumnName", SQLUserSpecificIdColumnName);
    SQLUserLoginColumnName = rs.getString("database.SQLUserLoginColumnName",
        SQLUserLoginColumnName);
    SQLUserFirstNameColumnName = rs.getString(
        "database.SQLUserFirstNameColumnName", SQLUserFirstNameColumnName);
    SQLUserLastNameColumnName = rs.getString(
        "database.SQLUserLastNameColumnName", SQLUserLastNameColumnName);
    SQLUserEMailColumnName = rs.getString("database.SQLUserEMailColumnName",
        SQLUserEMailColumnName);
    SQLUserPasswordColumnName = rs.getString(
        "database.SQLUserPasswordColumnName", SQLUserPasswordColumnName);
    SQLUserPasswordValidColumnName = rs.getString(
        "database.SQLUserPasswordValidColumnName",
        SQLUserPasswordValidColumnName);

    // For Groups
    SQLGroupSpecificIdColumnName = rs.getString(
        "database.SQLGroupSpecificIdColumnName", SQLGroupSpecificIdColumnName);
    SQLGroupNameColumnName = rs.getString("database.SQLGroupNameColumnName",
        SQLGroupNameColumnName);
    SQLGroupDescriptionColumnName = rs
        .getString("database.SQLGroupDescriptionColumnName",
        SQLGroupDescriptionColumnName);
    SQLGroupParentIdColumnName = rs.getString(
        "database.SQLGroupParentIdColumnName", SQLGroupParentIdColumnName);

    // For Users-Groups relations
    SQLUserGroupUIDColumnName = rs.getString(
        "database.SQLUserGroupUIDColumnName", SQLUserGroupUIDColumnName);
    SQLUserGroupGIDColumnName = rs.getString(
        "database.SQLUserGroupGIDColumnName", SQLUserGroupGIDColumnName);
  }

  // DB FIELDS
  // ---------
  public String getClassName() {
    return SQLClassName;
  }

  public String getJDBCUrl() {
    return SQLJDBCUrl;
  }

  public String getAccessLogin() {
    return SQLAccessLogin;
  }

  public String getAccessPasswd() {
    return SQLAccessPasswd;
  }

  public String getPasswordEncryption() {
    return SQLPasswordEncryption;
  }

  public boolean isPasswordEncrypted() {
    return (!Authentication.ENC_TYPE_CLEAR
        .equalsIgnoreCase(SQLPasswordEncryption));
  }

  // For Table Names
  public String getUserTableName() {
    return SQLUserTableName;
  }

  public String getGroupTableName() {
    return SQLGroupTableName;
  }

  public String getRelTableName() {
    return SQLUserGroupTableName;
  }

  // For Users
  public String getUserSpecificIdColumnName() {
    return SQLUserSpecificIdColumnName;
  }

  public String getUserLoginColumnName() {
    return SQLUserLoginColumnName;
  }

  public String getUserFirstNameColumnName() {
    return SQLUserFirstNameColumnName;
  }

  public String getUserLastNameColumnName() {
    return SQLUserLastNameColumnName;
  }

  public String getUserEMailColumnName() {
    return SQLUserEMailColumnName;
  }

  public String getUserPasswordColumnName() {
    return SQLUserPasswordColumnName;
  }

  public String getUserPasswordValidColumnName() {
    return SQLUserPasswordValidColumnName;
  }

  public boolean isUserPasswordAvailable() {
    return (SQLUserPasswordColumnName.length() > 0);
  }

  public boolean isUserPasswordValidAvailable() {
    return (SQLUserPasswordValidColumnName.length() > 0);
  }

  // For Groups
  public String getGroupSpecificIdColumnName() {
    return SQLGroupSpecificIdColumnName;
  }

  public String getGroupNameColumnName() {
    return SQLGroupNameColumnName;
  }

  public String getGroupDescriptionColumnName() {
    return SQLGroupDescriptionColumnName;
  }

  public String getGroupParentIdColumnName() {
    return SQLGroupParentIdColumnName;
  }

  // For Users-Groups relations
  public String getRelUIDColumnName() {
    return SQLUserGroupUIDColumnName;
  }

  public String getRelGIDColumnName() {
    return SQLUserGroupGIDColumnName;
  }

  public String trunc(String src, int max) {
    if ((src == null) || (src.length() <= max)) {
      return src;
    } else {
      return src.substring(0, max);
    }
  }
}
