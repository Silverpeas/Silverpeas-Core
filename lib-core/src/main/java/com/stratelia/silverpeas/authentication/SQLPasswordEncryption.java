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
/*
 * SQLPasswordEncryption.java
 *
 * Created on 6 aout 2001
 */

package com.stratelia.silverpeas.authentication;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import com.silverpeas.util.cryptage.UnixMD5Crypt;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * This class performs the authentification using an SQL table
 * @author tleroi
 * @version
 */
public class SQLPasswordEncryption {
  protected static String m_JDBCUrl;
  protected static String m_AccessLogin;
  protected static String m_AccessPasswd;
  protected static String m_DriverClass;
  protected static String m_UserTableName;
  protected static String m_UserLoginColumnName;
  protected static String m_UserPasswordColumnName;
  protected static String m_UserPasswordAvailableColumnName;
  protected static String m_PasswordEncryption;

  protected static Connection m_Connection;

  public static void init(String authenticationServerName,
      ResourceLocator propFile) {
    // Lecture du fichier de proprietes
    m_JDBCUrl = propFile.getString(authenticationServerName + ".SQLJDBCUrl");
    m_AccessLogin = propFile.getString(authenticationServerName
        + ".SQLAccessLogin");
    m_AccessPasswd = propFile.getString(authenticationServerName
        + ".SQLAccessPasswd");
    m_DriverClass = propFile.getString(authenticationServerName
        + ".SQLDriverClass");
    m_UserTableName = propFile.getString(authenticationServerName
        + ".SQLUserTableName");
    m_UserLoginColumnName = propFile.getString(authenticationServerName
        + ".SQLUserLoginColumnName");
    m_UserPasswordColumnName = propFile.getString(authenticationServerName
        + ".SQLUserPasswordColumnName");
    m_UserPasswordAvailableColumnName = propFile
        .getString(authenticationServerName
        + ".SQLUserPasswordAvailableColumnName");
    m_PasswordEncryption = propFile.getString(authenticationServerName
        + ".SQLPasswordEncryption");
  }

  protected static void openConnection() throws SQLException,
      InstantiationException, IllegalAccessException, ClassNotFoundException {
    Properties info = new Properties();
    Driver driverSQL;

    info.setProperty("user", m_AccessLogin);
    info.setProperty("password", m_AccessPasswd);
    driverSQL = (Driver) Class.forName(m_DriverClass).newInstance();
    m_Connection = driverSQL.connect(m_JDBCUrl, info);
  }

  protected static void closeConnection() throws SQLException {
    if (m_Connection != null) {
      m_Connection.close();
      m_Connection = null;
    }
    m_Connection = null;
  }

  public static void main(String args[]) {
    int nbServers;
    int i;
    String serverName;
    String className;
    ResourceLocator propFile = new ResourceLocator(
        "com.stratelia.silverpeas.authentication.autDomainSP", "");

    nbServers = Integer.parseInt(propFile.getString("autServersCount"));
    for (i = 0; i < nbServers; i++) {
      serverName = "autServer" + Integer.toString(i);
      try {
        className = propFile.getString(serverName + ".type");
        if ((getBooleanProperty(propFile, serverName + ".enabled", true))
            && (className != null)
            && (className
            .equalsIgnoreCase("com.stratelia.silverpeas.authentication.AuthenticationSQL"))) {
          init(serverName, propFile);
          openConnection();
          cryptPasswords();
          closeConnection();
        }
      } catch (Exception ex) {
        System.out.println("Cant instanciate class for server " + serverName
            + " : " + ex.getMessage());
      }
    }
  }

  protected static void cryptPasswords() {
    ResultSet rs = null;
    Statement stmt = null;
    PreparedStatement stmtUpdate = null;
    String sUpdateStart = "UPDATE " + m_UserTableName + " SET "
        + m_UserPasswordColumnName + " = '";
    String sUpdateMiddle = "' WHERE id=";
    String sClearPass;

    try {
      System.out.println("DatabaseURL : " + m_JDBCUrl);
      System.out.println("Table : " + m_UserTableName);
      System.out.println("Encryption type : " + m_PasswordEncryption);
      if (m_PasswordEncryption.equals("CryptUnix")) {
        System.out.println("Encrypting passwords....");
        stmt = m_Connection.createStatement();
        rs = stmt.executeQuery("SELECT * FROM " + m_UserTableName + "");
        while (rs.next()) {
          sClearPass = rs.getString(m_UserPasswordColumnName);
          if (sClearPass == null) {
            sClearPass = "";
          }
          stmtUpdate = m_Connection.prepareStatement(sUpdateStart
              + UnixMD5Crypt.crypt(sClearPass) + sUpdateMiddle
              + rs.getString("id"));
          stmtUpdate.executeUpdate();
          stmtUpdate.close();
          stmtUpdate = null;
        }
        System.out.println("Encryption Done !");
      }
    } catch (SQLException ex) {
      System.out.println("Error during password Crypting : " + ex.getMessage());
    } finally {
      try {
        if (rs != null) {
          rs.close();
          rs = null;
        }
        if (stmt != null) {
          stmt.close();
          stmt = null;
        }
        if (stmtUpdate != null) {
          stmtUpdate.close();
          stmtUpdate = null;
        }
      } catch (SQLException ex) {
      }
    }
  }

  protected static boolean getBooleanProperty(ResourceLocator resources,
      String propertyName, boolean defaultValue) {
    String value;
    boolean valret = defaultValue;

    value = resources.getString(propertyName);
    if (value != null) {
      if (value.equalsIgnoreCase("true")) {
        valret = true;
      } else {
        valret = false;
      }
    }
    return valret;
  }
}
