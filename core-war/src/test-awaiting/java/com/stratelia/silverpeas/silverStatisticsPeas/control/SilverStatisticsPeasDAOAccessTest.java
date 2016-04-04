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

package com.stratelia.silverpeas.silverStatisticsPeas.control;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.UserDetail;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 *
 * @author ehugonnet
 */
public class SilverStatisticsPeasDAOAccessTest extends AbstractSpringDatasourceTest {

  private static final String dateForTest = "2011-02-01";

  @Override
  public String getDatasetFileName() {
    return "test-stats-access-dataset.xml";
  }

  /**
   * Test of getYears method, of class SilverStatisticsPeasDAOAccesVolume.
   */
  @Test
  public void testGetVolumeYears() throws Exception {
    Collection<String> result = SilverStatisticsPeasDAOAccesVolume.getAccessYears();
    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(2));
    assertThat(result, hasItem("2010"));
    assertThat(result, hasItem("2011"));
  }

  /**
   * Test of getStatsUserVentil method, of class SilverStatisticsPeasDAOAccesVolume.
   */
  @Test
  public void testGetStatsUserVentilWithUserId() throws Exception {
    AdminController oldController = SilverStatisticsPeasDAOAccesVolume.myAdminController;
    try {
      String userId = "1";
      String currentUserId = "1";
      AdminController controller = prepareAdminController(currentUserId);
      SilverStatisticsPeasDAOAccesVolume.myAdminController = controller;
      Map<String, String[]> result = SilverStatisticsPeasDAOAccesVolume.getStatsUserVentil(
              dateForTest, currentUserId, null, userId);
      verify(controller, times(3)).getUserManageableSpaceClientIds(currentUserId);
      assertThat(result, is(notNullValue()));
      assertThat(result.size(), is(3));
      assertThat(result, hasKey("kmelia26"));
      assertThat(result, hasKey("kmelia38"));
      assertThat(result, hasKey("kmelia40"));
      String[] data = result.get("kmelia26");
      assertThat(data.length, is(3));
      assertThat(data, arrayContaining("129", null, "10"));
      data = result.get("kmelia38");
      assertThat(data.length, is(3));
      assertThat(data, arrayContaining("8", null, "8"));
      data = result.get("kmelia40");
      assertThat(data.length, is(3));
      assertThat(data, arrayContaining("49", null, "49"));

      userId = "5";
      result = SilverStatisticsPeasDAOAccesVolume.getStatsUserVentil(dateForTest, currentUserId,
              null, userId);
      verify(controller, times(6)).getUserManageableSpaceClientIds(currentUserId);
      assertThat(result, is(notNullValue()));
      assertThat(result.size(), is(3));
      assertThat(result, hasKey("kmelia26"));
      assertThat(result, hasKey("kmelia38"));
      assertThat(result, hasKey("kmelia40"));
      data = result.get("kmelia26");
      assertThat(data.length, is(3));
      assertThat(data, arrayContaining("129", null, "5"));
      data = result.get("kmelia38");
      assertThat(data.length, is(3));
      assertThat(data, arrayContaining("8", null, null));
      data = result.get("kmelia40");
      assertThat(data.length, is(3));
      assertThat(data, arrayContaining("49", null, null));
    } finally {
      SilverStatisticsPeasDAOAccesVolume.myAdminController = oldController;
    }
  }

  /**
   * Test of getStatsUserVentil method, of class SilverStatisticsPeasDAOAccesVolume.
   */
  @Test
  public void testGetStatsUserVentilWithGroupId() throws Exception {
    AdminController oldController = SilverStatisticsPeasDAOAccesVolume.myAdminController;
    try {
      String groupId = "1";
      String currentUserId = "1";
      AdminController controller = prepareAdminController(currentUserId);
      SilverStatisticsPeasDAOAccesVolume.myAdminController = controller;
      Map<String, String[]> result = SilverStatisticsPeasDAOAccesVolume.getStatsUserVentil(
              dateForTest, currentUserId, groupId, null);
      verify(controller, times(3)).getUserManageableSpaceClientIds(currentUserId);
      assertThat(result, is(notNullValue()));
      assertThat(result.size(), is(3));
      assertThat(result, hasKey("kmelia26"));
      assertThat(result, hasKey("kmelia38"));
      assertThat(result, hasKey("kmelia40"));
      String[] data = result.get("kmelia26");
      assertThat(data.length, is(3));
      assertThat(data, arrayContaining("129", "10", null));
      data = result.get("kmelia38");
      assertThat(data.length, is(3));
      assertThat(data, arrayContaining("8", "8", null));
      data = result.get("kmelia40");
      assertThat(data.length, is(3));
      assertThat(data, arrayContaining("49", "49", null));


      groupId = "2";
      result = SilverStatisticsPeasDAOAccesVolume.getStatsUserVentil(dateForTest, currentUserId,
              groupId, null);
      verify(controller, times(6)).getUserManageableSpaceClientIds(currentUserId);
      assertThat(result, is(notNullValue()));
      assertThat(result.size(), is(3));
      assertThat(result, hasKey("kmelia26"));
      assertThat(result, hasKey("kmelia38"));
      assertThat(result, hasKey("kmelia40"));
      data = result.get("kmelia26");
      assertThat(data.length, is(3));
      assertThat(data, arrayContaining("129", "67", null));
      data = result.get("kmelia38");
      assertThat(data.length, is(3));
      assertThat(data, arrayContaining("8", "8", null));
      data = result.get("kmelia40");
      assertThat(data.length, is(3));
      assertThat(data, arrayContaining("49", "49", null));
    } finally {
      SilverStatisticsPeasDAOAccesVolume.myAdminController = oldController;
    }
  }

  /**
   * Test of getStatsUserVentil method, of class SilverStatisticsPeasDAOAccesVolume.
   */
  @Test
  public void testGetStatsUserVentilWithGroupIdAndUserId() throws Exception {
    AdminController oldController = SilverStatisticsPeasDAOAccesVolume.myAdminController;
    try {
      String groupId = "2";
      String userId = "5";
      String currentUserId = "1";
      AdminController controller = prepareAdminController(currentUserId);
      SilverStatisticsPeasDAOAccesVolume.myAdminController = controller;
      Map<String, String[]> result = SilverStatisticsPeasDAOAccesVolume.getStatsUserVentil(
              dateForTest, currentUserId, groupId, userId);
      verify(controller, times(3)).getUserManageableSpaceClientIds(currentUserId);
      assertThat(result, is(notNullValue()));
      assertThat(result.size(), is(3));
      assertThat(result, hasKey("kmelia26"));
      assertThat(result, hasKey("kmelia38"));
      assertThat(result, hasKey("kmelia40"));
      String[] data = result.get("kmelia26");
      assertThat(data.length, is(3));
      assertThat(data, arrayContaining("129", "67", "5"));
      data = result.get("kmelia38");
      assertThat(data.length, is(3));
      assertThat(data, arrayContaining("8", "8", null));
      data = result.get("kmelia40");
      assertThat(data.length, is(3));
      assertThat(data, arrayContaining("49", "49", null));
    } finally {
      SilverStatisticsPeasDAOAccesVolume.myAdminController = oldController;
    }
  }

  /**
   * Test of getStatsUserVentil method, of class SilverStatisticsPeasDAOAccesVolume.
   */
  @Test
  public void testGetStatsUserVentilWithAnUnauthorizedUser() throws Exception {
    AdminController oldController = SilverStatisticsPeasDAOAccesVolume.myAdminController;
    try {
      String groupId = "2";
      String userId = "5";
      String currentUserId = "1";
      AdminController controller = prepareAdminController(currentUserId);
      when(controller.getUserManageableSpaceClientIds(currentUserId)).thenReturn(new String[]{
                "WA100"});
      SilverStatisticsPeasDAOAccesVolume.myAdminController = controller;
      Map<String, String[]> result = SilverStatisticsPeasDAOAccesVolume.getStatsUserVentil(
              dateForTest, currentUserId, groupId, userId);
      verify(controller, times(3)).getUserManageableSpaceClientIds(currentUserId);
      assertThat(result, is(notNullValue()));
      assertThat(result.size(), is(0));
    } finally {
      SilverStatisticsPeasDAOAccesVolume.myAdminController = oldController;
    }
  }

  @Test
  public void testGetStatisticsEvolutionForUserOnSpace() throws Exception {
    String groupId = "2";
    String userId = "5";
    String entityId = "WA18";
    List<String[]> result = SilverStatisticsPeasDAOAccesVolume.getStatsUserEvolution("SPACE",
            entityId, groupId, userId);
    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(8));
    String[] stats = result.get(3);
    assertThat(stats[0], is("2011-02-01"));
    assertThat(stats[1], is("5"));
  }

  @Test
  public void testGetStatisticsEvolutionForGroupOnSpace() throws Exception {
    String groupId = "1";
    String entityId = "WA18";
    List<String[]> result = SilverStatisticsPeasDAOAccesVolume.getStatsUserEvolution("SPACE",
            entityId, groupId, null);
    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(8));
    String[] stats = result.get(3);
    assertThat(stats[0], is("2011-02-01"));
    assertThat(stats[1], is("137"));

    groupId = "3";
    result = SilverStatisticsPeasDAOAccesVolume.getStatsUserEvolution("SPACE", entityId, groupId,
            null);
    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(8));
    stats = result.get(3);
    assertThat(stats[0], is("2011-02-01"));
    assertThat(stats[1], is("62"));
  }

  @Test
  public void testGetStatisticsEvolutionOnComponent() throws Exception {
    String entityId = "kmelia26";
    List<String[]> result = SilverStatisticsPeasDAOAccesVolume.getStatsUserEvolution("",
            entityId, null, null);
    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(8));
    String[] stats = result.get(3);
    assertThat(stats[0], is("2011-02-01"));
    assertThat(stats[1], is("129"));
  }

  @Test
  public void testGetStatisticsEvolutionForUserOnComponent() throws Exception {
    String groupId = "2";
    String userId = "5";
    String entityId = "kmelia26";
    List<String[]> result = SilverStatisticsPeasDAOAccesVolume.getStatsUserEvolution("",
            entityId, groupId, userId);
    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(8));
    String[] stats = result.get(3);
    assertThat(stats[0], is("2011-02-01"));
    assertThat(stats[1], is("5"));
  }

  @Test
  public void testGetStatisticsEvolutionForGroupOnComponent() throws Exception {
    String groupId = "1";
    String entityId = "kmelia26";
    List<String[]> result = SilverStatisticsPeasDAOAccesVolume.getStatsUserEvolution("",
            entityId, groupId, null);
    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(8));
    String[] stats = result.get(3);
    assertThat(stats[0], is("2011-02-01"));
    assertThat(stats[1], is("129"));

    groupId = "3";
    result = SilverStatisticsPeasDAOAccesVolume.getStatsUserEvolution("", entityId, groupId, null);
    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(8));
    stats = result.get(3);
    assertThat(stats[0], is("2011-02-01"));
    assertThat(stats[1], is("62"));
  }

  @Test
  public void testGetStatisticsEvolutionOnSpace() throws Exception {
    String entityId = "WA18";
    List<String[]> result = SilverStatisticsPeasDAOAccesVolume.getStatsUserEvolution("SPACE",
            entityId, null, null);
    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(8));
    String[] stats = result.get(3);
    assertThat(stats[0], is("2011-02-01"));
    assertThat(stats[1], is("137"));
  }

  private AdminController prepareAdminController(String currentUserId) {
    AdminController controller = mock(AdminController.class);
    ComponentInst kmelia26 = new ComponentInst();
    kmelia26.setDomainFatherId("WA18");
    when(controller.getComponentInst("kmelia26")).thenReturn(kmelia26);
    ComponentInst kmelia38 = new ComponentInst();
    kmelia38.setDomainFatherId("WA18");
    when(controller.getComponentInst("kmelia38")).thenReturn(kmelia38);
    when(controller.getUserManageableSpaceClientIds(currentUserId)).thenReturn(new String[]{"WA18",
              "WA19"});
    ComponentInst kmelia40 = new ComponentInst();
    kmelia40.setDomainFatherId("WA19");
    when(controller.getComponentInst("kmelia40")).thenReturn(kmelia40);
    when(controller.getUserManageableSpaceClientIds(currentUserId)).thenReturn(new String[]{"WA18",
              "WA19"});
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
