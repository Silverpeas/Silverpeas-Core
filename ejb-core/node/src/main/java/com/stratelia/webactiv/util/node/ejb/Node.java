package com.stratelia.webactiv.util.node.ejb;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Collection;

import javax.ejb.EJBObject;

import com.stratelia.webactiv.util.node.model.NodeDetail;

/**
 * This is the Node interface.
 * 
 * @author Nicolas Eysseric
 */
public interface Node extends EJBObject {

  /**
   * Get the attributes of THIS node and of its children
   * 
   * @return a NodeDetail
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @exception java.sql.SQLException
   * @exception java.rmi.RemoteException
   * @since 1.0
   */
  public NodeDetail getDetail() throws RemoteException, SQLException;

  public NodeDetail getDetail(String sorting) throws RemoteException,
      SQLException;

  /**
   * Get the attributes of THIS node
   * 
   * @return a NodeDetail
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @exception java.rmi.RemoteException
   * @since 1.0
   */
  public NodeDetail getHeader() throws RemoteException;

  /**
   * Get the header of each child of the node
   * 
   * @return a NodeDetail collection
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @exception java.sql.SQLException
   * @exception java.rmi.RemoteException
   * @since 1.0
   */
  public Collection getChildrenDetails() throws RemoteException, SQLException;

  public Collection getChildrenDetails(String sorting) throws RemoteException,
      SQLException;

  /**
   * Update the attributes of the node
   * 
   * @param nd
   *          the NodeDetail which contains updated data
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @exception java.rmi.RemoteException
   * @since 1.0
   */
  public void setDetail(NodeDetail nodeDetail) throws RemoteException;

  public void setRightsDependsOn(int nodeId) throws RemoteException;
}