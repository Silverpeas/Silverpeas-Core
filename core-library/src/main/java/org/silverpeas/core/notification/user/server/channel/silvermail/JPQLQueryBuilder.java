/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.notification.user.server.channel.silvermail;

import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.persistence.datasource.model.identifier.UniqueLongIdentifier;
import org.silverpeas.core.persistence.datasource.repository.PaginationCriterion;
import org.silverpeas.core.persistence.datasource.repository.QueryCriteria;
import org.silverpeas.core.persistence.datasource.repository.SimpleQueryCriteria;
import org.silverpeas.core.persistence.datasource.repository.jpa.NamedParameters;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A dynamic builder of a JPQL query.
 * @author mmoquillon
 */
public class JPQLQueryBuilder implements SilvermailCriteriaProcessor {

  private StringBuilder orderBy = null;
  private boolean done = false;
  private final SimpleQueryCriteria jpqlCriteria;
  private String conjonction;

  public JPQLQueryBuilder(final NamedParameters parameters) {
    this.jpqlCriteria = new SimpleQueryCriteria(parameters);
  }

  @Override
  public void startProcessing() {
    // Nothing to do for now.
  }

  @Override
  public void endProcessing() {
    if (orderBy != null && orderBy.length() > 0) {
      jpqlCriteria.clause().add(orderBy.toString());
    }
    done = true;
  }

  @Override
  public QueryCriteria result() {
    return this.jpqlCriteria;
  }

  @Override
  public SilvermailCriteriaProcessor then() {
    if (!done && jpqlCriteria.clause().text().length() > 0) {
      conjonction = "and";
    }
    return this;
  }

  @Override
  public SilvermailCriteriaProcessor processByIds(final List<Long> ids) {
    if (!done) {
      List<UniqueLongIdentifier> convertedIds =
          ids.stream().map(i -> new UniqueLongIdentifier().fromString(String.valueOf(i)))
              .collect(Collectors.toList());
      jpqlCriteria.clause().add(conjonction).add("id = :ids").parameters().add("ids", convertedIds);
      conjonction = null;
    }
    return this;
  }

  @Override
  public SilvermailCriteriaProcessor processUserId(final long userId) {
    if (!done) {
      jpqlCriteria.clause().add(conjonction).add("userId = :userId").parameters()
          .add("userId", userId);
      conjonction = null;
    }
    return this;
  }

  @Override
  public SilvermailCriteriaProcessor processFolderId(final long folderId) {
    if (!done) {
      jpqlCriteria.clause().add(conjonction).add("folderId = :folderId").parameters()
          .add("folderId", folderId);
      conjonction = null;
    }
    return this;
  }

  @Override
  public SilvermailCriteriaProcessor processReadState(final int readState) {
    if (!done) {
      jpqlCriteria.clause().add(conjonction).add("readen = :readState").parameters()
          .add("readState", readState);
      conjonction = null;
    }
    return this;
  }

  @Override
  public SilvermailCriteriaProcessor processOrdering(
      final List<SilvermailCriteria.QUERY_ORDER_BY> orderings) {
    if (!done) {
      for (SilvermailCriteria.QUERY_ORDER_BY anOrdering : orderings) {
        if (orderBy == null) {
          orderBy = new StringBuilder("order by ");
        } else {
          orderBy.append(", ");
        }
        orderBy.append(anOrdering.getPropertyName());
        orderBy.append(" ");
        orderBy.append(anOrdering.isAsc() ? "asc" : "desc");
      }
      conjonction = null;
    }
    return this;
  }

  @Override
  public SilvermailCriteriaProcessor processPagination(PaginationPage pagination) {
    jpqlCriteria.withPagination(new PaginationCriterion(pagination.getPageNumber(), pagination.
        getPageSize()));
    conjonction = null;
    return this;
  }
}
