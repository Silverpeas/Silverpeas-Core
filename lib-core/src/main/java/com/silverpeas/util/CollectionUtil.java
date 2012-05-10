/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.silverpeas.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;

/**
 * @author Yohann Chastagnier
 */
public class CollectionUtil {

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
    return collection != null && collection.size() > 0;
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
        result.put(extractor.getKey(toPerform), extractor
            .getValue(toPerform));
      }
    }
    return result;
  }

  /**
   * Extracting a property.
   * @param aClass property class to extract
   * @param collection collection from that to extract
   * @param propertyName name of the property
   * @return a collection with requested elements
   */
  public static <T> Collection<T> extractFrom(final Class<T> aClass,
      final Collection<?> collection, final String propertyName) {
    return extractFrom(collection, propertyName, false, 0);
  }

  /**
   * Extracting a property.
   * @param collection collection from that to extract
   * @param propertyName name of the property
   * @return a collection with requested elements
   */
  public static <T> Collection<T> extractFrom(final Collection<?> collection,
      final String propertyName) {
    return extractFrom(collection, propertyName, false, 0);
  }

  /**
   * Extracting a property.
   * @param collection collection from that to extract
   * @param propertyName name of the property
   * @param isListOfArray indicates if the list is an array list
   * @param numberColumn column index to extract from the array
   * @return a collection with requested elements
   */
  @SuppressWarnings({ "unchecked" })
  public static <T> Collection<T> extractFrom(final Collection<?> collection,
      final String propertyName, final boolean isListOfArray,
      final int numberColumn) {
    Set<T> result = null;
    if (collection != null) {
      result = new HashSet<T>(collection.size());
      if (!isListOfArray) {
        for (final Object object : collection) {
          result.add((T) getPropertyAsObject(object,
              propertyName));
        }
      } else {
        for (final Object[] myObject : (Collection<Object[]>) collection) {
          result.add((T) myObject[numberColumn]);
        }
      }
    }
    return result;
  }

  /**
   * Extracting a property from elements that each has an other property with a given value
   * @param class property class to extract
   * @param collection element collection
   * @param propertyNameToExtract name property to extract
   * @param propertyNameToCompare name value to compare
   * @param givenValueToCompare value to compare
   * @param nullValueExtracted null value extracted
   * @return a bean property collection
   */
  @SuppressWarnings({ "unchecked" })
  public static <T> Collection<T> extractFrom(final Class<T> aClass,
      final Collection<?> collection, final String propertyNameToExtract,
      final String propertyNameToCompare, final Object givenValueToCompare,
      final boolean nullValueExtracted) {
    Set<T> result = null;
    if (collection != null) {
      result = new HashSet<T>(collection.size());
      Object valueToExtract;
      Object valueToCompare;
      for (final Object element : collection) {
        valueToCompare = getPropertyAsObject(element,
            propertyNameToCompare);
        if ((givenValueToCompare == null && valueToCompare == null)
            || (givenValueToCompare != null && givenValueToCompare
                .equals(valueToCompare))) {
          valueToExtract = getPropertyAsObject(element,
              propertyNameToExtract);
          if (valueToExtract != null || nullValueExtracted) {
            result.add((T) valueToExtract);
          }
        }
      }
    }
    return result;
  }

  /**
   * Value converted from a bean property.
   * @param bean bean
   * @param propertyName
   */
  private static synchronized Object getPropertyAsObject(final Object bean,
      final String propertyName) {
    // Synchronized as PropertyEditor is not thread-safe.
    Object property = null;
    try {
      property = PropertyUtils.getProperty(bean, propertyName);
    } catch (final IllegalAccessException e) {
      throw new RuntimeException(e.getMessage(), e);
    } catch (final InvocationTargetException e) {
      throw new RuntimeException(e.getMessage(), e);
    } catch (final NoSuchMethodException e) {
      throw new RuntimeException(e.getMessage(), e);
    } catch (final NestedNullException e) {
      // In the case of a.b, with a == null, null is returned
      property = null;
    }
    if (property == null) {
      return null;
    }
    return property;
  }
}
