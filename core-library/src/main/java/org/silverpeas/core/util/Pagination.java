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

package org.silverpeas.core.util;

import org.silverpeas.core.admin.PaginationPage;

import java.util.function.Function;

/**
 * @author silveryocha
 */
public class Pagination<T, R extends SilverpeasList<T>> {

  final PaginationPage pagination;
  private Function<PaginationPage, R> paginatedDataSource;
  private Function<R, R> filter;
  private int minPerPage = 0;
  private int factor = 5;

  public Pagination(final PaginationPage pagination) {
    this.pagination = pagination;
  }

  /**
   * The factor applied on page size on the paginated data source in order to retrieve more
   * data in the aime to get enough for filtering operation.
   * @param factor a positive integer.
   * @return the process instance itself.
   */
  public Pagination<T, R> factor(final int factor) {
    this.factor = factor;
    return this;
  }

  /**
   * The minimum of data per page retrieved from a paginated datasource call.
   * @param minPerPage a minimum per page.
   * @return the process instance itself.
   */
  public Pagination<T, R> withMinPerPage(final int minPerPage) {
    this.minPerPage = minPerPage;
    return this;
  }

  /**
   * Gets data by applying a pagination clause.
   * @param paginatedDataSource the directive of data querying with pagination clause.
   * @return the process instance itself.
   */
  public Pagination<T, R> paginatedDataSource(
      final Function<PaginationPage, R> paginatedDataSource) {
    this.paginatedDataSource = paginatedDataSource;
    return this;
  }

  /**
   * Applying a filtering operation on data querying from the data source.
   * @param filter the filtering directive.
   * @return the process instance itself.
   */
  public Pagination<T, R> filter(final Function<R, R> filter) {
    this.filter = filter;
    return this;
  }

  /**
   * Execute the process of paginated data querying.
   * @return the result {@link SilverpeasList}.
   */
  @SuppressWarnings("unchecked")
  public R execute() {
    if (paginatedDataSource == null) {
      throw new IllegalArgumentException("paginatedDataSource must be defined");
    }
    if (factor <= 0) {
      throw new IllegalArgumentException("factor must be positive");
    }
    if (minPerPage < 0) {
      throw new IllegalArgumentException("minPerPage must be positive or equal to zero");
    }
    PaginationPage currentPagination = new PaginationPage(pagination.getPageNumber(),
        Math.max(pagination.getPageSize() * factor, minPerPage));
    R result = null;
    boolean running = true;
    while(running) {
      R currentResult = paginatedDataSource.apply(currentPagination);
      if (currentResult.size() < pagination.getPageSize()) {
        running = false;
      }
      currentResult = filter.apply(currentResult);
      result = completeResult(currentResult, result);
      currentPagination = new PaginationPage(currentPagination.getPageNumber() + 1,
          currentPagination.getPageSize());
      if (result.size() >= pagination.getPageSize()) {
        running = false;
      }
    }
    return result;
  }

  private R completeResult(final R currentResult, final R previousResult) {
    R result = previousResult;
    if (result == null) {
      result = pagination.getPageSize() > 0 && currentResult.size() > pagination.getPageSize()
          ? currentResult.stream().limit(pagination.getPageSize()).collect(SilverpeasList.collector(currentResult))
          : currentResult;
    } else {
      for (int i = 0; i < currentResult.size() && result.size() < pagination.getPageSize(); i++) {
        result.add(currentResult.get(i));
      }
    }
    return result;
  }
}
