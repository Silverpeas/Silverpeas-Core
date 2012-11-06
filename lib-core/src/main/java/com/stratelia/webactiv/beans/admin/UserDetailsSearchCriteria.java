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
package com.stratelia.webactiv.beans.admin;

import java.util.HashMap;
import java.util.Map;

import static com.silverpeas.util.StringUtil.isDefined;

/**
 * A conjonction of criteria in the search of user details.
 */
public class UserDetailsSearchCriteria implements SearchCriteria {

  public static String[] ANY_GROUPS = ANY;

  private static final String GROUP_IDS = "groupId";
  private static final String USER_IDS = "userIds";
  private static final String ROLE_IDS = "roleIds";
  private static final String DOMAIN_IDS = "domainIds";
  private static final String INSTANCE_ID = "instanceId";
  private static final String NAME = "name";
  private Map<String, Object> criteria = new HashMap<String, Object>();

  /**
   * The users fistname or lastname must matchs the specified pattern on the name.
   * @param name a pattern on the name.
   * @return itself.
   */
  @Override
  public UserDetailsSearchCriteria onName(String name) {
    if (isDefined(name)) {
      criteria.put(NAME, name);
    }
    return this;
  }

  /**
   * The users must have access the specified component instance.
   * @param instanceId the unique identifier of the component instance.
   * @return itself.
   */
  @Override
  public UserDetailsSearchCriteria onComponentInstanceId(String instanceId) {
    if (isDefined(instanceId)) {
      criteria.put(INSTANCE_ID, instanceId);
    }
    return this;
  }

  /**
   * The users must play at least one of the specified roles.
   * @param roleIds the unique identifiers of the roles.
   * @return itself.
   */
  @Override
  public UserDetailsSearchCriteria onRoleIds(String[] roleIds) {
    if (roleIds != null && roleIds.length > 0) {
      criteria.put(ROLE_IDS, roleIds);
    }
    return this;
  }

  /**
   * The users must belong at least to one of the specified groups.
   * @param groupIds the unique identifiers of the groups.
   * @return itself.
   */
  @Override
  public UserDetailsSearchCriteria onGroupIds(String... groupIds) {
    criteria.put(GROUP_IDS, groupIds);
    return this;
  }

  /**
   * The users must belong to the specified user domain.
   * @param domainId the unique identifier of the user domain.
   * @return itself.
   */
  @Override
  public UserDetailsSearchCriteria onDomainId(String domainId) {
    if (isDefined(domainId)) {
      criteria.put(DOMAIN_IDS, domainId);
    }
    return this;
  }

  /**
   * The users must be one of the specified ones.
   * @param userIds the unique identifiers of the users to fetch.
   * @return itself.
   */
  @Override
  public UserDetailsSearchCriteria onUserIds(String[] userIds) {
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

  public boolean isCriterionOnGroupIdsSet() {
    return criteria.containsKey(GROUP_IDS);
  }

  public boolean isCriterionOnDomainIdSet() {
    return criteria.containsKey(DOMAIN_IDS);
  }

  public boolean isCriterionOnNameSet() {
    return criteria.containsKey(NAME);
  }

  /**
   * Gets the disjonction on the role identifiers.
   * @return an array with each element of the disjonction.
   */
  public String[] getCriterionOnRoleIds() {
    return (String[]) criteria.get(ROLE_IDS);
  }

  /**
   * Gets the component instance the user or the group must belongs to.
   * @return the unique identifier of the component instance.
   */
  public String getCriterionOnComponentInstanceId() {
    return (String) criteria.get(INSTANCE_ID);
  }

  /**
   * Gets the disjonction on the user identifiers.
   * @return an array with each element of the disjonction.
   */
  public String[] getCriterionOnUserIds() {
    return (String[]) criteria.get(USER_IDS);
  }

  /**
   * Gets the disjonction on the group identifiers.
   * @return an array with each element of the disjonction.
   */
  public String[] getCriterionOnGroupIds() {
    return (String[]) criteria.get(GROUP_IDS);
  }

  /**
   * Gets the domain identifier.
   * @return the domain identifier.
   */
  public String getCriterionOnDomainId() {
    return (String) criteria.get(DOMAIN_IDS);
  }

  /**
   * Gets the pattern on the name the group or the user name must satisfy.
   * @return a pattern on the user or group name.
   */
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
    final UserDetailsSearchCriteria other = (UserDetailsSearchCriteria) obj;
    if (this.criteria != other.criteria
            && (this.criteria == null || !this.criteria.equals(other.criteria))) {
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

  /**
   * Useless as by default the criteria forms a conjonction.
   *
   * @return itself.
   */
  @Override
  public SearchCriteria and() {
    return this;
  }

  /**
   * Not supported. By default, the criteria form a conjonction.
   *
   * @return nothing, thrown an UnsupportedOperationException exception.
   */
  @Override
  public SearchCriteria or() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean isEmpty() {
    return criteria.isEmpty();
  }
}
