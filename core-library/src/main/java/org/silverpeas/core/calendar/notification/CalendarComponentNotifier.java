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
import org.silverpeas.core.calendar.CalendarEventOccurrence;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.notification.user.UserNotification;
import org.silverpeas.core.notification.user.client.constant.NotifAction;

import java.util.Collections;

/**
 * A notifier of attendees about their participation in a calendar component. It listens for change
 * in the attendance and then, for each change, it notifies the concerned attendee(s) about it.
 * <p>
 * When a calendar component is planned in a given calendar or when such a calendar component
 * is unplanned then a change is also occurring in the attendance as the attendees are all
 * respectively just added or just deleted. This notifier will then also inform such of
 * lifecycle events.
 * @author mmoquillon
 */
public class CalendarComponentNotifier extends AbstractNotifier<AttendeeLifeCycleEvent> {

  /**
   * An attendee has been removed. The attendee is informed about it (he shouldn't be the user
   * behind this attendance deletion).
   * @param event the lifecycle event on the deletion of an attendance.
   * @throws Exception if an error occurs while notifying the attendee.
   */
  @Override
  public void onDeletion(final AttendeeLifeCycleEvent event) throws Exception {
    Attendee attendee = event.getTransition().getBefore();
    CalendarOperation operation =
        event.getSubType() == LifeCycleEventSubType.SINGLE ? CalendarOperation.ATTENDEE_REMOVING :
            CalendarOperation.SINCE_ATTENDEE_REMOVING;
    UserNotification notification = new AttendeeNotificationBuilder(event.getEventOrOccurrence(),
        NotifAction.UPDATE).immediately()
        .from(User.getCurrentRequester())
        .to(attendee)
        .about(operation, attendee)
        .build();
    notification.send();
  }

  /**
   * An attendee has been updated:
   * <ul>
   * <li>The presence status has been updated: the concerned attendee is informed about it (he
   * shouldn't be the user behind this status change).</li>
   * <li>The participation status has been updated: all the others attendees are informed about
   * it.</li>
   * </ul>
   * @param event the lifecycle event on the update of a resource.
   */
  @Override
  public void onUpdate(final AttendeeLifeCycleEvent event) {
    Attendee before = event.getTransition().getBefore();
    Attendee after = event.getTransition().getAfter();
    if (before.getPresenceStatus() != after.getPresenceStatus()) {
      CalendarOperation operation =
          event.getSubType() == LifeCycleEventSubType.SINGLE ? CalendarOperation.ATTENDEE_PRESENCE :
              CalendarOperation.SINCE_ATTENDEE_PRESENCE;
      UserNotification notification = new AttendeeNotificationBuilder(event.getEventOrOccurrence(),
          NotifAction.UPDATE).immediately()
          .from(User.getCurrentRequester())
          .to(after)
          .about(operation, after)
          .build();
      notification.send();
    } else if (before.getParticipationStatus() != after.getParticipationStatus()) {
      if (after.getParticipationStatus() == Attendee.ParticipationStatus.AWAITING) {
        // notify the attendees about their participation to this new event
        Contribution modified = event.getEventOrOccurrence();
        boolean isRecurrent =
            modified instanceof CalendarEventOccurrence || ((CalendarEvent) modified).isRecurrent();
        CalendarOperation operation = isRecurrent ? CalendarOperation.SINCE_ATTENDEE_ADDING :
            CalendarOperation.ATTENDEE_ADDING;
        UserNotification notification =
            new AttendeeNotificationBuilder(modified, NotifAction.CREATE)
                .immediately()
                .from(User.getCurrentRequester())
                .to(Collections.singletonList(after))
                .about(operation, after)
                .build();
        notification.send();
      } else {
        CalendarOperation operation = event.getSubType() == LifeCycleEventSubType.SINGLE ?
            CalendarOperation.ATTENDEE_PARTICIPATION : CalendarOperation.SINCE_ATTENDEE_PARTICIPATION;
        UserNotification notification =
            new AttendeeNotificationBuilder(event.getEventOrOccurrence(), NotifAction.UPDATE)
                .immediately()
                .from(User.getCurrentRequester())
                .to(ownerOf(after.getCalendarComponent()))
                .about(operation, after)
                .build();
        notification.send();
      }
    }
  }

  /**
   * An attendee has been added among the attendees. The added attendee is
   * informed about it (he shouldn't be the user behind this attendance adding).
   * @param event the lifecycle event on the adding of the attendee.
   * @throws Exception if an error occurs while notifying the attendee.
   */
  @Override
  public void onCreation(final AttendeeLifeCycleEvent event) throws Exception {
    Attendee attendee = event.getTransition().getAfter();
    CalendarOperation operation =
        event.getSubType() == LifeCycleEventSubType.SINGLE ? CalendarOperation.ATTENDEE_ADDING :
            CalendarOperation.SINCE_ATTENDEE_ADDING;
    UserNotification notification = new AttendeeNotificationBuilder(event.getEventOrOccurrence(),
        NotifAction.UPDATE).immediately()
        .from(User.getCurrentRequester())
        .to(attendee)
        .about(operation, attendee)
        .build();
    notification.send();
  }
}
