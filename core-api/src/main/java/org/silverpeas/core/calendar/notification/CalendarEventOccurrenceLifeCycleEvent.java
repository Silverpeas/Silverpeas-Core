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

import org.silverpeas.core.calendar.CalendarEventOccurrence;
import org.silverpeas.core.notification.system.AbstractResourceEvent;

import javax.validation.constraints.NotNull;

/**
 * Notification event about changes in the life-cycle of one or several event occurrences.
 * @author mmoquillon
 */
public class CalendarEventOccurrenceLifeCycleEvent
    extends AbstractResourceEvent<CalendarEventOccurrence> {

  private final LifeCycleEventSubType subtype;

  /**
   * Constructs a new lifecycle event with the specified type and that implies the specified
   * {@link CalendarEventOccurrence} instances, each of them representing a different state in the
   * lifecycle of one or several occurrences of an event.
   * @param type the type of the lifecycle event (the type of the transition occurring in the
   * lifecycle).
   * @param subType the subtype of the the type of the lifecycle event.
   * @param occurrences the states of one or several event occurrences implied by the state
   * transition.
   */
  public CalendarEventOccurrenceLifeCycleEvent(final Type type, final LifeCycleEventSubType subType,
      @NotNull final CalendarEventOccurrence... occurrences) {
    super(type, occurrences);
    this.subtype = subType;
  }

  /**
   * Gets the subtype of the transition type.
   * @return the subtype of the event type.
   */
  public LifeCycleEventSubType getSubtype() {
    return subtype;
  }

}
  