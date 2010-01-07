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
package com.stratelia.webactiv.util.node.ejb;

import java.util.Collection;
import java.rmi.RemoteException;
import javax.ejb.*;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.node.model.NodeDetail;

/**
 * This is the Node Home interface.
 * @author Nicolas Eysseric
 */
public interface NodeHome extends EJBHome {

  /**
   * Create a new Node object
   * @param nd the NodeDetail which contains data
   * @return the new Node
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @see com.stratelia.webactiv.util.actor.model.ActorPK
   * @exception javax.ejb.RemoteException
   * @exception javax.ejb.CreateException
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public Node create(NodeDetail nd) throws RemoteException, CreateException;

  /**
   * Create an instance of a Node object
   * @param nodePK the PK of the Node to instanciate
   * @return the instanciated Node if it exists in database
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @exception javax.ejb.RemoteException
   * @exception javax.ejb.FinderException
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public Node findByPrimaryKey(NodePK nodePK) throws RemoteException,
      FinderException;

  /**
   * Create a collection of instance of a Node object The collection can be empty.
   * @param fatherPK the PK of the father from all the Nodes to instanciate
   * @return the instanciated Node's collection if it exists in database
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @exception javax.ejb.RemoteException
   * @exception javax.ejb.FinderException
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public Collection<NodePK> findByFatherPrimaryKey(NodePK fatherPK)
      throws FinderException, RemoteException;

  public Node findByNameAndFatherId(NodePK nodePK, String name, int nodeFatherId)
      throws RemoteException, FinderException;
}