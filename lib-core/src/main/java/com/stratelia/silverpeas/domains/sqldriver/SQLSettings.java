package com.stratelia.silverpeas.domains.sqldriver;

import com.stratelia.silverpeas.authentication.Authentication;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * This class read the property file and keep it's values accessible via the get
 * functions
 * 
 * @author tleroi
 * @version 1.0
 */

public class SQLSettings extends Object {
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
   * Performs initialization from a properties file. The optional properties are
   * retreive with getSureString.
   * 
   * @param rs
   *          Properties resource file
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

  // Local functions
  /**
   * Use this function to be sure to obtain a string without error, even if the
   * property is not found. (in that case, returns empty string)
   * 
   * @param rs
   *          the properties file
   * @param key
   *          the key value to retreive
   */
  protected String getSureString(ResourceLocator rs, String key) {
    String valret = null;

    try {
      valret = rs.getString(key, null);
      if (valret == null) {
        valret = "";
      }
    } catch (Exception e) {
      valret = "";
    }
    return valret;
  }

  protected String getStringValue(ResourceLocator rs, String key,
      String defaultValue) {
    String valret = defaultValue;

    try {
      valret = rs.getString(key, null);
      if (valret == null) {
        valret = defaultValue;
      }
    } catch (Exception e) {
      valret = defaultValue;
    }
    return valret;
  }

  protected boolean getBooleanValue(ResourceLocator rs, String key,
      boolean defaultValue) {
    String res = rs.getString(key, null);
    boolean valret = defaultValue;

    if (res != null) {
      if (res.equalsIgnoreCase("true") || res.equalsIgnoreCase("1")
          || res.equalsIgnoreCase("yes") || res.equalsIgnoreCase("oui")
          || res.equalsIgnoreCase("ok"))
        valret = true;
      else
        valret = false;
    }
    return valret;
  }

  protected int getIntValue(ResourceLocator rs, String key, int defaultValue) {
    String res = rs.getString(key, null);
    int valret = defaultValue;

    if (res != null) {
      try {
        valret = Integer.parseInt(res);
      } catch (Exception e) {
        SilverTrace.error("admin", "LDAPSettings.getUserIds()",
            "admin.MSG_ERR_LDAP_GENERAL", "Int parse error : " + key + " = "
                + res, e);
      }
    }
    return valret;
  }

  public String trunc(String src, int max) {
    if ((src == null) || (src.length() <= max)) {
      return src;
    } else {
      return src.substring(0, max);
    }
  }
}
