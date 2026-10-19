/*
 * Copyright (C) 2000 - 2026 Silverpeas
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

import jakarta.transaction.Status;
import jakarta.transaction.UserTransaction;
import javax.naming.InitialContext;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.comment.test.AfterSuccessfulCommentUpdateListener;
import org.silverpeas.core.comment.test.CommentBuilder;
import org.silverpeas.core.comment.test.WarBuilder4Comment;
import org.silverpeas.core.test.integration.rule.DbSetupRule;
import org.silverpeas.core.util.ServiceProvider;

import static org.junit.Assert.*;

/** Verifies that AFTER_SUCCESS consumers observe only committed updates. */
@RunWith(Arquillian.class)
public class CommentUpdateNotificationIT {

  @Rule
  public DbSetupRule database = DbSetupRule
      .createTablesFrom("/org/silverpeas/core/comment/create-database.sql")
      .loadInitialDataSetFrom("/org/silverpeas/core/comment/comment-dataset.sql");

  private CommentService service;
  private AfterSuccessfulCommentUpdateListener listener;

  @Deployment
  public static Archive<?> deployment() {
    return WarBuilder4Comment.onWarForTestClass(CommentUpdateNotificationIT.class).build();
  }

  @Before
  public void prepareServices() {
    service = ServiceProvider.getService(CommentService.class);
    listener = ServiceProvider.getService(AfterSuccessfulCommentUpdateListener.class);
    listener.reset();
  }

  @Test
  public void committedUpdateDeliversBothStatesOnce() throws Exception {
    Comment comment = existingComment();
    String previousMessage = comment.getMessage();
    comment.setMessage("Committed update");
    UserTransaction transaction = InitialContext.doLookup("java:comp/UserTransaction");
    transaction.begin();
    try {
      service.updateComment(comment);
      assertNull(listener.getLastEvent());
      transaction.commit();
    } finally {
      if (transaction.getStatus() == Status.STATUS_ACTIVE) {
        transaction.rollback();
      }
    }
    assertEquals(1, listener.getInvocationCount());
    assertEquals(previousMessage, listener.getLastEvent().getTransition().getBefore().getMessage());
    assertEquals("Committed update",
        listener.getLastEvent().getTransition().getAfter().getMessage());
    assertEquals(comment.getIdentifier(),
        listener.getLastEvent().getTransition().getAfter().getIdentifier());
    assertEquals("Committed update", service.getComment(comment.getIdentifier()).getMessage());
  }

  @Test
  public void rolledBackUpdateIsNotDelivered() throws Exception {
    Comment comment = existingComment();
    String previousMessage = comment.getMessage();
    comment.setMessage("Rolled back update");
    UserTransaction transaction = InitialContext.doLookup("java:comp/UserTransaction");
    transaction.begin();
    try {
      service.updateComment(comment);
      assertNull(listener.getLastEvent());
    } finally {
      transaction.rollback();
    }
    assertNull(listener.getLastEvent());
    assertEquals(0, listener.getInvocationCount());
    assertEquals(previousMessage, service.getComment(comment.getIdentifier()).getMessage());
  }

  private Comment existingComment() {
    return service.getAllCommentsOnResource("RtypeTest", CommentBuilder.getResourceReference())
        .get(0);
  }
}
