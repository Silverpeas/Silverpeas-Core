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

import com.silverpeas.subscribe.Subscription;
import com.silverpeas.subscribe.SubscriptionSubscriber;
import com.silverpeas.subscribe.constant.SubscriberType;
import com.silverpeas.util.MapUtil;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * User: Yohann Chastagnier
 * Date: 28/02/13
 */
public class SubscriptionUtil {

  /**
   * Adding containerToAdd into finalContainer.
   * @param finalContainer
   * @param containerToAdd
   */
  public static void mergeIndexedSubscriberIdsByType(
      Map<SubscriberType, Collection<String>> finalContainer,
      Map<SubscriberType, Collection<String>> containerToAdd) {
    if (finalContainer != null && containerToAdd != null) {
      for (Map.Entry<SubscriberType, Collection<String>> entry : containerToAdd.entrySet()) {
        if (entry.getValue() != null) {
          Collection<String> subscriberIds = finalContainer.get(entry.getKey());
          if (subscriberIds != null) {
            subscriberIds.addAll(entry.getValue());
          } else {
            finalContainer.put(entry.getKey(), new LinkedHashSet<String>(entry.getValue()));
          }
        }
      }
    }
  }

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

  /**
   * Removes from the given collection the subscription that the linked subscriber has not the same
   * domain visibility as the current requester one.
   * @param subscriptions a list of subscription to filter.
   * @param currentRequester the current user requester.
   */
  public static void filterSubscriptionsOnDomainVisibility(Collection<Subscription> subscriptions,
      UserDetail currentRequester) {
    if (currentRequester.isDomainRestricted()) {
      Iterator<Subscription> itOfSubscriptions = subscriptions.iterator();
      while (itOfSubscriptions.hasNext()) {
        Subscription subscription = itOfSubscriptions.next();
        if (!isSameVisibilityAsTheCurrentRequester(subscription.getSubscriber(),
            currentRequester)) {
          itOfSubscriptions.remove();
        }
      }
    }
  }

  /**
   * Removes from the given collection the subscribers that have not the same domain visibility as
   * the current requester one.
   * @param subscribers the subscribers to filter.
   * @param currentRequester the current user requester.
   */
  public static void filterSubscribersOnDomainVisibility(
      final Collection<SubscriptionSubscriber> subscribers, UserDetail currentRequester) {
    if (currentRequester.isDomainRestricted()) {
      Iterator<SubscriptionSubscriber> itOfSubscribers = subscribers.iterator();
      while (itOfSubscribers.hasNext()) {
        SubscriptionSubscriber subscriber = itOfSubscribers.next();
        if (!isSameVisibilityAsTheCurrentRequester(subscriber, currentRequester)) {
          itOfSubscribers.remove();
        }
      }
    }
  }

  /**
   * Indicates if the given subscription subscriber has same domain visibility as the current
   * requester.
   * @param subscriber the subscriber to verify.
   * @param currentRequester the current user requester.
   * @return true if same domain visibility, false otherwise.
   */
  public static boolean isSameVisibilityAsTheCurrentRequester(
      final SubscriptionSubscriber subscriber, UserDetail currentRequester) {
    if (currentRequester.isDomainRestricted()) {
      switch (subscriber.getType()) {
        case USER:
          return isSameVisibilityAsTheCurrentRequester(UserDetail.getById(subscriber.getId()),
              currentRequester);
        case GROUP:
          return isSameVisibilityAsTheCurrentRequester(Group.getById(subscriber.getId()),
              currentRequester);
      }
      return false;
    }
    return true;
  }

  /**
   * Indicates if the given user has same domain visibility as the current requester.
   * @param user the user to verify.
   * @param currentRequester the current user requester.
   * @return true if same domain visibility, false otherwise.
   */
  public static boolean isSameVisibilityAsTheCurrentRequester(final UserDetail user,
      UserDetail currentRequester) {
    return !currentRequester.isDomainRestricted() ||
        user.getDomainId().equals(currentRequester.getDomainId());
  }

  /**
   * Indicates if the given group has same domain visibility as the current requester.
   * @param group the group to verify.
   * @param currentRequester the current user requester.
   * @return true if same domain visibility, false otherwise.
   */
  public static boolean isSameVisibilityAsTheCurrentRequester(final Group group,
      UserDetail currentRequester) {
    return !currentRequester.isDomainRestricted() || StringUtil.isNotDefined(group.getDomainId()) ||
        group.getDomainId().equals(currentRequester.getDomainId());
  }
}
