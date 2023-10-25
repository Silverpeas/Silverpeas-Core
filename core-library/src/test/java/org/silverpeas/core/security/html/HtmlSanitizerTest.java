/*
 * Copyright (C) 2000 - 2023 Silverpeas
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
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.TestedBean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.silverpeas.core.util.StringUtil.EMPTY;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
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
}