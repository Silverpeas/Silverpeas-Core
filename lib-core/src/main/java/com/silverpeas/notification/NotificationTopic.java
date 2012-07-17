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
 * Topic on which notifications are published. A topic is a way to gather notifications of the same
 * type or about the same kind of events or actions occuring in Silverpeas. Notification is always
 * sent to a given topic and notification consumers indicates always the type of notification they
 * are interested by subscribing to the corresponding notification topics.
 */
public class NotificationTopic {

  /**
   * Gets a topic matching the specified name. The topic must exist in the underlying messaging
   * system.
   * @param topicName the name of the topic to get.
   * @return the topic with the specified name.
   */
  public static NotificationTopic onTopic(String topicName) {
    return new NotificationTopic(topicName);
  }

  private final String name;

  private NotificationTopic(String name) {
    this.name = name;
  }

  /**
   * Gets the name of this topic.
   * @return the topic name.
   */
  public String getName() {
    return name;
  }

}
