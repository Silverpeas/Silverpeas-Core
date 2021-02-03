/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

import org.silverpeas.core.admin.user.model.User;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * A filter to apply on the calendar events that occur in a given window of time.
 * @author mmoquillon
 */
public class CalendarEventFilter {

  private List<Calendar> calendars = new ArrayList<>();
  private List<User> participants = new ArrayList<>();
  private OffsetDateTime synchronizationDateLimit = null;

  CalendarEventFilter() {

  }

  /**
   * Filters on the specified calendars.
   * @param calendars one or several calendars in which the events will have to be fetched.
   * @return itself.
   */
  public CalendarEventFilter onCalendar(Calendar... calendars) {
    return onCalendar(Arrays.asList(calendars));
  }

  /**
   * Filters on the specified calendars.
   * @param calendars one or several calendars in which the events will have to be fetched.
   * @return itself.
   */
  public CalendarEventFilter onCalendar(List<Calendar> calendars) {
    this.calendars.addAll(calendars);
    return this;
  }

  /**
   * Filters on the specified participants for an event. A participant can be the author or an
   * attendee of an event.
   * @param users the users in Silverpeas that participate for at least one event.
   * @return itself.
   */
  public CalendarEventFilter onParticipants(User... users) {
    return onParticipants(Arrays.asList(users));
  }

  /**
   * Filters on the specified participants for an event. A participant can be the author or an
   * attendee of an event.
   * @param users the users in Silverpeas that participate for at least one event.
   * @return itself.
   */
  public CalendarEventFilter onParticipants(Collection<User> users) {
    this.participants.addAll(users);
    return this;
  }

  /**
   * Filters on the specified synchronization datetime limit. Only events being synchronized before
   * the specified datetime will be taken into account.
   * @param dateTime an {@link OffsetDateTime} the last synchronization date of events shouldn't be
   * after.
   * @return itself.
   */
  public CalendarEventFilter onSynchronizationDateLimit(final OffsetDateTime dateTime) {
    this.synchronizationDateLimit = dateTime;
    return this;
  }

  /**
   * Gets a list of calendars on which the events to filter must be planned.
   * @return a list of calendars or an empty list if there is no filter on calendars.
   */
  public List<Calendar> getCalendars() {
    return calendars;
  }

  /**
   * Gets a list of participants that should be concerned by the events to filter. A participant
   * is a Silverpeas user that is the author or an attendee for an event.
   * @return a list of users participating for at least one event or an empty list if there is no
   * filter on participants.
   */
  public List<User> getParticipants() {
    return participants;
  }

  /**
   * Gets the datetime before which all the last synchronization date of events should be.
   * @return either a datetime or nothing if the criterion on the events synchronization date isn't
   * set.
   */
  public Optional<OffsetDateTime> getSynchronizationDateLimit() {
    return Optional.ofNullable(synchronizationDateLimit);
  }
}
  