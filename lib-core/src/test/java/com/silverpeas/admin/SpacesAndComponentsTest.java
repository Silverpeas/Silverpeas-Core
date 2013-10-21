/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.admin;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.silverpeas.core.admin.OrganisationController;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.silverpeas.quota.exception.QuotaException;

import com.silverpeas.admin.components.Instanciateur;
import com.silverpeas.admin.components.PasteDetail;
import com.silverpeas.admin.components.WAComponent;
import com.silverpeas.jndi.SimpleMemoryContextFactory;

import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.ProfileInst;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.beans.admin.SpaceProfileInst;
import com.stratelia.webactiv.beans.admin.cache.TreeCache;
import com.stratelia.webactiv.util.DBUtil;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertEquals;

public class SpacesAndComponentsTest {

  private static DataSource dataSource;
  private static ClassPathXmlApplicationContext context;
  private static Admin admin;
  private static OrganisationController organizationController;
  String userId = "1";

  @BeforeClass
  public static void setUpClass() throws Exception {
    SimpleMemoryContextFactory.setUpAsInitialContext();
    context = new ClassPathXmlApplicationContext(new String[]{
      "spring-admin-spacecomponents-embbed-datasource.xml", "spring-domains.xml"});
    dataSource = context.getBean("jpaDataSource", DataSource.class);
    organizationController = context.getBean(OrganizationController.class);
    admin = context.getBean(Admin.class);
    InitialContext ic = new InitialContext();
    ic.rebind("jdbc/Silverpeas", dataSource);
    DBUtil.getInstanceForTest(dataSource.getConnection());
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    DBUtil.clearTestInstance();
    SimpleMemoryContextFactory.tearDownAsInitialContext();
    context.close();
  }

  @Before
  public void init() throws Exception {
    IDatabaseConnection connection = getConnection();
    DatabaseOperation.CLEAN_INSERT.execute(connection, getDataSet());
    connection.close();
  }

  @After
  public void after() throws Exception {
    IDatabaseConnection connection = getConnection();
    DatabaseOperation.DELETE_ALL.execute(connection, getDataSet());
    connection.close();
  }

  private IDatabaseConnection getConnection() throws Exception {
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    return connection;
  }

  protected IDataSet getDataSet() throws Exception {
    InputStream in = this.getClass().getClassLoader().getResourceAsStream(
        "com/silverpeas/admin/test-spacesandcomponents-dataset.xml");
    try {
      FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
      ReplacementDataSet dataSet = new ReplacementDataSet(builder.build(in));
      dataSet.addReplacementObject("[NULL]", null);
      return dataSet;
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  private AdminController getAdminController() {
    AdminController ac = new AdminController(userId);
    return ac;
  }

  @Before
  public void clearCache() {
    assertThat(admin, is(notNullValue()));
    admin.reloadCache();
  }

  @Test
  public void testAddSpace() {
    AdminController ac = getAdminController();

    String expectedRootSpaceId = "WA7";
    String expectedSubSpaceId = "WA8";

    // test space creation
    SpaceInst space = new SpaceInst();
    space.setCreatorUserId(userId);
    space.setName("Space 3");
    String spaceId = ac.addSpaceInst(space);
    assertThat(spaceId, is(notNullValue()));
    assertThat(spaceId, is(expectedRootSpaceId));

    // test subspace creation
    SpaceInst subspace = new SpaceInst();
    subspace.setCreatorUserId(userId);
    subspace.setName("Space 3 - 1");
    subspace.setDomainFatherId(spaceId);
    String subSpaceId = ac.addSpaceInst(subspace);
    assertThat(subSpaceId, is(notNullValue()));
    assertThat(subSpaceId, is(expectedSubSpaceId));

    // test subspace of root space
    String[] subSpaceIds = ac.getAllSubSpaceIds(spaceId);
    assertThat(subSpaceIds, arrayWithSize(1));

    // test level calculation
    subspace = ac.getSpaceInstById(subSpaceId);
    assertThat(subspace.getLevel(), is(1));

    SpaceInstLight subspaceLight = ac.getSpaceInstLight(subSpaceId);
    assertThat(subspaceLight.getLevel(), is(1));
  }

  @Test
  public void testUpdateSpace() {
    AdminController ac = getAdminController();
    String spaceId = "WA1";
    SpaceInst space = ac.getSpaceInstById(spaceId);
    assertThat(space.getDescription(), is(nullValue()));
    SpaceInstLight spaceLight = ac.getSpaceInstLight(spaceId);
    assertThat(spaceLight.getDescription(), is(nullValue()));
    String desc = "New description";
    space.setDescription(desc);
    ac.updateSpaceInst(space);

    space = ac.getSpaceInstById(spaceId);
    assertThat(space.getDescription(), is(desc));

    spaceLight = ac.getSpaceInstLight(spaceId);
    assertThat(spaceLight.getDescription(), is(desc));
  }

  @Test
  public void testDeleteSpace() throws AdminException {
    admin.deleteSpaceInstById(userId, "WA1", false);

    SpaceInst space = organizationController.getSpaceInstById("WA1");
    assertThat(space.getStatus(), is("R"));

    admin.deleteSpaceInstById(userId, "WA1", true);
    space = organizationController.getSpaceInstById("WA1");
    assertThat(space, is(nullValue()));
  }

  @Test
  public void testDeleteAndRestoreSpace() throws AdminException {
    admin.deleteSpaceInstById(userId, "WA1", false);

    SpaceInst space = organizationController.getSpaceInstById("WA1");
    assertThat(space.getStatus(), is("R"));
    assertThat(TreeCache.getSpaceInstLight("1"), is(nullValue()));
    assertThat(TreeCache.getSpaceInstLight("2"), is(nullValue()));

    admin.restoreSpaceFromBasket("WA1");
    space = organizationController.getSpaceInstById("WA1");
    assertThat(space.getStatus(), is(nullValue()));

    assertThat(TreeCache.getSpaceInstLight("1"), is(notNullValue()));
    assertThat(TreeCache.getSubSpaces("1"), hasSize(1));
    assertThat(TreeCache.getComponentsInSpaceAndSubspaces("1"), hasSize(3));
    assertThat(TreeCache.getComponent("kmelia1"), is(notNullValue()));
    assertThat(TreeCache.getComponent("almanach2"), is(notNullValue()));
  }

  public void testAddSubSpace() {
    AdminController ac = getAdminController();

    // test space creation
    SpaceInst space = new SpaceInst();
    space.setCreatorUserId(userId);
    space.setName("Space 1-3");
    space.setDomainFatherId("WA1");
    String spaceId = ac.addSpaceInst(space);
    assertThat(spaceId, is(notNullValue()));
    assertThat(spaceId, is("WA4"));

    // test subspace of space
    String[] subSpaceIds = ac.getAllSubSpaceIds("WA1");
    assertThat(subSpaceIds.length, is(2));
  }
  
  @Test
  public void testGetSubSpaces() throws AdminException {
    AdminController ac = getAdminController();
    String[] spaceIds = ac.getAllSubSpaceIds("WA1");
    assertEquals(1, spaceIds.length);
    
    SpaceInstLight subSpace = ac.getSpaceInstLight(spaceIds[0]);
    assertEquals("Space 1-2", subSpace.getName());
    assertEquals("Space 1-2 in english", subSpace.getName("en"));
    
    List<SpaceInstLight> subSpaces = admin.getSubSpaces("WA1");
    assertEquals(1, subSpaces.size());
    
    subSpace = subSpaces.get(0);
    assertEquals("Space 1-2", subSpace.getName());
    assertEquals("Space 1-2 in english", subSpace.getName("en"));
  }

  @Test
  public void testUpdateComponent() {
    AdminController ac = getAdminController();
    ComponentInst component = ac.getComponentInst("kmelia1");
    String desc = "New description";
    component.setDescription(desc);
    ac.updateComponentInst(component);

    component = ac.getComponentInst("kmelia1");
    assertThat(component.getDescription(), is(desc));
  }

  @Test
  public void testDeleteComponent() {
    AdminController ac = getAdminController();
    ac.deleteComponentInst("kmelia1", false);
    ComponentInst component = ac.getComponentInst("kmelia1");
    assertThat(component.getStatus(), is("R"));

    ac.deleteComponentInst("kmelia1", true);
    component = ac.getComponentInst("kmelia1");
    assertThat(component.getName(), is(""));
  }

  @Test
  public void testDeleteAndRestoreComponent() {
    AdminController ac = getAdminController();
    ac.deleteComponentInst("kmelia1", false);
    ComponentInst component = ac.getComponentInst("kmelia1");
    assertThat(component.getStatus(), is("R"));
    assertThat(TreeCache.getComponent("kmelia1"), is(nullValue()));
    ac.restoreComponentFromBasket("kmelia1");
    assertThat(TreeCache.getComponent("kmelia1"), is(notNullValue()));
  }

  @Test
  public void testProfileInheritance() {
    AdminController ac = getAdminController();
    String spaceId = "WA1";
    String componentId = "almanach2";
    // test inheritance
    assertThat(ac.isComponentAvailable(componentId, userId), is(true));

    // set space profile (admin)
    SpaceProfileInst profile = new SpaceProfileInst();
    profile.setSpaceFatherId(spaceId);
    profile.setName("admin");
    profile.addUser(userId);
    String profileId = ac.addSpaceProfileInst(profile, userId);
    assertThat(profileId, is("7"));

    // test inheritance
    ComponentInst component = ac.getComponentInst(componentId);
    ProfileInst p = component.getInheritedProfileInst("admin");
    assertThat(p.getAllUsers(), hasSize(1));
    assertThat(ac.isComponentAvailable(componentId, userId), is(true));
    
    // test non inheritance
    String anotherComponentId = "kmelia4";
    component = ac.getComponentInst(anotherComponentId);
    assertThat(component.getAllProfilesInst().size(), is(0));

    // remove users from space profile
    profile = ac.getSpaceProfileInst(profileId);
    profile.removeAllUsers();
    ac.updateSpaceProfileInst(profile, userId);

    // test inheritance
    component = ac.getComponentInst(componentId);
    p = component.getInheritedProfileInst("admin");
    assertThat(p.isEmpty(), is(true));

    // component is always available due to inheritance policy
    assertThat(ac.isComponentAvailable(componentId, userId), is(true));
    
    // test non inheritance
    component = ac.getComponentInst(anotherComponentId);
    assertThat(component.getAllProfilesInst().size(), is(0));
    assertThat(p.isEmpty(), is(true));
    
    // another component is always unavailable
    assertThat(ac.isComponentAvailable(anotherComponentId, userId), is(false));
  }

  @Test
  public void testProfileInheritanceBetweenSpaces() {
    AdminController ac = getAdminController();
    String rootSpaceId = "WA4";
    String subSpaceIdWithoutInheritance = "WA5";
    String subSpaceIdWithInheritance = "WA6";

    // force space to block inheritance
    SpaceInst spaceWithoutInheritance = ac.getSpaceInstById(subSpaceIdWithoutInheritance);
    spaceWithoutInheritance.setInheritanceBlocked(true);
    ac.updateSpaceInst(spaceWithoutInheritance);

    spaceWithoutInheritance = ac.getSpaceInstById(subSpaceIdWithoutInheritance);
    assertThat(spaceWithoutInheritance.isInheritanceBlocked(), is(true));
    SpaceInst spaceWithInheritance = ac.getSpaceInstById(subSpaceIdWithInheritance);
    assertThat(spaceWithInheritance.isInheritanceBlocked(), is(false));

    // set space profile (admin)
    SpaceProfileInst profile = new SpaceProfileInst();
    profile.setSpaceFatherId(rootSpaceId);
    profile.setName("admin");
    profile.addUser(userId);

    String profileId = ac.addSpaceProfileInst(profile, rootSpaceId);
    assertThat(profileId, is("7"));

    SpaceInst root = ac.getSpaceInstById(rootSpaceId);
    List<SpaceProfileInst> profiles = root.getProfiles();
    assertThat(profiles.size(), is(1));

    spaceWithoutInheritance = ac.getSpaceInstById(subSpaceIdWithoutInheritance);
    profiles = spaceWithoutInheritance.getAllSpaceProfilesInst();
    assertThat(profiles.size(), is(0));

    spaceWithInheritance = ac.getSpaceInstById(subSpaceIdWithInheritance);
    profiles = spaceWithInheritance.getAllSpaceProfilesInst();
    assertThat(profiles.size(), is(1));
  }

  @Test
  public void testProfileInheritanceBetweenNewSpaces() {
    AdminController ac = getAdminController();

    // creating new root space
    SpaceInst newRootSpace = new SpaceInst();
    newRootSpace.setName("Root space");
    newRootSpace.setCreatorUserId(userId);
    String newRootSpaceId = ac.addSpaceInst(newRootSpace);
    assertThat(newRootSpaceId, is("WA7"));
    newRootSpace = ac.getSpaceInstById(newRootSpaceId);
    // creating a first child
    SpaceInst sub1 = new SpaceInst();
    sub1.setName("Sub 1");
    sub1.setCreatorUserId(userId);
    sub1.setDomainFatherId(newRootSpaceId);
    String sub1Id = ac.addSpaceInst(sub1);
    assertThat(sub1Id, is("WA8"));
    sub1 = ac.getSpaceInstById(sub1Id);
    ac.updateSpaceOrderNum(sub1Id, 0);
    sub1 = ac.getSpaceInstById(sub1Id);
    // by default, inheritance is active, checking it
    assertThat(sub1.isInheritanceBlocked(), is(false));

    // blocking inheritance
    sub1.setInheritanceBlocked(true);
    sub1.setUpdaterUserId(userId);
    ac.updateSpaceInst(sub1);

    List<SpaceInstLight> subspaces = TreeCache.getSubSpaces(newRootSpaceId.substring(2));
    assertThat(subspaces.size(), is(1));
    SpaceInstLight subspace = subspaces.get(0);
    assertThat(subspace.isInheritanceBlocked(), is(true));

    // creating a second child
    SpaceInst sub2 = new SpaceInst();
    sub2.setName("Sub 2");
    sub2.setCreatorUserId(userId);
    sub2.setDomainFatherId(newRootSpaceId);
    String sub2Id = ac.addSpaceInst(sub2);
    assertThat(sub2Id, is("WA9"));

    sub2 = ac.getSpaceInstById(sub2Id);

    ac.updateSpaceOrderNum(sub2Id, 1);

    // adding a profile on root level
    SpaceProfileInst profile = new SpaceProfileInst();
    profile.setSpaceFatherId(newRootSpaceId);
    profile.setName("admin");
    profile.addUser(userId);

    String profileId = ac.addSpaceProfileInst(profile, userId);
    assertThat(profileId, is("7"));

    profile = ac.getSpaceProfileInst(profileId);
    profile.addUser("2");
    ac.updateSpaceProfileInst(profile, userId);

    // checking first child have no active profiles
    sub1 = ac.getSpaceInstById(sub1Id);
    assertThat(sub1.getAllSpaceProfilesInst().size(), is(0));

    // checking second child have one active profile
    sub2 = ac.getSpaceInstById(sub2Id);
    assertThat(sub2.getAllSpaceProfilesInst().size(), is(1));

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
    assertThat(profileId, is("7"));

    // set user2 as simple reader on space
    profile = new SpaceProfileInst();
    profile.setSpaceFatherId("WA2");
    profile.setName(SilverpeasRole.reader.name());
    profile.addUser("2");
    profileId = ac.addSpaceProfileInst(profile, "1");
    assertThat(profileId, is("8"));

    // test if user1 is manager of at least one space
    String[] managerIds = admin.getUserManageableSpaceIds("1");
    assertThat(managerIds.length, is(1));

    // test if user2 cannot manage spaces
    managerIds = admin.getUserManageableSpaceIds("2");
    assertThat(managerIds.length, is(0));
  }

  @Test
  public void testCopyAndPasteComponent() throws AdminException, QuotaException {
    AdminController ac = getAdminController();

    WAComponent waComponent = Instanciateur.getWAComponents().get("almanach");
    waComponent.setInstanceClassName("com.silverpeas.admin.FakeComponentInstanciator");

    String targetSpaceId = "WA3";
    PasteDetail pasteDetail = new PasteDetail("almanach2", userId);
    pasteDetail.setToSpaceId(targetSpaceId);
    String componentId = ac.copyAndPasteComponent(pasteDetail);

    String expectedComponentId = "almanach5";
    assertThat(componentId, is(expectedComponentId));

    ComponentInst component = ac.getComponentInst(expectedComponentId);
    assertThat(component, is(notNullValue()));
    assertThat(component.getLabel(), is("Dates clés"));
    assertThat(component.getId(), is(expectedComponentId));

    SpaceInst space = ac.getSpaceInstById(targetSpaceId);
    List<ComponentInst> components = space.getAllComponentsInst();
    assertThat(components, hasSize(2));
    component = components.get(1);
    assertThat(component.getId(), is(expectedComponentId));
  }

  @Test
  public void testCopyAndPasteRootSpace() throws AdminException, QuotaException {
    AdminController ac = getAdminController();

    WAComponent waComponent = Instanciateur.getWAComponents().get("kmelia");
    waComponent.setInstanceClassName("com.silverpeas.admin.FakeComponentInstanciator");

    String[] rootSpaceIds = ac.getAllRootSpaceIds();
    assertThat(rootSpaceIds.length, is(4));
    String targetSpaceId = null;
    PasteDetail pasteDetail = new PasteDetail(userId);
    pasteDetail.setFromSpaceId("WA1");
    pasteDetail.setToSpaceId(targetSpaceId);
    String newSpaceId = ac.copyAndPasteSpace(pasteDetail);
    String expectedSpaceId = "WA7";
    assertThat(newSpaceId, is(expectedSpaceId));

    SpaceInstLight spaceLight = ac.getSpaceInstLight(expectedSpaceId);
    assertThat(spaceLight, is(notNullValue()));
    assertThat(spaceLight.getName(), is("Copie de Space 1"));
    assertThat(spaceLight.getFullId(), is(expectedSpaceId));
    assertThat(spaceLight.getOrderNum(), is(rootSpaceIds.length));

    SpaceInst space = ac.getSpaceInstById(expectedSpaceId);

    // test pasted component
    String expectedComponentId = "kmelia5";
    List<ComponentInst> components = space.getAllComponentsInst();
    assertThat(components.size(), is(2));
    ComponentInst component = components.get(0);
    assertThat(component.getId(), is(expectedComponentId));

    // test pasted subspace
    String expectedSubSpaceId = "WA8";
    String[] subSpaceIds = space.getSubSpaceIds();
    assertThat(subSpaceIds.length, is(1));
    SpaceInst subSpace = ac.getSpaceInstById(subSpaceIds[0]);
    assertThat(subSpace, is(notNullValue()));
    assertThat(subSpace.getId(), is(expectedSubSpaceId));
    assertThat(subSpace.getName(), is("Space 1-2"));

    rootSpaceIds = ac.getAllRootSpaceIds();
    assertThat(rootSpaceIds.length, is(5));
  }

  @Test
  public void testCopyAndPasteSubSpaceInSpace() throws AdminException, QuotaException {
    AdminController ac = getAdminController();
    WAComponent waComponent = Instanciateur.getWAComponents().get("almanach");
    waComponent.setInstanceClassName("com.silverpeas.admin.FakeComponentInstanciator");

    String copiedSpaceId = "WA2";
    String targetSpaceId = "WA3";
    PasteDetail pasteDetail = new PasteDetail(userId);
    pasteDetail.setFromSpaceId(copiedSpaceId);
    pasteDetail.setToSpaceId(targetSpaceId);
    String newSpaceId = ac.copyAndPasteSpace(pasteDetail);

    String expectedSpaceId = "WA7";
    assertThat(newSpaceId, is(expectedSpaceId));

    SpaceInstLight copiedSpace = ac.getSpaceInstLight(copiedSpaceId);
    SpaceInstLight spaceLight = ac.getSpaceInstLight(expectedSpaceId);
    assertThat(spaceLight, is(notNullValue()));
    assertThat(spaceLight.getName(), is(copiedSpace.getName()));
    assertThat(spaceLight.getFullId(), is(expectedSpaceId));
    assertThat(spaceLight.getOrderNum(), is(0));
    
    SpaceInst space = ac.getSpaceInstById(expectedSpaceId);

    // test pasted component
    String expectedComponentId = "almanach5";
    List<ComponentInst> components = space.getAllComponentsInst();
    assertThat(components.size(), is(1));
    ComponentInst component = components.get(0);
    assertThat(component.getId(), is(expectedComponentId));

    // test new space is well subspace of target space
    SpaceInst targetSpace = ac.getSpaceInstById(targetSpaceId);
    String[] targetSubSpaceIds = targetSpace.getSubSpaceIds();
    assertThat(targetSubSpaceIds.length, is(1));
    assertThat(targetSubSpaceIds[targetSubSpaceIds.length - 1], is(expectedSpaceId));
  }

  @Test
  public void testCopyAndPasteSpaceInItsSubSpace() throws AdminException, QuotaException {
    AdminController ac = getAdminController();
    String copiedSpaceId = "WA1";
    String targetSpaceId = "WA2";
    PasteDetail pasteDetail = new PasteDetail(userId);
    pasteDetail.setFromSpaceId(copiedSpaceId);
    pasteDetail.setToSpaceId(targetSpaceId);
    String newSpaceId = ac.copyAndPasteSpace(pasteDetail);
    assertThat(newSpaceId, is(nullValue()));
  }

  @Test
  public void testMoveSubSpaceAsRootSpace() throws AdminException {
    AdminController ac = getAdminController();

    String spaceId = "WA2";
    SpaceInstLight space = ac.getSpaceInstLight(spaceId);
    String originalFatherId = space.getFatherId();

    // check profiles before moving
    SpaceInst fullSpace = ac.getSpaceInstById(spaceId);
    assertThat(fullSpace.getAllSpaceProfilesInst().size(), is(3));
    assertThat(fullSpace.getInheritedProfiles().size(), is(2));

    ac.moveSpace(spaceId, null);

    // check if space is well on top level
    List<String> rootSpaceIds = Arrays.asList(ac.getAllRootSpaceIds());
    assertThat(rootSpaceIds.contains(spaceId), is(true));
    assertThat(rootSpaceIds.size(),is(5));

    //check if space is no more in original parent
    List<String> subSpaceIds = Arrays.asList(ac.getAllSubSpaceIds(originalFatherId));
    assertThat(subSpaceIds.contains(spaceId), is(false));
    assertThat(subSpaceIds.isEmpty(), is(true));

    // check if space have no parent
    space = ac.getSpaceInstLight(spaceId);
    assertThat(space.getFatherId(), is("0"));
    assertThat(space.getLevel(), is(0));

    //check profiles after moving
    fullSpace = ac.getSpaceInstById(spaceId);
    assertThat(fullSpace.getAllSpaceProfilesInst().size(), is(1));
    assertThat(fullSpace.getInheritedProfiles().size(), is(0));

    // check almanach rights
    String componentId = "almanach2";
    assertThat(ac.isComponentAvailable(componentId, "1"), is(false));
    assertThat(ac.isComponentAvailable(componentId, "2"), is(true));
    assertThat(ac.isComponentAvailable(componentId, "3"), is(true));
  }

  @Test
  public void testMoveSpaceInItsSubspace() throws AdminException {
    AdminController ac = getAdminController();
    String spaceId = "WA1";
    String subspaceId = "WA2";
    ac.moveSpace(spaceId, subspaceId);
    SpaceInstLight space = ac.getSpaceInstLight(spaceId);
    // check if space is unchanged
    assertThat(space.getFatherId(), is("0"));
    assertThat(space.getLevel(), is(0));
  }

  @Test
  public void testMoveRootSpaceInASubspace() throws AdminException {
    AdminController ac = getAdminController();
    String spaceId = "WA1";
    String targetSpaceId = "WA3";
    // check profiles before moving
    SpaceInst fullSpace = ac.getSpaceInstById(spaceId);
    assertThat(fullSpace.getAllSpaceProfilesInst().size(), is(2));
    assertThat(fullSpace.getInheritedProfiles().size(), is(0));
    List<String> rootSpaceIds = Arrays.asList(ac.getAllRootSpaceIds());
    assertThat(rootSpaceIds, hasItem(spaceId));
    assertThat(rootSpaceIds, hasSize(4));
    ac.moveSpace(spaceId, targetSpaceId);
    // check if space is no more on top level
    rootSpaceIds = Arrays.asList(ac.getAllRootSpaceIds());
    assertThat(rootSpaceIds, not(hasItem(spaceId)));
    assertThat(rootSpaceIds, hasSize(3));
    //check if space is in new parent
    List<String> subSpaceIds = Arrays.asList(ac.getAllSubSpaceIds(targetSpaceId));
    assertThat(subSpaceIds, hasItem(spaceId));
    // check if space have got new parent
    SpaceInstLight space = ac.getSpaceInstLight(spaceId);
    assertThat(space.getFatherId(), is("3"));
    assertThat(space.getLevel(), is(1));
    //check profiles after moving
    fullSpace = ac.getSpaceInstById(spaceId);
    assertThat(fullSpace.getAllSpaceProfilesInst(), hasSize(3));
    assertThat(fullSpace.getProfiles(), hasSize(2));
    assertThat(fullSpace.getInheritedProfiles(), hasSize(1));
    assertThat(fullSpace.getSpaceProfileInst(SilverpeasRole.publisher.name()).getNumUser(), is(1));
    assertThat(fullSpace.getSpaceProfileInst(SilverpeasRole.reader.name()).getNumUser(), is(1));
    assertThat(fullSpace.getInheritedSpaceProfileInst(SilverpeasRole.publisher.name()).getNumUser(),
        is(1));

    // check GED rights
    String componentId = "kmelia1";
    ComponentInst component = ac.getComponentInst(componentId);
    assertThat(component.getAllProfilesInst(), hasSize(2));
    assertThat(component.getInheritedProfileInst(SilverpeasRole.publisher.name()).getNumUser(),
        is(2));
    assertThat(ac.isComponentAvailable(componentId, "2"), is(true));
    assertThat(ac.isComponentAvailable(componentId, "1"), is(true));
    String[] roles = organizationController.getUserProfiles("1", componentId);
    assertThat(roles, arrayWithSize(2));
    roles = organizationController.getUserProfiles("2", componentId);
    assertThat(roles, arrayWithSize(1));
  }

  @Test
  public void testMoveSpaceWithoutRole() throws AdminException {
    AdminController ac = getAdminController();
    String parentSpaceId = "WA100";
    String spaceId = "WA110";
    String tempParentSpaceId = "WA3";
    // check profiles before moving
    SpaceInst fullSpace = ac.getSpaceInstById(spaceId);
    assertThat(fullSpace.getAllSpaceProfilesInst(), hasSize(0));
    assertThat(fullSpace.getAllSpaceProfilesInst(), hasSize(0));
    // check if space on top level
    List<String> rootSpaceIds = Arrays.asList(ac.getAllRootSpaceIds());
    assertThat(rootSpaceIds, not(hasItem(spaceId)));
    assertThat(rootSpaceIds, hasSize(4));
    // check GED rights
    String componentId = "kmelia210";
    ComponentInst component = ac.getComponentInst(componentId);
    assertThat(component.getAllProfilesInst(), hasSize(0));
    assertThat(component.getInheritedProfileInst(SilverpeasRole.publisher.name()), is(nullValue()));
    assertThat(ac.isComponentAvailable(componentId, "2"), is(false));
    assertThat(ac.isComponentAvailable(componentId, "1"), is(false));
    String[] roles = organizationController.getUserProfiles("1", componentId);
    assertThat(roles, arrayWithSize(0));
    roles = organizationController.getUserProfiles("2", componentId);
    assertThat(roles, arrayWithSize(0));
    
    ac.moveSpace(spaceId, tempParentSpaceId);

    // check if space is no more on top level
    rootSpaceIds = Arrays.asList(ac.getAllRootSpaceIds());
    assertThat(rootSpaceIds, not(hasItem(spaceId)));
    assertThat(rootSpaceIds, hasSize(4));
    //check if space is in new parent
    List<String> subSpaceIds = Arrays.asList(ac.getAllSubSpaceIds(tempParentSpaceId));
    assertThat(subSpaceIds, hasItem(spaceId));
    // check if space have got new parent
    SpaceInstLight space = ac.getSpaceInstLight(spaceId);
    assertThat(space.getFatherId(), is("3"));
    assertThat(space.getLevel(), is(1));
    //check profiles after moving
    fullSpace = ac.getSpaceInstById(spaceId);
    assertThat(fullSpace.getAllSpaceProfilesInst(), hasSize(1));
    assertThat(fullSpace.getProfiles(), hasSize(0));
    assertThat(fullSpace.getInheritedProfiles(), hasSize(1));
    assertThat(fullSpace.getSpaceProfileInst(SilverpeasRole.publisher.name()), is(nullValue()));
    assertThat(fullSpace.getSpaceProfileInst(SilverpeasRole.reader.name()), is(nullValue()));
    assertThat(fullSpace.getInheritedSpaceProfileInst(SilverpeasRole.publisher.name()).getNumUser(),
        is(1));
    // check GED rights
    component = ac.getComponentInst(componentId);
    assertThat(component.getAllProfilesInst(), hasSize(1));
    assertThat(component.getInheritedProfileInst(SilverpeasRole.publisher.name()).getNumUser(),
        is(1));
    assertThat(ac.isComponentAvailable(componentId, "2"), is(true));
    assertThat(ac.isComponentAvailable(componentId, "1"), is(false));
    roles = organizationController.getUserProfiles("1", componentId);
    assertThat(roles, arrayWithSize(0));
    roles = organizationController.getUserProfiles("2", componentId);
    assertThat(roles, arrayWithSize(1));

    //Moving back
    ac.moveSpace(spaceId, parentSpaceId);

    space = ac.getSpaceInstLight(spaceId);
    assertThat(space.getFatherId(), is("100"));
    assertThat(space.getLevel(), is(1));

    //check profiles after moving
    fullSpace = ac.getSpaceInstById(spaceId);
    assertThat(fullSpace.getAllSpaceProfilesInst(), hasSize(0));
    assertThat(fullSpace.getProfiles(), hasSize(0));
    assertThat(fullSpace.getInheritedProfiles(), hasSize(0));
    assertThat(fullSpace.getSpaceProfileInst(SilverpeasRole.publisher.name()), is(nullValue()));
    assertThat(fullSpace.getSpaceProfileInst(SilverpeasRole.reader.name()), is(nullValue()));
    assertThat(fullSpace.getInheritedSpaceProfileInst(SilverpeasRole.publisher.name()),
        is(nullValue()));

    // check GED rights
    component = ac.getComponentInst(componentId);
    assertThat(component.getAllProfilesInst(), hasSize(1));
    assertThat(component.getInheritedProfileInst(SilverpeasRole.publisher.name()).getNumUser(),
        is(0));
    assertThat(ac.isComponentAvailable(componentId, "2"), is(false));
    assertThat(ac.isComponentAvailable(componentId, "1"), is(false));
    roles = organizationController.getUserProfiles("1", componentId);
    assertThat(roles, arrayWithSize(0));

    roles = organizationController.getUserProfiles("2", componentId);
    assertThat(roles, arrayWithSize(0));
  }

  @Test
  public void testApplicationMove() throws AdminException {
    AdminController admin = getAdminController();
    String sourceId = "WA1";
    String destId = "WA3";
    String componentId = "kmelia1";
    SpaceInst dest = admin.getSpaceInstById(destId);
    List<ComponentInst> components = dest.getAllComponentsInst();
    admin.moveComponentInst(destId, componentId, "",components.toArray(new ComponentInst[components.size()]));
    SpaceInst source = admin.getSpaceInstById(sourceId);
    assertThat(source.getAllComponentsInst().size(), is(1));
    dest = admin.getSpaceInstById(destId);
    assertThat(dest.getAllComponentsInst().size(), is(2));

    ComponentInst component = admin.getComponentInst(componentId);
    ProfileInst profile = component.getInheritedProfileInst(SilverpeasRole.publisher.name());
    assertThat(profile.getAllUsers().size(), is(1));

    boolean accessAllowed = admin.isComponentAvailable(componentId, "1");
    assertThat(accessAllowed, is(false));
    assertThat(profile.getAllUsers().contains("2"), is(true));

    // add rights to space
    SpaceProfileInst newProfile = new SpaceProfileInst();
    newProfile.setName("writer");
    newProfile.setSpaceFatherId(destId);
    newProfile.addUser("1");
    String newProfileId = admin.addSpaceProfileInst(newProfile, userId);
    assertThat(newProfileId, is("7"));

    // check propagation
    component = admin.getComponentInst(componentId);
    assertThat(component.getAllProfilesInst().size(), is(3));
    accessAllowed = admin.isComponentAvailable(componentId, "1");
    assertThat(accessAllowed, is(true));
  }
}