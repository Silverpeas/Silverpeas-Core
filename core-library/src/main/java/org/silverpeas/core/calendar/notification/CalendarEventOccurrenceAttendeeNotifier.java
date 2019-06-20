/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.calendar.notification;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.CalendarEventOccurrence;
import org.silverpeas.core.notification.user.UserNotification;
import org.silverpeas.core.notification.user.client.constant.NotifAction;

/**
 * Notifier to the attendees of a calendar event occurrence about a change in the lifecycle of this
 * single occurrence or since this occurrence up to the end of the event recurrence.
 * @author mmoquillon
 */
public class CalendarEventOccurrenceAttendeeNotifier
    extends AttendeeNotifier<CalendarEventOccurrenceLifeCycleEvent> {

  @Override
  public void onDeletion(final CalendarEventOccurrenceLifeCycleEvent event) throws Exception {
    // notify the attendees and the previous updater of the event occurrence deletion
    CalendarEventOccurrence deleted = event.getTransition().getBefore();
    CalendarOperation operation = CalendarOperation.EVENT_DELETION;
    if (event.getSubtype() == LifeCycleEventSubType.SINCE) {
      operation = CalendarOperation.SINCE_EVENT_DELETION;
    } else if (event.getSubtype() == LifeCycleEventSubType.SINGLE) {
      operation = CalendarOperation.EVENT_DELETION;
    }
    UserNotification notification =
        new AttendeeNotificationBuilder(deleted, NotifAction.DELETE).immediately()
            .from(User.getCurrentRequester())
            .to(concernedAttendeesIn(deleted.asCalendarComponent()))
            .about(operation)
            .build();
    notification.send();
  }

  @Override
  public void onUpdate(final CalendarEventOccurrenceLifeCycleEvent event) throws Exception {
    // notify the attendees and the previous updater about the modification of properties of the
    // event occurrence
    CalendarEventOccurrence before = event.getTransition().getBefore();
    CalendarEventOccurrence after = event.getTransition().getAfter();
    if (after.isModifiedSince(before)) {
      // the update is about the event occurrence itself
      CalendarOperation operation = CalendarOperation.EVENT_UPDATE;
      if (event.getSubtype() == LifeCycleEventSubType.SINCE) {
        operation = CalendarOperation.SINCE_EVENT_UPDATE;
      } else if (event.getSubtype() == LifeCycleEventSubType.SINGLE) {
        operation = CalendarOperation.EVENT_UPDATE;
      }

      UserNotification notification =
          new AttendeeNotificationBuilder(after, NotifAction.UPDATE).immediately()
              .from(User.getCurrentRequester())
              .to(concernedAttendeesIn(before.asCalendarComponent()))
              .about(operation)
              .build();
      notification.send();
    }

    if (!after.getAttendees().isSameAs(before.getAttendees())) {
      // the update is about the attendees themselves
      AttendeeLifeCycleEventNotifier.notifyAttendees(event.getSubtype(), after,
          before.getAttendees(), after.getAttendees());
    }
  }
}
  