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

package com.silverpeas.external.webConnections.dao;

import com.silverpeas.external.webConnections.model.ConnectionDetail;
import com.silverpeas.util.cryptage.CryptageException;
import com.silverpeas.util.cryptage.SilverCryptFactorySymetric;
import com.silverpeas.util.cryptage.SilverCryptKeysSymetric;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.UtilException;
import org.silverpeas.core.admin.OrganisationControllerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionDAO {
  private static String tableName = "SB_webConnections_info";
  private static ResourceLocator settings =
      new ResourceLocator("com.silverpeas.external.webConnections.settings.webConnectionsSettings",
      "fr");
  private static String keyCode = settings.getString("keycode");
  private static SilverCryptKeysSymetric symetricKeys = new SilverCryptKeysSymetric(keyCode);

  /**
   * Return a connection for componentId and userId
   * @param con : Connection
   * @param componentId : String
   * @param userId : String
   * @return connection : ConnectionDetail
   */
  public ConnectionDetail getConnection(Connection con, String componentId, String userId)
      throws SQLException {
    ConnectionDetail connection = null;
    String query = "select * from " + tableName + " where componentId = ? and userId = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, componentId);
      prepStmt.setInt(2, Integer.parseInt(userId));
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        connection = recupConnection(rs);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return connection;
  }

  /**
   * Return a connection corresponding to connectionId
   * @param con : Connection
   * @param connectionId : String
   * @return connection : ConnectionDetail
   * @throws SQLException
   */
  public ConnectionDetail getConnectionById(Connection con, String connectionId)
      throws SQLException {
    ConnectionDetail connection = null;
    String query = "select * from " + tableName + " where connectionId = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(connectionId));
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        connection = recupConnection(rs);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return connection;
  }

  /**
   * create a connection
   * @param con : Connection
   * @param connection : ConnectionDetail
   * @return the connectionId : String
   * @throws SQLException
   * @throws UtilException
   */
  public String createConnection(Connection con, ConnectionDetail connection) throws SQLException,
      UtilException {
    String id = "";
    PreparedStatement prepStmt = null;
    try {
      int newId = DBUtil.getNextId(tableName, "connectionId");
      id = String.valueOf(newId);
      String query =
          "INSERT INTO " + tableName +
          " (connectionId, userId, componentId, paramLogin, paramPassword) " +
          "VALUES (?,?,?,?,?)";
      prepStmt = con.prepareStatement(query);
      initParam(prepStmt, newId, connection);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
    return id;
  }

  /**
   * delete the connection corresponding to connectionId
   * @param con : Connection
   * @param connectionId : String
   * @throws SQLException
   */
  public void deleteConnection(Connection con, String connectionId) throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      String query = "delete from " + tableName + " where connectionId = ? ";
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(connectionId));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * update the connection corresponding to connectionId, with the login and the password
   * @param con : Connection
   * @param connectionId : String
   * @param login : String
   * @param password : String
   * @throws SQLException
   */
  public void updateConnection(Connection con, String connectionId, String login, String password)
      throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      String query =
          "update " + tableName + " set paramLogin = ? , paramPassword = ? where connectionId = ? ";
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, login);
      byte[] crypPassword = null;
      try {
        crypPassword = getCryptString(password);
      } catch (CryptageException e) {
        crypPassword = null;
      }
      prepStmt.setBytes(2, crypPassword);
      prepStmt.setInt(3, Integer.parseInt(connectionId));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * return all connections of the user corresponding to userId
   * @param con : Connection
   * @param userId : String
   * @return connections : a list of ConnectionDetail
   * @throws SQLException
   */
  public List<ConnectionDetail> getConnectionsByUser(Connection con, String userId)
      throws SQLException {
    ArrayList<ConnectionDetail> connections = null;
    String query = "select * from " + tableName + " where userId = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(userId));
      rs = prepStmt.executeQuery();
      connections = new ArrayList<ConnectionDetail>();
      while (rs.next()) {
        ConnectionDetail connection = recupConnection(rs);
        connections.add(connection);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return connections;
  }

  /**
   * create the connection from the resultSet
   * @param rs : ResultSet
   * @return the connection : ConnectionDetail
   * @throws SQLException
   */
  protected ConnectionDetail recupConnection(ResultSet rs) throws SQLException {
    ConnectionDetail connection = new ConnectionDetail();
    connection.setConnectionId(rs.getInt("connectionId"));
    connection.setUserId(rs.getString("userId"));
    connection.setComponentId(rs.getString("componentId"));
    Map<String, String> param = new HashMap<String, String>();
    String login = rs.getString("paramLogin");
    byte[] password = rs.getBytes("paramPassword");
    ComponentInst inst = OrganisationControllerFactory.getOrganisationController()
        .getComponentInst(connection.getComponentId());
    String nameLogin = inst.getParameterValue("login");
    String namePassword = inst.getParameterValue("password");
    param.put(nameLogin, login);
    String decrypPassword = "";
    try {
      decrypPassword = getUncryptString(password);
    } catch (CryptageException e) {
      decrypPassword = "";
    }
    param.put(namePassword, decrypPassword);
    connection.setParam(param);

    return connection;
  }

  /**
   * initialize the prepStmt with the connection
   * @param prepStmt : PreparedStatement
   * @param id : int
   * @param connection : ConnectionDetail
   * @throws SQLException
   */
  private static void initParam(PreparedStatement prepStmt, int id, ConnectionDetail connection)
      throws SQLException {
    prepStmt.setInt(1, id);
    prepStmt.setInt(2, Integer.parseInt(connection.getUserId()));
    prepStmt.setString(3, connection.getComponentId());
    ComponentInst inst = OrganisationControllerFactory
        .getOrganisationController().getComponentInst(connection.getComponentId());
    String login = connection.getParam().get(inst.getParameterValue("login"));
    String password = connection.getParam().get(inst.getParameterValue("password"));
    byte[] crypPassword = null;
    try {
      crypPassword = getCryptString(password);
    } catch (CryptageException e) {
      e.printStackTrace();
      crypPassword = null;
    }
    prepStmt.setString(4, login);
    prepStmt.setBytes(5, crypPassword);
  }

  /**
   * return the encrypt String corresponding to the string cryptedString
   * @param cryptedString : String
   * @return the encrypt string : byte[]
   * @throws CryptageException
   */
  private static byte[] getCryptString(String cryptedString) throws CryptageException {
    SilverCryptFactorySymetric factory = SilverCryptFactorySymetric.getInstance();
    return factory.goCrypting(cryptedString, symetricKeys);
  }

  /**
   * return the uncrypt string corresponding to the encrypt string cipherText
   * @param cipherText : byte[]
   * @return the uncrypt string : String
   * @throws CryptageException
   */
  private static String getUncryptString(byte[] cipherText) throws CryptageException {
    SilverCryptFactorySymetric factory = SilverCryptFactorySymetric.getInstance();
    return factory.goUnCrypting(cipherText, symetricKeys);
  }

}
