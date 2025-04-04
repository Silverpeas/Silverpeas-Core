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
package org.silverpeas.core.calendar;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.Temporal;

/**
 * An instance of a planned temporal object occurring in the timeline of a given calendar. The
 * planned object is one that is plannable to a calendar; when such a temporal object is explicitly
 * planned on a given calendar, then one or several occurrences (for recurrent temporal objects) of
 * such a temporal object can then be generated in the calendar, each of them occupying a given
 * range of datetime in the calendar.
 *
 * @author mmoquillon
 */
public interface Occurrence extends PlannedOnCalendar {

  /**
   * Gets the unique identifier of this occurrence of an object planned in a calendar.
   *
   * @return the unique identifier of this occurrence.
   */
  @Override
  String getId();

  /**
   * The start date or datetime of the occurrence. It is the inclusive lower bound of the period
   * into which this occurrence occurs in a calendar.
   * <p>
   * If this occurrence is on all days, then gets a date. Otherwise, gets a datetime in
   * UTC/Greenwich. on the timeline.
   * </p>
   *
   * @return a temporal instance of {@link LocalDate} if the occurrence takes all the day or a
   * temporal instance of {@link OffsetDateTime} in UTC/Greenwich otherwise.
   */
  @Override
  Temporal getStartDate();

  /**
   * The end date or datetime of the occurrence. It is the exclusive upper bound of the period into
   * which this occurrence occurs in a calendar.
   * <p>
   * If this occurrence is on all days, then gets a date. Otherwise, gets a datetime in
   * UTC/Greenwich.
   * </p>
   *
   * @return a temporal instance of {@link LocalDate} if the occurrence takes all the day or a
   * temporal instance of {@link OffsetDateTime} in UTC/Greenwich otherwise.
   */
  @Override
  Temporal getEndDate();
}
