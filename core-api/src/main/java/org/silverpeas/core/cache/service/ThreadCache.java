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
package org.silverpeas.core.cache.service;

import org.silverpeas.core.cache.model.AbstractSimpleCache;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Yohann Chastagnier
 * Date: 25/10/13
 */
class ThreadCache extends AbstractSimpleCache {

  private final ThreadLocal<Map<Object, Object>> cache = new ThreadLocal<>();

  /**
   * Gets the cache.
   * @return the underlying cache used for its implementation.
   */
  protected Map<Object, Object> getCache() {
    Map<Object, Object> threadCache = cache.get();
    if (threadCache == null) {
      threadCache = new HashMap<>();
      cache.set(threadCache);
    }
    return threadCache;
  }

  @Override
  public void clear() {
    cache.remove();
  }

  @Override
  public Object get(final Object key) {
    return getCache().get(key);
  }

  @Override
  public Object remove(final Object key) {
    Object value = get(key);
    removeIfPresent(key, value);
    return value;
  }

  @Override
  public <T> T remove(final Object key, final Class<T> classType) {
    T value = get(key, classType);
    removeIfPresent(key, value);
    return value;
  }

  @Override
  public void put(final Object key, final Object value) {
    getCache().put(key, value);
  }

  private void removeIfPresent(Object key, Object value) {
    if (value != null) {
      getCache().remove(key);
    }
  }
}
