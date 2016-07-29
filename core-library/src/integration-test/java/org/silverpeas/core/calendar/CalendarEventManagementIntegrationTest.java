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

import org.hamcrest.MatcherAssert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.calendar.repository.CalendarEventCriteria;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.test.rule.DbSetupRule.TableLine;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Yohann Chastagnier
 */
@RunWith(Arquillian.class)
public class CalendarEventManagementIntegrationTest extends BaseCalendarTest {

  @Deployment
  public static Archive<?> createTestArchive() {
    return CalendarWarBuilder.onWarForTestClass(CalendarEventManagementIntegrationTest.class)
        .addAsResource(INITIALIZATION_SCRIPT.substring(1)).build();
  }

  @Before
  public void verifyInitialData() throws Exception {
    // JPA and Basic SQL query must show that it exists no data
    assertThat(getCalendarEventTableLines(), hasSize(5));
  }

  @Test
  public void getCalendarEventById() throws Exception {
    CalendarEvent calendarEvent = CalendarEvent.getById("ID_E_3");
    assertThat(calendarEvent, notNullValue());
    assertThat(calendarEvent.getCalendar().getId(), is("ID_1"));
    assertThat(calendarEvent.getPeriod(), notNullValue());
    assertThat(calendarEvent.getPeriod().isInDays(), is(false));
    assertThat(calendarEvent.getPeriod().getStartDateTime().toInstant(),
        is(Instant.parse("2016-01-08T18:30:00Z")));
    assertThat(calendarEvent.getPeriod().getEndDateTime().toInstant(),
        is(Instant.parse("2016-01-22T13:38:22Z")));
    assertThat(calendarEvent.getTitle(), is("title C"));
    assertThat(calendarEvent.getDescription(), is("description C"));
    assertThat(calendarEvent.getLocation(), is("location C"));
    assertThat(calendarEvent.getVisibilityLevel(), is(VisibilityLevel.PUBLIC));
    assertThat(calendarEvent.getPriority(), is(1));
  }

  @Test
  public void getCalendarEventByMinimalCriteriaShouldWork() throws Exception {
    OffsetDateTime startDateTime =
        OffsetDateTime.of(LocalDateTime.of(2016, 1, 22, 0, 0), ZoneOffset.UTC);
    OffsetDateTime endDateTime =
        OffsetDateTime.of(LocalDateTime.of(2016, 1, 25, 0, 0), ZoneOffset.UTC);
    List<CalendarEvent> calendarEvents = CalendarEvent
        .findByCriteria(CalendarEventCriteria.from(Period.between(startDateTime, endDateTime)));
    assertThat(calendarEvents, hasSize(3));
  }
}
