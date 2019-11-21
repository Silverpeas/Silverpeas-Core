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
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.model.SearchCriteria;
import org.silverpeas.core.admin.user.model.UserDetailsSearchCriteria;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * An implementation of the search criteria for user details stored in a SQL data source and used by
 * the DAOs. By default, the criteria are linked together by a conjunction operator. Nevertheless,
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

  /**
   * Constructs new criteria on the users to search in the data source. Automatically, all the
   * deleted users are always excluded from the search scope, but not the removed users (the users
   * that in a deletion awaiting). To exclude the removed users, simply pass the state
   * {@link UserState#REMOVED} in the
   * {@link UserSearchCriteriaForDAO#onUserStatesToExclude(UserState...)} method invocation.
   * @return new search criteria for DAOs.
   */
  public static UserSearchCriteriaForDAO newCriteria() {
    return newCriteriaFrom(new UserDetailsSearchCriteria());
  }

  /**
   * Constructs new criteria on the users to search in the data source from the specified criteria
   * on the users' properties. Automatically, all the deleted users are always excluded from the
   * search scope, but not the removed users (the users that in a deletion awaiting). To exclude the
   * removed users, simply indicate the state {@link UserState#REMOVED} as to be excluded either in
   * the specified criteria or in the
   * {@link UserSearchCriteriaForDAO#onUserStatesToExclude(UserState...)} method invocation.
   * @return new search criteria for DAOs.
   */
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
    if (groupIds != Constants.ANY) {
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

  public UserSearchCriteriaForDAO onDomainIds(String... domainIds) {
    // all users that are part of the specified domain or that have administration priviledges
    // (the administrators should be visible by anyone in order to be contacted)
    criteria.onDomainIds(domainIds);
    return this;
  }

  @Override
  public UserSearchCriteriaForDAO onUserIds(String... userIds) {
    if (userIds != Constants.ANY) {
      criteria.onUserIds(userIds);
    }
    return this;
  }

  @Override
  public SearchCriteria onUserSpecificIds(final String... userSpecificIds) {
    if (userSpecificIds != Constants.ANY) {
      criteria.onUserSpecificIds(userSpecificIds);
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
      query.and("st_user.domainId").in(Arrays.stream(criteria.getCriterionOnDomainIds()).map(
          Integer::parseInt).collect(Collectors.toList()));
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

    if (criteria.isCriterionOnUserSpecificIdsSet()) {
      if (criteria.getCriterionOnDomainIds().length > 1) {
        throw new IllegalArgumentException(
            "one, and ony one, domain id must be set as filter when searching user on its " +
                "specific id");
      }
      final List<String> userSpecificIds = Arrays.stream(criteria.getCriterionOnUserSpecificIds())
          .collect(Collectors.toList());
      query.and("st_user.specificId").in(userSpecificIds);
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
      query.withPagination(page.asCriterion());
    }
  }

  private JdbcSqlQuery prepareJdbcSqlQuery(final String fields) {
    final Set<UserState> userStatesToExclude = new HashSet<>();
    if (criteria.isCriterionOnUserStatesToExcludeSet()) {
      Collections.addAll(userStatesToExclude, criteria.getCriterionOnUserStatesToExclude());
    }
    userStatesToExclude.add(UserState.DELETED);
    return JdbcSqlQuery.createSelect(fields)
        .from(String.join(",", tables))
        .where("st_user.state").notIn(userStatesToExclude);
  }
}
