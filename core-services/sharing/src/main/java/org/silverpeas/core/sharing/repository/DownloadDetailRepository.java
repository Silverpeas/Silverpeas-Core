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

import org.silverpeas.core.persistence.datasource.repository.EntityRepository;
import org.silverpeas.core.persistence.datasource.repository.PaginationCriterion;
import org.silverpeas.core.persistence.datasource.repository.WithSaveAndFlush;
import org.silverpeas.core.sharing.model.DownloadDetail;
import org.silverpeas.core.sharing.model.DownloadDetail.QUERY_ORDER_BY;
import org.silverpeas.core.sharing.model.Ticket;
import org.silverpeas.core.util.SilverpeasList;

/**
 *
 * @author ehugonnet
 */
public interface DownloadDetailRepository extends EntityRepository<DownloadDetail>,
    WithSaveAndFlush<DownloadDetail> {

  /**
   * Gets paginated ticket downloads.
   * @param ticket a ticket.
   * @param paginationCriterion a pagination.
   * @param orderBy an optional order by.
   * @return a paginated list of downloads.
   */
  SilverpeasList<DownloadDetail> getDownloadsByTicket(Ticket ticket,
      PaginationCriterion paginationCriterion, QUERY_ORDER_BY orderBy);

  /**
   * Gets paginated ticket downloads.
   * @param paginationCriterion a pagination.
   * @param orderBy an optional order by.
   * @param ticket a ticket.
   * @return a paginated list of downloads.
   */
  long deleteDownloadsByTicket(Ticket ticket);
}
