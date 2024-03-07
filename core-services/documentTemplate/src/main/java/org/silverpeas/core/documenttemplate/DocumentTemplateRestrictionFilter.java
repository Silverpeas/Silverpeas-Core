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

package org.silverpeas.core.documenttemplate;

import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.util.MemoizedSupplier;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.silverpeas.kernel.util.StringUtil.isDefined;

/**
 * This filter is in charge of filtering list of {@link DocumentTemplate}.
 * @author silveryocha
 */
public class DocumentTemplateRestrictionFilter {

  private final MemoizedSupplier<Set<String>> spaceIdsFromInstanceId = new MemoizedSupplier<>(
      () -> OrganizationController.get()
          .getPathToComponent(this.instanceId)
          .stream()
          .map(SpaceInstLight::getId)
          .collect(toSet()));
  private String instanceId;

  /**
   * The filter to invoke on a stream of {@link DocumentTemplate}.
   * @param documentTemplate a {@link DocumentTemplate} instance.
   * @return true if the document template MUST be kept, false otherwise.
   */
  public boolean applyOn(final DocumentTemplate documentTemplate) {
    final List<String> restrictedToSpaceIds = documentTemplate.getRestrictedToSpaceIds();
    return restrictedToSpaceIds.isEmpty() ||
        restrictedToSpaceIds.stream().anyMatch(getSpaceIds()::contains);
  }

  /**
   * Indicates to the filter the instance context into which it is used.
   * <p>
   *   This context will be taken into account when filtering a list of document template
   *   according to theirs restrictions.
   * </p>
   * @param instanceId identifier of a component instance.
   * @return the completed filter instance.
   */
  public DocumentTemplateRestrictionFilter setInstanceId(final String instanceId) {
    this.instanceId = instanceId;
    return this;
  }

  private Set<String> getSpaceIds() {
    return isDefined(instanceId) ? spaceIdsFromInstanceId.get() : Set.of();
  }
}
