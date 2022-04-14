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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.date;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;

/**
 * An enumeration of time units to be used to express a well known defined unit in the timeline.
 * This class was defined before Java Time API inclusion. Now this class defines a restrictive
 * set of {@link ChronoUnit} values. The difference with {@link ChronoUnit} is that only time units
 * in use in Silverpeas are defined so that we are ensured the time units are correctly supported
 * by the processes in Silverpeas that handle date time values and hence don't cause any unexpected
 * and unwanted behaviors.
 * @author mmoquillon
 */
public enum TimeUnit implements TemporalUnit {

  /**
   * Time unit representing one millisecond.
   */
  MILLISECOND,

  /**
   * Time unit representing one second.
   */
  SECOND,
  /**
   * Time unit representing one minute.
   */
  MINUTE,

  /**
   * Time unit representing one hour.
   */
  HOUR,

  /**
   * Time unit representing one day.
   */
  DAY,

  /**
   * Time unit representing one week.
   */
  WEEK,

  /**
   * Time unit representing one month.
   */
  MONTH,

  /**
   * Time unit representing one year.
   */
  YEAR;

  /**
   * Converts this time unit into a {@link ChronoUnit} value.
   * @return a {@link ChronoUnit} instance matching this time unit.
   */
  public ChronoUnit toChronoUnit() {
    switch (this) {
      case MILLISECOND:
        return ChronoUnit.MILLIS;
      case SECOND:
        return ChronoUnit.SECONDS;
      case MINUTE:
        return ChronoUnit.MINUTES;
      case HOUR:
        return ChronoUnit.HOURS;
      case DAY:
        return ChronoUnit.DAYS;
      case WEEK:
        return ChronoUnit.WEEKS;
      case MONTH:
        return ChronoUnit.MONTHS;
      case YEAR:
        return ChronoUnit.YEARS;
      default:
        throw new IllegalStateException("Unknown time unit: " + this.name());
    }
  }

  @Override
  public Duration getDuration() {
    return toChronoUnit().getDuration();
  }

  @Override
  public boolean isDurationEstimated() {
    return toChronoUnit().isDurationEstimated();
  }

  @Override
  public boolean isDateBased() {
    return toChronoUnit().isDateBased();
  }

  @Override
  public boolean isTimeBased() {
    return toChronoUnit().isTimeBased();
  }

  @Override
  public <R extends Temporal> R addTo(R temporal, long amount) {
    return toChronoUnit().addTo(temporal, amount);
  }

  @Override
  public long between(Temporal temporal1Inclusive, Temporal temporal2Exclusive) {
    return temporal1Inclusive.until(temporal2Exclusive, this);
  }
}
