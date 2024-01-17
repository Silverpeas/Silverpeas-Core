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
import org.silverpeas.core.test.integration.rule.DbSetupRule;

import javax.inject.Inject;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Integration tests on the DefaultCommentService behaviour.
 */
@RunWith(Arquillian.class)
public class DefaultCommentServiceIT {

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
        .onWarForTestClass(DefaultCommentServiceIT.class)
        .build();
  }

  @Before
  public void setUp() {
    assertThat(listener, notNullValue());
    assertThat(commentService, notNullValue());
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
    assertThat(true, is(true));
  }

  /**
   * When a comment is added, then any listeners subscribed for a such events should be invoked.
   */
  @Test
  public void subscribersShouldBeInvokedAtCommentAdding() {
    getCommentService()
        .createComment(CommentBuilder.getBuilder().buildWith("10", "Vu à la télé"));
    assertThat(listener.isInvoked(), is(true));
    assertThat(1, is(listener.getInvocationCount()));
    assertThat(listener.isCommentAdded(), is(true));
    assertThat(listener.isCommentRemoved(), is(false));
  }

  /**
   * When a comment is deleted, then any listeners subscribed for a such events should be invoked.
   */
  @Test
  public void subscribersShouldBeInvokedAtCommentDeletion() {
    CommentService commentController = getCommentService();
    List<Comment> allComments = commentController.getAllCommentsOnResource(TEST_RESOURCE_TYPE,
        CommentBuilder.getResourceReference());
    commentController.deleteComment(allComments.get(0).getIdentifier());
    assertThat(listener.isInvoked(), is(true));
    assertThat(1, is(listener.getInvocationCount()));
    assertThat(listener.isCommentAdded(), is(false));
    assertThat(listener.isCommentRemoved(), is(true));
  }

  /**
   * When several comments are deleted, then any listeners subscribed for a such events should be
   * invoked.
   */
  @Test
  public void subscribersShouldBeInvokedAtSeveralCommentsDeletion() {
    CommentService service = getCommentService();
    List<Comment> allComments = service.getAllCommentsOnResource(TEST_RESOURCE_TYPE,
        CommentBuilder.getResourceReference());
    service.deleteAllCommentsOnResource(TEST_RESOURCE_TYPE,
        CommentBuilder.getResourceReference());
    assertThat(listener.isInvoked(), is(true));
    assertThat(allComments.size(), is(listener.getInvocationCount()));
    assertThat(listener.isCommentAdded(), is(false));
    assertThat(listener.isCommentRemoved(), is(true));
  }

  /**
   * Gets a DefaultCommentService instance with which tests has to be performed.
   * @return a DefaultCommentService object to test.
   */
  protected CommentService getCommentService() {
    return commentService;
  }

}
