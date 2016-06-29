/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.admin.user.service;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.cache.service.SessionCacheService;
import org.silverpeas.core.cache.service.SimpleCacheService;
import org.silverpeas.core.util.ServiceProvider;

/**
 * In charge of providing users.
 * @author Yohann Chastagnier
 */
public interface UserProvider {

  String CURRENT_REQUESTER_KEY = User.class.getName() + "_CURRENT_REQUESTER";

  /**
   * Gets the instance of the implementation of the interface.
   * @return an implementation of {@link UserProvider}.
   */
  static UserProvider get() {
    return ServiceProvider.getService(UserProvider.class);
  }

  /**
   * Gets a user from the specified identifier.
   * @param userId a user identifier as string.
   * @return a user instance of {@link User}.
   */
  User getUser(String userId);

  /**
   * Gets the user that is behind the current request in Silverpeas.
   * @return a user instance of {@link User}.
   */
  default User getCurrentRequester() {
    User requester = null;
    SessionCacheService sessionCacheService = CacheServiceProvider.getSessionCacheService();
    SimpleCacheService sessionCache = sessionCacheService.getCurrentSessionCache();
    if (sessionCache != null) {
      requester = sessionCacheService.getUser(sessionCache);
    }
    return requester;
  }
}
