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

package com.silverpeas.subscribe.service;

import com.silverpeas.subscribe.SubscriptionResource;
import com.silverpeas.subscribe.SubscriptionSubscriber;
import com.silverpeas.subscribe.constant.SubscriptionMethod;

import java.util.Date;

/**
 * A subscription related to a resource behind a Primary PK.
 */
public class PKSubscription extends AbstractSubscription {

  /**
   * PK subscription constructor for which the type of the subscriber is USER and the
   * subscriber is the one that handles the subscription.
   * @param subscriberId id of the subscriber
   * @param subscriptionResource representation of the resource subscription
   */
  public PKSubscription(final String subscriberId,
      final PKSubscriptionResource subscriptionResource) {
    super(UserSubscriptionSubscriber.from(subscriberId), subscriptionResource, subscriberId);
  }

  /**
   * PK subscription constructor for a subscriber that handles the subscription too.
   * @param subscriber the subscriber
   * @param subscriptionResource representation of the resource subscription
   */
  public PKSubscription(final SubscriptionSubscriber subscriber,
      final PKSubscriptionResource subscriptionResource) {
    super(subscriber, subscriptionResource, subscriber.getId());
  }

  /**
   * PK subscription constructor for a subscriber that handles the subscription too.
   * @param subscriber the subscriber
   * @param subscriptionResource representation of the resource subscription
   * @param creatorId the user id that has handled the subscription
   */
  public PKSubscription(final SubscriptionSubscriber subscriber,
      final PKSubscriptionResource subscriptionResource, final String creatorId) {
    super(subscriber, subscriptionResource, SubscriptionMethod.UNKNOWN, creatorId, null);
  }

  /**
   * @see com.silverpeas.subscribe.service.AbstractSubscription
   */
  protected PKSubscription(final SubscriptionSubscriber subscriber,
      final SubscriptionResource resource, final SubscriptionMethod subscriptionMethod,
      final String creatorId, final Date creationDate) {
    super(subscriber, resource, subscriptionMethod, creatorId, creationDate);
  }
}
