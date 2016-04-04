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

public class StatisticVO {
  private String axisId = null;
  /**
   * axisValue represents axis full path
   */
  private String axisValue = null;
  private int axisLevel = 0;
  private String axisName = null;
  private String axisDescription = null;
  private int nbAccess = 0;

  /**
   * Default constructor
   * @param axisName
   * @param axisDescription
   * @param nbAccess
   */
  public StatisticVO(String axisId, String axisName, String axisDescription, int nbAccess) {
    super();
    this.axisId = axisId;
    this.axisName = axisName;
    this.axisDescription = axisDescription;
    this.nbAccess = nbAccess;
  }

  /**
   * @return the axisName
   */
  public String getAxisName() {
    return axisName;
  }

  /**
   * @param axisName the axisName to set
   */
  public void setAxisName(String axisName) {
    this.axisName = axisName;
  }

  /**
   * @return the axisDescription
   */
  public String getAxisDescription() {
    return axisDescription;
  }

  /**
   * @param axisDescription the axisDescription to set
   */
  public void setAxisDescription(String axisDescription) {
    this.axisDescription = axisDescription;
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

  /**
   * @return the axisId
   */
  public String getAxisId() {
    return axisId;
  }

  /**
   * @param axisId the axisId to set
   */
  public void setAxisId(String axisId) {
    this.axisId = axisId;
  }

  /**
   * @return the axisValue
   */
  public String getAxisValue() {
    return axisValue;
  }

  /**
   * @param axisValue the axisValue to set
   */
  public void setAxisValue(String axisValue) {
    this.axisValue = axisValue;
  }

  /**
   * @return the axisLevel
   */
  public int getAxisLevel() {
    return axisLevel;
  }

  /**
   * @param axisLevel the axisLevel to set
   */
  public void setAxisLevel(int axisLevel) {
    this.axisLevel = axisLevel;
  }

}
