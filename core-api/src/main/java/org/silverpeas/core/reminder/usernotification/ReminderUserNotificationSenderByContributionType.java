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

package org.silverpeas.core.reminder.usernotification;

import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.reminder.Reminder;
import org.silverpeas.core.util.ServiceProvider;

/**
 * In charge of sending user notifications about reminders according to the type of linked
 * {@link Contribution}.
 * @author silveryocha
 */
public interface ReminderUserNotificationSenderByContributionType {

  static ReminderUserNotificationSenderByContributionType get() {
    return ServiceProvider.getService(ReminderUserNotificationSenderByContributionType.class);
  }

  /**
   * <p>
   * Indicates if the implementation is able to send notification about a contribution of the
   * given type.
   * </p>
   * <p>
   * The main interest of this method is to increase the performance into mechanism of sending.
   * </p>
   * @param type a contribution type.
   * @return true if implementation is able to send notification about a contribution of the
   * given type, false otherwise.
   */
  boolean isReminderNotificationSenderOfContributionType(String type);

  /**
   * Sends the user notification about a specified reminder according the type of a
   * {@link Contribution}.
   * @param reminder a reminder.
   * @param contributionType a contribution type.
   */
  void sendAbout(final Reminder reminder, final String contributionType);
}
