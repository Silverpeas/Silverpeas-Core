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
 * FLOSS exception. You should have received a copy of the text describing
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
import org.silverpeas.core.calendar.event.Attendee;
import org.silverpeas.core.calendar.event.CalendarEvent;
import org.silverpeas.core.calendar.event.ExternalAttendee;
import org.silverpeas.core.calendar.event.InternalAttendee;
import org.silverpeas.core.calendar.event.view.AttendeeParticipationOn;
import org.silverpeas.core.date.Period;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.silverpeas.core.calendar.event.Attendee.ParticipationStatus.AWAITING;
import static org.silverpeas.core.calendar.event.Attendee.PresenceStatus.REQUIRED;

/**
 * @author Yohann Chastagnier
 */
public class CalendarEventMockBuilder {
  
  private CalendarEvent event = mock(CalendarEvent.class);
  private Set<Attendee> attendees = new HashSet<>();
  private Attributes attributes = new Attributes();
  private Categories categories = new Categories();
  
  private CalendarEventMockBuilder() {
    when(event.getVisibilityLevel()).thenReturn(VisibilityLevel.PUBLIC);
    when(event.getPriority()).thenReturn(Priority.NORMAL);
    when(event.getAttendees()).thenReturn(attendees);
    when(event.getAttributes()).thenReturn(attributes);
    when(event.getCategories()).thenReturn(categories);
  }

  public static CalendarEventMockBuilder from(Period period) {
    CalendarEventMockBuilder builder = new CalendarEventMockBuilder();
    builder.withPeriod(period);
    return builder;
  }

  public CalendarEvent build() {
    return event;
  }

  private CalendarEventMockBuilder withPeriod(final Period period) {
    when(event.getStartDateTime()).thenReturn(period.getStartDateTime());
    when(event.getEndDateTime()).thenReturn(period.getEndDateTime());
    when(event.isOnAllDay()).thenReturn(period.isInDays());
    return this;
  }

  public CalendarEventMockBuilder plannedOn(Calendar calendar) {
    when(event.getCalendar()).thenReturn(calendar);
    return this;
  }

  public CalendarEventMockBuilder withTitle(String title) {
    when(event.getTitle()).thenReturn(title);
    return this;
  }

  public CalendarEventMockBuilder withDescription(String description) {
    when(event.getDescription()).thenReturn(description);
    return this;
  }

  public CalendarEventMockBuilder withId(final String id) {
    when(event.getId()).thenReturn(id);
    return this;
  }

  public CalendarEventMockBuilder withCreateDate(final OffsetDateTime createDate) {
    when(event.getCreateDate()).thenReturn(Date.from(createDate.toInstant()));
    return this;
  }

  public CalendarEventMockBuilder withLastUpdateDate(final OffsetDateTime lastUpdateDate) {
    when(event.getLastUpdateDate()).thenReturn(Date.from(lastUpdateDate.toInstant()));
    return this;
  }

  public CalendarEventMockBuilder withLocation(final String location) {
    when(event.getLocation()).thenReturn(location);
    return this;
  }

  public CalendarEventMockBuilder withRecurrence(final Recurrence recurrence) {
    when(event.isRecurrent()).thenReturn(true);
    when(event.getRecurrence()).thenReturn(recurrence);
    return this;
  }

  public CalendarEventMockBuilder withCategories(final String... categories) {
    event.getCategories().addAll(categories);
    return this;
  }

  public CalendarEventMockBuilder withAttribute(final String name, final String value) {
    event.getAttributes().set(name, value);
    return this;
  }

  public CalendarEventMockBuilder withAttendee(final User user,
      Consumer<InternalAttendee> mockedAttendeeConfigurer) {
    InternalAttendee mockedAttendee = mock(InternalAttendee.class);
    final String userId = user.getId();
    final String userDisplayedName = user.getDisplayedName();
    when(mockedAttendee.getId()).thenReturn(userId);
    when(mockedAttendee.getUser()).thenReturn(user);
    when(mockedAttendee.getFullName()).thenReturn(userDisplayedName);
    when(mockedAttendee.getPresenceStatus()).thenReturn(REQUIRED);
    when(mockedAttendee.getParticipationStatus()).thenReturn(AWAITING);
    when(mockedAttendee.getParticipationOn()).thenReturn(new AttendeeParticipationOn());
    mockedAttendeeConfigurer.accept(mockedAttendee);
    attendees.add(mockedAttendee);
    return this;
  }

  public CalendarEventMockBuilder withAttendee(final String email,
      Consumer<ExternalAttendee> mockedAttendeeConfigurer) {
    ExternalAttendee mockedAttendee = mock(ExternalAttendee.class);
    when(mockedAttendee.getId()).thenReturn(email);
    when(mockedAttendee.getFullName()).thenReturn(email);
    when(mockedAttendee.getPresenceStatus()).thenReturn(REQUIRED);
    when(mockedAttendee.getParticipationStatus()).thenReturn(AWAITING);
    when(mockedAttendee.getParticipationOn()).thenReturn(new AttendeeParticipationOn());
    mockedAttendeeConfigurer.accept(mockedAttendee);
    attendees.add(mockedAttendee);
    return this;
  }

  public CalendarEventMockBuilder withCreator(final User creator) {
    final String creatorId = creator.getId();
    when(event.getCreatedBy()).thenReturn(creatorId);
    return this;
  }
}
