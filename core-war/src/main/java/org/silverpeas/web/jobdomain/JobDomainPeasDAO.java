/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.jobdomain;

import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.ServiceProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Class declaration Get connections data from database
 *
 */
public class JobDomainPeasDAO {

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  private static Connection getConnection() {
    try {
      return DBUtil.openConnection();
    } catch (Exception e) {
      throw new JobDomainPeasRuntimeException("JobDomainPeasDAO.getConnection()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", "", e);
    }
  }

  /**
   * Sélection des groupes à synchroniser en insert ou update de la table Domain<domainName>_Group
   *
   * @param domain
   * @return Collection de Group
   * @throws SQLException
   * @see
   */
  public static Collection<Group> selectGroupSynchroInsertUpdateTableDomain_Group(Domain domain)
      throws SQLException {
    String propDomainFileName = domain.getPropFileName();
    String domainName = propDomainFileName.substring(39);

    AdminController adminCtrl = ServiceProvider.getService(AdminController.class);

    // sélectionne les users dans Silverpeas
    Collection<Group> listRes = new ArrayList<Group>();

    String query = " SELECT g.id FROM Domain" + domainName
        + "_Group d, ST_Group g WHERE g.domainId = " + domain.getId()
        + " AND g.specificId = CAST(d.id AS varchar)";

    Statement stmt = null;
    ResultSet resultSet = null;
    Connection myCon = getConnection();
    try {
      stmt = myCon.createStatement();
      resultSet = stmt.executeQuery(query);
      while (resultSet.next()) {
        String groupId = String.valueOf(resultSet.getInt(1));
        Group group = adminCtrl.getGroupById(groupId);
        if (!group.isSynchronized()) {
          listRes.add(group);
        }
      }
    } finally {
      DBUtil.close(resultSet, stmt);
      DBUtil.close(myCon);
    }

    return listRes; // Collection de Group
  }

  /**
   * Sélection des utilisateurs à synchroniser en insert ou update de la table
   * Domain<domainName>_User
   *
   * @param domain
   * @return Collection de UserFull
   * @throws SQLException
   */
  public static Collection<UserFull> selectUserSynchroInsertUpdateTableDomain_User(
      Domain domain) throws SQLException {
    String propDomainFileName = domain.getPropFileName();
    String domainName = propDomainFileName.substring(39);

    AdminController adminCtrl = ServiceProvider.getService(AdminController.class);

    // sélectionne les users dans Silverpeas
    Collection<UserFull> listRes = new ArrayList<UserFull>();
    UserFull userFull;

    String query = " SELECT u.id FROM Domain" + domainName
        + "_User d, ST_User u WHERE u.domainId = " + domain.getId()
        + " AND u.specificId = CAST(d.id AS varchar)";

    Statement stmt = null;
    ResultSet resultSet = null;
    Connection myCon = getConnection();
    try {
      stmt = myCon.createStatement();
      resultSet = stmt.executeQuery(query);

      while (resultSet.next()) {
        String userId = String.valueOf(resultSet.getInt(1));
        userFull = adminCtrl.getUserFull(userId);

        listRes.add(userFull);
      }
    } finally {
      DBUtil.close(resultSet, stmt);
      DBUtil.close(myCon);
    }
    return listRes; // Collection de UserFull
  }

  /**
   * Sélection des utilisateurs à synchroniser en delete de la table Domain<domainName>_User
   *
   * @return Collection de UserDetail
   * @throws SQLException
   * @see
   */
  public static Collection<UserDetail> selectUserSynchroDeleteTableDomain_User(Domain domain)
      throws SQLException {
    AdminController adminCtrl = ServiceProvider.getService(AdminController.class);

    // sélectionne les users dans Silverpeas
    Collection<UserDetail> listRes = new ArrayList<UserDetail>();
    PreparedStatement stmt = null;
    ResultSet resultSet = null;
    Connection myCon = getConnection();
    try {
      stmt = myCon.prepareStatement(
          "SELECT id FROM ST_User WHERE domainId =  ? AND  state = 'DELETED'");
      stmt.setInt(1, Integer.parseInt(domain.getId()));
      resultSet = stmt.executeQuery();
      while (resultSet.next()) {
        listRes.add(adminCtrl.getUserDetail(resultSet.getString(1)));
      }
    } finally {
      DBUtil.close(resultSet, stmt);
      DBUtil.close(myCon);
    }
    return listRes;
  }
}