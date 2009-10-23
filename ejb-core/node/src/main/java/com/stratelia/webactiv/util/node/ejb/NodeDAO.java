/**
 * Copyright (C) 2000 - 2009 Silverpeas This program is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Affero General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing" This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
// TODO : reporter dans CVS (done)
package com.stratelia.webactiv.util.node.ejb;

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

/**
 * This is the Node Data Access Object.
 * 
 * @author Nicolas Eysseric
 */
public class NodeDAO {
  private static Hashtable alltrees = new Hashtable();

  /**
   * This class must not be instanciated
   * 
   * @since 1.0
   */
  public NodeDAO() {
  }

  public static ArrayList getTree(Connection con, NodePK nodePK) throws SQLException {
    ArrayList tree = (ArrayList) alltrees.get(nodePK.getComponentName());
    if (tree == null) {
      tree = (ArrayList) getAllHeaders(con, nodePK);
      alltrees.put(nodePK.getComponentName(), tree);
    }
    return tree;
  }

  public static void unvalidateTree(Connection con, NodePK nodePK) {
    alltrees.remove(nodePK.getComponentName());
  }

  /**
   * On node creation, check if another node have got the same name with same father
   * 
   * @return true if there is already a node with same name with same father false else
   * @param con
   *          A connection to the database
   * @param nd
   *          A NodeDetail contains new node data to compare
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @exception java.sql.SQLException
   * @since 1.0
   */

  public static boolean isSameNameSameLevelOnCreation(Connection con, NodeDetail nd)
      throws SQLException {

    int nbItems = 0;
    int level = nd.getLevel();
    String name = nd.getName();

    StringBuffer selectQuery = new StringBuffer();
    selectQuery.append("SELECT count(nodeId) FROM ").append(nd.getNodePK().getTableName());
    selectQuery.append(" WHERE nodeLevelNumber = ? ");
    selectQuery.append(" AND nodeName = ? ");
    selectQuery.append(" AND instanceId = ? ");

    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    try {
      prepStmt = con.prepareStatement(selectQuery.toString());
      prepStmt.setInt(1, level);
      prepStmt.setString(2, name);
      prepStmt.setString(3, nd.getNodePK().getComponentName());
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        nbItems = rs.getInt(1);
      }
    } catch (SQLException e) {
      SilverTrace
          .error("node", "NodeDAO.isSameNameSameLevelOnCreation()", "root.EX_SQL_QUERY_FAILED",
              "selectQuery = " + selectQuery.toString() + " level = " + level + " name = " + name
                  + " compo name = " + nd.getNodePK().getComponentName(), e);
      throw e;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return (nbItems != 0);
  }

  /**
   * On node update, check if another node have got the same name with same father
   * 
   * @return true if there is already a node with same name with same father false else
   * @param con
   *          A connection to the database
   * @param nd
   *          A NodeDetail contains new node data to compare
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static boolean isSameNameSameLevelOnUpdate(Connection con, NodeDetail nd)
      throws SQLException {
    int nbItems = 0;
    String id = nd.getNodePK().getId();
    int level = nd.getLevel();
    String name = nd.getName();

    StringBuffer selectQuery = new StringBuffer();
    selectQuery.append("SELECT count(nodeId) FROM ").append(nd.getNodePK().getTableName());
    selectQuery.append(" WHERE nodeId <> ? ");
    selectQuery.append(" AND nodeLevelNumber = ? ");
    selectQuery.append(" AND nodeName = ? ");
    selectQuery.append(" AND instanceId = ? ");

    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    try {
      prepStmt = con.prepareStatement(selectQuery.toString());
      prepStmt.setInt(1, new Integer(id).intValue());
      prepStmt.setInt(2, level);
      prepStmt.setString(3, name);
      prepStmt.setString(4, nd.getNodePK().getComponentName());

      rs = prepStmt.executeQuery();
      if (rs.next()) {
        nbItems = rs.getInt(1);
      }
    } catch (SQLException e) {
      SilverTrace.error("node", "NodeDAO.isSameNameSameLevelOnUpdate()",
          "root.EX_SQL_QUERY_FAILED", "selectQuery = " + selectQuery.toString() + " id = " + id
              + " level = " + level + " name = " + name + " compo name = "
              + nd.getNodePK().getComponentName(), e);
      throw e;
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return (nbItems != 0);
  }

  /**
   * Get children node PKs of a node
   * 
   * @return A collection of NodePK
   * @param con
   *          A connection to the database
   * @param nodePK
   *          A NodePK
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static Collection getChildrenPKs(Connection con, NodePK nodePK) throws SQLException {
    ArrayList a = null;
    StringBuffer childrenStatement = new StringBuffer();
    childrenStatement.append("select nodeId from ").append(nodePK.getTableName());
    childrenStatement.append(" where nodeFatherId = ? ");
    childrenStatement.append(" and instanceId = ? ");
    childrenStatement.append(" order by nodeId");

    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    try {
      prepStmt = con.prepareStatement(childrenStatement.toString());
      prepStmt.setInt(1, new Integer(nodePK.getId()).intValue());
      prepStmt.setString(2, nodePK.getComponentName());
      rs = prepStmt.executeQuery();
      a = new ArrayList();
      String nodeId = "";

      while (rs.next()) {
        nodeId = new Integer(rs.getInt(1)).toString();
        NodePK n = new NodePK(nodeId, nodePK);

        a.add(n); /* Stockage du sous thème */
      }
    } catch (SQLException e) {
      SilverTrace.error("node", "NodeDAO.getChildrenPKs()", "root.EX_SQL_QUERY_FAILED",
          "childrenStatement = " + childrenStatement.toString() + " id = " + nodePK.getId()
              + " compo name = " + nodePK.getComponentName(), e);
      throw e;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return a;
  }

  /**
   * Get descendant node PKs of a node
   * 
   * @return A collection of NodePK
   * @param con
   *          A connection to the database
   * @param nodePK
   *          A NodePK
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static Collection getDescendantPKs(Connection con, NodePK nodePK) throws SQLException {

    String path = null;
    StringBuffer selectQuery = new StringBuffer();
    selectQuery.append("select nodePath from ").append(nodePK.getTableName()).append(
        " where nodeId = ? and instanceId = ? ");
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
      SilverTrace.error("node", "NodeDAO.getDescendantPKs()", "root.EX_SQL_QUERY_FAILED",
          "selectQuery = " + selectQuery.toString() + " id = " + nodePK.getId() + " compo name = "
              + nodePK.getComponentName(), e);
      throw e;
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    ArrayList a = new ArrayList();
    if (path != null) {
      // path = path + "/%";
      path = path + nodePK.getId() + "/%";

      selectQuery = new StringBuffer();
      selectQuery.append("select nodeId from ").append(nodePK.getTableName());
      selectQuery.append(" where nodePath like '").append(path).append("'");
      selectQuery.append(" and instanceId = ? order by nodeId");

      try {
        prepStmt = con.prepareStatement(selectQuery.toString());
        prepStmt.setString(1, nodePK.getComponentName());
        rs = prepStmt.executeQuery();
        String nodeId = "";
        while (rs.next()) {
          nodeId = new Integer(rs.getInt(1)).toString();
          NodePK n = new NodePK(nodeId, nodePK);

          a.add(n); /* Stockage du sous thème */
        }
      } catch (SQLException e) {
        SilverTrace.error("node", "NodeDAO.getDescendantPKs()", "root.EX_SQL_QUERY_FAILED",
            "selectQuery = " + selectQuery.toString() + " compo name = "
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
   * 
   * @return A List of NodeDetail
   * @param con
   *          A connection to the database
   * @param nodePK
   *          A NodePK
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @since 1.6
   */
  public static List getDescendantDetails(Connection con, NodePK nodePK) throws SQLException {

    String path = null;
    StringBuffer selectQuery = new StringBuffer();
    selectQuery.append("SELECT nodePath from ").append(nodePK.getTableName()).append(
        " where nodeId = ? and instanceId = ?");
    ArrayList a = new ArrayList();

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
      SilverTrace.error("node", "NodeDAO.getDescendantDetails()", "root.EX_SQL_QUERY_FAILED",
          "selectQuery = " + selectQuery.toString() + " id = " + nodePK.getId() + " compo name = "
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
        SilverTrace.error("node", "NodeDAO.getDescendantDetails()", "root.EX_SQL_QUERY_FAILED",
            "selectQuery = " + selectQuery.toString() + " compo name = "
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
   * 
   * @return A List of NodeDetail
   * @param con
   *          A connection to the database
   * @param node
   *          A NodeDetail
   * @since 4.07
   */
  public static List getDescendantDetails(Connection con, NodeDetail node) throws SQLException {
    String path = node.getPath() + node.getNodePK().getId() + "/%";

    StringBuffer selectQuery = new StringBuffer();
    selectQuery.append("select * from ").append(node.getNodePK().getTableName());
    selectQuery.append(" where nodePath like '").append(path).append("'");
    selectQuery.append(" and instanceId = ? order by nodePath");

    ArrayList a = new ArrayList();

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
          "selectQuery = " + selectQuery.toString() + " compo name = "
              + node.getNodePK().getComponentName(), e);
      throw e;
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return a;
  }

  /**
   * Get nodeDetails by level
   * 
   * @return A collection of NodeDetail
   * @since 1.6
   */
  public static List getHeadersByLevel(Connection con, NodePK nodePK, int level)
      throws SQLException {

    ArrayList headers = new ArrayList();

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
    NodeI18NDetail nodeI18NDetail = new NodeI18NDetail(node.getLanguage(), node.getName(), node
        .getDescription());
    node.addTranslation((Translation) nodeI18NDetail);

    if (I18NHelper.isI18N) {
      List translations = NodeI18NDAO.getTranslations(con, node.getId());

      for (int t = 0; translations != null && t < translations.size(); t++) {
        nodeI18NDetail = (NodeI18NDetail) translations.get(t);
        node.addTranslation((Translation) nodeI18NDetail);
      }
    }
  }

  /**
   * Get all nodeDetails
   * 
   * @return A collection of NodeDetail
   * @since 1.6
   */
  public static List getAllHeaders(Connection con, NodePK nodePK) throws SQLException {
    return getAllHeaders(con, nodePK, null);
  }

  /**
   * Get all nodeDetails
   * 
   * @return A collection of NodeDetail
   * @since 1.6
   */
  public static List getAllHeaders(Connection con, NodePK nodePK, String sorting)
      throws SQLException {
    ArrayList headers = new ArrayList();
    StringBuffer nodeStatement = new StringBuffer();

    nodeStatement.append("select * from ").append(nodePK.getTableName());
    nodeStatement.append(" where instanceId ='").append(nodePK.getComponentName()).append("'");
    if (StringUtil.isDefined(sorting))
      nodeStatement.append(" order by ").append(sorting);
    else
      nodeStatement.append(" order by nodePath, orderNumber");

    Statement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(nodeStatement.toString());
      while (rs.next()) {
        NodeDetail nd = resultSet2NodeDetail(rs, nodePK);

        // Add default translation
        NodeI18NDetail nodeI18NDetail = new NodeI18NDetail(nd.getLanguage(), nd.getName(), nd
            .getDescription());
        nd.addTranslation((Translation) nodeI18NDetail);

        List translations = NodeI18NDAO.getTranslations(con, nd.getId());

        for (int t = 0; translations != null && t < translations.size(); t++) {
          nodeI18NDetail = (NodeI18NDetail) translations.get(t);
          nd.addTranslation((Translation) nodeI18NDetail);
        }

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

  public static List getSubTree(Connection con, NodePK nodePK, String status) throws SQLException {
    SilverTrace.info("node", "NodeDAO.getSubTree()", "root.MSG_GEN_ENTER_METHOD", "nodePK = "
        + nodePK + ", status = " + status);
    // get the path of the given nodePK
    NodeDetail detail = loadRow(con, nodePK);

    List headers = new ArrayList();

    if (status != null && status.length() > 0) {
      if (status.equals(detail.getStatus())) {
        headers.add(detail);
        headers = getSubTree(con, headers, nodePK, status);
      }
    } else {
      headers.add(detail);
      headers = getSubTree(con, headers, nodePK, status);
    }

    return headers;
  }

  private static List getSubTree(Connection con, List tree, NodePK nodePK, String status)
      throws SQLException {
    Collection childrenDetails = getChildrenDetails(con, nodePK);
    if (childrenDetails.size() > 0) {
      Iterator iterator = childrenDetails.iterator();
      NodeDetail child = null;
      while (iterator.hasNext()) {
        child = (NodeDetail) iterator.next();
        if (status != null && status.length() > 0) {
          if (status.equals(child.getStatus())) {
            tree.add(child);
            tree = getSubTree(con, tree, child.getNodePK(), status);
          }
        } else {
          tree.add(child);
          tree = getSubTree(con, tree, child.getNodePK(), status);
        }
      }
    }
    return tree;
  }

  /**
   * Get the path from root to a node
   * 
   * @return A collection of NodeDetail
   * @param con
   *          A connection to the database
   * @param nodePK
   *          A NodePK
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static Collection getAnotherPath(Connection con, NodePK nodePK) throws SQLException {
    ArrayList nodeDetails = new ArrayList();

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
    return (nodeDetails);
  }

  /**
   * ********************* Database Routines ***********************
   */

  /**
   * Create a NodeDetail from a ResultSet
   * 
   * @param rs
   *          the ResultSet which contains data
   * @return the NodeDetail
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static NodeDetail resultSet2NodeDetail(ResultSet rs, NodePK nodePK) throws SQLException {
    /* Récupération des données depuis la BD */
    NodePK pk = new NodePK(new Integer(rs.getInt(1)).toString(), nodePK);
    String name = rs.getString(2);
    String description = rs.getString(3);
    String creationDate = rs.getString(4);
    String creatorId = rs.getString(5);
    String path = rs.getString(6);
    int level = rs.getInt(7);
    NodePK fatherPK = new NodePK(new Integer(rs.getInt(8)).toString(), nodePK);
    String modelId = rs.getString(9);
    String status = rs.getString(10);
    String type = rs.getString(12);
    int order = rs.getInt(13);
    String language = rs.getString(14);
    int rightsDependsOn = rs.getInt(15);

    if (description == null)
      description = "";

    NodeDetail nd = new NodeDetail(pk, name, description, creationDate, creatorId, path, level,
        fatherPK, modelId, status, null, type);

    nd.setLanguage(language);
    nd.setOrder(order);
    nd.setRightsDependsOn(rightsDependsOn);
    return (nd);
  }

  /**
   * Get the detail of another Node
   * 
   * @param pk
   *          the PK of the Node
   * @return a NodeDetail
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static NodeDetail getAnotherHeader(Connection con, NodePK nodePK) throws SQLException {

    String nodeId = nodePK.getId();
    NodeDetail nd = null;

    StringBuffer nodeStatement = new StringBuffer();
    nodeStatement.append("select * from ").append(nodePK.getTableName());
    nodeStatement.append(" where nodeId = ").append(nodeId);
    nodeStatement.append(" and instanceId = '").append(nodePK.getComponentName()).append("'");
    /*
     * nodeStatement.append("select * from ").append(nodePK.getTableName()).append (" n1 ");
     * nodeStatement.append("LEFT OUTER JOIN ").append(NodeI18NDAO.TABLE_NAME ).append(" n2");
     * nodeStatement.append(" on (n1.nodeId = n2.nodeId)"); nodeStatement
     * .append(" where instanceId ='").append(nodePK.getComponentName ()).append("'");
     * nodeStatement.append(" and n1.nodeId = ").append(nodeId);
     * nodeStatement.append(" order by nodePath, orderNumber, n2.id");
     */

    Statement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(nodeStatement.toString());
      if (rs.next()) {
        nd = resultSet2NodeDetail(rs, nodePK);
        // Add default translation
        NodeI18NDetail nodeI18NDetail = new NodeI18NDetail(nd.getLanguage(), nd.getName(), nd
            .getDescription());
        nd.addTranslation((Translation) nodeI18NDetail);

        List translations = NodeI18NDAO.getTranslations(con, nd.getId());

        for (int t = 0; translations != null && t < translations.size(); t++) {
          nodeI18NDetail = (NodeI18NDetail) translations.get(t);
          nd.addTranslation((Translation) nodeI18NDetail);
        }
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
   * 
   * @return a NodeDetail collection
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static Collection getChildrenDetails(Connection con, NodePK nodePK) throws SQLException {
    return getChildrenDetails(con, nodePK, null);
  }

  public static Collection getChildrenDetails(Connection con, NodePK nodePK, String sorting)
      throws SQLException {

    String nodeId = nodePK.getId();
    ArrayList a = null;
    StringBuffer childrenStatement = new StringBuffer();
    childrenStatement.append("select * from ").append(nodePK.getTableName());
    childrenStatement.append(" where nodeFatherId = ? ");
    childrenStatement.append(" and instanceId = ? ");
    if (sorting != null) {
      childrenStatement.append(" order by ").append(sorting);
    } else {
      childrenStatement.append(" order by orderNumber asc");
    }

    /*
     * childrenStatement.append("select * from ").append(nodePK.getTableName()).append (" n1 ");
     * childrenStatement.append("LEFT OUTER JOIN ").append(NodeI18NDAO. TABLE_NAME).append(" n2");
     * childrenStatement.append(" on (n1.nodeId = n2.nodeId)");
     * childrenStatement.append(" where nodeFatherId = ? ");
     * childrenStatement.append(" and instanceId = ? "); if (sorting != null) {
     * childrenStatement.append(" order by ").append(sorting); } else {
     * childrenStatement.append(" order by orderNumber, n2.id asc"); }
     */

    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    try {
      prepStmt = con.prepareStatement(childrenStatement.toString());
      prepStmt.setInt(1, new Integer(nodeId).intValue());
      prepStmt.setString(2, nodePK.getComponentName());
      rs = prepStmt.executeQuery();
      a = new ArrayList();
      while (rs.next()) {
        NodeDetail nd = resultSet2NodeDetail(rs, nodePK);

        setTranslations(con, nd);

        a.add(nd);
      }
    } catch (SQLException e) {
      SilverTrace.error("node", "NodeDAO.getChildrenDetails()", "root.EX_SQL_QUERY_FAILED",
          "childrenStatement = " + childrenStatement.toString() + " nodeId = " + nodeId
              + " compo name = " + nodePK.getComponentName(), e);
      throw e;
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return a;
  }

  /**
   * Get the children number of this node
   * 
   * @return a int
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static int getChildrenNumber(Connection con, NodePK nodePK) throws SQLException {

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
      SilverTrace.error("node", "NodeDAO.getChildrenNumber()", "root.EX_SQL_QUERY_FAILED",
          "selectQuery = " + selectQuery.toString() + " nodeId = " + nodePK.getId()
              + " compo name = " + nodePK.getComponentName(), e);
      throw e;
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return nbChildren;
  }

  /**
   * Insert into the database the data of a node
   * 
   * @return a NodePK which contains the new row id
   * @param nd
   *          the NodeDetail which contains data
   * @param creatorId
   *          the id of the user who have create this node
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static NodePK insertRow(Connection con, NodeDetail nd) throws SQLException {
    NodePK pk = nd.getNodePK();

    int newId = 0;

    String name = nd.getName();
    String description = nd.getDescription();
    String creationDate = nd.getCreationDate();
    String creatorId = nd.getCreatorId();
    String path = nd.getPath();
    int level = nd.getLevel();
    int fatherId = new Integer(nd.getFatherPK().getId()).intValue();
    String modelId = nd.getModelId();
    String status = nd.getStatus();
    String type = nd.getType();
    String language = nd.getLanguage();

    int nbBrothers = getChildrenNumber(con, nd.getFatherPK());
    SilverTrace.info("node", "NodeDAO.insertRow()", "root.MSG_GEN_PARAM_VALUE", "fatherId = "
        + nd.getFatherPK().getId() + ", nbBrothers = " + nbBrothers);

    int order = nbBrothers + 1;

    try {
      if (nd.isUseId()) {
        newId = Integer.parseInt(nd.getNodePK().getId());
      } else {
        /* Recherche de la nouvelle PK de la table */
        newId = DBUtil.getNextId(nd.getNodePK().getTableName(), new String("nodeId"));
      }
    } catch (Exception e) {
      throw new NodeRuntimeException("NodeDAO.insertRow()", SilverpeasRuntimeException.ERROR,
          "root.EX_GET_NEXTID_FAILED", e);
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
   * Delete into the database a node but not it's descendants
   * 
   * @param path
   *          the path of the node to delete
   * @param creatorId
   *          the id of the user who have create this node
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static void deleteRow(Connection con, NodePK nodePK) throws SQLException {

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
   * Check if a Node exists in database
   * 
   * @return the fat pk (pk + detail)
   * @param pk
   *          the node PK to find
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static NodePK selectByPrimaryKey(Connection con, NodePK pk) throws SQLException {
    NodeDetail detail = loadRow(con, pk);
    NodePK primary = new NodePK(pk.getId(), pk);

    primary.nodeDetail = detail;
    return primary;
  }

  public static NodePK selectByNameAndFatherId(Connection con, NodePK pk, String name,
      int nodeFatherId) throws SQLException {
    NodeDetail detail = loadRow(con, pk, name, nodeFatherId);
    NodePK primary = new NodePK(detail.getNodePK().getId(), detail.getNodePK().getInstanceId());

    primary.nodeDetail = detail;
    return primary;
  }

  /**
   * Method declaration
   * 
   * @param con
   * @param pk
   * @return
   * @throws SQLException
   * @see
   */
  public static Collection selectByFatherPrimaryKey(Connection con, NodePK pk) throws SQLException {
    Collection children = getChildrenDetails(con, pk);
    List result = new ArrayList();
    NodeDetail detail = null;
    NodePK primary = null;

    for (Iterator i = children.iterator(); i.hasNext();) {
      detail = (NodeDetail) i.next();
      primary = detail.getNodePK();
      primary.nodeDetail = detail;
      result.add(primary);
    }
    return result;
  }

  public static NodeDetail loadRow(Connection con, NodePK nodePK) throws SQLException {
    return loadRow(con, nodePK, true);
  }

  /**
   * Load node attributes from database
   * 
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static NodeDetail loadRow(Connection con, NodePK nodePK, boolean getTranslations)
      throws SQLException {
    SilverTrace.info("node", "NodeDAO.loadRow()", "root.MSG_GEN_PARAM_VALUE", "nodePK = " + nodePK);
    NodeDetail detail = null;
    StringBuffer selectQuery = new StringBuffer();
    selectQuery.append("select * from ").append(nodePK.getTableName());
    selectQuery.append(" where nodeId=?");
    selectQuery.append(" and instanceId=?");
    SilverTrace.info("node", "NodeDAO.loadRow()", "root.MSG_GEN_PARAM_VALUE", "selectQuery = "
        + selectQuery.toString());

    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.prepareStatement(selectQuery.toString());
      stmt.setString(1, nodePK.getId());
      stmt.setString(2, nodePK.getComponentName());
      rs = stmt.executeQuery();
      if (rs.next()) {
        detail = resultSet2NodeDetail(rs, nodePK);

        if (getTranslations)
          setTranslations(con, detail);
      } else {
        // throw new NoSuchEntityException("Row for id " + nodePK.getId() +
        // " not found in database.");
        throw new NodeRuntimeException("NodeDAO.loadRow()", SilverpeasRuntimeException.ERROR,
            "root.EX_CANT_LOAD_ENTITY_ATTRIBUTES", "NodeId = " + nodePK.getId());
      }
    } catch (SQLException e) {
      SilverTrace.error("node", "NodeDAO.loadRow()", "root.EX_SQL_QUERY_FAILED", "selectQuery = "
          + selectQuery.toString(), e);
      throw e;
    } finally {
      DBUtil.close(rs, stmt);
    }

    return detail;
  }

  public static NodeDetail loadRow(Connection con, NodePK nodePK, String name, int nodeFatherId)
      throws SQLException {
    SilverTrace.info("node", "NodeDAO.loadRow()", "root.MSG_GEN_PARAM_VALUE", "nodePK = " + nodePK);
    NodeDetail detail = null;
    StringBuffer selectQuery = new StringBuffer();
    selectQuery.append("select * from ").append(nodePK.getTableName());
    selectQuery.append(" where lower(nodename)=?");
    selectQuery.append(" and instanceId=?");
    selectQuery.append(" and nodefatherid=?");
    SilverTrace.info("node", "NodeDAO.loadRow()", "root.MSG_GEN_PARAM_VALUE", "selectQuery = "
        + selectQuery.toString());

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
      SilverTrace.error("node", "NodeDAO.loadRow()", "root.EX_SQL_QUERY_FAILED", "selectQuery = "
          + selectQuery.toString(), e);
      throw e;
    } finally {
      DBUtil.close(rs, stmt);
    }

    return detail;
  }

  /**
   * Store node attributes into database
   * 
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static void storeRow(Connection con, NodeDetail nodeDetail) throws SQLException {
    int rowCount = 0;
    StringBuffer updateStatement = new StringBuffer();
    updateStatement.append("update ").append(nodeDetail.getNodePK().getTableName());
    updateStatement.append(" set nodeName =  ? , nodeDescription = ? , nodePath = ? , ");
    updateStatement
        .append(" nodeLevelNumber = ? , nodeFatherId = ? , modelId = ? , nodeStatus = ? , orderNumber = ?, lang = ?, rightsDependsOn = ? ");
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
      throw new NodeRuntimeException("NodeDAO.storeRow()", SilverpeasRuntimeException.ERROR,
          "root.EX_CANT_STORE_ENTITY_ATTRIBUTES", "NodeId = " + nodeDetail.getNodePK().getId());
    }
  }

  public static void moveNode(Connection con, NodeDetail nodeDetail) throws SQLException {
    int rowCount = 0;
    StringBuffer updateStatement = new StringBuffer();
    updateStatement.append("update ").append(nodeDetail.getNodePK().getTableName());
    updateStatement.append(" set nodePath = ? , ");
    updateStatement
        .append(" nodeLevelNumber = ? , nodeFatherId = ? , instanceId = ? , orderNumber = ?, rightsDependsOn = ? ");
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
    SilverTrace.info("node", "NodeDAO.updateRightsDependency()", "root.MSG_GEN_ENTER_METHOD",
        "nodePK = " + pk.toString() + ", rightsDependsOn = " + rightsDependsOn);

    StringBuffer updateStatement = new StringBuffer();
    updateStatement.append("update ").append(pk.getTableName());
    updateStatement.append(" set rightsDependsOn =  ? ");
    updateStatement.append(" where nodeId = ? and instanceId = ?");
    // updateStatement.append(" where rightsDependsOn = ? and instanceId = ?");
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(updateStatement.toString());
      prepStmt.setInt(1, rightsDependsOn);
      prepStmt.setInt(2, Integer.parseInt(pk.getId()));
      // prepStmt.setInt(2, Integer.parseInt(pk.getId()));
      prepStmt.setString(3, pk.getInstanceId());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }
}