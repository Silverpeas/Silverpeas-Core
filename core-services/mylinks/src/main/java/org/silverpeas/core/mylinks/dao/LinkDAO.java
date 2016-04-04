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

package org.silverpeas.core.mylinks.dao;

import org.silverpeas.core.mylinks.model.LinkDetail;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.StringUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LinkDAO {

  private static LinkDAO linkDAO = new LinkDAO();

  /**
   * Gets the DAO instance associated to the link persistence management.
   * @return the link DAO instance.
   */
  public static LinkDAO getLinkDao() {
    return linkDAO;
  }

  /**
   * Hide constructor of utility class
   */
  private LinkDAO() {
  }

  /**
   * Deletes all links linked to the component instance represented by the given identifier.
   * @param componentInstanceId the identifier of the component instance for which the resources
   * must be deleted.
   * @throws SQLException
   */
  public static void deleteComponentInstanceData(String componentInstanceId) throws SQLException {
    JdbcSqlQuery.createDeleteFor("SB_MyLinks_Link").where("instanceId = ?", componentInstanceId)
        .or("url like ?", "%" + componentInstanceId).execute();
  }

  /**
   * Retrieve user links
   * @param con the database connection
   * @param userId the user identifier
   * @return list of user links
   * @throws SQLException
   */
  public List<LinkDetail> getAllLinksByUser(Connection con, String userId)
      throws SQLException {
    List<LinkDetail> listLink = new ArrayList<>();

    String query =
        "SELECT * FROM SB_MyLinks_Link WHERE userId = ? AND (instanceId IS NULL OR instanceId = " +
            "'') AND (objectId IS NULL OR objectId = '')";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, userId);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        LinkDetail link = recupLink(rs);
        listLink.add(link);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return listLink;
  }

  /**
   * Retrieve all user link on component instance id
   * @param con the database connection
   * @param instanceId the component instance identifier
   * @return list of LinkDetail
   * @throws SQLException
   */
  public List<LinkDetail> getAllLinksByInstance(Connection con, String instanceId)
      throws SQLException {
    List<LinkDetail> listLink = new ArrayList<>();

    String query = "select * from SB_MyLinks_Link where instanceId = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        LinkDetail link = recupLink(rs);
        listLink.add(link);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return listLink;
  }

  public List<LinkDetail> getAllLinksByObject(Connection con, String instanceId,
      String objectId) throws SQLException {
    // Retrieve all link from object
    List<LinkDetail> listLink = new ArrayList<>();

    String query = "select * from SB_MyLinks_Link where instanceId = ? and objectId = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      prepStmt.setString(2, objectId);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        LinkDetail link = recupLink(rs);
        listLink.add(link);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return listLink;
  }

  /**
   * Retrieve link from identifier
   * @param con the connection
   * @param linkId the link identifier
   * @return the link detail
   * @throws SQLException
   */
  public LinkDetail getLink(Connection con, String linkId) throws SQLException {
    LinkDetail link = new LinkDetail();
    String query = "select * from SB_MyLinks_Link where linkId = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.valueOf(linkId));
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        // convert resultset to link
        link = recupLink(rs);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return link;
  }

  /**
   * Create new link
   * @param con the connection
   * @param linkToPersist link detail to create
   * @return link identifier
   * @throws SQLException
   */
  public int createLink(Connection con, LinkDetail linkToPersist) throws SQLException {
    linkToPersist.setHasPosition(false);
    int newId = 0;
    PreparedStatement prepStmt = null;
    try {
      newId = DBUtil.getNextId("SB_MyLinks_Link", "linkId");
      // Initialize query
      String query =
          "INSERT INTO SB_MyLinks_Link (linkId, name, description, url, visible, popup, userId, " +
              "instanceId, objectId) " +
              "VALUES (?,?,?,?,?,?,?,?,?)";
      // Initialize parameters
      prepStmt = con.prepareStatement(query);
      initParam(prepStmt, newId, linkToPersist);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
    return newId;
  }

  /**
   * Update a link
   * @param con the connection
   * @param linkToUpdate link detail to update
   * @throws SQLException
   */
  public void updateLink(Connection con, LinkDetail linkToUpdate) throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      StringBuilder queryBuffer = new StringBuilder();
      queryBuffer
          .append("update SB_MyLinks_Link set linkId = ? , name = ? , description = ?, url = ? , visible = ? , popup = ? , ");
      queryBuffer
          .append("userId = ? , instanceId = ? , objectId = ? ");
      if (linkToUpdate.hasPosition()) {
        queryBuffer.append(" , position = ? ");
      }
      queryBuffer.append("where linkId = ? ");
      // initialisation des paramètres
      prepStmt = con.prepareStatement(queryBuffer.toString());
      int linkId = linkToUpdate.getLinkId();
      int paramIndex = initParam(prepStmt, linkId, linkToUpdate);
      // Initialize last parameter
      prepStmt.setInt(paramIndex, linkId);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Remove a link
   * @param con the connection
   * @param linkId the link identifier to remove
   * @throws SQLException
   */
  public void deleteLink(Connection con, String linkId) throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      String query = "delete from SB_MyLinks_Link where linkId = ? ";
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.valueOf(linkId));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  private static LinkDetail recupLink(ResultSet rs) throws SQLException {
    LinkDetail link = new LinkDetail();
    // recuperation des colonnes du resulSet et construction de l'objet LinkDetail
    int linkId = rs.getInt("linkId");
    int position = rs.getInt("position");
    boolean hasPosition = !rs.wasNull();
    String name = rs.getString("name");
    String description = rs.getString("description");
    String url = rs.getString("url");
    boolean visible = false;
    if (rs.getInt("visible") == 1) {
      visible = true;
    }
    boolean popup = false;
    if (rs.getInt("popup") == 1) {
      popup = true;
    }
    String userId = rs.getString("userId");
    String instanceId = rs.getString("instanceId");
    String objectId = rs.getString("objectId");

    link.setLinkId(linkId);
    link.setPosition(position);

    link.setHasPosition(hasPosition);
    link.setName(name);
    link.setDescription(description);
    link.setUrl(url);
    link.setVisible(visible);
    link.setPopup(popup);
    link.setUserId(userId);
    link.setInstanceId(instanceId);
    link.setObjectId(objectId);

    return link;
  }

  private static int initParam(PreparedStatement prepStmt, int linkId,
      LinkDetail link) throws SQLException {
    int i = 1;
    prepStmt.setInt(i++, linkId);
    prepStmt.setString(i++, link.getName());
    String description = StringUtil.truncate(link.getDescription(), 255);
    prepStmt.setString(i++, description);
    prepStmt.setString(i++, link.getUrl());
    if (link.isVisible()) {
      prepStmt.setInt(i++, 1);
    } else {
      prepStmt.setInt(i++, 0);
    }
    if (link.isPopup()) {
      prepStmt.setInt(i++, 1);
    } else {
      prepStmt.setInt(i++, 0);
    }
    prepStmt.setString(i++, link.getUserId());
    prepStmt.setString(i++, link.getInstanceId());
    prepStmt.setString(i++, link.getObjectId());
    if (link.hasPosition()) {
      prepStmt.setInt(i++, link.getPosition());
    }

    return i;

  }

}
