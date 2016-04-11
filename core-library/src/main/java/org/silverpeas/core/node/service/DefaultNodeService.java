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
package org.silverpeas.core.node.service;

import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.AdministrationServiceProvider;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.node.dao.NodeDAO;
import org.silverpeas.core.node.dao.NodeI18NDAO;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodeI18NDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.model.NodeRuntimeException;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.exception.UtilException;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This is the default implementation of NodeService. A node is composed by some another nodes
 * (children) and have got one and only one father. It describes a tree.
 *
 * @author Nicolas Eysseric
 */
@Singleton
@Transactional(Transactional.TxType.SUPPORTS)
public class DefaultNodeService implements NodeService, ComponentInstanceDeletion {

  /**
   * Database name where is stored nodes
   */
  private static final SettingBundle nodeSettings =
      ResourceLocator.getSettingBundle("org.silverpeas.node.nodeSettings");

  @Override
  @Transactional
  public void delete(final String componentInstanceId) {
    try {
      NodeI18NDAO.deleteComponentInstanceData(componentInstanceId);
      NodeDAO.deleteComponentInstanceData(componentInstanceId);
    } catch (SQLException e) {
      throw new NodeRuntimeException("DefaultNodeService.delete()",
          SilverpeasRuntimeException.ERROR, "node.DELETING_COMPONENT_INSTANCE_PUBLICATIONS_FAILED",
          "instanceId = " + componentInstanceId, e);
    }
  }

  /**
   * Method declaration
   *
   * @param pk
   * @return
   * @see
   */
  private NodeDetail findNode(NodePK pk) {
    Connection con = getConnection();
    try {
      NodeDetail nodeDetail = NodeDAO.selectByPrimaryKey(con, pk);
      if (nodeDetail != null) {
        return nodeDetail;
      } else {
        throw new NodeRuntimeException("NodeBmEJB.findNode()", SilverpeasRuntimeException.ERROR,
            "node.NODE_UNFINDABLE", "nodeId = " + pk.getId());
      }
    } catch (SQLException e) {
      throw new NodeRuntimeException("NodeEJB.ejbFindByPrimaryKey()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_FIND_ENTITY", "NodeId = " + pk.getId(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public NodeDetail getDetailByNameAndFatherId(NodePK pk, String name, int nodeFatherId) {
    Connection con = getConnection();
    try {
      NodeDetail nodeDetail = NodeDAO.selectByNameAndFatherId(con, pk, name, nodeFatherId);
      if (nodeDetail != null) {
        return nodeDetail;
      } else {
        throw new NodeRuntimeException("NodeBmEJB.getDetailByNameAndNodeFatherId()",
            SilverpeasRuntimeException.ERROR, "node.GETTING_NODE_DETAIL_FAILED",
            "nodeId = " + pk.getId() + ",name=" + name + "nodeFatherId=" + nodeFatherId);
      }
    } catch (SQLException e) {
      throw new NodeRuntimeException("NodeEJB.ejbFindByNameAndFatherId()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_FIND_ENTITY", "name = " + name
          + ", component = " + pk.getComponentName() + ", parent ID = " + nodeFatherId, e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Get the attributes of a node and of its children
   *
   * @return a NodeDetail
   * @see NodeDetail
   * @since 1.0
   */
  @Override
  @Transactional(Transactional.TxType.NOT_SUPPORTED)
  public NodeDetail getDetail(NodePK pk) {
    try {
      NodeDetail nodeDetail = findNode(pk);
      if (!NodeDetail.FILE_LINK_TYPE.equals(nodeDetail.getType())) {
        nodeDetail.setChildrenDetails(getChildrenDetails(pk));
      }

      // Add default translation
      NodeI18NDetail nodeI18NDetail = new NodeI18NDetail(nodeDetail.getLanguage(),
          nodeDetail.getName(), nodeDetail.getDescription());
      nodeDetail.addTranslation(nodeI18NDetail);
      List<NodeI18NDetail> translations = getTranslations(Integer.parseInt(pk.getId()));
      for (int t = 0; translations != null && t < translations.size(); t++) {
        nodeI18NDetail = translations.get(t);
        nodeDetail.addTranslation(nodeI18NDetail);
      }
      return nodeDetail;
    } catch (Exception re) {
      throw new NodeRuntimeException("NodeBmEJB.getDetail()", SilverpeasRuntimeException.ERROR,
          "node.GETTING_NODE_DETAIL_FAILED", "nodeId = " + pk.getId(), re);
    }

  }

  /**
   * Get the attributes of a node and of its children with transaction support
   *
   * @return a NodeDetail
   */
  @Override
  @Transactional(Transactional.TxType.SUPPORTS)
  public NodeDetail getDetailTransactionally(NodePK pk) {
    return getDetail(pk);
  }

  /**
   * Get Translations of the node
   *
   * @param nodeId
   * @return List of translations
   */
  private List<NodeI18NDetail> getTranslations(int nodeId) {
    Connection con = getConnection();
    try {
      return NodeI18NDAO.getTranslations(con, nodeId);
    } catch (SQLException re) {
      throw new NodeRuntimeException("NodeBmEJB.getTranslations()",
          SilverpeasRuntimeException.ERROR, "node.GETTING_TRANSLATIONS_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public ArrayList<NodeDetail> getTree(NodePK pk) {
    Connection con = getConnection();
    try {
      return NodeDAO.getTree(con, pk);
    } catch (SQLException re) {
      throw new NodeRuntimeException("NodeBmEJB.getTree()",
          SilverpeasRuntimeException.ERROR, "node.GETTING_TREE_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public ArrayList<NodeDetail> getSubTree(NodePK pk) {

    return getSubTree(pk, null, 0, null);
  }

  @Override
  public ArrayList<NodeDetail> getSubTree(NodePK pk, String sorting) {

    return getSubTree(pk, null, 0, sorting);
  }

  @Override
  public ArrayList<NodeDetail> getSubTreeByStatus(NodePK pk, String status) {

    return getSubTree(pk, status, 0, null);
  }

  @Override
  public ArrayList<NodeDetail> getSubTreeByStatus(NodePK pk, String status, String sorting) {

    return getSubTree(pk, status, 0, sorting);
  }

  @Override
  public ArrayList<NodeDetail> getSubTreeByLevel(NodePK pk, int level) {

    return getSubTree(pk, null, level, null);
  }

  @Override
  public ArrayList<NodeDetail> getSubTreeByLevel(NodePK pk, int level, String sorting) {

    return getSubTree(pk, null, level, sorting);
  }

  @Override
  public ArrayList<NodeDetail> getSubTree(NodePK pk, String status, int level, String sorting) {

    Connection con = getConnection();
    try {
      List<NodeDetail> headers = NodeDAO.getAllHeaders(con, pk, sorting, level);
      NodeDetail root = NodeDAO.loadRow(con, pk);
      root.setChildrenDetails(new ArrayList<NodeDetail>());
      Map<String, NodeDetail> tree = new HashMap<String, NodeDetail>();
      tree.put(root.getNodePK().getId(), root);
      for (NodeDetail header : headers) {
        header.setChildrenDetails(new ArrayList<NodeDetail>());
        tree.put(header.getNodePK().getId(), header);
      }

      for (NodeDetail header : headers) {
        NodeDetail father = tree.get(header.getFatherPK().getId());
        if (father != null) {
          father.getChildrenDetails().add(header);
        }
      }
      ArrayList<NodeDetail> result = new ArrayList<NodeDetail>();
      if (level == 0) {
        root = tree.get(root.getNodePK().getId());
        result = (ArrayList<NodeDetail>) processNode(result, root);
      } else {
        for (NodeDetail header : headers) {
          result.add(header);
        }
      }
      return result;
    } catch (SQLException re) {
      throw new NodeRuntimeException("NodeBmEJB.getSubTreeByStatus()",
          SilverpeasRuntimeException.ERROR, "node.GETTING_SUBTREE_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  private List<NodeDetail> processNode(List<NodeDetail> result, NodeDetail node) {
    result.add(node);
    Collection<NodeDetail> children = node.getChildrenDetails();
    if (children != null) {
      for (NodeDetail child : children) {
        processNode(result, child);
      }
    }
    return result;
  }

  @Override
  @Transactional
  public void moveNode(NodePK nodePK, NodePK toNode) {
    NodeDetail root = getDetail(toNode);
    String newRootPath = root.getPath() + toNode.getId() + '/';
    String oldRootPath = null;

    Connection con = getConnection();
    try {
      List<NodeDetail> tree = getSubTree(nodePK);
      for (int t = 0; t < tree.size(); t++) {
        NodeDetail node = tree.get(t);
        deleteIndex(node.getNodePK());
        if (t == 0) {
          oldRootPath = node.getPath();
          node.setFatherPK(toNode);
          node.setOrder(root.getChildrenNumber());
        }
        delete(node.getNodePK());

        // change data
        String newPath = node.getPath().replaceAll(oldRootPath, newRootPath);
        node.setPath(newPath);
        node.setLevel(StringUtil.countMatches(newPath, "/"));
        node.getNodePK().setComponentName(toNode.getInstanceId());
        node.setRightsDependsOn(root.getRightsDependsOn());
        node.setUseId(true);
        NodePK newNodePK = save(node);
        NodeDetail newNode = getDetail(newNodePK);
        createIndex(newNode, true);
      }

      NodeDAO.unvalidateTree(con, nodePK);
      NodeDAO.unvalidateTree(con, toNode);
    } catch (Exception e) {
      throw new NodeRuntimeException("NodeBmEJB.moveNode()", SilverpeasRuntimeException.ERROR,
          "node.MOVING_SUBTREE_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param pk
   * @return
   * @
   * @see
   */
  @Override
  public NodeDetail getFrequentlyAskedDetail(NodePK pk) {
    return getDetail(pk);
  }

  /**
   * Method declaration
   *
   * @param pk
   * @return
   * @
   * @see
   */
  @Override
  public NodeDetail getTwoLevelDetails(NodePK pk) {
    NodeDetail nd = getDetail(pk);
    Connection con = getConnection();
    try {
      Collection<NodeDetail> children = NodeDAO.getChildrenDetails(con, pk);
      List<NodeDetail> childrenDetail = new ArrayList<NodeDetail>();
      for (NodeDetail childDetail : children) {
        Collection<NodeDetail> subChildren = NodeDAO
            .getChildrenDetails(con, childDetail.getNodePK());
        List<NodeDetail> subChildrenDetail = new ArrayList<NodeDetail>();
        for (NodeDetail subChild : subChildren) {
          subChildrenDetail.add(subChild);
        }
        childDetail.setChildrenDetails(subChildrenDetail);
        childrenDetail.add(childDetail);
      }
      nd.setChildrenDetails(childrenDetail);
      return nd;
    } catch (SQLException re) {
      throw new NodeRuntimeException("NodeBmEJB.getTwoLevelDetails()",
          SilverpeasRuntimeException.ERROR, "node.GETTING_NODE_DETAIL_FAILED", "nodeId = " + pk.
          getId(), re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public NodeDetail getHeader(NodePK pk, boolean getTranslations) {
    Connection con = getConnection();
    try {
      return NodeDAO.loadRow(con, pk, getTranslations);
    } catch (SQLException re) {
      throw new NodeRuntimeException("NodeBmEJB.getHeader()", SilverpeasRuntimeException.ERROR,
          "node.GETTING_NODE_HEADER_FAILED", "nodeId = " + pk.getId(), re);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Get the attributes of THIS node
   *
   * @return a NodeDetail
   * @see NodeDetail
   * @since 1.0
   */
  @Override
  public NodeDetail getHeader(NodePK pk) {
    Connection con = getConnection();
    try {
      return NodeDAO.loadRow(con, pk);
    } catch (SQLException re) {
      throw new NodeRuntimeException("NodeBmEJB.getHeader()", SilverpeasRuntimeException.ERROR,
          "node.GETTING_NODE_HEADER_FAILED", "nodeId = " + pk.getId(), re);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Update the attributes of the node
   *
   * @param nd the NodeDetail which contains updated data
   * @see NodeDetail
   * @since 1.0
   */
  @Override
  @Transactional
  public void setDetail(NodeDetail nd) {
    NodeDetail oldNodeDetail = getHeader(nd.getNodePK());
    Connection con = getConnection();
    try {
      // I18N
      if (nd.isRemoveTranslation()) {
        // Remove of a translation is required
        if ("-1".equals(nd.getTranslationId())) {
          // Default language = translation
          List<NodeI18NDetail> translations = NodeI18NDAO.getTranslations(con, nd.getId());
          if (translations != null && !translations.isEmpty()) {
            NodeI18NDetail translation = translations.get(0);
            nd.setLanguage(translation.getLanguage());
            nd.setName(translation.getName());
            nd.setDescription(translation.getDescription());
            NodeI18NDAO.removeTranslation(con, translation.getId());
            updateNodeDetail(con, nd);
          }
        } else {
          NodeI18NDAO.removeTranslation(con, Integer.parseInt(nd.getTranslationId()));
        }
      } else {
        // Add or update a translation
        if (nd.getLanguage() != null) {
          String defaultLanguage = oldNodeDetail.getLanguage();
          if (defaultLanguage == null) {
            // translation for the first time
            nd.setLanguage(I18NHelper.defaultLanguage);
            defaultLanguage = nd.getLanguage();
          }

          String newLanguage = nd.getLanguage();

          if (!newLanguage.equals(defaultLanguage)) {
            NodeI18NDetail translation = new NodeI18NDetail(nd.getLanguage(), nd.getName(), nd.
                getDescription());
            translation.setNodeId(String.valueOf(nd.getId()));
            String translationId = nd.getTranslationId();
            if (translationId != null && !"-1".equals(translationId)) {
              // update translation
              translation.setId(Integer.parseInt(translationId));
              translation.setNodeId(String.valueOf(nd.getId()));
              NodeI18NDAO.updateTranslation(con, translation);
            } else {
              NodeI18NDAO.saveTranslation(con, translation);
            }
            NodeDAO.unvalidateTree(con, nd.getNodePK());
          } else {
            // the default language is modified
            updateNodeDetail(con, nd);
          }
        } else {
          // No i18n managed by this object
          updateNodeDetail(con, nd);
        }
      }
      // createIndex(nd);
      createIndex(nd.getNodePK());
    } catch (SQLException re) {
      throw new NodeRuntimeException("NodeBmEJB.setDetail()", SilverpeasRuntimeException.ERROR,
          "node.UPDATING_NODE_FAILED", "nodeId = " + nd.getNodePK().getId(), re);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Remove a node and its descendants
   *
   * @param pk the node PK to delete
   * @see NodePK
   * @since 1.0
   */
  @Override
  @Transactional
  public void removeNode(NodePK pk) {
    Connection connection = getConnection();
    try {
      NodeDeletion.deleteNodes(pk, connection, new AnonymousMethodOnNode() {
        @Override
        public void invoke(NodePK pk) throws Exception {
          // remove wysiwyg attached to node
          WysiwygController.deleteWysiwygAttachments(pk.getInstanceId(), "Node_" + pk.getId());

        }
      });
    } catch (Exception re) {
      throw new NodeRuntimeException("NodeBmEJB.removeNode()", SilverpeasRuntimeException.ERROR,
          "node.DELETING_NODE_FAILED", "nodeId = " + pk.getId(), re);
    } finally {
      DBUtil.close(connection);
    }
  }

  /**
   * Get the path of this node from this node to root
   *
   * @param pk The PK of the node
   * @return a NodeDetail Collection (only header)
   * @see NodeDetail
   * @see java.util.Collection
   * @since 1.0
   */
  @Override
  public Collection<NodeDetail> getPath(NodePK pk) {
    Connection con = getConnection();
    try {
      return NodeDAO.getAnotherPath(con, pk);
    } catch (SQLException re) {
      throw new NodeRuntimeException("NodeBmEJB.getAnotherPath()", SilverpeasRuntimeException.ERROR,
          "node.GETTING_NODE_PATH_FAILED", "nodeId = " + pk.getId(), re);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Get the header of each child of the node
   *
   * @return a NodeDetail collection
   * @see NodeDetail
   * @since 1.0
   */
  @Override
  public Collection<NodeDetail> getChildrenDetails(NodePK pk) {
    Connection con = getConnection();
    try {
      return NodeDAO.getChildrenDetails(con, pk);
    } catch (SQLException re) {
      throw new NodeRuntimeException("NodeBmEJB.getChildrenDetails()",
          SilverpeasRuntimeException.ERROR, "node.GETTING_NODE_SONS_FAILED", "nodeId = " + pk.
          getId(), re);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Get the header of each child of the node this method is to be used on frequently asked nodes
   * (next to the root), because all ejb will be instanciated
   *
   * @return a NodeDetail collection
   * @see NodeDetail
   * @since 1.0
   */
  @Override
  public Collection<NodeDetail> getFrequentlyAskedChildrenDetails(NodePK pk) {
    return getChildrenDetails(pk);
  }

  /**
   * Method declaration
   *
   * @param pk
   * @param level
   * @return
   * @
   * @see
   */
  @Override
  public List<NodeDetail> getHeadersByLevel(NodePK pk, int level) {
    Connection con = getConnection();
    try {
      return NodeDAO.getHeadersByLevel(con, pk, level);
    } catch (SQLException re) {
      throw new NodeRuntimeException("NodeBmEJB.getHeadersByLevel()",
          SilverpeasRuntimeException.ERROR, "node.GETTING_NODES_BY_LEVEL_FAILED",
          "nodeId = " + pk.getId() + ", level = " + level, re);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param nodePK
   * @return
   * @
   * @see
   */
  @Override
  public Collection<NodeDetail> getAllNodes(NodePK nodePK) {
    Connection con = getConnection();
    try {
      return NodeDAO.getAllHeaders(con, nodePK);
    } catch (SQLException re) {
      throw new NodeRuntimeException("NodeBmEJB.getAllNodes()",
          SilverpeasRuntimeException.ERROR, "node.GETTING_ALL_NODES_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Get the children number of this node
   *
   * @return a int
   * @since 1.0
   */
  @Override
  public int getChildrenNumber(NodePK pk) {
    Connection con = getConnection();
    try {
      return NodeDAO.getChildrenNumber(con, pk);
    } catch (SQLException re) {
      throw new NodeRuntimeException("NodeBmEJB.getChildrenNumber()",
          SilverpeasRuntimeException.ERROR, "node.GETTING_NUMBER_OF_SONS_FAILED", "nodeId = " + pk.
          getId(), re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  @Transactional
  public NodePK createNode(NodeDetail node) {
    NodePK parentPK = node.getFatherPK();
    if (parentPK != null) {
      NodeDetail parent = getHeader(parentPK);
      node.setPath(parent.getFullPath());
      node.setLevel(parent.getLevel() + 1);
    } else {
      node.setPath("/");
    }

    if (node.getLanguage() == null) {
      // translation for the first time
      node.setLanguage(I18NHelper.defaultLanguage);
    }
    try {
      NodePK newNodePK = save(node);
      NodeDetail newNode = getDetail(newNodePK);
      createIndex(newNode, false);
      return newNode.getNodePK();
    } catch (Exception e) {
      throw new NodeRuntimeException("NodeBmEJB.createNode()",
          SilverpeasRuntimeException.ERROR, "node.CREATING_NODE_FAILED", e);
    }
  }

  /**
   * Create a new Node object
   *
   * @param nd the NodeDetail which contains data
   * @param fatherDetail the PK of the user who have create this node
   * @return the NodePK of the new Node
   * @see NodeDetail
   * @since 1.0
   */
  @Override
  public NodePK createNode(NodeDetail nd, NodeDetail fatherDetail) {
    try {
      if (!NodeDetail.FILE_LINK_TYPE.equals(nd.getType())) {
        nd.setPath(fatherDetail.getPath() + fatherDetail.getNodePK().getId() + "/");
      }
      nd.setLevel(fatherDetail.getLevel() + 1);
      nd.setFatherPK(fatherDetail.getNodePK());
      if (nd.getLanguage() == null) {
        // translation for the first time
        nd.setLanguage(I18NHelper.defaultLanguage);
      }
      NodePK newNodePK = save(nd);
      NodeDetail newNode = getDetail(newNodePK);

      createIndex(newNode, false);
      return newNode.getNodePK();
    } catch (Exception re) {
      throw new NodeRuntimeException("NodeBmEJB.createNode()",
          SilverpeasRuntimeException.ERROR, "node.CREATING_NODE_FAILED", re);
    }
  }

  /**
   * Create a new Node object.
   *
   * @param nd the NodeDetail which contains data
   * @return the NodePK of the new Node
   * @see NodeDetail
   * @throws javax.ejb.CreateException
   * @since 1.0
   */
  private NodePK save(NodeDetail nd) {
    NodePK newNodePK = null;
    Connection con = getConnection();
    try {
      // insert row in the database
      newNodePK = NodeDAO.insertRow(con, nd);
      int rightsDependsOn = nd.getRightsDependsOn();
      if (rightsDependsOn == 0) {
        rightsDependsOn = Integer.parseInt(newNodePK.getId());
      }
      if (nd.haveRights()) {
        NodeDAO.updateRightsDependency(con, newNodePK, rightsDependsOn);
      }
      nd.setNodePK(newNodePK);
      createTranslations(con, nd);
    } catch (SQLException e) {
      throw new NodeRuntimeException("NodeBMEJB.create()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_INSERT_ENTITY_ATTRIBUTES", e);
    } finally {
      DBUtil.close(con);
    }
    return newNodePK;
  }

  private void updateNodeDetail(NodeDetail detail) {
    Connection con = getConnection();
    try {
      updateNodeDetail(con, detail);
    } catch (SQLException ex) {
      throw new NodeRuntimeException("NodeBMEJB.update()", SilverpeasRuntimeException.ERROR,
          "root.EX_CANT_STORE_ENTITY_ATTRIBUTES", "NodeId = " + detail.getNodePK().getId(), ex);
    } finally {
      DBUtil.close(con);
    }
  }

  private void updateNodeDetail(Connection con, NodeDetail detail) throws SQLException {
    NodeDetail currentNode = NodeDAO.loadRow(con, detail.getNodePK());
    if (detail.getName() != null) {
      currentNode.setName(detail.getName());
    }
    if (detail.getDescription() != null) {
      currentNode.setDescription(detail.getDescription());
    }
    if (detail.getCreationDate() != null) {
      currentNode.setCreationDate(detail.getCreationDate());
    }
    if (detail.getCreatorId() != null) {
      currentNode.setCreatorId(detail.getCreatorId());
    }
    if (detail.getModelId() != null) {
      currentNode.setModelId(detail.getModelId());
    }
    if (detail.getStatus() != null) {
      currentNode.setStatus(detail.getStatus());
    }
    if (detail.getType() != null) {
      currentNode.setType(detail.getType());
    }
    if (NodeDetail.FILE_LINK_TYPE.equals(detail.getType())) {
      currentNode.setPath(detail.getPath());
    }
    if (detail.getFatherPK() != null
        && StringUtil.isInteger(detail.getFatherPK().getId())
        && StringUtil.isDefined(detail.getFatherPK().getInstanceId())) {
      currentNode.setFatherPK(detail.getFatherPK());
    }
    if (StringUtil.isDefined(detail.getPath())) {
      currentNode.setPath(detail.getPath());
    }
    currentNode.setRightsDependsOn(detail.getRightsDependsOn());
    currentNode.setOrder(detail.getOrder());
    currentNode.setLanguage(detail.getLanguage());
    NodeDAO.storeRow(con, currentNode);
  }

  private void delete(NodePK nodePK) {
    Connection con = getConnection();
    try {
      NodeDAO.deleteRow(con, nodePK);
    } catch (SQLException ex) {
      throw new NodeRuntimeException("NodeBMEJB.delete()", SilverpeasRuntimeException.ERROR,
          "root.EX_CANT_DELETE_ENTITY", "NodeId = " + nodePK.getId(), ex);
    } finally {
      DBUtil.close(con);
    }
  }

  private void createTranslations(Connection con, NodeDetail node) throws SQLException,
      UtilException {
    if (node.getTranslations() != null) {
      for (final NodeI18NDetail translation : node.getTranslations().values()) {
        if (node.getLanguage() != null && !node.getLanguage().equals(translation.getLanguage())) {
          translation.setObjectId(node.getNodePK().getId());
          NodeI18NDAO.saveTranslation(con, translation);
        }
      }
    }
  }

  private Connection getConnection() {
    try {
      return DBUtil.openConnection();
    } catch (Exception e) {
      throw new NodeRuntimeException("NodeEJB.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  /**
   * On node creation, check if another node have got the same name with same father
   *
   * @param nd A NodeDetail contains new node data to compare
   * @return true if there is already a node with same name with same father false else
   * @see NodeDetail
   * @since 1.0
   */
  @Override
  public boolean isSameNameSameLevelOnCreation(NodeDetail nd) {
    Connection con = getConnection();
    try {
      boolean result = NodeDAO.isSameNameSameLevelOnCreation(con, nd);
      return result;
    } catch (SQLException re) {
      throw new NodeRuntimeException("NodeBmEJB.isSameNameSameLevelOnCreation()",
          SilverpeasRuntimeException.ERROR,
          "node.KNOWING_IF_SAME_NAME_SAME_LEVEL_ON_CREATION_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * On node update, check if another node have got the same name with same father
   *
   * @param nd A NodeDetail contains new node data to compare
   * @return true if there is already a node with same name with same father false else
   * @see NodeDetail
   * @since 1.0
   */
  @Override
  public boolean isSameNameSameLevelOnUpdate(NodeDetail nd) {
    Connection con = getConnection();
    try {
      return NodeDAO.isSameNameSameLevelOnUpdate(con, nd);
    } catch (SQLException re) {
      throw new NodeRuntimeException("NodeBmEJB.isSameNameSameLevelOnUpdate()",
          SilverpeasRuntimeException.ERROR,
          "node.KNOWING_IF_SAME_NAME_SAME_LEVEL_ON_UPDATE_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Get children node PKs of a node
   *
   * @param nodePK A NodePK
   * @return A collection of NodePK
   * @see NodePK
   * @since 1.0
   */
  @Override
  public Collection<NodePK> getChildrenPKs(NodePK nodePK) {
    Connection con = getConnection();
    try {
      return NodeDAO.getChildrenPKs(con, nodePK);
    } catch (SQLException re) {
      throw new NodeRuntimeException("NodeBmEJB.getChildrenPKs()",
          SilverpeasRuntimeException.ERROR, "node.GETTING_PK_OF_SONS_FAILED",
          "nodeId = " + nodePK.getId(), re);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Get descendant node PKs of a node
   *
   * @param nodePK A NodePK
   * @return A collection of NodePK
   * @see NodePK
   * @since 1.0
   */
  @Override
  public Collection<NodePK> getDescendantPKs(NodePK nodePK) {
    Connection con = getConnection();
    try {
      return NodeDAO.getDescendantPKs(con, nodePK);
    } catch (SQLException re) {
      throw new NodeRuntimeException("NodeBmEJB.getDescendantPKs()",
          SilverpeasRuntimeException.ERROR,
          "node.GETTING_PK_OF_DESCENDANTS_FAILED", "nodeId = " + nodePK.getId(), re);
    } finally {
      DBUtil.close(con);
    }

  }

  /**
   * Get descendant node details of a node
   *
   * @param nodePK A NodePK
   * @return A List of NodeDetail
   * @see NodePK
   * @since 1.0
   */
  @Override
  public List<NodeDetail> getDescendantDetails(NodePK nodePK) {
    Connection con = getConnection();
    try {
      return NodeDAO.getDescendantDetails(con, nodePK);
    } catch (SQLException re) {
      throw new NodeRuntimeException("NodeBmEJB.getDescendantDetails()",
          SilverpeasRuntimeException.ERROR, "node.GETTING_DETAIL_OF_DESCENDANTS_FAILED", "nodeId = "
          + nodePK.getId(), re);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Get descendant node details of a node
   *
   * @param node A NodeDetail
   * @return A List of NodeDetail
   * @since 4.07
   */
  @Override
  public List<NodeDetail> getDescendantDetails(NodeDetail node) {
    Connection con = getConnection();
    try {
      return NodeDAO.getDescendantDetails(con, node);
    } catch (SQLException re) {
      throw new NodeRuntimeException("NodeBmEJB.getDescendantDetails()",
          SilverpeasRuntimeException.ERROR, "node.GETTING_DETAIL_OF_DESCENDANTS_FAILED", "nodeId = "
          + node.getNodePK().getId(), re);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Get the path from root to a node
   *
   * @param nodePK A NodePK
   * @return A collection of NodeDetail
   * @see NodePK
   * @see NodeDetail
   * @since 1.0
   * @deprecated
   */
  @Override
  public Collection<NodeDetail> getAnotherPath(NodePK nodePK) {
    // TODO : methode a supprimer ! il faut utiliser getPath()
    Connection con = getConnection();
    try {
      return NodeDAO.getAnotherPath(con, nodePK);
    } catch (SQLException re) {
      throw new NodeRuntimeException("NodeBmEJB.getAnotherPath()",
          SilverpeasRuntimeException.ERROR, "node.GETTING_NODE_PATH_FAILED",
          "nodeId = " + nodePK.getId(), re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void processWysiwyg(NodePK nodePK) {
    NodeDetail nodeDetail = getHeader(nodePK);
    createIndex(nodeDetail);
  }

  @Override
  @Transactional
  public void updateRightsDependency(NodeDetail nodeDetail) {
    updateNodeDetail(nodeDetail);
    try {
      spreadRightsDependency(nodeDetail, nodeDetail.getRightsDependsOn());
    } catch (SQLException e) {
      throw new NodeRuntimeException("NodeBmEJB.updateRightsDependency()",
          SilverpeasRuntimeException.ERROR, "node.SPREADING_RIGHTS_DEPENDENCY_FAILED", "nodeId = "
          + nodeDetail.getNodePK().getId(), e);
    }
  }

  private void spreadRightsDependency(NodeDetail currentNode, int rightsDependsOn) throws
      SQLException {
    Collection<NodeDetail> children = getChildrenDetails(currentNode.getNodePK());
    for (NodeDetail child : children) {
      if (!child.haveLocalRights()) {
        child.setRightsDependsOn(rightsDependsOn);
        updateNodeDetail(child);
        spreadRightsDependency(child, rightsDependsOn);
      }
    }
  }

  @Override
  public void sortNodes(List<NodePK> nodePKs) {
    Connection con = getConnection();
    try {
      NodeDAO.sortNodes(con, nodePKs);
    } catch (SQLException e) {
      throw new NodeRuntimeException("NodeBmEJB.sortNodes()", SilverpeasRuntimeException.ERROR,
          "node.SORTING_NODES_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void createIndex(NodeDetail nodeDetail) {
    createIndex(nodeDetail, true);
  }

  private void createIndex(NodePK pk) {
    NodeDetail node = getDetail(pk);
    createIndex(node);
  }

  private void createIndex(NodeDetail nodeDetail, boolean processWysiwygContent) {

    FullIndexEntry indexEntry = null;

    if (nodeDetail != null) {
      // Index the Node
      indexEntry = new FullIndexEntry(nodeDetail.getNodePK().getComponentName(), "Node", nodeDetail.
          getNodePK().getId());

      Iterator<String> languages = nodeDetail.getLanguages();
      while (languages.hasNext()) {
        String language = languages.next();
        NodeI18NDetail translation = (NodeI18NDetail) nodeDetail.getTranslation(language);

        indexEntry.setTitle(translation.getName(), language);
        indexEntry.setPreview(translation.getDescription(), language);

        if (processWysiwygContent) {
          updateIndexEntryWithWysiwygContent(indexEntry, nodeDetail.getNodePK(), language);
        }
      }

      indexEntry.setCreationDate(nodeDetail.getCreationDate());
      String userId;
      // cas d'une creation (avec creatorId, creationDate)
      if (nodeDetail.getCreatorId() != null) {
        userId = nodeDetail.getCreatorId();
        indexEntry.setCreationUser(userId);
      } // cas d'une modification
      else {
        NodeDetail node = getHeader(nodeDetail.getNodePK());
        indexEntry.setCreationDate(node.getCreationDate());
        userId = node.getCreatorId();
        indexEntry.setCreationUser(userId);
      }

      // index creator's full name
      if (nodeSettings.getString("indexAuthorName").equals("true")) {
        try {
          UserDetail ud = AdministrationServiceProvider.getAdminService().getUserDetail(userId);
          if (ud != null) {
            indexEntry.addTextContent(ud.getDisplayedName());
          }
        } catch (AdminException e) {
          // do not index on user name
        }
      }
    }
    IndexEngineProxy.addIndexEntry(indexEntry);
  }

  private void updateIndexEntryWithWysiwygContent(FullIndexEntry indexEntry, NodePK nodePK,
      String language) {

    try {
      if (nodePK != null) {
        String wysiwygContent = WysiwygController.load(nodePK.getComponentName(), "Node_" + nodePK
            .getId(), language);
        if (wysiwygContent != null) {
          indexEntry.addTextContent(wysiwygContent);
        }
      }
    } catch (Exception e) {
      // No wysiwyg associated
    }
  }

  /**
   * Called on : - removeNode()
   */
  @Override
  public void deleteIndex(NodePK pk) {

    IndexEntryKey indexEntry = new IndexEntryKey(pk.getComponentName(), "Node", pk.getId());
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }
}
