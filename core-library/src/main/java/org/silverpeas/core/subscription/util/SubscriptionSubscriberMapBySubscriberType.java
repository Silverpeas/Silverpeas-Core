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

import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.subscription.constant.SubscriberType;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.admin.user.model.UserDetail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Map to index subscription subscribers by their type.
 * @author Yohann Chastagnier
 */
public class SubscriptionSubscriberMapBySubscriberType
    extends HashMap<SubscriberType, SubscriptionSubscriberList> {
  private static final long serialVersionUID = 450436743664606932L;

  /**
   * Initializing from a list of {@link SubscriptionSubscriber}.
   * @param subscribers the list of subscription subscribers.
   */
  public SubscriptionSubscriberMapBySubscriberType(SubscriptionSubscriber... subscribers) {
    initialize().addAll(Arrays.asList(subscribers));
  }

  /**
   * Initializing from a list of {@link SubscriptionSubscriber}.
   * @param subscribers the list of subscription subscribers.
   */
  public SubscriptionSubscriberMapBySubscriberType(Collection<SubscriptionSubscriber> subscribers) {
    initialize().addAll(subscribers);
  }

  /**
   * Mandatory initializations.
   * @return
   */
  private SubscriptionSubscriberMapBySubscriberType initialize() {
    for (SubscriberType subscriberType : SubscriberType.getValidValues()) {
      put(subscriberType, new SubscriptionSubscriberList());
    }
    return this;
  }

  /**
   * Adds elements from a list of {@link SubscriptionSubscriber}.
   * @param subscribers the list of subscription subscribers.
   */
  public void addAll(Collection<SubscriptionSubscriber> subscribers) {
    if (CollectionUtil.isNotEmpty(subscribers)) {
      for (SubscriptionSubscriber subscriber : subscribers) {
        add(subscriber);
      }
    }
  }

  /**
   * Adds elements from a list of {@link SubscriptionSubscriber}.
   * @param subscribers the list of subscription subscribers.
   */
  public void addAll(SubscriptionSubscriberMapBySubscriberType subscribers) {
    if (subscribers != null) {
      for (Collection<SubscriptionSubscriber> subscriptionSubscribers : subscribers.values()) {
        addAll(subscriptionSubscribers);
      }
    }
  }

  /**
   * Adds the given subscriber into the current indexation.
   * @param subscriber the subscriber to add.
   */
  public void add(SubscriptionSubscriber subscriber) {
    if (subscriber != null) {
      SubscriptionSubscriberList current = get(subscriber.getType());
      if (!current.contains(subscriber)) {
        current.add(subscriber);
      }
    }
  }

  /**
   * Retrieves from the indexed map all unique identifiers of user identifiers (so the users of
   * groups are taken into account).
   * @return the complete list of user identifiers (those of groups too).
   */
  public List<String> getAllUserIds() {
    Set<String> userIds = new HashSet<>();
    for (SubscriptionSubscriberList subscriptionSubscribers : values()) {
      userIds.addAll(subscriptionSubscribers.getAllUserIds());
    }
    return new ArrayList<>(userIds);
  }

  /**
   * Removes from this map the subscribers that have not the same domain visibility as the one
   * of the given user.
   * @param user the user that represents the visibility to verify.
   * @return itself.
   */
  public SubscriptionSubscriberMapBySubscriberType filterOnDomainVisibilityFrom(
      final UserDetail user) {
    for (final Map.Entry<SubscriberType, SubscriptionSubscriberList> entry : entrySet()) {
      entry.getValue().filterOnDomainVisibilityFrom(user);
    }
    return this;
  }
}
