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

import org.silverpeas.core.annotation.constraint.DateRange;

import java.net.URL;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * The event in a calendar. An event in the calendar is described by a starting and an ending date
 * and a name. The start and end dates in an event must be in the same type (either a date or a
 * temporal).
 */
@DateRange(startDate = "startDate", endDate = "endDate")
public class CalendarEvent implements Plannable {
  private static final long serialVersionUID = 1L;

  private OffsetDateTime startDateTime;
  private OffsetDateTime endDateTime;
  private boolean allDay = false;
  private String title = "";
  private String description = "";
  private String location = "";
  private URL url = null;
  private PlannableAccessLevel accessLevel = PlannableAccessLevel.PUBLIC;
  private int priority = 0;
  private CalendarEventRecurrence recurrence = CalendarEventRecurrence.NO_RECURRENCE;
  private final CalendarEventCategories categories = new CalendarEventCategories();
  private final CalendarEventAttendees attendees = new CalendarEventAttendees();
  private String id = "";

  /**
   * Creates a new calendar event that is on all the days between the two specified dates.
   * @param day the day on all which the event will occur.
   * @return a calendar event extending on all the specified day.
   */
  public static CalendarEvent anEventOn(final LocalDate day) {
    return new CalendarEvent().between(day, day);
  }

  /**
   * Creates a new calendar event that is on all the days between the two specified dates.
   * @param startDay the start date of the event. The start date defines the inclusive date at
   * which the event starts.
   * @param endDay the end date of the event. The end date defines the inclusive date at which
   * the event ends up. An end date equal to the start date means the event occurs all the day.
   * @return a calendar event extending on all the day(s) between the two specified dates.
   */
  public static CalendarEvent anEventOn(final LocalDate startDay, final LocalDate endDay) {
    return new CalendarEvent().between(startDay, endDay);
  }

  /**
   * Creates a new calendar event starting at the specified start date time and ending at the
   * specified end date time.
   * @param startDateTime the start date time of the event. The start date time defines the
   * inclusive date and time at which the event starts.
   * @param endDateTime the end date time of the event. The end date time defines the inclusive
   * date and time at which the event ends up.
   * @return a calendar event occurring between the two date times.
   */
  public static CalendarEvent anEventAt(final OffsetDateTime startDateTime,
      OffsetDateTime endDateTime) {
    return new CalendarEvent().between(startDateTime, endDateTime);
  }

  /**
   * Creates a new calendar event starting at the specified start date time and ending at the
   * specified end date time.
   * @param startDateTime the start date time of the event. The start date time defines either the
   * inclusive date or the inclusive date and time at which the event starts.
   * @param endDateTime the end date time of the event. The end date time defines either the
   * inclusive date or the inclusive date and time at which the event ends up.
   * @param allDay a boolean indicating if the event is on all the day(s). If true, the time part
   * of the specified date times aren't taken into account; only the date is meaningful.
   * @return a calendar event either occurring between the two date times or extending on all of
   * the day(s) between them.
   */
  public static CalendarEvent anEventAt(final OffsetDateTime startDateTime,
      OffsetDateTime endDateTime, boolean allDay) {
    if (allDay) {
      return new CalendarEvent().between(startDateTime.toLocalDate(), endDateTime.toLocalDate());
    } else {
      return new CalendarEvent().between(startDateTime, endDateTime);
    }
  }

  /**
   * Specifies the access level to this event. In generally, it defines the intention of the user
   * about the access on the event he accepts to give. Usual values are PUBLIC, PRIVATE or
   * CONFIDENTIAL for example. By default, the access level is PUBLIC.
   * @param accessLevel the new access level to this event.
   * @return itself.
   */
  public CalendarEvent withAccessLevel(PlannableAccessLevel accessLevel) {
    this.accessLevel = accessLevel;
    return this;
  }

  /**
   * Sets a priority to this event.
   * @param priority an event priority.
   * @return itself.
   */
  public CalendarEvent withPriority(int priority) {
    this.priority = priority;
    return this;
  }

  /**
   * Gets the location where this event should occur.
   * @return the location of this event or an empty string if no location is defined for this event.
   */
  public String getLocation() {
    return location;
  }

  /**
   * Sets a location where this event should occur.
   * @param location the location of this event.
   * @return itself.
   */
  public CalendarEvent withLocation(String location) {
    if (location == null) {
      this.location = "";
    } else {
      this.location = location;
    }
    return this;
  }

  /**
   * Sets an URL of a resource describing or representing this event.
   * @return an URL of a resource in connection with this event or null if no URL is defined for
   * this event.
   */
  public URL getUrl() {
    return url;
  }

  /**
   * Gets the URL of a resource describing or representing this event.
   * @param url the URL of a resource in connection with this event.
   * @return itself.
   */
  public CalendarEvent withUrl(URL url) {
    this.url = url;
    return this;
  }

  /**
   * Gets the attendees to this event.
   * @return the attendees to this event.
   */
  public CalendarEventAttendees getAttendees() {
    return this.attendees;
  }

  /**
   * Gets the categories to which this event belongs.
   * @return the categories of this event.
   */
  public CalendarEventCategories getCategories() {
    return categories;
  }

  /**
   * Gets the classification of this event.
   * @return the classification of this event.
   */
  public PlannableAccessLevel getAccessLevel() {
    return accessLevel;
  }

  /**
   * Gets a description about this event.
   * @return a description about this event or an empty string if no description is attached to this
   * event.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Gets the title of this event.
   * @return the event title or an empty string if the event has no title.
   */
  public String getTitle() {
    return title;
  }

  /**
   * Gets the priority of this event.
   * @return the priority of the event.
   */
  public int getPriority() {
    return priority;
  }

  /**
   * Recurs this event with the specified event recurrence.
   * @param recurrence the recurrence defining the recurring property of this event.
   * @return itself.
   */
  public CalendarEvent recur(final CalendarEventRecurrence recurrence) {
    this.recurrence = recurrence;
    return this;
  }

  /**
   * Sets a title to this event.
   * @param title the title to set.
   * @return itself.
   */
  public CalendarEvent withTitle(String title) {
    if (title == null) {
      this.title = "";
    } else {
      this.title = title;
    }
    return this;
  }

  /**
   * Sets a description ot this event.
   * @param description the description to set.
   * @return itself.
   */
  public CalendarEvent withDescription(String description) {
    if (description == null) {
      this.description = "";
    } else {
      this.description = description;
    }
    return this;
  }

  /**
   * Gets the recurrence of this recurring event. If the event isn't a recurring one, then returns
   * NO_RECURRENCE.
   * @return this event recurrence or NO_RECURRENCE.
   */
  public CalendarEventRecurrence getRecurrence() {
    return this.recurrence;
  }

  /**
   * Is this event a recurring one?
   * @return true if this event is recurring, false otherwise.
   */
  public boolean isRecurring() {
    return this.recurrence != CalendarEventRecurrence.NO_RECURRENCE;
  }

  /**
   * Is this event occurring on all the day(s)?
   * @return true if this event is occurring on all its day(s).
   */
  public boolean isOnAllDay() {
    return allDay;
  }

  public CalendarEvent identifiedBy(String appId, String eventId) {
    this.id = appId+"-"+eventId;
    return this;
  }

  public String getId() {
    return id;
  }

  @Override
  public OffsetDateTime getStartDateTime() {
    return this.startDateTime;
  }

  @Override
  public OffsetDateTime getEndDateTime() {
    return this.endDateTime;
  }

  private CalendarEvent() {
  }

  private CalendarEvent between(LocalDate startDate, LocalDate endDate) {
    checkPeriod(startDate, endDate);
    this.startDateTime = startDate.atStartOfDay().atOffset(ZoneOffset.UTC);
    this.endDateTime = endDate.plusDays(1).atStartOfDay().minusMinutes(1).atOffset(ZoneOffset.UTC);
    this.allDay = true;
    return this;
  }

  private CalendarEvent between(OffsetDateTime startDateTime, OffsetDateTime endDateTime) {
    checkPeriod(startDateTime, endDateTime);
    this.startDateTime = startDateTime.withOffsetSameInstant(ZoneOffset.UTC);
    this.endDateTime = endDateTime.withOffsetSameInstant(ZoneOffset.UTC);
    this.allDay = false;
    return this;
  }

  private static void checkPeriod(final OffsetDateTime startDateTime,
      final OffsetDateTime endDateTime) {
    if (startDateTime.isAfter(endDateTime)) {
      throw new IllegalArgumentException("The end date time should be after the start date time");
    }
  }

  private static void checkPeriod(final LocalDate startDate, final LocalDate endDate) {
    if (startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("The end date should be after the start date");
    }
  }
}
