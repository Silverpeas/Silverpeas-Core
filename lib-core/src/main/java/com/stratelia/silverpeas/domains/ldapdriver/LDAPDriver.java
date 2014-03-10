/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.domains.ldapdriver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.silverpeas.authentication.exception.AuthenticationBadCredentialException;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AbstractDomainDriver;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.DomainProperty;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPModification;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Domain driver for LDAP access. Could be used to access any type of LDAP DB (even exchange)
 * IMPORTANT : For the moment, it is not possible to add, remove or update a group neither add or
 * remove an user. However, it is possible to update an user...
 *
 * @author tleroi
 */
public class LDAPDriver extends AbstractDomainDriver {

  LDAPSettings driverSettings = new LDAPSettings();
  LDAPSynchroCache synchroCache = new LDAPSynchroCache();
  LDAPUser userTranslator = null;
  AbstractLDAPGroup groupTranslator = null;
  boolean synchroInProcess = false;

  /**
   * Virtual method that performs extra initialization from a properties file. To overload by the
   * class who need it.
   *
   * @param rs name of resource file
   * @throws AdminException
   */
  @Override
  public void initFromProperties(ResourceLocator rs) throws AdminException {
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
   * Called when Admin starts the synchronization
   *
   * @return
   */
  @Override
  public long getDriverActions() {
    if (x509Enabled) {
      return ACTION_MASK_RO | ACTION_X509_USER | ACTION_UPDATE_USER;
    } else {
      return ACTION_MASK_RO | ACTION_UPDATE_USER;
    }
  }

  @Override
  public boolean isSynchroOnLoginEnabled() {
    SilverTrace.info("admin", "LDAPDriver.isSynchroOnLoginEnabled",
        "root.MSG_GEN_ENTER_METHOD", "Enabled = "
        + driverSettings.isSynchroAutomatic() + " - Synchro In Process = "
        + synchroInProcess);
    return driverSettings.isSynchroAutomatic();
  }

  @Override
  public boolean isSynchroOnLoginRecursToGroups() {
    SilverTrace.info("admin", "LDAPDriver.isSynchroOnLoginRecursToGroups",
        "root.MSG_GEN_ENTER_METHOD", "RecursToGroups = "
        + driverSettings.isSynchroRecursToGroups()
        + " - Synchro In Process = " + synchroInProcess);
    return driverSettings.isSynchroRecursToGroups();
  }

  @Override
  public boolean isGroupsInheritProfiles() {
    SilverTrace.info("admin", "LDAPDriver.isGroupsInheritProfiles",
        "root.MSG_GEN_ENTER_METHOD", "GroupsInheritProfiles = "
        + driverSettings.isGroupsInheritProfiles()
        + " - Synchro In Process = " + synchroInProcess);
    return driverSettings.isGroupsInheritProfiles();
  }

  @Override
  public boolean mustImportUsers() {
    SilverTrace.info("admin", "LDAPDriver.mustImportUsers",
        "root.MSG_GEN_ENTER_METHOD", "MustImportUsers = "
        + driverSettings.mustImportUsers() + " - Synchro In Process = "
        + synchroInProcess);
    return driverSettings.mustImportUsers();
  }

  @Override
  public boolean isSynchroThreaded() {
    SilverTrace.info("admin", "LDAPDriver.isGroupsInheritProfiles",
        "root.MSG_GEN_ENTER_METHOD", "GroupsInheritProfiles = "
        + driverSettings.isGroupsInheritProfiles()
        + " - Synchro In Process = " + synchroInProcess);
    return driverSettings.isSynchroThreaded();
  }

  @Override
  public String getTimeStamp(String minTimeStamp) throws AdminException {
    if (driverSettings.getTimeStampVar().length() > 0) {
      String ld = LDAPUtility.openConnection(driverSettings);
      AbstractLDAPTimeStamp timeStampU, timeStampG;

      SilverTrace.info("admin", "LDAPDriver.getTimeStamp",
          "root.MSG_GEN_ENTER_METHOD");
      try {
        timeStampU = userTranslator.getMaxTimeStamp(ld, minTimeStamp);
        timeStampG = groupTranslator.getMaxTimeStamp(ld, minTimeStamp);
        SilverTrace.info("admin", "LDAPDriver.getTimeStamp",
            "root.MSG_GEN_PARAM_VALUE", "timeStampU=" + timeStampU
            + " AND timeStampG=" + timeStampG);
        if (timeStampU.compareTo(timeStampG) >= 0) {
          return timeStampU.toString();
        } else {
          return timeStampG.toString();
        }
      } finally {
        LDAPUtility.closeConnection(ld);
      }
    } else {
      return "0";
    }
  }

  @Override
  public String getTimeStampField() {
    String timeStampField = driverSettings.getTimeStampVar();
    if (timeStampField != null && timeStampField.trim().length() > 0) {
      return timeStampField;
    } else {
      return null;
    }
  }

  @Override
  public UserDetail[] getAllChangedUsers(String fromTimeStamp,
      String toTimeStamp) throws AdminException {
    String ld = LDAPUtility.openConnection(driverSettings);
    UserDetail[] usersReturned;

    SilverTrace.info("admin", "LDAPDriver.getAllChangedUsers()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      if (driverSettings.getTimeStampVar().length() > 0) {
        usersReturned = userTranslator.getAllUsers(ld, "(|(&("
            + driverSettings.getTimeStampVar() + ">=" + fromTimeStamp + ")("
            + driverSettings.getTimeStampVar() + "<=" + toTimeStamp + "))"
            + "(!(" + driverSettings.getTimeStampVar() + "=*)))");
      } else {
        usersReturned = userTranslator.getAllUsers(ld, "");
      }
    } finally {
      LDAPUtility.closeConnection(ld);
    }
    return usersReturned;
  }

  @Override
  public Group[] getAllChangedGroups(String fromTimeStamp, String toTimeStamp)
      throws AdminException {
    String ld = LDAPUtility.openConnection(driverSettings);

    SilverTrace.info("admin", "LDAPDriver.getAllChangedGroups",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      if (driverSettings.getTimeStampVar().length() > 0) {
        return groupTranslator.getAllChangedGroups(ld, "(&("
            + driverSettings.getTimeStampVar() + ">=" + fromTimeStamp + ")("
            + driverSettings.getTimeStampVar() + "<=" + toTimeStamp + "))");
      } else {
        return groupTranslator.getAllChangedGroups(ld, "");
      }
    } finally {
      LDAPUtility.closeConnection(ld);
    }
  }

  /**
   * Called when Admin starts the synchronization
   */
  @Override
  public void beginSynchronization() throws Exception {
    synchroInProcess = true;
    synchroCache.beginSynchronization();
    userTranslator.beginSynchronization();
    groupTranslator.beginSynchronization();
  }

  /**
   * Called when Admin ends the synchronization
   */
  public String endSynchronization() throws Exception {
    StringBuilder valret = new StringBuilder("");

    synchroCache.endSynchronization();
    String result = userTranslator.endSynchronization();
    if (result != null && result.length() > 0) {
      valret.append("LDAP Domain User specific errors :\n").append(result).append("\n\n");
    }
    result = groupTranslator.endSynchronization();
    if (result != null && result.length() > 0) {
      valret.append("LDAP Domain Group specific errors :\n").append(result).append("\n\n");
    }
    synchroInProcess = false;
    return valret.toString();
  }

  /**
   * Import a given user in Database from the reference
   *
   * @param userLogin The User Login to import
   * @return The User object that contain new user information
   */
  @Override
  public UserDetail importUser(String userLogin) throws AdminException {
    String ld = LDAPUtility.openConnection(driverSettings);

    SilverTrace.info("admin", "LDAPDriver.importUser",
        "root.MSG_GEN_ENTER_METHOD", "UserId = " + userLogin);
    try {
      return userTranslator.getUserByLogin(ld, userLogin);
    } finally {
      LDAPUtility.closeConnection(ld);
    }
  }

  /**
   * Remove a given user from database
   *
   * @param userId The user id To remove synchro
   */
  @Override
  public void removeUser(String userId) throws AdminException {
    // In this driver, do nothing
  }

  /**
   * Update user information in database
   *
   * @param userId The User Id to synchronize
   * @return The User object that contain new user information
   */
  @Override
  public UserDetail synchroUser(String userId) throws Exception {
    SilverTrace.info("admin", "LDAPDriver.synchroUser",
        "root.MSG_GEN_ENTER_METHOD", "UserId = " + userId);
    return getUser(userId);
  }

  @Override
  public String createUser(UserDetail user) throws Exception {
    return null;
  }

  @Override
  public void deleteUser(String userId) throws Exception {
  }

  @Override
  public void updateUserFull(UserFull user) throws AdminException {
    String ld = null;

    try {
      ld = LDAPUtility.openConnection(driverSettings);
      LDAPConnection connection = LDAPUtility.getConnection(ld);

      LDAPEntry theEntry = LDAPUtility.getFirstEntryFromSearch(ld,
          driverSettings.getLDAPUserBaseDN(), driverSettings.getScope(),
          driverSettings.getUsersIdFilter(user.getSpecificId()), driverSettings.getUserAttributes());

      if (theEntry == null) {
        throw new AuthenticationBadCredentialException("LDAPDriver.updateUserFull()",
            SilverpeasException.ERROR, "admin.EX_USER_NOT_FOUND", "User=" + user.getSpecificId()
            + ";IdField=" + driverSettings.getUsersIdField());
      }

      String userFullDN = theEntry.getDN();
      List<LDAPModification> modifications = new ArrayList<LDAPModification>();

      // update basic informations (first name, last name and email)
      LDAPAttribute attribute = new LDAPAttribute(driverSettings.getUsersFirstNameField(), user
          .getFirstName());
      modifications.add(new LDAPModification(LDAPModification.REPLACE, attribute));

      attribute = new LDAPAttribute(driverSettings.getUsersLastNameField(), user.getLastName());
      modifications.add(new LDAPModification(LDAPModification.REPLACE, attribute));

      attribute = new LDAPAttribute(driverSettings.getUsersEmailField(), user.geteMail());
      modifications.add(new LDAPModification(LDAPModification.REPLACE, attribute));

      // prepare properties update
      for (String propertyName : user.getPropertiesNames()) {
        DomainProperty property = user.getProperty(propertyName);
        if (property.isUpdateAllowedToAdmin() || property.isUpdateAllowedToUser()) {
          LDAPModification modification = new LDAPModification(LDAPModification.REPLACE,
              new LDAPAttribute(property.getMapParameter(), user.getValue(propertyName)));
          modifications.add(modification);
        }
      }

      // Perform the update
      connection.modify(userFullDN, modifications.toArray(
          new LDAPModification[modifications.size()]));
    } catch (Exception ex) {
      Logger.getLogger(getClass().getSimpleName()).log(Level.SEVERE, ex.getMessage(), ex);
      throw new AdminException("LDAPDriver.updateUserFull()", SilverpeasException.ERROR,
          "admin.EX_LDAP_ACCESS_ERROR", ex);
    } finally {
      try {
        if (ld != null) {
          LDAPUtility.closeConnection(ld);
        }
      } catch (AdminException closeEx) {
        // The exception that could occur in the emergency stop is not interesting
        SilverTrace.error("admin", "LDAPDriver.updateUserFull",
            "root.EX_EMERGENCY_CONNECTION_CLOSE_FAILED", "", closeEx);
      }
    }

  }

  @Override
  public void updateUserDetail(UserDetail user) throws Exception {
  }

  /**
   * Retrieve user information from database
   *
   * @param userId The user id as stored in the database
   * @return The User object that contain new user information
   * @throws com.stratelia.webactiv.beans.admin.AdminException
   */
  @Override
  public UserFull getUserFull(String userId) throws AdminException {
    String ld = LDAPUtility.openConnection(driverSettings);
    SilverTrace.info("admin", "LDAPDriver.getUser", "root.MSG_GEN_ENTER_METHOD",
        "UserId = " + userId);
    try {
      return userTranslator.getUserFull(ld, userId);
    } finally {
      LDAPUtility.closeConnection(ld);
    }
  }

  /**
   * Retrieve user information from database
   *
   * @param userId The user id as stored in the database
   * @return The User object that contain new user information
   * @throws com.stratelia.webactiv.beans.admin.AdminException
   */
  @Override
  public UserDetail getUser(String userId) throws AdminException {
    String ld = LDAPUtility.openConnection(driverSettings);

    SilverTrace.info("admin", "LDAPDriver.getUser",
        "root.MSG_GEN_ENTER_METHOD", "UserId = " + userId);
    try {
      return userTranslator.getUser(ld, userId);
    } finally {
      LDAPUtility.closeConnection(ld);
    }
  }

  /**
   * Retrieve all users from the database
   *
   * @return User[] An array of User Objects that contain users information
   * @throws com.stratelia.webactiv.beans.admin.AdminException
   */
  @Override
  public UserDetail[] getAllUsers() throws AdminException {
    String ld = LDAPUtility.openConnection(driverSettings);
    SilverTrace.info("admin", "LDAPDriver.getAllUsers()", "root.MSG_GEN_ENTER_METHOD");
    try {
      return userTranslator.getAllUsers(ld, "");
    } finally {
      LDAPUtility.closeConnection(ld);
    }
  }

  @Override
  public UserDetail[] getUsersBySpecificProperty(String propertyName,
      String propertyValue) throws AdminException {
    DomainProperty property = getProperty(propertyName);
    if (property == null) {
      // This property is not defined in this domain
      return null;
    } else {
      String ld = LDAPUtility.openConnection(driverSettings);
      try {
        String extraFilter = "(" + property.getMapParameter() + "=" + propertyValue + ")";
        return userTranslator.getAllUsers(ld, extraFilter);
      } finally {
        LDAPUtility.closeConnection(ld);
      }
    }
  }

  @Override
  public UserDetail[] getUsersByQuery(Map<String, String> query) throws AdminException {
    String extraFilter = "";
    for (String propertyName : query.keySet()) {
      extraFilter += "(" + propertyName + "=" + query.get(propertyName) + ")";
    }

    String ld = LDAPUtility.openConnection(driverSettings);
    try {
      return userTranslator.getAllUsers(ld, extraFilter);
    } finally {
      LDAPUtility.closeConnection(ld);
    }
  }

  /**
   * Retrieve user's groups
   *
   * @param userId The user id as stored in the database
   * @return The User's groups specific Ids
   */
  @Override
  public String[] getUserMemberGroupIds(String userId) throws AdminException {
    String ld = LDAPUtility.openConnection(driverSettings);

    SilverTrace.info("admin", "LDAPDriver.getUserMemberGroupIds",
        "root.MSG_GEN_ENTER_METHOD", "userId = " + userId);
    try {
      return groupTranslator.getUserMemberGroupIds(ld, userId);
    } finally {
      LDAPUtility.closeConnection(ld);
    }
  }

  /**
   * Import a given group in Database from the reference
   *
   * @param groupName The group name to import
   * @return The group object that contain new group information
   */
  @Override
  public Group importGroup(String groupName) throws AdminException {
    String ld = LDAPUtility.openConnection(driverSettings);
    SilverTrace.info("admin", "LDAPDriver.getGroup",
        "root.MSG_GEN_ENTER_METHOD", "GroupName = " + groupName);
    try {
      return groupTranslator.getGroupByName(ld, groupName);
    } finally {
      LDAPUtility.closeConnection(ld);
    }
  }

  /**
   * Remove a given group from database
   *
   * @param groupId The group id To remove synchro
   */
  @Override
  public void removeGroup(String groupId) throws AdminException {
    // In this driver, do nothing
  }

  /**
   * Update group information in database
   *
   * @param groupId The group Id to synchronize
   * @return The group object that contain new group information
   */
  @Override
  public Group synchroGroup(String groupId) throws AdminException {
    SilverTrace.info("admin", "LDAPDriver.importGroup",
        "root.MSG_GEN_ENTER_METHOD", "GroupId = " + groupId);
    return getGroup(groupId);
  }

  @Override
  public String createGroup(Group m_Group) throws AdminException {
    return null;
  }

  @Override
  public void deleteGroup(String groupId) throws AdminException {
  }

  @Override
  public void updateGroup(Group m_Group) throws AdminException {
  }

  /**
   * Retrieve group information from database
   *
   * @param groupId The group id as stored in the database
   * @return The Group object that contains user information
   */
  @Override
  public Group getGroup(String groupId) throws AdminException {
    String ld = LDAPUtility.openConnection(driverSettings);

    SilverTrace.info("admin", "LDAPDriver.getGroup",
        "root.MSG_GEN_ENTER_METHOD", "GroupId = " + groupId);
    try {
      return groupTranslator.getGroup(ld, groupId);
    } finally {
      LDAPUtility.closeConnection(ld);
    }
  }

  @Override
  public Group getGroupByName(String groupName) throws AdminException {
    return null;
  }

  /**
   * Retrieve all groups contained in the given group
   *
   * @param groupId The group id as stored in the database
   * @return Group[] An array of Group Objects that contain groups information
   */
  @Override
  public Group[] getGroups(String groupId) throws AdminException {
    String ld = LDAPUtility.openConnection(driverSettings);

    SilverTrace.info("admin", "LDAPDriver.getGroups",
        "root.MSG_GEN_ENTER_METHOD", "FatherGroupId = " + groupId);
    try {
      return groupTranslator.getGroups(ld, groupId, "");
    } finally {
      LDAPUtility.closeConnection(ld);
    }
  }

  /**
   * Retrieve all groups from the database
   *
   * @return Group[] An array of Group Objects that contain groups information
   */
  @Override
  public Group[] getAllGroups() throws AdminException {
    String ld = LDAPUtility.openConnection(driverSettings);

    SilverTrace.info("admin", "LDAPDriver.getAllGroups",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return groupTranslator.getAllGroups(ld, "");
    } finally {
      LDAPUtility.closeConnection(ld);
    }
  }

  /**
   * Retrieve all root groups from the database
   *
   * @return Group[] An array of Group Objects that contain root groups information
   */
  @Override
  public Group[] getAllRootGroups() throws AdminException {
    String ld = LDAPUtility.openConnection(driverSettings);

    SilverTrace.info("admin", "LDAPDriver.getAllRootGroups",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      return groupTranslator.getGroups(ld, null, "");
    } finally {
      LDAPUtility.closeConnection(ld);
    }
  }

  @Override
  public String[] getGroupMemberGroupIds(String groupId) throws AdminException {
    String ld = LDAPUtility.openConnection(driverSettings);

    SilverTrace.info("admin", "LDAPDriver.getGroupMemberGroupIds",
        "root.MSG_GEN_ENTER_METHOD", "groupId = " + groupId);
    try {
      return groupTranslator.getGroupMemberGroupIds(ld, groupId);
    } finally {
      LDAPUtility.closeConnection(ld);
    }
  }

  /**
   * Start a new transaction
   *
   * @param bAutoCommit Specifies is transaction is automatically committed (without explicit
   * 'commit' statement)
   */
  @Override
  public void startTransaction(boolean bAutoCommit) {
    // Access in read only -> no need to support transaction mode
  }

  /**
   * Commit transaction
   */
  @Override
  public void commit() throws AdminException {
    // Access in read only -> no need to support transaction mode
  }

  /**
   * Rollback transaction
   */
  @Override
  public void rollback() throws AdminException {
    // Access in read only -> no need to support transaction mode
  }

  @Override
  public List<String> getUserAttributes() throws AdminException {
    return Arrays.asList(userTranslator.getUserAttributes());
  }

  @Override
  public void resetPassword(UserDetail user, String password) throws Exception {
    // Access in read only
  }

  @Override
  public void resetEncryptedPassword(UserDetail user, String encryptedPassword) throws Exception {
    // Access in read only
  }

}
