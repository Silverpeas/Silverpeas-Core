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
package org.silverpeas.core.web.external.webconnections.dao;

import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.security.encryption.cipher.Cipher;
import org.silverpeas.core.security.encryption.cipher.CipherFactory;
import org.silverpeas.core.security.encryption.cipher.CipherKey;
import org.silverpeas.core.security.encryption.cipher.CryptoException;
import org.silverpeas.core.security.encryption.cipher.CryptographicAlgorithmName;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.external.webconnections.model.ConnectionDetail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionDAO {
  private static final String SELECT_FROM = "select * from ";
  private static final String TABLE_NAME = "SB_webConnections_info";
  private static final SettingBundle settings = ResourceLocator.getSettingBundle(
      "org.silverpeas.external.webConnections.settings.webConnectionsSettings");
  // warning: the key code should be in hexadecimal!
  private static final String KEYCODE = settings.getString("keycode");

  /**
   * Return a connection for componentId and userId
   * @param con a connection
   * @param componentId a component identifier
   * @param userId a user identifier
   * @return a ConnectionDetail
   */
  public ConnectionDetail getConnection(Connection con, String componentId, String userId)
      throws SQLException {
    ConnectionDetail connection = null;
    String query = SELECT_FROM + TABLE_NAME + " where componentId = ? and userId = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, componentId);
      prepStmt.setInt(2, Integer.parseInt(userId));
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        connection = getConnectionFrom(rs);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return connection;
  }

  /**
   * Return a connection corresponding to connectionId
   * @param con a connection
   * @param connectionId the connection identifier
   * @return a ConnectionDetail
   * @throws SQLException if an error occurs
   */
  public ConnectionDetail getConnectionById(Connection con, String connectionId)
      throws SQLException {
    ConnectionDetail connection = null;
    String query = SELECT_FROM + TABLE_NAME + " where connectionId = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(connectionId));
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        connection = getConnectionFrom(rs);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return connection;
  }

  /**
   * create a connection
   *
   * @param con the connection
   * @param connection the ConnectionDetail
   * @throws SQLException if an error occurs
   */
  public void createConnection(Connection con, ConnectionDetail connection) throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      int newId = DBUtil.getNextId(TABLE_NAME, "connectionId");
      String query = "INSERT INTO " + TABLE_NAME +
          " (connectionId, userId, componentId, paramLogin, paramPassword) " +
          "VALUES (?,?,?,?,?)";
      prepStmt = con.prepareStatement(query);
      initParam(prepStmt, newId, connection);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * delete the connection corresponding to connectionId
   * @param con a connection
   * @param connectionId the connection identifier
   * @throws SQLException if an error occurs
   */
  public void deleteConnection(Connection con, String connectionId) throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      String query = "delete from " + TABLE_NAME + " where connectionId = ? ";
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(connectionId));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * update the connection corresponding to connectionId, with the login and the password
   * @param con a connection
   * @param connectionId connection identifier
   * @param login connection login
   * @param password connection password
   * @throws SQLException if an error occurs
   */
  public void updateConnection(Connection con, String connectionId, String login, String password)
      throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      String query =
          "update " + TABLE_NAME + " set paramLogin = ? , paramPassword = ? where connectionId = ? ";
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, login);
      byte[] cryptPassword;
      try {
        cryptPassword = getCryptString(password);
      } catch (CryptoException e) {
        cryptPassword = null;
      }
      prepStmt.setBytes(2, cryptPassword);
      prepStmt.setInt(3, Integer.parseInt(connectionId));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * return all connections of the user corresponding to userId
   * @param con the connection
   * @param userId the user identifier
   * @return a list of ConnectionDetail
   * @throws SQLException if an error occurs
   */
  public List<ConnectionDetail> getConnectionsByUser(Connection con, String userId)
      throws SQLException {
    ArrayList<ConnectionDetail> connections;
    String query = SELECT_FROM + TABLE_NAME + " where userId = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(userId));
      rs = prepStmt.executeQuery();
      connections = new ArrayList<>();
      while (rs.next()) {
        ConnectionDetail connection = getConnectionFrom(rs);
        connections.add(connection);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return connections;
  }

  /**
   * Gets the connection from the resultSet
   * @param rs a ResultSet
   * @return the connection
   * @throws SQLException if an error occurs
   */
  protected ConnectionDetail getConnectionFrom(ResultSet rs) throws SQLException {
    ConnectionDetail connection = new ConnectionDetail();
    connection.setConnectionId(rs.getInt("connectionId"));
    connection.setUserId(rs.getString("userId"));
    connection.setComponentId(rs.getString("componentId"));
    Map<String, String> param = new HashMap<>();
    String login = rs.getString("paramLogin");
    byte[] password = rs.getBytes("paramPassword");
    ComponentInst inst = OrganizationControllerProvider.getOrganisationController()
        .getComponentInst(connection.getComponentId());
    String nameLogin = inst.getParameterValue("login");
    String namePassword = inst.getParameterValue("password");
    param.put(nameLogin, login);
    String decryptPassword;
    try {
      decryptPassword = getUncryptString(password);
    } catch (CryptoException e) {
      decryptPassword = "";
    }
    param.put(namePassword, decryptPassword);
    connection.setParam(param);

    return connection;
  }

  private static void initParam(PreparedStatement prepStmt, int id, ConnectionDetail connection)
      throws SQLException {
    prepStmt.setInt(1, id);
    prepStmt.setInt(2, Integer.parseInt(connection.getUserId()));
    prepStmt.setString(3, connection.getComponentId());
    ComponentInst inst = OrganizationControllerProvider.getOrganisationController()
        .getComponentInst(connection.getComponentId());
    String login = connection.getParam().get(inst.getParameterValue("login"));
    String password = connection.getParam().get(inst.getParameterValue("password"));
    byte[] cryptPassword = null;
    try {
      cryptPassword = getCryptString(password);
    } catch (CryptoException e) {
      SilverLogger.getLogger(ConnectionDAO.class).error(e);
    }
    prepStmt.setString(4, login);
    prepStmt.setBytes(5, cryptPassword);
  }

  private static byte[] getCryptString(String text) throws CryptoException {
    CipherFactory cipherFactory = CipherFactory.getFactory();
    Cipher blowfish = cipherFactory.getCipher(CryptographicAlgorithmName.BLOWFISH);
    try {
      return blowfish.encrypt(text, CipherKey.aKeyFromHexText(KEYCODE));
    } catch (ParseException e) {
      throw new CryptoException("The key isn't in hexadecimal: '" + KEYCODE + "'", e);
    }
  }

  private static String getUncryptString(byte[] cipherText) throws CryptoException {
    CipherFactory cipherFactory = CipherFactory.getFactory();
    Cipher blowfish = cipherFactory.getCipher(CryptographicAlgorithmName.BLOWFISH);
    try {
      return blowfish.decrypt(cipherText, CipherKey.aKeyFromHexText(KEYCODE));
    } catch (ParseException e) {
      throw new CryptoException("The key isn't in hexadecimal: '" + KEYCODE + "'", e);
    }
  }

  /**
   * Deletes all connection data linked to the given component instance.
   * @param componentInstanceId the identifier of the component instance.
   * @throws SQLException if an error occurs
   */
  public void deleteByComponentInstanceId(final String componentInstanceId) throws SQLException {
    JdbcSqlQuery.createDeleteFor(TABLE_NAME).where("componentId = ?", componentInstanceId).execute();
  }
}
