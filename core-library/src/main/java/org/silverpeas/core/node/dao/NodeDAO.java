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
package org.silverpeas.core.node.dao;

import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodeI18NDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.model.NodePath;
import org.silverpeas.core.node.model.NodeRuntimeException;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.ejb.NoSuchEntityException;
import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is the Node Data Access Object.
 * @author Nicolas Eysseric
 */
@Singleton
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
  private final Map<String, List<NodeDetail>> allTrees = new ConcurrentHashMap<>();
  private static final String SELECT_QUERY = "selectQuery = ";
  private static final String COMPO_NAME = " compo name = ";
  private static final String ID_EQUALS = " id = ";
  private static final String NODE_ID_AND_INSTANCE_ID_CLAUSE =
      " where nodeId = ? and instanceId = ?";
  private static final String SELECT_FROM = "select * from ";
  private static final String NODE_STATEMENT = "nodeStatement = ";
  private static final String NODE_ID = "NodeId = ";
  private static final String UPDATE = "update ";

  private NodeDAO() {

  }

  /**
   * Deletes all nodes linked to the component instance represented by the given identifier.
   * @param componentInstanceId the identifier of the component instance for which the resources
   * must be deleted.
   * @throws SQLException
   */
  public void deleteComponentInstanceData(String componentInstanceId) throws SQLException {
    JdbcSqlQuery.createDeleteFor("sb_node_node").where("instanceId = ?", componentInstanceId)
        .execute();
  }

  public List<NodeDetail> getTree(Connection con, NodePK nodePK) throws SQLException {
    List<NodeDetail> tree = allTrees.get(nodePK.getComponentName());
    if (tree == null) {
      tree = getAllHeaders(con, nodePK);
      allTrees.put(nodePK.getComponentName(), tree);
    }
    return tree;
  }

  public void unvalidateTree(Connection con, NodePK nodePK) {
    Objects.requireNonNull(con);
    allTrees.remove(nodePK.getComponentName());
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
  public boolean isSameNameSameLevelOnCreation(Connection con, NodeDetail nd)
      throws SQLException {
    final int nbItems;
    try (final PreparedStatement prepStmt = con.prepareStatement(COUNT_NODES_PER_LEVEL)) {
      prepStmt.setInt(1, nd.getLevel());
      prepStmt.setString(2, nd.getName());
      prepStmt.setString(3, nd.getNodePK().getComponentName());
      try (final ResultSet rs = prepStmt.executeQuery()) {
        if (rs.next()) {
          nbItems = rs.getInt("nb");
        } else {
          nbItems = 0;
        }
      }
    } catch (SQLException e) {
      SilverLogger.getLogger(this)
          .error(SELECT_QUERY + COUNT_NODES_PER_LEVEL + " level = " + nd.getLevel() + " name = " +
              nd.getName() + COMPO_NAME + nd.getNodePK().getComponentName(), e);
      throw e;
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
  public boolean isSameNameSameLevelOnUpdate(Connection con, NodeDetail nd)
      throws SQLException {
    int nbItems;
    try (final PreparedStatement prepStmt = con.prepareStatement(
        COUNT_NODES_PER_LEVEL_WITHOUT_CURRENT)) {
      prepStmt.setInt(1, Integer.parseInt(nd.getNodePK().getId()));
      prepStmt.setInt(2, nd.getLevel());
      prepStmt.setString(3, nd.getName());
      prepStmt.setString(4, nd.getNodePK().getComponentName());
      try (final ResultSet rs = prepStmt.executeQuery()) {
        if (rs.next()) {
          nbItems = rs.getInt(1);
        } else {
          nbItems = 0;
        }
      }
    } catch (SQLException e) {
      SilverLogger.getLogger(this)
          .error(SELECT_QUERY + COUNT_NODES_PER_LEVEL_WITHOUT_CURRENT + ID_EQUALS +
              nd.getNodePK().getId() + " level = " + nd.getLevel() + " name = " + nd.getName() +
              COMPO_NAME + nd.getNodePK().getComponentName(), e);
      throw e;
    }
    return nbItems != 0;
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
  public Collection<NodePK> getChildrenPKs(Connection con, NodePK nodePK)
      throws SQLException {
    try (final PreparedStatement prepStmt = con.prepareStatement(SELECT_CHILDREN_IDS)) {
      prepStmt.setInt(1, Integer.parseInt(nodePK.getId()));
      prepStmt.setString(2, nodePK.getComponentName());
      try (final ResultSet rs = prepStmt.executeQuery()) {
        final List<NodePK> pks = new ArrayList<>();
        fetchSubNodes(rs, nodePK, pks);
        return pks;
      }
    } catch (SQLException e) {
      SilverLogger.getLogger(this)
          .error("childrenStatement = " + SELECT_CHILDREN_IDS + ID_EQUALS + nodePK.getId() +
              COMPO_NAME + nodePK.getComponentName(), e);
      throw e;
    }
  }

  private void fetchSubNodes(final ResultSet rs, final NodePK nodePK, final List<NodePK> pks)
      throws SQLException {
    while (rs.next()) {
      final String nodeId = String.valueOf(rs.getInt("nodeid"));
      final NodePK n = new NodePK(nodeId, nodePK);
      pks.add(n); /* Stockage du sous thème */
    }
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
  public Collection<NodePK> getDescendantPKs(Connection con, NodePK nodePK) throws SQLException {
    String path = getNodePath(con, nodePK, SELECT_DESCENDANTS_PK);
    final List<NodePK> nodePKS = new ArrayList<>();
    if (path != null) {
      path += nodePK.getId() + "/%";
      try (final PreparedStatement prepStmt = con.prepareStatement(SELECT_DESCENDANTS_ID_BY_PATH)) {
        prepStmt.setString(1, path);
        prepStmt.setString(2, nodePK.getComponentName());
        try (final ResultSet rs = prepStmt.executeQuery()) {
          fetchSubNodes(rs, nodePK, nodePKS);
        }
      } catch (SQLException e) {
        SilverLogger.getLogger(this)
            .error(SELECT_QUERY + SELECT_DESCENDANTS_ID_BY_PATH + COMPO_NAME +
                nodePK.getComponentName(), e);
        throw e;
      }
    }
    return nodePKS;
  }

  private String getNodePath(final Connection con, final NodePK nodePK, final String selectQuery)
      throws SQLException {
    String path = null;
    try (final PreparedStatement prepStmt = con.prepareStatement(selectQuery)) {
      prepStmt.setInt(1, Integer.parseInt(nodePK.getId()));
      prepStmt.setString(2, nodePK.getComponentName());
      try (final ResultSet rs = prepStmt.executeQuery()) {
        if (rs.next()) {
          path = rs.getString(1);
        }
      }
    } catch (SQLException e) {
      SilverLogger.getLogger(this)
          .error(SELECT_QUERY + selectQuery + ID_EQUALS + nodePK.getId() + COMPO_NAME +
              nodePK.getComponentName(), e);
      throw e;
    }
    return path;
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
  public List<NodeDetail> getDescendantDetails(Connection con, NodePK nodePK)
      throws SQLException {
    final String selectNodePath =
        "SELECT nodePath from " + nodePK.getTableName() + NODE_ID_AND_INSTANCE_ID_CLAUSE;
    String path = getNodePath(con, nodePK, selectNodePath);
    final List<NodeDetail> nodeDetails = new ArrayList<>();
    if (path != null) {
      path = path + nodePK.getId() + "/%";
      final String selectNodes =
          SELECT_FROM + nodePK.getTableName() + " where nodePath like '" + path +
              "' and instanceId = ? order by nodePath";
      try (final PreparedStatement prepStmt = con.prepareStatement(selectNodes)) {
        prepStmt.setString(1, nodePK.getComponentName());
        try (final ResultSet rs = prepStmt.executeQuery()) {
          while (rs.next()) {
            NodeDetail nd = resultSet2NodeDetail(rs, nodePK);
            nodeDetails.add(nd);
          }
        }
      } catch (SQLException e) {
        SilverLogger.getLogger(this).error(SELECT_QUERY + selectNodes + COMPO_NAME +
                nodePK.getComponentName(), e);
        throw e;
      }
    }
    return nodeDetails;
  }

  /**
   * Get descendant nodeDetails of a node
   * @param con A connection to the database
   * @param node A NodeDetail
   * @return A List of NodeDetail
   * @throws SQLException
   * @since 4.07
   */
  public List<NodeDetail> getDescendantDetails(Connection con, NodeDetail node)
      throws SQLException {
    final String path = node.getPath() + node.getNodePK().getId() + "/%";
    final String selectQuery =
        SELECT_FROM + node.getNodePK().getTableName() + " where nodePath like '" + path +
            "' and instanceId = ? order by nodePath";
    final List<NodeDetail> nodeDetails = new ArrayList<>();
    try (final PreparedStatement prepStmt = con.prepareStatement(selectQuery)) {
      prepStmt.setString(1, node.getNodePK().getComponentName());
      try (final ResultSet rs = prepStmt.executeQuery()) {
        while (rs.next()) {
          final NodeDetail nd = resultSet2NodeDetail(rs, node.getNodePK());
          nodeDetails.add(nd);
        }
      }
    } catch (SQLException e) {
      SilverLogger.getLogger(this).error(SELECT_QUERY + selectQuery + COMPO_NAME +
              node.getNodePK().getComponentName(), e);
      throw e;
    }
    return nodeDetails;
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
  public List<NodeDetail> getHeadersByLevel(Connection con, NodePK nodePK, int level)
      throws SQLException {
    final String selectQuery =
        SELECT_FROM + nodePK.getTableName() + " where nodeLevelNumber=" + level +
            " and instanceId='" + nodePK.getComponentName() +
            "' order by ordernumber asc, nodeId asc";
    return findSubNodeDetails(con, selectQuery, nodePK);
  }

  private List<NodeDetail> findSubNodeDetails(final Connection con, final String selectQuery,
      final NodePK nodePK) throws SQLException {
    final List<NodeDetail> details = new ArrayList<>();
    try (final Statement stmt = con.createStatement()) {
      try (final ResultSet rs = stmt.executeQuery(selectQuery)) {
        while (rs.next()) {
          final NodeDetail nd = resultSet2NodeDetail(rs, nodePK);
          setTranslations(con, nd);
          details.add(nd);
        }
      }
    } catch (SQLException e) {
      SilverLogger.getLogger(this).error(NODE_STATEMENT + selectQuery, e);
      throw e;
    }
    return details;
  }

  private void setTranslations(Connection con, NodeDetail node) throws SQLException {
    // Add default translation
    final NodeI18NDetail nodeI18NDetail =
        new NodeI18NDetail(node.getLanguage(), node.getName(), node.
        getDescription());
    node.addTranslation(nodeI18NDetail);
    if (I18NHelper.isI18nContentActivated) {
      List<NodeI18NDetail> translations = NodeI18NDAO.getTranslations(con, node.getId());
      for (int t = 0; translations != null && t < translations.size(); t++) {
        final NodeI18NDetail anotherNodeI18NDetail = translations.get(t);
        node.addTranslation(anotherNodeI18NDetail);
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
  public List<NodeDetail> getAllHeaders(Connection con, NodePK nodePK) throws SQLException {
    return getAllHeaders(con, nodePK, null, 0);
  }

  public List<NodeDetail> getAllHeaders(Connection con, NodePK nodePK, String sorting)
      throws SQLException {
    return getAllHeaders(con, nodePK, sorting, 0);
  }

  public List<NodeDetail> getAllHeaders(Connection con, NodePK nodePK, int level)
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
  public List<NodeDetail> getAllHeaders(Connection con, NodePK nodePK, String sorting,
      int level) throws SQLException {
    final StringBuilder selectQuery = new StringBuilder();
    selectQuery.append(SELECT_FROM).append(nodePK.getTableName());
    selectQuery.append(" where instanceId ='").append(nodePK.getComponentName()).append("'");
    if (level > 0) {
      selectQuery.append(" and nodeLevelNumber = ").append(level);
    }
    if (StringUtil.isDefined(sorting)) {
      selectQuery.append(" order by ").append(sorting);
    } else {
      selectQuery.append(" order by nodePath, orderNumber");
    }

    return findSubNodeDetails(con, selectQuery.toString(), nodePK);
  }

  public List<NodeDetail> getSubTree(Connection con, NodePK nodePK, String status)
      throws SQLException {
    // get the path of the given nodePK
    final NodeDetail detail = loadRow(con, nodePK);
    final List<NodeDetail> headers = new ArrayList<>();
    if (StringUtil.isDefined(status)) {
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

  private void getSubTree(Connection con, List<NodeDetail> tree, NodePK nodePK,
      String status) throws SQLException {
    final Collection<NodeDetail> childrenDetails = getChildrenDetails(con, nodePK);
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
   * @return A {@link NodePath} instance.
   * @throws java.sql.SQLException
   * @see NodePK
   * @see NodeDetail
   * @since 1.0
   */
  public NodePath getNodePath(Connection con, NodePK nodePK)
      throws SQLException {
    final NodePath nodePath = new NodePath();
    /* le node courant */
    NodeDetail nd = getAnotherHeader(con, nodePK);
    nodePath.add(nd);
    for (int i = nd.getLevel() - 1; i >= 1; i--) {
      nd = getAnotherHeader(con, nd.getFatherPK());
      nodePath.add(nd);
    }
    return nodePath;
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
  public NodeDetail resultSet2NodeDetail(ResultSet rs, NodePK nodePK) throws SQLException {
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
        new NodeDetail(pk, name, description, level, fatherPK.getId());
    nd.setCreationDate(creationDate);
    nd.setCreatorId(creatorId);
    nd.setPath(path);
    nd.setModelId(modelId);
    nd.setStatus(status);
    nd.setType(type);
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
  public NodeDetail getAnotherHeader(Connection con, NodePK nodePK) throws SQLException {
    final String nodeId = nodePK.getId();
    final String selectQuery =
        SELECT_FROM + nodePK.getTableName() + " where nodeId = " + nodeId + " and instanceId = '" +
            nodePK.getComponentName() + "'";
    try (final Statement stmt = con.createStatement()) {
      try (final ResultSet rs = stmt.executeQuery(selectQuery)) {
        if (rs.next()) {
          final NodeDetail nd = resultSet2NodeDetail(rs, nodePK);
          setTranslations(con, nd);
          return nd;
        } else {
          throw new NoSuchEntityException("Row for id " + nodeId + " not found in database.");
        }
      }
    } catch (SQLException e) {
      SilverLogger.getLogger(this).error(NODE_STATEMENT + selectQuery, e);
      throw e;
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
  public Collection<NodeDetail> getChildrenDetails(Connection con, NodePK nodePK)
      throws SQLException {
    String nodeId = nodePK.getId();
    final String selectQuery = SELECT_FROM + nodePK.getTableName() +
        " where nodeFatherId = ? and instanceId = ? order by orderNumber asc";
    try (final PreparedStatement prepStmt = con.prepareStatement(selectQuery)) {
      prepStmt.setInt(1, Integer.parseInt(nodeId));
      prepStmt.setString(2, nodePK.getComponentName());
      try (final ResultSet rs = prepStmt.executeQuery()) {
        final List<NodeDetail> nodeDetails = new ArrayList<>();
        while (rs.next()) {
          final NodeDetail nd = resultSet2NodeDetail(rs, nodePK);
          setTranslations(con, nd);
          nodeDetails.add(nd);
        }
        return nodeDetails;
      }
    } catch (SQLException e) {
      SilverLogger.getLogger(this)
          .error("childrenStatement = " + selectQuery + " nodeId = " + nodeId +
              COMPO_NAME + nodePK.getComponentName(), e);
      throw e;
    }
  }

  /**
   * Get the children number of this node
   * @param con A connection to the database
   * @param nodePK
   * @return a int
   * @throws java.sql.SQLException
   * @since 1.0
   */
  public int getChildrenNumber(Connection con, NodePK nodePK) throws SQLException {
    final String selectQuery = "select count(*) from " + nodePK.getTableName() +
        " where nodeFatherId = ? and instanceId = ?";
    try (final PreparedStatement prepStmt = con.prepareStatement(selectQuery)) {
      prepStmt.setInt(1, Integer.parseInt(nodePK.getId()));
      prepStmt.setString(2, nodePK.getComponentName());
      try (final ResultSet rs = prepStmt.executeQuery()) {
        final int nbChildren;
        if (rs.next()) {
          nbChildren = rs.getInt(1);
        } else {
          nbChildren = 0;
        }
        return nbChildren;
      }
    } catch (SQLException e) {
      SilverLogger.getLogger(this)
          .error(SELECT_QUERY + selectQuery + " nodeId = " + nodePK.getId() + COMPO_NAME +
                  nodePK.getComponentName(), e);
      throw e;
    }
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
  public NodePK insertRow(Connection con, NodeDetail nd) throws SQLException {
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

    nd.setOrder(nbBrothers + 1);

    try {
      if (nd.isUseId()) {
        newId = Integer.parseInt(nd.getNodePK().getId());
      } else {
        /* Recherche de la nouvelle PK de la table */
        newId = DBUtil.getNextId(nd.getNodePK().getTableName(), "nodeId");
      }
    } catch (Exception e) {
      throw new NodeRuntimeException(e);
    }

    final String insertQuery = "insert into " + nd.getNodePK().getTableName() +
        " values ( ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ?, ? , ?, ? , ?)";
    try (final PreparedStatement prepStmt = con.prepareStatement(insertQuery)) {
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
      prepStmt.setInt(13, nd.getOrder());
      prepStmt.setString(14, language);
      prepStmt.setInt(15, nd.getRightsDependsOn());
      prepStmt.executeUpdate();
      pk.setId(String.valueOf(newId));
      unvalidateTree(con, nd.getNodePK());
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
  public void deleteRow(Connection con, NodePK nodePK) throws SQLException {
    final String deleteQuery =
        "delete from " + nodePK.getTableName() + " where nodeId=" + nodePK.getId() +
            " and instanceId='" + nodePK.getComponentName() + "'";
    try (final Statement stmt = con.createStatement()) {
      stmt.executeUpdate(deleteQuery);
      unvalidateTree(con, nodePK);
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
  public NodeDetail selectByPrimaryKey(Connection con, NodePK pk) throws SQLException {

    try {
      return loadRow(con, pk);
    } catch (NodeRuntimeException e) {
      /*
       * NodeRuntimeException thrown by loadRow() should be replaced by returning null (not found)
       */
      return null;
    }
  }

  public NodeDetail selectByNameAndFatherId(Connection con, NodePK pk, String name,
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

  public NodeDetail loadRow(Connection con, NodePK nodePK) throws SQLException {
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
  public NodeDetail loadRow(Connection con, NodePK nodePK, boolean getTranslations)
      throws SQLException {
    try (final PreparedStatement stmt = con.prepareStatement(SELECT_NODE_BY_ID)) {
      stmt.setInt(1, Integer.parseInt(nodePK.getId()));
      stmt.setString(2, nodePK.getComponentName());
      try (final ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          final NodeDetail detail = resultSet2NodeDetail(rs, nodePK);
          if (getTranslations) {
            setTranslations(con, detail);
          }
          return detail;
        } else {
          throw new NodeRuntimeException("Cannot load node " + NODE_ID + nodePK.getId());
        }
      }
    } catch (SQLException e) {
      SilverLogger.getLogger(this).error(SELECT_QUERY + SELECT_NODE_BY_ID, e);
      throw e;
    }
  }

  public NodeDetail loadRow(Connection con, NodePK nodePK, String name, int nodeFatherId)
      throws SQLException {
    final String selectQuery = SELECT_FROM + nodePK.getTableName() +
        " where lower(nodename)=? and instanceId=? and nodefatherid=?";
    try (final PreparedStatement stmt = con.prepareStatement(selectQuery)) {
      stmt.setString(1, name.toLowerCase());
      stmt.setString(2, nodePK.getComponentName());
      stmt.setInt(3, nodeFatherId);
      try (final ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          final NodeDetail detail = resultSet2NodeDetail(rs, nodePK);
          setTranslations(con, detail);
          return detail;
        } else {
          throw new NodeRuntimeException("Cannot load node " + NODE_ID + nodePK.getId());
        }
      }
    } catch (SQLException e) {
      SilverLogger.getLogger(this).error(SELECT_QUERY + selectQuery, e);
      throw e;
    }
  }

  /**
   * Store node attributes into database
   * @param con a connection to the database
   * @param nodeDetail
   * @throws java.sql.SQLException
   * @since 1.0
   */
  public void storeRow(Connection con, NodeDetail nodeDetail) throws SQLException {
    final int rowCount;
    final String updateQuery = UPDATE + nodeDetail.getNodePK().getTableName() +
        " set nodeName =  ? , nodeDescription = ? , nodePath = ? , nodeLevelNumber = ? , " +
        "nodeFatherId = ? , modelId = ? , nodeStatus = ? , orderNumber = ?, lang = ?, " +
        "rightsDependsOn = ? " + NODE_ID_AND_INSTANCE_ID_CLAUSE;
    try (final PreparedStatement prepStmt = con.prepareStatement(updateQuery)) {
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
    }

    if (rowCount == 0) {
      throw new NodeRuntimeException(
          "Cannot store node " + NODE_ID + nodeDetail.getNodePK().getId());
    }
  }

  public void moveNode(Connection con, NodeDetail nodeDetail) throws SQLException {
    final int rowCount;
    final String updateQuery = UPDATE + nodeDetail.getNodePK().getTableName() +
        " set nodePath = ? , nodeLevelNumber = ? , nodeFatherId = ? , instanceId = ? , " +
        "orderNumber = ?, rightsDependsOn = ? where nodeId = ? ";
    try (final PreparedStatement prepStmt = con.prepareStatement(updateQuery)) {
      prepStmt.setString(1, nodeDetail.getPath());
      prepStmt.setInt(2, nodeDetail.getLevel());
      prepStmt.setInt(3, Integer.parseInt(nodeDetail.getFatherPK().getId()));
      prepStmt.setString(4, nodeDetail.getNodePK().getInstanceId());
      prepStmt.setInt(5, nodeDetail.getOrder());
      prepStmt.setInt(6, nodeDetail.getRightsDependsOn());
      prepStmt.setInt(7, Integer.parseInt(nodeDetail.getNodePK().getId()));
      rowCount = prepStmt.executeUpdate();
    }
    if (rowCount == 0) {
      throw new NodeRuntimeException(
          "Cannot store node " + NODE_ID + nodeDetail.getNodePK().getId());
    }
  }

  public void updateRightsDependency(Connection con, NodePK pk, int rightsDependsOn)
      throws SQLException {
    final String updateStatement =
        UPDATE + pk.getTableName() + " set rightsDependsOn =  ? " + NODE_ID_AND_INSTANCE_ID_CLAUSE;
    try (final PreparedStatement prepStmt = con.prepareStatement(updateStatement)) {
      prepStmt.setInt(1, rightsDependsOn);
      prepStmt.setInt(2, Integer.parseInt(pk.getId()));
      prepStmt.setString(3, pk.getInstanceId());
      prepStmt.executeUpdate();
    }
  }

  public void sortNodes(Connection con, List<NodePK> nodePKs) throws SQLException {
    final String query = "UPDATE SB_Node_Node SET orderNumber = ? WHERE nodeId = ? ";
    try (final PreparedStatement prepStmt = con.prepareStatement(query)) {
      int i = 0;
      for (NodePK nodePK : nodePKs) {
        prepStmt.setInt(1, i);
        prepStmt.setInt(2, Integer.parseInt(nodePK.getId()));
        prepStmt.executeUpdate();
        i++;
      }
    }
  }
}
