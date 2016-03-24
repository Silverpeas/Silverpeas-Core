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

import org.silverpeas.core.admin.user.model.GroupsSearchCriteria;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.user.model.UserDetailsSearchCriteria;
import org.silverpeas.core.admin.user.constant.UserState;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * A builder of search criteria on user groups.
 */
public class UserGroupsSearchCriteriaBuilder {

  private GroupsSearchCriteria searchCriteria;
  private String domainId = null;
  private boolean withMixedDomain = false;

  public static UserGroupsSearchCriteriaBuilder aSearchCriteria() {
    return new UserGroupsSearchCriteriaBuilder();
  }

  public UserGroupsSearchCriteriaBuilder withName(String name) {
    if (isDefined(name)) {
      String filterByName = name.replaceAll("\\*", "%");
      searchCriteria.onName(filterByName);
    }
    return this;
  }

  public UserGroupsSearchCriteriaBuilder withComponentInstanceId(String instanceId) {
    if (isDefined(instanceId)) {
      searchCriteria.onComponentInstanceId(instanceId);
    }
    return this;
  }

  public UserGroupsSearchCriteriaBuilder withResourceId(String resourceId) {
    if (isDefined(resourceId)) {
      searchCriteria.onResourceId(resourceId);
    }
    return this;
  }

  public UserGroupsSearchCriteriaBuilder withRoles(String[] roleIds) {
    if (roleIds != null && roleIds.length > 0) {
      searchCriteria.onRoleNames(roleIds);
    }
    return this;
  }

  public UserGroupsSearchCriteriaBuilder withGroupId(String groupId) {
    if(isDefined(groupId)) {
      if (groupId.equals(UserProfileResource.QUERY_ALL_GROUPS)) {
        searchCriteria.onGroupIds(UserDetailsSearchCriteria.ANY_GROUPS);
      } else {
        searchCriteria.onGroupIds(groupId);
      }
    }
    return this;
  }

  public UserGroupsSearchCriteriaBuilder withGroupIds(String[] groupIds) {
    if (groupIds != null && groupIds.length > 0) {
      searchCriteria.onGroupIds(groupIds);
    }
    return this;
  }

  public UserGroupsSearchCriteriaBuilder withSuperGroupId(String groupId) {
    if (isDefined(groupId)) {
      searchCriteria.onSuperGroupId(groupId);
    }
    return this;
  }

  public UserGroupsSearchCriteriaBuilder withRootGroupSet() {
    searchCriteria.onAsRootGroup();
    return this;
  }

  public UserGroupsSearchCriteriaBuilder withDomainId(String domainId) {
    this.domainId = domainId;
    return this;
  }

  public UserGroupsSearchCriteriaBuilder withMixedDomainId() {
    this.withMixedDomain = true;
    return this;
  }

  public UserGroupsSearchCriteriaBuilder withUserIds(String[] userIds) {
    if (userIds != null && userIds.length > 0) {
      searchCriteria.onUserIds(userIds);
    }
    return this;
  }

  public UserGroupsSearchCriteriaBuilder withUserStatesToExclude(UserState[] userStates) {
    if (userStates != null && userStates.length > 0) {
      searchCriteria.onUserStatesToExclude(userStates);
    }
    return this;
  }

  public UserGroupsSearchCriteriaBuilder withPaginationPage(final PaginationPage page) {
    searchCriteria.onPagination(page);
    return this;
  }

  public GroupsSearchCriteria build() {
    if (withMixedDomain) {
      searchCriteria.onMixedDomainOrOnDomainId(domainId);
    } else if (isDefined(domainId)) {
      searchCriteria.onDomainId(domainId);
    }
    return searchCriteria;
  }

  private UserGroupsSearchCriteriaBuilder() {
    searchCriteria = new GroupsSearchCriteria();
  }
}