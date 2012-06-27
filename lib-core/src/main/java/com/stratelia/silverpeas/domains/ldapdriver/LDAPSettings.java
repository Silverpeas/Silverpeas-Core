/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.domains.ldapdriver;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPConstraints;
import com.novell.ldap.LDAPSearchConstraints;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.domains.DriverSettings;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * This class read the property file and keep it's values accessible via the get functions
 *
 * @author tleroi
 * @version 1.0
 */
public class LDAPSettings extends DriverSettings {
  
  public final static String TIME_STAMP_MSAD = "uSNChanged";
  public final static String TIME_STAMP_MSAD_TT = "whenChanged";
  public final static String TIME_STAMP_NDS = "modifyTimeStamp";
  protected String LDAPHost = null;
  protected String LDAPImpl = null;
  protected int LDAPPort = 389;
  protected int LDAPProtocolVer = LDAPConnection.LDAP_V3;
  protected boolean LDAPOpAttributesUsed = false;
  protected String LDAPAccessLoginDN = null;
  protected String LDAPAccessPasswd = null;
  protected String LDAPUserBaseDN = null;
  protected boolean LDAPSearchRecurs = false;
  protected LDAPConstraints LDAPDefaultConstraints = null;
  protected LDAPSearchConstraints LDAPDefaultSearchConstraints = null;
  protected int LDAPMaxMsClientTimeLimit = 0;
  protected int LDAPMaxSecServerTimeLimit = 0;
  protected int LDAPMaxNbEntryReturned = 5000;
  protected int LDAPMaxNbReferrals = 0;
  protected int LDAPBatchSize = 1;
  protected boolean LDAPSecured = false;
  protected boolean sortControlSupported = true;
  protected int LDAPPortSecured = 636;
  protected boolean SYNCHROautomatic = false;
  protected boolean SYNCHRORecursToGroups = true;
  protected boolean SYNCHROthreaded = false;
  protected String SYNCHROtimeStampVar = "uSNChanged";
  protected boolean SYNCHROCacheEnabled = true;
  protected boolean SYNCHROImportUsers = true;
  protected String usersClassName = null;
  protected String usersFilter = null;
  // AdminUser
  protected String usersType = null;
  protected String usersIdField = null;
  protected String usersLoginField = null;
  protected String usersFirstNameField = null;
  protected String usersLastNameField = null;
  protected String usersEmailField = null;
  // Groups
  protected String groupsType = null;
  protected String groupsClassName = null;
  protected boolean groupsInheritProfiles = false;
  protected String groupsFilter = null;
  protected int groupsNamingDepth = 0;
  protected String groupsIdField = null;
  protected boolean groupsIncludeEmptyGroups = true;
  protected String groupsSpecificGroupsBaseDN = null;
  protected String groupsMemberField = null;
  protected String groupsNameField = null;
  protected String groupsDescriptionField = null;
  // IHM
  protected boolean ihmImportUsers = true;
  protected boolean ihmImportGroups = true;

  /**
   * Performs initialization from a properties file. The optional properties are retreive with
   * getSureString.
   *
   * @param rs Properties resource file
   */
  public void initFromProperties(ResourceLocator rs) {
    // Database Settings
    // -----------------
    LDAPImpl = rs.getString("database.LDAPImpl", null);
    LDAPHost = rs.getString("database.LDAPHost", null);
    LDAPPort = getIntValue(rs, "database.LDAPPort", LDAPPort);
    LDAPProtocolVer = getIntValue(rs, "database.LDAPProtocolVer",
        LDAPProtocolVer);
    LDAPOpAttributesUsed = getBooleanValue(rs, "database.LDAPOpAttributesUsed",
        LDAPOpAttributesUsed);
    LDAPProtocolVer = LDAPConnection.LDAP_V3; // Only compatible with V3
    LDAPAccessLoginDN = rs.getString("database.LDAPAccessLoginDN", null);
    LDAPAccessPasswd = rs.getString("database.LDAPAccessPasswd", null);
    LDAPUserBaseDN = rs.getString("database.LDAPUserBaseDN", null);
    LDAPMaxMsClientTimeLimit = getIntValue(rs,
        "database.LDAPMaxMsClientTimeLimit", LDAPMaxMsClientTimeLimit);
    LDAPMaxSecServerTimeLimit = getIntValue(rs,
        "database.LDAPMaxSecServerTimeLimit", LDAPMaxSecServerTimeLimit);
    LDAPMaxNbEntryReturned = getIntValue(rs, "database.LDAPMaxNbEntryReturned",
        LDAPMaxNbEntryReturned);
    LDAPMaxNbReferrals = getIntValue(rs, "database.LDAPMaxNbReferrals",
        LDAPMaxNbReferrals);
    LDAPBatchSize = getIntValue(rs, "database.LDAPBatchSize", LDAPBatchSize);
    LDAPSearchRecurs = getBooleanValue(rs, "database.LDAPSearchRecurs",
        LDAPSearchRecurs);
    LDAPSecured = getBooleanValue(rs, "database.LDAPSecured", LDAPSecured);
    LDAPPortSecured = getIntValue(rs, "database.LDAPPortSecured",
        LDAPPortSecured);
    sortControlSupported = getBooleanValue(rs, "database.SortControlSupported", ! "openldap".
        equalsIgnoreCase(LDAPImpl));
    LDAPDefaultSearchConstraints = getSearchConstraints(true);
    LDAPDefaultConstraints = getConstraints(true);

    // Synchro parameters
    // -------------------
    SYNCHROautomatic = getBooleanValue(rs, "synchro.Automatic",
        SYNCHROautomatic);
    SYNCHRORecursToGroups = getBooleanValue(rs, "synchro.RecursToGroups",
        SYNCHRORecursToGroups);
    SYNCHROthreaded = getBooleanValue(rs, "synchro.Threaded", SYNCHROthreaded);
    SYNCHROtimeStampVar = getStringValue(rs, "synchro.timeStampVar",
        SYNCHROtimeStampVar);
    SYNCHROCacheEnabled = getBooleanValue(rs, "synchro.CacheEnabled",
        SYNCHROCacheEnabled);
    SYNCHROImportUsers = getBooleanValue(rs, "synchro.importUsers", true);

    // Users Settings
    // --------------
    usersType = rs.getString("users.Type", null);
    usersClassName = rs.getString("users.ClassName", null);
    usersFilter = rs.getString("users.Filter", null);
    // AdminUser
    usersIdField = rs.getString("users.IdField", null);
    usersLoginField = rs.getString("users.LoginField", null);
    usersFirstNameField = rs.getString("users.FirstNameField", null);
    usersLastNameField = rs.getString("users.LastNameField", null);
    usersEmailField = getSureString(rs, "users.EmailField");

    // Groups Settings
    // ---------------
    groupsType = rs.getString("groups.Type", null);
    groupsClassName = rs.getString("groups.ClassName", null);
    groupsInheritProfiles = getBooleanValue(rs, "groups.InheritProfiles",
        groupsInheritProfiles);
    groupsFilter = rs.getString("groups.Filter", null);
    groupsNamingDepth = getIntValue(rs, "groups.NamingDepth", groupsNamingDepth);
    groupsIdField = rs.getString("groups.IdField", null);
    groupsIncludeEmptyGroups = getBooleanValue(rs, "groups.IncludeEmptyGroups",
        groupsIncludeEmptyGroups);
    groupsSpecificGroupsBaseDN = getSureString(rs,
        "groups.SpecificGroupsBaseDN");
    groupsMemberField = getSureString(rs, "groups.MemberField");
    groupsNameField = getSureString(rs, "groups.NameField");
    groupsDescriptionField = getSureString(rs, "groups.DescriptionField");

    // IHM Settings
    // ------------
    ihmImportUsers = getBooleanValue(rs, "ihm.importUsers", true);
    ihmImportGroups = getBooleanValue(rs, "ihm.importGroups", true);
  }

  // HOST FIELDS
  // -----------
  public boolean isSynchroAutomatic() {
    return SYNCHROautomatic;
  }
  
  public boolean isSynchroRecursToGroups() {
    return SYNCHRORecursToGroups;
  }
  
  public boolean isSynchroThreaded() {
    return SYNCHROthreaded;
  }
  
  public String getTimeStampVar() {
    return SYNCHROtimeStampVar;
  }
  
  public boolean isSynchroCacheEnabled() {
    return SYNCHROCacheEnabled;
  }
  
  public boolean mustImportUsers() {
    return SYNCHROImportUsers;
  }
  
  public AbstractLDAPTimeStamp newLDAPTimeStamp(String theValue) {
    if (TIME_STAMP_MSAD.equalsIgnoreCase(getTimeStampVar())) {
      return new LDAPTimeStampMSAD(this, theValue);
    } else {
      return new LDAPTimeStampNDS(this, theValue);
    }
  }
  
  public String getLDAPImpl() {
    return LDAPImpl;
  }
  
  public String getLDAPHost() {
    return LDAPHost;
  }
  
  public int getLDAPPort() {
    return ((LDAPSecured) ? LDAPPortSecured : LDAPPort);
  }
  
  public int getLDAPProtocolVer() {
    return LDAPProtocolVer;
  }
  
  public boolean isLDAPOpAttributesUsed() {
    return LDAPOpAttributesUsed;
  }
  
  public String getLDAPAccessLoginDN() {
    return LDAPAccessLoginDN;
  }
  
  public String getLDAPAccessPasswd() {
    return LDAPAccessPasswd;
  }
  
  public String getLDAPUserBaseDN() {
    return LDAPUserBaseDN;
  }
  
  public boolean getLDAPSearchRecurs() {
    return LDAPSearchRecurs;
  }
  
  public boolean isLDAPSecured() {
    return LDAPSecured;
  }
  
  public int getScope() {
    if (LDAPSearchRecurs) {
      return LDAPConnection.SCOPE_SUB;
    } else {
      return LDAPConnection.SCOPE_ONE;
    }
  }
  
  public LDAPSearchConstraints getSearchConstraints(boolean allocateNew) {
    if (allocateNew) {
      boolean doReferrals = true;
      if (LDAPMaxNbReferrals == 0) {
        doReferrals = false;
      }
      return new LDAPSearchConstraints(LDAPMaxMsClientTimeLimit,
          LDAPMaxSecServerTimeLimit, LDAPSearchConstraints.DEREF_NEVER, LDAPMaxNbEntryReturned,
          doReferrals, LDAPBatchSize, null, LDAPMaxNbReferrals);
    }
    return LDAPDefaultSearchConstraints;
  }
  
  public LDAPConstraints getConstraints(boolean allocateNew) {
    LDAPConstraints theConstraints;
    if (allocateNew) {
      boolean doReferrals = true;
      if (LDAPMaxNbReferrals == 0) {
        doReferrals = false;
      }
      return new LDAPConstraints(LDAPMaxMsClientTimeLimit, doReferrals, null, LDAPMaxNbReferrals);
    }
    return LDAPDefaultConstraints;
  }

  // USER FIELDS
  // -----------
  public LDAPUser newLDAPUser() throws AdminException {
    try {
      if (usersType != null) {
        return (LDAPUser) Class.forName(usersType).newInstance();
      }
      return new LDAPUser();
    } catch (Exception e) {
      throw new AdminException("LDAPSettings.newLDAPUser", SilverpeasException.ERROR,
          "admin.EX_ERR_CANT_INSTANCIATE_USER_CLASS", usersType, e);
    }
  }
  
  public String getUsersClassName() {
    return usersClassName;
  }
  
  public String getUsersFilter() {
    return usersFilter;
  }
  
  public String getUsersFullFilter() {
    if (StringUtil.isDefined(usersFilter)) {
      return "(&(objectClass=" + usersClassName + ")" + usersFilter + ")";
    }
    return "(objectClass=" + usersClassName + ")";
  }

  // AdminUser fields
  public String getUsersIdField() {
    return usersIdField;
  }
  
  public String getUsersLoginField() {
    return usersLoginField;
  }
  
  public String getUsersFirstNameField() {
    return usersFirstNameField;
  }
  
  public String getUsersLastNameField() {
    return usersLastNameField;
  }
  
  public String getUsersEmailField() {
    return usersEmailField;
  }
  
  public String getUsersIdFilter(String value) {
    if (LDAPUtility.isAGuid(getUsersIdField()) && (value != null)) {
      // Replace all "\\" by "\"
      StringBuilder singleSlashValue = new StringBuilder(value.length());
      boolean bIsFirst = true;
      char[] vca = value.toCharArray();
      
      for (char aVca : vca) {
        if (aVca == '\\') {
          if (bIsFirst) {
            singleSlashValue.append(aVca);
          }
          bIsFirst = !bIsFirst;
        } else {
          bIsFirst = true;
          singleSlashValue.append(aVca);
        }
      }
      return "(&" + getUsersFullFilter() + "(" + getUsersIdField() + "="
          + singleSlashValue.toString() + "))";
    } else {
      return "(&" + getUsersFullFilter() + "(" + getUsersIdField() + "="
          + LDAPUtility.normalizeFilterValue(value) + "))";
    }
  }
  
  public String getUsersLoginFilter(String value) {
    return "(&" + getUsersFullFilter() + "(" + getUsersLoginField() + "="
        + value + "))";
  }

  // GROUP FIELDS
  // ------------
  public AbstractLDAPGroup newLDAPGroup() throws AdminException {
    try {
      return (AbstractLDAPGroup) Class.forName(groupsType).newInstance();
    } catch (Exception e) {
      throw new AdminException("LDAPSettings.newLDAPGroup",
          SilverpeasException.ERROR,
          "admin.EX_ERR_CANT_INSTANCIATE_GROUP_CLASS", groupsType, e);
    }
  }
  
  public String getGroupsClassName() {
    return groupsClassName;
  }
  
  public boolean isGroupsInheritProfiles() {
    return groupsInheritProfiles;
  }
  
  public String getGroupsFilter() {
    return groupsFilter;
  }
  
  public int getGroupsNamingDepth() {
    return groupsNamingDepth;
  }
  
  public String getGroupsFullFilter() {
    if (groupsFilter != null && groupsFilter.length() > 0) {
      return "(&(objectClass=" + groupsClassName + ")" + groupsFilter + ")";
    } else {
      return "(objectClass=" + groupsClassName + ")";
    }
  }
  
  public String getGroupsMemberField() {
    return groupsMemberField;
  }
  
  public String getGroupsSpecificGroupsBaseDN() {
    if (!StringUtil.isDefined(groupsSpecificGroupsBaseDN)) {
      return LDAPUserBaseDN;
    } else if (groupsSpecificGroupsBaseDN.equalsIgnoreCase("root")) {
      return "";
    } else {
      return groupsSpecificGroupsBaseDN;
    }
  }

  // Group fields
  public String getGroupsIdField() {
    return groupsIdField;
  }
  
  public boolean getGroupsIncludeEmptyGroups() {
    return groupsIncludeEmptyGroups;
  }
  
  public String getGroupsNameField() {
    return groupsNameField;
  }
  
  public String getGroupsDescriptionField() {
    return groupsDescriptionField;
  }
  
  public String getGroupsIdFilter(String value) {
    if (LDAPUtility.isAGuid(getGroupsIdField()) && (value != null)) {
      // Replace all "\\" by "\"
      StringBuilder singleSlashValue = new StringBuilder(value.length());
      boolean bIsFirst = true;
      char[] vca = value.toCharArray();
      
      for (char aVca : vca) {
        if (aVca == '\\') {
          if (bIsFirst) {
            singleSlashValue.append(aVca);
          }
          bIsFirst = !bIsFirst;
        } else {
          bIsFirst = true;
          singleSlashValue.append(aVca);
        }
      }
      return "(&" + getGroupsFullFilter() + "(" + getGroupsIdField() + "="
          + singleSlashValue.toString() + "))";
    } else {
      return "(&" + getGroupsFullFilter() + "(" + getGroupsIdField() + "="
          + LDAPUtility.normalizeFilterValue(value) + "))";
    }
  }
  
  public String getGroupsNameFilter(String value) {
    return "(&" + getGroupsFullFilter() + "(" + getGroupsNameField() + "="
        + value + "))";
  }
  
  protected String[] getUserAttributes() {
    if (isLDAPOpAttributesUsed()) {
      String[] attrs = new String[5];
      attrs[0] = getUsersIdField();
      attrs[1] = getUsersEmailField();
      attrs[2] = getUsersFirstNameField();
      attrs[3] = getUsersLastNameField();
      attrs[4] = getUsersLoginField();
      return attrs;
    } else {
      return null;
    }
  }
  
  protected String[] getGroupAttributes() {
    if (isLDAPOpAttributesUsed()) {
      String[] attrs = new String[4];
      attrs[0] = getGroupsDescriptionField();
      attrs[1] = getGroupsIdField();
      attrs[2] = getGroupsMemberField();
      attrs[3] = getGroupsNameField();
      return attrs;
    } else {
      return null;
    }
  }
  
  public boolean displayImportUsers() {
    return ihmImportUsers;
  }
  
  public boolean displayImportGroups() {
    return ihmImportGroups;
  }
  
  public boolean isSortControlSupported() {
    return sortControlSupported;
  }
}
