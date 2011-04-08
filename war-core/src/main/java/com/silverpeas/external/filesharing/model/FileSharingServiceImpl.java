/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.external.filesharing.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.silverpeas.external.filesharing.dao.TicketDAO;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;

/**
 * Implementation of the business operations of file sharing service.
 */
public class FileSharingServiceImpl implements FileSharingService {

  private TicketDAO dao;
  private OrganizationController organizationController = new OrganizationController();

  public FileSharingServiceImpl() {
    dao = new TicketDAO();
  }

  @Override
  public List<TicketDetail> getTicketsByUser(String userId) {
    Connection con = openConnection();
    try {
      List<TicketDetail> tickets = dao.getTicketsByUser(con, userId);
      for (TicketDetail aTicket : tickets) {
        completeTicketDetail(aTicket);
      }
      return tickets;
    } catch (Exception e) {
      throw new FileSharingRuntimeException("FileSharingService.getTicketsByUser()",
          SilverpeasRuntimeException.ERROR, "filsSharing.MSG_TICKETS_NOT_EXIST", e);
    } finally {
      // fermer la connexion
      closeConnection(con);
    }
  }

  @Override
  public void deleteTicketsByFile(String fileId, boolean versioning) {
    Connection con = openConnection();
    try {
      dao.deleteTicketsByFile(con, fileId, versioning);
    } catch (Exception e) {
      throw new FileSharingRuntimeException("FileSharingService.getTicketsByFile()",
          SilverpeasRuntimeException.ERROR, "filsSharing.MSG_TICKETS_NOT_EXIST", e);
    } finally {
      // fermer la connexion
      closeConnection(con);
    }
  }

  @Override
  public TicketDetail getTicket(String key) {
    Connection con = openConnection();
    try {
      TicketDetail theTicket = dao.getTicket(con, key);
      completeTicketDetail(theTicket);
      return theTicket;
    } catch (Exception e) {
      throw new FileSharingRuntimeException("FileSharingService.getTicket()",
          SilverpeasRuntimeException.ERROR, "filsSharing.MSG_TICKET_NOT_EXIST", e);
    } finally {
      // fermer la connexion
      closeConnection(con);
    }
  }

  @Override
  public String createTicket(TicketDetail ticket) {
    Connection con = openConnection();
    try {
      return dao.createTicket(con, ticket);
    } catch (Exception e) {
      throw new FileSharingRuntimeException("FileSharingService.createTicket()",
          SilverpeasRuntimeException.ERROR, "filsSharing.MSG_CREATION_NOT_POSSIBLE", e);
    } finally {
      // fermer la connexion
      closeConnection(con);
    }
  }

  @Override
  public void addDownload(DownloadDetail download) {
    Connection con = openConnection();
    try {
      TicketDetail ticket = dao.getTicket(con, download.getKeyFile());
      ticket.setNbAccess(ticket.getNbAccess() + 1);
      dao.addDownload(con, download);
      dao.updateTicket(con, ticket);
    } catch (Exception e) {
      throw new FileSharingRuntimeException("FileSharingService.addDownload()",
          SilverpeasRuntimeException.ERROR, "filsSharing.MSG_ADD_DOWNLOAD_NOT_POSSIBLE", e);
    } finally {
      // fermer la connexion
      closeConnection(con);
    }
  }

  @Override
  public void updateTicket(TicketDetail ticket) {
    Connection con = openConnection();
    try {
      dao.updateTicket(con, ticket);
    } catch (Exception e) {
      throw new FileSharingRuntimeException("FileSharingService.updateTicket()",
          SilverpeasRuntimeException.ERROR, "filsSharing.MSG_UPDATE_NOT_POSSIBLE", e);
    } finally {
      // fermer la connexion
      closeConnection(con);
    }
  }

  @Override
  public void deleteTicket(String key) {
    Connection con = openConnection();
    try {
      dao.deleteTicket(con, key);
    } catch (Exception e) {
      throw new FileSharingRuntimeException("FileSharingService.deleteTicket()",
          SilverpeasRuntimeException.ERROR, "filsSharing.MSG_DELETE_NOT_POSSIBLE", e);
    } finally {
      // fermer la connexion
      closeConnection(con);
    }
  }

  private Connection openConnection() {
    Connection con = null;
    // initialisation de la connexion
    try {
      con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    } catch (UtilException e) {
      // traitement des exceptions
      throw new FileSharingRuntimeException("FileSharingService.openConnection()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
    return con;
  }

  private void closeConnection(Connection con) {
    try {
      if (con != null) {
        con.close();
      }
    } catch (SQLException e) {
      // traitement des exceptions
      throw new FileSharingRuntimeException("FileSharingService.closeConnection()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_CLOSE_FAILED", e);
    }
  }

  private OrganizationController getOrganizationController() {
    return organizationController;
  }

  /**
   * Complete the detail about the specified ticket with additional information (such as the name of
   * its creator for example).
   * @param ticket the ticket to complete.
   */
  private void completeTicketDetail(final TicketDetail ticket) {
    UserDetail creator = getOrganizationController().getUserDetail(ticket.getCreator().getId());
    ticket.setCreator(creator);
    if (ticket.getLastModifier() != null) {
      UserDetail modifier = getOrganizationController().getUserDetail(ticket.getLastModifier().getId());
      ticket.setLastModifier(modifier);
    }
  }
}