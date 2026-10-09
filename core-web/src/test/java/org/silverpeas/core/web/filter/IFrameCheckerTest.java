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
import static org.hamcrest.Matchers.containsString;
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

  /**
   * The weblib application serves, beside Silverpeas, the resources specific to the platform.
   */
  @Test
  void iframeOnRelativeUrlWithinWeblibIsAllowed() {
    assertAllowed("<iframe src=\"/weblib/pages/page.html\"></iframe>");
    assertAllowed("<iframe src=\"/weblib\"></iframe>");
    assertRejected("<iframe src=\"/weblibother/page.html\"></iframe>");
    assertRejected("<iframe src=\"/weblib/../other/app\"></iframe>");
  }

  /**
   * The files attached to the contributions, like the PDF documents, are commonly embedded within
   * the contents. Unlike the scripts, the iframes have not to be protected here from the code such
   * a file could carry: the file servers of Silverpeas serve it along with a content security
   * policy forbidding it any script.
   */
  @Test
  void iframeOnAnAttachedFileIsAllowed() {
    final String attachedFile =
        "/silverpeas/attached_file/componentId/kmelia1/attachmentId/7088b9d6/lang/fr/name/";
    assertAllowed("<iframe src=\"" + attachedFile + "document.pdf\"></iframe>");
    assertAllowed("<iframe src=\"" + attachedFile + "page.html\"></iframe>");
    assertAllowed("<iframe src=\"/silverpeas/File/7088b9d6\"></iframe>");
    assertAllowed("<iframe src=\"https://silverpeas.example.org" + attachedFile +
        "document.pdf\"></iframe>");
  }

  /**
   * The browsers accept a URL with some characters forbidden by the URI syntax, a whitespace for
   * example, by encoding them: such a URL is checked as the browsers would send it.
   */
  @Test
  void iframeOnUrlWithCharactersTheBrowsersEncodeIsCheckedAsEncoded() {
    assertAllowed("<iframe src=\"/silverpeas/Rkmelia/kmelia1/Main?name=my file.pdf\"></iframe>");
    assertAllowed("<iframe src=\"https://www.youtube.com/embed/my video\"></iframe>");
    assertRejected("<iframe src=\"https://www.evil.org/my video\"></iframe>");
    assertRejected("<iframe src=\"/silverpeas/../other/my file.pdf\"></iframe>");
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
  void iframeWithEventHandlerIsRejected() {
    assertRejected("<iframe src=\"https://www.youtube.com/\" onload=\"alert(1)\"></iframe>");
    assertRejected("<iframe src=\"https://www.youtube.com/\" ONFOCUSIN=alert(1)></iframe>");
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

  /**
   * The source is read the way the browsers read it: they know far more entities than the ones of
   * HTML 4, and they decode them even when they are loosely written. A source going out of the
   * allowed ones once decoded has to be rejected, however it is written.
   */
  @Test
  void iframeWhoseSourceIsDisguisedByEntitiesIsRejected() {
    assertRejected("<iframe src=\"https&colon;//www.evil.org/\"></iframe>");
    assertRejected("<iframe src=\"&sol;&sol;www.evil.org/\"></iframe>");
    assertRejected("<iframe src=\"https://www.evil.org&sol;@www.youtube.com/\"></iframe>");
    assertRejected("<iframe src=\"/silverpeas/&period;&period;/other/app\"></iframe>");
    assertRejected("<iframe src=\"/silverpeas/.&#46/other/app\"></iframe>");
    assertRejected("<iframe src=\"/silverpeas/&#x2e;&#x2E;/other/app\"></iframe>");
    assertRejected("<iframe src=\"/silverpeas/.&Tab;./other/app\"></iframe>");
  }

  @Test
  void iframeWhoseAllowedSourceIsWrittenWithEntitiesIsAllowed() {
    assertAllowed("<iframe src=\"https&colon;//www.youtube.com/embed/xyz\"></iframe>");
    assertAllowed("<iframe src=\"/silverpeas/Rkmelia&sol;kmelia1&#47;Main\"></iframe>");
    // an ampersand which introduces no entity is the separator of the parameters of a query
    assertAllowed("<iframe src=\"https://www.youtube.com/embed?a=1&b=2&copy=3\"></iframe>");
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

  /**
   * The reason of a rejection tells what is wrong with the iframe, so that the writer of a content
   * can fix it.
   */
  @Test
  void rejectionTellsItsReason() {
    assertRejectedFor("<p>text</p><iframe src=\"https://www.evil.org/\"></iframe>",
        "the source of an iframe isn't allowed in \"<iframe src=\"https://www.evil.org/\"></iframe>\"");
    assertRejectedFor("<iframe></iframe>",
        "the source of an iframe isn't allowed in \"<iframe></iframe>\"");
    assertRejectedFor("<iframe src=\"https://www.youtube.com/\" srcdoc=\"x\"></iframe>",
        "an iframe with a srcdoc attribute in \"<iframe src=\"https://www.youtube.com/\" srcdoc=");
    assertRejectedFor("<iframe/src=\"https://www.youtube.com/\"></iframe>",
        "an iframe that cannot be strictly parsed in \"<iframe/src=");
    assertRejectedFor("<iframe src=\"https://www.youtube.com/\" ONFOCUSIN=alert(1)></iframe>",
        "an iframe with an event handler attribute in \"<iframe src=\"https://www.youtube.com/\" " +
            "ONFOCUSIN=alert(1)></iframe>\"");
    assertThat(checker.rejectionIn("<iframe src=\"https://www.youtube.com/embed/xyz\"></iframe>"),
        is(Optional.empty()));
  }

  private void assertRejectedFor(final String text, final String reason) {
    final Optional<String> rejection = checker.rejectionIn(text);
    assertThat(text, rejection.isPresent(), is(true));
    assertThat(rejection.get(), containsString(reason));
  }

  private void assertAllowed(final String text) {
    assertThat(text, checker.areAllAllowedIn(text), is(true));
  }

  private void assertRejected(final String text) {
    assertThat(text, checker.areAllAllowedIn(text), is(false));
  }
}
