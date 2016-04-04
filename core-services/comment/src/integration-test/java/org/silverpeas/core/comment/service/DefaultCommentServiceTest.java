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

package org.silverpeas.core.comment.service;

import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.comment.test.CommentBuilder;
import org.silverpeas.core.comment.test.MyCommentActionListener;
import org.silverpeas.core.comment.test.WarBuilder4Comment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.rule.DbSetupRule;

import javax.inject.Inject;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

/**
 * Integration tests on the DefaultCommentService behaviour.
 */
@RunWith(Arquillian.class)
public class DefaultCommentServiceTest {

  private static final String TABLE_CREATION_SCRIPT = "/org/silverpeas/core/comment/create-database.sql";
  private static final String DATASET_SCRIPT = "/org/silverpeas/core/comment/comment-dataset.sql";
  private static final String TEST_RESOURCE_TYPE = "RtypeTest";

  @Inject
  private MyCommentActionListener listener;
  @Inject
  public CommentService commentService;

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom(TABLE_CREATION_SCRIPT).loadInitialDataSetFrom(DATASET_SCRIPT);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4Comment
        .onWarForTestClass(DefaultCommentServiceTest.class)
        .build();
  }

  @Before
  public void setUp() {
    assertNotNull(listener);
    assertNotNull(commentService);
    listener.reset();
  }

  @After
  public void tearDown() {
    listener.reset();
  }

  /**
   * Empty test just to check all is ok before any test running.
   */
  @Test
  public void emptyTest() {
    assertTrue(true);
  }

  /**
   * When a comment is added, then any listeners subscribed for a such events should be invoked.
   * @throws Exception if an error occurs during the test execution.
   */
  @Test
  public void subscribersShouldBeInvokedAtCommentAdding() throws Exception {
    getCommentService()
        .createComment(CommentBuilder.getBuilder().buildWith("Toto", "Vu à la télé"));
    assertThat(listener.isInvoked(), is(true));
    assertEquals(1, listener.getInvocationCount());
    assertTrue(listener.isCommentAdded());
    assertFalse(listener.isCommentRemoved());
  }

  /**
   * When a comment is deleted, then any listeners subscribed for a such events should be invoked.
   * @throws Exception if an error occurs during the test execution.
   */
  @Test
  public void subscribersShouldBeInvokedAtCommentDeletion() throws Exception {
    CommentService commentController = getCommentService();
    List<Comment> allComments = commentController.getAllCommentsOnPublication(TEST_RESOURCE_TYPE,
        CommentBuilder.getResourcePrimaryPK());
    commentController.deleteComment(allComments.get(0).getCommentPK());
    assertThat(listener.isInvoked(), is(true));
    assertEquals(1, listener.getInvocationCount());
    assertFalse(listener.isCommentAdded());
    assertTrue(listener.isCommentRemoved());
  }

  /**
   * When several comments are deleted, then any listeners subscribed for a such events should be
   * invoked.
   * @throws Exception if an error occurs during the test execution.
   */
  @Test
  public void subscribersShouldBeInvokedAtSeveralCommentsDeletion() throws Exception {
    CommentService service = getCommentService();
    List<Comment> allComments = service.getAllCommentsOnPublication(TEST_RESOURCE_TYPE,
        CommentBuilder.getResourcePrimaryPK());
    service.deleteAllCommentsOnPublication(TEST_RESOURCE_TYPE,
        CommentBuilder.getResourcePrimaryPK());
    assertThat(listener.isInvoked(), is(true));
    assertEquals(allComments.size(), listener.getInvocationCount());
    assertFalse(listener.isCommentAdded());
    assertTrue(listener.isCommentRemoved());
  }

  /**
   * Gets a DefaultCommentService instance with which tests has to be performed.
   * @return a DefaultCommentService object to test.
   */
  protected CommentService getCommentService() {
    return commentService;
  }

}
