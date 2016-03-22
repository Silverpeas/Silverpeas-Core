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

import org.silverpeas.core.sharing.model.Ticket;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 *
 * @author ehugonnet
 */
public class TicketMatcher  extends BaseMatcher<Ticket> {
  private Ticket ticket;
  public TicketMatcher(Ticket detail) {
    super();
    this.ticket = detail;
  }

  public boolean equals(Ticket other) {
    if (!ticket.getClass().getName().equals(other.getClass().getName())) {
      return false;
    }
    if (ticket.getSharedObjectId() != other.getSharedObjectId()) {
      return false;
    }
    if ((ticket.getComponentId() == null) ? (other.getComponentId() != null) : !ticket.getComponentId().equals(other.getComponentId())) {
      return false;
    }
    if ((ticket.getCreatorId() == null) ? (other.getCreatorId() != null) : !ticket.getCreatorId().equals(other.getCreatorId())) {
      return false;
    }
    if ((ticket.getCreationDate() == null) ? (other.getCreationDate() != null) : !ticket.getCreationDate().equals(other.getCreationDate())) {
      return false;
    }
    if ((ticket.getUpdateDate() == null) ? (other.getUpdateDate() != null) : !ticket.getUpdateDate().equals(other.getUpdateDate())) {
      return false;
    }
    if ((ticket.getEndDate() == null) ? (other.getEndDate() != null) : !ticket.getEndDate().equals(other.getEndDate())) {
      return false;
    }
    if (ticket.getNbAccessMax()!= other.getNbAccessMax()) {
      return false;
    }
    if (ticket.getNbAccess() != other.getNbAccess()) {
      return false;
    }
    return true;
  }

  @Override
  public boolean matches(Object item) {
     boolean match = false;
    if (item instanceof Ticket) {
      final Ticket actual = (Ticket) item;
      match = equals(actual);
    }
    return match;
  }

  @Override
  public void describeTo(Description description) {
     description.appendValue(ticket);
  }
}
