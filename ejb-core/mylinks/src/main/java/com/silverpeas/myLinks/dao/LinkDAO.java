/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

package com.silverpeas.myLinks.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.silverpeas.myLinks.model.LinkDetail;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.exception.UtilException;

public class LinkDAO {

  /**
   * Hide constructor of utility class
   */
  private LinkDAO() {
    super();
  }

  public static List<LinkDetail> getAllLinksByUser(Connection con, String userId)
      throws SQLException {
    // récupérer toutes les liens d'un utilisateur
    List<LinkDetail> listLink = new ArrayList<LinkDetail>();

    String query =
        "select * from SB_MyLinks_Link where userId = ? and (instanceId IS NULL or instanceId = '') and (objectId IS NULL or objectId = '') order by position asc nulls first";
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
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return listLink;
  }

  public static List<LinkDetail> getAllLinksByInstance(Connection con,
      String instanceId) throws SQLException {
    // récupérer toutes les liens d'un utilisateur sur un composant
    List<LinkDetail> listLink = new ArrayList<LinkDetail>();

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
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return listLink;
  }

  public static List<LinkDetail> getAllLinksByObject(Connection con,
      String instanceId, String objectId) throws SQLException {
    // récupérer toutes les liens d'un objet
    List<LinkDetail> listLink = new ArrayList<LinkDetail>();

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
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return listLink;
  }

  public static LinkDetail getLink(Connection con, String linkId)
      throws SQLException {
    // récupérer le lien
    LinkDetail link = new LinkDetail();
    String query = "select * from SB_MyLinks_Link where linkId = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.valueOf(linkId));
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        // recuperation des colonnes du resulSet et construction de
        // l'objet lien
        link = recupLink(rs);
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return link;
  }

  public static int createLink(Connection con, LinkDetail link)
      throws SQLException, UtilException {
    // Création d'un nouveau lien
    LinkDetail newLink = link;
    newLink.setHasPosition(false);
    int newId = 0;
    PreparedStatement prepStmt = null;
    try {
      newId = DBUtil.getNextId("SB_MyLinks_Link", "linkId");
      // création de la requete
      String query =
          "insert into SB_MyLinks_Link (linkId, name, description, url, visible, popup, userId, instanceId, objectId) "
              + "values (?,?,?,?,?,?,?,?,?)";
      // initialisation des paramètres
      prepStmt = con.prepareStatement(query);
      initParam(prepStmt, newId, newLink);
      prepStmt.executeUpdate();
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
    return newId;
  }

  public static void updateLink(Connection con, LinkDetail link)
      throws SQLException {
    LinkDetail updatedLink = link;
    PreparedStatement prepStmt = null;
    try {
      StringBuffer queryBuffer = new StringBuffer();
      queryBuffer
          .append("update SB_MyLinks_Link set linkId = ? , name = ? , description = ?, url = ? , visible = ? , popup = ? , ");
      queryBuffer
          .append("userId = ? , instanceId = ? , objectId = ? ");
      if (link.hasPosition()) {
        queryBuffer.append(" , position = ? ");
      }
      queryBuffer.append("where linkId = ? ");
      // initialisation des paramètres
      prepStmt = con.prepareStatement(queryBuffer.toString());
      int linkId = updatedLink.getLinkId();
      int npParam = initParam(prepStmt, linkId, updatedLink);
      // initialisation du dernier paramètre
      prepStmt.setInt(npParam++, linkId);
      prepStmt.executeUpdate();
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
  }

  public static void deleteLink(Connection con, String linkId)
      throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      String query = "delete from SB_MyLinks_Link where linkId = ? ";
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.valueOf(linkId));
      prepStmt.executeUpdate();
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
  }

  public static void deleteLinksOfComponent(Connection con, String instanceId)
      throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      String query = "delete from SB_MyLinks_Link where instanceId = ? ";
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      prepStmt.executeUpdate();
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
  }

  public static void deleteLinksOfObject(Connection con, String objectId)
      throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      String query = "delete from SB_MyLinks_Link where objectId = ? ";
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, objectId);
      prepStmt.executeUpdate();
    } finally {
      // fermeture
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
