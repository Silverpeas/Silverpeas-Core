/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.calendar.view;

import org.silverpeas.core.calendar.CalendarEventOccurrence;

import java.util.List;
import java.util.Map;

/**
 * A view in which the occurrences of the calendar events are grouped by a given event
 * or occurrence property whose type is T.
 * @author mmoquillon
 */
@FunctionalInterface
public interface CalendarEventView<T> {

  /**
   * Applies this view on the specified list of calendar event occurrences. The occurrences will
   * be grouped by a specific property.
   * @param occurrences a list of calendar event occurrences.
   * @return a map in which the occurrences are grouped by a specific property of type T.
   */
  Map<T, List<CalendarEventOccurrence>> apply(final List<CalendarEventOccurrence> occurrences);
}
  