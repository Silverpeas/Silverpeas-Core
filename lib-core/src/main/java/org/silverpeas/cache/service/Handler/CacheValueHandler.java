/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.cache.service.Handler;

import org.silverpeas.cache.service.CacheServiceFactory;
import org.silverpeas.cache.service.SimpleCacheService;

import java.lang.reflect.ParameterizedType;

/**
 * The aim of this abstraction is to factorize the treatment of simple uses of cache.
 * In most cases, the use of the cache is :
 * - 1 : checking that a value exists from the cache behind a key
 * - 2 : if it exists, using it
 * - 3 : if it doesn't exist, computing the value to be cached, caching it and using it
 * User: Yohann Chastagnier
 * Date: 30/12/13
 */
public abstract class CacheValueHandler<V> {

  private final Class<V> valueClass;

  /**
   * Default constructor.
   */
  protected CacheValueHandler() {
    try {
      //noinspection unchecked
      valueClass = ((Class<V>) ((ParameterizedType) this.getClass().
          getGenericSuperclass()).getActualTypeArguments()[0]);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Gets the cache service.
   * @param <C>
   * @return
   */
  protected abstract <C extends SimpleCacheService> C getCacheService();

  /**
   * Gets the suffix key to access the value in the cache.
   * The prefix of key is the name of the class (package name included) of the handler.
   * @return
   */
  protected abstract String getCacheKeySuffix();

  /**
   * Gets the value from the cache.
   * @return
   */
  public V get() {
    String cacheKey = getClass().getName() + "_" + getCacheKeySuffix();
    V value = CacheServiceFactory.getRequestCacheService().get(cacheKey, valueClass);
    if (value == null) {
      value = defaultValue();
      // Nothing is cached if value is null here
      if (value != null) {
        CacheServiceFactory.getRequestCacheService().put(cacheKey, value);
      }
    }
    return value;
  }

  /**
   * Provides a default value to be registred in the cache.
   * If default value is null, then nothing is cached.
   * @return
   */
  protected abstract V defaultValue();
}
