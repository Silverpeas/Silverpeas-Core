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
package org.silverpeas.core.calendar;

import java.time.temporal.Temporal;

/**
 * A builder of event occurrences dedicated to the implementation of the services defined in the
 * Silverpeas Calendar API.
 * @author mmoquillon
 */
public class CalendarEventOccurrenceBuilder {

  private CalendarEvent event;
  private Temporal start;
  private Temporal end;

  private CalendarEventOccurrenceBuilder(final CalendarEvent event)  {
    this.event = event;
  }

  public static CalendarEventOccurrenceBuilder forEvent(final CalendarEvent event) {
    return new CalendarEventOccurrenceBuilder(event);
  }

  public CalendarEventOccurrenceBuilder startingAt(final Temporal temporal) {
    this.start = temporal;
    return this;
  }

  public CalendarEventOccurrenceBuilder endingAt(final Temporal temporal) {
    this.end = temporal;
    return this;
  }

  public CalendarEventOccurrence build() {
    return new CalendarEventOccurrence(this.event, this.start, this.end);
  }
}
