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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.subscription.service;

import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.subscription.constant.SubscriptionMethod;

/**
 * @author ehugonnet
 */
public class NodeSubscription extends AbstractSubscription<NodeSubscriptionResource> {

  /**
   * Node subscription constructor for which the type of the subscriber is USER and the
   * subscriber is the one that handles the subscription.
   * @param subscriberId id of the subscriber
   * @param nodePK representation of the topic
   */
  public NodeSubscription(final String subscriberId, final NodePK nodePK) {
    super(UserSubscriptionSubscriber.from(subscriberId), NodeSubscriptionResource.from(nodePK),
        subscriberId);
  }

  /**
   * Node subscription constructor for a subscriber that handles the subscription too.
   * @param subscriber the subscriber
   * @param nodePK representation of the topic
   */
  public NodeSubscription(final SubscriptionSubscriber subscriber, final NodePK nodePK) {
    super(subscriber, NodeSubscriptionResource.from(nodePK), subscriber.getId());
  }

  /**
   * Node subscription constructor for a subscriber that handles the subscription too.
   * @param subscriber the subscriber
   * @param resource the topic resource
   * @param creatorId the user id that has handled the subscription
   */
  public NodeSubscription(final SubscriptionSubscriber subscriber,
      final NodeSubscriptionResource resource, final String creatorId) {
    super(subscriber, resource, creatorId);
  }

  /**
   * Node subscription constructor for a subscriber that handles the subscription too.
   * @param subscriber the subscriber
   * @param nodePK representation of the topic
   * @param creatorId the user id that has handled the subscription
   */
  protected NodeSubscription(final SubscriptionSubscriber subscriber, final NodePK nodePK,
      final String creatorId) {
    super(subscriber, NodeSubscriptionResource.from(nodePK), SubscriptionMethod.UNKNOWN, creatorId,
        null);
  }
}
