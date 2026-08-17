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
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.kernel.test.annotations.TestedBean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.silverpeas.kernel.util.StringUtil.EMPTY;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class HtmlSanitizerTest {

  @TestedBean
  private DefaultHtmlSanitizer service;

  @Test
  void sanitizeNotDefined() {
    assertThat(service.sanitize(null), emptyString());
    assertThat(service.sanitize(EMPTY), emptyString());
  }

  @Test
  void sanitizeText() {
    String payload = "BEFORE<iframe>INSIDE</iframe>AFTER";
    assertThat(service.sanitize(payload), is("BEFOREAFTER"));
    payload = "BEFORE< iframe  >INSIDE</ iframe >AFTER";
    assertThat(service.sanitize(payload),
        is("BEFORE&lt; iframe  &gt;INSIDE&lt;/ iframe &gt;AFTER"));
    payload = "BEFORE< \t iframe>INSIDE</\t iframe\n>AFTER";
    assertThat(service.sanitize(payload),
        is("BEFORE&lt; \t iframe&gt;INSIDE&lt;/\t iframe\n&gt;AFTER"));
  }

  @Test
  void sanitizeIFrame() {
    String iframe = "BEFORE<iframe>INSIDE</iframe>AFTER";
    assertThat(service.sanitize(iframe), is("BEFOREAFTER"));
    iframe = "BEFORE< iframe  >INSIDE</ iframe >AFTER";
    assertThat(service.sanitize(iframe), is("BEFORE&lt; iframe  &gt;INSIDE&lt;/ iframe &gt;AFTER"));
    iframe = "BEFORE< \t iframe>INSIDE</\t iframe\n>AFTER";
    assertThat(service.sanitize(iframe),
        is("BEFORE&lt; \t iframe&gt;INSIDE&lt;/\t iframe\n&gt;AFTER"));
  }

  @Test
  void sanitizeScript() {
    String script =
        "BEFORE<script type=\"text/javascript\">window.alert('Silverpeas')" + "</script>AFTER";
    assertThat(service.sanitize(script), is("BEFOREAFTER"));
    script =
        "BEFORE< script type=\"text/javascript\">window.alert('Silverpeas')" + "</ script >AFTER";
    assertThat(service.sanitize(script),
        is("BEFORE&lt; script type&#61;&#34;text/javascript&#34;&gt;window.alert(&#39;" +
            "Silverpeas&#39;)&lt;/ script &gt;AFTER"));
  }

  @Test
  void sanitizePayloadExploit() {
    final String payload = "<html><body onload=\"document.forms0.submit();\"><form " +
        "action=\"http://server/users/1/update\" method=\"GET\"><input type=\"hidden\" " +
        "name=\"userId\" value=\"2\" /><input type=\"hidden\" name=\"userLastName\" " +
        "value=\"Toto\" /><input type=\"hidden\" name=\"userAccessLevel\" value=\"ADMINISTRATOR\"" +
        " /><input type=\"hidden\" name=\"X-STKN\" value=\"ZKWXYZ\" /></form><span>test</span><a " +
        "href=\"http://server/users/1\"></a></body></html>";
    assertThat(service.sanitize(payload),
        is("test<a href=\"http://server/users/1\" rel=\"noopener noreferrer nofollow\" " +
            "target=\"_blank\"></a>"));
  }

  /**
   * The user notifications carry the thumbnail of a contribution as an image directly inlined
   * into their content, and this whatever the channel by which they are then distributed.
   */
  @Test
  void sanitizeInlinedImage() {
    String img = "<img src=\"data:image/png;base64,iVBORw0KGgo=\" alt=\"a thumbnail\" " +
        "height=\"60\"/>AFTER";
    assertThat(service.sanitize(img),
        is("<img src=\"data:image/png;base64,iVBORw0KGgo&#61;\" alt=\"a thumbnail\" " +
            "height=\"60\" />AFTER"));
    img = "<img src=\"data:image/jpeg;base64,/9j+AA==\" alt=\"a thumbnail\"/>AFTER";
    assertThat(service.sanitize(img),
        is("<img src=\"data:image/jpeg;base64,/9j&#43;AA&#61;&#61;\" alt=\"a thumbnail\" />AFTER"));
  }

  /**
   * Only raster images can be inlined. An SVG document can carry scripts, and a data URI must not
   * be a way to smuggle any other type of content.
   */
  @Test
  void sanitizeInlinedContentThatIsNotARasterImage() {
    String img = "<img src=\"data:image/svg+xml;base64,PHN2Zz4=\" alt=\"exploit\"/>AFTER";
    assertThat(service.sanitize(img), is("<img alt=\"exploit\" />AFTER"));
    img = "<img src=\"data:text/html;base64,PHNjcmlwdD4=\" alt=\"exploit\"/>AFTER";
    assertThat(service.sanitize(img), is("<img alt=\"exploit\" />AFTER"));
    // with no attribute left to keep, the element itself is dropped
    img = "<img src=\"data:image/png;base64,AAA<script>window.alert(1)</script>\"/>AFTER";
    assertThat(service.sanitize(img), is("AFTER"));
  }

  /**
   * Inlining is allowed for the images only: a link must not be able to carry its own content.
   */
  @Test
  void sanitizeInlinedContentOfALink() {
    final String link = "<a href=\"data:text/html;base64,PHNjcmlwdD4=\">exploit</a>";
    assertThat(service.sanitize(link), is("exploit"));
  }
}