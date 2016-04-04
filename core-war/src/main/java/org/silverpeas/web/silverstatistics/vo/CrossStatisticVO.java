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

package org.silverpeas.web.silverstatistics.vo;

import java.util.List;

public class CrossStatisticVO {
  private List<String> columnHeader = null;
  private List<String> firstRow = null;
  private List<List<CrossAxisAccessVO>> statsArray = null;

  /**
   * @param columnHeader
   * @param firstRow
   * @param statsArray
   */
  public CrossStatisticVO(List<String> columnHeader, List<String> firstRow,
      List<List<CrossAxisAccessVO>> statsArray) {
    super();
    this.columnHeader = columnHeader;
    this.firstRow = firstRow;
    this.statsArray = statsArray;
  }

  /**
   * @return the columnHeader
   */
  public List<String> getColumnHeader() {
    return columnHeader;
  }

  /**
   * @param columnHeader the columnHeader to set
   */
  public void setColumnHeader(List<String> columnHeader) {
    this.columnHeader = columnHeader;
  }

  /**
   * @return the firstRow
   */
  public List<String> getFirstRow() {
    return firstRow;
  }

  /**
   * @param firstRow the firstRow to set
   */
  public void setFirstRow(List<String> firstRow) {
    this.firstRow = firstRow;
  }

  /**
   * @return the statsArray
   */
  public List<List<CrossAxisAccessVO>> getStatsArray() {
    return statsArray;
  }

  /**
   * @param statsArray the statsArray to set
   */
  public void setStatsArray(List<List<CrossAxisAccessVO>> statsArray) {
    this.statsArray = statsArray;
  }

}
