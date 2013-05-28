/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.stratelia.webactiv.util.coordinates.control;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.coordinates.ejb.CoordinatesDAO;
import com.stratelia.webactiv.util.coordinates.model.Coordinate;
import com.stratelia.webactiv.util.coordinates.model.CoordinatePK;
import com.stratelia.webactiv.util.coordinates.model.CoordinatePoint;
import com.stratelia.webactiv.util.coordinates.model.CoordinateRuntimeException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

/**
 * Class declaration
 *
 * @author
 * @version %I%, %G%
 */
@Stateless(name = "Coordinates", description = "Stateless EJB to manage coordinates.")
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class CoordinatesBmEJB implements CoordinatesBm {

  private static final long serialVersionUID = -6692122009364112596L;

  public CoordinatesBmEJB() {
  }

  private Connection getConnection() {
    try {
      return DBUtil.makeConnection(JNDINames.PUBLICATION_DATASOURCE);
    } catch (Exception e) {
      throw new CoordinateRuntimeException("CoordinatesBmEJB.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
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
   * @see
   */
  @Override
  public Collection<String> getCoordinatesByFatherIds(List<Integer> fatherIds, CoordinatePK pk) {
    Connection con = getConnection();
    try {
      SilverTrace.info("coordinates", "CoordinatesBmEJB.getCoordinatesByFatherIds()",
          "root.MSG_GEN_PARAM_VALUE", "fatherIds BEFORE sorting : " + fatherIds);
      Collections.sort(fatherIds);
      SilverTrace.info("coordinates", "CoordinatesBmEJB.getCoordinatesByFatherIds()",
          "root.MSG_GEN_PARAM_VALUE", "fatherIds AFTER sorting : " + fatherIds);
      return CoordinatesDAO.selectByFatherIds(con, fatherIds, pk);
    } catch (SQLException e) {
      throw new CoordinateRuntimeException("CoordinatesBmEJB.getCoordinatesByFatherIds()",
          SilverpeasRuntimeException.ERROR, "coordinates.COORDINATES_LIST_NOT_AVAILABLE", e);
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
      throw new CoordinateRuntimeException("CoordinatesBmEJB.getCoordinatesByFatherPaths()",
          SilverpeasRuntimeException.ERROR, "coordinates.COORDINATES_LIST_NOT_AVAILABLE", e);
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
   * @see
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public int addCoordinate(CoordinatePK pk, List<CoordinatePoint> coordinatePoints) {
    SilverTrace.info("coordinates", "CoordinatesBmEJB.addCoordinate()", "root.MSG_GEN_PARAM_VALUE",
        "coordinatePoints = " + coordinatePoints);
    Connection con = getConnection();
    try {
      return CoordinatesDAO.addCoordinate(con, pk, coordinatePoints);
    } catch (SQLException e) {
      throw new CoordinateRuntimeException("CoordinatesBmEJB.addCoordinate()",
          SilverpeasRuntimeException.ERROR,
          "coordinates.ADDING_COORDINATE_FAILED", e);
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
   * @see
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void deleteCoordinates(CoordinatePK pk, List<String> coordinates) {
    SilverTrace.info("coordinates", "CoordinatesBmEJB.deleteCoordinates()",
        "root.MSG_GEN_PARAM_VALUE", "coordinates = " + coordinates);
    Connection con = getConnection();
    try {
      CoordinatesDAO.removeCoordinates(con, pk, coordinates);
    } catch (SQLException e) {
      throw new CoordinateRuntimeException("CoordinatesBmEJB.deleteCoordinates()",
          SilverpeasRuntimeException.ERROR, "coordinates.DELETING_COORDINATES_FAILED", e);
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
   * @see
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void deleteCoordinatesByPoints(CoordinatePK pk, List<String> coordinatePoints) {
    SilverTrace.info("coordinates", "CoordinatesBmEJB.deleteCoordinatesByPoints()",
        "root.MSG_GEN_PARAM_VALUE", "coordinatePoints = " + coordinatePoints);
    Connection con = getConnection();
    try {
      CoordinatesDAO.removeCoordinatesByPoints(con, pk, coordinatePoints);
    } catch (SQLException e) {
      throw new CoordinateRuntimeException("CoordinatesBmEJB.deleteCoordinatesByPoints()",
          SilverpeasRuntimeException.ERROR, "coordinates.DELETING_COORDINATES_BY_POINTS_FAILED", e);
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
   * @see
   */
  @Override
  public List<Coordinate> getCoordinatesByCoordinateIds(List<String> coordinateIds, CoordinatePK pk) {
    SilverTrace.info("coordinates", "CoordinatesBmEJB.getCoordinatesByCoordinateIds()",
        "root.MSG_GEN_PARAM_VALUE", "coordinateIds = " + coordinateIds);
    Connection con = getConnection();
    try {
      return CoordinatesDAO.selectCoordinatesByCoordinateIds(con, coordinateIds, pk);
    } catch (SQLException e) {
      throw new CoordinateRuntimeException("CoordinatesBmEJB.getCoordinatesByCoordinateIds()",
          SilverpeasRuntimeException.ERROR, "coordinates.COORDINATES_LIST_NOT_AVAILABLE", e);
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
   * @see
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void addPointToAllCoordinates(CoordinatePK pk, CoordinatePoint point) {
    SilverTrace.info("coordinates", "CoordinatesBmEJB.addPointToAllCoordinates()",
        "root.MSG_GEN_PARAM_VALUE", "point = " + point);
    Connection con = getConnection();
    try {
      CoordinatesDAO.addPointToAllCoordinates(con, pk, point);
    } catch (SQLException e) {
      throw new CoordinateRuntimeException("CoordinatesBmEJB.addPointToAllCoordinates()",
          SilverpeasRuntimeException.ERROR, "coordinates.ADDING_A_POINT_TO_COORDINATES_FAILED", e);
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
   * @see
   */
  @Override
  public Collection<String> getCoordinateIdsByNodeId(CoordinatePK pk, String nodeId) {
    Connection con = getConnection();
    try {
      return CoordinatesDAO.getCoordinateIdsByNodeId(con, pk, nodeId);
    } catch (SQLException e) {
      throw new CoordinateRuntimeException("CoordinatesBmEJB.getCoordinateIdsByNodeId()",
          SilverpeasRuntimeException.ERROR, "coordinates.COORDINATES_LIST_BY_POINTS_NOT_AVAILABLE",
          e);
    } finally {
      DBUtil.close(con);
    }
  }
}
