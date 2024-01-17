/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.reminder;

import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.persistence.datasource.repository.jpa.BasicJpaEntityRepository;
import org.silverpeas.core.persistence.datasource.repository.jpa.NamedParameters;

import javax.inject.Singleton;
import java.util.List;

/**
 * Default implementation of the JPA repository that stores the reminders for Silverpeas
 * @author mmoquillon
 */
@Repository
@Singleton
public class DefaultReminderRepository extends BasicJpaEntityRepository<Reminder>
    implements ReminderRepository {
  @Override
  public List<Reminder> findByUserId(final String id) {
    NamedParameters parameters = newNamedParameters().add("userId", id);
    return findByNamedQuery("remindersByUserId", parameters);
  }

  @Override
  public List<Reminder> findByContributionId(final ContributionIdentifier contributionId) {
    NamedParameters parameters = newNamedParameters().add("contributionId", contributionId);
    return findByNamedQuery("remindersByContributionId", parameters);
  }

  @Override
  public List<Reminder> findByContributionAndUserIds(final ContributionIdentifier contributionId,
      final String userId) {
    NamedParameters parameters =
        newNamedParameters().add("userId", userId).add("contributionId", contributionId);
    return findByNamedQuery("remindersByContributionIdAndUserId", parameters);
  }
}
  