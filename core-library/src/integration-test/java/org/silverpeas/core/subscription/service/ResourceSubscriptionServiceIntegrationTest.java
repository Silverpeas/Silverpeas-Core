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
package org.silverpeas.core.subscription.service;

import org.silverpeas.core.subscription.AbstractCommonSubscriptionIntegrationTest;
import org.silverpeas.core.subscription.constant.SubscriptionResourceType;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberList;
import org.silverpeas.core.node.model.NodePK;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class ResourceSubscriptionServiceIntegrationTest extends
    AbstractCommonSubscriptionIntegrationTest {

  /**
   * Test of getSubscribersOfComponent method, of interface SubscriptionService.
   */
  @Test
  public void testGetSubscribersForComponentResourceAndExtractUserIds() {
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
   * Test of getSubscribersOfComponentAndTypedResource method, of interface SubscriptionService.
   */
  @Test
  public void testGetSubscribersForNodeResourcesWithId0AndExtractUserIds() {
    SubscriptionSubscriberList result = ResourceSubscriptionProvider
        .getSubscribersOfComponentAndTypedResource(INSTANCE_ID, SubscriptionResourceType.NODE, "0");
    assertThat(result, hasSize(10));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("1")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("2")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("3")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("4")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("5")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("1")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("2")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("3")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("4")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from(GROUPID_WITH_ONE_USER)));

    assertThat(result.getAllUserIds(),
        containsInAnyOrder("1", "2", "3", "4", "5", USERID_OF_GROUP_WITH_ONE_USER));
  }

  /**
   * Test of getSubscribersOfComponentAndTypedResource method, of interface SubscriptionService.
   */
  @Test
  public void testGetSubscribersForNodeResourcesWithId0AndExtractUserIdsOtherMethod() {
    SubscriptionSubscriberList result = ResourceSubscriptionProvider
        .getSubscribersOfSubscriptionResource(
            NodeSubscriptionResource.from(new NodePK("0", INSTANCE_ID)));
    assertThat(result, hasSize(10));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("1")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("2")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("3")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("4")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("5")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("1")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("2")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("3")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("4")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from(GROUPID_WITH_ONE_USER)));

    assertThat(result.getAllUserIds(),
        containsInAnyOrder("1", "2", "3", "4", "5", USERID_OF_GROUP_WITH_ONE_USER));
  }

  /**
   * Test of getSubscribersOfComponentAndTypedResource method, of interface SubscriptionService.
   */
  @Test
  public void testGetSubscribersForNodeResourcesWithId10AndExtractUserIds() {
    SubscriptionSubscriberList result = ResourceSubscriptionProvider
        .getSubscribersOfComponentAndTypedResource(INSTANCE_ID, SubscriptionResourceType.NODE,
            "10");
    assertThat(result, hasSize(10));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("1")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("2")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("3")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("4")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("5")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("1")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("2")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("3")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("4")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from(GROUPID_WITH_ONE_USER)));

    assertThat(result.getAllUserIds(),
        containsInAnyOrder("1", "2", "3", "4", "5", USERID_OF_GROUP_WITH_ONE_USER));
  }
}