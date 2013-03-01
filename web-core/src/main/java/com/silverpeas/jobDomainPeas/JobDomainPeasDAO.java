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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.jobDomainPeas;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.Domain;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * Class declaration Get connections data from database
 * @author
 */
public class JobDomainPeasDAO {

  private static final String DB_NAME = JNDINames.SILVERPEAS_DATASOURCE;

  /**
   * Method declaration
   * @return
   * @see
   */
  private static Connection getConnection() {
    try {
      return DBUtil.makeConnection(DB_NAME);
    } catch (Exception e) {
      throw new JobDomainPeasRuntimeException(
          "JobDomainPeasDAO.getConnection()", SilverpeasException.ERROR,
          "root.EX_CONNECTION_OPEN_FAILED", "DbName=" + DB_NAME, e);
    }
  }

  /**
   * Sélection des groupes à synchroniser en insert ou update de la table Domain<domainName>_Group
   * @param domainName
   * @return Collection de Group
   * @throws SQLException
   * @see
   */
  public static Collection<Group> selectGroupSynchroInsertUpdateTableDomain_Group(
      Domain domain) throws SQLException {
    SilverTrace.info("jobDomainPeas",
        "JobDomainPeasDAO.selectGroupSynchroInsertUpdateTableDomain_Group",
        "root.MSG_GEN_ENTER_METHOD");

    String propDomainFileName = domain.getPropFileName();
    String domainName = propDomainFileName.substring(39);

    AdminController adminCtrl = new AdminController(null);

    // sélectionne les users dans Silverpeas
    Collection<Group> listRes = new ArrayList<Group>();

    String query = " SELECT g.id" + " FROM Domain" + domainName
        + "_Group d, ST_Group g " + " WHERE g.domainId = " + domain.getId()
        + " AND g.specificId = CAST(d.id AS varchar)";

    Statement stmt = null;
    Connection myCon = getConnection();
    try {
      stmt = myCon.createStatement();
      ResultSet resultSet = stmt.executeQuery(query);

      String groupId;
      Group group;
      while (resultSet.next()) {
        groupId = String.valueOf(resultSet.getInt(1));
        group = adminCtrl.getGroupById(groupId);
        if (!group.isSynchronized())
          listRes.add(group);
      }
    } finally {
      DBUtil.close(stmt);
      DBUtil.close(myCon);
    }

    return listRes; // Collection de Group
  }

  /**
   * Sélection des utilisateurs à synchroniser en insert ou update de la table
   * Domain<domainName>_User
   * @param domain
   * @return Collection de UserFull
   * @throws SQLException
   */
  public static Collection<UserFull> selectUserSynchroInsertUpdateTableDomain_User(
      Domain domain) throws SQLException {
    SilverTrace.info("jobDomainPeas",
        "JobDomainPeasDAO.selectUserSynchroInsertUpdateTableDomain_User",
        "root.MSG_GEN_ENTER_METHOD");

    String propDomainFileName = domain.getPropFileName();
    String domainName = propDomainFileName.substring(39);

    AdminController adminCtrl = new AdminController(null);

    // sélectionne les users dans Silverpeas
    Collection<UserFull> listRes = new ArrayList<UserFull>();
    UserFull userFull;

    String query = " SELECT u.id" + " FROM Domain" + domainName
        + "_User d, ST_User u " + " WHERE u.domainId = " + domain.getId()
        + " AND u.specificId = CAST(d.id AS varchar)";

    Statement stmt = null;
    Connection myCon = getConnection();
    try {
      stmt = myCon.createStatement();
      ResultSet resultSet = stmt.executeQuery(query);

      String userId;
      while (resultSet.next()) {
        userId = String.valueOf(resultSet.getInt(1));
        userFull = adminCtrl.getUserFull(userId);

        listRes.add(userFull);
      }
    } finally {
      DBUtil.close(stmt);
      DBUtil.close(myCon);
    }
    return listRes; // Collection de UserFull
  }

  /**
   * Sélection des utilisateurs à synchroniser en delete de la table Domain<domainName>_User
   * @param domainName
   * @return Collection de UserDetail
   * @throws SQLException
   * @see
   */
  public static Collection<UserDetail> selectUserSynchroDeleteTableDomain_User(Domain domain)
      throws SQLException {
    SilverTrace.info("jobDomainPeas",
        "JobDomainPeasDAO.selectUserSynchroDeleteTableDomain_User",
        "root.MSG_GEN_ENTER_METHOD");

    AdminController adminCtrl = new AdminController(null);

    // sélectionne les users dans Silverpeas
    Collection<UserDetail> listRes = new ArrayList<UserDetail>();
    UserDetail userDetail;

    String query = " SELECT id " + " FROM ST_User " + " WHERE domainId = "
        + domain.getId() + " AND state = 'DELETED'";

    Statement stmt = null;
    Connection myCon = getConnection();
    try {
      stmt = myCon.createStatement();
      ResultSet resultSet = stmt.executeQuery(query);

      String userId;
      while (resultSet.next()) {
        userId = resultSet.getString(1);
        userDetail = adminCtrl.getUserDetail(userId);
        listRes.add(userDetail);
      }
    } finally {
      DBUtil.close(stmt);
      DBUtil.close(myCon);
    }

    return listRes; // Collection de UserDetail
  }
}