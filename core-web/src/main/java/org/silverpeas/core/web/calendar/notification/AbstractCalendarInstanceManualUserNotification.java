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

package org.silverpeas.core.web.calendar.notification;

import org.silverpeas.core.calendar.CalendarEventOccurrence;
import org.silverpeas.core.calendar.notification.user.CalendarEventOccurrenceNotifyUserNotificationBuilder;
import org.silverpeas.core.notification.user.AbstractComponentInstanceManualUserNotification;
import org.silverpeas.core.notification.user.NotificationContext;
import org.silverpeas.core.notification.user.UserNotification;

import javax.ws.rs.WebApplicationException;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.silverpeas.core.SilverpeasExceptionMessages.unknown;
import static org.silverpeas.core.webapi.calendar.CalendarEventOccurrenceEntity.decodeId;

/**
 * @author silveryocha
 */
public abstract class AbstractCalendarInstanceManualUserNotification
    extends AbstractComponentInstanceManualUserNotification {

  private static final String OCCURRENCE_KEY = "CalendarEventOccurrenceKey";

  @Override
  protected boolean check(final NotificationContext context) {
    final String occurrenceId = context.getContributionId();
    final CalendarEventOccurrence occurrence = getCalendarEventOccurrence(occurrenceId);
    context.put(OCCURRENCE_KEY, occurrence);
    return occurrence.canBeAccessedBy(context.getSender());
  }

  @Override
  public UserNotification createUserNotification(final NotificationContext context) {
    final CalendarEventOccurrence occurrence = context.getObject(OCCURRENCE_KEY);
    return new CalendarEventOccurrenceNotifyUserNotificationBuilder(occurrence, context.getSender())
        .build();
  }

  private CalendarEventOccurrence getCalendarEventOccurrence(final String occurrenceId) {
    final String decodeId = decodeId(occurrenceId);
    return CalendarEventOccurrence.getById(decodeId)
        .orElseThrow(() -> new WebApplicationException(unknown("occurrence", decodeId), NOT_FOUND));
  }
}
