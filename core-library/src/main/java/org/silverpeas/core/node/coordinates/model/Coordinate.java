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

package org.silverpeas.core.node.coordinates.model;

import java.io.Serializable;
import java.util.Collection;

/**
 * Class declaration
 * @author
 * @version %I%, %G%
 */
public class Coordinate implements Serializable {

  private int coordinateId;
  private Collection coordinatePoints;

  /**
   * Empty Constructor needed for mapping Castor
   */
  public Coordinate() {
  }

  /**
   * Constructor declaration
   * @param coordinateId
   * @param coordinatePoints
   * @see
   */
  public Coordinate(int coordinateId, Collection coordinatePoints) {
    this.coordinateId = coordinateId;
    this.coordinatePoints = coordinatePoints;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public int getCoordinateId() {
    return this.coordinateId;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public Collection getCoordinatePoints() {
    return this.coordinatePoints;
  }

  /**
   * Method declaration
   * @param coordinateId
   * @see
   */
  public void setCoordinateId(int coordinateId) {
    this.coordinateId = coordinateId;
  }

  /**
   * Method declaration
   * @param coordinatePoints
   * @see
   */
  public void setCoordinatePoints(Collection coordinatePoints) {
    this.coordinatePoints = coordinatePoints;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("Coordinate {\n");
    result.append("  getCoordinateId() = ").append(getCoordinateId()).append("\n");
    result.append("  getCoordinatePoints() = ").append(getCoordinatePoints().toString()).append(
        "\n");
    result.append("}");
    return result.toString();
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 97 * hash + this.coordinateId;
    hash = 97 * hash + (this.coordinatePoints != null ? this.coordinatePoints.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Coordinate other = (Coordinate) obj;
    if (this.coordinateId != other.coordinateId) {
      return false;
    }
    if (this.coordinatePoints != other.coordinatePoints && (this.coordinatePoints == null
        || !this.coordinatePoints.equals(other.coordinatePoints))) {
      return false;
    }
    return true;
  }

}
