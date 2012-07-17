/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

/**
 * Front end of the subscribing service in a specific messaging system. It wraps the actual
 * underlying MOM system used for the subsccription without having a strong dependency on it (JMS,
 * AMQP, ...). The concrete MOM system implementing this interface is managed by the IoC container
 * under the name 'messageSubscribingService'.
 */
public interface MessageSubscribingService {

  /**
   * Subscribes the specified notification listener on the specified topic. The topic should exists,
   * otherwise a SubscribingException is thrown. If the subscriber is already subscribed to the
   * topic, then nothing is done.
   * @param listener the listener to subscribe.
   * @param onTopic the event topic to subscribe.
   */
  void subscribe(final NotificationSubscriber listener, final NotificationTopic onTopic);

  /**
   * Unsubscribes the specified notification listener from the specified topic. If the subscriber
   * isn't subscribed to the specified topic, nothing is done. The topic should exists, otherwise a
   * SubscribingException is thrown.
   * @param listener the listener to unsubscribe.
   * @param fromTopic the event topîc to unsubscribe.
   */
  void unsubscribe(final NotificationSubscriber listener, final NotificationTopic fromTopic);
}
