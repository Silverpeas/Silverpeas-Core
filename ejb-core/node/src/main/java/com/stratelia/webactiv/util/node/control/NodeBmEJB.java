/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.stratelia.webactiv.util.node.control;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.silverpeas.search.indexEngine.model.FullIndexEntry;
import org.silverpeas.search.indexEngine.model.IndexEngineProxy;
import org.silverpeas.search.indexEngine.model.IndexEntryPK;

import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.util.i18n.Translation;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.wysiwyg.control.WysiwygController;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.AdminReference;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.node.ejb.Node;
import com.stratelia.webactiv.util.node.ejb.NodeDAO;
import com.stratelia.webactiv.util.node.ejb.NodeHome;
import com.stratelia.webactiv.util.node.ejb.NodeI18NDAO;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodeI18NDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.node.model.NodeRuntimeException;

/**
 * This is the NodeBM EJB-tier controller. A node is composed by some another nodes (children) and
 * have got one and only one father. It describes a tree. It is implemented as a session EJB.
 *
 * @author Nicolas Eysseric
 */
public class NodeBmEJB implements SessionBean, NodeBmBusinessSkeleton {

  private static final long serialVersionUID = 1L;
  /**
   * Database name where is stored nodes
   */
  private String dbName = JNDINames.NODE_DATASOURCE;
  private static final ResourceLocator nodeSettings = new ResourceLocator(
      "org.silverpeas.util.node.nodeSettings", "fr");

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  private NodeHome getNodeHome() {
    try {
      return EJBUtilitaire.getEJBObjectRef(JNDINames.NODE_EJBHOME, NodeHome.class);
    } catch (UtilException re) {
      throw new NodeRuntimeException("NodeBmEJB.getNodeHome()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", re);
    }
  }

  /**
   * Method declaration
   *
   * @param pk
   * @return
   * @see
   */
  private Node findNode(NodePK pk) {
    NodeHome home = getNodeHome();
    try {
      Node newNodeInstance = home.findByPrimaryKey(pk);
      return newNodeInstance;
    } catch (Exception re) {
      throw new NodeRuntimeException("NodeBmEJB.findNode()", SilverpeasRuntimeException.ERROR,
          "node.NODE_UNFINDABLE", "nodeId = " + pk.getId(), re);
    }
  }

  private Node findNodeByNameAndFatherId(NodePK pk, String name, int nodeFatherId) {
    NodeHome home = getNodeHome();
    try {
      Node newNodeInstance = home.findByNameAndFatherId(pk, name, nodeFatherId);
      return newNodeInstance;
    } catch (Exception re) {
      throw new NodeRuntimeException("NodeBmEJB.findNodeByNameAndNodeFatherId()",
          SilverpeasRuntimeException.ERROR, "node.NODE_UNFINDABLE", "nodeId = "
          + pk.getId() + ",name=" + name + ",nodeFatherID=" + nodeFatherId, re);
    }
  }

  /**
   * Get the attributes of a node and of its children
   *
   * @return a NodeDetail
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @since 1.0
   */
  @Override
  public NodeDetail getDetail(NodePK pk) throws RemoteException {
    return getDetail(pk, null);
  }

  @Override
  public NodeDetail getDetail(NodePK pk, String sorting) throws RemoteException {
    Node node = findNode(pk);

    try {
      NodeDetail nodeDetail = node.getDetail(sorting);

      // Add default translation
      Translation nodeI18NDetail =
          new NodeI18NDetail(nodeDetail.getLanguage(), nodeDetail.getName(), nodeDetail.
          getDescription());
      nodeDetail.addTranslation((Translation) nodeI18NDetail);
      List<Translation> translations = getTranslations(Integer.parseInt(pk.getId()));
      for (int t = 0; translations != null && t < translations.size(); t++) {
        nodeI18NDetail = translations.get(t);
        nodeDetail.addTranslation(nodeI18NDetail);
      }
      return nodeDetail;
    } catch (Exception re) {
      throw new NodeRuntimeException("NodeBmEJB.getDetail()",
          SilverpeasRuntimeException.ERROR, "node.GETTING_NODE_DETAIL_FAILED",
          "nodeId = " + pk.getId(), re);
    }

  }

  @Override
  public NodeDetail getDetailByNameAndFatherId(NodePK pk, String name, int nodeFatherId) throws
      RemoteException {
    Node node = findNodeByNameAndFatherId(pk, name, nodeFatherId);
    try {
      NodeDetail nodeDetail = node.getDetail(null);
      return nodeDetail;
    } catch (Exception re) {
      throw new NodeRuntimeException("NodeBmEJB.getDetailByNameAndNodeFatherId()",
          SilverpeasRuntimeException.ERROR, "node.GETTING_NODE_DETAIL_FAILED",
          "nodeId = " + pk.getId() + ",name=" + name + "nodeFatherId=" + nodeFatherId, re);
    }
  }

  /**
   * Get Translations of the node
   *
   * @param nodeId
   * @return List of translations
   */
  private List<Translation> getTranslations(int nodeId) throws RemoteException {
    Connection con = DBUtil.makeConnection(dbName);
    try {
      return NodeI18NDAO.getTranslations(con, nodeId);
    } catch (Exception re) {
      throw new NodeRuntimeException("NodeBmEJB.getTranslations()",
          SilverpeasRuntimeException.ERROR, "node.GETTING_TRANSLATIONS_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public ArrayList<NodeDetail> getTree(NodePK pk) throws RemoteException {
    Connection con = DBUtil.makeConnection(dbName);
    try {
      return NodeDAO.getTree(con, pk);
    } catch (Exception re) {
      throw new NodeRuntimeException("NodeBmEJB.getTree()",
          SilverpeasRuntimeException.ERROR, "node.GETTING_TREE_FAILED", re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public ArrayList<NodeDetail> getSubTree(NodePK pk) throws RemoteException {
    SilverTrace.info("node", "NodeBmEJB.getSubTree()", "root.MSG_GEN_ENTER_METHOD", "pk = " + pk);
    return getSubTree(pk, null, 0, null);
  }

  @Override
  public ArrayList<NodeDetail> getSubTree(NodePK pk, String sorting) throws RemoteException {
    SilverTrace.info("node", "NodeBmEJB.getSubTree()", "root.MSG_GEN_ENTER_METHOD",
        "pk = " + pk + " sorting=" + sorting);
    return getSubTree(pk, null, 0, sorting);
  }

  @Override
  public ArrayList<NodeDetail> getSubTreeByStatus(NodePK pk, String status) throws RemoteException {
    SilverTrace.info("node", "NodeBmEJB.getSubTreeByStatus()", "root.MSG_GEN_ENTER_METHOD",
        "pk = " + pk + ", status = " + status);
    return getSubTree(pk, status, 0, null);
  }

  @Override
  public ArrayList<NodeDetail> getSubTreeByStatus(NodePK pk, String status, String sorting)
      throws RemoteException {
    SilverTrace.info("node", "NodeBmEJB.getSubTreeByStatus()", "root.MSG_GEN_ENTER_METHOD",
        "pk = " + pk + ", status = " + status + ", sorting=" + sorting);
    return getSubTree(pk, status, 0, sorting);
  }

  @Override
  public ArrayList<NodeDetail> getSubTreeByLevel(NodePK pk, int level) throws RemoteException {
    SilverTrace.info("node", "NodeBmEJB.getSubTreeByStatus()", "root.MSG_GEN_ENTER_METHOD",
        "pk = " + pk + ", level = " + level);
    return getSubTree(pk, null, level, null);
  }

  @Override
  public ArrayList<NodeDetail> getSubTreeByLevel(NodePK pk, int level, String sorting)
      throws RemoteException {
    SilverTrace.info("node", "NodeBmEJB.getSubTreeByStatus()", "root.MSG_GEN_ENTER_METHOD",
        "pk = " + pk + ", level = " + level + ", sorting=" + sorting);
    return getSubTree(pk, null, level, sorting);
  }

  @Override
  public ArrayList<NodeDetail> getSubTree(NodePK pk, String status, int level, String sorting)
      throws RemoteException {
    SilverTrace.info("node", "NodeBmEJB.getSubTreeByStatus()", "root.MSG_GEN_ENTER_METHOD",
        "pk = " + pk + ", status = " + status + ", level = " + level + ", sorting=" + sorting);
    Connection con = DBUtil.makeConnection(dbName);
    ArrayList<NodeDetail> result = null;
    try {
      List<NodeDetail> headers = NodeDAO.getAllHeaders(con, pk, sorting, level);
      NodeDetail root = NodeDAO.loadRow(con, pk);
      root.setChildrenDetails(new ArrayList<NodeDetail>());
      Map<String, NodeDetail> tree = new HashMap<String, NodeDetail>();
      tree.put(root.getNodePK().getId(), root);
      for(NodeDetail header : headers) {
        header.setChildrenDetails(new ArrayList<NodeDetail>());
        tree.put(header.getNodePK().getId(), header);
      }

     for(NodeDetail header : headers) {
        NodeDetail father = tree.get(header.getFatherPK().getId());
        if (father != null) {
          father.getChildrenDetails().add(header);
        }
      }
      result = new ArrayList<NodeDetail>();
      if (level == 0) {
        root = tree.get(root.getNodePK().getId());
        result = (ArrayList<NodeDetail>) processNode(result, root);
      } else {
        for(NodeDetail header : headers) {
          result.add(header);
        }
      }
      return result;
    } catch (Exception re) {
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
      Iterator<NodeDetail> it = children.iterator();
      NodeDetail child = null;
      while (it.hasNext()) {
        child = it.next();
        processNode(result, child);
      }
    }

    return result;
  }

  @Override
  public void moveNode(NodePK nodePK, NodePK toNode) throws RemoteException {
    NodeDetail root = getDetail(toNode);
    String newRootPath = root.getPath() + toNode.getId() + "/";

    int deltaLevel = 0;

    String oldRootPath = null;

    Connection con = DBUtil.makeConnection(dbName);
    try {
      List<NodeDetail> tree = getSubTree(nodePK);
      for (int t = 0; t < tree.size(); t++) {
        NodeDetail node = tree.get(t);
        deleteIndex(node.getNodePK());
        if (t == 0) {
          oldRootPath = node.getPath();
          node.setFatherPK(toNode);
          if (node.getLevel() > root.getLevel()) {
            deltaLevel = root.getLevel() - node.getLevel();
          } else {
            deltaLevel = node.getLevel() - root.getLevel();
          }
          deltaLevel++;
          node.setOrder(root.getChildrenNumber());
        }
        // remove node
        Node nodeEJB = findNode(node.getNodePK());
        nodeEJB.remove();

        // change data
        String newPath = node.getPath().replaceAll(oldRootPath, newRootPath);
        node.setPath(newPath);
        node.setLevel(node.getLevel() + deltaLevel);
        node.getNodePK().setComponentName(toNode.getInstanceId());
        node.setRightsDependsOn(root.getRightsDependsOn());
        node.setUseId(true);
        Node newNode = getNodeHome().create(node);
        NodeDetail newND = newNode.getDetail();
        createIndex(newND, true);
      }

      NodeDAO.unvalidateTree(con, nodePK);
      NodeDAO.unvalidateTree(con, toNode);
    } catch (Exception e) {
      throw new NodeRuntimeException("NodeBmEJB.moveNode()",
          SilverpeasRuntimeException.ERROR, "node.MOVING_SUBTREE_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param pk
   * @return
   * @throws RemoteException
   * @see
   */
  @Override
  public NodeDetail getFrequentlyAskedDetail(NodePK pk) throws RemoteException {
    return getDetail(pk);
  }

  /**
   * Method declaration
   *
   * @param pk
   * @return
   * @throws RemoteException
   * @see
   */
  @Override
  public NodeDetail getTwoLevelDetails(NodePK pk) throws RemoteException {
    NodeDetail nd = getDetail(pk);
    Connection con = DBUtil.makeConnection(dbName);

    try {
      Collection<NodeDetail> children = NodeDAO.getChildrenDetails(con, pk);
      Iterator<NodeDetail> i = children.iterator();
      ArrayList<NodeDetail> childrenDetail = new ArrayList<NodeDetail>();

      while (i.hasNext()) {
        NodeDetail childDetail = i.next();
        Collection<NodeDetail> subChildren =
            NodeDAO.getChildrenDetails(con, childDetail.getNodePK());
        Iterator<NodeDetail> j = subChildren.iterator();
        ArrayList<NodeDetail> subChildrenDetail = new ArrayList<NodeDetail>();

        while (j.hasNext()) {
          NodeDetail subChild = j.next();

          subChildrenDetail.add(subChild);
        }
        childDetail.setChildrenDetails(subChildrenDetail);
        childrenDetail.add(childDetail);
      }
      nd.setChildrenDetails(childrenDetail);
      return nd;
    } catch (Exception re) {
      throw new NodeRuntimeException("NodeBmEJB.getTwoLevelDetails()",
          SilverpeasRuntimeException.ERROR, "node.GETTING_NODE_DETAIL_FAILED",
          "nodeId = " + pk.getId(), re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public NodeDetail getHeader(NodePK pk, boolean getTranslations) throws RemoteException {
    Connection con = DBUtil.makeConnection(dbName);
    try {
      return NodeDAO.loadRow(con, pk, getTranslations);
    } catch (Exception re) {
      throw new NodeRuntimeException("NodeBmEJB.getHeader()",
          SilverpeasRuntimeException.ERROR, "node.GETTING_NODE_HEADER_FAILED",
          "nodeId = " + pk.getId(), re);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Get the attributes of THIS node
   *
   * @return a NodeDetail
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @since 1.0
   */
  @Override
  public NodeDetail getHeader(NodePK pk) throws RemoteException {
    Connection con = DBUtil.makeConnection(dbName);
    try {
      return NodeDAO.loadRow(con, pk);
    } catch (Exception re) {
      throw new NodeRuntimeException("NodeBmEJB.getHeader()",
          SilverpeasRuntimeException.ERROR, "node.GETTING_NODE_HEADER_FAILED",
          "nodeId = " + pk.getId(), re);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Update the attributes of the node
   *
   * @param nd the NodeDetail which contains updated data
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @since 1.0
   */
  @Override
  public void setDetail(NodeDetail nd) throws RemoteException {
    Node node = findNode(nd.getNodePK());
    NodeDetail oldNodeDetail = getHeader(nd.getNodePK());
    Connection con = DBUtil.makeConnection(dbName);
    try {
      // I18N
      if (nd.isRemoveTranslation()) {
        // Remove of a translation is required
        if ("-1".equals(nd.getTranslationId())) {
          // Default language = translation
          List<Translation> translations = NodeI18NDAO.getTranslations(con, nd.getId());

          if (translations != null && translations.size() > 0) {
            NodeI18NDetail translation = (NodeI18NDetail) translations.get(0);

            nd.setLanguage(translation.getLanguage());
            nd.setName(translation.getName());
            nd.setDescription(translation.getDescription());
            NodeI18NDAO.removeTranslation(con, translation.getId());
            node.setDetail(nd);
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
            NodeI18NDetail translation = new NodeI18NDetail(nd.getLanguage(),
                nd.getName(), nd.getDescription());
            translation.setNodeId(new Integer(nd.getId()).toString());

            String translationId = nd.getTranslationId();
            if (translationId != null && !translationId.equals("-1")) {
              // update translation
              translation.setId(Integer.parseInt(translationId));
              translation.setNodeId(new Integer(nd.getId()).toString());
              NodeI18NDAO.updateTranslation(con, translation);
            } else {
              NodeI18NDAO.saveTranslation(con, translation);
            }
            NodeDAO.unvalidateTree(con, nd.getNodePK());
          } else {
            // the default language is modified
            node.setDetail(nd);
          }
        } else {
          // No i18n managed by this object
          node.setDetail(nd);
        }
      }
      // createIndex(nd);
      createIndex(nd.getNodePK());
    } catch (Exception re) {
      throw new NodeRuntimeException("NodeBmEJB.setDetail()",
          SilverpeasRuntimeException.ERROR, "node.UPDATING_NODE_FAILED",
          "nodeId = " + nd.getNodePK().getId(), re);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Remove a node and its descendants
   *
   * @param pk the node PK to delete
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @since 1.0
   */
  @Override
  public void removeNode(NodePK pk) throws RemoteException {
    Connection connection = null;
    try {
      connection = DBUtil.makeConnection(dbName);
      NodeDeletion.deleteNodes(pk, connection, new AnonymousMethodOnNode() {
        @Override
        public void invoke(NodePK pk) throws Exception {
          // remove wysiwyg attached to node
          WysiwygController.deleteWysiwygAttachments(null, pk.getInstanceId(),
              "Node_" + pk.getId());
        }
      });
    } catch (Exception re) {
      throw new NodeRuntimeException("NodeBmEJB.removeNode()",
          SilverpeasRuntimeException.ERROR, "node.DELETING_NODE_FAILED",
          "nodeId = " + pk.getId(), re);
    } finally {
      DBUtil.close(connection);
    }
  }

  /**
   * Get the path of this node from this node to root
   *
   * @param pk The PK of the node
   * @return a NodeDetail Collection (only header)
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @see java.util.Collection
   * @since 1.0
   */
  @Override
  public Collection<NodeDetail> getPath(NodePK pk) throws RemoteException {
    return getAnotherPath(pk);
  }

  /**
   * Get the header of each child of the node
   *
   * @return a NodeDetail collection
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @since 1.0
   */
  @Override
  public Collection<NodeDetail> getChildrenDetails(NodePK pk) throws RemoteException {
    Connection con = DBUtil.makeConnection(dbName);
    try {
      return NodeDAO.getChildrenDetails(con, pk);
    } catch (Exception re) {
      throw new NodeRuntimeException("NodeBmEJB.getChildrenDetails()",
          SilverpeasRuntimeException.ERROR, "node.GETTING_NODE_SONS_FAILED",
          "nodeId = " + pk.getId(), re);
    } finally {
      DBUtil.close(con);
    }
  }
  
  /**
   * Get the header of each child of the node, order by sorting
   *
   * @return a NodeDetail collection
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @since 1.0
   */
  @Override
  public Collection<NodeDetail> getChildrenDetails(NodePK pk, String sorting) throws RemoteException {
    Connection con = DBUtil.makeConnection(dbName);
    try {
      return NodeDAO.getChildrenDetails(con, pk, sorting);
    } catch (Exception re) {
      throw new NodeRuntimeException("NodeBmEJB.getChildrenDetails()",
          SilverpeasRuntimeException.ERROR, "node.GETTING_NODE_SONS_FAILED",
          "nodeId = " + pk.getId(), re);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Get the header of each child of the node this method is to be used on frequently asked nodes
   * (next to the root), because all ejb will be instanciated
   *
   * @return a NodeDetail collection
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @since 1.0
   */
  @Override
  public Collection<NodeDetail> getFrequentlyAskedChildrenDetails(NodePK pk) throws RemoteException {
    return getChildrenDetails(pk);
  }

  /**
   * Method declaration
   *
   * @param pk
   * @param level
   * @return
   * @throws RemoteException
   * @see
   */
  @Override
  public List<NodeDetail> getHeadersByLevel(NodePK pk, int level) throws RemoteException {
    Connection con = DBUtil.makeConnection(dbName);
    try {
      return NodeDAO.getHeadersByLevel(con, pk, level);
    } catch (Exception re) {
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
   * @throws RemoteException
   * @see
   */
  @Override
  public Collection<NodeDetail> getAllNodes(NodePK nodePK) throws RemoteException {
    Connection con = DBUtil.makeConnection(dbName);
    try {
      return NodeDAO.getAllHeaders(con, nodePK);
    } catch (Exception re) {
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
  public int getChildrenNumber(NodePK pk) throws RemoteException {
    Connection con = DBUtil.makeConnection(dbName);
    try {
      return NodeDAO.getChildrenNumber(con, pk);
    } catch (Exception re) {
      throw new NodeRuntimeException("NodeBmEJB.getChildrenNumber()",
          SilverpeasRuntimeException.ERROR,
          "node.GETTING_NUMBER_OF_SONS_FAILED", "nodeId = " + pk.getId(), re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public NodePK createNode(NodeDetail node) throws RemoteException {
    NodePK parentPK = node.getFatherPK();
    NodeDetail parent = getHeader(parentPK);
    node.setPath(parent.getFullPath());
    node.setLevel(parent.getLevel() + 1);
    node.setFatherPK(parentPK);
    if (node.getLanguage() == null) {
      // translation for the first time
      node.setLanguage(I18NHelper.defaultLanguage);
    }
    try {
      NodeDetail newNode = getNodeHome().create(node).getDetail();
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
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @since 1.0
   */
  @Override
  public NodePK createNode(NodeDetail nd, NodeDetail fatherDetail) throws RemoteException {
    Node newNode = null;
    NodeHome home = getNodeHome();
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
      newNode = home.create(nd);
      NodeDetail newND = newNode.getDetail();

      createIndex(newND, false);
      return newND.getNodePK();
    } catch (Exception re) {
      throw new NodeRuntimeException("NodeBmEJB.createNode()",
          SilverpeasRuntimeException.ERROR, "node.CREATING_NODE_FAILED", re);
    }
  }

  /**
   * On node creation, check if another node have got the same name with same father
   *
   * @param nd A NodeDetail contains new node data to compare
   * @return true if there is already a node with same name with same father false else
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @since 1.0
   */
  @Override
  public boolean isSameNameSameLevelOnCreation(NodeDetail nd) throws RemoteException {
    Connection con = DBUtil.makeConnection(dbName);
    try {
      boolean result = NodeDAO.isSameNameSameLevelOnCreation(con, nd);
      return result;
    } catch (Exception re) {
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
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @since 1.0
   */
  @Override
  public boolean isSameNameSameLevelOnUpdate(NodeDetail nd) throws RemoteException {
    Connection con = DBUtil.makeConnection(dbName);
    try {
      return NodeDAO.isSameNameSameLevelOnUpdate(con, nd);
    } catch (Exception re) {
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
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @since 1.0
   */
  @Override
  public Collection<NodePK> getChildrenPKs(NodePK nodePK) throws RemoteException {
    Connection con = DBUtil.makeConnection(dbName);
    try {
      return NodeDAO.getChildrenPKs(con, nodePK);
    } catch (Exception re) {
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
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @since 1.0
   */
  @Override
  public Collection<NodePK> getDescendantPKs(NodePK nodePK) throws RemoteException {
    Connection con = DBUtil.makeConnection(dbName);
    try {
      return NodeDAO.getDescendantPKs(con, nodePK);
    } catch (Exception re) {
      throw new NodeRuntimeException("NodeBmEJB.getDescendantPKs()",
          SilverpeasRuntimeException.ERROR,
          "node.GETTING_PK_OF_DESCENDANTS_FAILED",
          "nodeId = " + nodePK.getId(), re);
    } finally {
      DBUtil.close(con);
    }

  }

  /**
   * Get descendant node details of a node
   *
   * @param nodePK A NodePK
   * @return A List of NodeDetail
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @since 1.0
   */
  @Override
  public List<NodeDetail> getDescendantDetails(NodePK nodePK) throws RemoteException {
    Connection con = DBUtil.makeConnection(dbName);
    try {
      return NodeDAO.getDescendantDetails(con, nodePK);
    } catch (Exception re) {
      throw new NodeRuntimeException("NodeBmEJB.getDescendantDetails()",
          SilverpeasRuntimeException.ERROR,
          "node.GETTING_DETAIL_OF_DESCENDANTS_FAILED", "nodeId = "
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
  public List<NodeDetail> getDescendantDetails(NodeDetail node) throws RemoteException {
    Connection con = DBUtil.makeConnection(dbName);
    try {
      return NodeDAO.getDescendantDetails(con, node);
    } catch (Exception re) {
      throw new NodeRuntimeException("NodeBmEJB.getDescendantDetails()",
          SilverpeasRuntimeException.ERROR,
          "node.GETTING_DETAIL_OF_DESCENDANTS_FAILED", "nodeId = "
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
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @since 1.0
   * @deprecated
   */
  @Override
  public Collection<NodeDetail> getAnotherPath(NodePK nodePK) throws RemoteException {
    // TODO : methode a supprimer ! il faut utiliser getPath()
    Connection con = DBUtil.makeConnection(dbName);
    try {
      return NodeDAO.getAnotherPath(con, nodePK);
    } catch (Exception re) {
      throw new NodeRuntimeException("NodeBmEJB.getAnotherPath()",
          SilverpeasRuntimeException.ERROR, "node.GETTING_NODE_PATH_FAILED",
          "nodeId = " + nodePK.getId(), re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void processWysiwyg(NodePK nodePK) throws RemoteException {
    NodeDetail nodeDetail = getHeader(nodePK);
    createIndex(nodeDetail);
  }

  @Override
  public void updateRightsDependency(NodeDetail nodeDetail) throws RemoteException {
    Node node = findNode(nodeDetail.getNodePK());
    node.setRightsDependsOn(nodeDetail.getRightsDependsOn());
    try {
      spreadRightsDependency(node, nodeDetail.getRightsDependsOn());
    } catch (Exception e) {
      throw new NodeRuntimeException("NodeBmEJB.updateRightsDependency()",
          SilverpeasRuntimeException.ERROR, "node.SPREADING_RIGHTS_DEPENDENCY_FAILED", "nodeId = "
          + nodeDetail.getNodePK().getId(), e);
    }
  }

  private void spreadRightsDependency(Node currentNode, int rightsDependsOn) throws
      RemoteException, SQLException {
    Collection<NodeDetail> children = currentNode.getChildrenDetails();
    for (NodeDetail child : children) {
      Node node = findNode(child.getNodePK());
      if (!child.haveLocalRights()) {
        node.setRightsDependsOn(rightsDependsOn);
        spreadRightsDependency(node, rightsDependsOn);
      }
    }
  }

  @Override
  public void sortNodes(List<NodePK> nodePKs) throws RemoteException {
    Connection con = DBUtil.makeConnection(dbName);
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
  public void createIndex(NodeDetail nodeDetail) throws RemoteException {
    createIndex(nodeDetail, true);
  }

  private void createIndex(NodePK pk) throws RemoteException {
    NodeDetail node = getDetail(pk);
    createIndex(node);
  }

  private void createIndex(NodeDetail nodeDetail, boolean processWysiwygContent)
      throws RemoteException {
    SilverTrace.info("node", "NodeBmEJB.createIndex()", "root.MSG_GEN_ENTER_METHOD",
        "nodeDetail = " + nodeDetail);
    FullIndexEntry indexEntry = null;

    if (nodeDetail != null) {
      // Index the Node
      indexEntry =
          new FullIndexEntry(nodeDetail.getNodePK().getComponentName(), "Node", nodeDetail.
          getNodePK().getId());

      Iterator<String> languages = nodeDetail.getLanguages();
      while (languages.hasNext()) {
        String language = languages.next();
        NodeI18NDetail translation = (NodeI18NDetail) nodeDetail.getTranslation(language);

        indexEntry.setTitle(translation.getName(), language);
        indexEntry.setPreview(translation.getDescription(), language);
      }

      indexEntry.setCreationDate(nodeDetail.getCreationDate());
      String userId = null;

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
          UserDetail ud = AdminReference.getAdminService().getUserDetail(userId);
          if (ud != null) {
            indexEntry.addTextContent(ud.getDisplayedName());
          }
        } catch (AdminException e) {
          // do not index on user name
        }
      }

      if (processWysiwygContent) {
        indexEntry = updateIndexEntryWithWysiwygContent(indexEntry, nodeDetail.getNodePK());
      }
    }
    IndexEngineProxy.addIndexEntry(indexEntry);
  }

  private FullIndexEntry updateIndexEntryWithWysiwygContent(FullIndexEntry indexEntry,
      NodePK nodePK) {
    SilverTrace.info("node", "NodeBmEJB.updateIndexEntryWithWysiwygContent()",
        "root.MSG_GEN_ENTER_METHOD", "indexEntry = " + indexEntry.toString()
        + ", nodePK = " + nodePK.toString());
    try {
      if (nodePK != null) {
        String wysiwygContent = WysiwygController.loadFileAndAttachment(nodePK.getSpace(), nodePK.
            getComponentName(), "Node_" + nodePK.getId());
        if (wysiwygContent != null) {
          indexEntry.addTextContent(wysiwygContent);
        }
      }
    } catch (Exception e) {
      // No wysiwyg associated
    }
    return indexEntry;
  }

  /**
   * Called on : - removeNode()
   */
  @Override
  public void deleteIndex(NodePK pk) {
    SilverTrace.info("node", "NodeBmEJB.deleteIndex()", "root.MSG_GEN_ENTER_METHOD", "pk = " + pk);
    IndexEntryPK indexEntry = new IndexEntryPK(pk.getComponentName(), "Node", pk.getId());
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  /**
   * Constructor declaration
   *
   * @see
   */
  public NodeBmEJB() {
  }

  /**
   * Method declaration
   *
   * @throws CreateException
   * @see
   */
  public void ejbCreate() throws CreateException {
  }

  /**
   * Method declaration
   *
   * @see
   */
  @Override
  public void ejbRemove() {
  }

  /**
   * Method declaration
   *
   * @see
   */
  @Override
  public void ejbActivate() {
  }

  /**
   * Method declaration
   *
   * @see
   */
  @Override
  public void ejbPassivate() {
  }

  /**
   * Method declaration
   *
   * @param sc
   * @see
   */
  @Override
  public void setSessionContext(SessionContext sc) {
  }
}
