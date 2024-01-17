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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.admin.user.notification;

import java.io.Serializable;

/**
 * A link between a given user and a given group of users in Silverpeas. The link symbolizes the
 * belonging of a user to a group in Silverpeas. This class is mainly used in the notification of
 * that an operations about the link between a user and a group has been performed.
 * @author mmoquillon
 */
public class GroupUserLink implements Serializable {

  private final String groupId;
  private final String userId;

  /**
   * Constructs a new link of bounding between a given group and the specified user.
   * @param groupId the unique identifier of a group of users in Silverpeas.
   * @param userId the unique identifier of the user linked or to link with the group.
   */
  public GroupUserLink(final String groupId, final String userId) {
    this.groupId = groupId;
    this.userId = userId;
  }

  /**
   * The group of users related by this link.
   * @return the unique identifier of a group in Silverpeas.
   */
  public String getGroupId() {
    return groupId;
  }

  /**
   * The user concerned by the link with a group of users.
   * @return the unique identifier of the user in Silverpeas.
   */
  public String getUserId() {
    return userId;
  }

}
  