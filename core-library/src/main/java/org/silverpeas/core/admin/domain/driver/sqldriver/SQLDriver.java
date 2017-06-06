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

import org.silverpeas.core.admin.domain.AbstractDomainDriver;
import org.silverpeas.core.admin.domain.DomainServiceProvider;
import org.silverpeas.core.admin.domain.model.DomainProperty;
import org.silverpeas.core.admin.domain.quota.UserDomainQuotaKey;
import org.silverpeas.core.admin.persistence.AdminPersistenceException;
import org.silverpeas.core.admin.quota.exception.QuotaException;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.exception.UtilException;
import org.silverpeas.core.security.authentication.password.PasswordEncryption;
import org.silverpeas.core.security.authentication.password.PasswordEncryptionProvider;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.silverpeas.core.SilverpeasExceptionMessages.*;

public class SQLDriver extends AbstractDomainDriver {

  protected Connection openedConnection = null;
  protected boolean inTransaction = false;
  protected SQLSettings drvSettings = new SQLSettings();
  protected SQLUserTable localUserMgr = new SQLUserTable(drvSettings);
  protected SQLGroupTable localGroupMgr = new SQLGroupTable(drvSettings);
  protected SQLGroupUserRelTable localGroupUserRelMgr = new SQLGroupUserRelTable(
      drvSettings);

  /**
   * Constructor
   */
  public SQLDriver() {
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.admin.domain.AbstractDomainDriver#getGroupMemberGroupIds
   * (java.lang.String)
   */
  @Override
  public String[] getGroupMemberGroupIds(String groupId) throws Exception {
    GroupDetail group = getGroup(groupId);
    if ((group != null) && (group.getSuperGroupId() != null)) {
      return new String[] { group.getSuperGroupId() };
    }
    return null;
  }

  /**
   * Virtual method that performs extra initialization from a properties file. To overload by the
   * class who need it.
   */
  @Override
  public void initFromProperties(SettingBundle rs) throws Exception {
    drvSettings.initFromProperties(rs);
  }

  @Override
  public UserDetail[] getAllChangedUsers(String fromTimeStamp, String toTimeStamp)
      throws Exception {
    return new UserDetail[0];
  }

  @Override
  public GroupDetail[] getAllChangedGroups(String fromTimeStamp, String toTimeStamp) throws Exception {
    return new GroupDetail[0];
  }

  @Override
  public UserDetail importUser(String userLogin) throws Exception {
    return null;
  }

  @Override
  public void removeUser(String userId) throws Exception {
  }

  @Override
  public UserDetail synchroUser(String userId) throws Exception {
    return null;
  }

  /**
   * Get an DomainSQL schema from the pool.
   */
  private void openConnection() throws AdminException {
    if (openedConnection == null) {
      try {
        DataSource dataSource = InitialContext.doLookup(drvSettings.getDataSourceJNDIName());
        openedConnection = dataSource.getConnection();
      } catch (Exception e) {
        throw new AdminException(failureOnOpeningConnectionTo("SQL domain"), e);
      }
    }
  }

  /**
   * Release the DomainSQL schema.
   */
  public void closeConnection() throws AdminException {
    if (openedConnection != null) {
      try {
        openedConnection.close();
      } catch (Exception e) {
        throw new AdminException(failureOnClosingConnectionTo("SQL domain"), e);
      } finally {
        openedConnection = null;
      }
    }
  }

  /**
   * @param ud
   * @return String
   */
  @Override
  public String createUser(UserDetail ud) throws Exception {
    try {
      this.openConnection();
      DomainServiceProvider.getUserDomainQuotaService().verify(UserDomainQuotaKey.from(ud));
      int userId = localUserMgr.createUser(openedConnection, ud);
      localUserMgr.updateUserPassword(openedConnection, userId, "");
      localUserMgr.updateUserPasswordValid(openedConnection, userId,
          false);
      return idAsString(userId);
    } catch (QuotaException qe) {
      throw qe;
    } catch (Exception e) {
      throw new AdminException(failureOnAdding("user", ud.getDisplayedName()), e);
    } finally {
      this.closeConnection();
    }
  }


  @Override
  public void resetPassword(UserDetail user, String password) throws Exception {
    PasswordEncryption encryption = PasswordEncryptionProvider.getDefaultPasswordEncryption();
    String encryptedPassword = encryption.encrypt(password);
    effectiveResetPassword(user, encryptedPassword);
  }

  @Override
  public void resetEncryptedPassword(UserDetail user, String encryptedPassword) throws Exception {
    effectiveResetPassword(user, encryptedPassword);
  }

  private void effectiveResetPassword(UserDetail user, String password) throws Exception {
    try {
      this.openConnection();
      localUserMgr.updateUserPassword(openedConnection, idAsInt(user.getSpecificId()), password);
      localUserMgr.updateUserPasswordValid(openedConnection, idAsInt(user.getSpecificId()), true);
    } catch (Exception e) {
      throw new AdminException(
          "Fail to effectively reset password for user " + user.getSpecificId(), e);
    } finally {
      this.closeConnection();
    }
  }


  /**
   * @param userId
   */
  @Override
  public void deleteUser(String userId) throws Exception {
    try {
      this.openConnection();
      localGroupUserRelMgr.removeAllUserRel(openedConnection, idAsInt(userId));
      localUserMgr.deleteUser(openedConnection, idAsInt(userId));
    } catch (Exception e) {
      throw new AdminException(failureOnDeleting("user", userId), e);
    } finally {
      this.closeConnection();
    }
  }

  /**
   * Update the data inside domain<DOMAIN_NAME>_user table
   * @param uf a UserFull object
   */
  @Override
  public void updateUserFull(UserFull uf) throws Exception {
    try {
      this.openConnection();
      localUserMgr.updateUser(openedConnection, uf);
      int userId = idAsInt(uf.getSpecificId());
      // MAJ Specific Properties (except password)
      String[] specificProps = getPropertiesNames();
      for (String specificProp : specificProps) {
        DomainProperty theProp = getProperty(specificProp);
        localUserMgr.updateUserSpecificProperty(openedConnection, userId, theProp,
            uf.getValue(theProp.getName()));
      }

      // PWD specific treatment
      if (drvSettings.isUserPasswordAvailable()) {
        if (StringUtil.isDefined(uf.getPassword())) {
          String existingPassword = localUserMgr.getUserPassword(openedConnection, userId);
          if (!existingPassword.equals(uf.getPassword())) {
            PasswordEncryption encryption =
                PasswordEncryptionProvider.getDefaultPasswordEncryption();
            String encryptedPassword = encryption.encrypt(uf.getPassword());
            localUserMgr.updateUserPassword(openedConnection, userId, encryptedPassword);
          }
        }
        localUserMgr.updateUserPasswordValid(openedConnection, userId, uf.isPasswordValid());
      }
    } catch (Exception e) {
      throw new AdminException(failureOnUpdate("user", uf.getSpecificId()), e);
    } finally {
      this.closeConnection();
    }
  }

  /**
   * @param ud a UserDetail object which contains all the data to update inside
   * domain<DOMAIN_NAME>_user table
   */
  @Override
  public void updateUserDetail(UserDetail ud) throws Exception {
    try {
      this.openConnection();
      localUserMgr.updateUser(openedConnection, ud);
    } catch (Exception e) {
      throw new AdminException(failureOnUpdate("user", ud.getSpecificId()), e);
    } finally {
      this.closeConnection();
    }
  }

  /**
   * @param userId
   * @return User
   */
  @Override
  public UserDetail getUser(String userId) throws Exception {
    try {
      this.openConnection();
      return localUserMgr.getUser(openedConnection, idAsInt(userId));
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("user", userId), e);
    } finally {
      this.closeConnection();
    }
  }

  @Override
  public UserFull getUserFull(String id) throws Exception {
    try {
      this.openConnection();
      int userId = idAsInt(id);
      UserFull uf = null;
      UserDetail ud = localUserMgr.getUser(openedConnection, userId);
      if (ud != null) {
        uf = new UserFull(this, ud);

        if (drvSettings.isUserPasswordAvailable()) {
          uf.setPasswordAvailable(true);
          uf.setPassword(localUserMgr.getUserPassword(
              openedConnection, userId));
          uf.setPasswordValid(localUserMgr.getUserPasswordValid(
              openedConnection, userId));
        }
        String[] specificProps = getPropertiesNames();
        DomainProperty theProp;
        String value;

        for (String specificProp : specificProps) {
          theProp = getProperty(specificProp);
          value = localUserMgr.getUserSpecificProperty(openedConnection, userId, theProp);
          uf.setValue(theProp.getName(), value);
        }
      }
      return uf;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("user", id), e);
    } finally {
      this.closeConnection();
    }
  }

  @Override
  public String[] getUserMemberGroupIds(String userId) throws Exception {
    return new String[0];
  }

  /**
   * @return User[]
   */
  public UserDetail[] getAllUsers() throws Exception {
    try {
      this.openConnection();
      List<UserDetail> users = localUserMgr.getAllUsers(openedConnection);
      return users.toArray(new UserDetail[users.size()]);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("all users", ""), e);
    } finally {
      this.closeConnection();
    }
  }

  public UserDetail[] getUsersBySpecificProperty(String propertyName,
      String propertyValue) throws Exception {
    DomainProperty property = getProperty(propertyName);
    if (property == null) {
      // This property is not defined in this domain
      return null;
    } else {
      try {
        this.openConnection();
        List<UserDetail> users = localUserMgr.getUsersBySpecificProperty(openedConnection,
            property.getMapParameter(), propertyValue);
        return users.toArray(new UserDetail[users.size()]);
      } catch (Exception e) {
        throw new AdminException(failureOnGetting("all users by property", propertyName), e);
      } finally {
        this.closeConnection();
      }
    }
  }

  @Override
  public UserDetail[] getUsersByQuery(Map<String, String> query) throws Exception {
    return new UserDetail[0];
  }

  @Override
  public GroupDetail importGroup(String groupName) throws Exception {
    return null;
  }

  @Override
  public void removeGroup(String groupId) throws Exception {
  }

  @Override
  public GroupDetail synchroGroup(String groupId) throws Exception {
    return null;
  }

  /**
   * @param group
   * @return String
   */
  public String createGroup(GroupDetail group) throws Exception {
    try {
      this.openConnection();
      int theGrpId = localGroupMgr.createGroup(openedConnection, group);
      String theGrpIdStr = Integer.toString(theGrpId);
      // Add the users in the group
      String[] asUserIds = group.getUserIds();
      for (String asUserId : asUserIds) {
        if (StringUtil.isDefined(asUserId)) {
          addUserInGroup(asUserId, theGrpIdStr);
        }
      }
      return theGrpIdStr;
    } catch (Exception e) {
      throw new AdminException(failureOnAdding("group", group.getName()), e);
    } finally {
      this.closeConnection();
    }
  }

  /**
   * @param groupId
   */
  public void deleteGroup(String groupId) throws Exception {
    try {
      int gid;

      this.openConnection();
      List<GroupDetail> allSubGroups = new ArrayList<>();
      allSubGroups.add(localGroupMgr.getGroup(openedConnection, idAsInt(groupId)));

      while (allSubGroups.size() > 0) {
        gid = idAsInt(allSubGroups.remove(0).getSpecificId());
        // Add sub groups
        allSubGroups.addAll(localGroupMgr.getDirectSubGroups(
            openedConnection, gid));
        // Remove the group
        localGroupUserRelMgr.removeAllGroupRel(openedConnection, gid);
        localGroupMgr.deleteGroup(openedConnection, gid);
      }
    } catch (Exception e) {
      throw new AdminException(failureOnDeleting("group", groupId), e);
    } finally {
      this.closeConnection();
    }
  }

  /**
   * @param group
   */
  public void updateGroup(GroupDetail group) throws Exception {
    List<String> alAddUsers = new ArrayList<>();

    try {
      if (group == null || group.getName().length() == 0 ||
          group.getSpecificId().length() == 0) {
        throw new AdminException(undefined("group"));
      }

      int groupId = idAsInt(group.getSpecificId());
      this.openConnection();

      // Update the group node
      localGroupMgr.updateGroup(openedConnection, group);

      // Update the users if necessary
      List<String> asOldUsersId = localGroupUserRelMgr.getDirectUserIdsOfGroup(openedConnection,
          groupId);
      // Compute the users list
      String[] asNewUsersId = group.getUserIds();

      for (String anAsNewUsersId : asNewUsersId) {
        if (!asOldUsersId.remove(anAsNewUsersId)) {
          alAddUsers.add(anAsNewUsersId);
        }
      }

      // Remove the users that are not in this group anymore
      for (String anAsOldUsersId : asOldUsersId) {
        localGroupUserRelMgr.removeGroupUserRel(openedConnection, groupId, idAsInt(anAsOldUsersId));
      }

      // Add the new users of the group
      for (String alAddUser : alAddUsers) {
        localGroupUserRelMgr.createGroupUserRel(openedConnection, groupId, idAsInt(alAddUser));
      }
    } catch (Exception e) {
      throw new AdminException(failureOnUpdate("group", group.getSpecificId()), e);
    } finally {
      this.closeConnection();
    }
  }

  /**
   * @param groupId
   * @return GroupDetail
   */
  public GroupDetail getGroup(String groupId) throws Exception {
    try {
      this.openConnection();
      GroupDetail valret = localGroupMgr.getGroup(openedConnection, idAsInt(groupId));
      if (valret != null) {
        // Get the selected users for this group
        List<String> asUsersId =
            localGroupUserRelMgr.getDirectUserIdsOfGroup(openedConnection, idAsInt(groupId));
        valret.setUserIds(asUsersId.toArray(new String[asUsersId.size()]));
      }
      return valret;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("group", groupId), e);
    } finally {
      this.closeConnection();
    }
  }

  /**
   * @param groupName
   * @return GroupDetail
   */
  public GroupDetail getGroupByName(String groupName) throws Exception {
    try {
      this.openConnection();
      GroupDetail valret = localGroupMgr.getGroupByName(openedConnection,
          groupName);
      return valret;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("group", groupName), e);
    } finally {
      this.closeConnection();
    }
  }

  /**
   * @param groupId
   * @return GroupDetail[]
   */
  public GroupDetail[] getGroups(String groupId) throws Exception {
    try {
      this.openConnection();
      List<GroupDetail> ar = localGroupMgr.getDirectSubGroups(openedConnection, idAsInt(groupId));

      for (GroupDetail theGroup : ar) {
        // Get the selected users for this group
        List<String> asUsersId = localGroupUserRelMgr.getDirectUserIdsOfGroup(openedConnection,
            idAsInt(theGroup.getSpecificId()));
        theGroup.setUserIds(asUsersId.toArray(new String[asUsersId.size()]));
      }

      return ar.toArray(new GroupDetail[ar.size()]);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("subgroups of group", groupId), e);
    } finally {
      this.closeConnection();
    }
  }

  /**
   * @return GroupDetail[]
   */
  public GroupDetail[] getAllGroups() throws Exception {
    try {
      this.openConnection();
      List<GroupDetail> ar = localGroupMgr.getAllGroups(openedConnection);

      for (GroupDetail theGroup : ar) {
        // Get the selected users for this group
        List<String> asUsersId = localGroupUserRelMgr.getDirectUserIdsOfGroup(openedConnection,
            idAsInt(theGroup.getSpecificId()));
        theGroup.setUserIds(asUsersId.toArray(new String[asUsersId.size()]));
      }

      return ar.toArray(new GroupDetail[ar.size()]);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("all groups", ""), e);
    } finally {
      this.closeConnection();
    }
  }

  /**
   * @return GroupDetail[]
   */
  public GroupDetail[] getAllRootGroups() throws Exception {
    try {
      this.openConnection();
      List<GroupDetail> ar = localGroupMgr.getDirectSubGroups(openedConnection, -1);
      for (GroupDetail theGroup : ar) {
        // Get the selected users for this group
        List<String> asUsersId = localGroupUserRelMgr.getDirectUserIdsOfGroup(openedConnection,
            idAsInt(theGroup.getSpecificId()));
        theGroup.setUserIds(asUsersId.toArray(new String[asUsersId.size()]));
      }

      return ar.toArray(new GroupDetail[ar.size()]);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("all root groups", ""), e);
    } finally {
      this.closeConnection();
    }
  }

  /**
   * @param userId
   * @param groupId
   */
  public void addUserInGroup(String userId, String groupId) throws Exception {
    try {
      this.openConnection();
      localGroupUserRelMgr.createGroupUserRel(openedConnection, idAsInt(groupId), idAsInt(userId));
    } catch (Exception e) {
      throw new AdminException(failureOnAdding("user " + userId, "in group" + groupId), e);
    } finally {
      this.closeConnection();
    }
  }

  /**
   * @param userId
   * @param groupId
   */
  public void removeUserFromGroup(String userId, String groupId)
      throws Exception {
    try {
      this.openConnection();
      localGroupUserRelMgr.removeGroupUserRel(openedConnection,
          idAsInt(groupId), idAsInt(userId));

    } catch (Exception e) {
      throw new AdminException(failureOnDeleting("user " + userId, "in group" + groupId), e);
    } finally {
      this.closeConnection();
    }
  }

  /**
   * Start a new transaction
   */
  @Override
  public void startTransaction(boolean bAutoCommit) {
    try {
      //inTransaction = true;
      openConnection();
      //if (openedConnection != null) {
      //  openedConnection.setAutoCommit(bAutoCommit);
      //}
    } catch (AdminException ex) {
      throw new UtilException("SQLDriver", "startTransaction", ex);
    }
  }

  /**
   * Commit transaction
   */
  public void commit() throws Exception {
    try {
      //if (openedConnection != null) {
      //  openedConnection.commit();
      //}
      //inTransaction = false;
      closeConnection();
    } catch (AdminPersistenceException e) {
      throw new AdminException("SQL Driver Transaction commit failure", e);
    }
  }

  /**
   * Rollback transaction
   */
  public void rollback() throws Exception {
    try {
      /*if (openedConnection != null) {
        openedConnection.rollback();
      }
      inTransaction = false;*/
      closeConnection();
    } catch (AdminPersistenceException e) {
      throw new AdminException("SQL Driver Transaction rollback failure", e);
    }
  }

  public List<String> getUserAttributes() throws Exception {
    return Arrays.asList(getPropertiesNames());
  }
}
