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

package com.silverpeas.admin.spaces;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.silverpeas.admin.components.WAComponent;
import com.silverpeas.util.PathTestUtil;

import static org.junit.Assert.*;

/**
 * Unit tests on the SpaceInstanciator objects behaviour.
 */
public class SpaceInstanciatorTest {

  private static final String XMLSPACETEMPLATES_DIR = PathTestUtil.TARGET_DIR
          + "test-classes" + File.separatorChar + "xmlspacetemplates";

  public SpaceInstanciatorTest() {
  }

  /**
   * When some space templates are present, then they should be available as SpaceTemplate objects
   * by the SpaceInstanciator instances.
   */
  @Test
  public void atInitAllTemplatesAreAvailables() {
    Map<String, WAComponent> componentModels = new HashMap<String, WAComponent>();
    SpaceInstanciator instanciator = new SpaceInstanciator(componentModels, XMLSPACETEMPLATES_DIR);
    Map<String, SpaceTemplate> spaceTemplates = instanciator.getAllSpaceTemplates();
    assertNotNull(spaceTemplates);
    assertEquals(1, spaceTemplates.size());
    assertTrue(spaceTemplates.containsKey("EspaceProjet"));
  }

  /**
   * When no space templates are present, then no SpaceTemplate objects should be provided by the
   * SpaceInstanciator instances.
   */
  @Test
  public void atInitNoTemplatesDirectoryImpliesNoTemplatesLoaded() {
    Map<String, WAComponent> componentModels = new HashMap<String, WAComponent>();
    SpaceInstanciator instanciator = new SpaceInstanciator(componentModels, "toto");
    Map<String, SpaceTemplate> spaceTemplates = instanciator.getAllSpaceTemplates();
    assertNotNull(spaceTemplates);
    assertTrue(spaceTemplates.isEmpty());
  }
}