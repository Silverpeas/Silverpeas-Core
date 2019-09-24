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
public interface SimpleCache {

  /**
   * Clear the content of the cache.
   */
  void clear();

  /**
   * Gets an element from the cache.
   * @param key the key with which the object to get is mapped in the cache.
   * @return the object mapped with the key or null if no there is no object mapped with the
   * specified key.
   */
  Object get(Object key);

  /**
   * Is this cache contains an object mapped with the specified key? It uses the
   * {@link SimpleCache#get(Object)} method for doing.
   * @param key the key with which the object to get is mapped in the cache.
   * @return true if there is an mapped with the key. False otherwise.
   * @see SimpleCache#get(Object)
   */
  default boolean has(Object key) {
    return get(key) == null;
  }

  /**
   * Gets a typed element from the cache.
   * Null is returned if an element exists for the given key but the object doesn't satisfy the
   * expected type.
   * @param <T> the concrete type of the object to get.
   * @param key the key with which the object to get is mapped in the cache.
   * @param classType the class type the instance to get as to satisfy.
   * @return the object mapped with the key or null if either there is no object mapped with the
   * specified key or the object doesn't satisfy the expected class type.
   */
  <T> T get(Object key, Class<T> classType);

  /**
   * Gets a typed element from the cache and computes it if it does not yet exist.
   * If an element exists for the given key but the object type doesn't correspond, a new
   * computation is performed.
   * @param <T> the concrete type of the object to get.
   * @param key the key with which the object to get is mapped in the cache.
   * @param classType the class of the instance to get.
   * @param valueSupplier the function that will computes the data to put into the cache.
   * @return the object mapped with the key or null if no there is no object mapped with the
   * specified key.
   */
  <T> T computeIfAbsent(Object key, Class<T> classType, Supplier<T> valueSupplier);

  /**
   * Removes an element from the cache and return it.
   * @param key the key with which the object to get is mapped in the cache.
   * @return the object removed from the cache.
   */
  Object remove(Object key);

  /**
   * Removes a typed element from the cache and return it.
   * Null is returned if an element exists for the given key but the object type doesn't
   * correspond.
   * @param <T> the concrete type of the object to remove.
   * @param key the key with which the object to get is mapped in the cache.
   * @param classType the class of the instance to remove.
   * @return the object removed from the cache.
   */
  <T> T remove(Object key, Class<T> classType);

  /**
   * Adds a value and generate a unique key to retrieve later the value.
   * After 12 hours without be used the value is trashed.
   * @param value the object to add in the cache.
   * @return the key with which the added object is mapped in the cache.
   */
  String add(Object value);

  /**
   * Puts a value for a given key.
   * After 12 hours without be used the value is trashed.
   * @param key the key with which the object to put has to be mapped
   * @param value the object to put in the cache.
   */
  void put(Object key, Object value);
}
