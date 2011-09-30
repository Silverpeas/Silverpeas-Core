/*
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.notification;

import java.util.EventListener;

/**
 * A subscriber of notifications published on a given topic in Silverpeas. The subscriber will be
 * notified of the events or actions occuring in Silverpeas and about the subscribed topic.
 *
 * At subscription, the subscriber indicates the topic of the notifications it is interested on.
 * It will be then informed of such notifications through the onNotification() callback; so that
 * the subscriber is also a notification listener. The subscriber can subscribe several topics.
 */
public interface NotificationSubscriber extends EventListener {

  /**
   * Gets the unique identifier of this subscriber in the MOM system.
   * The identifier is computed during the subscription into a MOM system.
   * @return the unique identifier of this subscriber.
   */
  String getId();

  /**
   * Sets the unique identifier for this subscriber.
   * The identifier is set by the MOM system at subscription.
   * @param id the unique identifier to set.
   */
  void setId(String id);

  /**
   * Subscribes to notifications received on the specified topic.
   * If the topic doesn't exist, a SubscriptionException is thrown.
   * If the subscriber is already subscribed to the specified topic, nothing is done.
   * @param onTopic the topic on which this listener listens.
   */
  void subscribeForNotifications(final NotificationTopic onTopic);

  /**
   * Unsubscribes from the notifications sent on the specified topic.
   * If the subscriber isn't subscribed to the specified topic, then nothing is done.
   * @param onTopic the topic to unsubscribe.
   */
  void unsubscribeForNotifications(final NotificationTopic onTopic);

  /**
   * Callback called at the reception of a notification on the topic on which it is listening.
   * @param notification the received notification .
   * @param onTopic the topic for which the notification was emitted.
   */
   void onNotification(final SilverpeasNotification notification, final NotificationTopic onTopic);
}
