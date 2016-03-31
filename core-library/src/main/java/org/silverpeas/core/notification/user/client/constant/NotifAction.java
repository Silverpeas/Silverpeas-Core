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

/**
 * @author Yohann Chastagnier
 */
public enum NotifAction {
  CREATE(1, 1), UPDATE(2, 2), DELETE(3, 3), REPORT(4, 4), COMMENT(5, 5), SUSPEND(6, 6),
  PENDING_VALIDATION(7, 7), REFUSE(8, 8), VALIDATE(9, 9), RESPONSE(10, 10), CLASSIFIED(11, 11),
  DECLASSIFIED(12, 12), PUBLISHED(13, 13);

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
    if (id != null) {
      for (NotifAction notifAction : NotifAction.values()) {
        if (id == notifAction.id) {
          return notifAction;
        }
      }
    }
    return null;
  }
}
