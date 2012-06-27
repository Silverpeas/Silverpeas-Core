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
package com.silverpeas.socialNetwork.invitation.web;

import com.silverpeas.annotation.Authenticated;
import com.silverpeas.socialNetwork.invitation.Invitation;
import com.silverpeas.socialNetwork.invitation.InvitationService;
import com.silverpeas.web.RESTWebService;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * It represents a resource published in the WEB that represents an invitation emitted by a user to
 * another one in the Silverpeas platform.
 *
 * The WEB resource belongs always to the current user in the session underlying at the HTTP
 * request. Then, it represents an invitation either sent or received by him. With a such policy,
 * the invitations belonging to another user cannot be fetched by him.
 */
@Service
@Scope("request")
@Path("invitations")
@Authenticated
public class InvitationResource extends RESTWebService {

  @Inject
  private InvitationService invitationService;

  @Path("inbox")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public InvitationEntity[] getReceivedInvitations() {
    List<Invitation> invitations = invitationService.getAllMyInvitationsReceive(Integer.valueOf(
            getUserDetail().getId()));
    return asWebEntity(invitations, locatedAt(getUriInfo().getAbsolutePathBuilder()));
  }

  @Path("outbox")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public InvitationEntity[] getSentInvitations() {
    List<Invitation> invitations = invitationService.getAllMyInvitationsSent(Integer.valueOf(
            getUserDetail().getId()));
    return asWebEntity(invitations, locatedAt(getUriInfo().getAbsolutePathBuilder()));
  }

  @Override
  public String getComponentId() {
    throw new UnsupportedOperationException("The InvitationResource doesn't belong to any component"
            + " instances");
  }

  private static UriBuilder locatedAt(final UriBuilder uri) {
    return uri;
  }

  private InvitationEntity[] asWebEntity(List<Invitation> invitations, UriBuilder baseUri) {
    InvitationEntity[] entities = new InvitationEntity[invitations.size()];
    baseUri.path("{invitationId}");
    for (int i = 0; i < invitations.size(); i++) {
      Invitation invitation = invitations.get(i);
      entities[i] = InvitationEntity.fromInvitation(invitation).withAsUri(baseUri.build(invitation.
              getId()));
    }
    return entities;
  }
}
