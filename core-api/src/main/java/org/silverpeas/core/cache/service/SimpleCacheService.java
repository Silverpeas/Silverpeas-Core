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
package org.silverpeas.core.cache.service;

/**
 * User: Yohann Chastagnier
 * Date: 11/09/13
 */
public interface SimpleCacheService {

  /**
   * Clear the content of the cache.
   */
  void clear();

  /**
   * Gets an element from the cache.
   * @param key
   * @return
   */
  Object get(Object key);

  /**
   * Gets a typed element from the cache.
   * Null is returned if an element exists for the given key but the object type doesn't
   * correspond.
   * @param key
   * @param classType
   * @param <T>
   * @return
   */
  <T> T get(Object key, Class<T> classType);

  /**
   * Removes an element from the cache and return it.
   * @param key
   * @return
   */
  Object remove(Object key);

  /**
   * Removes a typed element from the cache and return it.
   * Null is returned if an element exists for the given key but the object type doesn't
   * correspond.
   * @param key
   * @param classType
   * @param <T>
   * @return
   */
  <T> T remove(Object key, Class<T> classType);

  /**
   * Adds a value and generate a unique key to retrieve later the value.
   * After 12 hours without be used the value is trashed.
   * @param value
   * @return
   */
  String add(Object value);

  /**
   * Puts a value for a given key.
   * After 12 hours without be used the value is trashed.
   * @param key
   * @param value
   */
  void put(Object key, Object value);
}
