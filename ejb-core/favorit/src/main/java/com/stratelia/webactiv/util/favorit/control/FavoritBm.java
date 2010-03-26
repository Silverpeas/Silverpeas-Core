/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.util.favorit.control;

import javax.ejb.*;
import java.rmi.RemoteException;
import java.util.Collection;
import com.stratelia.webactiv.util.node.model.NodePK;

/**
 * Interface declaration
 * @author
 * @version %I%, %G%
 */
public interface FavoritBm extends EJBObject {

  /**
   * Method declaration
   * @param userId
   * @param node
   * @throws RemoteException
   * @see
   */
  public void addFavoritNode(String userId, NodePK node) throws RemoteException;

  /**
   * Method declaration
   * @param userId
   * @param node
   * @throws RemoteException
   * @see
   */
  public void removeFavoritNode(String userId, NodePK node)
      throws RemoteException;

  /**
   * Method declaration
   * @param userId
   * @throws RemoteException
   * @see
   */
  public void removeFavoritByUser(String userId) throws RemoteException;

  /**
   * Method declaration
   * @param node
   * @param path
   * @throws RemoteException
   * @see
   */
  public void removeFavoritByNodePath(NodePK node, String path)
      throws RemoteException;

  /**
   * Method declaration
   * @param userId
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection getFavoritNodePKs(String userId) throws RemoteException;

  /**
   * Method declaration
   * @param userId
   * @param space
   * @param componentName
   * @return
   * @throws RemoteException
   * @see
   */
  // NEWD DLE
  // public Collection getFavoritNodePKsBySpaceAndComponent(String userId,
  // String space, String componentName) throws RemoteException;
  public Collection getFavoritNodePKsByComponent(String userId,
      String componentName) throws RemoteException;
  // NEWF DLE

}
