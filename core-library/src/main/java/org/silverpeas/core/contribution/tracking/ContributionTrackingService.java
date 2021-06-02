/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

package org.silverpeas.core.contribution.tracking;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.ContributionDeletion;
import org.silverpeas.core.contribution.ContributionModification;
import org.silverpeas.core.contribution.ContributionModificationContextHandler;
import org.silverpeas.core.contribution.ContributionSettings;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.Instant;

/**
 * Service to track any modifications done in a contribution and for which a tracking is enabled.
 * @author mmoquillon
 */
@Service
public class ContributionTrackingService implements ContributionModification, ContributionDeletion {

  @Inject
  private ContributionModificationContextHandler modifHandler;

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public void update(final Contribution before, final Contribution after) {
    saveEventIfTrackingEnabled(after, () -> {
      TrackedActionType type = modifHandler.isMinorModification()
          .map(m -> m ? TrackedActionType.MINOR_UPDATE : TrackedActionType.MAJOR_UPDATE)
          .orElse(TrackedActionType.UPDATE);
      Instant dateTime = after.getLastUpdateDate().toInstant();
      TrackedAction action = new TrackedAction(type, dateTime, after.getLastUpdater());
      save(action, after.getIdentifier());
    });
  }

  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public void delete(final Contribution contribution) {
    saveEventIfTrackingEnabled(contribution, () -> {
      TrackedActionType type = TrackedActionType.DELETION;
      Instant dateTime = Instant.now();
      User deleter = User.getCurrentRequester();
      TrackedAction action = new TrackedAction(type, dateTime, deleter);
      save(action, contribution.getIdentifier());
    });
  }

  private void saveEventIfTrackingEnabled(Contribution contribution, Runnable eventSaving) {
    if (contribution.getClass().isAnnotationPresent(ModificationTracked.class)) {
      TrackedApplications trackedApps =
          ContributionSettings.getApplicationsTrackedForModifications();
      if (trackedApps.isTracked(contribution.getIdentifier().getComponentInstanceId())) {
        eventSaving.run();
      }
    }
  }

  public void save(TrackedAction action, ContributionIdentifier contributionId) {
    ContributionTrackingEvent event =
        new ContributionTrackingEvent(action, contributionId);
    event.save();
  }
}
