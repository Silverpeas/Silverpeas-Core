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
package org.silverpeas.core.calendar.notification;

import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.calendar.Attendee;
import org.silverpeas.core.calendar.AttendeeSet;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.notification.system.CDIResourceEventNotifier;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.util.ServiceProvider;

import java.util.HashSet;
import java.util.Set;

/**
 * A notifier of lifecycle events of {@link Attendee}s.
 * @author mmoquillon
 */
@Bean
public class AttendeeLifeCycleEventNotifier
    extends CDIResourceEventNotifier<Attendee, AttendeeLifeCycleEvent> {

  private Contribution eventOrOccurrence;

  public static AttendeeLifeCycleEventNotifier get() {
    return ServiceProvider.getService(AttendeeLifeCycleEventNotifier.class);
  }

  /**
   * Creates an event about the specified operation on the specified attendees in a single event.
   * @param type the type of operation on the attendees.
   * @param attendees the attendees concerned by the event.
   * @return the event relating a change in the lifecycle of the attendees.
   */
  @Override
  protected AttendeeLifeCycleEvent createResourceEventFrom(final ResourceEvent.Type type,
      final Attendee... attendees) {
    return new AttendeeLifeCycleEvent(eventOrOccurrence, type, LifeCycleEventSubType.SINGLE,
        attendees);
  }

  /**
   * Creates an event about the specified operation on the specified attendees in a single or
   * in several events (indicated by the operation subtype).
   * @param type the type of operation on the attendees.
   * @param subType the subtype of the lifecycle change indicating if it is for a single or more
   * events.
   * @param attendees the attendees concerned by the event.
   */
  protected final void notifyEventOn(final ResourceEvent.Type type, LifeCycleEventSubType subType,
      Attendee... attendees) {
    notify(new AttendeeLifeCycleEvent(eventOrOccurrence, type, subType, attendees));
  }



  public static void notifyAttendees(LifeCycleEventSubType subtype,
      final Contribution eventOrOccurrence, AttendeeSet before,
      AttendeeSet after) {
    AttendeeLifeCycleEventNotifier notifier = AttendeeLifeCycleEventNotifier.get();
    notifier.eventOrOccurrence = eventOrOccurrence;
    Set<String> allIds = new HashSet<>();
    if (before != null) {
      before.forEach(a -> allIds.add(a.getId()));
    }
    if (after != null) {
      after.forEach(a -> allIds.add(a.getId()));
    }
    allIds.forEach(i -> {
      Attendee attendeeOnLeft = before != null ? before.get(i).orElse(null) : null;
      Attendee attendeeOnRight = after != null ? after.get(i).orElse(null) : null;
      if (attendeeOnLeft != null && attendeeOnRight != null) {
        if (areDifferent(attendeeOnLeft, attendeeOnRight)) {
          notifier.notifyEventOn(ResourceEvent.Type.UPDATE, subtype, attendeeOnLeft,
              attendeeOnRight);
        }
      } else if (attendeeOnRight != null) {
        notifier.notifyEventOn(ResourceEvent.Type.CREATION, subtype, attendeeOnRight);
      } else {
        notifier.notifyEventOn(ResourceEvent.Type.DELETION, subtype, attendeeOnLeft);
      }
    });
  }

  private static boolean areDifferent(final Attendee attendeeOnLeft,
      final Attendee attendeeOnRight) {
    return attendeeOnLeft.getPresenceStatus() != attendeeOnRight.getPresenceStatus() ||
        attendeeOnLeft.getParticipationStatus() != attendeeOnRight.getParticipationStatus();
  }
}
