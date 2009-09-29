/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.util.favorit.control;

import javax.ejb.*;
import java.rmi.RemoteException;
import java.util.Collection;
import com.stratelia.webactiv.util.node.model.NodePK;

/**
 * Interface declaration
 * 
 * 
 * @author
 * @version %I%, %G%
 */
public interface FavoritBm extends EJBObject {

  /**
   * Method declaration
   * 
   * 
   * @param userId
   * @param node
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public void addFavoritNode(String userId, NodePK node) throws RemoteException;

  /**
   * Method declaration
   * 
   * 
   * @param userId
   * @param node
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public void removeFavoritNode(String userId, NodePK node)
      throws RemoteException;

  /**
   * Method declaration
   * 
   * 
   * @param userId
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public void removeFavoritByUser(String userId) throws RemoteException;

  /**
   * Method declaration
   * 
   * 
   * @param node
   * @param path
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public void removeFavoritByNodePath(NodePK node, String path)
      throws RemoteException;

  /**
   * Method declaration
   * 
   * 
   * @param userId
   * 
   * @return
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public Collection getFavoritNodePKs(String userId) throws RemoteException;

  /**
   * Method declaration
   * 
   * 
   * @param userId
   * @param space
   * @param componentName
   * 
   * @return
   * 
   * @throws RemoteException
   * 
   * @see
   */
  // NEWD DLE
  // public Collection getFavoritNodePKsBySpaceAndComponent(String userId,
  // String space, String componentName) throws RemoteException;
  public Collection getFavoritNodePKsByComponent(String userId,
      String componentName) throws RemoteException;
  // NEWF DLE

}
