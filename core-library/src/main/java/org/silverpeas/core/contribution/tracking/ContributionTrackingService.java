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
import org.silverpeas.core.contribution.ContributionCreation;
import org.silverpeas.core.contribution.ContributionDeletion;
import org.silverpeas.core.contribution.ContributionModification;
import org.silverpeas.core.contribution.ContributionModificationContextHandler;
import org.silverpeas.core.contribution.ContributionMove;
import org.silverpeas.core.contribution.ContributionSettings;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.Instant;
import java.util.stream.Stream;

/**
 * Service to track any modifications done in a contribution and for which a tracking is enabled.
 * Only the modifications on the contributions annotated with the annotation
 * {@link ModificationTracked} and that are managed by an application indicated as tracked are taken
 * in charge by the service. Applications are indicated as tracked in the configuration file
 * <code>org/silverpeas/contribution/settings/contribution.properties</code>.
 * @author mmoquillon
 */
@Service
public class ContributionTrackingService
    implements ContributionModification, ContributionDeletion, ContributionCreation,
    ContributionMove {

  static final String OUTER_MOVE_CONTEXT = "Move from %s to %s";
  static final String INNER_MOVE_CONTEXT = "Move between two nodes within %s";

  @Inject
  private ContributionModificationContextHandler modifHandler;

  /**
   * Saves the event on the update of the contribution. It is expected the update doesn't imply
   * a move of the contribution into another component instance. The requester (the user behind the
   * update) is taken as the author of the modification. In the case the modification is done
   * through a batch process (like a workflow one for example), then the updater set in the
   * contribution is taken as the author. Otherwise, it is the system user that is taken.
   * @param before the contribution before the modification.
   * @param after the contribution after the modification.
   */
  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public void update(final Contribution before, final Contribution after) {
    runIfTrackingEnabled(() -> {
      TrackedActionType type = modifHandler.isMinorModification()
          .map(m -> Boolean.TRUE.equals(m) ?
              TrackedActionType.MINOR_UPDATE :
              TrackedActionType.MAJOR_UPDATE)
          .orElse(TrackedActionType.UPDATE);
      Instant dateTime = Instant.now();
      User updater = User.getCurrentRequester();
      if (updater == null) {
        updater = after.getLastUpdater();
        if (updater == null) {
          updater = User.getSystemUser();
        }
      }
      TrackedAction action = new TrackedAction(type, dateTime, updater);
      save(action, before.getIdentifier(), "");
    }, before, after);
  }

  /**
   * Saves the event on the deletion of the specified contribution. The requester (the user
   * behind the deletion) is taken as the author of the modification. In the case the modification
   * is done through a batch process (like a workflow one for example), then the system user is
   * taken as the author.
   * @param contribution the contribution that was deleted.
   */
  @Override
  @Transactional(Transactional.TxType.MANDATORY)
  public void delete(final Contribution contribution) {
    runIfTrackingEnabled(() -> {
      TrackedActionType type = TrackedActionType.DELETION;
      Instant dateTime = Instant.now();
      User deleter = User.getCurrentRequester();
      if (deleter == null) {
        deleter = User.getSystemUser();
      }
      TrackedAction action = new TrackedAction(type, dateTime, deleter);
      save(action, contribution.getIdentifier(), "");
    }, contribution);
  }

  /**
   * Saves the event on the creation of the specified contribution. The requester (the user
   * behind the creation) is taken as the author of the modification. In the case the modification
   * is done through a batch process (like a workflow one for example), then the creator set in the
   * contribution is taken as the author. Otherwise, it is the system user that is taken.
   * @param contribution the contribution that was created.
   */
  @Override
  public void create(final Contribution contribution) {
    runIfTrackingEnabled(() -> {
      TrackedActionType type = TrackedActionType.CREATION;
      Instant dateTime = Instant.now();
      User creator = User.getCurrentRequester();
      if (creator == null) {
        creator = contribution.getCreator();
        if (creator == null) {
          creator = User.getSystemUser();
        }
      }
      TrackedAction action = new TrackedAction(type, dateTime, creator);
      save(action, contribution.getIdentifier(), "");
    }, contribution);
  }

  /**
   * Saves the event on the move of the contribution from a location to another one. The
   * requester (the user behind the mive) is taken as the author of the modification. In the case
   * the modification is done through a batch process (like a workflow one for example), then the
   * system user is taken as the author. The locations can be either inner of a component instance
   * and in this case the move is an inner move (see {@link TrackedActionType#INNER_MOVE}) or each
   * of them in a different component instance and in this case the move is an outer move
   * (see {@link TrackedActionType#OUTER_MOVE}).
   * <p>
   * The event is saved if and only if at least one of the component instance implied by the move is
   * tracked (indicated in the
   * <code>org/silverpeas/contribution/settings/contribution.properties</code> configuration file).
   * Whatever, the identifier of the contribution, in the saved event, will refer the contribution
   * before the move but, in the case of an outer move, additional information will be given about
   * the source and the destination of the move in the textual context of the event.
   * </p>
   * @param before the contribution before the move and from which the source location can be
   * figuring out.
   * @param after the contribution after the move and from which the destination location can be
   * figuring out.
   */
  @Override
  public void move(final Contribution before, final Contribution after) {
    runIfTrackingEnabled(() -> {
      TrackedActionType type;
      String context;
      if (isMovingInSameApplication(before, after)) {
        type = TrackedActionType.INNER_MOVE;
        context =
            String.format(INNER_MOVE_CONTEXT, before.getIdentifier().getComponentInstanceId());
      } else {
        type = TrackedActionType.OUTER_MOVE;
        context = String.format(OUTER_MOVE_CONTEXT, before.getIdentifier().getComponentInstanceId(),
            after.getIdentifier().getComponentInstanceId());
      }
      Instant dateTime = Instant.now();
      User mover = User.getCurrentRequester();
      if (mover == null) {
        mover = User.getSystemUser();
      }
      TrackedAction action = new TrackedAction(type, dateTime, mover);
      save(action, before.getIdentifier(), context);
    }, before, after);
  }

  private void runIfTrackingEnabled(Runnable eventSaving, Contribution... contributions) {
    if (contributions[0].getClass().isAnnotationPresent(ModificationTracked.class)) {
      TrackedApplications trackedApps =
          ContributionSettings.getApplicationsTrackedForModifications();
      boolean anAppIsTracked = Stream.of(contributions)
          .map(c -> c.getIdentifier().getComponentInstanceId())
          .anyMatch(trackedApps::isTracked);
      if (anAppIsTracked) {
        eventSaving.run();
      }
    }
  }

  public void save(TrackedAction action, ContributionIdentifier contributionId, String context) {
    ContributionTrackingEvent event =
        new ContributionTrackingEvent(action, contributionId).setContext(context);
    event.save();
  }

  private boolean isMovingInSameApplication(Contribution before, Contribution after) {
    return before.getIdentifier()
        .getComponentInstanceId()
        .equals(after.getIdentifier().getComponentInstanceId());
  }
}
