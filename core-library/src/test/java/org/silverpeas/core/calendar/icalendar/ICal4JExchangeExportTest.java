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

package org.silverpeas.core.calendar.icalendar;

import org.apache.commons.io.IOUtils;
import org.jglue.cdiunit.AdditionalPackages;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.CalendarEventMockBuilder;
import org.silverpeas.core.calendar.CalendarMockBuilder;
import org.silverpeas.core.calendar.DayOfWeekOccurrence;
import org.silverpeas.core.calendar.Priority;
import org.silverpeas.core.calendar.Recurrence;
import org.silverpeas.core.calendar.VisibilityLevel;
import org.silverpeas.core.calendar.event.CalendarEvent;
import org.silverpeas.core.calendar.ical4j.ICal4JDateCodec;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.silverpeas.core.calendar.event.Attendee.ParticipationStatus.*;
import static org.silverpeas.core.calendar.event.Attendee.PresenceStatus.INFORMATIVE;
import static org.silverpeas.core.calendar.event.Attendee.PresenceStatus.OPTIONAL;
import static org.silverpeas.core.util.CollectionUtil.asList;

/**
 * @author Yohann Chastagnier
 */
@RunWith(CdiRunner.class)
@AdditionalPackages({ICal4JExchange.class, ICal4JDateCodec.class})
public class ICal4JExchangeExportTest {

  private CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  private Calendar calendar = CalendarMockBuilder.from("instanceId").withId("calendarUuid")
      .atZoneId(ZoneId.of("Europe/Paris")).build();

  @Inject
  private Provider<ICalendarExchange> iCalendarExchangeProvider;

  private User creator;

  @Rule
  public CommonAPI4Test getCommonAPI4Test() {
    return commonAPI4Test;
  }

  @SuppressWarnings("Duplicates")
  @Before
  public void setup() {
    final ICalendarExchange iCalendarExchange = iCalendarExchangeProvider.get();
    assertThat(iCalendarExchange, instanceOf(ICal4JExchange.class));
    commonAPI4Test.injectIntoMockedBeanContainer(iCalendarExchange);

    creator = mock(User.class);
    when(creator.getId()).thenReturn("creatorId");
    when(creator.getDisplayedName()).thenReturn("Creator Test");
    when(creator.geteMail()).thenReturn("creator.test@silverpeas.org");
    when(UserProvider.get().getUser(creator.getId())).thenReturn(creator);
  }

  @Test(expected = NullPointerException.class)
  public void undefinedListOfEvents() throws ICalendarException {
    ICalendarExport.from(calendar, () -> null).to(ByteArrayOutputStream::new);
  }

  @Test
  public void emptyListOfEvents() throws ICalendarException {
    exportAndVerifyResult(emptyList(), "ical4j_export_no_event.txt");
  }

  @Test
  public void simpleOneOfTwoDaysDuration() throws ICalendarException {
    CalendarEvent event = CalendarEventMockBuilder.from(Period
        .between(LocalDate.parse("2016-12-14"), LocalDate.parse("2016-12-15")))
        .plannedOn(calendar)
        .withId("EVENT-UUID")
        .withTitle("EVENT-TITLE")
        .withDescription("EVENT-DESCRIPTION <a href=\"#\">Click me...</a> !!!")
        .withLocation("Grenoble")
        .withAttribute("url", "http://www.silverpeas.org/events/EVENT-UUID")
        .withCreateDate(OffsetDateTime.parse("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(OffsetDateTime.parse("2016-12-02T09:00:00Z"))
        .build();

    exportAndVerifyResult(singletonList(event), "ical4j_export_simple_two_days_duration.txt");
  }

  @Test
  public void categorizedOneOfOneDayDuration() throws ICalendarException {
    CalendarEvent event = CalendarEventMockBuilder.from(Period
        .between(LocalDate.parse("2016-12-25"), LocalDate.parse("2016-12-25")))
        .plannedOn(calendar)
        .withId("EVENT-UUID")
        .withTitle("EVENT-TITLE-CATEGORIZED")
        .withCategories("Work", "Project")
        .withCreateDate(OffsetDateTime.parse("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(OffsetDateTime.parse("2016-12-02T09:00:00Z"))
        .build();

    exportAndVerifyResult(singletonList(event), "ical4j_export_categorized_one_day_duration.txt");
  }

  @Test
  public void severalOfOneHourDurationAndDailyRecurrence() throws ICalendarException {
    CalendarEvent event1 = CalendarEventMockBuilder.from(Period
        .between(OffsetDateTime.parse("2016-12-15T12:32:00Z"),
                 OffsetDateTime.parse("2016-12-15T13:32:00Z")))
        .plannedOn(calendar)
        .withId("EVENT-UUID-1")
        .withExternalId("EXT-EVENT-UUID-1")
        .withTitle("EVENT-TITLE-1")
        .withPriority(Priority.HIGH)
        .withVisibilityLevel(VisibilityLevel.PRIVATE)
        .withCreateDate(OffsetDateTime.parse("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(OffsetDateTime.parse("2016-12-02T09:00:00Z"))
        .withRecurrence(Recurrence.every(TimeUnit.DAY))
        .build();
    CalendarEvent event2 = CalendarEventMockBuilder.from(Period
        .between(OffsetDateTime.parse("2016-12-15T13:50:00Z"),
                 OffsetDateTime.parse("2016-12-15T14:50:00Z")))
        .plannedOn(calendar)
        .withId("EVENT-UUID-2")
        .withTitle("EVENT-TITLE-2")
        .withCreateDate(OffsetDateTime.parse("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(OffsetDateTime.parse("2016-12-02T09:00:00Z"))
        .withRecurrence(Recurrence.every(3, TimeUnit.DAY))
        .build();
    CalendarEvent event3 = CalendarEventMockBuilder.from(Period
        .between(OffsetDateTime.parse("2016-12-15T15:27:00Z"),
                 OffsetDateTime.parse("2016-12-15T16:27:00Z")))
        .plannedOn(calendar)
        .withId("EVENT-UUID-3")
        .withTitle("EVENT-TITLE-3")
        .withCreateDate(OffsetDateTime.parse("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(OffsetDateTime.parse("2016-12-02T09:00:00Z"))
        .withRecurrence(
            Recurrence.every(4, TimeUnit.DAY).upTo(OffsetDateTime.parse("2016-12-31T15:27:00Z")))
        .build();
    CalendarEvent event4 = CalendarEventMockBuilder.from(Period
        .between(OffsetDateTime.parse("2016-12-15T18:45:00Z"),
                 OffsetDateTime.parse("2016-12-15T20:15:00Z")))
        .plannedOn(calendar)
        .withId("EVENT-UUID-4")
        .withTitle("EVENT-TITLE-4")
        .withCreateDate(OffsetDateTime.parse("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(OffsetDateTime.parse("2016-12-02T09:00:00Z"))
        .withRecurrence(
            Recurrence.every(2, TimeUnit.DAY).upTo(10))
        .build();
    CalendarEvent event5 = CalendarEventMockBuilder.from(Period
        .between(OffsetDateTime.parse("2016-12-15T20:00:00Z"),
                 OffsetDateTime.parse("2016-12-15T20:15:00Z")))
        .plannedOn(calendar)
        .withId("EVENT-UUID-5")
        .withTitle("EVENT-TITLE-5")
        .withCreateDate(OffsetDateTime.parse("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(OffsetDateTime.parse("2016-12-02T09:00:00Z"))
        .withRecurrence(
            Recurrence.every(TimeUnit.DAY)
                .excludeEventOccurrencesStartingAt(OffsetDateTime.parse("2016-12-18T20:00:00Z"),
                    OffsetDateTime.parse("2016-12-20T20:00:00Z"),
                    OffsetDateTime.parse("2016-12-25T20:00:00Z")))
        .build();
    CalendarEvent event6 = CalendarEventMockBuilder.from(Period
        .between(OffsetDateTime.parse("2016-12-15T20:30:00Z"),
                 OffsetDateTime.parse("2016-12-15T20:45:00Z")))
        .plannedOn(calendar)
        .withId("EVENT-UUID-6")
        .withTitle("EVENT-TITLE-6")
        .withCreateDate(OffsetDateTime.parse("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(OffsetDateTime.parse("2016-12-02T09:00:00Z"))
        .withRecurrence(
            Recurrence.every(2, TimeUnit.WEEK)
                .on(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))
        .build();
    CalendarEvent event7 = CalendarEventMockBuilder.from(Period
        .between(OffsetDateTime.parse("2016-12-15T21:00:00Z"),
                 OffsetDateTime.parse("2016-12-15T21:15:00Z")))
        .plannedOn(calendar)
        .withId("EVENT-UUID-7")
        .withTitle("EVENT-TITLE-7")
        .withCreateDate(OffsetDateTime.parse("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(OffsetDateTime.parse("2016-12-02T09:00:00Z"))
        .withRecurrence(
            Recurrence.every(1, TimeUnit.MONTH)
                .on(DayOfWeekOccurrence.nth(3, DayOfWeek.FRIDAY)))
        .build();
    CalendarEvent event8 = CalendarEventMockBuilder.from(Period
        .between(OffsetDateTime.parse("2016-12-15T21:15:00Z"),
                 OffsetDateTime.parse("2016-12-15T21:30:00Z")))
        .plannedOn(calendar)
        .withId("EVENT-UUID-8")
        .withTitle("EVENT-TITLE-8")
        .withCreateDate(OffsetDateTime.parse("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(OffsetDateTime.parse("2016-12-02T09:00:00Z"))
        .withRecurrence(Recurrence.every(TimeUnit.MONTH))
        .build();
    CalendarEvent event9 = CalendarEventMockBuilder.from(Period
        .between(OffsetDateTime.parse("2016-12-15T21:45:00Z"),
                 OffsetDateTime.parse("2016-12-15T22:00:00Z")))
        .plannedOn(calendar)
        .withId("EVENT-UUID-9")
        .withTitle("EVENT-TITLE-9")
        .withCreateDate(OffsetDateTime.parse("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(OffsetDateTime.parse("2016-12-02T09:00:00Z"))
        .withRecurrence(Recurrence.every(TimeUnit.YEAR))
        .build();

    exportAndVerifyResult(
        asList(event1, event2, event3, event4, event5, event6, event7, event8, event9),
        "ical4j_export_several_with_recurrence.txt");
  }

  @Test
  public void severalOnAllDaysAndDailyRecurrence() throws ICalendarException {
    CalendarEvent event1 = CalendarEventMockBuilder.from(Period
        .between(LocalDate.parse("2016-12-15"),
                 LocalDate.parse("2016-12-15")))
        .plannedOn(calendar)
        .withId("EVENT-UUID-1")
        .withExternalId("EXT-EVENT-UUID-1")
        .withTitle("EVENT-TITLE-1")
        .withCreateDate(OffsetDateTime.parse("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(OffsetDateTime.parse("2016-12-02T09:00:00Z"))
        .withRecurrence(Recurrence.every(TimeUnit.DAY))
        .build();
    CalendarEvent event2 = CalendarEventMockBuilder.from(Period
        .between(LocalDate.parse("2016-12-15"),
                 LocalDate.parse("2016-12-15")))
        .plannedOn(calendar)
        .withId("EVENT-UUID-2")
        .withTitle("EVENT-TITLE-2")
        .withCreateDate(OffsetDateTime.parse("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(OffsetDateTime.parse("2016-12-02T09:00:00Z"))
        .withRecurrence(Recurrence.every(3, TimeUnit.DAY))
        .build();
    CalendarEvent event3 = CalendarEventMockBuilder.from(Period
        .between(LocalDate.parse("2016-12-15"),
                 LocalDate.parse("2016-12-15")))
        .plannedOn(calendar)
        .withId("EVENT-UUID-3")
        .withTitle("EVENT-TITLE-3")
        .withCreateDate(OffsetDateTime.parse("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(OffsetDateTime.parse("2016-12-02T09:00:00Z"))
        .withRecurrence(
            Recurrence.every(4, TimeUnit.DAY).upTo(LocalDate.parse("2016-12-31")))
        .build();
    CalendarEvent event4 = CalendarEventMockBuilder.from(Period
        .between(LocalDate.parse("2016-12-15"),
                 LocalDate.parse("2016-12-15")))
        .plannedOn(calendar)
        .withId("EVENT-UUID-4")
        .withTitle("EVENT-TITLE-4")
        .withCreateDate(OffsetDateTime.parse("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(OffsetDateTime.parse("2016-12-02T09:00:00Z"))
        .withRecurrence(
            Recurrence.every(2, TimeUnit.DAY).upTo(10))
        .build();
    CalendarEvent event5 = CalendarEventMockBuilder.from(Period
        .between(LocalDate.parse("2016-12-15"),
                 LocalDate.parse("2016-12-15")))
        .plannedOn(calendar)
        .withId("EVENT-UUID-5")
        .withTitle("EVENT-TITLE-5")
        .withCreateDate(OffsetDateTime.parse("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(OffsetDateTime.parse("2016-12-02T09:00:00Z"))
        .withRecurrence(
            Recurrence.every(TimeUnit.DAY)
                .excludeEventOccurrencesStartingAt(LocalDate.parse("2016-12-18"),
                    LocalDate.parse("2016-12-20"),
                    LocalDate.parse("2016-12-25")))
        .build();
    CalendarEvent event6 = CalendarEventMockBuilder.from(Period
        .between(LocalDate.parse("2016-12-15"),
                 LocalDate.parse("2016-12-15")))
        .plannedOn(calendar)
        .withId("EVENT-UUID-6")
        .withTitle("EVENT-TITLE-6")
        .withCreateDate(OffsetDateTime.parse("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(OffsetDateTime.parse("2016-12-02T09:00:00Z"))
        .withRecurrence(
            Recurrence.every(2, TimeUnit.WEEK)
                .on(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))
        .build();
    CalendarEvent event7 = CalendarEventMockBuilder.from(Period
        .between(LocalDate.parse("2016-12-15"),
                 LocalDate.parse("2016-12-15")))
        .plannedOn(calendar)
        .withId("EVENT-UUID-7")
        .withTitle("EVENT-TITLE-7")
        .withCreateDate(OffsetDateTime.parse("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(OffsetDateTime.parse("2016-12-02T09:00:00Z"))
        .withRecurrence(
            Recurrence.every(1, TimeUnit.MONTH)
                .on(DayOfWeekOccurrence.nth(3, DayOfWeek.FRIDAY)))
        .build();
    CalendarEvent event8 = CalendarEventMockBuilder.from(Period
        .between(LocalDate.parse("2016-12-15"),
                 LocalDate.parse("2016-12-15")))
        .plannedOn(calendar)
        .withId("EVENT-UUID-8")
        .withTitle("EVENT-TITLE-8")
        .withCreateDate(OffsetDateTime.parse("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(OffsetDateTime.parse("2016-12-02T09:00:00Z"))
        .withRecurrence(Recurrence.every(TimeUnit.MONTH))
        .build();
    CalendarEvent event9 = CalendarEventMockBuilder.from(Period
        .between(LocalDate.parse("2016-12-15"),
                 LocalDate.parse("2016-12-15")))
        .plannedOn(calendar)
        .withId("EVENT-UUID-9")
        .withTitle("EVENT-TITLE-9")
        .withCreateDate(OffsetDateTime.parse("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(OffsetDateTime.parse("2016-12-02T09:00:00Z"))
        .withRecurrence(Recurrence.every(TimeUnit.YEAR))
        .build();

    exportAndVerifyResult(
        asList(event1, event2, event3, event4, event5, event6, event7, event8, event9),
        "ical4j_export_several_with_recurrence_on_all_day.txt");
  }

  @Test
  public void simpleOneOfTwoDayDurationWithAttendees() throws ICalendarException {
    User user = mock(User.class);
    when(user.getId()).thenReturn("userId");
    when(user.getDisplayedName()).thenReturn("User Test");
    when(user.geteMail()).thenReturn("user.test@silverpeas.org");
    CalendarEvent event = CalendarEventMockBuilder.from(Period
        .between(LocalDate.parse("2016-12-14"), LocalDate.parse("2016-12-15")))
        .plannedOn(calendar)
        .withId("EVENT-UUID-ATTENDEES")
        .withTitle("EVENT-TITLE")
        .withCreator(creator)
        .withAttendee(user, mockedAttendee -> {
          when(mockedAttendee.getPresenceStatus()).thenReturn(OPTIONAL);
          when(mockedAttendee.getParticipationStatus()).thenReturn(ACCEPTED);
        })
        .withAttendee("external.1@silverpeas.org", mockedAttendee ->
            when(mockedAttendee.getPresenceStatus()).thenReturn(INFORMATIVE))
        .withAttendee("external.2@silverpeas.org", mockedAttendee ->
            when(mockedAttendee.getParticipationStatus()).thenReturn(DECLINED))
        .withCreateDate(OffsetDateTime.parse("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(OffsetDateTime.parse("2016-12-02T09:00:00Z"))
        .build();

    exportAndVerifyResult(singletonList(event),
        "ical4j_export_simple_two_days_duration_with_attendees.txt");
  }

  @Test
  public void oneWithRecurrenceAndWithAttendees() throws ICalendarException {
    User user = mock(User.class);
    when(user.getId()).thenReturn("userId");
    when(user.getDisplayedName()).thenReturn("User Test");
    when(user.geteMail()).thenReturn("user.test@silverpeas.org");
    CalendarEvent event = CalendarEventMockBuilder
        .from(Period.between(LocalDate.parse("2016-11-30"), LocalDate.parse("2016-11-30")))
        .plannedOn(calendar)
        .withId("EVENT-UUID-RECURRENCE-ATTENDEES")
        .withTitle("EVENT-TITLE")
        .withRecurrence(Recurrence.every(TimeUnit.DAY).upTo(10))
        .withCreator(creator)
        .withAttendee(user, mockedAttendee -> {
          when(mockedAttendee.getPresenceStatus()).thenReturn(OPTIONAL);
          when(mockedAttendee.getParticipationStatus()).thenReturn(ACCEPTED);
        })
        .withAttendee("external.1@silverpeas.org", mockedAttendee ->
            when(mockedAttendee.getPresenceStatus()).thenReturn(INFORMATIVE))
        .withAttendee("external.2@silverpeas.org", mockedAttendee ->
            when(mockedAttendee.getParticipationStatus()).thenReturn(DECLINED))
        .withCreateDate(OffsetDateTime.parse("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(OffsetDateTime.parse("2016-12-02T09:00:00Z"))
        .build();

    exportAndVerifyResult(singletonList(event),
        "ical4j_export_one_with_recurrence_and_with_attendees.txt");
  }

  @Test
  public void oneWithRecurrenceAndWithAttendeesWhichAnsweredOnDate() throws ICalendarException {
    User user = mock(User.class);
    when(user.getId()).thenReturn("userId");
    when(user.getDisplayedName()).thenReturn("User Test");
    when(user.geteMail()).thenReturn("user.test@silverpeas.org");
    CalendarEvent event = CalendarEventMockBuilder.from(Period
        .between(LocalDate.parse("2016-11-30"), LocalDate.parse("2016-11-30")))
        .plannedOn(calendar)
        .withId("EVENT-UUID-RECURRENCE-ATTENDEES-ON-DATE-ANSWER")
        .withTitle("EVENT-TITLE")
        .withRecurrence(Recurrence.every(TimeUnit.DAY).upTo(10))
        .withCreator(creator)
        .withAttendee(user, mockedAttendee -> {
          when(mockedAttendee.getPresenceStatus()).thenReturn(OPTIONAL);
          when(mockedAttendee.getParticipationStatus()).thenReturn(ACCEPTED);
          mockedAttendee.getParticipationOn().set(LocalDate.parse("2016-12-06"), DECLINED);
          mockedAttendee.getParticipationOn().set(LocalDate.parse("2016-12-09"), TENTATIVE);
        })
        .withAttendee("external.1@silverpeas.org", mockedAttendee -> {
          when(mockedAttendee.getPresenceStatus()).thenReturn(INFORMATIVE);
          mockedAttendee.getParticipationOn().set(LocalDate.parse("2016-12-06"), DECLINED);
        })
        .withAttendee("external.2@silverpeas.org", mockedAttendee -> {
          when(mockedAttendee.getParticipationStatus()).thenReturn(DECLINED);
          mockedAttendee.getParticipationOn().set(LocalDate.parse("2016-12-10"), ACCEPTED);
        })
        .withCreateDate(OffsetDateTime.parse("2016-12-01T14:30:00Z"))
        .withLastUpdateDate(OffsetDateTime.parse("2016-12-02T09:00:00Z"))
        .build();

    exportAndVerifyResult(singletonList(event),
        "ical4j_export_one_with_recurrence_and_with_attendees_which_answered_on_date.txt");
  }

  /**
   * Centralization of verification.<br/>
   * <p>
   * The mechanism is the following:<br/>
   * <p/>
   * the first parameter represent the list of calendar events to export and the second one is the
   * name of the file that contains the expected result.<br/>
   * Each lines starting with '#' character is ignored.<br/>
   * If the file content is equal to the result of export, the test is successfully verified.<br/>
   * If not, the different lines between the file content and the export result are logged to the
   * console.<br/>
   * Only event parts are verified from the contents.
   * </p>
   * @param fileNameOfExpectedResult the name of the file that contains the expected export result.
   */
  @SuppressWarnings({"unchecked", "Duplicates"})
  private void exportAndVerifyResult(List<CalendarEvent> calendarEvents,
      String fileNameOfExpectedResult) throws ICalendarException {
    try {

      ByteArrayOutputStream emptyExportResult = new ByteArrayOutputStream();
      ICalendarExport.from(calendar, Stream::<CalendarEvent>empty).to(() -> emptyExportResult);
      List<String> empty = IOUtils.readLines(new StringReader(emptyExportResult.toString()));
      empty.remove(empty.size() - 1);

      ByteArrayOutputStream exportResult = new ByteArrayOutputStream();
      ICalendarExport.from(calendar, calendarEvents::stream).to(() -> exportResult);

      StringReader current = new StringReader(exportResult.toString());
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
      currentContent =
          currentContent.replaceAll("DTSTAMP.+\n", "DTSTAMP:VALUE IS NOT VERIFIED BUT IS MANDATORY\n");

      assertThat(currentContent, is(expectedContent));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String getFileContent(String fileName) {
    try (InputStream fileStream = getClass().getResourceAsStream(fileName)) {
      return StringUtil.join(IOUtils.readLines(fileStream), '\n');
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  static {
    // This static block permits to ensure that the UNIT TEST is entirely executed into UTC
    // TimeZone.
    TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
  }
}