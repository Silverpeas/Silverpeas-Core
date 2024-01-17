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
package org.silverpeas.core.notification.user.client.constant;

import org.silverpeas.core.notification.user.client.NotificationParameters;

/**
 * The level of information carried by the message.
 * @author Yohann Chastagnier
 */
public enum NotifMessageType {
  /**
   * The message is an usual one.
   */
  NORMAL(NotificationParameters.PRIORITY_NORMAL),

  /**
   * The message is for an urgency.
   */
  URGENT(NotificationParameters.PRIORITY_URGENT),

  /**
   * The message is about an error.
   */
  ERROR(NotificationParameters.PRIORITY_ERROR);

  private int id;

  NotifMessageType(final int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public static NotifMessageType decode(final Integer id) {
    if (id != null) {
      for (NotifMessageType notifMessageType : NotifMessageType.values()) {
        if (id == notifMessageType.id) {
          return notifMessageType;
        }
      }
    }
    return null;
  }
}
