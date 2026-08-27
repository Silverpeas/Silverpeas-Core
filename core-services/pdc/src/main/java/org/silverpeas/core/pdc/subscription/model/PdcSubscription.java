/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.pdc.subscription.model;

import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.subscription.constant.SubscriptionMethod;
import org.silverpeas.core.subscription.service.AbstractSubscription;
import org.silverpeas.core.subscription.service.UserSubscriptionSubscriber;

/**
 * A subscription of a user or of a group of users to position criteria on the axis of the PdC.
 * <p>
 * As any subscription in Silverpeas, it can be either created by the subscriber himself or forced
 * by a manager of the PdC for one or more users or groups of users.
 * </p>
 * @author mmoquillon
 */
public class PdcSubscription extends AbstractSubscription<PdcSubscriptionPositionCriteria> {

  /**
   * Constructs a subscription of the specified user to the given position criteria on the PdC. The
   * user is both the subscriber and the creator of the subscription, and hence the subscription is
   * a {@link org.silverpeas.core.subscription.constant.SubscriptionMethod#SELF_CREATION} one.
   * @param subscriberId the unique identifier of the user that subscribes.
   * @param resource the position criteria on the PdC aimed by the subscription.
   */
  public PdcSubscription(final String subscriberId, final PdcSubscriptionPositionCriteria resource) {
    super(UserSubscriptionSubscriber.from(subscriberId), resource, subscriberId);
  }

  /**
   * Constructs a subscription of the specified subscriber to the given position criteria on the
   * PdC. The subscription method is deduced from both the type of the subscriber and the user
   * that has handled the subscription.
   * @param subscriber the user or the group of users that is subscribed.
   * @param resource the position criteria on the PdC aimed by the subscription.
   * @param creatorId the unique identifier of the user that has handled the subscription.
   */
  public PdcSubscription(final SubscriptionSubscriber subscriber,
      final PdcSubscriptionPositionCriteria resource, final String creatorId) {
    super(subscriber, resource, creatorId);
  }

  /**
   * Constructs a subscription of the specified subscriber to the given position criteria on the PdC
   * with an explicit subscription method.
   * <p>
   * Unlike {@link #PdcSubscription(SubscriptionSubscriber, PdcSubscriptionPositionCriteria, String)}, the
   * method isn't deduced from the subscriber and the creator. It is required by the management of
   * the forced subscriptions on the PdC: a manager of the PdC that subscribes himself among the
   * other subscribers has to be subscribed by a
   * {@link SubscriptionMethod#FORCED} subscription and not by a self created one, otherwise the
   * position criteria he manages wouldn't be anymore listed as a forced subscription.
   * </p>
   * @param subscriber the user or the group of users that is subscribed.
   * @param resource the position criteria on the PdC aimed by the subscription.
   * @param method the method by which the subscription has been done.
   * @param creatorId the unique identifier of the user that has handled the subscription.
   */
  public PdcSubscription(final SubscriptionSubscriber subscriber,
      final PdcSubscriptionPositionCriteria resource, final SubscriptionMethod method,
      final String creatorId) {
    super(subscriber, resource, method, creatorId, null);
  }
}
