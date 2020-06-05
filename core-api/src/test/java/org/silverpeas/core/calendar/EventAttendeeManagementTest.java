/*
 * Copyright (C) 2000 - 2020 Silverpeas
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

import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests on the management of attendees to events.
 * @author mmoquillon
 */
public class EventAttendeeManagementTest {

  @Test
  public void addAnAttendeeToAnEvent() {
    CalendarEvent event =
        anEvent().withAttendee("joe@dalton.com").withAttendee("averel@dalton.com");
    assertThat(
        event.getAttendees().contains(ExternalAttendee.withEmail("joe@dalton.com")
            .to(event.asCalendarComponent())),
        is(true));
    assertThat(
        event.getAttendees().contains(ExternalAttendee.withEmail("averel@dalton.com")
            .to(event.asCalendarComponent())),
        is(true));
  }

  @Test
  public void delegateAnAttendeeToAnotherUserAddThisUserAmongTheAttendeesToTheEvent() {
    CalendarEvent event = anEvent();
    Attendee attendee = ExternalAttendee.withEmail("joe@dalton.com")
        .to(event.asCalendarComponent());
    event.getAttendees().add(attendee);
    assertThat(event.getAttendees().size(), is(1));

    attendee.delegateTo("averel@dalton.com");

    assertThat(event.getAttendees().size(), is(2));
    assertThat(
        event.getAttendees().contains(ExternalAttendee.withEmail("averel@dalton.com")
            .to(event.asCalendarComponent())),
        is(true));
  }

  @Test
  public void delegateAnAttendeeToAnotherUserChangeTheParticipationStatus() {
    CalendarEvent event = anEvent();
    Attendee attendee = ExternalAttendee.withEmail("joe@dalton.com")
        .to(event.asCalendarComponent());
    event.getAttendees().add(attendee);
    assertThat(event.getAttendees().size(), is(1));

    attendee.delegateTo("averel@dalton.com");

    assertThat(attendee.getParticipationStatus(), is(Attendee.ParticipationStatus.DELEGATED));
  }

  @Test
  public void delegateAnAttendeeToAnotherUserSetsTheDelegationBetweenTwoThem() {
    CalendarEvent event = anEvent();
    Attendee attendee = ExternalAttendee.withEmail("joe@dalton.com")
        .to(event.asCalendarComponent());
    event.getAttendees().add(attendee);
    assertThat(event.getAttendees().size(), is(1));

    attendee.delegateTo("averel@dalton.com");

    assertThat(attendee.getDelegate().isPresent(), is(true));
    assertThat(attendee.getDelegate().get(),
        is(ExternalAttendee.withEmail("averel@dalton.com")
            .to(event.asCalendarComponent())));
    assertThat(attendee.getDelegate().get().getDelegate().get(), is(attendee));
  }

  private CalendarEvent anEvent() {
    return CalendarEvent.on(LocalDate.now())
        .withTitle("Event Title")
        .withDescription("Event Description");
  }
}
