/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.importexport.ical;

import org.silverpeas.core.calendar.CalendarEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An exportable calendar is a collection of events that can be exported in an iCal calendar.
 */
public class ExportableCalendar implements Serializable {
  private static final long serialVersionUID = 4769214092203515307L;

  private Set<CalendarEvent> events;

  /**
   * Creates a new calendar ready to be exported with the specified calendar events.
   * @param events the events to export.
   * @return an exportable calendar.
   */
  public static ExportableCalendar with(final Collection<CalendarEvent> events) {
    Set<CalendarEvent> exportableEvents = new HashSet<>(events);
    return new ExportableCalendar(exportableEvents);
  }

  /**
   * Creates a new calendar ready to be exported with the specified calendar events.
   * @param events the events to export.
   * @return an exportable calendar.
   */
  public static ExportableCalendar with(final CalendarEvent... events) {
    Set<CalendarEvent> exportableEvents = new HashSet<>(Arrays.asList(events));
    return new ExportableCalendar(exportableEvents);
  }

  private ExportableCalendar(final Set<CalendarEvent> events) {
    this.events = events;
  }

  /**
   * Gets the events to export.
   * @return a list of events to export.
   */
  public List<CalendarEvent> getEvents() {
    return new ArrayList<>(this.events);
  }

  /**
   * Is this exportable calendar is empty?
   * @return true if contains no events.
   */
  public boolean isEmpty() {
    return this.events.isEmpty();
  }
}
