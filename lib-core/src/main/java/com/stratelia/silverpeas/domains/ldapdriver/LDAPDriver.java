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

package com.stratelia.silverpeas.domains.ldapdriver;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPModification;
import com.stratelia.silverpeas.authentication.AuthenticationBadCredentialException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AbstractDomainDriver;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.DomainProperty;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Domain driver for LDAP access. Could be used to access any type of LDAP DB (even exchange)
 * IMPORTANT : For the moment, this is a read-only driver (not possible to add, remove or update a
 * group or a user.
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
   */
  public void initFromProperties(ResourceLocator rs) throws Exception {
    driverSettings.initFromProperties(rs);
    synchroCache.init(driverSettings);
    userTranslator = driverSettings.newLDAPUser();
    userTranslator.init(driverSettings, this, synchroCache);
    groupTranslator = driverSettings.newLDAPGroup();
    groupTranslator.init(driverSettings, synchroCache);
  }

  public void addPropertiesToImport(List<DomainProperty> props) {
    addPropertiesToImport(props, null);
  }

  public void addPropertiesToImport(List<DomainProperty> props, Map<String, String> descriptions) {
    DomainProperty property = new DomainProperty();
    property.setName("lastName");
    property.setMapParameter(driverSettings.getUsersLastNameField());
    if (descriptions != null) {
      property.setDescription(descriptions.get("lastName"));
    }
    props.add(property);

    property = new DomainProperty();
    property.setName("firstName");
    property.setMapParameter(driverSettings.getUsersFirstNameField());
    if (descriptions != null) {
      property.setDescription(descriptions.get("firstName"));
    }
    props.add(property);

    property = new DomainProperty();
    property.setName("email");
    property.setMapParameter(driverSettings.getUsersEmailField());
    if (descriptions != null) {
      property.setDescription(descriptions.get("email"));
    }
    props.add(property);

    property = new DomainProperty();
    property.setName("login");
    property.setMapParameter(driverSettings.getUsersLoginField());
    if (descriptions != null) {
      property.setDescription(descriptions.get("login"));
    }
    props.add(property);
  }

  /**
   * Called when Admin starts the synchronization
   */
  public long getDriverActions() {
    if (x509Enabled) {
      return ACTION_MASK_RO | ACTION_X509_USER | ACTION_UPDATE_USER;
    } else {
      return ACTION_MASK_RO | ACTION_UPDATE_USER;
    }
  }

  public boolean isSynchroOnLoginEnabled() {
    SilverTrace.info("admin", "LDAPDriver.isSynchroOnLoginEnabled",
        "root.MSG_GEN_ENTER_METHOD", "Enabled = "
        + driverSettings.isSynchroAutomatic() + " - Synchro In Process = "
        + synchroInProcess);
    return driverSettings.isSynchroAutomatic();
  }

  public boolean isSynchroOnLoginRecursToGroups() {
    SilverTrace.info("admin", "LDAPDriver.isSynchroOnLoginRecursToGroups",
        "root.MSG_GEN_ENTER_METHOD", "RecursToGroups = "
        + driverSettings.isSynchroRecursToGroups()
        + " - Synchro In Process = " + synchroInProcess);
    return driverSettings.isSynchroRecursToGroups();
  }

  public boolean isGroupsInheritProfiles() {
    SilverTrace.info("admin", "LDAPDriver.isGroupsInheritProfiles",
        "root.MSG_GEN_ENTER_METHOD", "GroupsInheritProfiles = "
        + driverSettings.isGroupsInheritProfiles()
        + " - Synchro In Process = " + synchroInProcess);
    return driverSettings.isGroupsInheritProfiles();
  }

  public boolean mustImportUsers() {
    SilverTrace.info("admin", "LDAPDriver.mustImportUsers",
        "root.MSG_GEN_ENTER_METHOD", "MustImportUsers = "
        + driverSettings.mustImportUsers() + " - Synchro In Process = "
        + synchroInProcess);
    return driverSettings.mustImportUsers();
  }

  public boolean isSynchroThreaded() {
    SilverTrace.info("admin", "LDAPDriver.isGroupsInheritProfiles",
        "root.MSG_GEN_ENTER_METHOD", "GroupsInheritProfiles = "
        + driverSettings.isGroupsInheritProfiles()
        + " - Synchro In Process = " + synchroInProcess);
    return driverSettings.isSynchroThreaded();
  }

  public String getTimeStamp(String minTimeStamp) throws Exception {
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

  public String getTimeStampField() throws Exception {
    String timeStampField = driverSettings.getTimeStampVar();
    if (timeStampField != null && timeStampField.trim().length() > 0) {
      return timeStampField;
    } else {
      return null;
    }
  }

  public UserDetail[] getAllChangedUsers(String fromTimeStamp,
      String toTimeStamp) throws Exception {
    String ld = LDAPUtility.openConnection(driverSettings);
    UserDetail[] usersReturned;

    SilverTrace.info("admin", "LDAPDriver.getAllChangedUsers()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      if (driverSettings.getTimeStampVar().length() > 0) {
        usersReturned = userTranslator.getAllUsers(ld, "(&("
            + driverSettings.getTimeStampVar() + ">=" + fromTimeStamp + ")("
            + driverSettings.getTimeStampVar() + "<=" + toTimeStamp + "))");
      } else {
        usersReturned = userTranslator.getAllUsers(ld, "");
      }
    } finally {
      LDAPUtility.closeConnection(ld);
    }
    return usersReturned;
  }

  public Group[] getAllChangedGroups(String fromTimeStamp, String toTimeStamp)
      throws Exception {
    String ld = LDAPUtility.openConnection(driverSettings);
    Group[] groupsReturned;

    SilverTrace.info("admin", "LDAPDriver.getAllChangedGroups",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      if (driverSettings.getTimeStampVar().length() > 0) {
        groupsReturned = groupTranslator.getAllChangedGroups(ld, "(&("
            + driverSettings.getTimeStampVar() + ">=" + fromTimeStamp + ")("
            + driverSettings.getTimeStampVar() + "<=" + toTimeStamp + "))");
      } else {
        groupsReturned = groupTranslator.getAllChangedGroups(ld, "");
      }
    } finally {
      LDAPUtility.closeConnection(ld);
    }
    return groupsReturned;
  }

  /**
   * Called when Admin starts the synchronization
   */
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
    if ((result != null) && (result.length() > 0)) {
      valret.append("LDAP Domain User specific errors :\n").append(result).append("\n\n");
    }
    result = groupTranslator.endSynchronization();
    if ((result != null) && (result.length() > 0)) {
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
  public UserDetail importUser(String userLogin) throws Exception {
    String ld = LDAPUtility.openConnection(driverSettings);
    UserDetail userReturned;

    SilverTrace.info("admin", "LDAPDriver.importUser",
        "root.MSG_GEN_ENTER_METHOD", "UserId = " + userLogin);
    try {
      userReturned = userTranslator.getUserByLogin(ld, userLogin);
    } finally {
      LDAPUtility.closeConnection(ld);
    }
    return userReturned;
  }

  /**
   * Remove a given user from database
   *
   * @param userId The user id To remove synchro
   */
  public void removeUser(String userId) throws Exception {
    // In this driver, do nothing
  }

  /**
   * Update user information in database
   *
   * @param userId The User Id to synchronize
   * @return The User object that contain new user information
   */
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
  public void updateUserFull(UserFull user) throws Exception {
    String userFullDN = null;
    String ld = null;

    try {
      ld = LDAPUtility.openConnection(driverSettings);
      LDAPConnection connection = LDAPUtility.getConnection(ld);

      LDAPEntry theEntry = null;
      theEntry = LDAPUtility.getFirstEntryFromSearch(ld, driverSettings
          .getLDAPUserBaseDN(), driverSettings.getScope(), driverSettings
          .getUsersIdFilter(user.getSpecificId()), driverSettings.getUserAttributes());


      if (theEntry == null) {
        throw new AuthenticationBadCredentialException(
            "LDAPDriver.updateUserFull()", SilverpeasException.ERROR,
            "admin.EX_USER_NOT_FOUND", "User=" + user.getSpecificId()
            + ";IdField=" + driverSettings.getUsersIdField());
      }

      userFullDN = theEntry.getDN();

      // prepare properties update
      List<LDAPModification> modifications = new ArrayList<LDAPModification>();
      for (String propertyName : user.getPropertiesNames()) {
        DomainProperty property = user.getProperty(propertyName);
        if (property.isUpdateAllowedToAdmin() || property.isUpdateAllowedToUser()) {
          LDAPModification modification = new LDAPModification(LDAPModification.REPLACE,
              new LDAPAttribute(property.getMapParameter(),
                  user.getValue(propertyName)));
          modifications.add(modification);
        }
      }

      // Perform the update
      connection
          .modify(userFullDN, modifications.toArray(new LDAPModification[modifications.size()]));
    } catch (Exception ex) {
      throw new AdminException(
          "LDAPDriver.updateUserFull()",
          SilverpeasException.ERROR, "admin.EX_LDAP_ACCESS_ERROR", ex);
    } finally {
      try {
        if (ld != null) {
          LDAPUtility.closeConnection(ld);
        }
      } catch (AdminException closeEx) {
        // The exception that could
        // occur in the emergency stop is not interesting
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
   */
  public UserFull getUserFull(String userId) throws Exception {
    String ld = LDAPUtility.openConnection(driverSettings);
    UserFull userReturned;

    SilverTrace.info("admin", "LDAPDriver.getUser",
        "root.MSG_GEN_ENTER_METHOD", "UserId = " + userId);
    try {
      userReturned = userTranslator.getUserFull(ld, userId);
    } finally {
      LDAPUtility.closeConnection(ld);
    }
    return userReturned;
  }

  /**
   * Retrieve user information from database
   *
   * @param userId The user id as stored in the database
   * @return The User object that contain new user information
   */
  public UserDetail getUser(String userId) throws Exception {
    String ld = LDAPUtility.openConnection(driverSettings);
    UserDetail userReturned;

    SilverTrace.info("admin", "LDAPDriver.getUser",
        "root.MSG_GEN_ENTER_METHOD", "UserId = " + userId);
    try {
      userReturned = userTranslator.getUser(ld, userId);
    } finally {
      LDAPUtility.closeConnection(ld);
    }
    return userReturned;
  }

  /**
   * Retrieve all users from the database
   *
   * @return User[] An array of User Objects that contain users information
   */
  public UserDetail[] getAllUsers() throws Exception {
    String ld = LDAPUtility.openConnection(driverSettings);
    UserDetail[] usersReturned;

    SilverTrace.info("admin", "LDAPDriver.getAllUsers()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      usersReturned = userTranslator.getAllUsers(ld, "");
    } finally {
      LDAPUtility.closeConnection(ld);
    }
    return usersReturned;
  }

  public UserDetail[] getUsersBySpecificProperty(String propertyName,
      String propertyValue) throws Exception {
    DomainProperty property = getProperty(propertyName);
    if (property == null) {
      // This property is not defined in this domain
      return null;
    } else {
      String ld = LDAPUtility.openConnection(driverSettings);
      UserDetail[] usersReturned;
      try {
        String extraFilter = "(" + property.getMapParameter() + "="
            + propertyValue + ")";
        usersReturned = userTranslator.getAllUsers(ld, extraFilter);
      } finally {
        LDAPUtility.closeConnection(ld);
      }
      return usersReturned;
    }
  }

  public UserDetail[] getUsersByQuery(Map<String, String> query) throws Exception {
    String extraFilter = "";
    Iterator<String> properties = query.keySet().iterator();
    String propertyName = null;
    while (properties.hasNext()) {
      propertyName = properties.next();
      extraFilter += "(" + propertyName + "=" + query.get(propertyName) + ")";
    }

    String ld = LDAPUtility.openConnection(driverSettings);
    UserDetail[] usersReturned;
    try {
      usersReturned = userTranslator.getAllUsers(ld, extraFilter);
    } finally {
      LDAPUtility.closeConnection(ld);
    }
    return usersReturned;
  }

  /**
   * Retrieve user's groups
   *
   * @param userId The user id as stored in the database
   * @return The User's groups specific Ids
   */
  public String[] getUserMemberGroupIds(String userId) throws Exception {
    String ld = LDAPUtility.openConnection(driverSettings);
    String[] groupsReturned;

    SilverTrace.info("admin", "LDAPDriver.getUserMemberGroupIds",
        "root.MSG_GEN_ENTER_METHOD", "userId = " + userId);
    try {
      groupsReturned = groupTranslator.getUserMemberGroupIds(ld, userId);
    } finally {
      LDAPUtility.closeConnection(ld);
    }
    return groupsReturned;
  }

  /**
   * Import a given group in Database from the reference
   *
   * @param groupName The group name to import
   * @return The group object that contain new group information
   */
  public Group importGroup(String groupName) throws Exception {
    String ld = LDAPUtility.openConnection(driverSettings);
    Group groupReturned;

    SilverTrace.info("admin", "LDAPDriver.getGroup",
        "root.MSG_GEN_ENTER_METHOD", "GroupName = " + groupName);
    try {
      groupReturned = groupTranslator.getGroupByName(ld, groupName);
    } finally {
      LDAPUtility.closeConnection(ld);
    }
    return groupReturned;
  }

  /**
   * Remove a given group from database
   *
   * @param groupId The group id To remove synchro
   */
  public void removeGroup(String groupId) throws Exception {
    // In this driver, do nothing
  }

  /**
   * Update group information in database
   *
   * @param groupId The group Id to synchronize
   * @return The group object that contain new group information
   */
  public Group synchroGroup(String groupId) throws Exception {
    SilverTrace.info("admin", "LDAPDriver.importGroup",
        "root.MSG_GEN_ENTER_METHOD", "GroupId = " + groupId);
    return getGroup(groupId);
  }

  @Override
  public String createGroup(Group m_Group) throws Exception {
    return null;
  }

  @Override
  public void deleteGroup(String groupId) throws Exception {

  }

  @Override
  public void updateGroup(Group m_Group) throws Exception {

  }

  /**
   * Retrieve group information from database
   *
   * @param groupId The group id as stored in the database
   * @return The Group object that contains user information
   */
  public Group getGroup(String groupId) throws Exception {
    String ld = LDAPUtility.openConnection(driverSettings);
    Group groupReturned;

    SilverTrace.info("admin", "LDAPDriver.getGroup",
        "root.MSG_GEN_ENTER_METHOD", "GroupId = " + groupId);
    try {
      groupReturned = groupTranslator.getGroup(ld, groupId);
    } finally {
      LDAPUtility.closeConnection(ld);
    }
    return groupReturned;
  }

  @Override
  public Group getGroupByName(String groupName) throws Exception {
    return null;
  }

  /**
   * Retrieve all groups contained in the given group
   *
   * @param groupId The group id as stored in the database
   * @return Group[] An array of Group Objects that contain groups information
   */
  public Group[] getGroups(String groupId) throws Exception {
    String ld = LDAPUtility.openConnection(driverSettings);
    Group[] groupsReturned;

    SilverTrace.info("admin", "LDAPDriver.getGroups",
        "root.MSG_GEN_ENTER_METHOD", "FatherGroupId = " + groupId);
    try {
      groupsReturned = groupTranslator.getGroups(ld, groupId, "");
    } finally {
      LDAPUtility.closeConnection(ld);
    }
    return groupsReturned;
  }

  /**
   * Retrieve all groups from the database
   *
   * @return Group[] An array of Group Objects that contain groups information
   */
  public Group[] getAllGroups() throws Exception {
    String ld = LDAPUtility.openConnection(driverSettings);
    Group[] groupsReturned;

    SilverTrace.info("admin", "LDAPDriver.getAllGroups",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      groupsReturned = groupTranslator.getAllGroups(ld, "");
    } finally {
      LDAPUtility.closeConnection(ld);
    }
    return groupsReturned;
  }

  /**
   * Retrieve all root groups from the database
   *
   * @return Group[] An array of Group Objects that contain root groups information
   */
  public Group[] getAllRootGroups() throws Exception {
    String ld = LDAPUtility.openConnection(driverSettings);
    Group[] groupsReturned;

    SilverTrace.info("admin", "LDAPDriver.getAllRootGroups",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      groupsReturned = groupTranslator.getGroups(ld, null, "");
    } finally {
      LDAPUtility.closeConnection(ld);
    }
    return groupsReturned;
  }

  public String[] getGroupMemberGroupIds(String groupId) throws Exception {
    String ld = LDAPUtility.openConnection(driverSettings);
    String[] groupsReturned;

    SilverTrace.info("admin", "LDAPDriver.getGroupMemberGroupIds",
        "root.MSG_GEN_ENTER_METHOD", "groupId = " + groupId);
    try {
      groupsReturned = groupTranslator.getGroupMemberGroupIds(ld, groupId);
    } finally {
      LDAPUtility.closeConnection(ld);
    }
    return groupsReturned;
  }

  /**
   * Start a new transaction
   *
   * @param bAutoCommit Specifies is transaction is automatically committed (without explicit
   *                    'commit' statement)
   */
  public void startTransaction(boolean bAutoCommit) throws Exception {
    // Access in read only -> no need to support transaction mode
  }

  /**
   * Commit transaction
   */
  public void commit() throws Exception {
    // Access in read only -> no need to support transaction mode
  }

  /**
   * Rollback transaction
   */
  public void rollback() throws Exception {
    // Access in read only -> no need to support transaction mode
  }

  public List<String> getUserAttributes() throws Exception {
    return Arrays.asList(userTranslator.getUserAttributes());
  }

}
