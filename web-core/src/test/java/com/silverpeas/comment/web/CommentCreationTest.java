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

import java.util.UUID;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * Unit tests on the creation of a comment through the CommentResource web service.
 */
public class CommentCreationTest extends BaseCommentResourceTest {

  private UserDetail user;
  private String sessionKey;
  private CommentEntity theComment;

  @Before
  public void createAUserAndPrepareAComment() {
    user = aUser();
    sessionKey = authenticate(user);
    theComment = CommentEntity.fromComment(aUser(user).commentTheResource(CONTENT_ID).
        inComponent(COMPONENT_INSTANCE_ID).withAsText("ceci est un commentaire"));
  }

  @Test
  public void postACommentByANonAuthenticatedUser() {
    WebResource resource = resource();
    ClientResponse response = resource.path(RESOURCE_PATH).
        accept(MediaType.APPLICATION_JSON).
        type(MediaType.APPLICATION_JSON).
        post(ClientResponse.class, theComment);
    int recievedStatus = response.getStatus();
    int unauthorized = Status.UNAUTHORIZED.getStatusCode();
    assertThat(recievedStatus, is(unauthorized));
  }

  @Test
  public void postACommentWithADeprecatedSession() {
    WebResource resource = resource();
    ClientResponse response = resource.path(RESOURCE_PATH).
        header(HEADER_SESSION_KEY, UUID.randomUUID().toString()).
        accept(MediaType.APPLICATION_JSON).
        type(MediaType.APPLICATION_JSON).
        post(ClientResponse.class, theComment);
    int recievedStatus = response.getStatus();
    int unauthorized = Status.UNAUTHORIZED.getStatusCode();
    assertThat(recievedStatus, is(unauthorized));
  }

  @Test
  public void postANonAuthorizedComment() {
    denieAuthorizationToUsers();

    WebResource resource = resource();
    ClientResponse response = resource.path(RESOURCE_PATH).
        header(HEADER_SESSION_KEY, sessionKey).
        accept(MediaType.APPLICATION_JSON).
        type(MediaType.APPLICATION_JSON).
        post(ClientResponse.class, theComment);
    int recievedStatus = response.getStatus();
    int forbidden = Status.FORBIDDEN.getStatusCode();
    assertThat(recievedStatus, is(forbidden));
  }

  @Test
  public void postANewComment() {
    WebResource resource = resource();
    ClientResponse response = resource.path(RESOURCE_PATH).
        header(HEADER_SESSION_KEY, sessionKey).
        accept(MediaType.APPLICATION_JSON).
        type(MediaType.APPLICATION_JSON).
        post(ClientResponse.class, theComment);
    int recievedStatus = response.getStatus();
    int created = Status.CREATED.getStatusCode();
    assertThat(recievedStatus, is(created));
    assertThat(response.getEntity(CommentEntity.class), equalTo(theComment));
  }

  /**
   * In a such situation, the comment is created as a new one.
   */
  @Test
  public void postAnAlreadyExistingComment() {
    CommentEntity aComment = CommentEntity.fromComment(aUser(user).commentTheResource(CONTENT_ID).
        inComponent(COMPONENT_INSTANCE_ID).andSaveItWithAsText("coucou"));
    WebResource resource = resource();
    ClientResponse response = resource.path(RESOURCE_PATH).
        header(HEADER_SESSION_KEY, sessionKey).
        accept(MediaType.APPLICATION_JSON).
        type(MediaType.APPLICATION_JSON).
        post(ClientResponse.class, aComment);
    int recievedStatus = response.getStatus();
    int created = Status.CREATED.getStatusCode();
    assertThat(recievedStatus, is(created));
    CommentEntity createdComment = response.getEntity(CommentEntity.class);
    assertThat(createdComment.getId(), not(aComment.getId()));
  }

  @Test
  public void postAnInvalidComment() {
    CommentEntity aComment = new CommentEntity();
    WebResource resource = resource();
    ClientResponse response = resource.path(RESOURCE_PATH).
        header(HEADER_SESSION_KEY, sessionKey).
        accept(MediaType.APPLICATION_JSON).
        type(MediaType.APPLICATION_JSON).
        post(ClientResponse.class, aComment);
    int recievedStatus = response.getStatus();
    int failure = Status.INTERNAL_SERVER_ERROR.getStatusCode();
    assertThat(recievedStatus, is(failure));
  }
}
