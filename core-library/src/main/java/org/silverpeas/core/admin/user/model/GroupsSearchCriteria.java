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

import org.apache.commons.lang3.ArrayUtils;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.kernel.util.StringUtil;

import java.util.Arrays;
import java.util.Objects;

import static org.silverpeas.core.util.ArrayUtil.isNotEmpty;
import static org.silverpeas.kernel.util.StringUtil.isDefined;

/**
 * A conjunction of criteria in the search of user groups.
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public class GroupsSearchCriteria extends AbstractSearchCriteria {

  private static final String USER_ACCESS_LEVELS = "userAccessLevels";
  private static final String GROUP_ID = "groupId";
  private static final String USER_ID = "userId";
  private static final String SUPERGROUP_ID = "parentId";
  private static final String PROFILE_IDS = "profileIds";
  private static final String DOMAIN_IDS = "domainIds";
  private static final String INSTANCE_ID = "instanceId";
  private static final String WITH_CHILDREN = "withChildren";
  private static final String RESOURCE_ID = "resourceId";
  private static final String ROOT_GROUP = "mustBeRoot";
  private static final String NAME = "name";
  private static final String PAGINATION = "pagination";

  /**
   * Constructs a search criteria on the groups for which all the groups have to be got.
   */
  public GroupsSearchCriteria() {
    criteria.put(GROUP_ID, Constants.ANY);
  }

  @Override
  public GroupsSearchCriteria onName(String name) {
    if (isDefined(name)) {
      criteria.put(NAME, name);
    }
    return this;
  }

  public GroupsSearchCriteria clearOnName() {
    criteria.remove(NAME);
    return this;
  }

  @Override
  public GroupsSearchCriteria onComponentInstanceId(String instanceId) {
    if (isDefined(instanceId)) {
      criteria.put(INSTANCE_ID, instanceId);
    }
    return this;
  }

  /**
   * Indicates to service to retrieve all the children (of matching groups) which are matching
   * the criteria.
   * @return the criteria enriched with the directive of getting also the children.
   */
  public GroupsSearchCriteria withChildren() {
    criteria.put(WITH_CHILDREN, true);
    return this;
  }

  @Override
  public GroupsSearchCriteria onRoleNames(String... roleIds) {
    return (GroupsSearchCriteria) super.onRoleNames(roleIds);
  }

  public GroupsSearchCriteria onProfileIds(String... profileIds) {
    if (isNotEmpty(profileIds)) {
      criteria.put(PROFILE_IDS,
          Arrays.stream(profileIds).filter(StringUtil::isDefined).toArray(String[]::new));
    }
    return this;
  }

  @Override
  public GroupsSearchCriteria onGroupIds(String... groupIds) {
    if (isNotEmpty(groupIds)) {
      criteria.put(GROUP_ID,
          Arrays.stream(groupIds).filter(StringUtil::isDefined).toArray(String[]::new));
    }
    return this;
  }

  /**
   * The groups must be root ones.
   * @return itself.
   */
  public GroupsSearchCriteria onAsRootGroup() {
    criteria.put(ROOT_GROUP, true);
    return this;
  }

  /**
   * The groups must have as parent the specified super group.
   * @param superGroupId the unique identifier of the super group.
   * @return itself.
   */
  public GroupsSearchCriteria onSuperGroupId(String superGroupId) {
    if (isDefined(superGroupId)) {
      criteria.put(SUPERGROUP_ID, superGroupId);
    }
    return this;
  }

  @Override
  public GroupsSearchCriteria onDomainIds(String... domainIds) {
    if (isNotEmpty(domainIds)) {
      criteria.put(DOMAIN_IDS,
          Arrays.stream(domainIds).filter(StringUtil::isDefined).toArray(String[]::new));
    }
    return this;
  }

  @Override
  public GroupsSearchCriteria onAccessLevels(final UserAccessLevel... accessLevels) {
    if (isNotEmpty(accessLevels)) {
      criteria.put(USER_ACCESS_LEVELS, accessLevels);
    }
    return this;
  }

  @Override
  public GroupsSearchCriteria onUserStatesToExclude(final UserState... userStates) {
    return (GroupsSearchCriteria) super.onUserStatesToExclude(userStates);
  }

  @Override
  public GroupsSearchCriteria includeRemovedUsers() {
    return (GroupsSearchCriteria) super.includeRemovedUsers();
  }

  /**
   * The groups must belong to either the mixed domain or the specified one.
   * @param domainId the unique identifier of a domain.
   * @return itself.
   */
  public GroupsSearchCriteria onMixedDomainOrOnDomainId(String domainId) {
    if (isDefined(domainId)) {
      criteria.put(DOMAIN_IDS, new String[]{Domain.MIXED_DOMAIN_ID, domainId});
    } else {
      criteria.put(DOMAIN_IDS, new String[]{Domain.MIXED_DOMAIN_ID});
    }
    return this;
  }

  public boolean isCriterionOnProfileIdsSet() {
    return criteria.containsKey(PROFILE_IDS);
  }

  public boolean isCriterionOnResourceIdSet() {
    return criteria.containsKey(RESOURCE_ID);
  }

  public boolean isCriterionOnComponentInstanceIdSet() {
    return criteria.containsKey(INSTANCE_ID);
  }

  public boolean isCriterionOnSuperGroupIdSet() {
    return criteria.containsKey(SUPERGROUP_ID);
  }

  public boolean isCriterionOnGroupIdsSet() {
    return criteria.containsKey(GROUP_ID)
        && ArrayUtils.isNotEmpty((String[]) criteria.get(GROUP_ID));
  }

  public boolean isCriterionOnDomainIdSet() {
    return criteria.containsKey(DOMAIN_IDS) && isNotEmpty((String[]) criteria.get(DOMAIN_IDS));
  }

  public boolean isCriterionOnAccessLevelsSet() {
    return criteria.containsKey(USER_ACCESS_LEVELS);
  }

  public boolean isCriterionOnMixedDomainIdSet() {
    if (criteria.containsKey(DOMAIN_IDS)) {
      String[] domainIds = (String[]) criteria.get(DOMAIN_IDS);
      return Arrays.asList(domainIds).contains(Domain.MIXED_DOMAIN_ID);
    }
    return false;
  }

  public boolean isCriterionOnUserIdsSet() {
    return criteria.containsKey(USER_ID);
  }

  public boolean isCriterionOnNameSet() {
    return criteria.containsKey(NAME);
  }

  public boolean isCriterionOnPaginationSet() {
    return criteria.containsKey(PAGINATION);
  }

  public boolean childrenRequired() {
    return criteria.containsKey(WITH_CHILDREN);
  }

  public boolean mustBeRoot() {
    return criteria.containsKey(ROOT_GROUP);
  }

  /**
   * Gets the conjunction on the profile ids.
   *
   * @return an array with each element of the conjunction.
   */
  public String[] getCriterionOnProfileIds() {
    return (String[]) criteria.get(PROFILE_IDS);
  }

  @Override
  public boolean mustMatchAllRoles() {
    return isCriterionOnProfileIdsSet() && super.mustMatchAllRoles();
  }

  /**
   * Gets the resource for which the user or the group must have access rights.
   *
   * @return the unique identifier of the resource in a component instance.
   */
  public String getCriterionOnResourceId() {
    return (String) criteria.get(RESOURCE_ID);
  }

  /**
   * Gets the component instance the user or the group must belongs to.
   *
   * @return the unique identifier of the component instance.
   */
  public String getCriterionOnComponentInstanceId() {
    return (String) criteria.get(INSTANCE_ID);
  }

  /**
   * Gets the conjunction on the super group identifiers.
   *
   * @return an array with each element of the conjunction.
   */
  public String getCriterionOnSuperGroupId() {
    return (String) criteria.get(SUPERGROUP_ID);
  }

  /**
   * Gets the conjunction on the user identifiers.
   * @return an array with each element of the conjunction.
   */
  public String[] getCriterionOnUserIds() {
    return (String[]) criteria.get(USER_ID);
  }

  /**
   * Gets the conjunction on the group identifiers.
   *
   * @return an array with each element of the conjunction.
   */
  public String[] getCriterionOnGroupIds() {
    return (String[]) criteria.get(GROUP_ID);
  }

  /**
   * Gets the domain identifier onto which the groups must belong. It can include the mixed domain.
   *
   * @return the identifier of the domain.
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
   *
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
    final GroupsSearchCriteria other = (GroupsSearchCriteria) obj;
    return Objects.equals(this.criteria, other.criteria);
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 53 * hash + (this.criteria != null ? this.criteria.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean isEmpty() {
    return criteria.isEmpty();
  }

  @Override
  public void accept(SearchCriteriaVisitor visitor) throws SilverpeasRuntimeException {
    visitor.visit(this);
  }

  /**
   * Not relevent here. Does nothing.
   * @return itself.
   */
  @Override
  public SearchCriteria onUserIds(String... userIds) {
    if (isNotEmpty(userIds)) {
      criteria.put(USER_ID,
          Arrays.stream(userIds).filter(StringUtil::isDefined).toArray(String[]::new));
    }
    return this;
  }

  @Override
  public SearchCriteria onUserSpecificIds(final String... userSpecificIds) {
    return this;
  }

  @Override
  public SearchCriteria onResourceId(String resourceId) {
    if (isDefined(resourceId)) {
      criteria.put(RESOURCE_ID, resourceId);
    }
    return this;
  }

  @Override
  public SearchCriteria onPagination(PaginationPage page) {
    if (page != null) {
      criteria.put(PAGINATION, page);
    }
    return this;
  }

  public SearchCriteria clearPagination() {
    criteria.remove(PAGINATION);
    return this;
  }
}
