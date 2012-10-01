/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.jobStartPagePeas.control;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.silverpeas.components.model.AbstractTestDao;
import com.silverpeas.jobStartPagePeas.DisplaySorted;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.SpaceProfileInst;
import com.stratelia.webactiv.beans.admin.UserDetail;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author ehugonnet
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-domains.xml", "/spring-jpa-datasource.xml"})
public class JobStartPagePeasSessionControllerTest extends AbstractTestDao {

  @Override
  protected String getDatasetFileName() {
    return "test-spacesmanagers-dataset.xml";
  }

  public JobStartPagePeasSessionController getInstance(String userId) {
    AdminController adminController = new AdminController(userId);
    UserDetail user = adminController.getUserDetail(userId);
    MainSessionController controller = mock(MainSessionController.class);
    Selection selection = new Selection();
    when(controller.getSelection()).thenReturn(selection);
    when(controller.getCurrentUserDetail()).thenReturn(user);
    final OrganizationController organisation = mock(OrganizationController.class);
    when(controller.getOrganizationController()).thenReturn(organisation);
    ComponentContext context = mock(ComponentContext.class);
    JobStartPagePeasSessionController instance = new JobStartPagePeasSessionController(controller,
        context);
    return instance;
  }

  /**
   * Test of isInheritanceEnable method, of class JobStartPagePeasSessionController.
   */
  @Test
  public void testIsInheritanceEnable() {
    JobStartPagePeasSessionController instance = getInstance("1");
    assertThat(instance.isInheritanceEnable(), is(true));
  }

  /**
   * Test of isJSR168Used method, of class JobStartPagePeasSessionController.
   */
  @Test
  public void testIsJSR168Used() {
    JobStartPagePeasSessionController instance = getInstance("1");
    assertThat(instance.isJSR168Used(), is(true));
  }

  /**
   * Test of isUserAdmin method, of class JobStartPagePeasSessionController.
   */
  @Test
  public void testIsUserAdmin() {
    JobStartPagePeasSessionController instance = getInstance("1");
    assertThat(instance.isUserAdmin(), is(true));
    instance = getInstance("3");
    assertThat(instance.getUserDetail().getId(), is("3"));
    assertThat(instance.isUserAdmin(), is(false));
  }

  /**
   * Test of getSpaceInstById method, of class JobStartPagePeasSessionController.
   */
  @Test
  public void testGetSpaceInstById() {
    JobStartPagePeasSessionController instance = getInstance("1");
    SpaceInst result = instance.getSpaceInstById();
    assertThat(result, is(nullValue()));
    instance.setManagedSpaceId("1", true);
    result = instance.getSpaceInstById();
    assertThat(result.getId(), is("WA1"));
    assertThat(result.getName(), is("Space 1"));
    assertThat(result.getDescription(), is("Root Space"));
  }

  /**
   * Test of getManagedSpace method, of class JobStartPagePeasSessionController.
   */
  @Test
  public void testGetManagedSpace() {
    JobStartPagePeasSessionController instance = getInstance("1");
    instance.setManagedSpaceId("1", true);
    instance.init();
    DisplaySorted result = instance.getManagedSpace();
    assertThat(result, is(notNullValue()));
    assertThat(result.id, is("1"));
    assertThat(result.name, is("Space 1"));
    assertThat(result.htmlLine, is("<option value=1>Space 1</option>"));
  }

  /**
   * Test of getManagedSpaceComponents method, of class JobStartPagePeasSessionController.
   */
  @Test
  public void testGetManagedSpaceComponents() {
    JobStartPagePeasSessionController instance = getInstance("1");
    instance.init();
    instance.setSpaceId("1");
    DisplaySorted[] result = instance.getManagedSpaceComponents();
    assertThat(result, is(notNullValue()));
    assertThat(result.length, is(1));
    assertThat(result[0].id, is("kmelia1"));
    assertThat(result[0].name, is("GED"));
    assertThat(result[0].htmlLine, is("<a href=\"GoToComponent?ComponentId=kmelia1\" "
        + "TARGET=\"startPageContent\"><img name=\"element0\" "
        + "src=\"/silverpeas//util/icons/component/kmeliaSmall.gif\" "
        + "class=\"component-icon\"/>&nbsp</a><a href=\"GoToComponent?ComponentId=kmelia1\" "
        + "TARGET=\"startPageContent\">GED</a><br>"));
  }

  /**
   * Test of updateSpaceRole method, of class JobStartPagePeasSessionController.
   */
  @Test
  public void testUpdateSpaceRoleForSpaceManager() {
    JobStartPagePeasSessionController instance = getInstance("1");
    instance.init();
    instance.setSpaceId("1");
    List<UserDetail> users = instance.getAllCurrentUserSpace(SpaceProfileInst.SPACE_MANAGER);
    assertThat(users, is(notNullValue()));
    assertThat(users, hasSize(1));
    instance.getSelection().setSelectedElements(new String[]{"1", "3"});
    instance.updateSpaceRole(SpaceProfileInst.SPACE_MANAGER);
    users = instance.getAllCurrentUserSpace(SpaceProfileInst.SPACE_MANAGER);
    assertThat(users, is(notNullValue()));
    assertThat(users, hasSize(2));
    instance.setSubSpaceId("2");
    users = instance.getAllCurrentUserSpace(SpaceProfileInst.SPACE_MANAGER);
    assertThat(users, is(notNullValue()));
    assertThat(users, hasSize(0));
   /* SpaceInst spaceint1 = instance.getSpaceInstById();
    SpaceProfileInst inheritedProfile = spaceint1.getInheritedSpaceProfileInst(
        SpaceProfileInst.SPACE_MANAGER);
    assertThat(inheritedProfile, is(notNullValue()));
    List<String> inheritedUsers = inheritedProfile.getAllUsers();
    assertThat(inheritedUsers, is(notNullValue()));
    assertThat(inheritedUsers, hasSize(2));*/
  }

  /**
   * Test of updateSpaceRole method, of class JobStartPagePeasSessionController.
   */
  @Test
  public void testUpdateSpaceRoleFoUser() {
    String role = "user";
    JobStartPagePeasSessionController instance = getInstance("1");
    instance.init();
    instance.setSpaceId("1");
    List<UserDetail> users = instance.getAllCurrentUserSpace(role);
    assertThat(users, is(notNullValue()));
    assertThat(users, hasSize(0));
    instance.getSelection().setSelectedElements(new String[]{"1"});
    instance.createSpaceRole(role);
    users = instance.getAllCurrentUserSpace(role);
    assertThat(users, is(notNullValue()));
    assertThat(users, hasSize(1));
    instance.getSelection().setSelectedElements(new String[]{"1", "3"});
    instance.updateSpaceRole(role);
    users = instance.getAllCurrentUserSpace(role);
    assertThat(users, is(notNullValue()));
    assertThat(users, hasSize(2));
    instance.setSubSpaceId("2");
    users = instance.getAllCurrentUserSpace(role);
    assertThat(users, is(notNullValue()));
    assertThat(users, hasSize(0));/*
     SpaceInst spaceint1 = instance.getSpaceInstById();
     SpaceProfileInst inheritedProfile = spaceint1.getInheritedSpaceProfileInst(role);
     assertThat(inheritedProfile, is(notNullValue()));
     List<String> inheritedUsers = inheritedProfile.getAllUsers();
     assertThat(inheritedUsers, is(notNullValue()));
     assertThat(inheritedUsers, hasSize(2));*/
  }
}
