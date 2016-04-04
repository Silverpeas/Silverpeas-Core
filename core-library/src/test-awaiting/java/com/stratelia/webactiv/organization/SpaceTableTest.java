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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stratelia.webactiv.organization;

import com.silverpeas.components.model.AbstractTestDao;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

/**
 *
 * @author ehugonnet
 */
public class SpaceTableTest extends AbstractTestDao {

  public SpaceTableTest() {
  }

  @Override
  protected String getDatasetFileName() {
    return "test-admin-spaces-dataset.xml";
  }

  /**
   * Test of isSpaceInstExist method, of class SpaceTable.
   */
  @Test
  public void testIsSpaceInstExist() throws Exception {
    OrganizationSchema schema = new OrganizationSchema(getConnection().getConnection());
    SpaceTable instance = schema.space;
    assertTrue(instance.isSpaceInstExist(1));
    assertTrue(instance.isSpaceInstExist(2));
    assertTrue(instance.isSpaceInstExist(3));
    assertTrue(instance.isSpaceInstExist(4));
    assertTrue(instance.isSpaceInstExist(5));
    assertFalse(instance.isSpaceInstExist(6));
    assertFalse(instance.isSpaceInstExist(100));
  }

  /**
   * Test of getAllSpaceIds method, of class SpaceTable.
   */
  @Test
  public void testGetAllSpaceIds() throws Exception {
    OrganizationSchema schema = new OrganizationSchema(getConnection().getConnection());
    SpaceTable instance = schema.space;
    String[] expResult = new String[]{"1", "2", "3", "4", "5"};
    String[] result = instance.getAllSpaceIds();
    assertNotNull(result);
    assertEquals(expResult.length, result.length);
    assertArrayEquals(expResult, result);
  }

  /**
   * Test of getAllRootSpaceIds method, of class SpaceTable.
   */
  @Test
  public void testGetAllRootSpaceIds() throws Exception {
    OrganizationSchema schema = new OrganizationSchema(getConnection().getConnection());
    SpaceTable instance = schema.space;
    String[] expResult = new String[]{"1"};
    String[] result = instance.getAllRootSpaceIds();
    assertNotNull(result);
    assertEquals(expResult.length, result.length);
    assertArrayEquals(expResult, result);
  }

  /**
   * Test of getDirectSubSpaceIds method, of class SpaceTable.
   */
  @Test
  public void testGetDirectSubSpaceIds() throws Exception {
    OrganizationSchema schema = new OrganizationSchema(getConnection().getConnection());
    SpaceTable instance = schema.space;
    String[] result = instance.getDirectSubSpaceIds(2);
    String[] expResult = new String[]{"3", "4"};
    assertNotNull(result);
    assertEquals(expResult.length, result.length);
    assertArrayEquals(expResult, result);
  }


  @Override
  protected String getTableCreationFileName() {
    return "create-database.sql";
  }
}