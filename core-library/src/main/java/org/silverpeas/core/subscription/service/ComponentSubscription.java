/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.subscription.service;

import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.subscription.constant.SubscriptionMethod;

/**
 * @author ehugonnet
 */
public class ComponentSubscription extends AbstractSubscription<ComponentSubscriptionResource> {

  /**
   * Component subscription constructor for which the type of the subscriber is USER.
   * @param subscriberId id of the subscriber
   * @param instanceId component instance id aimed by the subscription
   */
  public ComponentSubscription(String subscriberId, String instanceId) {
    super(UserSubscriptionSubscriber.from(subscriberId),
        ComponentSubscriptionResource.from(instanceId), subscriberId);
  }

  /**
   * Component subscription constructor for a subscriber that handles the subscription too.
   * @param subscriber the subscriber
   * @param instanceId component instance id aimed by the subscription
   */
  public ComponentSubscription(final SubscriptionSubscriber subscriber, String instanceId) {
    super(subscriber, ComponentSubscriptionResource.from(instanceId), subscriber.getId());
  }

  /**
   * Component subscription constructor for a subscriber that handles the subscription too.
   * @param subscriber the subscriber
   * @param resource component instance subscription
   * @param creatorId the user id that has handled the subscription
   */
  public ComponentSubscription(final SubscriptionSubscriber subscriber,
      final ComponentSubscriptionResource resource, final String creatorId) {
    super(subscriber, resource, creatorId);
  }

  /**
   * Component subscription constructor for a subscriber that handles the subscription too.
   * @param subscriber the subscriber
   * @param instanceId component instance id aimed by the subscription
   * @param creatorId the user id that has handled the subscription
   */
  protected ComponentSubscription(final SubscriptionSubscriber subscriber, final String instanceId,
      final String creatorId) {
    super(subscriber, ComponentSubscriptionResource.from(instanceId), SubscriptionMethod.UNKNOWN,
        creatorId, null);
  }
}
