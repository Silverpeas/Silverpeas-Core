/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.web.util.viewgenerator.html.pagination;

import org.silverpeas.core.admin.PaginationPage;

/**
 * @author Yohann Chastagnier
 */
public class PaginationUtil {

  /**
   * Hidden constructor.
   */
  private PaginationUtil() {
  }

  /**
   * Centralizes the formatting of a pagniation context label.
   * @param nbItemsPerPage the number of items displayed in a page.
   * @param totalNumberOfItems the total number of items that can be displayed.
   * @param firstItemIndexOfCurrentPage the index in the list of items of the first item to display
   * in the current page.
   * @return the counter as string.
   */
  public static String formatFromFirstIndexOfItem(int nbItemsPerPage, int totalNumberOfItems,
      int firstItemIndexOfCurrentPage) {
    StringBuilder result = new StringBuilder();
    if (totalNumberOfItems <= firstItemIndexOfCurrentPage || totalNumberOfItems <= nbItemsPerPage) {
      result.append(totalNumberOfItems).append(" ");
    } else {
      int end = firstItemIndexOfCurrentPage + nbItemsPerPage;
      if (end > totalNumberOfItems) {
        end = totalNumberOfItems;
      }
      result.append(firstItemIndexOfCurrentPage + 1).append(" - ").append(end).append(" / ")
          .append(totalNumberOfItems).append(" ");
    }
    return result.toString();
  }

  /**
   * Centralizes the formatting of a pagniation context label.
   * @param paginationPage the pagination page.
   * @param totalNumberOfItems the total number of items that can be displayed.
   * in the current page.
   * @return the counter as string.
   */
  public static String formatFromFirstIndexOfItem(PaginationPage paginationPage,
      int totalNumberOfItems) {
    final int firstIndex = (paginationPage.getPageNumber() - 1) * paginationPage.getPageSize();
    return formatFromFirstIndexOfItem(paginationPage.getPageSize(), totalNumberOfItems, firstIndex);
  }
}
