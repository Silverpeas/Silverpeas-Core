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

import java.io.Serializable;

/**
 * A publisher of a notification.
 * A notification is a message (id est notice) informing one or several subscribers about an action
 * or an event occuring in Silverpeas and in what a Silverpeas object or resource is involved.
 *
 * The implementation of this interface should wrap the underlying messaging system used to deliver
 * notification to the several subscribers to a such type of announcements. The implementation
 * should be deployed into an IoC container under the name 'eventPublisher'.
 */
public interface NotificationPublisher {

  /**
   * Publishes an event. All subscribers for a such event will recieve it.
   * If the event publication failed, then an EventPublicationException will be thrown.
   * @param <T> the type of the object for which the event was generated. The object is carried by
   * the event.
   * @param event the event to publish.
   * @param onTopic the topic on which the event should be published.
   */
  <T extends Serializable> void publish(final SilverpeasNotification<T> event, final NotificationTopic onTopic);
}
