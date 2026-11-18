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
package org.silverpeas.web.pdc.control;

import org.silverpeas.core.admin.component.model.ComponentSearchCriteria;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.kernel.util.StringUtil;

import java.io.Serializable;
import java.util.List;

/**
 * The scope of a search performed by a user: the component instances in which the contents are
 * looked for.
 * <p>
 * The component instances aren't kept between two searches: they are computed at each call of
 * {@link #getComponentInstanceIds()}, so that a component instance created, or made accessible to
 * the user, since a previous search of the same user session is taken into account.
 * </p>
 * @param criteria the criteria the component instances to search in have to satisfy.
 * @author mmoquillon
 */
public record SearchScope(ComponentSearchCriteria criteria) implements Serializable {

  /**
   * Defines the scope of a search of the specified user, optionally restricted to a space and to
   * some component instances.
   * @param user the user performing the search.
   * @param spaceId the identifier of the space in which to search. If not defined, the search
   * isn't restricted to a space.
   * @param componentInstanceIds the identifiers of the component instances in which to search. If
   * empty, the search isn't restricted to some component instances.
   * @return the scope of the search.
   */
  public static SearchScope of(UserDetail user, String spaceId,
      List<String> componentInstanceIds) {
    final ComponentSearchCriteria criteria = new ComponentSearchCriteria().onUser(user);
    if (StringUtil.isDefined(spaceId)) {
      criteria.onWorkspace(spaceId);
    }
    if (CollectionUtil.isNotEmpty(componentInstanceIds)) {
      criteria.onComponentInstances(componentInstanceIds);
    }
    return new SearchScope(criteria);
  }

  /**
   * Gets the component instances in which the search has to be performed. They are computed at
   * each call.
   * @return the identifiers of the searchable component instances satisfying the criteria of this
   * scope.
   */
  public List<String> getComponentInstanceIds() {
    return OrganizationController.get().getSearchableComponentsByCriteria(criteria);
  }
}
