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

import javax.inject.Inject;

/**
 * Factory of notification publishers for beans not managed by an IoC container. IoC container
 * managed beans should use directly a NotificationPublisher instance as it will be injected as
 * dependency by the IoC container. This factory wraps the concrete implementation of the
 * NotifcationPublisher interface by using the IoC container.
 */
public class NotificationPublisherFactory {

  private static NotificationPublisherFactory instance = new NotificationPublisherFactory();

  @Inject
  private NotificationPublisher publisher;

  /**
   * Gets an instance of this factory.
   * @return a NotificationPublisherFactory instance.
   */
  public static NotificationPublisherFactory getFactory() {
    return instance;
  }

  /**
   * Gets a notification publisher.
   * @return an implementation of the NotificationPublisher interface.
   */
  public NotificationPublisher getNotificationPublisher() {
    return publisher;
  }

  private NotificationPublisherFactory() {
  }
}
