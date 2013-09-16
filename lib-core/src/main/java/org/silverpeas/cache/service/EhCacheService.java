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
package org.silverpeas.cache.service;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;

import java.util.UUID;

/**
 * Implementation of the CacheService that uses EhCache API.
 * User: Yohann Chastagnier
 * Date: 11/09/13
 */
public class EhCacheService implements CacheService {

  private final static String CACHE_NAME = "SILVERPEAS_COMMON_EH_CACHE";

  // In seconds, 12 hours (60seconds x 60minutes x 12hours)
  private final static int DEFAULT_TIME_TO_IDLE = 60 * 60 * 12;

  private CacheManager cacheManager;

  /**
   * Initialization of the service using EhCache API.
   * @param nbMaxElements
   */
  EhCacheService(int nbMaxElements) {
    cacheManager = CacheManager.getInstance();
    if (!cacheManager.cacheExists(CACHE_NAME)) {
      cacheManager.addCache(new Cache(new CacheConfiguration(CACHE_NAME, nbMaxElements)));
    } else {
      // Resizing dynamically the cache that already exists
      getCache().getCacheConfiguration().setMaxEntriesLocalHeap(nbMaxElements);
    }
  }

  /**
   * Gets the cache.
   * @return
   */
  Cache getCache() {
    return cacheManager.getCache(CACHE_NAME);
  }

  @Override
  public Object get(final Object key) {
    Element element = getCache().get(key);
    if (element == null) {
      return null;
    }
    return element.getObjectValue();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T get(final Object key, final Class<T> classType) {
    Object value = get(key);
    if (value == null || !classType.isAssignableFrom(value.getClass())) {
      return null;
    }
    return (T) value;
  }

  @Override
  public Object remove(final Object key) {
    Object value = get(key);
    if (value != null) {
      getCache().remove(key);
    }
    return value;
  }

  @Override
  public <T> T remove(final Object key, final Class<T> classType) {
    T value = get(key, classType);
    if (value != null) {
      getCache().remove(key);
    }
    return value;
  }

  @Override
  public String add(final Object value) {
    String uniqueKey = UUID.randomUUID().toString();
    put(uniqueKey, value);
    return uniqueKey;
  }

  @Override
  public String add(final Object value, final int timeToLive) {
    String uniqueKey = UUID.randomUUID().toString();
    put(uniqueKey, value, timeToLive);
    return uniqueKey;
  }

  @Override
  public String add(final Object value, final int timeToLive, final int timeToIdle) {
    String uniqueKey = UUID.randomUUID().toString();
    put(uniqueKey, value, timeToLive, timeToIdle);
    return uniqueKey;
  }

  @Override
  public void put(final Object key, final Object value) {
    put(key, value, 0);
  }

  @Override
  public void put(final Object key, final Object value, final int timeToLive) {
    put(key, value, timeToLive, DEFAULT_TIME_TO_IDLE);
  }

  @Override
  public void put(final Object key, final Object value, final int timeToLive,
      final int timeToIdle) {
    Element element = new Element(key, value);
    element.setTimeToLive(timeToLive);
    element.setTimeToIdle(timeToIdle);
    getCache().put(element);
  }
}
