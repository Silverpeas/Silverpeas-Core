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
package org.silverpeas.core.admin.user.dao;

import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.model.GroupsSearchCriteria;
import org.silverpeas.core.admin.user.model.SearchCriteria;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * An implementation of the search criteria for user groups stored in a SQL data source and used by
 * the DAOs. By default, the criterion are linked together by a conjonction operator. Nevertheless,
 * you can explicitly specify it by using the GroupSearchCriteriaForDAO#and() method.
 */
public class GroupSearchCriteriaForDAO implements SearchCriteria {

  private final GroupsSearchCriteria criteria;
  private final Set<String> tables = new HashSet<>();
  private String[] userIds = null;

  private GroupSearchCriteriaForDAO(final GroupsSearchCriteria criteria) {
    this.criteria = criteria;
    tables.add("st_group");
  }

  /**
   * Constructs new criteria on the user groups to search in the data source.
   * @return new search criteria for DAOs.
   */
  public static GroupSearchCriteriaForDAO newCriteria() {
    return newCriteriaFrom(new GroupsSearchCriteria());
  }

  /**
   * Constructs new criteria on the user groups to search in the data source from the specified
   * criteria on the groups' properties.
   * @param criteria criteria on the groups' properties the search must satisfy.
   * @return new search criteria for DAOs.
   */
  public static GroupSearchCriteriaForDAO newCriteriaFrom(final GroupsSearchCriteria criteria) {
    return new GroupSearchCriteriaForDAO(criteria);
  }

  @Override
  public GroupSearchCriteriaForDAO onName(String name) {
    if (isDefined(name)) {
      this.criteria.onName(name);
    }
    return this;
  }

  public GroupSearchCriteriaForDAO clearOnName() {
    criteria.clearOnName();
    return this;
  }

  @Override
  public GroupSearchCriteriaForDAO onGroupIds(String... groupIds) {
    if (groupIds != Constants.ANY) {
      this.criteria.onGroupIds(groupIds);
    }
    return this;
  }

  @Override
  public GroupSearchCriteriaForDAO onDomainIds(String... domainIds) {
    this.criteria.onDomainIds(domainIds);
    return this;
  }

  @Override
  public GroupSearchCriteriaForDAO onAccessLevels(UserAccessLevel... accessLevels) {
    // Not handled for now
    return this;
  }

  @Override
  public SearchCriteria onUserStatesToExclude(final UserState... userStates) {
    if (userStates != null && userStates.length > 0) {
      this.criteria.onUserStatesToExclude(userStates);
      // It is only used in order to transport the criterion which will be given to the
      // UserSearchCriteriaForDAO when getting the number of users linked to the group
    }
    return this;
  }

  public GroupSearchCriteriaForDAO onMixedDomainOrOnDomainId(String domainId) {
    if (isDefined(domainId)) {
      this.criteria.onMixedDomainOrOnDomainId(domainId);
    }
    return this;
  }

  @Override
  public SearchCriteria onUserIds(String... userIds) {
    if (userIds != Constants.ANY) {
      tables.add("st_group_user_rel");
      this.userIds = Arrays.copyOf(userIds, userIds.length);
    }
    return this;
  }

  @Override
  public SearchCriteria onUserSpecificIds(final String... userSpecificIds) {
    return this;
  }

  @Override
  public SearchCriteria onRoleNames(String... roleIds) {
    if (roleIds != null && roleIds.length > 0) {
      tables.add("st_userrole_group_rel");
      this.criteria.onRoleNames(roleIds);
    }
    return this;
  }

  public SearchCriteria onSuperGroupId(String superGroupId) {
    if (isDefined(superGroupId)) {
      this.criteria.onSuperGroupId(superGroupId);
    }
    return this;
  }

  public SearchCriteria withChildren() {
    this.criteria.withChildren();
    return this;
  }

  public SearchCriteria onAsRootGroup() {
    this.criteria.onAsRootGroup();
    return this;
  }

  public JdbcSqlQuery toSQLQuery(String fields) {
    JdbcSqlQuery query = prepareJdbcSqlQuery(fields);

    if (criteria.isCriterionOnNameSet()) {
      query.and("lower(st_group.name) like lower(?)", criteria.getCriterionOnName());
    }

    if (criteria.isCriterionOnGroupIdsSet()) {
      List<Integer> groupIds = Arrays.stream(criteria.getCriterionOnGroupIds())
          .map(Integer::parseInt)
          .collect(Collectors.toList());
      query.and("st_group.id").in(groupIds);
    }

    if (criteria.isCriterionOnDomainIdSet()) {
      List<Integer> domains =
          getCriterionOnDomainIds().stream().map(Integer::parseInt).collect(Collectors.toList());
      query.and("st_group.domainId").in(domains);
    }

    if (userIds != null && userIds.length > 0) {
      List<Integer> ids =
          Arrays.stream(userIds).map(Integer::parseInt).collect(Collectors.toList());
      query.and("st_group.id = st_group_user_rel.groupId")
          .and("st_group_user_rel.userid").in(ids);
    }

    if (criteria.isCriterionOnRoleNamesSet()) {
      List<Integer> rolesIds = Arrays.stream(criteria.getCriterionOnRoleNames())
          .map(Integer::parseInt)
          .collect(Collectors.toList());
      query.and("st_group.id = st_userrole_group_rel.groupId")
          .and("st_userrole_group_rel.userRoleId").in(rolesIds);
    }

    setCriterionOnSuperGroup(query);

    finalizeJdbcSqlQuery(query);

    return query;
  }

  @Override
  public String toString() {
    return toSQLQuery("*").toString();
  }

  @Override
  public boolean isEmpty() {
    return criteria.isEmpty();
  }

  public String getCriterionOnName() {
    return criteria.getCriterionOnName();
  }

  public List<String> getCriterionOnDomainIds() {
    List<String> domainIds = new ArrayList<>();
    if (criteria.isCriterionOnDomainIdSet()) {
      domainIds.add(criteria.getCriterionOnDomainId());
    }
    if (criteria.isCriterionOnMixedDomainIdSet()) {
      domainIds.add(Domain.MIXED_DOMAIN_ID);
    }
    return domainIds;
  }

  public UserState[] getCriterionOnUserStatesToExclude() {
    return this.criteria.getCriterionOnUserStatesToExclude();
  }

  public boolean childrenRequired() {
    return criteria.childrenRequired();
  }

  @Override
  public SearchCriteria onComponentInstanceId(String instanceId) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public SearchCriteria onResourceId(String resourceId) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public SearchCriteria onPagination(PaginationPage page) {
    if (page != null) {
      this.criteria.onPagination(page);
    }
    return this;
  }

  public GroupSearchCriteriaForDAO clearPagination() {
    criteria.clearPagination();
    return this;
  }

  /**
   * Gets the criterion on the pagination page to fetch.
   *
   * @return a pagination page.
   */
  public PaginationPage getPagination() {
    return criteria.getCriterionOnPagination();
  }

  /**
   * Is the pagination criterion set?
   *
   * @return true if a criterion on the pagination about user groups is set, false otherwise.
   */
  public boolean isPaginationSet() {
    return criteria.isCriterionOnPaginationSet();
  }

  private void setCriterionOnSuperGroup(final JdbcSqlQuery query) {
    if (criteria.isCriterionOnSuperGroupIdSet() && criteria.mustBeRoot()) {
      query.and("(st_group.superGroupId = ? or st_group.superGroupId is null)",
          Integer.parseInt(criteria.getCriterionOnSuperGroupId()));
    } else if (criteria.isCriterionOnSuperGroupIdSet()) {
      query.and("st_group.superGroupId = ?",
          Integer.parseInt(criteria.getCriterionOnSuperGroupId()));
    } else if (criteria.mustBeRoot()) {
      query.and("st_group.superGroupId is null");
    }
  }

  private JdbcSqlQuery prepareJdbcSqlQuery(final String fields) {
    return JdbcSqlQuery.createSelect(fields)
        .from(tables.stream().collect(Collectors.joining(",")))
        .where("st_group.id = st_group.id");
  }

  private void finalizeJdbcSqlQuery(final JdbcSqlQuery query) {
    query.orderBy("st_group.name");

    if (criteria.isCriterionOnPaginationSet()) {
      PaginationPage page = criteria.getCriterionOnPagination();
      query.withPagination(page.asCriterion());
    }
  }
}
