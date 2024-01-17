/*
 * Copyright (C) 2000 - 2024 Silverpeas
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

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A filter is an object that plays all the predicates against a given object and that applies the
 * operation associated with the matched predicate. If the object is an {@link java.util.Optional}
 * or a {@link org.silverpeas.core.util.Mutable} instance, then the predicates are directly applied
 * to the contained object; if they are empty, then no predicates are played.
 * <p>
 * With a chain of {@link #match(Predicate, Consumer)} methods, the operations are performed
 * for each predicate that is true; the corresponding operations are performed in the
 * order the matching rules are declared.
 * </p>
 * <p>
 * With a chain of {@link #matchFirst(Predicate, Function)} methods, only the operation of the first
 * predicate that is true is executed.
 * </p>
 * @param <T> the type of the object against which the predicate has to be played.
 * @param <U> the type of the object that is passed to the operation if its corresponding predicate
 * is true.
 * @author mmoquillon
 */
public interface Filter<T, U> {

  /**
   * Plays the specified predicate against an underlying object and applies the specified operation
   * if and only if the predicate is true.
   * @param predicate the predicate to match.
   * @param operation the operation associated with the predicate.
   * @return itself.
   */
  Filter<T, U> match(final Predicate<T> predicate, final Consumer<U> operation);

  /**
   * Plays the specified predicate against an underlying object and applies the specified function
   * if and only if the predicate is true. If the predicate is true, the result of the function is
   * stored in order to be retrieved later and the chain of predicate to play is stopped; the next
   * predicates won't be played and hence their associated functions won't be executed. The result
   * of the function mustn't be null otherwise an {@link AssertionError} is thrown.
   * @param predicate the predicate to match.
   * @param function the function associated with the predicate and that returns a non-null
   * computation result.
   * @param <V> the return type of the function.
   * @return a {@link FilterMatcher} instance from which the result can be retrieved and with which
   * other filtering rules can be set.
   */
  <V> FilterMatcher<T, U, V> matchFirst(final Predicate<T> predicate, final Function<U, V> function);

}
