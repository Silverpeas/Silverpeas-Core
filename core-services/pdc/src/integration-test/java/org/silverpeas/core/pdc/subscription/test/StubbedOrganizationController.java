/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.pdc.subscription.test;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Alternative;
import org.silverpeas.core.admin.service.DefaultOrganizationController;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.annotation.Service;

import static jakarta.interceptor.Interceptor.Priority.APPLICATION;

/**
 * A stub of the organization controller providing a single group of users, so that the
 * subscriptions of a user coming from one of his groups can be verified without requiring the whole
 * administration of Silverpeas.
 * @author mmoquillon
 */
@Service
@Alternative
@Priority(APPLICATION + 10)
public class StubbedOrganizationController extends DefaultOrganizationController {

  public static final String GROUP_ID = "42";
  public static final String USER_IN_GROUP_ID = "7";

  @Override
  public UserDetail[] getAllUsersOfGroup(final String groupId) {
    if (GROUP_ID.equals(groupId)) {
      final UserDetail user = new UserDetail();
      user.setId(USER_IN_GROUP_ID);
      return new UserDetail[]{user};
    }
    return new UserDetail[0];
  }

  @Override
  public String[] getAllGroupIdsOfUser(final String userId) {
    if (USER_IN_GROUP_ID.equals(userId)) {
      return new String[]{GROUP_ID};
    }
    return new String[0];
  }
}
