/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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

import com.silverpeas.comment.BaseCommentTest;
import com.silverpeas.comment.model.Comment;
import static com.silverpeas.comment.web.CommentTestResources.*;
import com.silverpeas.web.ResourceCreationTest;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.sun.jersey.api.client.ClientResponse;
import javax.ws.rs.core.Response.Status;
import static org.hamcrest.Matchers.*;
import org.junit.AfterClass;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests on the creation of a comment through the CommentResource web service.
 */
public class CommentCreationTest extends ResourceCreationTest<CommentTestResources> {

  private UserDetail user;
  private String sessionKey;
  private CommentEntity theComment;
  private CommentEntity theCommentFromTool;

  public CommentCreationTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }

  @BeforeClass
  public static void prepareMessagingContext() throws Exception {
    BaseCommentTest.boostrapMessagingSystem();
  }

  @AfterClass
  public static void releaseMessagingContext() throws Exception {
    BaseCommentTest.shutdownMessagingSystem();
  }

  @Before
  public void prepareTestResources() {
    user = aUser();
    sessionKey = authenticate(user);
    theComment =
        CommentEntity.fromComment(theUser(user).commentTheResource(CONTENT_TYPE, CONTENT_ID).
            inComponent(COMPONENT_INSTANCE_ID).withAsText("ceci est un commentaire"));
    theCommentFromTool =
        CommentEntity.fromComment(theUser(user).commentTheResource(CONTENT_TYPE, CONTENT_ID).
            inComponent(TOOL_ID).withAsText("ceci est un commentaire"));
  }

  @Test
  public void postANewComment() {
    ClientResponse response = post(theComment, at(aResourceURI()));
    int recievedStatus = response.getStatus();
    int created = Status.CREATED.getStatusCode();
    assertThat(recievedStatus, is(created));
    assertThat(response.getEntity(CommentEntity.class), equalTo(theComment));
  }

  @Test
  public void postANewCommentFromTool() {
    ClientResponse response = post(theCommentFromTool, at(aToolResourceURI()));
    int recievedStatus = response.getStatus();
    int created = Status.CREATED.getStatusCode();
    assertThat(recievedStatus, is(created));
    assertThat(response.getEntity(CommentEntity.class), equalTo(theCommentFromTool));
  }

  /**
   * In a such situation, the comment is created as a new one.
   */
  @Test
  public void postAnAlreadyExistingComment() {
    Comment existingComment = theUser(user).commentTheResource(CONTENT_TYPE, CONTENT_ID).
            inComponent(COMPONENT_INSTANCE_ID).withAsText("coucou");
    getTestResources().save(existingComment);
    CommentEntity aComment = CommentEntity.fromComment(existingComment);

    ClientResponse response = post(aComment, at(aResourceURI()));
    int recievedStatus = response.getStatus();
    int created = Status.CREATED.getStatusCode();
    assertThat(recievedStatus, is(created));
    CommentEntity createdComment = response.getEntity(CommentEntity.class);
    assertThat(createdComment.getId(), not(aComment.getId()));
  }

  @Test
  public void postAnAlreadyExistingCommentFromTool() {
    Comment existingComment = theUser(user).commentTheResource(CONTENT_TYPE, CONTENT_ID).
        inComponent(TOOL_ID).withAsText("coucou");
    getTestResources().save(existingComment);
    CommentEntity aComment = CommentEntity.fromComment(existingComment);

    ClientResponse response = post(aComment, at(aToolResourceURI()));
    int recievedStatus = response.getStatus();
    int created = Status.CREATED.getStatusCode();
    assertThat(recievedStatus, is(created));
    CommentEntity createdComment = response.getEntity(CommentEntity.class);
    assertThat(createdComment.getId(), not(aComment.getId()));
  }

  @Test
  public void postAnInvalidComment() {
    CommentEntity aComment = new CommentEntity();
    ClientResponse response = post(aComment, at(aResourceURI()));
    int recievedStatus = response.getStatus();
    int badRequest = Status.BAD_REQUEST.getStatusCode();
    assertThat(recievedStatus, is(badRequest));
  }

  @Override
  public String aResourceURI() {
    return RESOURCE_PATH;
  }

  public String aToolResourceURI() {
    return TOOL_RESOURCE_PATH;
  }

  @Override
  public String anUnexistingResourceURI() {
    return RESOURCE_PATH + "/toto";
  }

  @Override
  public CommentEntity aResource() {
    return theComment;
  }

  @Override
  public String getSessionKey() {
    return sessionKey;
  }

  @Override
  public Class<?> getWebEntityClass() {
    return CommentEntity.class;
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[]{COMPONENT_INSTANCE_ID};
  }

  @Override
  public String[] getExistingTools() {
    return new String[] { TOOL_ID };
  }
}
