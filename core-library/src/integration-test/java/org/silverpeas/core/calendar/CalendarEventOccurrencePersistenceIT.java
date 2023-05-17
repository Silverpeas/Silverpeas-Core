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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.calendar.repository.CalendarEventOccurrenceRepository;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.OperationContext;
import org.silverpeas.core.test.CalendarWarBuilder;
import org.silverpeas.core.test.integration.SQLRequester;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.silverpeas.core.date.TemporalConverter.asOffsetDateTime;

/**
 * Integration test on the persistence of the occurrences of calendar events.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class CalendarEventOccurrencePersistenceIT extends BaseCalendarTest {

  private static final String CALENDAR_ID = "ID_1";
  private CalendarEvent event;
  private final List<CalendarEventOccurrence> expectedOccurrences = new ArrayList<>();

  @Deployment
  public static Archive<?> createTestArchive() {
    return CalendarWarBuilder.onWarForTestClass(
        CalendarEventOccurrencePersistenceIT.class)
        .addAsResource(BaseCalendarTest.TABLE_CREATION_SCRIPT.substring(1))
        .addAsResource(INITIALIZATION_SCRIPT.substring(1))
        .build();
  }

  @Before
  public void persistSomeCalendarEventOccurrences() {
    OperationContext.fromUser("0");

    Calendar calendar = Calendar.getById(CALENDAR_ID);
    List<CalendarEventOccurrence> occurrences =
        calendar.between(LocalDate.of(2016, 1, 23), LocalDate.of(2016, 2, 28))
            .getEventOccurrences();
    assertThat(occurrences.size(), is(5));
    this.event = Transaction.performInOne(() -> {
      CalendarEventOccurrenceRepository repository = CalendarEventOccurrenceRepository.get();
      expectedOccurrences.add(repository.save(occurrences.get(0)));
      expectedOccurrences.add(repository.save(occurrences.get(2)));
      expectedOccurrences.add(repository.save(occurrences.get(4)));
      expectedOccurrences.sort(
          Comparator.comparing(o -> asOffsetDateTime(o.getStartDate())));
      return occurrences.get(0).getCalendarEvent();
    });
  }

  @Test
  public void getAllPersistedOccurrencesOfAGivenEvent() {
    List<CalendarEventOccurrence> occurrences = Transaction.performInOne(() -> {
      CalendarEventOccurrenceRepository repository = CalendarEventOccurrenceRepository.get();
      return repository.getAll(Collections.singletonList(event),
          Period.between(LocalDate.of(2016, 1, 23), LocalDate.of(2016, 2, 28)));
    });

    assertThat(occurrences, hasSize(3));
    assertThat(occurrences.containsAll(expectedOccurrences), is(true));

    expectedOccurrences.forEach(o -> {
      try {
        SQLRequester.ResultLine component =
            getCalendarComponentTableLineById(o.asCalendarComponent().getId());
        assertThat(component, notNullValue());
      } catch (Exception e) {
        fail(e.getMessage());
      }
    });
  }

  @Test
  public void deleteAllPersistedOccurrencesOfAGivenEvent() throws SQLException {
    long count = Transaction.performInOne(() -> {
      CalendarEventOccurrenceRepository repository = CalendarEventOccurrenceRepository.get();
      List<CalendarEventOccurrence> occurrences = repository.getAllByEvent(event);
      repository.delete(occurrences);
      return occurrences.size();
    });

    assertThat(count, is((long) expectedOccurrences.size()));

    List<SQLRequester.ResultLine> occurrences =
        getCalendarOccurrencesTableLineByEventId(event.getId());
    assertThat(occurrences.isEmpty(), is(true));

    expectedOccurrences.forEach(o -> {
      try {
        SQLRequester.ResultLine component =
            getCalendarComponentTableLineById(o.asCalendarComponent().getId());
        assertThat(component, nullValue());
      } catch (Exception e) {
        fail(e.getMessage());
      }
    });
  }

  @Test
  public void deleteAllPersistedOccurrencesSinceAGivenEventOccurrence() throws SQLException {
    long count = Transaction.performInOne(() -> {
      CalendarEventOccurrenceRepository repository = CalendarEventOccurrenceRepository.get();
      List<CalendarEventOccurrence> occurrences = repository.getAllSince(expectedOccurrences.get(1));
      repository.delete(occurrences);
      return occurrences.size();
    });

    assertThat(count, is((long) 2));

    List<SQLRequester.ResultLine> occurrences =
        getCalendarOccurrencesTableLineByEventId(event.getId());
    assertThat(occurrences.size(), is(1));
    assertThat(occurrences.get(0).get("id"), is(expectedOccurrences.get(0).getId()));

    Arrays.asList(expectedOccurrences.get(1), expectedOccurrences.get(2)).forEach(o -> {
      try {
        SQLRequester.ResultLine component =
            getCalendarComponentTableLineById(o.asCalendarComponent().getId());
        assertThat(component, nullValue());
      } catch (Exception e) {
        fail(e.getMessage());
      }
    });
  }
}
  