/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.core.webapi.profile;

import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.user.model.UserDetailsSearchCriteria;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * A builder of search criteria on user profiles.
 */
public class UserProfilesSearchCriteriaBuilder {

  private UserDetailsSearchCriteria searchCriteria;

  public static UserProfilesSearchCriteriaBuilder aSearchCriteria() {
    return new UserProfilesSearchCriteriaBuilder();
  }

  public UserProfilesSearchCriteriaBuilder withName(String name) {
    if (isDefined(name)) {
      String filterByName = name.replaceAll("\\*", "%");
      searchCriteria.onName(filterByName);
    }
    return this;
  }

  public UserProfilesSearchCriteriaBuilder withComponentInstanceId(String instanceId) {
    if (isDefined(instanceId)) {
      searchCriteria.onComponentInstanceId(instanceId);
    }
    return this;
  }

  public UserProfilesSearchCriteriaBuilder withResourceId(String resourceId) {
    if (isDefined(resourceId)) {
      searchCriteria.onResourceId(resourceId);
    }
    return this;
  }

  public UserProfilesSearchCriteriaBuilder withRoles(String[] roleIds) {
    if (roleIds != null && roleIds.length > 0) {
      searchCriteria.onRoleNames(roleIds);
    }
    return this;
  }

  public UserProfilesSearchCriteriaBuilder withGroupId(String groupId) {
    if(isDefined(groupId)) {
      if (groupId.equals(UserProfileResource.QUERY_ALL_GROUPS)) {
        searchCriteria.onGroupIds(UserDetailsSearchCriteria.ANY_GROUPS);
      } else {
        searchCriteria.onGroupIds(groupId);
      }
    }
    return this;
  }

  public UserProfilesSearchCriteriaBuilder withDomainId(String domainId) {
    if (isDefined(domainId) && Integer.valueOf(domainId) >= 0) {
      searchCriteria.onDomainId(domainId);
    }
    return this;
  }

  public UserProfilesSearchCriteriaBuilder withAccessLevels(UserAccessLevel[] accessLevels) {
    if (accessLevels != null && accessLevels.length > 0) {
      searchCriteria.onAccessLevels(accessLevels);
    }
    return this;
  }

  public UserProfilesSearchCriteriaBuilder withUserIds(String[] userIds) {
    if (userIds != null && userIds.length > 0) {
      searchCriteria.onUserIds(userIds);
    }
    return this;
  }

  public UserProfilesSearchCriteriaBuilder withUserStatesToExclude(UserState[] userStates) {
    if (userStates != null && userStates.length > 0) {
      searchCriteria.onUserStatesToExclude(userStates);
    }
    return this;
  }

  public UserProfilesSearchCriteriaBuilder withPaginationPage(final PaginationPage page) {
    searchCriteria.onPagination(page);
    return this;
  }

  public UserDetailsSearchCriteria build() {
    return searchCriteria;
  }

  private UserProfilesSearchCriteriaBuilder() {
    searchCriteria = new UserDetailsSearchCriteria();
  }
}