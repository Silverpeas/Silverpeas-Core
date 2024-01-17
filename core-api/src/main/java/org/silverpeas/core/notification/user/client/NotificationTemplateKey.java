/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.notification.user.client;

/**
 * Keys of the predefined fields injected by default in the text templates used in the notifications
 * to users.
 */
public enum NotificationTemplateKey {
  /**
   * The name of the user at the origin of the notification.
   */
  NOTIFICATION_SENDER_NAME("notification_sendername"),
  /**
   * The email address of the user at the origin of the notification.
   */
  NOTIFICATION_SENDER_EMAIL("notification_senderemail"),
  /**
   * The identifiers of the users targeted by the notification.
   */
  NOTIFICATION_RECEIVER_USERS("notification_receiver_users"),
  /**
   * The identifiers of the user groups targeted by the notification.
   */
  NOTIFICATION_RECEIVER_GROUPS("notification_receiver_groups"),
  /**
   * The base URL of the server (the port number included if other than 80)
   */
  NOTIFICATION_BASE_SERVER_URL("notification_base_serverurl"),
  /**
   * The URL of the server including the application context if any (the URL of Silverpeas).
   */
  NOTIFICATION_SERVER_URL("notification_serverurl"),
  /**
   * Link to the resource in Silverpeas for which the notification has been sent.
   */
  NOTIFICATION_LINK("notification_link"),
  /**
   * Label associated with the notification link above.
   */
  NOTIFICATION_LINK_LABEL("notification_linkLabel"),
  /**
   * Links to the attachments concerned by the notification.
   */
  NOTIFICATION_ATTACHMENTS("notification_attachments");

  private final String name;

  NotificationTemplateKey(final String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
