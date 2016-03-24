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

import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.user.model.SearchCriteria;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * An implementation of the search criteria for user groups stored in a SQL data source and used by
 * the DAOs. By default, the criterion are linked together by a conjonction operator. Nevertheless,
 * you can explicitly specify it by using the GroupSearchCriteriaForDAO#and() method.
 */
public class GroupSearchCriteriaForDAO implements SearchCriteria {

  private static final String QUERY = "select distinct {0} from {1} {2} order by name";
  private final StringBuilder filter = new StringBuilder();
  private Set<UserState> userStatesToExclude = new HashSet<UserState>();
  private final Set<String> tables = new HashSet<String>();
  private final List<String> domainIds = new ArrayList<String>();
  private PaginationPage page = null;

  public static GroupSearchCriteriaForDAO newCriteria() {
    return new GroupSearchCriteriaForDAO();
  }

  @Override
  public GroupSearchCriteriaForDAO and() {
    if (filter.length() > 0) {
      filter.append(" and ");
    }
    return this;
  }

  @Override
  public GroupSearchCriteriaForDAO or() {
    if (filter.length() > 0) {
      filter.append(" or ");
    }
    return this;
  }

  @Override
  public GroupSearchCriteriaForDAO onName(String name) {
    if (isDefined(name)) {
      tables.add("st_group");
      getFixedQuery().append("lower(st_group.name) like lower('").
          append(name).
          append("')");
    }
    return this;
  }

  @Override
  public GroupSearchCriteriaForDAO onGroupIds(String... groupIds) {
    if (groupIds != ANY) {
      tables.add("st_group");
      StringBuilder[] sqlLists = asSQLList(groupIds);
      StringBuilder theQuery = getFixedQuery().append("(st_group.id in ").append(sqlLists[0]);
      for (int i = 1; i < sqlLists.length; i++) {
        theQuery.append(" or st_group.id in ").append(sqlLists[i]);
      }
      theQuery.append(")");
    }
    return this;
  }

  @Override
  public GroupSearchCriteriaForDAO onDomainId(String domainId) {
    if (isDefined(domainId)) {
      domainIds.add(domainId);
      tables.add("st_group");
      getFixedQuery().append("st_group.domainId = ").append(Integer.valueOf(domainId));
    }
    return this;
  }

  @Override
  public GroupSearchCriteriaForDAO onAccessLevels(UserAccessLevel... accessLevels) {
    // Not handled for now
    removeLastOperatorIfAny();
    return this;
  }

  @Override
  public SearchCriteria onUserStatesToExclude(final UserState... userStates) {
    if (userStates != null && userStates.length > 0) {
      Collections.addAll(userStatesToExclude, userStates);
      // It is only used in order to transport the criterion which will be given to the
      // UserSearchCriteriaForDAO when getting the number of users linked to the group
    }
    removeLastOperatorIfAny();
    return this;
  }

  public GroupSearchCriteriaForDAO onMixedDomainOronDomainId(String domainId) {
    if (isDefined(domainId)) {
      domainIds.add(domainId);
      tables.add("st_group");
      getFixedQuery().append("(st_group.domainId = ").append(Integer.valueOf(domainId)).
          append(" or st_group.domainId = ").append(Integer.valueOf(Domain.MIXED_DOMAIN_ID)).
          append(")");
    }
    return this;
  }

  @Override
  public SearchCriteria onUserIds(String... userIds) {
    if (userIds != ANY) {
      tables.add("st_group_user_rel");
      StringBuilder[] sqlLists = asSQLList(userIds);
      getFixedQuery().append("st_group.id = st_group_user_rel.groupid");
      StringBuilder theQuery =
          getFixedQuery().append(" and (st_group_user_rel.userid in ").append(sqlLists[0]);
      for (int i = 1; i < sqlLists.length; i++) {
        theQuery.append(" or st_group_user_rel.userid in ").append(sqlLists[i]);
      }
      theQuery.append(")");
    }
    return this;
  }

  @Override
  public SearchCriteria onRoleNames(String... roleIds) {
    if (roleIds != null && roleIds.length > 0) {
      tables.add("st_group");
      tables.add("st_userrole_group_rel");
      StringBuilder[] sqlLists = asSQLList(roleIds);
      StringBuilder theQuery = getFixedQuery().
          append(
              "(ST_Group.id = ST_UserRole_Group_Rel.groupId and (ST_UserRole_Group_Rel.userRoleId" +
                  " in ").
          append(sqlLists[0]);
      for (int i = 1; i < sqlLists.length; i++) {
        theQuery.append(" or ST_UserRole_Group_Rel.userRoleId in ").append(sqlLists[i]);
      }
      theQuery.append("))");
    }
    return this;
  }

  public SearchCriteria onSuperGroupId(String superGroupId) {
    if (isDefined(superGroupId)) {
      tables.add("st_group");
      getFixedQuery().append("st_group.superGroupId = ").append(Integer.valueOf(superGroupId));
    }
    return this;
  }

  public SearchCriteria onAsRootGroup() {
    tables.add("st_group");
    getFixedQuery().append("st_group.superGroupId is null");
    return this;
  }

  public String toSQLQuery(String fields) {
    return MessageFormat.format(QUERY, fields, impliedTables(), queryFilter());
  }

  @Override
  public String toString() {
    return toSQLQuery("*");
  }

  @Override
  public boolean isEmpty() {
    return filter.length() == 0;
  }

  public boolean isCriterionOnDomainIdSet() {
    return !domainIds.isEmpty();
  }

  public List<String> getCriterionOnDomainIds() {
    return domainIds;
  }

  public Set<UserState> getCriterionOnUserStatesToExclude() {
    return userStatesToExclude;
  }

  private String impliedTables() {
    StringBuilder tablesUsedInCriteria = new StringBuilder();
    for (String aTable : tables) {
      tablesUsedInCriteria.append(aTable).append(", ");
    }
    return tablesUsedInCriteria.substring(0, tablesUsedInCriteria.length() - 2);
  }

  private String queryFilter() {
    String sqlFilter = "";
    if (filter.length() > 0) {
      sqlFilter += " where " + filter;
    }
    return sqlFilter;
  }

  private GroupSearchCriteriaForDAO() {
    tables.add("st_group");
  }

  /**
   * Gets the current query after fixing it if it is needed. The query is fixed if it contains some
   * contraints and we are coming to set another one without set a conjonction or a disjonction link
   * between the previous contraints and the next one.
   *
   * @return the current query fixed so that the contraints are linked with either a conjonction or
   * a disjonction operator.
   */
  private StringBuilder getFixedQuery() {
    if (filter.length() > 0 && !filter.toString().endsWith(" and ")
        && !filter.toString().endsWith(" or ")) {
      filter.append(" and ");
    }
    return filter;
  }

  // Oracle has a hard limitation with SQL lists with 'in' clause: it cannot take more than 1000
  // elements. So we split it in several SQL lists so that they contain less than 1000 elements.
  private StringBuilder[] asSQLList(String... items) {
    StringBuilder[] lists = new StringBuilder[(int) Math.ceil(items.length / 1000) + 1];
    int count = 0;
    int i = 0;
    lists[i] = new StringBuilder("(");
    for (String anItem : items) {
      if (++count >= 1000) {
        lists[i].setCharAt(lists[i].length() - 1, ')');
        lists[++i] = new StringBuilder("(");
        count = 0;
      }
      lists[i].append(anItem).append(",");
    }
    if (lists[i].toString().endsWith(",")) {
      lists[i].setCharAt(lists[i].length() - 1, ')');
    } else {
      lists[i].append("null").append(")");
    }
    return lists;
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
    removeLastOperatorIfAny();
    this.page = page;
    return this;
  }

  /**
   * Removes from the query the last operator if any.<br/>
   * It is useful to use it on a criteria that is not yet handled (for example).
   */
  private void removeLastOperatorIfAny() {
    if (filter.toString().endsWith(" and ")) {
      filter.delete(filter.toString().lastIndexOf(" and "), filter.length());
    } else if (filter.toString().endsWith(" or ")) {
      filter.delete(filter.toString().lastIndexOf(" or "), filter.length());
    }
  }

  /**
   * Gets the criterion on the pagination page to fetch.
   *
   * @return a pagination page.
   */
  public PaginationPage getPagination() {
    return page;
  }

  /**
   * Is the pagination criterion set?
   *
   * @return true if a criterion on the pagination about user groups is set, false otherwise.
   */
  public boolean isPaginationSet() {
    return page != null;
  }
}
