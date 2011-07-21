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
package com.silverpeas.domains.silverpeasdriver;

import com.silverpeas.util.StringUtil;
import com.silverpeas.util.cryptage.CryptMD5;
import com.silverpeas.util.cryptage.UnixMD5Crypt;
import com.stratelia.silverpeas.authentication.Authentication;
import com.stratelia.webactiv.beans.admin.AbstractDomainDriver;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.DomainProperty;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class SilverpeasDriver extends AbstractDomainDriver implements SilverpeasDomainDriver {

  @Inject
  private SPUserDao userDao;
  @Inject
  private SPGroupDao groupDao;
  private String passwordEncryption = null;

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
  public void initFromProperties(ResourceLocator rs) throws Exception {
    passwordEncryption = rs.getString("database.SQLPasswordEncryption");
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
   * 
   * @param ud
   * @return the new user id.
   */
  @Override
  public String createUser(UserDetail ud) {
    try {
      SPUser user = convertToSPUser(ud, new SPUser());
      int id = DBUtil.getNextId("domainsp_user", "id");
      user.setId(id);
      user = userDao.saveAndFlush(user);
      return String.valueOf(id);
    } catch (UtilException ex) {
      Logger.getLogger(SilverpeasDriver.class.getName()).log(Level.SEVERE, null, ex);
    }
    return "-1";
  }

  /**
   * 
   * @param userId
   */
  @Override
  public void deleteUser(String userId) {
    SPUser user = userDao.readByPrimaryKey(Integer.valueOf(userId));
    if(user.getGroups() != null) {
      for(SPGroup group : user.getGroups()) {
        group.getUsers().remove(user);
        groupDao.saveAndFlush(group);
      }
    }
    userDao.delete(user);
  }

  @Override
  public void updateUserFull(UserFull userFull) throws UtilException {
    SPUser oldUser = userDao.readByPrimaryKey(Integer.valueOf(userFull.getSpecificId()));
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
    if (Authentication.ENC_TYPE_UNIX.equals(passwordEncryption)
        && !userFull.getPassword().equals(oldUser.getPassword())) {
      oldUser.setPassword(UnixMD5Crypt.crypt(userFull.getPassword()));
    } else if (Authentication.ENC_TYPE_MD5.equals(passwordEncryption)
        && !userFull.getPassword().equals(oldUser.getPassword())) {
      oldUser.setPassword(CryptMD5.crypt(userFull.getPassword()));
    } else {
      oldUser.setPassword(userFull.getPassword());
    }
    oldUser.setPasswordValid(userFull.isPasswordValid());
    this.userDao.saveAndFlush(oldUser);
  }

  /**
   * @param ud 
   */
  @Override
  public void updateUserDetail(UserDetail ud) {
    SPUser user = userDao.readByPrimaryKey(Integer.valueOf(ud.getSpecificId()));
    if (user != null) {
      userDao.save(convertToSPUser(ud, user));
    }
  }

  /**
   * @param userId
   * @return User
   */
  @Override
  public UserDetail getUser(String userId) {
    SPUser spUser = userDao.readByPrimaryKey(Integer.parseInt(userId));
    return convertToUser(spUser, new UserDetail());
  }

  @Override
  public UserFull getUserFull(String userId) {
    SPUser user = userDao.readByPrimaryKey(Integer.valueOf(userId));
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
    List<SPUser> users = userDao.readAll();
    List<UserDetail> details = new ArrayList<UserDetail>(users.size());
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
    List<SPUser> users = new ArrayList<SPUser>();
    if ("title".equalsIgnoreCase(propertyName)) {
      users = userDao.findByTitle(propertyValue);
    } else if (propertyName.equalsIgnoreCase("company")) {
      users = userDao.findByCompany(propertyValue);
    } else if (propertyName.equalsIgnoreCase("position")) {
      users = userDao.findByPosition(propertyValue);
    } else if (propertyName.equalsIgnoreCase("phone")) {
      users = userDao.findByPhone(propertyValue);
    } else if (propertyName.equalsIgnoreCase("homePhone")) {
      users = userDao.findByHomephone(propertyValue);
    } else if (propertyName.equalsIgnoreCase("fax")) {
      users = userDao.findByFax(propertyValue);
    } else if (propertyName.equalsIgnoreCase("cellularPhone")) {
      users = userDao.findByCellphone(propertyValue);
    } else if (propertyName.equalsIgnoreCase("address")) {
      users = userDao.findByAddress(propertyValue);
    }
    List<UserDetail> userDetails = new ArrayList<UserDetail>(users.size());
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
      if (StringUtil.isDefined(group.getSuperGroupId())) {
        SPGroup parent = groupDao.readByPrimaryKey(Integer.valueOf(group.getSuperGroupId()));
        spGroup.setParent(parent);
      }
      String[] userIds = group.getUserIds();
      for (String userId : userIds) {
        SPUser user = userDao.readByPrimaryKey(Integer.valueOf(userId));
        spGroup.getUsers().add(user);
        user.getGroups().add(spGroup);
      }
      spGroup = groupDao.saveAndFlush(spGroup);
      return String.valueOf(spGroup.getId());
    } catch (UtilException ex) {
      Logger.getLogger(SilverpeasDriver.class.getName()).log(Level.SEVERE, null, ex);
    }
    return "";
  }

  /**
   * @param groupId
   */
  @Override
  public void deleteGroup(String groupId) {
    SPGroup group = groupDao.readByPrimaryKey(Integer.valueOf(groupId));
    if (group != null) {
      for (SPUser user : group.getUsers()) {
        user.getGroups().remove(group);
      }
      for (SPGroup subGroup : group.getSubGroups()) {
        subGroup.setParent(group.getParent());
        group.getParent().getSubGroups().add(subGroup);
        groupDao.saveAndFlush(subGroup);
      }
     SPGroup parent = group.getParent();
      if (parent != null && parent != null) {
        parent.getSubGroups().remove(group);
        groupDao.saveAndFlush(parent);
      }
      groupDao.delete(group);
    }
  }

  /**
   * 
   * @param group
   * @throws AdminException 
   */
  @Override
  public void updateGroup(Group group) throws AdminException {
    Set<SPUser> addedUsers = new HashSet<SPUser>();
    if (group == null || !StringUtil.isDefined(group.getName()) || !StringUtil.isDefined(
        group.getId())) {
      throw new AdminException("SilverpeasDriver.updateGroup", SilverpeasException.ERROR,
          "admin.EX_ERR_INVALID_GROUP");
    }

    // Update the group node
    SPGroup gr = groupDao.readByPrimaryKey(Integer.valueOf(group.getSpecificId()));
    gr = convertToSPGroup(group, gr);
    Set<SPUser> users = gr.getUsers();
    Map<String, SPUser> existingUsers = new HashMap<String, SPUser>(users.size());
    for (SPUser user : users) {
      existingUsers.put(String.valueOf(user.getId()), user);
    }

    String[] userIds = group.getUserIds();
    for (String userId : userIds) {
      if (existingUsers.containsKey(userId)) {
        addedUsers.add(existingUsers.get(userId));
        existingUsers.remove(userId);
      } else {
        SPUser newUser = userDao.readByPrimaryKey(Integer.valueOf(userId));
        addedUsers.add(newUser);
      }
    }
    gr.setUsers(addedUsers);

    groupDao.saveAndFlush(gr);
  }

  /**
   * @param groupId
   * @return Group
   */
  @Override
  public Group getGroup(String groupId) {
    SPGroup gr = groupDao.readByPrimaryKey(Integer.valueOf(groupId));
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
    SPGroup gr = groupDao.readByPrimaryKey(Integer.valueOf(groupId));
    Set<SPGroup> subGroups = gr.getSubGroups();
    List<Group> groups = new ArrayList<Group>(subGroups.size());
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
    List<SPGroup> groups = groupDao.readAll();
    List<Group> result = new ArrayList<Group>(groups.size());
    for (SPGroup spGroup : groups) {
      result.add(convertToGroup(spGroup));
    }
    return result.toArray(new Group[result.size()]);
  }

  @Override
  public Group[] getAllRootGroups() {
    List<SPGroup> groups = groupDao.listAllRootGroups("toto");
    List<Group> result = new ArrayList<Group>(groups.size());
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
  public void startTransaction(boolean bAutoCommit) throws Exception {
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
    SPUser user = userDao.readByPrimaryKey(Integer.valueOf(userId));
    SPGroup group = groupDao.readByPrimaryKey(Integer.valueOf(groupId));
    user.getGroups().add(group);
    group.getUsers().add(user);
    groupDao.saveAndFlush(group);
  }

  /**
   * @param userId
   * @param groupId
   */
  public void removeUserFromGroup(String userId, String groupId) {
    SPUser user = userDao.readByPrimaryKey(Integer.valueOf(userId));
    SPGroup group = groupDao.readByPrimaryKey(Integer.valueOf(groupId));
    user.getGroups().remove(group);
    group.getUsers().remove(user);
    groupDao.saveAndFlush(group);
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
      SPGroup parent = groupDao.readByPrimaryKey(Integer.valueOf(group.getSuperGroupId()));
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
    List<String> userIds = new ArrayList<String>(users.size());
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
    if (StringUtil.isDefined(detail.getSpecificId()) && StringUtil.isInteger(detail.getSpecificId())) {
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
}
