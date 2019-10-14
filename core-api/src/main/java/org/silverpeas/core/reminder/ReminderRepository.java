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
package org.silverpeas.core.reminder;

import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.persistence.datasource.repository.EntityRepository;
import org.silverpeas.core.util.ServiceProvider;

import java.util.List;

/**
 * Repository of reminders. It manages the persistence of the reminders of the users on
 * contributions.
 * @author mmoquillon
 */
public interface ReminderRepository extends EntityRepository<Reminder> {

  /**
   * Gets an instance of this repository.
   * @return a {@link ReminderRepository} instance.
   */
  static ReminderRepository get() {
    return ServiceProvider.getSingleton(ReminderRepository.class);
  }

  /**
   * Finds in this repository all the reminders set by and for the specified user.
   * @param id the unique identifier of a user.
   * @return a list of {@link Reminder} instances. Empty if the user has no reminders.
   */
  List<Reminder> findByUserId(final String id);

  /**
   * Finds in this repository all the reminders related to the specified contribution.
   * @param contributionId the unique identifier of a contribution.
   * @return a list of {@link Reminder} instances. Empty if there is no reminders that were set for
   * the contribution.
   */
  List<Reminder> findByContributionId(final ContributionIdentifier contributionId);

  /**
   * Finds in this repository all the reminders related to the specified contribution that were set
   * by and for the specified user.
   * @param contributionId the unique identifier of a contribution.
   * @param userId the unique identifier of a user.
   * @return a list of {@link Reminder} instances. Empty if the user has set no reminders for the
   * contribution.
   */
  List<Reminder> findByContributionAndUserIds(final ContributionIdentifier contributionId,
      final String userId);

}
  