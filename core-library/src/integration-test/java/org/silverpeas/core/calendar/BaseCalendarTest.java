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

import org.junit.After;
import org.junit.Before;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.cache.service.SessionCacheAccessor;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.test.integration.DataSetTest;
import org.silverpeas.core.test.integration.SQLRequester.ResultLine;

import java.sql.SQLException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Base class for tests in the calendar engine.
 * It prepares the database to use in tests.
 */
public abstract class BaseCalendarTest extends DataSetTest {

  static final String TABLE_CREATION_SCRIPT =
      "/org/silverpeas/core/calendar/create_table_calendar.sql";

  static final String INITIALIZATION_SCRIPT =
      "/org/silverpeas/core/calendar/initialize_common_data_calendar.sql";

  protected static final String INSTANCE_ID = "anInstanceId";

  @Override
  protected String getDbSetupTableCreationSqlScript() {
    return TABLE_CREATION_SCRIPT;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected String getDbSetupInitializations() {
    return INITIALIZATION_SCRIPT;
  }

  private final UserDetail user = new UserDetail();

  @Before
  public void setUp() throws Exception {
    user.setId("26");
    CacheAccessorProvider.getThreadCacheAccessor().getCache().clear();
    ((SessionCacheAccessor) CacheAccessorProvider.getSessionCacheAccessor())
        .newSessionCache(user);
  }

  @After
  public void tearDown() throws Exception {
    CacheAccessorProvider.getThreadCacheAccessor().getCache().clear();
  }

  protected User getUser() {
    return user;
  }

  /**
   * Returns the list of calendar lines persisted into sb_cal_calendar table.
   * @return List of lines represented by a map between column name and value.
   */
  protected List<ResultLine> getCalendarTableLines() throws Exception {
    return getDbSetupRule().mapJdbcSqlQueryResultAsListOfMappedValues(
        JdbcSqlQuery.select("* from sb_cal_calendar")
            .addSqlPart("order by instanceid, title, id"));
  }

  /**
   * Returns the calendar persisted into sb_cal_calendar table corresponding to the given identifier.
   * @param id the id searched for.
   * @return List of lines represented by a map between column name and value.
   */
  protected ResultLine getCalendarTableLineById(String id) throws Exception {
    return JdbcSqlQuery.unique(getDbSetupRule().mapJdbcSqlQueryResultAsListOfMappedValues(
        JdbcSqlQuery.select("* from sb_cal_calendar").where("id = ?", id)));
  }

  /**
   * Returns the list of calendar event lines persisted into sb_cal_event table.
   * @return List of lines represented by a map between column name and value.
   */
  protected List<ResultLine> getCalendarEventTableLines() throws Exception {
    return getDbSetupRule().mapJdbcSqlQueryResultAsListOfMappedValues(
        JdbcSqlQuery.select("ce.*, co.* from sb_cal_event ce")
            .addSqlPart("join sb_cal_components co on co.id = ce.componentId")
            .addSqlPart("join sb_cal_calendar c on c.id = co.calendarId")
            .addSqlPart("order by c.instanceId, co.startDate, co.endDate, co.title, ce.id"));
  }

  /**
   * Returns the calendar event persisted into sb_cal_event table corresponding to the given
   * identifier of a calendar.
   * @param id the id of a calendar.
   * @return List of lines represented by a map between column name and value.
   */
  protected List<ResultLine> getCalendarEventTableLinesByCalendarId(String id) throws Exception {
    return getDbSetupRule().mapJdbcSqlQueryResultAsListOfMappedValues(
        JdbcSqlQuery.select("ce.*, co.* from sb_cal_event ce")
            .addSqlPart("join sb_cal_components co on co.id = ce.componentId")
            .addSqlPart("join sb_cal_calendar c on c.id = co.calendarId")
            .where("co.calendarId = ?", id));
  }

  /**
   * Returns the calendar event persisted into sb_cal_event table corresponding to the given
   * identifier.
   * @param id the id searched for.
   * @return List of lines represented by a map between column name and value.
   */
  protected ResultLine getCalendarEventTableLineById(String id) throws Exception {
    return JdbcSqlQuery.unique(getDbSetupRule().mapJdbcSqlQueryResultAsListOfMappedValues(
        JdbcSqlQuery.select("ce.*, co.* from sb_cal_event ce")
            .addSqlPart("join sb_cal_components co on co.id = ce.componentId")
            .addSqlPart("join sb_cal_calendar c on c.id = co.calendarId")
            .where("ce.id = ?", id)));
  }

  protected List<ResultLine> getAttributesTableLinesByEventId(String id) throws Exception {
    return getDbSetupRule().mapJdbcSqlQueryResultAsListOfMappedValues(
        JdbcSqlQuery.select("* from SB_Cal_Attributes").where("id = ?", id));
  }

  protected List<ResultLine> getAttendeesTableLines() throws Exception {
    return getDbSetupRule().mapJdbcSqlQueryResultAsListOfMappedValues(
        JdbcSqlQuery.select("* from SB_Cal_Attendees"));
  }

  protected ResultLine getCalendarComponentTableLineById(String id) throws Exception {
    return JdbcSqlQuery.unique(getDbSetupRule().mapJdbcSqlQueryResultAsListOfMappedValues(
        JdbcSqlQuery.select("* from sb_cal_components")
        .where("id = ?", id)));
  }

  protected ResultLine getCalendarOccurrenceTableLineById(String id) throws SQLException {
    return JdbcSqlQuery.unique(getDbSetupRule().mapJdbcSqlQueryResultAsListOfMappedValues(
        JdbcSqlQuery.select("* from sb_cal_occurrences").where("id = ?", id)));
  }

  protected List<ResultLine> getCalendarOccurrencesTableLineByEventId(String eventId)
      throws SQLException {
    return getDbSetupRule().mapJdbcSqlQueryResultAsListOfMappedValues(
        JdbcSqlQuery.select("* from sb_cal_occurrences").where("eventId = ?", eventId));
  }

  protected void assertEventProperties(final CalendarEvent actual, final CalendarEvent expected) {
    assertThat(actual.getStartDate(), is(expected.getStartDate()));
    assertThat(actual.getEndDate(), is(expected.getEndDate()));
    assertThat(actual.isOnAllDay(), is(expected.isOnAllDay()));
    assertThat(actual.getTitle(), is(expected.getTitle()));
    assertThat(actual.getDescription(), is(expected.getDescription()));
    assertThat(actual.getLocation(), is(expected.getLocation()));
    assertThat(actual.getAttributes().isEmpty(), is(false));
    assertThat(actual.getAttributes(), is(expected.getAttributes()));
    assertThat(actual.getVisibilityLevel(), is(expected.getVisibilityLevel()));
    assertThat(actual.getAttendees(), is(expected.getAttendees()));
    assertThat(actual.getCategories(), is(expected.getCategories()));
    assertThat(actual.isRecurrent(), is(false));
  }

  protected void assertEventIsOnlyUpdated(OperationResult<?, ?> result) {
    assertThat(result.isEmpty(), is(false));
    assertThat(result.instance().isPresent(), is(false));
    assertThat(result.created().isPresent(), is(false));
    assertThat(result.updated().isPresent(), is(true));
  }

  protected void assertAnEventIsOnlyCreated(OperationResult<?, ?> result) {
    assertThat(result.isEmpty(), is(false));
    assertThat(result.instance().isPresent(), is(false));
    assertThat(result.updated().isPresent(), is(false));
    assertThat(result.created().isPresent(), is(true));
  }

  protected void assertOccurrenceIsUpdated(OperationResult<?, ?> result) {
    assertThat(result.isEmpty(), is(false));
    assertThat(result.created().isPresent(), is(false));
    assertThat(result.updated().isPresent(), is(false));
    assertThat(result.instance().isPresent(), is(true));
  }

  protected void assertEventIsUpdated(OperationResult<?, ?> result) {
    assertThat(result.isEmpty(), is(false));
    assertThat(result.updated().isPresent(), is(true));
  }

  protected void assertAnEventIsCreated(OperationResult<?, ?> result) {
    assertThat(result.isEmpty(), is(false));
    assertThat(result.created().isPresent(), is(true));
  }

  protected void assertEventIsDeleted(final OperationResult<?, ?> result) {
    assertThat(result.created().isPresent(), is(false));
    assertThat(result.updated().isPresent(), is(false));
    assertThat(result.instance().isPresent(), is(false));
    assertThat(result.isEmpty(), is(true));
  }
}
