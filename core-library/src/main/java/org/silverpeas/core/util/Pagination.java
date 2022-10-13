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

package org.silverpeas.core.util;

import org.silverpeas.core.admin.PaginationPage;

import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * @author silveryocha
 */
public class Pagination<T> {

  private final PaginationPage paginationPage;
  private Function<PaginationPage, SilverpeasList<T>> paginatedDataSource;
  private UnaryOperator<SilverpeasList<T>> filter;
  private int minPerPage = 0;
  private int factor = 5;
  private int nbMaxDataSourceCalls = 0;
  private boolean nbMaxDataSourceCallLimitReached = false;

  public Pagination(final PaginationPage paginationPage) {
    this.paginationPage = paginationPage;
  }

  /**
   * The factor applied on page size on the paginated data source in order to retrieve more
   * data in the aime to get enough for filtering operation.
   * @param factor a positive integer.
   * @return the process instance itself.
   */
  public Pagination<T> factor(final int factor) {
    this.factor = factor;
    return this;
  }

  /**
   * The minimum of data per page retrieved from a paginated datasource call.
   * @param minPerPage a minimum per page.
   * @return the process instance itself.
   */
  public Pagination<T> withMinPerPage(final int minPerPage) {
    this.minPerPage = minPerPage;
    return this;
  }

  /**
   * The maximum number of data source calls must be done.
   * <p>
   * zero or negative value means no limit.
   * </p>
   * @param nbMaxDataSourceCalls a maximum data source calls.
   * @return the process instance itself.
   */
  public Pagination<T> limitDataSourceCallsTo(final int nbMaxDataSourceCalls) {
    this.nbMaxDataSourceCalls = nbMaxDataSourceCalls;
    return this;
  }

  /**
   * Gets data by applying a pagination clause.
   * @param paginatedDataSource the directive of data querying with pagination clause.
   * @return the process instance itself.
   */
  public Pagination<T> paginatedDataSource(
      final Function<PaginationPage, SilverpeasList<T>> paginatedDataSource) {
    this.paginatedDataSource = paginatedDataSource;
    return this;
  }

  /**
   * Applying a filtering operation on data querying from the data source.
   * @param filter the filtering directive.
   * @return the process instance itself.
   */
  public Pagination<T> filter(final UnaryOperator<SilverpeasList<T>> filter) {
    this.filter = filter;
    return this;
  }

  /**
   * Execute the process of paginated data querying.
   * @return the result {@link SilverpeasList}.
   */
  public SilverpeasList<T> execute() {
    if (paginatedDataSource == null) {
      throw new IllegalArgumentException("paginatedDataSource must be defined");
    }
    if (factor <= 0) {
      throw new IllegalArgumentException("factor must be positive");
    }
    if (minPerPage < 0) {
      throw new IllegalArgumentException("minPerPage must be positive or equal to zero");
    }
    PaginationPage currentPagination = new PaginationPage(paginationPage.getPageNumber(),
        Math.max(paginationPage.getPageSize() * factor, minPerPage));
    if (!paginationPage.asCriterion().isOriginalSizeNeeded()) {
      currentPagination.originalSizeIsNotRequired();
    }
    SilverpeasList<T> result = null;
    boolean running = true;
    int nbDataSourceCalls = 0;
    while(running) {
      SilverpeasList<T> currentResult = paginatedDataSource.apply(currentPagination);
      nbDataSourceCalls++;
      if (currentResult.size() < paginationPage.getPageSize()) {
        running = false;
      }
      currentResult = filter.apply(currentResult);
      result = completeResult(currentResult, result);
      currentPagination = new PaginationPage(currentPagination.getPageNumber() + 1,
          currentPagination.getPageSize());
      if (!paginationPage.asCriterion().isOriginalSizeNeeded()) {
        currentPagination.originalSizeIsNotRequired();
      }
      if (result.size() >= paginationPage.getPageSize()) {
        running = false;
      } else if (nbDataSourceCalls == nbMaxDataSourceCalls) {
        running = false;
        nbMaxDataSourceCallLimitReached = true;
      }
    }
    return result;
  }

  /**
   * Indicates if the limit of number of data source call has been reached.
   * @see #limitDataSourceCallsTo(int)
   * @return true if reached, false otherwise.
   */
  public boolean isNbMaxDataSourceCallLimitReached() {
    return nbMaxDataSourceCallLimitReached;
  }

  private SilverpeasList<T> completeResult(final SilverpeasList<T> currentResult, final SilverpeasList<T> previousResult) {
    SilverpeasList<T> result = previousResult;
    if (result == null) {
      result = paginationPage.getPageSize() > 0 && currentResult.size() > paginationPage.getPageSize()
          ? currentResult.stream().limit(paginationPage.getPageSize()).collect(SilverpeasList.collector(currentResult))
          : currentResult;
    } else {
      for (int i = 0; i < currentResult.size() && result.size() < paginationPage.getPageSize(); i++) {
        result.add(currentResult.get(i));
      }
    }
    return result;
  }
}
