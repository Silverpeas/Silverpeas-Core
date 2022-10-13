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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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
import org.silverpeas.core.calendar.CalendarEventOccurrence;
import org.silverpeas.core.notification.system.CDIResourceEventNotifier;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.util.ServiceProvider;

/**
 * A notifier of a change in the life-cycle of one or several occurrences of a given calendar event.
 * @author mmoquillon
 */
@Bean
public class CalendarEventOccurrenceLifeCycleEventNotifier extends
    CDIResourceEventNotifier<CalendarEventOccurrence, CalendarEventOccurrenceLifeCycleEvent> {

  public static CalendarEventOccurrenceLifeCycleEventNotifier get() {
    return ServiceProvider.getService(CalendarEventOccurrenceLifeCycleEventNotifier.class);
  }

  @Override
  protected CalendarEventOccurrenceLifeCycleEvent createResourceEventFrom(
      final ResourceEvent.Type type, final CalendarEventOccurrence... resource) {
    return new CalendarEventOccurrenceLifeCycleEvent(type,
        LifeCycleEventSubType.NONE, resource);
  }

  public final void notifyEventOn(final ResourceEvent.Type type,
      LifeCycleEventSubType subType,
      CalendarEventOccurrence... occurrences) {
    notify(new CalendarEventOccurrenceLifeCycleEvent(type, subType, occurrences));
  }
}
