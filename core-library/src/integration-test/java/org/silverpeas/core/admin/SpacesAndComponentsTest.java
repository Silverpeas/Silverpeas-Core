/*
 * Copyright (C) 2000 - 2016 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of
 * the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License along with this
 * program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin;

import org.silverpeas.core.admin.component.model.PasteDetail;
import org.silverpeas.core.admin.component.WAComponentRegistry;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.space.SpaceProfileInst;
import org.silverpeas.core.admin.service.cache.TreeCache;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.space.SpaceServiceProvider;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.quota.exception.QuotaException;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.test.rule.MavenTargetDirectoryRule;
import org.silverpeas.core.admin.component.ComponentHelper;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileFolderManager;
import org.silverpeas.core.util.lang.SystemWrapper;
import org.silverpeas.core.util.memory.MemoryData;
import org.silverpeas.core.util.memory.MemoryUnit;

import javax.inject.Inject;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class SpacesAndComponentsTest {

  @Inject
  private WAComponentRegistry registry;
  @Inject
  private AdminController adminController;
  @Inject
  private Administration admin;
  @Inject
  private OrganizationController organizationController;
  private String userId = "1";

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom("create_space_components_database.sql")
          .loadInitialDataSetFrom("test-spaces_and_components-dataset.sql");

  @Rule
  public MavenTargetDirectoryRule mavenTargetDirectoryRule = new MavenTargetDirectoryRule(this);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(SpacesAndComponentsTest.class)
        .addSilverpeasExceptionBases()
        .addAdministrationFeatures()
        .addSynchAndAsynchResourceEventFeatures()
        .addIndexEngineFeatures()
        .addSilverpeasUrlFeatures()
        .addAsResource("org/silverpeas/jobStartPagePeas/settings")
        .addMavenDependencies("org.apache.lucene:lucene-core")
        .addMavenDependencies("org.apache.lucene:lucene-analyzers")
        .addPackages(false, "org.silverpeas.core.admin.space.quota")
        .addPackages(false, "org.silverpeas.core.contribution.contentcontainer.container")
        .addPackages(false, "org.silverpeas.core.contribution.contentcontainer.content")
        .addClasses(FileRepositoryManager.class, FileFolderManager.class, MemoryUnit.class,
            MemoryData.class, SpaceServiceProvider.class, ComponentHelper.class,
            AttachmentServiceProvider.class)
        .build();
  }

  @Before
  public void reloadCache() throws Exception {
    File silverpeasHome = mavenTargetDirectoryRule.getResourceTestDirFile();
    SystemWrapper.get().getenv().put("SILVERPEAS_HOME", silverpeasHome.getPath());
    adminController.reloadAdminCache();
    registry.init();
  }

  @Test
  public void testAddSpace() {
    String expectedRootSpaceId = "WA200";
    String expectedSubSpaceId = "WA201";

    // test space creation
    SpaceInst space = new SpaceInst();
    space.setCreatorUserId(userId);
    space.setName("Space 3");
    String spaceId = adminController.addSpaceInst(space);
    assertThat(spaceId, is(notNullValue()));
    assertThat(spaceId, is(expectedRootSpaceId));

    // test subspace creation
    SpaceInst subspace = new SpaceInst();
    subspace.setCreatorUserId(userId);
    subspace.setName("Space 3 - 1");
    subspace.setDomainFatherId(spaceId);
    String subSpaceId = adminController.addSpaceInst(subspace);
    assertThat(subSpaceId, is(notNullValue()));
    assertThat(subSpaceId, is(expectedSubSpaceId));

    // test subspace of root space
    String[] subSpaceIds = adminController.getAllSubSpaceIds(spaceId);
    assertThat(subSpaceIds, arrayWithSize(1));

    // test level calculation
    subspace = adminController.getSpaceInstById(subSpaceId);
    assertThat(subspace.getLevel(), is(1));

    SpaceInstLight subspaceLight = adminController.getSpaceInstLight(subSpaceId);
    assertThat(subspaceLight.getLevel(), is(1));
  }

  @Test
  public void testUpdateSpace() {
    String spaceId = "WA1";
    SpaceInst space = adminController.getSpaceInstById(spaceId);
    assertThat(space.getDescription(), is(nullValue()));
    SpaceInstLight spaceLight = adminController.getSpaceInstLight(spaceId);
    assertThat(spaceLight.getDescription(), is(nullValue()));
    String desc = "New description";
    space.setDescription(desc);
    adminController.updateSpaceInst(space);

    space = adminController.getSpaceInstById(spaceId);
    assertThat(space.getDescription(), is(desc));

    spaceLight = adminController.getSpaceInstLight(spaceId);
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
    assertThat(TreeCache.getSpaceInstLight(1), is(nullValue()));
    assertThat(TreeCache.getSpaceInstLight(2), is(nullValue()));

    admin.restoreSpaceFromBasket("WA1");
    space = organizationController.getSpaceInstById("WA1");
    assertThat(space.getStatus(), is(nullValue()));

    assertThat(TreeCache.getSpaceInstLight(1), is(notNullValue()));
    assertThat(TreeCache.getSubSpaces(1), hasSize(1));
    assertThat(TreeCache.getComponentsInSpaceAndSubspaces(1), hasSize(3));
    assertThat(TreeCache.getComponent("kmelia1"), is(notNullValue()));
    assertThat(TreeCache.getComponent("almanach2"), is(notNullValue()));
  }

  @Test
  public void testAddSubSpace() {
    // test space creation
    SpaceInst space = new SpaceInst();
    space.setCreatorUserId(userId);
    space.setName("Space 1-3");
    space.setDomainFatherId("WA1");
    String spaceId = adminController.addSpaceInst(space);
    assertThat(spaceId, is(notNullValue()));
    assertThat(spaceId, is("WA200"));

    // test subspace of space
    String[] subSpaceIds = adminController.getAllSubSpaceIds("WA1");
    assertThat(subSpaceIds.length, is(2));
  }

  @Test
  public void testGetSubSpaces() throws AdminException {
    String[] spaceIds = adminController.getAllSubSpaceIds("WA1");
    assertEquals(1, spaceIds.length);

    SpaceInstLight subSpace = adminController.getSpaceInstLight(spaceIds[0]);
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
    ComponentInst component = adminController.getComponentInst("kmelia1");
    String desc = "New description";
    component.setDescription(desc);
    adminController.updateComponentInst(component);

    component = adminController.getComponentInst("kmelia1");
    assertThat(component.getDescription(), is(desc));
  }

  @Test
  public void testDeleteComponent() throws AdminException {
    admin.deleteComponentInst(userId, "kmelia1", false);
    ComponentInst component = adminController.getComponentInst("kmelia1");
    assertThat(component.getStatus(), is("R"));

    admin.deleteComponentInst(userId, "kmelia1", true);
    component = adminController.getComponentInst("kmelia1");
    assertThat(component.getName(), is(""));
  }

  @Test
  public void testDeleteAndRestoreComponent() throws AdminException {
    admin.deleteComponentInst(userId, "kmelia1", false);
    ComponentInst component = adminController.getComponentInst("kmelia1");
    assertThat(component.getStatus(), is("R"));
    assertThat(TreeCache.getComponent("kmelia1"), is(nullValue()));
    adminController.restoreComponentFromBasket("kmelia1");
    assertThat(TreeCache.getComponent("kmelia1"), is(notNullValue()));
  }

  @Test
  public void testProfileInheritance() {
    String spaceId = "WA1";
    String componentId = "almanach2";
    // test inheritance
    assertThat(adminController.isComponentAvailable(componentId, userId), is(true));

    // set space profile (admin)
    SpaceProfileInst profile = new SpaceProfileInst();
    profile.setSpaceFatherId(spaceId);
    profile.setName("admin");
    profile.addUser(userId);
    String profileId = adminController.addSpaceProfileInst(profile, userId);
    assertThat(profileId, is("7"));

    // test inheritance
    ComponentInst component = adminController.getComponentInst(componentId);
    ProfileInst p = component.getInheritedProfileInst("admin");
    assertThat(p.getAllUsers(), hasSize(1));
    assertThat(adminController.isComponentAvailable(componentId, userId), is(true));

    // test non inheritance
    String anotherComponentId = "kmelia4";
    component = adminController.getComponentInst(anotherComponentId);
    assertThat(component.getAllProfilesInst().size(), is(0));

    // remove users from space profile
    profile = adminController.getSpaceProfileInst(profileId);
    profile.removeAllUsers();
    adminController.updateSpaceProfileInst(profile, userId);

    // test inheritance
    component = adminController.getComponentInst(componentId);
    p = component.getInheritedProfileInst("admin");
    assertThat(p.isEmpty(), is(true));

    // component is always available due to inheritance policy
    assertThat(adminController.isComponentAvailable(componentId, userId), is(true));

    // test non inheritance
    component = adminController.getComponentInst(anotherComponentId);
    assertThat(component.getAllProfilesInst().size(), is(0));
    assertThat(p.isEmpty(), is(true));

    // another component is always unavailable
    assertThat(adminController.isComponentAvailable(anotherComponentId, userId), is(false));
  }

  @Test
  public void testProfileInheritanceBetweenSpaces() {
    String rootSpaceId = "WA4";
    String subSpaceIdWithoutInheritance = "WA5";
    String subSpaceIdWithInheritance = "WA6";

    // force space to block inheritance
    SpaceInst spaceWithoutInheritance =
        adminController.getSpaceInstById(subSpaceIdWithoutInheritance);
    spaceWithoutInheritance.setInheritanceBlocked(true);
    adminController.updateSpaceInst(spaceWithoutInheritance);

    spaceWithoutInheritance = adminController.getSpaceInstById(subSpaceIdWithoutInheritance);
    assertThat(spaceWithoutInheritance.isInheritanceBlocked(), is(true));
    SpaceInst spaceWithInheritance = adminController.getSpaceInstById(subSpaceIdWithInheritance);
    assertThat(spaceWithInheritance.isInheritanceBlocked(), is(false));

    // set space profile (admin)
    SpaceProfileInst profile = new SpaceProfileInst();
    profile.setSpaceFatherId(rootSpaceId);
    profile.setName("admin");
    profile.addUser(userId);

    String profileId = adminController.addSpaceProfileInst(profile, rootSpaceId);
    assertThat(profileId, is("7"));

    SpaceInst root = adminController.getSpaceInstById(rootSpaceId);
    List<SpaceProfileInst> profiles = root.getProfiles();
    assertThat(profiles.size(), is(1));

    spaceWithoutInheritance = adminController.getSpaceInstById(subSpaceIdWithoutInheritance);
    profiles = spaceWithoutInheritance.getAllSpaceProfilesInst();
    assertThat(profiles.size(), is(0));

    spaceWithInheritance = adminController.getSpaceInstById(subSpaceIdWithInheritance);
    profiles = spaceWithInheritance.getAllSpaceProfilesInst();
    assertThat(profiles.size(), is(1));
  }

  @Test
  public void testProfileInheritanceBetweenNewSpaces() {
    // creating new root space
    SpaceInst newRootSpace = new SpaceInst();
    newRootSpace.setName("Root space");
    newRootSpace.setCreatorUserId(userId);
    String newRootSpaceId = adminController.addSpaceInst(newRootSpace);
    assertThat(newRootSpaceId, is("WA200"));
    // creating a first child
    SpaceInst sub1 = new SpaceInst();
    sub1.setName("Sub 1");
    sub1.setCreatorUserId(userId);
    sub1.setDomainFatherId(newRootSpaceId);
    String sub1Id = adminController.addSpaceInst(sub1);
    assertThat(sub1Id, is("WA201"));
    adminController.updateSpaceOrderNum(sub1Id, 0);
    sub1 = adminController.getSpaceInstById(sub1Id);
    // by default, inheritance is active, checking it
    assertThat(sub1.isInheritanceBlocked(), is(false));

    // blocking inheritance
    sub1.setInheritanceBlocked(true);
    sub1.setUpdaterUserId(userId);
    adminController.updateSpaceInst(sub1);

    List<SpaceInstLight> subspaces =
        TreeCache.getSubSpaces(Integer.parseInt(newRootSpaceId.substring(2)));
    assertThat(subspaces.size(), is(1));
    SpaceInstLight subspace = subspaces.get(0);
    assertThat(subspace.isInheritanceBlocked(), is(true));

    // creating a second child
    SpaceInst sub2 = new SpaceInst();
    sub2.setName("Sub 2");
    sub2.setCreatorUserId(userId);
    sub2.setDomainFatherId(newRootSpaceId);
    String sub2Id = adminController.addSpaceInst(sub2);
    assertThat(sub2Id, is("WA202"));

    sub2 = adminController.getSpaceInstById(sub2Id);

    adminController.updateSpaceOrderNum(sub2Id, 1);

    // adding a profile on root level
    SpaceProfileInst profile = new SpaceProfileInst();
    profile.setSpaceFatherId(newRootSpaceId);
    profile.setName("admin");
    profile.addUser(userId);

    String profileId = adminController.addSpaceProfileInst(profile, userId);
    assertThat(profileId, is("7"));

    profile = adminController.getSpaceProfileInst(profileId);
    profile.addUser("2");
    adminController.updateSpaceProfileInst(profile, userId);

    // checking first child have no active profiles
    sub1 = adminController.getSpaceInstById(sub1Id);
    assertThat(sub1.getAllSpaceProfilesInst().size(), is(0));

    // checking second child have one active profile
    sub2 = adminController.getSpaceInstById(sub2Id);
    assertThat(sub2.getAllSpaceProfilesInst().size(), is(1));

  }

  @Test
  public void testSpaceManager() throws AdminException {
    // set user1 as space manager
    SpaceProfileInst profile = new SpaceProfileInst();
    profile.setSpaceFatherId("WA2");
    profile.setName("Manager");
    profile.addUser(userId);
    String profileId = adminController.addSpaceProfileInst(profile, userId);
    assertThat(profileId, is("7"));

    // set user2 as simple reader on space
    profile = new SpaceProfileInst();
    profile.setSpaceFatherId("WA2");
    profile.setName(SilverpeasRole.reader.name());
    profile.addUser("2");
    profileId = adminController.addSpaceProfileInst(profile, "1");
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
    String targetSpaceId = "WA3";
    PasteDetail pasteDetail = new PasteDetail("almanach2", userId);
    pasteDetail.setToSpaceId(targetSpaceId);
    String componentId = adminController.copyAndPasteComponent(pasteDetail);

    String expectedComponentId = "almanach5";
    assertThat(componentId, is(expectedComponentId));

    ComponentInst component = adminController.getComponentInst(expectedComponentId);
    assertThat(component, is(notNullValue()));
    assertThat(component.getLabel(), is("Dates clés"));
    assertThat(component.getId(), is(expectedComponentId));

    SpaceInst space = adminController.getSpaceInstById(targetSpaceId);
    List<ComponentInst> components = space.getAllComponentsInst();
    assertThat(components, hasSize(2));
    component = components.get(1);
    assertThat(component.getId(), is(expectedComponentId));
  }

  @Test
  public void testCopyAndPasteRootSpace() throws AdminException, QuotaException {
    String[] rootSpaceIds = adminController.getAllRootSpaceIds();
    assertThat(rootSpaceIds.length, is(4));
    String targetSpaceId = null;
    PasteDetail pasteDetail = new PasteDetail(userId);
    pasteDetail.setFromSpaceId("WA1");
    pasteDetail.setToSpaceId(targetSpaceId);
    String newSpaceId = adminController.copyAndPasteSpace(pasteDetail);
    String expectedSpaceId = "WA200";
    assertThat(newSpaceId, is(expectedSpaceId));

    SpaceInstLight spaceLight = adminController.getSpaceInstLight(expectedSpaceId);
    assertThat(spaceLight, is(notNullValue()));
    assertThat(spaceLight.getName(), is("Copie de Space 1"));
    assertThat(spaceLight.getId(), is(expectedSpaceId));
    assertThat(spaceLight.getOrderNum(), is(rootSpaceIds.length));

    SpaceInst space = adminController.getSpaceInstById(expectedSpaceId);

    // test pasted component
    String expectedComponentId = "kmelia5";
    List<ComponentInst> components = space.getAllComponentsInst();
    assertThat(components.size(), is(2));
    ComponentInst component = components.get(0);
    assertThat(component.getId(), is(expectedComponentId));

    // test pasted subspace
    String expectedSubSpaceId = "WA201";
    List<SpaceInst> subSpaces = space.getSubSpaces();
    assertThat(subSpaces.size(), is(1));
    SpaceInst subSpace = subSpaces.get(0);
    assertThat(subSpace, is(notNullValue()));
    assertThat(subSpace.getId(), is(expectedSubSpaceId));
    assertThat(subSpace.getName(), is("Space 1-2"));

    rootSpaceIds = adminController.getAllRootSpaceIds();
    assertThat(rootSpaceIds.length, is(5));
  }

  @Test
  public void testCopyAndPasteSubSpaceInSpace() throws AdminException, QuotaException {
    String copiedSpaceId = "WA2";
    String targetSpaceId = "WA3";
    PasteDetail pasteDetail = new PasteDetail(userId);
    pasteDetail.setFromSpaceId(copiedSpaceId);
    pasteDetail.setToSpaceId(targetSpaceId);
    String newSpaceId = adminController.copyAndPasteSpace(pasteDetail);

    String expectedSpaceId = "WA200";
    assertThat(newSpaceId, is(expectedSpaceId));

    SpaceInstLight copiedSpace = adminController.getSpaceInstLight(copiedSpaceId);
    SpaceInstLight spaceLight = adminController.getSpaceInstLight(expectedSpaceId);
    assertThat(spaceLight, is(notNullValue()));
    assertThat(spaceLight.getName(), is(copiedSpace.getName()));
    assertThat(spaceLight.getId(), is(expectedSpaceId));
    assertThat(spaceLight.getOrderNum(), is(0));

    SpaceInst space = adminController.getSpaceInstById(expectedSpaceId);

    // test pasted component
    String expectedComponentId = "almanach5";
    List<ComponentInst> components = space.getAllComponentsInst();
    assertThat(components.size(), is(1));
    ComponentInst component = components.get(0);
    assertThat(component.getId(), is(expectedComponentId));

    // test new space is well subspace of target space
    SpaceInst targetSpace = adminController.getSpaceInstById(targetSpaceId);
    List<SpaceInst> targetSubSpaces = targetSpace.getSubSpaces();
    assertThat(targetSubSpaces.size(), is(1));
    assertThat(targetSubSpaces.get(targetSubSpaces.size() - 1).getId(), is(expectedSpaceId));
  }

  @Test
  public void testCopyAndPasteSpaceInItsSubSpace() throws AdminException, QuotaException {
    String copiedSpaceId = "WA1";
    String targetSpaceId = "WA2";
    PasteDetail pasteDetail = new PasteDetail(userId);
    pasteDetail.setFromSpaceId(copiedSpaceId);
    pasteDetail.setToSpaceId(targetSpaceId);
    String newSpaceId = adminController.copyAndPasteSpace(pasteDetail);
    assertThat(newSpaceId, is(nullValue()));
  }

  @Test
  public void testMoveSubSpaceAsRootSpace() throws AdminException {

    String spaceId = "WA2";
    SpaceInstLight space = adminController.getSpaceInstLight(spaceId);
    String originalFatherId = space.getFatherId();

    // check profiles before moving
    SpaceInst fullSpace = adminController.getSpaceInstById(spaceId);
    assertThat(fullSpace.getAllSpaceProfilesInst().size(), is(3));
    assertThat(fullSpace.getInheritedProfiles().size(), is(2));

    adminController.moveSpace(spaceId, null);

    // check if space is well on top level
    List<String> rootSpaceIds = Arrays.asList(adminController.getAllRootSpaceIds());
    assertThat(rootSpaceIds.contains(spaceId), is(true));
    assertThat(rootSpaceIds.size(), is(5));

    //check if space is no more in original parent
    List<String> subSpaceIds = Arrays.asList(adminController.getAllSubSpaceIds(originalFatherId));
    assertThat(subSpaceIds.contains(spaceId), is(false));
    assertThat(subSpaceIds.isEmpty(), is(true));

    // check if space have no parent
    space = adminController.getSpaceInstLight(spaceId);
    assertThat(space.getFatherId(), is("0"));
    assertThat(space.getLevel(), is(0));

    //check profiles after moving
    fullSpace = adminController.getSpaceInstById(spaceId);
    assertThat(fullSpace.getAllSpaceProfilesInst().size(), is(1));
    assertThat(fullSpace.getInheritedProfiles().size(), is(0));

    // check almanach rights
    String componentId = "almanach2";
    assertThat(adminController.isComponentAvailable(componentId, "1"), is(false));
    assertThat(adminController.isComponentAvailable(componentId, "2"), is(true));
    assertThat(adminController.isComponentAvailable(componentId, "3"), is(true));
  }

  @Test
  public void testMoveSpaceInItsSubspace() throws AdminException {
    String spaceId = "WA1";
    String subspaceId = "WA2";
    adminController.moveSpace(spaceId, subspaceId);
    SpaceInstLight space = adminController.getSpaceInstLight(spaceId);
    // check if space is unchanged
    assertThat(space.getFatherId(), is("0"));
    assertThat(space.getLevel(), is(0));
  }

  @Test
  public void testMoveRootSpaceInASubspace() throws AdminException {
    String spaceId = "WA1";
    String targetSpaceId = "WA3";
    // check profiles before moving
    SpaceInst fullSpace = adminController.getSpaceInstById(spaceId);
    assertThat(fullSpace.getAllSpaceProfilesInst().size(), is(2));
    assertThat(fullSpace.getInheritedProfiles().size(), is(0));
    List<String> rootSpaceIds = Arrays.asList(adminController.getAllRootSpaceIds());
    assertThat(rootSpaceIds, hasItem(spaceId));
    assertThat(rootSpaceIds, hasSize(4));
    adminController.moveSpace(spaceId, targetSpaceId);
    // check if space is no more on top level
    rootSpaceIds = Arrays.asList(adminController.getAllRootSpaceIds());
    assertThat(rootSpaceIds, not(hasItem(spaceId)));
    assertThat(rootSpaceIds, hasSize(3));
    //check if space is in new parent
    List<String> subSpaceIds = Arrays.asList(adminController.getAllSubSpaceIds(targetSpaceId));
    assertThat(subSpaceIds, hasItem(spaceId));
    // check if space have got new parent
    SpaceInstLight space = adminController.getSpaceInstLight(spaceId);
    assertThat(space.getFatherId(), is("3"));
    assertThat(space.getLevel(), is(1));
    //check profiles after moving
    fullSpace = adminController.getSpaceInstById(spaceId);
    assertThat(fullSpace.getAllSpaceProfilesInst(), hasSize(3));
    assertThat(fullSpace.getProfiles(), hasSize(2));
    assertThat(fullSpace.getInheritedProfiles(), hasSize(1));
    assertThat(fullSpace.getSpaceProfileInst(SilverpeasRole.publisher.name()).getNumUser(), is(1));
    assertThat(fullSpace.getSpaceProfileInst(SilverpeasRole.reader.name()).getNumUser(), is(1));
    assertThat(fullSpace.getInheritedSpaceProfileInst(SilverpeasRole.publisher.name()).getNumUser(),
        is(1));

    // check GED rights
    String componentId = "kmelia1";
    ComponentInst component = adminController.getComponentInst(componentId);
    assertThat(component.getAllProfilesInst(), hasSize(2));
    assertThat(component.getInheritedProfileInst(SilverpeasRole.publisher.name()).getNumUser(),
        is(2));
    assertThat(adminController.isComponentAvailable(componentId, "2"), is(true));
    assertThat(adminController.isComponentAvailable(componentId, "1"), is(true));
    String[] roles = organizationController.getUserProfiles("1", componentId);
    assertThat(roles, arrayWithSize(2));
    roles = organizationController.getUserProfiles("2", componentId);
    assertThat(roles, arrayWithSize(1));
  }

  @Test
  public void testMoveSpaceWithoutRole() throws AdminException {
    String parentSpaceId = "WA100";
    String spaceId = "WA110";
    String tempParentSpaceId = "WA3";
    // check profiles before moving
    SpaceInst fullSpace = adminController.getSpaceInstById(spaceId);
    assertThat(fullSpace.getAllSpaceProfilesInst(), hasSize(0));
    assertThat(fullSpace.getAllSpaceProfilesInst(), hasSize(0));
    // check if space on top level
    List<String> rootSpaceIds = Arrays.asList(adminController.getAllRootSpaceIds());
    assertThat(rootSpaceIds, not(hasItem(spaceId)));
    assertThat(rootSpaceIds, hasSize(4));
    // check GED rights
    String componentId = "kmelia210";
    ComponentInst component = adminController.getComponentInst(componentId);
    assertThat(component.getAllProfilesInst(), hasSize(0));
    assertThat(component.getInheritedProfileInst(SilverpeasRole.publisher.name()), is(nullValue()));
    assertThat(adminController.isComponentAvailable(componentId, "2"), is(false));
    assertThat(adminController.isComponentAvailable(componentId, "1"), is(false));
    String[] roles = organizationController.getUserProfiles("1", componentId);
    assertThat(roles, arrayWithSize(0));
    roles = organizationController.getUserProfiles("2", componentId);
    assertThat(roles, arrayWithSize(0));

    adminController.moveSpace(spaceId, tempParentSpaceId);

    // check if space is no more on top level
    rootSpaceIds = Arrays.asList(adminController.getAllRootSpaceIds());
    assertThat(rootSpaceIds, not(hasItem(spaceId)));
    assertThat(rootSpaceIds, hasSize(4));
    //check if space is in new parent
    List<String> subSpaceIds = Arrays.asList(adminController.getAllSubSpaceIds(tempParentSpaceId));
    assertThat(subSpaceIds, hasItem(spaceId));
    // check if space have got new parent
    SpaceInstLight space = adminController.getSpaceInstLight(spaceId);
    assertThat(space.getFatherId(), is("3"));
    assertThat(space.getLevel(), is(1));
    //check profiles after moving
    fullSpace = adminController.getSpaceInstById(spaceId);
    assertThat(fullSpace.getAllSpaceProfilesInst(), hasSize(1));
    assertThat(fullSpace.getProfiles(), hasSize(0));
    assertThat(fullSpace.getInheritedProfiles(), hasSize(1));
    assertThat(fullSpace.getSpaceProfileInst(SilverpeasRole.publisher.name()), is(nullValue()));
    assertThat(fullSpace.getSpaceProfileInst(SilverpeasRole.reader.name()), is(nullValue()));
    assertThat(fullSpace.getInheritedSpaceProfileInst(SilverpeasRole.publisher.name()).getNumUser(),
        is(1));
    // check GED rights
    component = adminController.getComponentInst(componentId);
    assertThat(component.getAllProfilesInst(), hasSize(1));
    assertThat(component.getInheritedProfileInst(SilverpeasRole.publisher.name()).getNumUser(),
        is(1));
    assertThat(adminController.isComponentAvailable(componentId, "2"), is(true));
    assertThat(adminController.isComponentAvailable(componentId, "1"), is(false));
    roles = organizationController.getUserProfiles("1", componentId);
    assertThat(roles, arrayWithSize(0));
    roles = organizationController.getUserProfiles("2", componentId);
    assertThat(roles, arrayWithSize(1));

    //Moving back
    adminController.moveSpace(spaceId, parentSpaceId);

    space = adminController.getSpaceInstLight(spaceId);
    assertThat(space.getFatherId(), is("100"));
    assertThat(space.getLevel(), is(1));

    //check profiles after moving
    fullSpace = adminController.getSpaceInstById(spaceId);
    assertThat(fullSpace.getAllSpaceProfilesInst(), hasSize(0));
    assertThat(fullSpace.getProfiles(), hasSize(0));
    assertThat(fullSpace.getInheritedProfiles(), hasSize(0));
    assertThat(fullSpace.getSpaceProfileInst(SilverpeasRole.publisher.name()), is(nullValue()));
    assertThat(fullSpace.getSpaceProfileInst(SilverpeasRole.reader.name()), is(nullValue()));
    assertThat(fullSpace.getInheritedSpaceProfileInst(SilverpeasRole.publisher.name()),
        is(nullValue()));

    // check GED rights
    component = adminController.getComponentInst(componentId);
    assertThat(component.getAllProfilesInst(), hasSize(1));
    assertThat(component.getInheritedProfileInst(SilverpeasRole.publisher.name()).getNumUser(),
        is(0));
    assertThat(adminController.isComponentAvailable(componentId, "2"), is(false));
    assertThat(adminController.isComponentAvailable(componentId, "1"), is(false));
    roles = organizationController.getUserProfiles("1", componentId);
    assertThat(roles, arrayWithSize(0));

    roles = organizationController.getUserProfiles("2", componentId);
    assertThat(roles, arrayWithSize(0));
  }

  @Test
  public void testApplicationMove() throws AdminException {
    String sourceId = "WA1";
    String destId = "WA3";
    String componentId = "kmelia1";
    SpaceInst dest = admin.getSpaceInstById(destId);
    List<ComponentInst> components = dest.getAllComponentsInst();
    admin.moveComponentInst(destId, componentId, "",
        components.toArray(new ComponentInst[components.size()]));
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