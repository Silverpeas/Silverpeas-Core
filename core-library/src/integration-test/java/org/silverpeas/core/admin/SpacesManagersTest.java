/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.admin;

import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.space.SpaceProfileInst;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.space.SpaceServiceProvider;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.admin.component.ComponentHelper;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileFolderManager;
import org.silverpeas.core.util.memory.MemoryData;
import org.silverpeas.core.util.memory.MemoryUnit;

import javax.inject.Inject;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class SpacesManagersTest {

  @Inject
  private AdminController adminController;

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom("create_space_managers_database.sql")
          .loadInitialDataSetFrom("test-spacesmanagers-dataset.sql");

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
  public void reloadCache() {
    adminController.reloadAdminCache();
  }

  @Test
  public void testAddRemoveSpaceManager() {
    //add profile
    SpaceProfileInst profile = new SpaceProfileInst();
    profile.setSpaceFatherId("WA2");
    profile.setName(SpaceProfileInst.SPACE_MANAGER);
    profile.addUser("2");
    String profileId = adminController.addSpaceProfileInst(profile, "1");
    assertThat(profileId, is("4"));

    //check manager
    profile = adminController.getSpaceProfileInst("4");
    assertThat(profile.getNumUser(), is(1));

    //remove manager
    profile.getAllUsers().clear();
    adminController.updateSpaceProfileInst(profile, "1");
    profile = adminController.getSpaceProfileInst("4");
    assertThat(profile.getNumUser(), is(0));
  }

  @Test
  public void testGetManageableSpaces() {
    String[] spaceIds = adminController.getUserManageableSpaceIds("1");
    assertThat(spaceIds, is(notNullValue()));
    assertThat(spaceIds.length, is(2));

    spaceIds = adminController.getUserManageableSpaceIds("2");
    assertThat(spaceIds, is(notNullValue()));
    assertThat(spaceIds.length, is(4));

    spaceIds = adminController.getUserManageableSubSpaceIds("1", "WA1");
    assertThat(spaceIds, is(notNullValue()));
    assertThat(spaceIds.length, is(2));

    spaceIds = adminController.getUserManageableSubSpaceIds("2", "WA1");
    assertThat(spaceIds, is(notNullValue()));
    assertThat(spaceIds.length, is(2));

    spaceIds = adminController.getUserManageableSubSpaceIds("2", "WA2");
    assertThat(spaceIds, is(notNullValue()));
    assertThat(spaceIds.length, is(1));
    assertThat(spaceIds[0], is("3"));
  }
}