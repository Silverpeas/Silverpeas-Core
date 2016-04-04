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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import com.silverpeas.comment.CommentRuntimeException;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.web.ResourceDeletionTest;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.junit.Before;
import org.junit.Test;
import static com.silverpeas.comment.web.CommentTestResources.*;

/**
 * Tests on the comment deletion by the CommentResource web service.
 */
public class CommentDeletionTest extends ResourceDeletionTest<CommentTestResources> {

  private UserDetail user;
  private String sessionKey;
  private Comment theComment;

  public CommentDeletionTest() {
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

  @Test(expected = CommentRuntimeException.class)
  public void deleteAnExistingComment() {
    deleteAt(aResourceURI());
    getTestResources().getCommentService().getComment(theComment.getCommentPK());
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