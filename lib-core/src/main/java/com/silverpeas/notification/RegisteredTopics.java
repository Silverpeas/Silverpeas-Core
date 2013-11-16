/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.notification;

/**
 * This enumeration lists all the notification topics that are registered within the Silverpeas
 * Notification System and for which any beans in Silverpeas can subscribe.
 */
public enum RegisteredTopics {

  /**
   * This topic is for notifications about an action performed on a node. A node in Silverpeas is a
   * way to categorize in a hierarchical way information.
   */
  NODE_TOPIC("node"), ADMIN_SPACE_TOPIC("admin/space"), ATTACHMENT_TOPIC("attachment"),
  COMMENT_TOPIC("comment"), ADMIN_COMPONENT_TOPIC("admin/component");

  public String getTopicName() {
    return topicName;
  }

  @Override
  public String toString() {
    return topicName;
  }

  private String topicName;

  private RegisteredTopics(String topicName) {
    this.topicName = topicName;
  }

  public static RegisteredTopics fromName(String name) {
    if (NODE_TOPIC.getTopicName().equals(name)) {
      return NODE_TOPIC;
    }
    if (ADMIN_SPACE_TOPIC.getTopicName().equals(name)) {
      return ADMIN_SPACE_TOPIC;
    }
    if (ATTACHMENT_TOPIC.getTopicName().equals(name)) {
      return ATTACHMENT_TOPIC;
    }
    if (COMMENT_TOPIC.getTopicName().equals(name)) {
      return COMMENT_TOPIC;
    }
    if (ADMIN_COMPONENT_TOPIC.getTopicName().equals(name)) {
      return ADMIN_COMPONENT_TOPIC;
    }
    throw new IllegalArgumentException(name + " is not a valid topic name");
  }
}
