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
public interface CacheService extends SimpleCacheService {

  /**
   * Adds a value and generate a unique key to retrieve later the value.
   * After the given time, the value is trashed.
   * @param value
   * @param timeToLive 0 = unlimited
   * @return
   */
  String add(Object value, int timeToLive);

  /**
   * Adds a value and generate a unique key to retrieve later the value.
   * After the given live time, the value is trashed.
   * After the given idle time, the value is trashed.
   * @param value
   * @param timeToLive 0 = unlimited
   * @param timeToIdle 0 = unlimited
   * @return
   */
  String add(Object value, int timeToLive, int timeToIdle);

  /**
   * Puts a value for a given key.
   * After the given time, the value is trashed.
   * @param key
   * @param value
   * @param timeToLive 0 = unlimited
   */
  void put(Object key, Object value, int timeToLive);

  /**
   * Puts a value for a given key.
   * After the given time, the value is trashed.
   * After the given idle time, the value is trashed.
   * @param key
   * @param value
   * @param timeToLive
   * @param timeToIdle 0 = unlimited
   */
  void put(Object key, Object value, int timeToLive, int timeToIdle);
}
