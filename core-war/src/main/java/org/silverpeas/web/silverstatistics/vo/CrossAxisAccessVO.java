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

public class CrossAxisAccessVO {
  private int firstAxisId = 0;
  private int secondAxisId = 0;
  private String firstAxisValue = null;
  private String secondAxisValue = null;
  private int nbAccess = 0;

  /**
   * @param firstAxisId
   * @param secondAxisId
   * @param firstAxisValue
   * @param secondAxisValue
   * @param nbAccess
   */
  public CrossAxisAccessVO(int firstAxisId, int secondAxisId, String firstAxisValue,
      String secondAxisValue, int nbAccess) {
    super();
    this.firstAxisId = firstAxisId;
    this.secondAxisId = secondAxisId;
    this.firstAxisValue = firstAxisValue;
    this.secondAxisValue = secondAxisValue;
    this.nbAccess = nbAccess;
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

  /**
   * @return the firstAxisValue
   */
  public String getFirstAxisValue() {
    return firstAxisValue;
  }

  /**
   * @param firstAxisValue the firstAxisValue to set
   */
  public void setFirstAxisValue(String firstAxisValue) {
    this.firstAxisValue = firstAxisValue;
  }

  /**
   * @return the secondAxisValue
   */
  public String getSecondAxisValue() {
    return secondAxisValue;
  }

  /**
   * @param secondAxisValue the secondAxisValue to set
   */
  public void setSecondAxisValue(String secondAxisValue) {
    this.secondAxisValue = secondAxisValue;
  }

  /**
   * @return the nbAccess
   */
  public int getNbAccess() {
    return nbAccess;
  }

  /**
   * @param nbAccess the nbAccess to set
   */
  public void setNbAccess(int nbAccess) {
    this.nbAccess = nbAccess;
  }

}
