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

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.ApplicationService;
import org.silverpeas.core.ApplicationServiceProvider;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.comment.model.CommentId;
import org.silverpeas.core.comment.service.notification.CommentEvent;
import org.silverpeas.core.comment.service.notification.CommentUserNotificationService;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class CommentMentionNotificationTest {
  @TestManagedMock private CommentService comments;
  @TestManagedMock private CommentMentionService mentions;
  @TestManagedMock private ApplicationServiceProvider applications;
  @TestedBean private TestNotificationService notifications;

  public static class TestNotificationService extends CommentUserNotificationService {
    private NotificationMetaData sent;
    @Override
    protected void notifyUsers(NotificationMetaData notification) { sent = notification; }
  }

  @Test
  void updateNotifiesOnlyNewMentionsAndNeverResendsExistingOnes() {
    var before = comment();
    before.setRichText("@[Existing](3)");
    var after = comment();
    after.setRichText("@[Existing](3) @[New](4)");
    var resource = resource();
    when(mentions.recipients(after, resource)).thenReturn(Set.of("3", "4"));
    try (var helper = mockStatic(UserNotificationHelper.class)) {
      notifications.onUpdate(new CommentEvent(ResourceEvent.Type.UPDATE, before, after));
      var notification = ArgumentCaptor.forClass(CommentMentionNotification.class);
      helper.verify(() -> UserNotificationHelper.buildAndSend(notification.capture()));
      assertEquals(Set.of("4"), notification.getValue().getUserIdsToNotify());
    }
    when(mentions.recipients(after, resource)).thenReturn(Set.of("3"));
    try (var helper = mockStatic(UserNotificationHelper.class)) {
      notifications.onUpdate(new CommentEvent(ResourceEvent.Type.UPDATE, before, after));
      helper.verifyNoInteractions();
    }
  }

  @Test
  void creationCombinesNativeInterestedUsersWithMentionsWithoutDuplicates() throws Exception {
    var comment = comment();
    var author = mock(User.class);
    when(author.getId()).thenReturn("2");
    var owner = mock(User.class);
    when(owner.getId()).thenReturn("3");
    var resource = resource();
    when(resource.getIdentifier()).thenReturn(ContributionIdentifier.from("kmelia1", "16", "Publication"));
    when(resource.getContributionType()).thenReturn("Publication");
    when(resource.getCreator()).thenReturn(owner);
    when(resource.canBeAccessedBy(owner)).thenReturn(true);
    when(comments.getAllCommentsOnResource(anyString(), any())).thenReturn(List.of());
    when(comments.getComment(any(CommentId.class))).thenReturn(comment);
    when(mentions.recipients(comment, resource)).thenReturn(Set.of("3", "4"));
    try (var users = mockStatic(User.class); var helper = mockStatic(UserNotificationHelper.class)) {
      users.when(() -> User.getById("2")).thenReturn(author);
      var metadata = mock(NotificationMetaData.class);
      helper.when(() -> UserNotificationHelper.build(any())).thenReturn(metadata);
      notifications.onCreation(new CommentEvent(ResourceEvent.Type.CREATION, comment));
      var builder = ArgumentCaptor.forClass(CommentUserNotification.class);
      helper.verify(() -> UserNotificationHelper.build(builder.capture()));
      assertEquals(Set.of("3", "4"), builder.getValue().getUserIdsToNotify());
      assertSame(metadata, notifications.sent);
    }
  }

  private Contribution resource() {
    var resource = mock(Contribution.class);
    var application = mock(ApplicationService.class);
    when(applications.getApplicationServiceById("kmelia1")).thenReturn(Optional.of(application));
    when(application.<Contribution>getContributionById(any())).thenReturn(Optional.of(resource));
    return resource;
  }

  private Comment comment() {
    return new Comment(new CommentId("kmelia1", "1"), "2", "Publication",
        new ResourceReference("16", "kmelia1"), new Date());
  }
}
