/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.domain;

import org.silverpeas.core.admin.domain.model.DomainProperty;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.util.SettingBundle;

import java.util.List;
import java.util.Map;

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
            ACTION_REMOVE_USER | ACTION_IMPORT_GROUP | ACTION_SYNCHRO_GROUP | ACTION_REMOVE_GROUP;
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

  String getTimeStamp(String minTimeStamp) throws AdminException;

  String getTimeStampField() throws AdminException;

  boolean isX509CertificateEnabled();

  UserDetail[] getAllChangedUsers(String fromTimeStamp, String toTimeStamp) throws AdminException;

  GroupDetail[] getAllChangedGroups(String fromTimeStamp, String toTimeStamp) throws AdminException;

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

  UserDetail getUser(String userId) throws AdminException;

  /**
   * Retrieve user information from database
   * @param userId The user id as stored in the database
   * @return The full User object that contain ALL user informations
   */
  UserFull getUserFull(String userId) throws AdminException;

  String[] getUserMemberGroupIds(String userId) throws AdminException;

  UserDetail[] getAllUsers() throws AdminException;

  UserDetail[] getUsersBySpecificProperty(String propertyName, String value) throws AdminException;

  UserDetail[] getUsersByQuery(Map<String, String> query) throws AdminException;

  GroupDetail importGroup(String groupName) throws AdminException;

  void removeGroup(String groupId) throws AdminException;

  GroupDetail synchroGroup(String groupId) throws AdminException;

  String createGroup(GroupDetail group) throws AdminException;

  void deleteGroup(String groupId) throws AdminException;

  void updateGroup(GroupDetail group) throws AdminException;

  GroupDetail getGroup(String groupId) throws AdminException;

  GroupDetail getGroupByName(String groupName) throws AdminException;

  GroupDetail[] getGroups(String groupId) throws AdminException;

  GroupDetail[] getAllGroups() throws AdminException;

  GroupDetail[] getAllRootGroups() throws AdminException;

  String[] getGroupMemberGroupIds(String groupId) throws AdminException;

  List<String> getUserAttributes() throws AdminException;

  void resetPassword(UserDetail user, String password) throws AdminException;

  void resetEncryptedPassword(UserDetail user, String encryptedPassword) throws AdminException;

}
