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

package com.stratelia.webactiv.util.node.ejb;

import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.util.i18n.Translation;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodeI18NDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.node.model.NodeRuntimeException;
import javax.ejb.NoSuchEntityException;

/**
 * This is the Node Data Access Object.
 * @author Nicolas Eysseric
 */
public class NodeDAO {

  private static final String SELECT_NODE_BY_ID = "SELECT nodeid, nodename, nodedescription, "
      + "nodecreationdate, nodecreatorid, nodepath, nodelevelnumber, nodefatherid, modelid, "
      + "nodestatus, instanceid, type, ordernumber, lang, rightsdependson FROM sb_node_node WHERE "
      + "nodeId = ? AND instanceId = ?";
  private static final String COUNT_NODES_PER_LEVEL =
      "SELECT COUNT(nodeid) as nb FROM sb_node_node "
      + "WHERE nodelevelnumber = ? AND nodeName = ? AND instanceid = ? ";
  private static final String COUNT_NODES_PER_LEVEL_WITHOUT_CURRENT =
      "SELECT COUNT(nodeid) as nb FROM sb_node_node "
      + "WHERE nodeid <> ? AND nodelevelnumber = ? AND nodeName = ? AND instanceid = ? ";
  private static final String SELECT_CHILDREN_IDS = "SELECT nodeid FROM sb_node_node WHERE "
      + "nodefatherid = ? AND instanceId = ? ORDER BY nodeid";
  private static final String SELECT_DESCENDANTS_PK = "SELECT nodepath FROM sb_node_node WHERE "
      + "nodeid = ? AND instanceid = ?";
  private static final String SELECT_DESCENDANTS_ID_BY_PATH = "SELECT nodeid FROM sb_node_node "
      + "WHERE nodePath LIKE ? AND instanceid = ? ORDER BY nodeid";
  private static Hashtable<String, ArrayList<NodeDetail>> alltrees =
      new Hashtable<String, ArrayList<NodeDetail>>();

  /**
   * This class must not be instanciated
   * @since 1.0
   */
  public NodeDAO() {
  }

  public static ArrayList<NodeDetail> getTree(Connection con, NodePK nodePK)
      throws SQLException {
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
   * @return true if there is already a node with same name with same father false else
   * @param con A connection to the database
   * @param nd A NodeDetail contains new node data to compare
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static boolean isSameNameSameLevelOnCreation(Connection con, NodeDetail nd) throws
      SQLException {
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
      SilverTrace.error("node", "NodeDAO.isSameNameSameLevelOnCreation()",
          "root.EX_SQL_QUERY_FAILED", "selectQuery = " + COUNT_NODES_PER_LEVEL
          + " level = " + nd.getLevel() + " name = " + nd.getName() + " compo name = "
          + nd.getNodePK().getComponentName(), e);
      throw e;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return nbItems != 0;
  }

  /**
   * On node update, check if another node have got the same name with same father
   * @return true if there is already a node with same name with same father false else
   * @param con A connection to the database
   * @param nd A NodeDetail contains new node data to compare
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static boolean isSameNameSameLevelOnUpdate(Connection con, NodeDetail nd) throws
      SQLException {
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
      SilverTrace.error("node", "NodeDAO.isSameNameSameLevelOnUpdate()",
          "root.EX_SQL_QUERY_FAILED",
          "selectQuery = " + COUNT_NODES_PER_LEVEL_WITHOUT_CURRENT + " id = "
          + nd.getNodePK().getId() + " level = " + nd.getLevel() + " name = " + nd.getName()
          + " compo name = " + nd.getNodePK().getComponentName(), e);
      throw e;
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return (nbItems != 0);
  }

  /**
   * Get children node PKs of a node
   * @return A collection of NodePK
   * @param con A connection to the database
   * @param nodePK A NodePK
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static Collection<NodePK> getChildrenPKs(Connection con, NodePK nodePK)
      throws SQLException {
    ArrayList<NodePK> a = null;
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(SELECT_CHILDREN_IDS);
      prepStmt.setInt(1, new Integer(nodePK.getId()).intValue());
      prepStmt.setString(2, nodePK.getComponentName());
      rs = prepStmt.executeQuery();
      a = new ArrayList<NodePK>();
      while (rs.next()) {
        String nodeId = String.valueOf(rs.getInt("nodeid"));
        NodePK n = new NodePK(nodeId, nodePK);
        a.add(n); /* Stockage du sous thème */
      }
    } catch (SQLException e) {
      SilverTrace.error("node", "NodeDAO.getChildrenPKs()", "root.EX_SQL_QUERY_FAILED",
          "childrenStatement = " + SELECT_CHILDREN_IDS + " id = " + nodePK.getId()
          + " compo name = " + nodePK.getComponentName(), e);
      throw e;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return a;
  }

  /**
   * Get descendant node PKs of a node
   * @return A collection of NodePK
   * @param con A connection to the database
   * @param nodePK A NodePK
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @exception java.sql.SQLException
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
      SilverTrace.error("node", "NodeDAO.getDescendantPKs()",
          "root.EX_SQL_QUERY_FAILED", "selectQuery = " + SELECT_DESCENDANTS_PK
          + " id = " + nodePK.getId() + " compo name = "
          + nodePK.getComponentName(), e);
      throw e;
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    ArrayList<NodePK> a = new ArrayList<NodePK>();
    if (path != null) {
      path = path + nodePK.getId() + "/%";
      try {
        prepStmt = con.prepareStatement(SELECT_DESCENDANTS_ID_BY_PATH);
        prepStmt.setString(1, path);
        prepStmt.setString(2, nodePK.getComponentName());
        rs = prepStmt.executeQuery();
        String nodeId = "";
        while (rs.next()) {
          nodeId = String.valueOf(rs.getInt("nodeid"));
          NodePK n = new NodePK(nodeId, nodePK);
          a.add(n); /* Stockage du sous thème */
        }
      } catch (SQLException e) {
        SilverTrace.error("node", "NodeDAO.getDescendantPKs()",
            "root.EX_SQL_QUERY_FAILED", "selectQuery = "
            + SELECT_DESCENDANTS_ID_BY_PATH + " compo name = "
            + nodePK.getComponentName(), e);
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
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @since 1.6
   */
  public static List<NodeDetail> getDescendantDetails(Connection con, NodePK nodePK)
      throws SQLException {

    String path = null;
    StringBuffer selectQuery = new StringBuffer();
    selectQuery.append("SELECT nodePath from ").append(nodePK.getTableName()).append(
        " where nodeId = ? and instanceId = ?");
    List<NodeDetail> a = new ArrayList<NodeDetail>();

    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    try {
      prepStmt = con.prepareStatement(selectQuery.toString());
      prepStmt.setInt(1, new Integer(nodePK.getId()).intValue());
      prepStmt.setString(2, nodePK.getComponentName());
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        path = rs.getString(1);
      }
    } catch (SQLException e) {
      SilverTrace.error("node", "NodeDAO.getDescendantDetails()",
          "root.EX_SQL_QUERY_FAILED", "selectQuery = " + selectQuery.toString()
          + " id = " + nodePK.getId() + " compo name = "
          + nodePK.getComponentName(), e);
      throw e;
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    if (path != null) {
      // path = path + "/%";
      path = path + nodePK.getId() + "/%";
      selectQuery = new StringBuffer();
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
        SilverTrace.error("node", "NodeDAO.getDescendantDetails()",
            "root.EX_SQL_QUERY_FAILED", "selectQuery = "
            + selectQuery.toString() + " compo name = "
            + nodePK.getComponentName(), e);
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

    StringBuffer selectQuery = new StringBuffer();
    selectQuery.append("select * from ").append(node.getNodePK().getTableName());
    selectQuery.append(" where nodePath like '").append(path).append("'");
    selectQuery.append(" and instanceId = ? order by nodePath");

    ArrayList<NodeDetail> a = new ArrayList<NodeDetail>();

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
      SilverTrace.error("node", "NodeDAO.getDescendantDetails()",
          "root.EX_SQL_QUERY_FAILED", "selectQuery = " + selectQuery.toString()
          + " compo name = " + node.getNodePK().getComponentName(), e);
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

    List<NodeDetail> headers = new ArrayList<NodeDetail>();

    StringBuffer nodeStatement = new StringBuffer();
    nodeStatement.append("select * from ").append(nodePK.getTableName());
    nodeStatement.append(" where nodeLevelNumber=").append(level);
    nodeStatement.append(" and instanceId='").append(nodePK.getComponentName()).append("'");
    nodeStatement.append(" order by nodeId asc ");
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
      SilverTrace.error("node", "NodeDAO.getHeadersByLevel()",
          "root.EX_SQL_QUERY_FAILED", "nodeStatement = "
          + nodeStatement.toString(), e);
      throw e;
    } finally {
      DBUtil.close(rs, stmt);
    }

    return headers;
  }

  private static void setTranslations(Connection con, NodeDetail node)
      throws SQLException {
    // Add default translation
    Translation nodeI18NDetail = new NodeI18NDetail(node.getLanguage(), node.getName(), node.
        getDescription());
    node.addTranslation(nodeI18NDetail);

    if (I18NHelper.isI18N) {
      List<Translation> translations = NodeI18NDAO.getTranslations(con, node.getId());

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
  public static List<NodeDetail> getAllHeaders(Connection con, NodePK nodePK,
      String sorting, int level) throws SQLException {

    List<NodeDetail> headers = new ArrayList<NodeDetail>();
    StringBuffer nodeStatement = new StringBuffer();

    nodeStatement.append("select * from ").append(nodePK.getTableName());
    nodeStatement.append(" where instanceId ='").append(
        nodePK.getComponentName()).append("'");

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
      SilverTrace.error("node", "NodeDAO.getAllHeaders()",
          "root.EX_SQL_QUERY_FAILED", "nodeStatement = "
          + nodeStatement.toString(), e);
      throw e;
    } finally {
      DBUtil.close(rs, stmt);
    }

    return headers;
  }

  public static List<NodeDetail> getSubTree(Connection con, NodePK nodePK, String status)
      throws SQLException {
    SilverTrace.info("node", "NodeDAO.getSubTree()",
        "root.MSG_GEN_ENTER_METHOD", "nodePK = " + nodePK + ", status = "
        + status);
    // get the path of the given nodePK
    NodeDetail detail = loadRow(con, nodePK);

    List<NodeDetail> headers = new ArrayList<NodeDetail>();

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
   * @return A collection of NodeDetail
   * @param con A connection to the database
   * @param nodePK A NodePK
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static Collection<NodeDetail> getAnotherPath(Connection con, NodePK nodePK)
      throws SQLException {
    List<NodeDetail> nodeDetails = new ArrayList<NodeDetail>();

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
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @throws java.sql.SQLException
   * @since 1.0
   */
  public static NodeDetail resultSet2NodeDetail(ResultSet rs, NodePK nodePK)
      throws SQLException {

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

    NodeDetail nd = new NodeDetail(pk, name, description, creationDate,
        creatorId, path, level, fatherPK, modelId, status, null, type);

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
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @throws java.sql.SQLException
   * @since 1.0
   */
  public static NodeDetail getAnotherHeader(Connection con, NodePK nodePK)
      throws SQLException {

    String nodeId = nodePK.getId();

    StringBuffer nodeStatement = new StringBuffer();
    nodeStatement.append("select * from ").append(nodePK.getTableName());
    nodeStatement.append(" where nodeId = ").append(nodeId);
    nodeStatement.append(" and instanceId = '").append(
        nodePK.getComponentName()).append("'");

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
        throw new NoSuchEntityException("Row for id " + nodeId
            + " not found in database.");
      }
    } catch (SQLException e) {
      SilverTrace.error("node", "NodeDAO.getAnotherHeader()",
          "root.EX_SQL_QUERY_FAILED", "nodeStatement = "
          + nodeStatement.toString(), e);
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
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @since 1.0
   */
  public static Collection<NodeDetail> getChildrenDetails(Connection con, NodePK nodePK)
      throws SQLException {
    return getChildrenDetails(con, nodePK, null);
  }

  public static Collection<NodeDetail> getChildrenDetails(Connection con, NodePK nodePK,
      String sorting) throws SQLException {

    String nodeId = nodePK.getId();
    List<NodeDetail> a = null;
    StringBuilder childrenStatement = new StringBuilder();
    childrenStatement.append("select * from ").append(nodePK.getTableName());
    childrenStatement.append(" where nodeFatherId = ? ");
    childrenStatement.append(" and instanceId = ? ");
    if (sorting != null) {
      childrenStatement.append(" order by ").append(sorting);
    } else {
      childrenStatement.append(" order by orderNumber asc");
    }
    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    try {
      prepStmt = con.prepareStatement(childrenStatement.toString());
      prepStmt.setInt(1, new Integer(nodeId).intValue());
      prepStmt.setString(2, nodePK.getComponentName());
      rs = prepStmt.executeQuery();
      a = new ArrayList<NodeDetail>();
      while (rs.next()) {
        NodeDetail nd = resultSet2NodeDetail(rs, nodePK);

        setTranslations(con, nd);

        a.add(nd);
      }
    } catch (SQLException e) {
      SilverTrace.error("node", "NodeDAO.getChildrenDetails()",
          "root.EX_SQL_QUERY_FAILED", "childrenStatement = "
          + childrenStatement.toString() + " nodeId = " + nodeId
          + " compo name = " + nodePK.getComponentName(), e);
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
  public static int getChildrenNumber(Connection con, NodePK nodePK)
      throws SQLException {

    int nbChildren = 0;

    StringBuffer selectQuery = new StringBuffer();
    selectQuery.append("select count(*) from ").append(nodePK.getTableName()).append(
        " where nodeFatherId = ? and instanceId = ?");

    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    try {
      prepStmt = con.prepareStatement(selectQuery.toString());
      prepStmt.setInt(1, new Integer(nodePK.getId()).intValue());
      prepStmt.setString(2, nodePK.getComponentName());
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        nbChildren = rs.getInt(1);
      }
    } catch (SQLException e) {
      SilverTrace.error("node", "NodeDAO.getChildrenNumber()",
          "root.EX_SQL_QUERY_FAILED", "selectQuery = " + selectQuery.toString()
          + " nodeId = " + nodePK.getId() + " compo name = "
          + nodePK.getComponentName(), e);
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
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @since 1.0
   */
  public static NodePK insertRow(Connection con, NodeDetail nd)
      throws SQLException {
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
    int fatherId = new Integer(nd.getFatherPK().getId()).intValue();
    String modelId = nd.getModelId();
    String status = nd.getStatus();
    String type = nd.getType();
    String language = nd.getLanguage();

    int nbBrothers = getChildrenNumber(con, nd.getFatherPK());
    SilverTrace.info("node", "NodeDAO.insertRow()", "root.MSG_GEN_PARAM_VALUE",
        "fatherId = " + nd.getFatherPK().getId() + ", nbBrothers = "
        + nbBrothers);

    int order = nbBrothers + 1;

    try {
      if (nd.isUseId()) {
        newId = Integer.parseInt(nd.getNodePK().getId());
      } else {
        /* Recherche de la nouvelle PK de la table */
        newId = DBUtil.getNextId(nd.getNodePK().getTableName(), "nodeId");
      }
    } catch (Exception e) {
      throw new NodeRuntimeException("NodeDAO.insertRow()",
          SilverpeasRuntimeException.ERROR, "root.EX_GET_NEXTID_FAILED", e);
    }

    StringBuffer insertQuery = new StringBuffer();
    insertQuery.append("insert into ").append(nd.getNodePK().getTableName()).append(
        " values ( ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ?, ? , ?, ? , ?)");
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

      pk.setId(new Integer(newId).toString());

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
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @since 1.0
   */
  public static void deleteRow(Connection con, NodePK nodePK)
      throws SQLException {

    StringBuffer deleteQuery = new StringBuffer();
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
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @since 1.0
   */
  public static NodePK selectByPrimaryKey(Connection con, NodePK pk)
      throws SQLException {

    try {
      NodeDetail detail = loadRow(con, pk);
      NodePK primary = new NodePK(pk.getId(), pk);

      primary.nodeDetail = detail;
      return primary;

    } catch (NodeRuntimeException e) {
      /*
       * NodeRuntimeException thrown by loadRow() should be replaced by returning null (not found)
       */
      return null;
    }
  }

  public static NodePK selectByNameAndFatherId(Connection con, NodePK pk,
      String name, int nodeFatherId) throws SQLException {

    try {
      NodeDetail detail = loadRow(con, pk, name, nodeFatherId);
      NodePK primary = new NodePK(detail.getNodePK().getId(), detail.getNodePK().
          getInstanceId());

      primary.nodeDetail = detail;
      return primary;

    } catch (NodeRuntimeException e) {
      /*
       * NodeRuntimeException thrown by loadRow() should be replaced by returning null (not found)
       */
      return null;
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
  public static Collection<NodePK> selectByFatherPrimaryKey(Connection con, NodePK pk)
      throws SQLException {

    Collection<NodeDetail> children = getChildrenDetails(con, pk);
    List<NodePK> result = new ArrayList<NodePK>();
    NodeDetail detail = null;
    NodePK primary = null;

    for (Iterator<NodeDetail> i = children.iterator(); i.hasNext();) {
      detail = i.next();
      primary = detail.getNodePK();
      primary.nodeDetail = detail;
      result.add(primary);
    }
    return result;
  }

  public static NodeDetail loadRow(Connection con, NodePK nodePK)
      throws SQLException {
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
  public static NodeDetail loadRow(Connection con, NodePK nodePK,
      boolean getTranslations) throws SQLException {

    SilverTrace.info("node", "NodeDAO.loadRow()", "root.MSG_GEN_PARAM_VALUE",
        "nodePK = " + nodePK);
    NodeDetail detail = null;
    SilverTrace.info("node", "NodeDAO.loadRow()", "root.MSG_GEN_PARAM_VALUE",
        "selectQuery = " + SELECT_NODE_BY_ID);

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
        throw new NodeRuntimeException("NodeDAO.loadRow()",
            SilverpeasRuntimeException.ERROR,
            "root.EX_CANT_LOAD_ENTITY_ATTRIBUTES", "NodeId = " + nodePK.getId());
      }
    } catch (SQLException e) {
      SilverTrace.error("node", "NodeDAO.loadRow()",
          "root.EX_SQL_QUERY_FAILED", "selectQuery = " + SELECT_NODE_BY_ID, e);
      throw e;
    } finally {
      DBUtil.close(rs, stmt);
    }

    return detail;
  }

  public static NodeDetail loadRow(Connection con, NodePK nodePK, String name,
      int nodeFatherId) throws SQLException {
    SilverTrace.info("node", "NodeDAO.loadRow()", "root.MSG_GEN_PARAM_VALUE",
        "nodePK = " + nodePK);
    NodeDetail detail = null;
    StringBuilder selectQuery = new StringBuilder();
    selectQuery.append("select * from ").append(nodePK.getTableName());
    selectQuery.append(" where lower(nodename)=?");
    selectQuery.append(" and instanceId=?");
    selectQuery.append(" and nodefatherid=?");
    SilverTrace.info("node", "NodeDAO.loadRow()", "root.MSG_GEN_PARAM_VALUE",
        "selectQuery = " + selectQuery.toString());

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
        throw new NodeRuntimeException("NodeDAO.loadRow()",
            SilverpeasRuntimeException.ERROR,
            "root.EX_CANT_LOAD_ENTITY_ATTRIBUTES", "NodeId = " + nodePK.getId());
      }
    } catch (SQLException e) {
      SilverTrace.error("node", "NodeDAO.loadRow()",
          "root.EX_SQL_QUERY_FAILED",
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
    StringBuffer updateStatement = new StringBuffer();
    updateStatement.append("update ").append(
        nodeDetail.getNodePK().getTableName());
    updateStatement.append(" set nodeName =  ? , nodeDescription = ? , nodePath = ? , ");
    updateStatement
        .append(
        " nodeLevelNumber = ? , nodeFatherId = ? , modelId = ? , nodeStatus = ? , orderNumber = ?, lang = ?, rightsDependsOn = ? ");
    updateStatement.append(" where nodeId = ? and instanceId = ?");
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(updateStatement.toString());
      prepStmt.setString(1, nodeDetail.getName());
      prepStmt.setString(2, nodeDetail.getDescription());
      prepStmt.setString(3, nodeDetail.getPath());
      prepStmt.setInt(4, nodeDetail.getLevel());
      prepStmt.setInt(5, new Integer(nodeDetail.getFatherPK().getId()).intValue());
      prepStmt.setString(6, nodeDetail.getModelId());
      prepStmt.setString(7, nodeDetail.getStatus());
      prepStmt.setInt(8, nodeDetail.getOrder());
      prepStmt.setString(9, nodeDetail.getLanguage());
      prepStmt.setInt(10, nodeDetail.getRightsDependsOn());
      prepStmt.setInt(11, new Integer(nodeDetail.getNodePK().getId()).intValue());
      prepStmt.setString(12, nodeDetail.getNodePK().getComponentName());
      rowCount = prepStmt.executeUpdate();

      unvalidateTree(con, nodeDetail.getNodePK());
    } finally {
      DBUtil.close(prepStmt);
    }

    if (rowCount == 0) {
      throw new NodeRuntimeException("NodeDAO.storeRow()",
          SilverpeasRuntimeException.ERROR,
          "root.EX_CANT_STORE_ENTITY_ATTRIBUTES", "NodeId = "
          + nodeDetail.getNodePK().getId());
    }
  }

  public static void moveNode(Connection con, NodeDetail nodeDetail)
      throws SQLException {
    int rowCount = 0;
    StringBuffer updateStatement = new StringBuffer();
    updateStatement.append("update ").append(
        nodeDetail.getNodePK().getTableName());
    updateStatement.append(" set nodePath = ? , ");
    updateStatement
        .append(
        " nodeLevelNumber = ? , nodeFatherId = ? , instanceId = ? , orderNumber = ?, rightsDependsOn = ? ");
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
      throw new NodeRuntimeException("NodeDAO.storeRow()",
          SilverpeasRuntimeException.ERROR,
          "root.EX_CANT_STORE_ENTITY_ATTRIBUTES", "NodeId = "
          + nodeDetail.getNodePK().getId());
    }
  }

  public static void updateRightsDependency(Connection con, NodePK pk,
      int rightsDependsOn) throws SQLException {
    SilverTrace.info("node", "NodeDAO.updateRightsDependency()", "root.MSG_GEN_ENTER_METHOD",
        "nodePK = " + pk.toString() + ", rightsDependsOn = " + rightsDependsOn);

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
    NodePK nodePK = new NodePK("useless");
    StringBuilder updateQuery = new StringBuilder();
    updateQuery.append("update ").append(nodePK.getTableName());
    updateQuery.append(" set orderNumber = ? ");
    updateQuery.append(" where nodeId = ? ");
    String query = updateQuery.toString();
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(query);
      for (int i = 0; i < nodePKs.size(); i++) {
        nodePK = nodePKs.get(i);
        prepStmt.setInt(1, i);
        prepStmt.setInt(2, Integer.parseInt(nodePK.getId()));

        prepStmt.executeUpdate();
      }

    } finally {
      DBUtil.close(prepStmt);
    }
  }
}
