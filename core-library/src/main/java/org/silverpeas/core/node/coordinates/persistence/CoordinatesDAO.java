/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.node.coordinates.persistence;

import org.silverpeas.core.node.coordinates.model.Coordinate;
import org.silverpeas.core.node.coordinates.model.CoordinatePK;
import org.silverpeas.core.node.coordinates.model.CoordinatePoint;
import org.silverpeas.core.node.model.NodePK;
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
  private static final String NODE_ID_EQUALS_TO = " nodeId = ";

  private static CoordinatePoint getCoordinatePointFromResultSet(ResultSet rs)
      throws SQLException {
    int coordinateId = rs.getInt("coordinatesId");
    int nodeId = rs.getInt("nodeid");
    boolean leaf = "1".equals(rs.getString("coordinatesleaf"));
    return new CoordinatePoint(coordinateId, nodeId, leaf);
  }

  private static List<CoordinatePoint> selectCoordinatePointsByNodeIds(Connection con,
      List<Integer> fatherIds, CoordinatePK pk) throws SQLException {
    List<CoordinatePoint> list = new ArrayList<>();
    StringBuilder whereClause = new StringBuilder(20 * fatherIds.size() + 200);
    Iterator<Integer> it = fatherIds.iterator();
    whereClause.append("(");
    while (it.hasNext()) {
      whereClause.append(NODE_ID_EQUALS_TO).append(it.next());
      if (it.hasNext()) {
        whereClause.append(" OR ");
      } else {
        whereClause.append(" ) ");
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

  public static Collection<String> selectByFatherIds(Connection con, List<Integer> fatherIds,
      CoordinatePK pk) throws SQLException {
    // get all points corresponding to fatherIds
    List<CoordinatePoint> points = selectCoordinatePointsByNodeIds(con, fatherIds, pk);
    List<CoordinatePoint> toCheck = new ArrayList<>(points); // toCheck always
    // contains points
    List<String> coordinatePKs = new ArrayList<>();
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
    return selectCoordinateIdsByNodeIds(con, fatherPaths, pk);
  }

  private static List<String> selectCoordinateIdsByNodeIds(Connection con,
      List<String> fatherPaths, CoordinatePK pk) throws SQLException {
    ResultSet rs = null;

    String fatherPath = "";
    StringBuilder whereClause = new StringBuilder();
    String fatherId = "";
    String rootFatherId = "";
    int axisToMatch = fatherPaths.size();

    Iterator<String> it = fatherPaths.iterator();
    while (it.hasNext()) {
      fatherPath = it.next();
      // enleve le premier /0/
      fatherPath = fatherPath.substring(1);
      fatherPath = fatherPath.substring(fatherPath.indexOf('/') + 1, fatherPath.length());
      // extrait l'id
      fatherId = fatherPath.substring(fatherPath.lastIndexOf('/') + 1, fatherPath.length());
      // extrait l'id de la racine
      rootFatherId = fatherPath.substring(0, fatherPath.indexOf('/'));
      whereClause.append(NODE_ID_EQUALS_TO).append(fatherId);
      whereClause.append(" or (nodeId = ")
          .append(rootFatherId)
          .append(" and coordinatesLeaf = '1') ");
      if (it.hasNext()) {
        whereClause.append(" Or ");
      }
    }

    String selectStatement = "select coordinatesId, count(*) " + "from "
        + pk.getTableName() + " ";
    if (!fatherPaths.isEmpty()) {
      selectStatement += "where " + whereClause.toString();
    }
    selectStatement += " And instanceId = '" + pk.getComponentName() + "' ";
    selectStatement += " GROUP BY coordinatesId";

    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(selectStatement);
      rs = prepStmt.executeQuery();
      List<String> list = new ArrayList<>();
      int coordinateId = 0;
      int nbMatches = 0;
      while (rs.next()) {
        coordinateId = rs.getInt(1);
        nbMatches = rs.getInt(2);
        if (nbMatches == axisToMatch) {
          list.add(Integer.toString(coordinateId));
        }
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

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

  public static int addCoordinate(Connection con, CoordinatePK pk,
      List<CoordinatePoint> coordinatePoints)
      throws SQLException {

    int coordinateId = getMaxCoordinateId(con, pk) + 1;
    for (CoordinatePoint point : coordinatePoints) {
      addCoordinatePoint(con, pk, point, coordinateId);
    }
    return coordinateId;
  }

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

  public static void removeCoordinatesByPoints(Connection con, CoordinatePK pk,
      List<String> coordinatePoints) throws SQLException {
    StringBuilder deleteQuery = new StringBuilder("DELETE FROM sb_coordinates_coordinates WHERE ");
    if (coordinatePoints != null) {
      Iterator<String> it = coordinatePoints.iterator();
      deleteQuery.append("(");
      while (it.hasNext()) {
        String pointId = it.next();
        deleteQuery.append(NODE_ID_EQUALS_TO).append(pointId);
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
    return Integer.parseInt(NodePK.UNCLASSED_NODE_ID) + maxFromTable;
  }

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

  private static Coordinate selectCoordinateByCoordinatePK(Connection con,
      CoordinatePK pk) throws SQLException {
    List<CoordinatePoint> list = new ArrayList<>();
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

  public static List<Coordinate> selectCoordinatesByCoordinateIds(Connection con,
      List<String> coordinateIds, CoordinatePK pk) throws SQLException {
    ArrayList<Coordinate> coordinates = new ArrayList<>();
    for (String coordinateId : coordinateIds) {
      coordinates.add(selectCoordinateByCoordinatePK(con, new CoordinatePK(coordinateId, pk)));
    }
    return coordinates;
  }

  static Collection<String> getCoordinateIds(Connection con, CoordinatePK pk)
      throws SQLException {
    List<String> coordinateIds = new ArrayList<>();
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

  public static Collection<String> getCoordinateIdsByNodeId(Connection con, CoordinatePK pk,
      String nodeId)
      throws SQLException {
    List<String> coordinateIds = new ArrayList<>();
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(SELECT_BY_NODEID);
      prepStmt.setInt(1, Integer.parseInt(nodeId));
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
