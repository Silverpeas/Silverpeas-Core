/*
 * Copyright (C) 2000 - 2018 Silverpeas
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

import org.apache.commons.io.FilenameUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.importexport.ImportException;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.OperationContext;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.test.CalendarWarBuilder;
import org.silverpeas.core.test.rule.MavenTargetDirectoryRule;
import org.silverpeas.core.test.util.SQLRequester;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

/**
 * Integration test on the synchronization of a synchronized calendar.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class CalendarSynchronizationIT extends BaseCalendarTest {

  /**
   * Empty calendar for the tests below.
   */
  private static final String CALENDAR_ID = "ID_CAL_WITHOUT_EVENT";
  /**
   * URL of an external calendar without any events.
   */
  private static final String EMPTY_EXTERNAL_URL =
      "file://{0}/org/silverpeas/core/calendar/ICAL_EMPTY_TEST_SYNCHRO.ics";
  /**
   * URL of an external calendar with at least two events.
   */
  private static final String EXTERNAL_URL =
      "file://{0}/org/silverpeas/core/calendar/ICAL_TEST_SYNCHRO.ics";
  /**
   * URL pattern of an external calendar in the tests below.
   */
  private static final String PATTERN_EXTERNAL_URL = "file://{0}/org/silverpeas/core/calendar/{1}";
  /**
   * Date time to set the last update date of an already imported event in order to simulate the
   * event was updated in the external calendar for some of the synchronization tests below. This
   * date time takes into account the actual datetime of the event in the external calendar (ics).
   */
  private static final OffsetDateTime UPDATE_DATETIME = OffsetDateTime.parse("2017-06-11T13:51:54Z");

  static {
    // This static block permits to ensure that the UNIT TEST is entirely executed into UTC
    // TimeZone.
    TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
  }

  @Rule
  public MavenTargetDirectoryRule mavenTargetDirectoryRule =
      new MavenTargetDirectoryRule(CalendarSynchronizationIT.class);

  @Inject
  private ICalendarEventSynchronization synchronization;

  private String emptyExternalUrl;
  private String externalUrl;

  @Deployment
  public static Archive<?> createTestArchive() {
    return CalendarWarBuilder.onWarForTestClass(CalendarSynchronizationIT.class)
        .addAsResource(BaseCalendarTest.TABLE_CREATION_SCRIPT.substring(1))
        .addAsResource(INITIALIZATION_SCRIPT.substring(1))
        .addAsResource("org/silverpeas/util/logging")
        .addAsResource("org/silverpeas/calendar/settings")
        .addAsResource("org/silverpeas/util/timezone.properties")
        .build();
  }

  @Before
  public void calendarShouldBeEmpty() throws Exception {
    List<SQLRequester.ResultLine> events = getCalendarEventTableLinesByCalendarId(CALENDAR_ID);
    assertThat(events.isEmpty(), is(true));

    emptyExternalUrl = getFilePath(EMPTY_EXTERNAL_URL);
    externalUrl = getFilePath(EXTERNAL_URL);

    OperationContext.fromUser("0");
  }

  @Test
  public void synchronizeFromAnEmptyCalendarShouldDoesNothing() throws Exception {
    Calendar calendar = prepareCalendarWithExternal(emptyExternalUrl);

    ICalendarImportResult result = calendar.synchronize();
    assertThat(result.isEmpty(), is(true));

    calendar = Calendar.getById(calendar.getId());
    assertThat(calendar.getLastSynchronizationDate().isPresent(), is(true));
    assertThat(calendar.getLastSynchronizationDate().get().toLocalDate(), is(LocalDate.now()));
    assertThat(calendar.isEmpty(), is(true));
  }

  @Test
  public void synchronizeFromANonEmptyCalendarShouldAddAllEvents() throws Exception {
    Calendar calendar = prepareCalendarWithExternal(externalUrl);

    ICalendarImportResult result = calendar.synchronize();
    assertThat(result.added(), is(2));
    assertThat(result.updated(), is(0));
    assertThat(result.deleted(), is(0));

    final Calendar synchronizedCalendar = Calendar.getById(CALENDAR_ID);
    assertThat(synchronizedCalendar.getLastSynchronizationDate().isPresent(), is(true));
    assertThat(synchronizedCalendar.getLastSynchronizationDate().get().toLocalDate(),
        is(LocalDate.now()));
    assertThat(synchronizedCalendar.isEmpty(), is(false));

    List<CalendarEvent> events = Calendar.getEvents()
        .filter(f -> f.onCalendar(synchronizedCalendar))
        .stream()
        .collect(Collectors.toList());
    assertThat(events.size(), is(2));
    events.forEach(e -> {
      assertThat(e.getLastSynchronizationDate().toLocalDate(), is(LocalDate.now()));
    });
  }

  @Test
  public void synchronizeASecondTimeFromANonEmptyCalendarShouldDoesNothing() throws Exception {
    Calendar calendar = prepareSynchronizedCalendar();
    OffsetDateTime lastSynchronizationDate = calendar.getLastSynchronizationDate().get();

    ICalendarImportResult result = calendar.synchronize();
    assertThat(result.isEmpty(), is(true));

    Calendar synchronizedCalendar = Calendar.getById(CALENDAR_ID);
    assertThat(synchronizedCalendar.getLastSynchronizationDate().get(),
        greaterThan(lastSynchronizationDate));
    assertThat(synchronizedCalendar.isEmpty(), is(false));

    List<CalendarEvent> events = Calendar.getEvents()
        .filter(f -> f.onCalendar(synchronizedCalendar))
        .stream()
        .collect(Collectors.toList());
    assertThat(events.size(), is(2));
    events.forEach(e -> {
      assertThat(e.getLastSynchronizationDate().toLocalDate(), is(LocalDate.now()));
    });
  }

  @Test
  public void testEventUpdateAfterSynchronization() throws Exception {
    final Calendar calendar = prepareSynchronizedCalendar();
    OffsetDateTime lastSynchronizationDate = calendar.getLastSynchronizationDate().get();

    CalendarEvent event =
        Calendar.getEvents().filter(f -> f.onCalendar(calendar)).stream().findFirst().get();
    updateLastUpdateDate(event, UPDATE_DATETIME);

    ICalendarImportResult result = calendar.synchronize();
    assertThat(result.added(), is(0));
    assertThat(result.updated(), is(1));
    assertThat(result.deleted(), is(0));

    Calendar synchronizedCalendar = Calendar.getById(CALENDAR_ID);
    assertThat(synchronizedCalendar.getLastSynchronizationDate().get(),
        greaterThan(lastSynchronizationDate));
  }

  @Test
  public void testEventDeletionAfterSynchronization() throws Exception {
    final Calendar calendar = prepareSynchronizedCalendar();
    OffsetDateTime lastSynchronizationDate = calendar.getLastSynchronizationDate().get();

    CalendarEvent nextDeletedEvent = addExternalEventIn(calendar);

    ICalendarImportResult result = calendar.synchronize();
    assertThat(result.added(), is(0));
    assertThat(result.updated(), is(0));
    assertThat(result.deleted(), is(1));

    Calendar synchronizedCalendar = Calendar.getById(CALENDAR_ID);
    assertThat(synchronizedCalendar.getLastSynchronizationDate().get(),
        greaterThan(lastSynchronizationDate));
    assertThat(synchronizedCalendar.externalEvent(nextDeletedEvent.getExternalId()).isPresent(),
        is(false));
    assertThat(synchronizedCalendar.event(nextDeletedEvent.getId()).isPresent(), is(false));
  }

  @Test
  public void testEventAddingUpdateAndDeletionAfterSynchronization() throws Exception {
    final Calendar calendar = prepareSynchronizedCalendar();
    OffsetDateTime lastSynchronizationDate = calendar.getLastSynchronizationDate().get();

    List<CalendarEvent> events = Calendar.getEvents()
        .filter(f -> f.onCalendar(calendar))
        .stream()
        .collect(Collectors.toList());
    final String externalId = UUID.randomUUID().toString();
    updateLastUpdateDate(events.get(0), UPDATE_DATETIME);
    updateExternalId(events.get(1), externalId);

    ICalendarImportResult result = calendar.synchronize();
    assertThat(result.added(), is(1));
    assertThat(result.updated(), is(1));
    assertThat(result.deleted(), is(1));

    Calendar synchronizedCalendar = Calendar.getById(CALENDAR_ID);
    assertThat(synchronizedCalendar.getLastSynchronizationDate().get(),
        greaterThan(lastSynchronizationDate));
    assertThat(synchronizedCalendar.externalEvent(externalId).isPresent(), is(false));
    assertThat(synchronizedCalendar.event(externalId).isPresent(), is(false));

    events = Calendar.getEvents()
        .filter(f -> f.onCalendar(synchronizedCalendar))
        .stream()
        .collect(Collectors.toList());
    assertThat(events.size(), is(2));
    events.forEach(e -> {
      assertThat(e.getLastSynchronizationDate().toLocalDate(), is(LocalDate.now()));
    });
  }

  @Test
  public void synchronizeAllCalendars() throws Exception {
    Calendar calendar = Calendar.getById("ID_1");
    String externalCalendarUrl =
        getFilePath(PATTERN_EXTERNAL_URL, "ICAL-EXPORT-SP-GOO-2017-05-03_00.ics");
    calendar.setExternalCalendarUrl(new URL(externalCalendarUrl));
    calendar.save();

    assertThat(calendar.getLastSynchronizationDate().isPresent(), is(false));

    /*calendar = Calendar.getById("ID_2");
    externalCalendarUrl = getFilePath(PATTERN_EXTERNAL_URL, "ICAL-EXPORT-YCH-2017-05-02_00.ics");
    calendar.setExternalCalendarUrl(new URL(externalCalendarUrl));
    calendar.save();

    calendar = Calendar.getById("ID_3");
    externalCalendarUrl =
        getFilePath(PATTERN_EXTERNAL_URL, "ICAL-EXPORT-YCH-2017-05-02_01_EXCEP.ics");
    calendar.setExternalCalendarUrl(new URL(externalCalendarUrl));
    calendar.save();

    calendar = Calendar.getById("ID_4");
    externalCalendarUrl =
        getFilePath(PATTERN_EXTERNAL_URL, "ICAL-EXPORT-YCH-2017-05-02_02_EXC_ATTENDEE.ics");
    calendar.setExternalCalendarUrl(new URL(externalCalendarUrl));
    calendar.save();*/

    synchronization.synchronizeAll();

    final List<Calendar> synchronizedCalendars = Calendar.getSynchronizedCalendars();
    assertThat(synchronizedCalendars, hasSize(1));
    synchronizedCalendars.forEach(c -> {
      assertThat(c.getLastSynchronizationDate().isPresent(), is(true));
      assertThat(c.isEmpty(), is(false));
    });
  }

  private Calendar prepareCalendarWithExternal(final String externalUrl)
      throws MalformedURLException {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    calendar.setExternalCalendarUrl(new URL(externalUrl));
    calendar.save();
    assertThat(calendar.getLastSynchronizationDate().isPresent(), is(false));
    return calendar;
  }

  private Calendar prepareSynchronizedCalendar()
      throws MalformedURLException, ImportException, InterruptedException {
    Calendar calendar = prepareCalendarWithExternal(externalUrl);
    calendar.synchronize();
    return calendar;
  }

  private void updateLastUpdateDate(final CalendarEvent event, final OffsetDateTime dateTime)
      throws SQLException {
    final String componentId = event.asCalendarComponent().getId();
    Transaction.performInOne(() -> {
      JdbcSqlQuery.createUpdateFor("sb_cal_components")
          .addUpdateParam("lastUpdateDate", dateTime)
          .where("id = ?", componentId)
          .execute();
      return null;
    });
  }

  private CalendarEvent addExternalEventIn(final Calendar calendar) throws SQLException {
    CalendarEvent event = CalendarEvent.on(LocalDate.now().minusMonths(2))
        .withExternalId(UUID.randomUUID().toString())
        .withTitle("next deleted event")
        .withDescription("This event will be deleted at the next synchronization");
    event.setLastSynchronizationDate(OffsetDateTime.now().minusDays(2));
    return event.planOn(calendar);
  }

  private void updateExternalId(final CalendarEvent event, final String externalId)
      throws SQLException {
    Transaction.performInOne(() -> {
      JdbcSqlQuery.createUpdateFor("sb_cal_event")
          .addUpdateParam("externalId", externalId)
          .where("id = ?", event.getId())
          .execute();
      return null;
    });
  }

  private String getFilePath(String pattern) {
    return getFilePath(pattern, "");
  }

  private String getFilePath(String pattern, String filename) {
    return FilenameUtils.normalize(MessageFormat
            .format(pattern, mavenTargetDirectoryRule.getResourceTestDirFile().getPath(), filename),
        true);
  }
}
  