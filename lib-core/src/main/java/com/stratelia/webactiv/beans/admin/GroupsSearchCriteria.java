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

import java.util.HashMap;
import java.util.Map;

import static com.silverpeas.util.StringUtil.isDefined;

/**
 * A conjonction of criteria in the search of user groups.
 */
public class GroupsSearchCriteria implements SearchCriteria {

  private static final String GROUP_ID = "groupId";
  private static final String SUPERGROUP_ID = "parentId";
  private static final String ROLE_IDS = "roleIds";
  private static final String DOMAIN_IDS = "domainIds";
  private static final String INSTANCE_ID = "instanceId";
  private static final String ROOT_GROUP = "mustBeRoot";
  private static final String NAME = "name";
  private Map<String, Object> criteria = new HashMap<String, Object>();

  /**
   * The groups name must satisfy the specified pattern.
   * @param name a pattern on the group name.
   * @return itself.
   */
  @Override
  public GroupsSearchCriteria onName(String name) {
    if (isDefined(name)) {
      criteria.put(NAME, name);
    }
    return this;
  }

  /**
   * The groups of users must have the right to access the specified component instance.
   * @param instanceId the unique identifier of the component instance.
   * @return itself.
   */
  @Override
  public GroupsSearchCriteria onComponentInstanceId(String instanceId) {
    if (isDefined(instanceId)) {
      criteria.put(INSTANCE_ID, instanceId);
    }
    return this;
  }

  /**
   * The groups must play one of the specified role names.
   * @param roleIds the role names. Warning, theses aren't role unique identifiers but names and
   * they belong to a given component. So, with the roles, a criterion on the component instance
   * must be also defined.
   * @return itself.
   */
  @Override
  public GroupsSearchCriteria onRoleIds(String[] roleIds) {
    if (roleIds != null && roleIds.length > 0) {
      criteria.put(ROLE_IDS, roleIds);
    }
    return this;
  }

  /**
   * The groups must be one of the specified ones.
   * @param groupIds the unique identifiers of some groups.
   * @return itself.
   */
  @Override
  public GroupsSearchCriteria onGroupIds(String... groupIds) {
    if (groupIds != null && groupIds.length > 0) {
      criteria.put(GROUP_ID, groupIds);
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

  /**
   * The groups must belong to the specified domain.
   * @param domainId the unique identifier of the domain.
   * @return itself.
   */
  @Override
  public GroupsSearchCriteria onDomainId(String domainId) {
    if (isDefined(domainId)) {
      criteria.put(DOMAIN_IDS, new String[]{domainId});
    }
    return this;
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

  public boolean isCriterionOnRoleIdsSet() {
    return criteria.containsKey(ROLE_IDS);
  }

  public boolean isCriterionOnComponentInstanceIdSet() {
    return criteria.containsKey(INSTANCE_ID);
  }

  public boolean isCriterionOnSuperGroupIdSet() {
    return criteria.containsKey(SUPERGROUP_ID);
  }

  public boolean isCriterionOnGroupIdsSet() {
    return criteria.containsKey(GROUP_ID);
  }

  public boolean isCriterionOnDomainIdSet() {
    return criteria.containsKey(DOMAIN_IDS);
  }

  public boolean isCriterionOnMixedDomainIdSet() {
    String[] domainIds = (String[])criteria.get(DOMAIN_IDS);
    for (String domainId : domainIds) {
      if (domainId.equals(Domain.MIXED_DOMAIN_ID)) {
        return true;
      }
    }
    return false;
  }

  public boolean isCriterionOnNameSet() {
    return criteria.containsKey(NAME);
  }

  public boolean mustBeRoot() {
    return criteria.containsKey(ROOT_GROUP);
  }

  /**
   * Gets the conjonction on the role identifiers.
   *
   * @return an array with each element of the conjonction.
   */
  public String[] getCriterionOnRoleIds() {
    return (String[]) criteria.get(ROLE_IDS);
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
   * Gets the conjonction on the user identifiers.
   *
   * @return an array with each element of the conjonction.
   */
  public String getCriterionOnSuperGroupId() {
    return (String) criteria.get(SUPERGROUP_ID);
  }

  /**
   * Gets the conjonction on the group identifiers.
   *
   * @return an array with each element of the conjonction.
   */
  public String[] getCriterionOnGroupIds() {
    return (String[]) criteria.get(GROUP_ID);
  }

  /**
   * Gets the domain identifier, other than the mixed domain, onto which the groups must belong.
   *
   * @return the identifier of the domain (other than the mixed domain).
   */
  public String getCriterionOnDomainId() {
     String[] domainIds = (String[]) criteria.get(DOMAIN_IDS);
     if (domainIds != null) {
       if (domainIds.length == 2) {
         return domainIds[1];
       } else if (!domainIds[0].equals(Domain.MIXED_DOMAIN_ID)) {
         return domainIds[0];
       }
     }
     return null;
  }

  /**
   * Gets the pattern on the name the group or the user name must satisfy.
   *
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
    final GroupsSearchCriteria other = (GroupsSearchCriteria) obj;
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

  /**
   * Not relevent here. Does nothing.
   * @return itself.
   */
  @Override
  public SearchCriteria onUserIds(String... userIds) {
    return this;
  }
}
