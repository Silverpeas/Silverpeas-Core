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

package com.silverpeas.notification;

/**
 * A publisher of a notification in Silverpeas. A notification is a message (id est notice)
 * informing one or several subscribers about an action or an event occuring in Silverpeas and in
 * what a Silverpeas object or resource is involved. The implementation of this interface should
 * wrap the underlying messaging system used to deliver notification to the several subscribers of a
 * such type of announcements. The implementation should be deployed into an IoC container under the
 * name 'notificationPublisher'.
 */
public interface NotificationPublisher {

  /**
   * Publishes a notification. All subscribers for a such announcement will recieve it. If the event
   * publication failed, then a PublishingException runtime exception will be thrown.
   * @param notification the notification to publish.
   * @param onTopic the topic on which the notification should be published.
   */
  void publish(final SilverpeasNotification notification, final NotificationTopic onTopic);
}
