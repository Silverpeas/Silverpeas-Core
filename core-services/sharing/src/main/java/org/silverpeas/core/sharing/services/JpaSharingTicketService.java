/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.core.sharing.services;

import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.sharing.model.DownloadDetail;
import org.silverpeas.core.sharing.model.Ticket;
import org.silverpeas.core.sharing.repository.DownloadDetailRepository;
import org.silverpeas.core.sharing.repository.TicketRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ehugonnet
 */
@Singleton
@Transactional
public class JpaSharingTicketService implements SharingTicketService, ComponentInstanceDeletion {

  @Inject
  TicketRepository repository;
  @Inject
  DownloadDetailRepository historyRepository;

  @Override
  public List<Ticket> getTicketsByUser(String userId) {
    List<Ticket> tickets = repository.findAllReservationsForUser(userId);
    return tickets;
  }

  @Override
  public void deleteTicketsForSharedObject(Long sharedObjectId, String type) {
    List<Ticket> tickets = repository.findAllTicketForSharedObjectId(sharedObjectId, type);
    repository.delete(tickets);
    repository.flush();
  }

  @Override
  public Ticket getTicket(String key) {
    return repository.getById(key);
  }

  @Override
  public String createTicket(Ticket ticket) {
    Ticket result = repository.saveAndFlush(ticket);
    return result.getToken();
  }

  @Override
  public void addDownload(DownloadDetail download) {
    Ticket ticket = repository.getById(download.getKeyFile());
    if (ticket != null) {
      List<DownloadDetail> downloads = new ArrayList<>(ticket.getDownloads());
      downloads.add(download);
      ticket.setDownloads(downloads);
      historyRepository.saveAndFlush(download);
      ticket.addDownload();
      repository.saveAndFlush(ticket);
    }
  }

  @Override
  public void updateTicket(Ticket ticket) {
    repository.saveAndFlush(ticket);
  }

  @Override
  public void deleteTicket(String key) {
    repository.delete(repository.getById(key));
  }

  /**
   * Deletes the resources belonging to the specified component instance. This method is invoked
   * by Silverpeas when a component instance is being deleted.
   * @param componentInstanceId the unique identifier of a component instance.
   */
  @Override
  @Transactional
  public void delete(final String componentInstanceId) {
    repository.deleteAllTicketsForComponentInstance(componentInstanceId);
  }
}
