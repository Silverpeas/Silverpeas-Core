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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util.filter;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A filter matcher. It applies each matching rules and kept the result of the first predicate
 * that is true. Once a predicate is true, all other predicates aren't verified and then the
 * associated operations aren't executed.
 * @author mmoquillon
 */
public class FilterMatcher<T, U, V> {

  private final U value;
  private final T criterion;
  private V result;

  /**
   * Constructs a new {@link FilterMatcher} instance.
   * @param criterion the type of the value to pass to predicates.
   * @param value the type of the value to pass to the functions when their associated predicate
   * is true.
   */
  FilterMatcher(final T criterion, final U value) {
    this.criterion = criterion;
    this.value = value;
  }

  /**
   * Plays the specified predicate against the criterion and applies the specified function
   * if and only if the predicate is true. If the criterion is true, the result is stored and the
   * other next predicates aren't played. The result of the function cannot be null otherwise a
   * {@link AssertionError} is thrown.
   * @param predicate the predicate to match.
   * @param function the function associated with the predicate and that returns a non-null
   * computation result.
   * @return itself.
   */
  public FilterMatcher<T, U, V> matchFirst(final Predicate<T> predicate,
      final Function<U, V> function) {
    if (result == null && predicate.test(criterion)) {
      result = function.apply(value);
      assert result != null;
    }
    return this;
  }

  /**
   * Gets the result returned by the function associated with the first predicates that is true.
   * @return an {@link Optional} value. Empty means that no predicates were true.
   */
  public Optional<V> result() {
    return Optional.ofNullable(result);
  }
}
  