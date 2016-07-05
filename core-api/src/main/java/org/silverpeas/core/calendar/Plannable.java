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
package org.silverpeas.core.calendar;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * A plannable object is an object that can be planned on a calendar and that can be serialized
 * on a data source.
 * @author mmoquillon
 */
public interface Plannable extends Serializable {

  /**
   * Gets the unique identifier of this plannable object.
   * @return
   */
  String getId();

  /**
   * Gets the date and the time in from UTC/Greenwich at which this plannable object starts on the
   * timeline. If the event is on all the day(s), then the time is meaningless and it is then
   * recommended to get the local date from the returned date time.
   * @return a date and time in UTC/Greenwich.
   */
  OffsetDateTime getStartDateTime();

  /**
   * Gets the date and the time in from UTC/Greenwich at which this plannable object ends on the
   * timeline. If the event is on all the day(s), then the time is meaningless and it is then
   * recommended to get the local date from the returned date time.
   * @return a date and time in UTC/Greenwich.
   */
  OffsetDateTime getEndDateTime();

  /**
   * Does this plannable object extend over all the day(s)? In the case it is on all the day(s)
   * from the start date to the end date, the time in the date time returned by the methods
   * {@link Plannable#getStartDateTime()} and {@link Plannable#getEndDateTime()} is meaningless and
   * shouldn't be taken into account.
   * @return true if this plannable object extend over all the day(s) between its start date and
   * its end date.
   */
  boolean isOnAllDay();

  /**
   * Gets the title of this plannable object. A title is a short resume or the subject of the
   * plannable object.
   * @return a short text about the reason of this plannable object.
   */
  String getTitle();
}
