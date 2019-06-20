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

import org.silverpeas.core.calendar.Attendee;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.calendar.CalendarEventOccurrence;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.notification.system.AbstractResourceEvent;

import javax.validation.constraints.NotNull;

/**
 * An lifecycle event of an {@link Attendee}. Such an event is triggered
 * when a change occurred in the lifecycle of an attendee (the attendee is added in an event, its
 * participation status has changed, and so on) and it is sent by the system notification bus.
 * @author mmoquillon
 */
public class AttendeeLifeCycleEvent extends AbstractResourceEvent<Attendee> {

  private final LifeCycleEventSubType subType;
  private Contribution eventOrOccurrence;

  /**
   * Constructs a new lifecycle event with the specified type and with the specified {@link
   * Attendee} instances representing each of them a state in a transition in the lifecycle of
   * an attendee.
   * @param eventOrOccurrence the contribution in relation with the action.
   * @param type the type of the event in the lifecycle of an attendee.
   * @param subType the subtype of the lifecycle transition.
   * @param attendees the different states of an attendee concerned by the event in its lifecycle.
   */
  public AttendeeLifeCycleEvent(final Contribution eventOrOccurrence, final Type type,
      final LifeCycleEventSubType subType,
      @NotNull final Attendee... attendees) {
    super(type, attendees);
    this.subType = subType;
    this.eventOrOccurrence = eventOrOccurrence;
  }

  public LifeCycleEventSubType getSubType() {
    return subType;
  }

  /**
   * Gets the event or occurrence related to the notification.
   * @return the contribution which is a {@link CalendarEvent} or a {@link CalendarEventOccurrence}.
   */
  public Contribution getEventOrOccurrence() {
    return eventOrOccurrence;
  }
}
