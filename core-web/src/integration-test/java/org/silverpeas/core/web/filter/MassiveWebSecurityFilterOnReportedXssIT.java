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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.integration.rule.DbSetupRule;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.test.WarBuilder4WebCore;
import org.silverpeas.kernel.util.Pair;
import org.silverpeas.web.test.stub.TestHttpRequest;
import org.silverpeas.web.test.stub.TestHttpResponse;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Checks the {@link MassiveWebSecurityFilter} rejects the stored XSS payloads reported against
 * Silverpeas 6.4.6 by the issues #1458 (login question), #1459 (whitePages card wysiwyg field) and
 * #1460 (forum message title and body).
 * <p>
 * Those reports were produced against the 6.4.6 tag, in which the XSS patterns of the filter only
 * covered {@code <script} and {@code <iframe}. The patterns have been enriched since then (see the
 * fix of the bug #15225) with the {@code svg}, {@code math} and {@code details} elements and with
 * the event handler attribute declarations, which are exactly what the three reported payloads rely
 * upon. This test asserts the injection requests are now answered a 403 instead of being persisted.
 * </p>
 * <p>
 * Beware this filter is an input guard, not an output encoding: it narrows the reachability of the
 * flaws but it doesn't fix the rendering code itself.
 * </p>
 * @author Miguel Moquillon
 */
@RunWith(Arquillian.class)
public class MassiveWebSecurityFilterOnReportedXssIT {

  private static final String DATASET_SCRIPT =
      "/org/silverpeas/core/web/filter/massive-web-security-database.sql";

  /**
   * The SQL injection checks, enabled along with the XSS ones, look the table names up into the
   * database, hence the dataset.
   */
  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom(DATASET_SCRIPT);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4WebCore.onWarForTestClass(MassiveWebSecurityFilterOnReportedXssIT.class)
        .addRESTWebServiceEnvironment()
        .testFocusedOn(
            warBuilder -> warBuilder.addPackages(true, "org.silverpeas.core.web.filter"))
        .build();
  }

  private static final String MULTIPART = "multipart/form-data; boundary=----SilverpeasBoundary";

  /**
   * Payload of the issues #1458 and #1459: an event handler attribute carried by an element the
   * browser fails to load on purpose.
   */
  private static final String IMG_ONERROR_PAYLOAD =
      "<img src=x onerror=\"window.__xss='poc-exec'\">";

  /**
   * Payload of the issue #1460 for the message body: both a forbidden element and an event handler
   * attribute.
   */
  private static final String SVG_ONLOAD_PAYLOAD =
      "<p>poc-body</p><svg onload=\"window.__xss_body='body-exec'\"></svg>";

  /**
   * Issue #1458: the login question is written through the profile update form and rendered, with
   * no encoding, on the anonymous password reminder page.
   */
  @Test
  public void loginQuestionInjectionOfIssue1458IsRejected() {
    assertBlocked(post("RMyProfil/jsp/UpdateMyInfos").addParameter("userLoginQuestion",
        IMG_ONERROR_PAYLOAD));
  }

  /**
   * Issue #1459: the wysiwyg fields of a whitePages card are written through the card creation form
   * and rendered as raw HTML by the wysiwyg field displayer.
   */
  @Test
  public void whitePagesCardInjectionOfIssue1459IsRejected() {
    assertBlocked(post("RwhitePages/whitePages3/effectiveCreate").addParameter(
        "competence", IMG_ONERROR_PAYLOAD));
  }

  /**
   * Issue #1460: both the title and the body of a forum message are rendered by JSP scriptlets with
   * no encoding. Each injection point is asserted on its own as a single request carrying only one
   * of them has to be rejected as well.
   */
  @Test
  public void forumMessageInjectionsOfIssue1460AreRejected() {
    assertBlocked(
        post("Rforums/forums4/viewForum.jsp").addParameter("messageTitle", IMG_ONERROR_PAYLOAD));
    assertBlocked(
        post("Rforums/forums4/viewForum.jsp").addParameter("messageText", SVG_ONLOAD_PAYLOAD));
  }

  /**
   * An event callback declaration isn't necessarily preceded by a whitespace. According to the HTML
   * tokenizer, an attribute name is also expected right after the closing quote of the previous
   * attribute value (a <i>missing-whitespace-between-attributes</i> parse error, whose recovery is
   * mandated by the specification) and right after a solidus. Hence the payloads below all declare
   * an {@code onerror} callback the browsers do run, but for the last one: a solidus terminates
   * neither an unquoted attribute value, so {@code src} there merely takes the whole
   * {@code x/onerror=alert(1)} string as its value and no callback is declared at all. It is
   * nonetheless expected to be rejected, as narrowing the detection to only the harmful forms would
   * buy nothing.
   */
  @Test
  public void eventCallbacksAreDetectedWhateverTheirPrecedingSeparator() {
    assertBlocked(injection("<img src=x onerror=alert(1)>"));
    assertBlocked(injection("<img src=\"x\"onerror=alert(1)>"));
    assertBlocked(injection("<img src='x'onerror=alert(1)>"));
    assertBlocked(injection("<img src=\"x\"/onerror=alert(1)>"));
    assertBlocked(injection("<img/src=x/onerror=alert(1)>"));
  }

  /**
   * The detection expects one of the characters after which the HTML tokenizer does await an
   * attribute name. A character which never plays such a role, like the question mark or the
   * ampersand of a query string, doesn't make an event callback out of the text that follows.
   * <p>
   * Be aware the detection remains a coarse one: as the {@code on} prefix is followed by any word
   * characters, a text such as {@code "a value only=42"} is caught whereas it declares nothing.
   * This is the behaviour of this filter since the fix of the bug #15225 and it is kept as is, a
   * false rejection being here preferred to a missed injection.
   * </p>
   */
  @Test
  public void aCallbackIsExpectedToFollowAnAttributeSeparator() {
    assertNotBlocked(injection("onerror=alert(1)"));
    assertNotBlocked(injection("https://www.silverpeas.org/page?online=true"));
  }

  private TestHttpRequest injection(final String payload) {
    return post("RMyProfil/jsp/UpdateMyInfos").addParameter("userLoginQuestion", payload);
  }

  /**
   * The three reported injections are performed by the genuine forms, which are all submitted as
   * multipart/form-data streams. The filter skips its checks on multipart contents only for the web
   * services and for the webPages component, so those routers have to be checked all the same. This
   * relies on {@link HttpRequest#getParameterMap()} taking the multipart form fields into account.
   */
  @Test
  public void reportedInjectionsAreRejectedEvenWhenSubmittedAsMultipart() {
    assertBlocked(multipart("RMyProfil/jsp/UpdateMyInfos").addParameter("userLoginQuestion",
        IMG_ONERROR_PAYLOAD));
    assertBlocked(multipart("RwhitePages/whitePages3/effectiveCreate").addParameter("competence",
        IMG_ONERROR_PAYLOAD));
    assertBlocked(
        multipart("Rforums/forums4/viewForum.jsp").addParameter("messageText", SVG_ONLOAD_PAYLOAD));
  }

  /**
   * Delimits the multipart exemption the above test relies upon: the very same request is exempted
   * only once it targets the webPages component and it carries a multipart content.
   */
  @Test
  public void multipartExemptionAppliesToWebPagesOnly() {
    assertNotBlocked(
        multipart("RwebPages/webPages1/Update").addParameter("aParameter", IMG_ONERROR_PAYLOAD));
    assertBlocked(
        post("RwebPages/webPages1/Update").addParameter("aParameter", IMG_ONERROR_PAYLOAD));
    assertBlocked(
        multipart("RMyProfil/jsp/UpdateMyInfos").addParameter("aParameter", IMG_ONERROR_PAYLOAD));
  }

  /**
   * Builds the URI of the given path the same way the filter computes the prefixes it exempts, so
   * that the test doesn't depend on how the application URL is set up.
   */
  private String uriOf(final String path) {
    return UriBuilder.fromUri(URLUtil.getApplicationURL()).path(path).build().toString();
  }

  private TestHttpRequest post(final String path) {
    return new TestHttpRequest("POST", uriOf(path));
  }

  private TestHttpRequest multipart(final String path) {
    return new TestHttpRequest("POST", uriOf(path)).addHeader("Content-Type", MULTIPART);
  }

  private void assertBlocked(final TestHttpRequest request) {
    Pair<Integer, String> status = filter(request);
    assertThat(status.getFirst(), is(HttpServletResponse.SC_FORBIDDEN));
    assertThat(status.getSecond().contains("XSS"), is(true));
  }

  private void assertNotBlocked(final TestHttpRequest request) {
    assertThat(filter(request).getFirst(), not(HttpServletResponse.SC_FORBIDDEN));
  }

  private Pair<Integer, String> filter(final TestHttpRequest request) {
    TestHttpResponse response = new TestHttpResponse();
    try {
      new MassiveWebSecurityFilter().doFilter(HttpRequest.decorate(request), response,
          (servletRequest, servletResponse) -> {
            // the request reaches the resource only if no injection has been detected
          });
    } catch (Exception e) {
      throw new AssertionError("Unexpected failure of the security filter", e);
    }
    return response.getActualStatus();
  }
}
