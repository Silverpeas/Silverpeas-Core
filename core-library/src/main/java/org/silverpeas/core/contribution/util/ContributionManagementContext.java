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
package org.silverpeas.core.contribution.util;

import org.silverpeas.core.ActionType;
import org.silverpeas.core.contribution.ContributionStatus;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.model.Location;
import org.silverpeas.core.subscription.SubscriptionResource;

/**
 * This class permits to specify a context into which a contribution has to be managed.
 * For example, it permits to define the context on the save action of a WYSIWYG of a contribution
 * from kmelia component.
 * @author Yohann Chastagnier
 */
public class ContributionManagementContext {
  private final ContributionIdentifier contributionId;
  private SubscriptionResource linkedSubscriptionResource;
  private Location location;
  private ActionType entityPersistenceAction = ActionType.READ;
  private ContributionStatus entityStatusBeforePersistAction = ContributionStatus.UNKNOWN;
  private ContributionStatus entityStatusAfterPersistAction = ContributionStatus.UNKNOWN;

  /**
   * Initializes a context by specifying the contribution handled by this context.
   * @param id a component instance identifier as string.
   * @return a new instance of {@link ContributionManagementContext}.
   */
  public static ContributionManagementContext atComponentInstanceId(final String id) {
    return new ContributionManagementContext(ContributionIdentifier.from(id, id, "UNKNOWN"));
  }

  /**
   * Initializes a context by specifying the contribution handled by this context.
   * @param contribution a {@link Contribution} instance.
   * @return a new instance of {@link ContributionManagementContext}.
   */
  public static ContributionManagementContext on(final Contribution contribution) {
    return new ContributionManagementContext(contribution.getIdentifier());
  }

  /**
   * Hidden constructor.
   */
  private ContributionManagementContext(final ContributionIdentifier contributionId) {
    this.contributionId = contributionId;
  }


  /**
   * Setup the finest subscription resource linked to the entity that is handled.
   * @param linkedSubscriptionResource the finest subscription resource (the resource on which
   * subscriptions are registered) linked to the entity that is handled.
   * @return itself.
   */
  public ContributionManagementContext aboutSubscriptionResource(
      final SubscriptionResource linkedSubscriptionResource) {
    this.linkedSubscriptionResource = linkedSubscriptionResource;
    return this;
  }

  /**
   * Setup the location at which the subscription resource is aimed.
   * @param location a {@link Location} instance from which MUST contribution is handled.
   * @return itself.
   */
  public ContributionManagementContext atLocation(final Location location) {
    this.location = location;
    return this;
  }

  /**
   * Gets the {@link ContributionIdentifier} of the managed contribution.
   * @return the handled contribution identifier handled into the context.
   */
  public ContributionIdentifier getContributionId() {
    return contributionId;
  }

  /**
   * Gets the finest subscription resource linked to the current managed entity.
   * @return the finest subscription resource linked to the current managed entity.
   */
  public SubscriptionResource getLinkedSubscriptionResource() {
    return linkedSubscriptionResource;
  }

  /**
   * Sets the context of persistence action performed on the entity.
   * @param entityStatusBeforePersistenceAction the status of the entity before its persistence (or
   * validation).
   * @param entityPersistenceAction the type of persistence action performed on the persisted
   * entity.
   * @param entityStatusAfterPersistenceAction the status of the entity after a successful persist
   * operation (or validation).
   * @return the instance of the current completed context.
   */
  public ContributionManagementContext forPersistenceAction(
      ContributionStatus entityStatusBeforePersistenceAction, ActionType entityPersistenceAction,
      ContributionStatus entityStatusAfterPersistenceAction) {
    this.entityStatusBeforePersistAction = entityStatusBeforePersistenceAction;
    this.entityPersistenceAction = entityPersistenceAction;
    this.entityStatusAfterPersistAction = entityStatusAfterPersistenceAction;
    return this;
  }

  /**
   * Gets the persistence (or validation) action that is performed on the entity.
   * @return the persistence (or validation) action that is performed on the entity.
   */
  public ActionType getEntityPersistenceAction() {
    return entityPersistenceAction;
  }

  /**
   * Gets the status of the entity before the persistence (or validation) action is performed.
   * @return the status of the entity before the persistence (or validation) action is performed.
   */
  public ContributionStatus getEntityStatusBeforePersistAction() {
    return entityStatusBeforePersistAction;
  }

  /**
   * Gets the status of the entity after the persistence (or validation) action is performed.
   * @return the status of the entity after the persistence (or validation) action is performed.
   */
  public ContributionStatus getEntityStatusAfterPersistAction() {
    return entityStatusAfterPersistAction;
  }

  /**
   * Gets the optional location of the contribution from which it is managed.
   * @return a {@link Location} if any, null otherwise.
   */
  public Location getLocation() {
    return location;
  }
}
