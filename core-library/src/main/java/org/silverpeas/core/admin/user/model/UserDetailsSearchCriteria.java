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
package org.silverpeas.core.admin.user.model;

import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.kernel.util.StringUtil;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;
import static org.silverpeas.kernel.util.StringUtil.isDefined;

/**
 * A conjunction of criteria in the search of user details.
 */
@SuppressWarnings("UnusedReturnValue")
public class UserDetailsSearchCriteria extends AbstractSearchCriteria {

  public static final String[] ANY_GROUPS = Constants.ANY;
  private static final String USER_ACCESS_LEVELS = "userAccessLevels";

  private static final String GROUP_IDS = "groupId";
  private static final String USER_IDS = "userIds";
  private static final String USER_SPECIFIC_IDS = "userSpecificIds";
  private static final String DOMAIN_IDS = "domainIds";
  private static final String RESOURCE_ID = "resourceId";
  private static final String INSTANCE_ID = "instanceId";
  private static final String NAME = "name";
  private static final String FIRST_NAME = "firstName";
  private static final String LAST_NAME = "lastName";
  private static final String PAGINATION = "pagination";
  private static final String ROLE_GROUPS = "groupIdsInRole";

  @Override
  public UserDetailsSearchCriteria onName(String name) {
    if (isDefined(name)) {
      criteria.put(NAME, name);
    }
    return this;
  }

  /**
   * Appends a criterion on the first name of the users for which the search must be constrained to.
   * The users to fetch have to satisfy this criterion.
   * @param firstName a pattern on the first name of the users to fetch.
   * @return the criteria enriched with a criterion on the user first name.
   */
  public SearchCriteria onFirstName(final String firstName) {
    if (isDefined(firstName)) {
      criteria.put(FIRST_NAME, firstName);
    }
    return this;
  }

  /**
   * Appends a criterion on the last name of the users for which the search must be constrained to.
   * The users to fetch have to satisfy this criterion.
   * @param lastName a pattern on the last name of the users to fetch.
   * @return the criteria enriched with a criterion on the user last name.
   */
  public SearchCriteria onLastName(final String lastName) {
    if (isDefined(lastName)) {
      criteria.put(LAST_NAME, lastName);
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
  public UserDetailsSearchCriteria onRoleNames(String... roleIds) {
    return (UserDetailsSearchCriteria) super.onRoleNames(roleIds);
  }

  /**
   * The users to search must be in specified groups playing the roles that are defined by the
   * criterion on the role names. If this criterion isn't set, then only the users playing directly
   * the roles will be searched and not those that are part of a group playing the roles. It is
   * strongly recommended to specify the groups along with their own children groups.
   * @param groupIdsByRoles map containing one or more unique identifiers of groups playing the
   * role that is specified in the criterion on the role names by role name.
   * @return itself.
   */
  public UserDetailsSearchCriteria withGroupsByRoles(Map<String, Set<String>> groupIdsByRoles) {
    if (!groupIdsByRoles.isEmpty()) {
      criteria.put(ROLE_GROUPS, groupIdsByRoles);
    }
    return this;
  }

  @Override
  public UserDetailsSearchCriteria onGroupIds(String... groupIds) {
    if (isNotEmpty(groupIds)) {
      criteria.put(GROUP_IDS,
          Arrays.stream(groupIds).filter(StringUtil::isDefined).toArray(String[]::new));
    } else if (groupIds == Constants.ANY) {
      criteria.put(GROUP_IDS, groupIds);
    }
    return this;
  }

  public UserDetailsSearchCriteria onDomainIds(String... domainIds) {
    if (isNotEmpty(domainIds)) {
      criteria.put(DOMAIN_IDS,
          Arrays.stream(domainIds).filter(StringUtil::isDefined).toArray(String[]::new));
    }
    return this;
  }

  @Override
  public UserDetailsSearchCriteria onAccessLevels(final UserAccessLevel... accessLevels) {
    if (isNotEmpty(accessLevels)) {
      criteria.put(USER_ACCESS_LEVELS, accessLevels);
    }
    return this;
  }

  @Override
  public UserDetailsSearchCriteria onUserStatesToExclude(final UserState... userStates) {
    return (UserDetailsSearchCriteria) super.onUserStatesToExclude(userStates);
  }

  @Override
  public UserDetailsSearchCriteria includeRemovedUsers() {
    return (UserDetailsSearchCriteria) super.includeRemovedUsers();
  }

  @Override
  public UserDetailsSearchCriteria onResourceId(String resourceId) {
    if (isDefined(resourceId)) {
      criteria.put(RESOURCE_ID, resourceId);
    }
    return this;
  }

  @Override
  public UserDetailsSearchCriteria onUserIds(String... userIds) {
    if (isNotEmpty(userIds)) {
      criteria.put(USER_IDS,
          Arrays.stream(userIds).filter(StringUtil::isDefined).toArray(String[]::new));
    }
    return this;
  }

  @Override
  public SearchCriteria onUserSpecificIds(final String... userSpecificIds) {
    if (isNotEmpty(userSpecificIds)) {
      criteria.put(USER_SPECIFIC_IDS,
          Arrays.stream(userSpecificIds).filter(StringUtil::isDefined).toArray(String[]::new));
    }
    return this;
  }

  @Override
  public UserDetailsSearchCriteria onPagination(PaginationPage page) {
    if (page != null) {
      criteria.put(PAGINATION, page);
    }
    return this;
  }

  @SuppressWarnings("unchecked")
  public boolean isCriterionOnGroupsByRolesSet() {
    return criteria.containsKey(ROLE_GROUPS) &&
        !((Map<String, Set<String>>) criteria.get(ROLE_GROUPS)).isEmpty();
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

  public boolean isCriterionOnUserSpecificIdsSet() {
    return isCriterionOnDomainIdSet() && criteria.containsKey(USER_SPECIFIC_IDS);
  }

  public boolean isCriterionOnGroupIdsSet() {
    return criteria.containsKey(GROUP_IDS) && isNotEmpty((String[]) criteria.get(GROUP_IDS));
  }

  public boolean isCriterionOnAnyGroupSet() {
    return criteria.containsKey(GROUP_IDS) && criteria.get(GROUP_IDS) == Constants.ANY;
  }

  public boolean isCriterionOnDomainIdSet() {
    return criteria.containsKey(DOMAIN_IDS) && isNotEmpty((String[]) criteria.get(DOMAIN_IDS));
  }

  public boolean isCriterionOnAccessLevelsSet() {
    return criteria.containsKey(USER_ACCESS_LEVELS);
  }

  public boolean isCriterionOnNameSet() {
    return criteria.containsKey(NAME);
  }

  public boolean isCriterionOnFirstNameSet() {
    return criteria.containsKey(FIRST_NAME);
  }

  public boolean isCriterionOnLastNameSet() {
    return criteria.containsKey(LAST_NAME);
  }

  public boolean isCriterionOnPaginationSet() {
    return criteria.containsKey(PAGINATION);
  }

  /**
   * Gets the mapping of groups playing the roles given by the criterion on the role
   * names by role name.
   * @return a map with set of group ids by role name.
   */
  @SuppressWarnings("unchecked")
  public Map<String, Set<String>> getCriterionOnGroupsByRoles() {
    return (Map<String, Set<String>>) criteria.get(ROLE_GROUPS);
  }

  /**
   * Gets the resource in the component instance the user or the group must have privilege to access.
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
   * Gets the disjunction on the user identifiers.
   * @return an array with each element of the disjunction.
   */
  public String[] getCriterionOnUserIds() {
    return (String[]) criteria.get(USER_IDS);
  }

  /**
   * Gets the disjunction on the user specific identifiers.
   * @return an array with each element of the disjunction.
   */
  public String[] getCriterionOnUserSpecificIds() {
    return (String[]) criteria.get(USER_SPECIFIC_IDS);
  }

  /**
   * Gets the disjunction on the group identifiers.
   * @return an array with each element of the disjunction.
   */
  public String[] getCriterionOnGroupIds() {
    return (String[]) criteria.get(GROUP_IDS);
  }

  /**
   * Gets the domain identifier.
   * @return the domain identifier.
   */
  public String[] getCriterionOnDomainIds() {
    return (String[]) criteria.get(DOMAIN_IDS);
  }

  /**
   * Gets access level criterion.
   * @return the access level criterion.
   */
  public UserAccessLevel[] getCriterionOnAccessLevels() {
    return (UserAccessLevel[]) criteria.get(USER_ACCESS_LEVELS);
  }

  /**
   * Gets the pattern on the name the group or the user name must satisfy.
   * @return a pattern on the user or group name.
   */
  public String getCriterionOnName() {
    return (String) criteria.get(NAME);
  }

  /**
   * Gets the pattern on the name the user first name must satisfy.
   * @return a pattern on the user first name.
   */
  public String getCriterionOnFirstName() {
    return (String) criteria.get(FIRST_NAME);
  }

  /**
   * Gets the pattern on the name the user last name must satisfy.
   * @return a pattern on the user last name.
   */
  public String getCriterionOnLastName() {
    return (String) criteria.get(LAST_NAME);
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
    return Objects.equals(this.criteria, other.criteria);
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 53 * hash + this.criteria.hashCode();
    return hash;
  }

  @Override
  public boolean isEmpty() {
    return criteria.isEmpty();
  }
}
