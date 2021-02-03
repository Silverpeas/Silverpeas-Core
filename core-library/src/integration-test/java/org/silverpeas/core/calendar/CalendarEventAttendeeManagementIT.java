/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import org.silverpeas.core.test.CalendarWarBuilder;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration tests on the persistence of the events with some attendees.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class CalendarEventAttendeeManagementIT extends BaseCalendarTest {

  private static final String CALENDAR_ID = "ID_3";
  private static final String EVENT_WITH_ATTENDEE = "ID_E_1";
  private static final String RECURRENT_EVENT = "ID_E_5";
  private static final String EVENT_WITH_ATTENDEE_AND_DATE_PART = "ID_E_5";
  private static final String EVENT_WITHOUT_ATTENDEE = "ID_E_4";

  static {
    // This static block permits to ensure that the UNIT TEST is entirely executed into UTC
    // TimeZone.
    TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return CalendarWarBuilder.onWarForTestClass(CalendarEventAttendeeManagementIT.class)
        .addAsResource(BaseCalendarTest.TABLE_CREATION_SCRIPT.substring(1))
        .addAsResource(INITIALIZATION_SCRIPT.substring(1))
        .build();
  }

  @Before
  public void prepareOperationContext() {
    OperationContext.fromUser("0");
  }

  @Test
  public void getAllTheAttendees() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent eventWithAttendees = calendar.event(EVENT_WITH_ATTENDEE).get();

    assertThat(eventWithAttendees.getAttendees().size(), is(2));

    Attendee actualAttendee = in(eventWithAttendees.getAttendees()).find(expectedUser().getId());
    assertThat(actualAttendee.getDelegate().isPresent(), is(false));
    assertThat(actualAttendee.getParticipationStatus(), is(Attendee.ParticipationStatus.ACCEPTED));
    assertThat(actualAttendee.getPresenceStatus(), is(Attendee.PresenceStatus.OPTIONAL));

    actualAttendee = in(eventWithAttendees.getAttendees()).find("john.doe@silverpeas.org");
    assertThat(actualAttendee.getDelegate().isPresent(), is(false));
    assertThat(actualAttendee.getParticipationStatus(), is(Attendee.ParticipationStatus.TENTATIVE));
    assertThat(actualAttendee.getPresenceStatus(), is(Attendee.PresenceStatus.REQUIRED));
  }

  @Test
  public void getAllTheAttendeesWithParticipationDate() {
    CalendarEvent eventWithAttendeesAndDatePart =
        CalendarEvent.getById(EVENT_WITH_ATTENDEE_AND_DATE_PART);

    assertThat(eventWithAttendeesAndDatePart.isOnAllDay(), is(true));
    assertThat(eventWithAttendeesAndDatePart.getAttendees().size(), is(1));

    Attendee actualAttendee =
        in(eventWithAttendeesAndDatePart.getAttendees()).find(expectedUser().getId());
    assertThat(actualAttendee.getDelegate().isPresent(), is(false));
    assertThat(actualAttendee.getParticipationStatus(), is(Attendee.ParticipationStatus.AWAITING));
    assertThat(actualAttendee.getPresenceStatus(), is(Attendee.PresenceStatus.OPTIONAL));
  }

  @Test
  public void addNewAttendees() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent event = calendar.event(EVENT_WITHOUT_ATTENDEE).get();
    Date lastUpdateDate = event.getLastUpdateDate();
    assertThat(event.getAttendees().isEmpty(), is(true));

    Attendee silverpeasUser =
        InternalAttendee.fromUser(getMockedUser()).to(event.asCalendarComponent());
    Attendee externalUser = ExternalAttendee.withEmail("toto@chez-les-papoos")
        .to(event.asCalendarComponent())
        .withPresenceStatus(Attendee.PresenceStatus.OPTIONAL);
    event.getAttendees().add(silverpeasUser);
    event.getAttendees().add(externalUser);
    event.update();

    event = calendar.event(EVENT_WITHOUT_ATTENDEE).get();
    assertThat(event.getLastUpdateDate(), greaterThan(lastUpdateDate));
    assertThat(event.getAttendees().size(), is(2));

    Attendee actualAttendee = in(event.getAttendees()).find(silverpeasUser);
    assertThat(actualAttendee.getDelegate().isPresent(), is(false));
    assertThat(actualAttendee.getParticipationStatus(), is(Attendee.ParticipationStatus.AWAITING));
    assertThat(actualAttendee.getPresenceStatus(), is(Attendee.PresenceStatus.REQUIRED));

    actualAttendee = in(event.getAttendees()).find(externalUser);
    assertThat(actualAttendee.getDelegate().isPresent(), is(false));
    assertThat(actualAttendee.getParticipationStatus(), is(Attendee.ParticipationStatus.AWAITING));
    assertThat(actualAttendee.getPresenceStatus(), is(Attendee.PresenceStatus.OPTIONAL));
  }

  @Test
  public void updateAnAttendee() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent eventWithAttendees = calendar.event(EVENT_WITH_ATTENDEE).get();
    Date lastUpdateDate = eventWithAttendees.getLastUpdateDate();

    Attendee attendee = in(eventWithAttendees.getAttendees()).find("john.doe@silverpeas.org");
    assertThat(attendee.getParticipationStatus(), is(Attendee.ParticipationStatus.TENTATIVE));
    assertThat(attendee.getPresenceStatus(), is(Attendee.PresenceStatus.REQUIRED));
    attendee.withPresenceStatus(Attendee.PresenceStatus.OPTIONAL).accept();
    eventWithAttendees.update();

    eventWithAttendees = calendar.event(EVENT_WITH_ATTENDEE).get();
    assertThat(eventWithAttendees.getLastUpdateDate(), is(lastUpdateDate));
    attendee = in(eventWithAttendees.getAttendees()).find("john.doe@silverpeas.org");
    assertThat(attendee.getParticipationStatus(), is(Attendee.ParticipationStatus.ACCEPTED));
    assertThat(attendee.getPresenceStatus(), is(Attendee.PresenceStatus.OPTIONAL));
  }

  @Test
  public void removeAnAttendee() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent eventWithAttendees = calendar.event(EVENT_WITH_ATTENDEE).get();
    Date lastUpdateDate = eventWithAttendees.getLastUpdateDate();
    Attendee attendeeToRemove = ExternalAttendee.withEmail("john.doe@silverpeas.org")
        .to(eventWithAttendees.asCalendarComponent());

    eventWithAttendees.getAttendees().remove(attendeeToRemove);
    eventWithAttendees.update();

    eventWithAttendees = calendar.event(EVENT_WITH_ATTENDEE).get();
    assertThat(eventWithAttendees.getLastUpdateDate(), greaterThan(lastUpdateDate));
    assertThat(eventWithAttendees.getAttendees().size(), is(1));
    Optional<Attendee> actualAttendee = eventWithAttendees.getAttendees()
        .stream()
        .filter(a -> a.equals(attendeeToRemove))
        .findFirst();
    assertThat(actualAttendee.isPresent(), is(false));
  }

  @Test
  public void delegateTheAttendanceToAnotherUser() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent eventWithAttendees = calendar.event(EVENT_WITH_ATTENDEE).get();
    Date lastUpdateDate = eventWithAttendees.getLastUpdateDate();

    Attendee attendeeToDelegate =
        in(eventWithAttendees.getAttendees()).find(expectedUser().getId());
    attendeeToDelegate.delegateTo(getMockedUser());
    eventWithAttendees.update();

    eventWithAttendees = calendar.event(EVENT_WITH_ATTENDEE).get();
    assertThat(eventWithAttendees.getLastUpdateDate(), greaterThan(lastUpdateDate));
    assertThat(eventWithAttendees.getAttendees().size(), is(3));

    Attendee delegate = in(eventWithAttendees.getAttendees()).find(getMockedUser().getId());
    attendeeToDelegate = in(eventWithAttendees.getAttendees()).find(expectedUser().getId());
    assertThat(delegate.getDelegate().isPresent(), is(true));
    assertThat(delegate.getDelegate().get(), is(attendeeToDelegate));
    assertThat(attendeeToDelegate.getDelegate().isPresent(), is(true));
    assertThat(attendeeToDelegate.getDelegate().get(), is(delegate));
    assertThat(attendeeToDelegate.getParticipationStatus(),
        is(Attendee.ParticipationStatus.DELEGATED));
  }

  @Test
  public void addANewAttendeeJustForOneOccurrence() {
    CalendarEvent event = CalendarEvent.getById(RECURRENT_EVENT);
    List<CalendarEventOccurrence> occurrences = allOccurrencesOf(event);
    Attendee newAttendee = occurrences.get(3).getAttendees().add("toto@chez-les-papoos.com");
    occurrences.get(3).update();

    occurrences = allOccurrencesOf(event);
    assertThat(occurrences.get(3).getAttendees().contains(newAttendee), is(true));
  }

  @Test
  public void deleteAnAttendeeJustForOneOccurrence() {
    CalendarEvent event = CalendarEvent.getById(RECURRENT_EVENT);
    event.getAttendees().add("john.doe@silverpeas.org");
    event.update();

    List<CalendarEventOccurrence> occurrences = allOccurrencesOf(event);
    occurrences.get(3).getAttendees().removeIf(a -> a.getId().equals("john.doe@silverpeas.org"));

    occurrences.get(3).update();

    occurrences = allOccurrencesOf(event);
    for (int i = 0; i < occurrences.size(); i++) {
      if (i == 3) {
        assertThat(occurrences.get(i).getAttendees().get("john.doe@silverpeas.org").isPresent(),
            is(false));
      } else {
        assertThat(occurrences.get(i).getAttendees().get("john.doe@silverpeas.org").isPresent(),
            is(true));
      }
    }
  }

  @Test
  public void addANewAttendeeSinceAGivenOccurrence() {
    CalendarEvent event = CalendarEvent.getById(RECURRENT_EVENT);
    List<CalendarEventOccurrence> occurrences = allOccurrencesOf(event);
    Attendee newAttendee = occurrences.get(3).getAttendees().add("toto@chez-les-papoos.com");
    event.updateSince(occurrences.get(3));

    occurrences = allOccurrencesOf(event);
    for (int i = 0; i < occurrences.size(); i++) {
      if (i >= 3) {
        assertThat(occurrences.get(i).getAttendees().contains(newAttendee), is(true));
      } else {
        assertThat(occurrences.get(i).getAttendees().contains(newAttendee), is(false));
      }
    }
  }

  @Test
  public void deleteAnAttendeeSinceAGivenOccurrence() {
    CalendarEvent event = CalendarEvent.getById(RECURRENT_EVENT);
    event.getAttendees().add("john.doe@silverpeas.org");
    event.update();

    List<CalendarEventOccurrence> occurrences = allOccurrencesOf(event);
    occurrences.get(3).getAttendees().removeIf(a -> a.getId().equals("john.doe@silverpeas.org"));

    occurrences.get(3).updateSinceMe();

    occurrences = allOccurrencesOf(event);
    for (int i = 0; i < occurrences.size(); i++) {
      if (i >= 3) {
        assertThat(occurrences.get(i).getAttendees().get("john.doe@silverpeas.org").isPresent(),
            is(false));
      } else {
        assertThat(occurrences.get(i).getAttendees().get("john.doe@silverpeas.org").isPresent(),
            is(true));
      }
    }
  }

  @Test
  public void changeTheParticipationStatusForJustOneOccurrence() {
    CalendarEvent event = CalendarEvent.getById(RECURRENT_EVENT);
    event.getAttendees().add("john.doe@silverpeas.org").tentativelyAccept();
    event.update();

    List<CalendarEventOccurrence> occurrences = allOccurrencesOf(event);
    Optional<Attendee> john = occurrences.get(3).getAttendees().get("john.doe@silverpeas.org");
    assertThat(john.isPresent(), is(true));
    assertThat(john.get().getParticipationStatus().isTentative(), is(true));
    john.get().accept();

    occurrences.get(3).update();

    occurrences = allOccurrencesOf(event);
    for (int i = 0; i < occurrences.size(); i++) {
      john = occurrences.get(i).getAttendees().get("john.doe@silverpeas.org");
      assertThat(john.isPresent(), is(true));
      if (i == 3) {
        assertThat(john.get().getParticipationStatus().isAccepted(), is(true));
      } else {
        assertThat(john.get().getParticipationStatus().isTentative(), is(true));
      }
    }
  }

  @Test
  public void changeTheParticipationStatusSinceAGivenOccurrence() {
    CalendarEvent event = CalendarEvent.getById(RECURRENT_EVENT);
    event.getAttendees().add("john.doe@silverpeas.org").tentativelyAccept();;
    event.update();

    List<CalendarEventOccurrence> occurrences = allOccurrencesOf(event);
    Optional<Attendee> john = occurrences.get(3).getAttendees().get("john.doe@silverpeas.org");
    assertThat(john.isPresent(), is(true));
    assertThat(john.get().getParticipationStatus().isTentative(), is(true));
    john.get().accept();

    occurrences.get(3).updateSinceMe();

    occurrences = allOccurrencesOf(event);
    for (int i = 0; i < occurrences.size(); i++) {
      john = occurrences.get(i).getAttendees().get("john.doe@silverpeas.org");
      assertThat(john.isPresent(), is(true));
      if (i >= 3) {
        assertThat(john.get().getParticipationStatus().isAccepted(), is(true));
      } else {
        assertThat(john.get().getParticipationStatus().isTentative(), is(true));
      }
    }
  }

  private User expectedUser() {
    User user = mock(User.class);
    when(user.getId()).thenReturn("1");
    return user;
  }

  private List<CalendarEventOccurrence> allOccurrencesOf(final CalendarEvent event) {
    return event.getCalendar()
        .between(LocalDate.parse("2016-01-01"), LocalDate.parse("2017-01-01"))
        .getEventOccurrences()
        .stream()
        .filter(o -> o.getCalendarEvent().equals(event))
        .collect(Collectors.toList());
  }

  public static AttendeeFinder in(final AttendeeSet attendees) {
    return new AttendeeFinder(attendees);
  }

  private static class AttendeeFinder {

    private final AttendeeSet attendees;

    public AttendeeFinder(final AttendeeSet attendees) {
      this.attendees = attendees;
    }

    public Attendee find(final String attendeeToFind) {
      Optional<Attendee> actualAttendee =
          attendees.stream().filter(a -> a.getId().equals(attendeeToFind)).findFirst();
      assertThat(actualAttendee.isPresent(), is(true));
      return actualAttendee.get();
    }

    public Attendee find(final Attendee attendeeToFind) {
      return find(attendeeToFind.getId());
    }
  }

}
