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
 * FLOSS exception.  You should have received a copy of the text describing
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
package org.silverpeas.core.cache.service;

import org.silverpeas.core.admin.user.model.User;

/**
 * Service to manage session caches.
 * <p/>
 * A session cache is a cache whose lifetime span over the session of a user in Silverpeas. As
 * such,
 * a session cache belongs to a given user and should be initialized at the user session opening.
 * @author mmoquillon
 */
public class SessionCacheService {

  private static final String CURRENT_REQUESTER_KEY = User.class.getName() + "_CURRENT_REQUESTER";
  private static final String CURRENT_SESSION_KEY = "@SessionCache@";

  /**
   * Creates a new session cache for the specified user and sets it as the cache of the current
   * session.
   * @param user a Silverpeas user for which a session has to be opened.
   * @return the session cache.
   */
  public SimpleCacheService newSessionCache(User user) {
    InMemoryCacheService sessionCache = new InMemoryCacheService();
    sessionCache.put(CURRENT_REQUESTER_KEY, user);
    CacheServiceProvider.getRequestCacheService().put(CURRENT_SESSION_KEY, sessionCache);
    return sessionCache;
  }

  /**
   * Sets the specified session cache as the current one. This is a technical method that shouldn't
   * be used by business operations. It could be removed in the future according to the evolution
   * of the implementation of the cache sessions.
   * @param sessionCache the cache session to set.
   * @throws IllegalArgumentException if the specified cache isn't a session cache already
   * initialized by the session cache management mechanism.
   */
  public void setCurrentSessionCache(SimpleCacheService sessionCache) {
    if (sessionCache.get(CURRENT_REQUESTER_KEY) == null) {
      throw new IllegalArgumentException(
          "Attempt to set a non session cache as the current session cache");
    }
    CacheServiceProvider.getRequestCacheService().put(CURRENT_SESSION_KEY, sessionCache);
  }

  /**
   * Gets the cache mapped with the current user session.
   * @return the current session cache.
   */
  public SimpleCacheService getCurrentSessionCache() {
    return CacheServiceProvider.getRequestCacheService()
        .get(CURRENT_SESSION_KEY, InMemoryCacheService.class);
  }

  /**
   * Gets the user for whom the specified session cache has been created.
   * @return the user to whom the specified session cache belongs.
   * @throws IllegalArgumentException if the specified cache isn't a session cache correctly
   * initialized.
   */
  public User getUser(SimpleCacheService sessionCache) {
    User user = sessionCache.get(CURRENT_REQUESTER_KEY, User.class);
    if (user == null) {
      throw new IllegalArgumentException("A non session cache is passed as argument");
    }
    return user;
  }
}
  