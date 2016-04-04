/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.date;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * A matcher of datable objects between them, whatever their concrete type (a date or a date time).
 */
public class DatableMatcher extends TypeSafeMatcher<Datable<?>> {

  private final DateOperator operator;
  private final Datable<?> expected;

  @Factory
  public static Matcher<Datable<?>> isBefore(final Datable<?> expected) {
    return new DatableMatcher(DateOperator.BEFORE, expected);
  }

  @Factory
  public static Matcher<Datable<?>> isAfter(final Datable<?> expected) {
    return new DatableMatcher(DateOperator.AFTER, expected);
  }

  @Factory
  public static Matcher<Datable<?>> isEqualTo(final Datable<?> expected) {
    return new DatableMatcher(DateOperator.EQUAL, expected);
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

  private DatableMatcher(final DateOperator operator, final Datable<?> expected) {
    this.operator = operator;
    this.expected = expected;
  }

  @Override
  public boolean matchesSafely(final Datable<?> actual) {
    if (!actual.getClass().equals(expected.getClass())) {
      return false;
    }
    boolean matching = false;
    switch (operator) {
      case BEFORE:
        if (actual instanceof DateTime) {
          matching = ((DateTime)actual).isBefore((DateTime)expected);
        } else {
          matching = ((Date)actual).isBefore((Date)expected);
        }
        break;
      case AFTER:
        if (actual instanceof DateTime) {
          matching = ((DateTime)actual).isAfter((DateTime)expected);
        } else {
          matching = ((Date)actual).isAfter((Date)expected);
        }
        break;
      case EQUAL:
        if (actual instanceof DateTime) {
          matching = ((DateTime)actual).isEqualTo((DateTime)expected);
        } else {
          matching = ((Date)actual).isEqualTo((Date)expected);
        }
        break;
    }
    return matching;
  }

  private static enum DateOperator {

    BEFORE,
    EQUAL,
    AFTER;
  }
}
