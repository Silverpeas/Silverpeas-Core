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
package org.silverpeas.core.subscription.util;

import org.silverpeas.core.subscription.AbstractCommonSubscriptionIntegrationTest;
import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.subscription.constant.SubscriberType;
import org.silverpeas.core.subscription.service.GroupSubscriptionSubscriber;
import org.silverpeas.core.subscription.service.UserSubscriptionSubscriber;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.HashSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Yohann Chastagnier
 */
@RunWith(Arquillian.class)
public class SubscriptionSubscriberMapBySubscriberTypeIntegrationTest
    extends AbstractCommonSubscriptionIntegrationTest {

  @Test
  public void emptyMap() {
    assertThat(new SubscriptionSubscriberMapBySubscriberType().keySet(),
        hasItems(validSubscriberTypes));
  }

  @Test
  public void addLimitCases() {
    SubscriptionSubscriberMapBySubscriberType indexedSubscribers =
        new SubscriptionSubscriberMapBySubscriberType();
    indexedSubscribers.add(null);
    assertThat(indexedSubscribers.get(SubscriberType.USER), hasSize(0));
    assertThat(indexedSubscribers.get(SubscriberType.GROUP), hasSize(0));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void addAllLimitCases() {
    SubscriptionSubscriberMapBySubscriberType indexedSubscribers =
        new SubscriptionSubscriberMapBySubscriberType();
    indexedSubscribers.addAll((Collection) null);
    indexedSubscribers.addAll((SubscriptionSubscriberMapBySubscriberType) null);
    assertThat(indexedSubscribers.get(SubscriberType.USER), hasSize(0));
    assertThat(indexedSubscribers.get(SubscriberType.GROUP), hasSize(0));
  }

  @Test
  public void addUserSubscriptionSubscribers() {
    SubscriptionSubscriberMapBySubscriberType indexedSubscribers =
        new SubscriptionSubscriberMapBySubscriberType();
    indexedSubscribers.addAll(prepareUserSubscriptionSubscribers("1"));
    assertThat(indexedSubscribers.get(SubscriberType.USER).getAllUserIds(), contains("1"));
    assertThat(indexedSubscribers.get(SubscriberType.GROUP), hasSize(0));
    assertThat(indexedSubscribers.get(SubscriberType.GROUP).getAllUserIds(), empty());
    assertThat(indexedSubscribers.getAllUserIds(), contains("1"));
  }

  @Test
  public void addGroupSubscriptionSubscribersThatContainsNoUser() {
    SubscriptionSubscriberMapBySubscriberType indexedSubscribers =
        new SubscriptionSubscriberMapBySubscriberType();
    indexedSubscribers.addAll(prepareGroupSubscriptionSubscribers("1"));
    assertThat(indexedSubscribers.get(SubscriberType.USER).getAllUserIds(), empty());
    assertThat(indexedSubscribers.get(SubscriberType.GROUP), hasSize(1));
    assertThat(indexedSubscribers.get(SubscriberType.GROUP).getAllUserIds(), empty());
    assertThat(indexedSubscribers.getAllUserIds(), empty());
  }

  @Test
  public void addGroupSubscriptionSubscribersThatContainsOneUser() {
    SubscriptionSubscriberMapBySubscriberType indexedSubscribers =
        new SubscriptionSubscriberMapBySubscriberType();
    indexedSubscribers.addAll(prepareGroupSubscriptionSubscribers(GROUPID_WITH_ONE_USER));
    assertThat(indexedSubscribers.get(SubscriberType.USER).getAllUserIds(), empty());
    assertThat(indexedSubscribers.get(SubscriberType.GROUP), hasSize(1));
    assertThat(indexedSubscribers.get(SubscriberType.GROUP).getAllUserIds(),
        contains(USERID_OF_GROUP_WITH_ONE_USER));
    assertThat(indexedSubscribers.getAllUserIds(), contains(USERID_OF_GROUP_WITH_ONE_USER));
  }

  @Test
  public void addUserAndGroupSubscriptionSubscribers() {
    SubscriptionSubscriberMapBySubscriberType indexedSubscribers =
        new SubscriptionSubscriberMapBySubscriberType();
    indexedSubscribers.addAll(prepareUserSubscriptionSubscribers("1", "26"));
    indexedSubscribers.addAll(prepareGroupSubscriptionSubscribers(GROUPID_WITH_ONE_USER));
    assertThat(indexedSubscribers.get(SubscriberType.USER).getAllUserIds(),
        containsInAnyOrder("1", "26"));
    assertThat(indexedSubscribers.get(SubscriberType.GROUP), hasSize(1));
    assertThat(indexedSubscribers.get(SubscriberType.GROUP).getAllUserIds(),
        contains(USERID_OF_GROUP_WITH_ONE_USER));
    assertThat(indexedSubscribers.getAllUserIds(),
        containsInAnyOrder("1", "26", USERID_OF_GROUP_WITH_ONE_USER));
  }

  @Test
  public void addFromAnotherMapOfIndexedSubscribers() {

    SubscriptionSubscriberMapBySubscriberType finalContainer =
        new SubscriptionSubscriberMapBySubscriberType();
    finalContainer.addAll(prepareUserSubscriptionSubscribers("1", "2"));
    SubscriptionSubscriberMapBySubscriberType containerToAdd =
        new SubscriptionSubscriberMapBySubscriberType();

    assertThat(finalContainer.get(SubscriberType.USER), hasSize(2));
    assertThat(finalContainer.get(SubscriberType.GROUP), hasSize(0));

    finalContainer.addAll(containerToAdd);
    assertThat(finalContainer.get(SubscriberType.USER), hasSize(2));
    assertThat(finalContainer.get(SubscriberType.GROUP), hasSize(0));

    containerToAdd.addAll(prepareUserSubscriptionSubscribers("10", "20"));
    finalContainer.addAll(containerToAdd);
    assertThat(finalContainer.get(SubscriberType.USER), hasSize(4));
    assertThat(finalContainer.get(SubscriberType.GROUP), hasSize(0));

    containerToAdd.addAll(prepareGroupSubscriptionSubscribers("30", "40", "50"));
    finalContainer.addAll(containerToAdd);
    assertThat(finalContainer.get(SubscriberType.USER), hasSize(4));
    assertThat(finalContainer.get(SubscriberType.GROUP),
        not(sameInstance(containerToAdd.get(SubscriberType.GROUP))));
    assertThat(finalContainer.get(SubscriberType.GROUP), hasSize(3));

    containerToAdd.addAll(prepareUserSubscriptionSubscribers("26"));
    containerToAdd.addAll(prepareGroupSubscriptionSubscribers("330", "440", "550"));
    finalContainer.addAll(containerToAdd);
    assertThat(finalContainer.get(SubscriberType.USER), hasSize(5));
    assertThat(finalContainer.get(SubscriberType.GROUP),
        not(sameInstance(containerToAdd.get(SubscriberType.GROUP))));
    assertThat(finalContainer.get(SubscriberType.GROUP), hasSize(6));
  }

  private Collection<SubscriptionSubscriber> prepareUserSubscriptionSubscribers(String... userIds) {
    Collection<SubscriptionSubscriber> result = new HashSet<>();
    for (String userId : userIds) {
      result.add(UserSubscriptionSubscriber.from(userId));
    }
    return result;
  }

  private Collection<SubscriptionSubscriber> prepareGroupSubscriptionSubscribers(
      String... groupIds) {
    Collection<SubscriptionSubscriber> result = new HashSet<>();
    for (String groupId : groupIds) {
      result.add(GroupSubscriptionSubscriber.from(groupId));
    }
    return result;
  }
}
