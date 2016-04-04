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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.silverpeas.silverStatisticsPeas.control;

import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.cache.service.InMemoryCacheService;
import org.silverpeas.web.test.WarBuilder4WarCore;
import org.silverpeas.core.test.rule.DbUnitLoadingRule;

import java.util.Collection;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author ehugonnet
 */
@RunWith(Arquillian.class)
public class SilverStatisticsPeasDAOVolumeTest {

  private static final String dateBegin = "2010-12-01";
  private static final String dateEnd = "2011-07-01";

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule("create-database.sql", "test-stats-volume-dataset.xml");

  @Before
  public void generalSetUp() throws Exception {
    InMemoryCacheService cache = new InMemoryCacheService();
    UserDetail user = new UserDetail();
    user.setId("1");
    cache.put(UserDetail.CURRENT_REQUESTER_KEY, user);
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4WarCore.onWarForTestClass(SilverStatisticsPeasDAOConnexionTest.class)
        .testFocusedOn(warBuilder -> {
          warBuilder.addPackages(true, "com.stratelia.silverpeas.silverStatisticsPeas");
        }).build();
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
    String userId = "1";
    String currentUserId = "1";
    AdminController controller = prepareAdminController(currentUserId);
    Map<String, String[]> result =
        SilverStatisticsPeasDAOAccesVolume.getStatsPublicationsVentil("2011-01-01", null, userId);
    verify(controller).getUserManageableSpaceClientIds(currentUserId);
    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(1));
    assertThat(result, hasKey("kmelia26"));
    String[] data = result.get("kmelia26");
    assertThat(data.length, is(3));
    assertThat(data, arrayContaining("223", null, "34"));

    userId = "5";
    result =
        SilverStatisticsPeasDAOAccesVolume.getStatsPublicationsVentil("2011-01-01", null, userId);
    verify(controller, times(2)).getUserManageableSpaceClientIds(currentUserId);
    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(1));
    assertThat(result, hasKey("kmelia26"));
    data = result.get("kmelia26");
    assertThat(data.length, is(3));
    assertThat(data, arrayContaining("223", null, "16"));
  }

  /**
   * Test of getStatsPublicationsVentil method, of class SilverStatisticsPeasDAOAccesVolume.
   */
  @Test
  public void testGetStatsPublicationsVentilWithGroupId() throws Exception {
    String groupId = "1";
    String currentUserId = "1";
    AdminController controller = prepareAdminController(currentUserId);
    Map<String, String[]> result =
        SilverStatisticsPeasDAOAccesVolume.getStatsPublicationsVentil("2011-01-01", groupId, null);
    verify(controller).getUserManageableSpaceClientIds(currentUserId);
    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(1));
    assertThat(result, hasKey("kmelia26"));
    String[] data = result.get("kmelia26");
    assertThat(data.length, is(3));
    assertThat(data, arrayContaining("223", "34", null));


    groupId = "2";
    result =
        SilverStatisticsPeasDAOAccesVolume.getStatsPublicationsVentil("2011-01-01", groupId, null);
    verify(controller, times(2)).getUserManageableSpaceClientIds(currentUserId);
    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(1));
    assertThat(result, hasKey("kmelia26"));
    data = result.get("kmelia26");
    assertThat(data.length, is(3));
    assertThat(data, arrayContaining("223", "110", null));
  }

  /**
   * Test of getStatsPublicationsVentil method, of class SilverStatisticsPeasDAOAccesVolume.
   */
  @Test
  public void testGetStatsPublicationsVentilWithGroupIdAndUserId() throws Exception {
    String groupId = "2";
    String userId = "5";
    String currentUserId = "1";
    AdminController controller = prepareAdminController(currentUserId);
    Map<String, String[]> result = SilverStatisticsPeasDAOAccesVolume
        .getStatsPublicationsVentil("2011-01-01", groupId, userId);
    verify(controller).getUserManageableSpaceClientIds(currentUserId);
    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(1));
    assertThat(result, hasKey("kmelia26"));
    String[] data = result.get("kmelia26");
    assertThat(data.length, is(3));
    assertThat(data, arrayContaining("223", "110", "16"));
  }

  /**
   * Test of getStatsPublicationsVentil method, of class SilverStatisticsPeasDAOAccesVolume.
   */
  @Test
  public void testGetStatsPublicationsVentilWithAnUnauthorizedUser() throws Exception {
    String groupId = "2";
    String userId = "5";
    String currentUserId = "1";
    AdminController controller = prepareAdminController(currentUserId);
    when(controller.getUserManageableSpaceClientIds(currentUserId))
        .thenReturn(new String[]{"WA100"});
    Map<String, String[]> result = SilverStatisticsPeasDAOAccesVolume
        .getStatsPublicationsVentil("2011-01-01", groupId, userId);
    verify(controller).getUserManageableSpaceClientIds(currentUserId);
    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(0));
  }

  private AdminController prepareAdminController(String currentUserId) {
    AdminController controller = mock(AdminController.class);
    ComponentInst kmelia26 = new ComponentInst();
    kmelia26.setDomainFatherId("WA18");
    when(controller.getComponentInst("kmelia26")).thenReturn(kmelia26);
    when(controller.getUserManageableSpaceClientIds(currentUserId))
        .thenReturn(new String[]{"WA18"});
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
