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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.silverpeas.external.webConnections.dao;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.silverpeas.external.webConnections.model.ConnectionDetail;
import com.silverpeas.external.webConnections.model.WebConnectionsInterface;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;

/**
 * @author
 */
public class WebConnectionService implements WebConnectionsInterface {
  private ConnectionDAO dao;

  public WebConnectionService() {
    dao = new ConnectionDAO();
  }

  public ConnectionDetail getWebConnection(String componentId, String userId) {
    Connection con = initCon();
    try {
      return dao.getConnection(con, componentId, userId);
    } catch (SQLException e) {
      throw new WebConnectionsRuntimeException("WebConnectionsInterface.getConnection()",
          SilverpeasRuntimeException.ERROR, "webConnections.MSG_CONNECTION_NOT_EXIST", e);
    } finally {
      fermerCon(con);
    }
  }

  public ConnectionDetail getWebConnectionById(String connectionId) {
    Connection con = initCon();
    try {
      return dao.getConnectionById(con, connectionId);
    } catch (Exception e) {
      throw new WebConnectionsRuntimeException("WebConnectionsInterface.getConnectionById()",
          SilverpeasRuntimeException.ERROR, "webConnections.MSG_CONNECTION_NOT_EXIST", e);
    } finally {
      fermerCon(con);
    }
  }

  public void createWebConnection(ConnectionDetail connection) {
    Connection con = initCon();
    try {
      dao.createConnection(con, connection);
    } catch (Exception e) {
      throw new WebConnectionsRuntimeException("WebConnectionsInterface.createConnection()",
          SilverpeasRuntimeException.ERROR, "webConnections.MSG_CONNECTION_NOT_CREATE", e);
    } finally {
      fermerCon(con);
    }
  }

  public void deleteWebConnection(String connectionId) {
    Connection con = initCon();
    try {
      dao.deleteConnection(con, connectionId);
    } catch (Exception e) {
      throw new WebConnectionsRuntimeException("WebConnectionsInterface.deleteConnection()",
          SilverpeasRuntimeException.ERROR, "webConnections.MSG_CONNECTION_NOT_DELETE", e);
    } finally {
      fermerCon(con);
    }
  }

  public void updateWebConnection(String connectionId, String login, String password) {
    Connection con = initCon();
    try {
      dao.updateConnection(con, connectionId, login, password);
    } catch (Exception e) {
      throw new WebConnectionsRuntimeException("WebConnectionsInterface.updateConnection()",
          SilverpeasRuntimeException.ERROR, "webConnections.MSG_CONNECTION_NOT_UPDATE", e);
    } finally {
      fermerCon(con);
    }
  }

  public List<ConnectionDetail> listWebConnectionsOfUser(String userId) throws RemoteException {
    Connection con = initCon();
    try {
      return dao.getConnectionsByUser(con, userId);
    } catch (Exception e) {
      throw new WebConnectionsRuntimeException("WebConnectionsInterface.getConnectionsByUser()",
          SilverpeasRuntimeException.ERROR, "webConnections.MSG_CONNECTIONS_NOT_EXIST", e);
    } finally {
      fermerCon(con);
    }
  }

  private Connection initCon() {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    } catch (UtilException e) {
      throw new WebConnectionsRuntimeException("WebConnectionsInterface.initCon()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
    return con;
  }

  private void fermerCon(Connection con) {
    try {
      if (con != null) {
        con.close();
      }
    } catch (SQLException e) {
      throw new WebConnectionsRuntimeException("WebConnectionsInterface.fermerCon()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_CLOSE_FAILED", e);
    }
  }
}
