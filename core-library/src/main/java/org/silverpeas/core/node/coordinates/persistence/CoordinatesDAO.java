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

package org.silverpeas.core.node.coordinates.persistence;

import org.silverpeas.core.node.coordinates.model.Coordinate;
import org.silverpeas.core.node.coordinates.model.CoordinatePK;
import org.silverpeas.core.node.coordinates.model.CoordinatePoint;
import org.silverpeas.core.persistence.jdbc.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Class declaration
 * @author neysseri
 */
public class CoordinatesDAO {

  public static final String COORDINATESCOLUMNNAMES =
      "coordinatesId, nodeId, coordinatesLeaf,coordinatesDisplayOrder,instanceId";
  private static final String SELECT_BY_NODEID =
      "SELECT DISTINCT(coordinatesId) AS id FROM sb_coordinates_coordinates WHERE nodeid = ? AND instanceid = ? ";
  private static final String SELECT_BY_COMPONENT =
      "SELECT DISTINCT(coordinatesid) FROM sb_coordinates_coordinates WHERE instanceid = ?";
  private static final String SELECT_BY_PK =
      "SELECT coordinatesid, nodeid, coordinatesleaf, "
          + "coordinatesdisplayorder,instanceid FROM sb_coordinates_coordinates WHERE coordinatesid = ? "
          + "AND coordinatesleaf = ? AND instanceid = ? ORDER BY coordinatesdisplayorder ";
  private static final String SELECT_MAX_ORDER =
      "SELECT MAX(coordinatesdisplayorder) FROM sb_coordinates_coordinates WHERE instanceId= ?";
  private static final String SELECT_MAX_ID =
      "SELECT MAX(coordinatesid) FROM sb_coordinates_coordinates WHERE instanceid = ?";
  private static final String INSERT_COORDINATE =
      "INSERT INTO sb_coordinates_coordinates ("
          + "coordinatesid, nodeid, coordinatesleaf, coordinatesdisplayorder, instanceid) VALUES ( ? , "
          + "? , ? , ? , ?)";
  private static final String DELETE_COORDINATES_BY_INSTANCEID =
      "DELETE FROM sb_coordinates_coordinates where instanceId = ?";

  /**
   * Method declaration
   * @param rs the Resultset which contains data from database
   * @return a CoordinatePoint build with data from resultset
   * @throws SQLException
   * @see
   */
  private static CoordinatePoint getCoordinatePointFromResultSet(ResultSet rs)
      throws SQLException {
    int coordinateId = rs.getInt("coordinatesId");
    int nodeId = rs.getInt("nodeid");
    boolean leaf = "1".equals(rs.getString("coordinatesleaf"));
    CoordinatePoint result = new CoordinatePoint(coordinateId, nodeId, leaf);
    return result;
  }

  /**
   * Method declaration
   * @param con the Connection to database
   * @param fatherIds an ArrayList of nodeId
   * @param pk a CoordinatePK
   * @return an ArrayList which contains CoordinatePoint corresponding to fatherIds
   * @throws SQLException
   * @see
   */
  private static List<CoordinatePoint> selectCoordinatePointsByNodeIds(Connection con,
      List<Integer> fatherIds, CoordinatePK pk) throws SQLException {
    List<CoordinatePoint> list = new ArrayList<CoordinatePoint>();
    StringBuilder whereClause = new StringBuilder(20 * fatherIds.size() + 200);
    if (fatherIds != null) {
      Iterator<Integer> it = fatherIds.iterator();
      whereClause.append("(");
      while (it.hasNext()) {
        whereClause.append(" nodeId = ").append(it.next());
        if (it.hasNext()) {
          whereClause.append(" OR ");
        } else {
          whereClause.append(" ) ");
        }
      }
    }
    String selectQuery = "SELECT coordinatesId, nodeId, coordinatesLeaf, coordinatesDisplayOrder, "
        + "instanceId FROM sb_coordinates_coordinates WHERE "
        + whereClause + " AND instanceId = '" + pk.getComponentName() + "' "
        + " ORDER BY coordinatesId, nodeId ASC";

    Statement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(selectQuery);
      while (rs.next()) {
        CoordinatePoint coordinatePoint = getCoordinatePointFromResultSet(rs);
        list.add(coordinatePoint);
      }
    } finally {
      DBUtil.close(rs, stmt);
    }

    return list;
  }

  /**
   * Method declaration
   * @param currentCoordinateId
   * @param fatherIds
   * @param toCheck
   * @param begin
   * @return
   * @see
   */
  private static int getNbMatchingCoordinates(int currentCoordinateId, List<Integer> fatherIds,
      List<CoordinatePoint> toCheck, int begin) {
    int i = begin; // toCheck indice
    int f = 1; // fatherIds indice
    boolean coordinateMatch = true;
    int nbMatchingCoordinates = 0;

    while (i < toCheck.size() && coordinateMatch) {
      CoordinatePoint coordinatePoint = toCheck.get(i);
      int currentRId = coordinatePoint.getCoordinateId();
      int currentNId = coordinatePoint.getNodeId();
      if (currentRId == currentCoordinateId && currentNId == (fatherIds.get(f)).intValue()) {
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
   * @param con
   * @param fatherIds
   * @param pk
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection<String> selectByFatherIds(Connection con, List<Integer> fatherIds,
      CoordinatePK pk) throws SQLException {
    // get all points corresponding to fatherIds
    List<CoordinatePoint> points = selectCoordinatePointsByNodeIds(con, fatherIds, pk);
    List<CoordinatePoint> toCheck = new ArrayList<CoordinatePoint>(points); // toCheck always
    // contains points
    List<String> coordinatePKs = new ArrayList<String>();
    int nbAxis = fatherIds.size(); // number of axis
    int currentCoordinateId;
    int nbMatchingPoints = 0;
    int i = 0;

    // check all points
    while (i < points.size()) {
      CoordinatePoint point = points.get(i);
      currentCoordinateId = point.getCoordinateId();
      if (point.getNodeId() == fatherIds.get(0).intValue()) {
        // verifie que les n-1 axes de la BD correspondent aux axes de fatherIds
        nbMatchingPoints = getNbMatchingCoordinates(currentCoordinateId,
            fatherIds, toCheck, i + 1);
        if (nbMatchingPoints == (nbAxis - 1)) {
          coordinatePKs.add(String.valueOf(currentCoordinateId));
          i = i + nbMatchingPoints;
        }
      }
      i++;
    }
    return coordinatePKs;
  }

  public static Collection<String> selectByFatherPaths(Connection con,
      List<String> fatherPaths, CoordinatePK pk) throws SQLException {
    List<String> coordinateIds = selectCoordinateIdsByNodeIds(con, fatherPaths, pk);
    return coordinateIds;
  }

  private static List<String> selectCoordinateIdsByNodeIds(Connection con,
      List<String> fatherPaths, CoordinatePK pk) throws SQLException {
    ResultSet rs = null;

    String fatherPath = "";
    String whereClause = "";
    String fatherId = "";
    String rootFatherId = "";
    int axisToMatch = fatherPaths.size();
    if (fatherPaths != null) {
      Iterator<String> it = fatherPaths.iterator();
      while (it.hasNext()) {
        fatherPath = it.next();
        // enleve le premier /0/
        fatherPath = fatherPath.substring(1);
        fatherPath = fatherPath.substring(fatherPath.indexOf('/') + 1,
            fatherPath.length());
        // extrait l'id
        fatherId = fatherPath.substring(fatherPath.lastIndexOf('/') + 1,
            fatherPath.length());
        // extrait l'id de la racine
        rootFatherId = fatherPath.substring(0, fatherPath.indexOf('/'));
        whereClause += " nodeId = " + fatherId;
        whereClause += " or (nodeId = " + rootFatherId
            + " and coordinatesLeaf = '1') ";
        if (it.hasNext()) {
          whereClause += " Or ";
        }
      }
    }

    String selectStatement = "select coordinatesId, count(*) " + "from "
        + pk.getTableName() + " ";
    if (fatherPaths != null && fatherPaths.size() > 0) {
      selectStatement += "where " + whereClause;
    }
    selectStatement += " And instanceId = '" + pk.getComponentName() + "' ";
    selectStatement += " GROUP BY coordinatesId";

    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(selectStatement);
      rs = prepStmt.executeQuery();
      List<String> list = new ArrayList<String>();
      int coordinateId = 0;
      int nbMatches = 0;
      while (rs.next()) {
        coordinateId = rs.getInt(1);
        nbMatches = rs.getInt(2);
        if (nbMatches == axisToMatch) {
          list.add(new Integer(coordinateId).toString());
        }
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param pk
   * @param point
   * @param coordinateId
   * @throws SQLException
   * @see
   */
  private static void addCoordinatePoint(Connection con, CoordinatePK pk,
      CoordinatePoint point, int coordinateId) throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(INSERT_COORDINATE);
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
   * @param con
   * @param pk
   * @param coordinatePoints
   * @return
   * @throws SQLException
   * @see
   */
  public static int addCoordinate(Connection con, CoordinatePK pk,
      List<CoordinatePoint> coordinatePoints)
      throws SQLException {

    int coordinateId = getMaxCoordinateId(con, pk) + 1;
    for (CoordinatePoint point : coordinatePoints) {
      addCoordinatePoint(con, pk, point, coordinateId);
    }
    return coordinateId;
  }

  /**
   * Method declaration
   * @param con
   * @param pk
   * @param coordinateIds
   * @throws SQLException
   * @see
   */
  public static void removeCoordinates(Connection con, CoordinatePK pk, List<String> coordinateIds)
      throws SQLException {
    StringBuilder deleteQuery = new StringBuilder("DELETE FROM sb_coordinates_coordinates WHERE ");
    if (coordinateIds != null) {
      Iterator<String> it = coordinateIds.iterator();
      deleteQuery.append("(");
      while (it.hasNext()) {
        String coordinateId = it.next();
        deleteQuery.append(" coordinatesId = ").append(coordinateId);
        if (it.hasNext()) {
          deleteQuery.append(" OR ");
        }
      }
      deleteQuery.append(" ) ");
    }
    deleteQuery.append(" AND instanceId = '").append(pk.getComponentName()).append("'");
    Statement stmt = null;

    try {
      stmt = con.createStatement();
      stmt.executeUpdate(deleteQuery.toString());
    } finally {
      DBUtil.close(stmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param pk
   * @param coordinatePoints
   * @throws SQLException
   * @see
   */
  public static void removeCoordinatesByPoints(Connection con, CoordinatePK pk,
      List<String> coordinatePoints) throws SQLException {
    StringBuilder deleteQuery = new StringBuilder("DELETE FROM sb_coordinates_coordinates WHERE ");
    if (coordinatePoints != null) {
      Iterator<String> it = coordinatePoints.iterator();
      deleteQuery.append("(");
      while (it.hasNext()) {
        String pointId = it.next();
        deleteQuery.append(" nodeId = ").append(pointId);
        if (it.hasNext()) {
          deleteQuery.append(" or ");
        }
      }
      deleteQuery.append(" ) ");
    }
    deleteQuery.append(" AND instanceId ='").append(pk.getComponentName()).append("'");
    Statement stmt = null;

    try {
      stmt = con.createStatement();
      stmt.executeUpdate(deleteQuery.toString());
    } finally {
      DBUtil.close(stmt);
    }
  }

  public static void removeCoordinatesByInstanceId(Connection con, String instanceId)
      throws SQLException {
    try (PreparedStatement deletion = con.prepareStatement(DELETE_COORDINATES_BY_INSTANCEID)) {
      deletion.setString(1, instanceId);
      deletion.execute();
    }
  }

  /**
   * Method declaration
   * @param con
   * @param pk
   * @return
   * @throws SQLException
   * @see
   */
  private static int getMaxCoordinateId(Connection con, CoordinatePK pk)
      throws SQLException {
    int maxFromTable = 0;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
      pstmt = con.prepareStatement(SELECT_MAX_ID);
      pstmt.setString(1, pk.getComponentName());
      rs = pstmt.executeQuery();
      if (rs.next()) {
        maxFromTable = rs.getInt(1);
      }
    } finally {
      DBUtil.close(rs, pstmt);
    }
    return maxFromTable;
  }

  /**
   * Method declaration
   * @param con
   * @param pk
   * @return
   * @throws SQLException
   * @see
   */
  private static int getMaxDisplayOrder(Connection con, CoordinatePK pk)
      throws SQLException {
    int maxFromTable = 0;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
      pstmt = con.prepareStatement(SELECT_MAX_ORDER);
      pstmt.setString(1, pk.getComponentName());
      rs = pstmt.executeQuery();
      if (rs.next()) {
        maxFromTable = rs.getInt(1);
      }
    } finally {
      DBUtil.close(rs, pstmt);
    }
    return maxFromTable;
  }

  /**
   * Method declaration
   * @param con
   * @param pk
   * @return
   * @throws SQLException
   * @see
   */
  private static Coordinate selectCoordinateByCoordinatePK(Connection con,
      CoordinatePK pk) throws SQLException {
    List<CoordinatePoint> list = new ArrayList<CoordinatePoint>();
    int id = Integer.parseInt(pk.getId());
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(SELECT_BY_PK);
      prepStmt.setInt(1, id);
      prepStmt.setString(2, "1");
      prepStmt.setString(3, pk.getComponentName());
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        list.add(getCoordinatePointFromResultSet(rs));
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return new Coordinate(id, list);
  }

  /**
   * Method declaration
   * @param con
   * @param coordinateIds
   * @param pk
   * @return
   * @throws SQLException
   * @see
   */
  public static ArrayList<Coordinate> selectCoordinatesByCoordinateIds(Connection con,
      List<String> coordinateIds, CoordinatePK pk) throws SQLException {
    ArrayList<Coordinate> coordinates = new ArrayList<Coordinate>();
    for (String coordinateId : coordinateIds) {
      coordinates.add(selectCoordinateByCoordinatePK(con, new CoordinatePK(coordinateId, pk)));
    }
    return coordinates;
  }

  /**
   * Method declaration
   * @param con
   * @param pk
   * @return
   * @throws SQLException
   * @see
   */
  static Collection<String> getCoordinateIds(Connection con, CoordinatePK pk)
      throws SQLException {
    List<String> coordinateIds = new ArrayList<String>();
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
      pstmt = con.prepareStatement(SELECT_BY_COMPONENT);
      pstmt.setString(1, pk.getComponentName());
      rs = pstmt.executeQuery();
      while (rs.next()) {
        coordinateIds.add(rs.getString("coordinatesid"));
      }
    } finally {
      DBUtil.close(rs, pstmt);
    }
    return coordinateIds;
  }

  /**
   * Method declaration
   * @param con
   * @param pk
   * @param point
   * @throws SQLException
   * @see
   */
  public static void addPointToAllCoordinates(Connection con, CoordinatePK pk,
      CoordinatePoint point) throws SQLException {
    int maxDisplayOrder = getMaxDisplayOrder(con, pk);
    point.setOrder(maxDisplayOrder + 1);
    Collection<String> coordinateIds = getCoordinateIds(con, pk);
    for (String id : coordinateIds) {
      int coordinateId = Integer.parseInt(id);
      addCoordinatePoint(con, pk, point, coordinateId);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param pk
   * @param nodeId
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection<String> getCoordinateIdsByNodeId(Connection con, CoordinatePK pk,
      String nodeId)
      throws SQLException {
    List<String> coordinateIds = new ArrayList<String>();
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(SELECT_BY_NODEID);
      prepStmt.setInt(1, new Integer(nodeId).intValue());
      prepStmt.setString(2, pk.getComponentName());
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        coordinateIds.add(rs.getString("id"));
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return coordinateIds;
  }

  private CoordinatesDAO() {
  }
}
