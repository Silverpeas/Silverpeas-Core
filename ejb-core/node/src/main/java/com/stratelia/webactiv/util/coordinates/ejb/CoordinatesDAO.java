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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
// TODO : reporter dans CVS (done)
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.util.coordinates.ejb;

import java.sql.*;
import java.util.*;

import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.coordinates.model.*;

import com.stratelia.silverpeas.silvertrace.*;

/**
 * Class declaration
 * 
 * 
 * @author neysseri
 */
public class CoordinatesDAO {

  public static final String COORDINATESCOLUMNNAMES = "coordinatesId, nodeId, coordinatesLeaf,coordinatesDisplayOrder,instanceId";

  /**
   * Method declaration
   * 
   * 
   * @param rs
   *          the Resultset which contains data from database
   * 
   * @return a CoordinatePoint build with data from resultset
   * 
   * @throws SQLException
   * 
   * @see
   */
  private static CoordinatePoint getCoordinatePointFromResultSet(ResultSet rs)
      throws SQLException {
    int coordinateId = rs.getInt(1);
    int nodeId = rs.getInt(2);
    boolean leaf = false;

    if (rs.getString(3).equals("1")) {
      leaf = true;

    }
    CoordinatePoint result = new CoordinatePoint(coordinateId, nodeId, leaf);

    return result;
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   *          the Connection to database
   * @param fatherIds
   *          an ArrayList of nodeId
   * @param pk
   *          a CoordinatePK
   * 
   * @return an ArrayList which contains CoordinatePoint corresponding to
   *         fatherIds
   * 
   * @throws SQLException
   * 
   * @see
   */
  private static ArrayList selectCoordinatePointsByNodeIds(Connection con,
      ArrayList fatherIds, CoordinatePK pk) throws SQLException {

    CoordinatePoint coordinatePoint = null;
    ArrayList list = new ArrayList();
    String fatherId = "";
    String whereClause = "";

    if (fatherIds != null) {
      Iterator it = fatherIds.iterator();

      whereClause += "(";
      while (it.hasNext()) {
        fatherId = ((Integer) it.next()).toString();
        whereClause += " nodeId = " + fatherId;
        if (it.hasNext()) {
          whereClause += " or ";
        } else {
          whereClause += " ) ";
        }
      }
    }

    String selectQuery = "select * " + "from " + pk.getTableName() + " where "
        + whereClause + " and instanceId = '" + pk.getComponentName() + "' "
        + " order by coordinatesId, nodeId ASC";

    Statement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(selectQuery);
      while (rs.next()) {
        coordinatePoint = getCoordinatePointFromResultSet(rs);
        list.add(coordinatePoint);
      }
    } finally {
      DBUtil.close(rs, stmt);
    }

    return list;
  }

  /**
   * Method declaration
   * 
   * 
   * @param currentCoordinateId
   * @param fatherIds
   * @param toCheck
   * @param begin
   * 
   * @return
   * 
   * @see
   */
  private static int getNbMatchingCoordinates(int currentCoordinateId,
      ArrayList fatherIds, ArrayList toCheck, int begin) {
    CoordinatePoint coordinatePoint = null;
    int currentRId;
    int currentNId;
    int i = begin; // toCheck indice
    int f = 1; // fatherIds indice
    boolean coordinateMatch = true;
    int nbMatchingCoordinates = 0;

    while (i < toCheck.size() && coordinateMatch) {
      coordinatePoint = (CoordinatePoint) toCheck.get(i);
      currentRId = coordinatePoint.getCoordinateId();
      currentNId = coordinatePoint.getNodeId();
      if (currentRId == currentCoordinateId
          && currentNId == ((Integer) fatherIds.get(f)).intValue()) {
        nbMatchingCoordinates++;
        f++;
        i++;
      } else {
        coordinateMatch = false;
      }
    }
    return nbMatchingCoordinates;
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param fatherIds
   * @param pk
   * 
   * @return
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static Collection selectByFatherIds(Connection con,
      ArrayList fatherIds, CoordinatePK pk) throws SQLException {

    // get all points corresponding to fatherIds
    ArrayList points = selectCoordinatePointsByNodeIds(con, fatherIds, pk);

    SilverTrace.info("coordinates", "CoordinatesDAO.selectByFatherIds()",
        "root.MSG_GEN_PARAM_VALUE", "fatherIds = " + fatherIds.toString()
            + " | points = " + points.toString());

    ArrayList toCheck = new ArrayList(points); // toCheck always contains points
    ArrayList coordinatePKs = new ArrayList(); // 
    CoordinatePoint point = null;
    int nbAxis = fatherIds.size(); // number of axis
    int currentCoordinateId;
    int nbMatchingPoints = 0;
    int i = 0;

    // check all points
    while (i < points.size()) {
      point = (CoordinatePoint) points.get(i);
      currentCoordinateId = point.getCoordinateId();
      if (point.getNodeId() == ((Integer) fatherIds.get(0)).intValue()) {

        // vérifie que les n-1 axes de la BD correspondent aux axes de fatherIds
        nbMatchingPoints = getNbMatchingCoordinates(currentCoordinateId,
            fatherIds, toCheck, i + 1);
        if (nbMatchingPoints == (nbAxis - 1)) {
          coordinatePKs.add(new Integer(currentCoordinateId).toString());
          i = i + nbMatchingPoints;
        }
      }
      i++;
    }
    return coordinatePKs;
  }

  public static Collection selectByFatherPaths(Connection con,
      ArrayList fatherPaths, CoordinatePK pk) throws SQLException {
    ArrayList coordinateIds = selectCoordinateIdsByNodeIds(con, fatherPaths, pk);
    return coordinateIds;
  }

  private static ArrayList selectCoordinateIdsByNodeIds(Connection con,
      ArrayList fatherPaths, CoordinatePK pk) throws SQLException {
    ResultSet rs = null;

    String fatherPath = "";
    String whereClause = "";
    String fatherId = "";
    String rootFatherId = "";
    int axisToMatch = fatherPaths.size();
    if (fatherPaths != null) {
      Iterator it = fatherPaths.iterator();
      while (it.hasNext()) {
        fatherPath = (String) it.next();
        SilverTrace.info("coordinates",
            "CoordinatesDAO.selectCoordinateIdsByNodeIds()",
            "root.MSG_GEN_PARAM_VALUE", "fatherPath = " + fatherPath);
        // enleve le premier /0/
        fatherPath = fatherPath.substring(1);
        fatherPath = fatherPath.substring(fatherPath.indexOf('/') + 1,
            fatherPath.length());
        SilverTrace.info("coordinates",
            "CoordinatesDAO.selectCoordinateIdsByNodeIds()",
            "root.MSG_GEN_PARAM_VALUE", "fatherPath = " + fatherPath);
        // extrait l'id
        fatherId = fatherPath.substring(fatherPath.lastIndexOf('/') + 1,
            fatherPath.length());
        SilverTrace.info("coordinates",
            "CoordinatesDAO.selectCoordinateIdsByNodeIds()",
            "root.MSG_GEN_PARAM_VALUE", "fatherId = " + fatherId);
        // extrait l'id de la racine
        rootFatherId = fatherPath.substring(0, fatherPath.indexOf('/'));
        SilverTrace.info("coordinates",
            "CoordinatesDAO.selectCoordinateIdsByNodeIds()",
            "root.MSG_GEN_PARAM_VALUE", "rootFatherId = " + rootFatherId);

        whereClause += " nodeId = " + fatherId;
        whereClause += " or (nodeId = " + rootFatherId
            + " and coordinatesLeaf = '1') ";
        if (it.hasNext())
          whereClause += " Or ";
      }
    }

    String selectStatement = "select coordinatesId, count(*) " + "from "
        + pk.getTableName() + " ";
    if (fatherPaths != null && fatherPaths.size() > 0)
      selectStatement += "where " + whereClause;
    selectStatement += " And instanceId = '" + pk.getComponentName() + "' ";
    selectStatement += " GROUP BY coordinatesId";

    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(selectStatement);
      rs = prepStmt.executeQuery();
      ArrayList list = new ArrayList();
      int coordinateId = 0;
      int nbMatches = 0;
      while (rs.next()) {
        coordinateId = rs.getInt(1);
        nbMatches = rs.getInt(2);
        if (nbMatches == axisToMatch)
          list.add(new Integer(coordinateId).toString());
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param pk
   * @param point
   * @param coordinateId
   * 
   * @throws SQLException
   * 
   * @see
   */
  private static void addCoordinatePoint(Connection con, CoordinatePK pk,
      CoordinatePoint point, int coordinateId) throws SQLException {
    String insertQuery = "insert into " + pk.getTableName()
        + " values ( ? , ? , ? , ? , ?)";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(insertQuery);
      prepStmt.setInt(1, coordinateId);
      prepStmt.setInt(2, point.getNodeId());
      if (point.isLeaf()) {
        prepStmt.setString(3, "1");
      } else {
        prepStmt.setString(3, "0");
      }
      prepStmt.setInt(4, point.getOrder());
      prepStmt.setString(5, pk.getComponentName());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param pk
   * @param coordinatePoints
   * 
   * @return
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static int addCoordinate(Connection con, CoordinatePK pk,
      ArrayList coordinatePoints) throws SQLException {
    SilverTrace.info("coordinates", "CoordinatesDAO.addCoordinate()",
        "root.MSG_GEN_PARAM_VALUE", "pk = " + pk.toString()
            + " and coordinatePoints = " + coordinatePoints.toString());

    int coordinateId = getMaxCoordinateId(con, pk) + 1;
    Iterator it = coordinatePoints.iterator();
    CoordinatePoint point = null;

    while (it.hasNext()) {
      point = (CoordinatePoint) it.next();
      SilverTrace.info("coordinates", "CoordinatesDAO.addCoordinate()",
          "root.MSG_GEN_PARAM_VALUE", "Try to insert point = "
              + point.toString());
      addCoordinatePoint(con, pk, point, coordinateId);
      SilverTrace.info("coordinates", "CoordinatesDAO.addCoordinate()",
          "root.MSG_GEN_PARAM_VALUE", "insertion of point = "
              + point.toString() + " succeeded !");
    }
    return coordinateId;
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param pk
   * @param coordinateIds
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static void removeCoordinates(Connection con, CoordinatePK pk,
      ArrayList coordinateIds) throws SQLException {
    SilverTrace.info("coordinates", "CoordinatesDAO.removeCoordinates()",
        "root.MSG_GEN_PARAM_VALUE", "pk = " + pk.toString()
            + " and coordinateIds = " + coordinateIds.toString());

    String coordinateId = "";
    String whereClause = "";

    if (coordinateIds != null) {
      Iterator it = coordinateIds.iterator();

      whereClause += "(";
      while (it.hasNext()) {
        coordinateId = (String) it.next();
        whereClause += " coordinatesId = " + coordinateId;
        if (it.hasNext()) {
          whereClause += " or ";
        } else {
          whereClause += " ) ";
        }
      }
    }

    String deleteQuery = "delete from " + pk.getTableName() + " where "
        + whereClause + " and instanceId = '" + pk.getComponentName() + "'";

    Statement stmt = null;

    try {
      stmt = con.createStatement();
      stmt.executeUpdate(deleteQuery);
    } finally {
      DBUtil.close(stmt);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param pk
   * @param coordinatePoints
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static void removeCoordinatesByPoints(Connection con, CoordinatePK pk,
      ArrayList coordinatePoints) throws SQLException {
    SilverTrace.info("coordinates",
        "CoordinatesDAO.removeCoordinatesByPoints()",
        "root.MSG_GEN_PARAM_VALUE", "pk = " + pk.toString()
            + " and coordinatePoints = " + coordinatePoints.toString());

    String pointId = "";
    String whereClause = "";

    if (coordinatePoints != null) {
      Iterator it = coordinatePoints.iterator();

      whereClause += "(";
      while (it.hasNext()) {
        pointId = (String) it.next();
        whereClause += " nodeId = " + pointId;
        if (it.hasNext()) {
          whereClause += " or ";
        } else {
          whereClause += " ) ";
        }
      }
    }

    String deleteQuery = "delete from " + pk.getTableName() + " where "
        + whereClause + " and instanceId ='" + pk.getComponentName() + "'";

    Statement stmt = null;

    try {
      stmt = con.createStatement();
      stmt.executeUpdate(deleteQuery);
    } finally {
      DBUtil.close(stmt);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param pk
   * 
   * @return
   * 
   * @throws SQLException
   * 
   * @see
   */
  private static int getMaxCoordinateId(Connection con, CoordinatePK pk)
      throws SQLException {
    int maxFromTable = 0;

    Statement stmt = null;
    ResultSet rs = null;

    try {
      String nextPKStatement = "SELECT MAX(coordinatesId) FROM "
          + pk.getTableName() + " where instanceId='" + pk.getComponentName()
          + "'";

      stmt = con.createStatement();
      rs = stmt.executeQuery(nextPKStatement);
      if (rs.next()) {
        maxFromTable = rs.getInt(1);
      }
    } finally {
      DBUtil.close(rs, stmt);
    }

    return maxFromTable;
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param pk
   * 
   * @return
   * 
   * @throws SQLException
   * 
   * @see
   */
  private static int getMaxDisplayOrder(Connection con, CoordinatePK pk)
      throws SQLException {

    int maxFromTable = 0;
    Statement stmt = null;
    ResultSet rs = null;

    try {
      String nextPKStatement = "SELECT MAX(coordinatesDisplayOrder) FROM "
          + pk.getTableName() + " where instanceId='" + pk.getComponentName()
          + "'";

      stmt = con.createStatement();
      rs = stmt.executeQuery(nextPKStatement);
      if (rs.next()) {
        maxFromTable = rs.getInt(1);

      }
    } finally {
      DBUtil.close(rs, stmt);
    }
    return maxFromTable;
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param pk
   * 
   * @return
   * 
   * @throws SQLException
   * 
   * @see
   */
  private static Coordinate selectCoordinateByCoordinatePK(Connection con,
      CoordinatePK pk) throws SQLException {

    CoordinatePoint coordinatePoint = null;
    ArrayList list = new ArrayList();
    String selectQuery = "select * " + "from " + pk.getTableName()
        + " where coordinatesId = ? " + " and coordinatesLeaf = ? "
        + " and instanceId = ? " + " order by coordinatesDisplayOrder ";

    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    try {
      prepStmt = con.prepareStatement(selectQuery);
      prepStmt.setInt(1, new Integer(pk.getId()).intValue());
      prepStmt.setString(2, "1");
      prepStmt.setString(3, pk.getComponentName());
      rs = prepStmt.executeQuery();

      while (rs.next()) {
        coordinatePoint = getCoordinatePointFromResultSet(rs);
        list.add(coordinatePoint);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return new Coordinate(new Integer(pk.getId()).intValue(), list);
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param coordinateIds
   * @param pk
   * 
   * @return
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static ArrayList selectCoordinatesByCoordinateIds(Connection con,
      ArrayList coordinateIds, CoordinatePK pk) throws SQLException {
    Iterator it = coordinateIds.iterator();
    ArrayList coordinates = new ArrayList();
    String coordinateId = "";
    Coordinate coordinate = null;

    while (it.hasNext()) {
      coordinateId = (String) it.next();
      coordinate = selectCoordinateByCoordinatePK(con, new CoordinatePK(
          coordinateId, pk));
      coordinates.add(coordinate);
    }
    return coordinates;
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param pk
   * 
   * @return
   * 
   * @throws SQLException
   * 
   * @see
   */
  private static Collection getCoordinateIds(Connection con, CoordinatePK pk)
      throws SQLException {

    ArrayList coordinateIds = new ArrayList();
    String selectQuery = "select distinct(coordinatesId) " + "from "
        + pk.getTableName() + " where instanceId ='" + pk.getComponentName()
        + "'";

    Statement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(selectQuery);
      String coordinateId = "";

      while (rs.next()) {
        coordinateId = new Integer(rs.getInt(1)).toString();
        coordinateIds.add(coordinateId);
      }
    } finally {
      DBUtil.close(rs, stmt);
    }

    return coordinateIds;
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param pk
   * @param point
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static void addPointToAllCoordinates(Connection con, CoordinatePK pk,
      CoordinatePoint point) throws SQLException {
    SilverTrace.info("coordinates",
        "CoordinatesDAO.addPointToAllCoordinates()",
        "root.MSG_GEN_PARAM_VALUE", "pk = " + pk.toString() + " and point = "
            + point.toString());

    Collection coordinateIds = getCoordinateIds(con, pk);
    int maxDisplayOrder = getMaxDisplayOrder(con, pk);

    point.setOrder(maxDisplayOrder + 1);
    Iterator it = coordinateIds.iterator();
    int coordinateId;

    while (it.hasNext()) {
      coordinateId = new Integer((String) it.next()).intValue();
      addCoordinatePoint(con, pk, point, coordinateId);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param pk
   * @param nodeId
   * 
   * @return
   * 
   * @throws SQLException
   * 
   * @see
   */
  public static Collection getCoordinateIdsByNodeId(Connection con,
      CoordinatePK pk, String nodeId) throws SQLException {

    ArrayList coordinateIds = new ArrayList();

    String selectQuery = "select distinct(coordinatesId) " + "from "
        + pk.getTableName() + " where nodeId = ? " + " and instanceId = ? ";

    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    try {
      prepStmt = con.prepareStatement(selectQuery);
      prepStmt.setInt(1, new Integer(nodeId).intValue());
      prepStmt.setString(2, pk.getComponentName());
      rs = prepStmt.executeQuery();

      String coordinateId = "";

      while (rs.next()) {
        coordinateId = new Integer(rs.getInt(1)).toString();
        coordinateIds.add(coordinateId);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return coordinateIds;
  }

}
