/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stratelia.webactiv.beans.admin;

import com.silverpeas.components.model.AbstractTestDao;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ehugonnet
 */
public class AdminTest extends AbstractTestDao {

  private Admin instance;
  
  public AdminTest() {
    instance = new Admin();
  }

  @Override
  protected void setUp() throws Exception {  
    super.setUp();    
    instance.reloadCache();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();    
    instance.reloadCache();
  }

  
  
  @Override
  protected String getDatasetFileName() {
    return "test-admin-spaces-dataset.xml";
  }

  /**
   * Test of getGeneralSpaceId method, of class Admin.
   */
  @Test
  public void testGetGeneralSpaceId() {
    String expResult = "WA1";
    String result = instance.getGeneralSpaceId();
    assertEquals(expResult, result);
  }

  /**
   * Test of getSpaceInstById method, of class Admin.
   */
  @Test
  public void testGetSpaceInstById() throws Exception {
    String sClientSpaceId = "2";    
    SpaceInst expResult = new SpaceInst();
    expResult.setId(sClientSpaceId);
    SpaceInst result = instance.getSpaceInstById(sClientSpaceId);
    assertNotNull(result);
    assertEquals(expResult.getId(), result.getId());
    assertEquals("MyTests", result.getName());
    assertEquals("Space for test", result.getDescription());
    assertEquals("0", result.getCreatorUserId());
    assertEquals(1281941919845L, result.getCreateDate().getTime());
    assertNotNull(result.getAllComponentsInst());
    assertEquals(2, result.getAllComponentsInst().size());
  }

  /**
   * Test of getAllSubSpaceIds method, of class Admin.
   */
  @Test
  public void testGetAllSubSpaceIdsForDomain() throws Exception {
    String sDomainFatherId = "1";
    String[] result = instance.getAllSubSpaceIds(sDomainFatherId);
    assertThat(result, org.hamcrest.collection.IsArrayContaining.hasItemInArray("WA2"));
    assertThat(result, org.hamcrest.collection.IsArrayContaining.hasItemInArray("WA5"));
  }

  /**
   * Test of isSpaceInstExist method, of class Admin.
   */
  @Test
  public void testIsSpaceInstExist() throws Exception {
    assertTrue(instance.isSpaceInstExist("WA3"));
    assertTrue(instance.isSpaceInstExist("WA1"));
    assertTrue(instance.isSpaceInstExist("WA5"));
    assertFalse(instance.isSpaceInstExist("WA10"));
  }

  /**
   * Test of getAllRootSpaceIds method, of class Admin.
   */
  @Test
  public void testGetAllRootSpaceIds() throws Exception {
    String[] expResult = new String[]{"WA1"};
    String[] result = instance.getAllRootSpaceIds();
    assertNotNull(result);
    assertEquals(1, result.length);
    assertEquals(expResult[0], result[0]);
  }

  /**
   * Test of getTreeView method, of class Admin.
   */
  @Test
  public void testGetTreeView() throws Exception {
    String userId = "0";
    String spaceId = "2";
    Map<String, SpaceAndChildren> result = instance.getTreeView(userId, spaceId);
    assertNotNull(result);
    assertEquals(3, result.size());
    SpaceAndChildren space = result.get("WA2");
    assertNotNull(space);
    space = result.get("WA3");
    assertNotNull(space);
    space = result.get("WA4");
    assertNotNull(space);
  }

  /**
   * Test of getUserSpaceTreeview method, of class Admin.
   */
  @Test
  public void testGetUserSpaceTreeview() throws Exception {
    String userId = "0";
    long startTime = System.currentTimeMillis();
    List<SpaceInstLight> result = instance.getUserSpaceTreeview(userId);
    System.out.println("New method : " + (System.currentTimeMillis() - startTime));
    assertNotNull(result);
    assertEquals(5, result.size());
    startTime = System.currentTimeMillis();
    List<SpaceInstLight> oldResult = instance.getUserSpaceTreeview(userId);
    System.out.println("Old method : " + (System.currentTimeMillis() - startTime));
    assertTrue(result.containsAll(oldResult));
    assertTrue(oldResult.containsAll(result));
  }

  /**
   * Test of getAllRootSpaceIds method, of class Admin.
   */
  @Test
  public void testGetAllRootSpaceIdsForUser() throws Exception {
    String sUserId = "0";
    String[] result = instance.getAllRootSpaceIds(sUserId);
    String[] expectedResult = new String[]{"WA1"};
    assertNotNull(result);
    assertEquals(expectedResult.length, result.length);
    for (int i = 0; i < result.length; i++) {
      assertEquals(expectedResult[i], result[i]);
    }
  }
}