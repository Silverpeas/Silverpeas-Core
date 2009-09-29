/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

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
   * @return a NodeDetail
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @since 1.0
   */
  public NodeDetail getHeader(NodePK pk) throws RemoteException;

  public NodeDetail getHeader(NodePK pk, boolean getTranslations)
      throws RemoteException;

  /**
   * Get the attributes of a node and of its children
   * 
   * @return a NodeDetail
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @since 1.0
   */
  public NodeDetail getDetail(NodePK pk) throws RemoteException;

  public NodeDetail getDetail(NodePK pk, String sorting) throws RemoteException;

  public NodeDetail getDetailByNameAndFatherId(NodePK pk, String name,
      int nodeFatherId) throws RemoteException;

  public ArrayList getTree(NodePK pk) throws RemoteException;

  public ArrayList getSubTree(NodePK pk) throws RemoteException;

  public ArrayList getSubTree(NodePK pk, String sorting) throws RemoteException;

  public ArrayList getSubTreeByStatus(NodePK pk, String status)
      throws RemoteException;

  /**
   * Method declaration
   * 
   * 
   * @param pk
   * 
   * @return
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public NodeDetail getTwoLevelDetails(NodePK pk) throws RemoteException;

  /**
   * Method declaration
   * 
   * 
   * @param pk
   * 
   * @return
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public NodeDetail getFrequentlyAskedDetail(NodePK pk) throws RemoteException;

  /**
   * Method declaration
   * 
   * 
   * @param pk
   * @param level
   * 
   * @return
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public List getHeadersByLevel(NodePK pk, int level) throws RemoteException;

  /**
   * Method declaration
   * 
   * 
   * @param nodePK
   * 
   * @return
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public Collection getAllNodes(NodePK nodePK) throws RemoteException;

  /**
   * Get the path of this node from this node to root
   * 
   * @return a NodeDetail Collection (only header)
   * @param pk
   *          The PK of the node
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @see java.util.Collection
   * @since 1.0
   */
  public Collection getPath(NodePK pk) throws RemoteException;

  /**
   * Get the header of each child of the node
   * 
   * @return a NodeDetail collection
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @since 1.0
   */
  public Collection getChildrenDetails(NodePK pk) throws RemoteException;

  /**
   * Get the header of each child of the node this function is to be used with
   * frequently used nodes because for each child, an ejb will be instanciated
   * (nodes next to the root will be frequently used) For less used nodes,
   * choose the getChildrenDetails() method
   * 
   * @return a NodeDetail collection
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @since 1.0
   */
  public Collection getFrequentlyAskedChildrenDetails(NodePK pk)
      throws RemoteException;

  /**
   * Get the children number of this node
   * 
   * @return a int
   * @since 1.0
   */
  public int getChildrenNumber(NodePK pk) throws RemoteException;

  /**
   * Update the attributes of the node
   * 
   * @param nodeDetail
   *          the NodeDetail which contains updated data
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @since 1.0
   */
  public void setDetail(NodeDetail nodeDetail) throws RemoteException;

  /**
   * Create a new Node object
   * 
   * @param nodeDetail
   *          the NodeDetail which contains data
   * @param creatorPK
   *          the PK of the user who have create this node
   * @return the NodePK of the new Node
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @see com.stratelia.webactiv.util.actor.model.ActorPK
   * @since 1.0
   */
  public NodePK createNode(NodeDetail nodeDetail, NodeDetail fatherDetail)
      throws RemoteException;

  /**
   * Remove a node and its descendants
   * 
   * @param pk
   *          the node PK to delete
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @since 1.0
   */
  public void removeNode(NodePK pk) throws RemoteException;

  public void moveNode(NodePK nodePK, NodePK toNode) throws RemoteException;

  /**
   * On node creation, check if another node have got the same name with same
   * father
   * 
   * @return true if there is already a node with same name with same father
   *         false else
   * @param con
   *          A connection to the database
   * @param nd
   *          A NodeDetail contains new node data to compare
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @since 1.0
   */
  public boolean isSameNameSameLevelOnCreation(NodeDetail nd)
      throws RemoteException;

  /**
   * On node update, check if another node have got the same name with same
   * father
   * 
   * @return true if there is already a node with same name with same father
   *         false else
   * @param con
   *          A connection to the database
   * @param nd
   *          A NodeDetail contains new node data to compare
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @since 1.0
   */
  public boolean isSameNameSameLevelOnUpdate(NodeDetail nd)
      throws RemoteException;

  /**
   * Get children node PKs of a node
   * 
   * @return A collection of NodePK
   * @param con
   *          A connection to the database
   * @param nodePK
   *          A NodePK
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @since 1.0
   */
  public Collection getChildrenPKs(NodePK nodePK) throws RemoteException;

  /**
   * Get descendant node PKs of a node
   * 
   * @return A collection of NodePK
   * @param con
   *          A connection to the database
   * @param nodePK
   *          A NodePK
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @since 1.0
   */
  public Collection getDescendantPKs(NodePK nodePK) throws RemoteException;

  /**
   * Get descendant nodeDetails of a node
   * 
   * @return A List of NodeDetail
   * @param nodePK
   *          A NodePK
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @since 1.0
   */
  public List getDescendantDetails(NodePK nodePK) throws RemoteException;

  /**
   * Get descendant nodeDetails of a node
   * 
   * @return A List of NodeDetail
   * @param node
   *          A NodeDetail
   * @since 4.07
   */
  public List getDescendantDetails(NodeDetail node) throws RemoteException;

  /**
   * Get the path from root to a node
   * 
   * @return A collection of NodeDetail
   * @param nodePK
   *          A NodePK
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @since 1.0
   */
  public Collection getAnotherPath(NodePK nodePK) throws RemoteException;

  /**
   * A wysiwyg's content has been added or modified to a node. Its content must
   * be added to the indexed content of the node
   * 
   * @param nodePK
   *          the identifier of the node associated to the wysiwyg
   * 
   * @throws RemoteException
   * 
   */
  public void processWysiwyg(NodePK nodePK) throws RemoteException;

  public void updateRightsDependency(NodeDetail nodeDetail)
      throws RemoteException;

  /**
   * Method declaration
   * 
   * 
   * @param nodeDetail
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public void createIndex(NodeDetail nodeDetail) throws RemoteException;

  /**
   * Method declaration
   * 
   * 
   * @param pk
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public void deleteIndex(NodePK pk) throws RemoteException;

}
