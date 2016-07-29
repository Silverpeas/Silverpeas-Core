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
import org.silverpeas.core.date.Period;

import java.net.URL;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * The event in a calendar. An event in the calendar is described by a starting and an ending date
 * and a name. The start and end dates in an event must be in the same type (either a date or a
 * temporal).
 */
@DateRange(startDate = "startDate", endDate = "endDate")
public class CalendarEvent implements Plannable, Recurrent, Prioritized {
  private static final long serialVersionUID = 1L;

  private Period period;
  private String title = "";
  private String description = "";
  private String location = "";
  private URL url = null;
  private VisibilityLevel visibilityLevel = VisibilityLevel.PUBLIC;
  private Priority priority = Priority.NORMAL;
  private Recurrence recurrence = Recurrence.NO_RECURRENCE;
  private final Categories categories = new Categories();
  private final Attendees attendees = new Attendees();
  private String id = "";

  /**
   * Creates a new calendar event that is spanning on the specified period of time.
   * @param period the period on which the event occurs.
   * @return a calendar event occurring on the specified period.
   */
  public static CalendarEvent on(Period period) {
    return new CalendarEvent(period);
  }

  /**
   * Creates a new calendar event that is on all the specified day.
   * @param day the day on which the event will occur.
   * @return a calendar event spanning on all the specified day.
   */
  public static CalendarEvent on(final LocalDate day) {
    return new CalendarEvent(Period.between(day, day));
  }

  /**
   * Specifies the visibility level to this event. In generally, it defines the intention of the
   * user about the visibility on the event he accepts to give. Usual values are PUBLIC, PRIVATE or
   * CONFIDENTIAL for example. By default, the visibility level is PUBLIC.
   * @param accessLevel the new visibility level to this event.
   * @return itself.
   */
  public CalendarEvent withVisibilityLevel(VisibilityLevel accessLevel) {
    this.visibilityLevel = accessLevel;
    return this;
  }

  /**
   * Sets a priority to this event.
   * @param priority an event priority.
   * @return itself.
   */
  @Override
  public CalendarEvent withPriority(Priority priority) {
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
  public Attendees getAttendees() {
    return this.attendees;
  }

  /**
   * Gets the categories to which this event belongs.
   * @return the categories of this event.
   */
  public Categories getCategories() {
    return categories;
  }

  /**
   * Gets the visibility level of this event.
   * @return the visibility level of this event.
   */
  public VisibilityLevel getVisibilityLevel() {
    return visibilityLevel;
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
  @Override
  public Priority getPriority() {
    return priority;
  }

  /**
   * Recurs this event with the specified event recurrence.
   * @param recurrence the recurrence defining the recurring property of this event.
   * @return itself.
   */
  @Override
  public CalendarEvent recur(final Recurrence recurrence) {
    if (isOnAllDay() && recurrence.getFrequency().isHourly()) {
      throw new IllegalArgumentException("Impossible to recur hourly an event on all day!");
    }
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
  @Override
  public Recurrence getRecurrence() {
    return this.recurrence;
  }

  /**
   * Is this event occurring on all the day(s)?
   * @return true if this event is occurring on all its day(s).
   */
  public boolean isOnAllDay() {
    return period.isInDays();
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
    return this.period.getStartDateTime();
  }

  @Override
  public OffsetDateTime getEndDateTime() {
    return this.period.getEndDateTime();
  }

  private CalendarEvent(Period period) {
    this.period = period;
  }
}
