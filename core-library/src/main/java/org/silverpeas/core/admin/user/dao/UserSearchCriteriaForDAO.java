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
package org.silverpeas.core.admin.user.dao;

import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.model.SearchCriteria;
import org.silverpeas.core.admin.user.model.UserDetailsSearchCriteria;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * An implementation of the search criteria for user details stored in a SQL data source and used by
 * the DAOs. By default, the criterion are linked together by a conjonction operator. Nevertheless,
 * you can explicitly specify it by using the UserSearchCriteriaForDAO#and() method.
 */
public class UserSearchCriteriaForDAO implements SearchCriteria {

  private static final String ORDERING = "lastName, firstName";
  private static final String NOT_SUPPORTED_YET = "Not supported yet.";
  private static final String ST_USER = "st_user";
  private final UserDetailsSearchCriteria criteria;
  private Set<String> tables = new HashSet<>();

  private UserSearchCriteriaForDAO(final UserDetailsSearchCriteria criteria) {
    this.criteria = criteria;
    tables.add(ST_USER);
  }

  public static UserSearchCriteriaForDAO newCriteria() {
    return new UserSearchCriteriaForDAO(new UserDetailsSearchCriteria());
  }

  public static UserSearchCriteriaForDAO newCriteriaFrom(final UserDetailsSearchCriteria criteria) {
    return new UserSearchCriteriaForDAO(criteria);
  }

  @Override
  public UserSearchCriteriaForDAO and() {
    criteria.and();
    return this;
  }

  @Override
  public UserSearchCriteriaForDAO or() {
    criteria.or();
    return this;
  }

  @Override
  public UserSearchCriteriaForDAO onName(String name) {
    if (isDefined(name)) {
      criteria.onName(name);
    }
    return this;
  }

  /**
   * Appends a criterion on the first name of the users for which the search must be constrained to.
   * The users to fetch have to satisfy this criterion.
   * @param firstName a pattern on the first name of the users to fetch.
   * @return the criteria enriched with a criterion on the user first name.
   */
  public UserSearchCriteriaForDAO onFirstName(final String firstName) {
    if (isDefined(firstName)) {
      criteria.onFirstName(firstName);
    }
    return this;
  }

  /**
   * Appends a criterion on the last name of the users for which the search must be constrained to.
   * The users to fetch have to satisfy this criterion.
   * @param lastName a pattern on the last name of the users to fetch.
   * @return the criteria enriched with a criterion on the user last name.
   */
  public UserSearchCriteriaForDAO onLastName(final String lastName) {
    if (isDefined(lastName)) {
      criteria.onLastName(lastName);
    }
    return this;
  }

  @Override
  public UserSearchCriteriaForDAO onGroupIds(String... groupIds) {
    if (groupIds != ANY) {
      tables.add("st_group_user_rel");
      criteria.onGroupIds(groupIds);
    }
    return this;
  }

  @Override
  public UserSearchCriteriaForDAO onAccessLevels(UserAccessLevel... accessLevels) {
    if (accessLevels != null && accessLevels.length > 0) {
      criteria.onAccessLevels(accessLevels);
    }
    return this;
  }

  @Override
  public UserSearchCriteriaForDAO onDomainId(String domainId) {
    // all users that are part of the specified domain or that have administration priviledges
    // (the administrators should be visible by anyone in order to be contacted)
    if (isDefined(domainId)) {
      criteria.onDomainId(domainId);
    }
    return this;
  }

  @Override
  public UserSearchCriteriaForDAO onUserIds(String... userIds) {
    if (userIds != ANY) {
      criteria.onUserIds(userIds);
    }
    return this;
  }

  @Override
  public UserSearchCriteriaForDAO onUserStatesToExclude(final UserState... userStates) {
    criteria.onUserStatesToExclude(userStates);
    return this;
  }

  JdbcSqlQuery toSQLQuery(String fields) {
    JdbcSqlQuery query = prepareJdbcSqlQuery(fields);

    if (criteria.isCriterionOnAccessLevelsSet()) {
      List<String> codes = Arrays.stream(criteria.getCriterionOnAccessLevels())
          .map(UserAccessLevel::getCode)
          .collect(Collectors.toList());
      query.and("st_user.accessLevel").in(codes);
    }

    if (criteria.isCriterionOnDomainIdSet()) {
      query.and("st_user.domainId = ?", Integer.parseInt(criteria.getCriterionOnDomainId()));
    }

    if (criteria.isCriterionOnFirstNameSet()) {
      String normalizedName =
          criteria.getCriterionOnFirstName().replaceAll("'", "''").replaceAll("\\*", "%");
      query.and("lower(st_user.firstName) like lower(?)", normalizedName);
    }

    if (criteria.isCriterionOnLastNameSet()) {
      String normalizedName =
          criteria.getCriterionOnLastName().replaceAll("'", "''").replaceAll("\\*", "%");
      query.and("lower(st_user.lastName) like lower(?)", normalizedName);
    }

    if (criteria.isCriterionOnNameSet()) {
      String normalizedName =
          criteria.getCriterionOnName().replaceAll("'", "''").replaceAll("\\*", "%");
      query.and("(lower(st_user.firstName) like lower(?) OR lower(st_user.lastName) like lower(?))",
          normalizedName, normalizedName);
    }

    if (criteria.isCriterionOnUserIdsSet()) {
      List<Integer> userIds = Arrays.stream(criteria.getCriterionOnUserIds())
          .map(Integer::parseInt)
          .collect(Collectors.toList());
      query.and("st_user.id").in(userIds);
    }

    if (criteria.isCriterionOnGroupIdsSet()) {
      List<Integer> groupIds = Arrays.stream(criteria.getCriterionOnGroupIds())
          .map(Integer::parseInt)
          .collect(Collectors.toList());
      query.and("st_user.id = st_group_user_rel.userId")
          .and("st_group_user_rel.groupId").in(groupIds);
    }

    finalizeJdbcSqlQuery(fields, query);

    return query;
  }

  @Override
  public String toString() {
    return toSQLQuery("*").getSqlQuery();
  }

  @Override
  public boolean isEmpty() {
    return criteria.isEmpty();
  }

  @Override
  public SearchCriteria onRoleNames(String... roleIds) {
    throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
  }

  @Override
  public SearchCriteria onComponentInstanceId(String instanceId) {
    throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
  }

  @Override
  public SearchCriteria onResourceId(String resourceId) {
    throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
  }

  @Override
  public SearchCriteria onPagination(PaginationPage page) {
    this.criteria.onPagination(page);
    return this;
  }

  private void finalizeJdbcSqlQuery(final String fields, final JdbcSqlQuery query) {
    boolean userSelection = false;
    if (!fields.toLowerCase().matches("(count|max|min)\\(.*\\)")) {
      query.orderBy(ORDERING);
      userSelection = true;
    }

    if (criteria.isCriterionOnPaginationSet() && userSelection) {
      PaginationPage page = criteria.getCriterionOnPagination();
      query.offset((page.getPageNumber() - 1) * page.getPageSize());
      query.limit(page.getPageSize());
    }
  }

  private JdbcSqlQuery prepareJdbcSqlQuery(final String fields) {
    List<UserState> userStatesToExclude = new ArrayList<>();
    if (criteria.isCriterionOnUserStatesToExcludeSet()) {
      Collections.addAll(userStatesToExclude, criteria.getCriterionOnUserStatesToExclude());
    }
    userStatesToExclude.add(UserState.DELETED);

    return JdbcSqlQuery.createSelect(fields)
        .from(tables.stream().collect(Collectors.joining(",")))
        .where("st_user.state").notIn(userStatesToExclude);
  }
}
