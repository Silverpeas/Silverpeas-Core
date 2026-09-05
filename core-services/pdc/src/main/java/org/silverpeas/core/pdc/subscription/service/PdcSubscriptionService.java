/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.pdc.subscription.service;

import org.silverpeas.core.pdc.classification.Value;
import org.silverpeas.core.pdc.pdc.model.AxisValueCriterion;
import org.silverpeas.core.pdc.subscription.model.PdcSubscriptionPositionCriteria;

import java.util.List;
import java.util.Optional;

/**
 * The service dedicated to the position criteria on the PdC that can be aimed by a subscription.
 * <p>
 * The subscriptions themselves are taken in charge by the subscription API of Silverpeas: use
 * {@link org.silverpeas.core.subscription.SubscriptionService} to subscribe a user or a group of
 * users to position criteria, to unsubscribe them, or to know who is subscribed to a given set.
 * This service is only about the lifecycle of the position criteria and about the business rules
 * that are specific to the PdC.
 * </p>
 */
public interface PdcSubscriptionService {

  /**
   * Gets the position criteria on the PdC with the specified identifier. The returned resource is
   * fully valued: both its name and its positions are loaded from the data source.
   * @param id the unique identifier of position criteria on the PdC.
   * @return the position criteria on the PdC or nothing if there is no such a set in the data
   * source.
   */
  Optional<PdcSubscriptionPositionCriteria> getPositionCriteria(String id);

  /**
   * Gets all the position criteria on the PdC that have been defined in Silverpeas, whatever the
   * users or the groups of users that are subscribed to them.
   * @return a list of position criteria on the PdC.
   */
  List<PdcSubscriptionPositionCriteria> getAllPositionCriteria();

  /**
   * Creates a new position criteria on the PdC. Once created, users or groups of users can
   * subscribe to it with the subscription API of Silverpeas.
   * @param name the name of the position criteria.
   * @param positions the positions on the axis of the PdC.
   * @return the newly created position criteria, with its unique identifier valued.
   */
  PdcSubscriptionPositionCriteria createPositionCriteria(String name, List<AxisValueCriterion> positions);

  /**
   * Updates both the name and the positions of the specified position criteria on the PdC. The
   * subscriptions on that set are left untouched.
   * @param resource the position criteria to update.
   */
  void updatePositionCriteria(PdcSubscriptionPositionCriteria resource);

  /**
   * Deletes the position criteria on the PdC with the specified identifier as well as all the
   * subscriptions on it.
   * @param id the unique identifier of position criteria on the PdC.
   */
  void deletePositionCriteria(String id);

  /**
   * Checks if any position criteria on the PdC matches the classification provided and, if so,
   * notifies all the users that are subscribed to that set and that are allowed to access the
   * classified contribution. The users subscribed through a group of users is taken into account.
   * @param classifyValues the positions on which the contribution has been classified.
   * @param componentId the component instance into which the classification event occurred.
   * @param silverObjectid the contribution that has been classified.
   */
  void checkSubscriptions(List<? extends Value> classifyValues, String componentId,
      int silverObjectid);

  /**
   * Checks the deletion of a value of an axis of the PdC. All the position criteria referring the
   * deleted value are deleted, as well as the subscriptions on them, and all their subscribers are
   * notified.
   * @param axisId the axis to be checked
   * @param axisName the name of the axis
   * @param oldPath old path that would be removed soon
   * @param newPath new path. That will be places instead of old for this axis
   * @param pathInfo should contain PdcBm.getFullPath data structure
   */
  void checkValueOnDelete(int axisId, String axisName, List<String> oldPath,
      List<String> newPath, List<org.silverpeas.core.pdc.pdc.model.Value> pathInfo);

  /**
   * Checks the deletion of an axis of the PdC. All the position criteria referring the deleted
   * axis are deleted, as well as the subscriptions on them, and all their subscribers are
   * notified.
   * @param axisId the axis to be checked
   * @param axisName the name of the axis
   */
  void checkAxisOnDelete(int axisId, String axisName);
}
