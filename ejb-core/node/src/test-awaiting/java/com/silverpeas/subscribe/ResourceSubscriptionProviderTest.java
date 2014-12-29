/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package com.silverpeas.subscribe;

import com.silverpeas.subscribe.constant.SubscriptionResourceType;
import com.silverpeas.subscribe.service.AbstractResourceSubscriptionService;
import com.silverpeas.subscribe.service.GroupSubscriptionSubscriber;
import com.silverpeas.subscribe.service.ResourceSubscriptionProvider;
import com.silverpeas.subscribe.service.UserSubscriptionSubscriber;
import com.silverpeas.subscribe.util.SubscriptionSubscriberList;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class ResourceSubscriptionProviderTest extends AbstractCommonSubscriptionTest {

  @Before
  public void initializations() {
    initializeUsersAndGroups();
    when(getMockedOrganizationController().getComponentInstLight(anyString()))
        .thenAnswer(new Answer<ComponentInstLight>() {
          @Override
          public ComponentInstLight answer(final InvocationOnMock invocation) throws Throwable {
            String componentId = (String) invocation.getArguments()[0];
            ComponentInstLight componentInstLight = new ComponentInstLight();
            componentInstLight.setId(componentId);
            componentInstLight.setName(componentId.replaceAll("[0-9]", ""));
            return componentInstLight;
          }
        });
  }

  @After
  public void cleanAfterTest() throws Exception {
    Map map = (Map) FieldUtils
        .readDeclaredStaticField(ResourceSubscriptionProvider.class, "componentImplementations",
            true);
    map.clear();
  }

  /**
   * Test of getSubscribersOfComponent method, of interface SubscriptionService.
   */
  @Test
  public void testGetSubscribersFromDefaultImplementation() {
    SubscriptionSubscriberList result =
        ResourceSubscriptionProvider.getSubscribersOfComponent(INSTANCE_ID);
    assertThat(result, hasSize(6));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("1")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("3")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("5")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("1")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("3")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from(GROUPID_WITH_ONE_USER)));

    assertThat(result.getAllUserIds(),
        containsInAnyOrder("1", "3", "5", USERID_OF_GROUP_WITH_ONE_USER));
  }

  /**
   * Test of getSubscribersOfComponent method, of interface SubscriptionService.
   */
  @Test
  public void testGetSubscribersFromComponentImplementation() {
    registerStubbedKmeliaImplementation();
    SubscriptionSubscriberList result =
        ResourceSubscriptionProvider.getSubscribersOfComponent(INSTANCE_ID);
    assertThat(result, hasSize(1));
    assertThat(result, hasItem(
        UserSubscriptionSubscriber.from("userIdFromTestKmeliaResourceSubscriptionService")));

    assertThat(result.getAllUserIds(), contains("userIdFromTestKmeliaResourceSubscriptionService"));
  }

  private void registerStubbedKmeliaImplementation() {
    ResourceSubscriptionProvider
        .registerResourceSubscriptionService(new TestKmeliaResourceSubscriptionService());
  }

  private static class TestKmeliaResourceSubscriptionService
      extends AbstractResourceSubscriptionService {
    @Override
    protected String getHandledComponentName() {
      return "kmelia";
    }

    @Override
    public SubscriptionSubscriberList getSubscribersOfComponentAndTypedResource(
        final String componentInstanceId, final SubscriptionResourceType resourceType,
        final String resourceId) {
      SubscriptionSubscriberList result = new SubscriptionSubscriberList();
      result
          .add(UserSubscriptionSubscriber.from("userIdFromTestKmeliaResourceSubscriptionService"));
      return result;
    }
  }
}