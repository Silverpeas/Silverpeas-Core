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

import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.notification.system.AbstractResourceEvent;

import javax.validation.constraints.NotNull;

/**
 * A lifecycle event of {@link org.silverpeas.core.calendar.CalendarEvent}
 * instances. Such an event is triggered when a change occurred in the lifecycle of a calendar
 * event (the event is planned into calendar, its state has changed, and so on) and it is sent by
 * the system notification bus.
 * @author mmoquillon
 */
public class CalendarEventLifeCycleEvent extends AbstractResourceEvent<CalendarEvent> {

  /**
   * Constructs a new lifecycle event with the specified type and that implies the specified
   * {@link CalendarEvent} instances, each of them representing a different state in the lifecycle
   * of the calendar event.
   * @param type the type of the lifecycle event (the type of the transition occurring in the
   * calendar event's lifecycle).
   * @param calendarEvent the states of a calendar event concerned by a state transition in
   * its lifecycle.
   */
  public CalendarEventLifeCycleEvent(final Type type,
      @NotNull final CalendarEvent... calendarEvent) {
    super(type, calendarEvent);
  }
}
