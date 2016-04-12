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
package org.silverpeas.core.admin.user.model;

import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;

import java.util.HashMap;
import java.util.Map;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * A conjonction of criteria in the search of user details.
 */
public class UserDetailsSearchCriteria implements SearchCriteria {

  public static String[] ANY_GROUPS = ANY;

  private static final String USER_ACCESS_LEVELS = "userAccessLevels";
  private static final String USER_STATES_TO_EXCLUDE = "userStatesToExclude";
  private static final String GROUP_IDS = "groupId";
  private static final String USER_IDS = "userIds";
  private static final String ROLE_NAMES = "roleIds";
  private static final String DOMAIN_IDS = "domainIds";
  private static final String RESOURCE_ID = "resourceId";
  private static final String INSTANCE_ID = "instanceId";
  private static final String NAME = "name";
  private static final String PAGINATION = "pagination";
  private Map<String, Object> criteria = new HashMap<String, Object>();

  @Override
  public UserDetailsSearchCriteria onName(String name) {
    if (isDefined(name)) {
      criteria.put(NAME, name);
    }
    return this;
  }

  @Override
  public UserDetailsSearchCriteria onComponentInstanceId(String instanceId) {
    if (isDefined(instanceId)) {
      criteria.put(INSTANCE_ID, instanceId);
    }
    return this;
  }

  @Override
  public UserDetailsSearchCriteria onRoleNames(String[] roleIds) {
    if (roleIds != null && roleIds.length > 0) {
      criteria.put(ROLE_NAMES, roleIds);
    }
    return this;
  }

  @Override
  public UserDetailsSearchCriteria onGroupIds(String... groupIds) {
    criteria.put(GROUP_IDS, groupIds);
    return this;
  }

  @Override
  public UserDetailsSearchCriteria onDomainId(String domainId) {
    if (isDefined(domainId)) {
      criteria.put(DOMAIN_IDS, domainId);
    }
    return this;
  }

  @Override
  public UserDetailsSearchCriteria onAccessLevels(final UserAccessLevel... accessLevels) {
    criteria.put(USER_ACCESS_LEVELS, accessLevels);
    return this;
  }

  @Override
  public UserDetailsSearchCriteria onUserStatesToExclude(final UserState... userStates) {
    criteria.put(USER_STATES_TO_EXCLUDE, userStates);
    return null;
  }

  @Override
  public UserDetailsSearchCriteria onResourceId(String resourceId) {
    if (isDefined(resourceId)) {
      criteria.put(RESOURCE_ID, resourceId);
    }
    return this;
  }

  @Override
  public UserDetailsSearchCriteria onUserIds(String[] userIds) {
    if (userIds != null && userIds.length > 0) {
      criteria.put(USER_IDS, userIds);
    }
    return this;
  }

  public boolean isCriterionOnRoleNamesSet() {
    return criteria.containsKey(ROLE_NAMES);
  }

  public boolean isCriterionOnResourceIdSet() {
    return criteria.containsKey(RESOURCE_ID);
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

  public boolean isCriterionOnAccessLevelsSet() {
    return criteria.containsKey(USER_ACCESS_LEVELS);
  }

  public boolean isCriterionOnUserStatesToExcludeSet() {
    return criteria.containsKey(USER_STATES_TO_EXCLUDE);
  }

  public boolean isCriterionOnNameSet() {
    return criteria.containsKey(NAME);
  }

  public boolean isCriterionOnPaginationSet() {
    return criteria.containsKey(PAGINATION);
  }

  /**
   * Gets the disjonction on the role names.
   * @return an array with each element of the disjonction.
   */
  public String[] getCriterionOnRoleNames() {
    return (String[]) criteria.get(ROLE_NAMES);
  }

  /**
   * Gets the resource in the component instance the user or the group must have priviledge to access.
   * @return the unique identifier of the resource in a component instance.
   */
  public String getCriterionOnResourceId() {
    return (String) criteria.get(RESOURCE_ID);
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
   * Gets access level criterion.
   * @return the access level criterion.
   */
  public UserAccessLevel[] getCriterionOnAccessLevels() {
    return (UserAccessLevel[]) criteria.get(USER_ACCESS_LEVELS);
  }

  /**
   * Gets user states to exclude criterion.
   * @return the access level criterion.
   */
  public UserState[] getCriterionOnUserStatesToExclude() {
    return (UserState[]) criteria.get(USER_STATES_TO_EXCLUDE);
  }

  /**
   * Gets the pattern on the name the group or the user name must satisfy.
   * @return a pattern on the user or group name.
   */
  public String getCriterionOnName() {
    return (String) criteria.get(NAME);
  }

  /**
   * Gets the pagination page into which the groups to return has to be part.
   * @return the page in a pagination mechanism to fetch.
   */
  public PaginationPage getCriterionOnPagination() {
    return (PaginationPage) criteria.get(PAGINATION);
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
   * Useless as by default the criteria forms a conjunction.
   *
   * @return itself.
   */
  @Override
  public UserDetailsSearchCriteria and() {
    return this;
  }

  /**
   * Not supported. By default, the criteria form a conjunction.
   *
   * @return nothing, thrown an UnsupportedOperationException exception.
   */
  @Override
  public UserDetailsSearchCriteria or() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean isEmpty() {
    return criteria.isEmpty();
  }

  @Override
  public UserDetailsSearchCriteria onPagination(PaginationPage page) {
    criteria.put(PAGINATION, page);
    return this;
  }
}
