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

package com.silverpeas.directory.control;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.silverpeas.directory.control.DirectorySessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.ProfileInst;
import com.stratelia.webactiv.beans.admin.SpaceInst;
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
    userscalled = directoryDSC.getLastListOfUsersCallded();
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
    userscalled = directoryDSC.getLastListOfUsersCallded();
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
    MainSessionController controller = mock(MainSessionController.class);
    final OrganizationController organisation = mock(OrganizationController.class);
    when(organisation.getAllUsers()).thenReturn(usersDomain.toArray(new UserDetail[3]));
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
    userscalled = directoryDSC.getLastListOfUsersCallded();
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
    ProfileInst pi = mock(ProfileInst.class);
    ArrayList<String> tableau =  new ArrayList<String>();
    tableau.add("1");
    tableau.add("2");
    tableau.add("3");
    
    when(pi.getAllUsers()).thenReturn(tableau);
    ComponentInst ci = mock(ComponentInst.class);
    ArrayList<ProfileInst> tableau_test = new ArrayList<ProfileInst>();
    tableau_test.add(pi);
    when(ci.getAllProfilesInst()).thenReturn(tableau_test);
    SpaceInst s = mock(SpaceInst.class);
    String[] tab = new String [0];
    when(s.getSubSpaceIds()).thenReturn(tab);
    ArrayList<ComponentInst> tableau_test2 = new ArrayList<ComponentInst>();
    tableau_test2.add(ci);
    when(s.getAllComponentsInst()).thenReturn(tableau_test2);
    
    MainSessionController controller = mock(MainSessionController.class);
    final OrganizationController organisation = mock(OrganizationController.class);
    when(controller.getOrganizationController()).thenReturn(organisation);
    when(organisation.getSpaceInstById("0")).thenReturn(s);
    when(organisation.getUserDetails(tableau.toArray(new String[tableau.size()]))).thenReturn(usersSpace.toArray(new UserDetail[usersSpace.size()]));
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
    userscalled = directoryDSC.getLastListOfUsersCallded();
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
    List<String> ol = new ArrayList<String>();
    List<String> nl = new ArrayList<String>();
    ol.add("1");
    ol.add("2");
    ol.add("5");
    nl.add("5");
    nl.add("3");
    nl.add("4");
    List<String> resultat = directoryDSC.fillList(ol, nl);
    assertNotNull(resultat);
    assertEquals("size 5", resultat.size(), 5);
    assertEquals(resultat.get(0), ol.get(0));
    assertEquals(resultat.get(1), ol.get(1));
    assertEquals(resultat.get(2), ol.get(2));
    assertEquals(resultat.get(3), nl.get(1));
    assertEquals(resultat.get(4), nl.get(2));
    
  }
}
