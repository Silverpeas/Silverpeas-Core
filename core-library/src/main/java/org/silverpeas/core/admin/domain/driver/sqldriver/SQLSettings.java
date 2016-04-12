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

package org.silverpeas.core.admin.domain.driver.sqldriver;

import org.silverpeas.core.admin.domain.driver.DriverSettings;
import org.silverpeas.core.util.SettingBundle;

/**
 * This class read the property file and keep it's values accessible via the get functions
 * @author tleroi
 * @version 1.0
 */

public class SQLSettings implements DriverSettings {

  // Definitions of max lengths due to limitations on Oracle databases
  public static final int DATABASE_TABLE_NAME_MAX_LENGTH = 30;
  public static final int DATABASE_COLUMN_NAME_MAX_LENGTH = 30;

  // For DB Access
  protected String sqlDataSourceJNDIName = "java:/datasources/silverpeas";

  // For Table Names
  protected String sqlUserTableName = "DomainSQL_User";
  protected String sqlGroupTableName = "DomainSQL_Group";
  protected String sqlUserGroupTableName = "DomainSQL_Group_User_Rel";

  // For Users
  protected String sqlUserSpecificIdColumnName = "id";
  protected String sqlUserLoginColumnName = "login";
  protected String sqlUserFirstNameColumnName = "firstName";
  protected String sqlUserLastNameColumnName = "lastName";
  protected String sqlUserEMailColumnName = "email";
  protected String sqlUserPasswordColumnName = "";
  protected String sqlUserPasswordValidColumnName = "";

  // For Groups
  protected String sqlGroupSpecificIdColumnName = "id";
  protected String sqlGroupNameColumnName = "name";
  protected String sqlGroupDescriptionColumnName = "description";
  protected String sqlGroupParentIdColumnName = "superGroupId";

  // For Users-Groups relations
  protected String sqlUserGroupUIDColumnName = "userId";
  protected String sqlUserGroupGIDColumnName = "groupId";

  /**
   * Performs initialization from a properties file. The optional properties are retreive with
   * getSureString.
   * @param rs Properties resource file
   */
  @Override
  public void initFromProperties(SettingBundle rs) {
    // Database Settings
    // -----------------
    sqlDataSourceJNDIName = rs.getString("database.SQLDataSourceJNDIName", sqlDataSourceJNDIName);

    // For Table Names
    sqlUserTableName = rs.getString("database.SQLUserTableName", sqlUserTableName);
    sqlGroupTableName = rs.getString("database.SQLGroupTableName", sqlGroupTableName);
    sqlUserGroupTableName = rs.getString("database.SQLUserGroupTableName", sqlUserGroupTableName);

    // For Users
    sqlUserSpecificIdColumnName = rs.getString(
        "database.SQLUserSpecificIdColumnName", sqlUserSpecificIdColumnName);
    sqlUserLoginColumnName = rs.getString("database.SQLUserLoginColumnName", sqlUserLoginColumnName);
    sqlUserFirstNameColumnName = rs.getString(
        "database.SQLUserFirstNameColumnName", sqlUserFirstNameColumnName);
    sqlUserLastNameColumnName = rs.getString(
        "database.SQLUserLastNameColumnName", sqlUserLastNameColumnName);
    sqlUserEMailColumnName = rs.getString("database.SQLUserEMailColumnName", sqlUserEMailColumnName);
    sqlUserPasswordColumnName = rs.getString(
        "database.SQLUserPasswordColumnName", sqlUserPasswordColumnName);
    sqlUserPasswordValidColumnName = rs.getString(
        "database.SQLUserPasswordValidColumnName", sqlUserPasswordValidColumnName);

    // For Groups
    sqlGroupSpecificIdColumnName = rs.getString(
        "database.SQLGroupSpecificIdColumnName", sqlGroupSpecificIdColumnName);
    sqlGroupNameColumnName = rs.getString("database.SQLGroupNameColumnName", sqlGroupNameColumnName);
    sqlGroupDescriptionColumnName = rs
        .getString("database.SQLGroupDescriptionColumnName", sqlGroupDescriptionColumnName);
    sqlGroupParentIdColumnName = rs.getString(
        "database.SQLGroupParentIdColumnName", sqlGroupParentIdColumnName);

    // For Users-Groups relations
    sqlUserGroupUIDColumnName = rs.getString(
        "database.SQLUserGroupUIDColumnName", sqlUserGroupUIDColumnName);
    sqlUserGroupGIDColumnName = rs.getString(
        "database.SQLUserGroupGIDColumnName", sqlUserGroupGIDColumnName);
  }

  // DB FIELDS
  // ---------
  public String getDataSourceJNDIName() {
    return sqlDataSourceJNDIName;
  }

  // For Table Names
  public String getUserTableName() {
    return sqlUserTableName;
  }

  public String getGroupTableName() {
    return sqlGroupTableName;
  }

  public String getRelTableName() {
    return sqlUserGroupTableName;
  }

  // For Users
  public String getUserSpecificIdColumnName() {
    return sqlUserSpecificIdColumnName;
  }

  public String getUserLoginColumnName() {
    return sqlUserLoginColumnName;
  }

  public String getUserFirstNameColumnName() {
    return sqlUserFirstNameColumnName;
  }

  public String getUserLastNameColumnName() {
    return sqlUserLastNameColumnName;
  }

  public String getUserEMailColumnName() {
    return sqlUserEMailColumnName;
  }

  public String getUserPasswordColumnName() {
    return sqlUserPasswordColumnName;
  }

  public String getUserPasswordValidColumnName() {
    return sqlUserPasswordValidColumnName;
  }

  public boolean isUserPasswordAvailable() {
    return (sqlUserPasswordColumnName.length() > 0);
  }

  public boolean isUserPasswordValidAvailable() {
    return (sqlUserPasswordValidColumnName.length() > 0);
  }

  // For Groups
  public String getGroupSpecificIdColumnName() {
    return sqlGroupSpecificIdColumnName;
  }

  public String getGroupNameColumnName() {
    return sqlGroupNameColumnName;
  }

  public String getGroupDescriptionColumnName() {
    return sqlGroupDescriptionColumnName;
  }

  public String getGroupParentIdColumnName() {
    return sqlGroupParentIdColumnName;
  }

  // For Users-Groups relations
  public String getRelUIDColumnName() {
    return sqlUserGroupUIDColumnName;
  }

  public String getRelGIDColumnName() {
    return sqlUserGroupGIDColumnName;
  }

  public String trunc(String src, int max) {
    if ((src == null) || (src.length() <= max)) {
      return src;
    } else {
      return src.substring(0, max);
    }
  }
}
