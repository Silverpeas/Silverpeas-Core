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
import org.silverpeas.core.calendar.CalendarEvent.EventOperationResult;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.core.persistence.datasource.OperationContext;
import org.silverpeas.core.test.CalendarWarBuilder;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.silverpeas.core.calendar.Attendee.ParticipationStatus.ACCEPTED;
import static org.silverpeas.core.calendar.Attendee.ParticipationStatus.AWAITING;

/**
 * Integration tests on the occurrences of a calendar event.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class CalendarEventOccurrenceIT extends BaseCalendarTest {

  private static final String CALENDAR_ID = "ID_1";
  private static final String CALENDAR_EVENT_ID = "ID_E_5";
  private static final LocalDate TODAY = LocalDate.now();
  private static final int OCCURRENCE_COUNT = 16;
  private static final String INITIAL_ATTENDEE = "renard@dans-le-poulailler@fr";
  private static final String NEW_ATTENDEE = "tintin@au-tibet.net";

  private CalendarEvent recurrentEvent;
  private CalendarEvent event;

  static {
    // This static block permits to ensure that the UNIT TEST is entirely executed into UTC
    // TimeZone.
    TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return CalendarWarBuilder.onWarForTestClass(
        CalendarEventOccurrenceIT.class)
        .addAsResource(BaseCalendarTest.TABLE_CREATION_SCRIPT.substring(1))
        .addAsResource(INITIALIZATION_SCRIPT.substring(1))
        .build();
  }

  @Before
  public void prepareARecurrentEventForTests() {
    OperationContext.fromUser("0");

    Calendar calendar = Calendar.getById(CALENDAR_ID);
    recurrentEvent = CalendarEvent.on(TODAY)
        .createdBy("2")
        .withTitle("Stand-up Meeting")
        .withDescription("Review on the current sprint")
        .inLocation("Room Chartreuse")
        .recur(Recurrence.every(1, TimeUnit.WEEK).until(OCCURRENCE_COUNT));
    recurrentEvent.getAttendees().add(INITIAL_ATTENDEE).accept();
    recurrentEvent.planOn(calendar);

    event = CalendarEvent.on(TODAY)
        .createdBy("2")
        .withTitle("Stand-up Meeting")
        .withDescription("Review on the current sprint")
        .inLocation("Room Chartreuse");
    event.getAttendees().add(INITIAL_ATTENDEE).accept();
    event.planOn(Calendar.getById(CALENDAR_ID));
  }

  @Test
  public void getOccurrenceByIdShouldWork() {
    // First date
    Optional<CalendarEventOccurrence> optionalOccurrence =
        CalendarEventOccurrence.getById(CALENDAR_EVENT_ID + "@2016-01-09");
    assertThat(optionalOccurrence.isPresent(), is(true));
    // Not the right date type
    optionalOccurrence =
        CalendarEventOccurrence.getById(CALENDAR_EVENT_ID + "@2016-01-09T00:00:00Z");
    assertThat(optionalOccurrence.isPresent(), is(false));
    // Not Exists
    optionalOccurrence =
        CalendarEventOccurrence.getById(CALENDAR_EVENT_ID + "@2016-01-10");
    assertThat(optionalOccurrence.isPresent(), is(false));
    // Exception
    optionalOccurrence =
        CalendarEventOccurrence.getById(CALENDAR_EVENT_ID + "@2016-01-16");
    assertThat(optionalOccurrence.isPresent(), is(false));
    // No exception
    optionalOccurrence =
        CalendarEventOccurrence.getById(CALENDAR_EVENT_ID + "@2016-01-23");
    assertThat(optionalOccurrence.isPresent(), is(true));
  }

  @Test
  public void fromARecurrentEventWeShouldGetAllItsOccurrencesInAGivenPeriod() {
    List<CalendarEventOccurrence> occurrences = allOccurrencesOf(recurrentEvent);

    assertThat(occurrences.size(), is(OCCURRENCE_COUNT));
    for (int i = 0; i < OCCURRENCE_COUNT; i++) {
      assertThat(occurrences.get(i).getStartDate(), is(TODAY.plusWeeks(i)));
      assertThat(occurrences.get(i).getEndDate(), is(TODAY.plusWeeks(i).plusDays(1)));
      assertThat(occurrences.get(i).getAttendees().get(INITIAL_ATTENDEE).isPresent(), is(true));
      assertThat(
          occurrences.get(i).getAttendees().get(INITIAL_ATTENDEE).get().getParticipationStatus(),
          is(ACCEPTED));
    }
  }

  @Test
  public void changeNonTemporalPropertiesOfTheSingleOccurrenceShouldUpdateTheEventItself() {
    final String newDescription = "Review the bugs dashboard";

    List<CalendarEventOccurrence> occurrences = allOccurrencesOf(event);
    assertThat(occurrences.size(), is(1));

    CalendarEventOccurrence anOccurrence = occurrences.get(0);
    anOccurrence.setDescription(newDescription);
    anOccurrence.getAttendees().add(NEW_ATTENDEE);

    EventOperationResult result = anOccurrence.update();
    // it is the event that is updated and not the occurrence in itself.
    assertThat(result.instance().isPresent(), is(false));
    assertThat(result.updated().isPresent(), is(true));

    CalendarEvent anEvent = Calendar.getById(CALENDAR_ID).event(event.getId()).get();
    assertThat(anEvent.getDescription(), is(newDescription));
    assertThat(anEvent.getAttendees().size(), is(2));
    assertThat(anEvent.getAttendees().get(INITIAL_ATTENDEE).isPresent(), is(true));
    assertThat(anEvent.getAttendees().get(INITIAL_ATTENDEE).get().getParticipationStatus(),
        is(ACCEPTED));
    assertThat(anEvent.getAttendees().get(NEW_ATTENDEE).isPresent(), is(true));
    assertThat(anEvent.getAttendees().get(NEW_ATTENDEE).get().getParticipationStatus(),
        is(AWAITING));
  }

  @Test
  public void changeDateOfTheSingleOccurrenceShouldUpdateTheEventItself() {
    List<CalendarEventOccurrence> occurrences = allOccurrencesOf(event);
    assertThat(occurrences.size(), is(1));

    CalendarEventOccurrence anOccurrence = occurrences.get(0);
    assertThat(anOccurrence.getAttendees().get(INITIAL_ATTENDEE).isPresent(), is(true));
    assertThat(anOccurrence.getAttendees().get(INITIAL_ATTENDEE).get().getParticipationStatus(),
        is(ACCEPTED));

    Period newPeriod = Period.between(LocalDate.from(anOccurrence.getStartDate()).plusDays(1),
        LocalDate.from(anOccurrence.getEndDate()).plusDays(1));
    anOccurrence.setPeriod(newPeriod);

    EventOperationResult result = anOccurrence.update();
    // it is the event that is updated and not the occurrence in itself.
    assertThat(result.instance().isPresent(), is(false));
    assertThat(result.updated().isPresent(), is(true));

    CalendarEvent anEvent = Calendar.getById(CALENDAR_ID).event(event.getId()).get();
    assertThat(anEvent.getStartDate(), is(newPeriod.getStartDate()));
    assertThat(anEvent.getEndDate(), is(newPeriod.getEndDate()));
    assertThat(anEvent.getAttendees().size(), is(1));
    assertThat(anEvent.getAttendees().get(INITIAL_ATTENDEE).get().getParticipationStatus(),
        is(AWAITING));
  }

  @Test
  public void changeNonTemporalPropertiesSinceTheSingleOccurrenceShouldUpdateTheEventItself() {
    final String newDescription = "Review the bugs dashboard";

    List<CalendarEventOccurrence> occurrences = allOccurrencesOf(event);
    assertThat(occurrences.size(), is(1));

    CalendarEventOccurrence anOccurrence = occurrences.get(0);
    anOccurrence.setDescription(newDescription);
    anOccurrence.getAttendees().add(NEW_ATTENDEE);

    EventOperationResult result = event.updateSince(anOccurrence);
    // it is the event that is updated and not the occurrence in itself.
    assertThat(result.instance().isPresent(), is(false));
    assertThat(result.updated().isPresent(), is(true));

    CalendarEvent anEvent = Calendar.getById(CALENDAR_ID).event(event.getId()).get();
    assertThat(anEvent.getDescription(), is(newDescription));
    assertThat(anEvent.getAttendees().size(), is(2));
    assertThat(anEvent.getAttendees().get(INITIAL_ATTENDEE).isPresent(), is(true));
    assertThat(anEvent.getAttendees().get(INITIAL_ATTENDEE).get().getParticipationStatus(),
        is(ACCEPTED));
    assertThat(anEvent.getAttendees().get(NEW_ATTENDEE).isPresent(), is(true));
    assertThat(anEvent.getAttendees().get(NEW_ATTENDEE).get().getParticipationStatus(),
        is(AWAITING));
  }

  @Test
  public void changeDateSinceTheSingleOccurrenceShouldUpdateTheEventItself() {
    List<CalendarEventOccurrence> occurrences = allOccurrencesOf(event);
    assertThat(occurrences.size(), is(1));

    CalendarEventOccurrence anOccurrence = occurrences.get(0);
    assertThat(anOccurrence.getAttendees().get(INITIAL_ATTENDEE).isPresent(), is(true));
    assertThat(anOccurrence.getAttendees().get(INITIAL_ATTENDEE).get().getParticipationStatus(),
        is(ACCEPTED));

    Period newPeriod = Period.between(LocalDate.from(anOccurrence.getStartDate()).plusDays(1),
        LocalDate.from(anOccurrence.getEndDate()).plusDays(1));
    anOccurrence.setPeriod(newPeriod);

    EventOperationResult result = event.updateSince(anOccurrence);
    // it is the event that is updated and not the occurrence in itself.
    assertThat(result.instance().isPresent(), is(false));
    assertThat(result.updated().isPresent(), is(true));

    CalendarEvent anEvent = Calendar.getById(CALENDAR_ID).event(event.getId()).get();
    assertThat(anEvent.getStartDate(), is(newPeriod.getStartDate()));
    assertThat(anEvent.getEndDate(), is(newPeriod.getEndDate()));
    assertThat(anEvent.getAttendees().size(), is(1));
    assertThat(anEvent.getAttendees().get(INITIAL_ATTENDEE).get().getParticipationStatus(),
        is(AWAITING));
  }

  @Test
  public void changeNonTemporalPropertiesOfOneOccurrenceShouldPersistIt() {
    final String newDescription = "Review the bugs dashboard";

    CalendarEventOccurrence anOccurrence = allOccurrencesOf(recurrentEvent).get(3);
    anOccurrence.setDescription(newDescription);
    anOccurrence.getAttendees().add(NEW_ATTENDEE);

    EventOperationResult result = anOccurrence.update();
    // the occurrence is well updated.
    assertThat(result.instance().isPresent(), is(true));
    assertThat(result.instance().get(), is(anOccurrence));

    anOccurrence = allOccurrencesOf(recurrentEvent).get(3);
    assertThat(anOccurrence.getDescription(), is(newDescription));
    assertThat(anOccurrence.getAttendees().size(), is(2));
    assertThat(anOccurrence.getAttendees().get(INITIAL_ATTENDEE).isPresent(), is(true));
    assertThat(anOccurrence.getAttendees().get(INITIAL_ATTENDEE).get().getParticipationStatus(),
        is(ACCEPTED));
    assertThat(anOccurrence.getAttendees().get(NEW_ATTENDEE).isPresent(), is(true));
    assertThat(anOccurrence.getAttendees().get(NEW_ATTENDEE).get().getParticipationStatus(),
        is(AWAITING));
  }

  @Test
  public void changeTheDateOfOneOccurrenceShouldPersistIt() {
    CalendarEventOccurrence anOccurrence = allOccurrencesOf(recurrentEvent).get(3);
    Period newPeriod = Period.between(LocalDate.from(anOccurrence.getStartDate()).plusDays(1),
        LocalDate.from(anOccurrence.getEndDate()).plusDays(1));
    anOccurrence.setPeriod(newPeriod);

    EventOperationResult result = anOccurrence.update();
    // the occurrences is well updated.
    assertThat(result.instance().isPresent(), is(true));
    assertThat(result.instance().get(), is(anOccurrence));

    List<CalendarEventOccurrence> occurrences = allOccurrencesOf(recurrentEvent);
    anOccurrence = occurrences.get(0);
    assertThat(anOccurrence.getPeriod().getStartDate(), is(recurrentEvent.getStartDate()));
    assertThat(anOccurrence.getPeriod().getEndDate(), is(recurrentEvent.getEndDate()));

    anOccurrence = occurrences.get(3);
    assertThat(anOccurrence.getPeriod(), is(newPeriod));
    assertThat(anOccurrence.getAttendees().get(INITIAL_ATTENDEE).isPresent(), is(true));
    assertThat(anOccurrence.getAttendees().get(INITIAL_ATTENDEE).get().getParticipationStatus(),
        is(AWAITING));
  }

  @Test
  public void changeTheSingleOccurrenceOfARecurrentEventShouldUpdateTheEventItself() {
    recurrentEvent.getRecurrence().until(1);
    recurrentEvent.update();
    List<CalendarEventOccurrence> occurrences = allOccurrencesOf(recurrentEvent);
    assertThat(occurrences.size(), is(1));

    final String newTitle = "15mn Meeting";
    Period newPeriod = Period.between(OffsetDateTime.now(), OffsetDateTime.now().plusMinutes(15));
    occurrences.get(0).setTitle(newTitle);
    occurrences.get(0).setPeriod(newPeriod);

    EventOperationResult result = occurrences.get(0).update();
    // it is the event that is updated and not the occurrence in itself.
    assertThat(result.instance().isPresent(), is(false));
    assertThat(result.updated().isPresent(), is(true));

    CalendarEvent actualEvent = Calendar.getById(CALENDAR_ID).event(recurrentEvent.getId()).get();
    assertThat(actualEvent.getTitle(), is(newTitle));
    assertThat(actualEvent.getStartDate(), is(newPeriod.getStartDate()));
    assertThat(actualEvent.getEndDate(), is(newPeriod.getEndDate()));
  }

  @Test
  public void changeNonTemporalPropertiesOffAllOccurrencesSinceAGivenDateShouldPersistAllOfThem() {
    final String newLocation = "Belledonne Room";

    List<CalendarEventOccurrence> occurrences = allOccurrencesOf(recurrentEvent);
    final Temporal recurrenceEndDate = recurrentEvent.getRecurrence().getEndDate().get();
    CalendarEventOccurrence anOccurrence = occurrences.get(7);
    anOccurrence.setLocation(newLocation);
    anOccurrence.getAttendees().add(NEW_ATTENDEE);

    EventOperationResult result = recurrentEvent.updateSince(anOccurrence);
    // a new event is created for all the modified occurrences since the one above.
    assertThat(result.created().isPresent(), is(true));
    assertThat(result.created().get().getStartDate(), is(anOccurrence.getStartDate()));
    assertThat(result.created().get().isRecurrent(), is(true));
    assertThat(result.created().get().getRecurrence().isEndless(), is(false));
    assertThat(result.created().get().getRecurrence().getRecurrenceEndDate().get(), is(recurrenceEndDate));
    // the original event is updated: its recurrence ends at the previous occurrence of the one
    // since which the occurrences were all modified.
    assertThat(result.updated().isPresent(), is(true));
    assertThat(result.updated().get().getRecurrence().getRecurrenceEndDate().get(),
        is(anOccurrence.getStartDate().minus(1, ChronoUnit.DAYS)));

    // the previous event has now 7 occurrences
    occurrences = allOccurrencesOf(recurrentEvent);
    assertThat(occurrences.size(), is(7));
    occurrences.forEach(o -> {
      assertThat(o.getLocation(), not(newLocation));
      assertThat(o.getAttendees().size(), is(1));
    });

    // the new event hos the 9 next occurrences with the updated data
    occurrences = allOccurrencesOf(result.created().get());
    assertThat(occurrences.size(), is(OCCURRENCE_COUNT - 6));
    occurrences.forEach(o -> {
      assertThat(o.getLocation(), is(newLocation));
      assertThat(o.getAttendees().size(), is(2));
      assertThat(o.getAttendees().get(INITIAL_ATTENDEE).isPresent(), is(true));
      assertThat(o.getAttendees().get(INITIAL_ATTENDEE).get().getParticipationStatus(),
          is(ACCEPTED));
      assertThat(o.getAttendees().get(NEW_ATTENDEE).isPresent(), is(true));
      assertThat(o.getAttendees().get(NEW_ATTENDEE).get().getParticipationStatus(), is(AWAITING));
    });
  }

  @Test
  public void changeTheDateOffAllOccurrencesSinceAGivenOccurrenceShouldPersistAllOfThem() {
    List<CalendarEventOccurrence> occurrences = allOccurrencesOf(recurrentEvent);
    final Temporal recurrenceEndDate = recurrentEvent.getRecurrence().getEndDate().get();
    CalendarEventOccurrence firstOccurrence = occurrences.get(0);
    CalendarEventOccurrence anOccurrence = occurrences.get(7);
    Period newPeriod = Period.between(LocalDate.from(anOccurrence.getStartDate()).plusDays(1),
        LocalDate.from(anOccurrence.getEndDate()).plusDays(1));
    anOccurrence.setPeriod(newPeriod);

    EventOperationResult result = anOccurrence.updateSinceMe();
    // a new event is created for all the modified occurrences since the one above.
    assertThat(result.created().isPresent(), is(true));
    assertThat(result.created().get().getStartDate(), is(anOccurrence.getStartDate()));
    assertThat(result.created().get().isRecurrent(), is(true));
    assertThat(result.created().get().getRecurrence().isEndless(), is(false));
    assertThat(result.created().get().getRecurrence().getRecurrenceEndDate().get(), is(recurrenceEndDate));
    // the original event is updated: its recurrence ends at the previous occurrence of the one
    // since which the occurrences were all modified.
    assertThat(result.updated().isPresent(), is(true));
    assertThat(result.updated().get().getRecurrence().getRecurrenceEndDate().get(),
        is(anOccurrence.getOriginalStartDate().minus(1, ChronoUnit.DAYS)));

    // the previous event has now 7 occurrences
    occurrences = allOccurrencesOf(recurrentEvent);
    assertThat(occurrences.size(), is(7));
    assertThat(occurrences.get(0).getPeriod(), is(firstOccurrence.getPeriod()));

    // the new event hos the 10 next occurrences with the updated data
    occurrences = allOccurrencesOf(result.created().get());
    assertThat(occurrences.size(), is(OCCURRENCE_COUNT - 7));
    assertThat(occurrences.get(0).getPeriod(), is(newPeriod));
    occurrences.forEach(
        o -> o.getAttendees().forEach(a -> assertThat(a.getParticipationStatus(), is(AWAITING))));
  }

  @Test
  public void changeSinceTheSingleOccurrenceOfARecurrentEventShouldUpdateTheEventItself() {
    recurrentEvent.getRecurrence().until(1);
    recurrentEvent.update();
    List<CalendarEventOccurrence> occurrences = allOccurrencesOf(recurrentEvent);
    assertThat(occurrences.size(), is(1));
    recurrentEvent = occurrences.get(0).getCalendarEvent();

    final String newTitle = "15mn Meeting";
    Period newPeriod = Period.between(OffsetDateTime.now(), OffsetDateTime.now().plusMinutes(15));
    occurrences.get(0).setTitle(newTitle);
    occurrences.get(0).setPeriod(newPeriod);

    EventOperationResult result = recurrentEvent.updateSince(occurrences.get(0));
    // it is the event that is updated and not the occurrence in itself.
    assertThat(result.updated().isPresent(), is(true));
    assertThat(result.created().isPresent(), is(false));

    CalendarEvent actualEvent = Calendar.getById(CALENDAR_ID).event(recurrentEvent.getId()).get();
    assertThat(actualEvent.getTitle(), is(newTitle));
    assertThat(actualEvent.getStartDate(), is(newPeriod.getStartDate()));
    assertThat(actualEvent.getEndDate(), is(newPeriod.getEndDate()));
  }

  @Test
  public void deleteTheSingleOccurrenceShouldDeleteTheEvent() throws Exception {
    List<CalendarEventOccurrence> occurrences = allOccurrencesOf(event);
    assertThat(occurrences.size(), is(1));

    EventOperationResult result = occurrences.get(0).delete();
    assertThat(result.isEmpty(), is(true));

    assertThat(getCalendarEventTableLineById(event.getId()), nullValue());
    assertThat(allOccurrencesOf(event).isEmpty(), is(true));
  }

  @Test
  public void deleteTheSingleModifiedOccurrenceShouldDeleteTheEvent() throws Exception {
    List<CalendarEventOccurrence> occurrences = allOccurrencesOf(event);
    assertThat(occurrences.size(), is(1));
    CalendarEventOccurrence occurrence = modify(occurrences.get(0));

    EventOperationResult result = occurrence.delete();
    assertThat(result.isEmpty(), is(true));

    assertThat(getCalendarEventTableLineById(event.getId()), nullValue());
    assertThat(allOccurrencesOf(event).isEmpty(), is(true));
  }

  @Test
  public void deleteOneOccurrenceShouldAddAnExceptionToTheRecurrentEvent() {
    List<CalendarEventOccurrence> occurrences = allOccurrencesOf(recurrentEvent);
    final int size = occurrences.size();
    Temporal deletedOccurrenceDate = occurrences.get(3).getStartDate();

    EventOperationResult result = occurrences.get(3).delete();
    assertThat(result.updated().isPresent(), is(true));

    occurrences = allOccurrencesOf(recurrentEvent);
    CalendarEvent event = occurrences.get(0).getCalendarEvent();
    assertThat(occurrences.size(), is(size - 1));
    assertThat(event.getRecurrence().getExceptionDates(), hasItem(deletedOccurrenceDate));
  }

  @Test
  public void deleteOneModifiedOccurrenceShouldAddAnExceptionToTheRecurrentEvent()
      throws SQLException {
    List<CalendarEventOccurrence> occurrences = allOccurrencesOf(recurrentEvent);
    final int size = occurrences.size();
    CalendarEventOccurrence occurrence = modify(occurrences.get(3));
    Temporal deletedOccurrenceDate = occurrence.getOriginalStartDate();

    EventOperationResult result = occurrence.delete();
    assertThat(result.updated().isPresent(), is(true));

    occurrences = allOccurrencesOf(recurrentEvent);
    CalendarEvent event = occurrences.get(0).getCalendarEvent();
    assertThat(occurrences.size(), is(size - 1));
    assertThat(event.getRecurrence().getExceptionDates(), hasItem(deletedOccurrenceDate));
    assertThat(getCalendarOccurrenceTableLineById(occurrence.getId()), nullValue());
  }

  @Test
  public void deleteTheSingleOccurrenceOfARecurrentEventShouldDeleteTheEventItself()
      throws Exception {
    recurrentEvent.getRecurrence().until(1);
    recurrentEvent.update();
    List<CalendarEventOccurrence> occurrences = allOccurrencesOf(recurrentEvent);
    assertThat(occurrences.size(), is(1));

    EventOperationResult result = occurrences.get(0).delete();
    assertThat(result.isEmpty(), is(true));

    assertThat(getCalendarEventTableLineById(recurrentEvent.getId()), nullValue());
  }

  @Test
  public void deleteSinceTheSingleOccurrenceShouldDeleteTheEvent() throws Exception {
    List<CalendarEventOccurrence> occurrences = allOccurrencesOf(event);
    assertThat(occurrences.size(), is(1));
    CalendarEventOccurrence occurrence = occurrences.get(0);

    EventOperationResult result = event.deleteSince(occurrence);
    assertThat(result.isEmpty(), is(true));

    assertThat(getCalendarEventTableLineById(event.getId()), nullValue());
    assertThat(allOccurrencesOf(event).isEmpty(), is(true));
    assertThat(getCalendarOccurrenceTableLineById(occurrence.getId()), nullValue());
  }

  @Test
  public void deleteSinceTheModifiedSingleOccurrenceShouldDeleteTheEvent() throws Exception {
    List<CalendarEventOccurrence> occurrences = allOccurrencesOf(event);
    assertThat(occurrences.size(), is(1));
    CalendarEventOccurrence occurrence = modify(occurrences.get(0));

    EventOperationResult result = occurrence.deleteSinceMe();
    assertThat(result.isEmpty(), is(true));

    assertThat(getCalendarEventTableLineById(event.getId()), nullValue());
    assertThat(allOccurrencesOf(event).isEmpty(), is(true));
    assertThat(getCalendarOccurrenceTableLineById(occurrence.getId()), nullValue());
  }

  @Test
  public void deleteSinceOneOccurrenceShouldUpdateTheEventRecurrenceEndDate() throws Exception {
    List<CalendarEventOccurrence> occurrences = allOccurrencesOf(recurrentEvent);
    CalendarEventOccurrence occurrence = occurrences.get(8);

    EventOperationResult result = recurrentEvent.deleteSince(occurrence);
    assertThat(result.updated().isPresent(), is(true));

    occurrences = allOccurrencesOf(recurrentEvent);
    assertThat(occurrences.size(), is(OCCURRENCE_COUNT - 8));
    assertThat(result.updated().get().getRecurrence().getRecurrenceEndDate().get(),
        is(occurrence.getStartDate().minus(1, ChronoUnit.DAYS)));
  }

  @Test
  public void deleteSinceOneModifiedOccurrenceShouldUpdateTheEventRecurrenceEndDate()
      throws Exception {
    List<CalendarEventOccurrence> occurrences = allOccurrencesOf(recurrentEvent);
    List<CalendarEventOccurrence> modifiedOccurrences =
        Arrays.asList(modify(occurrences.get(8)), modify(occurrences.get(10)),
            modify(occurrences.get(12)), modify(occurrences.get(14)));

    EventOperationResult result = recurrentEvent.deleteSince(modifiedOccurrences.get(0));
    assertThat(result.updated().isPresent(), is(true));

    occurrences = allOccurrencesOf(recurrentEvent);
    assertThat(occurrences.size(), is(OCCURRENCE_COUNT - 8));
    assertThat(result.updated().get().getRecurrence().getRecurrenceEndDate().get(),
        is(modifiedOccurrences.get(0).getOriginalStartDate().minus(1, ChronoUnit.DAYS)));
    for(CalendarEventOccurrence modifiedOccurrence: modifiedOccurrences) {
      assertThat(getCalendarOccurrenceTableLineById(modifiedOccurrence.getId()), nullValue());
    }
  }

  @Test
  public void deleteSinceTheSingleOccurrenceOfARecurrentEventShouldDeleteTheEventItself()
      throws Exception {
    recurrentEvent.getRecurrence().until(1);
    recurrentEvent.update();
    List<CalendarEventOccurrence> occurrences = allOccurrencesOf(recurrentEvent);
    assertThat(occurrences.size(), is(1));
    recurrentEvent = occurrences.get(0).getCalendarEvent();

    EventOperationResult result = recurrentEvent.deleteSince(occurrences.get(0));
    assertThat(result.isEmpty(), is(true));

    assertThat(getCalendarEventTableLineById(recurrentEvent.getId()), nullValue());
  }

  private List<CalendarEventOccurrence> allOccurrencesOf(final CalendarEvent event) {
    return Calendar.getById(CALENDAR_ID)
        .between(TODAY, TODAY.plusWeeks(OCCURRENCE_COUNT))
        .getEventOccurrences()
        .stream()
        .filter(o -> o.getCalendarEvent().equals(event))
        .collect(Collectors.toList());
  }

  private CalendarEventOccurrence modify(final CalendarEventOccurrence occurrence) {
    Period newPeriod = Period.between(occurrence.getStartDate().plus(1, ChronoUnit.DAYS),
        occurrence.getEndDate().plus(1, ChronoUnit.DAYS));
    occurrence.setPeriod(newPeriod);
    EventOperationResult result = occurrence.update();
    return result.instance().orElse(occurrence);
  }
}
  