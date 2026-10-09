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

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit tests on the detection by patterns of the XSS injections in a value, and on the reason given
 * for a rejection.
 *
 * @author mmoquillon
 */
class XssPatternCheckerTest {

  private final XssPatternChecker checker = new XssPatternChecker();

  @Test
  void harmlessValueIsAllowed() {
    assertAllowed("<p>Hello <b>world</b></p>");
    assertAllowed("https://www.silverpeas.org/page?online=true");
    // the iframes and the scripts are the matter of their own checker
    assertAllowed("<iframe src=\"https://www.evil.org/\"></iframe>");
    assertAllowed("<script>alert(1)</script>");
  }

  @Test
  void forbiddenElementIsRejectedWithItsReason() {
    assertRejected("<p>text</p><svg onload=\"alert(1)\"></svg>",
        "a forbidden element in \"<svg onload=\"alert(1)\"></svg>\"");
    assertRejected("< math>", "a forbidden element in \"< math>\"");
    assertRejected("<details open ontoggle=alert(1)>", "a forbidden element in \"<details");
  }

  @Test
  void eventCallbackIsRejectedWithItsReason() {
    assertRejected("<img src=x onerror=\"alert(1)\">",
        "an event callback attribute in \" onerror=\"alert(1)\">\"");
    assertRejected("<img src=\"x\"onerror=alert(1)>", "an event callback attribute in \"\"onerror=");
  }

  @Test
  void scriptingSchemeIsRejectedWithItsReason() {
    assertRejected("<a href=\"javascript:alert(1)\">link</a>",
        "a scripting scheme as an attribute value in \" href=\"javascript:alert(1)\">link</a>\"");
    assertRejected("<form><button formaction=javascript&colon;alert(1)>Go",
        "a scripting scheme as an attribute value in \" formaction=javascript&colon;");
  }

  @Test
  void excerptOfTheRejectedValueIsBounded() {
    final String longText = "<svg>" + "a".repeat(200);
    final Optional<String> rejection = checker.rejectionIn(longText);
    assertThat(rejection.isPresent(), is(true));
    assertThat(rejection.get(), startsWith("a forbidden element in \"<svg>aaaa"));
    assertThat(rejection.get(), endsWith("\u2026\""));
    assertThat(rejection.get().length(), lessThan(120));
  }

  private void assertAllowed(final String value) {
    assertThat(value, checker.rejectionIn(value), is(Optional.empty()));
  }

  private void assertRejected(final String value, final String reason) {
    final Optional<String> rejection = checker.rejectionIn(value);
    assertThat(value, rejection.isPresent(), is(true));
    assertThat(rejection.get(), containsString(reason));
  }
}
