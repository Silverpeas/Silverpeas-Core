/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
package org.silverpeas.core.date;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * A matcher of temporal objects between them, whatever their concrete type (a date or a datetime).
 */
public class TemporalMatcher extends TypeSafeMatcher<Temporal<?>> {

  private final DateOperator operator;
  private final Temporal<?> expected;

  @Factory
  public static Matcher<Temporal<?>> isBefore(final Temporal<?> expected) {
    return new TemporalMatcher(DateOperator.BEFORE, expected);
  }

  @Factory
  public static Matcher<Temporal<?>> isAfter(final Temporal<?> expected) {
    return new TemporalMatcher(DateOperator.AFTER, expected);
  }

  @Factory
  public static Matcher<Temporal<?>> isEqualTo(final Temporal<?> expected) {
    return new TemporalMatcher(DateOperator.EQUAL, expected);
  }

  @Override
  public void describeTo(Description description) {
    String status = "unknown";
    switch (operator) {
      case BEFORE:
        status = "before";
        break;
      case AFTER:
        status = "after";
        break;
      case EQUAL:
        status = "equal";
        break;
    }
    description.appendText("The actual date is not " + status + " the expected one (" +
        expected.toString() + ")");
  }

  private TemporalMatcher(final DateOperator operator, final Temporal<?> expected) {
    this.operator = operator;
    this.expected = expected;
  }

  @Override
  public boolean matchesSafely(final Temporal<?> actual) {
    if (!actual.getClass().equals(expected.getClass())) {
      return false;
    }
    boolean matching = false;
    switch (operator) {
      case BEFORE:
        if (actual.isTimeSupported()) {
          matching = ((DateTime)actual).isBefore((DateTime)expected);
        } else {
          matching = ((Date)actual).isBefore((Date)expected);
        }
        break;
      case AFTER:
        if (actual.isTimeSupported()) {
          matching = ((DateTime)actual).isAfter((DateTime)expected);
        } else {
          matching = ((Date)actual).isAfter((Date)expected);
        }
        break;
      case EQUAL:
        if (actual.isTimeSupported()) {
          matching = ((DateTime)actual).isEqualTo((DateTime)expected);
        } else {
          matching = ((Date)actual).isEqualTo((Date)expected);
        }
        break;
    }
    return matching;
  }

  private enum DateOperator {

    BEFORE,
    EQUAL,
    AFTER;
  }
}
