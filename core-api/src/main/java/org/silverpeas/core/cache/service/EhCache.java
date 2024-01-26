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
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.builders.UserManagedCacheBuilder;
import org.ehcache.expiry.ExpiryPolicy;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.cache.model.ExternalCache;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Implementation of the external cache by using the EhCache API. This cache is loaded and used by
 * the {@link org.silverpeas.kernel.cache.service.ApplicationCacheAccessor} through the Java SPI
 * mechanism.
 *
 * @author Yohann Chastagnier
 * @see ExternalCache
 * @see org.silverpeas.kernel.cache.service.ApplicationCacheAccessor
 */
public final class EhCache extends ExternalCache {

  private final UserManagedCache<Object, Element> managedCache;

  /**
   * Constructs the EhCache cache. The maximum number of elements the cache can be contained is
   * provided by the property {@code application.cache.common.nbMaxElements} in the general
   * Silverpeas settings. This method is dedicated to be used by the Java SPI mechanism.
   */
  @SuppressWarnings("unused")
  public EhCache() {
    int nbMaxElements = ResourceLocator.getGeneralSettingBundle().
        getInteger("application.cache.common.nbMaxElements", 0);
    managedCache = createCache(Math.max(nbMaxElements, 0));
  }

  /**
   * Constructs the EhCache cache by initializing it with the specified maximum number of elements.
   *
   * @param elementsMaxNb maximum capacity of the cache.
   */
  EhCache(long elementsMaxNb) {
    managedCache = createCache(elementsMaxNb);
  }

  private UserManagedCache<Object, Element> createCache(long elementsMaxNb) {
    var cacheBuilder =
        UserManagedCacheBuilder.newUserManagedCacheBuilder(Object.class, Element.class)
            .withExpiry(new PerElementExpiration());
    return (elementsMaxNb > 0 ?
        cacheBuilder.withResourcePools(ResourcePoolsBuilder.heap(elementsMaxNb)) :
        cacheBuilder).build(true);
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

  @Override
  public Map<Object, Object> getAll() {
    Map<Object, Object> entries = new HashMap<>();
    getCache().iterator()
        .forEachRemaining(e -> entries.put(e.getKey(), e.getValue().getObjectValue()));
    return entries;
  }

  /**
   * An element in the cache. It decorated any value to put into the cache with TTL and TTI
   * information.
   */
  static class Element {
    private final Object value;
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
  private static class PerElementExpiration implements ExpiryPolicy<Object, Element> {

    @Override
    public Duration getExpiryForCreation(final Object key, final Element value) {
      Duration expiration;
      if (value.getTimeToLive() <= 0 && value.getTimeToIdle() > 0) {
        expiration = Duration.ofSeconds(value.getTimeToIdle());
      } else if (value.getTimeToLive() > 0) {
        expiration = Duration.ofSeconds(value.getTimeToLive());
      } else {
        expiration = ExpiryPolicy.INFINITE;
      }
      return expiration;
    }

    @Override
    public Duration getExpiryForAccess(final Object key, final Supplier<? extends Element> value) {
      Duration expiration = null;
      Element element = value.get();
      if (element.getTimeToIdle() > 0) {
        expiration = Duration.ofSeconds(element.getTimeToIdle());
      }
      return expiration;
    }

    @Override
    public Duration getExpiryForUpdate(final Object key, final Supplier<? extends Element> oldValue,
        final Element newValue) {
      return getExpiryForCreation(key, newValue);
    }
  }
}
