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
package com.stratelia.webactiv.beans.admin.dao;

import com.stratelia.webactiv.beans.admin.SearchCriteria;
import java.util.HashSet;
import java.util.Set;

import static com.silverpeas.util.StringUtil.isDefined;

/**
 * An implementation of the search criteria for user details stored in a SQL data source and used by
 * the DAOs. By default, the criterion are linked together by a conjonction operator. Nevertheless,
 * you can explicitly specify it by using the UserSearchCriteriaForDAO#and() method.
 */
public class UserSearchCriteriaForDAO implements SearchCriteria {

  private StringBuilder query = new StringBuilder();
  private Set<String> tables = new HashSet<String>();

  public static UserSearchCriteriaForDAO newCriteria() {
    return new UserSearchCriteriaForDAO();
  }

  @Override
  public UserSearchCriteriaForDAO and() {
    if (query.length() > 0) {
      query.append(" and ");
    }
    return this;
  }

  @Override
  public UserSearchCriteriaForDAO or() {
    if (query.length() > 0) {
      query.append(" or ");
    }
    return this;
  }

  @Override
  public UserSearchCriteriaForDAO onName(String name) {
    if (isDefined(name)) {
      tables.add("st_user");
      getFixedQuery().append("(lower(st_user.firstName) like lower('").
              append(name).
              append("') or lower(st_user.lastName) like lower('").
              append(name).
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
  public UserSearchCriteriaForDAO onDomainId(String domainId) {
    // all users that are part of the specified domain or that have administration priviledges
    // (the administrators should be visible by anyone in order to be contacted)
    if (isDefined(domainId)) {
      tables.add("st_user");
      getFixedQuery().append("(st_user.domainId = ").append(Integer.valueOf(domainId)).
              append(" or st_user.accessLevel = 'A')");
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
  public String toString() {
    return query.toString();
  }

  @Override
  public boolean isEmpty() {
    return query.length() == 0;
  }

  public String impliedTables() {
    StringBuilder tablesUsedInCriteria = new StringBuilder();
    for (String aTable : tables) {
      tablesUsedInCriteria.append(aTable).append(", ");
    }
    return tablesUsedInCriteria.substring(0, tablesUsedInCriteria.length() - 2);
  }

  private UserSearchCriteriaForDAO() {
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
    if (query.length() > 0 && !query.toString().endsWith(" and ")
            && !query.toString().endsWith(" or ")) {
      query.append(" and ");
    }
    return query;
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
  public SearchCriteria onRoleIds(String... roleIds) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public SearchCriteria onComponentInstanceId(String instanceId) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
