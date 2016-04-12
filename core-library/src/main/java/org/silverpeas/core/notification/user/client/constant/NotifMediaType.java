/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.notification.user.client.constant;

import org.silverpeas.core.notification.user.client.NotificationParameters;

/**
 * @author Yohann Chastagnier
 */
public enum NotifMediaType {
  DEFAULT(NotificationParameters.ADDRESS_DEFAULT),
  COMPONENT_DEFINED(NotificationParameters.ADDRESS_COMPONENT_DEFINED),
  BASIC_POPUP(NotificationParameters.ADDRESS_BASIC_POPUP),
  BASIC_REMOVE(NotificationParameters.ADDRESS_BASIC_REMOVE),
  BASIC_SILVERMAIL(NotificationParameters.ADDRESS_BASIC_SILVERMAIL),
  BASIC_SMTP(NotificationParameters.ADDRESS_BASIC_SMTP_MAIL),
  BASIC_SERVER(NotificationParameters.ADDRESS_BASIC_SERVER),
  BASIC_USER_COMMUNICATION(NotificationParameters.ADDRESS_BASIC_COMMUNICATION_USER);

  private int id;

  private NotifMediaType(final int id) {
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
