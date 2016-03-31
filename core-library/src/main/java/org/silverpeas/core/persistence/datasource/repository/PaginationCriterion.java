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
 * A pagination criterion. This criterion is aimed to a query for persisted entities in order to
 * limit the number of return of such entities.
 * @author mmoquillon
 */
public class PaginationCriterion {

  /**
   * This instance represents no pagination. It is the equivalent of the null value but in a better
   * typed way.
   */
  public static final PaginationCriterion NO_PAGINATION = new PaginationCriterion(-1, -1);

  private final int pageNumber;
  private final int itemCount;

  /**
   * Constructs a new pagination criterion from the specified page number and count of items
   * this page has to gather.
   * @param page the page number. It identifies in a pagination the page of items.
   * @param count the number of items to return within the page.
   */
  public PaginationCriterion(int page, int count) {
    this.pageNumber = page;
    this.itemCount = count;
  }

  /**
   * The page number.
   * @return a positive integer identifying the page of items to return.
   */
  public int getPageNumber() {
    return pageNumber;
  }

  /**
   * The maximum number of items the page has to contain.
   * @return the maximum number of items to return.
   */
  public int getItemCount() {
    return itemCount;
  }

  /**
   * Is this pagination criterion defined.
   * @return true if this pagination criterion isn't the NO_PAGINATION instance.
   */
  public boolean isDefined() {
    return !this.equals(NO_PAGINATION);
  }
}
