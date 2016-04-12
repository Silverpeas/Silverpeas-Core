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

package org.silverpeas.web.directory.control;

import org.silverpeas.web.directory.model.DirectoryItemList;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.test.TestBeanContainer;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.component.ComponentHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DirectorySessionControllerTest {

  private OrganizationController mockOrganizationController;

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  @Before
  public void setup() throws Exception {
    mockOrganizationController = mock(OrganizationController.class);
    when(TestBeanContainer.getMockedBeanContainer().getBeanByType(OrganizationController.class))
        .thenReturn(mockOrganizationController);
    when(TestBeanContainer.getMockedBeanContainer().getBeanByType(ComponentHelper.class))
        .thenReturn(mock(ComponentHelper.class));
  }

  @Test
  public void testGetAllUsers() throws Exception {
    List<UserDetail> users = new ArrayList<>();
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
    DirectoryItemList userExpectedItems = new DirectoryItemList(users);

    MainSessionController controller = mock(MainSessionController.class);
    when(mockOrganizationController.getAllUsers()).thenReturn(users.toArray(new UserDetail[3]));
    when(mockOrganizationController.getComponentIdsForUser(anyString(), anyString()))
        .thenReturn(new String[0]);
    when(controller.getCurrentUserDetail()).thenReturn(new UserDetail());
    ComponentContext context = mock(ComponentContext.class);
    when(context.getCurrentComponentId()).thenReturn("directory12");
    DirectorySessionController directoryDSC = new DirectorySessionController(controller, context);
    // All users
    DirectoryItemList userCalledItems = directoryDSC.getAllUsers();
    assertNotNull(userCalledItems);
    assertEquals(3, userCalledItems.size());
    assertEquals(userExpectedItems.get(0), userCalledItems.get(0));
    assertEquals(userExpectedItems.get(1), userCalledItems.get(1));
    assertEquals(userExpectedItems.get(2), userCalledItems.get(2));
    // index: B
    userCalledItems = directoryDSC.getUsersByIndex("B");
    assertNotNull(userCalledItems);
    assertEquals(2, userCalledItems.size());
    assertEquals(userExpectedItems.get(0), userCalledItems.get(0));
    assertEquals(userExpectedItems.get(1), userCalledItems.get(1));
    // index: tous
    userCalledItems = directoryDSC.getLastListOfAllUsers();
    assertNotNull(userCalledItems);
    assertEquals(3, userCalledItems.size());
    assertEquals(userExpectedItems.get(0), userCalledItems.get(0));
    assertEquals(userExpectedItems.get(1), userCalledItems.get(1));
    assertEquals(userExpectedItems.get(2), userCalledItems.get(2));
    // pagination
    userCalledItems = directoryDSC.getLastListOfUsersCalled();
    assertNotNull(userCalledItems);
    assertEquals(3, userCalledItems.size());
    assertEquals(userExpectedItems.get(0), userCalledItems.get(0));

  }

  @Test
  public void testGetAllUsersByGroup() throws Exception {
    List<UserDetail> groupOfUsers = new ArrayList<>();
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
    groupOfUsers.add(user2);
    groupOfUsers.add(user3);
    DirectoryItemList usersOfGroupExpectedItems = new DirectoryItemList(groupOfUsers);

    MainSessionController controller = mock(MainSessionController.class);
    when(mockOrganizationController.getAllUsersOfGroup("2"))
        .thenReturn(groupOfUsers.toArray(new UserDetail[2]));
    when(mockOrganizationController.getComponentIdsForUser(anyString(), anyString()))
        .thenReturn(new String[0]);
    when(controller.getCurrentUserDetail()).thenReturn(new UserDetail());
    ComponentContext context = mock(ComponentContext.class);
    when(context.getCurrentComponentId()).thenReturn("directory12");
    DirectorySessionController directoryDSC = new DirectorySessionController(controller, context);
    DirectoryItemList usersOfGroupCalledItems = directoryDSC.getAllUsersByGroup("2");
    // All users By group
    assertNotNull(usersOfGroupCalledItems);
    assertEquals(2, usersOfGroupCalledItems.size());
    assertEquals(usersOfGroupExpectedItems.get(0), usersOfGroupCalledItems.get(0));
    assertEquals(usersOfGroupExpectedItems.get(1), usersOfGroupCalledItems.get(1));
    // index: S
    usersOfGroupCalledItems = directoryDSC.getUsersByIndex("S");
    assertNotNull(usersOfGroupCalledItems);
    assertEquals(1, usersOfGroupCalledItems.size());
    assertEquals(usersOfGroupExpectedItems.get(1), usersOfGroupCalledItems.get(0));
    // index: B
    usersOfGroupCalledItems = directoryDSC.getUsersByIndex("B");
    assertNotNull(usersOfGroupCalledItems);
    assertEquals(1, usersOfGroupCalledItems.size());
    assertEquals(usersOfGroupExpectedItems.get(0), usersOfGroupCalledItems.get(0));
    // index: tous
    usersOfGroupCalledItems = directoryDSC.getLastListOfAllUsers();
    assertNotNull(usersOfGroupCalledItems);
    assertEquals(2, usersOfGroupCalledItems.size());
    assertEquals(usersOfGroupExpectedItems.get(0), usersOfGroupCalledItems.get(0));
    assertEquals(usersOfGroupExpectedItems.get(1), usersOfGroupCalledItems.get(1));
    // pagination
    usersOfGroupCalledItems = directoryDSC.getLastListOfUsersCalled();
    assertNotNull(usersOfGroupCalledItems);
    assertEquals(2, usersOfGroupCalledItems.size());
    assertEquals(usersOfGroupExpectedItems.get(0), usersOfGroupCalledItems.get(0));
    assertEquals(usersOfGroupExpectedItems.get(1), usersOfGroupCalledItems.get(1));
  }

  @Test
  public void testGetAllUsersByDomain() throws Exception {
    List<UserDetail> usersOfDomain = new ArrayList<>();
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
    usersOfDomain.add(user1);
    usersOfDomain.add(user2);
    usersOfDomain.add(user3);
    DirectoryItemList usersOfDomainExpectedItems = new DirectoryItemList(usersOfDomain);

    Domain domain = new Domain();
    domain.setId("3");
    MainSessionController controller = mock(MainSessionController.class);
    List<String> domainIds = new ArrayList<>();
    domainIds.add("3");
    when(mockOrganizationController.getUsersOfDomains(domainIds)).thenReturn(usersOfDomain);
    when(mockOrganizationController.getDomain("3")).thenReturn(domain);
    when(mockOrganizationController.getComponentIdsForUser(anyString(), anyString()))
        .thenReturn(new String[0]);
    when(controller.getCurrentUserDetail()).thenReturn(new UserDetail());
    ComponentContext context = mock(ComponentContext.class);
    when(context.getCurrentComponentId()).thenReturn("directory12");
    DirectorySessionController directoryDSC = new DirectorySessionController(controller, context);
    DirectoryItemList usersOfDomainCalledItems = directoryDSC.getAllUsersByDomain("3");
    // All users By domain
    assertNotNull(usersOfDomainCalledItems);
    assertEquals(3, usersOfDomainCalledItems.size());
    assertEquals(usersOfDomainExpectedItems.get(0), usersOfDomainCalledItems.get(0));
    assertEquals(usersOfDomainExpectedItems.get(1), usersOfDomainCalledItems.get(1));
    assertEquals(usersOfDomainExpectedItems.get(2), usersOfDomainCalledItems.get(2));
    // index: G
    usersOfDomainCalledItems = directoryDSC.getUsersByIndex("G");
    assertNotNull(usersOfDomainCalledItems);
    assertEquals(1, usersOfDomainCalledItems.size());
    assertEquals(usersOfDomainExpectedItems.get(1), usersOfDomainCalledItems.get(0));
    // index: D
    usersOfDomainCalledItems = directoryDSC.getUsersByIndex("D");
    assertNotNull(usersOfDomainCalledItems);
    assertEquals(1, usersOfDomainCalledItems.size());
    assertEquals(usersOfDomainExpectedItems.get(0), usersOfDomainCalledItems.get(0));
    // index: tous
    usersOfDomainCalledItems = directoryDSC.getLastListOfAllUsers();
    assertNotNull(usersOfDomainCalledItems);
    assertEquals(3, usersOfDomainCalledItems.size());
    assertEquals(usersOfDomainExpectedItems.get(0), usersOfDomainCalledItems.get(0));
    assertEquals(usersOfDomainExpectedItems.get(1), usersOfDomainCalledItems.get(1));
    assertEquals(usersOfDomainExpectedItems.get(2), usersOfDomainCalledItems.get(2));
    // pagination
    usersOfDomainCalledItems = directoryDSC.getLastListOfUsersCalled();
    assertNotNull(usersOfDomainCalledItems);
    assertEquals(3, usersOfDomainCalledItems.size());
    assertEquals(usersOfDomainExpectedItems.get(0), usersOfDomainCalledItems.get(0));
    assertEquals(usersOfDomainExpectedItems.get(1), usersOfDomainCalledItems.get(1));
    assertEquals(usersOfDomainExpectedItems.get(2), usersOfDomainCalledItems.get(2));
  }

  @Test
  public void testGetAllUsersBySpace() throws Exception {
    List<UserDetail> usersOfSpace = new ArrayList<>();
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
    usersOfSpace.add(user1);
    usersOfSpace.add(user2);
    usersOfSpace.add(user3);
    DirectoryItemList usersOfSpaceExpectedItems = new DirectoryItemList(usersOfSpace);

    MainSessionController controller = mock(MainSessionController.class);

    String[] componentIds = {"kmelia12", "webPages245"};
    when(mockOrganizationController.getAllComponentIdsRecur("0")).thenReturn(componentIds);

    UserDetail[] component1 = {user1, user2};
    UserDetail[] component2 = {user1, user3};
    Map<String, UserDetail[]> components = new HashMap<>();
    components.put("kmelia12", component1);
    components.put("webPages245", component2);
    when(mockOrganizationController.getAllUsers("kmelia12")).thenReturn(components.get("kmelia12"));
    when(mockOrganizationController.getAllUsers("webPages245"))
        .thenReturn(components.get("webPages245"));
    when(mockOrganizationController.getComponentIdsForUser(anyString(), anyString()))
        .thenReturn(new String[0]);

    when(controller.getCurrentUserDetail()).thenReturn(new UserDetail());

    ComponentContext context = mock(ComponentContext.class);
    when(context.getCurrentComponentId()).thenReturn("directory12");
    DirectorySessionController directoryDSC = new DirectorySessionController(controller, context);
    DirectoryItemList usersOfSpaceCalledItems = directoryDSC.getAllUsersBySpace("0");
    // All users By domain
    assertNotNull(usersOfSpaceCalledItems);
    assertEquals(3, usersOfSpaceCalledItems.size());
    assertEquals(usersOfSpaceExpectedItems.get(0), usersOfSpaceCalledItems.get(0));
    assertEquals(usersOfSpaceExpectedItems.get(1), usersOfSpaceCalledItems.get(1));
    assertEquals(usersOfSpaceExpectedItems.get(2), usersOfSpaceCalledItems.get(2));
    // index: G
    usersOfSpaceCalledItems = directoryDSC.getUsersByIndex("G");
    assertNotNull(usersOfSpaceCalledItems);
    assertEquals(1, usersOfSpaceCalledItems.size());
    assertEquals(usersOfSpaceExpectedItems.get(1), usersOfSpaceCalledItems.get(0));
    // index: D
    usersOfSpaceCalledItems = directoryDSC.getUsersByIndex("D");
    assertNotNull(usersOfSpaceCalledItems);
    assertEquals(1, usersOfSpaceCalledItems.size());
    assertEquals(usersOfSpaceExpectedItems.get(0), usersOfSpaceCalledItems.get(0));
    // index: tous
    usersOfSpaceCalledItems = directoryDSC.getLastListOfAllUsers();
    assertNotNull(usersOfSpaceCalledItems);
    assertEquals(3, usersOfSpaceCalledItems.size());
    assertEquals(usersOfSpaceExpectedItems.get(0), usersOfSpaceCalledItems.get(0));
    assertEquals(usersOfSpaceExpectedItems.get(1), usersOfSpaceCalledItems.get(1));
    assertEquals(usersOfSpaceExpectedItems.get(2), usersOfSpaceCalledItems.get(2));
    // pagination
    usersOfSpaceCalledItems = directoryDSC.getLastListOfUsersCalled();
    assertNotNull(usersOfSpaceCalledItems);
    assertEquals(3, usersOfSpaceCalledItems.size());
    assertEquals(usersOfSpaceExpectedItems.get(0), usersOfSpaceCalledItems.get(0));
    assertEquals(usersOfSpaceExpectedItems.get(1), usersOfSpaceCalledItems.get(1));
    assertEquals(usersOfSpaceExpectedItems.get(2), usersOfSpaceCalledItems.get(2));
  }

  @Test
  public void testMergeList(){
    MainSessionController controller = mock(MainSessionController.class);
    ComponentContext context = mock(ComponentContext.class);
    when(context.getCurrentComponentId()).thenReturn("directory12");
    DirectorySessionController directoryDSC = new DirectorySessionController(controller, context);
    List<UserDetail> ol = new ArrayList<>();
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
    DirectoryItemList userItemsAtStart = new DirectoryItemList(ol);
    DirectoryItemList userItemsToMerge = new DirectoryItemList(nl);
    DirectoryItemList userItemsAfterMerge = new DirectoryItemList(ol);
    directoryDSC.mergeUsersIntoDirectoryItemList(nl, userItemsAfterMerge);
    assertNotNull(ol);
    assertEquals("size 5", 5, userItemsAfterMerge.size());
    assertEquals(userItemsAtStart.get(0), userItemsAfterMerge.get(0));
    assertEquals(userItemsAtStart.get(1), userItemsAfterMerge.get(1));
    assertEquals(userItemsAtStart.get(2), userItemsAfterMerge.get(2));
    assertEquals(userItemsToMerge.get(1), userItemsAfterMerge.get(3));
    assertEquals(userItemsToMerge.get(2), userItemsAfterMerge.get(4));
  }
}
