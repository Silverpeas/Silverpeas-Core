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

import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.subscription.constant.SubscriberType;
import org.silverpeas.kernel.util.StringUtil;

import static java.text.MessageFormat.format;
import static java.util.Optional.ofNullable;

/**
 * User: Yohann Chastagnier
 * Date: 20/02/13
 */
public class GroupSubscriptionSubscriber extends AbstractSubscriptionSubscriber {

  /**
   * A way to get an instance of a group subscriber.
   * @param groupId
   * @return
   */
  public static GroupSubscriptionSubscriber from(String groupId) {
    return new GroupSubscriptionSubscriber(groupId);
  }

  /**
   * Default constructor.
   * @param id
   */
  protected GroupSubscriptionSubscriber(final String id) {
    super(id, SubscriberType.GROUP);
  }

  /**
   * This method checks the group subscriber integrity:
   * <ul>
   *   <li>group identifier MUST be defined</li>
   *   <li>group MUST exist</li>
   *   <li>group MUST not contain an anonymous or a guest user</li>
   * </ul>
   * @throws SubscribeRuntimeException if not valid.
   */
  @Override
  public void checkValid() throws SubscribeRuntimeException {
    if (StringUtil.isNotDefined(getId())) {
      throw new SubscribeRuntimeException("group identifier is not specified");
    } else {
      final Group user = ofNullable(Group.getById(getId())).orElseThrow(
          () -> new SubscribeRuntimeException(
              format("group with identifier {0} not found", getId())));
      user.getAllUsers().forEach(u -> {
        if (u.isAnonymous() || u.isAccessGuest()) {
          throw new SubscribeRuntimeException("group contains an anonymous or a guest user");
        }
      });
    }
  }
}
