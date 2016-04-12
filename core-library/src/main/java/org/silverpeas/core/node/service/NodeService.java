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

import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.util.ServiceProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is the Node BM interface.
 *
 * @author Nicolas Eysseric
 */
public interface NodeService {

  static NodeService get() {
    return ServiceProvider.getService(NodeService.class);
  }

  /**
   * Get the attributes of THIS node
   *
   * @param pk the node primary key.
   * @return a NodeDetail
   */
  NodeDetail getHeader(NodePK pk);

  NodeDetail getHeader(NodePK pk, boolean getTranslations);

  /**
   * Get the attributes of a node and of its children
   *
   * @param pk the node primary key.
   * @return a NodeDetail
   */
  NodeDetail getDetail(NodePK pk);

  NodeDetail getDetailTransactionally(NodePK pk);

  NodeDetail getDetailByNameAndFatherId(NodePK pk, String name, int nodeFatherId);

  ArrayList<NodeDetail> getTree(NodePK pk);

  ArrayList<NodeDetail> getSubTree(NodePK pk);

  ArrayList<NodeDetail> getSubTree(NodePK pk, String sorting);

  ArrayList<NodeDetail> getSubTreeByStatus(NodePK pk, String status);

  ArrayList<NodeDetail> getSubTreeByStatus(NodePK pk, String status, String sorting);

  ArrayList<NodeDetail> getSubTreeByLevel(NodePK pk, int level);

  ArrayList<NodeDetail> getSubTreeByLevel(NodePK pk, int level, String sorting);

  ArrayList<NodeDetail> getSubTree(NodePK pk, String status, int level, String sorting);

  /**
   * Method declaration
   *
   * @param pk the node primary key.
   * @return
   */
  NodeDetail getTwoLevelDetails(NodePK pk);

  /**
   * Method declaration
   *
   * @param pk the node primary key.
   * @return
   */
  NodeDetail getFrequentlyAskedDetail(NodePK pk);

  /**
   * Method declaration
   *
   * @param pk the node primary key.
   * @param level
   * @return
   */
  List<NodeDetail> getHeadersByLevel(NodePK pk, int level);

  /**
   * Method declaration
   *
   * @param nodePK
   * @return
   */
  Collection<NodeDetail> getAllNodes(NodePK nodePK);

  /**
   * Get the path of this node from this node to root
   *
   * @param pk the node primary key.
   * @return a NodeDetail Collection (only header).
   */
  Collection<NodeDetail> getPath(NodePK pk);

  /**
   * Get the header of each child of the node.
   *
   * @param pk the node primary key.
   * @return a NodeDetail collection
   */
  Collection<NodeDetail> getChildrenDetails(NodePK pk);

  /**
   * Get the header of each child of the node this function is to be used with frequently used nodes
   * because for each child, an ejb will be instanciated (nodes next to the root will be frequently
   * used) For less used nodes, choose the getChildrenDetails() method
   *
   * @param pk the node primary key.
   * @return a NodeDetail collection
   */
  Collection<NodeDetail> getFrequentlyAskedChildrenDetails(NodePK pk);

  /**
   * Get the children number of this node
   *
   * @param pk the node primary key.
   * @return a int
   */
  int getChildrenNumber(NodePK pk);

  /**
   * Update the attributes of the node
   *
   * @param nodeDetail the NodeDetail which contains updated data
   * @
   * @since 1.0
   */
  void setDetail(NodeDetail nodeDetail);

  /**
   * Create a new Node object
   *
   * @param nodeDetail the NodeDetail which contains data
   * @param fatherDetail the parent of node to be added
   * @return the NodePK of the new Node
   */
  NodePK createNode(NodeDetail nodeDetail, NodeDetail fatherDetail);

  /**
   * Create a new Node object
   *
   * @param nodeDetail the NodeDetail which contains data
   * @return the NodePK of the new Node
   */
  NodePK createNode(NodeDetail nodeDetail);

  /**
   * Remove a node and its descendants
   *
   * @param pk the node PK to delete
   * @see NodePK
   */
  void removeNode(NodePK pk);

  void moveNode(NodePK nodePK, NodePK toNode);

  /**
   * On node creation, check if another node have got the same name with same father
   *
   * @return true if there is already a node with same name with same father false else
   * @param nd A NodeDetail contains new node data to compare
   */
  boolean isSameNameSameLevelOnCreation(NodeDetail nd);

  /**
   * On node update, check if another node have got the same name with same father
   *
   * @return true if there is already a node with same name with same father false else
   * @param nd A NodeDetail contains new node data to compare
   */
  boolean isSameNameSameLevelOnUpdate(NodeDetail nd);

  /**
   * Get children node PKs of a node
   *
   * @return A collection of NodePK
   * @param nodePK A NodePK
   * @see NodePK
   */
  Collection<NodePK> getChildrenPKs(NodePK nodePK);

  /**
   * Get descendant node PKs of a node
   *
   * @return A collection of NodePK
   * @param nodePK A NodePK
   * @see NodePK
   * @since 1.0
   */
  Collection<NodePK> getDescendantPKs(NodePK nodePK);

  /**
   * Get descendant nodeDetails of a node
   *
   * @return A List of NodeDetail
   * @param nodePK A NodePK
   * @see NodePK
   * @since 1.0
   */
  List<NodeDetail> getDescendantDetails(NodePK nodePK);

  /**
   * Get descendant nodeDetails of a node
   *
   * @return A List of NodeDetail
   * @param node A NodeDetail
   */
  List<NodeDetail> getDescendantDetails(NodeDetail node);

  /**
   * Get the path from root to a node
   *
   * @return A collection of NodeDetail
   * @param nodePK A NodePK
   * @see NodePK
   */
  Collection<NodeDetail> getAnotherPath(NodePK nodePK);

  /**
   * A wysiwyg's content has been added or modified to a node. Its content must be added to the
   * indexed content of the node
   *
   * @param nodePK the identifier of the node associated to the wysiwyg
   */
  void processWysiwyg(NodePK nodePK);

  void updateRightsDependency(NodeDetail nodeDetail);

  /**
   * Method declaration
   *
   * @param nodeDetail
   */
  void createIndex(NodeDetail nodeDetail);

  /**
   * Method declaration
   *
   * @param pk
   */
  void deleteIndex(NodePK pk);

  void sortNodes(List<NodePK> nodePKs);
}
