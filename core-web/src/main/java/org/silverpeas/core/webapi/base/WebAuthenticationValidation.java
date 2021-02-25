/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.webapi.base;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.cache.service.SessionCacheService;
import org.silverpeas.core.notification.message.MessageManager;
import org.silverpeas.core.security.session.SessionInfo;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;

/**
 * Validation of the authentication of a user accessing a web endpoint in Silverpeas.
 * This interface requires to be implemented by all of authentication validators in Silverpeas.
 * @author mmoquillon
 */
public interface WebAuthenticationValidation {

  /**
   * Gets the context of Silverpeas linked to the current request.
   * This context must be initialized before the functional request processing.
   * @return {@link SilverpeasRequestContext} instance.
   */
  SilverpeasRequestContext getSilverpeasContext();

  /**
   * Validates the authentication of the user requesting currently a web endpoint by using the
   * specified user privilege validation service. If no session was opened for the user, then open
   * a new one.
   * <p>
   * This method should be invoked for web services requiring an authenticated user. Otherwise, the
   * annotation Authenticated can be also used instead at class level.
   * </p>
   * @see UserPrivilegeValidator
   * @param validation the validation instance to use.
   * @throws WebApplicationException if the authentication isn't valid (no authentication and
   * authentication failure).
   */
  default void validateUserAuthentication(final UserPrivilegeValidation validation) {
    final HttpServletRequest request = getSilverpeasContext().getRequest();
    final SessionInfo session = validation
        .validateUserAuthentication(request, getSilverpeasContext().getResponse());
    final UserDetail currentUser = session.getUserDetail();
    getSilverpeasContext().setUser(currentUser);
    if (currentUser != null) {
      MessageManager.setLanguage(currentUser.getUserPreferences().getLanguage());
      if (User.getCurrentRequester() == null && session != SessionInfo.AnonymousSession) {
        ((SessionCacheService) CacheServiceProvider.getSessionCacheService())
            .setCurrentSessionCache(session.getCache());
      }
    }
  }
}
