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

import com.silverpeas.comment.CommentRuntimeException;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.model.CommentPK;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.silverpeas.comment.web.CommentEntityMatcher.*;
import static com.silverpeas.rest.RESTWebService.*;

/**
 * Tests on the comment deletion by the CommentResource web service.
 */
public class CommentDeletionTest extends BaseCommentResourceTest {

  private UserDetail user;
  private String sessionKey;
  private Comment theComment;

  public CommentDeletionTest() {
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
  public void deleteACommentByANonAuthenticatedUser() {
    WebResource resource = resource();
    try {
      resource.path(RESOURCE_PATH + "/3").accept(MediaType.APPLICATION_JSON).delete();
      fail("A non authenticated user shouldn't delete the comment");
    } catch (UniformInterfaceException ex) {
      int recievedStatus = ex.getResponse().getStatus();
      int unauthorized = Status.UNAUTHORIZED.getStatusCode();
      assertThat(recievedStatus, is(unauthorized));
    }
  }

  @Test
  public void deleteACommentWithADeprecatedSession() {
    WebResource resource = resource();
    try {
      resource.path(RESOURCE_PATH + "/3").header(HTTP_SESSIONKEY, UUID.randomUUID().toString()).
          accept(MediaType.APPLICATION_JSON).delete();
      fail("A user shouldn't delete the comment through an expired session");
    } catch (UniformInterfaceException ex) {
      int recievedStatus = ex.getResponse().getStatus();
      int unauthorized = Status.UNAUTHORIZED.getStatusCode();
      assertThat(recievedStatus, is(unauthorized));
    }
  }

  @Test
  public void deleteANonAuthorizedComment() {
    denieAuthorizationToUsers();

    WebResource resource = resource();
    try {
      resource.path(RESOURCE_PATH + "/" + theComment.getCommentPK().getId()).
          header(HTTP_SESSIONKEY, sessionKey).
          accept(MediaType.APPLICATION_JSON).
          delete();
      fail("A user shouldn't delete a non authorized comment");
    } catch (UniformInterfaceException ex) {
      int recievedStatus = ex.getResponse().getStatus();
      int forbidden = Status.FORBIDDEN.getStatusCode();
      assertThat(recievedStatus, is(forbidden));
    }
  }

  @Test
  public void deleteAnUnexistingComment() {
    WebResource resource = resource();
    try {
      resource.path(RESOURCE_PATH + "/3").
          header(HTTP_SESSIONKEY, sessionKey).
          accept(MediaType.APPLICATION_JSON).
          delete();
      fail("A user shouldn't delete an unexisting comment");
    } catch (UniformInterfaceException ex) {
      int recievedStatus = ex.getResponse().getStatus();
      int notFound = Status.NOT_FOUND.getStatusCode();
      assertThat(recievedStatus, is(notFound));
    }
  }

  @Test(expected=CommentRuntimeException.class)
  public void deleteAnExistingComment(){
    WebResource resource = resource();
    resource.path(RESOURCE_PATH + "/" + theComment.getCommentPK().getId()).
        header(HTTP_SESSIONKEY, sessionKey).
        accept(MediaType.APPLICATION_JSON).
        delete();
    getCommentService().getComment(theComment.getCommentPK());
  }

}