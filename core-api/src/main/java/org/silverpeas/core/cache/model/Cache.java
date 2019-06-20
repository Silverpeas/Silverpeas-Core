/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.cache.model;

import java.util.function.Supplier;

/**
 * User: Yohann Chastagnier
 * Date: 11/09/13
 */
public interface Cache extends SimpleCache {

  /**
   * Adds a value and generates a unique mapping key to be used to retrieve later the value.
   * @param value an object to add into the cache
   * @param timeToLive the time to live in seconds of the value in the cache. After this time,
   * the value expires and consequently it is removed from the cache. 0 = unlimited.
   * @return the key to which the added value is mapped in the cache
   */
  String add(Object value, int timeToLive);

  /**
   * Adds a value and generate a unique mapping key to be used to retrieve later the value.
   * When both the time to live and the time to idle are set, the lifetime of the value in the
   * cache is initially bounded by the time to live except if the lifetime computed from the time
   * to live at the last access of the value (access time plus the
   * time to idle) exceeds this initial lifetime.
   * @param value the object to add in the cache.
   * @param timeToLive the time to live in seconds of the object in the cache. The time to live
   * can be exceeded if the time to idle is set and the value is accessed after the time to live
   * minus the time to idle. 0 = unlimited.
   * @param timeToIdle the time to idle in seconds of the object in the cache between two accesses.
   * With the time to live set, the time to idle is taken into account only once the time starting
   * at the access time exceed the initial lifetime of the value computed from the time to live.
   * After this time, the value expires and consequently it is remove from the cache. 0 = unlimited.
   * @return the key to which the added object is mapped in the cache
   */
  String add(Object value, int timeToLive, int timeToIdle);

  /**
   * Puts a new value for the given key and updates its time to live.
   * @param key the key with which the object to put has to be mapped.
   * @param value the object to put in the cache.
   * @param timeToLive the time to live in seconds of the value in the cache. After this time,
   * the value expires and consequently it is removed from the cache. 0 = unlimited.
   */
  void put(Object key, Object value, int timeToLive);

  /**
   * Puts a new value for the given key and updates both its time to live and its time to idle.
   * When both the time to live and the time to idle are set, the lifetime of the value in the
   * cache is initially bounded by the time to live except if the lifetime computed from the time
   * to live at the last access of the value (access time plus the
   * time to idle) exceeds this initial lifetime.
   * @param key the key with which the object to put has to be mapped.
   * @param value the object to put in the cache.
   * @param timeToLive the time to live in seconds of the object in the cache. The time to live
   * can be exceeded if the time to idle is set and the value is accessed after the time to live
   * minus the time to idle. 0 = unlimited.
   * @param timeToIdle the time to idle in seconds of the object in the cache between two accesses.
   * With the time to live set, the time to idle is taken into account only once the time starting
   * at the access time exceed the initial lifetime of the value computed from the time to live.
   * After this time, the value expires and consequently it is remove from the cache. 0 = unlimited.
   */
  void put(Object key, Object value, int timeToLive, int timeToIdle);

  /**
   * Gets a typed element from the cache and computes it if it does not yet exist.
   * If an element exists for the given key but the object type doesn't correspond, a new
   * computation is performed.
   * @param <T> the concrete type of the object to get.
   * @param key the key with which the object to get is mapped in the cache.
   * @param timeToLive the time to live in seconds of the object in the cache. The time to live
   * can be exceeded if the time to idle is set and the value is accessed after the time to live
   * minus the time to idle. 0 = unlimited.
   * @param classType the class of the instance to get.
   * @param valueSupplier the function that will computes the data to put into the cache.
   * @return the object mapped with the key or null if no there is no object mapped with the
   * specified key.
   */
  <T> T computeIfAbsent(Object key, Class<T> classType, int timeToLive, Supplier<T> valueSupplier);

  /**
   * Gets a typed element from the cache and computes it if it does not yet exist.
   * If an element exists for the given key but the object type doesn't correspond, a new
   * computation is performed.
   * @param <T> the concrete type of the object to get.
   * @param key the key with which the object to get is mapped in the cache.
   * @param timeToLive the time to live in seconds of the object in the cache. The time to live
   * can be exceeded if the time to idle is set and the value is accessed after the time to live
   * minus the time to idle. 0 = unlimited.
   * @param timeToIdle the time to idle in seconds of the object in the cache between two accesses.
   * With the time to live set, the time to idle is taken into account only once the time starting
   * at the access time exceed the initial lifetime of the value computed from the time to live.
   * After this time, the value expires and consequently it is remove from the cache. 0 = unlimited.
   * @param classType the class of the instance to get.
   * @param valueSupplier the function that will computes the data to put into the cache.
   * @return the object mapped with the key or null if no there is no object mapped with the
   * specified key.
   */
  <T> T computeIfAbsent(Object key, Class<T> classType, int timeToLive, int timeToIdle, Supplier<T> valueSupplier);
}
