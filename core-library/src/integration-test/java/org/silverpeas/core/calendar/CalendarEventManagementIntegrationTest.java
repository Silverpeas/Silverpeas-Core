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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.test.CalendarWarBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Integration tests on the getting, on the saving, on the deletion and on the update of the events
 * in a given calendar.
 *
 * We first check the getting of an existing event works fine so that we can use afterwards the
 * getting method to get the previously saved event in order to check its persisted properties.
 * @author Yohann Chastagnier
 */
@RunWith(Arquillian.class)
public class CalendarEventManagementIntegrationTest extends BaseCalendarTest {

  private static final String CALENDAR_ID = "ID_1";
  private static final String EVENT_TITLE = "an event";
  private static final String EVENT_DESCRIPTION = "a description";
  private static final String AN_ATTRIBUTE_NAME = "location";
  private static final String AN_ATTRIBUTE_VALUE = "L'agence de Grenoble, en Isère (France)";
  private static final String USER_ID = "1";

  @Deployment
  public static Archive<?> createTestArchive() {
    return CalendarWarBuilder.onWarForTestClass(CalendarEventManagementIntegrationTest.class)
        .addAsResource(BaseCalendarTest.TABLE_CREATION_SCRIPT.substring(1))
        .addAsResource(INITIALIZATION_SCRIPT.substring(1))
        .build();
  }

  @Before
  public void verifyInitialData() throws Exception {
    // JPA and Basic SQL query must show that it exists no data
    assertThat(getCalendarEventTableLines(), hasSize(5));
  }

  @Test
  public void getExistingCalendarEventById() {
    Optional<CalendarEvent> mayBeEvent = Calendar.getById(CALENDAR_ID).getEvents().get("ID_E_3");
    assertThat(mayBeEvent.isPresent(), is(true));

    CalendarEvent calendarEvent = mayBeEvent.get();
    assertThat(calendarEvent.getCalendar().getId(), is("ID_1"));
    assertThat(calendarEvent.isOnAllDay(), is(false));
    assertThat(calendarEvent.getStartDateTime(),
        is(Instant.parse("2016-01-08T18:30:00Z").atOffset(ZoneOffset.UTC)));
    assertThat(calendarEvent.getEndDateTime(),
        is(Instant.parse("2016-01-22T13:38:22Z").atOffset(ZoneOffset.UTC)));
    assertThat(calendarEvent.getTitle(), is("title C"));
    assertThat(calendarEvent.getDescription(), is("description C"));
    //assertThat(calendarEvent.getLocation(), is("location C"));
    assertThat(calendarEvent.getVisibilityLevel(), is(VisibilityLevel.PUBLIC));
    assertThat(calendarEvent.getPriority(), is(Priority.HIGH));
    assertThat(calendarEvent.getAttributes().get("location").isPresent(), is(true));
    assertThat(calendarEvent.getAttributes().get("location").get(), is("location C"));
  }

  @Test
  public void saveANewEventOnAllDay() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    LocalDate today = LocalDate.now();
    CalendarEvent expectedEvent = CalendarEvent.on(today)
        .createdBy(USER_ID)
        .withTitle(EVENT_TITLE)
        .withDescription(EVENT_DESCRIPTION)
        .withAttribute(AN_ATTRIBUTE_NAME, AN_ATTRIBUTE_VALUE)
        .saveOn(calendar);
    assertThat(expectedEvent.isPersisted(), is(true));

    Optional<CalendarEvent> mayBeActualEvent = calendar.getEvents().get(expectedEvent.getId());
    assertThat(mayBeActualEvent.isPresent(), is(true));
    assertEventProperties(mayBeActualEvent.get(), expectedEvent);
  }

  @Test
  public void saveANewEventOnSeveralDays() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    LocalDate today = LocalDate.now();
    LocalDate dayAfterTomorrow = today.plusDays(2);
    CalendarEvent expectedEvent = CalendarEvent.on(Period.between(today, dayAfterTomorrow))
        .createdBy(USER_ID)
        .withTitle(EVENT_TITLE)
        .withDescription(EVENT_DESCRIPTION)
        .withAttribute(AN_ATTRIBUTE_NAME, AN_ATTRIBUTE_VALUE)
        .saveOn(calendar);
    assertThat(expectedEvent.isPersisted(), is(true));

    Optional<CalendarEvent> mayBeActualEvent = calendar.getEvents().get(expectedEvent.getId());
    assertThat(mayBeActualEvent.isPresent(), is(true));
    assertEventProperties(mayBeActualEvent.get(), expectedEvent);
  }

  @Test
  public void saveANewEventAtAGivenDateTime() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    OffsetDateTime now = OffsetDateTime.now();
    OffsetDateTime inThreeHours = now.plusHours(3);
    CalendarEvent expectedEvent = CalendarEvent.on(Period.between(now, inThreeHours))
        .createdBy(USER_ID)
        .withTitle(EVENT_TITLE)
        .withDescription(EVENT_DESCRIPTION)
        .withAttribute(AN_ATTRIBUTE_NAME, AN_ATTRIBUTE_VALUE)
        .saveOn(calendar);
    assertThat(expectedEvent.isPersisted(), is(true));

    Optional<CalendarEvent> mayBeActualEvent = calendar.getEvents().get(expectedEvent.getId());
    assertThat(mayBeActualEvent.isPresent(), is(true));
    assertEventProperties(mayBeActualEvent.get(), expectedEvent);
  }

  private void assertEventProperties(final CalendarEvent actual, final CalendarEvent expected) {
    assertThat(actual.getStartDateTime(), is(expected.getStartDateTime()));
    assertThat(actual.getEndDateTime(), is(expected.getEndDateTime()));
    assertThat(actual.isOnAllDay(), is(expected.isOnAllDay()));
    assertThat(actual.getTitle(), is(expected.getTitle()));
    assertThat(actual.getDescription(), is(expected.getDescription()));
    assertThat(actual.getAttributes(), is(expected.getAttributes()));
    assertThat(actual.getVisibilityLevel(), is(expected.getVisibilityLevel()));
    assertThat(actual.getAttendees(), is(expected.getAttendees()));
    assertThat(actual.getCategories(), is(expected.getCategories()));
    assertThat(actual.getRecurrence(), is(Recurrence.NO_RECURRENCE));
  }

}
