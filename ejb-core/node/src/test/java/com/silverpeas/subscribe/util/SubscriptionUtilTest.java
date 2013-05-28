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
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/**
 * User: Yohann Chastagnier
 * Date: 01/03/13
 */
public class SubscriptionUtilTest {

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
}
