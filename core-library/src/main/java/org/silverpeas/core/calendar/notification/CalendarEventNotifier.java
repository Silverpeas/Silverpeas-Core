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
package org.silverpeas.core.calendar.notification;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.Attendee;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.persistence.datasource.OperationContext;

import java.util.List;

/**
 * Notifier to the attendees of a calendar event about a change in the lifecycle of this event.
 * @author mmoquillon
 */
public class CalendarEventNotifier
    extends AbstractNotifier<CalendarEventLifeCycleEvent> {

  @Override
  public void onCreation(final CalendarEventLifeCycleEvent event) {
    final CalendarEvent created = event.getTransition().getAfter();
    final CalendarOperation attendeeOperation = created.isRecurrent()
        ? CalendarOperation.SINCE_ATTENDEE_ADDING
        : CalendarOperation.ATTENDEE_ADDING;
    // notify the attendees about their participation to this new event
    final List<Attendee> attendees = attendeesIn(created.asCalendarComponent());
    final AttendeeNotificationBuilder attendeeNotificationBuilder =
        new AttendeeNotificationBuilder(created, NotifAction.CREATE)
            .immediately()
            .from(getSender())
            .to(attendees)
            .about(attendeeOperation, attendees);
    attendeeNotificationBuilder.build().send();
    // notify the subscribers (by excluding attendees already notified)
    new SubscriberNotificationBuilder(created, NotifAction.CREATE)
        .from(getSender())
        .about(CalendarOperation.EVENT_CREATE)
        .excludingUsersIds(attendeeNotificationBuilder.getUserIdsToNotify())
        .build()
        .send();
  }

  @Override
  public void onUpdate(final CalendarEventLifeCycleEvent event) {
    final CalendarEvent before = event.getTransition().getBefore();
    final CalendarEvent after = event.getTransition().getAfter();
    if (after.isModifiedSince(before)) {
      // the update is about the event itself
      final CalendarOperation operation = after.isRecurrent()
          ? CalendarOperation.SINCE_EVENT_UPDATE
          : CalendarOperation.EVENT_UPDATE;
      // notify the attendees and the previous updater about the modification of properties of the
      // event
      final AttendeeNotificationBuilder attendeeNotificationBuilder =
          new AttendeeNotificationBuilder(after, NotifAction.UPDATE)
              .immediately()
              .from(getSender())
              .to(concernedAttendeesIn(before.asCalendarComponent()))
              .about(operation);
      attendeeNotificationBuilder.build().send();
      // notify the subscribers (by excluding attendees already notified)
      new SubscriberNotificationBuilder(after, NotifAction.UPDATE)
          .from(getSender())
          .about(operation)
          .excludingUsersIds(attendeeNotificationBuilder.getUserIdsToNotify())
          .build()
          .send();
    }
    if (!after.getAttendees().isSameAs(before.getAttendees())) {
      // the update is about the attendees themselves
      LifeCycleEventSubType subType =
          after.isRecurrent() ? LifeCycleEventSubType.SINCE : LifeCycleEventSubType.SINGLE;
      AttendeeLifeCycleEventNotifier.notifyAttendees(subType, after, before.getAttendees(),
          after.getAttendees());
    }
  }

  @Override
  public void onDeletion(final CalendarEventLifeCycleEvent event) throws Exception {
    final CalendarEvent deleted = event.getTransition().getBefore();
    final CalendarOperation operation = deleted.isRecurrent()
        ? CalendarOperation.SINCE_EVENT_DELETION
        : CalendarOperation.EVENT_DELETION;
    // notify the attendees and the previous updater of the event deletion
    new AttendeeNotificationBuilder(deleted, NotifAction.DELETE)
        .immediately()
        .from(getSender())
        .to(concernedAttendeesIn(deleted.asCalendarComponent()))
        .about(operation)
        .build()
        .send();
  }

  /**
   * Gets the user behind the invocation of the attendees notification. It should be set in the
   * {@link OperationContext} of the current process (thread). Otherwise it is the current
   * requester that is used.
   * @return the user sending the notification.
   */
  private User getSender() {
    return OperationContext.fromCurrentRequester().getUser();
  }
}
