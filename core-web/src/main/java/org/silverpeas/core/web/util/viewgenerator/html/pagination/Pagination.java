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
package org.silverpeas.core.web.util.viewgenerator.html.pagination;

import org.apache.commons.lang3.tuple.Pair;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.PaginationList;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.web.util.viewgenerator.html.SimpleGraphicElement;

import javax.portlet.RenderRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pagination is an interface to be implemented by a graphic element to print a pages index or a
 * elements counter.
 * @author neysseri
 */
public interface Pagination extends SimpleGraphicElement {
  String ITEMS_PER_PAGE_PARAM = "ItemsPerPage";
  String INDEX_PARAMETER_NAME = "PaginationPaneIndex";

  /**
   * Gets a new pagination page instance from given request and current pagination.
   * <p>If current pagination is null, a default one is taken into account</p>
   * @param request the request.
   * @param currentPagination the current pagination.
   * @return the new pagination page.
   */
  static PaginationPage getPaginationPageFrom(final RenderRequest request,
      final PaginationPage currentPagination) {
    final Map<String, String> parameters = new HashMap<>();
    request.getParameterMap().forEach((key, value) -> parameters.put(key, value[0]));
    return getPaginationPageFrom(parameters, currentPagination);
  }

  /**
   * Gets a new pagination page instance from given request and current pagination.
   * <p>If current pagination is null, a default one is taken into account</p>
   * @param request the request.
   * @param currentPagination the current pagination.
   * @return the new pagination page.
   */
  static PaginationPage getPaginationPageFrom(final HttpServletRequest request,
      final PaginationPage currentPagination) {
    final Map<String, String> parameters = new HashMap<>();
    request.getParameterMap().forEach((key, value) -> parameters.put(key, value[0]));
    return getPaginationPageFrom(parameters, currentPagination);
  }

  /**
   * Gets a new pagination page instance from given parameters and current pagination.
   * <p>If current pagination is null, a default one is taken into account</p>
   * @param params the pagination params.
   * @param currentPagination the current pagination.
   * @return the new pagination page.
   */
  static PaginationPage getPaginationPageFrom(final Map<String, String> params,
      final PaginationPage currentPagination) {
    final String pageSizeAsString = params.get(ITEMS_PER_PAGE_PARAM);
    final String itemIndexAsString = params.get(INDEX_PARAMETER_NAME);
    return AbstractPagination
        .getPaginationPageFrom(pageSizeAsString, itemIndexAsString, currentPagination);
  }

  /**
   * Gets the start index and the last index.
   * @return the indexes.
   */
  default Pair<Integer, Integer> getStartLastIndexes() {
    final int firstIndex = getIndexForCurrentPage();
    final int lastIndex;
    if (isLastPage()) {
      lastIndex = getLastItemIndex();
    } else {
      lastIndex = getIndexForNextPage();
    }
    return Pair.of(firstIndex, lastIndex);
  }

  default <T> SilverpeasList<T> getPaginatedListFrom(List<T> list) {
    if (list instanceof SilverpeasList && ((SilverpeasList<T>) list).isSlice()) {
      return (SilverpeasList<T>) list;
    }
    final Pair<Integer, Integer> indexes = getStartLastIndexes();
    final List<T> lightList = list.subList(indexes.getLeft(), indexes.getRight());
    return PaginationList.from(lightList, list.size());
  }

  void init(int nbItems, int nbItemsPerPage, int firstItemIndex);

  void setAltPreviousPage(String text);

  void setAltNextPage(String text);

  void setActionSuffix(String actionSuffix);

  int getIndexForPreviousPage();

  int getIndexForDirectPage(int page);

  int getIndexForCurrentPage();

  int getIndexForNextPage();

  boolean isLastPage();

  /**
   * Method declaration
   * @return
   * @see
   */
  String printIndex();

  String printIndex(String text);

  String printIndex(String text, boolean nbItemsPerPage);

  int getNbItems();

  int getFirstItemIndex();

  int getLastItemIndex();

  /**
   * Method declaration
   * @return
   * @see
   */
  String printCounter();

  void setBaseURL(String baseUrl);

  void setMultilang(LocalizationBundle multilang);
}