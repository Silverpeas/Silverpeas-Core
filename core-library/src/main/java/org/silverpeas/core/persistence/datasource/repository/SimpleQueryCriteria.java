/*
 * Copyright (C) 2000-2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.persistence.datasource.repository;

/**
 * A simple implementation of the {@link org.silverpeas.core.persistence.datasource.repository.QueryCriteria}
 * interface. It provides just a {@link org.silverpeas.core.persistence.datasource.repository.QueryCriteria.Clause}
 * instance to add each criterion as text representation.
 * @author mmoquillon
 */
public class SimpleQueryCriteria implements QueryCriteria {

  private final Clause clause;
  private PaginationCriterion pagination = PaginationCriterion.NO_PAGINATION;

  /**
   * Constructs a simple query criteria with the specified parameters to use when defining the
   * different clauses of the criteria.
   * @param <T> the concrete type of the parameters.
   * @param parameters the parameters to use when setting the different clauses.
   */
  public <T extends Parameters> SimpleQueryCriteria(T parameters) {
    this.clause = new Clause(parameters);
  }

  /**
   * Adds a criterion on the pagination to apply on the query for entities.
   * @param pagination the pagination criterion.
   * @return itself with criterion on a pagination.
   */
  public SimpleQueryCriteria withPagination(final PaginationCriterion pagination) {
    this.pagination = pagination;
    return this;
  }

  @Override
  public PaginationCriterion pagination() {
    return this.pagination;
  }

  @Override
  public Clause clause() {
    return this.clause;
  }

}
