/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stratelia.webactiv.organization;

import com.silverpeas.components.model.AbstractTestDao;
import org.junit.Test;
import static org.junit.Assert.*;

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
    System.out.println("getAllRootSpaceIds");
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
}