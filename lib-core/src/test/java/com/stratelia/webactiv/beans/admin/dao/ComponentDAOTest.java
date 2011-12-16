/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stratelia.webactiv.beans.admin.dao;

import com.google.common.collect.Lists;
import com.silverpeas.components.model.AbstractTestDao;
import org.junit.Test;
import org.junit.internal.matchers.IsCollectionContaining;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.sql.Connection;
import java.util.List;

import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;
/**
 *
 * @author ehugonnet
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class ComponentDAOTest extends AbstractTestDao {

  public ComponentDAOTest() {
  }


  @Override
  protected String getDatasetFileName() {
    return "test-components-dataset.xml";
  }

  /**
   * Test of getAllAvailableComponentIds method, of class ComponentDAO.
   */
  @Test
  public void testGetAllAvailableComponentIdsForUser() throws Exception {
    Connection con = null;
    try {
      con = getConnection().getConnection();
      List<String> groupIds = Lists.newArrayList();
      int userId = 0;
      List<String> result = ComponentDAO.getAllAvailableComponentIds(con, groupIds, userId);
      assertNotNull(result);
      assertEquals(17, result.size());
    } finally {
      if (con != null) {
        con.close();
      }
    }
  }

  /**
   * Test of getAllAvailableComponentIds method, of class ComponentDAO.
   */
  @Test
  public void testGetAllAvailableComponentIdsForUserAndGroups() throws Exception {
    Connection con = null;
    try {
      con = getConnection().getConnection();
      List<String> groupIds = Lists.newArrayList();
      int userId = 0;
      List<String> result = ComponentDAO.getAllAvailableComponentIds(con, groupIds, userId, null);
      assertNotNull(result);
      assertEquals(17, result.size());
      result = ComponentDAO.getAllAvailableComponentIds(con, groupIds, userId, "kmelia");
      assertNotNull(result);
      assertEquals(5, result.size());
      userId = 3;
      result = ComponentDAO.getAllAvailableComponentIds(con, groupIds, userId, null);
      assertNotNull(result);
      assertEquals(4, result.size());
      result = ComponentDAO.getAllAvailableComponentIds(con, groupIds, userId, "kmelia");
      assertNotNull(result);
      assertEquals(1, result.size());
      assertThat(result, IsCollectionContaining.hasItem("kmelia9"));
    } finally {
      if (con != null) {
        con.close();
      }
    }
  }

  @Test
  public void testGetComponentIdsInSpace() throws Exception {
    Connection con = null;
    try {
      con = getConnection().getConnection();
      int spaceId = 3;
      List<String> result = ComponentDAO.getComponentIdsInSpace(con, spaceId);
      assertNotNull(result);
      assertEquals("This space components should be present", 2, result.size());
      assertThat(result, IsCollectionContaining.hasItems("blog10", "kmelia11"));
      assertThat(result, contains("blog10", "kmelia11"));

      spaceId = 2;
      result = ComponentDAO.getComponentIdsInSpace(con, spaceId);
      assertNotNull(result);
      assertEquals("Subspace components should not be present", 2, result.size());
      assertThat(result, IsCollectionContaining.hasItems("questionReply12", "yellowpages19"));
      assertThat(result, contains("questionReply12", "yellowpages19"));
      
    } finally {
      if (con != null) {
        con.close();
      }
    }
  }
}