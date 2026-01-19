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
package org.silverpeas.core.contribution.publication.dao;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.contribution.publication.model.PublicationLink;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SeeAlsoDAO {
  private static final String TABLE_NAME = "SB_SeeAlso_Link";

  /**
   * Deletes all publication to publication links associated to the component instance represented
   * by the given identifier.
   * @param componentInstanceId the identifier of the component instance for which the resources
   * must be deleted.
   * @throws SQLException if the deletion fails.
   */
  public void deleteComponentInstanceData(String componentInstanceId) throws SQLException {
    JdbcSqlQuery.deleteFrom(TABLE_NAME)
        .where("objectinstanceid = ?", componentInstanceId)
        .or("targetinstanceid = ?", componentInstanceId).execute();
  }

  public void addLink(Connection con, PublicationPK objectPK,
      ResourceReference targetPK) throws SQLException {
    int newId = DBUtil.getNextId(TABLE_NAME, "id");
    String insertStatement = "insert into " + TABLE_NAME + " values ( ? , ? , ? , ? , ? )";
    try (PreparedStatement prepStmt = con.prepareStatement(insertStatement)) {
      prepStmt.setInt(1, newId);
      prepStmt.setInt(2, Integer.parseInt(objectPK.getId()));
      prepStmt.setString(3, objectPK.getInstanceId());
      prepStmt.setInt(4, Integer.parseInt(targetPK.getId()));
      prepStmt.setString(5, targetPK.getInstanceId());
      prepStmt.executeUpdate();
    }
  }

  public void deleteLink(String id) throws SQLException {
    JdbcSqlQuery.deleteFrom(TABLE_NAME).where("id = ?", Integer.parseInt(id)).execute();
  }

  public void deleteLinksByObjectId(Connection con, PublicationPK objectPK)
      throws SQLException {
    String deleteStatement = "delete from " + TABLE_NAME +
        " where objectId = ? AND objectInstanceId = ? ";
    try (PreparedStatement prepStmt = con.prepareStatement(deleteStatement)) {
      prepStmt.setInt(1, Integer.parseInt(objectPK.getId()));
      prepStmt.setString(2, objectPK.getInstanceId());
      prepStmt.executeUpdate();
    }
  }

  public void deleteLinksByTargetId(Connection con, ResourceReference targetPK)
      throws SQLException {
    String deleteStatement = "delete from " + TABLE_NAME +
        " where targetId = ? AND targetInstanceId = ? ";
    try (PreparedStatement prepStmt = con.prepareStatement(deleteStatement)) {
      prepStmt.setInt(1, Integer.parseInt(targetPK.getId()));
      prepStmt.setString(2, targetPK.getInstanceId());
      prepStmt.executeUpdate();
    }
  }

  public List<PublicationLink> getLinks(Connection con, PublicationPK pubPK) throws SQLException {
    String selectStatement = "select id, targetId, targetInstanceId from "
        + TABLE_NAME + " where objectId  = ? AND objectInstanceId = ? ";
    try (PreparedStatement prepStmt = con.prepareStatement(selectStatement)) {
      prepStmt.setInt(1, Integer.parseInt(pubPK.getId()));
      prepStmt.setString(2, pubPK.getInstanceId());
      List<PublicationLink> list = new ArrayList<>();
      try (ResultSet rs = prepStmt.executeQuery()) {
        while (rs.next()) {
          list.add(getLink(pubPK, rs));
        }
      }
      return list;
    }
  }

  /**
   * gets the publication identifiers which reference given publication
   * @param con SQL connection
   * @param pubPK publication identifier which are searching referencer
   * @return a list of publication identifier
   * @throws SQLException if the links fetching fails
   */
  public List<PublicationLink> getReverseLinks(Connection con, PublicationPK pubPK)
      throws SQLException {
    String selectStatement = "select id, objectId, objectInstanceId  from "
        + TABLE_NAME + " where targetId   = ? AND targetInstanceId = ? ";
    try (PreparedStatement prepStmt = con.prepareStatement(selectStatement)) {
      prepStmt.setInt(1, Integer.parseInt(pubPK.getId()));
      prepStmt.setString(2, pubPK.getInstanceId());
      List<PublicationLink> list = new ArrayList<>();
      try (ResultSet rs = prepStmt.executeQuery()) {
        while (rs.next()) {
          PublicationLink link = getLink(pubPK, rs);
          link.setReverse(true);
          list.add(link);
        }
      }
      return list;
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