/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.ProfiledObjectId;
import org.silverpeas.core.admin.ProfiledObjectType;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.user.constant.GroupState;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.model.UserDetailsSearchCriteria;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.util.MemoizedSupplier;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.text.MessageFormat.format;
import static org.silverpeas.core.SilverpeasExceptionMessages.failureOnGetting;
import static org.silverpeas.core.util.ArrayUtil.isNotEmpty;

/**
 * A builder of {@link org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery} to select some fields
 * of the users found from some given criteria.
 * @author mmoquillon
 */
public class SqlUserSelectorByCriteriaBuilder {

  private final String fields;
  private MemoizedSupplier<SilverpeasComponentInstance> componentInstanceSupplier;

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
    final JdbcSqlQuery query = JdbcSqlQuery.select(fields);

    applyJoinsAndDefaultCriteria(query, criteria);
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

  private void applyJoinsAndDefaultCriteria(final JdbcSqlQuery query,
      final UserDetailsSearchCriteria criteria) {
    query.from("st_user");
    if (criteria.isCriterionOnGroupIdsSet() || criteria.isCriterionOnAnyGroupSet()) {
      query.join("st_group_user_rel").on("st_group_user_rel.userid = st_user.id");
      query.join("st_group").on("st_group.id = st_group_user_rel.groupid");
    }
    query.where("st_user.state").notIn(getExcludedUserStates(criteria));
  }

  private void applyCriteriaOnGroupIds(final JdbcSqlQuery query,
      final UserDetailsSearchCriteria criteria) {
    if (criteria.isCriterionOnGroupIdsSet() || criteria.isCriterionOnAnyGroupSet()) {
      query.and("st_group.state").notIn(GroupState.REMOVED);
      final String[] groupIds = criteria.getCriterionOnGroupIds();
      if (isNotEmpty(groupIds)) {
        query.and("st_group_user_rel.groupId")
             .in(Stream.of(groupIds).map(Integer::parseInt).collect(Collectors.toList()));
      }
    }
  }

  private void applyCriteriaOnUserName(final JdbcSqlQuery query,
      final UserDetailsSearchCriteria criteria) {
    if (criteria.isCriterionOnNameSet()) {
      final String normalizedName = criteria.getCriterionOnName().replace('*', '%');
      query.and("(lower(st_user.firstName) like lower(?) OR lower(st_user.lastName) like lower(?))",
          normalizedName, normalizedName);
    } else {
      if (criteria.isCriterionOnFirstNameSet()) {
        final String normalizedName = criteria.getCriterionOnFirstName().replace('*', '%');
        query.and("lower(st_user.firstName) like lower(?)", normalizedName);
      }
      if (criteria.isCriterionOnLastNameSet()) {
        final String normalizedName = criteria.getCriterionOnLastName().replace('*', '%');
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
    getSharedComponentInstanceWithRights(criteria).ifPresent(i -> {
      int instanceId = ComponentInst.getComponentLocalId(i.getId());
      query.and("st_user.id IN (");
      query.addSqlPart(
          "SELECT distinct all_profiles.userid " +
          "FROM (" +
              "SELECT distinct urur.userId, ur.rolename " +
              "FROM st_userrole_user_rel urur " +
                    "INNER JOIN st_userrole ur ON urur.userroleId = ur.id " +
              "WHERE ur.instanceId = ?", instanceId);
      if (criteria.isCriterionOnResourceIdSet()) {
        final List<ProfiledObjectId> profiledResourceId = Stream
            .of(criteria.getCriterionOnResourceId())
            .map(ProfiledObjectId::from)
            .collect(Collectors.toList());
        query.and("ur.objecttype").in(profiledResourceId.stream()
            .map(ProfiledObjectId::getType)
            .map(ProfiledObjectType::getCode)
            .collect(Collectors.toList()));
        query.and("ur.objectId").in(profiledResourceId.stream()
            .map(ProfiledObjectId::getId)
            .map(Integer::parseInt)
            .collect(Collectors.toList()));
      } else {
        query.andNull("ur.objectId");
      }
      if (criteria.isCriterionOnRoleNamesSet()) {
        query.and("ur.rolename").in((Object[]) criteria.getCriterionOnRoleNames());
      }
      if (criteria.isCriterionOnGroupsByRolesSet()) {
        final Map<String, Set<String>> groupIdsByRoles = criteria.getCriterionOnGroupsByRoles();
        if (criteria.mustMatchAllRoles()) {
          groupIdsByRoles.forEach((r, g) -> {
            query.union();
            query.addSqlPart(format(
                "SELECT distinct gur.userId, ''{0}'' as rolename " +
                "FROM st_group_user_rel gur " +
                "WHERE gur.groupId", r))
                .in(g.stream().map(Integer::parseInt).collect(Collectors.toList()));
          });
        } else {
          query.union();
          query.addSqlPart(
              "SELECT distinct gur.userId, 'dummy' as rolename " +
              "FROM st_group_user_rel gur " +
              "WHERE gur.groupId")
              .in(groupIdsByRoles.values()
                  .stream()
                  .flatMap(Collection::stream)
                  .distinct()
                  .map(Integer::parseInt)
                  .collect(Collectors.toList()));
        }
      }
      query.addSqlPart(") all_profiles");
      if (criteria.mustMatchAllRoles()) {
        query.addSqlPart(
            "GROUP BY all_profiles.userid " +
            "HAVING COUNT(all_profiles.userid) = ?", criteria.getCriterionOnRoleNames().length);
      }
      query.addSqlPart(")");
    });
  }

  private Optional<SilverpeasComponentInstance> getSharedComponentInstanceWithRights(
      final UserDetailsSearchCriteria criteria) {
    if (componentInstanceSupplier == null) {
      componentInstanceSupplier = new MemoizedSupplier<>(() -> {
        if (!criteria.isCriterionOnComponentInstanceIdSet()) {
          return null;
        }
        final String instanceId = criteria.getCriterionOnComponentInstanceId();
        try {
          return Administration.get().getComponentInstance(instanceId);
        } catch (AdminException e) {
          throw new SilverpeasRuntimeException(failureOnGetting("component instance", instanceId));
        }
      });
    }
    return Optional.ofNullable(componentInstanceSupplier.get())
        .filter(i -> !i.isPersonal() && !i.isPublic());
  }

  private Set<UserState> getExcludedUserStates(final UserDetailsSearchCriteria criteria) {
    final Set<UserState> excludedStates = new HashSet<>();
    excludedStates.add(UserState.DELETED);
    if (criteria.isCriterionOnUserStatesToExcludeSet()) {
      excludedStates.addAll(Arrays.asList(criteria.getCriterionOnUserStatesToExclude()));
    }
    if (!criteria.mustIncludeRemovedUsers()) {
      getSharedComponentInstanceWithRights(criteria)
          // this clause is to be compliant with searchGroup service
          // this is a limitation induced by profile services which are excluding REMOVED users
          .ifPresent(i -> excludedStates.add(UserState.REMOVED));
    }
    return excludedStates;
  }
}
  