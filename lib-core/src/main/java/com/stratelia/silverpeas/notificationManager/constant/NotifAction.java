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

/**
 * @author Yohann Chastagnier
 */
public enum NotifAction {
  CREATE(1, 1), UPDATE(2, 2), DELETE(3, 3), REPORT(4, 4), COMMENT(5, 5), SUSPEND(6, 6), PENDING_VALIDATION(7, 7),
  REFUSE(8, 8), VALIDATE(9, 9);

  private int id;
  private int priority;

  private NotifAction(final int id, final int priority) {
    this.id = id;
    this.priority = priority;
  }

  public int getId() {
    return id;
  }

  public int getPriority() {
    return priority;
  }

  public static NotifAction decode(final Integer id) {
    NotifAction result = null;
    if (id != null) {
      if (id.intValue() == CREATE.id) {
        result = CREATE;
      } else if (id.intValue() == UPDATE.id) {
        result = UPDATE;
      } else if (id.intValue() == DELETE.id) {
        result = DELETE;
      } else if (id.intValue() == REPORT.id) {
        result = REPORT;
      } else if (id.intValue() == COMMENT.id) {
        result = COMMENT;
      } else if (id.intValue() == SUSPEND.id) {
        result = SUSPEND;
      } else if (id.intValue() == PENDING_VALIDATION.id) {
        result = PENDING_VALIDATION;
      } else if (id.intValue() == REFUSE.id) {
        result = REFUSE;
      } else if (id.intValue() == VALIDATE.id) {
        result = VALIDATE;
      }
    }
    return result;
  }
}
