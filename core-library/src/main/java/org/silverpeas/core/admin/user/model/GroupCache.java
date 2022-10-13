/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.admin.user.model;

import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.annotation.Technical;

import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Cache of user groups.
 */
@Technical
@Bean
@Singleton
public class GroupCache {

  private final ConcurrentMap<String, List<String>> map = new ConcurrentHashMap<>();

  /**
   * Clears the cache.
   */
  public synchronized void clearCache() {
    map.clear();
  }

  /**
   * Gets all the groups in which the specified user is part of.
   * @param userId the unique identifier of a user.
   * @return a list of group identifiers.
   */
  public Optional<List<String>> getAllGroupIdsOfUser(String userId) {
    return Optional.ofNullable(map.get(userId));
  }

  /**
   * Sets the specified groups as being those in which the given user is part of. If the user has
   * already a list of groups set, then nothing is done and that list is returned. Otherwise, the
   * groups are cached as being the groups in which the user is part of and then returned.
   * @param userId the unique identifier of a user.
   * @param groupIds a list with the unique identifier of the groups.
   * @return either the specified list of group identifiers or the one that is already cached.
   */
  public List<String> setAllGroupIdsOfUser(String userId, List<String> groupIds) {
    Objects.requireNonNull(groupIds);
    return map.putIfAbsent(userId, groupIds);
  }

  /**
   * Removes from the cache the specified user and therefore the groups in which he's part of.
   * @param userId the unique identifier of a user.
   */
  public void removeCacheOfUser(String userId) {
    map.remove(userId);
  }

}
