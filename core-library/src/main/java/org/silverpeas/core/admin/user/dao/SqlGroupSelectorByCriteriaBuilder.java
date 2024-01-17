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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.admin.user.dao;

import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.user.constant.GroupState;
import org.silverpeas.core.admin.user.model.GroupsSearchCriteria;
import org.silverpeas.core.admin.user.model.SearchCriteria;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A builder of {@link org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery} to select some fields
 * of the groups of users found from some given criteria.
 * @author mmoquillon
 */
public class SqlGroupSelectorByCriteriaBuilder {
  private final String fields;

  SqlGroupSelectorByCriteriaBuilder(final String fields) {
    this.fields = fields;
  }

  /**
   * Builds the SQL query to find the {@link GroupState#VALID} groups of users that match the
   * specified criteria.
   * @param criteria a set of criteria on the groups of users to find.
   * @return the SQL query matching the specified criteria.
   */
  public JdbcSqlQuery build(final GroupsSearchCriteria criteria) {
    final JdbcSqlQuery query = JdbcSqlQuery.createSelect(fields)
        .from(getTables(criteria))
        .where("st_group.state")
        .notIn(GroupState.REMOVED);

    applyCriteriaOnGroupName(query, criteria);
    applyCriteriaOnGroupIds(query, criteria);
    applyCriteriaOnDomain(query, criteria);
    applyCriteriaOnUserIds(query, criteria);
    applyCriteriaOnRoles(query, criteria);
    applyCriteriaOnSuperGroup(query, criteria);

    query.orderBy("st_group.name");

    if (criteria.isCriterionOnPaginationSet()) {
      PaginationPage page = criteria.getCriterionOnPagination();
      query.withPagination(page.asCriterion());
    }

    return query;
  }

  private void applyCriteriaOnSuperGroup(final JdbcSqlQuery query,
      final GroupsSearchCriteria criteria) {
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

  private void applyCriteriaOnRoles(final JdbcSqlQuery query, final GroupsSearchCriteria criteria) {
    if (criteria.isCriterionOnRoleNamesSet()) {
      List<Integer> rolesIds = Stream.of(criteria.getCriterionOnRoleNames())
          .map(Integer::parseInt)
          .collect(Collectors.toList());
      query.and("st_group.id = st_userrole_group_rel.groupId")
          .and("st_userrole_group_rel.userRoleId")
          .in(rolesIds);
    }
  }

  private void applyCriteriaOnUserIds(final JdbcSqlQuery query,
      final GroupsSearchCriteria criteria) {
    if (criteria.isCriterionOnUserIdsSet()) {
      List<Integer> ids = Stream.of(criteria.getCriterionOnUserIds())
          .map(Integer::parseInt)
          .collect(Collectors.toList());
      query.and("st_group.id = st_group_user_rel.groupId").and("st_group_user_rel.userId").in(ids);
    }
  }

  private void applyCriteriaOnGroupName(final JdbcSqlQuery query,
      final GroupsSearchCriteria criteria) {
    if (criteria.isCriterionOnNameSet()) {
      final String normalizedName = criteria.getCriterionOnName().replace('*', '%');
      query.and("lower(st_group.name) like lower(?)", normalizedName);
    }
  }

  private void applyCriteriaOnGroupIds(final JdbcSqlQuery query,
      final GroupsSearchCriteria criteria) {
    if (criteria.isCriterionOnGroupIdsSet() &&
        criteria.getCriterionOnGroupIds() != SearchCriteria.Constants.ANY) {
      List<Integer> groupIds = Stream.of(criteria.getCriterionOnGroupIds())
          .map(Integer::parseInt)
          .collect(Collectors.toList());
      query.and("st_group.id").in(groupIds);
    }
  }

  private void applyCriteriaOnDomain(final JdbcSqlQuery query,
      final GroupsSearchCriteria criteria) {
    if (criteria.isCriterionOnDomainIdSet()) {
      List<Integer> domainIds = Stream.of(criteria.getCriterionOnDomainIds())
          .map(Integer::parseInt)
          .collect(Collectors.toList());
      query.and("st_group.domainId").in(domainIds);
    }
  }

  private String[] getTables(final GroupsSearchCriteria criteria) {
    final List<String> tables = new ArrayList<>();
    tables.add("st_group");
    if (criteria.isCriterionOnRoleNamesSet()) {
      tables.add("st_userrole_group_rel");
    }
    if (criteria.isCriterionOnUserIdsSet()) {
      tables.add("st_group_user_rel");
    }
    return tables.toArray(new String[0]);
  }
}
  