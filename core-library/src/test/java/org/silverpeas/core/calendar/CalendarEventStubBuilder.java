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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
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

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.date.Period;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Yohann Chastagnier
 */
public class CalendarEventStubBuilder {

  private Period period;
  private final CalendarComponentTest component = new CalendarComponentTest();
  private final CalendarEvent event = mock(CalendarEvent.class);
  private final List<CalendarEventOccurrence> occurrences = new ArrayList<>();

  private CalendarEventStubBuilder() {
    withVisibilityLevel(VisibilityLevel.PUBLIC);
    withPriority(Priority.NORMAL);
    when(event.getAttendees()).thenReturn(component.getAttendees());
    when(event.getAttributes()).thenReturn(component.getAttributes());
    when(event.getCategories()).thenReturn(new CategorySet());
    when(event.getPersistedOccurrences()).thenReturn(occurrences);
    when(event.asCalendarComponent()).thenReturn(component);
    when(event.getIdentifier()).thenCallRealMethod();
  }

  public static CalendarEventStubBuilder from(Period period) {
    CalendarEventStubBuilder builder = new CalendarEventStubBuilder();
    builder.withPeriod(period);
    return builder;
  }

  public CalendarEvent build() {
    return event;
  }

  private void withPeriod(final Period period) {
    this.period = period;
    when(event.getStartDate()).thenReturn(period.getStartDate());
    when(event.getEndDate()).thenReturn(period.getEndDate());
    when(event.isOnAllDay()).thenReturn(period.isInDays());
    component.setPeriod(period);
  }

  public CalendarEventStubBuilder withExternalId(final String externalId) {
    when(event.getExternalId()).thenReturn(externalId);
    return this;
  }

  public CalendarEventStubBuilder plannedOn(Calendar calendar) {
    when(event.getCalendar()).thenReturn(calendar);
    component.setCalendar(calendar);
    return this;
  }

  public CalendarEventStubBuilder withTitle(String title) {
    when(event.getTitle()).thenReturn(title);
    component.setTitle(title);
    return this;
  }

  public CalendarEventStubBuilder withDescription(String description) {
    when(event.getDescription()).thenReturn(description);
    component.setDescription(description);
    return this;
  }

  public CalendarEventStubBuilder withPriority(Priority priority) {
    when(event.getPriority()).thenReturn(priority);
    component.setPriority(priority);
    return this;
  }

  public CalendarEventStubBuilder withVisibilityLevel(VisibilityLevel visibilityLevel) {
    when(event.getVisibilityLevel()).thenReturn(visibilityLevel);
    return this;
  }

  public CalendarEventStubBuilder withId(final String id) {
    when(event.getId()).thenReturn(id);
    return this;
  }

  public CalendarEventStubBuilder withCreationDate(final OffsetDateTime createDate) {
    when(event.getCreationDate()).thenReturn(Date.from(createDate.toInstant()));
    component.withCreateDate(Date.from(createDate.toInstant()));
    return this;
  }

  public CalendarEventStubBuilder withLastUpdateDate(final OffsetDateTime lastUpdateDate) {
    when(event.getLastUpdateDate()).thenReturn(Date.from(lastUpdateDate.toInstant()));
    component.withLastUpdateDate(Date.from(lastUpdateDate.toInstant()));
    return this;
  }

  public CalendarEventStubBuilder withLocation(final String location) {
    when(event.getLocation()).thenReturn(location);
    component.setLocation(location);
    return this;
  }

  public CalendarEventStubBuilder withRecurrence(final Recurrence recurrence) {
    recurrence.startingAt(period.getStartDate());
    when(event.isRecurrent()).thenReturn(true);
    when(event.getRecurrence()).thenReturn(recurrence);
    return this;
  }

  public CalendarEventStubBuilder withCategories(final String... categories) {
    event.getCategories().addAll(categories);
    return this;
  }

  public CalendarEventStubBuilder withAttribute(final String name, final String value) {
    component.getAttributes().set(name, value);
    return this;
  }

  public CalendarEventStubBuilder withAttendee(final User user, Consumer<Attendee> setup) {
    Attendee attendee = component.getAttendees().add(user);
    setup.accept(attendee);
    return this;
  }

  public CalendarEventStubBuilder withAttendee(final String email, Consumer<Attendee> setup) {
    Attendee attendee = component.getAttendees().add(email);
    setup.accept(attendee);
    return this;
  }

  public CalendarEventStubBuilder withCreator(final User creator) {
    when(event.getCreator()).thenReturn(creator);
    component.withCreatedBy(creator.getId());
    return this;
  }

  public CalendarEventStubBuilder withSequence(long sequence) {
    when(event.getSequence()).thenReturn(sequence);
    component.setSequence(sequence);
    return this;
  }

  public CalendarEventStubBuilder withOccurrenceOn(final Period period,
      Consumer<CalendarEventOccurrence> setup) {
    Temporal startDate = period.getStartDate();
    Temporal endDate = period.getEndDate();
    if (event.isOnAllDay()) {
      startDate = LocalDate.parse(period.getStartDate().toString().replaceFirst("T.+", ""));
      endDate = LocalDate.parse(period.getStartDate().toString().replaceFirst("T.+", ""));
    }
    CalendarEventOccurrence occurrence =
        new CalendarEventOccurrence(event, startDate, endDate);
    occurrence.setPeriod(period);
    setup.accept(occurrence);
    occurrences.add(occurrence);
    return this;
  }

  private static class CalendarComponentTest extends CalendarComponent {
    private Date createDate;
    private Date lastUpdateDate;
    private String createdBy;

    void withCreateDate(final Date createDate) {
      this.createDate = createDate;
    }

    void withLastUpdateDate(final Date lastUpdateDate) {
      this.lastUpdateDate = lastUpdateDate;
    }

    void withCreatedBy(final String createdBy) {
      this.createdBy = createdBy;
    }

    @Override
    public Date getCreationDate() {
      return createDate;
    }

    @Override
    public Date getLastUpdateDate() {
      return lastUpdateDate;
    }

    @Override
    public String getCreatorId() {
      return createdBy;
    }
  }
}
