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

import com.silverpeas.comment.model.Comment;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.silverpeas.comment.web.CommentEntityMatcher.*;

/**
 * Tests on the comment getting by the CommentResource web service.
 */
public class CommentGettingTest extends BaseCommentResourceTest {

  private UserDetail user;
  private String sessionKey;
  private Comment theComment;

  public CommentGettingTest() {
    super();
  }

  @Before
  public void createAUserAndAComment() {
    user = aUser();
    sessionKey = authenticate(user);
    theComment = aUser(user).commentTheResource(CONTENT_ID).inComponent(COMPONENT_INSTANCE_ID).
        andSaveItWithAsText("ceci est un commentaire");
  }

  @Test
  public void getACommentByANonAuthenticatedUser() {
    WebResource resource = resource();
    try {
      resource.path(RESOURCE_PATH + "/3").accept(MediaType.APPLICATION_JSON).get(String.class);
      fail("A non authenticated user shouldn't access the comment");
    } catch (UniformInterfaceException ex) {
      int recievedStatus = ex.getResponse().getStatus();
      int unauthorized = Status.UNAUTHORIZED.getStatusCode();
      assertThat(recievedStatus, is(unauthorized));
    }
  }

  @Test
  public void getACommentWithADeprecatedSession() {
    WebResource resource = resource();
    try {
      resource.path(RESOURCE_PATH + "/3").header(HEADER_SESSION_KEY, UUID.randomUUID().toString()).
          accept(MediaType.APPLICATION_JSON).get(String.class);
      fail("A user shouldn't access the comment through an expired session");
    } catch (UniformInterfaceException ex) {
      int recievedStatus = ex.getResponse().getStatus();
      int unauthorized = Status.UNAUTHORIZED.getStatusCode();
      assertThat(recievedStatus, is(unauthorized));
    }
  }

  @Test
  public void getANonAuthorizedComment() {
    denieAuthorizationToUsers();

    WebResource resource = resource();
    try {
      resource.path(RESOURCE_PATH + "/" + theComment.getCommentPK().getId()).
          header(HEADER_SESSION_KEY, sessionKey).
          accept(MediaType.APPLICATION_JSON).
          get(String.class);
      fail("A user shouldn't access a non authorized comment");
    } catch (UniformInterfaceException ex) {
      int recievedStatus = ex.getResponse().getStatus();
      int forbidden = Status.FORBIDDEN.getStatusCode();
      assertThat(recievedStatus, is(forbidden));
    }
  }

  @Test
  public void getAnUnexistingComment() {
    WebResource resource = resource();
    try {
      resource.path(RESOURCE_PATH + "/3").
          header(HEADER_SESSION_KEY, sessionKey).
          accept(MediaType.APPLICATION_JSON).
          get(String.class);
      fail("A user shouldn't get an unexisting comment");
    } catch (UniformInterfaceException ex) {
      int recievedStatus = ex.getResponse().getStatus();
      int notFound = Status.NOT_FOUND.getStatusCode();
      assertThat(recievedStatus, is(notFound));
    }
  }

  @Test
  public void getAComment() {
    WebResource resource = resource();
    CommentEntity entity = resource.path(RESOURCE_PATH + "/" + theComment.getCommentPK().getId()).
        header(HEADER_SESSION_KEY, sessionKey).
        accept(MediaType.APPLICATION_JSON).
        get(CommentEntity.class);
    assertNotNull(entity);
    assertThat(entity, matches(theComment));
  }

  @Test
  public void getAllComments() {
    Comment theComment1 = aUser(user).commentTheResource(CONTENT_ID).inComponent(
        COMPONENT_INSTANCE_ID).
        andSaveItWithAsText("ceci est un commentaire 1");
    Comment theComment2 = aUser(user).commentTheResource(CONTENT_ID).inComponent(
        COMPONENT_INSTANCE_ID).
        andSaveItWithAsText("ceci est un commentaire 2");
    Comment theComment3 = aUser(user).commentTheResource(CONTENT_ID).inComponent(
        COMPONENT_INSTANCE_ID).
        andSaveItWithAsText("ceci est un commentaire 3");

    CommentEntity[] entities = resource().path(RESOURCE_PATH).
        header(HEADER_SESSION_KEY, sessionKey).
        accept(MediaType.APPLICATION_JSON).
        get(CommentEntity[].class);
    assertNotNull(entities);
    assertThat(entities.length, equalTo(4));
  }

}