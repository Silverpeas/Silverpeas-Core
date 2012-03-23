/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.sharing.repository;

import com.silverpeas.sharing.model.Ticket;
import org.silverpeas.util.UuidPk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 *
 * @author ehugonnet
 */
public interface TicketRepository extends JpaRepository<Ticket, UuidPk> {

  @Query("SELECT t FROM Ticket t WHERE t.sharedObjectId = :sharedObjectId AND t.sharedObjectType = :ticketType")
  public List<Ticket> findAllTicketForSharedObjectId(@Param("sharedObjectId") Long sharedObjectId,
      @Param("ticketType") String ticketType);

  @Query("SELECT DISTINCT ticket FROM Ticket ticket WHERE ticket.creatorId = :userId")
  public List<Ticket> findAllReservationsForUser(@Param("userId") String userId);
}
