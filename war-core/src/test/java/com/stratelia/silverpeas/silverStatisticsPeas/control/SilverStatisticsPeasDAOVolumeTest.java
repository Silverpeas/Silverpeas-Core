/*
 * Copyright (C) 2000 - 2011 Silverpeas
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version
 * 3 of the License, or (at your option) any later version.
 * <p/>
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of
 * the text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.com/legal/licensing"
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 */
package com.stratelia.silverpeas.silverStatisticsPeas.control;

import com.silverpeas.components.model.AbstractTestDao;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import java.util.Collection;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author ehugonnet
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-jdbc-datasource.xml"})
public class SilverStatisticsPeasDAOVolumeTest extends AbstractTestDao {

  private static final String dateBegin = "2010-12-01";
  private static final String dateEnd = "2011-07-01";

  @Override
  protected String getDatasetFileName() {
    return "test-stats-volume-dataset.xml";
  }

  /**
   * Test of getYears method, of class SilverStatisticsPeasDAOAccesVolume.
   */
  @Test
  public void testGetVolumeYears() throws Exception {
    Collection<String> result = SilverStatisticsPeasDAOAccesVolume.getVolumeYears();
    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(1));
    assertThat(result, hasItem("2011"));
  }

  /**
   * Test of getStatsPublicationsVentil method, of class SilverStatisticsPeasDAOAccesVolume.
   */
  @Test
  public void testGetStatsPublicationsVentilWithUserId() throws Exception {
    AdminController oldController = SilverStatisticsPeasDAOAccesVolume.myAdminController;
    try {
      String userId = "1";
      String currentUserId = "1";
      AdminController controller = prepareAdminController(currentUserId);
      SilverStatisticsPeasDAOAccesVolume.myAdminController = controller;
      Map<String, String[]> result = SilverStatisticsPeasDAOAccesVolume.getStatsPublicationsVentil(
              "2011-01-01", currentUserId, null, userId);
      verify(controller).getUserManageableSpaceClientIds(currentUserId);
      assertThat(result, is(notNullValue()));
      assertThat(result.size(), is(1));
      assertThat(result, hasKey("kmelia26"));
      String[] data = result.get("kmelia26");
      assertThat(data.length, is(3));
      assertThat(data, arrayContaining("223", null, "34"));

      userId = "5";
      result = SilverStatisticsPeasDAOAccesVolume.getStatsPublicationsVentil("2011-01-01",
              currentUserId, null, userId);
      verify(controller, times(2)).getUserManageableSpaceClientIds(currentUserId);
      assertThat(result, is(notNullValue()));
      assertThat(result.size(), is(1));
      assertThat(result, hasKey("kmelia26"));
      data = result.get("kmelia26");
      assertThat(data.length, is(3));
      assertThat(data, arrayContaining("223", null, "16"));
    } finally {
      SilverStatisticsPeasDAOAccesVolume.myAdminController = oldController;
    }
  }

  /**
   * Test of getStatsPublicationsVentil method, of class SilverStatisticsPeasDAOAccesVolume.
   */
  @Test
  public void testGetStatsPublicationsVentilWithGroupId() throws Exception {
    AdminController oldController = SilverStatisticsPeasDAOAccesVolume.myAdminController;
    try {
      String groupId = "1";
      String currentUserId = "1";
      AdminController controller = prepareAdminController(currentUserId);
      SilverStatisticsPeasDAOAccesVolume.myAdminController = controller;
      Map<String, String[]> result = SilverStatisticsPeasDAOAccesVolume.getStatsPublicationsVentil(
              "2011-01-01", currentUserId, groupId, null);
      verify(controller).getUserManageableSpaceClientIds(currentUserId);
      assertThat(result, is(notNullValue()));
      assertThat(result.size(), is(1));
      assertThat(result, hasKey("kmelia26"));
      String[] data = result.get("kmelia26");
      assertThat(data.length, is(3));
      assertThat(data, arrayContaining("223", "34", null));


      groupId = "2";
      result = SilverStatisticsPeasDAOAccesVolume.getStatsPublicationsVentil("2011-01-01",
              currentUserId, groupId, null);
      verify(controller, times(2)).getUserManageableSpaceClientIds(currentUserId);
      assertThat(result, is(notNullValue()));
      assertThat(result.size(), is(1));
      assertThat(result, hasKey("kmelia26"));
      data = result.get("kmelia26");
      assertThat(data.length, is(3));
      assertThat(data, arrayContaining("223", "110", null));
    } finally {
      SilverStatisticsPeasDAOAccesVolume.myAdminController = oldController;
    }
  }

  /**
   * Test of getStatsPublicationsVentil method, of class SilverStatisticsPeasDAOAccesVolume.
   */
  @Test
  public void testGetStatsPublicationsVentilWithGroupIdAndUserId() throws Exception {
    AdminController oldController = SilverStatisticsPeasDAOAccesVolume.myAdminController;
    try {
      String groupId = "2";
      String userId = "5";
      String currentUserId = "1";
      AdminController controller = prepareAdminController(currentUserId);
      SilverStatisticsPeasDAOAccesVolume.myAdminController = controller;
      Map<String, String[]> result = SilverStatisticsPeasDAOAccesVolume.getStatsPublicationsVentil(
              "2011-01-01", currentUserId, groupId, userId);
      verify(controller).getUserManageableSpaceClientIds(currentUserId);
      assertThat(result, is(notNullValue()));
      assertThat(result.size(), is(1));
      assertThat(result, hasKey("kmelia26"));
      String[] data = result.get("kmelia26");
      assertThat(data.length, is(3));
      assertThat(data, arrayContaining("223", "110", "16"));
    } finally {
      SilverStatisticsPeasDAOAccesVolume.myAdminController = oldController;
    }
  }

  /**
   * Test of getStatsPublicationsVentil method, of class SilverStatisticsPeasDAOAccesVolume.
   */
  @Test
  public void testGetStatsPublicationsVentilWithAnUnauthorizedUser() throws Exception {
    AdminController oldController = SilverStatisticsPeasDAOAccesVolume.myAdminController;
    try {
      String groupId = "2";
      String userId = "5";
      String currentUserId = "1";
      AdminController controller = prepareAdminController(currentUserId);
      when(controller.getUserManageableSpaceClientIds(currentUserId)).thenReturn(new String[]{
                "WA100"});
      SilverStatisticsPeasDAOAccesVolume.myAdminController = controller;
      Map<String, String[]> result = SilverStatisticsPeasDAOAccesVolume.getStatsPublicationsVentil(
              "2011-01-01", currentUserId, groupId, userId);
      verify(controller).getUserManageableSpaceClientIds(currentUserId);
      assertThat(result, is(notNullValue()));
      assertThat(result.size(), is(0));
    } finally {
      SilverStatisticsPeasDAOAccesVolume.myAdminController = oldController;
    }
  }

  private AdminController prepareAdminController(String currentUserId) {
    AdminController controller = mock(AdminController.class);
    ComponentInst kmelia26 = new ComponentInst();
    kmelia26.setDomainFatherId("WA18");
    when(controller.getComponentInst("kmelia26")).thenReturn(kmelia26);
    when(controller.getUserManageableSpaceClientIds(currentUserId)).thenReturn(new String[]{"WA18"});
    UserDetail user1 = new UserDetail();
    user1.setId("1");
    UserDetail user2 = new UserDetail();
    user2.setId("2");
    UserDetail user3 = new UserDetail();
    user3.setId("3");
    when(controller.getAllUsersOfGroup("1")).thenReturn(new UserDetail[]{user1});
    when(controller.getAllUsersOfGroup("2")).thenReturn(new UserDetail[]{user1, user2, user3});
    return controller;
  }
}
