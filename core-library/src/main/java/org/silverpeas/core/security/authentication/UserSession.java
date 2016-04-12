/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.core.security.authentication;

import org.silverpeas.core.admin.user.model.UserDetail;

/**
 * This bean is used by the mechanism in charge of user session management.
 * It is taken into account in order to persist a token that represents the session of a user. This
 * persistent token permits to handle a user session which can live over a server execution
 * (restart).
 * @author Yohann Chastagnier
 */
public class UserSession {

  private final UserDetail user;

  /**
   * Creates a {@link UserSession} instance from the identifier of a user.
   * @param userId the identifier of a user.
   * @return a {@link UserSession} instance.
   */
  static UserSession forUserId(String userId) {
    return new UserSession(UserDetail.getById(userId));
  }

  private UserSession(final UserDetail user) {
    this.user = user;
  }

  /**
   * The user behind this session.
   * @return the {@link UserDetail} behind the session.
   */
  public UserDetail getUser() {
    return user;
  }
}
