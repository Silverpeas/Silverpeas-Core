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
package com.stratelia.webactiv.beans.admin;

import static com.silverpeas.util.StringUtil.isDefined;
import java.util.HashMap;
import java.util.Map;

/**
 * Criteria for searching users profiles or user groups.
 */
public class SearchCriteria {
  public static final String ANY_GROUP = "any";
  
  private static final String GROUP_ID = "groupId";
  private static final String USER_IDS = "userIds";
  private static final String ROLE_IDS = "roleIds";
  private static final String DOMAIN_ID = "domainId";
  private static final String INSTANCE_ID = "instanceId";
  private static final String NAME = "name";
  
  private Map<String, Object> criteria = new HashMap<String, Object>();

  public SearchCriteria onName(String name) {
    if (isDefined(name)) {
      criteria.put(NAME, name);
    }
    return this;
  }

  public SearchCriteria onComponentInstanceId(String instanceId) {
    if (isDefined(instanceId)) {
      criteria.put(INSTANCE_ID, instanceId);
    }
    return this;
  }

  public SearchCriteria onRoleIds(String[] roleIds) {
    if (roleIds != null && roleIds.length > 0) {
      criteria.put(ROLE_IDS, roleIds);
    }
    return this;
  }

  public SearchCriteria onGroupId(String group) {
    if (isDefined(group)) {
      criteria.put(GROUP_ID, group);
    }
    return this;
  }

  public SearchCriteria onDomainId(String domainId) {
    if (isDefined(domainId)) {
      criteria.put(DOMAIN_ID, domainId);
    }
    return this;
  }

  public SearchCriteria onUserIds(String[] userIds) {
    if (userIds != null && userIds.length > 0) {
      criteria.put(USER_IDS, userIds);
    }
    return this;
  }

  public boolean isCriterionOnRoleIdsSet() {
    return criteria.containsKey(ROLE_IDS);
  }

  public boolean isCriterionOnComponentInstanceIdSet() {
    return criteria.containsKey(INSTANCE_ID);
  }

  public boolean isCriterionOnUserIdsSet() {
    return criteria.containsKey(USER_IDS);
  }

  public boolean isCriterionOnGroupIdSet() {
    return criteria.containsKey(GROUP_ID);
  }

  public boolean isCriterionOnDomainIdSet() {
    return criteria.containsKey(DOMAIN_ID);
  }

  public boolean isCriterionOnNameSet() {
    return criteria.containsKey(NAME);
  }
  
  public String[] getCriterionOnRoleIds() {
    return (String[]) criteria.get(ROLE_IDS);
  }

  public String getCriterionOnComponentInstanceId() {
    return (String) criteria.get(INSTANCE_ID);
  }
  
  public String[] getCriterionOnUserIds() {
    return (String[]) criteria.get(USER_IDS);
  }

  public String getCriterionOnGroupId() {
    return (String) criteria.get(GROUP_ID);
  }

  public String getCriterionOnDomainId() {
    return (String) criteria.get(DOMAIN_ID);
  }

  public String getCriterionOnName() {
    return (String) criteria.get(NAME);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final SearchCriteria other = (SearchCriteria) obj;
    if (this.criteria != other.criteria &&
            (this.criteria == null || !this.criteria.equals(other.criteria))) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 53 * hash + (this.criteria != null ? this.criteria.hashCode() : 0);
    return hash;
  }
  
}
