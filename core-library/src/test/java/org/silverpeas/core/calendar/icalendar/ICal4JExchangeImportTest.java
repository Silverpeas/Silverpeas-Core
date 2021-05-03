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

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.calendar.Attendee;
import org.silverpeas.core.calendar.CalendarComponent;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.calendar.CalendarEventOccurrence;
import org.silverpeas.core.calendar.CalendarEventStubBuilder;
import org.silverpeas.core.calendar.DayOfWeekOccurrence;
import org.silverpeas.core.calendar.Priority;
import org.silverpeas.core.calendar.Recurrence;
import org.silverpeas.core.calendar.VisibilityLevel;
import org.silverpeas.core.calendar.ical4j.ICal4JDateCodec;
import org.silverpeas.core.calendar.ical4j.ICal4JImporter;
import org.silverpeas.core.calendar.ical4j.ICal4JRecurrenceCodec;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.core.importexport.ImportDescriptor;
import org.silverpeas.core.importexport.ImportException;
import org.silverpeas.core.persistence.datasource.OperationContext;
import org.silverpeas.core.persistence.datasource.model.jpa.JpaPersistOperation;
import org.silverpeas.core.persistence.datasource.model.jpa.JpaUpdateOperation;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.TestManagedBean;
import org.silverpeas.core.test.extention.TestManagedBeans;
import org.silverpeas.core.test.extention.TestedBean;
import org.silverpeas.core.util.StringUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.silverpeas.core.util.CollectionUtil.asList;
import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

/**
 * @author Yohann Chastagnier
 */
@EnableSilverTestEnv
@TestManagedBeans({JpaPersistOperation.class, JpaUpdateOperation.class})
public class ICal4JExchangeImportTest {

  @TestManagedBean
  private ICal4JDateCodec dateCodec = new ICal4JDateCodec();
  @TestManagedBean
  private ICal4JRecurrenceCodec recurrenceCodec = new ICal4JRecurrenceCodec(dateCodec);
  @TestedBean
  private ICal4JImporter iCalendarImporter;
  private User creator;

  private BiConsumer<Pair<CalendarEvent, List<CalendarEventOccurrence>>, Pair<CalendarEvent,
      List<CalendarEventOccurrence>>>
      defaultAssert = (actual, expected) -> {
    CalendarEvent actualEvent = actual.getLeft();
    CalendarEvent expectedEvent = expected.getLeft();
    List<CalendarEventOccurrence> actualOccurrences = actual.getRight();
    List<CalendarEventOccurrence> expectedOccurrences = expected.getRight();
    verify(actualEvent, expectedEvent, actualOccurrences, expectedOccurrences);
  };

  @BeforeEach
  public void setup() {
    creator = mock(User.class);
    when(creator.getId()).thenReturn("creatorId");
    when(creator.getDisplayedName()).thenReturn("Creator Test");
    when(creator.geteMail()).thenReturn("creator.test@silverpeas.org");
    when(UserProvider.get().getUser(creator.getId())).thenReturn(creator);

    OperationContext.fromUser("0");
  }

  @Test
  public void undefinedListOfEvents() {
    assertThrows(IllegalArgumentException.class, () ->
    iCalendarImporter.imports(ImportDescriptor.withInputStream(null),
        events -> Function.identity()));
  }

  @Test
  public void emptyFile() {
    assertThrows(ImportException.class,
        () -> importAndVerifyResult("ical4j_import_empty_file.txt", emptyList(), defaultAssert));
  }

  @Test
  public void fileWithoutEvent() throws ImportException {
    importAndVerifyResult("ical4j_import_no_event.txt", emptyList(), defaultAssert);
  }

  @Test
  public void verifyDateConversions() throws ImportException {
    CalendarEvent event1 = CalendarEventStubBuilder
        .from(Period.between(date("2016-12-14"), date("2016-12-16")))
        .withExternalId("EVENT-UUID-LOCAL-DATE")
        .withTitle("EVENT-TITLE")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z")).build();

    CalendarEvent event2 = CalendarEventStubBuilder
        .from(Period.between(
            datetime("2017-01-12T23:15:00Z"),
            datetime("2017-01-13T00:30:00Z")))
        .withExternalId("EVENT-UUID-UTC")
        .withTitle("EVENT-TITLE")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z")).build();

    CalendarEvent event3 = CalendarEventStubBuilder
        .from(Period.between(
            datetime("2017-01-12T16:15:00Z"),
            datetime("2017-01-12T23:30:00Z")))
        .withExternalId("EVENT-UUID-START-WITH-TIMEZONE-END-ON-DEFAULT-TIMEZONE")
        .withTitle("EVENT-TITLE")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z")).build();

    CalendarEvent event4 = CalendarEventStubBuilder
        .from(Period.between(
            datetime("2017-01-13T00:30:00Z"),
            datetime("2017-01-13T01:30:00Z")))
        .withExternalId("EVENT-UUID-UTC-START-EQUALS-END")
        .withTitle("EVENT-TITLE")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z")).build();

    CalendarEvent event5 = CalendarEventStubBuilder
        .from(Period.between(date("2016-12-16"), date("2016-12-17")))
        .withExternalId("EVENT-UUID-LOCAL-DATE-ONE-DAY-WITH-START-AND-END")
        .withTitle("EVENT-TITLE")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z")).build();

    CalendarEvent event6 = CalendarEventStubBuilder
        .from(Period.between(date("2016-12-16"), date("2016-12-17")))
        .withExternalId("EVENT-UUID-LOCAL-DATE-ONE-DAY-WITH-START-AND-END-RELAXED")
        .withTitle("EVENT-TITLE")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z")).build();

    CalendarEvent event7 = CalendarEventStubBuilder
        .from(Period.between(date("2016-12-16"), date("2016-12-17")))
        .withExternalId("EVENT-UUID-LOCAL-DATE-ONE-DAY-START-ONLY")
        .withTitle("EVENT-TITLE")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z")).build();

    importAndVerifyResult("ical4j_import_to_verify_date_conversion.txt",
        asList(event1, event2, event3, event4, event5, event6, event7), defaultAssert);
  }

  @Test
  public void simpleOneOfTwoDaysDuration() throws ImportException {
    CalendarEvent event = CalendarEventStubBuilder
        .from(Period.between(date("2016-12-14"), date("2016-12-16")))
        .withExternalId("EVENT-UUID").withTitle("EVENT-TITLE")
        .withDescription("EVENT-DESCRIPTION <a href=\"#\">Click me...</a> !!!")
        .withLocation("Grenoble")
        .withAttribute("url", "http://www.silverpeas.org/events/EVENT-UUID")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z")).build();

    importAndVerifyResult("ical4j_import_simple_two_days_duration.txt", singletonList(event),
        defaultAssert);
  }

  @Test
  public void categorizedOneOfOneDayDuration() throws ImportException {
    CalendarEvent event = CalendarEventStubBuilder
        .from(Period.between(date("2016-12-25"), date("2016-12-25")))
        .withExternalId("EVENT-UUID").withTitle("EVENT-TITLE-CATEGORIZED")
        .withCategories("Work", "Project")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z")).build();

    importAndVerifyResult("ical4j_import_categorized_one_day_duration.txt", singletonList(event),
        defaultAssert);
  }

  @Test
  public void severalOfOneHourDurationAndDailyRecurrence() throws ImportException {
    CalendarEvent event1 = CalendarEventStubBuilder.from(Period.between(
            datetime("2016-12-15T12:32:00Z"),
            datetime("2016-12-15T13:32:00Z")))
        .withExternalId("EXT-EVENT-UUID-1")
        .withTitle("EVENT-TITLE-1")
        .withPriority(Priority.HIGH)
        .withVisibilityLevel(VisibilityLevel.PRIVATE)
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
        .withRecurrence(Recurrence.every(TimeUnit.DAY)).build();
    CalendarEvent event2 = CalendarEventStubBuilder.from(Period.between(
            datetime("2016-12-15T13:50:00Z"),
            datetime("2016-12-15T14:50:00Z")))
        .withExternalId("EVENT-UUID-2")
        .withTitle("EVENT-TITLE-2")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
        .withRecurrence(Recurrence.every(3, TimeUnit.DAY)).build();
    CalendarEvent event3 = CalendarEventStubBuilder.from(Period.between(
            datetime("2016-12-15T15:27:00Z"),
            datetime("2016-12-15T16:27:00Z")))
        .withExternalId("EVENT-UUID-3").withTitle("EVENT-TITLE-3")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
        .withRecurrence(
            Recurrence.every(4, TimeUnit.DAY).until(datetime("2016-12-31T15:27:00Z")))
        .build();
    CalendarEvent event4 = CalendarEventStubBuilder.from(Period.between(
            datetime("2016-12-15T18:45:00Z"),
            datetime("2016-12-15T20:15:00Z")))
        .withExternalId("EVENT-UUID-4").withTitle("EVENT-TITLE-4")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
        .withRecurrence(Recurrence.every(2, TimeUnit.DAY).until(10)).build();
    CalendarEvent event5 = CalendarEventStubBuilder.from(Period.between(
            datetime("2016-12-15T20:00:00Z"),
            datetime("2016-12-15T20:15:00Z")))
        .withExternalId("EVENT-UUID-5").withTitle("EVENT-TITLE-5")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
        .withRecurrence(
            Recurrence.every(TimeUnit.DAY)
                .excludeEventOccurrencesStartingAt(
                    date("2016-12-18"),
                    date("2016-12-20"),
                    date("2016-12-25"))).build();
    CalendarEvent event6 = CalendarEventStubBuilder.from(Period.between(
            datetime("2016-12-15T20:30:00Z"),
            datetime("2016-12-15T20:45:00Z")))
        .withExternalId("EVENT-UUID-6").withTitle("EVENT-TITLE-6")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
        .withRecurrence(
            Recurrence.every(2, TimeUnit.WEEK)
                .on(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)).build();
    CalendarEvent event7 = CalendarEventStubBuilder.from(Period.between(
            datetime("2016-12-15T21:00:00Z"),
            datetime("2016-12-15T21:15:00Z")))
        .withExternalId("EVENT-UUID-7").withTitle("EVENT-TITLE-7")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
        .withRecurrence(
            Recurrence.every(1, TimeUnit.MONTH).on(DayOfWeekOccurrence.nth(3, DayOfWeek.FRIDAY)))
        .build();
    CalendarEvent event8 = CalendarEventStubBuilder.from(Period.between(
            datetime("2016-12-15T21:15:00Z"),
            datetime("2016-12-15T21:30:00Z")))
        .withExternalId("EVENT-UUID-8").withTitle("EVENT-TITLE-8")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
        .withRecurrence(Recurrence.every(TimeUnit.MONTH)).build();
    CalendarEvent event9 = CalendarEventStubBuilder.from(Period.between(
            datetime("2016-12-15T21:45:00Z"),
            datetime("2016-12-15T22:00:00Z")))
        .withExternalId("EVENT-UUID-9").withTitle("EVENT-TITLE-9")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
        .withRecurrence(Recurrence.every(TimeUnit.YEAR)).build();

    importAndVerifyResult("ical4j_import_several_with_recurrence.txt",
        asList(event1, event2, event3, event4, event5, event6, event7, event8, event9),
        defaultAssert);
  }

  @Test
  public void severalOnAllDaysAndDailyRecurrence() throws ImportException {
    CalendarEvent event1 = CalendarEventStubBuilder
        .from(Period.between(date("2016-12-15"), date("2016-12-15")))
        .withExternalId("EXT-EVENT-UUID-1")
        .withTitle("EVENT-TITLE-1").withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
        .withRecurrence(Recurrence.every(TimeUnit.DAY)).build();
    CalendarEvent event2 = CalendarEventStubBuilder
        .from(Period.between(date("2016-12-15"), date("2016-12-15")))
        .withExternalId("EVENT-UUID-2").withTitle("EVENT-TITLE-2")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
        .withRecurrence(Recurrence.every(3, TimeUnit.DAY)).build();
    CalendarEvent event3 = CalendarEventStubBuilder
        .from(Period.between(date("2016-12-15"), date("2016-12-15")))
        .withExternalId("EVENT-UUID-3").withTitle("EVENT-TITLE-3")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
        .withRecurrence(Recurrence.every(4, TimeUnit.DAY).until(date("2016-12-31")))
        .build();
    CalendarEvent event4 = CalendarEventStubBuilder
        .from(Period.between(date("2016-12-15"), date("2016-12-15")))
        .withExternalId("EVENT-UUID-4").withTitle("EVENT-TITLE-4")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
        .withRecurrence(Recurrence.every(2, TimeUnit.DAY).until(10)).build();
    CalendarEvent event5 = CalendarEventStubBuilder
        .from(Period.between(date("2016-12-15"), date("2016-12-15")))
        .withExternalId("EVENT-UUID-5").withTitle("EVENT-TITLE-5")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z")).withRecurrence(
            Recurrence.every(TimeUnit.DAY)
                .excludeEventOccurrencesStartingAt(date("2016-12-18"),
                    date("2016-12-20"), date("2016-12-25"))).build();
    CalendarEvent event6 = CalendarEventStubBuilder
        .from(Period.between(date("2016-12-15"), date("2016-12-15")))
        .withExternalId("EVENT-UUID-6").withTitle("EVENT-TITLE-6")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z")).withRecurrence(
            Recurrence.every(2, TimeUnit.WEEK)
                .on(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)).build();
    CalendarEvent event7 = CalendarEventStubBuilder
        .from(Period.between(date("2016-12-15"), date("2016-12-15")))
        .withExternalId("EVENT-UUID-7").withTitle("EVENT-TITLE-7")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z")).withRecurrence(
            Recurrence.every(1, TimeUnit.MONTH).on(DayOfWeekOccurrence.nth(3, DayOfWeek.FRIDAY)))
        .build();
    CalendarEvent event8 = CalendarEventStubBuilder
        .from(Period.between(date("2016-12-15"), date("2016-12-15")))
        .withExternalId("EVENT-UUID-8").withTitle("EVENT-TITLE-8")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
        .withRecurrence(Recurrence.every(TimeUnit.MONTH)).build();
    CalendarEvent event9 = CalendarEventStubBuilder
        .from(Period.between(date("2016-12-15"), date("2016-12-15")))
        .withExternalId("EVENT-UUID-9").withTitle("EVENT-TITLE-9")
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
        .withRecurrence(Recurrence.every(TimeUnit.YEAR)).build();

    importAndVerifyResult("ical4j_import_several_with_recurrence_on_all_day.txt",
        asList(event1, event2, event3, event4, event5, event6, event7, event8, event9),
        defaultAssert);
  }

  @Test
  public void simpleOneOfTwoDayDurationWithAttendees() throws ImportException {
    User user = mock(User.class);
    when(user.getId()).thenReturn("userId");
    when(user.getDisplayedName()).thenReturn("User Test");
    when(user.geteMail()).thenReturn("user.test@silverpeas.org");
    CalendarEvent event = CalendarEventStubBuilder
        .from(Period.between(date("2016-12-14"), date("2016-12-16")))
        .withExternalId("EVENT-UUID-ATTENDEES")
        .withTitle("EVENT-TITLE")
        .withCreator(creator)
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
        /*

        NOT YET HANDLED

        .withAttendee(user, a -> {
            a.setPresenceStatus(OPTIONAL);
            a.accept();
        })
        .withAttendee("external.1@silverpeas.org", a ->
            a.setPresenceStatus(INFORMATIVE))
        .withAttendee("external.2@silverpeas.org", a ->
            a.decline())

        */
        .build();

    importAndVerifyResult("ical4j_import_simple_two_days_duration_with_attendees.txt",
        singletonList(event), defaultAssert);
  }

  @Test
  public void oneWithRecurrenceAndWithAttendees() throws ImportException {
    User user = mock(User.class);
    when(user.getId()).thenReturn("userId");
    when(user.getDisplayedName()).thenReturn("User Test");
    when(user.geteMail()).thenReturn("user.test@silverpeas.org");
    CalendarEvent event = CalendarEventStubBuilder
        .from(Period.between(date("2016-11-30"), date("2016-11-30")))
        .withExternalId("EVENT-UUID-RECURRENCE-ATTENDEES")
        .withTitle("EVENT-TITLE")
        .withRecurrence(Recurrence.every(TimeUnit.DAY).until(10))
        .withCreator(creator)
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
        /*

        NOT YET HANDLED

        .withAttendee(user, a -> {
            a.setPresenceStatus(OPTIONAL);
            a.accept();
        })
        .withAttendee("external.1@silverpeas.org", a ->
            a.setPresenceStatus(INFORMATIVE))
        .withAttendee("external.2@silverpeas.org", a ->
            a.decline())

        */
        .build();

    importAndVerifyResult("ical4j_import_one_with_recurrence_and_with_attendees.txt",
        singletonList(event), defaultAssert);
  }

  @Test
  public void oneWithRecurrenceAndWithAttendeesWhichAnsweredOnDate() throws ImportException {
    User user = mock(User.class);
    when(user.getId()).thenReturn("userId");
    when(user.getDisplayedName()).thenReturn("User Test");
    when(user.geteMail()).thenReturn("user.test@silverpeas.org");
    when(UserProvider.get().getUser(user.getId())).thenReturn(user);
    final String externalAttendeeId_1 = "external.1@silverpeas.org";
    final String externalAttendeeId_2 = "external.2@silverpeas.org";
    CalendarEvent event = CalendarEventStubBuilder
        .from(Period.between(date("2016-11-30"), date("2016-11-30")))
        .withExternalId("EVENT-UUID-RECURRENCE-ATTENDEES-ON-DATE-ANSWER")
        .withTitle("EVENT-TITLE")
        .withRecurrence(Recurrence.every(TimeUnit.DAY).until(10))
        .withCreator(creator)
        .withCreationDate(datetime("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(datetime("2016-12-02T09:00:00Z"))
        /*

        NOT YET HANDLED

        .withAttendee(user, a -> {
            a.setPresenceStatus(OPTIONAL);
            a.accept();
        })
        .withAttendee(externalAttendeeId_1, a ->
            a.setPresenceStatus(INFORMATIVE))
        .withAttendee(externalAttendeeId_2, a ->
            a.decline())

        */
        .withOccurrenceOn(Period.between(date("2016-12-06"), date("2016-12-06")),
            o -> {
              o.getAttendees().get(user.getId()).ifPresent(Attendee::decline);
              o.getAttendees().get(externalAttendeeId_1).ifPresent(Attendee::decline);
            })
        .withOccurrenceOn(Period.between(date("2016-12-09"), date("2016-12-10")),
            o -> o.getAttendees().get(user.getId()).ifPresent(Attendee::tentativelyAccept))
        .withOccurrenceOn(Period.between(date("2016-12-10"), date("2016-12-11")),
            o -> o.getAttendees().get(externalAttendeeId_2).ifPresent(Attendee::accept))
        .build();

    importAndVerifyResult(
        "ical4j_import_one_with_recurrence_and_with_attendees_which_answered_on_date.txt",
        singletonList(event), defaultAssert);
  }

  /**
   * Centralization of verification.<br>
   * <p>
   * The mechanism is the following:<br>
   * <p/>
   * the first parameter represent the name of the file that contains events to import and the
   * second one is the list of expected calendar events.<br>
   * Each lines starting with '#' character is ignored.
   * </p>
   * @param fileNameOfImport the name of the file that contains events to import.
   */
  @SuppressWarnings({"unchecked", "Duplicates"})
  private void importAndVerifyResult(String fileNameOfImport, List<CalendarEvent> expectedEvents,
      BiConsumer<Pair<CalendarEvent, List<CalendarEventOccurrence>>, Pair<CalendarEvent,
          List<CalendarEventOccurrence>>> assertConsumer) throws ImportException {

    Map<String, Pair<CalendarEvent, List<CalendarEventOccurrence>>> result = new HashedMap<>();

    iCalendarImporter.imports(ImportDescriptor.withInputStream(new ByteArrayInputStream(
            getFileContent(fileNameOfImport).getBytes(StandardCharsets.UTF_8))),
        events -> result.putAll(events.collect(
            Collectors.toMap(p -> p.getLeft().getExternalId(), Function.identity()))));

    Map<String, Pair<CalendarEvent, List<CalendarEventOccurrence>>> expected =
        expectedEvents.stream().collect(Collectors
            .toMap(CalendarEvent::getExternalId, e -> Pair.of(e, e.getPersistedOccurrences())));

    assertThat("The expected list contains several event with same external id", expected.size(),
        is(expectedEvents.size()));


    assertThat(result.keySet(), containsInAnyOrder(expected.keySet().toArray()));

    result.forEach((i, actualResult) -> {
      Pair<CalendarEvent, List<CalendarEventOccurrence>> expectedResult = expected.get(i);
      assertConsumer.accept(actualResult, expectedResult);
    });
  }

  private void verify(final CalendarEvent actualEvent, final CalendarEvent expectedEvent,
      final List<CalendarEventOccurrence> actualOccurrences,
      final List<CalendarEventOccurrence> expectedOccurrences) {
    verify(actualEvent, expectedEvent);
    assertThat(actualOccurrences, hasSize(expectedOccurrences.size()));
    expectedOccurrences.forEach(expected -> {
      String reason =
          expected.getCalendarEvent().getExternalId() + " - occ - " + expected.getId() + " - ";
      CalendarEventOccurrence actual = actualOccurrences.stream()
          .filter(o -> expected.getOriginalStartDate().equals(o.getOriginalStartDate())).findFirst()
          .orElse(null);
      assertThat(reason + "getId", actual.getId(), is(expected.getId()));
      assertThat(reason + "getId", actual.getOriginalStartDate(), is(expected.getOriginalStartDate()));
      assertThat(reason + "getCalendar", actual.getCalendarEvent().getCalendar(), is(expected.getCalendarEvent().getCalendar()));
      assertThat(reason + "getStartDate", actual.getStartDate(), is(expected.getStartDate()));
      assertThat(reason + "getEndDate", actual.getEndDate(), is(expected.getEndDate()));
      assertThat(reason + "getTitle", actual.getTitle(), is(expected.getTitle()));
      assertThat(reason + "getDescription", actual.getDescription(), is(defaultStringIfNotDefined(expected.getDescription())));
      assertThat(reason + "getLocation", actual.getLocation(), is(defaultStringIfNotDefined(expected.getLocation())));
      assertThat(reason + "getAttendees", actual.getAttendees().stream().collect(Collectors.toSet()), is(expected.getAttendees().stream().collect(Collectors.toSet())));
      assertThat(reason + "getCategories", actual.getCategories(), is(expected.getCategories()));
      assertThat(reason + "getAttributes", actual.getAttributes(), is(expected.getAttributes()));
      assertThat(reason + "getPriority", actual.getPriority(), is(expected.getPriority()));
      assertThat(reason + "getVisibilityLevel", actual.getVisibilityLevel(), is(expected.getVisibilityLevel()));
      verifyComponent(reason, actual.asCalendarComponent(), expected.asCalendarComponent());
    });
  }

  private void verify(final CalendarEvent actual, final CalendarEvent expected) {
    String reason = actual.getExternalId() + " - ";
    assertThat(reason + "getId", actual.getId(), nullValue());
    assertThat(reason + "getId", actual.getId(), is(expected.getId()));
    assertThat(reason + "getExternalId", actual.getExternalId(), is(expected.getExternalId()));
    assertThat(reason + "getCalendar", actual.getCalendar(), is(expected.getCalendar()));
    assertThat(reason + "getStartDate", actual.getStartDate(), is(expected.getStartDate()));
    assertThat(reason + "getEndDate", actual.getEndDate(), is(expected.getEndDate()));
    assertThat(reason + "getTitle", actual.getTitle(), is(expected.getTitle()));
    assertThat(reason + "getDescription", actual.getDescription(), is(defaultStringIfNotDefined(expected.getDescription())));
    assertThat(reason + "getLocation", actual.getLocation(), is(defaultStringIfNotDefined(expected.getLocation())));
    assertThat(reason + "isRecurrent", actual.isRecurrent(), is(expected.isRecurrent()));
    assertThat(reason + "getRecurrence", actual.getRecurrence(), is(expected.getRecurrence()));
    assertThat(reason + "getAttendees", actual.getAttendees().stream().collect(Collectors.toSet()), is(expected.getAttendees().stream().collect(Collectors.toSet())));
    assertThat(reason + "getCategories", actual.getCategories(), is(expected.getCategories()));
    assertThat(reason + "getAttributes", actual.getAttributes(), is(expected.getAttributes()));
    assertThat(reason + "getPriority", actual.getPriority(), is(expected.getPriority()));
    assertThat(reason + "getVisibilityLevel", actual.getVisibilityLevel(), is(expected.getVisibilityLevel()));
    assertThat(reason + "getCreationDate", actual.getCreationDate(), is(expected.getCreationDate()));
    assertThat(reason + "getLastUpdateDate", actual.getLastUpdateDate(), is(expected.getLastUpdateDate()));
    verifyComponent(reason, actual.asCalendarComponent(), expected.asCalendarComponent());
  }

  private void verifyComponent(final String parentReason, final CalendarComponent actual,
      final CalendarComponent expected) {
    String reason = parentReason + "cmp - ";
    assertThat(reason + "getId", actual.getId(), nullValue());
    assertThat(reason + "getId", actual.getId(), is(expected.getId()));
    assertThat(reason + "getCalendar", actual.getCalendar(), is(expected.getCalendar()));
    assertThat(reason + "getStartDate", actual.getPeriod().getStartDate(), is(expected.getPeriod().getStartDate()));
    assertThat(reason + "getEndDate", actual.getPeriod().getEndDate(), is(expected.getPeriod().getEndDate()));
    assertThat(reason + "getTitle", actual.getTitle(), is(expected.getTitle()));
    assertThat(reason + "getDescription", actual.getDescription(), is(defaultStringIfNotDefined(expected.getDescription())));
    assertThat(reason + "getLocation", actual.getLocation(), is(defaultStringIfNotDefined(expected.getLocation())));
    assertThat(reason + "getAttendees", actual.getAttendees().stream().collect(Collectors.toSet()), is(expected.getAttendees().stream().collect(Collectors.toSet())));
    assertThat(reason + "getAttributes", actual.getAttributes(), is(expected.getAttributes()));
    assertThat(reason + "getPriority", actual.getPriority(), is(expected.getPriority()));
    assertThat(reason + "getCreationDate", actual.getCreationDate(), is(expected.getCreationDate()));
    assertThat(reason + "getLastUpdateDate", actual.getLastUpdateDate(), is(expected.getLastUpdateDate()));
  }

  private String getFileContent(String fileName) {
    try (InputStream fileStream = getClass().getResourceAsStream(fileName)) {
      return StringUtil.join(IOUtils.readLines(fileStream), '\n');
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