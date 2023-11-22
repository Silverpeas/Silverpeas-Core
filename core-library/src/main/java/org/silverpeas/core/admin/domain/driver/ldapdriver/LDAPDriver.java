/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPModification;
import org.silverpeas.core.admin.domain.AbstractDomainDriver;
import org.silverpeas.core.admin.domain.model.DomainProperty;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.security.authentication.exception.AuthenticationBadCredentialException;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singleton;
import static org.silverpeas.core.admin.domain.DomainDriver.ActionConstants.*;
import static org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery.unique;

/**
 * Domain driver for LDAP access. Could be used to access any type of LDAP DB (even exchange)
 * IMPORTANT: For the moment, it is not possible to add, remove or update a group neither add or
 * remove an user. However, it is possible to update an user...
 * @author tleroi
 */
public class LDAPDriver extends AbstractDomainDriver {

  LDAPSynchroCache synchroCache = new LDAPSynchroCache();
  protected LDAPSettings driverSettings = new LDAPSettings();
  protected LDAPUser userTranslator = null;
  protected AbstractLDAPGroup groupTranslator = null;

  /**
   * Initializes the driver from the specified settings bundle.
   * @param rs name of resource file
   * @throws AdminException if the initialization fails.
   */
  @Override
  public void initFromProperties(SettingBundle rs) throws AdminException {
    driverSettings.initFromProperties(rs);
    synchroCache.init(driverSettings);
    userTranslator = driverSettings.newLDAPUser();
    userTranslator.init(driverSettings, this, synchroCache);
    groupTranslator = driverSettings.newLDAPGroup();
    groupTranslator.init(driverSettings, synchroCache);
  }

  @Override
  public void addPropertiesToImport(List<DomainProperty> props) {
    addPropertiesToImport(props, null);
  }

  @Override
  public void addPropertiesToImport(List<DomainProperty> props, Map<String, String> descriptions) {
    props.add(getProperty("lastName", driverSettings.getUsersLastNameField(), descriptions));
    props.add(getProperty("firstName", driverSettings.getUsersFirstNameField(), descriptions));
    props.add(getProperty("email", driverSettings.getUsersEmailField(), descriptions));
    props.add(getProperty("login", driverSettings.getUsersLoginField(), descriptions));
  }

  private DomainProperty getProperty(String name, String mapParameter,
      Map<String, String> descriptions) {
    DomainProperty property = new DomainProperty();
    property.setName(name);
    property.setMapParameter(mapParameter);
    if (descriptions != null) {
      property.setDescription(descriptions.get(name));
    }
    return property;
  }

  /**
   * Gets all the actions this driver supports.
   * @return a bit mask identifying the supported actions.
   */
  @Override
  public long getDriverActions() {
    if (isX509Enabled()) {
      return ACTION_MASK_RO | ACTION_X509_USER | ACTION_UPDATE_USER;
    } else {
      return ACTION_MASK_RO | ACTION_UPDATE_USER;
    }
  }

  @Override
  public boolean isSynchroOnLoginEnabled() {
    return driverSettings.isSynchroAutomatic();
  }

  @Override
  public boolean isSynchroOnLoginRecursToGroups() {
    return driverSettings.isSynchroRecursToGroups();
  }

  @Override
  public boolean isGroupsInheritProfiles() {
    return driverSettings.isGroupsInheritProfiles();
  }

  @Override
  public boolean mustImportUsers() {
    return driverSettings.mustImportUsers();
  }

  @Override
  public boolean isSynchroThreaded() {
    return driverSettings.isSynchroThreaded();
  }

  /**
   * Called when Admin starts the synchronization
   */
  @Override
  public void beginSynchronization() {
    setSynchroInProcess(true);
    synchroCache.beginSynchronization();
    userTranslator.beginSynchronization();
    groupTranslator.beginSynchronization();
  }

  /**
   * Called when Admin ends the synchronization
   */
  @SuppressWarnings("unused")
  public String endSynchronization() {
    StringBuilder report = new StringBuilder();

    synchroCache.endSynchronization();
    String result = userTranslator.endSynchronization();
    if (result != null && !result.isEmpty()) {
      report.append("LDAP Domain User specific errors :\n").append(result).append("\n\n");
    }
    result = groupTranslator.endSynchronization();
    if (result != null && !result.isEmpty()) {
      report.append("LDAP Domain GroupDetail specific errors :\n").append(result).append("\n\n");
    }
    setSynchroInProcess(false);
    return report.toString();
  }

  /**
   * Import a given user in Database from the reference
   * @param userLogin The User Login to import
   * @return The User object that contain new user information
   */
  @Override
  public UserDetail importUser(String userLogin) throws AdminException {
    String ld = LDAPUtility.openConnection(driverSettings);

    try {
      return userTranslator.getUserByLogin(ld, userLogin);
    } finally {
      LDAPUtility.closeConnection(ld);
    }
  }

  /**
   * Remove a given user from database
   * @param userId The user id To remove synchro
   */
  @Override
  public void removeUser(String userId) throws AdminException {
    // In this driver, do nothing
  }

  /**
   * Update user information in database
   * @param userId The User Id to synchronize
   * @return The User object that contain new user information
   */
  @Override
  public UserDetail synchroUser(String userId) throws AdminException {
    return getUser(userId);
  }

  @Override
  public String createUser(UserDetail user) throws AdminException {
    return null;
  }

  @Override
  public void deleteUser(String userId) throws AdminException {
    // Silverpeas doesn't modify the remote LDAP
  }

  @Override
  public void updateUserFull(UserFull user) throws AdminException {
    String ld = null;

    try {
      ld = LDAPUtility.openConnection(driverSettings);
      LDAPConnection connection = LDAPUtility.getConnection(ld);

      LDAPEntry theEntry = getUserLDAPEntry(ld, user.getSpecificId());

      if (theEntry == null) {
        throw new AuthenticationBadCredentialException(
            "User not found: " + user.getSpecificId() + ";IdField=" +
                driverSettings.getUsersIdField());
      }

      String userFullDN = theEntry.getDN();
      List<LDAPModification> modifications = new ArrayList<>();

      // update basic information (first name, last name and email)
      updateBasicData(user, modifications);

      // prepare properties update
      preparePropertiesUpdate(ld, user, modifications);

      // Perform the update
      connection
          .modify(userFullDN, modifications.toArray(new LDAPModification[0]));
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
      throw new AdminException("LDAP access error", ex);
    } finally {
      try {
        if (ld != null) {
          LDAPUtility.closeConnection(ld);
        }
      } catch (AdminException ex) {
        // The exception that could occur in the emergency stop is not interesting
        SilverLogger.getLogger(this).error(ex.getMessage(), ex);
      }
    }
  }

  private void updateBasicData(final UserFull user, final List<LDAPModification> modifications) {
    LDAPAttribute attribute =
        getLDAPAttribute(driverSettings.getUsersFirstNameField(), user.getFirstName());
    modifications.add(new LDAPModification(LDAPModification.REPLACE, attribute));

    attribute = getLDAPAttribute(driverSettings.getUsersLastNameField(), user.getLastName());
    modifications.add(new LDAPModification(LDAPModification.REPLACE, attribute));

    attribute = getLDAPAttribute(driverSettings.getUsersEmailField(), user.getEmailAddress());
    modifications.add(new LDAPModification(LDAPModification.REPLACE, attribute));
  }

  private void preparePropertiesUpdate(final String ld, final UserFull user,
      final List<LDAPModification> modifications) throws AdminException {
    for (String propertyName : user.getPropertiesNames()) {
      DomainProperty property = user.getProperty(propertyName);
      if (property != null &&
          (property.isUpdateAllowedToAdmin() || property.isUpdateAllowedToUser())) {
        preparePropertyUpdate(ld, user, modifications, propertyName, property);
      }
    }
  }

  private void preparePropertyUpdate(final String ld, final UserFull user,
      final List<LDAPModification> modifications, final String propertyName,
      final DomainProperty property) throws AdminException {
    final LDAPAttribute attribute;
    if (property.getType().equals(DomainProperty.PROPERTY_TYPE_USERID)) {
      // setting user's DN
      String anotherUserId = user.getValue(propertyName);
      String anotherUserDN = null;
      if (StringUtil.isDefined(anotherUserId)) {
        UserDetail anotherUser = UserDetail.getById(anotherUserId);
        LDAPEntry anotherUserEntry = getUserLDAPEntry(ld, anotherUser.getSpecificId());
        if (anotherUserEntry != null) {
          anotherUserDN = anotherUserEntry.getDN();
        }
      }
      attribute = getLDAPAttribute(property.getMapParameter(), anotherUserDN);
    } else {
      attribute = getLDAPAttribute(property.getMapParameter(), user.getValue(propertyName));
    }
    modifications.add(new LDAPModification(LDAPModification.REPLACE, attribute));
  }

  private LDAPEntry getUserLDAPEntry(String connection, String id) throws AdminException {
    return LDAPUtility
        .getFirstEntryFromSearch(connection, driverSettings.getLDAPUserBaseDN(),
            driverSettings.getScope(), driverSettings.getUsersIdFilter(id),
            driverSettings.getUserAttributes());
  }

  private LDAPAttribute getLDAPAttribute(String name, String value) {
    LDAPAttribute attribute = new LDAPAttribute(name);
    if (StringUtil.isDefined(value)) {
      attribute.addValue(value);
    }
    return attribute;
  }

  @Override
  public void updateUserDetail(UserDetail user) {
    // Silverpeas doesn't modify the remote LDAP
  }

  /**
   * Retrieve user information from database
   * @param specificId The user id as stored in the database
   * @return The User object that contain new user information
   * @throws AdminException if the user cannot be gotten.
   */
  @Override
  public UserFull getUserFull(String specificId) throws AdminException {
    return unique(listUserFulls(singleton(specificId)));
  }

  @Override
  public List<UserFull> listUserFulls(final Collection<String> specificIds) throws AdminException {
    final String ld = LDAPUtility.openConnection(driverSettings);
    try {
      return userTranslator.listUserFulls(ld, specificIds, this.getDomainId());
    } finally {
      LDAPUtility.closeConnection(ld);
    }
  }

  /**
   * Retrieve user information from database
   * @param specificId The user id as stored in the database
   * @return The User object that contain new user information
   * @throws AdminException if the user cannot be gotten.
   */
  @Override
  public UserDetail getUser(String specificId) throws AdminException {
    return unique(listUsers(singleton(specificId)));
  }

  @Override
  public List<UserDetail> listUsers(final Collection<String> specificIds) throws AdminException {
    final String ld = LDAPUtility.openConnection(driverSettings);
    try {
      return userTranslator.listUsers(ld, specificIds);
    } finally {
      LDAPUtility.closeConnection(ld);
    }
  }

  /**
   * Retrieve all users from the database
   * @return User[] An array of User Objects that contain users information
   * @throws AdminException if the fetching of the users fails.
   */
  @Override
  public UserDetail[] getAllUsers() throws AdminException {
    String ld = LDAPUtility.openConnection(driverSettings);
    try {
      return userTranslator.getAllUsers(ld, "");
    } finally {
      LDAPUtility.closeConnection(ld);
    }
  }

  @Override
  public UserDetail[] getUsersBySpecificProperty(String propertyName, String propertyValue)
      throws AdminException {
    DomainProperty property = getProperty(propertyName);
    if (property == null) {
      // This property is not defined in this domain
      return new UserDetail[0];
    } else {
      String ld = LDAPUtility.openConnection(driverSettings);
      try {
        String escapedPropertyValue = propertyValue;
        if (StringUtil.isDefined(propertyValue)) {
          // In a first time, as it could exist already LDAP escaped characters into the filter,
          // an unescape is done
          String unescapedPropertyValue = LDAPUtility.unescapeLDAPSearchFilter(propertyValue);
          // Then the escaping is performed
          escapedPropertyValue = LDAPUtility.normalizeFilterValue(unescapedPropertyValue);
        }
        String extraFilter = "(" + property.getMapParameter() + "=" + escapedPropertyValue + ")";
        return userTranslator.getAllUsers(ld, extraFilter);
      } finally {
        LDAPUtility.closeConnection(ld);
      }
    }
  }

  @Override
  public UserDetail[] getUsersByQuery(Map<String, String> query) throws AdminException {
    StringBuilder extraFilter = new StringBuilder();
    for (Map.Entry<String, String> property : query.entrySet()) {
      extraFilter.append("(")
          .append(property.getKey())
          .append("=")
          .append(property.getValue())
          .append(")");
    }

    String ld = LDAPUtility.openConnection(driverSettings);
    try {
      return userTranslator.getAllUsers(ld, extraFilter.toString());
    } finally {
      LDAPUtility.closeConnection(ld);
    }
  }

  /**
   * Retrieve user's groups
   * @param specificId The user id as stored in the database
   * @return The User's groups specific Ids
   */
  @Override
  public String[] getUserMemberGroupIds(String specificId) throws AdminException {
    String ld = LDAPUtility.openConnection(driverSettings);
    try {
      return groupTranslator.getUserMemberGroupIds(ld, specificId);
    } finally {
      LDAPUtility.closeConnection(ld);
    }
  }

  /**
   * Import a given group in Database from the reference
   * @param groupName The group name to import
   * @return The group object that contain new group information
   */
  @Override
  public GroupDetail importGroup(String groupName) throws AdminException {
    String ld = LDAPUtility.openConnection(driverSettings);
    try {
      return groupTranslator.getGroupByName(ld, groupName);
    } finally {
      LDAPUtility.closeConnection(ld);
    }
  }

  /**
   * Remove a given group from database
   * @param groupId The group id To remove synchro
   */
  @Override
  public void removeGroup(String groupId) throws AdminException {
    // Silverpeas doesn't modify the remote LDAP
  }

  /**
   * Update group information in database
   * @param groupId The group Id to synchronize
   * @return The group object that contain new group information
   */
  @Override
  public GroupDetail synchroGroup(String groupId) throws AdminException {
    return getGroup(groupId);
  }

  @Override
  public String createGroup(GroupDetail group) throws AdminException {
    // Silverpeas doesn't modify the remote LDAP
    return null;
  }

  @Override
  public void deleteGroup(String groupId) throws AdminException {
    // Silverpeas doesn't modify the remote LDAP
  }

  @Override
  public void updateGroup(GroupDetail group) throws AdminException {
    // Silverpeas doesn't modify the remote LDAP
  }

  /**
   * Retrieve group information from database
   * @param specificId The group id as stored in the database
   * @return The GroupDetail object that contains user information
   */
  @Override
  public GroupDetail getGroup(String specificId) throws AdminException {
    String ld = LDAPUtility.openConnection(driverSettings);

    try {
      return groupTranslator.getGroup(ld, specificId);
    } finally {
      LDAPUtility.closeConnection(ld);
    }
  }

  @Override
  public GroupDetail getGroupByName(String groupName) throws AdminException {
    return null;
  }

  /**
   * Retrieve all groups contained in the given group
   * @param groupId The group id as stored in the database
   * @return GroupDetail[] An array of GroupDetail Objects that contain groups information
   */
  @Override
  public GroupDetail[] getGroups(String groupId) throws AdminException {
    String ld = LDAPUtility.openConnection(driverSettings);

    try {
      return groupTranslator.getGroups(ld, groupId, "");
    } finally {
      LDAPUtility.closeConnection(ld);
    }
  }

  /**
   * Retrieve all groups from the database
   * @return GroupDetail[] An array of GroupDetail Objects that contain groups information
   */
  @Override
  public GroupDetail[] getAllGroups() throws AdminException {
    String ld = LDAPUtility.openConnection(driverSettings);

    try {
      return groupTranslator.getAllGroups(ld, "");
    } finally {
      LDAPUtility.closeConnection(ld);
    }
  }

  /**
   * Retrieve all root groups from the database
   * @return GroupDetail[] An array of GroupDetail Objects that contain root groups information
   */
  @Override
  public GroupDetail[] getAllRootGroups() throws AdminException {
    String ld = LDAPUtility.openConnection(driverSettings);

    try {
      return groupTranslator.getGroups(ld, null, "");
    } finally {
      LDAPUtility.closeConnection(ld);
    }
  }

  @Override
  public String[] getGroupMemberGroupIds(String groupId) throws AdminException {
    String ld = LDAPUtility.openConnection(driverSettings);

    try {
      return groupTranslator.getGroupMemberGroupIds(ld, groupId);
    } finally {
      LDAPUtility.closeConnection(ld);
    }
  }

  @Override
  public List<String> getUserAttributes() throws AdminException {
    return Arrays.asList(userTranslator.getUserAttributes());
  }

  @Override
  public void resetPassword(UserDetail user, String password) throws AdminException {
    // Access in read only
  }

  @Override
  public void resetEncryptedPassword(UserDetail user, String encryptedPassword) {
    // Access in read only
  }
}
