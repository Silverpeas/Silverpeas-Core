/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.publication.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.silverpeas.core.contribution.publication.model.PublicationLink;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.persistence.jdbc.DBUtil;

/**
 * Class declaration
 * @author
 */
public class SeeAlsoDAO {
  private static final String SEEALSO_TABLENAME = "SB_SeeAlso_Link";

  private SeeAlsoDAO() {

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
   * @param objectPK
   * @param targetPK
   * @throws SQLException
   *
   */
  public static void addLink(Connection con, PublicationPK objectPK,
      ResourceReference targetPK) throws SQLException {
    int newId = DBUtil.getNextId(SEEALSO_TABLENAME, "id");

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

  public static void deleteLink(String id) throws SQLException {
    JdbcSqlQuery.createDeleteFor(SEEALSO_TABLENAME).where("id = ?", Integer.parseInt(id)).execute();
  }

  public static void deleteLinksByObjectId(Connection con, PublicationPK objectPK)
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

  public static void deleteLinksByTargetId(Connection con, ResourceReference targetPK)
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
   * @param pubPK
   * @return
   * @throws SQLException
   *
   */
  public static List<PublicationLink> getLinks(Connection con, PublicationPK pubPK) throws SQLException {
    ResultSet rs = null;
    String selectStatement = "select id, targetId, targetInstanceId from "
        + SEEALSO_TABLENAME + " where objectId  = ? AND objectInstanceId = ? ";
    PreparedStatement prepStmt = con.prepareStatement(selectStatement);

    try {
      prepStmt.setInt(1, Integer.parseInt(pubPK.getId()));
      prepStmt.setString(2, pubPK.getInstanceId());
      rs = prepStmt.executeQuery();

      List<PublicationLink> list = new ArrayList<>();
      while (rs.next()) {
        list.add(getLink(pubPK, rs));
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   * gets the publication identifiers which reference given publication
   * @param con SQL connection
   * @param pubPK publication identifier which are searching referencer
   * @return a list of publication identifier
   * @throws SQLException
   */
  public static List<PublicationLink> getReverseLinks(Connection con, PublicationPK pubPK)
      throws SQLException {
    ResultSet rs = null;
    String selectStatement = "select id, objectId, objectInstanceId  from "
        + SEEALSO_TABLENAME + " where targetId   = ? AND targetInstanceId = ? ";
    PreparedStatement prepStmt = con.prepareStatement(selectStatement);

    try {
      prepStmt.setInt(1, Integer.parseInt(pubPK.getId()));
      prepStmt.setString(2, pubPK.getInstanceId());
      rs = prepStmt.executeQuery();

      List<PublicationLink> list = new ArrayList<>();
      while (rs.next()) {
        PublicationLink link = getLink(pubPK, rs);
        link.setReverse(true);
        list.add(link);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  private static PublicationLink getLink(PublicationPK pubPK, ResultSet rs) throws SQLException {
    String id = Integer.toString(rs.getInt(1));
    String targetId = Integer.toString(rs.getInt(2));
    String targetInstanceId = rs.getString(3);
    ResourceReference targetPK = new ResourceReference(targetId, targetInstanceId);

    return new PublicationLink(id, pubPK, targetPK);
  }
}