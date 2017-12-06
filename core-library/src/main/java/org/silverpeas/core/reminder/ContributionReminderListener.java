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
    final List<Reminder> reminders = getReminders(before);
    reminders.forEach(r -> {
      if (r instanceof DurationReminder) {
        final DurationReminder durationReminder = (DurationReminder) r;
        durationReminder.unschedule();
        durationReminder.schedule();
      }
    });
  }

  @Override
  public void delete(final Contribution contribution) {
    getReminders(contribution).forEach(Reminder::unschedule);
  }

  /**
   * Gets reminders about a contribution.
   * @param contribution a contribution
   * @return the list of reminders if any, empty list otherwise.
   */
  private List<Reminder> getReminders(Contribution contribution) {
    List<Reminder> reminders = emptyList();
    if (contribution instanceof WithReminder) {
      reminders = Reminder.getByContributionId(contribution.getContributionId());
    }
    if (reminders.isEmpty()) {
      Optional<Contribution> parent = contribution.getParent();
      if (parent.isPresent() && parent.get() instanceof WithReminder) {
        reminders = Reminder.getByContributionId(parent.get().getContributionId());
      }
    }
    return reminders;
  }
}
  