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
package com.silverpeas.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * @author Yohann Chastagnier
 */
public class CollectionUtil {

  /**
   * Reverse the given list and returns it.
   * @param list
   * @param <T>
   * @return
   */
  public static <T> List<T> reverse(List<T> list) {
    Collections.reverse(list);
    return list;
  }

  /**
   * Checks if the given collection is not instancied or empty
   * @param <T>
   * @param collection
   * @return
   */
  public static <T> boolean isEmpty(final Collection<T> collection) {
    return !isNotEmpty(collection);
  }

  /**
   * Checks if the given collection is instancied and not empty
   * @param <T>
   * @param collection
   * @return
   */
  public static <T> boolean isNotEmpty(final Collection<T> collection) {
    return collection != null && !collection.isEmpty();
  }

  /**
   * Splits a collection into several collections. (Particularly useful for limitations of database
   * around the "in" clause)
   * @param collection
   * @return
   */
  public static <T> Collection<Collection<T>> split(final Collection<T> collection) {
    return split(collection, 500);
  }

  /**
   * Splits a collection into several collections. (Particularly useful for limitations of database
   * around the "in" clause)
   * @param collection
   * @param collectionSizeMax
   * @return
   */
  @SuppressWarnings("unchecked")
  public static <T> Collection<Collection<T>> split(final Collection<T> collection,
      final int collectionSizeMax) {
    Collection<Collection<T>> result = null;

    try {
      if (isNotEmpty(collection)) {
        if (collectionSizeMax > 0 && collection.size() > collectionSizeMax) {

          // Guessing the result size and initializing the result
          int size = (collection.size() / collectionSizeMax);
          if ((collection.size() % collectionSizeMax) != 0) {
            size++;
          }
          result = new ArrayList<Collection<T>>(size);

          // Browsing the collection
          Collection<T> curLot = null;
          for (final T element : collection) {

            // If necessary, initializing a lot
            if (curLot == null || curLot.size() >= collectionSizeMax) {
              curLot = new ArrayList<T>(collectionSizeMax);

              // Adding the new lot
              result.add(curLot);
            }

            // Adding an element into the current lot
            curLot.add(element);
          }
        } else {
          result = Collections.singletonList(collection);
        }
      }
    } catch (final Exception e) {
      throw new RuntimeException(e);
    } finally {
      if (result == null) {
        result = new ArrayList<Collection<T>>();
      }
    }

    // Retour du resultat
    return result;
  }

  /**
   * Transforming a collection into a map
   * @param <T> collection type elements
   * @param <K> map type key
   * @param <V> map type value
   * @param collection
   * @param extractor extractor interface
   * @return a map initialized from a list by an extractor
   */
  public static <T extends Object, K extends Object, V extends Object> HashMap<K, V> listToMap(
      final Collection<T> collection, final ExtractionList<T, K, V> extractor) {
    final LinkedHashMap<K, V> result;
    if (collection == null) {
      result = null;
    } else if (collection.isEmpty()) {
      result = new LinkedHashMap<K, V>();
    } else {
      result = new LinkedHashMap<K, V>((int) (collection.size() * 0.75f));
      if (extractor instanceof ExtractionComplexList<?, ?, ?>) {
        ((ExtractionComplexList<T, K, V>) extractor).setMap(result);
      }
      for (final T toPerform : collection) {
        result.put(extractor.getKey(toPerform), extractor.getValue(toPerform));
      }
    }
    return result;
  }

  public static <T> List<T> asList(T... values) {
    return new ArrayList<T>(Arrays.asList(values));
  }

  public static <T> Set<T> asSet(T... values) {
    return new HashSet<T>(Arrays.asList(values));
  }

  /**
   * Null elements are not taking into account.
   * @see Collections#addAll(java.util.Collection, Object[])
   */
  public static <T> boolean addAllIgnoreNull(Collection<? super T> c, T... elements) {
    boolean result = false;
    for (T element : elements) {
      if (element != null) {
        result |= c.add(element);
      }
    }
    return result;
  }
}
