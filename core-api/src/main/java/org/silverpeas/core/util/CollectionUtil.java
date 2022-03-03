/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.util;

import org.silverpeas.core.SilverpeasRuntimeException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * @author Yohann Chastagnier
 */
public class CollectionUtil {

  private static final int SPLIT_BATCH_SIZE = 500;

  private CollectionUtil() {
    throw new IllegalAccessError("Utility class");
  }


  /**
   * Reverse the given list and returns it.
   * @param <T> the type of the instance contained into the specified list
   * @param list the list to reverse
   * @return the specified list that has been reversed (same instance)
   */
  public static <T> List<T> reverse(List<T> list) {
    Collections.reverse(list);
    return list;
  }

  /**
   * Checks if the given collection is not instanced or empty
   * @param <T> the type of the instance contained into the specified collection
   * @param collection the collection to verify
   * @return true if specified collection is empty, false otherwise
   */
  public static <T> boolean isEmpty(final Collection<T> collection) {
    return !isNotEmpty(collection);
  }

  /**
   * Checks if the given collection is instanced and not empty
   * @param <T> the type of the instance contained into the specified collection
   * @param collection the collection to verify
   * @return true if specified collection is not empty, false otherwise
   */
  public static <T> boolean isNotEmpty(final Collection<T> collection) {
    return collection != null && !collection.isEmpty();
  }

  /**
   * Splits a collection into several collections. (Particularly useful for limitations of database
   * around the "in" clause)
   * @param <T> the concrete type of the items in the list.
   * @param collection the collection to split
   * @return the slices of the specified collection
   */
  @SuppressWarnings("unchecked")
  public static <T> List<List<T>> splitList(final List<T> collection) {
    return (List) split((Collection) collection, SPLIT_BATCH_SIZE);
  }

  /**
   * Splits a collection into several collections. (Particularly useful for limitations of database
   * around the "in" clause)
   * @param <T> the concrete type of the items in the collection.
   * @param collection the collection to split
   * @return the slices of the specified collection
   */
  public static <T> Collection<Collection<T>> split(final Collection<T> collection) {
    return split(collection, SPLIT_BATCH_SIZE);
  }

  /**
   * Splits a collection into several collections. (Particularly useful for limitations of database
   * around the "in" clause)
   * @param <T> the concrete type of the items in the collection.
   * @param collection the collection to split
   * @param collectionSizeMax the maximum elements in slice
   * @return the slices of the specified collection
   */
  @SuppressWarnings("unchecked")
  public static <T> Collection<Collection<T>> split(final Collection<T> collection,
      final int collectionSizeMax) {
    Collection<Collection<T>> result = null;

    try {
      if (isNotEmpty(collection)) {
        if (collectionSizeMax > 0 && collection.size() > collectionSizeMax) {

          // Guessing the result size and initializing the result
          int size = collection.size() / collectionSizeMax;
          if ((collection.size() % collectionSizeMax) != 0) {
            size++;
          }
          result = new ArrayList<>(size);

          // Browsing the collection
          Collection<T> curLot = null;
          for (final T element : collection) {

            // If necessary, initializing a lot
            if (curLot == null || curLot.size() >= collectionSizeMax) {
              curLot = new ArrayList<>(collectionSizeMax);

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
      throw new SilverpeasRuntimeException(e);
    } finally {
      if (result == null) {
        result = new ArrayList<>();
      }
    }

    // Returning the result
    return result;
  }

  @SafeVarargs
  public static <T> List<T> asList(T... values) {
    List<T> listWithValues = new ArrayList<>();
    Collections.addAll(listWithValues, values);
    return listWithValues;
  }

  @SafeVarargs
  public static <T> Set<T> asSet(T... values) {
    return new HashSet<>(Arrays.asList(values));
  }

  /**
   * Null elements are not taking into account.
   * @param <T> the concrete type of the items.
   * @param c collection in which the items will be added
   * @param elements the items to add
   * @return true of the items are added, false otherwise.
   * @see Collections#addAll(java.util.Collection, Object[])
   */
  @SafeVarargs
  public static <T> boolean addAllIgnoreNull(Collection<? super T> c, T... elements) {
    boolean result = false;
    for (T element : elements) {
      if (element != null) {
        result |= c.add(element);
      }
    }
    return result;
  }

  /**
   * Makes an union between both of the given lists.<br>
   * The result contains unique values.
   * @param list1 the first list.
   * @param list2 the second list.
   * @param <T> the type of the items in the list.
   * @return the union between the two lists.
   */
  @SuppressWarnings("unchecked")
  public static <T> List<T> union(List<T> list1, List<T> list2) {
    return new ArrayList<T>(union(list1, (Collection) list2));
  }

  /**
   * Makes an union between both of the given collections.<br>
   * The result contains unique values.
   * @param col1 the first collection.
   * @param col2 the second collection.
   * @param <T> the type of the items in the list
   * @return the union between the two collections.
   */
  public static <T> Collection<T> union(Collection<T> col1, Collection<T> col2) {
    Set<T> set = new LinkedHashSet<>();
    set.addAll(col1);
    set.addAll(col2);
    return set;
  }

  /**
   * Makes an intersection between both of the given lists.<br>
   * The result contains unique values.
   * @param list1 the first list.
   * @param list2 the second list.
   * @param <T> the type of the items in the list
   * @return the intersection between the two lists.
   */
  @SuppressWarnings("unchecked")
  public static <T> List<T> intersection(List<T> list1, List<T> list2) {
    return (List) intersection(list1, (Collection) list2);
  }

  /**
   * Makes an intersection between both of the given collections.<br>
   * The result contains unique values.
   * @param col1 the first collection.
   * @param col2 the second collection.
   * @param <T> the type of the items in the list
   * @return the intersection between the two collections.
   */
  public static <T> Collection<T> intersection(Collection<T> col1, Collection<T> col2) {
    return intersection(col1, col2, t -> t);
  }

  /**
   * Makes an intersection between both of the given lists.<br>
   * The result contains unique values.
   * @param list1 the first list.
   * @param list2 the second list.
   * @param discriminator get the discriminator data.
   * @param <T> the type of the items in the list
   * @return the intersection between the two lists.
   */
  @SuppressWarnings("unchecked")
  public static <T> List<T> intersection(List<T> list1, List<T> list2, Function<T, Object> discriminator) {
    return (List) intersection(list1, (Collection) list2, discriminator);
  }

  /**
   * Makes an intersection between both of the given collections.<br>
   * The result contains unique values.
   * @param col1 the first collection.
   * @param col2 the second collection.
   * @param discriminator get the discriminator data.
   * @param <T> the type of the items in the list
   * @return the intersection between the two collections.
   */
  public static <T> Collection<T> intersection(Collection<T> col1, Collection<T> col2,
      Function<T, Object> discriminator) {
    Collection<T> smaller = col1;
    Collection<T> larger = col2;
    if (col1.size() > col2.size()) {
      smaller = col2;
      larger = col1;
    }
    final Set<Object> matcher = smaller.stream().map(discriminator).collect(toSet());
    final Stream<T> intersection = larger.stream().filter(o -> matcher.remove(discriminator.apply(o)));
    if (col1 instanceof Set && col2 instanceof Set) {
      return intersection.collect(toSet());
    }
    return intersection.collect(toList());
  }

  /**
   * Finds the first index in the given List which matches the given predicate.
   * @param list the List to search, may not be null.
   * @param predicate the predicate to use, may not be null.
   * @param <T> the type of the items in the list
   * @return the first index of an Object in the List which matches the predicate.
   */
  public static <T> int indexOf(List<T> list, Predicate<T> predicate) {
    return indexOf(list, predicate, 0);
  }

  /**
   * Finds the first index in the given List which matches the given predicate.
   * @param list the List to search, may not be null.
   * @param predicate the predicate to use, may not be null.
   * @param firstIndex an integer representing the first index from which the search starts.
   * @param <T> the type of the items in the list
   * @return the first index of an Object in the List which matches the predicate.
   */
  public static <T> int indexOf(List<T> list, Predicate<T> predicate, int firstIndex) {
    for (int i = firstIndex; i < list.size(); i++) {
      final T item = list.get(i);
      if (predicate.test(item)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Gets the optional first element in the given List which matches the given predicate.
   * @param list the List to search, may not be null.
   * @param predicate the predicate to use, may not be null.
   * @param <T> the type of the items in the list
   * @return the optional Object in the List which matches the predicate.
   */
  public static <T> Optional<T> findFirst(List<T> list, Predicate<T> predicate) {
    return findFirst(list, predicate, 0);
  }

  /**
   * Gets the optional first element in the given List which matches the given predicate and the
   * position is at or after the first given index.
   * @param list the List to search, may not be null.
   * @param predicate the predicate to use, may not be null.
   * @param firstIndex an integer representing the first index from which the search starts.
   * @param <T> the type of the items in the list
   * @return the optional Object in the List which matches the predicate.
   */
  public static <T> Optional<T> findFirst(List<T> list, Predicate<T> predicate, int firstIndex) {
    int index = indexOf(list, predicate, firstIndex);
    return index >= 0 ? Optional.ofNullable(list.get(index)) : Optional.empty();
  }

  /**
   * Gets the optional next element in the given ordered List which matches the given {@link
   * RuptureContext}
   * which must be intialized by calling {@link RuptureContext#newOne(List)}.
   * @param context the context of the rupture.
   * @param predicate the predicate to use, may not be null.
   * @param <T> the type of the items in the list
   * @return the first index of an Object in the List which matches the predicate.
   */
  public static <T> Optional<T> findNextRupture(RuptureContext<T> context, Predicate<T> predicate) {
    if (context.isTerminated()) {
      throw new IllegalStateException(
          "the context of the rupture indicates that it is terminated");
    }
    if (context.lastPosition == -1 || !predicate.test(context.current)) {
      int position =
          CollectionUtil.indexOf(context.orderedList, predicate, context.lastPosition + 1);
      if (position >= 0) {
        context.lastPosition = position;
        context.current = context.orderedList.get(position);
      } else {
        context.lastPosition = context.orderedList.size();
        context.terminated = true;
        context.current = null;
      }
    }
    return Optional.ofNullable(context.current);
  }

  /**
   * Handles the context of a rupture treatment.<br>
   * It must by initialized by calling {@link RuptureContext#newOne(List)} after using
   * {@link CollectionUtil#findNextRupture(RuptureContext, Predicate)}.<br>
   * Callers can verify if it is no more possible to get an element from the rupture by calling
   * {@link RuptureContext#isTerminated()}.
   * @param <T> the type of the items in the list
   */
  public static class RuptureContext<T> {
    private final List<T> orderedList;
    private T current = null;
    private int lastPosition = -1;
    private boolean terminated = false;

    private RuptureContext(final List<T> orderedList) {
      this.orderedList = orderedList;
    }

    /**
     * Initializes a new context with the given ordered list.
     * @param orderedList an ordered list.
     * @param <T> the type of the items in the list
     * @return the initialized context.
     */
    public static <T> RuptureContext<T> newOne(final List<T> orderedList) {
      return new RuptureContext<>(orderedList);
    }

    /**
     * Indicates if the rupture is terminated.
     * @return true if terminated, false otherwise.
     */
    public boolean isTerminated() {
      return terminated;
    }

    /**
     * Resets the rupture as the caller was initializing a new one with {@link #newOne(List)}.
     */
    public void reset() {
      current = null;
      lastPosition = -1;
      terminated = false;
    }
  }
}
