/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

/*
 * @author Ludovic BERTIN
 * @version 1.0
 * date 21/9/2001
 */

package com.stratelia.silverpeas.authentication;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.Domain;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * The class AuthenticationServlet is called to authenticate user in Silverpeas
 */
public class LoginPasswordAuthentication {
  static final protected String m_JDBCUrl;
  static final protected String m_AccessLogin;
  static final protected String m_AccessPasswd;
  static final protected String m_DriverClass;
  static final protected String m_DomainTableName;
  static final protected String m_DomainIdColumnName;
  static final protected String m_DomainNameColumnName;
  static final protected String m_DomainAuthenticationServerColumnName;
  static final protected String m_KeyStoreTableName;
  static final protected String m_KeyStoreKeyColumnName;
  static final protected String m_KeyStoreLoginColumnName;
  static final protected String m_KeyStoreDomainIdColumnName;
  static protected Hashtable<String, String> m_Domains = new Hashtable<String, String>();
  static protected List<Domain> domains = new ArrayList<Domain>();
  static protected List<String> m_DomainsIds = new ArrayList<String>();
  static final protected String m_UserTableName;
  static final protected String m_UserIdColumnName;
  static final protected String m_UserLoginColumnName;
  static final protected String m_UserDomainColumnName;

  static protected int m_AutoInc = 1;

  private final static Admin admin = new Admin();

  static {
    ResourceLocator propFile = new ResourceLocator(
        "com.stratelia.silverpeas.authentication.domains", "");

    // Lecture du fichier de proprietes
    m_JDBCUrl = propFile.getString("SQLDomainJDBCUrl");
    m_AccessLogin = propFile.getString("SQLDomainAccessLogin");
    m_AccessPasswd = propFile.getString("SQLDomainAccessPasswd");
    m_DriverClass = propFile.getString("SQLDomainDriverClass");

    m_DomainTableName = propFile.getString("SQLDomainTableName");
    m_DomainIdColumnName = propFile.getString("SQLDomainIdColumnName");
    m_DomainNameColumnName = propFile.getString("SQLDomainNameColumnName");
    m_DomainAuthenticationServerColumnName =
        propFile.getString("SQLDomainAuthenticationServerColumnName");

    m_KeyStoreTableName = propFile.getString("SQLKeyStoreTableName");
    m_KeyStoreKeyColumnName = propFile.getString("SQLKeyStoreKeyColumnName");
    m_KeyStoreLoginColumnName = propFile.getString("SQLKeyStoreLoginColumnName");
    m_KeyStoreDomainIdColumnName = propFile.getString("SQLKeyStoreDomainIdColumnName");

    m_UserTableName = propFile.getString("SQLUserTableName");
    m_UserIdColumnName = propFile.getString("SQLUserIdColumnName");
    m_UserLoginColumnName = propFile.getString("SQLUserLoginColumnName");
    m_UserDomainColumnName = propFile.getString("SQLUserDomainColumnName");

    initDomains();
  }

  /**
   * Constructor
   */
  public LoginPasswordAuthentication() {
  }

  /**
   * Opens a new connection to Silverpeas database
   */
  static protected Connection openConnection() throws AuthenticationException {
    Properties info = new Properties();
    Driver driverSQL = null;
    Connection con;

    try {
      info.setProperty("user", m_AccessLogin);
      info.setProperty("password", m_AccessPasswd);
      driverSQL = (Driver) Class.forName(m_DriverClass).newInstance();
    } catch (Exception iex) {
      throw new AuthenticationHostException("LoginPasswordAuthentication.openConnection()",
          SilverpeasException.ERROR, "root.EX_CANT_INSTANCIATE_DB_DRIVER", "Driver=" +
              m_DriverClass, iex);
    }
    try {
      con = driverSQL.connect(m_JDBCUrl, info);
    } catch (SQLException ex) {
      throw new AuthenticationHostException("LoginPasswordAuthentication.openConnection()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", "JDBCUrl=" + m_JDBCUrl, ex);
    }
    return con;
  }

  /**
   * Close connection to Silverpeas database
   */
  static protected void closeConnection(Connection con) {
    DBUtil.close(con);
  }

  /**
   * Get list of domains
   * @return hashtable object (keys=domain ids, values=domain name)
   * @deprecated use getListDomains method instead
   */
  public Hashtable<String, String> getAllDomains() {
    return m_Domains;
  }

  /**
   * Use this method instead of m_Domains Hashtable class attributes
   * @return a list of domains
   */
  public List<Domain> getListDomains() {
    return domains;
  }

  public List<String> getDomainsIds() {
    return m_DomainsIds;
  }

  static public void initDomains() {
    m_DomainsIds.clear();
    m_Domains.clear();
    domains.clear();
    try {
      Domain[] allDomains = admin.getAllDomains();
      for (Domain domain : allDomains) {
        m_Domains.put(domain.getId(), domain.getName());
        m_DomainsIds.add(domain.getId());
        domains.add(domain);
      }
    } catch (AdminException e) {
      SilverTrace.error("authentication", "LoginPasswordAuthentication",
          "Problem to retrieve all the domains", e);
    }
  }

  /**
   * Main method that authenticates given user and return an authantication key
   * @param login User login
   * @param password User password
   * @param domainId User domain Id
   * @return authentication key used by LoginServlet
   */
  public String authenticate(String login, String password, String domainId,
      HttpServletRequest request) {
    // Test data coming from calling page
    if (login == null || password == null || domainId == null) {
      return null;
    }

    Connection m_Connection = null;
    try {
      // Open connection
      m_Connection = openConnection();

      AuthenticationServer authenticationServer = getAuthenticationServer(m_Connection, domainId);

      // Authentification test
      authenticationServer.authenticate(login, password, request);

      // Generate a random key and store it in database
      String key = getAuthenticationKey(login, domainId);

      if (request != null) {
        // Store information about password change capabilities in HTTP session
        HttpSession session = request.getSession();
        session.setAttribute(Authentication.PASSWORD_CHANGE_ALLOWED,
            (authenticationServer.isPasswordChangeAllowed()) ? "yes" : "no");
      }

      return key;
    } catch (AuthenticationException ex) {
      SilverTrace.warn("authentication",
          "LoginPasswordAuthentication.authenticate()",
          "authentication.EX_USER_REJECTED", "DomainId=" + domainId + ";User="
              + login, ex);
      String errorCause = "Error_2";
      Exception nested = ex.getNested();
      if (nested != null) {
        if (nested instanceof AuthenticationException) {
          ex = (AuthenticationException) nested;
        }
      }
      if (ex instanceof AuthenticationBadCredentialException) {
        errorCause = "Error_1";
      } else if (ex instanceof AuthenticationHostException) {
        errorCause = "Error_2";
      } else if (ex instanceof AuthenticationPwdNotAvailException) {
        errorCause = "Error_5";
      }
      return errorCause;
    } finally {
      closeConnection(m_Connection);
    }
  }

  /**
   * Method that authenticates given user and return an authentication key Used in case of ntlm
   * authentication
   * @param login User login
   * @param domainId User domain Id
   * @return authentication key used by LoginServlet
   */
  public String authenticate(String login, String domainId, HttpServletRequest request) {
    // Test data coming from calling page
    if (login == null || domainId == null) {
      return null;
    }

    PreparedStatement prepStmt = null;
    ResultSet resultSet = null;
    Connection m_Connection = null;
    boolean authenticationOK = false;
    try {
      // Open connection
      m_Connection = openConnection();

      String query = "select " + m_UserIdColumnName + " from "
          + m_UserTableName + " where " + m_UserLoginColumnName + " = ? and "
          + m_UserDomainColumnName + " = ?";
      prepStmt = m_Connection.prepareStatement(query);

      prepStmt.setString(1, login);
      prepStmt.setInt(2, Integer.parseInt(domainId));

      resultSet = prepStmt.executeQuery();

      authenticationOK = resultSet.next();
    } catch (Exception ex) {
      SilverTrace.warn("authentication", "LoginPasswordAuthentication.authenticate()",
          "authentication.EX_USER_REJECTED", "DomainId=" + domainId + ";User=" + login, ex);
      String errorCause = "Error_2";
      return errorCause;
    } finally {
      DBUtil.close(resultSet, prepStmt);
      closeConnection(m_Connection);
    }

    String key = null;

    if (authenticationOK) {
      // Generate a random key and store it in database
      try {
        key = getAuthenticationKey(login, domainId);
      } catch (Exception e) {
        SilverTrace.warn("authentication", "LoginPasswordAuthentication.authenticate()",
            "authentication.EX_CANT_GET_AUTHENTICATION_KEY", "DomainId=" + domainId + ";User=" +
                login, e);
        String errorCause = "Error_2";
        return errorCause;
      }
    }

    return key;
  }

  /**
   * Main method that change user password
   * @param login User login
   * @param oldPassword User old password
   * @param newPassword User new password
   * @param domainId User domain Id
   * @throws AuthenticationException
   */
  public void changePassword(String login, String oldPassword,
      String newPassword, String domainId) throws AuthenticationException {
    // Test data coming from calling page
    if (login == null || oldPassword == null || domainId == null
        || newPassword == null) {
      throw new AuthenticationBadCredentialException("LoginPasswordAuthentication.changePassword",
          SilverpeasException.ERROR, "authentication.EX_NULL_VALUE_DETECTED");
    }

    Connection m_Connection = null;
    try {
      // Open connection
      m_Connection = openConnection();

      AuthenticationServer authenticationServer = getAuthenticationServer(m_Connection, domainId);
      
      // Authentification test
      authenticationServer.changePassword(login, oldPassword, newPassword);
    } catch (AuthenticationException ex) {
      SilverTrace.error("authentication", "LoginPasswordAuthentication.changePassword()",
          "authentication.EX_USER_REJECTED", "DomainId=" + domainId + ";User=" + login, ex);
      throw ex;
    } finally {
      closeConnection(m_Connection);
    }
  }

  /**
   * Get the Authentication Server name for the given domain
   * @param domainId Domain Id
   * @return authentication server name
   */
  private String getAuthenticationServerName(Connection con, String domainId)
      throws AuthenticationException {
    Statement stmt = null;
    ResultSet rs = null;
    String query = "SELECT " + m_DomainAuthenticationServerColumnName
        + " FROM " + m_DomainTableName + " WHERE " + m_DomainIdColumnName
        + " = " + domainId + "";

    SilverTrace.info("authentication", "LoginPasswordAuthentication.getAuthenticationServerName()",
        "root.MSG_GEN_PARAM_VALUE", "query=" + query);
    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(query);
      if (rs.next()) {
        String serverName = rs.getString(m_DomainAuthenticationServerColumnName);
        if (!StringUtil.isDefined(serverName)) {
          throw new AuthenticationException(
              "LoginPasswordAuthentication.getAuthenticationServerName()",
              SilverpeasException.ERROR, "authentication.EX_SERVER_NOT_FOUND",
              "DomainId=" + domainId);
        } else {
          return serverName;
        }
      } else {
        throw new AuthenticationException(
            "LoginPasswordAuthentication.getAuthenticationServerName()",
            SilverpeasException.ERROR, "authentication.EX_DOMAIN_NOT_FOUND",
            "DomainId=" + domainId);
      }
    } catch (SQLException ex) {
      throw new AuthenticationException(
          "LoginPasswordAuthentication.getAuthenticationServerName()",
          SilverpeasException.ERROR, "authentication.EX_DOMAIN_INFO_ERROR",
          "DomainId=" + domainId);
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  /**
   * Build a random authentication key, store it in Silverpeas database and return it.
   * @param login user login
   * @param domainId user domain id
   * @return generated authentication key
   */
  public String getAuthenticationKey(String login, String domainId)
      throws AuthenticationException {
    // Random key generation
    long nStart = login.hashCode() * new Date().getTime() * (m_AutoInc++);
    Random rand = new Random(nStart);
    int key = rand.nextInt();

    String sKey = String.valueOf(key);

    storeAuthenticationKey(login, domainId, sKey);

    return sKey;
  }

  public void storeAuthenticationKey(String login, String domainId, String sKey)
      throws AuthenticationException {
    Statement stmt = null;
    int key = Integer.parseInt(sKey);

    String query = "INSERT INTO " + m_KeyStoreTableName + "("
        + m_KeyStoreKeyColumnName + ", " + m_KeyStoreLoginColumnName + ", "
        + m_KeyStoreDomainIdColumnName + ")" + " VALUES (" + key + ", '"
        + login + "', " + domainId + ")";

    Connection m_Connection = null;
    try {
      m_Connection = openConnection();

      stmt = m_Connection.createStatement();
      stmt.execute(query);
      SilverTrace.info("authentication", "LoginPasswordAuthentication.storeAuthenticationKey()",
          "root.MSG_GEN_PARAM_VALUE", "query=" + query);
    } catch (SQLException ex) {
      SilverTrace.error("authentication", "LoginPasswordAuthentication.storeAuthenticationKey()",
          "authentication.EX_WRITE_KEY_ERROR", "User=" + login + " exception=" + ex.getSQLState());
    } finally {
      DBUtil.close(stmt);
      closeConnection(m_Connection);
    }
  }

  public void resetPassword(String login, String newPassword, String domainId)
      throws AuthenticationException {
    // Test data coming from calling page
    if (login == null || domainId == null || newPassword == null) {
      throw new AuthenticationBadCredentialException("LoginPasswordAuthentication.resetPassword",
          SilverpeasException.ERROR, "authentication.EX_NULL_VALUE_DETECTED");
    }

    Connection connection = null;

    try {
      // Open connection
      connection = openConnection();

      // Build a AuthenticationServer instance
      AuthenticationServer authenticationServer = getAuthenticationServer(connection, domainId);
      
      // Authentification test
      authenticationServer.resetPassword(login, newPassword);
    } catch (AuthenticationException ex) {
      SilverTrace.error("authentication", "LoginPasswordAuthentication.resetPassword()",
          "authentication.EX_USER_REJECTED", "DomainId=" + domainId + ";User=" + login, ex);
      throw ex;
    } finally {
      closeConnection(connection);
    }
  }

  public boolean isPasswordChangeAllowed(String domainId) {
    Connection m_Connection = null;
    try {
      // Open connection
      m_Connection = openConnection();

      // Build a AuthenticationServer instance
      AuthenticationServer authenticationServer = getAuthenticationServer(m_Connection, domainId);

      return authenticationServer.isPasswordChangeAllowed();
    } catch (AuthenticationException ex) {
      SilverTrace.error("authentication", "LoginPasswordAuthentication.isPasswordChangeAllowed()",
          "authentication.EX_AUTHENTICATION_STATUS_ERROR", "DomainId=" + domainId + " exception=" +
              ex.getMessage());
    } finally {
      closeConnection(m_Connection);
    }
    return false;
  }
  
  private AuthenticationServer getAuthenticationServer(Connection con, String domainId)
      throws AuthenticationException {
    // Get authentication server name
    String authenticationServerName = getAuthenticationServerName(con, domainId);

    // Build a AuthenticationServer instance
    return new AuthenticationServer(authenticationServerName);
  }

}