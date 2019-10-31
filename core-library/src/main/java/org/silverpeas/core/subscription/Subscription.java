/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.subscription;

import org.silverpeas.core.subscription.constant.SubscriptionMethod;

import java.util.Date;

/**
 * A subscription of a user or a group to be notified about changes of a given resource in
 * Silverpeas.
 * @author ehugonnet
 */
public interface Subscription {

  /**
   * Gets the resource on which the subscription is.
   * @return a {@link SubscriptionResource} instance representing a resource in Silverpeas.
   */
  SubscriptionResource getResource();

  /**
   * Gets the subscriber. It can be either a user or a group of users. In the case of a group
   * of users, the subscription was done by a user with management rights on the resource.
   * @return a {@link SubscriptionSubscriber} instance representing either the user or the group of
   * users that have subscribed to the given resource.
   */
  SubscriptionSubscriber getSubscriber();

  /**
   * What is the method used to subscribe to the resource? It can be either by the subscriber
   * himself or it can be forced by another user with management rights.
   * @return a {@link SubscriptionMethod} instance representing the method used to create this
   * subscription.
   */
  SubscriptionMethod getSubscriptionMethod();

  /**
   * Gets the unique identifier of the user that did the subscription. It can be the identifier
   * of the subscriber or, for forced subscription, it is the identifier of a user with management
   * rights.
   * @return the unique identifier of a user.
   */
  String getCreatorId();

  /**
   * Gets the date at which the subscription has been done.
   * @return a date.
   */
  Date getCreationDate();
}
