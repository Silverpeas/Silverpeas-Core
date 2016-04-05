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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Yohann Chastagnier
 */
public class MapUtil {

  /**
   * Centralizes the map adding that containing collections
   *
   * @param <K>
   * @param <V>
   * @param map
   * @param key
   * @param values
   * @return
   */
  public static <K, V> Collection<V> putAddAll(final Class<? extends Collection> collectionClass,
      Map<K, Collection<V>> map, final K key, final Collection<V> values) {
    Collection<V> result = null;

    if (values != null && !values.isEmpty()) {
      for (V value : values) {
        result = putAdd(collectionClass, map, key, value);
      }
    } else {
      result = putAdd(collectionClass, map, key, null);
    }

    // map result (never null)
    return result;
  }

  /**
   * Centralizes the map adding that containing collections
   *
   * @param <K>
   * @param <V>
   * @param map
   * @param key
   * @param value
   * @return
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public static <K, V> Collection<V> putAdd(final Class<? extends Collection> collectionClass,
      Map<K, Collection<V>> map, final K key, final V value) {

    if (map == null) {
      map = new LinkedHashMap<K, Collection<V>>();
    }

    // Old value
    Collection<V> result = map.get(key);
    if (result == null) {
      try {
        result = collectionClass.newInstance();
      } catch (final Exception myException) {
        throw new IllegalArgumentException(myException);
      }
      map.put(key, result);
    }

    // adding the value
    result.add(value);

    // map result
    return result;
  }

  /**
   * Centralizes the map adding that containing list collections
   *
   * @param <K>
   * @param <V>
   * @param map
   * @param key
   * @param values
   * @return
   */
  public static <K, V> List<V> putAddAllList(Map<K, List<V>> map, final K key,
      final Collection<V> values) {
    return putAddAllList(ArrayList.class, map, key, values);
  }

  /**
   * Centralizes the map adding that containing list collections
   * @param <K>
   * @param <V>
   * @param map
   * @param key
   * @param value
   * @return
   */
  public static <K, V> List<V> putAddList(Map<K, List<V>> map, final K key, final V value) {
    return putAddList(ArrayList.class, map, key, value);
  }

  /**
   * Centralizes the map adding that containing list collections
   * @param <K>
   * @param <V>
   * @param map
   * @param key
   * @param values
   * @return
   */
  public static <K, V> Set<V> putAddAllSet(Map<K, Set<V>> map, final K key,
      final Collection<V> values) {
    return putAddAllSet(HashSet.class, map, key, values);
  }

  /**
   * Centralizes the map adding that containing set collections
   * @param <K>
   * @param <V>
   * @param map
   * @param key
   * @param value
   * @return
   */
  public static <K, V> Set<V> putAddSet(Map<K, Set<V>> map, final K key, final V value) {
    return putAddSet(HashSet.class, map, key, value);
  }

  /**
   * Centralizes the map adding that containing list collections
   * @param <K>
   * @param <V>
   * @param listClass
   * @param map
   * @param key
   * @param values
   * @return
   */
  public static <K, V> List<V> putAddAllList(final Class<? extends List> listClass,
      Map<K, List<V>> map, final K key, final Collection<V> values) {
    List<V> result = null;

    if (values != null && !values.isEmpty()) {
      for (V value : values) {
        result = putAddList(listClass, map, key, value);
      }
    } else {
      result = putAddList(listClass, map, key, null);
    }

    // map result (never null)
    return result;
  }

  /**
   * Centralizes the map adding that containing list collections
   *
   * @param <K>
   * @param <V>
   *   @param listClass
   * @param map
   * @param key
   * @param value
   * @return
   */
  @SuppressWarnings("unchecked")
  public static <K, V> List<V> putAddList(final Class<? extends List> listClass,
      Map<K, List<V>> map, final K key, final V value) {

    if (map == null) {
      map = new LinkedHashMap<K, List<V>>();
    }

    // Old value
    List<V> result = map.get(key);
    if (result == null) {
      try {
        result = listClass.newInstance();
      } catch (final Exception myException) {
        throw new IllegalArgumentException(myException);
      }
      map.put(key, result);
    }

    // adding the value
    result.add(value);

    // map result
    return result;
  }

  /**
   * Centralizes the map adding that containing list collections
   * @param <K>
   * @param <V>
   * @param setClass
   * @param map
   * @param key
   * @param values
   * @return
   */
  public static <K, V> Set<V> putAddAllSet(final Class<? extends Set> setClass, Map<K, Set<V>> map,
      final K key, final Collection<V> values) {
    Set<V> result = null;

    if (values != null && !values.isEmpty()) {
      for (V value : values) {
        result = putAddSet(setClass, map, key, value);
      }
    } else {
      result = putAddSet(setClass, map, key, null);
    }

    // map result (never null)
    return result;
  }

  /**
   * Centralizes the map adding that containing set collections
   *
   * @param <K>
   * @param <V>
   * @param setClass
   * @param map
   * @param key
   * @param value
   * @return
   */
  @SuppressWarnings("unchecked")
  public static <K, V> Set<V> putAddSet(final Class<? extends Set> setClass, Map<K, Set<V>> map,
      final K key, final V value) {

    if (map == null) {
      map = new LinkedHashMap<K, Set<V>>();
    }

    // Old value
    Set<V> result = map.get(key);
    if (result == null) {
      try {
        result = setClass.newInstance();
      } catch (final Exception myException) {
        throw new IllegalArgumentException(myException);
      }
      map.put(key, result);
    }

    // adding the value
    result.add(value);

    // map result
    return result;
  }

  /**
   * Centralizes the map removing that containing list collections
   *
   * @param <K>
   * @param <V>
   * @param map
   * @param key
   * @param value
   * @return
   */
  public static <K, V> List<V> removeValueList(final Map<K, List<V>> map, final K key, final V value) {
    List<V> result = null;
    if (map != null) {
      // Old value
      result = map.get(key);
      if (result != null) {
        result.remove(value);
      }
    }

    // map result
    return result;
  }

  /**
   * Centralizes the map removing that containing set collections
   *
   * @param <K>
   * @param <V>
   * @param map
   * @param key
   * @param value
   * @return
   */
  public static <K, V> Set<V> removeValueSet(final Map<K, Set<V>> map,
      final K key, final V value) {

    Set<V> result = null;
    if (map != null) {

      // Old value
      result = map.get(key);
      if (result != null) {
        result.remove(value);
      }
    }

    // map result
    return result;
  }

  public static <K, V> boolean equals(Map<? extends K, ? extends V> left,
      Map<? extends K, ? extends V> right) {
    Map<K, V> onlyOnRight = new HashMap<K, V>(right);
    for (Map.Entry<? extends K, ? extends V> entry : left.entrySet()) {
      K leftKey = entry.getKey();
      V leftValue = entry.getValue();
      if (right.containsKey(leftKey)) {
        V rightValue = onlyOnRight.remove(leftKey);
        if (!Objects.equals(leftValue, rightValue)) {
          return false;
        }
      } else {
        return false;
      }
    }
    return onlyOnRight.isEmpty();
  }
}
