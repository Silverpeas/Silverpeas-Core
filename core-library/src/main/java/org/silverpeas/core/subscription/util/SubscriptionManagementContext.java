/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.subscription.util;

import org.silverpeas.core.subscription.SubscriptionResource;
import org.silverpeas.core.contribution.ContributionStatus;
import org.silverpeas.core.ActionType;

/**
 * This class permits to specify a context into which the subscriptions have to be managed.
 * For example, it permits to define the context on the save action of a WYSIWYG of contribution
 * from kmelia component.
 * @author Yohann Chastagnier
 */
public class SubscriptionManagementContext {
  private final SubscriptionResource linkedSubscriptionResource;
  private final String entityId;
  private ActionType entityPersistenceAction = ActionType.READ;
  private ContributionStatus entityStatusBeforePersistAction = ContributionStatus.UNKNOWN;
  private ContributionStatus entityStatusAfterPersistAction = ContributionStatus.UNKNOWN;

  /**
   * Initializes a context by specifying the finest subscription resource linked to the entity that
   * is handled.
   * @param linkedSubscriptionResource the finest subscription resource (the resource on which
   * subscriptions are registered) linked to the entity that is handled.
   * @param andEntityId the identifier of the entity on which the persistence (or validation)
   * action
   * is performed.
   * @return a new instance of {@link SubscriptionManagementContext}
   * initialized with the given subscription resource.
   */
  public static SubscriptionManagementContext on(SubscriptionResource linkedSubscriptionResource,
      String andEntityId) {
    return new SubscriptionManagementContext(linkedSubscriptionResource, andEntityId);
  }

  /**
   * Hidden constructor.
   * @param linkedSubscriptionResource
   * @param entityId
   */
  private SubscriptionManagementContext(final SubscriptionResource linkedSubscriptionResource,
      final String entityId) {
    this.linkedSubscriptionResource = linkedSubscriptionResource;
    this.entityId = entityId;
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
  public SubscriptionManagementContext forPersistenceAction(
      ContributionStatus entityStatusBeforePersistenceAction, ActionType entityPersistenceAction,
      ContributionStatus entityStatusAfterPersistenceAction) {
    this.entityStatusBeforePersistAction = entityStatusBeforePersistenceAction;
    this.entityPersistenceAction = entityPersistenceAction;
    this.entityStatusAfterPersistAction = entityStatusAfterPersistenceAction;
    return this;
  }

  /**
   * Gets the identifier of the entity on which the persistence (or validation) action is
   * performed.
   * @return the identifier of the entity on which the persistence (or validation) action is
   * performed.
   */
  public String getEntityId() {
    return entityId;
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
}
