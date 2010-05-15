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

package com.silverpeas.myLinks.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import com.silverpeas.myLinks.model.LinkDetail;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.exception.UtilException;

public class LinkDAO {
  public static Collection getAllLinksByUser(Connection con, String userId)
      throws SQLException {
    // récupérer toutes les liens d'un utilisateur
    ArrayList listLink = null;

    String query =
        "select * from SB_MyLinks_Link where userId = ? and (instanceId IS NULL or instanceId = '') and (objectId IS NULL or objectId = '')";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, userId);
      rs = prepStmt.executeQuery();
      listLink = new ArrayList();
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

  public static Collection getAllLinksByInstance(Connection con,
      String instanceId) throws SQLException {
    // récupérer toutes les liens d'un utilisateur sur un composant
    ArrayList listLink = null;

    String query = "select * from SB_MyLinks_Link where instanceId = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      rs = prepStmt.executeQuery();
      listLink = new ArrayList();
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

  public static Collection getAllLinksByObject(Connection con,
      String instanceId, String objectId) throws SQLException {
    // récupérer toutes les liens d'un objet
    ArrayList listLink = null;

    String query = "select * from SB_MyLinks_Link where instanceId = ? and objectId = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      prepStmt.setString(2, objectId);
      rs = prepStmt.executeQuery();
      listLink = new ArrayList();
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
      prepStmt.setInt(1, new Integer(linkId).intValue());
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
      String query =
          "update SB_MyLinks_Link set linkId = ? , name = ? , description = ?, url = ? , visible = ? , popup = ? , "
              + "userId = ? , instanceId = ? , objectId = ? where linkId = ? ";
      // initialisation des paramètres
      prepStmt = con.prepareStatement(query);
      int linkId = updatedLink.getLinkId();
      initParam(prepStmt, linkId, updatedLink);
      // initialisation du dernier paramètre
      prepStmt.setInt(10, linkId);
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
      prepStmt.setInt(1, new Integer(linkId).intValue());
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
    // recuperation des colonnes du resulSet et construction de l'objet link
    int linkId = rs.getInt(1);
    String name = rs.getString(2);
    String description = rs.getString(3);
    String url = rs.getString(4);
    boolean visible = false;
    if (rs.getInt(5) == 1) {
      visible = true;
    }
    boolean popup = false;
    if (rs.getInt(6) == 1) {
      popup = true;
    }
    String userId = rs.getString(7);
    String instanceId = rs.getString(8);
    String objectId = rs.getString(9);

    link.setLinkId(linkId);
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

  private static void initParam(PreparedStatement prepStmt, int linkId,
      LinkDetail link) throws SQLException {
    prepStmt.setInt(1, new Integer(linkId).intValue());
    prepStmt.setString(2, link.getName());
    prepStmt.setString(3, link.getDescription());
    prepStmt.setString(4, link.getUrl());
    if (link.isVisible() == true) {
      prepStmt.setInt(5, 1);
    } else {
      prepStmt.setInt(5, 0);
    }
    if (link.isPopup() == true) {
      prepStmt.setInt(6, 1);
    } else {
      prepStmt.setInt(6, 0);
    }
    prepStmt.setString(7, link.getUserId());
    prepStmt.setString(8, link.getInstanceId());
    prepStmt.setString(9, link.getObjectId());
  }

}
