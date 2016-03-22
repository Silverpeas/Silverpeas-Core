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
* FLOSS exception. You should have received a copy of the text describing
* the FLOSS exception, and it is also available here:
* "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package org.silverpeas.core.sharing.services;

import org.silverpeas.core.sharing.model.DownloadDetail;
import org.silverpeas.core.sharing.model.Ticket;

import java.util.List;

/**
* The business service of file sharing.
* The file sharing service provides a way to share files from the Silverpeas portal to external
* users.
* The share of a file to external users consists of creation a ticket for downloading this file
* from the Silverpeas portal. This ticket can be limited in time and in download count.
*/
public interface SharingTicketService {

  /**
* Gets all the tickets emitted by the specified users.
* @param userId the identifier of the user that has emitted the tickets.
* @return the tickets of this user.
*/
  public List<Ticket> getTicketsByUser(String userId);

  /**
* Deletes all the tickets about the specified file.
* @param sharedObjectId the identifier of the shared object.
* @param type is this shared object type.
*/
  public void deleteTicketsForSharedObject(Long sharedObjectId, String type);

  /**
* Gets the ticket identified by the specified key.
* @param key the key identifying the ticket.
* @return the ticket.
*/
  public Ticket getTicket(String key);

  /**
* Creates a new ticket in the system.
* @param ticket the ticket to save.
* @return the key identifying the saved ticket.
*/
  public String createTicket(Ticket ticket);

  /**
* Updates the list of downloads that were done for a specified ticket.
* @param download the new download to add in the list.
*/
  public void addDownload(DownloadDetail download);

  /**
* Updates the information of the specified ticket.
* @param ticket the ticket with updated information.
*/
  public void updateTicket(Ticket ticket);

  /**
* Deletes the ticket identified by the specified key.
* @param key the key identifying the ticket.
*/
  public void deleteTicket(String key);

}