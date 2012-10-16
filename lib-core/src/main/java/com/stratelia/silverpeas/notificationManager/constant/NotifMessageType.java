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
package com.stratelia.silverpeas.notificationManager.constant;

import com.stratelia.silverpeas.notificationManager.NotificationParameters;

/**
 * @author Yohann Chastagnier
 */
public enum NotifMessageType {
  NORMAL(NotificationParameters.NORMAL),
  URGENT(NotificationParameters.URGENT),
  ERROR(NotificationParameters.ERROR);

  private int id;

  private NotifMessageType(final int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public static NotifMessageType decode(final Integer id) {
    NotifMessageType result = null;
    if (id != null) {
      if (id.intValue() == NORMAL.id) {
        result = NORMAL;
      } else if (id.intValue() == URGENT.id) {
        result = URGENT;
      } else if (id.intValue() == ERROR.id) {
        result = ERROR;
      }
    }
    return result;
  }
}
