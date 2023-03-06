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
package org.silverpeas.core.admin.user.model;

import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;

/**
 * Criteria to use in a search of resources managed and exposed in Silverpeas (like user profiles or
 * user groups).
 */
public interface SearchCriteria {

  class Constants {

    private Constants() {
    }

    /**
     * The whatever value to be used as criterion value if you don't care of a given criterion.
     */
    public static final String[] ANY = new String[0];
  }

  /**
   * Appends a criterion on the component instance for which the search must be constrained to. The
   * properties of the resources to fetch have to satisfy this criterion.
   *
   * @param instanceId the unique identifier of the component instance.
   * @return the criteria enriched with a criterion on the component instance.
   */
  SearchCriteria onComponentInstanceId(String instanceId);

  /**
   * Appends a criterion on a given component instance's resource for which the search must be
   * constrained to. This criterion has a meaning only when coupled with the criterion on the
   * component instance. The properties of the resources to fetch have to satisfy both the criterion
   * on the component instance and this one.
   *
   * @param resourceId the unique identifier of the resource managed in the component instance. As
   * each resource is particular to a given Silverpeas component, the unique identifier is made up
   * of the resource type and of the resource identifier.
   * @return the criteria enriched with a criterion on the resource in the component instance.
   */
  SearchCriteria onResourceId(String resourceId);

  /**
   * Appends a criterion on the user domain for which the search must be constrained to. The
   * properties of the resources to fetch have to satisfy this criterion.
   * This criterion replaces any previous criterion on the user domains.
   * @param domainIds the unique identifier of the user domain.
   * @return the criteria enriched with a criterion on the user domain.
   */
  SearchCriteria onDomainIds(String... domainIds);

  /**
   * Appends a criterion on the user groups for which the search must be constrained to. The
   * properties of the resources to fetch have to satisfy this criterion.
   * This criterion replaces any previous criterion on the user groups.
   * @param groupIds the unique identifiers of the groups.
   * @return the criteria enriched with a criterion on the user groups.
   */
  SearchCriteria onGroupIds(String... groupIds);

  /**
   * Appends a criterion on the user access level for which the search must be constrained to. The
   * properties of the resources to fetch have to satisfy this criterion.
   * This criterion replaces any previous criterion on the user access levels.
   * @param accessLevels the access levels aimed.
   * @return the criteria enriched with a criterion on the user access level.
   */
  SearchCriteria onAccessLevels(UserAccessLevel... accessLevels);

  /**
   * Appends a criterion on the resources name for which the search must be constrained to. The name
   * of the resources to fetch have to satisfy this criterion.
   *
   * @param name a pattern on the name the resources to fetch must have.
   * @return the criteria enriched with a criterion on the resource name.
   */
  SearchCriteria onName(String name);

  /**
   * Appends a criterion on the user roles for which the search must be constrained to. The
   * properties of the resources to fetch have to satisfy this criterion.
   * This criterion replaces any previous criterion on the user roles.
   * <p>
   * This criterion is useless without any criterion on either the component instance or on both
   * the component instance and the resource for which the roles are defined.
   * </p>
   * @param roleNames the name of the user roles on which the criterion has to be built.
   * @return the criteria enriched with a criterion on the role names.
   */
  SearchCriteria onRoleNames(String... roleNames);

  /**
   * Specifies that each result item MUST match all specified roles if any.
   * <p>
   *   If no role criteria is set, this criteria has no effect.
   * </p>
   * @return the criteria enriched with a criterion on the role name clause.
   */
  SearchCriteria matchingAllRoleNames();

  /**
   * Appends a criteria on the user profiles for which the search must be constrained to. The
   * properties of the resources to fetch have to satisfy this criterion.
   * This criterion replaces any previous criterion on the user profiles.
   * @param userIds the user identifiers.
   * @return the criteria enriched with a criterion on the user identifiers.
   */
  SearchCriteria onUserIds(String... userIds);

  /**
   * Appends a criterion on the user profiles for which the search must be constrained to. The
   * properties of the resources to fetch have to satisfy this criterion.
   * <p>
   * One, and only one, domain id will be mandatory!
   * </p>
   * This criterion replaces any previous criterion on the user profiles.
   * @param userSpecificIds the user specific identifiers.
   * @return the criteria enriched with a criterion on the user identifiers.
   */
  SearchCriteria onUserSpecificIds(String... userSpecificIds);

  /**
   * Appends a criterion on the user states to be excluded in the search of users. The
   * properties of the resources to fetch have to satisfy this criterion. By default, the deleted
   * users are always excluded but not the removed users. Latter have to be explicitly excluded.
   * This criterion replaces any previous criterion on the user states.
   * @param userStates the user states that exclude users from the result.
   * @return the criteria enriched with a criterion on the user states.
   */
  SearchCriteria onUserStatesToExclude(UserState... userStates);

  /**
   * Forces to take into account removed users.
   * <p>
   *   This method has priority over the {@link #onUserStatesToExclude(UserState...)} one.
   *   It means that if {@link #includeRemovedUsers()} is called then {@link UserState#REMOVED}
   *   state is removed from result of {@link #onUserStatesToExclude(UserState...)} method call.
   * </p>
   * @return the criteria enriched with a criterion on the user states.
   */
  SearchCriteria includeRemovedUsers();

  /**
   * Appends a criterion on a resources pagination. The pagination is a mechanism to distribute the
   * resources to fetch in one or more pages of same size and to navigate among theses different
   * available pages.
   * Yet, this criterion is about the page of resources to fetch.
   * @param page the page of resources to fetch.
   * @return the criteria enriched with a criterion on the resources pagination.
   */
  SearchCriteria onPagination(final PaginationPage page);

  /**
   * Is this criteria empty?
   *
   * @return true if this object has no any criteria, false otherwise.
   */
  boolean isEmpty();
}
