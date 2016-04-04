/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

/*
 * WADataPaginator.java
 *
 * Created on 26 mars 2001, 09:51
 */

package org.silverpeas.core.web.util.viewgenerator.html.arraypanes.pagination;

/**
 * This interface is used when pagination of large result sets is needed, mostly as part of the
 * rendering process.
 * @author jpouyadou
 * @version
 */
public interface WADataPaginator {
  /**
   * this method returns the next page of data, or null is there is no such page
   * @see #getPreviousPage
   */
  public WADataPage getNextPage();

  /**
   * this method returns the previous page of data, or null is there is no such page
   * @see #getNextPage
   */
  public WADataPage getPreviousPage();

  /**
   * this method returns the first page of data, or null is there is no such page. The next call to
   * getNextPage will thus return the second page.
   * @see #getNextPage
   * @see #getPreviousPage
   */
  public WADataPage getFirstPage();

  /**
   * this method returns the current page of data, or null is there is no such page.
   * @see #getNextPage
   * @see #getPreviousPage
   */
  public WADataPage getCurrentPage();

  /**
   * this method returns the last page of data, or null is there is no such page. T
   * @see #getNextPage
   * @see #getpreviousPage
   */
  public WADataPage getLastPage();

  /**
   * this method returns the count of pages in this set
   * @see #getNextPage
   * @see #getPreviousPage
   */
  public int getPageCount();

  /**
   * this method returns the total count of items
   */
  public int getItemCount();

  /**
   * This method retuns the current, 0-base page number, or -1 if there is no current page
   */
  public int getCurrentPageNumber();

  /**
   * This method sets the page size, that is, the count of items that fit on one page. Usually
   * called from the rendering process.
   */
  public void setPageSize(int size);

  /**
   * This method sets the header. The header is used mostly to know how to sort items
   */
  public void setHeader(WADataPaginatorHeader h);

  /*
   * this method returns the header set via the {@link #setHeader} method
   */
  public WADataPaginatorHeader getHeader();
}
