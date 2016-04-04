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

package org.silverpeas.core.web.util.viewgenerator.html.arraypanes;

/**
 * Class declaration
 * @author
 */
public class ArrayPaneStatusBean {
  private int firstVisibleLine = 0;
  private int maximumVisibleLine = 10;
  private int sortColumn = 0; // no column is sorted by default

  /**
   * Method declaration
   * @param firstVisibleLine
   * @see
   */
  public void setFirstVisibleLine(int firstVisibleLine) {
    this.firstVisibleLine = firstVisibleLine;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public int getFirstVisibleLine() {
    return firstVisibleLine;
  }

  /**
   * Method declaration
   * @param maximumVisibleLine
   * @see
   */
  public void setMaximumVisibleLine(int maximumVisibleLine) {
    this.maximumVisibleLine = maximumVisibleLine;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public int getMaximumVisibleLine() {
    return maximumVisibleLine;
  }

  /**
   * Method declaration
   * @param sortColumn
   * @see
   */
  public void setSortColumn(int sortColumn) {
    this.sortColumn = sortColumn;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public int getSortColumn() {
    return sortColumn;
  }
}