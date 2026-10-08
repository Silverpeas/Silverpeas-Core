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
package org.silverpeas.core.security.html;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests on the validation of the source of a resource embedded within a content.
 * @author mmoquillon
 */
class EmbeddedSourceValidatorTest {

  private final EmbeddedSourceValidator validator =
      new EmbeddedSourceValidator(List.of("www.youtube.com"), "/silverpeas");

  @Test
  void relativeSourceWithinSilverpeasIsAllowed() {
    assertAllowed("/silverpeas/attached_file/componentId/kmelia1/attachmentId/2/lang/fr/name/x.png");
    assertAllowed("/silverpeas");
    assertAllowed("Rkmelia/kmelia1/Main");
    assertAllowed("./Main.jsp#top");
    assertAllowed("?id=3");
  }

  @Test
  void httpsSourceOnAnAllowedHostIsAllowed() {
    assertAllowed("https://www.youtube.com/embed/xyz");
    assertAllowed("HTTPS://WWW.YOUTUBE.COM/embed/xyz");
    assertAllowed("https://www.youtube.com/embed?a=1&b=2");
  }

  /**
   * The browsers accept a URL with some characters forbidden by the URI syntax, a whitespace for
   * example, by encoding them before sending the request. The name of a file is commonly made
   * of such characters, so a source referring it is as legitimate as its encoded counterpart.
   */
  @Test
  void sourceWithCharactersTheBrowsersEncodeIsCheckedAsEncoded() {
    assertAllowed("/silverpeas/attached_file/componentId/kmelia1/attachmentId/2/lang/fr/name/my " +
        "image.png");
    assertAllowed("/silverpeas/attached_file/componentId/kmelia1/attachmentId/2/lang/fr/name/my" +
        "%20image.png");
    assertAllowed("/silverpeas/attached_file/componentId/kmelia1/attachmentId/2/lang/fr/name/" +
        "photo [1] {draft}.png");
    assertAllowed("/silverpeas/attached_file/componentId/kmelia1/attachmentId/2/lang/fr/name/" +
        "a|b^c`d\"e<f>g.png");
    assertAllowed("/silverpeas/FileServer?name=my image.png");
    assertAllowed("https://www.youtube.com/my image.png");
    assertAllowed("https://www.youtube.com/path?name=my image.png");
  }

  @Test
  void sourceWithSuchCharactersStillObeysTheRule() {
    assertRejected("/other/my image.png");
    assertRejected("/silverpeas/../other/my image.png");
    assertRejected("/silverpeas/File?path=../etc/my file");
    assertRejected("https://www.evil.org/my image.png");
    assertRejected("https://www.youtube.com @www.evil.org/");
    assertRejected("https://www.you tube.com/x.png");
    assertRejected("http://www.youtube.com/my image.png");
    assertRejected("javascript:alert(1) ");
  }

  @Test
  void sourceWithBackslashesIsRejectedAsTheBrowsersReadThemAsSlashes() {
    assertRejected("\\\\www.evil.org/x.png");
    assertRejected("/\\www.evil.org/x.png");
    assertRejected("https:\\\\www.evil.org/x.png");
    assertRejected("https://www.youtube.com\\@www.evil.org/");
  }

  @Test
  void sourceWithControlCharactersIsRejected() {
    assertRejected("https://www.youtube.com/x\n.png");
    assertRejected("https://www.yout\tube.com/x.png");
    assertRejected("/silverpeas/x\u0000.png");
  }

  @Test
  void relativeSourceOutsideSilverpeasIsRejected() {
    assertRejected("/other/app");
    assertRejected("/silverpeasother/app");
    assertRejected("///www.evil.org/");
    assertRejected("/silverpeas/../other/app");
    assertRejected("../other/app");
    assertRejected("/silverpeas/%2e%2e/other");
    assertRejected("/silverpeas/File?path=%2e%2e/etc/passwd");
  }

  @Test
  void nonHttpsOrNonAllowedHostSourceIsRejected() {
    assertRejected("http://www.youtube.com/embed/xyz");
    assertRejected("//www.youtube.com/embed/xyz");
    assertRejected("https://www.evil.org/");
    assertRejected("https://www.youtube.com.evil.org/");
    assertRejected("https://www.youtube.com@www.evil.org/");
    assertRejected("javascript:alert(1)");
    assertRejected("data:text/html,hello");
    assertRejected(null);
  }

  @Test
  void anyHostCanBeAllowed() {
    final EmbeddedSourceValidator anyHost =
        new EmbeddedSourceValidator(List.of(EmbeddedSourceValidator.ANY_HOST), "/silverpeas");
    assertThat(anyHost.isAllowed("https://www.evil.org/my image.png"), is(true));
    assertThat(anyHost.isAllowed("http://www.evil.org/x.png"), is(false));
  }

  private void assertAllowed(final String src) {
    assertThat(src, validator.isAllowed(src), is(true));
  }

  private void assertRejected(final String src) {
    assertThat(src, validator.isAllowed(src), is(false));
  }
}
