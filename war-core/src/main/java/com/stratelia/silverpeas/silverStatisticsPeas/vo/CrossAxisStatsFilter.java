/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.silverStatisticsPeas.vo;

public class CrossAxisStatsFilter {
  private String monthBegin = null;
  private String yearBegin = null;
  private String monthEnd = null;
  private String yearEnd = null;
  private int firstAxisId = 0;
  private int secondAxisId = 0;


  /**
   * Default constructor
   * 
   * @param monthBegin
   * @param yearBegin
   * @param monthEnd
   * @param yearEnd
   * @param firstAxisId
   * @param secondAxisId
   */
  public CrossAxisStatsFilter(String monthBegin, String yearBegin, String monthEnd, String yearEnd,
      int firstAxisId, int secondAxisId) {
    super();
    this.monthBegin = monthBegin;
    this.yearBegin = yearBegin;
    this.monthEnd = monthEnd;
    this.yearEnd = yearEnd;
    this.firstAxisId = firstAxisId;
    this.secondAxisId = secondAxisId;
  }

  /**
   * @return the monthBegin
   */
  public String getMonthBegin() {
    return monthBegin;
  }

  /**
   * @param monthBegin the monthBegin to set
   */
  public void setMonthBegin(String monthBegin) {
    this.monthBegin = monthBegin;
  }

  /**
   * @return the yearBegin
   */
  public String getYearBegin() {
    return yearBegin;
  }

  /**
   * @param yearBegin the yearBegin to set
   */
  public void setYearBegin(String yearBegin) {
    this.yearBegin = yearBegin;
  }

  /**
   * @return the monthEnd
   */
  public String getMonthEnd() {
    return monthEnd;
  }

  /**
   * @param monthEnd the monthEnd to set
   */
  public void setMonthEnd(String monthEnd) {
    this.monthEnd = monthEnd;
  }

  /**
   * @return the yearEnd
   */
  public String getYearEnd() {
    return yearEnd;
  }

  /**
   * @param yearEnd the yearEnd to set
   */
  public void setYearEnd(String yearEnd) {
    this.yearEnd = yearEnd;
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
