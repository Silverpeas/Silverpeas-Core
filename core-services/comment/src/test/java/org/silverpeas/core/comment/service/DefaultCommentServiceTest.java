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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.comment.dao.CommentDAO;
import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.comment.model.CommentId;
import org.silverpeas.core.comment.service.notification.CommentEvent;
import org.silverpeas.core.comment.service.notification.CommentEventNotifier;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class DefaultCommentServiceTest {

  @TestManagedMock
  private CommentDAO commentDAO;
  @TestManagedMock
  private CommentEventNotifier notifier;
  @TestedBean
  private DefaultCommentService service;

  private Comment before;
  private Comment after;

  @BeforeEach
  void prepareCommentStates() {
    before = comment("Original message");
    after = comment("Updated message");
    when(commentDAO.getComment(after.getIdentifier())).thenReturn(before);
  }

  @Test
  void updateNotifiesOnceAfterPersistenceWithBothStates() {
    service.updateComment(after);

    var order = inOrder(commentDAO, notifier);
    order.verify(commentDAO).getComment(after.getIdentifier());
    order.verify(commentDAO).updateComment(after);
    order.verify(notifier).notifyEventOn(ResourceEvent.Type.UPDATE, before, after);
    verifyNoMoreInteractions(notifier);
    var event = new CommentEvent(ResourceEvent.Type.UPDATE, before, after);
    assertEquals("Original message", event.getTransition().getBefore().getMessage());
    assertEquals("Updated message", event.getTransition().getAfter().getMessage());
    assertEquals(after.getResourceReference(),
        event.getTransition().getAfter().getResourceReference());
  }

  @Test
  void failedPersistenceDoesNotNotify() {
    doThrow(new IllegalStateException("Persistence failure")).when(commentDAO).updateComment(after);

    assertThrows(IllegalStateException.class, () -> service.updateComment(after));

    verifyNoInteractions(notifier);
  }

  @Test
  void failedReadDoesNotWriteOrNotify() {
    when(commentDAO.getComment(after.getIdentifier()))
        .thenThrow(new IllegalStateException("Read failure"));

    assertThrows(IllegalStateException.class, () -> service.updateComment(after));

    verify(commentDAO, never()).updateComment(any());
    verifyNoInteractions(notifier);
  }

  @Test
  void indexedUpdateNotifiesOnlyOnceAndStillIndexes() {
    try (var index = mockStatic(IndexEngineProxy.class)) {
      service.updateAndIndexComment(after);

      verify(commentDAO).updateComment(after);
      verify(notifier).notifyEventOn(ResourceEvent.Type.UPDATE, before, after);
      verifyNoMoreInteractions(notifier);
      index.verify(() -> IndexEngineProxy.addIndexEntry(any(FullIndexEntry.class)));
    }
  }

  private Comment comment(String message) {
    var result = new Comment(new CommentId("kmelia2", "42"), "10", "Publication",
        new ResourceReference("61", "kmelia2"), new Date(0));
    result.setMessage(message);
    return result;
  }
}
