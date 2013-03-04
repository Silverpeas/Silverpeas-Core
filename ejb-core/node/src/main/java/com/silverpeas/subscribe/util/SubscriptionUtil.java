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
import com.silverpeas.util.MapUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * User: Yohann Chastagnier
 * Date: 28/02/13
 */
public class SubscriptionUtil {

  /**
   * Indexes given subscribers by their type.
   * @param subscribers
   * @return a map which has type of subscriber as a key and the corresponding subscriber ids of
   *         type of subscriber as a value
   */
  public static Map<SubscriberType, Collection<String>> indexSubscriberIdsByType(
      Collection<SubscriptionSubscriber> subscribers) {
    return indexSubscriberIdsByType(null, subscribers);
  }

  /**
   * Indexes given subscribers by their type.
   * @param existingSubscribers
   * @param subscribers
   * @return a map which has type of subscriber as a key and the corresponding subscriber ids of
   *         type of subscriber as a value
   */
  public static Map<SubscriberType, Collection<String>> indexSubscriberIdsByType(
      Map<SubscriberType, Collection<String>> existingSubscribers,
      Collection<SubscriptionSubscriber> subscribers) {

    // Initializing the result
    Map<SubscriberType, Collection<String>> indexedSubscribers =
        (existingSubscribers == null) ? new HashMap<SubscriberType, Collection<String>>() :
            existingSubscribers;

    // From given subscribers
    if (subscribers != null) {
      for (SubscriptionSubscriber subscriber : subscribers) {
        MapUtil.putAdd(LinkedHashSet.class, indexedSubscribers, subscriber.getType(),
            subscriber.getId());
      }
    }

    // Initialize empty collections on subscriber type indexes that don't exist.
    // It avoid to callers to take in account null collections.
    for (SubscriberType subscriberType : SubscriberType.getValidValues()) {
      if (!indexedSubscribers.containsKey(subscriberType)) {
        indexedSubscribers.put(subscriberType, new LinkedHashSet<String>(0));
      }
    }

    // Returning indexed subscribers
    return indexedSubscribers;
  }
}
