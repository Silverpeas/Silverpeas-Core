/*
 * Copyright (C) 2000 - 2019 Silverpeas
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

package org.silverpeas.core.calendar.notification;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.calendar.CalendarEventOccurrence;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.reminder.BackgroundReminderProcess;
import org.silverpeas.core.reminder.DefaultContributionReminderUserNotification;
import org.silverpeas.core.reminder.Reminder;
import org.silverpeas.core.reminder.ReminderProcessName;
import org.silverpeas.core.template.SilverpeasTemplate;

import javax.inject.Named;
import javax.inject.Singleton;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.Temporal;

import static org.silverpeas.core.SilverpeasExceptionMessages.failureOnGetting;
import static org.silverpeas.core.reminder.BackgroundReminderProcess.Constants.PROCESS_NAME_SUFFIX;

/**
 * Implementation in charge of handling data about {@link org.silverpeas.core.calendar.Calendar}
 * entities.
 * @author silveryocha
 */
@Named(CalendarEventUserNotificationReminder.PROCESS_NAME + PROCESS_NAME_SUFFIX)
@Singleton
public class CalendarEventUserNotificationReminder implements BackgroundReminderProcess {

  static final String PROCESS_NAME = "CalendarEventUserNotification";
  public static final ReminderProcessName CALENDAR_EVENT_USER_NOTIFICATION = () -> PROCESS_NAME;

  @Override
  public ReminderProcessName getName() {
    return CALENDAR_EVENT_USER_NOTIFICATION;
  }

  /**
   * Sends the user notification about a specified reminder.
   * @param reminder a reminder.
   */
  @Override
  public void performWith(final Reminder reminder) {
    new UserNotification(reminder).build().send();
  }

  /**
   * Extension of the default reminder builder which is able to load the right data according the
   * scheduled date time.
   */
  static class UserNotification extends DefaultContributionReminderUserNotification {

    private CalendarEventOccurrence occurrence;
    private ZoneId calendarZoneId;

    UserNotification(final Reminder reminder) {
      super(reminder);
    }

    private CalendarEventOccurrence getOccurrence() {
      if (occurrence == null) {
        final CalendarEvent calendarEvent = (CalendarEvent) getResource();
        calendarZoneId = calendarEvent.getCalendar().getZoneId();
        final Temporal occStartDate = calendarEvent.isOnAllDay()
            ? LocalDate.from(normalizeTemporal(getScheduledDateTimeWithZeroDuration()))
            : OffsetDateTime.from(getScheduledDateTimeWithZeroDuration());

        occurrence = CalendarEventOccurrence.getBy(calendarEvent, occStartDate).orElseThrow(() ->
            new SilverpeasRuntimeException(failureOnGetting("occurrence from event and date",
            calendarEvent.getId() + " and " + occStartDate)));

        setResource(occurrence);
      }
      return occurrence;
    }

    @Override
    protected Temporal computeReminderContributionStart() {
      return normalizeTemporal(getOccurrence().getStartDate());
    }

    @Override
    protected Temporal computeReminderContributionEnd() {
      final Temporal temporal = normalizeTemporal(getOccurrence().getEndDate());
      return getOccurrence().isOnAllDay() ? ((LocalDate) temporal).minusDays(1) : temporal;
    }

    @Override
    protected ZoneId getZoneIdForNormalization() {
      return calendarZoneId;
    }

    @Override
    protected void performTemplateData(final Contribution localizedContribution,
        final SilverpeasTemplate template) {
      super.performTemplateData(localizedContribution, template);
      template.setAttribute("contributionType_" + CalendarEvent.TYPE, true);
    }
  }
}
