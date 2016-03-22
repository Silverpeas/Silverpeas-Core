/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.web.util.viewgenerator.html.pagination;

/**
 * @author: Yohann Chastagnier
 */
public class PaginationUtil {

  /**
   * Centralizes the formatting of a pagniation context label.
   * @param nbItemsPerPage the number of items displayed in a page.
   * @param totalNumberOfItems the total number of items that can be displayed.
   * @param firstItemIndexOfCurrentPage the index in the list of items of the first item to display
   * in the current page.
   * @return
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
   * Centralizes the formatting of a pagination context label.
   * @param nbItemsPerPage the number of items displayed in a page.
   * @param totalNumberOfItems the total number of items that can be displayed.
   * @param indexOfCurrentPage the index of the current page.
   * @return
   */
  public static String formatFromCurrentPageIndex(int nbItemsPerPage, int totalNumberOfItems,
      int indexOfCurrentPage) {
    return formatFromFirstIndexOfItem(nbItemsPerPage, totalNumberOfItems,
        (nbItemsPerPage * indexOfCurrentPage));
  }

  /**
   * Counts the total number of pages that must be displayed.
   * @param nbItemsPerPage the number of items displayed in a page.
   * @param totalNumberOfItems the total number of items that can be displayed.
   * @return the total number of pages.
   */
  public static int countTotalNumberOfPages(int nbItemsPerPage, int totalNumberOfItems) {
    if (nbItemsPerPage < 1 || totalNumberOfItems < 1) {
      return 1;
    }
    return ((totalNumberOfItems - 1) / nbItemsPerPage) + 1;
  }

  /**
   * Gets the page index.
   * @param nbItemsPerPage the number of items displayed in a page.
   * @param indexOfCurrentPage the index in the list of items of the first item to display
   * @return the page index.
   */
  public static int getPageIndexFromFirstDisplayedItemIndex(int nbItemsPerPage,
      int indexOfCurrentPage) {
    if (nbItemsPerPage < 1 || indexOfCurrentPage < 0) {
      return 0;
    }
    return indexOfCurrentPage / nbItemsPerPage;
  }
}
