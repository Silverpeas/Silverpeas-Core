/*
 * Copyright (C) 2000-2013 Silverpeas
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
package org.silverpeas.core.admin;

/**
 * A page in a pagination of resources.
 * This bean is dedicated to be used with search criteria.
 */
public class PaginationPage {

  private final int page;
  private final int count;

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
}
