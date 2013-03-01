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

package com.silverpeas.directory.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.beans.admin.Domain;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;

public class DirectorySessionControllerTest {

  @Test
  public void testGetAllUsers() throws Exception {
    List<UserDetail> users = new ArrayList<UserDetail>();
    UserDetail user1 = new UserDetail();
    user1.setId("1");
    user1.setLastName("bourakbi");
    user1.setFirstName("nidale");
    user1.seteMail("nidale.bourakbi@gmail.com");
    user1.setLogin("bourakbn");
    UserDetail user2 = new UserDetail();
    user2.setId("2");
    user2.setLastName("bensalem");
    user2.setFirstName("nabil");
    user2.seteMail("nabil@gmail.com");
    user2.setLogin("nabil");
    UserDetail user3 = new UserDetail();
    user3.setId("3");
    user3.setLastName("simpson");
    user3.setFirstName("nabil");
    user3.seteMail("nabil@gmail.com");
    user3.setLogin("nabil");
    users.add(user1);
    users.add(user2);
    users.add(user3);

    MainSessionController controller = mock(MainSessionController.class);
    final OrganizationController organisation = mock(OrganizationController.class);
    when(organisation.getAllUsers()).thenReturn(users.toArray(new UserDetail[3]));
    when(controller.getOrganizationController()).thenReturn(organisation);
    ComponentContext context = mock(ComponentContext.class);
    when(context.getCurrentComponentId()).thenReturn("directory12");
    DirectorySessionController directoryDSC = new DirectorySessionController(controller, context);
    // All users
    List<UserDetail> userscalled = directoryDSC.getAllUsers();
    assertNotNull(userscalled);
    assertEquals(3, userscalled.size());
    assertEquals(users.get(0), userscalled.get(0));
    assertEquals(users.get(1), userscalled.get(1));
    assertEquals(users.get(2), userscalled.get(2));
    // index: B
    userscalled = directoryDSC.getUsersByIndex("B");
    assertNotNull(userscalled);
    assertEquals(2, userscalled.size());
    assertEquals(users.get(0), userscalled.get(0));
    assertEquals(users.get(1), userscalled.get(1));
    // index: tous
    userscalled = directoryDSC.getLastListOfAllUsers();
    assertNotNull(userscalled);
    assertEquals(3, userscalled.size());
    assertEquals(users.get(0), userscalled.get(0));
    assertEquals(users.get(1), userscalled.get(1));
    assertEquals(users.get(2), userscalled.get(2));
    // pagination
    userscalled = directoryDSC.getLastListOfUsersCalled();
    assertNotNull(userscalled);
    assertEquals(3, userscalled.size());
    assertEquals(users.get(0), userscalled.get(0));

  }

  @Test
  public void testGetAllUsersByGroup() throws Exception {
    List<UserDetail> usersGroup = new ArrayList<UserDetail>();
    UserDetail user2 = new UserDetail();
    user2.setId("2");
    user2.setLastName("bensalem");
    user2.setFirstName("nabil");
    user2.seteMail("nabil@gmail.com");
    user2.setLogin("nabil");
    UserDetail user3 = new UserDetail();
    user3.setId("3");
    user3.setLastName("simpson");
    user3.setFirstName("nabil");
    user3.seteMail("nabil@gmail.com");
    user3.setLogin("nabil");
    usersGroup.add(user2);
    usersGroup.add(user3);

    MainSessionController controller = mock(MainSessionController.class);
    final OrganizationController organisation = mock(OrganizationController.class);
    when(organisation.getAllUsersOfGroup("2")).thenReturn(usersGroup.toArray(new UserDetail[2]));
    when(controller.getOrganizationController()).thenReturn(organisation);
    ComponentContext context = mock(ComponentContext.class);
    when(context.getCurrentComponentId()).thenReturn("directory12");
    DirectorySessionController directoryDSC = new DirectorySessionController(controller, context);
    List<UserDetail> userscalled;
    userscalled = directoryDSC.getAllUsersByGroup("2");
    // All users By group
    assertNotNull(userscalled);
    assertEquals(2, userscalled.size());
    assertEquals(usersGroup.get(0), userscalled.get(0));
    assertEquals(usersGroup.get(1), userscalled.get(1));
    // index: S
    userscalled = directoryDSC.getUsersByIndex("S");
    assertNotNull(userscalled);
    assertEquals(1, userscalled.size());
    assertEquals(usersGroup.get(1), userscalled.get(0));
    // index: B
    userscalled = directoryDSC.getUsersByIndex("B");
    assertNotNull(userscalled);
    assertEquals(1, userscalled.size());
    assertEquals(usersGroup.get(0), userscalled.get(0));
    // index: tous
    userscalled = directoryDSC.getLastListOfAllUsers();
    assertNotNull(userscalled);
    assertEquals(2, userscalled.size());
    assertEquals(usersGroup.get(0), userscalled.get(0));
    assertEquals(usersGroup.get(1), userscalled.get(1));
    // pagination
    userscalled = directoryDSC.getLastListOfUsersCalled();
    assertNotNull(userscalled);
    assertEquals(2, userscalled.size());
    assertEquals(usersGroup.get(0), userscalled.get(0));
    assertEquals(usersGroup.get(1), userscalled.get(1));
  }
  
  @Test
  public void testGetAllUsersByDomain() throws Exception {
    List<UserDetail> usersDomain = new ArrayList<UserDetail>();
    UserDetail user1 = new UserDetail();
    user1.setId("1");
    user1.setLastName("durand");
    user1.setFirstName("julien");
    user1.seteMail("julien.durand@gmail.com");
    user1.setLogin("julin");
    user1.setDomainId("3");
    UserDetail user2 = new UserDetail();
    user2.setId("2");
    user2.setLastName("groland");
    user2.setFirstName("françois");
    user2.seteMail("françois@gmail.com");
    user2.setLogin("françois");
    user2.setDomainId("3");
    UserDetail user3 = new UserDetail();
    user3.setId("3");
    user3.setLastName("simpson");
    user3.setFirstName("nabil");
    user3.seteMail("nabil@gmail.com");
    user3.setLogin("nabil");
    user3.setDomainId("3");
    usersDomain.add(user1);
    usersDomain.add(user2);
    usersDomain.add(user3);
    Domain domain = new Domain();
    domain.setId("3");
    MainSessionController controller = mock(MainSessionController.class);
    final OrganizationController organisation = mock(OrganizationController.class);
    List<String> domainIds = new ArrayList<String>();
    domainIds.add("3");
    when(organisation.getUsersOfDomains(domainIds)).thenReturn(usersDomain);
    when(organisation.getDomain("3")).thenReturn(domain);
    when(controller.getOrganizationController()).thenReturn(organisation);
    ComponentContext context = mock(ComponentContext.class);
    when(context.getCurrentComponentId()).thenReturn("directory12");
    DirectorySessionController directoryDSC = new DirectorySessionController(controller, context);
    List<UserDetail> userscalled = directoryDSC.getAllUsersByDomain("3");
    // All users By domain
    assertNotNull(userscalled);
    assertEquals(3, userscalled.size());
    assertEquals(usersDomain.get(0), userscalled.get(0));
    assertEquals(usersDomain.get(1), userscalled.get(1));
    assertEquals(usersDomain.get(2), userscalled.get(2));
    // index: G
    userscalled = directoryDSC.getUsersByIndex("G");
    assertNotNull(userscalled);
    assertEquals(1, userscalled.size());
    assertEquals(usersDomain.get(1), userscalled.get(0));
    // index: D
    userscalled = directoryDSC.getUsersByIndex("D");
    assertNotNull(userscalled);
    assertEquals(1, userscalled.size());
    assertEquals(usersDomain.get(0), userscalled.get(0));
    // index: tous
    userscalled = directoryDSC.getLastListOfAllUsers();
    assertNotNull(userscalled);
    assertEquals(3, userscalled.size());
    assertEquals(usersDomain.get(0), userscalled.get(0));
    assertEquals(usersDomain.get(1), userscalled.get(1));
    assertEquals(usersDomain.get(2), userscalled.get(2));
    // pagination
    userscalled = directoryDSC.getLastListOfUsersCalled();
    assertNotNull(userscalled);
    assertEquals(3, userscalled.size());
    assertEquals(usersDomain.get(0), userscalled.get(0));
    assertEquals(usersDomain.get(1), userscalled.get(1));
    assertEquals(usersDomain.get(2), userscalled.get(2));
  }

  @Test
  public void testGetAllUsersBySpace() throws Exception {
    List<UserDetail> usersSpace = new ArrayList<UserDetail>();
    UserDetail user1 = new UserDetail();
    user1.setId("1");
    user1.setLastName("durand");
    user1.setFirstName("julien");
    user1.seteMail("julien.durand@gmail.com");
    user1.setLogin("julin");    
    UserDetail user2 = new UserDetail();
    user2.setId("2");
    user2.setLastName("groland");
    user2.setFirstName("françois");
    user2.seteMail("françois@gmail.com");
    user2.setLogin("françois");
    UserDetail user3 = new UserDetail();
    user3.setId("3");
    user3.setLastName("simpson");
    user3.setFirstName("nabil");
    user3.seteMail("nabil@gmail.com");
    user3.setLogin("nabil");
    usersSpace.add(user1);
    usersSpace.add(user2);
    usersSpace.add(user3);
    
    MainSessionController controller = mock(MainSessionController.class);
    final OrganizationController organisation = mock(OrganizationController.class);
    when(controller.getOrganizationController()).thenReturn(organisation);
    
    String[] componentIds = {"kmelia12", "webPages245"};
    when(organisation.getAllComponentIdsRecur("0")).thenReturn(componentIds);
    
    UserDetail[] component1 = {user1, user2};
    UserDetail[] component2 = {user1, user3};
    Map<String, UserDetail[]> components = new HashMap<String, UserDetail[]>();
    components.put("kmelia12", component1);
    components.put("webPages245", component2);
    when(organisation.getAllUsers("kmelia12")).thenReturn(components.get("kmelia12"));
    when(organisation.getAllUsers("webPages245")).thenReturn(components.get("webPages245"));
    
    ComponentContext context = mock(ComponentContext.class);
    when(context.getCurrentComponentId()).thenReturn("directory12");
    DirectorySessionController directoryDSC = new DirectorySessionController(controller, context);
    List<UserDetail> userscalled = directoryDSC.getAllUsersBySpace("0");
    // All users By domain
    assertNotNull(userscalled);
    assertEquals(3, userscalled.size());
    assertEquals(usersSpace.get(0), userscalled.get(0));
    assertEquals(usersSpace.get(1), userscalled.get(1));
    assertEquals(usersSpace.get(2), userscalled.get(2));
    // index: G
    userscalled = directoryDSC.getUsersByIndex("G");
    assertNotNull(userscalled);
    assertEquals(1, userscalled.size());
    assertEquals(usersSpace.get(1), userscalled.get(0));
    // index: D
    userscalled = directoryDSC.getUsersByIndex("D");
    assertNotNull(userscalled);
    assertEquals(1, userscalled.size());
    assertEquals(usersSpace.get(0), userscalled.get(0));
    // index: tous
    userscalled = directoryDSC.getLastListOfAllUsers();
    assertNotNull(userscalled);
    assertEquals(3, userscalled.size());
    assertEquals(usersSpace.get(0), userscalled.get(0));
    assertEquals(usersSpace.get(1), userscalled.get(1));
    assertEquals(usersSpace.get(2), userscalled.get(2));
    // pagination
    userscalled = directoryDSC.getLastListOfUsersCalled();
    assertNotNull(userscalled);
    assertEquals(3, userscalled.size());
    assertEquals(usersSpace.get(0), userscalled.get(0));
    assertEquals(usersSpace.get(1), userscalled.get(1));
    assertEquals(usersSpace.get(2), userscalled.get(2));
  }
  
  @Test
  public void testFillList(){
    MainSessionController controller = mock(MainSessionController.class);
    final OrganizationController organisation = mock(OrganizationController.class);
    when(controller.getOrganizationController()).thenReturn(organisation);
    ComponentContext context = mock(ComponentContext.class);
    when(context.getCurrentComponentId()).thenReturn("directory12");
    DirectorySessionController directoryDSC = new DirectorySessionController(controller, context);
    List<UserDetail> ol = new ArrayList<UserDetail>();
    UserDetail[] nl = new UserDetail[3];
    UserDetail u1 = new UserDetail();
    u1.setId("1");
    UserDetail u2 = new UserDetail();
    u2.setId("2");
    UserDetail u5 = new UserDetail();
    u5.setId("5");
    ol.add(u1);
    ol.add(u2);
    ol.add(u5);
    nl[0] = new UserDetail();
    nl[0].setId("5");
    nl[1] = new UserDetail();
    nl[1].setId("3");
    nl[2] = new UserDetail();
    nl[2].setId("4");
    directoryDSC.fillList(ol, nl);
    assertNotNull(ol);
    assertEquals("size 5", ol.size(), 5);
    assertEquals(ol.get(0), ol.get(0));
    assertEquals(ol.get(1), ol.get(1));
    assertEquals(ol.get(2), ol.get(2));
    assertEquals(ol.get(3), nl[1]);
    assertEquals(ol.get(4), nl[2]);
  }
}
