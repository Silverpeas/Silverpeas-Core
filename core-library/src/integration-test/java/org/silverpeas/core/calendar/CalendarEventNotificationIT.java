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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.calendar.notification.AbstractNotifier;
import org.silverpeas.core.calendar.notification.AttendeeLifeCycleEvent;
import org.silverpeas.core.calendar.notification.AttendeeLifeCycleEventNotifier;
import org.silverpeas.core.calendar.notification.CalendarEventLifeCycleEvent;
import org.silverpeas.core.calendar.notification.CalendarEventOccurrenceLifeCycleEvent;
import org.silverpeas.core.calendar.notification.LifeCycleEventSubType;
import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.persistence.datasource.OperationContext;
import org.silverpeas.core.test.CalendarWarBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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
public class CalendarEventNotificationIT extends BaseCalendarTest {

  private static final String CALENDAR_ID = "ID_3";
  private static final String EVENT_ID = "ID_E_1";
  private static final String EVENT_TITLE = "an event";
  private static final String EVENT_DESCRIPTION = "a description";
  private static final String USER_ID = "1";

  @Inject
  private CalendarEventNotificationListener eventListener;

  @Inject
  private AttendanceNotificationListener attendanceListener;

  @Inject
  private CalendarEventOccurrenceNotificationListener occurrenceListener;

  @Deployment
  public static Archive<?> createTestArchive() {
    return CalendarWarBuilder.onWarForTestClass(CalendarEventNotificationIT.class)
        .addAsResource(BaseCalendarTest.TABLE_CREATION_SCRIPT.substring(1))
        .addAsResource(INITIALIZATION_SCRIPT.substring(1))
        .build();
  }

  @Before
  public void listenersAreSet() {
    assertThat(eventListener, notNullValue());
    assertThat(occurrenceListener, notNullValue());
    assertThat(attendanceListener, notNullValue());
    OperationContext.fromUser(USER_ID);
  }

  @After
  public void resetListeners() {
    attendanceListener.reset();
    occurrenceListener.reset();
    eventListener.reset();
  }

  /**
   * Planning an event notifies the attendees about their participation in this event.
   */
  @Test
  public void planninACalendarEventSendANotification() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent.on(LocalDate.now())
        .createdBy(USER_ID)
        .withTitle(EVENT_TITLE)
        .withDescription(EVENT_DESCRIPTION)
        .withAttendee("jack@london.uk")
        .planOn(calendar);

    // event planning notification
    assertThat(eventListener.hasBeenNotified(), is(true));
    assertThat(eventListener.getRecievedNotifAction(), is(NotifAction.CREATE));

    // occurrence change notification
    assertThat(occurrenceListener.hasBeenNotified(), is(false));

    // attendance change notification
    assertThat(attendanceListener.hasBeenNotified(), is(false));
  }

  /**
   * Deleting an event notifies the attendees about its deletion.
   */
  @Test
  public void deletingACalendarEventSendANotification() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    Optional<CalendarEvent> mayBeEvent = calendar.event(EVENT_ID);
    assertThat(mayBeEvent.isPresent(), is(true));
    CalendarEvent event = mayBeEvent.get();
    assertThat(event.getAttendees().size(), is(2));
    event.delete();

    assertThat(eventListener.hasBeenNotified(), is(true));
    assertThat(eventListener.getRecievedNotifAction(), is(NotifAction.DELETE));

    assertThat(occurrenceListener.hasBeenNotified(), is(false));

    assertThat(attendanceListener.hasBeenNotified(), is(false));
  }

  /**
   * Updating the properties of an event notifies the attendees about the change(s).
   */
  @Test
  public void updatingCalendarEventSendANotification() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    Optional<CalendarEvent> mayBeEvent = calendar.event(EVENT_ID);
    assertThat(mayBeEvent.isPresent(), is(true));
    CalendarEvent event = mayBeEvent.get();
    event.recur(Recurrence.every(TimeUnit.MONTH));
    event.update();

    assertThat(eventListener.hasBeenNotified(), is(true));
    assertThat(eventListener.getRecievedNotifAction(), is(NotifAction.UPDATE));

    assertThat(occurrenceListener.hasBeenNotified(), is(false));

    // Modifying recurrence reset the participation of attendees
    assertThat(attendanceListener.hasBeenNotified(), is(true));
    assertThat(attendanceListener.getReceivedNotifyAction(),
        contains(NotifAction.UPDATE, NotifAction.UPDATE));
  }

  /**
   * Adding a new attendee notifies him about its participation.
   */
  @Test
  public void addingAnAttendeeSendANotification() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    Optional<CalendarEvent> mayBeEvent = calendar.event(EVENT_ID);
    assertThat(mayBeEvent.isPresent(), is(true));
    CalendarEvent event = mayBeEvent.get();
    event.withAttendee(User.getById("2"));
    event.update();

    assertThat(eventListener.hasBeenNotified(), is(false));

    assertThat(occurrenceListener.hasBeenNotified(), is(false));

    assertThat(attendanceListener.hasBeenNotified(), is(true));
    assertThat(attendanceListener.getReceivedNotifyAction().size(), is(1));
    assertThat(attendanceListener.getReceivedNotifyAction().contains(NotifAction.CREATE), is(true));
  }

  /**
   * Removing an attendee from an event notifies him about the removal of its participation.
   */
  @Test
  public void removingAnAttendeeSendANotification() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    Optional<CalendarEvent> mayBeEvent = calendar.event(EVENT_ID);
    assertThat(mayBeEvent.isPresent(), is(true));
    CalendarEvent event = mayBeEvent.get();
    Optional<Attendee> mayBeAttendee = event.getAttendees().stream().findFirst();
    assertThat(mayBeAttendee.isPresent(), is(true));
    Attendee attendee = mayBeAttendee.get();
    event.getAttendees().remove(attendee);
    event.update();

    assertThat(eventListener.hasBeenNotified(), is(false));

    assertThat(occurrenceListener.hasBeenNotified(), is(false));

    assertThat(attendanceListener.hasBeenNotified(), is(true));
    assertThat(attendanceListener.getReceivedNotifyAction().size(), is(1));
    assertThat(attendanceListener.getReceivedNotifyAction().contains(NotifAction.DELETE), is(true));
  }

  /**
   * Updating the participation status of an attendee notifies other attendees about that.
   */
  @Test
  public void updateAttendeeParticipationSendANotification() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    Optional<CalendarEvent> mayBeEvent = calendar.event(EVENT_ID);
    assertThat(mayBeEvent.isPresent(), is(true));
    CalendarEvent event = mayBeEvent.get();
    Optional<Attendee> mayBeAttendee = event.getAttendees().stream().findFirst();
    assertThat(mayBeAttendee.isPresent(), is(true));
    Attendee attendee = mayBeAttendee.get();
    attendee.decline();
    event.update();

    assertThat(eventListener.hasBeenNotified(), is(false));

    assertThat(occurrenceListener.hasBeenNotified(), is(false));

    assertThat(attendanceListener.hasBeenNotified(), is(true));
    assertThat(attendanceListener.getReceivedNotifyAction().size(), is(1));
    assertThat(attendanceListener.getReceivedNotifyAction().contains(NotifAction.UPDATE), is(true));
  }

  /**
   * Delegating the participation to another person notifies the delegate about that.
   */
  @Test
  public void delegateParticipationSendTwoNotifications() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    Optional<CalendarEvent> mayBeEvent = calendar.event(EVENT_ID);
    assertThat(mayBeEvent.isPresent(), is(true));
    CalendarEvent event = mayBeEvent.get();
    Optional<Attendee> mayBeAttendee = event.getAttendees().stream().findFirst();
    assertThat(mayBeAttendee.isPresent(), is(true));
    Attendee attendee = mayBeAttendee.get();
    attendee.delegateTo(User.getById("2"));
    event.update();

    assertThat(eventListener.hasBeenNotified(), is(false));

    assertThat(occurrenceListener.hasBeenNotified(), is(false));

    assertThat(attendanceListener.hasBeenNotified(), is(true));
    assertThat(attendanceListener.getReceivedNotifyAction().size(), is(2));
    assertThat(attendanceListener.getReceivedNotifyAction().contains(NotifAction.UPDATE), is(true));
    assertThat(attendanceListener.getReceivedNotifyAction().contains(NotifAction.CREATE), is(true));
  }

  /**
   * Updates the presence status of an attendee notifies him about that.
   */
  @Test
  public void updateAttendeePresenceSendANotification() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    Optional<CalendarEvent> mayBeEvent = calendar.event(EVENT_ID);
    assertThat(mayBeEvent.isPresent(), is(true));
    CalendarEvent event = mayBeEvent.get();
    Optional<Attendee> mayBeAttendee = event.getAttendees().stream().findFirst();
    assertThat(mayBeAttendee.isPresent(), is(true));
    Attendee attendee = mayBeAttendee.get();
    attendee.setPresenceStatus(Attendee.PresenceStatus.INFORMATIVE);
    event.update();

    assertThat(eventListener.hasBeenNotified(), is(false));

    assertThat(occurrenceListener.hasBeenNotified(), is(false));

    assertThat(attendanceListener.hasBeenNotified(), is(true));
    assertThat(attendanceListener.getReceivedNotifyAction().size(), is(1));
    assertThat(attendanceListener.getReceivedNotifyAction().contains(NotifAction.UPDATE), is(true));
  }

  /**
   * Listens for change in the attendance in an event (or in a given event's occurrence).
   */
  @Bean
  @Singleton
  public static class AttendanceNotificationListener extends
      AbstractNotifier<AttendeeLifeCycleEvent> {


    private final List<NotifAction> notifAction = new ArrayList<>(2);

    protected void reset() {
      notifAction.clear();
    }

    public List<NotifAction> getReceivedNotifyAction() {
      return this.notifAction;
    }

    public boolean hasBeenNotified() {
      return !this.notifAction.isEmpty();
    }

    @Override
    public void onDeletion(final AttendeeLifeCycleEvent event) {
      notifAction.add(NotifAction.DELETE);
    }

    @Override
    public void onUpdate(final AttendeeLifeCycleEvent event) {
      notifAction.add(NotifAction.UPDATE);
      assertThat(event.getTransition().getBefore(), notNullValue());
      assertThat(event.getTransition().getAfter(), notNullValue());
    }

    @Override
    public void onCreation(final AttendeeLifeCycleEvent event) {
      notifAction.add(NotifAction.CREATE);
    }
  }

  /**
   * In the case of an update, we first check the event's properties are modified to set the
   * accordingly the notification action. Then we check the attendees are modified. In this case,
   * we send a notification about a change in the attendance in the event. This behaviour is
   * implemented by the attendee notification mechanism. We just simulate here this behaviour.
   */
  @Bean
  @Singleton
  public static class CalendarEventNotificationListener extends
      AbstractNotifier<CalendarEventLifeCycleEvent> {

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
    public void onDeletion(final CalendarEventLifeCycleEvent event) {
      notifAction = NotifAction.DELETE;
    }

    @Override
    public void onUpdate(final CalendarEventLifeCycleEvent event) {
      CalendarEvent before = event.getTransition().getBefore();
      CalendarEvent after = event.getTransition().getAfter();
      assertThat(event.getTransition().getBefore(), notNullValue());
      assertThat(event.getTransition().getAfter(), notNullValue());

      if (after.isModifiedSince(before)) {
        notifAction = NotifAction.UPDATE;
      }

      if (after.getAttendees().isNotSameAs(before.getAttendees())) {
        AttendeeLifeCycleEventNotifier.notifyAttendees(LifeCycleEventSubType.SINGLE, after,
            before.getAttendees(), after.getAttendees());
      }
    }

    @Override
    public void onCreation(final CalendarEventLifeCycleEvent event) {
      notifAction = NotifAction.CREATE;
    }
  }

  /**
   * In the case of an update, we first check the properties of the event's occurrence are modified
   * to set the accordingly the notification action. Then we check the attendees are modified. In
   * this case, we send a notification about a change in the attendance in the event. This behaviour
   * is implemented by the attendee notification mechanism. We just simulate here this behaviour.
   */
  @Bean
  @Singleton
  public static class CalendarEventOccurrenceNotificationListener
      extends AbstractNotifier<CalendarEventOccurrenceLifeCycleEvent> {

    private NotifAction notifAction = null;

    protected void reset() {
      notifAction = null;
    }

    public boolean hasBeenNotified() {
      return this.notifAction != null;
    }

    @Override
    public void onDeletion(final CalendarEventOccurrenceLifeCycleEvent event) {
      notifAction = NotifAction.DELETE;
    }

    @Override
    public void onUpdate(final CalendarEventOccurrenceLifeCycleEvent event) {
      CalendarEventOccurrence before = event.getTransition().getBefore();
      CalendarEventOccurrence after = event.getTransition().getAfter();
      assertThat(event.getTransition().getBefore(), notNullValue());
      assertThat(event.getTransition().getAfter(), notNullValue());

      if (after.isModifiedSince(before)) {
        notifAction = NotifAction.UPDATE;
      }

      if (after.getAttendees().isNotSameAs(before.getAttendees())) {
        AttendeeLifeCycleEventNotifier.notifyAttendees(LifeCycleEventSubType.SINGLE, after,
            before.getAttendees(), after.getAttendees());
      }
    }

    @Override
    public void onCreation(final CalendarEventOccurrenceLifeCycleEvent event) {
      notifAction = NotifAction.CREATE;
    }
  }
}
