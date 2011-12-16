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

package com.silverpeas.admin;

import com.silverpeas.admin.components.Instanciateur;
import com.silverpeas.admin.components.WAComponent;
import com.silverpeas.components.model.AbstractTestDao;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.ProfileInst;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.beans.admin.SpaceProfileInst;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-domains.xml", "/spring-jdbc-datasource.xml"})
public class SpacesAndComponentsTest extends AbstractTestDao {
  
  @Inject
  Admin admin;
  
  @Inject
  OrganizationController organizationController;
  
  String userId = "1";

  private AdminController getAdminController() {
    AdminController ac = new AdminController(userId);
    return ac;
  }
  
  @Before
  public void clearCache() {
    assertNotNull(admin);
    admin.reloadCache();
  }

  @Test
  public void testAddSpace() {
    AdminController ac = getAdminController();

    // test space creation
    SpaceInst space = new SpaceInst();
    space.setCreatorUserId(userId);
    space.setName("Space 3");
    String spaceId = ac.addSpaceInst(space);
    assertNotNull(spaceId);
    assertEquals("WA4", spaceId);

    // test subspace creation
    SpaceInst subspace = new SpaceInst();
    subspace.setCreatorUserId(userId);
    subspace.setName("Space 3 - 1");
    subspace.setDomainFatherId("4");
    String subSpaceId = ac.addSpaceInst(subspace);
    assertNotNull(subSpaceId);
    assertEquals("WA5", subSpaceId);

    // test subspace of root space
    String[] subSpaceIds = ac.getAllSubSpaceIds("WA4");
    assertEquals(1, subSpaceIds.length);

    // test level calculation
    subspace = ac.getSpaceInstById("WA5");
    assertEquals(1, subspace.getLevel());

    SpaceInstLight subspaceLight = ac.getSpaceInstLight("WA5");
    assertEquals(1, subspaceLight.getLevel());
  }

  @Test
  public void testUpdateSpace() {
    AdminController ac = getAdminController();
    String spaceId = "WA1";
    SpaceInst space = ac.getSpaceInstById(spaceId);
    assertNull(space.getDescription());
    SpaceInstLight spaceLight = ac.getSpaceInstLight(spaceId);
    assertNull(spaceLight.getDescription());
    String desc = "New description";
    space.setDescription(desc);
    ac.updateSpaceInst(space);

    space = ac.getSpaceInstById(spaceId);
    assertEquals(desc, space.getDescription());

    spaceLight = ac.getSpaceInstLight(spaceId);
    assertEquals(desc, spaceLight.getDescription());
  }

  @Test
  public void testDeleteSpace() throws AdminException {
    admin.deleteSpaceInstById(userId, "WA1", false);

    SpaceInst space = organizationController.getSpaceInstById("WA1");
    assertEquals("R", space.getStatus());

    admin.deleteSpaceInstById(userId, "WA1", true);
    space = organizationController.getSpaceInstById("WA1");
    assertNull(space);
  }

  public void testAddSubSpace() {
    AdminController ac = getAdminController();

    // test space creation
    SpaceInst space = new SpaceInst();
    space.setCreatorUserId(userId);
    space.setName("Space 1-3");
    space.setDomainFatherId("WA1");
    String spaceId = ac.addSpaceInst(space);
    assertNotNull(spaceId);
    assertEquals("WA4", spaceId);

    // test subspace of space
    String[] subSpaceIds = ac.getAllSubSpaceIds("WA1");
    assertEquals(2, subSpaceIds.length);
  }

  @Test
  public void testUpdateComponent() {
    AdminController ac = getAdminController();

    ComponentInst component = ac.getComponentInst("kmelia1");
    String desc = "New description";
    component.setDescription(desc);
    ac.updateComponentInst(component);

    component = ac.getComponentInst("kmelia1");
    assertEquals(desc, component.getDescription());
  }

  @Test
  public void testDeleteComponent() {
    AdminController ac = getAdminController();

    ac.deleteComponentInst("kmelia1", false);

    ComponentInst component = ac.getComponentInst("kmelia1");
    assertEquals("R", component.getStatus());

    ac.deleteComponentInst("kmelia1", true);
    component = ac.getComponentInst("kmelia1");
    assertEquals("", component.getName());
  }

  @Test
  public void testProfileInheritance() {
    AdminController ac = getAdminController();

    // set space profile (admin)
    SpaceProfileInst profile = new SpaceProfileInst();
    profile.setSpaceFatherId("WA1");
    profile.setName("admin");
    profile.addUser(userId);
    String profileId = ac.addSpaceProfileInst(profile, userId);
    assertEquals("4", profileId);

    // test inheritance
    assertEquals(true, ac.isComponentAvailable("almanach2", userId));
    // remove users from space profile
    profile = ac.getSpaceProfileInst(profileId);
    profile.removeAllUsers();
    ac.updateSpaceProfileInst(profile, userId);

    // test inheritance
    assertEquals(false, ac.isComponentAvailable("almanach2", userId));
  }

  @Test
  public void testSpaceManager() throws AdminException {
    AdminController ac = getAdminController();

    // set user1 as space manager
    SpaceProfileInst profile = new SpaceProfileInst();
    profile.setSpaceFatherId("WA2");
    profile.setName("Manager");
    profile.addUser(userId);
    String profileId = ac.addSpaceProfileInst(profile, userId);
    assertEquals("4", profileId);

    // set user2 as simple reader on space
    profile = new SpaceProfileInst();
    profile.setSpaceFatherId("WA2");
    profile.setName("reader");
    profile.addUser("2");
    profileId = ac.addSpaceProfileInst(profile, "1");
    assertEquals("5", profileId);

    // test if user1 is manager of at least one space
    String[] managerIds = admin.getUserManageableSpaceIds("1");
    assertEquals(1, managerIds.length);

    // test if user2 cannot manage spaces
    managerIds = admin.getUserManageableSpaceIds("2");
    assertEquals(0, managerIds.length);
  }

  @Test
  public void testCopyAndPasteComponent() throws AdminException {
    AdminController ac = getAdminController();

    WAComponent waComponent = Instanciateur.getWAComponents().get("almanach");
    waComponent.setInstanceClassName("com.silverpeas.admin.FakeComponentInstanciator");

    String targetSpaceId = "WA3";
    String componentId = ac.copyAndPasteComponent("almanach2", targetSpaceId, userId);

    String expectedComponentId = "almanach3";
    assertEquals(expectedComponentId, componentId);

    ComponentInst component = ac.getComponentInst(expectedComponentId);
    assertNotNull(component);
    assertEquals("Dates clés", component.getLabel());
    assertEquals(expectedComponentId, component.getId());

    SpaceInst space = ac.getSpaceInstById(targetSpaceId);
    List<ComponentInst> components = space.getAllComponentsInst();
    assertEquals(1, components.size());
    component = components.get(0);
    assertEquals(expectedComponentId, component.getId());
  }

  @Test
  public void testCopyAndPasteRootSpace() throws AdminException {
    AdminController ac = getAdminController();

    WAComponent waComponent = Instanciateur.getWAComponents().get("kmelia");
    waComponent.setInstanceClassName("com.silverpeas.admin.FakeComponentInstanciator");

    String[] rootSpaceIds = ac.getAllRootSpaceIds();
    assertEquals(2, rootSpaceIds.length);

    String targetSpaceId = null;
    String newSpaceId = ac.copyAndPasteSpace("WA1", targetSpaceId, userId);

    String expectedSpaceId = "WA4";
    assertEquals(expectedSpaceId, newSpaceId);

    SpaceInstLight spaceLight = ac.getSpaceInstLight(expectedSpaceId);
    assertNotNull(spaceLight);
    assertEquals("Copie de Space 1", spaceLight.getName());
    assertEquals(expectedSpaceId, spaceLight.getFullId());
    assertEquals(rootSpaceIds.length, spaceLight.getOrderNum());

    SpaceInst space = ac.getSpaceInstById(expectedSpaceId);

    // test pasted component
    String expectedComponentId = "kmelia3";
    List<ComponentInst> components = space.getAllComponentsInst();
    assertEquals(1, components.size());
    ComponentInst component = components.get(0);
    assertEquals(expectedComponentId, component.getId());

    // test pasted subspace
    String expectedSubSpaceId = "WA5";
    String[] subSpaceIds = space.getSubSpaceIds();
    assertEquals(1, subSpaceIds.length);
    SpaceInst subSpace = ac.getSpaceInstById(subSpaceIds[0]);
    assertNotNull(subSpace);
    assertEquals(expectedSubSpaceId, subSpace.getId());
    assertEquals("Space 1-2", subSpace.getName());

    rootSpaceIds = ac.getAllRootSpaceIds();
    assertEquals(3, rootSpaceIds.length);
  }

  @Test
  public void testCopyAndPasteSubSpaceInSpace() throws AdminException {
    AdminController ac = getAdminController();

    WAComponent waComponent = Instanciateur.getWAComponents().get("almanach");
    waComponent.setInstanceClassName("com.silverpeas.admin.FakeComponentInstanciator");

    String copiedSpaceId = "WA2";
    String targetSpaceId = "WA3";
    String newSpaceId = ac.copyAndPasteSpace(copiedSpaceId, targetSpaceId, userId);

    String expectedSpaceId = "WA4";
    assertEquals(expectedSpaceId, newSpaceId);

    SpaceInstLight copiedSpace = ac.getSpaceInstLight(copiedSpaceId);

    SpaceInstLight spaceLight = ac.getSpaceInstLight(expectedSpaceId);
    assertNotNull(spaceLight);
    assertEquals(copiedSpace.getName(), spaceLight.getName());
    assertEquals(expectedSpaceId, spaceLight.getFullId());
    assertEquals(0, spaceLight.getOrderNum());

    SpaceInst space = ac.getSpaceInstById(expectedSpaceId);

    // test pasted component
    String expectedComponentId = "almanach3";
    List<ComponentInst> components = space.getAllComponentsInst();
    assertEquals(1, components.size());
    ComponentInst component = components.get(0);
    assertEquals(expectedComponentId, component.getId());

    // test new space is well subspace of target space
    SpaceInst targetSpace = ac.getSpaceInstById(targetSpaceId);
    String[] targetSubSpaceIds = targetSpace.getSubSpaceIds();
    assertEquals(1, targetSubSpaceIds.length);
    assertEquals(expectedSpaceId, targetSubSpaceIds[targetSubSpaceIds.length - 1]);

  }
  
  @Test
  public void testApplicationMove() throws AdminException {
    AdminController admin = getAdminController();
    
    String sourceId = "WA1";
    String destId = "WA3";
    String componentId = "kmelia1";
    SpaceInst dest = admin.getSpaceInstById(destId);
    
    admin.moveComponentInst(destId, componentId, "", dest.getAllComponentsInst().toArray(new ComponentInst[0]));
    
    SpaceInst source = admin.getSpaceInstById(sourceId);
    assertEquals(0, source.getAllComponentsInst().size());
    
    dest = admin.getSpaceInstById(destId);
    assertEquals(1, dest.getAllComponentsInst().size());
    
    ComponentInst component = admin.getComponentInst(componentId);
    ProfileInst profile = component.getInheritedProfileInst("publisher");
    assertEquals(1, profile.getAllUsers().size());
    
    boolean accessAllowed = admin.isComponentAvailable(componentId, "1");
    assertEquals(false, accessAllowed);
    
    assertEquals(true, profile.getAllUsers().contains("2"));
    
    // add rights to space
    SpaceProfileInst newProfile = new SpaceProfileInst();
    newProfile.setName("writer");
    newProfile.setSpaceFatherId(destId);
    newProfile.addUser("1");
    String newProfileId = admin.addSpaceProfileInst(newProfile, userId);
    assertEquals("4", newProfileId);
    
    // check propagation
    component = admin.getComponentInst(componentId);
    assertEquals(3, component.getAllProfilesInst().size());
    
    accessAllowed = admin.isComponentAvailable(componentId, "1");
    assertEquals(true, accessAllowed);
  }

  @Override
  protected String getDatasetFileName() {
    return "test-spacesandcomponents-dataset.xml";
  }

}