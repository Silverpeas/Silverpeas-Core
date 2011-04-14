/*
 * Copyright (C) 2000 - 2011 Silverpeas
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
package com.silverpeas.comment.service;

import com.silverpeas.comment.model.Comment;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests on the DefaultCommentService behaviour.
 */
public class DefaultCommentServiceTest {

  public DefaultCommentServiceTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Empty test just to check all is ok before any test running.
   */
  @Test
  public void emptyTest() {
    assertTrue(true);
  }

  /**
   * When a comment is added, then any callbacks registered for a such events should be invoked.
   * @throws Exception if an error occurs during the test execution.
   */
  @Test
  public void callbacksShouldBeInvokedAtCommentAdding() throws Exception {
    CommentCallBack callback = new CommentCallBack();
    callback.subscribeForCommentAdding();
    getCommentService().createComment(CommentBuilder.getBuilder().buildWith("Toto", "Vu à la télé"));
    assertTrue(callback.isInvoked());
    assertEquals(1, callback.getInvocationCount());
    assertTrue(callback.isCommentAdded());
    assertFalse(callback.isCommentRemoved());
  }

  /**
   * When a comment is deleted, then any callbacks registered for a such events should be invoked.
   * @throws Exception if an error occurs during the test execution.
   */
  @Test
  public void callbacksShouldBeInvokedAtCommentDeletion() throws Exception {
    CommentCallBack callback = new CommentCallBack();
    callback.subscribeForCommentRemoving();

    CommentService commentController = getCommentService();
    List<Comment> allComments = commentController.getAllCommentsOnPublication(
        CommentBuilder.getResourcePrimaryPK());
    commentController.deleteComment(allComments.get(0).getCommentPK());
    assertTrue(callback.isInvoked());
    assertEquals(1, callback.getInvocationCount());
    assertFalse(callback.isCommentAdded());
    assertTrue(callback.isCommentRemoved());
  }

  /**
   * When several comments are deleted, then any callbacks registered for a such events
   * should be invoked.
   * @throws Exception if an error occurs during the test execution.
   */
  @Test
  public void callbacksShouldBeInvokedAtSeveralCommentsDeletion() throws Exception {
    CommentCallBack callback = new CommentCallBack();
    callback.subscribeForCommentRemoving();

    CommentService commentController = getCommentService();
    List<Comment> allComments = commentController.getAllCommentsOnPublication(
        CommentBuilder.getResourcePrimaryPK());
    commentController.deleteAllCommentsOnPublication(CommentBuilder.getResourcePrimaryPK());
    assertTrue(callback.isInvoked());
    assertEquals(allComments.size(), callback.getInvocationCount());
    assertFalse(callback.isCommentAdded());
    assertTrue(callback.isCommentRemoved());
  }

  @Test
  public void invocationWithIllegalArgumentsShouldDoesNothing() throws Exception {
    CommentCallBack callback = new CommentCallBack();
    callback.subscribeForCommentAdding();
    getCommentService().createComment(CommentBuilder.getBuilder().buildOrphelanWith("Toto", "Vu à la télé"));
    assertFalse(callback.isInvoked());
  }

  /**
   * Gets a DefaultCommentService instance with which tests has to be performed.
   * @return a DefaultCommentService object to test.
   */
  protected CommentService getCommentService() {
    return new MyDefaultCommentService();
  }

  /**
   * The callback to use within the unit tests.
   */
  private static class CommentCallBack extends CallBackOnCommentAction {

    private int invocation = 0;
    private boolean commentAdded = false;
    private boolean commentRemoved = false;

    @Override
    public void subscribe() {
      throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Has this callback been invoked?
     * @return true if this callback has been invoked at least one time, false otherwise.
     */
    public boolean isInvoked() {
      return invocation > 0;
    }

    /**
     * Is a comment added?
     * @return true if the callback was invoked for comment adding.
     */
    public boolean isCommentAdded() {
      return commentAdded;
    }

    /**
     * Is a comment removed?
     * @return true if the callback was invoked for comment removing.
     */
    public boolean isCommentRemoved() {
      return commentRemoved;
    }

    /**
     * Gets the count of invocation of this callback by a CallBackManager.
     * @return the invocation count.
     */
    public int getInvocationCount() {
      return invocation;
    }

    @Override
    public void commentAdded(int authorId, String resourceId, Comment addedComment) {
      invocation++;
      commentAdded = true;
    }

    @Override
    public void commentRemoved(int authorId, String resourceId, Comment removedComment) {
      invocation++;
      commentRemoved = true;
    }
  }
}