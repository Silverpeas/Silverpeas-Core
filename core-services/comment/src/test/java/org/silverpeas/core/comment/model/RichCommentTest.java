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
package org.silverpeas.core.comment.model;

import java.util.Date;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.core.ResourceReference;
import static org.junit.jupiter.api.Assertions.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class RichCommentTest {
  private Comment comment() {
    return new Comment(new CommentId("kmelia1", "1"), "2", "Publication", new ResourceReference("16", "kmelia1"), new Date());
  }
  @Test
  void historicalPlainTextIsNeverGuessedAsMarkdown() {
    var comment = comment();
    comment.setMessage("**literal** @[Alice](2)");
    assertNull(comment.getRichText());
    assertEquals("**literal** @[Alice](2)", comment.getMessage());
  }
  @Test
  void richSourceKeepsAReadableProjectionForAllExistingConsumers() {
    var comment = comment();
    comment.setRichText("**Bonjour** @[Alice](2)");
    assertEquals("**Bonjour** @[Alice](2)", comment.getRichText());
    assertEquals("Bonjour @Alice", comment.getMessage());
    comment.setMessage("Legacy API update");
    assertNull(comment.getRichText());
    assertEquals("Legacy API update", comment.getMessage());
  }
  @Test
  void sourceAndVisibleTextLimitsAreValidatedWithoutTruncation() {
    var comment = comment();
    assertThrows(IllegalArgumentException.class, () -> comment.setRichText("x".repeat(8001)));
    assertThrows(IllegalArgumentException.class, () -> comment.setRichText("x".repeat(2001)));
    assertThrows(IllegalArgumentException.class, () -> comment.setRichText("  "));
    comment.setRichText("**" + "x".repeat(2000) + "**");
    assertEquals(2000, comment.getMessage().length());
  }
}
