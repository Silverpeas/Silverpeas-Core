/*
 * Copyright (C) 2000 - 2023 Silverpeas
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
 * "https://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * A randomizer of a date to use in the tests.
 * @author mmoquillon
 */
public final class DateRandom {

  private final Random random = new Random();
  private static final int baseYear = 2000;

  private static final List<Integer> LONGER_MONTHS = List.of(0, 2, 4, 6, 7, 9, 11);

  /**
   * Returns a pseudorandom, uniformly distributed {@link Date} value between 2000, January 1st at
   * midnight (inclusive) and the last time of the specified year (exclusive), drawn from this
   * random number generator's sequence.
   * @param upperBound the upper bound (exclusive). Must be positive, otherwise by default the
   * upper bound is set at 2100.
   * @return a date between 2000, January 1st at midnight (inclusive) and the last time of the
   * specified upper bound value taken as a year (exclusive).
   */
  public Date nextDate(int upperBound) {
    Calendar calendar = Calendar.getInstance();

    int maxYear = upperBound <= 0 ? 100 : upperBound - baseYear;
    calendar.set(Calendar.YEAR, baseYear + random.nextInt(maxYear));

    int month = random.nextInt(12);
    calendar.set(Calendar.MONTH, month);
    if (month == 1) {
      calendar.set(Calendar.DAY_OF_MONTH, 1 + random.nextInt(28));
    } else if (LONGER_MONTHS.contains(month)) {
      calendar.set(Calendar.DAY_OF_MONTH, 1 + random.nextInt(31));
    } else {
      calendar.set(Calendar.DAY_OF_MONTH, 1 + random.nextInt(30));
    }

    calendar.set(Calendar.HOUR_OF_DAY, random.nextInt(24));
    calendar.set(Calendar.MINUTE, random.nextInt(60));
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    calendar.setLenient(false);

    return calendar.getTime();
  }
}
