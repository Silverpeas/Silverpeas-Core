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

import java.time.LocalDate;

/**
 * A calendar is a particular system for scheduling and organizing events and activities that occur
 * at different times or on different dates throughout the year.
 * @author mmoquillon
 */
public class Calendar {

  private String id;
  private String title;

  /**
   * Creates a new calendar with the specified identifier and the specified title.
   * @param id an identifier identifying uniquely the calendar. Usually, this identifier is the
   * identifier of the component instance to which it belongs (for example almanach32) or the
   * identifier of the user personal calendar.
   * @param title a title presenting usually the subject of the calendar.
   */
  public Calendar(String id, String title) {
    this.id = id;
    this.title = title;
  }

  /**
   * Creates a new calendar with the specified identifier and the specified title.
   * @param id an identifier identifying uniquely the calendar. Usually, this identifier is the
   * identifier of the component instance to which it belongs (for example almanach32) or the
   * identifier of the user personal calendar.
   */
  public Calendar(String id) {
    this(id, id);
  }

  public String getId() {
    return this.id;
  }

  public String getTitle() {
    return this.title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Adds a new event on the specified day.
   * @param day the date at which the event will occur.
   * @return the newly created event.
   */
  public CalendarEvent addEventOn(final LocalDate day) {
    return CalendarEvent.anEventOn(day, day);
  }
}
