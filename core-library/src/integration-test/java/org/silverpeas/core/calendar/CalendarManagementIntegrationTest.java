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
import org.silverpeas.core.test.CalendarWarBuilder;
import org.silverpeas.core.test.rule.DbSetupRule.TableLine;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Yohann Chastagnier
 */
@RunWith(Arquillian.class)
public class CalendarManagementIntegrationTest extends BaseCalendarTest {

  @Deployment
  public static Archive<?> createTestArchive() {
    return CalendarWarBuilder.onWarForTestClass(CalendarManagementIntegrationTest.class)
        .addAsResource(BaseCalendarTest.TABLE_CREATION_SCRIPT.substring(1))
        .addAsResource(INITIALIZATION_SCRIPT.substring(1))
        .build();
  }

  @Before
  public void verifyInitialData() throws Exception {
    // JPA and Basic SQL query must show that it exists no data
    assertThat(getCalendarTableLines(), hasSize(3));
    assertThat(Calendar.getByComponentInstanceId(INSTANCE_ID), empty());
  }

  @Test
  public void getCalendarBuComponentInstanceIdShouldWorkAndCalendarsAreSorted() throws Exception {
    List<Calendar> calendars = Calendar.getByComponentInstanceId("instance_B");
    assertThat(calendars, hasSize(1));
    Calendar calendar = calendars.get(0);
    assertThat(calendar.getId(), is("ID_2"));
    assertThat(calendar.getComponentInstanceId(), is("instance_B"));
    assertThat(calendar.getTitle(), is("title 2"));
    assertThat(calendar.getCreatedBy(), is("0"));
    assertThat(calendar.getCreateDate().toInstant(), is(Instant.parse("2016-07-28T16:50:00Z")));
    assertThat(calendar.getLastUpdatedBy(), is("0"));
    assertThat(calendar.getLastUpdateDate().toInstant(), is(Instant.parse("2016-07-28T16:50:00Z")));
    assertThat(calendar.getVersion(), is(0L));

    calendars = Calendar.getByComponentInstanceId("instance_A");
    assertThat(calendars, hasSize(2));
    calendar = calendars.get(0);
    assertThat(calendar.getId(), is("ID_1"));
    assertThat(calendar.getComponentInstanceId(), is("instance_A"));
    assertThat(calendar.getTitle(), is("title 1"));
    assertThat(calendar.getCreatedBy(), is("0"));
    assertThat(calendar.getCreateDate().toInstant(), is(Instant.parse("2016-07-28T16:50:00Z")));
    assertThat(calendar.getLastUpdatedBy(), is("1"));
    assertThat(calendar.getLastUpdateDate().toInstant(), is(Instant.parse("2016-07-28T16:55:00Z")));
    assertThat(calendar.getVersion(), is(1L));
    calendar = calendars.get(1);
    assertThat(calendar.getId(), is("ID_3"));
    assertThat(calendar.getComponentInstanceId(), is("instance_A"));
    assertThat(calendar.getTitle(), is("title 3"));
    assertThat(calendar.getCreatedBy(), is("0"));
    assertThat(calendar.getCreateDate().toInstant(), is(Instant.parse("2016-07-28T16:50:00Z")));
    assertThat(calendar.getLastUpdatedBy(), is("0"));
    assertThat(calendar.getLastUpdateDate().toInstant(), is(Instant.parse("2016-07-28T16:50:00Z")));
    assertThat(calendar.getVersion(), is(0L));
  }

  @Test
  public void createCalendarIntoPersistenceShouldWork() throws Exception{
    final Date testStartingDate = new Date();

    Calendar newCalendar = new Calendar(INSTANCE_ID);
    newCalendar.setTitle("a title");

    assertThat(newCalendar.getId(), nullValue());
    assertThat(newCalendar.getComponentInstanceId(), is(INSTANCE_ID));
    assertThat(newCalendar.getTitle(), is("a title"));
    assertThat(newCalendar.getCreator(), nullValue());
    assertThat(newCalendar.getCreateDate(), nullValue());
    assertThat(newCalendar.getLastUpdateDate(), nullValue());
    assertThat(newCalendar.getLastUpdatedBy(), nullValue());
    assertThat(newCalendar.getVersion(), is(0L));

    newCalendar.save();
    assertThat(newCalendar.getId(), notNullValue());

    // Verifying the data
    List<TableLine> persistedCalendars = getCalendarTableLines();
    assertThat(persistedCalendars, hasSize(4));
    TableLine persistedCalendar = getCalendarTableLineById(newCalendar.getId());

    assertThat(persistedCalendar.get("instanceId"), is(INSTANCE_ID));
    assertThat(persistedCalendar.get("title"), is("a title"));
    Date createDate = persistedCalendar.get("createDate");
    String createdBy = persistedCalendar.get("createdBy");
    assertThat(createDate, greaterThanOrEqualTo(testStartingDate));
    assertThat(createdBy, is(getMockedUser().getId()));
    assertThat(persistedCalendar.get("lastUpdateDate"), is(createDate));
    assertThat(persistedCalendar.get("lastUpdatedBy"), is(createdBy));
    assertThat(persistedCalendar.get("version"), is(0L));
  }

  @Test
  public void modifyCalendarShouldWork() throws Exception {
    TableLine beforeModify = getCalendarTableLineById("ID_3");
    assertThat(beforeModify, notNullValue());
    assertThat(beforeModify.get("title"), is("title 3"));

    Calendar calendarToModify = Calendar.getById("ID_3");
    String modifiedTitle = "title 3 has been modified and saved into persistence";
    calendarToModify.setTitle(modifiedTitle);
    calendarToModify.save();

    TableLine afterModify = getCalendarTableLineById("ID_3");
    assertThat(afterModify.get("title"), is(modifiedTitle));
  }

  @Test
  public void deleteCalendarShouldWork() throws Exception {
    TableLine beforeModify = getCalendarTableLineById("ID_3");
    assertThat(beforeModify, notNullValue());

    Calendar calendarToModify = Calendar.getById("ID_3");
    calendarToModify.delete();

    TableLine afterModify = getCalendarTableLineById("ID_3");
    assertThat(afterModify, nullValue());
  }
}
