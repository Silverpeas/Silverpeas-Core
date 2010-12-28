/**
 * Copyright (C) 2000 - 2009 Silverpeas
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

package com.silverpeas.admin;

import com.silverpeas.components.model.AbstractTestDao;
import java.util.List;


import org.junit.Test;

import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.GroupProfileInst;
import com.stratelia.webactiv.beans.admin.UserDetail;

public class UsersAndGroupsTest extends AbstractTestDao {


  @Test
  public void testAddUser() {
    UserDetail user = new UserDetail();
    user.setAccessLevel("A");
    user.setDomainId("0");
    user.seteMail("nicolas.eysseric@silverpeas.com");
    user.setFirstName("Nicolas");
    user.setLastName("EYSSERIC");
    user.setLogin("neysseri");
    AdminController ac = getAdminController();
    String userId = ac.addUser(user);
    assertEquals("2", userId);
  }

  @Test
  public void testUpdateUser() {
    AdminController ac = getAdminController();
    UserDetail user = ac.getUserDetail("1");
    String newEmail = "ney@silverpeas.com";
    user.seteMail(newEmail);
    ac.updateUser(user);
    user = ac.getUserDetail("1");
    assertEquals(newEmail, user.geteMail());
  }

  @Test
  public void testDeleteUser() {
    AdminController ac = getAdminController();
    String userId = ac.deleteUser("1");
    assertEquals("1", userId);
    UserDetail user = ac.getUserDetail(userId);
    assertEquals("R", user.getAccessLevel());
  }

  @Test
  public void testAddGroup() {
    AdminController ac = getAdminController();
    Group group = new Group();
    group.setDomainId("0");
    group.setName("Groupe 2");
    String groupId = ac.addGroup(group);
    assertEquals("2", groupId);
  }

  @Test
  public void testUpdateGroup() {
    AdminController ac = getAdminController();
    String desc = "New description";
    Group group = ac.getGroupById("1");
    group.setDescription(desc);
    ac.updateGroup(group);
    group = ac.getGroupById("1");
    assertEquals(desc, group.getDescription());
  }

  @Test
  public void testDeleteGroup() {
    AdminController ac = getAdminController();
    ac.deleteGroupById("1");
    Group group = ac.getGroupById("1");
    assertNull(group.getId());
  }

  @Test
  public void testUsersInGroup() {
    AdminController ac = getAdminController();
    Group subGroup = new Group();
    subGroup.setDomainId("0");
    subGroup.setName("Groupe 1-1");
    subGroup.setSuperGroupId("1");
    String groupId = ac.addGroup(subGroup);
    assertEquals("2", groupId);

    String[] subGroupIds = ac.getAllSubGroupIds("1");
    assertEquals(subGroupIds.length, 1);

    String[] userIds = new String[1];
    userIds[0] = "1";
    subGroup = ac.getGroupById(groupId);
    subGroup.setUserIds(userIds);
    ac.updateGroup(subGroup);

    // test if users of subgroups are indirectly attach to root group
    UserDetail[] users = ac.getAllUsersOfGroup("1");
    assertEquals(1, users.length);

    subGroup.setUserIds(new String[0]);
    ac.updateGroup(subGroup);

    users = ac.getAllUsersOfGroup("1");
    assertEquals(0, users.length);
  }

  @Test
  public void testGroupManager() throws AdminException {
    AdminController ac = getAdminController();
    GroupProfileInst profile = ac.getGroupProfile("1");
    profile.addUser("1");
    ac.updateGroupProfile(profile);
    Admin admin = new Admin();
    List<String> managerIds = admin.getUserManageableGroupIds("1");
    assertEquals(1, managerIds.size());
  }

  @Override
  protected String getDatasetFileName() {
    return "test-usersandgroups-dataset.xml";
  }
  
    private AdminController getAdminController() {
    AdminController ac = new AdminController(null);
    ac.reloadAdminCache();
    return ac;
  }

}