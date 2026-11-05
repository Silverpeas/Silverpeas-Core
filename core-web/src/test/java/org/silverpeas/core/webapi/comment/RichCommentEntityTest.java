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
package org.silverpeas.core.webapi.comment;

import java.util.Date;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.comment.model.CommentId;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.webapi.profile.UserProfileEntity;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class RichCommentEntityTest {
  @Test
  void legacyRepresentationStaysPlainAndRichClientsOptIn() {
    var comment = new Comment(new CommentId("kmelia1", "1"), "2", "Publication",
        new ResourceReference("16", "kmelia1"), new Date());
    comment.setRichText("**Hello** @[Alice](3)");
    var user = mock(User.class);
    try (var users = mockStatic(User.class); var profiles = mockStatic(UserProfileEntity.class)) {
      users.when(() -> User.getById("2")).thenReturn(user);
      profiles.when(() -> UserProfileEntity.fromUser(user)).thenReturn(mock(UserProfileEntity.class));
      var rich = CommentEntity.fromComment(comment, "en").withRichRepresentation(true);
      assertEquals("Hello @Alice", rich.getText());
      assertEquals("MARKDOWN", rich.getFormat());
      assertEquals(comment.getRichText(), rich.getRichText());
      var legacy = CommentEntity.fromComment(comment, "en").withRichRepresentation(false);
      assertEquals("Hello @Alice", legacy.getText());
      assertEquals("PLAIN", legacy.getFormat());
      assertNull(legacy.getRichText());
    }
  }

  @Test
  void serverDerivesThePlainTextAndRejectsIncompleteOrUnknownRichFormats() throws Exception {
    var entity = new CommentEntity();
    field(entity, "componentId", "kmelia1");
    field(entity, "resourceId", "16");
    field(entity, "resourceType", "Publication");
    field(entity, "text", "Forged projection");
    var author = mock(UserProfileEntity.class);
    when(author.getId()).thenReturn("2");
    field(entity, "author", author);
    field(entity, "format", "MARKDOWN");
    assertThrows(IllegalArgumentException.class, entity::toComment);
    field(entity, "richText", "**Actual** @[Alice](3)");
    assertEquals("Actual @Alice", entity.toComment().getMessage());
    field(entity, "format", "HTML");
    assertThrows(IllegalArgumentException.class, entity::toComment);
    field(entity, "format", "PLAIN");
    assertEquals("Forged projection", entity.toComment().getMessage());
    assertNull(entity.toComment().getRichText());
  }

  private void field(CommentEntity entity, String name, Object value) throws Exception {
    var field = CommentEntity.class.getDeclaredField(name);
    field.setAccessible(true);
    field.set(entity, value);
  }
}
