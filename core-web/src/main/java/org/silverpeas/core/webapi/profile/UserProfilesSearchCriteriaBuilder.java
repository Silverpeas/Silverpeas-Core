/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.webapi.profile;

import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.model.UserDetailsSearchCriteria;
import org.silverpeas.core.util.ArrayUtil;

import java.util.Arrays;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * A builder of search criteria on user profiles.
 */
public class UserProfilesSearchCriteriaBuilder {

  private UserDetailsSearchCriteria searchCriteria;
  private boolean includeRemoved = false;

  /**
   * Constructs a new builder of criteria for searching user profiles.
   * @return a {@link UserProfilesSearchCriteriaBuilder} instance.
   */
  public static UserProfilesSearchCriteriaBuilder aSearchCriteria() {
    return new UserProfilesSearchCriteriaBuilder();
  }

  /**
   * The users to find should satisfy the specified name. The wildcard character '*' is supported
   * to mean any characters.
   * @param name the name of a user. It can be the firstname or the lastname of the users to search.
   * @return itself.
   */
  public UserProfilesSearchCriteriaBuilder withName(String name) {
    if (isDefined(name)) {
      String filterByName = name.replaceAll("\\*", "%");
      searchCriteria.onName(filterByName);
    }
    return this;
  }

  /**
   * The users to find have an access right on the specified component instance.
   * @param instanceId the unique identifier of a component instance.
   * @return
   */
  public UserProfilesSearchCriteriaBuilder withComponentInstanceId(String instanceId) {
    if (isDefined(instanceId)) {
      searchCriteria.onComponentInstanceId(instanceId);
    }
    return this;
  }

  /**
   * The users to find have an access right on the specified resource. The component instance should
   * be set with the method {@link UserProfilesSearchCriteriaBuilder#withComponentInstanceId(String)}
   * in order to refer exactly the resource.
   * @param resourceId the unique identifier of a resource in a given component instance.
   * @return itself.
   */
  public UserProfilesSearchCriteriaBuilder withResourceId(String resourceId) {
    if (isDefined(resourceId)) {
      searchCriteria.onResourceId(resourceId);
    }
    return this;
  }

  /**
   * The users to find have to play at least one of the specified roles.
   * @param roleIds one or more identifiers of roles.
   * @return itself.
   */
  public UserProfilesSearchCriteriaBuilder withRoles(String... roleIds) {
    if (roleIds != null && roleIds.length > 0) {
      searchCriteria.onRoleNames(roleIds);
    }
    return this;
  }

  /**
   * The users to find have to be at least in one of the specified user groups.
   * @param groupIds one or more unique identifiers of user groups.
   * @return itself.
   */
  public UserProfilesSearchCriteriaBuilder withGroupIds(String... groupIds) {
    if(ArrayUtil.isNotEmpty(groupIds)) {
      if (ArrayUtil.contains(groupIds, UserProfileResource.QUERY_ALL_GROUPS)) {
        searchCriteria.onGroupIds(UserDetailsSearchCriteria.ANY_GROUPS);
      } else {
        searchCriteria.onGroupIds(groupIds);
      }
    }
    return this;
  }

  /**
   * The users to find have to be at least in one of the specified user directory domains.
   * @param domainIds one or more unique identifiers of domains.
   * @return itself.
   */
  public UserProfilesSearchCriteriaBuilder withDomainIds(String... domainIds) {
    searchCriteria.onDomainIds(domainIds);
    return this;
  }

  /**
   * The users to find must have at least one of the specified access levels
   * @param accessLevels one or more access levels.
   * @return itself.
   */
  public UserProfilesSearchCriteriaBuilder withAccessLevels(UserAccessLevel... accessLevels) {
    if (accessLevels != null && accessLevels.length > 0) {
      searchCriteria.onAccessLevels(accessLevels);
    }
    return this;
  }

  /**
   * The users to find are those with the specified unique identifiers of users.
   * @param userIds one ore more unique identifiers of users;
   * @return itself.
   */
  public UserProfilesSearchCriteriaBuilder withUserIds(String... userIds) {
    if (userIds != null && userIds.length > 0) {
      searchCriteria.onUserIds(userIds);
    }
    return this;
  }

  /**
   * The users to find are those with the specified domain specific identifiers of users.
   * @param userSpecificIds one ore more domain specific identifiers of users;
   * @return itself.
   */
  public UserProfilesSearchCriteriaBuilder withUserSpecificIds(String... userSpecificIds) {
    if (userSpecificIds != null && userSpecificIds.length > 0) {
      searchCriteria.onUserSpecificIds(userSpecificIds);
    }
    return this;
  }

  /**
   * Excludes from the search scope the users with the specified states. The deleted
   * users are always excluded as to be expected. Be cautious, by default the removed users are here
   * also excluded but they can be included explicitly by invoking the method
   * {@link UserProfilesSearchCriteriaBuilder#includeAlsoRemovedUsers()}.
   * @param userStates the states the users to find must not have.
   * @return itself.
   */
  public UserProfilesSearchCriteriaBuilder withUserStatesToExclude(UserState... userStates) {
    if (userStates != null && userStates.length > 0) {
      searchCriteria.onUserStatesToExclude(userStates);
    }
    return this;
  }

  /**
   * Includes also among the users to find those they are removed.
   * @return itself.
   */
  public UserProfilesSearchCriteriaBuilder includeAlsoRemovedUsers() {
    this.includeRemoved = true;
    return this;
  }

  /**
   * The search results are paginated and only the specified page should be returned from the
   * search results.
   * @param page the page corresponding to a paginated search results.
   * @return itself.
   */
  public UserProfilesSearchCriteriaBuilder withPaginationPage(final PaginationPage page) {
    searchCriteria.onPagination(page);
    return this;
  }

  /**
   * Builds the criteria according to the build properties that have been set.
   * @return a {@link UserDetailsSearchCriteria} instance.
   */
  public UserDetailsSearchCriteria build() {
    if (!includeRemoved) {
      UserState[] allExcludedStates;
      if (searchCriteria.isCriterionOnUserStatesToExcludeSet()) {
        UserState[] excludedStates = searchCriteria.getCriterionOnUserStatesToExclude();
        allExcludedStates = Arrays.copyOf(excludedStates, excludedStates.length + 1);
        allExcludedStates[excludedStates.length] = UserState.REMOVED;
      } else {
        allExcludedStates = new UserState[] {UserState.REMOVED};
      }
      searchCriteria.onUserStatesToExclude(allExcludedStates);
    }
    return searchCriteria;
  }

  private UserProfilesSearchCriteriaBuilder() {
    searchCriteria = new UserDetailsSearchCriteria();
  }
}