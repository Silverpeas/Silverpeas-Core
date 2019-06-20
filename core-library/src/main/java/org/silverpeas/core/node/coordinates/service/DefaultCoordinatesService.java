/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.node.coordinates.service;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.node.coordinates.model.Coordinate;
import org.silverpeas.core.node.coordinates.model.CoordinatePK;
import org.silverpeas.core.node.coordinates.model.CoordinatePoint;
import org.silverpeas.core.node.coordinates.model.CoordinateRuntimeException;
import org.silverpeas.core.node.coordinates.persistence.CoordinatesDAO;
import org.silverpeas.core.persistence.jdbc.DBUtil;

import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Class declaration
 *
 * @author
 * @version %I%, %G%
 */
@Transactional
public class DefaultCoordinatesService implements CoordinatesService, ComponentInstanceDeletion {

  protected DefaultCoordinatesService() {
  }

  private Connection getConnection() {
    try {
      return DBUtil.openConnection();
    } catch (Exception e) {
      throw new CoordinateRuntimeException(e);
    }
  }

  /**
   * Used only by the specific job'peas SmallAds This method must not be used by an another Job'peas
   * Instead, you must use getCoordinatesByFatherPaths()
   *
   * @param fatherIds
   * @param pk
   * @return
   * @
   *
   */
  @Override
  public Collection<String> getCoordinatesByFatherIds(List<Integer> fatherIds, CoordinatePK pk) {
    Connection con = getConnection();
    try {

      Collections.sort(fatherIds);

      return CoordinatesDAO.selectByFatherIds(con, fatherIds, pk);
    } catch (SQLException e) {
      throw new CoordinateRuntimeException("No coordinates available by father id", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<String> getCoordinatesByFatherPaths(List<String> fatherPaths, CoordinatePK pk) {
    Connection con = getConnection();
    try {
      return CoordinatesDAO.selectByFatherPaths(con, fatherPaths, pk);
    } catch (SQLException e) {
      throw new CoordinateRuntimeException("No coordinates available by father path", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param pk
   * @param coordinatePoints
   * @return
   * @
   *
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public int addCoordinate(CoordinatePK pk, List<CoordinatePoint> coordinatePoints) {

    Connection con = getConnection();
    try {
      return CoordinatesDAO.addCoordinate(con, pk, coordinatePoints);
    } catch (SQLException e) {
      throw new CoordinateRuntimeException("Coordinates adding failure", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param pk
   * @param coordinates
   * @
   *
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void deleteCoordinates(CoordinatePK pk, List<String> coordinates) {

    Connection con = getConnection();
    try {
      CoordinatesDAO.removeCoordinates(con, pk, coordinates);
    } catch (SQLException e) {
      throw new CoordinateRuntimeException("Coordinates deletion failure", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param pk
   * @param coordinatePoints
   * @
   *
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void deleteCoordinatesByPoints(CoordinatePK pk, List<String> coordinatePoints) {

    Connection con = getConnection();
    try {
      CoordinatesDAO.removeCoordinatesByPoints(con, pk, coordinatePoints);
    } catch (SQLException e) {
      throw new CoordinateRuntimeException("Coordinate deletion by points failure", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param coordinateIds
   * @param pk
   * @return
   * @
   *
   */
  @Override
  public List<Coordinate> getCoordinatesByCoordinateIds(List<String> coordinateIds, CoordinatePK pk) {

    Connection con = getConnection();
    try {
      return CoordinatesDAO.selectCoordinatesByCoordinateIds(con, coordinateIds, pk);
    } catch (SQLException e) {
      throw new CoordinateRuntimeException("No available coordinates", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param pk
   * @param point
   * @
   *
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void addPointToAllCoordinates(CoordinatePK pk, CoordinatePoint point) {

    Connection con = getConnection();
    try {
      CoordinatesDAO.addPointToAllCoordinates(con, pk, point);
    } catch (SQLException e) {
      throw new CoordinateRuntimeException("Points adding to coordinate failure", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param pk
   * @param nodeId
   * @return
   * @
   *
   */
  @Override
  public Collection<String> getCoordinateIdsByNodeId(CoordinatePK pk, String nodeId) {
    Connection con = getConnection();
    try {
      return CoordinatesDAO.getCoordinateIdsByNodeId(con, pk, nodeId);
    } catch (SQLException e) {
      throw new CoordinateRuntimeException("Coordinate by node id getting failure", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Deletes the resources belonging to the specified component instance. This method is invoked
   * by Silverpeas when a component instance is being deleted.
   * @param componentInstanceId the unique identifier of a component instance.
   */
  @Override
  @Transactional
  public void delete(final String componentInstanceId) {
    try (Connection connection = DBUtil.openConnection()) {
      CoordinatesDAO.removeCoordinatesByInstanceId(connection, componentInstanceId);
    } catch (SQLException e) {
      throw new SilverpeasRuntimeException(e.getMessage(), e);
    }
  }
}
