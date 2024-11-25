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
package org.silverpeas.core.webapi.socialnetwork.invitation;

import org.silverpeas.core.socialnetwork.invitation.Invitation;
import org.silverpeas.core.socialnetwork.invitation.InvitationService;
import org.silverpeas.core.web.rs.RESTWebService;
import org.silverpeas.core.web.rs.annotation.Authenticated;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.List;
import java.util.stream.Collectors;

import static org.silverpeas.core.webapi.socialnetwork.invitation.InvitationEntity.fromInvitation;

/**
 * It represents a resource published in the WEB that represents an invitation emitted by a user to
 * another one in the Silverpeas platform.
 * <p>
 * The WEB resource belongs always to the current user in the session underlying at the HTTP
 * request. Then, it represents an invitation either sent or received by him. With a such policy,
 * the invitations belonging to another user cannot be fetched by him.
 * </p>
 */
@RequestScoped
@Path(InvitationResource.PATH)
@Authenticated
public class InvitationResource extends RESTWebService {

  static final String PATH = "invitations";

  @Inject
  private InvitationService invitationService;

  @Path("inbox")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<InvitationEntity> getReceivedInvitations() {
    List<Invitation> invitations = invitationService.getAllMyInvitationsReceive(Integer.parseInt(
            getUser().getId()));
    return asWebEntities(invitations, locatedAt(getUri().getAbsolutePathBuilder()));
  }

  @Path("outbox")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<InvitationEntity> getSentInvitations() {
    List<Invitation> invitations = invitationService.getAllMyInvitationsSent(Integer.parseInt(
            getUser().getId()));
    return asWebEntities(invitations, locatedAt(getUri().getAbsolutePathBuilder()));
  }

  @Path("{id}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public InvitationEntity getInvitation(@PathParam("id") final Integer id) {
    checkInvitation(id);
    Invitation invitation = invitationService.getInvitation(id);
    return asWebEntity(invitation, locatedAt(getUri().getAbsolutePathBuilder()));
  }

  @DELETE
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response cancelInvitation(@PathParam("id") final Integer id) {
    checkInvitation(id);
    invitationService.ignoreInvitation(id);
    return Response.ok().build();
  }

  @PUT
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response acceptInvitation(@PathParam("id") final Integer id) {
    checkInvitation(id);
    int code = invitationService.acceptInvitation(id);
    if (code == InvitationService.INVITATION_NOT_EXISTING) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    } else if (code == InvitationService.RELATIONSHIP_ALREADY_EXISTING) {
      throw new WebApplicationException(Response.Status.CONFLICT);
    }
    return Response.ok().build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createInvitation(final InvitationEntity invitationEntity) {
    checkInvitationEntity(invitationEntity);

    Invitation invitation = new Invitation();
    invitation.setReceiverId(invitationEntity.getReceiverId());
    invitation.setMessage(invitationEntity.getMessage());
    invitation.setSenderId(Integer.parseInt(getUser().getId()));
    int id = invitationService.invite(invitation);
    invitationEntity.setId(id);
    return Response.ok(invitationEntity).build();
  }

  @Override
  protected String getResourceBasePath() {
    return PATH;
  }

  @Override
  public String getComponentId() {
    return null;
  }

  private void checkInvitationEntity(InvitationEntity invitationEntity) {
    int senderId = invitationEntity.getSenderId();
    int receiverId = invitationEntity.getReceiverId();
    if (senderId == receiverId) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    if (senderId != Integer.parseInt(getUser().getId())
        || receiverId != Integer.parseInt(getUser().getId())) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  private void checkInvitation(int invitationId) {
    Invitation invitation = invitationService.getInvitation(invitationId);
    if (invitation == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    if (invitation.getSenderId() != Integer.parseInt(getUser().getId()) ||
        invitation.getReceiverId() != Integer.parseInt(getUser().getId())) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  private static UriBuilder locatedAt(final UriBuilder uri) {
    return uri;
  }

  private List<InvitationEntity> asWebEntities(List<Invitation> invitations, UriBuilder baseUri) {
    baseUri.path("{invitationId}");
    return invitations.stream()
        .map(invitation ->
            fromInvitation(invitation).withAsUri(baseUri.build(invitation.getId())))
        .collect(Collectors.toList());
  }

  private InvitationEntity asWebEntity(Invitation invitation, UriBuilder baseUri) {
    baseUri.path("{invitationId}");
    return fromInvitation(invitation).withAsUri(baseUri.build(invitation.getId()));
  }
}
