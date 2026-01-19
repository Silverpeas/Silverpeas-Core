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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.persistence.datasource.OperationContext;
import org.silverpeas.core.security.token.TokenGeneratorProvider;
import org.silverpeas.core.security.token.persistent.repository.PersistentResourceTokenJPARepository;
import org.silverpeas.core.security.token.persistent.repository.PersistentResourceTokenRepository;
import org.silverpeas.core.security.token.persistent.service.DefaultTokenService;
import org.silverpeas.core.test.LibCoreWarBuilder;
import org.silverpeas.core.test.integration.SQLRequester.ResultLine;
import org.silverpeas.core.test.stub.StubbedUserProvider;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThrows;

/**
 * Integration tests on a {@link Calendar}.
 * @author Yohann Chastagnier
 */
@RunWith(Arquillian.class)
public class CalendarManagementIT extends BaseCalendarTest {

  @Deployment
  public static Archive<?> createTestArchive() {
    return LibCoreWarBuilder.onWarForTestClass(CalendarManagementIT.class)
        .addStubbedUserAPI()
        .addCalendarEngine()
        .addClasses(TokenGeneratorProvider.class, PersistentResourceTokenRepository.class,
            PersistentResourceTokenJPARepository.class, DefaultTokenService.class)
        .addAsResource(BaseCalendarTest.TABLE_CREATION_SCRIPT.substring(1))
        .addAsResource(INITIALIZATION_SCRIPT.substring(1))
        .build();
  }

  @Before
  public void setUpInitialData() throws Exception {
    // JPA and Basic SQL query must show that it exists no data
    assertThat(getCalendarTableLines(), hasSize(5));
    assertThat(Calendar.getByComponentInstanceId(INSTANCE_ID), empty());
    User user = StubbedUserProvider.addUser("26");
    OperationContext.fromUser(user);
    StubbedUserProvider.addUser("0");
    StubbedUserProvider.addUser("1");
    StubbedUserProvider.addUser("2");
  }

  @Test
  public void getCalendarByComponentInstanceIdShouldWorkAndCalendarsAreSorted() {
    List<Calendar> calendars = Calendar.getByComponentInstanceId("calendar2");
    assertThat(calendars, hasSize(1));
    Calendar calendar = calendars.get(0);
    assertThat(calendar.getId(), is("ID_2"));
    assertThat(calendar.getComponentInstanceId(), is("calendar2"));
    assertThat(calendar.getTitle(), is("title 2"));
    assertThat(calendar.getCreatorId(), is("0"));
    assertThat(calendar.getCreationDate().toInstant(), is(Instant.parse("2016-07-28T16:50:00Z")));
    assertThat(calendar.getLastUpdaterId(), is("0"));
    assertThat(calendar.getLastUpdateDate().toInstant(), is(Instant.parse("2016-07-28T16:50:00Z")));
    assertThat(calendar.getVersion(), is(0L));

    calendars = Calendar.getByComponentInstanceId("calendar1");
    assertThat(calendars, hasSize(2));
    calendar = calendars.get(0);
    assertThat(calendar.getId(), is("ID_1"));
    assertThat(calendar.getComponentInstanceId(), is("calendar1"));
    assertThat(calendar.getTitle(), is("title 1"));
    assertThat(calendar.getCreatorId(), is("0"));
    assertThat(calendar.getCreationDate().toInstant(), is(Instant.parse("2016-07-28T16:50:00Z")));
    assertThat(calendar.getLastUpdaterId(), is("1"));
    assertThat(calendar.getLastUpdateDate().toInstant(), is(Instant.parse("2016-07-28T16:55:00Z")));
    assertThat(calendar.getVersion(), is(1L));
    calendar = calendars.get(1);
    assertThat(calendar.getId(), is("ID_3"));
    assertThat(calendar.getComponentInstanceId(), is("calendar1"));
    assertThat(calendar.getTitle(), is("title 3"));
    assertThat(calendar.getCreatorId(), is("0"));
    assertThat(calendar.getCreationDate().toInstant(), is(Instant.parse("2016-07-28T16:50:00Z")));
    assertThat(calendar.getLastUpdaterId(), is("0"));
    assertThat(calendar.getLastUpdateDate().toInstant(), is(Instant.parse("2016-07-28T16:50:00Z")));
    assertThat(calendar.getVersion(), is(0L));
  }

  @Test
  public void createCalendarIntoPersistenceShouldWork() throws Exception{
    final Date testStartingDate = new Date();

    Calendar newCalendar = new Calendar(INSTANCE_ID, "a title");
    newCalendar.setZoneId(ZoneId.systemDefault());

    assertThat(newCalendar.getId(), nullValue());
    assertThat(newCalendar.getComponentInstanceId(), is(INSTANCE_ID));
    assertThat(newCalendar.getTitle(), is("a title"));
    assertThat(newCalendar.getCreator(), nullValue());
    assertThat(newCalendar.getCreationDate(), nullValue());
    assertThat(newCalendar.getLastUpdateDate(), nullValue());
    assertThat(newCalendar.getLastUpdaterId(), nullValue());
    assertThat(newCalendar.getVersion(), is(0L));

    newCalendar.save();
    assertThat(newCalendar.getId(), notNullValue());
    assertThat(newCalendar.isPersisted(), is(true));
    assertThat(newCalendar.isEmpty(), is(true));

    // Verifying the data
    List<ResultLine> persistedCalendars = getCalendarTableLines();
    assertThat(persistedCalendars, hasSize(6));
    ResultLine persistedCalendar = getCalendarTableLineById(newCalendar.getId());

    assertThat(persistedCalendar.get("instanceId"), is(INSTANCE_ID));
    assertThat(persistedCalendar.get("title"), is("a title"));
    assertThat(persistedCalendar.get("zoneId"), is(ZoneId.systemDefault().getId()));
    Date createDate = persistedCalendar.get("createDate");
    String createdBy = persistedCalendar.get("createdBy");
    assertThat(createDate, greaterThanOrEqualTo(testStartingDate));
    assertThat(createdBy, is(getUser().getId()));
    assertThat(persistedCalendar.get("lastUpdateDate"), is(createDate));
    assertThat(persistedCalendar.get("lastUpdatedBy"), is(createdBy));
    assertThat(persistedCalendar.get("version"), is(0L));
  }

  @Test
  public void modifyCalendarShouldWork() throws Exception {
    ResultLine beforeModify = getCalendarTableLineById("ID_3");
    assertThat(beforeModify, notNullValue());
    assertThat(beforeModify.get("title"), is("title 3"));

    Calendar calendarToModify = Calendar.getById("ID_3");
    String modifiedTitle = "title 3 has been modified and saved into persistence";
    calendarToModify.setTitle(modifiedTitle);
    calendarToModify.save();

    ResultLine afterModify = getCalendarTableLineById("ID_3");
    assertThat(afterModify.get("title"), is(modifiedTitle));
  }

  @Test
  public void deleteCalendarShouldWork() throws Exception {
    ResultLine beforeDeletion = getCalendarTableLineById("ID_3");
    List<ResultLine> eventsBeforeDeletion = getCalendarEventTableLines();
    assertThat(beforeDeletion, notNullValue());
    assertThat(eventsBeforeDeletion, hasSize(6));

    Calendar calendarToDelete = Calendar.getById("ID_3");
    calendarToDelete.delete();
    assertThat(calendarToDelete.isPersisted(), is(false));
    assertThat(getCalendarTableLines(), hasSize(4));


    ResultLine afterDeletion = getCalendarTableLineById("ID_3");
    List<ResultLine> eventsAfterDeletion = getCalendarEventTableLines();
    assertThat(afterDeletion, nullValue());
    assertThat(eventsAfterDeletion, hasSize(4));

    assertThrows(IllegalStateException.class, () -> calendarToDelete.event("ID_E_3"));
  }

  @Test
  public void addEventIntoCalendarShouldPersistIt() throws Exception {
    CalendarEvent event = CalendarEvent.on(LocalDate.now())
        .createdBy("1")
        .withTitle("a title")
        .withDescription("a description");
    assertThat(event.isPersisted(), is(false));
    Calendar calendar = Calendar.getById("ID_3");
    event.planOn(calendar);

    assertThat(event.isPersisted(), is(true));
    ResultLine afterAdd = getCalendarEventTableLineById(event.getId());
    assertThat(afterAdd, notNullValue());
    assertThat(afterAdd.get("title"), is("a title"));
    assertThat(afterAdd.get("description"), is("a description"));
    assertThat(afterAdd.get("inDays"), is(true));
  }
}
