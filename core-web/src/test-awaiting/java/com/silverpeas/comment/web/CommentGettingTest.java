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
import static com.silverpeas.comment.web.CommentEntityMatcher.matches;
import static com.silverpeas.comment.web.CommentTestResources.*;
import com.silverpeas.web.ResourceGettingTest;
import com.stratelia.webactiv.beans.admin.UserDetail;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.Matchers.equalTo;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests on the comment getting by the CommentResource web service.
 */
public class CommentGettingTest extends ResourceGettingTest<CommentTestResources> {

  private UserDetail user;
  private String sessionKey;
  private Comment theComment;

  public CommentGettingTest() {
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
        theUser(user).commentTheResource(CONTENT_TYPE, CONTENT_ID)
            .inComponent(COMPONENT_INSTANCE_ID).
            withAsText("ceci est un commentaire");
    getTestResources().save(theComment);
  }

  @Test
  public void getAComment() {
    CommentEntity entity = getAt(aResourceURI(), CommentEntity.class);
    assertNotNull(entity);
    assertThat(entity, matches(theComment));
  }

  @Test
  public void getAllComments() {
    Comment theComment1 = theUser(user).commentTheResource(CONTENT_TYPE, CONTENT_ID).inComponent(
        COMPONENT_INSTANCE_ID).
        withAsText("ceci est un commentaire 1");
    Comment theComment2 = theUser(user).commentTheResource(CONTENT_TYPE, CONTENT_ID).inComponent(
        COMPONENT_INSTANCE_ID).
        withAsText("ceci est un commentaire 2");
    Comment theComment3 = theUser(user).commentTheResource(CONTENT_TYPE, CONTENT_ID).inComponent(
        COMPONENT_INSTANCE_ID).
        withAsText("ceci est un commentaire 3");
    getTestResources().save(theComment1, theComment2, theComment3);

    CommentEntity[] entities = getAt(RESOURCE_PATH, CommentEntity[].class);
    assertNotNull(entities);
    assertThat(entities.length, equalTo(4));
    List<CommentEntity> listOfComments = Arrays.asList(entities);
    assertTrue(listOfComments.contains(CommentEntity.fromComment(theComment1)));
    assertTrue(listOfComments.contains(CommentEntity.fromComment(theComment2)));
    assertTrue(listOfComments.contains(CommentEntity.fromComment(theComment3)));
  }

  @Override
  public String aResourceURI() {
    return RESOURCE_PATH + "/" + theComment.getCommentPK().getId();
  }

  @Override
  public String anUnexistingResourceURI() {
    return RESOURCE_PATH + "/3";
  }

  @Override
  public Comment aResource() {
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
