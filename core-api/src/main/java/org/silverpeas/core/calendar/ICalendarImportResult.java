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

/**
 * Result of an import processing of calendar events into Silverpeas.
 * @author mmoquillon
 */
public class ICalendarImportResult {

  private int added = 0;
  private int updated = 0;
  private int deleted = 0;

  /**
   * Gets the number of {@link PlannableOnCalendar} objects added in the calendar after an import a calendar
   * content.
   * @return the number of added calendar components.
   */
  public int added() {
    return this.added;
  }

  /**
   * Gets the number of {@link PlannableOnCalendar} objects updated in the calendar after an import a calendar
   * content.
   * @return the number of updated calendar components.
   */
  public int updated() {
    return this.updated;
  }

  /**
   * Gets the number of {@link PlannableOnCalendar} objects deleted in the calendar after an import a calendar
   * content.
   * @return the number of deleted calendar components.
   */
  public int deleted() {
    return this.deleted;
  }

  /**
   * Is there any result about the import process?
   * @return false if the result is empty, that is to say there is no {@link PlannableOnCalendar} objects
   * added, updated or deleted in the concerned calendar.
   */
  public boolean isEmpty() {
    return added == 0 && updated == 0 && deleted == 0;
  }

  /**
   * Increment the counter of added {@link PlannableOnCalendar} object of 1.
   */
  void incAdded() {
    added += 1;
  }

  /**
   * Increment the counter of updated {@link PlannableOnCalendar} object of 1.
   */
  void incUpdated() {
    updated += 1;
  }

  /**
   * Increment the counter of deleted {@link PlannableOnCalendar} object of 1.
   */
  void incDeleted() {
    deleted += 1;
  }
}
