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

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.rmi.RemoteException;

import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.node.model.NodeDetail;

/**
 * This is the Node BM Business Skeleton interface.
 *
 * @author squere
 */
public interface NodeBmBusinessSkeleton {

  /**
   * Get the attributes of THIS node
   *
   * @param pk the node primary key.
   * @return a NodeDetail
   * @throws RemoteException    *
   * @since 1.0
   */
  public NodeDetail getHeader(NodePK pk) throws RemoteException;

  public NodeDetail getHeader(NodePK pk, boolean getTranslations)
      throws RemoteException;

  /**
   * Get the attributes of a node and of its children
   *
   * @param pk the node primary key.
   * @return a NodeDetail
   * @throws RemoteException    *
   * @since 1.0
   */
  public NodeDetail getDetail(NodePK pk) throws RemoteException;

  public NodeDetail getDetailByNameAndFatherId(NodePK pk, String name,
      int nodeFatherId) throws RemoteException;

  public ArrayList<NodeDetail> getTree(NodePK pk) throws RemoteException;

  public ArrayList<NodeDetail> getSubTree(NodePK pk) throws RemoteException;

  public ArrayList<NodeDetail> getSubTree(NodePK pk, String sorting) throws RemoteException;

  public ArrayList<NodeDetail> getSubTreeByStatus(NodePK pk, String status)
      throws RemoteException;

  public ArrayList<NodeDetail> getSubTreeByStatus(NodePK pk, String status, String sorting)
      throws RemoteException;

  public ArrayList<NodeDetail> getSubTreeByLevel(NodePK pk, int level)
      throws RemoteException;

  public ArrayList<NodeDetail> getSubTreeByLevel(NodePK pk, int level, String sorting)
      throws RemoteException;

  public ArrayList<NodeDetail> getSubTree(NodePK pk, String status, int level,
      String sorting) throws RemoteException;

  /**
   * Method declaration
   *
   * @param pk the node primary key.
   * @return
   * @throws RemoteException
   * @see
   */
  public NodeDetail getTwoLevelDetails(NodePK pk) throws RemoteException;

  /**
   * Method declaration
   *
   * @param pk the node primary key.
   * @return
   * @throws RemoteException
   * @see
   */
  public NodeDetail getFrequentlyAskedDetail(NodePK pk) throws RemoteException;

  /**
   * Method declaration
   *
   * @param pk the node primary key.
   * @param level
   * @return
   * @throws RemoteException
   * @see
   */
  public List<NodeDetail> getHeadersByLevel(NodePK pk, int level) throws RemoteException;

  /**
   * Method declaration
   *
   * @param nodePK
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection<NodeDetail> getAllNodes(NodePK nodePK) throws RemoteException;

  /**
   * Get the path of this node from this node to root
   *
   * @param pk the node primary key.
   * @return a NodeDetail Collection (only header).
   * @throws RemoteException
   * @since 1.0
   */
  public Collection<NodeDetail> getPath(NodePK pk) throws RemoteException;

  /**
   * Get the header of each child of the node.
   *
   * @param pk the node primary key.
   * @return a NodeDetail collection
   *
   * @since 1.0
   */
  public Collection<NodeDetail> getChildrenDetails(NodePK pk) throws RemoteException;

  /**
   * Get the header of each child of the node this function is to be used with frequently used nodes
   * because for each child, an ejb will be instanciated (nodes next to the root will be frequently
   * used) For less used nodes, choose the getChildrenDetails() method
   *
   * @param pk the node primary key.
   * @return a NodeDetail collection
   * @throws RemoteException
   * @since 1.0
   */
  public Collection<NodeDetail> getFrequentlyAskedChildrenDetails(NodePK pk) throws RemoteException;

  /**
   * Get the children number of this node
   *
   * @param pk the node primary key.
   * @return a int
   * @throws RemoteException
   * @since 1.0
   */
  public int getChildrenNumber(NodePK pk) throws RemoteException;

  /**
   * Update the attributes of the node
   *
   * @param nodeDetail the NodeDetail which contains updated data
   * @throws RemoteException
   * @since 1.0
   */
  public void setDetail(NodeDetail nodeDetail) throws RemoteException;

  /**
   * Create a new Node object
   *
   * @param nodeDetail the NodeDetail which contains data
   * @param fatherDetail the parent of node to be added
   * @return the NodePK of the new Node
   *
   * @throws RemoteException
   * @since 1.0
   */
  public NodePK createNode(NodeDetail nodeDetail, NodeDetail fatherDetail)
      throws RemoteException;

  /**
   * Create a new Node object
   *
   * @param nodeDetail the NodeDetail which contains data
   * @return the NodePK of the new Node
   *
   * @since 1.0
   */
  public NodePK createNode(NodeDetail nodeDetail) throws RemoteException;

  /**
   * Remove a node and its descendants
   *
   * @param pk the node PK to delete
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @since 1.0
   */
  public void removeNode(NodePK pk) throws RemoteException;

  public void moveNode(NodePK nodePK, NodePK toNode) throws RemoteException;

  /**
   * On node creation, check if another node have got the same name with same father
   *
   * @return true if there is already a node with same name with same father false else
   * @param con A connection to the database
   * @param nd A NodeDetail contains new node data to compare
   *
   * @since 1.0
   */
  public boolean isSameNameSameLevelOnCreation(NodeDetail nd)
      throws RemoteException;

  /**
   * On node update, check if another node have got the same name with same father
   *
   * @return true if there is already a node with same name with same father false else
   * @param con A connection to the database
   * @param nd A NodeDetail contains new node data to compare
   *
   * @since 1.0
   */
  public boolean isSameNameSameLevelOnUpdate(NodeDetail nd)
      throws RemoteException;

  /**
   * Get children node PKs of a node
   *
   * @return A collection of NodePK
   * @param con A connection to the database
   * @param nodePK A NodePK
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @since 1.0
   */
  public Collection<NodePK> getChildrenPKs(NodePK nodePK) throws RemoteException;

  /**
   * Get descendant node PKs of a node
   *
   * @return A collection of NodePK
   * @param con A connection to the database
   * @param nodePK A NodePK
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @since 1.0
   */
  public Collection<NodePK> getDescendantPKs(NodePK nodePK) throws RemoteException;

  /**
   * Get descendant nodeDetails of a node
   *
   * @return A List of NodeDetail
   * @param nodePK A NodePK
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @since 1.0
   */
  public List<NodeDetail> getDescendantDetails(NodePK nodePK) throws RemoteException;

  /**
   * Get descendant nodeDetails of a node
   *
   * @return A List of NodeDetail
   * @param node A NodeDetail
   * @since 4.07
   */
  public List<NodeDetail> getDescendantDetails(NodeDetail node) throws RemoteException;

  /**
   * Get the path from root to a node
   *
   * @return A collection of NodeDetail
   * @param nodePK A NodePK
   * @see com.stratelia.webactiv.util.node.model.NodePK
   *
   * @since 1.0
   */
  public Collection<NodeDetail> getAnotherPath(NodePK nodePK) throws RemoteException;

  /**
   * A wysiwyg's content has been added or modified to a node. Its content must be added to the
   * indexed content of the node
   *
   * @param nodePK the identifier of the node associated to the wysiwyg
   * @throws RemoteException
   */
  public void processWysiwyg(NodePK nodePK) throws RemoteException;

  public void updateRightsDependency(NodeDetail nodeDetail)
      throws RemoteException;

  /**
   * Method declaration
   *
   * @param nodeDetail
   * @throws RemoteException
   * @see
   */
  public void createIndex(NodeDetail nodeDetail) throws RemoteException;

  /**
   * Method declaration
   *
   * @param pk
   * @throws RemoteException
   * @see
   */
  public void deleteIndex(NodePK pk) throws RemoteException;

  public void sortNodes(List<NodePK> nodePKs) throws RemoteException;
}
