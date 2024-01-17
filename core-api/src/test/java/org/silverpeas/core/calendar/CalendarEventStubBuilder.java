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

import org.apache.commons.lang3.reflect.FieldUtils;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.function.Consumer;

/**
 * @author Yohann Chastagnier
 */
public class CalendarEventStubBuilder {

  private Period period;
  private CalendarComponentTest component = new CalendarComponentTest();
  private CategorySet categorySet = new CategorySet();
  private CalendarEvent event;
  private String id = "testId";
  private Recurrence recurrence;

  private CalendarEventStubBuilder(Period period) {
    event = CalendarEvent.on(period);
    withVisibilityLevel(VisibilityLevel.PUBLIC);
    withPriority(Priority.NORMAL);
  }

  public static CalendarEventStubBuilder from(Period period) {
    CalendarEventStubBuilder builder = new CalendarEventStubBuilder(period);
    return builder.withPeriod(period);
  }

  public CalendarEvent build() {
    try {
      FieldUtils.writeField(event, "id", UuidIdentifier.from(id), true);
      FieldUtils.writeField(event, "component", component, true);
      FieldUtils.writeField(event, "categories", categorySet, true);
      FieldUtils.writeField(event, "recurrence", recurrence, true);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    return event;
  }

  private CalendarEventStubBuilder withPeriod(final Period period) {
    this.period = period;
    component.setPeriod(period);
    return this;
  }

  public CalendarEventStubBuilder withExternalId(final String externalId) {
    event.withExternalId(externalId);
    return this;
  }

  public CalendarEventStubBuilder plannedOn(Calendar calendar) {
    component.setCalendar(calendar);
    return this;
  }

  public CalendarEventStubBuilder withTitle(String title) {
    component.setTitle(title);
    return this;
  }

  public CalendarEventStubBuilder withDescription(String description) {
    component.setDescription(description);
    return this;
  }

  public CalendarEventStubBuilder withPriority(Priority priority) {
    component.setPriority(priority);
    return this;
  }

  public CalendarEventStubBuilder withVisibilityLevel(VisibilityLevel visibilityLevel) {
    event.withVisibilityLevel(visibilityLevel);
    return this;
  }

  public CalendarEventStubBuilder withId(final String id) {
    this.id = id;
    return this;
  }

  public CalendarEventStubBuilder withCreationDate(final OffsetDateTime createDate) {
    component.withCreateDate(Date.from(createDate.toInstant()));
    return this;
  }

  public CalendarEventStubBuilder withLastUpdateDate(final OffsetDateTime lastUpdateDate) {
    component.withLastUpdateDate(Date.from(lastUpdateDate.toInstant()));
    return this;
  }

  public CalendarEventStubBuilder withLocation(final String location) {
    component.setLocation(location);
    return this;
  }

  public CalendarEventStubBuilder withRecurrence(final Recurrence recurrence) {
    this.recurrence = recurrence;
    recurrence.startingAt(period.getStartDate());
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
    component.withCreatedBy(creator.getId());
    try {
      FieldUtils.writeField(component, "creator", creator, true);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  public CalendarEventStubBuilder withSequence(long sequence) {
    component.setSequence(sequence);
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
