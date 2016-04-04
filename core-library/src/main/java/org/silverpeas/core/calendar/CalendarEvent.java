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

import org.silverpeas.core.contribution.model.SilverpeasContent;
import org.silverpeas.core.annotation.constraint.DateRange;
import org.silverpeas.core.date.Datable;
import org.silverpeas.core.date.Date;

import java.io.Serializable;
import java.net.URL;
import static org.silverpeas.core.util.StringUtil.*;

/**
 * The event in a calendar. An event in the calendar is described by a starting and an ending date
 * and a name. The start and end dates in an event must be in the same type (either a date or a
 * datable).
 */
@DateRange(startDate = "startDate", endDate = "endDate")
public class CalendarEvent implements Serializable {
  private static final long serialVersionUID = 1L;

  private Datable<?> startDate;
  private Datable<?> endDate;
  private String title = "";
  private String description = "";
  private String location = "";
  private URL url = null;
  private String accessLevel = "PUBLIC";
  private int priority = 0;
  private CalendarEventRecurrence recurrence = CalendarEventRecurrence.NO_RECURRENCE;
  private final CalendarEventCategories categories = new CalendarEventCategories();
  private final CalendarEventAttendees attendees = new CalendarEventAttendees();
  private String id = "";

  /**
   * Creates a new calendar event starting and ending at the specified date.
   * @param startDate the start date of the event.
   * @return a calendar event.
   */
  public static <T extends Datable<?>> CalendarEvent anEventAt(final T startDate) {
    return new CalendarEvent().startingAt(startDate).endingAt(startDate);
  }

  /**
   * Creates a new calendar event starting at the specified start date and ending at the specified
   * end date.
   * @param startDate the start date of the event. The start date defines the inclusive date at
   * which the event starts.
   * @param endDate the end date of the event. The end date defines the non-inclusive date at which
   * the event ends up.
   * @return a calendar event.
   */
  public static <T extends Datable<?>> CalendarEvent anEventAt(final T startDate, final T endDate) {
    return new CalendarEvent().startingAt(startDate).endingAt(endDate);
  }

  /**
   * Specifies the access level to this event. In generally, it defines the intention of the user
   * about the access on the event he accepts to give. Usual values are PUBLIC, PRIVATE or
   * CONFIDENTIAL for example. By default, the access level is PUBLIC.
   * @param accessLevel the new access level to this event.
   * @return itself.
   */
  public CalendarEvent withAccessLevel(String accessLevel) {
    if (!isDefined(accessLevel)) {
      throw new IllegalArgumentException("The accessLevel parameter isn't defined!");
    }
    this.accessLevel = accessLevel;
    return this;
  }

  /**
   * Sets an end date to this event. By default, the event end date is non inclusive and it is set
   * to the start date. For a recurring event, the end date is the one for each occurrences of the
   * event.
   * @param endDate the event end date.
   * @return itself.
   */
  public CalendarEvent endingAt(Datable<?> endDate) {
    this.endDate = endDate.clone();
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
   * Modifies the start date of this event.
   * @param startDate the new event start date.
   * @return itself.
   */
  public CalendarEvent startingAt(Datable<?> startDate) {
    this.startDate = startDate.clone();
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
  public String getAccessLevel() {
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
   * Gets the date at which this event ends.
   * @return the end date of this event.
   */
  public Datable<?> getEndDate() {
    return endDate.clone();
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
   * Gets the date at which this event should starts
   * @return the start date of the event.
   */
  public Datable<?> getStartDate() {
    return startDate.clone();
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
    return this.startDate instanceof Date || this.endDate instanceof Date;
  }

  public CalendarEvent from(SilverpeasContent content) {
    identifiedBy(content.getComponentInstanceId(), content.getId());
    this.title = content.getTitle();
    this.description = content.getDescription();
    return this;
  }

  public CalendarEvent identifiedBy(String appId, String eventId) {
    this.id = appId+"-"+eventId;
    return this;
  }

  public String getId() {
    return id;
  }

  private CalendarEvent() {
  }
}
