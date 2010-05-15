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

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.dbunit.JndiBasedDBTestCase;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;

import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.GroupProfileInst;
import com.stratelia.webactiv.beans.admin.UserDetail;

public class UsersAndGroupsTest extends JndiBasedDBTestCase {

  private String jndiName = "";

  @Override
  protected void setUp() throws Exception {
    prepareJndi();
    Hashtable<String, String> env = new Hashtable<String, String>();
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.fscontext.RefFSContextFactory");
    InitialContext ic = new InitialContext(env);
    Properties props = new Properties();
    props.load(UsersAndGroupsTest.class.getClassLoader().getResourceAsStream("jdbc.properties"));
    // Construct BasicDataSource reference
    Reference ref = new Reference("javax.sql.DataSource",
        "org.apache.commons.dbcp.BasicDataSourceFactory", null);
    ref.add(new StringRefAddr("driverClassName", props
        .getProperty("driverClassName")));
    ref.add(new StringRefAddr("url", props.getProperty("url")));
    ref.add(new StringRefAddr("username", props.getProperty("username")));
    ref.add(new StringRefAddr("password", props.getProperty("password")));
    ref.add(new StringRefAddr("maxActive", "4"));
    ref.add(new StringRefAddr("maxWait", "5000"));
    ref.add(new StringRefAddr("removeAbandoned", "true"));
    ref.add(new StringRefAddr("removeAbandonedTimeout", "5000"));
    ic.rebind(props.getProperty("jndi.name"), ref);
    jndiName = props.getProperty("jndi.name");
    super.setUp();
  }

  /**
   * Creates the directory for JNDI files System provider
   * @throws IOException
   */
  protected void prepareJndi() throws IOException {
    Properties jndiProperties = new Properties();
    jndiProperties.load(JndiBasedDBTestCase.class.getClassLoader().getResourceAsStream(
        "jndi.properties"));
    String jndiDirectoryPath = jndiProperties.getProperty(Context.PROVIDER_URL).substring(7);
    File jndiDirectory = new File(jndiDirectoryPath);
    if (!jndiDirectory.exists()) {
      jndiDirectory.mkdirs();
      jndiDirectory.mkdir();
    }
  }

  @Override
  protected String getLookupName() {
    return jndiName;
  }

  @Override
  protected IDataSet getDataSet() throws Exception {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSet(
        UsersAndGroupsTest.class
        .getResourceAsStream("test-usersandgroups-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    return dataSet;
  }

  @Test
  public void testAddUser() {
    UserDetail user = new UserDetail();
    user.setAccessLevel("A");
    user.setDomainId("0");
    user.seteMail("nicolas.eysseric@silverpeas.com");
    user.setFirstName("Nicolas");
    user.setLastName("EYSSERIC");
    user.setLogin("neysseri");
    AdminController ac = new AdminController(null);
    String userId = ac.addUser(user);
    assertEquals("2", userId);
  }

  @Test
  public void testUpdateUser() {
    AdminController ac = new AdminController(null);
    UserDetail user = ac.getUserDetail("1");
    String newEmail = "ney@silverpeas.com";
    user.seteMail(newEmail);
    ac.updateUser(user);
    user = ac.getUserDetail("1");
    assertEquals(newEmail, user.geteMail());
  }

  @Test
  public void testDeleteUser() {
    AdminController ac = new AdminController(null);
    String userId = ac.deleteUser("1");
    assertEquals("1", userId);
    UserDetail user = ac.getUserDetail(userId);
    assertEquals("R", user.getAccessLevel());
  }

  @Test
  public void testAddGroup() {
    AdminController ac = new AdminController(null);
    Group group = new Group();
    group.setDomainId("0");
    group.setName("Groupe 2");
    String groupId = ac.addGroup(group);
    assertEquals("2", groupId);
  }

  @Test
  public void testUpdateGroup() {
    AdminController ac = new AdminController(null);
    String desc = "New description";
    Group group = ac.getGroupById("1");
    group.setDescription(desc);
    ac.updateGroup(group);
    group = ac.getGroupById("1");
    assertEquals(desc, group.getDescription());
  }

  @Test
  public void testDeleteGroup() {
    AdminController ac = new AdminController(null);
    ac.deleteGroupById("1");
    Group group = ac.getGroupById("1");
    assertNull(group.getId());
  }

  @Test
  public void testUsersInGroup() {
    AdminController ac = new AdminController(null);
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
    AdminController ac = new AdminController(null);
    GroupProfileInst profile = ac.getGroupProfile("1");
    profile.addUser("1");
    ac.updateGroupProfile(profile);
    Admin admin = new Admin();
    List<String> managerIds = admin.getUserManageableGroupIds("1");
    assertEquals(1, managerIds.size());
  }

  @Override
  protected DatabaseOperation getTearDownOperation() throws Exception {
    return DatabaseOperation.DELETE_ALL;
  }

}