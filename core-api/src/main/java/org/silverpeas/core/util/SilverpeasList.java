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
 * FLOSS exception. You should have received a copy of the text describing
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
   * Returns a {@link Collector} that accumulates the input elements into a new {@link
   * SilverpeasList}. There are no guarantees on the type, mutability, serializability, or
   * thread-safety of the {@code SilverpeasList} returned.
   * <p> If source is paginated (a {@link PaginationList} for example), then the collected list
   * will also be.
   * </p>
   * @param <C> the type of the input elements.
   * @return a {@link Collector} which collects all the input elements into a {@link
   * SilverpeasList}, in encounter order.
   */
  @SuppressWarnings("unchecked")
  static <C, R extends SilverpeasList<C>, S> Collector<C, R, R> collector(
      SilverpeasList<S> source) {
    final Supplier<R> supplier;
    if (source != null && source.isPageWindow()) {
      supplier = () -> (R) PaginationList.from(new ArrayList<>(source.size()), source.maxSize());
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
   * Gets the maximum number of items the list contains.
   * <p>If the list is a page representation of a larger one, the {@link #maxSize()} returns a
   * higher result than the one of {@link #size()}, otherwise {@link #maxSize()} and
   * {@link #size()} returns the same result.</p>
   * @return the maximum size of the list.
   */
  long maxSize();

  /**
   * Indicates if the list is a page representation of a larger one.
   * @return true if page window, false otherwise.
   */
  default boolean isPageWindow() {
    return maxSize() != size();
  }
}