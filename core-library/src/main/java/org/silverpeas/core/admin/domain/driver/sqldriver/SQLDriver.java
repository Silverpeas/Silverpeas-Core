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
package org.silverpeas.core.admin.domain.driver.sqldriver;

import org.silverpeas.core.admin.domain.AbstractDomainDriver;
import org.silverpeas.core.admin.domain.DomainServiceProvider;
import org.silverpeas.core.admin.domain.model.DomainProperty;
import org.silverpeas.core.admin.domain.quota.UserDomainQuotaKey;
import org.silverpeas.core.admin.quota.exception.QuotaException;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.security.authentication.password.PasswordEncryption;
import org.silverpeas.core.security.authentication.password.PasswordEncryptionProvider;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.singleton;
import static org.silverpeas.core.SilverpeasExceptionMessages.*;
import static org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery.unique;

public class SQLDriver extends AbstractDomainDriver {

  public static final String GROUP = "group";
  private DataSource dataSource;
  protected SQLSettings drvSettings = new SQLSettings();
  protected SQLUserTable localUserMgr = new SQLUserTable(drvSettings);
  protected SQLGroupTable localGroupMgr = new SQLGroupTable(drvSettings);
  protected SQLGroupUserRelTable localGroupUserRelMgr = new SQLGroupUserRelTable(
      drvSettings);

  /**
   * Constructor
   */
  protected SQLDriver() {
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.admin.domain.AbstractDomainDriver#getGroupMemberGroupIds
   * (java.lang.String)
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public String[] getGroupMemberGroupIds(String groupId) throws AdminException {
    GroupDetail group = getGroup(groupId);
    if ((group != null) && (group.getSuperGroupId() != null)) {
      return new String[] { group.getSuperGroupId() };
    }
    return ArrayUtil.emptyStringArray();
  }

  /**
   * Virtual method that performs extra initialization from a properties file. To overload by the
   * class who need it.
   */
  @Override
  public void initFromProperties(SettingBundle rs) throws AdminException {
    drvSettings.initFromProperties(rs);
    try {
      dataSource = InitialContext.doLookup(drvSettings.getDataSourceJNDIName());
    } catch (NamingException e) {
      throw new AdminException(e.getMessage(), e);
    }
  }

  @Override
  public UserDetail importUser(String userLogin) throws AdminException {
    return null;
  }

  @Override
  public void removeUser(String userId) throws AdminException {
    // Silverpeas doesn't modify the database behind the SQL domain
  }

  @Override
  public UserDetail synchroUser(String userId) throws AdminException {
    return null;
  }

  /**
   * @param ud
   * @return String
   */
  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public String createUser(UserDetail ud) throws AdminException {
    try(Connection connection = dataSource.getConnection()) {
      DomainServiceProvider.getUserDomainQuotaService().verify(UserDomainQuotaKey.from(ud));
      int userId = localUserMgr.createUser(connection, ud);
      localUserMgr.updateUserPassword(connection, userId, "");
      localUserMgr.updateUserPasswordValid(connection, userId,
          false);
      return idAsString(userId);
    } catch (QuotaException qe) {
      throw new AdminException(qe.getMessage(), qe);
    } catch (Exception e) {
      throw new AdminException(failureOnAdding("user", ud.getDisplayedName()), e);
    }
  }


  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public void resetPassword(UserDetail user, String password) throws AdminException {
    PasswordEncryption encryption = PasswordEncryptionProvider.getDefaultPasswordEncryption();
    String encryptedPassword = encryption.encrypt(password);
    effectiveResetPassword(user, encryptedPassword);
  }

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public void resetEncryptedPassword(UserDetail user, String encryptedPassword) throws AdminException {
    effectiveResetPassword(user, encryptedPassword);
  }

  private void effectiveResetPassword(UserDetail user, String password) throws AdminException {
    try(Connection connection = dataSource.getConnection()) {
      localUserMgr.updateUserPassword(connection, idAsInt(user.getSpecificId()), password);
      localUserMgr.updateUserPasswordValid(connection, idAsInt(user.getSpecificId()), true);
    } catch (Exception e) {
      throw new AdminException(
          "Fail to effectively reset password for user " + user.getSpecificId(), e);
    }
  }


  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public void deleteUser(String userId) throws AdminException {
    try(Connection connection = dataSource.getConnection()) {
      localGroupUserRelMgr.removeAllUserRel(connection, idAsInt(userId));
      localUserMgr.deleteUser(connection, idAsInt(userId));
    } catch (Exception e) {
      throw new AdminException(failureOnDeleting("user", userId), e);
    }
  }

  /**
   * Update the data inside domain &lt;DOMAIN_NAME&gt;_user table
   * @param uf a UserFull object
   */
  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public void updateUserFull(UserFull uf) throws AdminException {
    try(Connection connection = dataSource.getConnection()) {
      localUserMgr.updateUser(connection, uf);
      int userId = idAsInt(uf.getSpecificId());
      // MAJ Specific Properties (except password)
      String[] specificProps = getPropertiesNames();
      for (String specificProp : specificProps) {
        DomainProperty theProp = getProperty(specificProp);
        localUserMgr.updateUserSpecificProperty(connection, userId, theProp,
            uf.getValue(theProp.getName()));
      }

      // PWD specific treatment
      if (drvSettings.isUserPasswordAvailable()) {
        if (StringUtil.isDefined(uf.getPassword())) {
          String existingPassword = localUserMgr.getUserPassword(connection, userId);
          if (!existingPassword.equals(uf.getPassword())) {
            PasswordEncryption encryption =
                PasswordEncryptionProvider.getDefaultPasswordEncryption();
            String encryptedPassword = encryption.encrypt(uf.getPassword());
            localUserMgr.updateUserPassword(connection, userId, encryptedPassword);
          }
        }
        localUserMgr.updateUserPasswordValid(connection, userId, uf.isPasswordValid());
      }
    } catch (Exception e) {
      throw new AdminException(failureOnUpdate("user", uf.getSpecificId()), e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public void updateUserDetail(UserDetail ud) throws AdminException {
    try(Connection connection = dataSource.getConnection()) {
      localUserMgr.updateUser(connection, ud);
    } catch (Exception e) {
      throw new AdminException(failureOnUpdate("user", ud.getSpecificId()), e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public UserDetail getUser(String specificId) throws AdminException {
    return unique(listUsers(singleton(specificId)));
  }

  @Override
  public List<UserDetail> listUsers(final Collection<String> specificIds) throws AdminException {
    try(Connection connection = dataSource.getConnection()) {
      return localUserMgr.getUsers(connection,
          specificIds.stream().map(SQLDriver::idAsInt).collect(Collectors.toList()));
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("user", specificIds), e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public UserFull getUserFull(String id) throws AdminException {
    return unique(listUserFulls(singleton(id)));
  }

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public List<UserFull> listUserFulls(Collection<String> ids) throws AdminException {
    try (Connection connection = dataSource.getConnection()) {
      final List<UserDetail> users = localUserMgr.getUsers(connection,
          ids.stream().map(SQLDriver::idAsInt).collect(Collectors.toList()));
      final List<UserFull> result = new ArrayList<>(users.size());
      for (final UserDetail ud : users) {
        int userId = idAsInt(ud.getSpecificId());
        UserFull uf = new UserFull(this, ud);
        if (drvSettings.isUserPasswordAvailable()) {
          uf.setPasswordAvailable(true);
          uf.setPassword(localUserMgr.getUserPassword(connection, userId));
          uf.setPasswordValid(localUserMgr.getUserPasswordValid(connection, userId));
        }
        String[] specificProps = getPropertiesNames();
        DomainProperty theProp;
        String value;
        for (String specificProp : specificProps) {
          theProp = getProperty(specificProp);
          value = localUserMgr.getUserSpecificProperty(connection, userId, theProp);
          uf.setValue(theProp.getName(), value);
        }
        result.add(uf);
      }
      return result;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("users", ids), e);
    }
  }

  @Override
  public String[] getUserMemberGroupIds(String specificId) throws AdminException {
    return new String[0];
  }

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public UserDetail[] getAllUsers() throws AdminException {
    try(Connection connection = dataSource.getConnection()) {
      List<UserDetail> users = localUserMgr.getAllUsers(connection);
      return users.toArray(new UserDetail[users.size()]);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("all users", ""), e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public UserDetail[] getUsersBySpecificProperty(String propertyName,
      String propertyValue) throws AdminException {
    DomainProperty property = getProperty(propertyName);
    if (property == null) {
      // This property is not defined in this domain
      return new UserDetail[0];
    } else {
      try(Connection connection = dataSource.getConnection()) {
        List<UserDetail> users = localUserMgr.getUsersBySpecificProperty(connection,
            property.getMapParameter(), propertyValue);
        return users.toArray(new UserDetail[users.size()]);
      } catch (Exception e) {
        throw new AdminException(failureOnGetting("all users by property", propertyName), e);
      }
    }
  }

  @Override
  public UserDetail[] getUsersByQuery(Map<String, String> query) throws AdminException {
    return new UserDetail[0];
  }

  @Override
  public GroupDetail importGroup(String groupName) throws AdminException {
    return null;
  }

  @Override
  public void removeGroup(String groupId) throws AdminException {
    // Silverpeas doesn't modify the database behind the SQL domain
  }

  @Override
  public GroupDetail synchroGroup(String groupId) throws AdminException {
    return null;
  }

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public String createGroup(GroupDetail group) throws AdminException {
    try(Connection connection = dataSource.getConnection()) {
      int theGrpId = localGroupMgr.createGroup(connection, group);
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
      throw new AdminException(failureOnAdding(GROUP, group.getName()), e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public void deleteGroup(String groupId) throws AdminException {
    try(Connection connection = dataSource.getConnection()) {
      int gid;
      List<GroupDetail> allSubGroups = new ArrayList<>();
      allSubGroups.add(localGroupMgr.getGroup(connection, idAsInt(groupId)));

      while (!allSubGroups.isEmpty()) {
        gid = idAsInt(allSubGroups.remove(0).getSpecificId());
        // Add sub groups
        allSubGroups.addAll(localGroupMgr.getDirectSubGroups(connection, gid));
        // Remove the group
        localGroupUserRelMgr.removeAllGroupRel(connection, gid);
        localGroupMgr.deleteGroup(connection, gid);
      }
    } catch (Exception e) {
      throw new AdminException(failureOnDeleting(GROUP, groupId), e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public void updateGroup(GroupDetail group) throws AdminException {
    List<String> alAddUsers = new ArrayList<>();

    if (group == null || group.getName().length() == 0 ||
        group.getSpecificId().length() == 0) {
      throw new AdminException(undefined(GROUP));
    }

    try(Connection connection = dataSource.getConnection()) {
      int groupId = idAsInt(group.getSpecificId());

      // Update the group node
      localGroupMgr.updateGroup(connection, group);

      // Update the users if necessary
      List<String> asOldUsersId = localGroupUserRelMgr.getDirectUserIdsOfGroup(connection, groupId);
      // Compute the users list
      String[] asNewUsersId = group.getUserIds();

      for (String anAsNewUsersId : asNewUsersId) {
        if (!asOldUsersId.remove(anAsNewUsersId)) {
          alAddUsers.add(anAsNewUsersId);
        }
      }

      // Remove the users that are not in this group anymore
      for (String anAsOldUsersId : asOldUsersId) {
        localGroupUserRelMgr.removeGroupUserRel(connection, groupId, idAsInt(anAsOldUsersId));
      }

      // Add the new users of the group
      for (String alAddUser : alAddUsers) {
        localGroupUserRelMgr.createGroupUserRel(connection, groupId, idAsInt(alAddUser));
      }
    } catch (Exception e) {
      throw new AdminException(failureOnUpdate(GROUP, group.getSpecificId()), e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public GroupDetail getGroup(String specificId) throws AdminException {
    try(Connection connection = dataSource.getConnection()) {
      GroupDetail valret = localGroupMgr.getGroup(connection, idAsInt(specificId));
      if (valret != null) {
        // Get the selected users for this group
        List<String> asUsersId =
            localGroupUserRelMgr.getDirectUserIdsOfGroup(connection, idAsInt(specificId));
        valret.setUserIds(asUsersId.toArray(new String[asUsersId.size()]));
      }
      return valret;
    } catch (Exception e) {
      throw new AdminException(failureOnGetting(GROUP, specificId), e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public GroupDetail getGroupByName(String groupName) throws AdminException {
    try(Connection connection = dataSource.getConnection()) {
      return localGroupMgr.getGroupByName(connection, groupName);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting(GROUP, groupName), e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public GroupDetail[] getGroups(String groupId) throws AdminException {
    try(Connection connection = dataSource.getConnection()) {
      List<GroupDetail> ar = localGroupMgr.getDirectSubGroups(connection, idAsInt(groupId));

      for (GroupDetail theGroup : ar) {
        // Get the selected users for this group
        List<String> asUsersId = localGroupUserRelMgr.getDirectUserIdsOfGroup(connection,
            idAsInt(theGroup.getSpecificId()));
        theGroup.setUserIds(asUsersId.toArray(new String[asUsersId.size()]));
      }

      return ar.toArray(new GroupDetail[ar.size()]);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("subgroups of group", groupId), e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public GroupDetail[] getAllGroups() throws AdminException {
    try(Connection connection = dataSource.getConnection()) {
      List<GroupDetail> ar = localGroupMgr.getAllGroups(connection);

      for (GroupDetail theGroup : ar) {
        // Get the selected users for this group
        List<String> asUsersId = localGroupUserRelMgr.getDirectUserIdsOfGroup(connection,
            idAsInt(theGroup.getSpecificId()));
        theGroup.setUserIds(asUsersId.toArray(new String[asUsersId.size()]));
      }

      return ar.toArray(new GroupDetail[ar.size()]);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("all groups", ""), e);
    }
  }

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public GroupDetail[] getAllRootGroups() throws AdminException {
    try(Connection connection = dataSource.getConnection()) {
      List<GroupDetail> ar = localGroupMgr.getDirectSubGroups(connection, -1);
      for (GroupDetail theGroup : ar) {
        // Get the selected users for this group
        List<String> asUsersId = localGroupUserRelMgr.getDirectUserIdsOfGroup(connection,
            idAsInt(theGroup.getSpecificId()));
        theGroup.setUserIds(asUsersId.toArray(new String[asUsersId.size()]));
      }

      return ar.toArray(new GroupDetail[ar.size()]);
    } catch (Exception e) {
      throw new AdminException(failureOnGetting("all root groups", ""), e);
    }
  }

  @Transactional(Transactional.TxType.MANDATORY)
  public void addUserInGroup(String userId, String groupId) throws AdminException {
    try(Connection connection = dataSource.getConnection()) {
      localGroupUserRelMgr.createGroupUserRel(connection, idAsInt(groupId), idAsInt(userId));
    } catch (Exception e) {
      throw new AdminException(failureOnAdding("user " + userId, "in group" + groupId), e);
    }
  }

  @Transactional(Transactional.TxType.MANDATORY)
  public void removeUserFromGroup(String userId, String groupId)
      throws AdminException {
    try(Connection connection = dataSource.getConnection()) {
      localGroupUserRelMgr.removeGroupUserRel(connection, idAsInt(groupId), idAsInt(userId));

    } catch (Exception e) {
      throw new AdminException(failureOnDeleting("user " + userId, "in group" + groupId), e);
    }
  }

  @Override
  public List<String> getUserAttributes() throws AdminException {
    return Arrays.asList(getPropertiesNames());
  }
}
