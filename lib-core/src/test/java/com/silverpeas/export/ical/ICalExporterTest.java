/*
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.export.ical;

import com.silverpeas.calendar.CalendarEvent;
import com.silverpeas.calendar.CalendarEventRecurrence;
import com.silverpeas.calendar.DateTime;
import com.silverpeas.calendar.Date;
import com.silverpeas.export.ExportDescriptor;
import com.silverpeas.export.ExporterFactory;
import java.util.Arrays;
import com.silverpeas.export.Exporter;
import com.silverpeas.export.NoDataToExportException;
import com.silverpeas.util.PathTestUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.silverpeas.export.ical.CalendarEventMatcher.*;
import static com.silverpeas.calendar.CalendarEvent.*;
import static com.silverpeas.calendar.CalendarEventRecurrence.*;
import static com.silverpeas.calendar.DayOfWeek.*;
import static com.silverpeas.calendar.DayOfWeekOccurrence.*;
import static com.silverpeas.calendar.TimeUnit.*;

/**
 * Unit tests on the export of calendar events in iCal format.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/spring-export.xml")
public class ICalExporterTest {

  private static final String ICS_PATH = PathTestUtil.TARGET_DIR
      + "test-classes" + File.separatorChar + "myexport.ics";
  private Exporter<CalendarEvent> exporter;
  private ExportDescriptor descriptor = new ExportDescriptor(ICS_PATH);

  public ICalExporterTest() {
  }

  @Before
  public void setUp() {
    ExporterFactory factory = ExporterFactory.getFactory();
    exporter = factory.getICalExporter();
    assertNotNull(exporter);

    assertThat(new File(ICS_PATH).exists(), is(false));
  }

  @After
  public void tearDown() {
    FileUtils.deleteQuietly(new File(ICS_PATH));
  }

  @Test
  public void emptyTest() {
    assertTrue(true);
  }

  /**
   * When no events have to be exported, then an exception is thrown by the impossibility to export
   * a calendar without any events in the iCal format.
   * @throws Exception if the test fails.
   */
  @Test(expected = NoDataToExportException.class)
  public void exportNoEventsThrowsExportException() throws Exception {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>();
    exporter.export(descriptor, events);
  }

  /**
   * Export one event in iCal format into a specified file.
   * @throws Exception if the export fails.
   */
  @Test
  public void exportOneEventGenerateAnICalFileWithInfoOnThatEvent() throws Exception {
    CalendarEvent event = generateEventWithTitle("toto", onDay(false));
    exporter.export(descriptor, event);

    File icsFile = new File(ICS_PATH);
    assertThat(icsFile.exists(), is(true));
    String content = FileUtils.readFileToString(icsFile);
    assertThat(content, describes(event));
  }

  /**
   * Export one event in iCal format into a specified file.
   * @throws Exception if the export fails.
   */
  @Test
  public void exportOneDayEventGenerateAnICalFileWithInfoOnThatEvent() throws Exception {
    CalendarEvent event = generateEventWithTitle("toto", onDay(true));
    exporter.export(descriptor, event);

    File icsFile = new File(ICS_PATH);
    assertThat(icsFile.exists(), is(true));
    String content = FileUtils.readFileToString(icsFile);
    assertThat(content, describes(event));
  }

  /**
   * Export several events in iCal format into a specified file.
   * @throws Exception if the export fails.
   */
  @Test
  public void exportSeveralEventsGenerateAnICalFileWithInfoOnThatEvents() throws Exception {
    CalendarEvent event1 = generateEventWithTitle("toto1", onDay(false));
    CalendarEvent event2 = generateEventWithTitle("toto2", onDay(false));
    CalendarEvent event3 = generateEventWithTitle("toto3", onDay(false));
    exporter.export(descriptor, event1, event2, event3);

    File icsFile = new File(ICS_PATH);
    assertThat(icsFile.exists(), is(true));
    String content = FileUtils.readFileToString(icsFile);
    for (CalendarEvent event : Arrays.asList(event1, event2, event3)) {
      assertThat(content, describes(event));
    }
  }

  /**
   * Export one recurring event in iCal format into a specified file. The recurring information of
   * the event should be indicated in the iCal file.
   * @throws Exception if the export fails.
   */
  @Test
  public void exportOneRecurringEventGenerateAnICalFileWithInfoOnThatEventAndItsRecurrence() throws
      Exception {
    CalendarEvent event = generateRecurringEventWithTitle("recurring", onDay(false));
    exporter.export(descriptor, event);
    File icsFile = new File(ICS_PATH);
    assertThat(icsFile.exists(), is(true));
    String content = FileUtils.readFileToString(icsFile);
    assertThat(content, describes(event));
  }

  /**
   * Export one event recurring on some days of week in iCal format into a specified file.
   * The recurring information of the event should be indicated in the iCal file.
   * @throws Exception if the export fails.
   */
  @Test
  public void exportOneRecurringEventOnSomeDaysOfWeekGenerateAnICalFileWithInfoOnThatEventAndItsRecurrence() throws
      Exception {
    CalendarEventRecurrence recurrence = every(MONTH).
        on(nthOccurrence(2, MONDAY), nthOccurrence(1, THURSDAY));
    CalendarEvent event = generateEventWithTitle("recurring", true).recur(recurrence);
    exporter.export(descriptor, event);
    File icsFile = new File(ICS_PATH);
    assertThat(icsFile.exists(), is(true));
    String content = FileUtils.readFileToString(icsFile);
    assertThat(content, describes(event));
  }

  /**
   * Export one recurring event with a recurring end date in iCal format into a specified file.
   * The recurring information of the event should be indicated in the iCal file.
   * @throws Exception if the export fails.
   */
  @Test
  public void exportOneEventWithARecurringEndDateGenerateAnICalFileWithInfoOnThatEventAndItsRecurrence() throws
      Exception {
    CalendarEvent event = generateRecurringEventWithTitle("recurring", onDay(false));
    Calendar endDate = Calendar.getInstance();
    endDate.add(Calendar.YEAR, 1);
    event.getRecurrence().upTo(new Date(endDate.getTime()));
    exporter.export(descriptor, event);

    File icsFile = new File(ICS_PATH);
    assertThat(icsFile.exists(), is(true));
    String content = FileUtils.readFileToString(icsFile);
    assertThat(content, describes(event));
  }

  /**
   * Export one recurring event with a recurring end date in iCal format into a specified file.
   * The recurring information of the event should be indicated in the iCal file.
   * @throws Exception if the export fails.
   */
  @Test
  public void exportOneEventWithARecurringEndDateTimeGenerateAnICalFileWithInfoOnThatEventAndItsRecurrence() throws
      Exception {
    CalendarEvent event = generateRecurringEventWithTitle("recurring", onDay(false));
    Calendar endDate = Calendar.getInstance();
    endDate.add(Calendar.YEAR, 1);
    event.getRecurrence().upTo(new DateTime(endDate.getTime()));
    exporter.export(descriptor, event);

    File icsFile = new File(ICS_PATH);
    assertThat(icsFile.exists(), is(true));
    String content = FileUtils.readFileToString(icsFile);
    assertThat(content, describes(event));
  }

  private CalendarEvent generateEventWithTitle(String title, boolean onDay) {
    CalendarEvent event;
    Calendar startingDate = Calendar.getInstance();
    if (onDay) {
      event = anEventAt(new Date(startingDate.getTime()));
    } else {
      Calendar endingDate = Calendar.getInstance();
      endingDate.add(Calendar.HOUR_OF_DAY, 2);
      event = anEventAt(new DateTime((startingDate.getTime())));
      event.endingAt(new DateTime(endingDate.getTime()));
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