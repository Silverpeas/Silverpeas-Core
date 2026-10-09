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
package org.silverpeas.core.web.filter;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests on the composition of the security checkers: a value is allowed only if all of them
 * allow it, and the first rejection is the one told.
 *
 * @author mmoquillon
 */
class XssCheckerTest {

  private final XssChecker checker = new XssChecker(List.of(new XssPatternChecker(),
      new IFrameChecker(List.of("www.youtube.com"), "/silverpeas"),
      new ScriptChecker(List.of("scripts.example.org"), "/silverpeas", false)));

  @Test
  void valueAllowedByAllTheCheckersIsAllowed() {
    assertThat(checker.rejectionIn("<p>Hello <b>world</b></p>"), is(Optional.empty()));
    assertThat(checker.rejectionIn("<iframe src=\"https://www.youtube.com/embed/xyz\"></iframe>" +
        "<script src=\"/weblib/custom.js\"></script>"), is(Optional.empty()));
    assertThat(checker.areAllAllowedIn("<p>text</p>"), is(true));
  }

  @Test
  void valueRejectedByAnyCheckerIsRejectedForItsReason() {
    assertThat(checker.rejectionIn("<p>text</p><svg onload=\"alert(1)\"></svg>"),
        is(Optional.of("a forbidden element in \"<svg onload=\"alert(1)\"></svg>\"")));
    assertThat(checker.rejectionIn("<p>text</p><iframe src=\"https://www.evil.org/\"></iframe>"),
        is(Optional.of(
            "the source of an iframe isn't allowed in \"<iframe src=\"https://www.evil.org/\"></iframe>\"")));
    assertThat(checker.rejectionIn("<p>text</p><script src=\"/other/evil.js\"></script>"),
        is(Optional.of(
            "the source of a script isn't allowed in \"<script src=\"/other/evil.js\"></script>\"")));
    assertThat(checker.areAllAllowedIn("<svg>"), is(false));
  }

  /**
   * The checkers are consulted in their order and the first rejection is told, so that the reason
   * is the one of the most specific checker.
   */
  @Test
  void firstRejectionIsTold() {
    assertThat(checker.rejectionIn("<iframe src=\"https://www.evil.org/\" onload=\"alert(1)\">"),
        is(Optional.of(
            "an event callback attribute in \" onload=\"alert(1)\">\"")));
  }

  /**
   * Any checker can be composed, without the composite knowing it: this is how a new one can be
   * introduced.
   */
  @Test
  void anySecurityCheckerCanBeComposed() {
    final SecurityChecker forbiddingTheWordEvil = text -> text.contains("evil") ?
        Optional.of("the word evil in " + SecurityChecker.excerptOf(text, text.indexOf("evil"))) :
        Optional.empty();
    final XssChecker composite = new XssChecker(List.of(new XssPatternChecker(),
        forbiddingTheWordEvil));
    assertThat(composite.rejectionIn("<p>a good text</p>"), is(Optional.empty()));
    assertThat(composite.rejectionIn("<p>an evil text</p>"),
        is(Optional.of("the word evil in \"evil text</p>\"")));
    assertThat(composite.rejectionIn("<svg>evil</svg>"),
        is(Optional.of("a forbidden element in \"<svg>evil</svg>\"")));
  }
}
