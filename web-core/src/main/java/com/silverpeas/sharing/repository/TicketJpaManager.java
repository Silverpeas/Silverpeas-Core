package com.silverpeas.sharing.repository;

import com.silverpeas.sharing.model.Ticket;
import org.silverpeas.persistence.model.identifier.UuidIdentifier;
import org.silverpeas.persistence.repository.jpa.JpaBasicEntityManager;

import java.util.List;

/**
 * @author: ebonnet
 */
public class TicketJpaManager extends JpaBasicEntityManager<Ticket, UuidIdentifier>
    implements TicketRepository {

  @Override
  public List<Ticket> findAllTicketForSharedObjectId(final Long sharedObjectId,
      final String ticketType) {
    return listFromNamedQuery("Ticket.findAllTicketForSharedObjectId",
        newNamedParameters().add("sharedObjectId", sharedObjectId));
  }

  @Override
  public List<Ticket> findAllReservationsForUser(final String userId) {
    return listFromNamedQuery("Ticket.findAllReservationsForUser",
        newNamedParameters().add("userId", userId));
  }
}
