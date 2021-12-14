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

package org.silverpeas.core.calendar.icalendar;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.calendar.Attendee;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.calendar.CalendarEventStubBuilder;
import org.silverpeas.core.calendar.CalendarMockBuilder;
import org.silverpeas.core.calendar.DayOfWeekOccurrence;
import org.silverpeas.core.calendar.Priority;
import org.silverpeas.core.calendar.Recurrence;
import org.silverpeas.core.calendar.ical4j.ICal4JDateCodec;
import org.silverpeas.core.calendar.ical4j.ICal4JExporter;
import org.silverpeas.core.calendar.ical4j.ICal4JRecurrenceCodec;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.core.importexport.ExportDescriptor;
import org.silverpeas.core.importexport.ExportException;
import org.silverpeas.core.persistence.datasource.OperationContext;
import org.silverpeas.core.persistence.datasource.model.jpa.JpaPersistOperation;
import org.silverpeas.core.persistence.datasource.model.jpa.JpaUpdateOperation;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.TestManagedBean;
import org.silverpeas.core.test.extention.TestManagedBeans;
import org.silverpeas.core.test.extention.TestedBean;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.StringUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.silverpeas.core.calendar.Attendee.PresenceStatus.INFORMATIVE;
import static org.silverpeas.core.calendar.Attendee.PresenceStatus.OPTIONAL;
import static org.silverpeas.core.calendar.VisibilityLevel.PRIVATE;
import static org.silverpeas.core.calendar.icalendar.ICalendarExporter.CALENDAR;
import static org.silverpeas.core.calendar.icalendar.ICalendarExporter.HIDE_PRIVATE_DATA;
import static org.silverpeas.core.util.CollectionUtil.asList;

/**
 * @author Yohann Chastagnier
 */
@EnableSilverTestEnv
@TestManagedBeans({JpaPersistOperation.class, JpaUpdateOperation.class})
class ICal4JExporterTest {

  private static final Calendar calendar = CalendarMockBuilder.from("instanceId")
      .withId("calendarUuid")
      .atZoneId(ZoneId.of("Europe/Paris"))
      .build();

  @TestManagedBean
  private final ICal4JDateCodec dateCodec = new ICal4JDateCodec();
  @SuppressWarnings("unused")
  @TestManagedBean
  private final ICal4JRecurrenceCodec recurrenceCodec = new ICal4JRecurrenceCodec(dateCodec);
  @SuppressWarnings("unused")
  @TestedBean
  private ICal4JExporter iCalendarExporter;
  private User creator;

  @BeforeEach
  public void setup() {
    creator = mock(User.class);
    when(creator.getId()).thenReturn("creatorId");
    when(creator.getDisplayedName()).thenReturn("Creator Test");
    when(creator.geteMail()).thenReturn("creator.test@silverpeas.org");
    when(UserProvider.get()
        .getUser(creator.getId())).thenReturn(creator);

    OperationContext.fromUser("0");
  }

  @Test
  void undefinedListOfEvents() {
    assertThrows(ExportException.class, () -> iCalendarExporter.exports(
        ExportDescriptor.withOutputStream(new ByteArrayOutputStream())
            .withParameter(CALENDAR, calendar), () -> null));
  }

  @Test
  void undefinedCalendar() {
    assertThrows(ExportException.class, () -> iCalendarExporter.exports(
        ExportDescriptor.withOutputStream(new ByteArrayOutputStream()), () -> {
          List<CalendarEvent> calendarEvents = emptyList();
          //noinspection RedundantOperationOnEmptyContainer
          return calendarEvents.stream();
        }));
  }

  @Test
  void emptyListOfEvents() throws ExportException {
    exportAndVerifyResult(newExportDescriptor(), emptyList(), "ical4j_export_no_event.txt");
  }

  @Test
  void simpleOneOfTwoDaysDuration() throws ExportException {
    // between December 14 and December 15 == [December 14, December 16[
    CalendarEvent event = withTwoDaysDuration().build();

    exportAndVerifyResult(newExportDescriptor(), singletonList(event),
        "ical4j_export_simple_two_days_duration.txt");
  }

  @Test
  void simpleOneOfTwoDaysDurationButHiddenDataWhenPrivate() throws ExportException {
    // between December 14 and December 15 == [December 14, December 16[
    CalendarEvent event = withTwoDaysDuration().build();

    exportAndVerifyResult(newHiddenDataExportDescriptor(), singletonList(event),
        "ical4j_export_simple_two_days_duration.txt");
  }

  @Test
  void simplePrivateOneOfTwoDaysDurationButHiddenDataWhenPrivate() throws ExportException {
    // between December 14 and December 15 == [December 14, December 16[
    CalendarEvent event = withTwoDaysDuration().withVisibilityLevel(PRIVATE)
        .build();

    exportAndVerifyResult(newHiddenDataExportDescriptor(), singletonList(event),
        "ical4j_export_simple_two_days_duration_hidden_data.txt");
  }

  public CalendarEventStubBuilder withTwoDaysDuration() {
    return CalendarEventStubBuilder.from(Period.between(date("2016-12-14"), date("2016-12-16")))
        .plannedOn(calendar)
        .withId("EVENT-UUID")
        .withTitle("EVENT-TITLE")
        .withDescription("EVENT-DESCRIPTION <a href=\"#\">Click me...</a> !!!")
        .withLocation("Grenoble")
        .withAttribute("url", "http://www.silverpeas.org/events/EVENT-UUID")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"));
  }

  @Test
  void simpleOneWithTwoHoursDuration() throws ExportException {
    // between December 14, 12H30 and December 14, 14:30 ==  December 14 [12H30, 14H30[
    CalendarEvent event = CalendarEventStubBuilder.from(
            Period.between(datetime("2016-12-14T12:30:00Z"), datetime("2016-12-14T14:30:00Z")))
        .plannedOn(calendar)
        .withId("EVENT-UUID")
        .withTitle("EVENT-TITLE")
        .withDescription("EVENT-DESCRIPTION <a href=\"#\">Click me...</a> !!!")
        .withLocation("Grenoble")
        .withAttribute("url", "http://www.silverpeas.org/events/EVENT-UUID")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
        .build();

    exportAndVerifyResult(newExportDescriptor(), singletonList(event),
        "ical4j_export_simple_two_hours_duration.txt");
  }

  @Test
  void categorizedOneOfOneDayDuration() throws ExportException {
    CalendarEvent event =
        CalendarEventStubBuilder.from(Period.between(date("2016-12-25"), date("2016-12-25")))
            .plannedOn(calendar)
            .withId("EVENT-UUID")
            .withTitle("EVENT-TITLE-CATEGORIZED")
            .withCategories("Work", "Project")
            .withCreationDate(datetime("2016-12-01T14:30:00Z"))
            .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
            .build();

    exportAndVerifyResult(newExportDescriptor(), singletonList(event),
        "ical4j_export_categorized_one_day_duration.txt");
  }

  @Test
  void oneOfTwoHoursDurationAndDailyRecurrence() throws ExportException {
    CalendarEvent event = CalendarEventStubBuilder.from(
            Period.between(datetime("2016-12-14T12:30:00Z"), datetime("2016-12-14T14:30:00Z")))
        .plannedOn(calendar)
        .withId("EVENT-UUID")
        .withTitle("EVENT-TITLE")
        .withDescription("EVENT-DESCRIPTION <a href=\"#\">Click me...</a> !!!")
        .withLocation("Grenoble")
        .withAttribute("url", "http://www.silverpeas.org/events/EVENT-UUID")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
        .withRecurrence(Recurrence.every(TimeUnit.DAY))
        .build();

    exportAndVerifyResult(newExportDescriptor(), singletonList(event),
        "ical4j_export_recurrent_two_hours_duration.txt");
  }

  @Test
  void oneOfTwoHoursDurationAndDailyRecurrenceUntil10Days() throws ExportException {
    CalendarEvent event = CalendarEventStubBuilder.from(
            Period.between(datetime("2016-12-14T12:30:00Z"), datetime("2016-12-14T14:30:00Z")))
        .plannedOn(calendar)
        .withId("EVENT-UUID")
        .withTitle("EVENT-TITLE")
        .withDescription("EVENT-DESCRIPTION <a href=\"#\">Click me...</a> !!!")
        .withLocation("Grenoble")
        .withAttribute("url", "http://www.silverpeas.org/events/EVENT-UUID")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
        .withRecurrence(Recurrence.every(TimeUnit.DAY)
            .until(datetime("2016-12-24T09:30:00Z")))
        .build();

    exportAndVerifyResult(newExportDescriptor(), singletonList(event),
        "ical4j_export_recurrent_two_hours_duration_until_10_days.txt");
  }

  @Test
  void oneOfTwoHoursDurationAndDailyRecurrenceWithExceptions() throws ExportException {
    CalendarEvent event = CalendarEventStubBuilder.from(
            Period.between(datetime("2016-12-14T12:30:00Z"), datetime("2016-12-14T14:30:00Z")))
        .plannedOn(calendar)
        .withId("EVENT-UUID")
        .withTitle("EVENT-TITLE")
        .withDescription("EVENT-DESCRIPTION <a href=\"#\">Click me...</a> !!!")
        .withLocation("Grenoble")
        .withAttribute("url", "http://www.silverpeas.org/events/EVENT-UUID")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
        .withRecurrence(Recurrence.every(TimeUnit.DAY)
            .excludeEventOccurrencesStartingAt(date("2016-12-21"),
                datetime("2016-12-27T11:00:00Z")))
        .build();

    exportAndVerifyResult(newExportDescriptor(), singletonList(event),
        "ical4j_export_recurrent_with_exceptions_two_hours_duration.txt");
  }

  @Test
  void oneOfOneDayDurationAndDailyRecurrenceUntil10Days() throws ExportException {
    CalendarEvent event =
        CalendarEventStubBuilder.from(Period.between(date("2016-12-14"), date("2016-12-15")))
            .plannedOn(calendar)
            .withId("EVENT-UUID")
            .withTitle("EVENT-TITLE")
            .withDescription("EVENT-DESCRIPTION <a href=\"#\">Click me...</a> !!!")
            .withLocation("Grenoble")
            .withAttribute("url", "http://www.silverpeas.org/events/EVENT-UUID")
            .withCreationDate(datetime("2016-12-01T14:30:00Z"))
            .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
            .withRecurrence(Recurrence.every(TimeUnit.DAY)
                .until(datetime("2016-12-24T09:00:00Z")))
            .build();

    exportAndVerifyResult(newExportDescriptor(), singletonList(event),
        "ical4j_export_recurrent_one_day_duration_until_10_days.txt");
  }

  @Test
  void severalOfOneHourDurationAndDailyRecurrence() throws ExportException {
    CalendarEvent event1 = CalendarEventStubBuilder.from(
            Period.between(datetime("2016-12-15T12:32:00Z"), datetime("2016-12-15T13:32:00Z")))
        .plannedOn(calendar)
        .withId("EVENT-UUID-1")
        .withExternalId("EXT-EVENT-UUID-1")
        .withTitle("EVENT-TITLE-1")
        .withPriority(Priority.HIGH)
        .withVisibilityLevel(PRIVATE)
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
        .withRecurrence(Recurrence.every(TimeUnit.DAY))
        .build();
    CalendarEvent event2 = CalendarEventStubBuilder.from(
            Period.between(datetime("2016-12-15T13:50:00Z"), datetime("2016-12-15T14:50:00Z")))
        .plannedOn(calendar)
        .withId("EVENT-UUID-2")
        .withTitle("EVENT-TITLE-2")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
        .withRecurrence(Recurrence.every(3, TimeUnit.DAY))
        .build();
    CalendarEvent event3 = CalendarEventStubBuilder.from(
            Period.between(datetime("2016-12-15T15:27:00Z"), datetime("2016-12-15T16:27:00Z")))
        .plannedOn(calendar)
        .withId("EVENT-UUID-3")
        .withTitle("EVENT-TITLE-3")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
        .withRecurrence(Recurrence.every(4, TimeUnit.DAY)
            .until(datetime("2016-12-31T15:27:00Z")))
        .build();
    CalendarEvent event4 = CalendarEventStubBuilder.from(
            Period.between(datetime("2016-12-15T18:45:00Z"), datetime("2016-12-15T20:15:00Z")))
        .plannedOn(calendar)
        .withId("EVENT-UUID-4")
        .withTitle("EVENT-TITLE-4")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
        .withRecurrence(Recurrence.every(2, TimeUnit.DAY)
            .until(10))
        .build();
    CalendarEvent event5 = CalendarEventStubBuilder.from(
            Period.between(datetime("2016-12-15T20:00:00Z"), datetime("2016-12-15T20:15:00Z")))
        .plannedOn(calendar)
        .withId("EVENT-UUID-5")
        .withTitle("EVENT-TITLE-5")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
        .withRecurrence(Recurrence.every(TimeUnit.DAY)
            .excludeEventOccurrencesStartingAt(date("2016-12-18"), date("2016-12-20"),
                date("2016-12-25")))
        .build();
    CalendarEvent event6 = CalendarEventStubBuilder.from(
            Period.between(datetime("2016-12-15T20:30:00Z"), datetime("2016-12-15T20:45:00Z")))
        .plannedOn(calendar)
        .withId("EVENT-UUID-6")
        .withTitle("EVENT-TITLE-6")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
        .withRecurrence(Recurrence.every(2, TimeUnit.WEEK)
            .on(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))
        .build();
    CalendarEvent event7 = CalendarEventStubBuilder.from(
            Period.between(datetime("2016-12-15T21:00:00Z"), datetime("2016-12-15T21:15:00Z")))
        .plannedOn(calendar)
        .withId("EVENT-UUID-7")
        .withTitle("EVENT-TITLE-7")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
        .withRecurrence(Recurrence.every(1, TimeUnit.MONTH)
            .on(DayOfWeekOccurrence.nth(3, DayOfWeek.FRIDAY)))
        .build();
    CalendarEvent event8 = CalendarEventStubBuilder.from(
            Period.between(datetime("2016-12-15T21:15:00Z"), datetime("2016-12-15T21:30:00Z")))
        .plannedOn(calendar)
        .withId("EVENT-UUID-8")
        .withTitle("EVENT-TITLE-8")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
        .withRecurrence(Recurrence.every(TimeUnit.MONTH))
        .build();
    CalendarEvent event9 = CalendarEventStubBuilder.from(
            Period.between(datetime("2016-12-15T21:45:00Z"), datetime("2016-12-15T22:00:00Z")))
        .plannedOn(calendar)
        .withId("EVENT-UUID-9")
        .withTitle("EVENT-TITLE-9")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
        .withRecurrence(Recurrence.every(TimeUnit.YEAR))
        .build();

    exportAndVerifyResult(newExportDescriptor(),
        asList(event1, event2, event3, event4, event5, event6, event7, event8, event9),
        "ical4j_export_several_with_recurrence.txt");
  }

  @Test
  void severalOnAllDaysAndDailyRecurrence() throws ExportException {
    CalendarEvent event1 =
        CalendarEventStubBuilder.from(Period.between(date("2016-12-15"), date("2016-12-16")))
            .plannedOn(calendar)
            .withId("EVENT-UUID-1")
            .withExternalId("EXT-EVENT-UUID-1")
            .withTitle("EVENT-TITLE-1")
            .withCreationDate(datetime("2016-12-01T14:30:00Z"))
            .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
            .withRecurrence(Recurrence.every(TimeUnit.DAY))
            .build();
    CalendarEvent event2 =
        CalendarEventStubBuilder.from(Period.between(date("2016-12-15"), date("2016-12-16")))
            .plannedOn(calendar)
            .withId("EVENT-UUID-2")
            .withTitle("EVENT-TITLE-2")
            .withCreationDate(datetime("2016-12-01T14:30:00Z"))
            .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
            .withRecurrence(Recurrence.every(3, TimeUnit.DAY))
            .build();
    CalendarEvent event3 =
        CalendarEventStubBuilder.from(Period.between(date("2016-12-15"), date("2016-12-16")))
            .plannedOn(calendar)
            .withId("EVENT-UUID-3")
            .withTitle("EVENT-TITLE-3")
            .withCreationDate(datetime("2016-12-01T14:30:00Z"))
            .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
            .withRecurrence(Recurrence.every(4, TimeUnit.DAY)
                .until(date("2016-12-31")))
            .build();
    CalendarEvent event4 =
        CalendarEventStubBuilder.from(Period.between(date("2016-12-15"), date("2016-12-16")))
            .plannedOn(calendar)
            .withId("EVENT-UUID-4")
            .withTitle("EVENT-TITLE-4")
            .withCreationDate(datetime("2016-12-01T14:30:00Z"))
            .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
            .withRecurrence(Recurrence.every(2, TimeUnit.DAY)
                .until(10))
            .build();
    CalendarEvent event5 =
        CalendarEventStubBuilder.from(Period.between(date("2016-12-15"), date("2016-12-16")))
            .plannedOn(calendar)
            .withId("EVENT-UUID-5")
            .withTitle("EVENT-TITLE-5")
            .withCreationDate(datetime("2016-12-01T14:30:00Z"))
            .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
            .withRecurrence(Recurrence.every(TimeUnit.DAY)
                .excludeEventOccurrencesStartingAt(date("2016-12-18"), date("2016-12-20"),
                    date("2016-12-25")))
            .build();
    CalendarEvent event6 =
        CalendarEventStubBuilder.from(Period.between(date("2016-12-15"), date("2016-12-16")))
            .plannedOn(calendar)
            .withId("EVENT-UUID-6")
            .withTitle("EVENT-TITLE-6")
            .withCreationDate(datetime("2016-12-01T14:30:00Z"))
            .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
            .withRecurrence(Recurrence.every(2, TimeUnit.WEEK)
                .on(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))
            .build();
    CalendarEvent event7 =
        CalendarEventStubBuilder.from(Period.between(date("2016-12-15"), date("2016-12-16")))
            .plannedOn(calendar)
            .withId("EVENT-UUID-7")
            .withTitle("EVENT-TITLE-7")
            .withCreationDate(datetime("2016-12-01T14:30:00Z"))
            .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
            .withRecurrence(Recurrence.every(1, TimeUnit.MONTH)
                .on(DayOfWeekOccurrence.nth(3, DayOfWeek.FRIDAY)))
            .build();
    CalendarEvent event8 =
        CalendarEventStubBuilder.from(Period.between(date("2016-12-15"), date("2016-12-16")))
            .plannedOn(calendar)
            .withId("EVENT-UUID-8")
            .withTitle("EVENT-TITLE-8")
            .withCreationDate(datetime("2016-12-01T14:30:00Z"))
            .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
            .withRecurrence(Recurrence.every(TimeUnit.MONTH))
            .build();
    CalendarEvent event9 =
        CalendarEventStubBuilder.from(Period.between(date("2016-12-15"), date("2016-12-16")))
            .plannedOn(calendar)
            .withId("EVENT-UUID-9")
            .withTitle("EVENT-TITLE-9")
            .withCreationDate(datetime("2016-12-01T14:30:00Z"))
            .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
            .withRecurrence(Recurrence.every(TimeUnit.YEAR))
            .build();

    exportAndVerifyResult(newExportDescriptor(),
        asList(event1, event2, event3, event4, event5, event6, event7, event8, event9),
        "ical4j_export_several_with_recurrence_on_all_day.txt");
  }

  @Test
  void simpleOneOfTwoDayDurationWithAttendees() throws ExportException {
    User user = mock(User.class);
    when(user.getId()).thenReturn("userId");
    when(user.getDisplayedName()).thenReturn("User Test");
    when(user.geteMail()).thenReturn("user.test@silverpeas.org");
    when(UserProvider.get()
        .getUser(user.getId())).thenReturn(user);
    CalendarEvent event =
        CalendarEventStubBuilder.from(Period.between(date("2016-12-14"), date("2016-12-16")))
            .plannedOn(calendar)
            .withId("EVENT-UUID-ATTENDEES")
            .withTitle("EVENT-TITLE")
            .withCreator(creator)
            .withCreationDate(datetime("2016-12-01T14:30:00Z"))
            .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
            .withAttendee(user, a -> {
              a.setPresenceStatus(OPTIONAL);
              a.accept();
            })
            .withAttendee("external.1@silverpeas.org", a -> a.setPresenceStatus(INFORMATIVE))
            .withAttendee("external.2@silverpeas.org", Attendee::decline)
            .build();

    exportAndVerifyResult(newExportDescriptor(), singletonList(event),
        "ical4j_export_simple_two_days_duration_with_attendees.txt");
  }

  @Test
  void oneWithRecurrenceAndWithAttendees() throws ExportException {
    User user = mock(User.class);
    when(user.getId()).thenReturn("userId");
    when(user.getDisplayedName()).thenReturn("User Test");
    when(user.geteMail()).thenReturn("user.test@silverpeas.org");
    when(UserProvider.get()
        .getUser(user.getId())).thenReturn(user);
    CalendarEvent event =
        CalendarEventStubBuilder.from(Period.between(date("2016-11-30"), date("2016-11-30")))
            .plannedOn(calendar)
            .withId("EVENT-UUID-RECURRENCE-ATTENDEES")
            .withTitle("EVENT-TITLE")
            .withRecurrence(Recurrence.every(TimeUnit.DAY)
                .until(10))
            .withCreator(creator)
            .withCreationDate(datetime("2016-12-01T14:30:00Z"))
            .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
            .withAttendee(user, a -> {
              a.setPresenceStatus(OPTIONAL);
              a.accept();
            })
            .withAttendee("external.1@silverpeas.org", a -> a.setPresenceStatus(INFORMATIVE))
            .withAttendee("external.2@silverpeas.org", Attendee::decline)
            .build();

    exportAndVerifyResult(newExportDescriptor(), singletonList(event),
        "ical4j_export_one_with_recurrence_and_with_attendees.txt");
  }

  @Test
  void oneWithRecurrenceAndWithAttendeesWhichAnsweredOnDate() throws ExportException {
    CalendarEvent event = withRecurrenceAndWithAttendeesWhichAnsweredOnDate().build();

    exportAndVerifyResult(newExportDescriptor(), singletonList(event),
        "ical4j_export_one_with_recurrence_and_with_attendees_which_answered_on_date.txt");
  }

  @Test
  void oneWithRecurrenceAndWithAttendeesWhichAnsweredOnDateButHiddenDataWhenPrivate()
      throws ExportException {
    CalendarEvent event = withRecurrenceAndWithAttendeesWhichAnsweredOnDate().build();

    exportAndVerifyResult(newHiddenDataExportDescriptor(), singletonList(event),
        "ical4j_export_one_with_recurrence_and_with_attendees_which_answered_on_date.txt");
  }

  @Test
  void onePrivateWithRecurrenceAndWithAttendeesWhichAnsweredOnDateButHiddenDataWhenPrivate()
      throws ExportException {
    CalendarEvent event =
        withRecurrenceAndWithAttendeesWhichAnsweredOnDate().withVisibilityLevel(PRIVATE)
            .build();

    exportAndVerifyResult(newHiddenDataExportDescriptor(), singletonList(event),
        "ical4j_export_one_with_recurrence_and_with_attendees_which_answered_on_date_hidden_data" +
            ".txt");
  }

  public CalendarEventStubBuilder withRecurrenceAndWithAttendeesWhichAnsweredOnDate() {
    User user = mock(User.class);
    when(user.getId()).thenReturn("userId");
    when(user.getDisplayedName()).thenReturn("User Test");
    when(user.geteMail()).thenReturn("user.test@silverpeas.org");
    when(UserProvider.get()
        .getUser(user.getId())).thenReturn(user);
    final String externalAttendeeId_1 = "external.1@silverpeas.org";
    final String externalAttendeeId_2 = "external.2@silverpeas.org";
    return CalendarEventStubBuilder.from(Period.between(date("2016-11-30"), date("2016-11-30")))
        .plannedOn(calendar)
        .withId("EVENT-UUID-RECURRENCE-ATTENDEES-ON-DATE-ANSWER")
        .withTitle("EVENT-TITLE")
        .withRecurrence(Recurrence.every(TimeUnit.DAY)
            .until(10))
        .withCreator(creator)
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
        .withAttendee(user, a -> {
          a.setPresenceStatus(OPTIONAL);
          a.accept();
        })
        .withAttendee(externalAttendeeId_1, a -> a.setPresenceStatus(INFORMATIVE))
        .withAttendee(externalAttendeeId_2, Attendee::decline)
        .withOccurrenceOn(Period.between(date("2016-12-06"), date("2016-12-06")), o -> {
          o.getAttendees()
              .get(user.getId())
              .ifPresent(Attendee::decline);
          o.getAttendees()
              .get(externalAttendeeId_1)
              .ifPresent(Attendee::decline);
        })
        .withOccurrenceOn(Period.between(date("2016-12-09"), date("2016-12-10")),
            o -> o.getAttendees()
                .get(user.getId())
                .ifPresent(Attendee::tentativelyAccept))
        .withOccurrenceOn(Period.between(date("2016-12-10"), date("2016-12-11")),
            o -> o.getAttendees()
                .get(externalAttendeeId_2)
                .ifPresent(Attendee::accept));
  }

  @Test
  void oneWithRecurrenceAndWithAttendeesDifferences() throws ExportException {
    User user = mock(User.class);
    when(user.getId()).thenReturn("userId");
    when(user.getDisplayedName()).thenReturn("User Test");
    when(user.geteMail()).thenReturn("user.test@silverpeas.org");
    when(UserProvider.get()
        .getUser(user.getId())).thenReturn(user);
    final String externalAttendeeId_1 = "external.1@silverpeas.org";
    final String externalAttendeeId_2 = "external.2@silverpeas.org";
    CalendarEvent event =
        CalendarEventStubBuilder.from(Period.between(date("2016-11-30"), date("2016-11-30")))
            .plannedOn(calendar)
            .withId("EVENT-UUID-RECURRENCE-ATTENDEES-ON-DATE-ANSWER")
            .withTitle("EVENT-TITLE")
            .withRecurrence(Recurrence.every(TimeUnit.DAY)
                .until(10))
            .withCreator(creator)
            .withCreationDate(datetime("2016-12-01T14:30:00Z"))
            .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
            .withAttendee(user, a -> {
              a.setPresenceStatus(OPTIONAL);
              a.accept();
            })
            .withAttendee(externalAttendeeId_1, a -> a.setPresenceStatus(INFORMATIVE))
            .withAttendee(externalAttendeeId_2, Attendee::decline)
            .withOccurrenceOn(
                Period.between(datetime("2016-12-06T17:30:00Z"), datetime("2016-12-06T18:30:00Z")),
                o -> {
                  o.getAttendees()
                      .removeIf(a -> user.getId()
                          .equals(a.getId()));
                  o.getAttendees()
                      .get(externalAttendeeId_1)
                      .ifPresent(Attendee::decline);
                })
            .withOccurrenceOn(Period.between(date("2016-12-09"), date("2016-12-10")), o -> {
              o.getAttendees()
                  .get(user.getId())
                  .ifPresent(Attendee::tentativelyAccept);
              o.getAttendees()
                  .removeIf(a -> externalAttendeeId_2.equals(a.getId()));
            })
            .build();

    exportAndVerifyResult(newExportDescriptor(), singletonList(event),
        "ical4j_export_one_with_recurrence_and_with_attendees_differences.txt");
  }

  /**
   * Centralization of verification.<br>
   * <p>
   * The mechanism is the following:<br>
   * <p/>
   * the first parameter represent the list of calendar events to export and the second one is the
   * name of the file that contains the expected result.<br> Each lines starting with '#' character
   * is ignored.<br> If the file content is equal to the result of export, the test is successfully
   * verified.<br> If not, the different lines between the file content and the export result are
   * logged to the console.<br> Only event parts are verified from the contents.
   * </p>
   * @param descriptor descriptor about the iCal export process
   * @param fileNameOfExpectedResult the name of the file that contains the expected export result.
   */
  @SuppressWarnings("Duplicates")
  private void exportAndVerifyResult(final ExportDescriptor descriptor,
      List<CalendarEvent> calendarEvents, String fileNameOfExpectedResult) throws ExportException {
    try {

      ByteArrayOutputStream emptyExportResult = new ByteArrayOutputStream();
      iCalendarExporter.exports(ExportDescriptor.withOutputStream(emptyExportResult)
          .withParameter(CALENDAR, calendar), Stream::empty);

      List<String> empty = IOUtils.readLines(new StringReader(emptyExportResult.toString()));
      empty.remove(empty.size() - 1);

      iCalendarExporter.exports(descriptor, calendarEvents::stream);

      StringReader current = new StringReader(descriptor.getOutputStream().toString());
      StringReader expected = new StringReader(getFileContent(fileNameOfExpectedResult));

      final List<String> currentContentLines = IOUtils.readLines(current);
      currentContentLines.remove(currentContentLines.size() - 1);
      Iterator<String> it = currentContentLines.iterator();
      while (it.hasNext() && !empty.isEmpty()) {
        String currentLine = it.next();
        String expectedLine = empty.remove(0);
        assertThat(expectedLine, is(currentLine));
        it.remove();
      }

      // Line to ignore from expected result extracted from a file.
      final List<String> expectedContentLines = IOUtils.readLines(expected);
      expectedContentLines.removeIf(currentExpectedLine -> currentExpectedLine.startsWith("#"));

      String currentContent = StringUtil.join(currentContentLines, "\n");
      String expectedContent = StringUtil.join(expectedContentLines, "\n");

      // Removing DTSTAMP
      currentContent = currentContent.replaceAll("DTSTAMP.+\n",
          "DTSTAMP:VALUE IS NOT VERIFIED BUT IS MANDATORY\n");

      assertThat(currentContent, is(expectedContent));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private ExportDescriptor newExportDescriptor() {
    return ExportDescriptor.withOutputStream(new ByteArrayOutputStream())
        .withParameter(CALENDAR, calendar);
  }

  public ExportDescriptor newHiddenDataExportDescriptor() {
    return newExportDescriptor().withParameter(HIDE_PRIVATE_DATA, true);
  }

  private String getFileContent(String fileName) {
    try (InputStream fileStream = getClass().getResourceAsStream(fileName)) {
      return StringUtil.join(IOUtils.readLines(Objects.requireNonNull(fileStream), Charsets.UTF_8),
          '\n');
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static LocalDate date(String date) {
    return LocalDate.parse(date);
  }

  private static OffsetDateTime datetime(String date) {
    return OffsetDateTime.parse(date);
  }

}