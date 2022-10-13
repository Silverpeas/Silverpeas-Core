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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.silverstatistics.access.model;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.persistence.datasource.repository.PaginationCriterion;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.silverpeas.core.silverstatistics.access.model.HistoryCriteria.QUERY_ORDER_BY.ACCESS_DATE_DESC;

/**
 * Class that permits to set search criteria for history statistics.
 * @author silveryocha
 */
public class HistoryCriteria {

  public enum QUERY_ORDER_BY {

    ACCESS_DATE_ASC("datestat, heurestat"), ACCESS_DATE_DESC("datestat desc, heurestat desc");

    private final String clause;

    QUERY_ORDER_BY(final String clause) {
      this.clause = clause;
    }

    public String getClause() {
      return clause;
    }
  }

  private final int actionType;
  private String resourceType;
  private List<String> resourceIds = emptyList();
  private List<String> componentInstanceIds = emptyList();
  private List<String> userIds = emptyList();
  private List<String> excludedUserIds = emptyList();
  private PaginationPage pagination;
  private List<QUERY_ORDER_BY> orderByList = singletonList(ACCESS_DATE_DESC);

  public HistoryCriteria(final int actionType) {
    this.actionType = actionType;
  }

  /**
   * Sets the criterion of resources the history must be attached to.
   * @param resourceReference the reference of the resource.
   * @return criteria itself.
   */
  public HistoryCriteria onResource(final ResourceReference resourceReference) {
    this.resourceIds = singletonList(resourceReference.getLocalId());
    this.componentInstanceIds = singletonList(resourceReference.getComponentInstanceId());
    return this;
  }

  /**
   * Sets the criterion of resources type the history must be attached.
   * @param resourceType the reference of the resource.
   * @return criteria itself.
   */
  public HistoryCriteria ofType(final String resourceType) {
    this.resourceType = resourceType;
    return this;
  }

  /**
   * Sets the criterion of identifier of users the history must be attached to.
   * @param userIds identifier of users.
   * @return criteria itself.
   */
  public HistoryCriteria aboutUsers(String... userIds) {
    return aboutUsers(stream(userIds));
  }

  /**
   * Sets the criterion of identifier of users the history must be attached to.
   * @param userIds identifier of users.
   * @return criteria itself.
   */
  public HistoryCriteria aboutUsers(Collection<String> userIds) {
    return aboutUsers(userIds.stream());
  }

  /**
   * Sets the criterion of identifier of users the history must be attached to.
   * @param userIds identifier of users.
   * @return criteria itself.
   */
  public HistoryCriteria aboutUsers(Stream<String> userIds) {
    this.userIds = userIds.filter(Objects::nonNull).collect(Collectors.toList());
    return this;
  }

  /**
   * Sets the criterion of identifier of users the history must not be attached to.
   * @param excludedUserIds identifier of users.
   * @return criteria itself.
   */
  public HistoryCriteria byExcludingUsers(String... excludedUserIds) {
    return byExcludingUsers(stream(excludedUserIds));
  }

  /**
   * Sets the criterion of identifier of users the history must not be attached to.
   * @param excludedUserIds identifier of users.
   * @return criteria itself.
   */
  public HistoryCriteria byExcludingUsers(Collection<String> excludedUserIds) {
    return byExcludingUsers(excludedUserIds.stream());
  }

  /**
   * Sets the criterion of identifier of users the history must not be attached to.
   * @param excludedUserIds identifier of users.
   * @return criteria itself.
   */
  public HistoryCriteria byExcludingUsers(Stream<String> excludedUserIds) {
    this.excludedUserIds = excludedUserIds.filter(Objects::nonNull).collect(Collectors.toList());
    return this;
  }

  /**
   * Sets the criteria of pagination.
   * @param pagination the pagination.
   * @return criteria itself.
   */
  public HistoryCriteria paginatedBy(PaginationPage pagination) {
    this.pagination = pagination;
    return this;
  }

  /**
   * Configures the order of the list.
   * @param orderBies the list of order by directives.
   * @return itself.
   */
  public HistoryCriteria orderedBy(QUERY_ORDER_BY... orderBies) {
    this.orderByList = Arrays.stream(orderBies).filter(Objects::nonNull).collect(Collectors.toList());
    return this;
  }

  public int getActionType() {
    return actionType;
  }

  public String getResourceType() {
    return resourceType;
  }

  public List<String> getResourceIds() {
    return resourceIds;
  }

  public List<String> getComponentInstanceIds() {
    return componentInstanceIds;
  }

  public List<String> getUserIds() {
    return userIds;
  }

  public List<String> getExcludedUserIds() {
    return excludedUserIds;
  }

  public PaginationCriterion getPagination() {
    return pagination != null ? pagination.asCriterion() : null;
  }

  public List<QUERY_ORDER_BY> getOrderByList() {
    return orderByList;
  }
}
