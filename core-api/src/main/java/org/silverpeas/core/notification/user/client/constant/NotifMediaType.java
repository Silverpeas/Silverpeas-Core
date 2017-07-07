/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
package org.silverpeas.core.notification.user.client.constant;

import org.silverpeas.core.notification.user.client.NotificationParameters;

/**
 * The different type of media that can be used to transmit a notification.
 * @author Yohann Chastagnier
 */
public enum NotifMediaType {

  /**
   * Use the notification channel that is set up by the user in its preferences.
   */
  DEFAULT(NotificationParameters.ADDRESS_DEFAULT),

  /**
   * Use the notification channel that is defined by the component instance itself.
   */
  COMPONENT_DEFINED(NotificationParameters.ADDRESS_COMPONENT_DEFINED),

  /**
   * Use explicitly {@link NotifChannel#POPUP}.
   */
  BASIC_POPUP(NotificationParameters.ADDRESS_BASIC_POPUP),

  /**
   * Use explicitly {@link NotifChannel#REMOVE}.
   */
  BASIC_REMOVE(NotificationParameters.ADDRESS_BASIC_REMOVE),

  /**
   * Use explicitly {@link NotifChannel#SILVERMAIL}.
   */
  BASIC_SILVERMAIL(NotificationParameters.ADDRESS_BASIC_SILVERMAIL),

  /**
   * Use explicitly {@link NotifChannel#SMTP}.
   */
  BASIC_SMTP(NotificationParameters.ADDRESS_BASIC_SMTP_MAIL),

  /**
   * Use explicitly {@link NotifChannel#SERVER}.
   */
  BASIC_SERVER(NotificationParameters.ADDRESS_BASIC_SERVER),

  /**
   * Use explicitly {@link NotifChannel#SMS}.
   */
  BASIC_USER_COMMUNICATION(NotificationParameters.ADDRESS_BASIC_COMMUNICATION_USER);

  private int id;

  NotifMediaType(final int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public static NotifMediaType decode(final Integer id) {
    if (id != null) {
      for (NotifMediaType notifMediaType : NotifMediaType.values()) {
        if (id == notifMediaType.id) {
          return notifMediaType;
        }
      }
    }
    return null;
  }
}
