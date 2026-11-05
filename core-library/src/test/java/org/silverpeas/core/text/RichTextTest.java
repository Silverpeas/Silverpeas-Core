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
package org.silverpeas.core.text;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class RichTextTest {
  @Test
  void projectionRemovesFormattingAndTechnicalMentionIds() {
    assertEquals("Bonjour @Alice lien", RichText.plainText("**Bonjour** @[Alice](2) [lien](https://example.org)"));
    assertEquals("", RichText.plainText(null));
    assertEquals("Corrected", RichText.plainText("~~Corrected~~"));
  }

  @Test
  void onlyCanonicalMentionsOutsideCodeAreRecipients() {
    assertEquals(Set.of("2", "3"), RichText.mentionIds("**@[Alice](2)** @[Bob](3) @[Alice](2)"));
    assertTrue(RichText.mentionIds("`@[Alice](2)`\n\n```\n@[Bob](3)\n```\n\n\\@[Carol](4) [Link](5) &#64;[Other](6)").isEmpty());
    assertTrue(RichText.mentionIds(null).isEmpty());
    assertEquals(Set.of("2"), RichText.mentionIds("🚀 ~~@[Alice](2)~~"));
  }
}
