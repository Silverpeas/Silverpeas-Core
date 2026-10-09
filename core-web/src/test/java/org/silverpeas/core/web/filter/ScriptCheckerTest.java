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
 * Unit tests on the checking of the scripts.
 *
 * @author mmoquillon
 */
class ScriptCheckerTest {

  private static final List<String> ALLOWED_HOSTS =
      List.of("scripts.example.org", "Silverpeas.Example.org");

  private final ScriptChecker checker = new ScriptChecker(ALLOWED_HOSTS, "/silverpeas", false);

  @Test
  void textWithoutScriptIsAllowed() {
    assertAllowed("<p>Hello <b>world</b></p>");
    assertAllowed("a script, some scripts and a description");
  }

  @Test
  void scriptLoadedFromSilverpeasIsAllowed() {
    assertAllowed("<script src=\"/silverpeas/util/javaScript/silverpeas.js\"></script>");
    assertAllowed("<p>before</p><script type=\"text/javascript\" src=\"/silverpeas/js/custom.js\">" +
        "</script><p>after</p>");
    assertAllowed("<SCRIPT SRC='/silverpeas/custom.js' defer></SCRIPT>");
    assertAllowed("<script src=/silverpeas/custom.js async></script>");
    assertAllowed("<script src=\"/silverpeas/custom.js?v=2&amp;min=true\"> </script >");
    assertAllowed("<script src=\"util/javaScript/custom.js\"></script>");
    assertAllowed("<script src=\"/silverpeas/a.js\"></script><script src=\"/silverpeas/b.js\">" +
        "</script>");
  }

  @Test
  void scriptLoadedFromWeblibIsAllowed() {
    assertAllowed("<script src=\"/weblib/custom.js\"></script>");
    assertAllowed("<script type=\"text/javascript\" src=\"/weblib/js/custom.min.js\"></script>");
  }

  @Test
  void scriptLoadedFromAnHttpsUrlOfAnAllowedHostIsAllowed() {
    assertAllowed("<script src=\"https://scripts.example.org/lib/widget.js\"></script>");
    assertAllowed("<script src=\"https://silverpeas.example.org:8443/weblib/custom.js\"></script>");
    assertAllowed("<script src=\"https://scripts.example.org/api.js?key=x&amp;v=2\"></script>");
  }

  @Test
  void scriptLoadedFromANonAllowedLocationIsRejected() {
    assertRejected("<script src=\"/other/app.js\"></script>");
    assertRejected("<script src=\"/weblibother/custom.js\"></script>");
    assertRejected("<script src=\"https://www.evil.org/weblib/custom.js\"></script>");
    assertRejected("<script src=\"http://scripts.example.org/lib/widget.js\"></script>");
    assertRejected("<script src=\"//scripts.example.org/lib/widget.js\"></script>");
    assertRejected("<script src=\"https://scripts.example.org@www.evil.org/x.js\"></script>");
    assertRejected("<script src=\"data:text/javascript,alert(1)\"></script>");
    assertRejected("<script src=\"/weblib/a.js\"></script><script src=\"/other/b.js\"></script>");
  }

  @Test
  void scriptLoadedFromAPathGoingUpIsRejected() {
    assertRejected("<script src=\"/weblib/../other/evil.js\"></script>");
    assertRejected("<script src=\"/silverpeas/../other/evil.js\"></script>");
    assertRejected("<script src=\"../other/evil.js\"></script>");
    assertRejected("<script src=\"/silverpeas/%2e%2e/other/evil.js\"></script>");
    assertRejected("<script src=\"/silverpeas/..;/other/evil.js\"></script>");
  }

  /**
   * The source is read the way the browsers read it: they know far more entities than the ones of
   * HTML 4 and they decode them even when they are loosely written. A source going out of the
   * allowed ones once decoded has to be rejected, however it is written.
   */
  @Test
  void scriptWhoseSourceIsDisguisedByEntitiesIsRejected() {
    assertRejected("<script src=\"https&colon;//www.evil.org/evil.js\"></script>");
    assertRejected("<script src=\"https&#58;//www.evil.org/evil.js\"></script>");
    assertRejected("<script src=\"&sol;&sol;www.evil.org/evil.js\"></script>");
    assertRejected("<script src=\"https://www.evil.org&sol;@scripts.example.org/x.js\"></script>");
    assertRejected("<script src=\"/silverpeas/&period;&period;/other/evil.js\"></script>");
    assertRejected("<script src=\"/silverpeas/&#46;&#46;/other/evil.js\"></script>");
    assertRejected("<script src=\"/silverpeas/.&#46/other/evil.js\"></script>");
    assertRejected("<script src=\"/silverpeas/&#x2e;&#x2E;/other/evil.js\"></script>");
    assertRejected("<script src=\"/silverpeas/.&Tab;./other/evil.js\"></script>");
  }

  @Test
  void scriptWhoseAllowedSourceIsWrittenWithEntitiesIsAllowed() {
    assertAllowed("<script src=\"https&colon;//scripts.example.org/lib/widget.js\"></script>");
    assertAllowed("<script src=\"/weblib&sol;js&#47;custom&period;js\"></script>");
    // an ampersand which introduces no entity is the separator of the parameters of a query
    assertAllowed("<script src=\"https://scripts.example.org/api.js?key=x&v=2\"></script>");
  }

  /**
   * The files attached to the contributions are uploaded by the users: they can be loaded as
   * scripts only once explicitly allowed.
   */
  @Test
  void scriptLoadedFromAnAttachedFileIsAllowedOnlyOnceExplicitlyEnabled() {
    final String script = "<script src=\"/silverpeas/attached_file/componentId/kmelia1/" +
        "attachmentId/7088b9d6/lang/fr/name/custom.js\"></script>";
    assertRejected(script);
    assertRejected("<script src=\"/silverpeas/File/7088b9d6\"></script>");
    assertRejected("<script src=\"/silverpeas/attached&lowbar;file/custom.js\"></script>");
    assertThat(new ScriptChecker(ALLOWED_HOSTS, "/silverpeas", true).areAllAllowedIn(script),
        is(true));
  }

  @Test
  void scriptCarryingCodeIsRejected() {
    assertRejected("<script>alert(1)</script>");
    assertRejected("<script type=\"text/javascript\">alert(1)</script>");
    assertRejected("<script src=\"/weblib/custom.js\">alert(1)</script>");
    assertRejected("<script src=\"/weblib/custom.js\"></script><script>alert(1)</script>");
  }

  @Test
  void scriptWithoutSingleSrcIsRejected() {
    assertRejected("<script></script>");
    assertRejected("<script type=\"text/javascript\"></script>");
    assertRejected("<script src=\"/weblib/custom.js\" src=\"/other/evil.js\"></script>");
  }

  @Test
  void scriptThatCannotBeStrictlyParsedIsRejected() {
    assertRejected("< script src=\"/weblib/custom.js\"></script>");
    assertRejected("<script/src=\"/weblib/custom.js\"></script>");
    assertRejected("<script src=\"/weblib/custom.js\"");
    assertRejected("<script src=\"/weblib/custom.js\">");
    assertRejected("<script title=\">\" src=\"/other/evil.js\"></script>");
    assertRejected("<scripts src=\"/weblib/custom.js\"></scripts>");
  }

  /**
   * A closing tag is accepted only as the end of an allowed script: alone, it would close a script
   * of the page into which the text is rendered.
   */
  @Test
  void closingTagOutOfAnAllowedScriptIsRejected() {
    assertRejected("</script>");
    assertRejected("text</ script >");
    assertRejected("<script src=\"/weblib/custom.js\"></script></script>");
  }

  /**
   * The reason of a rejection tells what is wrong with the script, so that the writer of a content
   * can fix it. In particular, a self-closing script tag isn't closed in HTML: the browsers read the
   * rest of the document as the code of the script.
   */
  @Test
  void rejectionTellsItsReason() {
    assertRejectedFor("<h1>title</h1><script src=\"/weblib/toto.js\"/><p>text</p>",
        "a script not immediately closed (carrying code or missing its closing tag) in " +
            "\"<script src=\"/weblib/toto.js\"/><p>text</p>\"");
    assertRejectedFor("<script src=\"/weblib/custom.js\">alert(1)</script>",
        "a script not immediately closed (carrying code or missing its closing tag) in " +
            "\"<script src=\"/weblib/custom.js\">alert(1)</script>\"");
    assertRejectedFor("<script>alert(1)</script>",
        "the source of a script isn't allowed in \"<script>alert(1)</script>\"");
    assertRejectedFor("<script src=\"/other/evil.js\"></script>",
        "the source of a script isn't allowed in \"<script src=\"/other/evil.js\"></script>\"");
    assertRejectedFor("<script/src=\"/weblib/custom.js\"></script>",
        "a script that cannot be strictly parsed in \"<script/src=\"/weblib/custom.js\"></script>\"");
    assertRejectedFor("text</script>",
        "a closing script tag out of an allowed script in \"</script>\"");
    // whatever the name of the callback, even one the patterns of the filter don't know
    assertRejectedFor("<script src=\"/weblib/custom.js\" onreadystatechange=\"alert(1)\"></script>",
        "a script with an event handler attribute in \"<script src=\"/weblib/custom.js\" " +
            "onreadystatechange=\"alert(1)\"></script>\"");
    assertThat(checker.rejectionIn("<script src=\"/weblib/custom.js\"></script>"),
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
