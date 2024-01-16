/*
 * Copyright (C) 2000 - 2024 Silverpeas
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

import org.ehcache.Cache;
import org.ehcache.UserManagedCache;
import org.ehcache.ValueSupplier;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.builders.UserManagedCacheBuilder;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expiry;
import org.silverpeas.core.cache.model.AbstractCache;

import java.util.Objects;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Implementation of the Cache that uses EhCache API.
 * User: Yohann Chastagnier Date: 11/09/13
 */
final class EhCache extends AbstractCache {

  private final UserManagedCache<Object, Element> managedCache;

  /**
   * Initialization of the service using EhCache API.
   *
   * @param nbMaxElements maximum capacity of the cache.
   */
  @SuppressWarnings("unchecked")
  EhCache(long nbMaxElements) {
    UserManagedCacheBuilder cacheBuilder =
        UserManagedCacheBuilder.newUserManagedCacheBuilder(Object.class, Element.class)
            .withExpiry(new PerElementExpiration());
    if (nbMaxElements > 0) {
      cacheBuilder = cacheBuilder.withResourcePools(ResourcePoolsBuilder.heap(nbMaxElements));
    }
    managedCache = cacheBuilder.build(true);
  }

  /**
   * Gets the cache.
   *
   * @return the underlying cache used for its implementation.
   */
  Cache<Object, Element> getCache() {
    return managedCache;
  }

  @Override
  public void clear() {
    getCache().clear();
  }

  @Override
  public Object get(final Object key) {
    Element element = getCache().get(key);
    if (element == null) {
      return null;
    }
    return element.getObjectValue();
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
    T value = null;
    Element element = getCache().get(key);
    if (element != null) {
      value = element.getValue(classType);
      if (value != null) {
        getCache().remove(key);
      }
    }
    return value;
  }

  @Override
  public void put(final Object key, final Object value, final int timeToLive,
      final int timeToIdle) {
    Element element = new Element(value)
        .withTimeToLive(timeToLive)
        .withTimeToIdle(timeToIdle);
    getCache().put(key, element);
  }

  @Override
  public <T> T computeIfAbsent(final Object key, final Class<T> classType, final int timeToLive,
      final int timeToIdle, final Supplier<T> valueSupplier) {
    Objects.requireNonNull(valueSupplier);
    T value = get(key, classType);
    if (value == null) {
      value = valueSupplier.get();
      put(key, value, timeToLive, timeToIdle);
    }
    return value;
  }

  /**
   * An element in the cache. It decorated any value to put into the cache with TTL and TTI
   * information.
   */
  static class Element {
    private Object value;
    private int ttl;
    private int tti;

    public Element(final Object value) {
      this.value = value;
    }

    Element withTimeToLive(int ttl) {
      this.ttl = ttl;
      return this;
    }

    Element withTimeToIdle(int tti) {
      this.tti = tti;
      return this;
    }

    public Object getObjectValue() {
      return value;
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(Class<T> clazz) {
      if (value == null || !clazz.isAssignableFrom(value.getClass())) {
        return null;
      }
      return (T) value;
    }

    public int getTimeToLive() {
      return ttl;
    }

    public int getTimeToIdle() {
      return tti;
    }
  }

  /**
   * A custom expiration rule based upon both TTL and TTI for each element in the cache.
   */
  private class PerElementExpiration implements Expiry<Object, Element> {

    @Override
    public Duration getExpiryForCreation(final Object key, final Element value) {
      Duration expiration = Duration.INFINITE;
      if (value.getTimeToLive() <= 0 && value.getTimeToIdle() > 0) {
        expiration = Duration.of(value.getTimeToIdle(), SECONDS);
      } else if (value.getTimeToLive() > 0) {
        expiration = Duration.of(value.getTimeToLive(), SECONDS);
      }
      return expiration;
    }

    @Override
    public Duration getExpiryForAccess(final Object key,
        final ValueSupplier<? extends Element> value) {
      Duration expiration = null;
      Element element = value.value();
      if (element.getTimeToIdle() > 0) {
        expiration = Duration.of(element.getTimeToIdle(), SECONDS);
      }
      return expiration;
    }

    @Override
    public Duration getExpiryForUpdate(final Object key,
        final ValueSupplier<? extends Element> oldValue, final Element newValue) {
      Duration expiration = Duration.INFINITE;
      if (newValue.getTimeToLive() <= 0 && newValue.getTimeToIdle() > 0) {
        expiration = Duration.of(newValue.getTimeToIdle(), SECONDS);
      } else if (newValue.getTimeToLive() > 0) {
        expiration = Duration.of(newValue.getTimeToLive(), SECONDS);
      }
      return expiration;
    }
  }
}
