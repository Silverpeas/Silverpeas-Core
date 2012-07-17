/*
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
package com.stratelia.silverpeas.notificationManager.constant;

import com.stratelia.silverpeas.notificationManager.NotificationParameters;

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
    NotifMediaType result = null;
    if (id != null) {
      if (id.intValue() == DEFAULT.id) {
        result = DEFAULT;
      } else if (id.intValue() == COMPONENT_DEFINED.id) {
        result = COMPONENT_DEFINED;
      } else if (id.intValue() == BASIC_POPUP.id) {
        result = BASIC_POPUP;
      } else if (id.intValue() == BASIC_REMOVE.id) {
        result = BASIC_REMOVE;
      } else if (id.intValue() == BASIC_SILVERMAIL.id) {
        result = BASIC_SILVERMAIL;
      } else if (id.intValue() == BASIC_SMTP.id) {
        result = BASIC_SMTP;
      } else if (id.intValue() == BASIC_SERVER.id) {
        result = BASIC_SERVER;
      } else if (id.intValue() == BASIC_USER_COMMUNICATION.id) {
        result = BASIC_USER_COMMUNICATION;
      }
    }
    return result;
  }
}
