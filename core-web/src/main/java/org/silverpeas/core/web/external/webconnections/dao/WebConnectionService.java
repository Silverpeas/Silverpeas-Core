/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.web.external.webconnections.dao;

import org.silverpeas.core.security.authorization.ForbiddenRuntimeException;
import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.web.external.webconnections.model.ConnectionDetail;
import org.silverpeas.core.web.external.webconnections.model.WebConnectionsInterface;
import org.silverpeas.core.exception.SilverpeasRuntimeException;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.silverpeas.core.persistence.jdbc.DBUtil.openConnection;

/**
 * @author
 */
@Singleton
public class WebConnectionService implements WebConnectionsInterface, ComponentInstanceDeletion {
  private ConnectionDAO dao;

  /**
   * Hidden constructor
   */
  protected WebConnectionService() {
  }

  @PostConstruct
  private void initialize() {
    dao = new ConnectionDAO();
  }

  @Override
  @Transactional
  public void delete(final String componentInstanceId) {
    try {
      dao.deleteByComponentInstanceId(componentInstanceId);
    } catch (SQLException e) {
      throw new WebConnectionsRuntimeException("WebConnectionsInterface.getConnection()",
          SilverpeasRuntimeException.ERROR, "webConnections.MSG_CONNECTION_NOT_EXIST", e);
    }
  }

  public ConnectionDetail getWebConnection(String componentId, String userId) {
    try (Connection con = openConnection()) {
      return dao.getConnection(con, componentId, userId);
    } catch (SQLException e) {
      throw new WebConnectionsRuntimeException("WebConnectionsInterface.getConnection()",
          SilverpeasRuntimeException.ERROR, "webConnections.MSG_CONNECTION_NOT_EXIST", e);
    }
  }

  public ConnectionDetail getWebConnectionById(String connectionId, String userId) {
    try (Connection con = openConnection()) {

      ConnectionDetail connectionDetail = dao.getConnectionById(con, connectionId);

      //check rights : check that the current user has the rights to get the web connection details
      if(!userId.equals(connectionDetail.getUserId())) {
        throw new ForbiddenRuntimeException("WebConnectionsInterface.getWebConnectionById()",
          SilverpeasRuntimeException.ERROR, "peasCore.RESOURCE_ACCESS_UNAUTHORIZED", "connectionId="+connectionId+", userId="+userId);
      }
      return connectionDetail;

    } catch (Exception e) {
      throw new WebConnectionsRuntimeException("WebConnectionsInterface.getConnectionById()",
          SilverpeasRuntimeException.ERROR, "webConnections.MSG_CONNECTION_NOT_EXIST", e);
    }
  }

  @Transactional
  public void createWebConnection(ConnectionDetail connection) {
    try (Connection con = openConnection()) {
      dao.createConnection(con, connection);
    } catch (Exception e) {
      throw new WebConnectionsRuntimeException("WebConnectionsInterface.createConnection()",
          SilverpeasRuntimeException.ERROR, "webConnections.MSG_CONNECTION_NOT_CREATE", e);
    }
  }

  @Transactional
  public void deleteWebConnection(String connectionId, String userId) {
    try (Connection con = openConnection()) {

      //check rights : check that the current user has the rights to delete the web connection
      getWebConnectionById(connectionId, userId);

      dao.deleteConnection(con, connectionId);

    } catch (Exception e) {
      throw new WebConnectionsRuntimeException("WebConnectionsInterface.deleteConnection()",
          SilverpeasRuntimeException.ERROR, "webConnections.MSG_CONNECTION_NOT_DELETE", e);
    }
  }

  @Transactional
  public void updateWebConnection(String connectionId, String login, String password, String userId) {
    try (Connection con = openConnection()) {

      //check rights : check that the current user has the rights to update the web connection
      getWebConnectionById(connectionId, userId);

      dao.updateConnection(con, connectionId, login, password);

    } catch (Exception e) {
      throw new WebConnectionsRuntimeException("WebConnectionsInterface.updateConnection()",
          SilverpeasRuntimeException.ERROR, "webConnections.MSG_CONNECTION_NOT_UPDATE", e);
    }
  }

  public List<ConnectionDetail> listWebConnectionsOfUser(String userId) throws RemoteException {
    try (Connection con = openConnection()) {
      return dao.getConnectionsByUser(con, userId);
    } catch (Exception e) {
      throw new WebConnectionsRuntimeException("WebConnectionsInterface.getConnectionsByUser()",
          SilverpeasRuntimeException.ERROR, "webConnections.MSG_CONNECTIONS_NOT_EXIST", e);
    }
  }
}
