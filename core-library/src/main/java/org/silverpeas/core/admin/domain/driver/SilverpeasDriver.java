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

package org.silverpeas.core.admin.domain.driver;

import org.silverpeas.core.admin.domain.AbstractDomainDriver;
import org.silverpeas.core.admin.domain.model.DomainProperty;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.exception.UtilException;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.security.authentication.password.PasswordEncryption;
import org.silverpeas.core.security.authentication.password.PasswordEncryptionProvider;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.silverpeas.core.SilverpeasExceptionMessages.undefined;

@Singleton
@Transactional
public class SilverpeasDriver extends AbstractDomainDriver implements SilverpeasDomainDriver {

  @Inject
  private SPUserRepository spUserRepository;

  @Inject
  private SPGroupRepository spGroupRepository;

  /**
   * Constructor
   */
  public SilverpeasDriver() {
  }

  /**
   * Virtual method that performs extra initialization from a properties file. To overload by the
   * class who need it.
   * @throws Exception
   */
  @Override
  public void initFromProperties(SettingBundle rs) throws Exception {
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
   * @param ud a user detail
   * @return the new user id.
   */
  @Override
  public String createUser(UserDetail ud) {
    try {
      SPUser user = convertToSPUser(ud, new SPUser());
      user = spUserRepository.saveAndFlush(user);
      return user.getId();
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
    return "-1";
  }

  /**
   * @param userId
   */
  @Override
  public void deleteUser(String userId) {
    if (StringUtil.isInteger(userId)) {
      SPUser user = spUserRepository.getById(userId);
      if (user.getGroups() != null) {
        for (SPGroup group : user.getGroups()) {
          group.getUsers().remove(user);
          spGroupRepository.saveAndFlush(group);
        }
      }
      spUserRepository.delete(user);
    }
  }

  @Override
  public void updateUserFull(UserFull userFull) throws UtilException {
    SPUser oldUser = spUserRepository.getById(userFull.getSpecificId());
    oldUser.setFirstname(userFull.getFirstName());
    oldUser.setLastname(userFull.getLastName());
    oldUser.setLogin(userFull.getLogin());
    oldUser.setEmail(userFull.geteMail());

    oldUser.setTitle(userFull.getValue("title"));
    oldUser.setCompany(userFull.getValue("company"));
    oldUser.setPosition(userFull.getValue("position"));
    oldUser.setBoss(userFull.getValue("boss"));
    oldUser.setPhone(userFull.getValue("phone"));
    oldUser.setHomephone(userFull.getValue("homePhone"));
    oldUser.setFax(userFull.getValue("fax"));
    oldUser.setCellphone(userFull.getValue("cellularPhone"));
    oldUser.setAddress(userFull.getValue("address"));
    oldUser.setLoginmail("");

    // Only update password when this field has been filled
    if (StringUtil.isDefined(userFull.getPassword()) &&
        !userFull.getPassword().equals(oldUser.getPassword())) {
       oldUser.setPassword(encrypt(userFull.getPassword()));
    }
    oldUser.setPasswordValid(userFull.isPasswordValid());
    this.spUserRepository.saveAndFlush(oldUser);
  }

  /**
   * @param ud
   */
  @Override
  public void updateUserDetail(UserDetail ud) {
    if (StringUtil.isInteger(ud.getSpecificId())) {
      SPUser user = spUserRepository.getById(ud.getSpecificId());
      if (user != null) {
        spUserRepository.save(convertToSPUser(ud, user));
      }
    }
  }

  /**
   * @param userId
   * @return User
   */
  @Override
  public UserDetail getUser(String userId) {
    if (!StringUtil.isInteger(userId)) {
      return null;
    }
    SPUser spUser = spUserRepository.getById(userId);
    return convertToUser(spUser, new UserDetail());
  }

  @Override
  public UserFull getUserFull(String userId) {
    if (!StringUtil.isInteger(userId)) {
      return null;
    }
    SPUser user = spUserRepository.getById(userId);
    UserFull userFull = new UserFull(this);
    if (user != null) {
      userFull.setFirstName(user.getFirstname());
      userFull.setLastName(user.getLastname());
      userFull.setValue("title", user.getTitle());
      userFull.setValue("company", user.getCompany());
      userFull.setValue("position", user.getPosition());
      userFull.setValue("boss", user.getBoss());
      userFull.setValue("phone", user.getPhone());
      userFull.setValue("homePhone", user.getHomephone());
      userFull.setValue("fax", user.getFax());
      userFull.setValue("cellularPhone", user.getCellphone());
      userFull.setValue("address", user.getAddress());
      userFull.setLogin(user.getLogin());
      userFull.seteMail(user.getEmail());
      userFull.setPassword(user.getPassword());
      userFull.setPasswordValid(user.isPasswordValid());
      userFull.setPasswordAvailable(true);
    }
    return userFull;
  }

  @Override
  public String[] getUserMemberGroupIds(String userId) throws Exception {
    return new String[0];
  }

  /**
   * @return User[]
   */
  @Override
  public UserDetail[] getAllUsers() {
    List<SPUser> users = spUserRepository.getAll();
    List<UserDetail> details = new ArrayList<>(users.size());
    for (SPUser sPUser : users) {
      details.add(convertToUser(sPUser, new UserDetail()));
    }
    return details.toArray(new UserDetail[details.size()]);
  }

  @Override
  public UserDetail[] getUsersBySpecificProperty(String propertyName,
      String propertyValue) throws Exception {
    DomainProperty property = getProperty(propertyName);
    if (property == null) {
      return null;
    }
    List<SPUser> users = new ArrayList<>();
    if ("title".equalsIgnoreCase(propertyName)) {
      users = spUserRepository.findByTitle(propertyValue);
    } else if ("company".equalsIgnoreCase(propertyName)) {
      users = spUserRepository.findByCompany(propertyValue);
    } else if ("position".equalsIgnoreCase(propertyName)) {
      users = spUserRepository.findByPosition(propertyValue);
    } else if ("phone".equalsIgnoreCase(propertyName)) {
      users = spUserRepository.findByPhone(propertyValue);
    } else if ("homePhone".equalsIgnoreCase(propertyName)) {
      users = spUserRepository.findByHomephone(propertyValue);
    } else if ("fax".equalsIgnoreCase(propertyName)) {
      users = spUserRepository.findByFax(propertyValue);
    } else if ("cellularPhone".equalsIgnoreCase(propertyName)) {
      users = spUserRepository.findByCellphone(propertyValue);
    } else if ("address".equalsIgnoreCase(propertyName)) {
      users = spUserRepository.findByAddress(propertyValue);
    }
    List<UserDetail> userDetails = new ArrayList<>(users.size());
    for (SPUser spUser : users) {
      userDetails.add(convertToUser(spUser, new UserDetail()));
    }
    return userDetails.toArray(new UserDetail[userDetails.size()]);
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
  @Override
  public String createGroup(GroupDetail group) {
    try {
      SPGroup spGroup = new SPGroup();
      int id = DBUtil.getNextId("domainsp_group", "id");
      spGroup.setId(id);
      group.setId(String.valueOf(id));
      spGroup.setDescription(group.getDescription());
      spGroup.setName(group.getName());
      if (StringUtil.isInteger(group.getSuperGroupId())) {
        SPGroup parent = spGroupRepository.getById(group.getSuperGroupId());
        spGroup.setParent(parent);
      }
      String[] userIds = group.getUserIds();
      for (String userId : userIds) {
        SPUser user = spUserRepository.getById(userId);
        spGroup.getUsers().add(user);
        user.getGroups().add(spGroup);
      }
      spGroup = spGroupRepository.saveAndFlush(spGroup);
      return String.valueOf(spGroup.getId());
    } catch (SQLException ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
    return "";
  }

  /**
   * @param groupId
   */
  @Override
  public void deleteGroup(String groupId) {
    SPGroup group = spGroupRepository.getById(groupId);
    if (group != null) {
      for (SPGroup subGroup : new ArrayList<>(group.getSubGroups())) {
        deleteGroup(String.valueOf(subGroup.getId()));
      }
      SPGroup reloadedGroup = spGroupRepository.getById(groupId);
      for (SPUser user : new ArrayList<>(reloadedGroup.getUsers())) {
        user.getGroups().remove(reloadedGroup);
      }
      SPGroup parent = reloadedGroup.getParent();
      if (parent != null) {
        parent.getSubGroups().remove(reloadedGroup);
        spGroupRepository.saveAndFlush(parent);
      }
      spGroupRepository.delete(reloadedGroup);
      spGroupRepository.flush();
    }
  }

  /**
   * @param group
   * @throws AdminException
   */
  @Override
  public void updateGroup(GroupDetail group) throws AdminException {
    Set<SPUser> addedUsers = new HashSet<>();
    if (group == null || !StringUtil.isDefined(group.getName()) || !StringUtil.isDefined(
        group.getId())) {
      throw new AdminException(undefined("group"));
    }

    // Update the group node
    SPGroup gr = spGroupRepository.getById(group.getSpecificId());
    gr = convertToSPGroup(group, gr);
    Set<SPUser> users = gr.getUsers();
    Map<String, SPUser> existingUsers = new HashMap<>(users.size());
    for (SPUser user : users) {
      existingUsers.put(String.valueOf(user.getId()), user);
    }

    String[] userIds = group.getUserIds();
    for (String userId : userIds) {
      if (existingUsers.containsKey(userId)) {
        addedUsers.add(existingUsers.get(userId));
        existingUsers.remove(userId);
      } else {
        SPUser newUser = spUserRepository.getById(userId);
        addedUsers.add(newUser);
      }
    }
    gr.setUsers(addedUsers);

    spGroupRepository.saveAndFlush(gr);
  }

  /**
   * @param groupId
   * @return GroupDetail
   */
  @Override
  public GroupDetail getGroup(String groupId) {
    SPGroup gr = spGroupRepository.getById(groupId);
    return convertToGroup(gr);
  }

  @Override
  public GroupDetail getGroupByName(String groupName) throws Exception {
    return null;
  }

  /**
   * @param groupId
   * @return GroupDetail[]
   */
  @Override
  public GroupDetail[] getGroups(String groupId) {
    SPGroup gr = spGroupRepository.getById(groupId);
    Set<SPGroup> subGroups = gr.getSubGroups();
    List<GroupDetail> groups = new ArrayList<>(subGroups.size());
    for (SPGroup spGroup : subGroups) {
      groups.add(convertToGroup(spGroup));
    }
    return groups.toArray(new GroupDetail[groups.size()]);
  }

  /**
   * @return GroupDetail[]
   */
  @Override
  public GroupDetail[] getAllGroups() {
    List<SPGroup> groups = spGroupRepository.getAll();
    List<GroupDetail> result = new ArrayList<>(groups.size());
    for (SPGroup spGroup : groups) {
      result.add(convertToGroup(spGroup));
    }
    return result.toArray(new GroupDetail[result.size()]);
  }

  @Override
  public GroupDetail[] getAllRootGroups() {
    List<SPGroup> groups = spGroupRepository.listAllRootGroups();
    List<GroupDetail> result = new ArrayList<>(groups.size());
    for (SPGroup spGroup : groups) {
      result.add(convertToGroup(spGroup));
    }
    return result.toArray(new GroupDetail[result.size()]);
  }

  @Override
  public String[] getGroupMemberGroupIds(String groupId) throws Exception {
    return new String[0];
  }

  @Override
  public void startTransaction(boolean bAutoCommit) {
  }

  @Override
  public void commit() throws Exception {
  }

  @Override
  public void rollback() throws Exception {
  }

  /**
   * @param userId
   * @param groupId
   * @throws Exception
   */
  public void addUserInGroup(String userId, String groupId) throws Exception {
    SPUser user = spUserRepository.getById(userId);
    SPGroup group = spGroupRepository.getById(groupId);
    user.getGroups().add(group);
    group.getUsers().add(user);
    spGroupRepository.saveAndFlush(group);
  }

  /**
   * @param userId
   * @param groupId
   */
  public void removeUserFromGroup(String userId, String groupId) {
    SPUser user = spUserRepository.getById(userId);
    SPGroup group = spGroupRepository.getById(groupId);
    user.getGroups().remove(group);
    group.getUsers().remove(user);
    spGroupRepository.saveAndFlush(group);
  }

  /**
   * Convert GroupDetail to SPGroupRow
   */
  SPGroup convertToSPGroup(GroupDetail group, SPGroup spGroup) {
    if (StringUtil.isDefined(group.getSpecificId()) && StringUtil.isInteger(group.getSpecificId())) {
      spGroup.setId(Integer.valueOf(group.getSpecificId()));
    }
    if (StringUtil.isDefined(group.getSuperGroupId()) && StringUtil.isInteger(
        group.getSuperGroupId())) {
      SPGroup parent = spGroupRepository.getById(group.getSuperGroupId());
      spGroup.setParent(parent);
    }
    spGroup.setName(group.getName());
    spGroup.setDescription(group.getDescription());
    return spGroup;
  }

  GroupDetail convertToGroup(SPGroup gr) {
    GroupDetail group = new GroupDetail();
    group.setSpecificId(String.valueOf(gr.getId()));
    group.setName(gr.getName());
    group.setDescription(gr.getDescription());
    if (gr.getParent() != null) {
      group.setSuperGroupId(String.valueOf(gr.getParent().getId()));
    }
    Set<SPUser> users = gr.getUsers();
    List<String> userIds = new ArrayList<>(users.size());
    for (SPUser user : users) {
      userIds.add(String.valueOf(user.getId()));
    }
    group.setUserIds(userIds.toArray(new String[userIds.size()]));
    return group;
  }

  @Override
  public List<String> getUserAttributes() throws Exception {
    // no attributes for this driver
    return null;
  }

  SPUser convertToSPUser(UserDetail detail, SPUser user) {
    if (StringUtil.isDefined(detail.getSpecificId()) &&
        StringUtil.isInteger(detail.getSpecificId())) {
      user.setId(Integer.valueOf(detail.getId()));
    }
    user.setFirstname(detail.getFirstName());
    user.setLastname(detail.getLastName());
    user.setLogin(detail.getLogin());
    user.setEmail(detail.geteMail());
    return user;
  }

  UserDetail convertToUser(SPUser user, UserDetail detail) {
    detail.setSpecificId(String.valueOf(user.getId()));
    detail.setFirstName(user.getFirstname());
    detail.setLastName(user.getLastname());
    detail.setLogin(user.getLogin());
    detail.seteMail(user.getEmail());
    return detail;
  }

  @Override
  public void resetPassword(UserDetail userDetail, String password) throws Exception {
    SPUser user = spUserRepository.getById(userDetail.getId());
    user.setPassword(encrypt(password));
    user.setPasswordValid(true);
    spUserRepository.saveAndFlush(user);
  }

  private String encrypt(String password) {
    PasswordEncryption encryption = PasswordEncryptionProvider.getDefaultPasswordEncryption();
    return encryption.encrypt(password);
  }

  @Override
  public void resetEncryptedPassword(UserDetail userDetail, String encryptedPassword) throws Exception {
    SPUser user = spUserRepository.getById(userDetail.getId());
    user.setPassword(encryptedPassword);
    user.setPasswordValid(true);
    spUserRepository.saveAndFlush(user);
  }
}
