/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.profile.web;

import static com.silverpeas.util.StringUtil.isDefined;
import com.stratelia.webactiv.beans.admin.SearchCriteria;
import com.stratelia.webactiv.beans.admin.UserDetail;

/**
 * A builder of search criteria on user details or on user groups.
 */
public class SearchCriteriaBuilder {

  private SearchCriteria searchCriteria;

  public static SearchCriteriaBuilder aSearchCriteria() {
    return new SearchCriteriaBuilder();
  }

  public SearchCriteriaBuilder withName(String name) {
    if (isDefined(name)) {
      String filterByName = name.replaceAll("\\*", "%");
      searchCriteria.onName(filterByName);
    }
    return this;
  }
  
  public SearchCriteriaBuilder withComponentInstanceId(String instanceId) {
    if (isDefined(instanceId)) {
      searchCriteria.onComponentInstanceId(instanceId);
    }
    return this;
  }
  
  public SearchCriteriaBuilder withRoles(String[] roleIds) {
    if (roleIds != null && roleIds.length > 0) {
      searchCriteria.onRoleIds(roleIds);
    }
    return this;
  }
  
  public SearchCriteriaBuilder withGroupId(String groupId) {
    if(isDefined(groupId)) {
      String group = groupId;
      if (groupId.equals(UserProfileResource.QUERY_ALL_GROUP)) {
        group = SearchCriteria.ANY_GROUP;
      }
      searchCriteria.onGroupId(group);
    }
    return this;
  }
  
  public SearchCriteriaBuilder withDomainId(String domainId, final UserDetail user) {
    if (user.isDomainRestricted()) {
      searchCriteria.onDomainId(user.getDomainId());
    } else if (isDefined(domainId) && Integer.valueOf(domainId) > 0) {
      searchCriteria.onDomainId(domainId);
    }
    return this;
  }
  
  public SearchCriteriaBuilder withUserIds(String[] userIds) {
    if (userIds != null && userIds.length > 0) {
      searchCriteria.onUserIds(userIds);
    }
    return this;
  }
  
  public SearchCriteria build() {
    return searchCriteria;
  }

  private SearchCriteriaBuilder() {
    searchCriteria = new SearchCriteria();
  }
}