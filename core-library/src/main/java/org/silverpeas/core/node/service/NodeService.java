/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.node.service;

import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.model.NodePath;
import org.silverpeas.core.util.ServiceProvider;

import java.util.Collection;
import java.util.List;

/**
 * Service working on a nodes in a graph.
 *
 * @author Nicolas Eysseric
 */
public interface NodeService {

  static NodeService get() {
    return ServiceProvider.getSingleton(NodeService.class);
  }

  /**
   * Gets synthetic details about the specified node without any translations.
   *
   * @param pk the node primary key.
   * @return a {@link NodeDetail} instance.
   */
  NodeDetail getHeader(NodePK pk);

  /**
   * Gets synthetic details about the specified node.
   *
   * @param pk the node primary key.
   * @param getTranslations a flag indicating if the translation has to be retrieved.
   * @return a {@link NodeDetail} instance.
   */
  NodeDetail getHeader(NodePK pk, boolean getTranslations);

  /**
   * Gets complete details about the specified node.
   *
   * @param pk the node primary key.
   * @return a {@link NodeDetail} instance.
   */
  NodeDetail getDetail(NodePK pk);

  /**
   * Gets complete details about the specified node with the given name and with as father the
   * specified one.
   *
   * @param pk the node primary key.
   * @param name the name of the node.
   * @param nodeFatherId the local unique identifier of the father of the asked node.
   * @return a {@link NodeDetail} instance.
   */
  NodeDetail getDetailByNameAndFatherId(NodePK pk, String name, int nodeFatherId);

  /**
   * Gets all the nodes that are part of the tree rooted from the specified node.
   * @param pk the unique identifier of the root node of the tree.
   * @return a list of nodes.
   */
  List<NodeDetail> getTree(NodePK pk);

  /**
   * Gets all the nodes that are part of the subtree from the specified node.
   * @param pk the unique identifier of the node from which the subtree should be returned.
   * @return a list of nodes.
   */
  List<NodeDetail> getSubTree(NodePK pk);

  /**
   * Gets all the nodes that are part of the subtree from the specified node and sorted according
   * to the sorting argument.
   * @param pk the unique identifier of the node from which the subtree should be returned.
   * @param sorting a coma-separated list of node's attributes from which the returned list should
   * be sorted.
   * @return a list of nodes.
   */
  List<NodeDetail> getSubTree(NodePK pk, String sorting);

  /**
   * Gets all the nodes that are part of the subtree from the specified node and having the given
   * status.
   * @param pk the unique identifier of the node from which the subtree should be returned.
   * @param status the status of the nodes to return.
   * @return a list of nodes.
   */
  List<NodeDetail> getSubTreeByStatus(NodePK pk, String status);

  /**
   * Gets all the nodes that are part of the subtree from the specified node and having the given
   * status.
   * @param pk the unique identifier of the node from which the subtree should be returned.
   * @param status the status of the nodes to return.
   * @param sorting a coma-separated list of node's attributes from which the returned list should
   * be sorted.
   * @return a list of nodes.
   */
  List<NodeDetail> getSubTreeByStatus(NodePK pk, String status, String sorting);

  /**
   * Gets all the nodes that are part of the subtree from the specified node down to the given deep
   * level of the subtree.
   * @param pk the unique identifier of the node from which the subtree should be returned.
   * @param level the level of the subtree to get.
   * @return a list of nodes from the specified node down to the given level of the subtree.
   */
  List<NodeDetail> getSubTreeByLevel(NodePK pk, int level);

  /**
   * Gets all the nodes that are part of the subtree from the specified node down to the given deep
   * level of the subtree.
   * @param pk the unique identifier of the node from which the subtree should be returned.
   * @param level the level of the subtree to get.
   * @param sorting a coma-separated list of node's attributes from which the returned list should
   * be sorted.
   * @return a list of nodes from the specified node down to the given level of the subtree.
   */
  List<NodeDetail> getSubTreeByLevel(NodePK pk, int level, String sorting);

  /**
   * Gets all the nodes that are part of the subtree from the specified node down to the given deep
   * level of the subtree and having the given status.
   * @param pk the unique identifier of the node from which the subtree should be returned.
   * @param status the status of the nodes to return.
   * @param level the level of the subtree to get.
   * @param sorting a coma-separated list of node's attributes from which the returned list should
   * be sorted.
   * @return a list of nodes.
   */
  List<NodeDetail> getSubTree(NodePK pk, String status, int level, String sorting);

  /**
   * Gets the synthetic details of all the nodes that are managed by the same component instance
   * that the specified node down to the deep level of the tree of nodes to return.
   * @param pk the unique identifier of a node.
   * @param level the level of the subtree to get.
   * @return a list of all nodes of a given component instance.
   */
  List<NodeDetail> getHeadersByLevel(NodePK pk, int level);

  /**
   * Gets all the nodes that are part of the tree of nodes managed by the same component instance
   * that the specified node.
   * @param nodePK the unique identifier of a node.
   * @return all the nodes of the component instance referred by the specified node.
   */
  Collection<NodeDetail> getAllNodes(NodePK nodePK);

  /**
   * Selects massively simple data about nodes.
   * <p>
   * For now, only the following data are retrieved:
   *   <ul>
   *     <li>nodeId</li>
   *     <li>instanceId</li>
   *     <li>rightsDependsOn</li>
   *   </ul>
   *   This method is designed for process performance needs.
   * </p>
   * @param instanceIds the instance ids aimed.
   * @return a list of {@link NodeDetail} instances.
   */
  List<NodeDetail> getMinimalDataByInstances(final Collection<String> instanceIds);

  /**
   * Gets the path of the specified node from the root one.
   * @param pk the unique identifier of the node in the data source.
   * @return the path of the node as a {@link NodePath} instance.
   */
  NodePath getPath(NodePK pk);

  /**
   * Gets the details of all of the children of the specified node.
   * @param pk the node primary key.
   * @return a collection of nodes.
   */
  Collection<NodeDetail> getChildrenDetails(NodePK pk);

  /**
   * Updates the specified node.
   * @param nodeDetail the details of the node with which it will be updated.
   */
  void setDetail(NodeDetail nodeDetail);

  /**
   * Creates a new node in Silverpeas.
   * @param nodeDetail the details of the node to save.
   * @param fatherDetail the parent of the node to save.
   * @return the unique identifier of the new node.
   */
  NodePK createNode(NodeDetail nodeDetail, NodeDetail fatherDetail);

  /**
   * Creates a new node in Silverpeas.
   * @param nodeDetail the details of the node to save.
   * @return the unique identifier of the new node.
   */
  NodePK createNode(NodeDetail nodeDetail);

  /**
   * Removes a node and its descendants
   * @param pk the unique identifier of the node to remove.
   * @see NodePK
   */
  void removeNode(NodePK pk);

  /**
   * Moves the specified node to the given another one that then will become its new father.
   * @param nodePK the unique identifier of the node to move.
   * @param toNode the unique identifier of the new father of the node.
   */
  void moveNode(NodePK nodePK, NodePK toNode);

  /**
   * Moves the specified node to the given another one that then will become its new father.
   * @param nodePK the unique identifier of the node to move.
   * @param toNode the unique identifier of the new father of the node.
   * @param preserveRights indicates if specific rights must be maintained or not
   */
  void moveNode(NodePK nodePK, NodePK toNode, boolean preserveRights);

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
   * Gets all the identifier of the children of the specified node.
   * @param nodePK the unique identifier of the father node.
   * @return A collection of {@link NodePK} instances.
   * @see NodePK
   */
  Collection<NodePK> getChildrenPKs(NodePK nodePK);

  /**
   * Gets the identifiers of all of the descendants of the specified node.
   * @param nodePK the unique identifier of a node.
   * @return A collection of {@link NodePK} instances.
   * @return A collection of NodePK
   * @see NodePK
   */
  Collection<NodePK> getDescendantPKs(NodePK nodePK);

  /**
   * Gets all the descendants of the specified node.
   * @param nodePK the unique identifier of a node.
   * @return A collection of {@link NodePK} instances.
   * @return A collection of NodePK
   * @see NodeDetail
   */
  List<NodeDetail> getDescendantDetails(NodePK nodePK);

  /**
   * Gets all the descendants of the specified node.
   * @param node a node.
   * @return A collection of {@link NodePK} instances.
   * @return A collection of NodePK
   * @see NodeDetail
   */
  List<NodeDetail> getDescendantDetails(NodeDetail node);

  /**
   * A wysiwyg's content has been added or modified to a node. Its content must be added to the
   * indexed content of the node
   *
   * @param nodePK the identifier of the node associated to the wysiwyg
   */
  void processWysiwyg(NodePK nodePK);

  /**
   * Updates the dependency on the access rights of the specified node.
   * @param nodeDetail a node.
   */
  void updateRightsDependency(NodeDetail nodeDetail);

  /**
   * Indexes the specified node.
   * @param nodeDetail a node.
   */
  void createIndex(NodeDetail nodeDetail);

  /**
   * Unindexes the specified node.
   * @param pk the unique identifier of the node.
   */
  void deleteIndex(NodePK pk);

  /**
   * Sorts the specified nodes in Silverpeas.
   * @param nodePKs a list of the unique identifiers of the nodes to sort.
   */
  void sortNodes(List<NodePK> nodePKs);
}
