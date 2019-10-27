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

package org.silverpeas.core.contribution.publication.dao;

import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.util.CollectionUtil;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.silverpeas.core.contribution.publication.dao.PublicationCriteria.QUERY_ORDER_BY.*;

/**
 * Class that permits to set publication search criteria for publication services.
 * @author silveryocha
 */
public class PublicationCriteria {

  public enum QUERY_ORDER_BY {

    BEGIN_VISIBILITY_DATE_ASC("pubBeginDate", true), BEGIN_VISIBILITY_DATE_DESC("pubBeginDate", false),
    LAST_UPDATE_DATE_ASC("pubUpdateDate", true), LAST_UPDATE_DATE_DESC("pubUpdateDate", false),
    CREATION_DATE_ASC("pubId", true), CREATION_DATE_DESC("pubId", false);

    private final String propertyName;
    private final boolean asc;

    QUERY_ORDER_BY(final String propertyName, final boolean asc) {
      this.propertyName = propertyName;
      this.asc = asc;
    }

    public String getPropertyName() {
      return propertyName;
    }

    public boolean isAsc() {
      return asc;
    }
  }

  private boolean emptyResultIfNoComponentInstances = true;
  private final List<String> componentInstanceIds = new ArrayList<>();
  private final Set<String> statuses = new HashSet<>();
  private final Set<Integer> includedNodeIds = new HashSet<>();
  private final Set<Integer> excludedNodeIds = new HashSet<>();
  private final List<QUERY_ORDER_BY> orderByList = new ArrayList<>();
  private boolean mustHaveAtLeastOneNodeFather = false;
  private OffsetDateTime visibilityDate = null;
  private OffsetDateTime lastUpdatedSince = null;
  private PaginationPage pagination;

  private PublicationCriteria() {
  }

  /**
   * Initializes the criteria.
   * <p>
   * Please be careful against performances...
   * </p>
   * @return an instance of criteria.
   */
  public static PublicationCriteria onAllComponentInstances() {
    final PublicationCriteria criteria = new PublicationCriteria();
    criteria.emptyResultIfNoComponentInstances = false;
    return criteria;
  }

  /**
   * Initializes the criteria with component instance ids.
   * <p>
   * By security, if no ids are given, the service using the criteria will return directly an
   * empty list instead of performing the sql query.
   * </p>
   * @param componentInstanceIds identifiers of component instances.
   * @return an instance of criteria.
   */
  public static PublicationCriteria onComponentInstanceIds(final String... componentInstanceIds) {
    return onComponentInstanceIds(Stream.of(componentInstanceIds).collect(Collectors.toList()));
  }

  /**
   * Initializes the criteria with component instance ids.
   * <p>
   * By security, if no ids are given, the service using the criteria will return directly an
   * empty list instead of performing the sql query.
   * </p>
   * @param componentInstanceIds identifiers of component instances.
   * @return an instance of criteria.
   */
  public static PublicationCriteria onComponentInstanceIds(final Collection<String> componentInstanceIds) {
    final PublicationCriteria criteria = new PublicationCriteria();
    criteria.componentInstanceIds.addAll(componentInstanceIds);
    return criteria;
  }

  /**
   * Additionally filter on the trash node.
   * @see #onComponentInstanceIds(String...).
   */
  public static PublicationCriteria excludingTrashNodeOnComponentInstanceIds(
      final String... componentInstanceIds) {
    return onComponentInstanceIds(componentInstanceIds).excludingNodes(NodePK.BIN_NODE_ID);
  }

  /**
   * Additionally filter on the trash node.
   * @see #onComponentInstanceIds(Collection).
   */
  public static PublicationCriteria excludingTrashNodeOnComponentInstanceIds(
      final Collection<String> componentInstanceIds) {
    return onComponentInstanceIds(componentInstanceIds).excludingNodes(NodePK.BIN_NODE_ID);
  }

  /**
   * Sets the criteria of component node ids.
   * @param statuses statuses of publications.
   * @return itself.
   */
  public PublicationCriteria ofStatus(final String... statuses) {
    this.statuses.addAll(Stream.of(statuses).collect(Collectors.toList()));
    return this;
  }

  public PublicationCriteria mustHaveAtLeastOneNodeFather() {
    this.mustHaveAtLeastOneNodeFather = true;
    return this;
  }

  /**
   * Sets the criteria of component node ids.
   * @param nodeIds identifiers of nodes.
   * @return itself.
   */
  public PublicationCriteria onNodes(final String... nodeIds) {
    return onNodes(Stream.of(nodeIds).collect(Collectors.toList()));
  }

  /**
   * Sets the criteria of component node ids.
   * @param nodeIds identifiers of nodes.
   * @return itself.
   */
  public PublicationCriteria onNodes(final Collection<String> nodeIds) {
    this.includedNodeIds.addAll(nodeIds.stream().map(Integer::parseInt).collect(Collectors.toList()));
    return this;
  }

  /**
   * Sets the criteria of component node ids.
   * @param nodeIds identifiers of nodes.
   * @return itself.
   */
  public PublicationCriteria excludingNodes(final String... nodeIds) {
    return excludingNodes(Stream.of(nodeIds).collect(Collectors.toList()));
  }

  /**
   * Sets the criteria of component node ids.
   * @param nodeIds identifiers of nodes.
   * @return itself.
   */
  public PublicationCriteria excludingNodes(final Collection<String> nodeIds) {
    this.excludedNodeIds.addAll(nodeIds.stream().map(Integer::parseInt).collect(Collectors.toList()));
    return this;
  }

  /**
   * The local date from which the publication are visible.
   * @param visibilityDate a local date.
   * @return itself.
   */
  public PublicationCriteria visibleAt(final OffsetDateTime visibilityDate) {
    this.visibilityDate = visibilityDate;
    return this;
  }

  /**
   * The local date from which the publication have been updated.
   * @param lastUpdatedSince a local date.
   * @return itself.
   */
  public PublicationCriteria lastUpdatedSince(final OffsetDateTime lastUpdatedSince) {
    this.lastUpdatedSince = lastUpdatedSince;
    return this;
  }

  /**
   * Limit to a given number of result.
   * @param limit a number of result (<= 0 means no limit).
   * @return itself.
   */
  public PublicationCriteria limitTo(int limit) {
    paginateBy(limit > 0 ? new PaginationPage(1, limit).originalSizeIsNotRequired() : null);
    return this;
  }

  /**
   * Sets the criteria of pagination.
   * @param pagination the pagination.
   * @return itself.
   */
  public PublicationCriteria paginateBy(PaginationPage pagination) {
    this.pagination = pagination;
    return this;
  }

  /**
   * Configures the order by begin date.
   * @return itself.
   */
  public PublicationCriteria orderByDescendingBeginDate() {
    return orderBy(BEGIN_VISIBILITY_DATE_DESC).orderByDescendingLastUpdateDate();
  }

  /**
   * Configures the order by begin date.
   * @return itself.
   */
  public PublicationCriteria orderByDescendingLastUpdateDate() {
    return orderBy(LAST_UPDATE_DATE_DESC, CREATION_DATE_DESC);
  }

  /**
   * Configures the order of the user notification list.
   * @param orderBies the list of order by directives.
   * @return itself.
   */
  public PublicationCriteria orderBy(PublicationCriteria.QUERY_ORDER_BY... orderBies) {
    CollectionUtil.addAllIgnoreNull(this.orderByList, orderBies);
    return this;
  }

  boolean emptyResultWhenNoFilteringOnComponentInstances() {
    return emptyResultIfNoComponentInstances && componentInstanceIds.isEmpty();
  }

  boolean mustJoinOnNodeFatherTable() {
    return mustHaveAtLeastOneNodeFather || !includedNodeIds.isEmpty() || !excludedNodeIds.isEmpty();
  }

  List<String> getComponentInstanceIds() {
    return componentInstanceIds;
  }

  Set<String> getStatuses() {
    return statuses;
  }

  Set<Integer> getIncludedNodeIds() {
    return includedNodeIds;
  }

  OffsetDateTime getVisibilityDate() {
    return visibilityDate;
  }

  OffsetDateTime getLastUpdatedSince() {
    return lastUpdatedSince;
  }

  Set<Integer> getExcludedNodeIds() {
    return excludedNodeIds;
  }

  public PaginationPage getPagination() {
    return pagination;
  }

  List<QUERY_ORDER_BY> getOrderByList() {
    return orderByList;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", PublicationCriteria.class.getSimpleName() + "[", "]")
        .add("emptyResultIfNoComponentInstances=" + emptyResultIfNoComponentInstances)
        .add("componentInstanceIds=" + componentInstanceIds).add("statuses=" + statuses)
        .add("includedNodeIds=" + includedNodeIds).add("excludedNodeIds=" + excludedNodeIds)
        .add("orderByList=" + orderByList).add("visibilityDate=" + visibilityDate)
        .add("lastUpdatedSince=" + lastUpdatedSince).add("pagination=" + pagination).toString();
  }
}
