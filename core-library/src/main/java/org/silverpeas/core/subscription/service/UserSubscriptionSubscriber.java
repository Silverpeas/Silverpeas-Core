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
package org.silverpeas.core.subscription.service;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.subscription.constant.SubscriberType;
import org.silverpeas.core.util.StringUtil;

import static java.text.MessageFormat.format;
import static java.util.Optional.ofNullable;

/**
 * User: Yohann Chastagnier
 * Date: 20/02/13
 */
public class UserSubscriptionSubscriber extends AbstractSubscriptionSubscriber {

  /**
   * A way to get an instance of a user subscriber.
   * @param userId
   * @return
   */
  public static UserSubscriptionSubscriber from(String userId) {
    return new UserSubscriptionSubscriber(userId);
  }

  /**
   * Default constructor.
   * @param id
   */
  protected UserSubscriptionSubscriber(final String id) {
    super(id, SubscriberType.USER);
  }

  /**
   * This method checks the user subscriber integrity:
   * <ul>
   *   <li>user identifier MUST be defined</li>
   *   <li>user MUST exist</li>
   *   <li>user MUST not be an anonymous one or a guest one</li>
   * </ul>
   * @throws SubscribeRuntimeException if not valid.
   */
  @Override
  public void checkValid() throws SubscribeRuntimeException {
    if (StringUtil.isNotDefined(getId())) {
      throw new SubscribeRuntimeException("user identifier is not specified");
    } else {
      final User user = ofNullable(User.getById(getId())).orElseThrow(
          () -> new SubscribeRuntimeException(
              format("user with identifier {0} not found", getId())));
      if (user.isAnonymous() || user.isAccessGuest()) {
        throw new SubscribeRuntimeException("user MUST not be an anonymous or a guest one");
      }
    }
  }
}
