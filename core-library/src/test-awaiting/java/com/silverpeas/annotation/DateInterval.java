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

package com.silverpeas.annotation;

import org.silverpeas.core.annotation.constraint.DateRange;
import org.silverpeas.core.date.Datable;

/**
 * Objects representing an interval of dates. It is aiming for tests on the DateRangeValidator
 * validation.
 */
@DateRange(startDate="from", endDate="to")
public class DateInterval {

  private Datable<?> from;
  private Datable<?> to;

  /**
   * Constructs a new interval from the two specified dates.
   * @param startDate the start date of the interval.
   * @param endDate the end date of the interval.
   */
  public DateInterval(final Datable<?> startDate, final Datable<?> endDate) {
    this.from = startDate;
    this.to = endDate;
  }

  /**
   * Gets the start date of the interval.
   * @return the interval start date.
   */
  public Datable<?> getStartDate() {
    return from;
  }

  /**
   * Gets the end date of the interval.
   * @return the interval end date.
   */
  public Datable<?> getEndDate() {
    return to;
  }


}
