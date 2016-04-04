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
package org.silverpeas.core.subscription;

import org.silverpeas.core.subscription.constant.SubscriptionResourceType;
import org.silverpeas.core.subscription.service.AbstractResourceSubscriptionService;
import org.silverpeas.core.subscription.service.GroupSubscriptionSubscriber;
import org.silverpeas.core.subscription.service.ResourceSubscriptionProvider;
import org.silverpeas.core.subscription.service.UserSubscriptionSubscriber;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberList;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class ResourceSubscriptionProviderTest extends AbstractCommonSubscriptionIntegrationTest {

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