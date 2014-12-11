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
package com.silverpeas.subscribe.util;

import com.silverpeas.subscribe.AbstractCommonSubscriptionTest;
import com.silverpeas.subscribe.ResourceSubscriptionService;
import com.silverpeas.subscribe.SubscriptionService;
import com.silverpeas.subscribe.SubscriptionSubscriber;
import com.silverpeas.subscribe.constant.SubscriberType;
import com.silverpeas.subscribe.service.GroupSubscriptionSubscriber;
import com.silverpeas.subscribe.service.UserSubscriptionSubscriber;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class SubscriptionSubscriberListTest extends AbstractCommonSubscriptionTest {

  @Before
  public void initializations() {
    initializeUsersAndGroups();
  }

  @Test
  public void addLimitCases() {
    SubscriptionSubscriberList result = new SubscriptionSubscriberList();
    result.add(null);
    assertThat(result, hasSize(1));
    assertThat(result.get(0), nullValue());
  }

  @SuppressWarnings("ConstantConditions")
  @Test(expected = NullPointerException.class)
  public void addAllLimitCases() {
    new SubscriptionSubscriberList().addAll(null);
  }

  @Test
  public void getAllIds() {
    SubscriptionSubscriberList result =
        getTestedDefaultService().getSubscribersOfComponent(INSTANCE_ID);
    assertThat(result, hasSize(6));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("1")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("3")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("5")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("1")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("3")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from(GROUPID_WITH_ONE_USER)));

    assertThat(result.getAllIds(), containsInAnyOrder("1", "3", "5", GROUPID_WITH_ONE_USER));
  }

  @Test
  public void getAllUserIds() {
    SubscriptionSubscriberList result =
        getTestedDefaultService().getSubscribersOfComponent(INSTANCE_ID);
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

  @Test
  public void indexBySubscriberType() {
    SubscriptionSubscriberList result =
        getTestedDefaultService().getSubscribersOfComponent(INSTANCE_ID);
    assertThat(result, hasSize(6));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("1")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("3")));
    assertThat(result, hasItem(UserSubscriptionSubscriber.from("5")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("1")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from("3")));
    assertThat(result, hasItem(GroupSubscriptionSubscriber.from(GROUPID_WITH_ONE_USER)));

    SubscriptionSubscriberMapBySubscriberType indexedSubscribers = result.indexBySubscriberType();
    assertThat(indexedSubscribers.keySet(),
        containsInAnyOrder(SubscriberType.USER, SubscriberType.GROUP));
  }

  protected ResourceSubscriptionService getTestedDefaultService() {
    return getBeanFromContext(ResourceSubscriptionService.class);
  }
}