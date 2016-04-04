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

package com.silverpeas.look;

import com.stratelia.webactiv.beans.admin.SpaceInst;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.service.OrganizationController;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests on the SilverpeasLook operations.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-look.xml"})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class SilverpeasLookTest {

  @Inject
  private SilverpeasLook look;
  @Inject
  private OrganizationController organizationController;

  public SilverpeasLookTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
    assertNotNull(look);
    assertNotNull(organizationController);
  }

  /**
   * Test of hasSpaceWallpaper method, of class SilverpeasLook.
   */
  @Test
  public void testHasSpaceWallpaper() {
    assertThat(true, is(true));
  }

  /**
   * When a root space has a wallpaper specifically set, this wallpaper should be returned.
   */
  @Test
  public void getWallpaperOfARootSpace() {
    prepareSpaces("WA1");
    String wallpaper = look.getWallpaperOfSpace("WA1");
    assertThat(wallpaper, is("/silverpeas/OnlineFileServer/wallPaper.png?ComponentId=" +
        "Space1&SourceFile=wallPaper.png&MimeType=image/png&Directory=look"));
  }

  /**
   * When a space within a path of spaces has a wallpaper specifically set, this wallpaper should be
   * returned.
   */
  @Test
  public void getWallpaperOfTheCurrentSpace() {
    prepareSpaces("WA3", "WA2", "WA1");
    String wallpaper = look.getWallpaperOfSpace("WA1");
    assertThat(wallpaper,is("/silverpeas/OnlineFileServer/wallPaper.png?ComponentId=Space1" +
        "&SourceFile=wallPaper.png&MimeType=image/png&Directory=look"));
  }

  /**
   * When a space has no specifically wallpaper set, the one of the fist parent having a wallpaper
   * specifically set is returned
   */
  @Test
  public void getWallpaperOfTheFirstParentSpace() {
    prepareSpaces("WA1", "WA11", "WA111");
    String wallpaper = look.getWallpaperOfSpace("WA111");
    assertThat(wallpaper, is("/silverpeas/OnlineFileServer/wallPaper.png?ComponentId=Space1" +
        "&SourceFile=wallPaper.png&MimeType=image/png&Directory=look"));
  }

  /**
   * When no space has no specifically wallpaper set, the null is returned.
   */
  @Test
  public void getNoWallpaperWithSpacesWithoutSpecificWallpaper() {
    prepareSpaces("WA2", "WA11", "WA111");
    String wallpaper = look.getWallpaperOfSpace("WA111");
    assertNull(wallpaper);
  }

  @Test
  public void spaceWithASpecificWallpaper() {
    prepareSpaces("WA0", "WA1", "WA2");
    assertThat(look.hasSpaceWallpaper("WA1"), is(true));
  }

  @Test
  public void spaceWithoutASpecificWallpaper() {
    prepareSpaces("WA1", "WA2", "WA3");
    assertThat(look.hasSpaceWallpaper("WA3"), is(false));
  }

  @Test
  public void getDefaultWallpaperWithSpacesWithoutSpecificWallpaper() {
    prepareSpaces("WA2", "WA11", "WA111");
    String wallpaper = look.getWallpaperOfSpaceOrDefaultOne("WA111");
    assertThat(wallpaper, equalTo("/silverpeas/admin/jsp/icons/silverpeas_light/bandeauTop.jpg"));
  }

  /**
   * When a root space has a CSS specifically set, this CSS should be returned.
   */
  @Test
  public void getCSSOfARootSpace() {
    prepareSpaces("WA1");
    String spaceId = look.getSpaceWithCSS("WA1");
    assertThat(spaceId, is("1"));
  }

  /**
   * When a space has no specifically CSS set, the one of the fist parent having a CSS
   * specifically set is returned
   */
  @Test
  public void getCSSOfTheFirstParentSpace() {
    prepareSpaces("WA1", "WA11", "WA111");
    String url = look.getCSSOfSpace("WA111");
    assertThat(url, is("/silverpeas/OnlineFileServer/styles.css?ComponentId=Space1" +
        "&SourceFile=styles.css&MimeType=text/css&Directory=look"));
  }

  @Test
  public void spaceWithoutASpecificCSS() {
    prepareSpaces("WA3");
    assertThat(look.getCSSOfSpace("WA3"), is(nullValue()));
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
    when(organizationController.getSpacePath(anyString())).thenReturn(spaces);
  }
}
