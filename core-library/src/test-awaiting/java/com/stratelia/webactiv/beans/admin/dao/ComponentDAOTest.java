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

package com.stratelia.webactiv.beans.admin.dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.silverpeas.components.model.AbstractTestDao;

import org.silverpeas.util.DBUtil;

import org.junit.Test;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;
/**
 *
 * @author ehugonnet
 */
public class ComponentDAOTest extends AbstractTestDao {

  public ComponentDAOTest() {
  }


  @Override
  protected String getDatasetFileName() {
    return "test-components-dataset.xml";
  }

  /**
   * Test of getAllAvailableComponentIds method, of class ComponentDAO.
   * @throws Exception
   */
  @Test
  public void testGetAllAvailableComponentIdsForUser() throws Exception {
    Connection con = null;
    try {
      con = getConnection().getConnection();
      List<String> groupIds = new ArrayList<String>();
      int userId = 0;
      List<String> result = ComponentDAO.getAllAvailableComponentIds(con, groupIds, userId);
      assertNotNull(result);
      assertEquals(17, result.size());
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Test of getAllAvailableComponentIds method, of class ComponentDAO.
   * @throws Exception
   */
  @Test
  public void testGetAllAvailableComponentIdsForUserAndGroups() throws Exception {
    Connection con = null;
    try {
      con = getConnection().getConnection();
      List<String> groupIds = new ArrayList<String>();
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
      assertThat(result, containsInAnyOrder("kmelia9"));
    } finally {
      DBUtil.close(con);
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
      assertThat(result, containsInAnyOrder("blog10", "kmelia11"));
      assertThat(result, contains("blog10", "kmelia11"));

      spaceId = 2;
      result = ComponentDAO.getComponentIdsInSpace(con, spaceId);
      assertNotNull(result);
      assertEquals("Subspace components should not be present", 2, result.size());
      assertThat(result, containsInAnyOrder("questionReply12", "yellowpages19"));
      assertThat(result, contains("questionReply12", "yellowpages19"));
    } finally {
      DBUtil.close(con);
    }
  }


  @Override
  protected String getTableCreationFileName() {
    return "create-database.sql";
  }
}