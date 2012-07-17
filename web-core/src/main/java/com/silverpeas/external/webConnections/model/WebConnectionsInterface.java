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

package com.silverpeas.external.webConnections.model;

import java.rmi.RemoteException;
import java.util.List;

public interface WebConnectionsInterface {

  /**
   * get the connection corresponding to componentId and userId
   * @param componentId : String
   * @param userId : String
   * @return connection : ConnectionDetail
   */
  public ConnectionDetail getWebConnection(String componentId, String userId);

  /**
   * get the connection corresponding to connectionId
   * @param connectionId : String
   * @return connection : ConnectionDetail
   */
  public ConnectionDetail getWebConnectionById(String connectionId);

  /**
   * create a new connection
   * @param connection : ConnectionDetail
   */
  public void createWebConnection(ConnectionDetail connection);

  /**
   * delete the connection corresponding to connectionId
   * @param connectionId : String
   */
  public void deleteWebConnection(String connectionId);

  /**
   * update the connection corresponding to connectionId, with login and password
   * @param connectionId : String
   * @param login : String
   * @param password : String
   */
  public void updateWebConnection(String connectionId, String login, String password);

  /**
   * get all connections for the user corresponding to userId
   * @param userId : String
   * @return a list of ConnectionDetail
   * @throws RemoteException
   */
  public List<ConnectionDetail> listWebConnectionsOfUser(String userId) throws RemoteException;

}
