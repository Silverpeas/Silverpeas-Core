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
package org.silverpeas.core.util;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * A common definition of Silverpeas's list in order to get common behaviours.
 * @author Yohann Chastagnier
 */
public interface SilverpeasList<T> extends List<T> {

  /**
   * Returns a {@link Collector} that accumulates the input items into a new {@link
   * SilverpeasList}. There are no guarantees on the type, mutability, serializability, or
   * thread-safety of the returned {@code SilverpeasList}.
   * <p> If the source is already a {@link SilverpeasList}, then the returned list will be of the
   * same concrete type than the source list.
   * </p>
   * @param <C> the type of the input items in the destination list.
   * @param <R> the concrete type of the {@link SilverpeasList}.
   * @param source the source list from which a SilverpeasList is built.
   * @return a {@link Collector} which collects all the input elements into a {@link
   * SilverpeasList}, in encounter order.
   */
  @SuppressWarnings("unchecked")
  static <C, R extends SilverpeasList<C>> Collector<C, R, R> collector(List<?> source) {
    final Supplier<R> supplier;
    if (source instanceof SilverpeasList) {
      supplier = () -> (R) ((SilverpeasList) source).newEmptyListWithSameProperties();
    } else {
      supplier = () -> (R) new SilverpeasArrayList<>();
    }
    final BiConsumer<R, C> consumer = SilverpeasList::add;
    final BinaryOperator<R> operator = (left, right) -> {
      left.addAll(right);
      return left;
    };
    return Collector.of(supplier, consumer, operator, Collector.Characteristics.IDENTITY_FINISH);
  }

  /**
   * Gets an array as a {@link SilverpeasList}.
   * @param arrayToConvert the array to get as {@link SilverpeasList}.
   * @param <T> the type of the elements into the array.
   * @return the {@link SilverpeasList} instance.
   */
  static <T> SilverpeasList<T> as(final T[] arrayToConvert) {
    final SilverpeasArrayList<T> list = new SilverpeasArrayList<>(arrayToConvert.length);
    list.addAll(Arrays.asList(arrayToConvert));
    return list;
  }

  /**
   * Gets a wrapper of any kind of implementation of {@link List} in order to get a {@link
   * SilverpeasList} behaviour.
   * <p>If the given list is already a {@link SilverpeasList} one, no wrap is done and the given
   * instance is returned immediately.</p>
   * @param listToWrap the list to wrap.
   * @param <T> the type of the elements into the list.
   * @return the {@link SilverpeasList} instance.
   */
  static <T> SilverpeasList<T> wrap(final List<T> listToWrap) {
    return listToWrap instanceof SilverpeasList ?
        (SilverpeasList<T>) listToWrap :
        new SilverpeasListWrapper<>(listToWrap);
  }

  /**
   * Builds a new empty {@link SilverpeasList} with the same properties than this list.
   * <p>
   *   This method is mainly dedicated to be used for making a collector for the Java Stream API.
   * </p>
   * @param <U> the concrete type of the items of the returned list.
   * @return an empty {@link SilverpeasList} with the same properties than this list.
   */
  <U> SilverpeasList<U> newEmptyListWithSameProperties();

  /**
   * Gets the number of items the original list contains.
   * <p>If the list is a slice of a larger one, the {@link #originalListSize()} returns a
   * higher result than the one of {@link #size()}, otherwise {@link #originalListSize()} and
   * {@link #size()} returns the same result.</p>
   * @return the original size of the list as long.
   */
  long originalListSize();

  /**
   * Indicates if the list is a slice of a larger one.
   * @return true if it represents a slice, false otherwise.
   */
  default boolean isSlice() {
    return originalListSize() != size();
  }
}