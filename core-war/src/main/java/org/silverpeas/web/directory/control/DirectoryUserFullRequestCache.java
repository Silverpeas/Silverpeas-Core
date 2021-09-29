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

package org.silverpeas.web.directory.control;

import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.web.directory.model.UserItem;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class permits to handles {@link UserFull} caches.
 * <p> Callers puts in a first time all the Silverpeas's user ids, and then, on the first
 * {@link UserFull} data access, all the data are retrieved in a single repository request. </p>
 * @author silveryocha
 */
public class DirectoryUserFullRequestCache {

  public static DirectoryUserFullRequestCache get() {
    return CacheServiceProvider.getRequestCacheService()
        .getCache()
        .computeIfAbsent(DirectoryUserFullRequestCache.class.getName(),
            DirectoryUserFullRequestCache.class, DirectoryUserFullRequestCache::new);
  }

  private final Set<String> waitingIds = Collections.synchronizedSet(new HashSet<>());
  private final Map<String, UserFull> cache = Collections.synchronizedMap(new HashMap<>());

  private DirectoryUserFullRequestCache() {
    // hidden constructor
  }

  /**
   * Adds a user handled by the cache.
   * @param user a {@link UserItem} instance.
   */
  public void addUserItem(final UserItem user) {
    final String originalId = user.getOriginalId();
    if (!cache.containsKey(originalId)) {
      waitingIds.add(originalId);
    }
  }

  /**
   * Gets the full user data of the user represented by the given item.
   * @param user a user item.
   * @return a {@link UserFull} if any, null otherwise.
   */
  public UserFull getUserFull(final UserItem user) {
    return getUserFull(user.getOriginalId());
  }

  private UserFull getUserFull(final String userId) {
    synchronized (waitingIds) {
      if (!waitingIds.isEmpty()) {
        UserFull.getByIds(waitingIds).forEach(u -> cache.put(u.getId(), u));
        waitingIds.clear();
      }
    }
    return cache.get(userId);
  }
}
