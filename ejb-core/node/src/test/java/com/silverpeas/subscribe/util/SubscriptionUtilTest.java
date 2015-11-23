/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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

import com.silverpeas.subscribe.SubscriptionSubscriber;
import com.silverpeas.subscribe.constant.SubscriberType;
import com.silverpeas.subscribe.service.GroupSubscriptionSubscriber;
import com.silverpeas.subscribe.service.UserSubscriptionSubscriber;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import static com.silverpeas.subscribe.util.SubscriptionUtil.isSameVisibilityAsTheCurrentRequester;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: Yohann Chastagnier
 * Date: 01/03/13
 */
public class SubscriptionUtilTest {

  @Test
  public void testMergeIndexedSubscriberIdsByType() {

    Map<SubscriberType, Collection<String>> finalContainer =
        new HashMap<SubscriberType, Collection<String>>();
    finalContainer.put(SubscriberType.USER, new LinkedHashSet<String>(Arrays.asList("1", "2")));
    Map<SubscriberType, Collection<String>> containerToAdd =
        new HashMap<SubscriberType, Collection<String>>();

    SubscriptionUtil.mergeIndexedSubscriberIdsByType(finalContainer, null);
    assertThat(finalContainer.size(), is(1));
    assertThat(finalContainer.get(SubscriberType.USER), hasSize(2));
    assertThat(finalContainer.get(SubscriberType.GROUP), nullValue());

    SubscriptionUtil.mergeIndexedSubscriberIdsByType(finalContainer, containerToAdd);
    assertThat(finalContainer.size(), is(1));
    assertThat(finalContainer.get(SubscriberType.USER), hasSize(2));
    assertThat(finalContainer.get(SubscriberType.GROUP), nullValue());

    containerToAdd.put(SubscriberType.USER, Arrays.asList("10", "20"));
    SubscriptionUtil.mergeIndexedSubscriberIdsByType(finalContainer, containerToAdd);
    assertThat(finalContainer.size(), is(1));
    assertThat(finalContainer.get(SubscriberType.USER), hasSize(4));
    assertThat(finalContainer.get(SubscriberType.GROUP), nullValue());

    containerToAdd.put(SubscriberType.GROUP, Arrays.asList("30", "40", "50"));
    SubscriptionUtil.mergeIndexedSubscriberIdsByType(finalContainer, containerToAdd);
    assertThat(finalContainer.size(), is(2));
    assertThat(finalContainer.get(SubscriberType.USER), hasSize(4));
    assertThat(finalContainer.get(SubscriberType.GROUP),
        not(sameInstance(containerToAdd.get(SubscriberType.GROUP))));
    assertThat(finalContainer.get(SubscriberType.GROUP), hasSize(3));
  }

  @Test
  public void testMergeIndexedSubscriberIdsByTypeWithFinalContainerAsArrayList() {

    Map<SubscriberType, Collection<String>> finalContainer =
        new HashMap<SubscriberType, Collection<String>>();
    finalContainer.put(SubscriberType.USER, new ArrayList<String>(Arrays.asList("1", "2")));
    Map<SubscriberType, Collection<String>> containerToAdd =
        new HashMap<SubscriberType, Collection<String>>();

    SubscriptionUtil.mergeIndexedSubscriberIdsByType(finalContainer, null);
    assertThat(finalContainer.size(), is(1));
    assertThat(finalContainer.get(SubscriberType.USER), hasSize(2));
    assertThat(finalContainer.get(SubscriberType.GROUP), nullValue());

    SubscriptionUtil.mergeIndexedSubscriberIdsByType(finalContainer, containerToAdd);
    assertThat(finalContainer.size(), is(1));
    assertThat(finalContainer.get(SubscriberType.USER), hasSize(2));
    assertThat(finalContainer.get(SubscriberType.GROUP), nullValue());

    containerToAdd.put(SubscriberType.USER, Arrays.asList("10", "20"));
    SubscriptionUtil.mergeIndexedSubscriberIdsByType(finalContainer, containerToAdd);
    assertThat(finalContainer.size(), is(1));
    assertThat(finalContainer.get(SubscriberType.USER), hasSize(4));
    assertThat(finalContainer.get(SubscriberType.GROUP), nullValue());

    containerToAdd.put(SubscriberType.GROUP, Arrays.asList("30", "40", "50"));
    SubscriptionUtil.mergeIndexedSubscriberIdsByType(finalContainer, containerToAdd);
    assertThat(finalContainer.size(), is(2));
    assertThat(finalContainer.get(SubscriberType.USER), hasSize(6));
    assertThat(finalContainer.get(SubscriberType.GROUP),
        not(sameInstance(containerToAdd.get(SubscriberType.GROUP))));
    assertThat(finalContainer.get(SubscriberType.GROUP), hasSize(3));
  }

  /**
   * Test of indexSubscriberIdsByType method, of class SubscriptionUtilTest.
   */
  @Test
  public void testIndexSubscriberIdsByType() {

    // Null parameter
    Map<SubscriberType, Collection<String>> subscriberIdsByType =
        SubscriptionUtil.indexSubscriberIdsByType(null);
    assertThat(subscriberIdsByType.size(), is(SubscriberType.getValidValues().size()));
    for (SubscriberType type : SubscriberType.getValidValues()) {
      assertThat(subscriberIdsByType.get(type).size(), is(0));
    }

    // Empty parameter
    subscriberIdsByType =
        SubscriptionUtil.indexSubscriberIdsByType(new ArrayList<SubscriptionSubscriber>(0));
    assertThat(subscriberIdsByType.size(), is(SubscriberType.getValidValues().size()));
    for (SubscriberType type : SubscriberType.getValidValues()) {
      assertThat(subscriberIdsByType.get(type).size(), is(0));
    }

    // 3 Users and 1 group
    subscriberIdsByType = SubscriptionUtil.indexSubscriberIdsByType(
        new ArrayList<SubscriptionSubscriber>(Arrays
            .asList(UserSubscriptionSubscriber.from("userA"),
                GroupSubscriptionSubscriber.from("group1"),
                UserSubscriptionSubscriber.from("userB"))));
    assertThat(subscriberIdsByType.size(), is(SubscriberType.getValidValues().size()));
    assertThat(subscriberIdsByType.get(SubscriberType.USER), hasSize(2));
    assertThat(subscriberIdsByType.get(SubscriberType.GROUP), hasSize(1));
  }

  @Test
  public void isUserSameVisibilityAsTheCurrentRequester() {
    UserDetail requester = mock(UserDetail.class);
    when(requester.getDomainId()).thenReturn("0");
    UserDetail user = mock(UserDetail.class);
    when(user.getDomainId()).thenReturn("0");
    assertThat(isSameVisibilityAsTheCurrentRequester(user, requester), is(true));

    // Not same domainId but no domain isolation
    when(user.getDomainId()).thenReturn("1");
    assertThat(isSameVisibilityAsTheCurrentRequester(user, requester), is(true));

    // Not same domainId but no domain isolation
    when(requester.isDomainRestricted()).thenReturn(true);
    assertThat(isSameVisibilityAsTheCurrentRequester(user, requester), is(false));
  }

  @Test
  public void isGroupSameVisibilityAsTheCurrentRequester() {
    UserDetail requester = mock(UserDetail.class);
    when(requester.getDomainId()).thenReturn("0");
    Group group = mock(Group.class);
    when(group.getDomainId()).thenReturn("0");
    assertThat(isSameVisibilityAsTheCurrentRequester(group, requester), is(true));

    // Not same domainId but no domain isolation
    when(group.getDomainId()).thenReturn("1");
    assertThat(isSameVisibilityAsTheCurrentRequester(group, requester), is(true));

    // Not same domainId but no domain isolation
    when(requester.isDomainRestricted()).thenReturn(true);
    assertThat(isSameVisibilityAsTheCurrentRequester(group, requester), is(false));
  }
}
