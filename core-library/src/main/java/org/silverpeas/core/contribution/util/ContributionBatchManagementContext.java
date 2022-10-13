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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.util;

import org.silverpeas.core.ActionType;
import org.silverpeas.core.contribution.ContributionStatus;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.publication.model.Location;
import org.silverpeas.core.subscription.SubscriptionResource;

import java.util.ArrayList;
import java.util.List;

/**
 * This class permits to specify a context into which a batch of contribution has to be managed.
 * For example, it permits to define the context on the save action of batch modification of
 * publications.
 * @author silveryocha
 */
public class ContributionBatchManagementContext {
  private final List<ContributionContext> contributionContexts = new ArrayList<>();
  private ActionType entityPersistenceAction = ActionType.READ;

  /**
   * Initializes a context by specifying the contribution handled by this context.
   * @return a new instance of {@link ContributionBatchManagementContext}.
   */
  public static ContributionBatchManagementContext initialize() {
    return new ContributionBatchManagementContext();
  }

  /**
   * Hidden constructor.
   */
  private ContributionBatchManagementContext() {
  }


  /**
   * Setup the finest subscription resource linked to the entity that is handled.
   * <p>
   * This method can be called several times in order to constitute a list of
   * {@link SubscriptionResource} to handle.
   * </p>
   * @param contribution the concerned contribution.
   * @param contributionStatus the status of the concerned contribution.
   * @param location an optional location.
   * @param linkedSubscriptionResource the finest subscription resource (the resource on which
   * subscriptions are registered) linked to the entity that is handled.
   * @return itself.
   */
  public ContributionBatchManagementContext addContributionContext(final Contribution contribution,
      final ContributionStatus contributionStatus, final Location location,
      final SubscriptionResource linkedSubscriptionResource) {
    this.contributionContexts.add(
        new ContributionContext(contribution, contributionStatus, location,
            linkedSubscriptionResource));
    return this;
  }

  /**
   * Gets the contribution contexts.
   * @return list of {@link ContributionContext} instance.
   */
  public List<ContributionContext> getContributionContexts() {
    return contributionContexts;
  }

  /**
   * Sets the context of persistence action performed on the entity.
   * @param entityPersistenceAction the type of persistence action performed on the persisted
   * entity.
   * @return the instance of the current completed context.
   */
  public ContributionBatchManagementContext forPersistenceAction(
      ActionType entityPersistenceAction) {
    this.entityPersistenceAction = entityPersistenceAction;
    return this;
  }

  /**
   * Gets the persistence (or validation) action that is performed on the entity.
   * @return the persistence (or validation) action that is performed on the entity.
   */
  public ActionType getEntityPersistenceAction() {
    return entityPersistenceAction;
  }

  public static class ContributionContext {
    private final Contribution contribution;
    private final ContributionStatus contributionStatus;
    private final Location location;
    private final SubscriptionResource linkedSubscriptionResource;

    private ContributionContext(final Contribution contribution,
        final ContributionStatus contributionStatus, final Location location, final SubscriptionResource linkedSubscriptionResource) {
      this.contribution = contribution;
      this.contributionStatus = contributionStatus;
      this.location = location;
      this.linkedSubscriptionResource = linkedSubscriptionResource;
    }

    public Contribution getContribution() {
      return contribution;
    }

    public ContributionStatus getContributionStatus() {
      return contributionStatus;
    }

    public Location getLocation() {
      return location;
    }

    public SubscriptionResource getLinkedSubscriptionResource() {
      return linkedSubscriptionResource;
    }
  }
}
