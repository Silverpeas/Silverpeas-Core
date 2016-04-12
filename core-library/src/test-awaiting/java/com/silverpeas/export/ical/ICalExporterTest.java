/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.export.ical;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.silverpeas.calendar.CalendarEvent;
import com.silverpeas.calendar.CalendarEventRecurrence;
import org.silverpeas.core.date.Date;
import org.silverpeas.core.date.DateTime;
import com.silverpeas.export.ExportDescriptor;
import com.silverpeas.export.ExportException;
import com.silverpeas.export.Exporter;
import com.silverpeas.export.ExporterProvider;
import com.silverpeas.export.NoDataToExportException;

import static com.silverpeas.calendar.CalendarEvent.anEventAt;
import static com.silverpeas.calendar.CalendarEventRecurrence.every;
import static com.silverpeas.calendar.DayOfWeek.*;
import static com.silverpeas.calendar.DayOfWeekOccurrence.nthOccurrence;
import static com.silverpeas.calendar.TimeUnit.MONTH;
import static com.silverpeas.calendar.TimeUnit.WEEK;
import static com.silverpeas.export.ical.CalendarEventMatcher.describes;
import static org.junit.Assert.*;

/**
 * Unit tests on the export of calendar events in iCal format.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/spring-export.xml")
public class ICalExporterTest {

  private static final FileObject root = FileUtil.createMemoryFileSystem().getRoot();
  private FileObject icsFile;
  private Exporter<ExportableCalendar> exporter;
  private ExportDescriptor descriptor;

  public ICalExporterTest() {
  }

  @Before
  public void setUp() throws IOException {
    icsFile = root.createData("myexport-" + UUID.randomUUID().toString() + ".ics");
    ExporterProvider factory = ExporterProvider.getFactory();
    exporter = factory.getICalExporter();
    assertNotNull(exporter);
    descriptor = ExportDescriptor.withWriter(new OutputStreamWriter(icsFile.getOutputStream(),
        Charsets.UTF_8));
  }

  @After
  public void clean() throws IOException {
    IOUtils.closeQuietly(descriptor.getOutputStream());
    icsFile.delete();
  }

  @Test
  public void emptyTest() {
    assertTrue(true);
  }

  /**
   * When no events have to be exported, then an exception is thrown by the impossibility to export
   * a calendar without any events in the iCal format.
   *
   * @throws Exception if the test fails.
   */
  @Test(expected = NoDataToExportException.class)
  public void exportNoEventsThrowsExportException() throws Exception {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>();
    exporter.export(descriptor, ExportableCalendar.with(events));
  }

  /**
   * Export one event in iCal format into a specified file.
   *
   * @throws Exception if the export fails.
   */
  @Test
  public void exportOneEventGenerateAnICalFileWithInfoOnThatEvent() throws Exception {
    CalendarEvent event = generateEventWithTitle("toto", onDay(false));
    exporter.export(descriptor, ExportableCalendar.with(event));
    String content = icsFile.asText(CharEncoding.UTF_8);
    assertThat(content, describes(event));
  }

  /**
   * Export one event in iCal format into a specified file.
   *
   * @throws Exception if the export fails.
   */
  @Test
  public void exportOneDayEventGenerateAnICalFileWithInfoOnThatEvent() throws Exception {
    CalendarEvent event = generateEventWithTitle("toto", onDay(true));
    exporter.export(descriptor, ExportableCalendar.with(event));
    String content = icsFile.asText(CharEncoding.UTF_8);
    assertThat(content, describes(event));
  }

  /**
   * Export several events in iCal format into a specified file.
   *
   * @throws Exception if the export fails.
   */
  @Test
  public void exportSeveralEventsGenerateAnICalFileWithInfoOnThatEvents() throws Exception {
    CalendarEvent event1 = generateEventWithTitle("toto1", onDay(false));
    CalendarEvent event2 = generateEventWithTitle("toto2", onDay(false));
    CalendarEvent event3 = generateEventWithTitle("toto3", onDay(false));
    exporter.export(descriptor, ExportableCalendar.with(event1, event2, event3));
    String content = icsFile.asText(CharEncoding.UTF_8);
    for (CalendarEvent event : Arrays.asList(event1, event2, event3)) {
      assertThat(content, describes(event));
    }
  }

  /**
   * Export one recurring event in iCal format into a specified file. The recurring information of
   * the event should be indicated in the iCal file.
   *
   * @throws Exception if the export fails.
   */
  @Test
  public void exportOneRecurringEventGenerateAnICalFileWithInfoOnThatEventAndItsRecurrence() throws
      Exception {
    CalendarEvent event = generateRecurringEventWithTitle("recurring", onDay(false));
    exporter.export(descriptor, ExportableCalendar.with(event));
    String content = icsFile.asText(CharEncoding.UTF_8);
    assertThat(content, describes(event));
  }

  /**
   * Export one event recurring on some days of week in iCal format into a specified file. The
   * recurring information of the event should be indicated in the iCal file.
   *
   * @throws Exception if the export fails.
   */
  @Test
  public void exportOneRecurringEventOnSomeDaysOfWeekGenerateAnICalFileWithInfoOnThatEventAndItsRecurrence()
      throws
      Exception {
    CalendarEventRecurrence recurrence = every(MONTH).
        on(nthOccurrence(2, MONDAY), nthOccurrence(1, THURSDAY));
    CalendarEvent event = generateEventWithTitle("recurring", true).recur(recurrence);
    exporter.export(descriptor, ExportableCalendar.with(event));
    String content = icsFile.asText(CharEncoding.UTF_8);
    assertThat(content, describes(event));
  }

  /**
   * Export one recurring event withWriter a recurring end date in iCal format into a specified
   * file. The recurring information of the event should be indicated in the iCal file.
   *
   * @throws Exception if the export fails.
   */
  @Test
  public void exportOneEventWithARecurringEndDateGenerateAnICalFileWithInfoOnThatEventAndItsRecurrence()
      throws
      Exception {
    CalendarEvent event = generateRecurringEventWithTitle("recurring", onDay(false));
    Calendar endDate = Calendar.getInstance();
    endDate.add(Calendar.YEAR, 1);
    event.getRecurrence().upTo(new Date(endDate.getTime()));
    exporter.export(descriptor, ExportableCalendar.with(event));
    String content = icsFile.asText(CharEncoding.UTF_8);
    assertThat(content, describes(event));
  }

  /**
   * Export one recurring event withWriter a recurring end date in iCal format into a specified
   * file. The recurring information of the event should be indicated in the iCal file.
   *
   * @throws Exception if the export fails.
   */
  @Test
  public void exportOneEventWithARecurringEndDateTimeGenerateAnICalFileWithInfoOnThatEventAndItsRecurrence()
      throws Exception {
    CalendarEvent event = generateRecurringEventWithTitle("recurring", onDay(false));
    Calendar endDate = Calendar.getInstance();
    endDate.add(Calendar.YEAR, 1);
    event.getRecurrence().upTo(new DateTime(endDate.getTime()));
    exporter.export(descriptor, ExportableCalendar.with(event));
    String content = icsFile.asText(CharEncoding.UTF_8);
    assertThat(content, describes(event));
  }

  @Test
  public void exportOneEventOnDayStartingAtAGivenTime() throws Exception {
    CalendarEvent event = anEventAt(DateTime.now()).withTitle("toto").withPriority(10);
    exporter.export(descriptor, ExportableCalendar.with(event));
    String content = icsFile.asText(CharEncoding.UTF_8);
    assertThat(content, describes(event));
  }

  /**
   * An event defined on a day cannot have an end date in iCal.
   *
   * @throws Exception if the ical export of the event failed.
   */
  @Test(expected = ExportException.class)
  public void exportOneEventOnDayEndingAtAGivenTime() throws Exception {
    CalendarEvent event = anEventAt(Date.today()).endingAt(DateTime.now()).withTitle("toto").
        withPriority(10);
    exporter.export(descriptor, ExportableCalendar.with(event));
  }

  private CalendarEvent generateEventWithTitle(String title, boolean onDay) {
    CalendarEvent event;
    Calendar startingDate = Calendar.getInstance();
    if (onDay) {
      event = anEventAt(new Date(startingDate.getTime()));
    } else {
      Calendar endingDate = Calendar.getInstance();
      endingDate.add(Calendar.HOUR_OF_DAY, 2);
      event = anEventAt(new DateTime((startingDate.getTime()))).endingAt(new DateTime(
          endingDate.getTime()));
    }
    event.withTitle(title).withPriority(10);
    event.getAttendees().add("emmanuel.hugonnet@silverpeas.com");
    event.getAttendees().add("miguel.moquillon@silverpeas.com");
    event.getCategories().add("code retreat");
    event.getCategories().add("agilité");
    event.getCategories().add("Alpes JUG");
    return event;
  }

  private CalendarEvent generateRecurringEventWithTitle(String title, boolean onDay) {
    CalendarEvent event = generateEventWithTitle(title, onDay);
    event.recur(every(2, WEEK).on(WEDNESDAY).upTo(10));
    return event;
  }

  private boolean onDay(boolean isOnDay) {
    return isOnDay;
  }
}