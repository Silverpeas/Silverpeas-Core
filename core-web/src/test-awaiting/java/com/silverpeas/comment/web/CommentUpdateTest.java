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
import com.silverpeas.comment.model.CommentPK;
import static com.silverpeas.comment.web.CommentTestResources.*;
import com.silverpeas.web.ResourceUpdateTest;
import com.stratelia.webactiv.beans.admin.UserDetail;
import static org.hamcrest.Matchers.equalTo;
import org.junit.AfterClass;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests on the update of a comment through the CommentResource web service.
 */
public class CommentUpdateTest extends ResourceUpdateTest<CommentTestResources> {

  private UserDetail user;
  private String sessionKey;
  private CommentEntity theComment;

  public CommentUpdateTest() {
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
    Comment commentToUseInTest =
        theUser(user).commentTheResource(CONTENT_TYPE, CONTENT_ID).inComponent(
            COMPONENT_INSTANCE_ID).withAsText("ceci est un commentaire");
    getTestResources().save(commentToUseInTest);
    theComment = CommentEntity.fromComment(commentToUseInTest);
  }

  @Test
  public void updateAnExistingComment() {
    theComment.newText("a new text");
    CommentEntity entity = putAt(aResourceURI(), theComment);
    assertThat(entity, equalTo(theComment));
    assertThat(entity.getText(), equalTo(theComment.getText()));
  }

  @Override
  public CommentEntity anInvalidResource() {
    Comment comment = theUser(user).commentTheResource(CONTENT_TYPE, CONTENT_ID).inComponent(
        COMPONENT_INSTANCE_ID).withAsText("ceci est un commentaire");
    comment.setCommentPK(new CommentPK("3", COMPONENT_INSTANCE_ID));
    return CommentEntity.fromComment(comment);
  }

  @Override
  public String aResourceURI() {
    return RESOURCE_PATH + "/" + theComment.getId();
  }

  @Override
  public String anUnexistingResourceURI() {
    return INVALID_RESOURCE_PATH + "/" + theComment.getId();
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
    return new String[] { COMPONENT_INSTANCE_ID };
  }
}
