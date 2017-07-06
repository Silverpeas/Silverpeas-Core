/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
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

/**
 * User: Yohann Chastagnier
 * Date: 11/09/13
 */
public interface Cache extends SimpleCache {

  /**
   * Adds a value and generate a unique key to retrieve later the value.
   * After the given time, the value is trashed.
   * @param value an object to add into the cache
   * @param timeToLive 0 = unlimited
   * @return the key to which the added object is mapped in the cache
   */
  String add(Object value, int timeToLive);

  /**
   * Adds a value and generate a unique key to retrieve later the value.
   * After the given live time, the value is trashed.
   * After the given idle time, the value is trashed.
   * @param value the object to add in the cache.
   * @param timeToLive 0 = unlimited
   * @param timeToIdle 0 = unlimited
   * @return the key to which the added object is mapped in the cache
   */
  String add(Object value, int timeToLive, int timeToIdle);

  /**
   * Puts a value for a given key.
   * After the given time, the value is trashed.
   * @param key the key with which the object to add has to be mapped.
   * @param value the object to add in the cache.
   * @param timeToLive 0 = unlimited
   */
  void put(Object key, Object value, int timeToLive);

  /**
   * Puts a value for a given key.
   * After the given time, the value is trashed.
   * After the given idle time, the value is trashed.
   * @param key the key with which the object to add has to be mapped.
   * @param value the object to add in the cache.
   * @param timeToLive 0 = unlimited
   * @param timeToIdle 0 = unlimited
   */
  void put(Object key, Object value, int timeToLive, int timeToIdle);
}
