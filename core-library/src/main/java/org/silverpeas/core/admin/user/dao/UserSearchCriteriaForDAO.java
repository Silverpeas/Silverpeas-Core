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
import org.silverpeas.core.admin.user.model.SearchCriteria;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * An implementation of the search criteria for user details stored in a SQL data source and used by
 * the DAOs. By default, the criterion are linked together by a conjonction operator. Nevertheless,
 * you can explicitly specify it by using the UserSearchCriteriaForDAO#and() method.
 */
public class UserSearchCriteriaForDAO implements SearchCriteria {

  private static final String QUERY = "select {0} from {1} where state not in ({2}) {3} {4}";
  private static final String ORDER_BY_LASTNAME = "order by lastName, firstName";

  private StringBuilder filter = new StringBuilder();
  private Set<UserState> userStatesToExclude = new HashSet<UserState>();
  private Set<String> tables = new HashSet<String>();
  private PaginationPage page = null;

  public static UserSearchCriteriaForDAO newCriteria() {
    return new UserSearchCriteriaForDAO();
  }

  @Override
  public UserSearchCriteriaForDAO and() {
    if (filter.length() > 0) {
      filter.append(" and ");
    }
    return this;
  }

  @Override
  public UserSearchCriteriaForDAO or() {
    if (filter.length() > 0) {
      filter.append(" or ");
    }
    return this;
  }

  @Override
  public UserSearchCriteriaForDAO onName(String name) {
    if (isDefined(name)) {
      tables.add("st_user");
      String normalizedName = name.replaceAll("'", "''");
      getFixedQuery().append("(lower(st_user.firstName) like lower('").
              append(normalizedName).
              append("') or lower(st_user.lastName) like lower('").
              append(normalizedName).
              append("'))");
    }
    return this;
  }

  @Override
  public UserSearchCriteriaForDAO onGroupIds(String... groupIds) {
    tables.add("st_user");
    tables.add("st_group_user_rel");
    StringBuilder theQuery = getFixedQuery().append("(st_group_user_rel.userid = st_user.id");
    if (groupIds != ANY) {
      StringBuilder[] sqlLists = asSQLList(groupIds);
      theQuery.append(" and (st_group_user_rel.groupId in ").
              append(sqlLists[0]);
      for (int i = 1; i < sqlLists.length; i++) {
        theQuery.append(" or st_group_user_rel.groupId in ").
                append(sqlLists[i]);
      }
      theQuery.append(")");
    }
    theQuery.append(")");
    return this;
  }

  @Override
  public UserSearchCriteriaForDAO onAccessLevels(UserAccessLevel... accessLevels) {
    if (accessLevels != null && accessLevels.length > 0) {
      tables.add("st_user");
      StringBuilder accessLevelsAsCodes = new StringBuilder();
      for (UserAccessLevel accessLevel : accessLevels) {
        if (accessLevelsAsCodes.length() > 0) {
          accessLevelsAsCodes.append(",");
        }
        accessLevelsAsCodes.append("'");
        accessLevelsAsCodes.append(accessLevel.getCode());
        accessLevelsAsCodes.append("'");
      }
      getFixedQuery().append("(st_user.accessLevel in (").append(accessLevelsAsCodes.toString())
          .append("))");
    }
    return this;
  }

  @Override
  public UserSearchCriteriaForDAO onDomainId(String domainId) {
    // all users that are part of the specified domain or that have administration priviledges
    // (the administrators should be visible by anyone in order to be contacted)
    if (isDefined(domainId)) {
      tables.add("st_user");
      getFixedQuery().append("st_user.domainId = ").append(Integer.valueOf(domainId));
    }
    return this;
  }

  @Override
  public SearchCriteria onUserIds(String... userIds) {
    if (userIds != ANY) {
      tables.add("st_user");
      StringBuilder[] sqlLists = asSQLList(userIds);
      StringBuilder theQuery = getFixedQuery().append("(st_user.id in ").append(sqlLists[0]);
      for (int i = 1; i < sqlLists.length; i++) {
        theQuery.append(" or st_user.id in ").append(sqlLists[i]);
      }
      theQuery.append(")");
    }
    return this;
  }

  @Override
  public UserSearchCriteriaForDAO onUserStatesToExclude(final UserState... userStates) {
    Collections.addAll(userStatesToExclude, userStates);
    String tmpFilter = filter.toString().replaceFirst(" (and|or) $", "");
    filter = new StringBuilder(tmpFilter);
    return this;
  }

  public String toSQLQuery(String fields) {
    String ordering = ORDER_BY_LASTNAME;
    if (fields.toLowerCase().matches("(count|max|min)\\(.*\\)")) {
      ordering = "";
    }

    StringBuilder excludedUserStatesSQLPart =
        new StringBuilder("'").append(UserState.DELETED).append("'");
    for (UserState userStateToExclude : userStatesToExclude) {
      excludedUserStatesSQLPart.append(", '").append(userStateToExclude).append("'");
    }

    return MessageFormat
        .format(QUERY, fields, impliedTables(), excludedUserStatesSQLPart.toString(), queryFilter(),
            ordering);
  }

  @Override
  public String toString() {
    return toSQLQuery("*");
  }

  @Override
  public boolean isEmpty() {
    return filter.length() == 0;
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
      sqlFilter += " and " + filter;
    }
    return sqlFilter;
  }

  private UserSearchCriteriaForDAO() {
    tables.add("st_user");
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
  public SearchCriteria onRoleNames(String... roleIds) {
    throw new UnsupportedOperationException("Not supported yet.");
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
    if (filter.toString().endsWith(" and ")) {
      filter.delete(filter.toString().lastIndexOf(" and "), filter.length());
    } else if  (filter.toString().endsWith(" or ")) {
      filter.delete(filter.toString().lastIndexOf(" or "), filter.length());
    }
    this.page = page;
    return this;
  }

  /**
   * Gets the criterion on the pagination page to fetch.
   * @return a pagination page.
   */
  public PaginationPage getPagination() {
    return page;
  }

  /**
   * Is the pagination criterion set?
   * @return true if a criterion on the pagination about user profiles is set, false otherwise.
   */
  public boolean isPaginationSet() {
    return page != null;
  }
}
