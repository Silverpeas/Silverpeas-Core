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
package org.silverpeas.core.cache.service;

import org.silverpeas.core.cache.model.SimpleCache;

/**
 * An accessor to a specific kind of cache. In Silverpeas, several caches can be handled, each of
 * them with a different life-cycle scope. Each of those caches can be got with a specific accessor
 * that implements this interface. An accessor references only one cache and this is by the accessor
 * Silverpeas can get and use the cache.
 *
 * @author mmoquillon
 */
public interface CacheAccessor {

  /**
   * Gets the cache referred by this accessor.
   *
   * @param <T> the concrete type of {@link SimpleCache}
   * @return either a new cache or a single one according to the policy of the service about the
   * cache(s) on which it works.
   */
  <T extends SimpleCache> T getCache();

}
