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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util.filter;

import org.silverpeas.core.util.Mutable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A filter by the type of the wrapped object. If the type of the object matches a given
 * predicate then the operation with the matched predicate is performed against this object.
 * The object cannot be null otherwise nothing is performed.
 * @author mmoquillon
 */
public class FilterByType implements Filter<Class<?>, Object> {

  private final Object value;

  /**
   * Constructs a filter by type on the specified value.
   * @param value the value against which the predicates will be played. If the value is an
   * {@link Optional} or a {@link Mutable} instance, then only its contained value is considered.
   */
  public FilterByType(final Object value) {
    if (value instanceof Optional) {
      this.value = ((Optional) value).orElse(null);
    } else if (value instanceof Mutable) {
      this.value = ((Mutable) value).orElse(null);
    } else {
      this.value = value;
    }
  }


  @Override
  public FilterByType match(final Predicate<Class<?>> predicate, final Consumer<Object> operation) {
    if (value != null && predicate.test(value.getClass())) {
      operation.accept(value);
    }
    return this;
  }

  @Override
  public <V> FilterMatcher<Class<?>, Object, V> matchFirst(final Predicate<Class<?>> predicate,
      final Function<Object, V> function) {
    return value != null ?
        new FilterMatcher<Class<?>, Object, V>(value.getClass(), value).matchFirst(predicate,
            function) : new EmptyFilterMatcher<>();
  }
}
  