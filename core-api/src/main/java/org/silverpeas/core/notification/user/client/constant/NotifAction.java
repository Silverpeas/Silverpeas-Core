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

/**
 * The user action related to a contribution that is behind the notification.
 * @author Yohann Chastagnier
 */
public enum NotifAction {
  /**
   * A contribution has been created.
   */
  CREATE(1, 1),

  /**
   * A contribution has been updated.
   */
  UPDATE(2, 2),

  /**
   * A contribution has been deleted.
   */
  DELETE(3, 3),

  /**
   * A report of information about a contribution. For example, a request for validation of a
   * contribution in order to be published.
   */
  REPORT(4, 4),

  /**
   * A contribution has been commented.
   */
  COMMENT(5, 5),

  /**
   * The publishing of a contribution has been suspended.
   */
  SUSPEND(6, 6),

  /**
   * A contribution is in validation pending.
   */
  PENDING_VALIDATION(7, 7),

  /**
   * The publishing of a contribution has been refused.
   */
  REFUSE(8, 9),

  /**
   * The publishing of a contribution has been validated.
   */
  VALIDATE(9, 10),

  /**
   * In waiting of response.
   */
  RESPONSE(10, 11),

  /**
   * A contribution has been classified.
   */
  CLASSIFIED(11, 12),

  /**
   * A contribution has been unclassified.
   */
  DECLASSIFIED(12, 13),

  /**
   * A contribution has been published.
   */
  PUBLISHED(13, 14),

  /**
   * A container has been populated.
   */
  POPULATED(14, 15),

  /**
   * A contribution has been canceled.
   */
  CANCELED(15, 8);

  private final int id;
  private final int priority;

  NotifAction(final int id, final int priority) {
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
