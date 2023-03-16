/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.webapi.sharing;

import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.sharing.model.Ticket;
import org.silverpeas.core.sharing.services.SharingTicketService;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.web.sharing.bean.SharingNotificationVO;
import org.silverpeas.core.web.sharing.notification.FileSharingUserNotification;
import org.silverpeas.core.web.rs.RESTWebService;
import org.silverpeas.core.web.rs.UserPrivilegeValidation;
import org.silverpeas.core.web.rs.annotation.Authenticated;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static org.silverpeas.core.sharing.services.SharingServiceProvider.getSharingTicketService;

@WebService
@Path(TicketResource.PATH)
@Authenticated
public class TicketResource extends RESTWebService {

  static final String PATH = "mytickets";

  private String componentId = null;

  @Override
  protected String getResourceBasePath() {
    return PATH;
  }

  @Override
  public String getComponentId() {
    return componentId;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<TicketEntity> getMyTickets(@QueryParam("page") final String page) {
    PaginationPage pagination = fromPage(page);
    List<Ticket> sharingTickets =
        getSharingTicketService().getTicketsByUser(getUser().getId(), pagination, null);
    if (CollectionUtil.isEmpty(sharingTickets)) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    List<TicketEntity> tickets = new ArrayList<>(sharingTickets.size());
    for (Ticket ticket : sharingTickets) {
      tickets.add(TicketEntity.fromTicket(ticket,
          getUri().getWebResourcePathBuilder().path(ticket.getToken()).build()));
    }
    return tickets;
  }

  @POST
  @Path("{componentId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response addTicket(@PathParam("componentId") String componentId,
      final TicketEntity ticketEntity) {
    validateUserAuthorizationOn(componentId);
    Ticket ticket;
    try {
      ticket = ticketEntity.toTicket(UserDetail.from(getUser()));
    } catch (ParseException e) {
      return Response.serverError().build();
    }
    if (ticket == null) {
      return Response.status(Status.FORBIDDEN).build();
    }
    String keyFile = getFileSharingService().createTicket(ticket);
    ticket.setToken(keyFile);
    ticketEntity.setToken(keyFile);
    ticketEntity.setUrl(ticket.getUrl(getHttpServletRequest()));
    SharingNotificationVO sharingParam =
        new SharingNotificationVO(ticketEntity.getUsers(), ticketEntity.getExternalEmails(),
            ticketEntity.getAdditionalMessage(), ticketEntity.getUrl());
    FileSharingUserNotification.notify(ticket, sharingParam);
    return Response.ok(ticketEntity).build();
  }

  private void validateUserAuthorizationOn(String componentId) {
    this.componentId = componentId;
    UserPrivilegeValidation validation = UserPrivilegeValidation.get();
    validateUserAuthorization(validation);
    if (getUser().isAnonymous() || getUser().isAccessGuest()) {
      throw new WebApplicationException("anonymous or guest user not allowed", Status.FORBIDDEN);
    }
  }

  private SharingTicketService getFileSharingService() {
    return getSharingTicketService();
  }
}
