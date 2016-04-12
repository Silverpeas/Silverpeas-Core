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

package org.silverpeas.core.contribution.publication.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.contribution.publication.model.PublicationRuntimeException;

/**
 * Class declaration
 * @author
 */
public class SeeAlsoDAO {
  private static String SEEALSO_TABLENAME = "SB_SeeAlso_Link";

  /**
   * Constructor declaration
   * @see
   */
  public SeeAlsoDAO() {
  }

  /**
   * Deletes all publication to publication links associated to the component instance represented
   * by the given identifier.
   * @param componentInstanceId the identifier of the component instance for which the resources
   * must be deleted.
   * @throws SQLException
   */
  public static void deleteComponentInstanceData(String componentInstanceId) throws SQLException {
    JdbcSqlQuery.createDeleteFor(SEEALSO_TABLENAME)
        .where("objectinstanceid = ?", componentInstanceId)
        .or("targetinstanceid = ?", componentInstanceId).execute();
  }

  /**
   * Method declaration
   * @param con
   * @param infoPK
   * @param infoLink
   * @throws SQLException
   * @see
   */
  public static void addLink(Connection con, WAPrimaryKey objectPK,
      WAPrimaryKey targetPK) throws SQLException {
    int newId = -1;

    try {
      /* Recherche de la nouvelle PK de la table */
      newId = DBUtil.getNextId(SEEALSO_TABLENAME, "id");
    } catch (Exception ex) {
      throw new PublicationRuntimeException("SeeAlsoDAO.addLink()",
          SilverpeasRuntimeException.ERROR, "root.EX_GET_NEXTID_FAILED", ex);
    }

    String insertStatement = "insert into " + SEEALSO_TABLENAME
        + " values ( ? , ? , ? , ? , ? )";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(insertStatement);
      prepStmt.setInt(1, newId);
      prepStmt.setInt(2, Integer.parseInt(objectPK.getId()));
      prepStmt.setString(3, objectPK.getInstanceId());
      prepStmt.setInt(4, Integer.parseInt(targetPK.getId()));
      prepStmt.setString(5, targetPK.getInstanceId());

      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void deleteLink(Connection con, WAPrimaryKey objectPK,
      WAPrimaryKey targetPK) throws SQLException {
    String deleteStatement = "delete from "
        + SEEALSO_TABLENAME
        + " where objectId = ? AND objectInstanceId = ? AND targetId = ? AND targetInstanceId = ? ";
    PreparedStatement prepStmt = con.prepareStatement(deleteStatement);

    try {
      prepStmt.setInt(1, Integer.parseInt(objectPK.getId()));
      prepStmt.setString(2, objectPK.getInstanceId());
      prepStmt.setInt(3, Integer.parseInt(targetPK.getId()));
      prepStmt.setString(4, targetPK.getInstanceId());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void deleteLinksByObjectId(Connection con, WAPrimaryKey objectPK)
      throws SQLException {
    String deleteStatement = "delete from " + SEEALSO_TABLENAME
        + " where objectId = ? AND objectInstanceId = ? ";
    PreparedStatement prepStmt = con.prepareStatement(deleteStatement);

    try {
      prepStmt.setInt(1, Integer.parseInt(objectPK.getId()));
      prepStmt.setString(2, objectPK.getInstanceId());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void deleteLinksByTargetId(Connection con, WAPrimaryKey targetPK)
      throws SQLException {
    String deleteStatement = "delete from " + SEEALSO_TABLENAME
        + " where targetId = ? AND targetInstanceId = ? ";
    PreparedStatement prepStmt = con.prepareStatement(deleteStatement);

    try {
      prepStmt.setInt(1, Integer.parseInt(targetPK.getId()));
      prepStmt.setString(2, targetPK.getInstanceId());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Method declaration
   * @param con
   * @param infoPK
   * @return
   * @throws SQLException
   * @see
   */
  public static List<ForeignPK> getLinks(Connection con, WAPrimaryKey objectPK)
      throws SQLException {
    ResultSet rs = null;
    String selectStatement = "select targetId, targetInstanceId from "
        + SEEALSO_TABLENAME + " where objectId  = ? AND objectInstanceId = ? ";
    PreparedStatement prepStmt = con.prepareStatement(selectStatement);

    try {
      prepStmt.setInt(1, Integer.parseInt(objectPK.getId()));
      prepStmt.setString(2, objectPK.getInstanceId());
      rs = prepStmt.executeQuery();

      String targetId = "";
      String targetInstanceId = "";
      List<ForeignPK> list = new ArrayList<ForeignPK>();
      while (rs.next()) {
        targetId = Integer.toString(rs.getInt(1));
        targetInstanceId = rs.getString(2);
        ForeignPK targetPK = new ForeignPK(targetId, targetInstanceId);

        list.add(targetPK);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   * gets the publication identifiers which reference given publication
   * @param con SQL connection
   * @param objectPK publication identifier which are searching referencer
   * @return a list of publication identifier
   * @throws SQLException
   */
  public static List<ForeignPK> getReverseLinks(Connection con, WAPrimaryKey objectPK)
      throws SQLException {
    ResultSet rs = null;
    String selectStatement = "select objectId, objectInstanceId  from "
        + SEEALSO_TABLENAME + " where targetId   = ? AND targetInstanceId = ? ";
    PreparedStatement prepStmt = con.prepareStatement(selectStatement);

    try {
      prepStmt.setInt(1, Integer.parseInt(objectPK.getId()));
      prepStmt.setString(2, objectPK.getInstanceId());
      rs = prepStmt.executeQuery();

      String objectId = "";
      String objectInstanceId = "";
      List<ForeignPK> list = new ArrayList<ForeignPK>();
      while (rs.next()) {
        objectId = Integer.toString(rs.getInt(1));
        objectInstanceId = rs.getString(2);
        ForeignPK targetPK = new ForeignPK(objectId, objectInstanceId);

        list.add(targetPK);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }

  }
}