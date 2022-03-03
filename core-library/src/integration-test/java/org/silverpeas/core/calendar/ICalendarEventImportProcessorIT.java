/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.persistence.datasource.OperationContext;
import org.silverpeas.core.test.CalendarWarBuilder;
import org.silverpeas.core.test.util.SQLRequester.ResultLine;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration test on the importation into Silverpeas of events encoded in the iCalendar format.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class ICalendarEventImportProcessorIT extends BaseCalendarTest {

  private static final String CALENDAR_ID = "ID_CAL_WITHOUT_EVENT";

  static {
    // This static block permits to ensure that the UNIT TEST is entirely executed into UTC
    // TimeZone.
    TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
  }

  @Inject
  private ICalendarEventImportProcessor importProcessor;
  private Calendar calendar;

  @Deployment
  public static Archive<?> createTestArchive() {
    return CalendarWarBuilder.onWarForTestClass(ICalendarEventImportProcessorIT.class)
        .addAsResource(BaseCalendarTest.TABLE_CREATION_SCRIPT.substring(1))
        .addAsResource(INITIALIZATION_SCRIPT.substring(1))
        .addAsResource("org/silverpeas/util/logging")
        .addAsResource("org/silverpeas/calendar/settings")
        .addAsResource("org/silverpeas/util/timezone.properties")
        .build();
  }

  @Before
  public void calendarShouldBeEmpty() throws Exception {
    List<ResultLine> events = getCalendarEventTableLinesByCalendarId(CALENDAR_ID);
    assertThat(events.isEmpty(), is(true));

    OperationContext.fromUser("0");
    calendar = Calendar.getById(CALENDAR_ID);
  }

  @Test
  public void importASimpleEvent() throws Exception {
    final String ics = "SIMPLE_EVENT.ics";
    final int addedEvents = 1;

    ICalendarImportResult result = importProcessor.importInto(calendar, iCalEventsFrom(ics));
    assertThat(result.added(), is(addedEvents));
    assertThat(result.updated(), is(0));

    List<ResultLine> events = getCalendarEventTableLinesByCalendarId(CALENDAR_ID);
    assertThat(events.size(), is(addedEvents));
    CalendarEvent event = calendar.externalEvent(events.get(0).get("externalId")).get();
    assertThat(event.getExternalId(), is("cc412802-843c-43bb-8249-7f626ba608cb"));
    assertThat(event.getTitle(), is("Déjeuner en famille"));
    assertThat(event.getLocation(), is("A la maison"));
    assertThat(event.getStartDate(), is(OffsetDateTime.parse("2017-05-09T10:00:00Z")));
    assertThat(event.getEndDate(), is(OffsetDateTime.parse("2017-05-09T12:00:00Z")));
    assertThat(event.isRecurrent(), is(true));
    assertThat(event.getRecurrence().getEndDate().isPresent(), is(true));
    assertThat(event.getRecurrence().getEndDate().get(),
        is(OffsetDateTime.parse("2017-06-30T10:00:00Z")));
    assertThat(event.getRecurrence().getFrequency().isWeekly(), is(true));
    assertThat(hasDayOfWeek(event.getRecurrence(), DayOfWeek.TUESDAY), is(true));
    assertThat(hasDayOfWeek(event.getRecurrence(), DayOfWeek.FRIDAY), is(true));
  }

  @Test
  public void importTwoTimesASimpleEvent() throws Exception {
    final String ics = "SIMPLE_EVENT.ics";
    final int addedEvents = 1;
    ICalendarImportResult result = importProcessor.importInto(calendar, iCalEventsFrom(ics));
    assertThat(result.added(), is(addedEvents));
    assertThat(result.updated(), is(0));

    List<ResultLine> events = getCalendarEventTableLinesByCalendarId(CALENDAR_ID);
    CalendarEvent addedEvent = calendar.externalEvent(events.get(0).get("externalId")).get();

    result = importProcessor.importInto(calendar, iCalEventsFrom(ics));
    assertThat(result.added(), is(0));
    assertThat(result.updated(), is(0));

    events = getCalendarEventTableLinesByCalendarId(CALENDAR_ID);
    assertThat(events.size(), is(addedEvents));
    CalendarEvent event = calendar.externalEvent(events.get(0).get("externalId")).get();
    assertThat(event, is(addedEvent));
  }

  @Test
  public void reimportWithUpdatesASimpleEvent() throws Exception {
    String ics = "SIMPLE_EVENT.ics";
    final int addedEvents = 1;
    ICalendarImportResult result = importProcessor.importInto(calendar, iCalEventsFrom(ics));
    assertThat(result.added(), is(addedEvents));
    assertThat(result.updated(), is(0));

    ics = "MODIFIED_SIMPLE_EVENT.ics";
    final int updatedEvents = 1;
    result = importProcessor.importInto(calendar, touch(ics));
    assertThat(result.added(), is(0));
    assertThat(result.updated(), is(updatedEvents));

    List<ResultLine> events = getCalendarEventTableLinesByCalendarId(CALENDAR_ID);
    assertThat(events.size(), is(addedEvents));
    CalendarEvent event = calendar.externalEvent(events.get(0).get("externalId")).get();
    assertThat(event.getTitle(), is("Déjeuner avec Fanny"));
  }

  @Test
  public void importTwoEvents() throws Exception {
    final String ics = "TWO_EVENTS.ics";
    final int addedEvents = 2;

    ICalendarImportResult result = importProcessor.importInto(calendar, iCalEventsFrom(ics));
    assertThat(result.added(), is(addedEvents));
    assertThat(result.updated(), is(0));

    List<ResultLine> events = getCalendarEventTableLinesByCalendarId(CALENDAR_ID);
    assertThat(events.size(), is(addedEvents));
  }

  @Test
  public void importEventWithModifiedOccurrences() throws Exception {
    final String ics = "EVENTS_WITH_MODIFIED_OCCURRENCE.ics";
    final int addedEvents = 2;

    ICalendarImportResult result = importProcessor.importInto(calendar, iCalEventsFrom(ics));
    assertThat(result.added(), is(addedEvents));
    assertThat(result.updated(), is(0));

    List<ResultLine> events = getCalendarEventTableLinesByCalendarId(CALENDAR_ID);
    assertThat(events.size(), is(addedEvents));

    CalendarEvent event = calendar.externalEvent("9588c7b0-62af-45bd-a8c8-c8e8eab27c52")
        .orElseThrow(AssertionError::new);
    List<CalendarEventOccurrence> occurrences = event.getPersistedOccurrences();
    assertThat(occurrences.size(), is(1));
    assertThat(occurrences.get(0).getStartDate(), is(OffsetDateTime.parse("2017-06-13T12:00:00Z")));
    assertThat(occurrences.get(0).getEndDate(), is(OffsetDateTime.parse("2017-06-13T13:00:00Z")));
  }

  @Test
  public void reimportWithUpdatesEventWithModifiedOccurrences() throws Exception {
    final String ics = "EVENTS_WITH_MODIFIED_OCCURRENCE.ics";
    final int updatedEvents = 2;
    importEventWithModifiedOccurrences();

    Instant now = Instant.now();

    ICalendarImportResult result = importProcessor.importInto(calendar, touch(ics));
    assertThat(result.added(), is(0));
    assertThat(result.updated(), is(updatedEvents));
    CalendarEvent event = calendar.externalEvent("9588c7b0-62af-45bd-a8c8-c8e8eab27c52")
        .orElseThrow(AssertionError::new);
    List<CalendarEventOccurrence> occurrences = event.getPersistedOccurrences();
    assertThat(occurrences.size(), is(1));
    assertThat(occurrences.get(0).getLastUpdateDate().toInstant().isAfter(now), is(true));
  }

  @Test
  public void importManyEventsWithSomeHavingModifiedOccurrences()
      throws Exception {
    final String ics = "ICAL-EXPORT-SP-GOO-2017-05-03_00.ics";
    final int addedEvents = 415;

    ICalendarImportResult result = importProcessor.importInto(calendar, iCalEventsFrom(ics));
    assertThat(result.added(), is(addedEvents));
    assertThat(result.updated(), is(0));

    List<ResultLine> events = getCalendarEventTableLinesByCalendarId(CALENDAR_ID);
    assertThat(events.size(), is(addedEvents));
  }

  @Test
  public void reimportManyEventsWithSomeHavingModifiedOccurrences()
      throws Exception {
    final String ics = "ICAL-EXPORT-SP-GOO-2017-05-03_00.ics";
    final int updatedEvents = 415;
    importManyEventsWithSomeHavingModifiedOccurrences();

    ICalendarImportResult result = importProcessor.importInto(calendar, touch(ics));
    assertThat(result.added(), is(0));
    assertThat(result.updated(), is(updatedEvents));
  }

  @Test
  public void importAnotherManyEventsWithSomeHavingModifiedOccurrences()
      throws Exception {
    final String ics = "ICAL-EXPORT-YCH-2017-05-02_00.ics";
    final int addedEvents = 415;

    ICalendarImportResult result = importProcessor.importInto(calendar, iCalEventsFrom(ics));
    assertThat(result.added(), is(addedEvents));
    assertThat(result.updated(), is(0));

    List<ResultLine> events = getCalendarEventTableLinesByCalendarId(CALENDAR_ID);
    assertThat(events.size(), is(addedEvents));
  }

  @Test
  public void importManyEventsWithSomeHavingModifiedOccurrencesAndExceptions()
      throws Exception {
    final String ics = "ICAL-EXPORT-YCH-2017-05-02_01_EXCEP.ics";
    final int addedEvents = 415;

    ICalendarImportResult result = importProcessor.importInto(calendar, iCalEventsFrom(ics));
    assertThat(result.added(), is(addedEvents));
    assertThat(result.updated(), is(0));

    List<ResultLine> events = getCalendarEventTableLinesByCalendarId(CALENDAR_ID);
    assertThat(events.size(), is(addedEvents));
  }

  @Test
  public void importManyEventsWithSomeHavingModifiedOccurrencesOrExceptionsOrAttendees()
      throws Exception {
    final String ics = "ICAL-EXPORT-YCH-2017-05-02_02_EXC_ATTENDEE.ics";
    final int addedEvents = 415;

    ICalendarImportResult result = importProcessor.importInto(calendar, iCalEventsFrom(ics));
    assertThat(result.added(), is(addedEvents));
    assertThat(result.updated(), is(0));

    List<ResultLine> events = getCalendarEventTableLinesByCalendarId(CALENDAR_ID);
    assertThat(events.size(), is(addedEvents));
  }

  private InputStream iCalEventsFrom(final String ics) {
    return getClass().getResourceAsStream(ics);
  }

  private boolean hasDayOfWeek(final Recurrence recurrence, final DayOfWeek dayOfWeek) {
    return recurrence.getDaysOfWeek().stream().anyMatch(dw -> dw.dayOfWeek() == dayOfWeek);
  }

  private Reader touch(final String ics) throws IOException {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
    String currentDateTime = OffsetDateTime.now(ZoneId.of("UTC")).plusSeconds(1).format(formatter);
    try (BufferedReader buffer = new BufferedReader(new InputStreamReader(iCalEventsFrom(ics)))) {
      return new StringReader(buffer.lines().map(l -> {
        if (l.startsWith("LAST-MODIFIED")) {
          return "LAST-MODIFIED:" + currentDateTime;
        } else if (l.startsWith("DTSTAMP")) {
          return "DTSTAMP:" + currentDateTime;
        } else {
          return l;
        }
      }).collect(Collectors.joining("\n")));
    }
  }
}
