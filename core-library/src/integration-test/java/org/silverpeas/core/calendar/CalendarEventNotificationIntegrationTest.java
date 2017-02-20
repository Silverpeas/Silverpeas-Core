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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.notification.AttendeeLifeCycleEvent;
import org.silverpeas.core.calendar.notification.AttendeeNotifier;
import org.silverpeas.core.calendar.notification.CalendarEventLifeCycleEvent;
import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.test.CalendarWarBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Integration tests on the notification sent when some actions were operated in a calendar event:
 * <ul>
 * <li>The event is created,</li>
 * <li>The event is updated</li>
 * <li>The event is deleted</li>
 * <li>An attendee is added</li>
 * <li>An attendee is removed</li>
 * <li>An attendee has been updated</li>
 * <li>The participation of an attendee has been updated</li>
 * <li>The presence status of an attendee has been updated</li>
 * </ul>
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class CalendarEventNotificationIntegrationTest extends BaseCalendarTest {

  private static final String CALENDAR_ID = "ID_3";
  private static final String EVENT_ID = "ID_E_1";
  private static final String EVENT_TITLE = "an event";
  private static final String EVENT_DESCRIPTION = "a description";
  private static final String USER_ID = "1";

  static {
    // This static block permits to ensure that the UNIT TEST is entirely executed into UTC
    // TimeZone.
    TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
  }

  @Inject
  private CalendarEventNotificationListener eventListener;

  @Inject
  private AttendeeNotificationListener attendeeListener;

  @Deployment
  public static Archive<?> createTestArchive() {
    return CalendarWarBuilder.onWarForTestClass(CalendarEventNotificationIntegrationTest.class)
        .addClasses(AttendeeNotifier.class)
        .addAsResource(BaseCalendarTest.TABLE_CREATION_SCRIPT.substring(1))
        .addAsResource(INITIALIZATION_SCRIPT.substring(1))
        .build();
  }

  @Before
  public void listenersAreSet() {
    assertThat(eventListener, notNullValue());
    assertThat(attendeeListener, notNullValue());
  }

  @After
  public void resetListeners() {
    attendeeListener.reset();
    eventListener.reset();
  }

  @Test
  public void planninACalendarEventSendANotification() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent.on(LocalDate.now())
        .createdBy(USER_ID)
        .withTitle(EVENT_TITLE)
        .withDescription(EVENT_DESCRIPTION)
        .withAttendee("jack@london.uk")
        .planOn(calendar);

    assertThat(eventListener.hasBeenNotified(), is(true));
    assertThat(eventListener.getRecievedNotifAction(), is(NotifAction.CREATE));

    assertThat(attendeeListener.hasBeenNotified(), is(true));
    assertThat(attendeeListener.getRecievedNotifAction().size(), is(1));
    assertThat(attendeeListener.getRecievedNotifAction().contains(NotifAction.CREATE), is(true));
  }

  @Test
  public void deletingACalendarEventSendANotification() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent event = calendar.event(EVENT_ID).get();
    assertThat(event.getAttendees().size(), is(2));
    event.delete();

    assertThat(eventListener.hasBeenNotified(), is(true));
    assertThat(eventListener.getRecievedNotifAction(), is(NotifAction.DELETE));

    assertThat(attendeeListener.hasBeenNotified(), is(true));
    assertThat(attendeeListener.getRecievedNotifAction().size(), is(2));
    assertThat(attendeeListener.getRecievedNotifAction().get(0), is(NotifAction.DELETE));
    assertThat(attendeeListener.getRecievedNotifAction().get(1), is(NotifAction.DELETE));
  }

  @Test
  public void updatingCalendarEventSendANotification() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent event = calendar.event(EVENT_ID).get();
    event.recur(Recurrence.every(TimeUnit.MONTH));
    event.update();

    assertThat(eventListener.hasBeenNotified(), is(true));
    assertThat(eventListener.getRecievedNotifAction(), is(NotifAction.UPDATE));

    // Modifying recurrence reset the participation of attendees
    assertThat(attendeeListener.hasBeenNotified(), is(true));
    assertThat(attendeeListener.getRecievedNotifAction(),
        contains(NotifAction.UPDATE, NotifAction.UPDATE));
  }

  @Test
  public void addingAnAttendeeSendANotification() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent event = calendar.event(EVENT_ID).get();
    event.withAttendee(User.getById("2"));
    event.update();

    assertThat(eventListener.hasBeenNotified(), is(true));
    assertThat(eventListener.getRecievedNotifAction(), is(NotifAction.UPDATE));

    assertThat(attendeeListener.hasBeenNotified(), is(true));
    assertThat(attendeeListener.getRecievedNotifAction().size(), is(1));
    assertThat(attendeeListener.getRecievedNotifAction().contains(NotifAction.CREATE), is(true));
  }

  @Test
  public void removingAnAttendeeSendANotification() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent event = calendar.event(EVENT_ID).get();
    Attendee attendee = event.getAttendees().stream().findFirst().get();
    event.getAttendees().remove(attendee);
    event.update();

    assertThat(eventListener.hasBeenNotified(), is(true));
    assertThat(eventListener.getRecievedNotifAction(), is(NotifAction.UPDATE));

    assertThat(attendeeListener.hasBeenNotified(), is(true));
    assertThat(attendeeListener.getRecievedNotifAction().size(), is(1));
    assertThat(attendeeListener.getRecievedNotifAction().contains(NotifAction.DELETE), is(true));
  }

  @Test
  public void updateAttendeeParticipationSendANotification() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent event = calendar.event(EVENT_ID).get();
    Attendee attendee = event.getAttendees().stream().findFirst().get();
    attendee.decline();
    event.update();

    assertThat(eventListener.hasBeenNotified(), is(true));
    assertThat(eventListener.getRecievedNotifAction(), is(NotifAction.UPDATE));

    assertThat(attendeeListener.hasBeenNotified(), is(true));
    assertThat(attendeeListener.getRecievedNotifAction().size(), is(1));
    assertThat(attendeeListener.getRecievedNotifAction().contains(NotifAction.UPDATE), is(true));
  }

  @Test
  public void delegateParticipationSendTwoNotifications() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent event = calendar.event(EVENT_ID).get();
    Attendee attendee = event.getAttendees().stream().findFirst().get();
    attendee.delegateTo(User.getById("2"));
    event.update();

    assertThat(eventListener.hasBeenNotified(), is(true));
    assertThat(eventListener.getRecievedNotifAction(), is(NotifAction.UPDATE));

    assertThat(attendeeListener.hasBeenNotified(), is(true));
    assertThat(attendeeListener.getRecievedNotifAction().size(), is(2));
    assertThat(attendeeListener.getRecievedNotifAction().contains(NotifAction.UPDATE), is(true));
    assertThat(attendeeListener.getRecievedNotifAction().contains(NotifAction.CREATE), is(true));
  }

  @Test
  public void updateAttendeePresenceSendANotification() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent event = calendar.event(EVENT_ID).get();
    Attendee attendee = event.getAttendees().stream().findFirst().get();
    attendee.setPresenceStatus(Attendee.PresenceStatus.INFORMATIVE);
    event.update();

    assertThat(eventListener.hasBeenNotified(), is(true));
    assertThat(eventListener.getRecievedNotifAction(), is(NotifAction.UPDATE));

    assertThat(attendeeListener.hasBeenNotified(), is(true));
    assertThat(attendeeListener.getRecievedNotifAction().size(), is(1));
    assertThat(attendeeListener.getRecievedNotifAction().contains(NotifAction.UPDATE), is(true));
  }

  @Singleton
  public static class AttendeeNotificationListener
      extends AttendeeNotifier<AttendeeLifeCycleEvent> {

    private List<NotifAction> notifAction = new ArrayList<>(2);

    protected void reset() {
      notifAction.clear();
    }

    public List<NotifAction> getRecievedNotifAction() {
      return this.notifAction;
    }

    public boolean hasBeenNotified() {
      return !this.notifAction.isEmpty();
    }

    @Override
    public void onDeletion(final AttendeeLifeCycleEvent event) throws Exception {
      notifAction.add(NotifAction.DELETE);
    }

    @Override
    public void onUpdate(final AttendeeLifeCycleEvent event) throws Exception {
      notifAction.add(NotifAction.UPDATE);
      assertThat(event.getTransition().getBefore(), notNullValue());
      assertThat(event.getTransition().getAfter(), notNullValue());
    }

    @Override
    public void onCreation(final AttendeeLifeCycleEvent event) throws Exception {
      notifAction.add(NotifAction.CREATE);
    }
  }

  @Singleton
  public static class CalendarEventNotificationListener
      extends AttendeeNotifier<CalendarEventLifeCycleEvent> {

    private NotifAction notifAction = null;

    protected void reset() {
      notifAction = null;
    }

    public NotifAction getRecievedNotifAction() {
      return this.notifAction;
    }

    public boolean hasBeenNotified() {
      return this.notifAction != null;
    }

    @Override
    public void onDeletion(final CalendarEventLifeCycleEvent event) throws Exception {
      notifAction = NotifAction.DELETE;
    }

    @Override
    public void onUpdate(final CalendarEventLifeCycleEvent event) throws Exception {
      notifAction = NotifAction.UPDATE;
      assertThat(event.getTransition().getBefore(), notNullValue());
      assertThat(event.getTransition().getAfter(), notNullValue());
    }

    @Override
    public void onCreation(final CalendarEventLifeCycleEvent event) throws Exception {
      notifAction = NotifAction.CREATE;
    }
  }
}
