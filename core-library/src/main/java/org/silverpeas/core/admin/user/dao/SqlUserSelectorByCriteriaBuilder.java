/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.admin.user.dao;

import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.model.UserDetailsSearchCriteria;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A builder of {@link JdbcSqlQuery} to select some fields of the users found
 * from some given criteria.
 * @author mmoquillon
 */
public class SqlUserSelectorByCriteriaBuilder {

  private final String fields;

  SqlUserSelectorByCriteriaBuilder(final String fields) {
    this.fields = fields;
  }

  /**
   * Builds the a SQL query to find the users that match the specified criteria with the peculiar
   * expectations:
   * <ul>
   *   <li>First, the criterion on the groups must include also their own children groups;</li>
   *   <li>Secondly, when the criterion on the role names are set along the one on the component
   *   instance, the criterion on the groups in those roles should be set.</li>
   * </ul>
   * If one of these expectations aren't fulfilled then the query couldn't be correct and any
   * unexpected results could be returned.
   * @param criteria a set of criteria on the users to find.
   * @return the SQL query matching the specified criteria.
   */
  public JdbcSqlQuery build(final UserDetailsSearchCriteria criteria) {
    JdbcSqlQuery query = JdbcSqlQuery.createSelect(fields)
        .from(getTables(criteria))
        .where("st_user.state")
        .notIn(getExcludedUserStates(criteria));

    applyCriteriaOnRoles(query, criteria);
    applyCriteriaOnDomain(query, criteria);
    applyCriteriaOnUserIds(query, criteria);
    applyCriteriaOnAccessLevel(query, criteria);
    applyCriteriaOnUserName(query, criteria);
    applyCriteriaOnGroupIds(query, criteria);

    if (!fields.toLowerCase().matches("(count|max|min)\\(.*\\)")) {
      query.orderBy("lastName, firstName");
      if (criteria.isCriterionOnPaginationSet()) {
        query.withPagination(criteria.getCriterionOnPagination().asCriterion());
      }
    }

    return query;
  }

  private void applyCriteriaOnGroupIds(final JdbcSqlQuery query,
      final UserDetailsSearchCriteria criteria) {
    if (criteria.isCriterionOnGroupIdsSet()) {
      String[] groupIds = criteria.getCriterionOnGroupIds();
      query.and("st_group_user_rel.groupId")
          .in(Stream.of(groupIds).map(Integer::parseInt).collect(Collectors.toList()))
          .and("st_group_user_rel.userId = st_user.id");
    }
  }

  private void applyCriteriaOnUserName(final JdbcSqlQuery query,
      final UserDetailsSearchCriteria criteria) {
    if (criteria.isCriterionOnNameSet()) {
      String normalizedName =
          criteria.getCriterionOnName().replaceAll("'", "''").replaceAll("\\*", "%");
      query.and("(lower(st_user.firstName) like lower(?) OR lower(st_user.lastName) like lower(?))",
          normalizedName, normalizedName);
    } else {
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
    }
  }

  private void applyCriteriaOnAccessLevel(final JdbcSqlQuery query,
      final UserDetailsSearchCriteria criteria) {
    if (criteria.isCriterionOnAccessLevelsSet()) {
      List<String> codes = Stream.of(criteria.getCriterionOnAccessLevels())
          .map(UserAccessLevel::getCode)
          .collect(Collectors.toList());
      query.and("st_user.accessLevel").in(codes);
    }
  }

  private void applyCriteriaOnUserIds(final JdbcSqlQuery query,
      final UserDetailsSearchCriteria criteria) {
    if (criteria.isCriterionOnUserIdsSet()) {
      String[] userIds = criteria.getCriterionOnUserIds();
      query.and("st_user.id")
          .in(Stream.of(userIds).map(Integer::parseInt).collect(Collectors.toList()));
    }
    if (criteria.isCriterionOnUserSpecificIdsSet()) {
      query.and("st_user.specificId").in(criteria.getCriterionOnUserSpecificIds());
    }
  }

  private void applyCriteriaOnDomain(final JdbcSqlQuery query,
      final UserDetailsSearchCriteria criteria) {
    if (criteria.isCriterionOnDomainIdSet()) {
      String[] domainIds = criteria.getCriterionOnDomainIds();
      if (domainIds.length > 1 && criteria.isCriterionOnUserSpecificIdsSet()) {
        throw new IllegalArgumentException(
            "Criterion on user specific id whereas there is a criterion on several user domains");
      }
      query.and("st_user.domainId")
          .in(Stream.of(domainIds).map(Integer::parseInt).collect(Collectors.toList()));
    }
  }

  private void applyCriteriaOnRoles(final JdbcSqlQuery query,
      final UserDetailsSearchCriteria criteria) {
    if (criteria.isCriterionOnComponentInstanceIdSet()) {
      int instanceId =
          ComponentInst.getComponentLocalId(criteria.getCriterionOnComponentInstanceId());
      query.and("(st_user.id IN (")
          .addSqlPart("SELECT st_userrole_user_rel.userId FROM st_userrole inner join " +
              "st_userrole_user_rel on st_userrole_user_rel.userroleId = st_userrole.id WHERE " +
              "st_userrole.instanceId = ?", instanceId);
      if (criteria.isCriterionOnResourceIdSet()) {
        query.and("st_userrole.objectId = ?",
            Integer.parseInt(criteria.getCriterionOnResourceId()));
      }
      if (criteria.isCriterionOnRoleNamesSet()) {
        query.and("st_userrole.roleName").in(criteria.getCriterionOnRoleNames());
      }
      query.addSqlPart(")");
      if (criteria.isCriterionOnGroupsInRolesSet()) {
        String[] groupIds = criteria.getCriterionOnGroupsInRoles();
        query.or("st_user.id IN (")
            .addSqlPart("SELECT userId FROM st_group_user_rel WHERE st_group_user_rel.groupId")
            .in(Stream.of(groupIds).map(Integer::parseInt).collect(Collectors.toList()))
            .addSqlPart(")");
      }
      query.addSqlPart(")");
    }
  }

  private Set<UserState> getExcludedUserStates(final UserDetailsSearchCriteria criteria) {
    final Set<UserState> excludedStates = new HashSet<>();
    excludedStates.add(UserState.DELETED);
    if (criteria.isCriterionOnUserStatesToExcludeSet()) {
      excludedStates.addAll(Arrays.asList(criteria.getCriterionOnUserStatesToExclude()));
    }
    return excludedStates;
  }

  private String[] getTables(final UserDetailsSearchCriteria criteria) {
    final List<String> tables = new ArrayList<>();
    tables.add("st_user");
    if (criteria.isCriterionOnGroupIdsSet()) {
      tables.add("st_group_user_rel");
    }
    return tables.toArray(new String[0]);
  }
}
  