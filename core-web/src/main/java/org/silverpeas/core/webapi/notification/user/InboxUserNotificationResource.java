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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.webapi.notification.user;

import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.notification.user.server.channel.silvermail.SILVERMAILException;
import org.silverpeas.core.notification.user.server.channel.silvermail.SILVERMAILMessage;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.web.rs.RESTWebService;
import org.silverpeas.core.web.rs.annotation.Authenticated;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.silverpeas.core.notification.user.server.channel.silvermail.SILVERMAILPersistence.*;
import static org.silverpeas.core.notification.user.server.channel.silvermail.SilvermailCriteria.QUERY_ORDER_BY.RECEPTION_DATE_DESC;

/**
 * A REST Web resource giving gallery data.
 * @author Yohann Chastagnier
 */
@WebService
@Path(InboxUserNotificationResourceURIs.BASE_URI)
@Authenticated
public class InboxUserNotificationResource extends RESTWebService {

  @Inject
  private InboxUserNotificationResourceURIs uri;

  /*
   * (non-Javadoc)
   * @see com.silverpeas.web.RESTWebService#getComponentId()
   */
  @Override
  public String getComponentId() {
    return "";
  }

  @Override
  protected String getResourceBasePath() {
    return InboxUserNotificationResourceURIs.BASE_URI;
  }

  /**
   * Gets the JSON representation of notification.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * data.
   */
  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public InboxUserNotificationEntity get(@PathParam("id") long id) {
    return asWebEntity(getMessage(id));
  }

  /**
   * Deletes the aimed notification.
   */
  @DELETE
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response delete(@PathParam("id") long id) {
    try {
      deleteMessage(id, getUser().getId());
    } catch (SILVERMAILException e) {
      throw new WebApplicationException(e);
    }
    return Response.ok().build();
  }

  /**
   * Gets the JSON representation of notification.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * data.
   */
  @GET
  @Path("{id}/read")
  @Produces(MediaType.APPLICATION_JSON)
  public InboxUserNotificationEntity markAsRead(@PathParam("id") long id) {
    try {
      return asWebEntity(getMessageAndMarkAsRead(id));
    } catch (SILVERMAILException e) {
      throw new WebApplicationException(e);
    }
  }

  /**
   * Gets the JSON representation of notification list.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * data.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAllPaginated(@QueryParam("page") String page) {
    PaginationPage pagination = fromPage(page);
    if (pagination == null) {
      pagination = PaginationPage.DEFAULT;
    }
    SilverpeasList<SILVERMAILMessage> notifications;
    try {
      notifications = getMessageOfFolder(getUser().getId(), "INBOX", pagination, RECEPTION_DATE_DESC);
    } catch (Exception e) {
      throw new WebApplicationException(e);
    }
    return Response.ok(asWebEntities(notifications)).
        header(RESPONSE_HEADER_ARRAYSIZE, notifications.originalListSize()).build();
  }

  /**
   * Deletes aime notifications.
   */
  @PUT
  @Produces(MediaType.APPLICATION_JSON)
  public Response markAsRead(final List<InboxUserNotificationEntity> entities) {
    if (entities.isEmpty()) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    markMessagesAsRead(getUser().getId(), entities.stream()
        .map(InboxUserNotificationEntity::getId)
        .map(String::valueOf)
        .collect(Collectors.toList()));
    return Response.ok().build();
  }

  /**
   * Deletes aime notifications.
   */
  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  public Response delete(final List<InboxUserNotificationEntity> entities) {
    if (entities.isEmpty()) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    deleteMessages(getUser().getId(), entities.stream()
        .map(InboxUserNotificationEntity::getId)
        .map(String::valueOf)
        .collect(Collectors.toList()));
    return Response.ok().build();
  }

  /**
   * Converts the list of notification into list of notification web entities.
   * @param notifications the notifications to convert.
   * @return the notification web entities.
   */
  public List<InboxUserNotificationEntity> asWebEntities(
      final Collection<SILVERMAILMessage> notifications) {
    return notifications.stream().map(this::asWebEntity).collect(Collectors.toList());
  }

  /**
   * Converts the notification into its corresponding web entity.<br/>
   * If the specified notification isn't defined, then an HTTP 404 error is sent back instead of
   * the entity representation of the notification.<br/>
   * If the specified notification does not concern the current user, then an HTTP 403 error is
   * sent back.
   * @param notification the notification to convert.
   * @return the corresponding notification entity.
   */
  public InboxUserNotificationEntity asWebEntity(final SILVERMAILMessage notification) {
    if (notification == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    if (!String.valueOf(notification.getUserId()).equals(getUser().getId())) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    return InboxUserNotificationEntity.from(notification)
        .withURI(uri.ofNotification(notification))
        .withMarkAsReadURI(uri.ofNotificationToMarkAsRead(notification));
  }
}
