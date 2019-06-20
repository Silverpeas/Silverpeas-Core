/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.sharing.repository;

import org.silverpeas.core.persistence.OrderBy;
import org.silverpeas.core.persistence.datasource.repository.PaginationCriterion;
import org.silverpeas.core.persistence.datasource.repository.jpa.BasicJpaEntityRepository;
import org.silverpeas.core.persistence.datasource.repository.jpa.NamedParameters;
import org.silverpeas.core.sharing.model.Ticket;
import org.silverpeas.core.sharing.model.Ticket.QUERY_ORDER_BY;
import org.silverpeas.core.util.SilverpeasList;

import java.util.List;

/**
 * @author: ebonnet
 */
public class TicketJpaRepository extends BasicJpaEntityRepository<Ticket>
    implements TicketRepository {

  @Override
  public List<Ticket> findAllTicketForSharedObjectId(final Long sharedObjectId,
      final String ticketType) {
    return listFromNamedQuery("Ticket.findAllTicketForSharedObjectId",
        newNamedParameters().add("sharedObjectId", sharedObjectId).add("ticketType", ticketType));
  }

  @Override
  public long countAllReservationsForUser(final String userId) {
    return countFromJpqlString("FROM Ticket t WHERE t.creatorId = :userId",
        newNamedParameters().add("userId", userId));
  }

  @Override
  public SilverpeasList<Ticket> findAllReservationsForUser(final String userId,
      final PaginationCriterion paginationCriterion, final QUERY_ORDER_BY orderBy) {
    final StringBuilder sb = new StringBuilder();
    sb.append("SELECT DISTINCT t FROM Ticket t WHERE t.creatorId = :userId");
    OrderBy.append(sb, orderBy != null ?
        orderBy.getOrderBy() :
        QUERY_ORDER_BY.CREATION_DATE_DESC.getOrderBy());
    return listFromJpqlString(sb.toString(), newNamedParameters().add("userId", userId),
        paginationCriterion);
  }

  @Override
  public void deleteAllTicketsForComponentInstance(final String instanceId) {
    NamedParameters parameters = newNamedParameters().add("instanceId", instanceId);
    deleteFromJpqlQuery("delete from DownloadDetail d where d.ticket.id in (select t.id from Ticket t where t.componentId = :instanceId)", parameters);
    deleteFromJpqlQuery("delete from Ticket t where t.componentId = :instanceId", parameters);
  }
}
