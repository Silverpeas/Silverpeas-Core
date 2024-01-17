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

package org.silverpeas.core.contribution.publication.dao;

import org.silverpeas.core.util.StringUtil;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

/**
 * Class that allows to set distribution tree criteria for publications and nodes.
 * @author silveryocha
 */
public class DistributionTreeCriteria {
  private final String instanceId;
  private boolean checkVisibility = false;
  private String statusSubQuery = null;
  private Collection<String> instanceIdsToIgnore = null;

  /**
   * Initializes distribution tree criteria axed on the given component instance id.
   * @param instanceId identifier of the aimed component instance host.
   * @return an initialized {@link DistributionTreeCriteria} instance.
   */
  public static DistributionTreeCriteria onInstanceId(final String instanceId) {
    return new DistributionTreeCriteria( instanceId);
  }

  /**
   * Hidden constructor
   */
  private DistributionTreeCriteria(final String instanceId) {
    this.instanceId = instanceId;
  }

  /**
   * Copy constructor.
   * <p>
   *   Only provided for internal use.
   * </p>
   * @param toCopy instance to copy.
   */
  DistributionTreeCriteria(final DistributionTreeCriteria toCopy) {
    this.instanceId = toCopy.instanceId;
    this.checkVisibility = toCopy.checkVisibility;
    this.statusSubQuery = toCopy.statusSubQuery;
    this.instanceIdsToIgnore = toCopy.instanceIdsToIgnore != null
        ? new HashSet<>(toCopy.instanceIdsToIgnore)
        : null;
  }

  /**
   * Sets the visibility check.
   * @param checkVisibility true to verify the visibility, false otherwise.
   * @return itself.
   */
  public DistributionTreeCriteria withVisibilityCheck(final boolean checkVisibility) {
    this.checkVisibility = checkVisibility;
    return this;
  }

  /**
   * Sets manually the filtering on the status.
   * <p>
   *   No table aliases is used into final query, so table name MUST be used to avoid getting
   *   field confusion at SQL query execution.
   * </p>
   * @param statusSubQuery a string representing the SQL status filtering part.
   * @return itself.
   */
  public DistributionTreeCriteria withManualStatusFilter(final String statusSubQuery) {
    this.statusSubQuery = statusSubQuery;
    return this;
  }

  /**
   * Sets manually the filtering on the status.
   * @param instanceIdsToIgnore a collection of component instance identifier which MUST be ignored.
   * @return itself.
   */
  public DistributionTreeCriteria ignoringInstanceIds(final Collection<String> instanceIdsToIgnore) {
    this.instanceIdsToIgnore = new HashSet<>(instanceIdsToIgnore);
    return this;
  }

  /**
   * Gets the aimed component instance host.
   * @return a string representing a component instance.
   */
  String getInstanceId() {
    return instanceId;
  }

  /**
   * Gets the SQL part of status filter if any.
   * @return optional string representing an SQL select part.
   */
  Optional<String> getStatusSubQuery() {
    return ofNullable(statusSubQuery).filter(StringUtil::isDefined);
  }

  /**
   * Indicates if the visibility checking MUST be applied.
   * @return a boolean.
   */
  boolean visibilityCheckRequired() {
    return checkVisibility;
  }

  /**
   * Gets instance ids to ignore.
   * @return collection of string.
   */
  Collection<String> getInstanceIdsToIgnore() {
    return ofNullable(instanceIdsToIgnore).orElse(emptyList());
  }
}
