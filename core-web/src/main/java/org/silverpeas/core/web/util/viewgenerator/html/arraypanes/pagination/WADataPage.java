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
 * WADataPage.java
 *
 * Created on 26 mars 2001, 10:03
 */

package org.silverpeas.core.web.util.viewgenerator.html.arraypanes.pagination;

/**
 * @author jpouyadou
 * @version
 */
public interface WADataPage {
  /**
   * This method returns the count of items on the page
   */
  public int getItemCount();

  public WAItem getFirstItem();

  public WAItem getLastItem();

  public WAItem getNextItem();

  public WAItem getPreviousItem();

  public WAItem getItemByName(String name);

  /**
   * this method returns the index, <strong>relative to the parent document</strong> of the first
   * item on the page. This index is inclusive, that is, the item actually belongs to the page.
   * @see getEndIndex()
   */
  public int getStartItemDocumentIndex();

  /**
   * this method returns the index, <strong>relative to the parent document</strong> of the last
   * item on the page. This index is exclusive, that is, the item actually belongs to the next page
   * (this index is the index of the last visible item + 1).
   * @see getStartIndex()
   */
  public int getEndItemDocumentIndex();
}
