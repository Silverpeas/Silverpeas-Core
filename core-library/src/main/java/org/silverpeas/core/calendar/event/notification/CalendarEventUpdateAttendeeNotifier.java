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
package org.silverpeas.core.calendar.event.notification;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.event.CalendarEvent;
import org.silverpeas.core.notification.user.UserNotification;
import org.silverpeas.core.notification.user.client.constant.NotifAction;

/**
 * Notifier of an update of a {@link CalendarEvent} instance. It listens for such changes in the
 * lifecycle of the {@link CalendarEvent} instances and then it notifies the attendees and the
 * creator about them.
 * @author mmoquillon
 */
public class CalendarEventUpdateAttendeeNotifier
    extends AttendeeNotifier<CalendarEventLifeCycleEvent> {

  @Override
  public void onUpdate(final CalendarEventLifeCycleEvent event) throws Exception {
    CalendarEvent before = event.getTransition().getBefore();
    CalendarEvent after = event.getTransition().getAfter();
    if (before.getAttendees().equals(after.getAttendees())) {
      // the update is about the event itself and not about the attendees
      UserNotification notification =
          new CalendarEventAttendeeNotificationBuilder(after, NotifAction.UPDATE).from(
              User.getCurrentRequester())
              .to(concernedAttendeesIn(after))
              .about(UpdateCause.EVENT_UPDATE)
              .build();
      notification.send();
    }
  }
}
