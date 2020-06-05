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

import org.silverpeas.core.contribution.ContributionDeletion;
import org.silverpeas.core.contribution.ContributionModification;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

/**
 * Deleter of reminders relative to a contribution that has been deleted.
 * @author silveryocha
 */
public class ContributionReminderListener
    implements ContributionModification, ContributionDeletion {
  @Override
  public void update(final Contribution before, final Contribution after) {
    try {
      final List<Reminder> toUnschedule = new ArrayList<>();
      getReminders(before).forEach(r -> {
        try {
          r.schedule();
        } catch (IllegalArgumentException | IllegalStateException e) {
          toUnschedule.add(r);
          SilverLogger.getLogger(this).warn(e);
        }
      });
      toUnschedule.stream()
          .filter(Reminder::isScheduled)
          .forEach(r -> r.unschedule(r.isSystemUser()));
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  @Override
  public void delete(final Contribution contribution) {
    try {
      List<Reminder> reminders = emptyList();
      if (contribution instanceof WithReminder) {
        reminders = Reminder.getByContribution(contribution.getContributionId());
      }
      if (reminders.isEmpty()) {
        Optional<Contribution> parent = contribution.getParent();
        if (parent.isPresent() && parent.get() instanceof WithReminder) {
          update(parent.get(), null);
        }
      } else {
        reminders.forEach(Reminder::unschedule);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).warn(e);
    }
  }

  /**
   * Gets reminders about a contribution.
   * @param contribution a contribution
   * @return the list of reminders if any, empty list otherwise.
   */
  private List<Reminder> getReminders(Contribution contribution) {
    List<Reminder> reminders = emptyList();
    if (contribution instanceof WithReminder) {
      reminders = Reminder.getByContribution(contribution.getContributionId());
    }
    if (reminders.isEmpty()) {
      Optional<Contribution> parent = contribution.getParent();
      if (parent.isPresent() && parent.get() instanceof WithReminder) {
        reminders = Reminder.getByContribution(parent.get().getContributionId());
      }
    }
    return reminders;
  }
}
  