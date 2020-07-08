/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.sharing.services;

import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.backgroundprocess.AbstractBackgroundProcessRequest;
import org.silverpeas.core.backgroundprocess.BackgroundProcessTask;
import org.silverpeas.core.sharing.model.DownloadDetail;
import org.silverpeas.core.sharing.model.DownloadDetail.QUERY_ORDER_BY;
import org.silverpeas.core.sharing.model.Ticket;
import org.silverpeas.core.sharing.repository.DownloadDetailRepository;
import org.silverpeas.core.sharing.repository.TicketRepository;
import org.silverpeas.core.util.SilverpeasList;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.List;

import static org.silverpeas.core.backgroundprocess.BackgroundProcessTask.LOCK_DURATION.A_DAY;

/**
 *
 * @author ehugonnet
 */
@Service
@Singleton
@Transactional
public class JpaSharingTicketService implements SharingTicketService, ComponentInstanceDeletion {

  @Inject
  TicketRepository repository;

  @Inject
  DownloadDetailRepository historyRepository;

  @Override
  public long countTicketsByUser(final String userId) {
    return repository.countAllReservationsForUser(userId);
  }

  @Override
  public SilverpeasList<Ticket> getTicketsByUser(String userId, final PaginationPage paginationPage,
      final Ticket.QUERY_ORDER_BY orderBy) {
    final SilverpeasList<Ticket> tickets = repository.findAllReservationsForUser(userId,
        paginationPage != null ? paginationPage.asCriterion() : null, orderBy);
    BackgroundProcessTask.push(new CleanUserTicketRequest(userId, this));
    return tickets;
  }

  @Override
  public void deleteTicketsForSharedObject(Long sharedObjectId, String type) {
    List<Ticket> tickets = repository.findAllTicketForSharedObjectId(sharedObjectId, type);
    tickets.forEach(t -> historyRepository.deleteDownloadsByTicket(t));
    historyRepository.flush();
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
    Ticket ticket = repository.getById(key);
    repository.delete(ticket);
    historyRepository.deleteDownloadsByTicket(ticket);
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

  @Override
  public SilverpeasList<DownloadDetail> getTicketDownloads(final Ticket ticket,
      final PaginationPage paginationPage, final QUERY_ORDER_BY orderBy) {
    return historyRepository
        .getDownloadsByTicket(ticket, paginationPage != null ? paginationPage.asCriterion() : null,
            orderBy);
  }

  /**
   * Background process which is in charge of cleaning shared ticket which are linked to
   * resources which do not exist anymore.
   */
  private static class CleanUserTicketRequest extends AbstractBackgroundProcessRequest {

    private final String userId;
    private final SharingTicketService service;

    CleanUserTicketRequest(final String userId, final SharingTicketService service) {
      super("CleanUserTicketRequest_" + userId, A_DAY);
      this.userId = userId;
      this.service = service;
    }

    @Override
    protected void process() {
      int pageIndex = 1;
      int pageSize = 10000;
      while (true) {
        final PaginationPage paginationPage = new PaginationPage(pageIndex, pageSize);
        final List<Ticket> tickets = service.getTicketsByUser(userId, paginationPage, null);
        for (Ticket ticket : tickets) {
          if (ticket.getResource() == null) {
            // delete obsolete ticket associated to deleted resource
            service.deleteTicket(ticket.getToken());
            // Same data will be performed again
            pageIndex = paginationPage.getPageNumber() - 1;
          }
        }
        if (tickets.size() < pageSize) {
          break;
        }
        pageIndex++;
      }
    }
  }
}
