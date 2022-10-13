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
package org.silverpeas.core.admin.domain;

import org.silverpeas.core.admin.domain.model.DomainProperty;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.util.SettingBundle;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;

public interface DomainDriver {

  class ActionConstants {

    /**
     * No possible actions Mask
     * @see #getDriverActions
     */
    public static final long ACTION_NONE = 0x00000000;
    /**
     * Read Users' infos action Mask
     * @see #getDriverActions
     */
    public static final long ACTION_READ_USER = 0x00000001;
    /**
     * Read Groups' infos action Mask
     * @see #getDriverActions
     */
    public static final long ACTION_READ_GROUP = 0x00000002;
    /**
     * Update Users' infos action Mask
     * @see #getDriverActions
     */
    public static final long ACTION_UPDATE_USER = 0x00000004;
    /**
     * Update Groups' infos action Mask
     * @see #getDriverActions
     */
    public static final long ACTION_UPDATE_GROUP = 0x00000008;
    /**
     * Create User action Mask
     * @see #getDriverActions
     */
    public static final long ACTION_CREATE_USER = 0x00000010;
    /**
     * Create GroupDetail action Mask
     * @see #getDriverActions
     */
    public static final long ACTION_CREATE_GROUP = 0x00000020;
    /**
     * Delete User action Mask
     * @see #getDriverActions
     */
    public static final long ACTION_DELETE_USER = 0x00000040;
    /**
     * Delete GroupDetail action Mask
     * @see #getDriverActions
     */
    public static final long ACTION_DELETE_GROUP = 0x00000080;
    /**
     * Add/Remove User from group action Mask
     * @see #getDriverActions
     */
    public static final long ACTION_EDIT_USER_IN_GROUP = 0x00000100;
    /**
     * Add a user in Silverpeas DB by synchronization with a reference LDAP DB
     * @see #getDriverActions
     */
    public static final long ACTION_IMPORT_USER = 0x00000200;
    /**
     * Updates user Silverpeas infos from LDAP DB
     * @see #getDriverActions
     */
    public static final long ACTION_SYNCHRO_USER = 0x00000400;
    /**
     * Remove user entry from Silverpeas
     * @see #getDriverActions
     */
    public static final long ACTION_REMOVE_USER = 0x00000800;
    /**
     * Add a group in Silverpeas DB by synchronization with a reference LDAP DB
     * @see #getDriverActions
     */
    public static final long ACTION_IMPORT_GROUP = 0x00001000;
    /**
     * Updates group Silverpeas infos from LDAP DB
     * @see #getDriverActions
     */
    public static final long ACTION_SYNCHRO_GROUP = 0x00002000;
    /**
     * Remove group entry from Silverpeas
     * @see #getDriverActions
     */
    public static final long ACTION_REMOVE_GROUP = 0x00004000;
    /**
     * Create a x509 certificate and store it in server's truststore
     * @see #getDriverActions
     */
    public static final long ACTION_X509_USER = 0x00008000;
    /**
     * Updates user Silverpeas infos from PUSH action
     * @see #getDriverActions
     */
    public static final long ACTION_RECEIVE_USER = 0x00010000;
    /**
     * Updates group Silverpeas infos from PUSH action
     * @see #getDriverActions
     */
    public static final long ACTION_RECEIVE_GROUP = 0x00020000;
    /**
     * Updates user Silverpeas infos from LDAP DB
     * @see #getDriverActions
     */
    public static final long ACTION_UNSYNCHRO_USER = 0x00040000;
    /**
     * Updates group Silverpeas infos from LDAP DB
     * @see #getDriverActions
     */
    public static final long ACTION_UNSYNCHRO_GROUP = 0x00080000;
    /**
     * All available actions Mask
     * @see #getDriverActions
     */
    public static final long ACTION_MASK_ALL = 0xFFFFFFFF;
    public static final long ACTION_MASK_RW =
        ACTION_READ_USER | ACTION_READ_GROUP | ACTION_UPDATE_USER | ACTION_UPDATE_GROUP |
            ACTION_CREATE_USER | ACTION_CREATE_GROUP | ACTION_DELETE_USER | ACTION_DELETE_GROUP |
            ACTION_EDIT_USER_IN_GROUP;
    public static final long ACTION_MASK_RO =
        ACTION_READ_USER | ACTION_READ_GROUP | ACTION_IMPORT_USER | ACTION_SYNCHRO_USER |
            ACTION_UNSYNCHRO_USER | ACTION_REMOVE_USER | ACTION_IMPORT_GROUP |
            ACTION_SYNCHRO_GROUP | ACTION_UNSYNCHRO_GROUP | ACTION_REMOVE_GROUP;
    public static final long ACTION_MASK_RO_PULL_USER = ACTION_READ_USER | ACTION_SYNCHRO_USER;
    public static final long ACTION_MASK_RO_LISTENER =
        ACTION_READ_USER | ACTION_READ_GROUP | ACTION_RECEIVE_USER | ACTION_REMOVE_USER |
            ACTION_RECEIVE_GROUP | ACTION_REMOVE_GROUP;
    public static final long ACTION_MASK_MIXED_GROUPS =
        ACTION_READ_GROUP | ACTION_UPDATE_GROUP | ACTION_CREATE_GROUP | ACTION_DELETE_GROUP |
            ACTION_EDIT_USER_IN_GROUP;

    private ActionConstants() {

    }
  }

  void init(int domainId, String initParam, String authenticationServer) throws AdminException;

  String[] getPropertiesNames();

  DomainProperty getProperty(String propName);

  String[] getMapParameters();

  List<DomainProperty> getPropertiesToImport(String language);

  void addPropertiesToImport(List<DomainProperty> props);

  void addPropertiesToImport(List<DomainProperty> props, Map<String, String> theDescriptions);

  Map<String, String> getPropertiesLabels(String language);

  Map<String, String> getPropertiesDescriptions(String language);

  void initFromProperties(SettingBundle rs) throws AdminException;

  long getDriverActions();

  boolean isSynchroOnLoginEnabled();

  boolean isSynchroThreaded();

  boolean isSynchroOnLoginRecursToGroups();

  boolean isGroupsInheritProfiles();

  boolean mustImportUsers();

  boolean isX509CertificateEnabled();

  void beginSynchronization() throws AdminException;

  boolean isSynchroInProcess() throws AdminException;

  String endSynchronization(boolean cancelSynchro) throws AdminException;

  UserDetail importUser(String userLogin) throws AdminException;

  void removeUser(String userId) throws AdminException;

  UserDetail synchroUser(String userId) throws AdminException;

  String createUser(UserDetail user) throws AdminException;

  void deleteUser(String userId) throws AdminException;

  void updateUserFull(UserFull user) throws AdminException;

  void updateUserDetail(UserDetail user) throws AdminException;

  /**
   * Retrieves common user information from database.
   * @param specificId The user id as stored in the database.
   * @return The full User object that contain ALL user information.
   */
  UserDetail getUser(String specificId) throws AdminException;

  /**
   * Retrieves the common user information from database against the given identifiers.
   * @param specificIds The user ids as stored in the database.
   * @return a list of common User object.
   */
  List<UserDetail> listUsers(Collection<String> specificIds) throws AdminException;

  /**
   * Retrieves common user information from database with the additional data.
   * @param specificId The user id as stored in the database.
   * @return The full User object that contain ALL user information.
   */
  UserFull getUserFull(String specificId) throws AdminException;

  /**
   * Retrieves common user information with the additional data from database against the given
   * identifiers.
   * @param specificIds The user ids as stored in the database.
   * @return a list of full User object.
   */
  List<UserFull> listUserFulls(Collection<String> specificIds) throws AdminException;

  String[] getUserMemberGroupIds(String specificId) throws AdminException;

  UserDetail[] getAllUsers() throws AdminException;

  UserDetail[] getUsersBySpecificProperty(String propertyName, String value) throws AdminException;

  UserDetail[] getUsersByQuery(Map<String, String> query) throws AdminException;

  GroupDetail importGroup(String groupName) throws AdminException;

  void removeGroup(String groupId) throws AdminException;

  GroupDetail synchroGroup(String groupId) throws AdminException;

  String createGroup(GroupDetail group) throws AdminException;

  void deleteGroup(String groupId) throws AdminException;

  void updateGroup(GroupDetail group) throws AdminException;

  GroupDetail getGroup(String specificId) throws AdminException;

  GroupDetail getGroupByName(String groupName) throws AdminException;

  GroupDetail[] getGroups(String groupId) throws AdminException;

  GroupDetail[] getAllGroups() throws AdminException;

  GroupDetail[] getAllRootGroups() throws AdminException;

  String[] getGroupMemberGroupIds(String groupId) throws AdminException;

  List<String> getUserAttributes() throws AdminException;

  void resetPassword(UserDetail user, String password) throws AdminException;

  void resetEncryptedPassword(UserDetail user, String encryptedPassword) throws AdminException;

  /**
   * Gets an optional {@link UserFilterManager} which permits to manage a filter to apply on the
   * user request results obtained from external user account repository.
   * @return an optional {@link UserFilterManager} implementation.
   */
  default Optional<UserFilterManager> getUserFilterManager() {
    return empty();
  }

  /**
   * Definition of a user filter manager.
   */
  interface UserFilterManager {

    /**
     * Gets the rule key.
     * @return a string.
     */
    String getRuleKey();

    /**
     * Gets the current rule.
     * @return a string.
     */
    String getRule();

    /**
     * Validates the given rule by performing a request of all users on external repository.
     * <p>
     *   In case of success, the filtered users are returned.
     * </p>
     * @param rule the rule to validate.
     * @return an array of {@link User}.
     * @throws AdminException in case of validation error.
     */
    User[] validateRule(final String rule) throws AdminException;

    /**
     * Validates the given rule by performing a request of all users on external repository and
     * save it on Silverpeas's domain repository.
     * <p>
     *   In case of success, the filtered users of validation processing are returned.
     * </p>
     * @param rule the rule to validate.
     * @return an array of {@link User}.
     * @throws AdminException in case of validation error.
     */
    User[] saveRule(final String rule) throws AdminException;
  }
}
