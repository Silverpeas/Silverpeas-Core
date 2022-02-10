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

package org.silverpeas.core.calendar;

import org.silverpeas.core.admin.user.model.User;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.Temporal;

/**
 * @author Yohann Chastagnier
 */
public class CalendarEventUtil {

  /**
   * Hidden constructor.
   */
  private CalendarEventUtil() {
  }

  /**
   * Centralizes the format treatment of title display, according the given parameters, which
   * determine the context, the title is modified.
   * @param component the component data.
   * @param componentInstanceId the identifier of component instance into which the component is
   * handled.
   * @param canBeAccessed indicates if the component is accessible for the current user into the current
   * context.
   * @return the display title as string.
   */
  public static String formatTitle(final CalendarComponent component,
      final String componentInstanceId, boolean canBeAccessed) {
    String title = canBeAccessed ? component.getTitle() : null;
    if (!componentInstanceId.equals(component.getCalendar().getComponentInstanceId())) {
      title = title != null ? title + '\n' : "";
      title += '(' + component.getCalendar().getTitle() + ')';
    }
    return title;
  }

  /**
   * Gets the given temporal according to the calendar component data.<br>
   * If the component is on all days, no offset is applied.
   * @param component the component data.
   * @param temporal the temporal to format.
   * @return the date, with offset if the component is not on all days.
   */
  public static Temporal getDateWithOffset(final CalendarComponent component,
      final Temporal temporal) {
    return getDateWithOffset(component, temporal, null);
  }

  /**
   * Gets the given temporal according to the calendar component data.<br>
   * If the component is on all days, no offset is applied.
   * If a specific zoneId is given, then the date is set to the offset of the given zoneId
   * instead of the one linked to the calendar.
   * @param component the component data.
   * @param temporal the temporal to format.
   * @param zoneId the zoneId requested (optional).
   * @return the date, with offset if the component is not on all days.
   */
  public static Temporal getDateWithOffset(final CalendarComponent component,
      final Temporal temporal, final ZoneId zoneId) {
    final ZoneId toZoneId = zoneId != null ? zoneId : component.getCalendar().getZoneId();
    return component.getPeriod().isInDays() ? temporal :
        ((OffsetDateTime) temporal).atZoneSameInstant(toZoneId).toOffsetDateTime();
  }

  /**
   * Formats the given temporal according to the calendar component data and given zoneId.
   * @param component the component data.
   * @param temporal the temporal to format.
   * @param zoneId the zoneId requested (optional).
   * @return the formatted date.
   * @see #getDateWithOffset(CalendarComponent, Temporal, ZoneId)
   */
  public static String formatDateWithOffset(final CalendarComponent component, final Temporal temporal,
      final ZoneId zoneId) {
    return getDateWithOffset(component, temporal, zoneId).toString();
  }

  /**
   * Makes the specified user in Silverpeas an attendee in the given calendar component.
   * @param user a user in Silverpeas.
   * @param component the calendar component in which the user attends.
   * @return an attendee in the specified calendar component.
   */
  public static Attendee asAttendee(final User user, final CalendarComponent component) {
    return InternalAttendee.fromUser(user).to(component);
  }
}
