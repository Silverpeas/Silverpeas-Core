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
package org.silverpeas.core.node.dao;

import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodeI18NDetail;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.model.NodeRuntimeException;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.i18n.I18NHelper;

import javax.ejb.NoSuchEntityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is the Node Data Access Object.
 * @author Nicolas Eysseric
 */
public class NodeDAO {

  private static final String SELECT_NODE_BY_ID = "SELECT nodeid, nodename, nodedescription, " +
      "nodecreationdate, nodecreatorid, nodepath, nodelevelnumber, nodefatherid, modelid, " +
      "nodestatus, instanceid, type, ordernumber, lang, rightsdependson FROM sb_node_node WHERE " +
      "nodeId = ? AND instanceId = ?";
  private static final String COUNT_NODES_PER_LEVEL =
      "SELECT COUNT(nodeid) as nb FROM sb_node_node " +
          "WHERE nodelevelnumber = ? AND nodeName = ? AND instanceid = ? ";
  private static final String COUNT_NODES_PER_LEVEL_WITHOUT_CURRENT =
      "SELECT COUNT(nodeid) as nb FROM sb_node_node " +
          "WHERE nodeid <> ? AND nodelevelnumber = ? AND nodeName = ? AND instanceid = ? ";
  private static final String SELECT_CHILDREN_IDS = "SELECT nodeid FROM sb_node_node WHERE " +
      "nodefatherid = ? AND instanceId = ? ORDER BY nodeid";
  private static final String SELECT_DESCENDANTS_PK =
      "SELECT nodepath FROM sb_node_node WHERE " + "nodeid = ? AND instanceid = ?";
  private static final String SELECT_DESCENDANTS_ID_BY_PATH = "SELECT nodeid FROM sb_node_node " +
      "WHERE nodePath LIKE ? AND instanceid = ? ORDER BY nodeid";
  private static final Map<String, ArrayList<NodeDetail>> alltrees = new ConcurrentHashMap<>();

  /**
   * This class must not be instanciated
   * @since 1.0
   */
  public NodeDAO() {
  }

  /**
   * Deletes all nodes linked to the component instance represented by the given identifier.
   * @param componentInstanceId the identifier of the component instance for which the resources
   * must be deleted.
   * @throws SQLException
   */
  public static void deleteComponentInstanceData(String componentInstanceId) throws SQLException {
    JdbcSqlQuery.createDeleteFor("sb_node_node").where("instanceId = ?", componentInstanceId)
        .execute();
  }

  public static ArrayList<NodeDetail> getTree(Connection con, NodePK nodePK) throws SQLException {
    ArrayList<NodeDetail> tree = alltrees.get(nodePK.getComponentName());
    if (tree == null) {
      tree = (ArrayList<NodeDetail>) getAllHeaders(con, nodePK);
      alltrees.put(nodePK.getComponentName(), tree);
    }
    return tree;
  }

  public static void unvalidateTree(Connection con, NodePK nodePK) {
    alltrees.remove(nodePK.getComponentName());
  }

  /**
   * On node creation, check if another node have got the same name with same father
   * @param con A connection to the database
   * @param nd A NodeDetail contains new node data to compare
   * @return true if there is already a node with same name with same father false else
   * @throws java.sql.SQLException
   * @see NodeDetail
   * @since 1.0
   */
  public static boolean isSameNameSameLevelOnCreation(Connection con, NodeDetail nd)
      throws SQLException {
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    int nbItems = 0;
    try {
      prepStmt = con.prepareStatement(COUNT_NODES_PER_LEVEL);
      prepStmt.setInt(1, nd.getLevel());
      prepStmt.setString(2, nd.getName());
      prepStmt.setString(3, nd.getNodePK().getComponentName());
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        nbItems = rs.getInt("nb");
      }
    } catch (SQLException e) {
      SilverTrace
          .error("node", "NodeDAO.isSameNameSameLevelOnCreation()", "root.EX_SQL_QUERY_FAILED",
              "selectQuery = " + COUNT_NODES_PER_LEVEL + " level = " + nd.getLevel() + " name = " +
                  nd.getName() + " compo name = " + nd.getNodePK().getComponentName(), e);
      throw e;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return nbItems != 0;
  }

  /**
   * On node update, check if another node have got the same name with same father
   * @param con A connection to the database
   * @param nd A NodeDetail contains new node data to compare
   * @return true if there is already a node with same name with same father false else
   * @throws java.sql.SQLException
   * @see NodeDetail
   * @since 1.0
   */
  public static boolean isSameNameSameLevelOnUpdate(Connection con, NodeDetail nd)
      throws SQLException {
    int nbItems = 0;
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(COUNT_NODES_PER_LEVEL_WITHOUT_CURRENT);
      prepStmt.setInt(1, Integer.parseInt(nd.getNodePK().getId()));
      prepStmt.setInt(2, nd.getLevel());
      prepStmt.setString(3, nd.getName());
      prepStmt.setString(4, nd.getNodePK().getComponentName());
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        nbItems = rs.getInt(1);
      }
    } catch (SQLException e) {
      SilverTrace.error("node", "NodeDAO.isSameNameSameLevelOnUpdate()", "root.EX_SQL_QUERY_FAILED",
          "selectQuery = " + COUNT_NODES_PER_LEVEL_WITHOUT_CURRENT + " id = " +
              nd.getNodePK().getId() + " level = " + nd.getLevel() + " name = " + nd.getName() +
              " compo name = " + nd.getNodePK().getComponentName(), e);
      throw e;
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return (nbItems != 0);
  }

  /**
   * Get children node PKs of a node
   * @param con A connection to the database
   * @param nodePK A NodePK
   * @return A collection of NodePK
   * @throws java.sql.SQLException
   * @see NodePK
   * @since 1.0
   */
  public static Collection<NodePK> getChildrenPKs(Connection con, NodePK nodePK)
      throws SQLException {
    List<NodePK> a = null;
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(SELECT_CHILDREN_IDS);
      prepStmt.setInt(1, Integer.parseInt(nodePK.getId()));
      prepStmt.setString(2, nodePK.getComponentName());
      rs = prepStmt.executeQuery();
      a = new ArrayList<>();
      while (rs.next()) {
        String nodeId = String.valueOf(rs.getInt("nodeid"));
        NodePK n = new NodePK(nodeId, nodePK);
        a.add(n); /* Stockage du sous thème */

      }
    } catch (SQLException e) {
      SilverTrace.error("node", "NodeDAO.getChildrenPKs()", "root.EX_SQL_QUERY_FAILED",
          "childrenStatement = " + SELECT_CHILDREN_IDS + " id = " + nodePK.getId() +
              " compo name = " + nodePK.getComponentName(), e);
      throw e;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return a;
  }

  /**
   * Get descendant node PKs of a node
   * @param con A connection to the database
   * @param nodePK A NodePK
   * @return A collection of NodePK
   * @throws java.sql.SQLException
   * @see NodePK
   * @since 1.0
   */
  public static Collection<NodePK> getDescendantPKs(Connection con, NodePK nodePK)
      throws SQLException {
    String path = null;
    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    try {
      prepStmt = con.prepareStatement(SELECT_DESCENDANTS_PK);
      prepStmt.setInt(1, Integer.parseInt(nodePK.getId()));
      prepStmt.setString(2, nodePK.getComponentName());
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        path = rs.getString(1);
      }
    } catch (SQLException e) {
      SilverTrace.error("node", "NodeDAO.getDescendantPKs()", "root.EX_SQL_QUERY_FAILED",
          "selectQuery = " + SELECT_DESCENDANTS_PK + " id = " + nodePK.getId() + " compo name = " +
              nodePK.getComponentName(), e);
      throw e;
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    List<NodePK> a = new ArrayList<>();
    if (path != null) {
      path = path + nodePK.getId() + "/%";
      try {
        prepStmt = con.prepareStatement(SELECT_DESCENDANTS_ID_BY_PATH);
        prepStmt.setString(1, path);
        prepStmt.setString(2, nodePK.getComponentName());
        rs = prepStmt.executeQuery();
        String nodeId;
        while (rs.next()) {
          nodeId = String.valueOf(rs.getInt("nodeid"));
          NodePK n = new NodePK(nodeId, nodePK);
          a.add(n); /* Stockage du sous thème */

        }
      } catch (SQLException e) {
        SilverTrace.error("node", "NodeDAO.getDescendantPKs()", "root.EX_SQL_QUERY_FAILED",
            "selectQuery = " + SELECT_DESCENDANTS_ID_BY_PATH + " compo name = " +
                nodePK.getComponentName(), e);
        throw e;
      } finally {
        DBUtil.close(rs, prepStmt);
      }
    }

    return a;
  }

  /**
   * Get descendant nodeDetails of a node
   * @param con A connection to the database
   * @param nodePK A NodePK
   * @return A List of NodeDetail
   * @throws SQLException
   * @see NodePK
   * @since 1.6
   */
  public static List<NodeDetail> getDescendantDetails(Connection con, NodePK nodePK)
      throws SQLException {

    String path = null;
    StringBuilder selectQuery = new StringBuilder();
    selectQuery.append("SELECT nodePath from ").append(nodePK.getTableName())
        .append(" where nodeId = ? and instanceId = ?");
    List<NodeDetail> a = new ArrayList<>();

    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    try {
      prepStmt = con.prepareStatement(selectQuery.toString());
      prepStmt.setInt(1, Integer.parseInt(nodePK.getId()));
      prepStmt.setString(2, nodePK.getComponentName());
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        path = rs.getString(1);
      }
    } catch (SQLException e) {
      SilverTrace.error("node", "NodeDAO.getDescendantDetails()", "root.EX_SQL_QUERY_FAILED",
          "selectQuery = " + selectQuery.toString() + " id = " + nodePK.getId() + " compo name = " +
              nodePK.getComponentName(), e);
      throw e;
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    if (path != null) {
      // path = path + "/%";
      path = path + nodePK.getId() + "/%";
      selectQuery = new StringBuilder();
      selectQuery.append("select * from ").append(nodePK.getTableName());
      selectQuery.append(" where nodePath like '").append(path).append("'");
      selectQuery.append(" and instanceId = ? order by nodePath");

      try {
        prepStmt = con.prepareStatement(selectQuery.toString());
        prepStmt.setString(1, nodePK.getComponentName());
        rs = prepStmt.executeQuery();
        while (rs.next()) {
          NodeDetail nd = resultSet2NodeDetail(rs, nodePK);
          a.add(nd);
        }
      } catch (SQLException e) {
        SilverTrace.error("node", "NodeDAO.getDescendantDetails()", "root.EX_SQL_QUERY_FAILED",
            "selectQuery = " + selectQuery.toString() + " compo name = " +
                nodePK.getComponentName(), e);
        throw e;
      } finally {
        DBUtil.close(rs, prepStmt);
      }
    }

    return a;
  }

  /**
   * Get descendant nodeDetails of a node
   * @param con A connection to the database
   * @param node A NodeDetail
   * @return A List of NodeDetail
   * @throws SQLException
   * @since 4.07
   */
  public static List<NodeDetail> getDescendantDetails(Connection con, NodeDetail node)
      throws SQLException {
    String path = node.getPath() + node.getNodePK().getId() + "/%";

    StringBuilder selectQuery = new StringBuilder();
    selectQuery.append("select * from ").append(node.getNodePK().getTableName());
    selectQuery.append(" where nodePath like '").append(path).append("'");
    selectQuery.append(" and instanceId = ? order by nodePath");

    ArrayList<NodeDetail> a = new ArrayList<>();

    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    try {
      prepStmt = con.prepareStatement(selectQuery.toString());
      prepStmt.setString(1, node.getNodePK().getComponentName());
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        NodeDetail nd = resultSet2NodeDetail(rs, node.getNodePK());
        a.add(nd);
      }
    } catch (SQLException e) {
      SilverTrace.error("node", "NodeDAO.getDescendantDetails()", "root.EX_SQL_QUERY_FAILED",
          "selectQuery = " + selectQuery.toString() + " compo name = " +
              node.getNodePK().getComponentName(), e);
      throw e;
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return a;
  }

  /**
   * Get nodeDetails by level.
   * @param con A connection to the database
   * @param nodePK
   * @param level
   * @return A collection of NodeDetail
   * @throws SQLException
   * @since 1.6
   */
  public static List<NodeDetail> getHeadersByLevel(Connection con, NodePK nodePK, int level)
      throws SQLException {

    List<NodeDetail> headers = new ArrayList<>();

    StringBuilder nodeStatement = new StringBuilder();
    nodeStatement.append("select * from ").append(nodePK.getTableName());
    nodeStatement.append(" where nodeLevelNumber=").append(level);
    nodeStatement.append(" and instanceId='").append(nodePK.getComponentName()).append("'");
    nodeStatement.append(" order by ordernumber asc, nodeId asc ");
    Statement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(nodeStatement.toString());
      while (rs.next()) {
        NodeDetail nd = resultSet2NodeDetail(rs, nodePK);

        setTranslations(con, nd);

        headers.add(nd);
      }
    } catch (SQLException e) {
      SilverTrace.error("node", "NodeDAO.getHeadersByLevel()", "root.EX_SQL_QUERY_FAILED",
          "nodeStatement = " + nodeStatement.toString(), e);
      throw e;
    } finally {
      DBUtil.close(rs, stmt);
    }

    return headers;
  }

  private static void setTranslations(Connection con, NodeDetail node) throws SQLException {
    // Add default translation
    NodeI18NDetail nodeI18NDetail = new NodeI18NDetail(node.getLanguage(), node.getName(), node.
        getDescription());
    node.addTranslation(nodeI18NDetail);
    if (I18NHelper.isI18nContentActivated) {
      List<NodeI18NDetail> translations = NodeI18NDAO.getTranslations(con, node.getId());
      for (int t = 0; translations != null && t < translations.size(); t++) {
        nodeI18NDetail = translations.get(t);
        node.addTranslation(nodeI18NDetail);
      }
    }
  }

  /**
   * Get all nodeDetails
   * @param con A connection to the database
   * @param nodePK
   * @return A collection of NodeDetail
   * @throws SQLException
   * @since 1.6
   */
  public static List<NodeDetail> getAllHeaders(Connection con, NodePK nodePK) throws SQLException {
    return getAllHeaders(con, nodePK, null, 0);
  }

  public static List<NodeDetail> getAllHeaders(Connection con, NodePK nodePK, String sorting)
      throws SQLException {
    return getAllHeaders(con, nodePK, sorting, 0);
  }

  public static List<NodeDetail> getAllHeaders(Connection con, NodePK nodePK, int level)
      throws SQLException {
    return getAllHeaders(con, nodePK, null, level);
  }

  /**
   * Get all nodeDetails
   * @param con A connection to the database
   * @param nodePK
   * @param sorting
   * @param level
   * @return A collection of NodeDetail
   * @throws SQLException
   * @since 1.6
   */
  public static List<NodeDetail> getAllHeaders(Connection con, NodePK nodePK, String sorting,
      int level) throws SQLException {

    List<NodeDetail> headers = new ArrayList<>();
    StringBuilder nodeStatement = new StringBuilder();

    nodeStatement.append("select * from ").append(nodePK.getTableName());
    nodeStatement.append(" where instanceId ='").append(nodePK.getComponentName()).append("'");

    if (level > 0) {
      nodeStatement.append(" and nodeLevelNumber = ").append(level);
    }

    if (StringUtil.isDefined(sorting)) {
      nodeStatement.append(" order by ").append(sorting);
    } else {
      nodeStatement.append(" order by nodePath, orderNumber");
    }

    Statement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(nodeStatement.toString());
      while (rs.next()) {
        NodeDetail nd = resultSet2NodeDetail(rs, nodePK);

        setTranslations(con, nd);

        headers.add(nd);
      }
    } catch (SQLException e) {
      SilverTrace.error("node", "NodeDAO.getAllHeaders()", "root.EX_SQL_QUERY_FAILED",
          "nodeStatement = " + nodeStatement.toString(), e);
      throw e;
    } finally {
      DBUtil.close(rs, stmt);
    }

    return headers;
  }

  public static List<NodeDetail> getSubTree(Connection con, NodePK nodePK, String status)
      throws SQLException {

    // get the path of the given nodePK
    NodeDetail detail = loadRow(con, nodePK);

    List<NodeDetail> headers = new ArrayList<>();

    if (status != null && status.length() > 0) {
      if (status.equals(detail.getStatus())) {
        headers.add(detail);
        getSubTree(con, headers, nodePK, status);
      }
    } else {
      headers.add(detail);
      getSubTree(con, headers, nodePK, status);
    }

    return headers;
  }

  private static void getSubTree(Connection con, List<NodeDetail> tree, NodePK nodePK,
      String status) throws SQLException {
    Collection<NodeDetail> childrenDetails = getChildrenDetails(con, nodePK);
    if (!childrenDetails.isEmpty()) {
      for (NodeDetail child : childrenDetails) {
        if (StringUtil.isDefined(status)) {
          if (status.equals(child.getStatus())) {
            tree.add(child);
            getSubTree(con, tree, child.getNodePK(), status);
          }
        } else {
          tree.add(child);
          getSubTree(con, tree, child.getNodePK(), status);
        }
      }
    }
  }

  /**
   * Get the path from root to a node
   * @param con A connection to the database
   * @param nodePK A NodePK
   * @return A collection of NodeDetail
   * @throws java.sql.SQLException
   * @see NodePK
   * @see NodeDetail
   * @since 1.0
   */
  public static Collection<NodeDetail> getAnotherPath(Connection con, NodePK nodePK)
      throws SQLException {
    List<NodeDetail> nodeDetails = new ArrayList<>();

    /* le node courant */
    NodeDetail nd = getAnotherHeader(con, nodePK);

    int currentLevel = nd.getLevel();

    nodeDetails.add(nd);
    currentLevel--;

    while (currentLevel >= 1) {
      nd = getAnotherHeader(con, nd.getFatherPK());
      nodeDetails.add(nd);
      currentLevel--;
    }
    return nodeDetails;
  }

  /**
   * ********************* Database Routines ***********************
   */
  /**
   * Create a NodeDetail from a ResultSet
   * @param rs the ResultSet which contains data
   * @param nodePK
   * @return the NodeDetail
   * @throws java.sql.SQLException
   * @see NodeDetail
   * @since 1.0
   */
  public static NodeDetail resultSet2NodeDetail(ResultSet rs, NodePK nodePK) throws SQLException {

    /* Récupération des données depuis la BD */
    NodePK pk = new NodePK(String.valueOf(rs.getInt(1)), nodePK);
    String name = rs.getString(2);
    String description = rs.getString(3);
    String creationDate = rs.getString(4);
    String creatorId = rs.getString(5);
    String path = rs.getString(6);
    int level = rs.getInt(7);
    NodePK fatherPK = new NodePK(String.valueOf(rs.getInt(8)), nodePK);
    String modelId = rs.getString(9);
    String status = rs.getString(10);
    String type = rs.getString(12);
    int order = rs.getInt(13);
    String language = rs.getString(14);
    int rightsDependsOn = rs.getInt(15);

    if (description == null) {
      description = "";
    }

    NodeDetail nd =
        new NodeDetail(pk, name, description, creationDate, creatorId, path, level, fatherPK,
            modelId, status, null, type);

    nd.setLanguage(language);
    nd.setOrder(order);
    nd.setRightsDependsOn(rightsDependsOn);
    return (nd);
  }

  /**
   * Get the detail of another Node
   * @param con A connection to the database
   * @param nodePK the PK of the Node
   * @return a NodeDetail
   * @throws java.sql.SQLException
   * @see NodeDetail
   * @since 1.0
   */
  public static NodeDetail getAnotherHeader(Connection con, NodePK nodePK) throws SQLException {

    String nodeId = nodePK.getId();

    StringBuilder nodeStatement = new StringBuilder();
    nodeStatement.append("select * from ").append(nodePK.getTableName());
    nodeStatement.append(" where nodeId = ").append(nodeId);
    nodeStatement.append(" and instanceId = '").append(nodePK.getComponentName()).append("'");

    Statement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(nodeStatement.toString());
      if (rs.next()) {
        NodeDetail nd = resultSet2NodeDetail(rs, nodePK);
        setTranslations(con, nd);
        return nd;
      } else {
        throw new NoSuchEntityException("Row for id " + nodeId + " not found in database.");
      }
    } catch (SQLException e) {
      SilverTrace.error("node", "NodeDAO.getAnotherHeader()", "root.EX_SQL_QUERY_FAILED",
          "nodeStatement = " + nodeStatement.toString(), e);
      throw e;
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  /**
   * Get the header of each child of the node
   * @param con A connection to the database
   * @param nodePK
   * @return a NodeDetail collection
   * @throws java.sql.SQLException
   * @see NodeDetail
   * @since 1.0
   */
  public static Collection<NodeDetail> getChildrenDetails(Connection con, NodePK nodePK)
      throws SQLException {
    String nodeId = nodePK.getId();
    List<NodeDetail> a = null;
    StringBuilder childrenStatement = new StringBuilder();
    childrenStatement.append("select * from ").append(nodePK.getTableName());
    childrenStatement.append(" where nodeFatherId = ? ");
    childrenStatement.append(" and instanceId = ? ");
    childrenStatement.append(" order by orderNumber asc");
    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    try {
      prepStmt = con.prepareStatement(childrenStatement.toString());
      prepStmt.setInt(1, Integer.parseInt(nodeId));
      prepStmt.setString(2, nodePK.getComponentName());
      rs = prepStmt.executeQuery();
      a = new ArrayList<NodeDetail>();
      while (rs.next()) {
        NodeDetail nd = resultSet2NodeDetail(rs, nodePK);

        setTranslations(con, nd);

        a.add(nd);
      }
    } catch (SQLException e) {
      SilverTrace.error("node", "NodeDAO.getChildrenDetails()", "root.EX_SQL_QUERY_FAILED",
          "childrenStatement = " + childrenStatement.toString() + " nodeId = " + nodeId +
              " compo name = " + nodePK.getComponentName(), e);
      throw e;
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return a;
  }

  /**
   * Get the children number of this node
   * @param con A connection to the database
   * @param nodePK
   * @return a int
   * @throws java.sql.SQLException
   * @since 1.0
   */
  public static int getChildrenNumber(Connection con, NodePK nodePK) throws SQLException {

    int nbChildren = 0;

    StringBuilder selectQuery = new StringBuilder();
    selectQuery.append("select count(*) from ").append(nodePK.getTableName())
        .append(" where nodeFatherId = ? and instanceId = ?");

    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    try {
      prepStmt = con.prepareStatement(selectQuery.toString());
      prepStmt.setInt(1, Integer.parseInt(nodePK.getId()));
      prepStmt.setString(2, nodePK.getComponentName());
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        nbChildren = rs.getInt(1);
      }
    } catch (SQLException e) {
      SilverTrace.error("node", "NodeDAO.getChildrenNumber()", "root.EX_SQL_QUERY_FAILED",
          "selectQuery = " + selectQuery.toString() + " nodeId = " + nodePK.getId() +
              " compo name = " + nodePK.getComponentName(), e);
      throw e;
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return nbChildren;
  }

  /**
   * Insert into the database the data of a node
   * @param con A connection to the database
   * @param nd the NodeDetail which contains data
   * @return a NodePK which contains the new row id
   * @throws java.sql.SQLException
   * @see NodeDetail
   * @since 1.0
   */
  public static NodePK insertRow(Connection con, NodeDetail nd) throws SQLException {
    NodePK pk = nd.getNodePK();

    int newId = 0;

    String name = nd.getName();
    String description = nd.getDescription();
    String creationDate = nd.getCreationDate();
    if (!StringUtil.isDefined(creationDate)) {
      // Column "nodecreationdate" is not null
      // Adding this statement to prevent SQLException
      creationDate = DateUtil.today2SQLDate();
    }
    String creatorId = nd.getCreatorId();
    String path = nd.getPath();
    int level = nd.getLevel();
    String modelId = nd.getModelId();
    String status = nd.getStatus();
    String type = nd.getType();
    String language = nd.getLanguage();
    int fatherId = -1;
    int nbBrothers = 0;
    if (nd.getFatherPK() != null) {
      fatherId = Integer.parseInt(nd.getFatherPK().getId());
      nbBrothers = getChildrenNumber(con, nd.getFatherPK());
    }
    int order = nbBrothers + 1;

    try {
      if (nd.isUseId()) {
        newId = Integer.parseInt(nd.getNodePK().getId());
      } else {
        /* Recherche de la nouvelle PK de la table */
        newId = DBUtil.getNextId(nd.getNodePK().getTableName(), "nodeId");
      }
    } catch (Exception e) {
      throw new NodeRuntimeException("NodeDAO.insertRow()", SilverpeasRuntimeException.ERROR,
          "root.EX_GET_NEXTID_FAILED", e);
    }

    StringBuilder insertQuery = new StringBuilder();
    insertQuery.append("insert into ").append(nd.getNodePK().getTableName())
        .append(" values ( ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ?, ? , ?, ? , ?)");
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(insertQuery.toString());
      prepStmt.setInt(1, newId);
      prepStmt.setString(2, name);
      prepStmt.setString(3, description);
      prepStmt.setString(4, creationDate);
      prepStmt.setString(5, creatorId);
      prepStmt.setString(6, path);
      prepStmt.setInt(7, level);
      prepStmt.setInt(8, fatherId);
      prepStmt.setString(9, modelId);
      prepStmt.setString(10, status);
      prepStmt.setString(11, nd.getNodePK().getComponentName());
      prepStmt.setString(12, type);
      prepStmt.setInt(13, order);
      prepStmt.setString(14, language);
      prepStmt.setInt(15, nd.getRightsDependsOn());
      prepStmt.executeUpdate();

      pk.setId(String.valueOf(newId));

      unvalidateTree(con, nd.getNodePK());
    } finally {
      DBUtil.close(prepStmt);
    }

    return pk;
  }

  /**
   * Delete into the database a node but not it's descendants.
   * @param con a connection to the database
   * @param nodePK the node PK to delete.
   * @throws java.sql.SQLException
   * @see NodeDetail
   * @since 1.0
   */
  public static void deleteRow(Connection con, NodePK nodePK) throws SQLException {

    StringBuilder deleteQuery = new StringBuilder();
    deleteQuery.append("delete from ").append(nodePK.getTableName());
    deleteQuery.append(" where nodeId=").append(nodePK.getId());
    deleteQuery.append(" and instanceId='").append(nodePK.getComponentName()).append("'");

    Statement stmt = null;

    try {
      stmt = con.createStatement();
      stmt.executeUpdate(deleteQuery.toString());

      unvalidateTree(con, nodePK);
    } finally {
      DBUtil.close(stmt);
    }
  }

  /**
   * Check if a Node exists in database.
   * @param con the current connection to the database.
   * @param pk the node PK to find
   * @return the fat pk (pk + detail)
   * @throws java.sql.SQLException
   * @see NodePK
   * @since 1.0
   */
  public static NodeDetail selectByPrimaryKey(Connection con, NodePK pk) throws SQLException {

    try {
      return loadRow(con, pk);
    } catch (NodeRuntimeException e) {
      /*
       * NodeRuntimeException thrown by loadRow() should be replaced by returning null (not found)
       */
      return null;
    }
  }

  public static NodeDetail selectByNameAndFatherId(Connection con, NodePK pk, String name,
      int nodeFatherId) throws SQLException {

    try {
      return loadRow(con, pk, name, nodeFatherId);
    } catch (NodeRuntimeException e) {
      /*
       * NodeRuntimeException thrown by loadRow() should be replaced by returning null (not found)
       */
      return null;
    }
  }

  public static NodeDetail loadRow(Connection con, NodePK nodePK) throws SQLException {
    return loadRow(con, nodePK, true);
  }

  /**
   * Load node attributes from database
   * @param con a connection to the database
   * @param nodePK
   * @param getTranslations
   * @return the loaded node details.
   * @throws java.sql.SQLException
   * @since 1.0
   */
  public static NodeDetail loadRow(Connection con, NodePK nodePK, boolean getTranslations)
      throws SQLException {


    NodeDetail detail = null;


    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.prepareStatement(SELECT_NODE_BY_ID);
      stmt.setInt(1, Integer.parseInt(nodePK.getId()));
      stmt.setString(2, nodePK.getComponentName());
      rs = stmt.executeQuery();
      if (rs.next()) {
        detail = resultSet2NodeDetail(rs, nodePK);

        if (getTranslations) {
          setTranslations(con, detail);
        }
      } else {
        // throw new NoSuchEntityException("Row for id " + nodePK.getId() +
        // " not found in database.");
        throw new NodeRuntimeException("NodeDAO.loadRow()", SilverpeasRuntimeException.ERROR,
            "root.EX_CANT_LOAD_ENTITY_ATTRIBUTES", "NodeId = " + nodePK.getId());
      }
    } catch (SQLException e) {
      SilverTrace.error("node", "NodeDAO.loadRow()", "root.EX_SQL_QUERY_FAILED",
          "selectQuery = " + SELECT_NODE_BY_ID, e);
      throw e;
    } finally {
      DBUtil.close(rs, stmt);
    }

    return detail;
  }

  public static NodeDetail loadRow(Connection con, NodePK nodePK, String name, int nodeFatherId)
      throws SQLException {

    NodeDetail detail = null;
    StringBuilder selectQuery = new StringBuilder();
    selectQuery.append("select * from ").append(nodePK.getTableName());
    selectQuery.append(" where lower(nodename)=?");
    selectQuery.append(" and instanceId=?");
    selectQuery.append(" and nodefatherid=?");


    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.prepareStatement(selectQuery.toString());
      stmt.setString(1, name.toLowerCase());
      stmt.setString(2, nodePK.getComponentName());
      stmt.setInt(3, nodeFatherId);
      rs = stmt.executeQuery();
      if (rs.next()) {
        detail = resultSet2NodeDetail(rs, nodePK);

        setTranslations(con, detail);
      } else {
        // throw new NoSuchEntityException("Row for id " + nodePK.getId() +
        // " not found in database.");
        throw new NodeRuntimeException("NodeDAO.loadRow()", SilverpeasRuntimeException.ERROR,
            "root.EX_CANT_LOAD_ENTITY_ATTRIBUTES", "NodeId = " + nodePK.getId());
      }
    } catch (SQLException e) {
      SilverTrace.error("node", "NodeDAO.loadRow()", "root.EX_SQL_QUERY_FAILED",
          "selectQuery = " + selectQuery.toString(), e);
      throw e;
    } finally {
      DBUtil.close(rs, stmt);
    }

    return detail;
  }

  /**
   * Store node attributes into database
   * @param con a connection to the database
   * @param nodeDetail
   * @throws java.sql.SQLException
   * @since 1.0
   */
  public static void storeRow(Connection con, NodeDetail nodeDetail) throws SQLException {

    int rowCount = 0;
    StringBuilder updateStatement = new StringBuilder();
    updateStatement.append("update ").append(nodeDetail.getNodePK().getTableName());
    updateStatement.append(" set nodeName =  ? , nodeDescription = ? , nodePath = ? , ");
    updateStatement.append(
        " nodeLevelNumber = ? , nodeFatherId = ? , modelId = ? , nodeStatus = ? , " +
            "orderNumber = ?, lang = ?, rightsDependsOn = ? ");
    updateStatement.append(" where nodeId = ? and instanceId = ?");
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(updateStatement.toString());
      prepStmt.setString(1, nodeDetail.getName());
      prepStmt.setString(2, nodeDetail.getDescription());
      prepStmt.setString(3, nodeDetail.getPath());
      prepStmt.setInt(4, nodeDetail.getLevel());
      prepStmt.setInt(5, Integer.parseInt(nodeDetail.getFatherPK().getId()));
      prepStmt.setString(6, nodeDetail.getModelId());
      prepStmt.setString(7, nodeDetail.getStatus());
      prepStmt.setInt(8, nodeDetail.getOrder());
      prepStmt.setString(9, nodeDetail.getLanguage());
      prepStmt.setInt(10, nodeDetail.getRightsDependsOn());
      prepStmt.setInt(11, Integer.parseInt(nodeDetail.getNodePK().getId()));
      prepStmt.setString(12, nodeDetail.getNodePK().getComponentName());
      rowCount = prepStmt.executeUpdate();

      unvalidateTree(con, nodeDetail.getNodePK());
    } finally {
      DBUtil.close(prepStmt);
    }

    if (rowCount == 0) {
      throw new NodeRuntimeException("NodeDAO.storeRow()", SilverpeasRuntimeException.ERROR,
          "root.EX_CANT_STORE_ENTITY_ATTRIBUTES", "NodeId = " + nodeDetail.getNodePK().getId());
    }
  }

  public static void moveNode(Connection con, NodeDetail nodeDetail) throws SQLException {
    int rowCount = 0;
    StringBuilder updateStatement = new StringBuilder();
    updateStatement.append("update ").append(nodeDetail.getNodePK().getTableName());
    updateStatement.append(" set nodePath = ? , ");
    updateStatement.append(
        " nodeLevelNumber = ? , nodeFatherId = ? , instanceId = ? , orderNumber = ?, " +
            "rightsDependsOn = ? ");
    updateStatement.append(" where nodeId = ? ");
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(updateStatement.toString());
      prepStmt.setString(1, nodeDetail.getPath());
      prepStmt.setInt(2, nodeDetail.getLevel());
      prepStmt.setInt(3, Integer.parseInt(nodeDetail.getFatherPK().getId()));
      prepStmt.setString(4, nodeDetail.getNodePK().getInstanceId());
      prepStmt.setInt(5, nodeDetail.getOrder());
      prepStmt.setInt(6, nodeDetail.getRightsDependsOn());
      prepStmt.setInt(7, Integer.parseInt(nodeDetail.getNodePK().getId()));
      rowCount = prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }

    if (rowCount == 0) {
      throw new NodeRuntimeException("NodeDAO.storeRow()", SilverpeasRuntimeException.ERROR,
          "root.EX_CANT_STORE_ENTITY_ATTRIBUTES", "NodeId = " + nodeDetail.getNodePK().getId());
    }
  }

  public static void updateRightsDependency(Connection con, NodePK pk, int rightsDependsOn)
      throws SQLException {


    StringBuilder updateStatement = new StringBuilder();
    updateStatement.append("update ").append(pk.getTableName());
    updateStatement.append(" set rightsDependsOn =  ? ");
    updateStatement.append(" where nodeId = ? and instanceId = ?");
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(updateStatement.toString());
      prepStmt.setInt(1, rightsDependsOn);
      prepStmt.setInt(2, Integer.parseInt(pk.getId()));
      prepStmt.setString(3, pk.getInstanceId());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void sortNodes(Connection con, List<NodePK> nodePKs) throws SQLException {
    String query = "UPDATE SB_Node_Node SET orderNumber = ? WHERE nodeId = ? ";
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(query);
      int i = 0;
      for (NodePK nodePK : nodePKs) {
        prepStmt.setInt(1, i);
        prepStmt.setInt(2, Integer.parseInt(nodePK.getId()));
        prepStmt.executeUpdate();
        i++;
      }
    } finally {
      DBUtil.close(prepStmt);
    }
  }
}
