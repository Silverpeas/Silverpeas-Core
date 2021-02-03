/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
/**
 * Copyright (C) 2000 - 2021 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.sharing.services;

import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.sharing.model.DownloadDetail;
import org.silverpeas.core.sharing.model.DownloadDetail.QUERY_ORDER_BY;
import org.silverpeas.core.sharing.model.Ticket;
import org.silverpeas.core.util.SilverpeasList;

/**
 * The business service of file sharing.
 * The file sharing service provides a way to share files from the Silverpeas portal to external
 * users.
 * The share of a file to external users consists of creation a ticket for downloading this file
 * from the Silverpeas portal. This ticket can be limited in time and in download count.
 */
public interface SharingTicketService {

  /**
   * Gets the number of tickets emitted by the specified users.
   * @param userId the identifier of the user that has emitted the tickets.
   * @return the number of tickets.
   */
  long countTicketsByUser(String userId);

  /**
   * Gets all the tickets emitted by the specified users.
   * @param userId the identifier of the user that has emitted the tickets.
   * @param paginationPage the needed page (performances)
   * @param orderBy the order by must be performed by the persistence because of performances
   * @return the tickets of this user.
   */
  SilverpeasList<Ticket> getTicketsByUser(String userId, final PaginationPage paginationPage,
      final Ticket.QUERY_ORDER_BY orderBy);

  /**
   * Deletes all the tickets about the specified file.
   * @param sharedObjectId the identifier of the shared object.
   * @param type is this shared object type.
   */
  void deleteTicketsForSharedObject(Long sharedObjectId, String type);

  /**
   * Gets the ticket identified by the specified key.
   * @param key the key identifying the ticket.
   * @return the ticket.
   */
  Ticket getTicket(String key);

  /**
   * Gets the ticket identified by the specified key.
   * @param ticket a ticket.
   * @param paginationPage the needed page (performances)
   * @param orderBy the order by must be performed by the persistence because of performances
   * @return the list of downloads of the given ticket.
   */
  SilverpeasList<DownloadDetail> getTicketDownloads(Ticket ticket, PaginationPage paginationPage,
      QUERY_ORDER_BY orderBy);

  /**
   * Creates a new ticket in the system.
   * @param ticket the ticket to save.
   * @return the key identifying the saved ticket.
   */
  String createTicket(Ticket ticket);

  /**
   * Updates the list of downloads that were done for a specified ticket.
   * @param download the new download to add in the list.
   */
  void addDownload(DownloadDetail download);

  /**
   * Updates the information of the specified ticket.
   * @param ticket the ticket with updated information.
   */
  void updateTicket(Ticket ticket);

  /**
   * Deletes the ticket identified by the specified key.
   * @param key the key identifying the ticket.
   */
  void deleteTicket(String key);

}