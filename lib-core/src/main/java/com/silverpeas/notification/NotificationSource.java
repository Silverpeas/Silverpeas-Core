/**
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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.silverpeas.notification;

import java.io.Serializable;

/**
 * The source from which a notification was generated. Usually, a notification is triggered by a
 * user action and it informs the event concerning the resource involved in the action. For example,
 * the creation of a new publication by a user in Silverpeas. So the notification is always
 * generated within the scope of a Silverpeas component instance and may involve the action of a
 * given user. This class can be extended to provide additional information about the context of the
 * notification generation.
 */
public class NotificationSource implements Serializable {
  private static final long serialVersionUID = -6121327382115619016L;

  private String componentId;
  private String userId;

  /**
   * Gets the unique identifier of the Silverpeas component instance within which a notification was
   * occured.
   * @return the component instance identifier.
   */
  public String getComponentInstanceId() {
    return componentId;
  }

  /**
   * Sets the unique identifier of the Silverpeas component instance within which the notification
   * is occuring.
   * @param componentId the component instance identifier.
   * @return itself.
   */
  public NotificationSource withComponentInstanceId(String componentId) {
    this.componentId = componentId;
    return this;
  }

  /**
   * Gets the unique identifier of the user performed the action that has triggered the
   * notification. If no users were involved in the notification, null is returned.
   * @return the user identifier or null if no users were involved in the notification.
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Sets the unique identifier of the user that is involved in the notification by performing an
   * action on a Silverpeas managed resource.
   * @param userId the user identifier.
   * @return itself.
   */
  public NotificationSource withUserId(String userId) {
    this.userId = userId;
    return this;
  }

}
