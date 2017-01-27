/*
 * Copyright (C) 2000 - 2017 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.calendar.event;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.Temporal;

/**
 * @author Yohann Chastagnier
 */
public class CalendarEventUtil {

  /**
   * Centralizes the format treatment of title display, according the given parameters, which
   * determine the context, the title is modified.
   * @param event the event data.
   * @param componentInstanceId the identifier of component instance into which the event is
   * handled.
   * @param canBeAccessed indicates if the event is accessible for the current user into the current
   * context.
   * @return the display title as string.
   */
  public static String formatTitle(final CalendarEvent event,
      final String componentInstanceId, boolean canBeAccessed) {
    String title = canBeAccessed ? event.getTitle() : null;
    if (!componentInstanceId.equals(event.getCalendar().getComponentInstanceId())) {
      title = '#' + (title != null ? (title + '\n') : "");
      title += '(' + event.getCalendar().getTitle() + ')';
    }
    return title;
  }

  /**
   * Gets the given temporal according to the calendar event data.<br/>
   * If the event is on all days, no offset is applied.
   * If a specific zoneId is given, then the date is set to the offset of the given zoneId
   * instead of the one linked to the calendar.
   * @param event the event data.
   * @param temporal the temporal to format.
   * @param zoneId the zoneId requested (optional).
   * @return the date, with offset if the event is not on all days.
   */
  public static Temporal getDateWithOffset(final CalendarEvent event, final Temporal temporal,
      final ZoneId zoneId) {
    final ZoneId toZoneId = zoneId != null ? zoneId : event.getCalendar().getZoneId();
    return event.isOnAllDay() ? temporal :
        ((OffsetDateTime) temporal).atZoneSameInstant(toZoneId).toOffsetDateTime();
  }

  /**
   * Formats the given temporal according to the calendar event data and given zoneId.
   * @param event the event data.
   * @param temporal the temporal to format.
   * @param zoneId the zoneId requested (optional).
   * @return the formatted date.
   * @see #getDateWithOffset(CalendarEvent, Temporal, ZoneId)
   */
  public static String formatDateWithOffset(final CalendarEvent event, final Temporal temporal,
      final ZoneId zoneId) {
    return getDateWithOffset(event, temporal, zoneId).toString();
  }
}
