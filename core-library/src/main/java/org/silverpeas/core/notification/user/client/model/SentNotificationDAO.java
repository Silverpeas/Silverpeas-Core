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

package org.silverpeas.core.notification.user.client.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.persistence.jdbc.LongText;
import org.silverpeas.core.persistence.jdbc.DBUtil;

public class SentNotificationDAO {

  private static final String COLUMNS =
      "notifId, userId, messageType, notifDate, title, link, sessionId, componentId, body";

  public static int saveNotifUser(Connection con, SentNotificationDetail notif) {
    // Création de la sauvegarde de la notification envoyée
    PreparedStatement prepStmt = null;
    int notifId = 0;
    try {
      String query = "insert into ST_NotifSended "
          + "(" + COLUMNS + ")"
          + " values (?,?,?,?,?,?,?,?,?)";
      // initialisation des paramètres
      prepStmt = con.prepareStatement(query);
      notifId = DBUtil.getNextId("ST_NotifSended", "notifId");
      prepStmt.setInt(1, notifId);
      prepStmt.setInt(2, notif.getUserId());
      prepStmt.setInt(3, notif.getMessageType());
      Date notifDate = notif.getNotifDate();
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(notifDate);
      prepStmt.setString(4, Long.toString(calendar.getTime().getTime()));
      prepStmt.setString(5, notif.getTitle());
      prepStmt.setString(6, notif.getLink());
      prepStmt.setString(7, notif.getSessionId());
      prepStmt.setString(8, notif.getComponentId());
      // sauvegarde du body dans la table ST_LongText et récupération de l'id
      int bodyId = LongText.addLongText(notif.getBody());
      prepStmt.setInt(9, bodyId);
      prepStmt.executeUpdate();
      for (String userId : notif.getUsers()) {
        saveReceiver(con, notifId, Integer.parseInt(userId));
      }
    } catch (Exception e) {
      return 0;
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
    return notifId;
  }

  public static List<SentNotificationDetail> getAllNotifByUser(Connection con, String userId)
      throws SQLException {
    // récupérer toutes les notifications envoyées par un utilisateur
    List<SentNotificationDetail> notifs = new ArrayList<SentNotificationDetail>();

    String query =
        "select " + COLUMNS + " from ST_NotifSended where userId = ? order by notifDate desc";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(userId));
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        SentNotificationDetail notif = recupNotif(rs);
        notifs.add(notif);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return notifs;
  }

  public static List<String> getReceivers(Connection con, int notifId)
      throws SQLException {
    // récupérer toutes les notifications envoyées par un utilisateur
    List<String> users = new ArrayList<String>();

    String query = "select userId from ST_NotifSendedReceiver where notifId = ?";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, notifId);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        users.add(Integer.toString(rs.getInt("userId")));
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return users;
  }

  public static SentNotificationDetail getNotif(Connection con, int notifId)
      throws SQLException {

    SentNotificationDetail notif = new SentNotificationDetail();

    String query = "select " + COLUMNS + " from ST_NotifSended where notifId = ?";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, notifId);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        notif = recupNotif(rs);
      }
      // récupérer les destinataires
      notif.setUsers(getReceivers(con, notifId));
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return notif;
  }

  public static void deleteNotif(Connection con, int notifId) throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      // supprimer la liste des users ayant reçu la notification
      deleteReceivers(con, notifId);
      // supprimer la notification
      String query = "delete from ST_NotifSended where notifId = ? ";
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, notifId);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  private static void deleteReceivers(Connection con, int notifId) throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      String query = "delete from ST_NotifSendedReceiver where notifId = ? ";
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, notifId);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  private static void saveReceiver(Connection con, int notifId, int userId) throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      String query = "insert into ST_NotifSendedReceiver (notifId, userId) values (?,?) ";
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, notifId);
      prepStmt.setInt(2, userId);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void deleteNotifByUser(Connection con, String userId) throws SQLException {
    // rechercher toutes les notifications de cet utilisateur
    List<SentNotificationDetail> notifs = getAllNotifByUser(con, userId);
    for (SentNotificationDetail notif : notifs) {
      deleteNotif(con, notif.getNotifId());
    }
  }

  protected static SentNotificationDetail recupNotif(ResultSet rs) throws SQLException {
    SentNotificationDetail notif = new SentNotificationDetail();
    notif.setNotifId(rs.getInt("notifId"));
    notif.setUserId(rs.getInt("userId"));
    notif.setMessageType(rs.getInt("messageType"));
    String date = rs.getString("notifDate");
    if (StringUtil.isDefined(date)) {
      try {
        notif.setNotifDate(new Date(Long.parseLong(date)));
      } catch (NumberFormatException e) {
        notif.setNotifDate(new Date());
      }

    } else {
      notif.setNotifDate(new Date());
    }
    notif.setTitle(rs.getString("title"));
    notif.setLink(rs.getString("link"));
    notif.setSessionId(rs.getString("sessionId"));
    notif.setComponentId(rs.getString("componentId"));
    // récupération du body depuis la table ST_LongText à partir de l'id
    String body = "";
    try {
      int longTextId = -1;
      longTextId = rs.getInt("body");
      body = LongText.getLongText(longTextId);
    } catch (Exception e) {
    }
    notif.setBody(body);
    return notif;
  }
}