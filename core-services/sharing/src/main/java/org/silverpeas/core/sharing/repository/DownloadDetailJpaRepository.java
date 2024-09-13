/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.sharing.repository;

import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.OrderBy;
import org.silverpeas.core.persistence.datasource.repository.PaginationCriterion;
import org.silverpeas.core.persistence.datasource.repository.jpa.BasicJpaEntityRepository;
import org.silverpeas.core.sharing.model.DownloadDetail;
import org.silverpeas.core.sharing.model.DownloadDetail.QUERY_ORDER_BY;
import org.silverpeas.core.sharing.model.Ticket;
import org.silverpeas.core.util.SilverpeasList;

/**
 * @author: ebonnet
 */
@Repository
public class DownloadDetailJpaRepository extends BasicJpaEntityRepository<DownloadDetail>
    implements DownloadDetailRepository {

  @Override
  public SilverpeasList<DownloadDetail> getDownloadsByTicket(final Ticket ticket,
      final PaginationCriterion paginationCriterion, final QUERY_ORDER_BY orderBy) {
    final StringBuilder sb = new StringBuilder();
    sb.append("select d from DownloadDetail d where d.ticket = :ticket");
    if (orderBy != null) {
      OrderBy.append(sb, orderBy.getOrderBy());
    }
    return listFromJpqlString(sb.toString(), newNamedParameters().add("ticket", ticket),
        paginationCriterion);
  }

  @Override
  public long deleteDownloadsByTicket(final Ticket ticket) {
    return deleteFromJpqlQuery("delete from DownloadDetail d where d.ticket = :ticket",
        newNamedParameters().add("ticket", ticket));
  }
}
