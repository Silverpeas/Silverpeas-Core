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
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.domain.model.DomainProperty;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.security.authentication.password.PasswordEncryption;
import org.silverpeas.core.security.authentication.password.PasswordEncryptionProvider;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.exception.UtilException;
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

@Singleton
@Transactional
public class SilverpeasDriver extends AbstractDomainDriver implements SilverpeasDomainDriver {

  @Inject
  private SPUserManager userManager;

  @Inject
  private SPGroupManager groupManager;

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
  public Group[] getAllChangedGroups(String fromTimeStamp, String toTimeStamp) throws Exception {
    return new Group[0];
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
      user = userManager.saveAndFlush(user);
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
      SPUser user = userManager.getById(userId);
      if (user.getGroups() != null) {
        for (SPGroup group : user.getGroups()) {
          group.getUsers().remove(user);
          groupManager.saveAndFlush(group);
        }
      }
      userManager.delete(user);
    }
  }

  @Override
  public void updateUserFull(UserFull userFull) throws UtilException {
    SPUser oldUser = userManager.getById(userFull.getSpecificId());
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
    this.userManager.saveAndFlush(oldUser);
  }

  /**
   * @param ud
   */
  @Override
  public void updateUserDetail(UserDetail ud) {
    if (StringUtil.isInteger(ud.getSpecificId())) {
      SPUser user = userManager.getById(ud.getSpecificId());
      if (user != null) {
        userManager.save(convertToSPUser(ud, user));
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
    SPUser spUser = userManager.getById(userId);
    return convertToUser(spUser, new UserDetail());
  }

  @Override
  public UserFull getUserFull(String userId) {
    if (!StringUtil.isInteger(userId)) {
      return null;
    }
    SPUser user = userManager.getById(userId);
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
    List<SPUser> users = userManager.getAll();
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
      users = userManager.findByTitle(propertyValue);
    } else if ("company".equalsIgnoreCase(propertyName)) {
      users = userManager.findByCompany(propertyValue);
    } else if ("position".equalsIgnoreCase(propertyName)) {
      users = userManager.findByPosition(propertyValue);
    } else if ("phone".equalsIgnoreCase(propertyName)) {
      users = userManager.findByPhone(propertyValue);
    } else if ("homePhone".equalsIgnoreCase(propertyName)) {
      users = userManager.findByHomephone(propertyValue);
    } else if ("fax".equalsIgnoreCase(propertyName)) {
      users = userManager.findByFax(propertyValue);
    } else if ("cellularPhone".equalsIgnoreCase(propertyName)) {
      users = userManager.findByCellphone(propertyValue);
    } else if ("address".equalsIgnoreCase(propertyName)) {
      users = userManager.findByAddress(propertyValue);
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
  public Group importGroup(String groupName) throws Exception {
    return null;
  }

  @Override
  public void removeGroup(String groupId) throws Exception {
  }

  @Override
  public Group synchroGroup(String groupId) throws Exception {
    return null;
  }

  /**
   * @param group
   * @return String
   */
  @Override
  public String createGroup(Group group) {
    try {
      SPGroup spGroup = new SPGroup();
      int id = DBUtil.getNextId("domainsp_group", "id");
      spGroup.setId(id);
      group.setId(String.valueOf(id));
      spGroup.setDescription(group.getDescription());
      spGroup.setName(group.getName());
      if (StringUtil.isInteger(group.getSuperGroupId())) {
        SPGroup parent = groupManager.getById(group.getSuperGroupId());
        spGroup.setParent(parent);
      }
      String[] userIds = group.getUserIds();
      for (String userId : userIds) {
        SPUser user = userManager.getById(userId);
        spGroup.getUsers().add(user);
        user.getGroups().add(spGroup);
      }
      spGroup = groupManager.saveAndFlush(spGroup);
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
    SPGroup group = groupManager.getById(groupId);
    if (group != null) {
      for (SPGroup subGroup : new ArrayList<>(group.getSubGroups())) {
        deleteGroup(String.valueOf(subGroup.getId()));
      }
      SPGroup reloadedGroup = groupManager.getById(groupId);
      for (SPUser user : new ArrayList<>(reloadedGroup.getUsers())) {
        user.getGroups().remove(reloadedGroup);
      }
      SPGroup parent = reloadedGroup.getParent();
      if (parent != null) {
        parent.getSubGroups().remove(reloadedGroup);
        groupManager.saveAndFlush(parent);
      }
      groupManager.delete(reloadedGroup);
      groupManager.flush();
    }
  }

  /**
   * @param group
   * @throws AdminException
   */
  @Override
  public void updateGroup(Group group) throws AdminException {
    Set<SPUser> addedUsers = new HashSet<>();
    if (group == null || !StringUtil.isDefined(group.getName()) || !StringUtil.isDefined(
        group.getId())) {
      throw new AdminException("SilverpeasDriver.updateGroup", SilverpeasException.ERROR,
          "admin.EX_ERR_INVALID_GROUP");
    }

    // Update the group node
    SPGroup gr = groupManager.getById(group.getSpecificId());
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
        SPUser newUser = userManager.getById(userId);
        addedUsers.add(newUser);
      }
    }
    gr.setUsers(addedUsers);

    groupManager.saveAndFlush(gr);
  }

  /**
   * @param groupId
   * @return Group
   */
  @Override
  public Group getGroup(String groupId) {
    SPGroup gr = groupManager.getById(groupId);
    return convertToGroup(gr);
  }

  @Override
  public Group getGroupByName(String groupName) throws Exception {
    return null;
  }

  /**
   * @param groupId
   * @return Group[]
   */
  @Override
  public Group[] getGroups(String groupId) {
    SPGroup gr = groupManager.getById(groupId);
    Set<SPGroup> subGroups = gr.getSubGroups();
    List<Group> groups = new ArrayList<>(subGroups.size());
    for (SPGroup spGroup : subGroups) {
      groups.add(convertToGroup(spGroup));
    }
    return groups.toArray(new Group[groups.size()]);
  }

  /**
   * @return Group[]
   */
  @Override
  public Group[] getAllGroups() {
    List<SPGroup> groups = groupManager.getAll();
    List<Group> result = new ArrayList<>(groups.size());
    for (SPGroup spGroup : groups) {
      result.add(convertToGroup(spGroup));
    }
    return result.toArray(new Group[result.size()]);
  }

  @Override
  public Group[] getAllRootGroups() {
    List<SPGroup> groups = groupManager.listAllRootGroups();
    List<Group> result = new ArrayList<>(groups.size());
    for (SPGroup spGroup : groups) {
      result.add(convertToGroup(spGroup));
    }
    return result.toArray(new Group[result.size()]);
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
    SPUser user = userManager.getById(userId);
    SPGroup group = groupManager.getById(groupId);
    user.getGroups().add(group);
    group.getUsers().add(user);
    groupManager.saveAndFlush(group);
  }

  /**
   * @param userId
   * @param groupId
   */
  public void removeUserFromGroup(String userId, String groupId) {
    SPUser user = userManager.getById(userId);
    SPGroup group = groupManager.getById(groupId);
    user.getGroups().remove(group);
    group.getUsers().remove(user);
    groupManager.saveAndFlush(group);
  }

  /**
   * Convert Group to SPGroupRow
   */
  SPGroup convertToSPGroup(Group group, SPGroup spGroup) {
    if (StringUtil.isDefined(group.getSpecificId()) && StringUtil.isInteger(group.getSpecificId())) {
      spGroup.setId(Integer.valueOf(group.getSpecificId()));
    }
    if (StringUtil.isDefined(group.getSuperGroupId()) && StringUtil.isInteger(
        group.getSuperGroupId())) {
      SPGroup parent = groupManager.getById(group.getSuperGroupId());
      spGroup.setParent(parent);
    }
    spGroup.setName(group.getName());
    spGroup.setDescription(group.getDescription());
    return spGroup;
  }

  Group convertToGroup(SPGroup gr) {
    Group group = new Group();
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
    SPUser user = userManager.getById(userDetail.getId());
    user.setPassword(encrypt(password));
    user.setPasswordValid(true);
    userManager.saveAndFlush(user);
  }

  private String encrypt(String password) {
    PasswordEncryption encryption = PasswordEncryptionProvider.getDefaultPasswordEncryption();
    return encryption.encrypt(password);
  }

  @Override
  public void resetEncryptedPassword(UserDetail userDetail, String encryptedPassword) throws Exception {
    SPUser user = userManager.getById(userDetail.getId());
    user.setPassword(encryptedPassword);
    user.setPasswordValid(true);
    userManager.saveAndFlush(user);
  }
}
