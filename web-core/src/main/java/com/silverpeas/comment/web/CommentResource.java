/*
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
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

package com.silverpeas.comment.web;

import com.silverpeas.accesscontrol.ComponentAccessController;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.model.CommentPK;
import com.silverpeas.comment.service.CommentService;
import com.stratelia.silverpeas.peasCore.SessionInfo;
import com.stratelia.silverpeas.peasCore.SessionManager;
import com.stratelia.webactiv.beans.admin.UserDetail;
import javax.inject.Inject;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * A REST Web resource representing a given comment.
 * It is a web service that provides an access to a comment referenced by its URL.
 */
@Service
@Scope("request")
@Path("comments/{componentId}/{contentId}")
public class CommentResource {

  @Inject
  private CommentService commentService;

  @HeaderParam("X-Silverpeas-SessionKey")
  private String sessionKey;

  @PathParam("componentId")
  private String componentId;

  @PathParam("contentId")
  private String contentId;

  /**
   * Gets the JSON representation of the specified existing comment.
   * If the comment doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authentified, a 401 HTTP code is returned.
   * If the user isn't authorized to access the comment, a 403 is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param commentId the unique identifier of the comment.
   * @return the JSON representation of the asked comment.
   */
  @Path("{commentId}")
  @Produces(MediaType.APPLICATION_JSON)
  public String getComment(@PathParam("commentId") String commentId) {
    checkUserPriviledges();
    try {
      Comment comment = commentService.getComment(new CommentPK(commentId, componentId));
      
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
    return "";
  }

  /**
   * Gets the key of the user session.
   * @return the user session key.
   */
  private String getUserSessionKey() {
    return sessionKey;
  }

  /**
   * Gets the identifier of the Silverpeas instance to which the commented content belongs.
   * @return the Silverpeas component instance identifier.
   */
  public String getComponentId() {
    return componentId;
  }

  /**
   * Gets the identifier of the content that is commentable.
   * @return the identifier of the commentable content.
   */
  public String getContentId() {
    return contentId;
  }

  /**
   * Checks the user has the correct priviledges to access the underlying referenced resource.
   * It the check fail, a WebException is thrown with the HTTP status code set according to the
   * failure.
   */
  private void checkUserPriviledges() {
    SessionManager sessionManager = SessionManager.getInstance();
    SessionInfo sessionInfo = sessionManager.getUserDataSession(sessionKey);
    if (sessionInfo == null) {
      throw new WebApplicationException(Status.UNAUTHORIZED);
    }
    UserDetail user = sessionInfo.getUserDetail();

    ComponentAccessController accessController = new ComponentAccessController();
    if (!accessController.isUserAuthorized(user.getId(), componentId)) {
      throw new WebApplicationException(Status.FORBIDDEN);
    }
  }


}
