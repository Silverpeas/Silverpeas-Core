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
 * @author Norbert CHAIX
 * @version 1.0
date 26/6/2000
 */
package com.stratelia.silverpeas.peasCore;

import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.stratelia.silverpeas.authentication.LoginPasswordAuthentication;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.CompoSpace;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * The class Admin is the main class of the Administrator.<BR>
 * The role of the administrator is to create and maintain spaces.
 */
public class TestAdmin extends Object {
  static private Admin m_Admin = null;
  static private String m_Auc = null;

  // Production database
  static private String m_sWaProductionDb = "";
  static private String m_sWaProductionUser = "";
  static private String m_sWaProductionPswd = "";

  // Admin database
  static private String m_sAdminDBDriver = "";
  static private String m_sWaAdminDb = "";
  static private String m_sWaAdminUser = "";
  static private String m_sWaAdminPswd = "";

  // Administrator Login and password
  static private String m_sAdministratorLogin = "";
  static private String m_sAdministratorPassword = "";

  // Constructor
  static {
    // Get the driver and the general admin id from the property file
    ResourceLocator resources = new ResourceLocator(
        "com.stratelia.webactiv.beans.admin.admin", "");

    m_sAdminDBDriver = resources.getString("AdminDBDriver");
    m_sWaAdminDb = resources.getString("WaAdminDb");
    m_sWaAdminUser = resources.getString("WaAdminUser");
    m_sWaAdminPswd = resources.getString("WaAdminPswd");
    m_sWaProductionDb = resources.getString("WaProductionDb");
    m_sWaProductionUser = resources.getString("WaProductionUser");
    m_sWaProductionPswd = resources.getString("WaProductionPswd");
    m_sAdministratorLogin = resources.getString("AdministratorLogin");
    m_sAdministratorPassword = resources.getString("AdministratorPassword");
  }

  // -------------------------------------------------------------------------
  // Instanciate tools (space, component, profiles)
  // -------------------------------------------------------------------------

  static public CompoSpace addComponent(String sComponentName) throws Exception {
    String sComponentId = null;
    LoginPasswordAuthentication lpAuth = null;

    // Authenticate the admin if necessary
    if (m_Admin == null) {
      m_Admin = new Admin();
      lpAuth = new LoginPasswordAuthentication();
      String sKey = lpAuth.authenticate(m_sAdministratorLogin,
          m_sAdministratorPassword, "0", null);
      m_Auc = m_Admin.authenticate(sKey, "", false);
    }

    // Add a space
    SpaceInst si = new SpaceInst();
    si.setName(sComponentName);
    String sSpaceId = m_Admin.addSpaceInst(m_Auc, si);

    try {
      // Add a component
      ComponentInst ci = new ComponentInst();
      ci.setName(sComponentName);
      ci.setDomainFatherId(sSpaceId);
      sComponentId = m_Admin.addComponentInst(m_Auc, ci);
    } catch (Exception e) {
      m_Admin.deleteSpaceInstById(m_Auc, sSpaceId, true);
      throw e;
    }

    CompoSpace cs = new CompoSpace();
    cs.setSpaceId(sSpaceId);
    cs.setComponentId(sComponentId);

    return cs;
  }

  static public void deleteComponent(CompoSpace cs) throws Exception {
    try {
      // Delete space
      m_Admin.deleteSpaceInstById(m_Auc, cs.getSpaceId(), true);
    } catch (Exception e) {
      m_Admin.deleteComponentInst(m_Auc, cs.getComponentId(), true);
      throw e;
    }
  }

  static public MainSessionController getMainSessionController(CompoSpace cs)
      throws Exception {
    LoginPasswordAuthentication lpAuth = new LoginPasswordAuthentication();
    lpAuth.authenticate(m_sAdministratorLogin, m_sAdministratorPassword, "0",
        null);
    MainSessionController msc = new com.stratelia.silverpeas.peasCore.MainSessionController(
        "User1 for a test", "");
    // msc.updateUserSpace(cs.getSpaceId());
    // msc.updateUserComponent(cs.getComponentId());

    return msc;
  }

  static public MainSessionController getMainSessionController()
      throws Exception {
    LoginPasswordAuthentication lpAuth = new LoginPasswordAuthentication();
    lpAuth.authenticate(m_sAdministratorLogin, m_sAdministratorPassword, "0",
        null);
    MainSessionController msc = new com.stratelia.silverpeas.peasCore.MainSessionController(
        "User1 for a test", "");
    return msc;
  }

  // -------------------------------------------------------------------------
  // Database related operations
  // -------------------------------------------------------------------------

  // Open a Production connection
  static public Connection openProdConnection(boolean bAutoCommit)
      throws AdminException {
    try {
      Connection connection = openConnection(m_sWaProductionDb,
          m_sWaProductionUser, m_sWaProductionPswd);
      connection.setAutoCommit(bAutoCommit);
      return connection;
    } catch (Exception e) {
      if (!(e instanceof AdminException && ((AdminException) e)
          .isAlreadyPrinted()))
        SilverTrace.error("peasCore", "TestAdmin.openProdConnection",
            "peasCore.EX_CANT_CREATE_PROD_CONNECTION", e);
      throw new AdminException(true);
    }
  }

  // Open an Admin connection
  static public Connection openAdminConnection(boolean bAutoCommit)
      throws AdminException // !!! private
  {
    try {
      Connection connection = openConnection(m_sWaAdminDb, m_sWaAdminUser,
          m_sWaAdminPswd);
      connection.setAutoCommit(bAutoCommit);
      return connection;
    } catch (Exception e) {
      SilverTrace.error("peasCore", "TestAdmin.openAdminConnection",
          "peasCore.peasCore.EX_CANT_CREATE_ADMIN_CONNECTION", e);
      throw new AdminException(true);
    }
  }

  // Open a connection
  static private Connection openConnection(String sDbUrl, String sUser,
      String sPswd) throws AdminException {
    try {
      // Load the driver (registers itself)
      Class.forName(m_sAdminDBDriver);

      // Get the connection to the DB
      return DriverManager.getConnection(sDbUrl, sUser, sPswd);
    } catch (Exception e) {
      throw new AdminException(e, false);
    }
  }

  static public void closeConnection(Connection connection) {
    try {
      if (connection != null && !connection.isClosed())
        connection.close();
    } catch (SQLException e) {
      // Debug.error(200,"TestAdmin closeConnection",
      // "Cannot close connection: " + connection + "   with code: " +
      // e.getErrorCode(), e, null);
      SilverTrace.error("peasCore", "TestAdmin.closeConnection",
          "root.EX_CONNECTION_CLOSE_FAILED", connection.toString(), e);

      connection = null;
    }
  }

  // -------------------------------------------------------------------------
  // URL functions
  // -------------------------------------------------------------------------

  static public void checkURL(String sURL) throws Exception {
    // Create the Url
    URL Url = new URL(sURL);

    // open the connection and disable the cache
    URLConnection con = Url.openConnection();
    con.setUseCaches(false);

    // Get the input stream
    con.getInputStream();
  }
}