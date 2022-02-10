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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.calendar.notification;

import org.silverpeas.core.calendar.Attendee;
import org.silverpeas.core.calendar.CalendarComponent;
import org.silverpeas.core.notification.system.CDIAfterSuccessfulTransactionResourceEventListener;
import org.silverpeas.core.notification.system.ResourceEvent;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.silverpeas.core.calendar.CalendarEventUtil.asAttendee;

/**
 * A notifier of attendees about some lifecycle events triggered by the Calendar engine.
 * @author mmoquillon
 */
public abstract class AbstractNotifier<T extends ResourceEvent>
    extends CDIAfterSuccessfulTransactionResourceEventListener<T> {

  protected List<Attendee> ownerOf(final CalendarComponent calendarComponent) {
    return singletonList(asAttendee(calendarComponent.getLastUpdater(), calendarComponent));
  }

  protected List<Attendee> concernedAttendeesIn(final CalendarComponent calendarComponent) {
    return calendarComponent.getAttendees().stream().collect(Collectors.toList());
  }

  protected List<Attendee> attendeesIn(final CalendarComponent calendarComponent) {
    return calendarComponent.getAttendees().stream().collect(Collectors.toList());
  }
}
