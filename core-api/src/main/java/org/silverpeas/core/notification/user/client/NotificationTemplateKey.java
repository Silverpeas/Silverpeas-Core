/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.notification.user.client;

public enum NotificationTemplateKey {
  NOTIFICATION_SENDER_NAME("notification_sendername"),
  NOTIFICATION_SENDER_EMAIL("notification_senderemail"),
  NOTIFICATION_RECEIVER_USERS("notification_receiver_users"),
  NOTIFICATION_RECEIVER_GROUPS("notification_receiver_groups"),
  NOTIFICATION_SERVER_URL("notification_serverurl"),
  NOTIFICATION_LINK("notification_link"),
  NOTIFICATION_LINK_LABEL("notification_linkLabel"),
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
