/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.node.coordinates.service;

import java.util.Collection;
import java.util.List;

import org.silverpeas.core.node.coordinates.model.Coordinate;
import org.silverpeas.core.node.coordinates.model.CoordinatePK;
import org.silverpeas.core.node.coordinates.model.CoordinatePoint;

public interface CoordinatesService {

  /**
   * Used only by the specific job'peas SmallAds This method must not be used by an another Job'peas
   * Instead, you must use getCoordinatesByFatherPaths()
   *
   * @param fatherIds
   * @param pk
   * @return
   * @
   * @see
   */
  public Collection<String> getCoordinatesByFatherIds(List<Integer> fatherIds, CoordinatePK pk);

  /**
   * @param fatherPaths
   * @param pk
   * @return
   */
  public Collection<String> getCoordinatesByFatherPaths(List<String> fatherPaths, CoordinatePK pk);

  /**
   * Method declaration
   *
   * @param pk
   * @param coordinatePoints
   * @return
   * @
   * @see
   */
  public int addCoordinate(CoordinatePK pk, List<CoordinatePoint> coordinatePoints);

  /**
   * Method declaration
   *
   * @param pk
   * @param coordinates
   * @
   * @see
   */
  public void deleteCoordinates(CoordinatePK pk, List<String> coordinates);

  /**
   * Method declaration
   *
   * @param pk
   * @param coordinatePoints
   * @
   * @see
   */
  public void deleteCoordinatesByPoints(CoordinatePK pk, List<String> coordinatePoints);

  /**
   * Method declaration
   *
   * @param coordinateIds
   * @param pk
   * @return
   * @
   * @see
   */
  public List<Coordinate> getCoordinatesByCoordinateIds(List<String> coordinateIds, CoordinatePK pk);

  /**
   * Method declaration
   *
   * @param pk
   * @param point
   * @
   * @see
   */
  public void addPointToAllCoordinates(CoordinatePK pk, CoordinatePoint point);

  /**
   * Method declaration
   *
   * @param pk
   * @param nodeId
   * @return
   * @
   * @see
   */
  public Collection<String> getCoordinateIdsByNodeId(CoordinatePK pk, String nodeId);
}
