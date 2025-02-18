/*
 * Copyright (C) 2000 - 2025 Silverpeas
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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.calendar;

import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.Planned;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.Temporal;

/**
 * A temporal object planned in a given calendar and that is serializable in a data source. A
 * planned object is defined in the timeline by a start datetime and by an end datetime with
 * split-minute accuracy. According to the type of the planned object, the end datetime can be
 * undefined (non ending). The temporal type of both the start datetime and the end datetime of the
 * planned object must be identical.
 *
 * @author mmoquillon
 */
public interface PlannedOnCalendar extends Contribution, Planned {

  /**
   * Gets the unique identifier of this planned object in the datasource.
   *
   * @return the unique identifier of this planned object.
   */
  String getId();

  /**
   * Gets the calendar into which this planned object is finally planned.
   *
   * @return the calendar into which this object is planned or null if it isn't yet planned.
   */
  Calendar getCalendar();

  /**
   * The start date or datetime of the planned object. It is the inclusive lower bound of the period
   * into which this object occurs in a calendar.
   * <p>
   * If this planned object is on all days, then gets a date. Otherwise, gets a datetime in
   * UTC/Greenwich.
   *
   * @return a temporal instance of {@link LocalDate} if the object is on all the day or a temporal
   * instance of {@link OffsetDateTime}) otherwise.
   */
  Temporal getStartDate();

  /**
   * The end date or datetime of the planned object. It is the exclusive upper bound of the period
   * into which this object occurs in a calendar.
   * <p>
   * If this planned object is on all days, then gets a date. Otherwise, gets a datetime in
   * UTC/Greenwich.
   * <p>
   * According to the type of the planned object, the end datetime can be undefined; in this case,
   * it must be indicated as such in the implemented method's documentation.
   *
   * @return a temporal instance of {@link LocalDate} if the object is on all the day or a temporal
   * instance of {@link OffsetDateTime}) otherwise.
   */
  Temporal getEndDate();

  /**
   * Does this planned object extend over all the day(s)? In the case it is on all the day(s) from
   * the start date to the end date, the time in the datetime returned by the methods
   * {@link PlannedOnCalendar#getStartDate()} and {@link PlannedOnCalendar#getEndDate()} is
   * meaningless and shouldn't be taken into account.
   *
   * @return true if this planned object extend over all the day(s) between its start date and its
   * end date.
   */
  boolean isOnAllDay();

  /**
   * Sets a title to this planned object. A title is a short resume or the subject of the planned
   * object.
   *
   * @param title a short text about the reason of this planned object.
   */
  void setTitle(String title);

  /**
   * Gets the {@link CalendarComponent} representation on the underlying calendar of this planned
   * object. Any change to the returned calendar component will change also this related planned
   * object.
   *
   * @return a {@link CalendarComponent} instance representing this planned object (without its
   * specific properties).
   */
  CalendarComponent asCalendarComponent();
}
