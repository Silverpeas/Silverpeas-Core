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
package org.silverpeas.core.admin.domain.driver.ldapdriver;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPConstraints;
import com.novell.ldap.LDAPSearchConstraints;
import org.silverpeas.core.admin.domain.driver.DriverSettings;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;

import java.util.Collection;

import static java.util.stream.Collectors.joining;

/**
 * This class read the property file and keep it's values accessible via the get functions
 * @author tleroi
 * @version 1.0
 */
public class LDAPSettings implements DriverSettings {

  public static final String TIME_STAMP_MSAD = "uSNChanged";
  public static final String TIME_STAMP_MSAD_TT = "whenChanged";
  public static final String TIME_STAMP_NDS = "modifyTimeStamp";
  private final LdapConfiguration configuration = new LdapConfiguration();
  private String ldapimpl = null;
  private int ldapProtocolVer = LDAPConnection.LDAP_V3;
  private boolean ldapOpAttributesUsed = false;
  private String ldapUserBaseDN = null;
  private boolean ldapSearchRecurs = false;
  private LDAPConstraints ldapDefaultConstraints = null;
  private LDAPSearchConstraints ldapDefaultSearchConstraints = null;
  private int ldapMaxMsClientTimeLimit = 0;
  private int ldapMaxSecServerTimeLimit = 0;
  private int ldapMaxNbEntryReturned = 5000;
  private int ldapMaxNbReferrals = 0;
  private int ldapBatchSize = 1;
  private boolean sortControlSupported = true;
  private boolean synchroAutomatic = false;
  private boolean synchroRecursToGroups = true;
  private boolean synchroThreaded = false;
  private boolean synchroCacheEnabled = true;
  private boolean synchroImportUsers = true;
  private String usersClassName = null;
  private String usersFilter = null;
  // AdminUser
  private String usersType = null;
  private String usersIdField = null;
  private String usersLoginField = null;
  private String usersFirstNameField = null;
  private String usersLastNameField = null;
  private String usersEmailField = null;
  // Account activation
  private String usersAccountControl = null;
  private String usersDisabledAccountFlag = null;
  // Groups
  private String groupsType = null;
  private String groupsClassName = null;
  private boolean groupsInheritProfiles = false;
  private String groupsFilter = null;
  private int groupsNamingDepth = 0;
  private String groupsIdField = null;
  private boolean groupsIncludeEmptyGroups = true;
  private String groupsSpecificGroupsBaseDN = null;
  private String groupsMemberField = null;
  private String groupsNameField = null;
  private String groupsDescriptionField = null;
  // IHM
  private boolean ihmImportUsers = true;
  private boolean ihmImportGroups = true;

  /**
   * Performs initialization from a properties file. The optional properties are retrieved with
   * getSureString.
   * @param rs Properties resource file
   */
  @Override
  public void initFromProperties(SettingBundle rs) {
    // Database Settings
    // -----------------
    ldapimpl = rs.getString("database.LDAPImpl", null);
    configuration.setEncryptedCredentials(rs.getBoolean("database.encryptedCredentials", false));
    configuration.setLdapHost(rs.getString("database.LDAPHost", null));
    configuration.setLdapPort(rs.getInteger("database.LDAPPort", configuration.getLdapPort()));
    ldapProtocolVer = rs.getInteger("database.LDAPProtocolVer", LDAPConnection.LDAP_V3);
    ldapOpAttributesUsed = rs.getBoolean("database.LDAPOpAttributesUsed", ldapOpAttributesUsed);
    ldapProtocolVer = LDAPConnection.LDAP_V3; // Only compatible with V3
    configuration.setUsername(rs.getString("database.LDAPAccessLoginDN", null));
    configuration
        .setPassword(rs.getString("database.LDAPAccessPasswd", ""));
    ldapUserBaseDN = rs.getString("database.LDAPUserBaseDN", null);
    ldapMaxMsClientTimeLimit =
        rs.getInteger("database.LDAPMaxMsClientTimeLimit", ldapMaxMsClientTimeLimit);
    ldapMaxSecServerTimeLimit =
        rs.getInteger("database.LDAPMaxSecServerTimeLimit", ldapMaxSecServerTimeLimit);
    ldapMaxNbEntryReturned =
        rs.getInteger("database.LDAPMaxNbEntryReturned", ldapMaxNbEntryReturned);
    ldapMaxNbReferrals = rs.getInteger("database.LDAPMaxNbReferrals", ldapMaxNbReferrals);
    ldapBatchSize = rs.getInteger("database.LDAPBatchSize", ldapBatchSize);
    ldapSearchRecurs = rs.getBoolean("database.LDAPSearchRecurs", ldapSearchRecurs);
    configuration.setSecure(rs.getBoolean("database.LDAPSecured", false));
    if (configuration.isSecure()) {
      configuration.setLdapPort(rs.getInteger("database.LDAPPortSecured", 636));
    }
    sortControlSupported = rs.getBoolean("database.SortControlSupported", !"openldap".
        equalsIgnoreCase(ldapimpl));
    ldapDefaultSearchConstraints = getSearchConstraints(true);
    ldapDefaultConstraints = getConstraints(true);

    // Synchro parameters
    // -------------------
    synchroAutomatic = rs.getBoolean("synchro.Automatic", synchroAutomatic);
    synchroRecursToGroups = rs.getBoolean("synchro.RecursToGroups", synchroRecursToGroups);
    synchroThreaded = rs.getBoolean("synchro.Threaded", synchroThreaded);
    synchroCacheEnabled = rs.getBoolean("synchro.CacheEnabled", synchroCacheEnabled);
    synchroImportUsers = rs.getBoolean("synchro.importUsers", true);

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
    usersEmailField = rs.getString("users.EmailField", "");
    usersAccountControl = rs.getString("users.accountControl", "");
    usersDisabledAccountFlag = rs.getString("users.accountControl.disabledFlags", "");

    // Groups Settings
    // ---------------
    groupsType = rs.getString("groups.Type", null);
    groupsClassName = rs.getString("groups.ClassName", null);
    groupsInheritProfiles = rs.getBoolean("groups.InheritProfiles", groupsInheritProfiles);
    groupsFilter = rs.getString("groups.Filter", null);
    groupsNamingDepth = rs.getInteger("groups.NamingDepth", groupsNamingDepth);
    groupsIdField = rs.getString("groups.IdField", null);
    groupsIncludeEmptyGroups =
        rs.getBoolean("groups.IncludeEmptyGroups", groupsIncludeEmptyGroups);
    groupsSpecificGroupsBaseDN = rs.getString("groups.SpecificGroupsBaseDN", "");
    groupsMemberField = rs.getString("groups.MemberField", "");
    groupsNameField = rs.getString("groups.NameField", "");
    groupsDescriptionField = rs.getString("groups.DescriptionField", "");

    // IHM Settings
    // ------------
    ihmImportUsers = rs.getBoolean("ihm.importUsers", true);
    ihmImportGroups = rs.getBoolean("ihm.importGroups", true);
  }

  // HOST FIELDS
  // -----------
  public boolean isSynchroAutomatic() {
    return synchroAutomatic;
  }

  public boolean isSynchroRecursToGroups() {
    return synchroRecursToGroups;
  }

  public boolean isSynchroThreaded() {
    return synchroThreaded;
  }

  public boolean isSynchroCacheEnabled() {
    return synchroCacheEnabled;
  }

  public boolean mustImportUsers() {
    return synchroImportUsers;
  }

  public String getLDAPImpl() {
    return ldapimpl;
  }

  public String getLDAPHost() {
    return configuration.getLdapHost();
  }

  public int getLDAPPort() {
    return configuration.getLdapPort();
  }

  public int getLDAPProtocolVer() {
    return ldapProtocolVer;
  }

  public boolean isLDAPOpAttributesUsed() {
    return ldapOpAttributesUsed;
  }

  public String getLDAPAccessLoginDN() {
    return configuration.getUsername();
  }

  public byte[] getLDAPAccessPasswd() {
    return configuration.getPassword();
  }

  public String getLDAPUserBaseDN() {
    return ldapUserBaseDN;
  }

  public boolean getLDAPSearchRecurs() {
    return ldapSearchRecurs;
  }

  public boolean isLDAPSecured() {
    return configuration.isSecure();
  }

  public int getScope() {
    if (ldapSearchRecurs) {
      return LDAPConnection.SCOPE_SUB;
    }
    return LDAPConnection.SCOPE_ONE;
  }

  public LDAPSearchConstraints getSearchConstraints(boolean allocateNew) {
    if (allocateNew) {
      boolean doReferrals = ldapMaxNbReferrals != 0;
      return new LDAPSearchConstraints(ldapMaxMsClientTimeLimit, ldapMaxSecServerTimeLimit,
          LDAPSearchConstraints.DEREF_NEVER, ldapMaxNbEntryReturned, doReferrals, ldapBatchSize,
          null, ldapMaxNbReferrals);
    }
    return ldapDefaultSearchConstraints;
  }

  public LDAPConstraints getConstraints(boolean allocateNew) {
    if (allocateNew) {
      boolean doReferrals = ldapMaxNbReferrals != 0;
      return new LDAPConstraints(ldapMaxMsClientTimeLimit, doReferrals, null, ldapMaxNbReferrals);
    }
    return ldapDefaultConstraints;
  }

  // USER FIELDS
  // -----------
  public LDAPUser newLDAPUser() throws AdminException {
    try {
      if (usersType != null) {
        return (LDAPUser) Class.forName(usersType).getConstructor().newInstance();
      }
      return new LDAPUser();
    } catch (Exception e) {
      throw new AdminException(e.getMessage(), e);
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

  public String getUsersAccountControl() {
    return usersAccountControl;
  }

  public String getUsersDisabledAccountFlag() {
    return usersDisabledAccountFlag;
  }

  public String getUsersIdFilter(String value) {
    if (LDAPUtility.isAGuid(getUsersIdField()) && (value != null)) {
      String singleSlashValue = parseValue(value);
      return "(&" + getUsersFullFilter() + "(" + getUsersIdField() + "=" +
          singleSlashValue + "))";
    } else {
      return "(&" + getUsersFullFilter() + "(" + getUsersIdField() + "=" +
          LDAPUtility.normalizeFilterValue(value) + "))";
    }
  }

  /**
   * Replaces all "\\" by "\".
   * @param value the value to parse for "\\".
   * @return the result of the value parsing.
   */
  private String parseValue(String value) {
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
    return singleSlashValue.toString();
  }

  public String getUsersIdFilter(Collection<String> values) {
    if (values.size() == 1) {
      return getUsersIdFilter(values.iterator().next());
    }
    return values.stream().map(this::getUsersIdFilter).collect(joining(StringUtil.EMPTY, "(|", ")"));
  }

  public String getUsersLoginFilter(String value) {
    return "(&" + getUsersFullFilter() + "(" + getUsersLoginField() + "=" + value + "))";
  }

  // GROUP FIELDS
  // ------------
  public AbstractLDAPGroup newLDAPGroup() throws AdminException {
    try {
      return (AbstractLDAPGroup) Class.forName(groupsType).getConstructor().newInstance();
    } catch (Exception e) {
      throw new AdminException(e.getMessage(), e);
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
    if (groupsFilter != null && !groupsFilter.isEmpty()) {
      return "(&(objectClass=" + groupsClassName + ")" + groupsFilter + ")";
    }
    return "(objectClass=" + groupsClassName + ")";
  }

  public String getGroupsMemberField() {
    return groupsMemberField;
  }

  public String getGroupsSpecificGroupsBaseDN() {
    if (!StringUtil.isDefined(groupsSpecificGroupsBaseDN)) {
      return ldapUserBaseDN;
    }
    if (groupsSpecificGroupsBaseDN.equalsIgnoreCase("root")) {
      return "";
    }
    return groupsSpecificGroupsBaseDN;
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
      String singleSlashValue = parseValue(value);
      return "(&" + getGroupsFullFilter() + "(" + getGroupsIdField() + "=" +
          singleSlashValue + "))";
    } else {
      return "(&" + getGroupsFullFilter() + "(" + getGroupsIdField() + "=" +
          LDAPUtility.normalizeFilterValue(value) + "))";
    }
  }

  public String getGroupsNameFilter(String value) {
    return "(&" + getGroupsFullFilter() + "(" + getGroupsNameField() + "=" + value + "))";
  }

  protected String[] getUserAttributes() {
    if (isLDAPOpAttributesUsed()) {
      String usersAccountControlAttribute = getUsersAccountControl();
      String[] attrs = new String[StringUtil.isDefined(usersAccountControlAttribute) ? 6 : 5];
      attrs[0] = getUsersIdField();
      attrs[1] = getUsersEmailField();
      attrs[2] = getUsersFirstNameField();
      attrs[3] = getUsersLastNameField();
      attrs[4] = getUsersLoginField();
      if (StringUtil.isDefined(usersAccountControlAttribute)) {
        attrs[5] = usersAccountControlAttribute;
      }
      return attrs;
    }
    return ArrayUtil.emptyStringArray();
  }

  protected String[] getGroupAttributes() {
    if (isLDAPOpAttributesUsed()) {
      String[] attrs = new String[4];
      attrs[0] = getGroupsDescriptionField();
      attrs[1] = getGroupsIdField();
      attrs[2] = getGroupsMemberField();
      attrs[3] = getGroupsNameField();
      return attrs;
    }
    return ArrayUtil.emptyStringArray();
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
