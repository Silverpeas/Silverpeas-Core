/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
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

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Collection;

import javax.ejb.EJBObject;

import com.stratelia.webactiv.util.node.model.NodeDetail;

/**
 * This is the Node interface.
 * @author Nicolas Eysseric
 */
public interface Node extends EJBObject {

  /**
   * Get the attributes of THIS node and of its children
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
   * @return a NodeDetail
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @exception java.rmi.RemoteException
   * @since 1.0
   */
  public NodeDetail getHeader() throws RemoteException;

  /**
   * Get the header of each child of the node
   * @return a NodeDetail collection
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @exception java.sql.SQLException
   * @exception java.rmi.RemoteException
   * @since 1.0
   */
  public Collection<NodeDetail> getChildrenDetails() throws RemoteException, SQLException;

  public Collection<NodeDetail> getChildrenDetails(String sorting) throws RemoteException,
      SQLException;

  /**
   * Update the attributes of the node
   * @param nd the NodeDetail which contains updated data
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @exception java.rmi.RemoteException
   * @since 1.0
   */
  public void setDetail(NodeDetail nodeDetail) throws RemoteException;

  public void setRightsDependsOn(int nodeId) throws RemoteException;
}