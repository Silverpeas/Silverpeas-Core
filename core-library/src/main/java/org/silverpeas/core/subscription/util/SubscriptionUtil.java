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
package org.silverpeas.core.subscription.util;

import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserDetail;

/**
 * Utility class shared by other classes of the same package.
 */
public class SubscriptionUtil {

  /**
   * Indicates if the given subscription subscriber has same domain visibility as the current
   * requester.
   * @param subscriber the subscriber to verify.
   * @param currentRequester the current user requester.
   * @return true if same domain visibility, false otherwise.
   */
  static boolean isSameVisibilityAsTheCurrentRequester(
      final SubscriptionSubscriber subscriber, UserDetail currentRequester) {
    if (currentRequester.isDomainRestricted()) {
      switch (subscriber.getType()) {
        case USER:
          return isSameVisibilityAsTheCurrentRequester(UserDetail.getById(subscriber.getId()),
              currentRequester);
        case GROUP:
          return isSameVisibilityAsTheCurrentRequester(Group.getById(subscriber.getId()),
              currentRequester);
      }
      return false;
    }
    return true;
  }

  /**
   * Indicates if the given user has same domain visibility as the current requester.
   * @param user the user to verify.
   * @param currentRequester the current user requester.
   * @return true if same domain visibility, false otherwise.
   */
  public static boolean isSameVisibilityAsTheCurrentRequester(final UserDetail user,
      UserDetail currentRequester) {
    return !currentRequester.isDomainRestricted() ||
        user.getDomainId().equals(currentRequester.getDomainId());
  }

  /**
   * Indicates if the given group has same domain visibility as the current requester.
   * @param group the group to verify.
   * @param currentRequester the current user requester.
   * @return true if same domain visibility, false otherwise.
   */
  public static boolean isSameVisibilityAsTheCurrentRequester(final Group group,
      UserDetail currentRequester) {
    return !currentRequester.isDomainRestricted() || StringUtil.isNotDefined(group.getDomainId()) ||
        group.getDomainId().equals(currentRequester.getDomainId());
  }
}
