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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.ApplicationService;
import org.silverpeas.core.ApplicationServiceProvider;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.comment.model.CommentId;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class CommentMentionServiceTest {
  @TestManagedMock private OrganizationController organization;
  @TestManagedMock private ApplicationServiceProvider applications;
  @TestedBean private CommentMentionService mentions;
  private Contribution resource;
  private User requester;
  private UserDetail alice;
  private UserDetail denied;
  private UserDetail removed;
  private final ContributionIdentifier target = ContributionIdentifier.from("kmelia1", "16", "Publication");

  @BeforeEach
  void prepare() {
    resource = mock(Contribution.class);
    requester = mock(User.class);
    when(requester.getId()).thenReturn("author");
    var application = mock(ApplicationService.class);
    when(applications.getApplicationServiceById("kmelia1")).thenReturn(Optional.of(application));
    when(application.<Contribution>getContributionById(any())).thenReturn(Optional.of(resource));
    alice = user("2", "Élodie Martin");
    denied = user("3", "Private user");
    removed = user("4", "Removed");
    when(removed.isDeactivatedState()).thenReturn(true);
    when(resource.canBeAccessedBy(requester)).thenReturn(true);
    when(resource.canBeAccessedBy(alice)).thenReturn(true);
    when(organization.<User>getAllUsers("kmelia1")).thenReturn(new User[]{alice, denied, removed});
  }

  private UserDetail user(String id, String name) {
    var user = mock(UserDetail.class);
    when(user.getId()).thenReturn(id);
    when(user.getDisplayedName()).thenReturn(name);
    when(organization.getUserDetail(id)).thenReturn(user);
    return user;
  }

  @Test
  void candidatesRespectTheResourceRightsActiveAccountsAndAccentInsensitiveSearch() {
    assertEquals(List.of(new CommentMentionService.Candidate("2", "Élodie Martin")), mentions.candidates(target, requester, "elodie"));
    when(resource.canBeAccessedBy(requester)).thenReturn(false);
    assertThrows(SecurityException.class, () -> mentions.candidates(target, requester, ""));
  }

  @Test
  void recipientsAreDistinctAuthorizedAndNeverIncludeTheCommentAuthorOrCode() {
    var comment = new Comment(new CommentId("kmelia1", "1"), "author", "Publication", new ResourceReference("16", "kmelia1"), new Date());
    comment.setRichText("@[Élodie](2) @[Again](2) @[Denied](3) @[Removed](4) `@[Code](5)` @[Self](author)");
    assertEquals(Set.of("2"), mentions.recipients(comment, resource));
    when(resource.canBeAccessedBy(alice)).thenReturn(false);
    assertTrue(mentions.recipients(comment, resource).isEmpty());
    comment.setMessage("@[Élodie](2)");
    assertTrue(mentions.recipients(comment, resource).isEmpty());
  }
}
