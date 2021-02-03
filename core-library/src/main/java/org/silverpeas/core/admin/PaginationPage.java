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
package org.silverpeas.core.admin;

import org.apache.commons.lang3.tuple.Pair;
import org.silverpeas.core.persistence.datasource.repository.PaginationCriterion;
import org.silverpeas.core.util.PaginationList;
import org.silverpeas.core.util.SilverpeasList;

import java.io.Serializable;
import java.util.List;
import java.util.StringJoiner;

/**
 * A page in a pagination of resources.
 * This bean is dedicated to be used with search criteria and permits also to handle pagination
 * context into UI pages.
 */
public class PaginationPage implements Serializable {
  private static final long serialVersionUID = -4691780072884201651L;

  private static final int DEFAULT_NB_ITEMS_PER_PAGE = 10;
  public static final PaginationPage DEFAULT = new PaginationPage(1, DEFAULT_NB_ITEMS_PER_PAGE);

  private int page;
  private final int count;
  private boolean originalSizeRequired = true;

  /**
   * Constructs a new page in a pagination mechanism.
   * @param pageNumber the number of the page.
   * @param pageSize the size in items of the page.
   */
  public PaginationPage(int pageNumber, int pageSize) {
    this.page = pageNumber;
    this.count = pageSize;
  }

  /**
   * Indicates that the caller does not require to use {@link SilverpeasList#originalListSize()}
   * method, so the original size of a result is not necessary.
   * @return itself.
   */
  public PaginationPage originalSizeIsNotRequired() {
    this.originalSizeRequired = false;
    return this;
  }

  /**
   * Gets this page number.
   * @return the page number.
   */
  public int getPageNumber() {
    return page;
  }

  /**
   * Gets the size of this page.
   * @return the count of items being in part of each page.
   */
  public int getPageSize() {
    return count;
  }

  /**
   * Converts the pagination page into a {@link PaginationCriterion} instance which represents the
   * criterion of pagination into the context of persistence.
   * @return the corresponding {@link PaginationCriterion} instance.
   */
  public PaginationCriterion asCriterion() {
    return new PaginationCriterion(getPageNumber(), getPageSize())
        .setOriginalSizeRequired(originalSizeRequired);
  }

  /**
   * Gets a paginated list from the given one by applying the pagination page context.<br>
   * If the list size is lower than the pagination page index, then pagination context is adjusted.
   * @param list the list to paginate.
   * @return the paginated list.
   */
  @SuppressWarnings("unchecked")
  public <T> SilverpeasList<T> getPaginatedListFrom(List<T> list) {
    final Pair<Integer, Integer> indexes = getStartLastIndexesFor(list);
    if (list instanceof SilverpeasList && ((SilverpeasList) list).isSlice()) {
      return (SilverpeasList) list;
    }
    final List lightList = list.subList(indexes.getLeft(), indexes.getRight());
    return PaginationList.from(lightList, list.size());
  }

  /**
   * Gets the start index and the last index.<br>
   * If the list size is lower than the pagination page index, then pagination context is adjusted.
   * @return the indexes.
   */
  private <T> Pair<Integer, Integer> getStartLastIndexesFor(List<T> list) {
    final SilverpeasList<T> silverpeasList = SilverpeasList.wrap(list);
    final int maxSize = (int) silverpeasList.originalListSize();
    int firstIndex = (getPageNumber() - 1) * getPageSize();
    if (firstIndex >= maxSize) {
      firstIndex = 0;
      page = 1;
    }
    final int lastIndex;
    final boolean isLastPage = firstIndex + getPageSize() >= maxSize;
    if (isLastPage) {
      lastIndex = maxSize;
    } else {
      lastIndex = firstIndex + getPageSize();
    }
    return Pair.of(firstIndex, lastIndex);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", PaginationPage.class.getSimpleName() + "[", "]")
        .add("page=" + page).add("count=" + count)
        .add("originalSizeRequired=" + originalSizeRequired).toString();
  }
}
