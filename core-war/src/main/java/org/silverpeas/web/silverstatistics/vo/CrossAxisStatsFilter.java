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

public class CrossAxisStatsFilter extends DateStatsFilter {
  private int firstAxisId = 0;
  private int secondAxisId = 0;

  /**
   * Default constructor
   * @param monthBegin
   * @param yearBegin
   * @param monthEnd
   * @param yearEnd
   * @param firstAxisId
   * @param secondAxisId
   */
  public CrossAxisStatsFilter(String monthBegin, String yearBegin, String monthEnd, String yearEnd,
      int firstAxisId, int secondAxisId) {
    super(monthBegin, yearBegin, monthEnd, yearEnd);
    this.firstAxisId = firstAxisId;
    this.secondAxisId = secondAxisId;
  }

  /**
   * @return the firstAxisId
   */
  public int getFirstAxisId() {
    return firstAxisId;
  }

  /**
   * @param firstAxisId the firstAxisId to set
   */
  public void setFirstAxisId(int firstAxisId) {
    this.firstAxisId = firstAxisId;
  }

  /**
   * @return the secondAxisId
   */
  public int getSecondAxisId() {
    return secondAxisId;
  }

  /**
   * @param secondAxisId the secondAxisId to set
   */
  public void setSecondAxisId(int secondAxisId) {
    this.secondAxisId = secondAxisId;
  }

}
