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
package org.silverpeas.core.webapi.comment;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.core.comment.CommentRuntimeException;
import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.comment.model.CommentId;
import org.silverpeas.core.comment.service.CommentService;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.core.web.rs.RESTWebService;
import org.silverpeas.core.web.rs.UserPrivilegeValidation;
import org.silverpeas.core.web.rs.annotation.Authorized;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.util.List;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;

/**
 * A REST Web resource representing a given comment. It is a web service that provides access to
 * a comment referenced by its URL.
 */
@RequestScoped
@Path(CommentResource.PATH + "/{componentId}/{contentType}/{contentId}")
@Authorized
public class CommentResource extends RESTWebService {

  static final String PATH = "comments";

  @Inject
  private CommentService commentService;
  @PathParam("componentId")
  private String componentId;
  @PathParam("contentType")
  private String contentType;
  @PathParam("contentId")
  private String contentId;

  @Inject
  private PublicationService publicationService;

  @Override
  protected String getResourceBasePath() {
    return PATH;
  }

  /**
   * Gets the JSON representation of the specified existing comment. If the comment doesn't exist, a
   * 404 HTTP code is returned. If the user isn't authenticated, a 401 HTTP code is returned. If the
   * user isn't authorized to access the comment, a 403 is returned. If a problem occurs when
   * processing the request, a 503 HTTP code is returned.
   * @param onCommentId the unique identifier of the comment.
   * @return the response to the HTTP GET request with the JSON representation of the asked comment.
   */
  @GET
  @Path("{commentId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CommentEntity getComment(@PathParam("commentId") String onCommentId) {
    try {
      CommentId id = new CommentId(inComponentId(), onCommentId);
      Comment theComment = commentService().getComment(id);
      URI commentURI = getUri().getRequestUri();
      return asWebEntity(theComment, identifiedBy(commentURI));
    } catch (CommentRuntimeException ex) {
      SilverLogger.getLogger(this).error(ex);
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex);
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets the JSON representation of all the comments on referred the resource. If the user isn't
   * authenticated, a 401 HTTP code is returned. If the user isn't authorized to access the comment,
   * a 403 is returned. If a problem occurs when processing the request, a 503 HTTP code is
   * returned.
   * @return the response to the HTTP GET request with the JSON representation of the comments on
   * the referred resource.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CommentEntity[] getAllComments() {
    try {
      ResourceReference ref = new ResourceReference(onContentId(), inComponentId());
      List<Comment> theComments = commentService().getAllCommentsOnResource(onContentType(), ref);
      return asWebEntities(theComments);
    } catch (CommentRuntimeException ex) {
      SilverLogger.getLogger(this).error(ex);
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex);
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Creates a new comment from its JSON representation and returns it with its URI identifying it
   * in Silverpeas. The unique identifier of the comment isn't taken into account, so if the comment
   * already exist, it is then cloned with a new identifier (thus with a new URI). If the user isn't
   * authenticated, a 401 HTTP code is returned. If the user isn't authorized to save the comment, a
   * 403 is returned. If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param commentToSave the comment to save in Silverpeas.
   * @return the response to the HTTP POST request with the JSON representation of the saved
   * comment.
   */
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response saveNewComment(final CommentEntity commentToSave) {
    checkIsValid(commentToSave);
    try {
      Comment comment = commentToSave.toComment();
      Comment savedComment;
      if (commentToSave.isIndexed()) {
        savedComment = commentService().createAndIndexComment(comment);
      } else {
        savedComment = commentService().createComment(comment);
      }
      URI commentURI =
          getUri().getRequestUriBuilder().path(savedComment.getId()).build();
      return Response.created(commentURI).
          entity(asWebEntity(savedComment, identifiedBy(commentURI))).build();
    } catch (CommentRuntimeException ex) {
      SilverLogger.getLogger(this).error(ex);
      throw new WebApplicationException(ex, Status.CONFLICT);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex);
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Updates the comment from its JSON representation and returns it once updated. If the comment to
   * update doesn't match with the requested one, a 400 HTTP code is returned. If the comment
   * doesn't exist, a 404 HTTP code is returned. If the user isn't authenticated, a 401 HTTP code is
   * returned. If the user isn't authorized to save the comment, a 403 is returned. If a problem
   * occurs when processing the request, a 503 HTTP code is returned.
   * @param commentId the unique identifier of the comment to update.
   * @param commentToUpdate the comment to update in Silverpeas.
   * @return the response to the HTTP PUT request with the JSON representation of the updated
   * comment.
   */
  @PUT
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("{commentId}")
  public CommentEntity updateComment(@PathParam("commentId") String commentId,
      final CommentEntity commentToUpdate) {
    checkIsValid(commentToUpdate);
    if (!commentToUpdate.getId().equals(commentId)) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }
    CommentId id = new CommentId(inComponentId(), commentId);
    try {
      Comment comment = commentService().getComment(id);
      if (!comment.canBeModifiedBy(getUser())) {
        throw new ForbiddenException();
      }
      comment.setMessage(commentToUpdate.getText());
      if (commentToUpdate.isIndexed()) {
        commentService().updateAndIndexComment(comment);
      } else {
        commentService().updateComment(comment);
      }
      URI commentURI = getUri().getRequestUriBuilder().path(comment.getId()).build();
      return asWebEntity(comment, identifiedBy(commentURI));
    } catch (CommentRuntimeException | SilverpeasRuntimeException ex) {
      SilverLogger.getLogger(this).error(ex);
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (WebApplicationException ex) {
      throw ex;
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex);
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Deletes the specified existing comment. If the comment doesn't exist, nothing is done, so that
   * the HTTP DELETE request remains idempotent as defined in the HTTP specification. If the user
   * isn't authenticated, a 401 HTTP code is returned. If the user isn't authorized to access the
   * comment, a 403 is returned. If a problem occurs when processing the request, a 503 HTTP code is
   * returned.
   * @param onCommentId the unique identifier of the comment to delete.
   */
  @DELETE
  @Path("{commentId}")
  public void deleteComment(@PathParam("commentId") String onCommentId) {
    try {
      CommentId id = new CommentId(inComponentId(), onCommentId);
      final Comment comment = commentService().getComment(id);
      if (!comment.canBeDeletedBy(getUser())) {
        throw new ForbiddenException();
      }
      commentService().deleteComment(id);
    } catch (CommentRuntimeException | SilverpeasRuntimeException ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (WebApplicationException ex) {
      throw ex;
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex);
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets the identifier of the Silverpeas instance to which the commented content belongs.
   * @return the Silverpeas component instance identifier.
   */
  protected String inComponentId() {
    return getComponentId();
  }

  /**
   * Gets the type of the content that is commentable.
   * @return the type of the commentable content.
   */
  protected String onContentType() {
    return contentType;
  }

  /**
   * Gets the identifier of the content that is commentable.
   * @return the identifier of the commentable content.
   */
  protected String onContentId() {
    return contentId;
  }

  /**
   * Gets a business service on comments.
   * @return a comment service instance.
   */
  protected CommentService commentService() {
    return commentService;
  }

  /**
   * Converts the specified list of comments into their corresponding web entities.
   * @param comments the comments to convert.
   * @return an array with the corresponding comment entities.
   */
  protected CommentEntity[] asWebEntities(List<Comment> comments) {
    CommentEntity[] entities = new CommentEntity[comments.size()];
    for (int i = 0; i < comments.size(); i++) {
      Comment comment = comments.get(i);
      URI commentURI = getUri().getRequestUriBuilder().path(comment.getId()).build();
      entities[i] = asWebEntity(comment, identifiedBy(commentURI));
    }
    return entities;
  }

  /**
   * Converts the comment into its corresponding web entity.
   * @param comment the comment to convert.
   * @param commentURI the URI of the comment.
   * @return the corresponding comment entity.
   */
  protected CommentEntity asWebEntity(final Comment comment, URI commentURI) {
    return CommentEntity.fromComment(comment).withURI(commentURI)
        .withCurrentUserLanguage(getUserPreferences().getLanguage());
  }

  protected URI identifiedBy(URI uri) {
    return uri;
  }

  @Override
  public String getComponentId() {
    return this.componentId;
  }

  protected String getContentType() {
    return this.contentType;
  }

  protected String getContentId() {
    return this.contentId;
  }

  /**
   * Check the specified comment is valid. A comment is valid if the following attributes are set:
   * componentId, resourceId, text and its author identifier.
   * @param theComment the comment to validate.
   */
  protected void checkIsValid(final CommentEntity theComment) {
    if (getUser().isAnonymous() || getUser().isAccessGuest()) {
      throw new WebApplicationException("anonymous or guest user cannot manage comments", FORBIDDEN);
    }
    if (!theComment.getComponentId().equals(getComponentId()) ||
        !theComment.getResourceType().equals(
            getContentType()) || !theComment.getResourceId().equals(
            getContentId())) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
  }

  @Override
  public void validateUserAuthorization(final UserPrivilegeValidation validation) {
    if (PublicationDetail.TYPE.equals(getContentType())) {
      PublicationDetail publi =
          publicationService.getDetail(new PublicationPK(getContentId(), getComponentId()));
      validation.validateUserAuthorizationOnPublication(getHttpServletRequest(), getUser(), publi);
    } else {
      super.validateUserAuthorization(validation);
    }
  }
}
