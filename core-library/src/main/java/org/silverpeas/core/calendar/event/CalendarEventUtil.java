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
}
