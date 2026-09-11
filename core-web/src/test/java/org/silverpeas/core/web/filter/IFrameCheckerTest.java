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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests on the checking of the iframes.
 *
 * @author mmoquillon
 */
class IFrameCheckerTest {

  private final IFrameChecker checker =
      new IFrameChecker(List.of("www.youtube.com", "Silverpeas.Example.org"), "/silverpeas");

  @Test
  void textWithoutIFrameIsAllowed() {
    assertAllowed("<p>Hello <b>world</b></p>");
    assertAllowed("</iframe>");
  }

  @Test
  void iframeOnHttpsUrlOfAnAllowedHostIsAllowed() {
    assertAllowed("<p><iframe src=\"https://www.youtube.com/embed/xyz\" width=\"560\" " +
        "allowfullscreen></iframe></p>");
    assertAllowed("<iframe src=\"https://silverpeas.example.org:8443/silverpeas/Rkmelia\"/>");
    assertAllowed("<IFRAME SRC='HTTPS://WWW.YOUTUBE.COM/embed/xyz'></IFRAME>");
    assertAllowed("<iframe src=https://www.youtube.com/embed/xyz></iframe>");
    assertAllowed("<iframe src=\"https://www.youtube.com/embed?a=1&amp;b=2\"></iframe>");
    assertAllowed("<iframe src=\"https://www.youtube.com/a\"></iframe><iframe " +
        "src=\"https://silverpeas.example.org/b\"></iframe>");
  }

  @Test
  void iframeOnRelativeUrlWithinSilverpeasIsAllowed() {
    assertAllowed("<iframe src=\"/silverpeas/Rkmelia/kmelia1/Main?id=3&amp;v=1\"></iframe>");
    assertAllowed("<iframe src=\"/silverpeas\"></iframe>");
    assertAllowed("<iframe src=\"Rkmelia/kmelia1/Main\"></iframe>");
    assertAllowed("<iframe src=\"./Main.jsp#top\"></iframe>");
    assertAllowed("<iframe src=\"?id=3\"></iframe>");

    final String iframe = "<iframe src=\"/silverpeas/Rkmelia/kmelia1/Main\"></iframe>";
    assertThat(new IFrameChecker(List.of(), "/silverpeas/").areAllAllowedIn(iframe), is(true));
  }

  @Test
  void iframeOnRelativeUrlOutsideSilverpeasIsRejected() {
    assertRejected("<iframe src=\"/other/app\"></iframe>");
    assertRejected("<iframe src=\"/silverpeasother/app\"></iframe>");
    assertRejected("<iframe src=\"///www.evil.org/\"></iframe>");
    assertRejected("<iframe src=\"/\\www.evil.org/\"></iframe>");
  }

  @Test
  void iframeOnRelativeUrlGoingUpInPathsIsRejected() {
    assertRejected("<iframe src=\"/silverpeas/../other/app\"></iframe>");
    assertRejected("<iframe src=\"../other/app\"></iframe>");
    assertRejected("<iframe src=\"Rkmelia/../../other\"></iframe>");
    assertRejected("<iframe src=\"/silverpeas/%2e%2e/other\"></iframe>");
    assertRejected("<iframe src=\"/silverpeas/%2E./other\"></iframe>");
    assertRejected("<iframe src=\"/silverpeas/&#46;&#46;/other\"></iframe>");
    assertRejected("<iframe src=\"/silverpeas/..;/other\"></iframe>");
    assertRejected("<iframe src=\"/silverpeas/File?path=../../etc/passwd\"></iframe>");
    assertRejected("<iframe src=\"/silverpeas/File?path=%2e%2e/etc/passwd\"></iframe>");
  }

  @Test
  void iframeOnNonHttpsUrlIsRejected() {
    assertRejected("<iframe src=\"http://www.youtube.com/embed/xyz\"></iframe>");
    assertRejected("<iframe src=\"//www.youtube.com/embed/xyz\"></iframe>");
    assertRejected("<iframe src=\"javascript:alert(1)\"></iframe>");
    assertRejected("<iframe src=\"data:text/html,hello\"></iframe>");
  }

  @Test
  void iframeOnANonAllowedHostIsRejected() {
    assertRejected("<iframe src=\"https://www.evil.org/\"></iframe>");
    assertRejected("<iframe src=\"https://www.youtube.com.evil.org/\"></iframe>");
    assertRejected("<iframe src=\"https://www.youtube.com@www.evil.org/\"></iframe>");
    assertRejected("<iframe src=\"https://www.evil.org/?https://www.youtube.com/\"></iframe>");
    assertRejected("<iframe src=\"https://www.youtube.com\\@www.evil.org/\"></iframe>");
    assertRejected("<iframe src=\"https&#58;//www.evil.org/\"></iframe>");
    assertRejected("<iframe src=\"https://www.youtube.com/a\"></iframe><iframe " +
        "src=\"https://www.evil.org/b\"></iframe>");
  }

  @Test
  void iframeWithoutSingleSrcIsRejected() {
    assertRejected("<iframe></iframe>");
    assertRejected("<iframe width=\"560\"></iframe>");
    assertRejected("<iframe src=\"https://www.youtube.com/\" " +
        "src=\"https://www.evil.org/\"></iframe>");
    assertRejected("<iframe src=\"https://www.youtube.com/\" srcdoc=\"&lt;script&gt;\"></iframe>");
  }

  @Test
  void iframeThatCannotBeStrictlyParsedIsRejected() {
    assertRejected("< iframe src=\"https://www.youtube.com/\"></iframe>");
    assertRejected("<iframe/src=\"https://www.youtube.com/\"></iframe>");
    assertRejected("<iframe src=\"https://www.youtube.com/\"");
    assertRejected("<iframe title=\">\" src=\"https://www.evil.org/\"></iframe>");
    assertRejected("<iframe src=\"https://www.youtube.com/\" title=\">\" srcdoc=\"x\"></iframe>");
    assertRejected("<iframe src=https://www.youtube.com/ a=\\\"x srcdoc=y b=\\\"></iframe>");
  }

  private void assertAllowed(final String text) {
    assertThat(text, checker.areAllAllowedIn(text), is(true));
  }

  private void assertRejected(final String text) {
    assertThat(text, checker.areAllAllowedIn(text), is(false));
  }
}
