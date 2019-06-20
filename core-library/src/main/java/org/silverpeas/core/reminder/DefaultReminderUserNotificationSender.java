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

package org.silverpeas.core.reminder;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.reminder.usernotification.ReminderUserNotificationSender;
import org.silverpeas.core.reminder.usernotification
    .ReminderUserNotificationSenderByContributionType;
import org.silverpeas.core.util.ServiceProvider;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author silveryocha
 */
@Singleton
public class DefaultReminderUserNotificationSender implements ReminderUserNotificationSender {

  private Map<String, Class<? extends ReminderUserNotificationSenderByContributionType>>
      potentialSendersByType = new HashMap<>();

  @Override
  public void sendAbout(final Reminder reminder) {
    final String contributionType = reminder.getContributionId().getType();

    final Class<? extends ReminderUserNotificationSenderByContributionType> senderClass =
        getNotificationSender(
        contributionType);

    final ReminderUserNotificationSenderByContributionType sender = ServiceProvider
        .getService(senderClass);

    sender.sendAbout(reminder, contributionType);
  }

  /**
   * From the given contribution type, returning the potential implementations of {@link Reminder}
   * notification sender.
   * @param type type of a {@link Contribution}.
   * @return the potential sender.
   */
  private Class<? extends ReminderUserNotificationSenderByContributionType> getNotificationSender(
      final String type) {
    return potentialSendersByType.computeIfAbsent(type, t -> {
      Class<? extends ReminderUserNotificationSenderByContributionType> potentialSender = null;
      // looking for potential implementations
      Collection<ReminderUserNotificationSenderByContributionType> senders = ServiceProvider
          .getAllServices(ReminderUserNotificationSenderByContributionType.class);
      for (ReminderUserNotificationSenderByContributionType sender : senders) {
        if (sender.isReminderNotificationSenderOfContributionType(t)) {
          if (potentialSender == null) {
            potentialSender = sender.getClass();
          } else {
            throw new SilverpeasRuntimeException(
                "only one reminder sender can be implemented by contribution type");
          }
        }
      }
      return potentialSender != null
          ? potentialSender
          : DefaultContributionReminderUserNotification.class;
    });
  }

}
