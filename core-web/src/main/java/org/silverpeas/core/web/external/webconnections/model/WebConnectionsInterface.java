/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.external.webconnections.model;

import org.silverpeas.core.util.ServiceProvider;

import java.util.List;

public interface WebConnectionsInterface {

  static WebConnectionsInterface get() {
    return ServiceProvider.getService(WebConnectionsInterface.class);
  }

  /**
   * Get the connection corresponding to componentId and userId
   * @param componentId  unique identifier of a component instance.
   * @param userId unique identifier of a user
   * @return connection details of the connection
   */
  ConnectionDetail getWebConnection(String componentId, String userId);

  /**
   * Get the connection corresponding to connectionId
   * @param connectionId  unique identifier of a connection.
   * @param userId unique identifier of a user
   * @return connection details of the connection
   */
  ConnectionDetail getWebConnectionById(String connectionId, String userId);

  /**
   * Create a new connection
   * @param connection details of the connection
   */
  void createWebConnection(ConnectionDetail connection);

  /**
   * Delete the connection corresponding to connectionId
   * @param connectionId unique identifier of a connection
   * @param userId unique identifier of a user
   */
  void deleteWebConnection(String connectionId, String userId);

  /**
   * Update the connection corresponding to connectionId, with login and password
   * @param connectionId unique identifier of a connection
   * @param login a user login
   * @param password a user password
   * @param userId a unique identifier of the user
   */
  void updateWebConnection(String connectionId, String login, String password, String userId);

  /**
   * Get all connections for the user corresponding to userId
   * @param userId : String
   * @return a list of details of connections
   */
  List<ConnectionDetail> listWebConnectionsOfUser(String userId);

}
