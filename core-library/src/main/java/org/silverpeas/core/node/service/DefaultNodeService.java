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
package org.silverpeas.core.node.service;

import org.apache.commons.lang3.StringUtils;
import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.node.dao.NodeDAO;
import org.silverpeas.core.node.dao.NodeI18NDAO;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodeI18NDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.model.NodePath;
import org.silverpeas.core.node.model.NodeRuntimeException;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.util.StringUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.silverpeas.core.node.model.NodeDetail.NO_RIGHTS_DEPENDENCY;

/**
 * This is the default implementation of NodeService. A node is composed by some another nodes
 * (children) and have got one and only one father. It describes a tree.
 *
 * @author Nicolas Eysseric
 */
@Service
@Singleton
@Transactional(Transactional.TxType.SUPPORTS)
public class DefaultNodeService implements NodeService, ComponentInstanceDeletion {

  /**
   * Database name where is stored nodes
   */
  private static final SettingBundle nodeSettings =
      ResourceLocator.getSettingBundle("org.silverpeas.node.nodeSettings");

  @Inject
  private NodeDAO nodeDAO;
  @Inject
  private NodeDeletion nodeDeletion;

  @Override
  @Transactional
  public void delete(final String componentInstanceId) {
    try {
      NodeI18NDAO.deleteComponentInstanceData(componentInstanceId);
      nodeDAO.deleteComponentInstanceData(componentInstanceId);
    } catch (SQLException e) {
      throw new NodeRuntimeException(e);
    }
  }

  private NodeDetail findNode(NodePK pk) {
    Connection con = getConnection();
    try {
      NodeDetail nodeDetail = nodeDAO.selectByPrimaryKey(con, pk);
      if (nodeDetail != null) {
        return nodeDetail;
      } else {
        throw new NodeRuntimeException("Node not found nodeId = " + pk.getId());
      }
    } catch (SQLException e) {
      throw new NodeRuntimeException(e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public NodeDetail getDetailByNameAndFatherId(NodePK pk, String name, int nodeFatherId) {
    Connection con = getConnection();
    try {
      return nodeDAO.selectByNameAndFatherId(con, pk, name, nodeFatherId);
    } catch (SQLException e) {
      throw new NodeRuntimeException(e);
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
  @Transactional(Transactional.TxType.SUPPORTS)
  public NodeDetail getDetail(NodePK pk) {
    try {
      NodeDetail nodeDetail = findNode(pk);
      if (!NodeDetail.FILE_LINK_TYPE.equals(nodeDetail.getNodeType())) {
        nodeDetail.setChildrenDetails(getChildrenDetails(pk));
      }

      // Add default translation
      NodeI18NDetail nodeI18NDetail = new NodeI18NDetail(nodeDetail.getLanguage(),
          nodeDetail.getName(), nodeDetail.getDescription());
      nodeDetail.addTranslation(nodeI18NDetail);
      List<NodeI18NDetail> translations = getTranslations(pk.getId());
      for (NodeI18NDetail translation : translations) {
        nodeI18NDetail = translation;
        nodeDetail.addTranslation(nodeI18NDetail);
      }
      return nodeDetail;
    } catch (Exception re) {
      throw new NodeRuntimeException(re);
    }

  }

  private List<NodeI18NDetail> getTranslations(String nodeId) {
    Connection con = getConnection();
    try {
      return NodeI18NDAO.getTranslations(con, nodeId);
    } catch (SQLException re) {
      throw new NodeRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public List<NodeDetail> getTree(NodePK pk) {
    Connection con = getConnection();
    try {
      return nodeDAO.getTree(con, pk);
    } catch (SQLException re) {
      throw new NodeRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public List<NodeDetail> getSubTree(NodePK pk) {

    return getSubTree(pk, null, 0, null);
  }

  @Override
  public List<NodeDetail> getSubTree(NodePK pk, String sorting) {

    return getSubTree(pk, null, 0, sorting);
  }

  @Override
  public List<NodeDetail> getSubTreeByStatus(NodePK pk, String status) {

    return getSubTree(pk, status, 0, null);
  }

  @Override
  public List<NodeDetail> getSubTreeByStatus(NodePK pk, String status, String sorting) {

    return getSubTree(pk, status, 0, sorting);
  }

  @Override
  public List<NodeDetail> getSubTreeByLevel(NodePK pk, int level) {

    return getSubTree(pk, null, level, null);
  }

  @Override
  public List<NodeDetail> getSubTreeByLevel(NodePK pk, int level, String sorting) {

    return getSubTree(pk, null, level, sorting);
  }

  @Override
  public List<NodeDetail> getSubTree(NodePK pk, String status, int level, String sorting) {

    Connection con = getConnection();
    try {
      List<NodeDetail> headers = nodeDAO.getAllHeaders(con, pk, sorting, level);
      NodeDetail root = nodeDAO.loadRow(con, pk);
      root.setChildrenDetails(new ArrayList<>());
      Map<String, NodeDetail> tree = new HashMap<>(headers.size());
      tree.put(root.getNodePK().getId(), root);
      for (NodeDetail header : headers) {
        header.setChildrenDetails(new ArrayList<>());
        tree.put(header.getNodePK().getId(), header);
      }

      for (NodeDetail header : headers) {
        NodeDetail father = tree.get(header.getFatherPK().getId());
        if (father != null) {
          father.getChildrenDetails().add(header);
        }
      }
      ArrayList<NodeDetail> result = new ArrayList<>();
      if (level == 0) {
        root = tree.get(root.getNodePK().getId());
        processNode(result, root);
      } else {
        result.addAll(headers);
      }
      return result;
    } catch (SQLException re) {
      throw new NodeRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  private void processNode(List<NodeDetail> result, NodeDetail node) {
    result.add(node);
    Collection<NodeDetail> children = node.getChildrenDetails();
    if (children != null) {
      for (NodeDetail child : children) {
        processNode(result, child);
      }
    }
  }

  @Override
  @Transactional
  public void moveNode(NodePK nodePK, NodePK toNode) {
    moveNode(nodePK, toNode, false);
  }

  @Override
  @Transactional
  public void moveNode(NodePK nodePK, NodePK toNode, boolean preserveRights) {
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

        // change data
        String newPath = node.getPath().replaceAll(oldRootPath, newRootPath);
        node.setPath(newPath);
        node.setLevel(StringUtils.countMatches(newPath, "/"));
        node.getNodePK().setComponentName(toNode.getInstanceId());
        if (preserveRights) {
          if (t == 0 && !node.haveLocalRights()) {
            node.setRightsDependsOn(root.getRightsDependsOn());
          }
        } else {
          node.setRightsDependsOn(NO_RIGHTS_DEPENDENCY);
        }
        node.setUseId(true);
        nodeDAO.moveNode(con, node);
        createIndex(node, true);
      }

      nodeDAO.unvalidateTree(con, nodePK);
      nodeDAO.unvalidateTree(con, toNode);
    } catch (Exception e) {
      throw new NodeRuntimeException(e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public NodeDetail getHeader(NodePK pk, boolean getTranslations) {
    Connection con = getConnection();
    try {
      return nodeDAO.loadRow(con, pk, getTranslations);
    } catch (SQLException re) {
      throw new NodeRuntimeException(re);
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
      return nodeDAO.loadRow(con, pk);
    } catch (SQLException re) {
      throw new NodeRuntimeException(re);
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
      if (nd.isRemoveTranslation()) {
        removeTranslation(con, oldNodeDetail, nd);
      } else {
        addOrUpdateTranslation(con, oldNodeDetail, nd);
      }
      createIndex(nd.getNodePK());
    } catch (SQLException re) {
      throw new NodeRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  private void addOrUpdateTranslation(final Connection con, final NodeDetail oldNodeDetail,
      final NodeDetail nd) throws SQLException {
    // Add or update a translation
    if (nd.getLanguage() != null) {
      String defaultLanguage = oldNodeDetail.getLanguage();
      if (defaultLanguage == null) {
        // translation for the first time
        nd.setLanguage(I18NHelper.DEFAULT_LANGUAGE);
        defaultLanguage = nd.getLanguage();
      }

      String newLanguage = nd.getLanguage();

      if (!newLanguage.equals(defaultLanguage)) {
        NodeI18NDetail translation = new NodeI18NDetail(nd.getLanguage(), nd.getName(), nd.
            getDescription());
        translation.setNodeId(nd.getId());
        String translationId = nd.getTranslationId();
        if (StringUtil.isDefined(translationId) && !"-1".equals(translationId)) {
          // update translation
          translation.setId(translationId);
          translation.setNodeId(nd.getId());
          NodeI18NDAO.updateTranslation(con, translation);
        } else {
          NodeI18NDAO.saveTranslation(con, translation);
        }
        nodeDAO.unvalidateTree(con, nd.getNodePK());
      } else {
        // the default language is modified
        update(con, updateNodeDetailWith(oldNodeDetail, nd));
      }
    } else {
      // No i18n managed by this object
      update(con, updateNodeDetailWith(oldNodeDetail, nd));
    }
  }

  private void removeTranslation(final Connection con, final NodeDetail oldNodeDetail,
      final NodeDetail nd) throws SQLException {
    // Remove of a translation is required
    if ("-1".equals(nd.getTranslationId())) {
      // Default language = translation
      List<NodeI18NDetail> translations = NodeI18NDAO.getTranslations(con, nd.getId());
      if (!translations.isEmpty()) {
        NodeI18NDetail translation = translations.get(0);
        nd.setLanguage(translation.getLanguage());
        nd.setName(translation.getName());
        nd.setDescription(translation.getDescription());
        NodeI18NDAO.removeTranslation(con, translation.getId());
        update(con, updateNodeDetailWith(oldNodeDetail, nd));
      }
    } else {
      NodeI18NDAO.removeTranslation(con, nd.getTranslationId());
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
  public void deleteNode(NodePK pk) {
    Connection connection = getConnection();
    try {
      nodeDeletion.deleteNodes(pk, connection, pk1 ->
          // remove wysiwyg attached to node
          WysiwygController.deleteWysiwygAttachments(pk1.getInstanceId(), "Node_" + pk1.getId()));
    } catch (Exception re) {
      throw new NodeRuntimeException(re);
    } finally {
      DBUtil.close(connection);
    }
  }

  @Override
  @Transactional
  public void removeNode(NodeDetail nodeDetail) {
    try (Connection connection = getConnection()) {
      User remover = User.getCurrentRequester();
      nodeDAO.removeNode(connection, nodeDetail, remover.getId());
    } catch (Exception re) {
      throw new NodeRuntimeException(re);
    }
  }

  @Override
  @Transactional
  public void restoreNode(NodeDetail node) {
    try(Connection connection = getConnection()) {
      nodeDAO.restoreNode(connection, node);
    } catch (Exception re) {
      throw new NodeRuntimeException(re);
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
  public NodePath getPath(NodePK pk) {
    Connection con = getConnection();
    try {
      return nodeDAO.getNodePath(con, pk);
    } catch (SQLException re) {
      throw new NodeRuntimeException(re);
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
      return nodeDAO.getChildrenDetails(con, pk);
    } catch (SQLException re) {
      throw new NodeRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public List<NodeDetail> getHeadersByLevel(NodePK pk, int level) {
    Connection con = getConnection();
    try {
      return nodeDAO.getHeadersByLevel(con, pk, level);
    } catch (SQLException re) {
      throw new NodeRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<NodeDetail> getAllNodes(NodePK nodePK) {
    Connection con = getConnection();
    try {
      return nodeDAO.getAllHeaders(con, nodePK);
    } catch (SQLException re) {
      throw new NodeRuntimeException(re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public List<NodeDetail> getMinimalDataByInstances(final Collection<String> instanceIds) {
    try (final Connection con = getConnection()) {
      return nodeDAO.getMinimalDataByInstances(con, instanceIds);
    } catch (SQLException e) {
      throw new NodeRuntimeException(e);
    }
  }

  @Override
  @Transactional
  public NodeDetail createNode(NodeDetail node) {
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
      node.setLanguage(I18NHelper.DEFAULT_LANGUAGE);
    }
    try {
      NodePK newNodePK = save(node);
      NodeDetail newNode = getDetail(newNodePK);
      createIndex(newNode, false);
      return newNode;
    } catch (Exception e) {
      throw new NodeRuntimeException(e);
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
  @Transactional
  public NodeDetail createNode(NodeDetail nd, NodeDetail fatherDetail) {
    try {
      if (!NodeDetail.FILE_LINK_TYPE.equals(nd.getNodeType())) {
        nd.setPath(fatherDetail.getPath() + fatherDetail.getNodePK().getId() + "/");
      }
      nd.setLevel(fatherDetail.getLevel() + 1);
      nd.setFatherPK(fatherDetail.getNodePK());
      if (nd.getLanguage() == null) {
        // translation for the first time
        nd.setLanguage(I18NHelper.DEFAULT_LANGUAGE);
      }
      NodePK newNodePK = save(nd);
      NodeDetail newNode = getDetail(newNodePK);

      createIndex(newNode, false);
      return newNode;
    } catch (Exception re) {
      throw new NodeRuntimeException(re);
    }
  }

  private NodePK save(NodeDetail nd) {
    NodePK newNodePK;
    Connection con = getConnection();
    try {
      // insert row in the database
      newNodePK = nodeDAO.insertRow(con, nd);
      String rightsDependsOn = nd.getRightsDependsOn();
      if (rightsDependsOn.equals(NodePK.ROOT_NODE_ID)) {
        rightsDependsOn = newNodePK.getId();
      }
      if (nd.haveRights()) {
        nodeDAO.updateRightsDependency(con, newNodePK, rightsDependsOn);
      }
      nd.setNodePK(newNodePK);
      createTranslations(con, nd);
    } catch (SQLException e) {
      throw new NodeRuntimeException(e);
    } finally {
      DBUtil.close(con);
    }
    return newNodePK;
  }

  /**
   * Updates the node referred by the identifier of the specified node with the attributes of the
   * given node. Children of the node aren't processed.
   *
   * @param detail the node with which its counter part in the data source has to be updated.
   */
  private void updateNodeDetail(NodeDetail detail) {
    Connection con = getConnection();
    try {
      NodeDetail oldDetail = getHeader(detail.getNodePK());
      update(con, updateNodeDetailWith(oldDetail, detail));
    } catch (SQLException ex) {
      throw new NodeRuntimeException(ex);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Updates the specified node got directly from the database with the attributes of the another
   * node. Children of the node aren't processed.
   *
   * @param nodeToUpdate the node to update. It must represents the current state of the node and
   * as such it has to be provided from the database.
   * @param newState the new state of the node.
   * @return the updated node.
   */
  private NodeDetail updateNodeDetailWith(NodeDetail nodeToUpdate, NodeDetail newState) {
    if (newState.getDescription() != null) {
      nodeToUpdate.setDescription(newState.getDescription());
    }
    if (newState.getName() != null) {
      nodeToUpdate.setName(newState.getName());
    }
    if (newState.getCreationDate() != null) {
      nodeToUpdate.setCreationDate(newState.getCreationDate());
    }
    if (newState.getCreatorId() != null) {
      nodeToUpdate.setCreatorId(newState.getCreatorId());
    }
    if (newState.getModelId() != null) {
      nodeToUpdate.setModelId(newState.getModelId());
    }
    if (newState.getStatus() != null) {
      nodeToUpdate.setStatus(newState.getStatus());
    }
    if (newState.getNodeType() != null) {
      nodeToUpdate.setNodeType(newState.getNodeType());
    }
    if (NodeDetail.FILE_LINK_TYPE.equals(newState.getNodeType())) {
      nodeToUpdate.setPath(newState.getPath());
    }
    if (newState.getFatherPK() != null
        && StringUtil.isInteger(newState.getFatherPK().getId())
        && StringUtil.isDefined(newState.getFatherPK().getInstanceId())) {
      nodeToUpdate.setFatherPK(newState.getFatherPK());
    }
    if (StringUtil.isDefined(newState.getPath())) {
      nodeToUpdate.setPath(newState.getPath());
    }
    nodeToUpdate.setRightsDependsOn(newState.getRightsDependsOn());
    nodeToUpdate.setOrder(newState.getOrder());
    nodeToUpdate.setLanguage(newState.getLanguage());
    return nodeToUpdate;
  }

  /**
   * Updates in the data source the specified node.
   *
   * @param con the connection to the datasource
   * @param detail the node from which its counterpart in the datasource is updated.
   * @throws SQLException if an error occurs while updating the node in the datasource.
   */
  private void update(Connection con, NodeDetail detail) throws SQLException {
    nodeDAO.storeRow(con, detail);
  }

  private void createTranslations(Connection con, NodeDetail node) throws SQLException {
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
      throw new NodeRuntimeException(e);
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
      return nodeDAO.isSameNameSameLevelOnCreation(con, nd);
    } catch (SQLException re) {
      throw new NodeRuntimeException(re);
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
      return nodeDAO.isSameNameSameLevelOnUpdate(con, nd);
    } catch (SQLException re) {
      throw new NodeRuntimeException(re);
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
      return nodeDAO.getChildrenPKs(con, nodePK);
    } catch (SQLException re) {
      throw new NodeRuntimeException(re);
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
      return nodeDAO.getDescendantPKs(con, nodePK);
    } catch (SQLException re) {
      throw new NodeRuntimeException(re);
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
      return nodeDAO.getDescendantDetails(con, nodePK);
    } catch (SQLException re) {
      throw new NodeRuntimeException(re);
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
      return nodeDAO.getDescendantDetails(con, node);
    } catch (SQLException re) {
      throw new NodeRuntimeException(re);
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
    spreadRightsDependency(nodeDetail, nodeDetail.getRightsDependsOn());
  }

  private void spreadRightsDependency(NodeDetail currentNode, String rightsDependsOn) {
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
      nodeDAO.sortNodes(con, nodePKs);
    } catch (SQLException e) {
      throw new NodeRuntimeException(e);
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
    Objects.requireNonNull(nodeDetail);
    final FullIndexEntry indexEntry = getFullIndexEntry(nodeDetail, processWysiwygContent);

    indexEntry.setCreationDate(nodeDetail.getCreationDate());
    final String userId;
    // case of a creation (with creatorId and creationDate)
    if (nodeDetail.getCreatorId() != null) {
      userId = nodeDetail.getCreatorId();
      indexEntry.setCreationUser(userId);
    } else {
      // case of a modification
      NodeDetail node = getHeader(nodeDetail.getNodePK());
      indexEntry.setCreationDate(node.getCreationDate());
      userId = node.getCreatorId();
      indexEntry.setCreationUser(userId);
    }

    // index creator's full name
    if (nodeSettings.getString("indexAuthorName").equals("true")) {
      UserDetail ud = UserDetail.getById(userId);
      if (ud != null) {
        indexEntry.addTextContent(ud.getDisplayedName());
      }
    }
    IndexEngineProxy.addIndexEntry(indexEntry);
  }

  private FullIndexEntry getFullIndexEntry(NodeDetail nodeDetail, boolean processWysiwygContent) {
    final FullIndexEntry indexEntry =
        new FullIndexEntry(new IndexEntryKey(nodeDetail.getNodePK().getComponentName(), "Node",
            nodeDetail.getNodePK().getId()));

    final Collection<String> languages = nodeDetail.getLanguages();
    languages.forEach(l -> {
      NodeI18NDetail translation = nodeDetail.getTranslations().get(l);
      indexEntry.setTitle(translation.getName(), l);
      indexEntry.setPreview(translation.getDescription(), l);
      if (processWysiwygContent) {
        updateIndexEntryWithWysiwygContent(indexEntry, nodeDetail.getNodePK(), l);
      }
    });
    return indexEntry;
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
