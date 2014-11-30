/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.stratelia.webactiv.node.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.Local;

import com.stratelia.webactiv.node.model.NodeDetail;
import com.stratelia.webactiv.node.model.NodePK;
import org.silverpeas.util.ServiceProvider;

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
  public NodeDetail getHeader(NodePK pk);

  public NodeDetail getHeader(NodePK pk, boolean getTranslations);

  /**
   * Get the attributes of a node and of its children
   *
   * @param pk the node primary key.
   * @return a NodeDetail
   */
  public NodeDetail getDetail(NodePK pk);

  public NodeDetail getDetailTransactionally(NodePK pk);

  public NodeDetail getDetailByNameAndFatherId(NodePK pk, String name, int nodeFatherId);

  public ArrayList<NodeDetail> getTree(NodePK pk);

  public ArrayList<NodeDetail> getSubTree(NodePK pk);

  public ArrayList<NodeDetail> getSubTree(NodePK pk, String sorting);

  public ArrayList<NodeDetail> getSubTreeByStatus(NodePK pk, String status);

  public ArrayList<NodeDetail> getSubTreeByStatus(NodePK pk, String status, String sorting);

  public ArrayList<NodeDetail> getSubTreeByLevel(NodePK pk, int level);

  public ArrayList<NodeDetail> getSubTreeByLevel(NodePK pk, int level, String sorting);

  public ArrayList<NodeDetail> getSubTree(NodePK pk, String status, int level, String sorting);

  /**
   * Method declaration
   *
   * @param pk the node primary key.
   * @return
   */
  public NodeDetail getTwoLevelDetails(NodePK pk);

  /**
   * Method declaration
   *
   * @param pk the node primary key.
   * @return
   */
  public NodeDetail getFrequentlyAskedDetail(NodePK pk);

  /**
   * Method declaration
   *
   * @param pk the node primary key.
   * @param level
   * @return
   */
  public List<NodeDetail> getHeadersByLevel(NodePK pk, int level);

  /**
   * Method declaration
   *
   * @param nodePK
   * @return
   */
  public Collection<NodeDetail> getAllNodes(NodePK nodePK);

  /**
   * Get the path of this node from this node to root
   *
   * @param pk the node primary key.
   * @return a NodeDetail Collection (only header).
   */
  public Collection<NodeDetail> getPath(NodePK pk);

  /**
   * Get the header of each child of the node.
   *
   * @param pk the node primary key.
   * @return a NodeDetail collection
   */
  public Collection<NodeDetail> getChildrenDetails(NodePK pk);

  /**
   * Get the header of each child of the node this function is to be used with frequently used nodes
   * because for each child, an ejb will be instanciated (nodes next to the root will be frequently
   * used) For less used nodes, choose the getChildrenDetails() method
   *
   * @param pk the node primary key.
   * @return a NodeDetail collection
   */
  public Collection<NodeDetail> getFrequentlyAskedChildrenDetails(NodePK pk);

  /**
   * Get the children number of this node
   *
   * @param pk the node primary key.
   * @return a int
   */
  public int getChildrenNumber(NodePK pk);

  /**
   * Update the attributes of the node
   *
   * @param nodeDetail the NodeDetail which contains updated data
   * @
   * @since 1.0
   */
  public void setDetail(NodeDetail nodeDetail);

  /**
   * Create a new Node object
   *
   * @param nodeDetail the NodeDetail which contains data
   * @param fatherDetail the parent of node to be added
   * @return the NodePK of the new Node
   */
  public NodePK createNode(NodeDetail nodeDetail, NodeDetail fatherDetail);

  /**
   * Create a new Node object
   *
   * @param nodeDetail the NodeDetail which contains data
   * @return the NodePK of the new Node
   */
  public NodePK createNode(NodeDetail nodeDetail);

  /**
   * Remove a node and its descendants
   *
   * @param pk the node PK to delete
   * @see com.stratelia.webactiv.node.model.NodePK
   */
  public void removeNode(NodePK pk);

  public void moveNode(NodePK nodePK, NodePK toNode);

  /**
   * On node creation, check if another node have got the same name with same father
   *
   * @return true if there is already a node with same name with same father false else
   * @param nd A NodeDetail contains new node data to compare
   */
  public boolean isSameNameSameLevelOnCreation(NodeDetail nd);

  /**
   * On node update, check if another node have got the same name with same father
   *
   * @return true if there is already a node with same name with same father false else
   * @param nd A NodeDetail contains new node data to compare
   */
  public boolean isSameNameSameLevelOnUpdate(NodeDetail nd);

  /**
   * Get children node PKs of a node
   *
   * @return A collection of NodePK
   * @param nodePK A NodePK
   * @see com.stratelia.webactiv.node.model.NodePK
   */
  public Collection<NodePK> getChildrenPKs(NodePK nodePK);

  /**
   * Get descendant node PKs of a node
   *
   * @return A collection of NodePK
   * @param nodePK A NodePK
   * @see com.stratelia.webactiv.node.model.NodePK
   * @since 1.0
   */
  public Collection<NodePK> getDescendantPKs(NodePK nodePK);

  /**
   * Get descendant nodeDetails of a node
   *
   * @return A List of NodeDetail
   * @param nodePK A NodePK
   * @see com.stratelia.webactiv.node.model.NodePK
   * @since 1.0
   */
  public List<NodeDetail> getDescendantDetails(NodePK nodePK);

  /**
   * Get descendant nodeDetails of a node
   *
   * @return A List of NodeDetail
   * @param node A NodeDetail
   */
  public List<NodeDetail> getDescendantDetails(NodeDetail node);

  /**
   * Get the path from root to a node
   *
   * @return A collection of NodeDetail
   * @param nodePK A NodePK
   * @see com.stratelia.webactiv.node.model.NodePK
   */
  public Collection<NodeDetail> getAnotherPath(NodePK nodePK);

  /**
   * A wysiwyg's content has been added or modified to a node. Its content must be added to the
   * indexed content of the node
   *
   * @param nodePK the identifier of the node associated to the wysiwyg
   */
  public void processWysiwyg(NodePK nodePK);

  public void updateRightsDependency(NodeDetail nodeDetail);

  /**
   * Method declaration
   *
   * @param nodeDetail
   */
  public void createIndex(NodeDetail nodeDetail);

  /**
   * Method declaration
   *
   * @param pk
   */
  public void deleteIndex(NodePK pk);

  public void sortNodes(List<NodePK> nodePKs);
}
