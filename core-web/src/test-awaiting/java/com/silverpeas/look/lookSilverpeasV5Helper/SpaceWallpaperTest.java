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

package com.silverpeas.look.lookSilverpeasV5Helper;

import com.silverpeas.look.LookSilverpeasV5Helper;
import java.util.ArrayList;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import org.silverpeas.core.util.ResourceLocator;
import java.util.List;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.util.SettingBundle;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.silverpeas.core.admin.service.OrganizationController;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Unit tests covering the operations on the space wallpapers provided by the
 * LookSilverpeasV5Helper objects.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/spring-look.xml")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class SpaceWallpaperTest {

  private SettingBundle resources = ResourceLocator.getSettingBundle(
      "org.silverpeas.util.viewGenerator.settings.SilverpeasV5");

  @Inject
  private OrganizationController organizationController;

  public SpaceWallpaperTest() {
  }

  @Before
  public void prepareMocks() {
    // check the resource bundle is loaded
    assertTrue(resources.getBoolean("IsLoaded", false));
    assertNotNull(organizationController);
    when(organizationController.isSpaceAvailable(anyString(), anyString())).thenReturn(false);
    when(organizationController.isComponentAvailable(anyString(), anyString())).thenReturn(false);
  }

  /**
   * When a root space has a wallpaper specifically set, this wallpaper should be returned.
   */
  @Test
  public void getWallpaperOfARootSpace() {
    prepareSpaces("WA1");
    LookSilverpeasV5Helper lookHelper = aLookSilverpeasV5HelperToTest();
    lookHelper.setSpaceId("WA1");
    String wallpaper = lookHelper.getSpaceWallPaper();
    assertEquals(
        "/silverpeas/OnlineFileServer/wallPaper.png?ComponentId=Space1&SourceFile=wallPaper.png&MimeType=image/png&Directory=look",
        wallpaper);
  }

  /**
   * When a space within a path of spaces has a wallpaper specifically set, this wallpaper should be
   * returned.
   */
  @Test
  public void getWallpaperOfTheCurrentSpace() {
    prepareSpaces("WA3", "WA2", "WA1");
    LookSilverpeasV5Helper lookHelper = aLookSilverpeasV5HelperToTest();
    lookHelper.setSpaceIdAndSubSpaceId("WA1");
    String wallpaper = lookHelper.getSpaceWallPaper();
    assertEquals(
        "/silverpeas/OnlineFileServer/wallPaper.png?ComponentId=Space1&SourceFile=wallPaper.png&MimeType=image/png&Directory=look",
        wallpaper);
  }

  /**
   * When a space has no specifically wallpaper set, the one of the fist parent having a wallpaper
   * specifically set is returned
   */
  @Test
  public void getWallpaperOfTheFirstParentSpace() {
    prepareSpaces("WA1", "WA11", "WA111");
    LookSilverpeasV5Helper lookHelper = aLookSilverpeasV5HelperToTest();
    lookHelper.setSpaceIdAndSubSpaceId("WA111");
    String wallpaper = lookHelper.getSpaceWallPaper();
    assertEquals(
        "/silverpeas/OnlineFileServer/wallPaper.png?ComponentId=Space1&SourceFile=wallPaper.png&MimeType=image/png&Directory=look",
        wallpaper);
  }

  /**
   * When no space has no specifically wallpaper set, the null is returned.
   */
  @Test
  public void getDefaultWallpaper() {
    prepareSpaces("WA2", "WA11", "WA111");
    LookSilverpeasV5Helper lookHelper = aLookSilverpeasV5HelperToTest();
    lookHelper.setSpaceId("WA111");
    String wallpaper = lookHelper.getSpaceWallPaper();
    assertNull(wallpaper);
  }

  @Test
  public void getWallpaperOfSpaceHavingOneShouldReturn1() {
    prepareSpaces("WA2", "WA1", "WA0");
    LookSilverpeasV5Helper lookHelper = aLookSilverpeasV5HelperToTest();
    lookHelper.setSpaceIdAndSubSpaceId("WA0");
    String wallpaper = lookHelper.getWallPaper("WA1");
    assertEquals("1", wallpaper);
  }

  @Test
  public void getWallpaperOfSpaceWithoutOneShouldReturn0() {
    prepareSpaces("WA2", "WA1", "WA0");
    LookSilverpeasV5Helper lookHelper = aLookSilverpeasV5HelperToTest();
    lookHelper.setSpaceIdAndSubSpaceId("WA0");
    String wallpaper = lookHelper.getWallPaper("WA0");
    assertEquals("0", wallpaper);
  }

  /**
   * Gets a mock of the organization controller.
   * @return a mock
   */
  private OrganizationController getOrganisationController() {
    return organizationController;
  }

  /**
   * Gets a LookSilverpeasV5Helper object initialized for tests.
   * @return the LookSilverpeasV5Helper object to test.
   */
  private LookSilverpeasV5Helper aLookSilverpeasV5HelperToTest() {
    MainSessionController sessionController = mock(MainSessionController.class);
    when(sessionController.getOrganisationController()).thenReturn(organizationController);
    when(sessionController.getUserId()).thenReturn("0");
    return new LookSilverpeasV5Helper(sessionController, resources);
  }

  /**
   * Prepares an hierarchy of spaces from the specified space identifiers.
   * The first identifier is the for the root space whereas the last one is a child one.
   * @param spaceIds the identifiers of space.
   */
  private void prepareSpaces(String... spaceIds) {
    List<SpaceInst> spaces = new ArrayList<SpaceInst>();
    for (String spaceId : spaceIds) {
      SpaceInst space = mock(SpaceInst.class);
      when(space.getId()).thenReturn(spaceId);
      spaces.add(space);
    }
    OrganizationController controller = getOrganisationController();
    when(controller.getSpacePath(anyString())).thenReturn(spaces);
  }
}
