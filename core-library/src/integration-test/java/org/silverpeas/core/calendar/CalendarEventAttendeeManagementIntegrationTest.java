/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.event.Attendee;
import org.silverpeas.core.calendar.event.CalendarEvent;
import org.silverpeas.core.calendar.event.ExternalAttendee;
import org.silverpeas.core.calendar.event.InternalAttendee;
import org.silverpeas.core.test.CalendarWarBuilder;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration tests on the persistence of the events with some attendees.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class CalendarEventAttendeeManagementIntegrationTest extends BaseCalendarTest {

  private static final String CALENDAR_ID = "ID_3";
  private static final String EVENT_WITH_ATTENDEE = "ID_E_1";
  private static final String EVENT_WITH_ATTENDEE_AND_DATE_PART = "ID_E_5";
  private static final String EVENT_WITHOUT_ATTENDEE = "ID_E_4";

  @Deployment
  public static Archive<?> createTestArchive() {
    return CalendarWarBuilder.onWarForTestClass(CalendarEventManagementIntegrationTest.class)
        .addAsResource(BaseCalendarTest.TABLE_CREATION_SCRIPT.substring(1))
        .addAsResource(INITIALIZATION_SCRIPT.substring(1))
        .build();
  }

  @Test
  public void getAllTheAttendees() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent eventWithAttendees = calendar.event(EVENT_WITH_ATTENDEE).get();

    assertThat(eventWithAttendees.getAttendees().size(), is(2));

    Attendee actualAttendee = in(eventWithAttendees.getAttendees()).find(
        InternalAttendee.fromUser(expectedUser()).to(eventWithAttendees));
    assertThat(actualAttendee.getDelegate().isPresent(), is(false));
    assertThat(actualAttendee.getParticipationStatus(), is(Attendee.ParticipationStatus.ACCEPTED));
    assertThat(actualAttendee.getPresenceStatus(), is(Attendee.PresenceStatus.OPTIONAL));

    actualAttendee = in(eventWithAttendees.getAttendees()).find(
        ExternalAttendee.withEmail("john.doe@silverpeas.org").to(eventWithAttendees));
    assertThat(actualAttendee.getDelegate().isPresent(), is(false));
    assertThat(actualAttendee.getParticipationStatus(), is(Attendee.ParticipationStatus.TENTATIVE));
    assertThat(actualAttendee.getPresenceStatus(), is(Attendee.PresenceStatus.REQUIRED));
  }

  @Test
  public void getAllTheAttendeesWithParticipationDate() {
    CalendarEvent eventWithAttendeesAndDatePart =
        CalendarEvent.getById(EVENT_WITH_ATTENDEE_AND_DATE_PART);

    assertThat(eventWithAttendeesAndDatePart.getAttendees().size(), is(1));

    Attendee actualAttendee = in(eventWithAttendeesAndDatePart.getAttendees())
        .find(InternalAttendee.fromUser(expectedUser()).to(eventWithAttendeesAndDatePart));
    assertThat(actualAttendee.getDelegate().isPresent(), is(false));
    assertThat(actualAttendee.getParticipationStatus(), is(Attendee.ParticipationStatus.AWAITING));
    assertThat(actualAttendee.getPresenceStatus(), is(Attendee.PresenceStatus.OPTIONAL));
    assertThat(actualAttendee.getParticipationOn().getAll().size(), is(1));
    final Map.Entry<OffsetDateTime, Attendee.ParticipationStatus> entry =
        actualAttendee.getParticipationOn().getAll().entrySet().iterator().next();
    assertThat(entry.getKey().toString(), is("2016-01-16T00:00:00Z"));
    assertThat(entry.getValue(), is(Attendee.ParticipationStatus.DECLINED));
  }

  @Test
  public void addNewAttendees() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent event = calendar.event(EVENT_WITHOUT_ATTENDEE).get();
    assertThat(event.getAttendees().isEmpty(), is(true));

    Attendee silverpeasUser = InternalAttendee.fromUser(getMockedUser()).to(event);
    Attendee externalUser = ExternalAttendee.withEmail("toto@chez-les-papoos")
        .to(event)
        .withPresenceStatus(Attendee.PresenceStatus.OPTIONAL);
    event.getAttendees().add(silverpeasUser);
    event.getAttendees().add(externalUser);
    event.update();

    event = calendar.event(EVENT_WITHOUT_ATTENDEE).get();
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

    Attendee attendee = in(eventWithAttendees.getAttendees()).find(
        ExternalAttendee.withEmail("john.doe@silverpeas.org").to(eventWithAttendees));
    assertThat(attendee.getParticipationStatus(), is(Attendee.ParticipationStatus.TENTATIVE));
    assertThat(attendee.getPresenceStatus(), is(Attendee.PresenceStatus.REQUIRED));
    attendee.withPresenceStatus(Attendee.PresenceStatus.OPTIONAL).accept();
    eventWithAttendees.update();

    eventWithAttendees = calendar.event(EVENT_WITH_ATTENDEE).get();
    attendee = in(eventWithAttendees.getAttendees()).find(
        ExternalAttendee.withEmail("john.doe@silverpeas.org").to(eventWithAttendees));
    assertThat(attendee.getParticipationStatus(), is(Attendee.ParticipationStatus.ACCEPTED));
    assertThat(attendee.getPresenceStatus(), is(Attendee.PresenceStatus.OPTIONAL));
  }

  @Test
  public void removeAnAttendee() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent eventWithAttendees = calendar.event(EVENT_WITH_ATTENDEE).get();
    Attendee attendeeToRemove =
        ExternalAttendee.withEmail("john.doe@silverpeas.org").to(eventWithAttendees);

    eventWithAttendees.getAttendees().remove(attendeeToRemove);
    eventWithAttendees.update();

    eventWithAttendees = calendar.event(EVENT_WITH_ATTENDEE).get();
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

    Attendee attendeeToDelegate = in(eventWithAttendees.getAttendees()).find(
        InternalAttendee.fromUser(expectedUser()).to(eventWithAttendees));
    attendeeToDelegate.delegateTo(getMockedUser());
    eventWithAttendees.update();

    eventWithAttendees = calendar.event(EVENT_WITH_ATTENDEE).get();
    assertThat(eventWithAttendees.getAttendees().size(), is(3));
    Attendee delegate = in(eventWithAttendees.getAttendees()).find(
        InternalAttendee.fromUser(getMockedUser()).to(eventWithAttendees));
    attendeeToDelegate = in(eventWithAttendees.getAttendees()).find(
        InternalAttendee.fromUser(expectedUser()).to(eventWithAttendees));
    assertThat(delegate.getDelegate().isPresent(), is(true));
    assertThat(delegate.getDelegate().get(), is(attendeeToDelegate));
    assertThat(attendeeToDelegate.getDelegate().isPresent(), is(true));
    assertThat(attendeeToDelegate.getDelegate().get(), is(delegate));
    assertThat(attendeeToDelegate.getParticipationStatus(),
        is(Attendee.ParticipationStatus.DELEGATED));
  }

  private User expectedUser() {
    User user = mock(User.class);
    when(user.getId()).thenReturn("1");
    return user;
  }

  public static AttendeeFinder in(final Set<Attendee> attendees) {
    return new AttendeeFinder(attendees);
  }

  private static class AttendeeFinder {

    private final Set<Attendee> attendees;

    public AttendeeFinder(final Set<Attendee> attendees) {
      this.attendees = attendees;
    }

    public Attendee find(final Attendee attendeeToFind) {
      Optional<Attendee> actualAttendee =
          attendees.stream().filter(a -> a.equals(attendeeToFind)).findFirst();
      assertThat(actualAttendee.isPresent(), is(true));
      return actualAttendee.get();
    }
  }

}
