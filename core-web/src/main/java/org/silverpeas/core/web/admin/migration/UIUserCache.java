/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.web.admin.migration;

import org.silverpeas.core.admin.user.model.User;

import static org.silverpeas.core.cache.service.CacheServiceProvider.getRequestCacheService;

/**
 * Handles a cache of user dedicated to the UI.<br>
 * When a user is not yet into the cache, then it is loaded from the persistence and put into cache.
 * @author silveryocha
 */
public class UIUserCache {

  private static final String CACHE_KEY_PREFIX = UIUserCache.class.getSimpleName() + "###";

  private UIUserCache() {
    throw new IllegalAccessError("Utility class");
  }

  /**
   * Gets from the dedicated UI cache a user by its identifier.
   * @param id an indetifier of a user.
   * @return a user if any, null otherwise.
   */
  public static User getById(final String id) {
    final String cacheKey = CACHE_KEY_PREFIX + id;
    return getRequestCacheService().getCache()
        .computeIfAbsent(cacheKey, User.class, () -> id != null ? User.getById(id) : null);
  }
}
