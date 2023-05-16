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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
import org.silverpeas.core.date.TemporalConverter;
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
import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
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
  private static final Instant UPDATE_DATETIME = Instant.parse("2017-06-11T13:51:54Z");

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
        .addCalendarSynchronizationFeatures()
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
    assertThat(toLocalDate(calendar.getLastSynchronizationDate().get()), is(LocalDate.now()));
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
    assertThat(toLocalDate(synchronizedCalendar.getLastSynchronizationDate().get()),
        is(LocalDate.now()));
    assertThat(synchronizedCalendar.isEmpty(), is(false));

    List<CalendarEvent> events = Calendar.getEvents()
        .filter(f -> f.onCalendar(synchronizedCalendar))
        .stream()
        .collect(Collectors.toList());
    assertThat(events.size(), is(2));
    events.forEach(e ->
        assertThat(toLocalDate(e.getLastSynchronizationDate()), is(LocalDate.now())));
  }

  @Test
  public void synchronizeASecondTimeFromANonEmptyCalendarShouldDoesNothing() throws Exception {
    Calendar calendar = prepareSynchronizedCalendar();
    Optional<Instant> maybeSyncDate = calendar.getLastSynchronizationDate();
    assertThat(maybeSyncDate.isPresent(), is(true));
    Instant lastSynchronizationDate = maybeSyncDate.get();

    ICalendarImportResult result = calendar.synchronize();
    assertThat(result.isEmpty(), is(true));

    Calendar synchronizedCalendar = Calendar.getById(CALENDAR_ID);
    maybeSyncDate = synchronizedCalendar.getLastSynchronizationDate();
    assertThat(maybeSyncDate.isPresent(), is(true));
    Instant actualLastSynchronizationDate = maybeSyncDate.get();
    assertThat(actualLastSynchronizationDate, greaterThan(lastSynchronizationDate));
    assertThat(synchronizedCalendar.isEmpty(), is(false));

    List<CalendarEvent> events = Calendar.getEvents()
        .filter(f -> f.onCalendar(synchronizedCalendar))
        .stream()
        .collect(Collectors.toList());
    assertThat(events.size(), is(2));
    events.forEach(e ->
        assertThat(toLocalDate(e.getLastSynchronizationDate()), is(LocalDate.now())));
  }

  @Test
  public void eventUpdateAfterSynchronization() throws Exception {
    final Calendar calendar = prepareSynchronizedCalendar();
    Instant lastSynchronizationDate = calendar.getLastSynchronizationDate().orElse(null);

    Optional<CalendarEvent> maybeEvent =
        Calendar.getEvents().filter(f -> f.onCalendar(calendar)).stream().findFirst();
    assertThat(maybeEvent.isPresent(), is(true));
    CalendarEvent event = maybeEvent.get();
    updateLastUpdateDate(event);

    ICalendarImportResult result = calendar.synchronize();
    assertThat(result.added(), is(0));
    assertThat(result.updated(), is(1));
    assertThat(result.deleted(), is(0));

    Calendar synchronizedCalendar = Calendar.getById(CALENDAR_ID);
    Optional<Instant> maybeSyncDate = synchronizedCalendar.getLastSynchronizationDate();
    assertThat(maybeSyncDate.isPresent(), is(true));
    Instant actualLastSynchronizationDate = maybeSyncDate.get();
    assertThat(actualLastSynchronizationDate, greaterThan(lastSynchronizationDate));
  }

  @Test
  public void eventDeletionAfterSynchronization() throws Exception {
    final Calendar calendar = prepareSynchronizedCalendar();
    Optional<Instant> maybeSyncDate = calendar.getLastSynchronizationDate();
    assertThat(maybeSyncDate.isPresent(), is(true));
    Instant lastSynchronizationDate = maybeSyncDate.get();

    CalendarEvent nextDeletedEvent = addExternalEventIn(calendar);

    ICalendarImportResult result = calendar.synchronize();
    assertThat(result.added(), is(0));
    assertThat(result.updated(), is(0));
    assertThat(result.deleted(), is(1));

    Calendar synchronizedCalendar = Calendar.getById(CALENDAR_ID);
    maybeSyncDate = synchronizedCalendar.getLastSynchronizationDate();
    assertThat(maybeSyncDate.isPresent(), is(true));
    Instant actualLastSynchronizationDate = maybeSyncDate.get();
    assertThat(actualLastSynchronizationDate, greaterThan(lastSynchronizationDate));
    assertThat(synchronizedCalendar.externalEvent(nextDeletedEvent.getExternalId()).isPresent(),
        is(false));
    assertThat(synchronizedCalendar.event(nextDeletedEvent.getId()).isPresent(), is(false));
  }

  @Test
  public void eventAddingUpdateAndDeletionAfterSynchronization() throws Exception {
    final Calendar calendar = prepareSynchronizedCalendar();
    Optional<Instant> maybeSyncDate = calendar.getLastSynchronizationDate();
    assertThat(maybeSyncDate.isPresent(), is(true));
    Instant lastSynchronizationDate = maybeSyncDate.get();

    List<CalendarEvent> events = Calendar.getEvents()
        .filter(f -> f.onCalendar(calendar))
        .stream()
        .collect(Collectors.toList());
    final String externalId = UUID.randomUUID().toString();
    updateLastUpdateDate(events.get(0));
    updateExternalId(events.get(1), externalId);

    ICalendarImportResult result = calendar.synchronize();
    assertThat(result.added(), is(1));
    assertThat(result.updated(), is(1));
    assertThat(result.deleted(), is(1));

    Calendar synchronizedCalendar = Calendar.getById(CALENDAR_ID);
    maybeSyncDate = synchronizedCalendar.getLastSynchronizationDate();
    assertThat(maybeSyncDate.isPresent(), is(true));
    Instant actualLastSynchronizationDate = maybeSyncDate.get();
    assertThat(actualLastSynchronizationDate, greaterThan(lastSynchronizationDate));
    assertThat(synchronizedCalendar.externalEvent(externalId).isPresent(), is(false));
    assertThat(synchronizedCalendar.event(externalId).isPresent(), is(false));

    events = Calendar.getEvents()
        .filter(f -> f.onCalendar(synchronizedCalendar))
        .stream()
        .collect(Collectors.toList());
    assertThat(events.size(), is(2));
    events.forEach(e ->
        assertThat(toLocalDate(e.getLastSynchronizationDate()), is(LocalDate.now())));
  }

  @Test
  public void synchronizeAllCalendars() throws Exception {
    Calendar calendar = Calendar.getById("ID_1");
    String externalCalendarUrl =
        getFilePath(PATTERN_EXTERNAL_URL, "ICAL-EXPORT-SP-GOO-2017-05-03_00.ics");
    calendar.setExternalCalendarUrl(new URL(externalCalendarUrl));
    calendar.save();

    assertThat(calendar.getLastSynchronizationDate().isPresent(), is(false));

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

  private Calendar prepareSynchronizedCalendar() throws MalformedURLException, ImportException  {
    Calendar calendar = prepareCalendarWithExternal(externalUrl);
    calendar.synchronize();
    return calendar;
  }

  private void updateLastUpdateDate(final CalendarEvent event) {
    final String componentId = event.asCalendarComponent().getId();
    Transaction.performInOne(() -> {
      JdbcSqlQuery.update("sb_cal_components")
          .withUpdateParam("lastUpdateDate", UPDATE_DATETIME)
          .where("id = ?", componentId)
          .execute();
      return null;
    });
  }

  private CalendarEvent addExternalEventIn(final Calendar calendar) {
    CalendarEvent event = CalendarEvent.on(LocalDate.now().minusMonths(2))
        .withExternalId(UUID.randomUUID().toString())
        .withTitle("next deleted event")
        .withDescription("This event will be deleted at the next synchronization");
    event.setLastSynchronizationDate(OffsetDateTime.now().minusDays(2).toInstant());
    return event.planOn(calendar);
  }

  private void updateExternalId(final CalendarEvent event, final String externalId) {
    Transaction.performInOne(() -> {
      JdbcSqlQuery.update("sb_cal_event")
          .withUpdateParam("externalId", externalId)
          .where("id = ?", event.getId())
          .execute();
      return null;
    });
  }

  private String getFilePath(String pattern) {
    return getFilePath(pattern, "");
  }

  private String getFilePath(String pattern, String filename) {
    return FilenameUtils.normalize(
        MessageFormat.format(pattern, mavenTargetDirectoryRule.getResourceTestDirFile().getPath(),
            filename), true);
  }

  private LocalDate toLocalDate(final Instant instant) {
    return TemporalConverter.asLocalDate(instant, ZoneId.systemDefault());
  }
}
  