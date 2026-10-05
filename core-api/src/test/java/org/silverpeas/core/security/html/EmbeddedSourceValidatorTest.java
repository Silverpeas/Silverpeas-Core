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
 * Unit tests on the validation of the source of a resource embedded within a content. By default,
 * the files attached to the contributions are allowed as any other resource Silverpeas hosts; a
 * validator can be asked to reject them, which is expected for the scripts.
 * @author mmoquillon
 */
class EmbeddedSourceValidatorTest {

  private static final String ATTACHED_FILE =
      "attached_file/componentId/kmelia1/attachmentId/7088b9d6/lang/fr/name/custom.js";
  private static final List<String> ALLOWED_HOSTS =
      List.of("www.youtube.com", "scripts.example.org", "silverpeas.example.org");

  private final EmbeddedSourceValidator validator =
      new EmbeddedSourceValidator(ALLOWED_HOSTS, "/silverpeas");

  private final EmbeddedSourceValidator validatorRejectingAttachedFiles =
      new EmbeddedSourceValidator(ALLOWED_HOSTS, "/silverpeas", false);

  @Test
  void sourceWithinThePlatformOrOnAnAllowedHostIsAllowed() {
    assertAllowed("/silverpeas/util/javaScript/silverpeas.js");
    assertAllowed("util/javaScript/custom.js");
    assertAllowed("/weblib/js/custom.js");
    assertAllowed("https://scripts.example.org/lib/widget.js");
    // a path merely looking like the one of the attached files
    assertAllowed("/silverpeas/attached_files/custom.js");
    assertAllowed("/silverpeas/util/attached_file/custom.js");
    assertAllowed("/weblib/attached_file/custom.js");
    assertAllowed("util/File/custom.js");
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

  @Test
  void sourceOutOfThePlatformAndOfTheAllowedHostsIsRejected() {
    assertRejected(null);
    assertRejected("/other/evil.js");
    assertRejected("/silverpeas/../other/evil.js");
    assertRejected("https://www.evil.org/evil.js");
    assertRejected("http://scripts.example.org/lib/widget.js");
    assertRejected("/silverpeas/../" + ATTACHED_FILE);
  }

  /**
   * The files attached to the contributions are uploaded by the users themselves: they are rejected
   * as the source of a script.
   */
  @Test
  void attachedFileIsRejectedWhenNotAllowed() {
    assertRejectedForScripts("/silverpeas/" + ATTACHED_FILE);
    assertRejectedForScripts("/silverpeas/" + ATTACHED_FILE + "?v=2");
    assertRejectedForScripts(ATTACHED_FILE);
    assertRejectedForScripts("./" + ATTACHED_FILE);
    assertRejectedForScripts("https://silverpeas.example.org/silverpeas/" + ATTACHED_FILE);
    assertRejectedForScripts("https://silverpeas.example.org:8443/silverpeas/" + ATTACHED_FILE);
  }

  /**
   * An attached file isn't served by its sole own URL: its permalink redirects to it and the other
   * file servers of Silverpeas serve the files uploaded by the users as well.
   */
  @Test
  void fileServedByAnotherFileServerIsRejectedWhenNotAllowed() {
    assertRejectedForScripts("/silverpeas/File/7088b9d6");
    assertRejectedForScripts("/silverpeas/Document/42");
    assertRejectedForScripts("/silverpeas/FileServer/custom.js?ComponentId=kmelia1&SourceFile=1.js");
    assertRejectedForScripts("/silverpeas/OnlineFileServer/custom.js?ComponentId=kmelia1");
    assertRejectedForScripts("/silverpeas/TempFileServer/custom.js");
    assertRejectedForScripts("File/7088b9d6");
  }

  /**
   * The path is read the way the server reads it: once decoded, without the parameters a path
   * segment can carry and whatever its empty and dot segments.
   */
  @Test
  void attachedFileIsRejectedHoweverItsPathIsWritten() {
    assertRejectedForScripts("/silverpeas/attached%5Ffile/componentId/kmelia1/name/custom.js");
    assertRejectedForScripts("/silverpeas/%61ttached_file/componentId/kmelia1/name/custom.js");
    assertRejectedForScripts("/silverpeas/attached_file;v=1/componentId/kmelia1/name/custom.js");
    assertRejected("/silverpeas;v=1/attached_file/componentId/kmelia1/name/custom.js");
    assertRejectedForScripts("/silverpeas//attached_file/componentId/kmelia1/name/custom.js");
    assertRejected("//silverpeas.example.org/silverpeas/" + ATTACHED_FILE);
    assertRejectedForScripts("/silverpeas/./attached_file/componentId/kmelia1/name/custom.js");
    assertRejectedForScripts("/silverpeas/.;v=1/attached_file/componentId/kmelia1/name/custom.js");
  }

  /**
   * The name of the file ending the URL of an attached file is free: the file actually served is
   * found out from its identifier. So an attached file is rejected whatever its apparent type.
   */
  @Test
  void attachedFileIsRejectedWhateverItsNameWhenNotAllowed() {
    final String attachedFile = "/silverpeas/attached_file/componentId/kmelia1/attachmentId/" +
        "7088b9d6/lang/fr/name/";
    assertRejectedForScripts(attachedFile + "page.html");
    assertRejectedForScripts(attachedFile + "document.pdf");
    assertRejectedForScripts(attachedFile + "image.png");
  }

  /**
   * It is the case of the media and of the iframes: an attached file is the usual source of the
   * images of a content and the PDF documents are commonly embedded. And it is the case of the
   * scripts once the attached files are explicitly allowed for them.
   */
  @Test
  void attachedFileIsAllowedByDefault() {
    assertAllowed("/silverpeas/" + ATTACHED_FILE);
    assertAllowed(ATTACHED_FILE);
    assertAllowed("/silverpeas/File/7088b9d6");
    assertAllowed("https://silverpeas.example.org/silverpeas/" + ATTACHED_FILE);
    final EmbeddedSourceValidator explicitlyAllowing =
        new EmbeddedSourceValidator(ALLOWED_HOSTS, "/silverpeas", true);
    assertThat(explicitlyAllowing.isAllowed("/silverpeas/" + ATTACHED_FILE), is(true));
  }

  @Test
  void attachedFileOfASilverpeasAtTheRootOfTheServerIsRejectedWhenNotAllowed() {
    final EmbeddedSourceValidator atRoot = new EmbeddedSourceValidator(List.of(), "/", false);
    assertThat(atRoot.isAllowed("/util/javaScript/silverpeas.js"), is(true));
    assertThat(atRoot.isAllowed("/" + ATTACHED_FILE), is(false));
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

  /**
   * Asserts the source is rejected whether the attached files are allowed or not.
   */
  private void assertRejected(final String src) {
    assertThat(src, validator.isAllowed(src), is(false));
    assertThat(src, validatorRejectingAttachedFiles.isAllowed(src), is(false));
  }

  /**
   * Asserts the source is rejected only when the attached files aren't allowed.
   */
  private void assertRejectedForScripts(final String src) {
    assertThat(src, validator.isAllowed(src), is(true));
    assertThat(src, validatorRejectingAttachedFiles.isAllowed(src), is(false));
  }
}
