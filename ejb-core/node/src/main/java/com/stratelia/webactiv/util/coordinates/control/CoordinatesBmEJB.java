/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.stratelia.webactiv.util.coordinates.control;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.ejb.SessionContext;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.coordinates.ejb.CoordinatesDAO;
import com.stratelia.webactiv.util.coordinates.model.CoordinatePK;
import com.stratelia.webactiv.util.coordinates.model.CoordinatePoint;
import com.stratelia.webactiv.util.coordinates.model.CoordinateRuntimeException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import java.util.List;

/**
 * Class declaration
 * @author
 * @version %I%, %G%
 */
public class CoordinatesBmEJB implements javax.ejb.SessionBean, CoordinatesBmBusinessSkeleton {
  private static final long serialVersionUID = -6692122009364112596L;

  private String dbName = JNDINames.PUBLICATION_DATASOURCE;

  /**
   * Constructor declaration
   * @see
   */
  public CoordinatesBmEJB() {
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  private Connection getConnection() {
    try {
      return DBUtil.makeConnection(dbName);
    } catch (Exception e) {
      throw new CoordinateRuntimeException("CoordinatesBmEJB.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  /**
   * Method declaration
   * @param con
   * @see
   */
  private void freeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        SilverTrace.error("coordinates", "CoordinatesBmEJB.freeConnection()",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  /**
   * Used only by the specific job'peas SmallAds This method must not be used by an another Job'peas
   * Instead, you must use getCoordinatesByFatherPaths()
   * @param fatherIds
   * @param pk
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection getCoordinatesByFatherIds(ArrayList fatherIds,
      CoordinatePK pk) throws RemoteException {
    Connection con = getConnection();
    Collection coordinates = null;

    try {
      SilverTrace.info("coordinates",
          "CoordinatesBmEJB.getCoordinatesByFatherIds()",
          "root.MSG_GEN_PARAM_VALUE", "fatherIds BEFORE sorting : "
          + fatherIds.toString());
      Collections.sort(fatherIds);
      SilverTrace.info("coordinates",
          "CoordinatesBmEJB.getCoordinatesByFatherIds()",
          "root.MSG_GEN_PARAM_VALUE", "fatherIds AFTER sorting : "
          + fatherIds.toString());
      coordinates = CoordinatesDAO.selectByFatherIds(con, fatherIds, pk);
    } catch (Exception e) {
      throw new CoordinateRuntimeException(
          "CoordinatesBmEJB.getCoordinatesByFatherIds()",
          SilverpeasRuntimeException.ERROR,
          "coordinates.COORDINATES_LIST_NOT_AVAILABLE", e);
    } finally {
      freeConnection(con);
    }
    return coordinates;
  }

  public Collection getCoordinatesByFatherPaths(ArrayList fatherPaths,
      CoordinatePK pk) throws RemoteException {
    Connection con = getConnection();
    Collection coordinates = null;

    try {
      coordinates = CoordinatesDAO.selectByFatherPaths(con, fatherPaths, pk);
    } catch (Exception e) {
      throw new CoordinateRuntimeException(
          "CoordinatesBmEJB.getCoordinatesByFatherPaths()",
          SilverpeasRuntimeException.ERROR,
          "coordinates.COORDINATES_LIST_NOT_AVAILABLE", e);
    } finally {
      freeConnection(con);
    }
    return coordinates;
  }

  /**
   * Method declaration
   * @param pk
   * @param coordinatePoints
   * @return
   * @throws RemoteException
   * @see
   */
  @Override
  public int addCoordinate(CoordinatePK pk, List coordinatePoints)
      throws RemoteException {
    SilverTrace.info("coordinates", "CoordinatesBmEJB.addCoordinate()",
        "root.MSG_GEN_PARAM_VALUE", "coordinatePoints = " + coordinatePoints.toString());
    Connection con = getConnection();
    int coordinateId;
    try {
      coordinateId = CoordinatesDAO.addCoordinate(con, pk, coordinatePoints);
    } catch (Exception e) {
      throw new CoordinateRuntimeException("CoordinatesBmEJB.addCoordinate()",
          SilverpeasRuntimeException.ERROR,
          "coordinates.ADDING_COORDINATE_FAILED", e);
    } finally {
      freeConnection(con);
    }
    return coordinateId;
  }

  /**
   * Method declaration
   * @param pk
   * @param coordinates
   * @throws RemoteException
   * @see
   */
  public void deleteCoordinates(CoordinatePK pk, ArrayList coordinates)
      throws RemoteException {
    SilverTrace.info("coordinates", "CoordinatesBmEJB.deleteCoordinates()",
        "root.MSG_GEN_PARAM_VALUE", "coordinates = " + coordinates.toString());
    Connection con = getConnection();

    try {
      CoordinatesDAO.removeCoordinates(con, pk, coordinates);
    } catch (Exception e) {
      throw new CoordinateRuntimeException(
          "CoordinatesBmEJB.deleteCoordinates()",
          SilverpeasRuntimeException.ERROR,
          "coordinates.DELETING_COORDINATES_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Method declaration
   * @param pk
   * @param coordinatePoints
   * @throws RemoteException
   * @see
   */
  public void deleteCoordinatesByPoints(CoordinatePK pk,
      ArrayList coordinatePoints) throws RemoteException {
    SilverTrace.info("coordinates",
        "CoordinatesBmEJB.deleteCoordinatesByPoints()",
        "root.MSG_GEN_PARAM_VALUE", "coordinatePoints = "
        + coordinatePoints.toString());
    Connection con = getConnection();

    try {
      CoordinatesDAO.removeCoordinatesByPoints(con, pk, coordinatePoints);
    } catch (Exception e) {
      throw new CoordinateRuntimeException(
          "CoordinatesBmEJB.deleteCoordinatesByPoints()",
          SilverpeasRuntimeException.ERROR,
          "coordinates.DELETING_COORDINATES_BY_POINTS_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Method declaration
   * @param coordinateIds
   * @param pk
   * @return
   * @throws RemoteException
   * @see
   */
  public ArrayList getCoordinatesByCoordinateIds(ArrayList coordinateIds,
      CoordinatePK pk) throws RemoteException {
    SilverTrace.info("coordinates",
        "CoordinatesBmEJB.getCoordinatesByCoordinateIds()",
        "root.MSG_GEN_PARAM_VALUE", "coordinateIds = "
        + coordinateIds.toString());
    Connection con = getConnection();

    try {
      return CoordinatesDAO.selectCoordinatesByCoordinateIds(con,
          coordinateIds, pk);
    } catch (Exception e) {
      throw new CoordinateRuntimeException(
          "CoordinatesBmEJB.getCoordinatesByCoordinateIds()",
          SilverpeasRuntimeException.ERROR,
          "coordinates.COORDINATES_LIST_NOT_AVAILABLE", e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Method declaration
   * @param pk
   * @param point
   * @throws RemoteException
   * @see
   */
  public void addPointToAllCoordinates(CoordinatePK pk, CoordinatePoint point)
      throws RemoteException {
    SilverTrace.info("coordinates",
        "CoordinatesBmEJB.addPointToAllCoordinates()",
        "root.MSG_GEN_PARAM_VALUE", "point = " + point.toString());
    Connection con = getConnection();

    try {
      CoordinatesDAO.addPointToAllCoordinates(con, pk, point);
    } catch (Exception e) {
      throw new CoordinateRuntimeException(
          "CoordinatesBmEJB.addPointToAllCoordinates()",
          SilverpeasRuntimeException.ERROR,
          "coordinates.ADDING_A_POINT_TO_COORDINATES_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Method declaration
   * @param pk
   * @param nodeId
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection getCoordinateIdsByNodeId(CoordinatePK pk, String nodeId)
      throws RemoteException {
    Connection con = getConnection();
    Collection coordinateIds = null;

    try {
      coordinateIds = CoordinatesDAO.getCoordinateIdsByNodeId(con, pk, nodeId);
    } catch (Exception e) {
      throw new CoordinateRuntimeException(
          "CoordinatesBmEJB.getCoordinateIdsByNodeId()",
          SilverpeasRuntimeException.ERROR,
          "coordinates.COORDINATES_LIST_BY_POINTS_NOT_AVAILABLE", e);
    } finally {
      freeConnection(con);
    }
    return coordinateIds;
  }

  /**
   * Method declaration
   * @see
   */
  public void ejbCreate() {
  }

  /**
   * Method declaration
   * @see
   */
  public void ejbRemove() {
  }

  /**
   * Method declaration
   * @see
   */
  public void ejbActivate() {
  }

  /**
   * Method declaration
   * @see
   */
  public void ejbPassivate() {
  }

  /**
   * Method declaration
   * @param sc
   * @see
   */
  public void setSessionContext(SessionContext sc) {
  }

}
