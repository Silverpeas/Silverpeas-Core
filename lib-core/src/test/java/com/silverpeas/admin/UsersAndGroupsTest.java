/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.silverpeas.admin;

import com.silverpeas.components.model.AbstractSpringJndiDaoTest;
import com.stratelia.webactiv.beans.admin.*;
import java.util.List;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(locations = { "classpath:/spring-jpa-datasource.xml",
    "classpath:/spring-domains.xml" })
public class UsersAndGroupsTest extends AbstractSpringJndiDaoTest {

  @Test
  public void shouldAddNewUser() {
    UserDetail user = new UserDetail();
    user.setAccessLevel("A");
    user.setDomainId("0");
    user.seteMail("nicolas.eysseric@silverpeas.com");
    user.setFirstName("Nicolas");
    user.setLastName("EYSSERIC");
    user.setLogin("neysseri");
    AdminController ac = getAdminController();
    String userId = ac.addUser(user);
    assertThat(userId, is("2"));
  }

  @Test
  public void shouldUpdateUser() {
    AdminController ac = getAdminController();
    UserDetail user = ac.getUserDetail("1");
    String newEmail = "ney@silverpeas.com";
    user.seteMail(newEmail);
    ac.updateUser(user);
    user = ac.getUserDetail("1");
    assertThat(user.geteMail(), is(newEmail));
  }

  @Test
  public void shouldDeleteUser() {
    AdminController ac = getAdminController();
    String userId = ac.deleteUser("1");
    assertThat(userId, is("1"));
    UserDetail user = ac.getUserDetail(userId);
    assertThat(user.getAccessLevel(), is("R"));
  }

  @Test
  public void shouldAddGroup() {
    AdminController ac = getAdminController();
    Group group = new Group();
    group.setDomainId("0");
    group.setName("Groupe 2");
    String groupId = ac.addGroup(group);
    assertThat(groupId, is("2"));
  }

  @Test
  public void testUpdateGroup() {
    AdminController ac = getAdminController();
    String desc = "New description";
    Group group = ac.getGroupById("1");
    group.setDescription(desc);
    ac.updateGroup(group);
    group = ac.getGroupById("1");
    assertThat(group.getDescription(), is(desc));
  }

  @Test
  public void shouldDeleteGroup() {
    AdminController ac = getAdminController();
    ac.deleteGroupById("1");
    Group group = ac.getGroupById("1");
    assertThat(group.getId(), is(nullValue()));
  }

  @Test
  public void shouldFindUsersInGroup() {
    AdminController ac = getAdminController();
    Group subGroup = new Group();
    subGroup.setDomainId("0");
    subGroup.setName("Groupe 1-1");
    subGroup.setSuperGroupId("1");
    String groupId = ac.addGroup(subGroup);
    assertThat(groupId, is("2"));

    String[] subGroupIds = ac.getAllSubGroupIds("1");
    assertThat(subGroupIds.length, is(1));

    String[] userIds = new String[1];
    userIds[0] = "1";
    subGroup = ac.getGroupById(groupId);
    subGroup.setUserIds(userIds);
    ac.updateGroup(subGroup);

    // test if users of subgroups are indirectly attach to root group
    UserDetail[] users = ac.getAllUsersOfGroup("1");
    assertThat(users.length, is(1));
    subGroup.setUserIds(new String[0]);
    ac.updateGroup(subGroup);
    users = ac.getAllUsersOfGroup("1");
    assertThat(users.length, is(0));
  }

  @Test
  public void testGroupManager() throws AdminException {
    AdminController ac = getAdminController();
    GroupProfileInst profile = ac.getGroupProfile("1");
    profile.addUser("1");
    ac.updateGroupProfile(profile);
    List<String> managerIds = AdminReference.getAdminService().getUserManageableGroupIds("1");
    assertThat(managerIds, hasSize(1));
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