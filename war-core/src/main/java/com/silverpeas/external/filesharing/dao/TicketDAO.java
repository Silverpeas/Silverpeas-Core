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
package com.silverpeas.external.filesharing.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.silverpeas.external.filesharing.model.DownloadDetail;
import com.silverpeas.external.filesharing.model.TicketDetail;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.exception.UtilException;

public class TicketDAO {
  public List<TicketDetail> getTicketsByUser(Connection con, String userId) throws SQLException {
    // récupérer toutes les tickets d'un utilisateur
    ArrayList<TicketDetail> tickets = null;

    String query = "select * from SB_fileSharing_ticket where creatorId = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, userId);
      rs = prepStmt.executeQuery();
      tickets = new ArrayList<TicketDetail>();
      while (rs.next()) {
        TicketDetail ticket = recupTicket(rs);
        tickets.add(ticket);
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return tickets;
  }

  public List<TicketDetail> getTicketsByComponentIds(Connection con, List<String> componentIds)
      throws SQLException {
    // récupérer toutes les tickets d'un utilisateur
    ArrayList<TicketDetail> tickets = null;

    String query = "select * from SB_fileSharing_ticket";
    String whereClause = " where componentId IN (";
    for (int c = 0; c < componentIds.size(); c++) {
      if (c != 0)
        whereClause += ",";
      whereClause += componentIds.get(c);
    }
    whereClause += ")";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query + whereClause);
      rs = prepStmt.executeQuery();
      tickets = new ArrayList<TicketDetail>();
      while (rs.next()) {
        TicketDetail ticket = recupTicket(rs);
        tickets.add(ticket);
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return tickets;
  }

  public TicketDetail getTicket(Connection con, String key) throws SQLException {
    // récupérer un ticket
    TicketDetail ticket = new TicketDetail();
    String query = "select * from SB_fileSharing_ticket where keyFile = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, key);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        // recuperation des colonnes du resulSet et construction de l'objet photo
        ticket = recupTicket(rs);
      }
      // récupérer la liste des téléchargements
      ticket.setDownloads(getAllDownloads(con, key));
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return ticket;
  }

  public void deleteTicketsByFile(Connection con, String fileId, boolean versioning)
      throws SQLException {
    // récupérer toutes les tickets associés à un fichier
    String query = "select keyFile from SB_fileSharing_ticket where fileId = ?";
    String and = " and versioning = 0";
    if (versioning)
      and = " and versioning = 1";
    query += and;
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, fileId);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        String key = rs.getString("keyFile");
        deleteTicket(con, key);
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
  }

  public List<DownloadDetail> getAllDownloads(Connection con, String key) throws SQLException {
    // récupérer tous les downloads sur un ticket
    List<DownloadDetail> downloads = new ArrayList<DownloadDetail>();

    String query = "select * from SB_fileSharing_history where keyFile = ?";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, key);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        DownloadDetail download = recupDownload(rs);
        downloads.add(download);
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return downloads;
  }

  public static String createTicket(Connection con, TicketDetail ticket) throws SQLException

  {
    // Création d'une commande
    String key = createUniKey();
    PreparedStatement prepStmt = null;
    try {
      Date today = new Date();
      // création de la requête
      String query =
          "insert into SB_fileSharing_ticket (fileId, componentId, versioning, creatorId, creationDate, endDate, nbAccessMax, keyFile)"
              +
              " values (?,?,?,?,?,?,?,?)";
      // initialisation des paramètres
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, ticket.getFileId());
      prepStmt.setString(2, ticket.getComponentId());
      prepStmt.setInt(3, 0);
      if (ticket.isVersioning())
        prepStmt.setInt(3, 1);
      prepStmt.setString(4, ticket.getCreatorId());
      prepStmt.setString(5, "" + today.getTime());
      Date endDate = ticket.getEndDate();
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(endDate);
      calendar.set(Calendar.HOUR_OF_DAY, 23);
      calendar.set(Calendar.MINUTE, 59);
      calendar.set(Calendar.SECOND, 59);
      prepStmt.setString(6, Long.toString(calendar.getTime().getTime()));
      prepStmt.setInt(7, ticket.getNbAccessMax());
      prepStmt.setString(8, key);
      prepStmt.executeUpdate();
    } catch (Exception e) {
      // TODO trace
      return null;
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
    return key;
  }

  public void updateTicket(Connection con, TicketDetail ticket) throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      Date today = new Date();
      String query =
          "update SB_fileSharing_ticket set fileId = ? , componentId = ? , updateId = ? , updateDate = ? , "
              +
              "endDate = ? , nbAccessMax = ? , nbAccess = ? where keyfile = ? ";
      // initialisation des paramètres
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, ticket.getFileId());
      prepStmt.setString(2, ticket.getComponentId());
      prepStmt.setString(3, ticket.getUpdateId());
      if (StringUtil.isDefined(ticket.getUpdateId()))
        prepStmt.setString(4, "" + today.getTime());
      else
        prepStmt.setString(4, null);
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(ticket.getEndDate());
      calendar.set(Calendar.HOUR_OF_DAY, 23);
      calendar.set(Calendar.MINUTE, 59);
      calendar.set(Calendar.SECOND, 59);
      prepStmt.setString(5, Long.toString(calendar.getTime().getTime()));
      prepStmt.setInt(6, ticket.getNbAccessMax());
      prepStmt.setInt(7, ticket.getNbAccess());
      prepStmt.setString(8, ticket.getKeyFile());

      prepStmt.executeUpdate();
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
  }

  public void addDownload(Connection con, DownloadDetail download) throws SQLException,
      UtilException {
    // Ajout d'un téléchargement
    PreparedStatement prepStmt = null;
    try {
      // création de la requête
      String query = "insert into SB_fileSharing_history (id, keyfile, downloadDate, downloadIp)" +
          " values (?,?,?,?)";
      // initialisation des paramètres
      int id = DBUtil.getNextId("SB_fileSharing_history", "id");
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, id);
      prepStmt.setString(2, download.getKeyFile());
      prepStmt.setString(3, "" + download.getDownloadDate().getTime());
      prepStmt.setString(4, download.getUserIP());
      prepStmt.executeUpdate();
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
  }

  private static void deleteDownloads(Connection con, String key) throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      // création de la requête
      String query = "delete from SB_fileSharing_history where keyfile = ? ";
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, key);
      prepStmt.executeUpdate();
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
  }

  public static void deleteTicket(Connection con, String key) throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      // delete according history
      deleteDownloads(con, key);

      String query = "delete from SB_fileSharing_ticket where keyFile = ? ";
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, key);
      prepStmt.executeUpdate();
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
  }

  public static String createUniKey() {
    return UUID.randomUUID().toString().substring(0, 32);
  }

  protected TicketDetail recupTicket(ResultSet rs) throws SQLException {
    TicketDetail ticket = new TicketDetail();
    // recuperation des colonnes du resulSet et construction de l'objet ticket

    ticket.setFileId(rs.getInt("fileId"));
    ticket.setComponentId(rs.getString("componentId"));
    boolean versioning = false;
    if (rs.getInt("versioning") == 1)
      versioning = true;
    ticket.setVersioning(versioning);
    ticket.setCreatorId(rs.getString("creatorId"));
    String creationDate = null;
    if (StringUtil.isDefined(rs.getString("creationDate"))) {
      try {
        creationDate = (String) rs.getString("creationDate");
      } catch (Exception e) {
        throw new SQLException(e.getMessage());
      }
      ticket.setCreationDate(new Date(Long.parseLong(creationDate)));
    }
    ticket.setUpdateId(rs.getString("updateId"));
    String updateDate = null;
    if (StringUtil.isDefined(rs.getString("updateDate"))) {
      try {
        updateDate = (String) rs.getString("updateDate");
      } catch (Exception e) {
        throw new SQLException(e.getMessage());
      }
      ticket.setUpdateDate(new Date(Long.parseLong(updateDate)));
    }
    String endDate = null;
    if (StringUtil.isDefined(rs.getString("endDate"))) {
      try {
        endDate = (String) rs.getString("endDate");
      } catch (Exception e) {
        throw new SQLException(e.getMessage());
      }
      ticket.setEndDate(new Date(Long.parseLong(endDate)));
    }

    ticket.setNbAccessMax(rs.getInt("NbAccessMax"));
    ticket.setNbAccess(rs.getInt("NbAccess"));
    ticket.setKeyFile(rs.getString("KeyFile"));

    return ticket;
  }

  protected DownloadDetail recupDownload(ResultSet rs) throws SQLException {
    DownloadDetail download = new DownloadDetail();
    // recuperation des colonnes du resulSet et construction de l'objet download

    download.setId(rs.getInt("id"));
    download.setKeyFile(rs.getString("keyFile"));
    String downloadDate = rs.getString("downloadDate");
    if (StringUtil.isDefined(downloadDate)) {
      try {
        download.setDownloadDate(new Date(Long.parseLong(downloadDate)));
      } catch (NumberFormatException e) {
        download.setDownloadDate(new Date());
      }

    } else {
      download.setDownloadDate(new Date());
    }
    download.setUserIP(rs.getString("downloadIp"));

    return download;
  }

}
