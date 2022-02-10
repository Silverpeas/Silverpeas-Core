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
package org.silverpeas.web.usercalendar;

import org.silverpeas.core.web.calendar.CalendarTimeWindowViewContext;

import java.time.ZoneId;

/**
 * The specific time window view context for user calendar component which inherits of all behaviors
 * of centralized {@link CalendarTimeWindowViewContext}.
 * @author Yohann Chastagnier
 */
public class UserCalendarTimeWindowViewContext extends CalendarTimeWindowViewContext {

  /**
   * Constructs a new calendar view of the specified user calendar.<br>
   * @param componentInstanceId the component instance identifier.
   * @param locale the locale to take into account (fr for the french locale (fr_FR) for example).
   * @param zoneId the zoneId to take into account (ZoneId.of("Europe/Paris") for example).
   */
  UserCalendarTimeWindowViewContext(final String componentInstanceId, final String locale, final
      ZoneId zoneId) {
    super(componentInstanceId, locale, zoneId);
  }
}
